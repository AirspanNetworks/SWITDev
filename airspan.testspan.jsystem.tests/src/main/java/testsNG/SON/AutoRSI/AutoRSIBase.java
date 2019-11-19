package testsNG.SON.AutoRSI;

import java.io.File;
import java.net.InetAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import Attenuators.AttenuatorSet;
import EnodeB.EnodeB;
import Netspan.EnbProfiles;
import Netspan.NetspanServer;
import Netspan.API.Enums.EnbStates;
import Netspan.API.Lte.AlarmInfo;
import Netspan.API.Lte.PciStatusCell;
import Netspan.API.Lte.RsiStatusCell;
import Netspan.API.Lte.SONStatus;
import Netspan.Profiles.SonParameters;
import UE.UE;
import Utils.*;
import Utils.Snmp.MibReader;
import jsystem.framework.ParameterProperties;
import jsystem.framework.report.Reporter;
import testsNG.TestspanTest;
import testsNG.Actions.AlarmsAndEvents;
import testsNG.Actions.EnodeBConfig;
import testsNG.Actions.Neighbors;
import testsNG.Actions.PeripheralsConfig;
import testsNG.Actions.Statuses;
import testsNG.Actions.Traffic;
import testsNG.Actions.Utils.TrafficGeneratorType;

public class AutoRSIBase extends TestspanTest {
	protected EnodeB dut;
	protected EnodeBConfig enodeBConfig;
	protected NetspanServer netspan;
	protected PeripheralsConfig peripheralsConfig;
	protected MibReader mibReader;
	protected Statuses status;
	protected Traffic traffic;
	protected InetAddress addr;
	protected UE testUE;
	protected ArrayList<UE> testUEs;
	protected Neighbors neighborsUtils;
	protected AlarmsAndEvents alarmsAndEvents;
	protected ArrayList<EnodeB> list3Party = new ArrayList<EnodeB>();
	protected DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss a");
	protected AttenuatorSet attenuatorSetUnderTest;
	protected boolean rsiInitAutoStatus;
	protected boolean pciInitAutoStatus;
	protected String initSonProfileName;
	protected ArrayList<Integer> initRsiList = new ArrayList<>();
	protected Neighbors neighbors;

	@Override
	public void init() throws Exception {
		netspanServer = NetspanServer.getInstance();
		enbInTest = new ArrayList<>();
		enbInTest.add(dut);
		enodeBConfig = EnodeBConfig.getInstance();
		netspan = NetspanServer.getInstance();
		mibReader = MibReader.getInstance();
		status = Statuses.getInstance();
		traffic = Traffic.getInstance(SetupUtils.getInstance().getAllUEs());
		addr = InetAddress.getLocalHost();
		neighborsUtils = Neighbors.getInstance();
		alarmsAndEvents = AlarmsAndEvents.getInstance();
		peripheralsConfig = PeripheralsConfig.getInstance();
		neighbors = Neighbors.getInstance();
		rsiInitAutoStatus = true;
		pciInitAutoStatus = true;

		super.init();

		dut.setCellContextNumber(1);
		if (traffic.getGeneratorType() == TrafficGeneratorType.TestCenter) {
			String TrafficFile = traffic.getTg().getDefaultConfigTccFile();
			int lastDot = TrafficFile.lastIndexOf('.');
			String HOTrafficFile = TrafficFile.substring(0, lastDot) + "_HO" + TrafficFile.substring(lastDot);
			traffic.configFile = new File(HOTrafficFile);
		}

		GeneralUtils.startLevel("Stop UEs");
		for (UE ue : SetupUtils.getInstance().getAllUEs()) {
			if (!ue.stop()) {
				report.report("Failed to stop ue " + ue.getName());
			}
			else {
				report.report("stopped ue " + ue.getName());
			}
		}
		GeneralUtils.stopLevel();

		GeneralUtils.unSafeSleep(5000);
		testUE = SetupUtils.getInstance().getDynamicUEs().get(0);

		if (!testUE.start())
			report.report("Failed to start ue " + testUE.getName());
		else
			report.report("started ue " + testUE.getName());
		testUEs = new ArrayList<>();
		testUEs.add(testUE);

		attenuatorSetUnderTest = AttenuatorSet.getAttenuatorSet(CommonConstants.ATTENUATOR_SET_NAME);
		peripheralsConfig.SetAttenuatorToMin(attenuatorSetUnderTest);

		for (EnodeB enodeB : enbInSetup) {
			if (enodeB != dut) {
				peripheralsConfig.changeEnbState(enodeB, EnbStates.OUT_OF_SERVICE);
			}
		}

		GeneralUtils.startLevel("Pre test");
		SONStatus sonStatus = netspan.getSONStatus(dut);
		initSonProfileName = netspan.getCurrentSonProfileName(dut);
		if (sonStatus != null) {
			for (RsiStatusCell rsi : sonStatus.RSICell) {
				GeneralUtils.startLevel("Cell ID: " + rsi.cellId);
				initRsiList.add(Integer.valueOf(rsi.currentRsiValue));
				report.report("Cell ID: " + rsi.cellId + ", RSI Status: " + rsi.rsiStatus + ", RSI value: "
						+ rsi.currentRsiValue);
				if (!rsi.rsiStatus.equals("Manual")) {
					report.report("Cell ID " + rsi.cellId + ": NMS show incorrect RSI status in SON status: "
							+ rsi.rsiStatus + ", changing to Manual)");
					rsiInitAutoStatus = false;
				}
				GeneralUtils.stopLevel();
			}
			if(!sonStatus.pciStatus.equals("Manual")){
				report.report("Changing AutoPci state to Manual)");
				pciInitAutoStatus = false;
			}
			if (!rsiInitAutoStatus || !pciInitAutoStatus) {
				configureAutoRSIToDisableViaNms();
			}
		}

		GeneralUtils.startLevel("Verify no neighbours");
		if (!netspan.verifyNoNeighbors(dut)) {
			report.report(dut.getName() + " has neighbours, removing them.");
			neighbors.deleteAllNeighbors(dut);
		}
		report.reportHtml("db get nghList", dut.lteCli("db get nghList"), true);
		GeneralUtils.stopLevel();

		GeneralUtils.stopLevel();
	}

	@Override
	public void end() {
		GeneralUtils.startLevel("After test");

		GeneralUtils.startLevel("Start UEs");
		for (UE ue : SetupUtils.getInstance().getAllUEs()) {
			if (!ue.start())
				report.report("Failed to start ue " + ue.getName());
		}
		GeneralUtils.stopLevel();

		for (EnodeB enodeB : enbInSetup) {
			if (enodeB != dut) {
				peripheralsConfig.changeEnbState(enodeB, EnbStates.IN_SERVICE);
			}
		}
		for (EnodeB enodeB : enbInSetup) {
			enodeB.waitForAllRunningAndInService(EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
		}

		String currentSonProfile = netspan.getCurrentSonProfileName(dut);
		if (!currentSonProfile.equals(initSonProfileName)) {
			boolean res = netspanServer.setProfile(dut, EnbProfiles.Son_Profile, initSonProfileName);
			if (!res) {
				report.report("Failed setting to original son profile!", Reporter.WARNING);
			}
		}

		for (int i = 0; i < initRsiList.size(); i++) {
			dut.setCellContextNumber(i + 1);
			report.report("Changing to different RSI as SW bug workaround.", Reporter.WARNING);
			boolean res = enodeBConfig.changeEnbCellRSI(dut, initRsiList.get(i) + 1); // bug
																						// workaround
			res = enodeBConfig.changeEnbCellRSI(dut, initRsiList.get(i));
			if (!res) {
				report.report("Failed setting Cell #" + (i + 1) + " to original RSI value!", Reporter.WARNING);
			}
		}

		SONStatus sonStatus = netspan.getSONStatus(dut);
		printRSIPCIDebug(sonStatus);
		GeneralUtils.stopLevel();
		super.end();

	}

	private void printRSIPCIDebug(SONStatus sonStatus) {

		if (sonStatus == null) {
			report.report("Received son status with null value");
		} else {
			GeneralUtils.startLevel("RSI Status");
			for (RsiStatusCell cell : sonStatus.RSICell) {
				GeneralUtils.startLevel("Cell ID: " + cell.cellId);
				report.report("Current RSI Status: " + cell.rsiStatus);
				report.report("Current RSI Value: " + cell.currentRsiValue);
				GeneralUtils.stopLevel();
			}
			GeneralUtils.stopLevel();
			GeneralUtils.startLevel("PCI Status");
			report.report("Current PCI Status: " + sonStatus.pciStatus);
			for (PciStatusCell cell : sonStatus.PCICells) {
				GeneralUtils.startLevel("Cell ID: " + cell.cellId);
				report.report("Current PCI Value: " + cell.physicalCellId);
				GeneralUtils.stopLevel();
			}
			GeneralUtils.stopLevel();
		}
	}

	@ParameterProperties(description = "Name of Enb")
	public void setDUT(String dut) {
		ArrayList<EnodeB> temp = (ArrayList<EnodeB>) SysObjUtils.getInstnce().initSystemObject(EnodeB.class, false,
				dut);
		this.dut = temp.get(0);
	}

	protected boolean printRSISONStatus(SONStatus SONStatus, String rsiStatusExpected, Integer currentRsiValueExpected,
			int logLevel) {
		boolean flag = true;
		if (SONStatus == null) {
			return false;
		}
		GeneralUtils.startLevel("Print RSI Status values");
		report.reportHtml("db get AutoRsi", dut.lteCli("db get AutoRsi"), true);
		report.reportHtml("db get AutoPciCell", dut.lteCli("db get AutoPciCell"), true);
		report.reportHtml("db get cellcfg cellid", dut.lteCli("db get CellCfg cellid"), true);
		for (RsiStatusCell rsi : SONStatus.RSICell) {
			if (rsi.cellId == String.valueOf(dut.getCellContextID())) {
				GeneralUtils.startLevel("Cell ID: " + rsi.cellId);
				report.report("RSI Status: " + rsi.rsiStatus);
				report.report("Current RSI Value: " + rsi.currentRsiValue);
				report.report("Available RSI Ranges: " + rsi.availableRsiRanges);
				if (rsiStatusExpected != null && !rsi.rsiStatus.equals(rsiStatusExpected)) {
					report.report("Cell ID " + rsi.cellId + ": NMS show incorrect RSI status in SON status (Actual: "
							+ rsi.rsiStatus + ", Expected: " + rsiStatusExpected + ")", logLevel);
					flag = false;
				}
				if (currentRsiValueExpected != null
						&& rsi.currentRsiValue.intValue() != currentRsiValueExpected.intValue()) {
					report.report("Cell ID " + rsi.cellId + ": NMS show incorrect RSI Value in SON status (Actual: "
							+ rsi.currentRsiValue + ", Expected: " + currentRsiValueExpected + ")", logLevel);
					flag = false;
				}
				GeneralUtils.stopLevel();
			}
		}
		GeneralUtils.stopLevel();
		return flag;
	}

	protected void printPCISONStatus(SONStatus SONStatus, String pciStatusExpected, Integer pciValueExpected) {
		if (SONStatus == null) {
			return;
		}
		try {
			report.startLevel("Print PCI Status values");
			report.reportHtml("db get AutoPciCell", dut.lteCli("db get AutoPciCell"), true);
			report.reportHtml("db get cellcfg cellid", dut.lteCli("db get CellCfg cellid"), true);
			if (pciStatusExpected != null && !SONStatus.pciStatus.equals(pciStatusExpected)) {
				report.report("PCI Status: '" + SONStatus.pciStatus + "' (Expected: '" + pciStatusExpected + "')",
						Reporter.FAIL);
				reason = "PCI Status: '" + SONStatus.pciStatus + "' (Expected: '" + pciStatusExpected + "')";
			} else {
				report.report("PCI Status: '" + SONStatus.pciStatus + "'");
			}
			for (int i = 0; i < SONStatus.PCICells.size(); i++) {
				PciStatusCell cellSonStatus = SONStatus.PCICells.get(i);
				report.startLevel("Cell ID: " + cellSonStatus.cellId);
				report.report("Physical Layer Cell Group: " + cellSonStatus.physicalLayerCellGroup);
				report.report("Physical Layer Identity: " + cellSonStatus.physicalLayerIdentity);
				report.report("Physical Cell ID (PCI): " + cellSonStatus.physicalCellId);
				report.report("PCI Status: " + cellSonStatus.pciStatus);
				if (pciValueExpected != null && i == 0
						&& cellSonStatus.physicalCellId.intValue() != pciValueExpected.intValue()) {
					report.report("Physical Cell ID (PCI): " + cellSonStatus.physicalCellId + " (Expected: "
							+ pciValueExpected + ")", Reporter.FAIL);
				}
				report.stopLevel();
			}
			report.stopLevel();
		} catch (Exception e) {
			e.printStackTrace();
			report.report("Failed to print SONStatus due to: " + e.getMessage(), Reporter.FAIL);
			reason = "Failed to print SONStatus due to: " + e.getMessage();
		}
	}

	protected void reportAboutFindAlarm(String alarmType) {
		List<String> alarmTypes = new ArrayList<String>();
		alarmTypes.add(alarmType);
		List<AlarmInfo> alarms = alarmsAndEvents.getAllAlarmsNodeWithSpecificType(alarmsAndEvents.getAllAlarmsNode(dut),
				alarmTypes);
		if (alarms.isEmpty()) {
			report.report("Alarm: '" + alarmType + "' does not exist", Reporter.FAIL);
			reason = "Alarm: '" + alarmType + "' does not exist";
		} else
			for (AlarmInfo alarm : alarms)
				alarmsAndEvents.printAlarmInfo(alarm);
	}

	protected boolean configureAutoPciAndAutoRsiToEnableViaNms(List<Pair<Integer, Integer>> pciRangesList,
			List<Triple<Integer, Integer, Integer>> rsiRangesList) {
		GeneralUtils.startLevel("Configure SON Profile via NMS");
		SonParameters sonParams = new SonParameters();
		if (pciRangesList != null) {
			sonParams.setSonCommissioning(true);
			sonParams.setAutoPCIEnabled(true);
			sonParams.setRangesList(pciRangesList);
			for (Pair<Integer, Integer> p : pciRangesList) {
				report.report(
						"Configure full range of Auto PCI: Start=" + p.getElement0() + ", End=" + p.getElement1());
			}
		}
		if (rsiRangesList != null) {
			sonParams.setSonCommissioning(true);
			sonParams.setAutoRSIEnabled(true);
			sonParams.setRangesRSIList(rsiRangesList);
			if (rsiRangesList.isEmpty()) {
				report.report("Configure Auto RSI with blank list.");
			} else {
				report.report("Configure Auto RSI ranges:");
				for (Triple<Integer, Integer, Integer> t : rsiRangesList) {
					report.report("Start=" + t.getLeftElement() + ", Step=" + t.getMiddleElement() + ", List Size="
							+ t.getRightElement());
				}
			}
		}
		boolean generalFlag = enodeBConfig.cloneAndSetSonProfileViaNetspan(dut, netspan.getCurrentSonProfileName(dut),
				sonParams);
		GeneralUtils.stopLevel();
		return generalFlag;
	}

	protected boolean configureAutoRSIToDisableViaNms() {

		SonParameters sonParams = new SonParameters();
		sonParams.setIsSonCommissioningEnabled(true);
		if(!rsiInitAutoStatus){
			sonParams.setAutoRSIEnabled(false);			
		}
		if(!pciInitAutoStatus){
			sonParams.setAutoPCIEnabled(false);
		}
		GeneralUtils.startLevel(dut.getNetspanName() + " configure "+(!rsiInitAutoStatus?"Auto RSI":"")+
				(!rsiInitAutoStatus && !pciInitAutoStatus?" And ":"")+
				(!pciInitAutoStatus?"AutoPCI":"")+" to Disable via NMS");
		boolean generalFlag = enodeBConfig.cloneAndSetSonProfileViaNetspan(dut, netspan.getCurrentSonProfileName(dut),
				sonParams);
		GeneralUtils.stopLevel();

		return generalFlag;
	}

	protected boolean startTrafficAndConnectUes() {
		GeneralUtils.startLevel("Starting traffic");
		try {
			Traffic.getInstance(SetupUtils.getInstance().getAllUEs()).startTraffic(testUEs);
		} catch (Exception e) {
			report.report("Failed starting traffic", Reporter.WARNING);
			e.printStackTrace();
		}
		report.report("Wait 2 Minutes");

		GeneralUtils.stopLevel();
		GeneralUtils.unSafeSleep(60 * 1000 * 2);

		String reason = peripheralsConfig.checkUesConnection(dut, testUEs, Reporter.WARNING);
		if (!reason.isEmpty()) {
			peripheralsConfig.rebootUEs(testUEs);
			reason = peripheralsConfig.checkUesConnection(dut, testUEs, Reporter.WARNING);
		}
		report.reportHtml("ue show link", dut.lteCli("ue show link"), true);
		report.reportHtml("ue show rate", dut.lteCli("ue show rate"), true);
		return reason.isEmpty();
	}

	protected void checkConnectionUEWithStopStart() {
		if (!peripheralsConfig.checkConnectionUEWithStopStart(testUE, dut)) {
			report.report("UE failed to connect to enodeB", Reporter.FAIL);
			reason = "UE failed to connect to enodeB";
		} else {
			report.report("UE connected to enodeB");
		}
	}

	protected int getInitialPci(SONStatus sonStatus) {
		int initialPci;
		if (sonStatus != null) {
			initialPci = sonStatus.PCICells.get(0).physicalCellId;
		} else {
			report.report("Failed getting initial PCI value from netspan, using SNMP", Reporter.WARNING);
			initialPci = dut.getPci();
		}
		return initialPci;
	}
}

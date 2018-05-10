package testsNG.SON.AutoPCI;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import Attenuators.AttenuatorSet;
import EnodeB.EnodeB;
import Netspan.EnbProfiles;
import Netspan.NetspanServer;
import Netspan.API.Enums.EnabledDisabledStates;
import Netspan.API.Enums.EnbStates;
import Netspan.API.Enums.HandoverType;
import Netspan.API.Enums.HoControlStateTypes;
import Netspan.API.Enums.SonAnrStates;
import Netspan.API.Enums.X2ControlStateTypes;
import Netspan.API.Lte.EventInfo;
import Netspan.API.Lte.PciStatusCell;
import Netspan.API.Lte.RsiStatusCell;
import Netspan.API.Lte.SONStatus;
import Netspan.Profiles.EnodeBAdvancedParameters;
import Netspan.Profiles.RadioParameters;
import Netspan.Profiles.SonParameters;
import TestingServices.TestConfig;
import UE.UE;
import Utils.GeneralUtils;
import Utils.Pair;
import Utils.SetupUtils;
import Utils.SysObjUtils;
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

public class AutoPCIBase extends TestspanTest {
	protected EnodeB dut;
	protected EnodeBConfig enodeBConfig;
	protected NetspanServer netspan;
	protected PeripheralsConfig peripheralsConfig;
	protected MibReader mibReader;
	protected Statuses status;
	protected Traffic traffic;
	protected InetAddress addr;
	protected UE testUE;
	protected ArrayList<UE> testUeList;
	protected Neighbors neighborsUtils;
	protected AlarmsAndEvents alarmsAndEvents;
	protected TestConfig testConfig;
	protected static boolean generalFlag;
	protected static String generalOid;
	protected ArrayList<EnodeB> list3Party = new ArrayList<EnodeB>();
	protected static int threePartyID = 0;
	protected static int startPCI;
	protected static String startPCIstatus = null;
	protected static int START_POWER_ADVANCED_PROFILE = 4;
	private ArrayList<EnodeB> otherEnb;
	private String attenuatorSetName = "rudat_set";
	private AttenuatorSet attenuatorSetUnderTest = null;
	private Date startDate;
	private Date endDate;
	protected int pciStart;
	protected int pciEnd;

	@SuppressWarnings("unchecked")
	@Override
	public void init() throws Exception {
		enbInTest = new ArrayList<>();
		enbInTest.add(dut);
		super.init();
		GeneralUtils.startLevel("Init");
		netspanServer = NetspanServer.getInstance();

		otherEnb = (ArrayList<EnodeB>) enbInSetup.clone();
		otherEnb.remove(dut);

		enodeBConfig = EnodeBConfig.getInstance();
		netspan = NetspanServer.getInstance();
		mibReader = MibReader.getInstance();
		status = Statuses.getInstance();
		traffic = Traffic.getInstance(SetupUtils.getInstance().getAllUEs());
		addr = InetAddress.getLocalHost();
		neighborsUtils = Neighbors.getInstance();
		alarmsAndEvents = AlarmsAndEvents.getInstance();
		peripheralsConfig = PeripheralsConfig.getInstance();
		testConfig = TestConfig.getInstace();
		if (attenuatorSetUnderTest == null) {
			attenuatorSetUnderTest = AttenuatorSet.getAttenuatorSet(attenuatorSetName);
		}
		if (peripheralsConfig.SetAttenuatorToMin(attenuatorSetUnderTest)) {
			report.report("Successfully set minimum attenuation");
		} else {
			report.report("Failed to set minimum attenuation", Reporter.WARNING);
			reason = "Failed to set minimum attenuation";
		}

		SONStatus sonStatus = netspan.getSONStatus(dut);
		printRSIPCIDebug(sonStatus);

		report.report("Change other eNodeBs to out of service");
		for (EnodeB enb : otherEnb) {
			if (peripheralsConfig.changeEnbState(enb, EnbStates.OUT_OF_SERVICE)) {
				report.report(enb.getNetspanName() + " is out of service");
			} else {
				report.report("Failed to set " + enb.getNetspanName() + " out of service.", Reporter.WARNING);
			}
		}
		GeneralUtils.startLevel("Initialize UEs");
		testUE = SetupUtils.getInstance().getDynamicUEs().get(0);
		GeneralUtils.stopLevel();
		testUeList = new ArrayList<UE>();
		testUeList.add(testUE);

		SONStatus startSONStatus = status.getSONStatus(dut);
		if (startSONStatus == null) {
			reason = dut.getNetspanName() + " failed to get SON status Information from Netspan";
			return;
		} else if (startSONStatus.pciStatus != null && startSONStatus.pciStatus.equals("Automatic")) {
			configureAutoPciToDisableViaNms();
			report.report("Wait 10 seconds");
			GeneralUtils.unSafeSleep(1000 * 10);

			report.report(dut.getNetspanName() + " Wait for all running and in service (TimeOut=15 Minutes)");
			dut.waitForAllRunningAndInService(EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
			report.reportHtml("db get AutoPciCell", dut.lteCli("db get AutoPciCell"), true);
			enodeBConfig.printEnodebState(dut, true);
		}
		printSONStatus(startSONStatus, "Manual");

		GeneralUtils.startLevel("Verify no neighbours");
		if (!netspan.verifyNoNeighbors(dut)) {
			report.report(dut.getName() + " has neighbours, removing them.");
			neighborsUtils.deleteAllNeighbors(dut);
		}
		report.reportHtml("db get nghList", dut.lteCli("db get nghList"), true);
		GeneralUtils.stopLevel();
		GeneralUtils.stopLevel();
		generatePciStartAndEnd();
		startDate = new Date();
		if (startSONStatus == null || !startSONStatus.pciStatus.equals("Manual")) {
			org.junit.Assume.assumeTrue(false);
		}
	}

	protected void changeEnodeBPciAndReboot() {
		GeneralUtils.startLevel("Change enodeB pci and reboot");
		GeneralUtils.startLevel("Setting Cell ID " + dut.getCellContextID() + ": PCI=" + pciStart);
		boolean result = enodeBConfig.changeEnbCellPci(dut, pciStart);
		if (result) {
			report.report("Changing cell pci worked - rebooting");
			dut.reboot();
			dut.waitForAllRunningAndInService(EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
		} else {
			report.report("Failed to change pci in enodeb", Reporter.WARNING);
			pciStart = getPropertiesStaticPCI(dut);
			pciEnd = pciStart + 9;
		}
		GeneralUtils.stopLevel();
		printRSIPCIDebug(status.getSONStatus(dut));
		GeneralUtils.stopLevel();
		report.report("Range pci Start value : " + pciStart);
		report.report("Range pci End value : " + pciEnd);
		reportAboutMibDB("cellAutoPciEnabled", "0");
	}

	protected void printRSIPCIDebug(SONStatus sonStatus) {
		GeneralUtils.startLevel("RSI and PCI Status");
		if (sonStatus == null) {
			report.report("Received son satus with null value");
		} else {
			for (RsiStatusCell cell : sonStatus.RSICell) {
				GeneralUtils.startLevel("Cell ID: " + cell.cellId);
				report.report("Current RSI Status: " + cell.rsiStatus);
				report.report("Current RSI Value: " + cell.currentRsiValue);
				GeneralUtils.stopLevel();
			}
			report.report("Current PCI Status: " + sonStatus.pciStatus);
			for (PciStatusCell cell : sonStatus.PCICells) {
				GeneralUtils.startLevel("Cell ID: " + cell.cellId);
				report.report("Current PCI Value: " + cell.physicalCellId);
				GeneralUtils.stopLevel();
			}
		}
		GeneralUtils.stopLevel();
	}

	@Override
	public void end() {
		endDate = new Date();
		List<EventInfo> timeEvents = alarmsAndEvents.getEventsNodeByDateRange(dut, startDate, endDate);
		if (timeEvents.isEmpty()) {
			report.report("No events to show");
		} else {
			GeneralUtils.startLevel("Events information");
			for (EventInfo event : timeEvents) {
				alarmsAndEvents.printEventInfo(event);
			}
			GeneralUtils.stopLevel();
		}
		GeneralUtils.startLevel("After test");
		report.report("Stop traffic");
		traffic.stopTraffic();
		revertToDefaultSonProfileAndDeleteClonedProfile(dut);
		GeneralUtils.startLevel("Checking PCI value.");
		Boolean isDefault = checkIfPciIsDefault();
		if (!isDefault.equals(null)) {
			if (!isDefault) {
				int initStatusPhysicalCellId = dut.getDefaultNetspanProfiles()
						.getCellNetspanProfile(dut.getCellContextID()).getPci();
				report.report("Reverting " + dut.getName() + " Cell #" + dut.getCellContextID()
						+ " to default PCI number: " + initStatusPhysicalCellId);
				enodeBConfig.changeEnbCellPci(dut, initStatusPhysicalCellId);
				dut.reboot();
			}
			GeneralUtils.unSafeSleep(30 * 1000);
			report.report(dut.getNetspanName() + " wait for all running and in service (TimeOut="
					+ (EnodeB.WAIT_FOR_ALL_RUNNING_TIME / 1000.0 / 60.0) + " Minutes)");
			dut.waitForAllRunningAndInService(EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
			dut.setExpectBooting(false);
		}
		enodeBConfig.printEnodebState(dut, true);
		GeneralUtils.stopLevel();

		SONStatus sonStatus = netspan.getSONStatus(dut);
		printRSIPCIDebug(sonStatus);

		for (EnodeB enb : otherEnb) {
			peripheralsConfig.changeEnbState(enb, EnbStates.IN_SERVICE);
		}
		GeneralUtils.stopLevel();
		super.end();
	}

	private Boolean checkIfPciIsDefault() {
		int sonStatusPhysicalCellId = GeneralUtils.ERROR_VALUE;
		List<PciStatusCell> PciStatusCellList = null;
		try {
			PciStatusCellList = status.getSONStatus(dut).PCICells;
			sonStatusPhysicalCellId = PciStatusCellList.get(0).physicalCellId;
			report.report("Current PCI: " + sonStatusPhysicalCellId);
		} catch (Exception e) {
			report.report("Failed getting PCI info from " + dut.getName(), Reporter.WARNING);
			return null;
		}
		int initStatusPhysicalCellId = GeneralUtils.ERROR_VALUE;
		try {
			initStatusPhysicalCellId = dut.getDefaultNetspanProfiles().getCellNetspanProfile(dut.getCellContextID())
					.getPci();
		} catch (Exception e) {
			report.report("Failed getting default PCI info from SUT for " + dut.getName() + " Cell #"
					+ dut.getCellContextID(), Reporter.WARNING);
			return null;
		}

		if (initStatusPhysicalCellId == GeneralUtils.ERROR_VALUE) {
			report.report("Cannot compare PCI values because default value is not available", Reporter.WARNING);
			return null;
		}
		return sonStatusPhysicalCellId == initStatusPhysicalCellId;
	}

	@ParameterProperties(description = "Name of Enb")
	public void setDUT(String dut) {
		ArrayList<EnodeB> temp = (ArrayList<EnodeB>) SysObjUtils.getInstnce().initSystemObject(EnodeB.class, false,
				dut);
		this.dut = temp.get(0);
	}

	protected boolean configureAutoPciToEnableViaNms(int startPCI, int endPCI) {
		SonParameters sonParams = new SonParameters();
		List<Pair<Integer, Integer>> rangesList = new ArrayList<Pair<Integer, Integer>>();
		rangesList.add(Pair.createPair(startPCI, endPCI));
		sonParams.setSonCommissioning(true);
		sonParams.setAutoPCIEnabled(true);
		sonParams.setRangesList(rangesList);
		GeneralUtils.startLevel(dut.getNetspanName() + " configure Auto PCI to enable via NMS");
		report.report("Configure full range of Auto PCI: Start=" + startPCI + ", End=" + endPCI);
		generalFlag = enodeBConfig.changeSonProfile(dut, sonParams);
		GeneralUtils.stopLevel();
		return generalFlag;
	}

	protected boolean configureAutoPciToDisableViaNms() {
		SonParameters sonParams = new SonParameters();
		sonParams.setSonCommissioning(true);
		sonParams.setAutoPCIEnabled(false);
		GeneralUtils.startLevel(dut.getNetspanName() + " configure Auto PCI to Disable via NMS");
		generalFlag = enodeBConfig.changeSonProfile(dut, sonParams);
		GeneralUtils.stopLevel();
		return generalFlag;
	}

	protected void validateInNmsSonStatusPciTakenFromValidRange(SONStatus sonStatus, int fromValuePCI, int toValuePCI) {
		if (sonStatus == null) {
			report.report("Son status is null. Cannot check pci value.");
			return;
		}
		for (PciStatusCell cell : sonStatus.PCICells) {
			if (cell.cellId == dut.getCellContextID()) {
				if (cell.physicalCellId < fromValuePCI || cell.physicalCellId > toValuePCI) {
					report.report("PCI: " + cell.physicalCellId + " (Should be in the range " + fromValuePCI + "-"
							+ toValuePCI + ")", Reporter.FAIL);
					reason = "PCI: " + cell.physicalCellId + " (Should be in the range " + fromValuePCI + "-"
							+ toValuePCI + ")";
				} else {
					report.report("EnodeB gets correct PCI value (in range " + fromValuePCI + "<=" + cell.physicalCellId
							+ "<=" + toValuePCI + ")");
				}
			}
		}
	}

	protected boolean reportAboutMibDB(String name, String expected) {
		String actual = dut.getSonCfgPciAuto();

		if (expected.equals(actual)) {
			report.report(name + "-> actual: " + actual + ", expected: " + expected);
			return true;
		} else {
			report.report(name + "-> actual: " + actual + ", expected: " + expected, Reporter.FAIL);
			reason = name + "-> actual: " + actual + ", expected: " + expected;
			return false;
		}
	}

	protected int getPropertiesStaticPCI(EnodeB dut) {
		int pci = GeneralUtils.ERROR_VALUE;

		SONStatus sonProperties = netspan.getSONStatus(dut);
		int context = dut.getCellContextID();
		if (sonProperties != null) {
			try {
				pci = sonProperties.PCICells.get(context - 1).physicalCellId;
			} catch (Exception e) {
				e.printStackTrace();
				GeneralUtils.printToConsole("Error in getting pci");
			}
		}

		return pci;
	}

	protected boolean addingToEnbViaNmsNeighborWithStaticPCI(EnodeB dut, String neihborName, String IPAdress,
			int staticPci, int neihborId) {
		Integer earfcn = Integer.valueOf(dut.getEarfcn());
		GeneralUtils.startLevel(
				"Add to eNB via NMS at neighbor with static PCI=" + staticPci + ", Downlink EARFCN=" + earfcn);

		EnodeB newParty = neighborsUtils.adding3rdPartyNeighbor(dut, neihborName, IPAdress, staticPci, neihborId,
				earfcn);
		if (newParty == null) {
			report.report("Failed to create 3rd enb", Reporter.WARNING);
			GeneralUtils.stopLevel();
			return false;
		} else {
			report.report("3rd party enb was created successfully");
		}
		boolean add = neighborsUtils.addNeighbor(dut, newParty, HoControlStateTypes.ALLOWED,
				X2ControlStateTypes.AUTOMATIC, HandoverType.TRIGGER_X_2, true, "0");
		if (add) {
			report.report("3rd party enb was added as neighbor");
			list3Party.add(newParty);
		} else {
			report.report("Failed to add 3rd party enb as neighbor", Reporter.WARNING);
		}
		GeneralUtils.stopLevel();
		return add;
	}

	protected void printSONStatus(SONStatus SONStatus, String pciStatusExpected) {
		if (SONStatus == null) {
			return;
		}
		GeneralUtils.startLevel("Print SON Status values");
		if (pciStatusExpected != null && !SONStatus.pciStatus.equals(pciStatusExpected)) {
			report.report("PCI Status: '" + SONStatus.pciStatus + "' (Expected: '" + pciStatusExpected + "')",
					Reporter.FAIL);
			reason = "PCI Status: '" + SONStatus.pciStatus + "' (Expected: '" + pciStatusExpected + "')";
		} else {
			report.report("PCI Status: '" + SONStatus.pciStatus + "'");
		}
		for (int i = 0; i < SONStatus.PCICells.size(); i++) {
			PciStatusCell cellSonStatus = SONStatus.PCICells.get(i);
			GeneralUtils.startLevel("Cell ID: " + cellSonStatus.cellId);
			report.report("Physical Layer Cell Group: " + cellSonStatus.physicalLayerCellGroup);
			report.report("Physical Layer Identity: " + cellSonStatus.physicalLayerIdentity);
			report.report("Physical Cell Id (PCI): " + cellSonStatus.physicalCellId);
			report.report("PCI Status: " + cellSonStatus.pciStatus);
			GeneralUtils.stopLevel();
		}
		GeneralUtils.stopLevel();
	}

	protected void revertToDefaultSonProfile(EnodeB dut) {
		GeneralUtils.startLevel("Revert to default SON Profile");
		enodeBConfig.setProfile(dut, EnbProfiles.Son_Profile, dut.defaultNetspanProfiles.getSON());
		GeneralUtils.stopLevel();
	}

	protected void deleteClonedProfile(EnodeB dut) {
		GeneralUtils.startLevel("Delete cloned profiles");
		enodeBConfig.deleteClonedProfiles();
		GeneralUtils.stopLevel();
	}

	protected void revertToDefaultAdvancedProfile(EnodeB dut) {
		GeneralUtils.startLevel("Revert to default Advanced Profile");
		enodeBConfig.setProfile(dut, EnbProfiles.EnodeB_Advanced_Profile,
				dut.defaultNetspanProfiles.getEnodeBAdvanced());
		GeneralUtils.stopLevel();
	}

	protected void configureInitListSizeOnEnbAdvancedConfigurationProfile(int lowPower, int powerStep,
			int powerLevelTimeInterval, int anrTimer, EnabledDisabledStates pciConfusionAllowed,
			int initialPciListSize) {
		GeneralUtils.startLevel("Configure 'eNodeB Advanced Configuration Profile'");
		this.printAdvancedProfile(lowPower, powerStep, powerLevelTimeInterval, anrTimer, pciConfusionAllowed,
				initialPciListSize);
		EnodeBAdvancedParameters advancedParams = new EnodeBAdvancedParameters();
		advancedParams.setLowPower(lowPower);
		advancedParams.setPowerStep(powerStep);
		advancedParams.setPowerLevelTimeInterval(powerLevelTimeInterval);
		advancedParams.setAnrTimer(anrTimer);
		advancedParams.setPciConfusionAllowed(pciConfusionAllowed);
		advancedParams.setInitialPciListSize(initialPciListSize);
		generalFlag = enodeBConfig.cloneAndSetAdvancedProfileViaNetspan(dut,
				dut.getDefaultNetspanProfiles().getEnodeBAdvanced(), advancedParams);
		GeneralUtils.stopLevel();

	}

	protected void printAdvancedProfile(int lowPower, int powerStep, int powerLevelTimeInterval, int anrTimer,
			EnabledDisabledStates pciConfusionAllowed, int initialPciListSize) {

		report.report("Low Power: " + lowPower);
		report.report("Power Step: " + powerStep);
		report.report("Power Level Time Interval: " + powerLevelTimeInterval);
		report.report("ANR Timer: " + anrTimer);
		report.report("PCI Confusion Allowed: " + pciConfusionAllowed.value());
		report.report("Initial PCI List Size: " + initialPciListSize);
	}

	protected boolean configurePciRangeAndANR(int startPCI, int endPCI, int interEarfcn, int defaultEarfcn) {
		SonParameters sonParams = new SonParameters();
		List<Pair<Integer, Integer>> rangesList = new ArrayList<Pair<Integer, Integer>>();
		// range
		rangesList.add(Pair.createPair(startPCI, endPCI));
		sonParams.setSonCommissioning(true);
		sonParams.setAutoPCIEnabled(true);
		sonParams.setRangesList(rangesList);

		// ANR
		sonParams.setAnrState(SonAnrStates.PERIODICAL_MEASUREMENT);
		List<Integer> anrFrequencyList = new ArrayList<Integer>();
		anrFrequencyList.add(interEarfcn);
		anrFrequencyList.add(defaultEarfcn);
		sonParams.setAnrFrequencyList(anrFrequencyList);

		GeneralUtils.startLevel("Configure Son Profile to enable Auto PCI, Start=" + startPCI + ", End=" + endPCI
				+ " (with enable ANR, " + interEarfcn + " and " + defaultEarfcn + " EARFCNs)");
		generalFlag = enodeBConfig.changeSonProfile(dut, sonParams);
		GeneralUtils.stopLevel();

		return generalFlag;
	}

	protected boolean configureOnlyANRtoEnableViaNms(int interEarfcn, int defaultEarfcn) {
		SonParameters sonParams = new SonParameters();
		sonParams.setSonCommissioning(true);
		sonParams.setAnrState(SonAnrStates.PERIODICAL_MEASUREMENT);
		List<Integer> anrFrequencyList = new ArrayList<Integer>();
		anrFrequencyList.add(interEarfcn);
		anrFrequencyList.add(defaultEarfcn);
		sonParams.setAnrFrequencyList(anrFrequencyList);

		GeneralUtils.startLevel(
				"Configure ANR to enable via NMS and set " + interEarfcn + ", " + defaultEarfcn + " EARFCNs");
		generalFlag = enodeBConfig.changeSonProfile(dut, sonParams);
		GeneralUtils.stopLevel();

		return generalFlag;
	}

	protected boolean changeTxPowerViaNms(int newTxPower) {
		boolean flag = false;
		GeneralUtils.startLevel("Change Tx Power to " + newTxPower + "dBm");
		RadioParameters radioParams = new RadioParameters();
		radioParams.setTxpower(newTxPower);
		flag = enodeBConfig.cloneAndSetRadioProfileViaNetSpan(dut, dut.getDefaultNetspanProfiles().getRadio(),
				radioParams);
		GeneralUtils.stopLevel();

		return flag;
	}

	protected void revertToDefaultRadioProfile(EnodeB dut) {
		GeneralUtils.startLevel("Revert to default Radio Profile");
		enodeBConfig.setProfile(dut, EnbProfiles.Radio_Profile, dut.defaultNetspanProfiles.getRadio());
		GeneralUtils.stopLevel();

	}

	protected void revertToDefaultSonProfileAndDeleteClonedProfile(EnodeB dut) {
		GeneralUtils.startLevel("Revert to default SON Profile and delete cloned profile");
		report.report("Checking if PCI is different from default");
		Boolean flag = checkIfPciIsDefault();
		if (flag != null && !flag) {
			dut.setExpectBooting(true);
		}
		enodeBConfig.setProfile(dut, EnbProfiles.Son_Profile, dut.defaultNetspanProfiles.getSON());
		enodeBConfig.deleteClonedProfiles();
		GeneralUtils.stopLevel();
	}

	protected boolean waitForGetTxPowerFromNetspan(long timeout) {
		GeneralUtils.printToConsole("will wait for geting Tx Power from Netspan" + timeout + " milis");
		long startTime = System.currentTimeMillis(); // fetch starting time
		while ((System.currentTimeMillis() - startTime) < timeout) {
			if (!netspan.getRFStatus(dut).isEmpty()) {
				return true;
			}
		}
		return false;
	}

	protected void printLteCliCommand(String command) {
		GeneralUtils.startLevel("lteCli: " + command);
		String res = dut.lteCli(command);

		for (String str : res.replaceAll("\n\n\n\n", "\n").split("\n")) {
			report.report(str);
		}
		GeneralUtils.stopLevel();
	}

	protected void checkConnectionUEWithStopStartAndRetries() {
		boolean connected = false;
		ArrayList<UE> ueList = new ArrayList<UE>();
		ueList.add(testUE);
		GeneralUtils.startLevel("Check if UE is connected to EnodeB");
		connected = peripheralsConfig.checkConnectionUEWithStopStart(testUE, dut);
		if (!connected) {
			report.report("UE was not connected. Wait 20 seconds");
			GeneralUtils.unSafeSleep(20 * 1000);
			connected = peripheralsConfig.checkIfAllUEsAreConnectedToNode(ueList, dut);
		}
		if (!connected) {
			report.report("UE was not connected. Reboot UE");
			testUE.reboot();
			report.report("Wait for 2 minutes");
			GeneralUtils.unSafeSleep(2 * 60 * 1000);
			connected = peripheralsConfig.checkIfAllUEsAreConnectedToNode(ueList, dut);
		}
		GeneralUtils.stopLevel();
		if (!connected) {
			report.report("UE failed to connect to enodeB", Reporter.WARNING);
			reason = "UE failed to connect to enodeB";
		} else {
			report.report("UE connected to enodeB");
		}
	}

	protected String startTrafficAndConnectUes(int reporterStatus) {
		GeneralUtils.startLevel("Starting traffic");
		try {
			traffic.startTraffic();
		} catch (Exception e) {
			report.report("Failed starting traffic", Reporter.WARNING);
			e.printStackTrace();
		}
		GeneralUtils.stopLevel();
		testUE.start();
		GeneralUtils.unSafeSleep(10 * 1000);
		return peripheralsConfig.checkUesConnection(dut, testUeList, reporterStatus);
	}

	protected Integer generatePciStart(int defaultValue) {
		Integer startPci = testConfig.getPciStart();
		if (startPci == null) {
			startPci = defaultValue;
		}
		return startPci;
	}

	protected Integer generatePciEnd(int defaultValue) {
		Integer endPci = testConfig.getPciEnd();
		if (endPci == null) {
			endPci = defaultValue;
		}
		return endPci;
	}

	public void startTrafficAndCheckIfUEConnected() {
		GeneralUtils.startLevel("Start traffic and check if UE is connected");
		reason += startTrafficAndConnectUes(Reporter.WARNING);
		GeneralUtils.stopLevel();
	}

	private void generatePciStartAndEnd() {
		pciStart = generatePciStart(300);
		pciEnd = generatePciEnd(309);
	}
}
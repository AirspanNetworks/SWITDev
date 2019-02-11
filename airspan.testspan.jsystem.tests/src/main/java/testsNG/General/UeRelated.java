package testsNG.General;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import Utils.CommonConstants;
import org.junit.Test;

import Attenuators.AttenuatorSet;
import EPC.EPC;
import EnodeB.EnodeB;
import Netspan.NetspanServer;
import Netspan.API.Enums.EnbStates;
import UE.UE;
import UE.UeState;
import Utils.GeneralUtils;
import Utils.SetupUtils;
import Utils.SysObjUtils;
import jsystem.framework.ParameterProperties;
import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;
import testsNG.TestspanTest;
import testsNG.Actions.PeripheralsConfig;
import testsNG.Actions.Traffic;
import testsNG.Actions.Utils.ParallelCommandsThread;
import testsNG.Actions.Utils.TrafficGeneratorType;

/**
 * UeRelated
 * <p>
 * BasicNetworkEntry Test
 * <p>
 * last update : 17/07/2016
 *
 * @Author Shuhamy Shahaf.
 * <p>
 * pre Test: Verifying ENodeBs. In Test : rebooting UE's. Epc Updating
 * UE's states Epc checking using epc and checking all of the connection
 * objects comparing to the UE's SIM getting from SUT
 */
public class UeRelated extends TestspanTest {

	private final int MAX_NUMBER_OF_RECOVERY_TRYS = 2;
	EPC epc;
	PeripheralsConfig peripheralsConfig;
	short numberOfRecoveryTrys = 0;
	private EnodeB dut;
	private ArrayList<EnodeB> duts;
	Traffic traffic;
	ParallelCommandsThread commandsThread = null;

	@Override
	public void init() throws Exception {

		epc = EPC.getInstance();
		enbInTest = new ArrayList<EnodeB>();
		enbInTest.add(dut);
		traffic = Traffic.getInstance(SetupUtils.getInstance().getAllUEs());
		peripheralsConfig = PeripheralsConfig.getInstance();
		super.init();
		setDUTs();
		ArrayList<String> commands = new ArrayList<>();
		commands.add("ue show link");
		commands.add("ue show rate");
		commands.add("rlc show amdlstats");
		commands.add("system show error");
		commands.add("system show phyerror");
		commands.add("system show memory");
		commands.add("db get PTPStatus");
		try {
			commandsThread = new ParallelCommandsThread(commands, dut, null, null);
		} catch (IOException e) {
			report.report("could not init Commands in parallel", Reporter.WARNING);
		}
		commandsThread.start();

		GeneralUtils.startLevel("Init");

		if (traffic.getGeneratorType() == TrafficGeneratorType.TestCenter) {
			String TrafficFile = traffic.getTg().getDefaultConfigTccFile();
			int lastDot = TrafficFile.lastIndexOf('.');
			String HOTrafficFile = TrafficFile.substring(0, lastDot) + "_HO" + TrafficFile.substring(lastDot);
			traffic.configFile = new File(HOTrafficFile);
		}
		GeneralUtils.startLevel("Prepare Setup for the test");

		for (EnodeB enb : duts) {
			peripheralsConfig.changeEnbState(enb, EnbStates.OUT_OF_SERVICE);
		}

		startUEs(dut);

		AttenuatorSet attenuatorSetUnderTest = AttenuatorSet.getAttenuatorSet(CommonConstants.ATTENUATOR_SET_NAME);
		GeneralUtils.startLevel("set the attenuators default value : " + attenuatorSetUnderTest.getDefaultValueAttenuation()
				+ " [dB]\n");
		peripheralsConfig.setAttenuatorSetValue(attenuatorSetUnderTest,
				attenuatorSetUnderTest.getDefaultValueAttenuation());
		GeneralUtils.stopLevel();

		GeneralUtils.stopLevel();
		GeneralUtils.stopLevel();
	}

	@Test
	@TestProperties(name = "BasicNetworkEntry", returnParam = {"IsTestWasSuccessful"}, paramsExclude = {
			"IsTestWasSuccessful"})
	public void BasicNetworkEntry() {
		ArrayList<UE> allUEs = new ArrayList<UE>();
		ArrayList<UE> staticUE = SetupUtils.getInstance().getStaticUEs(dut);
		ArrayList<UE> dynamicUE = SetupUtils.getInstance().getDynamicUEs();
		if (staticUE != null) {
			allUEs.addAll(staticUE);
		}
		if (dynamicUE != null) {
			allUEs.addAll(dynamicUE);
		}

		netWorkEntryBase(allUEs);
	}

	private Boolean recovery(short numberOfRecoveryTrys, ArrayList<UE> ueList, EnodeB enb) {
		if (numberOfRecoveryTrys == 1) {
			report.report("[RECOVERY]: waiting more 60 seconds");
			GeneralUtils.unSafeSleep(60 * 1000);
			report.report("Checking UEs status in general:");
			ArrayList<EnodeB> enbs = new ArrayList<EnodeB>();
			enbs.add(enb);
			if (peripheralsConfig.epcAndEnodeBsConnection(ueList, enbs)) {
				report.report("CheckUeListConnection failed", Reporter.FAIL);
				return false;
			}

			for (UE ue : ueList) {
				String usState = "disconnected or unknown";
				if (ue.getState() == UeState.connected || ue.getState() == UeState.idle) {
					usState = "connected";
				}
				report.report("UE: " + ue.getName() + " " + ue.getImsi() + " is in " + usState + " state");
			}

		} else {
			Long restartDelay = new Long(120 * 1000);
			report.report("[RECOVERY]: rebooting All Ues!");
			peripheralsConfig.rebootUEs(ueList, restartDelay);
		}
		return true;
	}

	/**
	 * Reboot UEs - using peripheralConfig : 30/05/16 * @Author Shahumy
	 *
	 * @throws Exception
	 */
	private void startUEs(EnodeB enb) {
		ArrayList<UE> temp = new ArrayList<>();
		ArrayList<UE> staticUEs = SetupUtils.getInstance().getStaticUEs(enb);
		ArrayList<UE> DynamicUes = SetupUtils.getInstance().getDynamicUEs();
		if (staticUEs != null)
			temp.addAll(staticUEs);
		if (DynamicUes != null)
			temp.addAll(DynamicUes);

		peripheralsConfig.startUEs(temp);
	}

	@ParameterProperties(description = "Name of Enb")
	public void setDUT(String dut) {
		ArrayList<EnodeB> temp = (ArrayList<EnodeB>) SysObjUtils.getInstnce().initSystemObject(EnodeB.class, false, dut);
		this.dut = temp.get(0);
	}

	public void setDUTs() {
		try {
			this.duts = SetupUtils.getInstance().getAllEnb();
			duts.remove(dut);
		} catch (Exception e) {
			this.duts = null;
		}
	}

	@Override
	public void end() {
		if(traffic != null) {
			traffic.revertToDefault();
		}
		stopCommandThreadIfStarted();
		changeDutsToInService();
		super.end();
	}

	/**
	 * stop CommandThread If Started
	 */
	private void stopCommandThreadIfStarted() {
		if (commandsThread != null) {
			commandsThread.stopCommands();
			commandsThread.moveFileToReporterAndAddLink();
		}
	}

	/**
	 * change Duts To In Service
	 */
	private void changeDutsToInService() {
		GeneralUtils.startLevel("Returning system to status before the test");
		if (duts != null) {
			for (EnodeB enb : duts) {
				peripheralsConfig.changeEnbState(enb, EnbStates.IN_SERVICE);
			}
		}
		GeneralUtils.stopLevel();
	}

	/**
	 * @throws Exception
	 * @author Shahaf Shuhamy tests only to only category 6 UES.
	 */
	@Test
	@TestProperties(name = "Carrier Aggregation BasicNetworkEntry", returnParam = {"IsTestWasSuccessful"}, paramsExclude = {"IsTestWasSuccessful"})
	public void CABasicNetworkEntry() {
		ArrayList<UE> allUEs = SetupUtils.getInstance().getCat6UEs();

		if (allUEs.size() == 0) {
			report.report("no Category 6 UES - Failing Test", Reporter.FAIL);
		}
		netWorkEntryBase(allUEs);
	}

	private void netWorkEntryBase(ArrayList<UE> allUEs) {
		boolean status;
		while (numberOfRecoveryTrys <= MAX_NUMBER_OF_RECOVERY_TRYS) {
			report.report("Starting traffic.");
			traffic.startTraffic(allUEs);
			report.report("Changing UE state from idle to connected using traffic.");
			status = peripheralsConfig.checkIfAllUEsAreConnectedToNode(allUEs, dut);
			report.report("Stopping traffic.");
			traffic.stopTraffic();
			if (status) {
				report.report("Status of test: BASIC NETWORK ENTRY test ended SUCCESSFULLY for version: " + dut.getRunningVersion() + ","
						+ " number of connected UEs: " + allUEs.size());
				isPass = true;
				return;
			} else {
				double numberOfNotConnectedUEs = peripheralsConfig.getUesNotConnected().size();
				double totalNumberOfUEs = allUEs.size();
				double result = numberOfNotConnectedUEs / totalNumberOfUEs;
				if (result <= 0.4) {
					int numberOfUesConnected = (int) (totalNumberOfUEs - numberOfNotConnectedUEs);
					report.report("Status of test: BASIC NETWORK ENTRY test ended With a WARNING for version: " + dut.getRunningVersion() + ","
							+ " number of connected UEs: " + numberOfUesConnected, Reporter.WARNING);
					return;
				} else {
					numberOfRecoveryTrys++;
					if (numberOfRecoveryTrys <= MAX_NUMBER_OF_RECOVERY_TRYS) {
						GeneralUtils.startLevel("Starting recovery actions #" + String.valueOf(numberOfRecoveryTrys) + ":");
						recovery(numberOfRecoveryTrys, allUEs, dut);
						GeneralUtils.stopLevel();
					}
				}
			}
		}
		report.report("Status of test: BASIC NETWORK ENTRY test FAILED for version: " + dut.getRunningVersion() + ", at least one UE isn't connected " + peripheralsConfig.getUesNotConnected(), Reporter.FAIL);
		reason = "At least one UE isn't connected";
		isPass = false;
	}
}

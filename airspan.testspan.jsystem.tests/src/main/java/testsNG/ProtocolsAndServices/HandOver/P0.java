package testsNG.ProtocolsAndServices.HandOver;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import Utils.*;
import org.junit.Test;
import Attenuators.AttenuatorSet;
import EPC.EPC;
import EnodeB.EnodeB;
import Netspan.EnbProfiles;
import Netspan.API.Enums.ConnectedModeEventTypes;
import Netspan.API.Enums.HandoverType;
import Netspan.API.Enums.HoControlStateTypes;
import Netspan.API.Enums.X2ControlStateTypes;
import Netspan.Profiles.MobilityParameters;
import UE.UE;
import Utils.Snmp.MibReader;
import Utils.WatchDog.CommandWatchDynamicUEs;
import Utils.WatchDog.WatchDogManager;
import jsystem.framework.ParameterProperties;
import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;
import testsNG.TestspanTest;
import testsNG.Actions.EnodeBConfig;
import testsNG.Actions.Neighbors;
import testsNG.Actions.PeripheralsConfig;
import testsNG.Actions.Traffic;
import testsNG.Actions.Traffic.TrafficType;
import testsNG.Actions.TrafficCapacity;
import testsNG.Actions.Utils.ParallelCommandsThread;
import testsNG.Actions.Utils.TrafficGeneratorType;

/**
 * @author mgoldenberg
 */
public class P0 extends TestspanTest {

	private static final int LONG_TEST_DURATION = 600000; // 10 minutes
	private static final int STABILITY_TEST_DURATION = 14400000; // 4 hours
	private int ATT_STEP_TIME = 500;
	private int ATT_STEP = 2;
	private static final float WARNING_LEVEL = 0.9f;
	private static final float PASS_LEVEL = 0.98f;

	public long maxduration = LONG_TEST_DURATION;
	private Neighbors neighbors;
	private Traffic traffic;
	private PeripheralsConfig peripheralsConfig;
	private AttenuatorSet attenuatorSetUnderTest;
	private EPC epc;
	private WatchDogManager wd;
	private CommandWatchDynamicUEs wdDynamicUEs;
	EnodeB enodeB;
	EnodeB neighbor;
	public boolean isIntra = false;
	boolean skipAddNgh = false;
	public boolean isNegative = false;
	private boolean isConfigNedded = true;
	int expectedNumOfHO = 30;
	HashMap<String, String> counters = new HashMap<>();
	public HashMap<String, String> passCriteria = new HashMap<>();
	ArrayList<UE> dynUEList = new ArrayList<>();
	ArrayList<UE> statUEList = new ArrayList<>();
	public String succCounterName;
	public String attCounterName;
	public String succCounterNameSNMP;
	public String attCounterNameSNMP;
	public EnodeB dut1;
	public EnodeB dut2;
	ParallelCommandsThread commandsThread1 = null;
	ParallelCommandsThread commandsThread2 = null;
	private int attenuatorMin;
	private int attenuatorMax;
	private EnodeBConfig enodeBConfig;
	private String neighbourMobilityProfile = null;
	private String mainMobilityProfile = null;

	// results variables
	public int numberOfDynamicUES;
	private double HO_Per_Min_Per_UE;
	private int first_ENB_succ = 0;
	private int first_ENB_att = 0;
	private double firstENB_HO_Success_rate_out_of_attempts;
	private int second_ENB_succ = 0;
	private int second_ENB_att = 0;
	private double secondENB_HO_Success_rate_out_of_attempts;
	private int total_succCounter;
	private int total_attCounter;
	private double total_HO_Success_rate_out_of_attempts;
	private double theoreticalAttempts;

	@Override
	public void init() throws Exception {
		if (dut1 == null)
			throw new Exception("DUT1 is not loaded");
		if (dut2 == null)
			throw new Exception("DUT2 is not loaded");
		enbInTest = new ArrayList<>();
		enbInTest.add(dut1);
		enbInTest.add(dut2);
		neighbors = Neighbors.getInstance();
		super.init();
		objectInit();
		initWatchDog();
	}

	/**
	 * init of objects in case you need to declare objects without running the
	 * super init and traffic declaration
	 *
	 * @author Moran Goldenberg
	 */
	public void objectInit() {
		enodeBConfig = EnodeBConfig.getInstance();
		enodeB = dut1;
		neighbor = dut2;
		peripheralsConfig = PeripheralsConfig.getInstance();
		epc = EPC.getInstance();
		traffic = Traffic.getInstance(SetupUtils.getInstance().getAllUEs());
		traffic.setTrafficType(TrafficType.HO);
		attenuatorSetUnderTest = AttenuatorSet.getAttenuatorSet(CommonConstants.ATTENUATOR_SET_NAME);
		attenuatorMin = attenuatorSetUnderTest.getMinAttenuation();
		attenuatorMax = attenuatorSetUnderTest.getMaxAttenuation();
		ATT_STEP_TIME = attenuatorSetUnderTest.getStepTime();
		ATT_STEP = attenuatorSetUnderTest.getAttenuationStep();
		commands();
	}

	/**
	 * init command for WatchDog and start it in case of a Fail - reporting with
	 * a warning and watchDog instance is null.
	 *
	 * @author Shahaf Shuhamy
	 */
	private void initWatchDog() {
		ArrayList<UE> dynamicUEs = new ArrayList<>();
		try {
			wd = WatchDogManager.getInstance();
			dynamicUEs = SetupUtils.getInstance().getDynamicUEs();
			wdDynamicUEs = new CommandWatchDynamicUEs(enbInTest, epc, dynamicUEs);
			wd.addCommand(wdDynamicUEs);
		} catch (Exception e) {
			e.printStackTrace();
			report.report("Could not Init Dynamic UEs command", Reporter.WARNING);
			wd = null;
			return;
		}
	}

	/**
	 * Pre test of HO test
	 *
	 * @param HOControlTypes
	 * @param X2Types
	 * @param HOType
	 * @param hoEventType
	 * @param numOfTry
	 * @return
	 */
	public boolean preTest(HoControlStateTypes HOControlTypes, X2ControlStateTypes X2Types, HandoverType HOType,
			ConnectedModeEventTypes hoEventType) {
		GeneralUtils.startLevel("HO Pre Test");
		try {
			initUes();
			if (isConfigNedded) {
				// change frequency if needed.
				if (!checkFrequency()) {
					return false;
				}
				setDefaultSonProfile();
				deleteNeighbours();
				peripheralsConfig.SetAttenuatorToMin(attenuatorSetUnderTest);
				GeneralUtils.unSafeSleep(5000);
				startTraffic();
				if (!addNeighbours(enodeB, neighbor, HOControlTypes, X2Types, HOType, true, "0"))
					return false;
				if (!checkAllDynamicConnected())
					return false;
				if (!checkStaticUesConnected())
					return false;
			}
			if (!findMainEnb())
				return false;
		} finally {
			GeneralUtils.stopAllLevels();
		}
		return true;
	}

	private void setDefaultSonProfile() {
		GeneralUtils.startLevel("Setting default SON profiles");
		for (EnodeB enb : enbInTest) {
			report.report("Set Son Profile:" + enb.defaultNetspanProfiles.getSON() + " For Enodeb:" + enb.getName());
			String SON = enb.defaultNetspanProfiles.getSON();
			if (SON != null) {
				enodeBConfig.setProfile(enb, EnbProfiles.Son_Profile, SON);
			} else {
				report.report("There is no SON profile name in SUT", Reporter.FAIL);
				reason = "There is no SON profile name in SUT";
				return;
			}
		}
		GeneralUtils.stopLevel();
	}

	private boolean checkStaticUesConnected() {
		GeneralUtils.startLevel("Checking if all static UEs are connected");
		if (statUEList != null) {
			if (!peripheralsConfig.epcAndEnodeBsConnection(statUEList, enbInTest)) {
				report.report("Not all static UEs are connected to EnodeB", Reporter.FAIL);
				reason = "Not all static UEs are connected to EnodeB";
				return false;
			}
		} else {
			report.report("There are no static UEs to check");
		}
		return true;
	}

	public void deleteNeighbours() {
		if (!skipAddNgh) {
			GeneralUtils.startLevel("Deleting all neighbors");
			for (EnodeB enodeB : enbInTest) {
				neighbors.deleteAllNeighbors(enodeB);
			}
			GeneralUtils.stopLevel();
		}
	}

	private void initUes() {
		// making dynamic and static lists of all UEs
		dynUEList = SetupUtils.getInstance().getDynamicUEs();
		statUEList = SetupUtils.getInstance().getStaticUEs(enodeB);
		if (statUEList == null) {
			statUEList = SetupUtils.getInstance().getStaticUEs(neighbor);
		} else {
			ArrayList<UE> staticUEs = SetupUtils.getInstance().getStaticUEs(neighbor);
			if (staticUEs != null)
				statUEList.addAll(staticUEs);
		}
		if (isConfigNedded) {
			peripheralsConfig.stopUEs(dynUEList);
			peripheralsConfig.startUEs(dynUEList);
		}
	}

	@SuppressWarnings("unused")
	private void changeMobilityProfile(ConnectedModeEventTypes hoEventType) {
		UE testUe = null;
		for (UE dynUE : SetupUtils.getInstance().getDynamicUEs()) {
			EnodeB currentEnb = epc.getCurrentEnodB(dynUE, enbInTest);
			if (currentEnb == enodeB) {
				testUe = dynUE;
				break;
			}
		}
		int RSRP1 = testUe.getRSRP(0);
		if (RSRP1 == GeneralUtils.ERROR_VALUE) {
			report.report("Error in getting RSRP from UE " + testUe.getLanIpAddress(), Reporter.WARNING);
		}
		report.report("RSRPavg = " + RSRP1);
		neighbourMobilityProfile = netspanServer.getCurrentProfileName(neighbor, EnbProfiles.Mobility_Profile);
		changingEventType(neighbor, RSRP1, hoEventType);
		mainMobilityProfile = netspanServer.getCurrentProfileName(enodeB, EnbProfiles.Mobility_Profile);
		changingEventType(enodeB, RSRP1 + 3, hoEventType);
	}

	private void changingEventType(EnodeB enb, int RSRPavg, ConnectedModeEventTypes hoEventType) {

		GeneralUtils.startLevel("Setting mobility profile for " + enb.getName());
		MobilityParameters mobilityParas = new MobilityParameters();

		report.report("Setting IsIntra: " + isIntra);
		mobilityParas.setIsIntra(isIntra);

		report.report("Setting eventType: " + hoEventType);
		mobilityParas.setEventType(hoEventType);

		report.report("Setting Hysteresis: " + 2);
		mobilityParas.setHysteresis(2.0);
		if (hoEventType == ConnectedModeEventTypes.A_3) {
			report.report("Setting A3Offset: " + 3);
			mobilityParas.setA3Offset(3.0);
		}
		if (hoEventType == ConnectedModeEventTypes.A_4) {
			report.report("Setting RsrpEventThreshold1: " + (RSRPavg + 3));
			mobilityParas.setRsrpEventThreshold1(RSRPavg + 3);
		}
		if (hoEventType == ConnectedModeEventTypes.A_5) {
			report.report("Setting RsrpEventThreshold1: " + (RSRPavg + 2));
			mobilityParas.setRsrpEventThreshold1(RSRPavg + 2);
			report.report("Setting RsrpEventThreshold2: " + (RSRPavg + 3));
			mobilityParas.setRsrpEventThreshold2(RSRPavg + 3);
		}

		if (!enodeBConfig.cloneAndSetMobilityProfileViaNetSpan(enb, enb.defaultNetspanProfiles.getMobility(),
				mobilityParas)) {
			report.report(String.format("%s - failed to set Mobility profile.", enb.getName()), Reporter.FAIL);
			GeneralUtils.stopLevel();
			return; // False;?
		}
		GeneralUtils.stopLevel();
	}

	private void startTraffic() {
		GeneralUtils.startLevel("Starting traffic.");
		try {
			traffic.startTraffic(TrafficCapacity.CUSTOM);
		} catch (Exception e) {
			e.printStackTrace();
		}
		GeneralUtils.stopLevel();
	}

	private boolean findMainEnb() {
		GeneralUtils.startLevel("Checking what is the main EnodeB");

		EnodeB currentEnb = getEnbConnectedToDynUe();
		if (currentEnb == null) {
			report.report("Cannot find main ENB, rebooting UEs and trying again.");
			peripheralsConfig.rebootUEs(SetupUtils.getInstance().getDynamicUEs());
			currentEnb = getEnbConnectedToDynUe();
			if (currentEnb == null) {
				report.report("Cannot find main ENB, no dynamic ues are connecetd.", Reporter.FAIL);
				reason = "Cannot find main ENB, no dynamic ues are connecetd.";
				GeneralUtils.stopLevel();// Checking what is the main EnodeB
											// stop level
				return false;
			}
		}
		enodeB = currentEnb;
		neighbor = enodeB == dut1 ? dut2 : dut1;
		report.report("Main EnodeB: " + enodeB.getNetspanName());
		report.report("Slave EnodeB: " + neighbor.getNetspanName());
		GeneralUtils.stopLevel();// Checking what is the main EnodeB stop level
		return true;
	}

	private boolean checkAllDynamicConnected() {

		for (int retry = 1; retry <= 3; retry++) {
			GeneralUtils.startLevel("Checking if all Dynamic UEs are connected ");
			if (peripheralsConfig.epcAndEnodeBsConnection(SetupUtils.getInstance().getDynamicUEs(), enbInTest)) {
				GeneralUtils.stopLevel();
				return true;
			} else {
				report.report("Not all dynamic Ue's are connected to EnodeB", Reporter.WARNING);
				peripheralsConfig.rebootUEs(SetupUtils.getInstance().getDynamicUEs());
				GeneralUtils.stopLevel();
			}
		}

		GeneralUtils.startLevel("Checking Dynamic UEs and removing any that are not connecetd.");
		for (UE ue : SetupUtils.getInstance().getDynamicUEs()) {
			ArrayList<UE> ueList = new ArrayList<>();
			ueList.add(ue);
			if (!peripheralsConfig.epcAndEnodeBsConnection(ueList, enbInTest)) {
				report.report("Removing and stopping ue " + ue.getName());
				if (!ue.stop())
					report.report("Failed stopping ue " + ue.getName(), Reporter.WARNING);
				dynUEList.remove(ue);
			}
		}
		if (dynUEList.isEmpty()) {
			report.report("There are no connected dynamic ues.", Reporter.FAIL);
			reason = "There are no connected dynamic ues.";
			return false;
		}
		GeneralUtils.stopLevel();
		return true;
	}

	private EnodeB getEnbConnectedToDynUe() {
		EnodeB currentEnb = null;
		for (UE dynUE : SetupUtils.getInstance().getDynamicUEs()) {
			currentEnb = epc.getCurrentEnodB(dynUE, enbInTest);
			if (currentEnb != null) {
				report.report("Found enb " + currentEnb.getName() + " connecetd to ue " + dynUE.getName());
				return currentEnb;
			}
		}
		return null;
	}

	public void postTest() {
		stopCommandsAndAttachFiles();
		if (isConfigNedded) {
			GeneralUtils.startLevel("HO Post Test");

			report.report("Test reason: " + reason);

			report.report("Stopping traffic");
			traffic.stopTraffic();
			if (mainMobilityProfile != null)
				netspanServer.setProfile(enodeB, EnbProfiles.Mobility_Profile, mainMobilityProfile);
			if (neighbourMobilityProfile != null)
				netspanServer.setProfile(neighbor, EnbProfiles.Mobility_Profile, neighbourMobilityProfile);
			deleteNeighbours();
			GeneralUtils.stopLevel();
		}
	}

	@Test
	@TestProperties(name = "BasicHO_X2IntraFrequency", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void BasicHO_X2IntraFrequency() {
		isIntra = true;
		reason = "";
		if (enodeB.isInstanceOfXLP_15_2()) {
			succCounterName = "HoX2IntraFreqInCompSuccRnlRadioRsn";
			succCounterNameSNMP = "HoX2IntraFreqInCompSuccRnlRadioRsn";
			attCounterName = "HoX2IntraFreqInAttRnlRadioRsn";
			attCounterNameSNMP = "HoX2IntraFreqInAttRnlRadioRsn";

		} else {
			succCounterName = "X2IntraFreqInCompSuccRnlRadioRsn";
			succCounterNameSNMP = "asLteStatsHoX2IntraFreqInCompSuccRnlRadioRsn";
			attCounterName = "X2IntraFreqInAttRnlRadioRsn";
			attCounterNameSNMP = "asLteStatsHoX2IntraFreqInAttRnlRadioRsn";
		}

		passCriteria.put(succCounterName, succCounterNameSNMP);
		passCriteria.put(attCounterName, attCounterNameSNMP);
		performHoOneTime(HoControlStateTypes.ALLOWED, X2ControlStateTypes.AUTOMATIC, HandoverType.TRIGGER_X_2,
				ConnectedModeEventTypes.A_3);
		postTest();
	}

	// @Test
	// @TestProperties(name = "BasicHO_X2ForwardIntraFrequency", returnParam =
	// {"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void BasicHO_X2ForwardIntraFrequency() {
		passCriteria.put("HoX2IntraFreqInAttRnlRadioRsn", "asLteStatsHoX2IntraFreqInAttRnlRadioRsn");
		passCriteria.put("HoX2IntraFreqInCompSuccRnlRadioRsn", "asLteStatsHoX2IntraFreqInCompSuccRnlRadioRsn");
		isIntra = true;
		reason = "";
		String forward = MibReader.getInstance().resolveByName("asLteStkCellCfgFwdHoEnable");
		performHoOneTime(HoControlStateTypes.ALLOWED, X2ControlStateTypes.NOT_ALLOWED, HandoverType.S_1_ONLY,
				ConnectedModeEventTypes.A_3);
		postTest();
		try {
			enodeB.snmpSet(forward, 0);
		} catch (IOException e) {
			report.report("Couldn't set snmp", Reporter.WARNING);
			e.printStackTrace();
		}
		report.report("Changed flag fwdHoEnable to disable");
	}

	@Test
	@TestProperties(name = "BasicHO_S1IntraFrequency", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void BasicHO_S1IntraFrequency() {
		isIntra = true;
		reason = "";
		if (enodeB.isInstanceOfXLP_15_2()) {
			succCounterName = "HoS1IntraFreqInCompSuccRnlRadioRsn";
			succCounterNameSNMP = "HoS1IntraFreqInCompSuccRnlRadioRsn";
			attCounterName = "HoS1IntraFreqInPrepSuccRnlRadioRsn";
			attCounterNameSNMP = "HoS1IntraFreqInPrepSuccRnlRadioRsn";

		} else {
			succCounterName = "S1IntraFreqInCompSuccRnlRadioRsn";
			succCounterNameSNMP = "asLteStatsHoS1IntraFreqInCompSuccRnlRadioRsn";
			attCounterName = "S1IntraFreqInPrepSuccRnlRadioRsn";
			attCounterNameSNMP = "asLteStatsHoS1IntraFreqInPrepSuccRnlRadioRsn";
		}
		passCriteria.put(succCounterName, succCounterNameSNMP);
		passCriteria.put(attCounterName, attCounterNameSNMP);
		performHoOneTime(HoControlStateTypes.ALLOWED, X2ControlStateTypes.NOT_ALLOWED, HandoverType.S_1_ONLY,
				ConnectedModeEventTypes.A_3);
		postTest();
	}

	/* Basic HO - X2 Inter Frequency */
	@Test
	@TestProperties(name = "BasicHO_X2InterFrequency", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void BasicHO_X2InterFrequency() {
		isIntra = false;
		reason = "";
		passCriteria.put("HoX2InterFreqInAttRnlRadioRsn", "HoX2InterFreqInAttRnlRadioRsn");
		passCriteria.put("HoX2InterFreqInCompSuccRnlRadioRsn", "HoX2InterFreqInCompSuccRnlRadioRsn");
		performHoOneTime(HoControlStateTypes.ALLOWED, X2ControlStateTypes.AUTOMATIC, HandoverType.TRIGGER_X_2,
				ConnectedModeEventTypes.A_3);
		postTest();
	}

	@Test
	@TestProperties(name = "BasicHO_S1InterFrequency", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void BasicHO_S1InterFrequency() {
		isIntra = false;
		reason = "";
		passCriteria.put("HoS1InterFreqInAttRnlRadioRsn", "HoS1InterFreqInAttRnlRadioRsn");
		passCriteria.put("HoS1InterFreqInCompSuccRnlRadioRsn", "HoS1InterFreqInCompSuccRnlRadioRsn");
		performHoOneTime(HoControlStateTypes.ALLOWED, X2ControlStateTypes.NOT_ALLOWED, HandoverType.S_1_ONLY,
				ConnectedModeEventTypes.A_3);
		postTest();
	}

	@Test
	@TestProperties(name = "MultipleHO_X2InterFrequency_Stability", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void MultipleHO_X2InterFrequency_Stability() {
		maxduration = STABILITY_TEST_DURATION;
		MultipleHO_X2InterFrequency();
	}

	private void helperPreTestAndHOLongTest(HoControlStateTypes hoControl, X2ControlStateTypes x2Control,
			HandoverType hoType, ConnectedModeEventTypes hoEventType) {
		boolean action = true;
		action &= preTest(hoControl, x2Control, hoType, hoEventType);
		if(action)
			action &= HoLongTest();
		if (!action)
			report.report("Failure reason: " + reason, Reporter.FAIL);
	}

	@Test
	@TestProperties(name = "MultipleHO_X2InterFrequency", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void MultipleHO_X2InterFrequency() {
		isIntra = false;
		reason = "";
		if (enodeB.isInstanceOfXLP_15_2()) {
			succCounterName = "HoX2InterFreqInCompSuccRnlRadioRsn";
			succCounterNameSNMP = "HoX2InterFreqInCompSuccRnlRadioRsn";
			attCounterName = "HoX2InterFreqInPrepSuccRnlRadioRsn";
			attCounterNameSNMP = "HoX2InterFreqInPrepSuccRnlRadioRsn";
		} else {
			succCounterName = "X2InterFreqInCompSuccRnlRadioRsn";
			succCounterNameSNMP = "asLteStatsHoX2InterFreqInCompSuccRnlRadioRsn";
			attCounterName = "X2InterFreqInPrepSuccRnlRadioRsn";
			attCounterNameSNMP = "asLteStatsHoX2InterFreqInPrepSuccRnlRadioRsn";
		}

		passCriteria.put(succCounterName, succCounterNameSNMP);
		passCriteria.put(attCounterName, attCounterNameSNMP);

		helperPreTestAndHOLongTest(HoControlStateTypes.ALLOWED, X2ControlStateTypes.AUTOMATIC, HandoverType.TRIGGER_X_2,
				ConnectedModeEventTypes.A_3);
		postTest();
	}

	@Test
	@TestProperties(name = "MultipleHO_S1InterFrequency_Stability", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void MultipleHO_S1InterFrequency_Stability() {
		maxduration = STABILITY_TEST_DURATION;
		MultipleHO_S1InterFrequency();
	}

	@Test
	@TestProperties(name = "MultipleHO_S1InterFrequency", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void MultipleHO_S1InterFrequency() {
		isIntra = false;
		reason = "";
		if (enodeB.isInstanceOfXLP_15_2()) {
			succCounterName = "HoS1InterFreqInCompSuccRnlRadioRsn";
			succCounterNameSNMP = "HoS1InterFreqInCompSuccRnlRadioRsn";
			attCounterName = "HoS1InterFreqInPrepSuccRnlRadioRsn";
			attCounterNameSNMP = "HoS1InterFreqInPrepSuccRnlRadioRsn";

		} else {
			succCounterName = "S1InterFreqInCompSuccRnlRadioRsn";
			succCounterNameSNMP = "asLteStatsHoS1InterFreqInCompSuccRnlRadioRsn";
			attCounterName = "S1InterFreqInPrepSuccRnlRadioRsn";
			attCounterNameSNMP = "asLteStatsHoS1InterFreqInPrepSuccRnlRadioRsn";
		}

		passCriteria.put(succCounterName, succCounterNameSNMP);
		passCriteria.put(attCounterName, attCounterNameSNMP);

		helperPreTestAndHOLongTest(HoControlStateTypes.ALLOWED, X2ControlStateTypes.NOT_ALLOWED, HandoverType.S_1_ONLY,
				ConnectedModeEventTypes.A_3);
		postTest();
	}

	@Test
	@TestProperties(name = "MultipleHO_X2IntraFrequency_Stability", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void MultipleHO_X2IntraFrequency_Stability() {
		maxduration = STABILITY_TEST_DURATION;
		MultipleHO_X2IntraFrequency();
	}

	@Test
	@TestProperties(name = "MultipleHO_X2IntraFrequency", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void MultipleHO_X2IntraFrequency() {
		isIntra = true;
		reason = "";
		setX2IntraCounters();

		helperPreTestAndHOLongTest(HoControlStateTypes.ALLOWED, X2ControlStateTypes.AUTOMATIC, HandoverType.TRIGGER_X_2,
				ConnectedModeEventTypes.A_3);
		postTest();
	}

	@Test
	@TestProperties(name = "MultipleHO_X2IntraFrequency_A4", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void MultipleHO_X2IntraFrequency_A4() {
		isIntra = true;
		reason = "";

		setX2IntraCounters();

		helperPreTestAndHOLongTest(HoControlStateTypes.ALLOWED, X2ControlStateTypes.AUTOMATIC, HandoverType.TRIGGER_X_2,
				ConnectedModeEventTypes.A_4);
		postTest();
	}

	private void setX2IntraCounters() {
		if (enodeB.isInstanceOfXLP_15_2()) {
			succCounterName = "HoX2IntraFreqInCompSuccRnlRadioRsn";
			succCounterNameSNMP = "HoX2IntraFreqInCompSuccRnlRadioRsn";
			attCounterName = "HoX2IntraFreqInAttRnlRadioRsn";
			attCounterNameSNMP = "HoX2IntraFreqInAttRnlRadioRsn";
		} else {
			succCounterName = "X2IntraFreqInCompSuccRnlRadioRsn";
			succCounterNameSNMP = "asLteStatsHoX2IntraFreqInCompSuccRnlRadioRsn";
			attCounterName = "X2IntraFreqInAttRnlRadioRsn";
			attCounterNameSNMP = "asLteStatsHoX2IntraFreqInAttRnlRadioRsn";
		}
		passCriteria.put(succCounterName, succCounterNameSNMP);
		passCriteria.put(attCounterName, attCounterNameSNMP);
	}

	@Test
	@TestProperties(name = "MultipleHO_S1IntraFrequency_Stability", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void MultipleHO_S1IntraFrequency_Stability() {
		maxduration = STABILITY_TEST_DURATION;
		MultipleHO_S1IntraFrequency();
	}

	@Test
	@TestProperties(name = "MultipleHO_S1IntraFrequency", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void MultipleHO_S1IntraFrequency() {
		isIntra = true;
		reason = "";
		if (enodeB.isInstanceOfXLP_15_2()) {
			succCounterName = "HoS1IntraFreqInCompSuccRnlRadioRsn";
			succCounterNameSNMP = "HoS1IntraFreqInCompSuccRnlRadioRsn";
			attCounterName = "HoS1IntraFreqInPrepSuccRnlRadioRsn";
			attCounterNameSNMP = "HoS1IntraFreqInPrepSuccRnlRadioRsn";

		} else {
			succCounterName = "S1IntraFreqInCompSuccRnlRadioRsn";
			succCounterNameSNMP = "asLteStatsHoS1IntraFreqInCompSuccRnlRadioRsn";
			attCounterName = "S1IntraFreqInPrepSuccRnlRadioRsn";
			attCounterNameSNMP = "asLteStatsHoS1IntraFreqInPrepSuccRnlRadioRsn";
		}
		passCriteria.put(succCounterName, succCounterNameSNMP);
		passCriteria.put(attCounterName, attCounterNameSNMP);

		helperPreTestAndHOLongTest(HoControlStateTypes.ALLOWED, X2ControlStateTypes.NOT_ALLOWED, HandoverType.S_1_ONLY,
				ConnectedModeEventTypes.A_3);
		postTest();
	}

	@Test
	@TestProperties(name = "MultipleHO_S1IntraFrequencyNoPreConfig", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void MultipleHO_S1IntraFrequencyNoPreConfig() {
		isConfigNedded = false;
		MultipleHO_S1IntraFrequency();
	}

	@Test
	@TestProperties(name = "MultipleHO_X2IntraFrequencyNoPreConfig", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void MultipleHO_X2IntraFrequencyNoPreConfig() {
		isConfigNedded = false;
		MultipleHO_X2IntraFrequency();
	}

	@Test
	@TestProperties(name = "MultipleHO_S1InterFrequencyNoPreConfig", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void MultipleHO_S1InterFrequencyNoPreConfig() {
		isConfigNedded = false;
		MultipleHO_S1InterFrequency();
	}

	@Test
	@TestProperties(name = "MultipleHO_X2InterFrequencyNoPreConfig", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void MultipleHO_X2InterFrequencyNoPreConfig() {
		isConfigNedded = false;
		MultipleHO_X2InterFrequency();
	}

	public boolean performHO() {
		boolean flag1 = true;
		boolean flag2 = true;

		flag1 = resetPassCriteriaCounters();
		if (!flag1)
			return false;

		GeneralUtils.startLevel("Performing 2 side HO.");
		report.report("Moving attenuator up to create Hand-Over");
		moveAtt(attenuatorSetUnderTest, attenuatorMin, attenuatorMax);
		report.report("Moving attenuator down to create Hand-Over");
		moveAtt(attenuatorSetUnderTest, attenuatorMax, attenuatorMin);

		enodeBConfig.waitGranularityPeriodTime(dut1);
		expectedNumOfHO = (int) Math.ceil(dynUEList.size() * 0.75);
		report.report(String.format("Expecting at list %s successful handovers on every node", expectedNumOfHO));
		flag1 = verifyCountersSingleHO(neighbor);
		if (!flag1) {
			report.report("HO wasn't performed as expected", Reporter.WARNING);
			reason = "HO wasn't performed as expected";
		} else
			report.step("Ho performed!");

		flag2 = verifyCountersSingleHO(enodeB);
		if (!flag2) {
			report.report("HO wasn't preformed as expected", Reporter.WARNING);
			reason = "HO wasn't performed as expected";

		} else {
			report.step("Ho preformed!");
		}
		GeneralUtils.stopLevel();
		return flag1 && flag2;
	}

	public void performHoOneTime(HoControlStateTypes HOControlTypes, X2ControlStateTypes X2Types, HandoverType HOType,
			ConnectedModeEventTypes hoEventType) {
		boolean action = true;
		report.report("HO Test");
		action &= preTest(HOControlTypes, X2Types, HOType, hoEventType);
		if(action)
			action &= performHO();
		if (!action) {
			report.report("HO Test Falied. Failure reason: " + reason, Reporter.FAIL);
		}
	}

	public boolean HoLongTest() {
		boolean flag = true;

		GeneralUtils.startLevel("getting counters value");
		resetPassCriteriaCounters();

		if (!gettingPassCratiriaValue()) {
			report.report("Retrying to reset pass criteria counters");
			resetPassCriteriaCounters();
			enodeBConfig.waitGranularityPeriodTime(dut1);
			if (!gettingPassCratiriaValue()) {
				GeneralUtils.stopLevel(); // getting counters value stop level
				return false;
			}
		}
		GeneralUtils.stopLevel(); // getting counters value stop level
		long startTime = System.currentTimeMillis();
		long endTime = System.currentTimeMillis() + maxduration;
		long resetCounters = startTime;
		int counter = 0;
		GeneralUtils.startLevel("Moving attenuator for " + maxduration / 60000 + " minutes to create Hand-Over");
		report.report("Attenuator IP: " + attenuatorSetUnderTest.getName());
		int waitingPeriodTime = dut1.getGranularityPeriod();
		while (System.currentTimeMillis() < endTime) {
			moveAtt(attenuatorSetUnderTest, attenuatorMin, attenuatorMax);
			counter++;
			if (System.currentTimeMillis() > endTime) {
				break;
			}
			moveAtt(attenuatorSetUnderTest, attenuatorMax, attenuatorMin);
			counter++;
			if (System.currentTimeMillis() - resetCounters >= 8 * 60 * 1000 * waitingPeriodTime) {
				resetCounters = System.currentTimeMillis();
				double temp = Math.floor(((System.currentTimeMillis() - startTime) / 1000 / 60.0) * 100);
				double tempDouble = temp / 100.0;
				report.report("Time elapsed: " + tempDouble + " minutes.");
				updateResultsVariables(counter);
				resetCounters();
			}
		}
		GeneralUtils.stopLevel(); // moving attenuator stop level
		report.report("Wait for " + waitingPeriodTime + " minutes for Counter to update");
		GeneralUtils.unSafeSleep(waitingPeriodTime * 60 * 1000);

		flag = LongHOResults(flag, counter);
		return flag;
	}

	/**
	 * Main add Neighbours method - This method will add neighbour. supports 2
	 * cases: a. the basic of Single cell b. unique case of 2 cells on each
	 * EnodeB Defined to 2 retries
	 *
	 * @param enodeB
	 *            - enodeB
	 * @param neighbor
	 *            - neighbor
	 * @param HOControlTypes
	 *            - HOControlTypes
	 * @param X2Types
	 *            - X2Types
	 * @param HOType
	 *            - HOType
	 * @param isStaticNeighbor
	 *            - isStaticNeighbor
	 * @param qOffsetRange
	 *            - qOffsetRange
	 * @return true if succeed
	 */
	public boolean addNeighbours(EnodeB enodeB, EnodeB neighbor, HoControlStateTypes HOControlTypes,
			X2ControlStateTypes X2Types, HandoverType HOType, boolean isStaticNeighbor, String qOffsetRange) {
		if (skipAddNeighbourIfNeeded())
			return true;
		GeneralUtils.startLevel("Adding neighbor " + neighbor.getName() + " to EnodeB " + enodeB.getName());
		int enbNumbersOfCells = netspanServer.getNumberOfNetspanCells(enodeB);
		int nbrNumbersOfCells = netspanServer.getNumberOfNetspanCells(neighbor);
		// Special case when we have multi cell -> add both of them.
		if (enbNumbersOfCells == nbrNumbersOfCells && enbNumbersOfCells == 2) {
			if (!addNeighboursSingleOrMultiCell(enodeB, neighbor, HOControlTypes, X2Types, HOType, isStaticNeighbor,
					qOffsetRange, true)) {
				reason = "Neighbor wasn't added to EnodeB\n";
				return false;
			}
		} else {
			// Default case of adding 1 neighbour
			if (!addNeighboursSingleOrMultiCell(enodeB, neighbor, HOControlTypes, X2Types, HOType, isStaticNeighbor,
					qOffsetRange, false)) {
				reason = "Neighbor wasn't added to EnodeB\n";
				return false;
			}
		}
		report.reportHtml(enodeB.getName() + ": db get nghList", enodeB.lteCli("db get nghList"), true);
		report.reportHtml(neighbor.getName() + ": db get nghList", neighbor.lteCli("db get nghList"), true);
		GeneralUtils.stopLevel();
		return true;
	}

	/**
	 * skip Add Neighbour If Needed according to skipAddNgh param
	 *
	 * @return - true if skip is needed
	 */
	private boolean skipAddNeighbourIfNeeded() {
		if (skipAddNgh) {
			report.report("Skipping adding neighbor.");
			return true;
		}
		return false;
	}

	/**
	 * Added neighbour - Defined to 2 retries. Depend on the cell number -
	 * supports Multi and Single cell.
	 *
	 * @param enodeB
	 *            - enodeB
	 * @param neighbor
	 *            - neighbor
	 * @param HOControlTypes
	 *            - HOControlTypes
	 * @param X2Types
	 *            - X2Types
	 * @param HOType
	 *            - HOType
	 * @param isStaticNeighbor
	 *            - isStaticNeighbor
	 * @param qOffsetRange
	 *            - qOffsetRange
	 * @param isMultiCell
	 *            - qOffsetRange
	 * @return - true if succeed
	 */
	private boolean addNeighboursSingleOrMultiCell(EnodeB enodeB, EnodeB neighbor, HoControlStateTypes HOControlTypes,
			X2ControlStateTypes X2Types, HandoverType HOType, boolean isStaticNeighbor, String qOffsetRange,
			boolean isMultiCell) {
		boolean isNeighbourAdded;

		if (isMultiCell) {
			isNeighbourAdded = neighbors.addNeighborMultiCell(enodeB, neighbor, HOControlTypes, X2Types, HOType,
					isStaticNeighbor, qOffsetRange);
		} else {
			isNeighbourAdded = neighbors.addNeighbor(enodeB, neighbor, HOControlTypes, X2Types, HOType,
					isStaticNeighbor, qOffsetRange);
		}
		// Verification
		if (isNeighbourAdded) {
			report.report("Neighbor was added to EnodeB");
			report.report("HO control type: " + HOControlTypes);
			report.report("X2 Type: " + X2Types);
			report.report("HO Type: " + HOType);
			return true;
		} else {
			report.report("Neighbor wasn't added to EnodeB", Reporter.WARNING);
			GeneralUtils.stopLevel(); // adding neighbor stop level
		}
		return false;
	}

	private boolean verifyCountersSingleHO(EnodeB enb) {
		GeneralUtils.startLevel("Counters value for EnodeB " + enb.getNetspanName());
		boolean isSucceeded = true;
		for (String counter : passCriteria.values()) {
			int counterSum = enb.getCountersValue(counter);
			if (counterSum >= expectedNumOfHO) {
				report.report("[Pass Criteria: ] EnodeB: " + enb.getName() + " counter " + counter + " value is: "
						+ counterSum + " as expected");
			} else {
				report.report("[Pass Criteria: ] EnodeB: " + enb.getName() + " counter " + counter + " value is: "
						+ counterSum + " instead of " + expectedNumOfHO, Reporter.WARNING);
				isSucceeded = false;
			}
		}
		GeneralUtils.stopLevel();
		return isSucceeded;

	}

	private boolean resetPassCriteriaCounters() {
		GeneralUtils.startLevel("changing counters value to 0");
		boolean isSucceeded = true;
		for (int i = 0; i < 2; i++) {
			resetCounters();
			enodeBConfig.waitGranularityPeriodTime(dut1);
			isSucceeded = verifyCountersReset(i);
			if (isSucceeded)
				break;

			if (i == 0)
				report.report("Counters values are not 0, reseting counters to 0 again.");
			else
				break;
		}
		first_ENB_succ = 0;
		first_ENB_att = 0;

		second_ENB_succ = 0;
		second_ENB_att = 0;
		GeneralUtils.stopLevel();
		return isSucceeded;
	}

	private void resetCounters() {
		for (String counter : passCriteria.keySet()) {
			HashMap<String, String> counterValue = new HashMap<>();
			counterValue.put(counter, "0");

			boolean result1 = enodeB.resetCounter("hostats", "*", counterValue);
			if (!result1) {
				enodeB.resetCounter("hostats", "*", counterValue);
			}
			boolean result2 = neighbor.resetCounter("hostats", "*", counterValue);
			if (!result2) {
				neighbor.resetCounter("hostats", "*", counterValue);
			}
			GeneralUtils.printToConsole(enodeB.getName() + " set value: " + String.valueOf(result1));
			GeneralUtils.printToConsole(neighbor.getName() + " set value: " + String.valueOf(result2));
		}
	}

	private boolean verifyCountersReset(int i) {
		boolean isSucceeded = true;
		for (String counter : passCriteria.values()) {
			int mainCounterValue = enodeB.getCountersValue(counter);
			if (mainCounterValue != 0) {
				if (i == 1) {
					report.report("Counter " + counter + " values are " + mainCounterValue + " instead of 0 in enodeB: "
							+ enodeB.getName(), Reporter.WARNING);
					reason = "Counter values are not 0 in enodeB";
				}
				isSucceeded = false;
			} else
				report.report("Counter " + counter + " values are 0 in enodeB: " + enodeB.getName());
			int neighborCounterValue = neighbor.getCountersValue(counter);
			if (neighborCounterValue != 0) {
				if (i == 1) {
					report.report("Counter " + counter + " values are " + neighborCounterValue
							+ " instead of 0 in enodeB: " + neighbor.getName(), Reporter.WARNING);
					reason = "Counter values are not 0 in enodeB";
				}
				isSucceeded = false;
			} else
				report.report("Counter " + counter + " values are 0 in enodeB: " + neighbor.getName());
		}
		return isSucceeded;
	}

	private void moveAtt(AttenuatorSet attenuatorSetUnderTest, int from, int to) {
		int multi = 1;
		int attenuatorsCurrentValue = from;
		if (from > to)
			multi = -1;
		int steps = Math.abs(from - to) / ATT_STEP;
		for (; steps >= 0; steps--) {
			long beforeAtt = System.currentTimeMillis();
			if (!peripheralsConfig.setAttenuatorSetValue(attenuatorSetUnderTest, attenuatorsCurrentValue)) {
				reason = "Failed to set attenuator value";
			}
			attenuatorsCurrentValue += (ATT_STEP * multi);
			long afterAtt = System.currentTimeMillis();
			long diff = afterAtt - beforeAtt;
			if (diff < ATT_STEP_TIME)
				GeneralUtils.unSafeSleep(ATT_STEP_TIME - diff);
		}
	}

	private boolean LongHOResults(boolean flag, int counter) {
		updateResultsVariables(counter);

		GeneralUtils.startLevel("Results");
		report.report("Attenuator step: " + ATT_STEP);
		report.report("Attenuator timing: " + ATT_STEP_TIME + "ms");
		report.report("Attenuation boundaries: " + attenuatorMin + "-" + attenuatorMax);
		report.report("Number of dynamic UE's: " + numberOfDynamicUES);
		report.report("Number of expected HO's: " + counter * numberOfDynamicUES);

		GeneralUtils.startLevel("number of expected HO's per minute per UE: " + HO_Per_Min_Per_UE);
		report.report("total Attenuator moves / test duration in minutes / number of dynamic UES");
		GeneralUtils.stopLevel(); // number of expected HO's per minute stop
									// level

		GeneralUtils.startLevel("Number of attempts: " + total_attCounter);
		report.report("SNMP MIB: " + attCounterNameSNMP + " On Both EnodeBs");
		report.report(enodeB.getNetspanName() + ": " + first_ENB_att);
		report.report(neighbor.getNetspanName() + ": " + second_ENB_att);
		GeneralUtils.stopLevel();// Number of attempts: stop level

		GeneralUtils.startLevel("Number of HO's: " + total_succCounter);
		report.report("SNMP MIB: " + succCounterNameSNMP + " On Both EnodeBs");
		report.report(enodeB.getNetspanName() + ": " + first_ENB_succ);
		report.report(neighbor.getNetspanName() + ": " + second_ENB_succ);
		GeneralUtils.stopLevel(); // Number of HO's: stop level

		if (total_attCounter == 0) {
			report.report("There were no attempts - test failed", Reporter.WARNING);
			reason = "There were no attempts";
			flag = false;
		} else {
			if (theoreticalAttempts < 60) {
				GeneralUtils.startLevel("Number of attempts out of real attenuator moves is: "
						+ new DecimalFormat("##.##").format(theoreticalAttempts) + "%");
				report.report("Number of attempts is less then 60%", Reporter.WARNING);
				reason = "Number of attempts is less then 60 percent";
				report.report("Number of attempts / attenuator moves");
				GeneralUtils.stopLevel();
				flag = false;
			} else {
				if (theoreticalAttempts < 85) {
					GeneralUtils.startLevel("Number of attempts out of real attenuator moves is: "
							+ new DecimalFormat("##.##").format(theoreticalAttempts) + "%");
					report.report("Number of attempts is less then 85%", Reporter.WARNING);
					reason = "Number of attempts is less then 85 percent";
					report.report("Number of attempts / attenuator moves");
					GeneralUtils.stopLevel();
				} else {
					GeneralUtils.startLevel("Number of attempts out of real attenuator moves is: "
							+ new DecimalFormat("##.##").format(theoreticalAttempts) + "%");
					report.report("Number of attempts / attenuator moves");
					GeneralUtils.stopLevel();
				}

				GeneralUtils.startLevel("Pass Criteria: Success rate (out of attempts): "
						+ GeneralUtils.fractureToPercent(total_HO_Success_rate_out_of_attempts) + "%");
				report.report("SNMP MIB: " + succCounterNameSNMP + " On Both EnodeBs / SNMP MIB: " + attCounterNameSNMP
						+ " On Both EnodeBs");
				if (total_HO_Success_rate_out_of_attempts < WARNING_LEVEL) {
					report.report(
							"Success rate lower then Threshold (" + GeneralUtils.fractureToPercent(PASS_LEVEL) + ")",
							Reporter.WARNING);
					reason = "Success rate is: " + GeneralUtils.fractureToPercent(total_HO_Success_rate_out_of_attempts)
							+ " percent";
					flag = false;
				}
				if (total_HO_Success_rate_out_of_attempts > WARNING_LEVEL
						&& total_HO_Success_rate_out_of_attempts < PASS_LEVEL) {
					report.report(
							"Success rate lower then Threshold (" + GeneralUtils.fractureToPercent(PASS_LEVEL) + ")",
							Reporter.WARNING);
					reason = "Success rate is: "
							+ new DecimalFormat("##.##").format(total_HO_Success_rate_out_of_attempts * 100)
							+ " percent";
				}

				report.report(enodeB.getNetspanName() + ": Success rate (out of attempts): "
						+ GeneralUtils.fractureToPercent(firstENB_HO_Success_rate_out_of_attempts));
				report.report(neighbor.getNetspanName() + ": Success rate (out of attempts): "
						+ GeneralUtils.fractureToPercent(secondENB_HO_Success_rate_out_of_attempts));
				GeneralUtils.stopLevel(); // Pass Criteria: Success rate (out of
			}
		}
		GeneralUtils.stopLevel(); // Results stop level
		return flag;
	}

	private void updateResultsVariables(int counter) {
		numberOfDynamicUES = dynUEList.size();
		HO_Per_Min_Per_UE = counter * 1.0 / GeneralUtils.milisToMinutes(maxduration) * 1.0;

		first_ENB_succ += enodeB.getCountersValue(succCounterNameSNMP);
		first_ENB_att += enodeB.getCountersValue(attCounterNameSNMP);
		firstENB_HO_Success_rate_out_of_attempts = (first_ENB_succ * 1.0) / (first_ENB_att * 1.0);

		second_ENB_succ += neighbor.getCountersValue(succCounterNameSNMP);
		second_ENB_att += neighbor.getCountersValue(attCounterNameSNMP);
		secondENB_HO_Success_rate_out_of_attempts = (second_ENB_succ * 1.0) / (second_ENB_att * 1.0);

		total_succCounter = first_ENB_succ + second_ENB_succ;
		total_attCounter = first_ENB_att + second_ENB_att;
		total_HO_Success_rate_out_of_attempts = (total_succCounter * 1.0) / (total_attCounter * 1.0);

		theoreticalAttempts = (total_attCounter * 1.0) / (counter * numberOfDynamicUES) * 100;
	}

	@ParameterProperties(description = "First DUT")
	public void setDUT1(String dut) {
		GeneralUtils.printToConsole("Load DUT1" + dut);
		this.dut1 = (EnodeB) SysObjUtils.getInstnce().initSystemObject(EnodeB.class, false, dut).get(0);
		GeneralUtils.printToConsole("DUT loaded" + this.dut1.getNetspanName());
	}

	@ParameterProperties(description = "Second DUT")
	public void setDUT2(String dut) {
		GeneralUtils.printToConsole("Load DUT2" + dut);
		this.dut2 = (EnodeB) SysObjUtils.getInstnce().initSystemObject(EnodeB.class, false, dut).get(0);
		GeneralUtils.printToConsole("DUT loaded" + this.dut2.getNetspanName());
	}

	public EnodeB getDut1() {
		return dut1;
	}

	public EnodeB getDut2() {
		return dut2;
	}

	public void resetUes() {
		GeneralUtils.startLevel("Reset dynamic Ues");
		peripheralsConfig.rebootUEs(SetupUtils.getInstance().getAllUEs());

		GeneralUtils.unSafeSleep(10000);
		if (!peripheralsConfig.epcAndEnodeBsConnection(SetupUtils.getInstance().getAllUEs(), enbInTest)) {
			peripheralsConfig.rebootUEs(SetupUtils.getInstance().getAllUEs(), 120000);
			if (!peripheralsConfig.epcAndEnodeBsConnection(SetupUtils.getInstance().getAllUEs(), enbInTest)) {
				GeneralUtils.startLevel("Removing UEs from test due to multiple failures.");
				for (UE ue : SetupUtils.getInstance().getAllUEs()) {
					ArrayList<UE> ueList = new ArrayList<>();
					ueList.add(ue);
					if (!peripheralsConfig.epcAndEnodeBsConnection(ueList, enbInTest)) {
						report.report(String.format("Removing ue \"%s\"", ue.getName()));
						dynUEList.remove(ue);
						statUEList.remove(ue);
					}
				}
				GeneralUtils.stopLevel();
			}
		}
		GeneralUtils.stopLevel();
	}

	@Override
	public void end() {
		if (traffic != null && traffic.getGeneratorType() == TrafficGeneratorType.TestCenter) {
			String TrafficFile = traffic.getTg().getDefaultConfigTccFile();
			traffic.configFile = new File(TrafficFile);
		}
		terminateWatchDog();
		super.end();
	}

	private void terminateWatchDog() {
		if (wd == null) {
			return;
		}
		try {
			// creating a file and writing content from WatchDog command from
			// test.
			GeneralUtils.printToConsole("Shutting down Watch Dog object");
			wd.removeCommand(wdDynamicUEs);
			wd.shutDown();
			StreamList tablePrinter = new StreamList();
			GeneralUtils.printToConsole("getting table from StreamList object");
			tablePrinter = wdDynamicUEs.getTable();
			GeneralUtils.printToConsole("Trying to print table");
			report.reportHtml("UEs statistics:", tablePrinter.printTablesHtmlForStream("UEs statistics:"), true);
			tablePrinter = wdDynamicUEs.getRsrpTable();
			GeneralUtils.printToConsole("Trying to print rsrp table");
			GeneralUtils.reportHtmlLink("UEs RSRP data:", tablePrinter.printTablesHtmlForStream("UEs RSRP data:"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void commands() {

		ArrayList<String> commands = new ArrayList<>();
		commands.add("ue show link");
		commands.add("ue show rate");
		commands.add("rlc show amdlstats");
		commands.add("system show error");
		commands.add("system show phyerror");
		commands.add("system show memory");
		commands.add("db get nghlist");
		commands.add("enb show ngh");
		commands.add("db get cellCfg");
		commands.add("db get PTPStatus");

		try {
			commandsThread1 = new ParallelCommandsThread(commands, dut1, null, null);
			commandsThread2 = new ParallelCommandsThread(commands, dut2, null, null);
		} catch (IOException e) {
			report.report("could not init Commands in parallel", Reporter.WARNING);
		}
		commandsThread1.start();
		commandsThread2.start();

	}

	private void stopCommandsAndAttachFiles() {
		commandsThread1.stopCommands();
		commandsThread2.stopCommands();
		commandsThread1.moveFileToReporterAndAddLink();
		commandsThread2.moveFileToReporterAndAddLink();
	}

	private void restartCommandsThread() {
		stopCommandsAndAttachFiles();
		commands();
	}

	private boolean checkFrequency() {
		GeneralUtils.startLevel("Checking that frequencies match the test type.");
		Integer enbEarfcn = enodeBConfig.getEARFCNforNode(enodeB);
		Integer neighborEarfcn = enodeBConfig.getEARFCNforNode(neighbor);

		report.report(enodeB.getName() + " Earfcn: " + enbEarfcn);
		report.report(neighbor.getName() + " Earfcn: " + neighborEarfcn);

		if (isIntra) {
			if (!enbEarfcn.equals(neighborEarfcn)) {
				report.report("EnodeB frequences are diffrent in an Intra test.", Reporter.FAIL);
				reason = "EnodeB frequences are diffrent in an Intra test.";
				GeneralUtils.stopLevel();
				return false;
			}
		} else if (enbEarfcn.equals(neighborEarfcn)) {
			report.report("EnodeB frequences are equal in an Inter test.", Reporter.FAIL);
			reason = "EnodeB frequences are equal in an Inter test.";
			GeneralUtils.stopLevel();
			return false;
		}

		GeneralUtils.stopLevel(); // Checking that frequencies match SUT
									// stop level
		return true;
	}

	private boolean gettingPassCratiriaValue() {
		boolean tempFlag = true;
		GeneralUtils.startLevel("Getting Pass Criteria counters value");
		for (String counter : passCriteria.values()) {
			int mainCounterValue = enodeB.getCountersValue(counter);
			if (mainCounterValue != 0) {
				report.report("Counter " + counter + " values are " + mainCounterValue + " instead of 0 in enodeB: "
						+ enodeB.getName(), Reporter.WARNING);
				reason = "Counter values are not 0 in enodeB";
				tempFlag = false;
			} else
				report.report("Counter " + counter + " values are 0 in enodeB: " + enodeB.getName());
			int neighborCounterValue = neighbor.getCountersValue(counter);
			if (neighborCounterValue != 0) {
				report.report("Counter " + counter + " values are " + neighborCounterValue + " instead of 0 in enodeB: "
						+ neighbor.getName(), Reporter.WARNING);
				reason = "Counter values are not 0 in enodeB";
				tempFlag = false;
			} else
				report.report("Counter " + counter + " values are 0 in enodeB: " + neighbor.getName());
		}
		GeneralUtils.stopLevel(); // Getting Pass Criteria counters value stop
									// level
		return tempFlag;
	}

	/**
	 * in case of a Fail - reporting with a warning and watchDog instance is
	 * null.
	 *
	 * @author Moran Goldenberg
	 */
	public boolean BasicHO_X2IntraFrequency_negativeTest_RanShering() {
		UE dynUE = null;
		dynUEList = SetupUtils.getInstance().getDynamicUEs();
		for (UE ue : dynUEList) {
			if (peripheralsConfig.isUEConnected(ue, enbInTest)) {
				dynUE = ue;
				break;
			}
		}
		if (dynUE == null) {
			report.report("There are no active dynamic UEs in the test");
			return false;
		}
		GeneralUtils.startLevel("Checking what is the main EnodeB");
		EnodeB currentEnb = epc.getCurrentEnodB(dynUE, enbInTest);
		if (currentEnb == null) {
			report.report("Dynamic UE is not connected", Reporter.WARNING);
			reason = "Dynamic UE is not connected";
			GeneralUtils.stopLevel(); // Checking what is the main EnodeB stop
			// level
			return false;
		} else {
			enodeB = currentEnb;
			neighbor = enodeB == dut1 ? dut2 : dut1;
			report.report("Main EnodeB: " + enodeB.getNetspanName());
			report.report("Neighbor EnodeB: " + neighbor.getNetspanName());
		}
		expectedNumOfHO = (int) Math.ceil(dynUEList.size());

		GeneralUtils.stopLevel();

		GeneralUtils.startLevel("Deleting all neighbors");
		neighbors.deleteAllNeighbors(dut1);
		neighbors.deleteAllNeighbors(dut2);
		GeneralUtils.stopLevel();// deleting all neighbors stop level

		GeneralUtils.startLevel("make enodeB's neighbors");
		if (!addNeighbours(enodeB, neighbor, HoControlStateTypes.ALLOWED, X2ControlStateTypes.AUTOMATIC,
				HandoverType.TRIGGER_X_2, true, "0")) {
			report.report("Couldn't configure enodeB's as neighbors");
			GeneralUtils.stopLevel();
			reason = "Neighbor wasn't added to EnodeB\n";
			return false;
		}
		GeneralUtils.stopLevel();

		GeneralUtils.startLevel("Performing 2 side HO.");
		report.report("Moving attenuator up to create Hand-Over");
		moveAtt(attenuatorSetUnderTest, attenuatorMin, attenuatorMax);
		report.report("Moving attenuator down to create Hand-Over");
		moveAtt(attenuatorSetUnderTest, attenuatorMax, attenuatorMin);

		enodeBConfig.waitGranularityPeriodTime(dut1);

		GeneralUtils.stopLevel();

		GeneralUtils.startLevel("HO results");
		boolean result1 = false;
		boolean result2 = false;
		if (isNegative) {
			result1 = negativeTestResults(enodeB);
			result2 = negativeTestResults(neighbor);
		} else {
			dynUEList.clear();
			ArrayList<UE> ues = null;
			try {
				ues = SetupUtils.getInstance().getDynamicUEs();
			} catch (Exception e) {
				report.report("Couldn't get dynamic list");
				e.printStackTrace();
				return false;
			}
			for (UE ue : ues) {
				if (peripheralsConfig.isUEConnected(ue, enbInTest) && !dynUEList.contains(ue))
					dynUEList.add(ue);
			}
			result1 = verifyCountersSingleHO(dut1);
			result2 = verifyCountersSingleHO(dut2);
		}
		if (!result1 || !result2) {
			GeneralUtils.stopLevel();
			return false;
		}
		GeneralUtils.stopLevel();

		return true;
	}

	private boolean negativeTestResults(EnodeB enb) {
		GeneralUtils.startLevel("Counters value for enodeB " + enb.getNetspanName());

		boolean isSucceeded = true;
		for (String counter : passCriteria.values()) {
			int counterSum = enb.getCountersValue(counter);
			if (counterSum == 0) {
				report.report("[Pass Criteria: ] EnodeB: " + enb.getName() + " counter " + counter + " value is: "
						+ counterSum + " as expected");
			} else {
				report.report("[Pass Criteria: ] EnodeB: " + enb.getName() + " counter " + counter + " value is: "
						+ counterSum, Reporter.WARNING);
				isSucceeded = false;
			}
		}
		GeneralUtils.stopLevel();// counters value stop level
		return isSucceeded;
	}
}

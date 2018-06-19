package testsNG.ProtocolsAndServices.RANSharing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;
import DMTool.DMtool;
import DMTool.PlmnData;
import EPC.EPC;
import EPC.MME;
import EnodeB.EnodeB;
import Netspan.EnbProfiles;
import Netspan.NetspanServer;
import Netspan.API.Enums.EnbStates;
import Netspan.Profiles.NetworkParameters;
import Netspan.Profiles.NetworkParameters.Plmn;
import TestingServices.TestConfig;
import UE.UE;
import Utils.GeneralUtils;
import Utils.SetupUtils;
import Utils.SysObjUtils;
import jsystem.framework.ParameterProperties;
import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;
import testsNG.TestspanTest;
import testsNG.Actions.EnodeBConfig;
import testsNG.Actions.PeripheralsConfig;
import testsNG.Actions.Traffic;
import testsNG.Actions.Utils.ParallelCommandsThread;

public class P0 extends TestspanTest {
	ArrayList<UE> dynUEList = new ArrayList<>();
	public EnodeB dut1;
	public EnodeB dut2;
	private EnodeBConfig enodeBConfig;
	private TestConfig testConfig;
	private EPC epc;
	private Traffic traffic;
	ArrayList<UE> UEList = new ArrayList<>();
	private PeripheralsConfig peripheralsConfig;
	ArrayList<String> passCriteria = new ArrayList<>();
	String firstMIB = "";
	String secondMIB = "";
	ArrayList<UE> expectedUEList = new ArrayList<>();
	private testsNG.ProtocolsAndServices.HandOver.P0 HO;
	private String succCounterName = "";
	private String succCounterNameSNMP = "";
	private String attCounterName = "";
	private String attCounterNameSNMP = "";
	private boolean isSingleDut = false;
	ParallelCommandsThread commandsThread1 = null;
	ParallelCommandsThread commandsThread2 = null;
	ArrayList<Plmn> usedPlmnsInTest;

	private static final int WAITTIME = 5 * 60 * 1000; // 5 minutes

	@Override
	public void init() throws Exception {

		enodeBConfig = EnodeBConfig.getInstance();
		testConfig = TestConfig.getInstace();
		GeneralUtils.startLevel("prepering environment to test");
		if (dut1 == null)
			throw new Exception("DUT1 is not loaded");
		if (dut2 == null)
			throw new Exception("DUT2 is not loaded");

		enbInTest = new ArrayList<>();
		enbInTest.add(dut1);
		enbInTest.add(dut2);

		epc = EPC.getInstance();
		peripheralsConfig = PeripheralsConfig.getInstance();
		NetspanServer netspanServer = NetspanServer.getInstance();
		for (EnodeB enb : enbInTest) {
			String networkProfile = netspanServer.getCurrentNetworkProfileName(enb);
			String sutNetworkProfile = enb.getDefaultNetspanProfiles().getNetwork();
			if (!networkProfile.equals(sutNetworkProfile)) {
				SetProfileBack(enb);
			}
		}
		super.init();
		startCommands();
		GeneralUtils.stopLevel();

	}

	@Test
	@TestProperties(name = "SharedENB_UEattachingFunctionalTest_TwoUEsDifferentPLMN", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void SharedENB_UEattachingFunctionalTest_TwoUEsDifferentPLMN() {
		firstMIB = "RrcConnEstabAttSum";
		secondMIB = "RrcConnEstabSuccSum";
		passCriteria.add(firstMIB);
		passCriteria.add(secondMIB);

		GeneralUtils.startLevel("Finding relevant MME for test ");
		MME currentMME = getMMEbyPLMNnumber(SetupUtils.getInstance().getallUEsPerEnodeb(dut1));
		MME otherMME = getOtherMME(currentMME);
		if (currentMME == null) {
			report.report("No MME with 2 PLMNS", Reporter.FAIL);
			GeneralUtils.stopLevel();
			return;
		} else
			report.report("MME Found: " + currentMME.getS1IpAddress() + " PLMN1: " + currentMME.getPLMNs().get(0)
					+ " PLMN2: " + currentMME.getPLMNs().get(1));
		if (otherMME == null) {
			report.report("No MME with 1 PLMN was detected", Reporter.FAIL);
			GeneralUtils.stopLevel();
			return;
		} else
			report.report("MME Found: " + otherMME.getS1IpAddress() + " PLMN3: " + otherMME.getPLMNs().get(0));
		GeneralUtils.stopLevel();

		stopAllUes();

		GeneralUtils.startLevel("Start traffic");
		try {
			traffic = Traffic.getInstance(SetupUtils.getInstance().getAllUEs());
			traffic.startTraffic();
		} catch (Exception e) {
			report.report("Couldn't start traffic", Reporter.WARNING);
			e.printStackTrace();
		}
		GeneralUtils.stopLevel();

		GeneralUtils.startLevel("Setting EnodeB for test");
		settingEnodeB(currentMME, dut1);
		settingEnodeB(otherMME, dut1);
		dut1.reboot();
		dut1.waitForAllRunning(EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
		peripheralsConfig.changeEnbState(dut2, EnbStates.OUT_OF_SERVICE);
		isSingleDut = true;
		GeneralUtils.stopLevel();

		GeneralUtils.startLevel("resseting counters");
		if (!isCountersZero(dut1)) {
			resetPassCriteriaCounters(dut1);
		}
		GeneralUtils.stopLevel();

		GeneralUtils.startLevel("Enabeling relevant UEs");
		boolean atleast1UE = false;
		boolean atleast1UE2 = false;
		ArrayList<UE> UEsForDUT = SetupUtils.getInstance().getallUEsPerEnodeb(dut1);
		atleast1UE = enableSpecificUE(UEsForDUT, currentMME.getPLMNs().get(1));
		atleast1UE2 = enableSpecificUE(UEsForDUT, otherMME.getPLMNs().get(0));
		if (!atleast1UE && !atleast1UE2) {
			report.report("No Ues were started in the test", Reporter.FAIL);
			GeneralUtils.stopLevel();
			return;
		}
		GeneralUtils.unSafeSleep(120000);
		GeneralUtils.stopLevel();

		GeneralUtils.startLevel("Results");
		ArrayList<EnodeB> allEnodeBs = new ArrayList<>();
		allEnodeBs.add(dut1);

		checkUeConnectedWithRightPLMN();

		GeneralUtils.startLevel("Check that MIBs are " + expectedUEList.size());
		enodeBConfig.waitGranularityPeriodTime(dut1);
		checkingMIBs(dut1);
		GeneralUtils.stopLevel();
		GeneralUtils.stopLevel();
	}

	@Test
	@TestProperties(name = "UEEntry_DifferentPLMN_SameMME", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void UEEntry_DifferentPLMN_SameMME() {
		firstMIB = "RrcConnEstabAttSum";
		secondMIB = "RrcConnEstabSuccSum";
		passCriteria.add(firstMIB);
		passCriteria.add(secondMIB);

		GeneralUtils.startLevel("setting enodeB " + dut2.getName() + " to out of service");
			peripheralsConfig.changeEnbState(dut2, EnbStates.OUT_OF_SERVICE);
		GeneralUtils.stopLevel();

		GeneralUtils.startLevel("Finding relevant MME for test ");
		MME currentMME = getMMEbyPLMNnumber(SetupUtils.getInstance().getallUEsPerEnodeb(dut1));
		if (currentMME == null) {
			report.report("No MME with 2 PLMNS", Reporter.FAIL);
			GeneralUtils.stopLevel();
			return;
		} else
			report.report("MME Found: " + currentMME.getS1IpAddress() + " PLMN1: " + currentMME.getPLMNs().get(0)
					+ " PLMN2: " + currentMME.getPLMNs().get(1));
		GeneralUtils.stopLevel();

		stopAllUes();

		GeneralUtils.startLevel("Start traffic");
		try {
			traffic = Traffic.getInstance(SetupUtils.getInstance().getAllUEs());
			traffic.startTraffic();
		} catch (Exception e) {
			report.report("Couldn't start traffic", Reporter.WARNING);
			e.printStackTrace();
		}
		GeneralUtils.stopLevel();

		GeneralUtils.startLevel("Setting EnodeB for test");
		settingEnodeB(currentMME, dut1);
		dut1.reboot();
		dut1.waitForAllRunning(EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
		isSingleDut = true;
		GeneralUtils.stopLevel();

		GeneralUtils.startLevel("Reseting counters");
		if (!isCountersZero(dut1)) {
			resetPassCriteriaCounters(dut1);
		}
		GeneralUtils.stopLevel();

		GeneralUtils.startLevel("Enabeling relevant UEs");
		boolean atleast1UE = false;
		boolean atleast1UE2 = false;
		ArrayList<UE> UEsForDUT = SetupUtils.getInstance().getallUEsPerEnodeb(dut1);
		atleast1UE = enableSpecificUE(UEsForDUT, currentMME.getPLMNs().get(0));
		atleast1UE2 = enableSpecificUE(UEsForDUT, currentMME.getPLMNs().get(1));
		if (!atleast1UE && !atleast1UE2) {
			report.report("No Ues were started in the test", Reporter.FAIL);
			GeneralUtils.stopLevel();
			return;
		}

		GeneralUtils.unSafeSleep(120000);
		GeneralUtils.stopLevel();

		GeneralUtils.startLevel("Results");
			checkUeConnectedWithRightPLMN(dut1);

		GeneralUtils.startLevel("Check that MIBs are " + expectedUEList.size());
		enodeBConfig.waitGranularityPeriodTime(dut1);
		checkingMIBs(dut1);
		GeneralUtils.stopLevel();
		GeneralUtils.stopLevel();
	}

	@Test
	@TestProperties(name = "UEEntry_DifferentPLMN_TwoMME", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void UEEntry_DifferentPLMN_TwoMME() {
		firstMIB = "RrcConnEstabAttSum";
		secondMIB = "RrcConnEstabSuccSum";
		passCriteria.add(firstMIB);
		passCriteria.add(secondMIB);

		GeneralUtils.startLevel("setting enodeB " + dut2.getName() + " to out of service");
		peripheralsConfig.changeEnbState(dut2, EnbStates.OUT_OF_SERVICE);
		GeneralUtils.stopLevel();

		GeneralUtils.startLevel("Finding relevant MME for test ");
		MME currentMME = getMMEbyPLMNnumber(SetupUtils.getInstance().getallUEsPerEnodeb(dut1));
		if (currentMME == null) {
			report.report("No MME with 2 PLMNS", Reporter.FAIL);
			GeneralUtils.stopLevel();
			return;
		} else
			report.report("MME Found: " + currentMME.getS1IpAddress() + " PLMN1: " + currentMME.getPLMNs().get(0)
					+ " PLMN2: " + currentMME.getPLMNs().get(1));

		MME otherMME = getOtherMME(currentMME);
		if (otherMME == null) {
			report.report("No MME with 1 PLMN was detected", Reporter.FAIL);
			GeneralUtils.stopLevel();
			return;
		} else
			report.report("MME Found: " + otherMME.getS1IpAddress() + " PLMN3: " + otherMME.getPLMNs().get(0));
		GeneralUtils.stopLevel();

		stopAllUes();

		GeneralUtils.startLevel("Start traffic");
		try {
			traffic = Traffic.getInstance(SetupUtils.getInstance().getAllUEs());
			traffic.startTraffic();
		} catch (Exception e) {
			report.report("Couldn't start traffic", Reporter.WARNING);
			e.printStackTrace();
		}
		GeneralUtils.stopLevel();

		GeneralUtils.startLevel("Setting EnodeB for test");
		ArrayList<MME> mmes = new ArrayList<MME>();
		ArrayList<Plmn> plmns = new ArrayList<>();
		mmes.add(currentMME);
		mmes.add(otherMME);
		plmns.add(currentMME.getPLMNs().get(0));
		plmns.add(otherMME.getPLMNs().get(0));
		settingEnodeB(plmns, mmes, dut1);
		dut1.reboot();
		dut1.waitForAllRunning(EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
		isSingleDut = true;
		GeneralUtils.stopLevel();

		GeneralUtils.startLevel("Reseting counters");
		if (!isCountersZero(dut1)) {
			resetPassCriteriaCounters(dut1);
		}
		GeneralUtils.stopLevel();

		GeneralUtils.startLevel("Enabeling relevant UEs");
		boolean atleast1UE = false;
		boolean atleast1UE2 = false;
		ArrayList<UE> UEsForDUT = SetupUtils.getInstance().getallUEsPerEnodeb(dut1);
		atleast1UE = enableSpecificUE(UEsForDUT, currentMME.getPLMNs().get(0));
		atleast1UE2 = enableSpecificUE(UEsForDUT, otherMME.getPLMNs().get(0));
		if (!atleast1UE && !atleast1UE2) {
			report.report("No Ues were started in the test", Reporter.FAIL);
			GeneralUtils.stopLevel();
			return;
		}

		GeneralUtils.stopLevel();

		GeneralUtils.startLevel("Results");
		GeneralUtils.startLevel("Checking that the UE's are connected and registered with the right PLMN");
		boolean connected = false;
		for (UE ue : expectedUEList) {
			connected = epc.checkUEConnectedToNode(ue, dut1);
			if (!connected)
				report.report("UE " + ue.getLanIpAddress() + " is not connected to EnodeB " + dut1.getName(), Reporter.FAIL);
			else {
				report.report("UE " + ue.getLanIpAddress() + " is connected to enodeB " + dut1.getName());
				ArrayList<Plmn> dutPLMNS = new ArrayList<NetworkParameters.Plmn>();
				dutPLMNS.add(ue.getPlmnByImsi());
				checkIfRightPlmn(dutPLMNS, ue);
			}
		}
		GeneralUtils.stopLevel();
		GeneralUtils.startLevel("Checking that the UE's are connected to the right MME");
		MME tempMME = new MME();
		for (UE ue : expectedUEList) {
			ArrayList<MME> mme = new ArrayList<>();
			for (Plmn tempPlmn : currentMME.getPLMNs()) {
				if (ue.getPlmnByImsi().equal(tempPlmn)) {
					mme.add(currentMME);
				}
			}
			for (Plmn tempPlmn : otherMME.getPLMNs()) {
				if (ue.getPlmnByImsi().equal(tempPlmn)) {
					mme.add(otherMME);
				}
			}
			tempMME = epc.getCurrentMME(ue, mme);
			if (tempMME == null)
				report.report("UE " + ue.getName() + " is not connected to the right MME", Reporter.FAIL);
		}
		GeneralUtils.stopLevel();

		GeneralUtils.startLevel("Check that MIBs are " + expectedUEList.size());
		enodeBConfig.waitGranularityPeriodTime(dut1);
		checkingMIBs(dut1);
		GeneralUtils.stopLevel();
		GeneralUtils.stopLevel();
	}

	@Override
	public void end() {
		GeneralUtils.startLevel("Setting the enodeB's back to default settings");
		if (EPC.getInstance().MME != null) {
			if (!isSingleDut)
				postTest(dut1, dut2);
			else
				postTest(dut1);
			for (UE ue : SetupUtils.getInstance().getAllUEs()) {
				ue.start();
			}
		}
		GeneralUtils.stopLevel();
		super.end();
		stopCommandsAndAttachFiles();
	}

	@Test
	@TestProperties(name = "SharedMME_UEattachingFunctionalTest_TwoUEsDifferentPLMN", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void SharedMME_UEattachingFunctionalTest_TwoUEsDifferentPLMN() {
		firstMIB = "RrcConnEstabAttSum";
		secondMIB = "RrcConnEstabSuccSum";
		passCriteria.add(firstMIB);
		passCriteria.add(secondMIB);

		GeneralUtils.startLevel("Finding relevant MME for test ");
		MME currentMME = getMMEbyPLMNnumber(2);
		if (currentMME == null) {
			report.report("No MME with 2 PLMNS", Reporter.FAIL);
			GeneralUtils.stopLevel();
			return;
		} else
			report.report("MME Found: " + currentMME.getS1IpAddress() + " PLMN1: " + currentMME.getPLMNs().get(0)
					+ " PLMN2: " + currentMME.getPLMNs().get(1));
		GeneralUtils.stopLevel();

		stopAllUes();

		GeneralUtils.startLevel("starting traffic");
		try {
			traffic = Traffic.getInstance(SetupUtils.getInstance().getAllUEs());
			traffic.startTraffic();
		} catch (Exception e) {
			report.report("Couldn't start traffic", Reporter.WARNING);
			e.printStackTrace();
		}
		GeneralUtils.stopLevel();

		GeneralUtils.startLevel("Setting EnodeB for test");
		settingEnodeB(currentMME, 0, dut1);
		settingEnodeB(currentMME, 1, dut2);
		dut1.reboot();
		dut2.reboot();
		dut1.waitForAllRunning(EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
		dut2.waitForAllRunning(EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
		GeneralUtils.stopLevel();

		if (!isCountersZero(dut1) || !isCountersZero(dut2)) {
			resetPassCriteriaCounters(dut1, dut2);
		}
		GeneralUtils.startLevel("Enabeling relevant UEs");
		boolean isUesconnected = false;
		ArrayList<UE> UEsForDUT = SetupUtils.getInstance().getallUEsPerEnodeb(dut1);
		if (enableSpecificUE(UEsForDUT, currentMME.getPLMNs().get(0)))
			isUesconnected = true;
		if (enableSpecificUE(UEsForDUT, currentMME.getPLMNs().get(1)))
			isUesconnected = true;
		UEsForDUT = SetupUtils.getInstance().getallUEsPerEnodeb(dut2);
		if (enableSpecificUE(UEsForDUT, currentMME.getPLMNs().get(0)))
			isUesconnected = true;
		if (enableSpecificUE(UEsForDUT, currentMME.getPLMNs().get(1)))
			isUesconnected = true;
		if (!isUesconnected) {
			report.report("No Ues were started in the test", Reporter.FAIL);
			GeneralUtils.stopLevel();
			return;
		}
		GeneralUtils.unSafeSleep(120000);
		GeneralUtils.stopLevel();

		GeneralUtils.startLevel("Results");
		ArrayList<EnodeB> allEnodeBs = new ArrayList<>();
		allEnodeBs.add(dut1);
		allEnodeBs.add(dut2);

		checkUeConnectedWithRightPLMN();

		GeneralUtils.startLevel("Check that MIBs are " + expectedUEList.size());
		enodeBConfig.waitGranularityPeriodTime(dut1);
		checkingMIBs(dut1, dut2);
		GeneralUtils.stopLevel();
		GeneralUtils.stopLevel();
	}

	@Test
	@TestProperties(name = "SharedMME_DifferentENBservingPLMN_X2Handover", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void SharedMME_DifferentENBservingPLMN_X2Handover() {
		firstMIB = "RrcConnEstabAttSum";
		secondMIB = "RrcConnEstabSuccSum";
		passCriteria.add(firstMIB);
		passCriteria.add(secondMIB);

		GeneralUtils.startLevel("Finding relevant MME for test ");
		MME currentMME = getMMEbyPLMNnumber(2);
		if (currentMME == null) {
			report.report("No MME with 2 PLMNS", Reporter.FAIL);
			GeneralUtils.stopLevel();
			return;
		} else
			report.report("MME Found: " + currentMME.getS1IpAddress() + " PLMN1: " + currentMME.getPLMNs().get(0)
					+ " PLMN2: " + currentMME.getPLMNs().get(1));
		GeneralUtils.stopLevel();

		HO = new testsNG.ProtocolsAndServices.HandOver.P0();
		HO.setDUT1(dut1.getName());
		HO.setDUT2(dut2.getName());
		HO.objectInit();

		stopAllUes();

		GeneralUtils.startLevel("starting traffic");
		try {
			traffic = Traffic.getInstance(SetupUtils.getInstance().getAllUEs());
			traffic.startTraffic();
		} catch (Exception e) {
			report.report("Can't start traffic", Reporter.WARNING);
			e.printStackTrace();
		}
		GeneralUtils.stopLevel();

		GeneralUtils.startLevel("Setting EnodeB for test");
		settingEnodeB(currentMME, dut1);
		settingEnodeB(currentMME, 1, dut2);
		dut1.reboot();
		dut2.reboot();
		dut1.waitForAllRunning(EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
		dut2.waitForAllRunning(EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
		GeneralUtils.stopLevel();

		GeneralUtils.startLevel("Reseting counters");
		if (!isCountersZero(dut1) || !isCountersZero(dut2)) {
			resetPassCriteriaCounters(dut1, dut2);
		}
		GeneralUtils.stopLevel();

		GeneralUtils.startLevel("Enabeling relevant UEs");
		ArrayList<UE> UEsForDUT = SetupUtils.getInstance().getallUEsPerEnodeb(dut1);
		boolean isUesconnected = false;
		if (enableSpecificUE(UEsForDUT, currentMME.getPLMNs().get(0)))
			isUesconnected = true;
		if (enableSpecificUE(UEsForDUT, currentMME.getPLMNs().get(1)))
			isUesconnected = true;
		UEsForDUT = SetupUtils.getInstance().getallUEsPerEnodeb(dut2);
		if (enableSpecificUE(UEsForDUT, currentMME.getPLMNs().get(0)))
			isUesconnected = true;
		if (enableSpecificUE(UEsForDUT, currentMME.getPLMNs().get(1)))
			isUesconnected = true;
		if (!isUesconnected) {
			report.report("No Ues were started in the test", Reporter.FAIL);
			GeneralUtils.stopLevel();
			return;
		}
		GeneralUtils.unSafeSleep(120000);
		GeneralUtils.stopLevel();

		GeneralUtils.startLevel("preforming HO");
		setCountersValue();
		if (!HO.BasicHO_X2IntraFrequency_negativeTest_RanShering())
			report.report("Hand Over failed", Reporter.FAIL);
		GeneralUtils.stopLevel();

		GeneralUtils.startLevel("Results");
		ArrayList<EnodeB> allEnodeBs = new ArrayList<>();
		allEnodeBs.add(dut1);
		allEnodeBs.add(dut2);

		checkUeConnectedWithRightPLMN();

		GeneralUtils.startLevel("Check that MIBs are " + expectedUEList.size());
		enodeBConfig.waitGranularityPeriodTime(dut1);
		checkingMIBs(dut1, dut2);
		GeneralUtils.stopLevel();
		GeneralUtils.stopLevel();
		GeneralUtils.startLevel("Deleting neighbors");
		HO.deleteAllNeighbors(dut1);
		HO.deleteAllNeighbors(dut2);
		GeneralUtils.stopLevel();
	}

	@Test
	@TestProperties(name = "SharedMME_DifferentENBservingPLMN_wrongPLMN_X2Handover", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void SharedMME_DifferentENBservingPLMN_wrongPLMN_X2Handover() {
		firstMIB = "RrcConnEstabAttSum";
		secondMIB = "RrcConnEstabSuccSum";
		passCriteria.add(firstMIB);
		passCriteria.add(secondMIB);

		GeneralUtils.startLevel("Finding relevant MME for test ");
		MME currentMME = getMMEbyPLMNnumber(2);
		MME otherMME = getOtherMME(currentMME);
		if (currentMME == null) {
			report.report("No MME with 2 PLMNS", Reporter.FAIL);
			GeneralUtils.stopLevel();
			return;
		} else
			report.report("MME Found: " + currentMME.getS1IpAddress() + " PLMN1: " + currentMME.getPLMNs().get(0)
					+ " PLMN2: " + currentMME.getPLMNs().get(1));
		if (otherMME == null) {
			report.report("No MME with 1 PLMNS", Reporter.FAIL);
			GeneralUtils.stopLevel();
			return;
		} else
			report.report("MME Found: " + otherMME.getS1IpAddress() + " PLMN3: " + otherMME.getPLMNs().get(0));
		GeneralUtils.stopLevel();

		HO = new testsNG.ProtocolsAndServices.HandOver.P0();
		HO.setDUT1(dut1.getName());
		HO.setDUT2(dut2.getName());
		HO.isNegative = true;
		HO.objectInit();

		stopAllUes();

		GeneralUtils.startLevel("starting traffic");
		try {
			traffic = Traffic.getInstance(SetupUtils.getInstance().getAllUEs());
			traffic.startTraffic();
		} catch (Exception e) {
			report.report("Couldn't start traffic", Reporter.WARNING);
			e.printStackTrace();
		}
		GeneralUtils.stopLevel();

		GeneralUtils.startLevel("Setting EnodeB for test");
		settingEnodeB(currentMME, dut1);
		settingEnodeB(otherMME, 0, dut2);
		dut1.reboot();
		dut2.reboot();
		dut1.waitForAllRunning(EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
		GeneralUtils.stopLevel();

		GeneralUtils.startLevel("Reset counters");
		if (!isCountersZero(dut1) || !isCountersZero(dut2)) {
			resetPassCriteriaCounters(dut1, dut2);
		}
		GeneralUtils.stopLevel();

		GeneralUtils.startLevel("Enabeling relevant UEs");
		ArrayList<UE> UEsForDUT = SetupUtils.getInstance().getallUEsPerEnodeb(dut1);
		boolean isUesconnected = false;
		if (enableSpecificUE(UEsForDUT, currentMME.getPLMNs().get(0)))
			isUesconnected = true;
		if (enableSpecificUE(UEsForDUT, currentMME.getPLMNs().get(1)))
			isUesconnected = true;
		UEsForDUT = SetupUtils.getInstance().getallUEsPerEnodeb(dut2);
		if (enableSpecificUE(UEsForDUT, currentMME.getPLMNs().get(0)))
			isUesconnected = true;
		if (enableSpecificUE(UEsForDUT, currentMME.getPLMNs().get(1)))
			isUesconnected = true;
		if (!isUesconnected) {
			report.report("No Ues were started in the test", Reporter.FAIL);
			GeneralUtils.stopLevel();
			return;
		}
		GeneralUtils.unSafeSleep(120000);
		GeneralUtils.stopLevel();

		GeneralUtils.startLevel("preforming HO");
		setCountersValue();
		if (!HO.BasicHO_X2IntraFrequency_negativeTest_RanShering()) {
			report.report("HO test failed - HO was preformed also it wasn't suppose to", Reporter.FAIL);
		} else
			report.report("No Hand Over was preformed as expected!");
		GeneralUtils.stopLevel();

		GeneralUtils.startLevel("Results");
		ArrayList<EnodeB> allEnodeBs = new ArrayList<>();
		allEnodeBs.add(dut1);
		allEnodeBs.add(dut2);

		checkUeConnectedWithRightPLMN();

		GeneralUtils.startLevel("Check that MIBs are " + expectedUEList.size());
		enodeBConfig.waitGranularityPeriodTime(dut1);
		checkingMIBs(dut1, dut2);
		GeneralUtils.stopLevel();
		GeneralUtils.stopLevel();
		GeneralUtils.startLevel("Deleting neighbors");
		HO.deleteAllNeighbors(dut1);
		HO.deleteAllNeighbors(dut2);
		GeneralUtils.stopLevel();
	}

	@Test
	@TestProperties(name = "UENetworkEntry_DifferentUEPLMN_SameMME", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void UENetworkEntry_DifferentUEPLMN_SameMME() {
		firstMIB = "RrcConnEstabAttSum";
		secondMIB = "RrcConnEstabSuccSum";
		passCriteria.add(firstMIB);
		passCriteria.add(secondMIB);

		GeneralUtils.startLevel("Finding relevant MME for test ");
		MME currentMME = getMMEbyPLMNnumber(2);
		if (currentMME == null) {
			report.report("No MME with 2 PLMNS", Reporter.FAIL);
			GeneralUtils.stopLevel();
			return;
		} else
			report.report("MME Found: " + currentMME.getS1IpAddress() + " PLMN1: " + currentMME.getPLMNs().get(0)
					+ " PLMN2: " + currentMME.getPLMNs().get(1));
		GeneralUtils.stopLevel();

		stopAllUes();

		GeneralUtils.startLevel("starting traffic");
		try {
			traffic = Traffic.getInstance(SetupUtils.getInstance().getAllUEs());
			traffic.startTraffic();
		} catch (Exception e) {
			report.report("Couldn't start traffic", Reporter.WARNING);
			e.printStackTrace();
		}
		GeneralUtils.stopLevel();

		GeneralUtils.startLevel("Setting EnodeB for test");
		if (settingEnodeB(currentMME, 0, dut1)) {
			dut1.reboot();
			dut1.waitForAllRunning(EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
		}
		GeneralUtils.stopLevel();

		if (!isCountersZero(dut1) || !isCountersZero(dut2)) {
			resetPassCriteriaCounters(dut1, dut2);
		}
		GeneralUtils.startLevel("Enabeling relevant UEs");
		boolean isUesconnected = false;
		ArrayList<UE> UEsForDUT = SetupUtils.getInstance().getallUEsPerEnodeb(dut1);
		if (enableSpecificUE(UEsForDUT, currentMME.getPLMNs().get(0)))
			isUesconnected = true;
		if (enableSpecificUE(UEsForDUT, currentMME.getPLMNs().get(1)))
			isUesconnected = true;
		UEsForDUT = SetupUtils.getInstance().getallUEsPerEnodeb(dut2);
		if (enableSpecificUE(UEsForDUT, currentMME.getPLMNs().get(0)))
			isUesconnected = true;
		if (enableSpecificUE(UEsForDUT, currentMME.getPLMNs().get(1)))
			isUesconnected = true;
		if (!isUesconnected) {
			report.report("No Ues were started in the test", Reporter.FAIL);
			GeneralUtils.stopLevel();
			return;
		}
		GeneralUtils.unSafeSleep(120000);
		GeneralUtils.stopLevel();

		GeneralUtils.startLevel("Results");
		ArrayList<EnodeB> allEnodeBs = new ArrayList<>();
		allEnodeBs.add(dut1);
		allEnodeBs.add(dut2);

		checkUeConnectedWithRightPLMN();

		GeneralUtils.startLevel("Check that MIBs are " + expectedUEList.size());
		enodeBConfig.waitGranularityPeriodTime(dut1);
		checkingMIBs(dut1, dut2);
		GeneralUtils.stopLevel();
		GeneralUtils.stopLevel();
	}

	private void stopAllUes() {
		GeneralUtils.startLevel("Stopping all ues");
		ArrayList<UE> allUES = null;
		try {
			allUES = SetupUtils.getInstance().getAllUEs();

			for (UE ue : allUES) {
				if (ue.stop()) {
					report.report("UE: " + ue.getName() + " [" + ue.getImsi() + "] stopped as expected");
					GeneralUtils.unSafeSleep(3000);
				} else
					report.report("UE: " + ue.getName() + " [" + ue.getImsi() + "] didn't stop", Reporter.WARNING);
			}
		} catch (Exception e) {
			report.report("couldn't get dynamic UEs list", Reporter.WARNING);
			e.printStackTrace();
		}
		GeneralUtils.stopLevel();
	}

	private MME getOtherMME(MME currentMME) {
		for (MME mme : EPC.getInstance().MME)
			if (!mme.getS1IpAddress().equals(currentMME.getS1IpAddress()))
				return mme;
		return null;
	}

	private boolean settingEnodeB(MME currentMME, int PLMNindex, EnodeB enb) {
		ArrayList<Plmn> plmns = new ArrayList<>();
		plmns.add(currentMME.getPLMNs().get(PLMNindex));

		GeneralUtils
				.startLevel("Setting EnodeB " + enb.getNetspanName() + " with " + currentMME.getPLMNs().get(PLMNindex));
		boolean setPLMN = false;
		try {
			setPLMN = setS1AndPLMN(enb, currentMME, plmns);
			if (!setPLMN)
				report.report("couldn't set the enodeB " + enb.getNetspanName() + " with the plmn "
						+ currentMME.getPLMNs().get(PLMNindex), Reporter.WARNING);
		} catch (Exception e) {
			report.report("Couldn't set S1 IP or PLMN in EnodeB");
			e.printStackTrace();
		}
		GeneralUtils.stopLevel();
		return setPLMN;
	}

	private boolean settingEnodeB(MME currentMME, EnodeB enb) {
		boolean res = true;
		try {
			ArrayList<Plmn> plmns = new ArrayList<>();
			plmns.addAll(currentMME.getPLMNs());
			GeneralUtils.startLevel("Setting EnodeB " + enb.getNetspanName() + " with " + currentMME.getPLMNs().get(0)
					+ " and " + currentMME.getPLMNs().get(1));
			boolean setPLMN;
			setPLMN = setS1AndPLMN(enb, currentMME, plmns);
			if (!setPLMN)
				report.report(
						"couldn't set the enodeB " + enb.getNetspanName() + " with the plmns: "
								+ currentMME.getPLMNs().get(0) + " , " + currentMME.getPLMNs().get(1),
						Reporter.WARNING);
			res = false;
		} catch (Exception e) {
			report.report("Couldn't set plmn in enodeB");
			e.printStackTrace();
			res = false;
		}
		GeneralUtils.stopLevel();
		return res;
	}

	private boolean settingEnodeB(ArrayList<Plmn> plmns, List<MME> mmes, EnodeB enb) {
		boolean res = true;
		String mmesToReport = "";
		String plmnForReport = "";
		for (MME mme : mmes) {
			mmesToReport = mmesToReport + "," + mme.getS1IpAddress();
		}
		mmesToReport = mmesToReport.substring(1);
		for (Plmn plmn : plmns) {
			plmnForReport = plmnForReport + "," + plmn.toString();
		}
		try {
			plmnForReport = plmnForReport.substring(1);
			GeneralUtils.startLevel(
					"Setting EnodeB " + enb.getNetspanName() + " with " + mmesToReport + " . PLMNS: " + plmnForReport);
			boolean setPLMN;
			if (mmes.size() < 2) {
				setPLMN = setS1AndPLMN(enb, mmes.get(0), plmns);
				if (!setPLMN) {
					report.report(
							"couldn't set the enodeB " + enb.getNetspanName() + " with the plmns: " + plmnForReport,
							Reporter.WARNING);
					res = false;
				}
			} else {
				setPLMN = setS1AndPLMN(enb, mmes.get(0), mmes.get(1), plmns);
				if (!setPLMN) {
					report.report(
							"couldn't set the enodeB " + enb.getNetspanName() + " with the plmns: " + plmnForReport,
							Reporter.WARNING);
					res = false;
				}
			}
		} catch (Exception e) {
			report.report("Couldn't set plmn in enodeB");
			e.printStackTrace();
			res = false;
		}
		GeneralUtils.stopLevel();
		return res;
	}

	private boolean enableSpecificUE(ArrayList<UE> allUES, Plmn plmn) {
		boolean wasUEstarted = false;
		for (UE ue : allUES) {
			if (ue.getPlmnByImsi().equal(plmn)) {
				if (!expectedUEList.contains(ue)) {
					if (ue.start()) {
						wasUEstarted = true;
						report.report("UE: " + ue.getName() + " (imsi: " + ue.getImsi() + ") " + "was enabled");
						GeneralUtils.unSafeSleep(10000);
					} else
						report.report("Couldn't enable UE: " + ue.getName() + " (imsi: " + ue.getImsi() + ") ",
								Reporter.WARNING);
					expectedUEList.add(ue);
				}

			}
		}
		return wasUEstarted;

	}

	private boolean setS1AndPLMN(EnodeB dut, MME currentMME, ArrayList<Plmn> plmnList) {
		NetworkParameters np = new NetworkParameters();
		np.setMMEIPS(currentMME.getS1IpAddress());
		np.setPLMNList(plmnList);
		return enodeBConfig.cloneAndSetNetworkProfileViaNetSpan(dut, dut.getDefaultNetspanProfiles().getNetwork(), np);

	}

	private boolean setS1AndPLMN(EnodeB dut, MME firstMME, MME secondMME, ArrayList<Plmn> plmnList) {
		NetworkParameters np = new NetworkParameters();
		np.setMMEIPS(firstMME.getS1IpAddress(), secondMME.getS1IpAddress());
		np.setPLMNList(plmnList);
		return enodeBConfig.cloneAndSetNetworkProfileViaNetSpan(dut, dut.getDefaultNetspanProfiles().getNetwork(), np);

	}

	private MME getMMEbyPLMNnumber(int i) {
		if (EPC.getInstance().MME != null) {
			for (MME mme : EPC.getInstance().MME)
				if (mme.getPLMNs().size() >= i)
					return mme;
		}
		return null;
	}

	private MME getMMEbyPLMNnumber(ArrayList<UE> allUES) {
		usedPlmnsInTest = new ArrayList<Plmn>();
		if (EPC.getInstance().MME != null) {
			for (int i = 0; i < allUES.size(); i++) {
				for (int j = 0; j < allUES.size(); j++) {
					if (allUES.get(i).getPlmnByImsi().equal(allUES.get(j).getPlmnByImsi()))
						continue;
					Plmn plmn1 = allUES.get(i).getPlmnByImsi();
					Plmn plmn2 = allUES.get(j).getPlmnByImsi();
					for (MME mme : EPC.getInstance().MME) {
						if (mme.getS1IpAddress() != null) {
							if (mme.containsPlmn(plmn1) && mme.containsPlmn(plmn2)) {
								usedPlmnsInTest.add(plmn1);
								usedPlmnsInTest.add(plmn2);
								return mme;
							}
						}
					}
				}
			}
		}
		return null;
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

	private void postTest(EnodeB enb) {
		SetProfileBack(enb);
		if (!enb.reboot(false))
			report.report("Couldn't reboot The enodeB " + enb.getNetspanName(), Reporter.WARNING);
		enb.waitForAllRunning(EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
		peripheralsConfig.changeEnbState(dut2, EnbStates.IN_SERVICE);
		enodeBConfig.deleteClonedProfiles();
	}

	private void postTest(EnodeB enb1, EnodeB enb2) {
		SetProfileBack(enb1);
		SetProfileBack(enb2);
		if (!enb1.reboot(false))
			report.report("Couldn't reboot The enodeB " + enb1.getNetspanName(), Reporter.WARNING);
		if (!enb2.reboot(false))
			report.report("Couldn't reboot The enodeB " + enb2.getNetspanName(), Reporter.WARNING);
		enb1.waitForAllRunning(EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
		enb2.waitForAllRunning(EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
		enodeBConfig.deleteClonedProfiles();
	}

	private void SetProfileBack(EnodeB enb) {
		if (!enodeBConfig.setProfile(enb, EnbProfiles.Network_Profile, enb.getDefaultNetspanProfiles().getNetwork())) {
			report.report("Couldn't set back the network profile to " + enb.getDefaultNetspanProfiles().getNetwork(),
					Reporter.WARNING);
		} else
			report.report("Network profile " + enb.getDefaultNetspanProfiles().getNetwork() + " was set on enodeB "
					+ enb.getNetspanName());
	}

	private void checkIfRightPlmn(ArrayList<Plmn> ExpectedPLMNs, UE ue) {
		GeneralUtils.startLevel("UE : " + ue.getLanIpAddress() + "[" + ue.getImsi() + "]");
		DMtool dm = new DMtool();
		try {
			dm.setUeIP(ue.getLanIpAddress());
			dm.init();
		} catch (Exception e) {
			e.printStackTrace();
		}

		HashMap<String, PlmnData> dmPLMNs = dm.getPLMNs();
		if (dmPLMNs == null) {
			report.report("Can't return UE information", Reporter.FAIL);
			dm.close();
			GeneralUtils.stopLevel();
			return;
		}
		PlmnData hPlmn = dmPLMNs.get("home");
		PlmnData selPlmn = dmPLMNs.get("selected");
		if (hPlmn != null && selPlmn != null) {
			Plmn homePlmn = convertToPlmn(hPlmn);
			Plmn selectedPlmn = convertToPlmn(selPlmn);
			Plmn uePlmn = ue.getPlmnByImsi();

			if (homePlmn.equal(uePlmn)) {
				report.report("The UE: " + ue.getLanIpAddress() + " [ " + ue.getImsi() + "] home Plmn is "
						+ homePlmn.show() + " as expected");
			} else
				report.report("The UE: " + ue.getLanIpAddress() + " [ " + ue.getImsi() + "] home Plmn is "
						+ homePlmn.show() + " and not " + uePlmn.show(), Reporter.FAIL);
			if (ExpectedPLMNs.size() > 1) {
				boolean res = false;
				for (Plmn tempPlmn : ExpectedPLMNs) {
					if (tempPlmn.equal(selectedPlmn)) {
						report.report("The UE: " + ue.getLanIpAddress() + " [ " + ue.getImsi() + "] selected Plmn is "
								+ selectedPlmn.show() + "  as expected");
						res = true;
						break;
					}
				}
				if (!res)
					report.report("The UE: " + ue.getLanIpAddress() + " [ " + ue.getImsi() + "] selected Plmn is "
							+ selectedPlmn.show() + " and not " + ExpectedPLMNs.get(0) + " OR " + ExpectedPLMNs.get(1),
							Reporter.FAIL);
			} else {
				if (ExpectedPLMNs.get(0).equal(selectedPlmn))
					report.report("The UE: " + ue.getLanIpAddress() + " [ " + ue.getImsi() + "] selected Plmn is "
							+ selectedPlmn.show() + "  as expected");
				else
					report.report("The UE: " + ue.getLanIpAddress() + " [ " + ue.getImsi() + "] selected Plmn is "
							+ selectedPlmn.show() + " and not " + ExpectedPLMNs.get(0), Reporter.FAIL);
			}
		} else
			report.report("Can't return UE information", Reporter.WARNING);
		dm.close();
		GeneralUtils.stopLevel();
	}

	private void checkUeConnectedWithRightPLMN() {
		GeneralUtils.startLevel("Checking that the UE's are connected and registered with the right PLMN");
		for (UE ue : expectedUEList) {
			EnodeB currentEnb = epc.getCurrentEnodB(ue, enbInTest);
			if (currentEnb != null) {
				ArrayList<Plmn> dutPLMNS = currentEnb.getConfiguredPLMNList();
				checkIfRightPlmn(dutPLMNS, ue);
			} else
				report.report("UE " + ue.getName() + " is not connected to any EnodeB ", Reporter.FAIL);
		}
		GeneralUtils.stopLevel();
	}

	private void checkUeConnectedWithRightPLMN(EnodeB enb) {
		GeneralUtils.startLevel("Checking that the UE's are connected and registered with the right PLMN");
		for (UE ue : expectedUEList) {
			boolean connected = epc.checkUEConnectedToNode(ue, enb);
			if (connected) {
				report.report("UE " + ue.getLanIpAddress() + " is connected to enodeB " + enb.getName());
				ArrayList<Plmn> dutPLMNS = enb.getConfiguredPLMNList();
				checkIfRightPlmn(dutPLMNS, ue);
			} else
				report.report("UE " + ue.getLanIpAddress() + " is not connected to EnodeB " + enb.getName(), Reporter.FAIL);
		}
		GeneralUtils.stopLevel();
	}

	private void checkingMIBs(EnodeB enb) {
		for (String mib : passCriteria) {
			int result = enb.getCountersValue(mib);
			if (result == 1 * expectedUEList.size()) {
				report.report(
						"[Pass Criteria: ] EnodeB: " + enb.getName() + " counter " + mib + " value is: " + result);
			} else {
				report.report("[Pass Criteria: ] EnodeB: " + enb.getName() + " counter " + mib + " value is: " + result,
						Reporter.WARNING);
			}
		}
	}

	private void checkingMIBs(EnodeB enb1, EnodeB enb2) {
		for (String mib : passCriteria) {
			int result = enb1.getCountersValue(mib);
			result += enb2.getCountersValue(mib);
			if (result == (1 * expectedUEList.size())) {
				report.report("[Pass Criteria: ] EnodeB: " + enb1.getName() + " + " + enb2.getName() + " counter " + mib
						+ " value is: " + result);
			} else {
				report.report("[Pass Criteria: ] EnodeB: " + enb1.getName() + " + " + enb2.getName() + " counter " + mib
						+ " value is: " + result, Reporter.WARNING);
			}
		}
	}

	private boolean resetPassCriteriaCounters(EnodeB enb) {
		HashMap<String, String> counterValue = new HashMap<>();
		GeneralUtils.startLevel("changing counters value to 0");
		boolean isSucceeded = true;
		boolean arezero = true;
		enb.resetCounter("asLteStatsRrcEntry", "*", counterValue);
		enodeBConfig.waitGranularityPeriodTime(enb);
		arezero = isCountersZero(enb);
		if (!arezero) {
			report.report("**trying to reset counters again**");
			enb.resetCounter("asLteStatsRrcEntry", "*", counterValue);
			enodeBConfig.waitGranularityPeriodTime(enb);
			arezero = isCountersZero(enb);
			if (!arezero) {
				isSucceeded = false;
				report.report("Counters are not 0 as expected", Reporter.WARNING);
			}
		}
		GeneralUtils.stopLevel();
		return isSucceeded;
	}

	private boolean resetPassCriteriaCounters(EnodeB enb1, EnodeB enb2) {
		boolean isSucceeded = true;
		HashMap<String, String> counterValue = new HashMap<>();
		GeneralUtils.startLevel("changing counters value to 0");
		enb1.resetCounter("asLteStatsRrcEntry", "*", counterValue);
		enb2.resetCounter("asLteStatsRrcEntry", "*", counterValue);
		enodeBConfig.waitGranularityPeriodTime(enb1);
		if (!isCountersZero(enb1) || !isCountersZero(enb2)) {
			report.report("***Trying to reset counters again***");
			enb1.resetCounter("asLteStatsRrcEntry", "*", counterValue);
			enb2.resetCounter("asLteStatsRrcEntry", "*", counterValue);
			enodeBConfig.waitGranularityPeriodTime(enb1);
			if (!isCountersZero(enb1) || !isCountersZero(enb2)) {
				isSucceeded = false;
				report.report("Counters are not 0 as expected after 2 cheaks", Reporter.WARNING);
			}
		}
		GeneralUtils.stopLevel();
		return isSucceeded;
	}

	private boolean isCountersZero(EnodeB enb) {
		boolean arezero = true;
		for (String counter : passCriteria) {
			if (enb.getCountersValue(counter) != 0) {
				arezero = false;
				report.report("Counter " + counter + " values are not 0 in enodeB: " + enb.getNetspanName());
				break;
			} else {
				report.report("Counter " + counter + " values are 0 in enodeB: " + enb.getName());
			}
		}
		return arezero;
	}

	private Plmn convertToPlmn(PlmnData plmnData) {
		Plmn data = new Plmn();
		data.setMCC(plmnData.getMCC());
		data.setMNC(plmnData.getMNC());
		return data;
	}

	private void setCountersValue() {
		if (dut1.isInstanceOfXLP_14_0()) {
			succCounterName = "IntraFreqOutSucc";
			succCounterNameSNMP = "asLteStatsHoIntraFreqOutSucc";
			attCounterName = "IntraFreqOutAtt";
			attCounterNameSNMP = "asLteStatsHoIntraFreqOutAtt";
		} else if (dut1.isInstanceOfXLP_15_2()) {
			succCounterName = "HoX2IntraFreqInCompSuccRnlRadioRsn";
			succCounterNameSNMP = "HoX2IntraFreqInCompSuccRnlRadioRsn";
			attCounterName = "HoX2IntraFreqInAttRnlRadioRsn";
			attCounterNameSNMP = "HoX2IntraFreqInAttRnlRadioRsn";

		} else {
			succCounterName = "HoX2IntraFreqInCompSuccRnlRadioRsn";
			succCounterNameSNMP = "asLteStatsHoX2IntraFreqInCompSuccRnlRadioRsn";
			attCounterName = "HoX2IntraFreqInAttRnlRadioRsn";
			attCounterNameSNMP = "asLteStatsHoX2IntraFreqInAttRnlRadioRsn";
		}

		HO.passCriteria.put(succCounterName, succCounterNameSNMP);
		HO.passCriteria.put(attCounterName, attCounterNameSNMP);
	}

	public void startCommands() {

		ArrayList<String> commands = new ArrayList<>();
		commands.add("ue show rate");
		commands.add("rlc show amdlstats");
		commands.add("system show error");
		commands.add("system show phyerror");
		commands.add("system show memory");
		commands.add("db get nghlist");
		commands.add("enb show ngh");
		commands.add("db get cellCfg");
		commands.add("db get PTPStatus");
		commands.add("cell show ransharingparams debug=1");
		commands.add("db get s1cfg");
		commands.add("db get mmeStatus");
		commands.add("db get PlmnCfg");
		commands.add("enb show MMEsStatus");
		commands.add("!ping -c 1 172.23.52.4");
		commands.add("!ping -c 1 172.23.52.21");

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

}

package testsNG.ProtocolsAndServices.HandOver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import Attenuators.AttenuatorSet;
import EnodeB.EnodeB;
import Netspan.EnbProfiles;
import Netspan.API.Enums.ConnectedModeEventTypes;
import Netspan.API.Enums.EnabledDisabledStates;
import Netspan.API.Enums.EnbStates;
import Netspan.API.Enums.HandoverType;
import Netspan.API.Enums.HoControlStateTypes;
import Netspan.API.Enums.X2ControlStateTypes;
import Netspan.Profiles.MobilityParameters;
import UE.UE;
import Utils.GeneralUtils;
import Utils.SetupUtils;
import Utils.SysObjUtils;
import jsystem.framework.ParameterProperties;
import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;
import testsNG.TestspanTest;
import testsNG.Actions.EnodeBConfig;
import testsNG.Actions.Neighbors;
import testsNG.Actions.PeripheralsConfig;
import testsNG.Actions.Traffic;

public class QosBased extends TestspanTest {

	private EnodeBConfig enodeBConfig;
	private Neighbors neighbors;
	private Traffic traffic;
	private PeripheralsConfig peripheralsConfig;
	private AttenuatorSet attenuatorSetUnderTest;
	private EnodeB dut1;
	private EnodeB dut2;
	private boolean isHoTest = false;
	private boolean isInter;
	private boolean isX2;
	private boolean isAccess;
	private boolean isNegative = false;
	private int maxAcceptableCalls = 1;
	private int tryNum = 1;
	private UE dynUe;
	private UE staticUe;
	public HashMap<String, Integer> passCriteria = new HashMap<>();
	public HashMap<String, Integer> otherCounters = new HashMap<>();
	private EnodeBConfig enodebConfig = EnodeBConfig.getInstance();
	
	@Override
	public void init() throws Exception {
		enbInTest = new ArrayList<EnodeB>();
		enbInTest.add(dut1);
		enbInTest.add(dut2);
		super.init();
		attenuatorSetUnderTest = AttenuatorSet.getAttenuatorSet("rudat_set");
		enodeBConfig = EnodeBConfig.getInstance();
		traffic = Traffic.getInstance(SetupUtils.getInstance().getAllUEs());
		neighbors = Neighbors.getInstance();
		peripheralsConfig = PeripheralsConfig.getInstance();
	}

	@Test
	@TestProperties(name = "Qos Based HO - Access A5 Network Entry trigger, Intra X2 successful HO ", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void NetworkEntryAccessA5IntraX2() {
		isAccess = true;
		isX2 = true;
		isInter = false;
		if(!preTest())
			return;
		testConfig();
		qosBasedTest();
		postTest();
	}

	@Test
	@TestProperties(name = "Qos Based HO - Access A5 Network Entry trigger, Intra S1 successful HO ", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void NetworkEntryAccessA5IntraS1() {
		isAccess = true;
		isX2 = false;
		isInter = false;
		if(!preTest())
			return;
		testConfig();
		qosBasedTest();
		postTest();
	}

	@Test
	@TestProperties(name = "Qos Based HO - Access A5 Network Entry trigger, Inter X2 successful HO ", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void NetworkEntryAccessA5InterX2() {
		isAccess = true;
		isX2 = true;
		isInter = true;
		if(!preTest())
			return;
		testConfig();
		qosBasedTest();
		postTest();
	}

	@Test
	@TestProperties(name = "Qos Based HO - Access A5 Network Entry trigger, Inter S1 successful HO ", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void NetworkEntryAccessA5InterS1() {
		isAccess = true;
		isX2 = false;
		isInter = true;
		if(!preTest())
			return;
		testConfig();
		qosBasedTest();
		postTest();
	}

	@Test
	@TestProperties(name = "Qos Based HO - Capacity A5 Network Entry trigger, Intra X2 successful HO ", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void NetworkEntryCapacityA5IntraX2() {
		isAccess = false;
		isX2 = true;
		isInter = false;
		if(!preTest())
			return;
		testConfig();
		qosBasedTest();
		postTest();
	}

	@Test
	@TestProperties(name = "Qos Based HO - Capacity A5 Network Entry trigger, Intra S1 successful HO ", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void NetworkEntryCapacityA5IntraS1() {
		isAccess = false;
		isX2 = false;
		isInter = false;
		if(!preTest())
			return;
		testConfig();
		qosBasedTest();
		postTest();
	}

	@Test
	@TestProperties(name = "Qos Based HO - Capacity A5 Network Entry trigger, Inter X2 successful HO ", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void NetworkEntryCapacityA5InterX2() {
		isAccess = false;
		isX2 = true;
		isInter = true;
		if(!preTest())
			return;
		testConfig();
		qosBasedTest();
		postTest();
	}

	@Test
	@TestProperties(name = "Qos Based HO - Capacity A5 Network Entry trigger, Inter S1 successful HO ", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void NetworkEntryCapacityA5InterS1() {
		isAccess = false;
		isX2 = false;
		isInter = true;
		if(!preTest())
			return;
		testConfig();
		qosBasedTest();
		postTest();
	}

	@Test
	@TestProperties(name = "Qos Based HO - Negative access A5 Network Entry trigger, Inter X2 NO HO", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void NegativeNetworkEntryAccessA5InterX2() {
		isNegative = true;
		isAccess = true;
		isX2 = true;
		isInter = true;
		if(!preTest())
			return;
		testConfig();
		qosBasedTest();
		postTest();
	}

	@Test
	@TestProperties(name = "Qos Based HO - Negative capacity A5 Network Entry trigger, Inter X2 NO HO", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void NegativeNetworkEntryCapacityA5InterX2() {
		isNegative = true;
		isAccess = false;
		isX2 = true;
		isInter = true;
		if(!preTest())
			return;
		testConfig();
		qosBasedTest();
		postTest();
	}

	@Test
	@TestProperties(name = "Qos Based HO - Negative capacity 3 Ues A5 Network Entry trigger, Inter X2 NO HO", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void NegativeNetworkEntryCapacity3UesA5InterX2() {
		isNegative = true;
		isAccess = false;
		isX2 = true;
		isInter = true;
		maxAcceptableCalls = 3;
		if(!preTest())
			return;
		testConfig();
		qosBasedTest();
		postTest();
	}

	@Test
	@TestProperties(name = "Qos Based HO - Access A3 HO trigger, Intra X2 FailOver", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void FailOverAccessA3IntraX2() {
		isHoTest = true;
		isAccess = true;
		isX2 = true;
		isInter = false;
		if(!preTest())
			return;
		testConfig();
		qosBasedTest();
		postTest();
	}

	@Test
	@TestProperties(name = "Qos Based HO - Access A3 HO trigger, Intra S1 FailOver", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void FailOverAccessA3IntraS1() {
		isHoTest = true;
		isAccess = true;
		isX2 = false;
		isInter = false;
		if(!preTest())
			return;
		testConfig();
		qosBasedTest();
		postTest();
	}

	@Test
	@TestProperties(name = "Qos Based HO - Access A3 HO trigger, Inter X2 FailOver", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void FailOverAccessA3InterX2() {
		isHoTest = true;
		isAccess = true;
		isX2 = true;
		isInter = true;
		if(!preTest())
			return;
		testConfig();
		qosBasedTest();
		postTest();
	}

	@Test
	@TestProperties(name = "Qos Based HO - Access A3 HO trigger, Inter S1 FailOver ", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void FailOverAccessA3InterS1() {
		isHoTest = true;
		isAccess = true;
		isX2 = false;
		isInter = true;
		if(!preTest())
			return;
		testConfig();
		qosBasedTest();
		postTest();
	}

	@Test
	@TestProperties(name = "Qos Based HO - Capacity A3 HO trigger, Intra X2 FailOver ", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void FailOverCapacityA3IntraX2() {
		isHoTest = true;
		isAccess = false;
		isX2 = true;
		isInter = false;
		if(!preTest())
			return;
		testConfig();
		qosBasedTest();
		postTest();
	}

	@Test
	@TestProperties(name = "Qos Based HO - Capacity A3 HO trigger, Intra S1 FailOver ", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void FailOverCapacityA3IntraS1() {
		isHoTest = true;
		isAccess = false;
		isX2 = false;
		isInter = false;
		if(!preTest())
			return;
		testConfig();
		qosBasedTest();
		postTest();
	}

	@Test
	@TestProperties(name = "Qos Based HO - Capacity A3 HO trigger, Inter X2 FailOver", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void FailOverCapacityA3InterX2() {
		isHoTest = true;
		isAccess = false;
		isX2 = true;
		isInter = true;
		if(!preTest())
			return;
		testConfig();
		qosBasedTest();
		postTest();
	}

	@Test
	@TestProperties(name = "Qos Based HO - Capacity A3 HO trigger, Inter S1 FailOver", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void FailOverCapacityA3InterS1() {
		isHoTest = true;
		isAccess = false;
		isX2 = false;
		isInter = true;
		if(!preTest())
			return;
		testConfig();
		qosBasedTest();
		postTest();
	}

	@Test
	@TestProperties(name = "Qos Based HO - Negative access A3 HO trigger, Inter X2 NO FailOver", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void NegativeFailOverAccessA3InterX2() {
		isNegative = true;
		isHoTest = true;
		isAccess = true;
		isX2 = true;
		isInter = true;
		if(!preTest())
			return;
		testConfig();
		qosBasedTest();
		postTest();
	}

	@Test
	@TestProperties(name = "Qos Based HO - Negative capacity A3 HO trigger, Inter X2 NO FailOver", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void NegativeFailOverCapacityA3InterX2() {
		isNegative = true;
		isHoTest = true;
		isAccess = false;
		isX2 = true;
		isInter = true;
		if(!preTest())
			return;
		testConfig();
		qosBasedTest();
		postTest();
	}

	@Test
	@TestProperties(name = "Qos Based HO - Negative capacity 3 Ues A3 HO trigger, Inter X2 NO FailOver", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void NegativeFailOverCapacity3UesA3InterX2() {
		isNegative = true;
		isHoTest = true;
		isAccess = false;
		isX2 = true;
		isInter = true;
		maxAcceptableCalls = 3;
		if(!preTest())
			return;
		testConfig();
		qosBasedTest();
		postTest();
	}

	private boolean preTest() {
		boolean actionState = true;
		GeneralUtils.startLevel("Pre Test");
		if (!checkFrequency()) {
			GeneralUtils.stopLevel(); // pretest stop level
			return false;
		}
		
		dynUe = SetupUtils.getInstance().getDynamicUEs().get(0);
		staticUe = SetupUtils.getInstance().getStaticUEs(dut1).get(0);
		openQosBasedHoLoggerThreshod();
		stopUes();
		try {
			traffic.startTraffic();
		} catch (Exception e) {
			report.report("Couldn't start traffic", Reporter.WARNING);
			e.printStackTrace();
		}
		peripheralsConfig.setAttenuatorSetValue(attenuatorSetUnderTest, 0);
		actionState &= neighbors.deleteAllNeighbors(dut1);
		actionState &= neighbors.deleteAllNeighbors(dut2);
		GeneralUtils.stopLevel();
		return actionState;
	}

	private boolean checkFrequency() {
		GeneralUtils.startLevel("Checking that frequencies match the test type.");
		report.report(dut1.getName() + " Earfcn: " + dut1.getEarfcn());
		report.report(dut2.getName() + " Earfcn: " + dut2.getEarfcn());
		
		if (isInter) {
			if (dut1.getEarfcn() == dut2.getEarfcn()) {
				report.report("EnodeB frequences are equal in an Inter test, stopping test", Reporter.FAIL);
				reason = "EnodeB frequences are equal in an Inter test.";
				GeneralUtils.stopLevel();
				return false;
			}
		}
		else
			if (dut1.getEarfcn() != dut2.getEarfcn()) {
				report.report("EnodeB frequences are diffrent in an Intra test, stopping test", Reporter.FAIL);
				reason = "EnodeB frequences are diffrent in an Intra test.";
				GeneralUtils.stopLevel();
				return false;
			}	
		
		GeneralUtils.stopLevel(); // Checking that frequencies match SUT stop level		
		return true;
	}

	private void testConfig() {
		GeneralUtils.startLevel("Test Config");		
		configreQosBasedParams();
		addNeighbors();
		if (!isHoTest) {
			setAttToIdealRfConditions();
			tryNum = 1;
		}
		GeneralUtils.stopLevel();
	}

	private boolean configreQosBasedParams() {
		boolean actionSucceeded = true;
		EnabledDisabledStates isAccessEnabled = isAccess ? EnabledDisabledStates.ENABLED
				: EnabledDisabledStates.DISABLED;
		MobilityParameters mobilityParams = new MobilityParameters();
		mobilityParams.setQosBasedMeasurement(EnabledDisabledStates.ENABLED);
		mobilityParams.setQosHoAccessAdmin(isAccessEnabled);
		if (isHoTest) {
			mobilityParams.setQosBasedEventType(ConnectedModeEventTypes.A_3);
		} else {
			mobilityParams.setQosBasedEventType(ConnectedModeEventTypes.A_5);
			mobilityParams.setQosBasedThreshold1(-44);
			mobilityParams.setQosBasedThreshold2(-115);
		}
		ArrayList<Integer> qosBasedEarfcnList = new ArrayList<>();
		qosBasedEarfcnList.add(dut2.getEarfcn());
		mobilityParams.setQosBasedEarfcnList(qosBasedEarfcnList);
		actionSucceeded &= enodeBConfig.cloneAndSetMobilityProfileViaNetSpan(dut1,
				dut1.defaultNetspanProfiles.getMobility(), mobilityParams);
		if (!isAccess){
			report.report("Setting maxAcceptableCalls to: " + maxAcceptableCalls);
			actionSucceeded &= dut1.setMaxVolteCalls(maxAcceptableCalls);
		}
		return actionSucceeded;
	}

	private void qosBasedTest() {
		GeneralUtils.startLevel("Qos Based Test");
		setPassCriteriaCounters();
		resetPassCriteriaCounters();
		printDebugData();
		if (isHoTest)
			qosBasedHoTriggeredByHO();
		else
			qosBasedHOTriggeredByNetworkEntry();
		enodebConfig.waitGranularityPeriodTime(dut1);
		if (!verifyCounters(dut1)) {
			if (tryNum == 2)
				report.report("Test Failed", Reporter.FAIL);
			else {
				tryNum = 2;
				report.report("Test failed first time, trying again", Reporter.WARNING);
				stopUes();
				qosBasedTest();
			}
		} else
			report.step("Test Passed");
		GeneralUtils.stopLevel();
	}

	private void qosBasedHOTriggeredByNetworkEntry() {
		boolean actionState;
		if (!isAccess) {
			report.report("Starting static ue");
			actionState = staticUe.start();
			GeneralUtils.unSafeSleep(5000);
			actionState &= peripheralsConfig.checkSingleUEConnectionToNode(staticUe, dut1);
			if (!actionState) {
				report.report("Failed to start static ue. Test Failed", Reporter.FAIL);
			}
			else
				report.report("Static ue started.");
		}
		report.report("Starting dynamic ue");
		actionState = dynUe.start();
		if (!actionState) {
			report.report("Failed to start dynamic ue. Test Failed", Reporter.FAIL);
		}
		GeneralUtils.unSafeSleep(5000);
		actionState = peripheralsConfig.checkSingleUEConnectionToNode(dynUe, dut1);
		if (!actionState) {
			report.report(String.format("The UE is not connected to %s. Test Failed", dut1.getName()), Reporter.FAIL);
		}
		if (!isNegative || maxAcceptableCalls == 3) {
			report.report("Waiting for Qos Based HO due to ERAB SETAP REQUEST");
			GeneralUtils.unSafeSleep(90000);
		} else {
			report.report("Stopping dynamic ue");
			actionState = dynUe.stop();
		}
	}

	private void qosBasedHoTriggeredByHO() {
		boolean actionState;
		actionState = attenuatorSetUnderTest.setAttenuation(attenuatorSetUnderTest.getMaxAttenuation());
		if (!isAccess) {
			report.report("Starting static ue");
			actionState = staticUe.start();
			GeneralUtils.unSafeSleep(5000);
			actionState = peripheralsConfig.checkSingleUEConnectionToNode(staticUe, dut1);
			if (!actionState) {
				report.report("Failed to start static ue. Test Failed", Reporter.FAIL);
			}
			else
				report.report("Static ue started.");
		}
		report.report("Starting dynamic ue");
		actionState = dynUe.start();
		if (!actionState) {
			report.report("Failed to start dynamic ue. Test Failed", Reporter.FAIL);
		}
		GeneralUtils.unSafeSleep(5000);
		actionState = peripheralsConfig.checkSingleUEConnectionToNode(dynUe, dut2);
		if (!actionState) {
			report.report(String.format("The UE is not connected to %s. Test Failed", dut1.getName()),
					Reporter.WARNING);
		}
		if (!isNegative || maxAcceptableCalls == 3) {
			report.report("Waiting for ERAB SETAP REQUEST");
			GeneralUtils.unSafeSleep(90000);
		}
		report.report(String.format("Moving Attenuator towards %s to trigger Qos Based Failover", dut1.getName()));
		actionState = peripheralsConfig.moveAtt(attenuatorSetUnderTest, attenuatorSetUnderTest.getMaxAttenuation(),
				attenuatorSetUnderTest.getMinAttenuation());
		if(isHoTest && isNegative)
			actionState = peripheralsConfig.checkSingleUEConnectionToNode(dynUe, dut1);
		report.report("Stopping dynamic ue");
		actionState = dynUe.stop();
		if (!actionState) {
			report.report("Couldn't move attenuator. Test Failed", Reporter.FAIL);
		}
	}

	private void setPassCriteriaCounters() {
		String accessocapacity = isAccess ? "Access" : "Capacity";
		int count = isNegative ? 0 : 1;
		if (isHoTest) {
			String x2os1 = isX2 ? "X2" : "S1";
			String interointra = isInter ? "Inter" : "Intra";
			String HoFailedCounter = String.format("Ho%s%sFreqInPrepFailRnlQosBasedHo%sLimitationRejected", x2os1,
					interointra, accessocapacity);
			passCriteria.put(HoFailedCounter, count);
		} else {
			String QosHoAttCounter = String.format("HoInterEnbOutQos%sAtt", accessocapacity);
			String QosHoSuccCounter = String.format("HoInterEnbOutQos%sSuccSentConnReconfig", accessocapacity);
			passCriteria.put(QosHoAttCounter, count);
			passCriteria.put(QosHoSuccCounter, count);
		}
	}

	private boolean verifyCounters(EnodeB enb) {
		GeneralUtils.startLevel("Counters value for EnodeB " + enb.getNetspanName());
		boolean isSucceeded = true;
		for (Map.Entry<String, Integer> counter : passCriteria.entrySet()) {
			int counterSum = enb.getCountersValue(counter.getKey());
			if (isCountAsExpected(counter.getValue(), counterSum, isHoTest && !isNegative)) {
				report.report("[Pass Criteria: ] EnodeB: " + enb.getName() + " counter " + counter.getKey()
						+ " value is: " + counterSum);
			} else {
				report.report("[Pass Criteria: ] EnodeB: " + enb.getName() + " counter " + counter.getKey()
						+ " value is: " + counterSum, Reporter.WARNING);
				isSucceeded = false;
			}
		}
		for (Map.Entry<String, Integer> counter : otherCounters.entrySet()) {
			int counterSum = enb.getCountersValue(counter.getKey());
			if (counterSum == counter.getValue()) {
				report.report("[Pass Criteria: ] EnodeB: " + enb.getName() + " counter " + counter.getKey()
						+ " value is: " + counterSum);
			} else {
				report.report("[Pass Criteria: ] EnodeB: " + enb.getName() + " counter " + counter.getKey()
						+ " value is: " + counterSum, Reporter.WARNING);
			}
		}
		GeneralUtils.stopLevel();
		return isSucceeded;
	}

	private boolean isCountAsExpected(int expected, int actual, boolean isGt) {
		if (isGt) {
			report.report(String.format("Expected value is more than or equals to %s", expected));
			return actual >= expected;
		} else {
			report.report(String.format("Expected value is: %s", expected));
			return actual == expected;
		}
	}

	private boolean resetPassCriteriaCounters() {
		GeneralUtils.startLevel("changing counters value to 0");
		for (int i = 0; i < 2; i++) {
			boolean isSucceeded = true;
			boolean result1 = dut1.resetCounter(null, null, null);
			GeneralUtils.printToConsole(dut1.getName() + " set value: " + String.valueOf(result1));
			enodebConfig.waitGranularityPeriodTime(dut1);
			for (String counter : otherCounters.keySet()) {
				if (dut1.getCountersValue(counter) != 0) {
					if (i == 1) {
						report.report("Counter " + counter + " values are not 0 in enodeB: " + dut1.getName(),
								Reporter.WARNING);
						reason = "Counter values are not 0 in enodeB";
					}
				} else
					report.report("Counter " + counter + " values are 0 in enodeB: " + dut1.getName());
			}
			for (String counter : passCriteria.keySet()) {
				if (dut1.getCountersValue(counter) != 0) {
					if (i == 1) {
						report.report("Counter " + counter + " values are not 0 in enodeB: " + dut1.getName(),
								Reporter.WARNING);
						reason = "Counter values are not 0 in enodeB";
					}
					isSucceeded = false;
				} else
					report.report("Counter " + counter + " values are 0 in enodeB: " + dut1.getName());
			}
			if (isSucceeded || i == 1) {
				GeneralUtils.stopLevel();
				return isSucceeded;
			} else
				report.report("Counters values are not 0, reseting counters to 0 again.");
		}
		GeneralUtils.stopLevel();
		return false;
	}

	private void openQosBasedHoLoggerThreshod() {
		report.report("Setting Qos Based HO logger threshold to 0");
		dut1.setSessionLogLevel("QOS_BASED_HO", "*", 0);
		dut2.setSessionLogLevel("QOS_BASED_HO", "*", 0);
	}

	private boolean addNeighbors() {
		boolean actionState = true;
		X2ControlStateTypes X2State = isX2 ? X2ControlStateTypes.AUTOMATIC : X2ControlStateTypes.NOT_ALLOWED;
		HandoverType hoType = isX2 ? HandoverType.TRIGGER_X_2 : HandoverType.S_1_ONLY;
		GeneralUtils.startLevel("Add Neighbors.");
		actionState &= neighbors.addNeighbor(dut1, dut2, HoControlStateTypes.ALLOWED, X2State, hoType, true, "0");
		actionState &= neighbors.addNeighbor(dut2, dut1, HoControlStateTypes.ALLOWED, X2State, hoType, true, "0");
		if (!actionState)
			report.report(" Failed to add Neighbors.", Reporter.WARNING);
		report.report("X2 Type: " + X2State);
		report.report("HO Type: " + hoType);
		GeneralUtils.reportHtmlLink(dut1.getName() + ": db get nghList", dut1.lteCli("db get nghList"));
		GeneralUtils.reportHtmlLink(dut2.getName() + ": db get nghList", dut2.lteCli("db get nghList"));
		GeneralUtils.stopLevel();
		return actionState;
	}

	private boolean setAttToIdealRfConditions() {
		int nghRsrp;
		int servingRsrp;
		int att = attenuatorSetUnderTest.getMinAttenuation();
		boolean isUeConnectedToServing;
		GeneralUtils.startLevel("Set ideal RF conditions");
		report.report(String.format("Setting attenuators to Min value - %s", att));
		attenuatorSetUnderTest.setAttenuation(att);
		report.report(String.format("Setting %s to OOS", dut1.getName()));
		peripheralsConfig.changeEnbState(dut1, EnbStates.OUT_OF_SERVICE);
		report.report("Starting dyn UE");
		dynUe.start();
		GeneralUtils.unSafeSleep(5000);
		nghRsrp = dynUe.getRSRP(0);
		report.report(String.format("Neighbor RSRP is: %s", nghRsrp));
		report.report("Stopping dyn UE");
		dynUe.stop();
		report.report(String.format("Setting %s to IS", dut1.getName()));
		peripheralsConfig.changeEnbState(dut1, EnbStates.IN_SERVICE);
		GeneralUtils.unSafeSleep(5000);
		report.report("Starting dyn UE");
		dynUe.start();
		GeneralUtils.unSafeSleep(5000);
		isUeConnectedToServing = peripheralsConfig.checkSingleUEConnectionToNode(dynUe, dut1);
		servingRsrp = dynUe.getRSRP(0);
		while (Math.abs(servingRsrp - nghRsrp) > 5 && isUeConnectedToServing) {
			attenuatorSetUnderTest.setAttenuation(++att);
			GeneralUtils.unSafeSleep(1000);
			servingRsrp = dynUe.getRSRP(0);
			GeneralUtils.printToConsole("Serving cell rsrp is: " + servingRsrp);
			isUeConnectedToServing = peripheralsConfig.checkSingleUEConnectionToNode(dynUe, dut1);
		}
		report.report(String.format("Selected attenuation is %s", att));
		report.report("Stopping dyn UE");
		dynUe.stop();
		GeneralUtils.stopLevel();
		if (!isUeConnectedToServing) {
			report.report("UE is not connected to Main enb", Reporter.WARNING);
			if (tryNum == 1) {
				tryNum = 2;
				report.report("Restarting dyn UE");
				dynUe.reboot();
				GeneralUtils.unSafeSleep(100000);
				report.report("Retrying to set RF conditions");
				return setAttToIdealRfConditions();
			}
			return false;
		}
		return true;
	}

	private void printDebugData() {
		GeneralUtils.startLevel("DEBUG DATA");
		GeneralUtils.reportHtmlLink(dut1.getName() + " - db get qosBasedHOEarfcn", dut1.lteCli("db get qosBasedHOEarfcn"));
		GeneralUtils.stopLevel();
	}

	private void stopUes() {
		GeneralUtils.startLevel("Stop Ues.");
		for (UE ue : SetupUtils.getInstance().getAllUEs()) {
			ue.stop();
		}
		GeneralUtils.stopLevel();
	}

	private void startUes() {
		GeneralUtils.startLevel("Start Ues.");
		for (UE ue : SetupUtils.getInstance().getAllUEs()) {
			ue.start();
		}
		GeneralUtils.unSafeSleep(10000);
		GeneralUtils.stopLevel();
	}

	public boolean postTest() {
		boolean actionState = true;
		GeneralUtils.startLevel("Post Test");
		if (isInter)
			enodeBConfig.setEnbRadioProfile(dut2, dut2.defaultNetspanProfiles.getRadio(), false);
		enodeBConfig.setProfile(dut1, EnbProfiles.Mobility_Profile, dut1.defaultNetspanProfiles.getMobility());
		actionState &= dut1.setMaxVolteCalls(64);
		actionState &= neighbors.deleteAllNeighbors(dut1);
		actionState &= neighbors.deleteAllNeighbors(dut2);
		traffic.stopTraffic();
		startUes();
		GeneralUtils.stopLevel();
		return actionState;
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
}

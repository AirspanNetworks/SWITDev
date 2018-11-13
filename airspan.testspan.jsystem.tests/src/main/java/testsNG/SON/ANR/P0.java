package testsNG.SON.ANR;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.Test;
import Attenuators.AttenuatorSet;
import EPC.EPC;
import EnodeB.EnodeB;
import EnodeB.Components.EnodeBComponentTypes;
import Netspan.EnbProfiles;
import Netspan.API.Enums.ConnectedModeEventTypes;
import Netspan.API.Enums.EnbStates;
import Netspan.API.Enums.SonAnrStates;
import Netspan.Profiles.MobilityParameters;
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
import testsNG.Actions.Log;
import testsNG.Actions.Neighbors;
import testsNG.Actions.PeripheralsConfig;
import testsNG.Actions.Traffic;
import testsNG.Actions.Utils.TrafficGeneratorType;

public class P0 extends TestspanTest {
	
	private enum COMMS_STATUS{
		VALUE_NOT_EXIST(String.valueOf(GeneralUtils.ERROR_VALUE)),
		COMMS_STATUS_INIT("0"),
		COMMS_STATUS_ACTIVE("1"),
		COMMS_STATUS_INACTIVE("2"),
		COMMS_STATUS_NA("3"),
		COMMS_STATUS_SCTP_ONLY("4");
		
		public String value;
		COMMS_STATUS(String index){
			value = index;
		}
		public static COMMS_STATUS get(String value){
			COMMS_STATUS commsStatus = COMMS_STATUS.VALUE_NOT_EXIST;
			for(COMMS_STATUS iterator : COMMS_STATUS.values()){
				if(iterator.value.equals(value)){
					commsStatus = iterator;
					break;
				}
			}
			return commsStatus;
		}
	}

	private int attenuatorStepTime = 2000;

	private String attenuatorSetName = "rudat_set";

	private AttenuatorSet attenuatorSetUnderTest;

	private float attenuatorsCurrentValue;

	private int defaultEarfcn = 38950;

	private int interEarfcn = 39150;

	private boolean isInterTest;

	private boolean isDeleteTest = false;
	
	private boolean mainReboot = false;

	private boolean isTestFinished = false;
	
	public boolean isTriggerS1 = false;

	private EnodeB enodeB;

	private EnodeB neighbor;

	private UE testUe;

	private ArrayList<UE> allUes;

	private ArrayList<Integer> anrFrequencyList;

	private EPC epc;

	private int defaultRemoveThreshold;

	private int nghRemoveThreshold = 60;
	
	private int defaultevalPeriod;
	
	private int evalPeriod = 1;
		
	private int MinAllowedHoSuccessRate = 50;
	
	private SonAnrStates anrState;

	private ConnectedModeEventTypes hoEventType;

	private Neighbors neighbors;
	private PeripheralsConfig peripheralsConfig;
	private EnodeBConfig enodeBConfig;
	private Traffic traffic;

	public EnodeB dut1;
	public EnodeB dut2;

	@Override
	public void init() throws Exception {
		GeneralUtils.startLevel("Test Init.");
		neighbors = Neighbors.getInstance();
		peripheralsConfig = PeripheralsConfig.getInstance();
		enodeBConfig = EnodeBConfig.getInstance();
		traffic = Traffic.getInstance(SetupUtils.getInstance().getAllUEs());
		if (traffic.getGeneratorType()==TrafficGeneratorType.TestCenter){
			String TrafficFile = traffic.getTg().getDefaultConfigTccFile();
			int lastDot = TrafficFile.lastIndexOf('.');
			String HOTrafficFile = TrafficFile.substring(0, lastDot) + "_HO" + TrafficFile.substring(lastDot);
			traffic.configFile = new File(HOTrafficFile);
		}
		if (TestConfig.getInstace() != null) {
			interEarfcn = TestConfig.getInstace().getInterEarfcn();
			defaultEarfcn = TestConfig.getInstace().getDefaultEarfcn();
		} else {
			report.report("Running in Test Mode!! - Configure parameters in SUT file", Reporter.WARNING);
			reason += "Running in Test Mode!! - Configure parameters in SUT file\n";
		}

		testUe = SetupUtils.getInstance().getDynamicUEs().get(0);
		allUes = SetupUtils.getInstance().getAllUEs();
		GeneralUtils.startLevel((String.format("Stoping unnecessarry ues.")));
		for (UE ue : allUes) {
			if(!ue.stop())
				report.report("Failed stopping UE " + ue.getName(),Reporter.WARNING);
			GeneralUtils.unSafeSleep(1000);
		}
		testUe.start();
		GeneralUtils.unSafeSleep(10000);
		GeneralUtils.stopLevel();
		epc = EPC.getInstance();

		if (dut1 == null)
			throw new Exception("DUT1 is not loaded");
		if (dut2 == null)
			throw new Exception("DUT2 is not loaded");

		//initial values
		enodeB = dut1;
		neighbor = dut2;
				
		enbInTest = new ArrayList<>();
		enbInTest.add(dut1);
		enbInTest.add(dut2);

		super.init();
		attenuatorSetUnderTest = AttenuatorSet.getAttenuatorSet(attenuatorSetName);
		GeneralUtils.stopLevel();
	}

	/*
	 * Add Intra frequency neighbor using ANR
	 */
	@Test
	@TestProperties(name = "Add Periodic ANR Intra Frequency Neighbors", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful", "anrState", "isInterTest"})
	public void addPeriodicAnrIntraFrequencyNeighbors() {

		// Pre-Test.
		anrState = SonAnrStates.PERIODICAL_MEASUREMENT;
		isInterTest = false;
		isDeleteTest = false;
		if (preTest())
			addAnrNeighbor();
		postTest();
	}

	public void addPeriodicAnrIntraFrequencyNeighborsNoPost() {

		// Pre-Test.
		anrState = SonAnrStates.PERIODICAL_MEASUREMENT;
		isInterTest = false;
		isDeleteTest = false;
		if (preTest())
			addAnrNeighbor();
	}
	
	/*
	 * Add Inter frequency neighbor using ANR
	 */
	@Test
	@TestProperties(name = "Add Periodic ANR Inter Frequency Neighbors", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful", "anrState", "isInterTest" })
	public void addPeriodicAnrInterFrequencyNeighbors()  {
		anrState = SonAnrStates.PERIODICAL_MEASUREMENT;
		isInterTest = true;
		isDeleteTest = false;
		if (preTest())
			addAnrNeighbor();
		postTest();
	}

	/*
	 * Remove Intra frequency neighbor using ANR
	 */
	@Test
	@TestProperties(name = "Add & Remove Periodic ANR Intra Frequency Neighbors", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful", "anrState", "isInterTest" })
	public void addAndRemovePeriodicAnrIntraFrequencyNeighbors() {
		anrState = SonAnrStates.PERIODICAL_MEASUREMENT;
		isInterTest = false;
		isDeleteTest = true;
		if (preTest()) {
			if(addAnrNeighbor())
				removePeriodicAnrNeighbor();
		}
		postTest();
	}

	/*
	 * Remove Inter frequency neighbor using ANR
	 */
	@Test
	@TestProperties(name = "Add & Remove Periodic ANR Inter Frequency Neighbors", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful", "anrState", "isInterTest" })
	public void addAndRemovePeriodicAnrInterFrequencyNeighbors() {
		anrState = SonAnrStates.PERIODICAL_MEASUREMENT;
		isInterTest = true;
		isDeleteTest = true;
		if (preTest()) {
			if(addAnrNeighbor())
				removePeriodicAnrNeighbor();
		}
		postTest();
	}

	/*
	 * Remove HO neighbor using ANR
	 */
	/*
	 * @Test
	 * 
	 * @TestProperties(name = "Remove Intra Frequency HO Based Neighbors",
	 * returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
	 * "IsTestWasSuccessful" }) public void removeIntraFrequencyHoNeighbors()
	 * throws Exception { anrState = SonAnrStates.HO_MEASUREMENT; hoEventType =
	 * ConnectedModeEventTypes.A_3; isInterTest = false; preTest();
	 * addAnrNeighbor(); removeHOAnrNeighbor(); postTest(); }
	 */

	/*
	 * Add Intra frequency neighbor using ANR
	 */
	@Test
	@TestProperties(name = "Add Ho Based A3 Neighbors", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful", "anrState", "isInterTest" })
	public void addHoBasedA3Neighbors() {
		neighbors.verifyAnrNeighbor(dut1, dut2);
		// Pre-Test.
		anrState = SonAnrStates.HO_MEASUREMENT;
		hoEventType = ConnectedModeEventTypes.A_3;
		isInterTest = false;
		MinAllowedHoSuccessRate=50;
		if (preTest())
			addAnrNeighbor();
		postTest();
	}

	/*
	 * Add Intra frequency neighbor using ANR
	 */
	@Test
	@TestProperties(name = "Add Ho Based A4 Neighbors", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful", "anrState", "isInterTest" })
	public void addHoBasedA4Neighbors() {
		anrState = SonAnrStates.HO_MEASUREMENT;
		hoEventType = ConnectedModeEventTypes.A_4;
		isInterTest = false;
		MinAllowedHoSuccessRate=50;
		if (preTest())
			addAnrNeighbor();
		postTest();
	}

	/*
	 * Add Intra frequency neighbor using ANR
	 */
	@Test
	@TestProperties(name = "Add Ho Based A5 Inter Frequency Neighbors", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful", "anrState", "isInterTest" })
	public void addHoBasedA5InterFrequencyNeighbors() {
		// Pre-Test.
		anrState = SonAnrStates.HO_MEASUREMENT;
		hoEventType = ConnectedModeEventTypes.A_5;
		isInterTest = true;
		MinAllowedHoSuccessRate=50;
		if (preTest())
			addAnrNeighbor();
		postTest();
	}

	public boolean preTest() {
			GeneralUtils.startLevel("ANR Pre test.");
			
			GeneralUtils.startLevel("Checking frequencies");
			int startEarfcm = neighbor.getEarfcn() ;
			report.report(neighbor.getName() + " Earfcn: " + startEarfcm);
			int mainEarfcm = enodeB.getEarfcn();
			report.report(enodeB.getName() + " Earfcn: " + mainEarfcm);
			GeneralUtils.reportHtmlLink("Main: db get cellcfg", enodeB.lteCli("db get cellcfg"));
			GeneralUtils.reportHtmlLink("Neighbor: db get cellcfg", neighbor.lteCli("db get cellcfg"));
			
			if (isInterTest) {
				report.report("Enb frequencies need to be different for inter test.");
				if (startEarfcm == mainEarfcm) {
					report.report("EnodeB frequences are equal in an Inter test.", Reporter.FAIL);
					reason = "EnodeB frequences are equal in an Inter test.";
					GeneralUtils.stopAllLevels();
					return false;
				}
			}
			else
			{
				report.report("Enb frequencies need to be equal for intra test.");
				if (startEarfcm != mainEarfcm) {
					report.report("EnodeB frequences are diffrent in an Intra test.", Reporter.FAIL);
					reason = "EnodeB frequences are diffrent in an Intra test.";
					GeneralUtils.stopAllLevels();
					return false;
				}
			}
			report.report("Frequencies are ok.");	
			
			GeneralUtils.stopLevel();
			SetAttenuatorToMax();
			for (EnodeB enb : enbInTest) {
				
				EnbStates enbState = enb.getRunningState();
				if(enbState != EnbStates.IN_SERVICE)
				{
					report.report("operationalStatus: " + enbState + " instead of " + EnbStates.IN_SERVICE, Reporter.WARNING);
					reason += "operationalStatus: " + enbState + " instead of " + EnbStates.IN_SERVICE;
				} else {
					report.report("operationalStatus: " + enbState);
				}
				
				EnbStates channelStatus = enb.getServiceState();
				if(channelStatus != EnbStates.IN_SERVICE)
				{
					report.report("ChannelStatusInfo: " + channelStatus +" instead of " + EnbStates.IN_SERVICE, Reporter.WARNING);
					reason += "ChannelStatusInfo: " + channelStatus +" instead of " + EnbStates.IN_SERVICE;
				} else {
					report.report("ChannelStatusInfo: " + channelStatus);
				}
				
				GeneralUtils.startLevel((String.format("Prepering %s for the test.", enb.getName())));
				setFrequencyList();
				Log.getInstance().setLogLineToAnalyze(enb, EnodeBComponentTypes.XLP, "ANR ENABLED");
				// verify ngh table is empty on both sides, if not delete all
				// neighbors.
				report.report(String.format("%s - Deleting all neighbors.", enb.getName()));
				neighbors.deleteAllNeighbors(enb);		
				
				// enable ANR on both Enbs.
				Boolean anrStateSuccess;
				try{
				anrStateSuccess = neighbors.SetANRState(enb, anrState, anrFrequencyList, MinAllowedHoSuccessRate);
				}
				catch (Exception e) {
					e.printStackTrace();
					anrStateSuccess = false;
				}
				if(anrStateSuccess == null){
					report.report(String.format("%s - Could not validate ANR Set type.", enb.getName()),Reporter.WARNING);
				}
				else if (anrStateSuccess) {
					report.report(String.format("%s - ANR Enabled type set successfully.", enb.getName()));
				} else {
					report.report(String.format("%s - ANR Set type failed.", enb.getName()),Reporter.FAIL);
					reason += String.format("%s - ANR Set type failed.", enb.getName());
				}
				GeneralUtils.stopLevel();
			}

			GeneralUtils.startLevel("Connecting ues and start traffic.");
			
			
			// start traffic using default tcc.
			report.report("Starting traffic.");
			try {
				traffic.startTraffic();
			} catch (Exception e) {
				report.report("Failed starting traffic", Reporter.WARNING);
				e.printStackTrace();
			}

			// verify Ue is connected.
			ArrayList<UE> Ues = new ArrayList<UE>();
			Ues.add(testUe);
			if (!PeripheralsConfig.getInstance().epcAndEnodeBsConnection(Ues, enbInTest)) {
				report.report("Ue is disconnected, restarting Ue.", Reporter.WARNING);
				peripheralsConfig.rebootUEs(Ues);
				if (!PeripheralsConfig.getInstance().epcAndEnodeBsConnection(Ues, enbInTest)) {
					report.report("Ue is disconnected, Stopping test.", Reporter.FAIL);
					GeneralUtils.stopLevel();
					GeneralUtils.stopLevel();
					return false;
				}
			}
			GeneralUtils.stopLevel();
			
			// select the first Enb using EPC
			EnodeB currentEnb = epc.getCurrentEnodB(testUe, enbInTest);
			enodeB = currentEnb != null ? currentEnb : dut1;
			neighbor = enodeB == dut1 ? dut2 : dut1;

			report.report("Main enodeB: " + enodeB.getName());
			report.report("neighbor enodeB: " + neighbor.getName());
			GeneralUtils.reportHtmlLink("Main: ue show link", enodeB.lteCliWithResponse("ue show link", "Legend:"));
			GeneralUtils.reportHtmlLink("Main: ue show rate", enodeB.lteCliWithResponse("ue show rate","Cell Total"));
			GeneralUtils.reportHtmlLink("Main: db get nghList", enodeB.lteCli("db get nghList"));
			GeneralUtils.reportHtmlLink("Slave: db get nghList", neighbor.lteCli("db get nghList"));
			
			// change ngh remove threshold in case of "delete test".
			if (isDeleteTest) {
				
				GeneralUtils.startLevel("Setting Eval period to " + evalPeriod + " and ngh remove threshold level to " + nghRemoveThreshold + " to reduce removal time.");
				defaultevalPeriod = enodeB.getEvalPeriod();
				defaultRemoveThreshold = enodeB.getNghRemoveThreshold();
				if (defaultRemoveThreshold != nghRemoveThreshold || defaultevalPeriod != evalPeriod) {
					enodeB.setEvalPeriod(evalPeriod);
					setRemoveThreshold(enodeB, nghRemoveThreshold);	
				}else{
					report.report(enodeB.getName() + " ngh remove threshold and eval period already set to the desired levels.");
				}
				GeneralUtils.stopLevel();					
			}
			
			
			// change HO event type if needed.			
			if (anrState == SonAnrStates.HO_MEASUREMENT) {
				int RSRP1 = getRSRPFromUE(testUe,0);
				
				if (RSRP1 == GeneralUtils.ERROR_VALUE) {			
					GeneralUtils.startLevel("Failed to get RSRP from UE. Rebooting with IP Power");
					testUe.rebootWithIpPowerWith2MinutesWaiting();
					GeneralUtils.stopLevel();
					
					RSRP1 = getRSRPFromUE(testUe,0);
					if (RSRP1 == GeneralUtils.ERROR_VALUE) {
						report.report("Error in getting RSRP from UE "+testUe.getLanIpAddress()+", Test Fail in the cause of UE not functionning", Reporter.FAIL);
						GeneralUtils.stopAllLevels();
						return false;						
					}
				}
				
				for (EnodeB enb : enbInTest) {
					GeneralUtils.startLevel("Setting mobility profile for " + enb.getName());
					MobilityParameters mobilityParas = new MobilityParameters();
					
					report.report("Setting IsIntra: " + !isInterTest);
					mobilityParas.setIsIntra(!isInterTest);
					
					report.report("Setting eventType: " + hoEventType);
					mobilityParas.setEventType(hoEventType);
					
					report.report("Setting Hysteresis: " + 1);
					mobilityParas.setHysteresis(1.0);
					if (hoEventType == ConnectedModeEventTypes.A_3) {
						report.report("Setting A3Offset: " + 3);
						mobilityParas.setA3Offset(3.0);
						//set specific to A3
					}
					if (hoEventType == ConnectedModeEventTypes.A_4) {
						report.report("Setting RsrpEventThreshold1: " + (RSRP1 + 3));
						mobilityParas.setRsrpEventThreshold1(RSRP1 + 3);
						//set specific to A4
					}
					if (hoEventType == ConnectedModeEventTypes.A_5) {
						report.report("Setting RsrpEventThreshold1: " + (RSRP1 + 2));
						mobilityParas.setRsrpEventThreshold1(RSRP1 + 2);
						report.report("Setting RsrpEventThreshold2: " + (RSRP1 + 3));
						mobilityParas.setRsrpEventThreshold2(RSRP1 + 3);
						//set specific to A5
					}
					
					if (!enodeBConfig.cloneAndSetMobilityProfileViaNetSpan(enb,
							enb.defaultNetspanProfiles.getMobility(), mobilityParas)) {
						report.report(String.format("%s - failed to set Mobility profile.", enb.getName()),
								Reporter.FAIL);
						GeneralUtils.stopLevel();
						GeneralUtils.stopLevel();
						return false;
					}
					GeneralUtils.stopLevel();
				}
				
			}
			
			if(mainReboot)
			{
				GeneralUtils.startLevel("Restarting " + enodeB.getName() + " due to changing eval period");
				enodeB.reboot();
				enodeB.waitForAllRunningAndInService(EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
				report.report("Verifying Ue connects after restart.");
				testUe.stop();
				GeneralUtils.unSafeSleep(5000);
				testUe.start();
				GeneralUtils.unSafeSleep(5000);
				GeneralUtils.reportHtmlLink("ue show link", enodeB.lteCli("ue show link"));
				GeneralUtils.reportHtmlLink("ue show rate", enodeB.lteCli("ue show rate"));
							
				ArrayList<UE> ues = new ArrayList<UE>();
				ues.add(testUe);
				if(!peripheralsConfig.checkIfAllUEsAreConnectedToNode(ues, enodeB)){
					report.report("Ue is disconnected from main enodeB, restarting Ue.", Reporter.WARNING);
					GeneralUtils.reportHtmlLink("ue show link", enodeB.lteCli("ue show link"));
					peripheralsConfig.rebootUEs(ues);
					GeneralUtils.unSafeSleep(60000);
					if (!peripheralsConfig.checkIfAllUEsAreConnectedToNode(ues, enodeB)) {
						report.report("Ue is disconnected from main enodeB, stopping test.", Reporter.FAIL);
						GeneralUtils.reportHtmlLink("ue show link", enodeB.lteCli("ue show link"));
						GeneralUtils.stopLevel();
						GeneralUtils.stopLevel();
						return false;
					}
				}
				GeneralUtils.stopLevel();
			}
			neighbor.waitForAllRunningAndInService(EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
			
			GeneralUtils.stopLevel();
			return true;
	}
	
	private int getRSRPFromUE(UE ue,int index){
		//if fail try again
		int result = ue.getRSRP(index);
		if (result == GeneralUtils.ERROR_VALUE){
			GeneralUtils.printToConsole("Error Value while trying to get RSRP from ue : "+ue.getName());
			GeneralUtils.printToConsole("trying to get RSRP again");
			GeneralUtils.unSafeSleep(500);
			result = ue.getRSRP(index);
			if(result != GeneralUtils.ERROR_VALUE){
				return result;				
			}
		}else{
			return result;
		}
		
		report.report("tried to get RSRP from and got an error twice, rebooting ue");
		ue.reboot();
		report.report("Waiting for UE to connect - time out 2 minutes");
		peripheralsConfig.waitForUeToConnect(ue,enbInTest,UE.UE_CONNECT_TIMEOUT);
		result = ue.getRSRP(index);
		return result;
	}
	

	public boolean addAnrNeighbor() {
		boolean result = true;
		GeneralUtils.startLevel((String.format("Adding ANR Neighbor.")));
		if (anrState == SonAnrStates.PERIODICAL_MEASUREMENT) {
			String measurmentLog = enodeB.getClass().toString().contains("14_0") ? "WR_UMM_ANR_MEAS_TRANS"
					: "PERIODICAL ANR MEASUREMENT REPORT";
			// check if the ues are sending measurement report.
			report.report("Waiting for ues to send measurment report.");
			if (!Log.getInstance().waitForLogLine(enodeB, EnodeBComponentTypes.XLP, measurmentLog, 30000)) {
				report.report("measurment report could not be detected.");
			}
		}

		// while loop - decrease attenuation, check ngh table until ngh was
		// found. pass criteria
		boolean attenuatorMove = false;
		GeneralUtils.startLevel((String.format("Decreasing attenuators value until ANR ngh addition on %s detection.",
				enodeB.getName())));
		GeneralUtils.reportHtmlLink("db get nghList", enodeB.lteCli("db get nghList"));
		GeneralUtils.reportHtmlLink("cell show anrstate", enodeB.lteCli("cell show anrstate"));
		while (!neighbors.verifyAnrNeighbor(enodeB, neighbor)
				&& attenuatorsCurrentValue > attenuatorSetUnderTest.getMinAttenuation()) {
			attenuatorsCurrentValue -= 1;
			peripheralsConfig.setAttenuatorSetValue(attenuatorSetUnderTest, attenuatorsCurrentValue);
			attenuatorMove = true;
			report.report(String.format("Attenuators value changed to %s, waiting %s seconds..",
					attenuatorsCurrentValue, attenuatorStepTime / 1000));
			
			GeneralUtils.unSafeSleep(attenuatorStepTime);
			GeneralUtils.reportHtmlLink("db get nghList", enodeB.lteCli("db get nghList"));
			GeneralUtils.reportHtmlLink("cell show anrstate", enodeB.lteCli("cell show anrstate"));
		}
		if(!attenuatorMove){
			report.report("Won't do that stage because nbr already exists");
		}
		GeneralUtils.stopLevel();
		
		// print test results.
		GeneralUtils.unSafeSleep(10000);
		GeneralUtils.startLevel("Add Neighbor Results");
		GeneralUtils.reportHtmlLink("db get nghList", enodeB.lteCli("db get nghlist"));
		if (!neighbors.verifyAnrNeighbor(enodeB, neighbor)) {
			report.report("[ERROR]: Can not find the neighbor on ngh list, test failed.", Reporter.FAIL);
			reason += "FAIL: [ERROR]: Can not find the neighbor on ngh list, test failed.\n";
			result = false;
		} else {
			report.report("Neighbour found with attenuator at " + attenuatorSetUnderTest.getAttenuation()[0]);
			
			if ((anrState == SonAnrStates.HO_MEASUREMENT)) {
				String HOtype = enodeB.getTriggerX2HO(neighbor);
				String expectedHoType = isTriggerS1 ? "0" : "1";
				if (HOtype.equals(expectedHoType)) {
					report.report(String.format("[PASS]: %s TriggerX2HO is %s.", enodeB.getName(), expectedHoType));
				}else {
					report.report(String.format("[ERROR]: %s TriggerX2HO is %s instead of %s",enodeB.getName(), HOtype, expectedHoType), Reporter.FAIL);
					reason += String.format("FAIL: [ERROR]: %s TriggerX2HO is %s instead of %s\n",enodeB.getName(), HOtype, expectedHoType);
				}
			}
			report.report(String.format("[PASS]: %s was added to %s as ANR Neighbor successfully.", neighbor.getName(),
					enodeB.getName()));
		}
		GeneralUtils.stopLevel();
		GeneralUtils.stopLevel();
		return result;
	}

	public boolean failToAddAnrNeighbor() throws Exception {
		boolean result = true;
		report.startLevel((String.format("Trying to add ANR Neighbor.")));
		if (anrState == SonAnrStates.PERIODICAL_MEASUREMENT) {
			String measurmentLog = enodeB.getClass().toString().contains("14_0") ? "WR_UMM_ANR_MEAS_TRANS"
					: "PERIODICAL ANR MEASUREMENT REPORT";
			// check if the ues are sending measurement report.
			report.report("Waiting for ues to send measurment report.");
			if (!Log.getInstance().waitForLogLine(enodeB, EnodeBComponentTypes.XLP, measurmentLog, 30000)) {
				report.report("measurment report could not be detected.");
			}
		}

		// while loop - decrease attenuation, check ngh table until ngh was
		// found. pass criteria
		report.startLevel((String.format("Decreasing attenuators value until ANR ngh addition on %s detection.",
				enodeB.getName())));
		GeneralUtils.reportHtmlLink("db get nghList", enodeB.lteCli("db get nghList"));
		while (!neighbors.verifyAnrNeighbor(enodeB, neighbor)
				&& attenuatorsCurrentValue > attenuatorSetUnderTest.getMinAttenuation()) {
			attenuatorsCurrentValue -= 1;
			peripheralsConfig.setAttenuatorSetValue(attenuatorSetUnderTest, attenuatorsCurrentValue);
			report.report(String.format("Attenuators value changed to %s, waiting %s seconds..",
					attenuatorsCurrentValue, attenuatorStepTime / 1000));
			Thread.sleep(attenuatorStepTime);
		}
		report.stopLevel();

		// print test results.
		report.startLevel("Add Neighbor Results");
		GeneralUtils.reportHtmlLink("db get nghList", enodeB.lteCli("db get nghlist"));
		if (neighbors.verifyAnrNeighbor(enodeB, neighbor)) {
			report.report("[ERROR]: Neighbor is on ngh list, test failed.", Reporter.FAIL);
			reason += "FAIL: [ERROR]: Neighbor is on ngh list, test failed.\n";
			result = false;
		} else {
			report.report(String.format("[PASS]: %s was NOT added to %s as ANR Neighbor as expected.", neighbor.getName(),
					enodeB.getName()));
		}
		report.stopLevel();
		report.stopLevel();
		return result;
	}
	
	private void removePeriodicAnrNeighbor() {
		GeneralUtils.startLevel("Removing Periodic ANR Neighbor.");

		// verify neighbor exists
		if (!neighbors.verifyAnrNeighbor(enodeB, neighbor)) {
			report.report("[ERROR]: Can not find the neighbor on ngh list after reboot, test failed.", Reporter.FAIL);
			reason += "FAIL: [ERROR]: Can not find the neighbor on ngh list after reboot, test failed.\n";
		} else {
			report.report(String.format("%s exists on ngh list.", neighbor.getName()));
		}

		// avoid X2 update message for deleting ANR Neighbor by adding static route that reject all packets send to DUT
		avoidX2UpdateMessage(enodeB, neighbor);
		
		// set neighbor to out of service.
		report.report(String.format("Setting %s operational status to \"out of service\".", neighbor.getName()));
		peripheralsConfig.changeEnbState(neighbor,EnbStates.OUT_OF_SERVICE);
		peripheralsConfig.epcAndEnodeBsConnection(new ArrayList<UE>(Arrays.asList(testUe)), enbInTest);

		// millisecond) and verify no neighbors.
		GeneralUtils.startLevel("Removal variables");
		GeneralUtils.reportHtmlLink(enodeB.getName() + ": db get anrcfg", enodeB.lteCli("db get anrcfg"));
		int currentEvalPeriod = enodeB.getEvalPeriod();
		int currentNghRemoveThreshold = enodeB.getNghRemoveThreshold();
		report.report("currentEvalPeriod: " + currentEvalPeriod + ", currentNghRemoveThreshold: " + currentNghRemoveThreshold);
		if (currentEvalPeriod != evalPeriod || currentNghRemoveThreshold != nghRemoveThreshold) {
			
			report.report("Expected Eval Period: " + evalPeriod);
			report.report("Expected ngh Remove Threshold: " + nghRemoveThreshold);
			report.report("Current Eval Period: " + currentEvalPeriod);
			report.report("Current ngh Remove Threshold: " + currentNghRemoveThreshold);
			
		}
		GeneralUtils.stopLevel();
		report.report("Wait EvalPeriod * nghRemoveThreshold * 1.5 + 60 = " + (evalPeriod * nghRemoveThreshold * 1.5 + 60) + " seconds for neighbor deletion");
		GeneralUtils.unSafeSleep((long)(evalPeriod * nghRemoveThreshold * 1.5 + 60) * 1000);
	
		GeneralUtils.startLevel("Remove Neighbor Results");
		GeneralUtils.reportHtmlLink("db get nghList", enodeB.lteCli("db get nghlist"));
		if (neighbors.verifyAnrNeighbor(enodeB, neighbor)) {
			report.report("[ERROR]: The neighbor is still on ngh list after deletion period expiration, test failed.",
				Reporter.FAIL);
			reason += "FAIL: [ERROR]: The neighbor is still on ngh list after deletion period expiration, test failed.\n";
		} else {
			report.report(String.format("[PASS]: %s was deleted from %s after deletion period.", neighbor.getName(),
					enodeB.getName()));
		}
		GeneralUtils.stopLevel();
		removeStaticRoute();
		GeneralUtils.stopLevel();
	}

	private void avoidX2UpdateMessage(EnodeB enodeB, EnodeB neighbor) {
		GeneralUtils.startLevel("Avoid X2 Update Message.");
		String dutX2IpAddress = enodeB.getS1IpAddress();
		String mnoBroadcastPlmn = enodeB.calculateMnoBroadcastPlmn();
		String eutranCellId = neighbor.getHomeCellIdentity();
		GeneralUtils.reportHtmlLink("db get nghListStatus x2ConStatus", enodeB.lteCli("db get nghListStatus x2ConStatus"));
		String asLteStkNghListCommsStatusValue = enodeB.getX2ConStatus(mnoBroadcastPlmn, eutranCellId);
		COMMS_STATUS x2Status = COMMS_STATUS.get(asLteStkNghListCommsStatusValue);
		report.report("asLteStkNghListCommsStatus's value = " + asLteStkNghListCommsStatusValue + " ("+x2Status+").");
		report.report("Adding static route to "+neighbor.getName()+"("+neighbor.getS1IpAddress()+") that reject sending packets to "+enodeB.getName()+" ("+dutX2IpAddress+") for avoiding X2 update message (route add -host " + dutX2IpAddress + " reject).");
		neighbor.shell("route add -host " + dutX2IpAddress + " reject");
		GeneralUtils.startLevel("Wainting for X2 status will be INACTIVE (5 minutes timeout).");
		for(int i = 1; i <= 30; i++){
			GeneralUtils.startLevel("Try Number #" + i);
			GeneralUtils.unSafeSleep(10 * 1000);
			GeneralUtils.reportHtmlLink("db get nghListStatus x2ConStatus", enodeB.lteCli("db get nghListStatus x2ConStatus"));
			report.report("Get x2ConStatus for: mnoBroadcastPlmn="+mnoBroadcastPlmn+", eutranCellId="+eutranCellId+".");
			asLteStkNghListCommsStatusValue = enodeB.getX2ConStatus(mnoBroadcastPlmn, eutranCellId);
			x2Status = COMMS_STATUS.get(asLteStkNghListCommsStatusValue);
			report.report("asLteStkNghListCommsStatus's value = " + asLteStkNghListCommsStatusValue + ", while expected value is 2.");
			GeneralUtils.stopLevel();
			if(x2Status == COMMS_STATUS.COMMS_STATUS_INACTIVE){
				break;
			}
		}
		GeneralUtils.stopLevel();
		if(x2Status != COMMS_STATUS.COMMS_STATUS_INACTIVE){
			report.report("COMMS_STATUS equals COMMS_STATUS_INACTIVE (it equals "+x2Status+")", Reporter.FAIL);
		}else{
			report.step("COMMS_STATUS equals COMMS_STATUS_INACTIVE");
		}
		GeneralUtils.stopLevel();
	}
	
	void removeStaticRoute(){
		String dutX2IpAddress = enodeB.getS1IpAddress();
		report.report("Removing static route from "+neighbor.getName()+" that reject sending packets to "+enodeB.getName()+"("+dutX2IpAddress+"), (route del -host " + dutX2IpAddress + " reject).");
		neighbor.shell("route del -host " + dutX2IpAddress + " reject");
	}

	private void setRemoveThreshold(EnodeB enodeB, int value) {
		report.report(String.format("%s - changing remove threshold to %s.", enodeB.getName(), value));
		enodeB.setNghRemoveThreshold(value);
		mainReboot = true;	
	}

	/*
	 * private void removeHOAnrNeighbor() throws Exception {
	 * report.startLevel((String.format("Removing HO ANR Neighbor."))); //
	 * change anr parameters to reduce deletion time.
	 * report.report(String.format(
	 * "%s - changing anr parameters in order to reduce neighbor deletion time."
	 * , enodeB.getName())); int currentMaxUeSupportedValue =
	 * Integer.parseInt("0" + enodeB.getMaxUeSupported().trim(), 10); if
	 * (currentMaxUeSupportedValue == 0) { report.report(String.format(
	 * "Could not MaxUeSupported parameter. Setting to default."));
	 * currentMaxUeSupportedValue = 128; }
	 * 
	 * enodeB.setMaxUeSupported(1); enodeB.reboot();
	 * enodeB.waitForRunning(15 * 1000 * 60);
	 * 
	 * // connect static ues staticUes = super.getStaticUEs(enodeB); for (UE ue
	 * : staticUes) { ue.start(); }
	 * 
	 * // verify static ue is connected. if (enodeB !=
	 * epc.getCurrentEnodB(staticUes.get(0), enbInTest)) {
	 * report.report(String.format("The static ue is not connected to %s.",
	 * enodeB.getName())); }
	 * 
	 * // move attenuator // while loop - decrease attenuation,
	 * report.startLevel((String.format(
	 * "Increasing attenuators value until ANR ngh removal from %s detection.",
	 * enodeB.getName())));
	 * 
	 * while (neighbors.verifyAnrNeighbor(enodeB, neighbor) &&
	 * attenuatorsCurrentValue < attenuatorSetUnderTest.getMaxAttenuation()) {
	 * attenuatorsCurrentValue += 1;
	 * peripheralsConfig.setAttenuatorSetValue(attenuatorSetUnderTest,
	 * attenuatorsCurrentValue); report.report(String.format(
	 * "Attenuators value changed to %s, waiting %s seconds..",
	 * attenuatorsCurrentValue, attenuatorStepTime));
	 * Thread.sleep(attenuatorStepTime); } report.stopLevel();
	 * 
	 * // wait for deletion ((evalPeriod * nghRemoveThreshold * 1.5 + 5) * //
	 * millisecond) and verify no nighbors. Thread.sleep((long) ((15 * 1 * 1.5 +
	 * 5) * 1000)); if (neighbors.verifyAnrNeighbor(enodeB, neighbor)) {
	 * report.report(
	 * "[ERROR]: The neighbor is still on ngh list after deletion period expiration, test failed."
	 * , Reporter.FAIL); reason +=
	 * "FAIL: [ERROR]: The neighbor is still on ngh list after deletion period expiration, test failed.\n"
	 * ; } else { report.report(String.format(
	 * "[PASS]: %s was deleted from %s after due to lack of HO.",
	 * neighbor.getName(), enodeB.getName())); }
	 * 
	 * // set the anr parameters to default. report.report(String.format(
	 * "%s - changing MaxUeSupported to the default value.", enodeB.getName()));
	 * enodeB.setEvalPeriod(currentMaxUeSupportedValue); enodeB.reboot();
	 * enodeB.waitForRunning(15 * 1000 * 60); report.stopLevel(); }
	 */

	public void postTest() {
		GeneralUtils.startLevel(String.format("ANR Post test."));
		
		if(isDeleteTest){
			GeneralUtils.startLevel(String.format("Returning %s operational status to default", neighbor.getName()));
			// set neighbor to in service.
			report.report(String.format("Setting %s operational status to \"in service\".", neighbor.getName()));
			peripheralsConfig.changeEnbState(neighbor,EnbStates.IN_SERVICE);
			GeneralUtils.stopLevel();
			
			// set the anr parameters to default.
			if(defaultRemoveThreshold != nghRemoveThreshold)
			{
				GeneralUtils.startLevel(String.format("Returning %s removal threshold value to default", enodeB.getName()));
				report.report(String.format("%s - changing anr parameters to the default values.", enodeB.getName()));
				setRemoveThreshold(enodeB, defaultRemoveThreshold);
				GeneralUtils.stopLevel();
			}
		}
		
		for (EnodeB enb : enbInTest) {
			// return to the default Son profile
			GeneralUtils.startLevel(String.format("Returning %s to its default state.", enb.getName()));
			report.report(String.format("%s - Setting SON profile to default and deleting neighbours.", enb.getName()));
			enodeBConfig.setProfile(enb, EnbProfiles.Son_Profile, enb.defaultNetspanProfiles.getSON());
			// delete all Neighbors and report if needed.
			Neighbors.getInstance().deleteAllNeighbors(enb);

			// change mobility profile to default if needed.
			if (anrState == SonAnrStates.HO_MEASUREMENT) {
				report.report("Trying to Set Mobility Profile to default Via Netspan.");
				enodeBConfig.setProfile(enb, EnbProfiles.Mobility_Profile,
						enb.defaultNetspanProfiles.getMobility());
			}
			GeneralUtils.stopLevel();
		}
		
		if(mainReboot)
		{
			GeneralUtils.startLevel("Restarting " + enodeB.getName() + " due to Eval period change");
			enodeB.reboot();
			mainReboot = false;			
			GeneralUtils.stopLevel();
		}
		enodeB.waitForAllRunningAndInService(EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
		neighbor.waitForAllRunningAndInService(EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
		
		enodeBConfig.deleteClonedProfiles();
		SetAttenuatorToMax();
		GeneralUtils.startLevel((String.format("Starting all ues.")));
		for (UE ue : allUes) {
			ue.start();
			GeneralUtils.unSafeSleep(1000);
		}
		GeneralUtils.stopLevel();
		// stop traffic
		traffic.stopTraffic();	
		
		isTestFinished = true;
		GeneralUtils.stopLevel();
	}

	@Override
	public void end(){
		if (!isTestFinished)
			postTest();
		isTestFinished = false;

		if (traffic.getGeneratorType() == TrafficGeneratorType.TestCenter) {
			String TrafficFile = traffic.getTg().getDefaultConfigTccFile();
			traffic.configFile = new File(TrafficFile);
		}

		super.end();
	}

	private void SetAttenuatorToMax() {
		// set attenuator to the max value.
		if (!peripheralsConfig.setAttenuatorSetValue(attenuatorSetUnderTest,
				attenuatorSetUnderTest.getMaxAttenuation())) {
			report.report("[ERROR]: Could not set attenuator value, Failing Test.", Reporter.FAIL);
			reason += "FAIL: [ERROR]: Could not set attenuator value, Failing Test.\n";
		} else {
			attenuatorsCurrentValue = attenuatorSetUnderTest.getAttenuation()[0];
			report.report(String.format("Attenuator value was set to maximum value - %sdb.", attenuatorsCurrentValue));
		}
	}

	private void setFrequencyList() {
		anrFrequencyList = new ArrayList<>();
		anrFrequencyList.add(defaultEarfcn);
		report.report(String.format("Earfcn \"%s\" was added to ANR frequency list.", defaultEarfcn));
		if (isInterTest) {
			anrFrequencyList.add(interEarfcn);
			report.report(String.format("Earfcn \"%s\" was added to ANR frequency list.", interEarfcn));
		}
	}

	@ParameterProperties(description = "First DUT")
	public void setDUT1(String dut) {
		GeneralUtils.printToConsole("Load DUT1 " + dut);
		this.dut1 = (EnodeB) SysObjUtils.getInstnce().initSystemObject(EnodeB.class, false, dut).get(0);
		GeneralUtils.printToConsole("DUT loaded" + this.dut1.getNetspanName());
	}

	@ParameterProperties(description = "Second DUT")
	public void setDUT2(String dut) {
		GeneralUtils.printToConsole("Load DUT2 " + dut);
		this.dut2 = (EnodeB) SysObjUtils.getInstnce().initSystemObject(EnodeB.class, false, dut).get(0);
		GeneralUtils.printToConsole("DUT loaded" + this.dut2.getNetspanName());
	}

	public EnodeB getDut1() {
		return dut1;
	}

	public EnodeB getDut2() {
		return dut2;
	}
	
	public void setIsInterTest(boolean isInterTest) {
		this.isInterTest = isInterTest;
	}

	public void setAnrState(SonAnrStates anrState) {
		this.anrState = anrState;
	}
}

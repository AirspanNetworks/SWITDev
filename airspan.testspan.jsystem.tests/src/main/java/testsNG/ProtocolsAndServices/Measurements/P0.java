package testsNG.ProtocolsAndServices.Measurements;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.junit.Test;
import org.snmp4j.smi.Variable;

import Attenuators.AttenuatorSet;
import DMTool.DMtool;
import DMTool.Measurement.MeasurementCfg;
import DMTool.Measurement.MeasurementCfgObject;
import DMTool.Measurement.MeasurementCfgReport;
import DMTool.Measurement.TableManager;
import DMTool.Measurement.TriggerEvents;
import ESon.EsonServer;
import EnodeB.EnodeB;
import Netspan.EnbProfiles;
import Netspan.API.Enums.EnabledDisabledStates;
import Netspan.API.Enums.EnbStates;
import Netspan.API.Enums.HandoverType;
import Netspan.API.Enums.HoControlStateTypes;
import Netspan.API.Enums.SonAnrStates;
import Netspan.API.Enums.X2ControlStateTypes;
import Netspan.Profiles.MobilityParameters;
import Netspan.Profiles.NetworkParameters;
import Netspan.Profiles.SonParameters;
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
import testsNG.Actions.Eson;
import testsNG.Actions.Neighbors;
import testsNG.Actions.PeripheralsConfig;
import testsNG.Actions.Traffic;
import testsNG.Actions.Utils.ParallelCommandsThread;

public class P0 extends TestspanTest{
	private DMtool dm;
	private Neighbors neighbors;
	private EnodeB dutInTest;
	private EnodeB dut2;
	private UE ueInTest;
	private ArrayList<EnodeB> otherEnb;
	private EnodeBConfig enodeBConfig;
	private ArrayList<EnodeB> virtualNeighborsList = new ArrayList<EnodeB>();
	private ArrayList<EnodeB> allNeighborsList; 
	private String attenuatorSetName = "rudat_set";
	private AttenuatorSet attenuatorSetUnderTest = null;
	private PeripheralsConfig peripheralsConfig;
	private int attenuatorStepTime = 2000;
	private HashMap<Integer, Integer> hashRealObject;
	private HashMap<Integer, TriggerEvents> hashRealReport;
	private HashMap<Integer, Integer> hashExpectedObject;
	private HashMap<Integer, TriggerEvents> hashExpectedReport;
	private EsonServer esonServer;
	private Eson eson;
	private boolean flagNetwork = false;
	private ArrayList<UE> dynamicArrayUEs = null;
	private ArrayList<UE> allArrayUEs = null;
	private Integer originalRatio = null;
	private Integer originalMinimum = null;
	private Traffic traffic = null;
	private int startGap;
	private int stopGap;
	private int interEarfcn = -1;
	private int middleEarfcn;
	private boolean needsToReboot = false;
	private ArrayList<UE> toRemove = new ArrayList<UE>();
	private int earfcn = -1;
	private ParallelCommandsThread commandsThreadDut1 = null;
	private ParallelCommandsThread commandsThreadDut2 = null;
	private boolean attenuatorRespond;
	
	//test objects
	private TableManager expectedTables;
	private TableManager measTables;
	private boolean sendOTDOACommandTest;
	private boolean otdoa;
	private String rnti="defaultValue";
	private String cellIndex="0";
	private String offSet="4";
	private String trig="0";               //0 to start ,1 to stop.
	
	
	@SuppressWarnings("unchecked")
	@Override
	public void init() throws Exception{
		enbInTest = new ArrayList<EnodeB>();
		enbInTest.add(dutInTest);
		enbInTest.add(dut2);
		super.init();
		GeneralUtils.startLevel("Init");
		otdoa = false;
		neighbors = Neighbors.getInstance();
		otherEnb = (ArrayList<EnodeB>) enbInSetup.clone();
		otherEnb.remove(dutInTest);
		enodeBConfig = EnodeBConfig.getInstance();
		allNeighborsList = new ArrayList<EnodeB>();
		if(attenuatorSetUnderTest==null){
			attenuatorSetUnderTest = AttenuatorSet.getAttenuatorSet(attenuatorSetName);
		}
		attenuatorRespond = true;
		peripheralsConfig = PeripheralsConfig.getInstance();
		checkAttenuatorConnection();
		dm  = new DMtool();
		esonServer = (EsonServer) system.getSystemObject("EsonServer");
		eson = new Eson();
		traffic = Traffic.getInstance(SetupUtils.getInstance().getAllUEs());
		sendOTDOACommandTest = true;
		trig = "0";
		report.report("Change other eNodeBs to out of service");
		for(EnodeB enb : otherEnb){
			if(peripheralsConfig.changeEnbState(enb, EnbStates.OUT_OF_SERVICE)){
				report.report(enb.getNetspanName() + " is out of service");
			}else{
				report.report("Failed to set " + enb.getNetspanName() + " out of service.", Reporter.WARNING);
			}
		}
		report.report("Set attenuator to minimum");
	
		if(peripheralsConfig.SetAttenuatorToMin(attenuatorSetUnderTest)){
			report.report("Successfully set minimum attenuation");				
		}else{
			report.report("Failed to set minimum attenuation", Reporter.WARNING);
			reason = "Failed to set minimum attenuation";
		}
		attenuatorStepTime = attenuatorSetUnderTest.getStepTime();
		
		if (TestConfig.getInstace() != null) {
			interEarfcn = TestConfig.getInstace().getInterEarfcn();
		}
		allArrayUEs = SetupUtils.getInstance().getAllUEs();
		
		ArrayList<String> commands = new ArrayList<>();
		commands.add("ue show link");
		commands.add("ue show rate");
		commands.add("db get PTPStatus");
		commandsThreadDut1 = new ParallelCommandsThread(commands, dutInTest, null, null);
		commandsThreadDut1.start();
		commandsThreadDut2 = new ParallelCommandsThread(commands, dut2, null, null);
		commandsThreadDut2.start();
		
		expectedTables = new TableManager();
		measTables = new TableManager();
		GeneralUtils.stopLevel();
	}
	
	private void checkAttenuatorConnection(){
		peripheralsConfig.SetAttenuatorToMin(attenuatorSetUnderTest);
		for(float attenuation : attenuatorSetUnderTest.getAttenuation()){
			if(attenuation == GeneralUtils.ERROR_VALUE){
				attenuatorRespond = false;
				report.report("Attenuator doesn't respond.",Reporter.FAIL);
				reason = "Attenuator doesn't respond.";
				GeneralUtils.stopLevel();
				org.junit.Assume.assumeTrue(false);
			}
		}
	}
	
	private boolean preTest(boolean multiple){
		GeneralUtils.startLevel("Pre test");
		report.report("Earfcn of "+dutInTest.getName()+": "+dutInTest.getEarfcn());
		report.report("Earfcn of "+dut2.getName()+": "+dut2.getEarfcn());
		
		report.report("Start traffic");
		try {
			traffic.startTraffic();
		} catch (Exception e) {
			e.printStackTrace();
			report.report("Failed to start traffic",Reporter.WARNING);
		}
		report.report("Get dynamic UE");		

		peripheralsConfig.startUEsOnlySnmp(allArrayUEs);
		dynamicArrayUEs = SetupUtils.getInstance().getDynamicUEs();
		GeneralUtils.startLevel("Get UE in test");
		ueInTest = getUeInTest();
		if(ueInTest==null){
			peripheralsConfig.rebootUEs(dynamicArrayUEs);
			ueInTest = getUeInTest();
			if(ueInTest==null){
				report.report("Failed to find any dynamic UE connected",Reporter.FAIL);
				reason="Failed to find any dynamic UE connected";
				GeneralUtils.stopAllLevels();
				return false;					
			}
		}
		GeneralUtils.stopLevel();
		earfcn = dutInTest.getEarfcn();
		
		getRsrpForMobilityProfile(multiple);
		
		peripheralsConfig.stopUEsOnlySnmp(allArrayUEs);
				
		report.report("Delete neighbors");
		boolean action = neighbors.deleteAllNeighbors(dutInTest);
		if(!action){
			report.report("Failed to delete neighbors",Reporter.WARNING);
			reason = "Failed to delete neighbors";
		}else{
			report.report("Deleted neighbors successfully");
		}
		try {
			neighbors.deleteAll3rdParty();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			report.report("Exception while deleting 3rd party EnodeBs",Reporter.WARNING);
		}
		GeneralUtils.unSafeSleep(5*1000);
		report.reportHtml("enb show ngh for "+dutInTest.getName(), dutInTest.lteCli("enb show ngh"), true);
		report.reportHtml("db get nghlist for "+dutInTest.getName(), dutInTest.lteCli("db get nghlist"), true);
		
		report.reportHtml("enb show ngh for "+dut2.getName(), dut2.lteCli("enb show ngh"), true);
		report.reportHtml("db get nghlist for "+dut2.getName(), dut2.lteCli("db get nghlist"), true);
		
		if (interEarfcn!=-1) {
			middleEarfcn = (earfcn + interEarfcn)/2;		
		}else{
			middleEarfcn = earfcn+1;
		}
		GeneralUtils.stopLevel();
		return true;
	}
	
	/**
	 * @param son - true: son enabled, false: son disabled
	 * @param anr - 0: Disabled, 1: HO, 2: Periodic
	 * @param dicic - true: enabled, false: disabled
	 * @param tbg - true: TBG enabled, false: TBG disabled
	 * @param nbr - number of inter neighbors (if inter are configured, 1 intra is configured as well)
	 * @param threshold - null: doesn't change attenuation, Over_a1: change att until UE's RSRP0 is over A1 threshold,
	 * 					  Below_A2: change att until UE's RSRP0 is below A2 threshold,	
	 * @param waitForSteady - true: wait for steady state after ue connects, false: don't wait for steady state
	 * @param enhance - true: enable enhance gap in TBG, false: disable enhance gap in TBG
	 * @param otdoa - active with mib and start measuring with simulate cli command
	 * @throws Exception
	 */
	//anr - 0 Disabled, 1 HO, 2 Periodic
	private void genericTest(boolean son, int anr, boolean dicic, boolean tbg, int nbr, Threshold threshold, boolean waitForSteady, boolean enhance,boolean otdoa) {
		report.report("Main EnodeB in test: "+dutInTest.getName());

		GeneralUtils.startLevel("Clone and set son profile");
		SonParameters sonParams = new SonParameters();
		setCsonState(sonParams, son);
		setAnrState(sonParams, anr, false);
		setDicicState(sonParams, dicic);
		boolean action = changeSonProfile(sonParams);
		
		if(!action) {
			report.report("Failed to clone and set son profile", Reporter.FAIL);
			reason = "Failed to clone and set son profile";
			GeneralUtils.stopLevel();
			return;
		}else{
			report.report("Clone and set son profile passed successfully");
		}
		GeneralUtils.stopLevel();
		
		report.reportHtml("db get anrcfg", dutInTest.lteCli("db get anrcfg"), true);
		
		GeneralUtils.startLevel("Clone and set mobility profile");
		if(tbg){
			if(enhance){
				action = setTBGEnable(true);				
			}else{
				action = setTBGEnable(false);				
			}
		}else{
			action = setTBGDisable();
		}
		if(!action){
			report.report("Failed to clone and set mobility profile", Reporter.FAIL);
			reason = "Failed to clone and set mobility profile";
			GeneralUtils.stopLevel();
			return;
		}else{
			report.report("Clone and set mobility profile passed successfully");
		}
		GeneralUtils.stopLevel();
		
		report.reportHtml("db get Sr15MeasReportConfig", dutInTest.lteCli("db get Sr15MeasReportConfig"), true);
		
		GeneralUtils.unSafeSleep(10*1000);
		if(son){
			if(!checkIfEsonServerConfigured()){
				GeneralUtils.startLevel("Eson server not configured. Configure server (clone and set network profile)");
				NetworkParameters network = new NetworkParameters();
				configureNetworkWithEsonServer(network);
				if (!changeNetworkProfile(network)) {
					report.report("Failed to clone and set network profile", Reporter.FAIL);
					reason = "Failed to clone and set network profile";
					GeneralUtils.stopLevel();
					return;
				} else {
					report.report("Configuration done");
					flagNetwork = true;
					needsToReboot = true;
				}
				GeneralUtils.stopLevel();
			}
			
			GeneralUtils.startLevel("Check if EnodeB is connected to eson server");
			if(eson.isDutConnectedToEsonServer(esonServer, dutInTest)){
				report.report("Enodeb "+dutInTest.getName()+" is connected to eson server");
			}else{
				GeneralUtils.startLevel("EnodeB was not connected. Reboot enodeB");
				dutInTest.reboot();
				dutInTest.waitForAllRunningAndInService(EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
				report.report("EnodeB rebooted");
				GeneralUtils.stopLevel();
				if(eson.isDutConnectedToEsonServer(esonServer, dutInTest)){
					report.report("Enodeb "+dutInTest.getName()+" is connected to eson server");
				}else{
					report.report("Enodeb "+dutInTest.getName()+" failed to connect to eson server",Reporter.FAIL);
					GeneralUtils.stopLevel();
					reason = "Enodeb "+dutInTest.getName()+" failed to connect to eson server";
					return;
				}
			}
			GeneralUtils.stopLevel();
		}
		
		if(nbr != 0){
			GeneralUtils.startLevel("Add neighbors");
			if (!addNeighbors(nbr)) {
				report.report("Failed to add neighbors", Reporter.FAIL);
				reason = "Failed to add neighbors";
				GeneralUtils.stopLevel();
				return;
			}else{
				report.report("Succeeded to add neighbors");
			}
			GeneralUtils.stopLevel();
		}
		
		if(!ueInTestConnected()){
			return;
		}
		
		dm.setUeIP(ueInTest.getLanIpAddress());
		dm.setPORT(ueInTest.getDMToolPort());
		try{
			dm.init();		
		}catch(Exception e){
			e.printStackTrace();
		}
		
		if(otdoa){
			report.report("Enable OTDOA");
			enableOTDOA();
			
			report.report("Enable debug flags - EnablePeriodUeShowLinkTable value to true to get ue rnti");
			String cliAnswer = dutInTest.lteCli("db set debugFlags [*] EnablePeriodUeShowLinkTable=1");
				GeneralUtils.printToConsole("result from db set cli : "+cliAnswer);
			
			report.report("Getting Single UE RNTI");
			getSingleUERNTI();
			if(rnti.equals("defaultValue")){
				report.report("Did not get rnti value from SNMP - check debugFlags [*] EnablePeriodUeShowLinkTable value",Reporter.FAIL);
				return;
			}
			
			if(sendOTDOACommandTest){
				cliAnswer = dutInTest.lteCli(otdoaCommand());
				GeneralUtils.printToConsole(otdoaCommand()+ " command Answer: \n" +cliAnswer);
			}
		}

		if(waitForSteady){
			GeneralUtils.startLevel("Waiting for anr steady state");
			if(!checkForSteady()){
				report.report("EnodeB failed to reach anr steady mode", Reporter.FAIL);
				reason = "EnodeB failed to reach anr steady mode";
				dm.close();
				GeneralUtils.stopLevel();
				return;
			}else{
				report.report("EnodeB reached anr steady mode");
			}
			GeneralUtils.stopLevel();
		}

		if(threshold != null){
			GeneralUtils.startLevel("Changing attenuation in order to reach wanted RSRP0 in UE");
			if(threshold == Threshold.Below_A2){
				if(!changeAttOverA1()){
					failRF();
					return;
				}
				GeneralUtils.unSafeSleep(5 * 1000);
				if(!changeAttBelowA2()){
					failRF();
					return;
				}
			}else{
				if(!changeAttBelowA2()){
					failRF();
					return;
				}
				GeneralUtils.unSafeSleep(5 * 1000);
				if(!changeAttOverA1()){
					failRF();
					return;
				}
			}
			GeneralUtils.unSafeSleep(5 * 1000);
			GeneralUtils.stopLevel();
			report.report("Reached wanted RF conditions");
		}
		
		report.reportHtml("ue show link", dutInTest.lteCli("ue show link"), true);
		report.report("Getting measurement tables from UE");
		getTablesFromUEToLocalTable(dm);
		
		dm.close();
		dm = null;
		
		GeneralUtils.startLevel("Tables received from UE");
		printTablesToReport(measTables.getObjectTable(), measTables.getReportTable(), measTables.getCfgTable());
		GeneralUtils.stopLevel();

		GeneralUtils.startLevel("Expected tables");
		printTablesToReport(expectedTables.getObjectTable(), expectedTables.getReportTable(), expectedTables.getCfgTable());
		GeneralUtils.stopLevel();

		GeneralUtils.startLevel("Results");
		boolean flagTables = checkTables();
		GeneralUtils.stopLevel();
		if(flagTables){
			report.report("Tables MeasurementCfg are equal");
		}else{
			report.report("Tables MeasurementCfg are not equal", Reporter.FAIL);
			reason = "Tables MeasurementCfg are not equal";
		}
	}

	//TEST 1
	@Test
	@TestProperties(name = "SonDisable_TBGDisable_AnrDisable_NoNbr", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
		"IsTestWasSuccessful"})
	public void test1_sonDisableTBGDisableAnrDisableNoNbr(){	
		reportConfiguration("Disabled","Disabled","Disabled","None",null,null);
		
		if(!preTest(false)){
			return;
		}
		createExpectedTablesTest1();
		genericTest(false, 0, false, false, 0, null, false, false, false);
	}

	//TEST 2
	@Test
	@TestProperties(name = "SonDisable_TBGDisable_AnrDisable_2Inter1Intra", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
		"IsTestWasSuccessful"})
	public void test2_sonDisableTBGDisableAnrDisable2Inter1Intra(){	
		reportConfiguration("Disabled","Disabled","Disabled","1 intra, 2 inter",null,null);
		if(!preTest(false)){
			return;
		}
		createExpectedTablesTest2();
		genericTest(false, 0, false, false, 2, null, false, false, false);
	}

	//TEST 3
	@Test
	@TestProperties(name = "SonDisable_TBGDisable_AnrDisable_1Intra", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful"})
	public void test3_sonDisableTBGDisableAnrDisable1Intra() {
		reportConfiguration("Disabled","Disabled","Disabled","1 intra",null,null);
		if(!preTest(false)){
			return;
		}
		report.report("Main EnodeB in test: "+dutInTest.getName());
		GeneralUtils.startLevel("Clone and set son profile");

		SonParameters sonParams = new SonParameters();
		setCsonState(sonParams, false);
		setAnrState(sonParams, 0, false);
		setDicicState(sonParams, false);
		boolean action = changeSonProfile(sonParams);

		if(!action){
			report.report("Failed to clone and set son profile", Reporter.FAIL);
			reason = "Failed to clone and set son profile";
			GeneralUtils.stopLevel();
			return;
		}else{
			report.report("Clone and set son profile passed successfully");
		}
		GeneralUtils.stopLevel();
		
		report.reportHtml("db get anrcfg", dutInTest.lteCli("db get anrcfg"), true);
		
		GeneralUtils.startLevel("Clone and set mobility profile");
		if(!setTBGDisable()){
			report.report("Failed to clone and set mobility profile",Reporter.FAIL);
			reason = "Failed to clone and set mobility profile";
			GeneralUtils.stopLevel();
			return;
		}else{
			report.report("Clone and set mobility profile passed successfully");
		}
		GeneralUtils.stopLevel();
		GeneralUtils.unSafeSleep(10*1000);
		GeneralUtils.startLevel("Add neighbors");
		if(!addNeighbors(1)){
			report.report("Failed to add neighbors",Reporter.FAIL);
			reason = "Failed to add neighbors";
			GeneralUtils.stopLevel();
			return;
		}else{
			report.report("Succeeded to add neighbors");
		}
		GeneralUtils.stopLevel();
		
		if(!ueInTestConnected()){
			return;
		}
		
		dm.setUeIP(ueInTest.getLanIpAddress());
		dm.setPORT(ueInTest.getDMToolPort());

		try{
			dm.init();			
		}catch(Exception e){
			e.printStackTrace();
		}
				
		report.report("Delete neighbor from eNodeB");
		try{
			action = neighbors.deleteNeighbor(dutInTest, virtualNeighborsList.get(0));
			if(!action){
				report.report("Failed to delete neighbor",Reporter.WARNING);
			}else{
				report.report("Succeeded to delete neighbor");
			}
		}catch(Exception e){
			e.printStackTrace();
			report.report("Exception while trying to delete neighbor",Reporter.WARNING);
		}
		
		GeneralUtils.unSafeSleep(10*1000);
				
		report.reportHtml("ue show link", dutInTest.lteCli("ue show link"), true);
		report.report("Getting measurement tables from UE");
		getTablesFromUEToLocalTable(dm);
		
		dm.close();
		dm = null;
		
		GeneralUtils.startLevel("Tables received from UE");
		printTablesToReport(measTables.getObjectTable(), measTables.getReportTable(), measTables.getCfgTable());
		GeneralUtils.stopLevel();
		
		createExpectedTablesTest3();
		GeneralUtils.startLevel("Expected tables");
		printTablesToReport(expectedTables.getObjectTable(), expectedTables.getReportTable(), expectedTables.getCfgTable());
		GeneralUtils.stopLevel();
		
		GeneralUtils.startLevel("Results");
		boolean flagTables = checkTables();
		GeneralUtils.stopLevel();
		if(flagTables){
			report.report("Tables MeasurementCfg are equal");
		}else{
			report.report("Tables MeasurementCfg are not equal",Reporter.FAIL);
			reason = "Tables MeasurementCfg are not equal";
		}
	}
	
	//TEST 4
	@Test
	@TestProperties(name = "SonDisable_TBGDisable_AnrPeriodic_NoNbr", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful"})
	public void test4_sonDisableTBGDisableAnrPeriodicNoNbr() {
		reportConfiguration("Disabled","Periodic","Disabled","None",null,null);
		if(!preTest(false)){
			return;
		}
		createExpectedTablesTest4();
		genericTest(false, 2, false, false, 0, null, false, false, false);
	}
	
	//TEST 5
	@Test
	@TestProperties(name = "SonDisable_TBGEnable_AnrDisable_1Intra2Inter", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful"})
	public void test5_sonDisableTBGEnableAnrDisable1Intra2Inter() {
		reportConfiguration("Disabled","Disabled","Enable","1 intra, 2 inter",null,null);
		if(!preTest(false)){
			return;
		}
		createExpectedTablesTest5();
		genericTest(false, 0, false, true, 2, null, false, false, false);
	}

	//TEST 6
	@Test
	@TestProperties(name = "SonEnable_TBGEnable_AnrDisable_2Inter1Intra", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful"})
	public void test6_sonEnableTBGEnableAnrDisable2Inter1Intra() {
		reportConfiguration("Enabled","Disabled","Enabled","1 intra, 2 inter",null,null);
		if(!preTest(false)){
			return;
		}
		createExpectedTablesTest6();
		genericTest(true, 0, false, true, 2, null, false, false, false);
	}
	
	//TEST 7
	@Test
	@TestProperties(name = "SonEnable_TBGEnable_AnrPeriodic_NoNbr", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful"})
	public void test7_sonEnableTBGEnableAnrPeriodicNoNbr() {
		reportConfiguration("Enabled","Periodic","Enabled","None",null,null);
		if(!preTest(false)){
			return;
		}
		createExpectedTablesTest7();
		genericTest(true, 2, false, true, 0, null, false, false, false);
	}
	
	//TEST 8
	@Test
	@TestProperties(name = "SonDisable_TBGDisable_AnrHO_NoNbr", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful"})
	public void test8_sonDisableTBGDisableAnrHONoNbr() {
		reportConfiguration("Disabled","Handover","Disabled","None",null,null);
		if(!preTest(false)){
			return;
		}
		createExpectedTablesTest8();
		genericTest(false, 1, false, false, 0, null, false, false, false);
	}
	
	//TEST 9
	@Test
	@TestProperties(name = "SonDisable_TBGEnable_AnrHO_NoNbr", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful"})
	public void test9_sonDisableTBGEnableAnrHONoNbr() {
		reportConfiguration("Disabled","Handover","Enabled","None",null,null);
		if(!preTest(false)){
			return;
		}
		createExpectedTablesTest9();
		genericTest(false, 1, false, true, 0, null, false, false, false);
	}
	
	//TEST 10
	@Test
	@TestProperties(name = "SonEnable_TBGEnable_AnrHO_NoNbr", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful"})
	public void test10_sonEnableTBGEnableAnrHONoNbr() {
		reportConfiguration("Enabled","Handover","Enabled","None",null,null);

		if(!preTest(false)){
			return;
		}
		createExpectedTablesTest10();
		genericTest(true, 1, false, true, 0, null, false, false, false);
	}

	//TEST 11
	@Test
	@TestProperties(name = "SonEnable_TBGEnable_AnrHO_1Inter1intra", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful"})
	public void test11_sonEnableTBGEnableAnrHONoNbr1Inter1intra() {
		reportConfiguration("Enabled","Handover","Enabled","1 intra, 1 inter",null,null);

		if(!preTest(false)){
			return;
		}
		createExpectedTablesTest11();
		genericTest(true, 1, false, true, 1, null, false, false, false);
	}

	//TEST 12
	@Test
	@TestProperties(name = "SonDisable_TBGEnable_AnrDisable_1Intra1Inter_RFbelowA2", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful"})
	public void test12_sonDisableTBGEnableAnrDisable1Intra1InterRFbelowA2() {
		reportConfiguration("Disabled","Disabled","Enabled","1 intra, 1 inter","Below A2",null);

		if(!preTest(false)){
			return;
		}
		createExpectedTablesTest12();
		genericTest(false, 0, false, true, 1, Threshold.Below_A2, false, false, false);
	}

	//TEST 13
	@Test
	@TestProperties(name = "SonDisable_TBGEnable_AnrDisable_1Intra1Inter_RFoverA1", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful"})
	public void test13_sonDisableTBGEnableAnrDisable1Intra1InterRFoverA1() {
		reportConfiguration("Disabled","Disabled","Enabled","1 intra, 1 inter","Over A1",null);

		if(!preTest(false)){
			return;
		}
		createExpectedTablesTest13();
		genericTest(false, 0, false, true, 1, Threshold.Over_A1, false, false, false);
	}
	
	//TEST 14
	@Test
	@TestProperties(name = "SonEnable_TBGEnable_AnrDisable_1Inter1Intra_RFbelowA2", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful"})
	public void test14_sonEnableTBGEnableAnrDisable1Inter1IntraRFbelowA2() {
		reportConfiguration("Enabled","Disabled","Enabled","1 intra, 1 inter","Below A2",null);

		if(!preTest(false)){
			return;
		}
		createExpectedTablesTest14();
		genericTest(true, 0, false, true, 1, Threshold.Below_A2, false, false, false);
	}

	//TEST 15
	@Test
	@TestProperties(name = "SonEnable_TBGEnable_AnrDisable_1Inter1Intra_RFoverA1", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful"})
	public void test15_sonEnableTBGEnableAnrDisable1Inter1IntraRFoverA1() {
		reportConfiguration("Enabled","Disabled","Enabled","1 intra, 1 inter","Over A1",null);

		if(!preTest(false)){
			return;
		}
		createExpectedTablesTest15();
		genericTest(true, 0, false, true, 1, Threshold.Over_A1, false, false, false);
	}

	//TEST 16
	@Test
	@TestProperties(name = "SonEnable_TBGEnable_AnrPeriodicSteady_NoNbr", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful"})
	public void test16_sonEnableTBGEnableAnrPeriodicSteadyNoNbr() {
		reportConfiguration("Enabled","Periodic, steady mode","Enabled","None",null,null);

		if(!preTest(false)){
			return;
		}
		createExpectedTablesTest16();
		genericTest(true, 2, false, true, 0, null, true, false, false);
	}
	
	//TEST 17
	@Test
	@TestProperties(name = "SonEnable_TBGEnable_AnrPeriodicSteady_NoNbr_RFbelowA2", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful"})
	public void test17_sonEnableTBGEnableAnrPeriodicSteadyNoNbrRFbelowA2() {
		reportConfiguration("Enabled","Periodic, steady mode","Enabled","None","Below A2",null);
		if(!preTest(false)){
			return;
		}
		createExpectedTablesTest17();
		genericTest(true, 2, false, true, 0, Threshold.Below_A2, true, false, false);
	}
	
	//TEST 18
	@Test
	@TestProperties(name = "SonEnable_TBGEnable_AnrPeriodicSteady_NoNbr_RFoverA1", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful"})
	public void test18_sonEnableTBGEnableAnrPeriodicSteadyNoNbrRFoverA1() {
		reportConfiguration("Enabled","Periodic, steady mode","Enabled","None","Over A1",null);

		if(!preTest(false)){
			return;
		}
		createExpectedTablesTest18();
		genericTest(true, 2, false, true, 0, Threshold.Over_A1, true, false, false);
	}
	
	private boolean genericTestMultipleUEs(boolean son, boolean dicic, boolean allUes, boolean cdrx) {
		originalRatio = dutInTest.getAnrLightRatio();
		originalMinimum = dutInTest.getAnrLightMinUes();
		
		
		if(originalRatio!=0){
			if(!dutInTest.setAnrLightRatio(0)){
				report.report("Failed to change anr light ratio in eNodeB to 0",Reporter.FAIL);
				reason = "Failed to change anr light ratio in eNodeB to 0";
				return false;
			}else{
				report.report("Succeeded to change anr light ratio in eNodeB to 0");
			}			
		}
		if(originalMinimum!=1){
			if(!dutInTest.setAnrLightMinUes(1)){
				report.report("Failed to change anr light min ues in eNodeB to 1",Reporter.FAIL);
				reason = "Failed to change anr light min ues in eNodeB to 1";
				return false;
			}else{
				report.report("Succeeded to change anr light min ues in eNodeB to 1");
			}			
		}
		
		GeneralUtils.startLevel("Clone and set son profile");
		
		SonParameters sonParams = new SonParameters();
		setCsonState(sonParams, son);
		setAnrState(sonParams, 2, false);
		setDicicState(sonParams, dicic);
		boolean action = changeSonProfile(sonParams);

		if(!action) {
			report.report("Failed to clone and set son profile", Reporter.FAIL);
			reason = "Failed to clone and set son profile";
			GeneralUtils.stopLevel();
			return false;
		}else{
			report.report("Clone and set son profile passed successfully");
		}
		GeneralUtils.stopLevel();
	
		report.reportHtml("db get anrcfg", dutInTest.lteCli("db get anrcfg"), true);
		
		GeneralUtils.startLevel("Clone and set mobility profile");
		if(!setTBGEnable(false)){
			report.report("Failed to clone and set mobility profile", Reporter.FAIL);
			reason = "Failed to clone and set mobility profile";
			GeneralUtils.stopLevel();
			return false;
		}else{
			report.report("Clone and set mobility profile passed successfully");
		}
		GeneralUtils.stopLevel();
		report.reportHtml("db get Sr15MeasReportConfig", dutInTest.lteCli("db get Sr15MeasReportConfig"), true);

		GeneralUtils.unSafeSleep(10*1000);
		
		if(son){
			if(!checkIfEsonServerConfigured()){
				GeneralUtils.startLevel("Eson server not configured. Configure server");
				NetworkParameters params = new NetworkParameters();
				configureNetworkWithEsonServer(params);
				if (!changeNetworkProfile(params)) {
					report.report("Failed to clone and set network profile", Reporter.FAIL);
					reason = "Failed to clone and set network profile";
					GeneralUtils.stopLevel();
					return false;
				} else {
					report.report("Configuration done");
					flagNetwork = true;
					needsToReboot = true;
				}
				GeneralUtils.stopLevel();
			}
			
			GeneralUtils.startLevel("Check if EnodeB is connected to eson server");
			if(eson.isDutConnectedToEsonServer(esonServer, dutInTest)){
				report.report("Enodeb "+dutInTest.getName()+" is connected to eson server");
			}else{
				GeneralUtils.startLevel("EnodeB was not connected. Reboot enodeB");
				dutInTest.reboot();
				dutInTest.waitForAllRunningAndInService(EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
				report.report("EnodeB rebooted");
				GeneralUtils.stopLevel();
				if(eson.isDutConnectedToEsonServer(esonServer, dutInTest)){
					report.report("Enodeb "+dutInTest.getName()+" is connected to eson server");
				}else{
					report.report("Enodeb "+dutInTest.getName()+" failed to connect to eson server",Reporter.FAIL);
					GeneralUtils.stopLevel();
					reason = "Enodeb "+dutInTest.getName()+" failed to connect to eson server";
					return false;
				}
			}
			GeneralUtils.stopLevel();
		}
		
		if(cdrx){
			GeneralUtils.startLevel("Configure network profile with cdrx enabled");
			NetworkParameters params = new NetworkParameters();
			configureNetworkWithCdrx(params);
			if (!changeNetworkProfile(params)) {
				report.report("Failed to clone and set network profile", Reporter.FAIL);
				reason = "Failed to clone and set network profile";
				GeneralUtils.stopLevel();
				return false;
			} else {
				report.report("Configuration done");
				flagNetwork = true;
			}
			GeneralUtils.stopLevel();
		}
		
		if(allUes){
			GeneralUtils.startLevel("Start dynamic UEs");
			for(UE ue:dynamicArrayUEs){
				ue.start();
			}			
		}else{
			GeneralUtils.startLevel("Start UEs except UE in test");
			for (UE ue : dynamicArrayUEs) {
				if (!ue.getLanIpAddress().equals(ueInTest.getLanIpAddress())) {
					ue.start();
				}
			}
		}
		GeneralUtils.unSafeSleep(5*1000);
		GeneralUtils.stopLevel();
		
		GeneralUtils.startLevel("Waiting for anr steady state");
		if(!checkForSteady()){
			report.report("EnodeB failed to reach anr steady mode", Reporter.FAIL);
			reason = "EnodeB failed to reach anr steady mode";
			GeneralUtils.stopLevel();
			return false;
		}else{
			report.report("EnodeB reached anr steady mode");
		}
		GeneralUtils.stopLevel();
		
		EnodeB nbr = otherEnb.get(0);
		GeneralUtils.startLevel("Setting neighbor "+nbr.getNetspanName()+" in service");		
		peripheralsConfig.changeEnbState(nbr, EnbStates.IN_SERVICE);
		if(!nbr.waitForAllRunningAndInService(60*1000)){
			report.report("EnodeB "+nbr.getNetspanName()+" failed to reach all running and in service",Reporter.FAIL);
			reason = "EnodeB "+nbr.getNetspanName()+" failed to reach all running and in service";
			GeneralUtils.stopLevel();
			return false;
		}else{
			report.report("EnodeB "+nbr.getNetspanName()+" reached all running and in service");
		}
		GeneralUtils.stopLevel();
		
		GeneralUtils.startLevel("Waiting for anr light state");
		if(!checkForLight()){
			report.report("EnodeB failed to reach anr light mode", Reporter.FAIL);
			reason = "EnodeB failed to reach anr light mode";
			GeneralUtils.stopLevel();
			return false;
		}else{
			report.report("EnodeB reached anr light mode");
		}
		GeneralUtils.stopLevel();
		peripheralsConfig.changeEnbState(otherEnb.get(0), EnbStates.OUT_OF_SERVICE);
		return true;
	}
	
	//TEST 19
	@Test
	@TestProperties(name = "SonEnable_TBGEnable_AnrPeriodicLight_NoNbr_ChosenUE", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful"})
	public void test19_sonEnableTBGEnableAnrPeriodicLightNoNbrChosenUE() {
		ArrayList<String> ues = new ArrayList<String>();
		int counter=0;
		reportConfiguration("Enabled","Periodic, light mode","Enabled","None",null,null);
		if(!preTest(true)){
			return;
		}
		createExpectedTablesTest19();
		if(!genericTestMultipleUEs(true,false,true, true)){
			return;
		}
		
		for(UE ue:dynamicArrayUEs){
			DMtool tool = new DMtool();
			tool.setUeIP(ue.getLanIpAddress());
			tool.setPORT(ue.getDMToolPort());
			try{
				tool.init();			
			}catch(Exception e){
				e.printStackTrace();
			}
			
			getTablesFromUEToLocalTable(tool);
			
			GeneralUtils.startLevel("Tables from ue "+ue.getLanIpAddress());
			printTablesToReport(measTables.getObjectTable(), measTables.getReportTable(), measTables.getCfgTable());
			GeneralUtils.stopLevel();
			if(checkTablesMultipleUEs()){
				counter++;
				ues.add(ue.getLanIpAddress());
				try{
					report.report("UE "+ue.getLanIpAddress()+" is chosen. Verify cdrx is not configured");
					report.reportHtml("getdrxConfig", tool.cli("getdrxConfig"), true);
					boolean drx = tool.isCdrxConfigured();
					if(!drx){
						report.report("UE "+ue.getLanIpAddress()+" doesn't have cdrx configured"); 
					}else{
						report.report("UE "+ue.getLanIpAddress()+" has cdrx configured",Reporter.FAIL);
						reason = "UE "+ue.getLanIpAddress()+" has cdrx configured";
					}
				}catch(Exception e){
					e.printStackTrace();
					report.report("Failed to get information from DM Tool", Reporter.FAIL);
				}
			}
			tool.close();	
			tool=null;
			createExpectedTablesTest19();
		}
				
		GeneralUtils.startLevel("Expected tables");
		printTablesToReport(expectedTables.getObjectTable(), expectedTables.getReportTable(), expectedTables.getCfgTable());
		GeneralUtils.stopLevel();
		
		GeneralUtils.startLevel("UEs that received expected tables");
		report.report(ues.toString());
		GeneralUtils.stopLevel();
		
		report.report("Checking tables of chosen UEs");
		if(counter == 1){
			report.report("Number of UEs that received the expected tables is 1 - as expected");
		}else{
			report.report("Number of UEs that received the expected tables is not 1 - not as expected",Reporter.FAIL);
			reason = "Number of UEs that received the expected tables is not 1 - not as expected";
		}
	}

	//TEST 20
	//@Test
	@TestProperties(name = "SonEnable_TBGEnable_AnrPeriodicLight_NoNbr_NoChosenUE", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful"})
	public void test20_sonEnableTBGEnableAnrPeriodicLightNoNbrNoChosenUE() {
		ArrayList<String> ues = new ArrayList<String>();
		int counter = 0;
		reportConfiguration("Enabled", "Periodic, light mode", "Enabled", "None", null,null);

		if (!preTest(true)) {
			return;
		}
		createExpectedTablesTest20();
		if(!genericTestMultipleUEs(true,false,true,false)){
			return;
		}

		for(UE ue:dynamicArrayUEs){
			DMtool tool = new DMtool();
			tool.setUeIP(ue.getLanIpAddress());
			tool.setPORT(ueInTest.getDMToolPort());
			try{
				tool.init();			
			}catch(Exception e){
				e.printStackTrace();
			}
			
			getTablesFromUEToLocalTable(tool);
			tool.close();	
			tool=null;
			
			GeneralUtils.startLevel("Tables from ue "+ue.getLanIpAddress());
			printTablesToReport(measTables.getObjectTable(), measTables.getReportTable(), measTables.getCfgTable());
			if(checkTablesMultipleUEs()){
				counter++;
				ues.add(ue.getLanIpAddress());
			}
			GeneralUtils.stopLevel();
			createExpectedTablesTest20();
		}
		
		GeneralUtils.startLevel("Expected tables");
		printTablesToReport(expectedTables.getObjectTable(), expectedTables.getReportTable(), expectedTables.getCfgTable());
		GeneralUtils.stopLevel();
		
		GeneralUtils.startLevel("UEs that received expected tables");
		report.report(ues.toString());
		GeneralUtils.stopLevel();
		
		report.report("Checking tables of non chosen UEs");
		if (counter == dynamicArrayUEs.size()-1) {
			report.report("Number of UEs that received the expected tables is "+counter+" - as expected");
		} else {
			report.report("Number of UEs that received the expected tables is not "+(dynamicArrayUEs.size()-1)+" - not as expected",Reporter.FAIL);
			reason = "Number of UEs that received the expected tables is not "+(dynamicArrayUEs.size()-1)+" - not as expected";
		}
	}
	
	//TEST 21
	@Test
	@TestProperties(name = "SonEnable_TBGEnable_AnrPeriodicLight_NoNbr_NoChosenUE_RFA2", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful"})
	public void test21_sonEnableTBGEnableAnrPeriodicLightNoNbrNoChosenUERFA2() {
		ArrayList<String> ues = new ArrayList<String>();
		int counter = 0;
		reportConfiguration("Enabled", "Periodic, light mode", "Enabled", "None", "Below A2",null);

		if (!preTest(true)) {
			return;
		}
		createExpectedTablesTest21();
		if(!genericTestMultipleUEs(true,false,true,false)){
			return;
		}
				
		GeneralUtils.startLevel("Changing attenuation in order to reach wanted RSRP0 in UEs");
		if (!changeAttOverA1AllUEs(dynamicArrayUEs)) {
			failRFWithoutDmTool();
			return;
		}
		GeneralUtils.unSafeSleep(5 * 1000);
		if (!changeAttBelowA2AllUEs(dynamicArrayUEs)) {
			failRFWithoutDmTool();
			return;
		}
		GeneralUtils.unSafeSleep(5 * 1000);
		GeneralUtils.stopLevel();
		report.report("Reached wanted RF conditions");

		for(UE ue:dynamicArrayUEs){
			DMtool tool = new DMtool();
			tool.setUeIP(ue.getLanIpAddress());
			tool.setPORT(ue.getDMToolPort());
			try{
				tool.init();			
			}catch(Exception e){
				e.printStackTrace();
			}
			
			getTablesFromUEToLocalTable(tool);
			tool.close();
			tool=null;
			
			GeneralUtils.startLevel("Tables from ue "+ue.getLanIpAddress());
			printTablesToReport(measTables.getObjectTable(), measTables.getReportTable(), measTables.getCfgTable());
			if(checkTablesMultipleUEs()){
				counter++;
				ues.add(ue.getLanIpAddress());
			}
			GeneralUtils.stopLevel();
			createExpectedTablesTest21();
		}

		GeneralUtils.startLevel("Expected tables");
		printTablesToReport(expectedTables.getObjectTable(), expectedTables.getReportTable(), expectedTables.getCfgTable());
		GeneralUtils.stopLevel();
		
		GeneralUtils.startLevel("UEs that received expected tables");
		report.report(ues.toString());
		GeneralUtils.stopLevel();
		
		report.report("Checking tables of non chosen UEs");
		if (counter == dynamicArrayUEs.size()-1) {
			report.report("Number of UEs that received the expected tables is "+counter+" - as expected");
		} else {
			report.report("Number of UEs that received the expected tables is not "+(dynamicArrayUEs.size()-1)+" - not as expected",Reporter.FAIL);
			reason = "Number of UEs that received the expected tables is not "+(dynamicArrayUEs.size()-1)+" - not as expected";
		}
	}
	
	// TEST 22
	@Test
	@TestProperties(name = "SonEnable_TBGEnable_AnrPeriodicLight_NoNbr_NoChosenUE_RFA1", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful"})
	public void test22_sonEnableTBGEnableAnrPeriodicLightNoNbrNoChosenUERFA1() {
		ArrayList<String> ues = new ArrayList<String>();
		int counter = 0;
		reportConfiguration("Enabled", "Periodic, light mode", "Enabled", "None", "Over A1",null);

		if (!preTest(true)) {
			return;
		}
		createExpectedTablesTest22();
		if(!genericTestMultipleUEs(true,false,true,false)){
			return;
		}
		
		GeneralUtils.startLevel("Changing attenuation in order to reach wanted RSRP0 in UEs");
		if (!changeAttBelowA2AllUEs(dynamicArrayUEs)) {
			failRFWithoutDmTool();
			return;
		}
		GeneralUtils.unSafeSleep(5 * 1000);
		if (!changeAttOverA1AllUEs(dynamicArrayUEs)) {
			failRFWithoutDmTool();
			return;
		}
		GeneralUtils.unSafeSleep(5 * 1000);
		GeneralUtils.stopLevel();
		report.report("Reached wanted RF conditions");

		for(UE ue:dynamicArrayUEs){
			DMtool tool = new DMtool();
			tool.setUeIP(ue.getLanIpAddress());
			tool.setPORT(ue.getDMToolPort());
			try{
				tool.init();			
			}catch(Exception e){
				e.printStackTrace();
			}
			
			getTablesFromUEToLocalTable(tool);
			tool.close();	
			tool=null;
			
			GeneralUtils.startLevel("Tables from ue "+ue.getLanIpAddress());
			printTablesToReport(measTables.getObjectTable(), measTables.getReportTable(), measTables.getCfgTable());
			if(checkTablesMultipleUEs()){
				counter++;
				ues.add(ue.getLanIpAddress());
			}
			GeneralUtils.stopLevel();
			createExpectedTablesTest22();
		}

		GeneralUtils.startLevel("Expected tables");
		printTablesToReport(expectedTables.getObjectTable(), expectedTables.getReportTable(), expectedTables.getCfgTable());
		GeneralUtils.stopLevel();
		
		GeneralUtils.startLevel("UEs that received expected tables");
		report.report(ues.toString());
		GeneralUtils.stopLevel();
		
		report.report("Checking tables of non chosen UEs");
		if (counter == dynamicArrayUEs.size()-1) {
			report.report("Number of UEs that received the expected tables is "+counter+" - as expected");
		} else {
			report.report("Number of UEs that received the expected tables is not "+(dynamicArrayUEs.size()-1)+" - not as expected",Reporter.FAIL);
			reason = "Number of UEs that received the expected tables is not "+(dynamicArrayUEs.size()-1)+" - not as expected";
		}
	}
	
	//TEST 23
	@Test
	@TestProperties(name = "SonDisable_TBGEnableDual_AnrDisabled_1Inter1Intra", returnParam = {
		"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful"})
	public void test23_sonDisableTBGEnableDualAnrDisable1Inter1Intra() {
		reportConfiguration("Disabled","Disabled","Enabled, Dual","1 intra, 1 inter",null,null);
		if(!preTest(false)){
			return;
		}
		createExpectedTablesTest23();
		genericTest(false, 0, false, true, 1, null, false, true, false);
	}
	
	//TEST 24
	@Test
	@TestProperties(name = "SonDisable_TBGEnableDual_AnrDisabled_1Inter1Intra_RFoverA1", returnParam = {
		"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful"})
	public void test24_sonDisableTBGEnableDualAnrDisable1Inter1IntraRFoverA1() {
		reportConfiguration("Disabled","Disabled","Enabled, Dual","1 intra, 1 inter","Below A2",null);
		if(!preTest(false)){
			return;
		}
		createExpectedTablesTest24();
		genericTest(false, 0, false, true, 1, Threshold.Over_A1, false, true, false);
	}
	
	//TEST 25
	@Test
	@TestProperties(name = "SonDisable_TBGEnableDual_AnrDisabled_1Inter1Intra_RFBelowA2", returnParam = {
		"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful"})
	public void test25_sonDisableTBGEnableDualAnrDisable1Inter1IntraRFBelowA2() {
		reportConfiguration("Disabled","Disabled","Enabled, Dual","1 intra, 1 inter","Over A1",null);
		if(!preTest(false)){
			return;
		}
		createExpectedTablesTest25();
		genericTest(false, 0, false, true, 1, Threshold.Below_A2, false, true, false);
	}
	
	//TEST 26
	@Test
	@TestProperties(name = "SonEnable_TBGEnableDual_AnrPeriodicSteady_NoNbr", returnParam = {
		"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful"})
	public void test26_sonEnableTBGEnableDualAnrPeriodicSteadyNoNbr() {
		reportConfiguration("Enabled","Periodic, steady mode","Enabled, Dual","None",null,null);
		dutInTest.lteCli("logger threshold set process=rfMgr cli=0");
		dut2.lteCli("logger threshold set process=rfMgr cli=0");
		if(!preTest(false)){
			int logLevel = dutInTest.getSSHlogSession().getLogLevel();
			dutInTest.lteCli("logger threshold set process=rfMgr cli="+logLevel);
			dut2.lteCli("logger threshold set process=rfMgr cli="+logLevel);
			return;
		}
		createExpectedTablesTest26();
		genericTest(true, 2, false, true, 0, null, true, true, false);
		int logLevel = dutInTest.getSSHlogSession().getLogLevel();
		dutInTest.lteCli("logger threshold set process=rfMgr cli="+logLevel);
		dut2.lteCli("logger threshold set process=rfMgr cli="+logLevel);
	}
	
	//TEST 27
	@Test
	@TestProperties(name = "SonDisable_TBGEnable_AnrPeriodic_NoNbr_UEINSteady", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful"})
	public void test27_sonDisableTBGEnableAnrPeriodicNoNbrUEINSteady() {
		reportConfiguration("Disabled", "Periodic, steady mode", "Enabled", "None", null,null);
		if (!preTest(true)) {
			return;
		}
		createExpectedTablesTest27();
		report.report("Main EnodeB in test: "+dutInTest.getName());

		GeneralUtils.startLevel("Clone and set son profile");
		SonParameters sonParams = new SonParameters();
		setCsonState(sonParams, false);
		setAnrState(sonParams, 2, false);
		setDicicState(sonParams, false);
		boolean action = changeSonProfile(sonParams);

		if(!action){
			report.report("Failed to clone and set son profile", Reporter.FAIL);
			reason = "Failed to clone and set son profile";
			GeneralUtils.stopLevel();
			return;
		}else{
			report.report("Clone and set son profile passed successfully");
		}
		GeneralUtils.stopLevel();
		
		report.reportHtml("db get anrcfg", dutInTest.lteCli("db get anrcfg"), true);
		
		GeneralUtils.startLevel("Clone and set mobility profile");
		if(!setTBGEnable(false)){
			report.report("Failed to clone and set mobility profile", Reporter.FAIL);
			reason = "Failed to clone and set mobility profile";
			GeneralUtils.stopLevel();
			return;
		}else{
			report.report("Clone and set mobility profile passed successfully");
		}
		GeneralUtils.stopLevel();
		GeneralUtils.unSafeSleep(10*1000);
		GeneralUtils.startLevel("Start UEs except UE in test");
		for(UE ue:dynamicArrayUEs){
			if(!ue.getLanIpAddress().equals(ueInTest.getLanIpAddress())){
				ue.start();								
			}
		}
		GeneralUtils.unSafeSleep(10 * 1000);
		GeneralUtils.stopLevel();

		GeneralUtils.startLevel("Waiting for anr steady state");
		if(!checkForSteady()){
			report.report("EnodeB failed to reach anr steady mode", Reporter.FAIL);
			reason = "EnodeB failed to reach anr steady mode";
			GeneralUtils.stopLevel();
			return;
		}else{
			report.report("EnodeB reached anr steady mode");
		}
		GeneralUtils.stopLevel();
		
		if(!ueInTestConnected()){
			return;
		}
		
		dm.setUeIP(ueInTest.getLanIpAddress());
		dm.setPORT(ueInTest.getDMToolPort());

		try{
			dm.init();			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		report.report("Getting measurement tables from UE");
		getTablesFromUEToLocalTable(dm);
		dm.close();
		dm = null;
		
		GeneralUtils.startLevel("Tables received from UE");
		printTablesToReport(measTables.getObjectTable(), measTables.getReportTable(), measTables.getCfgTable());
		GeneralUtils.stopLevel();

		GeneralUtils.startLevel("Expected tables");
		printTablesToReport(expectedTables.getObjectTable(), expectedTables.getReportTable(), expectedTables.getCfgTable());
		GeneralUtils.stopLevel();

		GeneralUtils.startLevel("Results");
		boolean flagTables = checkTables();
		GeneralUtils.stopLevel();
		if(flagTables){
			report.report("Tables MeasurementCfg are equal");
		}else{
			report.report("Tables MeasurementCfg are not equal", Reporter.FAIL);
			reason = "Tables MeasurementCfg are not equal";
		}
	}
	
	//TEST 28
	@Test
	@TestProperties(name = "SonDisable_TBGEnable_AnrPeriodic_NoNbr_UEINLight", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful"})
	public void test28_sonDisableTBGEnableAnrPeriodicNoNbrUEINLight() {
		reportConfiguration("Disabled", "Periodic, light mode", "Enabled", "None", null,null);
		if (!preTest(true)) {
			return;
		}
		createExpectedTablesTest28();
		report.report("Main EnodeB in test: " + dutInTest.getName());

		if(!genericTestMultipleUEs(false,false,false,false)){
			return;
		}
		
		GeneralUtils.startLevel("Return to minimum attenuation (UE will enter above A1)");
		returnAttToMin();
		GeneralUtils.stopLevel();
		
		report.reportHtml("ue show link before turning on UE in test", dutInTest.lteCli("ue show link"), true);
		
		if(!ueInTestConnected()){
			return;
		}
		
		
		dm.setUeIP(ueInTest.getLanIpAddress());
		dm.setPORT(ueInTest.getDMToolPort());
		try{
			dm.init();			
		}catch(Exception e){
			e.printStackTrace();
		}

		report.reportHtml("ue show link before getting measurement from UE in test", dutInTest.lteCli("ue show link"), true);
		report.report("Getting measurement tables from UE");
		getTablesFromUEToLocalTable(dm);
		dm.close();
		dm = null;
		
		GeneralUtils.startLevel("Tables received from UE");
		printTablesToReport(measTables.getObjectTable(), measTables.getReportTable(), measTables.getCfgTable());
		GeneralUtils.stopLevel();

		GeneralUtils.startLevel("Expected tables");
		printTablesToReport(expectedTables.getObjectTable(), expectedTables.getReportTable(), expectedTables.getCfgTable());
		GeneralUtils.stopLevel();

		GeneralUtils.startLevel("Results");
		boolean flagTables = checkTables();
		GeneralUtils.stopLevel();
		if (flagTables) {
			report.report("Tables MeasurementCfg are equal");
		} else {
			report.report("Tables MeasurementCfg are not equal", Reporter.FAIL);
			reason = "Tables MeasurementCfg are not equal";
		}
	}
	
	//TEST 29
	@Test
	@TestProperties(name = "SonDisable_TBGDisable_AnrPeriodic_NoNbr_DRX", returnParam = {
		"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void test29_sonDisableTBGDisableAnrPeriodicNoNbrDRX(){
		reportConfiguration("Disabled","Periodic","Disabled","None",null,null);
		report.report("DRX testing");
		GeneralUtils.startLevel("Check inter frequency for " + dut2.getNetspanName());
		if(!checkInterEarfcn(dut2)){
			report.report(dut2.getNetspanName() + " Earfcn not " + interEarfcn + " as expected, stop test.",Reporter.FAIL);
			reason = dut2.getNetspanName() + " Earfcn not inter Earfcn as expected";
			GeneralUtils.stopLevel();
			return;
		}else{
			report.report(dut2.getNetspanName() + " Earfcn: " + dut2.getEarfcn());
		}
		GeneralUtils.stopLevel();
		if(!preTest(false)){
			return;
		}
		createExpectedTablesTest29();
		report.report("Main EnodeB in test: "+dutInTest.getName());
		
		GeneralUtils.startLevel("Clone and set son profile");
		SonParameters sonParams = new SonParameters();
		setCsonState(sonParams, false);
		setAnrState(sonParams, 2, true);
		setDicicState(sonParams, false);
		boolean action = changeSonProfile(sonParams);
		if(!action) {
			report.report("Failed to clone and set son profile", Reporter.FAIL);
			reason = "Failed to clone and set son profile";
			GeneralUtils.stopLevel();
			return;
		}else{
			report.report("Clone and set son profile passed successfully");
		}
		GeneralUtils.stopLevel();
		
		report.reportHtml("db get anrcfg", dutInTest.lteCli("db get anrcfg"), true);
		
		GeneralUtils.startLevel("Clone and set mobility profile");
		if(!setTBGDisable()){
			report.report("Failed to clone and set mobility profile", Reporter.FAIL);
			reason = "Failed to clone and set mobility profile";
			GeneralUtils.stopLevel();
			return;
		}else{
			report.report("Clone and set mobility profile passed successfully");
		}
		GeneralUtils.stopLevel();
		GeneralUtils.unSafeSleep(10*1000);
		if(!peripheralsConfig.changeEnbState(dut2, EnbStates.IN_SERVICE)){
			report.report("Failed to set EnodeB "+dut2.getNetspanName()+" in service",Reporter.WARNING);
			reason = "Failed to set EnodeB "+dut2.getNetspanName()+" in service";			
		}
				
		if(!ueInTestConnected()){
			return;
		}
		
		dm.setUeIP(ueInTest.getLanIpAddress());
		dm.setPORT(ueInTest.getDMToolPort());
		try{
			dm.init();			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		report.report("Getting measurement tables from UE");
		getTablesFromUEToLocalTable(dm);
		dm.close();
		dm = null;
		
		GeneralUtils.startLevel("Tables received from UE");
		printTablesToReport(measTables.getObjectTable(), measTables.getReportTable(), measTables.getCfgTable());
		GeneralUtils.stopLevel();

		GeneralUtils.startLevel("Expected tables");
		printTablesToReport(expectedTables.getObjectTable(), expectedTables.getReportTable(), expectedTables.getCfgTable());
		GeneralUtils.stopLevel();

		GeneralUtils.startLevel("Results");
		boolean flagTables = checkTables();
		GeneralUtils.stopLevel();
		if(flagTables){
			report.report("Tables MeasurementCfg are equal");
		}else{
			report.report("Tables MeasurementCfg are not equal", Reporter.FAIL);
			reason = "Tables MeasurementCfg are not equal";
		}		
	}
	
	private void genericTestDicic(int anr, Integer dicic){
		report.report("Main EnodeB in test: "+dutInTest.getName());
		GeneralUtils.startLevel("Clone and set mobility profile");
		if(!setTBGDisable()){
			report.report("Failed to clone and set mobility profile",Reporter.FAIL);
			reason = "Failed to clone and set mobility profile";
			GeneralUtils.stopLevel();
			return;
		}else{
			report.report("Clone and set mobility profile passed successfully");
		}
		GeneralUtils.stopLevel();
		report.reportHtml("db get Sr15MeasReportConfig", dutInTest.lteCli("db get Sr15MeasReportConfig"), true);

		if(!ueInTestConnected()){
			return;
		}
		
		GeneralUtils.startLevel("Clone and set son profile");
		SonParameters sonParams = new SonParameters();
		setCsonState(sonParams, false);
		setAnrState(sonParams, anr, false);
		setDicicState(sonParams, true);
		boolean action = changeSonProfile(sonParams);
		
		if(!action) {
			report.report("Failed to clone and set son profile", Reporter.FAIL);
			reason = "Failed to clone and set son profile";
			GeneralUtils.stopLevel();
			return;
		}else{
			report.report("Clone and set son profile passed successfully");
		}
		GeneralUtils.stopLevel();
		
		report.reportHtml("db get anrcfg", dutInTest.lteCli("db get anrcfg"), true);
		
		GeneralUtils.unSafeSleep(5*1000);
		if(dicic!=null){
			String note = "";
			sonParams = new SonParameters();
			if(dicic==31){
				setDicicState(sonParams, false);
				note = "Update son profile to disable DICIC";
			}else if (dicic==32){
				setAnrState(sonParams, 2, false);
				note = "Update son profile to enable anr periodic";
			}else if(dicic==33){
				setAnrState(sonParams, 0, false);
				note = "Update son profile to disable anr";
			}			
			GeneralUtils.startLevel(note);
			action = changeSonProfile(sonParams);
			if(!action){
				report.report("Failed to update son profile", Reporter.FAIL);
				reason = "Failed to update son profile";
				GeneralUtils.stopLevel();
				return;
			}else{
				report.report("Update son profile passed successfully");
			}
			GeneralUtils.stopLevel();
			GeneralUtils.unSafeSleep(5*1000);
		}
		
		GeneralUtils.unSafeSleep(10*1000);
		GeneralUtils.startLevel("Add neighbors");
		if(!addNeighbors(1)){
			report.report("Failed to add neighbors",Reporter.FAIL);
			reason = "Failed to add neighbors";
			GeneralUtils.stopLevel();
			return;
		}else{
			report.report("Succeeded to add neighbors");
		}
		GeneralUtils.stopLevel();
		
		dm.setUeIP(ueInTest.getLanIpAddress());
		dm.setPORT(ueInTest.getDMToolPort());
		try{
			dm.init();			
		}catch(Exception e){
			e.printStackTrace();
		}
				
		report.reportHtml("ue show link", dutInTest.lteCli("ue show link"), true);
		report.report("Getting measurement tables from UE");
		getTablesFromUEToLocalTable(dm);
		dm.close();
		dm = null;
		
		GeneralUtils.startLevel("Tables received from UE");
		printTablesToReport(measTables.getObjectTable(), measTables.getReportTable(), measTables.getCfgTable());
		GeneralUtils.stopLevel();
		
		GeneralUtils.startLevel("Expected tables");
		printTablesToReport(expectedTables.getObjectTable(), expectedTables.getReportTable(), expectedTables.getCfgTable());
		GeneralUtils.stopLevel();
		
		GeneralUtils.startLevel("Results");
		boolean flagTables = checkTables();
		GeneralUtils.stopLevel();
		if(flagTables){
			report.report("Tables MeasurementCfg are equal");
		}else{
			report.report("Tables MeasurementCfg are not equal",Reporter.FAIL);
			reason = "Tables MeasurementCfg are not equal";
		}
	}
	
	//TEST 30
	@Test
	@TestProperties(name = "SonDisable_TBGDisable_AnrDisable_DicicEnable_1Intra1Inter", returnParam = {
		"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful"})
	public void test30_sonDisableTBGDisableAnrDisableDicicEnable1Intra1Inter() {
		reportConfiguration("Disabled","Disabled","Disabled","1 Intra, 1 Inter",null,"Enabled");
		if(!preTest(false)){
			return;
		}
		createExpectedTablesTest30();
		genericTestDicic(0,null);
	}
	
	//TEST 31
	@Test
	@TestProperties(name = "SonDisable_TBGDisable_AnrDisable_DicicDisableDuringTest_1Intra1Inter", returnParam = {
		"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful"})
	public void test31_sonDisableTBGDisableAnrDisableDicicDisableDuringTest1Intra1Inter() {
		reportConfiguration("Disabled","Disabled","Disabled","1 Intra, 1 Inter",null,"Disabled during test");
		if(!preTest(false)){
			return;
		}
		createExpectedTablesTest31();
		genericTestDicic(0,31);
	}
	
	//TEST 32
	@Test
	@TestProperties(name = "SonDisable_TBGDisable_AnrPeriodicDuringTest_DicicEnable_1Intra1Inter", returnParam = {
		"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful"})
	public void test32_sonDisableTBGDisableAnrPeriodicDuringTestDicicEnabled1Intra1Inter() {
		reportConfiguration("Disabled","Disabled during test","Periodic","1 Intra, 1 Inter",null,"Enabled");
		if(!preTest(false)){
			return;
		}
		createExpectedTablesTest32();
		genericTestDicic(0,32);
	}
	
	//TEST 33
	@Test
	@TestProperties(name = "SonDisable_TBGDisable_AnrDisableDuringTest_DicicEnable_1Intra1Inter", returnParam = {
		"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful"})
	public void test33_sonDisableTBGDisableAnrDisableDuringTestDicicEnable1Intra1Inter() {
		reportConfiguration("Disabled","Disabled during test","Disabled","1 Intra, 1 Inter",null,"Enabled");
		if(!preTest(false)){
			return;
		}
		createExpectedTablesTest33();
		genericTestDicic(2,33);
	}
	
	//TEST 34
	@Test
	@TestProperties(name = "SonDisable_TBGEnable_AnrDisable_DicicEnable_1Intra2Inter", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
		"IsTestWasSuccessful"})
	public void test34_sonDisableTBGEnableAnrDisableDicicEnable1Intra2Inter(){	
		reportConfiguration("Disabled","Disabled","Enabled","1 Intra, 2 Inter",null,"Enabled");
		
		if(!preTest(false)){
			return;
		}
		createExpectedTablesTest34();
		genericTest(false, 0, true, true, 2, null, false, false, false);
	}
	
	//TEST 35
	@Test
	@TestProperties(name = "SonDisable_TBGEnable_AnrPeriodic_DicicEnable_NoNbr", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
		"IsTestWasSuccessful"})
	public void test35_sonDisableTBGEnableAnrPeriodicDicicEnableNoNbr(){	
		reportConfiguration("Disabled","Periodic","Enabled","None",null,"Enabled");
		
		if(!preTest(false)){
			return;
		}
		createExpectedTablesTest35();
		genericTest(false, 2, true, true, 0, null, false, false, false);
	}
	
	//TEST 36
	@Test
	@TestProperties(name = "SonDisable_TBGEnable_AnrHO_DicicEnable_NoNbr", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
		"IsTestWasSuccessful"})
	public void test36_sonDisableTBGEnableAnrHODicicEnableNoNbr(){	
		reportConfiguration("Disabled","Handover","Enabled","None",null,"Enabled");
		
		if(!preTest(false)){
			return;
		}
		createExpectedTablesTest36();
		genericTest(false, 1, true, true, 0, null, false, false, false);
	}
	
	//TEST 37
	@Test
	@TestProperties(name = "SonDisable_TBGEnable_AnrHO_DicicEnable_1Intra1Inter", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
		"IsTestWasSuccessful"})
	public void test37_sonDisableTBGEnableAnrHODicicEnable1Intra1Inter(){	
		reportConfiguration("Disabled","Handover","Enabled","1 Intra, 1 Inter",null,"Enabled");
		
		if(!preTest(false)){
			return;
		}
		createExpectedTablesTest37();
		genericTest(false, 1, true, true, 1, null, false, false, false);
	}
	
	//TEST 38
	@Test
	@TestProperties(name = "SonDisable_TBGEnable_AnrDisable_DicicEnable_1Intra1Inter_RFbelowA2", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
		"IsTestWasSuccessful"})
	public void test38_sonDisableTBGEnableAnrDisableDicicEnable1Intra1InterRFbelowA2(){	
		reportConfiguration("Disabled","Handover","Enabled","1 Intra, 1 Inter","Below A2","Enabled");
		
		if(!preTest(false)){
			return;
		}
		createExpectedTablesTest38();
		genericTest(false, 0, true, true, 1, Threshold.Below_A2, false, false, false);
	}
	
	//TEST 39
	@Test
	@TestProperties(name = "SonDisable_TBGEnable_AnrDisable_DicicEnable_1Intra1Inter_RFoverA1", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
		"IsTestWasSuccessful"})
	public void test39_sonDisableTBGEnableAnrDisableDicicEnable1Intra1InterRFoverA1(){	
		reportConfiguration("Disabled","Disable","Enabled","1 Intra, 1 Inter","Over A1","Enabled");
		
		if(!preTest(false)){
			return;
		}
		createExpectedTablesTest39();
		genericTest(false, 0, true, true, 1, Threshold.Over_A1, false, false, false);
	}
	
	//TEST 40
	@Test
	@TestProperties(name = "SonDisable_TBGEnable_AnrPeriodicSteady_DicicEnable_NoNbr", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
		"IsTestWasSuccessful"})
	public void test40_sonDisableTBGEnableAnrPeriodicSteadyDicicEnableNoNbr(){	
		reportConfiguration("Disabled","Periodic, steady mode","Enabled","None",null,"Enabled");
		
		if(!preTest(false)){
			return;
		}
		createExpectedTablesTest40();
		genericTest(false, 2, true, true, 0, null, true, false, false);
	}
	
	//TEST 41
	@Test
	@TestProperties(name = "SonDisable_TBGEnable_AnrPeriodicSteady_DicicEnable_NoNbr_RFbelowA2", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
		"IsTestWasSuccessful"})
	public void test41_sonDisableTBGEnableAnrPeriodicSteadyDicicEnableNoNbrRFBelowA2(){	
		reportConfiguration("Disabled","Periodic, steady mode","Enabled","None","Below A2","Enabled");
		
		if(!preTest(false)){
			return;
		}
		createExpectedTablesTest41();
		genericTest(false, 2, true, true, 0, Threshold.Below_A2, true, false, false);
	}
	
	//TEST 42
	@Test
	@TestProperties(name = "SonDisable_TBGEnable_AnrPeriodicSteady_DicicEnable_NoNbr_RFoverA1", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
		"IsTestWasSuccessful"})
	public void test42_sonDisableTBGEnableAnrPeriodicSteadyDicicEnable1Intra1InterRFoverA1(){	
		reportConfiguration("Disabled","Periodic, steady mode","Enabled","None","Over A1","Enabled");
		
		if(!preTest(false)){
			return;
		}
		createExpectedTablesTest42();
		genericTest(false, 2, true, true, 0, Threshold.Over_A1, true, false, false);
	}
	
	//TEST 43
	@Test
	@TestProperties(name = "SonDisable_TBGEnable_AnrPeriodicLight_DicicEnable_NoNbr_ChosenUE", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful"})
	public void test43_sonDisableTBGEnableAnrPeriodicLightDicicEnableNoNbrChosenUE() {
		ArrayList<String> ues = new ArrayList<String>();
		int counter=0;
		reportConfiguration("Disabled","Periodic, light mode","Enabled","None",null,"Enabled");
		if(!preTest(true)){
			return;
		}
		createExpectedTablesTest43();
		if(!genericTestMultipleUEs(false,true,true,true)){
			return;
		}
			
		for(UE ue:dynamicArrayUEs){
			DMtool tool = new DMtool();
			tool.setUeIP(ue.getLanIpAddress());
			tool.setPORT(ue.getDMToolPort());
			try{
				tool.init();			
			}catch(Exception e){
				e.printStackTrace();
			}
			
			getTablesFromUEToLocalTable(tool);
			
			GeneralUtils.startLevel("Tables from ue "+ue.getLanIpAddress());
			printTablesToReport(measTables.getObjectTable(), measTables.getReportTable(), measTables.getCfgTable());
			GeneralUtils.stopLevel();
			if(checkTablesMultipleUEs()){
				counter++;
				ues.add(ue.getLanIpAddress());
				try{
					report.report("UE "+ue.getLanIpAddress()+" is chosen. Verify cdrx is not configured");
					report.reportHtml("getdrxConfig", tool.cli("getdrxConfig"), true);
					boolean drx = tool.isCdrxConfigured();
					if(!drx){
						report.report("UE "+ue.getLanIpAddress()+" doesn't have cdrx configured"); 
					}else{
						report.report("UE "+ue.getLanIpAddress()+" has cdrx configured",Reporter.FAIL);
						reason = "UE "+ue.getLanIpAddress()+" has cdrx configured";
					}
				}catch(Exception e){
					e.printStackTrace();
					report.report("Failed to get information from DM Tool", Reporter.FAIL);
				}
			}
			tool.close();	
			tool=null;
			createExpectedTablesTest43();
		}
					
		GeneralUtils.startLevel("Expected tables");
		printTablesToReport(expectedTables.getObjectTable(), expectedTables.getReportTable(), expectedTables.getCfgTable());
		GeneralUtils.stopLevel();
		
		GeneralUtils.startLevel("UEs that received expected tables");
		report.report(ues.toString());
		GeneralUtils.stopLevel();
		
		report.report("Checking tables of chosen UEs");
		if(counter == 1){
			report.report("Number of UEs that received the expected tables is 1 - as expected");
		}else{
			report.report("Number of UEs that received the expected tables is not 1 - not as expected",Reporter.FAIL);
			reason = "Number of UEs that received the expected tables is not 1 - not as expected";
		}
	}
	
	//TEST 44
	//@Test
	@TestProperties(name = "SonDisable_TBGEnable_AnrPeriodicLight_DicicEnable_NoNbr_NoChosenUE", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful"})
	public void test44_sonDisableTBGEnableAnrPeriodicLightDicicEnableNoNbrNoChosenUE() {
		ArrayList<String> ues = new ArrayList<String>();
		int counter = 0;
		reportConfiguration("Disabled","Periodic, light mode","Enabled","None",null,"Enabled");
		if (!preTest(true)) {
			return;
		}
		createExpectedTablesTest44();
		if(!genericTestMultipleUEs(false,true,true,false)){
			return;
		}
		
		returnAttToMin();
		GeneralUtils.unSafeSleep(5*1000);
		
		for(UE ue:dynamicArrayUEs){
			DMtool tool = new DMtool();
			tool.setUeIP(ue.getLanIpAddress());
			tool.setPORT(ueInTest.getDMToolPort());
			try{
				tool.init();			
			}catch(Exception e){
				e.printStackTrace();
			}

			getTablesFromUEToLocalTable(tool);
			tool.close();
			tool=null;
			
			GeneralUtils.startLevel("Tables from ue "+ue.getLanIpAddress());
			printTablesToReport(measTables.getObjectTable(), measTables.getReportTable(), measTables.getCfgTable());
			if(checkTablesMultipleUEs()){
				counter++;
				ues.add(ue.getLanIpAddress());
			}
			GeneralUtils.stopLevel();
			createExpectedTablesTest44();
		}
			
		GeneralUtils.startLevel("Expected tables");
		printTablesToReport(expectedTables.getObjectTable(), expectedTables.getReportTable(), expectedTables.getCfgTable());
		GeneralUtils.stopLevel();
		
		GeneralUtils.startLevel("UEs that received expected tables");
		report.report(ues.toString());
		GeneralUtils.stopLevel();
		
		report.report("Checking tables of non chosen UEs");
		if (counter == dynamicArrayUEs.size()-1) {
			report.report("Number of UEs that received the expected tables is "+counter+" - as expected");
		} else {
			report.report("Number of UEs that received the expected tables is not "+(dynamicArrayUEs.size()-1)+" - not as expected",Reporter.FAIL);
			reason = "Number of UEs that received the expected tables is not "+(dynamicArrayUEs.size()-1)+" - not as expected";
		}
	}
	
	//TEST 45
	@Test
	@TestProperties(name = "SonDisable_TBGEnable_AnrPeriodicLight_DicicEnable_NoNbr_NoChosenUE_RFA2", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful"})
	public void test45_sonDisableTBGEnableAnrPeriodicLightDicicEnableNoNbrNoChosenUERFA2() {
		ArrayList<String> ues = new ArrayList<String>();
		int counter = 0;
		reportConfiguration("Disabled","Periodic, light mode","Enabled","None","Below A2","Enabled");
		if (!preTest(true)) {
			return;
		}
		createExpectedTablesTest45();
		if(!genericTestMultipleUEs(false,true,true,false)){
			return;
		}
				
		GeneralUtils.startLevel("Changing attenuation in order to reach wanted RSRP0 in UEs");
		if (!changeAttOverA1AllUEs(dynamicArrayUEs)) {
			failRFWithoutDmTool();
			return;
		}
		GeneralUtils.unSafeSleep(5 * 1000);
		if (!changeAttBelowA2AllUEs(dynamicArrayUEs)) {
			failRFWithoutDmTool();
			return;
		}
		GeneralUtils.unSafeSleep(5 * 1000);
		GeneralUtils.stopLevel();
		report.report("Reached wanted RF conditions");
		
		for(UE ue:dynamicArrayUEs){
			DMtool tool = new DMtool();
			tool.setUeIP(ue.getLanIpAddress());
			tool.setPORT(ueInTest.getDMToolPort());
			try{
				tool.init();			
			}catch(Exception e){
				e.printStackTrace();
			}
			
			getTablesFromUEToLocalTable(tool);
			tool.close();	
			tool=null;
			
			GeneralUtils.startLevel("Tables from ue "+ue.getLanIpAddress());
			printTablesToReport(measTables.getObjectTable(), measTables.getReportTable(), measTables.getCfgTable());
			if(checkTablesMultipleUEs()){
				counter++;
				ues.add(ue.getLanIpAddress());
			}
			GeneralUtils.stopLevel();
			createExpectedTablesTest45();
		}
			
		GeneralUtils.startLevel("Expected tables");
		printTablesToReport(expectedTables.getObjectTable(), expectedTables.getReportTable(), expectedTables.getCfgTable());
		GeneralUtils.stopLevel();
		
		GeneralUtils.startLevel("UEs that received expected tables");
		report.report(ues.toString());
		GeneralUtils.stopLevel();
		
		report.report("Checking tables of non chosen UEs");
		if (counter == dynamicArrayUEs.size()-1) {
			report.report("Number of UEs that received the expected tables is "+counter+" - as expected");
		} else {
			report.report("Number of UEs that received the expected tables is not "+(dynamicArrayUEs.size()-1)+" - not as expected",Reporter.FAIL);
			reason = "Number of UEs that received the expected tables is not "+(dynamicArrayUEs.size()-1)+" - not as expected";
		}
	}
	
	//TEST 46
	@Test
	@TestProperties(name = "SonDisable_TBGEnable_AnrPeriodicLight_DicicEnable_NoNbr_NoChosenUE_RFA1", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful"})
	public void test46_sonDisableTBGEnableAnrPeriodicLightDicicEnableNoNbrNoChosenUERFA1() {
		ArrayList<String> ues = new ArrayList<String>();
		int counter = 0;
		reportConfiguration("Disabled","Periodic, light mode","Enabled","None","Over A1","Enabled");
		if (!preTest(true)) {
			return;
		}
		createExpectedTablesTest46();
		if(!genericTestMultipleUEs(false,true,true,false)){
			return;
		}
		
		
		GeneralUtils.startLevel("Changing attenuation in order to reach wanted RSRP0 in UEs");
		if (!changeAttBelowA2AllUEs(dynamicArrayUEs)) {
			failRFWithoutDmTool();
			return;
		}
		GeneralUtils.unSafeSleep(5 * 1000);
		if (!changeAttOverA1AllUEs(dynamicArrayUEs)) {
			failRFWithoutDmTool();
			return;
		}
		GeneralUtils.unSafeSleep(5 * 1000);
		GeneralUtils.stopLevel();
		report.report("Reached wanted RF conditions");
		
		for(UE ue:dynamicArrayUEs){
			DMtool tool = new DMtool();
			tool.setUeIP(ue.getLanIpAddress());
			tool.setPORT(ueInTest.getDMToolPort());
			try{
				tool.init();			
			}catch(Exception e){
				e.printStackTrace();
			}
			
			getTablesFromUEToLocalTable(tool);
			tool.close();
			tool=null;
			
			GeneralUtils.startLevel("Tables from ue "+ue.getLanIpAddress());
			printTablesToReport(measTables.getObjectTable(), measTables.getReportTable(), measTables.getCfgTable());
			if(checkTablesMultipleUEs()){
				counter++;
				ues.add(ue.getLanIpAddress());
			}
			GeneralUtils.stopLevel();
			createExpectedTablesTest46();
		}
				
		GeneralUtils.startLevel("Expected tables");
		printTablesToReport(expectedTables.getObjectTable(), expectedTables.getReportTable(), expectedTables.getCfgTable());
		GeneralUtils.stopLevel();
		
		GeneralUtils.startLevel("UEs that received expected tables");
		report.report(ues.toString());
		GeneralUtils.stopLevel();
		
		report.report("Checking tables of non chosen UEs");
		if (counter == dynamicArrayUEs.size()-1) {
			report.report("Number of UEs that received the expected tables is "+counter+" - as expected");
		} else {
			report.report("Number of UEs that received the expected tables is not "+(dynamicArrayUEs.size()-1)+" - not as expected",Reporter.FAIL);
			reason = "Number of UEs that received the expected tables is not "+(dynamicArrayUEs.size()-1)+" - not as expected";
		}
	}
	
	//TEST 47
	@Test
	@TestProperties(name = "SonDisable_TBGEnableDual_AnrPeriodicSteady_DicicEnable_NoNbr", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
		"IsTestWasSuccessful"})
	public void test47_sonDisableTBGEnableDualAnrPeriodicSteadyDicicEnableNoNbr(){	
		reportConfiguration("Disabled","Periodic, steady mode","Enabled, Dual","None",null,"Enabled");
		
		if(!preTest(false)){
			return;
		}
		createExpectedTablesTest47();
		genericTest(false, 2, true, true, 0, null, true, true, false);
	}
	
	private boolean ueInTestConnected(){
		boolean connected = true;
		GeneralUtils.startLevel("Starting UE in test");
		ueInTest.start();
		GeneralUtils.unSafeSleep(10 * 1000);
		if(!peripheralsConfig.checkSingleUEConnectionToNode(ueInTest, dutInTest)){
			 if(!rebootUeAndRecheckConnection(ueInTest)){
				 report.report("Failed to connect UE in test",Reporter.FAIL);
				 reason = "Failed to connect UE in test";
				 connected = false;
			 }else{
				 report.report("UE in test is connected");
			 }
		}else{
			report.report("UE in test is connected");
		}
		GeneralUtils.stopLevel();
		return connected;
	}
	
	private boolean rebootUeAndRecheckConnection(UE ueInTest) {
		boolean result = false;
		GeneralUtils.startLevel("ue failed to connect - trying to reboot and rechecking");
		peripheralsConfig.rebootUE(ueInTest);
		if(peripheralsConfig.checkSingleUEConnectionToNode(ueInTest, dutInTest)){
			result = true;
		}
		GeneralUtils.stopLevel();
		return result;
	}
	
	public void getRsrpForMobilityProfile(boolean multiple){
		GeneralUtils.startLevel("Getting minimum RSRP for mobility profile");
		if(!multiple){
			stopGap = ueInTest.getRSRP(0);
			if(stopGap == GeneralUtils.ERROR_VALUE){
				stopGap = -84;
			}else{
				stopGap = stopGap - 6;
			}
		}else{
			stopGap = getMinRsrp()-6;
		}
		startGap = stopGap - 6;
		if(!multiple){
			report.report("UE in test: "+ueInTest.getName());
		}
		GeneralUtils.stopLevel();
	}
	
	private int getMinRsrp(){
		ArrayList<UE> ueList = new ArrayList<UE>();
		boolean enodeb;
		
		int temp = -84;
		int rsrp = 0;
		toRemove.clear();
		for(UE ue:dynamicArrayUEs){
			ueList.clear();
			ueList.add(ue);
			enodeb = peripheralsConfig.checkIfAllUEsAreConnectedToNode(ueList, dutInTest);
			if(enodeb){
				rsrp = ue.getRSRP(0);
				if(rsrp == GeneralUtils.ERROR_VALUE){
					continue;
				}
				if(rsrp<temp){
					temp = rsrp;
				}
			}else{
				toRemove.add(ue);
			}
		}
		for(UE ue:toRemove){
			report.report("UE "+ue.getName()+" removed from test");
			dynamicArrayUEs.remove(ue);
		}
		return temp;
	}
	
	private UE getUeInTest(){
		ArrayList<UE> ueList = new ArrayList<UE>();
		boolean enodeb;
		for(UE ue:dynamicArrayUEs){
			ueList.clear();
			ueList.add(ue);
			enodeb = peripheralsConfig.checkIfAllUEsAreConnectedToNode(ueList, dutInTest);
			if(enodeb){
				return ue;			
			}
		}
		return null;
	}
	
	private void setDicicState(SonParameters sonParams, boolean state){
		EnabledDisabledStates stateToConfigure = null;
		if(state){
			stateToConfigure = EnabledDisabledStates.ENABLED;
		}else{
			stateToConfigure = EnabledDisabledStates.DISABLED;
		}
		sonParams.setOptimizationMode(stateToConfigure);
		sonParams.setIcicMode(stateToConfigure);
	}
	
	private void reportConfiguration(String cson, String anr, String tbg, String nbr, String rf,String dicic) {
		GeneralUtils.startLevel("Configuration of test");
		report.report("Cson: "+cson);
		report.report("Anr: "+anr);
		report.report("TBG: "+tbg);
		if(dicic!=null){
			report.report("DICIC: "+dicic);
		}
		report.report("Neighbors: "+nbr);
		if(rf!=null){
			report.report("RF conditions: "+rf);
		}
		GeneralUtils.stopLevel();
	}
	
	private boolean checkInterEarfcn(EnodeB enb){
		return dut2.getEarfcn() == interEarfcn;
	}
	
	private void setCsonState(SonParameters sonParams,boolean son){
		sonParams.setCSonEnabled(son);
	}
	
	private void setAnrState(SonParameters sonParams, int state, boolean per){
		if(state==0){
			sonParams.setSonCommissioning(true);
			sonParams.setAnrState(SonAnrStates.DISABLED);
		}else{
			sonParams.setSonCommissioning(true);
			ArrayList<Integer> anrFrequencyList = new ArrayList<>();
			anrFrequencyList.add(earfcn);
			if(!per){
				anrFrequencyList.add(middleEarfcn);			
			}else{
				anrFrequencyList.add(interEarfcn);
			}
			sonParams.setAnrFrequencyList(anrFrequencyList);			
			if(state==1){
				sonParams.setAnrState(SonAnrStates.HO_MEASUREMENT);	
			}else if(state==2){
				sonParams.setAnrState(SonAnrStates.PERIODICAL_MEASUREMENT);
			}
		}
	}
	
	private boolean changeSonProfile(SonParameters sonParams){
		if(enodeBConfig.changeSonProfile(dutInTest, sonParams)){
			return true;		
		}
		return false;
	}

	/**
	 * 
	 * @param dualMode
	 * @return
	 */
	private boolean setTBGEnable(boolean dualMode){
		MobilityParameters mobilityParams = new MobilityParameters();
		mobilityParams.setThresholdBasedMeasurement(EnabledDisabledStates.ENABLED);
		report.report("A1 threshold configured: "+stopGap);
		mobilityParams.setStopGap(stopGap);
		report.report("A2 threshold configured: "+startGap);
		mobilityParams.setStartGap(startGap);
		mobilityParams.setThresholdBasedMeasurementDual(dualMode);
		int numOfCellsInNode = dutInTest.getNumberOfActiveCells();
		while(numOfCellsInNode > 0) {
			dutInTest.setCellContextNumber(numOfCellsInNode);
			if(!enodeBConfig.cloneAndSetMobilityProfileViaNetSpan(dutInTest, dutInTest.getDefaultNetspanProfiles().getMobility(), mobilityParams)){
				return false;
			}
			numOfCellsInNode--;
		}
		
		return true;
	}
	
	private boolean setTBGDisable(){
		MobilityParameters mobilityParams = new MobilityParameters();
		mobilityParams.setThresholdBasedMeasurement(EnabledDisabledStates.DISABLED);
		int numOfCellsInNode = dutInTest.getNumberOfActiveCells();
		while(numOfCellsInNode > 0) {
			dutInTest.setCellContextNumber(numOfCellsInNode);
			if(!enodeBConfig.cloneAndSetMobilityProfileViaNetSpan(dutInTest, dutInTest.getDefaultNetspanProfiles().getMobility(), mobilityParams)){
				return false;
			}
			numOfCellsInNode--;
		}
		return true;
	}
	
	private boolean addNeighbors(int party){
		ArrayList<Integer> earcfnToCreate = new ArrayList<Integer>();
		earcfnToCreate.add(middleEarfcn);
		earcfnToCreate.add(interEarfcn);
		virtualNeighborsList = neighbors.addingThirdPartyNeihbors(dutInTest, party, true, earcfnToCreate);
		if(virtualNeighborsList.size()!=party){
			return false;
		}
		allNeighborsList.add(otherEnb.get(0));
		allNeighborsList.addAll(virtualNeighborsList);
		if (allNeighborsList.size() >= 1) {
			for (EnodeB enb : allNeighborsList) {
				if (!neighbors.addNeighbor(dutInTest, enb, HoControlStateTypes.ALLOWED, X2ControlStateTypes.AUTOMATIC,
						HandoverType.TRIGGER_X_2, true, "0")) {
					return false;
				}
			}
		}
		GeneralUtils.unSafeSleep(10*1000);
		report.reportHtml("db get nghlist", dutInTest.lteCli("db get nghlist"), true);
		return true;
	}

	private boolean checkForSteady(){
		String state = dutInTest.lteCli("cell show anrstate");
		long time = System.currentTimeMillis();
		while(!state.contains("ANR STATE = 3") && System.currentTimeMillis()-time<3*60*1000){
			GeneralUtils.unSafeSleep(10*1000);
			state = dutInTest.lteCli("cell show anrstate");
			report.report("Response from enodeB: "+state);
		}
		report.report("Response from enodeB: "+state);
		if(state.contains("ANR STATE = 3")){
			return true;
		}else{
			return false;
		}
	}
	
	private boolean checkForLight(){
		String state = dutInTest.lteCli("cell show anrstate");
		GeneralUtils.printToConsole(state);
		long time = System.currentTimeMillis();
		int maxVal = attenuatorSetUnderTest.getMaxAttenuation();
		float attenuatorsCurrentValue = attenuatorSetUnderTest.getAttenuation()[0];
		int step = attenuatorSetUnderTest.getAttenuationStep();
		while(!state.contains("ANR STATE = 5") && System.currentTimeMillis()-time<3*60*1000 && attenuatorsCurrentValue+step<=maxVal){
			attenuatorsCurrentValue+=step;
			if(peripheralsConfig.setAttenuatorSetValue(attenuatorSetUnderTest, attenuatorsCurrentValue)){
				report.report("Changed attenuator's value to "+attenuatorsCurrentValue);					
			}else{
				report.report("Failed to change attenuator's value to "+attenuatorsCurrentValue, Reporter.WARNING);
			}			
			GeneralUtils.unSafeSleep(5*1000);
			state = dutInTest.lteCli("cell show anrstate");
			report.report("Response from enodeB: "+state);
			if(state.contains("ANR STATE = 0")){
				return false;
			}
		}
		report.report("Response from enodeB: "+state);
		if(state.contains("ANR STATE = 5")){
			return true;
		}else{
			return false;
		}
	}

	private void returnAttToMin(){
		int minVal = attenuatorSetUnderTest.getMinAttenuation();
		float attenuatorsCurrentValue = attenuatorSetUnderTest.getAttenuation()[0];
		int step = attenuatorSetUnderTest.getAttenuationStep();
		while(attenuatorsCurrentValue-step>=minVal){
			attenuatorsCurrentValue-=step;
			peripheralsConfig.setAttenuatorSetValue(attenuatorSetUnderTest, attenuatorsCurrentValue);
			GeneralUtils.unSafeSleep(attenuatorStepTime);
		}
	}
	
	private void failRF(){
		GeneralUtils.stopLevel();
		report.report("Failed to reach wanted RF conditions",Reporter.FAIL);
		reason = "Failed to reach wanted RF conditions";
		dm.close();
	}
	
	private void failRFWithoutDmTool(){
		GeneralUtils.stopLevel();
		report.report("Failed to reach wanted RF conditions",Reporter.FAIL);
		reason = "Failed to reach wanted RF conditions";
	}
	
	private boolean checkTables(){
		if(checkObject()){
			report.report("MeasurementCfgObject tables are equal");
		}else{
			report.report("MeasurementCfgObject tables are not equal");
		}
		if(checkReport()){
			report.report("MeasurementCfgReport tables are equal");
		}else{
			report.report("MeasurementCfgReport tables are not equal");
		}
		boolean cfg = checkCfg();
		if(cfg){
			report.report("MeasurementCfg tables are equal");
		}else{
			report.report("MeasurementCfg tables are not equal",Reporter.FAIL);
			reason = "MeasurementCfg tables are not equal";
		}
		return cfg;
	}
	
	private boolean checkTablesMultipleUEs(){
		checkObject();
		checkReport();
		boolean cfg = checkCfg();
		return cfg;
	}
	
	private boolean checkObject(){
		createRealHashObject();
		createExpectedHashObject();
		Iterator<MeasurementCfgObject> iter = expectedTables.getObjectTable().iterator();
		while (iter.hasNext()) {
			MeasurementCfgObject exp_obj = iter.next();
			Iterator<MeasurementCfgObject> iter2 = measTables.getObjectTable().iterator();
			while (iter2.hasNext()) {
				MeasurementCfgObject obj = iter2.next();
				if(exp_obj.getEarfcn()==obj.getEarfcn()){
					iter2.remove();
					iter.remove();
					break;
				}
			}
		}
		if(measTables.getObjectTable().isEmpty() && expectedTables.getObjectTable().isEmpty()){
			return true;
		}
		return false;
	}
	
	private boolean checkReport(){
		createRealHashReport();
		createExpectedHashReport();
		Iterator<MeasurementCfgReport> iter = expectedTables.getReportTable().iterator();
		while (iter.hasNext()) {
			MeasurementCfgReport exp_obj = iter.next();
			Iterator<MeasurementCfgReport> iter2 = measTables.getReportTable().iterator();
			while (iter2.hasNext()) {
				MeasurementCfgReport obj = iter2.next();
				if(exp_obj.getEvent_trigger()==obj.getEvent_trigger()){
					iter.remove();
					iter2.remove();
					break;
				}
			}
		}
		if(measTables.getReportTable().isEmpty() && expectedTables.getReportTable().isEmpty()){
			return true;
		}
		return false;
	}
	
	private boolean checkCfg(){
		Iterator<MeasurementCfg> iter = expectedTables.getCfgTable().iterator();
		while (iter.hasNext()) {
			MeasurementCfg exp_obj = iter.next();
			Iterator<MeasurementCfg> iter2 = measTables.getCfgTable().iterator();
			while (iter2.hasNext()) {
				MeasurementCfg obj = iter2.next();
				if(hashExpectedObject.get(exp_obj.getObject_id()).equals(hashRealObject.get(obj.getObject_id())) 
						&&  hashExpectedReport.get(exp_obj.getReport_id())==hashRealReport.get(obj.getReport_id())) {
					iter.remove();
					iter2.remove();
					break;
				}
			}
		}
		if(measTables.getCfgTable().isEmpty() && expectedTables.getCfgTable().isEmpty()){
			return true;
		}
		return false;
	}
	
	private void createRealHashObject(){
		hashRealObject = new HashMap<Integer, Integer>();
		for(MeasurementCfgObject obj : measTables.getObjectTable()){
			hashRealObject.put(obj.getId(), obj.getEarfcn());
		}
	}
	
	private void createRealHashReport(){
		hashRealReport = new HashMap<Integer, TriggerEvents>();
		for(MeasurementCfgReport obj : measTables.getReportTable()){
			hashRealReport.put(obj.getId(), obj.getEvent_trigger());
		}
	}
	
	private void createExpectedHashObject(){
		hashExpectedObject = new HashMap<Integer, Integer>();
		for(MeasurementCfgObject obj : expectedTables.getObjectTable()){
			hashExpectedObject.put(obj.getId(), obj.getEarfcn());
		}
	}
	
	private void createExpectedHashReport(){
		hashExpectedReport = new HashMap<Integer, TriggerEvents>();
		for(MeasurementCfgReport obj : expectedTables.getReportTable()){
			hashExpectedReport.put(obj.getId(), obj.getEvent_trigger());
		}
	}
	
	private double getMeasCurrentUe(){
		double ret = 0;
		try{
			ret = dm.getMeas().servingMeas.meas.meas1/100.0;			
		}catch(Exception e){
			e.printStackTrace();
		}
		return ret;
	}
	
	private boolean changeAttBelowA2(){
		int trigger = dutInTest.getRSRPEventTriggerGaps();
		if(trigger==GeneralUtils.ERROR_VALUE){
			return false;
		}
		int maxVal = attenuatorSetUnderTest.getMaxAttenuation();
		int step = attenuatorSetUnderTest.getAttenuationStep();
		float attenuatorsCurrentValue = attenuatorSetUnderTest.getAttenuation()[0];
		double rsrp = getMeasCurrentUe();
		report.report("Attenuator's start value: "+attenuatorsCurrentValue);
		report.report("RSRP of UE: "+rsrp);
		while(rsrp>=trigger-2 && attenuatorsCurrentValue+step<=maxVal){
			attenuatorsCurrentValue+=step;
			if(peripheralsConfig.setAttenuatorSetValue(attenuatorSetUnderTest, attenuatorsCurrentValue)){
				report.report("Changed attenuator's value to "+attenuatorsCurrentValue);					
			}else{
				report.report("Failed to change attenuator's value to "+attenuatorsCurrentValue, Reporter.WARNING);
			}			
			GeneralUtils.unSafeSleep(attenuatorStepTime);
			rsrp = getMeasCurrentUe();
			report.report("RSRP of UE: "+rsrp);
		}
		if(!(rsrp<trigger-2)){
			return false;
		}
		return true;
	}
	
	private boolean changeAttOverA1(){
		int trigger = dutInTest.getRSRPEventStopGaps();
		if(trigger==GeneralUtils.ERROR_VALUE){
			return false;
		}
		int minVal = attenuatorSetUnderTest.getMinAttenuation();
		int step = attenuatorSetUnderTest.getAttenuationStep();
		float attenuatorsCurrentValue = attenuatorSetUnderTest.getAttenuation()[0];
		double rsrp = getMeasCurrentUe();
		report.report("Attenuator's start value: "+attenuatorsCurrentValue);
		report.report("RSRP of UE: "+rsrp);
		while(rsrp<=trigger+2 && attenuatorsCurrentValue-step>=minVal){
			attenuatorsCurrentValue-=step;
			if(peripheralsConfig.setAttenuatorSetValue(attenuatorSetUnderTest, attenuatorsCurrentValue)){
				report.report("Changed attenuator's value to "+attenuatorsCurrentValue);					
			}else{
				report.report("Failed to change attenuator's value to "+attenuatorsCurrentValue, Reporter.WARNING);
			}		
			GeneralUtils.unSafeSleep(attenuatorStepTime);
			rsrp = getMeasCurrentUe();
			report.report("RSRP of UE: "+rsrp);
			
		}
		if(!(rsrp>trigger+2)){
			return false;
		}
		return true;
	}
	
	private boolean changeAttBelowA2AllUEs(ArrayList<UE> arrayUEs) {
		int trigger = dutInTest.getRSRPEventTriggerGaps();
		if(trigger==GeneralUtils.ERROR_VALUE){
			return false;
		}
		int maxVal = attenuatorSetUnderTest.getMaxAttenuation();
		int step = attenuatorSetUnderTest.getAttenuationStep();
		float attenuatorsCurrentValue = attenuatorSetUnderTest.getAttenuation()[0];
		report.report("Attenuator's start value: "+attenuatorsCurrentValue);
		while(!allUEsBelowA2(arrayUEs,trigger-2) && attenuatorsCurrentValue+step<=maxVal){
			attenuatorsCurrentValue+=step;
			if(peripheralsConfig.setAttenuatorSetValue(attenuatorSetUnderTest, attenuatorsCurrentValue)){
				report.report("Changed attenuator's value to "+attenuatorsCurrentValue);					
			}else{
				report.report("Failed to change attenuator's value to "+attenuatorsCurrentValue, Reporter.WARNING);
			}		
			GeneralUtils.unSafeSleep(attenuatorStepTime);
		}
		report.report("Checking if RF conditions below A2 were achieved");
		if(!allUEsBelowA2(arrayUEs,trigger-2)){
			report.report("RF conditions below A2 were NOT achieved",Reporter.FAIL);
			return false;
		}
		report.report("RF conditions below A2 were achieved");
		return true;
	}

	private boolean allUEsBelowA2(ArrayList<UE> arrayUEs, int trigger) {
		boolean flag = true;
		for(UE ue:arrayUEs){
			double rsrp = ue.getRSRP(0);
			report.report("Rsrp of UE "+ue.getLanIpAddress()+": "+rsrp);
			if(rsrp>trigger || rsrp == GeneralUtils.ERROR_VALUE){
				flag = false;
			}
		}
		return flag;
	}

	private boolean changeAttOverA1AllUEs(ArrayList<UE> arrayUEs) {
		int trigger = dutInTest.getRSRPEventStopGaps();
		if(trigger==GeneralUtils.ERROR_VALUE){
			return false;
		}
		int minVal = attenuatorSetUnderTest.getMinAttenuation();
		int step = attenuatorSetUnderTest.getAttenuationStep();
		float attenuatorsCurrentValue = attenuatorSetUnderTest.getAttenuation()[0];
		report.report("Attenuator's start value: "+attenuatorsCurrentValue);
		while(!allUEsOverA1(arrayUEs,trigger+2) && attenuatorsCurrentValue-step>=minVal){
			attenuatorsCurrentValue-=step;
			if(peripheralsConfig.setAttenuatorSetValue(attenuatorSetUnderTest, attenuatorsCurrentValue)){
				report.report("Changed attenuator's value to "+attenuatorsCurrentValue);					
			}else{
				report.report("Failed to change attenuator's value to "+attenuatorsCurrentValue, Reporter.WARNING);
			}				
			GeneralUtils.unSafeSleep(attenuatorStepTime);	
		}
		report.report("Checking if RF conditions over A1 were achieved");
		if(!allUEsOverA1(arrayUEs,trigger+2)){
			report.report("RF conditions over A1 were NOT achieved",Reporter.FAIL);
			return false;
		}
		report.report("RF conditions over A1 were achieved");
		return true;
	}

	private boolean allUEsOverA1(ArrayList<UE> arrayUEs, int trigger) {
		boolean flag = true;
		for(UE ue:arrayUEs){
			double rsrp = ue.getRSRP(0);
			report.report("Rsrp of UE "+ue.getLanIpAddress()+": "+rsrp);
			if(rsrp<trigger || rsrp == GeneralUtils.ERROR_VALUE){
				flag = false;
			}
		}
		return flag;
	}
	
	/**
	 * getting tables from DM tool into Test parameter - measTables
	 * @param dm
	 */
	private void getTablesFromUEToLocalTable(DMtool dm){
		measTables.setObejctTable(dm.getMeasurementCfgObject());
		measTables.setReportTable(dm.getMeasurementCfgReport());
		measTables.setCFGTable(dm.getMeasurementCfg());
	}
	
	private boolean checkIfEsonServerConfigured(){
		return enodeBConfig.checkIfEsonServerConfigured(dutInTest);
	}
	
	private boolean changeNetworkProfile(NetworkParameters params){
		boolean action = false;
		action = enodeBConfig.changeNetworkProfile(dutInTest, params);
		if(!action){
			return false;
		}
		return true;
	}
	
	private void configureNetworkWithEsonServer(NetworkParameters params){
		params.setcSonConfig(true, esonServer.getServerIp(), 2050);
		
	}
	
	private void configureNetworkWithCdrx(NetworkParameters params){
		params.setCdrxConnectedMode(EnabledDisabledStates.ENABLED);
	}
	
	private void objectToReport(ArrayList<MeasurementCfgObject> object){
		GeneralUtils.startLevel("Measurement Cfg Object Table");
		for(MeasurementCfgObject obj : object){
			report.report("Id: "+obj.getId()+", Arfcn: "+obj.getEarfcn());
		}
		GeneralUtils.stopLevel();
	}

	private void reportToReport(ArrayList<MeasurementCfgReport> report_obj){
		GeneralUtils.startLevel("Measurement Cfg Report Table");
		for(MeasurementCfgReport obj : report_obj){
			report.report("Id: "+obj.getId()+", Event Trigger: "+obj.getEvent_trigger());
		}
		GeneralUtils.stopLevel();
	}
	
	private void cfgToReport(ArrayList<MeasurementCfg> cfg){
		GeneralUtils.startLevel("Measurement Cfg Table");
		for(MeasurementCfg obj : cfg){
			report.report("Id: "+obj.getId()+", Object Id: "+obj.getObject_id()+", Report Id: "+obj.getReport_id());
		}
		GeneralUtils.stopLevel();
	}
	
	private void printTablesToReport(ArrayList<MeasurementCfgObject> object, ArrayList<MeasurementCfgReport> report_obj, ArrayList<MeasurementCfg> cfg){
		objectToReport(object);
		reportToReport(report_obj);
		cfgToReport(cfg);
	}
	
	private void createExpectedTablesTest1() {
		//just empty tables
	}

	private void createExpectedTablesTest2() {
		expectedTables.setObjectQuery(1,earfcn,0,0,"0","0");
		expectedTables.setObjectQuery(2,middleEarfcn,0,0,"0","0");
		expectedTables.setObjectQuery(3,interEarfcn,0,0,"0","0");
		
		expectedTables.setReportQuery(1,TriggerEvents.EVENT_A3,"0","0",0,"0","0","0","0");
		expectedTables.setReportQuery(2,TriggerEvents.EVENT_A3,"0","0",0,"0","0","0","0");
		
		expectedTables.setCfgQuery(1,1,1);
		expectedTables.setCfgQuery(2,2,2);
		expectedTables.setCfgQuery(3,3,2);
	}
	
	private void createExpectedTablesTest3() {
		expectedTables.setObjectQuery(1,earfcn,0,0,"0","0");
		
		expectedTables.setReportQuery(1,TriggerEvents.EVENT_A3,"0","0",0,"0","0","0","0");
		
		expectedTables.setCfgQuery(1,1,1);
	}
	
	private void createExpectedTablesTest4() {
		expectedTables.setObjectQuery(1,earfcn,0,0,"0","0");
		expectedTables.setObjectQuery(2,middleEarfcn,0,0,"0","0");

		expectedTables.setReportQuery(1,TriggerEvents.EVENT_A3,"0","0",0,"0","0","0","0");
		expectedTables.setReportQuery(2,TriggerEvents.EVENT_A3,"0","0",0,"0","0","0","0");
		expectedTables.setReportQuery(3,TriggerEvents.PERIODIC,"0","0",0,"0","0","0","0");
		
		expectedTables.setCfgQuery(1,1,1);
		expectedTables.setCfgQuery(2,1,3);
		expectedTables.setCfgQuery(3,2,2);
		expectedTables.setCfgQuery(4,2,3);
	}
	
	private void createExpectedTablesTest5() {
		expectedTables.setObjectQuery(1,earfcn,0,0,"0","0");

		expectedTables.setReportQuery(1,TriggerEvents.EVENT_A3,"0","0",0,"0","0","0","0");
		expectedTables.setReportQuery(2,TriggerEvents.EVENT_A2,"0","0",0,"0","0","0","0");
		
		expectedTables.setCfgQuery(1,1,1);
		expectedTables.setCfgQuery(2,1,2);
	}
	
	private void createExpectedTablesTest6() {
		expectedTables.setObjectQuery(1,earfcn,0,0,"0","0");

		expectedTables.setReportQuery(1,TriggerEvents.EVENT_A3,"0","0",0,"0","0","0","0");
		expectedTables.setReportQuery(2,TriggerEvents.EVENT_A2,"0","0",0,"0","0","0","0");
		expectedTables.setReportQuery(3,TriggerEvents.PERIODIC,"0","0",0,"0","0","0","0");
		
		expectedTables.setCfgQuery(1,1,1);
		expectedTables.setCfgQuery(2,1,2);
		expectedTables.setCfgQuery(3,1,3);
	}
	
	private void createExpectedTablesTest7() {
		createExpectedTablesTest4();
	}
	
	private void createExpectedTablesTest8() {
		expectedTables.setObjectQuery(1,earfcn,0,0,"0","0");
		expectedTables.setObjectQuery(2,middleEarfcn,0,0,"0","0");

		expectedTables.setReportQuery(1,TriggerEvents.EVENT_A3,"0","0",0,"0","0","0","0");
		expectedTables.setReportQuery(2,TriggerEvents.EVENT_A3,"0","0",0,"0","0","0","0");
		
		expectedTables.setCfgQuery(1,1,1);
		expectedTables.setCfgQuery(2,2,2);
	}
	
	private void createExpectedTablesTest9() {
		expectedTables.setObjectQuery(1,earfcn,0,0,"0","0");

		expectedTables.setReportQuery(1,TriggerEvents.EVENT_A3,"0","0",0,"0","0","0","0");
		expectedTables.setReportQuery(2,TriggerEvents.EVENT_A2,"0","0",0,"0","0","0","0");
		
		expectedTables.setCfgQuery(1,1,1);
		expectedTables.setCfgQuery(2,1,2);
	}
	
	private void createExpectedTablesTest10() {
		expectedTables.setObjectQuery(1,earfcn,0,0,"0","0");

		expectedTables.setReportQuery(1,TriggerEvents.EVENT_A3,"0","0",0,"0","0","0","0");
		expectedTables.setReportQuery(2,TriggerEvents.PERIODIC,"0","0",0,"0","0","0","0");
		expectedTables.setReportQuery(3,TriggerEvents.EVENT_A2,"0","0",0,"0","0","0","0");
		
		expectedTables.setCfgQuery(1,1,1);
		expectedTables.setCfgQuery(2,1,2);
		expectedTables.setCfgQuery(3,1,3);
	}
	
	private void createExpectedTablesTest11() {
		createExpectedTablesTest10();
	}
	
	private void createExpectedTablesTest12() {
		expectedTables.setObjectQuery(1,earfcn,0,0,"0","0");
		expectedTables.setObjectQuery(2,middleEarfcn,0,0,"0","0");

		expectedTables.setReportQuery(1,TriggerEvents.EVENT_A3,"0","0",0,"0","0","0","0");
		expectedTables.setReportQuery(2,TriggerEvents.EVENT_A3,"0","0",0,"0","0","0","0");
		expectedTables.setReportQuery(3,TriggerEvents.EVENT_A1,"0","0",0,"0","0","0","0");
		
		expectedTables.setCfgQuery(1,1,1);
		expectedTables.setCfgQuery(2,1,3);
		expectedTables.setCfgQuery(3,2,2);
	}
	
	private void createExpectedTablesTest13() {
		expectedTables.setObjectQuery(1,earfcn,0,0,"0","0");
		expectedTables.setObjectQuery(2,middleEarfcn,0,0,"0","0");

		expectedTables.setReportQuery(1,TriggerEvents.EVENT_A3,"0","0",0,"0","0","0","0");
		expectedTables.setReportQuery(2,TriggerEvents.EVENT_A2,"0","0",0,"0","0","0","0");
		
		expectedTables.setCfgQuery(1,1,1);
		expectedTables.setCfgQuery(2,1,2);
	}
	
	private void createExpectedTablesTest14() {
		expectedTables.setObjectQuery(1,earfcn,0,0,"0","0");
		expectedTables.setObjectQuery(2,middleEarfcn,0,0,"0","0");

		expectedTables.setReportQuery(1,TriggerEvents.EVENT_A3,"0","0",0,"0","0","0","0");
		expectedTables.setReportQuery(2,TriggerEvents.EVENT_A3,"0","0",0,"0","0","0","0");
		expectedTables.setReportQuery(3,TriggerEvents.EVENT_A1,"0","0",0,"0","0","0","0");
		expectedTables.setReportQuery(4,TriggerEvents.PERIODIC,"0","0",0,"0","0","0","0");
		
		expectedTables.setCfgQuery(1,1,1);
		expectedTables.setCfgQuery(2,1,3);
		expectedTables.setCfgQuery(3,1,4);
		expectedTables.setCfgQuery(4,2,2);
	}
	
	private void createExpectedTablesTest15() {
		expectedTables.setObjectQuery(1,earfcn,0,0,"0","0");
		expectedTables.setObjectQuery(2,middleEarfcn,0,0,"0","0");

		expectedTables.setReportQuery(1,TriggerEvents.EVENT_A3,"0","0",0,"0","0","0","0");
		expectedTables.setReportQuery(2,TriggerEvents.EVENT_A2,"0","0",0,"0","0","0","0");
		expectedTables.setReportQuery(3,TriggerEvents.PERIODIC,"0","0",0,"0","0","0","0");
		
		expectedTables.setCfgQuery(1,1,1);
		expectedTables.setCfgQuery(2,1,2);
		expectedTables.setCfgQuery(3,1,3);
	}

	private void createExpectedTablesTest16(){
		expectedTables.setObjectQuery(1,earfcn,0,0,"0","0");
		expectedTables.setObjectQuery(2,middleEarfcn,0,0,"0","0");

		expectedTables.setReportQuery(1,TriggerEvents.EVENT_A3,"0","0",0,"0","0","0","0");
		expectedTables.setReportQuery(2,TriggerEvents.PERIODIC,"0","0",0,"0","0","0","0");
		expectedTables.setReportQuery(3,TriggerEvents.EVENT_A2,"0","0",0,"0","0","0","0");
		
		expectedTables.setCfgQuery(1,1,1);
		expectedTables.setCfgQuery(2,1,2);
		expectedTables.setCfgQuery(3,1,3);
	}
	
	private void createExpectedTablesTest17(){
		expectedTables.setObjectQuery(1,earfcn,0,0,"0","0");
		expectedTables.setObjectQuery(2,middleEarfcn,0,0,"0","0");

		expectedTables.setReportQuery(1,TriggerEvents.EVENT_A3,"0","0",0,"0","0","0","0");
		expectedTables.setReportQuery(2,TriggerEvents.EVENT_A3,"0","0",0,"0","0","0","0");
		expectedTables.setReportQuery(3,TriggerEvents.PERIODIC,"0","0",0,"0","0","0","0");
		expectedTables.setReportQuery(4,TriggerEvents.EVENT_A1,"0","0",0,"0","0","0","0");
		
		expectedTables.setCfgQuery(1,1,1);
		expectedTables.setCfgQuery(2,1,3);
		expectedTables.setCfgQuery(3,1,4);
		expectedTables.setCfgQuery(4,2,2);
		expectedTables.setCfgQuery(5,2,3);
	}
	
	private void createExpectedTablesTest18(){
		expectedTables.setObjectQuery(1,earfcn,0,0,"0","0");
		expectedTables.setObjectQuery(2,middleEarfcn,0,0,"0","0");

		expectedTables.setReportQuery(1,TriggerEvents.EVENT_A3,"0","0",0,"0","0","0","0");
		expectedTables.setReportQuery(2,TriggerEvents.PERIODIC,"0","0",0,"0","0","0","0");
		expectedTables.setReportQuery(3,TriggerEvents.EVENT_A2,"0","0",0,"0","0","0","0");
		
		expectedTables.setCfgQuery(1,1,1);
		expectedTables.setCfgQuery(2,1,2);
		expectedTables.setCfgQuery(3,1,3);
	}
	
	private void createExpectedTablesTest19(){
		expectedTables = null;
		expectedTables = new TableManager();
		expectedTables.setObjectQuery(1,earfcn,0,0,"0","0");
		expectedTables.setObjectQuery(2,middleEarfcn,0,0,"0","0");

		expectedTables.setReportQuery(1,TriggerEvents.EVENT_A3,"0","0",0,"0","0","0","0");
		expectedTables.setReportQuery(2,TriggerEvents.EVENT_A3,"0","0",0,"0","0","0","0");
		expectedTables.setReportQuery(3,TriggerEvents.PERIODIC,"0","0",0,"0","0","0","0");
		
		expectedTables.setCfgQuery(1,1,1);
		expectedTables.setCfgQuery(2,1,3);
		expectedTables.setCfgQuery(3,2,2);
		expectedTables.setCfgQuery(4,2,3);
	}
	
	private void createExpectedTablesTest20(){
		expectedTables = null;
		expectedTables = new TableManager();
		expectedTables.setObjectQuery(1,earfcn,0,0,"0","0");
		expectedTables.setObjectQuery(2,middleEarfcn,0,0,"0","0");

		expectedTables.setReportQuery(1,TriggerEvents.EVENT_A3,"0","0",0,"0","0","0","0");
		expectedTables.setReportQuery(2,TriggerEvents.EVENT_A3,"0","0",0,"0","0","0","0");
		expectedTables.setReportQuery(3,TriggerEvents.PERIODIC,"0","0",0,"0","0","0","0");
		expectedTables.setReportQuery(4,TriggerEvents.EVENT_A2,"0","0",0,"0","0","0","0");
		
		expectedTables.setCfgQuery(1,1,1);
		expectedTables.setCfgQuery(2,1,3);
		expectedTables.setCfgQuery(3,1,4);
	}
	
	private void createExpectedTablesTest21(){
		expectedTables = null;
		expectedTables = new TableManager();
		expectedTables.setObjectQuery(1,earfcn,0,0,"0","0");
		expectedTables.setObjectQuery(2,middleEarfcn,0,0,"0","0");

		expectedTables.setReportQuery(1,TriggerEvents.EVENT_A3,"0","0",0,"0","0","0","0");
		expectedTables.setReportQuery(2,TriggerEvents.EVENT_A3,"0","0",0,"0","0","0","0");
		expectedTables.setReportQuery(3,TriggerEvents.PERIODIC,"0","0",0,"0","0","0","0");
		expectedTables.setReportQuery(4,TriggerEvents.EVENT_A1,"0","0",0,"0","0","0","0");
		
		expectedTables.setCfgQuery(1,1,1);
		expectedTables.setCfgQuery(2,1,3);
		expectedTables.setCfgQuery(3,1,4);
		expectedTables.setCfgQuery(4,2,2);
	}
	
	private void createExpectedTablesTest22(){
		expectedTables = null;
		expectedTables = new TableManager();
		expectedTables.setObjectQuery(1,earfcn,0,0,"0","0");
		expectedTables.setObjectQuery(2,middleEarfcn,0,0,"0","0");

		expectedTables.setReportQuery(1,TriggerEvents.EVENT_A3,"0","0",0,"0","0","0","0");
		expectedTables.setReportQuery(2,TriggerEvents.EVENT_A3,"0","0",0,"0","0","0","0");
		expectedTables.setReportQuery(3,TriggerEvents.PERIODIC,"0","0",0,"0","0","0","0");
		expectedTables.setReportQuery(4,TriggerEvents.EVENT_A2,"0","0",0,"0","0","0","0");
		
		expectedTables.setCfgQuery(1,1,1);
		expectedTables.setCfgQuery(2,1,3);
		expectedTables.setCfgQuery(3,1,4);
	}
	
	private void createExpectedTablesTest23(){
		expectedTables.setObjectQuery(1,earfcn,0,0,"0","0");
		
		expectedTables.setReportQuery(1,TriggerEvents.EVENT_A3,"0","0",0,"0","0","0","0");
		expectedTables.setReportQuery(2,TriggerEvents.EVENT_A2,"0","0",0,"0","0","0","0");
		expectedTables.setReportQuery(3,TriggerEvents.EVENT_A2,"0","0",0,"0","0","0","0");
		
		expectedTables.setCfgQuery(1,1,1);
		expectedTables.setCfgQuery(2,1,2);
		expectedTables.setCfgQuery(3,1,3);
	}
	
	private void createExpectedTablesTest24(){
		expectedTables.setObjectQuery(1,earfcn,0,0,"0","0");
		expectedTables.setObjectQuery(2,middleEarfcn,0,0,"0","0");
		
		expectedTables.setReportQuery(1,TriggerEvents.EVENT_A3,"0","0",0,"0","0","0","0");
		expectedTables.setReportQuery(2,TriggerEvents.EVENT_A2,"0","0",0,"0","0","0","0");
		expectedTables.setReportQuery(3,TriggerEvents.EVENT_A2,"0","0",0,"0","0","0","0");
		
		expectedTables.setCfgQuery(1,1,1);
		expectedTables.setCfgQuery(2,1,2);
		expectedTables.setCfgQuery(3,1,3);
	}
	
	private void createExpectedTablesTest25(){
		expectedTables.setObjectQuery(1,earfcn,0,0,"0","0");
		expectedTables.setObjectQuery(2,middleEarfcn,0,0,"0","0");

		expectedTables.setReportQuery(1,TriggerEvents.EVENT_A3,"0","0",0,"0","0","0","0");
		expectedTables.setReportQuery(2,TriggerEvents.EVENT_A3,"0","0",0,"0","0","0","0");
		expectedTables.setReportQuery(3,TriggerEvents.EVENT_A1,"0","0",0,"0","0","0","0");
		
		expectedTables.setCfgQuery(1,1,1);
		expectedTables.setCfgQuery(2,1,3);
		expectedTables.setCfgQuery(3,2,2);
	}
	
	private void createExpectedTablesTest26(){
		expectedTables.setObjectQuery(1,earfcn,0,0,"0","0");
		expectedTables.setObjectQuery(2,middleEarfcn,0,0,"0","0");

		expectedTables.setReportQuery(1,TriggerEvents.EVENT_A3,"0","0",0,"0","0","0","0");
		expectedTables.setReportQuery(2,TriggerEvents.PERIODIC,"0","0",0,"0","0","0","0");
		expectedTables.setReportQuery(3,TriggerEvents.EVENT_A2,"0","0",0,"0","0","0","0");
		expectedTables.setReportQuery(4,TriggerEvents.EVENT_A2,"0","0",0,"0","0","0","0");
		
		expectedTables.setCfgQuery(1,1,1);
		expectedTables.setCfgQuery(2,1,2);
		expectedTables.setCfgQuery(3,1,3);
		expectedTables.setCfgQuery(4,1,4);
	}
	
	private void createExpectedTablesTest27() {
		createExpectedTablesTest6();
	}
	
	private void createExpectedTablesTest28(){
		expectedTables.setObjectQuery(1,earfcn,0,0,"0","0");
		expectedTables.setObjectQuery(2,middleEarfcn,0,0,"0","0");

		expectedTables.setReportQuery(1,TriggerEvents.EVENT_A3,"0","0",0,"0","0","0","0");
		expectedTables.setReportQuery(2,TriggerEvents.EVENT_A3,"0","0",0,"0","0","0","0");
		
		expectedTables.setCfgQuery(1,1,1);
		expectedTables.setCfgQuery(2,2,2);
	}
	
	private void createExpectedTablesTest29(){
		expectedTables.setObjectQuery(1,earfcn,0,0,"0","0");
		expectedTables.setObjectQuery(2,interEarfcn,0,0,"0","0");

		expectedTables.setReportQuery(1,TriggerEvents.EVENT_A3,"0","0",0,"0","0","0","0");
		expectedTables.setReportQuery(2,TriggerEvents.EVENT_A3,"0","0",0,"0","0","0","0");
		expectedTables.setReportQuery(3,TriggerEvents.PERIODIC,"0","0",0,"0","0","0","0");
		
		expectedTables.setCfgQuery(1,1,1);
		expectedTables.setCfgQuery(2,1,3);
		expectedTables.setCfgQuery(3,2,2);
		expectedTables.setCfgQuery(4,2,3);
	}
	
	private void createExpectedTablesTest30() {
		expectedTables.setObjectQuery(1,earfcn,0,0,"0","0");
		expectedTables.setObjectQuery(2,middleEarfcn,0,0,"0","0");
		
		expectedTables.setReportQuery(1,TriggerEvents.EVENT_A3,"0","0",0,"0","0","0","0");
		expectedTables.setReportQuery(2,TriggerEvents.EVENT_A3,"0","0",0,"0","0","0","0");
		expectedTables.setReportQuery(3,TriggerEvents.PERIODIC,"0","0",0,"0","0","0","0");
		
		expectedTables.setCfgQuery(1,1,1);
		expectedTables.setCfgQuery(2,1,3);
		expectedTables.setCfgQuery(3,2,2);
	}
	
	private void createExpectedTablesTest31() {
		expectedTables.setObjectQuery(1,earfcn,0,0,"0","0");
		expectedTables.setObjectQuery(2,middleEarfcn,0,0,"0","0");
		
		expectedTables.setReportQuery(1,TriggerEvents.EVENT_A3,"0","0",0,"0","0","0","0");
		expectedTables.setReportQuery(2,TriggerEvents.EVENT_A3,"0","0",0,"0","0","0","0");
		
		expectedTables.setCfgQuery(1,1,1);
		expectedTables.setCfgQuery(2,2,2);
	}
	
	private void createExpectedTablesTest32() {
		expectedTables.setObjectQuery(1,earfcn,0,0,"0","0");
		expectedTables.setObjectQuery(2,middleEarfcn,0,0,"0","0");
		
		expectedTables.setReportQuery(1,TriggerEvents.EVENT_A3,"0","0",0,"0","0","0","0");
		expectedTables.setReportQuery(2,TriggerEvents.EVENT_A3,"0","0",0,"0","0","0","0");
		expectedTables.setReportQuery(3,TriggerEvents.PERIODIC,"0","0",0,"0","0","0","0");
		
		expectedTables.setCfgQuery(1,1,1);
		expectedTables.setCfgQuery(2,1,3);
		expectedTables.setCfgQuery(3,2,2);
		expectedTables.setCfgQuery(4,2,3);
	}
	
	private void createExpectedTablesTest33() {
		createExpectedTablesTest30();
	}
	
	private void createExpectedTablesTest34() {
		createExpectedTablesTest6();
	}
	
	private void createExpectedTablesTest35() {
		createExpectedTablesTest32();
	}
	
	private void createExpectedTablesTest36() {
		createExpectedTablesTest6();
	}
	
	private void createExpectedTablesTest37() {
		createExpectedTablesTest6();
	}
	
	private void createExpectedTablesTest38() {
		expectedTables.setObjectQuery(1,earfcn,0,0,"0","0");
		expectedTables.setObjectQuery(2,middleEarfcn,0,0,"0","0");

		expectedTables.setReportQuery(1,TriggerEvents.EVENT_A3,"0","0",0,"0","0","0","0");
		expectedTables.setReportQuery(2,TriggerEvents.EVENT_A3,"0","0",0,"0","0","0","0");
		expectedTables.setReportQuery(3,TriggerEvents.EVENT_A1,"0","0",0,"0","0","0","0");
		expectedTables.setReportQuery(4,TriggerEvents.PERIODIC,"0","0",0,"0","0","0","0");
		
		expectedTables.setCfgQuery(1,1,1);
		expectedTables.setCfgQuery(2,1,3);
		expectedTables.setCfgQuery(3,1,4);
		expectedTables.setCfgQuery(4,2,2);
	}
	
	private void createExpectedTablesTest39(){
		createExpectedTablesTest15();
	}
	
	private void createExpectedTablesTest40(){
		createExpectedTablesTest15();
	}
	
	private void createExpectedTablesTest41(){
		expectedTables.setObjectQuery(1,earfcn,0,0,"0","0");
		expectedTables.setObjectQuery(2,middleEarfcn,0,0,"0","0");

		expectedTables.setReportQuery(1,TriggerEvents.EVENT_A3,"0","0",0,"0","0","0","0");
		expectedTables.setReportQuery(2,TriggerEvents.EVENT_A3,"0","0",0,"0","0","0","0");
		expectedTables.setReportQuery(3,TriggerEvents.PERIODIC,"0","0",0,"0","0","0","0");
		expectedTables.setReportQuery(4,TriggerEvents.EVENT_A1,"0","0",0,"0","0","0","0");
		
		expectedTables.setCfgQuery(1,1,1);
		expectedTables.setCfgQuery(2,1,3);
		expectedTables.setCfgQuery(3,1,4);
		expectedTables.setCfgQuery(4,2,2);
		expectedTables.setCfgQuery(5,2,3);
	}
	
	private void createExpectedTablesTest42(){
		createExpectedTablesTest15();
	}
	
	private void createExpectedTablesTest43(){
		expectedTables = null;
		expectedTables = new TableManager();
		createExpectedTablesTest32();
	}
	
	private void createExpectedTablesTest44(){
		createExpectedTablesTest22();
	}
	
	private void createExpectedTablesTest45(){
		expectedTables = null;
		expectedTables = new TableManager();
		expectedTables.setObjectQuery(1,earfcn,0,0,"0","0");
		expectedTables.setObjectQuery(2,middleEarfcn,0,0,"0","0");

		expectedTables.setReportQuery(1,TriggerEvents.EVENT_A3,"0","0",0,"0","0","0","0");
		expectedTables.setReportQuery(2,TriggerEvents.EVENT_A3,"0","0",0,"0","0","0","0");
		expectedTables.setReportQuery(3,TriggerEvents.PERIODIC,"0","0",0,"0","0","0","0");
		expectedTables.setReportQuery(4,TriggerEvents.EVENT_A1,"0","0",0,"0","0","0","0");
		
		expectedTables.setCfgQuery(1,1,1);
		expectedTables.setCfgQuery(2,1,3);
		expectedTables.setCfgQuery(3,1,4);
		expectedTables.setCfgQuery(4,2,2);
	}
	
	private void createExpectedTablesTest46(){
		createExpectedTablesTest22();
	}
	
	private void createExpectedTablesTest47(){
		createExpectedTablesTest26();
	}
	
	private enum Threshold{
		Over_A1,
		Below_A2;
	}

	@ParameterProperties(description = "Name of Enodeb Which the test will be run On")
	public void setDUT(String dut) {
		this.dutInTest = (EnodeB) SysObjUtils.getInstnce().initSystemObject(EnodeB.class,false,dut).get(0);
	}
	
	public String getDUT() {
		return this.dutInTest.getNetspanName();
	}

	@ParameterProperties(description = "Name of Enodeb Which the test will be run On")
	public void setDUT2(String dut) {
		this.dut2 = (EnodeB) SysObjUtils.getInstnce().initSystemObject(EnodeB.class,false,dut).get(0);
	}
	
	public String getDUT2() {
		return this.dut2.getNetspanName();
	}
	
	@Override
	public void end(){
		if(attenuatorRespond){
			commandsThreadDut1.stopCommands();
			commandsThreadDut1.moveFileToReporterAndAddLink();
			commandsThreadDut2.stopCommands();
			commandsThreadDut2.moveFileToReporterAndAddLink();
			GeneralUtils.startLevel("Post Test");
			report.report("Stop traffic");
			traffic.stopTraffic();
			
			report.report("Trying to delete neighbors and 3rd party EnodeBs");
			neighbors.deleteAllNeighbors(dutInTest);
			neighbors.delete3rdPartyList(virtualNeighborsList);
			
			GeneralUtils.unSafeSleep(5*1000);
			report.reportHtml("enb show ngh for "+dutInTest.getName(), dutInTest.lteCli("enb show ngh"), true);
			report.reportHtml("db get nghlist for "+dutInTest.getName(), dutInTest.lteCli("db get nghlist"), true);
			
			report.reportHtml("enb show ngh for "+dut2.getName(), dut2.lteCli("enb show ngh"), true);
			report.reportHtml("db get nghlist for "+dut2.getName(), dut2.lteCli("db get nghlist"), true);
			
			peripheralsConfig.stopUEsOnlySnmp(allArrayUEs);
			report.report("Set attenuator to default value");
			peripheralsConfig.setAttenuatorSetValue(attenuatorSetUnderTest, attenuatorSetUnderTest.getDefaultValueAttenuation());
			
			if(dutInTest!=null){
				enodeBConfig.setProfile(dutInTest, EnbProfiles.Son_Profile, dutInTest.getDefaultNetspanProfiles().getSON());
				revertMobilityProfiles(dutInTest);
				if(flagNetwork){
					enodeBConfig.setProfile(dutInTest, EnbProfiles.Network_Profile, dutInTest.getDefaultNetspanProfiles().getNetwork());
					if(needsToReboot){
						GeneralUtils.startLevel("Reboot EnodeB");
						dutInTest.reboot();
						dutInTest.waitForAllRunningAndInService(EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
						GeneralUtils.stopLevel();						
						needsToReboot = false;
					}
					flagNetwork = false;
				}
				enodeBConfig.deleteClonedProfiles();
			}
			if(originalRatio!=null){
				report.report("Setting anr ratio to original: "+originalRatio);
				dutInTest.setAnrLightRatio(originalRatio);
				originalRatio = null;
			}
			if(originalMinimum!=null){
				report.report("Setting anr min ues to original: "+originalMinimum);
				dutInTest.setAnrLightMinUes(originalMinimum);
				originalMinimum = null;
			}
			for(EnodeB enb : otherEnb){
				peripheralsConfig.changeEnbState(enb, EnbStates.IN_SERVICE);
			}
			
			if(otdoa){
				dutInTest.setOtdoaMode(false);
			}
			
			peripheralsConfig.startUEsOnlySnmp(allArrayUEs);
			GeneralUtils.stopLevel();
		}
		super.end();
	}
	
	private void revertMobilityProfiles(EnodeB dutInTest2) {
		int numOfCells = dutInTest2.getNumberOfActiveCells();
		while(numOfCells > 0) {
			dutInTest2.setCellContextNumber(numOfCells);
			enodeBConfig.setProfile(dutInTest2, EnbProfiles.Mobility_Profile, dutInTest2.getDefaultNetspanProfiles().getMobility());
			numOfCells--;
		}
	}

	//OTDOA TESTS
	@Test
	@TestProperties(name = "OTDOA1_BasicOperation", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void OTDOA1_BasicOperation() {
		
		if (!preTest(true)) {
			return;
		}
		
		createObjectExpectedTableOTDOA1();
		
		otdoa = true;
		genericTest(false, 0, false, false, 1, null, false, false, otdoa);
	}
		
	private void createObjectExpectedTableOTDOA1() {
		expectedTables.setObjectQuery(1,earfcn,0,0,"0","0");
		expectedTables.setObjectQuery(2,middleEarfcn,0,0,"0","0");
		
		expectedTables.setReportQuery(1,TriggerEvents.EVENT_A3,"0","0",0,"0","0","0","0");
		expectedTables.setReportQuery(2,TriggerEvents.EVENT_A3,"0","0",0,"0","0","0","0");
		
		expectedTables.setCfgQuery(1,1,1);
		expectedTables.setCfgQuery(2,2,2);
	}
		
	@Test
	@TestProperties(name = "OTDOA2_Basic Operation_No_Neighbors", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void OTDOA2_BasicOperation_NoNeighbors() {
		//enable otdoa
		if (!preTest(true)) {
			return;
		}
			
		createObjectExpectedTableOTDOA2();
		otdoa = true;
		genericTest(false, 0, false, false, 0, null, false, false, otdoa);
	}

	private void createObjectExpectedTableOTDOA2() {
//			expectedTables.setObjectQuery(1,earfcn,0,0,"0","0");
//			
//			expectedTables.setReportQuery(1,TriggerEvents.EVENT_A3,"0","0",0,"0","0","0","0");
//			
//			expectedTables.setCfgQuery(1,1,1);
	}
		
	@Test
	@TestProperties(name = "OTDOA3_Basic Operation_ANR_Periodic_1", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void OTDOA3_ANRPeriodic_Path1() {
		//enable otdoa
		if (!preTest(true)) {
			return;
		}
		
		createObjectExpectedTableOTDOA3();
		otdoa = true;
		sendOTDOACommandTest = false;
		genericTest(false, 2, false, false, 0, null, false, false, otdoa);
	}
	
	private void createObjectExpectedTableOTDOA3() {
		expectedTables.setObjectQuery(1,earfcn,0,0,"0","0");
		expectedTables.setObjectQuery(2,middleEarfcn,0,0,"0","0");
		
		expectedTables.setReportQuery(1,TriggerEvents.EVENT_A3,"0","0",0,"0","0","0","0");
		expectedTables.setReportQuery(2,TriggerEvents.EVENT_A3,"0","0",0,"0","0","0","0");
		expectedTables.setReportQuery(3,TriggerEvents.PERIODIC,"0","0",0,"0","0","0","0");
		
		expectedTables.setCfgQuery(1,1,1);
		expectedTables.setCfgQuery(2,2,2);
		expectedTables.setCfgQuery(3,1,3);
		expectedTables.setCfgQuery(4,2,3);
	}
		
	@Test
	@TestProperties(name = "OTDOA4_Basic Operation_ANR_Periodic_2", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void OTDOA4_ANRPeriodic_Path2() {
		//enable otdoa
		if (!preTest(true)) {
			return;
		}
			
		createObjectExpectedTableOTDOA4();
		otdoa = true;
		genericTest(false, 1, false, false, 1, null, false, false, otdoa);
	}
	
	private void createObjectExpectedTableOTDOA4() {
		expectedTables.setObjectQuery(1,earfcn,0,0,"0","0");
		expectedTables.setObjectQuery(2,middleEarfcn,0,0,"0","0");
		
		expectedTables.setReportQuery(1,TriggerEvents.EVENT_A3,"0","0",0,"0","0","0","0");
		expectedTables.setReportQuery(2,TriggerEvents.EVENT_A3,"0","0",0,"0","0","0","0");
		
		expectedTables.setCfgQuery(1,1,1);
		expectedTables.setCfgQuery(2,2,2);
	}
		
	@Test
	@TestProperties(name = "OTDOA5_Basic Operation_ANR_Periodic_3", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void OTDOA5_ANRPeriodic_Path3() {
		//enable otdoa
		if (!preTest(true)) {
			return;
		}
			
		createObjectExpectedTableOTDOA5();
		otdoa = true;
		trig = "1";
		genericTest(false, 2, false, false, 0, null, false, false, otdoa);
	}
		
	private void createObjectExpectedTableOTDOA5() {
		expectedTables.setObjectQuery(1,earfcn,0,0,"0","0");
		expectedTables.setObjectQuery(2,middleEarfcn,0,0,"0","0");
		
		expectedTables.setReportQuery(1,TriggerEvents.EVENT_A3,"0","0",0,"0","0","0","0");
		expectedTables.setReportQuery(2,TriggerEvents.EVENT_A3,"0","0",0,"0","0","0","0");
		expectedTables.setReportQuery(3,TriggerEvents.PERIODIC,"0","0",0,"0","0","0","0");
		
		expectedTables.setCfgQuery(1,1,1);
		expectedTables.setCfgQuery(2,2,2);
		expectedTables.setCfgQuery(3,1,3);
		expectedTables.setCfgQuery(4,2,3);
	}
				
	@Test
	@TestProperties(name = "OTDOA6_Basic Operation_SON Periodic_Phase 1", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void OTDOA6_BasicOperation_SonPeriodic_Path1() {
		//enable otdoa
		if (!preTest(true)) {
			return;
		}
		
		createObjectExpectedTableOTDOA6();
		otdoa = true;
		sendOTDOACommandTest = false;
		genericTest(true, 0, false, true, 1, null, false, false, otdoa);
	}
		
	private void createObjectExpectedTableOTDOA6() {
		expectedTables.setObjectQuery(1,earfcn,0,0,"0","0");
		
		expectedTables.setReportQuery(1,TriggerEvents.EVENT_A3,"0","0",0,"0","0","0","0");
		expectedTables.setReportQuery(2,TriggerEvents.EVENT_A2,"0","0",0,"0","0","0","0");
		expectedTables.setReportQuery(3,TriggerEvents.PERIODIC,"0","0",0,"0","0","0","0");
		
		expectedTables.setCfgQuery(1,1,1);
		expectedTables.setCfgQuery(2,1,2);
		expectedTables.setCfgQuery(3,1,3);
	}
		
	@Test
	@TestProperties(name = "OTDOA7_Basic Operation_SON Periodic_Phase 2", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void OTDOA7_BasicOperation_SonPeriodic_Path2() {
		//enable otdoa
		if (!preTest(true)) {
			return;
		}
			
		createObjectExpectedTableOTDOA7();
		otdoa = true;
		genericTest(true, 0, false, true, 1, null, false, false, otdoa);
	}
		
	private void createObjectExpectedTableOTDOA7() {
		expectedTables.setObjectQuery(1,earfcn,0,0,"0","0");
		
		expectedTables.setReportQuery(1,TriggerEvents.EVENT_A3,"0","0",0,"0","0","0","0");
		expectedTables.setReportQuery(2,TriggerEvents.EVENT_A2,"0","0",0,"0","0","0","0");
		expectedTables.setReportQuery(3,TriggerEvents.PERIODIC,"0","0",0,"0","0","0","0");
		
		expectedTables.setCfgQuery(1,1,1);
		expectedTables.setCfgQuery(2,1,2);
		expectedTables.setCfgQuery(3,1,3);
	}
		
	@Test
	@TestProperties(name = "OTDOA8_Basic Operation_SON Periodic_Phase 3", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void OTDOA8_BasicOperation_SonPeriodic_Path3() {
		//enable otdoa
		if (!preTest(true)) {
			return;
		}
		
		createObjectExpectedTableOTDOA8();
		otdoa = true;
		trig ="1";
		genericTest(true, 1, false, true, 1, null, false, false, otdoa);
	}
		
	private void createObjectExpectedTableOTDOA8() {
		expectedTables.setObjectQuery(1,earfcn,0,0,"0","0");
		
		expectedTables.setReportQuery(1,TriggerEvents.EVENT_A3,"0","0",0,"0","0","0","0");
		expectedTables.setReportQuery(2,TriggerEvents.EVENT_A2,"0","0",0,"0","0","0","0");
		expectedTables.setReportQuery(3,TriggerEvents.PERIODIC,"0","0",0,"0","0","0","0");
			
		expectedTables.setCfgQuery(1,1,1);
		expectedTables.setCfgQuery(2,1,2);
		expectedTables.setCfgQuery(3,1,3);
	}
	
	@Test
	@TestProperties(name = "OTDOA9_Recieiving A2 Report from UE", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void OTDOA9_Recieiving_A2_ReportFromUE() {
		//enable otdoa
		if (!preTest(true)) {
			return;
		}
		
		createObjectExpectedTableOTDOA9();
		otdoa = true;
		trig = "1";
		genericTest(false, 1, false, true, 1, Threshold.Below_A2, false, false, otdoa);
	}
		
	private void createObjectExpectedTableOTDOA9() {
		expectedTables.setObjectQuery(1,earfcn,0,0,"0","0");
		expectedTables.setObjectQuery(2,middleEarfcn,0,0,"0","0");
		
		expectedTables.setReportQuery(1,TriggerEvents.EVENT_A3,"0","0",0,"0","0","0","0");
		expectedTables.setReportQuery(2,TriggerEvents.EVENT_A3,"0","0",0,"0","0","0","0");
		expectedTables.setReportQuery(3,TriggerEvents.EVENT_A1,"0","0",0,"0","0","0","0");
		
		expectedTables.setCfgQuery(1,1,1);
		expectedTables.setCfgQuery(2,1,3);
		expectedTables.setCfgQuery(3,2,2);
	}
		
	@Test
	@TestProperties(name = "OTDOA10_Recieiving A1 Report from UE", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void OTDOA10_Recieiving_A1_ReportFromUE() {
		//enable otdoa
		if (!preTest(true)) {
			return;
		}
		
		createObjectExpectedTableOTDOA10();
		otdoa = true;
		trig = "1";
		genericTest(false, 1, false, true, 1, Threshold.Over_A1, false, false, otdoa);
	}
		
	private void createObjectExpectedTableOTDOA10() {
		expectedTables.setObjectQuery(1,earfcn,0,0,"0","0");
		expectedTables.setObjectQuery(2,middleEarfcn,0,0,"0","0");
		
		expectedTables.setReportQuery(1,TriggerEvents.EVENT_A3,"0","0",0,"0","0","0","0");
		expectedTables.setReportQuery(2,TriggerEvents.EVENT_A2,"0","0",0,"0","0","0","0");
		
		expectedTables.setCfgQuery(1,1,1);
		expectedTables.setCfgQuery(2,1,2);
	}
		
	private String otdoaCommand( ){
		return "ue simulate otdoameas cellIdx="+cellIndex+" offset="+offSet+" rnti="+rnti+" trig="+trig;
	}

	private void enableOTDOA() {
		dutInTest.setOtdoaMode(true);	
	}	

	/**
	 * updating rnti test global parameter for command "ue simulate otdoameas"
	 */
	private void getSingleUERNTI() {
		try{
			HashMap <String,Variable> a = dutInTest.getUEShowLinkTable();
			for(String key : a.keySet()){
				if(key.contains("1.3.6.1.4.1.989.1.20.1.4.75.1.1.0")){
					GeneralUtils.printToConsole("targeted mib in potential value : "+a.get(key).toString());
					rnti = a.get(key).toString();
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}

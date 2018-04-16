package testsNG.ProtocolsAndServices.ReEstablishment;

import java.util.ArrayList;
import java.util.List;

import DMTool.DMtool;
import EnodeB.EnodeB;
import Netspan.NetspanServer;
import Netspan.API.Enums.EnbStates;
import UE.UE;
import Utils.GeneralUtils;
import Utils.SetupUtils;
import Utils.SysObjUtils;
import jsystem.framework.report.Reporter;
import testsNG.TestspanTest;
import testsNG.Actions.PeripheralsConfig;
import testsNG.Actions.Traffic;
import testsNG.Actions.TrafficCapacity;

public class EstablishmentBase extends TestspanTest{

	protected EnodeB dut;
	protected EnodeB dut2;
	protected Traffic traffic;
	protected List<UE> ueList;
	protected List<DMtool> dmList;
	protected NetspanServer netspan;
	protected int numberOfUesInTest = 0;
	protected int numberOfTimeResCommandBeenSent = 0;
	protected PeripheralsConfig peripheralsConfig;
	protected String reEstablishmentCommand = "forceReestab";
	protected testCounters testCounters;
	
	@Override
	public void init() throws Exception{
		GeneralUtils.startLevel("Re Establishment Init");
		enbInTest = new ArrayList<>();
		enbInTest.add(dut);
		if(dut2 != null){
			enbInTest.add(dut2);
		}
		super.init();
		peripheralsConfig = PeripheralsConfig.getInstance();
		netspan = NetspanServer.getInstance();
		traffic = Traffic.getInstance(SetupUtils.getInstance().getAllUEs());
		testCounters = new testCounters();
		configureNodesState(EnbStates.OUT_OF_SERVICE);
		GeneralUtils.stopLevel();
	}
	
	/**
	 * setting the number of UEs to test according to number -> dynamics first.
	 * Delete all neighbors for nodes in test.
	 * reboot all UEs for UEs in test.
	 * init DM tool list for each UE.
	 * @param numOfUesInTest
	 */
	protected void preTest(int numOfUesInTest) {
		GeneralUtils.startLevel("Pre Test");
		deleteAllNeighborsForNodesInTest();
		rebootAllUEsInTest(numOfUesInTest);
		verifyNodesInTestDefaultProfilesInNetspan();
		initDmToolForEachUE(ueList); 					//dmList = new DM tool list
		GeneralUtils.stopLevel();
	}

	//verify default profiles in Netspan//
	protected void verifyNodesInTestDefaultProfilesInNetspan(){
		setDefaultProfilesToNode(dut);
		if(dut2 != null){
			setDefaultProfilesToNode(dut2);
		}
	}
	
	private void setDefaultProfilesToNode(EnodeB node){
		GeneralUtils.startLevel("setting default profiles to node : "+node.getNetspanName());
		netspan.checkAndSetDefaultProfiles(node,false);
		GeneralUtils.stopLevel();
	}
	
	//Init Dm Tool for each UE methods//
 	protected void initDmToolForEachUE(List<UE> ueList){
	 	GeneralUtils.startLevel("Init Dm Tool for each UE");
		dmList = new ArrayList<DMtool>();
		for(UE ue : ueList){ 
			dmList.add(initDmTool(ue));
		}
		GeneralUtils.stopLevel();
	}
	
	private DMtool initDmTool(UE ue){
		DMtool tool = new DMtool();
		GeneralUtils.printToConsole("init Dm Tool");
		try{
			GeneralUtils.printToConsole("init Dm Tool with Lan ip : "+ue.getLanIpAddress());
			tool.setUeIP(ue.getLanIpAddress());
			GeneralUtils.printToConsole("init Dm Tool with port : "+ue.getDMToolPort());
			tool.setPORT(ue.getDMToolPort());
			GeneralUtils.printToConsole("init Dm Tool init");
			tool.init();
		}catch(Exception e){
			report.report("Could not init Dm tool for UE : "+ue.getName());
			e.printStackTrace();
		}
		return tool;
	}
	
	//Delete all Neighbors methods.//
	protected void deleteAllNeighborsForNodesInTest(){
		deleteNeighborsForNode(dut);
		if(dut2 != null){
			deleteNeighborsForNode(dut);
		}		
	}
	
	protected boolean deleteNeighborsForNode(EnodeB node){
		boolean result = false;
		GeneralUtils.startLevel("Trying to Delete Neighbors for Node : "+node.getNetspanName());
		result = netspan.deleteAllNeighbors(dut);
		GeneralUtils.stopLevel();
		return result;
	}
	
	//UEs Reboot methods//
	protected void rebootAllUEsInTest(int numOfUesInTest) {
		GeneralUtils.startLevel("Reboot all UEs in Test");
		ueList = getUES(dut,numOfUesInTest);
		ueList.forEach( (ue)->ue.start() );
		ueList.forEach( (ue)->ue.reboot() );
		GeneralUtils.stopLevel();
	}
	
	private ArrayList<UE> getUES(EnodeB enb, int count){
		int i = 0;
		ArrayList<UE> retUes = new ArrayList<>();
		ArrayList<UE> staticUES = SetupUtils.getInstance().getStaticUEs(enb);
		ArrayList<UE> dynamicUES = SetupUtils.getInstance().getDynamicUEs();
		ArrayList<UE> tempUes = new ArrayList<>();
		if (dynamicUES != null)
			tempUes.addAll(dynamicUES);
		if (staticUES != null)
			tempUes.addAll(staticUES);
		
		for (UE ue : tempUes) {
			if (i == count)
				return retUes;
			retUes.add(ue);
			i++;
		}
		
		return retUes;
	}
	
	protected void configureNodesState(EnbStates state){
		if(enbInTest.size() == 1){
			for (EnodeB enodeB : enbInSetup) {
				if (enodeB != dut) {
					GeneralUtils.printToConsole("Setting state : "+ state +" to node : "+enodeB.getNetspanName());
					peripheralsConfig.changeEnbState(enodeB, state);
				}			
			}
		}
	}
	
	/**
	 * test Main Activity
	 */
	protected void reEstablishmentTest(){
		startTraffic();
		clearAllCounters();
		waitForOneGranularityPeriod();
		sendReEstablishmentCommandToAllUES();
		waitForOneGranularityPeriod();
	}
	
	protected void startTraffic(){
		try{
			GeneralUtils.startLevel("Start Traffic");
			traffic.startTraffic(TrafficCapacity.LOWTPT);			
		}catch(Exception e){
			report.report("Could not start Traffic",Reporter.WARNING);
		}
		finally{
			GeneralUtils.stopLevel();
		}
	}
	
	public void clearAllCounters() {
		GeneralUtils.startLevel("Reseting all Node Counters");
		report.report("kpi counters clearprotostats");
		dut.resetCounter(null, null, null);
		GeneralUtils.stopLevel();
	}
	
	protected void waitForOneGranularityPeriod(){
		GeneralUtils.startLevel("waiting one Granularity Period");
		int granularityTimeInMinutes = dut.getGranularityPeriod();
		report.report("Waiting one Granularity Period : "+granularityTimeInMinutes +" minutes");
		GeneralUtils.unSafeSleep(granularityTimeInMinutes * 1000 * 60);
		GeneralUtils.stopLevel();
	}
	
	//re Establishment command for 5 minutes to all UES - each 30 times a minute.
	protected void sendReEstablishmentCommandToAllUES(){
		GeneralUtils.startLevel("sending reEstablishment Command to all UEs in test");
		report.report("sending "+reEstablishmentCommand+" for 5 minutes to "+dmList.size()+" UEs");
		for(int i =0; i<=4; i++){
			long loopStartTime = System.currentTimeMillis();
			long timeToWaitAfterLoop = sendCommandsForOneMinute(dmList,loopStartTime);
			GeneralUtils.unSafeSleep(timeToWaitAfterLoop);
		}
		GeneralUtils.stopLevel();
	}
	
	private long sendCommandsForOneMinute(List<DMtool> tools,long startTime){
		int counter = 0;
		while(System.currentTimeMillis() - startTime < 60 * 1000){
			for(DMtool tool : tools){
				sendReEstablishmentCommand(tool);
				counter++;
				if(counter >= 30 * tools.size()){
					long timeLeft = 60 * 1000 -(System.currentTimeMillis() - startTime);
					if(timeLeft > 0){		//in case the action takes more then 1 min -> it returned negative value.
						return(timeLeft);
					}
				}
			}
		}
		return 0;
	}
	
	private void sendReEstablishmentCommand(DMtool tool){
		try{
			String result = tool.cli(reEstablishmentCommand);
			numberOfTimeResCommandBeenSent++;
			GeneralUtils.unSafeSleep(500);
			GeneralUtils.printToConsole("UE : "+tool.getUeIP()+", had result to re Establishment : "+result);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	/**
	 * checking counters, prints results.
	 * closing test parameters and reverting profiles to default
	 */
	protected void afterTest(){
		stopTraffic();
		checkCountersAndPrintResults();
		configureNodesState(EnbStates.IN_SERVICE);
		end();
	}
	
	private void stopTraffic(){
		GeneralUtils.startLevel("Stop Traffic");
		if(!traffic.stopTraffic()){
			report.report("stop Traffic Failed!");
		}
		GeneralUtils.stopLevel();
	}
	
	//checking counters
	protected void checkCountersAndPrintResults(){
		getAllReEstablishmentCounters();
		checkResultsAndPrintMessages();
		//check all conditions and print in each the right message.
	}
	
	private void checkResultsAndPrintMessages(){
		reEstablAttResultsPrint(true,"ReEstabAttempts",testCounters.rrcConnectReEstabAttSum,"reEstabSucc",testCounters.rrcConnectReEstabSuccSum,"reEstabFailSum",testCounters.rrcConnectReEstabFailSum);
		reEstablAttResultsPrint(false,"EstabAttempts",testCounters.rrcConnectEstabAttSum,"EstabSucc",testCounters.rrcConnectEstabSuccSum,"EstabFailSum",testCounters.rrcConnectEstabFailSum);
		
		report.report("number of sent commands"+" : "+numberOfTimeResCommandBeenSent +
				" ,"+"EstabSucc"+" : "+testCounters.rrcConnectEstabAttSum+
				" , "+"reEstabSucc"+" : "+testCounters.rrcConnectReEstabAttSum);
		if(numberOfTimeResCommandBeenSent != testCounters.rrcConnectEstabAttSum + testCounters.rrcConnectReEstabAttSum){
			report.report("KPI error due to: rrc connection reEstablishment attempts + rrc connection Establishment attempts is not "+numberOfTimeResCommandBeenSent,Reporter.FAIL);
		}
		
		report.report("5% of total commands"+" : "+((numberOfTimeResCommandBeenSent*5)/100) +
				" ,"+"EstabSucc"+" : "+testCounters.rrcConnectEstabAttSum+
				" , "+"reEstabSucc"+" : "+testCounters.rrcConnectReEstabAttSum);
		if((testCounters.rrcConnectReEstabFailSum + testCounters.rrcConnectEstabAttSum) < ((numberOfTimeResCommandBeenSent*5)/100)){
			report.report("KPI error due to: attempts are less than 5% of "+numberOfTimeResCommandBeenSent,Reporter.FAIL);
		}
	}
	
	private void reEstablAttResultsPrint(boolean reValue, String firstStr,int firstValue, String secStr,int secValue, String thirdStr , int thirdValue){
		String re = "";
		if(reValue){
			re = "Re";
		}
		report.report(firstStr+" : "+firstValue + ","+secStr+" : "+secValue+", "+thirdStr+" : "+thirdValue);
		if(firstValue != secValue + thirdValue){
			report.report("KPI error due to: sum of "+re+"Establishment attempts is not sum of "+re+"Establishments succsses + fails",Reporter.FAIL);
		}
	}
	
	private void getAllReEstablishmentCounters(){
		GeneralUtils.startLevel("get all counters values");
		testCounters.rrcConnectEstabAttSum = dut.getCountersValue("RrcConnEstabAttSum"); 
		report.report("RrcConnEstabAttSum = "+testCounters.rrcConnectEstabAttSum);
		
		testCounters.rrcConnectEstabFailSum = dut.getCountersValue("RrcConnEstabFailSum");
		report.report("RrcConnEstabFailSum = "+testCounters.rrcConnectEstabFailSum);
		
		testCounters.rrcConnectEstabSuccSum = dut.getCountersValue("RrcConnEstabSuccSum");
		report.report("RrcConnEstabSuccSum = "+testCounters.rrcConnectEstabSuccSum);
		
		testCounters.rrcConnectReEstabAttSum = dut.getCountersValue("RrcConnReEstabAttSum");
		report.report("RrcConnReEstabAttSum = "+testCounters.rrcConnectReEstabAttSum);
		
		testCounters.rrcConnectReEstabFailSum = dut.getCountersValue("RrcConnReEstabFailSum");
		report.report("RrcConnReEstabFailSum = "+testCounters.rrcConnectReEstabFailSum);
		
		testCounters.rrcConnectReEstabSuccSum = dut.getCountersValue("RrcConnReEstabSuccSum");
		report.report("RrcConnReEstabSuccSum = "+testCounters.rrcConnectReEstabSuccSum);
		GeneralUtils.stopLevel();
	}
	
	//sets and gets
	public String getDUT() {
		return this.dut.getNetspanName();
	}
	
	public void setDUT(String dut) {
		this.dut = (EnodeB) SysObjUtils.getInstnce().initSystemObject(EnodeB.class,false,dut).get(0);
	}
	
	public String getDUT2() {
		return this.dut2.getNetspanName();
	}
	
	public void setDUT2(String dut) {
		this.dut2 = (EnodeB) SysObjUtils.getInstnce().initSystemObject(EnodeB.class,false,dut).get(0);
	}
	
	@SuppressWarnings("unused")
	private class testCounters{
		int rrcConnectReEstabAttSum=0;
		int rrcConnectReEstabSuccSum=0;
		int rrcConnectReEstabFailSum=0;
		int rrcConnectEstabAttSum=0;
		int rrcConnectEstabSuccSum=0;
		int rrcConnectEstabFailSum=0;
	}
	
}

package testsNG.ProtocolsAndServices.S1X2HandlingAndEvents;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import org.junit.Test;
import EnodeB.EnodeB;
import IPG.IPG;
import Netspan.EnbProfiles;
import Netspan.API.Enums.EnbStates;
import Netspan.Profiles.NetworkParameters;
import UE.UE;
import Utils.GeneralUtils;
import Utils.ScpClient;
import Utils.SetupUtils;
import Utils.SysObjUtils;
import jsystem.framework.ParameterProperties;
import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;
import jsystem.framework.report.ReporterHelper;
import jsystem.framework.system.SystemManagerImpl;
import testsNG.TestspanTest;
import testsNG.Actions.EnodeBConfig;
import testsNG.Actions.PeripheralsConfig;
import testsNG.Actions.Traffic;
import testsNG.Actions.Traffic.TrafficType;
import testsNG.Actions.Utils.ParallelCommandsThread;

public class P0 extends TestspanTest{
	public  ArrayList<UE> ues=null;
	public  EnodeB dut;
	private EnodeBConfig enodebConfig;
	private PeripheralsConfig peripheralsConfig;
	private ArrayList<EnodeB> otherEnb;
	//private GemtekIperf iperf;
	private File logFile;
	private PrintStream ps;
	private IPG ipg;
	private String mmeIP = "";
	private String originalNetworkProfile;
	private ScpClient scpClient;
	private int numberOfUEs = 0;
	private Traffic traffic;
	private String connectionSuccess;
	private String connectionAttempts;
	private ParallelCommandsThread commandsThread = null;
	
	/**
    * init should be for all tests but each test will include his own preTest method
    */
	@SuppressWarnings("unchecked")
	@Override
	public void init() throws Exception {
		GeneralUtils.startLevel("Test Init");
		enbInTest = new ArrayList<EnodeB>();
		enbInTest.add(dut);
		super.init();
		otherEnb = (ArrayList<EnodeB>) enbInSetup.clone();
		otherEnb.remove(dut);
		enodebConfig = EnodeBConfig.getInstance();
		peripheralsConfig = PeripheralsConfig.getInstance();
		traffic = Traffic.getInstance(SetupUtils.getInstance().getAllUEs());
		
		GeneralUtils.stopLevel();
	}
	
	@Test
	@TestProperties(name = "S1_Reset_with_cause_Reset_All", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void S1ResetWithCauseResetAll() {
		if(s1ResetPreTest()){
			s1ResetTestProcess();
		}
		
		s1ResetAfterTest();
		
		traffic.stopTraffic();
	}

	@Test
	@TestProperties(name = "S1_setup_request_and_response", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void S1SetupRequestAndResponse() {
		GeneralUtils.startLevel("Verify EnodeB is in operational state");
		if(dut.waitForAllRunningAndInService(1 * 1000)){
			report.report("EnodeB is in service");
		}else{
			report.report("EnodeB is not in operational state", Reporter.FAIL);
			GeneralUtils.stopLevel();
			reason = "EnodeB is not in operational state";
			return;
		}
		GeneralUtils.stopLevel();
		
		String addressNetspan = enodebConfig.getMMeIpAdress(dut);
		if(!checkMmeAddress(addressNetspan)){
			return;
		}
		
		String addCommand = "route add -host "+addressNetspan+" reject";
		if(addressNetspan.contains(":")){
			addCommand = "ip -6 route add blackhole "+addressNetspan;
		}
		
		String delCommand = "route delete -host "+addressNetspan+" reject";
		if(addressNetspan.contains(":")){
			addCommand = "ip -6 route delete blackhole "+addressNetspan;
		}
		
		report.report("Sending CLI command to disconnect MME:");
		dut.expecteInServiceState = false;
		report.report(addCommand);
		String response = dut.shell(addCommand);
		report.report("Shell response: " + response);
		report.report("Ping response: " + dut.ping(addressNetspan,1));
		GeneralUtils.printToConsole("MME-IP:"+addressNetspan);
		if(!checkEnbNotConnectedToMme(3*60*1000)){
			report.report("EnodeB did not disconnect from MME and is still in service",Reporter.FAIL);
			reason="EnodeB did not disconnect from MME and is still in service";
			report.report("Sending CLI command to reconnect MME due to failure");
			report.report(delCommand);
			dut.shell(delCommand);
			return;
		}else{
			report.report("EnodeB disconnected from MME and is out of service");
		}			
		
		report.report("Sending CLI command to reconnect MME:");
		report.report(delCommand);
		String ans = dut.shell(delCommand);
		GeneralUtils.printToConsole(delCommand+"\n" + ans);
		if(!checkEnbConnectedToMmeAndInService(3*60*1000)){
			String failReason = "";
			if(!checkConnectionToMME()){
				report.report("EnodeB did not reconnect to MME",Reporter.FAIL);
				failReason = "EnodeB did not reconnect to MME";
				if(!dut.isInService()){
					report.report("EnodeB is out of service");
				}
			}else{
				report.report("EnodeB reconnected to MME but is still out of service",Reporter.FAIL);
				failReason = "EnodeB reconnected to MME but is still out of service";
			}
			reason=failReason;
			return;
		}else{
			dut.expecteInServiceState = true;
			report.report("EnodeB connected to MME and is in service");
		}
		
		checkMmeAddress(addressNetspan);			
		
	}
	
	private boolean checkConnectionToMME(){
		return dut.getMmeStatus().equals("1") && dut.getMmeStatusInfo().equals("connected");
	}
	
	private boolean checkMmeAddress(String addressNetspan) {
		GeneralUtils.startLevel("Verify EnodeB is connected to MME via correct address");
		String mmeAddress = dut.getMmeStatusIpAddress();
		if(addressNetspan !=null){
			InetAddress addrInetAddress = null;
			try {
				addrInetAddress = InetAddress.getByName(addressNetspan);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
			String addrStr = addrInetAddress.getHostAddress();
			if(addrStr.equals(mmeAddress)){
				report.report("EnodeB is connected to MME via correct address: "+mmeAddress);
			}else{
				report.report("EnodeB is not connected to MME via correct address: "+mmeAddress,Reporter.FAIL);
			}
		}else{
			report.report("EnodeB is not connected to MME via correct address: "+mmeAddress,Reporter.FAIL);
			GeneralUtils.stopLevel();
			reason = "EnodeB is not connected to MME via correct address"+mmeAddress;
			return false;
		}		
		printToReportTypeMmeAddress();
		GeneralUtils.stopLevel();	
		return true;
	}

	private void printToReportTypeMmeAddress() {
		String type = dut.getAddressType();
		String typePrint = "";
		if(type.equals("1")){
			typePrint = "ipv4";
		}else if(type.equals("2")){
			typePrint = "ipv6";
		}else{
			typePrint = "unknown";
		}
		report.report("MME address type: "+typePrint);	
	}

	/**
	* @author Shahaf Shuhamy
	* @author Gabriel Grunwald
	* Measurement Period change to 1 minute + wait 5 minutes
	* reset UEContentRelease MIB + wait 1 minute + another reset for the MIB 
	* @throws Exception 
	*/
	private boolean s1ResetPreTest() {
		try {
			ues = getUEs();
			numberOfUEs = ues.size();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			ipg = (IPG) SystemManagerImpl.getInstance().getSystemObject("IPG");
		} catch (Exception e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		originalNetworkProfile = dut.getDefaultNetspanProfiles().getNetwork();
		ipg.setRealMME(enodebConfig.getMMeIpAdress(dut));
		scpClient = new ScpClient(ipg.getServerIP(), ipg.getUserName(), ipg.getServerPassword());
		mmeIP = ipg.getFakeIP();
		logFile = new File("MessagesFile.log");
		try {
			logFile.createNewFile();
			this.ps = new PrintStream(logFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		ArrayList<String> commands = new ArrayList<>();
		commands.add("ue show link");
		commands.add("ue show rate");
		commands.add("System show memory");
		commands.add("System show error");
		commands.add("System show compilationtime");
		commands.add("Db get s1cfg");
		commands.add("db get PTPStatus");
		try {
			commandsThread = new ParallelCommandsThread(commands, dut, null, null);
			report.report("Started parallel commands.");
			commandsThread.start();
		} catch (IOException e1) {
			report.report("Failed starting parallel commands.",Reporter.WARNING);
			e1.printStackTrace();
		}
		
		GeneralUtils.startLevel("Pre Test");
		if (dut.isInstanceOfXLP_15_2()) {
			connectionSuccess = "RrcConnEstabSuccSum";
			connectionAttempts = "RrcConnEstabAttSum";	
		}else {
			connectionSuccess = "asLteStatsRrcConnEstabSuccSum";
			connectionAttempts = "asLteStatsRrcConnEstabAttSum";
		}
		report.report("Setting IPG script: ManInTheReset.js");
		ipg.setScriptName("ManInTheReset.js");
		ipg.setNumOfUEs(numberOfUEs);
		report.report("Trying to connect to SSH server");
		ipg.connectIPG();
		if(!ipg.isConnected()){
			report.report("Couldn't connect to SSH server",Reporter.FAIL);
			reason = "Couldn't connect to SSH server";
			GeneralUtils.stopLevel();
			return false;
		}else{
			report.report("SSH server connected");
		}
		try{
			String fileName = System.getProperty("user.dir")+File.separator+"resources"+File.separator+"IPGScripts"+File.separator+"ManInTheReset.js";
			scpClient.putFiles("/home/hadoop1/Desktop/LTE-IPG/LTE-IPG/scripts", fileName);
			report.report("Succeed to transfer script to remote server");
		}catch(Exception e){
			e.printStackTrace();
			report.report("Failed to transfer script to remote server",Reporter.FAIL);
			reason="Failed to transfer script to remote server";
			return false;
		}
		disableUEs();
		
		GeneralUtils.startLevel("Changing state of EnodeBs which are not in the test");
		try{
			for(EnodeB enb : otherEnb){
				peripheralsConfig.changeEnbState(enb, EnbStates.OUT_OF_SERVICE);
			}
		}catch(Exception e){
			report.report("Error in changing state",Reporter.FAIL);
		}
		finally{
			GeneralUtils.stopLevel();
		}
		
		if(!configEnodeBNetProfileToIpgProfile()){
			ipg.disconnectIPG();
			GeneralUtils.stopLevel();
			return false;
		}
		report.report("Clearing statistics for MIBs: ConnEstabAttSum and ConnEstabSuccSum to 0");
		clearStatistics();
				
		GeneralUtils.stopLevel();
		return true;
	}
	
	/**
	 * Scripts are a MUST in server side in location folder
	 * connect to server - server IP (sut or file)
	 * send server script location (sut or file)
	 * send run script command
	 * @throws Exception 
	 */
	private boolean s1ResetTestProcess() {		
		report.report("Starting IPG script");
		ipg.runScript();
				
		GeneralUtils.startLevel("Rebooting EnodeB and waiting for EnodeB to reach All Running");
		int timeToSleep = 5;
		if(!rebootDUTAndWait(timeToSleep*60*1000)){
			report.report("eNodeB did not reach All Running state",Reporter.FAIL);
			reason="eNodeB did not reach All Running state";
			GeneralUtils.stopLevel();
			return false;
		}
		report.report("EnodeB reached All Running state");
		GeneralUtils.stopLevel();
		
		GeneralUtils.startLevel("Read counters");
		if(!verifyStatisitcsAreZero())
		{
			report.report("Counters are not 0 - retrying to clear");
			clearStatistics();
			enodebConfig.waitGranularityPeriodTime(dut);
			if(!verifyStatisitcsAreZero()){
				report.report("Statistics are not 0 - not as expected",Reporter.FAIL);
				reason="Statistics are not 0 - not as expected";
				report.report("Disconnecting from IPG");
				GeneralUtils.stopLevel();
				ipg.disconnectIPG();
				return false;
			}else{
				report.report("Statistics are still 0 as expected");
				GeneralUtils.stopLevel();
			}
		}else{
			report.report("Statistics are still 0 as expected");
			GeneralUtils.stopLevel();
		}
		
		report.report("Starting traffic");
		try
		{
			traffic.setTrafficType(TrafficType.HO);
			traffic.startTraffic();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			report.report("Failed starting traffic", Reporter.WARNING);
		}
		enableUEs();	
			
		report.report("Waiting for IPG script to finish (up to 6 minutes timeout)");
		String ans = ipg.readCommandLine(6*60*1000,"Automation_Is_Done");
		if(!ans.contains("Automation_Is_Done")){
			if(ans.contains("All_UEs_are_connected")){
				report.report("All UEs are connected but error in sending messages",Reporter.FAIL);
				reason="All UEs are connected but error in sending messages";
			}else{
				report.report("Not all UEs connected to the eNodeB",Reporter.FAIL);
				reason="Not all UEs connected to the eNodeB";
			}
			afterScript(ans);
			return false;
		}
		
		report.report("Script ManInTheReset.js is done sending messages to UEs.");
		afterScript(ans);		
		
		enodebConfig.waitGranularityPeriodTime(dut);
		
		GeneralUtils.startLevel("Results");
		if(!checkResetMessages()){
			report.report(reason,Reporter.FAIL);
		}else{
			report.report("All results in MIBs are as expected");
		}
		GeneralUtils.stopLevel();
		
		return true;
	}
	
	private void afterScript(String ans){
		report.report("Stopping traffic");
		traffic.stopTraffic();
		GeneralUtils.startLevel("Output of JS script");
		ps.print(ans);
		addFileToReporter("Messages File");
		GeneralUtils.stopLevel();
		report.report("Disconnecting from ssh");
		ipg.disconnectIPG();
		disableUEs();
	}
	
	private void addFileToReporter(String name){
		try {
			ReporterHelper.copyFileToReporterAndAddLink(report, logFile, name);
		} catch (Exception e) {
			report.report("Exception in ReporterHelper.copyFileToReporterAndAddLink could not attach Command File");
			e.printStackTrace();
		}
	}
	
	/**
	 * Measurement period back to 5 minutes
	 * sample the UeContent MIB and compare to the number of reset packets Sent(Threshold)
	 * @throws IOException 
	 */
	private void s1ResetAfterTest() {
		commandsThread.stopCommands();
		commandsThread.moveFileToReporterAndAddLink();
		GeneralUtils.startLevel("After Test");
		GeneralUtils.startLevel("Returning to original state for other EnodeBs which are not in the test");
		for(EnodeB enb : otherEnb){
			peripheralsConfig.changeEnbState(enb, EnbStates.IN_SERVICE);
		}
		GeneralUtils.stopLevel();
		
		report.report("Setting default profile");
		enodebConfig.setProfile(dut, EnbProfiles.Network_Profile, originalNetworkProfile);
		GeneralUtils.startLevel("Deleting IPG profile");
		enodebConfig.deleteClonedProfiles();
		GeneralUtils.stopLevel();
		
		GeneralUtils.startLevel("Rebooting EnodeB and waiting for enodeB to reach All Running state");
		if(!rebootDUTAndWait(0))
			report.report(dut.getName() + "Failed to reach all running!", Reporter.WARNING);
		GeneralUtils.stopLevel();
		
		
		enableUEs();
		GeneralUtils.stopLevel();
	}
	
	private boolean clearStatistics(){
		HashMap<String,String> valuesToClear = new HashMap<String,String>();
		valuesToClear.put("ConnEstabAttSum", "0");
		valuesToClear.put("ConnEstabSuccSum", "0");
		try{
			dut.resetCounter("RrcStats", "*", valuesToClear);
		}catch(Exception e){
			e.printStackTrace();
			report.report("Failed to clear statistics due to exception");
			return false;
		}
		return true;
	}
	
	public boolean verifyStatisitcsAreZero(){
		boolean action = false;
		for(int i=0;i<2;i++){
			action = (dut.getCountersValue(connectionAttempts)==0);
			if(action){
				break;
			}
		}
		if(!action){
			return false;
		}
		for(int i=0;i<2;i++){
			action = (dut.getCountersValue(connectionSuccess)==0);
			if(action){
				break;
			}
		}
		return action;
	}
	
	private boolean checkResetMessages(){
		boolean attFlag = true;
		int att = dut.getCountersValue(connectionAttempts);
		int succ = dut.getCountersValue(connectionSuccess);
		report.report("MIB RrcConnEstabAttSum: "+att+", expected: between "+numberOfUEs*11+" and "+numberOfUEs*14);
		report.report("MIB RrcConnEstabSuccSum: "+succ+", expected: between "+numberOfUEs*11+" and "+numberOfUEs*14);
		
		if(!(att>=numberOfUEs*11 && att<=numberOfUEs*14)){
			reason = "RrcConnEstabAttSum result: "+att +" instead of between "+numberOfUEs*11+" and "+numberOfUEs*14;
			attFlag = false;
		}
		if(!(succ>=numberOfUEs*11 && succ<=numberOfUEs*14)){
			if(!reason.equals("")){
			reason += "\n";
			}
			reason += "RrcConnEstabSuccSum result: "+succ +" instead of between "+numberOfUEs*11+" and "+numberOfUEs*14;
			attFlag= false;
		}
		return attFlag;
	}

	private void disableUEs(){
		GeneralUtils.startLevel("Disabling UEs");
		for(UE ue:ues)
		{
			if(ue.stop())
				report.report(ue.getName() +" stopped");
			else
				report.report(ue.getName() +" failed to stop", Reporter.WARNING);
		}
		report.report("Waiting 5 seconds after UEs disable");
		GeneralUtils.unSafeSleep(5*1000);
		GeneralUtils.stopLevel();
	}
	
	private void enableUEs(){
		GeneralUtils.startLevel("Enabling UEs");
		for(UE ue:ues)
		{
			if(ue.start())
				report.report(ue.getName() +" started");
			else
				report.report(ue.getName() +" failed to start", Reporter.WARNING);
		}
		report.report("Waiting 5 seconds after UEs start");
		GeneralUtils.unSafeSleep(5*1000);
		GeneralUtils.stopLevel();
	}
	
	private boolean checkEnbNotConnectedToMme(long timeout){
		long startTime = System.currentTimeMillis();
		while(System.currentTimeMillis() - startTime < timeout){
			if(dut.getMmeStatus().equals("2") && dut.getMmeStatusInfo().equals("connection lost")){
				return true;
			}
			GeneralUtils.unSafeSleep(1000);
		}
		return false;
	}
	
	private boolean checkEnbConnectedToMmeAndInService(long timeout){
		long startTime = System.currentTimeMillis();
		while(System.currentTimeMillis() - startTime < timeout){
			if(checkConnectionToMME() && dut.isInService()){
				return true;
			}
			GeneralUtils.unSafeSleep(1000);
		}
		return false;
	}
	
	/**
	 * @author Shahaf Shuhamy
	 * @author Gabriel Grunwald
	 * function sends dut enodeb and ip for ManInTheMiddle machine and configure enodeb in 
	 * netspan according to it in networkProfile
	 */
	private boolean configEnodeBNetProfileToIpgProfile() {
		NetworkParameters np = new NetworkParameters();
		np.setMMEIP(mmeIP);
		try {
			report.startLevel("Cloning and setting new network profile from Netspan for EnodeB "+dut.getNetspanName());
			report.report("MME IP: " + mmeIP);
			np.setProfileName(originalNetworkProfile+"_IPG");
			boolean cloneAndSet = enodebConfig.cloneAndSetNetworkProfileViaNetSpan(dut, originalNetworkProfile, np);
			if(cloneAndSet){
				report.report("New network profile has been set up in enodeB "+dut.getNetspanName());
				report.stopLevel();
				return true;
			}else{
				report.report("Couldn't clone and set new cloned profile",Reporter.FAIL);
				reason="Couldn't clone and set new cloned profile";
				report.stopLevel();
			}
		}catch (Exception e) {
			report.report("Exception while cloning network profile",Reporter.FAIL);
			e.printStackTrace();
			try {
				report.stopLevel();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		return false;
	}
	
	private boolean rebootDUTAndWait(long timeToWait){
		long startTime = System.currentTimeMillis();
		dut.reboot();
		if(!dut.waitForAllRunningAndInService(EnodeB.WAIT_FOR_ALL_RUNNING_TIME)){
			return false;
		}
		long endTime = System.currentTimeMillis();
		if(endTime-startTime<timeToWait){
			long sleepTime = timeToWait-endTime+startTime;
			GeneralUtils.unSafeSleep(sleepTime);
		}
		return true;
	}

	private ArrayList<UE> getUEs() throws Exception{
		ArrayList<UE> temp = new ArrayList<>(); 
		ArrayList<UE> staticUEs = SetupUtils.getInstance().getStaticUEs(dut);
		ArrayList<UE> DynamicUes = SetupUtils.getInstance().getDynamicUEs();
		if(staticUEs!=null){
			temp.addAll(staticUEs);
		}
		if(DynamicUes!=null){
			temp.addAll(DynamicUes);
		}
		return temp;
	}
	
	/**
	 * @author Shahaf Shuhamy set Dut enodeB
	 * @return
	 */
	@ParameterProperties(description = "Name of Enodeb Which the test will be run On")
	public void setDUT(String dut) {
		this.dut = (EnodeB) SysObjUtils.getInstnce().initSystemObject(EnodeB.class,false,dut).get(0);
	}

	/**
	 * @author Shahaf Shuhamy get Dut enodeB
	 * @return
	 */
	public String getDUT() {
		return this.dut.getNetspanName();
	}
}

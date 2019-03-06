package testsNG.ProtocolsAndServices.NeighborManagement;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import EnodeB.AV100C;
import EnodeB.EnodeB;
import IPG.IPG;
import Netspan.EnbProfiles;
import Netspan.NetspanServer;
import Netspan.API.Enums.HandoverType;
import Netspan.API.Enums.HoControlStateTypes;
import Netspan.API.Enums.SonAnrStates;
import Netspan.API.Enums.X2ControlStateTypes;
import Utils.GeneralUtils;
import Utils.ScpClient;
import Utils.SysObjUtils;
import jsystem.framework.ParameterProperties;
import jsystem.framework.report.Reporter;
import jsystem.framework.report.ReporterHelper;
import jsystem.framework.system.SystemManagerImpl;
import testsNG.TestspanTest;
import testsNG.Actions.EnodeBConfig;
import testsNG.Actions.Neighbors;

public class NeighborManagementBase extends TestspanTest{
	protected Neighbors neighbor;
	public int MAXIMUM_ALLOWED_NEIGHBORS = 256;
	public EnodeB DUT1;
	public EnodeB DUT2;
	protected ArrayList<EnodeB> otherEnb;
	protected String[] debugCommands = {"db get nghlist","enb show ngh"};
	protected File logFile;
	protected String anrMode = "0";
	protected PrintStream ps;
	protected static final int LAST_NEIGHBOR_CONFIGURATION = 4;
	protected boolean performPostTest = true;
	protected ArrayList<EnodeB> realNeighborsList;
	protected ArrayList<EnodeB> virtualNeighborsList;
	protected ArrayList<EnodeB> allNeighbors;
	protected EnodeB tempEnodeB;
	protected EnodeB tempNeighbor;
	protected IPG ipg;
	protected ScpClient scpClient;
	
	@SuppressWarnings("unchecked")
	@Override
	public void init() throws Exception {
		neighbor = Neighbors.getInstance();
		enbInTest = new ArrayList<EnodeB>();
		enbInTest.add(DUT1);
		super.init();
		if(DUT1 instanceof AV100C)
			MAXIMUM_ALLOWED_NEIGHBORS = 32;
		GeneralUtils.startLevel("Preparing environment to test");
		otherEnb = (ArrayList<EnodeB>) enbInSetup.clone();
		otherEnb.remove(DUT1);
		for (EnodeB enb : otherEnb) {
			report.report("Set Son Profile:" + enb.defaultNetspanProfiles.getSON() + " For Enodeb:" + enb.getName());
			EnodeBConfig.getInstance().setProfile(enb, EnbProfiles.Son_Profile, enb.defaultNetspanProfiles.getSON());
			if (!EnodeBConfig.getInstance().isEnodebManaged(enb)) {
				reason += "WARNING: EnodeB: " + enb.getNetspanName() + " Is Not Managed in Netspan: "
						+ NetspanServer.getInstance().getHostname() + "\n";
			}
		}
		reason = "";
		try {
			neighbor.deleteAllNeighbors(DUT1);
			neighbor.deleteAll3rdParty();
			disableANR(DUT1);
		} catch (Exception e) {
			e.printStackTrace();
			report.report("Failed to delete neighbors", Reporter.FAIL);
		} finally {
			GeneralUtils.stopLevel();
		}
	}
	
	public void configurationMaximumAllowedNeighborsOneCell() throws NumberFormatException{
		realNeighborsList = new ArrayList<EnodeB>();
		virtualNeighborsList = new ArrayList<EnodeB>();
		allNeighbors = new ArrayList<EnodeB>();
		int neighborConf = 0;
		tempEnodeB = DUT1;
		tempNeighbor = DUT2;

		getDebugInfo(tempEnodeB);
		getDebugInfo(tempNeighbor);
		
		GeneralUtils.startLevel("Adding neighbors to enodeB");
		// adding the real neighbors to list
		int NumberOfRealNeighbors = otherEnb.size() - 1;
		for (int i = 0; i <= NumberOfRealNeighbors; i++) {
			realNeighborsList.add(otherEnb.get(i));
		}

		int numberOfVirtualEnodeBs = MAXIMUM_ALLOWED_NEIGHBORS - realNeighborsList.size();
		// adding 3rd party enodebs to the 3rd party list in Netspan
		GeneralUtils.startLevel("Adding 3rd parties to Netspan");
		virtualNeighborsList = neighbor.addingThirdPartyNeihbors(tempEnodeB, numberOfVirtualEnodeBs, false, null);
		GeneralUtils.stopLevel();
		GeneralUtils.startLevel("Adding maximum ("+MAXIMUM_ALLOWED_NEIGHBORS+") neighbors to enodeB");
		
		allNeighbors.addAll(realNeighborsList);
		allNeighbors.addAll(virtualNeighborsList);
		
		if(allNeighbors.size()>=1){
			//add real neighbors
			if (realNeighborsList.size() >= 1) {
				for(EnodeB enb : realNeighborsList){
					NeighborConf conf = NeighborConf.determineNeighborConf(neighborConf);
					neighborConf++;
					if (!neighbor.addNeighbor(tempEnodeB, enb, conf.getHoControlStatType(), conf.getX2ControlStatType(), conf.getHoType(), conf.isStatic(), "0")) {
						report.report("Neighbor " + enb.getNetspanName() + " was not added to the neighbor list",Reporter.FAIL);
						reason="FAIL: At least one neighbor was not added to the neighbor list\n";					
						
					}else{
						report.report("Neighbor " + enb.getNetspanName() + " was added to the neighbor list");
					}
					if(neighborConf >= LAST_NEIGHBOR_CONFIGURATION){
						neighborConf = 0;
					}
				}
			}
		}else{
			report.report("There are no neighbors to add to the EnodeB", Reporter.FAIL);
			reason="FAIL: There are no neighbors to add to the EnodeB\n";
		}
			
			// add virtual neighbors to the neighbor list
			if (virtualNeighborsList.size() >= 1) {
				for (EnodeB enb : virtualNeighborsList) {
					NeighborConf conf = NeighborConf.determineNeighborConf(neighborConf);
					neighborConf++;
					if (!neighbor.addNeighborOnlyNetspan(tempEnodeB, enb,conf.getHoControlStatType(), conf.getX2ControlStatType(), conf.getHoType(), conf.isStatic(), "0")) {
						report.report("Neighbor " + enb.getNetspanName() + " was not added to the neighbor list",Reporter.FAIL);
						reason="FAIL: At least one neighbor was not added to the neighbor list\n";						
						
					}else{
						report.report("Neighbor " + enb.getNetspanName() + " was added to the neighbor list");
					}
					if(neighborConf >= LAST_NEIGHBOR_CONFIGURATION){
						neighborConf = 0;
					}					
				}
				
			}else{
				report.report("There are no neighbors to add to the EnodeB", Reporter.FAIL);
				reason="FAIL: There are no neighbors to add to the EnodeB\n";
			}
		
		GeneralUtils.stopLevel();
		GeneralUtils.stopLevel();
		GeneralUtils.unSafeSleep(30*1000);
		
		//verify neighbors were added with right parameters
		GeneralUtils.startLevel("Verifying that neighbors were added with the right parameters");
		neighborConf = 0;
		for (EnodeB enb : allNeighbors) {
			NeighborConf conf = NeighborConf.determineNeighborConf(neighborConf);
			neighborConf++;
			if (!neighbor.verifyNeighborParametersNMSandSNMP(tempEnodeB, enb,conf.getHoControlStatType(), conf.getX2ControlStatType(), conf.getHoType(), conf.isStatic(), "0")) {
				report.report("Some of the parameters for neighbor " + enb.getNetspanName() + " were added with wrong values or neighbor wasn't added at all",Reporter.FAIL);
				reason="FAIL: At least one neighbor was added with wrong parameters or was not added at all\n";
			}else{
				report.report("Neighbor "+enb.getNetspanName()+" was verified and added with right values");
			}
			if(neighborConf >= LAST_NEIGHBOR_CONFIGURATION){
				neighborConf = 0;
			}
		}

		GeneralUtils.stopLevel();
		
		GeneralUtils.startLevel("Verifying neighbors exist");
		int netspanNbrCounter = 0;
		for (EnodeB enb : allNeighbors) {
			if (!neighbor.verifyNeighborExistsNSOrSNMP(tempEnodeB, enb)){
				report.report("EnodeB: "+ enb.getNetspanName()+ " was not added to the Enodeb: " + tempEnodeB.getNetspanName(), Reporter.FAIL);
				reason="FAIL: At least one neighbor was NOT added to the EnodeB\n";
			}
			else{ 
				report.report("EnodeB: "+ enb.getNetspanName()+ " was added to the Enodeb: " + tempEnodeB.getNetspanName());
				netspanNbrCounter++;
			}
		}
		GeneralUtils.stopLevel();
		
		report.report("Total Netspan Neighbors: " + netspanNbrCounter);
		
		getDebugInfo(tempEnodeB);
		getDebugInfo(tempNeighbor);
		
		postTest(tempEnodeB);
	}
	

	/**
	 * setting ANR state to node's profile.
	 * Fall back to SNMP.
	 * @author Shahaf Shuhamy
	 * @param enb
	 */
	protected void disableANR(EnodeB enb) {
		try{
			GeneralUtils.startLevel("Disabling ANR for node "+enb.getNetspanName());
			neighbor.SetANRState(enb, SonAnrStates.DISABLED, null, null);
		}catch(Exception e){
			e.printStackTrace();
		}
		finally{
			GeneralUtils.stopLevel();
		}
	}

	/**
	 * method Saving to anrMode member the node's ANR mode
	 * and setting value Parameter to node's ANR mode.
	 * @author Shahaf Shuhamy
	 * @param enb
	 * @param value
	 */
	protected void changeAnrValueSNMP(EnodeB enb, String value){
		try{
			String enbAnrMode = enb.getAnrMode();
			if(enbAnrMode.equals(value)){
				GeneralUtils.printToConsole("ANR snmp Value is already Settle");
				return;
			}
			
			GeneralUtils.printToConsole("get Value from SNMP: "+enbAnrMode);
			GeneralUtils.printToConsole("Setting Value with SNMP: "+value+" to node Anr feature");
			
			int snmpValue = Integer.valueOf(value);
			enb.setAnrMode(snmpValue);
			this.anrMode = value;
			
		}catch(Exception e){
			GeneralUtils.printToConsole("could not set value via SNMP!");
		}
	}
	
	public EnodeB getDut() {
		return DUT1;
	}

	@ParameterProperties(description = "Name of Enodeb Which the test will be run On")
	public void setDUT(String dut) {
		this.DUT1 = (EnodeB) SysObjUtils.getInstnce().initSystemObject(EnodeB.class,false,dut).get(0);
	}
	
	public String getDUT() {
		return this.DUT1.getNetspanName();
	}
	
	@ParameterProperties(description = "Name of the Second Enodeb Which the test will be run On")
	public void setSecondDUT(String secondDut){
		this.DUT2 = (EnodeB) SysObjUtils.getInstnce().initSystemObject(EnodeB.class, false, secondDut).get(0);
	}
	
	public String getSecondDut(){
		return this.DUT2.getNetspanName();
	}
	
	protected void getDebugInfo(EnodeB enb)
	{
		report.reportHtml("get nghList ("+enb.getName() + ")", enb.lteCliWithResponse("db get nghlist", "lte_cli:>>"), true);
		report.reportHtml("show ngh ("+enb.getName() + ")", enb.lteCliWithResponse("enb show ngh", "lte_cli:>>"), true);
	}
	
	protected void printAlignedPrint(String prnt){
		initializeLogger();
		ps.print(prnt);
		addFileToReporter("Debug info");
		ps.flush();
		closeLogger();
	}
	
	protected void initializeLogger(){
		Date initalizeDateLogFileName = new Date();
		String initDateString = initalizeDateLogFileName.toString();
		initDateString = initDateString.replace(":", "");
		logFile = new File(String.format("CLI_Debug_Info_EnodeB_%s_%s",DUT1.getNetspanName(),initDateString));
		try {
			logFile.createNewFile();
			this.ps = new PrintStream(logFile);
		} catch (IOException e) {
			report.report("Couldn't create log file");
			e.printStackTrace();
		}
		
	}
	
	protected void closeLogger(){
		logFile = null;
		ps.close();
		ps = null;
	}
	
	protected void addFileToReporter(String name){
		try {
			ReporterHelper.copyFileToReporterAndAddLink(report, logFile, name);
		} catch (Exception e) {
			report.report("Exception in ReporterHelper.copyFileToReporterAndAddLink could not attach Command File");
			e.printStackTrace();
		}
	}
	
	protected boolean checkAndPrintStatusOfVerification(boolean stat){
		if(!stat){
			report.report("FAIL: Neighbor was added with wrong parameters or wasn't found",Reporter.FAIL);
			reason += "Neighbor was added with wrong parameters or wasn't found";
			return false;
		}else{
			report.report("Verification passed successfully. Neighbor was added with the right parameters");
		}
		return true;
	}
	
	protected boolean checkAndPrintStatusOfAdding(EnodeB tempNeighbor, EnodeB  tempEnodeB, boolean stat){
		if (!stat) {
			report.report("The neighbor " + tempNeighbor.getNetspanName() + " wasn't added to the enodeb"+ tempEnodeB.getNetspanName(), Reporter.FAIL);
			reason = "The neighbor " + tempNeighbor.getNetspanName() + " wasn't added to the enodeb"+ tempEnodeB.getNetspanName();
			getDebugInfo(tempEnodeB);
			return false;
		}else{
			report.report("The neighbor " + tempNeighbor.getNetspanName() + " was added to the enodeb "+ tempEnodeB.getNetspanName()+ " as expected");
		}
		return true;
	}
	
	protected boolean checkAndPrintStatusOfDeleting(EnodeB tempNeighbor, EnodeB tempEnodeB, boolean stat){
		if(stat){
			report.report("The neighbor: " + tempNeighbor.getNetspanName() + " was not deleted from the EnodeB: "+ tempEnodeB.getNetspanName(), Reporter.FAIL);
			reason += "The neighbor: " + tempNeighbor.getNetspanName() + " was not deleted from the EnodeB: "+ tempEnodeB.getNetspanName();
			return false;
		}else{
			report.report("The neighbor: " + tempNeighbor.getNetspanName() + " was deleted from the EnodeB: "+ tempEnodeB.getNetspanName() + " as expected");
		}
		return true;
	}
	
	protected boolean changeConfigurationsFlagsToTrue(EnodeB tempEnodeB, EnodeB tempNeighbor){
		if(!neighbor.changeNbrX2ConfiguratioUpdateFlag(tempNeighbor, true)){
			reason = "Couldn't change the X2 Configuration update Flag to enabled for eNodeB " + tempNeighbor.getNetspanName();
			return false;
		}
		if(!neighbor.changeNbrX2ConfiguratioUpdateFlag(tempEnodeB, true)){
			reason = "Couldn't change the X2 Configuration update Flag to enabled for eNodeB " + tempEnodeB.getNetspanName();
			return false;
		}
		if(!neighbor.changeNbrAutoX2ControlFlag(tempEnodeB, true)){
			reason = "Couldn't change the Auto X2 Control Flag to enabled for eNodeB " + tempEnodeB.getNetspanName();
			return false;
		}
		if(!neighbor.changeNbrAutoX2ControlFlag(tempNeighbor, true)){
			reason = "Couldn't change the Auto X2 Control Flag to enabled for eNodeB " + tempNeighbor.getNetspanName();
			return false;
		}
		return true;
	}
	
	public void postTest(EnodeB tempEnodeB){
		// returning system to status before the test
		if(!performPostTest) return;
		GeneralUtils.startLevel("Returning system to status before the test");
		try{
			neighbor.deleteAllNeighbors(tempEnodeB);
			neighbor.deleteAll3rdParty();
			returnAnrToSetupState(DUT1);
		}catch(Exception e){
			e.printStackTrace();
			report.report("Failed to delete neighbors in post test",Reporter.FAIL);
		}finally{
			GeneralUtils.stopLevel();			
		}	
	}

	/**
	 * method setting default SON profile from SUT to Netspan.
	 * Fall Back to SNMP ANR.
	 * @author Shahaf Shuhamy
	 * @param enb
	 */
	protected void returnAnrToSetupState(EnodeB enb) {
		try{
			GeneralUtils.startLevel("Setting Son Profile Back to Default");
			EnodeBConfig.getInstance().setProfile(enb, EnbProfiles.Son_Profile, enb.defaultNetspanProfiles.getSON());
			EnodeBConfig.getInstance().deleteClonedProfiles();
		}catch(Exception e){
			e.printStackTrace();
			report.report("could not set Son profile!",Reporter.WARNING);
			GeneralUtils.printToConsole("SNMP FallBack");
			
			changeAnrValueSNMP(enb,anrMode);
		}
		finally{
			GeneralUtils.stopLevel();
		}
	}
	
	protected List<String> calculateNeighborsIPs(){
		List<String> ips = new ArrayList<String>();
		ips.add("172.21.39.5");
		ips.add("99.20.20.3");
		return ips;
	}
	
	protected void afterScript(String ans) throws Exception{
		GeneralUtils.startLevel("Output of JS script");
		ps.print(ans);
		addFileToReporter("Output JS");
		GeneralUtils.stopLevel();
		report.report("Disconnecting from ssh");
		ipg.disconnectIPG();
	}
	
	protected void configurationMaximumAllowedNeighborsViaIPG() throws Exception {
		tempEnodeB = DUT1;
		tempNeighbor = DUT2;
		
		List<String> neihborsIps = calculateNeighborsIPs();
		ArrayList<EnodeB> neihbors = new ArrayList<EnodeB>();
		
		// adding 3rd party enodebs to the 3rd party list in Netspan
		GeneralUtils.startLevel("Adding " + neihborsIps.size() + " 3rd parties to Netspan");
		for (int i = 0; i < neihborsIps.size(); i++) {
			int dlEarfcn = (int) (38850 + Math.floor(i / 32) * 100);
			EnodeB neihbor = neighbor.adding3rdPartyNeighbor(tempEnodeB, "IPG_Neihbor_" + i, neihborsIps.get(i), 0, i, dlEarfcn);

			neihbors.add(neihbor);
		}
		GeneralUtils.stopLevel();

		//-------------------------------------------------------------------------------

		GeneralUtils.startLevel("Adding Neighbors to EnodeB " + tempEnodeB.getNetspanName());
		for (EnodeB neihbor : neihbors) {
			if (!neighbor.addNeighborOnlyNetspan(tempEnodeB, neihbor, HoControlStateTypes.ALLOWED,
					X2ControlStateTypes.AUTOMATIC, HandoverType.TRIGGER_X_2, true, "0")) {
				report.report("Neighbor " + neihbor.getNetspanName() + " was not added to the neighbor list",
						Reporter.WARNING);
				reason = "At least one neighbor was not added to the neighbor list\n";
			} else {
				report.report("Neighbor " + neihbor.getNetspanName() + " was added to the neighbor list");
			}
		}
		GeneralUtils.stopLevel();
		
		//-----------------------------------------------------------------------------
		
		ipg = (IPG) SystemManagerImpl.getInstance().getSystemObject("IPG");
		scpClient = new ScpClient(ipg.getServerIP(), ipg.getUserName(), ipg.getServerPassword());
		logFile = new File("MessagesIPG.log");
		logFile.createNewFile();
		ps = new PrintStream(logFile);
		
		//------------------------------------------------------------------------
		
		ipg.setScriptName("128_neighbour_test.js");
		report.report("Trying to connect to SSH server");
		ipg.connectIPG();
		if(!ipg.isConnected()){
			report.report("Couldn't connect to SSH server",Reporter.FAIL);
			reason = "Couldn't connect to SSH server";
			GeneralUtils.stopLevel();
		}else{
			report.report("SSH server connected");
		}
		try{
			String fileName = System.getProperty("user.dir")+File.separator+"resources"+File.separator+"IPGScripts"+File.separator+"128_neighbour_test.js";
			scpClient.putFiles("/home/hadoop1/Desktop/LTE-IPG/LTE-IPG/scripts", fileName);
			report.report("Succeed to transfer script to remote server");
		}catch(Exception e){
			e.printStackTrace();
			report.report("Failed to transfer script to remote server",Reporter.FAIL);
			reason="Failed to transfer script to remote server";
		}
		
		Map<String, String> scriptParams = new HashMap<String, String>();
		scriptParams.put("enbIPx2", "20.20.124.1");
		scriptParams.put("setupNumber", "24");
		ipg.runScript(scriptParams);

		report.report("wait 3 minutes");
		GeneralUtils.unSafeSleep(1000 * 3 * 60);
		
		String ans = ipg.readCommandLine(1000 * 60,"Automation_Is_Done");
		afterScript(ans);
		
		neighbor.deleteAllNeighbors(tempEnodeB);
		neighbor.delete3rdPartyList(neihbors);
		
		getDebugInfo(tempEnodeB);
		getDebugInfo(tempNeighbor);
		
		postTest(tempEnodeB);
	}
}

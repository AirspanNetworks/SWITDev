package testsNG.ProtocolsAndServices.Connectivity;

import java.io.IOException;
import java.util.ArrayList;
import Attenuators.AttenuatorSet;
import EnodeB.EnodeB;
import Netspan.EnbProfiles;
import Netspan.API.Enums.EnbStates;
import Netspan.API.Enums.SecurityProfileOptionalOrMandatory;
import Netspan.Profiles.NetworkParameters;
import Netspan.Profiles.RadioParameters;
import Netspan.Profiles.SecurityParameters;
import UE.UE;
import Utils.DefaultNetspanProfiles;
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
import testsNG.Actions.SoftwareUtiles;
import testsNG.Actions.Traffic;
import testsNG.Actions.Utils.GemtekIperf;

public class P0 extends TestspanTest {
	public EnodeB dut;
	public EnodeB secondDut;
	public Neighbors neighbors;
	protected PeripheralsConfig peripheralsConfig;
	protected EnodeBConfig enbConfig;
	protected AttenuatorSet attenuatorSetUnderTest;
	protected String attenuatorSetName = "rudat_set";
	protected ArrayList<UE> ueList;
	protected ArrayList<EnodeB> otherEnbs;
	protected int FIVE_SEC = 5*1000;
	protected int ONE_MIN = 60*1000;
	protected int TIME_SLEEPING_PINGING_UES = 10 * 1000;
	protected int DEFAULT_GRANULARITY_TIME =  5 * 60 * 1000;
	protected ArrayList<String> mibsInTest = new ArrayList<String>();
	protected GemtekIperf iperf;
	DefaultNetspanProfiles defaultProfilesDut;
	DefaultNetspanProfiles defaultProfilesSecondDut;

	@Override
	public void init() throws Exception {
		enbInTest = new ArrayList<EnodeB>();
		enbInTest.add(dut);
		enbInTest.add(secondDut);
		report.startLevel("Test Init");
		super.init();
		updateMibCollectionForTest(mibsInTest);
		// init Dut and other Enodebs
		otherEnbs = SetupUtils.getInstance().getAllEnb();
		otherEnbs.remove(dut);
		// init UE list for every EnodeB
		ueList = getUES(dut);
		getAllUEsIperf(ueList);
		defaultProfilesDut = dut.getDefaultNetspanProfiles();
		defaultProfilesSecondDut = secondDut.getDefaultNetspanProfiles();
		neighbors = Neighbors.getInstance();
		peripheralsConfig = PeripheralsConfig.getInstance();
		enbConfig = EnodeBConfig.getInstance();
		attenuatorSetUnderTest = AttenuatorSet.getAttenuatorSet(attenuatorSetName);
		report.stopLevel();
	}

	/**
	 * parsing LAN ip from each UE obj in the list. and initing the iperf
	 * object.
	 * 
	 * @param ueList2
	 */
	private void getAllUEsIperf(ArrayList<UE> ueList2) {
		ArrayList<String> uelanIp = new ArrayList<String>();
		for (UE ue : ueList) {
			uelanIp.add(ue.getLanIpAddress());
		}
		iperf = new GemtekIperf(uelanIp);
	}

	// ------UE State Connection
	// Design-------------------------------------------------------------------------------
	/**
	 * 1. changing Security Profile to Test's profile
	 * 2. starting Gemtek's iperf
	 * 3. checking EPC and Current Test Mibs 
	 */
	//@Test
	@TestProperties(name = "UE_Network_Entry_Selection", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful","SecondSingleDUT" })
	public void UE_Network_Entry_Selection() {
		preTest();
		resetStatistics(dut, mibsInTest);
		SecurityParameters secProfParams = insertSecParams(dut, SecurityProfileOptionalOrMandatory.OPTIONAL, 1, 0, 0,
				SecurityProfileOptionalOrMandatory.OPTIONAL, 1, 0, 0);
		changeSecurityProfileNetspan(dut, secProfParams);
		rebootAndWaitForAllRunning(dut,EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
		setIperfTimeAndEnablePlusStartUE(10);
		//waiting 1 minute
		report.report("Waiting "+ SoftwareUtiles.milisToFormat(ONE_MIN)+" minutes");
		Wait(ONE_MIN);
		if (!checkEPCForUEToActiveEnb(dut, ueList.get(0))) {
			reason = "Failing test since ue is not connected with EPC";
			report.report(reason, Reporter.FAIL);
		}
		ueList.get(0).stop();
		report.report("Waiting for Granularity Period to Over - "+SoftwareUtiles.milisToFormat(DEFAULT_GRANULARITY_TIME) +" Minutes");
		Wait(DEFAULT_GRANULARITY_TIME);
		checkMibsForResults(dut, 1);
			
		changeSecurityProfileToDefault(dut);
		rebootAndWaitForAllRunning(dut,EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
		try {
			afterTest();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
/**
 * Java Doc Test
 */
	//@Test
	@TestProperties(name = "UE_Network_Entry_Selection_to_Idle_Mode", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful","SecondSingleDUT" })
	public void UE_Network_Entry_Selection_to_Idle_Mode() {
		preTest();
		resetStatistics(dut, mibsInTest);
		SecurityParameters secProfParams = insertSecParams(dut, SecurityProfileOptionalOrMandatory.OPTIONAL, 1, 0, 0,
				SecurityProfileOptionalOrMandatory.OPTIONAL, 1, 0, 0);
		//change netspan security profile
		changeSecurityProfileNetspan(dut, secProfParams);
		rebootAndWaitForAllRunning(dut,EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
		//start traffic
		setIperfTimeAndEnablePlusStartUE(11);
		//waiting 1 minute
		report.report("Waiting "+ SoftwareUtiles.milisToFormat(ONE_MIN)+" minutes");
		Wait(ONE_MIN);
		//check epc and mibs
		if(!checkEPCForUEToActiveEnb(dut, ueList.get(0))){
			reason = "Failing test since ue is not connected with EPC";
			report.report(reason, Reporter.FAIL);
		}
		// stop traffic
		ueList.get(0).stop();
		report.report("Waiting for Granularity Period to Over - "+SoftwareUtiles.milisToFormat(DEFAULT_GRANULARITY_TIME - ONE_MIN) +" Minutes");
		Wait(DEFAULT_GRANULARITY_TIME - ONE_MIN);
		checkMibsForResults(dut, 1);
		
		//ueList.get(0).stop();
		
		iperf.postHttpForSingleUE(ueList.get(0).getLanIpAddress());
		// wait 10 seconds
		report.report("Waiting "+SoftwareUtiles.milisToFormat(TIME_SLEEPING_PINGING_UES)+" seconds");
		Wait(TIME_SLEEPING_PINGING_UES);
		
		//check UE registered in EPC
		try{
		report.startLevel("check UE is not registerd under some ENB but does in EPC");
		if(checkUEConnectionToEPC(ueList.get(0))){
			if(!checkEPCForUEToActiveEnb(dut, ueList.get(0))){
				report.report("Ue connected to epc and not to ENB");
			}else{
				reason = "Failing test since ue is connected with ENB instead of not to";
				report.report(reason, Reporter.FAIL);
			}
			}else{
				reason = "Failing test since UE is not connected to EPC";
				report.report(reason,Reporter.FAIL);
			}
		report.stopLevel();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		//back to normal
        //ueList.get(0).stop();
		changeSecurityProfileToDefault(dut);
		rebootAndWaitForAllRunning(dut,EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
		try {
			afterTest();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Created by Shuhamy Shahaf
	 * Java Doc Test exit idle mode
	 */
	//@Test
	@TestProperties(name = "Connection_State_Exit_Idle_Mode", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful","SecondSingleDUT" })
	public void Connection_State_Exit_Idle_Mode() {
		preTest();
		resetStatistics(dut, mibsInTest);
		SecurityParameters secProfParams = insertSecParams(dut, SecurityProfileOptionalOrMandatory.OPTIONAL, 1, 0, 0,
				SecurityProfileOptionalOrMandatory.OPTIONAL, 1, 0, 0);
		//change Security Profile
		changeSecurityProfileNetspan(dut, secProfParams);
		rebootAndWaitForAllRunning(dut,EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
		//set single ue to enable
		setSingelUEToEnableMode(ueList, 0);
		//wait one minute
		report.report("waiting "+SoftwareUtiles.milisToFormat(2*1000*60)+" minutes");
		Wait(2*1000*60);
		if(!peripheralsConfig.checkForIdleMode(dut,ueList.get(0))){
			reason = "UE Should not be registerd under ENodeB - Not in Idle Mode";
			report.report(reason,Reporter.FAIL);
		}
	
		setIperfTimeAndEnablePlusStartUE(11);
		// starting traffic for 10 seconds
		Wait(TIME_SLEEPING_PINGING_UES);
		
		//stopping ue
		GeneralUtils.printToConsole("Stopping Ue "+ueList.get(0));
		ueList.get(0).stop();
		
		//check epc and mibs
		if (!checkEPCForUEToActiveEnb(dut, ueList.get(0))) {
			reason = "Failing test since ue is not connected with EPC";
			report.report(reason, Reporter.FAIL);
		}
		report.report("Waiting for Granularity Period to Over - "+SoftwareUtiles.milisToFormat(DEFAULT_GRANULARITY_TIME) +" Minutes");
		Wait(DEFAULT_GRANULARITY_TIME);
		checkMibsForResults(dut, 1);
		
		//change back to normal
		changeSecurityProfileToDefault(dut);
		rebootAndWaitForAllRunning(dut,EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
		try {
			afterTest();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * 
	 */
	@Deprecated
	//@Test
	@TestProperties(name = "Network Entry : Re-Establishment", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful","SecondSingleDUT" })
	public void NetworkEntry_Re_Establishment() {
		preTest();
		
		resetStatistics(dut, mibsInTest);
		//set UE to enable mode and start traffic
		setIperfTimeAndEnablePlusStartUE(15);
		//wait one minute
		report.report("waiting "+SoftwareUtiles.milisToFormat(ONE_MIN)+" minutes");
		Wait(ONE_MIN);
		//checking EPC for ue under ENB
		checkEPCForUEToActiveEnb(dut, ueList.get(0));
		//stopping Ue + 5 seconds wait
		GeneralUtils.printToConsole("Stopping UE");
		ueList.get(0).stop();
		
		report.report("Waiting for Granularity Period to Over - "+SoftwareUtiles.milisToFormat(DEFAULT_GRANULARITY_TIME) +" Minutes");
		Wait(DEFAULT_GRANULARITY_TIME);
		
		checkMibsForResults(dut, 1);
		report.report("Starting UE");
		//starting ue + 5 seconds wait
		ueList.get(0).start();
		Wait(ONE_MIN);
		
		report.report("Setting Attenuator to max Value");
		peripheralsConfig.setAttenuatorSetValue(attenuatorSetUnderTest, attenuatorSetUnderTest.getMaxAttenuation());
		GeneralUtils.printToConsole("Attenuator to: "+attenuatorSetUnderTest.getMaxAttenuation());
		
		report.report("Setting attenuator to 0");
		peripheralsConfig.setAttenuatorSetValue(attenuatorSetUnderTest, 0);
		GeneralUtils.printToConsole("Attenuator to: 0");
		
		checkEPCForUEToActiveEnb(dut, ueList.get(0));
		report.report("Waiting for Granularity Period to Over - "+SoftwareUtiles.milisToFormat(DEFAULT_GRANULARITY_TIME) +" Minutes");
		Wait(DEFAULT_GRANULARITY_TIME);
		GeneralUtils.printToConsole("Stopping UE");
		ueList.get(0).stop();
		if(!checkMibsForResults(dut, 1)){
			reason = "MIBS are not according to required results";
			report.report(reason,Reporter.FAIL);
		}
		
		try {
			afterTest();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	// ------UE State Connection
	// Design-------------------------------------------------------------------------------

	// --------------------------------------------------------------------------------------------TAI
	// Tests
	/**
	 * 
	 */
	//@Test
	@TestProperties(name = "Connection_State_Idle_Mode-Re-Selection_Same_TAI", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void Connection_State_Idle_Mode_ReSelection_Same_TAI() {
		//preTest();
	//	resetStatistics(dut, mibsInTest);
		peripheralsConfig.changeEnbState(secondDut, EnbStates.IN_SERVICE);
		// setting second enb in setup with the same plmn and earfcn as the first
		setSecondEnbProfiles();
		rebootAndWaitForAllRunning(dut,EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
		//verify no neighbors
		neighbors.deleteAllNeighbors(dut);
		neighbors.deleteAllNeighbors(secondDut);
		//set ue to enable
		setSingelUEToEnableMode(ueList, 0);
		try {
			iperf.setIperfTime(10 * 60);
		} catch (IOException d) {
			d.printStackTrace();
		}
		
		//start traffic
		iperf.postHttpForSingleUE(ueList.get(0).getLanIpAddress());

		//wait one minute
		report.report("waiting "+SoftwareUtiles.milisToFormat(ONE_MIN)+" minutes");
		Wait(ONE_MIN);
		
		//first EPC check
		if(!checkEPCForUEToActiveEnb(dut, ueList.get(0))){
			reason = "Failing test since ue is not connected with EPC";
			report.report(reason, Reporter.FAIL);
		}
		
		report.report("Waiting for Granularity Period to Over - "+SoftwareUtiles.milisToFormat(DEFAULT_GRANULARITY_TIME - ONE_MIN) +" Minutes");
		Wait(DEFAULT_GRANULARITY_TIME - ONE_MIN);
		
		checkMibsForResults(dut, 1);
		
		//set Attenuator value to max and wait 20 seconds
		peripheralsConfig.setAttenuatorSetValue(attenuatorSetUnderTest, attenuatorSetUnderTest.getMaxAttenuation());
		Wait(20*1000);
		
		//check EPC registration with the second eNB
		try {
			if (!checkEPCForUEToActiveEnb(secondDut, ueList.get(0))) {
				reason = "Failing test since ue is not connected with EPC";
				report.report(reason, Reporter.FAIL);
			}
		} catch (Exception e1) {
			report.report("Exception in checkUesConnectionEPC");
			e1.printStackTrace();
		}
		
		// changing profiles back to default
		peripheralsConfig.setAttenuatorSetValue(attenuatorSetUnderTest, attenuatorSetUnderTest.getDefaultValueAttenuation());
		setAllNetspanProfilesToDefault(secondDut);
		rebootAndWaitForAllRunning(dut,EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
		try {
			afterTest();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//@Test
	@Deprecated
	@TestProperties(name = "Connection_State_Idle_Mode-Re-Selection_Different_TAI", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void Connection_State_Idle_Mode_ReSelection_Different_TAI() {
		resetStatistics(dut, mibsInTest);

	}

	//@Test
	@Deprecated
	@TestProperties(name = "Barring Data for others (Default users)", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful","SecondSingleDUT" })
		public void Barring_Data_for_others() {
		preTest();
		resetStatistics(dut, mibsInTest);
		//set MIBS to known Values
		dut.setBarringValues(0,0,7,7,1,1,1);
		//enable ue + start traffic
		setIperfTimeAndEnablePlusStartUE(10);
		//wait 30 secs
		Wait(30*1000);
		//make sure UE is not Connected
		if(checkEPCForUEToActiveEnb(dut, ueList.get(0))){
			reason = "UE should not be connected";
			report.report(reason, Reporter.FAIL);
		}
		//set mibs to known values
		dut.setBarringValues(15,15,0,0,0,0,0);
		
		ueList.get(0).stop();
		ueList.get(0).start();
		Wait(5*60*1000);
		//make sure UEs are attached ot enb
		if(!checkEPCForUEToActiveEnb(dut, ueList.get(0))){
			reason = "Failing test since UE is not connected with EPC";
			report.report(reason, Reporter.FAIL);
		}
		try {
			afterTest();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// --------------------------------------------------------------------------------------------TAI
	// Tests

	// --------------------------------------------------------------------------------------------INTEGRITY&CIPHERING
	// Tests

	/*@Test
	@TestProperties(name = "Integrity (Authentication)  Null Level", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void Integrity_Null_Level() {
		SecurityParameters secProfParams = new SecurityParameters(dut.defaultNetspanProfiles.getSecurity(),
				SecurityProfileOptionalOrMandatory.OPTIONAL, 3, 2, 2, SecurityProfileOptionalOrMandatory.OPTIONAL, 1,
				0, 0);
		integrityAndCipheringTEST(secProfParams);
	}*/
	
	//@Test
	@TestProperties(name = "Integrity (Authentication)  AES", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" ,"SecondSingleDUT"})
	public void Integrity_AES_Level() {
		SecurityParameters secProfParams = new SecurityParameters(dut.defaultNetspanProfiles.getSecurity(),
				SecurityProfileOptionalOrMandatory.MANDATORY, 2, 3, 2, SecurityProfileOptionalOrMandatory.MANDATORY, 2,
				3, 2);
		integrityAndCipheringTEST(secProfParams);
	}

	//@Test
	@TestProperties(name = "Integrity (Authentication)  SNOW", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" ,"SecondSingleDUT"})
	public void Integrity_SNOW_Level() {
		SecurityParameters secProfParams = new SecurityParameters(dut.defaultNetspanProfiles.getSecurity(),
				SecurityProfileOptionalOrMandatory.MANDATORY, 2, 2, 3, SecurityProfileOptionalOrMandatory.MANDATORY, 2,
				2, 3);
		integrityAndCipheringTEST(secProfParams);
	}

	/*@Test
	@TestProperties(name = "Ciphering (Authentication)  Null Level", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void Ciphering_Null_Level() {
		SecurityParameters secProfParams = new SecurityParameters(dut.defaultNetspanProfiles.getSecurity(),
				SecurityProfileOptionalOrMandatory.MANDATORY, 3,2,2, SecurityProfileOptionalOrMandatory.MANDATORY, 3,
				2, 2);
		integrityAndCipheringTEST(secProfParams);
	}*/
	//@Test
	@TestProperties(name = "Ciphering (Authentication)  AES", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful","SecondSingleDUT" })
	public void Ciphering_AES_Level() {
		SecurityParameters secProfParams = new SecurityParameters(dut.defaultNetspanProfiles.getSecurity(),
				SecurityProfileOptionalOrMandatory.MANDATORY, 2, 3, 2, SecurityProfileOptionalOrMandatory.MANDATORY, 2,
				3, 2);
		integrityAndCipheringTEST(secProfParams);
	}

	//@Test
	@TestProperties(name = "Ciphering (Authentication)  SNOW", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" ,"SecondSingleDUT"})
	public void Ciphering_SNOW_Level() {
		SecurityParameters secProfParams = new SecurityParameters(dut.defaultNetspanProfiles.getSecurity(),
				SecurityProfileOptionalOrMandatory.MANDATORY, 2, 2, 3, SecurityProfileOptionalOrMandatory.MANDATORY, 2,
				2, 3);
		integrityAndCipheringTEST(secProfParams);
	}


	public void integrityAndCipheringTEST(SecurityParameters secProfParams) {
		preTest();
		resetStatistics(dut, mibsInTest);
		changeSecurityProfileNetspan(dut, secProfParams);
		rebootAndWaitForAllRunning(dut,EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
		// set UE in the same Authentication mode as the enb
		setIperfTimeAndEnablePlusStartUE(10);
		report.report("waiting "+SoftwareUtiles.milisToFormat(ONE_MIN) +" seconds");
		Wait(ONE_MIN);
		if (!checkEPCForUEToActiveEnb(dut, ueList.get(0))) {
			reason = "Failing test since UE is not connected with EPC";
			report.report(reason, Reporter.FAIL);
		}
		GeneralUtils.printToConsole("Stopping ue");
		ueList.get(0).stop();
		report.report("waiting "+SoftwareUtiles.milisToFormat(DEFAULT_GRANULARITY_TIME)+" minutes for Granularity");
		Wait(DEFAULT_GRANULARITY_TIME);
		checkMibsForResults(dut, 1);
		
         
		changeSecurityProfileToDefault(dut);
		rebootAndWaitForAllRunning(dut,EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
		try {
			afterTest();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// --------------------------------------------------------------------------------------------INTEGRITY&CIPHERING
	// Tests

	/**
	 * @author sshahaf 18/10/2016
	 * @return false if not in idle mode and true if in idle mode = connected to Epc and not to ENB
	 */
	/*private Boolean checkForIdleMode(EnodeB enb,UE ue){
		Boolean connectToEnb = false;
		Boolean connectToEpc = false;
		try{
		report.startLevel("Checking for Idle Mode");
		if(checkUEConnectionToEPC(ue)){
			connectToEpc = true;
			report.report("UE is registered only in epc!");
			if(checkEPCForUEToActiveEnb(enb, ue)){
				connectToEnb = true;
				report.report("UE is registered under some ENB ");
			}
		}
		}catch(Exception e){
			e.printStackTrace();
			report.report("Exception In CheckForIdleMode method Check Console");
		}
		finally{
			try{
				report.stopLevel();
			}catch(Exception d){
				d.printStackTrace();
			}
		}
		if(connectToEpc == true && connectToEnb == false){
			return true;
		}else
			return false;
		
	}*/
	
	/**
	 * set parameters to security profile object
	 * 
	 * @author Shahaf Shuhamy
	 * @param enb
	 * @param integLevMode
	 * @param integNullLev
	 * @param integAesLev
	 * @param integSNWLev
	 * @param cipherLevMode
	 * @param cipherNullLev
	 * @param cipherAESLev
	 * @param cipherSNWLev
	 * @return
	 */
	private SecurityParameters insertSecParams(EnodeB enb, SecurityProfileOptionalOrMandatory integLevMode,
			int integNullLev, int integAesLev, int integSNWLev, SecurityProfileOptionalOrMandatory cipherLevMode,
			int cipherNullLev, int cipherAESLev, int cipherSNWLev) {
		SecurityParameters sp = new SecurityParameters();
		sp.setProfileName(enb.defaultNetspanProfiles.getSecurity() + GeneralUtils.getPrefixAutomation());
		sp.setIntegrityMode(integLevMode);
		sp.setIntegrityNullLevel(integNullLev);
		sp.setIntegrityAESLevel(integAesLev);
		sp.setIntegritySNOWLevel(integSNWLev);
		sp.setCipheringMode(cipherLevMode);
		sp.setCipheringNullLevel(cipherNullLev);
		sp.setCipheringAESLevel(cipherAESLev);
		sp.setCipheringSNOWLevel(cipherSNWLev);
		return sp;
	}

	/**
	 * @author Shahaf Shuhamy setting iperf time, setting ue to enable and start
	 *         iperf
	 * @param timeForIperf
	 */
	private void setIperfTimeAndEnablePlusStartUE(int timeForIperf) {
		try {
			GeneralUtils.printToConsole("Setting time of Iperf to "+timeForIperf+" minutes");
			iperf.setIperfTime(timeForIperf * 60);
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		setSingelUEToEnableMode(ueList, 0);
		GeneralUtils.printToConsole("Waiting 5 seconds for ue to stabilize");
		Wait(5 * 1000);
		report.report("Starting traffic");
		iperf.postHttpForSingleUE(ueList.get(0).getLanIpAddress());
		Wait(5*1000);
	}

	/**
	 * @author Shahaf Shuhamy method waits and handle exception
	 * @param number in Millis
	 */
	private void Wait(long numberOfMillis) {
		try {
			GeneralUtils.printToConsole("Waiting "+SoftwareUtiles.milisToFormat(numberOfMillis) +" seconds");
			Thread.sleep(numberOfMillis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void setAllNetspanProfilesToDefault(EnodeB enb) {
		enbConfig.setEnbRadioProfile(enb, defaultProfilesSecondDut.getRadio(),false);
		enbConfig.setProfile(enb, EnbProfiles.Network_Profile, defaultProfilesSecondDut.getNetwork());
		enbConfig.deleteClonedProfiles();
	}

	/**
	 * @author Shahaf Shuhamy setting radio and network profile to the second
	 *         enodeB as the first EnodeB make sure only the right parameters
	 *         are inserted
	 */
	private void setSecondEnbProfiles() {
		// radio profile for earfcn
		// String radioProfileFromSut =
		// dut.getDefaultNetspanProfiles().getRadio();
		report.report("getting Radio Parameters From node: " + secondDut.getNetspanName());
		// sends original radio profile and the second dut to set it the orginal
		// radio profile.
		RadioParameters rp = null;
		try {
			rp = enbConfig.getRadioProfile(secondDut);
		} catch (Exception e1) {
			report.report("Could not init radio profile via snmp or netspan!");
		}
		// delete radio profile since it cant be set as a noraml profile
		
		if(netspanServer.isProfileExists(secondDut.defaultNetspanProfiles.getRadio() + "_NBI_Auto" + secondDut.getCellId(), EnbProfiles.Radio_Profile )){
			enbConfig.deleteEnbProfile(secondDut.defaultNetspanProfiles.getRadio() + "_NBI_Auto" + secondDut.getCellId(),
					EnbProfiles.Radio_Profile);
		}
		
		//cloneing Radio Proile logs in a Headline

		enbConfig.cloneAndSetRadioProfileViaNetSpan(secondDut, secondDut.defaultNetspanProfiles.getRadio(), rp);
		GeneralUtils.stopLevel();
		
		//cloneing networkProfile
		String firstDUTNetworkProfile = secondDut.getDefaultNetspanProfiles().getNetwork();
		try {
			NetworkParameters np = new NetworkParameters();
			report.startLevel("Cloning network profile");
			report.report("setting network parameters to network Class");
			np = enbConfig.getNetworkParameters(firstDUTNetworkProfile, np);
			report.report("Trying to Clone and Set Network Profile to netspan");
			np.setProfileName(firstDUTNetworkProfile + GeneralUtils.getPrefixAutomation());
			enbConfig.cloneAndSetNetworkProfileViaNetSpan(secondDut, firstDUTNetworkProfile, np);
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally{
			try{
				report.stopLevel();
			}catch(Exception f){
				f.printStackTrace();
			}
		}
		
	}

	/**
	 * @author Shahaf Shuhamy for future change in the mibs list should change
	 *         only here
	 * @param mIBS_TO_RESET
	 */
	protected void updateMibCollectionForTest(ArrayList<String> mIBS_TO_RESET) {
		mIBS_TO_RESET.add("ConnEstabAttSum");
		mIBS_TO_RESET.add("ConnEstabSuccSum");
	}

	/**
	 * setting UEs to stop mode setting ENBs to Out Of Service except for the
	 * DUT one.
	 */
	protected void preTest() {
		try {
			report.startLevel("Disable UEs and Irrelevant ENBs");
			setAllUEsToDisableMode(ueList);
			setAllENBsToOOSExceptForDut();
			reason = "";
			report.stopLevel();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @author Shahaf Shuhamy change DUT State to "In_Service" and the rest of
	 *         the ENBs to "Out_of_Service" first using netspan and if fail -
	 *         using SNMP.
	 */
	protected void setAllENBsToOOSExceptForDut() {
		report.report("EnodeBs Disabling");
		peripheralsConfig.changeEnbState(dut, EnbStates.IN_SERVICE);
		@SuppressWarnings("unchecked")
		ArrayList<EnodeB> toOOS = (ArrayList<EnodeB>) enbInSetup.clone();
		toOOS.remove(dut);
		for (EnodeB enb : toOOS) {
			peripheralsConfig.changeEnbState(enb, EnbStates.OUT_OF_SERVICE);
		}
	}

	/**
	 * @author Shahaf Shuhamy set All UES to stop mode vai SNMP
	 * @param ueList
	 */
	protected void setAllUEsToDisableMode(ArrayList<UE> ueList) {
		report.report("Disabling UES to Stop");
		for (UE ue : ueList) {
			ue.stop();
		}
	}

	/**
	 * reset All MIBs Indexes according to Test plan located in MIBS_TO_RESET
	 * 
	 * asLteStatsRrcConnEstabAttSum =0 asLteStatsRrcConnEstabSuccSum =0
	 * asLteStatsRrcUeContextRelSuccNbr =0 asLteStatsRrcUeContextRelReqSum =0
	 * 
	 * @param dutInMethod
	 * @param MIBS_TO_RESET_IN_TEST
	 * 
	 */
	protected void resetStatistics(EnodeB dutInMethod, ArrayList<String> MIBS_TO_RESET_IN_TEST) {
		try {
			report.startLevel("reseting db values to 0 for eNB "+dutInMethod.getNetspanName());
			for (String row : MIBS_TO_RESET_IN_TEST) {
				dutInMethod.dbSet("RrcStats", "*", row, "0");
			}

			report.report("connEstabAttSum  = " + dut.connectionEstabAttSumSNMP());
			report.report("connEstabSuccSum = " + dut.connectionEstabAttSuccSNMP());
			report.report("-------------------------------------------------------");

			try {
				report.report("Waiting for 5 minutes to change resets");
				Thread.sleep(5*60*1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			report.report("Reseting mibs again");
			for (String row : MIBS_TO_RESET_IN_TEST) {
				dutInMethod.dbSet("RrcStats", "*", row, "0");
			}
			
			report.report("printing MIBS After :");
			report.report("connEstabAttSum  = " + dut.connectionEstabAttSumSNMP());
			report.report("connEstabSuccSum = " + dut.connectionEstabAttSuccSNMP());
			report.report("-------------------------------------------------------");
			
		} catch (Exception d) {
			report.report(d.getMessage());
		}
		try {
			report.stopLevel();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * getSecurity Profile change/clone and update it - ask somone who knows
	 * 
	 * @throws Exception
	 */
	protected void changeSecurityProfileNetspan(EnodeB enb, SecurityParameters cloneProfileParams) {
		try {
			report.startLevel("Changeing Security profile : " + enb.defaultNetspanProfiles.getSecurity() + " in netspan for eNodeB " + enb.getNetspanName() );
			
			enbConfig.cloneAndSetSecurityProfileViaNetSpan(enb, enb.defaultNetspanProfiles.getSecurity(), cloneProfileParams);
			//for exception getting parameters from snmp and closing level
		} catch (Exception e) {
			report.report(e.getMessage()
					+ "Error happend while trying to clone and set Security Profile with Netspan, trying settings with SNMP");
			updateSecurityProfileWithSnmp(enb, cloneProfileParams);
		}
		//closing level to end method
		try{
			report.stopLevel();
		}catch(IOException d1){
			d1.printStackTrace();
		}
	}

	/**
	 * @author Shahaf Shuhamy Updating Security Profile with SNMP only values.
	 *         null integrity AES integrity SNOW integrity null ciphering AES
	 *         ciphering SNOW ciphering
	 * @param cloneProfileParams
	 */
	private void updateSecurityProfileWithSnmp(EnodeB enb, SecurityParameters cloneProfileParams) {
		if (enb.updateIntegrityNull(cloneProfileParams.getIntegrityNullLevel())
				|| enb.updateIntegrityAES(cloneProfileParams.getIntegrityAESLevel())
				|| enb.updateIntegritySNW(cloneProfileParams.getIntegritySNOWLevel())
				|| enb.updateCipheringNull(cloneProfileParams.getCipheringNullLevel())
				|| enb.updateCipheringAES(cloneProfileParams.getCipheringAESLevel())
				|| enb.updateCipheringSNW(cloneProfileParams.getCipheringSNOWLevel())) {
			report.report("setting profile values with snmp succeded");
		} else {
			report.report("Could not set snmp value in security profile values");
		}

	}

	/**
	 * ping to the number of ue in the list using simple cmd ping
	 * 
	 * @author Shahaf Shuhamy
	 * @param ueListInMethod
	 * @param UEIndex
	 */
	protected void pingToUe(ArrayList<UE> ueListInMethod, int UEIndex) {
		ArrayList<UE> listOfUEsToPing = new ArrayList<UE>();
		listOfUEsToPing.add(ueListInMethod.get(0));
		try {
			Traffic.pingToUEs(listOfUEsToPing);
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * stopping ping useing Traffic
	 */
	protected void stopPingingUEs() {
		try {
			Traffic.stopPinging();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * set only the Index number gets from the list gets to start.
	 * 
	 * @author Shahaf Shuhamy
	 * @param ueListInMethod
	 * @param numberOfUeIndexToEnable
	 */
	protected void setSingelUEToEnableMode(ArrayList<UE> ueListInMethod, int numberOfUeIndexToEnable) {
		GeneralUtils.printToConsole("enable ue " + ueListInMethod.get(numberOfUeIndexToEnable).getName());
		ueListInMethod.get(numberOfUeIndexToEnable).start();
	}

	/**
	 * method decides if the UE registered in EPC with the ENB Fail the test if
	 * not.
	 * 
	 * @param dutInMethod
	 * @param ueInMethod
	 */
	protected boolean checkEPCForUEToActiveEnb(EnodeB dutInMethod, UE ueInMethod) {
		try {
			ArrayList<UE> ueListInMethod = new ArrayList<UE>();
			ueListInMethod.add(ueInMethod);
			short numb = 1;
			if (!peripheralsConfig.checkIfAllUEsAreConnectedToNode(ueListInMethod, dutInMethod)) {
				return false;
			}
			return true;
		} catch (Exception e) {
			report.report("Exception accured while checking Epc: " + e.getMessage());
			return false;
		}
	}

	/**
	 * @author Shahaf Shuhamy
	 * getting true/false in case the UE is registered in EPC.
	 * @return
	 */
	protected boolean checkUEConnectionToEPC(UE ue){
		ArrayList<UE> uesToCheck = new ArrayList<UE>();
		uesToCheck.add(ue);
		try {
			if(peripheralsConfig.checkUesConnectionEPC(uesToCheck)){
				return true;
			}
		} catch (Exception e) {
			report.report("Exception in checkUEConnectionToEPC method");
			e.printStackTrace();
			return false;
		}
		return false;
	}
	
	/**
	 * checking mibs are equal to the result sends in the method's result
	 * parameter.
	 * 
	 * @param dut
	 * @param result
	 * @return true if at least one is equal to result and false if neither of
	 *         them.
	 */
	protected boolean checkMibsForResults(EnodeB dut, int result) {
		try {
			report.startLevel("Checking Mibs for eNB "+dut.getNetspanName());
		} catch (IOException e) {
			e.printStackTrace();
		}
		boolean methodResult;
		int connEstabAttSum = 0;
		int connEstabSuccSum = 0;
		connEstabAttSum = dut.connectionEstabAttSumSNMP();
		connEstabSuccSum = dut.connectionEstabAttSuccSNMP();
		if ((connEstabAttSum == result) && (connEstabSuccSum == result)) {
			report.report("connEstabAttSum = " + connEstabAttSum);
			report.report("connEstabSuccSum = " + connEstabSuccSum);
			methodResult = true;
		} else {
			report.report("MIB connEstabAttSum : " + connEstabAttSum + ", expected " + result,Reporter.WARNING);
			report.report("MIB connEstabAttSum : " + connEstabSuccSum + ", expected " + result,Reporter.WARNING);
			if(reason == ""){
				reason = "MIBs are not according to required results";
			}
			methodResult = false;
		}

		try {
			report.stopLevel();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return methodResult;

	}

	/**
	 * @author Shahaf Shuhamy getting security profile and updating it to a new
	 *         profile then setting it to Netspan
	 *         updated 20/10/2016
	 */
	protected void changeSecurityProfileToDefault(EnodeB enb) {
		report.report("setting back security profile " + dut.defaultNetspanProfiles.getSecurity() + "to ENodeB: "+enb.getName());
		enbConfig.changeSecurityProfileBack(enb);
		try{
		report.startLevel("deleating profiles from test causes");
		enbConfig.deleteClonedProfiles();
		enbConfig.clonedProfiles.clear();
		}catch(Exception e){
			e.printStackTrace();
		}
		finally{
			try{
			report.stopLevel();
			}catch(Exception d){
				d.printStackTrace();
			}
		}
	}

	/**
	 * @author Shahaf Shuhamy setting all ues back to enabled mode and setting
	 *         all enodeBs back to in service mode
	 * @throws IOException
	 */
	protected void afterTest() throws IOException {
		try {
			report.startLevel("Enabling UEs and All ENBs");
			setAllUEsToEnableMode(ueList);
			setAllENBsToInService();
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally{
			report.stopLevel();
		}
	}

	/**
	 * setting EnodeBs back to in service
	 */
	protected void setAllENBsToInService() {
		for (EnodeB enb : enbInSetup) {
			peripheralsConfig.changeEnbState(dut, EnbStates.IN_SERVICE);
		}
	}

	/**
	 * @author Shahaf Shuhamy setting all Ues to enable = start
	 * @param ueList2
	 */
	protected void setAllUEsToEnableMode(ArrayList<UE> ueList2) {
		for (UE ue : ueList2) {
			ue.start();
		}
	}

	/**
	 * @author Shahaf Shuhamy set Dut enodeB
	 * @return
	 */
	@ParameterProperties(description = "Name of first Enodeb Which the test will be run On")
	public void setDUT(String dut) {
		this.dut = (EnodeB) SysObjUtils.getInstnce().initSystemObject(EnodeB.class, false, dut).get(0);
	}

	@ParameterProperties(description = "Name of second Enodeb Which the test will be run On")
	public void setSecondDUT(String dut) {
		this.secondDut = (EnodeB) SysObjUtils.getInstnce().initSystemObject(EnodeB.class, false, dut).get(0);
	}

	/**
	 * @author Shahaf Shuhamy get Dut enodeB
	 * @return
	 */
	public String getDUT() {
		return this.dut.getNetspanName();
	}

	public String getSecondDut() {
		return this.secondDut.getNetspanName();
	}

	public ArrayList<UE> getUES(EnodeB enb) throws Exception {
		ArrayList<UE> staticUES = SetupUtils.getInstance().getStaticUEs(dut);
		ArrayList<UE> dynamicUES = SetupUtils.getInstance().getDynamicUEs();
		ArrayList<UE> retUes = new ArrayList<>();
		if (staticUES != null)
			retUes.addAll(staticUES);
		if (dynamicUES != null)
			retUes.addAll(dynamicUES);

		return retUes;
	}

	public void rebootAndWaitForAllRunning(EnodeB enb, long timeTillAllRunning){
		try{
		report.startLevel("rebooting and waiting for all running state for enodeB - "+enb.getNetspanName());
		enb.reboot();
		enb.waitForAllRunningAndInService(timeTillAllRunning);
		}catch(Exception e){
			e.printStackTrace();
		}
		
		finally{
			try {
				report.stopLevel();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	protected boolean granularityChange(String key, String value) {
		try {
			report.startLevel(
					"Changing granularity period in enodeB " + dut.getNetspanName() + " to " + value + " minutes");
		} catch (IOException e) {
			e.printStackTrace();
		}
		boolean resultChangeAction = false;
		for (int i = 0; i < 2; i++) {
			resultChangeAction = dut.dbSet("statsConfig", key, "sampleInterval", value);
			if (resultChangeAction) {
				report.report("Granularity period has been changed to " + value + " minutes");
				try {
					report.stopLevel();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return true;
			}
		}
		report.report("Granularity period couldn't been changed");
		try {
			report.stopLevel();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
}

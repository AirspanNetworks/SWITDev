package testsNG.ProtocolsAndServices.OTDOA;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.snmp4j.smi.Variable;

import EnodeB.AirVelocity;
import EnodeB.EnodeB;
import IPG.IPG;
import Netspan.EnbProfiles;
import Netspan.NetspanServer;
import Netspan.API.Enums.EnbStates;
import Netspan.Profiles.NetworkParameters;
import Netspan.Profiles.RadioParameters;
import Netspan.Profiles.RadioParameters.PRSBandWidthEnum;
import Netspan.Profiles.RadioParameters.PRSMutingPeriodiclyEnum;
import Netspan.Profiles.RadioParameters.PRSPeriodiclyEnum;
import Utils.GeneralUtils;
import Utils.SetupUtils;
import Utils.GeneralUtils.CellIndex;
import Utils.SysObjUtils;
import jsystem.framework.ParameterProperties;
import jsystem.framework.report.Reporter;
import jsystem.framework.system.SystemManagerImpl;
import testsNG.TestspanTest;
import testsNG.Actions.EnodeBConfig;
import testsNG.Actions.PeripheralsConfig;
import testsNG.Actions.Traffic;

public class OtdoaBase extends TestspanTest{
	protected EnodeB dut;
	protected EnodeB dut2;
	protected EnodeBConfig enodeBConfig;
	protected boolean radioChanged;                 
	protected boolean networkChanged;             
	protected NetspanServer netspan;
	protected PeripheralsConfig peripheralsConfig;
	protected Traffic traffic = null;
	protected IPG ipg; 
	protected int a = 4;
	protected String mmeIP;
	protected String rnti="defaultValue";
	protected String cellIndex="0";
	protected String offSet="4";
	protected String trig="0";    
	
	//lppa message parameters
	private String pci;
	private String tac;
	private String earfcn;
	private String bandwidth;
	private String prsCfgIdx;
	private String cpLength;
	private String numOfDlFrames;
	private String numOfAntennaPorts;
	private String sfnInitTime;
	private String latitude;
	private String longitude;
	private String altitude;
	private String bitStr;
	private String localRadioProfile;
	private String localRadioProfileCell2;
	
	@Override
	public void init() throws Exception{
		GeneralUtils.startLevel("Init Test");
		enbInTest = new ArrayList<>();
		enbInTest.add(dut);
		if(dut2 != null) {
			enbInTest.add(dut2);
		}
		super.init();
		peripheralsConfig = PeripheralsConfig.getInstance();
		enodeBConfig = EnodeBConfig.getInstance();
		netspan = NetspanServer.getInstance();
		traffic = Traffic.getInstance(SetupUtils.getInstance().getAllUEs());
		configureNodesState(EnbStates.OUT_OF_SERVICE);
		rnti="defaultValue";
		GeneralUtils.stopLevel();
	}
	
	/**
	 * 
	 * @param - true to Enable IPG
	 * @return
	 */
	public boolean preTestIPGTests(boolean ipgEnable){
		boolean result = false;
		if(ipgEnable){
			report.report("Validating IPG configuration in SUT");
			try {
				ipg = (IPG) SystemManagerImpl.getInstance().getSystemObject("IPG");
				ipg.setRealMME(enodeBConfig.getMMeIpAdress(dut));
				mmeIP = enodeBConfig.getMMeIpAdress(dut);
				result = true;
			} catch (Exception e) {
				report.report("IPG Configuration is NOT valid!");
				e.printStackTrace();
			}
		}
		boolean actionResult = false;
		if(dut instanceof AirVelocity){
			report.report("EnodeB has no GPS - injecting default GPS values for test");
			actionResult = enodeBConfig.setLatitudeAndLongitude(dut,new java.math.BigDecimal(31.987383),new java.math.BigDecimal(34.912389));
		}
		
		GeneralUtils.printToConsole("IPG init = "+result);
		GeneralUtils.printToConsole("set GPS coordinets = "+actionResult);
		return result;// && actionResult;
		
	}
	
	protected void reportConfiguration(boolean ecid,Integer ecidTimer, boolean otdoa,String subFrames, PRSBandWidthEnum prsBW,
			PRSPeriodiclyEnum prsPeriodicity, Integer prsOffSet,Integer prsPowerOffset, PRSMutingPeriodiclyEnum prsMutingPeri, String prsMutingPattern,Boolean isFDD,Integer frameConfig,String cellBandWidth) {
		GeneralUtils.startLevel("Configuration of test to clone radio profile");
		report.report("E-CID mode: "+(ecid?"Enabled":"Disabled"));
		report.report("E-CID procedure Timer: "+ecidTimer);			
		report.report("OTDOA mode: "+(otdoa?"Enabled":"Disabled"));
		report.report("Subframes: "+subFrames);
		report.report("PRS Bandwidth: "+prsBW.getPRS());
		report.report("PRS periodicity: "+prsPeriodicity.getPRS());
		report.report("PRS Offset: "+prsOffSet);
		report.report("PRS Power Offset: "+prsPowerOffset);
		report.report("PRS Muting Periodicity: "+prsMutingPeri.getPRS());
		report.report("PRS Muting Pattern: "+prsMutingPattern);
		report.report("Frame config: "+frameConfig);
		if(cellBandWidth!=null){
			report.report("Cell band width: "+cellBandWidth);			
		}
		GeneralUtils.stopLevel();
	}
	
	/**
	 * 
	 * @param dut node.
	 * @param ecid true - Enable/ false - Disable.
	 * @param ecidTimer - Integer.
	 * @param otdoa true - Enable/ false - Disable.
	 * @param subFrames - Integer.
	 * @param prsBW - String with set Validation for RadioParameters Class.
	 * @param prsPeriodicity - Integer.
	 * @param prsOffSet - Integer.
	 * @param prsPowerOffset - String with set Validation for RadioParameters Class.
	 * @param prsMutingPeri - String with set Validation for RadioParameters Class.
	 * @param prsMutingPattern - String with set Validation for RadioParameters Class.
	 * @param isFDD - boolean for FDD mode.
	 * @param frameConfig - represents frameConfig in case of isFDD is false.
	 * @return
	 */
	protected boolean setRadioWithNetspan(EnodeB dut,boolean ecid,Integer ecidTimer, boolean otdoa,String subFrames, PRSBandWidthEnum prsBW,
			PRSPeriodiclyEnum prsPeriodicity, Integer prsOffSet,Integer prsPowerOffset, PRSMutingPeriodiclyEnum prsMutingPeri, String prsMutingPattern,Boolean isFDD,Integer frameConfig,String cellBandWidth) {
		GeneralUtils.startLevel("Setting Radio Profile");
		RadioParameters radioParams = new RadioParameters();
		//ECID
		radioParams.setECIDMode(ecid);
		if(ecidTimer!=null){
			radioParams.setECIDProcedureTimer(ecidTimer);
		}
		//OTDOA
		radioParams.setOTDOAMode(otdoa);
		if(otdoa){
				if(subFrames!=null) radioParams.setSubframes(subFrames);
				if(prsBW!=null) radioParams.setPRSBandWidth(prsBW);
				if(prsPeriodicity!=null) radioParams.setPRSPeriodicly(prsPeriodicity);
				if(prsOffSet!=null) radioParams.setPRSOffset(prsOffSet);
				if(prsPowerOffset!=null) radioParams.setPRSPowerOffset(prsPowerOffset);
				if(prsMutingPeri!=null) radioParams.setPRSMutingPeriodicly(prsMutingPeri);
				if(prsMutingPattern!=null) radioParams.setPRSMutingPattern(prsMutingPattern);
			if(isFDD != null){
				if(isFDD == false){
					radioParams.setDuplex("2");
					radioParams.setFrameConfig(frameConfig.toString());
					
					if(cellBandWidth != null){
						radioParams.setBandwidth(cellBandWidth);
					}
				}
			}
		}
		
		String profileFromCell = netspan.getCurrentRadioProfileName(dut);
		if(dut.getCellContextID() == 1){
			localRadioProfile = profileFromCell;
		}else{
			localRadioProfileCell2 = profileFromCell;
		}
		
		boolean result = enodeBConfig.cloneAndSetProfileViaNetSpan(dut, profileFromCell, radioParams);
		
		if(result){
			radioChanged = true;
		}
		GeneralUtils.stopLevel();
		return result;
	}
	
	protected boolean setNetworkWithNetspan(EnodeB dut){
		GeneralUtils.startLevel("Setting Network Profile with 2 MME's for IPG");
		
		NetworkParameters np = new NetworkParameters();
		np.setMMEIPS(ipg.getFakeIP(),mmeIP);
		
		GeneralUtils.printToConsole("Setting Network profile parameters:");
		GeneralUtils.printToConsole("Fake MME Ip: "+ipg.getFakeIP());
		GeneralUtils.printToConsole("Real MME IP: "+mmeIP);
		
		
		boolean result = enodeBConfig.cloneAndSetNetworkProfileViaNetSpan(dut, dut.getDefaultNetspanProfiles().getNetwork(), np);
		if(result){
			networkChanged = true;
		}
		GeneralUtils.stopLevel();
		
		return result;
	}
	
	protected boolean verifyOTDOAAndECIDWithSNMP(Boolean ECID,Integer ecidTimer, Boolean OTDOA,Integer subFrames, Integer prsBW,
			Integer prsPeriodicity, Integer prsOffSet,Integer prsPowerOffset, Integer prsMutingPeri, Integer prsMutingPattern) {
		GeneralUtils.startLevel("Verify OTDOA and ECID modes and parameters in SNMP");
		Boolean result = true;
		
		Boolean ECIDSnmp = dut.getECIDMode();
		Integer ecidTimerSnmp = dut.getECIDTimer();
		
		Boolean OTDOAModeSnmp = dut.getOTDOAMode(CellIndex.FORTY);
		Integer prsPowerOffSetSnmp = dut.getPrsPowerOffset();
		Integer prsBwSnmp = dut.getCfgPrsBandwidth();
		//formula
		Integer prsCfgIndexSnmp = dut.getPrsCfgIndex();
		Integer prsMutePeriodSnmp = dut.getCfgPrsMutePeriod();
		Integer prsMutingPatternSnmp = dut.getCfgPrsMutePattSeq();
		

		result &= verifyParameter("ECID",ECIDSnmp,ECID);
		result &= verifyParameter("OTDOA",OTDOAModeSnmp,OTDOA);
		result &= verifyParameter("ECID - Timer", ecidTimerSnmp, ecidTimer);
		result &= verifyParameter("power Off Set", prsPowerOffSetSnmp, prsPowerOffset);
		result &= verifyParameter("prs Band Width", prsBwSnmp, prsBW);
		result &= verifyParameter("Mute Pattern", prsMutingPatternSnmp, prsMutingPattern);
		result &= verifyParameter("Mute periodicity", prsMutePeriodSnmp, prsMutingPeri);
		
		Integer prsCfgIndex = 0;
		if(prsPeriodicity != null){
			switch(prsPeriodicity){
				case 160:{
					prsCfgIndex = prsOffSet + subFrames;
					break;
				}
				case 320:{
					prsCfgIndex = 160 + prsOffSet + subFrames;
					break;
				}
				case 640:{
					prsCfgIndex = 480 + prsOffSet + subFrames;
					break;
				}
				case 1280:{
					prsCfgIndex = 1120 + prsOffSet + subFrames;
					break;
				}
				default:{
					report.report("Wrong value for prePeriodicity: "+prsPeriodicity,Reporter.WARNING);
					break;
				}				
			}
		}
		
		if(prsOffSet != null){
			if(prsCfgIndex == 0){
				report.report("PRS Index error Value!");
				result &= false;
			}else{
				if((prsCfgIndexSnmp == prsCfgIndex + 1) || (prsCfgIndexSnmp == prsCfgIndex - 1) || (prsCfgIndexSnmp.equals(prsCfgIndex))){
					report.report("PRS Index value is as expected");
					result &= true;
				}else {
					report.report("PRS Index is not as expected value -  snmp Current Value : "+prsCfgIndexSnmp+"\n wanted Value : "+prsCfgIndex);
					result &= false;
				}
			}
		}
		
		GeneralUtils.stopLevel();
		return result;
	}
	
	private boolean verifyParameter(String parameterName, Object currentValue, Object expectedValue){
		boolean result = true;
		if(expectedValue == null){
			return false;
		}
		
		if(currentValue.equals(expectedValue)){
			report.report(parameterName+" value is as expected");
		}else{
			report.report(parameterName+" is not as expected value -  snmp Current Value : "+currentValue+"\n wanted Value : "+expectedValue);
			result = false;
		}
		return result;
	}
	
	protected boolean validateTestMode(EnodeB dut,String testDuplexMode) {
		boolean isFDD = enodeBConfig.isDuplexFdd(dut);
		boolean result = false;
		if(isFDD & testDuplexMode.equals("FDD")){
			report.report("dut is configured as FDD node");
			result = true;
		}
		
		if(!(isFDD) & testDuplexMode.equals("TDD")){
			report.report("dut is configured as TDD node");
			result = true;
		}
		
		if(!result){
			report.report("Node is not configured with the right duplex for the Test, Ending Test!",Reporter.FAIL);
		}
		return result;
	}
	
	/**
	 * @param ECID
	 * @param OTDOA
	 */
	protected void setOTDOAAndECIDValues(boolean ECID, boolean OTDOA) {
		GeneralUtils.startLevel("Setting SNMP Values :OTDOA to "+OTDOA+", ECID to "+ECID);
		dut.setECIDMode(ECID);
		dut.setOtdoaMode(OTDOA);
		GeneralUtils.stopLevel();
		
	}
	
	protected String sendLPPCommandWithIPG(IPG ipg) {
		//prepare IPG
		ipg.setScriptName("ManInTheOTDOA.js");
		Map<String,String> scriptParmas = new LinkedHashMap<String,String>();
		scriptParmas.put("fakeIP",ipg.getFakeIP());
		scriptParmas.put("realMME",ipg.getRealMME());
		scriptParmas.put("s1Ip",dut.getS1IpAddress());
		report.report("Connecting IPG and Running Script");
		ipg.connectIPG();
		
		//run script.
		ipg.runScript(scriptParmas);
		GeneralUtils.printToConsole("Start Time");
		String resa = ipg.readCommandLine(2*60*1000, "this is not an Error");
		GeneralUtils.printToConsole("End Time");
		GeneralUtils.printToConsole("Before Handling");
		resa = resa.trim();
		GeneralUtils.printToConsole(resa);
		if(resa.contains("<eval>:281 TypeError: Cannot get property \"object\" of null")) return ""; // case of answer from EnodeB.
			
		//extract packet parameters to local class parameters.
		report.report("Extracting Parameters from lppa Response packet");
		extractValuesFromString(resa);
		return resa;
	}
	
	private void extractValuesFromString(String string) {
		String cleanString;
		string.trim();
		int idex = string.indexOf("-----------------");
		int idexEnd = string.indexOf("Error: This is not an error"); //last message in script
		cleanString = string.substring(idex,idexEnd);	
		cleanString = cleanString.replaceAll("(?m)^[ \t]*\r?\n", "");//remove all blank lines
		String[] parameterLines = cleanString.split("\n");
		
		for(String parameterLine : parameterLines){
			if(parameterLine.contains("PCI") || parameterLine.contains("prsCfgIdx") || parameterLine.contains("latitude") 
					|| parameterLine.contains("longitude") || parameterLine.contains("altitude") ){
				extractParameterWithEquals(parameterLine);
			}else{
				extractParameterWithColons(parameterLine);
			}
		}
		GeneralUtils.printToConsole("Packet Parameters:");
		GeneralUtils.printToConsole(pci);
		GeneralUtils.printToConsole(tac);
		GeneralUtils.printToConsole(earfcn);
		GeneralUtils.printToConsole(bandwidth);
		GeneralUtils.printToConsole(prsCfgIdx);
		GeneralUtils.printToConsole(cpLength);
		GeneralUtils.printToConsole(numOfDlFrames);
		GeneralUtils.printToConsole(numOfAntennaPorts);
		GeneralUtils.printToConsole(sfnInitTime);
		GeneralUtils.printToConsole(latitude);
		GeneralUtils.printToConsole(longitude);
		GeneralUtils.printToConsole(altitude);
		GeneralUtils.printToConsole("End Packet Parameters");
	}

	private boolean extractParameterWithColons(String parameterLine) {
		boolean result = false;
		if(parameterLine != null && parameterLine.contains("::=")){
			String[] results = parameterLine.split("::=");
			GeneralUtils.printToConsole("parameter Name: "+results[0] +" ,Parameter Value: "+results[1]);
			
			if(parameterLine.contains("Bandwidth")){
				bandwidth = GeneralUtils.removeNonDigitsFromString(results[1]).trim();
				GeneralUtils.printToConsole("Extract method in BandWidth parameter:"+bandwidth);
				result = true;
			}
			
			if(parameterLine.contains("TAC")){
				tac = GeneralUtils.removeNonDigitsFromString(results[1]).trim();
				GeneralUtils.printToConsole("Extract method in Tac parameter:"+tac);
				result = true;
			}
			
			if(parameterLine.contains("EARFCN")){
				earfcn = GeneralUtils.removeNonDigitsFromString(results[1]).trim();
				GeneralUtils.printToConsole("Extract method in EARFCN parameter:"+earfcn);
				result = true;
			}
			
			if(parameterLine.contains("CPLength")){
				cpLength = results[1].trim();
				GeneralUtils.printToConsole("Extract method in CPLength parameter:"+cpLength);
				result = true;
			}
			
			if(parameterLine.contains("NumberOfDlFrames")){
				numOfDlFrames = results[1].trim();
				GeneralUtils.printToConsole("Extract method in NumberOfDlFrames parameter:"+numOfDlFrames);
				result = true;
			}
			
			if(parameterLine.contains("NumberOfAntennaPorts")){
				numOfAntennaPorts = results[1].trim();
				GeneralUtils.printToConsole("Extract method in NumberOfAntennaPorts parameter:"+numOfAntennaPorts);
				result = true;
			}
			
			if(parameterLine.contains("SFNInitialisationTime")){
				sfnInitTime = GeneralUtils.removeNonDigitsFromString(results[1]).trim();
				GeneralUtils.printToConsole("Extract method in SFNInitialisationTime parameter:"+sfnInitTime);
				result = true;
			}
			
			if(parameterLine.contains("bitStr")){
				bitStr = GeneralUtils.removeNonDigitsFromString(results[1]).trim();
				GeneralUtils.printToConsole("Extract method in bit Sequence parameter:"+bitStr);
				result = true;
			}
			
		}else{
			GeneralUtils.printToConsole("Error in Extracting parameter from a line : "+parameterLine);
		}
		return result;
	}

	private boolean extractParameterWithEquals(String parameterLine) {
		boolean result = false;
		if(parameterLine != null && parameterLine.contains("=")){
			String[] results = parameterLine.split("=");
			GeneralUtils.printToConsole("parameter Name: "+results[0] +" ,Parameter Value: "+results[1]);
			
			switch(results[0]){
			case "PCI":
				pci = results[1];
				GeneralUtils.printToConsole("extract Method: PCI value - "+pci);
				result = true;
				break;
			case "prsCfgIdx":
				prsCfgIdx = results[1];
				GeneralUtils.printToConsole("extract Method: prsCfgIdx value - "+prsCfgIdx);
				result = true;
				break;
			case "latitude":
				latitude = results[1];
				GeneralUtils.printToConsole("extract Method: latitude value - "+latitude);
				result = true;
				break;
			case "longitude":
				longitude = results[1];
				GeneralUtils.printToConsole("extract Method: longitude value - "+longitude);
				result = true;
				break;
			case "altitude":
				altitude = results[1];
				GeneralUtils.printToConsole("extract Method: altitude value - "+altitude);
				result = true;
				break;
			default:
				GeneralUtils.printToConsole("Parameter dosent fit packet structure!");
				break;
			}
		}else{
			GeneralUtils.printToConsole("Error in Extracting parameter from a line : "+parameterLine);
		}
		
		return result;
	}

	protected boolean validateAllParameters(){
		boolean result;
		GeneralUtils.startLevel("verify existence for all Parameters from LPPA response");
		result = (pci!=null);
		report.report("Pci:"+pci);
		
		result &= (tac!=null);
		report.report("Tac:"+tac);
		
		result &= (earfcn!=null);
		report.report("Earfcn:"+earfcn);
		
		result &= (bandwidth!=null);
		report.report("Bandwidth:"+bandwidth);
		
		result &= (prsCfgIdx!=null);
		report.report("PrsCfgIdx:"+prsCfgIdx);
		
		result &= (cpLength!=null);
		report.report("CpLength:"+cpLength);
		
		result &= (numOfDlFrames!=null);
		report.report("NumOfDlFrames:"+numOfDlFrames);
		
		result &= (numOfAntennaPorts!=null);
		report.report("NumOfAntennaPorts:"+numOfAntennaPorts);
		
		result &= (sfnInitTime!=null);
		report.report("SfnInitTime:"+sfnInitTime);
		
		result &= (latitude!=null);
		report.report("Latitude:"+latitude);
		
		result &= (longitude!=null);
		report.report("Longitude:"+longitude);
		
		result &= (altitude!=null);
		report.report("Altitude:"+altitude);
		
		result &= (bitStr!=null);
		report.report("BitStr:"+bitStr);
		
		GeneralUtils.stopLevel();
		return result;
	}
	
	protected void startTraffic(){
		report.report("Start traffic");
		try {
			traffic.startTraffic();
		} catch (Exception e) {
			e.printStackTrace();
			report.report("Failed to start traffic",Reporter.WARNING);
		}
	}
	
 	protected boolean resetCounter(String counterName){
		boolean counterIsZero = false;
		boolean result = false;
		int i=0;
		GeneralUtils.startLevel("Resetting counters");
		while(!counterIsZero){
			report.report("Resetting counter "+counterName);
			dut.resetCounter(null, null, null);
			enodeBConfig.waitGranularityPeriodTime(dut);
			report.report("Checking if counter - "+counterName+"="+0);
			
			if(validateCounterEquals(counterName,0)){
				result = true;
				counterIsZero = true;
			}else{
				i++;
				report.report("Counter wasn't 0. Retrying");
				if(i>=2){
					report.report("Counter has not been reset! Test going with a warning",Reporter.WARNING);
					result = false;
					counterIsZero = true;
				}
			}
			
		}
		GeneralUtils.stopLevel();
		return result;
	}
	
	protected boolean validateCounterEquals(String counterName, int value){
		boolean methodResult = false;
		int counterValueSnmp = dut.getCountersValue(counterName);
		if (counterValueSnmp != 0){
			if(enodeBConfig.getNumberOfActiveCells(dut) > 1){
				counterValueSnmp /= 2;
			}
		}
		
		GeneralUtils.printToConsole("counter: "+counterName +" result : "+counterValueSnmp);
		if(value == counterValueSnmp){
			methodResult = true;
		}
		return methodResult;
	}
	
	protected boolean checkCounter(String counterName, int value){
		int waitingPeriodTime = dut.getGranularityPeriod();
		if(waitingPeriodTime<0){
			waitingPeriodTime = 0;
		}
		report.report("Waiting "+waitingPeriodTime+" minutes for counter to update");
		GeneralUtils.unSafeSleep(waitingPeriodTime*60*1000);
		boolean result = false;
		if(validateCounterEquals(counterName, value)){
			report.report("Counter : "+counterName+", equals "+value+" as expected");
			result = true;
		}else{
			report.report("Counter : "+counterName+" have a different expected value",Reporter.FAIL);
		}
		return result;
	}
	
	/**
 	* method will check Cell OTDOA value if Expected boolean is not null.
 	* method will print a warning in the case OTDOA value is not as expected.
 	* @param cellOneIndx - 40
 	* @param cellOneExpectedValue
 	* @param cellTwoIndex - 41
 	* @param cellTwoExpectedValue
 	*/
	protected void verifyOTDOASnmp(CellIndex cellOneIndx,Boolean cellOneExpectedValue, CellIndex cellTwoIndex, Boolean cellTwoExpectedValue) {
		if(cellOneExpectedValue != null){
			singleCellOTDOAValidation(cellOneIndx, "One", cellOneExpectedValue);
		}
		
		if(cellTwoExpectedValue != null){
			singleCellOTDOAValidation(cellTwoIndex, "Two", cellTwoExpectedValue);
		}
	}

	private void singleCellOTDOAValidation(CellIndex cellIndex,String cellName,Boolean expectedValue){
		Boolean otodaSNMP = dut.getOTDOAMode(cellIndex);
		if(otodaSNMP.equals(expectedValue)){
			report.report("Cell "+cellName+" Otdoa Value is as Expected!");
		}else{
			report.report("Cell "+cellName+" OTDOA value is NOT as expected!",Reporter.WARNING);
		}
	}
	
	protected String otdoaCommand( ){
		return "ue simulate otdoameas cellIdx="+cellIndex+" offset="+offSet+" rnti="+rnti+" trig="+trig;
	}
	
	/**
	 * updating rnti test global parameter for command "ue simulate otdoameas"
	 */
	protected void getSingleUERNTI() {
		report.report("Getting Single UE RNTI");
		try{
			HashMap <String,Variable> a = dut.getUEShowLinkTable();
			if(a.isEmpty()) {
				report.report("No UEs shown in 'ue show link' table");
			}
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
	
	public void rebootNode(EnodeB node){
		GeneralUtils.startLevel("Reboot node and Wait for all running");
		node.reboot();
		node.waitForAllRunningAndInService(EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
		GeneralUtils.stopLevel();
	}

	public void setDUT(String dut) {
		this.dut = (EnodeB) SysObjUtils.getInstnce().initSystemObject(EnodeB.class,false,dut).get(0);
	}
	
	@ParameterProperties(description = "Second DUT")
	public void setDUT2(String dut) {
		this.dut2 = (EnodeB) SysObjUtils.getInstnce().initSystemObject(EnodeB.class, false, dut).get(0);
	}

	public String getDUT2(){
		return dut2.getNetspanName();
	}
	
	public String getDUT() {
		return this.dut.getNetspanName();
	}
	
	public void configureNodesState(EnbStates state){
		for (EnodeB enodeB : enbInSetup) {
			if (enodeB != dut) {
				GeneralUtils.printToConsole("Setting state : "+ state +" to node : "+enodeB.getNetspanName());
				peripheralsConfig.changeEnbState(enodeB, state);
			}			
		}
	}
	
	@Override
	public void end(){
		GeneralUtils.startLevel("End Test Configurations");
		report.report("Stop traffic");
		traffic.stopTraffic();
		if(networkChanged){
			GeneralUtils.startLevel("Revert to default Network profile to profile name : "+dut.getDefaultNetspanProfiles().getNetwork());
			enodeBConfig.revertToDefaultProfile(dut, EnbProfiles.Network_Profile);
			GeneralUtils.stopLevel();
		}
		
		if(radioChanged){
			if(enodeBConfig.getNumberOfActiveCells(dut) > 1){
				if(localRadioProfileCell2 != null){
					GeneralUtils.startLevel("Revert to default Radio profile to profile name : "+localRadioProfileCell2+" for Cell 2");
					dut.setCellContextNumber(2);
					enodeBConfig.setEnbRadioProfile(dut, localRadioProfileCell2, false);
					GeneralUtils.stopLevel();
				}
			}
			GeneralUtils.startLevel("Revert to default Radio_Profile to profile name : "+localRadioProfile+" for Cell 1");
			dut.setCellContextNumber(1);
			enodeBConfig.setEnbRadioProfile(dut, localRadioProfile, false);
			GeneralUtils.stopLevel();
		}
		
		if(radioChanged || networkChanged){
			GeneralUtils.startLevel("Deleting Cloned Profiles");
			enodeBConfig.deleteClonedProfiles();
			GeneralUtils.stopLevel();
			if(networkChanged){
				rebootNode(dut);
			}
		}
		
		configureNodesState(EnbStates.IN_SERVICE);
		radioChanged = false;
		networkChanged = false;
		GeneralUtils.stopLevel();
		super.end();
	}
}

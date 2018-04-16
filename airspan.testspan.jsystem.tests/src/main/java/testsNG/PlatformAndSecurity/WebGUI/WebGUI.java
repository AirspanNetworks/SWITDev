package testsNG.PlatformAndSecurity.WebGUI;

import java.util.ArrayList;

import org.apache.commons.net.util.SubnetUtils;
import org.junit.Test;
import EnodeB.EnodeB;
import EnodeB.WebGuiParameters;
import Utils.GeneralUtils;
import Utils.SysObjUtils;
import jsystem.framework.ParameterProperties;
import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;
import testsNG.TestspanTest;

public class WebGUI extends TestspanTest{
	private EnodeB dut;
	private String snmpSeperator;
	
	@Test
	@TestProperties(name = "Get Ip Address From Node WEB gui and verify with node", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
		"IsTestWasSuccessful"})
	public void getIpAddressFromWebGUIAndVerify(){
		boolean testResult = false;
		WebGuiParameters parameterInTest = WebGuiParameters.IP_ADDRESS;
		String ipAddressFromWeb = getTestParameter(parameterInTest);
		
		if(!isNull(ipAddressFromWeb)){
			String ipFromNode = dut.getIpAddress();
			testResult = ipAddressFromWeb.equals(ipFromNode);
			printTestResults(testResult, parameterInTest, ipAddressFromWeb);
			return;
		}
		report.report("result from Web GUI had nothing in it",Reporter.FAIL);
	}
	
	@Test
	@TestProperties(name = "Get Default Gate Way From Node WEB gui and verify with node", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
		"IsTestWasSuccessful"})
	public void getDefaultGWFromWebGUIAndVerify(){
		WebGuiParameters parameterInTest = WebGuiParameters.DEFAULT_GATE_WAY;
		String defaultGWFromWeb = getTestParameter(parameterInTest);
		boolean testResult = false;
		snmpSeperator = ":";
		
		if(!isNull(defaultGWFromWeb)){
			ArrayList<String> addressesInHex = dut.getDefaultGateWayAddresses();
			ArrayList<String> addressesInDecimal = transformHexAddressesToDecimal(addressesInHex);
			testResult = checkIfObjectIsInAListOfObjects(defaultGWFromWeb,addressesInDecimal);
			printTestResults(testResult,parameterInTest,defaultGWFromWeb);
			return;
		}
		report.report("result from Web GUI had nothing in it",Reporter.FAIL);
	}

	@Test
	@TestProperties(name = "Get Subnet Mask From Node WEB gui and verify with node", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
		"IsTestWasSuccessful"})
	public void getSubNetMaskFromWebGUIAndVerify(){
		WebGuiParameters parameterInTest = WebGuiParameters.SUBNET_MASK;
		String subNetMaskFromWeb = getTestParameter(parameterInTest);
		boolean testResult = false;
		snmpSeperator = ":";
		
		if(!isNull(subNetMaskFromWeb)){
			ArrayList<String> subNetMasksInDecimal = new ArrayList<>();
			String subNetMaskCIDR = dut.getExternalSubNetCIDR();
			String subNetFromSNMP = getSubNetMaskFromCIDR(subNetMaskCIDR);
			if(!isNull(subNetFromSNMP)){
				subNetMasksInDecimal.add(subNetFromSNMP);			
				testResult = checkIfObjectIsInAListOfObjects(subNetMaskFromWeb,subNetMasksInDecimal);
				printTestResults(testResult,parameterInTest,subNetMaskFromWeb);
				return;
			}
			report.report("CIDR from snmp could not be parsed to Subnetmask",Reporter.FAIL);
		}
		report.report("result from Web GUI had nothing in it",Reporter.FAIL);
	}
	
	

	@Test
	@TestProperties(name = "Get VLAN Tag Id From Node WEB gui and verify with node", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
		"IsTestWasSuccessful"})
	public void getVlanTagIdFromWebGUIAndVerify(){
		WebGuiParameters parameterInTest = WebGuiParameters.VLANTagId;
		String vlanTagIdFromWeb = getTestParameter(parameterInTest);
		boolean testResult = false;
		
		if(!isNull(vlanTagIdFromWeb)){
			ArrayList<String> vlansInDecimal = dut.getVlanIds();
			testResult = checkIfObjectIsInAListOfObjects(vlanTagIdFromWeb,vlansInDecimal);
			printTestResults(testResult,parameterInTest,vlanTagIdFromWeb);
			return;
		}
		report.report("result from Web GUI had nothing in it",Reporter.FAIL);
	}
	
	private void printTestResults(boolean testResult, WebGuiParameters parameterInTest, String parameterFromWeb) {
		if(testResult){
			report.report("Found Matching result in EnodeB for "+parameterInTest+" from Web : "+parameterFromWeb);
		}else{
			report.report("Did not Found Matching result in EnodeB for "+parameterInTest+" from Web : "+parameterFromWeb,Reporter.FAIL);
		}
	}
	
	public String getTestParameter(WebGuiParameters parameter){
		GeneralUtils.startLevel("Trying to get "+parameter.getValue()+" from Web GUI");
		String result = dut.getParameterValueViaWebAccess(parameter); 
		if(result == null){
			report.report("Error in getting Secured Parameter from Web GUI",Reporter.FAIL);
		}
		GeneralUtils.stopLevel();
		return result;
	}
	
	private boolean checkIfObjectIsInAListOfObjects(Object defaultGWFromWeb, ArrayList<?> addressesInDecimal) {
		boolean result = false;
		int i=1;
		GeneralUtils.startLevel("Checking parameters from EnodeB comparing to parameter From Web GUI");
		for(Object address : addressesInDecimal){
			report.report("parameter "+ i++ +" = "+address.toString());
			if(checkParameterValues(address,defaultGWFromWeb)){
				result = true;
			}
		}
		GeneralUtils.stopLevel();
		return result;
	}
	
	private ArrayList<String> transformHexAddressesToDecimal(ArrayList<String> addresses) {
		ArrayList<String> decimalAddressesArray = new ArrayList<String>();
		for(String address : addresses){
			String decimalAddress = turnSingleHexIpToDecimal(address);
			decimalAddressesArray.add(decimalAddress);
		}
		return decimalAddressesArray;
	}
	
	private String turnSingleHexIpToDecimal(String address) {
		StringBuilder result = new StringBuilder();
		String[] bytes = address.split(snmpSeperator);
		for(String byteSegment : bytes){
			result.append(GeneralUtils.hex2decimal(byteSegment)+".");				
		}
		int index = result.lastIndexOf(".");
		result.deleteCharAt(index);
		return result.toString();
	}

	private boolean checkParameterValues(Object paramFromWeb,Object paramFromNode){
		boolean result = false;
		if(paramFromNode.equals(paramFromWeb)){
			result = true;
		}
		return result;
	}
	
	private String getSubNetMaskFromCIDR(String subNetMaskCIDR) {
		String dumiIp = "100.100.0.0";
		GeneralUtils.printToConsole(subNetMaskCIDR);
		String dumiIpWithCIDR = dumiIp+"/"+subNetMaskCIDR;
		SubnetUtils utils = new SubnetUtils(dumiIpWithCIDR);
		String result = utils.getInfo().getNetmask();
		if(result == null){
			GeneralUtils.printToConsole("Error in parsing ip with CIDR : "+dumiIpWithCIDR );
			return null;
		}
		return result;
	}
	
	private boolean isNull(Object object){
		return object == null;
	}
	
	@Override
	public void init() throws Exception{
		GeneralUtils.startLevel("Init");
		enbInTest = new ArrayList<EnodeB>();
		enbInTest.add(dut);
		super.init();
		GeneralUtils.stopLevel();
	}
	
	@ParameterProperties(description = "Name of Enodeb Which the test will be run On")
	public void setDUT(String dut) {
		this.dut = (EnodeB) SysObjUtils.getInstnce().initSystemObject(EnodeB.class,false,dut).get(0);
	}
	
	public String getDUT() {
		return this.dut.getNetspanName();
	}
}

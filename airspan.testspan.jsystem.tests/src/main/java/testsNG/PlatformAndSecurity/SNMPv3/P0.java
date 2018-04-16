package testsNG.PlatformAndSecurity.SNMPv3;

import java.util.ArrayList;
import org.junit.Test;
import EnodeB.EnodeB;
import EnodeB.Components.EnodeBComponent;
import Netspan.NetspanServer;
import Netspan.API.Enums.EnbStates;
import Netspan.API.Enums.SnmpAgentVersion;
import UE.UE;
import Utils.GeneralUtils;
import Utils.SysObjUtils;
import jsystem.framework.ParameterProperties;
import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;
import testsNG.TestspanTest;
import testsNG.Actions.PeripheralsConfig;


public class P0 extends TestspanTest{
	public ArrayList<UE> ues=null;
	public EnodeB dut;
	private PeripheralsConfig peripheralsConfig;
	private String snmpRWPassword = EnodeBComponent.UNSECURED_WRITECOMMUNITY;
	private String snmpROPassword = EnodeBComponent.UNSECURED_READCOMMUNITY;
	private String originalUser = "";
	private String originalPass = "";
	private String originalVersion = "";
	private final String v3Password = "passwordV3";
	
	@Override
	public void init() throws Exception {
		report.startLevel("Test Init");
		enbInTest = new ArrayList<EnodeB>();
		enbInTest.add(dut);
		super.init();
		peripheralsConfig = PeripheralsConfig.getInstance();
		netspanServer = NetspanServer.getInstance();
		checkOriginalSNMP();
		report.stopLevel();
	}
	
	@Test
	@TestProperties(name = "SNMPv2DUTv2", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void snmpV2DUTv2(){
		snmpAndDutPreTest(SnmpAgentVersion.V_2_C,SnmpAgentVersion.V_2_C);
		sameSnmpAndDUTVersionTestProcess();
	}
	
	@Test
	@TestProperties(name = "SNMPv3DUTv2", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void snmpV3DUTv2(){
		snmpAndDutPreTest(SnmpAgentVersion.V_3,SnmpAgentVersion.V_2_C);
		snmpV3DUTv2TestProcess();
	}
	
	@Test
	@TestProperties(name = "SNMPv3DUTv3", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void snmpV3DUTv3() {
		snmpAndDutPreTest(SnmpAgentVersion.V_3,SnmpAgentVersion.V_3);
		sameSnmpAndDUTVersionTestProcess();
	}
	
	@Test
	@TestProperties(name = "SNMPv2DUTv3", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void snmpV2DUTV3(){
		snmpAndDutPreTest(SnmpAgentVersion.V_2_C,SnmpAgentVersion.V_3);
		snmpV2DUTV3TestProcess();
	}
	
	private void snmpAndDutPreTest(SnmpAgentVersion snmpVersion,SnmpAgentVersion dutVersion){
		GeneralUtils.startLevel("Pre test");
		if(snmpVersion == SnmpAgentVersion.V_3){
			changeSnmpVersionToV3();
		}else{
			changeSnmpVersionToV2();
		}
		if(dutVersion == SnmpAgentVersion.V_3){
			changeDUTVersionToV3();
		}else{
			changeDUTVersionToV2();
		}
		GeneralUtils.stopLevel();
	}

	private void snmpV3DUTv2TestProcess() {
		report.report("Verifying values cannot be changed via SNMPv3. Trying to change operational status");
		report.reportHtml("\"db get stackCfg [*] operationalStatus\" before trying to set OUT OF SERVICE", dut.lteCli("db get stackCfg [*] operationalStatus"), true);
		if(setOutOfServiceViaSnmp()){
			report.report("Operational status changed via SNMPv3 - not as expected", Reporter.FAIL);
		}else{
			report.report("Failed to change operational status via SNMPv3 - as expected");
		}
		report.reportHtml("\"db get stackCfg [*] operationalStatus\" after trying to set OUT OF SERVICE", dut.lteCli("db get stackCfg [*] operationalStatus"), true);
	}

	private void sameSnmpAndDUTVersionTestProcess(){
		report.report("Verifying values can be changed in eNodeB via SNMP. Trying to set operational status out of service");
		report.reportHtml("\"db get stackCfg [*] operationalStatus\" before trying to set OUT OF SERVICE", dut.lteCli("db get stackCfg [*] operationalStatus"), true);
		setOutOfServiceViaSnmp();			
		if(dut.getOperationalStatus().equals("2")){
			report.report("Enodeb was set OUT OF SERVICE successfully");
		}else{				
			report.report("Failed to set eNodeB OUT OF SERVICE", Reporter.FAIL);
		}
		report.reportHtml("\"db get stackCfg [*] operationalStatus\" after trying to set OUT OF SERVICE", dut.lteCli("db get stackCfg [*] operationalStatus"), true);

	}

	private void snmpV2DUTV3TestProcess() {
		report.report("Verifying values cannot be changed via SNMPv2. Trying to change operational status");
		report.reportHtml("\"db get stackCfg [*] operationalStatus\" before trying to set OUT OF SERVICE", dut.lteCli("db get stackCfg [*] operationalStatus"), true);
		if(setOutOfServiceViaSnmp()){
			report.report("Operational status changed via SNMPv2 - not as expected", Reporter.FAIL);
		}else{
			report.report("Couldn't change operational status via SNMPv2 - as expected");
		}
		report.reportHtml("\"db get stackCfg [*] operationalStatus\" after trying to set OUT OF SERVICE", dut.lteCli("db get stackCfg [*] operationalStatus"), true);
	}

	private void changeSnmpVersionToV2(){
		report.report("Changing SNMP driver's version to V2");
		dut.snmpSet(snmpROPassword,snmpRWPassword,dut.getIpAddress(),"V2");
	}
	
	private void changeSnmpVersionToV3(){
		report.report("Changing SNMP driver's version to V3");
		dut.snmpSet("readwrite",v3Password,dut.getIpAddress(),"V3");
	}
	
	private void changeDUTVersionToV2(){
		report.report("Changing SNMP version in eNodeB to V2");
		changeSnmpVersionInEnodeb("v2C",snmpRWPassword,snmpROPassword);
	}
	
	private void changeDUTVersionToV3(){
		report.report("Changing SNMP version in eNodeB to V3");
		changeSnmpVersionInEnodeb("V3",v3Password,v3Password);
	}
	
	private boolean setOutOfServiceViaSnmp(){
		return dut.setOperationalStatus(EnbStates.OUT_OF_SERVICE);
	}

	private void checkOriginalSNMP(){
		originalVersion = dut.getSnmpVersion();
		if(originalVersion.equals("V3")){
			originalUser = dut.getReadCommunityOrUserName();
			originalPass = dut.getWriteCommunityOrPassword();
		}
	}

	private void changeSnmpVersionInEnodeb(String ver, String rw, String ro){
		if(ver.equals("v2C")){
			dut.shell("/bs/lteCli -c \"db set SnmpCfg [1] snmpVerConf=0 snmpRWPassword="+rw+" snmpROPassword="+ro+" snmpPortNumber=161\" > /tmp/web_snmpVer.result 2>/tmp/web_snmpVer.error");
		}else{
			dut.shell("/bs/lteCli -c \"db set SnmpCfg [1] snmpVerConf=2 snmpRWPassword="+rw+" snmpROPassword="+ro+" snmpPortNumber=161\" > /tmp/web_snmpVer.result 2>/tmp/web_snmpVer.error");
		}
		report.report("Waiting 30 seconds after sending command to enodeB");
		GeneralUtils.unSafeSleep(30*1000);
		report.reportHtml("db get SnmpCfg", dut.lteCli("db get SnmpCfg"), true);
	}

	@ParameterProperties(description = "Name of Enodeb Which the test will be run On")
	public void setDUT(String dut) {
		this.dut = (EnodeB) SysObjUtils.getInstnce().initSystemObject(EnodeB.class,false,dut).get(0);
	}

	public String getDUT() {
		return this.dut.getNetspanName();
	}
	
	@Override
	public void end(){
		GeneralUtils.startLevel("After test");
		if(originalVersion.equals("V3")){
			report.report("Returning to original version driver of SNMP");
			dut.snmpSet(originalUser,originalPass,dut.getIpAddress(),"V3");
			report.report("Returning to original snmp version of eNodeB");
			changeSnmpVersionInEnodeb("V3",originalPass,originalPass);
		}else{
			report.report("Returning to original version driver of SNMP");
			dut.snmpSet(snmpROPassword,snmpRWPassword,dut.getIpAddress(),"V2");
			report.report("Returning to original snmp version of eNodeB");
			changeSnmpVersionInEnodeb("v2C",snmpRWPassword,snmpROPassword);
		}
		peripheralsConfig.changeEnbState(dut, EnbStates.IN_SERVICE);
		GeneralUtils.stopLevel();
		super.end();
	}
}
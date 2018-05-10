package testsNG.UnitTest.AutomationTests;

import java.util.ArrayList;

import org.junit.Test;

import EnodeB.EnodeB;
import EnodeB.Components.EnodeBComponent;
import Utils.GeneralUtils;
import Utils.SysObjUtils;
import jsystem.framework.ParameterProperties;
import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;
import testsNG.TestspanTest;

public class AutomationTests extends TestspanTest{

	private EnodeB dut;	

	@Override
	public void init() throws Exception {
		enbInTest = new ArrayList<EnodeB>();
		enbInTest.add(dut);
		super.init();
	}
	
	@Test
	@TestProperties(name = "reboot", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {"IsTestWasSuccessful" })
	public void reboot() throws Exception {
		report.report("Start reboot test.");
		
		dut.reboot();
		GeneralUtils.unSafeSleep(60 * 1000);
		dut.waitForAllRunningAndInService(EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
		
		report.report("Finished reboot test.");
		
	}
	
	@Test
	@TestProperties(name = "switchBank", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {"IsTestWasSuccessful" })
	public void switchBank() throws Exception {
		report.report("Start reboot test.");
		
		dut.reboot(true);
		GeneralUtils.unSafeSleep(60 * 1000);
		dut.waitForAllRunningAndInService(EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
		
		report.report("Finished reboot test.");
		
	}
	
	@Test
	@TestProperties(name = "noTest", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {"IsTestWasSuccessful" })
	public void noTest() throws Exception {
		report.report("Start no test.");
		
		report.report("Finished no test.");
		
	}
	
	@Test
	@TestProperties(name = "rebootSerial", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {"IsTestWasSuccessful" })
	public void rebootSerial() throws Exception {
		report.report("Start rebootSerial test.");
		
		dut.setExpectBooting(true);
		String response = dut.getSerialSession().sendCommands(EnodeBComponent.SHELL_PROMPT, "reboot", "system");
		GeneralUtils.printToConsole("response for serial reboot command: " + response);
		GeneralUtils.unSafeSleep(10000);
		
		
		if(	(response.contains("The system is going down NOW!")) ||	(response.contains("Restarting system"))){
			report.report("EnodeB has been rebooted via Serial.");
		}else{
			report.report("Fail to reboot via Serial.", Reporter.WARNING);
		}
		
		dut.waitForAllRunningAndInService(EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
		
		report.report("Finished rebootSerial test.");
		
	}
	
	@Test
	@TestProperties(name = "unexpectedReboot", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {"IsTestWasSuccessful" })
	public void unexpectedReboot() throws Exception {
		report.report("Start unexpectedReboot test.");
		
		dut.shell("reboot");
		GeneralUtils.unSafeSleep(60 * 1000);
		dut.waitForAllRunningAndInService(EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
		
		report.report("Finished unexpectedReboot test.");
		
	}
	
	@ParameterProperties(description = "Name of Enb")
	public void setDUT(String dut) {
		ArrayList<EnodeB> temp=(ArrayList<EnodeB>)SysObjUtils.getInstnce().initSystemObject(EnodeB.class,false,dut);
		this.dut = temp.get(0);
	}
}


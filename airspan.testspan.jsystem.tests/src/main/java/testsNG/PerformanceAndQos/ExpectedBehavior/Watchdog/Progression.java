package testsNG.PerformanceAndQos.ExpectedBehavior.Watchdog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import EnodeB.EnodeB;
import Netspan.API.Enums.EnbStates;
import Utils.GeneralUtils;
import Utils.SysObjUtils;
import jsystem.framework.ParameterProperties;
import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;
import testsNG.TestspanTest;
import testsNG.Actions.Utils.ParallelCommandsThread;

public class Progression extends TestspanTest{
	private EnodeB dut;
	private ParallelCommandsThread syncCommands;
	
	@Override
	public void init() throws Exception {
		enbInTest = new ArrayList<EnodeB>();
		enbInTest.add(dut);
		super.init();
		startParallelCommands();
	}

	@Override
	public void end(){
		stopParallelCommands();
		super.end();
	}
	
	@ParameterProperties(description = "DUT")
	public void setDut(String dut) {
		GeneralUtils.printToConsole("Load DUT " + dut);
		this.dut = (EnodeB) SysObjUtils.getInstnce().initSystemObject(EnodeB.class, false, dut).get(0);
		GeneralUtils.printToConsole("DUT loaded" + this.dut.getName());
	}
	/***************** Tests ********************/
	
	@Test
	@TestProperties(name = "Watchdog test - in case SW is stuck, watchdog need to performs reboot",
	returnParam = { "IsTestWasSuccessful" },
	paramsExclude = {"IsTestWasSuccessful" })
	public void watchdogTest() {
		int serviceState = GeneralUtils.serviceStateToInt(dut.getServiceState());
		
		EnbStates running = dut.getRunningState();
		report.report(dut.getName() + ": CELLS OPERATIONAL STATUS");
		report.report("========================");
		report.report("****CELL 0 SERVICE STATE = " + serviceState + " ****");
		report.report("CELLS ALL RUNNING STATUS");
		report.report("========================");
		report.report("****CELL 0 ALL RUNNING STATE = " + running + " ****");
		
		if(!dut.isAvailable()){
			report.report(dut.getName() + " is not available via SNMP, cant continue the test.", Reporter.FAIL);
			reason = "DUT is not available via SNMP, cant continue the test.";
			return;
		}
		
		dut.setExpectBooting(true);
		report.report("Excute Shell command: pkill prcMngr");
		dut.shell("pkill prcMngr");
		
		GeneralUtils.startLevel("Waiting for reboot in the next 5 minutes.");
		boolean breakOccurred = false;
		for(int i = 1; i <= 5 * 30; i++){
			if(!dut.isAvailable()){
				GeneralUtils.stopLevel();
				report.report("eNodeB preformed reboot!");
				dut.waitForSnmpAvailable(EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
				breakOccurred = true;
				break;
			}
			report.report("Waiting.");
			GeneralUtils.unSafeSleep(2000);
		}
		if(!breakOccurred){
			GeneralUtils.stopLevel();
			report.report("eNodeB did not preform reboot!", Reporter.FAIL);
			reason = "DUT did not preform reboot!";
		}
		
		report.report("Wait For All Running And In Service");
		dut.waitForAllRunningAndInService(EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
	}
	
	/***************** Parallel Commands ********************/
	
	private void startParallelCommands(){
		report.report("Starting parallel commands");
		List<String> commandList = new ArrayList<String>();
		commandList.add("cell show operationalStatus");
		commandList.add("system show compilationtime");
		commandList.add("db get ProcessInformation");
		commandList.add("db get PTPStatus");
		
		try {
			syncCommands = new ParallelCommandsThread(commandList, dut, null, null);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		syncCommands.start();
	}
	
	private void stopParallelCommands() {
		report.report("Stopping Parallel Commands");
		try {
			syncCommands.stopCommands();
			syncCommands.moveFileToReporterAndAddLink();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
}

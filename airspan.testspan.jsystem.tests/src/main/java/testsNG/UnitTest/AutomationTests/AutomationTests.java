package testsNG.UnitTest.AutomationTests;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Test;

import EnodeB.EnodeB;
import EnodeB.Components.EnodeBComponent;
import UE.UE;
import UeSimulator.Amarisoft.AmariSoftServer;
import Utils.GeneralUtils;
import Utils.SetupUtils;
import Utils.SysObjUtils;
import jsystem.framework.ParameterProperties;
import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;
import testsNG.TestspanTest;
import testsNG.Actions.Utils.ParallelCommandsThread;

public class AutomationTests extends TestspanTest{

	private ArrayList<EnodeB> duts;
	private EnodeB dut;
	private ParallelCommandsThread commandsThread1;	

	@Override
	public void init() throws Exception {
		report.report("Init!");
		enbInTest = new ArrayList<EnodeB>();
		if (dut != null) {
			enbInTest.add(dut);
		}
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
	@TestProperties(name = "amarisoftUeTest", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {"IsTestWasSuccessful", "DUT" })
	public void amarisoftUeTest() throws Exception {
		report.report("Start amarisoftUeTest test.");
		
		ArrayList<UE> ues =  SetupUtils.getInstance().getAllUEs();
		GeneralUtils.startLevel("All UEs information");
		for (UE ue : ues) {
			try {
				GeneralUtils.startLevel(ue.getName());
				ue.start();
				report.report("ue.getImsi: " + ue.getImsi());
				report.report("ue.getLanIpAddress: " + ue.getLanIpAddress());
				report.report("ue.getWanIpAddress: " + ue.getWanIpAddress());
				report.report("ue.getIPerfDlMachine: " + ue.getIPerfDlMachine());
				report.report("ue.getIPerfUlMachine: " + ue.getIPerfUlMachine());
				report.report("ue.getVendor: " + ue.getVendor());
				report.report("ue.getUeCategory: " + ue.getUeCategory());
				report.report("ue.getVersion: " + ue.getVersion());
				report.report("ue.getBandWidth: " + ue.getBandWidth());
				report.report("ue.getUEStatus: " + ue.getUEStatus());
				report.report("ue.getDuplexMode: " + ue.getDuplexMode());
				report.report("ue.getRSRP1: " + ue.getRSRP(1));
				report.report("ue.getPCI: " + ue.getPCI());
				ue.stop();
				GeneralUtils.stopLevel();
			} catch (Exception e) {
				GeneralUtils.stopLevel();
				e.printStackTrace();
			}
		}
		GeneralUtils.stopLevel();
		report.report("Finished amarisoftUeTest test.");
		
	}
	
	
	@Test
	@TestProperties(name = "logTest", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {"IsTestWasSuccessful" })
	public void logTest() throws Exception {
		report.report("Start logTest test.");
		
		
		commands();
		GeneralUtils.unSafeSleep(2*30*60*60*1000);
		stopCommandsAndAttachFiles();
		
		report.report("Finished logTest test.");
		
	}
	
	public void commands() {

		ArrayList<String> commands = new ArrayList<>();
		commands.add("system show memory");
		commands.add("system show memdbg bucket=0 leaks=10");
		commands.add("system show memdbg bucket=1 leaks=10");
		commands.add("system show memdbg bucket=2 leaks=10");
		commands.add("system show memdbg bucket=3 leaks=10");
		commands.add("system show memdbg bucket=4 leaks=10");
		commands.add("system show memdbg bucket=5 leaks=10");
		commands.add("system show memdbg bucket=6 leaks=10");
		commands.add("system show memdbg bucket=7 leaks=10");
		commands.add("system show memdbg bucket=8 leaks=10");

		
		try {
			commandsThread1 = new ParallelCommandsThread(commands, dut, null, null);
		} catch (IOException e) {
			report.report("could not init Commands in parallel", Reporter.WARNING);
		}
		commandsThread1.start();

	}

	private void stopCommandsAndAttachFiles(){
		commandsThread1.stopCommands();
		commandsThread1.moveFileToReporterAndAddLink();
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

	@Test
	@TestProperties(name = "amarisoft", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {"IsTestWasSuccessful" })
	public void amarisoft() throws Exception {
		report.report("Start amarisoft test.");

		AmariSoftServer as = AmariSoftServer.getInstance();
		GeneralUtils.printToConsole("getImsiStartList: " + as.getImsiStartList());
		GeneralUtils.printToConsole("getImsiStopList: " + as.getImsiStopList());
		GeneralUtils.printToConsole("getPassword: " + as.getPassword());
		GeneralUtils.printToConsole("getRxgain: " + as.getRxgain());
		GeneralUtils.printToConsole("getTxgain: " + as.getTxgain());
		GeneralUtils.printToConsole("getIp: " + as.getIp());
		GeneralUtils.printToConsole("getusername: " + as.getusername());

		as.startServer(duts);
		report.report("Finished amarisoft test.");
	}

}
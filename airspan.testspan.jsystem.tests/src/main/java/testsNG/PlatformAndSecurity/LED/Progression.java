package testsNG.PlatformAndSecurity.LED;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import EnodeB.EnodeB;
import EnodeB.Ninja;
import Netspan.API.Enums.EnbStates;
import UE.UE;
import Utils.GeneralUtils;
import Utils.SetupUtils;
import Utils.SysObjUtils;
import jsystem.framework.ParameterProperties;
import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;
import testsNG.TestspanTest;
import testsNG.Actions.PeripheralsConfig;
import testsNG.Actions.Traffic;
import testsNG.Actions.Utils.ParallelCommandsThread;

public class Progression extends TestspanTest{
	enum LED_STATUS_PATTERNS{
		AS_LED_PATT_8_OFF(0),
		AS_LED_PATT_8_ON(16777215),
		AS_LED_PATT_8_BLINK(11184810),
		AS_LED_PATT_8_BLINK_G1_R1_G(8947848),
		AS_LED_PATT_8_BLINK_G1_R1_R(2236962),
		AS_LED_PATT_8_BLINK_G1_R2_G(8521760),
		AS_LED_PATT_8_BLINK_G1_R2_R(2663050),
		AS_LED_PATT_8_BLINK_G1_R3_G(8421504),
		AS_LED_PATT_8_BLINK_G1_R3_R(2763306),
		OAM_LED_PATTERN_2_SEC_ON_2_SEC_OFF(13421772),
		OAM_LED_PATTERN_4_SEC_ON_4_SEC_OFF(15790320);
		
		int value;
		LED_STATUS_PATTERNS(int v){
			value = v;
		}
		
		public static LED_STATUS_PATTERNS get(int v){
			for(LED_STATUS_PATTERNS x : LED_STATUS_PATTERNS.values()){
				if(x.value == v){
					return x;
				}
			}
			return null;
		}
	}
	final String[] ledStatusNames = {
			"ledDrvPowerLedPatt",
			"ledDrvBhGreenLedPatt",
			"ledDrvBhRedLedPatt",
			"ledDrvWanGreenLedPatt",
			"ledDrvWanRedLedPatt",
			"ledDrvLteGreenLedPatt",
			"ledDrvLteRedLedPatt",
			"ledDrvFirstUeSignalPatt",
			"ledDrvSecondUeSignalPatt",
			"ledDrvThirdUeSignalPatt",
			"ledDrvFourthUeSignalPatt"
	};
	
	private EnodeB dut;
	ParallelCommandsThread syncCommands;
	private List<String> commandList;

	@Override
	public void init() throws Exception {
		enbInTest = new ArrayList<EnodeB>();
		enbInTest.add(dut);
		super.init();
		stopUEs(dut);
		dut.lteCli("logger threshold set client=OAM_LED std_out=0");
		syncGeneralCommands();
	}

	@Override
	public void end(){
		stopSyncGeneralCommands();
		dut.lteCli("logger threshold set client=OAM_LED std_out=4");
		startUEs(dut);
		super.end();
	}
	
	@ParameterProperties(description = "DUT")
	public void setDUT(String dut) {
		GeneralUtils.printToConsole("Load DUT " + dut);
		this.dut = (EnodeB) SysObjUtils.getInstnce().initSystemObject(EnodeB.class, false, dut).get(0);
		GeneralUtils.printToConsole("DUT loaded" + this.dut.getName());
	}
	
	/********************************* TESTS *********************************/
	
	/**
	 * Test LEDs Behavior on booting
	 */
	@Test
	@TestProperties(name = "Test LEDs behavior on booting.",
	returnParam = { "IsTestWasSuccessful" }, 
	paramsExclude = {"IsTestWasSuccessful"})
	public void ledsOnBooting(){
		if(!(dut instanceof Ninja)){
			report.report("The test for Ninja hardware only, while DUT's hardware type is " + netspanServer.getNodeDetails(dut).hardwareType.replace(" ", ""));
			return;
		}
		
		report.report("Set DUT OutOfService for avoiding ALL RUNNING after Reboot.");
		PeripheralsConfig.getInstance().changeEnbState(dut, EnbStates.OUT_OF_SERVICE);
		
		report.report("Performing Reboot."); 
		dut.reboot();
		GeneralUtils.unSafeSleep(5 * 1000);
		report.report("Wait for SNMP availability.");
		dut.waitForSnmpAvailable(5 * 60 * 1000);
		GeneralUtils.unSafeSleep(30 * 1000);
		boolean snmpAvailable = dut.waitForSnmpAvailable(5 * 60 * 1000);
		if(snmpAvailable){
			dut.lteCli("logger threshold set client=OAM_LED std_out=0");
			
			LED_STATUS_PATTERNS[] ledStatusExpectedValues = {
					LED_STATUS_PATTERNS.AS_LED_PATT_8_ON,
					LED_STATUS_PATTERNS.AS_LED_PATT_8_ON,
					LED_STATUS_PATTERNS.AS_LED_PATT_8_OFF,
					LED_STATUS_PATTERNS.AS_LED_PATT_8_BLINK,
					LED_STATUS_PATTERNS.AS_LED_PATT_8_OFF,
					LED_STATUS_PATTERNS.AS_LED_PATT_8_OFF,
					LED_STATUS_PATTERNS.AS_LED_PATT_8_OFF
			};
			checkLedStatus(ledStatusExpectedValues);
			
			report.report("Set DUT InService for getting ALL RUNNING.");
			PeripheralsConfig.getInstance().changeEnbState(dut, EnbStates.IN_SERVICE);
			report.report("Wait for ALL RUNNING.");
			dut.waitForAllRunningAndInService(EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
			
			ledStatusExpectedValues[3] = LED_STATUS_PATTERNS.AS_LED_PATT_8_ON;
			ledStatusExpectedValues[5] = LED_STATUS_PATTERNS.AS_LED_PATT_8_ON;
			checkLedStatus(ledStatusExpectedValues);
		}else{
			report.report("FAILED to reach snmp availabilty.", Reporter.FAIL);
		}
	}
	
	/**
	 * Test LEDs Behavior with and without traffic
	 */
	@Test
	@TestProperties(name = "Test LEDs behavior with and without traffic.",
	returnParam = { "IsTestWasSuccessful" }, 
	paramsExclude = {"IsTestWasSuccessful"})
	public void ledsDuringTrafficAndWithout(){
		if(!(dut instanceof Ninja)){
			report.report("The test for Ninja hardware only, while DUT's hardware type is " + netspanServer.getNodeDetails(dut).hardwareType.replace(" ", ""));
			return;
		}
		
		Traffic traffic = null;
		try {
			traffic = Traffic.getInstance(SetupUtils.getInstance().getAllUEs());
			traffic.init();
		} catch (Exception e) {
			report.report("Failed to initialize traffic generator.");
			e.printStackTrace();
			return;
		}
		String TrafficFile = traffic.getConfigFile().getPath();
		int lastDot = TrafficFile.lastIndexOf('.');
		String HOTrafficFile = TrafficFile.substring(0, lastDot) + "_HO" + TrafficFile.substring(lastDot);
		traffic.configFile = new File(HOTrafficFile);
		boolean trafficOn = false;
		
		LED_STATUS_PATTERNS[] ledStatusExpectedValues = {
				LED_STATUS_PATTERNS.AS_LED_PATT_8_ON,
				LED_STATUS_PATTERNS.AS_LED_PATT_8_ON,
				LED_STATUS_PATTERNS.AS_LED_PATT_8_OFF,
				LED_STATUS_PATTERNS.AS_LED_PATT_8_ON,
				LED_STATUS_PATTERNS.AS_LED_PATT_8_OFF,
				LED_STATUS_PATTERNS.AS_LED_PATT_8_ON,
				LED_STATUS_PATTERNS.AS_LED_PATT_8_OFF
		};
		
		checkLedStatus(ledStatusExpectedValues);
		
		startUEs(dut);
		if(safeStartTraffic(traffic)){
			GeneralUtils.unSafeSleep(10 * 1000);
			trafficOn = true;
			ledStatusExpectedValues[5] = LED_STATUS_PATTERNS.AS_LED_PATT_8_BLINK;
			checkLedStatus(ledStatusExpectedValues);
		}else{
			trafficOn = false;
			report.report("Failed to start traffic", Reporter.WARNING);
		}
		
		if(trafficOn == false || safeStopTraffic(traffic)){
			stopUEs(dut);
			ledStatusExpectedValues[5] = LED_STATUS_PATTERNS.AS_LED_PATT_8_ON;
			report.report("Wait for 10 seconds to ensure traffic stopped completely.");
			GeneralUtils.unSafeSleep(10 * 1000);
			checkLedStatus(ledStatusExpectedValues);
		}
	}

	/********************* Helper functions ****************/
	
	void checkLedStatus(LED_STATUS_PATTERNS[] ledStatusExpectedValues){
		GeneralUtils.startLevel("Check LEDs Status");
		GeneralUtils.reportHtmlLink("led show state", dut.lteCli("led show state"));
		boolean testFailed = false;
		int[] ledStatusCurrentValues = dut.getLedStatusValues();
		for(int i = 0; i < ledStatusExpectedValues.length; i++){
			if(ledStatusCurrentValues[i] == ledStatusExpectedValues[i].value){
				report.report(ledStatusNames[i] + " equals " + LED_STATUS_PATTERNS.get(ledStatusCurrentValues[i]));
			}else{
				report.report(ledStatusNames[i] + " equals " + LED_STATUS_PATTERNS.get(ledStatusCurrentValues[i]) + ", while status expected: " + ledStatusExpectedValues[i], Reporter.FAIL);
				testFailed = true;
			}
		}
		if(testFailed){
			reason = "LED Status Incorrect according the STAGE.";
		}
		GeneralUtils.stopLevel();
	}
	
	private boolean safeStartTraffic(Traffic traffic){
		try {
			report.report("Start Traffic.");
			return traffic.startTraffic();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private boolean safeStopTraffic(Traffic traffic){
		try {
			report.report("Stop Traffic.");
			traffic.stopTraffic();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private void startUEs(EnodeB eNodeB) {
		ArrayList<UE> ues = SetupUtils.getInstance().getallUEsPerEnodeb(eNodeB);
		GeneralUtils.startLevel("Starting UEs.");
		for(UE ue : ues){
			report.report("Start UE " + ue.getName() + " succeeded: " + ue.start());
		}
		GeneralUtils.stopLevel();
	}

	private void stopUEs(EnodeB eNodeB) {
		ArrayList<UE> ues = SetupUtils.getInstance().getallUEsPerEnodeb(eNodeB);
		GeneralUtils.startLevel("Stopping UEs for avoiding traffic.");
		for(UE ue : ues){
			report.report("Stop UE " + ue.getName() + " succeeded: " + ue.stop());
		}
		GeneralUtils.stopLevel();
	}
	
	/**********************Parallel commands****************/
	
	private void syncGeneralCommands() {
		report.report("Starting parallel commands");
		commandList = new ArrayList<String>();
		commandList.add("led show state");
		commandList.add("db get ipsecstatus");
		commandList.add("ue show rate");
		commandList.add("db get PTPStatus");
		
		try {
			syncCommands = new ParallelCommandsThread(commandList, dut, null, null);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		syncCommands.start();
		GeneralUtils.unSafeSleep(20*1000);
	}
	
	private void stopSyncGeneralCommands(){
		report.report("Stopping parallel commands");
		syncCommands.stopCommands();
		syncCommands.moveFileToReporterAndAddLink();
	}
}

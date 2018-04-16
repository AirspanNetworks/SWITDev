package testsNG.PlatformAndSecurity.SystemMonitoring;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import EnodeB.EnodeB;
import EnodeB.Ninja;
import Netspan.API.Lte.AlarmInfo;
import Utils.GeneralUtils;
import Utils.SetupUtils;
import Utils.SysObjUtils;
import jsystem.framework.ParameterProperties;
import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;
import testsNG.TestspanTest;
import testsNG.Actions.Traffic;
import testsNG.Actions.Utils.ParallelCommandsThread;

public class Progression extends  TestspanTest{
	private final long ONE_MINUTE = 1 * 60 * 1000;
	private final long FIVE_MINUTE = 5 * 60 * 1000;
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
	public void end() {
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
	@TestProperties(name = "UE Over Heat UE_RFIC_SENSOR_1",
	returnParam = { "IsTestWasSuccessful"},
	paramsExclude = {"IsTestWasSuccessful"})
	public void ueOverHeatUE_RFIC_SENSOR_1() {
		testOverHeatTemperature(14);
	}
	
	@Test
	@TestProperties(name = "UE Over Cold UE_RFIC_SENSOR_2",
	returnParam = { "IsTestWasSuccessful"},
	paramsExclude = {"IsTestWasSuccessful"})
	public void ueOverColdUE_RFIC_SENSOR_2() {
		testOverColdTemperature(15);
	}
	
	@Test
	@TestProperties(name = "UE Over Heat UE_RFIC_SENSOR_3",
	returnParam = { "IsTestWasSuccessful"},
	paramsExclude = {"IsTestWasSuccessful"})
	public void ueOverHeatUE_RFIC_SENSOR_3() {
		testOverHeatTemperature(16);
	}
	
	@Test
	@TestProperties(name = "UE Over Heat UE_RFIC_SENSOR_4",
	returnParam = { "IsTestWasSuccessful"},
	paramsExclude = {"IsTestWasSuccessful"})
	public void ueOverHeatUE_RFIC_SENSOR_4() {
		testOverHeatTemperature(17);
	}
	
	@Test
	@TestProperties(name = "UE Over Cold UE_RFIC_SENSOR_5",
	returnParam = { "IsTestWasSuccessful"},
	paramsExclude = {"IsTestWasSuccessful"})
	public void ueOverColdUE_RFIC_SENSOR_5() {
		testOverColdTemperature(18);
	}

	/***************** Helper Functions ********************/
	
	void testOverHeatTemperature(int entry){
		if(!(dut instanceof Ninja)){
			report.report("The test for Ninja hardware only, while DUT's hardware type is " + netspanServer.getNodeDetails(dut).hardwareType.replace(" ", ""));
			return;
		}
		Traffic traffic = null;
		try {
			traffic = Traffic.getInstance(SetupUtils.getInstance().getAllUEs());
			traffic.init();
			String TrafficFile = traffic.getConfigFile().getPath();
			int lastDot = TrafficFile.lastIndexOf('.');
			String HOTrafficFile = TrafficFile.substring(0, lastDot) + "_HO" + TrafficFile.substring(lastDot);
			traffic.configFile = new File(HOTrafficFile);
		} catch (Exception e) {
			report.report("FAILED initialize Traffic Generator.", Reporter.FAIL);
			e.printStackTrace();
		}
		
		isPass = false;
		final int criticalMaxOriginal = dut.getSmonThresholdsCriticalMax(entry);
		dut.setExpectBooting(true);
		int currentTemperature = dut.getTemperatureSensorsSensorReading(entry);
		report.report("Current Temperature = "+currentTemperature);
		report.report("Set critical_max smonThreshold to "+(currentTemperature));
		dut.setSmonThresholdsCriticalMax(entry, currentTemperature);
		safeStartTraffic(traffic);
		if(dut.waitUntilNotAvailable(FIVE_MINUTE)){
			report.report("eNodeB preformed reboot!");
		}else{
			report.report("eNodeB did not preform reboot!", Reporter.FAIL);
		}
		safeStopTraffic(traffic);
		report.report("Checking Node over-heating Alarm sent to NMS.");
		List<AlarmInfo> alarmsInfo = alarmsAndEvents.getAllAlarmsNode(dut);
		for(AlarmInfo alarmInfo : alarmsInfo){
			if(alarmInfo.alarmInfo.contains("Node over-heating")){
				report.step("Node over-heating Alarm was uploaded to NMS.");
				alarmsAndEvents.printAlarmInfo(alarmInfo);
				isPass = true;
				break;
			}
		}
		if(!isPass){
			report.report("Node over-heating Alarm was not uploaded to NMS.", Reporter.FAIL);
		}
		
		report.report("Waiting for Snmp Availability.");
		dut.waitForSnmpAvailable(dut.WAIT_FOR_ALL_RUNNING_TIME);
		if(criticalMaxOriginal != GeneralUtils.ERROR_VALUE){
			report.report("Set critical_max smonThreshold to Original value "+(criticalMaxOriginal));
			dut.setSmonThresholdsCriticalMax(entry, criticalMaxOriginal);
		}
		
		report.report("Waiting for ALL RUNNING and InService.");
		dut.waitForAllRunningAndInService(dut.WAIT_FOR_ALL_RUNNING_TIME);
	}

	void testOverColdTemperature(int entry){
		if(!(dut instanceof Ninja)){
			report.report("The test for Ninja hardware only, while DUT's hardware type is " + netspanServer.getNodeDetails(dut).hardwareType.replace(" ", ""));
			return;
		}
		isPass = false;
		final int criticalMinOriginal = dut.getSmonThresholdsCriticalMin(entry);
		int currentTemperature = dut.getTemperatureSensorsSensorReading(entry);
		report.report("Current Temperature = "+currentTemperature);
		report.report("Set critical_min smonThreshold to "+(currentTemperature+20));
		dut.setSmonThresholdsCriticalMin(entry, currentTemperature+20);
		GeneralUtils.unSafeSleep(ONE_MINUTE);
		report.report("Checking Node over-cold Alarm sent to NMS.");
		List<AlarmInfo> alarmsInfo = alarmsAndEvents.getAllAlarmsNode(dut);
		for(AlarmInfo alarmInfo : alarmsInfo){
			if(alarmInfo.alarmInfo.contains("cold")){//.equals("Node over-cold")){
				report.step("Node over-cold Alarm was uploaded to NMS.");
				alarmsAndEvents.printAlarmInfo(alarmInfo);
				isPass = true;
				break;
			}
		}
		if(!isPass){
			report.report("Node over-cold Alarm was not uploaded to NMS.", Reporter.FAIL);
		}
		
		if(criticalMinOriginal != GeneralUtils.ERROR_VALUE){
			report.report("Set critical_min smonThreshold to Original value "+(criticalMinOriginal));
			dut.setSmonThresholdsCriticalMin(entry, criticalMinOriginal);
		}
	}
	
	private void safeStartTraffic(Traffic traffic) {
		if(traffic != null){
			try {
				report.report("Start Traffic.");
				traffic.startTraffic();
			} catch (Exception e) {
				GeneralUtils.printToConsole("Failed to start traffic");
				e.printStackTrace();
			}
		}
		
	}

	private void safeStopTraffic(Traffic traffic) {
		if(traffic != null){
			report.report("Stop Traffic.");
			traffic.stopTraffic();
		}
	}
	
	/***************** Nested Classes ********************/
	
	/***************** Parallel Commands ********************/
	
	private void startParallelCommands(){
		report.report("Starting parallel commands");
		List<String> commandList = new ArrayList<String>();
		commandList.add("cell show operationalStatus");
		commandList.add("system show compilationtime");
		commandList.add("db get temperatureSensors");
		commandList.add("db get smonThresholds");
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
package testsNG.PerformanceAndQos.LowResourcesEventsAndAlarms;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import Utils.*;
import org.junit.Test;

import Attenuators.AttenuatorSet;
import EnodeB.EnodeB;
import Netspan.API.Enums.EnbStates;
import Netspan.API.Lte.AlarmInfo;
import UE.UE;
import jsystem.framework.ParameterProperties;
import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;
import testsNG.TestspanTest;
import testsNG.Actions.EnodeBConfig;
import testsNG.Actions.PeripheralsConfig;
import testsNG.Actions.Traffic;
import testsNG.Actions.Utils.ParallelCommandsThread;
import testsNG.Actions.Utils.TrafficGeneratorType;

public class Progression extends TestspanTest{
	private EnodeB dut;
	private PeripheralsConfig peripheralsConfig;
	private EnodeBConfig enodeBConfig;
	private Traffic traffic;
	private List<String> commandList;
	private ParallelCommandsThread syncCommands;

	@Override
	public void init() throws Exception {
		GeneralUtils.startLevel("Test Init");
		
		enbInTest = new ArrayList<EnodeB>();
		enbInTest.add(dut);
		super.init();
		commandList = new ArrayList<String>();
		peripheralsConfig = PeripheralsConfig.getInstance();
		enodeBConfig = EnodeBConfig.getInstance();
		ArrayList<EnodeB> dnut = enbInSetup;
		dnut.remove(dut);
		for(EnodeB enb : dnut){
			peripheralsConfig.changeEnbState(enb, EnbStates.OUT_OF_SERVICE);
		}
		
		AttenuatorSet attenuatorSetUnderTest = AttenuatorSet.getAttenuatorSet(CommonConstants.ATTENUATOR_SET_NAME);
		peripheralsConfig.setAttenuatorSetValue(attenuatorSetUnderTest, 0);
		
		ArrayList<UE> ues = getDutUEs();
		peripheralsConfig.rebootUEs(ues, 120*1000);
		
		traffic = Traffic.getInstance(SetupUtils.getInstance().getAllUEs());
		traffic.init();
		String TrafficFile = traffic.getConfigFile().getPath();
		int lastDot = TrafficFile.lastIndexOf('.');
		String HOTrafficFile = TrafficFile.substring(0, lastDot) + "_HO" + TrafficFile.substring(lastDot);
		traffic.configFile = new File(HOTrafficFile);
		traffic.trafficLoadSet(new Pair<Double, Double>(150.0, 50.0));
		traffic.initFrameSize(64, null);
		
		GeneralUtils.stopLevel();
	}

	@Override
	public void end(){
		if (traffic.getGeneratorType()==TrafficGeneratorType.TestCenter){
			String TrafficFile = traffic.getTg().getDefaultConfigTccFile();
			traffic.configFile = new File(TrafficFile);
		}
		super.end();
	}
	
	@ParameterProperties(description = "DUT")
	public void setDut(String dut) {
		GeneralUtils.printToConsole("Load DUT " + dut);
		this.dut = (EnodeB) SysObjUtils.getInstnce().initSystemObject(EnodeB.class, false, dut).get(0);
		GeneralUtils.printToConsole("DUT loaded" + this.dut.getName());
	}
	
	@Test
	@TestProperties(name = "Check uploading Low Resources (CPU, RRC and Memory) traps to NMS", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void isLowResourcesTrapsUploadedToNMS() {
		/*Open Logs of KPI counters for SW debug*/
		dut.lteCli("kpi counters setdebugprint layerId=5 counterId=185 debugPrint=1");
		dut.lteCli("kpi counters setdebugprint layerId=5 counterId=186 debugPrint=1");
		dut.lteCli("kpi counters setdebugprint layerId=5 counterId=187 debugPrint=1");
		
		
		GeneralUtils.startLevel("Save original lowResEventsParams");
		int initialValueCpuLoadThreshold  = enodeBConfig.getCpuLoadThreshold(dut);
		int initialValueRrcLoadThreshold = enodeBConfig.getRrcLoadThreshold(dut);
		int initialValueMemoryLoadThreshold = enodeBConfig.getMemoryLoadThreshold(dut);
		int initialValueSampleInterval = enodeBConfig.getLowResourcesSampleInterval(dut);
		int initialValueReportInterval = enodeBConfig.getLowResourcesReportInterval(dut);
		int initialValueHyst = enodeBConfig.getLowResourceHyst(dut);
		GeneralUtils.stopLevel();
		GeneralUtils.startLevel("Save original values and Set new values of PdcpDisTmrBs for using more of the eNodeB Resources");
		int initialValuePdcpDisTmrBs[] = new int[10];
		for(int qciId = 1; qciId <= 10; qciId++){
			initialValuePdcpDisTmrBs[qciId-1] = enodeBConfig.getPdcpDisTmrBs(dut, qciId);
			enodeBConfig.setPdcpDisTmrBs(dut, qciId, 7);
		}
		GeneralUtils.stopLevel();
		
		setLowResEventsParamsTable(1, 1, 1, 10, 10, 1);
		syncGeneralCommands();
		if(safeStartTraffic()){
			boolean hasCpuTrap = false;
			boolean hasRrcTrap = false;
			boolean hasMemoTrap = false;
			report.report("Getting new Alarms from NMS:");
			for(int i=1; i<=30; i++){
				GeneralUtils.unSafeSleep(10000);
				List<AlarmInfo> alarmsInfo = alarmsAndEvents.getAllAlarmsNode(dut);
				if(alarmsInfo != null){
		        	for(AlarmInfo alarmInfo : alarmsInfo){
	        			if(!hasCpuTrap && alarmInfo.alarmType.equals("Low CPU Resources")){
	        				hasCpuTrap = true;
	        				alarmsAndEvents.printAlarmInfo(alarmInfo);
	        			}else if(!hasRrcTrap && alarmInfo.alarmType.equals("Low RRC Resources")){
	        				hasRrcTrap = true;
	        				alarmsAndEvents.printAlarmInfo(alarmInfo);
	        			}else if(!hasMemoTrap && alarmInfo.alarmType.equals("Low Memory Resources")){
	        				hasMemoTrap = true;
	        				alarmsAndEvents.printAlarmInfo(alarmInfo);
	        			}
	        			
	        			if(hasCpuTrap && hasRrcTrap && hasMemoTrap){
	        				break;
	        			}
		        	}
				}
			}
			
			report.report("Stopping Parallel Commands");
			try {
				syncCommands.stopCommands();
				syncCommands.moveFileToReporterAndAddLink();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			
        	
			try {
				report.report("Stop Traffic.");
				traffic.stopTraffic();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			if(!hasCpuTrap){
				report.report("Low CPU Resources alarm missing", Reporter.FAIL);
				reason = "Low CPU Resources alarm missing.";
			}
			if(!hasRrcTrap){
				report.report("Low RRC Resources alarm missing", Reporter.FAIL);
				reason += "Low RRC Resources alarm missing.";
			}
			if(!hasMemoTrap){
				report.report("Low Memory Resources alarm missing", Reporter.FAIL);
				reason += "Low Memory Resources alarm missing.";
			}
		}else{
			report.report("Couldn't start traffic", Reporter.FAIL);
			reason = "Couldn't start traffic.";
		}
		
		GeneralUtils.startLevel("Set original values.");
		setLowResEventsParamsTable(initialValueCpuLoadThreshold, initialValueRrcLoadThreshold, initialValueMemoryLoadThreshold,
				initialValueSampleInterval, initialValueReportInterval, initialValueHyst);
		GeneralUtils.startLevel("Set original values of PdcpDisTmrBs");
		for(int qciId = 1; qciId <= 10; qciId++){
			enodeBConfig.setPdcpDisTmrBs(dut, qciId, initialValuePdcpDisTmrBs[qciId-1]);
		}
		GeneralUtils.stopLevel();
		GeneralUtils.stopLevel();
		
		/*Close Logs of KPI counters for SW debug*/
		dut.lteCli("kpi counters setdebugprint layerId=5 counterId=185 debugPrint=0");
		dut.lteCli("kpi counters setdebugprint layerId=5 counterId=186 debugPrint=0");
		dut.lteCli("kpi counters setdebugprint layerId=5 counterId=187 debugPrint=0");
	}
	
	private boolean safeStartTraffic(){
		try {
			report.report("Start Traffic.");
			return traffic.startTraffic();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private void setLowResEventsParamsTable(int cpuLoadThreshold, int rrcLoadThreshold, int memoryLoadThreshold, 
			int sampleInterval, int reportInterval, int hyst){
		try {
			report.startLevel("Set lowResEventsParams Table:");
		
			enodeBConfig.setCpuLoadThreshold(dut, cpuLoadThreshold);
			enodeBConfig.setRrcLoadThreshold(dut, rrcLoadThreshold);
			enodeBConfig.setMemoryLoadThreshold(dut, memoryLoadThreshold);
			enodeBConfig.setLowResourcesSampleInterval(dut, sampleInterval);
			enodeBConfig.setLowResourcesReportInterval(dut, reportInterval);
			enodeBConfig.setLowResourceHyst(dut, hyst);
		
			report.stopLevel();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private ArrayList<UE> getDutUEs() {
		ArrayList<UE> ues = new ArrayList<UE>();
		try {
			ues.addAll(SetupUtils.getInstance().getStaticUEs(dut));
			ues.addAll(SetupUtils.getInstance().getDynamicUEs());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ues;
	}
	
	/**
	 * @author Shahaf Shuhamy uses String [] command list and starting another
	 *         Thread to commands in cli and put them in another File located in
	 *         root folder
	 */
	private void syncGeneralCommands() {
		report.report("Starting parallel commands");
		commandList.add("ue show link");
		commandList.add("ue show rate");
		commandList.add("rlc show amdlstats");
		commandList.add("db get trapGlobal");
		commandList.add("db get lowResEventsStatus");
		commandList.add("db get lowResEventsParams");
		commandList.add("system show memory");
		commandList.add("db get PTPStatus");
		
		try {
			syncCommands = new ParallelCommandsThread(commandList, dut, null, null);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		syncCommands.start();
	}
}

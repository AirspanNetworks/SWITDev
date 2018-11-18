package testsNG.PerformanceAndQos.Volte;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.snmp4j.smi.Variable;

import Action.UeAction.UeAction;
import EnodeB.EnodeB;
import Entities.LoadParam;
import Entities.StreamParams;
import Entities.ITrafficGenerator.CounterUnit;
import Entities.ITrafficGenerator.Protocol;
import Entities.ITrafficGenerator.TransmitDirection;
import UE.UE;
import UE.UESimulator;
import UE.VirtualUE;
import Utils.GeneralUtils;
import Utils.GeneralUtils.RebootType;
import Utils.SetupUtils;
import Utils.StreamList;
import Utils.WatchDog.CommandWatchActiveUEsNmsTable;
import Utils.WatchDog.CommandWatchUeLinkStatusVolteOrEmergency;
import Utils.WatchDog.WatchDogManager;
import jsystem.framework.ParameterProperties;
import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;
import testsNG.Actions.SoftwareUtiles;
import testsNG.Actions.TrafficCapacity;
import testsNG.Actions.Utils.CalculatorMap;
import testsNG.PerformanceAndQos.Throughput.TPTBase;

public class P0 extends TPTBase{

	protected static final int HALT_STREAM_PARAM_VOLTE = 200;
	protected static final double DATA_QCIS_THRESHOLD_PERCENT = 0.5;

	protected CommandWatchActiveUEsNmsTable wdActiveUesQci1;
	protected CommandWatchActiveUEsNmsTable wdActiveUesQci9;
	protected CommandWatchUeLinkStatusVolteOrEmergency wdUeLinkStatus;
	protected CommandWatchUeLinkStatusVolteOrEmergency wdUeLinkStatusEmergency;
	protected CommandWatchUeLinkStatusVolteOrEmergency wdUeLinkStatusEmergencyNoVolte;
	protected ArrayList<UE> ueListEmergency;
	
	protected double dlCriteriaQcisInList;
	protected double ulCriteriaQcisInList;
	protected double dlCriteriaDataQcis;
	protected double ulCriteriaDataQcis;
	
	protected boolean rejectTest;
	protected boolean checkActiveQci9;
	protected ArrayList<CounterUnit> counters;
	protected StreamList streamListCounters = null;
	protected Set<String> allStreams = null;
	protected ArrayList<String> headLines = null;
	protected HashMap<String,Double> streamsSum;
	protected HashMap<String,Integer> streamsCounter;
		
	protected ArrayList<ArrayList<StreamParams>> streamsForPriorityTest = new ArrayList<ArrayList<StreamParams>>();
	protected ArrayList<Long> priorityTestFirstResult;
	protected ArrayList<Long> priorityTestSecondResult;
	protected int firstResultSizeOfStreams = 1;
	protected int secondResultSizeOfStreams = 1;
	
	protected String shortTerm = CounterUnit.SHORT_TERM_AVG_LATENCY.value;
	protected String rfcAvgJitter = CounterUnit.RFC_4689_ABSOLUTE_AVG_JITTER.value;
	protected String avgJitter = CounterUnit.AVG_JITTER.value;
	protected String dropFrame = CounterUnit.DROPPED_FRAME_PERCENT.value;
	protected String dropFrameRate = CounterUnit.DROPPED_FRAME_PERCENT_RATE.value;
		
	protected ArrayList<StreamParams> streamParamsAllCounters;
	protected ArrayList<UE> uesToStop=null;
	protected UE newUe = null;
	protected ArrayList<LoadParam> loadingParametersUl = null;
	protected ArrayList<LoadParam> loadingParametersDl = null;
	private double loadPerUEDl = 0;
	private double loadPerUEUl = 0;
	protected ArrayList<String> qcisInList;
	
	private boolean enableJitterSuccess;
	
	private int runtime;
	private int totalUEsPerCell;
	private int udpDataLoad;
	private int qci1_5PacketSize;
	private int qci1LoadInFps;
	private int qci5LoadInKbps;
	
	private boolean isEmergencyCallTest;
	private boolean qci1DataBeforeECue;
	private boolean isSamplingTest = true;
	
	private String apnEmergencyCall = null;
	private String defaultApn = null;
	
	private String priorityTestFirstQcis = "";
	private String priorityTestSecondQcis = "";
	
	protected final int threshold = 16;
	
	public String getApnEmergencyCall() {
		return apnEmergencyCall;
	}

	public void setApnEmergencyCall(String apnEmergencyCall) {
		this.apnEmergencyCall = apnEmergencyCall;
	}

	public String getDefaultApn() {
		return defaultApn;
	}

	public void setDefaultApn(String defaultApn) {
		this.defaultApn = defaultApn;
	}

	@Override
	public void init() throws Exception {
		resetParams();
		qcisInList = new ArrayList<>();
		qcisInList.add("1");
		qcisInList.add("5");
		loadPerUEDl = 0;
		loadPerUEUl = 0;
		loadingParametersDl = null;
		loadingParametersUl = null;
		isEmergencyCallTest = false;
		qci1DataBeforeECue = false;
		isSamplingTest = true;
		wd = WatchDogManager.getInstance();
		
		try {
			uesToStop = getUES(dut);
		} catch (Exception e) {
			e.printStackTrace();
			uesToStop = new ArrayList<UE>();
		}
		super.init();
	}
	
	/**
	 * Tests
	 */
	
	@Test
	@TestProperties(name = "VoLTE - General Volte test", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful","RunTime","Load_DL","Expected_UL","Load_UL","Expected_DL","ApnEmergencyCall","DefaultApn" })
	public void General_Volte_Test(){
		packetSize = qci1_5PacketSize;
		testName = "VoLTE_Pt"+(totalUEsPerCell==1?"":"M")+"P_Basic_Call";
		if(totalUEsPerCell==1){
			streamsMode = "PTP";			
		}else{
			streamsMode = "PTMP";			
		}
		TEST_TIME_MILLIS = (long) (runtime*60*1000);
		qci.add('1');
		qci.add('5');
		if(udpDataLoad!=0){
			qci.add('9');
		}
		GeneralUtils.printToConsole(dut.getName() + " " + dut.getNetspanName());
		if(totalUEsPerCell==1){
			ueList = getUES(dut,1);
		}else{
			ueList = getUES(dut);
		}
		ueNameListStc = convertUeToNamesList(ueList);
		preTestVolte();
		TestProcess();
		afterTestVolte(TPT_THRESHOLD_PRECENT,false);
	}
	
	@Test
	@TestProperties(name = "Emergency Call - General Volte test", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful","RunTime","Load_DL","Expected_UL","Load_UL","Expected_DL","ApnEmergencyCall","DefaultApn" })
	public void General_Emergency_Call_Test(){
		isEmergencyCallTest = true;
		General_Volte_Test();
	}
	
	@Test
	@TestProperties(name = "VoLTE - Call Reject when maximum VoLTE Active Calls", returnParam = { "IsTestWasSuccessful" }, paramsInclude = {"DUT"})
	public void VoLTE_Call_Reject_when_maximum_VoLTE_Active_Calls(){
		int volteOriginal = dut.getMaxVolteCalls();
		boolean setMaxVolte;
		GeneralUtils.startLevel("Setting max volte calls to 1 and rebooting enodeb");
		setMaxVolte = setMaxVolteCallAndReboot(1);
		setMaxVolte = setMaxVolte && dut.getMaxVolteCalls()==1;
		GeneralUtils.stopLevel();
		if(!setMaxVolte){
			report.report("Failed to set max volte calls to 1 in enodeb "+dut.getNetspanName(),Reporter.FAIL);
		}else{
			report.report("Succeeded to set max volte calls to 1 in enodeb "+dut.getNetspanName());
		}
		
		if(setMaxVolte){
			setRuntime(1);
			setTotalUEsPerCell(1);
			setUdpDataLoad(0);
			setQci1_5PacketSize(112);
			setQci1LoadInFps(50);
			setQci5LoadInKbps(20);
			
			testName = "VoLTE_Call_Reject_when_maximum_VoLTE_Active_Calls";
			streamsMode = "PTP";
			packetSize = qci1_5PacketSize;
			TEST_TIME_MILLIS = (long) (runtime*60*1000);
			qci.add('1');
			qci.add('5');
			GeneralUtils.printToConsole(dut.getName() + " " + dut.getNetspanName());
			ueList = getUES(dut,1);
			ueNameListStc = convertUeToNamesList(ueList);
			preTestVolte();
			TestProcessRejectTest();
			afterTestVolteRejectTest(TPT_THRESHOLD_PRECENT);			
		}
		
		if(volteOriginal==GeneralUtils.ERROR_VALUE){
			if(dut.getArchitecture()==EnodeB.Architecture.XLP){
				volteOriginal = 80;
			}else{
				volteOriginal = 64;
			}
		}
		if(dut.getMaxVolteCalls() != volteOriginal){
			GeneralUtils.startLevel("Setting max volte calls to original value and rebooting enodeb");
			setMaxVolteCallAndReboot(volteOriginal);
			GeneralUtils.stopLevel();			
		}
	}
	
	@Test
	@TestProperties(name = "Emergency Call prioritized over reboot", returnParam = { "IsTestWasSuccessful" }, paramsInclude = {"DUT"})
	public void emergency_call_Prioritized_over_reboot(){
		isSamplingTest = false;
		isEmergencyCallTest = true;

		setRuntime(2);
		setTotalUEsPerCell(1);
		setUdpDataLoad(0);
		setQci1_5PacketSize(112);
		setQci1LoadInFps(50);
		setQci5LoadInKbps(20);
		
		testName = "Emergency Call prioritized over reboot";
		streamsMode = "PTP";
		packetSize = qci1_5PacketSize;
		TEST_TIME_MILLIS = (long) (runtime*60*1000);
		qci.add('1');
		qci.add('5');
		GeneralUtils.printToConsole(dut.getName() + " " + dut.getNetspanName());
		ueList = getUES(dut,1);
		ueNameListStc = convertUeToNamesList(ueList);
		preTestVolte();
		
		try {
			startTrafficInTest();
			GeneralUtils.startLevel("rebooting with Netspan");
			boolean actionReboot = enbConfig.rebootWithNetspanOnly(dut, RebootType.WARM_REBOOT,true,40*1000);
			GeneralUtils.stopLevel();
			
			if(actionReboot) {
				report.report("Boot detected before Emergency call finished!",Reporter.FAIL);
			}else{
				dut.setExpectBooting(true);
				report.report("Enodeb was not booted while UE was connected");
				GeneralUtils.startLevel("Stopping UEs");
				peripheralsConfig.stopUEsOnlySnmp(ueList);
				GeneralUtils.stopLevel();
				
				if(dut.waitForReboot(2*60*1000)) {
					report.report("Boot detected after Emergency call finished");
				}else {
					report.report("Boot NOT detected after Emergency call finished!",Reporter.FAIL);
				}
			}
			dut.setExpectBooting(false);
			report.report("Waiting for all running");
			dut.waitForAllRunningAndInService(EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
			report.report("Enodeb is in all running state");
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		//add parallel commands
		if(syncCommands != null){
			report.report("Stopping Parallel Commands");
			syncCommands.stopCommands();
			makeThreadObjectFinish();
			report.report("Commands File Path: " + syncCommands.getFile());
			syncCommands.moveFileToReporterAndAddLink();
		}
		GeneralUtils.startLevel("Stopping Traffic");
		trafficSTC.stopTraffic();
		GeneralUtils.stopLevel();
		
		GeneralUtils.startLevel("Starting UEs");
		peripheralsConfig.startUEsOnlySnmp(ueList);
		GeneralUtils.stopLevel();
	}
	
	@Test
	@TestProperties(name = "Emergency Call prioritized over regular VoLTE at max VoLTE users", returnParam = { "IsTestWasSuccessful" }, paramsInclude = {
			"DUT","ApnEmergencyCall","DefaultApn"})
	public void Emergency_Call_Prioritized_over_regular_VoLTE_at_max_VoLTE_users(){
		if(apnEmergencyCall == null || defaultApn == null){
			report.report("One the apns is not configured. Failing test", Reporter.FAIL);
		}
		int volteOriginal = dut.getMaxVolteCalls();
		boolean setMaxVolte;
		GeneralUtils.startLevel("Setting max volte calls to 1 and rebooting enodeb");
		setMaxVolte = setMaxVolteCallAndReboot(1);
		setMaxVolte = setMaxVolte && dut.getMaxVolteCalls()==1;
		GeneralUtils.stopLevel();
		if(!setMaxVolte){
			report.report("Failed to set max volte calls to 1 in enodeb "+dut.getNetspanName(),Reporter.FAIL);
		}else{
			report.report("Succeeded to set max volte calls to 1 in enodeb "+dut.getNetspanName());
		}
		
		if(setMaxVolte){
			ueListEmergency = getUES(dut,2);
			setRuntime(1);
			setTotalUEsPerCell(1);
			setUdpDataLoad(0);
			setQci1_5PacketSize(112);
			setQci1LoadInFps(50);
			setQci5LoadInKbps(20);
			
			testName = "VoLTE_Call_Reject_when_maximum_VoLTE_Active_Calls";
			streamsMode = "PTP";
			packetSize = qci1_5PacketSize;
			TEST_TIME_MILLIS = (long) (runtime*60*1000);
			qci.add('1');
			qci.add('5');
			GeneralUtils.printToConsole(dut.getName() + " " + dut.getNetspanName());
			ueList = new ArrayList<UE>();
			ueList.add(ueListEmergency.get(0));
			ueNameListStc = convertUeToNamesList(ueList);
			preTestVolte();
			TestProcessEmergencyCallPrioritized();
			afterTestVolteEmergencyCallPrioritized(TPT_THRESHOLD_PRECENT);			
		}
		
		if(volteOriginal==GeneralUtils.ERROR_VALUE){
			if(dut.getArchitecture()==EnodeB.Architecture.XLP){
				volteOriginal = 80;
			}else{
				volteOriginal = 64;
			}
		}
		if(dut.getMaxVolteCalls() != volteOriginal){
			GeneralUtils.startLevel("Setting max volte calls to original value and rebooting enodeb");
			setMaxVolteCallAndReboot(volteOriginal);
			GeneralUtils.stopLevel();			
		}
	}
	
	@Test
	@TestProperties(name = "VoLTE - PtP Call priority over Data", returnParam = { "IsTestWasSuccessful" }, paramsInclude = {"DUT"})
	public void VoLTE_PtP_Call_priority_Over_Data(){
		setRuntime(1);
		setTotalUEsPerCell(1);
		setUdpDataLoad(105);
		setQci1_5PacketSize(82);
		setQci1LoadInFps(50);
		setQci5LoadInKbps(20);
		priorityTestFirstQcis = "9";
		priorityTestSecondQcis = "1 and 5";
		
		testName = "VoLTE_PtP_Call_priority_Over_Data";
		streamsMode = "PTP";
		packetSize = qci1_5PacketSize;
		TEST_TIME_MILLIS = (long) (runtime*60*1000);
		qci.add('9');
		GeneralUtils.printToConsole(dut.getName() + " " + dut.getNetspanName());
		ueList = getUES(dut);
		if(ueList.size()<4){
			report.report("Minimum of 4 UEs is needed for test",Reporter.FAIL);
			reason = "Minimum of 4 UEs is needed for test";
			return;
		}
		ueNameListStc = convertUeToNamesList(ueList);
		preTestVolte();
		TestProcessCallPriorityOverDataTest();
		afterTestVolte(TPT_THRESHOLD_PRECENT, true);			
	}
	
	protected void TestProcessCallPriorityOverDataTest() {
		while (testIsNotDoneStatus) {
			resetParams();
			try {
				if(!startTrafficAndSampleCallPriorityOverDataTest()){
					testIsNotDoneStatus = false;
					resetTestBol = false;
					exceptionThrown = true;
					GeneralUtils.stopAllLevels();
					qci = new ArrayList<Character>();
					qci.add('9');
				}
			} catch (Exception e) {
				report.report("Stopping Parallel Commands From Java Exception");
				testIsNotDoneStatus = false;
				resetTestBol = false;
				exceptionThrown = true;
				reason = "network connection Error";
				report.report(e.getMessage() + " caused Test to stop", Reporter.FAIL);
				e.printStackTrace();
				GeneralUtils.stopAllLevels(); 
			}
			if (resetTestBol) {
				numberOfResets++;
				if(syncCommands != null){
					report.report("Stopping Parallel Commands");
					syncCommands.stopCommands();
				}
				makeThreadObjectFinish();
				if(syncCommands != null){
					report.report("Commands File Path: " + syncCommands.getFile());					
				}
				testIsNotDoneStatus = true;
				try {
					if(syncCommands != null){
						syncCommands.moveFileToReporterAndAddLink();
					}
					report.report("Stopping Traffic");
					trafficSTC.stopTraffic();
					report.report("Resetting test", Reporter.WARNING);
					reason = "reset Test because of stream halt";
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		if (numberOfResets >= 3) {
			// using this flag in order to NOT print results.
			testIsNotDoneStatus = false;
			printResultsForTest = false;
			report.report("Test Was Reseted Too many times because of more then " + PRECENT_OF_UE_RESET + "%"
					+ " Of the Streams are in Halt - check Setup", Reporter.FAIL);
			reason = "Test Was Restarted Too many Times due to Traffic halt";
		}
		try {
			syncCommands.stopCommands();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		makeThreadObjectFinish();
		try {
			syncCommands.moveFileToReporterAndAddLink();
		} catch (Exception e) {
			report.report("Exception in ReporterHelper.copyFileToReporterAndAddLink could not attach Command File");
			e.printStackTrace();
		}
	}
	
	protected boolean startTrafficAndSampleCallPriorityOverDataTest() throws Exception{
		if(!startTrafficInTest()){
			return false;
		}
		streamsForPriorityTest = null;
		streamsForPriorityTest = new ArrayList<ArrayList<StreamParams>>();
		if(!samplePriorityOverDataTest(false)){
			return true;
		}
		enableVolteStreamsForUEPriorityOverDataTest();
		setLoadPerStreamPriorityOverDataTest();
		priorityTestFirstResult = getUlDlResultsFromList(streamsForPriorityTest,false);
		firstResultSizeOfStreams = streamsForPriorityTest.size();
		streamsForPriorityTest = null;
		streamsForPriorityTest = new ArrayList<ArrayList<StreamParams>>();

		trafficSTC.saveConfigFileAndStart();
		report.report("Wait 10 seconds for traffic of second UE to stabilize");
		GeneralUtils.unSafeSleep(10*1000);
		if(wdActiveUesQci1!=null){
			wd.removeCommand(wdActiveUesQci1);			
		}
		wdActiveUesQci1 = new CommandWatchActiveUEsNmsTable(dut,ueNameListStc.size(),'1',true);
		wd.addCommand(wdActiveUesQci1);		
		startingTestTime = System.currentTimeMillis();
		if(!samplePriorityOverDataTest(true)){
			return true;
		}
		priorityTestSecondResult = getUlDlResultsFromList(streamsForPriorityTest,false);
		secondResultSizeOfStreams = streamsForPriorityTest.size();
		return true;
	}
	
	private boolean samplePriorityOverDataTest(boolean closeLevel){
		startingTestTime = System.currentTimeMillis();
		while (System.currentTimeMillis() - startingTestTime <= TEST_TIME_MILLIS) {
			sampleResultsStatusOk();
	
			if (restartTime) {
				startingTestTime = System.currentTimeMillis();
			}

			// if exception thrown = Connection closed -> stop test!
			if (exceptionThrown) {
				if(reason == ""){
					reason = "TRAFFIC ERROR";
				}
				
				report.report("Performance Error in Test! ,reason: "+reason);
				GeneralUtils.stopLevel();
				testIsNotDoneStatus = false;
				return false;
			}
			try {
				synchronized (this) {
					this.wait(1000);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			if(resetUEs){
				resetUEs = false;
				resetTestBol = true;
				report.report("Resetting UES");
				if(ueList.get(0) instanceof VirtualUE){
					UESimulator uesim = UESimulator.getInstance();
					uesim.stop();
				}else{
					rebootUEs(ueList);
				}
			}
				
			if (resetDueToMultiHaltStreams) {
				// more than 30% of the streams are in halt mode!
				resetDueToMultiHaltStreams = false;
				report.report("Reseting Time stamp for test");
				startingTestTime = System.currentTimeMillis();
				resetTestBol = true;
				if (numberOfResets < 3) {
					GeneralUtils.stopLevel();
					return false;
				} else {
					resetTestBol = false;
					startingTestTime = TEST_TIME_MILLIS;
					GeneralUtils.stopLevel();
					testIsNotDoneStatus = false;
					return false;
				}
			}
		}
		testIsNotDoneStatus = false;
		if(closeLevel){
			GeneralUtils.stopLevel();
		}
		return true;
	}
	
	private void setLoadPerStreamPriorityOverDataTest() {
		ArrayList<LoadParam> loadDl = new ArrayList<LoadParam>();
		ArrayList<LoadParam> loadUl = new ArrayList<LoadParam>();
		dlCriteriaQcisInList=0;
		ulCriteriaQcisInList=0;	
		for(String str:ueNameListStc){
			String name = str;
			int ueNum = Integer.valueOf(name.replaceAll("\\D+", "").trim());
			double loadInKbpsDl = (488+20)*8*(50)/1000.0;
			double loadInKbpsUl = 20;
			
			report.report(name+": setting DL for stream with qci 1 and 5: "+loadInKbpsDl+" Kbps");
			LoadParam lpDl = new LoadParam(loadInKbpsDl, '1', ueNum, LoadParam.LoadUnit.KILOBITS_PER_SECOND,packetSize);
			loadDl.add(lpDl);
			lpDl = new LoadParam(loadInKbpsDl, '5', ueNum, LoadParam.LoadUnit.KILOBITS_PER_SECOND,packetSize);
			loadDl.add(lpDl);
			report.report(name+": setting UL for stream with qci 1 and 5: "+loadInKbpsUl+" Kbps");
			LoadParam lpUl = new LoadParam(loadInKbpsUl, '1', ueNum, LoadParam.LoadUnit.KILOBITS_PER_SECOND,packetSize);
			loadUl.add(lpUl);
			lpUl = new LoadParam(loadInKbpsUl, '5', ueNum, LoadParam.LoadUnit.KILOBITS_PER_SECOND,packetSize);
			loadUl.add(lpUl);
			dlCriteriaQcisInList+=2.0*loadInKbpsDl/1000.0;
			ulCriteriaQcisInList+=2.0*loadInKbpsUl/1000.0;			
		}
		trafficSTC.setLoadStreamDL(loadDl);	
		trafficSTC.setLoadStreamUL(loadUl);
		try{
			trafficSTC.setFrameSizeStreamDL(loadDl);
			trafficSTC.setFrameSizeStreamUL(loadUl);			
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private void enableVolteStreamsForUEPriorityOverDataTest() {
		ArrayList<Character> qci1 = new ArrayList<Character>();
		qci1.add('1');
		qci1.add('5');
		
		try {
			trafficSTC.enableStreamsByNameAndQciAndDirection(this.protocol,ueNameListStc, qci1, TransmitDirection.BOTH);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private void afterTestVolteEmergencyCallPrioritized(double passCriteria) {
		boolean flagActive1 = false;
		if(wdActiveUesQci1 != null){
			flagActive1 = wdActiveUesQci1.getFlagActive();
		}
		boolean flagUeLinkStatus = false;
		if(wdUeLinkStatus != null){
			flagUeLinkStatus = wdUeLinkStatus.getFlagActive();			
		}
		boolean flagUeLinkStatusEmergency = false;
		if(wdUeLinkStatusEmergency != null){
			flagUeLinkStatusEmergency = wdUeLinkStatusEmergency.getFlagActive();			
		}
		boolean flagUeLinkStatusEmergencyNoVolte = false;
		if(wdUeLinkStatusEmergencyNoVolte != null){
			flagUeLinkStatusEmergencyNoVolte = wdUeLinkStatusEmergencyNoVolte.getFlagActive();			
		}
		boolean flagActive9 = false;
		if(wdActiveUesQci9!=null){
			flagActive9 = wdActiveUesQci9.getFlagActive();			
		}
		if(DLULwd != null){
			printDLandULAverageTableAndCounters();			
		}
		terminateWatchDogVolte();
		
		report.report("Stop traffic");
		trafficSTC.stopTraffic();

		if (exceptionThrown) {
			report.report("Exception failed the test - No Results");
		} else {
			sumAndPrintTablesPerPort();
			printPerStreamTables(listOfStreamList);

			GeneralUtils.printToConsole("Print Results state: "+printResultsForTest);
			if (printResultsForTest) {
				checkTptOverThreshold(debugPrinter, listOfStreamList, passCriteria, false);
				if(flagActive1 && qci1DataBeforeECue){
					report.report("Number of Qci 1 active UEs in NMS status table was 1 during the whole test");
				}else{
					report.report("Number of Qci 1 active UEs in NMS status table was not 1 during the whole test",Reporter.FAIL);
					reason = "Number of Qci 1 active UEs in NMS status table was not 1 during the whole test";
				}
				if(flagActive9){
					report.report("Number of Qci 9 active UEs in NMS status table was 1 or more during the whole test");
				}else{
					report.report("Number of Qci 9 active UEs in NMS status table was not 1 or more during the whole test",Reporter.FAIL);
					reason = "Number of Qci 9 active UEs in NMS status table was not 1 or more during the whole test";
				}
				if(flagUeLinkStatus){
					report.report("First UE was connected to enodeb with volte flag enabled before connection Emergency UE");
				}else{
					report.report("First UE was not connected to enodeb with volte flag enabled before connection Emergency UE",Reporter.FAIL);
					reason = "First UE was not connected to enodeb with volte flag enabled before connection Emergency UE";
				}
				if(flagUeLinkStatusEmergency){
					report.report("Second UE was connected with emergency flag enabled");
				}else{
					report.report("Second UE was not connected with emergency flag enabled",Reporter.FAIL);
					reason = "Second UE was not connected with emergency flag enabled";
				}
				if(flagUeLinkStatusEmergencyNoVolte){
					report.report("After connecting Emergency UE, no UE was connected with volte flag");
				}else{
					report.report("After connecting Emergency UE, at least 1 UE was connected with volte flag",Reporter.FAIL);
					reason = "After connecting Emergency UE, at least 1 UE was connected with volte flag";
				}
				printDataOfUes();
			}
		}
		if(ueList.size()>1){
			UeAction ueAction = new UeAction();
			ueAction.setUEs(newUe.getName());
			ueAction.setApnName(defaultApn);
			ueAction.setAPN();			
		}
	}
	
	/**
	 * Infra methods
	 */

	private void gettingLoadPerStreamQci9(){
		getRadioProfile();

		if (runWithDynamicCFI)
			radioParams.setCfi("2");

		String calculatorStringKey = ParseRadioProfileToString(radioParams);
		if (calculatorStringKey == null) {
			report.report("Calculator key value is empty - fail test", Reporter.FAIL);
			return;
		}
		CalculatorMap calcMap = new CalculatorMap();
		String dl_ul = calcMap.getPassCriteria(calculatorStringKey);
		String[] dlul = dl_ul.split("_");
		loadPerUEDl = Double.parseDouble(dlul[0]);
		loadPerUEUl = Double.parseDouble(dlul[1]);
	}
	
	private void configureLoadQci1And5And9(){
		gettingLoadPerStreamQci9();
	
		loadPerUEDl = (loadPerUEDl*(udpDataLoad/100.0))/ueNameListStc.size();
		loadPerUEUl = (loadPerUEUl*(udpDataLoad/100.0))/ueNameListStc.size();
		
		loadingParametersDl = new ArrayList<LoadParam>();
		loadingParametersUl = new ArrayList<LoadParam>();

		dlCriteriaQcisInList = 0;
		ulCriteriaQcisInList = 0;
		
		dlCriteriaDataQcis = 0;
		ulCriteriaDataQcis = 0;
		boolean printed = false;
		for(String str : ueNameListStc){
			String name = str;
			int ueNum = Integer.valueOf(name.replaceAll("\\D+", "").trim());
			double loadFps = (packetSize+20)*8*(qci1LoadInFps)/1000.0;
			
			LoadParam lpQci1 = new LoadParam(loadFps, '1', ueNum, LoadParam.LoadUnit.KILOBITS_PER_SECOND,packetSize);
			loadingParametersDl.add(lpQci1);
			loadingParametersUl.add(lpQci1);
			dlCriteriaQcisInList+=loadFps/1000.0;
			ulCriteriaQcisInList+=loadFps/1000.0;

			LoadParam lpQci5 = new LoadParam(qci5LoadInKbps, '5', ueNum, LoadParam.LoadUnit.KILOBITS_PER_SECOND,packetSize);
			loadingParametersDl.add(lpQci5);
			loadingParametersUl.add(lpQci5);
			dlCriteriaQcisInList+=qci5LoadInKbps/1000.0;
			ulCriteriaQcisInList+=qci5LoadInKbps/1000.0;
			
			LoadParam lpQci9Dl = new LoadParam(loadPerUEDl*1000, '9', ueNum, LoadParam.LoadUnit.KILOBITS_PER_SECOND,1400);
			loadingParametersDl.add(lpQci9Dl);
			if(udpDataLoad>100){
				dlCriteriaDataQcis+=loadPerUEDl*100/udpDataLoad;
			}else{
				dlCriteriaDataQcis+=loadPerUEDl;				
			}
			
			LoadParam lpQci9Ul = new LoadParam(loadPerUEUl*1000, '9', ueNum, LoadParam.LoadUnit.KILOBITS_PER_SECOND,1400);
			loadingParametersUl.add(lpQci9Ul);
			if(udpDataLoad>100){
				ulCriteriaDataQcis+=loadPerUEUl*100/udpDataLoad;
			}else{
				ulCriteriaDataQcis+=loadPerUEUl;			
			}
			
			if(!printed){
				GeneralUtils.startLevel("Load for each UE");
				report.report("DL and UL for stream with qci 1: "+qci1LoadInFps+" fps with frame size of "+packetSize);
				report.report("Equivalent to: ("+packetSize+"+20)*8*"+qci1LoadInFps+"/1000 = "+loadFps+" kbps");
				report.report("DL and UL for stream with qci 5: "+convertTo3DigitsAfterPoint(qci5LoadInKbps)+" kbps");
				report.report("DL for stream with qci 9: "+convertTo3DigitsAfterPoint(loadPerUEDl)+" Mbps");
				report.report("UL for stream with qci 9: "+convertTo3DigitsAfterPoint(loadPerUEUl)+" Mbps");
				GeneralUtils.stopLevel();
				printed = true;
			}
		}
	}
	
	private void configureLoadOnlyQci1And5(){
		loadingParametersDl = new ArrayList<LoadParam>();
		loadingParametersUl = new ArrayList<LoadParam>();

		dlCriteriaQcisInList = 0;
		ulCriteriaQcisInList = 0;
		boolean printed = false;
		for(String str : ueNameListStc){
			String name = str;
			int ueNum = Integer.valueOf(name.replaceAll("\\D+", "").trim());
			double loadFps = (packetSize+20)*8*(qci1LoadInFps)/1000.0;
			
			LoadParam lpQci1 = new LoadParam(loadFps, '1', ueNum, LoadParam.LoadUnit.KILOBITS_PER_SECOND,packetSize);
			loadingParametersDl.add(lpQci1);
			loadingParametersUl.add(lpQci1);
			dlCriteriaQcisInList+=loadFps/1000.0;
			ulCriteriaQcisInList+=loadFps/1000.0;

			LoadParam lpQci5 = new LoadParam(qci5LoadInKbps, '5', ueNum, LoadParam.LoadUnit.KILOBITS_PER_SECOND,packetSize);
			loadingParametersDl.add(lpQci5);
			loadingParametersUl.add(lpQci5);
			dlCriteriaQcisInList+=qci5LoadInKbps/1000.0;
			ulCriteriaQcisInList+=qci5LoadInKbps/1000.0;
			
			if(!printed){
				GeneralUtils.startLevel("Load for each UE");
				report.report("DL and UL for stream with qci 1: "+qci1LoadInFps+" fps with frame size of "+packetSize);
				report.report("Equivalent to: ("+packetSize+"+20)*8*"+qci1LoadInFps+"/1000 = "+loadFps+" kbps");
				report.report("DL and UL for stream with qci 5: "+convertTo3DigitsAfterPoint(qci5LoadInKbps)+" kbps");
				GeneralUtils.stopLevel();
				printed = true;
			}
		}
	}
	
	private void setLoadPerStreamTestVolte(){		
		if(udpDataLoad!=0){
			configureLoadQci1And5And9();
		}else{
			configureLoadOnlyQci1And5();
		}
		
		trafficSTC.setLoadStreamDL(loadingParametersDl);	
		trafficSTC.setLoadStreamUL(loadingParametersUl);
		try{
			trafficSTC.setFrameSizeStreamDL(loadingParametersDl);
			trafficSTC.setFrameSizeStreamUL(loadingParametersUl);			
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	protected ArrayList<StreamParams> filterArrayListWithQci1(ArrayList<StreamParams> toFilter){
		ArrayList<StreamParams> ret = new ArrayList<StreamParams>();
		for(StreamParams stream : toFilter){
			String name = stream.getName();
			if(('1')==name.charAt(name.length()-1)){
				ret.add(stream);
			}
		}
		return ret;
	}
	
	protected ArrayList<StreamParams> streamParamsOfStream(ArrayList<StreamParams> toFilter, String name){
		ArrayList<StreamParams> ret = new ArrayList<StreamParams>();
		for(StreamParams str:toFilter){
			if(name.equals(str.getName())){
				ret.add(str);
			}
		}
		return ret;
	}
	
	protected void getCounters(){
		String ts = GeneralUtils.timeFormat(System.currentTimeMillis());
		ArrayList<StreamParams> map = filterArrayListWithQci1(streamParamsAllCounters);
		ArrayList<String> values = new ArrayList<String>();

		String name = "";
		String shortTermToValues = "";
		String rfcAvgJitterToValues = "";
		String avgJitterToValues = "";
		String dropFrameToValues = "";
		String dropFrameRateToValues = "";
		
		for(StreamParams str : map){
			allStreams.add(str.getName());
		}
		for(String strParam:allStreams){
			ArrayList<StreamParams> arrayStream = streamParamsOfStream(map,strParam);
			name = strParam;
			for(StreamParams str : arrayStream){
				if(str.getUnit() != CounterUnit.BITS_PER_SECOND){
					String unit = str.getUnit().value;
					if(!streamsSum.containsKey(name+unit)){
						streamsSum.put(name+unit,str.getRxRateDouble());
						streamsCounter.put(name+unit, 1);
					}else{
						streamsSum.put(name+unit,streamsSum.get(name+unit)+str.getRxRateDouble());
						streamsCounter.put(name+unit, streamsCounter.get(name+unit)+1);
					}
					if(str.getUnit() == CounterUnit.SHORT_TERM_AVG_LATENCY){
						shortTermToValues = convertTo3DigitsAfterPoint(str.getRxRateDouble()/1000.0);
					}else if(enableJitterSuccess && str.getUnit() == CounterUnit.RFC_4689_ABSOLUTE_AVG_JITTER){
						rfcAvgJitterToValues = convertTo3DigitsAfterPoint(str.getRxRateDouble()/1000.0);
					}else if(enableJitterSuccess && str.getUnit() == CounterUnit.AVG_JITTER){
						avgJitterToValues = convertTo3DigitsAfterPoint(str.getRxRateDouble()/1000.0);
					}else if(str.getUnit() == CounterUnit.DROPPED_FRAME_PERCENT){
						dropFrameToValues = String.valueOf(str.getRxRateDouble());
					}else if(str.getUnit() == CounterUnit.DROPPED_FRAME_PERCENT_RATE){
						dropFrameRateToValues = String.valueOf(str.getRxRateDouble());
					}
				}
			}
			
			values.add(shortTermToValues);
			values.add(dropFrameToValues);
			values.add(dropFrameRateToValues);
			if(enableJitterSuccess){
				values.add(rfcAvgJitterToValues);
				values.add(avgJitterToValues);
			}
			streamListCounters.addValues(name, ts, headLines, values);
			values.clear();
		}
	}
	
	protected void printDataOfUes(){
		GeneralUtils.startLevel("Data counters of streams");
		ArrayList<String> sortedList = new ArrayList<String>(allStreams);
		Collections.sort(sortedList);
		for (String stream : sortedList) {
			report.reportHtml(stream, streamListCounters.printTablesHtmlForStream(stream), true);
		}
		GeneralUtils.stopLevel();
		double shortAvg = 0;
		double rfcAvgJitterAvg = 0;
		double avgJitterAvg = 0;
		double dropAvg = 0;
		double dropRateAvg = 0;
		
		double shortAvgGeneral = 0;
		double rfcAvgJitterAvgGeneral = 0;
		double avgJitterAvgGeneral = 0;
		double dropAvgGeneral = 0;
		double dropRateAvgGeneral = 0;
		GeneralUtils.startLevel("Average data counters of streams");
		StreamList streamList = new StreamList();
		ArrayList<String> values = new ArrayList<String>();
		int size = 0;
		for (String stream : sortedList) {
			shortAvg = streamsSum.get(stream+shortTerm)/streamsCounter.get(stream+shortTerm);
			dropAvg = streamsSum.get(stream+dropFrame)/streamsCounter.get(stream+dropFrame);
			dropRateAvg = streamsSum.get(stream+dropFrameRate)/streamsCounter.get(stream+dropFrameRate);
			
			shortAvgGeneral += streamsSum.get(stream+shortTerm);
			dropAvgGeneral += streamsSum.get(stream+dropFrame);
			dropRateAvgGeneral += streamsSum.get(stream+dropFrameRate);
			size+=streamsCounter.get(stream+shortTerm);
			
			values.add(convertTo3DigitsAfterPoint(shortAvg/1000.0));
			values.add(convertTo3DigitsAfterPoint(dropAvg));
			values.add(convertTo3DigitsAfterPoint(dropRateAvg));
			
			if(enableJitterSuccess){
				rfcAvgJitterAvg = streamsSum.get(stream+rfcAvgJitter)/streamsCounter.get(stream+rfcAvgJitter);				
				rfcAvgJitterAvgGeneral += streamsSum.get(stream+rfcAvgJitter);
				values.add(convertTo3DigitsAfterPoint(rfcAvgJitterAvg/1000.0));
				
				avgJitterAvg = streamsSum.get(stream+avgJitter)/streamsCounter.get(stream+avgJitter);				
				avgJitterAvgGeneral += streamsSum.get(stream+avgJitter);
				values.add(convertTo3DigitsAfterPoint(avgJitterAvg/1000.0));
			}
			streamList.addValues("Average_counters", stream, headLines, values);
			values.clear();
		}
		values.add(convertTo3DigitsAfterPoint(shortAvgGeneral/size/1000.0));
		values.add(convertTo3DigitsAfterPoint(dropAvgGeneral/size));
		values.add(convertTo3DigitsAfterPoint(dropRateAvgGeneral/size));
		if(enableJitterSuccess){
			values.add(convertTo3DigitsAfterPoint(rfcAvgJitterAvgGeneral/size/1000.0));
			values.add(convertTo3DigitsAfterPoint(avgJitterAvgGeneral/size/1000.0));
		}
		streamList.addValues("Average_counters", "Total Average", headLines, values);

		report.reportHtml("Average counters", streamList.printTablesHtmlForStream("Average_counters"), true);
		GeneralUtils.stopLevel();
	}
	
	protected boolean shouldReboot(){
		int version = Integer.valueOf(dut.getEnodeBversion().split("_")[0]);
		return version<threshold;
	}
	
	protected boolean setMaxVolteCallAndReboot(int max){
		boolean action = true;
		action = dut.setMaxVolteCalls(max);
		if(action){
			report.report("Succeeded to set max volte calls to "+max+" in enodeb "+dut.getNetspanName());
			boolean needReboot = shouldReboot();
			if(needReboot){
				report.report("Rebooting enodeb");
				dut.reboot();
				action = dut.waitForAllRunningAndInService(EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
				if(action){
					report.report("Enodeb is all running and in service");
				}else{
					report.report("Enodeb failed to reach all running and in service after reboot",Reporter.WARNING);
				}				
			}
		}else{
			report.report("Failed to set max volte calls to "+max,Reporter.WARNING);
		}
		return action;
	}
	
	protected void preTestVolte() {	
		GeneralUtils.startLevel("Setting default value to attenuator");
		if (attenuatorSetUnderTest != null) {
			if(peripheralsConfig.setAttenuatorSetValue(attenuatorSetUnderTest,
					attenuatorSetUnderTest.getDefaultValueAttenuation())){
				report.report("Succeeded to set value");
			}
		} else {
			report.report("There is no attenuator set");
		}
		GeneralUtils.stopLevel();
		
		changeOtherENBsToOOS();
		
		if (runWithDynamicCFI)
			enbConfig.enableDynamicCFI(this.dut);
		else
			enbConfig.disableDynamicCFI(this.dut);
	
		printResultsForTest = true;
		resetTestBol = false;
		exceptionThrown = false;
		rejectTest = false;
		checkActiveQci9 = false;
	}
	
	protected void afterTestVolte(double passCriteria, boolean priorityTest) {
		boolean flagActive = false;
		boolean flagUeLinkStatus = false;
		if(wdActiveUesQci1 != null){
			flagActive = wdActiveUesQci1.getFlagActive();			
		}
		if(wdUeLinkStatus != null){
			flagUeLinkStatus = wdUeLinkStatus.getFlagActive();			
		}
		if(DLULwd != null){
			printDLandULAverageTableAndCounters();			
		}
		terminateWatchDogVolte();
		
		report.report("Stop traffic");
		trafficSTC.stopTraffic();
	
		if (exceptionThrown) {
			report.report("Exception failed the test - No Results");
		} else {
			sumAndPrintTablesPerPort();
			printPerStreamTables(listOfStreamList);

			GeneralUtils.printToConsole("Print Results state: "+printResultsForTest);
			if (printResultsForTest) {
				if(priorityTest){
					checkPriorytyBeforeAndAfter();					
				}
				checkTptOverThreshold(debugPrinter, listOfStreamList, passCriteria, priorityTest);
				validateActiveFlag(flagActive);
				validateUELinkStatus(flagUeLinkStatus);
				printDataOfUes();
			}
		}
	}
	
	protected void checkPriorytyBeforeAndAfter(){
		double firstDl = priorityTestFirstResult.get(1)/1000000.0/firstResultSizeOfStreams;
		double secondDl = priorityTestSecondResult.get(1)/1000000.0/secondResultSizeOfStreams;

		GeneralUtils.startLevel("Check ratios of "+priorityTestFirstQcis+" before and after turning on qci "+priorityTestSecondQcis);
		report.report("Qcis "+priorityTestFirstQcis+" average DL before running qcis "+priorityTestSecondQcis+": "+convertTo3DigitsAfterPoint(firstDl)+" Mbps");
		report.report("Qcis "+priorityTestFirstQcis+" average DL after running qcis "+priorityTestSecondQcis+": "+convertTo3DigitsAfterPoint(secondDl)+" Mbps");
		if(!(secondDl<=firstDl-dlCriteriaQcisInList)){
			report.report("Qcis "+priorityTestFirstQcis+" average DL after running qcis "+priorityTestSecondQcis+" is not at least "+convertTo3DigitsAfterPoint(dlCriteriaQcisInList)+" Mbps lower", Reporter.FAIL);
		}else{
			report.report("Qcis "+priorityTestFirstQcis+" average DL after running qcis "+priorityTestSecondQcis+" is at least "+convertTo3DigitsAfterPoint(dlCriteriaQcisInList)+" Mbps lower");
		}
		if(!(secondDl>=0.9*firstDl)){
			report.report("Qcis "+priorityTestFirstQcis+" average DL after running qcis "+priorityTestSecondQcis+" is not at least 90%", Reporter.FAIL);
		}else{
			report.report("Qcis "+priorityTestFirstQcis+" average DL after running qcis "+priorityTestSecondQcis+" is at least 90%");
		}
		GeneralUtils.stopLevel();
	}
	
	protected void syncGeneralCommands() {
		commandList.add("ue show qos");
		commandList.add("volte show link");
		commandList.add("volte show rate");
		commandList.add("pdcp show ulstats");
		commandList.add("pdcp show dlstats");
		super.syncGeneralCommands();
	}
	
	@Override
	protected boolean startTrafficInTest() throws Exception{
	
		resetTestBol = false;
		trafficSTC.setactiveStreamsArray(stringArrayStreamNames);
		
		GeneralUtils.startLevel("Turning off ues in setup");
		peripheralsConfig.stopUEsOnlySnmp(SetupUtils.getInstance().getAllUEs());
		GeneralUtils.stopLevel();
		
		GeneralUtils.startLevel("Turning on ues in primary UE list");
		peripheralsConfig.startUEsOnlySnmp(ueList);
		GeneralUtils.stopLevel();
		
		GeneralUtils.startLevel("Disable un-needed UEs");
		disableUEsNotInTest(ueNameListStc,SetupUtils.getInstance().getAllUEs());
		GeneralUtils.unSafeSleep(1000 * 5);
		GeneralUtils.stopLevel();
		
		
		if(!peripheralsConfig.checkIfAllUEsAreConnectedToNode(ueList, dut)){
			report.report("test will go on with a warning about UES",Reporter.WARNING);
			reason = "UE not connected to DUT";
		}			

		syncGeneralCommands();
		
		if(!checkAllUesAreVolteOrEmergency(ueNameListStc.size())){
			resetUEs = true;
			return false;
		}
		
		
		// init and start traffic
		if(!trafficSTC.initTrafficWithNoStartTraffic(TrafficCapacity.FULLTPT)){
			report.report("Start traffic failed",Reporter.FAIL);
			throw new Exception("Start traffic failed");
		}
		
		if(!trafficSTC.enableExtraCounter(CounterUnit.RFC_4689_ABSOLUTE_AVG_JITTER)){
			report.report("Failed to enable jitter counters",Reporter.WARNING);
			enableJitterSuccess = false;
		}else{
			report.report("Succeeded to enable jitter counters");
			counters.add(CounterUnit.RFC_4689_ABSOLUTE_AVG_JITTER);
			headLines.add(CounterUnit.RFC_4689_ABSOLUTE_AVG_JITTER.value+"[ms]");
			counters.add(CounterUnit.AVG_JITTER);
			headLines.add(CounterUnit.AVG_JITTER.value+"[ms]");
			enableJitterSuccess = true;
		}
		
		GeneralUtils.startLevel("Disable un-needed streams");
		trafficSTC.disableUnneededStreams(this.protocol,ueNameListStc, qci);
		// if the last ue deleted and there is nothing to check!
		if (ueNameListStc.size() == 0) {
			report.report("No UEs in Test");
			GeneralUtils.stopLevel();
			throw new Exception("No UES in Test");
		}
		GeneralUtils.stopLevel();
					
		GeneralUtils.startLevel("Setting load per stream");
		setLoadPerStreamTestVolte();
		GeneralUtils.stopLevel();				
			
		// init Protocol
		GeneralUtils.startLevel("init Protocol ("+this.protocol+")");
		if(!trafficSTC.initProtocol(this.protocol)){
			reason = "Cant set traffic protocol.";
			report.report("Cant start traffic. protocol not supported.", Reporter.WARNING);
			GeneralUtils.stopLevel();
			return false;
		}
		GeneralUtils.stopLevel();
			
		// Save Configurate File and ReStarting Traffic
		report.report("Save Config File and Start Traffic");
		uploadConfigFileToReport();
		trafficSTC.saveConfigFileAndStart();
		
		if(isSamplingTest){
			report.report("Starting TPT-"+streamsMode+" with "+ueNameListStc.size() +" UEs, for "+SoftwareUtiles.milisToFormat(TEST_TIME_MILLIS));
			GeneralUtils.startLevel("Sampling Results each second");
		}
		// loop for time of TEST_TIME
		report.report("waiting 20 seconds for traffic to stabilize");
		
		GeneralUtils.unSafeSleep(1000 * 20);
		removeAllCommandsFromWatchDog();
		//DLULwd = new commandWatchDLAndUL(dut);
		//wd.addCommand(DLULwd);		
		if(isSamplingTest){
			wdActiveUesQci1 = new CommandWatchActiveUEsNmsTable(dut,ueNameListStc.size(),'1',true);
			wd.addCommand(wdActiveUesQci1);		
			wdUeLinkStatus = new CommandWatchUeLinkStatusVolteOrEmergency(dut,ueNameListStc.size(),null,isEmergencyCallTest);
			wd.addCommand(wdUeLinkStatus);
		}
		return true;
	}
	
	private boolean checkAllUesAreVolteOrEmergency(int numOfUes){
		boolean action = true;
		GeneralUtils.startLevel("Check if all UEs are connected with "+(isEmergencyCallTest?"Emergency":"VoLTE")+" flag");
		Boolean number = null;
		if(!isEmergencyCallTest){
			number = dut.getNumberOfUELinkStatusVolte();
		}else{
			number = dut.getNumberOfUELinkStatusEmergency();
		}
		if(number == null){
			report.report("Version does not support this MIB or UEs still not connected");				
		}else{
			if(number){
				report.report("Number of UEs connected with "+(isEmergencyCallTest?"Emergency":"VoLTE")+" is "+number+" instead of "+numOfUes, Reporter.FAIL);
				action = false;
			}else{
				report.report("Number of UEs connected with "+(isEmergencyCallTest?"Emergency":"VoLTE")+" is "+number+" - as expected");
			}			
		}
		GeneralUtils.stopLevel();
		return action;
	}
	
	private String getRntiUeInRejectTest(){
		HashMap <String,Variable> result = dut.getUEShowLinkTable();
		if(!result.isEmpty()){
			for (String key : result.keySet()) {
				String[] rntis = key.split("\\.");
				if(rntis.length == 0){
					return null;
				}
				String rnti = rntis[rntis.length-1];
				return rnti;
		    }
		}
		return null;
	}
	
	private void enableAditionalUE() {
		ArrayList<String> ues = new ArrayList<>();
		ArrayList<Character> qci1 = new ArrayList<Character>();
		
		qci1.add('1');
		qci1.add('5');
		ues.add(ueNameListStc.get(1));
		
		try {
			trafficSTC.enableStreamsByNameAndQciAndDirection(Protocol.UDP,ues, qci1, TransmitDirection.BOTH);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void printPerStreamTables(ArrayList<ArrayList<StreamParams>> listOfStreamList) {
		StreamList TablePrinter = new StreamList();
		ArrayList<String> headLines = new ArrayList<String>();
		headLines.add("L1bitRate[Mbit/s]");
		for (ArrayList<StreamParams> streams : listOfStreamList) {
			for (StreamParams stream : streams) {
				ArrayList<String> valuesList = new ArrayList<String>();
				valuesList.add(longToString3DigitFormat(stream.getRxRate()));
				String dateFormat = GeneralUtils.timeFormat(stream.getTimeStamp());
				TablePrinter.addValues(stream.getName(), dateFormat, headLines, valuesList);
			}
		}
		for (StreamParams stream : listOfStreamList.get(listOfStreamList.size()-1)) {
			report.reportHtml(stream.getName(), TablePrinter.printTablesHtmlForStream(stream.getName()), true);
		}
	}
	
	protected void checkTptOverThreshold(StreamList debugPrinter, ArrayList<ArrayList<StreamParams>> listOfStreamList2,
			double passCriteria, boolean priorityTest) {
		ArrayList<Long> listUlAndDlQciInList = new ArrayList<Long>();
		listUlAndDlQciInList = getUlDlResultsFromList(listOfStreamList2, true);
		ArrayList<Long> listUlAndDlDataQcis = new ArrayList<Long>();
		if(udpDataLoad != 0){
			listUlAndDlDataQcis = getUlDlResultsFromList(listOfStreamList2, false);			
		}else{
			listUlAndDlDataQcis.add(0L);
			listUlAndDlDataQcis.add(0L);
		}
		compareResults(debugPrinter, listUlAndDlQciInList.get(0), listUlAndDlQciInList.get(1), listUlAndDlDataQcis.get(0), listUlAndDlDataQcis.get(1), passCriteria, priorityTest);
	}
	
	protected ArrayList<Long> getUlDlResultsFromList(ArrayList<ArrayList<StreamParams>> listOfStreamList2, boolean isQciInList) {
		ArrayList<Long> listOfTotalRx = new ArrayList<Long>();
		Long ULrxTotal = 0L;
		Long DlrxTotal = 0L;
		boolean qciToSum = true;
		for (ArrayList<StreamParams> list : listOfStreamList2) {
			for (StreamParams stream : list) {
				String endsWith = stream.getName().substring(stream.getName().length()-1);
				qciToSum = qcisInList.contains(endsWith);
				if(isQciInList == qciToSum){
					if (stream.getName().contains("UL")) {
						ULrxTotal += stream.getRxRate();
					}
					if (stream.getName().contains("DL")) {
						DlrxTotal += stream.getRxRate();
					}					
				}
			}
		}
		listOfTotalRx.add(ULrxTotal);
		listOfTotalRx.add(DlrxTotal);
		return listOfTotalRx;
	}
	
	protected void compareResults(StreamList debugPrinter, Long uLrxTotalQcisInList, Long dlrxTotalQcisInList,
			Long uLrxTotalDataQcis, Long dlrxTotalDataQcis, double passCriteria, boolean priorityTest){

		int numberOfCells = enbConfig.getNumberOfActiveCells(dut);
		String numberOfCellsStr = "1";
		numberOfCellsStr = String.valueOf(numberOfCells);
		report.report("number of cells: " + numberOfCellsStr);

		verifyThreshold(passCriteria, uLrxTotalQcisInList, dlrxTotalQcisInList, stringQcisInList(), 
				dlCriteriaQcisInList, ulCriteriaQcisInList, ((priorityTest)?secondResultSizeOfStreams:listOfStreamList.size()),true);
		
		if(udpDataLoad!=0){
			verifyThreshold(DATA_QCIS_THRESHOLD_PERCENT, uLrxTotalDataQcis, dlrxTotalDataQcis, "data qcis",
					dlCriteriaDataQcis, ulCriteriaDataQcis, listOfStreamList.size(),false);
		}
	}
	
	protected String stringQcisInList(){
		String ret = "qci"+(qcisInList.size()>1?"s ":" ");
		for(String qci : qcisInList){
			ret+=qci+", ";
		}
		ret = ret.substring(0, ret.length() - 2);
		return ret;
	}
	
	private void verifyThreshold(double passCriteria, double uLrxTotal, double dlrxTotal, String qciInfo,
			double dlCriteria, double ulCriteria, int sizeOfStreamList, boolean isQci1){
		report.report("Threshold for "+qciInfo+": " + passCriteria * 100 + "%");
		
		int divide = isQci1?8:1;
		double ul = 0;
		double dl = 0;
		
		ul = uLrxTotal/1000000.0/sizeOfStreamList;
		dl = dlrxTotal/1000000.0/sizeOfStreamList;			
		
		String dlRateToPrint = convertTo3DigitsAfterPoint(dl);
		String dlCriteriaToPrint = convertTo3DigitsAfterPoint(passCriteria*dlCriteria/divide);
		
		String ulRateToPrint = convertTo3DigitsAfterPoint(ul);
		String ulCriteriaToPrint = convertTo3DigitsAfterPoint(passCriteria*ulCriteria/divide);
		
		if(dl<passCriteria*dlCriteria/divide){
			report.report("DL rx rate for "+qciInfo+" does not match the threshold! DL rate: " + dlRateToPrint +" Mbps, instead of: "
					+ dlCriteriaToPrint, Reporter.FAIL);
			reason = "DL rx rate for "+qciInfo+" does not match the threshold! DL rate: " + dlRateToPrint +" Mbps, instead of: "
					+ dlCriteriaToPrint;
		}else{
			report.report("RxRate for "+qciInfo+" is ok - Average downlink: " + dlRateToPrint +" Mbps, expected: " 
					+ dlCriteriaToPrint);		
		}
		
		if(ul<passCriteria*ulCriteria/divide){
			report.report("UL rx rate for "+qciInfo+" does not match the threshold! UL rate: " + ulRateToPrint +" Mbps, instead of: "
					+ ulCriteriaToPrint, Reporter.FAIL);
			reason = "UL rx rate for "+qciInfo+" does not match the threshold! UL rate: " + ulRateToPrint +" Mbps, instead of: "
					+ ulCriteriaToPrint;
		}else{
			report.report("RxRate for "+qciInfo+" is ok - Average uplink: " + ulRateToPrint +" Mbps, expected: " 
					+ ulCriteriaToPrint);		
		}
	}
	
	private String convertTo3DigitsAfterPoint(double val){
		NumberFormat formatter = new DecimalFormat("#.###");
		return formatter.format(val);
	}

	private void terminateWatchDogVolte() {
		if (wd == null) {
			return;
		}
		removeAllCommandsFromWatchDog();
		terminateWatchDog();
	}
	
	private void removeAllCommandsFromWatchDog(){
		if(wdActiveUesQci1!=null){
			wd.removeCommand(wdActiveUesQci1);			
		}
		if(wdUeLinkStatus!=null){
			wd.removeCommand(wdUeLinkStatus);			
		}
		if(wdActiveUesQci9!=null){
			wd.removeCommand(wdActiveUesQci9);			
		}
		if(DLULwd != null){
			wd.removeCommand(DLULwd);			
		}
		if(wdUeLinkStatusEmergency!=null){
			wd.removeCommand(wdUeLinkStatusEmergency);
		}
		if(wdUeLinkStatusEmergencyNoVolte!=null){
			wd.removeCommand(wdUeLinkStatusEmergencyNoVolte);
		}
	}
	
	private void validateActiveFlag(boolean active){
		if(active){
			report.report("Number of Qci 1 active UEs in eNodeB according to snmp was "+ueNameListStc.size()+" or more during the whole test");
		}else{
			report.report("Number of Qci 1 active UEs in eNodeB according to snmp was not "+ueNameListStc.size()+" or more during the whole test",Reporter.FAIL);
			reason = "Number of Qci 1 active UEs in eNodeB according to snmp was not "+ueNameListStc.size()+" or more during the whole test";
		}
	}
	
	private void validateUELinkStatus(boolean linked){
		if(linked){
			report.report("Number of UEs with "+ (isEmergencyCallTest?"Emergency":"VoLTE") +" support flag active was "+ueNameListStc.size()+" or more during the whole test");
		}else{
			report.report("Number of UEs with "+ (isEmergencyCallTest?"Emergency":"VoLTE") +" support flag active was not "+ueNameListStc.size()+" or more during the whole test", Reporter.FAIL);
			reason = "Number of UEs with "+ (isEmergencyCallTest?"Emergency":"VoLTE") +" support flag active was not "+ueNameListStc.size()+" or more during the whole test";
		}
	}
	
	protected void sampleAllCounters(){
		streamParamsAllCounters = trafficSTC.getTxAndRxCounter(counters);
	}
	
	protected ArrayList<StreamParams> getTPTCounters(){
		ArrayList<StreamParams> ret = new ArrayList<StreamParams>();
		for(StreamParams str : streamParamsAllCounters){
			if(str.getUnit() == CounterUnit.BITS_PER_SECOND){
				GeneralUtils.printToConsole("StreamName="+str.getName()+" -> txStreamCounter=" + str.getTxRate() +
						" / rxStreamCounter=" + str.getRxRate());
				ret.add(str);
			}
		}
		return ret;
	}
	
	/*private boolean checkForHalt() {
		long thresh = 0;
		for (StreamParams stream : streams) {
			thresh = HALT_STREAM_PARAM;
			if(stream.getName().endsWith("1") || stream.getName().endsWith("5")){
				thresh = HALT_STREAM_PARAM_VOLTE;
			}
			if (stream.getRxRate() < thresh) {
				report.report("Stream Halt on : "+stream.getName());
				return true;
			}
		}
		return false;
	}*/
	
	protected ArrayList<StreamParams> checkStreamsForHalter() {
		ArrayList<StreamParams> haltedStreams = new ArrayList<StreamParams>();
		long thresh = 0;
		for (StreamParams stream : streams) {
			thresh = HALT_STREAM_PARAM;
			if(stream.getName().endsWith("1") || stream.getName().endsWith("5")){
				thresh = HALT_STREAM_PARAM_VOLTE;
			}
			if (stream.getRxRate() < thresh) {
				haltedStreams.add(stream);
			}
		}
		return haltedStreams;
	}
	
	@Override
	protected void resetParams() {
		super.resetParams();
		enableJitterSuccess = false;
		streamsSum = new HashMap<String,Double>();
		streamsCounter = new HashMap<String,Integer>();
		
		counters = new ArrayList<CounterUnit>();
		counters.add(CounterUnit.BITS_PER_SECOND);
		counters.add(CounterUnit.SHORT_TERM_AVG_LATENCY);
		counters.add(CounterUnit.DROPPED_FRAME_PERCENT);
		counters.add(CounterUnit.DROPPED_FRAME_PERCENT_RATE);			
					
		headLines = new ArrayList<String>();
		headLines.add(CounterUnit.SHORT_TERM_AVG_LATENCY.value+"[ms]");
		headLines.add(CounterUnit.DROPPED_FRAME_PERCENT.value);
		headLines.add(CounterUnit.DROPPED_FRAME_PERCENT_RATE.value);
		
		streamListCounters = new StreamList();
		allStreams = new HashSet<String>();
	}
	
	@Override
	public void sampleResultsStatusOk() {
		restartTime = false;
		//ArrayList<StreamParams> haltedStreams = null;
		ArrayList<StreamParams> sampleArrayList = null;
		try {
			sampleAllCounters();
			sampleArrayList = getTPTCounters();
			streams.addAll(sampleArrayList);
		}catch(Exception e){
		}
			/*GeneralUtils.startLevel("Checking for HaltStream");
			if (checkForHalt()) {
				streams = null;
				streams = new ArrayList<StreamParams>();
				report.report("Found halt streams", Reporter.WARNING);
				report.report("Last sample was 0 - Re Sample one more time");
				sampleAllCounters();
				sampleArrayList = getTPTCounters();
				streams.addAll(sampleArrayList);
				if (checkForHalt()) {
					report.report("Found halt streams", Reporter.WARNING);
					report.report("Waiting 20 seconds");
					GeneralUtils.unSafeSleep(20 * 1000);

					streams = null;
					streams = new ArrayList<StreamParams>();
					report.report("Last sample was 0 - Re sample one more time");
					sampleAllCounters();
					sampleArrayList = getTPTCounters();
					streams.addAll(sampleArrayList);
					//restartTime = true;

					if (checkForHalt()) {

						haltedStreams = checkStreamsForHalter();
						reportForHalted(haltedStreams);
						report.report("Those Streams Are in Halt Status: " + printStreamName(haltedStreams));

						//number of halted UEs is grater than Test Threshold.
						double totalUESPercent = ueList.size() * PRECENT_OF_UE_RESET / 100;
						boolean uesThreashold = getNumberOfUEs(haltedStreams) <= totalUESPercent ? true : false;

						if (uesThreashold) {
							report.report("disabling halted Streams since they are less than 30%.");
								// Total UEs are over than 3 OR if Test take off less than 2 UEs.
							GeneralUtils.printToConsole("Total number of UEs in Setup : "+ueList.size());
							GeneralUtils.printToConsole("Number of UEs in Test Currently : "+ueNameListStc.size());
							if( ueNameListStc.size() > 3  && ueList.size() - ueNameListStc.size() < 2) { 	
								checkUEsRelationsWithStreams(haltedStreams);
							}else{
								report.report("Test UEs number is not enough For Throughtput! - Failing Test",Reporter.FAIL);	
								exceptionThrown = true;
								return;
							}
						}else{
							resetUEs = true;
						}
						removeAllCommandsFromWatchDog();			
						
						resetDueToMultiHaltStreams = true;
					}else{
						getCounters();
						report.report("No halt stream in third try");
						startingTestTime+=20*1000;
					}
				}else{
					getCounters();
					report.report("No halt stream in second try");
				}
			} else {
				getCounters();
				report.report("No halt stream");
			}

		} catch (Exception e) {
			report.report("Interrupted in SamepleResultsStatusOk method in Traffic Class - " + e.getMessage());
			e.printStackTrace();
			report.report("Error during test", Reporter.FAIL);
			resetDueToMultiHaltStreams = true;
			exceptionThrown = true;
		} finally {
			GeneralUtils.stopLevel();
		}*/
		listOfStreamList.add(sampleArrayList);
		streamsForPriorityTest.add(sampleArrayList);
		streams = null;
		streams = new ArrayList<StreamParams>();
	}

	private void setLoadPerStreamTestAditionalUE(){		
		ArrayList<LoadParam> load = new ArrayList<LoadParam>();
		
		String name = ueNameListStc.get(1);
		int ueNum = Integer.valueOf(name.replaceAll("\\D+", "").trim());
		double loadFps = (packetSize+20)*8*50/1000.0;
		report.report(name+": setting DL and UL for stream with qci 1: 50 fps with frame size of "+packetSize);
		report.report("Equivalent to: ("+packetSize+"+20)*8*50/1000 = "+loadFps+" kbps");
		LoadParam lp = new LoadParam(loadFps, '1', ueNum, LoadParam.LoadUnit.KILOBITS_PER_SECOND,packetSize);
		load.add(lp);
		report.report(name+": setting DL and UL for stream with qci 5: 20 kbps");
		LoadParam lp1 = new LoadParam(20, '5', ueNum, LoadParam.LoadUnit.KILOBITS_PER_SECOND,packetSize);
		load.add(lp1);
		dlCriteriaQcisInList*=1.5;
		ulCriteriaQcisInList*=1.5;
		
		trafficSTC.setLoadStreamDL(load);	
		trafficSTC.setLoadStreamUL(load);
		try{
			trafficSTC.setFrameSizeStreamDL(load);
			trafficSTC.setFrameSizeStreamUL(load);			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private boolean sampleRejectTest(boolean turnOnUE,boolean closeLevel){
		boolean turnOn = turnOnUE;
		while (System.currentTimeMillis() - startingTestTime <= TEST_TIME_MILLIS) {
			if(turnOn){
				if(System.currentTimeMillis() - startingTestTime >= TEST_TIME_MILLIS - 10000){
					ueList = getUES(dut,2);
					ueNameListStc = convertUeToNamesList(ueList);
					newUe = ueList.get(1);
					newUe.start();
					turnOn = false;
				}
			}
			sampleResultsStatusOk();
	
			if (restartTime) {
				startingTestTime = System.currentTimeMillis();
			}

			// if exception thrown = Connection closed -> stop test!
			if (exceptionThrown) {
				if(reason == ""){
					reason = "TRAFFIC ERROR";
				}
				
				report.report("Performance Error in Test! ,reason: "+reason);
				GeneralUtils.stopLevel();
				testIsNotDoneStatus = false;
				return false;
			}
			try {
				synchronized (this) {
					this.wait(1000);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			if(resetUEs){
				resetUEs = false;
				resetTestBol = true;
				report.report("Resetting UES");
				if(ueList.get(0) instanceof VirtualUE){
					UESimulator uesim = UESimulator.getInstance();
					uesim.stop();
				}else{
					rebootUEs(ueList);
				}
			}
				
			if (resetDueToMultiHaltStreams) {
				// more than 30% of the streams are in halt mode!
				resetDueToMultiHaltStreams = false;
				report.report("Reseting Time stamp for test");
				startingTestTime = System.currentTimeMillis();
				resetTestBol = true;
				if (numberOfResets < 3) {
					GeneralUtils.stopLevel();
					return false;
				} else {
					resetTestBol = false;
					startingTestTime = TEST_TIME_MILLIS;
					GeneralUtils.stopLevel();
					testIsNotDoneStatus = false;
					return false;
				}
			}
		}
		testIsNotDoneStatus = false;
		if(closeLevel){
			GeneralUtils.stopLevel();
		}
		return true;
	}
	
	protected boolean startTrafficAndSampleRejectTest() throws Exception{
		ueList = getUES(dut,1);
		ueNameListStc = convertUeToNamesList(ueList);
		if(!startTrafficInTest()){
			return false;
		}
		String rnti = getRntiUeInRejectTest();
		removeAllCommandsFromWatchDog();				
		wdActiveUesQci1 = new CommandWatchActiveUEsNmsTable(dut,1,'1',false);
		wd.addCommand(wdActiveUesQci1);
		wdUeLinkStatus = new CommandWatchUeLinkStatusVolteOrEmergency(dut,ueNameListStc.size(),rnti,isEmergencyCallTest);
		wd.addCommand(wdUeLinkStatus);
		startingTestTime = System.currentTimeMillis();
		newUe = null;
		if(!sampleRejectTest(true,false)){
			return true;
		}
		if(!peripheralsConfig.checkSingleUEConnectionToNode(newUe, dut)){
			report.report("test will go on with a warning about UES",Reporter.WARNING);
			reason = "UE not connected to DUT";
		}
		enableAditionalUE();
		setLoadPerStreamTestAditionalUE();
		trafficSTC.saveConfigFileAndStart();
		report.report("Wait 10 seconds for traffic of second UE to stabilize");
		GeneralUtils.unSafeSleep(10*1000);
		if(wdActiveUesQci9!=null){
			wd.removeCommand(wdActiveUesQci9);							
		}
		wdActiveUesQci9 = new CommandWatchActiveUEsNmsTable(dut,1,'9',true);
		wd.addCommand(wdActiveUesQci9);	
		startingTestTime = System.currentTimeMillis();
		sampleRejectTest(false,true);
		return true;
	}
	
	protected void TestProcessRejectTest() {
		while (testIsNotDoneStatus) {
			resetParams();
			try {
				if(!startTrafficAndSampleRejectTest()){
					testIsNotDoneStatus = false;
					resetTestBol = false;
					exceptionThrown = true;
					GeneralUtils.stopAllLevels();					
				}
			} catch (Exception e) {
				report.report("Stopping Parallel Commands From Java Exception");
				testIsNotDoneStatus = false;
				resetTestBol = false;
				exceptionThrown = true;
				reason = "network connection Error";
				report.report(e.getMessage() + " caused Test to stop", Reporter.FAIL);
				e.printStackTrace();
				GeneralUtils.stopAllLevels(); 
			}
			if (resetTestBol) {
				numberOfResets++;
				if(syncCommands != null){
					report.report("Stopping Parallel Commands");
					syncCommands.stopCommands();
				}
				makeThreadObjectFinish();
				if(syncCommands != null){
					report.report("Commands File Path: " + syncCommands.getFile());					
				}
				testIsNotDoneStatus = true;
				try {
					if(syncCommands != null){
						syncCommands.moveFileToReporterAndAddLink();
					}
					report.report("Stopping Traffic");
					trafficSTC.stopTraffic();
					report.report("Resetting test", Reporter.WARNING);
					reason = "reset Test because of stream halt";
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		if (numberOfResets >= 3) {
			// using this flag in order to NOT print results.
			testIsNotDoneStatus = false;
			printResultsForTest = false;
			report.report("Test Was Reseted Too many times because of more then " + PRECENT_OF_UE_RESET + "%"
					+ " Of the Streams are in Halt - check Setup", Reporter.FAIL);
			reason = "Test Was Restarted Too many Times due to Traffic halt";
		}
		try {
			syncCommands.stopCommands();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		makeThreadObjectFinish();
		try {
			syncCommands.moveFileToReporterAndAddLink();
		} catch (Exception e) {
			report.report("Exception in ReporterHelper.copyFileToReporterAndAddLink could not attach Command File");
			e.printStackTrace();
		}
	}
	
	private void afterTestVolteRejectTest(double passCriteria) {
		boolean flagActive1 = false;
		if(wdActiveUesQci1 != null){
			flagActive1 = wdActiveUesQci1.getFlagActive();
		}
		boolean flagUeLinkStatus = false;
		if(wdUeLinkStatus != null){
			flagUeLinkStatus = wdUeLinkStatus.getFlagActive();			
		}
		boolean flagActive9 = false;
		if(wdActiveUesQci9!=null){
			flagActive9 = wdActiveUesQci9.getFlagActive();			
		}
		if(DLULwd != null){
			printDLandULAverageTableAndCounters();			
		}
		terminateWatchDogVolte();
		
		report.report("Stop traffic");
		trafficSTC.stopTraffic();

		if (exceptionThrown) {
			report.report("Exception failed the test - No Results");
		} else {
			sumAndPrintTablesPerPort();
			printPerStreamTables(listOfStreamList);

			GeneralUtils.printToConsole("Print Results state: "+printResultsForTest);
			if (printResultsForTest) {
				checkTptOverThreshold(debugPrinter, listOfStreamList, passCriteria, false);
				if(flagActive1){
					report.report("Number of Qci 1 active UEs in NMS status table was 1 during the whole test");
				}else{
					report.report("Number of Qci 1 active UEs in NMS status table was not 1 during the whole test",Reporter.FAIL);
					reason = "Number of Qci 1 active UEs in NMS status table was not 1 during the whole test";
				}
				if(flagActive9){
					report.report("Number of Qci 9 active UEs in NMS status table was 1 or more during the whole test");
				}else{
					report.report("Number of Qci 9 active UEs in NMS status table was not 1 or more during the whole test",Reporter.FAIL);
					reason = "Number of Qci 9 active UEs in NMS status table was not 1 or more during the whole test";
				}
				if(flagUeLinkStatus){
					report.report("Only first UE connected to enodeb had volte flag enabled");
				}else{
					report.report("First UE connected to enodeb had not volte flag enabled, or other UE had flag enabled",Reporter.FAIL);
					reason = "First UE connected to enodeb had not volte flag enabled, or other UE had flag enabled";
				}
				printDataOfUes();
			}
		}
	}

	protected void TestProcessEmergencyCallPrioritized() {
		while (testIsNotDoneStatus) {
			resetParams();
			try {
				if(!startTrafficAndSampleEmergencyCallPrioritized()){
					testIsNotDoneStatus = false;
					resetTestBol = false;
					exceptionThrown = true;
					GeneralUtils.stopAllLevels();					
				}
			} catch (Exception e) {
				report.report("Stopping Parallel Commands From Java Exception");
				testIsNotDoneStatus = false;
				resetTestBol = false;
				exceptionThrown = true;
				reason = "network connection Error";
				report.report(e.getMessage() + " caused Test to stop", Reporter.FAIL);
				e.printStackTrace();
				GeneralUtils.stopAllLevels(); 
			}
			if (resetTestBol) {
				numberOfResets++;
				if(syncCommands != null){
					report.report("Stopping Parallel Commands");
					syncCommands.stopCommands();
				}
				makeThreadObjectFinish();
				if(syncCommands != null){
					report.report("Commands File Path: " + syncCommands.getFile());					
				}
				testIsNotDoneStatus = true;
				try {
					if(syncCommands != null){
						syncCommands.moveFileToReporterAndAddLink();
					}
					report.report("Stopping Traffic");
					trafficSTC.stopTraffic();
					report.report("Resetting test", Reporter.WARNING);
					reason = "reset Test because of stream halt";
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		if (numberOfResets >= 3) {
			// using this flag in order to NOT print results.
			testIsNotDoneStatus = false;
			printResultsForTest = false;
			report.report("Test Was Reseted Too many times because of more then " + PRECENT_OF_UE_RESET + "%"
					+ " Of the Streams are in Halt - check Setup", Reporter.FAIL);
			reason = "Test Was Restarted Too many Times due to Traffic halt";
		}
		try {
			syncCommands.stopCommands();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		makeThreadObjectFinish();
		try {
			syncCommands.moveFileToReporterAndAddLink();
		} catch (Exception e) {
			report.report("Exception in ReporterHelper.copyFileToReporterAndAddLink could not attach Command File");
			e.printStackTrace();
		}
	}
	
	protected boolean startTrafficAndSampleEmergencyCallPrioritized() throws Exception{
		ueList = new ArrayList<UE>();
		ueList.add(ueListEmergency.get(0));
		ueNameListStc = convertUeToNamesList(ueList);
		if(!startTrafficInTest()){
			return false;
		}
		String rnti = getRntiUeInRejectTest();
		removeAllCommandsFromWatchDog();				
		wdActiveUesQci1 = new CommandWatchActiveUEsNmsTable(dut,1,'1',false);
		wd.addCommand(wdActiveUesQci1);
		wdUeLinkStatus = new CommandWatchUeLinkStatusVolteOrEmergency(dut,ueNameListStc.size(),rnti,false);
		wd.addCommand(wdUeLinkStatus);
		startingTestTime = System.currentTimeMillis();
		newUe = null;
		if(!sampleRejectTest(false,false)){
			return true;
		}
		wd.removeCommand(wdUeLinkStatus);
		qci1DataBeforeECue = wdActiveUesQci1.getFlagActive();
		wd.removeCommand(wdActiveUesQci1);
		ueList = ueListEmergency;
		ueNameListStc = convertUeToNamesList(ueList);
		try{
			newUe = ueList.get(1);
		}catch(Exception e){
			report.report("Only 1 UE configured in setup");
			e.printStackTrace();
			return false;
		}
		UeAction ueAction = new UeAction();
		ueAction.setUEs(newUe.getName());
		ueAction.setApnName(apnEmergencyCall);
		ueAction.setAPN();
		newUe.start();
		GeneralUtils.unSafeSleep(10*1000);
		if(!peripheralsConfig.checkSingleUEConnectionToNode(newUe, dut)){
			report.report("test will go on with a warning about UES",Reporter.WARNING);
			reason = "UE not connected to DUT";
		}
		
		enableAditionalUE();
		setLoadPerStreamTestAditionalUE();
		trafficSTC.saveConfigFileAndStart();
		report.report("Wait 10 seconds for traffic of second UE to stabilize");
		GeneralUtils.unSafeSleep(10*1000);
		if(wdActiveUesQci9!=null){
			wd.removeCommand(wdActiveUesQci9);							
		}
		wdActiveUesQci9 = new CommandWatchActiveUEsNmsTable(dut,1,'9',true);
		wd.addCommand(wdActiveUesQci9);	
		if(wdUeLinkStatusEmergencyNoVolte!=null){
			wd.removeCommand(wdUeLinkStatusEmergencyNoVolte);							
		}
		wdUeLinkStatusEmergencyNoVolte = new CommandWatchUeLinkStatusVolteOrEmergency(dut, ueNameListStc.size(), "", false);
		wd.addCommand(wdUeLinkStatusEmergencyNoVolte);
		if(wdUeLinkStatusEmergency!=null){
			wd.removeCommand(wdUeLinkStatusEmergency);							
		}
		String rntiEmergency = getRntiUeInEmergencyTest();
		wdUeLinkStatusEmergency = new CommandWatchUeLinkStatusVolteOrEmergency(dut, ueNameListStc.size(), rntiEmergency, true);
		wd.addCommand(wdUeLinkStatusEmergency);
		wdActiveUesQci1 = new CommandWatchActiveUEsNmsTable(dut,1,'1',false);
		wd.addCommand(wdActiveUesQci1);
		startingTestTime = System.currentTimeMillis();
		sampleRejectTest(false,true);
		return true;
	}
	
	private String getRntiUeInEmergencyTest() {
		HashMap<String, Integer> result = null;
		result = dut.getUELinkStatusEmergencyTable();				
		
		if(!result.isEmpty()){
			for (String key : result.keySet()) {
				String[] rntis = key.split("\\.");
				String rnti = rntis[rntis.length-1];
				if(result.get(key)==1){
					return rnti;
				}
		    }
		}
		return null;
	}

	public int getRuntime() {
		return runtime;
	}

	@ParameterProperties(description = "Number of minutes the test will run")
	public void setRuntime(int runtime) {
		this.runtime = runtime;
	}

	public int getTotalUEsPerCell() {
		return totalUEsPerCell;
	}

	@ParameterProperties(description = "Number of UEs in test. Leave 0 for all UEs")
	public void setTotalUEsPerCell(int totalUEsPerCell) {
		this.totalUEsPerCell = totalUEsPerCell;
	}

	public int getUdpDataLoad() {
		return udpDataLoad;
	}

	public void setUdpDataLoad(int udpDataLoad) {
		this.udpDataLoad = udpDataLoad;
	}

	public int getQci1_5PacketSize() {
		return qci1_5PacketSize;
	}

	public void setQci1_5PacketSize(int qci1_5PacketSize) {
		this.qci1_5PacketSize = qci1_5PacketSize;
	}

	public int getQci1LoadInFps() {
		return qci1LoadInFps;
	}

	public void setQci1LoadInFps(int qci1LoadInFps) {
		this.qci1LoadInFps = qci1LoadInFps;
	}

	public int getQci5LoadInKbps() {
		return qci5LoadInKbps;
	}

	public void setQci5LoadInKbps(int qci5LoadInKbps) {
		this.qci5LoadInKbps = qci5LoadInKbps;
	}
	
	@Override
	public void end(){
		GeneralUtils.startLevel("After test");
		setENBsToService();
		setAllUEsToStart();
		GeneralUtils.stopLevel();
		super.end();
	}
}

package testsNG.PerformanceAndQos.Fairness;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import EnodeB.EnodeB;
import Netspan.API.Enums.EnbStates;
import Netspan.Profiles.RadioParameters;
import UE.UESimulator;
import Utils.GeneralUtils;
import Utils.Pair;
import Utils.SetupUtils;
import Utils.StreamList;
import Utils.SysObjUtils;
import jsystem.framework.ParameterProperties;
import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;
import jsystem.framework.report.Reporter.ReportAttribute;
import testsNG.TestspanTest;
import testsNG.Actions.EnodeBConfig;
import testsNG.Actions.PeripheralsConfig;
import testsNG.Actions.Traffic;
import testsNG.Actions.Utils.CalculatorMap;
import testsNG.Actions.Utils.ParallelCommandsThread;
import Entities.ITrafficGenerator.CounterUnit;
import Entities.ITrafficGenerator.Protocol;
import Entities.ITrafficGenerator.TransmitDirection;
import Entities.LoadParam;
import Entities.StreamParams;

public class P0 extends TestspanTest {
	private EnodeB dut = null;
	private UESimulator uesim = null;
	private PeripheralsConfig peripheralsConfig = null;
	private EnodeBConfig enodeBConfig = null;
	private Traffic traffic = null;
	private ParallelCommandsThread commandsThread = null;
	private CalculatorMap calculator = null;
	
	private ArrayList<CounterUnit> counters;
	private StreamList streamList = null;
	private Set<String> allStreams = null;
	private ArrayList<Character> qci = new ArrayList<Character>();
	private String DL_UL = null;
	private double dl = 0;
	private double ul = 0;
	private boolean getCalc;

	private final int SAMPLES = 10;
	private final int NUM_OF_RETRIES = 2;
	private final double THRESHOLD_PERCENT = 0.9;
	private final double HALT_THRESHOLD = 0.001;
	private final int PERCENTAGE_OF_FAIL = 10;
	private final double QCI_GBR_CRITERIA_THRESHOLD = 0.75;
	private final long TIME_TO_RECOVER = 4*60*1000;
	
	private double thresholdSteps;
	private double portLoadPercent;
	
	private int volt;
	private int numOfFailsBoundariesUe;
	private int checkRatio;
	private int checkTPTAll;
	private int checkTPTGbr;
	private int numOfUes;
	
	private double firstQciTPT;
	private int firstQciUes;
	private int firstQciWeight;

	private double secondQciTPT;
	private int secondQciUes;
	private int secondQciWeight;
	
	private boolean TPTGbr;
	private boolean qciGbr;	
	private boolean mixTwiceAmbrOrTwiceGbr;
	private int retry;
	private int numOfVolte;
	private boolean volte;
	private int checkNumVolte;
	ArrayList<String> commands;
	private boolean parallelHasStarted;
	
	private boolean tcpLogger;
	private double[] devs;
	
	private int devSample;
	
	@Override
	public void init() throws Exception {
		report.startLevel("Init");
		enbInTest = new ArrayList<EnodeB>();
		enbInTest.add(dut);
		super.init();
		peripheralsConfig = PeripheralsConfig.getInstance();
		for (EnodeB enb : enbInTest) {
			if(enb != dut){
				if(peripheralsConfig.changeEnbState(enb, EnbStates.OUT_OF_SERVICE)){
					report.report(enb.getNetspanName() + " is out of service");
				}else{
					report.report("Failed to set " + enb.getNetspanName() + " out of service.", Reporter.WARNING);
				}				
			}
		}
		uesim = UESimulator.getInstance();
		traffic = Traffic.getInstance(SetupUtils.getInstance().getAllUEs());
		enodeBConfig = EnodeBConfig.getInstance();
		calculator = new CalculatorMap();
		counters = new ArrayList<CounterUnit>();
		counters.add(CounterUnit.BITS_PER_SECOND);
		volt = dut.getMaxVolteCalls();
		streamList = new StreamList();
		allStreams = new HashSet<String>();
		retry = 1;
		numOfVolte = 0;
		volte = false;
		tcpLogger = false;
		report.stopLevel();
		commands = new ArrayList<>();
		commands.add("ue show link");
		commands.add("ue show ratedl");		
		commands.add("ue show rateul");
		commands.add("qci show rate");
		commands.add("ue show qos");
		commands.add("ue show rate lc=2");
		commands.add("rlc show amdlstats");
		commands.add("db get PTPStatus");
		initParallel();
		dl = 0;
		ul = 0;
		parallelHasStarted = false;
		GeneralUtils.startLevel("Getting information from calculator");
		String toCalc = getStringCalculator();
		if (toCalc != null) {
			String dl_ul = calculator.getPassCriteria(toCalc);
			String[] dlul = {};
			try{
				dlul = dl_ul.split("_");
				dl = Double.parseDouble(dlul[0]);
				ul = Double.parseDouble(dlul[1]);
			}catch(Exception e){
				failedToGetCalc();
				return;
			}
		} else {
			failedToGetCalc();
			return;
		}
		report.report("DL pass criteria: " + dl + " Mbps");
		report.report("UL pass criteria: " + ul + " Mbps");
		getCalc = true;
		GeneralUtils.stopLevel();
	}
	
	private void failedToGetCalc(){
		report.report("Failed to get value from calculator", Reporter.WARNING);
		GeneralUtils.stopLevel();
		reason = "Failed to get value from calculator";
		getCalc = false;
	}
	
	private void preTest(boolean resetDevs, boolean reset,double treshSteps, int num, String du,ArrayList<Character> q,double percent, 
			boolean tptG, boolean mix2AmbrOrGbr, int qci1Weight, int qci2Weight, boolean steps){
		numOfUes = num;
		DL_UL = du;
		qci = q;
		thresholdSteps = treshSteps;
		streamList = new StreamList();
		allStreams = new HashSet<String>();
		portLoadPercent = percent; 

		firstQciWeight = qci1Weight;
		secondQciWeight = qci2Weight;
				
		TPTGbr = tptG;
		mixTwiceAmbrOrTwiceGbr = mix2AmbrOrGbr;
		checkNumVolte = 0;
		
		if(resetDevs){
			int size = q.size();
			if(steps){
				size*=3;
			}
			devs = new double[size];
			for(int i=0;i<size;i++){
				devs[i]=0;
			}			
		}
		
		if(reset){	
			devSample = 0;
			
			numOfFailsBoundariesUe = 0;
			checkRatio = 0;
			checkTPTAll = 0;
			checkTPTGbr = 0;
			
			firstQciTPT = 0;
			firstQciUes = 0;

			secondQciTPT = 0;
			secondQciUes = 0;
		}
	}
	
	private boolean startSimulatorAndTraffic(boolean startTrafficAndSim, boolean stopTrafficAndSim){
		if(!getCalc){
			return false;
		}
		boolean action = false;
		
		if(startTrafficAndSim){								
			
			if(stopTrafficAndSim){
				setTestUESim(numOfUes);				
			}else{
				setTestUESim(numOfUes*2);				
			}
			uesim.start();
			GeneralUtils.startLevel("Starting traffic");
			try {
				action = traffic.startTraffic();
				configurePortLoads(dl*portLoadPercent,ul*portLoadPercent);
				if(TPTGbr){
					disableAllStreamsMixTest();
					enableNeededStreams();				
				}else{
					disableUnneededStreams();
				}					
				GeneralUtils.unSafeSleep(5*1000);
			} catch (Exception e) {
				action = false;
				e.printStackTrace();
			}
			if (!action) {
				report.report("Failed to start traffic", Reporter.WARNING);
				GeneralUtils.stopLevel();
				reason = "Failed to start traffic";
				return false;
			} else {
				report.report("Traffic started successfully");
			}
			commandsThread.start();
			parallelHasStarted = true;
			GeneralUtils.stopLevel();
		}
		
		if(!startTrafficAndSim){
			GeneralUtils.startLevel("Adding more streams (UEs)");
			configurePortLoads(dl*portLoadPercent,ul*portLoadPercent);
			enableNeededStreams();		
			GeneralUtils.stopLevel();
		}
		GeneralUtils.unSafeSleep(1*60*1000);

		return true;
	}
	
	private boolean genericTestOldPfs(int sampleFrom, int sampleTo, boolean startTrafficAndSim, boolean stopTrafficAndSim, int devsFrom){
		dut.setPfsType(PfsType.OldPfs.ordinal());
		if(!startSimulatorAndTraffic(startTrafficAndSim, stopTrafficAndSim)){
			return false;
		}
		GeneralUtils.startLevel("Sampling TPT ePFS type = "+PfsType.OldPfs.ordinal());
		traffic.getTxAndRxCounter(counters);
		for (int i = sampleFrom; i <= sampleTo; i++) {
			GeneralUtils.startLevel("SAMPLE #" + i);
			report.report("PFS type: "+dut.getPfsType());
			int num = dut.getUeNumberToEnb();
			report.report("Number of UEs connected to EnodeB: "+num);
			if(num==0){
				GeneralUtils.stopLevel();
				GeneralUtils.stopLevel();
				report.report("No UEs were connected to EnodeB", Reporter.WARNING);
				return false;
			}
			getDataOldPfs(i, devsFrom);					
			GeneralUtils.stopLevel();				
		}
		GeneralUtils.stopLevel();
		if(stopTrafficAndSim){
			stopTrafficAndSimulator();
			report.report("Parallel commands for old PFS");
			stopParallelAndAttachFile();
		}
		return true;
	}
	
	private void stopParallelAndAttachFile(){
		commandsThread.stopCommands();
		if(parallelHasStarted){
			commandsThread.moveFileToReporterAndAddLink();
			parallelHasStarted = false;
		}
		initParallel();
	}
	
	private ArrayList<StreamParams> filterArrayList(ArrayList<StreamParams> toFilter, String up_down, Character ch){
		ArrayList<StreamParams> ret = new ArrayList<StreamParams>();
		boolean ul_dl = true;
		for(StreamParams stream : toFilter){
			String name = stream.getName();
			if(up_down != null){
				ul_dl = name.contains(up_down);
			}
			if(ul_dl && ch.equals(name.charAt(name.length()-1))){
				ret.add(stream);
			}
		}
		return ret;
	}
	
	private void getDataOldPfs(int sample, int devsFrom) {
		ArrayList<StreamParams> map = null;
		
		double sumQci = 0;
		int numOfUEsPerSampleNotHalt = 0;
		
		map = traffic.getTxAndRxCounterDLOrUL(counters, DL_UL.equals("DL") ? TransmitDirection.DL : TransmitDirection.UL);
		devSample = devsFrom;
		for(Character ch : qci){
			if(!ch.equals('1')){
				sumQci = 0;
				numOfUEsPerSampleNotHalt = 0;
				
				ArrayList<StreamParams> filteredMap = filterArrayList(map,null,ch);
				
				GeneralUtils.printToConsole("Sample #"+sample);
				for(StreamParams str : filteredMap){
					GeneralUtils.printToConsole("Stream: "+str.getName()+", Tx: "+str.getTxRate()+", Rx: "+str.getRxMaxRate());
				}
				for(StreamParams str : filteredMap){
					double rx = 0;
					try{
						long val = str.getRxRate();
						rx = convertTo3DigitsAfterPoint(val/1000000.0);
					}catch(Exception e){
						e.printStackTrace();
					}
					if(rx>=HALT_THRESHOLD){
						sumQci+=rx;
						numOfUEsPerSampleNotHalt++;
					}
				}
				
				double min = GeneralUtils.ERROR_VALUE;
				double max = HALT_THRESHOLD;
				double avg = convertTo3DigitsAfterPoint(sumQci/numOfUEsPerSampleNotHalt);
				double dev = 0;
				if(numOfUEsPerSampleNotHalt!=0){
					for(StreamParams str : filteredMap){
						double rx = 0;
						try{
							long val = str.getRxRate();
							rx = convertTo3DigitsAfterPoint(val/1000000.0);
						}catch(Exception e){
							continue;
						}
						if(rx<HALT_THRESHOLD){
							continue;
						}else{
							if(rx < min || min == GeneralUtils.ERROR_VALUE){
								min = rx;
							}
							if(rx > max){
								max = rx;
							}
							dev += Math.pow(rx-avg,2);
						}
					}
					double devSqrt = 0;
					if(numOfUEsPerSampleNotHalt>1){
						devSqrt = convertTo3DigitsAfterPoint(Math.sqrt(dev/(numOfUEsPerSampleNotHalt-1)));
					}
					devs[devSample] += devSqrt;
					statsToReport("qci: "+ch, min,max,avg,devSqrt,"Mbps",true);
				}else{
					report.report("All streams had TPT lower than threshold (1 Kbs) for QCI "+ch, Reporter.WARNING);
				}
			}
			devSample++;
		}
	}
	
	private boolean genericTestNewPfs(int sampleFrom, int sampleTo, boolean startTrafficAndSim, boolean stopTrafficAndSim, int devsFrom, int divideInSteps){
		dut.setPfsType(PfsType.NewPfs.ordinal());
		if(!startSimulatorAndTraffic(startTrafficAndSim, stopTrafficAndSim)){
			return false;
		}
		
		GeneralUtils.startLevel("Sampling TPT ePFS type = "+PfsType.NewPfs.ordinal());
		traffic.getTxAndRxCounter(counters);
		for (int i = sampleFrom; i <= sampleTo; i++) {
			GeneralUtils.startLevel("SAMPLE #" + i);
			report.report("PFS type: "+dut.getPfsType());
			int num = dut.getUeNumberToEnb();
			report.report("Number of UEs connected to EnodeB: "+num);
			if(num==0){
				GeneralUtils.stopLevel();
				GeneralUtils.stopLevel();
				report.report("No UEs were connected to EnodeB", Reporter.WARNING);
				return false;
			}
			getDataNewPfs(i, devsFrom, divideInSteps);					
			GeneralUtils.stopLevel();				
		}
		GeneralUtils.stopLevel();
		
		printDataOfUes();
		streamList = new StreamList();
		
		if(stopTrafficAndSim){
			stopTrafficAndSimulator();

			if (checkTPTAll>SAMPLES/PERCENTAGE_OF_FAIL) {
				report.report("Test failed due to low TPT", Reporter.FAIL);
				reason = "Test failed due to low TPT";
			}				
			
			if(numOfFailsBoundariesUe>SAMPLES/PERCENTAGE_OF_FAIL){
				report.report("Test failed due to higher standard deviation in new PFS", Reporter.FAIL);
				reason = "Test failed due to higher standard deviation in new PFS";
			}				
			
			if(TPTGbr && !mixTwiceAmbrOrTwiceGbr){
				if(checkTPTGbr>SAMPLES/PERCENTAGE_OF_FAIL){
					report.report("Test failed due to low threshold in GBR TPT", Reporter.FAIL);
					reason = "Test failed due to low threshold in GBR TPT";
				}			
			}
			
			if(mixTwiceAmbrOrTwiceGbr){
				if(checkRatio>SAMPLES/PERCENTAGE_OF_FAIL){
					report.report("Test failed due to wrong ratio between the qcis", Reporter.FAIL);
					reason = "Test failed due to wrong ratio between the qcis";
				}
			}
		}	
		return true;
	}
	
	private void getDataNewPfs(int sample, int devsFrom, int divideInSteps){
		ArrayList<StreamParams> map = null;
		ArrayList<String> headLines = new ArrayList<String>();
		ArrayList<String> values = new ArrayList<String>();
		
		String countRx = "rx" + counters.get(0);
		String countTx = "tx" + counters.get(0);
		headLines.add(countTx+" in Mbps");
		headLines.add(countRx+" in Mbps");
		double sum = 0;
		double sumQci = 0;
		double sumQciGbrTpt = 0;
		int numOfUEsPerSampleNotHalt = 0;
		
		String ts = null;
		numOfVolte = 0;
		ts = GeneralUtils.timeFormat(System.currentTimeMillis());
		map = traffic.getTxAndRxCounterDLOrUL(counters, DL_UL.equals("DL") ? TransmitDirection.DL : TransmitDirection.UL);
		
		int br = 0;
		devSample = devsFrom;
		boolean checkBoundariesUes = true;
		for(Character ch : qci){
			if(ch.equals('4') && TPTGbr && !mixTwiceAmbrOrTwiceGbr){
				qciGbr = true;
			}else{
				qciGbr = false;
			}
			if(ch.equals('1')){
				headLines.clear();
				headLines.add(countTx+" in bps");
				headLines.add(countRx+" in bps");	
			}
			sumQci = 0;
			
			numOfUEsPerSampleNotHalt = 0;
			
			ArrayList<StreamParams> filteredMap = filterArrayList(map,null, ch);
			
			GeneralUtils.printToConsole("Sample #"+sample);
			for(StreamParams str : filteredMap){
				GeneralUtils.printToConsole("Stream: "+str.getName()+", Tx: "+str.getTxRate()+", Rx: "+str.getRxRate());
			}
			ArrayList<String> halted = new ArrayList<>();
			for(StreamParams str : filteredMap){
				String stream = str.getName();
				double rx = 0;
				long tx = 0;
				
				try{
					if(ch.equals('1')){
						rx = str.getRxRate();
						tx = str.getTxRate();
						values.add(String.valueOf(tx));
						values.add(String.valueOf(rx));		
					}else{
						long val = str.getRxRate();
						rx = convertTo3DigitsAfterPoint(val/1000000.0);
						
						tx = str.getTxRate()/1000;
						values.add(String.format("%.3f", tx/1000.0));
						values.add(String.valueOf(rx));						
					}
				}catch(Exception e){
					e.printStackTrace();
				}
				String streamToPrint = new StringBuilder(stream).insert(stream.length()-1, "_").toString();
				double thresh = HALT_THRESHOLD;
				if(ch.equals('1')){
					thresh*=1000000;
				}
					
				if(rx>=thresh){
					sumQci+=rx;
					numOfUEsPerSampleNotHalt++;
					if(qciGbr){
						sumQciGbrTpt += rx;
					}
					if(mixTwiceAmbrOrTwiceGbr){
						if(br == 0){
							firstQciUes++;
							firstQciTPT += rx;							
						}else{
							secondQciUes++;
							secondQciTPT += rx;							
						}
					}
					if(ch.equals('1')){
						numOfVolte++;
					}
				}else{
					halted.add(streamToPrint);
				}
				allStreams.add(streamToPrint);
				streamList.addValues(streamToPrint, ts, headLines, values);
				values.clear();
			}
			if(!halted.isEmpty()){
				GeneralUtils.startLevel("Number of streams which had TPT lower than threshold (1 Kbs) for QCI "+ch+": "+halted.size());
				for(String str : halted){
					report.report(str);
				}
				GeneralUtils.stopLevel();
			}
			if(qci.size()>1){
				if(ch.equals('1')){
					String printQciSum = "";
					printQciSum = String.format("%.3f", sumQci/1000.0);
					report.report("TPT for qci "+ch+": "+printQciSum+" Kbps");
				}else{
					sumQci = convertTo3DigitsAfterPoint(sumQci);
					report.report("TPT for qci "+ch+": "+sumQci+" Mbps");									
				}
			}
			if(ch.equals('1')){
				sum += convertTo3DigitsAfterPoint(sumQci/1000000.0);
			}else{
				sumQci = convertTo3DigitsAfterPoint(sumQci);
				sum += sumQci;				
			}
			report.report("Number of UEs with traffic higher than threshold for QCI "+ch+": "+numOfUEsPerSampleNotHalt);
			
			if(numOfUEsPerSampleNotHalt!=0){
				if(!ch.equals('1')){
					double min = GeneralUtils.ERROR_VALUE;
					double max = HALT_THRESHOLD;
					double avg = convertTo3DigitsAfterPoint(sumQci/numOfUEsPerSampleNotHalt);
					double dev = 0;
					for(StreamParams str : filteredMap){
						double rx = 0;
						try{
							long val = str.getRxRate();
							rx = convertTo3DigitsAfterPoint(val/1000000.0);
						}catch(Exception e){
							continue;
						}
						if(rx < HALT_THRESHOLD){
							continue;
						}else{
							if(rx < min || min == GeneralUtils.ERROR_VALUE){
								min = rx;
							}
							if(rx > max){
								max = rx;
							}
							dev += Math.pow(rx-avg,2);
						}
					}
					double devSqrt = 0;
					if(numOfUEsPerSampleNotHalt>1){
						devSqrt = convertTo3DigitsAfterPoint(Math.sqrt(dev/(numOfUEsPerSampleNotHalt-1)));						
					}
					double avgDev = convertTo3DigitsAfterPoint(devs[devSample]/(SAMPLES/divideInSteps));
					statsToReport("qci: "+ch, min,max,avg,devSqrt,"Mbps",false);
					if(devSqrt<=avgDev){
						report.report("Standard deviation in new PFS ["+devSqrt+" Mbps] is lower than in old PFS ["+avgDev+" Mbps]");
					}else{
						report.report("Standard deviation in new PFS ["+devSqrt+" Mbps] is higher than in old PFS ["+avgDev+" Mbps]", Reporter.WARNING);
						if(checkBoundariesUes){
							numOfFailsBoundariesUe++;
							checkBoundariesUes = false;
						}
					}
					GeneralUtils.stopLevel();
				}
			}else{
				report.report("All streams had TPT lower than threshold (1 Kbs) for QCI "+ch, Reporter.WARNING);
				if(checkBoundariesUes){
					numOfFailsBoundariesUe++;
					checkBoundariesUes = false;
				}
			}
			br++;
			devSample++;
		}
		
		double d_u = DL_UL.equals("DL") ? dl : ul;
		double criteria = convertTo3DigitsAfterPoint(d_u*THRESHOLD_PERCENT*thresholdSteps);
		sum = convertTo3DigitsAfterPoint(sum);
		if(sum<criteria){
			report.report("TPT ["+sum+" Mbps] is lower than threshold ["+criteria+" Mbps]", Reporter.WARNING);
			checkTPTAll++;
		}else{
			report.report("TPT ["+sum+" Mbps] is higher than threshold ["+criteria+" Mbps]");
		}	
		if(qciGbr){
			double criteriaGbr = convertTo3DigitsAfterPoint(criteria*QCI_GBR_CRITERIA_THRESHOLD);
			sumQciGbrTpt = convertTo3DigitsAfterPoint(sumQciGbrTpt);
			if(sumQciGbrTpt<criteriaGbr){
				report.report("GBR TPT is lower than threshold ["+sumQciGbrTpt+"] (Threshold: "+criteriaGbr+" Mbps)", Reporter.WARNING);
				checkTPTGbr++;
			}else{
				report.report("GBR TPT is higher than threshold ["+sumQciGbrTpt+"] (Threshold: "+criteriaGbr+" Mbps)");
			}				
		}
		
		if(mixTwiceAmbrOrTwiceGbr){
			if(firstQciUes!=0 && secondQciUes!=0){
				double temp = (secondQciTPT*firstQciWeight)/(secondQciUes*secondQciWeight);
				double firstQciAvg = firstQciTPT/firstQciUes;
				if(firstQciAvg>=0.9*temp && firstQciAvg<=1.1*temp){
					report.report("Ratio between the qcis is good");
				}else{
					report.report("Ratio between the qcis is not good", Reporter.WARNING);
					checkRatio++;
				}
			}
		}
		if(volte){
			if(volt==GeneralUtils.ERROR_VALUE){
				volt = numOfUes/2;
			}else{
				if(numOfUes/2<volt){
					volt = numOfUes/2;
				}
			}
			if(numOfVolte!=volt){
				report.report("Number of UEs with Volte TPT is "+numOfVolte+" instead of "+volt,Reporter.WARNING);
				checkNumVolte++;
			}else{
				report.report("Number of UEs with Volte TPT is "+numOfVolte+" as it should be");
			}
		}
	}
	
	@Test
	@TestProperties(name = "Disable_Timer_Inactivity", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void disableTimers() {
		
		if(dut.setSheddingMode(0)){
			report.report("Succeeded to change shedding mode value to 0");
		}else{
			report.report("Failed to change shedding mode value to 0",Reporter.FAIL);
		}
		if(dut.setQci1And2ActivityTimer(0)){
			report.report("Succeeded to change Qci 1 And 2 Activity Timer value to 0");
		}else{
			report.report("Failed to change Qci 1 And 2 Activity Timer value to 0",Reporter.FAIL);
		}
		dut.disableIdleModeAndReboot();
	}

	//@Test
	@TestProperties(name = "Enable_Timer_Inactivity", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void enableTimerInactivity() {
		dut.enableIdleModeAndReboot();
	}

	private void innerSingleQci(char ch, int num, String du){
		while(retry<=NUM_OF_RETRIES){
			report.report("Run #"+retry,ReportAttribute.BOLD);
			ArrayList<Character> q = new ArrayList<Character>();
			q.add(ch);
			preTest(true, true,1,num,du,q,1.2,false,false,0,0, false);
			
			report.report("Gathering data from old PFS",ReportAttribute.BOLD);
			if(!genericTestOldPfs(1,SAMPLES,true, true,0)){
				failTreatment();
				continue;
			}
			report.report("Testing new PFS",ReportAttribute.BOLD);
			if(genericTestNewPfs(1,SAMPLES,true, true,0, 1)){
				break;
			}else{
				failTreatment();
			}
		}
		if(retry==NUM_OF_RETRIES+1){
			report.report("Failed to run test for the "+(retry-1)+" time", Reporter.FAIL);
		}
	}
	
	//TESTS AMBR
	
	@Test
	@TestProperties(name = "AMBR_64UEs_DL", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void AMBR_64UEs_DL() {
		innerSingleQci('9', 64, "DL");
	}
	
	@Test
	@TestProperties(name = "AMBR_128UEs_DL", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void AMBR_128UEs_DL() {
		innerSingleQci('9', 128, "DL");
	}
	
	@Test
	@TestProperties(name = "AMBR_64UEs_UL", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void AMBR_64UEs_UL() {
		innerSingleQci('9', 64, "UL");
	}
	
	@Test
	@TestProperties(name = "AMBR_128UEs_UL", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void AMBR_128UEs_UL() {
		innerSingleQci('9', 128, "UL");
	}

	//TESTS GBR

	@Test
	@TestProperties(name = "GBR_64UEs_DL", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void GBR_64UEs_DL() {
		innerSingleQci('4', 64, "DL");
	}
	
	@Test
	@TestProperties(name = "GBR_128UEs_DL", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void GBR_128UEs_DL() {
		innerSingleQci('4', 128, "DL");
	}

	@Test
	@TestProperties(name = "GBR_64UEs_UL", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void GBR_64UEs_UL() {
		innerSingleQci('4', 64, "UL");
	}

	@Test
	@TestProperties(name = "GBR_128UEs_UL", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void GBR_128UEs_UL() {
		innerSingleQci('4', 128, "UL");
	}
	
	private void innerMultipleQci(char first, char second, int num, String du, double percent, boolean tptG,
			boolean mix2AmbrOrGbr, int qci1Weight, int qci2Weight){
		while(retry<=NUM_OF_RETRIES){
			report.report("Run #"+retry,ReportAttribute.BOLD);
			ArrayList<Character> q = new ArrayList<Character>();
			q.add(first);
			q.add(second);
			preTest(true, true,1,num,du,q,percent,tptG,mix2AmbrOrGbr,qci1Weight,qci2Weight, false);
			report.report("Gathering data from old PFS",ReportAttribute.BOLD);

			if(!genericTestOldPfs(1,SAMPLES,true, true,0)){
				failTreatment();
				continue;
			}
			report.report("Testing new PFS",ReportAttribute.BOLD);
			if(genericTestNewPfs(1,SAMPLES,true, true,0,1)){
				break;
			}else{
				failTreatment();
			}
		}
		if(retry==NUM_OF_RETRIES+1){
			report.report("Failed to run test for the "+(retry-1)+" time", Reporter.FAIL);
		}
	}
	
	//TESTS MIX AMBR GBR
	
	@Test
	@TestProperties(name = "MIX_AMBR_GBR_64UEs_DL", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void mix_Ambr_Gbr_64UEs_DL() {
		innerMultipleQci('9','4',64,"DL",1.5,true,false,0,0);
	}
	
	@Test
	@TestProperties(name = "MIX_AMBR_GBR_128UEs_DL", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void mix_Ambr_Gbr_128UEs_DL() {
		innerMultipleQci('9','4',128,"DL",1.5,true,false,0,0);
	}
	
	@Test
	@TestProperties(name = "MIX_AMBR_GBR_64UEs_UL", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void mix_Ambr_Gbr_64UEs_UL() {
		innerMultipleQci('9','4',64,"UL",1.5,true,false,0,0);
	}
	
	@Test
	@TestProperties(name = "MIX_AMBR_GBR_128UEs_UL", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void mix_Ambr_Gbr_128UEs_UL() {
		innerMultipleQci('9','4',128,"UL",1.5,true,false,0,0);
	}
	
	//TESTS MIX AMBR AMBR
	
	@Test
	@TestProperties(name = "MIX_AMBR_AMBR_64UEs_DL", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void mix_Ambr_Ambr_64UEs_DL() {
		innerMultipleQci('9','7',64,"DL",1.2,true,true,1,3);
	}
		
	@Test
	@TestProperties(name = "MIX_AMBR_AMBR_128UEs_DL", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void mix_Ambr_Ambr_128UEs_DL() {
		innerMultipleQci('9','7',128,"DL",1.2,true,true,1,3);
	}
		
	@Test
	@TestProperties(name = "MIX_AMBR_AMBR_64UEs_UL", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void mix_Ambr_Ambr_64UEs_UL() {
		innerMultipleQci('9','7',64,"UL",1.2,true,true,1,3);
	}
		
	@Test
	@TestProperties(name = "MIX_AMBR_AMBR_128UEs_UL", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void mix_Ambr_Ambr_128UEs_UL() {
		innerMultipleQci('9','7',128,"UL",1.2,true,true,1,3);
	}
	
	//TESTS MIX GBR GBR
	
	@Test
	@TestProperties(name = "MIX_GBR_GBR_64UEs_DL", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void mix_Gbr_Gbr_64UEs_DL() {
		innerMultipleQci('4','2',64,"DL",1.2,true,true,1,3);
	}
		
	@Test
	@TestProperties(name = "MIX_GBR_GBR_128UEs_DL", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void mix_Gbr_Gbr_128UEs_DL() {
		innerMultipleQci('4','2',128,"DL",1.2,true,true,1,3);
	}
		
	@Test
	@TestProperties(name = "MIX_GBR_GBR_64UEs_UL", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void mix_Gbr_Gbr_64UEs_UL() {
		innerMultipleQci('4','2',64,"UL",1.2,true,true,1,3);
	}
			
	@Test
	@TestProperties(name = "MIX_GBR_GBR_128UEs_UL", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void mix_Gbr_Gbr_128UEs_UL() {
		innerMultipleQci('4','2',128,"UL",1.2,true,true,1,3);
	}

	private void innerMultipleSteps(char first, char second, double threshSteps, int num, String d_u,
			double perc, boolean same){
		while(retry<=NUM_OF_RETRIES){
			report.report("Run #"+retry,ReportAttribute.BOLD);
			ArrayList<Character> q = new ArrayList<Character>();
			q.add(first);
			preTest(true, true,threshSteps,num,d_u,q,perc,false,false,0,0,true);
			report.report("Gathering data from old PFS",ReportAttribute.BOLD);
			if(!genericTestOldPfs(1,SAMPLES/2,true, false,0)){
				failTreatment();
				continue;
			}
			q.add(second);
			preTest(false,false,1,num*2,d_u,q,perc*2,true,same,1,3,false);
			if(!genericTestOldPfs(SAMPLES/2+2,SAMPLES,false, true,1)){
				failTreatment();
				continue;
			}
			
			q = new ArrayList<Character>();
			q.add(first);
			preTest(false, true,threshSteps,num,d_u,q,perc,false,false,0,0,true);
			report.report("Testing new PFS",ReportAttribute.BOLD);
			if(!genericTestNewPfs(1,SAMPLES/2,true, false,0,2)){
				failTreatment();
				continue;
			}
			
			q.add(second);
			preTest(false,false,1,num*2,d_u,q,perc*2,true,same,1,3,false);
			if(!genericTestNewPfs(SAMPLES/2+2,SAMPLES,false, true,1,2)){
				failTreatment();
			}else{
				break;
			}
		}
		if(retry==NUM_OF_RETRIES+1){
			report.report("Failed to run test for the "+(retry-1)+" time", Reporter.FAIL);
		}
	}
	
	//TEST MIX AMBR STEPS
	
	@Test
	@TestProperties(name = "MIX_AMBR_AMBR_64UEs_DL_STEPS", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void mix_Ambr_Ambr_64UEs_DL_Steps() {
		innerMultipleSteps('9','7',0.6,32,"DL",0.6,true);
	}
	
	@Test
	@TestProperties(name = "MIX_AMBR_AMBR_128UEs_DL_STEPS", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void mix_Ambr_Ambr_128UEs_DL_Steps() {
		innerMultipleSteps('9','7',0.6,64,"DL",0.6,true);
	}
	
	@Test
	@TestProperties(name = "MIX_AMBR_AMBR_64UEs_UL_STEPS", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void mix_Ambr_Ambr_64UEs_UL_Steps() {
		innerMultipleSteps('9','7',0.6,32,"UL",0.6,true);
	}
	
	@Test
	@TestProperties(name = "MIX_AMBR_AMBR_128UEs_UL_STEPS", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void mix_Ambr_Ambr_128UEs_UL_Steps() {
		innerMultipleSteps('9','7',0.6,64,"UL",0.6,true);
	}
	
	//TEST MIX GBR STEPS
	
	@Test
	@TestProperties(name = "MIX_GBR_GBR_64UEs_DL_STEPS", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void mix_Gbr_Gbr_64UEs_DL_Steps() {
		innerMultipleSteps('4','2',0.6,32,"DL",0.6,true);
	}
	
	@Test
	@TestProperties(name = "MIX_GBR_GBR_128UEs_DL_STEPS", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void mix_Gbr_Gbr_128UEs_DL_Steps() {
		innerMultipleSteps('4','2',0.6,64,"DL",0.6,true);
	}
	
	@Test
	@TestProperties(name = "MIX_GBR_GBR_64UEs_UL_STEPS", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void mix_Gbr_Gbr_64UEs_UL_Steps() {
		innerMultipleSteps('4','2',0.6,32,"UL",0.6,true);
	}
	
	@Test
	@TestProperties(name = "MIX_GBR_GBR_128UEs_UL_STEPS", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void mix_Gbr_Gbr_128UEs_UL_Steps() {
		innerMultipleSteps('4','2',0.6,64,"UL",0.6,true);
	}
	
	//TEST MIX AMBR GBR STEPS
	
	@Test
	@TestProperties(name = "MIX_AMBR_GBR_64UEs_DL_STEPS", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void mix_Ambr_Gbr_64UEs_DL_Steps() {
		innerMultipleSteps('9','4',0.75,32,"DL",0.75,false);
	}
		
	@Test
	@TestProperties(name = "MIX_AMBR_GBR_128UEs_DL_STEPS", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void mix_Ambr_Gbr_128UEs_DL_Steps() {
		innerMultipleSteps('9','4',0.75,64,"DL",0.75,false);
	}
		
	@Test
	@TestProperties(name = "MIX_AMBR_GBR_64UEs_UL_STEPS", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void mix_Ambr_Gbr_64UEs_UL_Steps() {
		innerMultipleSteps('9','4',0.75,32,"UL",0.75,false);
	}
		
	@Test
	@TestProperties(name = "MIX_AMBR_GBR_128UEs_UL_STEPS", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void mix_Ambr_Gbr_128UEs_UL_Steps() {
		innerMultipleSteps('9','4',0.75,64,"UL",0.75,false);
	}
	
	private void innerTestVolte(int num, String d_u){
		while(retry<=NUM_OF_RETRIES){
			report.report("Run #"+retry,ReportAttribute.BOLD);
			ArrayList<Character> q = new ArrayList<Character>();
			q.add('4');
			q.add('1');
			preTest(true, true, 1, num, d_u, q, 1.2, false, false, 0, 0, false);
			if(genericTestVolte()){
				break;
			}else{
				failTreatment();
				numOfVolte = 0;
			}
		}
		if(retry==NUM_OF_RETRIES+1){
			report.report("Failed to run test for the "+(retry-1)+" time", Reporter.FAIL);
		}		
	}
	
	@Test
	@TestProperties(name = "MIX_VOLTE_GBR_128UEs_DL", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void mix_Volte_Gbr_128UEs_DL() {
		innerTestVolte(128,"DL");
	}
	
	@Test
	@TestProperties(name = "MIX_VOLTE_GBR_128UEs_UL", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void mix_Volte_Gbr_128UEs_UL() {
		innerTestVolte(128,"UL");
	}
	
	@Test
	@TestProperties(name = "MIX_VOLTE_GBR_64UEs_DL", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void mix_Volte_Gbr_64UEs_DL() {
		innerTestVolte(64,"DL");
	}
	
	@Test
	@TestProperties(name = "MIX_VOLTE_GBR_64UEs_UL", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void mix_Volte_Gbr_64UEs_UL() {
		innerTestVolte(64,"UL");
	}
	
	private boolean genericTestVolte(){
		report.report("Gathering data from old PFS",ReportAttribute.BOLD);
		if(!genericTestVolteHelper(PfsType.OldPfs)){
			return false;
		}
		report.report("Testing new PFS",ReportAttribute.BOLD);
		if(!genericTestVolteHelper(PfsType.NewPfs)){
			return false;
		}
		return true;
	}
	
	private boolean genericTestVolteHelper(PfsType pfsType){
		if(!getCalc){
			return false;
		}
		dut.setPfsType(pfsType.ordinal());
		
		boolean action = false;
		setTestUESimVolte(numOfUes);					
		uesim.start();
		GeneralUtils.startLevel("Starting traffic");
		try {
			action = traffic.startTraffic();
			configurePortLoads(dl*portLoadPercent,ul*portLoadPercent);
			disableAllStreamsMixTest();
			enableNeededStreamsVolte();
			setLoadPerStreamTestVolte();	
			GeneralUtils.unSafeSleep(5*1000);
		} catch (Exception e) {
			action = false;
			e.printStackTrace();
		}
		if (!action) {
			report.report("Failed to start traffic", Reporter.FAIL);
			GeneralUtils.stopLevel();
			reason = "Failed to start traffic";
			return false;
		} else {
			report.report("Traffic started successfully");
		}
		commandsThread.start();
		parallelHasStarted = true;
		GeneralUtils.unSafeSleep(1*60*1000);
		GeneralUtils.stopLevel();
		
		if(pfsType == PfsType.OldPfs){
			GeneralUtils.startLevel("Sampling TPT ePFS type = "+pfsType.ordinal());
			traffic.getTxAndRxCounter(counters);
			for (int i = 1; i <= SAMPLES; i++) {				
				GeneralUtils.startLevel("SAMPLE #" + i);
				int num = dut.getUeNumberToEnb();
				report.report("Number of UEs connected to EnodeB: "+num);
				if(num==0){
					GeneralUtils.stopLevel();
					GeneralUtils.stopLevel();
					report.report("No UEs were connected to EnodeB", Reporter.WARNING);
					return false;
				}
				getDataOldPfs(i,0);					
				GeneralUtils.stopLevel();				
			}
			GeneralUtils.stopLevel();
			stopTrafficAndSimulator();
			report.report("Parallel commands for old PFS");
			stopParallelAndAttachFile();
			return true;
		}
		
		GeneralUtils.startLevel("Sampling TPT ePFS type = "+pfsType.ordinal());
		volte = true;
		traffic.getTxAndRxCounter(counters);
		for (int i = 1; i <= SAMPLES; i++) {
			GeneralUtils.startLevel("SAMPLE #" + i);
			int num = dut.getUeNumberToEnb();
			report.report("Number of UEs connected to EnodeB: "+num);
			if(num==0){
				GeneralUtils.stopLevel();
				GeneralUtils.stopLevel();
				report.report("No UEs were connected to EnodeB", Reporter.WARNING);
				return false;
			}
			getDataNewPfs(i,0,1);
			GeneralUtils.stopLevel();				
		}
		GeneralUtils.stopLevel();
		
		printDataOfUes();
		streamList = new StreamList();
		
		stopTrafficAndSimulator();

		if (checkTPTAll>SAMPLES/PERCENTAGE_OF_FAIL) {
			report.report("Test failed due to low TPT", Reporter.FAIL);
			reason = "Test failed due to low TPT";
		}				
			
		if(numOfFailsBoundariesUe>SAMPLES/PERCENTAGE_OF_FAIL){
			report.report("Test failed due to higher standard deviation in new PFS", Reporter.FAIL);
			reason = "Test failed due to higher standard deviation in new PFS";
		}	
		if(checkNumVolte>SAMPLES/PERCENTAGE_OF_FAIL){
			report.report("Test failed due to wrong number of UEs with Volte TPT", Reporter.FAIL);
			reason = "Test failed due to wrong number of UEs with Volte TPT";
		}
		return true;
	}
	
	private boolean almostTcpHelper(){
		report.report("Gathering data from old PFS",ReportAttribute.BOLD);
		if(!genericTestAlmostTcp(PfsType.OldPfs)){
			return false;
		}
		report.report("Testing new PFS",ReportAttribute.BOLD);
		if(!genericTestAlmostTcp(PfsType.NewPfs)){
			return false;
		}
		return true;
	}
	
	private boolean genericTestAlmostTcp(PfsType pfsType){
		dut.lteCli("logger threshold set process=enodeb0 client=UL_PFS cli=1");
		tcpLogger = true;
		if(!getCalc){
			return false;
		}
		dut.setPfsType(pfsType.ordinal());
		boolean action = false;
		setTestUESim(numOfUes);					
		uesim.start();
		GeneralUtils.startLevel("Starting traffic");
		try {
			action = traffic.startTraffic();
			configurePortLoads(dl*portLoadPercent,ul*portLoadPercent);
			setLoadPerStreamTestAlmostTcp();	
			GeneralUtils.unSafeSleep(5*1000);
		} catch (Exception e) {
			action = false;
			e.printStackTrace();
		}
		if (!action) {
			report.report("Failed to start traffic", Reporter.FAIL);
			GeneralUtils.stopLevel();
			reason = "Failed to start traffic";
			return false;
		} else {
			report.report("Traffic started successfully");
		}
		commandsThread.start();
		parallelHasStarted = true;
		GeneralUtils.stopLevel();
		
		if(pfsType == PfsType.OldPfs){
			GeneralUtils.startLevel("Sampling TPT ePFS type = "+pfsType.ordinal());
			traffic.getTxAndRxCounter(counters);
			for (int i = 1; i <= SAMPLES; i++) {
				GeneralUtils.startLevel("SAMPLE #" + i);
				report.report("PFS type: "+dut.getPfsType());
				int num = dut.getUeNumberToEnb();
				report.report("Number of UEs connected to EnodeB: "+num);
				if(num==0){
					GeneralUtils.stopLevel();
					GeneralUtils.stopLevel();
					report.report("No UEs were connected to EnodeB", Reporter.WARNING);
					return false;
				}
				getDataAlmostTcpOldPfs(i);					
				GeneralUtils.stopLevel();				
			}
			GeneralUtils.stopLevel();
			stopTrafficAndSimulator();
			stopParallelAndAttachFile();
			return true;
		}
		
		GeneralUtils.startLevel("Sampling TPT ePFS type = "+pfsType.ordinal());
		traffic.getTxAndRxCounter(counters);
		for (int i = 1; i <= SAMPLES; i++) {
			GeneralUtils.startLevel("SAMPLE #" + i);
			int num = dut.getUeNumberToEnb();
			report.report("Number of UEs connected to EnodeB: "+num);
			if(num==0){
				GeneralUtils.stopLevel();
				GeneralUtils.stopLevel();
				report.report("No UEs were connected to EnodeB", Reporter.WARNING);
				return false;
			}
			getDataAlmostTcpNewPfs(i);
			GeneralUtils.stopLevel();				
		}
		GeneralUtils.stopLevel();
		
		printDataOfUes();
		streamList = new StreamList();
		
		stopTrafficAndSimulator();

		if (checkTPTAll>SAMPLES/PERCENTAGE_OF_FAIL) {
			report.report("Test failed due to low TPT", Reporter.FAIL);
			reason = "Test failed due to low TPT. ";
		}				
			
		if(numOfFailsBoundariesUe>SAMPLES/PERCENTAGE_OF_FAIL){
			report.report("Test failed due to higher standard deviation in new PFS", Reporter.FAIL);
			reason = "Test failed due to higher standard deviation in new PFS";
		}		
		return true;
	}
	
	private void innerAlmostTcp(int num){
		while(retry<=NUM_OF_RETRIES){
			report.report("Run #"+retry,ReportAttribute.BOLD);
			ArrayList<Character> q = new ArrayList<Character>();
			q.add('9');
			preTest(false, true, 1, num, null, q, 1.2, false, false, 0, 0, false);
			devs = new double[2];
			for(int i=0;i<2;i++){
				devs[i]=0;
			}			
			if(almostTcpHelper()){
				break;
			}else{
				failTreatment();
			}
		}
		if(retry==NUM_OF_RETRIES+1){
			report.report("Failed to run test for the "+(retry-1)+" time", Reporter.FAIL);
		}		
	}
	
	@Test
	@TestProperties(name = "ALMOST_TCP_BO_AMBR_128UEs", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void almostTCPboAMBR128UEs() {
		innerAlmostTcp(128);
	}
	
	@Test
	@TestProperties(name = "ALMOST_TCP_BO_AMBR_64UEs", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void almostTCPboAMBR64UEs() {
		innerAlmostTcp(64);
	}
	
	private void getDataAlmostTcpOldPfs(int sample){
		ArrayList<StreamParams> map = null;
		map = traffic.getTxAndRxCounterDLOrUL(counters,TransmitDirection.UL);
		
		for(Character ch : qci){
			ArrayList<StreamParams> filteredMap = filterArrayList(map,null, ch);

			GeneralUtils.printToConsole("UL - Sample #"+sample);
			for(StreamParams str : filteredMap){
				GeneralUtils.printToConsole("Stream: "+str.getName()+", Tx: "+str.getTxRate()+", Rx: "+str.getRxRate());
			}
			secondQciUes = 0;
			secondQciTPT = 0;							
			firstQciUes = 0;
			firstQciTPT = 0;
			for(StreamParams str : filteredMap){
				String stream = str.getName();
				double rx = 0;
				
				try{
					rx = str.getRxRate();
				}catch(Exception e){
					e.printStackTrace();
				}
				if(stream.equals("UL_UE19") || stream.equals("UL_UE29") || stream.equals("UL_UE39")){
					rx = convertTo3DigitsAfterPoint(rx/1000000.0);
					if(rx>=HALT_THRESHOLD){
						secondQciUes++;
						secondQciTPT += rx;	
					}
				}else{
					if(rx>=HALT_THRESHOLD*1000000){
						firstQciUes++;
						firstQciTPT += rx;							
					}
				}
			}
					
			double minA = GeneralUtils.ERROR_VALUE;
			double maxA = HALT_THRESHOLD;
			
			double minB = GeneralUtils.ERROR_VALUE;
			double maxB = HALT_THRESHOLD;
			double avgGroupA = 0;
			if(firstQciUes!=0){
				avgGroupA = convertTo3DigitsAfterPoint(firstQciTPT/firstQciUes);
			}else{
				report.report("All streams from group A had UL TPT lower than threshold (1 Kbs) for QCI "+ch, Reporter.WARNING);
			}
			double avgGroupB = 0;
			if(secondQciUes!=0){
				avgGroupB = convertTo3DigitsAfterPoint(secondQciTPT/secondQciUes);
			}else{
				report.report("All streams from group B had UL TPT lower than threshold (1 Kbs) for QCI "+ch, Reporter.WARNING);
			}
			double devGroupA = 0;
			double devGroupB = 0;
			for(StreamParams str : filteredMap){
				String stream = str.getName();
				double rx = 0;
				try{
					rx = str.getRxRate();
				}catch(Exception e){
					continue;
				}
				if(stream.equals("UL_UE19") || stream.equals("UL_UE29") || stream.equals("UL_UE39")){
					if(avgGroupB!=0){
						rx = convertTo3DigitsAfterPoint(rx/1000000.0);
						if(rx>=HALT_THRESHOLD){
							if(rx<minB || minB == GeneralUtils.ERROR_VALUE){
								minB = rx;
							}
							if(rx>maxB){
								maxB = rx;
							}
							devGroupB += Math.pow(rx-avgGroupB,2);
						}
					}
				}else{
					if(avgGroupA!=0){
						if(rx>=HALT_THRESHOLD*1000000){
							if(rx<minA || minA == GeneralUtils.ERROR_VALUE){
								minA = rx;
							}
							if(rx>maxA){
								maxA = rx;
							}
							devGroupA += Math.pow(rx-avgGroupA,2);
						}								
					}
				}	
			}
			if(firstQciUes!=0){
				double devSqrtA = 0;
				if(firstQciUes>1){
					devSqrtA = convertTo3DigitsAfterPoint(Math.sqrt(devGroupA/(firstQciUes-1)));					
				}
				statsToReport("group A", minA,maxA,avgGroupA,devSqrtA,"bps",true);
				devs[0] += devSqrtA;				
			}
			if(secondQciUes!=0){
				double devSqrtB = 0;
				if(secondQciUes>1){
					devSqrtB = convertTo3DigitsAfterPoint(Math.sqrt(devGroupB/(secondQciUes-1)));					
				}
				statsToReport("group B", minB,maxB,avgGroupB,devSqrtB,"Mbps",true);
				devs[1] += devSqrtB;				
			}
		}
	}
	
	private void getDataAlmostTcpNewPfs(int sample){
		ArrayList<StreamParams> map = null;
		ArrayList<String> headLines = new ArrayList<String>();
		ArrayList<String> values = new ArrayList<String>();
		
		String countRx = "rx" + counters.get(0);
		String countTx = "tx" + counters.get(0);
		headLines.add(countTx+" in Mbps");
		headLines.add(countRx+" in Mbps");
		long sum = 0;
		int numOfUEsPerSampleNotHalt;
		
		String ts = null;
		
		ts = GeneralUtils.timeFormat(System.currentTimeMillis());
		map = traffic.getTxAndRxCounter(counters);

		boolean checkBoundariesUes = true;
		for(Character ch : qci){
			numOfUEsPerSampleNotHalt = 0;
			
			ArrayList<StreamParams> filteredMap = filterArrayList(map,"DL",ch);

			GeneralUtils.printToConsole("DL - Sample #"+sample);
			for(StreamParams str : filteredMap){
				GeneralUtils.printToConsole("Stream: "+str.getName()+", Tx: "+str.getTxRate()+", Rx: "+str.getRxMaxRate());
			}

			for(StreamParams str : filteredMap){
				String stream = str.getName();
				double rx = 0;
				long tx = 0;
				
				try{
					long val = str.getRxRate();
					rx = convertTo3DigitsAfterPoint(val/1000000.0);
					
					tx = str.getTxRate()/1000;
					values.add(String.format("%.3f", tx/1000.0));
					values.add(String.valueOf(rx));
				}catch(Exception e){
					e.printStackTrace();
				}
				if(rx>=HALT_THRESHOLD){
					sum+=rx;
				}
				String streamToPrint = new StringBuilder(stream).insert(stream.length()-1, "_").toString();
				allStreams.add(streamToPrint);
				streamList.addValues(streamToPrint, ts, headLines, values);
				values.clear();
			}
			double criteria = dl*THRESHOLD_PERCENT*thresholdSteps;
			if(sum<criteria){
				report.report("DL TPT ["+sum+" Mbps] is lower than threshold ["+criteria+" Mbps]", Reporter.WARNING);
				checkTPTAll++;
			}else{
				report.report("DL TPT ["+sum+" Mbps] is higher than threshold ["+criteria+" Mbps]");
			}
			
			filteredMap = filterArrayList(map,"UL",ch);
			
			GeneralUtils.printToConsole("UL - Sample #"+sample);
			for(StreamParams str : filteredMap){
				GeneralUtils.printToConsole("Stream: "+str.getName()+", Tx: "+str.getTxRate()+", Rx: "+str.getRxMaxRate());
			}
			ArrayList<String> halted = new ArrayList<>();
			secondQciUes = 0;
			secondQciTPT = 0;							
			firstQciUes = 0;
			firstQciTPT = 0;
			for(StreamParams str : filteredMap){
				String stream = str.getName();
				double rx = 0;
				long tx = 0;
				
				try{
					rx = str.getRxRate();
					tx = str.getTxRate();
					values.add(String.valueOf(tx));
					values.add(String.valueOf(rx));
				}catch(Exception e){
					e.printStackTrace();
				}
				String streamToPrint = new StringBuilder(stream).insert(stream.length()-1, "_").toString();
				if(stream.equals("UL_UE19") || stream.equals("UL_UE29") || stream.equals("UL_UE39")){
					headLines.clear();
					headLines.add(countTx+" in Mbps");
					headLines.add(countRx+" in Mbps");
					rx = convertTo3DigitsAfterPoint(rx/1000000.0);
					if(rx>=HALT_THRESHOLD){
						numOfUEsPerSampleNotHalt++;
						secondQciUes++;
						secondQciTPT += rx;	
					}else{
						halted.add(streamToPrint);
					}
				}else{
					headLines.clear();
					headLines.add(countTx+" in bps");
					headLines.add(countRx+" in bps");
					if(rx>=HALT_THRESHOLD*1000000){
						numOfUEsPerSampleNotHalt++;
						firstQciUes++;
						firstQciTPT += rx;
					}else{
						halted.add(streamToPrint);
					}
				}
				allStreams.add(streamToPrint);
				streamList.addValues(streamToPrint, ts, headLines, values);
				values.clear();
			}
			report.report("Number of UEs with UL traffic higher than threshold for QCI "+ch+": "+numOfUEsPerSampleNotHalt);
			if(!halted.isEmpty()){
				GeneralUtils.startLevel("Number of streams which had UL TPT lower than threshold (1 Kbs) for QCI "+ch+": "+halted.size());
				for(String str : halted){
					report.report(str);
				}
				GeneralUtils.stopLevel();
			}
			
			double avgGroupA = 0;
			if(firstQciUes!=0){
				avgGroupA = convertTo3DigitsAfterPoint(firstQciTPT/firstQciUes);
			}else{
				report.report("All streams from group A had UL TPT lower than threshold (1 Kbs) for QCI "+ch, Reporter.WARNING);
				numOfFailsBoundariesUe++;
				checkBoundariesUes = false;
			}
			double avgGroupB = 0;
			if(secondQciUes!=0){
				avgGroupB = convertTo3DigitsAfterPoint(secondQciTPT/secondQciUes);
			}else{
				report.report("All streams from group B had UL TPT lower than threshold (1 Kbs) for QCI "+ch, Reporter.WARNING);
				numOfFailsBoundariesUe++;
				checkBoundariesUes = false;
			}
			double minA = GeneralUtils.ERROR_VALUE;
			double maxA = HALT_THRESHOLD;
			double minB = GeneralUtils.ERROR_VALUE;
			double maxB = HALT_THRESHOLD;
			double devGroupA = 0;
			double devGroupB = 0;
			for(StreamParams str : filteredMap){
				String stream = str.getName();
				double rx = 0;
				try{
					rx = str.getRxRate();
				}catch(Exception e){
					continue;
				}
				if(stream.equals("UL_UE19") || stream.equals("UL_UE29") || stream.equals("UL_UE39")){
					if(avgGroupB!=0){
						rx = convertTo3DigitsAfterPoint(rx/1000000.0);
						if(rx>=HALT_THRESHOLD){
							if(rx<minB || minB == GeneralUtils.ERROR_VALUE){
								minB = rx;
							}
							if(rx>maxB){
								maxB = rx;
							}
							devGroupB += Math.pow(rx-avgGroupB,2);
						}							
					}
				}else{
					if(avgGroupA!=0){
						if(rx>=HALT_THRESHOLD*1000000){
							if(rx<minA || minA == GeneralUtils.ERROR_VALUE){
								minA = rx;
							}
							if(rx>maxA){
								maxA = rx;
							}
							devGroupA += Math.pow(rx-avgGroupA,2);	
						}								
					}
				}
			}
			if(firstQciUes!=0){
				double devSqrtA = 0;
				if(firstQciUes>1){
					devSqrtA = convertTo3DigitsAfterPoint(Math.sqrt(devGroupA/(secondQciUes-1)));					
				}
				double avgDev = convertTo3DigitsAfterPoint(devs[0]/SAMPLES);
				statsToReport("group A", minA,maxA,avgGroupA,devSqrtA,"bps",false);
				if(devSqrtA<=avgDev){
					report.report("Standard deviation in new PFS ["+devSqrtA+" bps] is lower than in old PFS ["+avgDev+" bps] for group A");
				}else{
					report.report("Standard deviation in new PFS ["+devSqrtA+" bps] is higher than in old PFS ["+avgDev+" bps] for group A", Reporter.WARNING);
					if(checkBoundariesUes){
						numOfFailsBoundariesUe++;
						checkBoundariesUes = false;
					}
				}
				GeneralUtils.stopLevel();
			}
			if(secondQciUes!=0){
				double devSqrtB = 0;
				if(secondQciUes>1){
					devSqrtB = convertTo3DigitsAfterPoint(Math.sqrt(devGroupB/(secondQciUes-1)));					
				}
				double avgDev = convertTo3DigitsAfterPoint(devs[1]/SAMPLES);
				statsToReport("group B", minB,maxB,avgGroupB,devSqrtB,"Mbps",false);
				if(devSqrtB<=avgDev){
					report.report("Standard deviation in new PFS ["+devSqrtB+" Mbps] is lower than in old PFS ["+avgDev+" Mbps] for group B");
				}else{
					report.report("Standard deviation in new PFS ["+devSqrtB+" Mbps] is higher than in old PFS ["+avgDev+" Mbps] for group B", Reporter.WARNING);
					if(checkBoundariesUes){
						numOfFailsBoundariesUe++;
						checkBoundariesUes = false;
					}
				}
				GeneralUtils.stopLevel();
			}
			
		}
	}
	
	private void configurePortLoads(Double dl, Double ul){
		Pair<Double,Double> configure = Pair.createPair(dl, ul);
		traffic.trafficLoadSet(configure);
	}
	
	private void setLoadPerStreamTestAlmostTcp(){
		GeneralUtils.startLevel("Setting load per stream");
		//disableUnneededStreams();
		disableDLGroupB();
		ArrayList<LoadParam> load = new ArrayList<LoadParam>();
		int i=1;
		for (i = 1; i <= 3; i++) {
			LoadParam lp = new LoadParam(1.5, '9', i, LoadParam.LoadUnit.MEGABITS_PER_SECOND,null);
			load.add(lp);
		}
		for( ;i<=numOfUes;i++){
			LoadParam lp = new LoadParam(15, '9', i, LoadParam.LoadUnit.KILOBITS_PER_SECOND,null);
			load.add(lp);
		}
		traffic.setLoadStreamUL(load);	
		try {
			traffic.stopTrafficWithoutDisconnect();
			GeneralUtils.unSafeSleep(5*1000);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			traffic.startTrafficSTC();
			report.report("Traffic started. Waiting 16 seconds until ues will stabilize");
			GeneralUtils.unSafeSleep(16*1000);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		GeneralUtils.stopLevel();
	}
	
	private void setLoadPerStreamTestVolte(){
		GeneralUtils.startLevel("Setting load per stream");
		
		double l = DL_UL.equals("DL") ? dl : ul;
		l = (l*portLoadPercent)/(numOfUes/2);
		ArrayList<LoadParam> load = new ArrayList<LoadParam>();
		for (int i = 1; i <= numOfUes/2; i++) {
			LoadParam lp = new LoadParam(l, '4', i, LoadParam.LoadUnit.MEGABITS_PER_SECOND,null);
			load.add(lp);
		}
		for(int i=129;i<129+numOfUes/2;i++){
			LoadParam lp = new LoadParam(13, '1', i, LoadParam.LoadUnit.KILOBITS_PER_SECOND,82);
			load.add(lp);
		}
		if(DL_UL.equals("DL")){
			traffic.setLoadStreamDL(load);				
		}else{
			traffic.setLoadStreamUL(load);	
		}
		try {
			traffic.stopTrafficWithoutDisconnect();
			GeneralUtils.unSafeSleep(5*1000);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			traffic.startTrafficSTC();
			report.report("Traffic started. Waiting 16 seconds until ues will stabilize");
			GeneralUtils.unSafeSleep(16*1000);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		GeneralUtils.stopLevel();
	}
	
	private void setTestUESimVolte(int numberUEs) {
		if (numberUEs == 64) {
			uesim.setTestByPath("Airspan!Cell2!test_STC_QCI9_7_Volte_BIDI_Automation64");
		} else {
			uesim.setTestByPath("Airspan!Cell2!test_STC_QCI9_7_Volte_BIDI_Automation");
		}
	}
	
	private void setTestUESim(int numberUEs) {
		if (numberUEs == 64) {
			uesim.setTestByPath("Airspan!Cell2!test_STC_QCI9_7_BIDI_Automation64");
		} else {
			uesim.setTestByPath("Airspan!Cell2!test_STC_QCI9_7_BIDI_Automation");
		}
	}

	private void disableUnneededStreams() {
		ArrayList<String> ues = new ArrayList<>();
		for (int i = 1; i <= numOfUes; i++) {
			ues.add("UE" + i);
		}
		try {
			traffic.disableUnneededStreams(Protocol.UDP,ues, qci);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void disableDLGroupB(){
		ArrayList<String> ues = new ArrayList<>();
		for (int i = 1; i <= 3; i++) {
			ues.add("UE" + i);
		}
		try {
			traffic.disableStreamsByUeNameQciAndPortDirectionAndEnableTheRest(Protocol.UDP,ues, qci, TransmitDirection.DL);
			//traffic.disableDLStreamsByNameAndQci(ues, qci);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void disableAllStreamsMixTest() {
		ArrayList<String> ues = new ArrayList<>();
		ArrayList<Character> qci2 = new ArrayList<>();
		ues.add("UE1");
		try {
			traffic.disableUnneededStreams(Protocol.UDP,ues, qci2);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void enableNeededStreamsVolte(){
		ArrayList<String> ues = new ArrayList<>();
		ArrayList<String> ues2 = new ArrayList<>();
		ArrayList<Character> qci1 = new ArrayList<Character>();
		ArrayList<Character> qci2 = new ArrayList<Character>();
		qci1.add(qci.get(0));
		qci2.add(qci.get(1));
		
		for(int i=1; i<=numOfUes/2;i++){
			ues.add("UE" + i);
		}
		for(int i=129; i<129+numOfUes/2;i++){
			ues2.add("UE" + i);
		}
		try {
			traffic.enableStreamsByNameAndQciAndDirection(Protocol.UDP,ues, qci1, TransmitDirection.BOTH);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			traffic.enableStreamsByNameAndQciAndDirection(Protocol.UDP,ues2, qci2, TransmitDirection.BOTH);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	private void enableNeededStreams(){
		ArrayList<String> ues = new ArrayList<>();
		ArrayList<String> ues2 = new ArrayList<>();
		ArrayList<Character> qci1 = new ArrayList<Character>();
		ArrayList<Character> qci2 = new ArrayList<Character>();
		qci1.add(qci.get(0));
		qci2.add(qci.get(1));
		
		for(int i=1; i<=numOfUes/2;i++){
			ues.add("UE" + i);
		}
		for(int i=numOfUes/2+1; i<=numOfUes;i++){
			ues2.add("UE" + i);
		}
		try {
			traffic.enableStreamsByNameAndQciAndDirection(Protocol.UDP,ues, qci1, TransmitDirection.BOTH);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			traffic.enableStreamsByNameAndQciAndDirection(Protocol.UDP,ues2, qci2, TransmitDirection.BOTH);
		} catch (Exception e) {
			e.printStackTrace();
		}
	
		try {
			traffic.startTrafficSTC();
			report.report("Traffic started. Waiting 16 seconds until ues will stabilize");
			GeneralUtils.unSafeSleep(16*1000);
		} catch (Exception e) {
			e.printStackTrace();
			report.report("Failed to start traffic", Reporter.WARNING);
		}
	}

	private String getStringCalculator() {
		try {
			RadioParameters radioParams = enodeBConfig.getRadioProfile(dut);
			GeneralUtils.printToConsole(radioParams.getCalculatorString("PTMP"));
			String calc = radioParams.getCalculatorString("PTMP");
			return calc;
		} catch (Exception e) {
			report.report("Error: " + e.getMessage());
		}
		return null;
	}

	private void printDataOfUes(){
		GeneralUtils.startLevel("Print data of UEs");
		ArrayList<String> sortedList = new ArrayList<String>(allStreams);
		Collections.sort(sortedList);
		for (String stream : sortedList) {
			report.reportHtml(stream, streamList.printTablesHtmlForStream(stream), true);
		}
		GeneralUtils.stopLevel();
	}
	
	private void statsToReport(String group, double min, double max, double avg, double dev, String unit, boolean old){
		GeneralUtils.startLevel("Stats for the run for "+group);
		report.report("Mininum: "+min+" "+unit);
		report.report("Maximum: "+max+" "+unit);
		report.report("Average: "+avg+" "+unit);
		report.report("Standard deviation: "+dev+" "+unit);
		if(old){
			GeneralUtils.stopLevel();
		}
	}
	
	private void initParallel(){
		try {
			commandsThread = new ParallelCommandsThread(commands, dut, null, null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private double convertTo3DigitsAfterPoint(double val){
		double temp = Math.floor(val*1000);
		double tempDouble = temp/1000.0;
		return tempDouble;
	}
	
	private void failTreatment(){
		report.report("Failed to run test. Retrying up to "+NUM_OF_RETRIES+" times", Reporter.WARNING);
		retry++;
		stopTrafficAndSimulator();	
		if(retry==2){
			stopParallelAndAttachFile();
			long recoverPrint = TIME_TO_RECOVER/60000;
			report.report("Waiting up to "+recoverPrint+" minutes for setup to recover");
			dut.waitForAllRunningAndInService(TIME_TO_RECOVER);
		}
	}
	
	private void stopTrafficAndSimulator(){
		report.report("Stopping traffic");
		traffic.stopTraffic();
		uesim.stop();
	}
	
	private enum PfsType{
		OldPfs,
		NewPfs;
	}
	
	@ParameterProperties(description = "Name of Enodeb Which the test will be run On")
	public void setDUT(String dut) {
		this.dut = (EnodeB) SysObjUtils.getInstnce().initSystemObject(EnodeB.class, false, dut).get(0);
	}

	public String getDUT() {
		return this.dut.getNetspanName();
	}

	@Override
	public void end() {
		stopParallelAndAttachFile();
		if (uesim.isTestRunning()){
			uesim.stop();
		}
		for (EnodeB enb : enbInTest) {
			if(enb != dut){
				peripheralsConfig.changeEnbState(enb, EnbStates.IN_SERVICE);		
			}
		}
		if(tcpLogger){
			dut.lteCli("logger threshold set process=enodeb0 client=UL_PFS cli=5");
			tcpLogger = false;
		}
		super.end();
	}
}

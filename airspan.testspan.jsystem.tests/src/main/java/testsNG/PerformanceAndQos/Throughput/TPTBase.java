package testsNG.PerformanceAndQos.Throughput;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import Attenuators.AttenuatorSet;
import EnodeB.EnodeB;
import EnodeB.Ninja;
import EnodeB.Components.UEDist;
import Entities.ITrafficGenerator.Protocol;
import Entities.StreamParams;
import Netspan.API.Enums.EnbStates;
import Netspan.Profiles.RadioParameters;
import TestingServices.TestConfig;
import UE.UE;
import UE.UESimulator;
import UE.VirtualUE;
import Utils.GeneralUtils;
import Utils.GeneralUtils.HtmlFieldColor;
import Utils.GeneralUtils.HtmlTable;
import Utils.Pair;
import Utils.SetupUtils;
import Utils.StreamList;
import Utils.SysObjUtils;
import Utils.Reporters.GraphAdder;
import Utils.WatchDog.WatchDogManager;
import Utils.WatchDog.commandWatchDLAndUL;
import jsystem.framework.ParameterProperties;
import jsystem.framework.report.Reporter;
import jsystem.framework.report.ReporterHelper;
import testsNG.TestspanTest;
import testsNG.Actions.EnodeBConfig;
import testsNG.Actions.PeripheralsConfig;
import testsNG.Actions.SoftwareUtiles;
import testsNG.Actions.Traffic;
import testsNG.Actions.TrafficCapacity;
import testsNG.Actions.Utils.CalculatorMap;
import testsNG.Actions.Utils.ParallelCommandsThread;

public class TPTBase extends TestspanTest {

	public int Load_UL;
	public int Load_DL;
	public double Expected_UL;
	public double Expected_DL;
	public int RunTime;
	private boolean use_Load_UL = false;
	private boolean use_Load_DL = false;
	private boolean use_Expected_UL = false;
	private boolean use_Expected_DL = false;
	private boolean use_Runtime = false;

	public final static String[] debugCommands = { "a", "b" };
	public Long TEST_TIME_MILLIS = new Long(2 * 60 * 1000);
	public Long TEST_TIME_SHORT = new Long(2 * 60 * 1000);
	public Long TEST_TIME_LONG = new Long(10 * 60 * 1000);
	public EnodeB dut;
	protected String testName;
	protected boolean isQci9;
	protected static final int HALT_STREAM_PARAM = 50 * 1000;
	protected static final int ATONEMENT_TIME_PARAM = 20 * 1000;
	protected static final double PRECENT_OF_UE_RESET = 30;
	protected static final double QOS_THRESHOLD_PRECENT = 0.5;
	protected static final double TPT_THRESHOLD_PRECENT = 0.9;
	protected String attenuatorSetName = "rudat_set";
	protected AttenuatorSet attenuatorSetUnderTest;
	protected Boolean testIsNotDoneStatus = true;
	protected ArrayList<StreamParams> streams = new ArrayList<StreamParams>();
	protected HashMap<Long, StreamParams> portRatesMap = new HashMap<Long, StreamParams>();
	protected ArrayList<ArrayList<StreamParams>> listOfStreamList = new ArrayList<ArrayList<StreamParams>>();
	protected ArrayList<StreamParams> haltedStreamsInLastSample = new ArrayList<StreamParams>();
	protected HashMap<String, StreamParams> haltedStreamsInTest = new HashMap<String, StreamParams>();
	protected List<String> commandList = new ArrayList<String>();
	protected ArrayList<Pair<UE, Integer>> SUTUECellList;
	protected ArrayList<UE> ueList;
	protected ArrayList<String> ueNameListStc;
	protected PeripheralsConfig peripheralsConfig;
	protected ParallelCommandsThread syncCommands;
	protected RadioParameters radioParams;
	protected Traffic trafficSTC;
	protected EnodeBConfig enbConfig;
	protected Integer packetSize;
	protected Integer numberParallelStreams;
	protected Double windowSizeInKbits;
	protected UEDist ueDist;
	protected StreamList debugPrinter = new StreamList();
	protected ArrayList<String> stringArrayStreamNames;
	protected String streamsMode;
	protected Pair<Double, Double> portsLoadPair;
	protected ArrayList<Character> qci = new ArrayList<Character>();
	protected int numberOfResets = 0;
	protected long EndTestUlMin = Long.MAX_VALUE;
	protected long EndTestUlMax = 0;
	protected long EndTestDlMin = Long.MAX_VALUE;
	protected long EndTestDlMax = 0;
	protected boolean resetUEs = false;
	protected boolean resetTestBol;
	protected boolean resetDueToMultiHaltStreams = false;
	protected boolean exceptionThrown = false;
	protected boolean printResultsForTest = true;
	protected boolean restartTime = false;
	protected boolean isCaTest = false;
	protected WatchDogManager wd;
	protected commandWatchDLAndUL DLULwd;
	protected int cellNumber = 0;

	protected boolean runWithDynamicCFI = false;
	protected Protocol protocol;
	protected Long startingTestTime;

	@Override
	public void init() throws Exception {
		if (enbInTest == null) {
			enbInTest = new ArrayList<EnodeB>();
		}
		enbInTest.add(dut);
		super.init();
		GeneralUtils.startLevel("Test Init");
		peripheralsConfig = PeripheralsConfig.getInstance();
		enbConfig = EnodeBConfig.getInstance();
		try {
			attenuatorSetUnderTest = AttenuatorSet.getAttenuatorSet(attenuatorSetName);
		} catch (Exception e) {
			attenuatorSetUnderTest = null;
			report.report("Attenuator Fail to initialize");
		}
		trafficSTC = Traffic.getInstance(SetupUtils.getInstance().getAllUEs());
		GeneralUtils.stopLevel();
		runWithDynamicCFI = TestConfig.getInstace().getDynamicCFI();
		isCaTest = false;
		numberParallelStreams = null;
		windowSizeInKbits = null;

		TEST_TIME_MILLIS = setTime(TEST_TIME_SHORT);
		this.protocol = Protocol.UDP;
	}

	protected void watchDogInit() {
		try {
			wd = WatchDogManager.getInstance();
			// DLULwd = new commandWatchDLAndUL(dut);
			// wd.addCommand(DLULwd);
		} catch (Exception e) {
			e.printStackTrace();
			report.report("Could not Init DL and UL command", Reporter.WARNING);
			wd = null;
			return;
		}

	}

	/**
	 * method is changing all Nodes to Out of Service except the first one. in
	 * Addition getting radio profile and purifying the Calculator according to the
	 * current test running.
	 * 
	 * @author Shahaf Shuhamy
	 */
	protected void preTest() {
		printTestParmas();
		if (attenuatorSetUnderTest != null) {
			peripheralsConfig.setAttenuatorSetValue(attenuatorSetUnderTest,
					attenuatorSetUnderTest.getDefaultValueAttenuation());
		} else {
			report.report("There is no attenuator set ");
		}
		changeOtherENBsToOOS();
		getRadioProfile();

		if (runWithDynamicCFI)
			enbConfig.enableDynamicCFI(this.dut);
		else
			enbConfig.disableDynamicCFI(this.dut);

		printResultsForTest = true;
		resetTestBol = false;
		exceptionThrown = false;
	}

	private void printTestParmas() {
		GeneralUtils.startLevel("Test Parameters");
		if (this.use_Load_UL == true) {
			report.report("load up link : " + this.Load_UL + "%");
		}
		if (this.use_Load_DL == true) {
			report.report("load down link : " + this.Load_DL + "%");
		}
		if (this.use_Expected_UL == true) {
			report.report("expected Up Link : " + this.Expected_UL + "%");
		}
		if (this.use_Expected_DL == true) {
			report.report("expected Down Link : " + this.Expected_DL + "%");
		}
		GeneralUtils.stopLevel();
	}

	/**
	 * change enodeB state in netspan or snmp if not succeeded.
	 * 
	 * @author Shahaf Shuhamy
	 * @throws IOException
	 */
	protected void changeOtherENBsToOOS() {
		GeneralUtils.startLevel("EnodeBs settings");
		peripheralsConfig.changeEnbState(dut, EnbStates.IN_SERVICE);
		ArrayList<EnodeB> toOOS = (ArrayList<EnodeB>) enbInSetup.clone();
		toOOS.remove(dut);
		for (EnodeB enb : toOOS) {
			if (!peripheralsConfig.changeEnbState(enb, EnbStates.OUT_OF_SERVICE)) {
				report.report("failed to set enodeB " + enb.getNetspanName() + " out of service", Reporter.WARNING);
			} else {
				report.report("EnodeB " + enb.getNetspanName() + " was set out of service");
			}
		}
		GeneralUtils.stopLevel();
	}

	/**
	 * getting radio parameters from netspan or snmp
	 * 
	 * @author Shahaf Shuahmy
	 * @throws IOException
	 */
	protected void getRadioProfile() {
		GeneralUtils.startLevel("Radio Parameters Initialize:");
		try {
			radioParams = enbConfig.getRadioProfile(dut);
			GeneralUtils.printToConsole(radioParams.getCalculatorString(streamsMode));
		} catch (Exception e) {
			report.report("Error: " + e.getMessage());
		}
		GeneralUtils.stopLevel();
	}

	/**
	 * converting arraylist of UES to arraylist of Strings
	 * 
	 * @author Shahaf Shuhamy
	 * @param ueList2
	 * @return
	 */
	protected ArrayList<String> convertUeToNamesList(ArrayList<UE> ueList2) {
		ArrayList<String> ueList = new ArrayList<String>();
		for (UE ue : ueList2) {
			ueList.add("UE" + ue.getName().replaceAll("\\D+", "").trim());
		}
		return ueList;
	}

	/**
	 * reseting parameters for test starting sync commands in a diffrent Thread
	 * starting traffic and sample results per port and per stream
	 * 
	 * @author Shahaf Shuhamy
	 */
	protected void TestProcess() {
		changeCPUWDStatus(false);
		while (testIsNotDoneStatus) {
			resetParams();
			try {
				if (!startTrafficAndSample()) {
					testIsNotDoneStatus = false;
					resetTestBol = false;
					exceptionThrown = true;
					GeneralUtils.stopAllLevels();
				}
			} catch (Exception e) {
				report.report("Stopping Parallel Commands From Java Exception" + e.getMessage());				
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
				if (syncCommands != null) {
					report.report("Stopping Parallel Commands");
					syncCommands.stopCommands();
				}
				makeThreadObjectFinish();
				if (syncCommands != null) {
					report.report("Commands File Path: " + syncCommands.getFile());
				}
				testIsNotDoneStatus = true;
				// stop level
				try {
					if (syncCommands != null) {
						syncCommands.moveFileToReporterAndAddLink();
					}
					report.report("Stopping Traffic");
					trafficSTC.stopTraffic();
					trafficSTC.addResultFilesToReport(String.valueOf(numberOfResets));
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
			if (syncCommands != null) {
				syncCommands.stopCommands();
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		makeThreadObjectFinish();
		try {
			if (syncCommands != null) {
				syncCommands.moveFileToReporterAndAddLink();
			}
		} catch (Exception e) {
			report.report("Exception in ReporterHelper.copyFileToReporterAndAddLink could not attach Command File");
			e.printStackTrace();
		}
		changeCPUWDStatus(true);
	}

	/**
	 * make Thread of commands to end his process in order to continue the program.
	 * 
	 * @author Shahaf Shuhamy
	 */
	protected void makeThreadObjectFinish() {
		try {
			if (syncCommands != null) {
				syncCommands.join();
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * restarting number of recovery try streams Array list restart list of streams
	 * for future use restarting
	 * 
	 * @author Shahaf Shuhamy
	 */
	protected void resetParams() {
		report.report("Resetting Test Parameters");
		// numberOfRecoverys = 0;
		streams = new ArrayList<StreamParams>();
		listOfStreamList = new ArrayList<ArrayList<StreamParams>>();
	}

	/**
	 * @author Shahaf Shuhamy uses String [] command list and starting another
	 *         Thread to commands in cli and put them in another File located in
	 *         root folder
	 */
	protected void syncGeneralCommands() {
		commandList.add("ue show link");
		commandList.add("ue show rate");
		commandList.add("rlc show amdlstats");
		commandList.add("qci show rate");
		commandList.add("system show memory");
		commandList.add("system show error");
		commandList.add("system show phyerror");
		commandList.add("system show compilationtime");
		commandList.add("cell show operationalstatus");
		commandList.add("db get PTPStatus");

		for (int numberOfTrys = 0; numberOfTrys < 2; numberOfTrys++) {
			try {
				syncCommands = new ParallelCommandsThread(commandList, dut, null, null);
				break;
			} catch (Exception e) {
				report.report("Cannot initialize streamers in ParallelCommandsThread Class, try number" + numberOfTrys);
				if (numberOfTrys == 2) {
					report.report("could not init Commands in parallel", Reporter.WARNING);
				}
			}
		}
		if (syncCommands != null) {
			report.report("Starting parallel commands");
			syncCommands.start();
		}
	}

	protected boolean startTrafficInTest() throws Exception {

		resetTestBol = false;
		trafficSTC.setactiveStreamsArray(stringArrayStreamNames);

		// init and start traffic
		if (!trafficSTC.initTrafficWithNoStartTraffic(TrafficCapacity.FULLTPT)) {
			report.report("start Traffic failed", Reporter.FAIL);
			throw new Exception("start Traffic Failed from stc class");
		}

		GeneralUtils.startLevel("Disable un-needed streams");
		trafficSTC.disableUnneededStreams(ueNameListStc, qci);
		// if the last ue deleted and there is nothing to check!
		if (ueNameListStc.size() == 0) {
			report.report("No UEs in Test");
			GeneralUtils.stopLevel();
			throw new Exception("No UES in Test");
		}
		GeneralUtils.stopLevel();

		GeneralUtils.startLevel("Turning on ues in setup");
		peripheralsConfig.startUEsOnlySnmp(SetupUtils.getInstance().getAllUEs());
		GeneralUtils.stopLevel();

		GeneralUtils.startLevel("Disable un-needed UEs");
		disableUEsNotInTest(ueNameListStc, SetupUtils.getInstance().getAllUEs());
		GeneralUtils.stopLevel();

		// init number fo parallel streams
		GeneralUtils.startLevel("init parallel streams");
		trafficSTC.initNumberParallelStreams(numberParallelStreams, streams);
		GeneralUtils.stopLevel();

		// init window size
		GeneralUtils.startLevel("init window size");
		trafficSTC.initWindowSize(windowSizeInKbits, streams);
		GeneralUtils.stopLevel();

		// init Frame Size
		GeneralUtils.startLevel("init Frame Size and taking off un-needed streams");
		trafficSTC.initFrameSize(packetSize, streams);
		GeneralUtils.stopLevel();

		// init Protocol
		GeneralUtils.startLevel("init Protocol (" + this.protocol + ")");
		if (!trafficSTC.initProtocol(this.protocol)) {
			reason = "Cant set traffic protocol.";
			report.report("Cant start traffic. protocol not supported.", Reporter.WARNING);
			GeneralUtils.stopLevel();
			return false;
		}
		GeneralUtils.stopLevel();

		GeneralUtils.startLevel("setting port load according to Radio profile");
		portsLoadPair = calculateAndConfigPortLoads();
		GeneralUtils.stopLevel();

		// Save Configurate File and ReStarting Traffic
		report.report("Save Config File and Start Traffic");
		uploadConfigFileToReport();
		trafficSTC.saveConfigFileAndStart();

		if (!peripheralsConfig.checkIfAllUEsAreConnectedToNode(ueList, dut)) {
			report.report("test will go on with a warning about UES", Reporter.WARNING);
			reason = "UE not connected to DUT";
		}

		if (ueList.get(0) instanceof VirtualUE) {
			UESimulator uesim = UESimulator.getInstance();
			uesim.start();
			checkForARP();
		}
		report.report("Starting TPT-" + streamsMode + " with " + ueNameListStc.size() + " UEs, for "
				+ SoftwareUtiles.milisToFormat(TEST_TIME_MILLIS));
		GeneralUtils.startLevel("Sampling Results each second");
		// loop for time of TEST_TIME
		syncGeneralCommands();
		watchDogInit();
		report.report("waiting 20 seconds for traffic to stabilize");
		GeneralUtils.unSafeSleep(1000 * 20);
		return true;
	}

	/**
	 * @author Shahaf Shuhamy start traffic init frame size save configurate file
	 *         and starting traffic all over again then starting a loop for 1 minute
	 *         : sampling and checking if sample is ok
	 * @throws IOException
	 */
	protected boolean startTrafficAndSample() throws Exception {
		if (!startTrafficInTest()) {
			return false;
		}
		startingTestTime = System.currentTimeMillis();
		long resetCounters = startingTestTime;

		while (System.currentTimeMillis() - startingTestTime <= TEST_TIME_MILLIS) {
			if(System.currentTimeMillis()-resetCounters>=30*60*1000){
				resetCounters = System.currentTimeMillis();
				double temp = Math.floor(((System.currentTimeMillis() - startingTestTime)/ 1000 / 60.0)*100);
				double tempDouble = temp/100.0;
				report.report("Time elapsed: " + tempDouble + " minutes.");
			}
			sampleResultsStatusOk();

			if (restartTime) {
				startingTestTime = System.currentTimeMillis();
			}

			// if exception thrown = Connection closed -> stop test!
			if (exceptionThrown) {
				if (reason == "") {
					reason = "TRAFFIC ERROR";
				}

				report.report("Performance Error in Test! ,reason: " + reason);
				break;
			}
			try {
				synchronized (this) {
					this.wait(1000);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			if (resetUEs) {
				resetUEs = false;
				resetTestBol = true;
				report.report("Resetting UES");
				if (ueList.get(0) instanceof VirtualUE) {
					UESimulator uesim = UESimulator.getInstance();
					uesim.stop();
				} else {
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
					return true;
				} else {
					resetTestBol = false;
					startingTestTime = TEST_TIME_MILLIS;
					break;
				}
			}
		}
		GeneralUtils.stopLevel();

		if (ueList.get(0) instanceof VirtualUE) {
			UESimulator uesim = UESimulator.getInstance();
			uesim.stop();
		}

		testIsNotDoneStatus = false;
		return true;
	}

	/**
	 * according to radio profile, calculate port load and set it.
	 */
	private Pair<Double, Double> calculateAndConfigPortLoads() {
		CalculatorMap calc = new CalculatorMap();

		Pair<Double, Double> trafficValuPair;
		try {
			trafficValuPair = calc.getDLandULconfiguration(radioParams);
		} catch (Exception e) {
			trafficValuPair = null;
			e.printStackTrace();
		}

		if (dut instanceof Ninja)
			trafficValuPair = ConvertToNinjaValues(trafficValuPair);

		if (trafficValuPair != null) {
			trafficValuPair = calculateTrafficLoadAccordingToMultiCell(trafficValuPair);
			trafficSTC.trafficLoadSet(trafficValuPair);
		}
		return trafficValuPair;
	}

	private Pair<Double, Double> ConvertToNinjaValues(Pair<Double, Double> trafficValuPair) {

		Pair<Double, Double> pair = new Pair<Double, Double>(trafficValuPair.getElement0() * 0.95 / 1.1,
				trafficValuPair.getElement1() * 0.61 / 1.1);
		return pair;
	}

	/**
	 * stopping all UEs
	 * 
	 * @param ueNameListStc2
	 * @param ueList2
	 */
	protected void disableUEsNotInTest(ArrayList<String> uesAllowedInTest, ArrayList<UE> ueList2) {
		// ueNameList [UE1,UE2....]
		// all UEs to map
		HashMap<String, UE> ueMap = new HashMap<String, UE>();
		for (UE ue : ueList2) {
			String ueNumber = GeneralUtils.removeNonDigitsFromString(ue.getName());
			ueMap.put(ueNumber, ue);
		}

		// remove all allowed from map
		for (String ueName : uesAllowedInTest) {
			String ueAllowedNumber = GeneralUtils.removeNonDigitsFromString(ueName);
			if (ueMap.containsKey(ueAllowedNumber)) {
				ueMap.remove(ueAllowedNumber);
			}
		}

		if (ueMap.isEmpty()) {
			GeneralUtils.printToConsole("all UEs are in start for this setup");
		}

		// stop all UEs in Map
		for (String ue : ueMap.keySet()) {
			GeneralUtils.printToConsole("ue to stop : ue" + ue);
			if (!ueMap.get(ue).stop()) {
				report.report("UE " + ue + " did not stopped!", Reporter.WARNING);
			} else {
				report.report("UE " + ue + " has stopped!");
			}
		}

	}

	/**
	 * return a pair of values where the first represents DownLoad and the 2nd
	 * Upload.
	 * 
	 * @param trafficValuPair
	 * @return
	 */
	private Pair<Double, Double> calculateTrafficLoadAccordingToMultiCell(Pair<Double, Double> trafficValuPair) {
		// in case SNMP fail in 2nd time - test can go wrong.
		if (cellNumber != 2) {
			cellNumber = enbConfig.getNumberOfActiveCells(dut);
		}
		double cellNumDoubl = Double.valueOf(cellNumber);
		Pair<Double, Double> retValues;
		Double multiCellWA = 1.0;
		Double ulParamModifier = 1.0;
		Double dlParamModifier = 1.0;
		retValues = Pair.createPair(trafficValuPair.getElement0(), trafficValuPair.getElement1());
		if (this.use_Load_DL && this.use_Load_UL) {
			ulParamModifier = this.Load_UL / 100.0;
			dlParamModifier = this.Load_DL / 100.0;

			String[] dlul = getCalculatorPassCriteria(radioParams);
			double calcPassCritDL = Double.parseDouble(dlul[0]);
			double calcPassCritUL = Double.parseDouble(dlul[1]);

			// get Custom Calculator loads
			report.report("using custom loads -> getting calculator values => DL : " + calcPassCritDL + ", UL : "
					+ calcPassCritUL);
			retValues = Pair.createPair(calcPassCritDL, calcPassCritUL);
			// multiple with custom Loads From JSystem
			retValues = Pair.createPair(retValues.getElement0() * dlParamModifier,
					retValues.getElement1() * ulParamModifier);
			report.report(
					"After Multiple with Custom Modifiers => DL : " + String.format("%.2f", retValues.getElement0())
							+ ", UL : " + String.format("%.2f", retValues.getElement1()));
			report.report("checking if CA test and PTP test");
			if (!streamsMode.equals("PTP")) {
				retValues = Pair.createPair(retValues.getElement0() * cellNumber, retValues.getElement1() * cellNumber);
			} else {
				if (isCaTest) {
					retValues = Pair.createPair(retValues.getElement0() * cellNumber, retValues.getElement1());
				}
			}
			report.report("After checking for CA PTP test => DL : " + String.format("%.2f", retValues.getElement0())
					+ ", UL : " + String.format("%.2f", retValues.getElement1()));

		} else {
			report.report("using Auto Generated loads => DL : " + retValues.getElement0() + ", UL : "
					+ retValues.getElement1());
			report.report("checking if CA test and PTP test");
			if (!streamsMode.equals("PTP")) {
				if (cellNumDoubl > 1) { // multiple with MC WA
					multiCellWA = 1.33;
				}
				retValues = multipleValuesByCellNumbers(retValues, cellNumDoubl * multiCellWA);
			} else {
				if (isCaTest) {
					retValues = Pair.createPair(retValues.getElement0() * cellNumDoubl, retValues.getElement1());

				}
			}
			report.report("after checking for CA PTP test => DL : " + retValues.getElement0() + ", UL : "
					+ retValues.getElement1());
		}
		return retValues;
	}

	private Pair<Double, Double> multipleValuesByCellNumbers(Pair<Double, Double> values, Double numberOfCells) {
		Pair<Double, Double> retValues = Pair.createPair(values.getElement0() * numberOfCells,
				values.getElement1() * numberOfCells);
		report.report("loads multiple the number of cells per cell => DL : " + retValues.getElement0() + ", UL : "
				+ retValues.getElement1());
		return retValues;
	}

	protected void uploadConfigFileToReport() {
		// insert method to make tcc file or XML
		File uploadToReportConfigFile = trafficSTC.getUploadedConfigFile();
		try {
			ReporterHelper.copyFileToReporterAndAddLink(report, uploadToReportConfigFile, "ConfigFile");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * sends ARP in STC - real method is "startArpNd(boolean for exception result,
	 * portNames as String)
	 * 
	 * @author Shahaf Shuhamy
	 */
	protected void checkForARP() {
		GeneralUtils.startLevel("Sending Arp STC");
		try {
			// check for ARP method.
			trafficSTC.startArpNd("DL", "UL");
		} catch (Exception e) {
			e.printStackTrace();
		}
		GeneralUtils.stopLevel();
	}

	/**
	 * Using IP power turning off the UE and turning it back on
	 * 
	 * @author Shahaf Shuhamy
	 * @param ueList
	 *            WARNING Synchronized Method for Sleep after Rebooting UEs
	 * @param locationOfUeRebooted
	 */
	protected synchronized void rebootUEs(ArrayList<UE> ueList) {
		try {
			if (attenuatorSetUnderTest != null) {
				peripheralsConfig.setAttenuatorSetValue(attenuatorSetUnderTest,
						attenuatorSetUnderTest.getDefaultValueAttenuation());
			} else {
				report.report("there is no attenuator set to reboot with!");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		peripheralsConfig.rebootUEs(ueList);
	}

	/**
	 * @author Shahaf Shuhamy sample statistics according to port sample statistics
	 *         according to stream checking for halt stream if halt stream -> check
	 *         which one
	 * @param startingTestTime
	 * @return
	 */
	public void sampleResultsStatusOk() {
		restartTime = false;
		ArrayList<StreamParams> haltedStreams = null;
		ArrayList<ArrayList<StreamParams>> sampleArrayList = null;
		try {
			sampleArrayList = samplePortsAndStreamsFromSTC();
		} catch (Exception e) {
		}
		// Check Traffic Halt only for non UDP tests
		if (this.protocol != Protocol.TCP) {
			if (trafficSTC.checkForHalt(HALT_STREAM_PARAM, streams)) {
				try {
					GeneralUtils.startLevel("Traffic halt found");
					report.report("found halt streams", Reporter.WARNING);
					trafficSTC.takeActionAfterFoundHaltStream();
					report.report("waiting 20 seconds");
					GeneralUtils.unSafeSleep(20 * 1000);

					streams = null;
					streams = new ArrayList<StreamParams>();
					report.report("last sample was 0 - Re Sample one more time");
					samplePortsAndStreamsFromSTC();
					// restartTime = true;

					if (trafficSTC.checkForHalt(HALT_STREAM_PARAM, streams)) {
						haltedStreams = trafficSTC.checkStreamsForHalter(HALT_STREAM_PARAM, streams);
						reportForHalted(haltedStreams);
						report.report("Those Streams Are in Halt Status: " + printStreamName(haltedStreams));

						// number of halted UEs is grater than Test Threshold.
						double totalUESPercent = ueList.size() * PRECENT_OF_UE_RESET / 100;
						boolean uesThreashold = getNumberOfUEs(haltedStreams) <= totalUESPercent;
						if (uesThreashold) {
							report.report("disabling halted Streams since they are less than 30%.");
							// Total UEs are over than 3 OR if Test take off
							// less than 2 UEs.
							GeneralUtils.printToConsole("Total number of UEs in Setup : " + ueList.size());
							GeneralUtils.printToConsole("Number of UEs in Test Currently : " + ueNameListStc.size());
							if (ueNameListStc.size() > 3 && ueList.size() - ueNameListStc.size() < 2) {
								checkUEsRelationsWithStreams(haltedStreams);
							} else {
								report.report("Test UEs number is not enough For Throughtput! - Failing Test",
										Reporter.FAIL);
								exceptionThrown = true;
								return;
							}

						} else {
							resetUEs = true;
						}
						resetDueToMultiHaltStreams = true;
					}

				} catch (Exception e) {
					report.report("interrupted in SamepleResultsStatusOk method in Traffic Class -" + e.getMessage());
					e.printStackTrace();
					report.report("Error during test", Reporter.FAIL);
					resetDueToMultiHaltStreams = true;
					exceptionThrown = true;
				}finally{
					GeneralUtils.stopLevel();
				}
			}
			
		}
		// check if more then 30% of streams are halted
		addSamplesToListOfStreamList(sampleArrayList);
		streams = null;
		streams = new ArrayList<StreamParams>();
	}

	protected void addSamplesToListOfStreamList(ArrayList<ArrayList<StreamParams>> sampleArrayList) {
		for (ArrayList<StreamParams> sample : sampleArrayList) {
			this.listOfStreamList.add(sample);
		}
	}

	protected int getNumberOfUEs(ArrayList<StreamParams> haltedStreams) {
		ArrayList<Integer> uesWithHalts = new ArrayList<>();
		for (StreamParams stream : haltedStreams) {
			String ueName = stream.getName();
			Integer ueNumber = Integer.valueOf(ueName.substring(5, stream.getName().length() - 1));

			if (!uesWithHalts.contains(ueNumber)) {
				uesWithHalts.add(ueNumber);
			}
		}
		return uesWithHalts.size();
	}

	protected void reportForHalted(ArrayList<StreamParams> halts) {
		Set<UE> uesToCheck = new HashSet<UE>();
		String name = "";
		for (StreamParams haltedStream : halts) {
			name = haltedStream.getName().substring(3, haltedStream.getName().length() - 1);
			uesToCheck.add(getUeToCheck(name));
		}
		ArrayList<UE> listToCheck = new ArrayList<UE>(uesToCheck);
		try {
			if (!name.equals("")) {
				try {
					peripheralsConfig.checkUesConnectionEPC(listToCheck);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			report.report("Failed to check connection to epc");
		}
	}

	private UE getUeToCheck(String name) {
		for (UE ue : ueList) {
			String ueName = ue.getName().replaceAll("\\D+", "").trim();
			String ueFromHalt = name.replaceAll("\\D+", "").trim();
			if (ueName.equals(ueFromHalt)) {
				return ue;
			}
		}
		return null;
	}

	/**
	 * traverse halted streams list, extracting UE name :"UEx" updating ues in test
	 * Ue list.
	 * 
	 * @author Shahaf Shuhamy
	 * @param haltedStreams
	 * @param streams
	 */
	public void checkUEsRelationsWithStreams(ArrayList<StreamParams> haltedStreams) {
		String ueFromSTC;
		for (StreamParams haltedStream : haltedStreams) {

			// extract UE name : "UEx"
			ueFromSTC = haltedStream.getName().substring(3, haltedStream.getName().length() - 1);

			// validations and remove
			GeneralUtils.printToConsole("ue name format : " + ueFromSTC);
			if (ueNameListStc.contains(ueFromSTC)) {
				ueNameListStc.remove(ueFromSTC);
				GeneralUtils.printToConsole("ue - " + ueFromSTC + " Has been removed from UE list ");
				report.report("UE : " + ueFromSTC + " has been removed", Reporter.WARNING);
			}
		}
	}

	protected void removeLastSampleAndReSampleOnce() throws Exception {
		report.report("removing last sample");
		streams.clear();
		report.report("re-Sample");
		ArrayList<ArrayList<StreamParams>> sampleArrayList = samplePortsAndStreamsFromSTC();
		addSamplesToListOfStreamList(sampleArrayList);
	}

	/**
	 * @author Shahaf Shuhamy sample ports and streams to "streams" list
	 * @return
	 * @throws Exception
	 */
	protected ArrayList<ArrayList<StreamParams>> samplePortsAndStreamsFromSTC() throws Exception {
		trafficSTC.sampleCurrentStatistics(packetSize, portRatesMap, debugPrinter);
		return trafficSTC.sampleForEachStream(packetSize, streams);
	}

	/**
	 * @author Shahaf Shuhamy checking if there are Streams with 0 Sample in order
	 *         to re-Sample.
	 * @param streams2
	 * @return
	 */
	protected int numberOfZeroRxStreams(ArrayList<StreamParams> streams2) {
		int counter = 0;
		for (StreamParams stream : streams2) {
			if (stream.getRxRate() == 0)
				counter++;
		}
		return counter;
	}

	/**
	 * @author Shahaf Shuhamy checking if the streamHalted param is in a group of
	 *         more then 30% then sum of all streams.
	 * @param ArrayList<StreamParams>
	 *            haltedStreams
	 * @param StreamParams
	 *            streamHalted
	 * @return
	 */
	protected boolean checkListThreshold(ArrayList<StreamParams> haltedStreams) {
		report.report("checking if more then " + PRECENT_OF_UE_RESET + "% of streams in halt status");
		if (checkThresholdPrecentOfStreamsHalt(haltedStreams)) {
			report.report("Stream Halt number of streams is: " + PRECENT_OF_UE_RESET + "% or higher");
			return true;
		} else {
			report.report("Streams in Halt Status are below threshold");
			return false;
		}
	}

	/**
	 * @author Shahaf Shuhamy printing stream name
	 * @param ArrayList<StreamParams>
	 *            haltedStreams
	 * @return
	 */
	protected String printStreamName(ArrayList<StreamParams> haltedStreams) {
		StringBuilder sb = new StringBuilder();
		for (StreamParams stream : haltedStreams) {
			sb.append(stream.getName());
			sb.append(" ");
		}
		return sb.toString();
	}

	/**
	 * @author Shahaf Shuhamy checking the stream halt Threashold according to the
	 *         List
	 * @param haltedStreams
	 * @return
	 */
	protected Boolean checkThresholdPrecentOfStreamsHalt(ArrayList<StreamParams> haltedStreams) {
		if (haltedStreams.size() >= streams.size() * PRECENT_OF_UE_RESET / 100) {
			return true;
		}
		return false;
	}

	/**
	 * @author Shahaf Shuhamy prints port tabels with StreamList Class print tables
	 *         per stream with StreamList Class print Debug Stream Tables with
	 *         StreamList Class Comparing results to calculator file.
	 * @param passCriteria
	 */
	protected void AfterTest(double passCriteria) {
		validateRadioProfile();
		printDLandULAverageTableAndCounters();
		terminateWatchDog();
		// stop traffic
		report.report("Stop traffic");
		trafficSTC.stopTraffic();

		// Do after Test Things
		setENBsToService();
		setAllUEsToStart();
		if (exceptionThrown) {
			report.report("Exception failed the test - No Results");
		} else {
			sumAndPrintTablesPerPort();
			printPerStreamTables(listOfStreamList);

			try {
				GeneralUtils.printToConsole("print Results state : " + printResultsForTest);
				if (printResultsForTest) {
					compareWithCalculator(debugPrinter, listOfStreamList, passCriteria);
				}
			} catch (Exception e) {
				e.printStackTrace();
				report.report("Failed to get Pass criteria Values", Reporter.FAIL);
			}

		}
		trafficSTC.addResultFilesToReport("");
	}

	private void printDLandULCounters(List<Integer> ulConters, List<Integer> dlCounters) {
		int averageDlCounter = 0;
		int averageUlCounter = 0;
		averageUlCounter = getAverageFromSingleList(ulConters);
		averageDlCounter = getAverageFromSingleList(dlCounters);
		if ((averageDlCounter > 3) || (averageUlCounter > 5)) {
			report.report("DownLink PER : " + averageDlCounter + ", UpLink CRC : " + averageUlCounter,
					Reporter.WARNING);
		} else {
			report.report("DownLink PER : " + averageDlCounter + ", UpLink CRC : " + averageUlCounter);
		}
	}

	private int getAverageFromSingleList(List<Integer> list) {
		int sum = 0;
		if (list.size() == 0)
			return 0;
		GeneralUtils.printAList(list);
		for (Integer singleNumber : list) {
			sum += singleNumber;
		}
		return sum / list.size();
	}

	protected void setAllUEsToStart() {
		report.report("starting all UES");
		ArrayList<UE> allUEs = SetupUtils.getInstance().getAllUEs();
		for (UE ue : allUEs) {
			ue.start();
		}
	}

	private void validateRadioProfile() {
		try {
			radioParams.getCalculatorString(streamsMode);
		} catch (Exception noRadio) {
			noRadio.printStackTrace();
			try {
				report.report("error in radio profile - trying to get radio profile again");
				getRadioProfile();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	protected void setENBsToService() {
		for (EnodeB enb : enbInSetup) {
			peripheralsConfig.changeEnbState(enb, EnbStates.IN_SERVICE);
			report.report("Changed EnodeB: " + enb.getNetspanName() + " To IN_Service Status");
		}

	}

	/**
	 * @author Shahaf Shuhamy printing Debug Streams
	 * @param debugPrinter
	 * @param listOfStreamList2
	 */
	protected void printDebugStreams(StreamList debugPrinter, ArrayList<ArrayList<StreamParams>> listOfStreamList2) {
		GeneralUtils.startLevel("Debug STC Counters:");
		for (StreamParams stream : listOfStreamList2.get(0)) {
			report.reportHtml(stream.getName(), debugPrinter.printTablesHtmlForStream(stream.getName()), true);
		}
		GeneralUtils.stopLevel();
	}

	/**
	 * print tables for each Stream
	 * 
	 * @author Shahaf Shuhamy
	 * @param listOfStreamList
	 */
	protected void printPerStreamTables(ArrayList<ArrayList<StreamParams>> listOfStreamList) {
		StreamList TablePrinter = new StreamList();
		ArrayList<String> headLines = new ArrayList<String>();
		headLines.add("L1bitRate[Mbit/s]");
		GeneralUtils.startLevel("Per Stream Tables");
		for (ArrayList<StreamParams> streams : listOfStreamList) {
			for (StreamParams stream : streams) {
				ArrayList<String> valuesList = new ArrayList<String>();
				valuesList.add(longToString3DigitFormat(stream.getRxRate()));
				String dateFormat = GeneralUtils.timeFormat(stream.getTimeStamp());
				TablePrinter.addValues(stream.getName(), dateFormat, headLines, valuesList);
			}
		}
		for (StreamParams stream : listOfStreamList.get(0)) {
			report.reportHtml(stream.getName(), TablePrinter.printTablesHtmlForStream(stream.getName()), true);
		}
		GeneralUtils.stopLevel();
	}

	/**
	 * @author Shahaf Shuhamy comparing streamList UL and DL to Calculator
	 * @param debugPrinter
	 * @param listOfStreamList2
	 * @param passCriteria
	 * @throws IOException
	 * @throws InvalidFormatException
	 */
	protected void compareWithCalculator(StreamList debugPrinter, ArrayList<ArrayList<StreamParams>> listOfStreamList2,
			double passCriteria) throws IOException, InvalidFormatException {
		ArrayList<Long> listUlAndDl = new ArrayList<Long>();
		Long ULrxTotal = new Long(0);
		Long DlrxTotal = new Long(0);
		listUlAndDl = getUlDlResultsFromList(ULrxTotal, DlrxTotal, listOfStreamList2);
		compareResultsWithCalculator(debugPrinter, listUlAndDl.get(0), listUlAndDl.get(1), passCriteria);
	}

	// private double passCriteriaGet(){
	// TestConfig tc = TestConfig.getInstace();
	// String passCri = tc.getpassCriteriaTPT();
	// GeneralUtils.printToConsole("trying to parse criteria value from SUT:
	// "+passCri+" to Double Value");
	// try{
	// Double doubleValue = Double.parseDouble(passCri);
	// if(doubleValue >=1 && doubleValue <= 100){
	// doubleValue /= 100.0;
	// return doubleValue;
	// }
	//
	// if((doubleValue > 100) || (doubleValue == 0)){
	// report.report("SUT passCriteria value is not legal - insert a number between
	// 1-100, using default Test Value");
	// return 0;
	// }
	//
	// return doubleValue;
	// }catch(Exception e){
	// GeneralUtils.printToConsole("Can not parse value to Double - return 0");
	// return 0;
	// }
	// }

	/**
	 * @author Shahaf Shuhamy creating a file to send to CalculatorMesurments Class
	 *         creating object from CalculatorMesurments class comparing results and
	 *         printing Test Results to reporter.
	 * @param debugPrinter
	 * @param uLrxTotal
	 * @param dlrxTotal
	 * @param passCriteria
	 * @throws IOException
	 * @throws InvalidFormatException
	 */
	protected void compareResultsWithCalculator(StreamList debugPrinter, Long uLrxTotal, Long dlrxTotal,
			double passCriteria) throws IOException, InvalidFormatException {

		if (runWithDynamicCFI)
			radioParams.setCfi("2");
		String numberOfCellsStr = "1";
		String[] dlul = getCalculatorPassCriteria(radioParams);
		double dl = Double.parseDouble(dlul[0]);
		double ul = Double.parseDouble(dlul[1]);

		if (dut instanceof Ninja) {
			passCriteria = 0.95;
			dl = portsLoadPair.getElement0();
			ul = portsLoadPair.getElement1();
		}

		if (this.protocol == Protocol.TCP) {
			dl = 0.9 * dl;
			ul = 0.9 * ul;
		}

		// Double passCeriteriaCandidate = passCriteriaGet();//TestConfig criteria
		// parameter overload ALL passCriterias
		// if(passCeriteriaCandidate != 0){
		// passCriteria = passCeriteriaCandidate;
		// GeneralUtils.printToConsole("OverAll pass criteria : "+passCriteria);
		// }

		int numberOfCells = enbConfig.getNumberOfActiveCells(dut);
		printPortSummeryBeforeTestEnds(debugPrinter, uLrxTotal, dlrxTotal);

		double ul_Divided_With_Number_Of_Streams = uLrxTotal / 1000000.0 / listOfStreamList.size();
		double dl_Divided_With_Number_Of_Streams = dlrxTotal / 1000000.0 / listOfStreamList.size();

		double ulPassCriteria = ul;
		double dlPassCriteria = dl;
		numberOfCellsStr = String.valueOf(numberOfCells);

		if (!streamsMode.equals("PTP")) {
			dlPassCriteria = dl * numberOfCells;
			ulPassCriteria = ul * numberOfCells;
		}

		if (isCaTest) {
			if (streamsMode.equals("PTP")) {
				dlPassCriteria = dl * numberOfCells;
			}
			numberOfCellsStr = "CA";
		}

		if (use_Expected_DL) {
			dlPassCriteria = modifyAndPortLinkAndPrint(dlPassCriteria, portsLoadPair.getElement0(), this.Expected_DL);
		} else {
			dlPassCriteria *= passCriteria;
			report.report("DL Threshold : " + passCriteria * 100 + "%");

		}
		if (use_Expected_UL) {
			ulPassCriteria = modifyAndPortLinkAndPrint(ulPassCriteria, portsLoadPair.getElement1(), this.Expected_UL);
		} else {
			ulPassCriteria *= passCriteria;
			report.report("UL Threshold : " + passCriteria * 100 + "%");
		}

		// print results
		String upRate = String.format("%.2f", ul_Divided_With_Number_Of_Streams);
		String calcUpRate = String.format("%.2f", ulPassCriteria);
		String downRate = String.format("%.2f", dl_Divided_With_Number_Of_Streams);
		String calcDownRate = String.format("%.2f", dlPassCriteria);

		report.report("number of cells : " + numberOfCellsStr);
		report.addProperty("double_DL_" + String.valueOf(packetSize),
				String.valueOf(dl_Divided_With_Number_Of_Streams));
		report.addProperty("double_UL_" + String.valueOf(packetSize),
				String.valueOf(ul_Divided_With_Number_Of_Streams));

		if (ul_Divided_With_Number_Of_Streams < ulPassCriteria) {
			report.report("UL : Expected: " + calcUpRate + "[Mbps] , Actual : "
					+ longToString3DigitFormat(uLrxTotal / listOfStreamList.size()) + "[Mbps]", Reporter.FAIL);
		} else {
			report.report("UL : Expected: " + calcUpRate + "[Mbps] , Actual : "
					+ longToString3DigitFormat(uLrxTotal / listOfStreamList.size()) + "[Mbps]");
		}

		if (dl_Divided_With_Number_Of_Streams < dlPassCriteria) {
			report.report("DL : Expected: " + calcDownRate + "[Mbps], Actual : "
					+ longToString3DigitFormat(dlrxTotal / listOfStreamList.size()) + "[Mbps]", Reporter.FAIL);
		} else {
			report.report("DL : Expected: " + calcDownRate + "[Mbps], Actual : "
					+ longToString3DigitFormat(dlrxTotal / listOfStreamList.size()) + "[Mbps]");
		}
		reason = "Exp - UL: " + calcUpRate + "Mbps" + " DL: " + calcDownRate + "Mbps<br>";
		reason += "Act - UL: " + upRate + "Mbps" + " DL: " + downRate + "Mbps";

		createHTMLTableWithResults(ul_Divided_With_Number_Of_Streams, ulPassCriteria, dl_Divided_With_Number_Of_Streams,
				dlPassCriteria, portsLoadPair.getElement0(), portsLoadPair.getElement1());
	}

	private String[] getCalculatorPassCriteria(RadioParameters radioParams) {
		String calculatorStringKey = ParseRadioProfileToString(radioParams);
		if (calculatorStringKey == null) {
			report.report("calculator key value is empty - fail test", Reporter.FAIL);
		}
		CalculatorMap calcMap = new CalculatorMap();
		String dl_ul = calcMap.getPassCriteria(calculatorStringKey);
		return dl_ul.split("_");
	}

	private void createHTMLTableWithResults(Double actualUl, Double expectedUL, Double actualDl, Double expectedDL,
			Double injectedDL, Double injectedUL) {
		ArrayList<String> results = new ArrayList<String>();
		results.add("Injected [Mbps]");
		results.add("Pass Criteria");
		results.add("Actual");

		results.add("UL");
		results.add(String.format("%.2f", injectedUL));
		results.add(String.format("%.2f", expectedUL));
		results.add(String.format("%.2f", actualUl));

		results.add("DL");
		results.add(String.format("%.2f", injectedDL));
		results.add(String.format("%.2f", expectedDL));
		results.add(String.format("%.2f", actualDl));

		GeneralUtils.HtmlTable table = new HtmlTable();
		// Head Line
		table.addNewColumn(results.get(0));
		table.addNewColumn(results.get(1));
		table.addNewColumn(results.get(2));
		// 2nd Line
		table.addNewRow(results.get(3));
		table.addField(HtmlFieldColor.WHITE, results.get(4));
		table.addField(HtmlFieldColor.WHITE, results.get(5));
		HtmlFieldColor line2Result = HtmlFieldColor.WHITE;
		if (actualUl >= expectedUL) {
			line2Result = HtmlFieldColor.GREEN;
		} else {
			line2Result = HtmlFieldColor.RED;
		}
		table.addField(line2Result, results.get(6));
		// 3rd Line
		table.addNewRow(results.get(7));
		table.addField(HtmlFieldColor.WHITE, results.get(8));
		table.addField(HtmlFieldColor.WHITE, results.get(9));
		HtmlFieldColor line3Result = HtmlFieldColor.WHITE;
		if (actualDl >= expectedDL) {
			line3Result = HtmlFieldColor.GREEN;
		} else {
			line3Result = HtmlFieldColor.RED;
		}
		table.addField(line3Result, results.get(10));
		table.reportTable("");
	}

	private double modifyAndPortLinkAndPrint(double calculatorPassWithSysModi, double systemLoadWithModifications,
			double expectedPassCriteriaPercent) {
		Double systemLoadWithCustomPercent = 0.0;
		Double result = 0.0;
		String calculationOne = "System Current Load For port : " + String.format("%.2f", systemLoadWithModifications);
		systemLoadWithCustomPercent = systemLoadWithModifications * (expectedPassCriteriaPercent / 100.0);
		String calculationTwo = "System load * custom JSystem Parameter = "
				+ String.format("%.2f", systemLoadWithCustomPercent);
		String conclusion;

		result = systemLoadWithCustomPercent;
		conclusion = "Pass Criteria : " + String.format("%.2f", systemLoadWithCustomPercent);
		if (systemLoadWithCustomPercent >= calculatorPassWithSysModi) {
			conclusion = "System Load : " + String.format("%.2f", systemLoadWithCustomPercent)
					+ " , Maximum Capacity : " + String.format("%.2f", systemLoadWithModifications)
					+ " => Pass Criteria will be : " + String.format("%.2f", calculatorPassWithSysModi);
			result = calculatorPassWithSysModi;
		}

		GeneralUtils.startLevel(conclusion);
		report.report(calculationOne);
		report.report(calculationTwo);
		GeneralUtils.stopLevel();

		return result;

	}

	protected String ParseRadioProfileToString(RadioParameters radioParams2) {
		// debug print
		GeneralUtils.printToConsole("TDD : FC_Dup_BW_CFI_Ssf\n" + "FDD: Dup_BW");
		String res = radioParams2.getCalculatorString(streamsMode);
		GeneralUtils.printToConsole("returning String line: " + res);
		return res;
	}

	/**
	 * @author Shahaf Shuhamy printing a simple table with the Test Summary Results
	 *         of Rx Ul and DL
	 * @param debugPrinter
	 * @param ulrxTotal
	 * @param dlrxTotal
	 */
	protected void printPortSummeryBeforeTestEnds(StreamList debugPrinter, Long ulrxTotal, Long dlrxTotal) {
		ArrayList<String> headLines = new ArrayList<String>();
		headLines.add("Min Per Stream");
		headLines.add("Average rx Rate");
		headLines.add("Max Per Stream");
		ArrayList<String> values = new ArrayList<String>();
		// up link configure arraylist
		Double ulMin = EndTestUlMin / 1000000.0;
		values.add(ulMin.toString());
		Double totalRate = Double
				.parseDouble(new DecimalFormat("##.####").format(ulrxTotal / 1000000.0 / listOfStreamList.size()));
		values.add(totalRate.toString());
		Double ulMax = EndTestUlMax / 1000000.0;
		values.add(ulMax.toString());
		debugPrinter.addValues("Port Summery Test Results", "UL", headLines, values);
		// down link configure arrayList
		values.clear();
		ulMin = EndTestDlMin / 1000000.0;
		values.add(ulMin.toString());
		totalRate = Double
				.parseDouble(new DecimalFormat("##.####").format(dlrxTotal / 1000000.0 / listOfStreamList.size()));
		values.add(totalRate.toString());
		ulMax = EndTestDlMax / 1000000.0;
		values.add(ulMax.toString());
		debugPrinter.addValues("Port Summery Test Results", "DL", headLines, values);
		// print table
		report.reportHtml("Port Summery Test Results",
				debugPrinter.printTablesHtmlForStream("Port Summery Test Results"), true);
	}

	/**
	 * @author Moran Goldenberg
	 * @param temp
	 */
	protected void printDLandULAverageTableAndCounters() {
		if (DLULwd == null) {
			report.report("no Table for UL and DL avrage counters.");
			return;
		}
		HtmlTable htmlTable = new HtmlTable();
		String value = new String();
		ArrayList<Integer> ulCounters = new ArrayList<>();
		ArrayList<Integer> dlCounters = new ArrayList<>();
		htmlTable.addNewColumn("DL PER");
		htmlTable.addNewColumn("UL CRC");
		Integer result = 0;
		int percent = 0;
		for (String sample : DLULwd.getDlCounterSum().keySet()) {
			HtmlFieldColor color = HtmlFieldColor.WHITE;
			if (DLULwd.getDlCounters().get(sample) != null) {
				percent = (DLULwd.getDlCounters().get(sample) * 100) / DLULwd.getGeneralCounter();
				if (percent > 50) {
					htmlTable.addNewRow(sample);
					if (DLULwd.getDlCounterSum().get(sample) != null || DLULwd.getDlCounterSum().get(sample) != 0) {
						result = DLULwd.getDlCounterSum().get(sample) / DLULwd.getDlCounters().get(sample);
						dlCounters.add(result);
						value = "" + result;
					} else
						value = "-999";
				}

			} else
				value = "-999";
			htmlTable.addField(color, value);
			color = HtmlFieldColor.WHITE;
			if (DLULwd.getUlCounters().get(sample) != null) {
				percent = (DLULwd.getUlCounters().get(sample) * 100) / DLULwd.getGeneralCounter();
				if (percent > 50) {
					if (DLULwd.getUlCounterSum().get(sample) != null) {
						result = DLULwd.getUlCounterSum().get(sample) / DLULwd.getUlCounters().get(sample);
						value = "" + result;
						ulCounters.add(result);
					} else
						value = "-999";
					;
				}
			} else
				value = "-999";
			htmlTable.addField(color, value);
			color = HtmlFieldColor.WHITE;
		}
		htmlTable.reportTable("DL PER and UL CRC Average Table");
		printDLandULCounters(ulCounters, dlCounters);
	}

	/**
	 * @author Moran Goldenberg
	 */
	protected void terminateWatchDog() {
		if (wd == null || DLULwd == null) {
			return;
		}
		GeneralUtils.printToConsole("Shutting down Watch Dog object");
		wd.removeCommand(DLULwd);
		wd.shutDown();
	}

	/**
	 * Summering the ArrayList to get Long Minimum and maximum Values
	 * 
	 * @param uLrxTotal
	 * @param dlrxTotal
	 * @param listOfStreamList2
	 * @return
	 */
	protected ArrayList<Long> getUlDlResultsFromList(Long uLrxTotal, Long dlrxTotal,
			ArrayList<ArrayList<StreamParams>> listOfStreamList2) {
		ArrayList<Long> listOfTotalRx = new ArrayList<Long>();
		for (ArrayList<StreamParams> list : listOfStreamList2) {
			for (StreamParams stream : list) {
				if (stream.getName().contains("UL")) {
					uLrxTotal += stream.getRxRate();
				}
				if (stream.getName().contains("DL")) {
					dlrxTotal += stream.getRxRate();
				}
			}
		}
		listOfTotalRx.add(uLrxTotal);
		listOfTotalRx.add(dlrxTotal);
		return listOfTotalRx;
	}

	protected void sumAndPrintTablesPerPort() {
		ArrayList<StreamParams> UlPortList = new ArrayList<StreamParams>();
		ArrayList<StreamParams> DlPortList = new ArrayList<StreamParams>();
		// for each streams list
		for (ArrayList<StreamParams> streams : listOfStreamList) {
			// inits min/max values
			StreamParams UlSumRx = new StreamParams();
			UlSumRx.setRxMaxRate(Integer.MIN_VALUE);
			UlSumRx.setRxMinRate(Integer.MAX_VALUE);
			StreamParams DlSumRx = new StreamParams();
			DlSumRx.setRxMaxRate(Integer.MIN_VALUE);
			DlSumRx.setRxMinRate(Integer.MAX_VALUE);
			// for each stream in the streams list
			int counterPerStreamForAvgDL = 0;
			int counterPerStreamForAvgUL = 0;
			for (StreamParams stream : streams) {
				if (stream.getName().contains("UL")) {
					checkMinMax(UlSumRx, stream, counterPerStreamForAvgUL);
					UlSumRx.setTimeStamp(stream.getTimeStamp());
				} else {
					checkMinMax(DlSumRx, stream, counterPerStreamForAvgDL);
					DlSumRx.setTimeStamp(stream.getTimeStamp());
				}
			}
			UlPortList.add(UlSumRx);
			DlPortList.add(DlSumRx);
			initParamsForEndTest(UlSumRx, DlSumRx);
		}
		// we got 2 lists each with UL and DL summers - left to do is to SUM
		// then All up into 2 objects that will contain min,Max and Average
		addGraphs(UlPortList, DlPortList, isQci9);
		printTablesHTML(UlPortList, "UL_PORTS:");
		printTablesHTML(DlPortList, "DL_PORTS:");
	}

	protected void addGraphs(ArrayList<StreamParams> UlPortList, ArrayList<StreamParams> DlPortList, boolean isQci9) {
		ArrayList<Double> ulRx = new ArrayList<>();
		ArrayList<Long> Time = new ArrayList<>();
		ArrayList<Double> dlRx = new ArrayList<>();
		for (int i = 0; i < UlPortList.size(); i++) {
			ulRx.add((UlPortList.get(i).getRxRate() / 1000) / 1000.0);
			Time.add(UlPortList.get(i).getTimeStamp());
			dlRx.add((DlPortList.get(i).getRxRate() / 1000) / 1000.0);
		}
		try {
			GraphAdder.AddGraph(String.format("%s %s", testName, dut.getNetspanName()), "Time Stamp / Sec", "RX / Mb",
					ulRx, dlRx, Time, isQci9, true);
			isQci9 = false;
		} catch (IOException e) {
			GeneralUtils.printToConsole(e.getMessage());
		} catch (Exception e) {
			GeneralUtils.printToConsole(e.getMessage());
		}
	}

	/**
	 * adding params for the End Test Print Table
	 * 
	 * @param streamParamObjUL
	 * @param streamParamobjDl
	 */
	protected void initParamsForEndTest(StreamParams streamParamObjUL, StreamParams streamParamobjDl) {
		if (streamParamObjUL.getRxRate() > EndTestUlMax) {
			EndTestUlMax = streamParamObjUL.getRxRate();
		}
		if (streamParamObjUL.getRxRate() < EndTestUlMin) {
			EndTestUlMin = streamParamObjUL.getRxRate();
		}

		if (streamParamobjDl.getRxRate() > EndTestDlMax) {
			EndTestDlMax = streamParamobjDl.getRxRate();
		}
		if (streamParamobjDl.getRxRate() < EndTestDlMin) {
			EndTestDlMin = streamParamobjDl.getRxRate();
		}
	}

	/**
	 * @author Shahaf Shuhamy Print Port Tables With StreamList Class Methods
	 * @param UlPortList
	 * @param UlDlMode
	 */
	protected void printTablesHTML(ArrayList<StreamParams> UlPortList, String UlDlMode) {
		ArrayList<String> headLines = new ArrayList<String>();
		StreamList ULPrinter = new StreamList();
		// headLines.add("Time_Stamp");
		headLines.add("L1bitRate_Min_[Mbit/s]");
		headLines.add("L1bitRate_Sum_[Mbit/s]");
		headLines.add("L1bitRate_Max_[Mbit/s]");
		int i = 0;
		for (StreamParams stream : UlPortList) {
			ArrayList<String> valuesArray = new ArrayList<String>();
			valuesArray.add(longToString3DigitFormat(stream.getRxMinRate()));
			valuesArray.add(longToString3DigitFormat(stream.getRxRate()));
			valuesArray.add(longToString3DigitFormat(stream.getRxMaxRate()));
			SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm:ss");
			Date resultdate = new Date(UlPortList.get(i++).getTimeStamp());
			ULPrinter.addValues(UlDlMode, sdf.format(resultdate), headLines, valuesArray);
		}
		report.reportHtml(UlDlMode, ULPrinter.printTablesHtmlForStream(UlDlMode), true);
	}

	/**
	 * formatting long type number to #.## string number after dividing in 1000000.0
	 * to get double value.
	 * 
	 * @author Shahaf Shuhamy
	 * @param number
	 * @return
	 */
	protected String longToString3DigitFormat(long number) {
		double temp;
		NumberFormat formatter = new DecimalFormat("#.###");
		temp = number / 1000000.0;
		return formatter.format(temp);
	}

	/**
	 * checking min max values from 2 StreamParams objects
	 * 
	 * @param streamParam
	 * @param streamFromList
	 * @param counter
	 */
	protected void checkMinMax(StreamParams streamParam, StreamParams streamFromList, int counter) {
		counter++;
		long temp = streamParam.getRxRate();
		// summs all streams for printing
		streamParam.setRxRate((temp + streamFromList.getRxRate()));
		// Min&Max
		if (streamParam.getRxMinRate() > streamFromList.getRxRate()) {
			streamParam.setRxMinRate(streamFromList.getRxRate());
		}
		if (streamParam.getRxMaxRate() < streamFromList.getRxRate()) {
			streamParam.setRxMaxRate(streamFromList.getRxRate());
		}
	}

	/**
	 * @author Shahaf Shuhamy set Dut enodeB
	 * @return
	 */
	public void setDUT(String dut) {
		this.dut = (EnodeB) SysObjUtils.getInstnce().initSystemObject(EnodeB.class, false, dut).get(0);
	}

	/**
	 * @author Shahaf Shuhamy get Dut enodeB
	 * @return
	 */
	public String getDUT() {
		return this.dut.getNetspanName();
	}

	/**
	 * getting Test UEs
	 * 
	 * @param enb
	 * @param count
	 * @return
	 * @throws Exception
	 */

	public ArrayList<UE> getUES(EnodeB enb, int count) {
		ArrayList<UE> tempUes = SetupUtils.getInstance().getallUEsPerEnodeb(dut);
		ArrayList<UE> cat6UES = (ArrayList<UE>) tempUes.stream().filter(ue -> ue.getUeCategory() == 6)
				.collect(Collectors.toList());
		ArrayList<UE> cat4UES = (ArrayList<UE>) tempUes.stream().filter(ue -> ue.getUeCategory() == 4)
				.collect(Collectors.toList());

		if (isCaTest) {
			return (ArrayList<UE>) cat6UES.stream().limit(count).collect(Collectors.toList());
		} else {
			if (cat4UES.size() >= count) {
				return (ArrayList<UE>) cat4UES.stream().limit(count).collect(Collectors.toList());
			}
			if (count == 1 && EnodeBConfig.getInstance().isCAEnableInNode(enb)) {
				report.report("Only cat6 UEs available, the test will be run as CA");
				isCaTest = true;
			}
			return (ArrayList<UE>) tempUes.stream().limit(count).collect(Collectors.toList());
		}
	}

	public ArrayList<UE> getUES(EnodeB enb) {
		if (isCaTest)
			return SetupUtils.getInstance().getCat6UEsPerEnodeB(dut);
		else
			return SetupUtils.getInstance().getallUEsPerEnodeb(dut);
	}

	@ParameterProperties(description = "load in precent from the calculator value")
	public void setLoad_UL(String load_UL) {
		use_Load_UL = !load_UL.trim().equals("");
		if (use_Load_UL) {
			this.Load_UL = Integer.valueOf(load_UL);
		}
	}

	@ParameterProperties(description = "load in precent from the calculator value")
	public void setLoad_DL(String load_DL) {
		use_Load_DL = !load_DL.trim().equals("");
		if (use_Load_DL) {
			this.Load_DL = Integer.valueOf(load_DL);
		}
	}

	@ParameterProperties(description = "Expected value in precent from the calculator value")
	public void setExpected_UL(String expected_UL) {
		use_Expected_UL = !expected_UL.trim().equals("");
		if (use_Expected_UL) {
			this.Expected_UL = Double.valueOf(expected_UL);
		}
	}

	@ParameterProperties(description = "Expected value in precent from the calculator value")
	public void setExpected_DL(String expected_DL) {
		use_Expected_DL = !expected_DL.trim().equals("");
		if (use_Expected_DL) {
			this.Expected_DL = Double.valueOf(expected_DL);
		}
	}

	@ParameterProperties(description = "Test runtime in minutes")
	public void setRunTime(String runTime) {
		use_Runtime = !runTime.trim().equals("");
		if (use_Runtime) {
			this.RunTime = Integer.valueOf(runTime);
		}
	}

	protected long setTime(long defaultTime) {
		long time = defaultTime;
		if (use_Runtime) {
			time = RunTime * 60 * 1000;
		}
		return time;
	}
}
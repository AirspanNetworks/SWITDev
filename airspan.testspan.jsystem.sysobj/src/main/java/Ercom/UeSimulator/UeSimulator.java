package Ercom.UeSimulator;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.joda.time.DateTime;

import EnodeB.EnodeB;
import Utils.StdOut;
import jsystem.framework.system.SystemObjectImpl;

/**
 * @author spuser
 *
 */
/**
 * @author spuser
 *
 */
public class UeSimulator extends SystemObjectImpl implements Runnable {
	
	// ----------------------------- variables -----------------------------
	
	/*
	 * Timeout before executing new scenario (in seconds)
	 */
	private static final int			TIMEOUT_BEFORE_NEXT_RUN		= 30;
																	
	/**
	 * Timeout to wait before check the number of connected UEs
	 */
	private static final int			WAIT_FOR_UE_TO_CONNECT		= 15;
																	
	/**
	 * empty string
	 */
	private static final String			EMPTY_STRING				= "";
																	
	/*
	 * polling interval for platform state in seconds
	 */
	private static final int			POLLING_INTERVAL			= 10;
																	
	/*
	 * test unique id
	 */
	private String						testUniqId					= "";
																	
	/*
	 * test run time in minutes
	 */
	private int							RunTime						= 10;
																	
	/*
	 * Names of the test to be entered to the queue
	 */
	private String						testName					= "";
																	
	/*
	 * Control Blade of UE simulator
	 */
	public ControlBlade					controlBlade				= null;
																	
	/*
	 * Blade what represent layer 1
	 */
	public L1Blade						l1Blade						= null;
																	
	/*
	 * Blade what represent layer 2
	 */
	public L2Blade						l2Blade						= null;
																	
	/*
	 * This parameter controls scenario thread state
	 */
	private boolean						running						= true;
																	
	/**
	 * Thread that running in parallel to JSYSTEM scenario Collecting relevant data. Thread stops
	 * then UeSimulator summary test is reached in scenario or timeout occurred.
	 */
	private Thread						scenarioThread;
										
	/*
	 * UE threshold, if number of connected UEs is under This threshold the test should not be
	 * started.
	 */
	private int							connectedUEThreshold		= 0;
																	
	/*
	 * EnodeB under test. We need to monitor number of connected UEs It will be done using EnodeB
	 * under test (EnbUnderTest.XLP.getConnectedUeCount())
	 */
	private EnodeB						EnbUnderTest				= null;
																	
	/*
	 * Execution State Of The Platform holds the time and the state of the platform Will register
	 * only idle periods for current platform
	 */
	@Deprecated
	private HashMap<DateTime, Integer>	executionStateOfThePlatform	= null;
																	
	/*
	 * Map stores the running test
	 */
	private Map<String, TestProperty>	startedTests				= null;
																	
	/*
	 * Test ID of currently running test
	 */
	private String						RunningTestID				= "";
																	
	// --------------- initialization functions ------------------------
	
	@Override
	public void init() throws Exception {
		super.init();
		startedTests = new HashMap<String, TestProperty>();
		executionStateOfThePlatform = new HashMap<DateTime, Integer>();
	}
	
	@Override
	public void close() {
		super.close();
	}
	
	/**
	 * default constructor
	 */
	public UeSimulator() {
	
	}
	
	// ----------------------------- Methods -----------------------------
	
	/**
	 * @return
	 * @throws Exception
	 */
	public String getPlatformDuplexMode() throws Exception {
		String l1dm = l1Blade.getL1BladeDuplexMode();
		String l2dm = l2Blade.getL2BladeDuplexMode();
		if (l1dm != null && l2dm != null && l1dm.equals(l2dm)) {
			return l1dm;
		}
		else {
			return "";
		}
	}
	
	/**
	 * @throws Exception
	 */
	public void changePlatformDupleModeFdd2Tdd() throws Exception {
		l1Blade.changeL1BladeDuplexModeFdd2Tdd();
		l1Blade.setTDDMode(true);
		l2Blade.changeL2BladeDuplexModeFdd2Tdd();
	}
	
	/**
	 * @throws Exception
	 */
	public void changePlatformDupleModeTdd2Fdd() throws Exception {
		l1Blade.changeL1BladeDuplexModeTdd2Fdd();
		l1Blade.setTDDMode(false);
		l2Blade.changeL2BladeDuplexModeTdd2Fdd();
	}
	
	/**
	 * @param cellId
	 * @throws Exception
	 */
	public void setPlatformCellID(int cellId) throws Exception {
		l1Blade.setCellId(cellId);
	}
	
	/**
	 * @param bw
	 * @throws Exception
	 */
	public void setPlatformBandwidth(int bw) throws Exception {
		l1Blade.setBandwidth(bw);
		l2Blade.configureL2Bandwidth(bw);
	}
	
	/**
	 * @param ulf
	 * @throws Exception
	 */
	public void setPlatformULFrequency(int ulf) throws Exception {
		l1Blade.setUlFrequency(ulf);
	}
	
	/**
	 * @param dlf
	 * @throws Exception
	 */
	public void setPlatformDLFrequency(int dlf) throws Exception {
		l1Blade.setDlFrequency(dlf);
	}
	
	/**
	 * start UE Simulator Scenario thread
	 */
	public void startUeSimulator() {
		setScenarioThread(new Thread(this));
		getScenarioThread().start();
	}
	
	/**
	 * @return
	 */
	public String findUniqTestId() {
		String testID = "";
		report.report("Retrive all Defined Tests in order to get name to uniqID mapping");
		Map<String, String> defTests = getControlBlade().getDefinedTests();
		Set<String> keys = defTests.keySet();
		
		for (String key : keys) {
			
			if (key.equals(getTestName())) {
				testID = defTests.get(key);
				report.report(
				        String.format("Name[%s]=uniqId[%s]", getTestName(), defTests.get(key)));
				break;
			}
		}
		return testID;
	}
	
	/**
	 * @param name
	 * @return
	 */
	public boolean isScenarioConfigured(String name) {
		boolean isConfigured = false;
		setTestName(name);
		setTestUniqId(findUniqTestId());
		if (getTestUniqId().equals(EMPTY_STRING)) {
			StdOut.print2console(
			        "Unable to find unique test id for test named [" + getTestName() + "]");
		}
		else {
			isConfigured = true;
		}
		return isConfigured;
	}
	
	/**
	 * @return
	 */
	private boolean initiateScenarioDirectExecution() {
		boolean decision = false;
		StdOut.print2console("Start scenario Execution [" + getTestName() + "]");
		
		// perform Direct Execution using XMLRPC infrastructure.Test directly passed to
		// execution without entering the queue. The return string containing name of the
		// directory on the control blade which stores all the result files of current test.
		String result = getControlBlade().performDirectExecution(this.testUniqId);
		
		// if where is no result returned from Control Blade.
		// current test will be marked as failed in statistics.
		if (result == null) {
			return decision;
		}
		
		// result returned from Control Blade is empty. In this case we must check if test started.
		// If test started he must be terminated because we don't have path to test results.
		if (result.isEmpty()) {
			StdOut.print2console("The result location not returned");
			
			// check if execution started. If platform running state different from "idle"
			// current execution must be terminated.
			String pes = "";
			pes = getControlBlade().getPlatformExecutionState();
			StdOut.print2console("Get Platform Execution State :: " + pes);
			if (pes.isEmpty() || !pes.equals("idle")) {
				getControlBlade().stopCurrentlyExecutedTest();
				StdOut.print2console(
				        "Current execution stoped, This test started without reporting result path.");
			}
			
			return decision;
		} // if
		
		try {
			TimeUnit.SECONDS.sleep(WAIT_FOR_UE_TO_CONNECT);
		}
		catch (InterruptedException e) {
			StdOut.print2console(e.getMessage());
		}
		
		// check the number of connected UEs after x seconds of test.
		// if number of UEs less then expected terminate the test.
		int numberOfConnectedUEs =0; //getEnbUnderTest().XLP.getConnectedUeCount();
		if (numberOfConnectedUEs < getConnectedUEThreshold()) {
			StdOut.print2console(String.format(
			        "The number of connected UEs less than required by user input %d < %d",
			        numberOfConnectedUEs, getConnectedUEThreshold()));
			StdOut.print2console("Current execution will be terminated.");
			getControlBlade().stopCurrentlyExecutedTest();
			StdOut.print2console("Current execution terminated.");
			return decision;
		}
		else {
			StdOut.print2console("Number of connected UEs :: " + numberOfConnectedUEs);
		}
		StdOut.print2console(
		        String.format("Result stored under following name on control blade[%s]", result));
				
		// successfully started test will be stored in map with name as KEY and TESTP_ROPERTY as
		// value
		// @see private Map<String,TestProperty> startedTests
		setStartedTest(getTestName(),
		        new TestProperty(getTestName(), getTestUniqId(), result, true));
				
		// set currently running test id
		setRunningTestID(getTestName());
		decision = true;
		
		return decision;
	} // initiateScenarioDirectExecution
	
	/*
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {
			
			// barrier time after this time test will stop.
			DateTime barrier = DateTime.now();
			
			// Create target time
			barrier = DateTime.now().plusMinutes(getRunTime());
			
			do {
				StdOut.print2console("Start new test");
				if (initiateScenarioDirectExecution()) {
					String state = "idle";
					
					// if scenario started successfully we need to monitor
					// blade state, check if blade state changed to idle every
					// poling interval, if state changed to idle start new test execution
					do {
						TimeUnit.SECONDS.sleep(POLLING_INTERVAL);
						state = getControlBlade().getPlatformExecutionState();
						//getStartedTest(getRunningTestID()).recordNumberOfCurrentlyConnectedUEs(DateTime.now(),
						        //EnbUnderTest.XLP.getConnectedUeCount());
					} while (!state.equals("idle") && DateTime.now().isBefore(barrier));
					
					// current test stopped remove it from running.
					setRunningTestID(EMPTY_STRING);
					
					// wait before start new execution
					TimeUnit.SECONDS.sleep(TIMEOUT_BEFORE_NEXT_RUN);
				}
				else {
					StdOut.print2console("Scenario execution failed to start.");
					
					// wait before start new execution
					TimeUnit.SECONDS.sleep(TIMEOUT_BEFORE_NEXT_RUN);
				}
			} while (isRunning() && DateTime.now().isBefore(barrier));
			
			// stop current test immediately
			getControlBlade().stopCurrentlyExecutedTest();
		}
		catch (InterruptedException ie) {
			StdOut.print2console("Thread interupted.");
		}
	}
	
	// --------------------- getters/Setters -------------------------
	
	public ControlBlade getControlBlade() {
		return controlBlade;
	}
	
	public void setControlBlade(ControlBlade controlBlade) {
		this.controlBlade = controlBlade;
	}
	
	public Thread getScenarioThread() {
		return scenarioThread;
	}
	
	public void setScenarioThread(Thread scenarioThread) {
		this.scenarioThread = scenarioThread;
	}
	
	public L1Blade getL1Blade() {
		return l1Blade;
	}
	
	public void setL1Blade(L1Blade l1Blade) {
		this.l1Blade = l1Blade;
	}
	
	public L2Blade getL2Blade() {
		return l2Blade;
	}
	
	public void setL2Blade(L2Blade l2Blade) {
		this.l2Blade = l2Blade;
	}
	
	public int getRunTime() {
		return RunTime;
	}
	
	public void setRunTime(int runTime) {
		RunTime = runTime;
	}
	
	public String getTestName() {
		return testName;
	}
	
	public void setTestName(String testName) {
		this.testName = testName;
	}
	
	public String getTestUniqId() {
		return testUniqId;
	}
	
	public void setTestUniqId(String testUniqId) {
		this.testUniqId = testUniqId;
	}
	
	public synchronized boolean isRunning() {
		return running;
	}
	
	public synchronized void setRunning(boolean running) {
		this.running = running;
	}
	
	public int getConnectedUEThreshold() {
		return connectedUEThreshold;
	}
	
	public void setConnectedUEThreshold(int connectedUEThreshold) {
		this.connectedUEThreshold = connectedUEThreshold;
	}
	
	public EnodeB getEnbUnderTest() {
		return EnbUnderTest;
	}
	
	public void setEnbUnderTest(EnodeB enbUnderTest) {
		EnbUnderTest = enbUnderTest;
	}
	
	public HashMap<DateTime, Integer> getExecutionStateOfThePlatform() {
		return executionStateOfThePlatform;
	}
	
	public void setExecutionStateOfThePlatform(
	        HashMap<DateTime, Integer> executionStateOfThePlatform) {
		this.executionStateOfThePlatform = executionStateOfThePlatform;
	}
	
	public Map<String, TestProperty> getStartedTests() {
		return startedTests;
	}
	
	public void setStartedTests(Map<String, TestProperty> startedTests) {
		this.startedTests = startedTests;
	}
	
	public void setStartedTest(String name, TestProperty tp) {
		this.startedTests.put(name, tp);
	}
	
	public TestProperty getStartedTest(String name) {
		return startedTests.get(name);
	}
	
	public synchronized String getRunningTestID() {
		return RunningTestID;
	}
	
	public synchronized void setRunningTestID(String runningTestID) {
		RunningTestID = runningTestID;
	}
	
}

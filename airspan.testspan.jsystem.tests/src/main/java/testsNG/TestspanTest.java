package testsNG;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.After;
import org.junit.Before;

import EnodeB.EnodeB;
import EnodeB.SnifferFileLocation;
import EnodeB.Components.Log.Logger;
import Netspan.NetspanServer;
import Netspan.API.Enums.EnbStates;
import Netspan.API.Lte.AlarmInfo;
import TestingServices.TestConfig;
import Utils.DebugFtpServer;
import Utils.GeneralUtils;
import Utils.InetAddressesHelper;
import Utils.ScenarioUtils;
import Utils.ScpClient;
import Utils.SetupUtils;
import Utils.TunnelManager;
import Utils.Reporters.GraphAdder;
import Utils.Snmp.MibReader;
import Utils.WatchDog.CommandMemoryCPU;
import Utils.WatchDog.CommandWatchInService;
import Utils.WatchDog.WatchDogManager;
import jsystem.framework.report.ListenerstManager;
import jsystem.framework.report.Reporter;
import junit.framework.SystemTestCase4;
import testsNG.Actions.AlarmsAndEvents;
import testsNG.Actions.EnodeBConfig;
import testsNG.Actions.PeripheralsConfig;
import testsNG.General.SWUpgrade;

/**
 * The Class TestspanTest.
 */
public class TestspanTest extends SystemTestCase4 {

	private static final String LOGGER_UPLOAD_ALL_LINK = "//asil-swit/upload/%s/%s";
	private static final String LOGGER_UPLOAD_ALL_DESC = "Logger upload all link";
	private static TestListener testListener;
	private ScpClient scpCli;
	private String basePath = System.getProperty("user.dir");
	private String beforeTest = "BeforeTest";
	private String afterTest = "AfterTest";
	private Boolean gotDBFiles = false;
	private ArrayList<CommandMemoryCPU> cpuCommands = new ArrayList<CommandMemoryCPU>();
	private HashMap<String, ArrayList<String>> filesFromDBPerNode = new HashMap<String, ArrayList<String>>();
	protected String myVersion = "";
	/** The Constant SYSOBJ_STRING_DELIMITER. */
	protected String reason = "";
	// must set enbInTest!!!!!!
	protected ArrayList<EnodeB> enbInTest = null;
	protected ArrayList<EnodeB> enbInSetup;

	protected NetspanServer netspanServer = null;
	protected AlarmsAndEvents alarmsAndEvents = null;

	/**
	 * The isTestWasSuccessful parameter will be returned by every test back to
	 * scenario and can be evaluated by JSYSTEM flow control tools. The following
	 * parameter can be assigned only with FAIL/PASS strings. The string variable
	 * selected because it easier to operate with Strings the parameter returned to
	 * JSYSTEM scenario. The following value must be returned by every test using
	 * "returnValue" property testProperty notation.
	 **/
	protected String isTestWasSuccessful = "";

	public static final String DEFAULT_SOURCE_POM_PROPERTIES_FILE_NAME = "pom.properties";
	public static final String PATH_TO_POM_PROPERTIES = System.getProperty("user.dir") + File.separator + "target"
			+ File.separator + "maven-archiver" + File.separator + DEFAULT_SOURCE_POM_PROPERTIES_FILE_NAME;
	private boolean enbsAreInService = true;

	private static HashMap<String, Integer> testStats = new HashMap<String, Integer>();
	private static HashMap<String, Integer> scenarioStats;
	private static HashMap<String, Integer> unexpectedInScenario = null;
	protected WatchDogManager wd;

	/**
	 * Inits the.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Before
	public void init() throws Exception {
		scenarioStats = ScenarioUtils.getInstance().getScenarioStats();
		unexpectedInScenario = ScenarioUtils.getInstance().getUnexpectedInScenario();

		initTestListener();
		TunnelManager.seteNodeBInTestList(enbInTest);
		TunnelManager.failedTestIfNoTriesLeftForAnyDut();
		GeneralUtils.startLevel("Initialize Components");
		testStats = new HashMap<String, Integer>();
		if (enbInTest == null)
			throw new Exception("must set enbInTest!!!!!!");

		GeneralUtils.printToConsole(ScenarioUtils.getInstance().getMemoryConsumption());

		myVersion = getVersion();
		alarmsAndEvents = AlarmsAndEvents.getInstance();
		report.setContainerProperties(0, "Version", myVersion);
		netspanServer = NetspanServer.getInstance();
		reason = "";
		GeneralUtils.startLevel("Loading setup's enodeBs");
		enbInSetup = SetupUtils.getInstance().getAllEnb();
		GeneralUtils.stopLevel();

		for (EnodeB eNodeB : enbInTest) {
			if (eNodeB == null) {
				report.report("EnodeB is not initialized", Reporter.WARNING);
				GeneralUtils.stopAllLevels();
				return;
			}

			if (eNodeB.blackListed) {
				enbsAreInService = false;
				report.report(eNodeB.getName() + " is black listed for failing to reach all running. stopping test.",
						Reporter.WARNING);
				break;
			}

			if (unexpectedInScenario == null) {
				unexpectedInScenario = new HashMap<String, Integer>();
			}
			GeneralUtils.printToConsole(String.format("Creating log files for test for eNondeB %s", eNodeB.getName()));

			Logger[] loggers = eNodeB.getLoggers();
			for (Logger logger : loggers) {
				logger.startLog(String.format("%s_%s", getMethodName(), logger.getParent().getName()));
				logger.clearTestCounters();
				logger.setCountErrorBool(true);
			}

			String enbName = eNodeB.getNetspanName();
			if (!unexpectedInScenario.containsKey(enbName)) {
				unexpectedInScenario.put(enbName, 0);
			}

			EnodeBConfig.getInstance().isEnodebManaged(eNodeB);

			eNodeB.setDeviceUnderTest(true);
			// check if bank was swapped and swap it again
			boolean bankSwapped = eNodeB.isBankSwaped();
			if (bankSwapped) {
				report.report("Bank was swapped on " + eNodeB.getNetspanName() + " Trying to swap bank back");
				eNodeB.reboot(true);
				eNodeB.waitForAllRunningAndInService(EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
			}

			alarmsAndEvents.deleteAllAlarmsNode(eNodeB);

			// commandInService
			wd = WatchDogManager.getInstance();
			CommandWatchInService commandInService = new CommandWatchInService(eNodeB);
			commandInService.name = eNodeB.getName() + "_commandInService";
			wd.addCommand(commandInService);
			// command CPU and Memory
			CommandMemoryCPU cpuCommand = new CommandMemoryCPU(eNodeB);
			cpuCommands.add(cpuCommand);
			wd.addCommand(cpuCommand);
			report.report("setting WD CPU and Memory Command for enodeB " + eNodeB.getName());

			PeripheralsConfig.getInstance().changeEnbState(eNodeB, EnbStates.IN_SERVICE);
			report.report(eNodeB.getNetspanName() + ": Wait for all Running (TimeOut="
					+ (EnodeB.WAIT_FOR_ALL_RUNNING_TIME / 60000) + " Minutes)");
			if (!eNodeB.waitForAllRunningAndInService(EnodeB.WAIT_FOR_ALL_RUNNING_TIME)) {
				report.report(eNodeB.getNetspanName() + " failed to reach all running.", Reporter.WARNING);
				enbsAreInService = false;
				eNodeB.blackListed = true;
			}
			if (eNodeB.isSkipCMP()) {
				report.report("EnodeB " + eNodeB.getNetspanName() + " is working with SKIP CMPv2");
			}
			eNodeB.setCellContextNumber(1);
			GeneralUtils.startLevel("Session connection status");
			eNodeB.showLoginStatus();
			GeneralUtils.stopLevel();

			if (eNodeB.isMACtoPHYEnabled()) {
				eNodeB.enableMACtoPHYcapture(SnifferFileLocation.Remote);
			}
			eNodeB.setNodeLoggerUrl(eNodeB, TestConfig.getInstace().getLoggerUploadAllUrl());
			try {
				netspanServer.checkAndSetDefaultProfiles(eNodeB, false);
			} catch (Exception e) {
				e.printStackTrace();
				report.report("checkAndSetDefaultProfiles failed due to: " + e.getMessage(), Reporter.WARNING);
			}

			gotDBFiles = getDBFiles(eNodeB, beforeTest);
		}

		calledOnceInInitFunc();
		GeneralUtils.stopLevel();

		if (!enbsAreInService)
			report.report("One or more of the enbs failed to reach all running state, failing and stopping test.",
					Reporter.FAIL);
		org.junit.Assume.assumeTrue(enbsAreInService);
	}

	static boolean isCalledOnceInInitFunc = false;

	private void calledOnceInInitFunc() {
		if (isCalledOnceInInitFunc == false) {
			isCalledOnceInInitFunc = true;
			setDebugFtpServer();
		}
	}

	private void setDebugFtpServer() {
		DebugFtpServer debugFtpServer = DebugFtpServer.getInstance();
		for (EnodeB eNodeB : enbInSetup) {
			try {
				System.out.print("Set debug FTP server.");

				String oid = MibReader.getInstance().resolveByName("asLteStkDebugFtpServerCfgFtpServerIp");
				eNodeB.lteCli("db set debugFtpServer [1] ftpAddress.type="+debugFtpServer.addressType+" [1]");
				if (debugFtpServer.addressType.equals("1")) 					
					eNodeB.snmpSet(oid, debugFtpServer.getDebugFtpServerIP());			
				else{					
					eNodeB.snmpSet(oid, ""); // set ipv4 field empty.					
				}
				
				oid = MibReader.getInstance().resolveByName("asLteStkDebugFtpServerCfgFtpAddress");

				eNodeB.lteCli("db set debugFtpServer [1] ftpAddress.address="+debugFtpServer.getDebugFtpServerIP()+" [1]");
				//byte[] ipAddr = InetAddressesHelper.ipStringToBytes(debugFtpServer.getDebugFtpServerIP());
				//eNodeB.snmpSet(oid, ipAddr);

				oid = MibReader.getInstance().resolveByName("asLteStkDebugFtpServerCfgFtpUser");
				eNodeB.snmpSet(oid, debugFtpServer.getDebugFtpServerUser());

				oid = MibReader.getInstance().resolveByName("asLteStkDebugFtpServerCfgFtpPassword");
				eNodeB.snmpSet(oid, debugFtpServer.getDebugFtpServerPassword());

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public String getVersion() {
		String version = "";
		// try to load from maven properties first
		try {
			// open containing file
			File file = new File(PATH_TO_POM_PROPERTIES);
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			// parsing file line by line
			String line = bufferedReader.readLine();
			while (line != null) {
				if (line.contains("version")) {
					int index = line.indexOf("=");
					version = line.substring(index + 1);
					break;
				}
				line = bufferedReader.readLine();
			}
			// close mapping file
			fileReader.close();
		} catch (Exception e) {
			// ignore
		}
		if ("" == version) {
			version = "No version found";
		}
		return version;
	}

	@After
	public void after() {
		if (enbsAreInService)
			end();
		else
			privateEnd();
	}

	/**
	 * End Method will be called after every test. The if statement will evaluate if
	 * the test was successful or not. If test failed for any reason something like
	 * assertion failure or Exception of any kind The
	 * <p>
	 * isTestWasSuccessful
	 * </p>
	 * will be assigned with default value that is "FAIL", if test was success it
	 * will be assigned with "PASS"
	 *
	 **/

	public void end() {
		privateEnd();
	}

	private void privateEnd() {
		EnodeBConfig.getInstance().deleteClonedProfiles();
		String coreFilesPath = "";
		printMemoryTables(cpuCommands);
		printMemoryGraph(cpuCommands);
		WatchDogManager.getInstance().shutDown();

		if (this.isPass) {
			isTestWasSuccessful = "PASS";
		} else {
			isTestWasSuccessful = "FAIL";
		}
		GeneralUtils.printToConsole("Test Status :: " + isTestWasSuccessful);

		ScenarioUtils.getInstance().calledOnceInEndFunc(enbInTest, this instanceof SWUpgrade);

		boolean isCoreOccurDuringTest = false;

		for (EnodeB eNodeB : enbInTest) {
			List<AlarmInfo> alarmsInfo = alarmsAndEvents.getAllAlarmsNode(eNodeB);
			if (!alarmsInfo.isEmpty()) {
				GeneralUtils.startLevel(eNodeB.getName() + "'s Alarms: ");
				for (AlarmInfo alarmInfo : alarmsInfo) {
					alarmsAndEvents.printAlarmInfo(alarmInfo);
				}
				GeneralUtils.stopLevel();
			} else
				report.report(eNodeB.getName() + "'s Alarms list is empty");

			report.addProperty(eNodeB.getNetspanName() + "_Version", eNodeB.getRunningVersion());
			Logger[] loggers = eNodeB.getLoggers();
			log.info(String.format("Closing log files for test for eNondeB %s", eNodeB.getName()));

			GeneralUtils.startLevel(String.format("eNodeB %s logs", eNodeB.getName()));

			for (Logger logger : loggers) {
				logger.setCountErrorBool(false);
				logger.closeEnodeBLog(String.format("%s_%s", getMethodName(), logger.getParent().getName()));
				testStatistics(logger, eNodeB);
				ScenarioUtils.getInstance().scenarioStatistics(logger, eNodeB);
			}

			Date date = new Date();
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yy");
			String loggerUploadAllEnodebIP = eNodeB.getLoggerUploadAllEnodebIP();
			if (loggerUploadAllEnodebIP != null) {
				report.addLink(LOGGER_UPLOAD_ALL_DESC,
						String.format(LOGGER_UPLOAD_ALL_LINK, dateFormat.format(date), loggerUploadAllEnodebIP));
			} else {
				report.report("Logger upload all not available, due to missing IP.");
			}
			GeneralUtils.stopLevel();

			GeneralUtils.startLevel(String.format("eNodeB %s Automation logs", eNodeB.getName()));
			for (Logger logger : loggers) {
				logger.closeAutoLog(String.format("%s_%s", getMethodName(), logger.getParent().getName()));
			}
			GeneralUtils.stopLevel();

			setUnexpectedRebootStatistics(eNodeB);

			if (eNodeB.isMACtoPHYEnabled()) {
				eNodeB.disableMACtoPHYcapture();
			}
			eNodeB.showMACtoPHYCaptureFiles();

			HashSet<String> coreSet = eNodeB.getCorePathList();
			int coreIndex = 1;
			String coreValue = "";
			for (String corePath : coreSet) {
				coreValue += (coreIndex + "," + eNodeB.getName() + "," + corePath
						+ ScenarioUtils.SYSOBJ_STRING_DELIMITER);
				coreIndex++;
				coreFilesPath += coreValue;
			}

			isCoreOccurDuringTest = (isCoreOccurDuringTest || eNodeB.isStateChangedToCoreDump());

			// Initialize all the Parameters that refer each test individually.
			eNodeB.clearTestPrameters();

			eNodeB.setDeviceUnderTest(false);

			// data base compare
			eNodeB.loggerUploadAll();
			ArrayList<String> fileNameList = null;
			if ((fileNameList = filesFromDBPerNode.get(eNodeB.getName())) != null) {
				if (!fileNameList.isEmpty()) {
					getDBFiles(eNodeB, afterTest);
					compareDBs(eNodeB);
				}
			}
		}

		if (isCoreOccurDuringTest) {
			if (reason != "") {
				reason += "<br>Core occured during test.";
			} else {
				reason = "Core occured during test.";
			}
		}

		if (reason != "") {
			report.addProperty("failureReason", reason);
			report.report("Fail reason: " + reason);
		}

		if (coreFilesPath != "" && coreFilesPath != null) {
			report.addProperty("CoreFiles", coreFilesPath);
		}

		if (!testStats.isEmpty() && testStats != null) {
			String logCounter = "";
			for (String key : testStats.keySet()) {
				logCounter += (key + "," + testStats.get(key) + ScenarioUtils.SYSOBJ_STRING_DELIMITER);
			}
			report.addProperty("LogCounter", logCounter);
		}

		if (!scenarioStats.isEmpty() && scenarioStats != null) {
			String logCounter = "";
			for (String key : scenarioStats.keySet()) {
				logCounter += (key + "," + scenarioStats.get(key) + ScenarioUtils.SYSOBJ_STRING_DELIMITER);
			}
			report.setContainerProperties(0, "LogCounter", logCounter);
		}

		GeneralUtils.printToConsole(ScenarioUtils.getInstance().getMemoryConsumption());
	}

	private void printMemoryGraph(ArrayList<CommandMemoryCPU> cpuCommands2) {
		if(cpuCommands2 == null) {
			report.report("cpu and memory watchdog hasn't been initiated!");
		}
		
		for(CommandMemoryCPU command : cpuCommands2) {
			printGraphForCPUCommand(command);
		}
		
	}

	private void printGraphForCPUCommand(CommandMemoryCPU command) {
		//make integers lists into double lists.
		ArrayList<Double> memoryDouble = turnIntArrayToDoubleArray(command.getMemoryValuesList());
		ArrayList<Double> cpuDouble = turnIntArrayToDoubleArray(command.getCpuValuesList());
 		ArrayList<Long> time = new ArrayList<Long>();
 		
 		//initialize time just for order of the graph
		time = initTimeList(time,memoryDouble.size());
		
		try {
			GraphAdder.AddGraph(String.format("%s %s", "graph Test", command.getNodeName()), "time stamp / sec", "node % usage", memoryDouble, cpuDouble, time, "memory","cpu",false);
		}catch(Exception e) {
			e.printStackTrace();
			report.report("problem while trying to create memory graphs! - check console");
		}
	}
	
	private ArrayList<Long> initTimeList(ArrayList<Long> time, int size) {
		ArrayList<Long> returnList = new ArrayList<Long>();
		for(int i=0; i<size; i++) {
			returnList.add(new Long(i));
		}
		return returnList;
	}

	private ArrayList<Double> turnIntArrayToDoubleArray(ArrayList<Integer> list){
		ArrayList<Double> doubleList = new ArrayList<Double>();
		for(Integer i : list) {
			doubleList.add(i.doubleValue());
		}
		return doubleList;
	}
	

	/**
	 * 
	 * @param cpuCommands
	 * @author sshahaf
	 */
	private void printMemoryTables(ArrayList<CommandMemoryCPU> cpuCommands2) {
		if (cpuCommands2 == null) {
			report.report("cpu and memory watchdog hasn't been initiated!");
		}

		for (CommandMemoryCPU command : cpuCommands2) {
			command.reportHTMLTableWithResults();
		}
	}

	/**
	 * Gets the checks if is test was successful.
	 *
	 * @return the isTestWasSuccessful
	 */
	public String getIsTestWasSuccessful() {
		return isTestWasSuccessful;
	}

	/**
	 * Sets the checks if is test was successful.
	 *
	 * @param isTestWasSuccessful
	 *            the isTestWasSuccessful to set
	 */
	public void setIsTestWasSuccessful(String isTestWasSuccessful) {
		this.isTestWasSuccessful = isTestWasSuccessful;
	}

	/**
	 * @author Avichai Yefet Add all ERRORs & WARNINGs to test Properties
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public void testStatistics(Logger logger, EnodeB eNodeB) {
		String keyProperty = "";
		HashMap<String, Integer> counters = (HashMap<String, Integer>) logger.getTestLoggerCounters().clone();
		if (counters != null) {
			for (String key : counters.keySet()) {
				String severity = ScenarioUtils.getInstance().getSeverity(key);
				String errorExpression = ScenarioUtils.getInstance().getErrorExpression(key);
				if (errorExpression.equals("NO ERROR FOUND")) {
					GeneralUtils.printToConsole("Failed to find error expression for key: " + key);
					continue;
				}
				keyProperty = eNodeB.getName() + "," + errorExpression + "," + severity;
				GeneralUtils.printToConsole("TestStatistics: " + keyProperty + "," + counters.get(key));
				testStats.put(keyProperty, counters.get(key));
			}
		}
	}

	private void setUnexpectedRebootStatistics(EnodeB eNodeB) {
		int value = eNodeB.getUnexpectedReboot();
		eNodeB.setUnexpectedReboot(0);
		if (value > 0) {
			if (this.isPass) {
				reason = "Number of unexpected reboots in test for EnodeB " + eNodeB.getName() + " was more than 0";
			}
			report.report("Number of unexpected reboots in test for EnodeB " + eNodeB.getName() + " was more than 0",
					Reporter.FAIL);
			String keyProperty = eNodeB.getName() + "," + "Unexpected Reboots" + "," + "FAIL";
			testStats.put(keyProperty, value);
		}

		String key = eNodeB.getNetspanName();
		value += unexpectedInScenario.get(key);
		unexpectedInScenario.put(key, value);
		if (value != 0) {
			GeneralUtils.printToConsole(
					"----added unexpected reboots to scenario statistics----" + eNodeB.getNetspanName() + "----");
			String keyProperty = eNodeB.getName() + "," + "Unexpected Reboots" + "," + "WARNING";
			scenarioStats.put(keyProperty, value);
		}
	}

	private void initTestListener() {
		if (null == testListener) {
			testListener = new TestListener();
			ListenerstManager.getInstance().addListener(testListener);
		}
	}

	/**
	 * 
	 * dumping all db from node to /bs/db ,creating a scpClient and a local folder
	 * with the name <enbName> + "BeforeTest" --> scpClient getting all files from
	 * EnodeB with filters of what names feature needs. saves files names in a map
	 * with enodeB.name -> arrayList of String --> filesFromDBPerNode
	 * 
	 * @author sshahaf
	 * @param eNodeB
	 * @return
	 */
	private Boolean getDBFiles(EnodeB eNodeB, String beforeTestExtention) {
		initDBFilesFeature(eNodeB, beforeTestExtention);

		dumpDBAndCreateLocalDirectory(eNodeB, eNodeB.getName() + beforeTestExtention);

		String filesNames = getFileNamesFromNode(eNodeB);
		if (filesNames == null) {
			report.report("could not get files from node!");
			return false;
		}
																							
		ArrayList<String> filesFromDB = filterFileNames(filesNames, ".*\\.xml", "Stat", "stat", "smonMeasurement.xml");
		if (filesFromDB.isEmpty()) {
			return false;
		}
		GeneralUtils.printToConsole("add files names to map - node : " + eNodeB.getName());
		filesFromDBPerNode.put(eNodeB.getName(), filesFromDB);

		if (!transferFilesToLocalComputer(scpCli, filesFromDB, eNodeB, beforeTestExtention)) {
			report.report("Could not get files from Node");
			return false;
		}

		return true;
	}

	private void initDBFilesFeature(EnodeB enb, String beforeTest) {
		gotDBFiles = false;
		GeneralUtils.deleteFolder(basePath + "\\" + enb.getName() + beforeTest);
	}

	private Boolean transferFilesToLocalComputer(ScpClient scpClientFromNode, ArrayList<String> filesNames, EnodeB enb,
			String testExtention) {
		String[] filesArr;
		ArrayList<String> filesWithPaths = new ArrayList<String>();
		GeneralUtils.printToConsole("add /bs/db to all files names to get them in one SCP request.");
		for (String str : filesNames) {
			filesWithPaths.add("/bs/db/" + str);
		}
		GeneralUtils.printToConsole("arrayList to array of strings");
		filesArr = new String[filesWithPaths.size()];
		filesArr = filesWithPaths.toArray(filesArr);
		try {
			GeneralUtils.printToConsole("getting all files from node with scp client");
			if (!scpClientFromNode.getFiles(System.getProperty("user.dir") + "\\" + enb.getName() + testExtention,
					filesArr)) {
				report.report("Could not get file with the path : /bs/db/");
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private void dumpDBAndCreateLocalDirectory(EnodeB dut, String directoryName) {
		dumpAllDBToNode(dut);
		createLocalFolder(directoryName);
		scpCli = new ScpClient(dut.getIpAddress(), dut.getScpUsername(), dut.getScpPassword(), dut.getScpPort());
	}

	private void createLocalFolder(String name) {
		File folder = new File(name);
		folder.mkdir();
	}

	private void dumpAllDBToNode(EnodeB dut) {
		String result = dut.lteCli("db dump all");
		if (result.contains("Dump non-persistent tables to xml completed")) {
			GeneralUtils.printToConsole("Db dump Succesfully!");
		}
	}

	/**
	 * chmod 777 on /bs/db ls -1a returning result if there is no 'not found' in it
	 * -> else return null.
	 * 
	 * @return
	 */
	private String getFileNamesFromNode(EnodeB dut) {
		String result = dut.shell("chmod 777 /bs/db");
		GeneralUtils.printToConsole(result);
		String cdCommand = dut.shell("cd /bs/db");
		GeneralUtils.printToConsole(cdCommand);
		String lsResult = dut.shell("ls -1a >> files");
		if (lsResult.contains("not found")) {
			return null;
		}
		if (!scpCli.getFiles(System.getProperty("user.dir"), "/bs/db/files")) {
			report.report("could not get files names file from Node");
			return null;
		}
		dut.shell("rm /bs/db/files");
		String filesNames = readFile("files");
		return filesNames;
	}

	private String readFile(String path) {
		StringBuilder sb = new StringBuilder();
		File file = new File(path);
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = br.readLine();
			while (line != null) {
				sb.append(line + "\n");
				line = br.readLine();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sb.toString();
	}

	private ArrayList<String> filterFileNames(String files, String allowedPattern, String... notAllowedContaines) {
		ArrayList<String> filesList = new ArrayList<>();
		filesList = filterAllowedNamesPattern(files, allowedPattern);
		filesList = filterNotAllowedNames(filesList, notAllowedContaines);
		return filesList;
	}

	private ArrayList<String> filterAllowedNamesPattern(String files, String allowedPattern) {
		GeneralUtils.printToConsole("filter allowed Names with Filter - " + allowedPattern);
		ArrayList<String> returnFiles = new ArrayList<String>();
		files = files.trim();
		Pattern patern = Pattern.compile(allowedPattern);
		Matcher m = patern.matcher(files);
		while (m.find()) {
			returnFiles.add(m.group(0));
		}
		return returnFiles;
	}

	public ArrayList<String> filterNotAllowedNames(ArrayList<String> files, String... notAllowedContains) {
		GeneralUtils.printToConsole("filtering DB Files names with not allowed Filters : " + notAllowedContains);
		ArrayList<String> notStatFiles = new ArrayList<String>();
		for (String str : files) {
			if (!checkIfStringAllowed(str, notAllowedContains)) {
				continue;
			}
			notStatFiles.add(str);
		}
		GeneralUtils.printToConsole("a complete list of files names contains " + notStatFiles.size() + " files");
		return notStatFiles;
	}

	private boolean checkIfStringAllowed(String str, String[] limitations) {
		for (String limitation : limitations) {
			if (str.contains(limitation)) {
				return false;
			}
		}
		return true;
	}

	private void compareDBs(EnodeB enb) {
		ArrayList<String> filesNamesNode = filesFromDBPerNode.get(enb.getName());
		if (filesNamesNode == null) {
			report.report("Node " + enb.getName() + " has no allocation for DB files");
			return;
		}

		HashMap<String, List<?>> reportMap = compareFilesWithDBs(enb, filesNamesNode);

		reportComparison(enb, reportMap);

		GeneralUtils.deleteFolder(basePath + "\\" + enb.getName() + beforeTest);
		GeneralUtils.deleteFolder(basePath + "\\" + enb.getName() + afterTest);
		GeneralUtils.deleteFileIfExists(basePath + "\\" + "files");
	}

	private HashMap<String, List<?>> compareFilesWithDBs(EnodeB enb, ArrayList<String> filesNames) {
		HashMap<String, List<?>> filesDifferencesMap = new HashMap<>();
		GeneralUtils.printToConsole("there are " + filesNames.size() + " files in current checking list");
		for (String fileName : filesNames) {
			File file1 = new File(System.getProperty("user.dir") + "\\" + enb.getName() + beforeTest + "\\" + fileName);
			File file2 = new File(System.getProperty("user.dir") + "\\" + enb.getName() + afterTest + "\\" + fileName);

			if (file1.exists() && file2.exists()) {
				List<?> currentFileDifferences = getDifferencesFromFile(file1, file2);
				if (!currentFileDifferences.isEmpty()) {
					GeneralUtils.printToConsole("adding differences to map of differences to report them later");
					filesDifferencesMap.put(fileName, currentFileDifferences);
				}
			}
		}
		return filesDifferencesMap;
	}

	private List<?> getDifferencesFromFile(File file1, File file2) {
		List<?> differences = new ArrayList<Object>();
		BufferedReader brF1 = null;
		BufferedReader brF2 = null;
		try {
			brF1 = new BufferedReader(new FileReader(file1));
			brF2 = new BufferedReader(new FileReader(file2));

			XMLUnit.setIgnoreWhitespace(true);
			XMLUnit.setIgnoreAttributeOrder(true);
			DetailedDiff diff = new DetailedDiff(XMLUnit.compareXML(brF1, brF2));

			brF1.close();
			brF2.close();
			differences = diff.getAllDifferences();

		} catch (Exception e) {
			GeneralUtils.printToConsole("DB Compare: " + file1.getName() + " is containing illegal XML content");
		}

		return differences;
	}

	private void reportComparison(EnodeB enb, HashMap<String, List<?>> reportMap) {
		if (reportMap.isEmpty()) {
			report.report(enb.getName() + " has no Differences in DBs");
			return;
		}

		GeneralUtils.startLevel(enb.getName() + " db Comparison");
		for (String fileName : reportMap.keySet()) {
			List<?> singleFileDifferences = reportMap.get(fileName);
			GeneralUtils.startLevel(fileName);
			for (Object difference : singleFileDifferences) {

				String str = difference.toString();
				int comparingIndex = str.indexOf(" comparing ");
				int toWordIndex = str.indexOf(" to ");

				if(toWordIndex > comparingIndex) {
					String startToFirstSplit = str.substring(0, comparingIndex);					
					String firstSplitToSecondSplit = str.substring(comparingIndex, toWordIndex);
					String secondSplitToEnd = str.substring(toWordIndex, str.length());
					
					report.report(startToFirstSplit);
					report.report(firstSplitToSecondSplit);
					report.report(secondSplitToEnd);
				}else {
					report.report("default comparison: ");
					report.report(str);
				}
			}
			GeneralUtils.stopLevel();
		}
		GeneralUtils.stopLevel();
	}

}
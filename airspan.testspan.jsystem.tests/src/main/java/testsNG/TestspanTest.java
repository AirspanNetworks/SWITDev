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

import org.apache.commons.lang3.StringUtils;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.After;
import org.junit.Assume;
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
import Utils.ScenarioUtils;
import Utils.ScpClient;
import Utils.SetupUtils;
import Utils.Reporters.GraphAdder;
import Utils.Snmp.MibReader;
import Utils.WatchDog.CommandMemoryCPU;
import Utils.WatchDog.CommandWatchInService;
import Utils.WatchDog.WatchDogManager;
import jsystem.framework.ParameterProperties;
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
    private ScpClient scpCli;
    private final String basePath = System.getProperty("user.dir");
    private final String beforeTest = "BeforeTest";
    private final String afterTest = "AfterTest";
    private ArrayList<CommandMemoryCPU> cpuCommands = new ArrayList<CommandMemoryCPU>();
    private ArrayList<CommandWatchInService> inServiceCommands = new ArrayList<CommandWatchInService>();
    private StringBuilder coreFilesPath;
    private boolean isCoreOccurDuringTest;

    private HashMap<String, ArrayList<String>> filesFromDBPerNode = new HashMap<String, ArrayList<String>>();

    /**
     * The Constant SYSOBJ_STRING_DELIMITER.
     */
    protected String reason = StringUtils.EMPTY;

    /**
     * Must set enbInTest!
     */
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
    protected String isTestWasSuccessful = StringUtils.EMPTY;

    public static final String DEFAULT_SOURCE_POM_PROPERTIES_FILE_NAME = "pom.properties";
    public static final String PATH_TO_POM_PROPERTIES = System.getProperty("user.dir") + File.separator + "target"
            + File.separator + "maven-archiver" + File.separator + DEFAULT_SOURCE_POM_PROPERTIES_FILE_NAME;
    private boolean enbsAreInService = true;

    private static HashMap<String, Integer> testStats;
    private static HashMap<String, Integer> scenarioStats;

    /**
     * Counts all the unexpected reboots, on all EnodeBs, while 1 scenario.
     * String - EnodeB name
     * Integer - Reboot status (0\1)
     */
    private static HashMap<String, Integer> unexpectedRebootHash = null;
    protected WatchDogManager wd;
    private boolean performDbComperison;
    /**
     * Initialize function
     *
     * @throws Exception - exception
     */
    @Before
    public void init() throws Exception {
        testStats = new HashMap<String, Integer>();
        scenarioStats = ScenarioUtils.getInstance().getScenarioStats();
        netspanServer = NetspanServer.getInstance();
        alarmsAndEvents = AlarmsAndEvents.getInstance();
        wd = WatchDogManager.getInstance();
        addToListenerManager();
        initUnexpectedRebootMap();
        GeneralUtils.startLevel("Pre Test");
        validateEnbIsSet();
        printMemoryConsumptionToConsole();
        checkAutoVersion();
        initAllEnodeBInSetupList();
        //Loop on all the EnodeBs in the current test
        for (EnodeB eNodeB : enbInTest) {
            if (isEnodeBBlackListed(eNodeB)) break;
            initEnodeB(eNodeB);
        }
        setDebugFtpServer();
        GeneralUtils.stopLevel();
        verifyEnBsAreInService();
    }

    /**
     * Init EnodeB
     *
     * @param eNodeB - eNodeB
     */
    private void initEnodeB(EnodeB eNodeB) {
        initUnexpectedRebootOfEnB(eNodeB);
        createEnodeBLogFiles(eNodeB);
        addEnodeBToUnexpectedRebootMap(eNodeB.getNetspanName());
        EnodeBConfig.getInstance().isEnodebManaged(eNodeB);
        eNodeB.setDeviceUnderTest(true);
        swapBanksBackIfNeeded(eNodeB);
        alarmsAndEvents.deleteAllAlarmsNode(eNodeB);
        initCommandWatchInService(eNodeB);
        initMemoryCPUCommand(eNodeB);
        PeripheralsConfig.getInstance().changeEnbState(eNodeB, EnbStates.IN_SERVICE);
        //todo remove to debug.
        enbsAreInService = false;
//        waitForAllRunningAndInService(eNodeB);
        reportIfCMPWasSkipped(eNodeB);
        eNodeB.setCellContextNumber(1);
        showLoginStatus(eNodeB);
        captureMACToPHY(eNodeB);
        eNodeB.setNodeLoggerUrl(eNodeB, TestConfig.getInstace().getLoggerUploadAllUrl());
        checkAndSetDefaultNetspanProfiles(eNodeB);
        getDBFiles(eNodeB, beforeTest);
    }

    /**
     * Verify EnodeBs Are In Service, fail and stop test if not.
     */
    private void verifyEnBsAreInService() {
        if (!enbsAreInService)
            report.report("One or more of the enbs failed to reach all running state, failing and stopping test.",
                    Reporter.FAIL);
        Assume.assumeTrue(enbsAreInService);
    }

    /**
     * Check And Set Default Netspan Profiles
     *
     * @param eNodeB - eNodeB
     */
    private void checkAndSetDefaultNetspanProfiles(EnodeB eNodeB) {
    	if(eNodeB.isNetspanProfilesVerified())
    		return;
        try {
        	if(netspanServer.checkAndSetDefaultProfiles(eNodeB, false))
        		eNodeB.setNetspanProfilesVerified(true);
        } catch (Exception e) {
            e.printStackTrace();
            report.report("checkAndSetDefaultProfiles failed due to: " + e.getMessage(), Reporter.WARNING);
        }
    }

    /**
     * Capture MAC to PHY, "isMACtoPHYEnabled" param is being taken from the SUT, set to false by default.
     *
     * @param eNodeB - eNodeB
     */
    private void captureMACToPHY(EnodeB eNodeB) {
        if (eNodeB.isMACtoPHYEnabled()) {
            eNodeB.enableMACtoPHYcapture(SnifferFileLocation.Remote);
        }
    }

    /**
     * Start reporter level, show login status, and stop reporter level.
     *
     * @param eNodeB - eNodeB
     */
    private void showLoginStatus(EnodeB eNodeB) {
        GeneralUtils.startLevel("Session connection status");
        eNodeB.showLoginStatus();
        GeneralUtils.stopLevel();
    }

    /**
     * Print report if SKIP CMPv2
     *
     * @param eNodeB - eNodeB
     */
    private void reportIfCMPWasSkipped(EnodeB eNodeB) {
        if (eNodeB.isSkipCMP()) {
            report.report("EnodeB " + eNodeB.getNetspanName() + " is working with SKIP CMPv2");
        }
    }

    /**
     * Wait For All Running - deal with 2 cases: SWUpgrade and the rest of the tests
     * In case of SWUpgrade - wait for all running anyway (this test will always run first)
     *
     * @param eNodeB - eNodeB
     */
    private void waitForAllRunningAndInService(EnodeB eNodeB) {
        if (this instanceof SWUpgrade) {
            report.report(eNodeB.getNetspanName() + ": Wait for all Running (TimeOut=" + 1 + " Minutes)");
            if (!eNodeB.waitForAllRunningAndInService(60 * 1000)) {
                report.report(eNodeB.getNetspanName() + " failed to reach all running.", Reporter.WARNING);
                report.report("SW upgrade test. Trying to upgrade when enodeb in no all running state");
            }
        } else {
            report.report(eNodeB.getNetspanName() + ": Wait for all Running (TimeOut=" +
                    (EnodeB.WAIT_FOR_ALL_RUNNING_TIME / 60000) + " Minutes)");
            if (!eNodeB.waitForAllRunningAndInService(EnodeB.WAIT_FOR_ALL_RUNNING_TIME)) {
                report.report(eNodeB.getNetspanName() + " failed to reach all running.", Reporter.WARNING);
                enbsAreInService = false;
                eNodeB.blackListed = true;
            }
        }
    }

    /**
     * Init Memory CPU Command
     *
     * @param eNodeB - eNodeB
     */
    private void initMemoryCPUCommand(EnodeB eNodeB) {
        CommandMemoryCPU cpuCommand = new CommandMemoryCPU(eNodeB);
        cpuCommands.add(cpuCommand);
        wd.addCommand(cpuCommand);
        report.report("setting WD CPU and Memory Command for enodeB " + eNodeB.getName());
    }

    /**
     * Init Command Watch In Service
     *
     * @param eNodeB - eNodeB
     */
    private void initCommandWatchInService(EnodeB eNodeB) {
        CommandWatchInService commandInService = new CommandWatchInService(eNodeB);
        commandInService.name = eNodeB.getName() + "_commandInService";
        wd.addCommand(commandInService);
        inServiceCommands.add(commandInService);
    }

    /**
     * Init Unexpected Reboot Of EnB to 0
     *
     * @param eNodeB - current eNodeB
     */
    private void initUnexpectedRebootOfEnB(EnodeB eNodeB) {
        eNodeB.setUnexpectedReboot(0);
    }

    /**
     * Check if bank was swapped and swap it back.
     *
     * @param eNodeB - eNodeB
     */
    private void swapBanksBackIfNeeded(EnodeB eNodeB) {
        if (eNodeB.isBankSwapped()) {
            report.report("Bank was swapped on " + eNodeB.getNetspanName() + ". Trying to swap bank back");
            eNodeB.reboot(true);
            eNodeB.waitForAllRunningAndInService(EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
        }
    }

    /**
     * Adding the EnodeB to the "unexpected reboot hashMap" if it wan't added.
     *
     * @param enbName - enbName
     */
    private void addEnodeBToUnexpectedRebootMap(String enbName) {
        if (!unexpectedRebootHash.containsKey(enbName)) {
            unexpectedRebootHash.put(enbName, 0);
        }
    }

    /**
     * Recognize and define all the nodeBs in setup - (SUT)
     */
    private void initAllEnodeBInSetupList() {
        GeneralUtils.startLevel("Loading setup's enodeBs");
        enbInSetup = SetupUtils.getInstance().getAllEnb();
        GeneralUtils.stopLevel();
    }

    /**
     * Initialize the Hash map
     */
    private void initUnexpectedRebootMap() {
        unexpectedRebootHash = ScenarioUtils.getInstance().getUnexpectedRebootInScenario();
    }

    /**
     * Create EnodeB Log Files, initialize the EnodeB counters.
     *
     * @param eNodeB - eNodeB
     */
    private void createEnodeBLogFiles(EnodeB eNodeB) {
        GeneralUtils.printToConsole(String.format("Creating log files for test for eNondeB %s", eNodeB.getName()));
        Logger[] loggers = eNodeB.getLoggers();
        for (Logger logger : loggers) {
            logger.startLog(String.format("%s_%s", getMethodName(), logger.getParent().getName()));
            logger.clearTestCounters();
            logger.setCountErrorBool(true);
        }
    }

    /**
     * Checks if the EnodeB is in "All running" black list, i.e. failed to reach all running.
     *
     * @param eNodeB - eNodeB
     * @return - true if it's black listed
     */
    private boolean isEnodeBBlackListed(EnodeB eNodeB) {
        //Black listed means: EnB failed to reach all running in previous tests
        boolean isInBlackList = false;
        if (eNodeB.blackListed) {
            boolean isRemoved = removeEnBFromBlackListIfNeeded(eNodeB);
            if (!isRemoved) {
                enbsAreInService = false;
                report.report(eNodeB.getName() + " is black listed for failing to reach all running. stopping test.",
                        Reporter.WARNING);
                isInBlackList = true;
            }
        }
        return isInBlackList;
    }

    /**
     * Remove from BlackList in case EnB is all running.
     *
     * @param eNodeB - eNodeB
     * @return - true - removed from Black List
     */
    private boolean removeEnBFromBlackListIfNeeded(EnodeB eNodeB) {
        boolean isAllRunning = eNodeB.isAllRunning();
        if (isAllRunning) {
            eNodeB.blackListed = false;
        }
        return isAllRunning;
    }

    /**
     * Check Automation Version and define it in the report prop file
     */
    private void checkAutoVersion() {
        String automationVersion = getVersion();
        report.setContainerProperties(0, "Version", automationVersion);
    }

    /**
     * Throws exception if any EnodeB was not set by the user.
     *
     * @throws Exception - Exception
     */
    private void validateEnbIsSet() throws Exception {
        //todo - consider remove: I found no use for the condition: enbInTest.contains(null)
        if (enbInTest == null || enbInTest.contains(null)) {
            GeneralUtils.stopAllLevels();
            throw new Exception("must set enbInTest!!!!!!");
        }
    }

    private static boolean isFTPServerSet = false;

    /**
     * Set the FTP server param to the every EnB in the setup. Once in run
     */
    private void setDebugFtpServer() {
        DebugFtpServer debugFtpServer = DebugFtpServer.getInstance();
        if (!isFTPServerSet) {
            for (EnodeB eNodeB : enbInSetup) {
                try {
                    GeneralUtils.printToConsole("Set debug FTP server.");
                    String oid = MibReader.getInstance().resolveByName("asLteStkDebugFtpServerCfgFtpAddressType");
                    if(eNodeB.getSNMP(oid).equals("noSuchInstance"))
                    	eNodeB.lteCli("db add debugftpserver [1]");
                    if(!eNodeB.getSNMP(oid).equals(debugFtpServer.addressType))
                    	eNodeB.snmpSet(oid, Integer.parseInt(debugFtpServer.addressType));
                    eNodeB.snmpSet(oid, Integer.parseInt(debugFtpServer.addressType));
                    oid = MibReader.getInstance().resolveByName("asLteStkDebugFtpServerCfgFtpServerIp");
                    eNodeB.snmpSet(oid, debugFtpServer.getDebugFtpServerIP());
                    oid = MibReader.getInstance().resolveByName("asLteStkDebugFtpServerCfgFtpAddress");
                    eNodeB.snmpSet(oid, debugFtpServer.getDebugFtpServerIPInBytes());
                    oid = MibReader.getInstance().resolveByName("asLteStkDebugFtpServerCfgFtpUser");
                    eNodeB.snmpSet(oid, debugFtpServer.getDebugFtpServerUser());
                    oid = MibReader.getInstance().resolveByName("asLteStkDebugFtpServerCfgFtpPassword");
                    eNodeB.snmpSet(oid, debugFtpServer.getDebugFtpServerPassword());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            isFTPServerSet = true;
        }
    }

    /**
     * Get The automation version from pom file
     *
     * @return - String - version number
     */
    public String getVersion() {
        String version = StringUtils.EMPTY;
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
        if (StringUtils.EMPTY == version) {
            version = "No version found";
        }
        return version;
    }

    /**
     * End Method will be called after every test. The if statement will evaluate if the test was successful or not.
     * If test failed for any reason something like assertion failure or Exception of any kind The
     * <p>isTestWasSuccessful</p> will be assigned with default value that is "FAIL", if test was success it will be
     * assigned with "PASS"
     **/
    @After
    public void end() {
        GeneralUtils.startLevel("Post Test");
        coreFilesPath = new StringBuilder(StringUtils.EMPTY);
        EnodeBConfig.getInstance().deleteClonedProfiles();
        printMemoryInformation();
        WatchDogManager.getInstance().shutDown();
        changeIsTestWasSuccessfulParam();
        ScenarioUtils.getInstance().calledOnceInEndFunc();
        for (EnodeB eNodeB : enbInTest) {
            endEnodeB(eNodeB);
        }
        uploadParamsToReporter();
        printMemoryConsumptionToConsole();
        GeneralUtils.stopLevel();
    }

    /**
     * Upload Params To Reporter
     */
    private void uploadParamsToReporter() {
        setReasonWhenCoreOccur(isCoreOccurDuringTest);
        printFailReasonToPropFile();
        printCoreFilesToPropFile(coreFilesPath);
        handleLogCounter();
    }

    /**
     * Tear down EnodeB Params
     *
     * @param eNodeB - eNodeB
     */
    private void endEnodeB(EnodeB eNodeB) {
        coreFilesPath.append(getCoreFilePathList(eNodeB));
        isCoreOccurDuringTest |= eNodeB.isStateChangedToCoreDump();
        printAlarmsInfo(eNodeB);
        addRunningVersionProp(eNodeB);
        getAndCompareDBFiles(eNodeB);
        closeLog(eNodeB);
        setUnexpectedRebootStatistics(eNodeB);
        printAndCloseMACtoPHYCapture(eNodeB);
        //If Core dump occurs once, then == true
        eNodeB.clearTestParameters();
        eNodeB.setDeviceUnderTest(false);
        eNodeB.loggerUploadAll();
    }

    /**
     * Print Memory Consumption
     */
    private void printMemoryConsumptionToConsole() {
        GeneralUtils.printToConsole(ScenarioUtils.getInstance().getMemoryConsumption());
    }

    /**
     * Add Log Test Counter, and print Log Scenario Counter To Prop File
     */
    private void handleLogCounter() {
        addLogTestCounterToPropFile();
        printLogScenarioCounterToPropFile();
    }

    /**
     * Print Log Scenario Counter To Prop File
     */
    private void printLogScenarioCounterToPropFile() {
        String scenarioStatsLogCounter = getLogCounter(scenarioStats);
        if (!scenarioStatsLogCounter.isEmpty()) {
            report.setContainerProperties(0, "LogCounter", scenarioStatsLogCounter);
        }
    }

    /**
     * Add Log Counter To Prop File
     */
    private void addLogTestCounterToPropFile() {
        String testStatsLogCounter = getLogCounter(testStats);
        if (!testStatsLogCounter.isEmpty()) {
            report.addProperty("LogCounter", testStatsLogCounter);
        }
    }

    /**
     * GetLogCounter in order to print it and add it to prop file
     */
    private String getLogCounter(HashMap<String, Integer> hashMapStatus) {
        if (hashMapStatus != null && !hashMapStatus.isEmpty()) {
            StringBuilder logCounter = new StringBuilder(StringUtils.EMPTY);
            for (String key : hashMapStatus.keySet()) {
                logCounter.append(getLogCounterStringPattern(key, hashMapStatus));
            }
            return logCounter.toString();
        }
        return StringUtils.EMPTY;
    }

    /**
     * Get Log Counter String Pattern, to be printed.
     *
     * @param key   - key
     * @param entry - entry
     * @return - Log Counter String Pattern
     */
    private String getLogCounterStringPattern(String key, HashMap<String, Integer> entry) {
        return key + "," + entry.get(key) + ScenarioUtils.SYSOBJ_STRING_DELIMITER;
    }

    /**
     * Print Core Files To Prop File
     *
     * @param coreFilesPath - coreFilesPath
     */
    private void printCoreFilesToPropFile(StringBuilder coreFilesPath) {
        if (!coreFilesPath.toString().isEmpty()) {
            report.addProperty("CoreFiles", coreFilesPath.toString());
        }
    }

    /**
     * Print Fail Reason To Prop File
     */
    private void printFailReasonToPropFile() {
        if (!reason.isEmpty()) {
            report.addProperty("failureReason", reason);
            report.report("Fail reason: " + reason);
        }
    }

    /**
     * Set this.reason (of failed test) when isCoreOccurDuringTest=true
     *
     * @param isCoreOccurDuringTest - isCoreOccurDuringTest bool
     */
    private void setReasonWhenCoreOccur(boolean isCoreOccurDuringTest) {
        if (isCoreOccurDuringTest) {
            if (!reason.isEmpty()) {
                reason += "<br>Core occurred during test.";
            } else {
                reason = "Core occurred during test.";
            }
        }
    }

    /**
     * get Core File Path List
     *
     * @param eNodeB - eNodeB
     * @return - coreFilesPath
     */
    private String getCoreFilePathList(EnodeB eNodeB) {
        String coreFilesPath = StringUtils.EMPTY;
        int coreIndex = 1;
        String coreValue = StringUtils.EMPTY;
        HashSet<String> corePathList = eNodeB.getCorePathList();
        for (String corePath : corePathList) {
            coreValue += (coreIndex + "," + eNodeB.getName() + "," + corePath
                    + ScenarioUtils.SYSOBJ_STRING_DELIMITER);
            coreIndex++;
            coreFilesPath += coreValue;
        }
        return coreFilesPath;
    }

    /**
     * Print MACtoPHYCapture And disable capture (if enabled before)
     *
     * @param eNodeB - eNodeB
     */
    private void printAndCloseMACtoPHYCapture(EnodeB eNodeB) {
        if (eNodeB.isMACtoPHYEnabled()) {
            eNodeB.disableMACtoPHYcapture();
        }
        eNodeB.showMACtoPHYCaptureFiles();
    }

    /**
     * Close Logs - Auto & EnodeB
     *
     * @param eNodeB - eNodeB
     */
    private void closeLog(EnodeB eNodeB) {
        Logger[] loggers = eNodeB.getLoggers();
        closeEnBLogs(eNodeB, loggers);
        closeAutomationLogs(eNodeB, loggers);
    }

    /**
     * Close Automation Logs
     *
     * @param eNodeB  - eNodeB
     * @param loggers - loggers
     */
    private void closeAutomationLogs(EnodeB eNodeB, Logger[] loggers) {
        GeneralUtils.startLevel(String.format("eNodeB %s Automation logs", eNodeB.getName()));
        for (Logger logger : loggers) {
            logger.closeAutoLog(String.format("%s_%s", getMethodName(), logger.getParent().getName()));
        }
        GeneralUtils.stopLevel();
    }

    /**
     * Close EnB Logs
     *
     * @param eNodeB  - eNodeB
     * @param loggers - loggers
     */
    private void closeEnBLogs(EnodeB eNodeB, Logger[] loggers) {
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
                    String.format(LOGGER_UPLOAD_ALL_LINK, dateFormat.format(date), loggerUploadAllEnodebIP.replace(":", ".")));
        } else {
            report.report("Logger upload all not available, due to missing IP.");
        }
        GeneralUtils.stopLevel();
    }

    /**
     * Get And Compare DB Files
     *
     * @param eNodeB - eNodeB
     */
    private void getAndCompareDBFiles(EnodeB eNodeB) {
    	if(performDbComperison){
        ArrayList<String> fileNameList = filesFromDBPerNode.get(eNodeB.getName());
	        if ((fileNameList != null) && (!fileNameList.isEmpty())) {
	            getDBFiles(eNodeB, afterTest);
	            compareDBs(eNodeB);
	        }
    	}
    }

    /**
     * Add Running Version Prop
     *
     * @param eNodeB - eNodeB
     */
    private void addRunningVersionProp(EnodeB eNodeB) {
        report.addProperty(eNodeB.getNetspanName() + "_Version", eNodeB.getRunningVersion());
    }

    /**
     * Print Alarms Info, for EnodeB, if there are any.
     *
     * @param eNodeB - eNodeB
     */
    private void printAlarmsInfo(EnodeB eNodeB) {
        List<AlarmInfo> alarmsInfo = alarmsAndEvents.getAllAlarmsNode(eNodeB);
        if (!alarmsInfo.isEmpty()) {
            GeneralUtils.startLevel(eNodeB.getName() + "'s Alarms: ");
            for (AlarmInfo alarmInfo : alarmsInfo) {
                alarmsAndEvents.printAlarmInfo(alarmInfo);
            }
            GeneralUtils.stopLevel();
        } else {
            report.report(eNodeB.getName() + "'s Alarms list is empty");
        }
    }

    /**
     * change "IsTestWasSuccessful" Param according to "isPass" Jsystem param.
     */
    private void changeIsTestWasSuccessfulParam() {
        if (this.isPass) {
            isTestWasSuccessful = "PASS";
        } else {
            isTestWasSuccessful = "FAIL";
        }
        GeneralUtils.printToConsole("Test Status: " + isTestWasSuccessful);
    }

    /**
     * print Memory Information - Tables and graph
     */
    private void printMemoryInformation() {
        printMemoryTables(cpuCommands);
        printMemoryGraph(cpuCommands);
    }

    /**
     * print Memory Graph
     *
     * @param cpuCommands2 - cpu Commands
     */
    private void printMemoryGraph(ArrayList<CommandMemoryCPU> cpuCommands2) {
        if (cpuCommands2 == null) {
            report.report("cpu and memory watchdog hasn't been initiated!");
            return;
        }
        for (CommandMemoryCPU command : cpuCommands2) {
            printGraphForCPUCommand(command);
        }

    }

    private void printGraphForCPUCommand(CommandMemoryCPU command) {
        //make integers lists into double lists.
        ArrayList<Double> memoryDouble = turnIntArrayToDoubleArray(command.getMemoryValuesList());
        ArrayList<Double> cpuDouble = turnIntArrayToDoubleArray(command.getCpuValuesList());
        ArrayList<Long> time = new ArrayList<Long>();

        //initialize time just for order of the graph
        time = initTimeList(memoryDouble.size());

        try {
            GraphAdder.AddGraph(String.format("%s %s", "graph Test", command.getNodeName()), "time stamp / sec", "node % usage", memoryDouble, cpuDouble, time, "memory", "cpu", false);
        } catch (Exception e) {
            e.printStackTrace();
            report.report("problem while trying to create memory graphs! - check console");
        }
    }

    private ArrayList<Long> initTimeList(int size) {
        ArrayList<Long> returnList = new ArrayList<Long>();
        for (int i = 0; i < size; i++) {
            returnList.add(new Long(i));
        }
        return returnList;
    }

    private ArrayList<Double> turnIntArrayToDoubleArray(ArrayList<Integer> list) {
        ArrayList<Double> doubleList = new ArrayList<Double>();
        for (Integer i : list) {
            doubleList.add(i.doubleValue());
        }
        return doubleList;
    }


    /**
     * Print Memory Tables
     *
     * @author sshahaf
     */
    private void printMemoryTables(ArrayList<CommandMemoryCPU> cpuCommands2) {
        if (cpuCommands2 == null) {
            report.report("cpu and memory watchdog hasn't been initiated!");
            return;
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
     * @param isTestWasSuccessful the isTestWasSuccessful to set
     */
    public void setIsTestWasSuccessful(String isTestWasSuccessful) {
        this.isTestWasSuccessful = isTestWasSuccessful;
    }

    /**
     * @return
     * @author Avichai Yefet Add all ERRORs & WARNINGs to test Properties
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

    /**
     * Set Unexpected Reboot Statistics of EnodeB
     *
     * @param eNodeB - eNodeB
     */
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
        value += unexpectedRebootHash.get(key);
        unexpectedRebootHash.put(key, value);
        if (value != 0) {
            GeneralUtils.printToConsole(
                    "----added unexpected reboots to scenario statistics----" + eNodeB.getNetspanName() + "----");
            String keyProperty = eNodeB.getName() + "," + "Unexpected Reboots" + "," + "WARNING";
            scenarioStats.put(keyProperty, value);
        }
    }

    /**
     * Initialize Test Listener (Jsystem HTML logs) - singleton
     */
    private void addToListenerManager() {
        TestListener testListener = TestListener.getInstance();
        ListenerstManager.getInstance().addListener(testListener);
    }

    /**
     * dumping all db from node to /bs/db ,creating a scpClient and a local folder
     * with the name <enbName> + "BeforeTest" --> scpClient getting all files from
     * EnodeB with filters of what names feature needs. saves files names in a map
     * with enodeB.name -> arrayList of String --> filesFromDBPerNode
     *
     * @param eNodeB
     * @return
     * @author sshahaf
     */
    private Boolean getDBFiles(EnodeB eNodeB, String beforeTestExtention) {
        if(performDbComperison){
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
        }
        return true;
    }

    private void initDBFilesFeature(EnodeB enb, String beforeTest) {
        GeneralUtils.deleteFolder(basePath + "\\" + enb.getName() + beforeTest);
    }

    private Boolean transferFilesToLocalComputer(ScpClient
                                                         scpClientFromNode, ArrayList<String> filesNames, EnodeB enb,
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
            if (!scpClientFromNode.getFiles(System.getProperty("user.dir") + File.separator + enb.getName() + testExtention,
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
        GeneralUtils.printToConsole("chmod 777 /bs/db response: " + result);
        String cdCommand = dut.shell("cd /bs/db");
        GeneralUtils.printToConsole("cd /bs/db response: " + cdCommand);
        String lsResult = dut.shell("ls -1a >> files");
        GeneralUtils.printToConsole("ls -1a >> files response: " + lsResult);
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

                if (toWordIndex > comparingIndex) {
                    String startToFirstSplit = str.substring(0, comparingIndex);
                    String firstSplitToSecondSplit = str.substring(comparingIndex, toWordIndex);
                    String secondSplitToEnd = str.substring(toWordIndex, str.length());

                    report.report(startToFirstSplit);
                    report.report(firstSplitToSecondSplit);
                    report.report(secondSplitToEnd);
                } else {
                    report.report("default comparison: ");
                    report.report(str);
                }
            }
            GeneralUtils.stopLevel();
        }
        GeneralUtils.stopLevel();
    }

    protected void changeCPUWDStatus(boolean enabled) {
        for (CommandMemoryCPU cmd : cpuCommands) {
            cmd.setEnabled(enabled);
        }
    }

    protected void changeInServiceWDStatus(boolean enabled) {
        for (CommandWatchInService cmd : inServiceCommands) {
            cmd.setEnabled(enabled);
        }
    }

    @ParameterProperties(description = "Set Perform Db Comperison")
	public void setPerformDbComperison(boolean performDbComperison) {
		this.performDbComperison = performDbComperison;
	}
}
package EnodeB.Components;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import EnodeB.Components.Cli.Cli;
import EnodeB.Components.Log.LogListener;
import EnodeB.Components.Log.Logger;
import EnodeB.Components.Log.LoggerEvent;
import EnodeB.Components.Session.Session;
import EnodeB.Components.Session.SessionManager;
import Utils.GeneralUtils;
import Utils.GeneralUtils.RebootType;
import Utils.MoxaCom;
import Utils.PingUtils;
import Utils.ScpClient;
import Utils.Snmp.MibReader;
import Utils.Snmp.SNMP;
import jsystem.framework.IgnoreMethod;
import jsystem.framework.report.Reporter;
import jsystem.framework.system.SystemObjectImpl;

/**
 * Implements a basic eNodeB component.
 */
public abstract class EnodeBComponent extends SystemObjectImpl implements LogListener {

	public static final String SECURED_USERNAME = "op";
	public static final String SECURED_PASSWORD = "Ss%7^q7NC#Uj!AnX";

	public static final String ADMIN_USERNAME = "admin";
	public static final String ADMIN_PASSWORD = "HeWGEUx66m=_4!ND";

	public static final String UNSECURED_USERNAME = "root";
	public static final String UNSECURED_PASSWORD = "air4best";
	
	public static final String UNSECURED_READCOMMUNITY = "public";
	public static final String UNSECURED_WRITECOMMUNITY = "private";

	public static final long CHECK_STATE_REST_TIME = 200;
	public static final long CHECK_STATE_DEFUALT_TIMEOUT = 720000;
	public static final long WAIT_FOR_SERIAL_PROMPT = 2 * 60 * 1000;
	public static final int PING_RETRIES = 3;
	public static final String SHELL_PROMPT_OP = "$ ";
	public static final String SHELL_PROMPT = "# ";
	public static final String LOGIN_PROMPT = "login: ";
	/**
	 * The Constant LTE_CLI_PROMPT.
	 */
	public static final String LTE_CLI_PROMPT = "lte_cli:>>";

	public static final List<String> rebootStrings =  Arrays.asList("Android Bootloader", "ASIL XLP Stage 1 Loader");
	public static final List<String> coreStrings = Arrays.asList("Corecare", "Segmentation fault", "middle of core dumping");
	
	protected String logFilePath;
	private volatile boolean expectBooting = false;

	private String ipAddress;
	
	private String username = SECURED_USERNAME;
	private String password = SECURED_PASSWORD;
	private String scpUsername = ADMIN_USERNAME;
	private String scpPassword = ADMIN_PASSWORD;
	private int scpPort = 22;
	private String serialUserName;
	private String serialPassword;
	private String serialSwitchUserCommand;
	
	private String readCommunity = UNSECURED_READCOMMUNITY;
	private String writeCommunity = UNSECURED_WRITECOMMUNITY;
	public MoxaCom serialCom;
	private Logger logger;
	protected SessionManager sessionManager;
	private boolean reachable;
	private ScpClient secureCopy;
	public SNMP snmp;
	private Object snmpLock = new Object();
	public Object credentialskLock = new Object();

	private HashSet<String> corePathSet;
	private boolean isStateChangedToCoreDump;
	private final String coreFtpSetver = "\\\\asil-rdat-shares\\cores\\";
	private boolean deviceUnderTest;
	private int unexpectedReboot = 0;
	public int SWTypeInstnace;
	public ArrayList<String> debugFlags;
	public boolean SkipCMP = false;
	private WaitForSrialPromptAndEchoToSkipCMP waitForSrialPromptAndEchoToSkipCMP;

	public boolean isExpectBooting() {
		return expectBooting;
	}

	public void setExpectBooting(boolean expectBooting) {
		this.expectBooting = expectBooting;
	}
	
	public boolean isSkipCMP() {
		return SkipCMP;
	}

	public void setSkipCMP(String skipCMP) {
		SkipCMP = Boolean.parseBoolean(skipCMP);
	}
	
	public Session getDefaultSession()
	{
		return sessionManager.getDefaultSession();
	}
	
	public Session getSSHlogSession() {
		return sessionManager.getSSHlogSession();
	}
	
	public Session getSerialSession() {
		return sessionManager.getSerialSession();
	}
	
	public boolean isDeviceUnderTest() {
		return deviceUnderTest;
	}

	public void setDeviceUnderTest(boolean deviceUnderTest) {
		this.deviceUnderTest = deviceUnderTest;
	}

	public void clearTestPrameters() {
		corePathSet.clear();
		isStateChangedToCoreDump = false;
	}

	public HashSet<String> getCorePathList() {
		return corePathSet;
	}

	public void setCorePathList(HashSet<String> corePathList) {
		this.corePathSet = corePathList;
	}

	public boolean isStateChangedToCoreDump() {
		return isStateChangedToCoreDump;
	}

	public void setStateChangedToCoreDump(boolean isStateChangedToCoreDump) {
		this.isStateChangedToCoreDump = isStateChangedToCoreDump;
	}

	public int getSWTypeInstnace() {
		return SWTypeInstnace;
	}

	public void setSWTypeInstnace(int sWTypeInstnace) {
		SWTypeInstnace = sWTypeInstnace;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Utils.TestspanSystemObject#init() init & connecting to serialCom.
	 * then connecting via SSH, if fails get the serialCom
	 */
	@Override
	public void init() throws Exception {
		super.init();
		corePathSet = new HashSet<String>();

		deviceUnderTest = false;

		isStateChangedToCoreDump = false;

		initSNMP();
		
		if (serialCom != null) {
			try {
				serialCom.init();
				serialCom.connect();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else
		GeneralUtils.printToConsole("SerialCom is missing! Initializing component without console accesss.");
		GeneralUtils.printToConsole("Unknown state, assuming that the component is Online.");

		if (logFilePath == null) {
			logFilePath = System.getProperty("user.dir") + "\\" + getName() + ".log";
		}

		sessionManager = new SessionManager(this);
		sessionManager.init();
		
		logger = new Logger(logFilePath, this, sessionManager, report);
		logger.addLogListener(this);
		logger.start();

		if (ipAddress != null) {
			// Init secure copy object with EnodeB IP
			secureCopy = new ScpClient(getIpAddress());
		}
		this.waitForSrialPromptAndEchoToSkipCMP = new WaitForSrialPromptAndEchoToSkipCMP(WAIT_FOR_SERIAL_PROMPT);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsystem.framework.system.SystemObjectImpl#close()
	 */
	@Override
	public void close() {
		logger.stop();
		sessionManager.closeAllSessions();
		super.close();
	}

	/**
	 * Adds known prompts on init.
	 *
	 * @param cli
	 *            the cli
	 */
	public void addPrompts(Cli cli) {
		cli.setPrompt(LOGIN_PROMPT);
		cli.addPrompt(SHELL_PROMPT, LOGIN_PROMPT, new String[] {serialUserName, serialPassword, serialSwitchUserCommand, EnodeBComponent.ADMIN_PASSWORD}, "exit");
	}

	/**
	 * Gets the component version.
	 *
	 * @return the version
	 * @throws IOException
	 */
	@IgnoreMethod
	public String getVersion() {
		int tries = 3;
		String version = "";
		do {
			String oid = MibReader.getInstance().resolveByName("asMaxBsCmSwRunningVersion") + "." + SWTypeInstnace;
			
			version = snmp.get(oid).trim();
			tries--;
		} while ((version == null || version.equals("")) && tries > 0);
		if (tries <= 0) {
			GeneralUtils.printToConsole("Could not get version via snmp for EnodeB " + getName());
		}
		return version;
	}

	/**
	 * Gets the ip address.
	 *
	 * @return the ip address
	 */
	public String getIpAddress() {
		return ipAddress;
	}

	/**
	 * ip address for SSH.
	 *
	 * @param ipAddress
	 *            the new ip address
	 */
	public synchronized void setIpAddress(String ipAddress) {
		if(ipAddress != this.ipAddress){ //avoid starting another Thread to same IP Address!
			this.ipAddress = ipAddress;
			final String ip = ipAddress; // so the thread method will compile.
			if (ipAddress != null){
				new Thread(new Runnable() {
	
					@Override
					public void run() {
						GeneralUtils.printToConsole("EnodeBComponent.setIpAddress thread start: ip="+ip+" getIpAddress()="+getIpAddress());
						boolean snmpWorking = true;
						while (ip.equals(getIpAddress())) {
							setReachable(PingUtils.isReachable(ip,EnodeBComponent.PING_RETRIES));
							if (!isReachable()) {
								GeneralUtils.printToConsole(getName() + " is unreachable.");
								snmpWorking = false;
							}
							
							if (!snmpWorking) {
								String oid = MibReader.getInstance().resolveByName("asLteStkCellStatusRunning");
								oid = oid + ".40";
								String ans = snmp.get(oid);
								if (ans != "" && !ans.equals(GeneralUtils.ERROR_VALUE + "")){
									GeneralUtils.printToConsole(getName() + " SNMP is working.");
									snmpWorking = true;
									sessionManager.updateAllSessionsLogLevel();
								}else{
									GeneralUtils.unSafeSleep(3000); //if snmp doesn't work, sleep before trying again.
								}
							}
						}
						GeneralUtils.printToConsole("EnodeBComponent.setIpAddress thread end: ip.equals(getIpAddress())==FALSE ip="+ip+" getIpAddress()="+getIpAddress());
					}
				}, getName() + " ping thread").start();
			}
		}
	}

	/**
	 * Gets the files using scp.
	 *
	 * @param localTargetDirectory
	 *            the local target directory
	 * @param remoteFiles
	 *            the remote files
	 * @throws IOException
	 * @author shaham
	 *         <p>
	 *         get Files Using SCP Client
	 */
	public void getFilesUsingSCP(String localTargetDirectory, String... remoteFiles){
		secureCopy.getFiles(localTargetDirectory, remoteFiles);
	}

	/**
	 * Gets the username.
	 *
	 * @return the username
	 */
	public String getUsername() {
		synchronized (credentialskLock) {
			return username;
		}
	}

	/**
	 * Sets the username.
	 *
	 * @param username
	 *            the new username
	 */
	public void setUsername(String username) {
		synchronized (credentialskLock) {
			this.username = username;
		}

	}

	/**
	 * Gets the password.
	 *
	 * @return the password
	 */
	public String getPassword() {
		synchronized (credentialskLock) {
			return password;
		}
	}

	/**
	 * Sets the password.
	 *
	 * @param password
	 *            the new password
	 */
	public void setPassword(String password) {
		synchronized (credentialskLock) {
			this.password = password;
		}
	}
	
	public void setSerialUsername(String serialUserName) {
		this.serialUserName = serialUserName;		
	}
	
	public String getSerialUsername() {
		return this.serialUserName;		
	}
	
	public void setSerialPassword(String serialPassword) {
		this.serialPassword = serialPassword;
	}
	
	public String getSerialPassword() {
		return this.serialPassword;
	}
	
	public void setSerialSwitchUserCommand(String switchUserCommand) {
		this.serialSwitchUserCommand = switchUserCommand;		
	}

	/**
	 * Sends the "pkill" command to the component's shell.
	 *
	 * @param process
	 *            - The name of the process we want to kill.
	 */
	public void kill(String process) {
		report.report(String.format("Killing process " + process));
		shell("pkill " + process);
	}

	/**
	 * Gets the log file path.
	 *
	 * @return the log file path
	 */
	public String getLogFilePath() {
		return logFilePath;
	}

	/**
	 * Adds a listener to logger.
	 *
	 * @param listener
	 *            the listener
	 */
	public void addListenerToLogger(LogListener listener) {
		logger.addLogListener(listener);
	}

	/**
	 * Removes a listener from logger.
	 *
	 * @param listener
	 *            the listener
	 */
	public void removeListenerFromLogger(LogListener listener) {
		logger.removeLogListener(listener);
	}

	/**
	 * Sets the log file path.
	 *
	 * @param logFilePath
	 *            the new log file path
	 */
	public void setLogFilePath(String logFilePath) {
		this.logFilePath = logFilePath;
	}

	/**
	 * Navigate to prompt and executes the supplied command.
	 *
	 * @param prompt
	 *            - The component prompt (Shell, LteCli, Tnet, etc..)
	 * @param command
	 *            the command
	 * @return the command value
	 */
	public String sendCommands(String prompt, String command, String response) {
		return sendCommandsOnSession(null, prompt, command, response);
	}

	/**
	 * Send commands on session.
	 *
	 * @param sessionName
	 *            the session name
	 * @param prompt
	 *            the prompt
	 * @param command
	 *            the command
	 * @return the string
	 * @throws IOException
	 */
	public String sendCommandsOnSession(String sessionName, String prompt, String command, String response){
		if (sessionName != null)
			return sessionManager.sendCommands(sessionName, prompt, command, response);
		else
			return sessionManager.sendCommandDefaultSession(prompt, command, response);
	}

	/**
	 * Open session.
	 *
	 * @return the string
	 */
	public String openSession(String sessionName) {
		String ans =sessionManager.openSession(sessionName);
		if (ans == null) {
			GeneralUtils.printToConsole("Failed opening session " + sessionName + ", returning default session instead.");
			ans = sessionManager.getDefaultSession().getName();
		}
		return ans;
	}

	/**
	 * Close session.
	 *
	 * @param sessionName
	 *            the session name
	 * @return true, if successful
	 */
	public boolean closeSession(String sessionName) {
		return sessionManager.closeSession(sessionName);
	}

	/**
	 * Send a shell command.
	 *
	 * @param command
	 *            the command
	 * @return the string
	 */
	public String shell(String command) {
		return sendCommands(SHELL_PROMPT, command, "");
	}

	public boolean isSessionConnected(String sessionName) {
		return sessionManager.isSessionConnected(sessionName);
	}
	
	public boolean isFileExists(String filePath) {
		String response = shell(String.format("ls %s", filePath));
		boolean isExist = !response.contains("No such file or directory");
		if (isExist)
			GeneralUtils.printToConsole(String.format("The file \"%s\" exists.", filePath));
		else
			GeneralUtils.printToConsole(String.format("The file \"%s\" exists.", filePath));
		return isExist;
	}

	public boolean deleteFile(String filePath) {
		GeneralUtils.printToConsole(String.format("Deleting The file \"%s\".", filePath));
		shell(String.format("rm -rf %s", filePath));
		boolean isExist = isFileExists(filePath);
		return !isExist;
	}

	/**
	 * Wait for message in shell until the timeout is reached. Also the method
	 * will look for fail messages.
	 *
	 * @param output
	 *            the output
	 * @param timeout
	 *            the timeout
	 * @param message
	 *            the message
	 * @param failMessages
	 *            the fail messages (put null if there are no such messages)
	 * @return true, if found the message within the timeout without any fail
	 *         messages
	 */
	public boolean waitForMessageInShell(String output, int timeout, String message, String... failMessages) {
		final int SEC_IN_MSEC = 1000;
		if (output != null) {
			GeneralUtils.printToConsole(output);
			if (output.contains(message)) {
				GeneralUtils.printToConsole("\"" + message + "\" been found in Shell ");
				GeneralUtils.printToConsole(" been found in Shell ");
				return true;
			}
			// if message not found look for fails(if exist)
			if (failMessages != null) {
				for (String failMessage : failMessages) {
					if (output.contains(failMessage)) {
						GeneralUtils.printToConsole("Automation detected \"" + failMessage + "\" in shell");
						return false;
					}
				}
			}
		}
		// no findings & no fails
		GeneralUtils.printToConsole("Automation did not find any fail messages in Shell");
		if (timeout > 0) {
			GeneralUtils.printToConsole("trying to pull more data from shell in order to detect \"" + message
					+ "\" , time left:#" + timeout / SEC_IN_MSEC + " [Sec]");
			try {
				Thread.sleep(CHECK_STATE_REST_TIME);
			} catch (InterruptedException e) {
			}
			// pull data
			long timeBeforeShell = System.currentTimeMillis();
			output = shell(" ");
			long delta = System.currentTimeMillis() - timeBeforeShell;
			GeneralUtils.printToConsole("The total time to grab data from shell " + (delta / SEC_IN_MSEC) + "[Sec]");
			timeout -= (CHECK_STATE_REST_TIME + delta);
			// check again with the new output and new timeout time
			return waitForMessageInShell(output, timeout, message, failMessages);
		}

		GeneralUtils.printToConsole("The Automation failed to detect the \"" + message + "\" within the current timeout");
		return false;
	}

	/**
	 * Reboot.
	 */
	public boolean reboot() {
		return reboot(RebootType.WARM_REBOOT);
	}
	public boolean reboot(GeneralUtils.RebootType rebootType) {
		String resetCommandOid = MibReader.getInstance().resolveByName("asLteStkStackCfgResetRequest");
		String toBePrimOid = MibReader.getInstance().resolveByName("asMaxBsCmSwCfgVersionToBecomePrimary") + "."
				+ SWTypeInstnace;
		String PrimOid = MibReader.getInstance().resolveByName("asMaxBsCmSwPrimaryVersion") + "." + SWTypeInstnace;
		boolean output = false;
		long timeout = 1000 * 60 * 5;
		setExpectBooting(true);
		if (snmp.get(toBePrimOid).equals("noSuchInstance")) {
			String prim = snmp.get(PrimOid);
			snmp.snmpSet(toBePrimOid, prim);
		}		
		output = snmp.snmpSet(resetCommandOid, rebootType.value);
		
		if (!output) {
			report.report("Fail to reboot via SNMP after 3 retries.", Reporter.WARNING);
			report.report("Trying to reboot eNodeB via Serial.");
			String response = sessionManager.getSerialSession().sendCommands(SHELL_PROMPT, "reboot;whoami;echo \"automation\" \"reboot\"", "automation reboot");
			if(	(response.contains("automation reboot"))){
				report.report("EnodeB " + this.getName() + " has recieved reboot command via serial.");
				return true;
			}else{
				report.report("EnodeB " + this.getName() + " has not recieved reboot command via serial.", Reporter.WARNING);
			}
		}
		boolean rebooted = waitForReboot(timeout);
		setExpectBooting(false);
		
		if (rebooted){			
			report.report("EnodeB " + this.getName() + " has been rebooted.");
			return true;
		}else{
			report.report("The Enodeb " + this.getName() + " failed to reboot!", Reporter.WARNING);
			return false;
		}
	}

	public boolean waitForReboot(long timeout) {
		report.report("Wait for enodeB " + this.getName() + " to reboot");	
		boolean rebooted = false;
		if (serialCom == null) {
			rebooted = waitForUnreachable(timeout);
		}
		else{
			rebooted = waitForExpectBootingValue(timeout, false);
		}
		return rebooted;
	}

	private boolean waitForUnreachable(long timeout) {
		long startTime = System.currentTimeMillis();
		GeneralUtils.printToConsole("Waiting for EnodeB " + this.getName() + " to be unreachable to detect a reboot.");
		while ((System.currentTimeMillis() - startTime) < timeout) {
			if (!isReachable()) {
				return true;
			}
			GeneralUtils.printToConsole("The EnodeB name " + this.getName() + " is still reachable");
			GeneralUtils.unSafeSleep(2000);
		}
		return false;
	}

	public boolean waitForExpectBootingValue(long timeout, boolean status) {
		long startTime = System.currentTimeMillis();
		GeneralUtils.printToConsole("Waiting for EnodeB " + this.getName() + " to detect reboot log line.");
		while ((System.currentTimeMillis() - startTime) < timeout) {
			if (isExpectBooting() == status) {
				return true;
			}
			GeneralUtils.printToConsole("The EnodeB name " + this.getName() + " has not detected reboot log line yet.");
			GeneralUtils.unSafeSleep(2000);
		}
		return false;
	}
	
	/**
	 * Gets the logger for the component.
	 *
	 * @return logger instance.
	 */
	@IgnoreMethod
	public Logger getLogger() {
		return this.logger;
	}

	@Override
	@IgnoreMethod
	public void getLogLine(LoggerEvent e) {
		String line = e.getLine();

		if (deviceUnderTest) {
			if (!isExpectBooting()  && checkCoreStrings(line)) {
				parseCoreFilesPath(line);
				report.report(getName() + " - Corecare detected!", Reporter.FAIL);
			}
			else if( !isExpectBooting()  && line.contains("PHY ASSERT DETECTED")){
				report.report(getName() + " - Phy assert detected!", Reporter.FAIL);
			}
			else if(checkRebootStrings(line)){
				if (isExpectBooting()) {
					setExpectBooting(false);
					report.report(getName() + " - reboot detected");
				}else{
					unexpectedReboot++;
					report.report(getName() + " - unexpected reboot detected!" , Reporter.FAIL);
				}
				if(SkipCMP && (this.waitForSrialPromptAndEchoToSkipCMP.isAlive() == false)){
					this.waitForSrialPromptAndEchoToSkipCMP.start();
				}
			}
		}
	}

	private class WaitForSrialPromptAndEchoToSkipCMP extends Thread{
		private final long timeout;
		
		public WaitForSrialPromptAndEchoToSkipCMP(long timeout) {
			this.timeout = timeout;
		}
		
		public void run(){
			long startTime = System.currentTimeMillis();
			while((System.currentTimeMillis() - startTime) < this.timeout){
				if(sessionManager.getSerialSession().loginSerial()){
					echoToSkipCmpv2();
					break;
				}
				GeneralUtils.unSafeSleep(5000);
			}
			return;
		}
	}
	
	private boolean checkRebootStrings(String line) {
		for (String item : rebootStrings) {
			if (line.contains(item)) {
				return true;
			}
		}
		return false;
	}

	private boolean checkCoreStrings(String line) {
		for (String item : coreStrings) {
			if (line.contains(item)) {
				return true;
			}
		}
		return false;
	}

	public synchronized void echoToSkipCmpv2(){		
		sendCommandsOnSession(sessionManager.getSerialSession().getName(), EnodeBComponent.SHELL_PROMPT, "echo 'SKIP_CMPV2=1' > /bs/data/debug_security.cfg", "#");
		
		String response = "Response from enodeb to \"cat /bs/data/debug_security.cfg\" command:\n";
		response += sendCommandsOnSession(sessionManager.getSerialSession().getName(), EnodeBComponent.SHELL_PROMPT, "cat /bs/data/debug_security.cfg", "#");
		GeneralUtils.printToConsole(response);
		
		sendCommandsOnSession(sessionManager.getSerialSession().getName(), EnodeBComponent.SHELL_PROMPT, "echo 'SKIP_CMPV2=1' > /bsdata/debug_security.cfg", "#");
		
		response = "Response from enodeb to \"cat /bsdata/debug_security.cfg\" command:\n";
		response += sendCommandsOnSession(sessionManager.getSerialSession().getName(), EnodeBComponent.SHELL_PROMPT, "cat /bsdata/debug_security.cfg", "#");
		GeneralUtils.printToConsole(response);
	}
	
	private void parseCoreFilesPath(String coreFileName) {

		GeneralUtils.printToConsole("Name of core file from logLine: "+coreFileName);
		String RunningVer="";
		try{
			RunningVer = coreFileName.substring(coreFileName.indexOf("ver_"));			
		}catch(Exception e){
			e.printStackTrace();
			report.report("Failed to parse core file path in string: "+coreFileName);
			return;
		}

		String pattern = "ver_(\\d+.\\d+.\\d+.\\d+)+(.\\d+)";
		try {
			// Create a Pattern object
			Pattern r = Pattern.compile(pattern);

			// Now create matcher object.
			Matcher m = r.matcher(RunningVer);
			m.find();

			RunningVer = m.group();
			if (RunningVer.length() < 4) {
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		RunningVer = RunningVer.substring(4);

		String corePath = coreFtpSetver;
		Date date = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1;
		int day = cal.get(Calendar.DAY_OF_MONTH);

		if (day < 10) {
			corePath += "0";
		}
		corePath += String.valueOf(day);
		corePath += "_";
		if (month < 10) {
			corePath += "0";
		}
		corePath += String.valueOf(month);
		corePath += "_";
		year = year % 100;
		corePath += String.valueOf(year);
		corePath += "\\";
		corePath += RunningVer;
		corePath += "\\";
		String coreFolderName = coreFileName.substring(coreFileName.indexOf("core."), coreFileName.indexOf(RunningVer));
		corePath += coreFolderName;
		corePath += RunningVer;

		corePathSet.add(corePath);
	}

	/**
	 * Checks if is reachable.
	 *
	 * @return true, if is reachable
	 */
	public synchronized boolean isReachable() {
		return reachable;
	}

	public synchronized void setReachable(boolean reachable) {
		this.reachable = reachable;
	}

	/**
	 * Sets the session log level.
	 *
	 * @param sessionName
	 *            the session name
	 * @param level
	 *            the level
	 */
	public void setSessionLogLevel(String sessionName, int level) {
		// empty method. Should be overridden by relevant components;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getName();
	}
	
	public boolean waitForSnmpAvailable(long timeOut) {
		long startTime = System.currentTimeMillis(); // fetch starting time
		
		while ((System.currentTimeMillis() - startTime) < timeOut) {
			synchronized(snmpLock){
				if(snmp.isAvailable()){
					return true;
				}
			}
			GeneralUtils.unSafeSleep(2 * 1000);
		}
		report.report("Reached Timeout - SNMP is NOT Available.", Reporter.WARNING);
		return false;
	}

	public void initSNMP() {
		if (ipAddress != null) {
			synchronized(snmpLock){
				snmp = new SNMP(readCommunity, writeCommunity, ipAddress, "V2");
				snmp.initiatePDU();
			}
		}
	}

	public int getUnexpectedReboot() {
		return unexpectedReboot;
	}

	public void setUnexpectedReboot(int unexpectedReboot) {
		this.unexpectedReboot = unexpectedReboot;
	}

	public String getReadCommunity() {
		return readCommunity;
	}

	public void setReadCommunity(String readCommunity) {
		this.readCommunity = readCommunity;
	}

	public String getWriteCommunity() {
		return writeCommunity;
	}

	public void setWriteCommunity(String writeCommunity) {
		this.writeCommunity = writeCommunity;
	}
	public void setDebugFlags() {
		if(debugFlags != null){
			for(String debugFlag : debugFlags){
				for(int i=1; i<=3; i++){
					String response = sendCommands(LTE_CLI_PROMPT, debugFlag, ""); 
					if(response.contains("entries updated")){
						break;
					}
				}
			}
		}
	}

	public String getParallelCommandsPrompt() {
		return LTE_CLI_PROMPT;
	}
	public String getScpUsername() {
		return scpUsername;
	}

	public void setScpUsername(String scpUsername) {
		this.scpUsername = scpUsername;
	}

	public String getScpPassword() {
		return scpPassword;
	}

	public void setScpPassword(String scpPassword) {
		this.scpPassword = scpPassword;
	}

	public int getScpPort() {
		return scpPort;
	}

	public void setScpPort(int scpPort) {
		this.scpPort = scpPort;
	}
}
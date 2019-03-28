package EnodeB.Components.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jsystem.framework.report.Reporter;

import org.junit.Assert;

import EnodeB.Components.EnodeBComponent;
import EnodeB.Components.Session.Session;
import EnodeB.Components.Session.SessionManager;
import Utils.GeneralUtils;
import Utils.Properties.TestspanConfigurationsManager;

public class Logger implements Runnable {
	public final Object lock = new Object();
	private static final String LOG_INTERVAL_PROPERTY_NAME = "logger.logInterval"; // value in milliseconds.

	//eventId for the logger events
	private static int eventID = 0;
	private boolean countErrorBool;

	private String logFilePath;

	private EnodeBComponent parent;
	private boolean isLogging;
	private HashMap<String, Integer> scenarioLoggerCounters;
	private HashMap<String, Integer> testLoggerCounters;

	private String name;
	private Reporter reporter;

	private long logInterval;

	private ArrayList<Session> loggedSessions;

	private LogWriter logWriterEnb;
	private LogWriter logWriterAuto;

	private ArrayList<LogListener> listeners;
	private Pattern pattern = null;

	public Logger(String logFilePath, EnodeBComponent parent, SessionManager sessionManager, Reporter reporter) {
		this.logFilePath = logFilePath;
		this.parent = parent;
		this.reporter = reporter;
		this.scenarioLoggerCounters = new HashMap<String, Integer>();
		this.testLoggerCounters = new HashMap<String, Integer>();
		this.name = parent.getName() + ".Logger";

		this.listeners = new ArrayList<LogListener>();
		this.logWriterEnb = new LogWriter(this);
		this.logWriterAuto = new LogWriter(this);
		this.countErrorBool = false;

		try{
			pattern = Pattern.compile("\\s+(\\D*\\(\\)):(\\d+) <(.*):(.*) (ERROR)> (.*)");
		}catch(Exception e){
			e.printStackTrace();
			GeneralUtils.printToConsole("Failed to compile ERROR pattern");
		}

		try {
			logInterval = Long.parseLong(TestspanConfigurationsManager.getInstance().getConfig(LOG_INTERVAL_PROPERTY_NAME));
		} catch (Exception e) {
			System.err.println("Error getting logger's log interval.");
			e.printStackTrace();
			isLogging = false;
			Assert.fail("Logger init failed: Log interval is not configured.");
		}

		if (getLoggedSessions() == null ) {
			setLoggedSessions(new ArrayList<Session>());
		}
		synchronized (lock) {
			addLoggedSessions(sessionManager);
		}
		initLoggedSessions();
	}

	public synchronized ArrayList<Session> getLoggedSessions() {
		return loggedSessions;
	}

	public synchronized void setLoggedSessions(ArrayList<Session> loggedSessions) {
		this.loggedSessions = loggedSessions;
	}

	public synchronized void addToLoggedSessions(Session session) {
		loggedSessions.add(session);
	}

	public synchronized void removeFromLoggedSessions(Session session) {
		loggedSessions.remove(session);
	}


	/**
	 * Init Logged Sessions if not Initialized
	 */
	public void initLoggedSessions() {
		for (Session session : this.getLoggedSessions()) {
			session.setLoggedSession(true);
			session.setEnableCliBuffer(false);
			GeneralUtils.printToConsole("update log level from logger");
			session.updateLogLevel();
		}
	}

	/**
	 * Add the Logged Sessions from sessionManager to loggedSessions Array - in order to stream from them in logger thread
	 * Adding just in case they are not in the array.
	 *
	 * @param sessionManager - sessionManager
	 */
	public void addLoggedSessions(SessionManager sessionManager) {
		// Console session
		if (sessionManager.getSerialSession() != null && (!getLoggedSessions().contains(sessionManager.getSerialSession()))) {
			addToLoggedSessions(sessionManager.getSerialSession());
		}
		// SSH sessions
		if (parent.getIpAddress() != null) {
			if (sessionManager.getSSHlogSession() != null && (!getLoggedSessions().contains(sessionManager.getSSHlogSession()))) {
				addToLoggedSessions(sessionManager.getSSHlogSession());
			}
			if (sessionManager.getSSHCommandSession() != null && (!getLoggedSessions().contains(sessionManager.getSSHCommandSession()))) {
				addToLoggedSessions(sessionManager.getSSHCommandSession());
			}
		}
	}

	/**
	 * Adds a log listener the the logger
	 *
	 * @param listener the listener
	 */
	public void addLogListener(LogListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	/**
	 * Invokes the getLogLine action on all listeners.
	 *
	 * @param line the log line.
	 */
	private void invokeListeners(String line) {
		synchronized (listeners)  {
			for (LogListener listener : listeners) {
				LoggerEvent event = new LoggerEvent(this, line, eventID++ , "");
				if (eventID == Integer.MAX_VALUE)
					eventID = 0;

				listener.getLogLine(event);
			}
		}
	}

	/**
	 * Clears the counted log lines counters.
	 */
	public void clearTestCounters() {
		testLoggerCounters.clear();
	}

	/**
	 * Turns a log into an array of log lines.
	 *
	 * @param log a string containing the log
	 * @return an array of log lines.
	 */
	public String[] processLines(String log) {
		if (!log.trim().isEmpty()) {
			String[] lines = log.split("\n");
			Calendar receivedDate = Calendar.getInstance();
			String timestamp =  String.format("%02d/%02d/%04d %02d:%02d:%02d:%03d    ::    ", receivedDate.get(Calendar.DAY_OF_MONTH), receivedDate.get(Calendar.MONTH) + 1 /*because January=0*/,
					receivedDate.get(Calendar.YEAR), receivedDate.get(Calendar.HOUR_OF_DAY),
		            receivedDate.get(Calendar.MINUTE),receivedDate.get(Calendar.SECOND),
					receivedDate.get(Calendar.MILLISECOND));
			for (int i = 0; i < lines.length; i++) {
				lines[i] = timestamp + lines[i];
			}
			return lines;
		}

		return new String[0];
	}


	/**
	 * Starts the logger
	 */
	public void start() {
		System.out.printf("[%s]: Starting logger.\n", getName());
		if (logInterval == 0)
			Assert.fail("logInteval is not configured. Failed to start the logger.");
		if (!isLogging)
			new Thread(this, parent + " logger").start();
	}

	/**
	 * Stops the logger
	 */
	public void stop() {
		System.out.printf("[%s]: Stopping logger.\n", getName());
		isLogging = false;
		try {
			// wait twice the log interval to make sure the thread stops.
			Thread.sleep(2 * logInterval);
		} catch (Exception e) {}
	}

	@Override
	public void run() {
		isLogging = true;
		System.out.printf("[%s]: logger thread started. \n", name);
		startLog(logFilePath);
		while (isLogging) {
			synchronized (lock) {
				streamLogsLoop();
			}
		}
		System.out.printf("[%s]: Logger was stopped.\n", getName());
		isLogging = false;
		logWriterEnb.closeAll();
		logWriterAuto.closeAll();
	}

	/**
	 * stream Logs Loop Processes log lines while isLogging param is true.
	 * This method protected with lock param, so open sessions can be added to loggedSessions Array while this thread runs.
	 */
	public void streamLogsLoop() {
		String[] buffers = new String[getLoggedSessions().size()];
		for (int sessionIndx = 0; sessionIndx < getLoggedSessions().size(); sessionIndx++) {
			Session session = getLoggedSessions().get(sessionIndx);
			String buffer = session.getLoggerBuffer();
			buffers[sessionIndx] = buffer;
			String[] lines = processLines(buffer);
			for (String logLine : lines) {
				if (logLine == null || logLine.length() == 0) {
					continue;
				}
				writeLogLine(session, logLine);
				invokeListeners(logLine);
			}
			analyzeLogLineIfNeeded(session, buffer);
		}
		GeneralUtils.unSafeSleep(logInterval);
		stopIfLogWritersEmpty();
		stopIfLoggedSessionArrayEmpty();
	}

	/**
	 * write Log Line to logWriterAuto / logWriterEnb
	 *
	 * @param session - session
	 * @param logLine - logLine
	 */
	private void writeLogLine(Session session, String logLine) {
		if (session.getName().contains(SessionManager.SSH_COMMANDS_SESSION_NAME))
			logWriterAuto.writeLog(logLine, "", session.getName());
		else
			logWriterEnb.writeLog(logLine, "", session.getName());
	}

	/**
	 * analyze Line If it is SSH log Session and countErrorBool==true
	 *
	 * @param session - session
	 * @param buffer  - buffer
	 */
	private void analyzeLogLineIfNeeded(Session session, String buffer) {
		if (countErrorBool && session.getName().contains(SessionManager.SSH_LOG_SESSION_NAME))
			analyzeLine(buffer);
	}

	/**
	 * Stop logger if there are no logged sessions
	 */
	private void stopIfLoggedSessionArrayEmpty() {
		if (getLoggedSessions().size() == 0) {
			System.err.printf("[%s]: There are no logged session connected to the logger.\n", name);
			stop();
		}
	}

	/**
	 * Stop logger if it's not needed.
	 */
	private void stopIfLogWritersEmpty() {
		if (logWriterEnb.size() == 0 && logWriterAuto.size() == 0 && listeners.size() == 0) {
			System.out.printf("[%s]: No log files are logged and no listeners registered, this is maybe an error. stopping logger.", name);
			stop();
		}
	}

	private void analyzeLine(String buffer) {
		String[] splittedOutput = buffer.trim().split("\n");
		if (splittedOutput != null){
			Matcher match = null;
			String key = "";
			if(pattern == null){
				try{
					pattern = Pattern.compile("\\s+(\\D*\\(\\)):(\\d+) <(.*):(.*) (ERROR)> (.*)");
				}catch(Exception e){
					e.printStackTrace();
					GeneralUtils.printToConsole("Failed to compile ERROR pattern");
				}
			}
			for(String line : splittedOutput){
				String severity = "@";
				line = line.trim();
				match = null;
				if(pattern!=null){
					match = pattern.matcher(line);
				}
				key = "";
				if(match!=null){
					if(match.find()){
						LoggerError tempLogger = new LoggerError(match.group(1),match.group(2),match.group(3),match.group(4),match.group(5),match.group(6));
						key = tempLogger.getKey();
						severity+="ERROR";
					}
				}

				if(!key.equals("")){
					key+=severity;
					if (scenarioLoggerCounters.get(key) == null) {
						scenarioLoggerCounters.put(key, 1); // put the initial value of "1" of new counted log lines.
					}
					else {
						int count = scenarioLoggerCounters.get(key) + 1; // increase log line count.
						scenarioLoggerCounters.put(key, count);
					}

					if (testLoggerCounters.get(key) == null) {
						testLoggerCounters.put(key, 1); // put the initial value of "1" of new counted log lines.
					}
					else {
						int count = testLoggerCounters.get(key) + 1; // increase log line count.
						testLoggerCounters.put(key, count);
					}
				}
			}
		}
	}

	/**
	 * Add new files to be logged.
	 * This method will add a file for the <b>console</b>
	 *
	 * @param logName
	 */
	public void startLog(String logName) {
		System.out.printf("[%s]: Creating log files. \n", name);
		if (getLoggedSessions().size() < 1)
			System.err.printf("[%s]: There are no log needed for this. Not creating any log files. \n", name);
		else {
			for (Session session : getLoggedSessions()) {
				if (session.getName().contains(SessionManager.SSH_COMMANDS_SESSION_NAME))
					logWriterAuto.addLog(logName, session.getName());
				else
					logWriterEnb.addLog(logName, session.getName());
			}
		}
	}

	/**
	 * Add new files to be logged- logWriterEnb
	 * This method will add a file for the <b>console</b>
	 *
	 * @param logName - logName
	 */
	public void startEnodeBLog(String logName, String prefix) {
		System.out.printf("[%s]: Creating log files. \n", name);
		if (getLoggedSessions().size() < 1)
			System.err.printf("[%s]: There are no log needed for this. Not creating any log files. \n", name);
		else {
			logWriterEnb.addLog(logName, prefix);
		}
	}

	/**
	 * Closes the log files that contains the logName and copies them to the test folder via the reporter.
	 *
	 * @param logName - logName
	 */
	public void closeEnodeBLog(String logName) {
		logWriterEnb.closeLog(logName);
	}

	/**
	 * Closes the log files that contains the logName and copies them to the test folder via the reporter.
	 *
	 * @param logName - logName
	 */
	public void closeEnodeBLog(String logName, String prefix) {
		logWriterEnb.closeLog(logName, prefix);
	}

	/**
	 * Closes the log files that contains the logName and copies them to the test folder via the reporter.
	 *
	 * @param logName - logName
	 */
	public void closeAutoLog(String logName) {
		logWriterAuto.closeLog(logName);
	}

	public void closeAllLogFiles() {
		logWriterEnb.closeAll();
		logWriterAuto.closeAll();
	}

	/**
	 * Get the parent component of the logger
	 *
	 * @return
	 */
	public EnodeBComponent getParent() {
		return parent;
	}


	/**
	 * Get the reporter instance the logger uses.
	 *
	 * @return
	 */
	public Reporter getReporter() {
		return reporter;
	}


	/**
	 * Get the logger's name.
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * Remove a lister from the logger
	 * @param listener
	 */
	public void removeLogListener(LogListener listener) {
		synchronized (listeners) {
			if (listeners.contains(listener))
				listeners.remove(listener);
		}
	}

	public void setCountErrorBool(boolean countErrorBool) {
		this.countErrorBool = countErrorBool;
	}

	/**
	 * Get the log interval in millis.
	 * @return
	 */
	public long getLogInterval(){
		return logInterval;
	}

	public boolean isLogging() {
		return isLogging;
	}

	public HashMap<String, Integer> getScenarioLoggerCounters() {
		return scenarioLoggerCounters;
	}

	public void setScenarioLoggerCounters(HashMap<String, Integer> scenarioLoggerCounters) {
		this.scenarioLoggerCounters = scenarioLoggerCounters;
	}

	public HashMap<String, Integer> getTestLoggerCounters() {
		return testLoggerCounters;
	}

	public void setTestLoggerCounters(HashMap<String, Integer> testLoggerCounters) {
		this.testLoggerCounters = testLoggerCounters;
	}

	public class LoggerError{
		private String function;
		private String lineNumber;
		private String process;
		private String client;
		private String logLevel;
		private String errorString;
		private int counter;

		public LoggerError(String function, String lineNumber, String process, String client, String logLevel,
				String errorString) {
			this.function = function;
			this.lineNumber = lineNumber;
			this.process = process;
			this.client = client;
			this.logLevel = logLevel;
			this.errorString = errorString;
			this.counter = 1;
		}

		public boolean equals(LoggerError other) {
			return process.equals(other.getProcess()) && client.equals(other.getClient());
		}

		public String getKey(){
			return process+":"+client;
		}

		public String getClient() {
			return client;
		}

		public String getProcess() {
			return process;
		}

		public String getFunction() {
			return function;
		}

		public String getLineNumber() {
			return lineNumber;
		}

		public String getLogLevel() {
			return logLevel;
		}

		public String getErrorString() {
			return errorString;
		}

		public int getCounter() {
			return counter;
		}
	}
}

package Action.EnodebAction;

import EnodeB.Components.Log.Logger;
import EnodeB.Components.Session.SessionManager;
import EnodeB.EnodeB;
import Utils.GeneralUtils;
import Utils.LogSessionParamsSet;
import Utils.SysObjUtils;
import jsystem.framework.ParameterProperties;
import jsystem.framework.TestProperties;
import org.junit.Test;
import jsystem.framework.scenario.Parameter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class LogsAction extends EnodebAction {

	/**
	 * String Constants
	 */
	private String EVERY_MODULE_STRING = "*";
	private static final String LOG_ACTION = "_LogAction";
	private static final String ACTION_NAME_STRING = "startEnodeBLogs";
	private static final String MODULES_STRING = "Modules";
	private static final String PROCESS_STRING = "Process";
	private static final String CLIENT_STRING = "Client";

	/**
	 * Static List that organizes all the open session in a scenario
	 */
	private static ArrayList<LogSessionParamsSet> logSessionParamsList;

	/**
	 * User Inputs
	 */
	private ArrayList<EnodeB> duts;
	private Session inputSession;
	private LogLevel inputLogLevel = LogLevel.SIX;
	private Modules inputModules = Modules.ALL;
	private String inputProcess = EVERY_MODULE_STRING;
	private String inputClient = EVERY_MODULE_STRING;

	/**
	 * This init method organises all the enB that requested foe streaming log.
	 * every value in this list represents a set of params for creating a log session
	 */
	public void createLogSessionParamsList() {
		if (logSessionParamsList == null) {
			logSessionParamsList = new ArrayList<>();
		}
		for (EnodeB eNodeB : duts) {
			LogSessionParamsSet newLogSessionParamsSet = createLogSessionParamSet(eNodeB);
			removeSessionIfAlreadyOpened(newLogSessionParamsSet);
			logSessionParamsList.add(newLogSessionParamsSet);
		}
	}

	/**
	 * Removes from the logSessionParamsList array all the session that closed
	 */
	public void updateLogSessionParamsList() {
		Iterator<LogSessionParamsSet> iter;
		iter = logSessionParamsList.iterator();
		while (iter.hasNext()) {
			LogSessionParamsSet logSessionParamsSet = iter.next();
			if (!logSessionParamsSet.isSessionOpen) {
				iter.remove();
			}
		}
	}

	/**
	 * Define log level, inputClient and inputProcess for every session according to user request.
	 *
	 * @return - LogProperties for each session
	 */
	private LogSessionParamsSet createLogSessionParamSet(EnodeB eNodeB) {
		return new LogSessionParamsSet(eNodeB, inputSession, inputModules, inputProcess, inputClient, inputLogLevel);
	}

	/**
	 * If already requested to open specific SSH\Serial session to an eNB, remove it in order to
	 * replace by the new one
	 *
	 * @param newLogSessionParamsSet - newLogSessionParamsSet
	 */
	private void removeSessionIfAlreadyOpened(LogSessionParamsSet newLogSessionParamsSet) {
		for (LogSessionParamsSet logSessionParamsSet : logSessionParamsList) {
			if ((newLogSessionParamsSet.enodeB.getName().equalsIgnoreCase(logSessionParamsSet.enodeB.getName())) &&
					(newLogSessionParamsSet.session.value.equalsIgnoreCase(inputSession.value))) {
				logSessionParamsList.remove(logSessionParamsSet);
			}
		}
	}

	/**
	 * Enum represented by String to Process dropdown to log:
	 * All or ParticularModel(Specific Process and Client)
	 */
	public enum Modules {
		ALL("All"),
		PARTICULAR_MODEL("Particular Model");

		final String value;

		Modules(String value) {
			this.value = value;
		}
	}

	/**
	 * Enum represented by String to session dropdown- SSH or Serial
	 */
	public enum Session {
		SSH("SSH"),
		SERIAL("Serial"),
		BOTH("SSH+Serial");

		final String value;

		Session(String value) {
			this.value = value;
		}
	}

	/**
	 * Enum represented by int to LogLevel dropdown - 1 to 6
	 */
	public enum LogLevel {
		ONE(1),
		TWO(2),
		THREE(3),
		FOUR(4),
		FIVE(5),
		SIX(6);

		final int value;

		LogLevel(int value) {
			this.value = value;
		}
	}

	@ParameterProperties(description = "Name of all EnB comma separated i.e. enb1,enb2")
	public void setDUTs(String dut) {
		this.duts = (ArrayList<EnodeB>) SysObjUtils.getInstnce().initSystemObject(EnodeB.class, false, dut.split(","));
	}

	@ParameterProperties(description = "Log session to start \\ stop ")
	public void setSession(Session inputSession) {
		this.inputSession = inputSession;
	}

	@ParameterProperties(description = "LogLevel to start")
	public void setLogLevel(LogLevel inputLogLevel) {
		this.inputLogLevel = inputLogLevel;
	}

	@ParameterProperties(description = "Modules For Start Log")
	public void setModules(Modules inputModules) {
		this.inputModules = inputModules;
	}

	@ParameterProperties(description = "Choose a specific Process")
	public void setProcess(String inputProcess) {
		this.inputProcess = inputProcess;
	}

	@ParameterProperties(description = "Choose a specific Client")
	public void setClient(String inputClient) {
		this.inputClient = inputClient;
	}

	@Test
	@TestProperties(
			name = "Start EnodeB Logs",
			returnParam = {"IsTestWasSuccessful"},
			paramsInclude = {"DUTs", "Session", "LogLevel", "Modules", "Process", "Client"})
	public void startEnodeBLogs() {
		createLogSessionParamsList();
		for (LogSessionParamsSet logSessionParamsSet : logSessionParamsList) {
			if (logSessionParamsSet.isSessionOpen) continue;
			printToReportLogDetails(logSessionParamsSet, "Open");
			openLogSession(logSessionParamsSet);
			setLogLevelAndProcessByName(logSessionParamsSet);
			addOpenSessionToLogger(logSessionParamsSet);
			logSessionParamsSet.isSessionOpen = true;
		}
	}

	@Test
	@TestProperties(
			name = "Stop EnodeB Logs",
			returnParam = {"IsTestWasSuccessful"},
			paramsInclude = {"DUTs", "Session"})
	public void stopEnodeBLogs() {
		for (EnodeB eNodeB : duts) {
			LogSessionParamsSet sessionParamSetToClose = getSessionParamSetToClose(eNodeB);
			if (sessionParamSetToClose == null) continue;
			if (!sessionParamSetToClose.isSessionOpen) continue;
			printToReportLogDetails(sessionParamSetToClose, "Close");
			removeFromLoggedSession(sessionParamSetToClose);
			closeAndGenerateEnBLogFiles(sessionParamSetToClose);
			sessionParamSetToClose.isSessionOpen = false;
		}
		updateLogSessionParamsList();
	}

	/**
	 * Find in logSessionParamsList the requested session to close else return null
	 *
	 * @param eNodeB - eNodeB
	 * @return - logSessionParamsSetToClose or null if not found
	 */
	private LogSessionParamsSet getSessionParamSetToClose(EnodeB eNodeB) {
		for (LogSessionParamsSet logSessionParamsSet : logSessionParamsList) {
			if ((logSessionParamsSet.enodeB.getName().equalsIgnoreCase(eNodeB.getName())) &&
					(logSessionParamsSet.session.value.equalsIgnoreCase(inputSession.value))) {
				return logSessionParamsSet;
			}
		}
		return null;
	}

//	/**
//	 * Find in logSessionParamsList the requested session to close
//	 *
//	 * @param eNodeB - eNodeB
//	 * @return - logSessionParamsSetToClose
//	 */
//	private LogSessionParamsSet getSessionParamSetToClose(EnodeB eNodeB) {
//	}

	/**
	 * Print To Report Log Details when opening log session
	 *
	 * @param logSessionParamsSet - logSessionParamsSet
	 * @param action              - Open or Close
	 */
	private void printToReportLogDetails(LogSessionParamsSet logSessionParamsSet, String action) {
		GeneralUtils.startLevel(action + " Session:");
		printToReportLogDetails(logSessionParamsSet.enodeB,
				logSessionParamsSet.session.value,
				logSessionParamsSet.logLevel,
				logSessionParamsSet.module,
				logSessionParamsSet.process,
				logSessionParamsSet.client);
		GeneralUtils.stopLevel();
	}

	/**
	 * Print To Report Log Details:
	 * Session, EnodeB, Log Level, Modules: All or Process, Client.
	 *
	 * @param eNodeB - eNodeB
	 */
	private void printToReportLogDetails(EnodeB eNodeB, String session, LogLevel logLevel, Modules modules, String process, String client) {
		report.report("Log Session: " + session + ". For EnodeB: " + eNodeB.getName());
		report.report("Log Level: " + String.valueOf(logLevel.value) + ". Modules: " + modules.value);
		if (Modules.PARTICULAR_MODEL.value.equals(modules.value)) {
			report.report("Process: " + process + ". Client: " + client);
		}
	}

	/**
	 * Add Open Session To LoggedSessions array, in order to stream from them in Logger Thread.
	 * If the sessions already exist in this array, it won't add them again.
	 *
	 * @param logSessionParamsSet - logSessionParamsSet
	 */
	private void addOpenSessionToLogger(LogSessionParamsSet logSessionParamsSet) {
		SessionManager sessionManager = logSessionParamsSet.enodeB.getXLP().getSessionManager();
		Logger logger = logSessionParamsSet.enodeB.getXLP().getLogger();
		synchronized (logger.lock) {
			logger.addLoggedSessions(sessionManager);
			logger.addLogListener(logSessionParamsSet.enodeB.getXLP());
			startLogs(logSessionParamsSet, logger);
			logger.setCountErrorBool(true);
		}
	}

	/**
	 * Close And Generate EnB Log Files
	 *
	 * @param logSessionParamsSet - logSessionParamsSet
	 */
	private void closeAndGenerateEnBLogFiles(LogSessionParamsSet logSessionParamsSet) {
		GeneralUtils.startLevel(String.format("eNodeB %s logs", logSessionParamsSet.enodeB.getName()));
		Logger loggers[] = logSessionParamsSet.enodeB.getLoggers();
		for (Logger logger : loggers) {
			logger.setCountErrorBool(false);
			closeLogs(logSessionParamsSet, logger);
			scenarioUtils.scenarioStatistics(logger, logSessionParamsSet.enodeB);
		}
		GeneralUtils.stopLevel();
	}

	/**
	 * Start log upon request
	 *
	 * @param logger - logger
	 */
	private void startLogs(LogSessionParamsSet logSessionParamsSet, Logger logger) {
		switch (logSessionParamsSet.session) {
			case SSH:
				logger.startEnodeBLog(logSessionParamsSet.sshSessionName, LOG_ACTION);
				break;
			case SERIAL:
				logger.startEnodeBLog(logSessionParamsSet.serialSessionName, LOG_ACTION);
				break;
			case BOTH:
				logger.startEnodeBLog(logSessionParamsSet.sshSessionName, LOG_ACTION);
				logger.startEnodeBLog(logSessionParamsSet.serialSessionName, LOG_ACTION);
		}
	}

	/**
	 * Close log upon request
	 *
	 * @param logger - logger
	 */
	private void closeLogs(LogSessionParamsSet logSessionParamsSet, Logger logger) {
		switch (logSessionParamsSet.session) {
			case SSH:
				logger.closeEnodeBLog(logSessionParamsSet.sshSessionName, LOG_ACTION);
				break;
			case SERIAL:
				logger.closeEnodeBLog(logSessionParamsSet.serialSessionName, LOG_ACTION);
				break;
			case BOTH:
				logger.closeEnodeBLog(logSessionParamsSet.sshSessionName, LOG_ACTION);
				logger.closeEnodeBLog(logSessionParamsSet.serialSessionName, LOG_ACTION);
		}
	}

	/**
	 * Open Log Session - SSH or Serial and define session names
	 *
	 * @param logSessionParamsSet - logSessionParamsSet
	 */
	private void openLogSession(LogSessionParamsSet logSessionParamsSet) {
		switch (logSessionParamsSet.session) {
			case SSH:
				logSessionParamsSet.sshSessionName = openSSHSession(logSessionParamsSet);
				setSSHParamsForReconnect(logSessionParamsSet);
				break;
			case SERIAL:
				logSessionParamsSet.serialSessionName = openSerialSession(logSessionParamsSet);
				setSerialParamsForReconnect(logSessionParamsSet);
				break;
			case BOTH:
				logSessionParamsSet.sshSessionName = openSSHSession(logSessionParamsSet);
				setSSHParamsForReconnect(logSessionParamsSet);
				logSessionParamsSet.serialSessionName = openSerialSession(logSessionParamsSet);
				setSerialParamsForReconnect(logSessionParamsSet);
		}
	}

	/**
	 * Open Serial Session if not opened, define name, set log session flag
	 *
	 * @param logSessionParamsSet - logSessionParamsSet
	 * @return - ssh session Name + LOG_ACTION suffix
	 */
	private String openSerialSession(LogSessionParamsSet logSessionParamsSet) {
		SessionManager sessionManager = logSessionParamsSet.enodeB.getXLP().getSessionManager();
		if (sessionManager.getSerialSession() == null) {
			logSessionParamsSet.enodeB.getXLP().initSerialCom();
			sessionManager.openSerialLogSession(logSessionParamsSet.logLevel.value);
			sessionManager.getSerialSession().setLoggedSession(true);
			sessionManager.getSerialSession().setEnableCliBuffer(false);
		}
		return sessionManager.getSerialSession().getName();
	}

	/**
	 * Open SSH Session if not opened, define name, set log session flag
	 *
	 * @return - serial session Name + LOG_ACTION suffix
	 */
	private String openSSHSession(LogSessionParamsSet logSessionParamsSet) {
		SessionManager sessionManager = logSessionParamsSet.enodeB.getXLP().getSessionManager();
		if (sessionManager.getSSHlogSession() == null) {
			sessionManager.openSSHLogSession(logSessionParamsSet.logLevel.value);
			sessionManager.getSSHlogSession().setLoggedSession(true);
			sessionManager.getSSHlogSession().setEnableCliBuffer(false);
		}
		return sessionManager.getSSHlogSession().getName();
	}

	/**
	 * Open Log Session - SSH or Serial and define session names
	 */
	private void setLogLevelAndProcessByName(LogSessionParamsSet logSessionParamsSet) {
		switch (logSessionParamsSet.session) {
			case SSH:
				logSessionParamsSet.enodeB.setSessionLogLevelPerProcess(logSessionParamsSet.sshSessionName,
						logSessionParamsSet.client, logSessionParamsSet.process, logSessionParamsSet.logLevel.value);
				break;
			case SERIAL:
				logSessionParamsSet.enodeB.setSessionLogLevelPerProcess(logSessionParamsSet.serialSessionName,
						logSessionParamsSet.client, logSessionParamsSet.process, logSessionParamsSet.logLevel.value);
				break;
			case BOTH:
				logSessionParamsSet.enodeB.setSessionLogLevelPerProcess(logSessionParamsSet.sshSessionName,
						logSessionParamsSet.client, logSessionParamsSet.process, logSessionParamsSet.logLevel.value);
				logSessionParamsSet.enodeB.setSessionLogLevelPerProcess(logSessionParamsSet.serialSessionName,
						logSessionParamsSet.client, logSessionParamsSet.process, logSessionParamsSet.logLevel.value);
		}
	}

	/**
	 * Set inputProcess, inputClient and log level for Reconnect thread, so in case of disconnecting an override won't occur.
	 *
	 * @param logSessionParamsSet - logSessionParamsSet
	 * @see EnodeB.Components.Session# updateLogLevel()
	 */
	private void setSSHParamsForReconnect(LogSessionParamsSet logSessionParamsSet) {
		logSessionParamsSet.enodeB.getSSHlogSession().setProcess(logSessionParamsSet.process);
		logSessionParamsSet.enodeB.getSSHlogSession().setClient(logSessionParamsSet.client);
		logSessionParamsSet.enodeB.getSSHlogSession().setLogLevel(logSessionParamsSet.logLevel.value);
	}

	/**
	 * Set inputProcess, inputClient and log level for Reconnect thread, so in case of disconnecting an override won't occur.
	 *
	 * @param logSessionParamsSet - logSessionParamsSet
	 * @see EnodeB.Components.Session# updateLogLevel()
	 */
	private void setSerialParamsForReconnect(LogSessionParamsSet logSessionParamsSet) {
		logSessionParamsSet.enodeB.getSerialSession().setProcess(logSessionParamsSet.process);
		logSessionParamsSet.enodeB.getSerialSession().setClient(logSessionParamsSet.client);
		logSessionParamsSet.enodeB.getSerialSession().setLogLevel(logSessionParamsSet.logLevel.value);
	}

	/**
	 * Open Log Session - SSH or Serial
	 * Wait for Logger thread to finish its iteration before removing from LoggedSession array.
	 */
	private void removeFromLoggedSession(LogSessionParamsSet logSessionParamsSet) {
		SessionManager sessionManager = logSessionParamsSet.enodeB.getXLP().getSessionManager();
		Logger logger = logSessionParamsSet.enodeB.getXLP().getLogger();
		synchronized (logger.lock) {
			switch (logSessionParamsSet.session) {
				case SSH:
					logger.removeFromLoggedSessions(sessionManager.getSSHlogSession());
					break;
				case SERIAL:
					logger.removeFromLoggedSessions(sessionManager.getSerialSession());
					break;
				case BOTH:
					logger.removeFromLoggedSessions(sessionManager.getSSHlogSession());
					logger.removeFromLoggedSessions(sessionManager.getSerialSession());
			}
		}
	}

	/**
	 * Handle UI to Jsystem for startEnodeBLogs Action
	 *
	 * @param map        - map
	 * @param methodName - methodName
	 */
	@Override
	public void handleUIEvent(HashMap<String, Parameter> map, String methodName) {
		if (methodName.equals(ACTION_NAME_STRING)) {
			handleUIEventGetCounterValue(map);
		}
	}

	/**
	 * Show parameters Process and Client in case the user select Modules: Particular Model
	 *
	 * @param map - map
	 */
	private void handleUIEventGetCounterValue(HashMap<String, Parameter> map) {
		map.get(PROCESS_STRING).setVisible(false);
		map.get(CLIENT_STRING).setVisible(false);
		Parameter modules = map.get(MODULES_STRING);
		setModulesMenuVisible(map, modules);
	}

	/**
	 * Set Modules Menu Visible.
	 * If user choose "ALL MODULES" option then Process & Client == *
	 *
	 * @param map     - map
	 * @param modules - modules
	 */
	private void setModulesMenuVisible(HashMap<String, Parameter> map, Parameter modules) {
		if (Modules.PARTICULAR_MODEL == Modules.valueOf(modules.getValue().toString())) {
			map.get(PROCESS_STRING).setVisible(true);
			map.get(CLIENT_STRING).setVisible(true);
		} else {
			map.get(PROCESS_STRING).setValue(EVERY_MODULE_STRING);
			map.get(CLIENT_STRING).setValue(EVERY_MODULE_STRING);
		}
	}
}
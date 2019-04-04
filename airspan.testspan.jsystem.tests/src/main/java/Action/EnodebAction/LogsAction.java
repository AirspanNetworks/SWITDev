package Action.EnodebAction;

import EnodeB.Components.Log.Logger;
import EnodeB.Components.Session.SessionManager;
import EnodeB.EnodeB;
import Utils.GeneralUtils;
import Utils.LogSessionParams;
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
	private final String LOG_ACTION = "LogAction";
	private final String MODULES_STRING = "Modules";
	private final String PROCESS_STRING = "Process";
	private final String CLIENT_STRING = "Client";

	/**
	 * Static List that organizes all the open session in a scenario
	 */
	private static ArrayList<LogSessionParams> logSessionParamsList;

	/**
	 * Jsystem User Inputs
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
			LogSessionParams newLogSessionParams = createLogSessionParamSet(eNodeB);
			removeSessionIfAlreadyOpened(newLogSessionParams);
			logSessionParamsList.add(newLogSessionParams);
		}
	}

	/**
	 * Removes from the logSessionParamsList array all the session that closed
	 */
	public void updateLogSessionParamsList() {
		Iterator<LogSessionParams> iter;
		iter = logSessionParamsList.iterator();
		while (iter.hasNext()) {
			LogSessionParams logSessionParams = iter.next();
			if (!logSessionParams.isSessionOpen) {
				iter.remove();
			}
		}
	}

	/**
	 * Define log level, inputClient and inputProcess for every session according to user request.
	 *
	 * @return - LogProperties for each session
	 */
	private LogSessionParams createLogSessionParamSet(EnodeB eNodeB) {
		return new LogSessionParams(eNodeB, inputSession, inputModules, inputProcess, inputClient, inputLogLevel);
	}

	/**
	 * If already requested to open specific SSH\Serial session to an eNB, remove it in order to
	 * replace by the new one
	 *
	 * @param newLogSessionParams - newLogSessionParams
	 */
	private void removeSessionIfAlreadyOpened(LogSessionParams newLogSessionParams) {
		Iterator<LogSessionParams> iter;
		iter = logSessionParamsList.iterator();
		while (iter.hasNext()) {
			LogSessionParams logSessionParams = iter.next();
			if ((newLogSessionParams.enodeB.getName().equalsIgnoreCase(logSessionParams.enodeB.getName())) &&
					(newLogSessionParams.session.value.equalsIgnoreCase(logSessionParams.session.value))) {
				iter.remove();
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
		for (LogSessionParams logSessionParams : logSessionParamsList) {
			if (logSessionParams.isSessionOpen) continue;
			printToReportLogDetails(logSessionParams, "Open");
			openLogSession(logSessionParams);
			setLogLevelAndProcessByName(logSessionParams);
			addOpenSessionToLogger(logSessionParams);
			logSessionParams.isSessionOpen = true;
		}
	}

	@Test
	@TestProperties(
			name = "Stop EnodeB Logs",
			returnParam = {"IsTestWasSuccessful"},
			paramsInclude = {"DUTs", "Session"})
	public void stopEnodeBLogs() {
		for (EnodeB eNodeB : duts) {
			LogSessionParams sessionParamSetToClose = getSessionParamSetToClose(eNodeB);
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
	 * @return - logSessionParamsToClose or null if not found
	 */
	private LogSessionParams getSessionParamSetToClose(EnodeB eNodeB) {
		for (LogSessionParams logSessionParams : logSessionParamsList) {
			if ((logSessionParams.enodeB.getName().equalsIgnoreCase(eNodeB.getName())) &&
					(logSessionParams.session.value.equalsIgnoreCase(inputSession.value))) {
				return logSessionParams;
			}
		}
		return null;
	}

	/**
	 * Print To Report Log Details when opening log session
	 *
	 * @param logSessionParams - logSessionParams
	 * @param action              - Open or Close
	 */
	private void printToReportLogDetails(LogSessionParams logSessionParams, String action) {
		GeneralUtils.startLevel(action + " Session:");
		printToReportLogDetails(logSessionParams.enodeB,
				logSessionParams.session.value,
				logSessionParams.logLevel,
				logSessionParams.module,
				logSessionParams.process,
				logSessionParams.client);
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
	 * @param logSessionParams - logSessionParams
	 */
	private void addOpenSessionToLogger(LogSessionParams logSessionParams) {
		SessionManager sessionManager = logSessionParams.enodeB.getXLP().getSessionManager();
		Logger logger = logSessionParams.enodeB.getXLP().getLogger();
		synchronized (logger.lock) {
			logger.addLoggedSessions(sessionManager);
			logger.addLogListener(logSessionParams.enodeB.getXLP());
			startLogs(logSessionParams, logger);
			logger.setCountErrorBool(true);
		}
	}

	/**
	 * Close And Generate EnB Log Files
	 *
	 * @param logSessionParams - logSessionParams
	 */
	private void closeAndGenerateEnBLogFiles(LogSessionParams logSessionParams) {
		GeneralUtils.startLevel(String.format("eNodeB %s logs", logSessionParams.enodeB.getName()));
		Logger loggers[] = logSessionParams.enodeB.getLoggers();
		for (Logger logger : loggers) {
			logger.setCountErrorBool(false);
			closeLogs(logSessionParams, logger);
			scenarioUtils.scenarioStatistics(logger, logSessionParams.enodeB);
		}
		GeneralUtils.stopLevel();
	}

	/**
	 * Start log upon request
	 *
	 * @param logger - logger
	 */
	private void startLogs(LogSessionParams logSessionParams, Logger logger) {
		switch (logSessionParams.session) {
			case SSH:
				logger.startEnodeBLog(logSessionParams.sshSessionName, LOG_ACTION);
				break;
			case SERIAL:
				logger.startEnodeBLog(logSessionParams.serialSessionName, LOG_ACTION);
				break;
			case BOTH:
				logger.startEnodeBLog(logSessionParams.sshSessionName, LOG_ACTION);
				logger.startEnodeBLog(logSessionParams.serialSessionName, LOG_ACTION);
		}
	}

	/**
	 * Close log upon request
	 *
	 * @param logger - logger
	 */
	private void closeLogs(LogSessionParams logSessionParams, Logger logger) {
		switch (logSessionParams.session) {
			case SSH:
				logger.closeEnodeBLog(logSessionParams.sshSessionName, LOG_ACTION);
				break;
			case SERIAL:
				logger.closeEnodeBLog(logSessionParams.serialSessionName, LOG_ACTION);
				break;
			case BOTH:
				logger.closeEnodeBLog(logSessionParams.sshSessionName, LOG_ACTION);
				logger.closeEnodeBLog(logSessionParams.serialSessionName, LOG_ACTION);
		}
	}

	/**
	 * Open Log Session - SSH or Serial and define session names
	 *
	 * @param logSessionParams - logSessionParams
	 */
	private void openLogSession(LogSessionParams logSessionParams) {
		switch (logSessionParams.session) {
			case SSH:
				logSessionParams.sshSessionName = openSSHSession(logSessionParams);
				setSSHParamsForReconnect(logSessionParams);
				break;
			case SERIAL:
				logSessionParams.serialSessionName = openSerialSession(logSessionParams);
				setSerialParamsForReconnect(logSessionParams);
				break;
			case BOTH:
				logSessionParams.sshSessionName = openSSHSession(logSessionParams);
				setSSHParamsForReconnect(logSessionParams);
				logSessionParams.serialSessionName = openSerialSession(logSessionParams);
				setSerialParamsForReconnect(logSessionParams);
		}
	}

	/**
	 * Open Serial Session if not opened, define name, set log session flag
	 *
	 * @param logSessionParams - logSessionParams
	 * @return - ssh session Name + LOG_ACTION suffix
	 */
	private String openSerialSession(LogSessionParams logSessionParams) {
		SessionManager sessionManager = logSessionParams.enodeB.getXLP().getSessionManager();
		if (sessionManager.getSerialSession() == null) {
			logSessionParams.enodeB.getXLP().initSerialCom();
			sessionManager.openSerialLogSession(logSessionParams.logLevel.value);
			sessionManager.getSerialSession().setLoggedSession(true);
			sessionManager.getSerialSession().setEnableCliBuffer(false);
			sessionManager.getSerialSession().setSessionStreamsForLogAction(true);
		}
		return sessionManager.getSerialSession().getName();
	}

	/**
	 * Open SSH Session if not opened, define name, set log session flag
	 *
	 * @return - serial session Name + LOG_ACTION suffix
	 */
	private String openSSHSession(LogSessionParams logSessionParams) {
		SessionManager sessionManager = logSessionParams.enodeB.getXLP().getSessionManager();
		if (sessionManager.getSSHlogSession() == null) {
			sessionManager.openSSHLogSession(logSessionParams.logLevel.value);
			sessionManager.getSSHlogSession().setLoggedSession(true);
			sessionManager.getSSHlogSession().setEnableCliBuffer(false);
			sessionManager.getSSHlogSession().setSessionStreamsForLogAction(true);
		}
		return sessionManager.getSSHlogSession().getName();
	}

	/**
	 * Open Log Session - SSH or Serial and define session names
	 */
	private void setLogLevelAndProcessByName(LogSessionParams logSessionParams) {
		switch (logSessionParams.session) {
			case SSH:
				logSessionParams.enodeB.setSessionLogLevelPerProcess(logSessionParams.sshSessionName,
						logSessionParams.client, logSessionParams.process, logSessionParams.logLevel.value);
				break;
			case SERIAL:
				logSessionParams.enodeB.setSessionLogLevelPerProcess(logSessionParams.serialSessionName,
						logSessionParams.client, logSessionParams.process, logSessionParams.logLevel.value);
				break;
			case BOTH:
				logSessionParams.enodeB.setSessionLogLevelPerProcess(logSessionParams.sshSessionName,
						logSessionParams.client, logSessionParams.process, logSessionParams.logLevel.value);
				logSessionParams.enodeB.setSessionLogLevelPerProcess(logSessionParams.serialSessionName,
						logSessionParams.client, logSessionParams.process, logSessionParams.logLevel.value);
		}
	}

	/**
	 * Set inputProcess, inputClient and log level for Reconnect thread, so in case of disconnecting an override won't occur.
	 *
	 * @param logSessionParams - logSessionParams
	 * @see EnodeB.Components.Session# updateLogLevel()
	 */
	private void setSSHParamsForReconnect(LogSessionParams logSessionParams) {
		logSessionParams.enodeB.getSSHlogSession().setProcess(logSessionParams.process);
		logSessionParams.enodeB.getSSHlogSession().setClient(logSessionParams.client);
		logSessionParams.enodeB.getSSHlogSession().setLogLevel(logSessionParams.logLevel.value);
	}

	/**
	 * Set inputProcess, inputClient and log level for Reconnect thread, so in case of disconnecting an override won't occur.
	 *
	 * @param logSessionParams - logSessionParams
	 * @see EnodeB.Components.Session# updateLogLevel()
	 */
	private void setSerialParamsForReconnect(LogSessionParams logSessionParams) {
		logSessionParams.enodeB.getSerialSession().setProcess(logSessionParams.process);
		logSessionParams.enodeB.getSerialSession().setClient(logSessionParams.client);
		logSessionParams.enodeB.getSerialSession().setLogLevel(logSessionParams.logLevel.value);
	}

	/**
	 * Open Log Session - SSH or Serial
	 * Wait for Logger thread to finish its iteration before removing from LoggedSession array.
	 */
	private void removeFromLoggedSession(LogSessionParams logSessionParams) {
		SessionManager sessionManager = logSessionParams.enodeB.getXLP().getSessionManager();
		Logger logger = logSessionParams.enodeB.getXLP().getLogger();
		synchronized (logger.lock) {
			switch (logSessionParams.session) {
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
		if (methodName.equals("startEnodeBLogs")) {
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
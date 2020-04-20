package Action.EnodebAction;

import EnodeB.Components.DAN;
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
import EnodeB.EnodeBWithDAN;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Logs Action - Start and Stop logs from EnB.
 * Created: PazZ
 * 04.2019
 */
public class LogsAction extends EnodebAction {

	/**
	 * String Constants
	 */
	private String EVERY_MODULE_STRING = "*";
	private final String LOG_ACTION = "LogAction";
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
	private LogSessionParams.Session inputSession;
	private LogSessionParams.LogLevel inputLogLevel = LogSessionParams.LogLevel.SIX;
	private LogSessionParams.Modules inputModules = LogSessionParams.Modules.ALL;
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

	@ParameterProperties(description = "Name of all EnB comma separated i.e. enb1,enb2")
	public void setDUTs(String dut) {
		this.duts = (ArrayList<EnodeB>) SysObjUtils.getInstnce().initSystemObject(EnodeB.class, false, dut.split(","));
	}

	@ParameterProperties(description = "Log session to start \\ stop ")
	public void setSession(LogSessionParams.Session inputSession) {
		this.inputSession = inputSession;
	}

	@ParameterProperties(description = "LogLevel to start")
	public void setLogLevel(LogSessionParams.LogLevel inputLogLevel) {
		this.inputLogLevel = inputLogLevel;
	}

	@ParameterProperties(description = "Modules For Start Log")
	public void setModules(LogSessionParams.Modules inputModules) {
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

	/**
	 * This Action start EnodeB logs according to user's request.
	 * In order to get the logs file, use "stopEnodeBLogs" action.
	 * Session Type: SSH / Serial / Both
	 * Log Level: 1 - 6 (Default value is 6)
	 * Modules: All / Specific Module (Specific Process and Client)
	 */
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
			addOpenSessionToLogger(logSessionParams);
			startLogFiles(logSessionParams);
			setLogLevelAndProcessByName(logSessionParams);
			logSessionParams.isSessionOpen = true;
		}
	}

	/**
	 * This Action stops EnodeB logs according to user's request.
	 * The logs must be started first with "startEnodeBLogs" Action.
	 */
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
			stopLogFiles(sessionParamSetToClose);
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
	 * @param action           - Open or Close
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
	private void printToReportLogDetails(EnodeB eNodeB, String session, LogSessionParams.LogLevel logLevel,
										 LogSessionParams.Modules modules, String process, String client) {
		report.report("Log Session: " + session + ". For EnodeB: " + eNodeB.getName());
		report.report("Log Level: " + String.valueOf(logLevel.value) + ". Modules: " + modules.value);
		if (LogSessionParams.Modules.PARTICULAR_MODEL.value.equals(modules.value)) {
			report.report("Process: " + process + ". Client: " + client);
		}
	}

	/**
	 * Add all the open Session To LoggedSessions array, in order to stream from them in Logger Thread.
	 * If the sessions already exist in this array, it won't add them again.
	 *
	 * @param logSessionParams - logSessionParams
	 */
	private void addOpenSessionToLogger(LogSessionParams logSessionParams) {
		logSessionParams.enodeB.addToLoggedSessionArray();
		logSessionParams.enodeB.addDansSessionsToLoggedSessionArray();
	}

	/**
	 * Start log files upon request - SSH, Serial (XLP and DAN)
	 */
	private void startLogFiles(LogSessionParams logSessionParams) {
		Logger loggerXLP = logSessionParams.enodeB.getXLP().getLogger();
		SessionManager sessionManagerXLP = logSessionParams.enodeB.getXLP().getSessionManager();
		switch (logSessionParams.session) {
			case SSH:
				startSSHLogFile(logSessionParams, loggerXLP, sessionManagerXLP);
				break;
			case SERIAL:
				startSerialLogFile(logSessionParams, loggerXLP, sessionManagerXLP);
				startDANsSerialLogFiles(logSessionParams);
				break;
			case BOTH:
				startSSHLogFile(logSessionParams, loggerXLP, sessionManagerXLP);
				startSerialLogFile(logSessionParams, loggerXLP, sessionManagerXLP);
				startDANsSerialLogFiles(logSessionParams);

		}
		loggerXLP.setCountErrorBool(true);
	}

	/**
	 * start DANs Serial Log Files and set flag "isDANSessionStreamsForLogAction" to true
	 *
	 * @param logSessionParams - logSessionParams
	 */
	private void startDANsSerialLogFiles(LogSessionParams logSessionParams) {
		startDANLogs(logSessionParams);
		setDANSessionStreamsForLogAction(logSessionParams, true);
	}

	/**
	 * start Serial Log File and set flag "isDANSessionStreamsForLogAction" to true
	 *
	 * @param logSessionParams  - logSessionParams
	 * @param loggerXLP         - loggerXLP
	 * @param sessionManagerXLP - sessionManagerXLP
	 */
	private void startSerialLogFile(LogSessionParams logSessionParams, Logger loggerXLP, SessionManager sessionManagerXLP) {
		loggerXLP.startEnodeBLog(logSessionParams.serialSessionName, LOG_ACTION);
		sessionManagerXLP.getSerialSession().setSessionStreamsForLogAction(true);
	}

	/**
	 * start SSH Log File and set flag "isDANSessionStreamsForLogAction" to true
	 *
	 * @param logSessionParams  - logSessionParams
	 * @param loggerXLP         - loggerXLP
	 * @param sessionManagerXLP - sessionManagerXLP
	 */
	private void startSSHLogFile(LogSessionParams logSessionParams, Logger loggerXLP, SessionManager sessionManagerXLP) {
		loggerXLP.startEnodeBLog(logSessionParams.sshSessionName, LOG_ACTION);
		sessionManagerXLP.getSSHlogSession().setSessionStreamsForLogAction(true);
	}

	/**
	 * Stop log files upon request - SSH, Serial (XLP and DAN)
	 *
	 */
	private void stopLogFiles(LogSessionParams logSessionParams) {
		GeneralUtils.startLevel(String.format("eNodeB %s logs", logSessionParams.enodeB.getName()));
		SessionManager sessionManagerXLP = logSessionParams.enodeB.getXLP().getSessionManager();
		Logger loggerXLP = logSessionParams.enodeB.getXLP().getLogger();
		loggerXLP.setCountErrorBool(false);
		switch (logSessionParams.session) {
			case SSH:
				stopSSHLogFile(logSessionParams, loggerXLP, sessionManagerXLP);
				break;
			case SERIAL:
				stopSerialLogFile(logSessionParams, loggerXLP, sessionManagerXLP);
				stopDANsLogFiles(logSessionParams);
				break;
			case BOTH:
				stopSSHLogFile(logSessionParams, loggerXLP, sessionManagerXLP);
				stopSerialLogFile(logSessionParams, loggerXLP, sessionManagerXLP);
				stopDANsLogFiles(logSessionParams);
		}
		scenarioUtils.scenarioStatistics(loggerXLP, logSessionParams.enodeB);
		GeneralUtils.stopLevel();
	}

	/**
	 * close SSH Log File and set flag  "isSessionStreamsForLogAction" to false
	 *
	 * @param logSessionParams -logSessionParams
	 * @param logger           - logger
	 * @param sessionManager   - sessionManager
	 */
	private void stopSSHLogFile(LogSessionParams logSessionParams, Logger logger, SessionManager sessionManager) {
		logger.closeEnodeBLog(logSessionParams.sshSessionName, LOG_ACTION);
		sessionManager.getSSHlogSession().setSessionStreamsForLogAction(false);
	}

	/**
	 * close Serial Log File and set flag  "isSessionStreamsForLogAction" to false
	 *
	 * @param logSessionParams -logSessionParams
	 * @param logger           - logger
	 * @param sessionManager   - sessionManager
	 */
	private void stopSerialLogFile(LogSessionParams logSessionParams, Logger logger, SessionManager sessionManager) {
		logger.closeEnodeBLog(logSessionParams.serialSessionName, LOG_ACTION);
		sessionManager.getSerialSession().setSessionStreamsForLogAction(false);
	}

	/**
	 * close DANs Serial Log Files and set flag  "isSessionStreamsForLogAction" to false
	 *
	 * @param logSessionParams - logSessionParams
	 */
	private void stopDANsLogFiles(LogSessionParams logSessionParams) {
		stopDANLogs(logSessionParams);
		setDANSessionStreamsForLogAction(logSessionParams, false);
	}

	/**
	 * Open Log Session - SSH or Serial and define session names
	 *
	 * @param logSessionParams - logSessionParams
	 */
	private void openLogSession(LogSessionParams logSessionParams) {
		switch (logSessionParams.session) {
			case SSH:
				openSSHLogSession(logSessionParams);
				break;
			case SERIAL:
				openSerialLogSession(logSessionParams);
				openDansSerialLogsSessions(logSessionParams);
				break;
			case BOTH:
				openSSHLogSession(logSessionParams);
				openSerialLogSession(logSessionParams);
				openDansSerialLogsSessions(logSessionParams);
		}
	}

	/**
	 * openSSHLogSession - open session if not opened, and set parameters for reconnect
	 *
	 * @param logSessionParams - logSessionParams
	 */
	private void openSSHLogSession(LogSessionParams logSessionParams) {
		EnodeB eNodeB = logSessionParams.enodeB;
		logSessionParams.sshSessionName = eNodeB.openSSHLogSession(eNodeB.getXLP().getSessionManager());
		setSSHParamsForReconnect(logSessionParams);
	}

	/**
	 * openSerialLogSession - open session if not opened, and set parameters for reconnect
	 *
	 * @param logSessionParams - logSessionParams
	 */
	private void openSerialLogSession(LogSessionParams logSessionParams) {
		EnodeB eNodeB = logSessionParams.enodeB;
		logSessionParams.serialSessionName = eNodeB.openSerialLogSession(eNodeB.getXLP().getSessionManager());
		setSerialParamsForReconnect(logSessionParams);
	}

	/**
	 * openDansSerialLogsSessions - open session if not opened, and set parameters for reconnect
	 *
	 * @param logSessionParams - logSessionParams
	 */
	private void openDansSerialLogsSessions(LogSessionParams logSessionParams) {
		EnodeB eNodeB = logSessionParams.enodeB;
		logSessionParams.dansSerialSessionNames = eNodeB.openSerialLogSessionForDANs();
		setDANSerialParamsForReconnect(logSessionParams);
	}

	/**
	 * set paranm "isSessionStreamsForLogAction" for all DAN devices
	 *
	 * @param logSessionParams - logSessionParams
	 */
	private void setDANSessionStreamsForLogAction(LogSessionParams logSessionParams, boolean isStreaming) {
		if (!logSessionParams.enodeB.hasDan()) return;
		DAN[] dans = ((EnodeBWithDAN) logSessionParams.enodeB).getDans();
		for (DAN dan : dans) {
			dan.getSessionManager().getSerialSession().setSessionStreamsForLogAction(isStreaming);
		}
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
	 */
	private void setSerialParamsForReconnect(LogSessionParams logSessionParams) {
		logSessionParams.enodeB.getSerialSession().setProcess(logSessionParams.process);
		logSessionParams.enodeB.getSerialSession().setClient(logSessionParams.client);
		logSessionParams.enodeB.getSerialSession().setLogLevel(logSessionParams.logLevel.value);
	}

	/**
	 * Set inputProcess, inputClient and log level for Reconnect thread, so in case of disconnecting an override won't occur.
	 * For All DAN components
	 *
	 * @param logSessionParams - logSessionParams
	 */
	private void setDANSerialParamsForReconnect(LogSessionParams logSessionParams) {
		if (!logSessionParams.enodeB.hasDan()) return;
		DAN[] dans = ((EnodeBWithDAN) logSessionParams.enodeB).getDans();
		for (DAN dan : dans) {
			dan.getSerialSession().setProcess(logSessionParams.process);
			dan.getSerialSession().setClient(logSessionParams.client);
			dan.getSerialSession().setLogLevel(logSessionParams.logLevel.value);
		}
	}

	/**
	 * Open Log Session - SSH or Serial
	 * Wait for Logger thread to finish its iteration before removing from LoggedSession array.
	 */
	private void removeFromLoggedSession(LogSessionParams logSessionParams) {
		SessionManager sessionManagerXLP = logSessionParams.enodeB.getXLP().getSessionManager();
			switch (logSessionParams.session) {
				case SSH:
					logSessionParams.enodeB.removeXLPSessionsFromLoggedSessionArray(sessionManagerXLP.getSSHlogSession());
					break;
				case SERIAL:
					logSessionParams.enodeB.removeXLPSessionsFromLoggedSessionArray(sessionManagerXLP.getSerialSession());
					logSessionParams.enodeB.removeDansSessionsFromLoggedSessionArray();
					break;
				case BOTH:
					logSessionParams.enodeB.removeXLPSessionsFromLoggedSessionArray(sessionManagerXLP.getSSHlogSession());
					logSessionParams.enodeB.removeXLPSessionsFromLoggedSessionArray(sessionManagerXLP.getSerialSession());
					logSessionParams.enodeB.removeDansSessionsFromLoggedSessionArray();
			}
		}

	/**
	 * Start Log files on all DAN loggers
	 *
	 * @param logSessionParamsSet - logSessionParamsSet
	 */
	private void startDANLogs(LogSessionParams logSessionParamsSet) {
		if (logSessionParamsSet.dansSerialSessionNames.isEmpty()) return;
		DAN[] dans = ((EnodeBWithDAN) logSessionParamsSet.enodeB).getDans();
		for (int i = 0; i < dans.length; i++) {
			Logger logger = dans[i].getLogger();
			logger.startEnodeBLog(logSessionParamsSet.dansSerialSessionNames.get(i), LOG_ACTION);
		}
	}

	/**
	 * Stop Log files on all DAN loggers
	 *
	 * @param logSessionParamsSet - logSessionParamsSet
	 */
	private void stopDANLogs(LogSessionParams logSessionParamsSet) {
		if (logSessionParamsSet.dansSerialSessionNames.isEmpty()) return;
		DAN[] dans = ((EnodeBWithDAN) logSessionParamsSet.enodeB).getDans();
		for (int i = 0; i < dans.length; i++) {
			Logger logger = dans[i].getLogger();
			logger.closeEnodeBLog(logSessionParamsSet.dansSerialSessionNames.get(i), LOG_ACTION);
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
		Parameter modules = map.get("Modules");
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
		if (LogSessionParams.Modules.PARTICULAR_MODEL ==
				LogSessionParams.Modules.valueOf(modules.getValue().toString())) {
			map.get(PROCESS_STRING).setVisible(true);
			map.get(CLIENT_STRING).setVisible(true);
		} else {
			map.get(PROCESS_STRING).setValue(EVERY_MODULE_STRING);
			map.get(CLIENT_STRING).setValue(EVERY_MODULE_STRING);
		}
	}
}
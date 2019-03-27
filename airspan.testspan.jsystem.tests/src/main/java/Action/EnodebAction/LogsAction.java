package Action.EnodebAction;

import EnodeB.Components.Log.Logger;
import EnodeB.Components.Session.SessionManager;
import EnodeB.EnodeB;
import Utils.GeneralUtils;
import Utils.SysObjUtils;
import jsystem.framework.ParameterProperties;
import jsystem.framework.TestProperties;
import org.junit.Test;
import jsystem.framework.scenario.Parameter;

import java.util.ArrayList;
import java.util.HashMap;

public class LogsAction extends EnodebAction {

	//String Constants
	private String EVERY_MODULE_STRING = "*";
	private static final String LOG_ACTION = "_LogAction";
	private static final String ACTION_NAME_STRING = "startEnodeBLogs";
	private static final String MODULES_STRING = "Modules";
	private static final String PROCESS_STRING = "Process";
	private static final String CLIENT_STRING = "Client";
	//User Inputs
	private ArrayList<EnodeB> duts;
	private Session session;
	private LogLevel logLevel = LogLevel.SIX;
	private Modules modules = Modules.ALL;
	private String process = EVERY_MODULE_STRING;
	private String client = EVERY_MODULE_STRING;
	//Test vars
	private static Modules modulesSSH;
	private static String processSSH;
	private static String clientSSH;
	private static LogLevel logLevelSSH;
	private static Modules modulesSerial;
	private static String processSerial;
	private static String clientSerial;
	private static LogLevel logLevelSerial;

	/**
	 * Enum represented by String to Process dropdown to log:
	 * All or ParticularModel(Specific Process and Client)
	 */
	private enum Modules {
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
	private enum Session {
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
	private enum LogLevel {
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

	/**
	 * SSH and serial session names are being used when we open the relevant session.
	 * Separated static constants in order to parallel track each, while the scenario is running.
	 */
	private static String sshSessionName;
	private static String serialSessionName;


	@ParameterProperties(description = "Name of all EnB comma separated i.e. enb1,enb2")
	public void setDUTs(String dut) {
		this.duts = (ArrayList<EnodeB>) SysObjUtils.getInstnce().initSystemObject(EnodeB.class, false, dut.split(","));
	}

	@ParameterProperties(description = "Log session to start \\ stop ")
	public void setSession(Session session) {
		this.session = session;
	}

	@ParameterProperties(description = "LogLevel to start")
	public void setLogLevel(LogLevel logLevel) {
		this.logLevel = logLevel;
	}

	@ParameterProperties(description = "Modules For Start Log")
	public void setModules(Modules modules) {
		this.modules = modules;
	}

	@ParameterProperties(description = "Choose a specific Process")
	public void setProcess(String process) {
		this.process = process;
	}

	@ParameterProperties(description = "Choose a specific Client")
	public void setClient(String client) {
		this.client = client;
	}

	@Test
	@TestProperties(
			name = "Start EnodeB Logs",
			returnParam = {"IsTestWasSuccessful"},
			paramsInclude = {"DUTs", "Session", "LogLevel", "Modules", "Process", "Client"})
	public void startEnodeBLogs() {
		defineLogProperties();
		for (EnodeB eNodeB : duts) {
			printToReportLogDetails(eNodeB, "Open");
			SessionManager sessionManager = eNodeB.getXLP().getSessionManager();
			openLogSession(eNodeB, sessionManager);
			setLogLevelAndProcessByName(eNodeB);
			addOpenSessionToLogger(eNodeB, sessionManager);
		}
	}

	@Test
	@TestProperties(
			name = "Stop EnodeB Logs",
			returnParam = {"IsTestWasSuccessful"},
			paramsInclude = {"DUTs", "Session"})
	public void stopEnodeBLogs() {
		for (EnodeB eNodeB : duts) {
			printToReportLogDetails(eNodeB, "Close");
			removeFromLoggedSession(eNodeB);
			closeAndGenerateEnBLogFiles(eNodeB);
		}
	}

	/**
	 * Define log level, client and process for every session according to user request.
	 */
	private void defineLogProperties() {
		switch (session) {
			case SSH:
				processSSH = process;
				clientSSH = client;
				logLevelSSH = logLevel;
				modulesSSH = modules;
				break;
			case SERIAL:
				processSerial = process;
				clientSerial = client;
				logLevelSerial = logLevel;
				modulesSerial = modules;
				break;
			case BOTH:
				processSSH = process;
				clientSSH = client;
				logLevelSSH = logLevel;
				modulesSSH = modules;
				processSerial = process;
				clientSerial = client;
				logLevelSerial = logLevel;
				modulesSerial = modules;
		}
	}

	/**
	 * Print To Report Log Details when opening log session
	 *
	 * @param eNodeB - eNodeB
	 * @param action - Open or Close
	 */
	private void printToReportLogDetails(EnodeB eNodeB, String action) {
		GeneralUtils.startLevel(action + " Session:");
		printDetailsPerSession(eNodeB);
		GeneralUtils.stopLevel();
	}

	/**
	 * print Details Per Session
	 *
	 * @param eNodeB - eNodeB
	 */
	private void printDetailsPerSession(EnodeB eNodeB) {
		switch (session) {
			case SSH:
				printToReportLogDetails(eNodeB, Session.SSH.value, logLevelSSH, modulesSSH, processSSH, clientSSH);
				break;
			case SERIAL:
				printToReportLogDetails(eNodeB, Session.SERIAL.value, logLevelSerial, modulesSerial, processSerial, clientSerial);
				break;
			case BOTH:
				printToReportLogDetails(eNodeB, Session.SSH.value, logLevelSSH, modulesSSH, processSSH, clientSSH);
				printToReportLogDetails(eNodeB, Session.SERIAL.value, logLevelSerial, modulesSerial, processSerial, clientSerial);
		}
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
	 * @param eNodeB         - eNodeB
	 * @param sessionManager - sessionManager
	 */
	private void addOpenSessionToLogger(EnodeB eNodeB, SessionManager sessionManager) {
		Logger logger = eNodeB.getXLP().getLogger();
		synchronized (logger.lock) {
			logger.addLoggedSessions(sessionManager);
			logger.addLogListener(eNodeB.getXLP());
			startLogs(logger);
			logger.setCountErrorBool(true);
		}
	}

	/**
	 * Close And Generate EnB Log Files
	 *
	 * @param eNodeB - eNodeB
	 */
	private void closeAndGenerateEnBLogFiles(EnodeB eNodeB) {
		GeneralUtils.startLevel(String.format("eNodeB %s logs", eNodeB.getName()));
		Logger loggers[] = eNodeB.getLoggers();
		for (Logger logger : loggers) {
			logger.setCountErrorBool(false);
			closeLogs(logger);
			scenarioUtils.scenarioStatistics(logger, eNodeB);
		}
		GeneralUtils.stopLevel();
	}

	/**
	 * Start log upon request
	 *
	 * @param logger - logger
	 */
	private void startLogs(Logger logger) {
		switch (session) {
			case SSH:
				logger.startEnodeBLog(sshSessionName, LOG_ACTION);
				break;
			case SERIAL:
				logger.startEnodeBLog(serialSessionName,LOG_ACTION);
				break;
			case BOTH:
				logger.startEnodeBLog(sshSessionName, LOG_ACTION);
				logger.startEnodeBLog(serialSessionName, LOG_ACTION);
		}
	}

	/**
	 * Close log upon request
	 *
	 * @param logger - logger
	 */
	private void closeLogs(Logger logger) {
		switch (session) {
			case SSH:
				logger.closeEnodeBLog(sshSessionName,LOG_ACTION);
				break;
			case SERIAL:
				logger.closeEnodeBLog(serialSessionName, LOG_ACTION);
				break;
			case BOTH:
				logger.closeEnodeBLog(sshSessionName, LOG_ACTION);
				logger.closeEnodeBLog(serialSessionName, LOG_ACTION);
		}
	}

	/**
	 * Open Log Session - SSH or Serial and define session names
	 *
	 * @param sessionManager - sessionManager
	 */
	private void openLogSession(EnodeB enodeB, SessionManager sessionManager) {
		switch (session) {
			case SSH:
				sshSessionName = openSSHSession(sessionManager);
				setSSHParamsForReconnect(enodeB);
				break;
			case SERIAL:
				serialSessionName = openSerialSession(enodeB, sessionManager);
				setSerialParamsForReconnect(enodeB);
				break;
			case BOTH:
				sshSessionName = openSSHSession(sessionManager);
				setSSHParamsForReconnect(enodeB);
				serialSessionName = openSerialSession(enodeB, sessionManager);
				setSerialParamsForReconnect(enodeB);
		}
	}

	/**
	 * Open Serial Session if not opened, define name, set log session flag
	 *
	 * @param enodeB         - enodeB
	 * @param sessionManager - enodeB
	 * @return - ssh session Name + LOG_ACTION suffix
	 */
	private String openSerialSession(EnodeB enodeB, SessionManager sessionManager) {
		if (sessionManager.getSerialSession() == null) {
			enodeB.getXLP().initSerialCom();
			sessionManager.openSerialLogSession();
			sessionManager.getSerialSession().setLoggedSession(true);
			sessionManager.getSerialSession().setEnableCliBuffer(false);
//			sessionManager.getSerialSession().setName(sessionManager.getSerialSession().getName() + LOG_ACTION);
		}
		return sessionManager.getSerialSession().getName();
	}

	/**
	 * Open SSH Session if not opened, define name, set log session flag
	 *
	 * @param sessionManager - sessionManager
	 * @return - serial session Name + LOG_ACTION suffix
	 */
	private String openSSHSession(SessionManager sessionManager) {
		if (sessionManager.getSSHlogSession() == null) {
			sessionManager.openSSHLogSession();
			sessionManager.getSSHlogSession().setLoggedSession(true);
			sessionManager.getSSHlogSession().setEnableCliBuffer(false);
//			sessionManager.getSSHlogSession().setName(sessionManager.getSSHlogSession().getName() + LOG_ACTION);
		}
		return sessionManager.getSSHlogSession().getName();
	}

	/**
	 * Open Log Session - SSH or Serial and define session names
	 */
	private void setLogLevelAndProcessByName(EnodeB eNodeB) {
		switch (session) {
			case SSH:
				eNodeB.setSSHSessionLogLevelPerProcess(sshSessionName, clientSSH, processSSH, logLevelSSH.value);
				break;
			case SERIAL:
				eNodeB.setSerialSessionLogLevelPerProcess(serialSessionName, clientSerial, processSerial, logLevelSerial.value);
				break;
			case BOTH:
				eNodeB.setSSHSessionLogLevelPerProcess(sshSessionName, clientSSH, processSSH, logLevelSSH.value);
				eNodeB.setSerialSessionLogLevelPerProcess(serialSessionName, clientSerial, processSerial, logLevelSerial.value);
		}
	}

	/**
	 * Set process, client and log level for Reconnect thread, so in case of disconnecting an override won't occur.
	 *
	 * @param eNodeB - eNodeB
	 * @see EnodeB.Components.Session# updateLogLevel()
	 */
	private void setSSHParamsForReconnect(EnodeB eNodeB) {
		eNodeB.getSSHlogSession().setProcess(processSSH);
		eNodeB.getSSHlogSession().setClient(clientSSH);
		eNodeB.getSSHlogSession().setLogLevel(logLevelSSH.value);
	}

	/**
	 * Set process, client and log level for Reconnect thread, so in case of disconnecting an override won't occur.
	 *
	 * @param eNodeB - eNodeB
	 * @see EnodeB.Components.Session# updateLogLevel()
	 */
	private void setSerialParamsForReconnect(EnodeB eNodeB) {
		eNodeB.getSerialSession().setProcess(processSerial);
		eNodeB.getSerialSession().setClient(clientSerial);
		eNodeB.getSerialSession().setLogLevel(logLevelSerial.value);
	}

	/**
	 * Open Log Session - SSH or Serial
	 * Wait for Logger thread to finish its iteration before removing from LoggedSession array.
	 */
	private void removeFromLoggedSession(EnodeB eNodeB) {
		SessionManager sessionManager = eNodeB.getXLP().getSessionManager();
		Logger logger = eNodeB.getXLP().getLogger();
		synchronized (logger.lock) {
			switch (session) {
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
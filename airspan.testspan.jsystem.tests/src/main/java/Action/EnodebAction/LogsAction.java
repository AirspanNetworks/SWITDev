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

	private ArrayList<EnodeB> duts;
	private Session session;
	private LogLevel logLevel = LogLevel.SIX;
	private Processes processes = Processes.ALL;
	private String process;
	private String client;
	private static final String LOG_ACTION = "_LogAction";
	private static final String ACTION_NAME_STRING = "startEnodeBLogs";
	private static final String PROCESS_STRING = "Process";
	private static final String PROCESSES_STRING = "Processes";
	private static final String CLIENT_STRING = "Client";


	/**
	 * Enum represented by String to Process dropdown to log:
	 * All or ParticularModel(Specific Process and Client)
	 */
	private enum Processes {
		ALL("All"),
		PARTICULAR_MODEL("Particular Model");

		final String value;

		Processes(String value) {
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

	@ParameterProperties(description = "Processes For Start Log")
	public void setProcesses(Processes processes) {
		this.processes = processes;
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
			paramsInclude = {"DUTs", "Session", "LogLevel", "Processes", "Process", "Client"})
	public void startEnodeBLogs() {
		for (EnodeB eNodeB : duts) {
			printToReportOpenLogDetails(eNodeB);
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
			printToReportCloseLogDetails(eNodeB);
			SessionManager sessionManager = eNodeB.getXLP().getSessionManager();
			removeFromLoggedSession(eNodeB, sessionManager);
			closeAndGenerateEnBLogFiles(eNodeB, eNodeB.getLoggers());
		}
	}

	/**
	 * Print To Report Log Details when opening log session
	 *
	 * @param eNodeB - eNodeB
	 */
	private void printToReportOpenLogDetails(EnodeB eNodeB) {
		GeneralUtils.startLevel("Opening Session:");
		printToReportLogDetails(eNodeB);
		GeneralUtils.stopLevel();
	}

	/**
	 * Print To Report Log Details when closing log session
	 *
	 * @param eNodeB - eNodeB
	 */
	private void printToReportCloseLogDetails(EnodeB eNodeB) {
		GeneralUtils.startLevel("Closing Session:");
		printToReportLogDetails(eNodeB);
		GeneralUtils.stopLevel();
	}

	/**
	 * Print To Report Log Details:
	 * Session, EnodeB, Log Level, Processes: All or Process, Client.
	 *
	 * @param eNodeB - eNodeB
	 */
	private void printToReportLogDetails(EnodeB eNodeB) {
		report.report("Log Session: " + session.value + ". For EnodeB: " + eNodeB.getName());
		report.report("Log Level: " + logLevel.value + ". Processes: " + processes.value);
		if (Processes.PARTICULAR_MODEL.value.equals(processes.value)) {
			report.report("Process: " + process + ". Client: " + client);
		}
	}

	/**
	 * Add Open Session To Logger Thread
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
	 * @param eNodeB  - eNodeB
	 * @param loggers - loggers
	 */
	private void closeAndGenerateEnBLogFiles(EnodeB eNodeB, Logger[] loggers) {
		GeneralUtils.startLevel(String.format("eNodeB %s logs", eNodeB.getName()));
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
				logger.startEnodeBLog(sshSessionName);
				break;
			case SERIAL:
				logger.startEnodeBLog(serialSessionName);
				break;
			case BOTH:
				logger.startEnodeBLog(sshSessionName);
				logger.startEnodeBLog(serialSessionName);
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
				logger.closeEnodeBLog(sshSessionName);
				break;
			case SERIAL:
				logger.closeEnodeBLog(serialSessionName);
				break;
			case BOTH:
				logger.closeEnodeBLog(sshSessionName);
				logger.closeEnodeBLog(serialSessionName);
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
				break;
			case SERIAL:
				serialSessionName = openSerialSession(enodeB, sessionManager);
				break;
			case BOTH:
				sshSessionName = openSSHSession(sessionManager);
				serialSessionName = openSerialSession(enodeB, sessionManager);
		}
	}

	/**
	 * Open Serial Session, define name, set log session flag
	 *
	 * @param enodeB         - enodeB
	 * @param sessionManager - enodeB
	 * @return - ssh session Name + LOG_ACTION suffix
	 */
	private String openSerialSession(EnodeB enodeB, SessionManager sessionManager) {
		enodeB.getXLP().initSerialCom();
		sessionManager.openSerialLogSession();
		sessionManager.getSerialSession().setLoggedSession(true);
		sessionManager.getSerialSession().setEnableCliBuffer(false);
		return sessionManager.getSerialSession().getName() + LOG_ACTION;
	}

	/**
	 * Open SSH Session, define name, set log session flag
	 *
	 * @param sessionManager - sessionManager
	 * @return - serial session Name + LOG_ACTION suffix
	 */
	private String openSSHSession(SessionManager sessionManager) {
		sessionManager.openSSHLogSession();
		sessionManager.getSSHlogSession().setLoggedSession(true);
		sessionManager.getSSHlogSession().setEnableCliBuffer(false);
		return sessionManager.getSSHlogSession().getName() + LOG_ACTION;
	}

	/**
	 * Open Log Session - SSH or Serial
	 * Wait for Logger thread to finish its iteration before removing from LoggedSession array.
	 *
	 * @param sessionManager - sessionManager
	 */
	private void removeFromLoggedSession(EnodeB eNodeB, SessionManager sessionManager) {
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
	 * Open Log Session - SSH or Serial and define session names
	 */
	private void setLogLevelAndProcessByName(EnodeB enodeB) {
		switch (session) {
			case SSH:
				setLogLevelAndProcess(enodeB, sshSessionName);
				break;
			case SERIAL:
				setLogLevelAndProcess(enodeB, serialSessionName);
				break;
			case BOTH:
				setLogLevelAndProcess(enodeB, sshSessionName);
				setLogLevelAndProcess(enodeB, serialSessionName);
		}
	}

	/**
	 * Select Processes and log level to stream
	 *
	 * @param eNodeB      - eNodeB
	 * @param sessionName - sessionName
	 */
	private void setLogLevelAndProcess(EnodeB eNodeB, String sessionName) {
		switch (processes) {
			case ALL:
				eNodeB.setSessionLogLevel(sessionName, logLevel.value);
				break;
			case PARTICULAR_MODEL:
				eNodeB.setSessionLogLevelPerProcess(sessionName, client, process, logLevel.value);
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
	 * Show parameters Process and Client in case the user select Processes: Particular Model
	 *
	 * @param map - map
	 */
	private void handleUIEventGetCounterValue(HashMap<String, Parameter> map) {
		map.get(PROCESS_STRING).setVisible(false);
		map.get(CLIENT_STRING).setVisible(false);
		Parameter processes = map.get(PROCESSES_STRING);
		setProcessesMenuVisible(map, processes);
	}

	/**
	 * Set Processes Menu Visible
	 *
	 * @param map       - map
	 * @param processes - processes
	 */
	private void setProcessesMenuVisible(HashMap<String, Parameter> map, Parameter processes) {
		if (Processes.PARTICULAR_MODEL == Processes.valueOf(processes.getValue().toString())) {
			map.get(PROCESS_STRING).setVisible(true);
			map.get(CLIENT_STRING).setVisible(true);
		} else {
			map.get(PROCESS_STRING).setValue(null);
			map.get(CLIENT_STRING).setValue(null);
		}
	}
}
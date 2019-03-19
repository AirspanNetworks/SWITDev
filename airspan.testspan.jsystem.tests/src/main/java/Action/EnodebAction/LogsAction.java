package Action.EnodebAction;

import EnodeB.Components.Log.Logger;
import EnodeB.Components.Session.SessionManager;
import EnodeB.EnodeB;
import Utils.SysObjUtils;
import jsystem.framework.ParameterProperties;
import jsystem.framework.TestProperties;
import org.junit.Test;
import jsystem.framework.scenario.Parameter;

import java.util.ArrayList;
import java.util.HashMap;

public class LogsAction extends EnodebAction {

	//	protected EnodeB dut;
	private ArrayList<EnodeB> duts;
	protected static Session session;
	private LogLevel logLevel = LogLevel.SIX;
	private Processes processes = Processes.ALL;
	protected String process;
	protected String client;

	/**
	 * Process dropdown to log: All or ParticularModel(Specific Process and Client)
	 */
	private enum Processes {
		ALL("All"),
		PARTICULAR_MODEL("Particular Model");

		public String value;

		Processes(String value) {
			this.value = value;
		}
	}

	/**
	 * session dropdown- SSH or Serial
	 */
	private enum Session {
		SSH("SSH"),
		SERIAL("Serial");

		String value;

		Session(String value) {
			this.value = value;
		}
	}

	/**
	 * session dropdown - SSH or Serial
	 */
	private enum LogLevel {
		ONE(1),
		TWO(2),
		THREE(3),
		FOUR(4),
		FIVE(5),
		SIX(6);

		int value;

		LogLevel(int value) {
			this.value = value;
		}
	}

//	@ParameterProperties(description = "Name of ENB from the SUT")
//	public void setDUT(String dut) {
//		ArrayList<EnodeB> temp = (ArrayList<EnodeB>) SysObjUtils.getInstnce().initSystemObject(EnodeB.class, false,
//				dut.split(","));
//		this.dut = temp.get(0);
//	}

	@ParameterProperties(description = "Name of all EnB comma separated i.e. enb1,enb2")
	public void setDUTs(String dut) {
		this.duts = (ArrayList<EnodeB>) SysObjUtils.getInstnce().initSystemObject(EnodeB.class, false, dut.split(","));
	}

	@ParameterProperties(description = "session for start log")
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
			SessionManager sessionManager = eNodeB.getXLP().getSessionManager();
			String sessionName = openLogSession(sessionManager);
			defineLogProperties(eNodeB, sessionName);
			addOpenSessionToLogger(eNodeB, sessionManager);
		}
	}

	/**
	 * add Open Session To Logger Thread
	 *
	 * @param eNodeB         - eNodeB
	 * @param sessionManager - sessionManager
	 */
	private void addOpenSessionToLogger(EnodeB eNodeB, SessionManager sessionManager) {
		Logger logger = eNodeB.getXLP().getLogger();
		synchronized (logger.lock) {
			logger.addLoggedSessions(sessionManager);
			logger.addLogListener(eNodeB.getXLP());
			logger.startLog(String.format("%s_%s", getMethodName(), logger.getParent().getName()));
			logger.setCountErrorBool(true);
		}
	}

	@Test
	@TestProperties(name = "Stop EnodeB Logs", returnParam = {"IsTestWasSuccessful"}, paramsInclude = {"DUTs"})
	public void stopEnodeBLogs() {
		for (EnodeB eNodeB : duts) {
			SessionManager sessionManager = eNodeB.getXLP().getSessionManager();
			removeFromLoggedSession(eNodeB,sessionManager);
			closeAndGenerateEnBLogFiles(eNodeB, eNodeB.getLoggers());
		}
	}

	/**
	 * open Log Session - SSH or Serial
	 *
	 * @param sessionManager - sessionManager
	 * @return - sessionName
	 */
	private String openLogSession(SessionManager sessionManager) {
		String sessionName;
		if (session == Session.SSH) {
			sessionManager.openSSHLogSession();
			sessionName = sessionManager.getSSHlogSession().getName();
			sessionManager.getSSHlogSession().setLoggedSession(true);
			sessionManager.getSSHlogSession().setEnableCliBuffer(false);
		} else {
			sessionManager.openSerialLogSession();
			sessionName = sessionManager.getSerialSession().getName();
			sessionManager.getSerialSession().setLoggedSession(true);
			sessionManager.getSerialSession().setEnableCliBuffer(false);
		}
		return sessionName;
	}

	/**
	 * open Log Session - SSH or Serial
	 *
	 * @param sessionManager - sessionManager
	 * @return - sessionName
	 */
	private void removeFromLoggedSession(EnodeB eNodeB, SessionManager sessionManager) {
		Logger logger = eNodeB.getXLP().getLogger();
		synchronized (logger.lock) {
			if (session == Session.SSH) {
				logger.removeFromLoggedSessions(sessionManager.getSSHlogSession());
			} else {
				logger.removeFromLoggedSessions(sessionManager.getSerialSession());
			}
		}
	}


	/**
	 * select Processes to log
	 *
	 * @param eNodeB      - eNodeB
	 * @param sessionName - sessionName
	 */
	private void defineLogProperties(EnodeB eNodeB, String sessionName) {
		if (processes.value.equals(Processes.ALL.value)) {
			eNodeB.setSessionLogLevel(sessionName, logLevel.value);
		}
		if (processes.value.equals(Processes.PARTICULAR_MODEL.value)) {
			eNodeB.setSessionLogLevelPerProcess(sessionName, client, process, logLevel.value);
		}
	}

	/**
	 * handle UI to Jsystem for startEnodeBLogs Action
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
	 * Show parameters Process and Client in case the user select Processes: Particular Model
	 *
	 * @param map - map
	 */
	private void handleUIEventGetCounterValue(HashMap<String, Parameter> map) {
		map.get("Process").setVisible(false);
		map.get("Client").setVisible(false);
		Parameter processes = map.get("Processes");
		if (Processes.PARTICULAR_MODEL == Processes.valueOf(processes.getValue().toString())) {
			map.get("Process").setVisible(true);
			map.get("Client").setVisible(true);
		} else {
			map.get("Process").setValue(null);
			map.get("Client").setValue(null);
		}
	}
}
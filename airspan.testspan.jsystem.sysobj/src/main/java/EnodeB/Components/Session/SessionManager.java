package EnodeB.Components.Session;

import java.util.ArrayList;

import EnodeB.Components.EnodeBComponent;
import Utils.GeneralUtils;
import Utils.Properties.TestspanConfigurationsManager;

public class SessionManager {
	public static final String SERIAL_SESSION_NAME = "Serial";
	public static final String SSH_SESSION_NAME = "SSH";
	public static final String SSH_LOG_SESSION_NAME = "SSHlogSession";
	public static final String SSH_COMMANDS_SESSION_NAME = "CommandsSession";
	public static final long SESSION_WAIT_TIMEOUT = 15 * 1000;
	private final static String LOG_LEVEL_PROPERTY_NAME = "logger.sessionLogLevel";
	private final static String CONSOLE_LOG_LEVEL_PROPERTY_NAME = "logger.consoleSessionLogLevel";
	private final static String COMMAND_LOG_LEVEL_PROPERTY_NAME = "logger.commandSessionLogLevel";
	private final static int LOG_LEVEL_NO_VALUE = -2;
	private ArrayList<Session> sessions;
	private Session SSHCommandSession;
	private Session SSHlogSession;
	private Session serialSession;
	private EnodeBComponent enodeBComponent;
	private int SSHlogLevel = -1;
	private int serialLogLevel = -1;
	private int commandsLogLevel = -1;
	public SessionManager(EnodeBComponent enodeBComponent) {
		this.enodeBComponent = enodeBComponent;
		this.sessions = new ArrayList<Session>();
	}

	/**
	 * open Serial Log Session
	 */
	public void openSerialLogSession() {
		serialLogLevel = setDefaultCommandSessionLevelFromPropFile(CONSOLE_LOG_LEVEL_PROPERTY_NAME);
		if (enodeBComponent.serialCom != null){
			openSerialSession();
//			if(SSHCommandSession == null && getEnodeBComponent() instanceof DAN){
//				SSHCommandSession = getSerialSession();
//			}
		}
	}

	/**
	 * open Serial Log Session with specific log level
	 *
	 * @param logLevel
	 */
	public void openSerialLogSession(int logLevel) {
		serialLogLevel = logLevel;
		if (enodeBComponent.serialCom != null){
			openSerialSession();
		}
	}

	/**
	 * open SSH Log Session
	 *
	 * @param logLevel
	 */
	public void openSSHLogSession(int logLevel) {
		SSHlogLevel = logLevel;
		String sessionName = openSession(getEnodeBComponent().getName() + "_" + SSH_LOG_SESSION_NAME, SSHlogLevel);
		if (sessionName != null)
		{
			SSHlogSession = getSession(sessionName);
			SSHlogSession.setShouldStayInCli(true);
		}
	}

	/**
	 * open SSH Log Session
	 */
	public void openSSHLogSession() {
		SSHlogLevel = setDefaultCommandSessionLevelFromPropFile(LOG_LEVEL_PROPERTY_NAME);
		String sessionName = openSession(getEnodeBComponent().getName() + "_" + SSH_LOG_SESSION_NAME, SSHlogLevel);
		if (sessionName != null)
		{
			SSHlogSession = getSession(sessionName);
			SSHlogSession.setShouldStayInCli(true);
		}
	}

	/**
	 * open SSH Command Session
	 */
	public void openSSHCommandSession() {
		commandsLogLevel = setDefaultCommandSessionLevelFromPropFile(COMMAND_LOG_LEVEL_PROPERTY_NAME);
		String sessionName = openSession(getEnodeBComponent().getName() + "_" + SSH_COMMANDS_SESSION_NAME, commandsLogLevel);
		if (sessionName != null)
			SSHCommandSession = getSession(sessionName);
	}

	/**
	 * get the command session log level from testspan.prop if exists
	 */
	public int setDefaultCommandSessionLevelFromPropFile(String propTitle) {
		int logLevel;
		try {
			logLevel = Integer.parseInt(TestspanConfigurationsManager.getInstance().getConfig(propTitle));
		} catch (Exception e) {
			GeneralUtils.printToConsole(propTitle + " is not defined in testpan.properties file. Can't set sessions log level!");
			logLevel = LOG_LEVEL_NO_VALUE; // property doesn't exist so don't use it.
		}
		return logLevel;
	}

	private synchronized boolean openSerialSession() {
		Session newConsoleSession = new Session(getEnodeBComponent().getName() + "_" + SERIAL_SESSION_NAME, getEnodeBComponent(), getEnodeBComponent().serialCom.getSerial(), serialLogLevel);
		boolean ans = newConsoleSession.waitForSessionToConnect(SESSION_WAIT_TIMEOUT);
		sessions.add(newConsoleSession);
		setSerialSession(newConsoleSession);
		boolean loginSuccess = newConsoleSession.loginSerial();
		String idResult = newConsoleSession.sendCommands(EnodeBComponent.SHELL_PROMPT, "id", "");
		GeneralUtils.printToConsole("serial id: " + idResult);
		GeneralUtils.printToConsole("Session " + newConsoleSession.getName() + " opened Status:" + ans);
		return loginSuccess;
	}

	public synchronized String openSession() {
		return openSession(SSH_SESSION_NAME + (sessions.size() + 1));
	}

	public synchronized String openSession(String name, int loglevel) {
		GeneralUtils.printToConsole("Trying to open new session " + name);
		if (enodeBComponent.getIpAddress() != null) {
			if (getSession(name) == null) {
				Session session = new Session(name, getEnodeBComponent(), loglevel);
				session.init();
				boolean ans = session.waitForSessionToConnect(SESSION_WAIT_TIMEOUT);
				sessions.add(session);
				GeneralUtils.printToConsole("Session " + name + " opened Status:" + ans);
				return name;
			}
			GeneralUtils.printToConsole("Session " + name + " already exists.");
		}
		return null;
	}
	public synchronized String openSession(String name) {
		return openSession(name, SSHlogLevel);
	}

	public Session[] getAllSessions() {
		return sessions.toArray(new Session[] { null });
	}

	public void restartSessions() {
		for (Session session : sessions) {
			session.reStartReconnectionThread();
		}
	}
	
	public void updateAllSessionsLogLevel(){
		for (Session session : sessions) {
			GeneralUtils.printToConsole("update log level from updateAllSessionsLogLevel");
			session.updateLogLevel();
		}
	}

	public void showLoginStatus() {
		for (Session session : sessions) {
			if (!session.getName().contains(SERIAL_SESSION_NAME)) {
				session.showLoginStatus();
			}			
		}
	}
	
	public Session getSession(String name) {
		for (Session session : sessions) {
			if (session.getName().equals(name))
				return session;
		}
		return null;
	}

	public synchronized boolean closeSession(String name) {
		Session session = getSession(name);
		if (session != null) {
			GeneralUtils.printToConsole("Closing session " + name);
			session.close();
			sessions.remove(session);
			return true;
		}
		return false;
	}

	public synchronized void closeAllSessions() {
		ArrayList<Session> clonedSessions = (ArrayList<Session>) sessions.clone();
		for (Session session : clonedSessions) {
			closeSession(session.getName());
		}
	}

	public synchronized String sendCommandDefaultSession(String prompt, String command, String response, int responseTimeout) {
		return sendCommands(this.SSHCommandSession.getName(), prompt, command, response, responseTimeout);
	}

	/** Wrapper tp sendCommands with an option to response Timeout
	 * @param sessionName
	 * @param prompt
	 * @param command
	 * @param response
	 * @return
	 */
	public synchronized String sendCommands(String sessionName, String prompt, String command, String response) {
		return sendCommands( sessionName,  prompt,  command,  response, 10);
	}

	public synchronized String sendCommands(String sessionName, String prompt, String command, String response, int responseTimeout) {
		Session session = getSession(sessionName);
		if (session != null) {
			if (session.isConnected() || session.waitForSessionToConnect(SESSION_WAIT_TIMEOUT))
				return session.sendCommands(prompt, command, response, responseTimeout);
			else
				GeneralUtils.printToConsole("Session: " + sessionName + " is not connectd");
		} else
			GeneralUtils.printToConsole("Cannot find session " + sessionName);
		return "";
	}

	public EnodeBComponent getEnodeBComponent() {
		return enodeBComponent;
	}

	@Override
	public String toString() {
		return getEnodeBComponent().getName();
	}

	/** Command Session
	 * @return - SSHCommandSession
	 */
	public Session getSSHCommandSession() {
		return SSHCommandSession;
	}
	
	public Session getSSHlogSession() {
		return SSHlogSession;
	}	

	public boolean isSessionConnected(String sessionName) {
		Session session = getSession(sessionName);
		if (session != null) {
			return session.isConnected();
		} else
			GeneralUtils.printToConsole("Cannot find session " + sessionName);
		return false;
	}

	public Session getSerialSession() {
		return serialSession;
	}

	public void setSerialSession(Session serialSession) {
		this.serialSession = serialSession;
	}
}
package Utils;

import Action.EnodebAction.LogsAction;
import EnodeB.EnodeB;


/**
 * LogSessionParams per EnB
 * PazZ
 */
public class LogSessionParams {
	/**
	 * Parameter set vars, for each Session
	 */
	public Pair<EnodeB, LogsAction.Session> enBSessionPair;
	public EnodeB enodeB;
	public LogsAction.Session session;
	public LogsAction.Modules module;
	public String process;
	public String client;
	public LogsAction.LogLevel logLevel;
	public boolean isSessionOpen;

	/**
	 * SSH and serial session names are being used when we open the relevant session.
	 * Separated constants in order to parallel track each, while the scenario is running.
	 */
	public String sshSessionName;
	public String serialSessionName;

	/**
	 * This constructor will be created for every session in order to save all the session params

	 */
	public LogSessionParams(EnodeB eNodeB, LogsAction.Session inputSession, LogsAction.Modules inputModule,
								String inputProcess, String inputClient, LogsAction.LogLevel inputLogLevel) {
		this.enodeB = eNodeB;
		this.session = inputSession;
		this.module = inputModule;
		this.process = inputProcess;
		this.client = inputClient;
		this.logLevel = inputLogLevel;
		this.isSessionOpen = false;
		this.enBSessionPair = new Pair<>(eNodeB, inputSession);
	}
}

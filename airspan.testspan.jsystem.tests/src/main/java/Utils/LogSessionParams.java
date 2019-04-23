package Utils;

import EnodeB.EnodeB;

import java.util.ArrayList;


/**
 * LogSessionParams per EnB
 * Created: PazZ
 * 04.2019
 */
public class LogSessionParams {
	/**
	 * Parameter set vars, for each Session
	 */
	public EnodeB enodeB;
	public Session session;
	public Modules module;
	public String process;
	public String client;
	public LogLevel logLevel;

	/**
	 * flag - true if session was opened
	 */
	public boolean isSessionOpen;

	/**
	 * SSH and Serial (DANs and XLP) session names are being used when we open the relevant session.
	 * Separated constants in order to parallel track each, while the scenario is running.
	 */
	public String sshSessionName;
	public String serialSessionName;
	public ArrayList<String> dansSerialSessionNames;


	/**
	 * This constructor will be created for every session in order to save all the session params
	 */
	public LogSessionParams(EnodeB eNodeB, Session inputSession, Modules inputModule,
							String inputProcess, String inputClient, LogLevel inputLogLevel) {
		this.enodeB = eNodeB;
		this.session = inputSession;
		this.module = inputModule;
		this.process = inputProcess;
		this.client = inputClient;
		this.logLevel = inputLogLevel;
		this.isSessionOpen = false;
	}

	/**
	 * Enum represented by String to Process dropdown to log:
	 * All or ParticularModel(Specific Process and Client)
	 */
	public enum Modules {
		ALL("All"),
		PARTICULAR_MODEL("Particular Model");

		public final String value;

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

		public final String value;

		Session(String value) {
			this.value = value;
		}
	}

	/**
	 * Enum represented by int to LogLevel dropdown - 1 to 6
	 */
	public enum LogLevel {
		ZERO(0),
		ONE(1),
		TWO(2),
		THREE(3),
		FOUR(4),
		FIVE(5),
		SIX(6);

		public final int value;

		LogLevel(int value) {
			this.value = value;
		}
	}
}
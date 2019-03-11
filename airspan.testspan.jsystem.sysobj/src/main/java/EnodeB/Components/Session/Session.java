package EnodeB.Components.Session;

import java.io.IOException;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import systemobject.terminal.SSH;
import systemobject.terminal.Terminal;
import EnodeB.Components.EnodeBComponent;
import EnodeB.Components.Cli.Cli;
import Utils.GeneralUtils;
import Utils.TerminalUtils;
import jsystem.framework.report.ListenerstManager;
import jsystem.framework.report.Reporter;

public class Session implements Runnable {

	private final static long RECONNECT_TIMEOUT = 30000;

	private Thread reconnectionThread;
	private String name;
	private Terminal terminal;
	private volatile boolean reconnect;
	private volatile boolean connected;
	private EnodeBComponent enbComp;
	private String privateBuffer;
	private String loggerBuffer;
	private String cliBuffer;
	private Thread loggerBufferThread;
	private Cli cli;
	private boolean loggedSession;
	private boolean shouldStayInCli = false;
	private boolean isSessionInitialized = false;
	
	//lock used to allow safe login without interruption from other commands. every access to sendRawCommands should be after aquiring this
	public Semaphore sshConnectionLock = new Semaphore(1); 
		
	// if true cli buffer is saved all the time,
	// otherwise it'll be filled only when a cli commend is being sent
	private boolean enableCliBuffer;
	private int logLevel = -1;

	private String hostname = "";

	public static Reporter report = ListenerstManager.getInstance();

	public Session(String name, EnodeBComponent enbComp, int logLevel) {
		this(name, enbComp, new SSH(enbComp.getIpAddress(), enbComp.getUsername(), enbComp.getPassword()),logLevel);
	}

	public Session(String name, EnodeBComponent enbComp, Terminal terminal, int logLevel) {

		this.name = name;
		this.terminal = terminal;
		this.reconnect = true;
		this.connected = false;
		this.enbComp = enbComp;
		if (enbComp == null) {
			GeneralUtils.printToConsole("enbComp is null, investigate! Session: " + name);
			GeneralUtils.printToConsole(Thread.currentThread().getStackTrace());
		}
		this.hostname = enbComp.getIpAddress();
		privateBuffer = cliBuffer = loggerBuffer = "";
		cli = new Cli(this);

		enbComp.addPrompts(cli);

		this.setEnableCliBuffer(true);
		this.logLevel=logLevel;

		reconnectionThread = new Thread(this, String.format("%s's reconnect thread.", toString()));
		
		if (this.terminal instanceof SSH) {
			reconnectSSH();
		} else // in case of terminal Serial.
		{
			this.connected = true;
		}
	}

	public boolean loginSerial() {
		GeneralUtils.printToConsole("getting to login prompt in serial:");
		int i = 1;
		long startTime = System.currentTimeMillis();
		
		while (System.currentTimeMillis() - startTime <= 30*1000) {
			GeneralUtils.printToConsole("Attempt #" + i++);
			String ans = this.sendCommands(EnodeBComponent.SHELL_PROMPT,"exit","login");			
			if (ans.contains("login")) {
				return true;
			}
			GeneralUtils.unSafeSleep(500);
		}
		
		return false;
	}

	public void init() {
		GeneralUtils.printToConsole("reconnection thread " + getName() + " starting.");
		reconnectionThread.start();
	}

	/**
	 * Wrapper to sendCommands - with default responseTimeout=10
	 * Send commands to the terminal via a specific prompt (like lteCli>>, tnet>, or shell)
	 *
	 * @param prompt
	 *            the prompt to send the commands from
	 * @param command
	 *            the commands you want to send
	 * @return the output of the command
	 */
	public String sendCommands(String prompt, String command, String response) {
		return sendCommands( prompt,  command,  response,10);
	}

	/**
	 * Send commands to the terminal via a specific prompt (like lteCli>>, tnet>, or shell)
	 * 
	 * @param prompt
	 *            the prompt to send the commands from
	 * @param command
	 *            the commands you want to send
	 * @return the output of the command
	 */
	public String sendCommands(String prompt, String command, String response,int responseTimeout) {
		String ans = "";
		try {
			boolean enableCliBuffer = this.enableCliBuffer;
			setEnableCliBuffer(true);

			cliBuffer = ""; // Clear Cli buffer
			ans = cli.sendCommands(prompt, command, response, responseTimeout);
			setEnableCliBuffer(enableCliBuffer);
		} catch (Exception e) {
			GeneralUtils.printToConsole("Error sending string to cli " + e.getMessage());
		}
		return ans;
	}

	/**
	 * Sends commands to the terminal directly
	 * 
	 * @param commands
	 *            the commands to send
	 */
	public synchronized void sendRawCommand(String command) {
		try
		{
			terminal.sendString(command + "\n", true);
		} catch (IOException e) {
			GeneralUtils.printToConsole("Error sending string to Session " + getName() + ": " + e.getMessage());
			connected = false;
		} catch (Exception e) {
			GeneralUtils.printToConsole("Error sending string to Session " + getName() + ": " + e.getMessage());
			e.printStackTrace();
			connected = false;
		}
	}

	@Override
	public String toString() {
		return "Session [name=" + name + "]";
	}

	@Override
	public void run() {
		while (reconnect) {
			reconnectSSH();
			GeneralUtils.unSafeSleep(RECONNECT_TIMEOUT);
		}
		GeneralUtils.printToConsole("Session " + getName()
				+ " reconnecting thread was closed. the session status is:connected=" + connected);
	}

	private void reconnectSSH() {
		if (enbComp.isReachable()) {
			if (!connected) {
				GeneralUtils.printToConsole("Trying to connect to Session " + getName() + " with user " + enbComp.getUsername());
				
				if(this.hostname != enbComp.getIpAddress()){//If the Terminal's hostname isn't updated.
					GeneralUtils.printToConsole("EnodeBComponent's IP Address have been changed from: "+this.hostname+" to: "+enbComp.getIpAddress());
					this.terminal = new SSH(enbComp.getIpAddress(), enbComp.getUsername(), enbComp.getPassword());
					this.hostname = enbComp.getIpAddress();
				}
				
				connected = connectSession();
			
				if (!connected) {
					return;
				}

				if (enbComp.getUsername() != EnodeBComponent.UNSECURED_USERNAME) {					
					boolean ans = false;
					try {
						sshConnectionLock.acquire();
						String suCommand = "su -";
						if (sendCommands("", "lte_cli:>>"))
							suCommand = "airspansu";
						GeneralUtils.printToConsole(enbComp.getName() + " is in a secured mode, switching to root with '" + suCommand + "' command.");
						try {
							sendCommands(suCommand, "Password");
							ans = sendCommands(EnodeBComponent.ADMIN_PASSWORD, "#");

							if (!ans) {
								sendCommands(suCommand, "Password");
								sendRawCommand(EnodeBComponent.ADMIN_PASSWORD);
							}
						} finally {
							sshConnectionLock.release();
						}
					} catch (Exception e) {
						GeneralUtils.printToConsole("Failed getting semaphore to perform su action");
						e.printStackTrace();
					}
				}
				GeneralUtils.printToConsole("update log level from reconnect ssh");
				updateLogLevel();
				enbComp.setDebugFlags();
				
				GeneralUtils.printToConsole("Session " + getName() + " connected with user " + enbComp.getUsername());
			}
			else
			{
				if(isShouldStayInCli())
				{
					if(!sendCommands("", "lte_cli:>>"))
					{
						GeneralUtils.printToConsole(getName() + " is out of lte_cli, trying to enter.");
						if(sendCommands("/bs/lteCli", "lte_cli:>>"))
						{
							GeneralUtils.printToConsole("update log level from stay in cli");
							updateLogLevel();
						}
						else
						{
							GeneralUtils.printToConsole(getName() + " is still out of lte_cli, disconnecting session.");
							disconnectSession();
						}
					}
				}
				else
					sendRawCommand("");
			}
		} else {
			disconnectSession();
		}
		return;
	}

	private boolean disconnectSession() {
		try {
			GeneralUtils.printToConsole("Session " + getName() + " terminal disconnecting");
			connected = false;
			terminal.disconnect();
			return true;
		} catch (Exception e) {
			GeneralUtils.printToConsole("Error in terminal disconnect");
			e.printStackTrace();
			return false;

		}
	}

	private boolean connectSession() {
		try {
			terminal.connect();
			return terminal.isConnected();
		} catch (Exception e) {
			GeneralUtils.printToConsole("Error in terminal " + getName() + " connect");
			e.printStackTrace();
			disconnectSession();
			return false;
		}
	}

	public void reStartReconnectionThread() {
		if (this.terminal instanceof SSH  && !reconnectionThread.isAlive()) {
			GeneralUtils.printToConsole(toString() + "'s reconnect thread stopped! restarting.");
			reconnect = true;
			reconnectionThread = new Thread(this, String.format("%s's reconnect thread.", toString()));
			reconnectionThread.start();
		}
	}

	/**
	 * Waits for the session to connect. This method only works on session that
	 * support reconnecting (By default every session except console sessions)
	 * 
	 * @param timeout
	 */
	public boolean waitForSessionToConnect(long timeout) {
		long sleepTime = 1000;
		while (!connected && timeout > 0) {
			GeneralUtils.unSafeSleep(sleepTime);
			timeout -= sleepTime;
		}
		GeneralUtils.printToConsole("Finished waiting for session, returing the connected flag: " + connected);

		return connected;
	}

	/**
	 * Read the buffer from the terminal and populate the cli & logger buffers
	 * (is enabled)
	 */
	private synchronized void readInputBuffer() {
		try {
			if (connected)
				privateBuffer += terminal.readInputBuffer().replaceAll("\r", "");
			else
				return;
		} catch (Exception e) {
			GeneralUtils.printToConsole("Failed to read from buffer " + e.getMessage());
			return;
		}

		privateBuffer = privateBuffer.replaceAll("[^[^\\p{Print}]\t\n]", "");
		int lastIndx = privateBuffer.lastIndexOf("\n");
		if (lastIndx > -1) {
			String buffer = privateBuffer.substring(0, lastIndx + 1);
			privateBuffer = privateBuffer.substring(lastIndx + 1);

			if (loggedSession)
				loggerBuffer += buffer;
			else
				loggerBuffer = "";

			if (enableCliBuffer)
				cliBuffer += buffer;
			else
				cliBuffer = "";
		}
	}

	public void showLoginStatus()
	{
		if (connected) 
		{
			String ans = this.sendCommands(EnodeBComponent.SHELL_PROMPT,"id","root");
			GeneralUtils.printToConsole(getName() + " response to id: " + ans);
			if(ans.contains("root"))
				report.report("session " + getName() + " is connected with user '"  + enbComp.getUsername() + "'");
			else
			{
				if (this.terminal instanceof SSH) {	
					report.report("session " + getName() + " cant verify root status, attempt to reconnect.");
					disconnectSession();
					reconnectSSH();
					ans = this.sendCommands(EnodeBComponent.SHELL_PROMPT,"id","root");
					report.reportHtml(getName() + " id:", ans, true);
				}
			}
		}
		else		
			report.report("session " + getName() + " is not connected!", Reporter.WARNING);
	}
	
	/**
	 * Gets the buffer the logger is using if it's enabled (loggedSession =
	 * true) <b>Important! after calling this method the buffer will clear
	 * itself.</b>
	 * 
	 * @return the logger buffer
	 */
	public String getLoggerBuffer() {
		// To avoid the synchronized method readInputBuffer() - use a thread
		// that will call it and wait for it instead.
		if (connected && (loggerBufferThread == null || !loggerBufferThread.isAlive())) {
			loggerBufferThread = new Thread(new Runnable() {
				@Override
				public void run() {
					readInputBuffer();
				}
			}, getName() + " log buffer thread");
			loggerBufferThread.start();
		}

		String buffer = loggerBuffer;
		loggerBuffer = "";
		return TerminalUtils.filterVT100(buffer);
	}

	private boolean verifyLogLevel(){
		String tableToVerify = enbComp.sendCommandsOnSession(name,EnodeBComponent.LTE_CLI_PROMPT, "logger threshold get", "");
//		String tableToPrint = "************************logger threshold get*************************\n";
//		tableToPrint += "EnodeB: "+enbComp.getIpAddress()+", session: "+getName()+ ", wanted log level: "+logLevel+"\n";
//		tableToPrint += tableToVerify;
//		tableToPrint += "*********************************************************************\n";
//		GeneralUtils.printToConsole(tableToPrint);
		int numOfMatches = 0;
		Pattern pattern = null;
		try{
			pattern = Pattern.compile("\\|\\|\\s*"+logLevel+"\\s*\\|");
		}catch(Exception e){
			e.printStackTrace();
		}
		Matcher match = null;
		if(pattern!=null){
			match = pattern.matcher(tableToVerify);			
		}
		if(match!=null){
			while(match.find()){
				numOfMatches++;
				if(numOfMatches>10){
					break;				
				}
			}			
		}
		return numOfMatches>10;
	}
	
	/**
	 * Updates the log level of the session to the value configured in the
	 * session.
	 * 
	 * @return true on success or false on failure
	 */
	public boolean updateLogLevel() {
		boolean verify = false;
		GeneralUtils.printToConsole("Setting Session " + getName() +" for EnodeB "+enbComp.getIpAddress()+ " log level to " + logLevel);
		if (connected && logLevel >= 0) {
			GeneralUtils.printToConsole("Setting log level");
			enbComp.setSessionLogLevel(name, logLevel);
			GeneralUtils.printToConsole("Verifying log level");
			verify = verifyLogLevel();
			if(!verify){					
				GeneralUtils.printToConsole("Failed to set log level. Setting log level again");
				GeneralUtils.unSafeSleep(10*1000);
				enbComp.setSessionLogLevel(name, logLevel);
				GeneralUtils.printToConsole("Verifying log level");
				verify = verifyLogLevel();
			}
		}
		else
			return false;
		
		if (verify) 
			GeneralUtils.printToConsole(getName() + " log level is at " + logLevel + " as wanted.");		
		else
			GeneralUtils.printToConsole("Failed setting " + getName() + " log level to " + logLevel);
		return verify;
	}

	/**
	 * Gets the buffer the cli is using if it's enabled (enableCliBuffer = true)
	 * <b>Important! after calling this method the buffer will clear itself.</b>
	 * 
	 * @return the cli buffer
	 */
	public synchronized String getCliBuffer() {
		readInputBuffer();
		String buffer = cliBuffer;
		cliBuffer = "";
		return buffer;
	}

	/**
	 * 
	 * @return the session's name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Closes the session
	 */
	public void close() {
		if (reconnect) {
			reconnect = false;
			disconnectSession();
		}
	}

	/**
	 * 
	 * @return true if the session is connected
	 */
	public boolean isConnected() {
		return connected;
	}

	/**
	 * 
	 * @return true if the logger is using this session to monitor the eNodeB
	 */
	public boolean isLoggedSession() {
		return loggedSession;
	}

	/**
	 * Enables or disables the logger buffer
	 * 
	 * @param loggedSession
	 */
	public void setLoggedSession(boolean loggedSession) {
		this.loggedSession = loggedSession;
	}

	/**
	 * @return true if the cli buffer is enabled
	 */
	public boolean isEnableCliBuffer() {
		return enableCliBuffer;
	}

	/**
	 * Enables or disables the logger buffer
	 * 
	 * @param enableCliBuffer
	 */
	public void setEnableCliBuffer(boolean enableCliBuffer) {
		this.enableCliBuffer = enableCliBuffer;
	}

	private boolean sendCommands(String cmd, String response) {
		privateBuffer = "";
		String ans = "";
		sendRawCommand(cmd);
		long startTime = System.currentTimeMillis(); // fetch starting time
		while ((System.currentTimeMillis() - startTime) < 3000) {
			readInputBuffer();
			ans += privateBuffer;
			if (ans.contains(response))
				return true;
			GeneralUtils.unSafeSleep(200);
		}

		return false;
	}
	
	public int getLogLevel(){
		if(logLevel == -1){
			return 3;
		}else{
			return logLevel;
		}
	}
	
	public boolean isShouldStayInCli() {
		return shouldStayInCli;
	}

	public void setShouldStayInCli(boolean shouldStayInCli) {
		this.shouldStayInCli = shouldStayInCli;
	}

	/**
	 * True if it was initialed
	 * @param sessionInitialized - sessionInitialized
	 */
	public void setSessionInitialized(boolean sessionInitialized) {
		isSessionInitialized = sessionInitialized;
	}

	/** isSessionInitialized getter
	 * @return - isSessionInitialized
	 */
	public boolean isSessionInitialized() {
		return isSessionInitialized;
	}
}
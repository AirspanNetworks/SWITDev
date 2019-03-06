package Utils.ConnectionManager.terminal;

import java.io.IOException;
import java.io.PrintStream;
//import java.util.logging.Level;
import java.util.ArrayList;

/**
 * Generic systemobject.Cli
 * 
 * @author Guy Arieli
 */
public class Cli {
	//private static Logger log = Logger.getLogger(Cli.class.getName());
	private static final String ENTER = "\r";
	
	
	private String enterStr = ENTER;
	
	
	protected Terminal terminal = null;
	
	
	private StringBuffer result = new StringBuffer();
	
	private Prompt resultPrompt;
	/**
	 * if set to true will send ENTER whan prompt wait timeout is recieved
	 */
	private boolean graceful = false;
	
	private long startTime = 0;
	
	/**
	 * Create a systemobject.terminal.Cli object
	 * 
	 * @param terminal The terminal to use, can be systemobject.terminal.Telnet or systemobject.terminal.RS232
	 *
	 * @exception IOException
	 */
	public Cli(Terminal terminal) throws IOException {
		this.terminal = terminal;
		terminal.connect();
	}
	
	
	public void addPrompt(Prompt prompt) {
		terminal.addPrompt(prompt);
	}
	
	
	public Prompt getPrompt(String prompt) {
		return terminal.getPrompt(prompt);
	}
	
	/**
	 * Sets the print stream to which the stream of the connection 
	 * will be dumped to.
	 * Set the print stream to System.out to dump terminal stream to the console,
	 * Set print stream to null to turn off stream dump.
	 */
	public void setPrintStream(PrintStream printStream){
		terminal.setPrintStream(printStream);
	}
		
	/**
	 * Login process.
	 * 
	 * @exception IOException
	 */
	
	public void login() throws Exception {
		login(60000);
	}
	
	
	public void login(long timeout) throws Exception {		
		login(timeout, false);		
	}
	
	
	public void login(long timeout, boolean delayedTyping) throws Exception {
		Thread.sleep(1000);
		command(null, timeout, true, delayedTyping);
	}
	
	
	/**
	 * Sends the command without waiting to any prompt.
	 * If delayedTyping is true, sends each byte desperately + small wait after each byte.
	 */
	public void sendString(String command,boolean delayedTyping) throws Exception {
		terminal.sendString(command, delayedTyping);
	}
	
	/**
	 * Send a command and wait for prompt.
	 * 
	 * @param command Command text.
	 * 
	 * @exception IOException
	 */
	public void command(String command) throws Exception {
		command(command, 20000, true, false);
	}
	
	
	
	/**
	 * Get the systemobject.terminal.Cli output text.
	 * 
	 * @return systemobject.terminal.Cli capture text.
	 */
	public String getResult() {
		String toReturn = result.toString();
		result = new StringBuffer();
		return toReturn;
	}
	/**
	 * Returns the prompt which identification triggered the termination
	 * of the cli operation.
	 */
	public Prompt getResultPrompt() {
		return resultPrompt;
	}

	
	
	/**
	 * Send a command and wait for prompt.
	 * 
	 * @param command  Command text.
	 * @param timeout  Command timeout in miliseconds
	 * @param addEnter If true "\r" will be add to the command.
	 * 
	 * @exception IOException
	 */
	public void command(String command, long timeout, boolean addEnter) throws Exception {
		command(command, timeout, addEnter, false);
	}	
	
	/**
	 * Send a command and wait for prompt.
	 * 
	 * @param command  Command text.
	 * @param timeout  Command timeout in miliseconds
	 * @param addEnter If true "\r" will be add to the command.
	 * 
	 * @exception IOException
	 */
	public void command(String command, long timeout, boolean addEnter, boolean delayedTyping) throws Exception {
		command(command, timeout, addEnter, delayedTyping, (String) null);
	}
	
	
	public void command(String command, long timeout, boolean addEnter, boolean delayedTyping, String promptString)
			throws Exception {
		if (promptString != null) {
			command(command, timeout, addEnter, delayedTyping, new String[] { promptString });
		} else {
			command(command, timeout, addEnter, delayedTyping, (String[]) null);
		}
		
	}
	
	
	public void command(String command, long timeout, boolean addEnter, boolean delayTyping, String[] promptStrings)
			throws Exception {
		command(command, timeout, addEnter, delayTyping, promptStrings, null);
	}
	
	public void command(String command, long timeout, boolean addEnter, boolean delayedTyping, String[] promptStrings,
			Prompt[] prompts) throws Exception {
		resultPrompt =null;
		ArrayList<Prompt> defaultPromts = null;
		if (prompts != null) {
			defaultPromts = terminal.getPrompts();
			terminal.removePrompts();
			for (int i = 0; i < prompts.length; i++) {
				terminal.addPrompt(prompts[i]);
			}
		}
		try {
			if (command != null) {
				if (addEnter) {
					command = command + getEnterStr(); //ENTER
				}
				//try {
				result.append(terminal.getResult());
				/*} catch (IOException e){
				 log.log(Level.WARNING,"Unable to clear buffer",e);
				 }*/
				terminal.sendString(command, delayedTyping);
			}
			startTime = System.currentTimeMillis();
			if (promptStrings != null) {
				terminal.waitForPrompt(promptStrings, timeout);
				result.append(terminal.getResult());
				return;
			}
			Prompt prompt = waitWithGrace(timeout);
			while (true) {
				if (timeout > 0) {
					if (System.currentTimeMillis() - startTime > (timeout)) {
						throw new IOException("timeout: " + timeout);
					}
				}
				/*
				 * If the scrallEnd property of the found prompt is set to true
				 * the check for terminal scrall end is skiped.
				 */
				if(prompt.isScrallEnd()){
					if (prompt.isCommandEnd()) {
						break;
					}
				} else {
					if (terminal.isScrallEnd()) {
						if (prompt.isCommandEnd()) {
							break;
						}
					} else {
						//System.err.println("Prompt found: " + prompt.getPrompt() +" but no command end");
						prompt = waitWithGrace(timeout);
						continue;
					}
				}
				String stringToSend = prompt.getStringToSend();
				if (stringToSend != null) {
					if (prompt.isAddEnter()) {
						stringToSend = stringToSend + getEnterStr(); //ENTER;
					}
					terminal.sendString(stringToSend, delayedTyping);
				}
				prompt = waitWithGrace(timeout);
			}
			
			resultPrompt = prompt;
		} finally {
			result.append(terminal.getResult());
			if (defaultPromts != null) {
				terminal.setPrompts(defaultPromts);
			}			
		}
	}
	
	
	
	/**
	 * Close the systemobject.terminal.Cli connection.
	 * 
	 * @exception IOException
	 */
	public void close() throws IOException {
		if (terminal != null && terminal.isConnected()) {
			terminal.disconnect();
		}
	}

	/**
	 * Reads the stream in the input buffer 
	 * and returns it as a String.
	 */
	public String read() throws Exception{
		return terminal.readInputBuffer();
	}

	/**
	 * connect the related terminal if it is not connected already
	 * @throws IOException
	 */
	public void connect() throws IOException {
		if (terminal != null && !terminal.isConnected()) {
			terminal.connect();
		}
	}
	
	/**
	 * disconnects the related terminal and then reconnects
	 * @throws IOException
	 */
	public void reconnect() throws IOException {
		close();
		connect();
	}
	
	/**
	 * checks if the current status of the terminal is "connected" or not
	 * @return true if the terminal is not null and connected
	 */
	public boolean isConnected() {
		if (terminal == null){
			return false;
		}
		return terminal.isConnected();
	}
	
	public String getEnterStr() {
		return enterStr;
	}
	
	
	public void setEnterStr(String enterStr) {
		this.enterStr = enterStr;
	}


	public boolean isGraceful() {
		return graceful;
	}


	public void setGraceful(boolean graceful) {
		this.graceful = graceful;
	}
	/**
	 * Check if working in graceful mode.
	 * If true will send ENTER if prompt wait fail
	 * @param timeout the timeout to wait
	 * @return the prompt
	 * @throws Exception
	 */
	private Prompt waitWithGrace(long timeout) throws Exception{
		try {
			Prompt p = terminal.waitForPrompt(timeout);
			result.append(terminal.getResult());
			return p;
		} catch (Exception e){
			if(!graceful){
				throw e;
			}
			
			return sendEnter(15 * 1000);
		}
			
	}
	
	/**
	 * sendEnter to terminal
	 * 
	 * @param timeout
	 * 				the prompt timeout
	 * @return	the prompt found
	 * @throws Exception
	 */
	private Prompt sendEnter(long timeout) throws Exception{
		startTime = System.currentTimeMillis();
		terminal.sendString(getEnterStr(), false);
		result.append(terminal.getResult());

		Prompt p = terminal.waitForPrompt(timeout);
		result.append(terminal.getResult());
		return p;
	}
		
}

package EnodeB.Components.Cli;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import Utils.GeneralUtils;


/**
 * Implementation of the cli prompt on the eNodeB.
 */
public class CliPrompt{
	
	/** Time to wait (in millis) before reading from buffer */
	public static final long BUFFER_WAIT_TIME = 100;
	
	/** The maximum time to wait for a response from the terminal */
	public static final long WAIT_FOR_RESPONSE_TIMEOUT = 2*1000;
	
	/**The delimiter between prompts in the scanning path string */
	public static final String SCANNING_PATH_DELIMITER = "->";
	
	/** The prompt. */
	private String prompt;
	
	/** The prompts that were scanned to get to this prompt. */
	private String scanningPath;
	
	/** The neighboring prompts. */
	private Hashtable<CliPrompt, String[]> neighbors;
	
	/** The cli. */
	private Cli cli;
	
	/** The buffer. */
	private String buffer;
	
	/**
	 * Instantiates a new cli prompt.
	 *
	 * @param prompt the prompt
	 */
	public CliPrompt(String prompt) {
		this.prompt = prompt;
		this.neighbors = new Hashtable<CliPrompt, String[]>();
		this.scanningPath = "";
		this.buffer = "";
	}
	
	/**
	 * Scans all prompts and executes the action in scanAction.
	 *
	 * @param scanAction the action that will be executed on each prompt the method scans
	 * @param args the args that are passed to the action.
	 * @return the cli prompt that the scan method needed to find.
	 */
	private CliPrompt scan(CliPromptAction scanAction, String... args) {
		if (scanAction.executeAction(this, args)) {
			scanningPath = "";
			return this;
		}
		CliPrompt[] availableNeighbors = getAvailableNeighbors();
		if (availableNeighbors != null) {
			for (CliPrompt neighbor : availableNeighbors) {
				neighbor.buffer = buffer;
				updateNeighborScanningPath(neighbor);
				CliPrompt scanResult = neighbor.scan(scanAction, args);
				scanningPath = "";
				buffer = neighbor.buffer;
				if (scanResult != null)
					return scanResult;
			}
		}

		scanningPath = "";
		return null;
	}

	/**
	 * Executes a set of commands on a target prompt.
	 *
	 * @param targetPrompt the prompt that the commands will be executed on.
	 * @param command the commands to execute
	 * @param responseTimeout - response Timeout
	 * @return the result of the commands.
	 * @throws Exception 
	 */
	public String execute(String targetPrompt, String command, String response, int responseTimeout) throws Exception {
		flushBuffer();
		sendNewLine();
		getBuffer();
		if (targetPrompt.equals(prompt) && isActivePrompt()) {
			//GeneralUtils.printToConsole(this + ": is the current active prompt.");
			flushBuffer();
			sendCommand(command);
			getBuffer();
			if (response != "" && response!=null) {
				GeneralUtils.printToConsole("Waiting timeout of " + responseTimeout + " seconds for the response.");
				long endTimeMillis = System.currentTimeMillis() + responseTimeout * 1000;
				while (true){
					if (buffer.contains(response)) {
						break;
					}
					else {
						if (System.currentTimeMillis() > endTimeMillis) {
							GeneralUtils.printToConsole("Cannot find response: " + response + " to command: " + command);
				            break;
						}
					}
					sendNewLine();
					getBuffer();
					GeneralUtils.unSafeSleep(1000);
				}
			}
			this.buffer = this.buffer.replaceAll(".*"+command, "");
			long executeTimeout = WAIT_FOR_RESPONSE_TIMEOUT;
			while (executeTimeout > 0 && !isBufferEndsWithKnownPrompt()) {
				executeTimeout -= BUFFER_WAIT_TIME;
			}
			if (executeTimeout <= 0) 
				GeneralUtils.printToConsole(this + ": Reached wait timeout, sending the result gathered so far.\n");
			return this.buffer;
		}
		else if (isActivePrompt()) {
			//GeneralUtils.printToConsole(this + ": Is the active prompt. Navigating to target " + targetPrompt);
			CliPrompt target = navigateToPrompt(targetPrompt);
			if (target != null)
				return target.execute(targetPrompt, command, response, responseTimeout);
		}
		else {
			//GeneralUtils.printToConsole(this + ": This is not the active prompt. Searching for the active prompt.");
			CliPrompt activePrompt = getActivePrompt();
			if (activePrompt != null){
				return activePrompt.execute(targetPrompt, command, response, responseTimeout);
			}
		}
		return flushBuffer();
	}
	
	private String flushBuffer() throws Exception {
		String buffer = getBuffer();
		clearBuffer();
		return buffer;
	}

	/**
	 * Finds a specific prompt.
	 *
	 * @param prompt that need to be found
	 * @return the prompt.
	 */
	public CliPrompt findPrompt(String prompt) {
		if (prompt.equals(this.prompt))
			return this;
		
		return scan(new CliPromptAction() {
			
			@Override
			public boolean executeAction(CliPrompt prompt, String[] args) {
				return args[0].equals(prompt.prompt);
			}
		}, prompt);
	}
	
	/**
	 * Gets the active prompt.
	 *
	 * @return the active prompt
	 * @throws Exception when active prompt not found
	 */
	private CliPrompt getActivePrompt() throws Exception  {
		// Check if session is connected
		if (!getCli().getSession().isConnected()) {
			GeneralUtils.printToConsole(this + ": getActivePrompt - Session is disconnected. Returning null");
			Thread.dumpStack();
			return null;
		}
		
		if (isActivePrompt())
			return this;
		
		CliPromptAction getActiveAction = new CliPromptAction() {
			
			@Override
			public boolean executeAction(CliPrompt prompt, String[] args) {
				try {
					return prompt.isActivePrompt();
				} catch (Exception e) {
					return false;
				}
			}
		};
		CliPrompt activePrompt = scan(getActiveAction, "");
		
		if (activePrompt == null) {
			GeneralUtils.printToConsole(this + ": Active prompt not found. Trying again.");

			sendNewLine();
			getBuffer();
		    activePrompt = scan(getActiveAction, "");
			if (activePrompt == null) 
			{
				sendNewLine();
				getBuffer();
				GeneralUtils.printToConsole(this + ": Active prompt not found!\nbuffer:\n" + buffer);
			}
		}
		
		if (activePrompt != null)	
			cli.setCurrentPrompt(activePrompt);
		
		return activePrompt;
	}

	/**
	 * Navigates to a specific prompt.
	 *
	 * @param targetPrompt the prompt that will become active.
	 * @return the target prompt
	 */
	private CliPrompt navigateToPrompt(String targetPrompt) {
		if (targetPrompt.equals(prompt))
			return this;
		
		CliPrompt target = scan(new CliPromptAction() {
			
			@Override
			public boolean executeAction(CliPrompt prompt, String[] args) {
				try {
					//System.out.printf("[%s]: entering navigation method\n", prompt);
					String targetPrompt = args[0];
					if (prompt.getPrompt().equals(targetPrompt)) {
						CliPrompt[] pathPrompts = prompt.getPromptsFromScanningPath();
						if (pathPrompts != null) {
							//System.out.printf("[%s]: Navigating to prompt through: %s\n", prompt, prompt.scanningPath);
							for (int pathIndex = 0; pathIndex < pathPrompts.length; pathIndex++) {
								CliPrompt pathPrompt = pathPrompts[pathIndex];
								String[] commands = null;
								if (pathIndex < pathPrompts.length - 1)
									commands = pathPrompt.neighbors.get(pathPrompts[pathIndex + 1]);
								else
									commands = pathPrompt.neighbors.get(prompt);
								
								for (String command : commands) {
									prompt.sendCommand(command);
									GeneralUtils.unSafeSleep(500);
								}
							}
							
							if (prompt.getActivePrompt() == prompt)
								return true;
							else {
								GeneralUtils.unSafeSleep(WAIT_FOR_RESPONSE_TIMEOUT);
								return prompt.isActivePrompt();
							}
						}
					}
				} catch (Exception e) {
					GeneralUtils.printToConsole(this + ":  Found exception on navigate to prompt method. \nException: " + e.getMessage() + "\nBuffer:%s\n" + buffer);
					
				}
				return false;
			}
		}, targetPrompt);
	
		
		if (target == null)
			GeneralUtils.printToConsole(this + ": Failed to navigate to \"" + targetPrompt + "\"\nbuffer:\n" + buffer);
		else
			cli.setCurrentPrompt(target);
		
		return target;
	}
	
	/**
	 * Gets the prompts from scanning path.
	 *
	 * @return the prompts from scanning path
	 */
	private CliPrompt[] getPromptsFromScanningPath() {
		//System.out.printf("[%s]: Getting prompts from path: %s\n", this, scanningPath);
		String[] stringPrompts = scanningPath.split(SCANNING_PATH_DELIMITER);
		CliPrompt[] prompts = new CliPrompt[stringPrompts.length];
		for (int promptIndex = prompts.length - 1; promptIndex >= 0; promptIndex--) {
			if (promptIndex == (prompts.length - 1)) 
				prompts[promptIndex] = getNeighborByPrompt(stringPrompts[promptIndex]);
			else
				prompts[promptIndex] = prompts[promptIndex + 1].getNeighborByPrompt(stringPrompts[promptIndex]);
		}
		return prompts;
	}

	/**
	 * Gets the neighbor by prompt.
	 *
	 * @param prompt the prompt
	 * @return the neighbor by prompt
	 */
	private CliPrompt getNeighborByPrompt(String prompt) {
		Enumeration<CliPrompt> neighborPrompts = neighbors.keys();
		while (neighborPrompts.hasMoreElements()) {
			CliPrompt neighbor = (CliPrompt) neighborPrompts.nextElement();
			if (neighbor.prompt.equals(prompt))
				return neighbor;	
		}
		return null;
	}

	/**
	 * Checks if the buffer ends with known prompt.
	 *
	 * @return true, if the buffer ends with known prompt
	 * @throws Exception 
	 */
	private boolean isBufferEndsWithKnownPrompt() throws Exception {
		if (isActivePrompt() || getActivePrompt() != null)
			return true;
		else
			return false;
	}
	
	/**
	 * Checks if this prompt is the active prompt.
	 *
	 * @return true, if the current is the active prompt
	 * @throws Exception 
	 */
	private boolean isActivePrompt() throws Exception {
		String buffer = getBuffer();
		if (buffer.isEmpty() || !buffer.contains(prompt)){
			sendNewLine();
			buffer = getBuffer();
		}
		return buffer.contains(prompt);
	}
	
	private void sendNewLine() throws IOException, InterruptedException {
		sendCommand("\n");
	}
	
	/**
	 * Send commands to the terminal.
	 *
	 * @param command the command
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	private void sendCommand(String command) {
		if (cli == null) {
			GeneralUtils.printToConsole(toString() + ": The cli is null. can't execute command.");
			return;
		}
		if (cli.getSession().isConnected()) {
			try {
				cli.getSession().sshConnectionLock.acquire();
				try {
					cli.getSession().sendRawCommand(command);
				} finally {
					cli.getSession().sshConnectionLock.release();
				}
			} catch (InterruptedException e) {
				GeneralUtils.printToConsole("Failed getting semaphore to send command" + command);
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Update neighbor's scanning path.
	 *
	 * @param neighbor the neighbor to update
	 */
	private void updateNeighborScanningPath(CliPrompt neighbor) {
		if (scanningPath.isEmpty())
			neighbor.scanningPath = prompt;
		else
		neighbor.scanningPath = scanningPath + SCANNING_PATH_DELIMITER + prompt;
	}
	
	
	/**
	 * Gets the available neighbors for scan.
	 *
	 * @return the available neighbors
	 */
	private CliPrompt[] getAvailableNeighbors() {
		 Enumeration<CliPrompt> neighborsKeys = neighbors.keys();
		 ArrayList<CliPrompt> avilablePrompts = new ArrayList<CliPrompt>();
		 while (neighborsKeys.hasMoreElements()) {
			CliPrompt prompt = (CliPrompt) neighborsKeys.nextElement();
			if (!this.scanningPath.contains(prompt.prompt))
				avilablePrompts.add(prompt);
		}
		 
		 if (avilablePrompts.size() == 0)
			 return null;
		 else
			 return avilablePrompts.toArray(new CliPrompt[]{null});
	 }
	
	/**
	 * Adds a neighboring prompt.
	 *
	 * @param prompt the prompt
	 * @param commandsTo the commands to get to the neighbor.
	 * @param commandsFrom the commands to get from the neighbor to the current prompt.
	 * @return the neighboring prompt
	 */
	public CliPrompt addNeighboringPrompt(String prompt, String[] commandsTo, String[] commandsFrom) {
		CliPrompt p = findPrompt(prompt);
		if (p == null)
			p = new CliPrompt(prompt);
		return addNeighboringPrompt(p, commandsTo, commandsFrom);
	}
	
	/**
	 * Adds a neighboring prompt.
	 *
	 * @param prompt the prompt
	 * @param commandsTo the commands to get to the neighbor.
	 * @param commandsFrom the commands to get from the neighbor to the current prompt.
	 * @return the neighboring prompt
	 */
	public CliPrompt addNeighboringPrompt(CliPrompt prompt, String[] commandsTo, String[] commandsFrom) {
		neighbors.put(prompt, commandsTo);
		prompt.neighbors.put(this, commandsFrom);
		prompt.setCli(cli);
		return prompt;
	}
	
	/**
	 * Cleans the buffer.
	 */
	public void clearBuffer() {
		buffer = "";
	}
	
	/**
	 * Gets the buffer.
	 *
	 * @return the buffer
	 * @throws Exception 
	 */
	public String getBuffer() {
		GeneralUtils.unSafeSleep(BUFFER_WAIT_TIME);
		buffer += cli.getSession().getCliBuffer();
		return buffer;
	}
	
	/**
	 * Gets the prompt.
	 *
	 * @return the prompt
	 */
	public String getPrompt() {
		return prompt;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String output = "";
		if (getCli() != null)
			output = getCli().getName() + ".";
		return output  + "Prompt." + prompt;
	}
	
	/**
	 * Gets the cli.
	 *
	 * @return the cli
	 */
	public Cli getCli() {
		return cli;
	}
	
	/**
	 * Sets the cli.
	 *
	 * @param cli the new cli
	 */
	public void setCli(Cli cli) {
		this.cli = cli;
	}
}

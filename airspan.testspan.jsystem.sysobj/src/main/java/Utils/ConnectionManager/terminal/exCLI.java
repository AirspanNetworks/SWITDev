package Utils.ConnectionManager.terminal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import javax.naming.TimeLimitExceededException;

import Utils.ConnectionManager.UserInfo.UserSequence;
import systemobject.terminal.Cli;
import systemobject.terminal.Terminal;
import systemobject.terminal.Prompt;


//import Utils.ConnectionManager.terminal.Cli;
//import Utils.ConnectionManager.terminal.Prompt;
//import Utils.ConnectionManager.terminal.Terminal;

/**
 * 
 * @author doguz
 * This class extend basic Top-Q Cli for:
 * - Output extract from sent command to final prompt
 * - Modified command method
 * - Added: method switchPrompt (for support smooth switch to lteCli mode and back)
 */
public class exCLI extends Cli {
	
	
	static final String DEFAULT_CACHE_PREFIX = "telnet_";
	static final String DEFAULT_CACHE_EXT = ".log";
	static final String DEFAULT_UNPRINTABLE = "\\[[0-9;m]+";
	
	private String cache_prefix = "";
	private exPrompt resultPrompt = null;
	
	public exCLI(Terminal terminal) throws IOException {
		this(terminal, DEFAULT_CACHE_PREFIX);
	}
	
	public exCLI(Terminal terminal, String cache_prefix) throws IOException {
		super(terminal);
		this.cache_prefix = cache_prefix;
	}
	
	private String readFile(String file, String... clearPatterns) throws IOException {
	    BufferedReader reader = new BufferedReader(new FileReader (file));
	    String         line = null;
	    StringBuilder  stringBuilder = new StringBuilder();
	    String         ls = System.getProperty("line.separator");
	    
	    if(clearPatterns.length == 0) {
	    	clearPatterns = new String[] {DEFAULT_UNPRINTABLE};
	    }
	    
	    try {
	        while((line = reader.readLine()) != null) {
	            stringBuilder.append(line);
	            stringBuilder.append(ls);
	        }
	        	
	        String temp_result = stringBuilder.toString();
	        
	        for(String pattern : clearPatterns) {
	        	temp_result = temp_result.replaceAll(pattern, "");
	        }
	        
	        return temp_result;
	    } finally {
	        reader.close();
	    }
	}
	
	private void setPrintStream(String file_path) throws IOException {
		File temp_log = new File(file_path);
		
		if(!temp_log.exists()) {
			temp_log.createNewFile();
		}
		
		super.setPrintStream(new PrintStream(temp_log));
	}
	
	/**
	 * 
	 * @param command        : Command to be send
	 * @param timeout		 : Time period for wait output print out (Too short timeout can affect reading command output
	 * @param addEnter		 : Add enter after command
	 * @param delayedTyping  : Delay taping flag
	 * @return               : Command output
	 * @throws Exception
	 */
	public String exec_command(String command, long timeout, boolean addEnter, boolean delayedTyping) throws Exception {
		getResult();
		File log_cache = File.createTempFile(cache_prefix, DEFAULT_CACHE_EXT);
		setPrintStream(log_cache.getAbsolutePath());
		
		command(command + "\n", timeout, true, delayedTyping);
		
		Thread.sleep(100);
		long startTime = System.currentTimeMillis();
		do {
//			try{ 
//				
//				resultPrompt = terminal.waitFor();
//				break;			
//			} finally {
				Thread.sleep(50);
//			}
			
		}while (System.currentTimeMillis() - startTime < timeout);
			
		return readFile(log_cache.getAbsolutePath());
	}

	public exPrompt waitWithGrace(long timeout) throws Exception {
		return waitWithGrace(timeout, 3);
	}

	/**
	 *
	 * @return
	 * @throws Exception
	 */
	public exPrompt waitWithGrace(long timeout, int counter) throws Exception {
		exPrompt p;
//		do {
			try {
//				exPrompt p = (exPrompt) terminal.waitForPrompt(timeout);
				p = (exPrompt) terminal.waitForPrompt(Math.min(15 * 1000, timeout));
			} catch (Exception e) {
				if (!isGraceful())
					throw e;

				p = sendEnter(Math.min(15 * 1000, timeout));
			}
//			finally {
//				counter--;
//			}
//		}while (p == null && counter >= 0);

		if(p == null)
			throw new IOException("exCLI: Cannot occur prompt");

		return p;
	}
	
	public exPrompt sendEnter(long timeout) throws Exception {
		long startTime = System.currentTimeMillis();
		terminal.sendString(getEnterStr(), false);
//		result.append(terminal.getResult());

		Prompt p = terminal.waitForPrompt(timeout);
		p.setCommandEnd(p.getStringToSend() == null);
		
//		result.append(terminal.getResult());
		return (exPrompt)p;
	}
	
	
	/**
	 * Method replace original prompt 
	 *  Reason are replace all existing prompts with desired set; After login complete, old prompts restored back 
	 * @param timeout
	 * @param prompts
	 * @throws Exception
	 */
	public void login(long timeout, Prompt... prompts) throws Exception {
		List<Prompt> defaultPrompts = terminal.getPrompts();
		removePrompts();
		addPrompts(prompts);
		try {
//			sendEnter(1000);
			super.login(timeout, true);
		}
		finally {
			terminal.removePrompts();
			addPrompts(defaultPrompts);
		}
	}
	
	/**
	 * Method extend login ability for perform subsequence login options for sudo and lte_cli
	 * @param timeout
	 * @param prompts
	 * @return
	 * @throws Exception
	 */
	public boolean login(long timeout, UserSequence prompts) throws Exception {
		
		login(timeout, prompts.toArray(new exPrompt[] {}));
		
		UserSequence instance = prompts;
		while(instance.hasNext()) {
			instance = instance.next();
			login(timeout, instance.toArray(new exPrompt[] {}));
		}
		
		exPrompt current_pr = waitWithGrace(timeout);
		if(current_pr.getPrompt() != prompts.getFinalPrompt().getPrompt()) {
			return false;
		}
		return true;
	}
	
	/**
	 * Refresh session. results buffer
	 * @throws IOException
	 * @throws TimeLimitExceededException
	 * @throws InterruptedException
	 */
	public void refreshSession() throws IOException, TimeLimitExceededException, InterruptedException {
		ArrayList<Prompt> defaultPrompts = new ArrayList<Prompt>();
		try {
			defaultPrompts = terminal.getPrompts();
//			StringBuffer result = new StringBuffer();
			terminal.removePrompts();
			terminal.disconnect();
			terminal.connect();
		}
		finally {
			addPrompts(defaultPrompts.toArray(new Prompt[] {}));
			resultPrompt = (exPrompt)terminal.waitFor();
		}
	}
	
	public void removePrompts() {
		terminal.removePrompts();
	}
	
	public void addPrompts(Prompt...prompts) {
		for(Prompt prompt : prompts) {
			addPrompt(prompt);
		}
	}
	
	public void addPrompts(List<Prompt> prompts) {
		addPrompts(prompts.toArray(new Prompt[] {}));
	}
}

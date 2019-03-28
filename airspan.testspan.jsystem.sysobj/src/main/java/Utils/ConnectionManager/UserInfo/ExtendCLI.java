package Utils.ConnectionManager.UserInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.regex.Pattern;

import javax.naming.TimeLimitExceededException;

import org.apache.tools.ant.taskdefs.WaitFor;

import Utils.ConnectionManager.terminal.Cli;
import Utils.ConnectionManager.terminal.Prompt;
import Utils.ConnectionManager.terminal.Terminal;
import jsystem.framework.report.Reporter;

/**
 * 
 * @author doguz
 * This class extend basic Top-Q Cli for:
 * - Output extract from sent command to final prompt
 * - Modified command method
 * - Added: method switchPrompt (for support smooth switch to lteCli mode and back)
 */
public class ExtendCLI extends Cli {
	
	
	static final String DEFAULT_CACHE_PREFIX = "telnet_";
	static final String DEFAULT_CACHE_EXT = ".log";
	static final String DEFAULT_UNPRINTABLE = "\\[[0-9;m]+";
	private String cache_prefix = "";
	
	public ExtendCLI(Terminal terminal) throws IOException {
		this(terminal, DEFAULT_CACHE_PREFIX);
	}
	
	public ExtendCLI(Terminal terminal, String cache_prefix) throws IOException {
		super(terminal);
		this.cache_prefix = cache_prefix;
	}
	
	
//	/**
//	 * Extend standart Cli
//	 * @author doguz
//	 * @param output
//	 * @param command
//	 * @param prompt
//	 * @param delimiters
//	 * @return
//	 */
//	public String getCommandOutput(String output, String command, String prompt, String[] delimiters) {
//		
//		String defaultDelimiter = "\n";
//		
//		for(String delimiter : delimiters){
//			output = output.replace(delimiter, defaultDelimiter);	
//		}
//		
//		List<String> lines = Arrays.asList(output.split(defaultDelimiter));
//		Queue<String> line_cache = new LinkedList<String>(lines);
//		List<String> temp = new ArrayList<String>();
//		Collections.reverse(lines); 
//		String line = "";
//		
//		while(line_cache.size() > 0 || line.endsWith(command)) {
//			line = line_cache.poll();
//			if(line.length() > 0)
//				temp.add(line);	
//		}
//		
//		String result = "";
//		for(String t : temp) {
//			result += t + defaultDelimiter;
//		}
//		
//		return result;
//	}
//	
//	public String getCommandOutput(String output, String command, String prompt){
//		return this.getCommandOutput(output, command, prompt, new String[] {"\n"});
//	}
	
//	/**
//	 * 
//	 * @param timeout
//	 * @param prompt
//	 * @throws Exception
//	 */
//	public void login(long timeout, IPrompt prompt) throws Exception {
//		login(timeout, prompt, 0);
//	}
//	
//	/**
//	 * 
//	 * @param timeout
//	 * @param prompt
//	 * @param retry
//	 * @throws Exception
//	 */
//	public void login(long timeout, IPrompt prompt, int retry) throws Exception {
////		ArrayList<IPrompt> defaultPromts = terminal.prompts;
//		IPrompt runtime_prompt = prompt;
//		
////		int login_timout = 1000;
//		try {
//
////			defaultPromts = terminal.getPrompts();
//			terminal.removePrompts();
//			startTime = System.currentTimeMillis();
//			
//			
//			IPrompt temp = null;
//			
//			do {
//				IPrompt next_prompt = ((UserStatementAbstract)runtime_prompt).getSiblingPrompt();
//				addPrompt(next_prompt);
//				if (runtime_prompt.getStringToSend() != null) {
//					String stringToSend = runtime_prompt.getStringToSend();
//					if (stringToSend != null) {
//						if (runtime_prompt.isAddEnter()) {
//							stringToSend = stringToSend + getEnterStr(); //ENTER;
//						}
//						Thread.sleep(50);
//						sendString(stringToSend, true);
//						Thread.sleep(50);
//						IPrompt current_prompt = waitWithGrace(timeout);
//						int retry_count = retry;
//						
//						 do{
//							if(current_prompt.getPrompt() != next_prompt.getPrompt()) {
//								Thread.sleep(50);
//								current_prompt = waitWithGrace(timeout);
////								throw new IOException("Wrong login/logout sequence; Prompt '" + current_prompt.getPrompt() + " got instead of '" + next_prompt.getPrompt() + "'");
//							}
//							retry_count--;
//						}while(retry_count > 0);
//						
//						runtime_prompt = next_prompt;
//					}
//				}
//				removePrompts();
//			}while(runtime_prompt instanceof UserStatementAbstract);
//			addPrompt(runtime_prompt);
//			
////			while (runtime_prompt instanceof UserStatementAbstract) {
////				
////				IPrompt next_prompt = ((UserStatementAbstract)runtime_prompt).getSiblingPrompt();
////				addPrompt(next_prompt);
////				if (runtime_prompt.getStringToSend() != null) {
////					String stringToSend = runtime_prompt.getStringToSend();
////					if (stringToSend != null) {
////						if (runtime_prompt.isAddEnter()) {
////							stringToSend = stringToSend + getEnterStr(); //ENTER;
////						}
////						Thread.sleep(50);
////						sendString(stringToSend, true);
////						Thread.sleep(50);
////						IPrompt current_prompt = waitWithGrace(timeout);
////						int retry_count = retry;
////						
////						 do{
////							if(current_prompt.getPrompt() != next_prompt.getPrompt()) {
////								Thread.sleep(50);
////								current_prompt = waitWithGrace(timeout);
////							}
////							retry_count--;
////						}while(retry_count > 0);
////						
////						runtime_prompt = next_prompt;
////					}
////				}
////				removePrompts();
////			}
////			addPrompt(runtime_prompt);
//			
//		} finally {
////			if (defaultPromts != null) {
////				removePrompts();
////				addPrompts(defaultPromts.toArray(new IPrompt[0]));
////			}			
////			this.sendString("\n", true);
//			
//		}	
//		
//	}
	
//	@Override
//	protected IPrompt waitWithGrace(long timeout) throws Exception {
//		
//		startTime = System.currentTimeMillis();
//		do {
//			try {
//				IPrompt p = terminal.waitForPrompt(100);
//				result.append(terminal.getResult());
//				return p;
//			} catch (TimeLimitExceededException e){
//				refreshSession();
//			}
//			
//		}while(System.currentTimeMillis() - startTime > timeout);
//		
//		return null;
//	}
	
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
	
	public String exec_command(String command, long timeout, boolean addEnter, boolean delayedTyping) throws Exception {
		getResult();
		File log_cache = File.createTempFile(cache_prefix, DEFAULT_CACHE_EXT);
		setPrintStream(log_cache.getAbsolutePath());
		
		sendString(command + "\n", delayedTyping);
		
		Thread.sleep(100);
		long startTime = System.currentTimeMillis();
		do {
			try{ 
				resultPrompt = terminal.waitFor();
				break;			
			} finally {
				Thread.sleep(50);
			}
			
		}while (System.currentTimeMillis() - startTime < timeout);
			
		return readFile(log_cache.getAbsolutePath());
	}
		
	public Prompt getCurrentPrompt() throws Exception {
		if(resultPrompt == null) {
			long startTime = System.currentTimeMillis();
			long timeout = 1000;
			do {
				try {
					this.resultPrompt = waitWithGrace(1000);
					break;
				}
				catch (TimeLimitExceededException e) {
					refreshSession();
				}
			}while (System.currentTimeMillis() - startTime > timeout);
		}
		return resultPrompt;
		
	}

	public void login(long timeout, Prompt... prompts) throws Exception {
		List<Prompt> defaultPrompts = terminal.getPrompts();
		terminal.removePrompts();
		addPrompts(prompts);
		try {
			super.login(timeout);
		}
		finally {
			terminal.removePrompts();
			addPrompts(defaultPrompts);
		}
	}
	
	public boolean login(long timeout, UserSequence prompts) throws Exception {
		
		login(timeout, prompts.toArray(new Prompt[] {}));
		
		if(prompts.siblingPrompts() != null) {
			for(UserSequence u_seq : prompts.siblingPrompts()) {
				login(timeout, u_seq);
			}
		}
		
		Prompt current_pr = getCurrentPrompt();;
		if(current_pr.getPrompt() != prompts.getFinalPrompt().getPrompt()) {
			return false;
		}
		return true;
	}
	

	public void refreshSession() throws IOException, TimeLimitExceededException, InterruptedException {
		ArrayList<Prompt> defaultPrompts = new ArrayList<Prompt>();
		try {
			defaultPrompts = terminal.getPrompts();
			result = new StringBuffer();
			terminal.removePrompts();
			terminal.disconnect();
			terminal.connect();
		}
		finally {
			addPrompts(defaultPrompts.toArray(new Prompt[] {}));
			resultPrompt = terminal.waitFor();
		}
	}
	
	public void removePrompts() {
		terminal.removePrompts();
	}
	
	@Override
	public void close() throws IOException {
		super.close();
	}
	
	public void addPrompts(Prompt...prompts) {
		for(Prompt prompt : prompts) {
			addPrompt(prompt);
		}
	}
	
	public void addPrompts(List<Prompt> prompts) {
		for(Prompt prompt : prompts) {
			addPrompt(prompt);
		}
	}
}

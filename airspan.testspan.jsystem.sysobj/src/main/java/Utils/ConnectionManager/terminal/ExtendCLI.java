package Utils.ConnectionManager.terminal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOError;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

import org.apache.tools.ant.util.regexp.Regexp;
import org.glassfish.tyrus.core.SendCompletionAdapter;
import org.hamcrest.core.IsInstanceOf;

/**
 * 
 * @author doguz
 * This class extend basic Top-Q Cli for:
 * - Output extract from sent command to final prompt
 * - Modified command method
 * - Added: method switchPrompt (for support smooth switch to lteCli mode and back)
 */
public class ExtendCLI extends Cli {
	
	private String log_file_path = "";
	private File log_cache = null;
	static final String DEFAULT_CACHE_FILENAME = "session.log";
	static final String DEFAULT_CACHE_FILEDIR = "tmp";
	static final String DEFAULT_UNPRINTABLE = "\\[[0-9;m]+";
	
//	private IPrompt sessionPrompt = null;
	
	public ExtendCLI(Terminal terminal) throws IOException {
		this(terminal, System.getProperty("user.home") + File.separator + DEFAULT_CACHE_FILEDIR + File.separator + DEFAULT_CACHE_FILENAME);
	}
	
	public ExtendCLI(Terminal terminal, String cache_file_path) throws IOException {
		super(terminal);
		create_cache_file(cache_file_path);
	}
	
	private void create_cache_file(String file_path) throws IOException {
		File temp_log = new File(file_path);
				
		if(temp_log.exists()) {
			temp_log.delete();
		}
		temp_log.createNewFile();
		log_cache = temp_log;
		log_file_path = file_path;
	}
	
	/**
	 * Extend standart Cli
	 * @author doguz
	 * @param output
	 * @param command
	 * @param prompt
	 * @param delimiters
	 * @return
	 */
	public String getCommandOutput(String output, String command, String prompt, String[] delimiters) {
		
		String defaultDelimiter = "\n";
		
		for(String delimiter : delimiters){
			output = output.replace(delimiter, defaultDelimiter);	
		}
		
		List<String> lines = Arrays.asList(output.split(defaultDelimiter));
		Queue<String> line_cache = new LinkedList<String>(lines);
		List<String> temp = new ArrayList<String>();
		Collections.reverse(lines); 
		String line = "";
		
		while(line_cache.size() > 0 || line.endsWith(command)) {
			line = line_cache.poll();
			if(line.length() > 0)
				temp.add(line);	
		}
		
		String result = "";
		for(String t : temp) {
			result += t + defaultDelimiter;
		}
		
		return result;
	}
	
	public String getCommandOutput(String output, String command, String prompt){
		return this.getCommandOutput(output, command, prompt, new String[] {"\n"});
	}
	
	/**
	 * 
	 * @param timeout
	 * @param prompt
	 * @throws Exception
	 */
	public void login(long timeout, IPrompt prompt) throws Exception {
		login(timeout, prompt, 0);
	}
	
	/**
	 * 
	 * @param timeout
	 * @param prompt
	 * @param retry
	 * @throws Exception
	 */
	public void login(long timeout, IPrompt prompt, int retry) throws Exception {
		ArrayList<IPrompt> defaultPromts = null;
		IPrompt runtime_prompt = prompt;
		
		try {

			defaultPromts = terminal.getPrompts();
			terminal.removePrompts();
			startTime = System.currentTimeMillis();
			
			addPrompt(runtime_prompt);
			
			sendString("\n", true);
			
			while (runtime_prompt instanceof LinkedPrompt) {
				
				IPrompt next_prompt = ((LinkedPrompt)runtime_prompt).getLinkedPrompt();
				
				if (runtime_prompt.getStringToSend() != null) {
					String stringToSend = runtime_prompt.getStringToSend();
					if (stringToSend != null) {
						if (runtime_prompt.isAddEnter()) {
							stringToSend = stringToSend + getEnterStr(); //ENTER;
						}
						Thread.sleep(50);
						sendString(stringToSend, true);
						Thread.sleep(50);
						IPrompt current_prompt = waitWithGrace(timeout);
						
						while(retry > 0) {
							if(current_prompt.getPrompt() != next_prompt.getPrompt()) {
								Thread.sleep(200);
								current_prompt = waitWithGrace(timeout);
//								throw new IOException("Wrong login/logout sequence; Prompt '" + current_prompt.getPrompt() + " got instead of '" + next_prompt.getPrompt() + "'");
							}
							retry--;
						}
						
						runtime_prompt = next_prompt;
					}
				}
				terminal.removePrompts();
				addPrompt(next_prompt);
			}
			
			defaultPromts.add(runtime_prompt);
			
		} finally {
//			defaultPromts.add(prompt);
			result.append(terminal.getResult());
			if (defaultPromts != null) {
				terminal.removePrompts();
				addPrompts(defaultPromts.toArray(new IPrompt[0]));
			}			
			sendString("\n", true);
		}	
		
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
	
	public String exec_command(String command, long timeout, boolean addEnter, boolean delayedTyping) throws Exception {
		getResult();
		create_cache_file(log_file_path);
		setPrintStream(log_file_path);
		
		long startTime = System.currentTimeMillis();
		
		command(command, timeout, addEnter, delayedTyping, getCurrentPrompt().getPrompt());
		Thread.sleep(200);
		
		while (System.currentTimeMillis() - startTime < timeout) {
			resultPrompt = waitWithGrace(timeout);
			Thread.sleep(1000);
		}
		return readFile(log_file_path);
	}
	
	public IPrompt getCurrentPrompt() throws Exception {
		if(resultPrompt == null)
			sendString("\n", true);
			resultPrompt = waitWithGrace(500);
		return resultPrompt;
	}
	
	public IPrompt getCurrentPrompt(long timeout) throws Exception {
		return this.waitWithGrace(timeout);
	}

	public void resetToPrompt(IPrompt prompt) throws Exception{
		long timeout = 0;
		login(timeout, prompt);
	}
	
	@Override
	public void close() throws IOException {
		super.close();
		if(log_cache != null) {
			log_cache.delete();
		}
	}
}

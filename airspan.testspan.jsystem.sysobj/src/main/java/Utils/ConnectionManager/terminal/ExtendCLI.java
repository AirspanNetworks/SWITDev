package Utils.ConnectionManager.terminal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.apache.tools.ant.util.regexp.Regexp;

/**
 * 
 * @author doguz
 * This class extend basic Top-Q Cli for:
 * - Output extract from sent command to final prompt
 * - Modified command method
 * - Added: method switchPrompt (for support smooth switch to lteCli mode and back)
 */
public class ExtendCLI extends Cli {
	
	private boolean outputExpected = true;
	
	private IPrompt sessionPrompt = null;
	
	public ExtendCLI(Terminal terminal) throws IOException {
		super(terminal);
		outputExpected = true;
		// TODO Auto-generated constructor stub
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
	public List<String> getResult(String command, String prompt, String[] delimiters) {
	
		String output = super.getResult();
		
		output = output.replaceAll("\\[[0-9;m]+", "");
		
		String defaultDelimiter = "\n";
		List<String> result = new ArrayList<String>();
		boolean collectFlag = false;
		
		for(String delimiter : delimiters){
			output = output.replace(delimiter, defaultDelimiter);	
		}
		
		List<String> lines = Arrays.asList(output.split(defaultDelimiter));
		
		for(String line : lines) {
			
			if(collectFlag && line.length() > 0)
				result.add(line.replaceAll("\\[[0-9;m]+", ""));
			
			if(line.endsWith(command))
				collectFlag = true;
			else if(line == prompt){
				collectFlag = false;
			}			
		}
		
		return result;
	}
	
	public List<String> getResult(String command, String prompt){
		return this.getResult(command, prompt, new String[] {"\n"});
	}
	
	public void login(long timeout, IPrompt...prompts) throws Exception {
//		command(null, 0, true, true, null, prompts);
		ArrayList<IPrompt> defaultPromts = null;
		IPrompt prompt = null;
		
		try {
			if (prompts != null) {
				defaultPromts = terminal.getPrompts();
				terminal.removePrompts();
				for (IPrompt pr : prompts) {
					if(resultPrompt != null) {
						if(pr.getPrompt() == resultPrompt.getPrompt()) {
							resultPrompt = pr;
						}
					}
					terminal.addPrompt(pr);
				}
			}
			startTime = System.currentTimeMillis();
			
			prompt = waitWithGrace(timeout);

			while (prompt.getStringToSend() != null) {
				if (timeout > 0) {
					if (System.currentTimeMillis() - startTime > (timeout)) {
						throw new IOException("timeout: " + timeout);
					}
				}
				String stringToSend = prompt.getStringToSend();
				if (stringToSend != null) {
					if (prompt.isAddEnter()) {
						stringToSend = stringToSend + getEnterStr(); //ENTER;
					}
					Thread.sleep(100);
					sendString(stringToSend, true);
					if(prompt instanceof LinkedPrompt) {
						prompt = ((LinkedPrompt) prompt).getLinkedPrompt();
					}else {
						prompt = waitWithGrace(timeout);
					}
				}
			}
		} finally {
			sessionPrompt = prompt;
			result.append(terminal.getResult());
			if (defaultPromts != null) {
				terminal.setPrompts(defaultPromts);
			}			
		}	
		
	}
	
	@Override
	public IPrompt waitWithGrace(long timeout) throws Exception {
		if(outputExpected) {
			outputExpected = false;
			resultPrompt = super.waitWithGrace(timeout);
		}
		
		return resultPrompt;
	}
	
	public void sendString(String stringToSend, boolean delayedTyping) throws IOException, InterruptedException {
		outputExpected = true;
		terminal.sendString(stringToSend, delayedTyping);
	}
	
	public void command(String command, long timeout, boolean addEnter, boolean delayedTyping) throws Exception {
		startTime = System.currentTimeMillis();
		
		if (addEnter) {
			command = command + getEnterStr(); //ENTER
		}
		resultPrompt = waitWithGrace(timeout);
		sendString(command, delayedTyping);
		resultPrompt = waitWithGrace(timeout);
	}
	
	public void switchToPrompt(Prompt prompt) throws Exception {
//		Prompt current = waitWithGrace(2000);
//		if(current.getPrompt() == prompt.getPrompt())
//			return;
//		
		addPrompt(prompt);
		login(0, prompt);
		
	}
	
	public IPrompt getCurrentPrompt() throws Exception {
		return this.waitWithGrace(0);
	}
	
	public IPrompt getCurrentPrompt(long timeout) throws Exception {
		return this.waitWithGrace(timeout);
	}

	
	public void resetToPrompt(IPrompt...prompts) throws Exception{
		long timeout = 0;
		login(timeout, prompts);
	}

//	public void resetToPrompt(List<IPrompt> logout_sequence) {
//		IPrompt[] array_prommpts = new IPrompt[logout_sequence.size()];
//		
//		
//	}
}

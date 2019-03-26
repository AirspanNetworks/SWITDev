package Utils.ConnectionManager.terminal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Terminal {
	Logger log = Logger.getLogger(Terminal.class.getName());
    protected static final int IN_BUFFER_SIZE = 65536;
    private StringBuffer result = new StringBuffer();
    protected OutputStream out = null;
    protected InputStream in = null;
    protected int bufChar = 10;
    protected long scrallEndTimeout = 200;
    ArrayList<IPrompt> prompts = new ArrayList<IPrompt>();

    public abstract void connect() throws IOException;
    public abstract void disconnect() throws IOException;
    public abstract boolean isConnected();
    public abstract String getConnectionName();
    
    private boolean delayedTyping = false;
    private boolean asciiFilter = true;
    private PrintStream printStream = System.out;
    
    public void addFilter(InOutInputStream input){
    	input.setInputStream(in);
    	in = input;
    }

    //public void 
    public synchronized void sendString(String command, boolean delayedTyping) throws IOException, InterruptedException{
        byte[] buf = command.getBytes("ASCII");
        
        // Do not override if delayed typing was set to TRUE from elsewhere
        if(this.delayedTyping != true){
        	setDelayedTyping(delayedTyping);
        }
        
        if (isDelayedTyping()){
            for (int i = 0; i < buf.length; i++){
                out.write(buf[i]);
                out.flush();
                Thread.sleep(20);
            }
        } else {
            out.write(buf);
            out.flush();
        }
    }
    
	public String readInputBuffer() throws Exception {
		int avail = in.available();
		if (avail <= 0 ){
			return "";
		}
		byte[] bytes = new byte[avail];
		in.read(bytes);
		return new String(bytes);
	}

	public static String readToEnd(InputStream in) throws IOException {
      byte[] b = new byte[1024];
      @SuppressWarnings("unused")
      int n;
      StringBuilder sb = new StringBuilder();
      while ((n = in.read(b)) >= 0) {
         sb.append(b);
      }
      return sb.toString();
   }
	
    public synchronized void addRemark(String remark){
        result.append(remark);
    }

    public synchronized boolean isScrallEnd() throws Exception{
    	if(scrallEndTimeout == 0){ // if set to 0 allways return true.
    		return true;
    	}
    	int avil0 = in.available();
    	Thread.sleep(scrallEndTimeout);
    	int avil1 = in.available();
    	//log.info("\nisScrallEnd avil0: " + avil0 + ", avil1: " + avil1 + ", bufChar: " + bufChar);
    	if(avil1 > bufChar){
    		//log.info("\navail1 is bigger then bufChar");
    		return false;
    	}
    	if(avil0 == avil1){ // no change after 1/2 time and avail under bufChar
    		return true;
    	}
    	Thread.sleep(scrallEndTimeout);
    	if(in.available() < bufChar){
    		return true;
    	}
    	//log.info("\navail1 is bigger then bufChar 2");
    	return false;
    }
    
    public synchronized void waitForPrompt(String[] prompts, long timeout) throws IOException, InterruptedException{
        long startTime = System.currentTimeMillis();
        StringBuffer sb = new StringBuffer();
        while (true) {
            if (timeout > 0) {
                if (System.currentTimeMillis() - startTime > timeout) {
                    result.append(sb);
                    throw new IOException("Prompt not resolved during timeout: " + timeout);
                }
            }
            int avail = in.available();
            if (avail > 0) {
            //System.out.println("Available: " + avail);
                while (avail > 0) {
                    int b = in.read();
                    if (b < 0) {
                        avail = in.available();
                        if (System.currentTimeMillis() - startTime > timeout) {
                            result.append(sb);
                            throw new IOException("Prompt not resolved during timeout#1: " + timeout);
                        }
                    	continue;
                    }
                    if (b == 8) {
                        sb.append('B');
                    }
                    if (b >= 127 ||
                             b < 9 ||
                             (b >= 14 && b <= 31) ||
                             b == 11 ||
                             b == 12) { // not ascii byte will be ignored
                    	avail = in.available();
                        continue;
                    }
                    sb.append((char)b);
                    if (printStream != null){
                    	printStream.print((char)b);                    
                    }
                    
                    String bufString = sb.toString();
                    boolean allPromptsFound = true;
                    for (int i = 0; i < prompts.length; i++){
                        if(bufString.indexOf(prompts[i]) < 0){
                        	allPromptsFound = false;
                        	break;
                        }
                    }
                    if(allPromptsFound){
                        result.append(sb);
                    	return;
                    }
                    avail = in.available();
                    if (timeout > 0) {
                        if (System.currentTimeMillis() - startTime > timeout) {
                            result.append(sb);
                            throw new IOException("Prompt not resolved during timeout#2: " + timeout);
                        }
                    }
                }
            } else {
                Thread.sleep(10);
            }
        }
    	
    }

    public synchronized IPrompt waitForPrompt(long timeout) throws IOException, InterruptedException{
        long startTime = System.currentTimeMillis();
        StringBuffer sb = new StringBuffer();
        IPrompt prompt = null;
        while (true) { 
            if (timeout > 0) {
                if (System.currentTimeMillis() - startTime > timeout) {
                    result.append(sb);
                    throw new IOException("Prompt waiting timeout: " + timeout);
                }
            }
            int avail = in.available();
            if (avail > 0) {
            //System.out.println("Available: " + avail);
                while (avail > 0) {
                    int b = in.read();
                    if (b < 0) {
                        avail = in.available();
                    	continue;
                        //result.append(sb);
                        //throw new IOException("connection close");
                    }
                    if (b == 8) {
                        sb.append('B');
                    }
                    if(asciiFilter){
                        if (b >= 127 || b < 9 || (b >= 14 && b <= 31) || b == 11 || b == 12) { // not ascii byte will be ignored
                        	avail = in.available(); // if the last value is a non-ascii character
                            continue;
                       }
                    }
                    sb.append((char)b);
                    if (printStream != null){
                    	printStream.print((char)b);
                    }
                    int promptArraySize = prompts.size();
                    for (int j = 0; j < promptArraySize; j++) {
                        prompt = (IPrompt)prompts.get(j);
                        if (prompt == null || prompt.getPrompt() == null){
                            continue;
                        }
                        String bufString = sb.toString();
                        if (prompt.isRegularExpression()){
                        	Pattern p = prompt.getPattern();
                        	Matcher m = p.matcher(bufString);
                            if(m.find()){
                                result.append(sb);
                                return prompt;
                            }
                        } else {
                            if (bufString.endsWith(prompt.getPrompt())){
                                result.append(sb);
                                return prompt;
                            }
                        }
                    }
                    avail = in.available();
                    /**
                     * change the timeout to be activate in a state were there is endless amount of output
                     * from the cli.
                     */ 
                    if (timeout > 0) {
                        if (System.currentTimeMillis() - startTime > timeout) {
                            result.append(sb);
                            throw new IOException("Prompt waiting timeout#1: " + timeout);
                        }
                    }
                }
            } else {
                Thread.sleep(10);
//                sendString("\r", false);
                
            }
        }
    }

    public synchronized IPrompt waitFor() throws IOException, InterruptedException{
    	return waitForPrompt(20000);
    }

    public synchronized String getResult(){
        String toRetun = result.toString();
        result = new StringBuffer();
        return toRetun;
    }

    public void closeStreams() throws IOException{
        if (in != null){
            in.close();
        }
        if (out != null){
            out.close();
        }
    }

    public void addPrompt(String promptString, boolean isRegExp){
        Prompt prompt = new Prompt(promptString,isRegExp);
        addPrompt(prompt);
    }
    
    public void addPrompt(IPrompt prompt){
        prompts.remove(prompt);
        prompts.add(prompt);
    }
    
    public void addPrompts(IPrompt...prompts) {
    	for(IPrompt prompt : prompts) {
    		addPrompt(prompt);
    	}
    }
    
    public IPrompt getPrompt(String prompt){
        for (int i = 0; i < prompts.size(); i++){
            IPrompt p = (IPrompt)prompts.get(i);
            if (p.getPrompt().equals(prompt)){
                return p;
            }
        }
        return null;
    }
    
    public void removePrompts(){
    	prompts = new ArrayList<IPrompt>();
    }
    
    @SuppressWarnings("unchecked")
	public ArrayList<IPrompt> getPrompts(){
    	return (ArrayList<IPrompt>)prompts.clone();
    }
    
    public void setPrompts(ArrayList<IPrompt> prompts){
    	this.prompts = prompts;
    }
	public int getBufChar() {
		return bufChar;
	}
	public void setBufChar(int bufChar) {
		this.bufChar = bufChar;
	}
	public long getScrallEndTimeout() {
		return scrallEndTimeout;
	}
	public void setScrallEndTimeout(long scrallEndTimeout) {
		this.scrallEndTimeout = scrallEndTimeout;
	}
	public boolean isDelayedTyping() {
		return delayedTyping;
	}
	public void setDelayedTyping(boolean delayedTyping) {
		this.delayedTyping = delayedTyping;
	}
	public boolean isAsciiFilter() {
		return asciiFilter;
	}
	public void setAsciiFilter(boolean asciiFilter) {
		this.asciiFilter = asciiFilter;
	}

	/**
	 * Sets the print stream to which the stream of the connection 
	 * will be dumped to.
	 * Set the print stream to System.out to dump terminal stream to the console,
	 * Set print stream to null to turn off stream dump.
	 */
	public void setPrintStream(PrintStream printStream) {
		this.printStream = printStream;
	}
	
}

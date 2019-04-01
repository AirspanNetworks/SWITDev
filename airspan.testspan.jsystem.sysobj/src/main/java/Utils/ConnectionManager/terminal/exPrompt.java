package Utils.ConnectionManager.terminal;

import java.util.regex.Pattern;
import systemobject.terminal.Prompt;

public class exPrompt extends Prompt{
    private String prompt;
    private boolean isRegularExpression = false;
    private boolean isScrallEnd = false;
    private boolean isCommandEnd = false;
    private String stringToSend = null;
    private boolean addEnter = true;
    private Pattern pattern = null;

    public exPrompt(){
        
    }
    
    public exPrompt(String prompt) {
    	this(prompt, false);
    }
    
    public exPrompt(String prompt, boolean isRegExp){
        this.prompt = prompt;
        this.isRegularExpression = isRegExp;
        setCommandEnd(true);
    }
    
    public exPrompt(String prompt, boolean isRegExp, boolean isCommandEnd){
    	this(prompt, isRegExp);
    	setCommandEnd(isCommandEnd);
     
    }
    
    public exPrompt(String prompt, boolean isRegExp, String stringToSend, boolean setEnter) {
    	this(prompt, isRegExp);
    	setStringToSend(stringToSend);
    	setAddEnter(setEnter);
		setCommandEnd(false);
    }
    
	public String getPrompt() {
        return prompt;
    }

	public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

	public boolean isRegularExpression() {
        return isRegularExpression;
    }

	public void setRegularExpression(boolean regularExpression) {
        isRegularExpression = regularExpression;
    }

	public boolean equals(Object o){
        return (o instanceof Prompt && ((Prompt)o).getPrompt().equals(getPrompt()));
    }

	public boolean isScrallEnd() {
        return isScrallEnd;
    }

	public void setScrallEnd(boolean scrallEnd) {
        isScrallEnd = scrallEnd;
    }

	public String getStringToSend() {
        return stringToSend;
    }

	public void setStringToSend(String stringToSend) {
        this.stringToSend = stringToSend;
    }

    
	public boolean isAddEnter() {
        return addEnter;
    }

    
	public void setAddEnter(boolean addEnter) {
        this.addEnter = addEnter;
    }

    
	public boolean isCommandEnd() {
        return isCommandEnd;
    }

    
	public void setCommandEnd(boolean commandEnd) {
        isCommandEnd = commandEnd;
    }
    
    
	public Pattern getPattern(){
    	if(pattern == null){
    		pattern = Pattern.compile(prompt,Pattern.DOTALL);
    	}
    	return pattern;
    }
    
    @Override
    public String toString() {
    	StringBuilder res = new StringBuilder(String.format("Prompt: '%s'; StringToSend: '%s'", this.getPrompt(), this.getStringToSend()));
		res.append(String.format("; is final shell: '%s'", this.isCommandEnd()));
		return res.toString();
    }
}

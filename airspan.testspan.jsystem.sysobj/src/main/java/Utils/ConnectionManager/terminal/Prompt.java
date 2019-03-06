package Utils.ConnectionManager.terminal;

import java.util.regex.Pattern;

public class Prompt {
    private String prompt;
    private boolean isRegularExpression = false;
    private boolean isScrallEnd = false;
    private boolean isCommandEnd = false;
    private String stringToSend = null;
    private boolean addEnter = true;
    private Pattern pattern = null;

    public Prompt(){
        
    }
    public Prompt(String prompt, boolean isRegExp){
        this.prompt = prompt;
        this.isRegularExpression = isRegExp;
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
}

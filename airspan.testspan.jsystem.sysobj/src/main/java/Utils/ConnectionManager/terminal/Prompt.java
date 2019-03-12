package Utils.ConnectionManager.terminal;

import java.util.regex.Pattern;

public class Prompt implements IPrompt {
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
    public Prompt(String prompt, boolean isRegExp, String stringToSend, boolean setEnter) {
    	this(prompt, isRegExp);
    	setStringToSend(stringToSend);
		setCommandEnd(setEnter);
    }
    
    /* (non-Javadoc)
	 * @see Utils.ConnectionManager.terminal.IPrompt#getPrompt()
	 */
    @Override
	public String getPrompt() {
        return prompt;
    }

    /* (non-Javadoc)
	 * @see Utils.ConnectionManager.terminal.IPrompt#setPrompt(java.lang.String)
	 */
    @Override
	public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    /* (non-Javadoc)
	 * @see Utils.ConnectionManager.terminal.IPrompt#isRegularExpression()
	 */
    @Override
	public boolean isRegularExpression() {
        return isRegularExpression;
    }

    /* (non-Javadoc)
	 * @see Utils.ConnectionManager.terminal.IPrompt#setRegularExpression(boolean)
	 */
    @Override
	public void setRegularExpression(boolean regularExpression) {
        isRegularExpression = regularExpression;
    }

    /* (non-Javadoc)
	 * @see Utils.ConnectionManager.terminal.IPrompt#equals(java.lang.Object)
	 */
    @Override
	public boolean equals(Object o){
        return (o instanceof Prompt && ((IPrompt)o).getPrompt().equals(getPrompt()));
    }

    /* (non-Javadoc)
	 * @see Utils.ConnectionManager.terminal.IPrompt#isScrallEnd()
	 */
    @Override
	public boolean isScrallEnd() {
        return isScrallEnd;
    }

    /* (non-Javadoc)
	 * @see Utils.ConnectionManager.terminal.IPrompt#setScrallEnd(boolean)
	 */
    @Override
	public void setScrallEnd(boolean scrallEnd) {
        isScrallEnd = scrallEnd;
    }

    /* (non-Javadoc)
	 * @see Utils.ConnectionManager.terminal.IPrompt#getStringToSend()
	 */
    @Override
	public String getStringToSend() {
        return stringToSend;
    }

    /* (non-Javadoc)
	 * @see Utils.ConnectionManager.terminal.IPrompt#setStringToSend(java.lang.String)
	 */
    @Override
	public void setStringToSend(String stringToSend) {
        this.stringToSend = stringToSend;
    }

    /* (non-Javadoc)
	 * @see Utils.ConnectionManager.terminal.IPrompt#isAddEnter()
	 */
    @Override
	public boolean isAddEnter() {
        return addEnter;
    }

    /* (non-Javadoc)
	 * @see Utils.ConnectionManager.terminal.IPrompt#setAddEnter(boolean)
	 */
    @Override
	public void setAddEnter(boolean addEnter) {
        this.addEnter = addEnter;
    }

    /* (non-Javadoc)
	 * @see Utils.ConnectionManager.terminal.IPrompt#isCommandEnd()
	 */
    @Override
	public boolean isCommandEnd() {
        return isCommandEnd;
    }

    /* (non-Javadoc)
	 * @see Utils.ConnectionManager.terminal.IPrompt#setCommandEnd(boolean)
	 */
    @Override
	public void setCommandEnd(boolean commandEnd) {
        isCommandEnd = commandEnd;
    }
    
    /* (non-Javadoc)
	 * @see Utils.ConnectionManager.terminal.IPrompt#getPattern()
	 */
    @Override
	public Pattern getPattern(){
    	if(pattern == null){
    		pattern = Pattern.compile(prompt,Pattern.DOTALL);
    	}
    	return pattern;
    }
}

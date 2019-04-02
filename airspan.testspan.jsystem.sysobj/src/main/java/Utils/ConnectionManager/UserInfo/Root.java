package Utils.ConnectionManager.UserInfo;

//import systemobject.terminal.Prompt;
import Utils.ConnectionManager.terminal.exPrompt;

public class Root extends UserSequence {
	/**
	 * Regular session with login 'root'
	 * @param password
	 */
	public Root(String password) {
		add(new exPrompt(PromptsCommandsInfo.LOGIN_PATTERN, true, "root", true));
		add(new exPrompt(PromptsCommandsInfo.PASSWORD_PATTERN, false, password, true));
		add(new exPrompt(PromptsCommandsInfo.ROOT_PATTERN, false));
	}
	
	public Root(String password, boolean is_ltecli) {
		this(password);
		
		if(is_ltecli) {
			UserSequence temp_sibling = new UserSequence();
			temp_sibling.add(new exPrompt(PromptsCommandsInfo.ROOT_PATTERN, false, PromptsCommandsInfo.LTECLI_COMMAND, true));
			temp_sibling.add(new exPrompt(PromptsCommandsInfo.LTECLI_PATTERN, false));
			addSibling(temp_sibling);
		}
	}
}

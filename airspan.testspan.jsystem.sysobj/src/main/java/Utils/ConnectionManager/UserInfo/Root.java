package Utils.ConnectionManager.UserInfo;

import Utils.ConnectionManager.terminal.Prompt;

public class Root extends UserSequence {
	/**
	 * Regular session with login 'root'
	 * @param password
	 */
	public Root(String password) {
		add(new Prompt(PromptsCommandsInfo.LOGIN_PATTERN, true, "root", true));
		add(new Prompt(PromptsCommandsInfo.PASSWORD_PATTERN, false, password, true));
		add(new Prompt(PromptsCommandsInfo.ROOT_PATTERN, false));
	}
	
	public Root(String password, boolean is_ltecli) {
		this(password);
		
		if(is_ltecli) {
			remove(new Prompt(PromptsCommandsInfo.ROOT_PATTERN, false));
			add(new Prompt(PromptsCommandsInfo.ROOT_PATTERN, false, PromptsCommandsInfo.LTECLI_COMMAND, true));
			add(new Prompt(PromptsCommandsInfo.LTECLI_PATTERN, false));
													
		}
	}
}

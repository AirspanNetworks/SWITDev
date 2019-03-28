package Utils.ConnectionManager.UserInfo;

import java.util.ArrayList;

import Utils.ConnectionManager.terminal.Prompt;

public class Admin extends UserSequence {
	/**
	 * Regular session with login 'admin'
	 * @param password
	 */
	public Admin(String password) {
		add(new Prompt(PromptsCommandsInfo.LOGIN_PATTERN, true, "admin", true));
		add(new Prompt(PromptsCommandsInfo.PASSWORD_PATTERN, false, password, true));
		add(new Prompt(PromptsCommandsInfo.ADMIN_PATTERN, false));
	}
	/**
	 * Regular session with login 'admin' switched to sudo
	 * @param password
	 * @param is_sudo
	 */
	public Admin(String password, boolean is_sudo) {
		
		this(password);
		
		if(is_sudo) {
			
			remove(new Prompt(PromptsCommandsInfo.ADMIN_PATTERN, true));
			add(new Prompt(PromptsCommandsInfo.ADMIN_PATTERN, false, PromptsCommandsInfo.SUDO_COMMAND, true));
			add(new Prompt(PromptsCommandsInfo.ROOT_PATTERN, false));
									
		}
	}
	
	/**
	 * Regular session with login 'admin' switched to 'lte_cli' 
	 * @param password
	 * @param is_sudo
	 * @param is_ltecli
	 */
	public Admin(String password, boolean is_sudo, boolean is_ltecli) {
		this(password, is_sudo);
		
		if(is_ltecli) {
			remove(new Prompt(PromptsCommandsInfo.ROOT_PATTERN, true));
			add(new Prompt(PromptsCommandsInfo.ROOT_PATTERN, false, PromptsCommandsInfo.LTECLI_COMMAND, true));
			add(new Prompt(PromptsCommandsInfo.LTECLI_PATTERN, false));
													
		}
	}
}

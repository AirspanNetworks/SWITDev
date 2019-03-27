package Utils.ConnectionManager.UserInfo;

import Utils.ConnectionManager.terminal.LinkedPrompt;
import Utils.ConnectionManager.terminal.Prompt;

public class Admin extends LinkedPrompt {
	/**
	 * Regular session with login 'admin'
	 * @param password
	 */
	public Admin(String password) {
		super(PromptsCommandsInfo.LOGIN_PATTERN, true, "admin", false);
		LinkedPrompt password_pr = new LinkedPrompt(PromptsCommandsInfo.PASSWORD_PATTERN, false, password, true);
		password_pr.setLinkedPrompt(new Prompt(PromptsCommandsInfo.ADMIN_PATTERN, true));
		setLinkedPrompt(password_pr);
		
//		setExit_sequence(new LinkedPrompt(PromptsCommandsInfo.ADMIN_PATTERN, false, PromptsCommandsInfo.EXIT_COMMAND, true));
		
	}
	/**
	 * Regular session with login 'admin' switched to sudo
	 * @param password
	 * @param is_sudo
	 */
	public Admin(String password, boolean is_sudo) {
		
		this(password);
		
		if(is_sudo) {
			LinkedPrompt sudo = new LinkedPrompt(PromptsCommandsInfo.ADMIN_PATTERN, false, PromptsCommandsInfo.SUDO_COMMAND, false);
			LinkedPrompt password_pr = new LinkedPrompt(PromptsCommandsInfo.PASSWORD_PATTERN, false, password, false);
			password_pr.setLinkedPrompt(new Prompt(PromptsCommandsInfo.ROOT_PATTERN, true));
			sudo.setLinkedPrompt(password_pr);
			LinkedPrompt last_pr = (LinkedPrompt) getLinkedPrompt();
			last_pr.setLinkedPrompt(sudo);
			
//			setExit_sequence(new LinkedPrompt(PromptsCommandsInfo.ROOT_PATTERN, false, PromptsCommandsInfo.EXIT_COMMAND, true));
			
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
			LinkedPrompt lte_cli = new LinkedPrompt(PromptsCommandsInfo.SUDO_COMMAND, false, PromptsCommandsInfo.LTECLI_COMMAND, true);
			lte_cli.setLinkedPrompt(new Prompt(PromptsCommandsInfo.LTECLI_PATTERN, false));
			LinkedPrompt password_pr = (LinkedPrompt) getLinkedPrompt();
			password_pr.setLinkedPrompt(lte_cli);
			
//			setExit_sequence(new LinkedPrompt(PromptsCommandsInfo.LTECLI_PATTERN, false, PromptsCommandsInfo.CntrC_COMMAND, true));
			
		}
	}
}

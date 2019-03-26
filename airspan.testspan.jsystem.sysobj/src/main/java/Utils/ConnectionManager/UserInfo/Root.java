package Utils.ConnectionManager.UserInfo;

import Utils.ConnectionManager.terminal.LinkedPrompt;
import Utils.ConnectionManager.terminal.Prompt;

public class Root extends UserInfoAbstract {
	/**
	 * Regular session with login 'root'
	 * @param password
	 */
	public Root(String password) {
		super(PromptsCommandsInfo.LOGIN_PATTERN, true, "root", true);
		LinkedPrompt password_pr = new LinkedPrompt(PromptsCommandsInfo.PASSWORD_PATTERN, false, password, true);
		password_pr.setLinkedPrompt(new Prompt(PromptsCommandsInfo.ROOT_PATTERN, false));
		setLinkedPrompt(password_pr);
		
		setExit_sequence(new LinkedPrompt(PromptsCommandsInfo.ROOT_PATTERN, false, PromptsCommandsInfo.EXIT_COMMAND, true));
	}
	
	public Root(String password, boolean is_ltecli) {
		this(password);
		
		if(is_ltecli) {
			LinkedPrompt lte_cli = new LinkedPrompt(PromptsCommandsInfo.SUDO_COMMAND, false, PromptsCommandsInfo.LTECLI_COMMAND, true);
			lte_cli.setLinkedPrompt(new Prompt(PromptsCommandsInfo.LTECLI_PATTERN, false));
			LinkedPrompt password_pr = (LinkedPrompt) getLinkedPrompt();
			password_pr.setLinkedPrompt(lte_cli);
			
			setExit_sequence(new LinkedPrompt(PromptsCommandsInfo.LTECLI_PATTERN,  false, PromptsCommandsInfo.CntrC_COMMAND, true));
		}
	}
}

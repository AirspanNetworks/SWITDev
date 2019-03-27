package Utils.ConnectionManager.UserInfo;

import Utils.ConnectionManager.terminal.LinkedPrompt;
import Utils.ConnectionManager.terminal.Prompt;

public class Op extends LinkedPrompt {
	
	public Op(String password) {
		super(PromptsCommandsInfo.LOGIN_PATTERN, true, "op", true);
		LinkedPrompt password_pr = new LinkedPrompt(PromptsCommandsInfo.PASSWORD_PATTERN, false, password, true);
		password_pr.setLinkedPrompt(new Prompt(PromptsCommandsInfo.LTECLI_PATTERN, false));
		setLinkedPrompt(password_pr);
		
//		setExit_sequence(new LinkedPrompt(PromptsCommandsInfo.LTECLI_PATTERN, false, PromptsCommandsInfo.CntrC_COMMAND, true));
	}
	
	public Op(String password, boolean is_sudo) {
		this(password);
		
		if(is_sudo) {
			LinkedPrompt password_pr = (LinkedPrompt) getLinkedPrompt();
			LinkedPrompt sudo = new LinkedPrompt(PromptsCommandsInfo.LTECLI_PATTERN, false, "airspansudo", true);
			sudo.setLinkedPrompt(new Prompt(PromptsCommandsInfo.ROOT_PATTERN, false));
			password_pr.setLinkedPrompt(sudo);
			setLinkedPrompt(password_pr);
			
//			setExit_sequence(new LinkedPrompt(PromptsCommandsInfo.ROOT_PATTERN, false, PromptsCommandsInfo.EXIT_COMMAND, true));
		}
	}
	
	public Op(String password, boolean is_sudo, boolean is_ltecli) {
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

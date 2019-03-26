package Utils.ConnectionManager.UserInfo;

import Utils.ConnectionManager.terminal.IPrompt;
import Utils.ConnectionManager.terminal.LinkedPrompt;
import Utils.ConnectionManager.terminal.Prompt;

public abstract class UserInfoAbstract extends LinkedPrompt {
	
	public UserInfoAbstract(String prompt, boolean isRegExp, String command, boolean isAddEnter) {
		super(prompt, isRegExp, command, isAddEnter);
		this.exit_sequence = new LinkedPrompt(prompt, isRegExp);
	}

	private LinkedPrompt exit_sequence = null;

	protected void setExit_sequence(LinkedPrompt exit_sequence) {
		exit_sequence.setLinkedPrompt(this.exit_sequence);
		this.exit_sequence = exit_sequence;
	}

	public final IPrompt getExit_sequence() {
		return exit_sequence;
	}
}

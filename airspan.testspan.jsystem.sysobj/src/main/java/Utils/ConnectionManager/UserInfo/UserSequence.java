package Utils.ConnectionManager.UserInfo;

import java.util.ArrayList;
import java.util.List;

import Utils.ConnectionManager.terminal.Prompt;

public class UserSequence extends ArrayList<Prompt> {
	
	List<UserSequence> sibling = null;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public Prompt getFinalPrompt() {
		if(siblingPrompts() == null) {
			for(Prompt pr : this) {
				if(pr.isCommandEnd())
					return pr;
			}
		}
		else {
			return siblingPrompts().get(siblingPrompts().size()-1).getFinalPrompt();
		}
		return null;
	}
	
	public List<UserSequence> siblingPrompts() {
		return sibling;
	}
	
	public boolean enforceSessionReset() {
		return false;
	}
}
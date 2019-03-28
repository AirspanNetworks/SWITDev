package Utils.ConnectionManager.UserInfo;

import org.apache.commons.lang3.tuple.Triple;

import Utils.ConnectionManager.terminal.IPrompt;
import Utils.ConnectionManager.terminal.Prompt;

public class UserStatementAbstract extends Prompt implements IPrompt {
	
	private IPrompt linked_prompt = null;
	
	public final IPrompt getLinkedPrompt() {
		return linked_prompt;
	}

	public final void setLinkedPrompt(IPrompt child) {
		this.linked_prompt = child;
		child.getPattern();
		this.setCommandEnd(false);
	}

	
	public UserStatementAbstract(String prompt, boolean isRegExp) {
		super(prompt, isRegExp);
	}

	public UserStatementAbstract(String prompt, boolean isRegExp, String stringToSend, boolean setEnter) {
		super(prompt, isRegExp, stringToSend, setEnter);
	}
	
	public UserStatementAbstract(String prompt, boolean isRegExp, String stringToSend, boolean setEnter, IPrompt linkedPrompt) {
		this(prompt, isRegExp, stringToSend, setEnter);
		if(linkedPrompt != null)
			setLinkedPrompt(linkedPrompt);
	}
	
	@SafeVarargs
	public static IPrompt generatePromptSequence(Triple<String, Boolean, String>...linked_prompts) {
		UserStatementAbstract result = null;
		
		for(Triple<String, Boolean, String> item : linked_prompts) {
			if(result == null) {
				result = new UserStatementAbstract(item.getLeft(), item.getMiddle(), item.getRight(), true);
			}else {
				result.setLinkedPrompt(new UserStatementAbstract(item.getLeft(), item.getMiddle(), item.getRight(), true));
			}
		}
		
		return result;
	}
	
	public static IPrompt generatePromptSequence(IPrompt...linked_prompts) {
		UserStatementAbstract result = null;
		
		for(IPrompt item : linked_prompts) {
			if(result == null) {
				result = (UserStatementAbstract)item;
			}else {
				result.setLinkedPrompt((UserStatementAbstract)item);
			}
		}
		
		return result;
	}
	
	@Override
	public String toString() {
		StringBuilder res = new StringBuilder(super.toString());	
		if(linked_prompt != null) {
			res.append("\n\t" + linked_prompt.toString());
		}
		
		return res.toString();
	}
	
	@Override
	public IPrompt getFinalPrompt() {
		if(linked_prompt != null) {
			return linked_prompt.getFinalPrompt();
		}
		
		return super.getFinalPrompt();
	}
	
	public boolean setFinalPrompt(IPrompt prompt) {
		if(linked_prompt != null) {
			if(((UserStatementAbstract)linked_prompt).getLinkedPrompt() == null){
				setLinkedPrompt(prompt);
				return true;
			}
		}
		
		return false;
	}
	
	
}

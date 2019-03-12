package Utils.ConnectionManager.terminal;

import java.util.ArrayList;
import java.util.List;

public class LinkedPrompt extends Prompt implements IPrompt {
	
	public final IPrompt getLinkedPrompt() {
		return linked_prompt;
	}

	public final void setLinkedPrompt(IPrompt child) {
		this.linked_prompt = child;
	}

	private IPrompt linked_prompt = null;
	
	public LinkedPrompt() {
		// TODO Auto-generated constructor stub
	}

	public LinkedPrompt(String prompt, boolean isRegExp) {
		super(prompt, isRegExp);
		// TODO Auto-generated constructor stub
	}

	public LinkedPrompt(String prompt, boolean isRegExp, String stringToSend, boolean setEnter) {
		super(prompt, isRegExp, stringToSend, setEnter);
		// TODO Auto-generated constructor stub
	}
	
	public LinkedPrompt(String prompt, boolean isRegExp, String stringToSend, boolean setEnter, IPrompt linkedPrompt) {
		this(prompt, isRegExp, stringToSend, setEnter);
		setLinkedPrompt(linkedPrompt);
	}
}

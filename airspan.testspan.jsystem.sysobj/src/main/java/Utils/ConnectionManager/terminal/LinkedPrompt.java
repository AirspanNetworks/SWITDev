package Utils.ConnectionManager.terminal;

import org.apache.commons.lang3.tuple.Triple;

public class LinkedPrompt extends Prompt implements IPrompt {
	
	public final IPrompt getLinkedPrompt() {
		return linked_prompt;
	}

	public final void setLinkedPrompt(IPrompt child) {
		this.linked_prompt = child;
		this.setCommandEnd(false);
	}

	private IPrompt linked_prompt = null;
	
//	public LinkedPrompt() {
//	}

	public LinkedPrompt(String prompt, boolean isRegExp) {
		super(prompt, isRegExp);
	}

	public LinkedPrompt(String prompt, boolean isRegExp, String stringToSend, boolean setEnter) {
		super(prompt, isRegExp, stringToSend, setEnter);
	}
	
	public LinkedPrompt(String prompt, boolean isRegExp, String stringToSend, boolean setEnter, IPrompt linkedPrompt) {
		this(prompt, isRegExp, stringToSend, setEnter);
		setLinkedPrompt(linkedPrompt);
	}
	
	@SafeVarargs
	public static IPrompt generatePromptSequence(Triple<String, Boolean, String>...linked_prompts) {
		LinkedPrompt result = null;
		
		for(Triple<String, Boolean, String> item : linked_prompts) {
			if(result == null) {
				result = new LinkedPrompt(item.getLeft(), item.getMiddle(), item.getRight(), true);
			}else {
				result.setLinkedPrompt(new LinkedPrompt(item.getLeft(), item.getMiddle(), item.getRight(), true));
			}
		}
		
		return result;
	}
	
	public static IPrompt generatePromptSequence(IPrompt...linked_prompts) {
		LinkedPrompt result = null;
		
		for(IPrompt item : linked_prompts) {
			if(result == null) {
				result = (LinkedPrompt)item;
			}else {
				result.setLinkedPrompt((LinkedPrompt)item);
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
}

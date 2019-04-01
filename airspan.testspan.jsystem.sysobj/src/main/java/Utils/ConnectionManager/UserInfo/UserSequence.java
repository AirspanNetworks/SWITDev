package Utils.ConnectionManager.UserInfo;

import java.util.ArrayList;
import java.util.Iterator;
import Utils.ConnectionManager.terminal.exPrompt;


public class UserSequence extends ArrayList<exPrompt> implements Iterator<UserSequence> {
	
	private UserSequence sibling = null;

//	public final UserSequence getSibling() {
//		return sibling;
//	}
//
//	public final void setSibling(UserSequence sibling) {
//		this.sibling = sibling;
//	}

	public final UserSequence getSibling() {
		UserSequence instance = this;
		while(instance.hasNext()) {
			instance = next();
		}
		return instance;
	}

	public final void setSibling(UserSequence sibling) {
		UserSequence instance = getSibling();
		instance.sibling = sibling;
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public exPrompt getFinalPrompt() {
		UserSequence instance = getSibling();
		
		for(exPrompt pr : instance) {
			if(pr.isCommandEnd()) {
				return pr;
			}
		}
		return null;
	}
	
	public boolean enforceSessionReset() {
		return false;
	}

	@Override
	public boolean hasNext() {
		return sibling == null ? false : true;
	}

	@Override
	public UserSequence next() {
		return sibling;
	}
	
	@Override
	public String toString() {
		return toString("");
	}
	
	public String toString(String indent) {
		StringBuffer result = new StringBuffer();
		for(exPrompt p : this) {
			result.append(indent + p.getPrompt());
			if(p.getStringToSend() != null) {
				result.append("; Command: " + p.getStringToSend());
			}
			result.append("\n");
		}
		
		UserSequence instance = this;
		while(instance.hasNext()) {
			instance = next();
			result.append(indent + instance.toString(indent + " "));
		}
		return result.toString();
	}
}
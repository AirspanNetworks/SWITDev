package Utils.ConnectionManager.UserInfo;

import java.util.ArrayList;
import java.util.Iterator;
import Utils.ConnectionManager.terminal.exPrompt;


public class UserSequence extends ArrayList<exPrompt> implements Iterator<UserSequence> {
	
	private UserSequence sibling = null;

	public void setSibling(UserSequence sibling) {
		this.sibling = sibling;
	}

	public UserSequence getLastSibling() {
//		UserSequence instance = this;
//		System.out.print(instance.toString());
//		while(instance.hasNext()) {
//			instance = next();
//		}
//		return instance;
		if(hasNext())
			return next().getLastSibling();
		else
			return this;
	}

	public void addSibling(UserSequence sibling) {
		UserSequence instance = getLastSibling();
		instance.setSibling(sibling);
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public exPrompt getFinalPrompt() {
		UserSequence instance = getLastSibling();
		
		for(exPrompt pr : instance) {
			if(pr.isCommandEnd()) {
				return pr;
			}
		}
		throw new IndexOutOfBoundsException("No final prompt found");
	}
	
	public boolean enforceSessionReset() {
		return false;
	}

	@Override
	public boolean hasNext() {
		boolean s = (sibling != null);
//		System.out.print("\nUS: " + this.toString() + (s ? "" : "doesn't ") + " have sibling\n");
		return s;
	}

	@Override
	public UserSequence next() {
//		System.out.print("\nNext of " + this.toString() + "\n\tis: " + sibling + "\n");
		return this.sibling;
	}
	
	@Override
	public String toString() {
		return super.toString() + "; " + (this.hasNext() ? "H" : "Doesn't h") + "ave sibling";
	}
//	
//	private String toString(String indent) {
//		StringBuffer result = new StringBuffer();
//		for(exPrompt p : this) {
//			result.append(indent + p.toString() + "\n");
//		}
//		
//		UserSequence instance = this;
//		while(instance.hasNext()) {
//			instance = next();
//			result.append(indent + instance.toString(indent + " "));
//		}
//		return result.toString();
//	}
}
package Utils.ConnectionManager.UserInfo;

import java.io.IOException;
import java.util.ArrayList;
import Utils.ConnectionManager.terminal.Prompt;

@SuppressWarnings("serial")
public class Op extends UserSequence {
	
	private String op_password = "";
	private String root_password = "";
	private String[] passwords = null;
	
	private Op(String password) throws IOException {
		passwords = password.split(PromptsCommandsInfo.DELIMITER);
		
		this.op_password = passwords[0];
		
		add(new Prompt(PromptsCommandsInfo.LOGIN_PATTERN, true, "op", true));
		add(new Prompt(PromptsCommandsInfo.PASSWORD_PATTERN, false, this.op_password, true));
		add(new Prompt(PromptsCommandsInfo.LTECLI_PATTERN, false));
	}
	
	private Op(String password, boolean is_sudo) throws IOException {
		this(password);
		
		if(is_sudo) {
			if(passwords.length < 2) {
				throw new IOException("Password string for user 'op' must care two passwords separated by '" + PromptsCommandsInfo.DELIMITER + "'");
			}
			this.root_password = passwords[1];
			
			if(sibling == null) {
				sibling = new ArrayList<UserSequence>();
			}
			
			UserSequence temp_sibling = new UserSequence();
			temp_sibling.add(new Prompt(PromptsCommandsInfo.LTECLI_PATTERN, false, PromptsCommandsInfo.AIRSPAN_COMMAND, true));
			temp_sibling.add(new Prompt(PromptsCommandsInfo.PASSWORD_PATTERN, false, this.root_password, true));
			temp_sibling.add(new Prompt(PromptsCommandsInfo.ROOT_PATTERN, false));
			
			sibling.add(temp_sibling);							
		}
	}
	
	public Op(String password, boolean is_sudo, boolean is_ltecli) throws IOException {
		this(password, is_sudo);
		
		if(is_ltecli) {
			if(!is_sudo) {
				throw new IOException("User 'op' cannot use lte_cli without sudo mode (set it TRUE)");
			}
			
			if(sibling == null) {
				sibling = new ArrayList<UserSequence>();
			}
			UserSequence temp_sibling = new UserSequence();
			temp_sibling.add(new Prompt(PromptsCommandsInfo.ROOT_PATTERN, false, PromptsCommandsInfo.LTECLI_COMMAND, true));
			temp_sibling.add(new Prompt(PromptsCommandsInfo.LTECLI_PATTERN, false));
			sibling.add(temp_sibling);
		}
	}
	
	@Override
	public boolean enforceSessionReset() {
		return true;
	}
}

package Utils.ConnectionManager.UserInfo;


import java.io.IOException;

//import systemobject.terminal.Prompt;
import Utils.ConnectionManager.terminal.exPrompt;


@SuppressWarnings("serial")
public class Admin extends UserSequence {
	/**
	 * Regular session with login 'admin'
	 * @param password
	 */
	public Admin(String password) {
		add(new exPrompt(PromptsCommandsInfo.LOGIN_PATTERN, true, "admin", true));
		add(new exPrompt(PromptsCommandsInfo.PASSWORD_PATTERN, false, password, true));
		add(new exPrompt(PromptsCommandsInfo.ADMIN_PATTERN, false));
	}
	/**
	 * Regular session with login 'admin' switched to sudo
	 * @param password
	 * @param is_sudo
	 */
	public Admin(String password, boolean is_sudo) {
		
		this(password);
		
		if(is_sudo) {
			UserSequence temp_sibling = new UserSequence();
			
			temp_sibling.add(new exPrompt(PromptsCommandsInfo.ADMIN_PATTERN, false, PromptsCommandsInfo.SUDO_COMMAND, true));
			temp_sibling.add(new exPrompt(PromptsCommandsInfo.PASSWORD_PATTERN, false, password, true));
			temp_sibling.add(new exPrompt(PromptsCommandsInfo.ROOT_PATTERN, false));
			setSibling(temp_sibling);
		}
	}
	
	/**
	 * Regular session with login 'admin' switched to 'lte_cli' 
	 * @param password
	 * @param is_sudo
	 * @param is_ltecli
	 * @throws IOException 
	 */
	public Admin(String password, boolean is_sudo, boolean is_ltecli) throws IOException {
		this(password, is_sudo);
		
		if(is_ltecli) {
			if(!is_sudo) {
				throw new IOException("User 'admin' cannot use lte_cli without sudo mode (set it TRUE)");
			}
			
			UserSequence ltecli = new UserSequence();
			ltecli.add(new exPrompt(PromptsCommandsInfo.ROOT_PATTERN, false, PromptsCommandsInfo.LTECLI_COMMAND, true));
			ltecli.add(new exPrompt(PromptsCommandsInfo.LTECLI_PATTERN, false));
			setSibling(ltecli);
			
		}
	}
}

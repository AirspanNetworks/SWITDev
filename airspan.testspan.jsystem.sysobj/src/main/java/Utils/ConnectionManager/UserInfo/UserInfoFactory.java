/**
 * Factorey class aim to get smooth access to avaliable login credentials
 */
package Utils.ConnectionManager.UserInfo;

import java.io.IOException;
import java.util.ArrayList;

//import systemobject.terminal.Prompt;
import Utils.ConnectionManager.terminal.exPrompt;

/**
 * @author doguz
 *
 */
public final class UserInfoFactory {	
	/**
	 * 
	 * @param userName  - login user string
	 * @param password  - login password string
	 * @param is_sudo   - Boolean flag
	 * @param is_ltecli - Boolean flag
	 * @return UserSequence derived class object
	 * @throws IOException Exception
	 */
	public static UserSequence getLoginSequenceForUser(String userName, String password, boolean is_sudo, boolean is_ltecli) throws IOException {
		switch (userName) {
			case "admin": return new Admin(password, is_sudo, is_ltecli);
			case "root": return new Root(password, is_ltecli);
			case "op": return new Op(password, is_sudo, is_ltecli);
			default: return null;
		}
	}
	
	public static ArrayList<exPrompt> getExitSequence(){
		ArrayList<exPrompt> result =  new ArrayList<>();
		result.add(new exPrompt(PromptsCommandsInfo.PASSWORD_PATTERN, false, PromptsCommandsInfo.CntrC_COMMAND, false));
		result.add(new exPrompt(PromptsCommandsInfo.LTECLI_PATTERN, false, PromptsCommandsInfo.CntrC_COMMAND, false));
		result.add(new exPrompt(PromptsCommandsInfo.ROOT_PATTERN, false, PromptsCommandsInfo.EXIT_COMMAND, true));
		result.add(new exPrompt(PromptsCommandsInfo.ADMIN_PATTERN, false, PromptsCommandsInfo.EXIT_COMMAND, false));
		result.add(new exPrompt(PromptsCommandsInfo.ТNЕТ_PATTERN, false, PromptsCommandsInfo.QUIT_COMMAND, true));
		result.add(new exPrompt(PromptsCommandsInfo.LOGIN_PATTERN, true));
		
		return result;
	}

	public static ArrayList<exPrompt> getAvaliablePrompts(String userPrompt){
		ArrayList<exPrompt> result =  new ArrayList<>();
		result.add(new exPrompt(userPrompt, false, true));
		result.add(new exPrompt(PromptsCommandsInfo.ROOT_PATTERN, false));
		result.add(new exPrompt(PromptsCommandsInfo.LTECLI_PATTERN, false));
		result.add(new exPrompt(PromptsCommandsInfo.LOGIN_PATTERN, false));
		result.add(new exPrompt(PromptsCommandsInfo.PASSWORD_PATTERN, false));
		result.add(new exPrompt(PromptsCommandsInfo.ТNЕТ_PATTERN, false));
		result.add(new exPrompt(PromptsCommandsInfo.LOGIN_PATTERN, false));
		return result;
	}

}

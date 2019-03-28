/**
 * 
 */
package Utils.ConnectionManager.UserInfo;

import java.io.IOException;
import java.util.ArrayList;
import Utils.ConnectionManager.terminal.Prompt;

/**
 * @author doguz
 *
 */
public final class UserInfoFactory {	
	/**
	 * 
	 * @param userName
	 * @param password
	 * @param is_sudo
	 * @param is_ltecli
	 * @return
	 * @throws IOException 
	 */
	public static UserSequence getLoginSequenceForUser(String userName, String password, boolean is_sudo, boolean is_ltecli) throws IOException {
		switch (userName) {
			case "admin": return new Admin(password, is_sudo, is_ltecli);
			case "root": return new Root(password, is_ltecli);
			case "op": return new Op(password, is_sudo, is_ltecli);
			default: return null;
		}
	}
	
	public static ArrayList<Prompt> getExitSequence(){
		ArrayList<Prompt> result =  new ArrayList<Prompt>();
		result.add(new Prompt(PromptsCommandsInfo.PASSWORD_PATTERN, false, PromptsCommandsInfo.CntrC_COMMAND, false));
		result.add(new Prompt(PromptsCommandsInfo.LTECLI_PATTERN, false, PromptsCommandsInfo.CntrC_COMMAND, false));
		result.add(new Prompt(PromptsCommandsInfo.ROOT_PATTERN, false, PromptsCommandsInfo.EXIT_COMMAND, false));
		result.add(new Prompt(PromptsCommandsInfo.ADMIN_PATTERN, false, PromptsCommandsInfo.EXIT_COMMAND, false));
		result.add(new Prompt(PromptsCommandsInfo.LOGIN_PATTERN, true));
		
		return result;
	}
}

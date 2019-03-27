/**
 * 
 */
package Utils.ConnectionManager.UserInfo;

import Utils.ConnectionManager.terminal.IPrompt;

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
	 */
	public static IPrompt getUserInfo(String userName, String password, boolean is_sudo, boolean is_ltecli) {
		switch (userName) {
			case "admin": return new Admin(password, is_sudo, is_ltecli);
			case "root": return new Root(password, is_ltecli);
			case "op": return new Op(password, is_sudo, is_ltecli);
			default: return null;
		}
	}
}

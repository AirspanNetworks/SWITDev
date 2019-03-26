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
	
//	public static IPrompt getUserInfo(String userName, String password) {
//		switch (userName) {
//			case "admin": return new Admin(password);
//			case "root": return new Root(password);
//			case "op": return new Op(password);
//			default: return null;
//		}
//	}
//	public static IPrompt getUserInfo(String userName, String password, boolean is_sudo) {
//		switch (userName) {
//			case "admin": return new Admin(password, is_sudo);
//			case "root": return new Root(password);
//			case "op": return new Op(password, is_sudo);
//			default: return null;
//		}
//	}
	
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

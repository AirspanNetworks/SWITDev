package EnodeB.Components.XLP;

import Utils.GeneralUtils;
import Utils.PasswordUtils;

public class XLP_16_5 extends XLP_16_0 {

	@Override
	public void init() throws Exception {
		setScpUsername(PasswordUtils.ROOT_USERNAME);
		setScpPort(2020);
		setScpPassword(PasswordUtils.ROOT_PASSWORD_16_50);
		setPassword(PasswordUtils.COSTUMER_PASSWORD_16_50);
		super.init();
	}

	@Override
	public String getMatchingPassword(String username) {
		String ans;
		switch (username) {
			case "root":
			case "airspansu":
				ans = PasswordUtils.ROOT_PASSWORD_16_50;
				break;
			case "op":
				ans = PasswordUtils.COSTUMER_PASSWORD_16_50;
				break;
			default:
				GeneralUtils.printToConsole("Unrecognised username: " + username + ". using admin password as default.");
				ans = PasswordUtils.ROOT_PASSWORD_16_50;
				break;
		}
		return ans;
	}
}
package EnodeB.Components.XLP;

import EnodeB.Components.EnodeBComponent;
import EnodeB.Components.Session.SessionManager;
import Utils.PasswordUtils;

public class DebugPort extends EnodeBComponent{
	private String DEBUG_PORT = "192.168.0.20";

	public void init() throws Exception {
		setUsername(PasswordUtils.ADMIN_USERNAME);
		setPassword(PasswordUtils.ADMIN_PASSWORD);
		setName("DebugPort");
		setExpectBooting(true);
		setIpAddress(DEBUG_PORT);
		super.init();
	}

	public boolean isSessionConnected() {
		return isSessionConnected(SessionManager.SSH_COMMANDS_SESSION_NAME);
	}

	public void closeSession() {
		setIpAddress(null);
		close();
	}

	public String sendCommands(String lteCliPrompt, String cmd) {
		return sendCommandsOnSession(SessionManager.SSH_COMMANDS_SESSION_NAME, EnodeBComponent.LTE_CLI_PROMPT, cmd, "");
	}

	public String getDEBUG_PORT() {
		return DEBUG_PORT;
	}

	public void setDEBUG_PORT(String dEBUG_PORT) {
		DEBUG_PORT = dEBUG_PORT;
	}
}
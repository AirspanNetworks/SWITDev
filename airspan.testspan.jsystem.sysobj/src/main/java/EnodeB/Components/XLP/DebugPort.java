package EnodeB.Components.XLP;

import EnodeB.Components.EnodeBComponent;
import EnodeB.Components.Session.SessionManager;

public class DebugPort extends EnodeBComponent{
	public final String DEBUG_PORT = "192.168.0.20";
	public final String DEBUG_PORT_USERNAME = "admin";
	public final String DEBUG_PORT_PASSWOED = "HeWGEUx66m=_4!ND";

	@Override
	public void init() throws Exception {
		setUsername(DEBUG_PORT_USERNAME);
		setPassword(DEBUG_PORT_PASSWOED);
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
}
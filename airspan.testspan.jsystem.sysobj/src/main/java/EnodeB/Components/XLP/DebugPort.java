package EnodeB.Components.XLP;

import EnodeB.Components.EnodeBComponent;
import EnodeB.Components.Session.SessionManager;

public class DebugPort extends EnodeBComponent{
	private String DEBUG_PORT = "192.168.0.20";
	private String DEBUG_PORT_USERNAME = "admin";
	private String DEBUG_PORT_PASSWOED = "HeWGEUx66m=_4!ND";

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

	public String getDEBUG_PORT() {
		return DEBUG_PORT;
	}

	public void setDEBUG_PORT(String dEBUG_PORT) {
		DEBUG_PORT = dEBUG_PORT;
	}

	public String getDEBUG_PORT_USERNAME() {
		return DEBUG_PORT_USERNAME;
	}

	public void setDEBUG_PORT_USERNAME(String dEBUG_PORT_USERNAME) {
		DEBUG_PORT_USERNAME = dEBUG_PORT_USERNAME;
	}

	public String getDEBUG_PORT_PASSWOED() {
		return DEBUG_PORT_PASSWOED;
	}

	public void setDEBUG_PORT_PASSWOED(String dEBUG_PORT_PASSWOED) {
		DEBUG_PORT_PASSWOED = dEBUG_PORT_PASSWOED;
	}
	
	
}
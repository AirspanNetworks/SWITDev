package IPG;

import java.util.Map;

import Utils.GeneralUtils;
import Utils.SSHConnector;
import jsystem.framework.system.SystemObjectImpl;

/**
 * @author ggrunwald Target's class is to connect to a server, and run a script
 *         from it
 */

public class IPG extends SystemObjectImpl {
	private String scriptsDirectory = "/home/swit/Desktop/IPG/";
	private String scriptName;
	private String scriptParam;
	private String serverIP;
	private String userName;
	private String serverPassword;
	private String fakeIP;
	private String realMME = "";
	private SSHConnector sshIPG = null;
	private int numOfUEs = 0;
	// default path and jar. To change it, use "setMyCommand" function
	private String runScriptCommand;

	// connect to the server
	public void connectIPG() {
		sshIPG = new SSHConnector(serverIP, userName, serverPassword);
		runScriptCommand = String.format("cd %s && lte_ipg -script %s", scriptsDirectory, scriptsDirectory);
		sshIPG.initConnection();
	}

	public SSHConnector getSshIPG() {
		return sshIPG;
	}

	public void setSshIPG(SSHConnector sshIPG) {
		this.sshIPG = sshIPG;
	}

	public void disconnectIPG() {
		if (sshIPG != null) {
			sshIPG.disconnect();
			sshIPG = null;
		}
	}

	public void runScript() {
		String command = runScriptCommand + scriptName;
		scriptParam = "'{\"fakeIP\":\"" + fakeIP + "\",\"realMME\":\"" + realMME + "\"";
		if (numOfUEs != 0) {
			scriptParam += ",\"numberOfUEs\":\"" + numOfUEs + "\"}'";
		} else {
			scriptParam += "}'";
		}
		command += " -scriptParam " + scriptParam;
		GeneralUtils.printToConsole(command);
		sshIPG.sendCommand(command, 0);
	}

	public void runScript(Map<String, String> scriptParams) {
		String command = runScriptCommand + scriptName;
		int i = 0;
		if (scriptParams != null && !scriptParams.isEmpty()) {
			scriptParam = "'{";
			for (String key : scriptParams.keySet()) {
				if (i == 0) {
					scriptParam += "\"" + key + "\":\"" + scriptParams.get(key) + "\"";
				} else {
					scriptParam += ",\"" + key + "\":\"" + scriptParams.get(key) + "\"";
				}
				i++;
			}
			scriptParam += "}'";
			command += " -scriptParam " + scriptParam;
		}
		GeneralUtils.printToConsole(command);
		sshIPG.sendCommand(command, 0);
	}

	public String getScriptsDirectory() {
		return scriptsDirectory;
	}

	public void setScriptsDirectory(String scriptsDirectory) {
		this.scriptsDirectory = scriptsDirectory;
	}

	public String getMyCommand() {
		return runScriptCommand;
	}

	public void setMyCommand(String runScriptCommand) {
		this.runScriptCommand = runScriptCommand;
	}

	public String readCommandLine(long timeout, String finish) {
		return sshIPG.readCommandLine(timeout, finish);
	}

	public String getScriptName() {
		return scriptName;
	}

	public void setScriptName(String scriptName) {
		this.scriptName = scriptName;
	}

	public String getScriptParam() {
		return scriptParam;
	}

	public void setScriptParam(String scriptParam) {
		this.scriptParam = scriptParam;
	}

	public String getServerIP() {
		return serverIP;
	}

	public void setServerIP(String serverIP) {
		this.serverIP = serverIP;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getServerPassword() {
		return serverPassword;
	}

	public void setServerPassword(String serverPassword) {
		this.serverPassword = serverPassword;
	}

	public String getFakeIP() {
		return fakeIP;
	}

	public void setFakeIP(String fakeIP) {
		this.fakeIP = fakeIP;
	}

	public int getNumOfUEs() {
		return numOfUEs;
	}

	public void setNumOfUEs(int numOfUEs) {
		this.numOfUEs = numOfUEs;
	}

	public String getRealMME() {
		return realMME;
	}

	public void setRealMME(String realMME) {
		this.realMME = realMME;
	}

	public boolean isConnected() {
		return sshIPG.isConnected();
	}
}

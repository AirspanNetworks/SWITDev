package Action.BasicAction;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.glassfish.grizzly.streams.StreamReader;
import org.glassfish.grizzly.streams.StreamWriter;
import org.junit.Test;

import Action.Action;
//import EnodeB.Components.Cli.Cli;
import Netspan.NetspanServer;
import PowerControllers.PowerController;
import PowerControllers.PowerControllerPort;
import Utils.GeneralUtils;
import Utils.SSHConnector;
import Utils.ConnectionManager.ConnectionInfo;
import Utils.ConnectionManager.ConnectorTypes;
import Utils.ConnectionManager.terminal.Prompt;
import Utils.ConnectionManager.terminal.Telnet;
import Utils.ConnectionManager.terminal.Terminal;
import Utils.WatchDog.commandWatchDLAndUL;
import Utils.ConnectionManager.terminal.ExtendCLI;
import Utils.ConnectionManager.terminal.IPrompt;
import Utils.ConnectionManager.terminal.LinkedPrompt;
import jsystem.framework.ParameterProperties;
import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;

public class BasicAction extends Action {
	private String timeToWait = "00:00:00";
	private String ipPowerPort;
	private String debugCommands;
	private String serialCommand = "id";
	
	public String getSerialCommand() {
		return serialCommand;
	}
	
	@ParameterProperties(description = "Command to be send to serial")
	public void setSerialCommand(String serialCommand) {
		this.serialCommand = serialCommand;
	}

	private String ip = "192.168.58.169";
	private int port = 2001;
	private boolean lteCliRequired = false;
	
	public final boolean getSudoRequired() {
		return sudoRequired;
	}
	
	@ParameterProperties(description = "Set false if not required (Default: true)")
	public final void setSudoRequired(boolean isSudoRequired) {
		this.sudoRequired = isSudoRequired;
	}

	private boolean sudoRequired = true;
	
	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	private String userName;
	private String password;
	private long sleepTime = 2;
	private String netspan;
	
	@ParameterProperties(description = "Set true is command belong to lteCli scope")
	public boolean getLteCliRequired() {
		return lteCliRequired;
	}

	public void setLteCliRequired(boolean isLteCliRequired) {
		this.lteCliRequired = isLteCliRequired;
	}

	
	@ParameterProperties(description = "Waiting time in seconds after sending last command. Default - no waiting")
	public void setSleepTime(long sleepTime) {
		this.sleepTime = sleepTime;
	}

	@ParameterProperties(description = "IP for SSH")
	public void setIp(String ip) {
		this.ip = ip;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getTimeToWait() {
		return timeToWait;
	}
	
	public String getNetspan() {
		return netspan;
	}
	
	@ParameterProperties(description = "Netspan Object in SUT")
	public void setNetspan(String netspan) {
		this.netspan = netspan;
	}
	
	@ParameterProperties(description = "Time To Wait In format HH:MM:SS")
	public void setTimeToWait(String timeToWait) {
		this.timeToWait = timeToWait;
	}
	
	@ParameterProperties(description = "ipPower port")
	public void setIpPowerPort(String port) {
		this.ipPowerPort = port;
	}

	@ParameterProperties(description = "Debug Commands List (Split by comma)")
	public void setDebugCommands(String debugCommands) {
		this.debugCommands = debugCommands;
	}
	
	@Test // 1
	@TestProperties(name = "waitAction", returnParam = "LastStatus", paramsInclude = { "timeToWait" })
	public void waitAction() {
		long timeToWaitMillisecond;
		timeToWaitMillisecond = setRunTimeToMilliSeconds(timeToWait);
		report.report("Wait " + timeToWait);

		if (GeneralUtils.unSafeSleep(timeToWaitMillisecond)) {
			report.report("Wait " + timeToWaitMillisecond + " Milliseconds Succeeded");
		} else {
			report.report("Failed to Wait " + timeToWaitMillisecond + " Milliseconds", Reporter.FAIL);
		}
	}
	
	@Test // 2
	@TestProperties(name = "Power ON Ip Power Port", returnParam = "LastStatus", paramsInclude = { "ipPowerPort" })
	public void powerOnIpPowerPort() {
		report.report("trying to Power On port ");
		try {
			PowerController powerControllerPort = (PowerController) system.getSystemObject("aviosys"); 
			
			PowerControllerPort chosenPort = powerControllerPort.getPort(this.ipPowerPort);
			if(chosenPort == null) {
				report.report("there is no Such port in IP Power!",Reporter.FAIL);
				return;
			}
			
			if(!chosenPort.powerOn()) {
				report.report("could not power ON port "+this.ipPowerPort,Reporter.FAIL);
				return;
			}
			report.report("port "+this.ipPowerPort +" set To ON!");
		}catch(Exception e) {
			report.report("Failed to init Ip Power System - check SUT");
			e.printStackTrace();
		}
	}
	
	
	@Test // 3
	@TestProperties(name = "Power OFF Ip Power Port", returnParam = "LastStatus", paramsInclude = { "ipPowerPort" })
	public void powerOffIpPowerPort() {
		report.report("trying to Power Off port ");
		try {
			PowerController powerControllerPort = (PowerController) system.getSystemObject("aviosys"); 
			
			PowerControllerPort chosenPort = powerControllerPort.getPort(this.ipPowerPort);
			if(chosenPort == null) {
				report.report("there is no Such port in IP Power!",Reporter.FAIL);
				return;
			}
			
			if(!chosenPort.powerOff()) {
				report.report("could not power OFF port "+this.ipPowerPort,Reporter.FAIL);
				return;
			}
			report.report("port "+this.ipPowerPort +" set To OFF!");
		}catch(Exception e) {
			report.report("Failed to init Ip Power System - check SUT");
			e.printStackTrace();
		}
	}
		
	@Test
	@TestProperties(name = "Send Commands To Serial", returnParam = "LastStatus", paramsInclude = { "Ip", "Port", "Password",
			"UserName", "SerialCommand", "SleepTime", "LteCliRequired", "SudoRequired" })
	public void sendCommandsToSerial() throws Exception {
		boolean isNull = false;
		ConnectionInfo conn_info;
		ExtendCLI cli = null;
		
		String EXIT = "exit";
		String CntrlC = "\u0003";
		
//		password = "HeWGEUx66m=_4!ND";
//		userName = "admin";
//		isLteCliRequired = false;
//		isSudoRequired = true;
////		serialCommand = "ue show link";
//		serialCommand = "/bs/bin/set_bank.sh";
		
		try {
			GeneralUtils.startLevel("Starting parameters");
			if(ip == null){
				report.report("IP cannot be empty");
				isNull = true;
			}
			if(port == 0){
				report.report("Port cannot be empty");
				isNull = true;
			}
			if(userName == null){
				report.report("UserName cannot be empty");
				isNull = true;
			}
			if(password == null){
				report.report("Password cannot be empty");
				isNull = true;
			}
			if(serialCommand == null){
				report.report("Serial Command cannot be empty");
				isNull = true;
			}
			
			if(isNull){
				return;
			}
			
		}
		finally {
			GeneralUtils.stopLevel();
		}
		
		GeneralUtils.startLevel("Create connection items");
		
		List<IPrompt> login_sequence = new ArrayList<IPrompt>();
		List<IPrompt> logout_sequence = new ArrayList<IPrompt>();
		List<IPrompt> session_sequence = new ArrayList<IPrompt>();
 		
		LinkedPrompt loggin_start = new LinkedPrompt("(login:)", true, userName, true);
		IPrompt password_prompt = new Prompt("Password:", false, password, true);
		loggin_start.setLinkedPrompt(password_prompt);
		login_sequence.add(loggin_start);
		
		if(sudoRequired) {
			LinkedPrompt sudo_su = new LinkedPrompt("$", false, "sudo su", true);
			sudo_su.setLinkedPrompt(password_prompt);
			login_sequence.add(sudo_su);
			login_sequence.add(new Prompt("#", false));
		}
		if(lteCliRequired) {
			login_sequence.add(new Prompt("#", false, "/bs/lteCli", true));
			login_sequence.add(new Prompt("lte_cli:>>", false));
		}
		
		session_sequence.add(new Prompt("$", false));
		session_sequence.add(new Prompt("#", false));
		session_sequence.add(new Prompt("lte_cli:>>", false));
		
		logout_sequence.add(new Prompt("(login:)", true));
		logout_sequence.add(new Prompt("Password:", false, CntrlC, true));
		logout_sequence.add(new Prompt("$", false, EXIT, true));
		logout_sequence.add(new Prompt("#", false, EXIT, true));
		logout_sequence.add(new Prompt("lte_cli:>>", false, CntrlC, true));
		
		try {
			conn_info = new ConnectionInfo("Serial", ip, port, userName, password, ConnectorTypes.Telnet);
			report.report("Connection: " + conn_info.toString());
			Terminal terminal = new Telnet(conn_info.host, conn_info.port);
			cli = new ExtendCLI(terminal);
			cli.addPrompts(session_sequence.toArray(new IPrompt[0]));
			cli.setGraceful(true);
			cli.setEnterStr("\n");
			
			GeneralUtils.startLevel("Connected: " + (terminal.isConnected() ? "Yes" : "No"));
			
			report.report("Reset session...");
			
			cli.sendString(cli.getEnterStr(), false);
			cli.resetToPrompt(logout_sequence.toArray(new IPrompt[0]));
			report.report("Reset session completed");
			report.report("Login starting...");
			cli.login(0, login_sequence.toArray(new IPrompt[0]));
			report.report("Login completed");
			report.report("Send command: " + serialCommand);
			cli.command(serialCommand, 10000, true, true);
			List<String> output = cli.getResult(serialCommand, cli.getCurrentPrompt().getPrompt(), new String[] {"\n", "\r"});
			
			for (String string : output) {
				report.report("--->  " + string);
			}
				
			
//			report.report("Logged In: " + (terminal.isConnected() ? "Yes" : "No"));
			GeneralUtils.stopLevel();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			report.report(e.getLocalizedMessage(), Reporter.FAIL);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			report.report(e.getMessage(), Reporter.FAIL);
		}
		finally {
			if(cli != null)
				cli.resetToPrompt(logout_sequence.toArray(new IPrompt[0]));
			GeneralUtils.stopAllLevels();
		}
	}
	
	@Test
	@TestProperties(name = "Send Commands In Device", returnParam = "LastStatus", paramsInclude = { "Ip", "Password",
			"UserName", "DebugCommands","SleepTime" })
	public void sendCommandsInDevice() {
		boolean isNull = false;
		if(ip == null){
			report.report("IP cannot be empty",Reporter.FAIL);
			isNull = true;
		}
		if(userName == null){
			report.report("UserName cannot be empty",Reporter.FAIL);
			isNull = true;
		}
		if(password == null){
			report.report("Password cannot be empty",Reporter.FAIL);
			isNull = true;
		}
		if(debugCommands == null){
			report.report("DebugCommands cannot be empty",Reporter.FAIL);
			isNull = true;
		}
		
		if(isNull){
			return;
		}
		
		report.report("Debug Commands to send:");
		for (String cmd : this.debugCommands.split(",")) {
			report.report(cmd);
		}

		SSHConnector ssh = new SSHConnector(ip, userName, password);
		ssh.initConnection();
		
		if(ssh.isConnected()){
			for (String cmd : this.debugCommands.split(",")) {
				String output = ssh.sendCommand(cmd, 1000);
				GeneralUtils.unSafeSleep(1000);
				report.report("Response for "+cmd+":"+output);
			}
			int wait = ((int)sleepTime) *1000;
			GeneralUtils.unSafeSleep(wait);
			ssh.disconnect();
		}else{
			report.report("Failed to connect to device",Reporter.FAIL);
		}
	}
	
	@Test 
    @TestProperties(name = "Change Netspan Server to Selected SUT object", returnParam = {"LastStatus", "Answer"}, paramsInclude = {"netspan"})
    public void changeNetspan() throws Exception {
    	NetspanServer nms = NetspanServer.getInstance(netspan);
    	nms.init();
    	report.report("Netspan changed to " + netspan + ", Hostname is: " + nms.getHostname() + " NBI ver is: " + nms.getNBI());
    }
	
	@ParameterProperties(description = "Run time in format HH:MM:SS (not mandatory)")
	public Integer setRunTimeToMilliSeconds(String runTime) {
		Integer result;
		Pattern p = Pattern.compile("(\\d+):(\\d+):(\\d+)");
		Matcher m = p.matcher(runTime);
		if(m.find()){
			int hours = Integer.valueOf(m.group(1))*60*60;
			int minutes = Integer.valueOf(m.group(2))*60;
			int seconds = Integer.valueOf(m.group(3));
			result = hours+minutes+seconds;
			result = result *1000;
		}else{
			result = null;
		}
		return result;
	}
}
package Action.BasicAction;


import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Test;

import Action.Action;
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
	private String serialCommand;
	private String expectedPatern;
	
	@ParameterProperties(description = "Evaluate if pattern exists in output (Ignored if omitted")
	public String getExpectedPatern() {
		return expectedPatern;
	}

	public void setExpectedPatern(String expectedPatern) {
		this.expectedPatern = expectedPatern;
	}

	public String getSerialCommand() {
		return serialCommand;
	}
	
	@ParameterProperties(description = "Command to be send to serial")
	public void setSerialCommand(String serialCommand) {
		this.serialCommand = serialCommand;
	}

	private String ip = "";
	private int port = 0;
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
	
	public static final String LOGIN_PATTERN = "login:";
	public static final String PASSWORD_PATTERN = "Password:";
	public static final String ADMIN_PATTERN = "$";
	public static final String SUDO_PATTERN = "#";
	public static final String LTECLI_PATTERN = "lte_cli:>>";
		
	@Test
	@TestProperties(name = "Send Commands To Serial", returnParam = "LastStatus", paramsInclude = { "Ip", "Port", "Password",
			"UserName", "SerialCommand", "SleepTime", "LteCliRequired", "SudoRequired", "ExpectedPatern" })
	public void sendCommandsToSerial() throws Exception {
		boolean isNull = false;
		ConnectionInfo conn_info;
		ExtendCLI cli = null;
		String EXIT = "exit";
		String CntrlC = "\u0003";
		String SUDO_COMMAND = "sudo su";
		String LTECLI_COMMAND = "/bs/lteCli";
		
//		ip = "192.168.58.169";
//		port = 2001;
//		password = "HeWGEUx66m=_4!ND";
//		userName = "admin";
////		lteCliRequired = false;
//		sudoRequired = true;
////		lteCliRequired = true;
//		serialCommand = "ls -la /";
////		serialCommand = "ue show link";
//		expectedPatern = "/mnt/flash ; dima;rsys";
//		sleepTime = 5;
//		
		try {
//			GeneralUtils.startLevel("Starting parameters");
			if(ip == null){
				GeneralUtils.startLevel("IP cannot be empty");
				isNull = true;
			}
			if(port == 0){
				GeneralUtils.startLevel("Port cannot be empty");
				isNull = true;
			}
			if(userName == null){
				GeneralUtils.startLevel("UserName cannot be empty");
				isNull = true;
			}
			if(password == null){
				GeneralUtils.startLevel("Password cannot be empty");
				isNull = true;
			}
			if(serialCommand == null){
				GeneralUtils.startLevel("Serial Command cannot be empty");
				isNull = true;
			}
		}
		finally {
			if(isNull){
				report.report("Some of parameters not valid", Reporter.FAIL);
				GeneralUtils.stopLevel();
				return;
			}
		}
		
		IPrompt logout_sequence;
		IPrompt logged_out = new Prompt("login:", false, true);
		IPrompt admin_logout = new LinkedPrompt(ADMIN_PATTERN, false, EXIT, true, logged_out);
		IPrompt password_reset = new LinkedPrompt(PASSWORD_PATTERN, false, CntrlC, true, logged_out);
		IPrompt sudo_logout = new LinkedPrompt(SUDO_PATTERN, false, EXIT, true, admin_logout);
		IPrompt ltecli_logout = new LinkedPrompt(LTECLI_PATTERN, false, CntrlC, true, sudo_logout);
		
		
		LinkedPrompt login_sequence = new LinkedPrompt(LOGIN_PATTERN, true, userName, true);
		LinkedPrompt admin_password = new LinkedPrompt(PASSWORD_PATTERN, false, password, true);
		IPrompt sudo_switch = null;
		
		if(sudoRequired) {
			IPrompt sudo_prompt = new Prompt(SUDO_PATTERN, false, true); 
			sudo_switch = new LinkedPrompt(PASSWORD_PATTERN, false, password, true);
			
			if(!lteCliRequired) {
				((LinkedPrompt)sudo_switch).setLinkedPrompt(sudo_prompt);
				logout_sequence = sudo_logout;
			}
			else {
				LinkedPrompt lte_cli_switch = new LinkedPrompt(SUDO_PATTERN, false, LTECLI_COMMAND, true, new Prompt(LTECLI_PATTERN, false, true));
				((LinkedPrompt)sudo_switch).setLinkedPrompt(lte_cli_switch);
				logout_sequence = ltecli_logout;
			}
			
			LinkedPrompt sudo_su = new LinkedPrompt(ADMIN_PATTERN, false, SUDO_COMMAND, true, sudo_switch);
			admin_password.setLinkedPrompt(sudo_su);
		} else {
			admin_password.setLinkedPrompt(new Prompt(ADMIN_PATTERN, false, true));
			logout_sequence = admin_logout;
		}
		
		login_sequence.setLinkedPrompt(admin_password);
		
		try {
			
			conn_info = new ConnectionInfo("Serial", ip, port, userName, password, ConnectorTypes.Telnet);
			report.report("Connection: " + conn_info.toString());
			Terminal terminal = new Telnet(conn_info.host, conn_info.port);
			cli = new ExtendCLI(terminal);
			cli.setGraceful(true);
			cli.setEnterStr("\n");
			
			GeneralUtils.startLevel("Connecting & loging in...");
			Thread.sleep(20);
			cli.sendString(cli.getEnterStr(), false);
			
			cli.addPrompts(
					new Prompt(ADMIN_PATTERN, false, true), 
					new Prompt(SUDO_PATTERN, false, true),
					new Prompt(LTECLI_PATTERN, false, true),
					new Prompt(LOGIN_PATTERN, false, true));
			
			IPrompt current_pr = cli.getCurrentPrompt();
			
			switch (current_pr.getPrompt()) {
				case SUDO_PATTERN: cli.resetToPrompt(sudo_logout); break;
				case ADMIN_PATTERN: cli.resetToPrompt(admin_logout); break;
				case LTECLI_PATTERN: cli.resetToPrompt(ltecli_logout); break;
				case PASSWORD_PATTERN: cli.resetToPrompt(password_reset);break;
			}
			
			report.report("Reset session completed");

			Thread.sleep(500);
			cli.login(sleepTime * 1000, login_sequence);
			report.report("Login to serial completed");
			GeneralUtils.stopLevel();
			
//			GeneralUtils.startLevel("Send command: " + serialCommand);
			Thread.sleep(100);
			String output_str = cli.exec_command(serialCommand, sleepTime * 1000, true, false);
			
			int status = Reporter.PASS;
			String result_text = "Test completed";
			
			if(expectedPatern != null) {
//				Map<String, String> expectedCollection = readExpected(expectedPatern);
				
				result_text += " as following:\n";
				
				for(String key : expectedPatern.split("\\s*;\\s*")) {
					int local_stat = output_str.contains(key) ? Reporter.PASS : Reporter.FAIL;
					result_text += String.format("Expected pattern '%s' %s exists in output\n", key, local_stat == Reporter.PASS ? "" : "not");
					status = status == Reporter.PASS ?  local_stat: status;
				}
				GeneralUtils.reportHtmlLink("Command " + serialCommand + " output", output_str, !Boolean.parseBoolean(String.format("%d", status)), "green", expectedPatern.split(";"));
			}
			else {
				result_text = "Test completed";
				GeneralUtils.reportHtmlLink("Command " + serialCommand + " output", output_str);
			}
			
			report.report(result_text, status);
			
			
		} catch (IOException e) {
			e.printStackTrace();
			report.report(e.getLocalizedMessage(), Reporter.FAIL);
		} catch (Exception e) {
			e.printStackTrace();
			report.report(e.getMessage(), Reporter.FAIL);
		}
		finally {
			if(cli != null) {
				cli.resetToPrompt(logout_sequence);
				cli.close();
			}
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
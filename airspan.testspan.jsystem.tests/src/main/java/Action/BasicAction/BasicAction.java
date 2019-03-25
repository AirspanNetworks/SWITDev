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
import jsystem.sysobj.traffic.analizers.SetPacket;

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
	private String userPrompt = "$";
	
//	@ParameterProperties(description = "Provide if user prompt require special prompt, default '$' used if omitted")
//	public String getUserPrompt() {
//		return userPrompt;
//	}

//	public void setUserPrompt(String userPrompt) {
//		this.userPrompt = userPrompt;
//	}

	private String password;
	private long sleepTime = 2;
	
	public final long getSleepTime() {
		return sleepTime;
	}

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
	
	@ParameterProperties(description = "Login user name")
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
			reason = "Failed to Wait " + timeToWaitMillisecond + " Milliseconds";
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
				report.report("There is no such port in IP Power",Reporter.FAIL);
				reason = "There is no such port in IP Power";
				return;
			}
			
			if(!chosenPort.powerOn()) {
				report.report("Could not power ON port "+this.ipPowerPort,Reporter.FAIL);
				reason = "Could not power ON port "+this.ipPowerPort;
				return;
			}
			report.report("Port "+this.ipPowerPort +" set to ON");
		}catch(Exception e) {
			report.report("Failed to init Ip Power System - check SUT");
			e.printStackTrace();
		}
	}
	
	@Test // 3
	@TestProperties(name = "Power OFF Ip Power Port", returnParam = "LastStatus", paramsInclude = { "ipPowerPort" })
	public void powerOffIpPowerPort() {
		report.report("Trying to Power Off port ");
		try {
			PowerController powerControllerPort = (PowerController) system.getSystemObject("aviosys"); 
			
			PowerControllerPort chosenPort = powerControllerPort.getPort(this.ipPowerPort);
			if(chosenPort == null) {
				report.report("There is no such port in IP Power",Reporter.FAIL);
				reason = "There is no such port in IP Power";
				return;
			}
			
			if(!chosenPort.powerOff()) {
				report.report("could not power OFF port "+this.ipPowerPort,Reporter.FAIL);
				reason = "could not power OFF port "+this.ipPowerPort;
				return;
			}
			report.report("port "+this.ipPowerPort +" set To OFF!");
		}catch(Exception e) {
			report.report("Failed to init Ip Power System - check SUT");
			e.printStackTrace();
		}
	}
	
	private void setUnitTestforserial() {
		setIp("192.168.58.12");
		setPort(4004);
		setUserName("root");
		setPassword("HeWGEUx66m=_4!ND");
//		setUserPrompt("#");
		setSerialCommand("ls -la /");
		setSudoRequired(false);
		setLteCliRequired(false);
		setSleepTime(20);
		
	}
	
	private static void resetSerialSession(ExtendCLI session) throws Exception {
		session.sendString("\n", false);
		Thread.sleep(20);
		session.sendString("\n", false);
		Thread.sleep(20);
		session.sendString("\n", false);
		Thread.sleep(20);
		session.sendString(CntrC_COMMAND + session.getEnterStr(), false);
		Thread.sleep(20);
		session.sendString("\n", false);
		Thread.sleep(20);
		session.sendString(EXIT_COMMAND + session.getEnterStr(), false);
		Thread.sleep(20);
		session.sendString("\n", false);
		Thread.sleep(20);
		session.sendString(CntrC_COMMAND + session.getEnterStr(), false);
		Thread.sleep(20);
		session.sendString("\n", false);
		Thread.sleep(20);
	}
	
	public static final String LOGIN_PATTERN = "login:";
	public static final String PASSWORD_PATTERN = "Password:";
	public static final String ADMIN_PATTERN = "$";
	public static final String ROOT_PATTERN = "#";
	public static final String LTECLI_PATTERN = "lte_cli:>>";
	public static final String EXIT_COMMAND = "exit";
	public static final String CntrC_COMMAND = "\u0003";	
	
	@Test
	@TestProperties(name = "Send Commands To Serial", returnParam = "LastStatus", paramsInclude = { "Ip", "Port",
			"UserName", "UserPrompt", "Password", "SerialCommand", "LteCliRequired", "SudoRequired", "SleepTime", "ExpectedPatern" })
	public void sendCommandsToSerial() throws Exception {
		boolean isNull = false;
		ConnectionInfo conn_info;
		ExtendCLI cli = null;
		String SUDO_COMMAND = "sudo su";
		String LTECLI_COMMAND = "/bs/lteCli";
		
		/* Internal unittest only */
//		 setUnitTestforserial(); 
		 
		
		try {
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
			}else {
				switch(userName) {
					case "root": userPrompt = ROOT_PATTERN; break;
					case "admin" : userPrompt = ADMIN_PATTERN; break;
				}
			}
			
			if(password == null){
				GeneralUtils.startLevel("Password cannot be empty");
				isNull = true;
			}
			if(serialCommand == null){
				GeneralUtils.startLevel("Serial Command cannot be empty");
				isNull = true;
			}
			if(userPrompt == null){
				GeneralUtils.startLevel("User prompt cannot be empty");
				isNull = true;
			}
		}
		finally {
			if(isNull){
				report.report("Some of parameters not valid", Reporter.FAIL);
				reason = "Some of parameters not valid";
				GeneralUtils.stopLevel();
				return;
			}
		}
		
		IPrompt logout_sequence;
		IPrompt logged_out = new Prompt("login:", false, true);
		IPrompt user_logout = new LinkedPrompt(userPrompt, false, EXIT_COMMAND, true, logged_out);
		IPrompt password_reset = new LinkedPrompt(PASSWORD_PATTERN, false, CntrC_COMMAND, true, logged_out);
		IPrompt sudo_logout = new LinkedPrompt(ROOT_PATTERN, false, EXIT_COMMAND, true, user_logout);
		IPrompt ltecli_logout = new LinkedPrompt(LTECLI_PATTERN, false, CntrC_COMMAND, true);
		
		
		LinkedPrompt user_login = new LinkedPrompt(LOGIN_PATTERN, true, userName, true);
		LinkedPrompt user_password = new LinkedPrompt(PASSWORD_PATTERN, false, password, true);
		
		IPrompt sudo_switch = null;
		
		if(sudoRequired) {
			IPrompt sudo_prompt = new Prompt(ROOT_PATTERN, false, true); 
			sudo_switch = new LinkedPrompt(PASSWORD_PATTERN, false, password, true);
			
			if(!lteCliRequired) {
				((LinkedPrompt)sudo_switch).setLinkedPrompt(sudo_prompt);
				logout_sequence = sudo_logout;
			}
			else {
				LinkedPrompt lte_cli_switch = new LinkedPrompt(ROOT_PATTERN, false, LTECLI_COMMAND, true, new Prompt(LTECLI_PATTERN, false, true));
				((LinkedPrompt)sudo_switch).setLinkedPrompt(lte_cli_switch);
				logout_sequence = ltecli_logout;
			}
			
			LinkedPrompt sudo_su = new LinkedPrompt(userPrompt, false, SUDO_COMMAND, true, sudo_switch);
			user_password.setLinkedPrompt(sudo_su);
		} else {
			if(lteCliRequired) {
				LinkedPrompt lte_cli_switch = new LinkedPrompt(ROOT_PATTERN, false, LTECLI_COMMAND, true, new Prompt(LTECLI_PATTERN, false, true));
				user_password.setLinkedPrompt(lte_cli_switch);
				((LinkedPrompt)ltecli_logout).setLinkedPrompt(user_logout);
				logout_sequence = ltecli_logout;
				
			}
			else {
				user_password.setLinkedPrompt(new Prompt(userPrompt, false, true));
				logout_sequence = user_logout;
			}
		}
		
		user_login.setLinkedPrompt(user_password);
		
		try {
			report.report("Login properties:\n" + user_login.toString());
			conn_info = new ConnectionInfo("Serial", ip, port, userName, password, ConnectorTypes.Telnet);
			report.report("Connection: " + conn_info.toString());
			Terminal terminal = new Telnet(conn_info.host, conn_info.port);
			cli = new ExtendCLI(terminal);
			cli.setGraceful(true);
			cli.setEnterStr("\n");
			cli.sendString(cli.getEnterStr(), false);
			GeneralUtils.startLevel("Reset session");
			resetSerialSession(cli);
		
			Thread.sleep(20);
			
			cli.addPrompts(
					new Prompt(userPrompt, false, true), 
					new Prompt(ROOT_PATTERN, false, true),
					new Prompt(LTECLI_PATTERN, false, true),
					new Prompt(LOGIN_PATTERN, false, true));
			
			IPrompt current_pr = cli.getCurrentPrompt();
			
//			switch (current_pr.getPrompt()) {
//				case ROOT_PATTERN: cli.resetToPrompt(sudo_logout); break;
//				case ADMIN_PATTERN: cli.resetToPrompt(user_logout); break;
//				case LTECLI_PATTERN: cli.resetToPrompt(ltecli_logout); break;
//				case PASSWORD_PATTERN: cli.resetToPrompt(password_reset);break;
//				default:
//					
//					break;
//			}
//			
//			current_pr = cli.getCurrentPrompt();
			report.report("Session reset to prompt: " + current_pr.getPrompt());
			
			Thread.sleep(100);
			report.report("Login properties:\n" + user_login.toString());
			cli.login(sleepTime * 1000, user_login);
			
			current_pr = cli.getCurrentPrompt(3);
			if(current_pr.getPrompt() != user_login.getFinalPrompt().getPrompt()) {
				throw new IOException("Shell stay in wrong prompt (Prompt: '" + current_pr.getPrompt() + "')");
			}
			report.report("Login to serial completed");
			GeneralUtils.stopLevel();
			
			Thread.sleep(100);
			String output_str = cli.exec_command(serialCommand, sleepTime * 1000, true, false);
			
			int status = Reporter.PASS;
			String result_text = "Test completed";
			
			if(expectedPatern != null) {
				
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
			
			report.report(result_text + "Output:\n" + output_str, status);
			
			
		} catch (IOException e) {
			report.report(e.getMessage(), Reporter.FAIL);
		} catch (Exception e) {
			report.report(e.getMessage(), Reporter.FAIL);
		}
		finally {
			if(cli != null) {
				cli.resetToPrompt(logout_sequence);
				cli.close();
			}
			GeneralUtils.stopLevel();
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
			reason = "Some of parameters not valid";
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
			reason = "Failed to connect to device";
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
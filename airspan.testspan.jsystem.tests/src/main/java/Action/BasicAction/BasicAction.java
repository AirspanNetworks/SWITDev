package Action.BasicAction;

import java.io.Console;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import Action.Action;
import Netspan.NBIVersion;
import Netspan.NetspanServer;
import PowerControllers.PowerController;
import PowerControllers.PowerControllerPort;
import Utils.GeneralUtils;
import Utils.SSHConnector;
import Utils.ConnectionManager.ConnectionInfo;
import Utils.ConnectionManager.ConnectorTypes;
import Utils.ConnectionManager.TelnetConnector;
import Utils.ConnectionManager.terminal.Cli;
import Utils.ConnectionManager.terminal.Telnet;
import Utils.ConnectionManager.terminal.Terminal;
import Utils.ConnectionManager.terminal.Prompt;
import jsystem.framework.ParameterProperties;
import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;

public class BasicAction extends Action {
	private String timeToWait = "00:00:00";
	private String ipPowerPort;
	private String debugCommands;
	private String serialCommand;
	
	public String getSerialCommand() {
		return serialCommand;
	}
	
	@ParameterProperties(description = "Command to be send to serial")
	public void setSerialCommand(String serialCommand) {
		this.serialCommand = serialCommand;
	}

	private String ip;
	private int port;
	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	private String userName;
	private String password;
	private String sleepTime;
	private String netspan;
	
	@ParameterProperties(description = "Waiting time in seconds after sending last command. Default - no waiting")
	public void setSleepTime(String sleepTime) {
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
			"UserName", "SerialCommand", "SleepTime" })
	public void sendCommandsToSerial() {
		boolean isNull = false;
		
//		
		GeneralUtils.startLevel("Starting parameters");
		
		try {
			
			if(ip == null){
				GeneralUtils.logToLevel("IP cannot be empty",Reporter.FAIL);
				isNull = true;
			}
			if(port == 0){
				GeneralUtils.logToLevel("Port cannot be empty",Reporter.FAIL);
				isNull = true;
			}
			if(userName == null){
				GeneralUtils.logToLevel("UserName cannot be empty",Reporter.FAIL);
				isNull = true;
			}
			if(password == null){
				GeneralUtils.logToLevel("Password cannot be empty",Reporter.FAIL);
				isNull = true;
			}
			if(serialCommand == null){
				GeneralUtils.logToLevel("Serial Command cannot be empty",Reporter.FAIL);
				isNull = true;
			}
			
			if(isNull){
				GeneralUtils.logToLevel("Parameters not comleted");
				return;
			}
			
			Long timeOut; 
			timeOut = Long.parseLong(timeToWait);
			
			GeneralUtils.stopLevel();
			GeneralUtils.startLevel("Create connection items");
			
			ConnectionInfo conn_info = new ConnectionInfo("Serial", ip, port, userName, password, ConnectorTypes.Telnet);
			
			GeneralUtils.logToLevel("Connection: " + conn_info.toString());
			
			Terminal terminal = new Telnet(conn_info.host, conn_info.port);
			terminal.connect();
			GeneralUtils.logToLevel("Connected: " + (terminal.isConnected() ? "Yes" : "No"));
//			Cli cli = new Cli(terminal);
			
			Prompt admin_login = new Utils.ConnectionManager.terminal.Prompt("login:", true);
			admin_login.setStringToSend(userName);
			admin_login.setCommandEnd(true);
			
			Prompt password_prompt = new Prompt("Password:", false);
			password_prompt.setStringToSend(password);
			password_prompt.setCommandEnd(true);
			
			Prompt sudo_su = new Prompt("#", false);
			sudo_su.setStringToSend("sudo su");
			sudo_su.setAddEnter(true);
			
			Prompt admin_prompt = new Prompt("#", false);
			Prompt sudo_prompt = new Prompt("$", false);
			Prompt lteCli = new Prompt("lteCli:>>", false);
			
			GeneralUtils.stopLevel();
			GeneralUtils.startLevel("Read current prompt");
			Prompt active_prompt = terminal.waitForPrompt(timeOut);
			
			GeneralUtils.logToLevel("Active prompt: " + active_prompt.toString());
			GeneralUtils.stopLevel();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			GeneralUtils.printToConsole(e.getMessage());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			GeneralUtils.printToConsole(e.getMessage());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			GeneralUtils.printToConsole(e.getMessage());
		}
		finally {
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
			int wait = sleepTime == null ? 0 : Integer.valueOf(sleepTime)*1000;
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
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
	private boolean isLteCliRequired = false;
	
	
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
	public boolean isLteCliRequired() {
		return isLteCliRequired;
	}

	public void setLteCliRequired(boolean isLteCliRequired) {
		this.isLteCliRequired = isLteCliRequired;
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
			"UserName", "SerialCommand", "SleepTime" })
	public void sendCommandsToSerial() throws Exception {
		boolean isNull = false;
		ConnectionInfo conn_info;
		ExtendCLI cli = null;
		
		password = "HeWGEUx66m=_4!ND";
		userName = "admin";
				
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
		LinkedPrompt loggin_start = new LinkedPrompt("(login:)", true, userName, true);
		Prompt logged_out = new Prompt("(login:)", true);
		Prompt password_prompt = new Prompt("Password:", false, password, true);
		
		loggin_start.setLinkedPrompt(password_prompt);
		
		Prompt password_reset = new Prompt("Password:", false, "\u0003", true);
		Prompt admin_exit = new Prompt("$", false, "exit", true);
		LinkedPrompt sudo_su = new LinkedPrompt("$", false, "sudo su", true);
		sudo_su.setLinkedPrompt(password_prompt);
		Prompt sudo_session = new Prompt("#", false);
		Prompt sudo_exit = new Prompt("#", false, "exit", true);
		
		Prompt lteCli_session = new Prompt("lte_cli:>>", false);
		Prompt lteCli_switchTo = new Prompt("#", false, "/bs/lteCli", true);
		
		Prompt lteCli_exit = new Prompt("lte_cli:>>", false, "\u0003", true);
		
		try {
			conn_info = new ConnectionInfo("Serial", ip, port, userName, password, ConnectorTypes.Telnet);
			report.report("Connection: " + conn_info.toString());
			Terminal terminal = new Telnet(conn_info.host, conn_info.port);
			cli = new ExtendCLI(terminal);
			
			cli.addPrompt(loggin_start);
//			cli.addPrompt(password_prompt);
			cli.addPrompt(sudo_su);
			cli.addPrompt(sudo_session);
			cli.addPrompt(lteCli_session);
			cli.setGraceful(true);
			cli.setEnterStr("\n");
			
			report.report("Connected: " + (terminal.isConnected() ? "Yes" : "No"));
			
			GeneralUtils.startLevel("Read current prompt");
			PrintStream stdarr = new PrintStream("C:\\probot\\tmp\\telnet.log");
			cli.setPrintStream(stdarr);
			cli.sendString(cli.getEnterStr(), false);
			
			cli.setGraceful(false);
			cli.resetToPrompt(logged_out, sudo_exit, admin_exit, password_reset, lteCli_exit);
			
			cli.login(0, true);
			
			isLteCliRequired = true;
			
			if(isLteCliRequired) {
				cli.switcToPrompt(lteCli_switchTo);
			}
						
//			String cmd = "/bs/bin/set_bank.sh";
			String cmd = "ue show link";
			report.report("Send command: " + cmd);
			cli.command(cmd);
			List<String> output = cli.getResult(cmd, sudo_session.getPrompt(), new String[] {"\n", "\r"});
			
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
				cli.resetToPrompt(logged_out, sudo_exit, admin_exit, password_reset, lteCli_exit);
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
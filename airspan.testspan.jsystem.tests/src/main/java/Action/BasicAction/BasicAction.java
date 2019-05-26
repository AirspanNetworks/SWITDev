package Action.BasicAction;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Test;

import Action.Action;
import Action.TrafficAction.TrafficAction.ExpectedType;
import Action.TrafficAction.TrafficAction.LoadType;
import Entities.ITrafficGenerator.Protocol;
import Entities.ITrafficGenerator.TransmitDirection;
import Netspan.NetspanServer;
import PowerControllers.PowerController;
import PowerControllers.PowerControllerPort;
import Utils.GeneralUtils;
import Utils.SSHConnector;
import Utils.ScpClient;
import Utils.ConnectionManager.ConnectionInfo;
import Utils.ConnectionManager.ConnectorTypes;
import Utils.ConnectionManager.UserInfo.UserSequence;
import Utils.ConnectionManager.UserInfo.PromptsCommandsInfo;
import Utils.ConnectionManager.UserInfo.UserInfoFactory;
//import Utils.ConnectionManager.terminal.Prompt;
//import Utils.ConnectionManager.terminal.Telnet;
//import Utils.ConnectionManager.terminal.Terminal;
import jsystem.framework.ParameterProperties;
import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;
import jsystem.framework.report.ReporterHelper;
import jsystem.framework.scenario.Parameter;
import Utils.ConnectionManager.terminal.exCLI;
//import systemobject.terminal.Prompt;
import Utils.ConnectionManager.terminal.exPrompt;
import ch.ethz.ssh2.SCPClient;
import systemobject.terminal.Telnet;
import systemobject.terminal.Terminal;


public class BasicAction extends Action {
	private String timeToWait = "00:00:00";
	private String ipPowerPort;
	private String debugCommands;
	private String serialCommand;
	private String expectedPatern;
	private Comparison comparison = Comparison.EQUAL_TO;
	private PerformAction performAction = PerformAction.CountLines;
	private int ammount = 1;
	private String fileName;
	
	private enum Comparison {
		EQUAL_TO, BIGGER_THAN, SMALLER_THAN
	}
	
	public String getFileName() {
		return fileName;
	}
	
	@ParameterProperties(description = "Full name of file, with full path")
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public int getAmmount() {
		return ammount;
	}

	@ParameterProperties(description = "Number of appearances/lines wanted in file. Default = 1")
	public void setAmmount(String ammount) {
		this.ammount = Integer.valueOf(ammount);
	}

	public PerformAction getPerformAction() {
		return performAction;
	}

	public void setPerformAction(PerformAction performAction) {
		this.performAction = performAction;
	}

	private enum PerformAction{
		CountLines, FindPattern;
	}
	
	public Comparison getComparison() {
		return comparison;
	}

	@ParameterProperties(description = "Actual [BIGGER/SMALLER] than expected or equals")
	public void setComparison(Comparison comparison) {
		this.comparison = comparison;
	}

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
	public final String getPassword() {
		return password;
	}

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
	
	@ParameterProperties(description = "Password string (In case of user 'op' with sudo must be passed two passwords delimited with '====' ")
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

	@ParameterProperties(description = "Debug Commands List (Split by ;)")
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
		setIp("192.168.58.169");
		setPort(2001);
		setUserName("admin");
		
//		setIp("192.168.58.12");
//		setPort(4004);
//		setUserName("op");
		
		setPassword("HeWGEUx66m=_4!ND");
//		setPassword("Ss%7^q7NC#Uj!AnX====HeWGEUx66m=_4!ND");
		setSerialCommand("ls -la /");
//		setSerialCommand("ue show link");
//		setSerialCommand("show banks");
		
		setSudoRequired(true);
		setLteCliRequired(false);
		setSleepTime(10);
		
	}
	

	@Test
	@TestProperties(name = "Send Commands To Serial", returnParam = "LastStatus", paramsInclude = { "Ip", "Port",
			"UserName", "UserPrompt", "Password", "SerialCommand", "LteCliRequired", "SudoRequired", "SleepTime", "ExpectedPatern" })
	public void sendCommandsToSerial() throws Exception {
		boolean isNull = false;
		ConnectionInfo conn_info;
		exCLI cli = null;
		
		/* Internal unittest only */
//		 setUnitTestforserial();
		 
		// Verify input parameters
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
					case "root": userPrompt = PromptsCommandsInfo.ROOT_PATTERN; break;
					case "admin" : userPrompt = PromptsCommandsInfo.ADMIN_PATTERN; break;
					case "op" : userPrompt = PromptsCommandsInfo.LTECLI_PATTERN;break;
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
		}catch (Exception e) {
			System.out.print("Error occurs: " + e.toString());
		}finally {
			if(isNull){
				report.report("Some of parameters not valid", Reporter.FAIL);
				reason = "Some of parameters not valid";
				GeneralUtils.stopLevel();
				return;
			}
		}
		
		// Read login sequence by user name (if sudo and/or lte required - also)
		UserSequence user_login = UserInfoFactory.getLoginSequenceForUser(userName, password, sudoRequired, lteCliRequired);
		ArrayList<exPrompt> exit_sequence = UserInfoFactory.getExitSequence();
		
		try {
			report.report("Login properties:\n" + user_login.toString());
			conn_info = new ConnectionInfo("Serial", ip, port, userName, password, ConnectorTypes.Telnet);
			report.report("Connection: " + conn_info.toString());
			Terminal terminal = new Telnet(conn_info.host, conn_info.port);
			cli = new exCLI(terminal);
			cli.setGraceful(true);
			cli.setEnterStr("\n");
			GeneralUtils.startLevel("Prepare session");
			
			// Read current prompt for decide to reset it
			cli.addPrompts(
					new exPrompt(userPrompt, false, true), 
					new exPrompt(PromptsCommandsInfo.ROOT_PATTERN, false),
					new exPrompt(PromptsCommandsInfo.LTECLI_PATTERN, false),
					new exPrompt(PromptsCommandsInfo.LOGIN_PATTERN, false),
					new exPrompt(PromptsCommandsInfo.PASSWORD_PATTERN, false),
					new exPrompt(PromptsCommandsInfo.ТNЕТ_PATTERN, false));
			
			
			exPrompt current_pr = cli.waitWithGrace(sleepTime * 100);
			
			if((user_login.enforceSessionReset() || current_pr.getPrompt() != user_login.getFinalPrompt().getPrompt()) && 
					current_pr.getPrompt() != PromptsCommandsInfo.LOGIN_PATTERN) {
				// Session require reset current state and login
				report.report("Session reset needed (Current prompt: '" + current_pr.getPrompt() + "' vs. desired: '" + user_login.getFinalPrompt().getPrompt() + "')");
				
				cli.login(sleepTime * 1000, exit_sequence.toArray(new exPrompt[] {}));
				
				current_pr = cli.waitWithGrace(sleepTime * 100);
				report.report("Session reset completed (Prompt: '" + current_pr.getPrompt() + "')");
				
			}else {
				// No reset or login needed
				report.report("No reset needed");
			}
			
			if(current_pr.getPrompt() == PromptsCommandsInfo.LOGIN_PATTERN) {
				// Session require login only
				report.report("Login needed:\n" + user_login.toString());
				if(!cli.login(sleepTime * 1000, user_login)) {
					report.report("Login failed (Prompt: '" + current_pr.getPrompt() + "')", Reporter.WARNING);
					throw new IOException("Login failed (Prompt: '" + current_pr.getPrompt() + "')");
				}
				report.report("Login to serial completed");
			}
			current_pr = cli.waitWithGrace(sleepTime * 100);
			report.report("Session prepare completed (Prompt: '" + current_pr.getPrompt() + "')");
			GeneralUtils.stopLevel();
			
			GeneralUtils.startLevel("Command execution - '" + serialCommand + "'");
			Thread.sleep(100);
			String output_str = cli.exec_command(serialCommand, sleepTime * 1000, true, false);
			
			int status = Reporter.PASS;
			String result_text = "Test completed";
			
			if(expectedPatern != null) {
//				report.report("Pattern verification not implemented", Reporter.WARNING);
				result_text += " as following:\n";
//				
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
			report.report(e.getMessage(), Reporter.FAIL);
		} catch (Exception e) {
			report.report(e.getMessage(), Reporter.FAIL);
		}
		finally {
			
			if(user_login.enforceSessionReset()) {
				cli.login(2000, exit_sequence.toArray(new exPrompt[] {}));
			}
			
			if(cli != null) {
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
		for (String cmd : this.debugCommands.split(";")) {
			report.report(cmd);
		}

		SSHConnector ssh = new SSHConnector(ip, userName, password);
		ssh.initConnection();
		
		if(ssh.isConnected()){
			for (String cmd : this.debugCommands.split(";")) {
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
	
	@Test
	@TestProperties(name = "Verify Input In File", returnParam = "LastStatus", paramsInclude = { "Ip", "Password",
			"UserName", "Comparison","ExpectedPatern", "PerformAction","Ammount","FileName" })
	public void verifyInputInFile() {
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
		if(fileName == null){
			report.report("fileName cannot be empty",Reporter.FAIL);
			isNull = true;
		}
		if(performAction == PerformAction.FindPattern){
			if(expectedPatern == null){
				report.report("ExpectedPatern cannot be empty",Reporter.FAIL);
				isNull = true;
			}			
		}
		
		if(isNull){
			reason = "Some of parameters not valid";
			return;
		}
		
		if(performAction == PerformAction.FindPattern){
			report.report("Pattern to search: "+expectedPatern);
		}
		report.report("Number of times: "+ammount);
		report.report("Comparison: "+comparison.toString());

		ScpClient scpClient = new ScpClient(ip, userName, password);
		if(scpClient.getFiles(System.getProperty("user.dir"),fileName)){
			int ammountInFile = 0;
			File toUpload = new File(fileName);
			try{
				FileReader read = new FileReader(toUpload);
				BufferedReader br = new BufferedReader(read);
				String line;
				Pattern p=null;
				if(expectedPatern!=null){
					p = Pattern.compile(expectedPatern);					
				}
				
				while((line = br.readLine()) != null){
					if(performAction == PerformAction.CountLines){
						ammountInFile++;
					}else{
						Matcher m = p.matcher(line);
						if(m.find()){
							ammountInFile++;
						}
					}
				}
			}catch(Exception e){
				e.printStackTrace();
			}

			switch(comparison){
				case EQUAL_TO:
					if(ammount == ammountInFile){
						if(performAction == PerformAction.CountLines){
							report.report("Number of lines in file is "+ammount+" as expected");
						}else{
							report.report("Pattern was found "+ammount+" times as expected");
						}
					}else{
						if(performAction == PerformAction.CountLines){
							report.report("Number of lines in file is "+ammountInFile+" instead of "+ammount,Reporter.FAIL);
						}else{
							report.report("Pattern was found "+ammountInFile+" times instead of "+ammount,Reporter.FAIL);
						}
					}
					break;
				case BIGGER_THAN:
					if(ammountInFile > ammount){
						if(performAction == PerformAction.CountLines){
							report.report("Number of actual lines is "+ammountInFile+" and is bigger than the expected "+ammount);
						}else{
							report.report("Number of actual times for pattern is "+ammountInFile+" and is bigger than the expected "+ammount);
						}
					}else{
						if(performAction == PerformAction.CountLines){
							report.report("Number of actual lines is "+ammountInFile+" and is not bigger than the expected "+ammount,Reporter.FAIL);
						}else{
							report.report("Number of actual times for pattern is "+ammountInFile+" and is not bigger than the expected "+ammount,Reporter.FAIL);
						}
					}
					break;
				case SMALLER_THAN:
					if(ammountInFile < ammount){
						if(performAction == PerformAction.CountLines){
							report.report("Number of actual lines is "+ammountInFile+" and is smaller than the expected "+ammount);
						}else{
							report.report("Number of actual times for pattern is "+ammountInFile+" and is smaller than the expected "+ammount);
						}
					}else{
						if(performAction == PerformAction.CountLines){
							report.report("Number of actual lines is "+ammountInFile+" and is not smaller than the expected "+ammount,Reporter.FAIL);
						}else{
							report.report("Number of actual times for pattern is "+ammountInFile+" and is not smaller than the expected "+ammount,Reporter.FAIL);
						}
					}
					break;
			}
			
			try {
				ReporterHelper.copyFileToReporterAndAddLink(report, toUpload, toUpload.getName());
			} catch (Exception e) {
				GeneralUtils.printToConsole("FAIL to upload TP Result File: " + fileName);
				e.printStackTrace();
			}
		}else{
			report.report("Failed to connect to device",Reporter.FAIL);
			reason = "Failed to connect to device";
		}
		scpClient.close();
	}
	
	@Override
	public void handleUIEvent(HashMap<String, Parameter> map, String methodName) throws Exception {

		if (methodName.equals("verifyInputInFile")) {
			handleUIEventVerifyInputInFile(map);
		}
	}

	private void handleUIEventVerifyInputInFile(HashMap<String, Parameter> map) {
		map.get("ExpectedPatern").setVisible(true);
		Parameter performAction = map.get("PerformAction");

		if(PerformAction.CountLines == PerformAction.valueOf(performAction.getValue().toString())){
			map.get("ExpectedPatern").setVisible(false);
		}
	}
}
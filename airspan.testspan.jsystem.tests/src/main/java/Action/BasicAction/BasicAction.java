package Action.BasicAction;

import org.junit.Test;

import Action.Action;
import PowerControllers.PowerController;
import PowerControllers.PowerControllerPort;
import Utils.GeneralUtils;
import Utils.SSHConnector;
import jsystem.framework.ParameterProperties;
import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;

public class BasicAction extends Action {
	private Long timeToWaitMillisecond = null;
	private String ipPowerPort;
	private String debugCommands;
	private String ip;
	private String userName;
	private String password;
	
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

	@ParameterProperties(description = "Time To Wait In Millisecond")
	public void setMllisecond(String mllisecond) {
		this.timeToWaitMillisecond = Long.valueOf(mllisecond);
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
	@TestProperties(name = "Wait Milliseconds", returnParam = "LastStatus", paramsInclude = { "Mllisecond" })
	public void waitMilliseconds() {
		report.report("Wait " + this.timeToWaitMillisecond + " Milliseconds");

		if (GeneralUtils.unSafeSleep(this.timeToWaitMillisecond)) {
			report.report("Wait " + this.timeToWaitMillisecond + " Milliseconds Succeeded");
		} else {
			report.report("Failed to Wait " + this.timeToWaitMillisecond + " Milliseconds", Reporter.FAIL);
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
	@TestProperties(name = "Send Commands In Device", returnParam = "LastStatus", paramsInclude = { "Ip", "Password",
			"UserName", "DebugCommands" })
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
				String output = ssh.sendCommand(cmd, 200);
				GeneralUtils.unSafeSleep(1000);
				report.report("Response for "+cmd+":"+output);
			}
			GeneralUtils.unSafeSleep(120000);
			ssh.disconnect();
		}else{
			report.report("Failed to connect to device",Reporter.FAIL);
		}
	}
}
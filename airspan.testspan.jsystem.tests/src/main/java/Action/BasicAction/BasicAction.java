package Action.BasicAction;


import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.time.DateUtils;
import org.junit.Test;

import Action.Action;
import EnodeB.EnodeB;
import Netspan.NetspanServer;
import Netspan.API.Lte.AlarmInfo;
import Netspan.API.Lte.EventInfo;
import PowerControllers.PowerController;
import PowerControllers.PowerControllerPort;
import Utils.GeneralUtils;
import Utils.SSHConnector;
import Utils.SysObjUtils;
import jsystem.framework.ParameterProperties;
import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;
import testsNG.Actions.AlarmsAndEvents;

public class BasicAction extends Action {
	private String timeToWait = "00:00:00";
	private String ipPowerPort;
	private String debugCommands;
	private String ip;
	private String userName;
	private String password;
	private String sleepTime;
	private String netspan;
	private EnodeB dut;
	private static Map<String,Date> startDate = null;
	private final int eventStartDeafult = 20;
	
	@ParameterProperties(description = "Name of Enodeb Which the test will be run On")
	public void setDUT(String dut) {
		this.dut = (EnodeB) SysObjUtils.getInstnce().initSystemObject(EnodeB.class, false, dut).get(0);
	}

	public String getDUT() {
		return this.dut.getNetspanName();
	}

	@Test
	@TestProperties(name = "Clear Alarms", returnParam = "LastStatus", paramsInclude = { "DUT" })
	public void clearAlarms() {
		if(dut == null){
			report.report("No dut in test", Reporter.FAIL);
			return;
		}
		AlarmsAndEvents alarmsAndEvents = AlarmsAndEvents.getInstance();
		alarmsAndEvents.deleteAllAlarmsNode(dut);
	}
	
	@Test
	@TestProperties(name = "Clear Events", returnParam = "LastStatus", paramsInclude = { "DUT" })
	public void clearEvents() {
		if(dut == null){
			report.report("No dut in test", Reporter.FAIL);
			return;
		}
		if(startDate == null){
			startDate = new HashMap<String,Date>();
		}
		Date dateFrom = new Date();
		startDate.put(dut.getNetspanName(), dateFrom);
	}
	
	@Test
	@TestProperties(name = "Get All Alarms", returnParam = "LastStatus", paramsInclude = { "DUT" })
	public void getAlarms() {
		if(dut == null){
			report.report("No dut in test", Reporter.FAIL);
			return;
		}
		printAlarmsInfo(dut);
	}
	
	@Test
	@TestProperties(name = "Get All Events", returnParam = "LastStatus", paramsInclude = { "DUT" })
	public void getEvents() {
		if(dut == null){
			report.report("No dut in test", Reporter.FAIL);
			return;
		}
		Date from = null;
		if(startDate.get(dut.getNetspanName()) == null){
			report.report("Clear events was not used. Getting events of last "+eventStartDeafult+" minutes");
			from = new Date();
			from = DateUtils.addMinutes(from, -eventStartDeafult);
		}else{
			from = startDate.get(dut.getNetspanName());
		}
		Date to = new Date();
		printEventsInfo(dut,from,to);
	}
	
	private void printEventsInfo(EnodeB enodeb,Date from, Date to){
		AlarmsAndEvents alarmsAndEvents = AlarmsAndEvents.getInstance();
		List<EventInfo> eventsInfo = alarmsAndEvents.getEventsNodeByDateRange(enodeb, from, to);
		if (!eventsInfo.isEmpty()) {
            GeneralUtils.startLevel(enodeb.getName() + "'s Alarms: ");
            for (EventInfo eventInfo : eventsInfo) {
                alarmsAndEvents.printEventInfo(eventInfo);
            }
            GeneralUtils.stopLevel();
        } else {
            report.report(enodeb.getName() + "'s events list is empty");
        }
	}
	
	private void printAlarmsInfo(EnodeB eNodeB) {
		AlarmsAndEvents alarmsAndEvents = AlarmsAndEvents.getInstance();
        List<AlarmInfo> alarmsInfo = alarmsAndEvents.getAllAlarmsNode(eNodeB);
        if (!alarmsInfo.isEmpty()) {
            GeneralUtils.startLevel(eNodeB.getName() + "'s Alarms: ");
            for (AlarmInfo alarmInfo : alarmsInfo) {
                alarmsAndEvents.printAlarmInfo(alarmInfo);
            }
            GeneralUtils.stopLevel();
        } else {
            report.report(eNodeB.getName() + "'s Alarms list is empty");
        }
    }
	
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
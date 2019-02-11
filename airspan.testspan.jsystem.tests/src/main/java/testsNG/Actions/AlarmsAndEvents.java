package testsNG.Actions;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import EnodeB.EnodeB;
import Netspan.AlarmSeverity;
import Netspan.NetspanServer;
import Netspan.API.Lte.AlarmInfo;
import Netspan.API.Lte.EventInfo;
import Utils.GeneralUtils;
import jsystem.framework.report.ListenerstManager;
import jsystem.framework.report.Reporter;

public class AlarmsAndEvents {
	private static AlarmsAndEvents instance;
	private static NetspanServer netspanServer = null;
	public static Reporter report = ListenerstManager.getInstance();
	
	private AlarmsAndEvents() {
	}
	
	public static AlarmsAndEvents getInstance() {
		if (instance == null)
			instance = new AlarmsAndEvents();
		try {
			netspanServer = NetspanServer.getInstance();
		} catch (Exception e) {
			report.report("Netspan Server is unavialable Error: " + e.toString(), Reporter.WARNING);
		}
		return instance;
	}
	
	public List<EventInfo> getAllEventsNode(EnodeB dut){
		return netspanServer.getAllEventsNode(dut);
	}
	
	public List<EventInfo> getAllEventsNode(EnodeB dut, Date startTime){
		return netspanServer.getAllEventsNode(dut, startTime);
	}
	
	public List<EventInfo> getAllEventsNode(EnodeB dut, Date startTime, Date endTime){
		return netspanServer.getEventsNodeByDateRange(dut, startTime, endTime);
	}

	/**
	 * Initialize the alarms for the input dut
	 *
	 * @param dut - dut
	 * @return - response to the "delete all alarms" request
	 */
	public boolean deleteAllAlarmsNode(EnodeB dut) {
		boolean response = false;
		try{
			response = netspanServer.deleteAllAlarmsNode(dut);
		}catch(Exception e){
			e.printStackTrace();
			report.report("deleteAllAlarmsNode failed due to: " + e.getMessage(), Reporter.WARNING);
		}
		return response;
	}
	
	public List<AlarmInfo> getAllAlarmsNode(EnodeB dut) {
		List<AlarmInfo> response = new ArrayList<AlarmInfo>();
		try{
			response = netspanServer.getAllAlarmsNode(dut);
		}catch(Exception e){
			e.printStackTrace();
			report.report("getAllAlarmsNode failed due to: " + e.getMessage(), Reporter.WARNING);
		}
		return response;
	}
	
	public List<EventInfo> getAllEventsNodeWithSpecificType(List<EventInfo> events, String eventType) {
		List<EventInfo> eventsRes = new ArrayList<EventInfo>();
		for(EventInfo event: events) {
			if(event.getEventType().toLowerCase().equals(eventType.toLowerCase())){
					eventsRes.add(event);					
			}
		}
		return eventsRes;
	}
	
	public List<EventInfo> getAllEventsNodeWithSpecificInfo(List<EventInfo> events, String eventInfo) {
		List<EventInfo> eventsRes = new ArrayList<EventInfo>();
		for(EventInfo event: events) {
			if(event.getEventInfo().toLowerCase().contains(eventInfo.toLowerCase())){
				eventsRes.add(event);
			}
		}
		return eventsRes;
	}
	
	public List<EventInfo> getAllEventsNodeWithSpecificTypeAndMessage(List<EventInfo> events, String eventType, String eventMessage) {
		List<EventInfo> eventsRes = new ArrayList<EventInfo>();
		for(EventInfo event: events) {
			if(event.getEventType().toLowerCase().equals(eventType.toLowerCase())){
				if(event.getEventInfo().toLowerCase().contains(eventMessage.toLowerCase())){
					eventsRes.add(event);
				}
			}
		}
		return eventsRes;
	}
	
	public List<AlarmInfo> getAllAlarmsNodeWithSpecificType(List<AlarmInfo> alarms, List<String> alarmTypes) {
		List<AlarmInfo> alarmsRes = new ArrayList<AlarmInfo>();
		for(String alarmType : alarmTypes) {
			GeneralUtils.printToConsole("Looking for alarm: " + alarmType);
			for(AlarmInfo alarm: alarms) {
				GeneralUtils.printToConsole("checking alarm: " + alarm.alarmType + " alarm info: " + alarm.alarmInfo);
				if(alarm.alarmType.trim().toLowerCase().equals(alarmType.toLowerCase())){
					alarmsRes.add(alarm);
				}
			}
		}
		return alarmsRes;
	}
	
	public boolean isAlarmSeverityExists(List<AlarmInfo> alarms, AlarmSeverity severity) {
		for(AlarmInfo alarm: alarms) {
			if(AlarmSeverity.translation(alarm.severity) == severity){
				return true;
			}
		}
		return false;
	}
	
	public List<AlarmInfo> getAllNodeAlarmsWithSpecificSeverity(List<AlarmInfo> alarms, AlarmSeverity severity) {
		List<AlarmInfo> alarmsRes = new ArrayList<AlarmInfo>();
		for (AlarmInfo alarm : alarms) {
			if (AlarmSeverity.translation(alarm.severity) == severity) {
				alarmsRes.add(alarm);
			}
		}
		return alarmsRes;
	}
	
	public boolean isAlarmInfoExists(List<AlarmInfo> alarms, String info) {
		for (AlarmInfo alarm : alarms) {
			if (alarm.alarmInfo.toLowerCase().equals(info.toLowerCase())) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isAlarmTypeExists(List<AlarmInfo> alarms, String alarmType) {
		for (AlarmInfo alarm : alarms) {
			if (alarm.alarmType.toLowerCase().equals(alarmType.toLowerCase())) {
				return true;
			}
		}
		return false;
	}
	
	public List<EventInfo> getEventsNodeByDateRange(EnodeB dut, Date from, Date to) {
		return netspanServer.getEventsNodeByDateRange(dut, from, to);
	}
	
	public boolean waitForAlarmShowUp(EnodeB dut, String alarmType, long timeout) {
		List<AlarmInfo> allAlarms;
		List<AlarmInfo> alarmsWithType;

		long startTime = System.currentTimeMillis(); // fetch starting time
		while ((System.currentTimeMillis() - startTime) < timeout) {
			allAlarms = this.getAllAlarmsNode(dut);
			List<String> alarmTypeList = new ArrayList<String>();
			alarmTypeList.add(alarmType);
			alarmsWithType = this.getAllAlarmsNodeWithSpecificType(allAlarms, alarmTypeList);
			if (!alarmsWithType.isEmpty()) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * created by Moran Goldenberg
	 * @param dut
	 * @param alarmType
	 * @param timeOutInMinutes
	 * @return true if the events appears until timeout
	 */
	public EventInfo waitForEventShowUp(EnodeB dut, String alarmType, int timeOutInMinutes) {
		List<EventInfo> allEvents;
		List<EventInfo> eventsWithType;

		Date startTime = new Date(); // fetch starting time
		Date endTime = org.apache.commons.lang3.time.DateUtils.addMinutes(startTime, timeOutInMinutes);
		startTime = org.apache.commons.lang3.time.DateUtils.addSeconds(startTime, -20);
		Date current = new Date();
		while (current.before(endTime)) {
			allEvents = this.getEventsNodeByDateRange(dut, startTime, current);
			eventsWithType = this.getAllEventsNodeWithSpecificType(allEvents, alarmType);
			if (!eventsWithType.isEmpty()) {
					ArrayList<EventInfo> newList = new ArrayList<EventInfo>(eventsWithType);
					return newList.get(0);
			}
			GeneralUtils.unSafeSleep(30000);
			current = new Date();
		}
		
		return null;
	}
	
	public void printEventsList(List<EventInfo> events) {
		for (EventInfo event : events) {
			GeneralUtils.startLevel("Event Type: " + event.getEventType());
			report.report("Source Type: " + event.getSourceType());
			report.report("Source Name: " + event.getSourceName());
			report.report("Event Info: " + event.getEventInfo());
			report.report("Received Time: " + event.getReceivedTime().toString());
			GeneralUtils.stopLevel();
		}
	}

	public boolean isExistSpecificEventType(List<EventInfo> events, String eventType, int levelReport) {
		for (EventInfo event : events) {
			if (event.getEventType().toLowerCase().equals(eventType.toLowerCase())) {
				report.report("Event Type '" + eventType + "' found");
				return true;
			}
		}
		report.report("Event Type '" + eventType + "' does not exist", levelReport);
		return false;
	}

	public boolean isExistSpecificAlarmType(List<AlarmInfo> alarms, String alarmType) {
		for (AlarmInfo alarm : alarms) {
			if (alarm.alarmType.toLowerCase().equals(alarmType.toLowerCase())) {
				return true;
			}
		}
		report.report("Alarm Type '" + alarmType + "' does not exist", Reporter.FAIL);
		return false;
	}
	
	
	/**
	 * Print all Alarm Parameters
	 */
	public void printAlarmInfo(AlarmInfo alarmInfo) {
		GeneralUtils.startLevel("AlarmType: " + alarmInfo.alarmType);
		report.report("AlarmId: " + alarmInfo.alarmId.toString());
		report.report("AlarmSource: " + alarmInfo.alarmSource);
		report.report("AlarmInfo: " + alarmInfo.alarmInfo);
		report.report("Severity: " + alarmInfo.severity);
		report.report("FirstReceived: " + alarmInfo.firstReceived);
		report.report("LastReceived: : " + alarmInfo.lastReceived);
		report.report("AlarmCount: " + alarmInfo.alarmCount);
		report.report("Acknowledged: " + alarmInfo.acknowledged);
		report.report("UserName: " + alarmInfo.userName);
		GeneralUtils.stopLevel();
	}
	
	public void printEventInfo(EventInfo eventInfo) {
		GeneralUtils.startLevel("EventId: " + eventInfo.getEventType());
		report.report("EventTypeId: " + eventInfo.getEventTypeId().toString());
		report.report("SourceType: " + eventInfo.getSourceType());
		report.report("SourceName: " + eventInfo.getSourceName());
		report.report("SourceId: " + eventInfo.getSourceId());
		report.report("SourceIfIndex: : " + eventInfo.getSourceIfIndex().toString());
		report.report("SourceMacAddress: " + eventInfo.getSourceMacAddress());
		report.report("SourceUniqueId: " + eventInfo.getSourceUniqueId());
		report.report("EventInfo: " + eventInfo.getEventInfo());
		report.report("ReceivedTime: "+ eventInfo.getReceivedTime().toString());
		report.report("EventId: "+ eventInfo.getEventId());
		GeneralUtils.stopLevel();
	}
	
}
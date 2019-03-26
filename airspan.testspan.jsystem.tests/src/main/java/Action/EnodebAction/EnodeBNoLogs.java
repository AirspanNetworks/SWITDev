package Action.EnodebAction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.time.DateUtils;
import org.junit.Test;

import EnodeB.EnodeB;
import Netspan.API.Lte.AlarmInfo;
import Netspan.API.Lte.EventInfo;
import Utils.GeneralUtils;
import Utils.SysObjUtils;
import jsystem.framework.ParameterProperties;
import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;
import testsNG.Actions.AlarmsAndEvents;

public class EnodeBNoLogs extends EnodebAction {
	private EnodeB dut;
	private static Map<String,Date> startDate;
	private final int eventStartDeafult = 20;
	private ArrayList<String> listOfAlarms;
	private ArrayList<String> listOfEvents;
	private verifyAlarmAction verifyAction = verifyAlarmAction.Equals;
	
	@ParameterProperties(description = "Equals: search for exact event. Contains: check if your string is in event info string")
	public void setVerifyAction(verifyAlarmAction verifyAction) {
		this.verifyAction = verifyAction;
	}

	public enum verifyAlarmAction{
		Equals, Contains;
	}
	
	@ParameterProperties(description = "List of alarms type ids, separated by ;")
	public void setListOfAlarms(String listOfAlarms) {
		this.listOfAlarms = new ArrayList<String>(Arrays.asList(listOfAlarms.split(";")));
	}

	@ParameterProperties(description = "List of events info, separated by #")
	public void setListOfEvents(String listOfEvents) {
		this.listOfEvents = new ArrayList<String>(Arrays.asList(listOfEvents.split("#")));
	}

	@ParameterProperties(description = "Name of Enodeb Which the action will be run On")
	public void setDUT(String dut) {
		this.dut = (EnodeB) SysObjUtils.getInstnce().initSystemObject(EnodeB.class, false, dut).get(0);
	}

	@Test
	@TestProperties(name = "Start Collect Alarms", returnParam = "LastStatus", paramsInclude = { "DUT" })
	public void startCollectAlarms() {
		if(isDutNull()){
			return;
		}
		AlarmsAndEvents alarmsAndEvents = AlarmsAndEvents.getInstance();
		alarmsAndEvents.deleteAllAlarmsNode(dut);
	}
	
	@Test
	@TestProperties(name = "Start Collect Events", returnParam = "LastStatus", paramsInclude = { "DUT" })
	public void startCollectEvents() {
		if(isDutNull()){
			return;
		}
		if(startDate == null){
			startDate = new HashMap<String,Date>();
		}
		Date dateFrom = new Date();
		startDate.put(dut.getNetspanName(), dateFrom);
		GeneralUtils.printToConsole("List of start times:");
		GeneralUtils.printToConsole(startDate.toString());
	}
	
	@Test
	@TestProperties(name = "Get All Alarms", returnParam = "LastStatus", paramsInclude = { "DUT" })
	public void getAlarms() {
		if(isDutNull()){
			return;
		}
		printAlarmsInfo(dut);
	}
	
	@Test
	@TestProperties(name = "Get All Events", returnParam = "LastStatus", paramsInclude = { "DUT" })
	public void getEvents() {
		if(isDutNull()){
			return;
		}
		Date from = setStartTime();
		Date to = new Date();
		report.report("Getting events from: "+from.toString()+" to: "+to.toString());
		printEventsInfo(dut,from,to);
	}
	
	@Test
	@TestProperties(name = "Verify Alarms", returnParam = "LastStatus", paramsInclude = { "DUT","ListOfAlarms" })
	public void verifyAlarms() {
		if(isDutNull()){
			return;
		}
		if(listOfAlarms.isEmpty()){
			report.report("No alarms were set to verify", Reporter.FAIL);
			reason = "No alarms were set to verify";
			return;
		}
		verifyAlarmsHelper();
	}
	
	@Test
	@TestProperties(name = "Verify Events", returnParam = "LastStatus", paramsInclude = { "DUT",
			"ListOfEvents","VerifyAction" })
	public void verifyEvents() {
		if(isDutNull()){
			return;
		}
		if(listOfEvents.isEmpty()){
			report.report("No events were set to verify", Reporter.FAIL);
			reason = "No events were set to verify";
			return;
		}
		if(verifyAction == verifyAlarmAction.Equals){
			report.report("Searching for exact event info");
		}else{
			report.report("Searching for containing event info");
		}
		verifyEventsHelper();
	}
	
	private boolean isDutNull(){
		if(dut == null){
			report.report("No dut in test", Reporter.FAIL);
			reason = "No dut in test";
			return true;
		}
		return false;
	}
	
	private Date setStartTime(){
		Date from = null;
		if(startDate == null || (startDate != null && startDate.get(dut.getNetspanName()) == null)){
			report.report("Clear events was not used. Getting events of last "+eventStartDeafult+" minutes", Reporter.WARNING);
			from = new Date();
			from = DateUtils.addMinutes(from, -eventStartDeafult);
		}else{
			from = startDate.get(dut.getNetspanName());
		}
		return from;
	}
	
	private void printEventsInfo(EnodeB enodeb,Date from, Date to){
		AlarmsAndEvents alarmsAndEvents = AlarmsAndEvents.getInstance();
		List<EventInfo> eventsInfo = alarmsAndEvents.getEventsNodeByDateRange(enodeb, from, to);
		if (!eventsInfo.isEmpty()) {
            GeneralUtils.startLevel(enodeb.getName() + "'s Events: ");
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
	
	private void verifyAlarmsHelper(){
		AlarmsAndEvents alarmsAndEvents = AlarmsAndEvents.getInstance();
        List<AlarmInfo> alarmsInfo = alarmsAndEvents.getAllAlarmsNode(dut);
        for(String id : listOfAlarms){
        	int count = 0;
        	for(AlarmInfo alarm : alarmsInfo){
        		if(id.equals(alarm.alarmTypeId.toString())){
        			count++;
        		}
        	}
        	if(count == 0){
        		report.report("Alarm with type id ["+id+"] was not found",Reporter.FAIL);
        		reason += "Alarm with type id ["+id+"] was not found.<br> ";
        	}else{
        		report.report("Alarm with type id ["+id+"] was found ["+count+"] time"+(count==1?"":"s"));
        	}
        }
	}
	
	private void verifyEventsHelper(){
		AlarmsAndEvents alarmsAndEvents = AlarmsAndEvents.getInstance();
        Date from = setStartTime();
        Date to = new Date();
		List<EventInfo> eventsInfo = alarmsAndEvents.getEventsNodeByDateRange(dut, from, to);
        for(String info : listOfEvents){
        	int count = 0;
        	for(EventInfo event : eventsInfo){
        		if(verifyAction == verifyAlarmAction.Equals){
        			if(info.toLowerCase().equals(event.getEventInfo().toLowerCase())){
        				count++;
        			}
        		}else{
        			if(event.getEventInfo().toLowerCase().contains(info.toLowerCase())){
        				count++;
        			}
        		}
        	}
        	if(count == 0){
        		report.report("Event with event info ["+info+"] was not found",Reporter.FAIL);
        		reason += "Event with event info ["+info+"] was not found.<br> ";
        	}else{
        		report.report("Event with event info ["+info+"] was found ["+count+"] time"+(count==1?"":"s"));
        	}
        }
	}
}
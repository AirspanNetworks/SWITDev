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
	
	public verifyAlarmAction getVerifyAction() {
		return verifyAction;
	}

	@ParameterProperties(description = "Equals: search for exact event. Contains: check if your string is in event info string")
	public void setVerifyAction(verifyAlarmAction verifyAction) {
		this.verifyAction = verifyAction;
	}

	public enum verifyAlarmAction{
		Equals, Contains;
	}
	
	public ArrayList<String> getListOfAlarms() {
		return listOfAlarms;
	}

	@ParameterProperties(description = "List of alarms type ids, separated by ;")
	public void setListOfAlarms(String listOfAlarms) {
		this.listOfAlarms = new ArrayList<String>(Arrays.asList(listOfAlarms.split(";")));
	}

	public ArrayList<String> getListOfEvents() {
		return listOfEvents;
	}

	@ParameterProperties(description = "List of events info, separated by #")
	public void setListOfEvents(String listOfEvents) {
		this.listOfEvents = new ArrayList<String>(Arrays.asList(listOfEvents.split("#")));
	}

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
		GeneralUtils.printToConsole(startDate.toString());
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
		Date from = setStartTime();
		Date to = new Date();
		report.report("Getting events from: "+from.toString()+" to: "+to.toString());
		printEventsInfo(dut,from,to);
	}
	
	private Date setStartTime(){
		Date from = null;
		if(startDate == null || (startDate != null && startDate.get(dut.getNetspanName()) == null)){
			report.report("Clear events was not used. Getting events of last "+eventStartDeafult+" minutes");
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
	
	@Test
	@TestProperties(name = "Verify Alarms", returnParam = "LastStatus", paramsInclude = { "DUT","ListOfAlarms" })
	public void verifyAlarms() {
		if(dut == null){
			report.report("No dut in test", Reporter.FAIL);
			return;
		}
		if(listOfAlarms.isEmpty()){
			report.report("No alarms were set to verify", Reporter.FAIL);
			return;
		}
		AlarmsAndEvents alarmsAndEvents = AlarmsAndEvents.getInstance();
        List<AlarmInfo> alarmsInfo = alarmsAndEvents.getAllAlarmsNode(dut);
        for(String id : listOfAlarms){
        	int count = 0;
        	for(AlarmInfo alarm : alarmsInfo){
        		if(id.equals(alarm.alarmTypeId)){
        			count++;
        		}
        	}
        	if(count == 0){
        		report.report("Alarm with type id ["+id+"] was not found",Reporter.FAIL);
        	}else{
        		report.report("Alarm with type id ["+id+"] was found ["+count+"] time"+(count==1?"":"s"));
        	}
        }
	}
	
	@Test
	@TestProperties(name = "Verify Events", returnParam = "LastStatus", paramsInclude = { "DUT",
			"ListOfEvents","VerifyAction" })
	public void verifyEvents() {
		if(dut == null){
			report.report("No dut in test", Reporter.FAIL);
			return;
		}
		if(listOfEvents.isEmpty()){
			report.report("No events were set to verify", Reporter.FAIL);
			return;
		}
		if(verifyAction == verifyAlarmAction.Equals){
			report.report("Searching for exact event info");
		}else{
			report.report("Searching for containing event info");
		}
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
        	}else{
        		report.report("Event with event info ["+info+"] was found ["+count+"] time"+(count==1?"":"s"));
        	}
        }
	}
}
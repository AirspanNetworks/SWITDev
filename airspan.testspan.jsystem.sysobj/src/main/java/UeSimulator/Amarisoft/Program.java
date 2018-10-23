package UeSimulator.Amarisoft;
import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import UE.AmarisoftUE;
import UeSimulator.Amarisoft.JsonObjects.Actions.UEAction;
import UeSimulator.Amarisoft.JsonObjects.Actions.UEAction.Actions;
import UeSimulator.Amarisoft.JsonObjects.ConfigFile.Cell;
import UeSimulator.Amarisoft.JsonObjects.ConfigFile.SimEvent;
import UeSimulator.Amarisoft.JsonObjects.ConfigFile.UeList;
import Utils.GeneralUtils;


public class Program {

	public static AmariSoftServer clientEndPoint;
	
	public static void main(String[] args) throws JsonProcessingException {
		try
		{
		
			AmariSoftServer a = new AmariSoftServer();
			a.setDlMachineNetworks("91.91.117.240");
			a.easyInit();
			a.startServer("automationConfigFile");
			a.addUes(10, 13, 6);
			
			a.stopServer();
		}
		catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
		} 
	}

	private static void workUes(AmariSoftServer clientEndPoint) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		int startUe = 100;
		boolean isPowerOn = true;
		for (int i = 1; i <= 150; i++) {				
			UEAction getUE = new UEAction();
			getUE.setUeId(i);
			getUE.setMessage(Actions.POWER_ON);
			clientEndPoint.sendMessage(mapper.writeValueAsString(getUE));
			sleep(100);
		}	
		while (true) {
			for (int i = 151; i <= 256; i++) {				
				UEAction getUE = new UEAction();
				getUE.setUeId(startUe + i);
				if(isPowerOn)
					getUE.setMessage(Actions.POWER_ON);
				else
					getUE.setMessage(Actions.POWER_OFF);
				clientEndPoint.sendMessage(mapper.writeValueAsString(getUE));
				sleep(100);
			}		
			sleep(30000);
			isPowerOn = !isPowerOn;			
		}
	}

	private static void sleep(long sleepTime) {
		try {
			Thread.sleep(sleepTime);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void AddUe(String imsi, int release, int category, int ueId) throws JsonProcessingException
	{
		ObjectMapper mapper = new ObjectMapper();
		ArrayList<UeList> ueLists = new ArrayList<UeList>();
		UeList ueList = new UeList();
		ueList.setAsRelease(release);
		ueList.setUeCategory(category);
		ueList.setForcedCqi(15);
		ueList.setForcedRi(2);
		ueList.setSimAlgo("milenage");
		ueList.setImsi(imsi);
		ueList.setImeisv("1234567891234567");
		ueList.setK("5C95978B5E89488CB7DB44381E237809");
		ueList.setOp("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF");
		ueList.setTunSetupScript("ue-ifup_TCP");
		ueList.setUeId(ueId);
		//ueList.setAdditionalProperty("ue_count", 5);
		ueLists.add(ueList);
		UEAction addUE = new UEAction();
		addUE.setMessage(Actions.UE_ADD);
		addUE.setUeList(ueLists);
		String message = mapper.writeValueAsString(addUE);
		System.out.println(message);
		clientEndPoint.sendMessage(message);
	}

	private static AmariSoftServer setConfig() {
		//AmariSoftServer clientEndPoint = new AmariSoftServer("192.168.58.91", "9002", "root", "SWITswit");
		ArrayList<Cell> cells = new ArrayList<Cell>();
		Cell cell = new Cell();
		cell.setDlEarfcn(39790);
		cell.setNAntennaDl(2);
		cell.setNAntennaUl(1);
		cell.setGlobalTimingAdvance(2);
		cells.add(cell);
		cell = new Cell();
		cell.setDlEarfcn(39988);
		cell.setNAntennaDl(2);
		cell.setNAntennaUl(1);
		cell.setGlobalTimingAdvance(2);
		cells.add(cell);
		clientEndPoint.configObject.setCells(cells);
		clientEndPoint.configObject.getRfDriver().setArgs("dev0=/dev/sdr0,dev1=/dev/sdr1");
		ArrayList<SimEvent> simEvents = new ArrayList<SimEvent>();
		SimEvent simEvent = new SimEvent();
		simEvent.setEvent("power_on");
		simEvent.setStartTime(0);
		simEvents.add(simEvent);
		ArrayList<UeList> ueLists = new ArrayList<UeList>();
		UeList ueList = new UeList();
		ueList.setAsRelease(13);
		ueList.setUeCategory(6);
		ueList.setForcedCqi(15);
		ueList.setForcedRi(2);
		ueList.setHalfDuplex(false);
		ueList.setSimAlgo("milenage");
		ueList.setImsi("200010001008301");
		ueList.setK("5C95978B5E89488CB7DB44381E237809");
		ueList.setOp("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF");
		ueList.setTunSetupScript("ue-ifup_TCP");
		ueList.setSimEvents(simEvents);
		ueLists.add(ueList);
		ueList = new UeList();
		ueList.setAsRelease(13);
		ueList.setUeCategory(6);
		ueList.setForcedCqi(15);
		ueList.setForcedRi(2);
		ueList.setHalfDuplex(false);
		ueList.setSimAlgo("milenage");
		ueList.setImsi("200010001008302");
		ueList.setK("5C95978B5E89488CB7DB44381E237809");
		ueList.setOp("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF");
		ueList.setTunSetupScript("ue-ifup_TCP");
		ueList.setSimEvents(simEvents);
		ueLists.add(ueList);
		clientEndPoint.configObject.setUeList(ueLists);
		//clientEndPoint.writeConfigFile("CA_2.51G_TCP");

		return clientEndPoint;
	}
}

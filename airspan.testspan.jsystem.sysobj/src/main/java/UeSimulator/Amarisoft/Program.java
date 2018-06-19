package UeSimulator.Amarisoft;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import UE.AmarisoftUE;
import UE.UE;
import UeSimulator.Amarisoft.JsonObjects.Actions.UEAction;
import UeSimulator.Amarisoft.JsonObjects.Actions.UEAction.Actions;
import UeSimulator.Amarisoft.JsonObjects.ConfigFile.Cell;
import UeSimulator.Amarisoft.JsonObjects.ConfigFile.SimEvent;
import UeSimulator.Amarisoft.JsonObjects.ConfigFile.UeList;
import UeSimulator.Amarisoft.JsonObjects.Status.UeStatus;
import jsystem.framework.GeneralEnums;
import systemobject.terminal.SSH;
import systemobject.terminal.Terminal;

public class Program {

	public static AmariSoftServer clientEndPoint;
	
	public static void main(String[] args) throws JsonProcessingException {
		// open websocket
//		Terminal dlMachine = new SSH("192.168.58.148", "swit", "swit_user1");
//		try {
//			dlMachine.connect();
//		} catch (IOException e2) {
//			// TODO Auto-generated catch block
//			e2.printStackTrace();
//		}
//		int port = 9;
//		try {
//			dlMachine.sendString("iperf -s -i 1 -p 500" + port + " -B 91.99.1.240 -f k & \n", true);
//		} catch (IOException e2) {
//			// TODO Auto-generated catch block
//			e2.printStackTrace();
//		} catch (InterruptedException e2) {
//			// TODO Auto-generated catch block
//			e2.printStackTrace();
//		} // start server
		//clientEndPoint = new AmariSoftServer("192.168.58.91", "9002", "root", "SWITswit");
		//clientEndPoint.setConfig();
		//clientEndPoint.setConfigFile("1UE_CA_UDP_SWIT24");
		try
		{
		//clientEndPoint.startServer();
			AmariSoftServer a = new AmariSoftServer();
			a.easyInit();
			a.startServer("automationConfigFile");
			a.getConfig();
		

		
		//sleep(20000);
		//clientEndPoint.startIperfServer(1, 9);
		//clientEndPoint.startTraffic(100, 9);
		//sleep(2000);
//		try {
//			dlMachine.sendString("iperf -c 42.42.101.101 -i 5 -p 500" + port + " -t 99999 & \n", true);
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		} catch (InterruptedException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}

		// add listener
//		clientEndPoint.addMessageHandler(new AmariSoftServer.MessageHandler() {
//			public void handleMessage(String message) {
//				System.out.println("Message recieved: " + message);
//				ObjectMapper mapper = new ObjectMapper();
//
//				// Convert JSON string to Object
//				UeStatus stat = null;
//				try {
//					stat = mapper.readValue(message, UeStatus.class);
//					Double ulRate = stat.getUeList().get(0).getUlBitrate() / 1000;
//					Double dlRate = stat.getUeList().get(0).getDlBitrate() / 1000;
//					String emmState = stat.getUeList().get(0).getEmmState();
//					int ueID = stat.getUeList().get(0).getUeId();
//					System.out.println("UE ID: " + ueID + "\tulRate (kbit): " + ulRate.intValue() + "\tdlRate (kbit): "
//							+ dlRate.intValue() + "\temmState:" + emmState);
//
//				} catch (JsonParseException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} catch (JsonMappingException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//
//			}
//		});
//		String imsi = "20001000100";
//		int imsiEnd = 8300;
//		UE ue = new AmarisoftUE();
//		for (int i = 2; i <= 21; i++) {			
//			ue.setImsi(imsi + (imsiEnd +i ));
//			clientEndPoint.addUe(ue, 13,6,i,0);
//			sleep(100);
//		}
//		//imsiEnd = 9000;
//		for (int i = 22; i <= 41; i++) {		
//			ue.setImsi(imsi + (imsiEnd + i));
//			clientEndPoint.addUe(ue, 13,6,i,1);
//			sleep(100);
//		}
//		for (int i = 1; i <= 64; i++) {				
//			clientEndPoint.uePowerOn(i);
//			sleep(100);
//		}	
//		while (true){
//			try {
//				workUes(clientEndPoint);
//			} catch (Exception e) {
//				long currentTime = System.currentTimeMillis();
//				SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
//				Date resultDate = new Date(currentTime);
//				String formatedTime = sdf.format(resultDate);
//				System.out.println(formatedTime+ " - system crashed, restarting.");
//				sleep(10000);
//				clientEndPoint.startServer();
//			}
//		}
		}
		catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
		} 
		finally {
			clientEndPoint.closeSocket();
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

package testsNG.ProtocolsAndServices.ESon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.xml.datatype.XMLGregorianCalendar;

import Utils.CommonConstants;
import org.junit.Test;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import Attenuators.AttenuatorSet;
import ESon.EsonServer;
import EnodeB.EnodeB;
import EnodeB.Components.EnodeBComponent;
import Netspan.EnbProfiles;
import Netspan.API.Enums.EnbStates;
import Netspan.API.Enums.HandoverType;
import Netspan.API.Enums.HoControlStateTypes;
import Netspan.API.Lte.AlarmInfo;
import Netspan.API.Lte.EventInfo;
import Netspan.Profiles.NetworkParameters;
import Netspan.Profiles.SonParameters;
import UE.UE;
import Utils.GeneralUtils;
import Utils.SetupUtils;
import Utils.SysObjUtils;
import jsystem.framework.ParameterProperties;
import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;
import jsystem.framework.system.SystemManagerImpl;
import testsNG.TestspanTest;
import testsNG.Actions.AlarmsAndEvents;
import testsNG.Actions.EnodeBConfig;
import testsNG.Actions.Neighbors;
import testsNG.Actions.PeripheralsConfig;
import testsNG.Actions.Traffic;

public class P0 extends TestspanTest {

	// Standard Test Parameters
	public EnodeB dut;
	public EnodeB dut2;
	public ArrayList<UE> ues = null;
	public ArrayList<UE> dynamicUEs = null;
	private EnodeBConfig enbConfig;
	private AlarmsAndEvents alarms;
	private EsonServer eSonSession;
	private String lteV6FileName = "lte-net.cfg";
	private String snmpV6FileName = "snmpd.conf";
	private PeripheralsConfig peripheralsConfig;
	private String lastJsonData = "";
	private Traffic trafficGen;
	private Neighbors neighborManager;
	private ArrayList<Integer> pcis = new ArrayList<Integer>();
	private ArrayList<Integer> neighbours = new ArrayList<Integer>();
	private ArrayList<String> ECGIs = new ArrayList<>();
	private AttenuatorSet attenuatorSetUnderTest;
	private UE ueInTestOptions;

	// snmpd.conf static configurations
	private final String rwCommunity = EnodeBComponent.UNSECURED_WRITECOMMUNITY;
	private final String roCommunity = EnodeBComponent.UNSECURED_READCOMMUNITY;
	private final String trapSink = "127.0.0.1 3232";
	private final String agentuser = "root";
	private final String dlmod = "system /bs/asLteSnmp.so";
	private final String ipV6Vlan = "2";
	private final String ipV6Tag = "TAGGED";

	@Override
	public void init() throws Exception {
		GeneralUtils.startLevel("Test init");
		enbInTest = new ArrayList<EnodeB>();
		ues = SetupUtils.getInstance().getAllUEs();
		dynamicUEs = SetupUtils.getInstance().getDynamicUEs();
		enbInTest.add(dut);
		if(dut2 != null){
			enbInTest.add(dut2);
		}
		super.init();
		trafficGen = Traffic.getInstance(SetupUtils.getInstance().getAllUEs());
		enbConfig = EnodeBConfig.getInstance();
		alarms = AlarmsAndEvents.getInstance();
		neighborManager = Neighbors.getInstance();
		peripheralsConfig = PeripheralsConfig.getInstance();
		attenuatorSetUnderTest = AttenuatorSet.getAttenuatorSet(CommonConstants.ATTENUATOR_SET_NAME);
		eSonSession = (EsonServer) SystemManagerImpl.getInstance().getSystemObject("EsonServer");
		GeneralUtils.stopLevel();
	}
	
	
	/**
	 * Test Case - TC-75555
	 */
	@Test
	@TestProperties(name = "Connectivity To eSON Server IPv4", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful", "DUT2" })
	public void connectivity_To_eSON_Server_IPv4() {
		 if (!preTest(dut, true, true, true, true, true, true)) {
		 report.report("configurating profiles failed - finishing Test!",
		 Reporter.FAIL);
		 return;
		 }
		 if (!checkAllRunningAndEvents()) {
		 report.report("there are no Events acknowledge eson Connection");
		 }
		report.report("checking connection to ESon Server");
		if (checkConnectivityToEson()) {
			report.report("DUT is connected to Eson Server!");
		} else {
			report.report("DUT is not connected to Eson Server (" + eSonSession.getServerIp() + ")", Reporter.FAIL);
		}
		GeneralUtils.printToConsole("Testing number of Neighbors prints");
		getNeighboursFromEson(lastJsonData);
		afterTest();
	}

	/**
	 * Test Case - TC-75556
	 */
	@TestProperties(name = "Connectivity To eSON Server IPv6", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void connectivity_To_eSON_Server_IPv6() {
		preTest(dut, true, true, true, true, true, true);
		configFilesIpV6(dut);
	}

	/**
	 * Test Case - TC-75557
	 */
	@Test
	@TestProperties(name = "eNodeB Closing Connectivity to AirHOP Server IPv4", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful", "DUT2" })
	public void eNodeB_Closing_Connectivity_to_AirHOP_Server_IPv4() {
		preTest(dut, true, true, true, true, true, true);
		if (!checkAllRunningAndEvents()) {
			report.report("there are no Events acknowledge eson Connection");
		}
		report.report("checking connection to ESon Server");
		if (checkConnectivityToEson()) {
			report.report("DUT is connected to Eson Server!");
		} else {
			report.report("DUT is not connected to Eson Server (" + eSonSession.getServerIp() + ")", Reporter.FAIL);
		}

		try{
			trafficGen.startTraffic();
		}catch(Exception e){
			report.report("start Traffic hasn't started",Reporter.WARNING);
			e.printStackTrace();
		}
		
		GeneralUtils.unSafeSleep(5 * 1000);
		int numOfUEsInDUT = numberOfConnectedUEsForNode(dut, ues);
		report.report("Number of connected UES: " + numOfUEsInDUT);

		trafficGen.stopTraffic();
		// change network profile and check alarms and events.
		setNetworkProfileToNode(dut);
		report.report("Waiting 1 Minute");
		GeneralUtils.unSafeSleep(60 * 1000);
		checkAlarmsInNMS();

		afterTest();
	}

	/**
	 * Test Case - TC-75558
	 */
	@TestProperties(name = "eNodeB Closing Connectivity to AirHOP Server IPv6", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void eNodeB_Closing_Connectivity_to_AirHOP_Server_IPv6() {
		// about the same only with IPV6
	}

	
	/**
	 * Test Case - TC-75585
	 */
	@Test
	@TestProperties(name = "node should report current capacity class settings to Eson Server", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful", "DUT2" })
	public void reportCurrentCapacitySettings(){
		preTest(dut, true, true, true, true, true, true);
		if (!checkAllRunningAndEvents()) {
			report.report("there are no Events acknowledge eson Connection");
		}
		
		report.report("checking connection to ESon Server");
		if (checkConnectivityToEson()) {
			report.report("DUT is connected to Eson Server!");
		} else {
			report.report("DUT is not connected to Eson Server (" + eSonSession.getServerIp() + ")", Reporter.FAIL);
			report.report("stopping Test!");
			return;
		}
		
		getNeighboursFromEson(lastJsonData);
		//changing MLB and checking ESON -> 70
		GeneralUtils.startLevel("trying to change MLB Capacity Class Value to 70 in NMS");
		setSonProfileToNode(dut,true, true, true, true, true, true,70);
		report.report("waiting 1 minutes");
		GeneralUtils.unSafeSleep(1000 * 60);
		
		String dutECGi = getDutEcgi();
		if(dutECGi.equals("")){
			report.report("no currect value in PCIS in Eson Server!");
			return;
		}
		String dutInfo = eSonSession.GetEcgiInfo(dutECGi);
		Integer capacityValue = JsonPath.read(dutInfo, "$.cell.capacity_class_value");
		checkCapacityValue(capacityValue,70);
		GeneralUtils.stopLevel();
		
		//Out Of service and Checking Eson. -> 255
		GeneralUtils.startLevel("trying to set node to 'Out Of Service' State ");
		peripheralsConfig.changeEnbState(dut, EnbStates.OUT_OF_SERVICE);
		report.report("waiting 1 minutes");
		GeneralUtils.unSafeSleep(1000 * 60);
		
		dutInfo = eSonSession.GetEcgiInfo(dutECGi);
		capacityValue = JsonPath.read(dutInfo, "$.cell.capacity_value");
		checkCapacityValue(capacityValue,255);
		GeneralUtils.stopLevel();
		
		//return to in service and wait
		GeneralUtils.startLevel("trying to set node to 'In Service' State");
		peripheralsConfig.changeEnbState(dut, EnbStates.IN_SERVICE);
		report.report("waiting 1 minutes");
		GeneralUtils.unSafeSleep(1000 * 60);

		//Setting Son profile to Disable CSon and checking for result in Eson Server.
		setSonProfileToNode(dut,true, false, false, false, false, false ,100);
		report.report("waiting 1 minutes after changing Son profile");
		GeneralUtils.unSafeSleep(1000 * 60);
		
		dutInfo = eSonSession.GetEcgiInfo(dutECGi);
		if(!dutInfo.contains("COE is not found!")){
			report.report("there is info in Eson pci - "+dutECGi+ ", while there shouldn't be",Reporter.FAIL);
		}
		GeneralUtils.stopLevel();
		
		afterTest();
	}
	
	

	/**
	 * Test Case - TC-75566
	 */
	@Test
	@TestProperties(name = "number of UEs display in AirHOP server op1", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void number_of_UEs_display_in_AirHOP_server_op1() {
		preTest(dut, true, true, true, true, true, true);
		preTest(dut2, true, true, true, true, true, true);
		number_of_UEs_Base();
		afterTest();
	}

	/**
	 * Test Case - TC-75687
	 */
	@Test
	@TestProperties(name = "number of UEs display in AirHOP server op2", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void number_of_UEs_display_in_AirHOP_server_op2() {
		preTest(dut, true, true, true, true, true, false);
		preTest(dut2, true, true, true, true, true, false);
		number_of_UEs_Base();
		afterTest();
	}

	/**
	 * Test Case - TC-75688
	 */
	@Test
	@TestProperties(name = "number of UEs display in AirHOP server op3", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void number_of_UEs_display_in_AirHOP_server_op3() {
		preTest(dut, true, true, true, true, false, false);
		preTest(dut2, true, true, true, true, false, false);
		number_of_UEs_Base();
		afterTest();

	}

	/**
	 * Test Case - TC-75689
	 */
	@Test
	@TestProperties(name = "number of UEs display in AirHOP server op4", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void number_of_UEs_display_in_AirHOP_server_op4() {
		preTest(dut, true, true, true, true, false, true);
		preTest(dut2, true, true, true, true, false, true);
		number_of_UEs_Base();
		afterTest();

	}

	private void number_of_UEs_Base() {
		HashMap<Integer, Integer> esonStatus = new HashMap<Integer, Integer>();
		attachUEToDUT();
		int numberOfUEsInDUT = 0;
		int numberOfUEsInDUT2 = 0;
		// make only 1 dynamic ue start and make the rest to stop mode.
		enableOnlyOneDynamicUE();
		if (!checkConnectivityToEson()) {
			report.report("No connectivity", Reporter.FAIL);
			reason = "No connectivity To Eson Server";
			report.report("Ending Test in the cause of no connectivity to Eson server");
			return;
		}

		try{
			trafficGen.startTraffic();
		}catch(Exception e){
			report.report("start Traffic hasn't started",Reporter.WARNING);
			e.printStackTrace();
		}
		
		esonStatus = parseLastESonStatus();
		for (Integer nodePci : esonStatus.keySet()) {
			if (nodePci == dut.getPci()) {
				GeneralUtils.printToConsole("node pci: "+nodePci+", set value : "+ Integer.valueOf(esonStatus.get(nodePci)));
				numberOfUEsInDUT = Integer.valueOf(esonStatus.get(nodePci));
			}
			if (nodePci == dut2.getPci()) {
				GeneralUtils.printToConsole("node pci: "+nodePci+", set value : "+ Integer.valueOf(esonStatus.get(nodePci)));
				numberOfUEsInDUT2 = Integer.valueOf(esonStatus.get(nodePci));
			}
		}
		
		lastJsonData = eSonSession.refresh();
		if(HOInit()){
			esonStatus = parseLastESonStatus();
		
			int afterHOUEsInDUT1 = 0;
			int afterHOUEsInDUT2 = 0;

			for (Integer nodePci : esonStatus.keySet()) {
				if (nodePci == dut.getPci()) {
					GeneralUtils.printToConsole("node pci: "+nodePci+", set value : "+ Integer.valueOf(esonStatus.get(nodePci)));
					afterHOUEsInDUT1 = Integer.valueOf(esonStatus.get(nodePci));
				}
				if (nodePci == dut2.getPci()) {
					GeneralUtils.printToConsole("node pci: "+nodePci+", set value : "+ Integer.valueOf(esonStatus.get(nodePci)));
					afterHOUEsInDUT2 = Integer.valueOf(esonStatus.get(nodePci));
				}
			}
			report.report("Results: ");
			report.report("number of UEs before HO - DUT1 : " + numberOfUEsInDUT);
			report.report("number of UEs before HO - DUT2 : " + numberOfUEsInDUT2);
			report.report("number of UEs after HO - DUT1 : " + afterHOUEsInDUT1);
			report.report("number of UEs after HO - DUT2 : " + afterHOUEsInDUT2);

//			if (!(afterHOUEsInDUT1 >= numberOfUEsInDUT + numberOfUEsInDUT2)) {	
//				report.report("Number of UEs is not as it should after HandOver ", Reporter.FAIL);
//			}
			if(checkIfUEUnderTestConnectedToNode(dut)){
				report.report("UE Connected To Dut!");
			}else{
				report.report("UE wiht imsi : "+ueInTestOptions.getImsi()+", is not connected to DUT",Reporter.FAIL);
			}

			if (afterHOUEsInDUT1 == 0) {
				report.report("there are UEs in node : " + dut.getNetspanName(), Reporter.FAIL);
			}
			report.report("Moved Attenuator");
			trafficGen.stopTraffic();
		}else{
			report.report("Ending Test in a Fail since HO Proccess Failed!",Reporter.FAIL);
		}
	}

	private boolean checkIfUEUnderTestConnectedToNode(EnodeB dutTest) {
		boolean result = true;
		if(peripheralsConfig.checkSingleUEConnectionToNode(ueInTestOptions, dutTest)){
			GeneralUtils.printToConsole("UE with Imsi : "+ueInTestOptions.getImsi()+", Connected to node : "+dutTest.getName());
		}else{
			GeneralUtils.printToConsole("UE with Imsi : "+ueInTestOptions.getImsi()+", not Connected to node : "+dutTest.getName());
			result = false;
		}
		return result;
	}


	private void attachUEToDUT() {
		GeneralUtils.printToConsole("Setting Second dut to Out Of Service");
		peripheralsConfig.changeEnbState(dut2, EnbStates.OUT_OF_SERVICE);
		GeneralUtils.printToConsole("waiting 30 seconds");
		GeneralUtils.unSafeSleep(1000*30);
		GeneralUtils.printToConsole("Setting Second dut to In Service");
		peripheralsConfig.changeEnbState(dut2, EnbStates.IN_SERVICE);
	}


	@Test
	@TestProperties(name = "neighbour_Relation_ANR_Enabled", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void neighbour_Relation_ANR_Enabled() {
		preTest(dut, true, true, true, true, true, true);
		preTest(dut2, true, true, true, true, true, true);
		enbInTest.add(dut2);

		if (!checkAllRunningAndEvents()) {
			report.report("there are no Events acknowledge eson Connection");
		}
		report.report("checking connection to ESon Server");

		if (checkConnectivityToEson()) {
			report.report("there connected to eson server");
		} else {
			report.report("DUT is not connected to Eson Server (" + eSonSession.getServerIp() + ")", Reporter.FAIL);
			afterTest();
			return;
		}

	}

	// p2
	@TestProperties(name = "toggling_Connectivity_To_AirHOP_Server_IPv4", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful", "DUT2" })
	public void toggling_Connectivity_To_AirHOP_Server_IPv4() {
		// preTest();

		// connect node to eson
		if (!checkAllRunningAndEvents()) {
			report.report("there are no Events acknowledge eson Connection");
		}
		report.report("checking connection to ESon Server");
		if (checkConnectivityToEson()) {
			report.report("DUT is connected to Eson Server!");
		} else {
			report.report("DUT is not connected to Eson Server (" + eSonSession.getServerIp() + ")", Reporter.FAIL);
		}

		// Disconnect node from eSON server by changing network profile
		report.report("changeing network profile in DUT");
		enbConfig.setProfile(dut, EnbProfiles.Network_Profile, dut.getDefaultNetspanProfiles().getNetwork());

		// checking ue connected to epc
		int numOfUesInDUT = numberOfConnectedUEsForNode(dut, ues);

		// connect to eson server again
		setNetworkProfileToNode(dut);
		if (checkConnectivityToEson()) {
			report.report("DUT is connected to Eson Server!");
		} else {
			report.report("DUT is not connected to Eson Server (" + eSonSession.getServerIp() + ")", Reporter.FAIL);
		}
		report.report("checking that number of ues from eson and from EPC are equals");
		if (!checkUEConnectedWithESONServer(lastJsonData, numOfUesInDUT)) {
			report.report("Number of Ues is not the same in ESon server and EPC", Reporter.FAIL);
		}

		// Disconnect Enode From Eson by changing Son profile
		report.report("setting Disable cson profile to DUT");
		setSonProfileToNode(dut, false, false, false, false, false, false,100);
		// chcking alarms in NMS
		report.report("Waiting 1 Minute");
		GeneralUtils.unSafeSleep(60 * 1000);
		checkAlarmsInNMS();
		// checking ue connection
		try {
			if (!peripheralsConfig.checkIfAllUEsAreConnectedToNode(ues, dut)) {
				report.report("UE is not connected - Fail", Reporter.FAIL);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// setting son profile back to ESON connected
		setSonProfileToNode(dut, true, true, true, true, true, true,100);
		if (checkConnectivityToEson()) {
			report.report("DUT is connected to Eson Server!");
		} else {
			report.report("DUT is not connected to Eson Server (" + eSonSession.getServerIp() + ")", Reporter.FAIL);
		}
		report.report("checking that number of ues from eson and from EPC are equals");
		if (!checkUEConnectedWithESONServer(lastJsonData, numOfUesInDUT)) {
			report.report("Number of Ues is not the same in ESon server and EPC", Reporter.FAIL);
		}
		afterTest();

	}

	@TestProperties(name = "toggling_Connectivity_To_AirHOP_Server_IPv6", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void toggling_Connectivity_To_AirHOP_Server_IPv6() {

	}

	// methods
	private int numberOfConnectedUEsForNode(EnodeB dut, ArrayList<UE> ues) {
		int numOfUesInDUT = -999;
		try {
			if (!peripheralsConfig.checkIfAtLeastOneUEConnectedToNode(ues, dut)) {
				report.report("UE is not connected - Fail", Reporter.FAIL);
			}
			numOfUesInDUT = peripheralsConfig.howManyUEsConnectedToENb(dut);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return numOfUesInDUT;
	}

	public boolean checkAlarmsInNMS() {
		List<AlarmInfo> alarmList = alarmsAndEvents.getAllAlarmsNode(dut);
		for (AlarmInfo alarm : alarmList) {
			if (alarm.alarmInfo.contains("Failed to resume eSON conn")) {
				report.report("Alarm from NMS : " + alarm.alarmInfo);
				return true;
			}
		}
		report.report("No alarms from NMS about ESon losing connection", Reporter.FAIL);
		return false;
	}

	/**
	 * check if numberOfUes should be connected are really connected with Eson
	 * Json last Information.
	 * 
	 * @param lastJsonData
	 */
	private boolean checkUEConnectedWithESONServer(String lastJsonData, int numberOfUesShouldBeConnected) {
		report.report("number of used according to EPC: " + numberOfUesShouldBeConnected);
		ArrayList<String> pcis = JsonPath.read(lastJsonData, "$.coes[*].cell.pci");
		ArrayList<String> numOfUes = JsonPath.read(lastJsonData, "$.coes[*].coe.num_ues");
		String dutPci = "" + dut.getPci();

		for (String pci : pcis) {
			if (pci.equals(dutPci)) {
				if (numberOfUesShouldBeConnected == Integer.valueOf(numOfUes.get(pcis.indexOf(pci)))) {
					report.report("numbers from Eson and from EPC are Equals : " + numberOfUesShouldBeConnected + " = "
							+ numOfUes.get(pcis.indexOf(pci)));
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * check in 5 seconds interval the events containing eson server is
	 * established string maximum time of 30 seconds.
	 * 
	 * @author Shahaf Shuhamy
	 * @return false - if there are no Events containing eson connection or if
	 *         there was an error(only works for netspan 15_1 and higher).
	 */
	private boolean checkAllRunningAndEvents() {
		List<EventInfo> eventsForNode = alarms.getAllEventsNode(dut);
		List<EventInfo> eventsAfterSieving = new ArrayList<EventInfo>();
		try {
			boolean looper = true;
			long time = System.currentTimeMillis();
			report.startLevel("Checking Events for Eson server connection.");
			while (looper) {
				if (eventsForNode == null) {
					report.report("Events are not Available with current netspan");
					return false;
				}
				eventsAfterSieving = returnEventsContainString(eventsForNode, " eSON server is established");
				// if there are some events or 30 seconds have passed.
				if ((eventsAfterSieving.isEmpty()) || (System.currentTimeMillis() - time >= 30 * 1000)) {
					looper = false;
				} else {
					GeneralUtils.unSafeSleep(5 * 1000);
				}
			}
		} catch (Exception e) {
			report.report("Exception getting alarms And Events.");
			return false;
		} finally {
			try {
				report.stopLevel();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return !eventsAfterSieving.isEmpty();
	}

	/**
	 * select all event from list that have the words in the String .
	 * 
	 * @author Shahaf Shuhamy
	 * @param eventsForNode
	 * @param messageToCheck
	 * @return the list events containing the String
	 */
	private List<EventInfo> returnEventsContainString(List<EventInfo> eventsForNode, String messageToCheck) {
		ArrayList<EventInfo> result = new ArrayList<EventInfo>();
		for (EventInfo event : eventsForNode) {
			XMLGregorianCalendar a = event.getReceivedTime();
			GeneralUtils.printToConsole("Day: " + a.getDay() + " hour: " + a.getHour());
			if (event.getEventInfo().contains(messageToCheck)) {
				GeneralUtils.printToConsole(event.getEventInfo());
				result.add(event);
			}
		}
		return result;
	}

	/**
	 * setting string Json type info into String parameter lastJsonData.
	 * 
	 * @return
	 */
	private boolean checkConnectivityToEson() {
		if (eSonSession.openConnection()) {
			if (eSonSession.login()) {
				if (eSonSession.chooseEsonServer()) {
					lastJsonData = eSonSession.refresh();
					return getNodeDetails(lastJsonData);
				}
			} else {
				// login failed
				report.report("Credentials with Eson Server are probably wrong");
				return false;
			}
		} else {
			// no connection
			report.report("Connection to eson server faild");
			return false;
		}
		return false;
	}

	private boolean getNodeDetails(String jsonData) {
		boolean resultStatus = false;
		try {
			GeneralUtils.startLevel("Eson last Info");
			for (EnodeB dut : enbInTest) {

				pcis = JsonPath.read(jsonData, "$.coes[*].cell.pci");
				int dutPci = dut.getPci();

				report.report("Dut pci: " + dutPci);
				report.report("Eson Version: " + JsonPath.read(jsonData, "$.version"));
				report.report("PCIs in Eson Server : " + pcis);

				GeneralUtils.printToConsole("JSON DATA FOR FUTURE USE:" + jsonData);

				for (Integer number : pcis) {
					resultStatus = resultStatus || (dutPci == number);
				}
				return resultStatus;
			}

		} catch (PathNotFoundException e) {
			GeneralUtils.printToConsole("Exception Info : There are no Cells with EnodeBs connected");
		}finally {
			GeneralUtils.stopLevel();
		}
		return false;
	}

	/**
	 * @author Shahaf Shuhamy set Dut enodeB
	 * @return
	 */
	@ParameterProperties(description = "Name of Enodeb Which the test will be run On")
	public void setDUT(String dut) {
		this.dut = (EnodeB) SysObjUtils.getInstnce().initSystemObject(EnodeB.class, false, dut).get(0);
	}

	/**
	 * @author Shahaf Shuhamy get Dut enodeB
	 * @return
	 */
	public String getDUT() {
		return this.dut.getNetspanName();
	}

	/**
	 * @author Shahaf Shuhamy set Dut enodeB
	 * @return
	 */
	@ParameterProperties(description = "Name of Second Enodeb Which the test will be run On")
	public void setDUT2(String dut2) {
		this.dut2 = (EnodeB) SysObjUtils.getInstnce().initSystemObject(EnodeB.class, false, dut2).get(0);
	}

	/**
	 * @author Shahaf Shuhamy get Dut enodeB
	 * @return
	 */
	public String getDUT2() {
		return this.dut2.getNetspanName();
	}

	/**
	 * Initialize ESon parameters and configurating netspan profiles.
	 * 
	 * @author Shahaf Shuhamy
	 */
	private boolean preTest(EnodeB enb, boolean commision, boolean cSon, boolean rach, boolean mcim, boolean mro,
			boolean mlb) {
		GeneralUtils.startLevel("configure Netspan Profiles for node "+enb.getNetspanName());
		if (!(setSonProfileToNode(enb, commision, cSon, rach, mcim, mro, mlb,100)) && (setNetworkProfileToNode(enb))) {
			return false;
		} else {
			setNetworkProfileToNode(enb);
		}
		GeneralUtils.stopLevel();
		return true;
	}

	private void configFilesIpV6(EnodeB node) {
		try {
			report.startLevel("Configurating IPv6 Files in node");
			String ip = node.getIpAddress();
			// if the IP is already IP 6
			if (!ip.contains(":")) {
				report.report("trying to configurate files for ipv6");
				String ipv6 = enbConfig.IPv4ToIPv6Convention(ip);
				if (enbConfig.createIPv6LteConfigFile(node, "1", ipv6, "64", "", ipV6Vlan, ipV6Tag, lteV6FileName)) {
					report.report("lte file successesfully configured");
					report.report("Changing premissions for file lte-net.cfg");
					node.shell("cd /bsdata chmod 777 lte-netV6.cfg");
					// TODO change premissions for a file name.
					if (enbConfig.createIPv6SnmpConfigFile(node, rwCommunity, roCommunity, trapSink, agentuser, dlmod,
							"agentaddress udp6:[" + ipv6 + "]:161", snmpV6FileName)) {
						report.report("snmp file successesfully configured");
						report.report("change Changing premissions for file snmpd.conf");
						// changing premissions for files.
						node.shell("cd /bsdata chmod 777 snmpd.conf");
						printDebugSnmpTables(node);
					} else {
						report.report("could not change snmp file");
					}
				} else {
					report.report("could not change lte file");
				}
			} else {
				report.report("setup is in IPV6 already");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				report.stopLevel();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void printDebugSnmpTables(EnodeB node) {
		String snmpTable = node.shell("/bs/lteCli -c \"db get SnmpCfg\"");
		GeneralUtils.printToConsole(snmpTable);
		snmpTable = node.shell("/bs/lteCli -c \"db get DhcpInfo isSnmpAgentUp\"");
		GeneralUtils.printToConsole(snmpTable);
	}

	/**
	 * setting network profile to connect to eson server.
	 * 
	 * @author Shahaf Shuhamy
	 * @param node
	 * @return
	 */
	private boolean setNetworkProfileToNode(EnodeB node) {
		try {
			report.startLevel("Configure Network profile");
			GeneralUtils.printToConsole("---------------------------------------------------");
			GeneralUtils.printToConsole("| cloning from profile : " + node.defaultNetspanProfiles.getNetwork());
			GeneralUtils.printToConsole("| setting network profile to node : " + node.getNetspanName());
			GeneralUtils.printToConsole(
					"| setting network profile Value: cSonConfig with ip " + eSonSession.getServerIp() + " : 2050");
			GeneralUtils.printToConsole("---------------------------------------------------");
			// Network profile configuration
			NetworkParameters netparams = new NetworkParameters();
			String netProfileName = node.defaultNetspanProfiles.getNetwork();
			netparams.setcSonConfig(true, eSonSession.getServerIp(), 2050);
			enbConfig.cloneAndSetNetworkProfileViaNetSpan(node, netProfileName, netparams);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			try {
				report.stopLevel();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	/**
	 * 
	 * @param node
	 *            - enodeB that should set Son profile into
	 * @param commission
	 *            - enable/Disable
	 * @param cSon
	 *            - enable/Disable
	 * @param rach
	 *            - enable/Disable
	 * @param mcim
	 *            - enable/Disable
	 * @param mro
	 *            - enable/Disable
	 * @param mlb
	 *            - enable/Disable
	 * @return
	 */
	private boolean setSonProfileToNode(EnodeB node, boolean commission, boolean cSon, boolean rach, boolean mcim,
			boolean mro, boolean mlb, int capacityClassValue) {
		try {
			report.startLevel("Configurate Son Profile");
			GeneralUtils.printToConsole("---------------------------------------------------");
			GeneralUtils.printToConsole("| setting Son profile to node : " + node.getNetspanName());
			GeneralUtils.printToConsole("| from profile name : " + node.defaultNetspanProfiles.getSON());
			GeneralUtils.printToConsole("| setting Son profile Value: commission = " + commission);
			GeneralUtils.printToConsole("| setting Son profile Value: cSon = " + cSon);
			GeneralUtils.printToConsole("| setting Son profile Value: rach = " + rach);
			GeneralUtils.printToConsole("| setting Son profile Value: mcim = " + mcim);
			GeneralUtils.printToConsole("| setting Son profile Value: mro = " + mro);
			GeneralUtils.printToConsole("| setting Son profile Value: mlb = " + mlb);
			GeneralUtils.printToConsole("---------------------------------------------------");
			// Son profile configuration
			SonParameters sonParams = new SonParameters();
			String sonProfile = node.defaultNetspanProfiles.getSON();
			sonParams.setSonCommissioning(commission);
			// CentralizedSon, Cson mode, rach,mcim,mro,mlb - list of ints.
			sonParams.setCSonEnabled(cSon);
			sonParams.setcSonRachMode(rach);
			sonParams.setcSonMcimMode(mcim);
			sonParams.setcSonMroMode(mro);
			sonParams.setcSonMlbMode(mlb);
			// inner mlb values
			if (mlb) {
				sonParams.setcSonCapacityClass(capacityClassValue);
				sonParams.setcSonPDSCHLoad(98);
				sonParams.setcSonPUSCHLoad(98);
				sonParams.setcSonRRCLoad(98);
				sonParams.setcSonCPUCHLoad(95);
			}
			report.report("configure Son profile");
			enbConfig.cloneAndSetSonProfileViaNetspan(node, sonProfile, sonParams);
		} catch (Exception e) {
			report.report("Exception configuratin son profile", Reporter.FAIL);
			return false;
		} finally {
			try {
				report.stopLevel();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	/**
	 * Returning profiles to what they were and rebooting dut.
	 */
	private void afterTest() {
		try {
			report.startLevel("after Test");
			if (dut2 != null) {
				enbConfig.setProfile(dut2, EnbProfiles.Son_Profile, dut2.getDefaultNetspanProfiles().getSON());
				enbConfig.setProfile(dut2, EnbProfiles.Network_Profile, dut2.getDefaultNetspanProfiles().getNetwork());
			}
			enbConfig.setProfile(dut, EnbProfiles.Son_Profile, dut.getDefaultNetspanProfiles().getSON());
			enbConfig.setProfile(dut, EnbProfiles.Network_Profile, dut.getDefaultNetspanProfiles().getNetwork());
			enbConfig.deleteClonedProfiles();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				report.stopLevel();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private boolean HOInit() {
			GeneralUtils.startLevel("init HandOver Properties");
			if(neighborManager.addNeighbor(dut, dut2, HoControlStateTypes.ALLOWED,Netspan.API.Enums.X2ControlStateTypes.AUTOMATIC, HandoverType.TRIGGER_X_2, true, "0")){
				report.report("moved attenuator");
			 	peripheralsConfig.preformHO(attenuatorSetUnderTest, attenuatorSetUnderTest.getMinAttenuation(), attenuatorSetUnderTest.getMaxAttenuation());
			 	peripheralsConfig.preformHO(attenuatorSetUnderTest, attenuatorSetUnderTest.getMaxAttenuation(), attenuatorSetUnderTest.getMinAttenuation());
			}else{
				//could not add a neighbor
				report.report("Could not add a neighbor - ending Test");
				return false;
			}
				GeneralUtils.stopLevel();
		return true;
	}

	/**
	 * enable only the first dynamic UE in the list given to the method and the
	 * rest will be in stop. useing local ues list and local dynamicUEs.
	 * 
	 * @return
	 */
	private boolean enableOnlyOneDynamicUE() {
		String dynamicUEName = "";
		UE dynamicUE = null;
		Boolean status = false;

		if (this.dynamicUEs != null) {
			dynamicUE = dynamicUEs.get(0);
			dynamicUEName = dynamicUE.getName();
		}
		
	
		for (UE ue : this.ues) {
			if (ue.getName().equals(dynamicUEName)) {
				ue.start();
				ueInTestOptions = ue;
				status = true;
			} else {
				ue.stop();
			}
		}

		return status;
	}

	/**
	 * returns a map include pcis and numberOfUEs connected to this pci.
	 * 
	 * @return null if there is an error.
	 */
	private HashMap<Integer, Integer> parseLastESonStatus() {
		HashMap<Integer, Integer> currentState = null;
		ArrayList<Integer> pcis = new ArrayList<>();
		ArrayList<Integer> numOfUes = new ArrayList<>();
		Integer sampleNumOfUEs = 0;
		Integer indexOfCurrentPci = -99;

		pcis = JsonPath.read(lastJsonData, "$.coes[*].cell.pci");
		numOfUes = JsonPath.read(lastJsonData, "$.coes[*].coe.num_ues");

		GeneralUtils.printToConsole("Pcis : " + pcis.toString());
		GeneralUtils.printToConsole("num_OfUEs : " + numOfUes.toString());

		if (pcis.size() == numOfUes.size()) {
			currentState = new HashMap<Integer, Integer>();
			for (Integer nodeNumber : pcis) {
				indexOfCurrentPci = pcis.indexOf(nodeNumber);
				sampleNumOfUEs = numOfUes.get(indexOfCurrentPci);
				currentState.put(nodeNumber, sampleNumOfUEs);
			}
		} else {
			report.report("number of Pcis is not equal to number of UEs connected to those Pcis:");
			report.report("Pcis : " + pcis.toString());
			report.report("number of UEs : " + numOfUes.toString());
			return currentState;
		}

		return currentState;
	}

	private void getNeighboursFromEson(String esonData) {
		try {
			pcis = JsonPath.read(esonData, "$.coes[*].cell.pci");
			neighbours = JsonPath.read(esonData, "$.coes[*].cell.coe.num_neighbors");
			ECGIs = JsonPath.read(esonData, "$.coes[*].cell.ecgi");
			GeneralUtils.printToConsole("Testing Print: ");
			GeneralUtils.printToConsole("PCIS : " + pcis);
			GeneralUtils.printToConsole("Neighbors : " + neighbours);
			GeneralUtils.printToConsole("ECGIs : " + ECGIs);
		} catch (Exception e) {
			GeneralUtils.startLevel("Exception");
			e.printStackTrace();
			GeneralUtils.stopLevel();
		}
	}
	
	private void checkCapacityValue(int capacityValue, Integer value) {
		GeneralUtils.printToConsole("Capacity Value : "+capacityValue);
		if(capacityValue == value){
			report.report("Current Cell Capcity : "+capacityValue);
		}else{
			report.report("Current Cell Capcity : "+capacityValue+" and not "+value,Reporter.FAIL);
		}
		
	}

	private String getDutEcgi() {
		int dutPci = dut.getPci();
		int dutIndex = pcis.indexOf(dutPci);
		if(dutIndex < 0){
			GeneralUtils.printToConsole("Error Value! in index under PCIS: "+pcis);
			return "";
		}
		String dutEcgi = ECGIs.get(dutIndex);
		return dutEcgi;
	}
}

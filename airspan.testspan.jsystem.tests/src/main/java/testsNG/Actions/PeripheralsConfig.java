package testsNG.Actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import Attenuators.AttenuatorSet;
import EPC.EPC;
import EnodeB.EnodeB;
import Netspan.NetspanServer;
import Netspan.API.Enums.EnbStates;
import PowerControllers.PowerControllerPort;
import TestingServices.TestConfig;
import UE.UE;
import UE.UeState;
import Utils.GeneralUtils;
import jsystem.framework.report.ListenerstManager;
import jsystem.framework.report.Reporter;

public class PeripheralsConfig {

	private static PeripheralsConfig instance;

	private static final int ATT_SLEEP = 500;
	private static final int ATT_STEP = 2;
	private static final int WAITTIME = 300000;
	Reporter report = ListenerstManager.getInstance();
	NetspanServer netspanServer;
	String[] EnbNetspanNames;
	EPC epc;
	boolean ifShouldRebootUes = false;

	boolean connectionWorking = false;

	private ArrayList<UE> uesNotConnected = new ArrayList<UE>();
	
	private PeripheralsConfig() {
		try {
			netspanServer = NetspanServer.getInstance();
		} catch (Exception e) {
			report.report("Can't load NetspanServer Instance");
			e.printStackTrace();
		}
		try {
			epc = EPC.getInstance();
		} catch (Exception s) {
			report.report("Can't load EPC instance");
			s.printStackTrace();
		}

	}

	public static PeripheralsConfig getInstance() {
		if (instance == null)
			instance = new PeripheralsConfig();
		return instance;
	}

	
	public boolean setAttenuatorSetValue(AttenuatorSet attenuatorSetUnderTest, float attenuationValue) {

		for (int i = 0; i < 3; i++) {
			if(!attenuatorSetUnderTest.setAttenuation(attenuationValue)){
				continue;
			}
			GeneralUtils.unSafeSleep(70);
			float[] attenuatorsValues  = attenuatorSetUnderTest.getAttenuation();
			
			boolean tempStatus = true;
			for (int j = 0; j < attenuatorsValues.length; j++) {
				if (attenuatorsValues[j] != attenuationValue) {
					if(i==2) 
						report.report("Attenuator [" + j + "] has different value than expected value: "+attenuatorsValues[j] + ", expected: "+attenuationValue, Reporter.WARNING);
					
					tempStatus = false;

				} else {
					GeneralUtils.printToConsole("Attenuator [" + j + "]: " + String.valueOf(attenuatorsValues[j]) + "[dB] s");
				}
			}
			if(!tempStatus) continue;
			else return true;

		}		
		return false;
	}
	

	public boolean setAPN(ArrayList<UE> ueList, String apnName){
		boolean action = true;
		boolean actionToReturn = true;
		for (UE ue : ueList) {
			action = true;
			action &= ue.setAPN(apnName);
			if(!action){
				report.report("Failed to set apn to UE "+ue.getName(),Reporter.WARNING);
			}
			action &= ue.stop();
			GeneralUtils.unSafeSleep(5000);
			action &= ue.start();
			actionToReturn &= action;
		}
		return actionToReturn;
	}

	/**
	 * checking if UEs connected to EPC via cli.
	 * 
	 * @param ueList
	 * @return
	 * @throws Exception
	 * @author Shahaf Shuahmy
	 */
	public boolean checkUesConnectionEPC(ArrayList<UE> ueList){
		GeneralUtils.startLevel("EPC Log");
		boolean ret = epc.CheckUeListConnection(ueList);

		ret = ret && checkTestResult(ueList);
		GeneralUtils.stopLevel();
		return ret;
	}

	/**
	 * Following function checks test result and will fail test if one of the
	 * UEs is disconnected
	 * 
	 * @param uesUnderTest
	 *            - UE[] array of UEs under test
	 * @return void
	 * @author shaham
	 * @since 1.4
	 */
	private boolean checkTestResult(ArrayList<UE> uesUnderTest) {
		boolean isConnected = false;
		isConnected = checkUEsState(uesUnderTest);
		if (!isConnected) {
			report.report("One or more UE disconnected or in unknown state");
		} else {
			report.report("All UEs connected");
		}
		return isConnected;
	}

	/**
	 * Following function walk over the UE array and check UE status by reading
	 * name field of UE status property.
	 * 
	 * @param uesUnderTest
	 *            - UE[] array of UEs under test
	 * @return boolean- if all UE connected (true), if one of the UEs is
	 *         disconnected (false)
	 * @author shaham
	 * @since 1.4
	 */
	private boolean checkUEsState(ArrayList<UE> uesUnderTest) {
		boolean status = true;
		for (UE ue : uesUnderTest) {
			if ((ue.getState() == UeState.unknown) || (ue.getState() == UeState.disconnected)) {
				report.report(String.format("Status of [%s] with following IMSI [%s] is [%s]", ue.getName(),
						ue.getImsi(), "UE disconnected or in unknown state"));
				status = false;
				continue;
			}
			report.report(String.format("Status of [%s] with following IMSI [%s] is [%s]", ue.getName(), ue.getImsi(),
					"connected"));
		}

		return status;
	}

	/**
	 * Perform reboot for all UEs under test
	 * 
	 * @param uesUnderTest
	 *            - UE[] array of UEs under test
	 * @return void
	 * @author shaham
	 * @since 1.4
	 */
	public void rebootUE(UE ueUnderTest) {
		ArrayList<UE> uesUnderTest = new ArrayList<>();
		uesUnderTest.add(ueUnderTest);
		rebootUEs(uesUnderTest);
	}
	
	public boolean rebootUEs(ArrayList<UE> uesUnderTest) {
		return rebootUEs(uesUnderTest, 120000);
	}
	public boolean rebootUEs(ArrayList<UE> uesUnderTest, long restartDelay) {
		boolean flag = true;
		try {
			String UE_RestartMethod = TestConfig.getInstace().getUE_Restart();
			if (UE_RestartMethod != null && UE_RestartMethod.equalsIgnoreCase("IPPower")) {
				GeneralUtils.startLevel("Reboot Ues with IP Power");
				report.report("Turning off Ues with IP Power");
				flag &= turnOffAllUEs(uesUnderTest);
				report.report("Waiting 10 seconds for UEs after power off");
				GeneralUtils.unSafeSleep(10000);
				report.report("Turning on Ues with IP Power");
				flag &= turnOnAllUEs(uesUnderTest);
				GeneralUtils.stopLevel();
			}else {
				GeneralUtils.startLevel("Reboot Ues with SNMP command");
				flag &= rebootUEsSNMP(uesUnderTest);
				GeneralUtils.stopLevel();
			}
			report.report("Waiting "+ (restartDelay/1000) +" seconds for UEs after power on");
			flag &= GeneralUtils.unSafeSleep(restartDelay);
		} catch (Exception ex) {
			ex.printStackTrace();
			report.report("Reboot UEs Failed due to: " + ex.getMessage(), Reporter.WARNING);
			GeneralUtils.stopLevel();
			flag = false;
		}
		return flag;
	}
	
	private boolean rebootUEsSNMP(ArrayList<UE> uesUnderTest){
		boolean action = true;
		for (UE ue : uesUnderTest) {
			if(ue.reboot()){
				report.report("UE "+ue.getName()+" was rebooted");
			}else{
				report.report("Failed to reboot UE "+ue.getName(),Reporter.WARNING);
				action = false;
			}
		}
		return action;
	}	
	
	public void preformHO(AttenuatorSet attenuatorSetUnderTest, int attenuatorMin, int attenuatorMax){
		
		GeneralUtils.startLevel("Moving attenuator to create Hand-Over");
		moveAtt(attenuatorSetUnderTest, attenuatorMin,attenuatorMax);
		GeneralUtils.stopLevel();
		report.report("Wait for 5 minutes for Counter to update");
		GeneralUtils.unSafeSleep(WAITTIME);
	}

	private boolean turnOffAllUEs(ArrayList<UE> uesUnderTest) throws NullPointerException, IOException{
		boolean action = true;
		Set<PowerControllerPort> setToOff = new HashSet<PowerControllerPort>();
		for (UE ue : uesUnderTest) {
			PowerControllerPort powerControllerPort = ue.getPowerControllerPort();
			if (powerControllerPort != null)
				setToOff.add(powerControllerPort);
			else {
				report.report("[ERROR]: Could not find port for Ue " + ue.getName().toString(),Reporter.WARNING);
				action = false;
			}
		}

		for (PowerControllerPort pcp : setToOff) {
			if(!pcp.powerOff()){
				action = false;
			}
		}
		return action;
	}

	private boolean turnOnAllUEs(ArrayList<UE> uesUnderTest) throws NullPointerException, IOException{
		boolean action = true;
		Set<PowerControllerPort> setToOn = new HashSet<PowerControllerPort>();
		for (UE ue : uesUnderTest) {
			PowerControllerPort powerControllerPort = ue.getPowerControllerPort();
			if (powerControllerPort != null)
				setToOn.add(powerControllerPort);
			else {
				report.report("[ERROR]: Could not find port for Ue " + ue.getName().toString(),Reporter.WARNING);
				action = false;
			}
		}

		for (PowerControllerPort pcp : setToOn) {
			if(!pcp.powerOn()){
				action = false;
			}
		}
		return action;
	}

	public Integer getUeConnectionPerQCI(NetspanServer netspan, EnodeB enb, int qci) {
		HashMap<Integer, Integer> netspanMapUeConnected = new HashMap<>();
		netspanMapUeConnected = getUeMapFromNetspan(enb);
		if (netspanMapUeConnected == null) {
			return null;
		}
		return netspanMapUeConnected.get(qci);
	}

	public Integer getTotalUeConnection(EnodeB enb) {
		HashMap<Integer, Integer> netspanMapUeConnected = new HashMap<>();
		netspanMapUeConnected = getUeMapFromNetspan(enb);
		if (netspanMapUeConnected == null)
			return null;
		Integer sumUes = new Integer(0);
		for (Integer value : netspanMapUeConnected.values()) {
			sumUes += value;
		}
		return sumUes;
	}

	private HashMap<Integer, Integer> getUeMapFromNetspan(EnodeB enb) {
		return netspanServer.getUeConnectedPerCategory(enb);

	}

	public int checkUesConnectionToEnbsSNMP(EnodeB enb) {
		int currentEnbConnectedUes = 0;
		currentEnbConnectedUes = enb.getUeNumberToEnb();
		return currentEnbConnectedUes;
	}

	/**
	 * checking UEs connections to EnodeBs via netspan(getTotaluUeConnection) if
	 * not success then via SNMP(checkUesConnectionToEnbsSNMP). returns int of
	 * how many are connected. * @Author Shuhamy Shahaf.
	 */

	public int howManyUEsConnectedToENb(EnodeB enb){
		Integer result = 0;
		GeneralUtils.startLevel("SNMP status");
		GeneralUtils.startLevel("Checking UEs total number connected to ENodeBs");
		result += checkUesConnectionToEnbsSNMP(enb);
		report.report("SNMP: Total Number of UEs connected to ENB" + enb.getNetspanName() + " is:" + result);
		GeneralUtils.stopLevel();
		GeneralUtils.stopLevel();
		return result;
	}

	/**
	 * checking if all the ues are connected to node - false if even one UE is not connected.
	 * via snmp + netspan * @Author Shuhamy Shahaf.
	 * 
	 * @param numberOfRecoveryTrys
	 * @param ueList
	 * @param eNBList
	 * @return true if either are true and false of neither.
	 * @throws IOException 
	 * @throws Exception
	 */
	public boolean checkIfAllUEsAreConnectedToNode(ArrayList<UE> ueList, EnodeB enb){
		uesNotConnected = new ArrayList<UE>() ;
		boolean enbCommandStatus = true;
		if(epc == null){
			report.report("EPC is not initalized",Reporter.WARNING);
			return false;
		}
		
		GeneralUtils.startLevel("Checking if the UEs are connected to the ENodeBs by the EPC");
		
		ArrayList<EnodeB> enbList = new ArrayList<EnodeB>();
		enbList.add(enb);
		for (UE ue : ueList) {
			EnodeB currentEnb = epc.getCurrentEnodB(ue, enbList);
			if(currentEnb == null){
				enbCommandStatus = false;
				
				report.report("Checking who's node matches imsi : "+ue.getImsi());
				String connectImsi = epc.getNodeAccordingToImsi(ue.getImsi());
				if(connectImsi != ""){
					report.report(connectImsi);
				}
			}
			if (currentEnb != enb) {
				uesNotConnected.add(ue);
				enbCommandStatus = false;
			}
		}

		if (enbCommandStatus) {
			report.report("UEs connected to enodeB from current list: " + enb.getNetspanName());
			GeneralUtils.stopLevel();
			return true;
		} else {
			report.report("Some of the UEs are not connected to EnodeBs and to EPC");
			GeneralUtils.stopLevel();
			return false;
		}
	}
	
	/**
	 * Checking with EPC if at least one ue is connected to node.
	 * false will be returned if every single ue is not connected.
	 * @param ueList
	 * @param enb
	 * @return
	 */
	public boolean checkIfAtLeastOneUEConnectedToNode(ArrayList<UE> ueList, EnodeB enb){
		boolean atLeastOneUEConnected = false;
		for(UE ue : ueList){
			if(checkSingleUEConnectionToNode(ue, enb)){
				atLeastOneUEConnected = true;
			}
		}
		return atLeastOneUEConnected;
	}
	
	public boolean checkSingleUEConnectionToNode(UE ue, EnodeB enb){
		boolean result = true;
		String not ="";
		if(epc.checkUEConnectedToNode(ue, enb)){
		}else{
			result = false;
			not = "not";
		}
		report.report(String.format("[INFO]: Ue %s with IMSI %s is "+not+" connected to EnodeB %s.",ue.getName(),ue.getImsi(), enb.getName()));
		return result;
	}
	
	public boolean WaitForUEsAndEnodebConnectivity(ArrayList<UE> ueList, EnodeB enb, long timeOut){
		boolean status = false;
		long startTime = System.currentTimeMillis(); // fetch starting time
		
		GeneralUtils.startLevel("Waiting for UEs to connect eNodeB.");
		while ((System.currentTimeMillis() - startTime) < timeOut) {
			if(checkIfAllUEsAreConnectedToNode(ueList, enb)){
				status = true;
				break;
			}
			GeneralUtils.unSafeSleep(5 * 1000);
		}
		GeneralUtils.stopLevel();
		
		return status;
	}
	
	public boolean WaitForUEsAndEnodebDisconnect(ArrayList<UE> ueList, EnodeB enb, long timeOut){
		boolean status = false;
		long startTime = System.currentTimeMillis(); // fetch starting time
		
		GeneralUtils.startLevel("Waiting for UEs to disconnect eNodeB.");
		while ((System.currentTimeMillis() - startTime) < timeOut) {
			if(!checkIfAllUEsAreConnectedToNode(ueList, enb)){
				status = true;
				break;
			}
			GeneralUtils.unSafeSleep(5 * 1000);
		}
		GeneralUtils.stopLevel();
		
		return status;
	}
	
	public boolean epcAndEnodeBsConnection(ArrayList<UE> ueList, ArrayList<EnodeB> enbs) {
		GeneralUtils.startLevel("Check UEs at the ENodeBs");
		boolean res = true;
		for (UE ue : ueList) {
			EnodeB currentEnb = epc.getCurrentEnodB(ue, enbs);
			if (currentEnb == null) {
				ue.setState(UeState.disconnected);
				report.report("UE: " + ue.getName() + " is Disconnected");
				res = false;
			} else {
				ue.setState(UeState.connected);
				report.report("UE: " + ue.getName() + " is connected to: " + currentEnb.getNetspanName());
			}
		}
		GeneralUtils.stopLevel();
		return res;
	}

	/**
	 * method checking if The UE list is connected to any of the currect
	 * EnodeB's via SNMP and by 90% success rate.
	 * 
	 * @Author Shuhamy Shahaf.
	 */
	public boolean verifyEnodeBToUe(ArrayList<UE> currenUeList, EnodeB enb) {
		Integer numberOfUEsConnetedToENB = 0;
		try {
			numberOfUEsConnetedToENB = howManyUEsConnectedToENb(enb);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (numberOfUEsConnetedToENB >= currenUeList.size()){
			report.report(String.format("%s or more UEs connected to %s, verification passed.", currenUeList.size(),
					enb.getName()));
			return true;
		}			
		else{
			report.report(String.format("less then %s UEs connected to %s, verification failed.", currenUeList.size(),
					enb.getName()));
			return false;			
		}
	}

	public boolean changeEnbStateOnlyNetspan(EnodeB enb, EnbStates enbState) {
		// change with netspan
		if (netspanServer.setNodeServiceState(enb, enbState)) {
			report.report("Action change status via Netspan has succeeded: EnodeB " + enb.getNetspanName()
					+ " has been changed to " + enbState);
			return true;
		}
		return false;
	}
	
	public boolean changeEnbState(EnodeB enb, EnbStates enbState) {
		if (enbState == EnbStates.UNKNOWN) {
			report.report("Cant set enodeb state to unknown!");
			return false;
		}
		boolean changedByNetspan = false;
		synchronized (enb.inServiceStateLock){
			try{
				changedByNetspan = netspanServer.setNodeServiceState(enb, enbState);

			}catch(Exception e){
				e.printStackTrace();
				report.report("changeEnbState via netspan failed due to: " + e.getMessage(), Reporter.WARNING);
				changedByNetspan = false;
			}
			// change with netspan
			if (changedByNetspan) {
				report.report("Action change status via Netspan has succeeded: EnodeB " + enb.getNetspanName()
						+ " has been changed to " + enbState);
				
			} else {
				// change with snmp
				report.report("Cannot make action via Netspan: Cannot change " + enb.getNetspanName() + " to " + enbState
						+ " - trying SNMP");
				enb.setOperationalStatus(enbState);
			}	
			GeneralUtils.unSafeSleep(5000);
			EnbStates state = null;
			state = enb.getServiceState();
			if (state.equals(EnbStates.IN_SERVICE)){
				enb.expecteInServiceState = true;
			}
			else 
				enb.expecteInServiceState = false;
			return state == enbState;
		}
		
	}

	/**
	 * @author sshahaf 18/10/2016
	 * @return false if not in idle mode and true if in idle mode = connected to EPC and not to ENB
	 */
	public Boolean checkForIdleMode(EnodeB enb,UE ue){
		Boolean connectToEnb = false;
		Boolean connectToEpc = false;
		try{
		GeneralUtils.startLevel("Checking for Idle Mode");
		ArrayList<UE> uesToCheck = new ArrayList<UE>();
		uesToCheck.add(ue);
		if(checkUesConnectionEPC(uesToCheck)){
			connectToEpc = true;
			report.report("UE is registered only in epc!");
			
			if(checkIfAllUEsAreConnectedToNode(uesToCheck,enb)){
				connectToEnb = true;
				report.report("UE is registered under some ENB ");
			}
		}
		}catch(Exception e){
			e.printStackTrace();
			report.report("Exception In CheckForIdleMode method Check Console");
		}
		finally{
			GeneralUtils.stopLevel();
		}
		if(connectToEpc == true && connectToEnb == false){
			return true;
		}else
			return false;
		
	}

	public ArrayList<UE> getUesNotConnected() {
		return uesNotConnected;
	}

	public void setUesNotConnected(ArrayList<UE> uesNotConnected) {
		this.uesNotConnected = uesNotConnected;
	}
	
	public boolean stopUEsOnlyIpPower(ArrayList<UE> ues){
		boolean action = true;
		GeneralUtils.startLevel("Stop all UEs via IP Power");
		try{
			action &= turnOffAllUEs(ues);
		}catch(Exception e){
			report.report("error trying to turn IP power OFF");
			e.printStackTrace();
			return false;
		}
		report.report("Wait 5 seconds");
		GeneralUtils.unSafeSleep(1000 * 5);
		GeneralUtils.stopLevel();
		return action;
	}
	
	public ArrayList<UE> stopUEsOnlySnmp(ArrayList<UE> ues){
		ArrayList<UE> snmpFailUEs = new ArrayList<UE>();
		GeneralUtils.startLevel("Stop all UEs via snmp");
		for (UE ue : ues) {
			if(!ue.stop()){
				snmpFailUEs.add(ue);
				report.report("UE "+ue.getLanIpAddress()+" has not stopped via snmp");
			}else{
				report.report("UE "+ue.getLanIpAddress()+" has stopped via snmp");
			}
		}
		report.report("Wait 5 seconds");
		GeneralUtils.unSafeSleep(1000 * 5);
		GeneralUtils.stopLevel();
		return snmpFailUEs;
	}
		
	public boolean stopUEs(ArrayList<UE> ues){
		boolean res = true;
		GeneralUtils.startLevel("Stop all UEs");
		ArrayList<UE> snmpFailUEs = stopUEsOnlySnmp(ues);
		if(!snmpFailUEs.isEmpty()){
			report.report("Failed to stop some UEs via snmp. Trying with IP Power");
			res = stopUEsOnlyIpPower(snmpFailUEs);
		}
		GeneralUtils.stopLevel();
		return res;
	}
	
	public boolean startUEsOnlyIpPower(ArrayList<UE> ues){
		boolean action = true;
		GeneralUtils.startLevel("Start all UEs via IP Power");
		try{
			action &= turnOnAllUEs(ues);
		}catch(Exception e){
			report.report("error trying to turn IP power ON");
			e.printStackTrace();
			return false;
		}
		report.report("Wait 2 minutes");
		GeneralUtils.unSafeSleep(2*60*1000);
		GeneralUtils.stopLevel();
		return action;
	}
	
	public ArrayList<UE> startUEsOnlySnmp(ArrayList<UE> ues){
		ArrayList<UE> snmpFailUEs = new ArrayList<UE>();
		GeneralUtils.startLevel("Start all UEs via snmp");
		for (UE ue : ues) {
			if(!ue.start()){
				snmpFailUEs.add(ue);
				report.report("UE "+ue.getLanIpAddress()+" has not started via snmp");
			}else{
				report.report("UE "+ue.getLanIpAddress()+" has started via snmp");
			}
		}
		report.report("Wait 10 seconds");
		GeneralUtils.unSafeSleep(1000 * 10);
		GeneralUtils.stopLevel();
		return snmpFailUEs;
	}
		
	public boolean startUEs(ArrayList<UE> ues){
		boolean res = true;
		GeneralUtils.startLevel("Start all UEs");
		ArrayList<UE> snmpFailUEs = startUEsOnlySnmp(ues);
		if(!snmpFailUEs.isEmpty()){
			report.report("Failed to start some UEs via snmp. Trying with IP Power");
			res = startUEsOnlyIpPower(snmpFailUEs);
		}
		GeneralUtils.stopLevel();
		
		return res;
	}

	
	public void stopStartUes(ArrayList<UE> ueList){
		stopUEs(ueList);
		startUEs(ueList);
	}
	
	public boolean moveAtt(AttenuatorSet attenuatorSetUnderTest, int from, int to) {
		int multi = 1;
		int attenuatorsCurrentValue = from;
		if (from > to)
			multi = -1;
		int steps = Math.abs(from-to)/ATT_STEP;
		for (;steps>=0;steps--){
			long beforeAtt = System.currentTimeMillis();
			if (!this.setAttenuatorSetValue(attenuatorSetUnderTest, attenuatorsCurrentValue)){
				report.report("Failed to set attenuator value");
				return false;
			}
			attenuatorsCurrentValue += (ATT_STEP * multi);
			long afterAtt = System.currentTimeMillis();
			long diff = afterAtt - beforeAtt;
			if (diff < ATT_SLEEP)
				GeneralUtils.unSafeSleep(ATT_SLEEP - diff);
		}
		return true;
	}
	
	public String checkUesConnection(EnodeB dut, ArrayList<UE> UEs,int reporterStatus) {
		String reason = "";		
		GeneralUtils.startLevel("Check UEs connection to eNB");
		EnbStates channelStatus = dut.getServiceState();
		if (channelStatus == EnbStates.UNKNOWN) {
			report.report("enodeB state is Unknown.");
		}
		boolean status = checkIfAllUEsAreConnectedToNode(UEs, dut);
		
		report.report("Ues are " + (status ? "" : "not ") + "connected.");
		
		if ((status == false) && (channelStatus.equals(EnbStates.OUT_OF_SERVICE))) {
			report.report(dut.getNetspanName() + " is Out_Of_Service and UEs wasn't connected", reporterStatus);
			reason = dut.getNetspanName() + " is Out_Of_Service and UEs wasn't connected";
		} else if ((status == false) && (channelStatus.equals(EnbStates.IN_SERVICE))) {
			report.report("Enb is In_Service but one of UEs wasn't connected", reporterStatus);
			reason = dut.getNetspanName() + " is In_Service but one of UEs wasn't connected ";
		}
		GeneralUtils.stopLevel();
		return reason;
	}

	public boolean SetAttenuatorToMin(AttenuatorSet attenuatorSet) {
		return setAttenuatorSetValue(attenuatorSet, attenuatorSet.getMinAttenuation());
	}
	
	public boolean SetAttenuatorToMax(AttenuatorSet attenuatorSet) {
		return setAttenuatorSetValue(attenuatorSet, attenuatorSet.getMaxAttenuation());
	}
	
	public boolean checkConnectionUEWithStopStart(UE ue, EnodeB enb){
		ue.stop();
		GeneralUtils.unSafeSleep(5*1000);
		ue.start();
		GeneralUtils.unSafeSleep(10*1000);
		ArrayList<UE> ueList = new ArrayList<UE>();
		ueList.add(ue);
		boolean connected = checkIfAllUEsAreConnectedToNode(ueList, enb);
		return connected;
	}

	public boolean waitForUeToConnect(UE ue, ArrayList<EnodeB> enbInTest, int ueConnectTimeout) {
		boolean result = false;
		long timeStamp = System.currentTimeMillis();
		boolean timeLoopCondition = true;
		while(timeLoopCondition){
			//timeout loop condition.
			if(System.currentTimeMillis() - timeStamp > ueConnectTimeout){
				timeLoopCondition = false;
				result = false;
			}
			//if ue is Connected.
			if(isUEConnected(ue,enbInTest)){
				timeLoopCondition = false;
				result = true;
			}
			
			GeneralUtils.unSafeSleep(5 * 1000);
		}
		return result;
		
	}
	
	public boolean isUEConnected(UE ue,ArrayList<EnodeB> node){
		EnodeB currentEnb = epc.getCurrentEnodB(ue, node);
		if (currentEnb !=null)
			return true; 
		return false;
	}
}

package testsNG.ProtocolsAndServices.BulkStatus;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import org.junit.Test;

import EnodeB.EnodeB;
import EnodeB.ProtoBuf.PbLteStatusOuterClass.PbLteAnrStatus;
import EnodeB.ProtoBuf.PbLteStatusOuterClass.PbLteCellStatus;
import EnodeB.ProtoBuf.PbLteStatusOuterClass.PbLteMmeStatus;
import EnodeB.ProtoBuf.PbLteStatusOuterClass.PbLteNwElementStatus;
import EnodeB.ProtoBuf.PbLteStatusOuterClass.PbLteRfStatus;
import Netspan.EnbProfiles;
import Netspan.Profiles.NetworkParameters;
import Utils.GeneralUtils;
import Utils.SysObjUtils;
import jsystem.framework.ParameterProperties;
import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;
import testsNG.TestspanTest;
import testsNG.Actions.EnodeBConfig;

public class Progression extends TestspanTest{
	private EnodeB dut;
	private EnodeBConfig eNodeBConfig;
	final int CALL_TRACE_SERVER_NETWORK_TYPE = 2;
	final int FIRST_RF_PATH_ID = 1;
	
	/********************************* INFRA *********************************/
	
	@Override
	public void init() throws Exception {
		eNodeBConfig = EnodeBConfig.getInstance();
		enbInTest = new ArrayList<>();
		enbInTest.add(dut);
		super.init();
		GeneralUtils.startLevel("Test Init");
		dut.deleteAllNeighborsByCli();
		GeneralUtils.stopLevel();
	}

	@Override
	public void end(){
		dut.deleteAllNeighborsByCli();
		super.end();
	}
	
	@ParameterProperties(description = "DUT")
	public void setDut(String dut) {
		GeneralUtils.printToConsole("Load DUT " + dut);
		this.dut = (EnodeB) SysObjUtils.getInstnce().initSystemObject(EnodeB.class, false, dut).get(0);
		GeneralUtils.printToConsole("DUT loaded" + this.dut.getName());
	}
	
	/********************************* TESTS *********************************/
	
	@Test
	@TestProperties(name = "Bulk - Test the limits of NrtChanges field (16 bits).", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void testTheLimitsOfNrtChangesByProtocolBuffer(){
		
		int nrtChanges = dut.getNrtChanges();
		
		report.report("NrtChanges = " + nrtChanges);
		
		GeneralUtils.startLevel("Add and remove 2^15 neighbors for increasing NrtChanges counter.");
		
		final int NUMBER_2SQRT15 = 35000;
		final int NUMBER_OF_NEIGHBORS_EACH_TIME = 200;
		int numberOfIterations = ((int)(NUMBER_2SQRT15/(NUMBER_OF_NEIGHBORS_EACH_TIME*2)));
		for(int i = 1; i <= numberOfIterations; i++){
			try {
				dut.add3PartyNbrs(NUMBER_OF_NEIGHBORS_EACH_TIME, 1, 1, null, null);
				GeneralUtils.unSafeSleep(5000);
				dut.remove3PartyNbrs(NUMBER_OF_NEIGHBORS_EACH_TIME, 1, 1);
				GeneralUtils.unSafeSleep(5000);
			} catch (IOException e) {
				report.report("Failed Add/Remove Neighbor.", Reporter.WARNING);
				e.printStackTrace();
				break;
			}
			nrtChanges = dut.getNrtChanges();
			report.report("NrtChanges = " + nrtChanges);
			if(nrtChanges > NUMBER_2SQRT15){break;}
		}
		
		GeneralUtils.stopLevel();
		
		nrtChanges = dut.getNrtChanges();
		
		if(nrtChanges > NUMBER_2SQRT15){
			report.report("Succeeded to raise NrtChanges more than 2^15 (NrtChanges = " + nrtChanges + ").");
		}else{
			report.report("Failed to raise NrtChanges more than 2^15 (NrtChanges = " + nrtChanges + ").", Reporter.FAIL);
		}
		
		GeneralUtils.startLevel("Add and remove another 2^15 neighbors for increasing NrtChanges counter beyond his limit (16 bits) for initializing it.");
		
		numberOfIterations = ((int)(NUMBER_2SQRT15/(NUMBER_OF_NEIGHBORS_EACH_TIME*2)));
		for(int i = 1; i <= numberOfIterations; i++){
			try {
				dut.add3PartyNbrs(NUMBER_OF_NEIGHBORS_EACH_TIME, 1, 1, null, null);
				GeneralUtils.unSafeSleep(5000);
				dut.remove3PartyNbrs(NUMBER_OF_NEIGHBORS_EACH_TIME, 1, 1);
				GeneralUtils.unSafeSleep(5000);
			} catch (IOException e) {
				report.report("Failed Add/Remove Neighbor.", Reporter.WARNING);
				e.printStackTrace();
				break;
			}
			nrtChanges = dut.getNrtChanges();
			report.report("NrtChanges = " + nrtChanges);
			if(nrtChanges <= 1000){break;}
		}
		
		GeneralUtils.stopLevel();
		
		nrtChanges = dut.getNrtChanges();
		
		if(nrtChanges <= 1000){
			report.report("Succeeded to initialize NrtChanges counter (NrtChanges = " + nrtChanges + ").");
		}else{
			report.report("Failed to initialize NrtChanges counter (NrtChanges = " + nrtChanges + ").", Reporter.FAIL);
		}
	}
	
	@Test
	@TestProperties(name = "Bulk - Make changes in the relevent tables and verify counters increment.", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void change(){
		NrtChangesParams nrtChangesParams = make3NrtChanges(dut);
		GeneralUtils.unSafeSleep(3000);
		if(verifyNrtChangesViaProtocolBufferAndUndoChanges(dut, nrtChangesParams) == false){
			reason += "FAILED to verify NrtChanges Via Protocol Buffer.";
		}
		
		CellChangesParams cellChangesParams = make3CellChanges(dut);
		GeneralUtils.unSafeSleep(3000);
		if(verifyCellChangesViaProtocolBufferAndUndoChanges(dut, cellChangesParams) == false){
			reason += "FAILED to verify CellChanges Via Protocol Buffer.";
		}
		
		NetworkElementChangesParams networkElementChangesParams = make3NetworkElementChanges(dut);
		GeneralUtils.unSafeSleep(3000);
		if(verifyNetworkElementChangesViaProtocolBufferAndUndoChanges(dut, networkElementChangesParams) == false){
			reason += "FAILED to verify NetworkElementChanges Via Protocol Buffer.";
		}
		
		MmeChangesParams mmeChangesParams = make3MmeChanges(dut);
		GeneralUtils.unSafeSleep(3000);
		if(verifyMmeChangesViaProtocolBufferAndUndoChanges(dut, mmeChangesParams) == false){
			reason += "FAILED to verify MmeChanges Via Protocol Buffer.";
		}
		
		RfChangesParams rfChangesParams = make3RfChanges(dut);
		GeneralUtils.unSafeSleep(3000);
		if(verifyRfChangesViaProtocolBufferAndUndoChanges(dut, rfChangesParams) == false){
			reason += "FAILED to verify RfChanges Via Protocol Buffer.";
		}
	}
	
	/********************************* HELPER ********************************/
	
	private NrtChangesParams make3NrtChanges(EnodeB eNodeB){
		NrtChangesParams nrtChangesParams = new NrtChangesParams();
		nrtChangesParams.mnoBroadcastPlmn = 1;
		nrtChangesParams.eutranCellId = 1;
		
		eNodeB.setNrtChanges(0);
		
		GeneralUtils.startLevel("Make 3 NrtChanges.");
		
		report.report("Add Nieghbor.");
		nrtChangesParams.enbType = 1;
		report.report("Set eNodeB type to " + nrtChangesParams.enbType + " (Home eNodeB).");
		
		nrtChangesParams.qOffsetCell = 7;
		report.report("Set qOffseteCell " + nrtChangesParams.qOffsetCell + ".");
		
		try {
			eNodeB.add3PartyNbrs(1, 1, 1, nrtChangesParams.enbType, nrtChangesParams.qOffsetCell);
		} catch (IOException e) {
			report.report("Failed Add Neighbor.", Reporter.WARNING);
			e.printStackTrace();
		}
		GeneralUtils.unSafeSleep(3000);
		
		GeneralUtils.stopLevel();
		
		return nrtChangesParams;
	}
	
	private boolean verifyNrtChangesViaProtocolBufferAndUndoChanges(EnodeB eNodeB, NrtChangesParams nrtChangesParams){
		boolean result = true;
		int numberOfChangesTookEffect = 0;
		
		GeneralUtils.startLevel("Verify NrtChanges set successfully via Protocol Buffer.");
		
		PbLteAnrStatus pbLteAnrStatus = eNodeB.getPbLteSAnrStatus(nrtChangesParams.mnoBroadcastPlmn, nrtChangesParams.eutranCellId);
		if(pbLteAnrStatus == null){
			report.report("Failed to get AnrStatus.", Reporter.FAIL);
			result = false;
		}else{
			report.report("Nieghbor was added successfully.");
			numberOfChangesTookEffect+=2;
			
			if(eNodeB.getNghListEnbType(nrtChangesParams.mnoBroadcastPlmn, nrtChangesParams.eutranCellId) != nrtChangesParams.enbType){
				report.report("Failed to set eNodeB Type.", Reporter.WARNING);
			}
			else if(pbLteAnrStatus.getNbEnbType() != nrtChangesParams.enbType){
				report.report("EnbType equals "+nrtChangesParams.enbType+", while EnbType equals "+pbLteAnrStatus.getNbEnbType()+" according to Protocol Buffer.", Reporter.FAIL);
				numberOfChangesTookEffect++;
			}else{
				report.report("eNodeB Type was seted successfully.");
				numberOfChangesTookEffect++;
			}
			if(eNodeB.getNghListQOffsetCell(nrtChangesParams.mnoBroadcastPlmn, nrtChangesParams.eutranCellId) != nrtChangesParams.qOffsetCell){
				report.report("Failed to set QOffsetCell.", Reporter.WARNING);
			}else if(pbLteAnrStatus.getQOffsetRange() != nrtChangesParams.qOffsetCell){
				report.report("QOffsetCell equals "+nrtChangesParams.qOffsetCell+", while QOffsetCell equals "+pbLteAnrStatus.getQOffsetRange()+" according to Protocol Buffer.", Reporter.FAIL);
				numberOfChangesTookEffect++;
			}else{
				report.report("QOffsetCell was seted successfully.");
				numberOfChangesTookEffect++;
			}
		}

		int nrtChanges = eNodeB.getNrtChanges();
		if(numberOfChangesTookEffect == 0 ){
			report.report("Zero changes took effect.", Reporter.FAIL);
			result = false;
		}else if(nrtChanges != numberOfChangesTookEffect){
			report.report("NrtChanges counter NOT EQUALS the number of changes that took effect (NrtChanges="+nrtChanges+", NumberOfChangesThatTookEffect="+numberOfChangesTookEffect+").", Reporter.FAIL);
			result = false;
		}else{
			report.report("NrtChanges counter EQUALS the number of changes that took effect (NrtChanges="+nrtChanges+", NumberOfChangesThatTookEffect="+numberOfChangesTookEffect+").");
		}
		report.report("Delete Neighbor.");
		eNodeB.remove3PartyNbrs(1, 1, 1);
		GeneralUtils.stopLevel();
		return result;
	}
	
	private CellChangesParams make3CellChanges(EnodeB eNodeB){
		CellChangesParams cellChangesParams = new CellChangesParams(eNodeB.getPbLteCellStatus(40), 40);
		cellChangesParams.rsiValue = cellChangesParams.pbLteCellStatusOriginalValues != null && cellChangesParams.pbLteCellStatusOriginalValues.getRsiValue() != 7 ? 7 : 8;
		cellChangesParams.mlbStatus = cellChangesParams.pbLteCellStatusOriginalValues != null && cellChangesParams.pbLteCellStatusOriginalValues.getMlbStatus() != 0 ? false : true;
		
		eNodeB.setCellChanges(0);
		
		GeneralUtils.startLevel("Make 2 CellChanges.");
		
		report.report("Set RsiValue to " + cellChangesParams.rsiValue + ".");
		eNodeBConfig.setAutoRsiCellRsiValue(eNodeB, cellChangesParams.rsiValue);
		
		report.report("Set Mlb Status to " + cellChangesParams.mlbStatus + ".");
		eNodeBConfig.setsonCellCfgMlbActive(eNodeB, GeneralUtils.booleanToInteger(cellChangesParams.mlbStatus));
		
		GeneralUtils.stopLevel();
		
		return cellChangesParams;
	}
	
	private boolean verifyCellChangesViaProtocolBufferAndUndoChanges(EnodeB eNodeB, CellChangesParams cellChangesParams){
		boolean result = true;
		int numberOfChangesTookEffect = 0;
		
		GeneralUtils.startLevel("Verify CellChanges set successfully via Protocol Buffer.");
		
		PbLteCellStatus pbLteCellStatus = eNodeB.getPbLteCellStatus(cellChangesParams.cellNumber);

		if(pbLteCellStatus == null){
			report.report("Failed to get CellStatus.", Reporter.FAIL);
			result = false;
		}else{
			if(eNodeBConfig.getAutoRsiCellRsiValue(eNodeB) != cellChangesParams.rsiValue){
				report.report("Failed to set RsiValue.", Reporter.WARNING);
			}else if(pbLteCellStatus.getRsiValue() != cellChangesParams.rsiValue){
				report.report("RsiValue equals "+cellChangesParams.rsiValue+", while RsiValue equals "+pbLteCellStatus.getRsiValue()+" according to Protocol Buffer.", Reporter.FAIL);
				numberOfChangesTookEffect++;
			}else{
				report.report("RsiValue was seted successfully.");
				numberOfChangesTookEffect++;
			}
			
			if (eNodeBConfig.getsonCellCfgMlbActive(eNodeB) != GeneralUtils.booleanToInteger(cellChangesParams.mlbStatus)){
				report.report("Failed to set MlbStatus.", Reporter.WARNING);
			}else if (pbLteCellStatus.getMlbStatus() != GeneralUtils.booleanToInteger(cellChangesParams.mlbStatus)){
				report.report("MlbActive equals "+GeneralUtils.booleanToInteger(cellChangesParams.mlbStatus)+", while MlbActive equals "+pbLteCellStatus.getMlbStatus()+" according to Protocol Buffer.", Reporter.FAIL);
				numberOfChangesTookEffect++;
			}else{
				report.report("MlbStatus was seted successfully.");
				numberOfChangesTookEffect++;
			}
		}

		int CellChanges = eNodeB.getCellChanges();
		if(numberOfChangesTookEffect == 0 ){
			report.report("Zero changes took effect.", Reporter.FAIL);
			result = false;
		}else if(CellChanges != numberOfChangesTookEffect){
			report.report("CellChanges counter NOT EQUALS the number of changes that took effect (CellChanges="+CellChanges+", NumberOfChangesThatTookEffect="+numberOfChangesTookEffect+").", Reporter.FAIL);
			result = false;
		}else{
			report.report("CellChanges counter EQUALS the number of changes that took effect (CellChanges="+CellChanges+", NumberOfChangesThatTookEffect="+numberOfChangesTookEffect+").");
		}
		
		report.report("Set original values back.");
		if(cellChangesParams.pbLteCellStatusOriginalValues != null){
			eNodeBConfig.setAutoRsiCellRsiValue(eNodeB, cellChangesParams.pbLteCellStatusOriginalValues.getRsiValue());
			eNodeBConfig.setsonCellCfgMlbActive(eNodeB, cellChangesParams.pbLteCellStatusOriginalValues.getMlbStatus());
		}
		GeneralUtils.stopLevel();
		
		return result;
	}
	
	private NetworkElementChangesParams make3NetworkElementChanges(EnodeB eNodeB){
		NetworkElementChangesParams networkElementChangesParams = new NetworkElementChangesParams(eNodeB.getPbLteNetworkElementStatus(CALL_TRACE_SERVER_NETWORK_TYPE), CALL_TRACE_SERVER_NETWORK_TYPE);
		
		eNodeB.setNetworkElementChanges(0);
		
		GeneralUtils.startLevel("Make 1 NetworkElementChange.");
		
		networkElementChangesParams.nwElementIpAddress = 
				networkElementChangesParams.pbLteNwElementStatusOriginalValues != null && 
				networkElementChangesParams.pbLteNwElementStatusOriginalValues.getIpAddress().equals("7.7.7.7") ? "8.8.8.8" : "7.7.7.7";
		report.report("Set nwElementIpAddress to " + networkElementChangesParams.nwElementIpAddress + ".");
		eNodeB.provisionStartEvent();
		try {
			eNodeB.setCallTraceEnbCfgTraceServerIpAddr(InetAddress.getByName(networkElementChangesParams.nwElementIpAddress));
		} catch (UnknownHostException e) {
			report.report("InetAddress: failed to parse IP Address.", Reporter.WARNING);
			e.printStackTrace();
		}
		eNodeB.provisionEndedEvent();
		GeneralUtils.stopLevel();
		
		return networkElementChangesParams;
	}

	private boolean verifyNetworkElementChangesViaProtocolBufferAndUndoChanges(EnodeB eNodeB, NetworkElementChangesParams networkElementChangesParams){
		boolean result = true;
		int numberOfChangesTookEffect = 0;
		
		GeneralUtils.startLevel("Verify NetworkElementChanges set successfully via Protocol Buffer.");
		
		PbLteNwElementStatus pbLteNwElementStatus = eNodeB.getPbLteNetworkElementStatus(networkElementChangesParams.nwElementType);
		if(pbLteNwElementStatus == null){
			report.report("Failed to get NetworkElementStatus.", Reporter.FAIL);
			result = false;
		}else{
			if(!eNodeB.getCallTraceEnbCfgTraceServerIpAddr().equals(networkElementChangesParams.nwElementIpAddress)){
				report.report("Failed to set eNodeB Type.", Reporter.WARNING);
			}else if(!pbLteNwElementStatus.getIpAddress().equals(networkElementChangesParams.nwElementIpAddress)){
				report.report("nwElementIpAddress equals "+networkElementChangesParams.nwElementIpAddress+", while nwElementIpAddress equals "+pbLteNwElementStatus.getIpAddress()+" according to Protocol Buffer.", Reporter.FAIL);
				numberOfChangesTookEffect++;
			}else{
				report.report("nwElementIpAddress was seted successfully.");
				numberOfChangesTookEffect++;
			}
			
		}

		int networkElementChanges = eNodeB.getNetworkElementChanges();
		if(numberOfChangesTookEffect == 0){
			report.report("Zero changes took effect.", Reporter.FAIL);
			result = false;
		}else if(networkElementChanges != numberOfChangesTookEffect){
			report.report("NetworkElementChanges counter NOT EQUALS the number of changes that took effect (NetworkElementChanges="+networkElementChanges+", NumberOfChangesThatTookEffect="+numberOfChangesTookEffect+").", Reporter.FAIL);
			result = false;
		}else{
			report.report("NetworkElementChanges counter EQUALS the number of changes that took effect (NetorkElementChanges="+networkElementChanges+", NumberOfChangesThatTookEffect="+numberOfChangesTookEffect+").");
		}

		report.report("Set original values back.");
		if(networkElementChangesParams.pbLteNwElementStatusOriginalValues != null){
			eNodeB.provisionStartEvent();
			try {
				eNodeB.setCallTraceEnbCfgTraceServerIpAddr(InetAddress.getByName(networkElementChangesParams.pbLteNwElementStatusOriginalValues.getIpAddress() ));
			} catch (UnknownHostException e) {
				report.report("InetAddress: failed to parse IP Address.", Reporter.WARNING);
				e.printStackTrace();
			}
			eNodeB.provisionEndedEvent();
		}
		GeneralUtils.stopLevel();
		
		return result;
	}
	
	MmeChangesParams make3MmeChanges(EnodeB eNodeB){
		MmeChangesParams mmeChangesParams = new MmeChangesParams(eNodeB.getPbLteMmeStatus(eNodeB.getMmeConfigIpAddress()));
		mmeChangesParams.ipAddr = mmeChangesParams.pbLteMmeStatusOriginalValues != null && mmeChangesParams.pbLteMmeStatusOriginalValues.getGatewayIpAddress().equals("7.7.7.7") ? "8.8.8.8" : "7.7.7.7";
		mmeChangesParams.s1CVlanInfo = mmeChangesParams.pbLteMmeStatusOriginalValues != null && mmeChangesParams.pbLteMmeStatusOriginalValues.getVlanId() != 7 ? 7 : 8;
		
		eNodeB.setMmeChanges(0);
		
		GeneralUtils.startLevel("Make 2 MmeChanges.");
		
		report.report("Set MmeIpAddress to " + mmeChangesParams.ipAddr + ".");
		
		NetworkParameters networkParameters = new NetworkParameters();
		networkParameters.setMMEIP(mmeChangesParams.ipAddr);
		eNodeBConfig.cloneAndSetProfileViaNetSpan(eNodeB, eNodeB.defaultNetspanProfiles.getNetwork(), networkParameters);
		
		eNodeB.reboot();
		report.report("Wait for SNMP to be available.");
		GeneralUtils.unSafeSleep(60 * 1000);
		eNodeB.waitForSnmpAvailable(1000 * 60 * 5);
		GeneralUtils.unSafeSleep(3 * 60 * 1000);
		
		GeneralUtils.stopLevel();
		
		return mmeChangesParams;
	}
	
	private boolean verifyMmeChangesViaProtocolBufferAndUndoChanges(EnodeB eNodeB, MmeChangesParams mmeChangesParams){
		boolean result = true;
		int numberOfChangesTookEffect = 0;
		
		GeneralUtils.startLevel("Verify MmeChanges set successfully via Protocol Buffer.");
		
		PbLteMmeStatus pbLteMmeStatus = eNodeB.getPbLteMmeStatus(mmeChangesParams.ipAddr);
		
		if(pbLteMmeStatus == null){
			report.report("Failed to get MmeStatus by the new IP = "+mmeChangesParams.ipAddr+".", Reporter.FAIL);
			result = false;
			
			if(mmeChangesParams.pbLteMmeStatusOriginalValues != null){
				pbLteMmeStatus = eNodeB.getPbLteMmeStatus(mmeChangesParams.pbLteMmeStatusOriginalValues.getIpAddress());
			}
			if(pbLteMmeStatus != null){
				report.report("Found MmeStatus by the old IP = "+mmeChangesParams.pbLteMmeStatusOriginalValues.getIpAddress()+".", Reporter.FAIL);
			}
		}
		
		if(pbLteMmeStatus == null){ 
			report.report("Failed to get MmeStatus.", Reporter.FAIL);
			result = false;
		}else{
			if(!eNodeB.getMmeStatusIpAddress().equals(mmeChangesParams.ipAddr)){
				report.report("Failed to set MmeStatusIpAddress.", Reporter.WARNING);
			}else if(!pbLteMmeStatus.getIpAddress().equals(mmeChangesParams.ipAddr)){
				report.report("mmeIpAddress equals "+mmeChangesParams.ipAddr+", while mmeIpAddress equals "+pbLteMmeStatus.getIpAddress()+" according to Protocol Buffer.", Reporter.FAIL);
				numberOfChangesTookEffect+=2;
			}else{
				report.report("mmeIpAddress was seted successfully.");
				numberOfChangesTookEffect+=2;
			}
		}

		int mmeChanges = eNodeB.getMmeChanges();
		if(numberOfChangesTookEffect == 0 ){
			report.report("Zero changes took effect.", Reporter.FAIL);
			result = false;
		}else if(mmeChanges != numberOfChangesTookEffect){
			report.report("MmeChanges counter NOT EQUALS the number of changes that took effect (mmeChanges="+mmeChanges+", NumberOfChangesThatTookEffect="+numberOfChangesTookEffect+").", Reporter.FAIL);
			result = false;
		}else{
			report.report("MmeChanges counter EQUALS the number of changes that took effect (mmeChanges="+mmeChanges+", NumberOfChangesThatTookEffect="+numberOfChangesTookEffect+").");
		}
		
		report.report("Set original values back.");
		eNodeBConfig.setProfile(eNodeB, EnbProfiles.Network_Profile, eNodeB.getDefaultNetspanProfiles().getNetwork());
		eNodeBConfig.deleteClonedProfiles();

		eNodeB.reboot();
		report.report("Wait for ALL RUNNINNG.");
		GeneralUtils.unSafeSleep(60 * 1000);
		eNodeB.waitForAllRunning(EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
		
		GeneralUtils.stopLevel();
		
		return result;
	}
	
	RfChangesParams make3RfChanges(EnodeB eNodeB){
		RfChangesParams rfChangesParams = new RfChangesParams(eNodeB.getPbLteRfStatus(FIRST_RF_PATH_ID), eNodeB.getTxPower());
		rfChangesParams.txPower = rfChangesParams.txPowerOriginalValue != 7 ? 7 : 8;
		
		eNodeB.setRfChanges(0);
		
		GeneralUtils.startLevel("Make RfChanges.");
		
		report.report("Set txPower to " + rfChangesParams.txPower + ".");
		eNodeB.setTxPower(rfChangesParams.txPower);
		
		GeneralUtils.stopLevel();
		
		return rfChangesParams;
	}
	
	private boolean verifyRfChangesViaProtocolBufferAndUndoChanges(EnodeB eNodeB, RfChangesParams rfChangesParams){
		boolean result = true;
		int numberOfChangesTookEffect = 0;
		
		GeneralUtils.startLevel("Verify RfChanges set successfully via Protocol Buffer.");
		
		PbLteRfStatus pbLteRfStatus = eNodeB.getPbLteRfStatus(FIRST_RF_PATH_ID);

		if(pbLteRfStatus == null){
			report.report("Failed to get RfChanges.", Reporter.FAIL);
			result = false;
		}else{
			if(eNodeB.getTxPower() == rfChangesParams.txPower){
				report.report("txPower was seted successfully.");
				numberOfChangesTookEffect++;
			}else{
				report.report("Failed to set txPower.", Reporter.WARNING);
			}
		}

		int rfChanges = eNodeB.getRfChanges();
		if(numberOfChangesTookEffect == 0){
			report.report("Zero changes took effect.", Reporter.FAIL);
			result = false;
		}else if(rfChanges < numberOfChangesTookEffect){
			report.report("RfChanges counter Lower Than the number of changes that took effect (RfChanges="+rfChanges+", NumberOfChangesThatTookEffect="+numberOfChangesTookEffect+").", Reporter.FAIL);
			result = false;
		}else{
			report.report("RfChanges counter Greater Than the number of changes that took effect (RfChanges="+rfChanges+", NumberOfChangesThatTookEffect="+numberOfChangesTookEffect+").");
		}
		
		report.report("Set original values back.");
		eNodeB.setTxPower(rfChangesParams.txPowerOriginalValue);
		
		GeneralUtils.stopLevel();
		
		return result;
	}
	
	/********************************* HELPER CLASSES ********************************/
	
	private class NrtChangesParams{
		public long mnoBroadcastPlmn;
		public long eutranCellId;
		public int enbType;
		public int qOffsetCell;
	}
	
	private class CellChangesParams{
		public final PbLteCellStatus pbLteCellStatusOriginalValues;
		public final int cellNumber;
		public int rsiValue;
		public boolean autoPci;
		public boolean mlbStatus;
		
		CellChangesParams(PbLteCellStatus pbLteCellStatus, int cellNumber){
			this.pbLteCellStatusOriginalValues = pbLteCellStatus;
			this.cellNumber = cellNumber;
		}
	}
	
	private class NetworkElementChangesParams{
		public final PbLteNwElementStatus pbLteNwElementStatusOriginalValues;
		public final int nwElementType;
		public int nwElementCommsStatus;
		public String nwElementIpAddress;
		
		NetworkElementChangesParams(PbLteNwElementStatus pbLteNwElementStatus, int nwElementType){
			this.pbLteNwElementStatusOriginalValues = pbLteNwElementStatus;
			this.nwElementType = nwElementType;
		}
	}
	
	private class MmeChangesParams{
		public final PbLteMmeStatus pbLteMmeStatusOriginalValues;
		public String ipAddr;
		public int s1CVlanInfo;
		
		public MmeChangesParams(PbLteMmeStatus pbLteMmeStatus) {
			this.pbLteMmeStatusOriginalValues = pbLteMmeStatus;
		}
	}
	
	private class RfChangesParams{
		public final PbLteRfStatus pbLteRfStatus;
		public final int txPowerOriginalValue;
		public int txPower;
		
		public RfChangesParams(PbLteRfStatus pbLteRfStatus, int txPower) {
			this.pbLteRfStatus = pbLteRfStatus;
			this.txPowerOriginalValue = txPower;
		}
	}
}

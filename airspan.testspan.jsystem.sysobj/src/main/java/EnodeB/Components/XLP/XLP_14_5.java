/**
 * 
 */
package EnodeB.Components.XLP;

import java.io.IOException;
import java.util.HashMap;
import org.snmp4j.smi.Variable;
import java.util.Hashtable;
import EnodeB.EnodeB;
import Netspan.API.Enums.HandoverTypes;
import Netspan.API.Enums.HoControlStateTypes;
import Netspan.API.Enums.SonAnrStates;
import Netspan.API.Enums.X2ControlStateTypes;
import Utils.GeneralUtils;
import Utils.Snmp.MibReader;
import jsystem.framework.report.Reporter;

/**
 * @author mgoldenberg
 *
 */
public class XLP_14_5 extends XLP {

	String mnoBroadcastPlmn = null;
	String eutranCellId = null;

	@Override
	public boolean addNbr(EnodeB enodeB, EnodeB neighbor, HoControlStateTypes hoControlStatus,X2ControlStateTypes x2ControlStatus, HandoverTypes handoverType, boolean isStaticNeighbor,String qOffsetRange) throws IOException {
		//Initialize qOffSet, in case the user doesn't want to fill it (It's optional)
		int qOffSet = 0;
		//create double valued instance.
		String mnoBroadcastPlmn = neighbor.calculateMnoBroadcastPlmn();
		String eutranCellId = neighbor.calculateEutranCellId();
		String instance = mnoBroadcastPlmn+"."+eutranCellId;
		
		//create OIDs
		MibReader reader = MibReader.getInstance();
		
		String plnm1Oid = reader.resolveByName("asLteStkCmdQNghListMvnoBroadcastPlmn1");
		String plnm2Oid = reader.resolveByName("asLteStkCmdQNghListMvnoBroadcastPlmn2");
		String plnm3Oid = reader.resolveByName("asLteStkCmdQNghListMvnoBroadcastPlmn3");
		String plnm4Oid = reader.resolveByName("asLteStkCmdQNghListMvnoBroadcastPlmn4");
		String plnm5Oid = reader.resolveByName("asLteStkCmdQNghListMvnoBroadcastPlmn5");
		String enbTypeOid = reader.resolveByName("asLteStkCmdQNghListEnbType");
		String dlEarfcnOid = reader.resolveByName("asLteStkCmdQNghListDlEarfcn");
		String enbIPOid = reader.resolveByName("asLteStkCmdQNghListEnbIpAddr");
		String pciOid = reader.resolveByName("asLteStkCmdQNghListPci");
		String tacOid = reader.resolveByName("asLteStkCmdQNghListTac");
		String x2TrigHoOid = reader.resolveByName("asLteStkCmdQNghListTriggerX2Ho");
		String qOffOid = reader.resolveByName("asLteStkCmdQNghListQOffsetCell");
		String notRemoveOid = reader.resolveByName("asLteStkCmdQNghListNotRemovable");
		String X2CtrlOid = reader.resolveByName("asLteStkCmdQNghListX2ControlState");
		String HoCtrlOid = reader.resolveByName("asLteStkCmdQNghListHoControlState");
		String cellMaskOid = reader.resolveByName("asLteStkCmdQNghListCellMask");
		String cmdFlagOid = reader.resolveByName("asLteStkCmdQNghListCmdFlag");
		String rowStatOid = reader.resolveByName("asLteStkCmdQNghListRowStatus");
		String cellIndiviOid = reader.resolveByName("asLteStkCmdQNghListCellIndividualOffset");
		
		//parse parameters
		String ipAdd = neighbor.getIpAddress();
		int neighborEarfcn = neighbor.getEarfcn();
		int x2TrigHo = Integer.valueOf(handoverType.convertEnum());
		if (qOffsetRange != null) {
			qOffSet = Integer.valueOf(qOffsetRange);
		}
		int x2Ctrl = Integer.valueOf(x2ControlStatus.convertEnum());
		int hoCtrl = Integer.valueOf(hoControlStatus.convertEnum());
		int myInt = (isStaticNeighbor) ? 1 : 0;
		int tac = Integer.valueOf(getTac(neighbor));
		int pci= neighbor.getPci();
		
		//snmp adding Neighbor
		provisionStartEvent();
		snmp.snmpSet(plnm1Oid+"."+instance, 0);
		snmp.snmpSet(plnm2Oid+"."+instance, 0);
		snmp.snmpSet(plnm3Oid+"."+instance, 0);
		snmp.snmpSet(plnm4Oid+"."+instance, 0);
		snmp.snmpSet(plnm5Oid+"."+instance, 0);
		snmp.snmpSet(enbTypeOid+"."+instance, 0);	
		snmp.snmpSet(dlEarfcnOid+"."+instance, neighborEarfcn);
		snmp.snmpSet(enbIPOid+"."+instance, ipAdd);
		snmp.snmpSet(pciOid+"."+instance, pci);
		snmp.snmpSet(tacOid+"."+instance, tac);
		snmp.snmpSet(x2TrigHoOid+"."+instance, x2TrigHo);
		if (qOffsetRange != null) {
			snmp.snmpSet(qOffOid + "." + instance, qOffSet);
		}
		snmp.snmpSet(notRemoveOid+"."+instance, myInt);
		snmp.snmpSet(X2CtrlOid+"."+instance, x2Ctrl);
		snmp.snmpSet(HoCtrlOid+"."+instance, hoCtrl);
		snmp.snmpSet(cellMaskOid+"."+instance, 1);
		snmp.snmpSet(cmdFlagOid+"."+instance, 0);
		snmp.snmpSet(rowStatOid+"."+instance, 1);
		snmp.snmpSet(cellIndiviOid+"."+instance, 15);
		provisionEndedEvent();
		return true;

	}
	
	private boolean edit3PartyNbrs(int numberOfNbr, final long mnoBroadcastPlmn, long startEutranCellId, int zeroForAddOneForRemove, Integer enbType_optional, Integer qOffsetCell_optional) throws IOException {
		if(zeroForAddOneForRemove != 1 && zeroForAddOneForRemove != 0){
			return false;
		}else{
			String cmdFlagOid = MibReader.getInstance().resolveByName("asLteStkCmdQNghListCmdFlag");
			String rowStatOid = MibReader.getInstance().resolveByName("asLteStkCmdQNghListRowStatus");
			String enbTypeOid = MibReader.getInstance().resolveByName("asLteStkCmdQNghListEnbType");
			String qOffsetCellOid = MibReader.getInstance().resolveByName("asLteStkCmdQNghListQOffsetCell");
			
			cmdFlagOid += ("." + mnoBroadcastPlmn + ".");
			rowStatOid += ("." + mnoBroadcastPlmn + ".");
			enbTypeOid += ("." + mnoBroadcastPlmn + ".");
			qOffsetCellOid += ("." + mnoBroadcastPlmn + ".");
			
			if(numberOfNbr <= 0 || startEutranCellId <=0){
				throw new IOException("Non positive number!");
			}
			
			provisionStartEvent();
			for(long instance = startEutranCellId; instance < (startEutranCellId + numberOfNbr); instance++){
				snmp.snmpSet(cmdFlagOid+"."+instance, zeroForAddOneForRemove);
				snmp.snmpSet(rowStatOid+"."+instance, 1);
				
				if(enbType_optional != null){
					snmp.snmpSet(enbTypeOid+"."+instance, enbType_optional);
				}
				if(qOffsetCell_optional != null){
					snmp.snmpSet(qOffsetCellOid+"."+instance, qOffsetCell_optional);
				}
				
				if(instance == Long.MAX_VALUE && Long.MAX_VALUE != (startEutranCellId + numberOfNbr)){
					report.report("EutranCellId greater than Long.", Reporter.WARNING);
					break;
				}
			}
			provisionEndedEvent();
			return true;
		}
	}
	
	public boolean add3PartyNbrs(int numberOfNbr, final long mnoBroadcastPlmn, long startEutranCellId, Integer enbType_optional, Integer qOffsetCell_optional) throws IOException {
		return edit3PartyNbrs(numberOfNbr, mnoBroadcastPlmn, startEutranCellId, 0, enbType_optional, qOffsetCell_optional);
	}
	
	public boolean remove3PartyNbrs(int numberOfNbr, final long mnoBroadcastPlmn, long startEutranCellId) throws IOException {
		return edit3PartyNbrs(numberOfNbr, mnoBroadcastPlmn, startEutranCellId, 1, null, null);
	}
	
	
	/**
	 * method ends re provision session
	 * @return
	 */
	@Override
	public boolean provisionEndedEvent() {
		boolean result = false;
		String oid = MibReader.getInstance().resolveByName("asLteStkActionConfigCompleted");
		try{
			result = snmp.snmpSet(oid, 1);
		}catch(Exception e){
			report.report("Couldn't end reprovision");
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * method starts re provision session
	 * @return
	 */
	@Override
	public boolean provisionStartEvent() {
		boolean result = false;
		String oid = MibReader.getInstance().resolveByName("asLteStkActionReprovisionStarted");
		try{
			result =snmp.snmpSet(oid, 1);
		}catch(Exception e){
			report.report("Couldn't start reprovision");
			e.printStackTrace();
		}
		return result;
		
	}
	
	/**
	 * @author ggrunwald
	 * @param enb
	 * @return
	 * @throws IOException
	 */
	private String getTac(EnodeB enb) throws IOException{
		String myTac = "";
		String tac = MibReader.getInstance().resolveByName("asLteStkCellCfgTac");
		try{
			myTac = enb.getSNMP(tac);
		}catch(Exception e){
			report.report("Couldn't get TAC",Reporter.WARNING);
			e.printStackTrace();
		}
		return myTac;
	}
	
	public boolean deleteNeighborBySNMP(EnodeB neighbor) throws IOException {
		//create double valued instance
		updatePLMNandEutranCellID(neighbor);
		String instance = mnoBroadcastPlmn+"."+eutranCellId;
		//get all OIDs
		String cmdFlagOid = MibReader.getInstance().resolveByName("asLteStkCmdQNghListCmdFlag");
		String rowStatOid = MibReader.getInstance().resolveByName("asLteStkCmdQNghListRowStatus");
		//snmp to delete Neighbor
		provisionStartEvent();
		snmp.snmpSet(cmdFlagOid+"."+instance, 1);
		snmp.snmpSet(rowStatOid+"."+instance, 1);
		provisionEndedEvent();
		
		return true;
	}

	public boolean deleteAllNeighborsByCli() {
		return super.dbDelete("nghList", "*");
	}

	//getting the CLI parameters of EnodeB and neighbor for CLI verify check
	public void updatePLMNandEutranCellID(EnodeB neighbor){

		mnoBroadcastPlmn = neighbor.calculateMnoBroadcastPlmn();
		eutranCellId = neighbor.calculateEutranCellId();
	}
	
	
	public boolean verifyNbrList(EnodeB enodeB, EnodeB neighbor, HoControlStateTypes hoControlStatus,
			X2ControlStateTypes x2ControlStatus, HandoverTypes handoverType, boolean isStaticNeighbor,
			String qOffsetRange) throws IOException {
		boolean wasAdded = true;
		boolean wasFound = false;
		String oidPlmn = MibReader.getInstance().resolveByName("asLteStkNghListDlEarfcn");
		
		HashMap<String, Variable> plmnValues = snmp.SnmpWalk(oidPlmn);

		if (plmnValues == null) {
			report.report("Cannot get Neighbor table Using SNMP", Reporter.WARNING);
			return false;
		}
		updatePLMNandEutranCellID(neighbor);
		
		String fullInstance = mnoBroadcastPlmn.trim() + "." + eutranCellId.trim();
		String output = oidPlmn+"."+fullInstance;
		if(plmnValues.containsKey(output)){
			report.report("EnodeB: " + neighbor.getNetspanName() + " was found in the " + enodeB.getNetspanName()+ " neighbor's list");
			wasFound = true;
		}
		if(wasFound){
			String HOStateOID = MibReader.getInstance().resolveByName("asLteStkNghListHoControlState");
			String COStateOID = MibReader.getInstance().resolveByName("asLteStkNghListX2ControlState");
			String HOtypeOID = MibReader.getInstance().resolveByName("asLteStkNghListTriggerX2Ho");
			String StaticOID = MibReader.getInstance().resolveByName("asLteStkNghListNotRemovable");
			String qoffsetOID = MibReader.getInstance().resolveByName("asLteStkNghListQOffsetCell");
			
			String HOstate = "";
			String Costate = "";
			String HOtype = "";
			String StaticState = "";
			boolean Static = false;
			String qoffset = "";
			
			for(int i=0;i<3;i++){
				HOstate = snmp.get(HOStateOID+"."+fullInstance).trim();
				if(!HOstate.equals("")){
					break;
				}
			}
			for(int i=0;i<3;i++){
				Costate = snmp.get(COStateOID+"."+fullInstance).trim();
				if(!Costate.equals("")){
					break;
				}
			}
			for(int i=0;i<3;i++){
				HOtype = snmp.get(HOtypeOID+"."+fullInstance).trim();
				if(!HOtype.equals("")){
					break;
				}
			}
			for(int i=0;i<3;i++){
				StaticState = snmp.get(StaticOID+"."+fullInstance).trim();
				if(!StaticState.equals("")){
					Static = StaticState.equals("1") ? true:false;
					break;
				}
			}
			for(int i=0;i<3;i++){
				qoffset = snmp.get(qoffsetOID+"."+fullInstance).trim();
				if(!qoffset.equals("")){
					break;
				}
			}
			
			if(null!=hoControlStatus)
			{
				if(HOstate.equals("")){
					report.report("Ho Control parameter is empty", Reporter.WARNING);
				}else if(!HOstate.equals(hoControlStatus.convertEnum())){
					report.report("Ho Control State is " + HOstate + " and not "+ hoControlStatus.convertEnum() + " as expected", Reporter.WARNING);
					wasAdded = false;
				}else{
					report.report("Ho Control State is " + hoControlStatus +  " as expected");
				}
			}
			
			if(null!=x2ControlStatus)
			{
				if(Costate.equals("")){
					report.report("X2 Control parameter is empty", Reporter.WARNING);
				}else if(!Costate.equals(x2ControlStatus.convertEnum())){
					report.report("X2 Control State is " + Costate + " and not "+ x2ControlStatus.convertEnum() + " as expected", Reporter.WARNING);
					wasAdded = false;
				}else{
					report.report("X2 Control Type is " + x2ControlStatus +  " as expected");
				}
			}
			
			if(null!=handoverType)
			{
				if(HOtype.equals("")){
					report.report("Handover Type parameter is empty", Reporter.WARNING);
				}else if(!HOtype.equals(handoverType.convertEnum())){
					report.report("Handover Type is " + HOtype + " and not "+ handoverType.convertEnum() + " as expected", Reporter.WARNING);
					wasAdded = false;
				}else{
					report.report("Handover Type is " + handoverType +  " as expected");
				}
			}
			
			if(StaticState.equals("")){
				report.report("Static neighbor parameter is empty", Reporter.WARNING);
			}else if(Static != isStaticNeighbor){
				report.report("Static Neighbor value is " + Static + " and not "+ String.valueOf(isStaticNeighbor) + " as expected", Reporter.WARNING);
				wasAdded = false;
			}else{
				report.report("Static Neighbor value is " + Static +  " as expected");
			}
			
			if(null!=qOffsetRange)
			{
				if(qoffset.equals("")){
					report.report("Q-Offset parameter is empty", Reporter.WARNING);
				}else if(!qoffset.equals(qOffsetRange)){
					report.report("Q-Offset value is " + qoffset + " and not "+ qOffsetRange + " as expected", Reporter.WARNING);
					wasAdded = false;
				}else{
					report.report("Q-Offset value is " + qoffset +  " as expected");
				}
			}
		}
		if (!wasFound) {
			report.report("Couldn't find the neighbor " + neighbor.getNetspanName() + " in the " + enodeB.getNetspanName()+ " neighbor list", Reporter.WARNING);
			report.startLevel("Debug info (Walk on asLteStkNghList indexes)");
			for(String keys : plmnValues.keySet())
			{
				report.report(keys);
			}
			report.stopLevel();
			wasAdded = false;
		}
		return wasAdded;
	}

	/**
	 * (non-Javadoc)
	 * @throws IOException
	 *
	 */
	@Override
	public boolean verifyNbrList(EnodeB neighbor) throws IOException {
		String oidPlmn = MibReader.getInstance().resolveByName("asLteStkNghListDlEarfcn");
		
		HashMap<String, Variable> plmnValues = snmp.SnmpWalk(oidPlmn);

		if (plmnValues == null) {
			report.report("Cannot get Neighbor table Using SNMP", Reporter.WARNING);
			return false;
		}
		
		updatePLMNandEutranCellID(neighbor);
		
		String fullInstance = mnoBroadcastPlmn.trim() + "." + eutranCellId.trim();
		String output = oidPlmn+"."+fullInstance;
		if(plmnValues.containsKey(output))
		{
			report.report("Neighbor: " + neighbor.getName() + " was found in EnodeB: "
					+ getName() + " list");
			return true;
		}
	
		
		return false;
	}
	
	/**
	 * (non-Javadoc)
	 *
	 */
	@Override
	public boolean verifyNoNeighbors() {
		return  super.dbGetInMatrix("nghList") == null;
	}
	
	  
	 
	@Override
	public String[] getNeighbors() {
		Hashtable<String, String[]> nghList = super.dbGet("nghList");
		if (nghList != null)
			return nghList.get("enbIpAddr.address");
		else {
			GeneralUtils.printToConsole("Couldn't get table \"nghList\"");
			return null;
		}
	}
	
	public boolean setANRState(SonAnrStates anrState) {
		String oidEnable = MibReader.getInstance().resolveByName("asLteStkCellAnrCfgEnable");
		String oidState = MibReader.getInstance().resolveByName("asLteStkCellAnrCfgAnrState");
		
		
		if(oidEnable.trim().isEmpty() || oidState.trim().isEmpty())
			return false;
		
		switch(anrState)
		{
		case DISABLED:
			snmp.snmpSet(oidEnable, 0);
			return true;

		case HO_MEASUREMENT:
			snmp.snmpSet(oidEnable, 1);
			snmp.snmpSet(oidState, 1);
			return true;
		case PERIODICAL_MEASUREMENT:
			snmp.snmpSet(oidEnable, 1);
			snmp.snmpSet(oidState, 2);
			return true;
		default:
			return false;		
		}	
	}

	@Override
	public String getTriggerX2Ho(EnodeB neighbor) {
		String oid = MibReader.getInstance().resolveByName("asLteStkNghListTriggerX2Ho");
		
		updatePLMNandEutranCellID(neighbor);
		
		String fullInstance = mnoBroadcastPlmn.trim() + "." + eutranCellId.trim();
		String output = oid+"."+fullInstance;
		String ans = "";
		try {
			ans = snmp.get(output);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return ans;
	}
	
	@Override
	public boolean verifyAnrNeighbor(EnodeB neighbor) {
		String oid = MibReader.getInstance().resolveByName("asLteStkNghListDiscoveredByAnr");

		HashMap<String, Variable> anrValues = snmp.SnmpWalk(oid);

		if (anrValues == null) {
			report.report("Cannot get Neighbor table Using SNMP", Reporter.WARNING);
			return false;
		}
		
		updatePLMNandEutranCellID(neighbor);
		
		String fullInstance = mnoBroadcastPlmn.trim() + "." + eutranCellId.trim();
		String output = oid+"."+fullInstance;
		GeneralUtils.printToConsole("'PLMN' and 'Eutran Cell ID' values: "+ fullInstance);
		return anrValues.containsKey(output);

	}

	@Override
	public boolean resetCounter(String tableName, String index,HashMap<String, String> KeyValuePairs) {
		return dbSet(tableName, index, KeyValuePairs);
		
	}

	@Override
	public boolean deleteAnrNeighborsBySNMP() {
		boolean action = true;
		String oid = MibReader.getInstance().resolveByName("asLteStkNghListMnoBroadcastPlmn");

		HashMap<String, Variable> walkResult = snmp.SnmpWalk(oid);
		if(walkResult!=null){
			if(walkResult.size()!=0){
				String cmdFlagOid = MibReader.getInstance().resolveByName("asLteStkCmdQNghListCmdFlag");
				String rowStatOid = MibReader.getInstance().resolveByName("asLteStkCmdQNghListRowStatus");
				provisionStartEvent();
				for(String instance:walkResult.keySet()){
					String[] temp = instance.split("\\.");
					String ins = "";
					int len = temp.length;
					if(len>=2){
						ins = temp[temp.length-2]+"."+temp[temp.length-1];
					}
					action = action && snmp.snmpSet(cmdFlagOid+"."+ins, 1);
					action = action && snmp.snmpSet(rowStatOid+"."+ins, 1);
				}
				provisionEndedEvent();
			}
		}else{
			report.report("Failed to get nbr list via SNMP");
			return false;
		}
		return action;
	}
	
	public boolean setGranularityPeriod(int value) {
		report.report("No setting granularity period. Only valid for XLP 15.2 and up");
		return false;
	}	

	@Override
	public String getDefaultSSHUsername(){
		return ADMIN_USERNAME;
	}
	
	@Override
	public String getDefaultSerialUsername(){
		return ADMIN_USERNAME;
	}
}

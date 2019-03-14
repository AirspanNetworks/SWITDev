
package EnodeB.Components.XLP;

import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;

import EnodeB.EnodeB;
import Netspan.API.Enums.HandoverType;
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
public class XLP_14_0 extends XLP{

	public boolean verifyNbrList(EnodeB enodeB, EnodeB neighbor, HoControlStateTypes hoControlStatus,
			X2ControlStateTypes x2ControlStatus, HandoverType HandoverType, boolean isStaticNeighbor,
			String qOffsetRange){
		// TODO Auto-generated method stub
		return false;
	}
	
	/** (non-Javadoc)
	 * @see EnodeB.Components.XLP.XLP#deleteNeighborByCli(EnodeB.EnodeB, EnodeB.EnodeB, java.lang.String)
	 */
	@Override
	public boolean deleteNeighborBySNMP(EnodeB neighbor){
		// TODO Auto-generated method stub
		return false;
	}

	/** (non-Javadoc)
	 * @see EnodeB.Components.XLP.XLP#deleteAllNeighborsByCli(EnodeB.EnodeB)
	 */
	@Override
	public boolean deleteAllNeighborsByCli() {
		return super.dbDelete("nghCellCfg", "*");
	}

	/** (non-Javadoc)
	 * @see EnodeB.Components.XLP.XLP#verifyNoNeighbors(EnodeB.EnodeB)
	 */
	@Override
	public boolean verifyNoNeighbors() {
		//TODO :: change the table name
		return super.dbGetInMatrix("nghList") == null;
	}

	/** (non-Javadoc)
	 * @see EnodeB.Components.XLP.XLP#gettingCLIParameters(EnodeB.EnodeB, EnodeB.EnodeB)
	 */
	@Override
	public void updatePLMNandEutranCellID(EnodeB neighbor) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public String[] getNeighbors() {
		Hashtable<String, String[]> nghList = super.dbGet("nghCellCfg");
		if (nghList != null)
			return nghList.get("enbIpAddr.address");
		else {
			GeneralUtils.printToConsole("Couldn't get table \"nghCellCfg\"");
			return null;
		}
	}

	@Override
	public boolean verifyNbrList(EnodeB neighbor) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public boolean setANRState(SonAnrStates anrState) {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellAnrCfgEnable");
		
		if(oid.trim().isEmpty())
			return false;
		
		if(anrState != SonAnrStates.DISABLED)
			snmp.snmpSet(oid, 1);
		else
			snmp.snmpSet(oid, 0);
		return true;
	}

	@Override
	public String getTriggerX2Ho(EnodeB neighbor) {
		// TODO Auto-generated method stub
		return GeneralUtils.ERROR_VALUE + "";
	}
	
	@Override
	public boolean verifyAnrNeighbor(EnodeB neighbor) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean resetCounter(String tableName, String index, HashMap<String, String> KeyValuePairs) {
		return dbSet(tableName, index, KeyValuePairs);
	}

	@Override
	public boolean addNbr(EnodeB enodeB, EnodeB neighbor, HoControlStateTypes hoControlStatus,
			X2ControlStateTypes x2ControlStatus, HandoverType HandoverType, boolean isStaticNeighbor,
			String qOffsetRange) throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean remove3PartyNbrs(int numberOfNbr, long mnoBroadcastPlmn, long startEutranCellId) throws IOException {
		report.report("remove3PartyNbrs Unimplemented method", Reporter.WARNING);
		return false;
	}

	@Override
	public boolean add3PartyNbrs(int numberOfNbr, long mnoBroadcastPlmn, long startEutranCellId, Integer enbType_optional, Integer qOffsetCell_optional) throws IOException {
		report.report("add3PartyNbrs Unimplemented method", Reporter.WARNING);
		return false;
	}

	@Override
	public boolean deleteAnrNeighborsBySNMP() {
		report.report("deleteAnrNeighborsBySNMP Unimplemented method", Reporter.WARNING);
		return false;
	}
	
	public boolean setGranularityPeriod(int value) {
		report.report("No setting granularity period. Only valid for XLP 15.2 and up");
		return false;
	}

	@Override
	public String getDefaultSSHUsername(){
		return UNSECURED_USERNAME;
	}
	
	@Override
	public String getDefaultSerialUsername(){
		return UNSECURED_USERNAME;
	}	
}

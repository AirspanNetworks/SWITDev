package EnodeB.Components.XLP;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import Utils.PasswordUtils;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.Variable;
import EnodeB.ProtoBuf.ProtoBuf;
import EnodeB.ProtoBuf.PbLteStatusOuterClass.PbLteAnrStatus;
import EnodeB.ProtoBuf.PbLteStatusOuterClass.PbLteCellStatus;
import EnodeB.ProtoBuf.PbLteStatusOuterClass.PbLteMmeStatus;
import EnodeB.ProtoBuf.PbLteStatusOuterClass.PbLteNwElementStatus;
import EnodeB.ProtoBuf.PbLteStatusOuterClass.PbLteRfStatus;
import EnodeB.ProtoBuf.PbLteStatusOuterClass.PbLteSgwStatus;
import Utils.GeneralUtils;
import Utils.Snmp.MibReader;
import jsystem.framework.report.Reporter;

public class XLP_15_2 extends XLP_14_5 {
	@Override
	public boolean resetCounter(String tableName, String index, HashMap<String, String> KeyValuePairs) {
		lteCli("kpi counters clearprotostats");
		return true;
	}

	@Override
	public int getCountersValue(String valueName) {
		HashMap<String, Variable> valueMap;
		int ret = 0;
		String protobufData = MibReader.getInstance().resolveByName("asLteEnbStatsData");
		valueMap = snmp.SnmpWalk(protobufData, false);
		for (Entry<String, Variable> entry : valueMap.entrySet()) {
			try{
				byte[] protoBuf = ((OctetString) entry.getValue()).getValue();
				ret += ProtoBuf.getStatsPbLteStatsCell(protoBuf, valueName);
			}catch(Exception e){
				e.printStackTrace();
				return GeneralUtils.ERROR_VALUE;
			}
		}
		return ret;
	}
	
	public int getSingleSampleCountersValue(String valueName) {
		HashMap<String, Variable> valueMap;
		int ret = 0;
		String protobufData = MibReader.getInstance().resolveByName("asLteEnbStatsData");
		valueMap = snmp.SnmpWalk(protobufData, false);
		for (Entry<String, Variable> entry : valueMap.entrySet()) {
			try{
				byte[] protoBuf = ((OctetString) entry.getValue()).getValue();
				ret = (int)ProtoBuf.getStatsPbLteStatsCell(protoBuf, valueName);
				break;
			}catch(Exception e){
				e.printStackTrace();
				return GeneralUtils.ERROR_VALUE;
			}
		}
		return ret;
	}
	
	
	@Override
	public PbLteAnrStatus getPbLteSAnrStatus(long mnoBroadcastPlmn , long eutranCellId) {
		String oid = MibReader.getInstance().resolveByName("asLteStkBulkStatusNrtData");
		try {
			byte[] protoBufData = ((OctetString)snmp.getVariable(oid)).getValue();
			List<PbLteAnrStatus> pbLteAnrStatusList = ProtoBuf.getPbLteAnrStatus(protoBufData);
			for(PbLteAnrStatus pbLteAnrStatus : pbLteAnrStatusList){
				int tmpMnoBroadcastPlmn = pbLteAnrStatus.getNbPlmnId();
				int tmpEutranCellId = pbLteAnrStatus.getNbCellIdentity();
				if (mnoBroadcastPlmn == tmpMnoBroadcastPlmn && eutranCellId == tmpEutranCellId){
					return pbLteAnrStatus;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public PbLteCellStatus getPbLteCellStatus(int cellNumber) {
		String oid = MibReader.getInstance().resolveByName("asLteStkBulkStatusCellData");
		try {
			byte[] protoBufData = ((OctetString)snmp.getVariable(oid)).getValue();
			List<PbLteCellStatus> pbLteCellStatusList = ProtoBuf.getPbLteCellStatus(protoBufData);
			for(PbLteCellStatus pbLteCellStatus : pbLteCellStatusList){
				int tmpCellNumber = pbLteCellStatus.getCellNumber();
				if (cellNumber == tmpCellNumber){
					return pbLteCellStatus;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public PbLteNwElementStatus getPbLteNetworkElementStatus(int networkType) {
		String oid = MibReader.getInstance().resolveByName("asLteStkBulkStatusNetworkElementData");
		try {
			byte[] protoBufData = ((OctetString)snmp.getVariable(oid)).getValue();
			List<PbLteNwElementStatus> pbLteNwElementStatusList = ProtoBuf.getPbLteNetworkElementStatus(protoBufData);
			for(PbLteNwElementStatus pbLteNwElementStatus : pbLteNwElementStatusList){
				int tmpNetworkType = pbLteNwElementStatus.getNetworkType();
				if (networkType == tmpNetworkType){
					return pbLteNwElementStatus;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public PbLteMmeStatus getPbLteMmeStatus(String ipAddress) {
		String oid = MibReader.getInstance().resolveByName("asLteStkBulkStatusMmeData");
		try {
			byte[] protoBufData = ((OctetString)snmp.getVariable(oid)).getValue();
			List<PbLteMmeStatus> pbLteMmeStatusList = ProtoBuf.getPbLteMmeStatus(protoBufData);
			for(PbLteMmeStatus pbLteMmeStatus : pbLteMmeStatusList){
				String tmpIpAddress = pbLteMmeStatus.getIpAddress();
				if (ipAddress.equals(tmpIpAddress)){
					return pbLteMmeStatus;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public PbLteSgwStatus getPbLteSgwStatus(String ipAddress) {
		String oid = MibReader.getInstance().resolveByName("asLteStkBulkStatusSgwData");
		try {
			byte[] protoBufData = ((OctetString)snmp.getVariable(oid)).getValue();
			List<PbLteSgwStatus> pbLteSgwStatusList = ProtoBuf.getPbLteSgwStatus(protoBufData);
			for(PbLteSgwStatus pbLteSgwStatus : pbLteSgwStatusList){
				String tmpIpAddress = pbLteSgwStatus.getIpAddress();
				if (ipAddress.equals(tmpIpAddress)){
					return pbLteSgwStatus;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public PbLteRfStatus getPbLteRfStatus(int rfPathId) {
		String oid = MibReader.getInstance().resolveByName("asLteStkBulkStatusRfData");
		try {
			byte[] protoBufData = ((OctetString)snmp.getVariable(oid)).getValue();
			List<PbLteRfStatus> pbLteSgwStatusList = ProtoBuf.getPbLteRfStatus(protoBufData);
			for(PbLteRfStatus pbLteSgwStatus : pbLteSgwStatusList){
				int tmpRfPathId = pbLteSgwStatus.getRfPathID();
				if (rfPathId == tmpRfPathId){
					return pbLteSgwStatus;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	
	@Override
	public int getNrtChanges(){
		String oid = MibReader.getInstance().resolveByName("asLteStkBulkStatusNrtChanges");
		try {
			return Integer.parseInt(snmp.get(oid));
		} catch (Exception e) {
			e.printStackTrace();
			report.report("Couldn't get the NrtChanges", Reporter.WARNING);
		}
		return GeneralUtils.ERROR_VALUE;
	}
	
	@Override
	public boolean setNrtChanges(int value) {
		String oid = MibReader.getInstance().resolveByName("asLteStkBulkStatusNrtChanges");
		return snmp.snmpSet(oid, value);
	}
	
	@Override
	public int getCellChanges(){
		String oid = MibReader.getInstance().resolveByName("asLteStkBulkStatusCellChanges");
		try {
			return Integer.parseInt(snmp.get(oid));
		} catch (Exception e) {
			e.printStackTrace();
			report.report("Couldn't get the CellChanges", Reporter.WARNING);
		}
		return GeneralUtils.ERROR_VALUE;
	}
	
	@Override
	public boolean setCellChanges(int value) {
		String oid = MibReader.getInstance().resolveByName("asLteStkBulkStatusCellChanges");
		return snmp.snmpSet(oid, value);
	}
	
	@Override
	public int getMmeChanges(){
		String oid = MibReader.getInstance().resolveByName("asLteStkBulkStatusMmeChanges");
		try {
			return Integer.parseInt(snmp.get(oid));
		} catch (Exception e) {
			e.printStackTrace();
			report.report("Couldn't get the MmeChanges", Reporter.WARNING);
		}
		return GeneralUtils.ERROR_VALUE;
	}
	
	@Override
	public boolean setMmeChanges(int value) {
		String oid = MibReader.getInstance().resolveByName("asLteStkBulkStatusMmeChanges");
		return snmp.snmpSet(oid, value);
	}
	
	@Override
	public int getNetworkElementChanges(){
		String oid = MibReader.getInstance().resolveByName("asLteStkBulkStatusNetworkElementChanges");
		try {
			return Integer.parseInt(snmp.get(oid));
		} catch (Exception e) {
			e.printStackTrace();
			report.report("Couldn't get the NetworkElement", Reporter.WARNING);
		}
		return GeneralUtils.ERROR_VALUE;
	}
	
	@Override
	public boolean setNetworkElementChanges(int value) {
		String oid = MibReader.getInstance().resolveByName("asLteStkBulkStatusNetworkElementChanges");
		return snmp.snmpSet(oid, value);
	}
	
	@Override
	public int getRfChanges(){
		String oid = MibReader.getInstance().resolveByName("asLteStkBulkStatusRfChanges");
		try {
			return Integer.parseInt(snmp.get(oid));
		} catch (Exception e) {
			e.printStackTrace();
			report.report("Couldn't get the RfChanges", Reporter.WARNING);
		}
		return GeneralUtils.ERROR_VALUE;
	}
	
	@Override
	public boolean setRfChanges(int value) {
		String oid = MibReader.getInstance().resolveByName("asLteStkBulkStatusRfChanges");
		return snmp.snmpSet(oid, value);
	}
	
	@Override
	public int getSgwChanges(){
		String oid = MibReader.getInstance().resolveByName("asLteStkBulkStatusSgwChanges");
		try {
			return Integer.parseInt(snmp.get(oid));
		} catch (Exception e) {
			e.printStackTrace();
			report.report("Couldn't get the SgwChanges", Reporter.WARNING);
		}
		return GeneralUtils.ERROR_VALUE;
	}
	
	@Override
	public boolean setSgwChanges(int value) {
		String oid = MibReader.getInstance().resolveByName("asLteStkBulkStatusSgwChanges");
		return snmp.snmpSet(oid, value);
	}
	
	public boolean setGranularityPeriod(int value) {
		String oid = MibReader.getInstance().resolveByName("asLteStatsIfControlRbStatsGranularityPeriod");
		try {
			return snmp.snmpSet(oid,value);
		} catch (Exception e) {
			GeneralUtils.printToConsole("Error setting Granularity Period: " + e.getMessage());
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public String getDefaultSSHUsername(){
		return PasswordUtils.ADMIN_USERNAME;
	}
	
	@Override
	public String getDefaultSerialUsername(){
		return PasswordUtils.ADMIN_USERNAME;
	}
}

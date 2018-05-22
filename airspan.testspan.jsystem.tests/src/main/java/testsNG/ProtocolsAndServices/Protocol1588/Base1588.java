package testsNG.ProtocolsAndServices.Protocol1588;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import EnodeB.EnodeB;
import Netspan.AlarmSeverity;
import Netspan.EnbProfiles;
import Netspan.NetspanServer;
import Netspan.API.Enums.PrimaryClockSourceEnum;
import Netspan.API.Lte.AlarmInfo;
import Netspan.API.Lte.EventInfo;
import Netspan.API.Lte.PTPStatus;
import Netspan.Profiles.SyncParameters;
import Utils.GeneralUtils;
import Utils.InetAddressesHelper;
import Utils.SysObjUtils;
import Utils.Snmp.MibReader;
import jsystem.framework.ParameterProperties;
import jsystem.framework.report.Reporter;
import testsNG.TestspanTest;
import testsNG.Actions.AlarmsAndEvents;
import testsNG.Actions.EnodeBConfig;
import testsNG.Actions.PeripheralsConfig;
import testsNG.Actions.Statuses;

/**
 * @author Meital Mizrachi
 */

public class Base1588 extends TestspanTest {
	protected static final int PTP_INTERFACE_INDEX = 6;
	
	protected testsNG.SON.ANR.P0 anrTest;
	protected EnodeB dut;
	protected EnodeB dut2;
	protected EnodeBConfig enodeBConfig;
	protected NetspanServer netspanServer;
	protected PeripheralsConfig peripheralsConfig;
	protected AlarmsAndEvents alarmsAndEvents;
	protected MibReader mibReader;
	protected Statuses status;
	protected PTPStatus ptpStatus;
	protected String PTP_IP = null;
	protected boolean generalFlag;
	protected String generalOid;
	protected Date generalFromDate;
	protected Date generalToDate;
	protected DateFormat format = new SimpleDateFormat("HH:mm:ss a");
	protected long TEST_TIME = 10*1000*60;

	protected void reportPing(String ip, String nameDeviceIp, int tries) throws Exception {
		String pingResponse = dut.ping(ip, tries);
		if ((pingResponse != null) && (pingResponse.contains(tries + " packets received")))
			report.report("Ping to the " + nameDeviceIp + "(" + ip + ") succceded");
		else {
			report.report("There is no ping to the " + nameDeviceIp + "(" + ip + ") on enodeb: " + dut.getNetspanName(),
					Reporter.WARNING);
			report.report(pingResponse);
			reason = "There is no ping to the " + nameDeviceIp + "(" + ip + ") on enodeb: " + dut.getNetspanName();
		}
	}

	protected List<AlarmInfo> reportOnSearchAlarms(boolean needToBeExists,List<String> toSearch) {
		List<AlarmInfo> allAlarms = alarmsAndEvents.getAllAlarmsNode(dut);
		List<AlarmInfo> alarms = alarmsAndEvents.getAllAlarmsNodeWithSpecificType(allAlarms, toSearch);
		List<String> searchwords = new ArrayList<String>(toSearch);
		
		if (alarms.isEmpty() && needToBeExists) {
			report.report("'" + searchwords.toString() + "' alarm does NOT exist", Reporter.FAIL);
			reason = "'" + searchwords.toString() + "' alarm does NOT exist";
		}

		else if (alarms.isEmpty() && !needToBeExists)
			report.report("'" + searchwords.toString() + "' alarm does NOT exist");

		else if (!alarms.isEmpty() && needToBeExists)
			report.report("There is " + alarms.size() + " '" + searchwords.toString() + "' alarms");

		else if (!alarms.isEmpty() && !needToBeExists) {
			report.report("There is " + alarms.size() + " '" + searchwords.toString() + "' alarms", Reporter.FAIL);
			reason = "There is " + alarms.size() + " '" + searchwords.toString() + "' alarms";
		}
		return alarms;
	}

	protected void reportOnEnbState(boolean needToBeInService) {
		if (dut.isInService() && needToBeInService)
			report.report("Enb " + dut.getNetspanName() + " is IN SERVICE");

		else if (dut.isInService() && !needToBeInService) {
			report.report("Enb " + dut.getNetspanName() + " is IN SERVICE", Reporter.FAIL);
			reason = "Enb " + dut.getNetspanName() + " is IN SERVICE";
		}

		else if (!dut.isInService() && needToBeInService) {
			report.report("Enb " + dut.getNetspanName() + " is OUT OF SERVICE", Reporter.FAIL);
			reason = "Enb " + dut.getNetspanName() + " is OUT OF SERVICE";
		}

		else if (!dut.isInService() && !needToBeInService)
			report.report("Enb " + dut.getNetspanName() + " is OUT OF SERVICE");
	}

	protected void reportOnSyncStatus(boolean needToBeLock)  {
		boolean flagSyncStatus = dut.isPTPLocked();
		if (flagSyncStatus && needToBeLock)
			report.report("Sync Status is locked");

		else if (flagSyncStatus && !needToBeLock) {
			report.report("Sync Status is locked", Reporter.FAIL);
			reason += " Sync Status is locked";
		}

		else if (!flagSyncStatus && needToBeLock) {
			report.report("Sync Status is unlocked", Reporter.FAIL);
			reason += " Sync Status is unlocked";
		}

		else if (!flagSyncStatus && !needToBeLock)
			report.report("Sync Status is unlocked");
	}

	protected void reportOnMasterConnectivity(boolean masterNeedToBeConnectivity, PTPStatus ptpStatus){
		boolean flagMasterConnectivity = !ptpStatus.masterConnectivity.contains("Not Connected");
		if (flagMasterConnectivity && masterNeedToBeConnectivity)
			report.report("masterConnectivity = 1");

		else if (flagMasterConnectivity && !masterNeedToBeConnectivity) {
			report.report("masterConnectivity = 1", Reporter.FAIL);
			reason += " Enb is Connected to primary Grand Master";
		}

		else if (!flagMasterConnectivity && masterNeedToBeConnectivity) {
			report.report("masterConnectivity = 0", Reporter.FAIL);
			reason += " Enb is Not Connected to primary Grand Master";
		}

		else if (!flagMasterConnectivity && !masterNeedToBeConnectivity)
			report.report("masterConnectivity = 0");
	}

	protected void reportAboutExistAlarmWithSeverity(List<AlarmInfo> allAlarms, String alarmType,
			AlarmSeverity severity) {
		List<String> alarmTypeList = new ArrayList<String>();
		alarmTypeList.add(alarmType);
		List<AlarmInfo> alarmsWithType = alarmsAndEvents.getAllAlarmsNodeWithSpecificType(allAlarms, alarmTypeList);
		if (alarmsWithType.isEmpty()) {
			report.report("'" + alarmType + "' alarm does NOT exist", Reporter.FAIL);
			reason = "'" + alarmType + "' alarm does NOT exist";
			return;
		}
		generalFlag = alarmsAndEvents.isAlarmSeverityExists(alarmsWithType, severity);

		if (generalFlag)
			report.report("'" + alarmType + "' alarm exists with severity " + severity);
		else {
			String stringReport = "'" + alarmType + "' alarm exists with severity "
					+ AlarmSeverity.translation(alarmsWithType.get(0).severity) + " (Severity should be: " + severity
					+ ")";
			report.report(stringReport, Reporter.FAIL);
			reason = stringReport;
		}
	}

	protected void reportAboutExistEventWithInfo(List<EventInfo> allEvents, String eventType, String eventInfo) {
		List<EventInfo> eventsWithType = alarmsAndEvents.getAllEventsNodeWithSpecificType(allEvents, eventType);
		if (eventsWithType.isEmpty()) {
			report.report("'" + eventType + "' event does NOT exist", Reporter.FAIL);
			reason = "'" + eventType + "' event does NOT exist";
			return;
		}
		for (EventInfo event : eventsWithType) {
			if (event.getEventInfo().toLowerCase().contains(eventInfo.toLowerCase())) {
				report.report("'" + eventType + "' event exists with Info=" + event.getEventInfo());
				return;
			}
		}
		String currInfo = eventsWithType.get(0).getEventInfo();
		report.report("'" + eventType + "' event exists with Info=" + currInfo + " (Info should contain: " + eventInfo + ")",
				Reporter.FAIL);
		reason = "'" + eventType + "' event exists with Info=" + currInfo + " (Info should contain: " + eventInfo + ")";
	}

	protected void reportAboutOneClockSourceStatusParameter(String name, String expected, String actual)
			throws Exception {
		if (expected.equals(actual))
			report.report(name + "-> actual: " + actual + ", expected: " + expected);
		else {
			report.report(name + "-> actual: " + actual + ", expected: " + expected, Reporter.FAIL);
			reason = "Netspan-> Clock Source Status-> " + name + "-> actual: " + actual + ", expected: " + expected;
		}
	}

	protected boolean reportAboutMibDB(String name, String expected, String mib, Integer index) throws Exception {
		String oid = mibReader.resolveByName(mib);
		String actual = dut.getSNMP(oid, index);

		if (expected.equals(actual)) {
			report.report(name + "-> actual: " + actual + ", expected: " + expected);
			return true;
		} else {
			report.report(name + "-> actual: " + actual + ", expected: " + expected, Reporter.FAIL);
			reason = name + "-> actual: " + actual + ", expected: " + expected;
			return false;
		}
	}

	protected boolean reportAboutLockDB(long timeOut, boolean waitForLock) throws Exception {
		generalOid = mibReader.resolveByName("asLteStkPtpStatusMasterSyncLocked");
		String expected = waitForLock ? "1" : "0";
		long time = dut.waitForUpdate(timeOut, generalOid, 1, expected);

		if (waitForLock)
			report.addProperty("TimeCounterPTPLockInMillis", Long.toString(time));

		if (time == GeneralUtils.ERROR_VALUE && waitForLock) {
			report.report("PTP does not locked", Reporter.FAIL);
			reason = "PTP does not locked";
		} else if (time != GeneralUtils.ERROR_VALUE && waitForLock)
			report.report("PTP is locked");

		else if (time == GeneralUtils.ERROR_VALUE && !waitForLock) {
			report.report("PTP is locked, after downloading PTP interface", Reporter.FAIL);
			reason = "PTP is locked, after downloading PTP interface";
		} else if (time != GeneralUtils.ERROR_VALUE && !waitForLock)
			report.report("PTP is unlocked");

		if (time == GeneralUtils.ERROR_VALUE)
			return false;
		else
			return true;
	}

	protected boolean reportAndWaitHoldOverExpiredDB(long timeOut) throws Exception {
		generalOid = mibReader.resolveByName("asLteStkPtpStatusMasterHoldOverExpired");
		long time = dut.waitForUpdate(timeOut, generalOid, 1, "1");
		if (time == GeneralUtils.ERROR_VALUE) {
			report.report("HoldOverExpired = 0", Reporter.FAIL);
			reason = "HoldOver is NOT expired";
			return false;
		} else {
			report.report("HoldOverExpired = 1");
			return true;
		}
	}

	protected boolean reportAndWaitStatusServiceDB(long timeOut, boolean inSrevice) throws Exception {
		generalOid = mibReader.resolveByName("asLteStkChannelStatusServiceState");
		String expected = inSrevice ? "1" : "0";
		long time = dut.waitForUpdate(timeOut, generalOid, 40, expected);

		if (time == GeneralUtils.ERROR_VALUE && inSrevice) {
			report.report("Enb is not In Service", Reporter.FAIL);
			reason = "Enb is not In Service";
		} else if (time != GeneralUtils.ERROR_VALUE && inSrevice)
			report.report("Enb is In Service");

		else if (time == GeneralUtils.ERROR_VALUE && !inSrevice) {
			report.report("After downloading PTP interface, Enb is not Out Of Service", Reporter.FAIL);
			reason = "After downloading PTP interface, Enb is not Out Of Service";
		} else if (time != GeneralUtils.ERROR_VALUE && !inSrevice)
			report.report(" Enb is Out Of Service");

		if (time == GeneralUtils.ERROR_VALUE)
			return false;
		else
			return true;
	}

	protected boolean reportAboutSetMibDB(String name, String mib, int value, Integer index) throws Exception {
		String oid = mibReader.resolveByName(mib);

		generalFlag = dut.snmpSet(oid + "." + index, value);
		if (generalFlag)
			report.report("Succeeded to set DB Parameter " + name + " to " + value);
		else
			report.report("Failed to set DB Parameter " + name + " to " + value, Reporter.WARNING);

		return generalFlag;
	}

	protected void reportAboutNmsSyncProfileConfiguration(SyncParameters syncParams) throws Exception {
		report.report("New Synchronisation Profile Parameters: ");
		report.report("-Primary Master Ip Address : " + syncParams.getPrimaryMasterIpAddress());
		report.report("-Primary Master Domain : " + syncParams.getPrimaryMasterDomain());
		report.report("-Secondary Master Ip Address : " + syncParams.getSecondaryMasterIpAddress());
		report.report("-Secondary Master Domain : " + syncParams.getSecondaryMasterDomain());
		report.report("-Announce Rate : " + syncParams.getAnnounceRateInMsgPerSec());
		report.report("-Sync Rate : " + syncParams.getSyncRateInMsgPerSec());
		report.report("-Delay Rate : " + syncParams.getDelayRequestResponseRateInMsgPerSec());
		report.report("-Lease Duration : " + syncParams.getLeaseDurationInSec());
	}

	protected void checkAndReportAboutVerifyNmsConfiguration(EnodeB dut, SyncParameters syncParams) throws Exception {
		reportAboutOneParameterNmsConfiguration("Primary Master IP Address", syncParams.getPrimaryMasterIpAddress(),
				"asLteStkPtpMasterCfgPrimaryMasterIpAddress", true, false, 1);
		reportAboutOneParameterNmsConfiguration("Primary Master Domain", syncParams.getPrimaryMasterDomain().toString(),
				"asLteStkPtpMasterCfgPrimaryMasterDomain", false, false, 1);
		reportAboutOneParameterNmsConfiguration("Secondary Master IP Address", syncParams.getSecondaryMasterIpAddress(),
				"asLteStkPtpMasterCfgSecondaryMasterIpAddress", true, false, 1);
		reportAboutOneParameterNmsConfiguration("Secondary Master Domain",
				syncParams.getSecondaryMasterDomain().toString(), "asLteStkPtpMasterCfgSecondaryMasterDomain", false,
				false, 1);
		reportAboutOneParameterNmsConfiguration("Announce Rate", syncParams.getAnnounceRateInMsgPerSec().value(),
				"asLteStkPtpMasterCfgAnnounceRate", false, true, 1);
		reportAboutOneParameterNmsConfiguration("Synchronisation Rate", syncParams.getSyncRateInMsgPerSec().value(),
				"asLteStkPtpMasterCfgSyncRate", false, true, 1);
		reportAboutOneParameterNmsConfiguration("Delay Rate",
				syncParams.getDelayRequestResponseRateInMsgPerSec().value(), "asLteStkPtpMasterCfgDelayRate", false,
				true, 1);
		reportAboutOneParameterNmsConfiguration("Lease Duration", syncParams.getLeaseDurationInSec().toString(),
				"asLteStkPtpMasterCfgLeaseDuration", false, false, 1);
	}

	protected void reportAboutOneParameterNmsConfiguration(String parameterName, String expected, String mib,
			boolean mibValueInHexIp, boolean mibValueInBase2, Integer index) {
		String oid = mibReader.resolveByName(mib);
		String actual = "";
		boolean flagBase10Mib = false;

		if (mibValueInHexIp)
			actual = InetAddressesHelper.toDecimalIp(dut.getSNMP(oid, index), 16);
		else
			actual = dut.getSNMP(oid, index);

		if (mibValueInBase2) {
			Double actualBase10 = 1 / (Math.pow(2, Integer.parseInt(actual)));
			flagBase10Mib = Double.parseDouble(expected) == actualBase10;
			actual = actualBase10.toString();
		}

		if (actual.equals(expected) || flagBase10Mib)
			report.report(parameterName + "-> actual: " + actual + ", expected: " + expected);
		else {
			report.report(parameterName + "-> actual: " + actual + ", expected: " + expected, Reporter.FAIL);
			reason = parameterName + "-> actual: " + actual + ", expected: " + expected;
		}
	}

	protected void returnBackToOriginalPTPinterface() {
		if (PTP_IP != null) {
			reportOnPTPInterfaceStatus();
			GeneralUtils.startLevel("Return back to the original 1588(PTP) sub-interface (" + PTP_IP + ")");
			dut.changePTPinterface(PTP_IP);
			GeneralUtils.stopLevel();
			reportOnPTPInterfaceStatus();
		}
	}

	protected void revertToDefaultSyncProfile() throws Exception {
		report.startLevel("Revert to default Synchronization Profile and delete cloned profile");
		if (enodeBConfig.setProfile(dut, EnbProfiles.Sync_Profile, dut.defaultNetspanProfiles.getSynchronisation())) {
			report.report("Wait 2 seconds");
			GeneralUtils.unSafeSleep(2000);
			enodeBConfig.deleteClonedProfiles();
		}
		report.stopLevel();
	}

	@Override
	public void init() throws Exception {
		netspanServer = NetspanServer.getInstance();
		enbInTest = new ArrayList<>();
		enbInTest.add(dut);

		super.init();
		enodeBConfig = EnodeBConfig.getInstance();
		netspanServer = NetspanServer.getInstance();
		mibReader = MibReader.getInstance();
		alarmsAndEvents = AlarmsAndEvents.getInstance();
		status = Statuses.getInstance();
		if(netspanServer.getPrimaryClockSource(dut) != PrimaryClockSourceEnum.IEEE_1588){
			reason = dut.getName() + " is not in 1588 mode";
			report.report(dut.getName() + " is not in 1588 mode",Reporter.WARNING);
			ptpStatus = null;
			return;
		}
		else{
			report.startLevel(dut.getNetspanName() + " trying to get PTP Information");
			ptpStatus = status.getPTPStatus(dut);
			if (ptpStatus == null) {
				reason = dut.getNetspanName() + " failed to get PTP Information";
				PTP_IP = null;
			} else {
				report.report(dut.getNetspanName() + " succeeded to get PTP Information");
				PTP_IP = enodeBConfig.getPTPInterfaceIP(dut);
			}
			report.stopLevel();
		}
		
		String ptpGM = enodeBConfig.getGMIP(dut);
		report.startLevel("ping to the Grand Master");
		reportPing(ptpGM, " Grand Master", 3);
		report.stopLevel();
		
		debugTablesPrint();
		
	}

	/**
	 * prints db get ProcessInformation and db get qcTfcsManagerConfig.
	 * 
	 */
	private void debugTablesPrint() {
		String processInfo = dut.lteCli("db get ProcessInformation");
		String ptpStatus = dut.lteCli("db get PTPStatus");
		
		report.reportHtml(dut.getName() + " - ProcessInformation", processInfo, true);
		
		report.reportHtml(dut.getName() + " - PTPStatus", ptpStatus, true);
	}

	@ParameterProperties(description = "Name of PTP node")
	public void setDUT(String dut) {
		ArrayList<EnodeB> temp = (ArrayList<EnodeB>) SysObjUtils.getInstnce().initSystemObject(EnodeB.class, false, dut);
		this.dut = temp.get(0);
	}
	
	@ParameterProperties(description = "Name of second Enb")
	public void setDUT2(String dut2) {
		ArrayList<EnodeB> temp = (ArrayList<EnodeB>) SysObjUtils.getInstnce().initSystemObject(EnodeB.class, false, dut2);
		this.dut2 = temp.get(0);
	}
	
	public void printTxStatusTable() {
		String ptpMasters = dut.lteCli("db get PTPmasters");
		
		report.reportHtml(dut.getName() + " - ProcessInformation", ptpMasters, true);
	}
	
	public boolean printPTPStatus(long testTime) throws Exception{
		Long currentTime = System.currentTimeMillis();
		while(System.currentTimeMillis() - currentTime <= testTime){
			
			ptpStatus = status.getPTPStatus(dut);
		
			String PTPStatus = dut.lteCli("db get PTPStatus");
			report.reportHtml("db get PTPStatus", PTPStatus, true);
		
			String cellCfg = dut.lteCli("db get nghCellCfg");
			report.reportHtml("db get nghCellCfg", cellCfg, true);
		
			reportOnSyncStatus(true);
			reportOnMasterConnectivity(true,ptpStatus);
		
			GeneralUtils.unSafeSleep(100*60*15);
		}
		
		return true;
	}

	protected void reportOnPTPInterfaceStatus() {
		report.reportHtml("db get ipInterfacesStatus  [" + PTP_INTERFACE_INDEX + "] InterfaceStatus", dut.lteCli("db get ipInterfacesStatus [6] InterfaceStatus"), true);
	}

}
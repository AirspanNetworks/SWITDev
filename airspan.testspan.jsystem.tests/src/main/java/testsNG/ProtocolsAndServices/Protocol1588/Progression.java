package testsNG.ProtocolsAndServices.Protocol1588;

import java.util.Date;
import java.util.List;

import org.junit.Test;

import Netspan.AlarmSeverity;
import Netspan.API.Enums.AnnounceRate;
import Netspan.API.Enums.DelayRate;
import Netspan.API.Enums.SyncRate;
import Netspan.API.Lte.AlarmInfo;
import Netspan.API.Lte.EventInfo;
import Netspan.Profiles.SyncParameters;
import Utils.GeneralUtils;
import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;
import testsNG.ProtocolsAndServices.Protocol1588.Base1588;

public class Progression extends Base1588 {
	@Test // 1
	@TestProperties(name = "NMS_Configuration", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void nmsConfiguration() {
		try {
			if (ptpStatus == null)
				return;

			report.startLevel("Change parameters of Synchronization Profile in Netspan");
			SyncParameters syncParameters = new SyncParameters("20.0.0.100", "20.0.0.100", 3, 3,
					AnnounceRate.HALF_ANNOUNCE, SyncRate.ONE_HUNDRED_TWENTY_EIGHT, DelayRate.EIGHT, 250);
			reportAboutNmsSyncProfileConfiguration(syncParameters);
			this.generalFlag = enodeBConfig.cloneAndSetSyncProfileViaNetSpan(dut,
					dut.getDefaultNetspanProfiles().getSynchronisation(), syncParameters);
			report.stopLevel();

			if (!this.generalFlag) {
				reason = "Failed to clone and set Sync Profile Via NetSpan";
				return;
			}

			report.report("Wait 4 seconds");
			GeneralUtils.unSafeSleep(4000);

			report.startLevel("Verify the parameters are updated accordingly in the DB (PTPmasters table)");
			checkAndReportAboutVerifyNmsConfiguration(dut, syncParameters);
			report.stopLevel();

			revertToDefaultSyncProfile();

		} catch (Exception e) {
			e.printStackTrace();
			report.report("Error: "+e.getMessage(), Reporter.FAIL);
			reason = "Error: "+e.getMessage();
		}
	}

	@Test // 2
	@TestProperties(name = "PTP_Lock", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void ptpLock() {
		try {
			if (ptpStatus == null)
				return;

			report.startLevel("Reboot Enb " + dut.getNetspanName());
			dut.reboot();
			report.stopLevel();

			report.report("Wait for Reachable (TimeOut=5 Minutes)");
			dut.waitForReachable(1000 * 60 * 5);

			report.report("Wait for SNMP will be available (TimeOut=5 Minutes)");
			dut.waitForSnmpAvailable(1000 * 60 * 5);

			report.report("Wait 30 seconds");
			GeneralUtils.unSafeSleep(1000 * 30);

			report.startLevel("Before PTP locked: Verify Clock Source Status Via Netspan");
			ptpStatus = status.getPTPStatusViaNetspan(dut);
			reportAboutOneClockSourceStatusParameter("Synchronization Status", "Acquiring", ptpStatus.syncStatus);
			reportAboutOneClockSourceStatusParameter("Holdover", "No", ptpStatus.holdover);
			report.stopLevel();

			report.startLevel("Wait for PTP Lock (TimeOut=15 Minutes)");
			this.generalFlag = reportAboutLockDB(60 * 1000 * 15, true);
			report.stopLevel();
			if (!this.generalFlag)
				return;

			report.startLevel("After PTP lock: Verify Clock Source Status Via Netspan");
			ptpStatus = status.getPTPStatusViaNetspan(dut);
			reportAboutOneClockSourceStatusParameter("Synchronization Status", "Locked", ptpStatus.syncStatus);
			reportAboutOneClockSourceStatusParameter("Holdover", "No", ptpStatus.holdover);
			report.report("Master Connectivity- " + ptpStatus.masterConnectivity);
			if (ptpStatus.masterConnectivity.contains("Not Connected")) {
				report.report("After PTP lock, Enb does not connected to Master Connectivity", Reporter.FAIL);
				reason = "After PTP lock, Enb does not connected to Master Connectivity";
			}
			report.report("Active Master Status- " + ptpStatus.activeMasterStatus);
			report.stopLevel();

		} catch (Exception e) {
			e.printStackTrace();
			report.report("Error: "+e.getMessage(), Reporter.FAIL);
			reason = "Error: "+e.getMessage();
		}
	}

	@Test // 3
	@TestProperties(name = "Events_And_Alarms_In_HoldOver_State", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void eventsAndAlarmsInHoldOverState() {
		try {
			if (ptpStatus == null)
				return;
			report.startLevel("Remove 1588(PTP) sub-interface (" + PTP_IP + ")");
			dut.downPTPInterface(3);
			report.stopLevel();

			generalFromDate = new Date();

			report.startLevel("Wait for PTP Unlock (TimeOut=5 Minute)");
			this.generalFlag = reportAboutLockDB(60 * 1000 * 5, false);
			report.stopLevel();
			if (!this.generalFlag)
				return;

			report.startLevel("Verify DB field PTPStatus");
			reportAboutMibDB("Lock", "0", "asLteStkPtpStatusMasterSyncLocked", 1);
			reportAboutMibDB("HoldOver", "1", "asLteStkPtpStatusMasterHoldOver", 1);
			reportAboutMibDB("MasterConnectivity", "0", "asLteStkPtpStatusMasterConnected", 1);
			report.stopLevel();

			report.startLevel("Verify Clock Source Status Via Netspan");
			ptpStatus = status.getPTPStatusViaNetspan(dut);
			reportAboutOneClockSourceStatusParameter("Synchronization Status", "Acquiring", ptpStatus.syncStatus);
			reportAboutOneClockSourceStatusParameter("Holdover", "Yes", ptpStatus.holdover);
			report.report("Holdover Expired Time: " + ptpStatus.holdExpiredTime.getValue() + " seconds");
			if (ptpStatus.holdExpiredTime.getValue() > 150) {
				report.report("Holdover Expired Time counting up from 2:30 minutes", Reporter.FAIL);
				reason = "Holdover Expired Time counting up from 2:30 minutes";
			}
			reportAboutOneClockSourceStatusParameter("Holdover Expired", "Not Expired", ptpStatus.holdoverExpired);
			report.stopLevel();

			report.startLevel("Check Alarms");
			report.report("Wait 1 minute");
			GeneralUtils.unSafeSleep(1000 * 60);
			List<AlarmInfo> allNodeAlarms = alarmsAndEvents.getAllAlarmsNode(dut);
			reportAboutExistAlarmWithSeverity(allNodeAlarms, "Node in Holdover", AlarmSeverity.MAJOR);
			reportAboutExistAlarmWithSeverity(allNodeAlarms, "Node PTP Grand Master Lost", AlarmSeverity.MAJOR);
			report.stopLevel();

			generalToDate = new Date();
			report.startLevel(
					"Check Events Ranged " + format.format(generalFromDate) + " - " + format.format(generalToDate));
			List<EventInfo> allNodeEvents = alarmsAndEvents.getEventsNodeByDateRange(dut, generalFromDate,
					generalToDate);
			// Node PTP Degraded stream
			reportAboutExistEventWithInfo(allNodeEvents, "Node Synchronization Lock Status Change",
					"All Configured clock sources unavailable: eNodeB in Holdover");
			reportAboutExistEventWithInfo(allNodeEvents, "Node PTP Grand Master Status Change",
					"None of the configured Grand Masters are available");
			report.stopLevel();

			report.startLevel("Return back 1588(PTP) sub-interface (" + PTP_IP + ", 255.255.255.0)");
			dut.upPTPInterface(PTP_IP, 3);
			report.stopLevel();

			generalFromDate = new Date();

			report.report("Wait 10 seconds");
			GeneralUtils.unSafeSleep(1000 * 10);

			report.startLevel("Wait for PTP Lock (TimeOut=15 Minutes)");
			this.generalFlag = reportAboutLockDB(60 * 1000 * 15, true);
			report.stopLevel();
			if (!this.generalFlag)
				return;
			else
				reportAndWaitStatusServiceDB(60 * 1000 * 15, true);

			report.startLevel("Verify DB field PTPStatus");
			reportAboutMibDB("Lock", "1", "asLteStkPtpStatusMasterSyncLocked", 1);
			reportAboutMibDB("HoldOverExpired", "0", "asLteStkPtpStatusMasterHoldOverExpired", 1);
			reportAboutMibDB("MasterConnectivity", "1", "asLteStkPtpStatusMasterConnected", 1);
			report.stopLevel();

			report.startLevel("Verify Clock Source Status Via Netspan");
			ptpStatus = status.getPTPStatusViaNetspan(dut);
			reportAboutOneClockSourceStatusParameter("Synchronization Status", "Locked", ptpStatus.syncStatus);
			reportAboutOneClockSourceStatusParameter("Holdover", "No", ptpStatus.holdover);
			report.stopLevel();

			report.startLevel("Check Alarms");
			allNodeAlarms = alarmsAndEvents.getAllAlarmsNode(dut);
			reportAboutExistAlarmWithSeverity(allNodeAlarms, "Node in Holdover", AlarmSeverity.NORMAL);
			reportAboutExistAlarmWithSeverity(allNodeAlarms, "Node PTP Grand Master Lost", AlarmSeverity.NORMAL);
			report.stopLevel();

			generalToDate = new Date();
			report.startLevel(
					"Check Events Ranged " + format.format(generalFromDate) + " - " + format.format(generalToDate));
			allNodeEvents = alarmsAndEvents.getEventsNodeByDateRange(dut, generalFromDate, generalToDate);
			// Node PTP Degraded stream
			reportAboutExistEventWithInfo(allNodeEvents, "Node Synchronization Lock Status Change",
					"Synchronization Locked: Normal Operation");
			report.stopLevel();

		} catch (Exception e) {
			e.printStackTrace();
			report.report("Error: "+e.getMessage(), Reporter.FAIL);
			reason = "Error: "+e.getMessage();
		}
	}

	@Test // 4
	@TestProperties(name = "Events_And_Alarms_After_HoldOver_Expire", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void eventsAndAlarmsAfterHoldOverExpire() {
		try {
			if (ptpStatus == null)
				return;
			report.startLevel("Remove 1588(PTP) sub-interface (" + PTP_IP + ")");
			dut.downPTPInterface(3);
			report.stopLevel();

			generalFromDate = new Date();

			report.startLevel("Wait until HoldOver will be expired (TimeOut=10 Minutes)");
			this.generalFlag = reportAndWaitHoldOverExpiredDB(60 * 1000 * 10);
			report.stopLevel();
			if (!this.generalFlag)
				return;

			report.report("Wait 2.5 minutes");
			GeneralUtils.unSafeSleep(1000 * 60 * 2 + 1000 * 30);

			if (!dut.getCellServiceState().equals("0")) {
				report.report("After downloading PTP Interface and HoldOver is expired, Enb is NOT Out Of Service");
				reason = "After downloading PTP Interface and HoldOver is expired, Enb is NOT Out Of Service";
				return;
			} else
				report.report("Enb is Out Of Service");

			report.startLevel("Verify DB field PTPStatus");
			reportAboutMibDB("HoldOver", "0", "asLteStkPtpStatusMasterHoldOver", 1);
			reportAboutMibDB("HoldOverExpired", "1", "asLteStkPtpStatusMasterHoldOverExpired", 1);
			report.stopLevel();

			report.startLevel("Verify Clock Source Status Via Netspan");
			ptpStatus = status.getPTPStatusViaNetspan(dut);
			reportAboutOneClockSourceStatusParameter("Synchronization Status", "Acquiring", ptpStatus.syncStatus);
			reportAboutOneClockSourceStatusParameter("Holdover", "No", ptpStatus.holdover);
			reportAboutOneClockSourceStatusParameter("Holdover Expired Time", "0",
					ptpStatus.holdExpiredTime.getValue().toString());
			reportAboutOneClockSourceStatusParameter("Holdover Expired", "Expired", ptpStatus.holdoverExpired);
			report.stopLevel();

			report.startLevel("Check Alarms");
			List<AlarmInfo> allNodeAlarms = alarmsAndEvents.getAllAlarmsNode(dut);
			reportAboutExistAlarmWithSeverity(allNodeAlarms, "Node in Holdover", AlarmSeverity.NORMAL);
			reportAboutExistAlarmWithSeverity(allNodeAlarms, "Node PTP Grand Master Lost", AlarmSeverity.MAJOR);
			reportAboutExistAlarmWithSeverity(allNodeAlarms, "Channel Out of Service (OOS)", AlarmSeverity.CRITICAL);
			reportAboutExistAlarmWithSeverity(allNodeAlarms, "RF1 Transmitter Off", AlarmSeverity.MAJOR);
			reportAboutExistAlarmWithSeverity(allNodeAlarms, "RF2 Transmitter Off", AlarmSeverity.MAJOR);
			reportAboutExistAlarmWithSeverity(allNodeAlarms, "Node Loss of Sync", AlarmSeverity.CRITICAL);
			report.stopLevel();

			generalToDate = new Date();
			report.startLevel(
					"Check Events Ranged " + format.format(generalFromDate) + " - " + format.format(generalToDate));
			List<EventInfo> allNodeEvents = alarmsAndEvents.getEventsNodeByDateRange(dut, generalFromDate,
					generalToDate);
			reportAboutExistEventWithInfo(allNodeEvents, "Channel Status Change", "Out of Service");
			reportAboutExistEventWithInfo(allNodeEvents, "Node Synchronization Lock Status Change",
					"All Configured clock sources unavailable: eNodeB in Holdover");
			reportAboutExistEventWithInfo(allNodeEvents, "Node PTP Grand Master Status Change",
					"None of the configured Grand Masters are available");
			reportAboutExistEventWithInfo(allNodeEvents, "RF1 Status Change", "Alarm Sync: Out of Service");
			reportAboutExistEventWithInfo(allNodeEvents, "RF2 Status Change", "Alarm Sync: Out of Service");
			report.stopLevel();

			report.startLevel("Return back 1588(PTP) sub-interface (" + PTP_IP + ", 255.255.255.0)");
			if (!dut.upPTPInterface(PTP_IP, 3))
			{
				report.stopLevel();
				return;
			}
			report.stopLevel();

			generalFromDate = new Date();

			report.startLevel("Wait for In Service (TimeOut=15 Minutes)");
			generalFlag = reportAndWaitStatusServiceDB(60 * 1000 * 15, true);
			report.stopLevel();
			if (!generalFlag)
				return;

			report.report("Wait 2.5 minutes");
			GeneralUtils.unSafeSleep(1000 * 60 * 2 + 1000 * 30);

			report.startLevel("Verify DB field PTPStatus");
			reportAboutMibDB("Lock", "1", "asLteStkPtpStatusMasterSyncLocked", 1);
			reportAboutMibDB("HoldOverExpired", "0", "asLteStkPtpStatusMasterHoldOverExpired", 1);
			reportAboutMibDB("MasterConnectivity", "1", "asLteStkPtpStatusMasterConnected", 1);
			report.stopLevel();

			report.startLevel("Verify Clock Source Status Via Netspan");
			ptpStatus = status.getPTPStatusViaNetspan(dut);
			reportAboutOneClockSourceStatusParameter("Synchronization Status", "Locked", ptpStatus.syncStatus);
			reportAboutOneClockSourceStatusParameter("Holdover", "No", ptpStatus.holdover);
			report.stopLevel();

			report.startLevel("Check Alarms");
			allNodeAlarms = alarmsAndEvents.getAllAlarmsNode(dut);
			reportAboutExistAlarmWithSeverity(allNodeAlarms, "Node Loss of Sync", AlarmSeverity.NORMAL);
			reportAboutExistAlarmWithSeverity(allNodeAlarms, "Node PTP Grand Master Lost", AlarmSeverity.NORMAL);
			reportAboutExistAlarmWithSeverity(allNodeAlarms, "Channel Out of Service (OOS)", AlarmSeverity.NORMAL);
			reportAboutExistAlarmWithSeverity(allNodeAlarms, "RF1 Transmitter Off", AlarmSeverity.NORMAL);
			reportAboutExistAlarmWithSeverity(allNodeAlarms, "RF2 Transmitter Off", AlarmSeverity.NORMAL);
			report.stopLevel();

			generalToDate = new Date();
			report.startLevel(
					"Check Events Ranged " + format.format(generalFromDate) + "-" + format.format(generalToDate));
			allNodeEvents = alarmsAndEvents.getEventsNodeByDateRange(dut, generalFromDate, generalToDate);
			reportAboutExistEventWithInfo(allNodeEvents, "Channel Status Change", "In Service");
			// "Node PTP Degraded stream"
			reportAboutExistEventWithInfo(allNodeEvents, "Node Synchronization Lock Status Change",
					"Synchronization Locked: Normal Operation");
			reportAboutExistEventWithInfo(allNodeEvents, "RF1 Status Change",
					"Alarm Sync: In-Service (Normal Operation)");
			reportAboutExistEventWithInfo(allNodeEvents, "RF2 Status Change",
					"Alarm Sync: In-Service (Normal Operation)");
			report.stopLevel();

		} catch (Exception e) {
			e.printStackTrace();
			report.report("Error: "+e.getMessage(), Reporter.FAIL);
			reason = "Error: "+e.getMessage();
		}
	}

	// @Test 5 //NOT Ready
	@TestProperties(name = "Events_And_Alarms_In_High_Jitter_State", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void eventsAndAlarmsInHighJitterState() {
		try {
			if (ptpStatus == null)
				return;

			report.startLevel("Set DB Parameter PTPmasters->ptpPDVthreshold to 200");
			reportAboutSetMibDB("PTPmasters->ptpPDVthreshold", "asLteStkPtpMasterCfgPdvThreshold", 200, 1);
			report.stopLevel();

			report.startLevel("Verify DB field PTPStatus");
			reportAboutMibDB("Lock", "1", "asLteStkPtpStatusMasterSyncLocked", 1);
			reportAboutMibDB("HoldOver", "0", "asLteStkPtpStatusMasterHoldOver", 1);
			// reportAboutMibDB("HighJitter", "1",
			// "asLteStkPtpStatusMasterHoldOverExpired", "1");
			report.stopLevel();

			report.startLevel("Check Alarms");
			List<AlarmInfo> allNodeAlarms = alarmsAndEvents.getAllAlarmsNode(dut);
			// Node PTP Degraded Stream; Status=highjitter, Info=Lock=1,
			// HOreason=0
			// (None)
			report.stopLevel();

			report.startLevel("Check Events");
			// Node PTP Degraded Stream; Status=highjitter, Info=Lock=1,
			// HOreason=0
			// (None)
			report.stopLevel();

			report.startLevel("Set DB Parameter PTPmasters->ptpPDVthreshold back to 10000");
			reportAboutSetMibDB("PTPmasters->ptpPDVthreshold", "asLteStkPtpMasterCfgPdvThreshold", 10000, 1);
			report.stopLevel();

			report.startLevel("Verify DB field PTPStatus");
			reportAboutMibDB("Lock", "1", "asLteStkPtpStatusMasterSyncLocked", 1);
			reportAboutMibDB("HoldOver", "0", "asLteStkPtpStatusMasterHoldOver", 1);
			// reportAboutMibDB("HighJitter", "0", "", "1");
			report.stopLevel();

		} catch (Exception e) {
			e.printStackTrace();
			report.report("Error: "+e.getMessage(), Reporter.FAIL);
			reason = "Error: "+e.getMessage();
		}
	}
}

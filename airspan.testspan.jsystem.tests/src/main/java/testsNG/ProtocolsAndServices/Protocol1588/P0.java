package testsNG.ProtocolsAndServices.Protocol1588;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import Netspan.API.Lte.EventInfo;
import Utils.GeneralUtils;
import Utils.InetAddressesHelper;
import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;

public class P0 extends Base1588 {

	@Test 
	@TestProperties(name = "VLAN_ID_&_PBIT_Configuration", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful","DUT2" })
	public void vlanIdAndPbitConfiguration() {
		if (ptpStatus == null)
			return;
		try {
			
			report.reportHtml("db get PTPmasters", dut.lteCli("db get PTPmasters"), true);
			report.reportHtml("db get PTPStatus", dut.lteCli("db get PTPStatus"), true);
			
			int tries = 3;
			String ptpIP = enodeBConfig.getPTPInterfaceIP(dut);
			report.startLevel("ping to the Network interface");
			reportPing(ptpIP, "Network interface", tries);
			report.stopLevel();

			String ptpGW = InetAddressesHelper.changeIP(ptpIP, 3, "254");
			report.startLevel("ping to the closest router");
			reportPing(ptpGW, "closest router", tries);
			report.stopLevel();

			String ptpGM = enodeBConfig.getGMIP(dut);
			report.startLevel("ping to the Grand Master");
			reportPing(ptpGM, " Grand Master", tries);
			report.stopLevel();

		} catch (Exception e) {
			e.printStackTrace();
			report.report("Error: "+e.getMessage(), Reporter.FAIL);
			reason = "Error: "+e.getMessage();
		}
	}

	@Test // 5
	@TestProperties(name = "1588_NMS_Status", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful","DUT2" })
	public void nmsStatus1588() {
		if (ptpStatus == null)
			return;

		try {
			reportOnSyncStatus(true);
			printTxStatusTable();
		} catch (Exception e) {
			e.printStackTrace();
			report.report("Error: "+e.getMessage(), Reporter.FAIL);
			reason = "Error: "+e.getMessage();
		}
	}

	

	@Test // 6
	@TestProperties(name = "1588_NMS_Alarms", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful","DUT2" })
	public void nmsAlarms1588() {
		if (ptpStatus == null)
			return;

			reportOnSyncStatus(true);
			reportOnMasterConnectivity(true, ptpStatus);
			reportOnEnbState(true);

			reportOnPTPInterfaceStatus();
			
			GeneralUtils.startLevel("Remove 1588(PTP) sub-interface (" + PTP_IP + ")");
			generalFromDate = new Date();
			if (!dut.downPTPInterface(3)) {
				GeneralUtils.stopLevel();
				return;
			}
			GeneralUtils.stopLevel();
			reportOnPTPInterfaceStatus();
			
			report.report("Wait 30 seconds");
			GeneralUtils.unSafeSleep(1000 * 30);

			ptpStatus = status.getPTPStatusViaNetspan(dut);
			if (ptpStatus != null) {
				reportOnSyncStatus(false);
				reportOnMasterConnectivity(false, ptpStatus);
			}
			else
			{
				report.report("Failed getting ptpStatus from netspan.",Reporter.WARNING);
			}
			GeneralUtils.startLevel("Searching for event");
			generalToDate = new Date();
			List<EventInfo> allNodeEvents = alarmsAndEvents.getEventsNodeByDateRange(dut, generalFromDate,
					generalToDate);
			reportAboutExistEventWithInfo(allNodeEvents, "Node PTP Grand Master Status Change", "None of the configured Grand Masters are available");
			GeneralUtils.stopLevel();

			reportOnPTPInterfaceStatus();
			GeneralUtils.startLevel("Return back 1588(PTP) sub-interface (" + PTP_IP + ", 255.255.255.0)");
			generalFromDate = new Date();
			dut.upPTPInterface(PTP_IP, 3);
			GeneralUtils.stopLevel();
			reportOnPTPInterfaceStatus();
			
			report.report("Wait 60 seconds");
			GeneralUtils.unSafeSleep(1000 * 60);

			GeneralUtils.startLevel("Searching for event");
			generalToDate = new Date();
			allNodeEvents = alarmsAndEvents.getEventsNodeByDateRange(dut, generalFromDate,
					generalToDate);
			reportAboutExistEventWithInfo(allNodeEvents, "Node PTP Quality Status Change", "Lock=1");
			GeneralUtils.stopLevel();

			ptpStatus = status.getPTPStatusViaNetspan(dut);
			if (ptpStatus != null) {
				reportOnSyncStatus(true);
				reportOnMasterConnectivity(true, ptpStatus);
			}
			else
			{
				report.report("Failed getting ptpStatus from netspan.",Reporter.WARNING);
			}

			reportOnPTPInterfaceStatus();
			
			String WRONG_IP = InetAddressesHelper.changeIP(PTP_IP, 2, "50");
			GeneralUtils.startLevel("Modify 1588(PTP) sub-interface to wrong network (" + WRONG_IP + ")");
			generalFromDate = new Date();
			report.report("Remove current PTP interface");
			dut.downPTPInterface(3);
			String command = "ifconfig br0.6 " + WRONG_IP + " netmask 255.255.255.0 up";
			report.report("creating new wrong interface: " + command);
			dut.shell(command);
			GeneralUtils.stopLevel();
			
			reportOnPTPInterfaceStatus();
			report.report("Wait 20 seconds");
			GeneralUtils.unSafeSleep(1000 * 20);

			ptpStatus = status.getPTPStatusViaNetspan(dut);
			if (ptpStatus != null) {
				reportOnSyncStatus(false);
				reportOnMasterConnectivity(false, ptpStatus);
			}
			else
			{
				report.report("Failed getting ptpStatus from netspan.",Reporter.WARNING);
			}
			
			GeneralUtils.startLevel("Searching for event");
			generalToDate = new Date();
			allNodeEvents = alarmsAndEvents.getEventsNodeByDateRange(dut, generalFromDate,
					generalToDate);
			reportAboutExistEventWithInfo(allNodeEvents, "Node PTP Grand Master Status Change", "None of the configured Grand Masters are available");
			GeneralUtils.stopLevel();
			
			returnBackToOriginalPTPinterface();

	}

	@Test // 7
	@TestProperties(name = "System_Behavior_After_Holdover_Time_Expired", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful","DUT2" })
	public void systemBehaviorAfterHoldoverTimeExpired() {
		if (ptpStatus == null)
			return;

		GeneralUtils.startLevel("Searching alarms with type 'node in holdover'");
		List<String> alarmType = new ArrayList<String>();
		alarmType.add("node in holdover");
		alarmType.add("eNodeB is in holdover");
		reportOnSearchAlarms(false,alarmType);
		GeneralUtils.stopLevel();
		
		ptpStatus = status.getPTPStatus(dut);
		if (ptpStatus == null) {
			report.report(dut.getName() + " failed to get PTP Information",Reporter.FAIL);
			return;
		}
		reportOnEnbState(true);
		reportOnSyncStatus(true);

		reportOnPTPInterfaceStatus();
		
		GeneralUtils.startLevel("Remove 1588(PTP) sub-interface (" + PTP_IP + ")");
		if (!dut.downPTPInterface(3)) {
			GeneralUtils.stopLevel();
			return;
		}
		GeneralUtils.stopLevel();
		
		reportOnPTPInterfaceStatus();
		
		dut.setExpectBooting(true);
		report.report("wait 5 minutes");
		GeneralUtils.unSafeSleep(1000*5*60);

		ptpStatus = status.getPTPStatus(dut);
		if (ptpStatus == null) {
			report.report(dut.getName() + " failed to get PTP Information",Reporter.FAIL);
			return;
		}
		
		reportOnEnbState(false);
		reportOnSyncStatus(false);
		
		reportOnPTPInterfaceStatus();
		GeneralUtils.startLevel("Return back 1588(PTP) sub-interface (" + PTP_IP + ", 255.255.255.0)");
		dut.upPTPInterface(PTP_IP, 3);
		GeneralUtils.stopLevel();
		
		reportOnPTPInterfaceStatus();
		
		report.report("Wait for PTP Lock (TimeOut=15 Minutes)");
		String oid = mibReader.resolveByName("asLteStkPtpStatusMasterSyncLocked");
		long timeCounterPTPLockInMillis = dut.waitForUpdate(60 * 1000 * 15, oid, 1, "1");
		dut.expecteInServiceState = true;
		if (timeCounterPTPLockInMillis == GeneralUtils.ERROR_VALUE) {
			report.report("After Returning back 1588(PTP) sub-interface, PTP does not locked", Reporter.FAIL);
			reason = "After Returning back 1588(PTP) sub-interface, PTP does not locked";
			return;
		}

		ptpStatus = status.getPTPStatus(dut);
		if (ptpStatus == null) {
			report.report(dut.getName() + " failed to get PTP Information",Reporter.FAIL);
			return;
		}
		reportOnSyncStatus(true);
	}

	@Test // 9
	@TestProperties(name = "Time_To_Initial_Lock_To_Master", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful","DUT2" })
	public void timeToInitialLockToMaster() {
		if (ptpStatus == null)
			return;

		GeneralUtils.startLevel("Reboot Enb " + dut.getNetspanName());
		dut.reboot();
		GeneralUtils.stopLevel();

		report.report(dut.getNetspanName() + ": Wait for Reachable (TimeOut=15 Minutes)");
		dut.waitForReachable(1000 * 60 * 15);

		GeneralUtils.startLevel(dut.getNetspanName() + ": Wait for In Service (TimeOut=15 Minutes)");
		if (dut.waitForAllRunningAndInService(1000 * 60 * 15))
			report.report("Enb " + dut.getNetspanName() + " is In Service");
		else {
			report.report("Enb " + dut.getNetspanName() + " is not In Service after 15 minutes", Reporter.FAIL);
			reason = "Enb " + dut.getNetspanName() + " is not In Service after 15 minutes";
		}
		GeneralUtils.stopLevel();
		
		reportOnPTPInterfaceStatus();
		
		GeneralUtils.startLevel("Remove 1588(PTP) sub-interface (" + PTP_IP + ")");
		if (!dut.downPTPInterface(3)) {
			return;
		}
		GeneralUtils.stopLevel();

		reportOnPTPInterfaceStatus();
		
		report.report("Wait 30 seconds");
		GeneralUtils.unSafeSleep(1000 * 30);

		GeneralUtils.startLevel("Searching alarms with type 'node in holdover'");
		List<String> alarmToSearch = new ArrayList<String>();
		alarmToSearch.add("node in holdover");
		alarmToSearch.add("eNodeB is in holdover");
		reportOnSearchAlarms(true,alarmToSearch);
		GeneralUtils.stopLevel();

		reportOnPTPInterfaceStatus();
		
		GeneralUtils.startLevel("Return back 1588(PTP) sub-interface (" + PTP_IP + ", 255.255.255.0)");
		dut.upPTPInterface(PTP_IP, 3);
		GeneralUtils.stopLevel();

		reportOnPTPInterfaceStatus();
		
		GeneralUtils.startLevel(dut.getName() + ": Wait for In Service (TimeOut=15 Minutes)");
		if (dut.waitForAllRunningAndInService(1000 * 60 * 15))
			report.report("Enb " + dut.getNetspanName() + " is In Service");
		else {
			report.report("Enb " + dut.getNetspanName() + " is not In Service after 15 minutes", Reporter.FAIL);
			reason = "Enb " + dut.getNetspanName() + " is not In Service after 15 minutes";
		}
		GeneralUtils.stopLevel();

		ptpStatus = status.getPTPStatusViaNetspan(dut);
		if (ptpStatus == null) {
			report.report(dut.getName() + " failed to get PTP Information",Reporter.FAIL);
			return;
		}
		reportOnMasterConnectivity(true, ptpStatus);

	}

	// @Test // 11
	/**
	 * DUT is a velocity instance and 2nd node is not velocity 
	 */
	@Test
	@TestProperties(name = "PTP_Validation_By_ANR", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void ptpValidationByANR() {
		if (ptpStatus == null)
			return;
		anrTest = new testsNG.SON.ANR.P0();
		anrTest.setDUT1(dut.getName());
		anrTest.setDUT2(dut2.getName());
		try{
			anrTest.init();
			anrTest.addPeriodicAnrIntraFrequencyNeighborsNoPost();
			
			GeneralUtils.startLevel("print PTP status Every 1.5 min");
			printPTPStatus(TEST_TIME);
			GeneralUtils.stopLevel();
			
			anrTest.postTest();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
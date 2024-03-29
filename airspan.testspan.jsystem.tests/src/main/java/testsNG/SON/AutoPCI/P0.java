package testsNG.SON.AutoPCI;

import java.util.Date;
import java.util.List;
import java.util.Random;

import org.junit.Test;

import EnodeB.EnodeB;
import Netspan.API.Lte.EventInfo;
import Netspan.API.Lte.SONStatus;
import Utils.GeneralUtils;
import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;

public class P0 extends AutoPCIBase {
	
	@Test // 1
	@TestProperties(name = "Allocation_Algorithm_Without_Neighbor_Configuration", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void allocationAlgorithmWithoutNeighborConfiguration() {
		configureAutoPciToEnableViaNms(pciStart, pciEnd);
		if(shouldReboot){
			report.report("eNB should reboot once");

			dut.setExpectBooting(true);

			report.report("Wait 1 minute");
			GeneralUtils.unSafeSleep(1000 * 60);

			report.report(dut.getNetspanName() + " Wait for all running and in service (TimeOut=15 Minutes)");
			dut.waitForAllRunningAndInService(EnodeB.WAIT_FOR_ALL_RUNNING_TIME);	
		}else{
			report.report("eNB should not reboot. Wait up to 40 seconds to verify");
			dut.setExpectBooting(true);
			if(dut.waitForReboot(40*1000)){
				report.report("EnodeB was rebooted - not expected",Reporter.FAIL);
				dut.waitForAllRunningAndInService(EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
			}else{
				report.report("EnodeB was not rebooted - as expected");
			}
			dut.setExpectBooting(false);
		}

		
		enodeBConfig.printEnodebState(dut, true);

		report.reportHtml("db get AutoPciCell", dut.lteCli("db get AutoPciCell"), true);
		SONStatus SONStatus = status.getSONStatus(dut);
		printSONStatus(SONStatus, null);
		validateInNmsSonStatusPciTakenFromValidRange(SONStatus, pciStart, pciEnd);

		startTrafficAndCheckIfUEConnected();
	}

	@Test // 2
	@TestProperties(name = "Allocation_Algorithm_With_9_3rd_Party_Enb_Neighbor_Configuration", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void allocationAlgorithmWith9rd3PartyEnbNeighborConfiguration() {
		changeEnodeBPciAndReboot();
		startTrafficAndCheckIfUEConnected();

		GeneralUtils.startLevel("Configure 9 3rd Party eNB with PCIs between " + pciStart + "-" + pciEnd + " (without "
				+ pciStart + ") and same Downlink EARFCN=" + dut.getEarfcn() + ", and add to eNB");
		String cmd = addr.getHostAddress();
		String[] separated = cmd.split("\\.");
		boolean isIpv6 = dut.getIpAddress().contains(":");
		Random rand = new Random();
		for (int i = 0, indexPci = pciStart; i < 9; i++, indexPci++) {
			int random = rand.nextInt(254)+1;
			String IPAdress = i + "."+random+"." + separated[2] + "." + separated[3];
			if(isIpv6){
	        	IPAdress = "abcd::" + i + ":"+random+":" + separated[2] + ":" + separated[3];
			}
			if (indexPci == pciStart) {
				indexPci++;
			}
			addingToEnbViaNmsNeighborWithStaticPCI(dut, addr.getHostName() + "_AutoPCI_" +i+"_"+ random, IPAdress, indexPci, random);
		}
		report.reportHtml("db get nghList", dut.lteCli("db get nghList") , true);
		GeneralUtils.stopLevel();

		report.report("Wait 10 seconds");
		GeneralUtils.unSafeSleep(1000 * 10);

		GeneralUtils.startLevel("Checking if EnodeB is still with same static PCI and UE still connected");
		if (pciStart != getPropertiesStaticPCI(dut)) {
			report.report(dut.getNetspanName() + " didn't have same static PCI number (WAS: " + pciStart + ", NOW: "
					+ getPropertiesStaticPCI(dut) + ")", Reporter.FAIL);
			reason = "Enb didn't have same static PCI number";
		} else {
			report.report(dut.getNetspanName() + " still with same static PCI number : " + pciStart);
		}
		reason += peripheralsConfig.checkUesConnection(dut, testUeList, Reporter.WARNING);
		GeneralUtils.stopLevel();
		
		report.report("Wait 5 seconds");
		GeneralUtils.unSafeSleep(1000 * 5);

		configureAutoPciToEnableViaNms(pciStart, pciEnd);

		report.report("eNB should not reboot. Wait up to 20 seconds to verify");
		dut.setExpectBooting(true);
		if(dut.waitForReboot(20*1000)){
			report.report("EnodeB was rebooted - not expected",Reporter.FAIL);
			dut.waitForAllRunningAndInService(EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
		}else{
			report.report("No reboot issue (because free PCI of range is = static PCI)");
		}
		
		dut.setExpectBooting(false);

		enodeBConfig.printEnodebState(dut, true);

		report.reportHtml("db get AutoPciCell", dut.lteCli("db get AutoPciCell"), true);
		SONStatus newSONStatus = status.getSONStatus(dut);
		printSONStatus(newSONStatus, "Automatic");

		reportAboutMibDB("cellAutoPciEnabled", "1");

		validateInNmsSonStatusPciTakenFromValidRange(status.getSONStatus(dut), pciStart, pciEnd);

		checkConnectionUEWithStopStartAndRetries();
		
		GeneralUtils.startLevel("Delete All new 3rdParty");
		neighborsUtils.deleteAllNeighbors(dut);
		neighborsUtils.delete3rdPartyList(list3Party);
		GeneralUtils.stopLevel();
	}

	@Test // 3
	@TestProperties(name = "Allocation_Success_With_Policy_Violation_Detected", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void allocationSuccessWithPolicyViolationDetected() {
		changeEnodeBPciAndReboot();
		Date from = new Date();
		List<String> neighbors = netspan.getNodeNeighborsName(dut);
		if (!neighbors.isEmpty()) {
			report.report(dut.getNetspanName() + " have static Neighbours");
			boolean flag = netspan.deleteAllNeighbors(dut);
			if (flag) {
				report.report("Delete all Neighbors of " + dut.getNetspanName() + " succeeded");
			}
		} else {
			report.report(dut.getNetspanName() + " didn't have static Neighbour Configuration");
		}
		
		SONStatus sonStatus = netspan.getSONStatus(dut);
		printRSIPCIDebug(sonStatus);
		
		startTrafficAndCheckIfUEConnected();

		configureAutoPciToEnableViaNms(pciStart, pciEnd);
		
		report.report("eNB should not reboot. Wait up to 20 seconds to verify");
		dut.setExpectBooting(true);
		if(dut.waitForReboot(20*1000)){
			report.report("EnodeB was rebooted - not expected",Reporter.FAIL);
			dut.waitForAllRunningAndInService(EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
		}else{
			report.report("EnodeB was not rebooted - as expected");
		}
		dut.setExpectBooting(false);

		enodeBConfig.printEnodebState(dut, true);

		report.reportHtml("db get AutoPciCell", dut.lteCli("db get AutoPciCell"), true);
		SONStatus SONStatus = status.getSONStatus(dut);
		printSONStatus(SONStatus, "Automatic");

		if (getPropertiesStaticPCI(dut) != pciStart) {
			report.report(dut.getNetspanName() + " didn't have same static PCI number (WAS: " + pciStart + ", NOW: "
					+ getPropertiesStaticPCI(dut) + ")", Reporter.FAIL);
			reason = "Enb didn't have same static PCI number";
		} else {
			report.report(dut.getNetspanName() + " still with same static PCI number: " + pciStart);
		}

		report.reportHtml("cell show autoPCI list=1", dut.lteCli("cell show autoPCI list=1"), true);

		String cmd = addr.getHostAddress();
		GeneralUtils.printToConsole("**** Host address: "+cmd+" ****");
		String[] separated = cmd.split("\\.");
		Random rand = new Random();
		int random = rand.nextInt(254)+1;
		String IPAdress = "" + 0 + "."+random+"." + separated[2] + "." + separated[3];

		if(dut.getIpAddress().contains(":"))
        	IPAdress = "abcd::" + 0 + ":"+random+":" + separated[2] + ":" + separated[3];
		
		if(!addingToEnbViaNmsNeighborWithStaticPCI(dut, addr.getHostName() + "_AutoPCI_"+random, IPAdress, pciStart+3, random)){
			report.report("Failed to add neighbor",Reporter.FAIL);
			report.reportHtml("db get nghList", dut.lteCli("db get nghList") , true);
			neighborsUtils.delete3rdPartyList(list3Party);
			return;
		}

		report.reportHtml("db get nghList", dut.lteCli("db get nghList") , true);
		report.report("Wait 15 seconds");
		GeneralUtils.unSafeSleep(1000 * 15);
		report.reportHtml("cell show autoPCI list=1", dut.lteCli("cell show autoPCI list=1"), true);

		dut.setExpectBooting(true);
		Date to = new Date();
		List<EventInfo> timeEvents = alarmsAndEvents.getEventsNodeByDateRange(dut, from, to);
		List<EventInfo> events = alarmsAndEvents.getAllEventsNodeWithSpecificInfo(timeEvents, "PCI Policy Violation Detected");
		if (events.isEmpty()) {
			report.report("Event 'PCI Policy Violation Detected' does not exist", Reporter.FAIL);
			reason = "Event 'PCI Policy Violation Detected' does not exist";
		}else{
			GeneralUtils.startLevel("After adding new neighbor, event 'PCI Policy Violation Detected' exists");
			for(EventInfo event: events){
				alarmsAndEvents.printEventInfo(event);
			}
			GeneralUtils.stopLevel();
		}

		report.report("Wait 90 seconds");
		GeneralUtils.unSafeSleep(1000 * 90);
		
		to = new Date();
		timeEvents = alarmsAndEvents.getEventsNodeByDateRange(dut, from, to);
		events = alarmsAndEvents.getAllEventsNodeWithSpecificInfo(timeEvents, "PCI Policy Violation Resolved");
		if (events.isEmpty()) {
			report.report("Event 'PCI Policy Violation Resolved' does not exist", Reporter.FAIL);
			reason = "Event 'PCI Policy Violation Resolved' does not exist";
		}else{
			GeneralUtils.startLevel("After adding new neighbor, event 'PCI Policy Violation Resolved' exists.");
			for(EventInfo event: events){
				alarmsAndEvents.printEventInfo(event);
			}
			GeneralUtils.stopLevel();
		}
		
		if(shouldReboot){
			report.report("Wait 3 minutes to enodeb to reboot");
			GeneralUtils.unSafeSleep(3*60*1000);
			report.report("Wait for all running of enodeb");
			dut.waitForAllRunningAndInService(EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
			report.report("Enodeb reached all running and in service");	
		}else{
			report.report("eNB should not reboot. Wait up to 40 seconds to verify");
			dut.setExpectBooting(true);
			if(dut.waitForReboot(40*1000)){
				report.report("EnodeB was rebooted - not expected",Reporter.FAIL);
				dut.waitForAllRunningAndInService(EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
			}else{
				report.report("EnodeB was not rebooted - as expected");
			}
			dut.setExpectBooting(false);
		}
		
		report.reportHtml("cell show autoPCI list=1", dut.lteCli("cell show autoPCI list=1"), true);
		
		SONStatus newSONStatus = status.getSONStatus(dut);
		validateInNmsSonStatusPciTakenFromValidRange(newSONStatus,pciStart,pciEnd);
		
		GeneralUtils.startLevel("Delete All new 3rdParty");
		neighborsUtils.deleteAllNeighbors(dut);
		neighborsUtils.delete3rdPartyList(list3Party);
		GeneralUtils.stopLevel();
	}

	@Test
	@TestProperties(name = "Change_From_AutoPCI_To_Static_PCI_Value_Inside_The_Range", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void changeFromAutoPciToStaticPciValueInsideTheRange()  {
		changeEnodeBPciAndReboot();
		configureAutoPciToEnableViaNms(pciStart, pciEnd);

		report.report("eNB should not reboot. Wait up to 20 seconds to verify");
		dut.setExpectBooting(true);
		if(dut.waitForReboot(20*1000)){
			report.report("EnodeB was rebooted - not expected",Reporter.FAIL);
			dut.waitForAllRunningAndInService(EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
		}else{
			report.report("EnodeB was not rebooted - as expected");
		}
		dut.setExpectBooting(false);

		report.reportHtml("db get AutoPciCell", dut.lteCli("db get AutoPciCell"), true);
		SONStatus SONStatus = status.getSONStatus(dut);
		printSONStatus(SONStatus, "Automatic");
		validateInNmsSonStatusPciTakenFromValidRange(SONStatus, pciStart, pciEnd);

		reportAboutMibDB("cellAutoPciEnabled", "1");

		startTrafficAndCheckIfUEConnected();

		configureAutoPciToDisableViaNms();

		report.report("eNB should not reboot. Wait up to 20 seconds to verify");
		dut.setExpectBooting(true);
		if(dut.waitForReboot(20*1000)){
			report.report("EnodeB was rebooted - not expected",Reporter.FAIL);
			dut.waitForAllRunningAndInService(EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
		}else{
			report.report("No reboot issue (because free PCI of range is = static PCI)");
		}
		
		dut.setExpectBooting(false);
		enodeBConfig.printEnodebState(dut, true);

		reportAboutMibDB("cellAutoPciEnabled", "0");

		report.reportHtml("db get AutoPciCell", dut.lteCli("db get AutoPciCell"), true);
		SONStatus newSONStatus = status.getSONStatus(dut);
		printSONStatus(newSONStatus, "Manual");
		if (pciStart != getPropertiesStaticPCI(dut)) {
			report.report(dut.getNetspanName() + " didn't have same static PCI number (WAS: " + pciStart + ", NOW: "
					+ getPropertiesStaticPCI(dut) + ")", Reporter.FAIL);
			reason = "Enb didn't have same static PCI number";
		} else {
			report.report(dut.getNetspanName() + " still with same static PCI number: " + pciStart);
		}
		
		GeneralUtils.startLevel("Check if UE is connected");
		reason += peripheralsConfig.checkUesConnection(dut, testUeList, Reporter.WARNING);
		GeneralUtils.stopLevel();
	}

	@Test
	@TestProperties(name = "Change_From_Static_To_AutoPCI_Value_Outside_The_Range", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void changeFromStaticToAutoPciValueOutsideTheRange() {
		startTrafficAndCheckIfUEConnected();

		dut.setExpectBooting(true);
		configureAutoPciToEnableViaNms(pciStart, pciEnd);

		if(shouldReboot){
			report.report("Configuring Auto PCI cause with outside range, the eNB make reboot");

			report.report("Wait 1 minute");
			GeneralUtils.unSafeSleep(1000 * 60);

			report.report(dut.getNetspanName() + " Wait for all running and in service (TimeOut=15 Minutes)");
			dut.waitForAllRunningAndInService(EnodeB.WAIT_FOR_ALL_RUNNING_TIME);	
		}else{
			report.report("eNB should not reboot. Wait up to 40 seconds to verify");
			dut.setExpectBooting(true);
			if(dut.waitForReboot(40*1000)){
				report.report("EnodeB was rebooted - not expected",Reporter.FAIL);
				dut.waitForAllRunningAndInService(EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
			}else{
				report.report("EnodeB was not rebooted - as expected");
			}
			dut.setExpectBooting(false);
		}

		enodeBConfig.printEnodebState(dut, true);

		report.reportHtml("db get AutoPciCell", dut.lteCli("db get AutoPciCell"), true);
		SONStatus newSONStatus = status.getSONStatus(dut);
		printSONStatus(newSONStatus, "Automatic");
		validateInNmsSonStatusPciTakenFromValidRange(newSONStatus, pciStart, pciEnd);
	}
}
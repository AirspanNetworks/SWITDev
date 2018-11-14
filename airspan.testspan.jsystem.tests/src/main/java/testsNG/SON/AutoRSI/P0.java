package testsNG.SON.AutoRSI;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import DMTool.DMtool;
import DMTool.DMtool.Sib2;
import Netspan.API.Lte.EventInfo;
import Netspan.API.Lte.SONStatus;
import Netspan.Profiles.SonParameters;
import Utils.GeneralUtils;
import Utils.Pair;
import Utils.Triple;
import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;

public class P0 extends AutoRSIBase {

    @Test // 1
    @TestProperties(name = "Increasing_Order_Of_RSIs", returnParam = {
            "IsTestWasSuccessful"}, paramsExclude = {"IsTestWasSuccessful"})
    public void nmsShallValidateTheRsiListConfigurationAccordingToIncreasingOrderOfRSIs() {
        printRSISONStatus(netspan.getSONStatus(dut), "Manual", null, Reporter.FAIL);

        if (!startTrafficAndConnectUes()) {
            report.report("Failed connecting UE, failing test!", Reporter.FAIL);
            return;
        }

        List<Triple<Integer, Integer, Integer>> rangesList = new ArrayList<Triple<Integer, Integer, Integer>>();
        rangesList.add(Triple.createTriple(1, 10, 60));
        rangesList.add(Triple.createTriple(1, 9, 52));
        rangesList.add(Triple.createTriple(1, 8, 50));
        rangesList.add(Triple.createTriple(1, 7, 49));
        rangesList.add(Triple.createTriple(1, 6, 48));
        rangesList.add(Triple.createTriple(1, 5, 47));
        rangesList.add(Triple.createTriple(1, 4, 46));
        rangesList.add(Triple.createTriple(1, 3, 45));
        rangesList.add(Triple.createTriple(1, 2, 44));
        rangesList.add(Triple.createTriple(1, 1, 40));

        if (configureAutoPciAndAutoRsiToEnableViaNms(null, rangesList))
            report.report("Succefully set RSI list.");
        else
            report.report("Failed to set RSI list.", Reporter.FAIL);

        report.report("Wait 2 Minutes");
        GeneralUtils.unSafeSleep(1000 * 60 * 2);

        printRSISONStatus(netspan.getSONStatus(dut), null, null, Reporter.FAIL);

        reportAboutFindAlarm("Auto RSI Config Invalid");

        checkConnectionUEWithStopStart();
        traffic.stopTraffic();
    }

    @Test // 2
    @TestProperties(name = "NMS_Shall_Present_'Auto_RSI_Selection'", returnParam = {
            "IsTestWasSuccessful"}, paramsExclude = {"IsTestWasSuccessful"})
    public void nmsShallPresentAutoRsiSelection() {
        printRSISONStatus(netspan.getSONStatus(dut), "Manual", null, Reporter.FAIL);
        printPCISONStatus(netspan.getSONStatus(dut), "Manual", null);

        if (!startTrafficAndConnectUes()) {
            report.report("Failed connecting UE, failing test!", Reporter.FAIL);
            return;
        }

        List<Triple<Integer, Integer, Integer>> rangesList = new ArrayList<Triple<Integer, Integer, Integer>>();
        rangesList.add(Triple.createTriple(0, 10, 60));
        configureAutoPciAndAutoRsiToEnableViaNms(null, rangesList);

        report.report("Wait 40 seconds");
        GeneralUtils.unSafeSleep(1000 * 40);

        printRSISONStatus(netspan.getSONStatus(dut), "Automatic", null, Reporter.FAIL);

        checkConnectionUEWithStopStart();
        traffic.stopTraffic();
    }

    @Test // 3
    @TestProperties(name = "Manually_Configured_RSI_Mode_From_NMS", returnParam = {
            "IsTestWasSuccessful"}, paramsExclude = {"IsTestWasSuccessful"})
    public void manuallyConfiguredRsiModeFromNMS() {
        int intialRsi = initRsiList.get(0);

        printRSISONStatus(netspan.getSONStatus(dut), "Manual", null, Reporter.FAIL);
        printPCISONStatus(netspan.getSONStatus(dut), "Manual", null);

        if (!startTrafficAndConnectUes()) {
            report.report("Failed connecting UE, failing test!", Reporter.FAIL);
            return;
        }

        DMtool dmTool = new DMtool();
        dmTool.setUeIP(testUE.getLanIpAddress());
        try {
            dmTool.init();
        } catch (Exception e) {
            report.report("Failed to init DMTool", Reporter.WARNING);
            e.printStackTrace();
        }

        Sib2 sib2 = dmTool.getSib2();
        if (sib2 != null)
            report.report("DMTool: root Sequence Index = " + sib2.rootSequenceIndex);
        else
            report.report("Failed getting Sib2 from DMtool", Reporter.WARNING);
        printRSISONStatus(netspan.getSONStatus(dut), "Manual", null, Reporter.FAIL);

        checkConnectionUEWithStopStart();

        GeneralUtils.startLevel("Changing RSI and validating");
        Date fromDate = new Date();
        int newRsiPrachSequenceIndex = intialRsi + 1;
        enodeBConfig.changeEnbCellRSI(dut, newRsiPrachSequenceIndex);

        report.report("Wait 10 seconds");
        GeneralUtils.unSafeSleep(1000 * 10);

        sib2 = dmTool.getSib2();
        if (sib2 != null)
            report.report("DMTool: root Sequence Index = " + sib2.rootSequenceIndex);
        else
            report.report("Failed getting Sib2 from DMtool", Reporter.WARNING);

        printRSISONStatus(netspan.getSONStatus(dut), "Manual", newRsiPrachSequenceIndex, Reporter.FAIL);

        boolean ueConnected = peripheralsConfig.checkIfAllUEsAreConnectedToNode(testUEs, dut);
        if (!ueConnected) {
            report.report("UE disconnected from enodeB.", Reporter.FAIL);
        }

        Date toDate = new Date();

        report.report("Getting events Ranged from " + dateFormat.format(fromDate) + " to " + dateFormat.format(toDate));
        List<EventInfo> eventsNodeByRange = alarmsAndEvents.getEventsNodeByDateRange(dut, fromDate, toDate);

        alarmsAndEvents.isExistSpecificEventType(eventsNodeByRange, "Auto RSI Selected Value", Reporter.FAIL);

        GeneralUtils.stopLevel();

        traffic.stopTraffic();
    }

    @Test // 4
    @TestProperties(name = "Auto_RSI_Enabled_Auto_PCI_Disabled", returnParam = {
            "IsTestWasSuccessful"}, paramsExclude = {"IsTestWasSuccessful"})
    public void autoRsiEnabledAutoPciDisabled() {
        SonParameters sonParams = new SonParameters();
        sonParams.setAutoPCIEnabled(false);
        enodeBConfig.cloneAndSetProfileViaNetSpan(dut, netspan.getCurrentSonProfileName(dut), sonParams);

        printRSISONStatus(netspan.getSONStatus(dut), "Manual", null, Reporter.FAIL);
        printPCISONStatus(netspan.getSONStatus(dut), "Manual", null);

        if (!startTrafficAndConnectUes()) {
            report.report("Failed connecting UE, failing test!", Reporter.FAIL);
            return;
        }

        List<Triple<Integer, Integer, Integer>> rangesList = new ArrayList<Triple<Integer, Integer, Integer>>();
        rangesList.add(Triple.createTriple(1, 1, 10));
        configureAutoPciAndAutoRsiToEnableViaNms(null, rangesList);

        report.report("Wait 10 seconds");
        GeneralUtils.unSafeSleep(1000 * 10);

        printRSISONStatus(netspan.getSONStatus(dut), "Automatic", null, Reporter.FAIL);
        printPCISONStatus(netspan.getSONStatus(dut), "Manual", null);

        report.report("Wait 30 seconds");
        GeneralUtils.unSafeSleep(1000 * 30);

//        reportAboutFindAlarm("Auto RSI Config Invalid");

        checkConnectionUEWithStopStart();
        traffic.stopTraffic();
    }

    @Test // 5
    @TestProperties(name = "PCI and RSI enabled RSI list longer or equal pci list", returnParam = {
            "IsTestWasSuccessful"}, paramsExclude = {"IsTestWasSuccessful"})
    public void autoPCIandRSIenabledAndRsiListIsEqualOrLargerThanSizeOfPciListTheENB() {
        SONStatus sonStatus = netspan.getSONStatus(dut);
        int initialPci = getInitialPci(sonStatus);

        printRSISONStatus(netspan.getSONStatus(dut), "Manual", null, Reporter.FAIL);
        printPCISONStatus(netspan.getSONStatus(dut), "Manual", null);

        if (!startTrafficAndConnectUes()) {
            report.report("Failed connecting UE, failing test!", Reporter.FAIL);
            return;
        }

        List<Pair<Integer, Integer>> rangesListPCI = new ArrayList<Pair<Integer, Integer>>();
        rangesListPCI.add(Pair.createPair(1, 503));
        configureAutoPciAndAutoRsiToEnableViaNms(rangesListPCI, null);

        report.report("Wait 10 seconds");
        GeneralUtils.unSafeSleep(1000 * 10);

        printPCISONStatus(netspan.getSONStatus(dut), "Automatic", initialPci);

        Date fromDate = new Date();

        List<Triple<Integer, Integer, Integer>> rangesListRSI = new ArrayList<Triple<Integer, Integer, Integer>>();
        rangesListRSI.add(Triple.createTriple(1, 1, 600));
        configureAutoPciAndAutoRsiToEnableViaNms(null, rangesListRSI);

        report.report("Wait 10 seconds");
        GeneralUtils.unSafeSleep(1000 * 10);

        printPCISONStatus(netspan.getSONStatus(dut), "Automatic", initialPci);
        printRSISONStatus(netspan.getSONStatus(dut), "Automatic", null, Reporter.FAIL);

        report.report("Wait 10 seconds");
        GeneralUtils.unSafeSleep(1000 * 10);

        Date toDate = new Date();

        GeneralUtils.startLevel("Events Ranged " + dateFormat.format(fromDate) + " - " + dateFormat.format(toDate));
        List<EventInfo> eventsNodeByRange = alarmsAndEvents.getEventsNodeByDateRange(dut, fromDate, toDate);
        alarmsAndEvents.isExistSpecificEventType(eventsNodeByRange, "Auto RSI Change", Reporter.WARNING);
        alarmsAndEvents.isExistSpecificEventType(eventsNodeByRange, "Auto RSI Selected Value", Reporter.FAIL);
        alarmsAndEvents.printEventsList(eventsNodeByRange);
        GeneralUtils.stopLevel();

        checkConnectionUEWithStopStart();
        traffic.stopTraffic();
    }

    @Test // 6
    @TestProperties(name = "PCI and RSI_enabled_RSI_list_smaller_than_PCI_list", returnParam = {
            "IsTestWasSuccessful"}, paramsExclude = {"IsTestWasSuccessful"})
    public void autoPCIandRSIenabledAndSizeOfRsiListIsSmallerThanSizeOfPciList() {
        SONStatus sonStatus = netspan.getSONStatus(dut);

        int initialPci = getInitialPci(sonStatus);

        printRSISONStatus(netspan.getSONStatus(dut), "Manual", null, Reporter.FAIL);
        printPCISONStatus(netspan.getSONStatus(dut), "Manual", null);

        if (!startTrafficAndConnectUes()) {
            report.report("Failed connecting UE, failing test!", Reporter.FAIL);
            return;
        }

        List<Pair<Integer, Integer>> rangesListPCI = new ArrayList<Pair<Integer, Integer>>();
        rangesListPCI.add(Pair.createPair(1, 503));
        configureAutoPciAndAutoRsiToEnableViaNms(rangesListPCI, null);

        report.report("Wait 10 seconds");
        GeneralUtils.unSafeSleep(1000 * 10);

        printPCISONStatus(netspan.getSONStatus(dut), "Automatic", initialPci);

        List<Triple<Integer, Integer, Integer>> rangesListRSI = new ArrayList<Triple<Integer, Integer, Integer>>();
        rangesListRSI.add(Triple.createTriple(1, 1, 200));
        configureAutoPciAndAutoRsiToEnableViaNms(null, rangesListRSI);

        printPCISONStatus(netspan.getSONStatus(dut), "Automatic", initialPci);
        printRSISONStatus(netspan.getSONStatus(dut), "Automatic", null, Reporter.FAIL);

        report.report("Wait 10 seconds");
        GeneralUtils.unSafeSleep(1000 * 10);

        alarmsAndEvents.isExistSpecificAlarmType(alarmsAndEvents.getAllAlarmsNode(dut), "Auto RSI Config Invalid");
        checkConnectionUEWithStopStart();
        traffic.stopTraffic();
    }

    @Test // 7
    @TestProperties(name = "NMS_configuring_auto_RSI_mode_from_SON_profile", returnParam = {
            "IsTestWasSuccessful"}, paramsExclude = {"IsTestWasSuccessful"})
    public void nmsShallAllowConfiguringAutoRsiModeEnableOrDisableFromSonProfile() {
        SONStatus sonStatus = netspan.getSONStatus(dut);

        int initialPci = getInitialPci(sonStatus);

        printRSISONStatus(netspan.getSONStatus(dut), "Manual", null, Reporter.FAIL);
        printPCISONStatus(netspan.getSONStatus(dut), "Manual", null);

        if (!startTrafficAndConnectUes()) {
            report.report("Failed connecting UE, failing test!", Reporter.FAIL);
            return;
        }

        DMtool dmTool = new DMtool();
        dmTool.setUeIP(testUE.getLanIpAddress());
        try {
            dmTool.init();
        } catch (Exception e) {
            report.report("Failed to init DMTool", Reporter.WARNING);
            e.printStackTrace();
        }

        GeneralUtils.startLevel("Set and validate PRACH Root sequence index");
        enodeBConfig.changeEnbCellRSI(dut, initialPci + 2);
        Sib2 sib2 = dmTool.getSib2();
        if (sib2 != null) {
            report.report("DMTool: root Sequence Index = " + sib2.rootSequenceIndex);
            report.report("DMTool: prach Config Index = " + sib2.prachConfigIndex);
        } else
            report.report("Failed getting Sib2 from DMtool", Reporter.WARNING);

        printRSISONStatus(netspan.getSONStatus(dut), "Manual", null, Reporter.FAIL);

        boolean ueConnected = peripheralsConfig.checkIfAllUEsAreConnectedToNode(testUEs, dut);
        if (!ueConnected) {
            report.report("UE disconnected from enodeB.", Reporter.FAIL);
        }

        GeneralUtils.stopLevel();

        report.report("Wait 10 seconds");
        GeneralUtils.unSafeSleep(1000 * 10);

        GeneralUtils.startLevel("Set autoRSI with blank list and validate.");
        List<Triple<Integer, Integer, Integer>> rangesListRSI = new ArrayList<Triple<Integer, Integer, Integer>>();
        configureAutoPciAndAutoRsiToEnableViaNms(null, rangesListRSI);
        try {
            sib2 = dmTool.getSib2();
        } catch (Exception e) {
            //Ignore exception, the code checks again if sib2 is null anyway, and export report.report if it is.
        }
        if (sib2 != null) {
            report.report("DMTool: root Sequence Index = " + sib2.rootSequenceIndex);
            report.report("DMTool: prach Config Index = " + sib2.prachConfigIndex);
        } else
            report.report("Failed getting Sib2 from DMtool", Reporter.WARNING);

        printRSISONStatus(netspan.getSONStatus(dut), "Automatic", null, Reporter.FAIL);
        GeneralUtils.stopLevel();


        report.report("Wait 10 seconds");
        GeneralUtils.unSafeSleep(1000 * 10);

        GeneralUtils.startLevel("Disable AutoRSI in SON profile.");
        configureAutoRSIToDisableViaNms();

        report.report("Wait 10 seconds");
        GeneralUtils.unSafeSleep(1000 * 10);

        printRSISONStatus(netspan.getSONStatus(dut), "Manual", null, Reporter.FAIL);

        GeneralUtils.stopLevel();
        int numFails = 0;
        GeneralUtils.startLevel("Come back to enable/disable 20 times(the AutoRSI is enable the system choose RSI from list, if disable back to Static RSI)");
        for (int i = 1; i <= 20; i++) {
            boolean currentFailed = true;
            GeneralUtils.startLevel("Step " + String.valueOf(i));
            currentFailed &= configureAutoPciAndAutoRsiToEnableViaNms(null, rangesListRSI);
            report.report("Wait 10 seconds");
            GeneralUtils.unSafeSleep(1000 * 10);
            currentFailed &= printRSISONStatus(netspan.getSONStatus(dut), "Automatic", null, Reporter.WARNING);
            currentFailed &= configureAutoRSIToDisableViaNms();
            report.report("Wait 10 seconds");
            GeneralUtils.unSafeSleep(1000 * 10);
            currentFailed &= printRSISONStatus(netspan.getSONStatus(dut), "Manual", null, Reporter.WARNING);
            GeneralUtils.stopLevel();
            if (!currentFailed) {
                numFails++;
            }
        }
        if (numFails > 1) {
            report.report("More than 1 sample failed", Reporter.FAIL);
            reason = "More than 1 sample failed";
        }
        GeneralUtils.stopLevel();
        traffic.stopTraffic();
    }
}
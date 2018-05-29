package testsNG.SON.PnP;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.xml.datatype.XMLGregorianCalendar;

import org.joda.time.Duration;
import org.joda.time.Interval;
import org.junit.Test;
import EnodeB.EnodeB;
import EnodeB.EnodeBWithDonor;
import EnodeB.Ninja;
import EnodeB.Components.EnodeBComponent;
import Netspan.API.Lte.EventInfo;
import UE.UE;
import Utils.DNS;
import Utils.GeneralUtils;
import Utils.GeneralUtils.HtmlFieldColor;
import Utils.GeneralUtils.HtmlTable;
import Utils.GeneralUtils.RebootType;
import Utils.Pair;
import Utils.SetupUtils;
import Utils.SysObjUtils;
import Utils.Triple;
import Utils.TunnelManager;
import Utils.WatchDog.WatchDogManager;
import jsystem.framework.ParameterProperties;
import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;
import testsNG.TestspanTest;
import testsNG.Actions.PeripheralsConfig;
import testsNG.Actions.SoftwareUtiles;
import testsNG.Actions.Traffic;
import testsNG.Actions.Utils.ParallelCommandsThread;
import testsNG.Actions.Utils.TrafficGeneratorType;

public class Progression extends TestspanTest{
	private final Triple[] relayColdRebootPnpEventListToFollow = {				//EventType=LeftElement, SourceType=MiddleElement, EventInfo.contains(RightElement) 
			new Triple<String, String, String>("PnP Identification", "iRelay", "Reboot=cold"),
			new Triple<String, String, String>("PnP State Change", "iRelay", "PnP State=Discover"),
			new Triple<String, String, String>("PnP State Change", "iRelay", "PnP State=Configure"),
			new Triple<String, String, String>("Node Reprovision", "iRelay", "Type=Automatic,Reason=Plug and Play"),
			new Triple<String, String, String>("Node Connection State Change", "iRelay", "ConnectionState=Online"),
			new Triple<String, String, String>("PnP State Change", "iRelay", "PnP State=Completed"), 
	};
	private final Triple[] relayScanListEventListToFollow = {
			/*new Triple<String, String, String>("Scan Status", "iRelay", "Status=stop,Info=Scan started"),
			new Triple<String, String, String>("Scan Status", "iRelay", "Status=stop,Info=Scan completed"),*/
			new Triple<String, String, String>("Node Connection State Change", "iRelay", "ConnectionState=NoComms"),
	};
	private final Triple[] relayWarmRebootPnpEventListToFollow = {
			new Triple<String, String, String>("PnP Identification", "iRelay", "Reboot=warm"),
			new Triple<String, String, String>("PnP State Change", "iRelay", "PnP State=Discover"),
			new Triple<String, String, String>("PnP State Change", "iRelay", "PnP State=Completed"),
			new Triple<String, String, String>("Node Connection State Change", "iRelay", "ConnectionState=Online"),
	};
	private final Triple[] eNodebColdRebootPnpSoftwareDownloadEventListToFollow = {
			new Triple<String, String, String>("PnP Identification", "eNodeB", "Reboot=cold"),
			new Triple<String, String, String>("PnP State Change", "eNodeB", "PnP State=Discover"),
			new Triple<String, String, String>("PnP State Change", "eNodeB", "PnP State=Software"),
			new Triple<String, String, String>("Software Download", "eNodeB", "Download completed"),
			new Triple<String, String, String>("Software Activate", "eNodeB", "Activate in progress")
	};
	private final Triple[] eNodebColdRebootPnpEventListToFollow = {
			new Triple<String, String, String>("PnP Identification", "eNodeB", "Reboot=cold"),
			new Triple<String, String, String>("PnP State Change", "eNodeB", "PnP State=Discover"),
			new Triple<String, String, String>("PnP State Change", "eNodeB", "PnP State=Software"),
			new Triple<String, String, String>("PnP State Change", "eNodeB", "PnP State=Configure"),
			new Triple<String, String, String>("Node Reprovision", "eNodeB", "Type=Automatic,Reason=Plug and Play"),
			new Triple<String, String, String>("Node Connection State Change", "eNodeB", "ConnectionState=Online"),
			new Triple<String, String, String>("PnP State Change", "eNodeB", "PnP State=Completed")
	};
	private final Triple[] eNodebSoftwareActivateCompletedEventListToFollow = {
			new Triple<String, String, String>("Software Activate", "eNodeB", "Activate completed")
	};
	private final long COLLECT_EVENTS_FROM_NMS_TIMEOUT = 90 * 1000;
	private final int WAIT_FOR_PTR_REQUEST_TIMEOUT = 10 * 60;
	private final long WAIT_FOR_RELAY_CONNECT_DONOR = 4 * 60 * 1000;
	private final long WAIT_FOR_RELAY_DISCONNECT_DONOR = 1 * 60 * 1000;
	private final long WAIT_UNTIL_SNMP_AVAIALBLILITY = 5 * 60 * 1000;
	private long WAIT_FOR_ALL_RUNNING_TIME = 10 * 60 * 1000;
	private final long WAIT_FOR_DEBUG_PORT_TIMEOUT = 1 * 60 * 1000;
	private final long FIRST_RELAY_ATTACH_EXPECTED_DURATION_IN_MILI = ((3 * 60) + 50) * 1000;
	private final long FIRST_DNS_QUERY_EXPECTED_DURATION_IN_MILI = ((1 * 60) + 20) * 1000;
	private final long FIRST_IPSEC_BRING_UP_EXPECTED_DURATION_IN_MILI = ((1 * 60) + 40) * 1000;
	private final long COLD_RELAY_PNP_EVENTS_EXPECTED_DURATION_IN_MILI = 20 * 1000;
	private final long SCAN_LIST_EVENTS_EXPECTED_DURATION_IN_MILI = ((1 * 60) + 10) * 1000;
	private final long SECOND_RELAY_ATTACH_EXPECTED_DURATION_IN_MILI = ((1 * 60) + 10) * 1000;
	private final long SECOND_DNS_QUERY_EXPECTED_DURATION_IN_MILI = 35 * 1000;
	private long SECOND_IPSEC_BRING_UP_EXPECTED_DURATION_IN_MILI = 20 * 1000;
	private final long WARM_RELAY_PNP_EVENTS_EXPECTED_DURATION_IN_MILI = 20 * 1000;
	private long COLD_ENODEB_PNP_EVENTS_EXPECTED_DURATION_IN_MILI = 30 * 1000;
	private long WAIT_FOR_ALL_RUNNING_EXPECTED_DURATION_IN_MILI = 50 * 1000;
	
	private EnodeB dut;
	private ParallelCommandsThread syncCommands;
	private boolean isEnodeBWithDonor;
	private boolean isNinja;
	private HtmlTable htmlTable;
	private HtmlTimelineStage[] htmlTimelineTable;
	private Pair<Long, String>[] expectedDurationsAndStageNamesOrdered;
	
	@Override
	public void init() throws Exception {
		enbInTest = new ArrayList<EnodeB>();
		enbInTest.add(dut);
		super.init();
		if(dut instanceof EnodeBWithDonor){
			isEnodeBWithDonor = true;
			if(dut instanceof Ninja){
				isNinja = true;
			}
		}
		htmlTable = new HtmlTable();
		
		//startParallelCommands(dut);
	}

	@Override
	public void end(){
		 WatchDogManager.getInstance().shutDown();
		 if(isEnodeBWithDonor){
			 DNS.getInstance().closeConnection();
		 }
		//stopParallelCommands();
		super.end();
	}
	
	@ParameterProperties(description = "DUT")
	public void setDut(String dut) {
		GeneralUtils.printToConsole("Load DUT " + dut);
		this.dut = (EnodeB) SysObjUtils.getInstnce().initSystemObject(EnodeB.class, false, dut).get(0);
		GeneralUtils.printToConsole("DUT loaded" + this.dut.getName());
	}
	
	/***************** Tests ********************/
	
	@Test
	@TestProperties(name = "Test PnP after Cold Reboot",
	returnParam = { "IsTestWasSuccessful"},
	paramsExclude = {"IsTestWasSuccessful"})
	public void pnpColdReboot() {
		this.expectedDurationsAndStageNamesOrdered = dut.getExpectedDurationsAndStageNamesOrderedForColdReboot();
		int step = 1;
		int timelineStageIndex = 0;
		htmlTimelineTable = new HtmlTimelineStage[40];
		WatchAllRunningTimeout watchAllRunningTimeout = new WatchAllRunningTimeout(dut);
		WaitForPtrRequest waitForFirstPtrRequest = null, waitForSecondPtrRequest = null;
		WatchNmsEvents watchNmsEventsRelayColdReboot = null, watchNmsEventsRelayScanList = null, watchNmsEventsRelayWarmReboot = null, watchNmsEventsEnodebColdReboot = null;
		WaitForPtrRequest waitForFirstPtrRequestfterSoftwareDownload = null, waitForSecondPtrRequestfterSoftwareDownload = null;
		WatchNmsEvents watchNmsEventsEnodebColdRebootSoftwareDownload = null, watchNmsEventsRelayColdRebootAfterSoftwareDownload = null, watchNmsEventsRelayScanListAfterSoftwareDownload = null, watchNmsEventsRelayWarmRebootAfterSoftwareDownload = null, watchNmsEventsEnodebSoftwareActivate = null;
		if(isEnodeBWithDonor){
			EnodeBWithDonor eNodeBWithDonorDut = (EnodeBWithDonor)dut;
			EnodeB donor = eNodeBWithDonorDut.getDonor();
			if (donor.isInOperationalStatus()){
				report.report("Donor is in Running State.");
			}else{
				report.report("Donor is NOT in Running State.", Reporter.FAIL);
				reason = "Donor is NOT in Running State.";
				return;
			}
			String relayVersion = eNodeBWithDonorDut.getRelayRunningVersion();
			if(relayVersion != ""){
				report.report("Relay's Running Version: " + relayVersion);
			}
		}
		report.report(dut.getName() + "'s Running Version: " + dut.getRunningVersion());
		suspendIpsecTunnelManagerIfEnabled(dut); 
		//openLogs(dut);
		Pair<Long, Triple<Integer, String, String>> rebootTimeAndSwActivationDetails = performColdRebootAndConvertNmsProfileToPnP(step++, timelineStageIndex++, dut, watchAllRunningTimeout);
		final long rebootTime = rebootTimeAndSwActivationDetails.getElement0();
		if(rebootTime == 0){
			return;
		}
		if(isEnodeBWithDonor){
			EnodeBWithDonor eNodeBWithDonor = (EnodeBWithDonor)dut;
			waitForDebugPortAvailability((EnodeBWithDonor)eNodeBWithDonor);
			startToCollectPacketsSentByRelayToDnsAndWaitForRelayToConnectToDonor(step++, timelineStageIndex++, rebootTime, eNodeBWithDonor, "First Relay Attach.", FIRST_RELAY_ATTACH_EXPECTED_DURATION_IN_MILI);
			GeneralUtils.boldReportLine(step++ + ". For First DNS Query see Appendix A.");
			waitForFirstPtrRequest = waitForPtrRequest(timelineStageIndex++, "First DNS Query.", eNodeBWithDonor, rebootTime, FIRST_DNS_QUERY_EXPECTED_DURATION_IN_MILI);//Doesn't Wait, uses WatchDog
			waitForFirstIPSecBringUp(step++, timelineStageIndex++, rebootTime, (EnodeBWithDonor)eNodeBWithDonor, FIRST_IPSEC_BRING_UP_EXPECTED_DURATION_IN_MILI);
			GeneralUtils.boldReportLine(step++ + ". For Cold Relay PnP see Appendix B.");
			watchNmsEventsRelayColdReboot = startFollowNmsEvents(timelineStageIndex++, COLD_RELAY_PNP_EVENTS_EXPECTED_DURATION_IN_MILI, eNodeBWithDonor, rebootTime, COLLECT_EVENTS_FROM_NMS_TIMEOUT, "Cold Relay PnP.", relayColdRebootPnpEventListToFollow, 0, false);//Doesn't Wait, uses WatchDog
			waitForRelayDisconnectDonor(step++, eNodeBWithDonor);
			GeneralUtils.boldReportLine(step++ + ". For Build Scan List see Appendix C.");
			watchNmsEventsRelayScanList = startFollowNmsEvents(GeneralUtils.ERROR_VALUE, SCAN_LIST_EVENTS_EXPECTED_DURATION_IN_MILI, eNodeBWithDonor, rebootTime, COLLECT_EVENTS_FROM_NMS_TIMEOUT, "Build Scan List.", relayScanListEventListToFollow, 0, false);//Doesn't Wait, uses WatchDog
			startToCollectPacketsSentByRelayToDnsAndWaitForRelayToConnectToDonor(step++, timelineStageIndex++, rebootTime, eNodeBWithDonor, "Second Relay Attach.", SECOND_RELAY_ATTACH_EXPECTED_DURATION_IN_MILI);//Doesn't Wait, uses WatchDog
			GeneralUtils.boldReportLine(step++ + ". For Second DNS Query see Appendix D.");
			waitForSecondPtrRequest = waitForPtrRequest(timelineStageIndex++, "Second DNS Query.", eNodeBWithDonor, rebootTime, SECOND_DNS_QUERY_EXPECTED_DURATION_IN_MILI);//Doesn't Wait, uses WatchDog
		}
		waitForSnmpAvailability(step++, timelineStageIndex++, rebootTime, dut, SECOND_IPSEC_BRING_UP_EXPECTED_DURATION_IN_MILI, RebootType.COLD_REBOOT);
		if(isEnodeBWithDonor){
			GeneralUtils.boldReportLine(step++ + ". For Warm Relay PnP see Appendix E.");
			watchNmsEventsRelayWarmReboot = startFollowNmsEvents(timelineStageIndex++, WARM_RELAY_PNP_EVENTS_EXPECTED_DURATION_IN_MILI, dut, rebootTime, COLLECT_EVENTS_FROM_NMS_TIMEOUT, "Warm Relay PnP.", relayWarmRebootPnpEventListToFollow, 0, false);//Doesn't Wait, uses WatchDog
			GeneralUtils.unSafeSleep(6000);//In purpose that Warm Relay PnP stage will finish before Cold eNodeB PnP for the timeline.
		}else if(dut.isSwUpgradeDuringPnP() == false){
			//Waiting for NMS Events.
			GeneralUtils.unSafeSleep(5 * 60 * 1000);
		}
		long rebootTimeAfterSoftwareDownload = 0;
		if(dut.isSwUpgradeDuringPnP()){
			GeneralUtils.startLevel(step++ + ". For Cold eNodeB PnP & Software Download see Appendix E1.");
			suspendIpsecTunnelManagerIfEnabled(dut);
			ArrayList<Pair<EnodeB, Triple<Integer, String, String>>> dutInArray = new ArrayList<Pair<EnodeB, Triple<Integer, String, String>>>();
			dutInArray.add(new Pair<EnodeB, Triple<Integer, String, String>>(dut, rebootTimeAndSwActivationDetails.getElement1()));
			SoftwareUtiles.getInstance().followSoftwareActivationProgressViaNetspan(System.currentTimeMillis(), dutInArray);
			GeneralUtils.stopLevel();
			watchNmsEventsEnodebColdRebootSoftwareDownload = startFollowNmsEvents(timelineStageIndex++, COLD_ENODEB_PNP_EVENTS_EXPECTED_DURATION_IN_MILI, dut, rebootTime, COLLECT_EVENTS_FROM_NMS_TIMEOUT, "Cold eNodeB PnP & Software Download.", eNodebColdRebootPnpSoftwareDownloadEventListToFollow, 0, false);//Doesn't Wait, uses WatchDog
			rebootTimeAfterSoftwareDownload = System.currentTimeMillis();
			saveStageTimeForHtmlTable(timelineStageIndex++, "Reboot After Software Download.", rebootTime, rebootTimeAfterSoftwareDownload, 1, true);
			GeneralUtils.unSafeSleep(60 * 1000);
			if(isEnodeBWithDonor){
				EnodeBWithDonor eNodeBWithDonor = (EnodeBWithDonor)dut;
				waitForDebugPortAvailability(eNodeBWithDonor);
				eNodeBWithDonor.echoToSkipCmpv2();
				startToCollectPacketsSentByRelayToDnsAndWaitForRelayToConnectToDonor(step++, timelineStageIndex++, rebootTime, eNodeBWithDonor, "First Relay Attach After Software Download.", FIRST_RELAY_ATTACH_EXPECTED_DURATION_IN_MILI);
				GeneralUtils.boldReportLine(step++ + ". For First DNS Query After Software Download see Appendix E2.");
				waitForFirstPtrRequestfterSoftwareDownload = waitForPtrRequest(timelineStageIndex++, "First DNS Query After Software Download.", eNodeBWithDonor, rebootTime, FIRST_DNS_QUERY_EXPECTED_DURATION_IN_MILI);//Doesn't Wait, uses WatchDog
				waitForFirstIPSecBringUp(step++, timelineStageIndex++, rebootTime, eNodeBWithDonor, FIRST_IPSEC_BRING_UP_EXPECTED_DURATION_IN_MILI);
				GeneralUtils.boldReportLine(step++ + ". For Cold Relay PnP After Software Download see Appendix E3.");
				watchNmsEventsRelayColdRebootAfterSoftwareDownload = startFollowNmsEvents(timelineStageIndex++, COLD_RELAY_PNP_EVENTS_EXPECTED_DURATION_IN_MILI, eNodeBWithDonor, rebootTime, COLLECT_EVENTS_FROM_NMS_TIMEOUT, "Cold Relay PnP After Software Download.", relayColdRebootPnpEventListToFollow, rebootTimeAfterSoftwareDownload, true);//Doesn't Wait, uses WatchDog
				waitForRelayDisconnectDonor(step++, eNodeBWithDonor);
				GeneralUtils.boldReportLine(step++ + ". For Build Scan List After Software Download see Appendix E4.");
				watchNmsEventsRelayScanListAfterSoftwareDownload = startFollowNmsEvents(GeneralUtils.ERROR_VALUE, SCAN_LIST_EVENTS_EXPECTED_DURATION_IN_MILI, eNodeBWithDonor, rebootTime, COLLECT_EVENTS_FROM_NMS_TIMEOUT, "Build Scan List After Software Download.", relayScanListEventListToFollow, rebootTimeAfterSoftwareDownload, true);//Doesn't Wait, uses WatchDog
				startToCollectPacketsSentByRelayToDnsAndWaitForRelayToConnectToDonor(step++, timelineStageIndex++, rebootTime, eNodeBWithDonor, "Second Relay Attach After Software Download.", SECOND_RELAY_ATTACH_EXPECTED_DURATION_IN_MILI);//Doesn't Wait, uses WatchDog
				GeneralUtils.boldReportLine(step++ + ". For Second DNS Query After Software Download see Appendix E5.");
				waitForSecondPtrRequestfterSoftwareDownload = waitForPtrRequest(timelineStageIndex++, "Second DNS Query After Software Download.", eNodeBWithDonor, rebootTime, SECOND_DNS_QUERY_EXPECTED_DURATION_IN_MILI);//Doesn't Wait, uses WatchDog
			}else if(dut.isIpsecTunnelEnabled()){
				GeneralUtils.unSafeSleep(30 * 1000);
				dut.echoToSkipCmpv2();
			}
			waitForSnmpAvailability(step++, timelineStageIndex++, rebootTime, dut, SECOND_IPSEC_BRING_UP_EXPECTED_DURATION_IN_MILI, RebootType.COLD_REBOOT);
			if(isEnodeBWithDonor){
				GeneralUtils.boldReportLine(step++ + ". For Warm Relay PnP After Software Download see Appendix E6.");
				watchNmsEventsRelayWarmRebootAfterSoftwareDownload = startFollowNmsEvents(timelineStageIndex++, WARM_RELAY_PNP_EVENTS_EXPECTED_DURATION_IN_MILI, dut, rebootTime, COLLECT_EVENTS_FROM_NMS_TIMEOUT, "Warm Relay PnP After Software Download.", relayWarmRebootPnpEventListToFollow, rebootTimeAfterSoftwareDownload, true);//Doesn't Wait, uses WatchDog
				GeneralUtils.unSafeSleep(6000);//In purpose that Warm Relay PnP stage will finish before Cold eNodeB PnP for the timeline.
			}else{
				//Waiting for NMS Events.
				GeneralUtils.unSafeSleep(5 * 60 * 1000);
			}
		}
		GeneralUtils.boldReportLine(step++ + ". For Cold eNodeB PnP see Appendix F.");
		watchNmsEventsEnodebColdReboot = startFollowNmsEvents(timelineStageIndex++, COLD_ENODEB_PNP_EVENTS_EXPECTED_DURATION_IN_MILI, dut, rebootTime, COLLECT_EVENTS_FROM_NMS_TIMEOUT, "Cold eNodeB PnP.", eNodebColdRebootPnpEventListToFollow, rebootTimeAfterSoftwareDownload, dut.isSwUpgradeDuringPnP());//Doesn't Wait, uses WatchDog
		if(dut.isSwUpgradeDuringPnP()){
			GeneralUtils.boldReportLine(step++ + ". For eNodeb Software Activate Completed see Appendix F1.");
			watchNmsEventsEnodebSoftwareActivate = startFollowNmsEvents(timelineStageIndex++, COLD_ENODEB_PNP_EVENTS_EXPECTED_DURATION_IN_MILI, dut, rebootTime, COLLECT_EVENTS_FROM_NMS_TIMEOUT, "eNodeb Software Activate Completed.", eNodebSoftwareActivateCompletedEventListToFollow, rebootTimeAfterSoftwareDownload, true);//Doesn't Wait, uses WatchDog
		}
		if(isEnodeBWithDonor){
			checkIfClockSyncLock(step++, ((EnodeBWithDonor)dut));
		}
		watchAllRunningTimeout.run();
		waitForAllRunning(step++, timelineStageIndex++, rebootTime, dut, WAIT_FOR_ALL_RUNNING_EXPECTED_DURATION_IN_MILI);
		watchAllRunningTimeout.stopCounting();
		watchAllRunningTimeout.printStatus();
		if(dut.isSwUpgradeDuringPnP()){
			SoftwareUtiles softwareUtiles = SoftwareUtiles.getInstance();
			softwareUtiles.isUpdatedViaSnmp(dut, Reporter.FAIL);
			softwareUtiles.isRelayVersionUpdated(dut, rebootTimeAndSwActivationDetails.getElement1().getRightElement(), Reporter.FAIL);
		}
		checkIfUEsAreConnectedToEnodeB(step++, dut);
		waitForAllProcessesToEnd(waitForFirstPtrRequest, watchNmsEventsRelayColdReboot, watchNmsEventsRelayScanList, waitForSecondPtrRequest, watchNmsEventsRelayWarmReboot, watchNmsEventsEnodebColdRebootSoftwareDownload);
		waitForAllProcessesToEnd(waitForFirstPtrRequestfterSoftwareDownload, watchNmsEventsRelayColdRebootAfterSoftwareDownload, watchNmsEventsRelayScanListAfterSoftwareDownload, waitForSecondPtrRequestfterSoftwareDownload, watchNmsEventsRelayWarmRebootAfterSoftwareDownload,	watchNmsEventsEnodebColdReboot);
		addAllStagesTimeToHtmlTableAndPrintHtmlTable(timelineStageIndex, rebootTime);
		if(isEnodeBWithDonor){
			if(waitForFirstPtrRequest != null){waitForFirstPtrRequest.printStatus("Appendix A");}
			if(watchNmsEventsRelayColdReboot != null){watchNmsEventsRelayColdReboot.printStatus("Appendix B");}
			if(watchNmsEventsRelayScanList != null){watchNmsEventsRelayScanList.printStatus("Appendix C");}
			if(waitForSecondPtrRequest != null){waitForSecondPtrRequest.printStatus("Appendix D");}
			if(watchNmsEventsRelayWarmReboot != null){watchNmsEventsRelayWarmReboot.printStatus("Appendix E");}
		}
		if(dut.isSwUpgradeDuringPnP()){
			if(watchNmsEventsEnodebColdRebootSoftwareDownload != null){watchNmsEventsEnodebColdRebootSoftwareDownload.printStatus("Appendix E1");}
			if(isEnodeBWithDonor){
				if(waitForFirstPtrRequestfterSoftwareDownload != null){waitForFirstPtrRequestfterSoftwareDownload.printStatus("Appendix E2");}
				if(watchNmsEventsRelayColdRebootAfterSoftwareDownload != null){watchNmsEventsRelayColdRebootAfterSoftwareDownload.printStatus("Appendix E3");}
				if(watchNmsEventsRelayScanListAfterSoftwareDownload != null){watchNmsEventsRelayScanListAfterSoftwareDownload.printStatus("Appendix E4");}
				if(waitForSecondPtrRequestfterSoftwareDownload != null){waitForSecondPtrRequestfterSoftwareDownload.printStatus("Appendix E5");}
				if(watchNmsEventsRelayWarmRebootAfterSoftwareDownload != null){watchNmsEventsRelayWarmRebootAfterSoftwareDownload.printStatus("Appendix E6");}
			}
		}
		if(watchNmsEventsEnodebColdReboot != null){watchNmsEventsEnodebColdReboot.printStatus("Appendix F");}
		if(dut.isSwUpgradeDuringPnP()){
			if(watchNmsEventsEnodebSoftwareActivate != null){watchNmsEventsEnodebSoftwareActivate.printStatus("Appendix F1");}
		}
		//closeLogs(dut);
	}
	
	@Test
	@TestProperties(name = "Test PnP after Warm Reboot",
	returnParam = { "IsTestWasSuccessful"},
	paramsExclude = {"IsTestWasSuccessful"})
	public void pnpWarmReboot() {
		this.expectedDurationsAndStageNamesOrdered = dut.getExpectedDurationsAndStageNamesOrderedForWarmReboot();
		int step = 1;
		int timelineStageIndex = 0;
		htmlTimelineTable = new HtmlTimelineStage[40];
		WatchAllRunningTimeout watchAllRunningTimeout = new WatchAllRunningTimeout(dut);
		if(isEnodeBWithDonor){
			EnodeBWithDonor eNodeBWithDonorDut = (EnodeBWithDonor)dut;
			EnodeB donor = eNodeBWithDonorDut.getDonor();
			if (donor.isInOperationalStatus()){
				report.report("Donor is in Running State.");
			}else{
				report.report("Donor is NOT in Running State.", Reporter.FAIL);
				reason = "Donor is NOT in Running State.";
				return;
			}
			String relayVersion = eNodeBWithDonorDut.getRelayRunningVersion();
			if(relayVersion != ""){
				report.report("Relay's Running Version: " + relayVersion);
			}
		}
		report.report(dut.getName() + "'s Running Version: " + dut.getRunningVersion());
		suspendIpsecTunnelManagerIfEnabled(dut); 
		final long rebootTime = performWarmReboot(step++, timelineStageIndex++, dut, watchAllRunningTimeout);
		if(rebootTime == 0){
			return;
		}
		if(isEnodeBWithDonor){
			EnodeBWithDonor eNodeBWithDonor = (EnodeBWithDonor)dut;
			waitForDebugPortAvailability((EnodeBWithDonor)eNodeBWithDonor);
			startToCollectPacketsSentByRelayToDnsAndWaitForRelayToConnectToDonor(step++, timelineStageIndex++, rebootTime, eNodeBWithDonor, "Relay Attach.", FIRST_RELAY_ATTACH_EXPECTED_DURATION_IN_MILI);
		}
		waitForSnmpAvailability(step++, timelineStageIndex++, rebootTime, dut, FIRST_IPSEC_BRING_UP_EXPECTED_DURATION_IN_MILI, RebootType.WARM_REBOOT);
		if(isEnodeBWithDonor){
			checkIfClockSyncLock(step++, ((EnodeBWithDonor)dut));
		}
		watchAllRunningTimeout.run();
		waitForAllRunning(step++, timelineStageIndex++, rebootTime, dut, WAIT_FOR_ALL_RUNNING_EXPECTED_DURATION_IN_MILI);
		watchAllRunningTimeout.stopCounting();
		watchAllRunningTimeout.printStatus();
		checkIfUEsAreConnectedToEnodeB(step++, dut);
		addAllStagesTimeToHtmlTableAndPrintHtmlTable(timelineStageIndex, rebootTime);
	}

	/***************** Test Helper Functions ********************/
	
	private Pair<Long, Triple<Integer, String, String>> performColdRebootAndConvertNmsProfileToPnP(int step, int timelineStageIndex, EnodeB eNodeB, WatchAllRunningTimeout watchAllRunningTimeout) {
		GeneralUtils.startLevel(step + ". Start Up.");
		report.report("Perform Cold Reboot.");
		long rebootTime = 0;
		Triple<Integer, String, String> swActivationDetails = null;
		if(eNodeB.reboot(RebootType.COLD_REBOOT)){
			rebootTime = System.currentTimeMillis();
			if(eNodeB.isSwUpgradeDuringPnP()){
				WAIT_FOR_ALL_RUNNING_TIME = 30 * 60 * 1000;
				swActivationDetails = SoftwareUtiles.getInstance().updatDefaultSoftwareImage(eNodeB);
			}
			watchAllRunningTimeout.startCounting(rebootTime, WAIT_FOR_ALL_RUNNING_TIME);
			report.report("Convert To PnP Configuration in NMS.");
			netspanServer.convertToPnPConfig(eNodeB);
		}else{
			report.report("eNodeB is available via SNMP.", Reporter.FAIL);
			report.report("eNodeB did NOT perform Reboot.", Reporter.FAIL);
			reason = "eNodeB did NOT perform Reboot.";
		}
		saveStageTimeForHtmlTable(timelineStageIndex, "Cold Reboot.", rebootTime, rebootTime, 1, true);
		GeneralUtils.unSafeSleep(1*60*1000);
		GeneralUtils.stopLevel();
		
		return new Pair<Long, Triple<Integer, String, String>>(rebootTime, swActivationDetails);
	}
	
	private long performWarmReboot(int step, int timelineStageIndex, EnodeB eNodeB, WatchAllRunningTimeout watchAllRunningTimeout) {
		GeneralUtils.startLevel(step + ". Start Up.");
		report.report("Perform Warm Reboot.");
		long rebootTime = 0;
		if(eNodeB.reboot(RebootType.WARM_REBOOT)){
			rebootTime = System.currentTimeMillis();
			WAIT_FOR_ALL_RUNNING_TIME = 5 * 60 * 1000;
			watchAllRunningTimeout.startCounting(rebootTime, WAIT_FOR_ALL_RUNNING_TIME);
		}else{
			report.report("eNodeB is available via SNMP.", Reporter.FAIL);
			report.report("eNodeB did NOT perform Reboot.", Reporter.FAIL);
			reason = "eNodeB did NOT perform Reboot.";
		}
		saveStageTimeForHtmlTable(timelineStageIndex, "Warm Reboot.", rebootTime, rebootTime, 1, true);
		GeneralUtils.unSafeSleep(1*60*1000);
		GeneralUtils.stopLevel();
		
		return rebootTime;
	}
	
	private boolean waitForDebugPortAvailability(EnodeBWithDonor eNodeBWithDonor){
		long startTime = System.currentTimeMillis();
		while((System.currentTimeMillis() - startTime) <= WAIT_FOR_DEBUG_PORT_TIMEOUT){
			if(isNinjaAvailableViaDebugPort(eNodeBWithDonor)){
				return true;
			}
		}
		return false;
	}
	
	private boolean isNinjaAvailableViaDebugPort(EnodeBWithDonor eNodeBWithDonor) {
		boolean isAvailable = false;
		try {
			eNodeBWithDonor.debugPort.init();
		} catch (Exception e) {
			GeneralUtils.printToConsole("FAILED to initialize Ninja's Debug Port.");
			e.printStackTrace();
		}
		if(eNodeBWithDonor.debugPort.isSessionConnected()){
			report.report("Ninja is available via Debug Port: " + eNodeBWithDonor.debugPort.getDEBUG_PORT());
			isAvailable  = true;
		}else{
			isAvailable = false;
		}
		eNodeBWithDonor.debugPort.closeSession();
		return isAvailable;
	}
	
	private void startToCollectPacketsSentByRelayToDnsAndWaitForRelayToConnectToDonor(int step, int timelineStageIndex, long rebootTime, EnodeBWithDonor eNodeBWithDonor, String stageName, long stageExpectedDuration) {
		GeneralUtils.startLevel(step +". "+ stageName);
		boolean isStageCompleted = false;
		long stageFinishedTimeInMili = GeneralUtils.ERROR_VALUE;
		UE relay = eNodeBWithDonor.getRelay();
		String wanIpAddress = relay.getWanIpAddress();
		DNS dns = DNS.getInstance();
		
		dns.startToCollectPackets(wanIpAddress, WAIT_FOR_PTR_REQUEST_TIMEOUT);//Collecting all packets from IP Address to DNS.
		
		report.report("Wait for the Relay connect Donor (WAN IP Address="+wanIpAddress+").");
		ArrayList<UE> relayInArray = new ArrayList<UE>();
		relayInArray.add(relay);
		if(PeripheralsConfig.getInstance().WaitForUEsAndEnodebConnectivity(relayInArray, eNodeBWithDonor.getDonor(), WAIT_FOR_RELAY_CONNECT_DONOR)){
			report.report("Relay is connected to the Donor.");
			isStageCompleted = true;
			stageFinishedTimeInMili = System.currentTimeMillis();
		}else{
			report.report("Reached timeout ("+WAIT_FOR_RELAY_CONNECT_DONOR/1000+" sec) and the Relay is NOT connected to the Donor.", Reporter.WARNING);
		}
		if(stageName.toLowerCase().contains("second")){stageName = "Scan List And Second Relay Attach.";}
		saveStageTimeForHtmlTable(timelineStageIndex, stageName, rebootTime, stageFinishedTimeInMili, stageExpectedDuration, isStageCompleted);
		GeneralUtils.stopLevel();
	}
	
	private WaitForPtrRequest waitForPtrRequest(int timelineStageIndex, String stageName, EnodeBWithDonor eNodeBWithDonor, long rebootTime, long stageExpectedDuration){
		WaitForPtrRequest wfpr = new WaitForPtrRequest(stageName, eNodeBWithDonor, rebootTime);
		wfpr.startWaiting(timelineStageIndex, stageExpectedDuration);
		return wfpr;
	}
	
	private void waitForFirstIPSecBringUp(int step, int timelineStageIndex, long rebootTime, EnodeBWithDonor eNodeB, long stageExpectedDuration) {
		String stageName = "";
		if(eNodeB.isIpsecTunnelEnabled()){
			stageName = "First IPSec Bring Up.";
		}else{
			stageName = "Getting IP Via DHCP";
		}
		GeneralUtils.startLevel(step +". "+ stageName);
		boolean isStageCompleted = false;
		long stageFinishedTimeInMili = GeneralUtils.ERROR_VALUE;
		if(eNodeB.isIpsecTunnelEnabled()){
			try {
				if(TunnelManager.getInstance(enbInTest, report).waitForIPSecTunnelToOpen(WAIT_UNTIL_SNMP_AVAIALBLILITY, eNodeB)){
					report.report("IPSec Tunnel is Opened.");
					isStageCompleted = true;
					stageFinishedTimeInMili = System.currentTimeMillis();
				}else{
					report.report("IPSec Tunnel Was NOT Opened.", Reporter.WARNING);
				}
			} catch (Exception e) {
				GeneralUtils.printToConsole("FAILED to get TunnelManager");
				e.printStackTrace();
			}
		}else{
			if(eNodeB.waitForReachable(WAIT_UNTIL_SNMP_AVAIALBLILITY)){
				report.report(eNodeB.getName() + " Is Reachable Via " + eNodeB.getIpAddress());
			}else{
				report.report(eNodeB.getName() + " Is NOT Reachable Via " + eNodeB.getIpAddress(), Reporter.WARNING);
			}
		}
		saveStageTimeForHtmlTable(timelineStageIndex, stageName, rebootTime, stageFinishedTimeInMili, stageExpectedDuration, isStageCompleted);
		GeneralUtils.stopLevel();
	}
	
	private void waitForRelayDisconnectDonor(int step, EnodeB eNodeB) {
		GeneralUtils.startLevel(step + ". Wait for the Relay disconnect Donor");
		EnodeBWithDonor eNodeBWithDonor = ((EnodeBWithDonor)eNodeB);
		ArrayList<UE> relayInArray = new ArrayList<>();
		relayInArray.add(eNodeBWithDonor.getRelay());
		if(PeripheralsConfig.getInstance().WaitForUEsAndEnodebDisconnect(relayInArray, eNodeBWithDonor.getDonor(), WAIT_FOR_RELAY_DISCONNECT_DONOR)){
			report.report("Relay disconnect Donor.");
		}else{
			report.report("Reached timeout ("+WAIT_FOR_RELAY_DISCONNECT_DONOR/1000+" sec) and the Relay STILL connected to the Donor.", Reporter.WARNING);
		}
		GeneralUtils.stopLevel();
	}
	
	private WatchNmsEvents startFollowNmsEvents(int timelineStageIndex, long stageExpectedDuration, EnodeB eNodeB, long startTime, long timeout, 
			String stageName, Triple<String, String, String>[] eventListToFollow, long rebootTimeAfterSoftwareDownload, boolean isRebootAfterSoftwareDownload){
		WatchNmsEvents watchNmsEvents = new WatchNmsEvents(eNodeB, eventListToFollow);
		watchNmsEvents.startFollowNmsEvents(timelineStageIndex, stageExpectedDuration, startTime, timeout, stageName, rebootTimeAfterSoftwareDownload, isRebootAfterSoftwareDownload);
		return watchNmsEvents;
	}
	
	private void waitForSnmpAvailability(int step, int timelineStageIndex, long rebootTime, EnodeB eNodeB, long stageExpectedDuration, RebootType rebootType) {
		String snmpAvailabilityStageName = "SNMP Availability.";
		if(isEnodeBWithDonor && eNodeB.isIpsecTunnelEnabled() && (rebootType == RebootType.COLD_REBOOT)){
			snmpAvailabilityStageName = "Second IPSec Bring Up.";
		}else if(eNodeB.isIpsecTunnelEnabled()){
			snmpAvailabilityStageName = "IPSec Bring Up.";
		}
		GeneralUtils.startLevel(step + ". " + snmpAvailabilityStageName);
		if(eNodeB.isIpsecTunnelEnabled()){
			boolean isIPSecTunnelOpened = false;
			try {
				isIPSecTunnelOpened = TunnelManager.getInstance(enbInTest, report).waitForIPSecTunnelToOpen(WAIT_UNTIL_SNMP_AVAIALBLILITY, eNodeB);
			} catch (Exception e) {
				GeneralUtils.printToConsole("FAILED to get TunnelManager");
				e.printStackTrace();
			}
			if(isIPSecTunnelOpened){
				saveStageTimeForHtmlTable(timelineStageIndex, snmpAvailabilityStageName, rebootTime, System.currentTimeMillis(), stageExpectedDuration, true);
				resumeIpsecTunnelManagerIfEnabled(eNodeB);
				report.report("IPSec Tunnel is Opened.");
				if(isEnodeBWithDonor){
					EnodeBWithDonor eNodeBWithDonor = (EnodeBWithDonor)eNodeB;
					if(isNinjaAvailableViaDebugPort(eNodeBWithDonor) && eNodeBWithDonor.isAvailable()){
						report.report("Ninja is available via Debug Port: " + eNodeBWithDonor.debugPort.getDEBUG_PORT() + ", while IPSec Tunnel is open: " + eNodeB.getIpAddress(), Reporter.FAIL);
						reason = "Ninja is available via Debug Port.";
					}else{
						report.report("Ninja is NOT available via Debug Port: " + eNodeBWithDonor.debugPort.getDEBUG_PORT() + ", while IPSec Tunnel is open: "+ eNodeB.getIpAddress());
					}
				}
			}else{
				report.report("FAILED to open IPSec Tunnel.", Reporter.WARNING);
				saveStageTimeForHtmlTable(timelineStageIndex, snmpAvailabilityStageName, rebootTime, GeneralUtils.ERROR_VALUE, stageExpectedDuration, false);
			}
		}else if(eNodeB.waitForSnmpAvailable(WAIT_UNTIL_SNMP_AVAIALBLILITY)){
			report.report("SNMP Is Available.");
			saveStageTimeForHtmlTable(timelineStageIndex, snmpAvailabilityStageName, rebootTime, System.currentTimeMillis(), stageExpectedDuration, true);
		}else{
			report.report("SNMP Is NOT Available.", Reporter.WARNING);
			saveStageTimeForHtmlTable(timelineStageIndex, snmpAvailabilityStageName, rebootTime, GeneralUtils.ERROR_VALUE, stageExpectedDuration, false);
		}
		GeneralUtils.stopLevel();
	}

	private void checkIfClockSyncLock(int step, EnodeBWithDonor eNodeBWithDonor) {
		GeneralUtils.startLevel(step + ". Clock Sync.");
		String syncState = netspanServer.getSyncState(eNodeBWithDonor);
		if(syncState != null && syncState.equals("Synchronized")){
			report.report("eNodeB is Synchronized.");
		}else{
			report.report("eNodeB is NOT Synchronized (Synchronization Status = "+syncState+").", Reporter.WARNING);
		}
		GeneralUtils.stopLevel();
	}
	
	private void waitForAllRunning(int step, int timelineStageIndex, long rebootTime, EnodeB eNodeB, long stageExpectedDuration) {
		GeneralUtils.startLevel(step + ". Wait for All Running And InService.");
		if(eNodeB.waitForAllRunningAndInService(WAIT_FOR_ALL_RUNNING_TIME)){
			report.step("eNodeB Reached to All Running And InService.");
		}else{
			report.report("eNodeB Did NOT reached to All Running And InService.", Reporter.FAIL);
			reason = "eNodeB Did NOT reached to All Running And InService.";
		}
		saveStageTimeForHtmlTable(timelineStageIndex, "All Running.", rebootTime, System.currentTimeMillis(), stageExpectedDuration, true);
		GeneralUtils.stopLevel();
	}

	private void checkIfUEsAreConnectedToEnodeB(int step, EnodeB eNodeB) {
		GeneralUtils.startLevel(step + ". Attach external UE To eNodeB.");
		if(eNodeB.isInOperationalStatus()){
			Traffic traffic = Traffic.getInstance(SetupUtils.getInstance().getAllUEs());
			if (traffic.getGeneratorType() == TrafficGeneratorType.TestCenter) {
				String TrafficFile = traffic.getTg().getDefaultConfigTccFile();
				int lastDot = TrafficFile.lastIndexOf('.');
				String HOTrafficFile = TrafficFile.substring(0, lastDot) + "_HO" + TrafficFile.substring(lastDot);
				traffic.configFile = new File(HOTrafficFile);
			}
			ArrayList<UE> allUEs = SetupUtils.getInstance().getallUEsPerEnodeb(eNodeB);
			report.report("Starting traffic.");
			traffic.startTraffic(allUEs);
			report.report("Changing UE state from idle to connected using traffic.");
			boolean status = false;
			for(UE ue : allUEs){
				ArrayList<UE> ueInArray = new ArrayList<>();
				ueInArray.add(ue);
				status = status || PeripheralsConfig.getInstance().checkIfAllUEsAreConnectedToNode(ueInArray, eNodeB);
			}
			if(status){
				report.report("UEs are connected To The eNodeB");
				GeneralUtils.reportHtmlLink("ue show link", eNodeB.lteCli("ue show link"));
			}else{
				report.report("No UEs connected To The eNodeB", Reporter.WARNING);
			}
			report.report("Stopping traffic.");
			traffic.stopTraffic();
		}else{
			report.report("eNodeB is NOT in Running State.", Reporter.FAIL);
			reason = "eNodeB is NOT in Running State.";
		}
		GeneralUtils.stopLevel();
	}
	
	private void waitForAllProcessesToEnd(WaitForPtrRequest waitForFirstPtrRequest,
			WatchNmsEvents watchNmsEventsRelayColdReboot, WatchNmsEvents watchNmsEventsRelayScanList,
			WaitForPtrRequest waitForSecondPtrRequest, WatchNmsEvents watchNmsEventsRelayWarmReboot,
			WatchNmsEvents watchNmsEventsEnodebColdReboot) {
		int waitingTimeout = 0;
		while(((waitForFirstPtrRequest != null && waitForFirstPtrRequest.getState() != WatchdogState.ENDED) ||
				(watchNmsEventsRelayColdReboot != null && watchNmsEventsRelayColdReboot.getState() != WatchdogState.ENDED) ||
				(watchNmsEventsRelayScanList != null && watchNmsEventsRelayScanList.getState() != WatchdogState.ENDED) ||
				(waitForSecondPtrRequest != null && waitForSecondPtrRequest.getState() != WatchdogState.ENDED) ||
				(watchNmsEventsRelayWarmReboot != null && watchNmsEventsRelayWarmReboot.getState() != WatchdogState.ENDED) ||
				(watchNmsEventsEnodebColdReboot != null && watchNmsEventsEnodebColdReboot.getState() != WatchdogState.ENDED)) 
				&& (waitingTimeout  < 60)){
			waitingTimeout++;
			GeneralUtils.unSafeSleep(5000);
		}
	}

	/***************** Infra Helper Functions ********************/
	
	private void openLogs(EnodeB eNodeB){
		report.report("Open logs.");
		if(isNinja){
			eNodeB.shell("cp /bsdata/prestartup.tnet /bs/relay/");
			eNodeB.shell("cp /bsdata/prestartup.tnet /bs/relay/board/");
			eNodeB.shell("cp /bsdata/prestartup.tnet /bs/relay/lib/altair/");
			
			eNodeB.lteCli("logger threshold set process=oam std_out=2");
			eNodeB.lteCli("logger threshold set client=OAM_RELAY std_out=0");
			eNodeB.lteCli("logger threshold set client=RELAY_COMM std_out=0");
			eNodeB.lteCli("logger threshold set client=RELAY_MSG std_out=0");
			eNodeB.lteCli("logger threshold set client=SECURITY std_out=0");
			eNodeB.lteCli("logger threshold set client=DHCP std_out=0");
		}
		eNodeB.lteCli("logger threshold set std_out=3");
	}
	
	private void closeLogs(EnodeB eNodeB){
		GeneralUtils.startLevel("Close logs.");
		eNodeB.lteCli("logger threshold set std_out=4");
		if(isNinja){
			eNodeB.shell("rm /bs/relay/prestartup.tnet");
			eNodeB.shell("rm /bs/relay/board/prestartup.tnet");
			eNodeB.shell("rm /bs/relay/lib/altair/prestartup.tnet");
			
			eNodeB.reboot();
			report.report("Wait for All Running And InService.");
			eNodeB.waitForAllRunningAndInService(EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
		}
		GeneralUtils.stopLevel();
	}
	
	private void resumeIpsecTunnelManagerIfEnabled(EnodeB eNodeB) {
		if(eNodeB.isIpsecTunnelEnabled()){
			try {
				TunnelManager.getInstance(enbInTest, report).safeResume();
			} catch (Exception e) {
				GeneralUtils.printToConsole("FAILED to get TunnelManager");
				e.printStackTrace();
			}
		}
	}

	private void suspendIpsecTunnelManagerIfEnabled(EnodeB eNodeB) {
		if(eNodeB.isIpsecTunnelEnabled()){
			try {
				TunnelManager.getInstance(enbInTest, report).safeSuspend();
			} catch (Exception e) {
				GeneralUtils.printToConsole("FAILED to get TunnelManager");
				e.printStackTrace();
			}
		}
	}
	
	private String sendCommandToNinjaViaDebugPort(EnodeBWithDonor eNodeBWithDonor, String cmd) {
		String response = "";
		try {
			eNodeBWithDonor.debugPort.init();
		} catch (Exception e) {
			GeneralUtils.printToConsole("FAILED to initialize Ninja's Debug Port.");
			e.printStackTrace();
		}
		response = eNodeBWithDonor.debugPort.sendCommands(EnodeBComponent.LTE_CLI_PROMPT, cmd);
		eNodeBWithDonor.debugPort.closeSession();
		return response;
	}
	
	private void saveStageTimeForHtmlTable(int timelineStageIndex, String stageName, long rebootTimeInMili, long stageFinishedTimeInMili, long stageExpectedDuration, boolean isStageCompleted){
		if(timelineStageIndex != GeneralUtils.ERROR_VALUE){
			htmlTimelineTable[timelineStageIndex] = new HtmlTimelineStage(stageName, rebootTimeInMili, stageFinishedTimeInMili, stageExpectedDuration, isStageCompleted);
		}
	}
	
	private void addAllStagesTimeToHtmlTableAndPrintHtmlTable(int timelineStageIndex, long rebootTime) {//htmlTable.addNewColumn(stageName);
		addStageNamesToHtmlTable(timelineStageIndex);
		addRowExpectedHtmlTimeline(htmlTable);
		addAllStagesTimeToHtmlTable(timelineStageIndex, rebootTime);
		htmlTable.reportTable("");
	}
	
	private void addStageNamesToHtmlTable(int tableSize){
		for(int i = 0; i < tableSize; i++){
			htmlTable.addNewColumn(expectedDurationsAndStageNamesOrdered[i].getElement1());
		}
	}
	
	private void addAllStagesTimeToHtmlTable(int tableSize, long rebootTime){
		htmlTable.addNewRow("Actual Timeline"); 
		addStageTimeToHtmlTable(htmlTimelineTable[0].stageName, htmlTimelineTable[0].rebootTimeInMili+1000,
				htmlTimelineTable[0].stageFinishedTimeInMili+1000, htmlTimelineTable[0].stageFinishedTimeInMili, 2000, htmlTimelineTable[0].isStageCompleted);
		for(int i = 1; i < tableSize; i++){
			if(htmlTimelineTable[i] != null){
				if(htmlTimelineTable[i].stageFinishedTimeInMili == GeneralUtils.ERROR_VALUE){
					htmlTimelineTable[i].stageFinishedTimeInMili = htmlTimelineTable[i-1].stageFinishedTimeInMili + expectedDurationsAndStageNamesOrdered[i].getElement0();
				}
				addStageTimeToHtmlTable(htmlTimelineTable[i].stageName, htmlTimelineTable[i].rebootTimeInMili,
					htmlTimelineTable[i].stageFinishedTimeInMili, htmlTimelineTable[i-1].stageFinishedTimeInMili, expectedDurationsAndStageNamesOrdered[i].getElement0(), htmlTimelineTable[i].isStageCompleted);
			}else{
				saveStageTimeForHtmlTable(i, expectedDurationsAndStageNamesOrdered[i].getElement1(), rebootTime, htmlTimelineTable[i-1].stageFinishedTimeInMili + expectedDurationsAndStageNamesOrdered[i].getElement0(), expectedDurationsAndStageNamesOrdered[i].getElement0(), false);
				addStageTimeToHtmlTable(expectedDurationsAndStageNamesOrdered[i].getElement1(), rebootTime, htmlTimelineTable[i-1].stageFinishedTimeInMili + expectedDurationsAndStageNamesOrdered[i].getElement0(), htmlTimelineTable[i-1].stageFinishedTimeInMili, expectedDurationsAndStageNamesOrdered[i].getElement0(), false);
			}
		}
	}
	
	private boolean addStageTimeToHtmlTable(String stageName, long rebootTimeInMili, long stageFinishedTimeInMili,
			long previousStageFinishedTimeInMili, long stageExpectedDuration, boolean isStageCompleted){
		GeneralUtils.printToConsole("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		GeneralUtils.printToConsole("stageName="+stageName);
		GeneralUtils.printToConsole("previousStageFinishedTimeInMili="+previousStageFinishedTimeInMili);
		GeneralUtils.printToConsole("stageFinishedTimeInMili="+stageFinishedTimeInMili);
		GeneralUtils.printToConsole("rebootTimeInMili="+rebootTimeInMili);
		GeneralUtils.printToConsole("stageFinishedTimeInMili="+stageFinishedTimeInMili);
		GeneralUtils.printToConsole("stageExpectedDuration="+stageExpectedDuration);
		GeneralUtils.printToConsole("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		String stageTimeStr = "--:--:--";
		HtmlFieldColor severity = HtmlFieldColor.WHITE;
		Interval stageDurationInterval = null;
		try{
			long usingStageFinishedTimeInMiliForGuaranteeLegalInterval = previousStageFinishedTimeInMili < stageFinishedTimeInMili ? stageFinishedTimeInMili : previousStageFinishedTimeInMili + 2000;
			stageDurationInterval = new Interval(previousStageFinishedTimeInMili, usingStageFinishedTimeInMiliForGuaranteeLegalInterval);
			Interval stageTimestampInterval = new Interval(rebootTimeInMili, usingStageFinishedTimeInMiliForGuaranteeLegalInterval);
			Duration stageDurationDuration = stageDurationInterval.toDuration();
			Duration stageTimestampDuration = stageTimestampInterval.toDuration();
			stageTimeStr = "Timestamp: "+twoCharacter(stageTimestampDuration.getStandardHours()) + ":" 
												+ twoCharacter(stageTimestampDuration.getStandardMinutes()%60) + ":" 
												+ twoCharacter(stageTimestampDuration.getStandardSeconds()%60) 
												+ "<br />"
												+ "Duration: "+twoCharacter(stageDurationDuration.getStandardHours()) + ":" 
												+ twoCharacter(stageDurationDuration.getStandardMinutes()%60) + ":" 
												+ twoCharacter(stageDurationDuration.getStandardSeconds()%60);
		}catch(Exception e){
			e.printStackTrace();
			GeneralUtils.printToConsole("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
			GeneralUtils.printToConsole("stageName="+stageName);
			GeneralUtils.printToConsole("previousStageFinishedTimeInMili="+previousStageFinishedTimeInMili);
			GeneralUtils.printToConsole("stageFinishedTimeInMili="+stageFinishedTimeInMili);
			GeneralUtils.printToConsole("rebootTimeInMili="+rebootTimeInMili);
			GeneralUtils.printToConsole("stageFinishedTimeInMili="+stageFinishedTimeInMili);
			GeneralUtils.printToConsole("stageExpectedDuration="+stageExpectedDuration);
			GeneralUtils.printToConsole("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		}
		
		severity = HtmlFieldColor.GREEN;
		if(isStageCompleted == false){
			severity = HtmlFieldColor.GRAY;
			stageTimeStr = "Didn't detect stage completed, theoretical times only: " + stageTimeStr;
		}else if(stageDurationInterval == null){
			severity = HtmlFieldColor.GRAY;
			stageTimeStr = "Bad Interval, " + stageTimeStr;
		}else if(stageDurationInterval.toDurationMillis() > ((long)(stageExpectedDuration * 1.5))){
			severity = HtmlFieldColor.RED;
		}else if(stageDurationInterval.toDurationMillis() > ((long)(stageExpectedDuration * 1.25))){
			severity = HtmlFieldColor.YELLOW;
		}
	
		htmlTable.addField(severity, stageTimeStr);
		return true;
	}
	
	private String twoCharacter(long i) {
		String time = "" + i;
		if(time.length() < 2){
			time = "0" + time;
		}else if(time.length() > 2){
			while(time.length() > 2){
				i = i / 10;
				time = "" + i;
			}
		}
		return time;
	}
	
	private void addRowExpectedHtmlTimeline(HtmlTable htmlTable){
		htmlTable.addNewRow("Expected Timeline");
		
		long timestamp = 0;
		for(int i = 0; i < expectedDurationsAndStageNamesOrdered.length; i++){
			timestamp+=expectedDurationsAndStageNamesOrdered[i].getElement0();
			htmlTable.addField(HtmlFieldColor.WHITE, "Timestamp: " + timeInMiliToString(timestamp)
						+ "<br />" + "Duration: " + timeInMiliToString(expectedDurationsAndStageNamesOrdered[i].getElement0()));
		}
	}
	
	private void printExpectedHtmlTimelineTable(){
		HtmlTable expectedHtmlTable = new HtmlTable();
		expectedHtmlTable.addNewRow("Expected Timeline");
		
		long timestamp = 0;
		for(int i = 0; i < expectedDurationsAndStageNamesOrdered.length; i++){
			expectedHtmlTable.addNewColumn("Timestamp: " + timeInMiliToString(timestamp)
						+ "<br />" + "Duration: " + timeInMiliToString(expectedDurationsAndStageNamesOrdered[i].getElement0()));
			expectedHtmlTable.addField(HtmlFieldColor.WHITE, expectedDurationsAndStageNamesOrdered[i].getElement1());
			timestamp+=expectedDurationsAndStageNamesOrdered[i].getElement0();
		}
		expectedHtmlTable.reportTable("");
	}
	
	private String timeInMiliToString(long timestampInMili){
		String timestampStr = twoCharacter(timestampInMili/(60*60*1000)) + ":" + twoCharacter((timestampInMili/(60*1000))%60) + ":" + twoCharacter((timestampInMili/(1000))%60);
		return timestampStr;
	}

	/***************** Nested Classes ********************/
	
	private enum WatchdogState{
		INITIALIZED,
		RUNNING,
		ENDED, 
		PRINTED
	}
	class WatchNmsEvents extends Utils.WatchDog.Command {
		private WatchDogManager wd;
		private EnodeB eNodeB;
		private long watchNmsStartTime;
		private long timeoutInMili;
		private ArrayList<Triple<String, String, String>> tmpEventListToFollow;
		private ArrayList<EventInfo> uploadedEventListFromFollowList;
		private Date coldRebootStartTimeDate;
		private String stageName;
		private WatchdogState state;
		private int timelineStageIndex;
		private long coldRebootStartTime;
		private long stageExpectedDuration;
		private XMLGregorianCalendar uploadedTime;
		
		public WatchNmsEvents(EnodeB eNodeB, Triple<String, String, String>[] eventListToFollow){
			this.wd = WatchDogManager.getInstance();
			this.eNodeB = eNodeB;
			this.tmpEventListToFollow = new ArrayList<>();
			this.uploadedEventListFromFollowList = new ArrayList<>();
			for(int i = 0; i < eventListToFollow.length; i++){
				this.tmpEventListToFollow.add(eventListToFollow[i]);
			}
			uploadedTime = null;
			this.state = WatchdogState.INITIALIZED;
		}
		
		public void startFollowNmsEvents(int timelineStageIndex, long stageExpectedDuration, long coldRebootStartTime, long timeoutInMili, String stageName, long rebootTimeAfterSoftwareDownload, boolean isRebootAfterSoftwareDownload){
			this.timelineStageIndex = timelineStageIndex;
			this.stageExpectedDuration = stageExpectedDuration;
			this.coldRebootStartTime =coldRebootStartTime;
			if(isRebootAfterSoftwareDownload){
				this.coldRebootStartTimeDate = new Date(rebootTimeAfterSoftwareDownload);
			}else{
				this.coldRebootStartTimeDate = new Date(coldRebootStartTime);
			}
			this.timeoutInMili = timeoutInMili;
			this.stageName = stageName;
			this.watchNmsStartTime = System.currentTimeMillis();
			this.wd.addCommand(this);
			this.state = WatchdogState.RUNNING;
		}
		
		private void endFollowNmsEvents(){
			wd.removeCommand(this);
			this.state = WatchdogState.ENDED;
		}
	
		@Override
		public void run() {
			if(((System.currentTimeMillis() - this.watchNmsStartTime) < timeoutInMili)){
				List<EventInfo> eventInfoListFromNMS = alarmsAndEvents.getAllEventsNode(eNodeB, coldRebootStartTimeDate, new Date(System.currentTimeMillis()));
				ArrayList<Triple<String, String, String>> tmpEventListToRemoveFromFollowList = new ArrayList<Triple<String, String, String>>();
				for(Triple<String, String, String> eventToFollow : this.tmpEventListToFollow){
					for(EventInfo eventInfoFromNMS : eventInfoListFromNMS){
						if(eventInfoFromNMS.getEventType().equals(eventToFollow.getLeftElement()) && 
								eventInfoFromNMS.getSourceType().equals(eventToFollow.getMiddleElement()) &&
								eventInfoFromNMS.getEventInfo().contains(eventToFollow.getRightElement())){
							uploadedEventListFromFollowList.add(eventInfoFromNMS);
							tmpEventListToRemoveFromFollowList.add(eventToFollow);
							if(uploadedTime == null || uploadedTime.getMillisecond() < eventInfoFromNMS.getReceivedTime().getMillisecond()){
								uploadedTime = eventInfoFromNMS.getReceivedTime();
							}
							break;
						}
					}
				}
				for(Triple<String, String, String> eventToRemove : tmpEventListToRemoveFromFollowList){
					this.tmpEventListToFollow.remove(eventToRemove);
				}
				if(this.tmpEventListToFollow.isEmpty()){
					Date date = new Date();
					if(uploadedTime != null){
						date .setYear(uploadedTime.getYear()-1900);
						date.setMonth(uploadedTime.getMonth()-1);
						date.setDate(uploadedTime.getDay());
						date.setHours(uploadedTime.getHour());
						date.setMinutes(uploadedTime.getMinute());
						date.setSeconds(uploadedTime.getSecond());
					}
					saveStageTimeForHtmlTable(this.timelineStageIndex, this.stageName, this.coldRebootStartTime, /*date.getTime()*/System.currentTimeMillis(), stageExpectedDuration, true);
					endFollowNmsEvents();
				}
			}else{
				saveStageTimeForHtmlTable(this.timelineStageIndex, this.stageName, this.coldRebootStartTime, GeneralUtils.ERROR_VALUE, stageExpectedDuration, false);
				endFollowNmsEvents();
			}
		}
		
		public boolean printStatus(String appendixX){
			if(this.state == WatchdogState.ENDED){
				GeneralUtils.startLevel(appendixX + ": " + stageName);
				for(EventInfo eventInfoFromNMS : uploadedEventListFromFollowList){
					report.report("Event Uploaded To NMS: Event Type: " + eventInfoFromNMS.getEventType() + 
							", Source Type: " + eventInfoFromNMS.getSourceType() + 
							", Event Info: " + eventInfoFromNMS.getEventInfo()+ 
							", Received Time: " + eventInfoFromNMS.getReceivedTime().toString());
				}
				if(this.tmpEventListToFollow.isEmpty()){
					report.report("All Relevant Events To "+this.stageName+" Stage Uploaded To NMS.");
				}else{
					report.report("Stopped Following NMS Events due to Timeout of " +(timeoutInMili/1000)+ " seconds.", Reporter.WARNING);
					GeneralUtils.startLevel("Missing Events:");
					for(Triple<String, String, String> eventToFollow : this.tmpEventListToFollow){
						report.report("Event Type: " + eventToFollow.getLeftElement() + 
								", Source Type: " + eventToFollow.getMiddleElement() + 
								", Event Info contains: " + eventToFollow.getRightElement(), Reporter.WARNING);
					}
					GeneralUtils.stopLevel();
				}
				GeneralUtils.stopLevel();
				this.state = WatchdogState.PRINTED;
				return true;
			}else{
				return false;
			}
		}

		@Override
		public int getExecutionDelaySec() {
			return 5;
		}

		public WatchdogState getState() {
			return this.state;
		}
	}
	
	class WaitForPtrRequest extends Utils.WatchDog.Command{
		private WatchDogManager wd;
		private EnodeBWithDonor eNodeBWithDonor;
		private WatchdogState state;
		private String stageName;
		private Triple<Boolean, String, String> result;
		private long rebootTime;
		private int timelineStageIndex;
		private long stageExpectedDuration;
		
		WaitForPtrRequest(String stageName, EnodeBWithDonor eNodeBWithDonor, long rebootTime){
			 this.wd = WatchDogManager.getInstance();
			 this.eNodeBWithDonor = eNodeBWithDonor;
			 this.stageName = stageName;
			 this.rebootTime = rebootTime;
			 this.result = new Triple<Boolean, String, String>(false, "", "");
			 this.state = WatchdogState.INITIALIZED;
		}
		
		public void startWaiting(int timelineStageIndex, long stageExpectedDuration){
			this.timelineStageIndex = timelineStageIndex;
			this.stageExpectedDuration = stageExpectedDuration;
			wd.addCommand(this, 1);
			this.state = WatchdogState.RUNNING;
		}
		
		@Override
		public void run() {
			this.result = checkIfPtrRequestReceived(this.eNodeBWithDonor);
			this.state = WatchdogState.ENDED;
		}

		@Override
		public int getExecutionDelaySec() {
			return 5;
		}
		
		private Triple<Boolean, String, String> checkIfPtrRequestReceived(EnodeBWithDonor eNodeBWithDonor) {
			String relayWanIpAddress = eNodeBWithDonor.getRelay().getWanIpAddress();
			DNS dns = DNS.getInstance();
			Pair<Boolean, String> dnsResult = new Pair<Boolean, String>(false, "");
			
			dnsResult = dns.isDnsPtrRequestReceived(relayWanIpAddress);
			if(dnsResult.getElement0()){
				saveStageTimeForHtmlTable(this.timelineStageIndex, this.stageName, this.rebootTime, System.currentTimeMillis(), stageExpectedDuration, true);
			}else{
				saveStageTimeForHtmlTable(this.timelineStageIndex, this.stageName, this.rebootTime, GeneralUtils.ERROR_VALUE, stageExpectedDuration, false);
			}
			
			Triple<Boolean, String, String> result =  Triple.createTriple(dnsResult.getElement0(), dnsResult.getElement1(),
					isEnodeBWithDonor ? sendCommandToNinjaViaDebugPort((EnodeBWithDonor)eNodeBWithDonor, "db get DhcpInfo") : "");
			
			return result;
		}
		
		public boolean printStatus(String appendixX){
			if(this.state == WatchdogState.ENDED){
				GeneralUtils.startLevel(appendixX + ": " + stageName);
				if(this.result.getLeftElement()){
					report.report("DNS Ptr Request Was Received.");
				}else{
					report.report("DNS Ptr Request Was NOT Received.", Reporter.WARNING);
				}
				GeneralUtils.reportHtmlLink("DNS response.", this.result.getMiddleElement());
				GeneralUtils.reportHtmlLink("db get DhcpInfo", result.getRightElement());
				GeneralUtils.stopLevel();
				this.state = WatchdogState.PRINTED;
				return true;
			}else{
				return false;
			}
		}
		
		public WatchdogState getState() {
			return this.state;
		}
	}
	
	class WatchAllRunningTimeout extends Utils.WatchDog.Command{
		private WatchDogManager wd;
		private long rebootTime;
		private long timeoutInMili;
		private EnodeB eNodeB;
		private WatchdogState state;
		private boolean isReachedToAllRunningBeforeTimeout;
		
		WatchAllRunningTimeout(EnodeB eNodeB){
			 this.wd = WatchDogManager.getInstance();
			 this.eNodeB = eNodeB;
			 this.state = WatchdogState.INITIALIZED;
			 this.isReachedToAllRunningBeforeTimeout = false;
		}
		
		public void startCounting(long startTime, long timeoutInMili){
			this.rebootTime = startTime;
			this.timeoutInMili = timeoutInMili;
			wd.addCommand(this);
			this.state = WatchdogState.RUNNING;
		}
		
		public void stopCounting(){
			this.state = WatchdogState.ENDED;
			wd.removeCommand(this);
			if((System.currentTimeMillis() - rebootTime) < timeoutInMili){
				GeneralUtils.printToConsole("WatchAllRunningTimeout.stopCounting()-> eNodeB is" + (eNodeB.isInOperationalStatus() ? "" : "not") + " in operational status");
				if(eNodeB.isInOperationalStatus()){
					this.isReachedToAllRunningBeforeTimeout = true;
				}else{
					this.isReachedToAllRunningBeforeTimeout = false;
				}
			}
		}
		
		@Override
		public void run() {
			GeneralUtils.printToConsole("WatchAllRunningTimeout.run()");
			if(this.state == WatchdogState.RUNNING && ((System.currentTimeMillis() - rebootTime) >= timeoutInMili)){
				this.state = WatchdogState.ENDED;
				GeneralUtils.printToConsole("WatchAllRunningTimeout.run() - Timeout Expired, eNodeB is" + (eNodeB.isInOperationalStatus() ? "" : "not") + " in operational status");
				if(eNodeB.isInOperationalStatus()){
					this.isReachedToAllRunningBeforeTimeout = true;
				}else{
					this.isReachedToAllRunningBeforeTimeout = false;
				}
				stopCounting();
			}
		}

		@Override
		public int getExecutionDelaySec() {
			return 30;
		}
		
		public void printStatus(){
			if(this.state == WatchdogState.ENDED){
				if(this.isReachedToAllRunningBeforeTimeout == false){
					report.report("The eNodeB failed to reach ALL RUNNING within " + (timeoutInMili/(1000 * 60)) + " minutes.", Reporter.FAIL);
					reason = "The eNodeB did failed to reach ALL RUNNING within " + (timeoutInMili/(1000 * 60)) + " minutes.";
					this.state = WatchdogState.PRINTED;
				}
			}
		}
	}
	
	class HtmlTimelineStage{
		public final String stageName;
		public final long rebootTimeInMili;
		public long stageFinishedTimeInMili;
		public final long stageExpectedDuration;
		public boolean isStageCompleted;
		
		HtmlTimelineStage(String stageName, long rebootTimeInMili, long stageFinishedTimeInMili, long stageExpectedDuration, boolean isStageCompleted){
			this.stageName = stageName;
			this.rebootTimeInMili = rebootTimeInMili;
			this.stageFinishedTimeInMili = stageFinishedTimeInMili;
			this.stageExpectedDuration = stageExpectedDuration;
			this.isStageCompleted = isStageCompleted;
		}
	}
	
	/***************** Parallel Commands ********************/
	
	private void startParallelCommands(EnodeB eNodeB){
		report.report("Starting parallel commands");
		List<String> commandList = new ArrayList<String>();
		if(isEnodeBWithDonor){
			commandList.add("!ip r s");
		}
		commandList.add("cell show operationalStatus");
		commandList.add("system show compilationtime");
		commandList.add("db get PTPStatus");
		
		List<String> donorCommandList = new ArrayList<String>();
		donorCommandList.add("cell show operationalStatus");
		donorCommandList.add("system show compilationtime");
		donorCommandList.add("ue show link");
		
		try {
			syncCommands = new ParallelCommandsThread(commandList, eNodeB, donorCommandList, null);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		syncCommands.start();
	}
	
	private void stopParallelCommands() {
		report.report("Stopping Parallel Commands");
		try {
			syncCommands.stopCommands();
			syncCommands.moveFileToReporterAndAddLink();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
}

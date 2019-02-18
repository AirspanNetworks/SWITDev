package testsNG.Actions.Utils;

import EnodeB.Components.XLP.XLP;
import EnodeB.EnodeB;
import Netspan.API.Lte.EventInfo;
import Utils.GeneralUtils;
import jsystem.framework.report.ListenerstManager;
import jsystem.framework.report.Reporter;
import testsNG.Actions.AlarmsAndEvents;

import java.util.Date;
import java.util.List;

/**
 * EnodebSwStatus - This object organizes all the relevant details of EnodeB, while updating SW version
 */
public class EnodebSwStatus {

	/**
	 * The enodeB that called for this thread
	 */
	private Reporter report = ListenerstManager.getInstance();

	public final String[] swUpgradeEventInfoList = new String[]{"Download in progress", "Download completed",
			"Activate in progress", "Activate completed"};

	//todo add getters and setters
	private EnodeB eNodeB;
	private XLP.SwStatus swStatus;
	private Boolean isSwDownloadCompleted;
	private boolean isInRunningState;
	private int receivedEventIndex;
	private int numberOfExpectedReboots;
	private int numberOfActualReboot;
	//todo - I didn't find uusage for this argument
	private String targetVersion;
	private String relayTargetVersion;
	private boolean isTargetEqualRunning;
	private boolean isTargetEqualStandby;
	private boolean isUpgradeRequired;

	private AlarmsAndEvents alarmsAndEvents;

	/**
	 * EnodebSwStatus Constructor
	 *
	 * @param eNodeB - eNodeB Object
	 */
	public EnodebSwStatus(EnodeB eNodeB) {
		this.eNodeB = eNodeB;
		this.swStatus = XLP.SwStatus.SW_STATUS_IDLE;
		this.isSwDownloadCompleted = false;
		this.isInRunningState = false;
		this.isTargetEqualRunning = false;
		this.isUpgradeRequired = false;
		this.receivedEventIndex = 0;
		this.numberOfActualReboot = 0;
		this.numberOfExpectedReboots = 0;
		this.alarmsAndEvents = AlarmsAndEvents.getInstance();
	}

	//Setters

	public void setSwStatus(XLP.SwStatus swStatus) {
		this.swStatus = swStatus;
	}

	public void setSwDownloadCompleted(Boolean swDownloadCompleted) { isSwDownloadCompleted = swDownloadCompleted; }

	public void setInRunningState(boolean inRunningState) {
		isInRunningState = inRunningState;
	}

	public void setReceivedEventIndex(int receivedEventIndex) {
		this.receivedEventIndex = receivedEventIndex;
	}

	public void setNumberOfExpectedReboots(int numberOfExpectedReboots) { this.numberOfExpectedReboots = numberOfExpectedReboots; }

	public void setTargetEqualRunning(boolean targetEqualRunning) { isTargetEqualRunning = targetEqualRunning; }

	public void setTargetEqualStandby(boolean targetEqualStandby) { isTargetEqualStandby = targetEqualStandby; }

	public void setUpgradeRequired(boolean upgradeRequired) {
		isUpgradeRequired = upgradeRequired;
	}

	public void setAlarmsAndEvents(AlarmsAndEvents alarmsAndEvents) {
		this.alarmsAndEvents = alarmsAndEvents;
	}

	public void setRelayTargetVersion(String realyTargetVersion) {
		this.relayTargetVersion = realyTargetVersion;
	}

	public void setTargetVersion(String targetVersion) {
		this.targetVersion = targetVersion;
	}

	//Getters

	public EnodeB geteNodeB() {
		return eNodeB;
	}

	public XLP.SwStatus getSwStatus() {
		return swStatus;
	}

	public Boolean isSwDownloadCompleted() { return isSwDownloadCompleted; }

	public boolean isInRunningState() {
		return isInRunningState;
	}

	public int getReceivedEventIndex() {
		return receivedEventIndex;
	}

	public int getNumberOfExpectedReboots() {
		return numberOfExpectedReboots;
	}

	public int getNumberOfActualReboot() {
		return numberOfActualReboot;
	}

	public boolean isTargetEqualRunning() { return isTargetEqualRunning; }

	public boolean isTargetEqualStandby() { return isTargetEqualStandby; }

	public boolean isUpgradeRequired() {
		return isUpgradeRequired;
	}

	public AlarmsAndEvents getAlarmsAndEvents() {
		return alarmsAndEvents;
	}

	public String getRelayTargetVersion() {
		return relayTargetVersion;
	}

	/**
	 * Loop on all the received events from netspan and check if we got the expected 4 events.
	 * + Update the receivedEventIndex parameter in order to understand how many events (from the relevant 4) we received.
	 *
	 * @param softwareActivateStartTimeInDate - softwareActivateStartTimeInDate
	 */
	public void reportUploadedNetspanEvent(Date softwareActivateStartTimeInDate) {
		List<EventInfo> eventInfoListFromNMS = alarmsAndEvents.getAllEventsNode(geteNodeB(),
				softwareActivateStartTimeInDate, new Date(System.currentTimeMillis()));
		for (EventInfo eventInfo : eventInfoListFromNMS) {
			if (swUpgradeEventInfoList.length > receivedEventIndex) {
				if (eventInfo.getEventInfo().contains(swUpgradeEventInfoList[receivedEventIndex])) {
					GeneralUtils.startLevel(geteNodeB().getName() + "-" + eventInfo.getSourceType() + ": "
							+ swUpgradeEventInfoList[receivedEventIndex]);
					report.report("Event Type: " + eventInfo.getEventType());
					report.report("Source Type: " + eventInfo.getSourceType());
					report.report("Event Info: " + eventInfo.getEventInfo());
					report.report("Received Time: " + eventInfo.getReceivedTime().toString());
					GeneralUtils.stopLevel();
					receivedEventIndex++;
					break;
				}
			}
		}
	}

	public void reportUploadedAllNetspanEvents(Date softwareActivateStartTimeInDate) {
		for (int i = 1; i <= swUpgradeEventInfoList.length; i++) {
			reportUploadedNetspanEvent(softwareActivateStartTimeInDate);
		}
	}

	public void increaseNumberOfExpectedReboots() {
		numberOfExpectedReboots++;
	}

	public void increaseNumberOfReboots() {
		numberOfActualReboot++;
	}
}
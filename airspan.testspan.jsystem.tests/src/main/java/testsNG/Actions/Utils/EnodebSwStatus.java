package testsNG.Actions.Utils;

import EnodeB.Components.XLP.XLP;
import EnodeB.EnodeB;
import testsNG.Actions.AlarmsAndEvents;

/**
 * EnodebSwStatus - This object organizes all the relevant details of EnodeB, while updating SW version
 */
public class EnodebSwStatus {

	//todo add getters and setters
	private EnodeB eNodeB;
	private XLP.SwStatus swStatus;
	private Boolean isSwDownloadCompleted;
	private boolean isInRunningState;
	private int numberOfExpectedReboots;
	private int numberOfActualReboot;
	//todo - I didn't find uusage for this argument
	private String targetVersion;
	private String relayTargetVersion;
	private boolean isTargetEqualRunning;
	private boolean isTargetEqualStandby;
	private boolean isUpgradeRequired;
	private ReceivedEvent receivedEvent;


	/**
	 * EnodebSwStatus Constructor
	 *
	 * @param eNodeB - eNodeB Object
	 */
	public EnodebSwStatus(EnodeB eNodeB) {
		this.receivedEvent = new ReceivedEvent();
		this.eNodeB = eNodeB;
		this.swStatus = XLP.SwStatus.SW_STATUS_IDLE;
		this.isSwDownloadCompleted = false;
		this.isInRunningState = false;
		this.isTargetEqualRunning = false;
		this.isUpgradeRequired = false;
		this.numberOfActualReboot = 0;
		this.numberOfExpectedReboots = 0;
	}

	//Setters

	public void setSwStatus(XLP.SwStatus swStatus) {
		this.swStatus = swStatus;
	}

	public void setSwDownloadCompleted(Boolean swDownloadCompleted) {
		isSwDownloadCompleted = swDownloadCompleted;
	}

	public void setInRunningState(boolean inRunningState) {
		isInRunningState = inRunningState;
	}

	public void setNumberOfExpectedReboots(int numberOfExpectedReboots) {
		this.numberOfExpectedReboots = numberOfExpectedReboots;
	}

	public void setTargetEqualRunning(boolean targetEqualRunning) {
		isTargetEqualRunning = targetEqualRunning;
	}

	public void setTargetEqualStandby(boolean targetEqualStandby) {
		isTargetEqualStandby = targetEqualStandby;
	}

	public void setUpgradeRequired(boolean upgradeRequired) {
		isUpgradeRequired = upgradeRequired;
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

	public Boolean isSwDownloadCompleted() {
		return isSwDownloadCompleted;
	}

	public boolean isInRunningState() {
		return isInRunningState;
	}

	public int getNumberOfExpectedReboots() {
		return numberOfExpectedReboots;
	}

	public int getNumberOfActualReboot() {
		return numberOfActualReboot;
	}

	public boolean isTargetEqualRunning() {
		return isTargetEqualRunning;
	}

	public boolean isTargetEqualStandby() {
		return isTargetEqualStandby;
	}

	public boolean isUpgradeRequired() {
		return isUpgradeRequired;
	}

	public String getRelayTargetVersion() {
		return relayTargetVersion;
	}

	public ReceivedEvent getReceivedEvent(){
		return receivedEvent;
	}

	public void increaseNumberOfExpectedReboots() {
		numberOfExpectedReboots++;
	}

	public void increaseNumberOfReboots() {
		numberOfActualReboot++;
	}

	/**
	 * Inner Class
	 * Organizes the received Events for the current EnB SW object.
	 */
	public class ReceivedEvent {
		public boolean downloadProgress;
		public boolean downloadCompleted;
		public boolean activateProgress;
		public boolean activateCompleted;

		/**
		 * Received Event constructor, initial all the events to false at the beginning.
		 */
		private ReceivedEvent() {
			this.downloadProgress = false;
			this.downloadCompleted = false;
			this.activateProgress = false;
			this.activateCompleted = false;
		}
	}
}
package testsNG.Actions.Utils;

import EnodeB.Components.XLP.XLP;
import EnodeB.EnodeB;

/**
 * EnodebSwStatus - This object organizes all the relevant details of EnodeB, while updating SW version
 */
public class EnodebSwStatus {

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
	private NetspanSWEvents receivedEvents;

	/**
	 * EnodebSwStatus Constructor
	 *
	 * @param eNodeB - eNodeB Object
	 */
	public EnodebSwStatus(EnodeB eNodeB) {
		this.receivedEvents = new NetspanSWEvents();
		this.eNodeB = eNodeB;
		this.swStatus = XLP.SwStatus.SW_STATUS_IDLE;
		this.isSwDownloadCompleted = false;
		this.isInRunningState = false;
		this.isTargetEqualRunning = false;
		this.numberOfActualReboot = 0;
		this.numberOfExpectedReboots = 0;
	}

	//Setters

	public void setSwStatus(XLP.SwStatus swStatus) {
		this.swStatus = swStatus;
	}

	public void setSwDownloadCompleted(Boolean swDownloadCompleted) { isSwDownloadCompleted = swDownloadCompleted; }

	public void setInRunningState(boolean inRunningState) {
		isInRunningState = inRunningState;
	}

	public void setNumberOfExpectedReboots(int numberOfExpectedReboots) { this.numberOfExpectedReboots = numberOfExpectedReboots; }

	public void setTargetEqualRunning(boolean targetEqualRunning) { isTargetEqualRunning = targetEqualRunning; }

	public void setTargetEqualStandby(boolean targetEqualStandby) { isTargetEqualStandby = targetEqualStandby; }

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

	public int getNumberOfExpectedReboots() {
		return numberOfExpectedReboots;
	}

	public int getNumberOfActualReboot() {
		return numberOfActualReboot;
	}

	public boolean isTargetEqualRunning() { return isTargetEqualRunning; }

	public boolean isTargetEqualStandby() { return isTargetEqualStandby; }

	public String getRelayTargetVersion() {
		return relayTargetVersion;
	}

	public void increaseNumberOfExpectedReboots() {
		numberOfExpectedReboots++;
	}

	public void increaseNumberOfReboots() {
		numberOfActualReboot++;
	}

	public NetspanSWEvents getReceivedEvent(){
		return receivedEvents;
	}
}
package Netspan.Profiles;

import java.util.ArrayList;
import java.util.List;

import Netspan.EnbProfiles;
import Netspan.API.Enums.AnnounceRate;
import Netspan.API.Enums.DelayRate;
import Netspan.API.Enums.SyncRate;

public class SyncParameters implements INetspanProfile{
	String profileName = null;
	String primaryClockSource = null;
	String primaryMasterIpAddress = null;
	String secondaryMasterIpAddress = null;
	Integer primaryMasterDomain = null;
	Integer secondaryMasterDomain = null;
	String ptpProfileType = null;
	AnnounceRate announceRateInMsgPerSec = null;
	SyncRate syncRateInMsgPerSec = null;
	DelayRate delayRequestResponseRateInMsgPerSec = null;
	Integer leaseDurationInSec = null;
	Boolean NlmIntraFrequencyScanMode = null;
	boolean NlmScanAllBands = true;
	ArrayList<Integer> NlmIntraFrequencyScanList;
	int NlmSyncSonInformationRequestPeriodicity;
	


	
 	public SyncParameters(String primaryIp, String secondaryIp, Integer primaryDomain, Integer secondaryDomain,
 			AnnounceRate announceRate, SyncRate syncRate, DelayRate delay, Integer lease) {
 		this.primaryMasterIpAddress = primaryIp;
 		this.secondaryMasterIpAddress = secondaryIp;
 		this.primaryMasterDomain = primaryDomain;
 		this.secondaryMasterDomain = secondaryDomain;
 		this.announceRateInMsgPerSec = announceRate;
 		this.syncRateInMsgPerSec = syncRate;
 		this.delayRequestResponseRateInMsgPerSec = delay;
 		this.leaseDurationInSec = lease;
 	}
 	
	public String getPtpProfileType() {
		return ptpProfileType;
	}

	public void setPtpProfileType(String ptpProfileType) {
		this.ptpProfileType = ptpProfileType;
	}

	public String getPrimaryClockSource() {
		return primaryClockSource;
	}

	public void setPrimaryClockSource(String primaryClockSource) {
		this.primaryClockSource = primaryClockSource;
	}

	public Integer getSecondaryMasterDomain() {
		return secondaryMasterDomain;
	}

	public void setSecondaryMasterDomain(Integer secondaryMasterDomain) {
		this.secondaryMasterDomain = secondaryMasterDomain;
	}

	public AnnounceRate getAnnounceRateInMsgPerSec() {
		return announceRateInMsgPerSec;
	}

	public void setAnnounceRateInMsgPerSec(AnnounceRate announceRateInMsgPerSec) {
		this.announceRateInMsgPerSec = announceRateInMsgPerSec;
	}

	public SyncRate getSyncRateInMsgPerSec() {
		return syncRateInMsgPerSec;
	}

	public void setSyncRateInMsgPerSec(SyncRate syncRateInMsgPerSec) {
		this.syncRateInMsgPerSec = syncRateInMsgPerSec;
	}

	public DelayRate getDelayRequestResponseRateInMsgPerSec() {
		return delayRequestResponseRateInMsgPerSec;
	}

	public void setDelayRequestResponseRateInMsgPerSec(DelayRate delayRequestResponseRateInMsgPerSec) {
		this.delayRequestResponseRateInMsgPerSec = delayRequestResponseRateInMsgPerSec;
	}

	public Integer getLeaseDurationInSec() {
		return leaseDurationInSec;
	}

	public void setLeaseDurationInSec(Integer leaseDurationInSec) {
		this.leaseDurationInSec = leaseDurationInSec;
	}

	public Integer getPrimaryMasterDomain() {
		return primaryMasterDomain;
	}

	public void setPrimaryMasterDomain(Integer primaryMasterDomain) {
		this.primaryMasterDomain = primaryMasterDomain;
	}

	SyncParameters(String profileName) {
		if (profileName == null) {
			throw new NullPointerException();
		}

		this.profileName = profileName;
	}

	public String getProfileName() {
		return profileName;
	}

	public void setProfileName(String profileName) {
		if (profileName == null) {
			throw new NullPointerException();
		}

		this.profileName = profileName;
	}

	public String getPrimaryMasterIpAddress() {
		return primaryMasterIpAddress;
	}

	public void setPrimaryMasterIpAddress(String primaryMasterIpAddress) {
		this.primaryMasterIpAddress = primaryMasterIpAddress;
	}

	public String getSecondaryMasterIpAddress() {
		return secondaryMasterIpAddress;
	}

	public void setSecondaryMasterIpAddress(String secondaryMasterIpAddress) {
		this.secondaryMasterIpAddress = secondaryMasterIpAddress;
	}

	public Boolean getNlmIntraFrequencyScanMode() {
		return NlmIntraFrequencyScanMode;
	}

	public void setNlmIntraFrequencyScanMode(Boolean nlmIntraFrequencyScanMode) {
		NlmIntraFrequencyScanMode = nlmIntraFrequencyScanMode;
	}

	public boolean isNlmScanAllBands() {
		return NlmScanAllBands;
	}

	public void setNlmScanAllBands(boolean nlmScanAllBands) {
		NlmScanAllBands = nlmScanAllBands;
	}
	
	public List<Integer> getNlmIntraFrequencyScanList() {
		return NlmIntraFrequencyScanList;
	}

	public void setNlmIntraFrequencyScanList(ArrayList<Integer> nlmIntraFrequencyScanList) {
		NlmIntraFrequencyScanList = nlmIntraFrequencyScanList;
	}
	
	public int getNlmSyncSonInformationRequestPeriodicity() {
		return NlmSyncSonInformationRequestPeriodicity;
	}

	public void setNlmSyncSonInformationRequestPeriodicity(int nlmSyncSonInformationRequestPeriodicity) {
		NlmSyncSonInformationRequestPeriodicity = nlmSyncSonInformationRequestPeriodicity;
	}

	public SyncParameters() {
	}
	
	@Override
	public String toString() {
		return "SyncParameters [profileName=" + profileName + ", primaryMasterIpAddress=" + primaryMasterIpAddress
				+ ", secondaryMasterIpAddress=" + secondaryMasterIpAddress + "]";
	}
	@Override
	public EnbProfiles getType() {
		return EnbProfiles.Sync_Profile;
	}
}

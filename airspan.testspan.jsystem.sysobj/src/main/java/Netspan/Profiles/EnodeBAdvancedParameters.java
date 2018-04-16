package Netspan.Profiles;

import Netspan.EnbProfiles;
import Netspan.API.Enums.EnabledDisabledStates;

public class EnodeBAdvancedParameters implements INetspanProfile{
	private String profileName = null;
	private Integer lowPower = null; //AirSON->Auto PCI
	private Integer powerStep = null; //AirSON->Auto PCI
	private Integer powerLevelTimeInterval = null; //AirSON->Auto PCI
	private Integer anrTimer = null; //AirSON->Auto PCI
	private EnabledDisabledStates pciConfusionAllowed = null; //AirSON->Auto PCI
	private Integer initialPciListSize = null; //AirSON->Auto PCI
	private Integer startPCI = null; //NLM->PCI start
	private Integer stopPCI = null; //NLM-> PCI stop
	private Integer RSRPTreshholdForNLSync = null; //NLM-> RSRP Threshold for NL Synchronization
	private Integer SonInformationRequestPeriodicly = null;
	
	

	public String getProfileName() {
		return profileName;
	}
	public void setProfileName(String profileName) {
		this.profileName = profileName;
	}
	public Integer getLowPower() {
		return lowPower;
	}
	public void setLowPower(Integer lowPower) {
		this.lowPower = lowPower;
	}
	public Integer getPowerStep() {
		return powerStep;
	}
	public void setPowerStep(Integer powerStep) {
		this.powerStep = powerStep;
	}
	public Integer getPowerLevelTimeInterval() {
		return powerLevelTimeInterval;
	}
	public void setPowerLevelTimeInterval(Integer powerLevelTimeInterval) {
		this.powerLevelTimeInterval = powerLevelTimeInterval;
	}
	public Integer getAnrTimer() {
		return anrTimer;
	}
	public void setAnrTimer(Integer anrTimer) {
		this.anrTimer = anrTimer;
	}
	public EnabledDisabledStates getPciConfusionAllowed() {
		return pciConfusionAllowed;
	}
	public void setPciConfusionAllowed(EnabledDisabledStates pciConfusionAllowed) {
		this.pciConfusionAllowed = pciConfusionAllowed;
	}
	public Integer getInitialPciListSize() {
		return initialPciListSize;
	}
	public void setInitialPciListSize(Integer initialPciListSize) {
		this.initialPciListSize = initialPciListSize;
	}
	@Override
	public EnbProfiles getType() {
		return EnbProfiles.EnodeB_Advanced_Profile;
	}
	public Integer getStartPCI() {
		return startPCI;
	}
	public void setStartPCI(Integer startPCI) {
		this.startPCI = startPCI;
	}
	public Integer getStopPCI() {
		return stopPCI;
	}
	public void setStopPCI(Integer stopPCI) {
		this.stopPCI = stopPCI;
	}
	public Integer getRSRPTreshholdForNLSync() {
		return RSRPTreshholdForNLSync;
	}
	public void setRSRPTreshholdForNLSync(Integer rSRPTreshholdForNLSync) {
		RSRPTreshholdForNLSync = rSRPTreshholdForNLSync;
	}
	public Integer getSonInformationRequestPeriodicly() {
		return SonInformationRequestPeriodicly;
	}
	
	public void setSonInformationRequestPeriodicly(Integer sonInformationRequestPeriodicly) {
		SonInformationRequestPeriodicly = sonInformationRequestPeriodicly;
	}
}

package Netspan.Profiles;

import Netspan.EnbProfiles;
import Netspan.NBI_15_5.Lte.AutonomousRebootValues;

public class ManagementParameters implements INetspanProfile{
	public Boolean maintenanceWindow;
	public String profileName;
	public String timeZone;
	public String maintenanceWindowStartTime; //HH:MM
	public String maintenanceWindowEndTime;
	public Integer maxRandomDelayPercent;
	public AutonomousRebootValues autonomousReboot;
	@Override
	public EnbProfiles getType() {
		return EnbProfiles.Management_Profile;
	}
	
	public String getProfileName() {
		return profileName;
	}
	public void setProfileName(String profileName) {
		this.profileName = profileName;
	}
	
}

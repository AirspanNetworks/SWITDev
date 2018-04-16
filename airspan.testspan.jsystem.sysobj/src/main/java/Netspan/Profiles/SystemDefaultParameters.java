package Netspan.Profiles;

import Netspan.EnbProfiles;

public class SystemDefaultParameters implements INetspanProfile{
	public String profileName;

	public String getProfileName() {
		return profileName;
	}

	public void setProfileName(String profileName) {
		this.profileName = profileName;
	}
	
	@Override
	public EnbProfiles getType() {
		return EnbProfiles.System_Default_Profile;
	}

}

package Netspan.Profiles;

import Netspan.EnbProfiles;

public interface INetspanProfile {
	public EnbProfiles getType();
	
	public String getProfileName();
	public void setProfileName(String profileName);
}

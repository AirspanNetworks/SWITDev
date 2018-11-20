package Netspan.Profiles;

import Netspan.EnbProfiles;

public interface INetspanProfile {
	EnbProfiles getType();
	
	String getProfileName();
	void setProfileName(String profileName);
}

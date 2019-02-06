package Netspan.Profiles;

import Netspan.EnbProfiles;
import Netspan.API.Enums.EnabledDisabledStates;

public class CellAdvancedParameters implements INetspanProfile{
	private String profileName = null;	
	private Integer sib10Duration = null;
	private Integer sib11Duration = null;
	private EnabledDisabledStates etwsUserMode = null;
	
	public Integer getSib10Duration() {
		return sib10Duration;
	}
	public void setSib10Duration(Integer sib10Duration) {
		this.sib10Duration = sib10Duration;
	}
	public Integer getSib11Duration() {
		return sib11Duration;
	}
	public void setSib11Duration(Integer sib11Duration) {
		this.sib11Duration = sib11Duration;
	}
	public EnabledDisabledStates getEtwsUserMode() {
		return etwsUserMode;
	}
	public void setEtwsUserMode(EnabledDisabledStates etwsUserMode) {
		this.etwsUserMode = etwsUserMode;
	}
	public String getProfileName() {
		return profileName;
	}
	public void setProfileName(String profileName) {
		this.profileName = profileName;
	}
	
	@Override
	public EnbProfiles getType() {
		return EnbProfiles.Cell_Advanced_Profile;
	}
}

package Netspan.Profiles;

import Netspan.EnbProfiles;
import Netspan.API.Enums.SecurityProfileOptionalOrMandatory;

public class SecurityParameters implements INetspanProfile{
	private String profileName;
	private SecurityProfileOptionalOrMandatory integrityMode;
	private int integrityNullLevel;
	private int integrityAESLevel;
	private int integritySNOWLevel;
	
	private SecurityProfileOptionalOrMandatory cipheringMode;
	private int cipheringNullLevel;
	private int cipheringAESLevel;
	private int cipheringSNOWLevel;
	
	public SecurityParameters(){}

	public SecurityParameters(String profileName, SecurityProfileOptionalOrMandatory integrityModeProfile,
			int integrityNullLevelPrfl,int integrityAESLevelPrfl,int integritySNOWLevelPrfl,
			SecurityProfileOptionalOrMandatory cipheringModeProfile, int cipheringNullLevelPrfl,
			int cipheringAESLevelPrfl,int cipheringSNOWLevelPrfl) throws NullPointerException{
	
		if(profileName == null) {throw new NullPointerException();}
		
		this.profileName = profileName;
		
		this.integrityMode = integrityModeProfile;
		this.integrityNullLevel = integrityNullLevelPrfl;
		this.integrityAESLevel =integrityAESLevelPrfl;
		this.integritySNOWLevel =integritySNOWLevelPrfl;
		
		this.cipheringMode =cipheringModeProfile;
		this.cipheringNullLevel =cipheringNullLevelPrfl;
		this.cipheringAESLevel = cipheringAESLevelPrfl;
		this.cipheringSNOWLevel = cipheringSNOWLevelPrfl;
	}
	
	
	public String getProfileName() {
		return profileName;
	}




	public void setProfileName(String profileName) {
		this.profileName = profileName;
	}




	public SecurityProfileOptionalOrMandatory getIntegrityMode() {
		return integrityMode;
	}
	public void setIntegrityMode(SecurityProfileOptionalOrMandatory integrityMode) {
		this.integrityMode = integrityMode;
	}
	public int getIntegrityNullLevel() {
		return integrityNullLevel;
	}
	public void setIntegrityNullLevel(int integrityNullLevel) {
		this.integrityNullLevel = integrityNullLevel;
	}
	public int getIntegrityAESLevel() {
		return integrityAESLevel;
	}
	public void setIntegrityAESLevel(int integrityAESLevel) {
		this.integrityAESLevel = integrityAESLevel;
	}
	public int getIntegritySNOWLevel() {
		return integritySNOWLevel;
	}
	public void setIntegritySNOWLevel(int integritySNOWLevel) {
		this.integritySNOWLevel = integritySNOWLevel;
	}
	public SecurityProfileOptionalOrMandatory getCipheringMode() {
		return cipheringMode;
	}
	public void setCipheringMode(SecurityProfileOptionalOrMandatory cipheringMode) {
		this.cipheringMode = cipheringMode;
	}
	public int getCipheringNullLevel() {
		return cipheringNullLevel;
	}
	public void setCipheringNullLevel(int cipheringNullLevel) {
		this.cipheringNullLevel = cipheringNullLevel;
	}
	public int getCipheringAESLevel() {
		return cipheringAESLevel;
	}
	public void setCipheringAESLevel(int cipheringAESLevel) {
		this.cipheringAESLevel = cipheringAESLevel;
	}
	public int getCipheringSNOWLevel() {
		return cipheringSNOWLevel;
	}
	public void setCipheringSNOWLevel(int cipheringSNOWLevel) {
		this.cipheringSNOWLevel = cipheringSNOWLevel;
	}

	@Override
	public String toString() {
		return "SecurityParameters [profileName=" + profileName + ", integrityMode=" + integrityMode
				+ ", integrityNullLevel=" + integrityNullLevel + ", integrityAESLevel=" + integrityAESLevel
				+ ", integritySNOWLevel=" + integritySNOWLevel + ", cipheringMode=" + cipheringMode
				+ ", cipheringNullLevel=" + cipheringNullLevel + ", cipheringAESLevel=" + cipheringAESLevel
				+ ", cipheringSNOWLevel=" + cipheringSNOWLevel + "]";
	}
	
	@Override
	public EnbProfiles getType() {
		return EnbProfiles.Security_Profile;
	}	
}

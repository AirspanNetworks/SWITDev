package Netspan.Profiles;

import Netspan.API.Enums.CellBarringPolicies;

public class CellBarringPolicyParameters {
	public CellBarringPolicies cellBarringPolicy;
	public Boolean IsAccessClassBarred;
	public Boolean IsEmergencyAccessBarred;
	public Boolean IsSignalingAccessBarred;
	public Boolean IsDataAccessBarred;
	public AccessBarringAdvanced signalingAccessBarring;
	public AccessBarringAdvanced dataAccessBarring;
	
}

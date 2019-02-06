package Netspan.Profiles;

import Netspan.EnbProfiles;
import Netspan.API.Enums.EnabledDisabledStates;

public class MultiCellParameters implements INetspanProfile {
	
	private String profileName = null;
	private boolean carrierAggMode = false;
	private EnabledDisabledStates intraEnbLoadBalanceMode = null;
	private Integer compositeLoadDiffMax = null;
	private Integer compositeLoadDiffMin = null;
	private Integer calculationInterval = null;
	
	public boolean getCarrierAggMode() {
		return carrierAggMode;
	}

	public void setCarrierAggMode(boolean carrierAggMode) {
		this.carrierAggMode = carrierAggMode;
	}

	public EnabledDisabledStates getIntraEnbLoadBalanceMode() {
		return intraEnbLoadBalanceMode;
	}

	public void setIntraEnbLoadBalanceMode(EnabledDisabledStates intraEnbLoadBalanceMode) {
		this.intraEnbLoadBalanceMode = intraEnbLoadBalanceMode;
	}

	public Integer getCompositeLoadDiffMax() {
		return compositeLoadDiffMax;
	}

	public void setCompositeLoadDiffMax(Integer compositeLoadDiffMax) {
		this.compositeLoadDiffMax = compositeLoadDiffMax;
	}

	public Integer getCompositeLoadDiffMin() {
		return compositeLoadDiffMin;
	}

	public void setCompositeLoadDiffMin(Integer compositeLoadDiffMin) {
		this.compositeLoadDiffMin = compositeLoadDiffMin;
	}

	public Integer getCalculationInterval() {
		return calculationInterval;
	}

	public void setCalculationInterval(Integer calculationInterval) {
		this.calculationInterval = calculationInterval;
	}

	@Override
	public EnbProfiles getType() {
		return EnbProfiles.MultiCell_Profile; 
	}

	@Override
	public String getProfileName() {
		return profileName;
	}

	@Override
	public void setProfileName(String profileName) {
		this.profileName = profileName;
	}

}

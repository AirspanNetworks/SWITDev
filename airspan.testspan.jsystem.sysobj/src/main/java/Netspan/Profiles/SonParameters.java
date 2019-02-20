package Netspan.Profiles;

import java.util.ArrayList;
import java.util.List;

import Netspan.EnbProfiles;
import Netspan.API.Enums.DicicSchemeType;
import Netspan.API.Enums.EnabledStates;
import Netspan.API.Enums.PnpModes;
import Netspan.API.Enums.SonAnrStates;
import Netspan.API.Lte.LteSonDynIcic;
import Utils.Pair;
import Utils.Triple;

public class SonParameters implements INetspanProfile {

	private String profileName;
	private Boolean cSonEnabled;
	private PnpModes pnpMode;
	private Boolean autoPCIEnabled;

	public enum PciHandling {
		Immediate("Immediate"), Deferred("Deferred"), Alarm("Alarm");
		private final String value;

		PciHandling(String v) {
			value = v;
		}

		public String value() {
			return value;
		}

		public static PciHandling fromValue(String v) {
			PciHandling ph = null;
			switch (v) {
			case "Immediate":
				ph = Immediate;
				break;
			case "Deferred":
				ph = Deferred;
				break;
			case "Alarm":
				ph = Alarm;
				break;
			}
			return ph;
		}
	}

	private PciHandling pciCollisionHandling;
	private PciHandling pciConfusionHandling;
	private PciHandling pciPolicyViolationHandling;
	private List<Pair<Integer, Integer>> rangesList = new ArrayList<Pair<Integer, Integer>>();
	private SonAnrStates anrState;
	private List<Integer> anrFrequencyList = new ArrayList<Integer>();
	private Boolean isSonCommissioningEnabled;
	private Boolean cSonRachMode;
	private Boolean cSonMcimMode;
	private Boolean cSonMroMode;
	private Boolean cSonMlbMode;
	private Integer cSonCapacityClass;
	private Integer cSonPDSCHLoad;
	private Integer cSonPUSCHLoad;
	private Integer cSonRRCLoad;
	private Integer cSonCPUCHLoad;
	private Integer MinAllowedHoSuccessRate;
	private Boolean autoRSIEnabled;
	private List<Triple<Integer, Integer, Integer>> rangesRSIList = new ArrayList<Triple<Integer, Integer, Integer>>();
	private EnabledStates optimizationMode;
	private LteSonDynIcic dynamicIcic;
	private EnabledStates icicMode;
	private DicicSchemeType icicSchemeType;
	private Integer minThresholdCeu;
	private Integer maxThresholdCeu;
	private EnabledStates unmanagedInterferenceHandling;

	public Boolean getIsSonCommissioningEnabled() {
		return isSonCommissioningEnabled;
	}

	public void setIsSonCommissioningEnabled(Boolean isSonCommissioningEnabled) {
		this.isSonCommissioningEnabled = isSonCommissioningEnabled;
	}

	public EnabledStates getIcicMode() {
		return icicMode;
	}

	public void setIcicMode(EnabledStates icicMode) {
		this.icicMode = icicMode;
	}

	public DicicSchemeType getIcicSchemeType() {
		return icicSchemeType;
	}

	public void setIcicSchemeType(DicicSchemeType icicSchemeType) {
		this.icicSchemeType = icicSchemeType;
	}

	public Integer getMinThresholdCeu() {
		return minThresholdCeu;
	}

	public void setMinThresholdCeu(Integer minThresholdCeu) {
		this.minThresholdCeu = minThresholdCeu;
	}

	public Integer getMaxThresholdCeu() {
		return maxThresholdCeu;
	}

	public void setMaxThresholdCeu(Integer maxThresholdCeu) {
		this.maxThresholdCeu = maxThresholdCeu;
	}

	public EnabledStates getUnmanagedInterferenceHandling() {
		return unmanagedInterferenceHandling;
	}

	public void setUnmanagedInterferenceHandling(EnabledStates unmanagedInterferenceHandling) {
		this.unmanagedInterferenceHandling = unmanagedInterferenceHandling;
	}

	public PciHandling getPciConfusionHandling() {
		return pciConfusionHandling;
	}

	public void setPciConfusionHandling(PciHandling pciConfusionHandling) {
		this.pciConfusionHandling = pciConfusionHandling;
	}

	public PciHandling getPciPolicyViolationHandling() {
		return pciPolicyViolationHandling;
	}

	public void setPciPolicyViolationHandling(PciHandling pciPolicyViolationHandling) {
		this.pciPolicyViolationHandling = pciPolicyViolationHandling;
	}

	public PciHandling getPciCollisionHandling() {
		return pciCollisionHandling;
	}

	public void setPciCollisionHandling(PciHandling pciCollisionHandling) {
		this.pciCollisionHandling = pciCollisionHandling;
	}

	public Integer getcSonCapacityClass() {
		return cSonCapacityClass;
	}

	public void setcSonCapacityClass(Integer cSonCapacityClass) {
		this.cSonCapacityClass = cSonCapacityClass;
	}

	public Integer getcSonPUSCHLoad() {
		return cSonPUSCHLoad;
	}

	public void setcSonPUSCHLoad(Integer cSonPUSCHLoad) {
		this.cSonPUSCHLoad = cSonPUSCHLoad;
	}

	public Integer getcSonRRCLoad() {
		return cSonRRCLoad;
	}

	public void setcSonRRCLoad(Integer cSonRRCLoad) {
		this.cSonRRCLoad = cSonRRCLoad;
	}

	public Integer getcSonCPUCHLoad() {
		return cSonCPUCHLoad;
	}

	public void setcSonCPUCHLoad(Integer cSonCPUCHLoad) {
		this.cSonCPUCHLoad = cSonCPUCHLoad;
	}

	public Integer getMinAllowedHoSuccessRate() {
		return MinAllowedHoSuccessRate;
	}

	public void setMinAllowedHoSuccessRate(Integer MinAllowedHoSuccessRate) {
		this.MinAllowedHoSuccessRate = MinAllowedHoSuccessRate;
	}

	public Boolean iscSonRachMode() {
		return cSonRachMode;
	}

	public void setcSonRachMode(boolean cSonRachMode) {
		this.cSonRachMode = cSonRachMode;
	}

	public Boolean iscSonMcimMode() {
		return cSonMcimMode;
	}

	public void setcSonMcimMode(boolean cSonMcimMode) {
		this.cSonMcimMode = cSonMcimMode;
	}

	public Boolean iscSonMroMode() {
		return cSonMroMode;
	}

	public void setcSonMroMode(boolean cSonMroMode) {
		this.cSonMroMode = cSonMroMode;
	}

	public Boolean iscSonMlbMode() {
		return cSonMlbMode;
	}

	public void setcSonMlbMode(boolean cSonMlbMode) {
		this.cSonMlbMode = cSonMlbMode;
	}

	public SonParameters(String profileName) {
		if (profileName == null) {
			throw new NullPointerException();
		}

		this.profileName = profileName;
	}

	public SonParameters() {
	}

	public Boolean isSonCommissioning() {
		return isSonCommissioningEnabled;
	}

	public void setSonCommissioning(boolean value) {
		isSonCommissioningEnabled = value;
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

	public Boolean isCSonEnabled() {
		return cSonEnabled;
	}

	public void setCSonEnabled(boolean cSonEnabled) {
		this.cSonEnabled = cSonEnabled;
	}

	public PnpModes getPnpMode() {
		return pnpMode;
	}

	public void setPnpMode(PnpModes pnpMode) {
		this.pnpMode = pnpMode;
	}

	public Boolean isAutoPCIEnabled() {
		return autoPCIEnabled;
	}

	public void setAutoPCIEnabled(boolean autoPCIEnabled) {
		this.autoPCIEnabled = autoPCIEnabled;
	}

	public List<Pair<Integer, Integer>> getRangesList() {
		return rangesList;
	}

	public void setRangesList(List<Pair<Integer, Integer>> rangesList) {
		this.rangesList = rangesList;
	}

	public SonAnrStates getAnrState() {
		return anrState;
	}

	public void setAnrState(SonAnrStates anrState) {
		this.anrState = anrState;
	}

	public List<Integer> getAnrFrequencyList() {
		return anrFrequencyList;
	}

	public void setAnrFrequencyList(List<Integer> anrFrequencyList) {
		this.anrFrequencyList = anrFrequencyList;
	}

	@Override
	public String toString() {
		return "SonParameters [profileName=" + profileName + ", sonEnabled=" + cSonEnabled + ", pnpMode=" + pnpMode
				+ ", autoPCIEnabled=" + autoPCIEnabled + ", rangesList=" + rangesList + ", anrState=" + anrState
				+ ", anrFrequencyList=" + anrFrequencyList + "]";
	}

	public Integer getcSonPDSCHLoad() {
		return cSonPDSCHLoad;
	}

	public void setcSonPDSCHLoad(Integer cSonPDSCHLoad) {
		this.cSonPDSCHLoad = cSonPDSCHLoad;
	}

	@Override
	public EnbProfiles getType() {
		return EnbProfiles.Son_Profile;
	}

	public Boolean isAutoRSIEnabled() {
		return autoRSIEnabled;
	}

	public void setAutoRSIEnabled(Boolean autoRSIEnabled) {
		this.autoRSIEnabled = autoRSIEnabled;
	}

	public List<Triple<Integer, Integer, Integer>> getRangesRSIList() {
		return rangesRSIList;
	}

	public void setRangesRSIList(List<Triple<Integer, Integer, Integer>> rangesRSIList) {
		this.rangesRSIList = rangesRSIList;
	}

	public LteSonDynIcic getDynamicIcic() {
		return dynamicIcic;
	}

	public void setDynamicIcic(LteSonDynIcic dynamicIcic) {
		this.dynamicIcic = dynamicIcic;
	}

	public EnabledStates getOptimizationMode() {
		return optimizationMode;
	}

	public void setOptimizationMode(EnabledStates optimizationMode) {
		this.optimizationMode = optimizationMode;
	}
}

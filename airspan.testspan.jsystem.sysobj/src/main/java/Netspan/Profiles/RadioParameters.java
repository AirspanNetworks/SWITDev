package Netspan.Profiles;

import java.util.ArrayList;

import Netspan.EnbProfiles;
import Utils.GeneralUtils;

/**
 * @author sshahaf
 *
 */
public class RadioParameters implements INetspanProfile {
	String profileName = null;
	String duplex = null;
	String bandwidth = null;
	String frameConfig = null;
	String specialSubFrame = null;
	String cfi = null;
	Boolean carrierAggMode = null;
	Boolean intraEnbLoadBalance = null;
	Integer maxCompositeLoadDiff = null;
	Integer minCompositeLoadDiff = null;
	Integer calculationInterval = null;
	ArrayList<Integer> activeCells = null;
	public RadioParameters donorRadioParameters = null;

	// unique cell radio profile parameters
	Integer earfcn = null;
	Integer downLinkFrequency = null;
	Integer upLinkFrequency = null;
	Integer txpower = null;
	Integer band = null;
	String additionalSpectrumEmission = null;
	Boolean mfbi = null;
	// Resource management
	Boolean rmMode = null;
	Integer SFRSegment = null;
	Integer SFRIndex = null;
	String SFRThresholdType = null;
	Integer SFRThresholdValue = null; // max 56 , min 0
	Integer SFRMaxMCSCellEdge = null; // min 0, max 28
	Integer SFRMinMCSCellEdge = null; // max at 28, min at 0
	// Location Based Services
		Boolean ECIDMode = null;
		Integer ECIDProcedureTimer = null; // 10k at most
		// OTDOA MODE
		Boolean OTDOAMode = false;
		String subframes = null;
		PRSBandWidthEnum PRSBandWidth = null;
		PRSPeriodiclyEnum PRSPeriodicly = null;
		Integer PRSOffset = null;
		Integer PRSPowerOffset = null;
		PRSMutingPeriodiclyEnum PRSMutingPeriodicly = null;
		String PRSMutingPattern = null;

	public Boolean getOTDOAMode() {
		return OTDOAMode;
	}

	/**
	 * This will represent OTDOA Mode and must be set to true in order to work with other parameters regarding OTDOA.
	 */
	public void setOTDOAMode(Boolean oTDOAMode) {
		OTDOAMode = oTDOAMode;
	}

	public String getSubframes() {
		return subframes;
	}

	/**
	 * subframes comma based: 1,2,3,4,5
	 * @param subframes
	 */
	public void setSubframes(String subframes) {
		if(!subframes.contains(",") && (subframes.length() != 1)){
			GeneralUtils.printToConsole("Error with subframs Value! - default \"9\" ");
			this.subframes = "9";
			return;
		}
		this.subframes = subframes;
	}

	public PRSBandWidthEnum getPRSBandWidth() {
		if(PRSBandWidth == null){
			return PRSBandWidthEnum.SIX;
		}
		return PRSBandWidth;
	}

	public void setPRSBandWidth(PRSBandWidthEnum pRSBandWidth) {
		this.PRSBandWidth = pRSBandWidth;
	}

	public PRSPeriodiclyEnum getPRSPeriodicly() {
		return PRSPeriodicly;
	}

	public void setPRSPeriodicly(PRSPeriodiclyEnum pRSPeriodicly) {
		this.PRSPeriodicly = pRSPeriodicly;
	}

	public Integer getPRSOffset() {
		return PRSOffset;
	}

	public void setPRSOffset(Integer pRSOffset) {
		PRSOffset = pRSOffset;
	}

	public Integer getPRSPowerOffset() {
		return PRSPowerOffset;
	}

	public void setPRSPowerOffset(Integer pRSPowerOffset) {
		PRSPowerOffset = pRSPowerOffset;
	}

	public PRSMutingPeriodiclyEnum getPRSMutingPeriodicly() {
		return PRSMutingPeriodicly;
	}

	public void setPRSMutingPeriodicly(PRSMutingPeriodiclyEnum pRSMutingPeriodicly) {
		PRSMutingPeriodicly = pRSMutingPeriodicly;
	}

	public String getPRSMutingPattern() {
		return PRSMutingPattern;
	}

	public void setPRSMutingPattern(String pRSMutingPattern) {
		if(pRSMutingPattern.length() > 16){
			GeneralUtils.printToConsole("Error with PRES Muting Pattern - value is bigger than 16 bits, using default value \"11\" ");
			PRSMutingPattern = "11";
			return;
		}
		PRSMutingPattern = pRSMutingPattern;
	}

	public Integer getDownLinkFrequency() {
		return downLinkFrequency;
	}

	public void setDownLinkFrequency(Integer downLinkFrequency) {
		this.downLinkFrequency = downLinkFrequency;
	}

	public Integer getUpLinkFrequency() {
		return upLinkFrequency;
	}

	public void setUpLinkFrequency(Integer upLinkFrequency) {
		this.upLinkFrequency = upLinkFrequency;
	}
    
	public String getAdditionalSpectrumEmission() {
		return additionalSpectrumEmission;
	}

	public void setAdditionalSpectrumEmission(String additionalSpectrumEmission) {
		this.additionalSpectrumEmission = additionalSpectrumEmission;
	}

	public Boolean getRmMode() {
		return rmMode;
	}

	public void setRmMode(Boolean rmMode) {
		this.rmMode = rmMode;
	}

	public Integer getSFRSegment() {
		return SFRSegment;
	}

	public void setSFRSegment(Integer sFRSegment) {
		SFRSegment = sFRSegment;
	}

	public Integer getSFRIndex() {
		return SFRIndex;
	}

	public void setSFRIndex(Integer sFRIndex) {
		SFRIndex = sFRIndex;
	}

	public String getSFRThresholdType() {
		return SFRThresholdType;
	}

	public void setSFRThresholdType(String sFRThresholdType) {
		SFRThresholdType = sFRThresholdType;
	}

	public Integer getSFRThresholdValue() {
		return SFRThresholdValue;
	}

	public void setSFRThresholdValue(Integer sFRThresholdValue) {
		SFRThresholdValue = sFRThresholdValue;
	}

	public Integer getSFRMaxMCSCellEdge() {
		return SFRMaxMCSCellEdge;
	}

	public void setSFRMaxMCSCellEdge(Integer sFRMaxMCSCellEdge) {
		SFRMaxMCSCellEdge = sFRMaxMCSCellEdge;
	}

	public Integer getSFRMinMCSCellEdge() {
		return SFRMinMCSCellEdge;
	}

	public void setSFRMinMCSCellEdge(Integer sFRMinMCSCellEdge) {
		SFRMinMCSCellEdge = sFRMinMCSCellEdge;
	}

	public Boolean getECIDMode() {
		return ECIDMode;
	}

	public void setECIDMode(Boolean eCIDMode) {
		ECIDMode = eCIDMode;
	}

	public Integer getECIDProcedureTimer() {
		return ECIDProcedureTimer;
	}

	public void setECIDProcedureTimer(Integer eCIDProcedureTimer) {
		ECIDProcedureTimer = eCIDProcedureTimer;
	}

	public Integer getMaxCompositeLoadDiff() {
		return maxCompositeLoadDiff;
	}

	public void setMaxCompositeLoadDiff(Integer maxCompositeLoadDiff) throws Exception {
		if (maxCompositeLoadDiff <= 100 && maxCompositeLoadDiff >= 0) {
			this.maxCompositeLoadDiff = maxCompositeLoadDiff;
		} else {
			throw new Exception(
					"maxCompositeLoadDiff value is not in range 0-100, maxCompositeLoadDiff : " + maxCompositeLoadDiff);
		}
	}

	public Integer getMinCompositeLoadDiff() {
		return minCompositeLoadDiff;
	}

	public void setMinCompositeLoadDiff(Integer minCompositeLoadDiff) throws Exception {
		if (minCompositeLoadDiff <= 100 && minCompositeLoadDiff >= 0) {
			this.minCompositeLoadDiff = minCompositeLoadDiff;
		} else {
			throw new Exception(
					"minCompositeLoadDiff value is not in range 0-100, minCompositeLoadDiff : " + minCompositeLoadDiff);
		}
	}

	public Integer getCalculationInterval() {
		return calculationInterval;
	}

	public void setCalculationInterval(Integer calculationInterval) throws Exception {
		if (calculationInterval <= 100 & calculationInterval >= 10) {
			this.calculationInterval = calculationInterval;
		} else {
			throw new Exception(
					"calculationInterval value is not in range 10-100, calculationInterval : " + calculationInterval);
		}
	}

	public Boolean getIntraEnbLoadBalance() {
		return intraEnbLoadBalance;
	}

	public void setIntraEnbLoadBalance(Boolean intraEnbLoadBalance) {
		this.intraEnbLoadBalance = intraEnbLoadBalance;
	}

	public Boolean getCarrierAggMode() {
		return carrierAggMode;
	}

	public void setCarrierAggMode(Boolean carrierAggMode) {
		this.carrierAggMode = carrierAggMode;
	}

	public RadioParameters() {
	}

	public Integer getTxpower() {
		return txpower;
	}

	public void setTxpower(Integer txpower) {
		this.txpower = txpower;
	}

	public Integer getEarfcn() {
		return earfcn;
	}

	public ArrayList<Integer> getActiveCells() {
		return activeCells;
	}

	public void addToCellList(Integer cell) {
		if (activeCells == null) {
			activeCells = new ArrayList<Integer>();
			activeCells.add(cell);
		}
	}

	/**
	 * this will Override the entire list!
	 * 
	 * @param activeCells
	 */
	public void setActiveCells(ArrayList<Integer> activeCells) {
		this.activeCells = activeCells;
	}

	public void setEarfcn(Integer earfcn) {
		this.earfcn = earfcn;
	}

	public Boolean getMfbi() {
		return mfbi;
	}

	public void setMfbi(Boolean mfbi) {
		this.mfbi = mfbi;
	}

	public String getProfileName() {
		return profileName;
	}

	public void setProfileName(String profileName) {
		this.profileName = profileName;
	}

	public void setCfi(String cfi) {
		this.cfi = cfi;
	}

	public boolean isMfbi() {
		return mfbi;
	}

	public String getBandwidth() {
		return bandwidth;
	}

	public void setBandwidth(String bandwidth) {
		this.bandwidth = bandwidth;
	}

	public void setBandWidthSNMP(String bandWidth) throws NoSuchFieldException {
		switch (bandWidth) {
		// SNMP
		case "2": {
			this.bandwidth = "5";
			break;
		}
		// snmp
		case "3": {
			this.bandwidth = "10";
			break;
		}
		// snmp
		case "4":{
			this.bandwidth = "15";
			break;
		}
		// snmp
		case "5": {
			this.bandwidth = "20";
			break;
		}
		// error
		default: {
			throw new NoSuchFieldException("ERROR in BandWidth SNMP set Method");
		}
		}
	}

	public String getSpecialSubFrame() {
		return specialSubFrame;
	}

	public void setSpecialSubFrame(String specialSubFrame) throws NoSuchFieldException {
		if (specialSubFrame.equals("SSF_5") || specialSubFrame.equals("5")) {
			this.specialSubFrame = "5";
		} else {
			if (specialSubFrame.equals("SSF_7") || specialSubFrame.equals("7")) {
				this.specialSubFrame = "7";
			} else {
				throw new NoSuchFieldException("ERROR in SpecialSubFrame SNMP set Method");
			}
		}
	}

	public void setCFI(String cfi) {
		this.cfi = cfi;
	}

	public String getCfi() {
		return this.cfi;
	}

	public Integer getBand() {
		return band;
	}

	public void setBand(Integer band) {
		this.band = band;
	}

	public String getDuplex() {
		return duplex;
	}

	/**
	 * last modification 11/01/17 - there was no filter set and snmp caused
	 * excpetion after test.
	 * 
	 * @author Shahaf Shuhamy
	 * @param duplex
	 */
	public void setDuplex(String duplex) {
		switch (duplex) {
		case ("1"): {
			GeneralUtils.printToConsole("set radio/duplex to FDD");
			duplex = "FDD";
			break;
		}
		case ("2"): {
			duplex = "TDD";
			GeneralUtils.printToConsole("set radio/duplex to TDD");
			break;
		}
		}
		GeneralUtils.printToConsole("set radio/duplex to " + duplex);
		this.duplex = duplex;
	}

	/**
	 * last modification 18/01/2017
	 * 
	 * @author Shahaf Shuhamy
	 * @return
	 */
	public String getFrameConfig() {
		return this.frameConfig;
	}

	public void setFrameConfig(String frameConfig) {
		if (frameConfig.equals("DL_60_UL_20_SP_20") || frameConfig.equals("2")) {
			this.frameConfig = "2";
		} else if (frameConfig.equals("DL_40_UL_40_SP_20") || frameConfig.equals("1")) {
			this.frameConfig = "1";
		} else {
			GeneralUtils.printToConsole("Wrong value for Frame Config: " + frameConfig + " will use Default Value:1");
			this.frameConfig = "1";
		}
	}

	/**
	 * returns a string built as follows:
	 * FrameConfig_Duplex_Bandwidth_SpecialSubFrame
	 */
	public String getCalculatorString(String ptXp) {
		StringBuilder sb = new StringBuilder();
		if (getDuplex().equals("TDD")) {
			sb.append(getFrameConfig() + "_");
			sb.append(notFrameConfigToString());
			sb.append("_" + getSpecialSubFrame());
		} else {
			sb = notFrameConfigToString();
		}
		sb.append("_" + ptXp);

		if (donorRadioParameters != null) {
			sb.append("#").append(donorRadioParameters.getCalculatorString(ptXp));
		}

		return sb.toString();
	}

	public StringBuilder notFrameConfigToString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getDuplex() + "_");
		sb.append(getBandwidth() + "_");
		sb.append(cfi);
		return sb;
	}

	@Override
	public String toString() {
		String result = null;
		if (duplex.equals("FDD")) {
			result = mandatoryToString() + "]";
		} else {
			result = mandatoryToString() + ", frameConfig=" + frameConfig + ", specialSubFrame=" + specialSubFrame
					+ "earfcn=" + earfcn + ", ul=" + downLinkFrequency + ", dl=" + upLinkFrequency + ", mfbi=" + mfbi + ", txpower=" + txpower
					+ "]";
		}
		if (donorRadioParameters != null) {
			result = result + "\nDonor's Radio Profile:" + donorRadioParameters.toString();
		}
		return result;
	}

	private String mandatoryToString() {
		return "RadioParameters [profileName=" + profileName + ", duplex=" + duplex + ", bandwidth=" + bandwidth
				+ ", cfi=" + cfi + ", band=" + band;
	}

	@Override
	public EnbProfiles getType() {
		return EnbProfiles.Radio_Profile;
	}

	public boolean isFDD() {
		boolean result = false;

		try {
			result = duplex.equals("FDD");
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public enum PRSBandWidthEnum {
		SIX(6), FIFTEEN(15), TWENTY_FIVE(25), FIFTY(50), SEVENTY_FIVE(75), HUNDRED(100);

		public int prs;

		private PRSBandWidthEnum(int prs) {
			this.prs = prs;
		}

		private PRSBandWidthEnum(String prs) {
			switch (prs) {
			case "6":
				this.prs = 6;
				break;
			case "15":
				this.prs = 15;
				break;
			case "25":
				this.prs = 25;
				break;
			case "50":
				this.prs = 50;
				break;
			case "75":
				this.prs = 75;
				break;
			case "100":
				this.prs = 100;
				break;
			default:
				GeneralUtils.printToConsole("PRS BandWidth Error Value - using default 6");
				this.prs = 6;
				break;
			}
		}

		public Integer getPRS() {
			return prs;
		}
	}

	public enum PRSPeriodiclyEnum {
		ONE_HUNDRED_SIXTY(160), THREE_HUNDRED_TWENTY(320), SIX_HUNDRED_FORTY(640), ONE_THOUSEND_TWO_HUNDRED_EIGHTY(
				1280);

		private int prs;

		private PRSPeriodiclyEnum(int prs) {
			this.prs = prs;
		}

		private PRSPeriodiclyEnum(String prs) {
			switch (prs) {
			case "160":
				this.prs = 160;
				break;
			case "320":
				this.prs = 320;
				break;
			case "640":
				this.prs = 640;
				break;
			case "1280":
				this.prs = 1280;
				break;
			default:
				GeneralUtils.printToConsole("PRS BandWidth Error Value - using default 160");
				this.prs = 160;
				break;
			}
		}

		public Integer getPRS() {
			return prs;
		}
	}

	public enum PRSMutingPeriodiclyEnum {
		TWO(2), FOUR(4), EIGHT(8), SIXTEE(16);

		private int prs;

		private PRSMutingPeriodiclyEnum(int prs) {
			this.prs = prs;
		}

		private PRSMutingPeriodiclyEnum(String prs) {
			switch (prs) {
			case "2":
				this.prs = 2;
				break;
			case "4":
				this.prs = 4;
				break;
			case "8":
				this.prs = 8;
				break;
			case "16":
				this.prs = 16;
				break;
			default:
				GeneralUtils.printToConsole("PRS BandWidth Error Value - using default 2");
				this.prs = 2;
				break;
			}
		}

		public Integer getPRS() {
			return prs;
		}
	}
}

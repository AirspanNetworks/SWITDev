package Netspan.Profiles;

import java.util.ArrayList;

import Netspan.EnbProfiles;
import Netspan.API.Enums.ConnectedModeEventTypes;
import Netspan.API.Enums.EnabledStates;


public class MobilityParameters implements INetspanProfile{
	private String profileName = null;
	private Boolean isIntra= null;
	private ConnectedModeEventTypes eventType = null;
	private EnabledStates thresholdBasedMeasurement = null;
	private Boolean thresholdBasedMeasurementDual = null;
	private Integer stopGap = null;
	private Integer startGap = null;
	
	private Double A3Offset = null;	
	private Integer RsrpEventThreshold1 = null;
	private Integer RsrpEventThreshold2 = null;
	

	private Double Hysteresis = null;
	
	// Qos Based Params.
	private EnabledStates QosBasedMeasurement = null;
	private EnabledStates QosHoAccessAdmin = null;
	private ConnectedModeEventTypes QosBasedEventType = null;
	private ArrayList<Integer> QosBasedEarfcnList = null;
	private Integer QosBasedThreshold1 = null;
	private Integer QosBasedThreshold2 = null;
	
	public Integer getQosBasedThreshold1() {
		return QosBasedThreshold1;
	}
	
	public void setQosBasedThreshold1(Integer qosBasedThreshold1) {
		QosBasedThreshold1 = qosBasedThreshold1;
	}
	
	public Integer getQosBasedThreshold2() {
		return QosBasedThreshold2;
	}
	
	public void setQosBasedThreshold2(Integer qosBasedThreshold2) {
		QosBasedThreshold2 = qosBasedThreshold2;
	}
	
	public EnabledStates getQosBasedMeasurement() {
		return QosBasedMeasurement;
	}

	public void setQosBasedMeasurement(EnabledStates qosBasedMeasurement) {
		QosBasedMeasurement = qosBasedMeasurement;
	}
	
	public EnabledStates getQosHoAccessAdmin() {
		return QosHoAccessAdmin;
	}

	public void setQosHoAccessAdmin(EnabledStates qosHoAccessAdmin) {
		QosHoAccessAdmin = qosHoAccessAdmin;
	}

	public ConnectedModeEventTypes getQosBasedEventType() {
		return QosBasedEventType;
	}

	public void setQosBasedEventType(ConnectedModeEventTypes qosBasedEventType) {
		QosBasedEventType = qosBasedEventType;
	}

	public ArrayList<Integer> getQosBasedEarfcnList() {
		return QosBasedEarfcnList;
	}

	public void setQosBasedEarfcnList(ArrayList<Integer> qosBasedEarfcnList) {
		QosBasedEarfcnList = qosBasedEarfcnList;
	}

	public Integer getStopGap() {
		return stopGap;
	}

	public void setStopGap(Integer stopGap) {
		this.stopGap = stopGap;
	}

	public Integer getStartGap() {
		return startGap;
	}

	public void setStartGap(Integer startGap) {
		this.startGap = startGap;
	}

	public Boolean getThresholdBasedMeasurementDual() {
		return thresholdBasedMeasurementDual;
	}

	public void setThresholdBasedMeasurementDual(Boolean thresholdBasedMeasurementDual) {
		this.thresholdBasedMeasurementDual = thresholdBasedMeasurementDual;
	}

	public MobilityParameters(){}

	public String getProfileName() {
		return profileName;
	}

	public void setProfileName(String profileName) {
		this.profileName = profileName;
	}

	public ConnectedModeEventTypes getEventType() {
		return eventType;
	}

	public void setEventType(ConnectedModeEventTypes eventType) {
		this.eventType = eventType;
	}

	@Override
	public String toString() {
		return "MobilityParameters profileName:" + profileName + ", intraFreqEventType:" + eventType;
	}

	public EnabledStates getThresholdBasedMeasurement() {
		return thresholdBasedMeasurement;
	}
	
	public void setThresholdBasedMeasurement(EnabledStates thresholdBasedMeasurement) {
		this.thresholdBasedMeasurement = thresholdBasedMeasurement;
	}

	@Override
	public EnbProfiles getType() {
		return EnbProfiles.Mobility_Profile;
	}

	public Double getA3Offset() {
		return A3Offset;
	}

	public void setA3Offset(Double a3Offset) {
		A3Offset = a3Offset;
	}

	public Integer getRsrpEventThreshold1() {
		return RsrpEventThreshold1;
	}

	public void setRsrpEventThreshold1(Integer rsrpEventThreshold1) {
		RsrpEventThreshold1 = rsrpEventThreshold1;
	}

	public Integer getRsrpEventThreshold2() {
		return RsrpEventThreshold2;
	}

	public void setRsrpEventThreshold2(Integer rsrpEventThreshold2) {
		RsrpEventThreshold2 = rsrpEventThreshold2;
	}

	public Double getHysteresis() {
		return Hysteresis;
	}

	public void setHysteresis(Double hysteresis) {
		Hysteresis = hysteresis;
	}

	public Boolean getIsIntra() {
		return isIntra;
	}

	public void setIsIntra(Boolean isIntra) {
		this.isIntra = isIntra;
	}	
}


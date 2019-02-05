package Action.EnodebAction;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Test;

import EnodeB.EnodeB;
import Netspan.API.Enums.ConnectedModeEventTypes;
import Netspan.API.Enums.EnabledStates;
import Netspan.Profiles.MobilityParameters;
import Utils.GeneralUtils;
import Utils.SysObjUtils;
import jsystem.framework.ParameterProperties;
import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;
import jsystem.framework.scenario.Parameter;
import testsNG.Actions.EnodeBConfig;

public class Mobility extends EnodebAction {
	protected EnodeB dut;
	protected Integer cellId = 1;
	protected String cloneFromName = null;
	protected Boolean isIntra = null;
	protected ConnectedModeEventTypes modeEventType = null;
	protected Double hysteresis = null;
	protected Double a3Offset = null;
	protected Integer rsrpEventThreshold1 = null;
	protected Integer rsrpEventThreshold2 = null;
	protected EnabledStates qosBasedMeasurement = null;
	protected EnabledStates qosHoAccessAdmin = null;
	protected ConnectedModeEventTypes qosBasedEventType = null;
	protected Integer qosBasedThreshold1 = null;
	protected Integer qosBasedThreshold2 = null;
	protected ArrayList<Integer> qosBasedEarfcnList = new ArrayList<Integer>();
	protected EnabledStates thresholdBasedMeasurement = null;
	protected Integer stopGap = null;
	protected Integer startGap = null;
	protected Boolean thresholdBasedMeasurementDual = null;

	@ParameterProperties(description = "Name of ENB from the SUT")
	public void setDUT(String dut) {
		ArrayList<EnodeB> temp = (ArrayList<EnodeB>) SysObjUtils.getInstnce().initSystemObject(EnodeB.class, false,
				dut.split(","));
		this.dut = temp.get(0);
	}

	@ParameterProperties(description = "Cell ID (Type: int, default=1)")
	public void setCellId(String cellId) {
		try {
			this.cellId = Integer.valueOf(cellId);
		} catch (Exception exc) {
			report.report("Failed to load CellId due to: " + exc.getMessage(), Reporter.FAIL);
		}
	}

	@ParameterProperties(description = "Clone from name")
	public void setCloneFromName(String cloneFromName) {
		this.cloneFromName = cloneFromName;
	}

	@ParameterProperties(description = "Set IsIntra")
	public void setIsIntra(boolean isIntra) {
		this.isIntra = Boolean.valueOf(isIntra);
	}

	@ParameterProperties(description = "Set EventType")
	public void setEventType(ConnectedModeEventTypes eventType) {
		this.modeEventType = eventType;
	}

	@ParameterProperties(description = "Set Hysteresis (Type: long)")
	public void setHysteresis(String hysteresis) {
		try {
			this.hysteresis = Double.valueOf(hysteresis);
		} catch (Exception exc) {
			report.report("Failed to Set Hysteresis due to: " + exc.getMessage(), Reporter.FAIL);
		}
	}

	@ParameterProperties(description = "Set A3Offset (Type: long)")
	public void setA3Offset(String a3Offset) {
		try {
			this.a3Offset = Double.valueOf(a3Offset);
		} catch (Exception exc) {
			report.report("Failed to Set A3Offset due to: " + exc.getMessage(), Reporter.FAIL);
		}
	}

	@ParameterProperties(description = "Set RsrpEventThreshold1 (Type: int)")
	public void setRsrpEventThreshold1(String rsrpEventThreshold1) {
		try {
			this.rsrpEventThreshold1 = Integer.valueOf(rsrpEventThreshold1);
		} catch (Exception exc) {
			report.report("Failed to Set RsrpEventThreshold1 due to: " + exc.getMessage(), Reporter.FAIL);
		}
	}

	@ParameterProperties(description = "Set RsrpEventThreshold2 (Type: int)")
	public void setRsrpEventThreshold2(String rsrpEventThreshold2) {
		try {
			this.rsrpEventThreshold2 = Integer.valueOf(rsrpEventThreshold2);
		} catch (Exception exc) {
			report.report("Failed to Set RsrpEventThreshold2 due to: " + exc.getMessage(), Reporter.FAIL);
		}
	}

	@ParameterProperties(description = "Set QosBasedMeasurement")
	public void setQosBasedMeasurement(EnabledStates qosBasedMeasurement) {
		this.qosBasedMeasurement = qosBasedMeasurement;
	}

	@ParameterProperties(description = "Set QosHoAccessAdmin")
	public void setQosHoAccessAdmin(EnabledStates qosHoAccessAdmin) {
		this.qosHoAccessAdmin = qosHoAccessAdmin;
	}

	@ParameterProperties(description = "Set QosBasedEventType")
	public void setQosBasedEventType(ConnectedModeEventTypes qosBasedEventType) {
		this.qosBasedEventType = qosBasedEventType;
	}

	@ParameterProperties(description = "Set QosBasedThreshold1 (Type: int)")
	public void setQosBasedThreshold1(String qosBasedThreshold1) {
		try {
			this.qosBasedThreshold1 = Integer.valueOf(qosBasedThreshold1);
		} catch (Exception exc) {
			report.report("Failed to Set QosBasedThreshold1 due to: " + exc.getMessage(), Reporter.FAIL);
		}
	}

	@ParameterProperties(description = "Set QosBasedThreshold2 (Type: int)")
	public void setQosBasedThreshold2(String qosBasedThreshold2) {
		try {
			this.qosBasedThreshold2 = Integer.valueOf(qosBasedThreshold2);
		} catch (Exception exc) {
			report.report("Failed to Set QosBasedThreshold2 due to: " + exc.getMessage(), Reporter.FAIL);
		}
	}

	@ParameterProperties(description = "Set QosBasedEarfcnList split by ; (for example: 39790;40690;40695;38950)")
	public void setQosBasedEarfcnList(String qosBasedEarfcnList) {
		try {
			for (String qos : qosBasedEarfcnList.split(";")) {
				this.qosBasedEarfcnList.add(Integer.valueOf(qos));
			}
		} catch (Exception exc) {
			report.report("Failed to load QosBasedEarfcnList due to: " + exc.getMessage(), Reporter.FAIL);
		}
	}

	@ParameterProperties(description = "Set ThresholdBasedMeasurement")
	public void setThresholdBasedMeasurement(EnabledStates thresholdBasedMeasurement) {
		this.thresholdBasedMeasurement = thresholdBasedMeasurement;
	}

	@ParameterProperties(description = "Set StopGap (Type: int)")
	public void setStopGap(String stopGap) {
		try {
			this.stopGap = Integer.valueOf(stopGap);
		} catch (Exception exc) {
			report.report("Failed to set StopGap due to: " + exc.getMessage(), Reporter.FAIL);
		}
	}

	@ParameterProperties(description = "Set StartGap (Type: int)")
	public void setStartGap(String startGap) {
		try {
			this.startGap = Integer.valueOf(startGap);
		} catch (Exception exc) {
			report.report("Failed to set StartGap due to: " + exc.getMessage(), Reporter.FAIL);
		}
	}

	@ParameterProperties(description = "Set ThresholdBasedMeasurementDual")
	public void setThresholdBasedMeasurementDual(boolean thresholdBasedMeasurementDual) {
		this.thresholdBasedMeasurementDual = Boolean.valueOf(thresholdBasedMeasurementDual);
	}

	@Test // 1
	@TestProperties(name = "Clone and Set Mobility Profile", returnParam = "LastStatus", paramsInclude = { "DUT",
			"CellId", "CloneFromName", "IsIntra", "EventType", "Hysteresis", "A3Offset", "RsrpEventThreshold1",
			"RsrpEventThreshold2", "QosBasedMeasurement", "QosHoAccessAdmin", "QosBasedEventType", "QosBasedThreshold1",
			"QosBasedThreshold2", "QosBasedEarfcnList", "ThresholdBasedMeasurement", "StopGap", "StartGap",
			"ThresholdBasedMeasurementDual" })
	public void cloneAndSetMobilityProfile() {
		MobilityParameters mobilityParams = new MobilityParameters();
		GeneralUtils.startLevel(this.dut.getName() + " (Cell " + this.cellId + ") Clone and Set Mobility Profile");
		report.report("Clone From Name: " + this.cloneFromName);

		if (this.isIntra != null) {
			report.report("Set IsIntra=" + this.isIntra);
			mobilityParams.setIsIntra(this.isIntra);
		}
		if (this.modeEventType != null) {
			report.report("Set EventType=" + this.modeEventType);
			mobilityParams.setEventType(this.modeEventType);
		}
		if (this.hysteresis != null) {
			report.report("Set Hysteresis=" + this.hysteresis);
			mobilityParams.setHysteresis(this.hysteresis);
		}
		if (this.a3Offset != null) {
			report.report("Set A3Offset=" + this.a3Offset);
			mobilityParams.setA3Offset(this.a3Offset);
		}
		if (this.rsrpEventThreshold1 != null) {
			report.report("Set RsrpEventThreshold1=" + this.rsrpEventThreshold1);
			mobilityParams.setRsrpEventThreshold1(this.rsrpEventThreshold1);
		}
		if (this.rsrpEventThreshold2 != null) {
			report.report("Set RsrpEventThreshold2=" + this.rsrpEventThreshold2);
			mobilityParams.setRsrpEventThreshold2(this.rsrpEventThreshold2);
		}
		if (this.qosBasedMeasurement != null) {
			report.report("Set QosBasedMeasurement=" + this.qosBasedMeasurement);
			mobilityParams.setQosBasedMeasurement(this.qosBasedMeasurement);
		}
		if (this.qosHoAccessAdmin != null) {
			report.report("Set QosHoAccessAdmin=" + this.qosHoAccessAdmin);
			mobilityParams.setQosHoAccessAdmin(this.qosHoAccessAdmin);
		}
		if (this.qosBasedEventType != null) {
			report.report("Set QosBasedEventType=" + this.qosBasedEventType);
			mobilityParams.setQosBasedEventType(this.qosBasedEventType);
		}
		if (this.qosBasedThreshold1 != null) {
			report.report("Set QosBasedThreshold1=" + this.qosBasedThreshold1);
			mobilityParams.setQosBasedThreshold1(this.qosBasedThreshold1);
		}
		if (this.qosBasedThreshold2 != null) {
			report.report("Set QosBasedThreshold2=" + this.qosBasedThreshold2);
			mobilityParams.setQosBasedThreshold2(this.qosBasedThreshold2);
		}
		if (this.qosBasedEarfcnList != null && !this.qosBasedEarfcnList.isEmpty()) {
			report.report("Set QosBasedEarfcnList=" + this.qosBasedEarfcnList.toString());
			mobilityParams.setQosBasedEarfcnList(this.qosBasedEarfcnList);
		}
		if (this.thresholdBasedMeasurement != null) {
			report.report("Set ThresholdBasedMeasurement=" + this.thresholdBasedMeasurement);
			mobilityParams.setThresholdBasedMeasurement(this.thresholdBasedMeasurement);
		}
		if (this.stopGap != null) {
			report.report("Set StopGap=" + this.stopGap);
			mobilityParams.setStopGap(this.stopGap);
		}
		if (this.startGap != null) {
			report.report("Set StartGap=" + this.startGap);
			mobilityParams.setStartGap(this.startGap);
		}
		if (this.thresholdBasedMeasurementDual != null) {
			report.report("Set ThresholdBasedMeasurementDual=" + this.thresholdBasedMeasurementDual);
			mobilityParams.setThresholdBasedMeasurementDual(this.thresholdBasedMeasurementDual);
		}
		GeneralUtils.stopLevel();

		boolean flag = EnodeBConfig.getInstance().cloneAndSetMobilityProfileViaNetSpan(this.dut, this.cloneFromName,
				this.cellId, mobilityParams);

		if (!flag) {
			report.report("Clone and Set Mobility Profile Failed", Reporter.FAIL);
		}
	}

	@Override
	public void handleUIEvent(HashMap<String, Parameter> map, String methodName) throws Exception {
		map.get("A3Offset").setVisible(false);
		map.get("RsrpEventThreshold1").setVisible(false);
		map.get("RsrpEventThreshold2").setVisible(false);
		map.get("StopGap").setVisible(false);
		map.get("StartGap").setVisible(false);
		map.get("ThresholdBasedMeasurementDual").setVisible(false);
		
		Parameter eventType = map.get("EventType");
		Parameter thresholdBasedMeasurement = map.get("ThresholdBasedMeasurement");

		if (thresholdBasedMeasurement.getValue() != null && thresholdBasedMeasurement.getValue().toString().equals(EnabledStates.ENABLED.toString())) {
			map.get("ThresholdBasedMeasurementDual").setVisible(true);
			map.get("StopGap").setVisible(true);
			map.get("StartGap").setVisible(true);
		}

		if (eventType != null && eventType.getValue() != null) {
			switch (ConnectedModeEventTypes.valueOf(eventType.getValue().toString())) {
			case A_3:
				map.get("A3Offset").setVisible(true);
				break;
			case A_4:
				map.get("RsrpEventThreshold1").setVisible(true);
				break;
			case A_5:
				map.get("RsrpEventThreshold1").setVisible(true);
				map.get("RsrpEventThreshold2").setVisible(true);
				break;
			default:
				break;
			}
		}
	}
}
package Action.EnodebAction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;

import EnodeB.EnodeB;
import EnodeB.EnodeB.Architecture;
import Netspan.NetspanServer;
import Netspan.API.Lte.RFStatus;
import Utils.SysObjUtils;
import jsystem.framework.ParameterProperties;
import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;
import jsystem.framework.scenario.Parameter;
import testsNG.Actions.EnodeBConfig;

public class Status extends EnodebAction {
	protected RfNumber rfNumber = RfNumber.RF_1;
	public OperationalStatus operationalStatus = null;
	public StatusParameter statusParameter = null;
	public String configuredTxPower = null;
	public String actualTxPower = null;
	public String measuredVswr = null;
	public EnodeB dut;
	public static final String VERIFY_RF_STATUS = "verifyRFStatus";
	public ExpectedStatus expectedStatus = null;
	public enum RfNumber {
		RF_1("RF 1"), RF_2("RF 2");
		private final String value;
		RfNumber(String v) {
	        value = v;
	    }
	    public String value() {
	        return value;
	    }
	}

	public enum OperationalStatus {
		InService, OutOfService;
	}

	public enum StatusParameter {
		Operational_Status, Configured_Tx_Power, Actual_Tx_Power, Measured_Vswr;
	}
	
	public enum ExpectedStatus {
		LOCKED, NOT_LOCKED;
	}

	@ParameterProperties(description = "Select Rf Number (default=RF_1)")
	public void setRfNumber(RfNumber rfNumber) {
		this.rfNumber = rfNumber;
	}

	@ParameterProperties(description = "Select Status Parameter")
	public void setStatusParameter(StatusParameter statusParameter) {
		this.statusParameter = statusParameter;
	}

	@ParameterProperties(description = "Expected Operational Status value")
	public void setOperationalStatus(OperationalStatus operationalStatus) {
		this.operationalStatus = operationalStatus;
	}

	@ParameterProperties(description = "Expected Configured Tx Power value (dBm)")
	public void setConfiguredTxPower(String configuredTxPower) {
		this.configuredTxPower = configuredTxPower;
	}

	@ParameterProperties(description = "Expected Actual Tx Power value (dBm)")
	public void setActualTxPower(String actualTxPower) {
		this.actualTxPower = actualTxPower;
	}

	@ParameterProperties(description = "Expected Measured VSWR value")
	public void setMeasuredVswr(String measuredVswr) {
		this.measuredVswr = measuredVswr;
	}

	@ParameterProperties(description = "Enter Name of ENB from the SUT")
	public void setDUT(String dut) {
		ArrayList<EnodeB> temp = (ArrayList<EnodeB>) SysObjUtils.getInstnce().initSystemObject(EnodeB.class, false,
				dut.split(","));
		this.dut = temp.get(0);
	}
	
	@ParameterProperties(description = "Select Expected Status")
	public void setExpectedStatus(ExpectedStatus expectedStatus) {
		this.expectedStatus = expectedStatus;
	}

	@Test // 1
	@TestProperties(name = "Verify RF Status", returnParam = "LastStatus", paramsInclude = { "DUT", "RfNumber",
			"OperationalStatus", "ConfiguredTxPower", "ActualTxPower", "MeasuredVswr", "StatusParameter" })
	public void verifyRFStatus() throws Exception {
		List<RFStatus> rfs = NetspanServer.getInstance().getRFStatus(this.dut);
		boolean frNumberExist = false;

		for (RFStatus rf : rfs) {
			if (rf.RfNumber.equals(this.rfNumber.value())) {
				frNumberExist = true;

				switch (this.statusParameter) {
				case Operational_Status:
					super.reportCompareBetweenTwoStrings(rf.RfNumber + " " + this.statusParameter.toString(), rf.OperationalStatus, this.operationalStatus.toString());
					break;
				case Configured_Tx_Power:
					super.reportCompareBetweenTwoStrings(rf.RfNumber + " " + this.statusParameter.toString(), rf.ConfiguredTxPower, this.configuredTxPower);
					break;
				case Actual_Tx_Power:
					super.reportCompareBetweenTwoStrings(rf.RfNumber + " " + this.statusParameter.toString(), rf.ActualTxPower, this.actualTxPower);
					break;
				case Measured_Vswr:
					super.reportCompareBetweenTwoStrings(rf.RfNumber + " " + this.statusParameter.toString(), rf.MeasuredVswr, this.measuredVswr);
					break;
				}
			}
		}

		if (!frNumberExist) {
			report.report(this.rfNumber.value() + " does not exist", Reporter.FAIL);
		}
	}

	@Test // 2
	@TestProperties(name = "Verify GPS and Synchronization Status", returnParam = "LastStatus", paramsInclude = { "DUT", "ExpectedStatus" })
	public void verifyGPSandSynchronizationStatus() throws Exception {
		Architecture architectureNode = this.dut.getArchitecture();
		String finalReport = "";
		Boolean flag = false;
		
		switch (architectureNode) {
		case FSM:
			report.report("Verify Synchronization Status");
			flag = NetspanServer.getInstance().getPTPStatus(this.dut).isSyncStatus();
			finalReport = "Synchronization Status: " + NetspanServer.getInstance().getPTPStatus(this.dut).syncStatus + ", Expected Status: " + this.expectedStatus;
			break;
		case XLP:
			report.report("Verify GPS Status");
			flag = NetspanServer.getInstance().getGPSStatus(this.dut).contains("Locked");
			finalReport = "GPS Lock: " + NetspanServer.getInstance().getGPSStatus(this.dut) + ", Expected Status: " + this.expectedStatus;
			break;
		default:
			finalReport = "Failed to get GPS or Synchronization Status";
			break;
		}
		
		if(flag && (this.expectedStatus == ExpectedStatus.LOCKED)) {
			report.report(finalReport);
		}
		else if(!flag && (this.expectedStatus == ExpectedStatus.NOT_LOCKED)) {
			report.report(finalReport);
		}
		else {
			report.report(finalReport, Reporter.FAIL);
		}
	}
	
	@Override
	public void handleUIEvent(HashMap<String, Parameter> map, String methodName) throws Exception {
		switch (methodName) {
		case VERIFY_RF_STATUS:
			handleUIEventVerifyRFStatus(map, methodName);
		}
	}

	private void handleUIEventVerifyRFStatus(HashMap<String, Parameter> map, String methodName) {
		map.get("OperationalStatus").setVisible(false);
		map.get("ConfiguredTxPower").setVisible(false);
		map.get("ActualTxPower").setVisible(false);
		map.get("MeasuredVswr").setVisible(false);

		Parameter statusParameter = map.get("StatusParameter");

		if (statusParameter != null && statusParameter.getValue() != null) {
			switch (StatusParameter.valueOf(statusParameter.getValue().toString())) {
			case Operational_Status:
				map.get("OperationalStatus").setVisible(true);
				break;
			case Configured_Tx_Power:
				map.get("ConfiguredTxPower").setVisible(true);
				break;
			case Actual_Tx_Power:
				map.get("ActualTxPower").setVisible(true);
				break;
			case Measured_Vswr:
				map.get("MeasuredVswr").setVisible(true);
				break;
			}
		}
	}
}
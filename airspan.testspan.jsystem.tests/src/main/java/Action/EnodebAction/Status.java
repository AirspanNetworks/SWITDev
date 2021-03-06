package Action.EnodebAction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import EnodeB.EnodeB;
import EnodeB.EnodeB.Architecture;
import Netspan.NetspanServer;
import Netspan.API.Lte.RFStatus;
import Netspan.Profiles.ConnectedUETrafficDirection;
import Utils.GeneralUtils;
import Utils.SysObjUtils;
import jsystem.framework.ParameterProperties;
import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;
import jsystem.framework.scenario.Parameter;
import junit.framework.Assert;
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
		InService, OutOfService
	}

	public enum StatusParameter {
		Operational_Status, Configured_Tx_Power, Actual_Tx_Power, Measured_Vswr
	}
	
	public enum ExpectedStatus {
		LOCKED, NOT_LOCKED
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
			reason = this.rfNumber.value() + " does not exist";
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
			reason = finalReport;
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
	
	private ConnectedUETrafficDirection Direction = ConnectedUETrafficDirection.ALL;
	private int Category = 0;
	private int expectedUEInCategory = 0;
	
	public final int getExpectedUEInCategory() {
		return expectedUEInCategory;
	}
	
	@ParameterProperties(description = "Expected UE count in category (Default: 0)")
	public final void setExpectedUEInCategory(String expectedUEInCategory) {
		this.expectedUEInCategory = Integer.valueOf(expectedUEInCategory);
	}

	public final int getCategory() {
		return Category;
	}
	
	@ParameterProperties(description = "Traffic cetegory of UE")
	public final void setCategory(String Category) {
		this.Category = Integer.valueOf(Category);
	}

	public final ConnectedUETrafficDirection getDirection() {
		return this.Direction;
	}
	
	@ParameterProperties(description = "Desired Traffic direction for UE [UL, DL, ALL] (Default: ALL)")
	public final void setDirection(String tdirection) {
		this.Direction = ConnectedUETrafficDirection.valueOf(tdirection);
	}

	@Test // 3
	@TestProperties(name = "Verify UE Category", returnParam = "LastStatus", paramsInclude = { "DUT", "Category", "Direction", "ExpectedUEInCategory"})
	public void verifyUEConnectedCategory() {
		
		NetspanServer netspan;
		List<MutablePair<ConnectedUETrafficDirection,Integer>> MarkUpList = new ArrayList<>();
		HashMap<ConnectedUETrafficDirection, HashMap<Integer, Integer>> connectionTable = null;
		
		try {
			GeneralUtils.startLevel("Test verifyUEConnectedCategory: Category " + getCategory() + " expecting " + getExpectedUEInCategory() + " UE's");
			report.report(String.format("NetSpan: %s", this.dut));
			netspan = NetspanServer.getInstance();
			connectionTable = netspan.getUeConnectedPerCategory(this.dut);
			
			if(!connectionTable.containsKey(this.Direction)) {
				report.report("Desired Direction " + this.Direction + " not exists; check test configuration", Reporter.FAIL);
				return;
			}
			
			Assert.assertTrue("Desired category not exists", connectionTable.get(this.Direction).containsKey(this.Category));
			
			Integer real_count = connectionTable.get(this.Direction).get(this.Category);
			if(real_count != getExpectedUEInCategory()) {
				MarkUpList.add(new MutablePair<ConnectedUETrafficDirection, Integer>(this.Direction, this.Category));
			}
			
			String html_text = CategoryMapToHTML(connectionTable, "red", MarkUpList);
			report.reportHtml("Conected UE's map", html_text, true);
			if(MarkUpList.size() > 0) {
				report.report("UE count in category " + this.Category + " doesn't match expected: " +  real_count + " vs. " + getExpectedUEInCategory(), Reporter.FAIL);
			}else {
				report.report("UE count in category " + this.Category + " match expected: " + real_count);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			GeneralUtils.stopLevel();
		}
		
	}
	
	private static String CategoryMapToHTML(HashMap<ConnectedUETrafficDirection, HashMap<Integer, Integer>> connectionTable, String fail_color, List<MutablePair<ConnectedUETrafficDirection, Integer>> pairs) {
		
		StringBuffer result_data = new StringBuffer();
		StringBuffer result_header = new StringBuffer("<tr>\n\t<td>Category</td>\n");
		boolean header_ready = false;
		for(ConnectedUETrafficDirection direction : connectionTable.keySet() ) {
			result_data.append("<tr>\n\t<td>" + direction.toString() + "</td>\n");
			for(Integer category : connectionTable.get(direction).keySet() ) {
				boolean signForColor = false;
				for(Pair<ConnectedUETrafficDirection,Integer> pair : pairs) {
					MutablePair<ConnectedUETrafficDirection, Integer> pair_for_compare = 
							new MutablePair<ConnectedUETrafficDirection, Integer>(direction, category);
					if(pair_for_compare == pair) {
						signForColor = true;
						break;
					}		
				}
				if(signForColor) {
					result_data.append("\t<td><font color=\"" + fail_color + "\">" + connectionTable.get(direction).get(category) + "</font></td>\n");
					if(!header_ready)
						result_header.append("\t<th><font color=\"" + fail_color + "\">" + category.toString() + "</font></th>\n");
				}else {
					if(!header_ready)
						result_header.append("\t<th>" + category.toString() + "</th>\n");
					result_data.append("\t<td>" + connectionTable.get(direction).get(category) + "</td>\n");
				}
			}
			result_data.append("</tr>\n");
			result_header.append("</tr>\n");
			header_ready = true;
		}
		
		return "<table>\n" + result_header.toString() + result_data.toString() + "\n</table>";
	}
}
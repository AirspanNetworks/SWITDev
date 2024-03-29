package Action.EnodebAction;

import java.util.ArrayList;

import org.junit.Test;

import EnodeB.EnodeB;
import Netspan.NetspanServer;
import Netspan.API.Enums.HandoverType;
import Netspan.API.Enums.HoControlStateTypes;
import Netspan.API.Enums.X2ControlStateTypes;
import Utils.GeneralUtils;
import Utils.SysObjUtils;
import jsystem.framework.ParameterProperties;
import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;
import testsNG.Actions.Neighbors;

public class NeighborManagement extends EnodebAction {
	protected EnodeB dut;
	protected EnodeB neighbour;
	protected String sourceCell;
	protected String targetCell;
	protected HoControlStateTypes hoControlState;
	protected X2ControlStateTypes x2ControlState;
	protected HandoverType HandoverType;
	protected boolean isStaticNeighbor = false;
	protected String qOffsetRange;
	protected int numberOfNeighbors;
	protected int earfcn;

	@ParameterProperties(description = "Name of ENB from the SUT")
	public void setDUT(String dut) {
		ArrayList<EnodeB> temp = (ArrayList<EnodeB>) SysObjUtils.getInstnce().initSystemObject(EnodeB.class, false,
				dut.split(","));
		this.dut = temp.get(0);
	}

	@ParameterProperties(description = "Name of Neighbour from the SUT")
	public void setNeighbour(String neighbour) {
		ArrayList<EnodeB> temp = (ArrayList<EnodeB>) SysObjUtils.getInstnce().initSystemObject(EnodeB.class, false,
				neighbour.split(","));
		this.neighbour = temp.get(0);
	}

	@ParameterProperties(description = "Cell number of the current EnodeB")
	public void setSourceCell(String sourceCell) {
		this.sourceCell = sourceCell;
	}

	@ParameterProperties(description = "Cell number of the neighbour EnodeB")
	public void setTargetCell(String targetCell) {
		this.targetCell = targetCell;
	}

	@ParameterProperties(description = "Set Ho Control State Type")
	public void setHoControlStateTypes(HoControlStateTypes hoControlState) {
		this.hoControlState = hoControlState;
	}

	@ParameterProperties(description = "Set X2 Control State Type")
	public void setX2ControlStateTypes(X2ControlStateTypes x2ControlState) {
		this.x2ControlState = x2ControlState;
	}

	@ParameterProperties(description = "Set Handover Type")
	public void setHandoverType(HandoverType HandoverType) {
		this.HandoverType = HandoverType;
	}

	@ParameterProperties(description = "Is Static Neighbor")
	public void setIsStaticNeighbor(boolean isStaticNeighbor) {
		this.isStaticNeighbor = isStaticNeighbor;
	}

	@ParameterProperties(description = "Set qOffsetRange")
	public void setQOffsetRange(String qOffsetRange) {
		this.qOffsetRange = qOffsetRange;
	}

	@ParameterProperties(description = "Number of Neighbors")
	public void setNumberOfNeighbors(String numberOfNeighbors) {
		this.numberOfNeighbors = Integer.valueOf(numberOfNeighbors);
	}

	@ParameterProperties(description = "earfcn of DUT")
	public void setEarfcn(String earfcn) {
		this.earfcn = Integer.valueOf(earfcn);
	}


	@Test // 1
	@TestProperties(name = "Add Neighbour", returnParam = "LastStatus", paramsInclude = {
			"DUT", "sourceCell",
			"Neighbour", "targetCell",
			"HoControlStateTypes", "X2ControlStateTypes", "HandoverType", "IsStaticNeighbor", "QOffsetRange"})
	public void addNeighbour() {
		printToReportAddNeighbourDetails();
		dut.setCellContextNumber(Integer.parseInt(sourceCell));
		neighbour.setCellContextNumber(Integer.parseInt(targetCell));
		boolean flag = Neighbors.getInstance().addNeighbor(this.dut, this.neighbour, this.hoControlState,
				this.x2ControlState, this.HandoverType, this.isStaticNeighbor, this.qOffsetRange);
		report.reportHtml(dut.getName() + ": db get nghList", dut.lteCli("db get nghList"), true);
		if (!flag) {
			report.report("Add Neighbour Failed", Reporter.FAIL);
			reason = "Add Neighbour Failed";
		} else {
			report.report("Add Neighbour Succeeded");
		}
	}

	/**
	 * print To Report "Add Neighbour Test" Details
	 */
	private void printToReportAddNeighbourDetails() {
		GeneralUtils.startLevel("EnodeB " + this.dut.getName() + " Add Neighbour = " + this.neighbour.getName());
		report.report("Source cell: " + this.sourceCell + ". Target Cell: " + this.targetCell);
		report.report("HoControlStateTypes: " + this.hoControlState);
		report.report("X2ControlStateTypes: " + this.x2ControlState);
		report.report("HandoverType: " + this.HandoverType);
		report.report("IsStaticNeighbor: " + this.isStaticNeighbor);
		report.report("QOffsetRange: " + this.qOffsetRange);
		GeneralUtils.stopLevel();
	}

	@Test // 2
	@TestProperties(name = "Delete All Neighbours", returnParam = "LastStatus", paramsInclude = {"DUT"})
	public void deleteAllNeighbours() {
		report.report("Delete All Neighbours of " + this.dut.getNetspanName());
		boolean flag = Neighbors.getInstance().deleteAllNeighbors(this.dut);
		report.reportHtml(dut.getName() + ": db get nghList", dut.lteCli("db get nghList"), true);
		if (!flag) {
			report.report("Delete All Neighbours of " + this.dut.getNetspanName() + " Failed", Reporter.FAIL);
			reason = "Delete All Neighbours of " + this.dut.getNetspanName() + " Failed";
		} else {
			report.report("Delete All Neighbours of " + this.dut.getNetspanName() + " Succeeded");
		}
	}

	@Test
	@TestProperties(name = "Add Virtual Neighbour", returnParam = "LastStatus", paramsInclude = {"DUT", "NumberOfNeighbors", "earfcn"})
	public void addMultiplVirtualNeighbor() {
		GeneralUtils.startLevel("EnodeB " + this.dut.getNetspanName() + " add " + numberOfNeighbors + " Virtual neighbors");
		ArrayList<Integer> earfcns = new ArrayList<>();
		earfcns.add(earfcn);
		ArrayList<EnodeB> thirdPartyNeieghbors = Neighbors.getInstance().addingThirdPartyNeihbors(dut, numberOfNeighbors, false, earfcns);
		GeneralUtils.stopLevel();

		if (thirdPartyNeieghbors.size() != numberOfNeighbors) {
			report.report("number of created neighbors is : " + thirdPartyNeieghbors.size() + ", and not " + numberOfNeighbors + " as expected!", Reporter.WARNING);
		}

		GeneralUtils.startLevel("adding third party Nodes to dut : " + dut.getNetspanName());
		for (EnodeB node : thirdPartyNeieghbors) {
			try {
				NetspanServer.getInstance().addNeighbor(dut, node, HoControlStateTypes.ALLOWED, X2ControlStateTypes.AUTOMATIC, HandoverType.TRIGGER_X_2, true, "0");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		GeneralUtils.stopLevel();
	}

	@Test
	@TestProperties(name = "Verify Neighbour", returnParam = "LastStatus", paramsInclude = {"DUT", "Neighbour",
			"HoControlStateTypes", "X2ControlStateTypes", "HandoverType", "IsStaticNeighbor", "QOffsetRange"})
	public void verifyNeighbour() {
		GeneralUtils.startLevel("EnodeB " + this.dut.getName() + " verify Neighbour = " + this.neighbour.getName());
		report.report("IsStaticNeighbor: " + this.isStaticNeighbor);
		if (this.hoControlState != null)
			report.report("HoControlStateTypes: " + this.hoControlState);
		if (this.x2ControlState != null)
			report.report("X2ControlStateTypes: " + this.x2ControlState);
		if (this.HandoverType != null)
			report.report("HandoverType: " + this.HandoverType);
		if (this.qOffsetRange != null)
			report.report("QOffsetRange: " + this.qOffsetRange);
		GeneralUtils.stopLevel();

		boolean flag = Neighbors.getInstance().verifyNeighborParametersNMSandSNMP(this.dut, this.neighbour, this.hoControlState,
				this.x2ControlState, this.HandoverType, this.isStaticNeighbor, this.qOffsetRange);

		report.reportHtml(dut.getName() + ": db get nghList", dut.lteCli("db get nghList"), true);
		if (!flag) {
			report.report("Neighbour not verified!", Reporter.FAIL);
			reason = "Neighbour not verified!";
		} else {
			report.report("Neighbour verified!");
		}
	}

	@Test
	@TestProperties(name = "Delete Neighbour", returnParam = "LastStatus",
			paramsInclude = {"DUT", "sourceCell", "Neighbour", "targetCell",
	})
	public void deleteNeighbour() {
		report.report("EnodeB " + this.dut.getName() + " delete Neighbour = " + this.neighbour.getName());
		dut.setCellContextNumber(Integer.parseInt(sourceCell));
		neighbour.setCellContextNumber(Integer.parseInt(targetCell));
		boolean flag = Neighbors.getInstance().deleteNeighbor(this.dut, this.neighbour);
		report.reportHtml(dut.getName() + ": db get nghList", dut.lteCli("db get nghList"), true);
		if (!flag) {
			report.report("Neighbour not deleted!", Reporter.FAIL);
			reason = "Neighbour not deleted!";
		} else {
			report.report("Neighbour deleted!");
		}
	}
}
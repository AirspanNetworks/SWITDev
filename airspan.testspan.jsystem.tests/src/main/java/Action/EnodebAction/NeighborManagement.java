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
	protected HoControlStateTypes hoControlState;
	protected X2ControlStateTypes x2ControlState;
	protected HandoverType handoverType;
	protected boolean isStaticNeighbor;
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

	@ParameterProperties(description = "Set Ho Control State Type")
	public void setHoControlStateTypes(HoControlStateTypes hoControlState) {
		this.hoControlState = hoControlState;
	}

	@ParameterProperties(description = "Set X2 Control State Type")
	public void setX2ControlStateTypes(X2ControlStateTypes x2ControlState) {
		this.x2ControlState = x2ControlState;
	}

	@ParameterProperties(description = "Set Handover Type")
	public void setHandoverType(HandoverType handoverType) {
		this.handoverType = handoverType;
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
	@TestProperties(name = "Add Neighbour", returnParam = "LastStatus", paramsInclude = { "DUT", "Neighbour",
			"HoControlStateTypes", "X2ControlStateTypes", "HandoverType", "IsStaticNeighbor", "QOffsetRange" })
	public void addNeighbour() {
		GeneralUtils.startLevel("EnodeB " + this.dut.getName() + " Add Neighbour = " + this.neighbour.getName());
		report.report("HoControlStateTypes: " + this.hoControlState);
		report.report("X2ControlStateTypes: " + this.x2ControlState);
		report.report("HandoverType: " + this.handoverType);
		report.report("IsStaticNeighbor: " + this.isStaticNeighbor);
		report.report("QOffsetRange: " + this.qOffsetRange);
		GeneralUtils.stopLevel();

		boolean flag = Neighbors.getInstance().addNeighbor(this.dut, this.neighbour, this.hoControlState,
				this.x2ControlState, this.handoverType, this.isStaticNeighbor, this.qOffsetRange);
		report.reportHtml(dut.getName() + ": db get nghList", dut.lteCli("db get nghList"), true);
		if (flag == false) {
			report.report("Add Neighbour Failed", Reporter.FAIL);
		} else {
			report.report("Add Neighbour Succeeded");
		}
	}

	@Test // 2
	@TestProperties(name = "Delete All Neighbours", returnParam = "LastStatus", paramsInclude = { "DUT" })
	public void deleteAllNeighbours() {
		report.report("Delete All Neighbours of " + this.dut.getNetspanName());
		boolean flag = Neighbors.getInstance().deleteAllNeighbors(this.dut);
		report.reportHtml(dut.getName() + ": db get nghList", dut.lteCli("db get nghList"), true);
		if (flag == false) {
			report.report("Delete All Neighbours of " + this.dut.getNetspanName() + " Failed", Reporter.FAIL);
		} else {
			report.report("Delete All Neighbours of " + this.dut.getNetspanName() + " Succeeded");
		}
	}
	
	@Test
	@TestProperties(name = "Add Virtual Neighbour", returnParam = "LastStatus", paramsInclude = { "DUT", "NumberOfNeighbors","earfcn"})
	public void addMultiplVirtualNeighbor(){
		GeneralUtils.startLevel("EnodeB "+this.dut.getNetspanName()+" add "+numberOfNeighbors +" Virtual neighbors");
		ArrayList<Integer> earfcns = new ArrayList<>();
		earfcns.add(earfcn);
		ArrayList<EnodeB> thirdPartyNeieghbors = Neighbors.getInstance().addingThirdPartyNeihbors(dut,numberOfNeighbors,false,earfcns);
		GeneralUtils.stopLevel();
		
		if(thirdPartyNeieghbors.size() != numberOfNeighbors){
			report.report("number of created neighbors is : "+thirdPartyNeieghbors.size() +", and not "+numberOfNeighbors+" as expected!",Reporter.WARNING);
		}
		
		GeneralUtils.startLevel("adding third party Nodes to dut : "+dut.getNetspanName());
		for(EnodeB node : thirdPartyNeieghbors){
			try{
				NetspanServer.getInstance().addNeighbor(dut, node, HoControlStateTypes.ALLOWED, X2ControlStateTypes.AUTOMATIC, HandoverType.TRIGGER_X_2, true, "0");
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		GeneralUtils.stopLevel();
	}
	
	@Test
	@TestProperties(name = "Verify Neighbour", returnParam = "LastStatus", paramsInclude = { "DUT", "Neighbour",
			"HoControlStateTypes", "X2ControlStateTypes", "HandoverType", "IsStaticNeighbor", "QOffsetRange" })
	public void verifyNeighbour() {
		GeneralUtils.startLevel("EnodeB " + this.dut.getName() + " verify Neighbour = " + this.neighbour.getName());
		report.report("IsStaticNeighbor: " + this.isStaticNeighbor);
		if (this.hoControlState != null)		
			report.report("HoControlStateTypes: " + this.hoControlState);
		if (this.x2ControlState != null)
			report.report("X2ControlStateTypes: " + this.x2ControlState);
		if (this.handoverType != null)
			report.report("HandoverType: " + this.handoverType);	
		if (this.qOffsetRange != null)
			report.report("QOffsetRange: " + this.qOffsetRange);
		GeneralUtils.stopLevel();

		boolean flag = Neighbors.getInstance().verifyNeighborParametersNMSandSNMP(this.dut, this.neighbour, this.hoControlState,
				this.x2ControlState, this.handoverType, this.isStaticNeighbor, this.qOffsetRange);

		report.reportHtml(dut.getName() + ": db get nghList", dut.lteCli("db get nghList"), true);
		if (flag == false) {
			report.report("Neighbour not verified!", Reporter.FAIL);
		} else {
			report.report("Neighbour verified!");
		}
	}
	
	@Test
	@TestProperties(name = "Delete Neighbour", returnParam = "LastStatus", paramsInclude = { "DUT", "Neighbour" })
	public void deleteNeighbour() {
		report.report("EnodeB " + this.dut.getName() + " delete Neighbour = " + this.neighbour.getName());
		boolean flag = Neighbors.getInstance().deleteNeighbor(this.dut, this.neighbour);
		report.reportHtml(dut.getName() + ": db get nghList", dut.lteCli("db get nghList"), true);
		if (flag == false) {
			report.report("Neighbour not deleted!", Reporter.FAIL);
		} else {
			report.report("Neighbour deleted!");
		}
	}
	
}
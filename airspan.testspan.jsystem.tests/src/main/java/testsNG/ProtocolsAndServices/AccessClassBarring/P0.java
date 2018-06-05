package testsNG.ProtocolsAndServices.AccessClassBarring;

import java.util.ArrayList;

import org.junit.Test;

import EPC.EPC;
import EnodeB.EnodeB;
import Netspan.API.Enums.CellBarringPolicies;
import Netspan.Profiles.CellBarringPolicyParameters;
import UE.UE;
import Utils.GeneralUtils;
import Utils.SetupUtils;
import Utils.SysObjUtils;
import Utils.Snmp.MibReader;
import jsystem.framework.ParameterProperties;
import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;
import testsNG.TestspanTest;
import testsNG.Actions.EnodeBConfig;
import testsNG.Actions.PeripheralsConfig;
import testsNG.Actions.Traffic;


public class P0 extends TestspanTest {
	EnodeB dut;
	PeripheralsConfig peripheralsConfig;
	EnodeBConfig enbConfig;
	EPC epc;
	ArrayList<UE> UEList = new ArrayList<>();
	private Traffic traffic;
	
	@Override
	public void init() throws Exception {
		enbInTest = new ArrayList<EnodeB>();
		enbInTest.add(dut);
		report.startLevel("Test Init");
		super.init();
		peripheralsConfig = PeripheralsConfig.getInstance();
		enbConfig = EnodeBConfig.getInstance();
		epc = EPC.getInstance();
		UEList = SetupUtils.getInstance().getAllUEs();
		report.stopLevel();
	}
	
	@Test
	@TestProperties(name = "Cell_Barred", returnParam = {"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful"})
	public void Cell_Barred() {
		boolean isUEConneted = false;
		int numOfCells = dut.getNumberOfCells();
		int cellBarredFromMib;
		GeneralUtils.startLevel("Start traffic");
		try {
			traffic = Traffic.getInstance(SetupUtils.getInstance().getAllUEs());
			traffic.startTraffic();
		} catch (Exception e) {
			report.report("Couldn't start traffic", Reporter.WARNING);
			e.printStackTrace();
		}
		GeneralUtils.stopLevel();
		report.report("Changing the cell barred value to CELL BARRED");
		CellBarringPolicyParameters cb = new CellBarringPolicyParameters();
		cb.cellBarringPolicy = CellBarringPolicies.CELL_BARRED;
		for (int i = 1; i <= numOfCells; i++) {
			dut.setCellContextNumber(i);
			enbConfig.setAccessClassBarring(dut, cb);
		}
		
		GeneralUtils.unSafeSleep(10000);
		for(int i =0; i<numOfCells; i++){
			cellBarredFromMib = dut.getCellBarredMibValue(i);
			if (cellBarredFromMib == 0)
				report.report("cell number : " + (i+1) +  ", Cell Barred value in enodeB MIB:asLteStkCellSib1CfgCellBarred is 0 as expected");
			else 
				report.report("cell number : " + (i+1) +  ", Cell Barred value in enodeB MIB:asLteStkCellSib1CfgCellBarred is " + cellBarredFromMib, Reporter.FAIL);
		}
		
		for(UE ue : UEList){
			if (epc.checkUEConnectedToNode(ue, dut)){
				report.report("UE " + ue.getName() + " [ " + ue.getImsi() + " ] is connected to enodeB " + dut.getName(), Reporter.WARNING);
				isUEConneted = true;
				break;
			}
		}
		if(!isUEConneted)
			report.report("There are no UEs connected to enodeB " + dut.getName() + " as expected");
		report.reportHtml("UE show Link ("+dut.getName() + ")", dut.lteCliWithResponse("ue show link", "lte_cli:>>"), true);
		report.reportHtml("UE show Rate ("+dut.getName() + ")", dut.lteCliWithResponse("ue show rate", "lte_cli:>>"), true);
		report.report("Changing the cell barred value back to NOT BARRED");
		cb.cellBarringPolicy = CellBarringPolicies.NOT_BARRED;
		for (int i = 1; i <= numOfCells; i++) {
			dut.setCellContextNumber(i);
			enbConfig.setAccessClassBarring(dut, cb);
		}
		GeneralUtils.unSafeSleep(10000);
		for(int i =0; i<numOfCells; i++){
			cellBarredFromMib = dut.getCellBarredMibValue(i);
			if (cellBarredFromMib == 1)
				report.report("cell number : " + (i+1) +  ", Cell Barred value in enodeB MIB:asLteStkCellSib1CfgCellBarred is 1 as expected");
			else 
				report.report("cell number : " + (i+1) +  ", Cell Barred in enodeB MIB:asLteStkCellSib1CfgCellBarred is " + cellBarredFromMib, Reporter.FAIL);
		}
		
		isUEConneted = false;
		for(UE ue : UEList){
			if (epc.checkUEConnectedToNode(ue, dut)){
				report.report("UE " + ue.getName() + " [ " + ue.getImsi() + " ] is connected to enodeB " + dut.getName() + " as expected");
				isUEConneted = true;
			}
		}
		if(!isUEConneted)
			report.report("There are no UEs connected to enodeB " + dut.getName() , Reporter.WARNING);
		report.reportHtml("UE show Link ("+dut.getName() + ")", dut.lteCliWithResponse("ue show link", "lte_cli:>>"), true);
		report.reportHtml("UE show Rate ("+dut.getName() + ")", dut.lteCliWithResponse("ue show rate", "lte_cli:>>"), true);
		GeneralUtils.startLevel("Stop Traffic");
			traffic.stopTraffic();
		GeneralUtils.stopLevel();
			
	}
	
	@Test
	@TestProperties(name = "AC_Barring_TC_1", returnParam = {"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful"})
	public void AC_Barring_TC_1() {
		boolean isUEConneted = false;
		int numOfCells = dut.getNumberOfCells();
		int cellBarredFromMib;
		GeneralUtils.startLevel("Start traffic");
		try {
			traffic = Traffic.getInstance(SetupUtils.getInstance().getAllUEs());
			traffic.startTraffic();
		} catch (Exception e) {
			report.report("Couldn't start traffic", Reporter.WARNING);
			e.printStackTrace();
		}
		GeneralUtils.stopLevel();
		report.report("Changing the cell barred value to CELL BARRED");
		CellBarringPolicyParameters cb = new CellBarringPolicyParameters();
		cb.cellBarringPolicy = CellBarringPolicies.AC_BARRING;
		for (int i = 1; i <= numOfCells; i++) {
			dut.setCellContextNumber(i);
			enbConfig.setAccessClassBarring(dut, cb);
		}
		
		GeneralUtils.unSafeSleep(10000);
		//need to check the message with DM toll
			
	}
	
	@ParameterProperties(description = "First DUT")
	public void setDUT1(String dut) {
		GeneralUtils.printToConsole("Load DUT1" + dut);
		this.dut = (EnodeB) SysObjUtils.getInstnce().initSystemObject(EnodeB.class, false, dut).get(0);
		GeneralUtils.printToConsole("DUT loaded" + this.dut.getNetspanName());
	}
}

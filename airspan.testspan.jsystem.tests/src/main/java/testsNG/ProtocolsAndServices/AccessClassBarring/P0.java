package testsNG.ProtocolsAndServices.AccessClassBarring;

import static org.junit.Assert.assertArrayEquals;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;

import DMTool.DMtool;
import DMTool.Evt;
import DMTool.EvtClient;
import EPC.EPC;
import EnodeB.EnodeB;
import Netspan.API.Enums.CellBarringPolicies;
import Netspan.API.Enums.EnbStates;
import Netspan.Profiles.CellBarringPolicyParameters;
import UE.UE;
import Utils.GeneralUtils;
import Utils.SetupUtils;
import Utils.SysObjUtils;
import jsystem.framework.ParameterProperties;
import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;
import testsNG.TestspanTest;
import testsNG.Actions.EnodeBConfig;
import testsNG.Actions.PeripheralsConfig;
import testsNG.Actions.Traffic;


public class P0 extends TestspanTest {
	private EnodeB dut;
	private PeripheralsConfig peripheralsConfig;
	private EnodeBConfig enbConfig;
	private EPC epc;
	private ArrayList<UE> UEList = new ArrayList<>();
	private Traffic traffic;
	private DMtool dm;
	private EvtLis evt;
	
	@Override
	public void init() throws Exception {
		evt = new EvtLis();
		enbInTest = new ArrayList<EnodeB>();
		enbInTest.add(dut);
		report.startLevel("Test Init");
		///////////////////////////////////////////////HENNNNNNNNNNNNNNNNNNNNNNNNNNNN
		//dm = new DMtool();
		//dm.setUeIP("192.168.58.225");
		//dm.setPORT(37772);
		//dm.addlistenertoEvents(evt);
		//dm.init();	
		///////////////////////////////////////////////HENNNNNNNNNNNNNNNNNNNNNNNNNNNN
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
		report.report("AC_Barring_TC_1");

		UE choosenUE = null;
		/*GeneralUtils.startLevel("Start traffic");
		try {
			traffic = Traffic.getInstance(SetupUtils.getInstance().getAllUEs());
			traffic.startTraffic();
		} catch (Exception e) {
			report.report("Couldn't start traffic", Reporter.WARNING);
			e.printStackTrace();
		}
		GeneralUtils.stopLevel();*/
		for (UE ue : SetupUtils.getInstance().getAllUEs()) {
			if (epc.checkUEConnectedToNode(ue, dut)) {
				choosenUE = ue;
				report.report("choosen UE: "  + choosenUE.getName());
				break;
				}
		}
		if (choosenUE == null) {
			report.report("There is no ues connected to enodeBs", Reporter.FAIL);
			return;
		}

		dm = new DMtool();
		dm.setUeIP(choosenUE.getLanIpAddress());
		report.report("UE IP: " + choosenUE.getLanIpAddress() );
		dm.setPORT(choosenUE.getDMToolPort());
		report.report("UE Port: " + choosenUE.getDMToolPort());
		dm.addlistenertoEvents(evt);
		GeneralUtils.unSafeSleep(5000);
		try {
			dm.init();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			report.report(dm.cli("showPlmn"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		evt.resetEventHappened();
		report.report("Changing the cell barred value to CELL BARRED with emergancy");
		CellBarringPolicyParameters cb = new CellBarringPolicyParameters();
		cb.cellBarringPolicy = CellBarringPolicies.AC_BARRING;
		cb.IsAccessClassBarred = true;
		cb.IsEmergencyAccessBarred = false;
		cb.IsSignalingAccessBarred = true;
		cb.IsDataAccessBarred = false;
		for (int i = 1; i <= dut.getNumberOfCells(); i++) {
			dut.setCellContextNumber(i);
			enbConfig.setAccessClassBarring(dut, cb);
		}
		GeneralUtils.unSafeSleep(120000);
		
		report.report("Changing the cell barred value to CELL BARRED without emergancy");
		evt.resetEventHappened();
		cb = new CellBarringPolicyParameters();
		cb.cellBarringPolicy = CellBarringPolicies.AC_BARRING;
		cb.IsAccessClassBarred = true;
		cb.IsEmergencyAccessBarred = false;
		cb.IsSignalingAccessBarred = false;
		cb.IsDataAccessBarred = false;
		for (int i = 1; i <= dut.getNumberOfCells(); i++) {
			dut.setCellContextNumber(i);
			enbConfig.setAccessClassBarring(dut, cb);
		}
		GeneralUtils.unSafeSleep(120000);
		
		if (evt.eventHappened) {
			report.report("ue got the event as expected");
		}
		else 
			report.report("ue didnt get the event", Reporter.FAIL);
		dm.close();
		dm = null;
		//traffic.stopTraffic();
	}
	

	@Test
	@TestProperties(name = "AC_Barring_TC_2", returnParam = {"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful"})
	public void AC_Barring_TC_2() {
		boolean isUEConneted = false;
		int numOfCells = dut.getNumberOfCells();
		int cellBarredFromMib;
		/*GeneralUtils.startLevel("Start traffic");
		try {
			traffic = Traffic.getInstance(SetupUtils.getInstance().getAllUEs());
			traffic.startTraffic();
		} catch (Exception e) {
			report.report("Couldn't start traffic", Reporter.WARNING);
			e.printStackTrace();
		}
		GeneralUtils.stopLevel();*/
		report.report("Changing the cell barred value to CELL BARRED");
		CellBarringPolicyParameters cb = new CellBarringPolicyParameters();
		cb.cellBarringPolicy = CellBarringPolicies.AC_BARRING;
		cb.IsAccessClassBarred = true;
		cb.IsEmergencyAccessBarred = true;
		cb.IsSignalingAccessBarred = false;
		cb.IsDataAccessBarred = false;
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
	
	public class EvtLis extends EvtClient{
		boolean eventHappened = false;
		@Override
		public void Evt(short[] payload, long evtSeqNumber) {
			// this is repesents hex to short of the RRC payload
			// (10001212556f182004330e4681000002008857d915448c000380) in the TP
 			short[] expected = new short[] { 16, 0, 18, 18, 85, 111, 24, 32, 4, 51, 14, 70, -127, 0, 0, 2, 0, -120, 87, -39,
					21, 68, -116, 0, 3, -128 };
 			
 			String expectedStr = "";
 			for(int j = 0; j<expected.length; j++) {
 				//String expHex = Integer.toHexString(expected[j] & 0x00ff);
 				//expectedStr += expHex +" ";
 				expectedStr += expected[j] + " ";
 			}
 			report.report("expected: " + expectedStr);
			short[] actual = Arrays.copyOfRange(payload, 288, 314);
			String alert = "";
			for (int i =0; i< actual.length; i++) {
				//String hex = Integer.toHexString(actual[i] & 0x00ff);
				//alert += hex + " ";
				alert += actual[i] + " ";
			}
			report.report("actual: " + alert);
			if (Arrays.equals(expected, actual)) {
			//if (Arrays.equals(expectedStr, alert)) {
				System.out.println("BOOOOOOMMMM");
				eventHappened = true;
			}
		}
		
		public boolean getEventstatus() {
			
			return eventHappened;
		}
		
		public void resetEventHappened() {
			this.eventHappened = false;
			
		}
	}
}

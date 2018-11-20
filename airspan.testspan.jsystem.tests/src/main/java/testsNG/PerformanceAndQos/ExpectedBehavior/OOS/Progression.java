package testsNG.PerformanceAndQos.ExpectedBehavior.OOS;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import Attenuators.AttenuatorSet;
import DMTool.DMtool;
import DMTool.sqnCellMeasReport;
import EnodeB.EnodeB;
import Netspan.EnbProfiles;
import Netspan.API.Enums.HandoverType;
import Netspan.API.Enums.HoControlStateTypes;
import Netspan.API.Enums.X2ControlStateTypes;
import Netspan.Profiles.SonParameters;
import UE.UE;
import Utils.GeneralUtils;
import Utils.GeneralUtils.CellIndex;
import Utils.Pair;
import Utils.SetupUtils;
import Utils.SysObjUtils;
import jsystem.framework.ParameterProperties;
import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;
import testsNG.TestspanTest;
import testsNG.Actions.EnodeBConfig;
import testsNG.Actions.Neighbors;
import testsNG.Actions.PeripheralsConfig;

public class Progression extends TestspanTest{
	private static final int PCI_COLLISION_DETECTION_TIMER = 2 * 60 * 1000;
	private EnodeB dut;
	private EnodeB neighbor;
	private EnodeBConfig enodeBConfig;
	private ArrayList<Pair<UE, DMtool>> ueDmLists;
	private PeripheralsConfig peripheralsConfig;

	/********************************* INFRA *********************************/
	
	@Override
	public void init() throws Exception {
		enbInTest = new ArrayList<>();
		enbInTest.add(dut);
		if(neighbor != null){
			enbInTest.add(neighbor);
		}
		super.init();			
		
		initObjects();
		for(EnodeB enb : enbInSetup){
			if(!enbInTest.contains(enb)){
				enb.setServiceState(GeneralUtils.CellIndex.ENB, 0);
			}
		}
		AttenuatorSet attenuatorSetUnderTest = AttenuatorSet.getAttenuatorSet("rudat_set");
		report.report("set the attenuators default value : 0 [dB]\n");
		PeripheralsConfig.getInstance().setAttenuatorSetValue(attenuatorSetUnderTest, 0);
		initDmToolAndcheckUEsConnection(dut);
	}

	public void initObjects(){
		enodeBConfig = EnodeBConfig.getInstance();
		peripheralsConfig = PeripheralsConfig.getInstance();
	}
	
	public void initDmToolAndcheckUEsConnection(EnodeB enodeb){
		ueDmLists = new ArrayList<>();
		ArrayList<UE> ues = SetupUtils.getInstance().getallUEsPerEnodeb(enodeb);
		for(UE ue : ues){
			ueDmLists.add(new Pair<UE, DMtool>(ue, new DMtool()));
		}
		GeneralUtils.unSafeSleep(5000);
		GeneralUtils.startLevel("UEs Measurements Status.");
		peripheralsConfig.stopStartUes(ues);
		for(Pair<UE, DMtool> p : ueDmLists){
			p.getElement1().setUeIP(p.getElement0().getLanIpAddress());
			p.getElement1().setPORT(p.getElement0().getDMToolPort());
			
			sqnCellMeasReport measReport;
			try{
				p.getElement1().init();
			}catch(Exception e){
				ueDmLists.remove(p);
				continue;
			}
			try{
				measReport = p.getElement1().getMeas();
			} catch (Exception e) {
				report.report("Failed to get Measurements Report", Reporter.WARNING);
				e.printStackTrace();
				continue;
			}
			printMeasReport(p.getElement0().getName(), measReport);
			p.getElement1().close();
		}
		GeneralUtils.stopLevel();
	}
	
	public void closeDmTools(){
		for(Pair<UE, DMtool> p : ueDmLists){
			p.getElement1().close();
		}
	}
	
	@Override
	public void end(){
		closeDmTools();
		for(EnodeB enb : enbInSetup){
			if(enb != dut){
				enb.setServiceState(GeneralUtils.CellIndex.ENB, 1);
			}
		}
		super.end();
	}
	
	

	@ParameterProperties(description = "DUT")
	public void setDUT(String dut) {
		GeneralUtils.printToConsole("Load DUT " + dut);
		this.dut = (EnodeB) SysObjUtils.getInstnce().initSystemObject(EnodeB.class, false, dut).get(0);
		GeneralUtils.printToConsole("DUT loaded" + this.dut.getName());
	}
	
	@ParameterProperties(description = "Neighbor (optional - if empty, NMS's 3rd party eNodeB is used).")
	public void setNeighbor(String neighbor) {
		GeneralUtils.printToConsole("Load Neighbor " + neighbor);
		this.neighbor = (EnodeB) SysObjUtils.getInstnce().initSystemObject(EnodeB.class, false, neighbor).get(0);
		GeneralUtils.printToConsole("Neighbor loaded" + this.neighbor.getName());
	}

	/********************************* TESTS *********************************/
	
	/**
	 * Test OOS & IS Behavior
	 */
	@Test
	@TestProperties(name = "Set cell 0 to OOS - test behavior: Out Of Service Expected for Cell 0, In Service for Cell 1.",
	returnParam = { "IsTestWasSuccessful" }, 
	paramsExclude = {"IsTestWasSuccessful","Neighbor"})
	public void verifyOosBehaviorAfterSettingCell0ToOos(){
		if(enodeBConfig.getNumberOfActiveCells(dut) > 1){
			testOutOfServiceInServiceExpectedBehavior(dut, CellIndex.FORTY, CellIndex.FORTY_ONE);
		}else{
			report.report("TEST for MC eNodeB, but NumberOfActiveCells=" + enodeBConfig.getNumberOfActiveCells(dut), Reporter.WARNING);
		}
	}
	
	/**
	 * Test OOS & IS Behavior
	 */
	@Test
	@TestProperties(name = "Set cell 1 to OOS - test behavior: Out Of Service Expected for Cell 1, In Service for Cell 0.", 
	returnParam = { "IsTestWasSuccessful" }, 
	paramsExclude = {"IsTestWasSuccessful","Neighbor"})
	public void verifyOosBehaviorAfterSettingCell1ToOos(){
		if(enodeBConfig.getNumberOfActiveCells(dut) > 1){
			testOutOfServiceInServiceExpectedBehavior(dut, CellIndex.FORTY_ONE, CellIndex.FORTY);
		}else{
			report.report("TEST for MC eNodeB, but NumberOfActiveCells=" + enodeBConfig.getNumberOfActiveCells(dut), Reporter.WARNING);
		}
	}

	
	
	/********************************* HELPER ********************************/
	
	private void testOutOfServiceInServiceExpectedBehavior(EnodeB eNodeB, CellIndex oosCellIndex, CellIndex isCellIndex) {
		int cells = enodeBConfig.getNumberOfActiveCells(eNodeB);
		synchronized (eNodeB.inServiceStateLock){
			eNodeB.expecteInServiceState = false;
		}
		if(cells > 1 || oosCellIndex == CellIndex.FORTY){
			report.report("Set Cell "+(oosCellIndex.value%40)+" to Out Of Service.");
			eNodeB.setServiceState(oosCellIndex, 0);
		}
		if(cells > 1 || isCellIndex == CellIndex.FORTY){
			report.report("Set Cell "+(isCellIndex.value%40)+" to In Service.");
			eNodeB.setServiceState(isCellIndex, 1);
		}
		GeneralUtils.unSafeSleep(5000);
		if(cells > 1 || oosCellIndex == CellIndex.FORTY){
			checkForOutOfServiceBehavior(eNodeB, oosCellIndex);
		}
		if(cells > 1 || isCellIndex == CellIndex.FORTY){
			checkForInServiceBehavior(eNodeB, isCellIndex);
		}
		
		if(cells > 1 || oosCellIndex == CellIndex.FORTY){
			report.report("Post Test: Set Cell "+(oosCellIndex.value%40)+" to In Service.");
			eNodeB.setServiceState(oosCellIndex, 1);
		}
		synchronized (eNodeB.inServiceStateLock){
			eNodeB.expecteInServiceState = true;
		}
	}
	
	public void checkForOutOfServiceBehavior(EnodeB eNodeB, CellIndex oosCellIndex){
		GeneralUtils.startLevel("Check for Out Of Service behavior for Cell "+ (oosCellIndex.value%40));
		if(!eNodeB.isInService(oosCellIndex.value)){
			report.report("****CELL "+(oosCellIndex.value%40)+" SERVICE STATE = 0 ****");
		}else{
			report.report("****CELL "+(oosCellIndex.value%40)+" SERVICE STATE = 1 ****", Reporter.FAIL);
			reason = "SERVICE STATE = 1.";
		}
		peripheralsConfig.stopStartUes(SetupUtils.getInstance().getallUEsPerEnodeb(eNodeB));
		if(areThereIsAnyUeConnected(eNodeB.getPci(oosCellIndex.value))){
			report.report("One or more UEs are connected to Cell " + (oosCellIndex.value%40), Reporter.FAIL);
			reason += "One or more UEs are connected.";
		}else{
			report.report("No UEs connected to Cell " + (oosCellIndex.value%40));
		}
		GeneralUtils.stopLevel();
	}
	
	public void checkForInServiceBehavior(EnodeB eNodeB, CellIndex isCellIndex){
		GeneralUtils.startLevel("Check for In Service behavior for cell "+ (isCellIndex.value%40));
		if(eNodeB.isInService(isCellIndex.value)){
			report.report("****CELL "+(isCellIndex.value%40)+" SERVICE STATE = 1 ****");
		}else{
			report.report("****CELL "+(isCellIndex.value%40)+" SERVICE STATE = 0 ****", Reporter.FAIL);
			reason = "SERVICE STATE = 0.";
		}
		peripheralsConfig.stopStartUes(SetupUtils.getInstance().getallUEsPerEnodeb(eNodeB));
		if(areThereIsAnyUeConnected(eNodeB.getPci(isCellIndex.value))){
			report.report("One or more UEs are connected to Cell " + (isCellIndex.value%40));
		}else{
			report.report("No UEs connected to Cell " + (isCellIndex.value%40), Reporter.FAIL);
			reason += "No UEs connected.";
		}
		GeneralUtils.stopLevel();
	}

	
	private boolean areThereIsAnyUeConnected(int pci){
		boolean hasAnyUeConnected = false;
		GeneralUtils.startLevel("UEs Measurements Status.");
		GeneralUtils.unSafeSleep(10000);
		for(Pair<UE, DMtool> p : ueDmLists){
			try {
				p.getElement1().init();
			} catch (Exception e) {
				continue;
			}
			
			sqnCellMeasReport measReport;
			try {
				measReport = p.getElement1().getMeas();
			} catch (Exception e) {
				report.report("Failed to get Measurements Report", Reporter.WARNING);
				e.printStackTrace();
				continue;
			}
			p.getElement1().close();
			printMeasReport(p.getElement0().getName(), measReport);
			if(measReport.servingMeas.id.id2 == pci){
				hasAnyUeConnected = true;
			}
		}
		GeneralUtils.stopLevel();
		return hasAnyUeConnected;
	}
	

	private void printMeasReport(String ueName, sqnCellMeasReport measReport) {
		GeneralUtils.startLevel(ueName +": Measurements Report");
		report.report("Connected to eNodeB with PCI=" + measReport.servingMeas.id.id2);
		report.report("Earfcn=" + measReport.servingMeas.id.id1);
		double rsrp = measReport.servingMeas.meas.meas1;
		rsrp /= 100;
		report.report("Rsrp=" + rsrp);
		GeneralUtils.stopLevel();
	}
}

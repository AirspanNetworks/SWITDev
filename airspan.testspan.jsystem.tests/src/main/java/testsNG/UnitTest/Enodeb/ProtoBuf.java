package testsNG.UnitTest.Enodeb;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import EnodeB.EnodeB;
import Netspan.EnbProfiles;
import Netspan.API.Enums.EnabledDisabledStates;
import Netspan.Profiles.MobilityParameters;
import Utils.SysObjUtils;
import jsystem.framework.ParameterProperties;
import jsystem.framework.TestProperties;
import testsNG.TestspanTest;
import testsNG.Actions.EnodeBConfig;

public class ProtoBuf extends TestspanTest {
	private EnodeB dut;	
	private EnodeBConfig enodeBConfig;
	
	
	@Override
	public void init() throws Exception {
		enbInTest = new ArrayList<EnodeB>();
		enbInTest.add(dut);
		enodeBConfig = EnodeBConfig.getInstance();
		super.init();
	}
	
	@Test
	@TestProperties(name = "getCounters", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {"IsTestWasSuccessful" })
	public void getCounters() throws Exception {
		changeMobility();
		esonCheck();
		revertMobilityProfiles(dut);
	}
	
	private void esonCheck() {
			List<Integer> dutPcis = getAllNodePCIs(dut);
			report.report(dut.getName() + " PCI: " + dutPcis);
			ArrayList<Integer> esonPcis = new ArrayList<Integer>();
			esonPcis.add(256);
			esonPcis.add(123);
			esonPcis.add(268);
			esonPcis.add(68);
			esonPcis.add(260);
			esonPcis.add(258);
			for(Integer dutPci : dutPcis){
				if(esonPcis.contains(dutPci)) {
					report.report("match pci : "+dutPci);
				}else {
					report.report("didn't match pci : "+dutPci);
				}
			}
	}

	private void changeMobility() {
		MobilityParameters mobilityParams = new MobilityParameters();
		mobilityParams.setThresholdBasedMeasurement(EnabledDisabledStates.ENABLED);
		mobilityParams.setThresholdBasedMeasurementDual(true);
		int numOfCellsInNode = dut.getNumberOfActiveCells();
		while(numOfCellsInNode > 0) {
			dut.setCellContextNumber(numOfCellsInNode);
			if(!enodeBConfig.cloneAndSetMobilityProfileViaNetSpan(dut, dut.getDefaultNetspanProfiles().getMobility(), mobilityParams)){
				report.report("did not changed mobility in dut "+dut.getNetspanName()+", on cell "+numOfCellsInNode);
			}else {
				report.report("changed mobility in dut "+dut.getNetspanName()+", on cell "+numOfCellsInNode);
			}
			numOfCellsInNode--;
		}
		
	}
	
	private void revertMobilityProfiles(EnodeB dutInTest2) {
		int numOfCells = dutInTest2.getNumberOfActiveCells();
		while(numOfCells > 0) {
			dutInTest2.setCellContextNumber(numOfCells);
			enodeBConfig.setProfile(dutInTest2, EnbProfiles.Mobility_Profile, dutInTest2.getDefaultNetspanProfiles().getMobility());
			numOfCells--;
		}
	}
	
	private List<Integer> getAllNodePCIs(EnodeB node) {
		List<Integer> result = new ArrayList<Integer>();
		int numOfCells = node.getNumberOfActiveCells();
		
		while(numOfCells > 0) {
			int cellOID = (numOfCells - 1) + 40;
			int cellPci = node.getPci(cellOID);
			result.add(cellPci);
			numOfCells--;
		}
		return result;
	}

	@ParameterProperties(description = "Name of Enb")
	public void setDUT(String dut) {
		ArrayList<EnodeB> temp=(ArrayList<EnodeB>)SysObjUtils.getInstnce().initSystemObject(EnodeB.class,false,dut);
		this.dut = temp.get(0);
	}
}

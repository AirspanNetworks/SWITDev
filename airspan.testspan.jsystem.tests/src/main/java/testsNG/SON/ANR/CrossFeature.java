package testsNG.SON.ANR;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.PatternSyntaxException;
import Attenuators.AttenuatorSet;
import DMTool.DMtool;
import DMTool.Measurement.MeasurementCfgReport;
import DMTool.Measurement.TriggerEvents;
import EnodeB.EnodeB;
import Netspan.NetspanServer;
import Netspan.API.Enums.EnbStates;
import UE.UE;
import Utils.SetupUtils;
import Utils.SysObjUtils;
import Utils.Snmp.MibReader;
import jsystem.framework.ParameterProperties;
import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;
import testsNG.TestspanTest;
import testsNG.Actions.AlarmsAndEvents;
import testsNG.Actions.EnodeBConfig;
import testsNG.Actions.Neighbors;
import testsNG.Actions.PeripheralsConfig;
import testsNG.Actions.Statuses;
import testsNG.Actions.Traffic;

public class CrossFeature extends TestspanTest {
	protected EnodeB dut;
	protected EnodeBConfig enodeBConfig;
	protected NetspanServer netspan;
	protected PeripheralsConfig peripheralsConfig;
	protected MibReader mibReader;
	protected Statuses status;
	protected Traffic traffic;
	protected InetAddress addr;
	protected ArrayList<UE> allUEs;
	protected Neighbors neighborsUtils;
	protected AlarmsAndEvents alarmsAndEvents;
	protected DMtool dm = new DMtool();
	private String attenuatorSetName = "rudat_set";
	private AttenuatorSet attenuatorSetUnderTest;
	private ArrayList<EnodeB> otherEnb;
	
	@Override
	public void init() throws Exception {
		netspanServer = NetspanServer.getInstance();
		enbInTest = new ArrayList<>();
		enbInTest.add(dut);
		super.init();
		enodeBConfig = EnodeBConfig.getInstance();
		netspan = NetspanServer.getInstance();
		mibReader = MibReader.getInstance();
		status = Statuses.getInstance();
		traffic = Traffic.getInstance(SetupUtils.getInstance().getAllUEs());
		addr = InetAddress.getLocalHost();
		neighborsUtils = Neighbors.getInstance();
		alarmsAndEvents = AlarmsAndEvents.getInstance();
		peripheralsConfig = PeripheralsConfig.getInstance();
		attenuatorSetUnderTest = AttenuatorSet.getAttenuatorSet(attenuatorSetName);
		allUEs = SetupUtils.getInstance().getAllUEs();
		
		if (!dut.isInService()) {
			report.report("eNodeB " + dut.getNetspanName() + " is Out Of Service", Reporter.WARNING);
		}
		otherEnb = (ArrayList<EnodeB>) enbInSetup.clone();
		otherEnb.remove(dut);
		for(EnodeB enb : otherEnb){
			try{
				peripheralsConfig.changeEnbState(enb, EnbStates.OUT_OF_SERVICE);
			}catch(Exception e){
				report.report("Failed to set "+enb.getNetspanName()+" out of service");
			}
		}
		try{
			peripheralsConfig.SetAttenuatorToMin(attenuatorSetUnderTest);
			report.report("Successfully set attenuator to minimum");
		}catch(Exception e){
			report.report("Failed to set minimum attenuation",Reporter.WARNING);
			reason = "Failed to set minimum attenuation";
		}
		traffic.startTraffic(allUEs);
		report.report("Start Traffic");
	}

	@ParameterProperties(description = "Name of Enb")
	public void setDUT(String dut) {
		ArrayList<EnodeB> temp = (ArrayList<EnodeB>) SysObjUtils.getInstnce().initSystemObject(EnodeB.class, false, dut);
		this.dut = temp.get(0);
	}

	//@Test // Cross Feature: Test 35 & 36
	@TestProperties(name = "ANR_Normal_State_Per_Cell", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful", "anrState", "isInterTest" })
	public void anrNormalStatePerCell() throws IOException {
		List<Integer> earfcnList = new ArrayList<Integer>();
		int inter = 39790; //TODO: need to change to inter frequency (EARFCN of enbTest)
		int intra = 39990; //TODO: need to change intra frequency
		earfcnList.add(inter);
		earfcnList.add(intra);
		enodeBConfig.configureOnlyANRtoEnableViaNms(dut, "Configure ANR to enable with inter("+inter+" EARFCN) and intra("+intra+" EARFCN) frequency", earfcnList);
		int numberOfActiveCells = dut.getNumberOfActiveCells();
		report.report(dut.getNetspanName() + " Number Of Active Cells: " + numberOfActiveCells);
		for (int i = 1; i <= numberOfActiveCells; i++) {
			report.startLevel("Test on Cell ID: " + i + " with PCI: "+dut.getPci(39+i));
			dut.setCellContextNumber(i);
			peripheralsConfig.stopUEs(allUEs);
			peripheralsConfig.startUEs(allUEs);
			enodeBConfig.waitForAnrState(dut, 3*60*1000, 1);
			enodeBConfig.showANRstate(dut, "ANR STATE = 1");
			anrNormalStateHelper();
			report.stopLevel();
		}
		enodeBConfig.revertToDefaultSonProfile(dut);
	}
	
	//@Test // Cross Feature: Test 37
	@TestProperties(name = "from_ANR_Steady_State_to_Normal_State", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful", "anrState", "isInterTest" })
	public void fromAnrSteadyStateToNormalState() throws IOException {
		List<Integer> earfcnList = new ArrayList<Integer>();
		int inter = 39790; //TODO: need to change to inter frequency (EARFCN of enbTest)
		int intra = 39990; //TODO: need to change intra frequency
		earfcnList.add(inter);
		earfcnList.add(intra);
		enodeBConfig.configureOnlyANRtoEnableViaNms(dut, "Configure ANR to enable with inter("+inter+" EARFCN) and intra("+intra+" EARFCN) frequency", earfcnList);
		int numberOfActiveCells = dut.getNumberOfActiveCells();
		report.report(dut.getNetspanName() + " Number Of Active Cells: " + numberOfActiveCells);
		report.report("Wait for ANR Steady State (TimeOut = 3 Minutes)");
		boolean flag = enodeBConfig.waitForAnrState(dut, 3*60*1000, 3);
		enodeBConfig.showANRstate(dut, "ANR STATE = 3");
		if(flag == false) {
			report.startLevel("Try again to get ANR Steady State (TimeOut = 3 Minutes)");
			peripheralsConfig.stopUEs(allUEs);
			peripheralsConfig.startUEs(allUEs);
			report.report("Wait for Steady State");
			flag = enodeBConfig.waitForAnrState(dut, 3*60*1000, 3);
			report.stopLevel();
			if(flag == false) {
				report.report("Can not get ANR Steady State", Reporter.WARNING);
				return;
			}
		}
		for (int i = 1; i <= numberOfActiveCells; i++) {
			report.startLevel("Test on Cell ID: " + i + " with PCI: "+dut.getPci(39+i));
			dut.setCellContextNumber(i);
			enodeBConfig.showANRstate(dut, "ANR STATE = 3");
			anrNormalStateHelper();
			report.stopLevel();
		}
		enodeBConfig.revertToDefaultSonProfile(dut);
	}
	
	//@Test // Cross Feature: Test 38 & 40
	@TestProperties(name = "ANR_Steady_State_Per_Cell", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful", "anrState", "isInterTest" })
	public void anrSteadyStatePerCell() throws IOException {

	}
	
	//@Test // Cross Feature: Test 39 & 40
	@TestProperties(name = "ANR_Steady_State_And_Change_NRT_for_Neighbor_Per_Cell", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful", "anrState", "isInterTest" })
	public void anrSteadyStateAndChangeNRTforNeighborPerCell() throws IOException {

	}

	private void anrNormalStateHelper() throws IOException {
		UE testUe = null;
		boolean flag = false;
		int enbPci = dut.getPci(39+dut.getCellContextID());
		
		for(UE ue : allUEs) {
			int ueContextPci = ue.getPCI();
			if(enbPci == ueContextPci) {
				report.report("UE " + ue.getLanIpAddress() + "[" + ue.getImsi() + "] Connected to PCI: "+ue.getPCI());
				testUe = ue;
				flag = true;
				break;
			}
		}
		if(flag == false) {
			report.report("No UEs connected to Cell "+dut.getCellContextID()+": "+dut.getNetspanName()+" (PCI = "+enbPci+")", Reporter.WARNING);
			return;
		}
		try {
			dm.setUeIP(testUe.getLanIpAddress());
			dm.setPORT(testUe.getDMToolPort());
			dm.init();
			ArrayList<MeasurementCfgReport> reportTable = dm.getMeasurementCfgReport();
			
			if(reportTable.isEmpty() || reportTable == null) {
				report.report("No Measurement Cfg Report Table from UE[" + testUe.getImsi() + "]", Reporter.FAIL);
			}
			for (MeasurementCfgReport elem : reportTable) {
				if ((TriggerEvents.EVENT_A1 == elem.getEvent_trigger())
						|| (TriggerEvents.EVENT_A2 == elem.getEvent_trigger())) {
					report.report(elem.getEvent_trigger().toString() + " configured", Reporter.FAIL);
				} else if (Integer.parseInt(elem.getInterval().replaceAll("ms", "")) > 1024) {
					report.report(elem.getEvent_trigger().value() + " configured, interval: " + elem.getInterval()
							+ "(Greater than one second)", Reporter.FAIL);
				} else {
					report.report(elem.getEvent_trigger().value() + " configured, interval: " + elem.getInterval());
				}
			}
		} catch (NumberFormatException | PatternSyntaxException e) {
			e.printStackTrace();
			report.report("Parse Interval failed due to " + e.getMessage(), Reporter.FAIL);
		} catch (Exception e) {
			e.printStackTrace();
			report.report("DMtool failed due to " + e.getMessage(), Reporter.FAIL);
		} finally {
			dm.close();
		}
	}
}

package testsNG.ProtocolsAndServices.HandOver;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Test;

import EnodeB.EnodeB;
import Netspan.API.Enums.EnbTypes;
import Netspan.API.Enums.SonAnrStates;
import Utils.GeneralUtils;
import Utils.SetupUtils;
import Utils.SysObjUtils;
import jsystem.framework.ParameterProperties;
import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;
import testsNG.TestspanTest;
import testsNG.Actions.EnodeBConfig;
import testsNG.Actions.PeripheralsConfig;

public class Progression extends TestspanTest {

	private EnodeBConfig enodeBConfig;
	private EnodeB dut1;
	private EnodeB dut2;
	private P0 HO = new P0();
	private testsNG.SON.ANR.P0 ANR = new testsNG.SON.ANR.P0();
	private boolean isInter;
	private SonAnrStates anrState;
	private boolean rebootRequired = false;

	@Override
	public void init() throws Exception {
		report.startLevel("Init Tests");
		enbInTest = new ArrayList<EnodeB>();
		enbInTest.add(dut1);
		enbInTest.add(dut2);
		super.init();
		HO.setDUT1(dut1.getName());
		HO.setDUT2(dut2.getName());
		HO.init();
		ANR.setDUT1(dut1.getName());
		ANR.setDUT2(dut2.getName());
		ANR.init();
		PeripheralsConfig.getInstance().startUEs(SetupUtils.getInstance().getAllUEs());
		enodeBConfig = EnodeBConfig.getInstance();
		report.stopLevel();
	}

	@Test
	@TestProperties(name = "Home to Macro, Intra freq, Periodical ANR, ECGI Based HO", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void HomeToMacroIntraPeriodicAnrEcgiBasedH0() throws Exception {
		isInter = false;
		anrState = SonAnrStates.PERIODICAL_MEASUREMENT;
		home2MacroPreTest();
		performEcgiHO();
		postTest();
	}

	@Test
	@TestProperties(name = "Home to Macro, Intra freq, HO Based ANR, ECGI Based HO", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void HomeToMacroIntraHOAnrEcgiBasedH0() throws Exception {
		isInter = false;
		anrState = SonAnrStates.HO_MEASUREMENT;
		home2MacroPreTest();
		performEcgiHO();
		postTest();
	}

	@Test
	@TestProperties(name = "Home to Macro, Inter freq, Periodical ANR, ECGI Based HO", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void HomeToMacroInterPeriodicAnrEcgiBasedH0() throws Exception {
		isInter = true;
		anrState = SonAnrStates.PERIODICAL_MEASUREMENT;
		home2MacroPreTest();
		performEcgiHO();
		postTest();
	}

	//@Test
	@TestProperties(name = "Home to Macro, Inter freq, No ANR, ECGI Based HO", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void HomeToMacroInterNoAnrEcgiBasedH0() throws Exception {
		isInter = true;
		anrState = SonAnrStates.DISABLED;
		home2MacroPreTest();
		performEcgiHO();
		postTest();
	}

	@Test
	@TestProperties(name = "Home to Macro, Intra freq, No ANR, ECGI Based HO", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void HomeToMacroIntraNoAnrEcgiBasedH0() throws Exception {
		isInter = false;
		anrState = SonAnrStates.DISABLED;
		home2MacroPreTest();
		performEcgiHO();
		postTest();
	}

	@Test
	@TestProperties(name = "Home to Macro, Inter freq, HO Based ANR, ECGI Based HO", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void HomeToMacroInterHOAnrEcgiBasedH0() throws Exception {
		isInter = true;
		anrState = SonAnrStates.HO_MEASUREMENT;
		home2MacroPreTest();
		performEcgiHO();
		postTest();
	}

	@Test
	@TestProperties(name = "Home to Macro, Intra freq, Periodical ANR, X2 HO", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void HomeToMacroIntraPeriodicAnrX2H0() throws Exception {
		isInter = false;
		anrState = SonAnrStates.PERIODICAL_MEASUREMENT;
		home2MacroPreTest();
		performX2HO();
		postTest();
	}

	@Test
	@TestProperties(name = "Home to Macro, Intra freq, HO Based ANR, X2 HO", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void HomeToMacroIntraHoAnrX2H0() throws Exception {
		isInter = false;
		anrState = SonAnrStates.HO_MEASUREMENT;
		home2MacroPreTest();
		performX2HO();
		postTest();
	}

	@Test
	@TestProperties(name = "Home to Macro, Inter freq, Periodical ANR, X2 HO", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void HomeToMacroInterPeriodicAnrX2H0() throws Exception {
		isInter = true;
		anrState = SonAnrStates.PERIODICAL_MEASUREMENT;
		home2MacroPreTest();
		performX2HO();
		postTest();
	}

	@Test
	@TestProperties(name = "Home to Macro, Inter freq, HO Based ANR, X2 HO", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void HomeToMacroInterHoAnrX2H0() throws Exception {
		isInter = true;
		anrState = SonAnrStates.HO_MEASUREMENT;
		home2MacroPreTest();
		performX2HO();
		postTest();
	}

	@Test
	@TestProperties(name = "Home to Macro, Intra freq, Periodical ANR, S1 HO", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void HomeToMacroIntraPeriodicAnrS1H0() throws Exception {
		isInter = false;
		anrState = SonAnrStates.PERIODICAL_MEASUREMENT;
		home2MacroPreTest();
		performS1HO();
		postTest();
	}

	@Test
	@TestProperties(name = "Home to Macro, Intra freq, HO Based ANR, S1 HO", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void HomeToMacroIntraHoAnrS1H0() throws Exception {
		isInter = false;
		anrState = SonAnrStates.HO_MEASUREMENT;
		home2MacroPreTest();
		performS1HO();
		postTest();
	}

	@Test
	@TestProperties(name = "Home to Macro, Inter freq, Periodical ANR, S1 HO", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void HomeToMacroInterPeriodicAnrS1H0() throws Exception {
		isInter = true;
		anrState = SonAnrStates.PERIODICAL_MEASUREMENT;
		home2MacroPreTest();
		performS1HO();
		postTest();
	}

	@Test
	@TestProperties(name = "Home to Macro, Inter freq, HO Based ANR, S1 HO", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void HomeToMacroInterHoAnrS1H0() throws Exception {
		isInter = true;
		anrState = SonAnrStates.HO_MEASUREMENT;
		home2MacroPreTest();
		performS1HO();
		postTest();
	}

	private void performEcgiHO() {
		try {
			report.startLevel("ECGI Based HO Test.");
			openEcgiLoggerThreshod();
			if (!anrState.equals(SonAnrStates.DISABLED)) {
				ANR.setAnrState(anrState);
				ANR.setIsInterTest(isInter);
				if (ANR.preTest())
					ANR.failToAddAnrNeighbor();
			}
			printDebugData();
			HO.skipAddNgh = true;
			HO.passCriteria.put("HoEcgiOutAtt", "HoEcgiOutAtt");
			HO.passCriteria.put("HoEcgiOutExcSucc", "HoEcgiOutExcSucc");
			HO.passCriteria.put("HoHomeMacroOutAtt", "HoHomeMacroOutAtt");
			HO.passCriteria.put("HoHomeMacroOutExcSucc", "HoHomeMacroOutExcSucc");
			if (isInter)
				HO.BasicHO_S1InterFrequency();
			else
				HO.BasicHO_S1IntraFrequency();
			returnEcgiHoTablesToDefault();
			if (!anrState.equals(SonAnrStates.DISABLED))
				ANR.postTest();
			report.stopLevel();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void performX2HO() {
		try {
			report.startLevel("X2 HO Test.");
			enodeBConfig.addPciRangesForNrtEnbConfig(dut1, dut2.getPci(), dut2.getPci(), true, 1, 1, dut2.getBand());
			enodeBConfig.addPciRangesForNrtEnbConfig(dut2, dut1.getPci(), dut1.getPci(), true, 1, 1, dut1.getBand());
			ANR.setAnrState(anrState);
			ANR.setIsInterTest(isInter);
			openEcgiLoggerThreshod();
			if (ANR.preTest())
				ANR.addAnrNeighbor();
			printDebugData();
			HO.skipAddNgh = true;
			HO.passCriteria.put("HoHomeMacroOutAtt", "HoHomeMacroOutAtt");
			HO.passCriteria.put("HoHomeMacroOutExcSucc", "HoHomeMacroOutExcSucc");
			if (isInter)
				HO.BasicHO_X2InterFrequency();
			else
				HO.BasicHO_X2IntraFrequency();
			returnEcgiHoTablesToDefault();
			ANR.postTest();
			report.stopLevel();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void performS1HO() {
		try {
			report.startLevel("S1 HO Test.");
			enodeBConfig.addPciRangesForNrtEnbConfig(dut1, dut2.getPci(), dut2.getPci(), true, 2, 1, dut2.getBand());
			enodeBConfig.addPciRangesForNrtEnbConfig(dut2, dut1.getPci(), dut1.getPci(), true, 2, 1, dut1.getBand());
			ANR.setAnrState(anrState);
			ANR.setIsInterTest(isInter);
			ANR.isTriggerS1 = true;
			openEcgiLoggerThreshod();
			if (ANR.preTest())
				ANR.addAnrNeighbor();
			printDebugData();
			HO.skipAddNgh = true;
			HO.passCriteria.put("HoHomeMacroOutAtt", "HoHomeMacroOutAtt");
			HO.passCriteria.put("HoHomeMacroOutExcSucc", "HoHomeMacroOutExcSucc");
			if (isInter)
				HO.BasicHO_S1InterFrequency();
			else
				HO.BasicHO_S1IntraFrequency();
			returnEcgiHoTablesToDefault();
			ANR.postTest();
			report.stopLevel();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void printDebugData() {
		try {
			report.startLevel("DEBUG DATA");
			GeneralUtils.reportHtmlLink(dut1.getName() + " - enb show ngh", dut1.lteCli("enb show ngh"));
			GeneralUtils.reportHtmlLink(dut2.getName() + " - enb show ngh", dut2.lteCli("enb show ngh"));
			GeneralUtils.reportHtmlLink(dut1.getName() + " - db get nghList", dut1.lteCli("db get nghList"));
			GeneralUtils.reportHtmlLink(dut2.getName() + " - db get nghList", dut2.lteCli("db get nghList"));
			GeneralUtils.reportHtmlLink(dut1.getName() + " - db get nghListStatus", dut1.lteCli("db get nghListStatus"));
			GeneralUtils.reportHtmlLink(dut2.getName() + " - db get nghListStatus", dut2.lteCli("db get nghListStatus"));
			GeneralUtils.reportHtmlLink(dut1.getName() + " - db get PciRangesForNrtEnb", dut1.lteCli("db get PciRangesForNrtEnb"));
			GeneralUtils.reportHtmlLink(dut2.getName() + " - db get PciRangesForNrtEnb", dut2.lteCli("db get PciRangesForNrtEnb"));
			GeneralUtils.reportHtmlLink(dut1.getName() + " - db get PciRangesHomeEnb", dut1.lteCli("db get PciRangesHomeEnb"));
			GeneralUtils.reportHtmlLink(dut2.getName() + " - db get PciRangesHomeEnb", dut2.lteCli("db get PciRangesHomeEnb"));
			GeneralUtils.reportHtmlLink(dut1.getName() + " - db get CellCfg", dut1.lteCli("db get CellCfg"));
			GeneralUtils.reportHtmlLink(dut2.getName() + " - db get CellCfg", dut2.lteCli("db get CellCfg"));
			report.stopLevel();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private boolean home2MacroPreTest() {
		// TODO Auto-generated method stub
		boolean actionState = true;
		try {
			report.startLevel("Configure Home vs Macro.");
			if (Integer.parseInt(dut1.getEnbType()) != EnbTypes.HOME.ordinal() || Integer.parseInt(dut2.getEnbType()) != EnbTypes.MACRO.ordinal()) {
				rebootRequired = true;
				actionState &= enodeBConfig.setEnbType(dut1, EnbTypes.HOME);
				actionState &= enodeBConfig.setEnbType(dut2, EnbTypes.MACRO);
				actionState &= enodeBConfig.performReProvision(dut1);
				actionState &= enodeBConfig.performReProvision(dut2);
				actionState &= rebootAndWaitForAllrunning(enbInTest);
			} else {
				report.report("Skipping reboot since the required configuration already exist.");
				report.report(String.format("%s is already configured as HOME.", dut1.getName()));
				report.report(String.format("%s is already configured as MACRO.", dut2.getName()));
			}
			actionState &= home2MacroSetPciRanges();
			report.stopLevel();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return actionState;
	}

	private boolean home2MacroSetPciRanges() {
		boolean actionState = true;
		actionState &= enodeBConfig.addPciRangesForNrtEnbConfig(dut1, 503, 503, true, 1, 0, 0);
		actionState &= enodeBConfig.addPciRangesForNrtEnbConfig(dut2, 503, 503, true, 1, 0, 0);
		actionState &= enodeBConfig.addPciRangeHomeEnbConfig(dut1, dut1.getPci(), dut1.getPci(), 0, 0);
		actionState &= enodeBConfig.addPciRangeHomeEnbConfig(dut2, dut1.getPci(), dut1.getPci(), 0, 0);
		return actionState;
	}

	private void openEcgiLoggerThreshod() {
		report.report("Setting ECGI_BASED_HO logger threshold to 0");
		dut1.setSessionLogLevel("ECGI_BASED_HO", "*", 0);
		dut2.setSessionLogLevel("ECGI_BASED_HO", "*", 0);
	}

	private boolean postTest() {
		// TODO Auto-generated method stub
		boolean actionState = true;
		if (rebootRequired) {
			try {
				report.startLevel("Return the setup to its default state.");
				actionState &= enodeBConfig.setEnbType(dut1, EnbTypes.MACRO);
				actionState &= enodeBConfig.setEnbType(dut2, EnbTypes.MACRO);
				actionState &= enodeBConfig.changeEnbCellPci(dut1,
						Integer.parseInt(dut1.getName().replaceAll("\\D+", "")));
				actionState &= enodeBConfig.changeEnbCellPci(dut2,
						Integer.parseInt(dut2.getName().replaceAll("\\D+", "")));
				actionState &= enodeBConfig.performReProvision(dut1);
				actionState &= enodeBConfig.performReProvision(dut2);
				rebootAndWaitForAllrunning(enbInTest);
				report.stopLevel();
				return actionState;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return actionState;
	}

	private boolean returnEcgiHoTablesToDefault() {
		boolean actionState = true;
		try {
			report.startLevel("Return ECGI HO pci ranges tables to default. ");
			actionState &= enodeBConfig.returnPciRangesConfigToDefault(dut1);
			actionState &= enodeBConfig.returnPciRangesConfigToDefault(dut2);
			report.stopLevel();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return actionState;
	}

	private Boolean rebootAndWaitForAllrunning(ArrayList<EnodeB> duts) {
		boolean status = true;
		for (EnodeB dut : duts) {
			status = dut.reboot();
			if (!status) {
				report.report("Reboot Failed", Reporter.FAIL);
				return false;
			}
		}
		GeneralUtils.unSafeSleep(30 * 1000);

		for (EnodeB dut : duts) {
			status = dut.waitForAllRunningAndInService(EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
		}
		int inserviceTime = (int) (EnodeB.WAIT_FOR_ALL_RUNNING_TIME / 1000 / 60);
		if (!status)
			report.report("Enodeb did not reach all running and in service during " + inserviceTime + " minutes",
					Reporter.WARNING);
		return status;
	}

	@ParameterProperties(description = "First DUT")
	public void setDUT1(String dut) {
		GeneralUtils.printToConsole("Load DUT1" + dut);
		this.dut1 = (EnodeB) SysObjUtils.getInstnce().initSystemObject(EnodeB.class, false, dut).get(0);
		GeneralUtils.printToConsole("DUT loaded" + this.dut1.getNetspanName());
	}

	@ParameterProperties(description = "Second DUT")
	public void setDUT2(String dut) {
		GeneralUtils.printToConsole("Load DUT2" + dut);
		this.dut2 = (EnodeB) SysObjUtils.getInstnce().initSystemObject(EnodeB.class, false, dut).get(0);
		GeneralUtils.printToConsole("DUT loaded" + this.dut2.getNetspanName());
	}

	public EnodeB getDut1() {
		return dut1;
	}

	public EnodeB getDut2() {
		return dut2;
	}
}

package testsNG.SON.DICIC;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Test;

import Attenuators.AttenuatorSet;
import EnodeB.EnodeB;
import Netspan.EnbProfiles;
import Netspan.API.Enums.EnabledDisabledStates;
import Netspan.API.Enums.HandoverType;
import Netspan.API.Enums.HoControlStateTypes;
import Netspan.API.Enums.SonAnrStates;
import Netspan.API.Enums.X2ControlStateTypes;
import Netspan.Profiles.SonParameters;
import UE.UE;
import Utils.GeneralUtils;
import Utils.SetupUtils;
import Utils.SysObjUtils;
import jsystem.framework.ParameterProperties;
import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;
import testsNG.TestspanTest;
import testsNG.Actions.EnodeBConfig;
import testsNG.Actions.Neighbors;
import testsNG.Actions.PeripheralsConfig;
import testsNG.Actions.Traffic;
import testsNG.SON.ANR.P0;

public class Progression extends TestspanTest {

	private String attenuatorSetName = "rudat_set";
	private EnodeB dut1;
	private EnodeB dut2;
	private P0 ANR = new P0();
	private EnodeBConfig enodeBConfig;
	private Neighbors neighbors;
	private Traffic traffic;
	private PeripheralsConfig peripheralsConfig;
	private AttenuatorSet attenuatorSetUnderTest;

	private boolean isUe;
	private boolean isX2;
	private boolean isRntp;
	private boolean isUnmanaged;
	private boolean isAnr = false;

	@Override
	public void init() throws Exception {
		GeneralUtils.startLevel("Init Tests");
		enodeBConfig = EnodeBConfig.getInstance();
		neighbors = Neighbors.getInstance();
		enbInTest = new ArrayList<EnodeB>();
		enbInTest.add(dut1);
		enbInTest.add(dut2);
		super.init();
		ANR.setDUT1(dut1.getName());
		ANR.setDUT2(dut2.getName());
		ANR.init();
		traffic = Traffic.getInstance(SetupUtils.getInstance().getAllUEs());
		peripheralsConfig = PeripheralsConfig.getInstance();
		attenuatorSetUnderTest = AttenuatorSet.getAttenuatorSet(attenuatorSetName);
		GeneralUtils.stopLevel();
	}

	@Test
	@TestProperties(name = "D-ICIC Static neighbor, no ue, no X2, no rntp", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void dicicStaticNghNoUeNoX2NoRntp() throws Exception {
		isUe = false;
		isX2 = false;
		isRntp = false;
		preTest();
		dicicTest();
		postTest();
	}

	@Test
	@TestProperties(name = "D-ICIC Static neighbor, no ue, no X2, rntp on", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void dicicStaticNghNoUeNoX2RntpOn() throws Exception {
		isUe = false;
		isX2 = false;
		isRntp = true;
		preTest();
		dicicTest();
		postTest();
	}

	@Test
	@TestProperties(name = "D-ICIC Static neighbor, no ue, X2 allowed, no rntp", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void dicicStaticNghNoUeX2AllowedNoRntp() throws Exception {
		isUe = false;
		isX2 = true;
		isRntp = false;
		preTest();
		dicicTest();
		postTest();
	}

	@Test
	@TestProperties(name = "D-ICIC Static neighbor, no ue, X2 allowed, rntp on", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void dicicStaticNghNoUeX2AllowedRntpOn() throws Exception {
		isUe = false;
		isX2 = true;
		isRntp = true;
		preTest();
		dicicTest();
		postTest();
	}

	@Test
	@TestProperties(name = "D-ICIC Static neighbor, ue connected, X2 allowed, no rntp", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void dicicStaticNghUeConectedX2AllowdNoRntp() throws Exception {
		isUe = true;
		isX2 = true;
		isRntp = false;
		preTest();
		dicicTest();
		postTest();
	}

	@Test
	@TestProperties(name = "D-ICIC Static neighbor, ue connected, X2 allowed, rntp on", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void dicicStaticNghUeConnectedX2AllowdRntpOn() throws Exception {
		isUe = true;
		isX2 = true;
		isRntp = true;
		preTest();
		dicicTest();
		postTest();
	}

	@Test
	@TestProperties(name = "D-ICIC Static neighbor, ue connected, no X2, no rntp", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void dicicStaticNghUeConectedNoX2NoRntp() throws Exception {
		isUe = true;
		isX2 = false;
		isRntp = false;
		preTest();
		dicicTest();
		postTest();
	}

	@Test
	@TestProperties(name = "D-ICIC Static neighbor, ue connected, no X2, rntp on", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void dicicStaticNghUeConnectedNoX2RntpOn() throws Exception {
		isUe = true;
		isX2 = false;
		isRntp = true;
		preTest();
		dicicTest();
		postTest();
	}

	@Test
	@TestProperties(name = "D-ICIC Periodic neighbor, no ue, no X2, no rntp", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void dicicPeriodicNghNoUeNoX2NoRntp() throws Exception {
		isAnr = true;
		isUe = false;
		isX2 = false;
		isRntp = false;
		preTest();
		dicicTest();
		postTest();
	}

	@Test
	@TestProperties(name = "D-ICIC Periodic neighbor, no ue, no X2, rntp on", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void dicicPeriodicNghNoUeNoX2RntpOn() throws Exception {
		isAnr = true;
		isUe = false;
		isX2 = false;
		isRntp = true;
		preTest();
		dicicTest();
		postTest();
	}

	@Test
	@TestProperties(name = "D-ICIC Periodic neighbor, no ue, X2 allowed, no rntp", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void dicicPeriodicNghNoUeX2AllowedNoRntp() throws Exception {
		isAnr = true;
		isUe = false;
		isX2 = true;
		isRntp = false;
		preTest();
		dicicTest();
		postTest();
	}

	@Test
	@TestProperties(name = "D-ICIC Periodic neighbor, no ue, X2 allowed, rntp on", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void dicicPeriodicNghNoUeX2AllowedRntpOn() throws Exception {
		isAnr = true;
		isUe = false;
		isX2 = true;
		isRntp = true;
		preTest();
		dicicTest();
		postTest();
	}

	@Test
	@TestProperties(name = "D-ICIC Periodic neighbor, ue connected, X2 allowed, no rntp", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void dicicPeriodicNghUeConectedX2AllowdNoRntp() throws Exception {
		isAnr = true;
		isUe = true;
		isX2 = true;
		isRntp = false;
		preTest();
		dicicTest();
		postTest();
	}

	@Test
	@TestProperties(name = "D-ICIC Periodic neighbor, ue connected, X2 allowed, rntp on", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void dicicPeriodicNghUeConnectedX2AllowdRntpOn() throws Exception {
		isAnr = true;
		isUe = true;
		isX2 = true;
		isRntp = true;
		preTest();
		dicicTest();
		postTest();
	}

	@Test
	@TestProperties(name = "D-ICIC Periodic neighbor, ue connected, no X2, no rntp", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void dicicPeriodicNghUeConectedNoX2NoRntp() throws Exception {
		isAnr = true;
		isUe = true;
		isX2 = false;
		isRntp = false;
		preTest();
		dicicTest();
		postTest();
	}

	@Test
	@TestProperties(name = "D-ICIC Periodic neighbor, ue connected, no X2, rntp on", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void dicicPeriodicNghUeConnectedNoX2RntpOn() throws Exception {
		isAnr = true;
		isUe = true;
		isX2 = false;
		isRntp = true;
		preTest();
		dicicTest();
		postTest();
	}

	//@Test
	@TestProperties(name = "D-ICIC Static neighbor, RNTP aging", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void dicicStaticNghRntpAging() throws Exception {
		isUe = true;
		isX2 = true;
		isRntp = true;
		preTest();
		if(dicicTest())
			rntpAgingTest();
		else
			report.report("DICIC test failed, skipping aging test.", Reporter.FAIL);
		postTest();
	}
	
	@Test
	@TestProperties(name = "D-ICIC Static neighbor, RSRP aging", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void dicicStaticNghRsrpAging() throws Exception {
		isUe = true;
		isX2 = false;
		isRntp = false;
		preTest();
		if(dicicTest())
			rsrpAgingTest();
		else
			report.report("DICIC test failed, skipping aging test.", Reporter.FAIL);
		postTest();
	}

	private boolean rntpAgingTest() {
		boolean actionState;
		int rntpAgingTimer = dut1.getRntpAgingTimer();
		enodeBConfig.setProfile(dut2, EnbProfiles.Son_Profile, dut2.defaultNetspanProfiles.getSON());
		GeneralUtils.unSafeSleep(rntpAgingTimer + 5000);
		actionState = enodeBConfig.verifyUnmanagedInterference(dut1.getNetspanName(), dut2.getPci());
		if (actionState)
			report.report("Unmanaged interference didn't change after RNTP aging timer expired, Test Failed.", Reporter.FAIL);
		else
			report.report("Unmanaged interference changed after RNTP aging timer expired.");
		return true;
	}

	private boolean rsrpAgingTest() {
		boolean actionState;
		int rsrpAgingTimer = dut1.getRsrpAgingTimer();
		stopUes();
		GeneralUtils.unSafeSleep(rsrpAgingTimer + 5000);
		actionState = enodeBConfig.verifyAvgRsrp(dut1.getNetspanName(), dut2.getNetspanName(), false);
		if (!actionState)
			report.report("Average RSRP didn't change after RSRP aging timer expired, Test Failed.", Reporter.FAIL);
		else
			report.report("Average RSRP changed after RSRP aging timer expired.");
		return true;
	}

	private boolean dicicTest() {
		try {
			for (int i = 0; i < 2; i++) {
				GeneralUtils.startLevel("Test configurations");
				boolean actionState = true;
				actionState &= addNeighbors();
				actionState &= enableDicic(dut1);
				if (isRntp)
					actionState &= enableDicic(dut2);
				if (isUe) {
					report.report("Starting traffic.");
					actionState &= traffic.startTraffic();
					startUes();
				}
				GeneralUtils.stopLevel();
				GeneralUtils.unSafeSleep(10000);
				if (verifyConfigurations() && actionState)
					return verifyDicicResults();
				else if (i == 1) {
					report.report("Configuration failed twice. Test Failed.", Reporter.FAIL);
					return false;
				} else
					report.report("Configuration failed. Trying again.", Reporter.WARNING);
			}
		} catch (Exception e) {
			e.printStackTrace();
			GeneralUtils.stopLevel();
			return false;
		}
		return true;
	}

	private boolean verifyConfigurations() {
		boolean actionState1 = true;
		boolean actionState2 = true;
		try {
			GeneralUtils.startLevel("Configuration verification");
			// verify ue connected
			if (isUe)
				actionState1 = peripheralsConfig.checkIfAllUEsAreConnectedToNode(SetupUtils.getInstance().getDynamicUEs(), dut1);
			else
				actionState1 = !peripheralsConfig.checkIfAllUEsAreConnectedToNode(SetupUtils.getInstance().getDynamicUEs(), dut1);
			String text = isUe ? "not " : "";
			if (!actionState1)
				report.report(String.format("Some Ues are %sconnected.", text), Reporter.WARNING);
			// verify Ngh & x2 state
			X2ControlStateTypes X2State = isX2 ? X2ControlStateTypes.AUTOMATIC : X2ControlStateTypes.NOT_ALLOWED;
			HandoverType hoType = isX2 ? HandoverType.TRIGGER_X_2 : HandoverType.S_1_ONLY;
			actionState2 = neighbors.verifyNeighborParametersOnlySNMP(dut1, dut2, HoControlStateTypes.ALLOWED, X2State,
					hoType, !isAnr, "0");
			if (!actionState2)
				report.report(
						"Neighbor verification failed, it is either missing on nghList or X2 not in the expected state.",
						Reporter.WARNING);
			GeneralUtils.stopLevel();
			return actionState1 && actionState2;
		} catch (Exception e) {
			e.printStackTrace();
			GeneralUtils.stopLevel();
			return false;
		}
	}

	private boolean verifyDicicResults() {
		boolean actionState = true;
		isUnmanaged = isUe ? !isRntp | !isX2 : false;
		GeneralUtils.startLevel("Test Results");
		report.report(String.format(
				"Expected Results: IsIcic - %s, Average RSRP bigger than -140 - %s, Unmanaged interference - %s.", isX2,
				isUe, isUnmanaged));
		actionState = enodeBConfig.verifyIsIcic(dut1.getNetspanName(), dut2.getNetspanName(), isX2);
		if (!actionState)
			report.report("IsIcic is different than expected, Test Failed.", Reporter.FAIL);
		actionState = enodeBConfig.verifyAvgRsrp(dut1.getNetspanName(), dut2.getNetspanName(), isUe);
		if (!actionState)
			report.report("Average RSRP is different than expected, Test Failed.", Reporter.FAIL);
		if (isUnmanaged) {
			actionState = enodeBConfig.verifyUnmanagedInterference(dut1.getNetspanName(), dut2.getPci());
			if (!actionState)
				report.report("Unmanaged interference is different than expected, Test Failed.", Reporter.FAIL);
		}
		printDebugData();
		GeneralUtils.stopLevel();
		return actionState;
	}

	private boolean addNeighbors() {
		boolean actionState = true;
		if (isAnr)
			actionState = addAnrNgh();
		else {
			X2ControlStateTypes X2State = isX2 ? X2ControlStateTypes.AUTOMATIC : X2ControlStateTypes.NOT_ALLOWED;
			HandoverType hoType = isX2 ? HandoverType.TRIGGER_X_2 : HandoverType.S_1_ONLY;
			GeneralUtils.startLevel("Add Neighbors.");
			actionState &= neighbors.addNeighbor(dut1, dut2, HoControlStateTypes.ALLOWED, X2State, hoType, true, "0");
			actionState &= neighbors.addNeighbor(dut2, dut1, HoControlStateTypes.ALLOWED, X2State, hoType, true, "0");
			if (!actionState)
				report.report(" Failed to add Neighbors.", Reporter.WARNING);
			GeneralUtils.stopLevel();
		}
		return actionState;
	}

	private boolean addAnrNgh() {
		boolean actionState = true;
		try {
			startUes();
			if (!isX2)
				enodeBConfig.addPciRangesForNrtEnbConfig(dut1, 0, 503, false, 2, 1, dut2.getBand());
			ANR.isTriggerS1 = !isX2;
			ANR.setAnrState(SonAnrStates.PERIODICAL_MEASUREMENT);
			ANR.setIsInterTest(false);
			ANR.preTest();
			actionState = ANR.addAnrNeighbor();
			peripheralsConfig.setAttenuatorSetValue(attenuatorSetUnderTest, 0);
			stopUes();
			// wait for timeout.
			return actionState;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean preTest() {
		boolean actionState = true;
		GeneralUtils.startLevel("Pre Test");
		openDicicLoggerThreshod();
		stopUes();
		if(!isAnr)
			peripheralsConfig.setAttenuatorSetValue(attenuatorSetUnderTest, 0);
		actionState &= neighbors.deleteAllNeighbors(dut1);
		actionState &= neighbors.deleteAllNeighbors(dut2);
		GeneralUtils.stopLevel();
		return actionState;
	}

	private void openDicicLoggerThreshod() {
		report.report("Setting DICIC logger threshold to 2");
		dut1.setSessionLogLevel("DICIC", "*", 2);
		dut2.setSessionLogLevel("DICIC", "*", 2);
		report.report("Setting ECGI_BASED_HO logger threshold to 0");
		dut1.setSessionLogLevel("ECGI_BASED_HO", "*", 0);
		dut2.setSessionLogLevel("ECGI_BASED_HO", "*", 0);
	}

	private void printDebugData() {
		GeneralUtils.startLevel("DEBUG DATA");
		GeneralUtils.reportHtmlLink(dut1.getName() + " - dicic show neighbors", dut1.lteCli("dicic show neighbors"));
		GeneralUtils.reportHtmlLink(dut1.getName() + " - dicic show configuration", dut1.lteCli("dicic show configuration"));
		GeneralUtils.reportHtmlLink(dut1.getName() + " - db get nghListStatus", dut1.lteCli("db get nghListStatus"));
		GeneralUtils.reportHtmlLink(dut1.getName() + " - db get nghList", dut1.lteCli("db get nghList"));
		GeneralUtils.stopLevel();
	}

	public boolean postTest() {
		boolean actionState = true;
		GeneralUtils.startLevel("Post Test");
		if (isAnr) {
			if (!isX2)
				enodeBConfig.returnPciRangesConfigToDefault(dut1);
			ANR.postTest();
		}
		enodeBConfig.setProfile(dut1, EnbProfiles.Son_Profile, dut1.defaultNetspanProfiles.getSON());
		if (isRntp)
			enodeBConfig.setProfile(dut2, EnbProfiles.Son_Profile, dut2.defaultNetspanProfiles.getSON());
		enodeBConfig.deleteClonedProfiles();
		actionState &= neighbors.deleteAllNeighbors(dut1);
		actionState &= neighbors.deleteAllNeighbors(dut2);
		traffic.stopTraffic();
		GeneralUtils.stopLevel();
		return actionState;
	}

	private boolean enableDicic(EnodeB enb) {
		boolean actionState = true;
		try {
			GeneralUtils.startLevel(enb.getName() + " Enable D-ICIC Feature.");
			SonParameters sonParams = new SonParameters();
			sonParams.setOptimizationMode(EnabledDisabledStates.ENABLED);
			sonParams.setIcicMode(EnabledDisabledStates.ENABLED);
			actionState &= enodeBConfig.setDicicState(enb, sonParams);
			if (!actionState)
				report.report(enb.getName() + " Failed to enable D-ICIC", Reporter.WARNING);
			GeneralUtils.stopLevel();
			return actionState;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	private void stopUes() {
		try {
			GeneralUtils.startLevel("Stop Ues.");
			for (UE ue : SetupUtils.getInstance().getAllUEs()) {
				ue.stop();
			}
			GeneralUtils.stopLevel();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void startUes() {
		try {
			GeneralUtils.startLevel("Start Ues.");
			for (UE ue : SetupUtils.getInstance().getAllUEs()) {
				ue.start();
			}
			GeneralUtils.unSafeSleep(10000);
			GeneralUtils.stopLevel();
		} catch (Exception e) {
			e.printStackTrace();
		}
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

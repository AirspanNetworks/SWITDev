package testsNG.SON.TPM;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import Attenuators.AttenuatorSet;
import DMTool.DMtool;
import DMTool.sqnCellMeasReport;
import EnodeB.EnodeB;
import Netspan.API.Enums.EnbStates;
import Netspan.API.Enums.HandoverType;
import Netspan.API.Enums.HoControlStateTypes;
import Netspan.API.Enums.X2ControlStateTypes;
import UE.UE;
import Utils.GeneralUtils;
import Utils.SetupUtils;
import Utils.GeneralUtils.CellIndex;
import Utils.SysObjUtils;
import jsystem.framework.ParameterProperties;
import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;
import testsNG.TestspanTest;
import testsNG.Actions.PeripheralsConfig;
import testsNG.Actions.Utils.ParallelCommandsThread;

/**
 * @author Avichai Yefet
 */ 
public class Progression extends TestspanTest{
	final long TPM_STATUS_TABLE_UPDATE_EVERY_15_MINUTES = 15 * 60 * 1000;
	private EnodeB dut, neighbor;
	private ParallelCommandsThread syncCommands;
	private AttenuatorSet attenuatorSetUnderTest;
	
	@Override
	public void init() throws Exception {
		enbInTest = new ArrayList<EnodeB>();
		enbInTest.add(dut);
		super.init();
		dut.lteCli("logger threshold set client=WR_TPM cli=0");
		attenuatorSetUnderTest = AttenuatorSet.getAttenuatorSet("rudat_set");
		report.report("Set the attenuators default value : " + attenuatorSetUnderTest.getDefaultValueAttenuation()+ " [dB]\n");
		attenuatorSetUnderTest.setAttenuation(attenuatorSetUnderTest.getDefaultValueAttenuation());
		report.report("Delete All Neighbors.");
		netspanServer.deleteAllNeighbors(dut);
		report.report("Enable TPM and TPMMO.");
		dut.setTPMEnable(true, CellIndex.FORTY);
		dut.setTPMMoEnable(true, CellIndex.FORTY);
		startParallelCommands();
	}

	@Override
	public void end() {
		report.report("Disable TPM and TPMMO.");
		dut.setTPMMoEnable(false, CellIndex.FORTY);
		dut.setTPMEnable(false, CellIndex.FORTY);
		dut.lteCli("logger threshold set client=WR_TPM cli=4");
		stopParallelCommands();
		super.end();
	}
	
	@ParameterProperties(description = "DUT")
	public void setDut(String dut) {
		GeneralUtils.printToConsole("Load DUT " + dut);
		this.dut = (EnodeB) SysObjUtils.getInstnce().initSystemObject(EnodeB.class, false, dut).get(0);
		GeneralUtils.printToConsole("DUT loaded" + this.dut.getName());
	}
	
	@ParameterProperties(description = "Neighbor")
	public void setNeighbor(String neighbor) {
		GeneralUtils.printToConsole("Load Neighbor " + neighbor);
		this.neighbor = (EnodeB) SysObjUtils.getInstnce().initSystemObject(EnodeB.class, false, neighbor).get(0);
		GeneralUtils.printToConsole("Neighbor loaded" + this.neighbor.getName());
	}
	
	/***************** Tests ********************/
	
	@Test
	@TestProperties(name = "TPM-MO re-initialization",
	returnParam = { "IsTestWasSuccessful"},
	paramsExclude = {"IsTestWasSuccessful", "Neighbor"})
	public void tpmMoInitialization() {
		GeneralUtils.startLevel("Performing TPM-MO initialization"); 
		report.report("Set TPMMo=1, TPMNLPeriodic=10, TPMCycleMultiple=1, TPMServRsrpRlfThresh=-50, TPMNghRsrpRlfThresh=-50.");
		dut.setTpmMo(1, CellIndex.FORTY);
		dut.setTpmNlPeriodic(10, CellIndex.FORTY);
		dut.setTpmCycleMultiple(1, CellIndex.FORTY);
		dut.setTPMServRsrpRlfThresh(-50, CellIndex.FORTY);
		dut.setTPMNghRsrpRlfThresh(-50, CellIndex.FORTY);
		GeneralUtils.unSafeSleep(5 *1000);
		long startTime = System.currentTimeMillis();
		report.report("Expected Results: TPMMoPwrAdj=10");
		GeneralUtils.stopLevel();
		
		report.report("TPM Status table is updating every 15 Minutes (waiting for update).");
		
		GeneralUtils.startLevel("TPM-MO initialization Results:");
		int tpmMoPwrAdj = 0;
		while(tpmMoPwrAdj != 10 && ((System.currentTimeMillis() - startTime) < TPM_STATUS_TABLE_UPDATE_EVERY_15_MINUTES)){
			tpmMoPwrAdj = dut.getTpmMoPwrAdj(CellIndex.FORTY);
		}
		report.report("Results: TPMMoPwrAdj="+tpmMoPwrAdj);
		if(tpmMoPwrAdj == 10){
			report.step("***PASS***");
		}else{
			report.report("***FAIL***", Reporter.FAIL);
			reason = "TPMMoPwrAdj NOT equals 10.";
		}
		GeneralUtils.stopLevel();
	}
	
	@Test
	@TestProperties(name = "TPM - Test Counters after Forward HO.",
	returnParam = { "IsTestWasSuccessful"},
	paramsExclude = {"IsTestWasSuccessful"})
	public void testTpmCountersAfterForwardHO() {
		if(!netspanServer.addNeighbor(dut, neighbor, HoControlStateTypes.ALLOWED, X2ControlStateTypes.AUTOMATIC, HandoverType.TRIGGER_X_2, true, "0")){report.report("Faild to add neighbor.", Reporter.WARNING);}
		GeneralUtils.startLevel("Performing Forward HO");
		final int intraHoTooLatePoorCoverageOriginal = dut.GetIntraHoTooLatePoorCoverage(CellIndex.FORTY);
		final int intraHoTooLateGoodCoverageUnpreparedOriginal = dut.GetIntraHoTooLateGoodCoverageUnprepared(CellIndex.FORTY);
		report.report("Performing Forward HO");
		int attFwHoValue = (attenuatorSetUnderTest.getMaxAttenuation() - attenuatorSetUnderTest.getDefaultValueAttenuation()) 
							> (attenuatorSetUnderTest.getDefaultValueAttenuation() - attenuatorSetUnderTest.getMinAttenuation())? 
									attenuatorSetUnderTest.getMaxAttenuation() :
										attenuatorSetUnderTest.getMinAttenuation();
		report.report("Set the attenuators for performing Forward HO to: " + attFwHoValue+ " [dB]\n");
		attenuatorSetUnderTest.setAttenuation(attFwHoValue);
		GeneralUtils.unSafeSleep(5 *1000);
		long startTime = System.currentTimeMillis();
		report.report("Expected Results: increment of one of the counters: IntraHoTooLatePoorCoverage="+intraHoTooLatePoorCoverageOriginal+", IntraHoTooLateGoodCoverageUnprepared="+intraHoTooLateGoodCoverageUnpreparedOriginal);
		GeneralUtils.stopLevel();
		
		report.report("TPM Status table is updating every 15 Minutes (waiting for update).");
		
		GeneralUtils.startLevel("Forward HO Results:");
		int intraHoTooLatePoorCoverage = dut.GetIntraHoTooLatePoorCoverage(CellIndex.FORTY);
		int intraHoTooLateGoodCoverageUnprepared = dut.GetIntraHoTooLateGoodCoverageUnprepared(CellIndex.FORTY);
		while(intraHoTooLatePoorCoverage <= intraHoTooLatePoorCoverageOriginal &&
				intraHoTooLateGoodCoverageUnprepared <= intraHoTooLateGoodCoverageUnpreparedOriginal &&
				((System.currentTimeMillis() - startTime) < TPM_STATUS_TABLE_UPDATE_EVERY_15_MINUTES)){
			intraHoTooLatePoorCoverage = dut.GetIntraHoTooLatePoorCoverage(CellIndex.FORTY);
			intraHoTooLateGoodCoverageUnprepared = dut.GetIntraHoTooLateGoodCoverageUnprepared(CellIndex.FORTY);
		}
		report.report("Results: IntraHoTooLatePoorCoverage="+intraHoTooLatePoorCoverage+", IntraHoTooLateGoodCoverageUnprepared="+intraHoTooLateGoodCoverageUnprepared);
		if(intraHoTooLatePoorCoverage > intraHoTooLatePoorCoverageOriginal ||
				intraHoTooLateGoodCoverageUnprepared > intraHoTooLateGoodCoverageUnpreparedOriginal){
			report.step("***PASS***");
		}else{
			report.report("***FAIL***", Reporter.FAIL);
			reason = "The counters didn't increase.";
		}
		GeneralUtils.stopLevel();
	}

	@Test
	@TestProperties(name = "TPM - Test Counters after 3 Normal HOs",
	returnParam = { "IsTestWasSuccessful"},
	paramsExclude = {"IsTestWasSuccessful"})
	public void testTpmCountersAfterNormalHOs() {
		if(!netspanServer.addNeighbor(dut, neighbor, HoControlStateTypes.ALLOWED, X2ControlStateTypes.AUTOMATIC, HandoverType.TRIGGER_X_2, true, "0")){report.report("Faild to add neighbor.", Reporter.WARNING);}
		GeneralUtils.startLevel("Performing 3 times regular HOs");
		
		int outgoingIntraFreqHoAttemptOriginal = dut.getOutgoingIntraFreqHoAttempt(CellIndex.FORTY);
		int activeRfCfgTxPowerOriginal = dut.getRfStatusAchievedTxPower(dut.getCellContextID());
		int tpmDelMoIncOriginal = dut.getTPMDelMoInc(CellIndex.FORTY);
		int tpmDelMoDecOriginal = dut.getTPMDelMoDec(CellIndex.FORTY);
		report.report("TPM OutgoingIntraFreqHoAttempt="+outgoingIntraFreqHoAttemptOriginal+", ActiveRfCfg TxPower="+activeRfCfgTxPowerOriginal+
				", TPM TPMDelMoInc="+tpmDelMoIncOriginal+", TPMDelMoDec="+tpmDelMoDecOriginal);
		report.report("Performing 3 times regular HOs");
		final int NUMBER_OF_HOs = 3;
		performHOs(NUMBER_OF_HOs, attenuatorSetUnderTest.getDefaultValueAttenuation());
		GeneralUtils.unSafeSleep(5 *1000);
		long startTime = System.currentTimeMillis();
		report.report("Expected Results: 1. increment of OutgoingIntraFreqHoAttempt, 2. ActiveRfCfg TxPower increase by TPMDelMoInc or decrease by TPMDelMoDec.");
		GeneralUtils.stopLevel();
		
		report.report("TPM Status table is updating every 15 Minutes (waiting for update).");
		
		GeneralUtils.startLevel("3 times regular HOs Results:");
		int outgoingIntraFreqHoAttempt = dut.getOutgoingIntraFreqHoAttempt(CellIndex.FORTY);
		int activeRfCfgTxPower = dut.getRfStatusAchievedTxPower(dut.getCellContextID());
		while((outgoingIntraFreqHoAttempt <= outgoingIntraFreqHoAttemptOriginal ||
				activeRfCfgTxPower == activeRfCfgTxPowerOriginal) &&
				((System.currentTimeMillis() - startTime) < TPM_STATUS_TABLE_UPDATE_EVERY_15_MINUTES)){
			outgoingIntraFreqHoAttempt = dut.getOutgoingIntraFreqHoAttempt(CellIndex.FORTY);
			activeRfCfgTxPower = dut.getRfStatusAchievedTxPower(dut.getCellContextID());
		}
		report.report("Results: 1. OutgoingIntraFreqHoAttempt="+outgoingIntraFreqHoAttempt+", 2. ActiveRfCfg TxPower="+activeRfCfgTxPower);
		if((outgoingIntraFreqHoAttempt >= outgoingIntraFreqHoAttemptOriginal + NUMBER_OF_HOs) &&
				((activeRfCfgTxPower == activeRfCfgTxPowerOriginal + tpmDelMoIncOriginal) || 
				(activeRfCfgTxPower == activeRfCfgTxPowerOriginal - tpmDelMoDecOriginal))){
			report.step("***PASS***");
		}else{
			report.report("***FAIL***", Reporter.FAIL);
			reason = "One the counters didn't change.";
		}
		GeneralUtils.stopLevel();
	}
	
	@Test
	@TestProperties(name = "TPM ReEstablishment Same Cell Poor Coverage",
	returnParam = { "IsTestWasSuccessful"},
	paramsExclude = {"IsTestWasSuccessful","Neighbor"})
	public void reEstablishmentSameCellPoorCoverage() {
		for(EnodeB enb : enbInSetup){
			if(enb != dut){
				PeripheralsConfig.getInstance().changeEnbState(enb, EnbStates.OUT_OF_SERVICE);
			}
		}
		ArrayList<UE> allUEsArray = new ArrayList<>();
		allUEsArray.addAll(SetupUtils.getInstance().getStaticUEs(dut));
		allUEsArray.addAll(SetupUtils.getInstance().getDynamicUEs());
		ArrayList<DMtool> dmToolArray = new ArrayList<DMtool>();
		int dutPci = dut.getPci();
		report.report("Stopping and Starting UEs.");
		for(UE ue : allUEsArray){
			ue.stop();
			ue.start();
			DMtool dm = new DMtool();
			dm.setUeIP(ue.getLanIpAddress());
			dm.setPORT(ue.getDMToolPort());
			try {
				dm.init();
			} catch (Exception e) {
				GeneralUtils.printToConsole("Error with dmTool.init() due to: " + e.getMessage());
				e.printStackTrace();
			}
			if(isUeConnectedToPci(dm, dutPci)){
				dmToolArray.add(dm);
			}else{
				ue.stop();
			}
		}
		GeneralUtils.unSafeSleep(10 * 1000);
		int reEstablishmentSameCellPoorCoverageOriginal = dut.getReEstablishmentSameCellPoorCoverage(CellIndex.FORTY);
		report.report("reEstablishmentSameCellPoorCoverage="+reEstablishmentSameCellPoorCoverageOriginal);
		
		GeneralUtils.startLevel("Performing ReEstablishment of All UEs.");
		report.report("force Restablishment UEs");
		for(DMtool dm : dmToolArray){
			try {
				dm.cli("forceReestab");
			} catch (Exception e) {
				GeneralUtils.printToConsole("Error with dmTool.cli(forceReestab) due to: " + e.getMessage());
				e.printStackTrace();
			}
		}
		report.report("Expected Result: reEstablishmentSameCellPoorCoverage increase by " + dmToolArray.size());
		GeneralUtils.stopLevel();
		
		report.report("TPM Status table is updating every 15 Minutes (waiting for update).");
		
		GeneralUtils.startLevel("ReEstablishment UEs Results:");
		int reEstablishmentSameCellPoorCoverage = reEstablishmentSameCellPoorCoverageOriginal;
		long startTime = System.currentTimeMillis();
		while((reEstablishmentSameCellPoorCoverage <= reEstablishmentSameCellPoorCoverageOriginal) && ((System.currentTimeMillis() - startTime) < TPM_STATUS_TABLE_UPDATE_EVERY_15_MINUTES)){
			GeneralUtils.unSafeSleep(5 * 1000);
			reEstablishmentSameCellPoorCoverage = dut.getReEstablishmentSameCellPoorCoverage(CellIndex.FORTY);
		}
		GeneralUtils.unSafeSleep(10 * 1000);
		reEstablishmentSameCellPoorCoverage = dut.getReEstablishmentSameCellPoorCoverage(CellIndex.FORTY);
		report.report("Result: reEstablishmentSameCellPoorCoverage=" + reEstablishmentSameCellPoorCoverage);
		if(reEstablishmentSameCellPoorCoverage == reEstablishmentSameCellPoorCoverageOriginal + dmToolArray.size()){
			report.step("*** PASS ***");
		}else{
			report.report("*** FAIL ***", Reporter.FAIL);
			reason = "reEstablishmentSameCellPoorCoverage counter NOT equals" + (reEstablishmentSameCellPoorCoverageOriginal + dmToolArray.size());
		}
		GeneralUtils.stopLevel();
		
		for(EnodeB enb : enbInSetup){
			if(enb != dut){
				PeripheralsConfig.getInstance().changeEnbState(enb, EnbStates.IN_SERVICE);
			}
		}
		for(UE ue : allUEsArray){
			ue.start();
		}
	}
	
	private boolean isUeConnectedToPci(DMtool dm, int pci) {
		sqnCellMeasReport measReport;
		try {
			measReport = dm.getMeas();
			if(measReport.servingMeas.id.id2 == pci){
				return true;
			}
		} catch (Exception e) {
			GeneralUtils.printToConsole("FAILED to get Measurements from UE."); 
			e.printStackTrace();
		}
		return false;
	}

	/***************** Helper Functions ********************/
	
	private void performHOs(int numberOfHOs, int startingAttenuation) {
		int endAttenuation = attenuatorSetUnderTest.getMaxAttenuation() == startingAttenuation? attenuatorSetUnderTest.getMinAttenuation() : attenuatorSetUnderTest.getMaxAttenuation();
		PeripheralsConfig peripheralsConfig = PeripheralsConfig.getInstance();
		for(int i = 1; i <= numberOfHOs; i++){
			peripheralsConfig.moveAtt(attenuatorSetUnderTest, startingAttenuation, endAttenuation);
			startingAttenuation += endAttenuation;
			endAttenuation = startingAttenuation - endAttenuation;
			startingAttenuation = startingAttenuation - endAttenuation;
		}
	}
	
	/***************** Nested Classes ********************/
	
	
	
	/***************** Parallel Commands ********************/
	
	private void startParallelCommands(){
		report.report("Starting parallel commands");
		List<String> commandList = new ArrayList<String>();
		commandList.add("cell show operationalStatus");
		commandList.add("system show compilationtime");
		commandList.add("db get tpmCellCfg");
		commandList.add("db get tpmStatus");
		commandList.add("tpm show all_counters");
		commandList.add("db get rfStatus");
		commandList.add("db get activeRfCfg");
		commandList.add("db get PTPStatus");
		
		try {
			syncCommands = new ParallelCommandsThread(commandList, dut, null, null);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		syncCommands.start();
	}
	
	private void stopParallelCommands() {
		report.report("Stopping Parallel Commands");
		try {
			syncCommands.stopCommands();
			syncCommands.moveFileToReporterAndAddLink();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
}

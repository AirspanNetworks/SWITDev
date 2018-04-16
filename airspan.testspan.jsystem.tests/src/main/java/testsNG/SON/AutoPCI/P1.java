package testsNG.SON.AutoPCI;

import java.util.List;

import org.junit.Test;

import EnodeB.EnodeB;
import Netspan.API.Enums.EnabledDisabledStates;
import Netspan.API.Lte.RFStatus;
import Netspan.API.Lte.SONStatus;
import Utils.GeneralUtils;
import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;

public class P1 extends AutoPCIBase {

	// @Test
	@TestProperties(name = "Reallocation_RESOLVE_Failure", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void reallocationResolveFailure() {
	}

	// @Test
	@TestProperties(name = "Collision_Handling_'Out_Of_Service'", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void collisionHandlingOutOfService() {
	}

	// @Test
	@TestProperties(name = "Collision_Handling_And_ENB_Will_Draw_To_The_Lowest_PCI ", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void collisionHandlingAndEnbWillDrawToTheLowestPci() {
	}

	// @Test
	@TestProperties(name = "Change_Auto_PCI_During_Emergency_Call(1)", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void changeAutoPciDuringEmergencyCall1() {
	}

	// @Test
	@TestProperties(name = "PCI_Collision_Warm_Reset_Effect_On_PCI", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void pciCollisionWarmResetEffectOnPci() {
	}

	// @Test
	@TestProperties(name = "Auto_PCI_Allocation_Failed_OOS", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void autoPciAllocationFailedOOS() {
	}

	// @Test
	@TestProperties(name = "Auto_PCI_Recovery_From_OOS", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void autoPciRecoveryFromOOS() {
	}

	// @Test
	@TestProperties(name = "Auto_PCI_Allocation_Failed_OOS_With_2nd_Degree_NGH", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void autoPciAllocationFailedOosWith2ndDegreeNGH() {
	}

	// @Test
	@TestProperties(name = "Change_From_Static_To_Auto_PCI_Value_Inside_The_Range", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void changeFromStaticToAutoPciValueInsideTheRange() {
	}

	// @Test
	@TestProperties(name = "Change_From_Auto_PCI_To_Static_PCI_Value_Outside_The_Range", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void changeFromAutoPciToStaticPciValueOutsideTheRange() {
	}

	@Test
	@TestProperties(name = "ANR_Based_PCI_Allocation_No_UE_Connected", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void anrBasedPciAllocationNoUeConnected() {
		Integer pciStart = generatePciStart(300);
		Integer pciEnd = generatePciEnd(302);
		peripheralsConfig.stopUEs(testUeList);

		SONStatus readerSONStatus;

		int interEarfcn = testConfig.getInterEarfcn();
		int defaultEarfcn = testConfig.getDefaultEarfcn();
		if (dut.getEarfcn() != interEarfcn) {
			report.report(dut.getName() + " Earfcn is different from expected " + interEarfcn + ". stop test", Reporter.FAIL);
			return;
		}
		changeTxPowerViaNms(14);
		configureInitListSizeOnEnbAdvancedConfigurationProfile(START_POWER_ADVANCED_PROFILE, 1, 20, 100, EnabledDisabledStates.ENABLED, 2);

		configureOnlyANRtoEnableViaNms(interEarfcn, defaultEarfcn);

		report.report("Wait 2 minutes");
		GeneralUtils.unSafeSleep(1000 * 60 * 2);
		enodeBConfig.changeEnbCellPci(dut, pciStart);
		configurePciRangeAndANR(pciStart, pciEnd, interEarfcn, defaultEarfcn);

		dut.setExpectBooting(true);

		report.report("Wait 1 minute");
		GeneralUtils.unSafeSleep(1000 * 60);

		report.report(dut.getNetspanName() + " Wait for all running and in service (TimeOut=15 Minutes)");
		dut.waitForAllRunningAndInService(EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
		enodeBConfig.printEnodebState(dut, true);

		readerSONStatus = status.getSONStatus(dut);
		printSONStatus(readerSONStatus, "Automatic");
		boolean equalsPciStart = readerSONStatus.PCICells.get(0).physicalCellId.equals(pciStart);
		boolean equalsPciStartPlus1 = readerSONStatus.PCICells.get(0).physicalCellId.equals(pciStart+1);
		
		if (equalsPciStart || equalsPciStartPlus1) {
			report.report("PCI: " + readerSONStatus.PCICells.get(0).physicalCellId + ", as expected.");			
		}
		else {
			report.report("PCI: " + readerSONStatus.PCICells.get(0).physicalCellId + "(Should be "+pciStart+" or "+(pciStart+1)+")",
					Reporter.FAIL);
			reason = "Enb did not get PCI from Initial Pci List Size";
		}

		peripheralsConfig.checkIfAllUEsAreConnectedToNode(testUeList, dut);

		waitForGetTxPowerFromNetspan(10 * 1000 * 60);

		GeneralUtils.startLevel("Verify Tx power is starting from 'low power' and every interval Tx is raising by a step");
		long timeOut = (15 * 1000 * 60);
		long startTime = System.currentTimeMillis();
		int txPowerIndex = 2;
		while ((System.currentTimeMillis() - startTime) < timeOut) {
			List<RFStatus> rfs = status.getRFStatus(dut);
			if ((!rfs.isEmpty() && rfs.get(0).ActualTxPowerDbm == txPowerIndex)
					&& (rfs.get(1).ActualTxPowerDbm == txPowerIndex)) {
				report.report(rfs.get(0).RfNumber + " Actual Tx Power: " + rfs.get(0).ActualTxPower + ", "
						+ rfs.get(1).RfNumber + " Actual Tx Power: " + rfs.get(1).ActualTxPower);
				txPowerIndex++;
			}
			if (!rfs.isEmpty() && (rfs.get(0).ActualTxPowerDbm == 14) && (rfs.get(1).ActualTxPowerDbm == 14)) {
				report.report(rfs.get(0).RfNumber + " Actual Tx Power: " + rfs.get(0).ActualTxPower + ", "
						+ rfs.get(1).RfNumber + " Actual Tx Power: " + rfs.get(1).ActualTxPower);
				report.report("Tx Power is NOT raising by a step", Reporter.FAIL);
				break;
			}
			if (((System.currentTimeMillis() - startTime) > (1000 * 60 * 3)) && (txPowerIndex == 2)) {
				if (!rfs.isEmpty()) {
					report.report(rfs.get(0).RfNumber + " Actual Tx Power: " + rfs.get(0).ActualTxPower + ", "
							+ rfs.get(1).RfNumber + " Actual Tx Power: " + rfs.get(1).ActualTxPower);
				}
				report.report("Tx Power is NOT raising by a step", Reporter.FAIL);
				break;
			}
		}
		GeneralUtils.stopLevel();

		report.report("Wait 2 minutes");
		GeneralUtils.unSafeSleep(1000 * 60 * 2);

		readerSONStatus = status.getSONStatus(dut);
		printSONStatus(readerSONStatus, "Automatic");
		if (readerSONStatus.PCICells.get(0).physicalCellId != pciStart+2) {
			report.report("PCI: " + readerSONStatus.PCICells.get(0).physicalCellId + "(Should be "+(pciStart+2)+")", Reporter.FAIL);
			reason = "Enb did not end with a right PCI number";
		}

		revertToDefaultAdvancedProfile(dut);
		revertToDefaultSonProfile(dut);
		revertToDefaultRadioProfile(dut);
		deleteClonedProfile(dut);

		peripheralsConfig.startUEs(testUeList);
	}

	// @Test
	@TestProperties(name = "Allocation_Algorithm_With_256_1st_Degree_NGH_ENB_Neighbor_Configuration", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void allocationAlgorithmWith256st1DegreeNghEnbNeighborConfiguration() {
	}

	// @Test
	@TestProperties(name = "Verify_Auto_PCI_Functionality_As_Auto_RSI_And_ANR_Are_Enabled", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void verifyAutoPciFunctionalityAsAutoRsiAndAnrAreEnabled() {
	}
}
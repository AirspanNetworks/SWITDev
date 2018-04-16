package testsNG.PlatformAndSecurity.PowerControl;

import org.junit.Test;

import EnodeB.EnodeB.Architecture;
import Netspan.EnbProfiles;
import Netspan.API.Enums.EnbStates;
import Netspan.Profiles.RadioParameters;
import UE.UE;
import Utils.GeneralUtils;
import Utils.SetupUtils;
import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;
import testsNG.Actions.Traffic.TrafficType;

/**
 * @author Meital Mizrachi
 */

public class P1 extends PowerControlBase {

	@Test // 4
	@TestProperties(name = "Validation over max power", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void validationWrongValueMaxTxPlus1PowerSendFromNmsAndReceivedOnEnb() {
		if (!(dut.getArchitecture() == Architecture.XLP)) {
			report.report("The Enb is not XLP", Reporter.WARNING);
			reason = "The Enb is not XLP";
			return;
		}
		dut.lteCli("logger threshold set process=rfMgr cli=0");
		String productCode = enbConfig.getNodeInfo(dut).productCode;
		report.report("Read Tx max power of product from CSV file");
		int txMaxPower = enbConfig.readTxMaxPowerOfProductViaShell(dut,productCode);
		if (txMaxPower == GeneralUtils.ERROR_VALUE) {
			report.report("Product code: +" + productCode + " does not exist in CSV file", Reporter.FAIL);
			reason = "Product code: " + productCode + " does not exist in CSV file";
			return;
		} else {
			report.report("Max power of " + dut.getName() + " is: " + txMaxPower + "dBm");
		}
		GeneralUtils.startLevel("Change Tx power of " + dut.getName() + " to " + (txMaxPower + 1) + "dBm");
		RadioParameters radioParams = new RadioParameters();
		radioParams.setTxpower(txMaxPower + 1);
		boolean flag = enbConfig.cloneAndSetRadioProfileViaNetSpan(dut, dut.getDefaultNetspanProfiles().getRadio(), radioParams);
		if(flag) {
			report.report("Succeeded to clone and set radio profile of "+dut.getName()+" via netspan with max Tx Power+1 ["+(txMaxPower+1)+" dBm]", Reporter.WARNING);
		}else{
			report.report("Failed to clone and set radio profile of "+dut.getName()+" via netspan with max Tx Power+1 ["+(txMaxPower+1)+" dBm]");
		}
		report.reportHtml("db get activerfcfg [*] txPower", dut.lteCli("db get activerfcfg [*] txPower"), true);
		int enbCurrentTxPower = dut.getRfStatusAchievedTxPower(dut.getCellContextID())/100;
		if(enbCurrentTxPower == (txMaxPower + 1)) {
			report.report("Succeeded to change Tx Power on enb of " + dut.getName() + " to Max Tx Power+1", Reporter.FAIL);
		}else{
			report.report("Tx power is not equal to Max Tx power+1 ["+ txMaxPower +" dBm]. Current Tx power: "+enbCurrentTxPower+" dBm");
		}
		GeneralUtils.stopLevel();
		
		enbConfig.revertToDefaultProfile(dut, EnbProfiles.Radio_Profile);
		int logLevel = dut.getSSHlogSession().getLogLevel();
		dut.lteCli("logger threshold set process=rfMgr cli="+logLevel);
	}

	@Test // 8
	@TestProperties(name = "Stability_Transmitter_Disable/Enable", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void stabilityTransmitterDisableOrEnable() {
			boolean status;
			if (staticUEs == null) {
				report.report("No Static UEs defined in the Test", Reporter.FAIL);
				return;
			}
			
			GeneralUtils.startLevel((String.format("Stoping unnecessarry ues.")));
			for (UE ue : SetupUtils.getInstance().getAllUEs()) {
				if(!ue.stop())
					report.report("Failed stopping UE " + ue.getName(),Reporter.WARNING);
				GeneralUtils.unSafeSleep(1000);
			}
			for (UE ue : staticUEs) {
				if(!ue.start())
					report.report("Failed Starting UE " + ue.getName(),Reporter.WARNING);
				GeneralUtils.unSafeSleep(1000);
			}
			GeneralUtils.stopLevel();
			
			traffic.setTrafficType(TrafficType.HO);
			traffic.startTraffic(staticUEs);
			GeneralUtils.startLevel("enable / disable TX power 100 times");
			
			
			for (int i = 0; i < 100; i++) {
				GeneralUtils.startLevel("Iteration #" + (i + 1));
				peripheralsConfig.changeEnbState(dut, EnbStates.IN_SERVICE);
				GeneralUtils.unSafeSleep(1000);
				EnbStates enbState = dut.getServiceState();
				if(enbState != EnbStates.IN_SERVICE)
				{
					report.report("operationalStatus: " + enbState + " instead of " + EnbStates.IN_SERVICE, Reporter.WARNING);
					reason += "operationalStatus: " + enbState + " instead of " + EnbStates.IN_SERVICE;
				} else {
					report.report("operationalStatus: " + enbState);
				}
				resetUes(staticUEs);
				status = peripheralsConfig.checkIfAllUEsAreConnectedToNode(staticUEs, dut);
				if (status == false) {
					report.report("eNB is in-service but one of UEs wasn't connected", Reporter.FAIL);
				}
				peripheralsConfig.changeEnbState(dut, EnbStates.OUT_OF_SERVICE);
				GeneralUtils.unSafeSleep(2000);
				enbState = dut.getServiceState();
				if(enbState != EnbStates.OUT_OF_SERVICE)
				{
					report.report("operationalStatus: " + enbState + " instead of " + EnbStates.OUT_OF_SERVICE, Reporter.WARNING);
					reason += "operationalStatus: " + enbState + " instead of " + EnbStates.OUT_OF_SERVICE;
				} else {
					report.report("operationalStatus: " + enbState);
				}
				status = peripheralsConfig.checkIfAllUEsAreConnectedToNode(staticUEs, dut);
				if (status == true) {
					report.report("eNB is out-of-service but UEs succeed to connect", Reporter.FAIL);
				}
				GeneralUtils.stopLevel();
			}
			
			GeneralUtils.stopLevel();
			traffic.stopTraffic();			
	}
}
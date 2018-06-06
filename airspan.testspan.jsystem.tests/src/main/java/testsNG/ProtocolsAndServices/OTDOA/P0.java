package testsNG.ProtocolsAndServices.OTDOA;

import org.junit.Test;

import Netspan.API.Enums.EnbStates;
import Netspan.Profiles.RadioParameters.PRSBandWidthEnum;
import Netspan.Profiles.RadioParameters.PRSMutingPeriodiclyEnum;
import Netspan.Profiles.RadioParameters.PRSPeriodiclyEnum;
import Utils.GeneralUtils;
import Utils.GeneralUtils.CellIndex;
import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;

public class P0 extends OtdoaBase {

	@Test
	@TestProperties(name = "lppa_Message_Cell1=Disable_Cell2=Enable", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void lppa_Message_Cell1_Disable_Cell2_Enable() {
		boolean ipgEnable = true;
		Boolean firstCellOtdoa = true;
		Boolean secondCellOtdoa = null;

		// All Types of EnodeB
		preTestIPGTests(ipgEnable);
		setNetworkWithNetspan(dut);

		// Multi Cell - disable all cells OTDOA abilities and change cell to 2
		if (enodeBConfig.getNumberOfActiveCells(dut) > 1) {
			report.report("Disable OTDOA for cell 1");
			dut.setCellContextNumber(1);
			setRadioWithNetspan(dut, false, null, false, null, null, null, null, null, null, null, null, null, null);
			dut.setCellContextNumber(2);
			secondCellOtdoa = true;
			firstCellOtdoa = false;
		}
		report.report("Enable OTDOA for cell " + dut.getCellContextID());
		setRadioWithNetspan(dut, true, 1000, true, "4", PRSBandWidthEnum.FIFTEEN,
				PRSPeriodiclyEnum.THREE_HUNDRED_TWENTY, 0, 9000, PRSMutingPeriodiclyEnum.SIXTEE, "1111111111111111",
				null, null, null);
		rebootNode(dut);
		verifyOTDOASnmp(CellIndex.FORTY, firstCellOtdoa, CellIndex.FORTY_ONE, secondCellOtdoa);

		sendLPPCommandWithIPG(ipg);
		if (!validateAllParameters()) {
			report.report("parameters are missing from Response.", Reporter.FAIL);
		} else {
			report.report("Every Parameter is included in Response.");
		}
	}

	@Test
	@TestProperties(name = "lppa Message Cell1=Enable Cell2=Disable", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void test2_Cell0_Enable_Cell1_Disable() {
		boolean ipgEnable = true;
		Boolean firstCellOtdoa = true;
		Boolean secondCellOtdoa = null;

		// All Types of EnodeB
		preTestIPGTests(ipgEnable);
		setNetworkWithNetspan(dut);

		// Multi Cell - disable all cells OTDOA abilities and change cell to 1
		if (enodeBConfig.getNumberOfActiveCells(dut) > 1) {
			report.report("Disable OTDOA for cell 2");
			dut.setCellContextNumber(2);
			setRadioWithNetspan(dut, false, null, false, null, null, null, null, null, null, null, null, null, null);
			dut.setCellContextNumber(1);
			secondCellOtdoa = false;
		}
		report.report("enable OTDOA for cell " + dut.getCellContextID());
		setRadioWithNetspan(dut, true, 1000, true, "4", PRSBandWidthEnum.FIFTEEN,
				PRSPeriodiclyEnum.THREE_HUNDRED_TWENTY, 0, 9000, PRSMutingPeriodiclyEnum.SIXTEE, "1111111111111111",
				null, null, null);
		rebootNode(dut);
		verifyOTDOASnmp(CellIndex.FORTY, firstCellOtdoa, CellIndex.FORTY_ONE, secondCellOtdoa);

		sendLPPCommandWithIPG(ipg);
		if (!validateAllParameters()) {
			report.report("parameters are missing from Response.", Reporter.FAIL);
		} else {
			report.report("Every Parameter is included in Response.");
		}
	}

	@Test
	@TestProperties(name = "lppa Message Cell1=Disable Cell2=Disable", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void lppa_Message_Cell1_Disable_Cell2_Disable() {
		boolean ipgEnable = true;
		Boolean firstCellOtdoa = false;
		Boolean secondCellOtdoa = null;

		// All Types of EnodeB
		preTestIPGTests(ipgEnable);
		setNetworkWithNetspan(dut);

		report.report("Disable OTDOA for cell 1");
		dut.setCellContextNumber(1);
		setRadioWithNetspan(dut, false, null, false, null, null, null, null, null, null, null, null, null, null);
		if (enodeBConfig.getNumberOfActiveCells(dut) > 1) {
			dut.setCellContextNumber(2);
			report.report("Disable OTDOA for cell 2");
			setRadioWithNetspan(dut, false, null, false, null, null, null, null, null, null, null, null, null, null);
			secondCellOtdoa = false;
		}
		rebootNode(dut);
		verifyOTDOASnmp(CellIndex.FORTY, firstCellOtdoa, CellIndex.FORTY_ONE, secondCellOtdoa);

		String lppaResponse = sendLPPCommandWithIPG(ipg);
		if (lppaResponse.equals("")) {
			report.report("No response from node - test pass!");
		} else {
			report.report("there is response from node!", Reporter.FAIL);
		}
	}

	@Test
	@TestProperties(name = "lppa Message via IPG and netspan Request Counter", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "DUT2", "IsTestWasSuccessful" })
	public void lppa_Message_via_IPG_and_netspan_Request_Counter() {
		boolean ipgEnable = true;
		Boolean firstCellOtdoa = true;
		Boolean secondCellOtdoa = null;

		// All Types of EnodeB
		preTestIPGTests(ipgEnable);
		setNetworkWithNetspan(dut);

		// Multi Cell - disable all cells OTDOA abilities and change cell to 1
		if (enodeBConfig.getNumberOfActiveCells(dut) > 1) {
			report.report("Disable OTDOA for cell 2");
			dut.setCellContextNumber(2);
			setRadioWithNetspan(dut, false, null, false, null, null, null, null, null, null, null, null, null, null);
			dut.setCellContextNumber(1);
			secondCellOtdoa = false;
		}

		// basic configuration for Netspan to enable OTDOA.
		report.report("Enable OTDOA for cell 1");
		setRadioWithNetspan(dut, true, 1000, true, "4", PRSBandWidthEnum.FIFTEEN,
				PRSPeriodiclyEnum.THREE_HUNDRED_TWENTY, 0, 9000, PRSMutingPeriodiclyEnum.SIXTEE, "1111111111111111",
				null, null, null);
		rebootNode(dut);
		verifyOTDOASnmp(CellIndex.FORTY, firstCellOtdoa, CellIndex.FORTY_ONE, secondCellOtdoa);

		resetCounter("OtdoaLppaMsgRequest");
		sendLPPCommandWithIPG(ipg);
		checkCounter("OtdoaLppaMsgRequest", 1);
	}

	@Test
	@TestProperties(name = "lppa Message via IPG and netspan Response Counter", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "DUT2", "IsTestWasSuccessful" })
	public void lppa_Message_via_IPG_and_netspan_Response_Counter() {
		boolean ipgEnable = true;
		Boolean firstCellOtdoa = true;
		Boolean secondCellOtdoa = null;

		// All Types of EnodeB
		preTestIPGTests(ipgEnable);
		setNetworkWithNetspan(dut);

		// Multi Cell - disable all cells OTDOA abilities and change cell to 1
		if (enodeBConfig.getNumberOfActiveCells(dut) > 1) {
			report.report("Disable OTDOA for cell 2");
			dut.setCellContextNumber(2);
			setRadioWithNetspan(dut, false, null, false, null, null, null, null, null, null, null, null, null, null);
			dut.setCellContextNumber(1);
			secondCellOtdoa = false;
		}

		// basic configuration for Netspan to enable OTDOA.
		report.report("Enable OTDOA for cell 1");
		setRadioWithNetspan(dut, true, 1000, true, "4", PRSBandWidthEnum.FIFTEEN,
				PRSPeriodiclyEnum.THREE_HUNDRED_TWENTY, 0, 9000, PRSMutingPeriodiclyEnum.SIXTEE, "1111111111111111",
				null, null, null);
		rebootNode(dut);
		verifyOTDOASnmp(CellIndex.FORTY, firstCellOtdoa, CellIndex.FORTY_ONE, secondCellOtdoa);

		resetCounter("OtdoaLppaMsgResponse");
		sendLPPCommandWithIPG(ipg);
		checkCounter("OtdoaLppaMsgResponse", 1);
	}

	@Test
	@TestProperties(name = "lppa Message While OTDOA disable", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "DUT2", "IsTestWasSuccessful" })
	public void lppa_Message_While_OTDOA_disable() {
		boolean ipgEnable = true;
		Boolean firstCellOtdoa = false;
		Boolean secondCellOtdoa = null;

		// All Types of EnodeB -> change network profile to 2 MMEs.
		preTestIPGTests(ipgEnable);
		setNetworkWithNetspan(dut);

		// Disable both Cells OTDOA feature.
		if (enodeBConfig.getNumberOfActiveCells(dut) > 1) {
			report.report("Disable OTDOA for cell 2");
			dut.setCellContextNumber(2);
			setRadioWithNetspan(dut, false, null, false, null, null, null, null, null, null, null, null, null, null);
			dut.setCellContextNumber(1);
			secondCellOtdoa = false;
		}

		report.report("Disable OTDOA for cell 1");
		setRadioWithNetspan(dut, false, null, false, null, null, null, null, null, null, null, null, null, null);
		rebootNode(dut);
		verifyOTDOASnmp(CellIndex.FORTY, firstCellOtdoa, CellIndex.FORTY_ONE, secondCellOtdoa);

		// validate fail counter + 1.
		resetCounter("OtdoaLppaMsgFailAll");
		sendLPPCommandWithIPG(ipg);
		checkCounter("OtdoaLppaMsgFailAll", 1);
	}

	@Test
	@TestProperties(name = "ue meas Cli Command and Verify Counter - OtdoaRrcConnectionReconfigStart", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void ue_meas_Cli_Command_and_Verify_Counter_OtdoaRrcConnectionReconfigStart() {
		boolean ipgEnable = true;
		Boolean firstCellOtdoa = true;
		Boolean secondCellOtdoa = null;
		peripheralsConfig.changeEnbState(dut2, EnbStates.OUT_OF_SERVICE);
		// All Types of EnodeB -> change network profile to 2 MMEs.
		preTestIPGTests(ipgEnable);
		setNetworkWithNetspan(dut);

		// Disable both Cells OTDOA feature.
		if (enodeBConfig.getNumberOfActiveCells(dut) > 1) {
			report.report("Disable OTDOA for cell 2");
			dut.setCellContextNumber(2);
			setRadioWithNetspan(dut, false, null, false, null, null, null, null, null, null, null, null, null, null);
			dut.setCellContextNumber(1);
			secondCellOtdoa = false;
		}
		report.report("Enable OTDOA for cell 1");
		setRadioWithNetspan(dut, true, 1000, true, "4", PRSBandWidthEnum.FIFTEEN,
				PRSPeriodiclyEnum.THREE_HUNDRED_TWENTY, 0, 9000, PRSMutingPeriodiclyEnum.SIXTEE, "1111111111111111",
				null, null, null);

		rebootNode(dut);
		verifyOTDOASnmp(CellIndex.FORTY, firstCellOtdoa, CellIndex.FORTY_ONE, secondCellOtdoa);

		startTraffic();
		getSingleUERNTI();
		if (rnti.equals("defaultValue")) {
			report.report("could not get rnti - fail test", Reporter.FAIL);
			return;
		}

		trig = "0"; // -> start in the trigger CLI Command
		resetCounter("OtdoaRrcConnectionReconfigStart");
		String cliAnswer = dut.lteCli(otdoaCommand());
		GeneralUtils.printToConsole(otdoaCommand() + " command Answer: \n" + cliAnswer);
		checkCounter("OtdoaRrcConnectionReconfigStart", 1);
		peripheralsConfig.changeEnbState(dut2, EnbStates.IN_SERVICE);
	}

	@Test
	@TestProperties(name = "ue meas Cli Command and Verify Counter - OtdoaRrcConnectionReconfigStop", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void ue_meas_Cli_Command_and_Verify_Counter_OtdoaRrcConnectionReconfigStop() {
		boolean ipgEnable = true;
		Boolean firstCellOtdoa = true;
		Boolean secondCellOtdoa = null;
		peripheralsConfig.changeEnbState(dut2, EnbStates.OUT_OF_SERVICE);

		// All Types of EnodeB -> change network profile to 2 MMEs.
		preTestIPGTests(ipgEnable);
		setNetworkWithNetspan(dut);

		// Disable both Cells OTDOA feature.
		if (enodeBConfig.getNumberOfActiveCells(dut) > 1) {
			report.report("Disable OTDOA for cell 2");
			dut.setCellContextNumber(2);
			setRadioWithNetspan(dut, false, null, false, null, null, null, null, null, null, null, null, null, null);
			dut.setCellContextNumber(1);
			secondCellOtdoa = false;
		}
		report.report("Enable OTDOA for cell 1");
		setRadioWithNetspan(dut, true, 1000, true, "4", PRSBandWidthEnum.FIFTEEN,
				PRSPeriodiclyEnum.THREE_HUNDRED_TWENTY, 0, 9000, PRSMutingPeriodiclyEnum.SIXTEE, "1111111111111111",
				null, null, null);

		rebootNode(dut);
		verifyOTDOASnmp(CellIndex.FORTY, firstCellOtdoa, CellIndex.FORTY_ONE, secondCellOtdoa);

		startTraffic();
		getSingleUERNTI();
		if (rnti.equals("defaultValue")) {
			report.report("could not get rnti from snmp - fail test", Reporter.FAIL);
			return;
		}

		trig = "1"; // -> start in the trigger CLI Command
		resetCounter("OtdoaRrcConnectionReconfigStop");
		String cliAnswer = dut.lteCli(otdoaCommand());
		GeneralUtils.printToConsole(otdoaCommand() + " command Answer: \n" + cliAnswer);
		checkCounter("OtdoaRrcConnectionReconfigStop", 1);
		peripheralsConfig.changeEnbState(dut2, EnbStates.IN_SERVICE);
	}

	@Test
	@TestProperties(name = "basic NMS Radio Verification :E-CID Enable, OTODA Enable", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void basic_NMS_Radio_Verification_ECID_Enable_OTODA_Enable() {
		// SNMP opposite values in order to make the test Valid
		setOTDOAAndECIDValues(false, false);
		if (!verifyOTDOAAndECIDWithSNMP(false, null, false, null, null, null, null, null, null, null)) {
			report.report("snmp Results were no match for excpectations", Reporter.WARNING);
		}

		// set Radio with Netspan.
		setRadioWithNetspan(dut, true, null, true, null, null, null, null, null, null, null, null, null, null);
		if (!verifyOTDOAAndECIDWithSNMP(true, null, true, null, null, null, null, null, null, null)) {
			report.report("snmp Results were no match for excpectations", Reporter.FAIL);
		} else {
			report.report("snmp Results were Correct!");
		}
	}

	@Test
	@TestProperties(name = "basic NMS Radio Verification : E-CID Enable ,OTODA Disable", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void basic_NMS_Radio_Verification_ECID_Enable_OTODA_Disable() {
		// SNMP opposite values in order to make the test Valid
		setOTDOAAndECIDValues(false, true);
		if (!verifyOTDOAAndECIDWithSNMP(false, null, true, null, null, null, null, null, null, null)) {
			report.report("snmp Results were no match for excpectations", Reporter.WARNING);
		}

		// set Radio with Netspan.
		setRadioWithNetspan(dut, true, null, false, null, null, null, null, null, null, null, null, null, null);
		if (!verifyOTDOAAndECIDWithSNMP(true, null, false, null, null, null, null, null, null, null)) {
			report.report("snmp Results were no match for excpectations", Reporter.FAIL);
		} else {
			report.report("snmp Results were Correct!");
		}
	}

	@Test
	@TestProperties(name = "basic NMS Radio Verification :E-CID Disable, OTDOA Enable", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void basic_NMS_Radio_Verification_ECID_Disable_OTDOA_Enable() {
		// SNMP opposite values in order to make the test Valid
		setOTDOAAndECIDValues(true, false);
		if (!verifyOTDOAAndECIDWithSNMP(true, null, false, null, null, null, null, null, null, null)) {
			report.report("snmp Results were no match for excpectations", Reporter.WARNING);
		}

		// ECID,timer,otdoa,subframes,bandWidth,prs Periodicity,offSet, power,Muting
		// Periodicity ,pattern,isFDD,FrameConfig
		setRadioWithNetspan(dut, false, null, true, null, null, null, null, null, null, null, null, null, null);
		if (!verifyOTDOAAndECIDWithSNMP(false, null, true, null, null, null, null, null, null, null)) {
			report.report("snmp Results were no match for excpectations", Reporter.FAIL);
		} else {
			report.report("snmp Results were Correct!");
		}
	}

	@Test
	@TestProperties(name = "basic NMS Radio Verification :E-CID Disable, OTDOA Disable", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void basic_NMS_Radio_Verification_ECID_Disable_OTDOA_Disable() {
		// SNMP opposite values in order to make the test Valid
		setOTDOAAndECIDValues(true, true);
		if (!verifyOTDOAAndECIDWithSNMP(true, null, true, null, null, null, null, null, null, null)) {
			report.report("snmp Results were no match for excpectations", Reporter.WARNING);
		}

		// ECID,timer,otdoa,subframes,bandWidth,prs Periodicity,offSet, power,Muting
		// Periodicity ,pattern,isFDD,FrameConfig
		setRadioWithNetspan(dut, false, null, false, null, null, null, null, null, null, null, null, null, null);
		if (!verifyOTDOAAndECIDWithSNMP(false, null, false, null, null, null, null, null, null, null)) {
			report.report("snmp Results were no match for excpectations", Reporter.FAIL);
		} else {
			report.report("snmp Results were Correct!");
		}
	}

	@Test
	@TestProperties(name = "test15 - NMS Radio Configuration 1 TDD Mode FC 1", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "DUT2", "IsTestWasSuccessful" })
	public void test15_NMS_TDD_config1() {
		if (!validateTestMode(dut, "TDD")) {
			return;
		}

		PRSBandWidthEnum prsBw = PRSBandWidthEnum.HUNDRED;
		PRSMutingPeriodiclyEnum mutePriod = PRSMutingPeriodiclyEnum.SIXTEE;
		PRSPeriodiclyEnum prsPeriod = PRSPeriodiclyEnum.ONE_HUNDRED_SIXTY;
		// ECID,timer,otdoa,subframes,bandWidth ,prs Periodicity ,offSet, power,Muting
		// Periodicity ,pattern,isFDD,FrameConfig
		if (!setRadioWithNetspan(dut, true, 1000, true, "9", PRSBandWidthEnum.HUNDRED,
				PRSPeriodiclyEnum.ONE_HUNDRED_SIXTY, 100, 9000, PRSMutingPeriodiclyEnum.SIXTEE, "1111111111111111",
				false, 1, null)) {
			report.report("could not change radio profile!", Reporter.FAIL);
			return;
		}
		// check with SNMP.
		Integer decimalValue = Integer.parseInt("1111111111111111", 2);
		if (!verifyOTDOAAndECIDWithSNMP(true, 1000, true, 8, prsBw.getPRS(), prsPeriod.getPRS(), 100, 9000,
				mutePriod.getPRS(), decimalValue)) {
			report.report("snmp Results were no match for expectations", Reporter.FAIL);
		} else {
			report.report("snmp Results were Correct!");
		}
	}

	@Test
	@TestProperties(name = "test16 - NMS Radio Configuration 2 TDD Mode FC 2", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "DUT2", "IsTestWasSuccessful" })
	public void test16_NMS_TDD_config2() {
		if (!validateTestMode(dut, "TDD")) {
			return;
		}

		PRSBandWidthEnum prsBw = PRSBandWidthEnum.SEVENTY_FIVE;
		PRSMutingPeriodiclyEnum mutePriod = PRSMutingPeriodiclyEnum.EIGHT;
		PRSPeriodiclyEnum prsPeriod = PRSPeriodiclyEnum.THREE_HUNDRED_TWENTY;
		// ECID,timer,otdoa,subframes,bandWidth ,prs Periodicity ,offSet, power,Muting
		// Periodicity ,pattern,isFDD,FrameConfig
		if (!setRadioWithNetspan(dut, true, 1000, true, "3,4", PRSBandWidthEnum.SEVENTY_FIVE,
				PRSPeriodiclyEnum.THREE_HUNDRED_TWENTY, 200, 5000, PRSMutingPeriodiclyEnum.EIGHT, "11111111", false, 2,
				null)) {
			report.report("could not change radio profile!", Reporter.FAIL);
			return;
		}
		// check with SNMP.
		Integer decimalValue = Integer.parseInt("11111111", 2);
		if (!verifyOTDOAAndECIDWithSNMP(true, 1000, true, 4, prsBw.getPRS(), prsPeriod.getPRS(), 200, 5000,
				mutePriod.getPRS(), decimalValue)) {
			report.report("snmp Results were no match for expectations", Reporter.FAIL);
		} else {
			report.report("snmp Results were Correct!");
		}
	}

	@Test
	@TestProperties(name = "test17 - NMS Radio Configuration 3 TDD Mode FC 2", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "DUT2", "IsTestWasSuccessful" })
	public void test17_NMS_TDD_config3() {
		if (!validateTestMode(dut, "TDD")) {
			return;
		}

		PRSBandWidthEnum prsBw = PRSBandWidthEnum.FIFTEEN;
		PRSMutingPeriodiclyEnum mutePriod = PRSMutingPeriodiclyEnum.FOUR;
		PRSPeriodiclyEnum prsPeriod = PRSPeriodiclyEnum.SIX_HUNDRED_FORTY;
		// ECID,timer,otdoa,subframes,bandWidth ,prs Periodicity ,offSet, power,Muting
		// Periodicity ,pattern,isFDD,FrameConfig
		if (!setRadioWithNetspan(dut, true, 1000, true, "8,9", PRSBandWidthEnum.FIFTEEN,
				PRSPeriodiclyEnum.SIX_HUNDRED_FORTY, 400, 3000, PRSMutingPeriodiclyEnum.FOUR, "1100", false, 2, null)) {
			report.report("could not change radio profile!", Reporter.FAIL);
			return;
		}
		// check with SNMP.
		Integer decimalValue = Integer.parseInt("1100", 2);
		if (!verifyOTDOAAndECIDWithSNMP(true, 1000, true, 8, prsBw.getPRS(), prsPeriod.getPRS(), 400, 3000,
				mutePriod.getPRS(), decimalValue)) {
			report.report("snmp Results were no match for expectations", Reporter.FAIL);
		} else {
			report.report("snmp Results were Correct!");
		}
	}

	// NOT TESTED YET!
	@TestProperties(name = "test12 - NMS Radio Configuration 1 FDD Mode", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void test12_NMS_FDD_config1() {
		if (!validateTestMode(dut, "FDD")) {
			return;
		}
		// ECID,timer,otdoa,subframes,bandWidth ,prs Periodicity ,offSet, power,Muting
		// Periodicity ,pattern,isFDD,FrameConfig
		setRadioWithNetspan(dut, true, 1000, true, "4", PRSBandWidthEnum.HUNDRED, PRSPeriodiclyEnum.ONE_HUNDRED_SIXTY,
				0, 9000, PRSMutingPeriodiclyEnum.SIXTEE, "1111111111111111", true, null, null);
		// check with SNMP.
		Integer decimalValue = Integer.parseInt("1111111111111111", 2);
		if (!verifyOTDOAAndECIDWithSNMP(true, 1000, true, 4, 100, 160, 0, 9000, 16, decimalValue)) {
			report.report("snmp Results were no match for excpectations", Reporter.FAIL);
		} else {
			report.report("snmp Results were Correct!");
		}
	}

	// NOT TESTED YET!
	@TestProperties(name = "test13 - NMS Radio Configuration 2 FDD Mode", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void test13_NMS_FDD_config2() {
		if (!validateTestMode(dut, "FDD")) {
			return;
		}
		PRSBandWidthEnum prsBw = PRSBandWidthEnum.FIFTEEN;
		PRSMutingPeriodiclyEnum mutePriod = PRSMutingPeriodiclyEnum.SIXTEE;
		PRSPeriodiclyEnum prsPeriod = PRSPeriodiclyEnum.THREE_HUNDRED_TWENTY;
		// ECID,timer,otdoa,subframes,bandWidth ,prs Periodicity ,offSet, power,Muting
		// Periodicity ,pattern,isFDD,FrameConfig
		setRadioWithNetspan(dut, true, 1000, true, "1,2,3,4", PRSBandWidthEnum.FIFTEEN,
				PRSPeriodiclyEnum.THREE_HUNDRED_TWENTY, 10, 0, PRSMutingPeriodiclyEnum.SIXTEE, "1111111100000000", true,
				null, null);
		// check with SNMP.
		Integer decimalValue = Integer.parseInt("1111111100000000", 2);
		if (!verifyOTDOAAndECIDWithSNMP(true, 1000, true, 4, prsBw.getPRS(), prsPeriod.getPRS(), 10, 0,
				mutePriod.getPRS(), decimalValue)) {
			report.report("snmp Results were no match for excpectations", Reporter.FAIL);
		} else {
			report.report("snmp Results were Correct!");
		}
	}

	@Test
	@TestProperties(name = "test18 - NMS Radio Configuration 1 TDD Mode FC 2,Negative", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void test18_NMS_TDD_config2() {
		if (!validateTestMode(dut, "TDD")) {
			return;
		}
		reportConfiguration(true, 1000, true, "3,9", PRSBandWidthEnum.FIFTEEN,
				PRSPeriodiclyEnum.SIX_HUNDRED_FORTY, 400, 3000, PRSMutingPeriodiclyEnum.FOUR, "1100", false, 2, null);
		// ECID,timer,otdoa,subframes,bandWidth ,prs Periodicity ,offSet, power,Muting
		// Periodicity ,pattern,isFDD,FrameConfig
		if (!setRadioWithNetspan(dut, true, 1000, true, "3,9", PRSBandWidthEnum.FIFTEEN,
				PRSPeriodiclyEnum.SIX_HUNDRED_FORTY, 400, 3000, PRSMutingPeriodiclyEnum.FOUR, "1100", false, 2, null)) {
			report.report("netspan Could not Clone Profile as expected!");
		} else {
			report.report("Netspan received clone profile!", Reporter.FAIL);
		}

	}

	//@Test
	@TestProperties(name = "test19 - NMS Radio Configuration 2 TDD Mode CellBandWitdh 5,Negative", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void test19_NMS_TDD_config2() {
		if (!validateTestMode(dut, "TDD")) {
			return;
		}

		reportConfiguration(true, 1000, true, "8,9", PRSBandWidthEnum.FIFTY,
				PRSPeriodiclyEnum.SIX_HUNDRED_FORTY, 400, 3000, PRSMutingPeriodiclyEnum.FOUR, "1100", false, 2, "5");
		// ECID,timer,otdoa,subframes,bandWidth ,prs Periodicity ,offSet, power,Muting
		// Periodicity ,pattern,isFDD,FrameConfig
		if (!setRadioWithNetspan(dut, true, 1000, true, "8,9", PRSBandWidthEnum.FIFTY,
				PRSPeriodiclyEnum.SIX_HUNDRED_FORTY, 400, 3000, PRSMutingPeriodiclyEnum.FOUR, "1100", false, 2, "5")) {
			report.report("netspan Could not Clone Profile as expected!");
		} else {
			report.report("Netspan received clone profile!", Reporter.FAIL);
		}
	}

	@Test
	@TestProperties(name = "test20 - NMS Radio Configuration 3 TDD Mode FC 2,Negative", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void test20_NMS_TDD_config3() {
		if (!validateTestMode(dut, "TDD")) {
			return;
		}

		reportConfiguration(true, 1000, true, "8,9", PRSBandWidthEnum.HUNDRED,
				PRSPeriodiclyEnum.SIX_HUNDRED_FORTY, 400, 3000, PRSMutingPeriodiclyEnum.FOUR, "11100", false, 2,
				null);
		// ECID,timer,otdoa,subframes,bandWidth ,prs Periodicity ,offSet, power,Muting
		// Periodicity ,pattern,isFDD,FrameConfig
		if (!setRadioWithNetspan(dut, true, 1000, true, "8,9", PRSBandWidthEnum.HUNDRED,
				PRSPeriodiclyEnum.SIX_HUNDRED_FORTY, 400, 3000, PRSMutingPeriodiclyEnum.FOUR, "11100", false, 2,
				null)) {
			report.report("netspan Could not Clone Profile as expected!");
		} else {
			report.report("Netspan received clone profile!", Reporter.FAIL);
		}
	}

	// NOT TESTED YET!
	@TestProperties(name = "test14 - NMS Radio Configuration 3 FDD Mode", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void test14_NMS_FDD_config3() {
		if (!validateTestMode(dut, "FDD")) {
			return;
		}
		PRSBandWidthEnum prsBw = PRSBandWidthEnum.SIX;
		PRSMutingPeriodiclyEnum mutePriod = PRSMutingPeriodiclyEnum.TWO;
		PRSPeriodiclyEnum prsPeriod = PRSPeriodiclyEnum.ONE_THOUSEND_TWO_HUNDRED_EIGHTY;
		// ECID,timer,otdoa,subframes,bandWidth ,prs Periodicity ,offSet, power,Muting
		// Periodicity ,pattern,isFDD,FrameConfig
		setRadioWithNetspan(dut, true, 1000, true, "6,7,8,9", PRSBandWidthEnum.SIX,
				PRSPeriodiclyEnum.ONE_THOUSEND_TWO_HUNDRED_EIGHTY, 100, 10000, PRSMutingPeriodiclyEnum.TWO, "10", true,
				null, null);
		// check with SNMP.
		Integer decimalValue = Integer.parseInt("10", 2);
		if (!verifyOTDOAAndECIDWithSNMP(true, 1000, true, 8, prsBw.getPRS(), prsPeriod.getPRS(), 100, 10000,
				mutePriod.getPRS(), decimalValue)) {
			report.report("snmp Results were no match for excpectations", Reporter.FAIL);
		} else {
			report.report("snmp Results were Correct!");
		}
	}

	// NOT TESTED YET!
	@TestProperties(name = "test21 - NMS Radio Configuration 1 FDD ,Negative", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void test21_NMS_FDD_config1() {
		if (!validateTestMode(dut, "TDD")) {
			return;
		}

		// ECID,timer,otdoa,subframes,bandWidth ,prs Periodicity ,offSet, power,Muting
		// Periodicity ,pattern,isFDD,FrameConfig
		if (!setRadioWithNetspan(dut, true, 1000, true, "3", PRSBandWidthEnum.HUNDRED,
				PRSPeriodiclyEnum.ONE_THOUSEND_TWO_HUNDRED_EIGHTY, 0, 3000, PRSMutingPeriodiclyEnum.SIXTEE,
				"1111111100000000", true, null, null)) {
			report.report("netspan Could not Clone Profile as expected!");
		} else {
			report.report("Netspan received clone profile!", Reporter.FAIL);
		}
	}
}

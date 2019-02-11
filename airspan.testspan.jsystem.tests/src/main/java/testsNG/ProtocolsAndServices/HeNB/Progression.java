package testsNG.ProtocolsAndServices.HeNB;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Test;

import EPC.EPC;
import EPC.MME;
import EnodeB.EnodeB;
import Netspan.EnbProfiles;
import Netspan.API.Enums.EnabledDisabledStates;
import Netspan.API.Enums.EnbTypes;
import Netspan.API.Enums.NetworkElementStatus;
import Netspan.NBI_15_5.Netspan_15_5_abilities;
import Netspan.Profiles.NetworkParameters;
import Utils.GeneralUtils;
import Utils.SysObjUtils;
import jsystem.framework.ParameterProperties;
import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;
import testsNG.TestspanTest;
import testsNG.Actions.EnodeBConfig;
import testsNG.ProtocolsAndServices.HandOver.P0;

public class Progression extends TestspanTest {

	private EnodeB dut1;
	private EnodeB dut2;
	private P0 HO = new P0();
	private EnodeBConfig enodeBConfig;
	private boolean skipReboot;
	private boolean isRedundency;
	private EnabledDisabledStates henbGwEnabled;
	private EPC epc;
	MME mme1, mme2;
	
	@Override
	public void init() throws Exception {
		report.startLevel("Init Tests");
		enodeBConfig = EnodeBConfig.getInstance();
		enbInTest = new ArrayList<EnodeB>();
		enbInTest.add(dut1);
		epc = EPC.getInstance();
		if(dut2 != null)
			enbInTest.add(dut2);
		super.init();
		if(dut2 != null){
			HO.setDUT1(dut1.getName());
			HO.setDUT2(dut2.getName());
			HO.init();			
		}
		report.stopLevel();
	}

	@Test
	@TestProperties(name = "Home EnodeB X2 HO", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void homeEnbX2H0() throws Exception {
		report.report("Home EnodeB X2 HO");
		if(!checkEnodeBEarfcn())
			return;
		henbGwEnabled = EnabledDisabledStates.DISABLED;
		preTest();
		report.startLevel("X2 HO Test.");
		HO.BasicHO_X2IntraFrequency();
		report.stopLevel();
		postTest();
	}

	@Test
	@TestProperties(name = "Home EnodeB S1 HO", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void homeEnbS1H0() throws Exception {
		report.report("Home EnodeB S1 HO");
		if(!checkEnodeBEarfcn())
			return;
		henbGwEnabled = EnabledDisabledStates.DISABLED;
		preTest();
		report.startLevel("S1 HO Test.");
		HO.BasicHO_S1IntraFrequency();
		report.stopLevel();
		postTest();
	}

	@Test
	@TestProperties(name = "Home EnodeB X2 HO Behind GW", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void homeEnbX2H0BehindGw() throws Exception {
		report.report("Home EnodeB X2 HO Behind GW");
		if(!checkEnodeBEarfcn())
			return;
		henbGwEnabled = EnabledDisabledStates.ENABLED;
		preTest();
		report.startLevel("X2 HO Test.");
		HO.BasicHO_X2IntraFrequency();
		report.stopLevel();
		postTest();
	}

	@Test
	@TestProperties(name = "Home EnodeB S1 HO Behind GW", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void homeEnbS1H0BehindGw() throws Exception {
		report.report("Home EnodeB S1 HO Behind GW");
		if(!checkEnodeBEarfcn())
			return;
		henbGwEnabled = EnabledDisabledStates.ENABLED;
		preTest();
		report.startLevel("S1 HO Test.");
		HO.BasicHO_S1IntraFrequency();
		report.stopLevel();
		postTest();
	}
	
	@Test
	@TestProperties(name = "Home EnodeB Redundency Without GW", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful", "DUT2" })
	public void homeEnbRedundencyWithoutGw() throws Exception {
		report.report("Home EnodeB Redundency Without GW");
		henbGwEnabled = EnabledDisabledStates.DISABLED;
		isRedundency = true;
		preTest();
		redundencyTest();
		postTest();
	}
	
	@Test
	@TestProperties(name = "Home EnodeB Redundency Behind GW", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful", "DUT2"})
	public void homeEnbRedundencyBehindGw() throws Exception {
		report.report("Home EnodeB Redundency Behind GW");
		henbGwEnabled = EnabledDisabledStates.ENABLED;
		isRedundency = true;
		preTest();
		redundencyTest();
		postTest();
	}

	private boolean checkEnodeBEarfcn() {
		if (!enodeBConfig.getEARFCNforNode(dut1).equals(enodeBConfig.getEARFCNforNode(dut2))) {
			report.report("EnodeB frequences are diffrent in an Intra test. stopping test", Reporter.FAIL);
			return false;
		}
		return true;	
	}
	
	private boolean preTest() {
		boolean actionSucceeded = true;
		try {
			if(!isRedundency)
				actionSucceeded &= setPciRanges(false);
			report.startLevel("Enable Home Enb Feature. ");
			boolean rebootRequired = Integer.parseInt(dut1.getEnbType()) != EnbTypes.HOME.ordinal();
			rebootRequired |= !dut1.getOperateBehindHenbGw().equals(henbGwEnabled);
			if(isRedundency)
				rebootRequired |= !verifyMmesConfig();
			if ( rebootRequired ) {
				if(isRedundency)
					setMmes();
				actionSucceeded &= enodeBConfig.setEnbType(dut1, EnbTypes.HOME);
				if (!dut1.getOperateBehindHenbGw().equals(henbGwEnabled))
					actionSucceeded &= enodeBConfig.setOperateBehindHenbGw(dut1, henbGwEnabled);
				actionSucceeded &= enodeBConfig.performReProvision(dut1);
				actionSucceeded &= dut1.reboot();
				GeneralUtils.unSafeSleep(60 * 1000);
				actionSucceeded &= dut1.waitForAllRunningAndInService(EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
				report.stopLevel();
			} else {
				report.report("Operate Behind HeNB GW is already " + henbGwEnabled.toString() + ".");
				report.report(dut1.getName() + " is already configured as HOME.");
				skipReboot = true;
				report.stopLevel();
				return true;
			}
			return actionSucceeded;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	private boolean setMmes(){
		NetworkParameters np = new NetworkParameters();
		np.setMMEIPS( mme1.getS1IpAddress(), mme2.getS1IpAddress() );
		return enodeBConfig.cloneAndSetNetworkProfileViaNetSpan(dut1, dut1.getDefaultNetspanProfiles().getNetwork(), np);
	}
	
	private void printDebugData() {
		GeneralUtils.startLevel("DEBUG DATA");
		GeneralUtils.reportHtmlLink(dut1.getName() + " - db get stackcfg eNB_Type,HeNB_GW_Pres", dut1.lteCli("db get stackcfg eNB_Type,HeNB_GW_Pres"));
		GeneralUtils.reportHtmlLink(dut1.getName() + " - db get mmeStatus", dut1.lteCli("db get mmeStatus"));
		GeneralUtils.reportHtmlLink(dut1.getName() + " - enb show mmesstatus", dut1.lteCli("enb show mmesstatus"));
		GeneralUtils.stopLevel();
	}
	
	private boolean verifyMmesConfig(){
		boolean flag1 = false, flag2 = false;
		if(epc.MME.length < 2){
			report.report("Please set 2 mmes in epc node on SUT.", Reporter.FAIL);
			return false;
		}
		mme1 = epc.MME[0];
		mme2 = epc.MME[1];
		ArrayList<String> mmeIps = enodeBConfig.getMMeIpAdresses(dut1);
		for(String mmeIp : mmeIps)
		{
			if(mmeIp.equals(mme1.getS1IpAddress()))
				flag1 = true;
			if(mmeIp.equals(mme2.getS1IpAddress()))
				flag2 = true;
		}
		return flag1 && flag2;
	}
	
	private boolean fixMmesOrder(){
		HashMap<String, NetworkElementStatus> mmeStatuses = ((Netspan_15_5_abilities)netspanServer).getMMEStatuses(dut1);
		NetworkElementStatus status1 = mmeStatuses.get(mme1.getS1IpAddress());
		if (status1 == null) {
			report.report("Failed to get MME Status for first MME with IP address: " + mme1.getS1IpAddress(), Reporter.FAIL);
			return false;
		}
		NetworkElementStatus status2 = mmeStatuses.get(mme2.getS1IpAddress());
		if (status2 == null) {
			report.report("Failed to get MME Status for second MME with IP address: " + mme2.getS1IpAddress(), Reporter.FAIL);
			return false;
		}
		MME temp;
		if(status2.equals(NetworkElementStatus.ACTIVE)){
			temp = mme1;
			mme1 = mme2;
			mme2 = temp;
		}
		return true;
	}
	
	private boolean verifyMmesStatus(NetworkElementStatus mme1ExpectedStatus, NetworkElementStatus mme2ExpectedStatus){
		HashMap<String, NetworkElementStatus> mmeStatuses = ((Netspan_15_5_abilities)netspanServer).getMMEStatuses(dut1);
		NetworkElementStatus status = mmeStatuses.get(mme1.getS1IpAddress());
		if(status != null && status.equals(mme1ExpectedStatus))
			report.step(String.format("MME %s status is %s as expected", mme1.getS1IpAddress(), mme1ExpectedStatus));
		else{
			report.report(String.format("MME %s status is %s instead of %s", mme1.getS1IpAddress(), status, mme1ExpectedStatus), Reporter.FAIL);
			GeneralUtils.reportHtmlLink(dut1.getName() + " - db get mmeStatus", dut1.lteCli("db get mmeStatus"));
		}
		
		status = mmeStatuses.get(mme2.getS1IpAddress());
		if(status != null && status.equals(mme2ExpectedStatus))
			report.step(String.format("MME %s status is %s as expected", mme2.getS1IpAddress(), mme2ExpectedStatus));
		else{
			report.report(String.format("MME %s status is %s instead of %s", mme2.getS1IpAddress(), status, mme2ExpectedStatus), Reporter.FAIL);
			GeneralUtils.reportHtmlLink(dut1.getName() + " - db get mmeStatus", dut1.lteCli("db get mmeStatus"));
		}
		return false;
	}
	
	private boolean redundencyTest(){
		dut1.expecteInServiceState = false;
		GeneralUtils.startLevel("Redundency Test");
		if(!fixMmesOrder())
		{
			GeneralUtils.stopLevel();
			return false;
		}
		printDebugData();
		report.report("STEP 1: Get initial MME statuses");
		NetworkElementStatus expectedStatus = henbGwEnabled == EnabledDisabledStates.ENABLED ? NetworkElementStatus.SCTP_ONLY : NetworkElementStatus.ACTIVE;
		verifyMmesStatus(NetworkElementStatus.ACTIVE, expectedStatus);
		report.report("STEP 2: Block connection to " + mme1.getS1IpAddress() + " and wait 2 min");
		dut1.shell("route add -host " + mme1.getS1IpAddress() + " reject");
		GeneralUtils.unSafeSleep(120 * 1000);
		report.report("STEP 3: Get MME statuses");
		verifyMmesStatus(NetworkElementStatus.INACTIVE, NetworkElementStatus.ACTIVE);
		report.report("STEP 4: Renew connection to " + mme1.getS1IpAddress() + " and wait 2 min");
		dut1.shell("route del -host " + mme1.getS1IpAddress() + " reject");
		GeneralUtils.unSafeSleep(120 * 1000);
		report.report("STEP 5: Get MME statuses");
		verifyMmesStatus(expectedStatus, NetworkElementStatus.ACTIVE);
		report.report("STEP 6: Block connection to " + mme2.getS1IpAddress() + " and wait 2 min");
		dut1.shell("route add -host " + mme2.getS1IpAddress() + " reject");
		GeneralUtils.unSafeSleep(120 * 1000);
		report.report("STEP 7: Get MME statuses");
		verifyMmesStatus(NetworkElementStatus.ACTIVE, NetworkElementStatus.INACTIVE);
		report.report("STEP 8: Renew connection to " + mme2.getS1IpAddress() + " and wait 2 min");
		dut1.shell("route del -host " + mme2.getS1IpAddress() + " reject");
		GeneralUtils.unSafeSleep(120 * 1000);
		report.report("STEP 9: Get MME statuses");
		verifyMmesStatus(NetworkElementStatus.ACTIVE, expectedStatus);
		dut1.expecteInServiceState = true;
		GeneralUtils.stopLevel();
		return false;
	}
	
	private boolean postTest() {
		boolean actionSucceeded = true;
		try {
			if(!isRedundency)
				actionSucceeded &= setPciRanges(true);
			if (!skipReboot) {
				report.startLevel("Disable Home Enb Feature.");
				actionSucceeded &= enodeBConfig.setProfile(dut1, EnbProfiles.Network_Profile,
						dut1.getDefaultNetspanProfiles().getNetwork());
				actionSucceeded &= enodeBConfig.setEnbType(dut1, EnbTypes.MACRO);
				actionSucceeded &= enodeBConfig.performReProvision(dut1);
				actionSucceeded &= dut1.reboot();
				GeneralUtils.unSafeSleep(60 * 1000);
				actionSucceeded &= dut1.waitForAllRunningAndInService(EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
				report.stopLevel();
			}
			return actionSucceeded;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	boolean setPciRanges(boolean isDefault) {
		boolean actionState = true;
		if (isDefault) {
			actionState &= enodeBConfig.returnPciRangesConfigToDefault(dut1);
			actionState &= enodeBConfig.returnPciRangesConfigToDefault(dut2);
			return actionState;
		}
		actionState &= enodeBConfig.addPciRangeHomeEnbConfig(HO.dut1, 503, 503, 0, 0);
		actionState &= enodeBConfig.addPciRangeHomeEnbConfig(HO.dut2, 0, 503, 0, 0);
		return actionState;
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

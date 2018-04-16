package testsNG.PlatformAndSecurity.PowerControl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import org.junit.Test;
import EnodeB.EnodeB.Architecture;
import Netspan.API.Enums.EnbStates;
import Netspan.API.Lte.AlarmInfo;
import Netspan.API.Lte.RFStatus;
import Netspan.Profiles.RadioParameters;
import UE.UE;
import Utils.GeneralUtils;
import Utils.SetupUtils;
import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;
import testsNG.Actions.AlarmsAndEvents;
import testsNG.Actions.Statuses;
import testsNG.Actions.Traffic.TrafficType;
import testsNG.Actions.Utils.ParallelCommandsThread;

/**
 * @author Meital Mizrachi
 */

public class P0 extends PowerControlBase {

	@Test // 1
	@TestProperties(name = "Close_Loop_Power_Control_With_TX_Power_26", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void closeLoopPowerControlWithTxPower26() {
		if (!(dut.getArchitecture() == Architecture.XLP)) {
			report.report("The Enb is not XLP", Reporter.WARNING);
			reason = "The Enb is not XLP";
			return;
		}
		try {
			dut.lteCli("logger threshold set process=rfMgr cli=0");
			changeTxPowerTest(26);
		} catch (Exception e) {
			report.report("changeTxPowerTest failed due to: " + e.getMessage(), Reporter.FAIL);
			e.printStackTrace();
		}
		int logLevel = dut.getSSHlogSession().getLogLevel();
		dut.lteCli("logger threshold set process=rfMgr cli="+logLevel);
	}

	@Test // 2
	@TestProperties(name = "Close_Loop_Power_Control_With_TX_Power_30", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void closeLoopPowerControlWithTxPower30() {
		if (dut!=null && !(dut.getArchitecture() == Architecture.XLP)) {
				report.report("The Enb is not XLP", Reporter.WARNING);
				reason = "The Enb is not XLP";
				return;
			}
		
		try {
			dut.lteCli("logger threshold set process=rfMgr cli=0");
			changeTxPowerTest(30);
		} catch (Exception e) {
			report.report("changeTxPowerTest failed due to: " + e.getMessage(), Reporter.FAIL);
			e.printStackTrace();
		}
		int logLevel = dut.getSSHlogSession().getLogLevel();
		dut.lteCli("logger threshold set process=rfMgr cli="+logLevel);
	}

	@Test // 3
	@TestProperties(name = "Close_Loop_Power_Control_With_Max_Power", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void closeLoopPowerControlWithMaxPower() {
			if (!(dut.getArchitecture() == Architecture.XLP)) {
				report.report("The Enb is not XLP", Reporter.WARNING);
				reason = "The Enb is not XLP";
				return;
			}
			dut.lteCli("logger threshold set process=rfMgr cli=0");
			if (enbConfig.getNodeInfo(dut) != null) {
				String productCode = enbConfig.getNodeInfo(dut).productCode;
				int txMaxPower = enbConfig.readTxMaxPowerOfProductViaShell(dut,productCode);

				if (txMaxPower == GeneralUtils.ERROR_VALUE) {
					report.report("Product code: +" + productCode + " does not exist in CSV file", Reporter.FAIL);
					reason = "Product code: " + productCode + " does not exist in CSV file";
					return;
				} else {
					report.report("Max power of " + dut.getName() + " is: " + txMaxPower + "dBm");
				}
				try {
					changeTxPowerTest(txMaxPower);
				} catch (Exception e) {
					report.report("changeTxPowerTest failed due to: " + e.getMessage(), Reporter.FAIL);
					e.printStackTrace();
				}
			}
			int logLevel = dut.getSSHlogSession().getLogLevel();
			dut.lteCli("logger threshold set process=rfMgr cli="+logLevel);
	}

	@Test // 5
	@TestProperties(name = "VSWR_Measurement_10_Times", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void vswrMeasurement() {
		if (!(dut.getArchitecture() == Architecture.XLP)) {
			report.report("The Enb is not XLP", Reporter.WARNING);
			reason = "The Enb is not XLP";
			return;
		}
		
		//db get Inventory productCode
		String oid = mibReader.resolveByName("asMaxCmInventoryProductCode");
		String productCode = dut.getSNMP(oid, 2);
		if(!productCode.contains("DFEM")) {
			report.report("The device does not support VSWR Measurements", Reporter.WARNING);
			reason = "The device does not support VSWR Measurements";
			return;
		}
		
		syncGeneralCommands();

		for (int i = 1; i <= 10; i++) {
			GeneralUtils.startLevel(i + ". VSWR Measurement");
			
			GeneralUtils.startLevel("Change Tx power to 30dBm");
			RadioParameters radioParams = new RadioParameters();
			radioParams.setTxpower(30);
			boolean flag = enbConfig.cloneAndSetRadioProfileViaNetSpan(dut, dut.getDefaultNetspanProfiles().getRadio(), radioParams);
			
			if (flag == false) {
				reason = "Failed to Clone and Set Radio Profile Via NetSpan";
				syncCommands.stopCommands();
				return;
			}
			GeneralUtils.stopLevel();
			
			report.report("Wait 10 seconds");
			GeneralUtils.unSafeSleep(10000);

			List<RFStatus> rfstatuses = status.getRFStatus(dut);
			for (RFStatus rfstat : rfstatuses) {
				if (rfstat.MeasuredVswr != null) {
					float vswr = GeneralUtils.tryParseStringToFloat(rfstat.MeasuredVswr);
					if (VSWR_LOWER_LIMIT <= vswr && vswr <= VSWR_WARNING_LEVEL) {
						report.report(rfstat.RfNumber + ": VSWR = " + vswr + " (" + VSWR_LOWER_LIMIT + " <= VSWR <= "
								+ VSWR_WARNING_LEVEL + ")");
					} else if (VSWR_WARNING_LEVEL < vswr && vswr <= VSWR_UPPER_LIMIT) {
						report.report(rfstat.RfNumber + ": VSWR = " + vswr + " (" + VSWR_WARNING_LEVEL + " < VSWR <= "
								+ VSWR_UPPER_LIMIT + ")", Reporter.WARNING);
						reason = rfstat.RfNumber + ": VSWR = " + vswr + " (" + VSWR_WARNING_LEVEL + " < VSWR <= "
								+ VSWR_UPPER_LIMIT + ")";
					} else if (VSWR_UPPER_LIMIT < vswr) {
						report.report(rfstat.RfNumber + ": VSWR is more then threshold(" + VSWR_UPPER_LIMIT
								+ "), actual: " + vswr, Reporter.FAIL);
						reason = rfstat.RfNumber + ": VSWR is more then threshold(" + VSWR_UPPER_LIMIT + "), actual: "
								+ vswr;
					} else {
						report.report(rfstat.RfNumber + ": VSWR is less than threshold(" + VSWR_LOWER_LIMIT
								+ "), actual: " + vswr, Reporter.FAIL);
						reason = rfstat.RfNumber + ": VSWR is less than threshold(" + VSWR_LOWER_LIMIT + "), actual: "
								+ vswr;
					}
				} else {
					report.report("Cannot measure VSWR on " + rfstat.RfNumber, Reporter.FAIL);
					reason = "Cannot measure VSWR on " + rfstat.RfNumber;
				}
			}
			
			GeneralUtils.startLevel("Revert to default Radio Profile and delete cloned profile");
			enbConfig.setEnbRadioProfile(dut, dut.defaultNetspanProfiles.getRadio(), false);
			enbConfig.deleteClonedProfiles();
			report.report("Wait 5 seconds");
			GeneralUtils.unSafeSleep(5000);
			GeneralUtils.stopLevel();
			
			GeneralUtils.stopLevel();
		}		
		
		GeneralUtils.startLevel("Stopping Parallel Commands");
		report.report("db get rfStatus");
		report.report("db get cellCFg txpower");
		syncCommands.stopCommands();
		GeneralUtils.stopLevel();
		
		syncCommands.moveFileToReporterAndAddLink();
	}

	@Test // 6
	@TestProperties(name = "Transmitter_Disable", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void transmitterDisable() {
			if (staticUEs == null) {
				report.report("No Static UEs defined in the Test", Reporter.WARNING);
			}
			GeneralUtils.startLevel((String.format("Starting all ues.")));
			ArrayList<UE> ues = new ArrayList<UE>();
			ues.addAll(SetupUtils.getInstance().getDynamicUEs());
			ues.addAll(staticUEs);
			
			for (UE ue : ues) {
				if(!ue.start())
					report.report("Failed starting UE " + ue.getName(),Reporter.WARNING);
				GeneralUtils.unSafeSleep(1000);
			}
			GeneralUtils.stopLevel();
			
			//step1
			report.report("Start traffic");
			traffic.setTrafficType(TrafficType.HO);
			if(!traffic.startTraffic(staticUEs))
				report.report("Failed starting traffic",Reporter.WARNING);

			report.report("Wait 2 Minutes");
			GeneralUtils.unSafeSleep(60 * 1000 * 2);
			
			int txPower = dut.getTxPower();
			EnbStates channelStatus = dut.getServiceState();
			if (channelStatus.equals(EnbStates.IN_SERVICE)) {
				report.report("ChannelStatusInfo: " + channelStatus);
			} else {
				report.report("ChannelStatusInfo: " + channelStatus + ", Instead of: " + EnbStates.IN_SERVICE,
						Reporter.FAIL);
				reason += "Enb Status Info: " + channelStatus + ", Instead of: " + EnbStates.IN_SERVICE;
			}
			report.report("Enb txpower: " + txPower);

			GeneralUtils.startLevel("Connecting UEs");
			if(!tryingConnectUEsAndSendTraffic(ues))
			{
				report.report("Failed connecting UEs",Reporter.WARNING);
			}

			GeneralUtils.stopLevel();
			//step2
			channelStatus = dut.getServiceState();
			if(!channelStatus.equals(EnbStates.IN_SERVICE))
			{
				report.report("ChannelStatusInfo: " + channelStatus +" instead of " + EnbStates.IN_SERVICE, Reporter.FAIL);
				reason += "ChannelStatusInfo: " + channelStatus +" instead of " + EnbStates.IN_SERVICE;
			} else {
				report.report("ChannelStatusInfo: " + channelStatus);
			}
			
			for (RFStatus rfStatus : Statuses.getInstance().getRFStatus(dut)) {
				if(rfStatus.OperationalStatus.equals("InService"))
				{
					report.report("RFStatus operational: " + rfStatus.RfNumber  +", status: " + rfStatus.OperationalStatus );
					
				} else {
					report.report("RFStatus operational: " + rfStatus.RfNumber  +", status: " + rfStatus.OperationalStatus + " instead of InService" , Reporter.FAIL);
					reason += "RFStatus operational: " + rfStatus.RfNumber  +", status: " + rfStatus.OperationalStatus + " instead of InService";
				}
				
			} 
			//step3
			GeneralUtils.startLevel("Change node state via NMS from IN_SERVICE to OUT_OF_SERVICE");
			peripheralsConfig.changeEnbState(dut, EnbStates.OUT_OF_SERVICE);
			GeneralUtils.stopLevel();
			report.report("Wait 10 seconds");
			GeneralUtils.unSafeSleep(10000);
			
			channelStatus = dut.getServiceState();
			if(!channelStatus.equals(EnbStates.OUT_OF_SERVICE))
			{
				report.report("ChannelStatusInfo: " + channelStatus +" instead of " + EnbStates.OUT_OF_SERVICE, Reporter.FAIL);
				reason += "ChannelStatusInfo: " + channelStatus +" instead of " + EnbStates.OUT_OF_SERVICE;
			} else {
				report.report("ChannelStatusInfo: " + channelStatus);
			}
			
			AlarmsAndEvents alarams = AlarmsAndEvents.getInstance();
			List<AlarmInfo> alarmsInfo = alarams.getAllAlarmsNode(dut);
			boolean alarmFound = false;
			for (AlarmInfo alarminfo : alarmsInfo) {
				if(alarminfo.alarmInfo.contains("Out of Service")){
					alarmFound = true;
					break;
				}
			}
			if (alarmFound) {
				report.report("Out of service alarm found!");
			} else {
				report.report("Out of service alarm NOT found!", Reporter.FAIL);
			}
			
			if(tryingConnectUEsAndSendTraffic(ues))
			{
				report.report("Ues connected when enodeb should be out of service!", Reporter.FAIL);
			}
			//step4
			
			GeneralUtils.startLevel("Change node state via NMS from OUT_OF_SERVICE to IN_SERVICE");
			peripheralsConfig.changeEnbState(dut, EnbStates.IN_SERVICE);

			GeneralUtils.stopLevel();

			report.report("Wait 10 seconds");
			GeneralUtils.unSafeSleep(10000);
			if(!tryingConnectUEsAndSendTraffic(ues))
			{
				//list is updated from tryingConnectUEsAndSendTraffic method that UEs peripheralsConfig.
				int numOfUesNotConnected = peripheralsConfig.getUesNotConnected().size();
				if(numOfUesNotConnected == ues.size()){
					report.report("Failed connecting UEs",	Reporter.FAIL);
				}else{
					report.report("at least one UE connected");
				}
			}
			
			report.report("Stopping traffic");
			traffic.stopTraffic();
	}

	@Test // 7
	@TestProperties(name = "Verify_Transmitter_Disable_After_System_Reboot", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void verifyTransmitterDisableAfterSystemReboot() {
		
			ArrayList<String> commands = new ArrayList<>();
			commands.add("cell show operationalstatus");
			commands.add("db get rfstatus");
			commands.add("db get PTPStatus");
			ArrayList<String> danCommands = new ArrayList<>();
			danCommands.add("dancli HWM_Msr_Prnt 1");
			danCommands.add("dancli HWM_Msr_Prnt 50");
			dut.lteCli("logger threshold set process=rfMgr cli=0");
			try {
				commandsThread = new ParallelCommandsThread(commands, dut, null, danCommands);
			} catch (IOException e) {
				report.report("could not init Commands in parallel", Reporter.WARNING);
			}
			commandsThread.start();
			
			if (peripheralsConfig.changeEnbState(dut, EnbStates.OUT_OF_SERVICE)) {
				report.report("Enb is in OUT OF SERVICE");
			}

			report.report("Wait 10 seconds");
			GeneralUtils.unSafeSleep(10000);

			GeneralUtils.startLevel("Reboot Enb");
			dut.reboot();
			GeneralUtils.stopLevel();

			report.report(dut.getName() + ": Wait for Reachable (TimeOut=10 Minutes)");
			dut.waitForReachable(10 * 1000 * 60);
			
			report.report(dut.getName() + ": Wait for All Running (TimeOut=15 Minutes)");
			dut.verifyNotReachAllRunningState(15 * 1000 * 60);
			
			String stateService = "";
			
			stateService  = dut.getCellServiceState();
			if (stateService.equals("0")) {
				report.report("Enb is in Out Of Service");
				report.report("After reboot of the system, the eNB is still Out Of Service");
			} else if(stateService.equals("1")) {
				report.report("Enb is NOT OUT_OF_SERVICE");
				report.report("After reboot of the system, the Enb changed from Out Of Service to In Service",
						Reporter.FAIL);
				reason += "After reboot of the system, the Enb changed from Out Of Service to In Service";
			} else {
				report.report("Enb is not reachable");
				report.report("After reboot of the system, the Enb is not reachable",
						Reporter.FAIL);
				reason += "After reboot of the system, the Enb  is not reachable";
			}

			report.report("Revert Enb to be IN SERVICE");
			peripheralsConfig.changeEnbState(dut, EnbStates.IN_SERVICE);
			
			commandsThread.stopCommands();
			commandsThread.moveFileToReporterAndAddLink();
			int logLevel = dut.getSSHlogSession().getLogLevel();
			dut.lteCli("logger threshold set process=rfMgr cli="+logLevel);
	}
}

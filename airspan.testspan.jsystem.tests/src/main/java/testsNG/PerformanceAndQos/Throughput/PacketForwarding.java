package testsNG.PerformanceAndQos.Throughput;

import java.util.ArrayList;
import org.junit.Test;
import EnodeB.EnodeB;
import Entities.StreamParams;
import Entities.ITrafficGenerator.CounterUnit;
import Entities.ITrafficGenerator.Protocol;
import Entities.ITrafficGenerator.TransmitDirection;
import Netspan.API.Enums.HandoverType;
import Netspan.API.Enums.HoControlStateTypes;
import Netspan.API.Enums.X2ControlStateTypes;
import UE.UE;
import Utils.GeneralUtils;
import Utils.Pair;
import Utils.SetupUtils;
import Utils.SysObjUtils;
import jsystem.framework.ParameterProperties;
import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;
import testsNG.Actions.Neighbors;

public class PacketForwarding extends TPTBase {
	protected EnodeB dut2;
	protected Neighbors neighborsUtils;
	protected ArrayList<CounterUnit> counters = new ArrayList<CounterUnit>();
	protected final int HO_TIMES = 10;
	protected final String QCI_7 = "7";
	protected final String QCI_9 = "9";
	protected final String DL_PORT = "DL";
	protected final String UL_PORT = "UL";
	protected final String AM_ERAB = "AM";
	protected final String UM_ERAB = "UM";

	@Override
	public void init() {
		try {
			enbInTest = new ArrayList<EnodeB>();
			enbInTest.add(dut2);
			super.init();
			neighborsUtils = Neighbors.getInstance();
			if (!dut.isInService()) {
				report.report("eNodeB " + dut.getNetspanName() + " is Out Of Service", Reporter.WARNING);
			} else if (!dut2.isInService()) {
				report.report("eNodeB " + dut2.getNetspanName() + " is Out Of Service", Reporter.WARNING);
			}
			if (peripheralsConfig.SetAttenuatorToMin(attenuatorSetUnderTest)) {
				report.report("Successfully set attenuator to minimum");
			}
		} catch (Exception e) {
			report.report("Failed to init test due to: " + e.getMessage(), Reporter.WARNING);
			reason = "Failed to init test due to: " + e.getMessage();
		}
		ueList = SetupUtils.getInstance().getAllUEs();
		peripheralsConfig.startUEs(ueList);

		counters.add(CounterUnit.FRAME_COUNT);
		counters.add(CounterUnit.DROPPED_FRAME_COUNT);
		counters.add(CounterUnit.DROPPED_FRAME_PERCENT);

		dut.setSessionLogLevel("PKT_FWD", "*", 1);
		dut2.setSessionLogLevel("PKT_FWD", "*", 1);

		dut.setSessionLogLevel("PTPd INFO", "*", 6);
		dut2.setSessionLogLevel("PTPd INFO", "*", 6);

		if ((dut.getPacketForwardingEnable(1) == 0) || (dut2.getPacketForwardingEnable(1) == 0)) {
			report.report("Setting PktFwd flag to enable");
			dut.setPacketForwardingEnable(true, dut.getCellContextID());
			dut2.setPacketForwardingEnable(true, dut.getCellContextID());
			dut.reboot();
			dut2.reboot();
			report.report("Wait 20 seconds");
			GeneralUtils.unSafeSleep(1000 * 20);
			dut.waitForAllRunningAndInService(EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
			dut2.waitForAllRunningAndInService(1 * 60 * 1000);
		}
	}

	@ParameterProperties(description = "Second DUT")
	public void setDUT2(String dut) {
		GeneralUtils.printToConsole("Load DUT2 " + dut);
		this.dut2 = (EnodeB) SysObjUtils.getInstnce().initSystemObject(EnodeB.class, false, dut).get(0);
		GeneralUtils.printToConsole("DUT2 loaded" + this.dut2.getNetspanName());
	}

	@Test
	@TestProperties(name = "Stream_1Mbps_DL_Packets_In_both_AM_and_UM_ERABs_and_perform_multiple_X2_HOs", returnParam = {
			"IsTestWasSuccessful" }, paramsInclude = {"DUT", "DUT2"})
	public void stream1MDLPacketsInBothAMAndUMERABsAndPerformMultipleX2HOs() {
		String hoCounterString = "HoX2IntraFreqInCompSuccRnlRadioRsn";
		neighborsUtils.addNeighbor(dut, dut2, HoControlStateTypes.ALLOWED, X2ControlStateTypes.AUTOMATIC,
				HandoverType.TRIGGER_X_2, true, "0");
		neighborsUtils.addNeighbor(dut2, dut, HoControlStateTypes.ALLOWED, X2ControlStateTypes.AUTOMATIC,
				HandoverType.TRIGGER_X_2, true, "0");
		testName = "Stream_1Mbps_DL_Packets_In_both_AM_and_UM_ERABs_and_perform_multiple_X2_HOs";
		packetForwardingTest(1, 1, 800, hoCounterString);
		neighborsUtils.deleteNeighbor(dut, dut2);
	}

	@Test
	@TestProperties(name = "Stream_1Mbps_DL_Packets_In_both_AM_and_UM_ERABs_and_perform_multiple_S1_HOs", returnParam = {
			"IsTestWasSuccessful" }, paramsInclude = {"DUT", "DUT2"})
	public void stream1MDLPacketsInBothAMAndUMERABsAndPerformMultipleS1HOs() {
		String hoCounterString = "HoS1IntraFreqInCompSuccRnlRadioRsn";
		neighborsUtils.addNeighbor(dut, dut2, HoControlStateTypes.ALLOWED, X2ControlStateTypes.NOT_ALLOWED,
				HandoverType.S_1_ONLY, true, "0");
		neighborsUtils.addNeighbor(dut2, dut, HoControlStateTypes.ALLOWED, X2ControlStateTypes.NOT_ALLOWED,
				HandoverType.S_1_ONLY, true, "0");
		testName = "Stream_1Mbps_DL_Packets_In_both_AM_and_UM_ERABs_and_perform_multiple_S1_HOs";
		packetForwardingTest(1, 1, 800, hoCounterString);
		neighborsUtils.deleteNeighbor(dut, dut2);
	}

	@Test
	@TestProperties(name = "Stream_10Mbps_DL_Packets_In_both_AM_and_UM_ERABs_and_perform_multiple_X2_HOs", returnParam = {
			"IsTestWasSuccessful" }, paramsInclude = {"DUT", "DUT2"})
	public void stream10MDLPacketsInBothAMAndUMERABsAndPerformMultipleX2HOs() {
		String hoCounterString = "HoX2IntraFreqInCompSuccRnlRadioRsn";
		neighborsUtils.addNeighbor(dut, dut2, HoControlStateTypes.ALLOWED, X2ControlStateTypes.AUTOMATIC,
				HandoverType.TRIGGER_X_2, true, "0");
		neighborsUtils.addNeighbor(dut2, dut, HoControlStateTypes.ALLOWED, X2ControlStateTypes.AUTOMATIC,
				HandoverType.TRIGGER_X_2, true, "0");
		testName = "Stream_10Mbps_DL_Packets_In_both_AM_and_UM_ERABs_and_perform_multiple_X2_HOs";
		packetForwardingTest(10, 1, 800, hoCounterString);
		neighborsUtils.deleteNeighbor(dut, dut2);
	}

	@Test
	@TestProperties(name = "Stream_10Mbps_DL_Packets_In_both_AM_and_UM_ERABs_and_perform_multiple_S1_HOs", returnParam = {
			"IsTestWasSuccessful" }, paramsInclude = {"DUT", "DUT2"})
	public void stream10MDLPacketsInBothAMAndUMERABsAndPerformMultipleS1HOs() {
		String hoCounterString = "HoS1IntraFreqInCompSuccRnlRadioRsn";
		neighborsUtils.addNeighbor(dut, dut2, HoControlStateTypes.ALLOWED, X2ControlStateTypes.NOT_ALLOWED,
				HandoverType.S_1_ONLY, true, "0");
		neighborsUtils.addNeighbor(dut2, dut, HoControlStateTypes.ALLOWED, X2ControlStateTypes.NOT_ALLOWED,
				HandoverType.S_1_ONLY, true, "0");
		testName = "Stream_10Mbps_DL_Packets_In_both_AM_and_UM_ERABs_and_perform_multiple_S1_HOs";
		packetForwardingTest(10, 1, 800, hoCounterString);
		neighborsUtils.deleteNeighbor(dut, dut2);
	}

	@Test
	@TestProperties(name = "Stream_35Mbps_DL_Packets_In_both_AM_and_UM_ERABs_and_perform_multiple_X2_HOs", returnParam = {
			"IsTestWasSuccessful" }, paramsInclude = {"DUT", "DUT2"})
	public void stream35MDLPacketsInBothAMAndUMERABsAndPerformMultipleX2HOs() {
		String hoCounterString = "HoX2IntraFreqInCompSuccRnlRadioRsn";
		neighborsUtils.addNeighbor(dut, dut2, HoControlStateTypes.ALLOWED, X2ControlStateTypes.AUTOMATIC,
				HandoverType.TRIGGER_X_2, true, "0");
		neighborsUtils.addNeighbor(dut2, dut, HoControlStateTypes.ALLOWED, X2ControlStateTypes.AUTOMATIC,
				HandoverType.TRIGGER_X_2, true, "0");
		testName = "Stream_35Mbps_DL_Packets_In_both_AM_and_UM_ERABs_and_perform_multiple_X2_HOs";
		packetForwardingTest(35, 5, 1400, hoCounterString);
		neighborsUtils.deleteNeighbor(dut, dut2);
	}

	@Test
	@TestProperties(name = "Stream_35Mbps_DL_Packets_In_both_AM_and_UM_ERABs_and_perform_multiple_S1_HOs", returnParam = {
			"IsTestWasSuccessful" }, paramsInclude = {"DUT", "DUT2"})
	public void stream35MDLPacketsInBothAMAndUMERABsAndPerformMultipleS1HOs() {
		String hoCounterString = "HoS1IntraFreqInCompSuccRnlRadioRsn";
		neighborsUtils.addNeighbor(dut, dut2, HoControlStateTypes.ALLOWED, X2ControlStateTypes.NOT_ALLOWED,
				HandoverType.S_1_ONLY, true, "0");
		neighborsUtils.addNeighbor(dut2, dut, HoControlStateTypes.ALLOWED, X2ControlStateTypes.NOT_ALLOWED,
				HandoverType.S_1_ONLY, true, "0");
		testName = "Stream_35Mbps_DL_Packets_In_both_AM_and_UM_ERABs_and_perform_multiple_S1_HOs";
		packetForwardingTest(35, 5, 1400, hoCounterString);
		neighborsUtils.deleteNeighbor(dut, dut2);
	}

	// @Test
	@TestProperties(name = "Stream_1M_DL_packets_in_both_AM_and_UM_ERABs_and_perform_multiple_X2_HOs_Intra_Enb_Multi_Cell", returnParam = {
			"IsTestWasSuccessful" }, paramsInclude = {"DUT", "DUT2"})
	public void stream1MDLpacketsInBothAMandUMERABsAndPerformMultipleX2HOsIntraEnbMultiCell() {
	}

	public boolean packetForwardingThroughputProcess(double DL, double UL, ArrayList<UE> UEs) {
		try {
			resetTestBol = false;

			// init and start traffic
			if (!trafficSTC.startTraffic()) {
				return false;
			}
			GeneralUtils.startLevel("Disable un-needed streams");
			trafficSTC.disableUnneededStreams(Protocol.UDP,ueNameListStc, qci);
			// if the last ue deleted and there is nothing to check!
			if (ueNameListStc.size() == 0) {
				report.report("No UEs in Test", Reporter.WARNING);
				GeneralUtils.stopLevel();
			}
			GeneralUtils.stopLevel();

			GeneralUtils.startLevel("Disable un-needed UEs");
			disableUEsNotInTest(ueNameListStc, UEs);
			GeneralUtils.stopLevel();

			// init Frame Size
			GeneralUtils.startLevel("init Frame Size and taking off un-needed streams");
			trafficSTC.initFrameSize(packetSize, streams);
			GeneralUtils.stopLevel();

			Pair<Double, Double> trafficValuPair = Pair.createPair(DL, UL);
			trafficSTC.trafficLoadSet(trafficValuPair);

			// Save Configurate File and ReStarting Traffic

			report.report("Packet Size: " + packetSize + "Byte");

			report.report("Save Config File and Start Traffic");
			uploadConfigFileToReport();
			trafficSTC.saveConfigFileAndStart();

			report.report("Wait 10 seconds");
			GeneralUtils.unSafeSleep(1000 * 10);

			trafficSTC.clearStreamsResultsSTC();

			// unExcpected Exception during loading file
		} catch (Exception e) {
			e.printStackTrace();

			GeneralUtils.printToConsole("startTraffic Proccess Failed due to: " + e.getMessage());
			GeneralUtils.stopLevel();
			return false;
		}
		return true;
	}

	public boolean packetForwardingHandOverProcess(EnodeB enb1, EnodeB enb2, int HOtimes, String counterString) {
		if (HOtimes % 2 != 0) {
			HOtimes++;
		}
		resetAllHandOverCounters(enb1, enb2, 3, counterString);

		GeneralUtils.startLevel("Moving attenuator " + HOtimes + " times to create Hand-Over");
		for (int i = 0; i < HOtimes / 2; i++) {
			report.report("Moving attenuator from " + attenuatorSetUnderTest.getMinAttenuation() + " to "
					+ attenuatorSetUnderTest.getMaxAttenuation() + " to create Hand-Over");
			peripheralsConfig.moveAtt(attenuatorSetUnderTest, attenuatorSetUnderTest.getMinAttenuation(),
					attenuatorSetUnderTest.getMaxAttenuation());
			report.report("Moving attenuator from " + attenuatorSetUnderTest.getMaxAttenuation() + " to "
					+ attenuatorSetUnderTest.getMinAttenuation() + " to create Hand-Over");
			peripheralsConfig.moveAtt(attenuatorSetUnderTest, attenuatorSetUnderTest.getMaxAttenuation(),
					attenuatorSetUnderTest.getMinAttenuation());
		}
		GeneralUtils.stopLevel();
		enbConfig.waitGranularityPeriodTime(enb1);

		int enb1NewCounter = enb1.getCountersValue(counterString);
		int enb2NewCounter = enb2.getCountersValue(counterString);

		report.report(enb1.getName() + " new HO Counter "+counterString+": " + enb1NewCounter);
		report.report(enb2.getName() + " new HO Counter "+counterString+": " + enb2NewCounter);

		int sumNewCounter = enb1NewCounter + enb2NewCounter;
		int sumCounterExpected = HOtimes * SetupUtils.getInstance().getDynamicUEs().size();

		if (sumNewCounter < sumCounterExpected) {
			report.report("Sum HO Counters actual: " + sumNewCounter
					+ ", Sum HO Counters expected(HOtimes*dynamicUEs): " + sumCounterExpected);
			return false;
		} else {
			report.report("All Hand-Over succeeded");
			return true;
		}
	}

	public Long checkPacketLost(ArrayList<StreamParams> rxAndTxValues, Long sumDroppedExpected,
			String port, String ERAB, String qci) {
		long streamTxSum = 0, streamRxSum = 0, droppedFrameCountSum = 0;
		for(StreamParams params : rxAndTxValues){
			if(params.getName().contains(port) && params.getName().endsWith(qci)){
				if(params.getUnit() == CounterUnit.FRAME_COUNT){
					streamTxSum += params.getTxRate();
					streamRxSum += params.getRxRate();
				}else if(params.getUnit() == CounterUnit.DROPPED_FRAME_COUNT){
					droppedFrameCountSum += params.getRxRate();
				}
			}
		}
		
		GeneralUtils.startLevel(port + " " + ERAB + " QCI " + qci + " Streams Result");
		report.report("Sum TX: " + streamTxSum + " Packets");
		if (streamRxSum <= 0) {
			report.report("Sum RX: " + streamRxSum + " Packets", Reporter.FAIL);
			reason = "Traffic flow failed, RX=0";
		} else {
			report.report("Sum RX: " + streamRxSum + " Packets");
		}
		double packetLostlimitToSuccess = streamTxSum * 0.000001; // 〖~10〗^(-6)
																	// packet
																	// lost

		if (droppedFrameCountSum == 0) {
			report.report("Sum Dropped: " + droppedFrameCountSum + " Packets");
		} else if (packetLostlimitToSuccess >= droppedFrameCountSum) {
			report.report("Sum Dropped: " + droppedFrameCountSum + " Packets (Limit to success〖~10〗^(-6) packet lost)");
		} else if ((sumDroppedExpected != null) && (droppedFrameCountSum - sumDroppedExpected) > 0) {
			if (qci.equals(QCI_7)) {
				report.report("Sum Dropped: " + droppedFrameCountSum + " Packets", Reporter.WARNING);
				reason = "UM Packets lost more than expected";
			} else { // QCI_9
				report.report("Sum Dropped: " + droppedFrameCountSum + " Packets", Reporter.FAIL);
				reason = "AM Packets lost more than expected";
			}
		} else {
			report.report("Sum Dropped: " + droppedFrameCountSum + " Packets", Reporter.WARNING);
			reason = "Packets lost more than expected";
		}
		GeneralUtils.stopLevel();
		return droppedFrameCountSum;
	}

	public void printStreams(ArrayList<StreamParams> rxAndTxValues) {
		GeneralUtils.startLevel("Print All Streams");
		printStreamsByPort(rxAndTxValues, DL_PORT);
		printStreamsByPort(rxAndTxValues, UL_PORT);
		GeneralUtils.stopLevel();
	}

	public void printStreamsByPort(ArrayList<StreamParams> rxAndTxValues, String port) {
		long streamTx = 0, streamRx = 0, droppedFrameCount = 0;
		double droppedFramePercent = 0;
		for(StreamParams params : rxAndTxValues){
			if(params.getName().contains(port)){
				if(params.getUnit() == CounterUnit.FRAME_COUNT){
					streamTx = params.getTxRate();
					streamRx = params.getRxRate();
				}else if(params.getUnit() == CounterUnit.DROPPED_FRAME_COUNT){
					droppedFrameCount = params.getRxRate();
				}else if(params.getUnit() == CounterUnit.DROPPED_FRAME_PERCENT){
					droppedFramePercent = params.getRxRateDouble();
				}
				if (streamRx <= 0) {
					report.report(params.getName() + ": txFrameCount=" + streamTx + ", rxFrameCount=" + streamRx
							+ ", droppedFrameCount=" + droppedFrameCount + ", droppedFramePercent="
							+ droppedFramePercent + "%", Reporter.WARNING);
				} else {
					report.report(params.getName() + ": txFrameCount=" + streamTx + ", rxFrameCount=" + streamRx
							+ ", droppedFrameCount=" + droppedFrameCount + ", droppedFramePercent="
							+ droppedFramePercent + "%");
				}
			}
		}
	}

	public void resetAllHandOverCounters(EnodeB enb1, EnodeB enb2, int attemptsNumber, String counterString) {
		GeneralUtils.startLevel("Reset all HO counters (" + attemptsNumber + " attempts)");
		for (int i = 0; i < attemptsNumber; i++) {
			report.report(enb1.getName() + " reset all counters");
			enb1.resetCounter(null, null, null);

			report.report(enb2.getName() + " reset all counters");
			enb2.resetCounter(null, null, null);

			enbConfig.waitGranularityPeriodTime(enb1);

			int enb1Counter = enb1.getCountersValue(counterString);
			int enb2Counter = enb2.getCountersValue(counterString);

			report.report(enb1.getName() + " HO Counter "+counterString+": " + enb1Counter);
			report.report(enb2.getName() + " HO Counter "+counterString+": " + enb2Counter);

			if (enb1Counter == 0 && enb2Counter == 0) {
				report.report("reset all HO counters Succeeded");
				GeneralUtils.stopLevel();
				return;
			} else {
				report.report("reset all HO counters failed", Reporter.WARNING);
			}
		}
		GeneralUtils.stopLevel();
	}

	public void packetForwardingTest(int dl, int ul, int packetSizeInput, String hoCounterString) {
		packetSize = packetSizeInput;
		qci.add('9');
		qci.add('7');
		ueNameListStc = convertUeToNamesList(ueList);
		if (!packetForwardingThroughputProcess(dl, ul, ueList)) {
			report.report("Start Traffic failed", Reporter.FAIL);
			reason = "Start Traffic failed";
			return;
		}
		if (!peripheralsConfig.epcAndEnodeBsConnection(ueList, enbInTest)) {
			report.report("After traffic UE is not connected", Reporter.FAIL);
			reason = "After traffic UE is not connected";
			trafficSTC.stopTraffic();
			return;
		}
		report.report("Wait 5 minutes");
		GeneralUtils.unSafeSleep(1000 * 60 * 5);

		ArrayList<StreamParams> rxAndTxValues = trafficSTC.getTxAndRxCounter(counters);
		printStreams(rxAndTxValues);

		Long sumDlQci9Dropped = checkPacketLost(rxAndTxValues, null, DL_PORT, AM_ERAB, QCI_9);
		Long sumDlQci7Dropped = checkPacketLost(rxAndTxValues, null, DL_PORT, UM_ERAB, QCI_7);

		if (!packetForwardingHandOverProcess(dut, dut2, HO_TIMES, hoCounterString)) {
			report.report("Some of HOs failed", Reporter.FAIL);
			reason = "Some of HOs failed";
		}
		rxAndTxValues = trafficSTC.getTxAndRxCounter(counters);
		printStreams(rxAndTxValues);

		checkPacketLost(rxAndTxValues, sumDlQci9Dropped, DL_PORT, AM_ERAB, QCI_9);
		checkPacketLost(rxAndTxValues, sumDlQci7Dropped, DL_PORT, UM_ERAB, QCI_7);

		trafficSTC.stopTraffic();
	}
}
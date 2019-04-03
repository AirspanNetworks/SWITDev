package Action.MultiEnodebAction;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Assume;
import org.junit.Test;

import Action.EnodebAction.EnodebAction;
import Attenuators.AttenuatorSet;
import EnodeB.EnodeB;
import UE.UE;
import Utils.CommonConstants;
import Utils.GeneralUtils;
import Utils.SetupUtils;
import Utils.SysObjUtils;
import jsystem.framework.ParameterProperties;
import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;
import testsNG.Actions.PeripheralsConfig;

public class MultiEnodebAction extends EnodebAction {
	
	private static int ATT_STEP_TIME = 500;
	private static int ATT_STEP = 2;
	private final float WARNING_LEVEL = 0.9f;
	private final float PASS_LEVEL = 0.98f;

	public static long maxduration;
	private static PeripheralsConfig peripheralsConfig;
	private static AttenuatorSet attenuatorSetUnderTest;
	static EnodeB enodeB;
	static EnodeB neighbor;
	int expectedNumOfHO = 30;
	public static HashMap<String, String> passCriteria = new HashMap<>();
	static ArrayList<UE> dynUEList = new ArrayList<>();

	//public static EnodeB dut1;
	//public static EnodeB dut2;
	private static int attenuatorMin;
	private static int attenuatorMax;

	public static String counterAttempt;
	public static String counterSuccess;
	public static String counterAttemptsnmp;
	public static String counterSuccesssnmp;

	private FrequencyType freqType = FrequencyType.Intra;
	private HandOverType hoType = HandOverType.S1;
	static Integer timeToHO = null;
	private Integer defaultTimeHO = 10;
	
	// results variables
	public static int numberOfDynamicUES;
	private static double HO_Per_Min_Per_UE;
	private static int first_ENB_succ = 0;
	private static int first_ENB_att = 0;
	private static double firstENB_HO_Success_rate_out_of_attempts;
	private static int second_ENB_succ = 0;
	private static int second_ENB_att = 0;
	private static double secondENB_HO_Success_rate_out_of_attempts;
	private static int total_succCounter;
	private static int total_attCounter;
	private static double total_HO_Success_rate_out_of_attempts;
	private static double theoreticalAttempts;
	
	private static boolean startedHO = false;
	private static boolean finishedHO = false;
	private static int finalCounter = 0;
	
	@Override
	public void init() {
        if (enodeB == null){
        	report.report("DUT1 is not loaded",Reporter.FAIL);
        	reason = "DUT1 is not loaded";
        	Assume.assumeTrue(false);        	
        }
		if (neighbor == null){
			report.report("DUT2 is not loaded",Reporter.FAIL);
			reason = "DUT2 is not loaded";
			Assume.assumeTrue(false);			
		}
		enbInTest = new ArrayList<>();
		enbInTest.add(enodeB);
		enbInTest.add(neighbor);
        super.init();
	}
	
	@Test
    @TestProperties(name = "Start HandOver", returnParam = {"LastStatus"}, paramsInclude = {"DUT1","DUT2","TimeToHO",
    		"FreqType","HoType"})
	public void startHO() {
		if(isStartedHO()){
			report.report("HO has already been started and cannot be started again", Reporter.FAIL);
			reason = "HO has already been started and cannot be started again";
			return;
		}
		if(timeToHO == null){
			report.report("No time was set. Default of " + defaultTimeHO + " minutes");
			timeToHO = defaultTimeHO;
		}
		preTest();		
		setCounters();
		report.report("Changing counters value to 0");
		resetCounters();
		int waitingPeriodTime = enodeB.getGranularityPeriod();
		report.report("Wait for " + waitingPeriodTime + " minutes for Counter to update");
		GeneralUtils.unSafeSleep(waitingPeriodTime * 60 * 1000);
		if (!gettingPassCratiriaValue()) {
			report.report("Failed to reset counters", Reporter.FAIL);
			reason = "Failed to reset counters";
			return;
		}
		report.report("Number of dynamic UES: "+dynUEList.size());
		report.report("Frequency type: "+freqType);
		report.report("Hand Over type: "+hoType);
		HandOverClass hoclass = new HandOverClass();
		hoclass.start();
    }
	
	@Test
    @TestProperties(name = "Get Statistics Hand Over", returnParam = {"LastStatus"}, paramsInclude = {})
	public void getStatisticsHO() {
		if(!isStartedHO()){
			report.report("Hand Over was not started. No results", Reporter.FAIL);
			reason = "Hand Over was not started. No results";
		}else{
			while(true){
				if(isFinishedHO()){
					LongHOResults();
					afterGetStats();
					return;
				}
				report.report("Hand Over did not finish. Wait more 5 minutes");
				GeneralUtils.unSafeSleep(5*60*1000);
			}
		}
	}
	
	private void afterGetStats(){
		setStartedHO(false);
		setFinishedHO(false);
		passCriteria.clear();
		first_ENB_succ = 0;
		first_ENB_att = 0;
		second_ENB_succ = 0;
		second_ENB_att = 0;
	}
	
	private void setCounters() {
		if(freqType == FrequencyType.Intra){
			if(hoType == HandOverType.S1){
				counterSuccess = "HoS1IntraFreqInCompSuccRnlRadioRsn";
				counterSuccesssnmp = "HoS1IntraFreqInCompSuccRnlRadioRsn";
				counterAttempt = "HoS1IntraFreqInPrepSuccRnlRadioRsn";
				counterAttemptsnmp = "HoS1IntraFreqInPrepSuccRnlRadioRsn";
			}else{
				counterSuccess = "HoX2IntraFreqInCompSuccRnlRadioRsn";
				counterSuccesssnmp = "HoX2IntraFreqInCompSuccRnlRadioRsn";
				counterAttempt = "HoX2IntraFreqInAttRnlRadioRsn";
				counterAttemptsnmp = "HoX2IntraFreqInAttRnlRadioRsn";
			}
		}else{
			if(hoType == HandOverType.S1){
				counterSuccess = "HoS1InterFreqInCompSuccRnlRadioRsn";
				counterSuccesssnmp = "HoS1InterFreqInCompSuccRnlRadioRsn";
				counterAttempt = "HoS1InterFreqInPrepSuccRnlRadioRsn";
				counterAttemptsnmp = "HoS1InterFreqInPrepSuccRnlRadioRsn";
			}else{
				counterSuccess = "HoX2InterFreqInCompSuccRnlRadioRsn";
				counterSuccesssnmp = "HoX2InterFreqInCompSuccRnlRadioRsn";
				counterAttempt = "HoX2InterFreqInPrepSuccRnlRadioRsn";
				counterAttemptsnmp = "HoX2InterFreqInPrepSuccRnlRadioRsn";
			}
		}
		
		passCriteria.put(counterSuccess, counterSuccesssnmp);
		passCriteria.put(counterAttempt, counterAttemptsnmp);
	}

	private void preTest() {
		dynUEList = SetupUtils.getInstance().getDynamicUEs();
		peripheralsConfig = PeripheralsConfig.getInstance();
		attenuatorSetUnderTest = AttenuatorSet.getAttenuatorSet(CommonConstants.ATTENUATOR_SET_NAME);
		attenuatorMin = attenuatorSetUnderTest.getMinAttenuation();
		attenuatorMax = attenuatorSetUnderTest.getMaxAttenuation();
		ATT_STEP_TIME = attenuatorSetUnderTest.getStepTime();
		ATT_STEP = attenuatorSetUnderTest.getAttenuationStep();
		
	}

	private static void resetCounters() {
		for (String counter : passCriteria.keySet()) {
			HashMap<String, String> counterValue = new HashMap<>();
			counterValue.put(counter, "0");

			boolean result1 = enodeB.resetCounter("hostats", "*", counterValue);
			if (!result1) {
				enodeB.resetCounter("hostats", "*", counterValue);
			}
			boolean result2 = neighbor.resetCounter("hostats", "*", counterValue);
			if (!result2) {
				neighbor.resetCounter("hostats", "*", counterValue);
			}
			GeneralUtils.printToConsole(enodeB.getName() + " set value: " + String.valueOf(result1));
			GeneralUtils.printToConsole(neighbor.getName() + " set value: " + String.valueOf(result2));
		}
	}

	private static void moveAtt(AttenuatorSet attenuatorSetUnderTest, int from, int to) {
		int multi = 1;
		int attenuatorsCurrentValue = from;
		if (from > to)
			multi = -1;
		int steps = Math.abs(from - to) / ATT_STEP;
		for (; steps >= 0; steps--) {
			long beforeAtt = System.currentTimeMillis();
			peripheralsConfig.setAttenuatorSetValue(attenuatorSetUnderTest, attenuatorsCurrentValue);
			attenuatorsCurrentValue += (ATT_STEP * multi);
			long afterAtt = System.currentTimeMillis();
			long diff = afterAtt - beforeAtt;
			if (diff < ATT_STEP_TIME)
				GeneralUtils.unSafeSleep(ATT_STEP_TIME - diff);
		}
	}

	private void LongHOResults() {

		GeneralUtils.startLevel("Results");
		report.report("Attenuator step: " + ATT_STEP);
		report.report("Attenuator timing: " + ATT_STEP_TIME + "ms");
		report.report("Attenuation boundaries: " + attenuatorMin + "-" + attenuatorMax);
		report.report("Number of dynamic UE's: " + numberOfDynamicUES);
		report.report("Number of expected HO's: " + finalCounter * numberOfDynamicUES);

		GeneralUtils.startLevel("number of expected HO's per minute per UE: " + HO_Per_Min_Per_UE);
		report.report("total Attenuator moves / test duration in minutes / number of dynamic UES");
		GeneralUtils.stopLevel(); // number of expected HO's per minute stop
									// level

		GeneralUtils.startLevel("Number of attempts: " + total_attCounter);
		report.report("SNMP MIB: " + counterAttemptsnmp + " On Both EnodeBs");
		report.report(enodeB.getNetspanName() + ": " + first_ENB_att);
		report.report(neighbor.getNetspanName() + ": " + second_ENB_att);
		GeneralUtils.stopLevel();// Number of attempts: stop level

		GeneralUtils.startLevel("Number of HO's: " + total_succCounter);
		report.report("SNMP MIB: " + counterSuccesssnmp + " On Both EnodeBs");
		report.report(enodeB.getNetspanName() + ": " + first_ENB_succ);
		report.report(neighbor.getNetspanName() + ": " + second_ENB_succ);
		GeneralUtils.stopLevel(); // Number of HO's: stop level

		if (total_attCounter == 0) {
			report.report("There were no attempts - test failed", Reporter.WARNING);
		} else {
			if (theoreticalAttempts < 60) {
				GeneralUtils.startLevel("Number of attempts out of real attenuator moves is: "
						+ new DecimalFormat("##.##").format(theoreticalAttempts) + "%");
				report.report("Number of attempts is less then 60%", Reporter.FAIL);
				reason = "Number of attempts is less then 60%";
				report.report("Number of attempts / attenuator moves");
				GeneralUtils.stopLevel();
			} else {
				if (theoreticalAttempts < 85) {
					GeneralUtils.startLevel("Number of attempts out of real attenuator moves is: "
							+ new DecimalFormat("##.##").format(theoreticalAttempts) + "%");
					report.report("Number of attempts is less then 85%", Reporter.WARNING);
					report.report("Number of attempts / attenuator moves");
					GeneralUtils.stopLevel();
				} else {
					GeneralUtils.startLevel("Number of attempts out of real attenuator moves is: "
							+ new DecimalFormat("##.##").format(theoreticalAttempts) + "%");
					report.report("Number of attempts / attenuator moves");
					GeneralUtils.stopLevel();
				}

				GeneralUtils.startLevel("Pass Criteria: Success rate (out of attempts): "
						+ GeneralUtils.fractureToPercent(total_HO_Success_rate_out_of_attempts) + "%");
				report.report("SNMP MIB: " + counterSuccesssnmp + " On Both EnodeBs / SNMP MIB: " + counterAttemptsnmp
						+ " On Both EnodeBs");
				if (total_HO_Success_rate_out_of_attempts < WARNING_LEVEL) {
					report.report("Success rate lower then Threshold (" + GeneralUtils.fractureToPercent(PASS_LEVEL) + ")",
							Reporter.FAIL);
					reason = "Success rate lower then Threshold (" + GeneralUtils.fractureToPercent(PASS_LEVEL) + ")";
				}
				if (total_HO_Success_rate_out_of_attempts > WARNING_LEVEL
						&& total_HO_Success_rate_out_of_attempts < PASS_LEVEL) {
					report.report(
							"Success rate lower then Threshold (" + GeneralUtils.fractureToPercent(PASS_LEVEL) + ")",
							Reporter.WARNING);
				}

				report.report(enodeB.getNetspanName() + ": Success rate (out of attempts): "
						+ GeneralUtils.fractureToPercent(firstENB_HO_Success_rate_out_of_attempts));
				report.report(neighbor.getNetspanName() + ": Success rate (out of attempts): "
						+ GeneralUtils.fractureToPercent(secondENB_HO_Success_rate_out_of_attempts));
				GeneralUtils.stopLevel(); // Pass Criteria: Success rate (out of
			}
		}
		GeneralUtils.stopLevel(); // Results stop level
	}

	private static void updateResultsVariables(int counter) {
		numberOfDynamicUES = dynUEList.size();
		HO_Per_Min_Per_UE = counter * 1.0 / GeneralUtils.milisToMinutes(maxduration) * 1.0;

		first_ENB_succ += enodeB.getCountersValue(counterSuccesssnmp);
		first_ENB_att += enodeB.getCountersValue(counterAttemptsnmp);
		firstENB_HO_Success_rate_out_of_attempts = (first_ENB_succ * 1.0) / (first_ENB_att * 1.0);

		second_ENB_succ += neighbor.getCountersValue(counterSuccesssnmp);
		second_ENB_att += neighbor.getCountersValue(counterAttemptsnmp);
		secondENB_HO_Success_rate_out_of_attempts = (second_ENB_succ * 1.0) / (second_ENB_att * 1.0);

		total_succCounter = first_ENB_succ + second_ENB_succ;
		total_attCounter = first_ENB_att + second_ENB_att;
		total_HO_Success_rate_out_of_attempts = (total_succCounter * 1.0) / (total_attCounter * 1.0);

		theoreticalAttempts = (total_attCounter * 1.0) / (counter * numberOfDynamicUES) * 100;
	}

	
	static class HandOverClass implements Runnable{
		public void start() {
			maxduration = timeToHO*60*1000;
			GeneralUtils.startLevel("Moving attenuator for " + maxduration / 60000 + " minutes to create Hand-Over");
			report.report("Attenuator IP: " + attenuatorSetUnderTest.getName());
			GeneralUtils.stopLevel();
			setStartedHO(true);
			Thread runnableThread = new Thread(this);
			runnableThread.start();
		}
		
		@Override
		public void run() {
			long startTime = System.currentTimeMillis();
			long endTime = System.currentTimeMillis() + timeToHO*60*1000;
			long resetCounters = startTime;
			int counter = 0;
			int waitingPeriodTime = enodeB.getGranularityPeriod();
			while (System.currentTimeMillis() < endTime) {
				moveAtt(attenuatorSetUnderTest, attenuatorMin, attenuatorMax);
				counter++;
				if (System.currentTimeMillis() > endTime) {
					break;
				}
				moveAtt(attenuatorSetUnderTest, attenuatorMax, attenuatorMin);
				counter++;
				if (System.currentTimeMillis() - resetCounters >= 8 * 60 * 1000 * waitingPeriodTime) {
					resetCounters = System.currentTimeMillis();
					double temp = Math.floor(((System.currentTimeMillis() - startTime) / 1000 / 60.0) * 100);
					double tempDouble = temp / 100.0;
					report.report("Time elapsed: " + tempDouble + " minutes.");
					updateResultsVariables(counter);
					resetCounters();
				}
			}
			 // moving attenuator stop level
			report.report("Finished moving attenuator");
			attenuatorSetUnderTest.close();
			attenuatorSetUnderTest = null;
			report.report("Wait for " + waitingPeriodTime + " minutes for Counter to update");
			GeneralUtils.unSafeSleep(waitingPeriodTime * 60 * 1000);
			updateResultsVariables(counter);
			setFinishedHO(true);
			finalCounter = counter;
		}
	}
	
	private boolean gettingPassCratiriaValue() {
		boolean tempFlag = true;
		GeneralUtils.startLevel("Getting Pass Criteria counters value");
		for (String counter : passCriteria.values()) {
			int mainCounterValue = enodeB.getCountersValue(counter);
			if (mainCounterValue != 0) {
				report.report("Counter " + counter + " values are " + mainCounterValue + " instead of 0 in enodeB: "
						+ enodeB.getName(), Reporter.WARNING);
				tempFlag = false;
			} else
				report.report("Counter " + counter + " values are 0 in enodeB: " + enodeB.getName());
			int neighborCounterValue = neighbor.getCountersValue(counter);
			if (neighborCounterValue != 0) {
				report.report("Counter " + counter + " values are " + neighborCounterValue + " instead of 0 in enodeB: "
						+ neighbor.getName(), Reporter.WARNING);
				tempFlag = false;
			} else
				report.report("Counter " + counter + " values are 0 in enodeB: " + neighbor.getName());
		}
		GeneralUtils.stopLevel(); // Getting Pass Criteria counters value stop
									// level
		return tempFlag;
	}
	
	public static synchronized boolean isStartedHO() {
		return startedHO;
	}

	public static synchronized void setStartedHO(boolean startedHO) {
		MultiEnodebAction.startedHO = startedHO;
	}

	public static synchronized boolean isFinishedHO() {
		return finishedHO;
	}

	public static synchronized void setFinishedHO(boolean finishedHO) {
		MultiEnodebAction.finishedHO = finishedHO;
	}

	public HandOverType getHoType() {
		return hoType;
	}

	public void setHoType(HandOverType hoType) {
		this.hoType = hoType;
	}

	public FrequencyType getFreqType() {
		return freqType;
	}

	public void setFreqType(FrequencyType freqType) {
		this.freqType = freqType;
	}

	public enum FrequencyType {
        Intra, Inter;
    }
	
	public enum HandOverType {
        S1, X2;
    }
	
	public Integer getTimeToHO() {
		return timeToHO;
	}

	@ParameterProperties(description = "Time of performing HO in minutes")
	public void setTimeToHO(String timeToHO) {
		this.timeToHO = Integer.valueOf(timeToHO);
	}
	
	@ParameterProperties(description = "First DUT")
	public void setDUT1(String dut) {
		GeneralUtils.printToConsole("Load DUT1" + dut);
		MultiEnodebAction.enodeB = (EnodeB) SysObjUtils.getInstnce().initSystemObject(EnodeB.class, false, dut).get(0);
		GeneralUtils.printToConsole("DUT loaded" + this.enodeB.getNetspanName());
	}

	@ParameterProperties(description = "Second DUT")
	public void setDUT2(String dut) {
		GeneralUtils.printToConsole("Load DUT2" + dut);
		MultiEnodebAction.neighbor = (EnodeB) SysObjUtils.getInstnce().initSystemObject(EnodeB.class, false, dut).get(0);
		GeneralUtils.printToConsole("DUT loaded" + this.neighbor.getNetspanName());
	}

	public EnodeB getDut1() {
		return enodeB;
	}

	public EnodeB getDut2() {
		return neighbor;
	}

	@Override
	public void end() {
		super.end();
	}
}

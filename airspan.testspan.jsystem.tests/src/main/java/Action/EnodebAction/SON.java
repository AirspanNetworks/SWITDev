package Action.EnodebAction;

import java.util.ArrayList;

import org.junit.Test;

import Attenuators.AttenuatorSet;
import EnodeB.EnodeB;
import Utils.GeneralUtils;
import Utils.SysObjUtils;
import jsystem.framework.ParameterProperties;
import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;
import testsNG.Actions.Neighbors;
import testsNG.Actions.PeripheralsConfig;

public class SON extends EnodebAction {	
	private EnodeB dut1;
	private EnodeB dut2;
	private Neighbors neighbors;
	private AttenuatorSet attenuatorSetUnderTest;
	private String attenuatorSetName = "rudat_set";
	private Integer attenuatrStartValue;
	private Integer attenuatrStopValue;
	private PeripheralsConfig peripheralsConfig;
	private boolean checkMutualNeighbor = true;
	
	@Override
	public void init() {
		neighbors = Neighbors.getInstance();
		attenuatorSetUnderTest = AttenuatorSet.getAttenuatorSet(attenuatorSetName);
		peripheralsConfig = PeripheralsConfig.getInstance();
		enbInTest = new ArrayList<>();
		enbInTest.add(dut1);
		enbInTest.add(dut2);
		super.init();
	}
	
	@Test
	@TestProperties(name = "Discover Neighbor By Moving Attenuator", returnParam = {
			"IsTestWasSuccessful" }, paramsInclude = { "Dut1", "Dut2" , "attenuatorSetName" , "attenuatrStartValue", "attenuatrStopValue", "checkMutualNeighbor" })
	public void discoverNeighborByMovingAttenuator() {		
		GeneralUtils.startLevel("Verify EnodeBs in service");
		if(dut1.isInService())
			report.report(dut1.getName() + " is in service.");
		else
		{
			report.report(dut1.getName() + " is not in service.", Reporter.FAIL);
			GeneralUtils.stopLevel();
			return;
		}
		
		if(dut2.isInService())
			report.report(dut2.getName() + " is in service.");
		else
		{
			report.report(dut2.getName() + " is not in service.", Reporter.FAIL);
			GeneralUtils.stopLevel();
			return;
		}
		GeneralUtils.stopLevel();
		Integer startAttValue = attenuatrStartValue;
		if (startAttValue == null) {
			startAttValue = attenuatorSetUnderTest.getMaxAttenuation();
		}
		Integer stopAttValue = attenuatrStopValue;
		if (stopAttValue == null) {
			stopAttValue = attenuatorSetUnderTest.getMinAttenuation();
		}
		
		addANRNeighbor(startAttValue, stopAttValue);
	}
	
	private boolean addANRNeighbor(int startValue, int stopValue) {
		int direction = stopValue > startValue ? 1 : -1;
		report.report("Start: " + startValue + ", stop: " + stopValue + ", direction: " + direction);
		attenuatorSetUnderTest.setAttenuation(startValue);
		GeneralUtils.unSafeSleep(5000);
		float attenuatorsCurrentValue = attenuatorSetUnderTest.getAttenuation()[0];
		if (startValue != attenuatorsCurrentValue) {
			report.report("att value not match start value.");
			attenuatorSetUnderTest.setAttenuation(startValue);
			GeneralUtils.unSafeSleep(5000);
			attenuatorsCurrentValue = attenuatorSetUnderTest.getAttenuation()[0];
		}
		int attenuatorStepTime = attenuatorSetUnderTest.getAttenuationStep();
		boolean result = true;
		report.reportHtml(dut1.getName() + " - ue show link", dut1.lteCli("ue show link"), true);
		report.reportHtml(dut2.getName() + " - ue show link", dut2.lteCli("ue show link"), true);
		GeneralUtils.startLevel("Adding ANR Neighbor.");

		// while loop - decrease attenuation, check ngh table until ngh was
		// found. pass criteria
		boolean attenuatorMove = false;
		GeneralUtils.startLevel((String.format("Moving attenuators value until ANR ngh addition on %s detection.",
				dut1.getName())));
		report.reportHtml("db get nghList", dut1.lteCli("db get nghList"), true);
		report.reportHtml("cell show anrstate", dut1.lteCli("cell show anrstate"), true);
		while (!neighbors.verifyAnrNeighbor(dut1, dut2)
				&& !stopValueReached(attenuatorsCurrentValue, direction, stopValue)) {
			attenuatorsCurrentValue += direction;
			peripheralsConfig.setAttenuatorSetValue(attenuatorSetUnderTest, attenuatorsCurrentValue);
			attenuatorMove = true;
			report.report(String.format("Attenuators value changed to %s, waiting %s seconds..",
					attenuatorsCurrentValue, attenuatorStepTime / 1000.0));
			
			GeneralUtils.unSafeSleep(attenuatorStepTime);
			report.reportHtml("db get nghList", dut1.lteCli("db get nghList"), true);
			report.reportHtml("cell show anrstate", dut1.lteCli("cell show anrstate"), true);
		}
		if(!attenuatorMove){
			report.report("Won't do this stage because nbr already exists");
		}
		GeneralUtils.stopLevel();
		
		// print test results.
		GeneralUtils.startLevel("Add Neighbor Results");
		GeneralUtils.unSafeSleep(10000);
		report.reportHtml(dut1.getName() + " - db get nghList", dut1.lteCli("db get nghlist"), true);
		if (!neighbors.verifyAnrNeighbor(dut1, dut2)) {
			report.report("[ERROR]: Can not find the neighbor on ngh list, test failed.", Reporter.FAIL);
			result = false;
		} else {
			report.report("Neighbour found with attenuator at " + attenuatorSetUnderTest.getAttenuation()[0]);
			report.report(String.format("[PASS]: %s was added to %s as ANR Neighbor successfully.", dut2.getName(),
					dut1.getName()));
		}
		
		if (checkMutualNeighbor) {
			report.reportHtml(dut2.getName() + " - db get nghList", dut2.lteCli("db get nghlist"), true);
			if (!neighbors.verifyAnrNeighbor(dut2, dut1)) {
				report.report("[ERROR]: Can not find the neighbor on ngh list, test failed.", Reporter.FAIL);
				result = false;
			} else {
				report.report(String.format("[PASS]: %s was added to %s as ANR Neighbor successfully.", dut1.getName(),
						dut2.getName()));
			}
		}
		GeneralUtils.stopLevel();
		GeneralUtils.stopLevel();
		return result;
	}


	private boolean stopValueReached(float attenuatorsCurrentValue, int direction, int stopValue) {
		if (direction > 0) 
			return attenuatorsCurrentValue >= stopValue;
		else		
			return attenuatorsCurrentValue <= stopValue;
	}
	
	@ParameterProperties(description = "Attenuator to use (leave empty for SUT default)")
	public void setAttenuatorSetName(String attenuatorSetName) {
		this.attenuatorSetName =attenuatorSetName;
	}
	
	@ParameterProperties(description = "Attenuator start value (default max attenuator value)")
	public void setAttenuatrStartValue(String attenuatrStartValue) {
		try{
			this.attenuatrStartValue = Integer.parseInt(attenuatrStartValue);
		}
		catch (NumberFormatException e) {
			report.report("Illigal value entered for attenuatrStartValue, using default value.");
		}
	}
	
	@ParameterProperties(description = "Attenuator stop value (default min attenuator value)")
	public void setAttenuatrStopValue(String attenuatrStopValue) {
		try{
			this.attenuatrStopValue = Integer.parseInt(attenuatrStopValue);
		}
		catch (NumberFormatException e) {
			report.report("Illigal value entered for attenuatrStopValue, using default value.");
		}		
	}
	
	@ParameterProperties(description = "Main EnodeB")
	public void setDut1(String dut1) {
		ArrayList<EnodeB> temp = (ArrayList<EnodeB>) SysObjUtils.getInstnce().initSystemObject(EnodeB.class, false,
				dut1.split(","));
		this.dut1 = temp.get(0);
	}
	
	@ParameterProperties(description = "Neighbour EnodeB")
	public void setDut2(String dut2) {
		ArrayList<EnodeB> temp = (ArrayList<EnodeB>) SysObjUtils.getInstnce().initSystemObject(EnodeB.class, false,
				dut2.split(","));
		this.dut2 = temp.get(0);
	}
	
	@ParameterProperties(description = "Check mutuall neighbor addition")
	public void setCheckMutualNeighbor(boolean checkMutualNeighbor) {
		this.checkMutualNeighbor = checkMutualNeighbor;
	}
}

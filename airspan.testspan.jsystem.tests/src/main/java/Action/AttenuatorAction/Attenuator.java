package Action.AttenuatorAction;

import org.junit.Test;

import Action.Action;
import Attenuators.AttenuatorSet;
import Utils.GeneralUtils;
import jsystem.framework.ParameterProperties;
import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;
import testsNG.Actions.PeripheralsConfig;

public class Attenuator extends Action {
	public enum TimeUnits {
	    HOUR(1000*60*60),
	    MINUTE(1000*60),
	    SECOND(1000);

	    private int multiplier;

		TimeUnits(int multiplier) {
	        this.multiplier = multiplier;
	    }

	    public int multiplier() {
	        return multiplier;
	    }
	}
	protected TimeUnits totalTimeUnit;
	
	protected String attenuatorMinValue;
	
	protected String attenuatorMaxValue;
	
	protected String attenuatorStep;
	
	protected String attenuatorStepTime;
	
	protected String totalTime;
	
	protected String attenuatorSetName = "rudat_set";
	
	protected String attenuatorValue;
	
	@ParameterProperties(description = "Set attenuatorMinValue")
	public void setAttenuatorMinValue(String attenuatorMinValue) {
		this.attenuatorMinValue = attenuatorMinValue;
	}
	
	@ParameterProperties(description = "Set attenuatorMaxValue")
	public void setAttenuatorMaxValue(String attenuatorMaxValue) {
		this.attenuatorMaxValue = attenuatorMaxValue;
	}
	
	@ParameterProperties(description = "Set attenuatorStep")
	public void setAttenuatorStep(String attenuatorStep) {
		this.attenuatorStep = attenuatorStep;
	}
	
	@ParameterProperties(description = "Set attenuatorstepTime(miliseconds)")
	public void setAttenuatorStepTime(String attenuatorstepTime) {
		this.attenuatorStepTime = attenuatorstepTime;
	}
	
	@ParameterProperties(description = "Set totalTimeUnit")
	public void setTotalTimeUnit(TimeUnits totalTimeUnit) {
		this.totalTimeUnit = totalTimeUnit;
	}
	
	@ParameterProperties(description = "Set totalTime")
	public void setTotalTime(String totalTime) {
		this.totalTime = totalTime;
	}	
	
	@ParameterProperties(description = "Set attenuatorValue")
	public void setAttenuatorValue(String attenuatorValue) {
		this.attenuatorValue = attenuatorValue;
	}
	
	@ParameterProperties(description = "Set attenuatorSetName default=\"rudat_set\"")
	public void setAttenuatorSetName(String attenuatorSetName) {
		if(attenuatorSetName != null && !attenuatorSetName.equals(""))
			this.attenuatorSetName = attenuatorSetName;
	}
	
	@Test
	@TestProperties(name = "Set attenuator", returnParam = "LastStatus", paramsInclude = { "attenuatorSetName", "attenuatorValue" })
	public void setAttenuator() {
		float attValue = GeneralUtils.ERROR_VALUE;
		try {
			attValue = Float.parseFloat(attenuatorValue);
		} catch (Exception e) {
			report.report("Failed to set value to attenuator!. Illegal value requested - " + attenuatorValue, Reporter.FAIL);
			reason = "Failed to set value to attenuator!. Illegal value requested - " + attenuatorValue;
			return;
		}
		AttenuatorSet attenuatorSetUnderTest = AttenuatorSet.getAttenuatorSet(attenuatorSetName);
		
		report.report("Set attenuator " + attenuatorSetName + " to value " + attValue);
		
		boolean success = PeripheralsConfig.getInstance().setAttenuatorSetValue(attenuatorSetUnderTest, attValue);
		float attenuation = GeneralUtils.ERROR_VALUE;
		float[] attenuations = AttenuatorSet.getAttenuatorSet(attenuatorSetName).getAttenuation();
		if(attenuations.length > 0){
			attenuation = attenuations[0];
			report.report("Attenuator value after set: " + attenuation);
		}
		if (!success || attenuation != attValue) {
			report.report("Set attenuator Failed", Reporter.FAIL);
			reason = "Set attenuator Failed";
		} else {
			report.report("Set attenuator Succeeded");
		}
	}
	
	@Test
	@TestProperties(name = "Move attenuator", returnParam = "LastStatus", paramsInclude = { "attenuatorSetName", "attenuatorMinValue", "attenuatorMaxValue", "attenuatorStep", "attenuatorStepTime", "totalTimeUnit", "totalTime" })
	public void moveAttenuator() {		
		float attMin = 0, attMax = 0, attStep = 0, stepTime = 0, totalTimeMilisec = 0;
		AttenuatorSet attenuatorSetUnderTest = AttenuatorSet.getAttenuatorSet(attenuatorSetName);
		
		try{
			if (attenuatorMinValue != null)				
				attMin = Float.parseFloat(attenuatorMinValue);
			else{
				report.report("attenuatorMinValue not provided, taking value from SUT.");
				attMin = attenuatorSetUnderTest.getMinAttenuation();
			}
			
			if (attenuatorMaxValue != null) 
				attMax = Float.parseFloat(attenuatorMaxValue);
			else{
				report.report("attenuatorMaxValue not provided, taking value from SUT.");
				attMax = attenuatorSetUnderTest.getMaxAttenuation();
			}
			
			if (attenuatorStep != null) 
				attStep = Float.parseFloat(attenuatorStep);
			else{
				report.report("attenuatorStep not provided, taking value from SUT.");
				attStep = attenuatorSetUnderTest.getAttenuationStep();
			}
			
			if (attenuatorStepTime != null) 
				stepTime = Float.parseFloat(attenuatorStepTime);
			else{
				report.report("attenuatorStepTime not provided, taking value from SUT.");
				stepTime = attenuatorSetUnderTest.getStepTime();
			}
			totalTimeMilisec = Float.parseFloat(totalTime) * totalTimeUnit.multiplier;
			GeneralUtils.printToConsole("totalTimeMilisec = " + totalTimeMilisec);
		}catch (Exception e) {
			report.report("Could not accept all of the parameters. " + e.getMessage(), Reporter.FAIL);
			reason = "Could not accept all of the parameters. " + e.getMessage();
			e.printStackTrace();
			report.report("attenuatorSetName: " + attenuatorSetName + ", AttenuatorMinValue: " + attenuatorMinValue + ", attenuatorMaxValue: " + attenuatorMaxValue + ", attenuatorStep: " + attenuatorStep + ", attenuatorStepTime: " + attenuatorStepTime + ", attenuatorStepTime: " + totalTime);
			return;
		}
		
		report.report("Moving attenuator " + attenuatorSetName + " from value: " + attMin + " to value " + attMax + " with steps of " + attStep + " every " + stepTime + " miliseconds for a total time of " + totalTime + " " + totalTimeUnit);
		long endTime =  System.currentTimeMillis() + (long)totalTimeMilisec;
		while (System.currentTimeMillis() < endTime) {				
			moveAtt(attenuatorSetUnderTest, attMin, attMax, attStep, stepTime);
			if(System.currentTimeMillis() > endTime){
				break;
			}
			moveAtt(attenuatorSetUnderTest, attMax, attMin, attStep, stepTime);
		}
	}
	
	private boolean moveAtt(AttenuatorSet attenuatorSetUnderTest, float from, float to, float attenuatorStep, float attenuatorDelay) {
		int multi = 1;
		float attenuatorsCurrentValue = from;
		if (from > to)
			multi = -1;
		int steps = (int) (Math.abs(from - to) / attenuatorStep);
		boolean success = false;
		for (; steps >= 0; steps--) {
			long beforeAtt = System.currentTimeMillis();
			success = PeripheralsConfig.getInstance().setAttenuatorSetValue(attenuatorSetUnderTest, attenuatorsCurrentValue);
			attenuatorsCurrentValue += (attenuatorStep * multi);
			long afterAtt = System.currentTimeMillis();
			long diff = afterAtt - beforeAtt;
			if (diff < attenuatorDelay)
				GeneralUtils.unSafeSleep((long) (attenuatorDelay - diff));
		}
		return success;
	}
}

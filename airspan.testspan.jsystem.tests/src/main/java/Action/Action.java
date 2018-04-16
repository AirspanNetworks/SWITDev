package Action;

import org.junit.After;
import org.junit.Before;

import jsystem.framework.report.Reporter;
import junit.framework.SystemTestCase4;

public class Action extends SystemTestCase4 {
	protected int actionID = 0;
	protected String LastStatus = "";

	protected enum Comparison {
		EQUAL_TO, NOT_EQUAL_TO, BIGGER_THAN, SMALLER_THAN;
	}
	
	@Before
	public void init() {
	}

	@After
	public void end() {
		setLastStatusRetParam();
	}

	private void setLastStatusRetParam() {
		if (this.isPass) {
			setLastStatus("PASS");
		} else {
			setLastStatus("FAIL");
		}
	}

	public synchronized final int getActionID() {
		return actionID;
	}

	public synchronized final void setActionID(int actionID) {
		this.actionID = actionID;
	}

	public synchronized final String getLastStatus() {
		return LastStatus;
	}

	public synchronized final void setLastStatus(String lastStatus) {
		LastStatus = lastStatus;
	}

	public Integer parseInt(String value) {
		Integer res;
		try {
			res = Integer.parseInt(value);
		} catch (Exception e) {
			report.report("Cant convert value:" + value + " to Int", Reporter.FAIL);
			res = null;
		}
		return res;
	}

	public void comparison(Comparison comparison, String currentValue, String expectedValue) {
		boolean flag = true;
		Long num1;
		Long num2;

		switch (comparison) {
		case EQUAL_TO:
			if (!expectedValue.equals(currentValue))
				flag = false;
			break;
		case NOT_EQUAL_TO:
			if (expectedValue.equals(currentValue))
				flag = false;
			break;
		case BIGGER_THAN:
			num1 = Long.valueOf(currentValue);
			num2 = Long.valueOf(expectedValue);
			if (num1 <= num2)
				flag = false;
			break;
		case SMALLER_THAN:
			num1 = Long.valueOf(currentValue);
			num2 = Long.valueOf(expectedValue);
			if (num1 >= num2)
				flag = false;
			break;
		}

		if (flag == false) {
			report.report("Current Value: " + currentValue + ", Expected value: " + comparison + " " + expectedValue,
					Reporter.FAIL);
		} else {
			report.report("Current Value: " + currentValue + " is " + comparison + " " + expectedValue);
		}
	}
	
	public boolean reportCompareBetweenTwoStrings(String parameterName, String currentValue, String expectedValue) {
		if(currentValue != null && currentValue.equals(expectedValue)) {
			report.report(parameterName + ": Current Value=" + currentValue + ", Expected value="+expectedValue);
			return true;
		}
		else {
			report.report(parameterName + ": Current Value=" + currentValue + ", Expected value="+expectedValue, Reporter.FAIL);
			return false;
		}
	}
}
package Ercom.UeSimulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.joda.time.DateTime;

public class UeSimulatorStatisticHolder {

	// ----------------------------- variables --------------------------
	
	private List<TestProperty> startedTests ;

	private HashMap<DateTime, Integer> executionStateForThePlatform ;

	private HashMap<DateTime, Integer> numberOfConnectedUEs ;
	
	// ----------------------------- Methods ----------------------------
	
	public UeSimulatorStatisticHolder(){
		this.startedTests = new ArrayList<TestProperty>();
		this.executionStateForThePlatform = new HashMap<DateTime, Integer>();
		this.numberOfConnectedUEs = new HashMap<DateTime, Integer>();
	}
	// ----------------------------- getters/Setters --------------------

	public List<TestProperty> getStartedTests() {
		return startedTests;
	}

	
	public void setStartedTests(String Name, String UniqId, String ResultPath, boolean ValidTest ) {
		TestProperty newTest = new TestProperty();
		newTest.setName(Name);
		newTest.setUniqId(UniqId);
		newTest.setResultPath(ResultPath);
		newTest.setValidTest(ValidTest);
		startedTests.add(new TestProperty());
	}
	
	public void setStartedTests(List<TestProperty> startedTests) {
		this.startedTests = startedTests;
	}

	public HashMap<DateTime, Integer> getExecutionStateForThePlatform() {
		return executionStateForThePlatform;
	}

	public void setExecutionStateForThePlatform(
			HashMap<DateTime, Integer> executionStateForThePlatform) {
		this.executionStateForThePlatform = executionStateForThePlatform;
	}
	

	public HashMap<DateTime, Integer> getNumberOfConnectedUEs() {
		return numberOfConnectedUEs;
	}

	public void setNumberOfConnectedUEs(
			HashMap<DateTime, Integer> numberOfConnectedUEs) {
		this.numberOfConnectedUEs = numberOfConnectedUEs;
	}

	// ----------------------------- Inner class -----------------------

	static class TestProperty {
		private String name;

		private String uniqId;

		private String resultPath;
		
		private boolean validTest;

		public TestProperty(){
			
		}
		
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getUniqId() {
			return uniqId;
		}

		public void setUniqId(String uniqId) {
			this.uniqId = uniqId;
		}

		public String getResultPath() {
			return resultPath;
		}

		public void setResultPath(String resultPath) {
			this.resultPath = resultPath;
		}

		public boolean isValidTest() {
			return validTest;
		}

		public void setValidTest(boolean validTest) {
			this.validTest = validTest;
		}
	}


}

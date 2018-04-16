package Ercom.UeSimulator;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import Utils.Pair;

public class TestProperty {

	// ----------------------------- variables --------------------------
	
	private String name;

	private String uniqId;

	private String resultPath;
	
	private boolean validTest;

	private List<Pair<DateTime, Integer>> numberOfConnectedUEs = new ArrayList<Pair<DateTime, Integer>>();
	
	// ----------------------------- Methods ----------------------------
	
	public TestProperty(){
		
	}
	
	
	public TestProperty(String Name, String UniqId, String ResultPath, boolean ValidTest ) {

		this.setName(Name);
		this.setUniqId(UniqId);
		this.setResultPath(ResultPath);
		this.setValidTest(ValidTest);
		
	}
	
	// ----------------------------- getters/Setters --------------------
	
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

		
		public List<Pair<DateTime, Integer>> getNumberOfConnectedUEs() {
			return numberOfConnectedUEs;
		}


		public void setNumberOfConnectedUEs(List<Pair<DateTime, Integer>> numberOfConnectedUEs) {
			this.numberOfConnectedUEs = numberOfConnectedUEs;
		}


		public void recordNumberOfCurrentlyConnectedUEs(DateTime timeStamp,Integer amount){
			this.numberOfConnectedUEs.add(new Pair<DateTime, Integer>(timeStamp,amount));
		}
		
}

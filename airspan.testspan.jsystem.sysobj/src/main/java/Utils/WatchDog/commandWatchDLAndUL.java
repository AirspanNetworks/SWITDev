package Utils.WatchDog;

import java.util.HashMap;

import EnodeB.EnodeB;
import Utils.GeneralUtils;

public class commandWatchDLAndUL extends Command{
	private EnodeB enb;
	private HashMap<String, Integer> sumOfDLSamples = new HashMap<>();
	private HashMap<String, Integer> sumOfULSamples = new HashMap<>();
	private HashMap<String, Integer> dlCounters = new HashMap<>();
	private HashMap<String, Integer> ulCounters = new HashMap<>();
	int generalCounter = 0;
	
	public commandWatchDLAndUL(EnodeB node) {
		this.enb = node;
	}
	
	public EnodeB getEnb() {
		return enb;
	}

	public HashMap<String, Integer> getDlCounterSum() {
		return sumOfDLSamples;
	}

	public HashMap<String, Integer> getUlCounterSum() {
		return sumOfULSamples;
	}

	public HashMap<String, Integer> getDlCounters() {
		return dlCounters;
	}

	public HashMap<String, Integer> getUlCounters() {
		return ulCounters;
	}

	public int getGeneralCounter() {
		return generalCounter;
	}

	@Override
	public void run() {
		HashMap<String, Integer> dlSamplevalues = enb.getDLPer();
		HashMap<String, Integer> ulSampleValues = enb.getULCrcPer();
		generalCounter++;
		getDLandULvalues(dlSamplevalues,sumOfDLSamples,dlCounters);
		getDLandULvalues(ulSampleValues,sumOfULSamples,ulCounters);
	}
	


	@Override
	public int getExecutionDelaySec() {
		return 10;
	}
	
	public void getDLandULvalues(HashMap<String, Integer> sampleValues, HashMap<String, Integer> SumValues, HashMap<String, Integer> countersValues){
		
	for(String sample : sampleValues.keySet()){
		String UEID = sample.substring(sample.lastIndexOf(".")+1);
		if (SumValues.containsKey(UEID)){
			SumValues.put(UEID,SumValues.get(UEID) + sampleValues.get(sample));
			countersValues.put(UEID, countersValues.get(UEID)+1);
			GeneralUtils.printToConsole("UE : " + UEID + ", value: " + sampleValues.get(sample) + ", counter: " + countersValues.get(UEID));
		}
		else {
			SumValues.put(UEID,sampleValues.get(sample));
			countersValues.put(UEID, 1);
			GeneralUtils.printToConsole("UE : " + UEID + ", value: " + sampleValues.get(sample) + ", counter: " + countersValues.get(UEID));
			}
		}
	}
	
}

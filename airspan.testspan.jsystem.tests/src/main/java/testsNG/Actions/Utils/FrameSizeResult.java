package testsNG.Actions.Utils;

import java.util.HashMap;
import java.util.Map;

/**
 * this class Gathers one Result from a STC contains timeStamp, frameSize and a
 * map for counterStates
 * 
 * @author Shuhamy Shahaf
 *
 */
public class FrameSizeResult {

	public HashMap<String, CounterStatisticsData> counterStats;
	public Integer frameSize;
	private long timeStamp;

	public FrameSizeResult(Integer frameSize) {
		counterStats = new HashMap<String, CounterStatisticsData>();
		this.frameSize = frameSize;
	}

	public void addSample(long timestamp, String description, Map<String, Long> counters) {
		for (String counterName : counters.keySet()) {
			addSample(timestamp, description, counterName, counters.get(counterName));
			this.setTimeStamp(timestamp);
		}

	}

	public String getStreamName() {
		for (String name : counterStats.keySet()) {
			if (name != null) {
				return counterStats.get(name).getDescription();
			}
		}
		return null;
	}

	public void addSample(long timestamp, String description, String counterName, long counterValue) {
		CounterStatisticsData counterStatisticsData = counterStats.get(counterName);
		if (counterStatisticsData == null) {
			counterStatisticsData = new CounterStatisticsData(counterName);
			counterStats.put(counterName, counterStatisticsData);
		}
		counterStatisticsData.addCounterValue(description, counterValue);
	}

	public long getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}

	public class CounterStatisticsData {
		public long currentRate;
		private String counterName;
		private String description;
		private Map<String, Long> numberOfSamples;
		private Map<String, Long> minValue;
		private Map<String, Long> maxValue;
		private Map<String, Long> sumOfValues;

		public CounterStatisticsData(String counterName) {
			this.setCounterName(counterName);
			numberOfSamples = new HashMap<String, Long>();
			minValue = new HashMap<String, Long>();
			maxValue = new HashMap<String, Long>();
			sumOfValues = new HashMap<String, Long>();
		}

		public long getCurrentRate() {
			return currentRate;
		}

		public String getDescription() {
			return description;
		}

		public void addCounterValue(String description, long value) {
			// Initialize description if needed
			if (!numberOfSamples.containsKey(description)) {
				numberOfSamples.put(description, 0l);
				sumOfValues.put(description, 0l);
				minValue.put(description, value);
				maxValue.put(description, value);
				currentRate = value;
				this.description = description;
			}

			Long numberOfSamples = this.numberOfSamples.get(description);
			Long sumOfValues = this.sumOfValues.get(description);
			Long minValue = this.minValue.get(description);
			Long maxValue = this.maxValue.get(description);

			numberOfSamples++;
			sumOfValues += value;
			minValue = Math.min(minValue, value);
			maxValue = Math.max(maxValue, value);

			this.numberOfSamples.put(description, numberOfSamples);
			this.sumOfValues.put(description, sumOfValues);
			this.minValue.put(description, minValue);
			this.maxValue.put(description, maxValue);

		}

		public String getCounterName() {
			return counterName;
		}

		public void setCounterName(String counterName) {
			this.counterName = counterName;
		}
	}
}

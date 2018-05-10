package Utils.Iperf;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import Entities.ITrafficGenerator.CounterUnit;
import Entities.ITrafficGenerator.Protocol;
import Entities.ITrafficGenerator.TransmitDirection;
import Entities.StreamParams;
import UE.UE;
import Utils.GeneralUtils;
import Utils.Pair;

public class UEIPerf implements Runnable {

	/**
	 * IPerf traffic.
	 * 
	 * @author Avichai Yefet
	 */

	public static final String IPERF_TIME_LIMIT = "9999999";

	private UE ue;

	protected IPerfMachine iperfMachineDL;
	protected IPerfMachine iperfMachineUL;
	
	protected ArrayList<IPerfStream> ulStreamArrayList;
	protected ArrayList<IPerfStream> dlStreamArrayList;

	private double ulUeLoad;
	private double dlUeLoad;
	private Integer frameSize;
	private Protocol protocol;
	private long lastSampleTime;

	public UEIPerf(UE ue, IPerfMachine iperfMachineDL, IPerfMachine iperfMachineUL, double ulLoad,
			double dlLoad, Integer frameSize, ArrayList<Character> qciList) throws IOException, InterruptedException {
		
		this.ue = ue;

		this.iperfMachineDL = iperfMachineDL;
		this.iperfMachineUL = iperfMachineUL;
		this.ulUeLoad = ulLoad;
		this.dlUeLoad = dlLoad;
		this.frameSize = frameSize;
		this.protocol = Protocol.UDP;
		this.ulStreamArrayList = new ArrayList<IPerfStream>();
		this.dlStreamArrayList = new ArrayList<IPerfStream>();
		this.lastSampleTime = 0;
		
		for(Character qciChar : qciList){
			int qciInt = Integer.valueOf(qciChar)-Integer.valueOf('0');
			boolean state = qciInt == 9? true : false;
			String ueNumber = GeneralUtils.removeNonDigitsFromString(this.ue.getName());
			try {
				ulStreamArrayList.add(new IPerfStream(TransmitDirection.UL, ueNumber, qciInt, this.ue.getIPerfDlMachine(), this.ue.getIPerfDlMachine(), state, ulLoad/qciList.size(), frameSize));
				dlStreamArrayList.add(new IPerfStream(TransmitDirection.DL, ueNumber, qciInt, this.ue.getWanIpAddress(), this.ue.getIPerfUlMachine(), state, dlLoad/qciList.size(), frameSize));
			} catch (Exception e) {
				GeneralUtils.printToConsole(e.getMessage());
				e.printStackTrace();
			}
		}
	}

	public void run() {
		try {
			runTrafficUL();
			runTrafficDL();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void updateCounters(String ulCountersStr, String dlCountersStr){
		updateDLCounters(dlCountersStr);
		updateULCounters(ulCountersStr);
	}
	
	protected void updateDLCounters(String dlCountersStr) { 
		if(iperfMachineUL != null){
			try {
				for(IPerfStream dlIPerfStream : dlStreamArrayList){
					if(dlIPerfStream.isActive()){
						Integer parallel = dlIPerfStream.getNumberOfParallelIPerfStreams();
						Pair<Double, ArrayList<Long>> currentSample = iperfMachineUL.parseCounterFromIPerfServerRespond((parallel != null && parallel > 1? true : false), dlIPerfStream.getLastIntervalUsedForLastSample(), dlCountersStr, dlIPerfStream.getTpFileName());
						if(dlIPerfStream.getLastIntervalUsedForLastSample() == currentSample.getElement0()){ 
							GeneralUtils.printToConsole("Failed to get sample - ReSample.");
							String reSampleDlCountersStr = iperfMachineUL.getStrCounters(iperfMachineUL.getPreAddressTpFile() + dlIPerfStream.getTpFileName());
							currentSample = iperfMachineUL.parseCounterFromIPerfServerRespond((parallel != null && parallel > 1? true : false), dlIPerfStream.getLastIntervalUsedForLastSample(), reSampleDlCountersStr, dlIPerfStream.getTpFileName());
						}
						dlIPerfStream.setCountersInBits(currentSample);
					}
				}
			} catch (Exception e) {
				GeneralUtils.printToConsole("FAILED update DL counter.");
				e.printStackTrace();
			}
		}
	}

	protected void updateULCounters(String ulCountersStr) {
		if(iperfMachineDL != null){
			try {
				for(IPerfStream ulIPerfStream : ulStreamArrayList){
					if(ulIPerfStream.isActive()){
						Integer parallel = ulIPerfStream.getNumberOfParallelIPerfStreams();
						Pair<Double, ArrayList<Long>> currentSample = iperfMachineDL.parseCounterFromIPerfServerRespond((parallel != null && parallel > 1? true : false), ulIPerfStream.getLastIntervalUsedForLastSample(), ulCountersStr, ulIPerfStream.getTpFileName());
						if(ulIPerfStream.getLastIntervalUsedForLastSample() == currentSample.getElement0()){ 
							GeneralUtils.printToConsole("Failed to get sample - ReSample.");
							String reSampleUlCountersStr = iperfMachineDL.getStrCounters(iperfMachineDL.getPreAddressTpFile() + ulIPerfStream.getTpFileName());
							currentSample = iperfMachineDL.parseCounterFromIPerfServerRespond((parallel != null && parallel > 1? true : false), ulIPerfStream.getLastIntervalUsedForLastSample(), reSampleUlCountersStr, ulIPerfStream.getTpFileName());
						}
						ulIPerfStream.setCountersInBits(currentSample);
					}
				}
			} catch (Exception e) {
				GeneralUtils.printToConsole("FAILED update UL counter.");
				e.printStackTrace();
			}
		}
	}

	public void stopTraffic() {
		stopUlTraffic();
		stopDlTraffic();
	}

	public void stopUlTraffic() {
		if(iperfMachineUL != null){
			iperfMachineUL.stopIPerf();
		}else{
			GeneralUtils.printToConsole("UL IPerf Machine equals NULL.");
		}
	}

	public void stopDlTraffic() {
		if(iperfMachineDL != null){
			iperfMachineDL.stopIPerf();
		}else{
			GeneralUtils.printToConsole("DL IPerf Machine equals NULL.");
		}
	}

	public void runTrafficUL() {
		/**It is critical to start to listen before transmitting traffic for TCP Packets!!!
		   without it - it won't work! **/
		startULListener();
		runTrafficULClient();
	}

	public void runTrafficULClient() {
		if(iperfMachineUL != null){
			for(IPerfStream ulIPerfStream : ulStreamArrayList){
				if(ulIPerfStream.isActive()){
					iperfMachineUL.startIPerfTraffic(ulIPerfStream.getIperfClientCommand(), ulIPerfStream.getClientOutputFileName());
				}
			}
		}else{
			GeneralUtils.printToConsole("UL IPerf Machine equals NULL.");
		}
	}

	public void startULListener() {
		if(iperfMachineDL != null){
			for(IPerfStream ulIPerfStream : ulStreamArrayList){
				if(ulIPerfStream.isActive()){
					iperfMachineDL.startIPerfListener(ulIPerfStream.getNumberOfParallelIPerfStreams(), ulIPerfStream.getIperfServerCommand(), ulIPerfStream.getTpFileName());
				}
			}
		}else{
			GeneralUtils.printToConsole("DL IPerf Machine equals NULL.");
		}
	}

	public void runTrafficDL() {
		/**It is critical to start to listen before transmitting traffic for TCP Packets!!!
		   without it - it won't work! **/
		startDLListener();
		runTrafficDLClient();
	}

	public void runTrafficDLClient() {
		if(iperfMachineDL != null){
			for(IPerfStream dlIPerfStream : dlStreamArrayList){
				if(dlIPerfStream.isActive()){
					iperfMachineDL.startIPerfTraffic(dlIPerfStream.getIperfClientCommand(), dlIPerfStream.getClientOutputFileName());
				}
			}
		}else{
			GeneralUtils.printToConsole("DL IPerf Machine equals NULL.");
		}
	}

	public void startDLListener() {
		if(iperfMachineUL != null){
			for(IPerfStream dlIPerfStream : dlStreamArrayList){
				if(dlIPerfStream.isActive()){
					iperfMachineUL.startIPerfListener(dlIPerfStream.getNumberOfParallelIPerfStreams(), dlIPerfStream.getIperfServerCommand(), dlIPerfStream.getTpFileName());
				}
			}
		}else{
			GeneralUtils.printToConsole("UL IPerf Machine equals NULL.");
		}
	}
	
	

	private ArrayList<ArrayList<StreamParams>> getCounters(ArrayList<IPerfStream> streamArrayList, ArrayList<CounterUnit> counterUnitList) {
		ArrayList<ArrayList<StreamParams>> sampleArrayList = new ArrayList<ArrayList<StreamParams>>();
		int minNumberOfSamples = IPerfMachine.getMinNumberOfSamples();
		for(int i = 1; i <= minNumberOfSamples; i++){
			sampleArrayList.add(new ArrayList<StreamParams>());
		}
		long currentTime = System.currentTimeMillis();
		long firstSampleTime = currentTime > (this.lastSampleTime + (30 * 1000)) ? currentTime : (this.lastSampleTime + 1000);
		if(firstSampleTime == currentTime){
			GeneralUtils.printToConsole("firstSampleTime=currentTime="+firstSampleTime);
		}
		if(firstSampleTime == (this.lastSampleTime + 1000)){
			GeneralUtils.printToConsole("firstSampleTime=(this.lastSampleTime + 1000)="+firstSampleTime);
		}
		for(IPerfStream iperfStream : streamArrayList){
			if(iperfStream.isActive() && counterUnitList.contains(CounterUnit.BITS_PER_SECOND)){
				ArrayList<Long> currentValues = iperfStream.getCountersInBits();
				long sampleTime = firstSampleTime;
				for(int sampleIndex = 0; sampleIndex < minNumberOfSamples; sampleIndex++){
					long currentValue = 0;
					if(sampleIndex < currentValues.size()){
						currentValue = currentValues.get(sampleIndex);
					}
					StreamParams tempStreamParams = new StreamParams();
					tempStreamParams.setName(iperfStream.getStreamName());
					tempStreamParams.setTimeStamp(sampleTime);
					tempStreamParams.setActive(true);
					tempStreamParams.setUnit(CounterUnit.BITS_PER_SECOND);
					tempStreamParams.setTxRate((long)(iperfStream.getStreamLoad()*1000*1000));
					tempStreamParams.setRxRate(currentValue);
					tempStreamParams.setPacketSize(iperfStream.getFrameSize());
					sampleArrayList.get(sampleIndex).add(tempStreamParams);
					sampleTime += 1000;
				}
				iperfStream.setCountersInBits(new Pair<Double, ArrayList<Long>>(0.0, new ArrayList<Long>()));//Initial sample array.
			}
		}
		this.lastSampleTime = firstSampleTime + ((minNumberOfSamples - 1) * 1000);
		GeneralUtils.printToConsole("END OF getCounters Function: this.lastSampleTime="+this.lastSampleTime);
		return sampleArrayList;
	}
	
	public ArrayList<ArrayList<StreamParams>> getCounters(ArrayList<CounterUnit> counterUnitList){
		ArrayList<IPerfStream> allStreams = new ArrayList<IPerfStream>();
		allStreams.addAll(this.dlStreamArrayList);
		allStreams.addAll(this.ulStreamArrayList);
		return getCounters(allStreams, counterUnitList);
	}
	
	public ArrayList<ArrayList<StreamParams>> getDlCounters(ArrayList<CounterUnit> counterUnitList){
		return getCounters(this.dlStreamArrayList, counterUnitList);
	}

	public ArrayList<ArrayList<StreamParams>> getUlCounters(ArrayList<CounterUnit> counterUnitList) {
		return getCounters(this.ulStreamArrayList, counterUnitList);
	}

	public UE getUe() {
		return ue;
	}

	public double getUlUeLoad() {
		return ulUeLoad;
	}

	public void setUlLoad(double ulUeLoad) {
		this.ulUeLoad = ulUeLoad;
		int numberOfUlActiveStreamsPerDirection = 0;
		for(IPerfStream ulIPerfStream : ulStreamArrayList){
			if(ulIPerfStream.isActive()){
				numberOfUlActiveStreamsPerDirection++;
			}
		}
		double ulStreamLoad = ulUeLoad/numberOfUlActiveStreamsPerDirection;
		for(IPerfStream ulIPerfStream : ulStreamArrayList){
			ulIPerfStream.setStreamLoad(ulStreamLoad);
		}
	}

	public double getDlUeLoad() {
		return dlUeLoad;
	}

	public void setDlLoad(double dlUeLoad) {
		this.dlUeLoad = dlUeLoad;
		int numberOfDlActiveStreamsPerDirection = 0;
		for(IPerfStream dlIPerfStream : dlStreamArrayList){
			if(dlIPerfStream.isActive()){
				numberOfDlActiveStreamsPerDirection++;
			}
		}
		double dlStreamLoad = dlUeLoad/numberOfDlActiveStreamsPerDirection;
		for(IPerfStream dlIPerfStream : dlStreamArrayList){
			dlIPerfStream.setStreamLoad(dlStreamLoad);
		}
	}

	public ArrayList<ArrayList<String>> setStreamsState(ArrayList<String> ueNameList, ArrayList<Character> qciList,
			TransmitDirection transmitDirection, boolean state) {
		ArrayList<IPerfStream> allStream = new ArrayList<IPerfStream>();
		ArrayList<String> notStateStreamList = new ArrayList<String>();
		ArrayList<String> stateStreamList = new ArrayList<String>();
		allStream.addAll(dlStreamArrayList);
		allStream.addAll(ulStreamArrayList);
		for(IPerfStream iperfStream : allStream){
			//qci from stram
			char qciFromSTC = iperfStream.getStreamName().charAt(iperfStream.getStreamName().length() - 1); 
			//UE name form Stream (UE_)
			String ueNameFromIPerf = iperfStream.getStreamName().substring(3,iperfStream.getStreamName().length()-1);
			//check if the stream's Transmit Direction is NOT in transmitDirection 
			if(!transmitDirection.value.contains(iperfStream.getTransmitDirection().value)){
				notStateStreamList.add(iperfStream.getStreamName());
				System.out.println("Stream: "+iperfStream.getStreamName()+" isActive set to "+!state+" for transmit direction");
				iperfStream.setActive(!state);
			}//check if the QCI is NOT in QCI list
			else if(!qciList.contains(qciFromSTC)){
				notStateStreamList.add(iperfStream.getStreamName());
				System.out.println("Stream : "+iperfStream.getStreamName()+" isActive set to "+!state+" for QCI");
				iperfStream.setActive(!state);
			}//check if UE Name is NOT in Test UE Name List
			else if(!ueNameList.contains(ueNameFromIPerf)){
				notStateStreamList.add(iperfStream.getStreamName());
				System.out.println("Stream : "+iperfStream.getStreamName()+" isActive set to "+!state+" for UE");
				iperfStream.setActive(!state);
			}else{
				stateStreamList.add(iperfStream.getStreamName());
				System.out.println("Stream : "+iperfStream.getStreamName()+" isActive set to "+state);
				iperfStream.setActive(state);
			}
		}
		
		ArrayList<ArrayList<String>> enableDisableStreamName = new ArrayList<ArrayList<String>>();
		if(state){
			enableDisableStreamName.add(stateStreamList);
			enableDisableStreamName.add(notStateStreamList);
		}else{
			enableDisableStreamName.add(notStateStreamList);
			enableDisableStreamName.add(stateStreamList);
		}
		return enableDisableStreamName; 
	}

	public void setActiveState(ArrayList<String> streamNames, boolean state) {
		ArrayList<IPerfStream> allStream = new ArrayList<IPerfStream>();
		allStream.addAll(dlStreamArrayList);
		allStream.addAll(ulStreamArrayList);
		for(IPerfStream iperfStream : allStream){
			if(streamNames.contains(iperfStream.getStreamName())){
				iperfStream.setActive(state);
			}
		}
	}

	public Pair<Integer, Integer> getNumberOfDlAndUlActiveStreams() {
		int numberOfActiveDlStreams = 0;
		int numberOfActiveUlStreams = 0;
		for(IPerfStream dlIPerfStream : dlStreamArrayList){
			if(dlIPerfStream.isActive()){
				numberOfActiveDlStreams++;
			}
		}
		for(IPerfStream ulIPerfStream : ulStreamArrayList){
			if(ulIPerfStream.isActive()){
				numberOfActiveUlStreams++;
			}
		}
		return new Pair<Integer, Integer>(numberOfActiveDlStreams, numberOfActiveUlStreams);
	}

	public void setAllStreamActiveState(boolean state) {
		for(IPerfStream dlIPerfStream : dlStreamArrayList){
			dlIPerfStream.setActive(state);
		}
		for(IPerfStream ulIPerfStream : ulStreamArrayList){
			ulIPerfStream.setActive(state);
		}
	}

	public String getDlActiveStreamFileNames() {
		String dlActiveStreamFileNames = "";
		if(iperfMachineUL != null){
			for(IPerfStream dlIPerfStream : dlStreamArrayList){
				if(dlIPerfStream.isActive()){
					dlActiveStreamFileNames += (" " + iperfMachineUL.getPreAddressTpFile() + dlIPerfStream.getTpFileName());
				}
			}
		}else{
			GeneralUtils.printToConsole("UL IPerf Machine equals NULL.");
		}
		return dlActiveStreamFileNames;
	}
	
	public String getUlActiveStreamFileNames() {
		String ulActiveStreamFileNames = "";
		if(iperfMachineDL != null){
			for(IPerfStream ulIPerfStream : ulStreamArrayList){
				if(ulIPerfStream.isActive()){
					ulActiveStreamFileNames += (" " + iperfMachineDL.getPreAddressTpFile() + ulIPerfStream.getTpFileName());
				}
			}
		}else{
			GeneralUtils.printToConsole("DL IPerf Machine equals NULL.");
		}
		return ulActiveStreamFileNames;
	}

	public Integer getFrameSize() {
		return frameSize;
	}

	public void setFrameSize(Integer frameSize) {
		this.frameSize = frameSize;
		for(IPerfStream ulIPerfStream : ulStreamArrayList){
			ulIPerfStream.setFrameSize(frameSize);
		}
		for(IPerfStream dlIPerfStream : dlStreamArrayList){
			dlIPerfStream.setFrameSize(frameSize);
		}
	}

	public void setNumberParallelStreams(Integer numberParallel) {
		for(IPerfStream ulIPerfStream : ulStreamArrayList){
			ulIPerfStream.setNumberOfParallelIPerfStreams(numberParallel);
		}
		for(IPerfStream dlIPerfStream : dlStreamArrayList){
			dlIPerfStream.setNumberOfParallelIPerfStreams(numberParallel);
		}
	}
	
	public void setWindowSizeInKbs(Double windowSizeInKbs) {
		for(IPerfStream ulIPerfStream : ulStreamArrayList){
			ulIPerfStream.setWindowSizeInKbits(windowSizeInKbs);
		}
		for(IPerfStream dlIPerfStream : dlStreamArrayList){
			dlIPerfStream.setWindowSizeInKbits(windowSizeInKbs);
		}
	}
	
	public Protocol getProtocol() {
		return protocol;
	}

	public void setProtocol(Protocol protocol) {
		this.protocol = protocol;
		for(IPerfStream ulIPerfStream : ulStreamArrayList){
			ulIPerfStream.setProtocol(protocol);
		}
		for(IPerfStream dlIPerfStream : dlStreamArrayList){
			dlIPerfStream.setProtocol(protocol);
		}
	}

	public ArrayList<Pair<String, ArrayList<String>>> getAllActiveStreamsIPerfCommands() {
		ArrayList<Pair<String, ArrayList<String>>> activeStreamsIPerfCommands = new ArrayList<Pair<String, ArrayList<String>>>();
		for(IPerfStream ulIPerfStream : ulStreamArrayList){
			if(ulIPerfStream.isActive()){
				ArrayList<String> activeStreamIPerfCommands = new ArrayList<String>();
				activeStreamIPerfCommands.add(ulIPerfStream.getIperfClientCommand());
				activeStreamIPerfCommands.add(ulIPerfStream.getIperfServerCommand());
				activeStreamsIPerfCommands.add(new Pair<String, ArrayList<String>>(ulIPerfStream.getStreamName(), activeStreamIPerfCommands));
			}
		}
		for(IPerfStream dlIPerfStream : dlStreamArrayList){
			if(dlIPerfStream.isActive()){
				ArrayList<String> activeStreamIPerfCommands = new ArrayList<String>();
				activeStreamIPerfCommands.add(dlIPerfStream.getIperfClientCommand());
				activeStreamIPerfCommands.add(dlIPerfStream.getIperfServerCommand());
				activeStreamsIPerfCommands.add(new Pair<String, ArrayList<String>>(dlIPerfStream.getStreamName(), activeStreamIPerfCommands));
			}
		}
		return activeStreamsIPerfCommands;
	}

	public ArrayList<File> getResultFiles() {
		ArrayList<File> resultFiles = new ArrayList<File>();
		if(iperfMachineDL != null){
			for(IPerfStream ulIPerfStream : ulStreamArrayList){
				if(ulIPerfStream.isActive()){
					File resultFile = iperfMachineDL.getFile(ulIPerfStream.getTpFileName());
					resultFiles.add(resultFile);
				}
			}
		}
		if(iperfMachineUL != null){
			for(IPerfStream dlIPerfStream : dlStreamArrayList){
				if(dlIPerfStream.isActive()){
					File resultFile = iperfMachineUL.getFile(dlIPerfStream.getTpFileName());
					resultFiles.add(resultFile);
				}
			}	
		}
		return resultFiles;
	}
	
	public ArrayList<File> getClientOutputFiles() {
		ArrayList<File> clientOutputFiles = new ArrayList<File>();
		if(iperfMachineDL != null){
			for(IPerfStream ulIPerfStream : ulStreamArrayList){
				if(ulIPerfStream.isActive()){
					File clientOutputFile = iperfMachineUL.getFile(ulIPerfStream.getClientOutputFileName());
					clientOutputFiles.add(clientOutputFile);
				}
			}
		}
		if(iperfMachineUL != null){
			for(IPerfStream dlIPerfStream : dlStreamArrayList){
				if(dlIPerfStream.isActive()){
					File clientOutputFile = iperfMachineDL.getFile(dlIPerfStream.getClientOutputFileName());
					clientOutputFiles.add(clientOutputFile);
				}
			}	
		}
		return clientOutputFiles;
	}

	public int getNumberOfParallelIPerfStreamsOnDL() {
		for(IPerfStream dlStream : dlStreamArrayList){
			if(dlStream.isActive()){
				return dlStream.getNumberOfParallelIPerfStreams();
			}
		}
		return 0;
	}

	public int getNumberOfParallelIPerfStreamsOnUL() {
		for(IPerfStream ulStream : ulStreamArrayList){
			if(ulStream.isActive()){
				return ulStream.getNumberOfParallelIPerfStreams();
			}
		}
		return 0;
	}
}
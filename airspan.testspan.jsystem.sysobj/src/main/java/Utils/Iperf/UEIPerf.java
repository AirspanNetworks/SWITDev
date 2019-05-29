package Utils.Iperf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Entities.ITrafficGenerator.CounterUnit;
import Entities.ITrafficGenerator.Protocol;
import Entities.ITrafficGenerator.TransmitDirection;
import Entities.StreamParams;
import UE.UE;
import Utils.GeneralUtils;
import Utils.Pair;

public class UEIPerf {

	/**
	 * IPerf traffic.
	 * 
	 * @author Avichai Yefet
	 */

	public static final String IPERF_TIME_LIMIT = "9999999";

	protected UE ue;

	protected volatile IPerfMachine iperfMachineDL;
	
	protected volatile IPerfMachine iperfMachineUL;
	protected volatile ArrayList<IPerfStream> ulStreamArrayList;
	protected volatile ArrayList<IPerfStream> dlStreamArrayList;

	protected double ulUeLoad;
	protected double dlUeLoad;
	protected Integer frameSize;
	protected Protocol protocol;
	protected long lastSampleTime;

	public UEIPerf(UE ue, IPerfMachine iperfMachineDL, IPerfMachine iperfMachineUL, double ulLoad,
			double dlLoad, Integer frameSize, ArrayList<Character> qciList, Protocol protocol,
			TransmitDirection direction, Integer runTime) throws IOException, InterruptedException {
		
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
			//boolean state = qciInt == 9? true : false;
			String ueNumber = GeneralUtils.removeNonDigitsFromString(this.ue.getName());
			try {
				if(direction == TransmitDirection.BOTH || direction == TransmitDirection.UL){
					ulStreamArrayList.add(new IPerfStream(TransmitDirection.UL, ueNumber, qciInt, this.ue.getIPerfDlMachine(), this.ue.getIPerfDlMachine(), true, ulLoad/qciList.size(), frameSize,protocol,runTime));					
				}
				if(direction == TransmitDirection.BOTH || direction == TransmitDirection.DL){
					dlStreamArrayList.add(new IPerfStream(TransmitDirection.DL, ueNumber, qciInt, this.ue.getWanIpAddress(), this.ue.getIPerfUlMachine(), true, dlLoad/qciList.size(), frameSize,protocol,runTime));
				}
			} catch (Exception e) {
				GeneralUtils.printToConsole(e.getMessage());
				e.printStackTrace();
			}
		}
	}

	public void runTraffic() {
		try {
			long startTime = System.currentTimeMillis();
			runTrafficUL(startTime);
			runTrafficDL(startTime);
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
						Pair<Double, ArrayList<Long>> currentSample = iperfMachineUL.parseCounterFromIPerfServerRespond((parallel != null && parallel > 1), dlIPerfStream.getLastIntervalUsedForLastSample(), dlCountersStr, dlIPerfStream.getTpFileName());
						if(dlIPerfStream.getLastIntervalUsedForLastSample() == currentSample.getElement0()){ 
							GeneralUtils.printToConsole("Failed to get sample - ReSample.");
							String reSampleDlCountersStr = iperfMachineUL.getStrCounters(iperfMachineUL.getPreAddressTpFile() + dlIPerfStream.getTpFileName());
							currentSample = iperfMachineUL.parseCounterFromIPerfServerRespond((parallel != null && parallel > 1), dlIPerfStream.getLastIntervalUsedForLastSample(), reSampleDlCountersStr, dlIPerfStream.getTpFileName());
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
						Pair<Double, ArrayList<Long>> currentSample = iperfMachineDL.parseCounterFromIPerfServerRespond((parallel != null && parallel > 1), ulIPerfStream.getLastIntervalUsedForLastSample(), ulCountersStr, ulIPerfStream.getTpFileName());
						if(ulIPerfStream.getLastIntervalUsedForLastSample() == currentSample.getElement0()){ 
							GeneralUtils.printToConsole("Failed to get sample - ReSample.");
							String reSampleUlCountersStr = iperfMachineDL.getStrCounters(iperfMachineDL.getPreAddressTpFile() + ulIPerfStream.getTpFileName());
							currentSample = iperfMachineDL.parseCounterFromIPerfServerRespond((parallel != null && parallel > 1), ulIPerfStream.getLastIntervalUsedForLastSample(), reSampleUlCountersStr, ulIPerfStream.getTpFileName());
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
			for(IPerfStream ulIPerfStream : ulStreamArrayList){
				ulIPerfStream.setActive(false);
				ulIPerfStream.setRunningTraffic(false);
			}
		}else{
			GeneralUtils.printToConsole("UL IPerf Machine equals NULL.");
		}
	}

	public void stopDlTraffic() {
		if(iperfMachineDL != null){
			iperfMachineDL.stopIPerf();
			for(IPerfStream dlIPerfStream : dlStreamArrayList){
				dlIPerfStream.setActive(false);
				dlIPerfStream.setRunningTraffic(false);
			}
		}else{
			GeneralUtils.printToConsole("DL IPerf Machine equals NULL.");
		}
	}

	public void runTrafficUL(long startTime) {
		/**It is critical to start to listen before transmitting traffic for TCP Packets!!!
		   without it - it won't work! **/
		startULListener();
		runTrafficULClient(startTime);
	}

	protected void runTrafficULClient(long startTime) {
		if(iperfMachineUL != null){
			for(IPerfStream ulIPerfStream : ulStreamArrayList){
				if(ulIPerfStream.isActive() && !ulIPerfStream.isRunningTraffic()){
					IPerf.commandsUlClient += iperfMachineUL.startIPerfTraffic(ulIPerfStream.getIperfClientCommand(), ulIPerfStream.getClientOutputFileName(), TransmitDirection.UL)+"\n";
					ulIPerfStream.setRunningTraffic(true);
					ulIPerfStream.setTimeStart(startTime);
				}
			}
		}else{
			GeneralUtils.printToConsole("UL IPerf Machine equals NULL.");
		}
	}

	protected void startULListener() {
		if(iperfMachineDL != null){
			for(IPerfStream ulIPerfStream : ulStreamArrayList){
				if(ulIPerfStream.isActive() && !ulIPerfStream.isRunningTraffic()){
					IPerf.commandsUlServer += iperfMachineDL.startIPerfListener(ulIPerfStream.getNumberOfParallelIPerfStreams(), ulIPerfStream.getIperfServerCommand(), ulIPerfStream.getTpFileName(), TransmitDirection.UL)+"\n";
				}
			}
		}else{
			GeneralUtils.printToConsole("DL IPerf Machine equals NULL.");
		}
	}

	public void runTrafficDL(long startTime) {
		/**It is critical to start to listen before transmitting traffic for TCP Packets!!!
		   without it - it won't work! **/
		startDLListener();
		runTrafficDLClient(startTime);
	}

	protected void runTrafficDLClient(long startTime) {
		if(iperfMachineDL != null){
			for(IPerfStream dlIPerfStream : dlStreamArrayList){
				if(dlIPerfStream.isActive() && !dlIPerfStream.isRunningTraffic()){
					IPerf.commandsDlClient += iperfMachineDL.startIPerfTraffic(dlIPerfStream.getIperfClientCommand(), dlIPerfStream.getClientOutputFileName(), TransmitDirection.DL)+"\n";
					dlIPerfStream.setRunningTraffic(true);
					dlIPerfStream.setTimeStart(startTime);
				}
			}
		}else{
			GeneralUtils.printToConsole("DL IPerf Machine equals NULL.");
		}
	}

	protected void startDLListener() {
		if(iperfMachineUL != null){
			for(IPerfStream dlIPerfStream : dlStreamArrayList){
				if(dlIPerfStream.isActive() && !dlIPerfStream.isRunningTraffic()){
					IPerf.commandsDlServer += iperfMachineUL.startIPerfListener(dlIPerfStream.getNumberOfParallelIPerfStreams(), dlIPerfStream.getIperfServerCommand(), dlIPerfStream.getTpFileName(), TransmitDirection.DL)+"\n";
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
			if(ulIPerfStream.isActive() && !ulIPerfStream.isRunningTraffic()){
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
			if(dlIPerfStream.isActive() && !dlIPerfStream.isRunningTraffic()){
				numberOfDlActiveStreamsPerDirection++;
			}
		}
		double dlStreamLoad = dlUeLoad/numberOfDlActiveStreamsPerDirection;
		for(IPerfStream dlIPerfStream : dlStreamArrayList){
			dlIPerfStream.setStreamLoad(dlStreamLoad);				
		}
	}

	public ArrayList<ArrayList<String>> setStreamsState(Protocol protocol,ArrayList<String> ueNameList, ArrayList<Character> qciList,
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
			String ueNameFromIPerf = iperfStream.getStreamName().substring(7,iperfStream.getStreamName().length()-1);
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
			}else if(iperfStream.getProtocol() != protocol){
				notStateStreamList.add(iperfStream.getStreamName());
				System.out.println("Stream : "+iperfStream.getStreamName()+" isActive set to "+!state+" for traffic type");
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

	public ArrayList<ArrayList<String>> setStreamsStateAndNoChangeOthers(Protocol protocol,ArrayList<String> ueNameList, ArrayList<Character> qciList,
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
			String ueNameFromIPerf = iperfStream.getStreamName().substring(7,iperfStream.getStreamName().length()-1);
			if(transmitDirection.value.contains(iperfStream.getTransmitDirection().value) &&
					qciList.contains(qciFromSTC) && ueNameList.contains(ueNameFromIPerf) &&
					protocol == iperfStream.getProtocol()){
				stateStreamList.add(iperfStream.getStreamName());
				System.out.println("Stream : "+iperfStream.getStreamName()+" isActive set to "+state);
				iperfStream.setActive(state);
			}
		}
		
		ArrayList<ArrayList<String>> enableDisableStreamName = new ArrayList<ArrayList<String>>();
		enableDisableStreamName.add(stateStreamList);
		enableDisableStreamName.add(notStateStreamList);

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

	public Pair<Integer, Integer> getNumberOfDlAndUlActiveAndNotRunningStreams() {
		int numberOfActiveDlStreams = 0;
		int numberOfActiveUlStreams = 0;
		for(IPerfStream dlIPerfStream : dlStreamArrayList){
			if(dlIPerfStream.isActive() && !dlIPerfStream.isRunningTraffic()){
				numberOfActiveDlStreams++;
			}
		}
		for(IPerfStream ulIPerfStream : ulStreamArrayList){
			if(ulIPerfStream.isActive() && !ulIPerfStream.isRunningTraffic()){
				numberOfActiveUlStreams++;
			}
		}
		return new Pair<Integer, Integer>(numberOfActiveDlStreams, numberOfActiveUlStreams);
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
		/*this.protocol = protocol;
		for(IPerfStream ulIPerfStream : ulStreamArrayList){
			ulIPerfStream.setProtocol(protocol);
		}
		for(IPerfStream dlIPerfStream : dlStreamArrayList){
			dlIPerfStream.setProtocol(protocol);
		}*/
	}

	public ArrayList<Pair<String, ArrayList<String>>> getAllActiveStreamsIPerfCommands() {
		ArrayList<Pair<String, ArrayList<String>>> activeStreamsIPerfCommands = new ArrayList<Pair<String, ArrayList<String>>>();
		for(IPerfStream ulIPerfStream : ulStreamArrayList){
			if(ulIPerfStream.isActive() && !ulIPerfStream.isRunningTraffic()){
				ArrayList<String> activeStreamIPerfCommands = new ArrayList<String>();
				activeStreamIPerfCommands.add(ulIPerfStream.getIperfClientCommand());
				activeStreamIPerfCommands.add(ulIPerfStream.getIperfServerCommand());
				activeStreamsIPerfCommands.add(new Pair<String, ArrayList<String>>(ulIPerfStream.getStreamName(), activeStreamIPerfCommands));
			}
		}
		for(IPerfStream dlIPerfStream : dlStreamArrayList){
			if(dlIPerfStream.isActive()  && !dlIPerfStream.isRunningTraffic()){
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

	public String getKillCommand(){
		return "kill -9 ";
	}
	
	public void removeStreams(ArrayList<String> streamList){
		Iterator<IPerfStream> iter = dlStreamArrayList.iterator();
		while(iter.hasNext()){
			IPerfStream ips = iter.next();
			if(streamList.contains(ips.getStreamName())){
				iter.remove();
			}
		}
		
		iter = ulStreamArrayList.iterator();
		while(iter.hasNext()){
			IPerfStream ips = iter.next();
			if(streamList.contains(ips.getStreamName())){
				iter.remove();
			}
		}
	}
	
	public ArrayList<String> stopTraffic(ArrayList<String> streamList, String resultGrepDl, String resultGrepUl) {
		ArrayList<String> removedProcesses = new ArrayList<>();
		String process = null;
		if(!IPerf.commandsDl.contains("kill -9 ")){
			IPerf.commandsDl = "kill -9 ";			
		}
		if(!IPerf.commandsUl.contains(getKillCommand())){
			IPerf.commandsUl = getKillCommand();	
		}
		Iterator<IPerfStream> iter = dlStreamArrayList.iterator();
		while(iter.hasNext()){
			IPerfStream ips = iter.next();
			if(streamList.contains(ips.getStreamName())){
				process = getProcessNumber(resultGrepDl, ips.getIperfClientCommand());
				if(process == null){
					process = getProcessNumber(resultGrepUl, ips.getIperfClientCommand());
					if(process != null){
						IPerf.commandsUl += process+" ";				
					}
				}else{
					IPerf.commandsDl += process+" ";				
				}
				process = getProcessNumber(resultGrepDl, ips.getIperfServerCommand());
				if(process == null){
					process = getProcessNumber(resultGrepUl, ips.getIperfServerCommand());
					if(process != null){
						IPerf.commandsUl += process+" ";
					}
				}else{
					IPerf.commandsDl += process+" ";
				}
				
				// Remove the the selected process line to support Amarisoft.
				if(process != null){
					String[] lines = resultGrepUl.split("\n");
					for(String line:lines){
						if(line.contains(process)){
							resultGrepUl = resultGrepUl.replace(line, "");
							removedProcesses.add(process);
						}
					}
				}
			}
		}
		
		iter = ulStreamArrayList.iterator();
		while(iter.hasNext()){
			IPerfStream ips = iter.next();
			if(streamList.contains(ips.getStreamName())){
				process = getProcessNumber(resultGrepUl, ips.getIperfClientCommand());
				if(process == null){
					process = getProcessNumber(resultGrepDl, ips.getIperfClientCommand());
					if(process != null){
						IPerf.commandsDl += process+" ";						
					}
				}else{
					IPerf.commandsUl += process+" ";					
				}
				process = getProcessNumber(resultGrepUl, ips.getIperfServerCommand());
				if(process == null){
					process = getProcessNumber(resultGrepDl, ips.getIperfServerCommand());
					if(process != null){
						IPerf.commandsDl += process+" ";
					}
				}else{
					IPerf.commandsUl += process+" ";					
				}
				if(process != null){
					removedProcesses.add(process);
				}
			}
		}
		return removedProcesses;
	}
	
	private String getProcessNumber(String file, String command){
		Pattern p = Pattern.compile("[0-9a-z]\\s+(\\d+).*"+command);
		String[] lines = file.split("\n");
		boolean found, isSudo;
		for(String line:lines){
			Matcher m = p.matcher(line);
			found = m.find();
			isSudo = line.contains("sudo");
			if(found && !isSudo){
				return m.group(1);
			}
		}
		return null;
	}
	
	public ArrayList<ArrayList<StreamParams>> getAllStreamsResults(ArrayList<String> streamList) {
		ArrayList<ArrayList<StreamParams>> toReturn = new ArrayList<ArrayList<StreamParams>>();
		for(IPerfStream ips : dlStreamArrayList){
			if(streamList.contains(ips.getStreamName())){
				toReturn = extractStatisticsFromFile(ips,toReturn);
			}
		}
		for(IPerfStream ips : ulStreamArrayList){
			if(streamList.contains(ips.getStreamName())){
				toReturn = extractStatisticsFromFile(ips,toReturn);
			}
		}
		return toReturn;
	}
	
	private ArrayList<ArrayList<StreamParams>> extractStatisticsFromFile(IPerfStream ips, ArrayList<ArrayList<StreamParams>> ret){
		//ArrayList<StreamParams> toReturn = new ArrayList<StreamParams>();
		File file = new File(ips.getTpFileName());
		/*if(ips.getTransmitDirection() == TransmitDirection.UL){
			file = iperfMachineDL.getFile(ips.getTpFileName());
		}else{
			file = iperfMachineUL.getFile(ips.getTpFileName());
		}*/
		Pattern p = Pattern.compile("(\\d+.\\d+-\\s*\\d+.\\d+).*KBytes\\s+(\\d+|\\d+.\\d+)\\s+Kbits/sec.*");
		try{
			FileReader read = new FileReader(file);
			BufferedReader br = new BufferedReader(read);
			String line;
			long sampleTime = ips.getTimeStart();
			int sampleIndex = 0;
			while((line = br.readLine()) != null){
				Matcher m = p.matcher(line);
				if(m.find()){
					//System.out.println(m.group(1));
					//System.out.println(m.group(2));
					//String sampleTime = m.group(1);
					Long currentValue = 0L;
					if(m.group(2).contains(".")){
						currentValue = Long.valueOf(m.group(2).split("\\.")[0]);
					}else{
						currentValue = Long.valueOf(m.group(2));
					}
					
					StreamParams tempStreamParams = new StreamParams();
					tempStreamParams.setName(ips.getStreamName());
					tempStreamParams.setTimeStamp(sampleTime);
					tempStreamParams.setActive(true);
					tempStreamParams.setUnit(CounterUnit.BITS_PER_SECOND);
					tempStreamParams.setTxRate((long)(ips.getStreamLoad()*1000*1000));
					tempStreamParams.setRxRate(currentValue*1000);
					tempStreamParams.setPacketSize(ips.getFrameSize());
					sampleTime+=1000;
					try{
						ret.get(sampleIndex);
					}catch(Exception e){
						ret.add(new ArrayList<StreamParams>());
					}
					ret.get(sampleIndex).add(tempStreamParams);
					sampleIndex++;
				}
			}
			br.close();
			read.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		return ret;
	}

	public ArrayList<File> getResultFiles(ArrayList<String> streamList) {
		ArrayList<File> resultFiles = new ArrayList<File>();
		if(iperfMachineDL != null){
			for(IPerfStream ulIPerfStream : ulStreamArrayList){
				if(streamList.contains(ulIPerfStream.getStreamName())){
					File resultFile = new File(ulIPerfStream.getTpFileName());
					resultFiles.add(resultFile);
				}
			}
		}
		if(iperfMachineUL != null){
			for(IPerfStream dlIPerfStream : dlStreamArrayList){
				if(streamList.contains(dlIPerfStream.getStreamName())){
					File resultFile = new File(dlIPerfStream.getTpFileName());
					resultFiles.add(resultFile);
				}
			}
		}
		return resultFiles;
	}

	public ArrayList<File> getTransmitOutputFiles(ArrayList<String> streamList) {
		ArrayList<File> resultFiles = new ArrayList<File>();
		if(iperfMachineUL != null){
			for(IPerfStream ulIPerfStream : ulStreamArrayList){
				if(streamList.contains(ulIPerfStream.getStreamName())){
					File resultFile = new File(ulIPerfStream.getClientOutputFileName());
					resultFiles.add(resultFile);
				}
			}
		}
		if(iperfMachineDL != null){
			for(IPerfStream dlIPerfStream : dlStreamArrayList){
				if(streamList.contains(dlIPerfStream.getStreamName())){
					File resultFile = new File(dlIPerfStream.getClientOutputFileName());
					resultFiles.add(resultFile);
				}
			}
		}
		return resultFiles;
	}

	public Protocol getProtocolToStart() {
		if(iperfMachineUL != null){
			for(IPerfStream ulIPerfStream : ulStreamArrayList){
				if(ulIPerfStream.isActive && !ulIPerfStream.isRunningTraffic()){
					return ulIPerfStream.getProtocol();
				}
			}
		}
		if(iperfMachineDL != null){
			for(IPerfStream dlIPerfStream : dlStreamArrayList){
				if(dlIPerfStream.isActive && !dlIPerfStream.isRunningTraffic()){
					return dlIPerfStream.getProtocol();
				}
			}	
		}
		return null;
	}

	public ArrayList<ArrayList<StreamParams>> getResultsAfterTest() {
		ArrayList<ArrayList<StreamParams>> toReturn = new ArrayList<ArrayList<StreamParams>>();
		for(IPerfStream ips : dlStreamArrayList){
			toReturn = extractStatisticsFromFile(ips,toReturn);
		}
		for(IPerfStream ips : ulStreamArrayList){
			toReturn = extractStatisticsFromFile(ips,toReturn);
		}
		return toReturn;
	}

	public String getAllNamesInDlMachine(ArrayList<String> streamList) {
		String names = "";
		for(IPerfStream ips : dlStreamArrayList){
			if(streamList.contains(ips.getStreamName())){
				names += ips.getClientOutputFileName()+" ";
			}
		}
		for(IPerfStream ips : ulStreamArrayList){
			if(streamList.contains(ips.getStreamName())){
				names += ips.getTpFileName()+" ";
			}
		}
		return names;
	}

	public String getAllNamesInUlMachine(ArrayList<String> streamList) {
		String names = "";
		for(IPerfStream ips : dlStreamArrayList){
			if(streamList.contains(ips.getStreamName())){
				names += ips.getTpFileName()+" ";
			}
		}
		for(IPerfStream ips : ulStreamArrayList){
			if(streamList.contains(ips.getStreamName())){
				names += ips.getClientOutputFileName()+" ";
			}
		}
		return names;
	}

	public void copyAllFiles(String filesDl, String filesUl) {
		iperfMachineDL.getFileList(filesDl);
		iperfMachineUL.getFileList(filesUl);
	}


}

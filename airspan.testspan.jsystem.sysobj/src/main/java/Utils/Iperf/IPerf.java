package Utils.Iperf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import Entities.ITrafficGenerator;
import Entities.LoadParam;
import Entities.StreamParams;
import UE.AndroidUE;
import UE.UE;
import Utils.GeneralUtils;
import Utils.Pair;
import jsystem.framework.system.SystemManagerImpl;
import jsystem.framework.system.SystemObjectImpl;



public class IPerf extends SystemObjectImpl implements ITrafficGenerator{
	
	/**
	 * IPerf traffic.
	 * 
	 * @author Avichai Yefet
	 */
	
	private double ulPortLoad = 10;
	private double dlPortLoad = 70;
	private Integer frameSize = 1400;
	
	private static IPerf instance=null;
	
	public IPerfMachine iperfMachineDL;
	public IPerfMachine iperfMachineUL;
	
	private ArrayList<UE> ues;
	private ArrayList<UEIPerf> allUEsIPerfList;
	private String tpDlCountersFileNames;
	private String tpUlCountersFileNames;
	private String defultConfigFile;
	private String currentConfigFile;
	public IPerf() throws Exception {
		super();
		this.allUEsIPerfList = new ArrayList<UEIPerf>();
		this.defultConfigFile = "IPerfConfigFile.xml";
		PrintWriter defultConfigFileWriter = new PrintWriter(this.defultConfigFile);
        defultConfigFileWriter.close();
	}
	
	
	public synchronized static IPerf getInstance(ArrayList<UE> ues){
		
		try{
			if(instance==null){
				instance=(IPerf) SystemManagerImpl.getInstance().getSystemObject("IPerf");
			}
			instance.init(ues);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return instance;
	}

	
	private static boolean areIPerfMachinesConnected=false;
	public void init(ArrayList<UE> ues) throws Exception{
		Connect();
		
		setUEs(ues);
		
		ArrayList<Character> qciList = new ArrayList<Character>();
		tpDlCountersFileNames = "";
		tpUlCountersFileNames = "";
		qciList.add('9');
		qciList.add('7');
		allUEsIPerfList.clear();
		if(ues != null){
			for(UE ue : ues){
				UEIPerf ueIPerf = null;
				if(ue instanceof AndroidUE){
					ueIPerf = new AndroidIPerf((AndroidUE)ue, iperfMachineDL, iperfMachineUL, ulPortLoad/ues.size(), dlPortLoad/ues.size(), frameSize, qciList);
				}else{
					ueIPerf = new UEIPerf(ue, iperfMachineDL, iperfMachineUL, ulPortLoad/ues.size(), dlPortLoad/ues.size(), frameSize, qciList);
					tpDlCountersFileNames += (" " + ueIPerf.getDlActiveStreamFileNames());
					tpUlCountersFileNames += (" " + ueIPerf.getUlActiveStreamFileNames());
				}
				this.allUEsIPerfList.add(ueIPerf);
			}
		}
		updateConfigFile();
	}
	
	private static boolean isTrafficOn=false;
	public void startTraffic() throws Exception{
		
		if(isTrafficOn==false){
			Connect();
			ExecutorService exe =  Executors.newFixedThreadPool(allUEsIPerfList.size());
			for(UEIPerf ueIPerf : allUEsIPerfList){
				exe.execute(ueIPerf);
			}
			GeneralUtils.unSafeSleep(30000);
			isTrafficOn=true;
		}
		
	}
	
	private static boolean isDLon=false;
	public void startTrafficDL() throws Exception{
		
		if(isDLon==false){
			for(UEIPerf ueIPerf : allUEsIPerfList){
				ueIPerf.runTrafficDL();
			}
			isDLon=true;
		}
		
	}
	
	private static boolean isULon=false;
	public void startTrafficUL() throws Exception{
		
		if(isULon==false){
			for(UEIPerf ueIPerf : allUEsIPerfList){
				ueIPerf.runTrafficUL();
			}
			isULon=true;
		}
		
	}
	
	public void stopTraffic() throws Exception{
		if(isTrafficOn==true){
			for(UEIPerf ueIPerf : allUEsIPerfList){
				ueIPerf.stopTraffic();
			}
			isTrafficOn=true;
		}
		isTrafficOn=false;
		isDLon=false;
		isULon=false;
		Disconnect();
	}


	public ArrayList<UE> getUEs() {
		return ues;
	}


	public void setUEs(ArrayList<UE> uEs) {
		ues = uEs;
	}
	

	public double getUlPortLoad() {
		return ulPortLoad;
	}


	public void setUlPortLoad(double ulPortLoad) {
		this.ulPortLoad = ulPortLoad;
		for(UEIPerf ueIPerf : allUEsIPerfList){
			ueIPerf.setUlLoad(ulPortLoad);
		}
	}


	public double getDlPortLoad() {
		return dlPortLoad;
	}


	public void setDlPortLoad(double dlPortLoad) {
		this.dlPortLoad = dlPortLoad;
		for(UEIPerf ueIPerf : allUEsIPerfList){
			ueIPerf.setDlLoad(dlPortLoad);
		}
	}

	public Integer getFrameSize() {
		return frameSize;
	}


	public void setFrameSize(Integer frameSize) {
		this.frameSize = frameSize;
		for(UEIPerf ueIPerf : allUEsIPerfList){
			ueIPerf.setFrameSize(frameSize);
		}
	}
	
	@Override
	public boolean setProtocol(Protocol protocol) {
		for(UEIPerf ueIPerf : allUEsIPerfList){
			ueIPerf.setProtocol(protocol);
		}
		return true;
	}

	/********** implements ITrafficGenerator *******/
	
	@Override
	public void Connect() throws Exception {
		if(areIPerfMachinesConnected == false){
			areIPerfMachinesConnected = true;
			if(iperfMachineDL != null){
				areIPerfMachinesConnected = areIPerfMachinesConnected && iperfMachineDL.connect();
			}
			if(iperfMachineUL != null){
				areIPerfMachinesConnected = areIPerfMachinesConnected && iperfMachineUL.connect();
			}
		}
	}


	@Override
	public void Disconnect() throws Exception {
		if(areIPerfMachinesConnected == true){ 
			if(iperfMachineDL != null){ 
				iperfMachineDL.disconnect();
			}
			if(iperfMachineUL != null){
				iperfMachineUL.disconnect();
			}
			areIPerfMachinesConnected = false;
		}
	}

	@Override
	public void SetAllStreamActiveState(boolean state) throws Exception {
		for (UEIPerf ueIPerf : allUEsIPerfList) {
			ueIPerf.setAllStreamActiveState(state);
		}
	}
	
	@Override
	public void StartTrafficOnAllPorts() throws Exception {
		startTraffic();
	}


	@Override
	public void StopTrafficOnAllPorts() throws Exception {
		stopTraffic();
	}

	@Override
	public void readAllCounters(){
		//From DL Machine getting UL counters & From UL Machine getting DL counters 
		String ulCountersStr = iperfMachineDL == null ? "" : iperfMachineDL.getStrCounters(tpUlCountersFileNames);
		String dlCountersStr = iperfMachineUL == null ? "" :iperfMachineUL.getStrCounters(tpDlCountersFileNames);
		
		for(UEIPerf ueIPerf : allUEsIPerfList){
			ueIPerf.updateCounters(ulCountersStr, dlCountersStr);
		}
	}

	@Override
	public String getDefultConfigFile() {
		try {
			init(this.ues); //revert To Default - Called once on Traffic.getInstance
		} catch (Exception e) {
			GeneralUtils.printToConsole("Failed to initialize IPerf");
			e.printStackTrace();
		}
		return defultConfigFile;
	}

	public String getCurrentConfigFile() {
		return currentConfigFile;
	}

	public void setCurrentConfigFile(String currentConfigFile) {
		this.currentConfigFile = currentConfigFile;
	}
	
	@Override
	public String getUploadedConfigFile() {
		return updateConfigFile();
	}
	
	private String updateConfigFile(){
		//creating the file according the last used IPerf commands
				String fileContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
				fileContent += ("<IPerfConfigFile>");
				for(UEIPerf ueIPerf : allUEsIPerfList){
					ArrayList<Pair<String, ArrayList<String>>> streamNameAndIPerfCommandsList = ueIPerf.getAllActiveStreamsIPerfCommands();
					for(Pair<String, ArrayList<String>> streamNameAndIPerfCommands : streamNameAndIPerfCommandsList){
						String streamName = streamNameAndIPerfCommands.getElement0();
						ArrayList<String> iperfCommands = streamNameAndIPerfCommands.getElement1();
						fileContent += ("<"+streamName+">");
						for(String iperfCommand : iperfCommands){
							if(iperfCommand.contains("-c")){
								fileContent += ("<ClientCommand>");
								fileContent += ("iperf " + iperfCommand);
								fileContent += ("</ClientCommand>");
							}else{
								fileContent += ("<ServerCommand>");
								fileContent += ("iperf " + iperfCommand);
								fileContent += ("</ServerCommand>");
							}
						}
						fileContent += ("</"+streamName+">");
					}
				}
				fileContent += ("</IPerfConfigFile>");
				PrintWriter defultConfigFileWriter;
				try {
					defultConfigFileWriter = new PrintWriter(defultConfigFile);
					defultConfigFileWriter.println(fileContent);
					defultConfigFileWriter.close();
				} catch (FileNotFoundException e) {
					GeneralUtils.printToConsole("File NOT found.");
					e.printStackTrace();
				}
				
		        
				return fileContent;
	}

	@Override
	public boolean arpNdStart() throws Exception {
		GeneralUtils.printToConsole("IPerf.arpNdStart free true!!");
		return true;
	}

	@Override
	public boolean configPortLoad(double dl, double ul) {
		this.ulPortLoad = ul/1000;
		this.dlPortLoad = dl/1000;
		int numberOfUEsWithActiveDlStreams = 0;
		int numberOfUEsWithActiveUlStreams = 0;
		for(UEIPerf ueIPerf : allUEsIPerfList){
			Pair<Integer, Integer> dlAndUlActiveStreams = ueIPerf.getNumberOfDlAndUlActiveStreams();
			if(dlAndUlActiveStreams.getElement0() > 0){
				numberOfUEsWithActiveDlStreams++;
			}
			if(dlAndUlActiveStreams.getElement1() > 0){
				numberOfUEsWithActiveUlStreams++;
			}
		}
		double ulPerUeLoad = this.ulPortLoad/numberOfUEsWithActiveUlStreams;
		double dlPerUeLoad = this.dlPortLoad/numberOfUEsWithActiveDlStreams;
		for(UEIPerf ueIPerfIte : allUEsIPerfList){
			ueIPerfIte.setDlLoad(dlPerUeLoad);
			ueIPerfIte.setUlLoad(ulPerUeLoad);
		}
		return true;
	}

	@Override
	public ArrayList<ArrayList<StreamParams>> getActiveStreamCurrentSample(ArrayList<CounterUnit> counterUnitList,
			TransmitDirection transmitDirection) {
		ArrayList<ArrayList<StreamParams>> sampleArrayList = new ArrayList<ArrayList<StreamParams>>();
		ArrayList<ArrayList<StreamParams>> ueSampleArrayList = new ArrayList<ArrayList<StreamParams>>();
		int minNumberOfSamples = IPerfMachine.getMinNumberOfSamples();
		for(int i = 1; i <= minNumberOfSamples; i++){
			sampleArrayList.add(new ArrayList<StreamParams>());
			ueSampleArrayList.add(new ArrayList<StreamParams>());
		}
		for(UEIPerf ueIPerf : allUEsIPerfList){
			Pair<Integer, Integer> dlAndUlActiveStreams = ueIPerf.getNumberOfDlAndUlActiveStreams();
			if(dlAndUlActiveStreams.getElement0() > 0 || dlAndUlActiveStreams.getElement1() > 0){
				switch (transmitDirection) {
					case BOTH:
						ueSampleArrayList = ueIPerf.getCounters(counterUnitList);
						break;
					case DL:
						ueSampleArrayList = ueIPerf.getDlCounters(counterUnitList);
						break;
					case UL:
						ueSampleArrayList = ueIPerf.getUlCounters(counterUnitList);
						break;
					default:
						break;
				}
				for(int sampleIndex = 0; sampleIndex < minNumberOfSamples; sampleIndex++){
					sampleArrayList.get(sampleIndex).addAll(ueSampleArrayList.get(sampleIndex));
				}
			}
		}
		GeneralUtils.printToConsole("IPerfMachine.minNumberOfSamples="+minNumberOfSamples);
		//Initial minNumberOfSamples for trying to get max number of samples in the next round.
		IPerfMachine.setMinNumberOfSamples(10);
		return sampleArrayList;
	}
	
	@Override
	public void setActiveState(ArrayList<StreamParams> streamsParams, boolean state) {
		ArrayList<String> streamNames = new ArrayList<String>();
		for(StreamParams streamParams : streamsParams){
			streamNames.add(streamParams.getName());
		}
		for (UEIPerf ueIPerf : allUEsIPerfList) {
			ueIPerf.setActiveState(streamNames, state);
		}
	}

	@Override
	public ArrayList<ArrayList<String>> disableStreamsByUeNameQciAndPortDirectionAndEnableTheRest(ArrayList<String> ueNamesNotAllowdInTest,
			ArrayList<Character> qciListNotAllowdInTest, 
			TransmitDirection transmitDirection){
		report.report("Disabling Un-needed UEs, QCIs And Transmit Direction.");
		return setStreamsState(ueNamesNotAllowdInTest, qciListNotAllowdInTest, transmitDirection, false);
	}
	
	@Override
	public ArrayList<ArrayList<String>> enableStreamsByUeNameQciAndPortDirectionAndDisableTheRest(ArrayList<String> ueNamesAllowdInTest,
			ArrayList<Character> qciListAllowdInTest, 
			TransmitDirection transmitDirection){
		report.report("Enabling Needed Streams according UE number, QCIs And Transmit Direction.");
		return setStreamsState(ueNamesAllowdInTest, qciListAllowdInTest, transmitDirection, true);
	}
	
	private ArrayList<ArrayList<String>> setStreamsState(ArrayList<String> ueNameList,
			ArrayList<Character> qciList, 
			TransmitDirection transmitDirection,
			boolean state){
		
		ArrayList<ArrayList<String>> enableDisableStreams = new ArrayList<ArrayList<String>>();
		tpDlCountersFileNames = "";
		tpUlCountersFileNames = "";
		for(UEIPerf ueIPerf : allUEsIPerfList){
			ArrayList<ArrayList<String>> enableDisableUeIPerfStreams = ueIPerf.setStreamsState(ueNameList, qciList, transmitDirection, state);
			enableDisableStreams.add(enableDisableUeIPerfStreams.get(0));
			enableDisableStreams.add(enableDisableUeIPerfStreams.get(1));
			tpDlCountersFileNames += (" " + ueIPerf.getDlActiveStreamFileNames());
			tpUlCountersFileNames += (" " + ueIPerf.getUlActiveStreamFileNames());
		}
		updateConfigFile();
		return enableDisableStreams;
	}

	@Override
	public boolean isConnected() {
		GeneralUtils.printToConsole("isConnected NOT Implemented!");
		return true;
	}


	@Override
	public void setLoadStreamDLPort(ArrayList<LoadParam> load) {
		GeneralUtils.printToConsole("setLoadStreamDLPort NOT Implemented!");
	}


	@Override
	public void setLoadStreamULPort(ArrayList<LoadParam> load) {
		GeneralUtils.printToConsole("setLoadStreamULPort NOT Implemented!");
		
	}

	@Override
	public void SubscribeToCounter(CounterUnit counterUnit) throws Exception {
		GeneralUtils.printToConsole("Supporting just of BITS PER SECOND unit.");
	}


	@Override
	public void printArpInfo() {
		GeneralUtils.printToConsole("printArpInfo NOT Implemented!");
		report.report("Arp Was Successful!");
	}


	@Override
	public boolean clearStreamsResults() {
		GeneralUtils.printToConsole("clearStreamsResults NOT Implemented!");
		return true;
	}


	@Override
	public void recoveryConnection() {
		GeneralUtils.printToConsole("recoveryConnection NOT Implemented!");
	}


	@Override
	public void setFrameSizeStreamDLPort(ArrayList<LoadParam> arg0) throws IOException {
		GeneralUtils.printToConsole("setFrameSizeStreamDLPort NOT Implemented!");
	}


	@Override
	public void setFrameSizeStreamULPort(ArrayList<LoadParam> arg0) throws IOException {
		GeneralUtils.printToConsole("setFrameSizeStreamULPort NOT Implemented!");
	}


	@Override
	public ArrayList<String> enableStreamsByUeNameQciAndPortDirection(ArrayList<String> ueNamesAllowdInTest,
			ArrayList<Character> qciListAllowdInTest, TransmitDirection transmitDirection) {
		GeneralUtils.printToConsole("enableStreamsByUeNameQciAndPortDirection NOT Implemented!");
		return new ArrayList<String>();
	}


	@Override
	public ArrayList<File> getResultFiles() {
		ArrayList<File> resultFiles = new ArrayList<File>();
		for(UEIPerf ueIPerf : allUEsIPerfList){
			resultFiles.addAll(ueIPerf.getResultFiles());
		}
		return resultFiles;
	}

	@Override
	public ArrayList<File> getTransmitOutputFiles() {
		ArrayList<File> resultFiles = new ArrayList<File>();
		for(UEIPerf ueIPerf : allUEsIPerfList){
			resultFiles.addAll(ueIPerf.getClientOutputFiles());
		}
		return resultFiles;
	}
	
	@Override
	public boolean closeAllConnections() {
		try {
			stopTraffic();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}


	@Override
	public String setConfigFileWithHOPrefix(String TrafficFile) {
		GeneralUtils.printToConsole("IPerf: No Special File For HO - returning default config file.");
		return getDefultConfigFile();
	}


	@Override
	public boolean enableExtraCounter(CounterUnit counterUnit) {
		GeneralUtils.printToConsole("IPerf: Enable extra counters is not implemented");
		return false;
	}


	@Override
	public void takeActionAfterFoundHaltStream() {
		IPerfMachine.setMinNumberOfSamples(1);
	}


	@Override
	public void setNumberParallelStreams(Integer numberParallel) {
		for(UEIPerf ueIPerf : allUEsIPerfList){
			ueIPerf.setNumberParallelStreams(numberParallel);
		}
	}


	@Override
	public void setWindowSizeInKbs(Double windowSizeInKbs) {
		for(UEIPerf ueIPerf : allUEsIPerfList){
			ueIPerf.setWindowSizeInKbs(windowSizeInKbs);
		}
	}
}
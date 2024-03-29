package Utils.Iperf;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import Entities.ITrafficGenerator;
import Entities.LoadParam;
import Entities.StreamParams;
import Entities.ITrafficGenerator.Protocol;
import Entities.ITrafficGenerator.TransmitDirection;
import UE.AmarisoftUE;
import UE.AndroidUE;
import UE.UE;
import Utils.GeneralUtils;
import Utils.Pair;
import jsystem.framework.report.ReporterHelper;
import jsystem.framework.system.SystemManagerImpl;
import jsystem.framework.system.SystemObjectImpl;



public class IPerf extends SystemObjectImpl implements ITrafficGenerator{
	
	/**
	 * IPerf traffic.
	 * 
	 * @author Avichai Yefet
	 */
	public static String clientSideCommandsFile = "clientSide.txt";
	public static String serverSideCommandsFile = "serverSide.txt";
	public static String commandsUl;
	public static String commandsDl;
	
	public static String commandsUlServer;
	public static String commandsDlServer;
	
	public static String commandsUlClient;
	public static String commandsDlClient;
	
	
	private double ulPortLoad = 10;
	private double dlPortLoad = 70;
	private Integer frameSize = 1400;
	
	private static IPerf instance=null;
	
	public volatile IPerfMachine iperfMachineDL;
	public volatile IPerfMachine iperfMachineUL;
	
	private ArrayList<UE> ues;
	private volatile ArrayList<UEIPerf> allUEsIPerfList;
	private String tpDlCountersFileNames = null;
	private String tpUlCountersFileNames = null;
	private String defultConfigFile;
	private String currentConfigFile;
	private static boolean connected = false;
	
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

	public void init(ArrayList<UE> ues) throws Exception{
		Connect();
		
		setUEs(ues);
		
		ArrayList<Character> qciList = new ArrayList<Character>();
		tpDlCountersFileNames = "";
		tpUlCountersFileNames = "";
		qciList.add('9');
		allUEsIPerfList.clear();
		if(ues != null){
			for(UE ue : ues){
				UEIPerf ueIPerf = null;
				if(ue instanceof AmarisoftUE){
					ueIPerf = new AmarisoftIperf((AmarisoftUE)ue, iperfMachineDL, iperfMachineUL, ulPortLoad/ues.size(), dlPortLoad/ues.size(), frameSize, qciList,Protocol.UDP,TransmitDirection.BOTH,null);
				}else if(ue instanceof AndroidUE){
					ueIPerf = new AndroidIPerf((AndroidUE)ue, iperfMachineDL, iperfMachineUL, ulPortLoad/ues.size(), dlPortLoad/ues.size(), frameSize, qciList,Protocol.UDP,TransmitDirection.BOTH,null);
				}else{
					ueIPerf = new UEIPerf(ue, iperfMachineDL, iperfMachineUL, ulPortLoad/ues.size(), dlPortLoad/ues.size(), frameSize, qciList,Protocol.UDP,TransmitDirection.BOTH,null);
					tpDlCountersFileNames += (" " + ueIPerf.getDlActiveStreamFileNames());
					tpUlCountersFileNames += (" " + ueIPerf.getUlActiveStreamFileNames());
				}
				this.allUEsIPerfList.add(ueIPerf);
			}
		}
		updateConfigFile();
	}

	@Override
	public void initStreams(Protocol protocol, ArrayList<String> ues, ArrayList<Character> qciListAllowdInTest,
			TransmitDirection transmitDirection,Integer runTime, boolean resetStreams) throws Exception{
		if(tpDlCountersFileNames != null){
			tpDlCountersFileNames = "";
			tpUlCountersFileNames = "";			
		}
		if(resetStreams){
			allUEsIPerfList = new ArrayList<UEIPerf>();			
		}
		for(UE ue:this.ues){
			String ueName = "UE" + ue.getName().replaceAll("\\D+", "").trim();
			if(ues.contains(ueName)){
				UEIPerf ueIPerf = null;
				if(ue instanceof AmarisoftUE){
					ueIPerf = new AmarisoftIperf((AmarisoftUE)ue, iperfMachineDL, iperfMachineUL, ulPortLoad/ues.size(), dlPortLoad/ues.size(), frameSize, qciListAllowdInTest,protocol,transmitDirection,runTime);
				}else if(ue instanceof AndroidUE){
					ueIPerf = new AndroidIPerf((AndroidUE)ue, iperfMachineDL, iperfMachineUL, ulPortLoad/ues.size(), dlPortLoad/ues.size(), frameSize, qciListAllowdInTest,protocol,transmitDirection,runTime);
				}else{
					ueIPerf = new UEIPerf(ue, iperfMachineDL, iperfMachineUL, ulPortLoad/ues.size(), dlPortLoad/ues.size(), frameSize, qciListAllowdInTest,protocol,transmitDirection,runTime);
					tpDlCountersFileNames += (" " + ueIPerf.getDlActiveStreamFileNames());
					tpUlCountersFileNames += (" " + ueIPerf.getUlActiveStreamFileNames());
				}
				this.allUEsIPerfList.add(ueIPerf);
			}
		}
		updateConfigFile();
	}
	
	public void startTraffic() throws Exception {
		Connect();
		//ExecutorService exe = Executors.newFixedThreadPool(allUEsIPerfList.size());
		String ulServerCommandsFile = "UL"+ serverSideCommandsFile;
		String dlclientCommandsFile = "DL"+ clientSideCommandsFile;
		String dlServerCommandsFile = "DL"+ serverSideCommandsFile;
		String ulclientCommandsFile = "UL"+ clientSideCommandsFile;
		
		String ulServerCommandsFileWithPrefix = iperfMachineDL.preAddressTpFile + "UL"+ serverSideCommandsFile;
		String dlclientCommandsFileWithPrefix = iperfMachineDL.preAddressTpFile + "DL"+ clientSideCommandsFile;
		String dlServerCommandsFileWithPrefix = iperfMachineUL.preAddressTpFile + "DL"+ serverSideCommandsFile;
		String ulclientCommandsFileWithPrefix = iperfMachineUL.preAddressTpFile + "UL"+ clientSideCommandsFile;
		
		/*iperfMachineDL.sendCommand("echo '' > " + ulServerCommandsFile + " ; chmod +x " + ulServerCommandsFile);
		iperfMachineDL.sendCommand("echo '' > " + dlclientCommandsFile + " ; chmod +x " + dlclientCommandsFile);
		iperfMachineUL.sendCommand("echo '' > " + dlServerCommandsFile + " ; chmod +x " + dlServerCommandsFile);
		iperfMachineUL.sendCommand("echo '' > " + ulclientCommandsFile + " ; chmod +x " + ulclientCommandsFile);
		*/
		
		//commandsUl = "";
		//commandsDl = "";

		commandsUlServer = "";
		commandsDlServer = "";
			
		commandsUlClient = "";
		commandsDlClient = "";
		
		Protocol pro = getProtocol();
		for (UEIPerf ueIPerf : allUEsIPerfList) {
			ueIPerf.runTraffic();
			//exe.execute(ueIPerf);
			GeneralUtils.unSafeSleep(100);
		}
		
		GeneralUtils.printToConsole(System.getProperty("user.dir"));
		BufferedWriter writerDlServer = new BufferedWriter(new FileWriter(dlServerCommandsFile));
		writerDlServer.write(commandsDlServer);
		writerDlServer.close();
		
		BufferedWriter writerUlServer = new BufferedWriter(new FileWriter(ulServerCommandsFile));
		writerUlServer.write(commandsUlServer);
		writerUlServer.close();		
		
		BufferedWriter writerDlClient = new BufferedWriter(new FileWriter(dlclientCommandsFile));
		writerDlClient.write(commandsDlClient);
		writerDlClient.close();
		
		BufferedWriter writerUlClient = new BufferedWriter(new FileWriter(ulclientCommandsFile));
		writerUlClient.write(commandsUlClient);
		writerUlClient.close();	
		
		GeneralUtils.printToConsole("DL server:" + commandsDlServer);
		if(!iperfMachineDL.putFile(ulServerCommandsFile)){
			iperfMachineDL.sendCommand(convertToEchoCommands(commandsDlServer,ulServerCommandsFileWithPrefix));
		}
		
		GeneralUtils.printToConsole("DL client:" + commandsDlClient);
		if(!iperfMachineDL.putFile(dlclientCommandsFile)){
			iperfMachineDL.sendCommand(convertToEchoCommands(commandsDlClient,dlclientCommandsFileWithPrefix));

		}
		
		GeneralUtils.printToConsole("UL server:" + commandsUlServer);
		if(!iperfMachineUL.putFile(dlServerCommandsFile)){
			iperfMachineDL.sendCommand(convertToEchoCommands(commandsUlServer,dlServerCommandsFileWithPrefix));
		}
		
		GeneralUtils.printToConsole("UL client:" + commandsUlClient);
		if(!iperfMachineUL.putFile(ulclientCommandsFile)){
			iperfMachineDL.sendCommand(convertToEchoCommands(commandsUlClient,ulclientCommandsFileWithPrefix));
		}
		
		iperfMachineDL.sendCommand("chmod +x " + ulServerCommandsFileWithPrefix);
		iperfMachineDL.sendCommand("chmod +x " + dlclientCommandsFileWithPrefix);
		iperfMachineUL.sendCommand("chmod +x " + dlServerCommandsFileWithPrefix);
		iperfMachineUL.sendCommand("chmod +x " + ulclientCommandsFileWithPrefix);

		
		GeneralUtils.unSafeSleep(1000);
		
		if(pro == Protocol.TCP){
			iperfMachineDL.sendCommand("cat " + ulServerCommandsFileWithPrefix);
			iperfMachineDL.sendCommand(ulServerCommandsFileWithPrefix);
			iperfMachineUL.sendCommand("cat " + dlServerCommandsFileWithPrefix);
			iperfMachineUL.sendCommand(dlServerCommandsFileWithPrefix);
			GeneralUtils.unSafeSleep(1000);
			iperfMachineDL.sendCommand("cat " + dlclientCommandsFileWithPrefix);
			iperfMachineDL.sendCommand(dlclientCommandsFileWithPrefix);
			iperfMachineUL.sendCommand("cat "+ ulclientCommandsFileWithPrefix);
			iperfMachineUL.sendCommand(ulclientCommandsFileWithPrefix);
		}else{
			iperfMachineUL.sendCommand("cat "+ulclientCommandsFileWithPrefix);
			iperfMachineUL.sendCommand(ulclientCommandsFileWithPrefix);
			GeneralUtils.unSafeSleep(1000);
			iperfMachineDL.sendCommand("cat " +dlclientCommandsFileWithPrefix);
			iperfMachineDL.sendCommand(dlclientCommandsFileWithPrefix);
			GeneralUtils.unSafeSleep(10000);
			iperfMachineDL.sendCommand("cat " +ulServerCommandsFileWithPrefix);
			iperfMachineDL.sendCommand(ulServerCommandsFileWithPrefix);
			iperfMachineUL.sendCommand("cat " + dlServerCommandsFileWithPrefix);
			iperfMachineUL.sendCommand(dlServerCommandsFileWithPrefix);
		}
		
		GeneralUtils.unSafeSleep(2000);
		iperfMachineDL.sendCommand("ps -aux | grep iperf");
		GeneralUtils.unSafeSleep(2000);
		iperfMachineUL.sendCommand("ps -aux | grep iperf");
	}

	/*public void startTrafficDL() throws Exception {
		for (UEIPerf ueIPerf : allUEsIPerfList) {
			ueIPerf.runTrafficDL(System.currentTimeMillis());
		}
	}
	
	public void startTrafficUL() throws Exception{
		for(UEIPerf ueIPerf : allUEsIPerfList){
			ueIPerf.runTrafficUL(System.currentTimeMillis());
		}
	}*/
	
	private String convertToEchoCommands(String commandsToConvert, String commandsFile) {
		String[] temp = commandsToConvert.split("\n");
		String toRet="";
		for(String str : temp){
			toRet += "echo " + str + ">> "+commandsFile;
		}
		return toRet;
	}


	private Protocol getProtocol() {
		Protocol pro = Protocol.UDP;
		for(UEIPerf ueIPerf : allUEsIPerfList){
			pro = ueIPerf.getProtocolToStart();
			if(pro != null){
				return pro;
			}
		}
		return pro;
	}


	public void stopTraffic() throws Exception{
		for(UEIPerf ueIPerf : allUEsIPerfList){
			if (ueIPerf instanceof AndroidIPerf) {			
				ueIPerf.stopTraffic();
			}
		}
		iperfMachineDL.stopIPerf();
		iperfMachineUL.stopIPerf();
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
		/*for(UEIPerf ueIPerf : allUEsIPerfList){
			ueIPerf.setProtocol(protocol);
		}*/
		return true;
	}

	/********** implements ITrafficGenerator *******/
	
	@Override
	public void Connect() throws Exception {
		if(!connected){
			if(iperfMachineDL != null){
				iperfMachineDL.connect();
			}
			if(iperfMachineUL != null){
				iperfMachineUL.connect();
			}
			connected = true;
		}
	}


	@Override
	public void Disconnect() throws Exception {
		if(iperfMachineDL != null){ 
			iperfMachineDL.disconnect();
		}
		if(iperfMachineUL != null){
			iperfMachineUL.disconnect();
		}
		connected = false;
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
		/*String ulCountersStr = iperfMachineDL == null ? "" : iperfMachineDL.getStrCounters(tpUlCountersFileNames);
		String dlCountersStr = iperfMachineUL == null ? "" :iperfMachineUL.getStrCounters(tpDlCountersFileNames);
		
		for(UEIPerf ueIPerf : allUEsIPerfList){
			ueIPerf.updateCounters(ulCountersStr, dlCountersStr);
		}*/
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
			Pair<Integer, Integer> dlAndUlActiveStreams = ueIPerf.getNumberOfDlAndUlActiveAndNotRunningStreams();
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
		/*ArrayList<ArrayList<StreamParams>> ueSampleArrayList = new ArrayList<ArrayList<StreamParams>>();
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
		IPerfMachine.setMinNumberOfSamples(10);*/
		return new ArrayList<>();
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
	public ArrayList<ArrayList<String>> disableStreamsByUeNameQciAndPortDirectionAndEnableTheRest(Protocol protocol, ArrayList<String> ueNamesNotAllowdInTest,
			ArrayList<Character> qciListNotAllowdInTest, 
			TransmitDirection transmitDirection){
		report.report("Disabling Un-needed UEs, QCIs And Transmit Direction.");
		return setStreamsState(protocol,ueNamesNotAllowdInTest, qciListNotAllowdInTest, transmitDirection, false);
	}
	
	@Override
	public ArrayList<ArrayList<String>> enableStreamsByUeNameQciAndPortDirectionAndDisableTheRest(Protocol protocol, ArrayList<String> ueNamesAllowdInTest,
			ArrayList<Character> qciListAllowdInTest, 
			TransmitDirection transmitDirection){
		report.report("Enabling Needed Streams according UE number, QCIs And Transmit Direction.");
		return setStreamsState(protocol,ueNamesAllowdInTest, qciListAllowdInTest, transmitDirection, true);
	}
	
	private ArrayList<ArrayList<String>> setStreamsState(Protocol protocol,ArrayList<String> ueNameList,
			ArrayList<Character> qciList, 
			TransmitDirection transmitDirection,
			boolean state){
		
		ArrayList<ArrayList<String>> enableDisableStreams = new ArrayList<ArrayList<String>>();
		enableDisableStreams.add(new ArrayList<String>());
		enableDisableStreams.add(new ArrayList<String>());
		tpDlCountersFileNames = "";
		tpUlCountersFileNames = "";
		for(UEIPerf ueIPerf : allUEsIPerfList){
			ArrayList<ArrayList<String>> enableDisableUeIPerfStreams = ueIPerf.setStreamsState(protocol,ueNameList, qciList, transmitDirection, state);
			enableDisableStreams.get(0).addAll(enableDisableUeIPerfStreams.get(0));
			enableDisableStreams.get(1).addAll(enableDisableUeIPerfStreams.get(1));
			tpDlCountersFileNames += (" " + ueIPerf.getDlActiveStreamFileNames());
			tpUlCountersFileNames += (" " + ueIPerf.getUlActiveStreamFileNames());
		}
		updateConfigFile();
		return enableDisableStreams;
	}

	private ArrayList<ArrayList<String>> setStreamsStateAndNoChangeOthers(Protocol protocol,ArrayList<String> ueNameList,
			ArrayList<Character> qciList, 
			TransmitDirection transmitDirection,
			boolean state){
		
		ArrayList<ArrayList<String>> enableDisableStreams = new ArrayList<ArrayList<String>>();
		enableDisableStreams.add(new ArrayList<String>());
		enableDisableStreams.add(new ArrayList<String>());
		tpDlCountersFileNames = "";
		tpUlCountersFileNames = "";
		for(UEIPerf ueIPerf : allUEsIPerfList){
			ArrayList<ArrayList<String>> enableDisableUeIPerfStreams = ueIPerf.setStreamsStateAndNoChangeOthers(protocol,ueNameList, qciList, transmitDirection, state);
			enableDisableStreams.get(0).addAll(enableDisableUeIPerfStreams.get(0));
			enableDisableStreams.get(1).addAll(enableDisableUeIPerfStreams.get(1));
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
	public ArrayList<String> enableStreamsByUeNameQciAndPortDirection(Protocol protocol,ArrayList<String> ueNamesAllowdInTest,
			ArrayList<Character> qciListAllowdInTest, TransmitDirection transmitDirection) {
		return setStreamsStateAndNoChangeOthers(protocol,ueNamesAllowdInTest, qciListAllowdInTest, transmitDirection, true).get(0);
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
	public ArrayList<File> getCommandFiles() {
		ArrayList<File> resultFiles = new ArrayList<File>();
		resultFiles.add(iperfMachineDL.getFile("UL" + serverSideCommandsFile));
		resultFiles.add(iperfMachineDL.getFile("DL" + clientSideCommandsFile));
		resultFiles.add(iperfMachineUL.getFile("DL" + serverSideCommandsFile));
		resultFiles.add(iperfMachineUL.getFile("UL" + clientSideCommandsFile));
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
	public void resetIperfList(){
		this.allUEsIPerfList = new ArrayList<UEIPerf>();
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

	@Override
	public void getResultFilesByList(ArrayList<String> streamList) {
		ArrayList<File> resultFiles = new ArrayList<File>();
		for(UEIPerf ueIPerf : allUEsIPerfList){
			resultFiles.addAll(ueIPerf.getResultFiles(streamList));
		}
		if (!resultFiles.isEmpty()){
			GeneralUtils.startLevel("Result Files");
			for(File resultFile : resultFiles){
				File toUpload = new File(resultFile.getName());
				resultFile.renameTo(toUpload);
				try {
					ReporterHelper.copyFileToReporterAndAddLink(report, toUpload, toUpload.getName());
				} catch (Exception e) {
					GeneralUtils.printToConsole("FAIL to upload TP Result File: " + resultFile.getName());
					e.printStackTrace();
				}
			}
			GeneralUtils.stopLevel();
		}
		ArrayList<File> transmitOutputFiles = new ArrayList<File>();
		for(UEIPerf ueIPerf : allUEsIPerfList){
			transmitOutputFiles.addAll(ueIPerf.getTransmitOutputFiles(streamList));
		}
		if (!transmitOutputFiles.isEmpty()){
			GeneralUtils.startLevel("Transmit Ouput Files");
			for(File transmitOutputFile : transmitOutputFiles){
				File toUpload = new File(transmitOutputFile.getName());
				transmitOutputFile.renameTo(toUpload);
				try {
					ReporterHelper.copyFileToReporterAndAddLink(report, toUpload, toUpload.getName());
				} catch (Exception e) {
					GeneralUtils.printToConsole("FAIL to upload TP Result File: " + transmitOutputFile.getName());
					e.printStackTrace();
				}
			}
			GeneralUtils.stopLevel();
		}
	}
	
	@Override
	public void stopTraffic(ArrayList<String> streamList) {
		Pair<Boolean, String> resUl, resDl;
		while(true){ 
			commandsUl = "";
			commandsDl = "";
			String resultGrepDl = iperfMachineDL.sendCommand("ps -aux | grep iperf").getElement1();
			GeneralUtils.unSafeSleep(2000);
			String resultGrepUl = iperfMachineUL.sendCommand("ps -aux | grep iperf").getElement1();
			GeneralUtils.unSafeSleep(2000);
	
			for(UEIPerf ueIPerf : allUEsIPerfList){
				ArrayList<String> processes = ueIPerf.stopTraffic(streamList, resultGrepDl, resultGrepUl);
				// Remove the the selected process line to support Amarisoft.
				for(String process : processes){
					String[] lines = resultGrepUl.split("\n");
					for(String line:lines){
						if(line.contains(process)){
							resultGrepUl = resultGrepUl.replace(line, "");
						}
					}
				}
			}
			iperfMachineDL.sendCommand(commandsDl);
			iperfMachineUL.sendCommand(commandsUl);
			GeneralUtils.unSafeSleep(3000);
			resDl = iperfMachineDL.sendCommand("ps -aux | grep iperf");		
			resUl = iperfMachineUL.sendCommand("ps -aux | grep iperf");
			if(resDl.getElement0() && resUl.getElement0())
				break;
			GeneralUtils.unSafeSleep(1000);
		}
	}
	
	@Override
	public void removeStreams(ArrayList<String> streamList){
		Iterator<UEIPerf> iter = allUEsIPerfList.iterator();
		UEIPerf ueIPerf;
		while(iter.hasNext()){
			ueIPerf = iter.next();
			ueIPerf.removeStreams(streamList);
			if(ueIPerf instanceof AmarisoftIperf)
				iter.remove();
		}
	}
	
	@Override
	public ArrayList<ArrayList<StreamParams>> getAllStreamsResults(ArrayList<String> streamList) {
		ArrayList<ArrayList<StreamParams>> toReturn = new ArrayList<ArrayList<StreamParams>>();
		for(UEIPerf ueIPerf : allUEsIPerfList){	
			ArrayList<ArrayList<StreamParams>> temp = ueIPerf.getAllStreamsResults(streamList);
			for(int index=0;index<temp.size();index++){
				try{
					toReturn.get(index);
				}catch(Exception e){
					toReturn.add(new ArrayList<StreamParams>());
				}
				toReturn.get(index).addAll(temp.get(index));
			}
		}
		return toReturn;
	}


	@Override
	public ArrayList<ArrayList<StreamParams>> getResultsAfterTest(
			ArrayList<ArrayList<StreamParams>> listOfStreamList2) {
		ArrayList<ArrayList<StreamParams>> toReturn = new ArrayList<ArrayList<StreamParams>>();
		for(UEIPerf ueIPerf : allUEsIPerfList){	
			ArrayList<ArrayList<StreamParams>> temp = ueIPerf.getResultsAfterTest();
			for(int index=0;index<temp.size();index++){
				try{
					toReturn.get(index);
				}catch(Exception e){
					toReturn.add(new ArrayList<StreamParams>());
				}
				toReturn.get(index).addAll(temp.get(index));
			}
		}
		return toReturn;
	}
	
	@Override
	public boolean copyAllResultFiles(ArrayList<String> streamList){
		String filesDl = "";
		String filesUl = "";
		for(UEIPerf ueIPerf : allUEsIPerfList){
			filesDl += ueIPerf.getAllNamesInDlMachine(streamList);
		}
		for(UEIPerf ueIPerf : allUEsIPerfList){
			filesUl += ueIPerf.getAllNamesInUlMachine(streamList);
		}
		for(UEIPerf ueIPerf : allUEsIPerfList){
			ueIPerf.copyAllFiles(filesDl,filesUl);
			break;
		}
		return true;
	}
}

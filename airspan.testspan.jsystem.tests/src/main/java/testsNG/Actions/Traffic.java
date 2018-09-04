package testsNG.Actions;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import EnodeB.EnodeB;
import Entities.ITrafficGenerator;
import Entities.ITrafficGenerator.CounterUnit;
import Entities.ITrafficGenerator.Protocol;
import Entities.ITrafficGenerator.TransmitDirection;
import Entities.LoadParam;
import Entities.StreamParams;
import UE.UE;
import Utils.GeneralUtils;
import Utils.Pair;
import Utils.SetupUtils;
import Utils.StreamList;
import Utils.Iperf.IPerf;
//import Utils.Iperf.IPerf;
import jsystem.framework.report.ListenerstManager;
import jsystem.framework.report.Reporter;
import jsystem.framework.report.ReporterHelper;
import jsystem.framework.system.SystemManagerImpl;
import jsystem.framework.system.SystemObject;
import jsystem.framework.system.SystemObjectManager;
import jsystem.sysobj.testcenter.TestCenter;
import jsystem.sysobj.testcenter.emulations.TestCenterEmulations;
import jsystem.sysobj.testcenter.objects.StcObjectResults;
import jsystem.sysobj.testcenter.objects.StcPort;
import jsystem.sysobj.testcenter.objects.StcStatisticsView;
import jsystem.sysobj.testcenter.objects.StcStream;
import jsystem.sysobj.traffic.TrafficException;
import jsystem.sysobj.traffic.PacketStream.EnumStreamRate;
import jsystem.sysobj.traffic.PacketStream.EnumStreamSize;
import jsystem.sysobj.traffic.customer.CustomerTrafficPort;
import jsystem.sysobj.traffic.emulations.L23TrafficPortEmulations;
import junit.framework.Assert;
import testsNG.Actions.Utils.*;

public class Traffic {

	public static String FILE_NAME = "CalculatorTpt.xlsx";
	private static Traffic instance;
	private static ExecutorService executorService;
	private static ArrayList<PingToUE> pingToUEList;
	private TestCenterEmulations tg;
	private final int TRAFFIC_STABLIZING_TIME = 16000; // 16 SEC
	private final int MILIS_IN_SEC = 1000;
	private final String DL_PORT_NAME = "DL";
	private final int DL_PORT_INDX = 0;
	private final String UL_PORT_NAME = "UL";
	private final int UL_PORT_INDX = 1;
	protected CustomerTrafficPort downlinkPort; // DL port
	protected CustomerTrafficPort uplinkPort; // UL Port
	protected String[] streams;
	protected StreamParams[] activeStreams = new StreamParams[] {};
	public File configFile = null;
	public File uploadToReportConfigFile = null;
	private TrafficGeneratorType generatorType = TrafficGeneratorType.ITraffic;
	private TrafficType genType;
	private SystemObjectManager system = SystemManagerImpl.getInstance();
	private StcStatisticsView generatorResults;
	private StcStatisticsView analyzerResults;
	private StcStatisticsView txStreamResults;
	private StcStatisticsView rxStreamResults;
	private StcStatisticsView rxStreamBlockResults;
	protected HashMap<Integer, FrameSizeResult> fsResults = new HashMap<Integer, FrameSizeResult>();
	Map<String, Long> counters = null;
	protected List<String> streamCounters = null;
	private ArrayList<String> activeStreamsArray = new ArrayList<String>();
	private String originalSessionName;
	private StcStreamULDLAdapter streamsAdapter;
	private Reporter report = ListenerstManager.getInstance();
	private boolean firstTestFlag = true;
	/* ITraffic GENERATOR */
	ITrafficGenerator trafficGenerator;

	/**
	 * trying to initalize gen Traffic generator, Itraffic Instance after and
	 * Iperf instance if those arent working.
	 * 
	 * @author Shahaf Shuhamy
	 * @throws Exception
	 */
	private Traffic(ArrayList<UE> ues){
		
		try {
			GeneralUtils.printToConsole("before trying to set RestSTC");
			trafficGenerator = (ITrafficGenerator) system.getSystemObject("RestSTC");
			generatorType = TrafficGeneratorType.ITraffic;
			GeneralUtils.printToConsole("done setting RestSTC");
		} catch (Exception e) {
			GeneralUtils.printToConsole("Could not init Rest Object , "+e.getMessage());
			try {
				GeneralUtils.printToConsole("trying to set Gen");
				tg = (TestCenterEmulations) system.getSystemObject("gen");
				generatorType = TrafficGeneratorType.TestCenter;
				GeneralUtils.printToConsole("done setting Gen");
			} catch (Exception e1) {
				GeneralUtils.printToConsole("Could not init Gen Object , "+e1.getMessage());
				GeneralUtils.printToConsole("trying to set IPERF");
				trafficGenerator = IPerf.getInstance(ues);
				if(trafficGenerator == null){
					report.report("There is no Traffic instance in SUT!",Reporter.WARNING);
					instance = null;
					return;
				}
				generatorType = TrafficGeneratorType.ITraffic;
				GeneralUtils.printToConsole("done setting IPERF");
			}
		}
	}

	public enum GeneratorType{
		STC, Iperf;
	}
	
	private Traffic(ArrayList<UE> ues, GeneratorType type){
		if(type == GeneratorType.STC){
			try {
				GeneralUtils.printToConsole("Trying to set RestSTC");
				trafficGenerator = (ITrafficGenerator) system.getSystemObject("RestSTC");
				generatorType = TrafficGeneratorType.ITraffic;
				GeneralUtils.printToConsole("done setting RestSTC");
			} catch (Exception e) {
				e.printStackTrace();
				report.report("There is no traffic STC instance in SUT!",Reporter.FAIL);
				instance = null;
				return;
			}
		}else if(type == GeneratorType.Iperf){
			GeneralUtils.printToConsole("Trying to set IPERF");
			trafficGenerator = IPerf.getInstance(ues);
			if(trafficGenerator == null){
				report.report("There is no traffic IPERF instance in SUT!",Reporter.FAIL);
				instance = null;
				return;
			}
			generatorType = TrafficGeneratorType.ITraffic;
			GeneralUtils.printToConsole("done setting IPERF");
		}
	}
	
	/**
	 * returns instance of the current SUT generator
	 * 
	 * @return
	 * @throws Exception
	 */
	public static Traffic getInstance(ArrayList<UE> ues){
		if (instance == null){
			instance = new Traffic(ues);
		}
		if(instance == null){
			return null;
		}
		instance.revertToDefault(instance.generatorType);
		return instance;
	}
	
	public static Traffic getInstanceWithSpecificGeneratorType(ArrayList<UE> ues, GeneratorType type){
		if (instance == null){
			instance = new Traffic(ues, type);
		}
		if(instance == null){
			return null;
		}
		instance.revertToDefault(instance.generatorType);
		return instance;
	}
	
	private void revertToDefault(TrafficGeneratorType type){
		
		switch(instance.generatorType){
		case ITraffic:
			instance.configFile = new File(instance.trafficGenerator.getDefultConfigFile());
			instance.trafficGenerator.setCurrentConfigFile(instance.trafficGenerator.getDefultConfigFile());
			break;
			
		case TestCenter:
			instance.configFile = new File(instance.tg.getDefaultConfigTccFile());
			break;
			}
	}

	/**
	 * inits STC Sprirent getting ports init config file disableing Stc debug
	 * loading ports
	 * 
	 * @throws Exception
	 */
	public boolean init() throws Exception {
		switch (generatorType) {
		case TestCenter:
			tg = (TestCenterEmulations) system.getSystemObject("gen");
			tg.setLifeTime(SystemObject.PERMANENT_LIFETIME);
			downlinkPort = tg.getCustomerPorts()[DL_PORT_INDX];
			uplinkPort = tg.getCustomerPorts()[UL_PORT_INDX];
			initStcConfigFile();
			disableStcDebug();
			if (!loadRelocate(DL_PORT_INDX, UL_PORT_INDX)) {
				return false;
			}
			return true;
		case ITraffic:
				trafficGenerator.closeAllConnections();
				if(!initAndConnectSession()){
					return false;
				}
			return true;
		default:
			return false;
		}
	}


	private boolean initAndConnectSession(){
		try{
			GeneralUtils.printToConsole("Checking for tcc files location");
			initStcConfigFile();
			GeneralUtils.printToConsole("STC config file in place");
			
			GeneralUtils.printToConsole("trying to connect to STC Chassis");
			trafficGenerator.Connect();
			GeneralUtils.printToConsole("Stc Session connected");
			
			return true;
		}catch(Exception e){
			e.printStackTrace();
		}
		return false;
	}


	/**
	 * if "configFile" is null - init it via tg object if file isn't exist then
	 * make the ConfigFile null again in order to fail in assert and not get
	 * into the loop
	 */
	private void initStcConfigFile() {
		String path = null;

		if (configFile == null) {
			switch (generatorType) {
			case TestCenter:
				path = tg.getDefaultConfigTccFile();
				break;
			case ITraffic:
				path = trafficGenerator.getDefultConfigFile();
			}

			if (path == null || path.isEmpty())
				Assert.fail("No config file been configured. please add defaultConfigTccFile tag under gen in SUT");
			GeneralUtils.printToConsole(String.format("Trying to load the defaultConfigTccFile from \"%s\"", path));
			configFile = new File(path);

		}

		if (!configFile.exists()) {
			configFile = null;
			Assert.fail("No such tcc file (" + configFile + ") exist, please reconfigure defaultConfigTccFile to the right path");
		}
	}

	/**
	 * via trafficGen Shell disabling commands setLogFile setPureTclLogFile
	 * setPrintCommand setPrintReturn
	 */
	private void disableStcDebug() {
		tg.getTraffic().getShell().setLogFile(null);
		tg.getTraffic().getShell().setPureTclLogFile(null);
		tg.getTraffic().getShell().setPrintCommand(false);
		tg.getTraffic().getShell().setPrintReturn(false);

	}

	/**
	 * starts Traffic in both implementations : 1. Test Center 2. ITraffic
	 * 
	 * @throws Exception
	 */
	public boolean startTraffic() throws Exception {
		return startTraffic(TrafficCapacity.LOWTPT);
	}
	
	public boolean initTrafficWithNoStartTraffic() throws Exception{
		return initTrafficWithNoStartTraffic(TrafficCapacity.LOWTPT);
	}
	
	public boolean initTrafficWithNoStartTraffic(TrafficCapacity capacity) throws Exception{
		switch(capacity){
		case FULLTPT:
			if (!init()) {
				return false;
			}
			
			switch (generatorType) {
			case TestCenter:
				streamsAdapter = initActiveStreams();
				try {
					activeStreamsArray = turnListsIntoStringArray(streamsAdapter.UlStreams, streamsAdapter.DlStreams);
				} catch (TrafficException e) {
					e.printStackTrace();
				}
				updateStreamsAccordingToActiveStreamArray();
				GeneralUtils.printToConsole("subscribing to STC counters for later use...");
				subscribeToSTCFile();
				break;
			case ITraffic:
				GeneralUtils.printToConsole("subscribing to STC counters for later use...");
				subscribeToSTCFile();
				break;
			}
			return true;
		
		case LOWTPT:
			if(!trafficGenerator.isConnected()){ 	//in the case that not TPT test - file loading causing problems.
				GeneralUtils.printToConsole("Stc Session Not connected");
				if (!init()) {
					GeneralUtils.printToConsole("STC session init fail");
					return false;
				}
			}
			trafficGenerator.SetAllStreamActiveState(true);
			Double loadDL = new Double(10);
			Double loadUL = new Double(1);
			Pair<Double,Double> loadPair = new Pair<Double,Double>(loadDL,loadUL);
			trafficLoadSet(loadPair);
			subscribeToSTCFile();
			return true;
			
		case CUSTOM:
			if (!init()) {
				return false;
			}
			subscribeToSTCFile();
			return true;
		}
		
		return false;	
	}
	
	public void initStreams(Protocol protocol, ArrayList<String> ues, ArrayList<Character> qciListAllowdInTest,
			TransmitDirection transmitDirection,Integer runTime) throws Exception{
		trafficGenerator.initStreams(protocol, ues, qciListAllowdInTest, transmitDirection,runTime);
	}
	
	public boolean startTraffic(TrafficCapacity capacity) throws Exception {
		
		if (!init()) {
			return false;
		}
		
		switch(capacity){
		case FULLTPT:
			return fullTPTStart();
		
		case LOWTPT:
				//in the case that not TPT test - file loading causing problems.
			ArrayList<String> uesNames = convertUeToNamesList(SetupUtils.getInstance().getAllUEs());
			ArrayList<Character> qci = new ArrayList<>();
			qci.add(new Character('9'));
			disableUnneededStreams(Protocol.UDP,uesNames,qci);
			Double loadDL = new Double(10);
			Double loadUL = new Double(1);
			Pair<Double,Double> loadPair = new Pair<Double,Double>(loadDL,loadUL);
			trafficLoadSet(loadPair);
			subscribeToSTCFile();
			startArpNd("DL", "UL");
			startTrafficSTC();
			return true;
			
		case CUSTOM:
				subscribeToSTCFile();
				startArpNd("DL", "UL");
				startTrafficSTC();
				return true;
		}
		
		return false;	
	}
	
	protected ArrayList<String> convertUeToNamesList(ArrayList<UE> ueList2) {
		ArrayList<String> ueList = new ArrayList<String>();
		for (UE ue : ueList2) {
			ueList.add("UE" + ue.getName().replaceAll("\\D+", "").trim());
		}
		return ueList;
	}

	private boolean fullTPTStart() throws Exception{
		
		
		switch (generatorType) {
		case TestCenter:
			startTrafficSTC();
			streamsAdapter = initActiveStreams();
			try {
				activeStreamsArray = turnListsIntoStringArray(streamsAdapter.UlStreams, streamsAdapter.DlStreams);
			} catch (TrafficException e) {
				e.printStackTrace();
			}
			updateStreamsAccordingToActiveStreamArray();
			GeneralUtils.printToConsole("subscribing to STC counters for later use...");
			subscribeToSTCFile();
			break;
		case ITraffic:
			startTrafficSTC();
			GeneralUtils.printToConsole("subscribing to STC counters for later use...");
			subscribeToSTCFile();
			startArpNd("DL", "UL");
			break;
		}

		GeneralUtils.printToConsole(String.format("traffic is running... wait %d seconds until ues will stabilize",
				TRAFFIC_STABLIZING_TIME / MILIS_IN_SEC));

		Thread.sleep(TRAFFIC_STABLIZING_TIME);
		return true;
	}
	/**
	 * FOR IPERF ONLY!
	 * 
	 * @param ues
	 * @return
	 * @throws Exception
	 */
	public boolean startTraffic(ArrayList<UE> ues) { /************** WILL BE DELETED IN NEXT MERGE **************/
		try {	
	
			switch (generatorType) {
			case IPERF:
				//iperf.startTraffic();
				break;
			default:
				return startTraffic();
			}
	
			GeneralUtils.printToConsole(String.format("traffic is running... wait %d seconds until ues will stabilize",
					TRAFFIC_STABLIZING_TIME / MILIS_IN_SEC));
	
			// Wait the traffic to stabilize before ending test, meanwhile printing
			GeneralUtils.unSafeSleep(TRAFFIC_STABLIZING_TIME);
		} catch (Exception e) {
			GeneralUtils.printToConsole(String.format("Could not start traffic! \n" + e.getMessage()));
			return false;
		}
		return true;
	}

	public void stopTrafficWithoutDisconnect() throws Exception{
		GeneralUtils.printToConsole("stopping traffic on all ports");
		trafficGenerator.StopTrafficOnAllPorts();
	}
	
	/**
	 * Switch Case : TestCenter - Stopping traffic. ITraffic stopTraffic on
	 * ports,release ports,disconnect.
	 * 
	 * @throws Exception
	 */
	public boolean stopTraffic() {
		try {
			switch (generatorType) {
			case TestCenter:
				tg.getTraffic().portStop(DL_PORT_NAME, UL_PORT_NAME);
				tg.getTraffic().disconnect();
				break;
			case ITraffic:
				GeneralUtils.printToConsole("stopping traffic on all ports");
				trafficGenerator.StopTrafficOnAllPorts();
				trafficGenerator.Disconnect();
				trafficGenerator.closeAllConnections();
				break;
			}
			GeneralUtils.printToConsole("Traffic been stopped");

		} catch (Exception e) {
			GeneralUtils.printToConsole(
					"Could not stop traffic! , please check if you have started to run traffic before stopping\n" + e);
			return false;
		}
		return true;
	}
	
	
	
	public void shutDown(){
		try{
			switch (generatorType) {
			case TestCenter:
				tg.stop();
				tg.close();
				break;
			case ITraffic:
				GeneralUtils.printToConsole("release all ports");
				GeneralUtils.printToConsole("Disconnecting");
				trafficGenerator.Disconnect();
				GeneralUtils.printToConsole("waiting 10 seconds");
				Thread.sleep(10 * 1000);
				if(uploadToReportConfigFile != null){
					Files.delete(Paths.get(uploadToReportConfigFile.getPath()));
					uploadToReportConfigFile = null;
				}
				break;
			}
		}
		catch(Exception e){
			e.printStackTrace();
			GeneralUtils.printToConsole("Shutting down STC had an Error.");
		}
	}

	/**
	 * Apply Changes to Stc starting ports UL and DL
	 * 
	 * @throws Exception
	 */
	public void startTrafficSTC() throws Exception {

		switch (generatorType) {
		case TestCenter:
			tg.getTraffic().apply();
			try {
				tg.getTraffic().portStart(DL_PORT_NAME, UL_PORT_NAME);
			} catch (Exception e) {
				GeneralUtils.printToConsole("Could not start traffic!");
				Assert.fail(e.getMessage());
			}
			break;
		case ITraffic:
			trafficGenerator.StartTrafficOnAllPorts();
		}

	}

	/**
	 * Loading Config file relocating ports ITraffic dosn't have 3 trys
	 * 
	 * @param ports
	 * @throws Exception
	 */
	private boolean loadRelocate(int... ports) throws Exception {
		for (int i = 1; i <= 3; i++) {
			try {
				GeneralUtils.printToConsole("loading tcc file...");
				loadConfigFile();
				GeneralUtils.printToConsole("Starting to take control of the tcc ports");
				for (int port : ports) {
					relocatePort(port);
				}
				GeneralUtils.printToConsole("Ports are under control!");
				break;
			} catch (Exception e) {
				if (i >= 3 || generatorType == TrafficGeneratorType.ITraffic) {

					report.report("reset stc more then 3 times - Failing Test", Reporter.WARNING);
					e.printStackTrace();
					return false;
				}
				GeneralUtils.printToConsole(e.getMessage());
				e.printStackTrace();
				report.report("Try To Recover STC connection- from traffic error");
				tg.getTraffic().disconnect();
				tg.close();
				trafficInitSTCRecovery();
			}
		}
		return true;
	}

	private void trafficInitSTCRecovery() throws Exception {
		tg = (TestCenterEmulations) system.getSystemObject("gen");
		tg.setLifeTime(SystemObject.PERMANENT_LIFETIME);
		// getting ports from traffic generator
		downlinkPort = tg.getCustomerPorts()[DL_PORT_INDX];
		uplinkPort = tg.getCustomerPorts()[UL_PORT_INDX];
		initStcConfigFile();
		disableStcDebug();

	}

	/**
	 * relocating a single port int number
	 * 
	 * @param portNum
	 * @throws Exception
	 */
	public void relocatePort(int portNum) throws Exception {
		switch (generatorType) {
			case TestCenter:
	
				L23TrafficPortEmulations currentPort = tg.getPorts()[portNum];
				String name = "";
				// sets the port name according to the port number
				if (portNum == UL_PORT_INDX) {
					name = UL_PORT_NAME;
				} else // portNum=dlPortNum
				{
					name = DL_PORT_NAME;
				}
				GeneralUtils.printToConsole(String.format("setting up %s port ", name));
				currentPort.setPortName(name);
	
				// take control of the port
				for (int i = 1; i <= 3; i++) {
					try {
						currentPort.getTraffic().portReserveName(currentPort);
						GeneralUtils.printToConsole("Successfully reserved port " + name);
						break;
					} catch (Exception e) {
						report.report("Couldn't reserve port " + name + " for the " + i + " time.");
						if (i >= 3) {
							GeneralUtils.printToConsole("!!!!!!!------Methodicly PORTS RESET-----!!!!!!");
							throw new Exception("Couldn't reserve ports 3 times");
						}
					}
				}
				break;
			}
	}

	/**
	 * loading configuration file - saving it readObjectNames - getting instance
	 * of the STC Reconnecting
	 * 
	 * @throws Exception
	 */
	private void loadConfigFile() throws Exception {
		report.report("STC Config file name: " + configFile.getCanonicalPath());

		switch (generatorType) {
			case TestCenter:
				try {
					tg.getTraffic().loadConfigFile(configFile.getCanonicalPath());
					// fill all objects into tg
					GeneralUtils.printToConsole("readObjectNames");
					tg.getTraffic().readObjectNames();
					// reconnecting stc with tg
					GeneralUtils.printToConsole("Reconnect");
					((TestCenter) tg).getTraffic().reconnect();
				} catch (Exception e) {
					throw new Exception("Loading Config file Failed");
				}
				break;
		}

	}

	/**
	 * Deciding what to Subscribe to in the STC
	 */
	private void subscribeToSTCFile() {
		try {
			switch (generatorType) {
			case TestCenter:
				tg.getTraffic().subscribe("GeneratorPortResults");
				tg.getTraffic().subscribe("AnalyzerPortResults");
				tg.getTraffic().subscribe("TxStreamResults");
				tg.getTraffic().subscribe("RxStreamSummaryResults");
				tg.getTraffic().subscribe("RxStreamBlockResults");
				tg.getTraffic().subscribe("TxStreamBlockResults");
				break;
			case ITraffic:
				trafficGenerator.SubscribeToCounter(CounterUnit.BITS_PER_SECOND);
				break;
			}

		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("Could not subscribe to STC ,please make sure that traffic was started before this test");
		}

	}

	public boolean enableExtraCounter(CounterUnit counterUnit){
		try {
			switch (generatorType) {
				case ITraffic:
					return trafficGenerator.enableExtraCounter(counterUnit);
				
				default:
					return false;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * inits the PacketSize According to what getting. last modified 08/12/16
	 * 
	 * @author Shahaf Shuhamy
	 * @param frameSize
	 * @param streams
	 * @throws TrafficException
	 */
	public void initFrameSize(Integer frameSize, ArrayList<StreamParams> streams) throws TrafficException {
		fsResults.put(frameSize, new FrameSizeResult(frameSize));
		switch (generatorType) {
		case TestCenter:
			try {
				for (StcPort port : tg.getTraffic().getPorts(DL_PORT_NAME, UL_PORT_NAME)) {
					for (StcStream stream : port.getStreams().values()) {
						stream.configFrameSize(EnumStreamSize.STREAM_SIZE_FIXED, frameSize, -1, -1, -1);
					}
				}
				tg.getTraffic().apply();
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;

		case ITraffic:
			try {
				trafficGenerator.setFrameSize(frameSize);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void initNumberParallelStreams(Integer numberParallelStreams, ArrayList<StreamParams> streams) {
		switch (generatorType) {
		case ITraffic:
			trafficGenerator.setNumberParallelStreams(numberParallelStreams);
		}
	}
	
	public void initWindowSize(Double windowSize, ArrayList<StreamParams> streams) {
		switch (generatorType) {
		case ITraffic:
			trafficGenerator.setWindowSizeInKbs(windowSize);
		}
	}
	
	public boolean initProtocol(Protocol protocol) {
		switch (generatorType) {
		case TestCenter:
			if (protocol == Protocol.TCP) 
				return false;
			else
				return true;			
		case ITraffic:
			return trafficGenerator.setProtocol(protocol);
		default:
			report.report("unkonwn traffic generator type.",Reporter.WARNING);
			return false;			
		}
	}

	/**
	 * @author Shahaf Shuhamy checking if Single Stream is in the Test Streams
	 * @param stream
	 * @param ueNameListStc
	 * @return
	 * @throws TrafficException
	 */
	private boolean checkIfStreamNeedToBeActive(String name, ArrayList<String> ueNameListStc) throws TrafficException {
		// check find the stream in the streams list and check if its Forbidden
		// ueName = "UEXX"
		if(generatorType == TrafficGeneratorType.ITraffic){
			for (String ueName : ueNameListStc) {
				if(name.equals(ueName)){
					return true;
				}
			}
			return false;
		}
		
		for (String ueName : ueNameListStc) {
			if (name.contains(ueName)) {
				// DL_UE19 -> UE1
				// DL_UE109 -> UE10
				String nameWithoutQCi = name;
				int _index = nameWithoutQCi.indexOf("_");
				nameWithoutQCi = name.substring(_index + 1, name.length() - 1);
				if (nameWithoutQCi.equals(ueName)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * getting Active ports
	 * 
	 * @return
	 */
	protected ArrayList<String> getActivePorts() {
		// Returns an ARRAY containing the given dlPortName & ulPortName , in
		// order.
		ArrayList<String> a = new ArrayList<String>();
		a.add(DL_PORT_NAME);
		a.add(UL_PORT_NAME);
		GeneralUtils.printToConsole("arrayList from getActivePorts : " + Arrays.toString(a.toArray()));
		return a;
	}

	/**
	 * save file and port start with sleeping of 10 * 1000 millies
	 * 
	 * @throws Exception
	 */
	public void saveConfigFileAndStart() throws Exception {
		if (generatorType == TrafficGeneratorType.ITraffic) {
			startArpNd("DL", "UL");
			trafficGenerator.StartTrafficOnAllPorts();
			return;
		}
		if (firstTestFlag) {
			GeneralUtils.printToConsole("First Test Flag is true - saving Config File");
			tg.getTraffic().saveConfigFile();
		}
		report.report("Apply Changes to STC");
		tg.getTraffic().apply();
		report.report("Stop Ports");
		tg.getTraffic().portStop(DL_PORT_NAME, UL_PORT_NAME);
		report.report("waiting 10 seconds");
		Thread.sleep(10 * 1000);
		report.report("Start ports");
		tg.getTraffic().portStart(DL_PORT_NAME, UL_PORT_NAME);
		report.report("waiting 25 seconds");
		Thread.sleep(25 * 1000);

	}

	/**
	 * @author Shahaf Shuhamy read Statistics from 5 subscribes getting all
	 *         counters summaring rx ports adding to counters list
	 * @param frameSize
	 * @param portsMap
	 * @param debugPrinter
	 * @throws Exception
	 */
	public void sampleCurrentStatistics(Integer frameSize, HashMap<Long, StreamParams> portsMap, StreamList debugPrinter)
			throws Exception {
		counters = new HashMap<String, Long>();
		if (generatorType == TrafficGeneratorType.ITraffic) {
			trafficGenerator.readAllCounters();
			return;
		}
		readCounters(debugPrinter);
		if (checkSTCCurrentParams()) {
			// init a table to save each port in it
			for (String port : getActivePorts()) {
				try {
					long TxL1BitRateCounter = (long) generatorResults.getCounter(port, "L1BitRate");
					long RxL1BitRateCounter = (long) analyzerResults.getCounter(port, "L1BitRate");
					GeneralUtils.printToConsole("TxL1BitRateCounter = " + TxL1BitRateCounter + " / RxL1BitRateCounter = "
							+ RxL1BitRateCounter);
					String portName = port.equals(DL_PORT_NAME) ? UL_PORT_NAME : DL_PORT_NAME;
					addParamsToPortList(portsMap, TxL1BitRateCounter, RxL1BitRateCounter, portName);
					counters.put("TxL1BitRate", TxL1BitRateCounter);
					counters.put("RxL1BitRate", RxL1BitRateCounter);
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (Exception e) {
					System.err.println("Exception was catched while trying to pull Stc L1BitRate counters");
					e.printStackTrace();
				}
				String portName = port.equals(DL_PORT_NAME) ? UL_PORT_NAME : DL_PORT_NAME;
				fsResults.get(frameSize).addSample(System.currentTimeMillis(), portName, counters);
			}
		}
	}

	private void readCounters(StreamList debugPrinter) throws Exception {
		switch (generatorType) {
		case TestCenter:
			GeneralUtils.printToConsole("Read Statistics from Gen,Analyzer,Tx and Rx");
			generatorResults = tg.getTraffic().readStatistics("GeneratorPortResults");
			analyzerResults = tg.getTraffic().readStatistics("AnalyzerPortResults");
			txStreamResults = tg.getTraffic().readStatistics("TxStreamResults");
			rxStreamResults = tg.getTraffic().readStatistics("RxStreamSummaryResults");
			rxStreamBlockResults = tg.getTraffic().readStatistics("RxStreamBlockResults");
			GeneralUtils.printToConsole("getAllCounters useing Debug Printer");
			getAllCounters(rxStreamBlockResults, debugPrinter, "RX");
		case ITraffic:
			GeneralUtils.printToConsole("Read Statistics From Itraffic Object, Tx and Rx");
			GeneralUtils.printToConsole("getAllCounters useing Debug Printer");

		}

	}

	/**
	 * adding sample of reading statistics to a Map which key is TimeStamp
	 * 
	 * @param portsMap
	 * @param txL1BitRateCounter
	 * @param rxL1BitRateCounter
	 * @param port
	 */
	private void addParamsToPortList(HashMap<Long, StreamParams> portsMap, long txL1BitRateCounter,
			long rxL1BitRateCounter, String port) {
		StreamParams tempParams = new StreamParams();
		tempParams.setRxRate(rxL1BitRateCounter);
		tempParams.setTxRate(txL1BitRateCounter);
		tempParams.setName(port);
		portsMap.put(System.currentTimeMillis(), tempParams);
	}
	
	public ArrayList<StreamParams> templateGetTxAndRxCounter(ArrayList<CounterUnit> counterUnitList, TransmitDirection transmitDirection){
		trafficGenerator.readAllCounters();
		ArrayList<ArrayList<StreamParams>> sampleArrayList = trafficGenerator.getActiveStreamCurrentSample(counterUnitList, transmitDirection);
		return sampleArrayList.get(0);
	}
	
	public ArrayList<StreamParams> getTxAndRxCounter(ArrayList<CounterUnit> counterUnitList){
		return templateGetTxAndRxCounter(counterUnitList, TransmitDirection.BOTH);
	}
	
	public ArrayList<StreamParams> getTxAndRxCounterDLOrUL(ArrayList<CounterUnit> counterUnitList, TransmitDirection transmitDirection) {
		return templateGetTxAndRxCounter(counterUnitList, transmitDirection);
	}

	/**
	 * sampling all of the streams and summing them up into a map
	 * 
	 * @author Shahaf Shuhamy
	 * @param frameSize
	 * @param streams
	 * @return 
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<ArrayList<StreamParams>> sampleForEachStream(Integer frameSize, ArrayList<StreamParams> streams) {
		if(generatorType == TrafficGeneratorType.ITraffic){
			ArrayList<CounterUnit> counterUnitList = new ArrayList<CounterUnit>();
			counterUnitList.add(CounterUnit.BITS_PER_SECOND);
			ArrayList<ArrayList<StreamParams>> sampleArrayList = trafficGenerator.getActiveStreamCurrentSample(counterUnitList, TransmitDirection.BOTH);
			for(ArrayList<StreamParams> sample : sampleArrayList){
				for(StreamParams streamParamIte : sample){
					counters.put("Tx"+CounterUnit.BITS_PER_SECOND.value, streamParamIte.getTxRate());
					counters.put("Rx"+CounterUnit.BITS_PER_SECOND.value, streamParamIte.getRxRate());
					
					streams.add(streamParamIte);
					fsResults.get(streamParamIte.getPacketSize()).addSample(streamParamIte.getTimeStamp(), streamParamIte.getName(), counters);
					
					GeneralUtils.printToConsole("StreamName="+streamParamIte.getName()+" / txStreamCounter=" + streamParamIte.getTxRate() +
												" / rxStreamCounter=" + streamParamIte.getRxRate());
				}
			}
			return sampleArrayList;
		}else{
			for (String stream : this.activeStreamsArray) {
				try {
					long txStreamCounter = GeneralUtils.ERROR_VALUE;
					long rxStreamCounter = GeneralUtils.ERROR_VALUE;
	
					switch (generatorType) {
					case TestCenter:
						txStreamCounter = (long) txStreamResults.getCounter(stream, "L1BitRate");
						rxStreamCounter = (long) rxStreamResults.getCounter(stream, "L1BitRate");
						break;
					}
					if (txStreamCounter != GeneralUtils.ERROR_VALUE && rxStreamCounter != GeneralUtils.ERROR_VALUE) {
						GeneralUtils.printToConsole(
								"txStreamCounter = " + txStreamCounter + " / rxStreamCounter = " + rxStreamCounter);
						counters.put("TxL1BitRate", txStreamCounter);
						counters.put("RxL1BitRate", rxStreamCounter);
						addStreamToList(streams, stream, txStreamCounter, rxStreamCounter);
						fsResults.get(frameSize).addSample(System.currentTimeMillis(), stream, counters);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	private void addStreamToList(ArrayList<StreamParams> streams, String streamName, long txStreamCounter,
			long rxStreamCounter) {
		StreamParams tempParams = new StreamParams();
		tempParams.setName(streamName);
		tempParams.setTimeStamp(System.currentTimeMillis());
		tempParams.setActive(true);
		tempParams.setRxRate(rxStreamCounter);
		tempParams.setTxRate(txStreamCounter);
		streams.add(tempParams);
	}

	/**
	 * return true if there are no streams under the threshold of 50Kb 
	 * @param streams
	 * 
	 * @param streamCountersRxTx2
	 * @throws IOException
	 * 
	 */
	public Boolean checkForHalt(long TEST_STREAM_HALT_PARAM, ArrayList<StreamParams> streams) {
		// sum every Streams from fsResults and Compare them to Calculator
		// check if the rx equals to the calculator
		for (StreamParams stream : streams) {
			if (stream.getRxRate() < TEST_STREAM_HALT_PARAM) {
				report.report("Stream Halt on : "+stream.getName());
				return true;
			}
		}
		return false;
	}

	/**
	 * function Checks which stream isn't according to number of ue ratio. in
	 * about 90%.
	 * 
	 * @param haltStreamParam
	 */
	public ArrayList<StreamParams> checkStreamsForHalter(int haltStreamParam, ArrayList<StreamParams> streams) {
		ArrayList<StreamParams> haltedStreams = new ArrayList<StreamParams>();
		for (StreamParams stream : streams) {
			if (stream.getRxRate() < haltStreamParam) {
				haltedStreams.add(stream);
			}
		}
		return haltedStreams;
	}

	/**
	 * check if STC params are correct diffrent then null or 0
	 * 
	 * @return
	 * @throws Exception
	 */
	private Boolean checkSTCCurrentParams() throws Exception {
		Boolean nullCheck = (generatorResults == null || analyzerResults == null || txStreamResults == null
				|| rxStreamResults == null) ? true : false;
		Boolean zeroSizeCheck = (generatorResults.getNames().size() == 0 || analyzerResults.getNames().size() == 0
				|| txStreamResults.getNames().size() == 0 || rxStreamResults.getNames().size() == 0) ? true : false;
		if (zeroSizeCheck || nullCheck) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * updating streams list according to activeStreamArray
	 */
	private void updateStreamsAccordingToActiveStreamArray() {
		String[] sp = new String[activeStreamsArray.size()];
		for (int i = 0; i < activeStreamsArray.size(); i++) {
			sp[i] = new String(activeStreamsArray.get(i));
		}
		setStreams(sp);
	}

	public void setStreams(String[] streams) {
		this.streams = streams;
	}

	private void configStreams() throws Exception {
		// config streams
		for (StreamParams stream : getStreams()) {
			switch (generatorType) {
			case TestCenter:
				tg.getTraffic().getObject(stream.getName()).configActive(stream.isActive());
				break;
			case ITraffic:
				return;
			}
		}

	}

	private StreamParams[] getStreams() {
		return activeStreams;
	}

	/**
	 * getting streams from stc and pasring them into more comfortable objects
	 * 
	 * @return StcStreamULDLAdapter object
	 * @throws Exception
	 */
	public StcStreamULDLAdapter initActiveStreams() throws Exception {
		switch (generatorType) {
		case TestCenter:
			// TestCenterTrafficEmulations l= tg.getTraffic();
			StcStreamULDLAdapter streamAdapter = new StcStreamULDLAdapter();
			HashMap<Integer, StcStream> dlMapStreams = new HashMap<Integer, StcStream>();
			HashMap<Integer, StcStream> ulMapStream = new HashMap<Integer, StcStream>();
			tg.getTraffic().readObjectNames();
			// add prints to output
			// tg.getTraffic().printName2object();
			GeneralUtils.printToConsole("parsing Streams To String Array");
			ArrayList<StcPort> J = tg.getTraffic().getPorts(DL_PORT_NAME);
			ArrayList<StcPort> k = tg.getTraffic().getPorts(UL_PORT_NAME);
			// getting streams for each port
			dlMapStreams = J.get(0).getStreams();
			ulMapStream = k.get(0).getStreams();
			for (StcStream stream : dlMapStreams.values()) {
				streamAdapter.DlStreams.add(stream);
			}
			for (StcStream stream : ulMapStream.values()) {
				streamAdapter.UlStreams.add(stream);
			}
			return streamAdapter;
		}
		return streamsAdapter;
	}

	/**
	 * giving stream their Names in known Format
	 * 
	 * @param ulStreams
	 * @param dlStreams
	 * @return
	 * @throws TrafficException
	 */
	public ArrayList<String> turnListsIntoStringArray(ArrayList<StcStream> ulStreams, ArrayList<StcStream> dlStreams)
			throws TrafficException {
		ArrayList<String> streams = new ArrayList<String>();;
		for(StcStream stc : ulStreams){
			streams.add(stc.getName());
		}
		for(StcStream stc : dlStreams){
			streams.add(stc.getName());
		}
		activeStreams = new StreamParams[streams.size()];
		int i = 0;
		for (String streamName : streams) {
			activeStreams[i] = new StreamParams(streamName, true);
			i++;
		}
		return streams;
	}

	public void setactiveStreamsArray(ArrayList<String> streamsAdapter) {
		if(streamsAdapter!=null){
			this.activeStreamsArray = new ArrayList<String>();
			this.activeStreamsArray.addAll(streamsAdapter);			
		}
	}
	
	public void addEnabledStreams(ArrayList<String> streamsToAdd){
		for(String stream : streamsToAdd){
			if(!this.activeStreamsArray.contains(stream)){
				this.activeStreamsArray.add(stream);
			}
		}
	}
	
	/**
	 * unsing Reflection in order to get Streams Counters for Debug Tables.
	 * 
	 * @author Hen Goldburd & Shahaf Shuhamy
	 * @param statView
	 * @param debugPrinter
	 * @param prefix
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	@SuppressWarnings("unchecked")
	private void getAllCounters(StcStatisticsView statView, StreamList debugPrinter, String prefix)
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		if (generatorType == TrafficGeneratorType.ITraffic) {
			return;
		}
		GeneralUtils.printToConsole("getAllCounter(Reflection) method");
		SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm:ss");
		Date resultDate = new Date(System.currentTimeMillis());

		Field resultsField = statView.getClass().getDeclaredField("results"); // NoSuchFieldException
		resultsField.setAccessible(true);
		HashMap<String, StcObjectResults> rxStreamResultsFullMap = (HashMap<String, StcObjectResults>) resultsField
				.get(statView);
		for (Entry<String, StcObjectResults> val : rxStreamResultsFullMap.entrySet()) {
			if (val == null) {
				continue;
			}
			String steamName = val.getKey();
			StcObjectResults values = val.getValue();

			Field listOfCaptionsField = values.getClass().getDeclaredField("captions"); // NoSuchFieldException
			listOfCaptionsField.setAccessible(true);
			ArrayList<String> listOfCaptions = (ArrayList<String>) listOfCaptionsField.get(values);
			ArrayList<String> listOfValues = new ArrayList<String>();
			for (String counterName : listOfCaptions) {
				if (counterName == null) {
					continue;
				} else {
					listOfValues.add(String.valueOf(statView.getCounter(steamName, counterName)));
				}
			}
			debugPrinter.addValues(steamName, sdf.format(resultDate), listOfCaptions, listOfValues);
		}
	}

	/**
	 * updating "streams" object in Test according to the fsResults object
	 * 
	 * @param ArrayList
	 *            streams
	 * @author Shahaf Shuhamy
	 */
	public void getStreamsParse(ArrayList<StreamParams> streams) {
		for (Integer frameSize : fsResults.keySet()) {
			// inits name of Stream and true for is active
			StreamParams tempStream = new StreamParams(fsResults.get(frameSize).getStreamName(), true);
			// init frameSize
			tempStream.setPacketSize(frameSize);
			// init TxL1Rate, RxL1BitRate
			tempStream.setRxRate(fsResults.get(frameSize).counterStats.get("RxL1BitRate").getCurrentRate());
			tempStream.setTxRate(fsResults.get(frameSize).counterStats.get("TxL1BitRate").getCurrentRate());
			// init time Stamp
			tempStream.setTimeStamp(fsResults.get(frameSize).getTimeStamp());
			// init recovery as first value means if it comes to handle then
			// first reboot
			//tempStream.setRecoveryStatus(recoveryStates.streamFirst);
			streams.add(tempStream);
		}

	}

	/**
	 * summing rx counters from list of StreamParams
	 * 
	 * @author Shahaf Shuhamy
	 * @param streams
	 * @param string
	 * @return
	 */
	public long sumUpStreamRx(ArrayList<StreamParams> streams, String string) {
		long rxRate = 0;
		for (StreamParams stream : streams) {
			if (stream.getName().contains(string)) {
				rxRate += stream.getRxRate();
			}
		}
		return rxRate;
	}

	public TestCenterEmulations getTg() {
		return tg;
	}

	/**
	 * disable Unneeded Stream from StreamList before the test is starting
	 * per QCI.
	 * 
	 * @author Hen Goldburd & Shahaf Shuhamy
	 * @param uesNameAllowdInTest
	 * @param streams2
	 * @param qci
	 */
	//uesNameAllowd ={UE1,UE10,UE3}
	public void disableUnneededStreams(Protocol protocol,ArrayList<String> uesNameAllowdInTest,ArrayList<Character> qci) throws Exception {
		ArrayList<String> disabledStreamsList = new ArrayList<String>();
		ArrayList<String> enabledStreamsList = new ArrayList<String>();
		ArrayList<String> arrayAfterParseActiveStreams = new ArrayList<String>();
		
		if(uesNameAllowdInTest.size() == 0){
			report.report("there are no UEs in Test!",Reporter.FAIL);
		}
		
		if (generatorType == TrafficGeneratorType.ITraffic) {
			ArrayList<ArrayList<String>> returnedArray = 
					trafficGenerator.enableStreamsByUeNameQciAndPortDirectionAndDisableTheRest(protocol,uesNameAllowdInTest,qci, TransmitDirection.BOTH);
			enabledStreamsList.addAll(returnedArray.get(0));
			disabledStreamsList.addAll(returnedArray.get(1));
			setactiveStreamsArray(returnedArray.get(0));
		}
		
		if(generatorType == TrafficGeneratorType.TestCenter){
			try {
				configStreams();
			} catch (Exception e) {
				e.printStackTrace();
			}
			// Check for UEs List and modify number of UEs
			try {
				report.report("Disabling Un-needed UEs and QCIs");
				for (StcPort port : tg.getTraffic().getPorts(DL_PORT_NAME, UL_PORT_NAME)) {
					for (StcStream stream : port.getStreams().values()) {
						if (!checkIfStreamNeedToBeActive(stream.getName(), uesNameAllowdInTest)) {
							// UE is not in test
							disabledStreamsList.add(stream.getName());
							stream.configActive(false);
						} else {
							// QCI is not in Test
							if (qci.size() != 0) {
								if (!qci.contains(stream.getName().charAt(stream.getName().length() - 1))) {
									disabledStreamsList.add(stream.getName());
									stream.configActive(false);
								} else {
									enabledStreamsList.add(stream.getName());
									stream.configActive(true);
									arrayAfterParseActiveStreams.add(stream.getName());
								}
							}
						}
					}
				}
			
				setactiveStreamsArray(arrayAfterParseActiveStreams);
				tg.getTraffic().apply();
			} catch (Exception e) {
				e.printStackTrace();
				throw new Exception("Exception in Unneeded streams method");
			}
		}
		report.startLevel("Disable streams");
		reportArrayList(disabledStreamsList);
		report.stopLevel();
		report.startLevel("Enabled Streams");
		reportArrayList(enabledStreamsList);
		report.stopLevel();

	}

	
	public void disableDLStreamsByNameAndQci(Protocol protocol, ArrayList<String> ues,ArrayList<Character> qci) throws Exception{
		ArrayList<String> disabledStreamsList = new ArrayList<String>();
		if (generatorType == TrafficGeneratorType.ITraffic) {
			report.report("Disabling wanted UEs and QCIs from DL port");
			disabledStreamsList = trafficGenerator.disableStreamsByUeNameQciAndPortDirectionAndEnableTheRest(protocol, ues, qci, TransmitDirection.DL).get(1);
		}
		
		if(generatorType == TrafficGeneratorType.TestCenter){
			try {
				configStreams();
			} catch (Exception e) {
				e.printStackTrace();
			}
			// Check for UEs List and modify number of UEs
			try {
				report.report("Disabling Un-needed UEs and QCIs");
				for (StcPort port : tg.getTraffic().getPorts(DL_PORT_NAME, UL_PORT_NAME)) {
					for (StcStream stream : port.getStreams().values()) {
						if(stream.getName().contains("DL") && checkIfStreamNeedToBeActive(stream.getName(), ues) && qci.contains(stream.getName().charAt(stream.getName().length() - 1))){
							disabledStreamsList.add(stream.getName());
							GeneralUtils.printToConsole("Stream : "+stream.getName()+" Disable");
							stream.configActive(true);
						}
					}
				}
				tg.getTraffic().apply();
			} catch (Exception e) {
				e.printStackTrace();
				throw new Exception("Exception in enable needed streams method");
			}
		}
		report.startLevel("New disabled Streams");
		reportArrayList(disabledStreamsList);
		report.stopLevel();
	}
	
	// This function ONLY enables stream and not disabled not needed ones
	public void enableStreamsByNameAndQciAndDirection(Protocol protocol,ArrayList<String> ues,ArrayList<Character> qci,
			TransmitDirection direction) throws Exception {
		ArrayList<String> enabledStreamsList = new ArrayList<String>();
		
		if (generatorType == TrafficGeneratorType.ITraffic) {
			ArrayList<String> returnedArray = 
					trafficGenerator.enableStreamsByUeNameQciAndPortDirection(protocol,ues,qci,direction);
			enabledStreamsList.addAll(returnedArray);
			addEnabledStreams(returnedArray);
		}
		
		if(generatorType == TrafficGeneratorType.TestCenter){
			try {
				configStreams();
			} catch (Exception e) {
				e.printStackTrace();
			}
			// Check for UEs List and modify number of UEs
			try {
				report.report("Disabling Un-needed UEs and QCIs");
				for (StcPort port : tg.getTraffic().getPorts(DL_PORT_NAME, UL_PORT_NAME)) {
					for (StcStream stream : port.getStreams().values()) {
						if(checkIfStreamNeedToBeActive(stream.getName(), ues) && qci.contains(stream.getName().charAt(stream.getName().length() - 1))){
							enabledStreamsList.add(stream.getName());
							GeneralUtils.printToConsole("Stream : "+stream.getName()+" Enable");
							stream.configActive(true);
						}
					}
				}
				addEnabledStreams(enabledStreamsList);
				tg.getTraffic().apply();
			} catch (Exception e) {
				e.printStackTrace();
				throw new Exception("Exception in enable needed streams method");
			}
		}
		report.startLevel("New Enabled Streams");
		reportArrayList(enabledStreamsList);
		report.stopLevel();
	}
	
	/**
	 * prints ArrayList<String> with Report
	 * 
	 * @param stringArray
	 */
	private void reportArrayList(ArrayList<String> stringArray) {
		for (String string : stringArray) {
			report.report(string);
		}
	}

	public static void pingToUEs(ArrayList<UE> ueList) throws IOException, InterruptedException {
		executorService = Executors.newFixedThreadPool(ueList.size());
		pingToUEList = new ArrayList<>();
		int iterator = 0;
		for (UE ue : ueList) {
			iterator++;
			Runnable pingToUE = new PingToUE(ue, iterator);
			pingToUEList.add((PingToUE) pingToUE);
			executorService.execute(pingToUE);
		}
		GeneralUtils.printToConsole("Waiting 10 sec for UEs to stabilize");
		Thread.sleep(10000);
	}

	/**
	 * decides config File type according to Inner enum type : HO/regular.
	 * @param trafficType
	 * @return
	 */
	public boolean setTrafficType(TrafficType trafficType){ 
		switch(generatorType){
		case ITraffic:
			try{
				if(trafficType == TrafficType.HO){
					String trafficFile = trafficGenerator.getDefultConfigFile();
					String fileWithHO = trafficGenerator.setConfigFileWithHOPrefix(trafficFile);
					configFile = new File(fileWithHO);
					trafficGenerator.setCurrentConfigFile(fileWithHO);
				}
			}catch(Exception e){
				e.printStackTrace();
			}
			break;
			
		case TestCenter:
			try{
				//this part havnt been checked.
				if(trafficType == TrafficType.HO){
					String trafficFile = tg.getDefaultConfigTccFile();
					setConfigFileWithHOPrefix(trafficFile);
				}
				
			}catch(Exception d){
				d.printStackTrace();
			}
		}
			
		
		return false;
	}
	
	private void setConfigFileWithHOPrefix(String TrafficFile){
		int lastDot = TrafficFile.lastIndexOf('.');
		String HOTrafficFile = TrafficFile.substring(0, lastDot) + "_HO" + TrafficFile.substring(lastDot);
		configFile = new File(HOTrafficFile);
	}

	public static void stopPinging() throws Exception {
		for (PingToUE pingToUE : pingToUEList) {
			pingToUE.end();
		}
		executorService.shutdown();
	}

	public TrafficGeneratorType getGeneratorType() {
		return generatorType;
	}

	public void setGeneratorType(TrafficGeneratorType generatorType) {
		this.generatorType = generatorType;
	}

	public void startArpNd(String... names) throws Exception {
		switch(generatorType){
		case ITraffic:
			{
				GeneralUtils.startLevel("Arp Nd");
				
				try{
					if(trafficGenerator.arpNdStart()){
						report.report("Arp Was Successful!");
					}else{
						report.report("Arp Failed!!");
					}
				}catch(Exception e){
					e.printStackTrace();
					report.report("Arp process had Error");
				}
				
				GeneralUtils.unSafeSleep(3 * 1000);
				printArpInfo();
				GeneralUtils.stopLevel();
				return;
			}
			
		case TestCenter:
			{
				report.report("Sending Cumulative ARP/ND");
				boolean result;
				StringBuilder sb = new StringBuilder();
				result = this.getTg().getTraffic().arpSend(names);
				if (!result) {
					report.report("Cumulative arp fail - Sendig Arps for each Stream.");
					for (String name : names) {
						sb.append("ARP cache for " + name + "\n");
						sb.append(Arrays.toString(this.getTg().getTraffic().arpGetCache(name)) + "\n");
					}
				} else {
					report.report("Cumulative ARP was Successful");
				}
			}
		}
	}
	
	private void printArpInfo() {
		GeneralUtils.startLevel("Arp info");
		trafficGenerator.printArpInfo();
		GeneralUtils.stopLevel();
	}

	/**
	 * @author Shahaf Shuhamy
	 * @return
	 */
	public boolean isTrafficItraffic(){
		return generatorType == TrafficGeneratorType.ITraffic ? true : false;
	}

	public boolean closeTraffic() {
		switch (generatorType) {
		case TestCenter:
			tg.close();
		case ITraffic:
			try {
				trafficGenerator.Disconnect();
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;

	}

	/**
	 * unMark streams in the param haltedStreams for Rest and for TestCenter.
	 * @param haltedStreams
	 * @throws Exception
	 */
	public void updateActiveStreams(ArrayList<StreamParams> haltedStreams) throws Exception {
		ArrayList<StreamParams> localHaltedStreams = new ArrayList<StreamParams>();
		int haltedStreamsOriginalSize = haltedStreams.size();
		switch (generatorType) {
		case TestCenter: 
			for (StcPort port : tg.getTraffic().getPorts(DL_PORT_NAME, UL_PORT_NAME)) {
				for (StcStream stream : port.getStreams().values()) {
					for (StreamParams haltStream : haltedStreams) {
						if (stream.getName().equals(haltStream.getName())) {
							localHaltedStreams.add(haltStream);
							stream.configActive(false);
							report.report("stream :" + stream.getName() + " has been deActivated",Reporter.WARNING);
						}
					}
					//minimize time - thinking about UE simulator
					if (localHaltedStreams.size() == haltedStreamsOriginalSize) {
						GeneralUtils.printToConsole("apply");
						removeHaltStreamFromActiveStreamArray(haltedStreams);
						tg.getTraffic().apply();
						return;
					}
				}
			}
			GeneralUtils.printToConsole("apply");
			report.report("Some UEs are not active anymore - test is with less UES",Reporter.WARNING);
			tg.getTraffic().apply();
			break;
		

		case ITraffic: 
			trafficGenerator.setActiveState(haltedStreams, false);
		break;
			
		default:
			report.report("updateActiveStreams method error - generatorType is not Itraffic/TestCenter");
			break;
	  }
	}

	private void removeHaltStreamFromActiveStreamArray(ArrayList<StreamParams> haltedStreams) {
		for(StreamParams stream : haltedStreams){
			this.activeStreamsArray.remove(stream.getName()); //= (String[]) ArrayUtils.removeElement(this.activeStreamsArray, stream.getName());
		}
	}
	
	public File getConfigFile(){
		return configFile;
	}

	/**
	 * gets current xml configuration for STC
	 * @return
	 */
	public File getUploadedConfigFile(){
		long time = System.currentTimeMillis();
		String filePrefix = GeneralUtils.simpleTimeFormat();
	switch(generatorType){
		case ITraffic:
			try{
				String file = trafficGenerator.getUploadedConfigFile();
				uploadToReportConfigFile = GeneralUtils.createFileWithContent("configFile"+filePrefix+".xml", file);
				break;
			}catch(Exception d){
				d.printStackTrace();
			}
				
		case TestCenter:
			     return configFile;
		}
	return uploadToReportConfigFile;
	}
	
	public void setGenType(TrafficType genType){
		this.genType = genType;
	}
	
	public TrafficType getGenType(){
		return genType;
	}
	
	 public enum TrafficType{
		 REGULAR("regular"),HO("ho");
		 
		 private String value;
		 
		 TrafficType(String v){
			 value = v;
		 }
		 
		 public String getValue(){
			 return value;
		 }
	}

	 /**
	  * setting Ul and Dl ports to new Load.
	  * Dl the First element and UL and is the second element
	  * @param trafficLoadPair
	  */
	public void trafficLoadSet(Pair<Double, Double> trafficLoadPair) {
		double Dl = trafficLoadPair.getElement0();
		double Ul = trafficLoadPair.getElement1();
		switch(generatorType){
			case ITraffic:
				if(!trafficGenerator.configPortLoad(Dl*1000,Ul*1000)){
					report.report("could not set port load to STC",Reporter.WARNING);
				}else{
					report.report("load port with the value: "+String.format("%.2f",Dl)+" Mbps to DownLink");
					report.report("load port with the value: "+String.format("%.2f",Ul)+" Mbps to UpLink");
				}
				break;
				
			case TestCenter:
				try{
				ArrayList<StcPort> ports = tg.getTraffic().getPorts();
				for(StcPort port : ports){
						if(port.getName().equals("UL")){
							port.getGenerator().configLoad(EnumStreamRate.STREAM_RATE_KPS, Ul * 1000);
							report.report("Changed UL port to transmit : "+Ul +"Mbps");
						}else{
							port.getGenerator().configLoad(EnumStreamRate.STREAM_RATE_KPS, Dl * 1000);
							report.report("Changed DL port to transmit : "+Dl +"Mbps");
						}
					}
				}catch(Exception e){
					e.printStackTrace();
					report.report("could not set port Load!",Reporter.WARNING);
					GeneralUtils.printToConsole("Exception in trafficLoadSet under TestCenter case");
				}
				break;
		}
	}
	
	public boolean isConnected(){
		boolean result = false;
		switch(generatorType){
		case ITraffic:
			result = trafficGenerator.isConnected();
			break;
		default:
			report.report("is connected is not umplemented for other Generator other than RestAPI");
			break;
		}
		return result;
	}
	
	public boolean clearStreamsResultsSTC(){
		report.report("Clear Streams Results");
		boolean flag = trafficGenerator.clearStreamsResults();
		if(flag == false) {
			report.report("Clear Streams Results failed", Reporter.WARNING);
		}
		return flag;
	}

	public void setLoadStreamDL(ArrayList<LoadParam> load){
		trafficGenerator.setLoadStreamDLPort(load);
	}

	public void setLoadStreamUL(ArrayList<LoadParam> load){
		trafficGenerator.setLoadStreamULPort(load);
	}

	public void setFrameSizeStreamDL(ArrayList<LoadParam> load) throws IOException{
		trafficGenerator.setFrameSizeStreamDLPort(load);
	}

	public void setFrameSizeStreamUL(ArrayList<LoadParam> load) throws IOException{
		trafficGenerator.setFrameSizeStreamULPort(load);
	}
	
	public void disableStreamsByUeNameQciAndPortDirectionAndEnableTheRest(Protocol protocol,ArrayList<String> ueNamesNotAllowdInTest,
			ArrayList<Character> qciListNotAllowdInTest, 
			TransmitDirection transmitDirection) {
		if(generatorType == TrafficGeneratorType.ITraffic){
			trafficGenerator.disableStreamsByUeNameQciAndPortDirectionAndEnableTheRest(protocol,ueNamesNotAllowdInTest,
					qciListNotAllowdInTest, transmitDirection);
		}
		
	}

	public void addCommandFilesToReport() {
		if(generatorType == TrafficGeneratorType.ITraffic){
			ArrayList<File> commandFiles = trafficGenerator.getCommandFiles();
			if (!commandFiles.isEmpty()) {
				GeneralUtils.startLevel("Command Files.");
				for (File commandFile : commandFiles) {
					File toUpload = new File(commandFile.getName());
					commandFile.renameTo(toUpload);
					try {
						ReporterHelper.copyFileToReporterAndAddLink(report, toUpload, toUpload.getName());
					} catch (Exception e) {
						GeneralUtils.printToConsole("FAIL to upload TP commands File: " + commandFile.getName());
						e.printStackTrace();
					}
				}
				GeneralUtils.stopLevel();
			}
		}
	}
	
	public void addResultFilesToReport(String tryNumber) {
		if(generatorType == TrafficGeneratorType.ITraffic){
			ArrayList<File> commandFiles = trafficGenerator.getCommandFiles();
			if (!commandFiles.isEmpty()) {
				GeneralUtils.startLevel("Command Files.");
				for (File commandFile : commandFiles) {
					File toUpload = new File(tryNumber + commandFile.getName());
					commandFile.renameTo(toUpload);
					try {
						ReporterHelper.copyFileToReporterAndAddLink(report, toUpload, toUpload.getName());
					} catch (Exception e) {
						GeneralUtils.printToConsole("FAIL to upload TP commands File: " + commandFile.getName());
						e.printStackTrace();
					}
				}
				GeneralUtils.stopLevel();
			}
			ArrayList<File> resultFiles = trafficGenerator.getResultFiles();
			if (!resultFiles.isEmpty()){
				GeneralUtils.startLevel("Result Files.");
				for(File resultFile : resultFiles){
					File toUpload = new File(tryNumber + resultFile.getName());
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
			
			ArrayList<File> transmitOutputFiles = trafficGenerator.getTransmitOutputFiles();
			if(!resultFiles.isEmpty()){
				GeneralUtils.startLevel("Transmit Output Files.");
				for(File transmitOutputFile : transmitOutputFiles){
					File toUpload = new File(tryNumber + transmitOutputFile.getName());
					transmitOutputFile.renameTo(toUpload);
					try {
						ReporterHelper.copyFileToReporterAndAddLink(report, toUpload, toUpload.getName());
					} catch (Exception e) {
						GeneralUtils.printToConsole("FAIL to upload TP Transmit Output File: " + transmitOutputFile.getName());
						e.printStackTrace();
					}
				}
				GeneralUtils.stopLevel();
			}
			
			trafficGenerator.resetIperfList();
		}
	}
	
	public ArrayList<UE> getUES(EnodeB enb){
		ArrayList<UE> staticUES = SetupUtils.getInstance().getStaticUEs(enb);
		ArrayList<UE> dynamicUES = SetupUtils.getInstance().getDynamicUEs();
		ArrayList<UE> tempUes = new ArrayList<>();
		if (staticUES != null)
			tempUes.addAll(staticUES);
		if (dynamicUES != null)
			tempUes.addAll(dynamicUES);
		return tempUes;
	}

	public void takeActionAfterFoundHaltStream() {
		if(generatorType == TrafficGeneratorType.ITraffic){
			trafficGenerator.takeActionAfterFoundHaltStream();
		}
	}
	
	public boolean setAllStreamsToState(boolean state){
		try {
			trafficGenerator.SetAllStreamActiveState(state);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public void stopTraffic(ArrayList<String> streamList) {
		trafficGenerator.stopTraffic(streamList);
	}

	public ArrayList<ArrayList<StreamParams>> getAllStreamsResults(ArrayList<String> streamList) {
		return trafficGenerator.getAllStreamsResults(streamList);
	}
}
package UeSimulator.Amarisoft;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import EnodeB.EnodeB;
import UE.AmarisoftUE;
import UE.UE;
import UeSimulator.Amarisoft.JsonObjects.Actions.UEAction;
import UeSimulator.Amarisoft.JsonObjects.Actions.UEAction.Actions;
import UeSimulator.Amarisoft.JsonObjects.ConfigFile.*;
import UeSimulator.Amarisoft.JsonObjects.Status.CellStatus;
import UeSimulator.Amarisoft.JsonObjects.Status.CellsWrapper;
import UeSimulator.Amarisoft.JsonObjects.Status.ConfigGet;
import UeSimulator.Amarisoft.JsonObjects.Status.UeAdd;
import UeSimulator.Amarisoft.JsonObjects.Status.UeStatus;
import Utils.GeneralUtils;
import Utils.TerminalUtils;
import jsystem.framework.report.ListenerstManager;
import jsystem.framework.report.Reporter;
import jsystem.framework.report.ReporterHelper;
import jsystem.framework.system.SystemManagerImpl;
import jsystem.framework.system.SystemObjectImpl;
import jsystem.utils.FileUtils;
import systemobject.terminal.SSH;
import systemobject.terminal.Terminal;

/**
 * ChatServer Client
 *
 * @author Jiji_Sasidharan
 */
@ClientEndpoint
public class AmariSoftServer extends SystemObjectImpl{

	private static AmariSoftServer instance;
	public static String amarisoftIdentifier = "amarisoft";
	private Reporter report = ListenerstManager.getInstance();

    private static Object waitLock = new Object();

	private Terminal lteUeTerminal;
	private Terminal lteUecommands;
	private String ip;
	private String port;
	private String userName;
	private String password;
	private String timingAdvance;
	private double txgain;
	private double rxgain;
	private String ueConfigFileName = "automationConfigFile";
	public ConfigObject configObject;
    private Session userSession = null;
    private MessageHandler messageHandler;
    private String[] sdrList;
    private String[] imsiStartList;
    private String[] imsiStopList;
    private Stack<String> dlMachineNetworks;
    public AmarisoftGroup[] UEgroup ;
    private ArrayList<AmarisoftUE> ueMap;
	private ArrayList<AmarisoftUE> unusedUEs;
    private HashMap<String, Integer> sdrCellsMap;
    volatile private Object returnValue;
	private String loggerBuffer="";
	private String loggerBufferForCommands="";
	private String cliBuffer = "";
	private String cliBufferForCommands = "";
	private Thread loggerBufferThread;
	private String logFileName = "AmarisoftLog";
	private boolean waitForResponse = false;
	private boolean connected = false;
	private boolean saveLogFile = false;
	private boolean running = false;
	private String pathPreConfig = null;
	private String currentConfigFile;

	@Override
	public void init() throws Exception {
		super.init();
		port = 900 + sdrList[0];
    	//connect();
    	ueMap = new ArrayList();
    	sdrCellsMap = new HashMap<>();
    	fillUeList();
    	if (UEgroup != null)
    		checkIfGroupsAreIdentical();
	}

	public void easyInit()
    {
    	ip="192.168.58.91";
    	userName="root";
    	password = "SWITswit";
    	port="9000";
    	connect();
    	ueMap = new ArrayList();
    	sdrCellsMap = new HashMap<>();
    	setImsiStartList("200010001008301");
    	setImsiStopList("200010001008400");
    	fillUeList();
    	if (UEgroup != null)
    		checkIfGroupsAreIdentical();
    }
	
    private void checkIfGroupsAreIdentical() {
		for(int i = 0; i < UEgroup.length; i++) {
			AmarisoftGroup group1 = UEgroup[i];
			 for(int j = i+1; j<UEgroup.length; j++) {
				 AmarisoftGroup group2 = UEgroup[j];
				 compareGroups(group1, group2);
			 }
		}
		
	}

	private void compareGroups(AmarisoftGroup group1, AmarisoftGroup group2) {
		for(Long imsi1: group1.getIMSIs()) {
			for(Long imsi2: group2.getIMSIs()) {
				if(imsi1 == imsi2)
					report.report("The imsi " + imsi1 + " is duplicate in 2 groups (" + group1.getGroupName()+ "," + group2.getGroupName());
			}
		}
		
	}
	
	private void fillUeList() {
		int ueId = 1;
		unusedUEs = new ArrayList<>();
		for (int i = 0; i < imsiStartList.length; i++) {
			Long startImsi = new Long(imsiStartList[i]);
			Long stopImsi = new Long(imsiStopList[i]);
			for (Long imsi = startImsi; imsi <= stopImsi ; imsi++) {
				ArrayList<String> groupName = new ArrayList<>();
				if (UEgroup != null) {
					for (int j = 0; j < UEgroup.length; j++) {
						if(checkIfImsiIsInGroup(imsi, UEgroup[j].getGroupName()))
							groupName.add(UEgroup[j].getGroupName());
					}
				}
				AmarisoftUE ue = new AmarisoftUE(ueId, groupName,  this);
				ue.setImsi(imsi+"");
				unusedUEs.add(ue);
				ueId++;
			}
		}
		if (UEgroup != null)
			checkGroupsValidation();
	}

	private boolean checkIfImsiIsInGroup(long imsi, String groupName) {
		for(AmarisoftGroup group: UEgroup) {
			if(group.getGroupName() == groupName) {
				for(Long IMSI : group.getIMSIs()) {
					if(IMSI == imsi) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	private void checkGroupsValidation() {	
		boolean imsiWasFound = false;
		for(AmarisoftGroup group: UEgroup) {		
			for(Long groupimsi : group.getIMSIs()) {
				imsiWasFound = false;
				for(AmarisoftUE amariUE: unusedUEs) {
					if(Long.parseLong(amariUE.getImsi()) == groupimsi) {
						imsiWasFound = true;
						break;
					}
						
				}
				if(!imsiWasFound)
					report.report("IMSI: " + groupimsi + " doesn't exists in the main imsi list in the SUT", Reporter.WARNING);
			}
		}
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}
	
	public String getusername() {
		return userName;
	}

	public void setuserName(String userName) {
		this.userName = userName;
	}
	
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getTimingAdvance() {
		return timingAdvance;
	}

	public void setTimingAdvance(String timingAdvance) {
		this.timingAdvance = timingAdvance;
	}
	public double getTxgain() {
		return txgain;
	}

	public void setTxgain(String txgain) {
		this.txgain = Double.valueOf(txgain);
	}

	public double getRxgain() {
		return rxgain;
	}

	public void setRxgain(String rxgain) {
		this.rxgain = Double.valueOf(rxgain);
	}
	
	public String[] getSdrList() {
		return sdrList;
	}

	public void setSdrList(String sdrList) {
		this.sdrList = sdrList.split(",");
	}
	
	public String[] getImsiStartList() {
		return imsiStartList;
	}

	public void setImsiStartList(String imsiStartList) {
		this.imsiStartList = imsiStartList.split(",");
	}
	
	public String[] getImsiStopList() {
		return imsiStopList;
	}

	public void setImsiStopList(String imsiStopList) {
		this.imsiStopList = imsiStopList.split(",");
	} 
    
	public Stack<String> getDlMachineNetworks() {
		return dlMachineNetworks;
	}

	public void setDlMachineNetworks(String dlMachineNetworks) {
		Stack<String> ips = new Stack<>();
		String[] rawDlMachineNetworks = dlMachineNetworks.split(",");
		for (int i = 0; i < rawDlMachineNetworks.length; i++) {
			String[] dlMachineNetwork = rawDlMachineNetworks[i].split("\\.");
			String network = String.join(".", Arrays.copyOf(dlMachineNetwork, dlMachineNetwork.length - 1));

			String range = dlMachineNetwork[3];
			if (range.contains("-")) {
				int rangeStart = Integer.parseInt(range.split("-")[0]);
				int rangeStop = Integer.parseInt(range.split("-")[1]);
				for (int j = rangeStart; j <= rangeStop; j++) {
					ips.add(network + "." + j);
				}
			}
			else
				ips.add(network + "." + range);			
		}
		
		
		this.dlMachineNetworks = ips;
	}

	/** getInstance of Amarisoft
	 * @return - Instance(
	 */
	public static AmariSoftServer getInstance()  {
		try {
			if (instance == null) {				
				instance = (AmariSoftServer) SystemManagerImpl.getInstance().getSystemObject("amariSoftServer");
			}
		} catch (Exception e) {
			ListenerstManager.getInstance().report("Amarisoft server SUT object is missing.");
			GeneralUtils.printToConsole(e.getMessage());
		}
		return instance;
	}
	
    public AmariSoftServer() { 
    }

    public boolean stopServer(){
    	boolean result = false;
		if (running) {
			sendCommands("quit", "#", lteUeTerminal, true);
			if (!sendCommands("ps -aux |grep lteue", "/root/ue/config/"+currentConfigFile, lteUecommands, false)) {
				running = false;
				result =  true;
			} else {
				report.report("Closing server failed.", Reporter.WARNING);
				running = true;
				result =  false;
			}
		}
		disconnectSession();
		return result;
    }
    
    private void disconnectSession() {
    	try {
			lteUecommands.disconnect();
			lteUeTerminal.disconnect();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public boolean startServer(EnodeB dut,String namePreConfig){
    	ArrayList<EnodeB> tempEnodebList = new ArrayList<>();
    	tempEnodebList.add(dut);
    	return startServer(tempEnodebList,namePreConfig);
    }
    
    public boolean startServer(ArrayList<EnodeB> duts,String namePreConfig){
    	connect();
    	pathPreConfig = namePreConfig;
    	if(pathPreConfig != null){
    		if(startServer(pathPreConfig)){
    			getEarfcnList(duts);
    			report.report("Using configuration file: "+pathPreConfig);
    			currentConfigFile = pathPreConfig;
    			return true;
    		}
    	}
    	setConfig(duts, Integer.parseInt(timingAdvance));
    	if(startServer(ueConfigFileName)){
    		report.report("Using configuration file: "+ueConfigFileName);
    		currentConfigFile = ueConfigFileName;
    		return true;
    	}
    	return false;
    }
    
    public boolean startServer(String configFile){
    	try {   
    		boolean ans = sendCommands("/root/ue/lteue /root/ue/config/" + configFile,"sample_rate=", lteUeTerminal, true);
    		if (!ans) {
    			GeneralUtils.printToConsole("Failed starting server with config file: " + configFile);
    			running = false;
    			return false;
			}
    		if(!sendCommands("ps -aux |grep lteue", " /root/ue/config/" + configFile, lteUecommands, false)) {
    			GeneralUtils.printToConsole("Failed starting server with config file: " + configFile);
    			running = false;
    			return false;
    		}
    		
        	URI endpointURI = new URI("ws://"+ip+":"+port);
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, endpointURI);
            startMessageHandler();
            running  = true;
        } catch (Exception e) {
        	GeneralUtils.printToConsole("Failed starting server with config file: " + configFile);
        	e.printStackTrace();
            return false;
        }
    	return true;
    }

	private void startMessageHandler() {
		addMessageHandler(new AmariSoftServer.MessageHandler() {
			public void handleMessage(String message) {
				synchronized (waitLock) {
					System.out.println("Message recieved: " + message);
					ObjectMapper mapper = new ObjectMapper();

					// Convert JSON string to Object
					if (message.contains("\"message\":\"ue_get\"")) {
						try {
							returnValue = mapper.readValue(message, UeStatus.class);
						} catch (JsonParseException e) {
							e.printStackTrace();
						} catch (JsonMappingException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}

					if (message.contains("\"message\":\"ue_add\"")) {
						try {
							returnValue = mapper.readValue(message, UeAdd.class);
						} catch (JsonParseException e) {
							e.printStackTrace();
						} catch (JsonMappingException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					
					if (message.contains("\"message\":\"config_get\"")) {
						try {
							returnValue = mapper.readValue(message, ConfigGet.class);////Object??
						} catch (JsonParseException e) {
							e.printStackTrace();
						} catch (JsonMappingException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					waitLock.notify();

				}
			}
		});
	}

	public void closeSocket() {
	}

	public boolean sendCommands(String cmd, String response, Terminal terminal, boolean isRunningTerminal) {
		String privateBuffer = "";
		String ans = "";
		cliBufferForCommands = "";
		if (!connected) {
			report.report("Attempted to send command \"" + cmd +"\" to machine that is not connected.", Reporter.WARNING);
			return false;
		}
		waitForResponse = true;
		sendRawCommand(cmd, terminal);
		long startTime = System.currentTimeMillis(); // fetch starting time
		while ((System.currentTimeMillis() - startTime) < 3000) {
			GeneralUtils.unSafeSleep(200);
			try {
				if (isRunningTerminal)
					privateBuffer += cliBuffer;
				else 
					privateBuffer += cliBufferForCommands;
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			ans += privateBuffer;
			if (ans.contains(response)){
				waitForResponse = false;
				return true;			
			}
			sendRawCommand("", terminal);
		}
		waitForResponse = false;
		return false;
	}
		
	public void sendRawCommand(String command, Terminal terminal){
		if (terminal == null) {
			return;
		}
		try {
			terminal.sendString(command + "\n", false);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
    
    /**
     * Callback hook for Connection open events.
     *
     * @param userSession the userSession which is opened.
     */
    @OnOpen
    public void onOpen(Session userSession) {
        System.out.println("opening websocket");
        this.userSession = userSession;
    }

    /**
     * Callback hook for Connection close events.
     *
     * @param userSession the userSession which is getting closed.
     * @param reason the reason for connection close
     * @throws IOException 
     */
    @OnClose
    public void onClose(Session userSession, CloseReason reason) throws IOException {
        System.out.println("closing websocket: " + reason.toString());
        running = false;
        this.userSession.close();
        this.userSession = null;
    }

    /**
     * Callback hook for Message Events. This method will be invoked when a client send a message.
     *
     * @param message The text message
     */
    @OnMessage
    public void onMessage(String message) {
        if (this.messageHandler != null) {
            this.messageHandler.handleMessage(message);
        }
    }

    /**
     * register message handler
     *
     * @param msgHandler
     */
    public void addMessageHandler(MessageHandler msgHandler) {
        this.messageHandler = msgHandler;
    }

    /**
     * Send a message.
     *
     * @param message
     */
    public void sendMessage(String message) {
    	try {
			this.userSession.getBasicRemote().sendText(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    /**
     * Message handler.
     *
     * @author Jiji_Sasidharan
     */
    public interface MessageHandler {

        void handleMessage(String message);
    }

    private void connect() { 
		this.lteUeTerminal = new SSH(ip, userName, password);
		this.lteUecommands = new SSH(ip, userName, password);
		try {
			this.lteUeTerminal.connect();
			this.lteUecommands.connect();
		} catch (IOException e) {
			e.printStackTrace();
			disconnectSession();
			return;
		}
		connected = true;
		startLogger();
	}
    
    public String[] processLines(String log) {	
		if (!log.trim().isEmpty()) {
			String[] lines = log.split("\n");
			Calendar receivedDate = Calendar.getInstance();
			String timestamp =  String.format("%02d/%02d/%04d %02d:%02d:%02d:%03d    ::    ", receivedDate.get(Calendar.DAY_OF_MONTH), receivedDate.get(Calendar.MONTH) + 1 /*because January=0*/, 
					receivedDate.get(Calendar.YEAR), receivedDate.get(Calendar.HOUR_OF_DAY), 
		            receivedDate.get(Calendar.MINUTE),receivedDate.get(Calendar.SECOND), 
					receivedDate.get(Calendar.MILLISECOND));
			for (int i = 0; i < lines.length; i++) {
				lines[i] = timestamp + lines[i];
			}
			return lines;
		}
			
		return new String[0];
	}

	public void startLogger() {	
		if (!connected) {
			GeneralUtils.printToConsole("Cant start Amarisoft log file when not connected.");
			return;
		}
		if (saveLogFile) {
			GeneralUtils.printToConsole("Cant start Amarisoft log file logging is already running.");
			return;
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				GeneralUtils.printToConsole("Starting Amarisoft log to file: " + logFileName);
				saveLogFile = true;
				while (saveLogFile) {
					try {
						String[] lines = processLines(getLoggerBuffer());
						BufferedWriter writer = new BufferedWriter(new FileWriter(logFileName, true));
						for (String logLine : lines) {
							if (logLine == null || logLine.length() == 0) {
								continue;
							}
							writer.append(logLine + "\n");
						}
						writer.close();
						GeneralUtils.unSafeSleep(50);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}, "Amarisoft log thread").start();
	}
	
	public void closeLog(){
		copyLogToCurrentTest();
		saveLogFile=false;
		GeneralUtils.unSafeSleep(200);
		FileUtils.deleteFile(logFileName);
	}
	
	public void copyLogToCurrentTest(){
		try {
			File logFile =  new File(logFileName);
			ReporterHelper.copyFileToReporterAndAddLink(report, logFile, logFile.getName());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private synchronized void readInputBuffer(Terminal terminal, boolean isRunningTerminal) {
		String privateBuffer = "";
		try {
			if (connected )
				privateBuffer += terminal.readInputBuffer().replaceAll("\r", "");
			else
				return;
		} catch (Exception e) {
			GeneralUtils.printToConsole("Failed to read from buffer " + e.getMessage());
			return;
		}

		privateBuffer = privateBuffer.replaceAll("[^[^\\p{Print}]\t\n]", "");
		int lastIndx = privateBuffer.lastIndexOf("\n");
		if (lastIndx > -1) {
			String buffer = privateBuffer.substring(0, lastIndx + 1);
			if (isRunningTerminal) {
				loggerBuffer +=buffer;
				if (waitForResponse) {
					cliBuffer += buffer;				
				}
				else
					cliBuffer = "";
			}
			else {
				loggerBufferForCommands +=buffer;
				if (waitForResponse) {
					cliBufferForCommands += buffer;				
				}
				else
					cliBufferForCommands = "";
			}
			
		}
	}
    
	private synchronized void readInputBuffer() {
		readInputBuffer(lteUeTerminal, true);
		readInputBuffer(lteUecommands, false);
	}
    private String getLoggerBuffer() {
		// To avoid the synchronized method readInputBuffer() - use a thread
		// that will call it and wait for it instead.
		if (connected && (loggerBufferThread == null || !loggerBufferThread.isAlive())) {
			loggerBufferThread = new Thread(new Runnable() {
				@Override
				public void run() {
					readInputBuffer();
				}
			}, getName() + " log buffer thread");
			loggerBufferThread.start();
		}

		String buffer = loggerBuffer;
		loggerBuffer = "";
		return TerminalUtils.filterVT100(buffer);
	}
    
    synchronized private Object sendSynchronizedMessage(String message)
	{
    	Object ans;
    	if (!running) {
			report.report("attpempted to send message \"" + message + "\" to amarisoft server that is not running." , Reporter.WARNING);
			return null;
		}
		synchronized (waitLock) {
			sendMessage(message);
			try {
				waitLock.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
				waitLock.notify();
			}
			ans = returnValue;
			returnValue = null;
		}
		
		return ans;
	}
    
	private void setConfigFile(String fileName) {
		ueConfigFileName = fileName;
	}
	
	private void writeConfigFile() {
		ObjectMapper mapper = new ObjectMapper();
    	mapper.configure(SerializationFeature.INDENT_OUTPUT, false);
    	try {    		
			String stat = mapper.writeValueAsString(configObject);
			GeneralUtils.printToConsole(stat);
			GeneralUtils.startLevel("server conectivity details");
				report.report("Port: " + configObject.getComAddr());
				report.report("Sdr number: " + configObject.getRfDriver().getArgs());
				report.report("TX gain: " + configObject.getTxGain());
				report.report("RX gain: " + configObject.getRxGain());
				report.report("Bandwidth: " + configObject.getBandwidth());
				report.report("Multi ue: " + configObject.getMultiUe());
				report.report("DL ertfcn: " + configObject.getCells().get(0).getDlEarfcn());
				report.report("Global timing advance: " + configObject.getCells().get(0).getGlobalTimingAdvance());
			GeneralUtils.stopLevel();
			String newStat = stat.replace("\"", "\\\"");
			sendCommands("echo \"" + newStat + "\" > /root/ue/config/" + ueConfigFileName,"", lteUeTerminal, true);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		
	}
	
	private Integer getCellId(EnodeB enodeB, int cellId) {
		Integer ans = sdrCellsMap.get(enodeB.getName() + "_" + cellId);
		if (ans == null) {
			GeneralUtils.printToConsole(enodeB.getName() + " with cellID " + cellId + " not found");
			return 0;
		}
		return ans;
	}
	
	public boolean addUes(String groupName, int release, int category, Boolean powerControlEnabled,
			String channelType, String speed, String direction,String position) {
		return addUes(groupName, release, category, 0, powerControlEnabled,
				channelType, speed, direction,position);
	}
	
	public boolean addUes(String groupName, int release, int category, EnodeB enodeB, int cellId, Boolean powerControlEnabled,
			String channelType, String speed, String direction,String position) {
		int amarisoftCellId = getCellId(enodeB, cellId);
		return addUes(groupName, release, category, amarisoftCellId, powerControlEnabled,
				channelType, speed, direction,position);
	}
	
	private boolean addUes(String groupName, int release, int category, int cellId, Boolean powerControlEnabled,
			String channelType, String speed, String direction,String position) {
		GeneralUtils.startLevel("Adding UEs to Amarisoft simulator from group " + groupName);
		boolean result = true;
		boolean atListOneUE = false;
			if (unusedUEs.size() <= 0) {
				report.report("Failed adding UE to simulator. There are no free UEs in amarisoft to add", Reporter.WARNING);
				return false;
			}
			if (groupName.equals("amarisoft")) {
				while (unusedUEs.size() > 0) {
					int ueId = unusedUEs.get(0).ueId;
					result = result && addUe(unusedUEs.get(0), release, category, ueId, cellId, powerControlEnabled,
							channelType, speed, direction,position);
				}
			}
			else {
				int i = 0;
				boolean wasAdded = false;
				while(i<unusedUEs.size()) {
					wasAdded = false;
					ArrayList<String> groups = unusedUEs.get(i).groupName;
					for(String group: groups) {
						if (group.equals(groupName)){
							int ueId = unusedUEs.get(i).ueId;
							result = result && addUe(unusedUEs.get(i), release, category, ueId, cellId, powerControlEnabled,
									channelType, speed, direction,position);
							wasAdded = true;
							atListOneUE = true;
						}	
					}
					if (!wasAdded)
						i++;
				}	
			}
			if (!atListOneUE)
				report.report("There are no ues in group " + groupName + " to add to amarisoft", Reporter.WARNING);
		GeneralUtils.stopLevel();
		return result;
	}

	public boolean addUes(int amount, int release, int category, EnodeB enodeB, int cellId, Boolean powerControlEnabled,
			String channelType, String speed, String direction,String position)
	{
		int amarisoftCellId = getCellId(enodeB, cellId);
		return addUes(amount, release, category, amarisoftCellId, powerControlEnabled,
				channelType, speed, direction,position);
	}

	public boolean addUes(int amount, int release, int category, Boolean powerControlEnabled,
			String channelType, String speed, String direction,String position)
	{
		return addUes(amount, release, category, 0, powerControlEnabled,
				channelType, speed, direction,position);
	}

	public boolean addUes(int amount, int release, int category, int cellId, Boolean powerControlEnabled,
			String channelType, String speed, String direction,String position)
	{
		GeneralUtils.startLevel("Adding " + amount + " UEs to Amarisoft simulator.");
		boolean result = true;
		for (int i = 0; i < amount; i++) {
			if (unusedUEs.size() <= 0) {
				report.report("Failed adding UE to simulator. " + i + " UEs were added out of " + amount + " requsted.", Reporter.WARNING);
				return false;
			}
			int ueId = unusedUEs.get(0).ueId;
			result = result && addUe(unusedUEs.get(0), release, category, ueId, cellId, powerControlEnabled,
					 channelType,  speed,  direction, position);
		}
		GeneralUtils.stopLevel();
		return result;
	}
	
	public boolean addUe(UE ue, int release, int category, int ueId, int cellId, Boolean powerControlEnabled,
			String channelType, String speed, String direction,String position)
	{
		ArrayList<UeList> ueLists = new ArrayList<UeList>();
		UeList ueProperties = new UeList();
		ueProperties.setAsRelease(release);
		ueProperties.setCellIndex(cellId);
		ueProperties.setUeCategory(category);
		ue.setUeCategory(category);
		ueProperties.setForcedCqi(15);
		ueProperties.setForcedRi(2);
		ueProperties.setSimAlgo("milenage");
		ueProperties.setImsi(ue.getImsi());
		ueProperties.setImeisv("1234567891234567");
		ueProperties.setK("5C95978B5E89488CB7DB44381E237809");
		ueProperties.setOp("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF");
		ueProperties.setTunSetupScript(".ue-ifup_auto");
		ueProperties.setUeId(ueId);
		if(powerControlEnabled){
			ueProperties.setPowerControlEnabled(true);			
		}
		if(channelType != null){
			Channel ch = new Channel();
			ch.setType(channelType);
			ueProperties.setChannel(ch);			
		}
		if(speed != null){
			ueProperties.setSpeed(Float.valueOf(speed));			
		}
		if(direction != null){
			ueProperties.setDirection(Float.valueOf(direction));			
		}
		if(position != null){
			String[] temp = position.split(",");
			if(temp.length == 2){
				try{
					float f1 = Float.valueOf(temp[0]);
					float f2 = Float.valueOf(temp[1]);
					List<Float> lst = new ArrayList<>();
					lst.add(f1);
					lst.add(f2);
					ueProperties.setPosition(lst);
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
		//ueList.setAdditionalProperty("ue_count", 5);
		ueLists.add(ueProperties);
		UEAction addUE = new UEAction();
		addUE.setMessage(Actions.UE_ADD);
		addUE.setUeList(ueLists);
		try {
			ObjectMapper mapper = new ObjectMapper();
			String message = mapper.writeValueAsString(addUE);
			GeneralUtils.printToConsole(message);
			Object ans = sendSynchronizedMessage(message);
			UeAdd ueAdd = (UeAdd)ans;
			List<String> info = ueAdd.getInfo();
			if (info.size() == 0) {
				report.report("Ue " + ueId + " added to simulator.");
			}
			else
			{
				report.report("Failed adding Ue " + ueId + " to simulator, reason: " + info.get(0), Reporter.WARNING);
				return false;
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}		
		
		//check that RSRP is not 0
		if(ue.getRSRP(ueId) == 0) {
			report.report("RSRP of ue: " + ueId + " is 0, check that enodeB is active and the UE can hear it", Reporter.WARNING);
		}
		
		try {
			ue.init();
		} catch (Exception e) {
			GeneralUtils.printToConsole("Failed to init ue.");
			e.printStackTrace();
		}
		
		AmarisoftUE amariUE = (AmarisoftUE)ue;
		amariUE.ueId = ueId;
		ueMap.add(amariUE);
		unusedUEs.remove(amariUE);
		return true;
	}
	public boolean deleteUE(int ueId)
	{
		ObjectMapper mapper = new ObjectMapper();
		UEAction getUE = new UEAction();
		getUE.setUeId(ueId);
		getUE.setMessage(Actions.UE_DELETE);
		try {
			sendSynchronizedMessage(mapper.writeValueAsString(getUE));
		} catch (JsonProcessingException e) {
			GeneralUtils.printToConsole("Failed to delete ue: " + ueId);
			GeneralUtils.printToConsole(e.getMessage());
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public boolean deleteUes(int amount, int UEId)
	{
		GeneralUtils.startLevel("deleting " + amount + " UEs from Amarisoft simulator.");
		boolean result = true;
		for (int i = 0; i < amount; i++) {
			 if(ueMap.size() <= 0) {
				report.report("Failed deleting UE from simulator. " + i + " UEs were deleted out of " + amount + " requsted.", Reporter.WARNING);
				return false;
			}
			
			report.report("Deleting UE : " + UEId+i);
			boolean deleteUEResult = deleteUE(UEId + i);
			if (deleteUEResult) {
				ueMap.remove(UEId + i);
				AmarisoftUE ue = new AmarisoftUE(UEId + i, null, this);
				unusedUEs.add(UEId + i, ue);
				unusedUEs.add(UEId + i, ue);
			}
			result = result && deleteUEResult;
		}
		GeneralUtils.stopLevel();
		return result;
	}
	
	//Deleting lastUEs from list according to given amount
	public boolean deleteUes(int amount)
	{
		GeneralUtils.startLevel("deleting " + amount + " UEs from Amarisoft simulator.");
		boolean result = true;
		int deletedAmount = 0;
		int ues = ueMap.size();
		for(int i = 0; i<ues; i++) {
			if (deletedAmount < amount) {
				int lastUE = ueMap.size()-1;
				AmarisoftUE tempue = ueMap.get(lastUE);
				if(deleteUE(tempue.ueId)) {
					ueMap.remove(lastUE);
					deletedAmount++;
					report.report("UE : " + tempue.ueId + " ( " + tempue.getImsi() + " ) was deleted");
					unusedUEs.add(tempue);
				}
				else {
					report.report("UE :" + tempue.ueId + " ( " + tempue.getImsi() + " ) haven't been deleted from ue simulator");
					result = false;
				}
			}
		}
		GeneralUtils.stopLevel();
		return result;
	}
	
	public boolean deleteUes(String groupName)
	{
		GeneralUtils.startLevel("deleting all UES in group: " + groupName + " from Amarisoft simulator.");
		boolean result = true;
		for(int i = 0; i< ueMap.size(); i++) {
			ArrayList<String> groups = ueMap.get(i).groupName;
			for(String group: groups) {
				if (group.equals(groupName)) {
					int ueNum = ueMap.get(i).ueId;
					if (deleteUE(ueNum)) {
						ueMap.remove(ueNum);
						AmarisoftUE ue = new AmarisoftUE(ueNum, null, this);
						report.report("UE : " + ueNum + " ( " + ue.getImsi() + " ) was deleted");
						unusedUEs.add(ue);
					}
					else {
						report.report("UE :" + ueMap.get(ueNum).getImsi() + " haven't been deleted from ue simulator");
						result = false;
						
					}
					ueNum++;
				}
			}
			
		}
		GeneralUtils.stopLevel();
		return result;
	}
	
	public boolean uePowerOn(int ueId)
	{
		UE ue = getUEbyUEID(ueMap, ueId);
		if (ue == null) {
			GeneralUtils.printToConsole("Cant turn on Ueid " + ueId + " because it does not exist on the simulator.");
			return false;
		}
		ObjectMapper mapper = new ObjectMapper();
		UEAction getUE = new UEAction();
		getUE.setUeId(ueId);
		getUE.setMessage(Actions.POWER_ON);
		try {
			sendSynchronizedMessage(mapper.writeValueAsString(getUE));
		} catch (JsonProcessingException e) {
			System.out.println("Failed uePowerOn to ue " + ueId);
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}		
		if (ue.getLanIpAddress() == null) {
			String ip = getIpAddress(ueId);
			if (ip != null) {
				ue.setLanIpAddress(ip);
				ue.setWanIpAddress(ip);
				if(dlMachineNetworks.size() == 1) 
					ue.setIPerfDlMachine(dlMachineNetworks.get(0));
				else {
					if(ue.getIPerfDlMachine() == null)
						ue.setIPerfDlMachine(dlMachineNetworks.pop());
				}
				ue.setIPerfUlMachine(ip);
			}	
		}
		return true;
	}	

	public ConfigGet getConfig() {
		ConfigGet config = null;
		try {
			ObjectMapper mapper = new ObjectMapper();
			UEAction getConfig = new UEAction();

			getConfig.setMessage(Actions.CONFIG_GET);
			GeneralUtils.printToConsole("sending config_ue");

			Object ans = sendSynchronizedMessage(mapper.writeValueAsString(getConfig));

			config = (ConfigGet) ans;
		} catch (JsonProcessingException e) {
			GeneralUtils.printToConsole("Failed config_ue");
			e.printStackTrace();
		}
		return config;

	}

	private CellStatus getCellsStatus(int cellIndex) {
		ArrayList<CellStatus> cells = getCellsStatus();
		CellStatus cell = null;
		if (cells.size() < cellIndex) {
			GeneralUtils.printToConsole("Cell " + cellIndex + " does not exist!");
			return null;
		}
		try {
			cell = cells.get(cellIndex);
		} catch (Exception e) {
			GeneralUtils.printToConsole("Failed getting cellStatus!");
		}
		return cell;
	}
	
	public ArrayList<CellStatus> getCellsStatus() {
		ConfigGet s = getConfig();
		CellsWrapper cellWrapper = s.getCells();
		ArrayList<CellStatus> cells = new ArrayList<>();
		if (cellWrapper.getCell0() != null) {
			cells.add(cellWrapper.getCell0());
		}
		if (cellWrapper.getCell1() != null) {
			cells.add(cellWrapper.getCell1());
		}
		if (cellWrapper.getCell2() != null) {
			cells.add(cellWrapper.getCell2());
		}
		if (cellWrapper.getCell3() != null) {
			cells.add(cellWrapper.getCell3());
		}
	
		for (CellStatus cellStatus : cells) {
			System.out.println(cellStatus.getDlEarfcn());
			System.out.println(cellStatus.getMode());
			System.out.println(cellStatus.getNRbDl());
			System.out.println(cellStatus.getNRbUl());
			System.out.println(cellStatus.getPci());
			System.out.println(cellStatus.getSpConfig());
			System.out.println(cellStatus.getUl_earfcnn());
			System.out.println(cellStatus.getUldlConfig());
		}
		
		return cells;	
	}
	
	private UeStatus getUeInfo(int ueId) {
		UeStatus ueStatus = null;
		try {
			ObjectMapper mapper = new ObjectMapper();
			UEAction getUE = new UEAction();
			getUE.setUeId(ueId);

			getUE.setMessage(Actions.UE_GET);
			GeneralUtils.printToConsole("sending get_ue " + ueId);

			Object ans = sendSynchronizedMessage(mapper.writeValueAsString(getUE));

			ueStatus = (UeStatus) ans;
		} catch (JsonProcessingException e) {
			GeneralUtils.printToConsole("Failed get_ue " + ueId);
			e.printStackTrace();
		}
		return ueStatus;

	}

	synchronized private String getIpAddress(int ueId) {
		String ueIp = null;
		long t = System.currentTimeMillis();
		long end = t + 10000;
		while (System.currentTimeMillis() < end) {
			UeStatus ueStatus = getUeInfo(ueId);
			if (ueStatus != null) {
				if (ueStatus.getUeList() != null && ueStatus.getUeList().size() > 0) {
					if (ueStatus.getUeList().get(0) != null) {
						ueIp = ueStatus.getUeList().get(0).getIp();
						if(ueIp == null){
							if(ueStatus.getUeList().get(0).getPdnList() != null){
								ueIp = ueStatus.getUeList().get(0).getPdnList().get(0).getIpv4();								
							}
						}
						GeneralUtils.printToConsole("Found IP " + ueIp);
					}
				}
			}
			if (ueIp != null) {
				GeneralUtils.printToConsole("Found IP, exiting while");
				break;
			}
			GeneralUtils.unSafeSleep(100);
		}
		return ueIp;
	}

	public boolean uePowerOff(int ueId)
	{
		ObjectMapper mapper = new ObjectMapper();
		UEAction getUE = new UEAction();
		getUE.setUeId(ueId);
		getUE.setMessage(Actions.POWER_OFF);
		try {
			sendSynchronizedMessage(mapper.writeValueAsString(getUE));
		} catch (JsonProcessingException e) {
			GeneralUtils.printToConsole("Failed uePowerOff to ue " + ueId);
			GeneralUtils.printToConsole(e.getMessage());
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private void setConfig(ArrayList<EnodeB> duts, int globalTime) {
		configObject = new ConfigObject();
		configObject.setLogOptions("all.level=none,all.max_size=0");
		configObject.setLogFilename("/tmp/ue0.log");
		configObject.setComAddr("0.0.0.0:"+port);
		ArrayList<Cell> cells = new ArrayList<Cell>();
		String rfDriver = "";
		ArrayList<Integer> earfcnList = getEarfcnList(duts);
		for (int i = 0; i < sdrList.length; i++) {
			Cell cell = new Cell();
			cell.setDlEarfcn(earfcnList.get(i));
			cell.setNAntennaDl(2);
			cell.setNAntennaUl(1);
			cell.setGlobalTimingAdvance(globalTime);
			GeneralUtils.printToConsole("Adding cell with earfcn: " + earfcnList.get(i) + " to run on sdr " + sdrList[i]);
			cells.add(cell);
			rfDriver += "dev"+i+"=/dev/sdr"+sdrList[i]+",";
		}
		int ind = rfDriver.lastIndexOf(",");
		System.out.println("rfDriver String before: " + rfDriver);		
		if( ind>=0 )
			rfDriver = new StringBuilder(rfDriver).replace(ind, ind+1,"").toString();
		System.out.println("rfDriver String after: " + rfDriver);

		configObject.setBandwidth(duts.get(0).getBandwidth().getBw());
		configObject.setCells(cells);
		configObject.setTxGain(txgain);
		configObject.setRxGain(rxgain);
		configObject.setMultiUe(true);
		configObject.setRfDriver(new RfDriver());
		configObject.getRfDriver().setName("sdr");
		configObject.getRfDriver().setArgs(rfDriver);
		setConfigFile(ueConfigFileName);
		writeConfigFile();
	}

	private ArrayList<Integer> getEarfcnList(ArrayList<EnodeB> duts) {
		ArrayList<Integer> earfcns = new ArrayList<>();
		int cellNum = 0;
		for (EnodeB enodeB : duts) {
			int numOfCells = enodeB.getNumberOfActiveCells();
			for (int j = 1; j <= numOfCells; j++) {
				enodeB.setCellContextNumber(j);
				earfcns.add(enodeB.getEarfcn());
				sdrCellsMap.put(enodeB.getName() + "_" + j, cellNum);
				cellNum++;
			}
			enodeB.setCellContextNumber(1);
		}
		
		return earfcns;
	}

	public ArrayList<UE> getUeList() {
		return new ArrayList<UE>(ueMap);
	}

	public String getUeStatus(int ueId) {
		String status = GeneralUtils.ERROR_VALUE + "";
		UeStatus ueStatus = getUeInfo(ueId);
		if (ueStatus != null) {
			if (ueStatus.getUeList() != null && ueStatus.getUeList().size() > 0) {
				if (ueStatus.getUeList().get(0) != null) {
					status = ueStatus.getUeList().get(0).getRrcState();
					GeneralUtils.printToConsole("Found status " + status);
				}
			}
		}
		return status;
	}
	
	public Double getUeRsrp(int ueId) {
		Double ans = (double) GeneralUtils.ERROR_VALUE;
		UeStatus ueStatus = getUeInfo(ueId);
		if (ueStatus != null) {
			if (ueStatus.getUeList() != null && ueStatus.getUeList().size() > 0) {
				if (ueStatus.getUeList().get(0) != null) {
					ans = ueStatus.getUeList().get(0).getRsrp();
					GeneralUtils.printToConsole("Found RSRP " + ans);
				}
			}
		}
		return ans;
	}
	
	public Double getUeUlFreq(int ueId) {
		Double ans = (double) GeneralUtils.ERROR_VALUE;
		UeStatus ueStatus = getUeInfo(ueId);
		if (ueStatus != null) {
			if (ueStatus.getUeList() != null && ueStatus.getUeList().size() > 0) {
				if (ueStatus.getUeList().get(0) != null) {
					ans = ueStatus.getUeList().get(0).getRsrp();
					GeneralUtils.printToConsole("Found RSRP " + ans);
				}
			}
		}
		return ans;
	}

	public String getVersion() {
		String ans = GeneralUtils.ERROR_VALUE+"";
		ConfigGet config = getConfig();
		if (config != null) {
			if (config.getVersion() != null) {				
				ans = config.getVersion();
			}
		}
		return ans;
	}

	public boolean isRunning() {
		return running;
	}

	public int getUeId(String IMSI) {
		for (AmarisoftUE ue: ueMap) {
			if (ue.getImsi() == IMSI) {
				return ue.ueId;
			}
		}
		GeneralUtils.printToConsole("Could not find UE with IMSI: " + IMSI);
		return 0;
	}

	private int getUeConnectedCellIndex(int ueId) {
		int ans = GeneralUtils.ERROR_VALUE;
		UeStatus ueStatus = getUeInfo(ueId);
		if (ueStatus != null) {
			if (ueStatus.getUeList() != null && ueStatus.getUeList().size() > 0) {
				if (ueStatus.getUeList().get(0) != null) {
					ans = ueStatus.getUeList().get(0).getCellIndex();
					GeneralUtils.printToConsole("Found cellIndex " + ans);
					return ans;
				}
			}
		}
		GeneralUtils.printToConsole("Cant find cellID for UE " + ueId);
		
		return GeneralUtils.ERROR_VALUE;
	}
	
	public int getUeConnectedPCI(int ueId) {
		int cellIndex = getUeConnectedCellIndex(ueId);		
		CellStatus cell = getCellsStatus(cellIndex);
		if (cell == null) {
			return GeneralUtils.ERROR_VALUE;
		}
		return cell.getPci();
	}

	public String getUeConnectedDuplexMode(int ueId) {
		int cellIndex = getUeConnectedCellIndex(ueId);		
		CellStatus cell = getCellsStatus(cellIndex);
		if (cell == null) {
			return GeneralUtils.ERROR_VALUE + "";
		}
		return cell.getMode();
	}

	public int getUeUlEarfcn(int ueId) {
		int cellIndex = getUeConnectedCellIndex(ueId);		
		CellStatus cell = getCellsStatus(cellIndex);
		if (cell == null) {
			return GeneralUtils.ERROR_VALUE;
		}
		return cell.getDlEarfcn();
	}

	public int getUeDlEarfcn(int ueId) {
		int cellIndex = getUeConnectedCellIndex(ueId);		
		CellStatus cell = getCellsStatus(cellIndex);
		if (cell == null) {
			return GeneralUtils.ERROR_VALUE;
		}
		return cell.getDlEarfcn();
	}

	public int getUeNumOfrb(int ueId) {
		int cellIndex = getUeConnectedCellIndex(ueId);		
		CellStatus cell = getCellsStatus(cellIndex);
		if (cell == null) {
			return GeneralUtils.ERROR_VALUE;
		}
		return cell.getNRbDl();
	}

	
	private AmarisoftUE getUEbyUEID(ArrayList<AmarisoftUE> ues, int UEID) {
		for(AmarisoftUE ue: ues) {
			if (ue.ueId == UEID)
				return ue;
		}
		return null;
	}
    public ArrayList<AmarisoftUE> getUeMap() {
		return ueMap;
	}
    public ArrayList<AmarisoftUE> getUnusedUEs() {
		return unusedUEs;
	}



    public boolean RRC_Reestablishment(int ueId) {
    	ObjectMapper mapper = new ObjectMapper();
		UEAction getUE = new UEAction();
		getUE.setUeId(ueId);
		getUE.setMessage(Actions.RRC_RESET);
		try {
			sendSynchronizedMessage(mapper.writeValueAsString(getUE));
		} catch (JsonProcessingException e) {
			GeneralUtils.printToConsole("Failed reestablish to ue " + ueId);
			GeneralUtils.printToConsole(e.getMessage());
			e.printStackTrace();
			return false;
		}
		return true;
    }

    public void startTrafficLogs() {
    	sendSynchronizedMessage("t g");
    }






	
}
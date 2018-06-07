package UeSimulator.Amarisoft;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

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
import UeSimulator.Amarisoft.JsonObjects.Status.UeStatus;
import Utils.GeneralUtils;
import jsystem.framework.report.Reporter;
import jsystem.framework.system.SystemManagerImpl;
import jsystem.framework.system.SystemObjectImpl;
import systemobject.terminal.SSH;
import systemobject.terminal.Terminal;

/**
 * ChatServer Client
 *
 * @author Jiji_Sasidharan
 */
@ClientEndpoint
public class AmariSoftServer extends SystemObjectImpl{

	public static String amarisoftIdentifier = "amarisoft";
	
    private static Object waitLock = new Object();

	private Terminal sshTerminal;
	private Terminal lteUeTerminal;
	private String ip;
	private String port;
	private String userName;
	private String password;
	private double txgain;
	private double rxgain;
	private String ueConfigFileName = "automationConfigFile";
	public ConfigObject configObject;
    private Session userSession = null;
    private MessageHandler messageHandler;
    private String[] sdrList;
    private String[] imsiStartList;
    private String[] imsiStopList;
    private String[] dlMachineNetworks;
    private HashMap<Integer,UE> ueMap;
    private HashMap<Integer,UE> unusedUes;
    volatile private Object returnValue;

    @Override
	public void init() throws Exception {
		super.init();
		port = 900 + sdrList[0];
    	connect();
    	ueMap = new HashMap<>();
    	fillUeList();
	}
	
	private void fillUeList() {
		int ueId = 2;// start at 2 because amarisoft must start with atleast 1 UE.
		unusedUes = new HashMap<>();
		for (int i = 0; i < imsiStartList.length; i++) {
			Long startImsi = new Long(imsiStartList[i]);
			Long stopImsi = new Long(imsiStopList[i]);
			for (Long imsi = startImsi; imsi <= stopImsi ; imsi++) {
				AmarisoftUE ue = new AmarisoftUE(ueId, this);
				ue.setImsi(imsi+"");
				unusedUes.put(ueId, ue);
				ueId++;
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
    
	public String[] getDlMachineNetworks() {
		return dlMachineNetworks;
	}

	public void setDlMachineNetworks(String dlMachineNetworks) {
		ArrayList<String> ips = new ArrayList<>();
		String[] rawDlMachineNetworks = dlMachineNetworks.split(",");
		for (int i = 0; i < rawDlMachineNetworks.length; i++) {
			String[] dlMachineNetwork = rawDlMachineNetworks[i].split(".");
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
		
		
		this.dlMachineNetworks = (String[]) ips.toArray();
	} 
    
	public static AmariSoftServer getInstance() throws Exception {
		AmariSoftServer ns = (AmariSoftServer) SystemManagerImpl.getInstance().getSystemObject("amariSoftServer");
		return ns;
	}
	
    public AmariSoftServer() { 
    }

    public boolean startServer(EnodeB dut){
    	ArrayList<EnodeB> tempEnodebList = new ArrayList<>();
    	tempEnodebList.add(dut);
    	return startServer(tempEnodebList);
    }
    
    public boolean startServer(ArrayList<EnodeB> duts){
    	setConfig(duts);
    	return startServer(ueConfigFileName);
    }
    
    public boolean startServer(String configFile){
    	try {   
    		boolean ans = sendCommands(lteUeTerminal, "/root/ue/lteue /root/ue/config/" + configFile,"(ue)");
    		if (!ans) {
    			System.out.println("Failed starting server with config file: " + configFile);
    			return false;
			}
        	URI endpointURI = new URI("ws://"+ip+":"+port);
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, endpointURI);
            System.out.println(container.getDefaultAsyncSendTimeout());
            System.out.println(container.getDefaultMaxBinaryMessageBufferSize());
            System.out.println(container.getDefaultMaxSessionIdleTimeout());
            System.out.println(container.getDefaultMaxTextMessageBufferSize());
            startMessageHandler();
        } catch (Exception e) {
            System.out.println("Failed starting server with config file: " + configFile);
            System.out.println(e.getMessage());
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
					UeStatus stat = null;
					try {
						returnValue = mapper.readValue(message, UeStatus.class);
						waitLock.notify();
					} catch (JsonParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (JsonMappingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
	}

	public void closeSocket() {
		
		
	}

	public boolean sendCommands(Terminal terminal, String cmd, String response) {
		String privateBuffer = "";
		String ans = "";
		sendRawCommand(terminal, cmd);
		long startTime = System.currentTimeMillis(); // fetch starting time
		while ((System.currentTimeMillis() - startTime) < 3000) {
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				privateBuffer += terminal.readInputBuffer();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			ans += privateBuffer;
			if (ans.contains(response))
				return true;			
		}

		return false;
	}
		
	public void sendRawCommand(Terminal terminal, String command){
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    /**
     * Message handler.
     *
     * @author Jiji_Sasidharan
     */
    public static interface MessageHandler {

        public void handleMessage(String message);
    }

    private void connect() { 
		this.sshTerminal = new SSH(ip, userName, password);
		this.lteUeTerminal = new SSH(ip, userName, password);
		try {
			this.sshTerminal.connect();
			this.lteUeTerminal.connect();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

    synchronized private Object sendSynchronizedMessage(String message)
	{
    	
		synchronized (waitLock) {
			sendMessage(message);
			try {
				waitLock.wait();
			} catch (InterruptedException e) {
			}
		}
		Object ans = returnValue;
		returnValue = null;
		return ans;
	}
    
	public void setConfigFile(String fileName) {
		ueConfigFileName = fileName;
	}
	
	public void writeConfigFile() {
		ObjectMapper mapper = new ObjectMapper();
    	mapper.configure(SerializationFeature.INDENT_OUTPUT, false);
    	try {    		
			String stat = mapper.writeValueAsString(configObject);
			String newStat = stat.replace("\"", "\\\"");
			sendCommands(sshTerminal ,"echo \"" + newStat + "\" > /root/ue/config/" + ueConfigFileName,"");
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		
	}

	public boolean addUes(int amount, int release, int category)
	{
		boolean result = true;
		for (int i = 0; i < amount; i++) {
			if (unusedUes.size() <= 0) {
				report.report("Failed adding UE to simulator. " + i + " UEs were added out of " + amount + " requsted.", Reporter.WARNING);
				return false;
			}
			Object[] keys = unusedUes.keySet().toArray();
			
			int a = (Integer)keys[0];
			result = result && addUe(unusedUes.get(a), release, category, a);
		}
		return result;
	}
	
	public boolean addUe(UE ue, int release, int category, int ueId)
	{
		ObjectMapper mapper = new ObjectMapper();
		ArrayList<UeList> ueLists = new ArrayList<UeList>();
		UeList ueProperties = new UeList();
		ueProperties.setAsRelease(release);
		ueProperties.setUeCategory(category);
		ueProperties.setForcedCqi(15);
		ueProperties.setForcedRi(2);
		ueProperties.setSimAlgo("milenage");
		ueProperties.setImsi(ue.getImsi());
		ueProperties.setImeisv("1234567891234567");
		ueProperties.setK("5C95978B5E89488CB7DB44381E237809");
		ueProperties.setOp("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF");
		ueProperties.setTunSetupScript("ue-ifup_TCP");
		ueProperties.setUeId(ueId);
		//ueList.setAdditionalProperty("ue_count", 5);
		ueLists.add(ueProperties);
		UEAction addUE = new UEAction();
		addUE.setMessage(Actions.UE_ADD);
		addUE.setUeList(ueLists);
		String message;
		try {
			message = mapper.writeValueAsString(addUE);
			sendSynchronizedMessage(message);	
		} catch (JsonProcessingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}		
	
		try {
			ue.init();
		} catch (Exception e) {
			GeneralUtils.printToConsole("Failed to init ue.");
			e.printStackTrace();
		}
		
		ueMap.put(ueId,ue);
		unusedUes.remove(ueId);
		return true;
	}
	
	public boolean uePowerOn(int ueId)
	{
		UE ue = ueMap.get(ueId);
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
			ue.setLanIpAddress(ip);
			ue.setWanIpAddress(ip);
			ue.setIPerfDlMachine("91.99.1.240");
			ue.setIPerfUlMachine(ip);
		}
		return true;
	}
	
	synchronized private String getIpAddress(int ueId) {
		String ueIp = null;
		try {			
			ObjectMapper mapper = new ObjectMapper();
			UEAction getUE = new UEAction();
			getUE.setUeId(ueId);
			
			getUE.setMessage(Actions.UE_GET);
			long t= System.currentTimeMillis();
			long end = t + 10000;
			while (System.currentTimeMillis() < end) {
				GeneralUtils.printToConsole("sending get_ue" + ueId);
				Object ans = sendSynchronizedMessage(mapper.writeValueAsString(getUE));
				UeStatus ueStatus = (UeStatus)ans;
				if (ueStatus != null) {
					if (ueStatus.getUeList() != null && ueStatus.getUeList().size() > 0) {
						if (ueStatus.getUeList().get(0) != null) {
							ueIp = ueStatus.getUeList().get(0).getIp();
							GeneralUtils.printToConsole("Found IP" + ueIp);
						}
					}
				}
				if (ueIp != null) {
					GeneralUtils.printToConsole("Found IP, exiting while");
					break;
				}
				GeneralUtils.unSafeSleep(100);
			}
		} catch (Exception e) {
			GeneralUtils.printToConsole("Failed UE_GET to ue " + ueId);
			GeneralUtils.printToConsole(e.getMessage());
			e.printStackTrace();
			return "";
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
			System.out.println("Failed uePowerOff to ue " + ueId);
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public void setConfig(ArrayList<EnodeB> duts) {
		configObject = new ConfigObject();
		configObject.setLogOptions("all.level=none,all.max_size=0");
		configObject.setLogFilename("/tmp/ue0.log");
		configObject.setComAddr("0.0.0.0:"+port);
		ArrayList<Cell> cells = new ArrayList<Cell>();
		String rfDriver = "";
		for (int i = 0; i < sdrList.length; i++) {
			Cell cell = new Cell();
			int earfcn;
			if (duts.size() > i) 
				earfcn = duts.get(i).getEarfcn();			
			else
				earfcn = duts.get(duts.size()-1).getEarfcn();
			cell.setDlEarfcn(earfcn);
			cell.setNAntennaDl(2);
			cell.setNAntennaUl(1);
			cell.setGlobalTimingAdvance(2);
			cells.add(cell);
			rfDriver += "dev"+i+"=/dev/sdr"+sdrList[i]+",";
		}
		int ind = rfDriver.lastIndexOf(",");
		if( ind>=0 )
			rfDriver = new StringBuilder(rfDriver).replace(ind, ind+1,"").toString();
		System.out.println("rfDriver String: " + rfDriver);
		configObject.setBandwidth(duts.get(0).getBandwidth().getBw());
		configObject.setCells(cells);
		configObject.setTxGain(txgain);
		configObject.setRxGain(rxgain);
		configObject.setMultiUe(true);
		configObject.setRfDriver(new RfDriver());
		configObject.getRfDriver().setName("sdr");
		configObject.getRfDriver().setArgs(rfDriver);
		ArrayList<UeList> ueLists = new ArrayList<UeList>();
		UeList ueList = new UeList();
		ueList.setAsRelease(13);
		ueList.setUeCategory(4);
		ueList.setForcedCqi(15);
		ueList.setForcedRi(2);
		ueList.setHalfDuplex(false);
		ueList.setSimAlgo("milenage");
		ueList.setImsi(imsiStartList[0]);
		ueList.setK("5C95978B5E89488CB7DB44381E237809");
		ueList.setOp("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF");
		ueList.setTunSetupScript("ue-ifup");
		AmarisoftUE ue= new AmarisoftUE(1, this);
		
		ueLists.add(ueList);
		configObject.setUeList(ueLists);
		setConfigFile(ueConfigFileName);
		writeConfigFile();
	}

	public ArrayList<UE> getUeList() {
		return new ArrayList<UE>(ueMap.values());
	}
}
package EPC;

import java.io.IOException;
import java.util.ArrayList;

import EnodeB.EnodeB;
import jsystem.framework.IgnoreMethod;
import jsystem.framework.system.SystemManagerImpl;
import jsystem.framework.system.SystemObjectImpl;
import systemobject.terminal.Terminal;
import UE.UE;
import Utils.GeneralUtils;

public abstract class EPC extends SystemObjectImpl {
	public static final long   	UE_SAMPLE_INTERVAL   = 2000;
	public static final long   	GENERAL_TIMEOUT   = 60000;
	public String  EPC_PROMPT;
	
	private String ipAddress;
	private String s1IpAdress;
	private String S6aAddress; 
	private static EPC instance;
	public MME[] MME = null;

	protected ArrayList<UE> pairedUEs;
	protected ArrayList<String> IMSIList;
	
	protected Terminal epcConnection;
	private static Object EPCObject = new Object();
	private String username = "root";
	private String password = "SWITcis";
	
	protected boolean ueTrackingFlag;
	
	
	public static EPC getInstance() {
		synchronized(EPCObject){
			if(instance != null){
				return instance;
			}
			try {
				 instance = (EPC) SystemManagerImpl.getInstance().getSystemObject("EPC");
				 return instance;
			} catch (Exception e) {
				GeneralUtils.printToConsole("Cant load EPC from SUT");
				e.printStackTrace();
			}
			return null;
		}
	}
	
	
	@Override
	public void init() throws Exception {
		super.init();
		pairedUEs = new ArrayList<UE>();
		IMSIList = new ArrayList<String>();
	}
	
	@IgnoreMethod
	protected abstract String getEPCVersion();
	
	public abstract EnodeB getCurrentEnodB(UE ue, ArrayList<EnodeB> possibleEnbs);
	
	public abstract boolean checkUEConnectedToNode(UE ue, EnodeB node);
	
	public abstract int checkNumberOfConnectedUEs(ArrayList<UE> ues, EnodeB enb,boolean printToConsole) throws Exception;
	
	public void addUE(UE ue) {
		if (!pairedUEs.contains(ue)) {
			GeneralUtils.printToConsole(String.format("Adding UE %s to EPC", ue.getName()));
			pairedUEs.add(ue);
			if (ue.getImsi() != null && !ue.getImsi().equals(""))
				IMSIList.add(ue.getImsi());
		}
	}
	
	public void removeUE(UE ue) {
		if (pairedUEs.contains(ue)) {
			GeneralUtils.printToConsole(String.format("Removing UE %s from EPC", ue.getName()));
			pairedUEs.remove(ue);
			IMSIList.remove(ue.getImsi());
		}
	}
	
	public void stopTrackingUEs() {
		ueTrackingFlag = false;
	}
	
//	public void startTrackingUEs() {
//		new Thread(this, getName() + " UE tracking thread").start();
//	}
	
	/*
	 * abstract method
	 */
	public abstract Boolean CheckUeListConnection(ArrayList<UE> ueList) ;

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getS1IpAdress() {
		return s1IpAdress;
	}

	public void setS1IpAdress(String s1IpAdress) {
		this.s1IpAdress = s1IpAdress;
	}
	
	public String getS6aAddress() {
		return S6aAddress;
	}

	public void setS6aAddress(String s6aAddress) {
		S6aAddress = s6aAddress;
	}
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public abstract String getNodeAccordingToImsi(String imsi);
	
	@Override
	public void close() {
		stopTrackingUEs();
		try {
			epcConnection.disconnect();
		} catch (IOException e) {}
		
		super.close();
	}


	public abstract String getEPCConfig();
	
	protected synchronized String sendCommand(String command, boolean printToConsole){
		String buffer = "";
		
		try {
			epcConnection.sendString(command, true);
			epcConnection.sendString("\n", true);
			buffer = epcConnection.readInputBuffer();
			epcConnection.sendString("\n", true);
			long startTime = System.currentTimeMillis(); // fetch starting time
			while (!buffer.trim().endsWith(EPC_PROMPT) && (System.currentTimeMillis() - startTime) < GENERAL_TIMEOUT) {
				buffer += epcConnection.readInputBuffer();
			}
		} catch (Exception e) {		
			e.printStackTrace();
		}
		if(printToConsole)
			GeneralUtils.printToConsole(buffer);
		return buffer;
	}
}

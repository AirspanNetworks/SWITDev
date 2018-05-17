package EPC;

import java.io.IOException;
import java.util.ArrayList;
import EnodeB.EnodeB;
import jsystem.framework.report.Reporter;
import UE.UE;
import UE.UeState;
import Utils.GeneralUtils;
import systemobject.terminal.SSH;

public class CiscoEPC extends EPC{

	public static final String   UE_SAMPLE_COMMAND    = "show subscribers all\r\n";
	public static final int      UE_LINK_CHAR_LOC     = 4;
	public static final char     UE_LINK_ONLINE_CHAR  = 'A';
	public static final char     UE_LINK_IDLE_CHAR    = 'D';
	
	
	public CiscoEPC() {
		EPC_PROMPT  = "#";
	}

	@Override
	public void init() throws Exception {
		super.init();
		
		if (getIpAddress() != null) {
			GeneralUtils.printToConsole("Connecting to " + getIpAddress());
			try{
			    connect();
			}catch(IOException ex){
					report.report("Unable to connect to EPC: "+ex.getMessage(),Reporter.WARNING);
			    ex.printStackTrace();
			    throw new IOException("Unable to connect to EPC");
			}
			GeneralUtils.printToConsole("Now connected to " + getIpAddress());
			//startTrackingUEs();
		}
	}
	
	private void connect() throws IOException { 
		this.epcConnection = new SSH(getIpAddress(), getUsername(), getPassword());
		this.epcConnection.connect();
	}
	
	
	/*
	 * checking foreach ue in the Epc if the ue is connected by comparing the SIM in the command
	 * show subscribers all in the cli
	 */
	@Override
	public Boolean CheckUeListConnection(ArrayList<UE> ueList) {

		if(!checkConnection()){
			report.report("Check connection failed",Reporter.WARNING);
			return false;
		}
		
		ArrayList<UE> UetoCheck = new ArrayList<UE>(ueList);
		String buffer = null;
		/*
		 * IOException and Interrupt Exception can Happen
		 */
		
		for (UE ue : ueList) {
			try {
				buffer = sendCommand("show subscribers imsi "+ ue.getImsi() + "\r\n", true);
			}catch (Exception e) {
				try {
					e.printStackTrace();
					report.report("Retry: sending command to epc "+ this.getName()+"\n");
					this.wait(5000);
					buffer = sendCommand("show subscribers all | grep "+ ue.getImsi() + "\r\n", true);
				} catch (Exception e1) {
					report.report(e1.getMessage(),Reporter.FAIL);
					return false;
				}
			}
			String[] lines = buffer.split("\n");
			for (String line : lines) {	
				if (line.contains(ue.getImsi())) {
					ue.setState(getUEState(line));
					UetoCheck.remove(ue);
					break;
				}
			}
		}
		
		for (UE ue : UetoCheck) {
			ue.setState(UeState.disconnected);
		}
		
		return true;
	}

	private UeState getUEState(String line) {
	  
		switch (line.charAt(UE_LINK_CHAR_LOC)) {
			case UE_LINK_ONLINE_CHAR: return UeState.connected;
			case UE_LINK_IDLE_CHAR: return UeState.idle;
		}
		
		return UeState.unknown;
		
	}

	@Override
	protected String getEPCVersion() {
		return "Cisco EPC";
	}

	@Override
	public EnodeB getCurrentEnodB(UE ue, ArrayList<EnodeB> possibleEnbs) {
		String allEnbNames="";
		if(checkConnection()){
			try{						
				for(EnodeB enb : possibleEnbs){
					allEnbNames+=(enb.getName()+" ");
					String buffer = sendCommand(String.format("show subscribers enodeb-address %s |grep %s",enb.getS1IpAddress(),ue.getImsi()), true);
					String[] lines = buffer.split("\n");
					for (String line : lines) {
						if (line.contains(ue.getImsi())){
							report.report(String.format("[INFO]: Ue %s with IMSI %s is connected to EnodeB %s.",ue.getName(),ue.getImsi(), enb.getName()));							
							return enb;
						}
					}
				}
			}
			catch(Exception e) {
				report.report(String.format("[WARNING]: EPC connction failed message: %s", e.getMessage()), Reporter.WARNING);
			}
		}
		report.report(String.format("[INFO]: Ue %s with IMSI %s is NOT connected to any eNB from this list: %s.",ue.getName(),ue.getImsi(), allEnbNames));
		return null;
		
	}
	
	public MME getCurrentMME(UE ue, ArrayList<MME> possibleMmes) {
		String allMMEs="";
		if(checkConnection()){
			try{						
				for(MME mme : possibleMmes){
					mme.setMMEName();
					allMMEs+=(mme.getS1IpAddress()+" ");
					String buffer = sendCommand(String.format("show subscribers mme-service %s imsi %s",mme.getName(),ue.getImsi()), true);
					if (buffer.contains(ue.getImsi())){
						report.report(String.format("[INFO]: Ue %s with IMSI %s is connected to EnodeB %s.",ue.getName(),ue.getImsi(), mme.getName()));							
						return mme;
					}
				}
			}
			catch(Exception e) {
				report.report(String.format("[WARNING]: EPC connction failed message: %s", e.getMessage()), Reporter.WARNING);
			}
		}
		report.report(String.format("[INFO]: Ue %s with IMSI %s is NOT connected to any MME from this list: %s.",ue.getName(),ue.getImsi(), allMMEs));
		return null;
		
	}

	/**
	 * return a message with state:<state> and peer address:<s1IPNode>.
	 * return empty "" if imsi is not connected to no one.
	 * @param imsi
	 * @return
	 * @throws Exception 
	 */
	public String getNodeAccordingToImsi(String imsi){
		String bufferAfterparse = "";
		if(checkConnection()){
			String command = "show subscriber full imsi "+imsi+" | grep \"Peer address\"";
			String buffer = "";
			try
			{
				epcConnection.sendString(command, true);
				epcConnection.sendString("\n", true);
				buffer = epcConnection.readInputBuffer();
			}
			catch (Exception e) {
				GeneralUtils.printToConsole("Errors in getNodeAccordingToImsi: " + e.getMessage());
				e.printStackTrace();
			}
			bufferAfterparse = GeneralUtils.getStringWithPreFix(buffer,"state:");
			if(bufferAfterparse == ""){
				return "";
			}
			
			bufferAfterparse = bufferAfterparse.replace("[local]swit_epc2#", "");
			if(bufferAfterparse != ""){
				GeneralUtils.printToConsole(bufferAfterparse);
			}else{
				return GeneralUtils.ERROR_VALUE + "";
			}
		}else
		{
			report.report(this + " not connected!" , Reporter.WARNING);
		}
		return bufferAfterparse;
			
	}
	
	private Boolean checkConnection() {
		try {
			if(!this.epcConnection.isConnected())
				connect();		
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	@Override
	public String getEPCConfig() {
		return this.sendCommand("show config", true);
	}
	
	/**
	 * @return how many Ues are connected to node
	 * @author Shahaf Shuhamy
	 */
	public int checkNumberOfConnectedUEs(ArrayList<UE> ues, EnodeB enb,boolean printToConsole) throws Exception {
		String buffer="";
		String command = String.format("show subscribers enodeb-address %s | grep 200", enb.getS1IpAddress());
		
		if(checkConnection()){
			buffer = this.sendCommand(command, printToConsole);
			if(printToConsole){
				GeneralUtils.printToConsole("Buffer Output:\n"+buffer+"\n");
			}
			ArrayList<String> imsisContaind = new ArrayList<String>();
			for(UE ue : ues){
				String imsi = ue.getImsi();
				if(buffer.contains(imsi)){
					imsisContaind.add(imsi);
				}
			}
			return imsisContaind.size();
		}
		report.report("No connection to Epc");
		return 0;
	}

	@Override
	public boolean checkUEConnectedToNode(UE ue, EnodeB node) {
		String buffer = sendCommand(String.format("show subscribers enodeb-address %s |grep %s",node.getS1IpAddress(),ue.getImsi()), true);
		String[] lines = buffer.split("\n");
		for (String line : lines) {
			if (line.contains(ue.getImsi())){
				return true;
			}
		}
		return false;
	}
}

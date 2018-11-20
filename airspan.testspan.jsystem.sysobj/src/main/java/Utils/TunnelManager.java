package Utils;

import java.io.IOException;
import java.util.ArrayList;

import EnodeB.EnodeB;
import jsystem.framework.report.Reporter;

public class TunnelManager extends Thread{
	private static TunnelManager instance;
	final int MAX_NUMBER_OF_TRIES_TO_SET_VIRTUAL_IP = 5;
	private boolean isThreadNeeded;
	private enum TunnelManagerThreadState {NOT_STARTED, RUNNING, SUSPENDED, ENDED, STOPPED}

	private TunnelManagerThreadState isRunning;
	private ArrayList<EnodebIPSecDetails> eNodebIPSecDetailsList;
	private static ArrayList<EnodeB> eNodeBInTestList;
	private IPSecServer ipSecServer;
	private Reporter report;
	
	/**************Infra Functions*******************/
	
	public synchronized static TunnelManager getInstance(ArrayList<EnodeB> eNodeBs, Reporter report) throws Exception {
		if (instance == null){
			instance = new TunnelManager(eNodeBs, report);
		}else{
			for(EnodeB eNodeB : eNodeBs){
				instance.addEnodeBsToENodebIPSecDetailsList(eNodeB);
			}
		}
		return instance;
	}
	
	private TunnelManager(ArrayList<EnodeB> eNodeBs, Reporter report) throws Exception{
		super();
		this.report = report;
		this.ipSecServer = IPSecServer.getInstance();
		this.eNodebIPSecDetailsList = new ArrayList<>();
		this.eNodeBInTestList = new ArrayList<>();
		this.isRunning = TunnelManagerThreadState.NOT_STARTED;
		for(EnodeB eNodeB : eNodeBs){
			this.eNodebIPSecDetailsList.add(new EnodebIPSecDetails(eNodeB, eNodeB.getIpsecCertificateMacAddress(), eNodeB.getIpAddress(), eNodeB.getS1IpAddress(), ""));
		}
	}
	
	public void addEnodeBsToENodebIPSecDetailsList(EnodeB eNodeB) throws IOException{
		boolean isEnodbInList = false;
		for(EnodebIPSecDetails enodebIPSecDetails : this.eNodebIPSecDetailsList){
			if(enodebIPSecDetails.geteNodeB() == eNodeB){
				isEnodbInList = true;
			}
		}
		if(!isEnodbInList){
			this.eNodebIPSecDetailsList.add(new EnodebIPSecDetails(eNodeB, eNodeB.getIpsecCertificateMacAddress(), eNodeB.getIpAddress(), eNodeB.getS1IpAddress(), ""));
		}
	}
	
	public synchronized TunnelManagerThreadState safeStart(){
		if(this.isRunning == TunnelManagerThreadState.NOT_STARTED){
			this.isThreadNeeded = true;
			instance.start();
			this.isRunning = TunnelManagerThreadState.RUNNING;
		}
		return isRunning;
	}
	
	public synchronized TunnelManagerThreadState safeSuspend(){
		if(this.isRunning == TunnelManagerThreadState.RUNNING){
			instance.suspend();
			this.isRunning = TunnelManagerThreadState.SUSPENDED;
		}
		return isRunning;
	}
	
	public synchronized TunnelManagerThreadState safeResume(){
		if(this.isRunning == TunnelManagerThreadState.SUSPENDED){
			instance.resume();
			this.isRunning = TunnelManagerThreadState.RUNNING;
		}
		return isRunning;
	}
	
	public synchronized TunnelManagerThreadState safeEnd(){ 
		this.isThreadNeeded = false;
		if(isRunning == TunnelManagerThreadState.SUSPENDED){
			safeResume();
		}
		for(int i = 1; i <= 20; i++){
			if(this.isRunning == TunnelManagerThreadState.ENDED){
				instance = null;
				break;
			}else{
				GeneralUtils.unSafeSleep(3000);
			}
		}
		if(this.isRunning != TunnelManagerThreadState.ENDED){
			instance.stop();
			instance = null;
			this.isRunning = TunnelManagerThreadState.STOPPED;
		}
		return isRunning;
	}
	
	public boolean waitForIPSecTunnelToOpen(long timeoutInMili, EnodeB eNodeB){
		EnodebIPSecDetails eNodebIPSecDetails = null;
		for(EnodebIPSecDetails eNodebIPSecDetailsIterator : this.eNodebIPSecDetailsList){
			if(eNodebIPSecDetailsIterator.geteNodeB() == eNodeB){
				eNodebIPSecDetails = eNodebIPSecDetailsIterator;
				break;
			}
		}
		if(eNodebIPSecDetails == null){
			return false;
		}
		long startTime = System.currentTimeMillis();
		boolean isIPSecTunnelOpend = false;
		while((System.currentTimeMillis() - startTime) < timeoutInMili){
			String virtualIP = ipSecServer.getEnbInternalIp(eNodebIPSecDetails.macAddress);
			if (virtualIP != ""){
				if(eNodeB.getIpAddress() != virtualIP){
					eNodebIPSecDetails.setVirtualIP(virtualIP);
					eNodeB.setIpAddress(virtualIP);
					eNodeB.setS1IpAddress(virtualIP);
					eNodeB.initSNMP();
				}
				if(eNodeB.isReachable()){
					isIPSecTunnelOpend = true;
					break;
				}
			}
			GeneralUtils.unSafeSleep(3000);
		}
		return isIPSecTunnelOpend;
	}
	
	public TunnelManagerResult openTunnel() throws TunnelManagerException{
		GeneralUtils.startLevel("Opening IPSec Tunnel");
		for(EnodebIPSecDetails eNodebIPSecDetails : eNodebIPSecDetailsList){
			EnodeB eNodeB = eNodebIPSecDetails.geteNodeB();
			eNodeB.shell("echo SKIP_CMPV2=1 > /bs/data/debug_security.cfg");
			eNodeB.lteCli("db set IPSecCfg [1] enableIPSec=1");
			eNodeB.reboot();
		}
		GeneralUtils.stopLevel();
		if(setVirtualIPsAsPublicIPs(this.eNodebIPSecDetailsList)){
			safeStart();
			int minutes = (int) (EnodeB.WAIT_FOR_ALL_RUNNING_TIME / 60000);
			report.report("Waiting for all Running (TimeOut="+minutes+" Minutes)");
			long startTime = System.currentTimeMillis(); // fetch starting time
			while((System.currentTimeMillis() - startTime) < EnodeB.WAIT_FOR_ALL_RUNNING_TIME){
				if(isAllENodeBsInAllRunning(this.eNodebIPSecDetailsList)){
					report.report("IPSec Tunnel opened successfully and all eNodeBs reach to ALL RUNNING state.");
					return TunnelManagerResult.IPSEC_TUNNEL_OPENED_SUCCESSFULLY;
				}
				GeneralUtils.unSafeSleep(2000);
			}
			report.report("IPSec Tunnel opened, but not all eNodeBs reached to ALL RUNNING state.", Reporter.FAIL);
			return TunnelManagerResult.IPSEC_TUNNEL_OPENED_BUT_DID_NOT_REACH_ALL_RUNNING_STATE;
		}else{
			report.report("Failed to open IPSec Tunnel.", Reporter.FAIL);
			throw new TunnelManagerException(TunnelManagerResult.COULD_NOT_OPEN_IPSEC_TUNNEL);
		}
	}
	
	public TunnelManagerResult closeTunnel(){
		report.report("Closing IPSec Tunnel");
		GeneralUtils.printToConsole("Disable IPSec and Reboot.");
		for(EnodebIPSecDetails eNodebIPSecDetails : this.eNodebIPSecDetailsList){
			EnodeB eNodeB = eNodebIPSecDetails.geteNodeB();
			eNodeB.lteCli("db set IPSecCfg [1] enableIPSec=0");
			eNodeB.reboot();
		}
		safeEnd();
		setAddressToPublicIp(this.eNodebIPSecDetailsList);
		GeneralUtils.unSafeSleep(60000);
		for(EnodebIPSecDetails eNodebIPSecDetails : this.eNodebIPSecDetailsList){
			report.report("Generate snmd.conf file - on Velocity the file is changing to default values (SW BUG)", Reporter.WARNING);
			generateClassicSnmpdFile(eNodebIPSecDetails);
		}

		long startTime = System.currentTimeMillis(); // fetch starting time
		while((System.currentTimeMillis() - startTime) < EnodeB.WAIT_FOR_ALL_RUNNING_TIME){
			if(isAllENodeBsInAllRunning(this.eNodebIPSecDetailsList)){
				report.report("IPSec Tunnel closed successfully and all eNodeBs reached to ALL RUNNING state.");
				return TunnelManagerResult.IPSEC_TUNNEL_CLOSED_SUCCESSFULLY;
			}
		}
		report.report("IPSec Tunnel closed, but not all eNodeBs reached to ALL RUNNING state.");
		return TunnelManagerResult.IPSEC_TUNNEL_CLOSED_BUT_DID_NOT_REACH_ALL_RUNNING_STATE;
	}
	
	public void run(){
		this.isRunning = TunnelManagerThreadState.RUNNING;
		while(this.isThreadNeeded){
			if(areThereAnyTriesLeft(this.eNodebIPSecDetailsList)){
				ArrayList<EnodebIPSecDetails> tmpENodebIPSecDetailsList = new ArrayList<>();
				for(EnodebIPSecDetails eNodebIPSecDetails : this.eNodebIPSecDetailsList){
					if((eNodebIPSecDetails.geteNodeB().getIpAddress() != eNodebIPSecDetails.getVirtualIP()) || (!eNodebIPSecDetails.geteNodeB().isReachable())){
						if(!this.isThreadNeeded){
							tmpENodebIPSecDetailsList.clear();
							break;
						}
						GeneralUtils.printToConsole("TunnelManagerThread: DUT isn't reachable - Getting virtual IP again from start" + eNodebIPSecDetails.geteNodeB().getName());
						if(eNodebIPSecDetails.getNumberOfTriesToSetVirtualIP() < MAX_NUMBER_OF_TRIES_TO_SET_VIRTUAL_IP){
							tmpENodebIPSecDetailsList.add(eNodebIPSecDetails);
						}
						eNodebIPSecDetails.setNumberOfTriesToSetVirtualIP(
								MAX_NUMBER_OF_TRIES_TO_SET_VIRTUAL_IP < (eNodebIPSecDetails.numberOfTriesToSetVirtualIP+1)?
										MAX_NUMBER_OF_TRIES_TO_SET_VIRTUAL_IP : (eNodebIPSecDetails.numberOfTriesToSetVirtualIP+1));
					}else{
						eNodebIPSecDetails.setNumberOfTriesToSetVirtualIP(0);
					}
				}
				if(!tmpENodebIPSecDetailsList.isEmpty()){
					if(!setVirtualIPsAsPublicIPs(tmpENodebIPSecDetailsList)){
						report.report("TunnelManagerThread: Failed to instanciate IPSec tunnel for all DUTs.", Reporter.WARNING);
					}
				}else{
					GeneralUtils.printToConsole("TunnelManagerThread: All DUTs are reachable");
				}
			}else{
				break;
			}
			GeneralUtils.unSafeSleep(30000);
		}
		if(this.isThreadNeeded && !areThereAnyTriesLeft(this.eNodebIPSecDetailsList)){
			report.report("TunnelManagerThread: Failed to instanciate IPSec tunnel - stopping TunnelManagerThread", Reporter.WARNING);
		}
		this.isRunning = TunnelManagerThreadState.ENDED;
	}
	
	/************Helper Functions
	 * @throws TunnelManagerException **************/
	
	private synchronized boolean setVirtualIPsAsPublicIPs(ArrayList<EnodebIPSecDetails> eNodebIPSecDetailsList){
		ArrayList<Integer> eNodeBsIndexes = new ArrayList<>();
		for(int i = 0; i < eNodebIPSecDetailsList.size(); i++){eNodeBsIndexes.add(i);}
		GeneralUtils.printToConsole("TunnelManager: Getting Virtual IP.");
		for (int i = 0; i < 30; i++) {
			if(!eNodeBsIndexes.isEmpty()){
				ArrayList<Integer> indexesToRemove = new ArrayList<>();
				for(int j : eNodeBsIndexes){
					EnodeB eNodeB = eNodebIPSecDetailsList.get(j).geteNodeB();
					String virtualIP = ipSecServer.getEnbInternalIp(eNodebIPSecDetailsList.get(j).macAddress);
					if (virtualIP != ""){
						if(eNodeB.getIpAddress() != virtualIP && (!eNodeB.isReachable())){
							eNodebIPSecDetailsList.get(j).setVirtualIP(virtualIP);
							if(eNodeBInTestList.contains(eNodeB)){
								GeneralUtils.printToConsole("TunnelManagerThread: IPSec Tunnel opened Changing " + eNodeB.getName() + "'s managment IP ("+eNodebIPSecDetailsList.get(j).getPublicIP()+") to virtual IP (" + virtualIP + ").");
							}
							eNodeB.setIpAddress(virtualIP);
							eNodeB.setS1IpAddress(virtualIP);
							eNodeB.initSNMP();
							GeneralUtils.unSafeSleep(3000);
						}else if(eNodeB.getIpAddress() != eNodebIPSecDetailsList.get(j).getPublicIP() && (!eNodeB.isReachable())){
							if(eNodeBInTestList.contains(eNodeB)){
								GeneralUtils.printToConsole("TunnelManagerThread: "+eNodeB.getName() + " is not reachable - changing managment IP back to public IP ("+ eNodebIPSecDetailsList.get(j).getPublicIP() +")");
							}
							eNodeB.setIpAddress(eNodebIPSecDetailsList.get(j).getPublicIP());
							eNodeB.setS1IpAddress(eNodebIPSecDetailsList.get(j).getS1IpAddress());
							eNodeB.initSNMP();
							GeneralUtils.unSafeSleep(3000);
							if(eNodeB.isReachable() && eNodebIPSecDetailsList.get(j).isTunnelOpenedBefore()){
								if(eNodeBInTestList.contains(eNodeB)){
									GeneralUtils.printToConsole("TunnelManager: "+eNodeB.getName() + " is reachable by public IP ("+ eNodebIPSecDetailsList.get(j).getPublicIP() +") after IPSec Tunnel opened before.");
								}
							}
						}else{
							if(eNodeBInTestList.contains(eNodeB) && eNodeB.getIpAddress() == eNodebIPSecDetailsList.get(j).getVirtualIP()){
								GeneralUtils.printToConsole("TunnelManager: "+eNodeB.getName() + " is reachable by Virtual IP ("+ eNodebIPSecDetailsList.get(j).getVirtualIP() +").");
							}
							eNodebIPSecDetailsList.get(j).setTunnelOpenedBefore(true);
							indexesToRemove.add(j);
						}
						
					}
				}
				for(Integer indexToRemove : indexesToRemove){eNodeBsIndexes.remove(indexToRemove);}
				GeneralUtils.unSafeSleep(10000);
			}else{
				return true;
			}
		}
		for(int j : eNodeBsIndexes){
			EnodeB eNodeB = eNodebIPSecDetailsList.get(j).geteNodeB();
			if(eNodeBInTestList.contains(eNodeB)){
				report.report("TunnelManager: Failed to open IPSec Tunnel for " + eNodeB.getName() + ": Attampt number " + eNodebIPSecDetailsList.get(j).getNumberOfTriesToSetVirtualIP(), Reporter.WARNING);
			}else{
				GeneralUtils.printToConsole("TunnelManagerThread: Failed to open IPSec Tunnel for " + eNodeB.getName() + ": Attampt number " + eNodebIPSecDetailsList.get(j).getNumberOfTriesToSetVirtualIP());
			}
		}
		return false;
	}
	
	private boolean areThereAnyTriesLeft(ArrayList<EnodebIPSecDetails> eNodebIPSecDetailsList){
		for(EnodebIPSecDetails eNodebIPSecDetails : eNodebIPSecDetailsList){
			if(eNodebIPSecDetails.getNumberOfTriesToSetVirtualIP() < MAX_NUMBER_OF_TRIES_TO_SET_VIRTUAL_IP){
				return true;
			}
		}
		return false;
	}
	
	private void areThereAnyTriesLeftStatus(){
		for(EnodebIPSecDetails eNodebIPSecDetails : eNodebIPSecDetailsList){
			if(eNodebIPSecDetails.getNumberOfTriesToSetVirtualIP() >= MAX_NUMBER_OF_TRIES_TO_SET_VIRTUAL_IP){
				EnodeB eNodeB = eNodebIPSecDetails.geteNodeB();
				if(eNodeBInTestList.contains(eNodeB)){
					report.report(eNodeB.getName() + " reach the limit of MAX attempts ("+ MAX_NUMBER_OF_TRIES_TO_SET_VIRTUAL_IP +") to open IPSec Tunnel.", Reporter.WARNING);
				}
			}
		}
	}
	
	public static void failedTestIfNoTriesLeftForAnyDut(){
		if(instance != null){
			instance.areThereAnyTriesLeftStatus();
		}
	}
	
	private void setAddressToPublicIp(ArrayList<EnodebIPSecDetails> eNodebIPSecDetailsList) {
		GeneralUtils.printToConsole("Change managment IP to public IP.");
		for(EnodebIPSecDetails eNodebIPSecDetails : eNodebIPSecDetailsList){
			EnodeB eNodeB = eNodebIPSecDetails.geteNodeB();
			eNodeB.setIpAddress(eNodebIPSecDetails.getPublicIP());
			eNodeB.setS1IpAddress(eNodebIPSecDetails.getS1IpAddress());
			eNodeB.initSNMP();
		}
	}
	
	private boolean isAllENodeBsInAllRunning(ArrayList<EnodebIPSecDetails> eNodebIPSecDetailsList){
		for(EnodebIPSecDetails eNodebIPSecDetails : eNodebIPSecDetailsList){
			if (!eNodebIPSecDetails.geteNodeB().isAllRunning()) {
				return false;
			}
		}
		return true;
	}
	
	void generateClassicSnmpdFile(EnodebIPSecDetails eNodebIPSecDetails){
		EnodeB eNodeB = eNodebIPSecDetails.geteNodeB();
		
		eNodeB.shell("rwcommunity  private > /bsdata/snmpd.conf");
		eNodeB.shell("rocommunity  public >> /bsdata/snmpd.conf");
		eNodeB.shell("agentuser  root >> /bsdata/snmpd.conf");
		eNodeB.shell("dlmod system /bs/asLteSnmp.so >> /bsdata/snmpd.conf");
		eNodeB.shell("agentaddress UDP:"+ eNodebIPSecDetails.getPublicIP() +":161 >> /bsdata/snmpd.conf");
		eNodeB.shell("trapsink  127.0.0.1  3232 >> /bsdata/snmpd.conf");
	}
	
	/***************Results Values****************/
	
	public enum TunnelManagerResult{IPSEC_TUNNEL_OPENED_SUCCESSFULLY, IPSEC_TUNNEL_CLOSED_SUCCESSFULLY,
		COULD_NOT_OPEN_IPSEC_TUNNEL, IPSEC_TUNNEL_OPENED_BUT_DID_NOT_REACH_ALL_RUNNING_STATE,
		IPSEC_TUNNEL_CLOSED_BUT_DID_NOT_REACH_ALL_RUNNING_STATE}
	
	public class TunnelManagerException extends Exception{
		private static final long serialVersionUID = 1L;
		private TunnelManagerResult result;
		
		TunnelManagerException(TunnelManagerResult result){
			this.result = result;
		}
		
		public TunnelManagerResult getResult() {
			return result;
		}
		public void setResult(TunnelManagerResult result) {
			this.result = result;
		}
	}
	
	/***************Helper Nested Class****************/
	
	private class EnodebIPSecDetails{
		private EnodeB eNodeB;
		private String macAddress;
		private String publicIP;
		private String s1IpAddress;
		private String virtualIP;
		private boolean tunnelOpenedBefore;
		private int numberOfTriesToSetVirtualIP;
		
		public EnodebIPSecDetails(EnodeB eNodeB, String macAddress, String publicIP, String s1IpAddress, String virtualIP){
			this.eNodeB = eNodeB;
			this.macAddress = macAddress;
			this.publicIP = publicIP;
			this.s1IpAddress = s1IpAddress;
			this.virtualIP = virtualIP;
			this.tunnelOpenedBefore = false;
			numberOfTriesToSetVirtualIP = 0;
		}
		
		public EnodeB geteNodeB() {
			return eNodeB;
		}
		public void seteNodeB(EnodeB eNodeB) {
			this.eNodeB = eNodeB;
		}
		public String getMacAddress() {
			return macAddress;
		}
		public void setMacAddress(String macAddress) {
			this.macAddress = macAddress;
		}
		public String getPublicIP() {
			return publicIP;
		}
		public void setPublicIP(String publicIP) {
			this.publicIP = publicIP;
		}
		public String getVirtualIP() {
			return virtualIP;
		}
		public void setVirtualIP(String virtualIP) {
			this.virtualIP = virtualIP;
		}
		public boolean isTunnelOpenedBefore() {
			return tunnelOpenedBefore;
		}
		public void setTunnelOpenedBefore(boolean tunnelOpenedBefore) {
			this.tunnelOpenedBefore = tunnelOpenedBefore;
		}

		public int getNumberOfTriesToSetVirtualIP() {
			return numberOfTriesToSetVirtualIP;
		}

		public void setNumberOfTriesToSetVirtualIP(int numberOfTriesToSetVirtualIP) {
			this.numberOfTriesToSetVirtualIP = numberOfTriesToSetVirtualIP;
		}

		public String getS1IpAddress() {
			return s1IpAddress;
		}

		public void setS1IpAddress(String s1IpAddress) {
			this.s1IpAddress = s1IpAddress;
		}
		
	}

	/*************Getters And Setters************/
	
	public static ArrayList<EnodeB> geteNodeBInTestList() {
		return eNodeBInTestList;
	}

	public static void seteNodeBInTestList(ArrayList<EnodeB> eNodeBInTestList) {
		TunnelManager.eNodeBInTestList = eNodeBInTestList;
	}
}

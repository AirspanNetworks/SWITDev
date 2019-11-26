package testsNG.Actions.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class CloseAllIPerfSessions {
	static String lastUpdateDate = "16/05/2018";
	static String version = "1.0.7";
	static String lastChanges = "Close ssh session also when exception was thrown and change pkill to -9 flag.";
	
	public boolean closeAllIPerfSessions(String filePath) {
		System.out.println("Start: Close All IPerf Sessions.");
		System.out.println("SUT file path: " + filePath);
		IPerfMachineDetails[] myIPerfMachinesDetails = parseSUT(filePath);
		
		boolean resDL = false;
		boolean resUL = false;
		
		if(myIPerfMachinesDetails != null){
			
			resDL = killIPerfSessions(myIPerfMachinesDetails[0]);
			resUL = killIPerfSessions(myIPerfMachinesDetails[1]);
			
			if(resDL){
				System.out.println("Succeeded to stop DL IPerf traffic.");
			}
			
			if(resUL){
				System.out.println("Succeeded to stop UL IPerf traffic.");
			}
		}else{
			System.out.println("FAILED to parse IPerf Machines from SUT.");
		}
		System.out.println("End: Close All IPerf Sessions.");
		return resDL || resUL;
	}

	
	
	private boolean killIPerfSessions(IPerfMachineDetails iperfMachineDetails) {
		boolean result = false;
		if(iperfMachineDetails != null){
			switch (iperfMachineDetails.getIperfMachineType()) {
			case IPerfLinuxMachine:
				result = killIPerfSessionsOnLinuxMachine(iperfMachineDetails);
				break;
			case IPerfLocalWindowsMachine:
				result = killIPerfSessionsOnLocalWindowsMachine(iperfMachineDetails);
				break;
			case IPerfRemoteWindowsMachine:
				result = killIPerfSessionsOnRemoteWindowsMachine(iperfMachineDetails);
				break;
			}
		}else{
			System.out.println("iperfMachineDetails == null");
			result = false;
		}
		return result;
	}



	private boolean killIPerfSessionsOnRemoteWindowsMachine(IPerfMachineDetails iperfMachineDetails) {
		System.out.println("CloseAllIPerfSessions.killIPerfSessionsOnRemoteWindowsMachine - Unimplemented Method!");
		return false;
	}



	private boolean killIPerfSessionsOnLocalWindowsMachine(IPerfMachineDetails iperfMachineDetails) {
		boolean result = false;
		try {
			System.out.println("Connected to "+iperfMachineDetails.getMachineName());
			System.out.println("Run: taskkill /f /im iperf.exe");
			Runtime.getRuntime().exec("taskkill /f /im iperf.exe");
			result = true;
		} catch (IOException e) {
			try {
				Runtime.getRuntime().exec("taskkill /f /im iperf.exe");
				result = true;
				System.out.println("DONE");
			} catch (IOException e1) {
				System.out.println("FAILED execute: taskkill /f /im iperf.exe");
				result = false;
				e1.printStackTrace();
			}
		}
		return result;
	}



	private boolean killIPerfSessionsOnLinuxMachine(IPerfMachineDetails iperfMachineDetails) {
		boolean result = false;
		Session session = null;
        try{
        	System.out.println("Run: pkill -9 iperf");
	    	JSch jsch = new JSch();
	    	session=jsch.getSession(iperfMachineDetails.getUsername(), iperfMachineDetails.getHostname(), 22);
	    	session.setPassword(iperfMachineDetails.getPassword());
	    	session.setConfig("StrictHostKeyChecking", "no");
	    	System.out.println("Connected to "+iperfMachineDetails.getMachineName());
	    	session.connect();
	    	if(session.isConnected()){
		    	System.out.println("Run: pkill -9 iperf");
		    	
		    	ChannelShell  channelShell = (ChannelShell) session.openChannel("shell");
		    	
		    	BufferedReader fromServer = new BufferedReader(new InputStreamReader(channelShell.getInputStream()));  
		    	OutputStream toServer = channelShell.getOutputStream();
		    	channelShell.connect();  
		    	final String endOfOutput = "endOfOutput"; // for understanding when to stop reading from output stream.
		    	String command = "ps -aux | grep iperf ; pkill -9 iperf ; pkill -9 iperf ; sleep 3 ; echo ~~~~~~~~~~~~~pkill -9 iperf~~~~~~~~~~~ ; ps -aux | grep iperf ; echo "+endOfOutput+"\r\n";
				toServer.write(command.getBytes());
				result = true;
				try{Thread.sleep(2000);}catch(Exception e){e.printStackTrace();}
		    	toServer.flush();
		    	
		    	
		    	StringBuilder builder = new StringBuilder();  

		    	int count = 0;  
		    	String line = "";  

		    	while(line != null) {  
		    		if (line.contains("endOfOutput") && !line.contains("echo")){
		    	        break;
		    	    }
		    		
		    	    line = fromServer.readLine();
		    	    builder.append(line).append("\n");
		    	}  
		    	String response = builder.toString();  
		    	System.out.println(response);
		    	channelShell.disconnect();
	    	}
	        session.disconnect();
	        System.out.println("DONE");
	    }catch(Exception e){
	    	try{
	    		session.disconnect();
	    	}catch(Exception e1){
	    		e1.printStackTrace();
	    	}
	    	if(!result){
	    		System.out.println("FAILED to execute: pkill -9 iperf");
	    	}
	    	e.printStackTrace();
	    }
        
		return result;
	}



	private IPerfMachineDetails[] parseSUT(String filePath) {
		File fXmlFile = new File(filePath);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		Document doc = null;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(fXmlFile);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		doc.getDocumentElement().normalize();

		System.out.println("Root element :" + doc.getDocumentElement().getNodeName());

		IPerfMachineDetails iperfDLMachineDetails = getIPerfMachineDetails(doc, "iperfMachineDL");
		IPerfMachineDetails iperfULMachineDetails = getIPerfMachineDetails(doc,"iperfMachineUL");
		
		IPerfMachineDetails[] iperfMachinesDetails = new IPerfMachineDetails[2];
		
		iperfMachinesDetails[0] = iperfDLMachineDetails;
		iperfMachinesDetails[1] = iperfULMachineDetails;
		
		
		
		return iperfMachinesDetails;
	}
	
	private IPerfMachineDetails getIPerfMachineDetails(Document doc, String iperfMachineTagName){
		IPerfMachineDetails iperfMachineDetails = null;
		if(doc != null){
			NodeList iperfMachineNodeList = doc.getElementsByTagName(iperfMachineTagName);
		
			if(iperfMachineNodeList.getLength() > 0){
				Node iperfMachineNode = iperfMachineNodeList.item(0);
				Element iperfMachineElement = (Element) iperfMachineNode;
				
				String iperfMachineType = null, hostnsme = null, username = null, password = null;
				
				try{
					iperfMachineType = iperfMachineElement.getElementsByTagName("class").item(0).getTextContent();
					hostnsme = iperfMachineElement.getElementsByTagName("hostname").item(0).getTextContent();
					username = iperfMachineElement.getElementsByTagName("username").item(0).getTextContent();
					password = iperfMachineElement.getElementsByTagName("password").item(0).getTextContent();
				}catch(Exception e){
					e.printStackTrace();
					System.out.println("One or more of the IPerf Machine Details are missing.");
				}
				
				if(iperfMachineType != null && hostnsme != null && username != null && password != null){
					iperfMachineDetails = new IPerfMachineDetails(iperfMachineTagName, hostnsme, username, password, iperfMachineType);
				}
			}
		}
		return iperfMachineDetails;
	}


	private enum IPerfMachineType {IPerfLinuxMachine, IPerfLocalWindowsMachine, IPerfRemoteWindowsMachine}
	
	private class IPerfMachineDetails{
		private String machineName;
		private String hostname;
		private String username;
		private String password;
		private IPerfMachineType iperfMachineType;
		
		public IPerfMachineDetails(String machineName, String hostname, String username, String password, String iperfMachineType){
			setMachineName(machineName);
			setHostname(hostname);
			setUsername(username);
			setPassword(password);
			setIperfMachineType(iperfMachineType);
		}
		
		public String getMachineName() {
			return machineName;
		}
		public void setMachineName(String machineName) {
			this.machineName = machineName;
		}
		public String getHostname() {
			return hostname;
		}
		public void setHostname(String hostname) {
			this.hostname = hostname;
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
		public IPerfMachineType getIperfMachineType() {
			return iperfMachineType;
		}
		public void setIperfMachineType(String iperfMachineType) {
			if(iperfMachineType.contains(IPerfMachineType.IPerfLinuxMachine.toString())){
				this.iperfMachineType = IPerfMachineType.IPerfLinuxMachine;
			}else if (iperfMachineType.contains(IPerfMachineType.IPerfLocalWindowsMachine.toString())) {
				this.iperfMachineType = IPerfMachineType.IPerfLocalWindowsMachine;
			}else if (iperfMachineType.contains(IPerfMachineType.IPerfRemoteWindowsMachine.toString())) {
				this.iperfMachineType = IPerfMachineType.IPerfRemoteWindowsMachine;
			}else{
				this.iperfMachineType = null;
			}
		}
	}
}

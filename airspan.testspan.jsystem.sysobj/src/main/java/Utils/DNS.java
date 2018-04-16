package Utils;

import jsystem.framework.report.Reporter;
import jsystem.framework.system.SystemManagerImpl;
import jsystem.framework.system.SystemObjectImpl;

/**
 * DNS.
 * 
 * @author Avichai Yefet
 */
public class DNS extends SystemObjectImpl{
	private static DNS instance;
	private String ipAddress;
	private String username;
	private String password;
	private SSHConnector socket;
	private long startToCollectPacketsTime;
	private long timeoutInMili;
	private int ptrRequestNumberReceived;
	
	/********* Infra **********/
	
	@Override
	public void init() throws Exception {
		super.init();
		socket = new SSHConnector(ipAddress, username, password);
	}
	
	@Override
	public void close() {
		super.close();
	}

	public synchronized static DNS getInstance() {
		if (instance == null){
			try {
				instance = (DNS) SystemManagerImpl.getInstance().getSystemObject("DNS");
				instance.startToCollectPacketsTime = 0;
				instance.timeoutInMili = 0;
				instance.ptrRequestNumberReceived = 1;
			} catch (Exception e) {
				GeneralUtils.printToConsole("DNS - Failed Initialize.");  
				e.printStackTrace();
			}
		}
		return instance;
	}
	
	public void openConnection(){
		if(socket.isConnected() == false){  
			socket.initConnection();
		}
	}
	
	public void closeConnection(){
		if(socket.isConnected() == true){
			socket.disconnect();
		}
	}
	/********* Operations **********/
	
	public synchronized  boolean startToCollectPackets(String fromIpAddress, int timeoutInSeconds){ //The FILE is written in the end of the timeout.
		openConnection();
		boolean result = false;
		if((System.currentTimeMillis() - startToCollectPacketsTime) >= timeoutInMili){
			if(socket.isConnected()){
				String tcpdumpCommand = "nohup timeout "+timeoutInSeconds+
						" tcpdump -penni any dst port 53 and host "+fromIpAddress+" -l > /usr/pcaps/dnsPtrRequestFrom"+fromIpAddress+" &"; 
				GeneralUtils.printToConsole("tcpdumpCommand="+tcpdumpCommand);
				// ">" write over file if exist, so no need to delete the last file (last results).
				String response = socket.sendCommand(tcpdumpCommand, 5);
				this.startToCollectPacketsTime = System.currentTimeMillis();
				GeneralUtils.unSafeSleep(10 * 1000);
				this.timeoutInMili = timeoutInSeconds * 1000;
				if(response != null){
					result = true;
				}
			}else{
				GeneralUtils.printToConsole("Func: DNS.startToCollectPackets - DNS Socket is closed");
				report.report("DNS Socket is closed (" + ipAddress + ")", Reporter.WARNING);
			}
		}else{
			GeneralUtils.printToConsole("Func: DNS.startToCollectPackets - Still Colecting, last start does NOT finish.");
			result = true;
		}
		//Doesn't close connection, because the command stopped follow the close connection
		return result;
	}
	
	public synchronized  Pair<Boolean, String> isDnsPtrRequestReceived(String fromIpAddress){
		openConnection();
		String getResponseCmd = "cat /usr/pcaps/dnsPtrRequestFrom"+fromIpAddress;
		String clearResponseCmd = "echo Ptr Request Number "+ptrRequestNumberReceived+" Has Been Received > /usr/pcaps/dnsPtrRequestFrom"+fromIpAddress;
		String response = "";
		Pair<Boolean, String> result = new Pair<Boolean, String>(false, response);
		do{
			if(socket.isConnected()){
				response = socket.sendCommand(getResponseCmd, 5);
				if(response != null){
					String[] lines = response.split("\n");
					for(String line : lines){
						if(line.contains(fromIpAddress) && line.contains("PTR")){
							result = new Pair<Boolean, String>(true, response);
							break;
						}
					}
					if(result.getElement0() == true){
						socket.sendCommand(clearResponseCmd, 5);
						ptrRequestNumberReceived++;
						break;
					}
				}
			}else{
				GeneralUtils.printToConsole("Func: DNS.isDnsPtrRequestReceived - DNS Socket is closed");
				report.report("DNS Socket is closed (" + ipAddress + ")", Reporter.WARNING);
				break;
			}
			GeneralUtils.unSafeSleep(3000);
		}while((System.currentTimeMillis() - startToCollectPacketsTime) <= timeoutInMili);
		//Doesn't close connection, because the command stopped follow the close connection and sometimes I'm waiting for multiple PTR REQEUST. 
		return result;
	}

	/********* Setters & Getters **********/
	
	public String getIpAddress() {
		return ipAddress;
	}
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
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
}

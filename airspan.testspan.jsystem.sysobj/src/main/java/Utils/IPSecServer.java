package Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jsystem.framework.system.SystemManagerImpl;
import jsystem.framework.system.SystemObjectImpl;

public class IPSecServer extends SystemObjectImpl {

	/** Command execution time out */
	private static final int COMMAND_TIMEOUT = 2000; // 2 seconds

	private static IPSecServer instance;

	/** The SSH connector to DHCP */
	private SSHConnector ssh;

	/** The IP address. */
	public String ipAddress = "";

	/** The user name the component uses to login. */
	public String username = "root";

	/** The password the component uses to login. */
	public String password = "";

	@Override
	public void init() throws Exception {
		super.init();
		ssh = new SSHConnector(ipAddress, username, password);
		ssh.initConnection();
	}
	
	public synchronized static IPSecServer getInstance() {
		if (instance == null){
			try {
				instance = (IPSecServer) SystemManagerImpl.getInstance().getSystemObject("IPSecServer");
			} catch (Exception e) {
				GeneralUtils.printToConsole("IPSecServer - Failed Initialize.");  
				e.printStackTrace();
			}
		}
		return instance;
	}

	/**
	 * @param strCommand
	 *            - command to perform. This command will be send to underlying
	 *            LINUX machine via SSH connection
	 * 
	 * @return String - Response to executed command
	 * 
	 * @throws Exception
	 */
	protected synchronized String sendCommandViaSshCmd(String strCommand) throws Exception {
		return ssh.sendCommand(strCommand, COMMAND_TIMEOUT);
	}

	public String getEnbInternalIp(String macAddress){
		// send command to find IPSec session id for enb mac.
		String command = "ipsec status";
		String response;
		try {
			response = sendCommandViaSshCmd(command);
			String regexPattern = 	"ESTABLISHED.*" + macAddress.replace(":", "").toUpperCase() + ".*\\s*.*\\s*.*===\\s*(\\d*.\\d*.\\d*.\\d*)";
			Pattern r = Pattern.compile(regexPattern);
			Matcher m = r.matcher(response);
			String res =  m.find() ? m.group(1) : "";
			return res;
		} catch (Exception e) {
			return "";
		}
	}
	
	public void start() throws Exception {
		sendCommandViaSshCmd("ipsec start");
	}
	
	public void stop() throws Exception {
		sendCommandViaSshCmd("ipsec stop");
	}
	
	public void restart() throws Exception {
		sendCommandViaSshCmd("ipsec restart");
	}

	public void closeDhcpConnection() {
		ssh.disconnect();
	}

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

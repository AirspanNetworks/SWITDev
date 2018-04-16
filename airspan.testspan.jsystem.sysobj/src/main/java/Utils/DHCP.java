package Utils;

import EnodeB.EnodeB;
import jsystem.framework.system.SystemObjectImpl;


public class DHCP extends SystemObjectImpl {
	
	/** Command execution time out */
	private static final int COMMAND_TIMEOUT = 2000; // 2 seconds
	
	/** Sed replace command pattern */
	private static final String FIND_AND_REPLACE_BETWEEN_A_AND_B_with_SED = "sed -i '/%s/,/%s/s/%s/%s/g' %s";  //"sed -i '/from/,/to/s/old/new/g' %s"
	
	/** Sed replace command pattern */
	private static final String FIND_Text_BETWEEN_Line_A_AND_B_BETWEEN_String_C_AND_D_with_SED = "sed -n '/%s/,/%s/s/%s\\(.*\\)%s/\\1/p' %s";  //"sed -n '/fromLine/,/toLine/s/fromText/toText/p' %s"
	
	/** Command execution time out */
	private static String RESTART_DHCP = "service isc-dhcp-server restart";
	
	/** Command execution time out */
	private static String CONFIG_FILE = "/etc/dhcp/dhcpd.conf";
	
	/** The SSH connector to DHCP */
	private SSHConnector sshDhcp;
	
	/** The IP address. */
	public String ipAddress = "";

	/** The user name the component uses to login. */
	public String username = "root";

	/** The password the component uses to login. */
	public String password = "";
	
	@Override
	public void init() throws Exception {
		super.init();
		sshDhcp = new SSHConnector(ipAddress,username,password);
		sshDhcp.initConnection();
	}
	
	/**
	 * @param strCommand - command to perform. This command will be send to
	 * underlying LINUX machine via SSH connection
	 * 
	 * @return String - Response to executed command
	 * 
	 * @throws Exception
	 */
	protected String sendCommandViaSshCmd(String strCommand) {
		report.report(String.format("Sending command to DHCP server.\nCommand: %s", strCommand));
		return sshDhcp.sendCommand(strCommand, COMMAND_TIMEOUT);
	}
	
	protected String findEnodeBDhcpParameters(EnodeB enodeB, String from ,String to){
		String command = String.format(FIND_Text_BETWEEN_Line_A_AND_B_BETWEEN_String_C_AND_D_with_SED, enodeB.getNetspanName(),"END_CUSTOM_OPTIONS",from,to,CONFIG_FILE);
		String response = "";
		try {
			 response = sendCommandViaSshCmd(command);
			 response = response.split("\n")[response.split("\n").length - 2].trim();
		}
		catch (Exception e) {
			StdOut.print2console("Failed to excute following command [ "+command+"]");
			StdOut.print2console("Possible reason : broken SSH connection with DHCP server");
			StdOut.print2console(e.getMessage());
			StdOut.print2console(e.getStackTrace().toString());
			return response;
		}
		return response;
	}
	
	protected boolean findAndReplaceEnodeBDhcpParameters(EnodeB enodeB, String find ,String replace){
		String command = String.format(FIND_AND_REPLACE_BETWEEN_A_AND_B_with_SED, enodeB.getNetspanName(),"END_CUSTOM_OPTIONS",find,replace,CONFIG_FILE);
		try {
			sendCommandViaSshCmd(command);
		}
		catch (Exception e) {
			StdOut.print2console("Failed to excute following command [ "+command+"]");
			StdOut.print2console("Possible reason : broken SSH connection with DHCP server");
			StdOut.print2console(e.getMessage());
			StdOut.print2console(e.getStackTrace().toString());
			return false;
		}
		return true;
	}
	
	public boolean setDhcpConfigNetspanIP(EnodeB enodeB, String netspanIP){
		String findLine = "option PNP.LSMIP.*";
		String replaceLine = String.format("option PNP.LSMIP %s;", netspanIP);
		findAndReplaceEnodeBDhcpParameters(enodeB, findLine, replaceLine);
		sendCommandViaSshCmd(RESTART_DHCP);
		return true;
	}
	
	public String getDhcpConfigNetspanIP(EnodeB enodeB){
		String fromString = "option PNP.LSMIP ";
		String toString = ";";
		return findEnodeBDhcpParameters(enodeB, fromString, toString);
	}
	
	public boolean setDhcpConfigIRelay(EnodeB enodeB, boolean isIRelayConfig) {
		String findLine = "option PNP.Backhaul_Identification.*";
		String replaceLine = String.format("option PNP.Backhaul_Identification %s;", isIRelayConfig);
		findAndReplaceEnodeBDhcpParameters(enodeB, findLine, replaceLine);
		sendCommandViaSshCmd(RESTART_DHCP);
		return true;
	}
	
	public String getDhcpConfigIRelay(EnodeB enodeB){
		String fromString = "option PNP.Backhaul_Identification ";
		String toString = ";";
		return findEnodeBDhcpParameters(enodeB, fromString, toString);
	}
	
	public String getDhcpConfigIpAddress(EnodeB enodeB, boolean isIPv6){
		CONFIG_FILE = isIPv6 ? "/etc/dhcp/dhcpd6.conf": "/etc/dhcp/dhcpd.conf";
		String fromString = isIPv6 ? "fixed-address6 " : "fixed-address ";
		String toString = ";";
		return findEnodeBDhcpParameters(enodeB, fromString, toString);
	}
	
	public boolean setDhcpConfigIpVersion(boolean isIPv6) {
		CONFIG_FILE = isIPv6 ? "/etc/dhcp/dhcpd6.conf": "/etc/dhcp/dhcpd.conf";
		RESTART_DHCP = isIPv6 ? "service isc-dhcp-server6 restart" : "service isc-dhcp-server restart";
		String ipVersion = isIPv6 ? "6" : "4";
		String dhcp6 = isIPv6 ? "start" : "stop";
		String dhcp4 = isIPv6 ? "stop" : "start";		
		sendCommandViaSshCmd(String.format("/etc/init.d/isc-dhcp-server6 %s", dhcp6));
		sendCommandViaSshCmd(String.format("/etc/init.d/isc-dhcp-server %s", dhcp4));		
		return ipVersion.equals(getDhcpConfigIpVersion());
	}
	
	public String getDhcpConfigIpVersion(){
		String ipVersion = "No DHCP";
		String status6 = sendCommandViaSshCmd("/etc/init.d/isc-dhcp-server6 status");
		String status4 = sendCommandViaSshCmd("/etc/init.d/isc-dhcp-server status");
		if(status6.contains("active (running)")){
			ipVersion = "6";
		}
		else if(status4.contains("active (running)")){
			ipVersion = "4";
		}
		return ipVersion;
	}
	
	public void closeDhcpConnection(){
		sshDhcp.disconnect();
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

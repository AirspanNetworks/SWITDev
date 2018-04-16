package Ercom.UeSimulator;



import java.net.MalformedURLException;

import com.aqua.sysobj.conn.CliCommand;
import com.aqua.sysobj.conn.CliConnectionImpl;
import com.aqua.sysobj.conn.LinuxDefaultCliConnection;

import Utils.StdOut;
import jsystem.framework.system.SystemObjectImpl;

/**
 * 
 *
 */
public abstract class Blade extends SystemObjectImpl {
	
	private static final String FIND_AND_REPLACE_with_SED = "sed -i 's/%s/%s/g' %s";

	private static final String READ_SYMBOLIC_LINK_AND_REPLACE_SUB_STRING = "readlink rf | sed 's/%s/%s/g'";
	
	/** Command execution time out */
	private static final int COMMAND_TIMEOUT = 1800000; // half an hour

	/** Prefix http:// */
	private static final String HTTP_PREFIX = "http://";

	/** The IP address. */
	public String ipAddress = "";

	/** The user name the component uses to login. */
	public String username = "root";

	/** The password the component uses to login. */
	public String password = "";

	/** This variable response for transmission */
	protected Transmittable transportLayer = null;

	/** SSH connection to LINUX on specific blade */
	protected CliConnectionImpl sshCmd = null;

	@Override
	public void init() throws Exception {
		super.init();
		this.transportLayer = ObjectFactory.getTransportLayer();
	}

	@Override
	public void close() {
		super.close();
	}

	/**
	 * The following function executes command on Blades using transportLayer.
	 * In this specific case the transportLayer implemented using XMLRPC
	 * protocol. In order to execute the command we need to create URL because
	 * the XMLRPC request send using POST HTTP method
	 * "POST //p0/testMgt HTTP/1.1". In this case "id = p0". After that we need
	 * to extract method name from command. URL, method name and parameters for
	 * this method will be send to transportLayer.
	 * 
	 * @param id - id of platform
	 * @param command - String command which will be executed
	 * @param params - parameters for command
	 * @return Object - response to executed command.
	 */
	protected Object sendCommand(String id, Command command, Object... params) {
		String url = createUrlString(id, command);
		Object val = null;
		try {
			val = transportLayer.execute(url, command.getMethodName(), params);
		}
		catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return val;
	}

	/**
	 * @param strCommand - command to perform. This command will be send to
	 * underlying LINUX machine via SSH connection
	 * 
	 * @return String - Response to executed command
	 * 
	 * @throws Exception
	 */
	protected String sendCommandViaSshCmd(String strCommand) throws Exception {
		CliCommand command = new CliCommand(strCommand);
		command.setTimeout(COMMAND_TIMEOUT);
		sshCmd.handleCliCommand(strCommand, command);
		return command.getResult();
	}
	
	
	protected boolean findAndReplaceString(String find ,String replace,String fPath){
		String command = String.format(FIND_AND_REPLACE_with_SED, find,replace,fPath);
		try {
			sendCommandViaSshCmd(command);
		}
		catch (Exception e) {
			StdOut.print2console("Failed to excute following command [ "+command+"] for L2 blade");
			StdOut.print2console("Possible reason : broken SSH connection with L2 Blade");
			StdOut.print2console(e.getMessage());
			StdOut.print2console(e.getStackTrace().toString());
			return false;
		}
		return true;
	}
	
	protected void changeDuplexModeOfTheBlade(String from, String to) throws Exception{
		String rflink = null;
		sendCommandViaSshCmd("cd /ercom/");
		String command = String.format(READ_SYMBOLIC_LINK_AND_REPLACE_SUB_STRING, from.toUpperCase(),to.toUpperCase());
		rflink = sendCommandViaSshCmd(command);
		String paresedResponce[] = rflink.split("\r\n");
		sendCommandViaSshCmd("rm -f rf");
		command = String.format("ln -s %s rf",paresedResponce[1]);
		sendCommandViaSshCmd(command);
	}
	
	
	protected String getCurrentBladeDuplexMode() throws Exception{
		String rflink = null;
		String duplexMode = "";
		sendCommandViaSshCmd("cd /ercom/");
		rflink = sendCommandViaSshCmd("readlink rf");
		if(rflink != null){
			if(rflink.contains("TDD")){
				duplexMode = "TDD";
			}else if(rflink.contains("FDD")){
				duplexMode = "FDD";
			}
		}
		return duplexMode;
	}
	
	// ---------------------------- private Methods ----------------------

	/**
	 * Initiate new SSH session to this blade.
	 * 
	 * @return CliConnection in this case this is SSH connection to current
	 * blade.
	 * 
	 * @throws Exception - if session not created
	 */
	protected CliConnectionImpl initSshSessionToBlade() throws Exception {
		CliConnectionImpl session = new LinuxDefaultCliConnection(
				this.ipAddress, this.username, this.password);
		session.setMaxIdleTime(COMMAND_TIMEOUT);
		session.init();
		return session;
	}

	/**
	 * Close SSh connection to this blade
	 */
	protected void closeSshSessionToBlade() {
		sshCmd.close();
	}

	private String createUrlString(String id, Command command) {
		StringBuilder url = new StringBuilder();
		url.append(HTTP_PREFIX);
		url.append(getIpAddress() + "//");
		url.append(id + "//");
		url.append(command.getAddress());
		return url.toString();
	}

	// ----------------------------- getters -----------------------------
	public String getIpAddress() {
		return ipAddress;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}

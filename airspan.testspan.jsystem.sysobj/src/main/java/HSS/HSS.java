package HSS;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import UE.UE;
import Utils.SSHConnector;
import jsystem.framework.system.SystemManagerImpl;
import jsystem.framework.system.SystemObjectImpl;

public class HSS extends SystemObjectImpl{

	String ipAddress;
	String username;
	String password;
	
	private SSHConnector sshHss = null;
	public String getIPAddress() {
		return ipAddress;
	}
	public void setIPAddress(String iPAddress) {
		ipAddress = iPAddress;
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
	
	//connect to the server
	public void connectHSS() throws Exception{
		sshHss = new SSHConnector(ipAddress,username,password);
		sshHss.initConnection();
		sshHss.sendCommand("/opt/lte/hss/bin/HSS_CLI_OP -t 7000 -g 1 -s " + EPC.EPC.getInstance().getS6aAddress(), 100);
	
	}
	
	public void disconnectHSS() throws Exception{
		sshHss.sendCommand("exit", 100);
		if(sshHss!=null){
			sshHss.disconnect();
			sshHss = null;
		}
	}
	
	public static HSS getInstance() throws Exception {
		return (HSS) SystemManagerImpl.getInstance().getSystemObject("HSS");
	}
	
	public HashMap<String, String> getMNCAndMCCFromHSS(UE ue) throws Exception{
		HashMap<String, String> values = new HashMap<>();
		String command = "imsi-info show " + ue.getImsi();
		String output= sshHss.sendCommand(command,100);
		Pattern r = Pattern.compile("MCC\\s*(\\d*)\\s*MNC\\s*(\\d*)");
		Matcher m = r.matcher(output);
		if (m.find()) {
			values.put("MCC", m.group(1));
			values.put("MNC", m.group(2));
		}
		return values;
	}
}

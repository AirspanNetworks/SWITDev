package Utils;

import jsystem.framework.system.SystemManagerImpl;
import jsystem.framework.system.SystemObjectImpl;

public class DebugFtpServer extends SystemObjectImpl{
	private static DebugFtpServer instance;
	private String debugFtpServerIP;
	private String debugFtpServerUser;
	private String debugFtpServerPassword;
	
	public DebugFtpServer(){
		debugFtpServerIP="100.100.0.70";
		debugFtpServerUser="Airspan";
		debugFtpServerPassword="Airspan";
	}
	
	public synchronized static DebugFtpServer getInstance() {
		if(instance == null){
			try {
				instance = (DebugFtpServer) SystemManagerImpl.getInstance().getSystemObject("DebugFtpServer");
			} catch (Exception e) {
				instance = new DebugFtpServer();
			}
		}
		return instance;
	}
	
	public String getDebugFtpServerIP() {
		return debugFtpServerIP;
	}
	public void setDebugFtpServerIP(String debugFtpServerIP) {
		this.debugFtpServerIP = debugFtpServerIP;
	}
	public String getDebugFtpServerUser() {
		return debugFtpServerUser;
	}
	public void setDebugFtpServerUser(String debugFtpServerUser) {
		this.debugFtpServerUser = debugFtpServerUser;
	}
	public String getDebugFtpServerPassword() {
		return debugFtpServerPassword;
	}
	public void setDebugFtpServerPassword(String debugFtpServerPassword) {
		this.debugFtpServerPassword = debugFtpServerPassword;
	}

	
}

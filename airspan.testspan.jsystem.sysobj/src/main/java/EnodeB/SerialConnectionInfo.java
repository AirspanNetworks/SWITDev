package EnodeB;

import jsystem.framework.system.SystemObjectImpl;

public class SerialConnectionInfo extends SystemObjectImpl{
	private String userName;
	private String serialPort;
	private String serialIP;
	
	public String getSerialPort() {
		return serialPort;
	}
	public void setSerialPort(String serialPort) {
		this.serialPort = serialPort;
	}
	public String getSerialIP() {
		return serialIP;
	}
	public void setSerialIP(String serialIP) {
		this.serialIP = serialIP;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
}

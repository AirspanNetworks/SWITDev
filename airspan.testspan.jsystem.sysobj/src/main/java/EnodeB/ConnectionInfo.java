package EnodeB;

import jsystem.framework.system.SystemObjectImpl;

public class ConnectionInfo extends SystemObjectImpl{
	private String ipAddress;
	private String userName;
	private String password;
	private String readCommunity;
	private String writeCommunity;
	public SerialConnectionInfo serialInfo;
	public SerialConnectionInfo[] danInfo;
	private int sshPort = 22;

	public int getSshPort() {
		return sshPort;
	}
	public void setSshPort(int sshPort) {
		this.sshPort = sshPort;
	}
	public String getIpAddress() {
		return ipAddress;
	}
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	public String getReadCommunity() {
		return readCommunity;
	}
	public void setReadCommunity(String readCommunity) {
		this.readCommunity = readCommunity;
	}
	public String getWriteCommunity() {
		return writeCommunity;
	}
	public void setWriteCommunity(String writeCommunity) {
		this.writeCommunity = writeCommunity;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
}
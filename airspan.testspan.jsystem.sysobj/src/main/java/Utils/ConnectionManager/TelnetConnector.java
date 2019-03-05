package Utils.ConnectionManager;

import Utils.ConnectionManager.*;
import systemobject.terminal.Telnet;


public class TelnetConnector implements IRemoteConnector {
	
	private ConnectionInfo connect_info;
	
	public TelnetConnector(ConnectionInfo connection_info) {
		this.connect_info = connection_info;
	}

	@Override
	public void initConnection() {
		// TODO Auto-generated method stub

	}

	@Override
	public void disconnect() {
		// TODO Auto-generated method stub

	}

	@Override
	public void reConnect() {
		// TODO Auto-generated method stub

	}

	@Override
	public String sendCommand(String command, int timeout) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String sendCommand(String command, int retries, int timeout) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String readCommandLine(long timeout, String finish) {
		// TODO Auto-generated method stub
		return null;
	}

//	@Override
//	public int getPort() {
//		// TODO Auto-generated method stub
//		return 0;
//	}
//
//	@Override
//	public void setPort(int port) {
//		// TODO Auto-generated method stub
//
//	}
//
//	@Override
//	public String getUserName() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public void setUserName(String userName) {
//		// TODO Auto-generated method stub
//
//	}
//
//	@Override
//	public String getPassword() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public void setPassword(String password) {
//		// TODO Auto-generated method stub
//
//	}
//
//	@Override
//	public boolean isConnected() {
//		// TODO Auto-generated method stub
//		return false;
//	}
//
//	@Override
//	public String getHost() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public void setHost(String host) {
//		// TODO Auto-generated method stub
//
//	}

}

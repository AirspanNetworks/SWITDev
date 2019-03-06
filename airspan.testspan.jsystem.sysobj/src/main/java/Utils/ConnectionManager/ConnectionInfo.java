package Utils.ConnectionManager;

import Utils.ConnectionManager.ConnectorTypes;

public class ConnectionInfo {
	
	public static int DEFAULT_SSH_PORT = 22;
	public static int DEFAULT_TELNET_PORT = 21;
	
	public String name;
	public String host;
	public int port;
	public String user;
	public String password;
	ConnectorTypes type;
	
	public ConnectionInfo(String name) {
		this.name = name;
	}
	
	public ConnectionInfo(String name, ConnectorTypes connect_type){
		this(name);
		this.type = connect_type;
		switch (type) {
			case SSH: 		this.port = DEFAULT_SSH_PORT; break;
			case Telnet:	this.port = DEFAULT_TELNET_PORT; break;
		}
	}
		
	public ConnectionInfo(String name,String host, int port, String user, String password, ConnectorTypes connect_type){
		this(name, connect_type);
		
		this.host = host;
		this.port = port;
		this.user = user;
		this.password = password;
	}
	
	@Override
	public String toString() {
		return String.format("Host: %s:%d, Login: %s:%s", host, port, user, password);
	}
}

package Utils.ConnectionManager;

import java.util.Collection;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public final class ConnectorContainer {

	private Map<String,Map<ConnectorTypes,IRemoteConnector>> collection;
	
	private ConnectorContainer() {
		collection = new HashMap<String, Map<ConnectorTypes,IRemoteConnector>>();
	}
	
	public void init_module(ConnectionInfo connection_info) {
		collection.put(connection_info.name, new HashMap<ConnectorTypes,IRemoteConnector>());
	}
	
	public IRemoteConnector init_connection(ConnectionInfo connection_info) {
		IRemoteConnector result_connector = null;
		switch (connection_info.type) {
			case SSH:
				result_connector = new SSHConnector(connection_info);
				break;
			case Telnet:
				result_connector = new TelnetConnector(connection_info);
				break;
		}
		
		return result_connector;
	}
	
	public final IRemoteConnector getConnnection(ConnectionInfo connection_info) {
		
		IRemoteConnector result_connector = null;
		
		if(this.collection.containsKey(connection_info.name)) {
			if(this.collection.get(connection_info.name).containsKey(connection_info.type)) {
				result_connector = this.collection.get(connection_info.name).get(connection_info.type);
			}
			else {
				result_connector = init_connection(connection_info);
				Map<ConnectorTypes, IRemoteConnector> sub_module = new HashMap<ConnectorTypes, IRemoteConnector>();  
				sub_module.put(connection_info.type, result_connector);
				this.collection.replace(connection_info.name, sub_module);
			}
		}
		else {
			result_connector = init_connection(connection_info);
			Map<ConnectorTypes, IRemoteConnector> sub_module = new HashMap<ConnectorTypes, IRemoteConnector>();  
			sub_module.put(connection_info.type, result_connector);
			this.collection.put(connection_info.name, sub_module);
		}
		return result_connector;
	}
	
	public static ConnectorContainer _instance;
	
	public static ConnectorContainer Current(ConnectorTypes type) {
		if(_instance == null) {
			_instance = new ConnectorContainer();
		}
		return _instance;
	}
}

package PowerControllers;

import jsystem.framework.IgnoreMethod;
import jsystem.framework.system.SystemObjectImpl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public abstract class PowerController  extends SystemObjectImpl{
	
	public PowerControllerPort[] ports;
	
	private String ipAddress;
	private String username;
	private String password;
	
	public abstract boolean powerOnPort(PowerControllerPort port) throws NullPointerException, IOException;
	public abstract boolean powerOffPort(PowerControllerPort port) throws NullPointerException, IOException;
	
	@IgnoreMethod
	public PowerControllerPort getPort(String portName) {
		for (PowerControllerPort port : ports) {
			if (port.getPort().equals(portName))
				return port;
		}
		return null;
	}
	
	// Return the port description
	@IgnoreMethod
	public PowerControllerPort getDescription(String portDescription) {
		for (PowerControllerPort port : ports) {
			if (port.getDescription().equals(portDescription))
				return port;
		}
		return null;
	}

	
	/**
	 * This function receive string which represent the device name\description and returns the
	 * power controller ports.
	 * </p>
	 * @param port - string device name or description
	 * @return List<PowerControllerPort> - power controller port
	 */
	public List<PowerControllerPort> getPortFromNameOrDescription(String portOrDescription) {
		List<PowerControllerPort> actualPorts = new ArrayList<PowerControllerPort>();
		PowerControllerPort Port = getPort(portOrDescription);
		if(Port != null){
			actualPorts.add(Port);
		}else{			
			for (PowerControllerPort port : ports) {
				if (port.getDescription().equals(portOrDescription)){
					actualPorts.add(port);
				}
			}
		}
		return actualPorts;
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

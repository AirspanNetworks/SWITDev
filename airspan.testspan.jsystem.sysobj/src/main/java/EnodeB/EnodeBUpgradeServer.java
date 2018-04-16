package EnodeB;

import Netspan.API.Enums.ServerProtocolType;
import jsystem.framework.system.SystemObjectImpl;

public class EnodeBUpgradeServer extends SystemObjectImpl {
	/** The servers name. */
	private String name;
	
	/** The servers protocol. */
	private ServerProtocolType protocolType;
	
	/** The server ip. */
	private String upgradeServerIp;
	
	/** The server ip. */
	private String upgradeServerIpv6;
	
	/** The upgrade user. */
	private String upgradeUser;
	
	/** The upgrade password. */
	private String upgradePassword;
	
	/**
	 * @return the name
	 */
	public String getUpgradeServerName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setUpgradeServerName( String name ) {
		this.name = name;
	}
	
	/**
	 * @return the protcolType
	 */
	public ServerProtocolType getUpgradeServerProtocolType() {
		return protocolType;
	}
	/**
	 * @param protcolType the protcolType to set
	 */
	public void setProtocolType( ServerProtocolType protcolType ) {
		this.protocolType = protcolType;
	}
	
	
	public String getUpgradeServerIp() {
		return upgradeServerIp;
	}
	public void setUpgradeServerIp(String upgradeServerIp) {
		this.upgradeServerIp = upgradeServerIp;
	}
	/**
	 * @return the upgradeUser
	 */
	public String getUpgradeUser() {
		return upgradeUser;
	}
	/**
	 * @param upgradeUser the upgradeUser to set
	 */
	public void setUpgradeUser( String upgradeUser ) {
		this.upgradeUser = upgradeUser;
	}
	/**
	 * @return the upgradePassword
	 */
	public String getUpgradePassword() {
		return upgradePassword;
	}
	
	
	/**
	 * @param upgradePassword the upgradePassword to set
	 */
	public void setUpgradePassword( String upgradePassword ) {
		this.upgradePassword = upgradePassword;
	}
	
	public String getUpgradeServerIpv6() {
		return upgradeServerIpv6;
	}
	
	public void setUpgradeServerIpv6(String upgradeServerIpv6) {
		this.upgradeServerIpv6 = upgradeServerIpv6;
	}
	public void setUpgradeServerObject(String name, String serverIpAddress, ServerProtocolType protocolType, String user, String password) {
		this.name = name;
		this.upgradeServerIp = serverIpAddress;
		this.protocolType = protocolType;
		this.upgradeUser = user;
		this.upgradePassword = password;
	}


}

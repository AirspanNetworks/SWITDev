package EnodeB;

import Netspan.API.Enums.HardwareCategory;
import Netspan.API.Enums.ServerProtocolType;
import jsystem.framework.system.SystemObjectImpl;

public class EnodeBUpgradeImage  extends SystemObjectImpl{
	/**
	 * @author Moran Goldenberg
	 */
	private String name; 
	private HardwareCategory hardwareCategory;
	private String upgradeServerName;
	private ServerProtocolType protocolType;
	private String buildPath;
	private String version;
	private String imageType;
	
	public EnodeBUpgradeImage() {
		super();
	}
	public EnodeBUpgradeImage(String name, HardwareCategory hardwareCategory, String upgradeServerName,
			ServerProtocolType protocolType, String buildPath, String version) {
		super();
		this.name = name;
		this.hardwareCategory = hardwareCategory;
		this.upgradeServerName = upgradeServerName;
		this.protocolType = protocolType;
		this.buildPath = buildPath;
		this.version = version;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	public HardwareCategory getHardwareCategory() {
		return hardwareCategory;
	}
	public void setHardwareCategory(HardwareCategory hardwareCategory) {
		this.hardwareCategory = hardwareCategory;
	}
	public String getUpgradeServerName() {
		return upgradeServerName;
	}
	public void setUpgradeServerName(String upgradeServerName) {
		this.upgradeServerName = upgradeServerName;
	}
	public ServerProtocolType getProtocolType() {
		return protocolType;
	}
	public void setProtocolType(ServerProtocolType protocolType) {
		this.protocolType = protocolType;
	}
	public String getBuildPath() {
		return buildPath;
	}
	public void setBuildPath(String buildPath) {
		this.buildPath = buildPath;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getImageType() {
		return imageType;
	}
	public void setImageType(String imageType) {
		this.imageType = imageType;
	}
}

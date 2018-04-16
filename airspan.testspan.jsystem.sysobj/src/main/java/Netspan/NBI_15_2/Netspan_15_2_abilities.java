package Netspan.NBI_15_2;

import EnodeB.EnodeB;

public interface Netspan_15_2_abilities {
	public boolean getAdvancedConfigurationProfile(EnodeB node, String cloneFromName);
	public boolean getManagementProfile(EnodeB node, String cloneFromName);
	public boolean getMobilityProfile(EnodeB node, String cloneFromName);
	public boolean getSecurityProfile(EnodeB node, String cloneFromName);
	public boolean getNetworkProfile(EnodeB node, String cloneFromName);
	public boolean getRadioProfile(EnodeB node, String cloneFromName);
	public boolean getSyncProfile(EnodeB node, String cloneFromName);
	public boolean getSonProfile(EnodeB node, String cloneFromName);
}

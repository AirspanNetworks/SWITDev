package Netspan.NBI_15_2;

import EnodeB.EnodeB;

public interface Netspan_15_2_abilities {
	boolean getAdvancedConfigurationProfile(EnodeB node, String cloneFromName);
	boolean getManagementProfile(EnodeB node, String cloneFromName);
	boolean getMobilityProfile(EnodeB node, String cloneFromName);
	boolean getSecurityProfile(EnodeB node, String cloneFromName);
	boolean getNetworkProfile(EnodeB node, String cloneFromName);
	boolean getRadioProfile(EnodeB node, String cloneFromName);
	boolean getSyncProfile(EnodeB node, String cloneFromName);
	boolean getSonProfile(EnodeB node, String cloneFromName);
}

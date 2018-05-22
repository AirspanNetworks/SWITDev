package Netspan;

public enum EnbProfiles {
	Cell_Advanced_Profile("cellAdvanced"),
	Radio_Profile("radio"),
	Mobility_Profile("mobility"),
	System_Default_Profile("systemDefault"),
	EnodeB_Advanced_Profile("enodebAdvanced"),
	Network_Profile("network"),
	Sync_Profile("sync"),
	Security_Profile("security"),
	Son_Profile("son"),
 	Management_Profile("management"),
 	MultiCell_Profile("multiCell"),
	Neighbour_Management_Profile("neighbourManagement"),
	Relay_Profile("relay");
    
    private final String value;

    EnbProfiles(String v) {
        value = v;
    }

    public String value() {
        return value;
    }
}

package Netspan;

public enum CellProfiles {
	Cell_Advanced_Profile("cellAdvanced"),
	Radio_Profile("radio"),
	Mobility_Profile("mobility"),
	Embms_Profile("embms"),
	Traffic_Management_Profile("TrafficManagement"),
	Call_Trace_Profile("CallTrace");
    
    private final String value;

    CellProfiles(String v) {
        value = v;
    }

    public String value() {
        return value;
    }
}

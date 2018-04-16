package Netspan;


public enum NBIVersion {
	
	 	NBI_14_0("NBI_14_0"),
	    NBI_14_50("NBI_14_50");
	    
	    private final String value;

	    NBIVersion(String v) {
	        value = v;
	    }

	    public String value() {
	        return value;
	    }

}

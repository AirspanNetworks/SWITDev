package testsNG.Actions.Utils;

public enum StreamsMode {
	UL_PTP("UL_PTP"), UL_PTMP("UL_PTMP"), DL_PTP("DL_PTP"), DL_PTMP("DL_PTMP"), BIDI_PTP(
			"BIDI_PTP"), BIDI_PTMP("BIDI_PTMP");
	
	private String name; 
	
	StreamsMode(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
	}
}

package Netspan;

public enum NBIVersion {
	NBI_14_5("14_5"), 
	NBI_15_2("15_2"), 
	NBI_15_5("15_5"), 
	NBI_16_0("16_0"), 
	NBI_16_5("16_5");

	private final String value;

	NBIVersion(String v) {
		value = v;
	}

	public String value() {
		return value;
	}

}

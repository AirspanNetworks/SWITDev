package Netspan;

public enum NBIVersion {
	NBI_14_5("14_5"), NBI_15_2("15_2"), NBI_15_5("15_5"), NBI_16_0("16_0"), NBI_16_5("16_5"), NBI_17_0("17_0");

	private final String value;

	NBIVersion(String v) {
		value = v;
	}

	public String value() {
		return value;
	}

	public static NBIVersion fromValue(String v) {
		for (NBIVersion c : NBIVersion.values()) {
			if (c.value.equals(v)) {
				return c;
			}
		}
		throw new IllegalArgumentException(v);
	}

}

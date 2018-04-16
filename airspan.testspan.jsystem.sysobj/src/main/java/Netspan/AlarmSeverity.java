package Netspan;

public enum AlarmSeverity {
	CRITICAL,MAJOR,MINOR,WARNING,NORMAL;
	
	public static AlarmSeverity translation(Integer i){
		switch(i) {
		case 3: return CRITICAL;
		case 4: return MAJOR;
		case 5: return MINOR;
		case 6: return WARNING;
		case 7: return NORMAL;
		default: throw new AssertionError("Unknown op: " + i);
		}
	}
	
	public static AlarmSeverity translation(String i){
		switch(i.toUpperCase()) {
		case "CRITICAL": return CRITICAL;
		case "MAJOR": return MAJOR;
		case "MINOR": return MINOR;
		case "WARNING": return WARNING;
		case "NORMAL": return NORMAL;
		default: throw new AssertionError("Unknown op: " + i);
		}
	}
	
	public static String toString(AlarmSeverity a){
		switch(a) {
		case CRITICAL: return "CRITICAL";
		case MAJOR: return "MAJOR";
		case MINOR: return "MINOR";
		case WARNING: return "WARNING";
		case NORMAL: return "NORMAL";
		default: throw new AssertionError("AlarmSeverity enum: Unknown " + a);
		}
	}
}

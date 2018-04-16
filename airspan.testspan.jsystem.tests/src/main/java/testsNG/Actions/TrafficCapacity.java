package testsNG.Actions;

public enum TrafficCapacity {
	FULLTPT("FULLTPT"),LOWTPT("LOWTPT"),CUSTOM("CUSTOM");
	String type;
	
	public String getType(){
		return type;
	}
	
	TrafficCapacity(String trafficCapacity){
		type = trafficCapacity;
	}
}

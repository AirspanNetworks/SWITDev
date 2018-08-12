package UeSimulator.Amarisoft;

import jsystem.framework.system.SystemObjectImpl;

public class AmarisoftGroup extends SystemObjectImpl{
	static String groupName;
	static String[] imsiStartList;
	static String[] imsiStopList;
	
	
	public static String checkIfImsiIsInGroup(Long imsi) {
		for (int i = 0; i < imsiStartList.length; i++) {
			Long startImsi = new Long(imsiStartList[i]);
			Long stopImsi = new Long(imsiStopList[i]);
			for (Long UEimsi = startImsi; imsi <= stopImsi ; UEimsi++) {
				if (UEimsi.equals(imsi)) {
					return groupName;
				}
			}
		}
		return "";
	}
}

package UeSimulator.Amarisoft;

import java.util.ArrayList;

import EnodeB.EnodeB;
import Utils.SysObjUtils;
import jsystem.framework.system.SystemObjectImpl;

public class AmarisoftGroup extends SystemObjectImpl{
	private String groupName;
	private String[] imsiStart;
	private String[] imsiStop;
	
	
	public String getGroupName() {
		return groupName;
	}


	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}


	public String[] getImsiStart() {
		return imsiStart;
	}

	public void setImsiStart(String imsiStart) {
		
		this.imsiStart = imsiStart.split(",");
	}


	public String[] getImsiStop() {
		return imsiStop;
	}


	public void setImsiStop(String imsiStop) {
		this.imsiStop = imsiStop.split(",");
	}

	public String checkIfImsiIsInGroup(Long imsi) {
		for (int i = 0; i < imsiStart.length; i++) {
			Long startImsi = new Long(imsiStart[i]);
			Long stopImsi = new Long(imsiStop[i]);
			for (Long UEimsi = startImsi; UEimsi <= stopImsi ; UEimsi++) {
				if (UEimsi.equals(imsi)) {
					return groupName;
				}
			}
		}
		return "";
	}
	
}

package UE;

import java.util.ArrayList;

import UeSimulator.Amarisoft.AmariSoftServer;
import Utils.GeneralUtils;

public class AmarisoftUE extends UE implements Comparable{

	public int ueId;
	public ArrayList<String> groupName;
	private AmariSoftServer server;
	
	public AmarisoftUE() {
		super("AmarisoftUE");
	}
	
	public AmarisoftUE(int ueId, ArrayList<String> groupName, AmariSoftServer server) {
		this();
		this.ueId = ueId;
		this.groupName = groupName;
		this.server = server;
		setName("AmarisoftUE" + (10000 + ueId));
	}

	@Override
	public boolean start() {
		return server.uePowerOn(ueId);
	}

	@Override
	public boolean reboot() {
		boolean flag = server.uePowerOff(ueId);
		GeneralUtils.unSafeSleep(2000);
		flag = flag && server.uePowerOn(ueId);
		return flag;
	}

	@Override
	public boolean stop() {
		return server.uePowerOff(ueId);
	}

	@Override
	public String getVersion() {
		return server.getVersion();
	}

	@Override
	public String getBandWidth() {
		String ans = "";
		int numOfRbs = server.getUeNumOfrb(ueId);
		if (numOfRbs == 6) 
			return "1.4";		
		else
			ans = (numOfRbs/5) + "";
		return ans;
	}

	@Override
	public String getUEStatus() {
		return server.getUeStatus(ueId);
	}

	@Override
	public String getDuplexMode() {
		return server.getUeConnectedDuplexMode(ueId);
	}

	@Override
	public int getRSRP(int index) {
		Double ans = server.getUeRsrp(ueId);
		return ans.intValue();		
	}
	
	@Override
	public int getPCI() {
		return server.getUeConnectedPCI(ueId);
		
	}
	
	@Override
	public boolean setAPN(String apnName) {
		GeneralUtils.printToConsole("AmarisoftUE does not have setAPN method");
		return false;
	}

	public boolean rrcReestablishment(){
		return server.RRC_Reestablishment(ueId);
	}

	@Override
	public int compareTo(Object o) {
		// TODO Auto-generated method stub
		return ueId - ((AmarisoftUE)o).ueId;
	}
}

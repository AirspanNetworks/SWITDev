package UE;

import UeSimulator.Amarisoft.AmariSoftServer;
import Utils.GeneralUtils;
import Utils.Snmp.MibReader;
import Utils.Snmp.SNMP;
import jsystem.framework.report.Reporter;

public class AmarisoftUE extends UE{

	private int ueId;
	private AmariSoftServer server;
	
	public AmarisoftUE() {
		super("AmarisoftUE");
	}
	
	public AmarisoftUE(int ueId, AmariSoftServer server) {
		this();
		this.ueId = ueId;
		this.server = server;
		setName("AmarisoftUE" + (10000 + ueId));
	}
	
	@Override
	public void init() throws Exception {
		super.init();
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
		GeneralUtils.printToConsole("AmarisoftUE does not have reboot method.");
		return GeneralUtils.ERROR_VALUE+"";
	}

	@Override
	public String getBandWidth() {
		GeneralUtils.printToConsole("AmarisoftUE does not have get Band width method.");
		return null;
	}

	@Override
	public String getUEUlFrequency() {
		GeneralUtils.printToConsole("AmarisoftUE does not have get UL freq method.");
		return null;
	}

	@Override
	public String getUEDlFrequency() {
		GeneralUtils.printToConsole("AmarisoftUE does not have get DL freq method.");
		return null;
	}

	@Override
	public String getUEStatus() {
		GeneralUtils.printToConsole("AmarisoftUE does not have reboot method.");
		return GeneralUtils.ERROR_VALUE+"";
	}

	@Override
	public String getDuplexMode() {
		GeneralUtils.printToConsole("AmarisoftUE does not have get duplex mode method.");
		return null;
	}

	@Override
	public int getRSRP(int index) {
		GeneralUtils.printToConsole("AmarisoftUE does not have getRSRP method.");
		return GeneralUtils.ERROR_VALUE;		
	}
	
	@Override
	public int getPCI() {
		GeneralUtils.printToConsole("AmarisoftUE does not have getPCI method.");
		return GeneralUtils.ERROR_VALUE;
		
	}
	
	@Override
	public boolean setAPN(String apnName) {
		GeneralUtils.printToConsole("AmarisoftUE does not have setAPN method");
		return false;
	}

}

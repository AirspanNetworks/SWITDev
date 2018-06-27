package UE;

import jsystem.framework.report.Reporter;

public class VirtualUE extends UE{

	public VirtualUE() {
		super("UESimulator");
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean start() {
		// TODO Auto-generated method stub
		return false;
		
	}

	@Override
	public boolean reboot() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean stop() {
		// TODO Auto-generated method stub
		return false;
		
	}

	@Override
	public String getVersion() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getBandWidth() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getUEStatus() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDuplexMode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getRSRP(int index) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public int getPCI() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public boolean setAPN(String apnName) {
		report.report("Could not perform setAPN method, VirtualUE does not support SNMP", Reporter.WARNING);
		return false;
	}

}

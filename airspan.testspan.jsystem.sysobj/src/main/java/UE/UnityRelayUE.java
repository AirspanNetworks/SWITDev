package UE;

import Utils.Snmp.MibReader;
import Utils.Snmp.SNMP;
import jsystem.framework.report.Reporter;

public class UnityRelayUE extends UE{

	protected SNMP snmp;
	
	public UnityRelayUE() {
		super("UnityRelayUE");
		}

	
	@Override
	public void init() throws Exception {
		super.init();
		initSNMP();
	}
	
	public void initSNMP() {
        if (getLanIpAddress() != null) {
            snmp = new SNMP("public", "private", getLanIpAddress(), "V2",getSNMPPort());
            snmp.initiatePDU();
        }
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
		String oid = MibReader.getInstance().resolveByName("asCmnSwStsRunningVersion");
		String result = null;
		result = snmp.get(oid);
		return result;
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

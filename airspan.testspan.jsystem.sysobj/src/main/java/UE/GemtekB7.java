package UE;

import Utils.GeneralUtils;
import Utils.Snmp.MibReader;
import Utils.Snmp.SNMP;
import jsystem.framework.report.Reporter;

public class GemtekB7 extends UE{
	protected SNMP snmp;
	private String user = "operator";
	private String password = "operator";

	public GemtekB7() {
		super("GemtekB7");
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

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public boolean start() {
		GeneralUtils.printToConsole("GEMTEK Band 7 UE does not have start method.");
		return false;
	}

	@Override
	public boolean reboot() {
		String oid = MibReader.getInstance().resolveByName("rebootGB7");
		boolean flag = true;
		try {
			flag &= snmp.snmpSet(oid, 1);
			GeneralUtils.printToConsole((String.format("Restarting ue \"%s\" by SNMP.", this.getName())));
		} catch (Exception e) {
			report.report("can't send command reboot snmp protocol",Reporter.WARNING);
			e.printStackTrace();
			flag = false;
		}
		return flag;
	}

	@Override
	public boolean stop() {
		GeneralUtils.printToConsole("GEMTEK Band 7 UE does not have stop method.");
		return false;
	}

	@Override
	public String getVersion() {
		String oid = MibReader.getInstance().resolveByName("swVerGB7");
		String result = null;
		result = snmp.get(oid);
		GeneralUtils.printToConsole("getting version from ue : "+this.getName());
		return result;
	}

	@Override
	public String getBandWidth() {
		GeneralUtils.printToConsole("GEMTEK Band 7 UE does not have get Band width method.");
		return null;
	}

	@Override
	public String getUEStatus() {
		String oid = MibReader.getInstance().resolveByName("upTimeGB7");
		String upTime = null;
		try{
			upTime = snmp.get(oid);
			GeneralUtils.printToConsole("up time for UE : "+upTime);
		}catch(Exception e){
			report.report("can't send command get upTime snmp protocol",Reporter.WARNING);
			e.printStackTrace();
		}
		return upTime;
	}

	@Override
	public String getDuplexMode() {
		GeneralUtils.printToConsole("GEMTEK Band 7 UE does not have get duplex mode method.");
		return null;
	}

	@Override
	public int getRSRP(int index) {
		String oid = MibReader.getInstance().resolveByName("rsrpGB7");
		String rsrp = "";
		try{
			rsrp = snmp.get(oid);
			int ret = Integer.parseInt(rsrp);
			if(ret<=-44 && ret>=-139){
				return ret;
			}
			GeneralUtils.printToConsole("rsrp for UE : "+rsrp);
			return GeneralUtils.ERROR_VALUE;
		}catch(Exception e){
			e.printStackTrace();
			report.report("can't send command get rsrp snmp protocol",Reporter.WARNING);
			return GeneralUtils.ERROR_VALUE;
		}
	}
	
	@Override
	public int getPCI() {
		String oid = MibReader.getInstance().resolveByName("pciGB7");
		String pci = "";
		try{
			pci = snmp.get(oid);
			GeneralUtils.printToConsole("pci for UE : "+pci);
			return Integer.parseInt(pci);
		}catch(Exception e){
			e.printStackTrace();
			report.report("can't send command get pci snmp protocol",Reporter.WARNING);
			return GeneralUtils.ERROR_VALUE;
		}
	}
	
	@Override
	public boolean setAPN(String apnName) {
		report.report("Could not perform setAPN method, Band 7 UE does not support SNMP", Reporter.WARNING);
		return false;
	}

}

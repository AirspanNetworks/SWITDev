package UE;

import jsystem.framework.report.Reporter;

import Utils.GeneralUtils;
import Utils.Snmp.SNMP;

public class BecUE extends UE {
	
	protected SNMP snmp;
	
	public BecUE() {
		super("BecUE");
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
	public boolean stop(){
		report.report("Could not perform stop method, BecUE does not support SNMP",Reporter.WARNING);
		return true;
		/*
		String oid = MibReader.getInstance().resolveByName("becUeStop");
		try{
			return snmp.snmpSet(oid, 1);
		}catch(Exception e){
			report.report("can't send command snmp protocol");
			e.printStackTrace();
		}
		return false;
		*/
	}
	
	@Override
	public boolean start(){
		report.report("Could not perform start method, BecUE does not support SNMP",Reporter.WARNING);
		return true;
		/*
		String oid = MibReader.getInstance().resolveByName("becUeStart");
		try{
			return snmp.snmpSet(oid, 1);
		}catch(Exception e){
			report.report("can't send command snmp protocol");
			e.printStackTrace();
		}
		return false;
		*/
	}
	
	@Override
	public boolean reboot(){
		report.report("Could not perform reboot method, BecUE does not support SNMP",Reporter.WARNING);
		return true;
		/*
		String oid = MibReader.getInstance().resolveByName("becUeReboot");
		boolean flag = true;
		try{
			flag &= snmp.snmpSet(oid, 1);
		}catch(Exception e){
			report.report("can't send command snmp protocol");
			e.printStackTrace();
			flag = false;
		}
		return flag;
		*/
	}

	@Override
	public String getVersion(){
		report.report("Could not perform getVersion method, BecUE does not support SNMP",Reporter.WARNING);
		return GeneralUtils.ERROR_VALUE + "";
	}

	@Override
	public String getDuplexMode(){
		report.report("Could not perform getDuplexMode method, BecUE does not support SNMP",Reporter.WARNING);
		return GeneralUtils.ERROR_VALUE + "";
	}
	
	@Override
	public String getUEStatus(){
		report.report("Could not perform getUEStatus method, BecUE does not support SNMP",Reporter.WARNING);
		return GeneralUtils.ERROR_VALUE + "";
	}

	@Override
	public String getBandWidth(){
		report.report("Could not perform getBandWidth method, BecUE does not support SNMP",Reporter.WARNING);
		return GeneralUtils.ERROR_VALUE + "";
	}

	@Override
	public int getRSRP(int index) {
		report.report("Could not perform getRSRP method, BecUE does not support SNMP",Reporter.WARNING);
		return GeneralUtils.ERROR_VALUE;
	}
	
	@Override
	public int getPCI() {
		report.report("Could not perform getPCI method, BecUE does not support SNMP",Reporter.WARNING);
		return GeneralUtils.ERROR_VALUE;
	}
	
	@Override
	public boolean setAPN(String apnName) {
		report.report("Could not perform setAPN method, BecUE does not support SNMP", Reporter.WARNING);
		return false;
	}
}

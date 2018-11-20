package UE;

import Utils.Snmp.SNMP;

import Utils.GeneralUtils;
import Utils.Snmp.MibReader;

public class GemtekUE extends UE {

	protected SNMP snmp;
	private String user = "administrator";
	private String password = "administrator";
	public GemtekUE() {
		super("Gemtek");
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
		String oid = MibReader.getInstance().resolveByName("pmpDevCpeCellSelectionStartStopLte");
		try{
			return snmp.snmpSet(oid, 1);
		}catch(Exception e){
			report.report("can't send command snmp protocol");
			e.printStackTrace();
		}
		return false;
	}
	
	@Override
	public boolean start(){
		String oid = MibReader.getInstance().resolveByName("pmpDevCpeCellSelectionStartStopLte");
		try{
			return snmp.snmpSet(oid, 0);
		}catch(Exception e){
			report.report("can't send command snmp protocol");
			e.printStackTrace();
		}
		return false;
	}
	
	@Override
	public boolean reboot(){
		String oid = MibReader.getInstance().resolveByName("pmpDevCpeRestoreDefaultReboot");
		boolean flag = true;
		try {
			flag &= snmp.snmpSet(oid, 1);
			report.report((String.format("Restarting ue \"%s\" by SNMP.", this.getName())));
		} catch (Exception e) {
			report.report("can't send command snmp protocol");
			e.printStackTrace();
			flag = false;
		}
		return flag;
	}
	
	/**
	 * @author sshahaf created on 18/10/2016
	 * @return rx current result from UE
	 */
	public String getCurrentRx(){
		String oid = MibReader.getInstance().resolveByName("pmpDevCpeSystemUplinkCurrentDataRate");
		String result =null;
			 result = snmp.get(oid);
		return result;
	}
	//--------------------------------------------------------------------------------------------//
	/**
	 * @author Shahaf Shuhamy , last update : 31/10/16
	 * 
	 * @return UE version with Gemtek SNMP - "1.4.2"
	 */
	@Override
	public String getVersion(){
		 //1.3.6.1.4.1.17713.20.2.7.6.2
		String oid = MibReader.getInstance().resolveByName("pmpDevCpeSnmpMibsVersion");
		String result = null;
		result = snmp.get(oid);
		return result;
	}
	
	/**
	 * @author Shahaf Shuhamy ,last update : 31/10/16
	 * @return true if UE needs reboot in order to function correctly and false otherwise.
	 */
	public boolean isRebootRequired(){
		String oid = MibReader.getInstance().resolveByName("pmpDevCpeSnmpRebootRequirement");
		String result = null;
		result = snmp.get(oid);

		return result.equals("1");
	}
	
	/**
	 * @author Shahaf Shuhamy ,last update : 31/10/16
	 * @return UE duplex Mode - TDD/FDD
	 */
	public String getDuplexMode(){
		String oid = MibReader.getInstance().resolveByName("pmpDevCpeEarfcnAndFrequencySettingDivisionDuplexingMode");
		String result = null;
		result = snmp.get(oid);
		return result;
	}
	
	/**
	 *  READ only
	 * @author Shahaf Shuhamy ,last update : 31/10/16
	 * @return one of the following status:
	 * Device Init,
		SIM Detecting,
		Device Ready,
		Search,
		Network Entry,
		Attached,
		Idle,
		No Signal,
	 */
	public String getUEStatus(){
		String oid = MibReader.getInstance().resolveByName("pmpDevCpeLteCpeState");
		String result = null;
		result = snmp.get(oid);
		return result;
	}
	
	/**
	 * @author Shahaf Shuhamy ,last update : 31/10/16
	 * @return bandwidth /1000
	 */
	public String getBandWidth(){
		String oid = MibReader.getInstance().resolveByName("pmpDevCpeLteBandwidth");
		String result = null;
		result = snmp.get(oid);
		int bandWidth = Integer.valueOf(result);
		if(bandWidth /1000 >= 1000){
		return String.valueOf(bandWidth/1000);
		}
		return result;
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
	public int getRSRP(int index) {
		String oid = MibReader.getInstance().resolveByName("pmpDevCpeLteRSRP");
		String rsrp = "";
		if (index==0) {
			oid = oid + ".6.0";
		}else if (index==1) {
			oid = oid + ".7.0";
		}else{
			report.report("Error in getting RSRP from UE "+getLanIpAddress()+", index must be 0 or 1");
			return GeneralUtils.ERROR_VALUE;
		}
		try{
			rsrp = snmp.get(oid);
			int ret = Integer.parseInt(rsrp);
			if(ret<=-44 && ret>=-139){
				return ret;
			}
			return GeneralUtils.ERROR_VALUE;
		}catch(Exception e){
			e.printStackTrace();
			report.report("Error in getting RSRP from UE "+getLanIpAddress());
			return GeneralUtils.ERROR_VALUE;
		}
	}
	
	@Override
	public int getPCI() {
		String oid = MibReader.getInstance().resolveByName("pmpDevCpeLtePCID");
		String pci = "";
		try{
			pci = snmp.get(oid);
			return Integer.parseInt(pci);
		}catch(Exception e){
			e.printStackTrace();
			report.report("Error in getting PCI from UE");
			return GeneralUtils.ERROR_VALUE;
		}
	}
	
	public boolean changeEARFCN(int earfcn){
		boolean result = true;
		boolean setResult = setEarfcn(earfcn); 
		Integer demiEarfcn = getEarfcn();
			if(!setResult){
				result = false;
			}
			
			if(demiEarfcn != earfcn){
				GeneralUtils.printToConsole("value from SNMP isn't matching the one method tried to set!");
				result = false;
			}
			
		return result;
	}
	
	public Integer getEarfcn(){
		String oid = MibReader.getInstance().resolveByName("earfcnUE");
		String earfcn = "";
		try{
			earfcn = snmp.get(oid);
			report.report("SNMP returned Value : "+earfcn);
			return Integer.parseInt(earfcn);
		}catch(Exception e){
			e.printStackTrace();
			report.report("Error in getting earfcn from UE");
			return GeneralUtils.ERROR_VALUE;
		}
	}
	
	public boolean setEarfcn(Integer earfcn){
		boolean result = true;
		String oid = MibReader.getInstance().resolveByName("earfcnUE");
		try{
			report.report("trying to set Value : "+earfcn);
			String strearfc = String.valueOf(earfcn);
			snmp.snmpSet(oid, strearfc);
		}catch(Exception e){
			e.printStackTrace();
			report.report("Error in setting earfcn from UE");
			result = false;
		}
		return result;
	}

	@Override
	public boolean setAPN(String apnName) {
		String oid = MibReader.getInstance().resolveByName("pmpDevCpeLteAPNAttach");
		String oid1 = MibReader.getInstance().resolveByName("pmpDevCpeMobileNetworkAPNAttach");
		boolean flag = true;
		try {
			report.report((String.format("Setting ue \"%s\" APN to %s by SNMP.", this.getName(), apnName)));
			flag &= snmp.snmpSet(oid, 1);
			oid = MibReader.getInstance().resolveByName("pmpDevCpeLteAPNName");
			flag &= snmp.snmpSet(oid, apnName);
			if (!flag){
				flag = snmp.snmpSet(oid1, 1);
				oid1 = MibReader.getInstance().resolveByName("pmpDevCpeMobileNetworkAPNName");
				flag &= snmp.snmpSet(oid1, apnName);
			}
			return snmp.get(oid).equals(apnName) || snmp.get(oid1).equals(apnName); 
		} catch (Exception e) {
			report.report("can't send command snmp protocol");
			e.printStackTrace();
			flag = false;
		}
		return flag;
	}

}

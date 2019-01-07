package testsNG.PlatformAndSecurity.IPSec;

import java.net.Socket;
import java.util.ArrayList;

import org.junit.Test;
import EnodeB.EnodeB;
import Utils.GeneralUtils;
import Utils.SysObjUtils;
import Utils.TunnelManager;
import jsystem.framework.ParameterProperties;
import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;
import testsNG.TestspanTest;

public class P0 extends TestspanTest {

	public ArrayList<EnodeB> duts;
	private TunnelManager tunnelManager;

	@Override
	public void init() throws Exception {
		enbInTest = new ArrayList<>();
		for(EnodeB dut : duts){
			dut.setIpsecTunnelEnabled("true");
			enbInTest.add(dut);
		}
		TunnelManager.seteNodeBInTestList(enbInTest);
		super.init();
		
	}
	
	/**
	 * Open IPSec Tunnel.
	 * 
	 * @author ayefet
	 * 	 */
	@Test
	@TestProperties(name = "Open IPSec Tunnel", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void openIPSecTunnel() {
		try {
			tunnelManager = TunnelManager.getInstance(duts, report);
			tunnelManager.openTunnel();
		} catch (Exception e) {
			e.printStackTrace();
		}
		for(EnodeB dut : duts){
			GeneralUtils.startLevel(dut.getName() + ": Check if all ports are open.");
			if(!isPortsOpen(dut)){
				report.report("Not all port are open (22, 80, 161)", Reporter.FAIL);
			}
			GeneralUtils.stopLevel();
		}
	}
	
	/**
	 * Close IPSec Tunnel.
	 * 
	 * @author ayefet
	 */
	@Test
	@TestProperties(name = "Close IPSec Tunnel", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void closeIPSecTunnel() {
		if(tunnelManager != null){
			tunnelManager.closeTunnel();
		}
	}
	
	/*Test infra functions*/

	public ArrayList<EnodeB> getDuts() {
		return this.duts;
	}
	
	@ParameterProperties(description = "DUTs")
	public void setDuts(String dutsStr) {
		duts = new ArrayList<EnodeB>();
		String[] dutNames = dutsStr.trim().split(",");
		for(int i = 0; i < dutNames.length; i++){
			GeneralUtils.printToConsole("Load DUT" + i + " " + dutNames[i]);
			this.duts.add((EnodeB) SysObjUtils.getInstnce().initSystemObject(EnodeB.class, false, dutNames[i]).get(0));
		}
	}

	private boolean isPortsOpen(EnodeB eNodeB){
		boolean isOpen = true;
		isOpen &= isPortOpen(eNodeB.getIpAddress(), 22);
		isOpen &= isPortOpen(eNodeB.getIpAddress(), 80);
		if(eNodeB.isSNMPAvailable()){
			report.report("SNMP is available");
		}else{
			report.report("SNMP is not available");
			isOpen = false;
		}
		return isOpen;
	}
	
	private boolean isPortOpen(String ip, int port) {
		try {
			Socket socket = new Socket(ip, port);
			socket.close();
			report.report(String.format("Port %s:%s is open", ip, port));
			return true;
		} catch (Exception ex) {
			report.report(String.format("Port %s:%s is closed", ip, port));
			return false;
		}
	}
}

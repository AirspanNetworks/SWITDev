package Action.EnodebAction;

import java.util.ArrayList;

import org.junit.Test;

import ESon.EsonServer;
import EnodeB.EnodeB;
import IPG.IPG;
import Netspan.Profiles.NetworkParameters;
import Utils.SysObjUtils;
import jsystem.framework.ParameterProperties;
import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;
import jsystem.framework.system.SystemManagerImpl;
import testsNG.Actions.EnodeBConfig;

public class Network extends EnodebAction {
	
	protected EnodeB dut;
	
	@ParameterProperties(description = "Name of ENB from the SUT")
	public void setDUT(String dut) {
		ArrayList<EnodeB> temp = (ArrayList<EnodeB>) SysObjUtils.getInstnce().initSystemObject(EnodeB.class, false,
				dut.split(","));
		this.dut = temp.get(0);
	}
	
	@Test
	@TestProperties(name = "Configure Eson Server", returnParam = { "LastStatus" }, paramsInclude = {"DUT"})
	public void configureEsonServer() {
		EnodeBConfig enodeBConfig = EnodeBConfig.getInstance();
		EsonServer esonServer;
		try{
			esonServer = (EsonServer) system.getSystemObject("EsonServer");
		}catch(Exception e){
			e.printStackTrace();
			report.report("No Eson server tag was found in SUT",Reporter.FAIL);
			return;
		}
		report.report("Configuring Eson server "+esonServer.getServerIp()+" (update network profile)");
		NetworkParameters network = new NetworkParameters();
		network.setcSonConfig(true, esonServer.getServerIp(), 2050);
		
		if (!enodeBConfig.updateNetworkProfile(dut, network)) {
			report.report("Failed to configure Eson server", Reporter.FAIL);
		} else {
			report.report("Configuration done");
		}
	}
	
	@Test
	@TestProperties(name = "Configure IPG as Second MME", returnParam = { "LastStatus" }, paramsInclude = {"DUT"})
	public void configureIpgAsSecondMME() {
		EnodeBConfig enodeBConfig = EnodeBConfig.getInstance();
		IPG ipg = null;
		try {
			ipg = (IPG) SystemManagerImpl.getInstance().getSystemObject("IPG");
		} catch (Exception e) {
			e.printStackTrace();
			report.report("No IPG tag was found in SUT",Reporter.FAIL);
			return;
		}
		report.report("Configuring IPG address "+ipg.getFakeIP()+" as second MME (update network profile)");

		String realMmeIP = enodeBConfig.getMMeIpAdress(dut);
		NetworkParameters network = new NetworkParameters();
		network.setMMEIPS(realMmeIP,ipg.getFakeIP());
		
		if (!enodeBConfig.updateNetworkProfile(dut, network)) {
			report.report("Failed to configure IPG", Reporter.FAIL);
		} else {
			report.report("Configuration done");
		}
	}
	
	@Override
	public void init() {
		if(enbInTest == null) {
			enbInTest = new ArrayList<EnodeB>();
		}
		if(dut != null){
			enbInTest.add(dut);			
		}
		super.init();
	}
}

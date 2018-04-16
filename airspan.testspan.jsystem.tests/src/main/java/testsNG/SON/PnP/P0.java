package testsNG.SON.PnP;

import java.util.ArrayList;

import org.junit.Test;

import EnodeB.EnodeB;
import Netspan.EnbProfiles;
import Netspan.NetspanServer;
import Netspan.API.Enums.PnpModes;
import Netspan.Profiles.SonParameters;
import TestingServices.TestConfig;
import UE.UE;
import Utils.DHCP;
import Utils.GeneralUtils;
import Utils.SetupUtils;
import Utils.SysObjUtils;
import jsystem.framework.ParameterProperties;
import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;
import testsNG.TestspanTest;
import testsNG.Actions.EnodeBConfig;
import testsNG.Actions.PeripheralsConfig;
import testsNG.Actions.Traffic;

public class P0 extends TestspanTest {

	private DHCP dhcp;

	public EnodeB dut1;

	private ArrayList<UE> ues;

	private NetspanServer netspanServer;

	private EnodeBConfig enodeBConfig;

	private Traffic traffic;

	private PeripheralsConfig peripheralsConfig;

	private boolean isWarmReset;

	private boolean isIRelayConfig = false;

	private boolean isIPv6 = false;

	private boolean isInterTest = false;

	private boolean noNms = false;
	/*
	 * @see tests.TestspanTest#init() Initialization of DHCP system object.
	 */
	@Override
	public void init() throws Exception {
		enbInTest = new ArrayList<>();
		enbInTest.add(dut1);
		ues = SetupUtils.getInstance().getStaticUEs();
		GeneralUtils.startLevel("Test Init");
		super.init();
		report.report("Initialize DHCP object.");
		dhcp = (DHCP) system.getSystemObject("DHCP");
		report.report("DHCP initialized.");
		report.report("Initialize Netspan object.");
		netspanServer = NetspanServer.getInstance();
		report.report("Initialize EnodeBConfig object.");
		enodeBConfig = EnodeBConfig.getInstance();
		report.report("Initialize Traffic object.");
		traffic = Traffic.getInstance(SetupUtils.getInstance().getAllUEs());
		report.report("Initialize PeripheralsConfig object.");
		peripheralsConfig = PeripheralsConfig.getInstance();
		GeneralUtils.stopLevel();
	}

	/**
	 * PnP procedure in NMS cold reboot IPV4.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Test
	@TestProperties(name = "PnP procedure in NMS cold reboot IPV4", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void PnPColdRebootIPV4() throws Exception {
		isWarmReset = false;
		mainProcedure();
		postTest();
	}

	/**
	 * PnP procedure in NMS cold reboot IPV6.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Test
	@TestProperties(name = "PnP procedure in NMS cold reboot IPV6", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void PnPColdRebootIPV6() throws Exception {
		isWarmReset = false;
		isIPv6 = true;
		mainProcedure();
		postTest();
	}

	/**
	 * PnP procedure in NMS cold reboot IPV4 including PnP iRelay configuration.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Test
	@TestProperties(name = "PnP procedure in NMS cold reboot IPV4 including PnP iRelay configuration", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void PnPColdRebootIPV4iRelayConfiguration() throws Exception {
		isWarmReset = false;
		isIRelayConfig = true;
		mainProcedure();
		postTest();
	}

	/**
	 * PnP procedure in NMS warm reboot IPV4 + change frequency under
	 * Re-provision.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Test
	@TestProperties(name = "PnP procedure in NMS warm reboot IPV4 + Change frequency", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void PnPWarmRebootIPV4ChangeFrequency() throws Exception {
		isWarmReset = true;
		isInterTest = true;
		mainProcedure();
		postTest();
	}

	/**
	 * PnP procedure in NMS cold reboot IPV4 mapping Without NMS.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Test
	@TestProperties(name = "PnP procedure in NMS warm reboot IPV4 No NMS", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void PnPWarmRebootIPV4NoNMS() throws Exception {
		isWarmReset = true;
		noNms = true;
		mainProcedure();
		postTest();
	}

	/**
	 * PnP procedure in NMS cold reboot IPV4 With SW Upgrade.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	// @Test
	@TestProperties(name = "PnP procedure in NMS cold reboot IPV4 With SW Upgrade", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void PnPColdRebootIPV4WithSWUpgrade() throws Exception {
		isWarmReset = false;
		mainProcedure();
		postTest();
	}

	private boolean mainProcedure() {
		GeneralUtils.startLevel("Main PnP Procedure");			
		if(!checkFrequency())
		{
			GeneralUtils.stopLevel();
			return false;
		}
		
		// verify lte-net.cfg file exist in /bsdata.
		report.report("Manage bsdata Config files: \"lte-net\", \"dhcpd\" and \"dhcpVlan\".");
		if (!manageConfigFiles()) {
			report.report("There is a problem with the config files, Please verify the setup is in working mode.",
					Reporter.FAIL);
			reason = "Problem with config files - check /bsdata/";
			GeneralUtils.stopLevel();
			return false;
		}

		// set DHCP parameters.
		if (!setDhcpConfiguration()) {
			report.report("Set DHCP parameters failed. Stoping test.", Reporter.FAIL);
			reason = "Couldn't set DHCP parameters correctly.";
			GeneralUtils.stopLevel();
			return false;
		}

		// set PnP mode to enabled always.
		GeneralUtils.startLevel("Setting PnP mode to \"Enabled_Always\".");
		SonParameters sonParams = new SonParameters();
		sonParams.setSonCommissioning(true);
		sonParams.setPnpMode(PnpModes.ENABLED_ALWAYS);
		if (!enodeBConfig.setPnPState(dut1, sonParams)) {
			report.report(String.format("Couldn't set PnP Mode on %s", dut1.getName()));
			reason = "Couldn't set DHCP parameters correctly.";
			GeneralUtils.stopLevel();
			GeneralUtils.stopLevel();
			return false;
		}
		GeneralUtils.stopLevel();

		// set PnP Warm Reset mode to enabled/disabled.
		GeneralUtils.startLevel(String.format("Setting warm reset mode state to %s.", isWarmReset));
		if (!enodeBConfig.setWarmResetState(dut1, isWarmReset)) {
			report.report(String.format("Couldn't set PnP Warm Reset Mode on %s", dut1.getName()), Reporter.FAIL);
			reason = "Couldn't set PnP Warm Reset Mode.";
			GeneralUtils.stopLevel();
			GeneralUtils.stopLevel();
			return false;
		}
		GeneralUtils.stopLevel();

		// convert to PnP config.
		GeneralUtils.startLevel(String.format("Moving %s to PnP node list", dut1.getName()));
		if (!netspanServer.convertToPnPConfig(dut1)) {
			report.report(String.format("Failed to convert %s to PnP Config.", dut1.getName()), Reporter.FAIL);
			reason = "Couldn't move node to PnP list.";
			GeneralUtils.stopLevel();
			GeneralUtils.stopLevel();
			return false;
		}
		GeneralUtils.stopLevel();
		// if(swUpgrade)
		// setSwVersion();

		// reboot to all-running
		GeneralUtils.startLevel(String.format("Rebooting %s to start PNP Procedure.", dut1.getName()));
		dut1.reboot();
		if (noNms)
			blockNmsIp();
		if (isIPv6)
			setAddressToIpv6();
		
		if (!dut1.waitForAllRunningAndInService(EnodeB.WAIT_FOR_ALL_RUNNING_TIME)) { 
			report.report(String.format("%s couldn't reach all-running", dut1.getName()), Reporter.FAIL);
			GeneralUtils.stopLevel();
			GeneralUtils.stopLevel();
			return false;
		}
		GeneralUtils.stopLevel();

		// Verify test specific parameters

		additionalVerifications();

		// connect ues and run traffic
		GeneralUtils.startLevel(String.format("Verifing UEs connected to %s.", dut1.getName()));
		report.report("Starting traffic.");
		try {
			traffic.startTraffic();
		} catch (Exception e) {
			report.report("exception in start traffic!");
			e.printStackTrace();
		}
		for (UE ue : ues) {
			ue.stop();
			GeneralUtils.unSafeSleep(5000);
			ue.start();
		}
		GeneralUtils.unSafeSleep(20000);

		if (!peripheralsConfig.verifyEnodeBToUe(ues, dut1)) {
			report.report("The ues failed to connect.", Reporter.WARNING);
			GeneralUtils.stopLevel();
			GeneralUtils.stopLevel();
			return false;
		}
		GeneralUtils.stopLevel();
		GeneralUtils.stopLevel();
		return true;
	}

	private void setAddressToIpv6() {
		report.report("Change managment IP to IPv6.");
		dut1.setIpAddress(dhcp.getDhcpConfigIpAddress(dut1, true));
		dut1.initSNMP();
	}

	private boolean manageConfigFiles() {
		boolean actionSucceeded = true;
		// Manage lte-net files.
		if (isIPv6) {
			if (dut1.isFileExists("/bsdata/lte-netV6.cfg")) {
				dut1.shell("cp /bsdata/lte-netV6.cfg /bsdata/lte-netV6.cfg.bak");
			}
		} else {
			if (dut1.isFileExists("/bsdata/lte-net.cfg")) {
				dut1.shell("cp /bsdata/lte-net.cfg /bsdata/lte-net.cfg.bak");
			} else {
				dut1.shell("cp /bsdata/lte-net.cfg.bak /bsdata/lte-net.cfg");
			}
			actionSucceeded &= dut1.isFileExists("/bsdata/lte-net.cfg");
		}

		// Create dhcpVlan file.
		dut1.shell("echo -e \"-1\" > /bsdata/dhcpVlan");
		dut1.shell("echo -e \"2\" >> /bsdata/dhcpVlan");
		actionSucceeded &= dut1.isFileExists("/bsdata/dhcpVlan");

		// Verify snmpd.conf exists.
		dut1.shell("cp /bsdata/snmpd.conf /bsdata/snmpd.conf.bak");
		actionSucceeded &= dut1.isFileExists("/bsdata/snmpd.conf");
		if (actionSucceeded)
			report.report("All the required config files are present in bsdata.");
		return actionSucceeded;
	}

	private boolean additionalVerifications() {
		GeneralUtils.startLevel("Additional verifications.");
		boolean isPassed = true;
		if (isIRelayConfig)
			verifyIRelay();
		if (isInterTest)
			isPassed &= verifyFreq();
		GeneralUtils.stopLevel();
		return isPassed;
	}

	private boolean verifyFreq() {
		report.report("Verify frequency changed.");
		return String.valueOf(TestConfig.getInstace().getInterEarfcn()).equals(dut1.getEarfcn());
	}

	private void verifyIRelay() {
		report.report("Verify iRelay config.");
		dut1.lteCli("db get DhcpInfo iRelay");
	}

	private void blockNmsIp() {
		report.report("Block Netspan IP.");
		while (!dut1.isReachable()) {
			GeneralUtils.unSafeSleep(1000);
		}
		GeneralUtils.unSafeSleep(5000);
		dut1.shell(String.format("route add -host %s reject", netspanServer.getHostname()));
	}

	private void unBlockNmsIp() throws Exception {
		report.report("Unblock Netspan IP.");
		dut1.shell(String.format("route delete -host %s reject", netspanServer.getHostname()));
	}

	private boolean checkFrequency() {
		GeneralUtils.startLevel("Check Earfcn.");
		int interEarfcn = TestConfig.getInstace().getInterEarfcn();
		boolean ans = dut1.getEarfcn() == interEarfcn;
		if (ans) {
			report.report("Earfcn is as expected");
		}else{
			report.report("Earfcn is as not expected, stop test", Reporter.FAIL);
			reason = "Earfcn is as not expected, stop test";
		}
		GeneralUtils.stopLevel();
		return ans;
	}

	private boolean setDhcpConfiguration() {
		boolean setSucceeded = true;
		GeneralUtils.startLevel("Configuring DHCP Parameters.");
		String configuredParameter;

		String ipVersion = isIPv6 ? "6" : "4";
		configuredParameter = dhcp.getDhcpConfigIpVersion();
		if (!configuredParameter.equals(ipVersion)) {
			GeneralUtils.startLevel("Set DHCP parameter - IP Version.");
			setSucceeded &= dhcp.setDhcpConfigIpVersion(isIPv6);
			GeneralUtils.stopLevel();
		}

		configuredParameter = dhcp.getDhcpConfigNetspanIP(dut1);
		GeneralUtils.startLevel("Set DHCP parameter - Netspan IP.");
		if (isIPv6) {
			if (!configuredParameter.equals(netspanServer.getIpv6Addrss())) {
				setSucceeded &= dhcp.setDhcpConfigNetspanIP(dut1, netspanServer.getIpv6Addrss());
			}
		} else {
			if (!configuredParameter.equals(netspanServer.getHostname())) {
				setSucceeded &= dhcp.setDhcpConfigNetspanIP(dut1, netspanServer.getHostname());
			}
		}
		GeneralUtils.stopLevel();

		configuredParameter = dhcp.getDhcpConfigIRelay(dut1);
		if (!configuredParameter.equals(Boolean.toString(isIRelayConfig))) {
			GeneralUtils.startLevel("Set DHCP parameter - iRelay Configuration.");
			setSucceeded &= dhcp.setDhcpConfigIRelay(dut1, isIRelayConfig);
			GeneralUtils.stopLevel();
		}

		if (setSucceeded)
			report.report("DHCP Configuration succeeded.");
		GeneralUtils.stopLevel();
		return setSucceeded;
	}

	private void postTest() {
		try {
			report.startLevel(String.format("Post test."));
			// return to the default Son profile
			report.startLevel(String.format("Returning %s to its default state.", dut1.getName()));
			if (noNms)
				unBlockNmsIp();
			if (isIPv6)
				returnToIPv4();
			netspanServer.movePnPConfigToNodeList(dut1);
			report.report(String.format("%s - Setting SON profile to default.", dut1.getName()));
			enodeBConfig.setProfile(dut1, EnbProfiles.Son_Profile, dut1.defaultNetspanProfiles.getSON());
			report.stopLevel();
			enodeBConfig.deleteClonedProfiles();
			// stop traffic
			traffic.stopTraffic();
			traffic.getTg().close();
			report.stopLevel();
		} catch (Exception e) {
			report.report("Exception was caught during PostTest, May cause setup issues.", Reporter.WARNING);
			e.printStackTrace();
		}
	}

	private void returnToIPv4() throws Exception {
		Thread.sleep(10000);
		report.report(String.format("%s - Returning managment IP to IPv4.", dut1.getName()));
		dut1.setPnpMode(0);
		dut1.shell("rm /bsdata/snmpd.conf");
		dut1.shell("rm /bsdata/lte-netV6.cfg");
		dut1.shell("cp /bsdata/lte-net.cfg.bak /bsdata/lte-net.cfg");
		dut1.shell("cp /bsdata/snmpd.conf.bak /bsdata/snmpd.conf");
		dut1.reboot();
		dut1.setIpAddress(dhcp.getDhcpConfigIpAddress(dut1, false));
		dut1.initSNMP();
		if (!dut1.waitForAllRunningAndInService(EnodeB.WAIT_FOR_ALL_RUNNING_TIME)) {
			report.report(String.format("%s couldn't reach all-running", dut1.getName()));
		}
	}

	@ParameterProperties(description = "First DUT")
	public void setDut1(String dut) {
		GeneralUtils.printToConsole("Load DUT1 " + dut);
		this.dut1 = (EnodeB) SysObjUtils.getInstnce().initSystemObject(EnodeB.class, false, dut).get(0);
		GeneralUtils.printToConsole("DUT loaded" + this.dut1.getName());
	}

	public EnodeB getDut1() {
		return dut1;
	}

}

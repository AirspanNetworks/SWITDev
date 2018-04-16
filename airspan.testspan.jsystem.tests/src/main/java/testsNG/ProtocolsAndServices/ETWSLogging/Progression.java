package testsNG.ProtocolsAndServices.ETWSLogging;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import EnodeB.EnodeB;
import IPG.IPG;
import Netspan.EnbProfiles;
import Netspan.API.Enums.EnabledDisabledStates;
import Netspan.NBI_15_5.Netspan_15_5_abilities;
import Netspan.Profiles.CellAdvancedParameters;
import Netspan.Profiles.NetworkParameters;
import Utils.FileServer;
import Utils.GeneralUtils;
import Utils.ScpClient;
import Utils.SetupUtils;
import Utils.SysObjUtils;
import jsystem.framework.ParameterProperties;
import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;
import jsystem.framework.system.SystemManagerImpl;
import testsNG.TestspanTest;
import testsNG.Actions.EnodeBConfig;
import testsNG.Actions.PeripheralsConfig;
import testsNG.Actions.Utils.ParallelCommandsThread;

public class Progression extends TestspanTest {

	final int STRESS_TEST_TIME = 1800000;
	private String etwsDir = System.getProperty("user.dir") + "/ETWS";
	private EnodeBConfig enodeBConfig;
	private PeripheralsConfig peripheralsConfig;
	private EnodeB dut;
	private IPG ipg;
	private FileServer fileServer;
	private ScpClient ipgScpClient;
	private boolean changeMme;
	private boolean isUserMode;
	ParallelCommandsThread commandsThread1 = null;

	Map<String, String> scriptParams = new HashMap<String, String>();

	@Test
	@TestProperties(name = "ETWS Logging Functionality No Data", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void etwsLoggingFunctionalityNoData() {
		changeMme = false;
		preTest();
		etwsNoDataTest();
		postTest();
	}

	@Test
	@TestProperties(name = "ETWS Logging Functionality Standard Mode", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void etwsLoggingFunctionalityStandardMode() {
		if(preTest())
			etwsFunctionalityTest();
		postTest();
	}

	@Test
	@TestProperties(name = "ETWS Logging Stress User Mode", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void etwsLoggingStressUserMode() {
		preTest();
		etwsStressTest(false);
		postTest();
	}

	@Test
	@TestProperties(name = "ETWS Logging Stress User Mode No FTP", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void etwsLoggingStressUserModeNoFTP() {
		preTest();
		etwsStressTest(true);
		postTest();
	}

	@Override
	public void init() throws Exception {
		GeneralUtils.startLevel("Init Tests");
		enbInTest = new ArrayList<EnodeB>();
		enbInTest.add(dut);
		super.init();
		enodeBConfig = EnodeBConfig.getInstance();
		peripheralsConfig = PeripheralsConfig.getInstance();
		fileServer = (FileServer) SystemManagerImpl.getInstance().getSystemObject("FileServer");
		ipg = (IPG) SystemManagerImpl.getInstance().getSystemObject("IPG");
		ipgScpClient = new ScpClient(ipg.getServerIP(), ipg.getUserName(), ipg.getServerPassword());
		ipg.setScriptName("ManInTheETWS.js");
		GeneralUtils.stopLevel();
	}

	public void commands() {
		ArrayList<String> commands = new ArrayList<>();
		commands.add("!ps | grep ./prcMngr | grep -v grep | wc -l");
		commands.add("!ls /var/log/etws/etwsLog/ |grep -v logger| wc -l");
		commands.add("db get PTPStatus");
		try {
			commandsThread1 = new ParallelCommandsThread(commands, dut, null, null);

		} catch (IOException e) {
			report.report("could not init Commands in parallel", Reporter.WARNING);
		}
		commandsThread1.start();
	}

	private boolean preTest() {
		boolean actionState = true;
		GeneralUtils.startLevel("Pre Test");
		clearEtwsFilesFromFileServer();
		deleteEtwsFilesLocaly();
		peripheralsConfig.stopUEs(SetupUtils.getInstance().getAllUEs());
		ipg.setRealMME(enodeBConfig.getMMeIpAdress(dut));
		actionState = prepareIpg();
		NetworkParameters params = new NetworkParameters();
		params.setMMEIPS(ipg.getRealMME(), ipg.getFakeIP());
		actionState &= enodeBConfig.changeNetworkProfile(dut, params);
		actionState &= dut.reboot();
		actionState &= dut.waitForAllRunning(EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
		if(!actionState)
			report.report("PreTest failed skipping test",Reporter.FAIL);
		GeneralUtils.stopLevel();
		return actionState;
	}

	private void clearEtwsFilesFromFileServer() {
		GeneralUtils.startLevel("Clear ETWS files from file server");
		fileServer.connect();
		String filter = dut.getCellIdentity() + "__ETWSLog";
		ArrayList<String> files = fileServer.listFiles("");
		for (String file : files) {
			if (file.contains(filter))
				fileServer.deleteFile(file);
		}
		fileServer.disconnect();
		GeneralUtils.stopLevel();
	}

	private boolean enableEtws(int uploadPeriod, int uploadPeriodNoData) {
		boolean actionState = true;
		NetworkParameters params = new NetworkParameters();
		params.setEtwsEnabled(EnabledDisabledStates.ENABLED);
		boolean isExists = ((Netspan_15_5_abilities) netspanServer).isFileServerExists(fileServer.getName());
		if (!isExists)
			((Netspan_15_5_abilities) netspanServer).createFileServer(fileServer);
		params.setEtwsFileServerName(fileServer.getName());
		params.setEtwsUploadPeriod(uploadPeriod);
		params.setEtwsUploadPeriodNoData(uploadPeriodNoData);
		actionState = enodeBConfig.changeNetworkProfile(dut, params);
		return actionState;
	}

	private boolean enableUserMode(int sib10Duration, int sib11Duration) {
		boolean actionState = true;
		CellAdvancedParameters params = new CellAdvancedParameters();
		params.setEtwsUserMode(EnabledDisabledStates.ENABLED);
		params.setSib10Duration(sib10Duration);
		params.setSib11Duration(sib11Duration);
		actionState = enodeBConfig.changeCellAdvancedProfile(dut, params);
		isUserMode = true;
		return actionState;
	}

	private boolean disableEtws() {
		boolean actionState = true;
		NetworkParameters params = new NetworkParameters();
		params.setEtwsEnabled(EnabledDisabledStates.DISABLED);
		actionState = enodeBConfig.changeNetworkProfile(dut, params);
		return actionState;
	}

	private boolean deleteEtwsFilesLocaly() {
		boolean isDeleted = true;
		report.report("Delete all local test generated files.");
		File folder = new File(etwsDir);
		try {
			if (folder.exists()) {
				File[] listOfFiles = folder.listFiles();
				for (int i = 0; i < listOfFiles.length; i++) {
					File file = listOfFiles[i];
					isDeleted &= file.delete();
				}
			} else
				folder.mkdir();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return isDeleted;
	}

	private boolean verifyEtwsNoDataFiles() {
		extractEtwsFiles();
		return countTextInETWSFiles("No logs were recorded") >= 2;
	}

	private boolean verifyEtwsSib1011(int expected) {
		boolean verified = true;
		int sib10Count = countTextInETWSFiles("SIB10");
		int sib11Count = countTextInETWSFiles("SIB11");
		if (sib10Count != expected) {
			report.report(String.format("SIB 10 count is %s instead of %s", sib10Count, expected));
			verified = false;
		}
		if (sib11Count != expected) {
			report.report(String.format("SIB 11 count is %s instead of %s", sib11Count, expected));
			verified &= false;
		}
		return verified;
	}

	private int countTextInETWSFiles(String text) {
		int count = 0;
		File folder = new File(etwsDir);
		File[] listOfFiles = folder.listFiles();
		try {
			for (int i = 0; i < listOfFiles.length; i++) {
				File file = listOfFiles[i];
				if (file.isFile() && file.getName().endsWith(".db")) {
					String content = FileUtils.readFileToString(file);
					if (content.contains(text)) {
						count += StringUtils.countMatches(content, text);
					}
				}
			}
			report.report(String.format("The text \'%s\' was found %d times", text, count));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return count;
	}

	private boolean extractEtwsFiles() {
		fileServer.connect();
		String filter = dut.getCellIdentity() + "__ETWSLog";
		ArrayList<String> files = fileServer.listFiles("");
		for (String fileName : files) {
			if (fileName.contains(filter)) {
				fileServer.getFile(fileName, etwsDir + "/" + fileName);
				File file = new File(etwsDir + "/" + fileName);
				file = GeneralUtils.extractGZFile(file, etwsDir + "/" + fileName.replace(".gz", ""));
				GeneralUtils.extractTarFile(file, etwsDir);
			}
		}
		clearEtwsFilesFromFileServer();
		fileServer.disconnect();
		return true;
	}

	private boolean etwsNoDataTest() {
		enableEtws(3, 1);
		printDebugData();
		GeneralUtils.unSafeSleep(60000);
		disableEtws();
		isPass = verifyEtwsNoDataFiles();
		if (isPass)
			report.step("Test passed. \"No Data\" files uploaded to file server as expected");
		else
			report.report("Test failed. couldn't find \"No Data\" files in the file server");
		return isPass;
	}

	private boolean etwsFunctionalityTest() {
		enableEtws(3, 1000);
		printDebugData();
		report.report("Send ETWS message and wait %s seconds", 30);
		ipg.runScript(scriptParams);
		GeneralUtils.unSafeSleep(30000);
		disableEtws();
		isPass = !verifyEtwsNoDataFiles();
		if (!isPass)
			report.report("Too many \"No Data\" files found in the file server", Reporter.WARNING);
		isPass = verifyEtwsSib1011(2);
		if (isPass)
			report.step("Test passed. SIB10 & SIB11 count is as expected");
		else
			report.report("Test failed. SIB10 & SIB11 count is not as expected", Reporter.FAIL);
		return isPass;
	}

	private boolean etwsStressTest(boolean ftpClosed) {
		if (ftpClosed) {
			report.report("Block File Server");
			dut.shell(String.format("route add -host %s reject", fileServer.getIpAddress()));
		}
		commands();
		enableEtws(10, 1000);
		int numOfSibs = STRESS_TEST_TIME/1000;
		enableUserMode(numOfSibs, numOfSibs);
		printDebugData();
		report.report("Send ETWS message and wait %s seconds", numOfSibs);
		ipg.runScript(scriptParams);
		// wait test time + 30 sec for script delay.
		GeneralUtils.unSafeSleep(STRESS_TEST_TIME + 30 * 1000);
		if (ftpClosed) {
			report.report("Unblock File Server and wait for upload 5 minutes");
			dut.shell(String.format("route delete -host %s reject", fileServer.getIpAddress()));
			GeneralUtils.unSafeSleep(300 * 1000);
		}
		disableEtws();
		stopCommandsAndAttachFiles();
		isPass = !verifyEtwsNoDataFiles();
		if (!isPass)
			report.report("Too many \"No Data\" files found in the file server", Reporter.WARNING);
		isPass = verifyEtwsSib1011(STRESS_TEST_TIME / 80);
		if (isPass)
			report.step("Test passed. SIB10 & SIB11 count is as expected");
		else
			report.report("Test failed. SIB10 & SIB11 count is not as expected", Reporter.FAIL);
		return isPass;
	}

	private boolean prepareIpg() {
		String jsName = System.getProperty("user.dir") + File.separator + "resources" + File.separator + "IPGScripts"
				+ File.separator + "ManInTheETWS.js";
		String etwsMessageName = System.getProperty("user.dir") + File.separator + "resources" + File.separator
				+ "IPGScripts" + File.separator + "Primary_Secondary_with_concurrentIE.s1p";
		String setupResponseMessageName = System.getProperty("user.dir") + File.separator + "resources" + File.separator
				+ "IPGScripts" + File.separator + "S1_Response.s1p";
		try {
			ipgScpClient.putFiles(ipg.getScriptsDirectory(), jsName, etwsMessageName, setupResponseMessageName);
		} catch (IOException e) {
			report.report("Couldn't copy scripts to IPG server");
			e.printStackTrace();
			return false;
		}
		scriptParams.put("fakeIP", ipg.getFakeIP());
		scriptParams.put("realMME", ipg.getRealMME());
		scriptParams.put("s1Ip", dut.getS1IpAddress());
		ipg.connectIPG();
		if (!ipg.isConnected()) {
			report.report("Couldn't connect to IPG server", Reporter.FAIL);
			reason = "Couldn't connect to IPG server";
			GeneralUtils.stopLevel();
			return false;
		} else {
			report.report("IPG server is connected");
			return true;
		}
	}

	private void printDebugData() {
		GeneralUtils.startLevel("DEBUG DATA");
		GeneralUtils.reportHtmlLink(dut.getName() + " - db get pwsCfg", dut.lteCli("db get pwsCfg"));
		GeneralUtils.reportHtmlLink(dut.getName() + " - db get logProcCfg", dut.lteCli("db get logProcCfg"));
		GeneralUtils.stopLevel();
	}

	public boolean postTest() {
		boolean actionState = true;
		GeneralUtils.startLevel("Post Test");
		deleteEtwsFilesLocaly();
		ipg.close();
		actionState = enodeBConfig.setProfile(dut, EnbProfiles.Network_Profile,
				dut.defaultNetspanProfiles.getNetwork());
		if (isUserMode)
			actionState &= enodeBConfig.setProfile(dut, EnbProfiles.Cell_Advanced_Profile,
					dut.defaultNetspanProfiles.getCellAdvanced());
		enodeBConfig.deleteClonedProfiles();
		actionState &= dut.reboot();
		actionState &= dut.waitForAllRunning(EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
		if(!actionState)
			report.report("PostTest failed",Reporter.WARNING);
		GeneralUtils.stopLevel();
		return actionState;
	}

	private void stopCommandsAndAttachFiles() {
		commandsThread1.stopCommands();
		commandsThread1.moveFileToReporterAndAddLink();
	}

	@ParameterProperties(description = "DUT")
	public void setDUT1(String dut) {
		System.out.println("Load DUT1" + dut);
		this.dut = (EnodeB) SysObjUtils.getInstnce().initSystemObject(EnodeB.class, false, dut).get(0);
		System.out.println("DUT loaded" + this.dut.getNetspanName());
	}

	public EnodeB getDut1() {
		return dut;
	}
}

package testsNG.PlatformAndSecurity.Init;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Test;
import EnodeB.EnodeB;
import EnodeB.EnodeBUpgradeServer;
import TestingServices.SWUpgradeConfig;
import TestingServices.TestConfig;
import Utils.GeneralUtils;
import Utils.ScpClient;
import Utils.SysObjUtils;
import Netspan.API.Enums.ServerProtocolType;
import Netspan.API.Software.SoftwareStatus;
import Netspan.API.Software.*;
import jsystem.framework.ParameterProperties;
import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;
import jsystem.framework.report.ReporterHelper;
import testsNG.TestspanTest;
import testsNG.Actions.EnodeBConfig;
import testsNG.Actions.SoftwareUtiles;
import testsNG.Actions.SoftwareUtiles.VersionCopyWorker;

public class P0 extends TestspanTest {

	 public EnodeB dut;
	 private String buildPath;
	 private EnodeBUpgradeServer enbUpgrade;
	 private TestConfig tc = TestConfig.getInstace();
	 EnodeBConfig config = EnodeBConfig.getInstance();
	 SoftwareUtiles softwareUtiles;
	 String buildName = null;
	 String imageName = null;
	 boolean ispassed = false;
	 boolean isUpgrade = false;

	@Override
	public void init() throws Exception {
		enbInTest = new ArrayList<>();
		enbInTest.add(dut);
		super.init();
		objectInit();
			
	}
	public boolean objectInit() throws Exception {
		softwareUtiles = SoftwareUtiles.getInstance();
		buildPath = System.getProperty("BuildMachineVerPath");
        if (buildPath.equals("")) {
        	SWUpgradeConfig swUpgreadeinputs = tc.getSwUpgradeConfig();
        	if(swUpgreadeinputs == null){
        		report.report("No inputs for the test");
        		reason = "No inputs for the test";
        		return false;
        	}
        	buildPath = swUpgreadeinputs.getAbsolutPath();
        }
        enbUpgrade = SysObjUtils.getInstnce().initSystemObject(EnodeBUpgradeServer.class, "UpgradeServer").get(0);
        return true;
	}
	
	@Override 
	public void end(){
		if (imageName!=null){
			softwareUtiles.softwareConfigSet(dut.getNetspanName(), RequestType.ABORT, imageName);
			softwareUtiles.deleteSoftwareImage(imageName);	
		}
		if (!ispassed)
			uploadSWerrorFile(dut);
		super.end();
		
	}
	
	@Test
	@TestProperties(name = "FTP_software_download_from_new_version_enc", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void FTP_software_download_from_new_version_enc(){
		EnodeBUpgradeServer server = new EnodeBUpgradeServer();
		server.setUpgradeServerObject(enbUpgrade.getUpgradeServerIp()+ "_" + ServerProtocolType.FTP.toString(), enbUpgrade.getUpgradeServerIp(), ServerProtocolType.FTP, enbUpgrade.getUpgradeUser(), enbUpgrade.getUpgradePassword());
		setFromNewVersion(server, "enc");
	}
	@Test
	@TestProperties(name = "TFTP_software_download_from_new_version_enc", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void TFTP_software_download_from_new_version_enc(){
		EnodeBUpgradeServer server = new EnodeBUpgradeServer();
		server.setUpgradeServerObject(enbUpgrade.getUpgradeServerIp()+ "_" + ServerProtocolType.TFTP.toString(), enbUpgrade.getUpgradeServerIp(), ServerProtocolType.TFTP, enbUpgrade.getUpgradeUser(), enbUpgrade.getUpgradePassword());
		setFromNewVersion(server, "enc");
		
	}
	@Test
	@TestProperties(name = "SFTP_software_download_from_new_version_enc", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void SFTP_software_download_from_new_version_enc(){
		EnodeBUpgradeServer server = new EnodeBUpgradeServer();
		server.setUpgradeServerObject(enbUpgrade.getUpgradeServerIp()+ "_" + ServerProtocolType.SFTP.toString(), enbUpgrade.getUpgradeServerIp(), ServerProtocolType.SFTP, enbUpgrade.getUpgradeUser(), enbUpgrade.getUpgradePassword());
		setFromNewVersion(server, "enc");
		
	}
	//@Test
	@TestProperties(name = "SFTP_software_download_from_new_version_swu", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void SFTP_software_download_from_new_version_swu(){
		EnodeBUpgradeServer server = new EnodeBUpgradeServer();
		server.setUpgradeServerObject(enbUpgrade.getUpgradeServerIp()+ "_" + ServerProtocolType.SFTP.toString(), enbUpgrade.getUpgradeServerIp(), ServerProtocolType.SFTP, enbUpgrade.getUpgradeUser(), enbUpgrade.getUpgradePassword());
		setFromNewVersion(server, "swu");
		
	}
	@Test
	@TestProperties(name = "SFTP_software_download_from_new_version_tar", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void SFTP_software_download_from_new_version_tar(){
		EnodeBUpgradeServer server = new EnodeBUpgradeServer();
		server.setUpgradeServerObject(enbUpgrade.getUpgradeServerIp()+ "_" + ServerProtocolType.SFTP.toString(), enbUpgrade.getUpgradeServerIp(), ServerProtocolType.SFTP, enbUpgrade.getUpgradeUser(), enbUpgrade.getUpgradePassword());
		setFromNewVersion(server, "tar");
		
	}
	@Test
	@TestProperties(name = "SFTP_software_download_to_new_version_enc", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void SFTP_software_download_to_new_version_enc(){
		EnodeBUpgradeServer server = new EnodeBUpgradeServer();
		server.setUpgradeServerObject(enbUpgrade.getUpgradeServerIp()+ "_" + ServerProtocolType.SFTP.toString(), enbUpgrade.getUpgradeServerIp(), ServerProtocolType.SFTP, enbUpgrade.getUpgradeUser(), enbUpgrade.getUpgradePassword());
		setToNewVersion(server, "enc");
		
	}

	private void setFromNewVersion(EnodeBUpgradeServer server, String fileType){	
		String currentVer = dut.getRunningVersion();
		changeBuildPathToLowerVersion();
		String targetVer = getVersionFromPath(buildPath);
		if (currentVer.equals(targetVer)){
			report.report("The target version is the same as current version", Reporter.WARNING);
			changeBuildPathToLowerVersion();
		}
		SoftwareStatus result = softwareUtiles.getSoftwareStatus(dut.getNetspanName(), dut.getImageType());
		if(result.StandbyVersion.equals(targetVer)){
			report.report("The target version is the same as standby version, changing to lower version");
			changeBuildPathToLowerVersion();
			targetVer = getVersionFromPath(buildPath);
		}
		
		for (int i = 0; i<4; i++){
			ispassed = setPathAndFiles(fileType, server.getUpgradeServerProtocolType().toString());
			if(!ispassed){
				changeBuildPathToLowerVersion();
			}
			else 
				break;
		}
		if(!ispassed){
			report.report("Couldn't find version with suitable files to upgrade", Reporter.FAIL);
			return;
		}
       
		
		ispassed =  copyFiles(server.getUpgradeServerProtocolType());
		if(!ispassed){
			report.report("Couldn't copy files to server", Reporter.FAIL);
			return;
		}

		ispassed = softwareUtiles.createUpgradeServer(server);
		if (!ispassed){
			report.report("Coudln't create software server in netspan", Reporter.FAIL);
			return;
		}
			
		
		imageName = targetVer+"_"+server.getUpgradeServerProtocolType().toString()+"_Automation";
		ispassed = softwareUtiles.createUpgradeImage(server, buildName, imageName, targetVer, dut);
		if (!ispassed){
			report.report("Couldnlt create upgrade image in netspan", Reporter.FAIL);
			return;
		}
			
			GeneralUtils.startLevel("Downloading file");
		
		ispassed = softwareUtiles.softwareConfigSet(dut.getNetspanName(), RequestType.DOWNLOAD, imageName);
		
		if (!ispassed){
			report.report("Couldn't config software upgrade in netspan", Reporter.FAIL);
			return;
		}
			
		ispassed = softwareUtiles.downloadFile(dut, false);
		GeneralUtils.stopLevel();
		if(!ispassed){
			report.report("File wasn't downloaded as expected", Reporter.FAIL);
			return;
		}
		else{
			GeneralUtils.unSafeSleep(60 * 1000);
			ispassed = isStatusOK(dut, targetVer);
			if (!ispassed)
				return;
		}
	}
	public void setToNewVersion(EnodeBUpgradeServer server, String fileType){
		isUpgrade = true;
		String currentVer = dut.getRunningVersion();
		
		buildPath = buildPath.substring( 0 , buildPath.lastIndexOf('\\') );
		ispassed = setPathAndFiles( fileType, server.getUpgradeServerProtocolType().toString());
		if(!ispassed)
			return;
		String targetVer = "";
		switch (dut.getArchitecture()) {
		case XLP:
			targetVer = softwareUtiles.getXLPBuild();
			break;
		case FSM:
			targetVer = softwareUtiles.getFSMBuild();
			break;
		}
		targetVer = targetVer.split("\\.")[1];
		targetVer = targetVer.replaceAll("_", "\\.");
		report.report("Target version: " + targetVer);
		if (currentVer.equals(targetVer)) {
			report.report("The target version is the same as current version", Reporter.FAIL);
			return;
		}
		
		ispassed =  copyFiles(server.getUpgradeServerProtocolType());
		if(!ispassed){
			report.report("Couldn't copy files to server", Reporter.FAIL);
			return;
		}
			
		ispassed = softwareUtiles.createUpgradeServer(server);
		if (!ispassed){
			report.report("Coudln't create software server in netspan", Reporter.FAIL);
			return;
		}
			
		
		imageName = targetVer+"_"+server.getUpgradeServerProtocolType().toString()+"_Automation";
		ispassed = softwareUtiles.createUpgradeImage(server, buildName, imageName, targetVer, dut);
		if (!ispassed){
			report.report("Couldnlt create upgrade image in netspan", Reporter.FAIL);
			return;
		}
			
		GeneralUtils.startLevel("Activating software Image");
		dut.setExpectBooting(true);
		ispassed = softwareUtiles.softwareConfigSet(dut.getNetspanName(), RequestType.ACTIVATE, imageName);
		
		
		if (!ispassed){
			report.report("Couldn't config software uograde in netspan", Reporter.FAIL);
			return;
		}
			
		ispassed = softwareUtiles.downloadFile(dut, true);
		GeneralUtils.stopLevel();

		if(!ispassed){
			report.report("File wasn't downloaded as expected", Reporter.FAIL);
			return;
		}
		else{
			report.report("Wait for all running from enodeB " + dut.getNetspanName());
			dut.waitForAllRunningAndInService(EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
			ispassed = isStatusOK(dut, targetVer);
			if (!ispassed)
				return;
		}
		
	}
	private void changeBuildPathToLowerVersion() {
		if(!buildPath.contains("release")){
			buildPath = buildPath + File.separator + "release";
		}
		String targetBuild = buildPath;
		String tempStr = getVersionFromPath(targetBuild);
		String lastPart = tempStr.substring(tempStr.lastIndexOf('.'), tempStr.length());
		lastPart = lastPart.replace(".", "");
		if (lastPart.equals("")){
			report.report("couldn't get the version of the build");
			return;
		}
		String newPath = buildPath.substring(0, buildPath.lastIndexOf(File.separator));
		newPath = newPath.substring(0, newPath.lastIndexOf("_"));
		lastPart = "" + (Integer.parseInt(lastPart) -1);
		String path = newPath;
		for(int i = Integer.parseInt(lastPart); i>0; i--){
			lastPart = "" + i;
			if(lastPart.length()!=3){
				lastPart = String.format("%03d", i);
			}
			path = newPath + "_" + lastPart;
			if(new File(path).exists()){
				buildPath = path;
				return;
			}	
		}
	}
	public boolean copyFiles(ServerProtocolType protocolType){
		boolean ispass = false;
		GeneralUtils.startLevel("Copy files to Upgrade server");
        VersionCopyWorker tempWorker = softwareUtiles.new VersionCopyWorker(dut.getArchitecture(),protocolType);
        
        buildName  = (dut.getArchitecture() == EnodeB.Architecture.FSM)? softwareUtiles.getFSMBuild() : softwareUtiles.getXLPBuild();
        
        boolean copingFile = tempWorker.startCopy();
        if(!copingFile){
        	report.report("wasn't able to copy the file " + buildName + " to " + softwareUtiles.getDestServer().getPath(), Reporter.WARNING);
        }
        else{
        	report.report("file " + buildName +" was copied to " + softwareUtiles.getDestServer().getPath());
        	ispass = true;
        }
       
        GeneralUtils.stopLevel();
        return ispass;
	}

	
	private String getVersionFromPath(String path){
		path = path + "\\";
		String version = "";
		final String regex = "[\\\\]+[0-9,_]+[\\\\]+";
		final Pattern pattern = Pattern.compile(regex);
		final Matcher matcher = pattern.matcher(path);
		if(matcher.find()) {
			version = matcher.group(0);
			version = version.replace("\\", "");
			version = version.replaceAll("_", "\\.");
		}
		return version;
	}
	private boolean setPathAndFiles(String fileType, String destinationServerType) {
		String FolderType = destinationServerType;
		if (destinationServerType.equals("SFTP")){
			FolderType = FolderType + "\\upload";
		}
		softwareUtiles.setDestServer(new File(File.separator + File.separator + enbUpgrade.getUpgradeServerIp() + File.separator + FolderType));
        if (!setSourcePath()) {
            report.report("There is no suitable Config (Path/Version/BuildMachine trigger) to start the test", Reporter.FAIL);
            isPass=false;
            reason += "FAIL: There is no suitable Config (Path/Version/BuildMachine trigger) to start the test\n";
            return false;
        }
        
        ArrayList<String> folders = new ArrayList<String>(Arrays.asList("release", "debug", "debug_fsm", "tests"));
        for (String folder : folders){
        	softwareUtiles.setSourceServer(buildPath + File.separator + folder);
        	if (softwareUtiles.setBuildFiles(dut.getArchitecture() , fileType)){
        		buildPath = softwareUtiles.getSourceServer().getPath();
        		report.report("Build path is: " +  buildPath);
        		return true;
        	}
        }
        report.report("There are no suitable files to upgrade on " + buildPath, Reporter.WARNING);
        return false;
	}
	public boolean isStatusOK(EnodeB node, String targetVer){
		GeneralUtils.startLevel("results");
		SoftwareStatus result = softwareUtiles.getSoftwareStatus(node.getNetspanName(), node.getImageType());
		if(result.NmsStatus.contains("Idle")){
			report.report("Netspan state is Idle as expected");
		}
		else{
			report.report("Netspan state is " + result.NodeState + " instead of Idle", Reporter.FAIL);
			isPass = false;
		}
		if(isUpgrade){
			if(result.RunningVersion.equals(targetVer)){
				report.report("installed version is : " + result.RunningVersion);
			}
			else{
				report.report("installed version is : " + result.RunningVersion, Reporter.FAIL);
				isPass = false;
			}
		}
		else {
			if(result.StandbyVersion.equals(targetVer)){
				report.report("standby version is " + result.StandbyVersion + " as expected");
			}
			else {
				report.report("Standby version is " + result.StandbyVersion + " instead of " + targetVer, Reporter.FAIL);
				isPass = false;
			}
		}
		
		if(result.NodeState.contains("Idle")){
			report.report("Node state is: " + result.NodeState);
		}
		else{
			report.report("Node state is: " + result.NodeState, Reporter.FAIL);
			isPass = false;
		}

		GeneralUtils.stopLevel();
		return isPass;
	}
	
	private boolean setSourcePath() {
        if (buildPath != null) {
            if (buildPath.startsWith("\\") && !buildPath.startsWith("\\\\"))
            	buildPath = "\\" + buildPath;

            softwareUtiles.setSourceServer(buildPath);
            report.report("Build path is: " + buildPath);
            return true;
        } else {
            return false;
        }

    }
	private void uploadSWerrorFile(EnodeB enb) {
		try{
			ScpClient scpCli = new ScpClient(enb.getIpAddress(), enb.getScpUsername(), enb.getScpPassword(), enb.getScpPort());
			enb.grantPermission("/dev/shm");
			scpCli.getFiles("", "/dev/shm/swdlerr");
			
			File swdlerrFile = new File("/swdlerr");
			File swdlerrFileUniqName = new File(enb.getName() + "-swdlerr");
			swdlerrFile.renameTo(swdlerrFileUniqName);
			report.report(swdlerrFile.getName());
			ReporterHelper.copyFileToReporterAndAddLink(report, swdlerrFileUniqName, enb.getName()+"'s swdlerr file");
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	
	@ParameterProperties(description = "First DUT")
	public void setDut(String dut) {
		GeneralUtils.printToConsole("Load DUT " + dut);
		this.dut = (EnodeB) SysObjUtils.getInstnce().initSystemObject(EnodeB.class, false, dut).get(0);
		GeneralUtils.printToConsole("DUT loaded" + this.dut.getName());
	}
	public EnodeB getDut() {
		return dut;
	}
	
}
package testsNG.General;

import EnodeB.EnodeB;
import EnodeB.EnodeBUpgradeImage;
import EnodeB.EnodeBUpgradeServer;
import EnodeB.Components.XLP.XLP.SwStatus;
import TestingServices.SWUpgradeConfig;
import TestingServices.TestConfig;
import Utils.GeneralUtils;
import Utils.Pair;
import Utils.ScpClient;
import Utils.SysObjUtils;
import Utils.Triple;
import Netspan.API.Enums.HardwareCategory;
import Netspan.API.Enums.ServerProtocolType;
import Netspan.API.Lte.EventInfo;
import Netspan.API.Software.RequestType;
import jsystem.framework.ParameterProperties;
import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;
import jsystem.framework.report.ReporterHelper;
import org.junit.Test;

import Action.EnodebAction.Enodeb;
import testsNG.Actions.EnodeBConfig;
import testsNG.Actions.SoftwareUtiles;
import testsNG.Actions.SoftwareUtiles.EnodebResetWorker;
import testsNG.Actions.SoftwareUtiles.EnodebSwStatus;
import testsNG.Actions.SoftwareUtiles.EnodebUpgradeWorker;
import testsNG.Actions.SoftwareUtiles.SWUpgradeConnectionMethod;
import testsNG.Actions.SoftwareUtiles.VersionCopyWorker;
import testsNG.TestspanTest;

import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SWUpgrade extends TestspanTest {

    private String build;
    private ArrayList<EnodeB> duts;
    private TestConfig tc;
    private String buildM;
    private String pathBySut;
    private SoftwareUtiles softwareUtiles;
    private EnodeBUpgradeServer enbUpgrade;
    private EnodeBConfig enbConfig;

    @SuppressWarnings("unchecked")
	@Override
    public void init() throws Exception {
    	tc = TestConfig.getInstace();
    	softwareUtiles = SoftwareUtiles.getInstance();
    	enbConfig = EnodeBConfig.getInstance();
        enbInTest = (ArrayList<EnodeB>) duts.clone();
        super.init();

        buildM = System.getProperty("BuildMachineVerPath");
        if (buildM == null) {
        	SWUpgradeConfig swUpgreadeinputs = tc.getSwUpgradeConfig();
        	if(swUpgreadeinputs == null){
        		report.report("No inputs for the test");
        		reason = "No inputs for the test";
        		return;
        	}
        	
            build = swUpgreadeinputs.getBuild();
            pathBySut = swUpgreadeinputs.getAbsolutPath();
        }
        else
        	build = getVersionFromPath(buildM);

        enbUpgrade = SysObjUtils.getInstnce().initSystemObject(EnodeBUpgradeServer.class, false, "UpgradeServer").get(0);
    }

    @Test
    @TestProperties(name = "Software Upgrade", returnParam = {"IsTestWasSuccessful"}, paramsExclude = {"IsTestWasSuccessful"})
    public void softwareUpgradeTest() throws Exception {
        softwareUtiles.setDestServer(new File(File.separator + File.separator + enbUpgrade.getUpgradeServerIp() + File.separator + "tftp"));
        swUpgrade(ServerProtocolType.TFTP);
    }
	
	@Test
    @TestProperties(name = "Software Upgrade SFTP", returnParam = {"IsTestWasSuccessful"}, paramsExclude = {"IsTestWasSuccessful"})
    public void softwareUpgradeSFTP() throws Exception {
        softwareUtiles.setDestServer(new File(File.separator + File.separator + enbUpgrade.getUpgradeServerIp() + File.separator + "sftp"+ File.separator +"upload"));
		swUpgrade(ServerProtocolType.SFTP);
    }
	
	@Test
    @TestProperties(name = "Software Upgrade Via Netspan", returnParam = {"IsTestWasSuccessful"}, paramsExclude = {"IsTestWasSuccessful"})
    public void softwareUpgradeFromNetspan() {
		isPass = true;
		ArrayList<Pair<EnodeB, Triple<Integer, String, String>>> eNodebList = new ArrayList<Pair<EnodeB, Triple<Integer, String, String>>>();
		for(EnodeB dut : duts){
			GeneralUtils.startLevel("Update Default Software Image For " + dut.getName());
			Triple<Integer, String, String> swActivationDetails = softwareUtiles.updatDefaultSoftwareImage(dut);
			if(swActivationDetails.getLeftElement() >= 0){
				eNodebList.add(new Pair<EnodeB, Triple<Integer, String, String>>(dut, swActivationDetails));
			}else{
				report.report("Failed to Update Software Image - Ignoring " + dut.getName() + " for the rest of test.", Reporter.FAIL);
				isPass = false;
			}
			GeneralUtils.stopLevel();
		}
		
		
		long softwareActivateStartTimeInMili = System.currentTimeMillis();
		for(Pair<EnodeB, Triple<Integer, String, String>> eNodeB : eNodebList){
			netspanServer.softwareConfigSet(eNodeB.getElement0().getNetspanName(), RequestType.ACTIVATE, eNodeB.getElement0().getDefaultNetspanProfiles().getSoftwareImage());
		}
		GeneralUtils.unSafeSleep(10 * 1000);
		ArrayList<EnodebSwStatus> eNodebSwStausList = softwareUtiles.followSoftwareActivationProgressViaNetspan(softwareActivateStartTimeInMili, eNodebList);
		
		softwareUtiles.waitForAllRunningAndInService(softwareActivateStartTimeInMili, eNodebSwStausList);
		
		isPass = softwareUtiles.validateRunningVersion(eNodebSwStausList);
        if(isPass){        	
        	report.addProperty("SoftwareUpgradeType", SWUpgradeConnectionMethod.Netspan.toString());
        	report.report("SW upgrade test passed successfully");
        }else{
        	report.report("One or more of the enodeBs hasn't been updated", Reporter.FAIL);
        	reason += "One or more of the enodeBs hasn't been updated";
        }
    }

	private void swUpgrade(ServerProtocolType downloadType)  {

        ExecutorService executorService = Executors.newCachedThreadPool();
        ArrayList<VersionCopyWorker> versionCopyWorkerList;
        ArrayList<EnodebUpgradeWorker> enodebUpgradeWorkers = new ArrayList<>();
        ArrayList<EnodebResetWorker> enodebResetWorkers = new ArrayList<>(duts.size());
        HashSet<EnodeB.Architecture> types = new HashSet<>();
        
        if(!setPathAndFiles())  	
            return;

        GeneralUtils.startLevel("Checking If there is any Enodeb to upgrade");
        ArrayList<EnodeB> updateViaNetspan = new ArrayList<>();
        ArrayList<EnodeB> updateViaSnmp = new ArrayList<>();
        for (EnodeB enodeB : duts) {
        	enodeB.setLoggerDebugCapEnable(true);
        	GeneralUtils.printToConsole("db get loggerconfig debugcapenable :\n" + enodeB.lteCli("db get loggerconfig debugcapenable"));
            types.add(enodeB.getArchitecture());
            enbConfig.setGranularityPeriod(enodeB,1);
            if (softwareUtiles.isVersionExists(enodeB)){
            	updateViaSnmp.add(enodeB);
            	report.report("The Enodeb " + enodeB.getNetspanName() + " contains version " + build + ", so sw upgrade will be done via SNMP");
            }
            else{
            	updateViaNetspan.add(enodeB);
            	report.report("The Enodeb " + enodeB.getNetspanName() + " does not contain version " + build + ", so sw upgrade will be done via Netspan");
            }
        }
        GeneralUtils.stopLevel();


        /**
         * setting the size of the list of the objects that manages the build copying
         */
        versionCopyWorkerList = new ArrayList<>(types.size());

        /**
         * Copying the relevant builds from the source server, to the upgrade server
         */
        GeneralUtils.startLevel("Copy files to Upgrade server");
        for (EnodeB.Architecture type : types) {
            VersionCopyWorker tempWorker = softwareUtiles.new VersionCopyWorker(type,downloadType);
            versionCopyWorkerList.add(tempWorker);
            tempWorker.startCopy();
        }

        /**
         * Displaying the logs of the copies that has been made
         */
        boolean breakTest = false;
        for (VersionCopyWorker worker : versionCopyWorkerList) {
            breakTest = breakTest || !worker.isPass();
        }
        GeneralUtils.stopLevel();
        if (breakTest) {
            report.report("One of the copies failed", Reporter.WARNING);
            isPass=false;
            report.report("Copy process fail!", Reporter.WARNING);
        }
        EnodeBUpgradeServer server = new EnodeBUpgradeServer();
        
       
        HashMap<String, String> imageNames = new HashMap<String,String>();
        for (EnodeB enodeB : updateViaNetspan) {
        	GeneralUtils.startLevel("Prepare netspan for software upgrade");        	
    		server.setUpgradeServerObject(enbUpgrade.getUpgradeServerIp()+ "_" + downloadType.toString(), enbUpgrade.getUpgradeServerIp(), downloadType, enbUpgrade.getUpgradeUser(), enbUpgrade.getUpgradePassword());

        	softwareUtiles.createUpgradeServer(server);
        	String targetVer = softwareUtiles.getCurrentBuild(enodeB);
        	HardwareCategory hardwareCategory = enbConfig.getHardwareCategory(enodeB);
        	String imageName = targetVer + "_" + server.getUpgradeServerProtocolType().toString() + "_Automation_" + hardwareCategory;
			imageNames.put(enodeB.getName(), imageName);
			String buildName  = (enodeB.getArchitecture() == EnodeB.Architecture.FSM)? softwareUtiles.getFSMBuild() : softwareUtiles.getXLPBuild();
			String imageType = netspanServer.getSoftwareStatus(enodeB.getNetspanName(), null).ImageType;
			softwareUtiles.createUpgradeImage(server, buildName, imageName, targetVer, hardwareCategory, imageType);

			GeneralUtils.stopLevel();
        }

        /**
         * Start Updating the nodes simultaneously
         */
        GeneralUtils.startLevel("Download version");
        executorService = Executors.newCachedThreadPool();
        for (EnodeB enodeB : updateViaNetspan) {   
        	EnodebUpgradeWorker worker = softwareUtiles.new EnodebUpgradeWorker(build, enodeB, downloadType,enbUpgrade.getUpgradeUser(),enbUpgrade.getUpgradePassword());
        	worker.setConnectionMethod(SWUpgradeConnectionMethod.Netspan);
        	worker.setImageName(imageNames.get(enodeB.getName()));
            enodebUpgradeWorkers.add(worker);
            executorService.execute(worker);
        }
        
        for (EnodeB enodeB : updateViaSnmp) {
            EnodebUpgradeWorker worker = softwareUtiles.new EnodebUpgradeWorker(build, enodeB, downloadType,enbUpgrade.getUpgradeUser(),enbUpgrade.getUpgradePassword());
            worker.setConnectionMethod(SWUpgradeConnectionMethod.SNMP);
            enodebUpgradeWorkers.add(worker);
            executorService.execute(worker);
        }
        executorService.shutdown();
        try {
			if(!executorService.awaitTermination(EnodeB.UPGRADE_TIMEOUT, TimeUnit.MILLISECONDS))
			{
				report.report("Upgrade Worker not finished, will wait 10 more mintues");
				executorService.awaitTermination((long)10 * 60 * 1000, TimeUnit.MILLISECONDS);
			}
		} catch (InterruptedException e1) {
			report.report("Error while waiting for upgrade worker to finish", Reporter.WARNING);
			e1.printStackTrace();
		}

        /**
         * Validating upgrade and displaying all of the logs in Logger
         */
        GeneralUtils.unSafeSleep(1000 * 10);
        SWUpgradeConnectionMethod lowestConnectionMethod = SWUpgradeConnectionMethod.Netspan;
        for (EnodebUpgradeWorker worker : enodebUpgradeWorkers) {
        	if (lowestConnectionMethod == null) {
        		lowestConnectionMethod = worker.getConnectionMethod();
			}
        	else{
	        	if (worker.getConnectionMethod().ordinal() < lowestConnectionMethod.ordinal()) {
	        		lowestConnectionMethod = worker.getConnectionMethod();
				}
        	}
        	GeneralUtils.printToConsole("lowestConnectionMethod is " + lowestConnectionMethod.name() + ", ordinal is: " + lowestConnectionMethod.ordinal());
            if (worker.isPass()) {
                report.report("Time to Download new Version to EnodeB " + worker.getEnodeB().getNetspanName() + " with " + worker.getConnectionMethod().name() + " Is: " + milisToFormat(worker.getTime()));
                
            }else{
            	EnodeB enb = worker.getEnodeB();
                report.report("EnodeB: " + enb.getNetspanName() + " Failed to Update");
                uploadSWerroFile(enb);
            }
        }
        GeneralUtils.stopLevel();


        /**
         * Restarting all of the EnodeBs and waiting for all running on the slowest one
         */
        GeneralUtils.startLevel("Reset and Wait for all running");
        executorService = Executors.newCachedThreadPool();
        for (EnodeB enodeB : updateViaSnmp) {
            GeneralUtils.printToConsole("Reseting Enodeb:" + enodeB.getNetspanName());
            EnodebResetWorker worker = softwareUtiles.new EnodebResetWorker(enodeB);
            worker.connectionMethod = SWUpgradeConnectionMethod.SNMP;
            enodebResetWorkers.add(worker);
            executorService.execute(worker);

        }
        for (EnodeB enodeB : updateViaNetspan) {
            GeneralUtils.printToConsole("Reseting Enodeb:" + enodeB.getNetspanName());
            EnodebResetWorker worker = softwareUtiles.new EnodebResetWorker(enodeB);
            worker.connectionMethod = SWUpgradeConnectionMethod.Netspan;
            worker.imageName = imageNames.get(enodeB.getName());
            enodebResetWorkers.add(worker);
            executorService.execute(worker);

        }
        executorService.shutdown();
        try {
			executorService.awaitTermination(EnodeB.WAIT_FOR_ALL_RUNNING_TIME + 12*60*1000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e1) {
			report.report("Error while waiting for reset worker to finish", Reporter.WARNING);
			e1.printStackTrace();
		}

        for (EnodebResetWorker worker : enodebResetWorkers) {
            if (worker.isPass()){
                report.report("Time to Activate (Reset + All running) EnodeB: " + worker.getEnodeB().getNetspanName() + " Is: " + milisToFormat(worker.getTime()));
            }
            else {
            	report.report("EnodeB " + worker.getEnodeB().getNetspanName() + " Failed Reset and Wait for all running stage", Reporter.WARNING);
            }
        }
        GeneralUtils.stopLevel();
        
        GeneralUtils.startLevel("Validation");
    	boolean validateUpdate = true;
        for(EnodeB dut : enbInTest){
        	dut.setLoggerDebugCapEnable(true);
        	GeneralUtils.printToConsole("db get loggerconfig debugcapenable :\n" + dut.lteCli("db get loggerconfig debugcapenable"));
        	if(softwareUtiles.noValidationNodes().contains(dut)){
        		report.report(dut.getName()+" had a version issue and will not be validate since version did not get all Running");
        		validateUpdate = false;
        		continue;
        	}
        	validateUpdate = validateUpdate && softwareUtiles.isVersionUpdated(dut,Reporter.WARNING);
        }
        GeneralUtils.stopLevel();
        isPass = validateUpdate;
        if(isPass){        	
        	report.addProperty("SoftwareUpgradeType", lowestConnectionMethod.name());
        	report.report("SW upgrade test passed successfully");
        }else{
        	report.report("One or more of the enodeBs hasn't been updated", Reporter.FAIL);
        	reason += "One or more of the enodeBs hasn't been updated";
        }
	}
	
	private void uploadSWerroFile(EnodeB enb) {
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

	private boolean setPathAndFiles() {
		//if No inputs for the test
        if (!setSourcePath()) {
            report.report("There is no suitable Config (Path/Version/BuildMachine trigger) to start the test", Reporter.FAIL);
            isPass=false;
            reason += "FAIL: There is no suitable Config (Path/Version/BuildMachine trigger) to start the test\n";
            return false;
        }

        //if No files to upgrade
        if (!softwareUtiles.setBuilds()) {
            report.report("There are no suitable files to upgrade", Reporter.FAIL);
            isPass=false;
            reason += "FAIL: There are no suitable files to upgrade\n";
            return false;
        }
		return true;
	}

    private boolean setSourcePath() {
        if (buildM != null) {
            if (buildM.startsWith("\\") && !buildM.startsWith("\\\\"))
                buildM = "\\" + buildM;

            softwareUtiles.setSourceServer(buildM);
            report.report("Upgrading all components available to build Triggered By BuildMachine: " + buildM);
            return true;
        } else if (pathBySut != null) {
            pathBySut = pathBySut.trim();
            softwareUtiles.setSourceServer(pathBySut);
            report.report("Upgrading all components available to build: " + pathBySut);
            return true;
        } else if (build != null) {
            softwareUtiles.setSourceServerNoPath(build);
            report.report("Upgrading all components available to build: " + build);
            return true;
        } else {
            return false;
        }

    }

    @ParameterProperties(description = "Name of All Enb comma seperated e.g enb1,enb2")
    public void setDUTs(String dut) {
        this.duts = (ArrayList<EnodeB>) SysObjUtils.getInstnce().initSystemObject(EnodeB.class, false, dut.split(","));
    }

    public ArrayList<EnodeB> getDuts() {
        return duts;
    }

    private String milisToFormat(long milis) {
        return String.format("%d min, %d sec",
                TimeUnit.MILLISECONDS.toMinutes(milis),
                TimeUnit.MILLISECONDS.toSeconds(milis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milis))
        );
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
}

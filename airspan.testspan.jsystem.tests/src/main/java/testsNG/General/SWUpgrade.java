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
import Netspan.API.Enums.ServerProtocolType;
import Netspan.API.Lte.EventInfo;
import Netspan.API.Software.RequestType;
import jsystem.framework.ParameterProperties;
import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;
import jsystem.framework.report.ReporterHelper;
import org.junit.Test;
import org.python.modules.re;

import com.google.protobuf.Internal.BooleanList;

import testsNG.Actions.EnodeBConfig;
import testsNG.Actions.SoftwareUtiles;
import testsNG.Actions.SoftwareUtiles.EnodebResetWorker;
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
    private TestConfig tc = TestConfig.getInstace();
    private String buildM;
    private String pathBySut;
    private SoftwareUtiles softwareUtiles = SoftwareUtiles.getInstance();
    private EnodeBUpgradeServer enbUpgrade;
    private EnodeBConfig enbConfig = EnodeBConfig.getInstance();

    @SuppressWarnings("unchecked")
	@Override
    public void init() throws Exception {
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
		ArrayList<EnodebSwStatus> eNodebSwStausList = new ArrayList<EnodebSwStatus>();
		ArrayList<EnodebSwStatus> eNodebThatNeedsRebootSwStausList = new ArrayList<EnodebSwStatus>();
		ArrayList<EnodebSwStatus> eNodebThatNeedsSecondRebootSwStausList = new ArrayList<EnodebSwStatus>();
		for(EnodeB dut : duts){
			GeneralUtils.startLevel("Update Default Software Image For " + dut.getName());
			int numberOfExpectedReboots = softwareUtiles.updatDefaultSoftwareImage(dut);
			eNodebSwStausList.add(new EnodebSwStatus(dut, numberOfExpectedReboots));
			GeneralUtils.stopLevel();
		}
		
		
		long softwareActivateStartTimeInMili = System.currentTimeMillis();
		for(EnodebSwStatus eNodebSwStaus : eNodebSwStausList){
			EnodeBUpgradeImage enodeBUpgradeImage = netspanServer.getSoftwareImage(eNodebSwStaus.eNodeB.getDefaultNetspanProfiles().getSoftwareImage());
			EnodeBUpgradeServer enodeBUpgradeServer = netspanServer.getSoftwareServer(enodeBUpgradeImage.getUpgradeServerName());
			GeneralUtils.startLevel(eNodebSwStaus.eNodeB.getName() + "'s Software Image Details.");
			report.report("Software Image = " + enodeBUpgradeImage.getName());
			report.report("Software Version = " + enodeBUpgradeImage.getVersion());
			report.report("Software Server = " + enodeBUpgradeImage.getUpgradeServerName());
			report.report("Software Server IP = " + enodeBUpgradeServer.getUpgradeServerIp());
			report.report("Software Server Protocol Type = " + enodeBUpgradeServer.getUpgradeServerProtocolType());
			GeneralUtils.stopLevel();
			if(eNodebSwStaus.numberOfExpectedReboots > 0){
				eNodebSwStaus.eNodeB.setExpectBooting(true);
				eNodebThatNeedsRebootSwStausList.add(eNodebSwStaus);
				if(eNodebSwStaus.numberOfExpectedReboots > 1){
					eNodebThatNeedsSecondRebootSwStausList.add(eNodebSwStaus);
				}
			}
			
			netspanServer.softwareConfigSet(eNodebSwStaus.eNodeB.getNetspanName(), RequestType.ACTIVATE, eNodebSwStaus.eNodeB.getDefaultNetspanProfiles().getSoftwareImage());
		}
		GeneralUtils.unSafeSleep(10 * 1000);
		if(!eNodebThatNeedsRebootSwStausList.isEmpty()){
			followSoftwareActivationProgress(softwareActivateStartTimeInMili, eNodebThatNeedsRebootSwStausList);
			if(!eNodebThatNeedsSecondRebootSwStausList.isEmpty()){
				for(EnodebSwStatus eNodebSwStaus : eNodebThatNeedsSecondRebootSwStausList){
					eNodebSwStaus.receivedEventIndex = 0;
				}
				followSoftwareActivationProgress(System.currentTimeMillis(), eNodebThatNeedsSecondRebootSwStausList);
			}
		}
		
		waitForAllRunningAndInService(softwareActivateStartTimeInMili, eNodebThatNeedsRebootSwStausList);
		
		validateRunningVersion(eNodebSwStausList);
    }

	private void followSoftwareActivationProgress(long softwareActivateStartTimeInMili, ArrayList<EnodebSwStatus> eNodebSwStausList) {
		Date softwareActivateStartTimeInDate = new Date(softwareActivateStartTimeInMili);
		GeneralUtils.startLevel("Verify Software Activation.");
		@SuppressWarnings("unchecked")
		ArrayList<EnodebSwStatus> eNodebSwStausListTmp = (ArrayList<EnodebSwStatus>) eNodebSwStausList.clone();
		GeneralUtils.startLevel("Verify Software Download.");
		do {
			ArrayList<EnodebSwStatus> eNodebSwStausListToRemove = new ArrayList<EnodebSwStatus>();
			for(EnodebSwStatus eNodebSwStaus : eNodebSwStausListTmp){
				Pair<Boolean, SwStatus> swStatus = eNodebSwStaus.eNodeB.isSoftwareDownloadCompletedSuccessfully(); 
				eNodebSwStaus.swStatus = swStatus.getElement1();
				if((eNodebSwStaus.swUpgradeEventInfoList.length <= eNodebSwStaus.receivedEventIndex) || (swStatus.getElement0())){
					eNodebSwStaus.isSwDownloadCompleted = true;
					eNodebSwStausListToRemove.add(eNodebSwStaus);
				}else if((eNodebSwStaus.swStatus == SwStatus.SW_STATUS_INSTALL_FAILURE || eNodebSwStaus.swStatus == SwStatus.SW_STATUS_ACTIVATION_FAILURE)){
					eNodebSwStaus.isSwDownloadCompleted = false;
					eNodebSwStausListToRemove.add(eNodebSwStaus);
				}else{
					eNodebSwStaus.isSwDownloadCompleted = false;
				}
				GeneralUtils.unSafeSleep(10 * 1000);
				eNodebSwStaus.reportUploadedNetspanEvent(softwareActivateStartTimeInDate);
			}
			for(EnodebSwStatus eNodebSwStaus : eNodebSwStausListToRemove){
				eNodebSwStausListTmp.remove(eNodebSwStaus);
			}
			
		}while ((!eNodebSwStausListTmp.isEmpty()) && (System.currentTimeMillis() - softwareActivateStartTimeInMili <= (EnodeB.UPGRADE_TIMEOUT/3)));
		for(EnodebSwStatus eNodebSwStaus : eNodebSwStausListTmp){
			if(eNodebSwStaus.isSwDownloadCompleted == false){
				report.report(eNodebSwStaus.eNodeB.getName() + ": Software Download Didn't End.", Reporter.WARNING);
			}
		}
		GeneralUtils.stopLevel();
		waitForReboot(eNodebSwStausList, softwareActivateStartTimeInDate, 5 * 60 * 1000);
		GeneralUtils.stopLevel();
	}
	
	private void waitForReboot(ArrayList<EnodebSwStatus> eNodebSwStausList, Date softwareActivateStartTimeInDate, long timeout) {
		GeneralUtils.startLevel("Verify Software Activation.");
		@SuppressWarnings("unchecked")
		ArrayList<EnodebSwStatus> eNodebSwStausListTmp = (ArrayList<EnodebSwStatus>) eNodebSwStausList.clone();
		final long waitForRebootStartTime = System.currentTimeMillis();
		do{
			ArrayList<EnodebSwStatus> eNodebSwStausListToRemove = new ArrayList<EnodebSwStatus>();
			for(EnodebSwStatus eNodebSwStaus : eNodebSwStausListTmp){
				eNodebSwStaus.reportUploadedAllNetspanEvents(softwareActivateStartTimeInDate);
				GeneralUtils.printToConsole(eNodebSwStaus.eNodeB.getName() + ".isExpectBooting() = " + eNodebSwStaus.eNodeB.isExpectBooting());
				if(eNodebSwStaus.eNodeB.isExpectBooting() == false){
					eNodebSwStausListToRemove.add(eNodebSwStaus);
					eNodebSwStaus.numberOfReboot++;
					if(eNodebSwStaus.numberOfReboot < eNodebSwStaus.numberOfExpectedReboots){
						eNodebSwStaus.eNodeB.setExpectBooting(true);
					}
				}
			}
			for(EnodebSwStatus eNodebSwStaus : eNodebSwStausListToRemove){
				eNodebSwStausListTmp.remove(eNodebSwStaus);
			}
			GeneralUtils.unSafeSleep(5000);
		}while((!eNodebSwStausListTmp.isEmpty()) &&(System.currentTimeMillis() - waitForRebootStartTime <= timeout));
		for(EnodebSwStatus eNodebSwStaus : eNodebSwStausListTmp){
			report.report(eNodebSwStaus.eNodeB.getName() + " has NOT been rebooted.", Reporter.WARNING);
		}
		GeneralUtils.stopLevel();
	}

	private void waitForAllRunningAndInService(long softwareActivateStartTimeInMili, ArrayList<EnodebSwStatus> eNodebSwStausList) {
		@SuppressWarnings("unchecked")
		ArrayList<EnodebSwStatus> eNodebSwStausListTmp = (ArrayList<EnodebSwStatus>) eNodebSwStausList.clone();
		GeneralUtils.startLevel("Wait For ALL RUNNING And In Service.");
		while((!eNodebSwStausListTmp.isEmpty()) && (System.currentTimeMillis() - softwareActivateStartTimeInMili <= (EnodeB.UPGRADE_TIMEOUT))){
			ArrayList<EnodebSwStatus> eNodebSwStausListToRemove = new ArrayList<EnodebSwStatus>();
			for(EnodebSwStatus eNodebSwStaus : eNodebSwStausListTmp){
				if(eNodebSwStaus.isSwDownloadCompleted){
					if(eNodebSwStaus.eNodeB.isInOperationalStatus()){
						eNodebSwStaus.isSwUpgradeCompleted = true;
						eNodebSwStausListToRemove.add(eNodebSwStaus);
						report.report(eNodebSwStaus.eNodeB.getName() + " is in Running State.", Reporter.PASS);
					}
				}else if ((eNodebSwStaus.swStatus == SwStatus.SW_STATUS_INSTALL_FAILURE || eNodebSwStaus.swStatus == SwStatus.SW_STATUS_ACTIVATION_FAILURE)) {
					eNodebSwStaus.isSwUpgradeCompleted = false;
					eNodebSwStausListToRemove.add(eNodebSwStaus);
					report.report(eNodebSwStaus.eNodeB.getName() + ": Software Upgrade Failed ("+eNodebSwStaus.swStatus+").", Reporter.FAIL);
				}else{
					eNodebSwStaus.isSwUpgradeCompleted = false;
				}
			}
			for(EnodebSwStatus eNodebSwStaus : eNodebSwStausListToRemove){
				eNodebSwStausListTmp.remove(eNodebSwStaus);
			}
			GeneralUtils.unSafeSleep(5 * 1000);
		}
		for(EnodebSwStatus eNodebSwStaus : eNodebSwStausListTmp){
			if(eNodebSwStaus.isSwDownloadCompleted == false){
				report.report(eNodebSwStaus.eNodeB.getName() + ": Didn't Reach To Running State.", Reporter.FAIL);
			}
		}
		GeneralUtils.stopLevel();
	}
	
	private void validateRunningVersion(ArrayList<EnodebSwStatus> eNodebSwStausList) {
		GeneralUtils.startLevel("Validate Running Version.");
		for(EnodebSwStatus eNodebSwStaus : eNodebSwStausList){
			softwareUtiles.isUpdatedViaSnmp(eNodebSwStaus.eNodeB, Reporter.FAIL);
		}
		GeneralUtils.stopLevel();
	}

	private class EnodebSwStatus{
		public final String[] swUpgradeEventInfoList = new String[]{
			"Download in progress",
			"Download completed",
			"Activate in progress",
			"Activate completed"
		};
		public EnodeB eNodeB;
		public SwStatus swStatus;
		public boolean isSwDownloadCompleted;
		public boolean isSwUpgradeCompleted;
		public int receivedEventIndex;
		public final int numberOfExpectedReboots;
		public int numberOfReboot;
				
		public EnodebSwStatus(EnodeB eNodeB, int numberOfExpectedReboots) {
			this.eNodeB = eNodeB;
			this.swStatus = SwStatus.SW_STATUS_IDLE;
			this.isSwDownloadCompleted = false;
			this.isSwUpgradeCompleted = false;
			this.receivedEventIndex = 0;
			this.numberOfReboot = 0;
			this.numberOfExpectedReboots = numberOfExpectedReboots;
		}
		
		public void reportUploadedNetspanEvent(Date softwareActivateStartTimeInDate){
			List<EventInfo> eventInfoListFromNMS = alarmsAndEvents.getAllEventsNode(eNodeB, softwareActivateStartTimeInDate, new Date(System.currentTimeMillis()));
			for(EventInfo eventInfo :eventInfoListFromNMS){
				if(swUpgradeEventInfoList.length > receivedEventIndex){
					if(eventInfo.getEventInfo().contains(swUpgradeEventInfoList[receivedEventIndex])){
						GeneralUtils.startLevel(eNodeB.getName() + "-"+eventInfo.getSourceType()+": " + 
								swUpgradeEventInfoList[receivedEventIndex]);
						report.report("Event Type: " + eventInfo.getEventType());
						report.report("Source Type: " + eventInfo.getSourceType()); 
						report.report("Event Info: " + eventInfo.getEventInfo());
						report.report("Received Time: " + eventInfo.getReceivedTime().toString());
						GeneralUtils.stopLevel();
						receivedEventIndex++;
						break;
					}
				}
			}
		}
		
		public void reportUploadedAllNetspanEvents(Date softwareActivateStartTimeInDate){
			for(int i = 1; i <= swUpgradeEventInfoList.length; i++){
				reportUploadedNetspanEvent(softwareActivateStartTimeInDate);
			}
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
        	String imageName = targetVer + "_" + server.getUpgradeServerProtocolType().toString() + "_Automation";
			imageNames.put(enodeB.getName(), imageName);
			String buildName  = (enodeB.getArchitecture() == EnodeB.Architecture.FSM)? softwareUtiles.getFSMBuild() : softwareUtiles.getXLPBuild();
			softwareUtiles.createUpgradeImage(server, buildName, imageName, targetVer, enodeB);

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
			executorService.awaitTermination(EnodeB.WAIT_FOR_ALL_RUNNING_TIME + 60*1000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e1) {
			report.report("Error while waiting for reset worker to finish", Reporter.WARNING);
			e1.printStackTrace();
		}

        for (EnodebResetWorker worker : enodebResetWorkers) {
            if (worker.isPass()){
                report.report("Time to Activate (Reset + All running) EnodeB: " + worker.getEnodeB().getNetspanName() + " Is: " + milisToFormat(worker.getTime()));
            }
            else {
            	report.report("EnodeB " + worker.getEnodeB().getNetspanName() + " Failed to reach All-Running", Reporter.FAIL);
            }
        }
        GeneralUtils.stopLevel();
        
        GeneralUtils.startLevel("Validation");
    	boolean validateUpdate = true;
        for(EnodeB dut : enbInTest){
        	if(softwareUtiles.noValidationNodes().contains(dut)){
        		report.report(dut.getName()+" had a version issue and will not be validate since version did not get all Running");
        		continue;
        	}
        	validateUpdate = validateUpdate && softwareUtiles.isUpdatedViaSnmp(dut,Reporter.WARNING);
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

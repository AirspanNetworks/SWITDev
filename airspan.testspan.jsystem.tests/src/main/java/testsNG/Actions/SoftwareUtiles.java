package testsNG.Actions;

import EnodeB.EnodeB;
import EnodeB.EnodeBUpgradeImage;
import EnodeB.EnodeBUpgradeServer;
import EnodeB.Components.EnodeBComponentTypes;
import EnodeB.EnodeB.Architecture;
import Netspan.NetspanServer;
import Netspan.API.Enums.ImageType;
import Netspan.API.Enums.ServerProtocolType;
import Netspan.API.Lte.EventInfo;
import Netspan.API.Software.RequestType;
import Netspan.API.Software.SoftwareStatus;
import TestingServices.TestConfig;
import Utils.GeneralUtils;
import Utils.Pair;
import jsystem.framework.report.ListenerstManager;
import jsystem.framework.report.Reporter;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static java.nio.file.StandardCopyOption.*;

/**
 * Created by owiesel on 10-May-16. Software Upgrade Test Helper
 */

public class SoftwareUtiles {
	public enum SWUpgradeConnectionMethod {
		CLI, SNMP, Netspan
 	}
    /**
     * the version that needs to be copied
     **/
    private String FSMBuild = "";
    private String XLPBuild = "";
    /**
     * the source of the copy
     **/
    private File sourceServer = new File("P:\\BS\\Development\\Air4G_LTE\\Builds\\");
    /**
     * the destination of the copy
     **/
    private File destServer;

    /**
     * The enodeB that called for this thread
     */
    private Reporter report = ListenerstManager.getInstance();

    private Pair<String, Integer> reason;

    /**
     * instance for singleton
     */
    private static SoftwareUtiles instance = null;
    private boolean warned = false;
    NetspanServer netspanServer;
    private ArrayList<EnodeB> notToValidateNodes;

    
    
    private SoftwareUtiles() {
    	try {
			netspanServer = NetspanServer.getInstance();
			notToValidateNodes = new ArrayList<EnodeB>();
		} catch (Exception e) {
			report.report("Can't load NetspanServer Instance");
			e.printStackTrace();
		}
    }

    public static SoftwareUtiles getInstance() {
        if (instance == null) {
            instance = new SoftwareUtiles();
        }
        return instance;
    }

    public boolean setBuilds() {
        boolean status = false;
        for (int i = 0; i < 4; i++) {
            status = setBuildFiles();
            if (status)
                return true;
            else
                GeneralUtils.unSafeSleep(30 * 1000);
        }
        return false;
    }
    
    public boolean setBuilds(EnodeB.Architecture architercture, String fileType) {
        boolean status = false;
        for (int i = 0; i < 4; i++) {
            status = setBuildFiles(architercture, fileType);
            if (status)
                return true;
            else
                GeneralUtils.unSafeSleep(30 * 1000);
        }
        return false;
    }

    public boolean setBuildFiles() {
        boolean flagXLP = true;
        boolean flagFSM = true;
        try {
            File[] files = sourceServer.listFiles();
            for (File file : files) {
                GeneralUtils.printToConsole("Checking File:" + file.toString());
                if (file.getName().contains("fsm")){
                	if(file.getName().contains("swu")) {
	                    FSMBuild = file.getName();
	                    flagFSM = false;
	                } else if (file.getName().contains("enc") && flagFSM) {
	                    FSMBuild = file.getName();
	                	flagFSM = false;	                	
	                } else if (file.getName().contains("tar.gz") && flagFSM) {
	                    FSMBuild = file.getName();
	                }
                }else if (file.getName().contains("enodeb")) {
                	if(file.getName().contains("swu")) {
	                    XLPBuild = file.getName();
	                    flagXLP = false;
	                } else if (file.getName().contains("enodeb") && file.getName().contains("enc") && flagXLP) {
	                    XLPBuild = file.getName();
	                } else if (file.getName().contains("enodeb") && file.getName().contains("tar.bz2") && XLPBuild.trim() == "") {
	                    XLPBuild = file.getName();
	                }
                }
            }
            report.report("FSM Build Name is:" + FSMBuild);
            report.report("XLP Build Name is:" + XLPBuild);
        } catch (Exception e) {
        	report.report("The Path provided does not contain the relevant files", Reporter.WARNING);
            reason = Pair.createPair("The Path provided does not contain the relevant files", Reporter.WARNING);
        }
        return !(FSMBuild.equals("") && XLPBuild.equals(""));
    }
    
    /**
     * created by Moran Goldenberg
     * @param architercture
     * @param fileType
     * @return true if finds fsmbuild file or XLPbuild file according to the enodeB architecture
     */
    public boolean setBuildFiles(EnodeB.Architecture architercture, String fileType) {
            File[] files = sourceServer.listFiles();
            if (files == null)
            		return false;
            for (File file : files) {
                GeneralUtils.printToConsole("Checking File:" + file.toString());
                switch (architercture) {
				case XLP: 
					if(file.getName().contains("enodeb") && file.getName().contains(fileType)){
						XLPBuild = file.getName();
						report.report("XLP Build Name is:" + XLPBuild);
						return true;
					}
					break;
				case FSM:
					if(file.getName().contains("fsm") && file.getName().contains(fileType)){
						FSMBuild = file.getName();
						report.report("FSM Build Name is:" + FSMBuild);
						return true;
					}
					break;
				default:
					break;
				}
            }
          return false;
    }

    private boolean isStandbyUpdatedViaSnmp(EnodeB enodeB) {
        String build = getCurrentBuild(enodeB);
        String secondVersion = enodeB.getSecondaryVersion();

        GeneralUtils.printToConsole(enodeB.getNetspanName() + ": current build:" + build);
        GeneralUtils.printToConsole(enodeB.getNetspanName() + ": secondVersion:" + secondVersion);

        if (secondVersion.contains(build)) {
            GeneralUtils.printToConsole(enodeB.getNetspanName() + ": The Standby bank contains version " + build);
            report.report(enodeB.getName() +" - The Standby bank contains version " + build);
            reason = Pair.createPair("The Standby bank contains version " + build, Reporter.PASS);
            return true;
        }
        else{
	        GeneralUtils.printToConsole(enodeB.getNetspanName() + ": The Standby bank does not contains version " + build);
	        report.report(enodeB.getName() +" - The Standby bank does not contains version " + build);
	        reason = Pair.createPair("The Standby bank does not contains version " + build, Reporter.FAIL);
	        return false;
        }
    }

    public boolean isUpdatedViaSnmp(EnodeB enodeB, int logLevel) {
        String build = getCurrentBuild(enodeB).trim();
        String running = enodeB.getRunningVersion().trim();
        String standby = enodeB.getSecondaryVersion().trim();
        if (build.contains(running) || running.contains(build)) {
            report.report("The running bank contains version " + build + " on Enodeb: " + enodeB.getNetspanName());
            return true;
        } else if (build.contains(standby) || standby.contains(build)) {
            report.report("The Standby Bank contains target version: " + build + " on Enodeb: " + enodeB.getNetspanName(),logLevel);
            if (enodeB.swapBanksAndReboot()) {
            	enodeB.setUnexpectedReboot(0);
                if (enodeB.waitForAllRunningAndInService(EnodeB.WAIT_FOR_ALL_RUNNING_TIME)) {
                	String runningVersion = enodeB.getRunningVersion().trim();
                    if (build.contains(runningVersion) || runningVersion.contains(build)) {
                        report.report("The running bank contains version " + build + " on Enodeb: " + enodeB.getNetspanName());
                        return true;
                    } else {
                        report.report("The running bank does not contains version " + build + " on Enodeb: " + enodeB.getNetspanName() + " Current Build:" + runningVersion, logLevel);
                        return false;
                    }
                }else {
                    report.report("Failed to reach all running on Enodeb: " + enodeB.getNetspanName(), logLevel);
                    return false;
                }
            } else {
                report.report("Swap Bank and Reset Failed on Enodeb: " + enodeB.getNetspanName(), logLevel);
                return false;
            }
        } else {
            report.report("Nor the running bank nor the standby bank contains version " + build + " on Enodeb: " + enodeB.getNetspanName(), logLevel);
            return false;
        }
    }
    
    public boolean isVersionExists(EnodeB enodeB) {
	    String build = getCurrentBuild(enodeB).trim();
	    String running = enodeB.getRunningVersion().trim();
	    String standby = enodeB.getSecondaryVersion().trim();
	    report.report(enodeB.getName() + " running version is: " + running);
	    report.report(enodeB.getName() + " standby version is: " + standby);
	    if (running.contains(build) || standby.contains(build)) {	        
	        return true;
	    }
	    return false;
    }
    public String getCurrentBuild(EnodeB enodeB) {
        String build="";
        try{
		    if (enodeB.getArchitecture() == EnodeB.Architecture.FSM) {
		        GeneralUtils.printToConsole("Build FSM is:" + FSMBuild);
		        build = FSMBuild.split("\\.")[1];
		        build = build.replaceAll("_", ".");
		    } else {
		        GeneralUtils.printToConsole("Build XLP is:" + XLPBuild);
		        build = XLPBuild.split("\\.")[1];
		        build = build.replaceAll("_", ".");
		    }
        }
        catch(Exception e){
        	System.err.println("Failed getting current build, got: " + build);
        	build = "NoInfo";
        }
        return build;
    }

    public void setRelease(String release) {
    }

    public String getFSMBuild() {
        return FSMBuild;
    }

    public void setFSMBuild(String FSMBuild) {
        this.FSMBuild = FSMBuild;
    }

    public String getXLPBuild() {
        return XLPBuild;
    }

    public void setXLPBuild(String XLPBuild) {
        this.XLPBuild = XLPBuild;
    }

    public Pair<String, Integer> getReason() {
        Pair<String, Integer> ret = reason;
        reason = null;
        return ret;
    }

    public void setSourceServer(String sourceServer) {
        this.sourceServer = new File(sourceServer);
    }

    public void setSourceServerNoPath(String build) {
        this.sourceServer = new File(sourceServer.getPath() + makeRelease(build) + "\\" + build + "\\release\\");
    }

    public void setReason(Pair<String, Integer> reason) {
        this.reason = reason;
    }

    public void setReason(String reason, Integer result) {
        setReason(new Pair<String, Integer>(reason, result));
    }

    private String makeRelease(String build) {
        String[] verSplit = build.split("_");
        return "\\" + verSplit[1] + "." + verSplit[2];
    }

    public File getDestServer() {
        return destServer;
    }

    public void setDestServer(File destServer) {
        this.destServer = destServer;
    }

    public File getSourceServer() {
        return sourceServer;
    }

    public boolean isWarned() {
        boolean out = warned;
        warned = false;
        return out;
    }

    public void setWarned(boolean warned) {
        this.warned = warned;
    }

    /**
     * Reset Thread
     */
    public class EnodebResetWorker implements Runnable {
        private EnodeB enodeB;
        private boolean pass;
        private long time;
        public SWUpgradeConnectionMethod connectionMethod = null;
		public String imageName;
        
        public EnodebResetWorker(EnodeB enodeB) {
            this.enodeB = enodeB;
            pass = false;
        }

        @Override
        public void run() {
            time = System.currentTimeMillis();
            report.report("Swap Bank and Resetting: " + enodeB.getName(), Reporter.PASS);
            boolean swapBankAndReset = false;
            if (connectionMethod == SWUpgradeConnectionMethod.Netspan){
            	enodeB.setExpectBooting(true);
            	report.report("Rebooting " + enodeB.getNetspanName() + " via Netspan");
            	swapBankAndReset = softwareConfigSet(enodeB.getNetspanName(), RequestType.ACTIVATE, imageName);
            	if (swapBankAndReset) {					
	            	swapBankAndReset &= enodeB.waitForReboot(5 * 60 * 1000);
	        		enodeB.setExpectBooting(false);
            	}
            	else
            		swapBankAndReset = swapBanksAndResetEnodeb(enodeB);
            }
            else
            	swapBankAndReset = swapBanksAndResetEnodeb(enodeB);
            if (!swapBankAndReset) {
            	report.report("EnodeB: " + enodeB.getName() + " wasn't rebooted", Reporter.WARNING);
                time = System.currentTimeMillis() - time;
                return;
            }
            pass = enodeB.waitForAllRunning(EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
            if (!isPass()) {
            	report.report("EnodeB: " + enodeB.getName() + " didn't reach All runing state during " + (EnodeB.WAIT_FOR_ALL_RUNNING_TIME) / 60000 + "Minutes", Reporter.FAIL);
                time = System.currentTimeMillis() - time;
                report.report("Swaping Banks and rebooting unit "+enodeB.getName());
                swapBanksAndResetEnodeb(enodeB);
                notToValidateNodes.add(enodeB);
                boolean allRun = enodeB.waitForAllRunning(EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
                if(allRun){
                	report.report("All Running State for node : "+enodeB.getName());
                }else{
                	report.report("Failed to get to All Running after swaping back banks",Reporter.WARNING);
                }
                return;
            }

            report.report("Reset has been done, and validated", Reporter.PASS);
            time = System.currentTimeMillis() - time;
            GeneralUtils.unSafeSleep(30 * 1000);

        }

        public boolean isPass() {
            return pass;
        }

        public long getTime() {
            return time;
        }

        public boolean swapBanksAndResetEnodeb(EnodeB enodeB) {
            return enodeB.swapBanksAndReboot();
        }

        public EnodeB getEnodeB() {
            return enodeB;
        }

    }

    /**
     * Copy Thread
     */
    public class VersionCopyWorker {
        private EnodeB.Architecture type;
        private boolean pass;

        ServerProtocolType downloadType;

        public boolean isPass() {
            return pass;
        }

        public VersionCopyWorker(EnodeB.Architecture type, ServerProtocolType downloadType) {
            this.type = type;
            pass = true;
            this.downloadType = downloadType;
        }
        
        public boolean startCopy() {
            try {
                if (copyVerToTftpServer(type, 3)) {
                    pass = true;
                } else {
                	report.report("Copy failed", Reporter.WARNING);
                    pass = false;
                }
            } catch (Exception e) {
            	report.report("Error Fail: Fail while trying to copy the build to the upgrade server\nMessage: "
                                + e.getMessage(), Reporter.WARNING);
                pass = false;
            }
			return pass;  
        }

        public boolean copyVerToTftpServer(EnodeB.Architecture component, int tries) {
            /**
             * if the node is equals to fsm, then add "fsm." at the beginning
             * and ".tar.gz" at the end of the build else to add "enodeb." and
             * ".enc" et the end
             */
        	
        	if(tries <=0) 
        		return false;
        	
            File destFile;
            File sourceFile;
            if (component == EnodeB.Architecture.FSM) {
                destFile = new File(destServer.getPath().toString() + File.separator + FSMBuild);
                sourceFile = new File(sourceServer.getPath().toString() + File.separator + FSMBuild);
            }else{
                destFile = new File(destServer.getPath().toString() + File.separator + XLPBuild);
                sourceFile = new File(sourceServer.getPath().toString() + File.separator + XLPBuild);
            }
            /**
             * copying the file from the server
             **/
            try {
                if (!Files.exists(new File(destFile.getPath()).toPath())) {
                	report.report("Copying file " + type, Reporter.PASS);
                    Files.copy(sourceFile.toPath(), destFile.toPath(), REPLACE_EXISTING);
                } else {
                	report.report(type + " file already exists", Reporter.PASS);
                    return true;
                }
            } catch (Exception e) {
                GeneralUtils.printToConsole("An error has been occur while trying to copy the file to the upgrade server\nError Message: "+ e.getMessage());
                if (tries>0) {
                    GeneralUtils.unSafeSleep(5*1000);
                    if (!Files.exists(new File(destFile.getPath()).toPath())) {
                        return copyVerToTftpServer(component, tries - 1);
                    }
                    report.report(type + " file has been copied after one or more retries", Reporter.PASS);
                    return true;
                }
                report.report("An error has been occur while trying to copy the file to the upgrade server\nError Message: "+ e.getMessage(), Reporter.FAIL);
                return false;
            }
            if (destServer.exists()) {
            	report.report(type + " file has been copied", Reporter.PASS);
                return true;
            }
            return false;
        }
    }

    /**
     * Update Thread
     */
    public class EnodebUpgradeWorker implements Runnable {
        private EnodeB enodeB;
        private boolean pass;
        private long time;
        private SWUpgradeConnectionMethod connectionMethod = null;
        private ServerProtocolType downloadType;
        private String user;
        private String password;
        private String imageName = "";

        public EnodebUpgradeWorker(String build, EnodeB enodeB, ServerProtocolType downloadType,String user,String password) {
            this.enodeB = enodeB;
            GeneralUtils.printToConsole("Create thread for enodeb: " + enodeB.getNetspanName() + " Method: " + downloadType + " User:"+ user + " Password:" + password);
            pass = true;
            enodeB.getArchitecture();
            this.downloadType = downloadType;
            this.user = user;
            this.password = password;
            connectionMethod = SWUpgradeConnectionMethod.SNMP;
        }

        @Override
        public void run() {
            time = System.currentTimeMillis();
            report.report("Starting to update EnodeB " + enodeB.getName() + " with " + connectionMethod);
            if (connectionMethod == SWUpgradeConnectionMethod.Netspan) 
            	pass = downloadSoftwareViaNetspan();
            else
            	pass = downloadSoftwareViaSnmp();
            if(!isPass()) {
            	connectionMethod = SWUpgradeConnectionMethod.SNMP;       
            	report.report(enodeB.getName() +" - Download failed, retry SNMP", Reporter.WARNING);
            	pass = downloadSoftwareViaSnmp();
            	if (!isPass()){
            		report.report(enodeB.getName() +" - SNMP download failed", Reporter.WARNING);
            		if(TestConfig.getInstace().getSWUCliFallback()){
            			report.report("'SWUCliFallback' flag is true in SUT. Fallback download via CLI");
            			downloadSoftwareViaCli();
            			connectionMethod = SWUpgradeConnectionMethod.CLI;            			
            		}else{
            			report.report("'SWUCliFallback' flag is false in SUT or doesn't exist. Skip download via CLI");
            		}
            	}
            }
            if (!isStandbyUpdatedViaSnmp(enodeB)) {
            	report.report(enodeB.getName() +" - The Secondary bank doesn't contain the target version ", Reporter.WARNING);
                pass = false;                
            }
            else
            	pass = true;
            time = System.currentTimeMillis() - time;
            GeneralUtils.printToConsole("Time for Upgrade:" + time);

        }

        private boolean downloadSoftwareViaCli() {
        	String build;
            if (enodeB.getArchitecture() == EnodeB.Architecture.FSM) {
                build = FSMBuild;
            } else {
                build = XLPBuild;
            }
            if(downloadType == ServerProtocolType.SFTP) build = "upload/" + build;
            enodeB.downloadSWCli(build, downloadType, user, password);
			String doneMessage = "post download stage finished succesfully";
			report.report(enodeB.getName() +" - Waiting for software download to finish and report \"" + doneMessage + "\"");
			boolean doneMessageFound = Log.getInstance().waitForLogLine(enodeB, EnodeBComponentTypes.XLP, doneMessage, EnodeB.UPGRADE_TIMEOUT / 3);
			if (doneMessageFound) {
				return true;
			}		
    		report.report(enodeB.getName() +" - Software download failed due to timeout", Reporter.WARNING);
            return false;
		}

        public boolean downloadSoftwareViaNetspan() {
            boolean ispassed = softwareConfigSet(enodeB.getNetspanName(), RequestType.DOWNLOAD, imageName);
            ispassed &= downloadFile(enodeB, false);
            return ispassed;
        }
        
		public boolean downloadSoftwareViaSnmp() {
            String build;
            if (enodeB.getArchitecture() == EnodeB.Architecture.FSM) {
                build = FSMBuild;
            } else {
                build = XLPBuild;
            }
            if(downloadType == ServerProtocolType.SFTP) build = "upload/" + build;
            boolean downloadPass = enodeB.downloadSWSnmp(build, downloadType, user, password);
            GeneralUtils.unSafeSleep(5 * 1000);
            if (downloadPass) {
            	boolean downloadState = enodeB.getDownloadStates();
                if (downloadState) {
                    return true;
                }
            }
            return false;
        }

        public boolean isPass() {
            return pass;
        }

        public long getTime() {
            return time;
        }

        public void setTime(long time) {
            this.time = time;
        }
        
        public String getImageName() {
            return imageName;
        }

        public void setImageName(String imageName) {
            this.imageName = imageName;
        }

        public EnodeB getEnodeB() {
            return enodeB;
        }

		public SWUpgradeConnectionMethod getConnectionMethod() {
			return connectionMethod;
		}

		public void setConnectionMethod(SWUpgradeConnectionMethod connectionMethod) {
			this.connectionMethod = connectionMethod;
		}
    }

    public static String milisToFormat(long milis) {
        return String.format("%d min, %d sec",
                TimeUnit.MILLISECONDS.toMinutes(milis),
                TimeUnit.MILLISECONDS.toSeconds(milis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milis))
        );
    }
	
	
	/**
	 * created by Moran Goldenberg
	 * @param server
	 * @return true if the software server was found in Netspan with all parameters and false if it didn't find it.
	 */
	public boolean isSoftwareServerExists(EnodeBUpgradeServer server){
		EnodeBUpgradeServer localServer;
		try {
			localServer = netspanServer.getSoftwareServer(server.getUpgradeServerName());
			if (localServer!= null){
				if (localServer.getUpgradeServerIp().equals(server.getUpgradeServerIp())){
					if (localServer.getUpgradeServerProtocolType() == server.getUpgradeServerProtocolType()){
						report.report("The upgrade server was found.");
						report.report("Server Name: " + server.getUpgradeServerName());
						report.report("Server IP: " + server.getUpgradeServerIp() );
						report.report("Server Protocol: " + server.getUpgradeServerProtocolType().toString());
						return true;
					}
					else{
						report.report("Protocol type soesn't match to "+ server.getUpgradeServerProtocolType().toString());
						return false;
					}
				}
				else {
					report.report("The IP adress doesn't match to " + server.getUpgradeServerIp());
					return false;
				}
			}
			else {
				report.report("There is no upgrade server by the name " + server.getUpgradeServerName());
				return false;
			}
		} catch (Exception e) {
			report.report("Couldn't get upgradeServer object", Reporter.WARNING);
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * created by Moran Goldenberg
	 * @param image
	 * @return true if the software Image was found in Netspan with all parameters and false if it didn't find it.
	 */
	public boolean isSoftwareImageExists(EnodeBUpgradeImage image){
		EnodeBUpgradeImage localimage;
		try {
			localimage = netspanServer.getSoftwareImage(image.getName());
			if (localimage!= null){
				if (localimage.getHardwareCategory() == image.getHardwareCategory()){
					if (localimage.getUpgradeServerName().equals(image.getUpgradeServerName())){
						if(localimage.getBuildPath().equals(image.getBuildPath())){
							if(localimage.getVersion().equals(image.getVersion())){
								report.report("The upgrade Image was found.");
								report.report("Image Name: " + image.getName());
								report.report("Image Hardware Category: " + image.getHardwareCategory().toString());
								report.report("Software server name: " + image.getUpgradeServerName());
								report.report("Build path: " + image.getBuildPath());
								report.report("build version: " + image.getVersion());
								return true;
							}
							else{
								//report.report("varsion doesn't match to "+ image.getVersion());
								report.report("version is " + localimage.getVersion() + " and not " + image.getVersion() + " as expected");
								return false;
							}
						}
						else{
							report.report("Build path doesn't match to "+ image.getBuildPath());
							report.report("Build path is " + localimage.getBuildPath() + " and not " + image.getBuildPath() + " as expected");
							return false;
						}
					}
					else{
						report.report("Hardware category doesn't match to "+ image.getHardwareCategory().toString());
						report.report("Hardware category is " + localimage.getHardwareCategory() + " and not " + image.getHardwareCategory() + " as expected");
						return false;
					}
				}
				else {
					report.report("Image name doesn't match to " + image.getName());
					report.report("Image name is " + localimage.getName() + " and not " + image.getName() + " as expected");
					return false;
				}	
			}
			else {
				report.report("There is no upgrade Image by the name " + image.getName());
				return false;
			}
		} catch (Exception e) {
			report.report("Couldn't get upgradeImage object", Reporter.WARNING);
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * created by Moran Goldenberg
	 * @param image
	 * @return true if the software Image was found in Netspan by its name
	 */
	public boolean isSoftwareImageExists(String imageName){
		EnodeBUpgradeImage localimage;
		try {
			localimage = netspanServer.getSoftwareImage(imageName);
			if (localimage!= null){
				report.report("There is upgrade Image by the name " + imageName);
				return true;
			}
			else {
				report.report("There is no upgrade Image by the name " + imageName);
				return false;
			}
		} catch (Exception e) {
			report.report("Couldn't get upgradeImage object", Reporter.WARNING);
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * created by Moran Goldenberg
	 * @param nodeName
	 * @param imageType 
	 * @return software status from netspan
	 */
	public SoftwareStatus getSoftwareStatus(String nodeName, ImageType imageType){
		SoftwareStatus status = new SoftwareStatus();
		try {
			status = netspanServer.getSoftwareStatus(nodeName, imageType);
			if (status == null ){
				report.report("Couldn't get software server status from netspan");
			}
		} catch (Exception e) {
			report.report("Couldn't get software server status from netspan");
			e.printStackTrace();
		}
		return status;
	}
	
	public boolean softwareConfigSet(String nodeName, RequestType requestType, String softwareImage){
		boolean result = false;
		try {
			result = netspanServer.softwareConfigSet(nodeName, requestType, softwareImage);
		} catch (Exception e) {
			report.report("Couldn't config software downlad by netspan");
			e.printStackTrace();
		}
		return result;
	}
	
	public boolean createSoftwareImage(EnodeBUpgradeImage upgradeImage){
		boolean result;
		try {
			result = netspanServer.createSoftwareImage(upgradeImage);
			if (!result){
				return false;
			}
			else{
				report.report("Software Image was created");
				report.report("Image name: " + upgradeImage.getName());
				report.report("Hardware Category: "+ upgradeImage.getHardwareCategory());
				report.report("Software server: "+ upgradeImage.getUpgradeServerName());
				report.report("Build path: " + upgradeImage.getBuildPath());
				report.report("Software version: "+ upgradeImage.getVersion());
			}
			
		} catch (Exception e) {
			report.report("Couldn't create software Image by netspan");
			e.printStackTrace();
		}
		return true;
	}
	
	public boolean updateSoftwareImage(EnodeBUpgradeImage upgradeImage){
		boolean result;
		try {
			result = netspanServer.updateSoftwareImage(upgradeImage);
			if (!result){
				return false;
			}
			else{
				report.report("Software Image was updated");
				report.report("Image name: " + upgradeImage.getName());
				report.report("Hardware Category: "+ upgradeImage.getHardwareCategory());
				report.report("Software server: "+ upgradeImage.getUpgradeServerName());
				report.report("Build Path: " + upgradeImage.getBuildPath());
				report.report("Software version: "+ upgradeImage.getVersion());
			}
			
		} catch (Exception e) {
			report.report("Couldn't update software Image by netspan");
			e.printStackTrace();
		}
		return true;
	}
	
	public boolean createSoftwareServer(EnodeBUpgradeServer upgradeServer){
		boolean result;
		try {
			result = netspanServer.createSoftwareServer(upgradeServer);
			if (!result){
				return false;
			}
			else{
				report.report("Software Server was created");
				report.report("Server name: " + upgradeServer.getUpgradeServerName());
				report.report("Server IP: "+ upgradeServer.getUpgradeServerIp());
				report.report("Protocol Type: "+ upgradeServer.getUpgradeServerProtocolType());
			}
			
		} catch (Exception e) {
			report.report("Couldn't create software server by netspan");
			e.printStackTrace();
		}
		return true;
	}
	
	public boolean createUpgradeServer(EnodeBUpgradeServer server){
		boolean flag = true;
		GeneralUtils.startLevel("Creating Software server");
			boolean isSWExist = isSoftwareServerExists(server);
			if (!isSWExist){
				boolean wasCreated = createSoftwareServer(server);
				if (!wasCreated){
					report.report("The Software Server was not created as expected", Reporter.WARNING);
					flag = false;
				}
			}
		GeneralUtils.stopLevel();
		return flag;
	}
	
	public boolean deleteSoftwareImage(String softwareImageName){
		boolean result;
		try {
			result = netspanServer.deleteSoftwareImage(softwareImageName);
			return result;
		} catch (Exception e) {
			report.report("Couldn't delete software Image by netspan");
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean downloadFile(EnodeB node, boolean isUpgrade){
		boolean tempPass = false;
		AlarmsAndEvents event = AlarmsAndEvents.getInstance();
		EventInfo tempEvent = new EventInfo();
		Date current = new Date();
		Date timeOut = org.apache.commons.lang3.time.DateUtils.addMinutes(current, 20);
		while(current.before(timeOut)){
			tempEvent = event.waitForEventShowUp(node, "Software Download", 10);
			if (tempEvent != null){
			
				if(tempEvent.getEventInfo().toLowerCase().contains("download completed") || tempEvent.getEventInfo().toLowerCase().contains("download and activate completed")){
					report.report(tempEvent.getEventInfo());
					tempPass = true;
					break;
				}
				if (tempEvent.getEventInfo().contains("Download failed")){
					report.report("software download failed", Reporter.WARNING);
					report.report(tempEvent.getEventInfo());
					return false;
				}
				if (tempEvent.getEventInfo().contains("Download in progress"))
					report.report(tempEvent.getEventInfo());
			}
				GeneralUtils.unSafeSleep(30000);
				current = new Date();
		}
		if(!tempPass)
		{
			report.report("There was no event to indicate on software download", Reporter.WARNING);
			return false;
		}
		
		if (isUpgrade && !tempEvent.getEventInfo().contains("download and activate completed")){
			current = new Date();
			while(current.before(timeOut)){
				tempEvent = event.waitForEventShowUp(node, "Software Activate", 10);
				if (tempEvent != null){
				
					if(tempEvent.getEventInfo().toLowerCase().contains("activate completed")){
						report.report(tempEvent.getEventInfo());
						tempPass = true;
						break;
					}
					if (tempEvent.getEventInfo().toLowerCase().contains("activate failed")){
						report.report("Activation failed", Reporter.WARNING);
						report.report(tempEvent.getEventInfo());
						return false;
					}
					if (tempEvent.getEventInfo().toLowerCase().contains("activate in progress"))
						report.report(tempEvent.getEventInfo());
				}
					GeneralUtils.unSafeSleep(30000);
					current = new Date();
			}
		}
		return tempPass;
	}
	
	public boolean createUpgradeImage(EnodeBUpgradeServer server, String buildName, String imageName, String targetVer, EnodeB enodeb){
		if (server.getUpgradeServerProtocolType() == ServerProtocolType.SFTP){
			buildName = "/upload/"+buildName;
		}
		EnodeBUpgradeImage image = new EnodeBUpgradeImage(imageName , EnodeBConfig.getInstance().getHardwareCategory(enodeb), server.getUpgradeServerName(), server.getUpgradeServerProtocolType(), buildName, targetVer);
		boolean flag = true;

		GeneralUtils.startLevel("Creating Software Image");
			boolean isSWExist = isSoftwareImageExists(image.getName());
			if (!isSWExist){
				 boolean wasCreated = createSoftwareImage(image);
				 if(!wasCreated){
					 report.report("The Software Image was not created as expected", Reporter.WARNING);
					flag = false;
				}
			}
			else {
				isSWExist = isSoftwareImageExists(image);
				if(!isSWExist){
					boolean wasUpdated = updateSoftwareImage(image);
					if(!wasUpdated){
						 report.report("The Software Image was not updated as expected", Reporter.WARNING);
						flag = false;
					}
				}
			}
		GeneralUtils.stopLevel();
		return flag;
	}
	
	public ArrayList<EnodeB> noValidationNodes(){
		return this.notToValidateNodes;
	}
	
	public int updatDefaultSoftwareImage(EnodeB eNodeB) {
		int numberOfExpectedReboots = 0;
		String softwareImage = eNodeB.defaultNetspanProfiles.getSoftwareImage();
		if(softwareImage != null){
			EnodeBUpgradeImage enodeBUpgradeImage = netspanServer.getSoftwareImage(softwareImage);
			EnodeBUpgradeServer enodeBUpgradeServer = netspanServer.getSoftwareServer(enodeBUpgradeImage.getUpgradeServerName());
			switch (enodeBUpgradeServer.getUpgradeServerProtocolType()) {
			case SFTP:
				setDestServer(new File(File.separator + File.separator + enodeBUpgradeServer.getUpgradeServerIp() + File.separator + "sftp"+ File.separator +"upload"));
				break;
			default:
				setDestServer(new File(File.separator + File.separator + enodeBUpgradeServer.getUpgradeServerIp() + File.separator + "tftp"));
				break;
			}
		
			String buildsPath = System.getProperty("BuildMachineVerPath");
			String relayBuildPath = System.getProperty("BuildMachineRelayVerPath");
			
			if (buildsPath != null && buildsPath.startsWith("\\") && !buildsPath.startsWith("\\\\")){
				buildsPath = "\\" + buildsPath;
			}
			if (relayBuildPath != null && relayBuildPath.startsWith("\\") && !relayBuildPath.startsWith("\\\\")){
				relayBuildPath = "\\" + relayBuildPath;
			}
	        
			setSourceServer(buildsPath);
	        setBuildFiles();
	        VersionCopyWorker versionCopyWorker = new VersionCopyWorker(eNodeB.getArchitecture(), enodeBUpgradeServer.getUpgradeServerProtocolType());
	        versionCopyWorker.startCopy();
	        
	        String fsmBuildFileName = getFSMBuild();
	        String xlpBuildFileName = getXLPBuild();
	        String relayBuildFileName = getRelayBuildFileName(relayBuildPath);
	        
	        String fsmBuild = "";
	        if(fsmBuildFileName != null){
	        	fsmBuild = fsmBuildFileName.substring(fsmBuildFileName.indexOf(".") + 1);
	        	fsmBuild = fsmBuild.substring(0, fsmBuild.indexOf("."));
	        }
	        String xlpBuild = "";
	        if(xlpBuildFileName != null){
	        	xlpBuild = xlpBuildFileName.substring(xlpBuildFileName.indexOf(".") + 1);
	        	xlpBuild = xlpBuild.substring(0, xlpBuild.indexOf("."));
	        }
	        String relayBuild = "";
	        if(relayBuildFileName != null && relayBuildFileName != ""){
	        	relayBuild = relayBuildFileName.substring(relayBuildFileName.indexOf("-") + 1);
	        	relayBuild = relayBuild.substring(0, relayBuild.indexOf(".pak")); 
	        }
	        
	        String buildFileName = ""; 
	        String build = "";
	        if(eNodeB.getArchitecture() == Architecture.FSM){
	        	buildFileName = fsmBuildFileName;
	        	build = fsmBuild;
	        }else if(eNodeB.getArchitecture() == Architecture.XLP){
	        	buildFileName = xlpBuildFileName;
	        	build = xlpBuild;
	        }
	        build = build.replaceAll("_", ".");
	        if(buildFileName != ""){
	        	SoftwareStatus softwareStatus = netspanServer.getSoftwareStatus(eNodeB.getNetspanName(), eNodeB.getImageType());
	        	if(softwareStatus != null){
	        		report.report(eNodeB.getName() + "'s Running Version: " + softwareStatus.RunningVersion);
	        		if(!softwareStatus.RunningVersion.equals(build)){
	        			numberOfExpectedReboots++;
	        		}else{
	        			report.report("Running Version equals Target Version - No Need To Upgrade eNodeB Version.");
	        		}
	        	}
	        	
	        	EnodeBUpgradeImage upgradeImage = new EnodeBUpgradeImage();
				upgradeImage.setName(softwareImage);
				upgradeImage.setImageType(eNodeB.getImageType().value());
				upgradeImage.setBuildPath(buildFileName);
				upgradeImage.setVersion(build);
				if(netspanServer.updateSoftwareImage(upgradeImage) == false){
					report.report("FAILED To Update Software Image.", Reporter.WARNING);
					numberOfExpectedReboots = -1;
				}
	        }
	        if(relayBuild != ""){
	        	SoftwareStatus softwareStatus = netspanServer.getSoftwareStatus(eNodeB.getNetspanName(), ImageType.RELAY);
	        	if(softwareStatus != null){
	        		report.report(eNodeB.getName() + "'s Relay Running Version: " + softwareStatus.RunningVersion);
	        		if(!softwareStatus.RunningVersion.equals(relayBuild)){
	        			numberOfExpectedReboots++;
	        		}else{
	        			report.report("Running Version equals Target Version - No Need To Upgrade Relay Version.");
	        		}
	        	}
	        	
	        	EnodeBUpgradeImage upgradeImage = new EnodeBUpgradeImage();
				upgradeImage.setName(softwareImage);
				upgradeImage.setImageType(ImageType.RELAY.value());
				upgradeImage.setBuildPath(relayBuildFileName);
				upgradeImage.setVersion(relayBuild);
				if(softwareStatus != null){
					if(netspanServer.updateSoftwareImage(upgradeImage) == false){
						report.report("FAILED To Update Software Image with Relay Version.", Reporter.WARNING);
						numberOfExpectedReboots = -1;
					}
				}
	        }
		}else{
			report.report("SoftwareImage doesn't exist.", Reporter.FAIL);
		}
		report.report("Number Of Expected Reboots: " + numberOfExpectedReboots);
		return numberOfExpectedReboots;
	}

	private String getRelayBuildFileName(String relayBuildPath) {
		String fileName = "";
		File relaySourceServer = new File(relayBuildPath);
		File[] files = relaySourceServer.listFiles();
		if(files != null){
	        for (File file : files) {
	            GeneralUtils.printToConsole("Checking File:" + file.toString());
	            if (file.getName().contains(".pak") && (!file.getName().contains("mirror"))){
	            	fileName = file.getName();
	            	report.report("Relay Build Name Is: " + fileName);
	            	File destFile = new File(destServer.getPath().toString() + File.separator + fileName);;
	                File sourceFile = file;
		            /**
		             * copying the file from the server
		             **/
		            try {
		                if (!Files.exists(new File(destFile.getPath()).toPath())) {
		                	report.report("Copying file: Relay Build", Reporter.PASS);
		                    Files.copy(sourceFile.toPath(), destFile.toPath(), REPLACE_EXISTING);
		                } else {
		                	report.report("Relay file already exists", Reporter.PASS);
		                }
		            } catch (Exception e) {
		            	
		            }
	            	break;
	            }
			}
		}
		return fileName;
	}
}
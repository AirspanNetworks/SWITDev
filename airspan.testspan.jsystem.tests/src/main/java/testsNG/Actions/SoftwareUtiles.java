package testsNG.Actions;

import EnodeB.AirUnity;
import EnodeB.EnodeB;
import EnodeB.EnodeBUpgradeImage;
import EnodeB.EnodeBUpgradeServer;
import EnodeB.Components.EnodeBComponentTypes;
import EnodeB.Components.XLP.XLP.SwStatus;
import EnodeB.EnodeB.Architecture;
import Netspan.NetspanServer;
import Netspan.API.Enums.HardwareCategory;
import Netspan.API.Enums.ImageType;
import Netspan.API.Enums.ServerProtocolType;
import Netspan.API.Lte.EventInfo;

import Netspan.API.Software.RequestType;
import Netspan.API.Software.SoftwareStatus;
import TestingServices.TestConfig;
import Utils.CommonConstants;
import Utils.GeneralUtils;
import Utils.Pair;
import jsystem.framework.report.ListenerstManager;
import jsystem.framework.report.Reporter;
import org.apache.commons.lang3.StringUtils;
import testsNG.Actions.Utils.EnodebSwStatus;
import testsNG.Actions.Utils.NetspanSWEvents;
import testsNG.Actions.Utils.StringTools;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.file.StandardCopyOption.*;

/**
 * Created by owiesel on 10-May-16. Software Upgrade Test Helper
 */

public class SoftwareUtiles {
	public enum SWUpgradeConnectionMethod {
		CLI, SNMP, Netspan
	}

	private long softwareActivateStartTimeInMili;
	private Date softwareActivateStartTimeInDate;

	/**
	 * set Software Activate Start Time when SW upgrade starts
	 *
	 * @param softwareActivateStartTimeInMili - softwareActivateStartTimeInMili
	 */
	public void setSoftwareActivateStartTimeInMili(long softwareActivateStartTimeInMili) {
		this.softwareActivateStartTimeInMili = softwareActivateStartTimeInMili;
		this.softwareActivateStartTimeInDate = new Date(softwareActivateStartTimeInMili);
	}

	long timeout = 5 * 60 * 1000;

	/**
	 * the version that needs to be copied
	 **/
	private String FSMBuild = StringUtils.EMPTY;
	private String FSMv4Build = StringUtils.EMPTY;
	private String XLPBuild = StringUtils.EMPTY;

	/**
	 * the default source of the copy
	 **/
	private File sourceServer = new File(
			StringTools.getStringWithUnifiedFileSeperator("\\\\FS4\\Projects\\Development\\Internal\\Builds\\"));

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
		boolean flagFSMv4 = true;
		try {
			File[] files = sourceServer.listFiles();
			for (File file : files) {
				GeneralUtils.printToConsole("Checking File:" + file.toString());
				if (file.getName().contains("fsm")) {
					if (file.getName().contains("fsmv4")) {
						if (file.getName().contains("enc") && flagFSMv4) {
							FSMv4Build = file.getName();
							flagFSMv4 = false;
						} else if (file.getName().contains("tar.gz") && flagFSMv4) {
							FSMv4Build = file.getName();
						}
					} else {
						if (file.getName().contains("swu")) {
							FSMBuild = file.getName();
							flagFSM = false;
						} else if (file.getName().contains("enc") && flagFSM) {
							FSMBuild = file.getName();
							flagFSM = false;
						} else if (file.getName().contains("tar.gz") && flagFSM) {
							FSMBuild = file.getName();
						}
					}

				} else if (file.getName().contains("enodeb")) {
					if (file.getName().contains("swu")) {
						XLPBuild = file.getName();
						flagXLP = false;
					} else if (file.getName().contains("enodeb") && file.getName().contains("enc") && flagXLP) {
						XLPBuild = file.getName();
					} else if (file.getName().contains("enodeb") && file.getName().contains("tar.bz2")
							&& XLPBuild.trim() == "") {

						XLPBuild = file.getName();

					}
				}
			}
			report.report("FSM Build Name is:" + FSMBuild);
			report.report("FSMv4 Build Name is:" + FSMv4Build);
			report.report("XLP Build Name is:" + XLPBuild);
		} catch (Exception e) {
			report.report("The Path provided does not contain the relevant files", Reporter.WARNING);
			reason = Pair.createPair("The Path provided does not contain the relevant files", Reporter.WARNING);
		}
		return !(FSMBuild.equals("") && FSMv4Build.equals("") && XLPBuild.equals(""));
	}

	/**
	 * created by Moran Goldenberg
	 *
	 * @param architercture
	 * @param fileType
	 * @return true if finds fsmbuild file or XLPbuild file according to the
	 * enodeB architecture
	 */
	public boolean setBuildFiles(EnodeB.Architecture architercture, String fileType) {
		File[] files = sourceServer.listFiles();
		if (files == null)
			return false;
		for (File file : files) {
			GeneralUtils.printToConsole("Checking File:" + file.toString());
			switch (architercture) {
				case XLP:
					if (file.getName().contains("enodeb") && file.getName().contains(fileType)) {
						XLPBuild = file.getName();
						report.report("XLP Build Name is:" + XLPBuild);
						return true;
					}
					break;
				case FSM:
					if (file.getName().contains("fsm") && file.getName().contains(fileType)) {
						FSMBuild = file.getName();
						report.report("FSM Build Name is:" + FSMBuild);
						return true;
					}
					break;
				case FSMv4:
					if (file.getName().contains("fsmv4") && file.getName().contains(fileType)) {
						FSMv4Build = file.getName();
						report.report("FSM Build Name is:" + FSMv4Build);
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
			report.report(enodeB.getName() + " - The Standby bank contains version " + build);
			reason = Pair.createPair("The Standby bank contains version " + build, Reporter.PASS);
			return true;
		} else {
			GeneralUtils
					.printToConsole(enodeB.getNetspanName() + ": The Standby bank does not contains version " + build);
			report.report(enodeB.getName() + " - The Standby bank does not contains version " + build);
			reason = Pair.createPair("The Standby bank does not contains version " + build, Reporter.FAIL);
			return false;
		}
	}

	/**
	 * This method gets an EnB and checks if the version was updated. Via SNMP,
	 * and if fails, it will get the version via NetSpan. If the version was not
	 * updated as requested, it checks the version on the standby bank, and swap
	 * banks if required.
	 *
	 * @param enodeB   - EnodeB
	 * @param logLevel - logLevel
	 * @return - true as success
	 */
	public boolean isVersionUpdated(EnodeB enodeB, int logLevel) {
		String build = getCurrentBuild(enodeB).trim();
		// Get get version via SNMP or Netspan
		String running = getVersion(enodeB, CommonConstants.RUNNING_STRING);
		// If the response is valid checking if updated
		if (isBuildContainsTheUpdatedRunningVersion(enodeB, build, running)) {
			return true;
		}
		String standby = getVersion(enodeB, CommonConstants.STANDBY_STRING);
		// If the running version wasn't updated. go to the standby bank, checks
		// if it was updated there.
		if (build.contains(standby) || standby.contains(build)) {
			report.report("The Standby Bank contains target version: " + build + " on Enodeb: " + enodeB.getNetspanName()
					+ ", about to swap banks.");
			// Takes the requested version from the standby bank if it necessary
			return swapBanks(enodeB, logLevel, build);
		} else {
			report.report("Nor the running bank nor the standby bank contains version " + build + " on Enodeb: "
					+ enodeB.getNetspanName(), logLevel);
			return false;
		}
	}

	/**
	 * This method get the Version via SNMP and if failed, via Netspan.
	 *
	 * @param versionViaSNMP    - version Via SNMP
	 * @param versionViaNetspan - version Via Netspan
	 * @return - String - Version, or ERROR_VALUE if fails
	 */
	private String getVersion(String versionViaSNMP, String versionViaNetspan) {
		// Get details via SNMP
		String version = versionViaSNMP.trim();
		// Checking response validation
		if (!isValidVersionResponse(version)) {
			// If can't get via SNMP - Get details via Netspan
			report.report("Didn't get the correct version via SNMP, retry via Netspan.");
			version = versionViaNetspan;
		}
		return version;
	}

	/**
	 * This method get the Version via SNMP and if failed, via Netspan.
	 *
	 * @param enodeB          - enodeB
	 * @param bankVersionType - bank Version Type ("Running" or "Standby")
	 * @return - String - Version, or ERROR_VALUE if fails
	 */
	private String getVersion(EnodeB enodeB, String bankVersionType) {
		// Get details via SNMP
		String version;
		switch (bankVersionType) {
			case CommonConstants.RUNNING_STRING:
				version = enodeB.getRunningVersion().trim();
				break;
			case CommonConstants.STANDBY_STRING:
				version = enodeB.getSecondaryVersion().trim();
				break;
			default:
				version = String.valueOf(GeneralUtils.ERROR_VALUE);
		}
		// Checking response validation
		if (!isValidVersionResponse(version)) {
			// If can't get via SNMP - Get details via Netspan
			report.report("Didn't get the correct version via SNMP, retry via Netspan.");
			switch (bankVersionType) {
				case CommonConstants.RUNNING_STRING:
					version = netspanServer.getRunningVersionOfEnb(enodeB);
					break;
				case CommonConstants.STANDBY_STRING:
					version = netspanServer.getStandByVersionOfEnb(enodeB);
					break;
			}
		}
		return version;
	}

	/**
	 * swap Banks
	 *
	 * @param enodeB   - enodeB
	 * @param logLevel -logLevel
	 * @param build    - build
	 * @return false if the action failed
	 */
	private boolean swapBanks(EnodeB enodeB, int logLevel, String build) {
		report.report("Starting Fallback procedure via SNMP");
		report.report("Switch Bank");
		if (enodeB.swapBanksAndReboot()) {
			/*
			 * Heng - don't worry about the wait this is a safe switch to wait
			 * for reboot the wait for all running itself will take 5 minutes so
			 * this wait will not affect runtime but will help up to make the
			 * code simpler and not wait for reboot event at this point
			 */
			GeneralUtils.unSafeSleep(7 * 60 * 1000);
			enodeB.setUnexpectedReboot(0);
			if (enodeB.waitForAllRunningAndInService(EnodeB.WAIT_FOR_ALL_RUNNING_TIME)) {
				// After swapping, again - Get details via SNMP or Netspan
				String runningVersion = getVersion(enodeB, CommonConstants.RUNNING_STRING);
				if (isBuildContainsTheUpdatedRunningVersion(enodeB, build, runningVersion)) {
					return true;
				} else {
					report.report("The running bank does not contains version " + build + " on Enodeb: "
							+ enodeB.getNetspanName() + " Current Build:" + runningVersion, logLevel);
					return false;
				}
			} else {
				report.report("Failed to reach all running on Enodeb: " + enodeB.getNetspanName(), logLevel);
				return false;

			}
		} else {
			report.report("Swap Bank and Reset Failed on Enodeb: " + enodeB.getNetspanName(), logLevel);
			return false;
		}
	}

	/**
	 * Checks if the actual version on the build is like the expected running
	 * version
	 *
	 * @param enodeB  - enodeB
	 * @param build   - build version
	 * @param running - running version
	 * @return true if they are equal
	 */
	private boolean isBuildContainsTheUpdatedRunningVersion(EnodeB enodeB, String build, String running) {
		if ((build.contains(running) || running.contains(build))) {
			report.report("The running bank contains version " + build + " on Enodeb: " + enodeB.getNetspanName());
			return true;
		}
		return false;
	}

	/**
	 * Checks if response of version is a valid response.
	 *
	 * @param versionFromResponse - running Version From Response
	 * @return true if the response != null,ErrorValue
	 */
	private boolean isValidVersionResponse(String versionFromResponse) {
		return (versionFromResponse != null) && (!versionFromResponse.equals(String.valueOf(GeneralUtils.ERROR_VALUE)));
	}

	public boolean isVersionExists(EnodeB enodeB) {
		String build = getCurrentBuild(enodeB).trim();
		String running = enodeB.getRunningVersion().trim();
		String standby = enodeB.getSecondaryVersion().trim();
		report.report(enodeB.getName() + " running version is: " + running);
		report.report(enodeB.getName() + " standby version is: " + standby);
		return running.contains(build) || standby.contains(build);

	}

	public String getCurrentBuild(EnodeB enodeB) {
		String build = "";
		try {
			if (enodeB.getArchitecture() == EnodeB.Architecture.FSM) {
				GeneralUtils.printToConsole("Build FSM is:" + FSMBuild);
				build = FSMBuild.split("\\.")[1];
				build = build.replaceAll("_", ".");
			} else if (enodeB.getArchitecture() == EnodeB.Architecture.FSMv4) {
				GeneralUtils.printToConsole("Build FSMv4 is:" + FSMBuild);
				build = FSMBuild.split("\\.")[1];
				build = build.replaceAll("_", ".");
			} else {
				GeneralUtils.printToConsole("Build XLP is:" + XLPBuild);
				build = XLPBuild.split("\\.")[1];
				build = build.replaceAll("_", ".");
			}
		} catch (Exception e) {
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

	public String getFSMv4Build() {
		return FSMv4Build;
	}

	public void setFSMv4Build(String FSMv4Build) {
		this.FSMv4Build = FSMv4Build;
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

	/**
	 * set Dest Server (by softwareImage), also considering the difference
	 * between Linux and Windows.
	 *
	 * @param softwareImage - softwareImage
	 */
	public void setDestServer(String softwareImage) {
		EnodeBUpgradeImage enodeBUpgradeImage = netspanServer.getSoftwareImage(softwareImage);
		EnodeBUpgradeServer enodeBUpgradeServer = netspanServer
				.getSoftwareServer(enodeBUpgradeImage.getUpgradeServerName());
		String destPath;
		switch (GeneralUtils.getOS()) {
			case CommonConstants.WINDOWS_OS:
				destPath = generateDestFilePath(enodeBUpgradeServer.getUpgradeServerProtocolType(),
						enodeBUpgradeServer.getUpgradeServerIp());
				setDestServer(new File(File.separator + destPath));
				break;
			case CommonConstants.LINUX_OS:
				destPath = generateDestFilePath(enodeBUpgradeServer.getUpgradeServerProtocolType(), "mnt/asil-swit");
				setDestServer(new File(destPath));
				break;
		}

	}

	/**
	 * generate Dest File Path depending on ServerProtocolType (SFTP or TFTP).
	 * The relative path can be adjusted to Linux and to Windows: Windows: can
	 * access the path via ipNumber Linux: The path should be mounted (in
	 * advance)
	 *
	 * @param serverProtocolType SFTP or TFTP
	 * @param relativeFolderPath - IP for windows (enodeBUpgradeServer.getUpgradeServerIp) or
	 *                           mounted path (in advance)
	 */
	public String generateDestFilePath(ServerProtocolType serverProtocolType, String relativeFolderPath) {
		switch (serverProtocolType) {
			case SFTP: {
				return StringTools.getStringWithUnifiedFileSeperator("\\" + relativeFolderPath + "\\sftp\\upload");
			}
			// TFTP
			default: {
				return StringTools.getStringWithUnifiedFileSeperator("\\" + relativeFolderPath + "\\tftp");
			}
		}
	}

	/**
	 * set Source Server, also considering the difference between Linux and
	 * Windows.
	 *
	 * @param sourceServer - path of the version (user's input from Jenkins)
	 */
	public void setSourceServer(String sourceServer) {
		sourceServer = StringTools.getStringWithUnifiedFileSeperator(sourceServer);
		switch (GeneralUtils.getOS()) {
			case CommonConstants.WINDOWS_OS:
				sourceServer = correctPathForWindowsDoubleSlash(sourceServer);
				break;
			case CommonConstants.LINUX_OS:
				sourceServer = sourceServer.replace("Projects", "builds");
				break;
		}
		this.sourceServer = new File(sourceServer);
	}

	/**
	 * Default path setter, if there is no path
	 *
	 * @param build - build
	 */
	public void setSourceServerNoPath(String build) {
		this.sourceServer = new File(StringTools.getStringFileSeperator(sourceServer.getPath() + makeRelease(build),
				build, "release", StringUtils.EMPTY));
	}

	/**
	 * Correct Path For Windows Double Slash - if the path doesn't start with
	 * "//" - fix it.
	 *
	 * @param path - path
	 * @return - new path
	 */
	public String correctPathForWindowsDoubleSlash(String path) {
		if (path != null && path.startsWith("\\") && !path.startsWith("\\\\"))
			path = "\\" + path;
		return path;
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
			if (connectionMethod == SWUpgradeConnectionMethod.Netspan) {
				enodeB.setExpectBooting(true);
				report.report("Rebooting " + enodeB.getNetspanName() + " via Netspan");
				swapBankAndReset = softwareConfigSet(enodeB.getNetspanName(), RequestType.ACTIVATE, imageName);
				if (swapBankAndReset) {
					// Heng - dont worry about the wait this is a safe switch to
					// wait for reboot
					// the wair for allrunnig itself will take 5 minutes so this
					// wait will not affect runtime
					// but will help up to make the code simplier and not wait
					// for reboot event at this point
					GeneralUtils.unSafeSleep(2 * 60 * 1000);
					swapBankAndReset &= enodeB.waitForReboot(5 * 60 * 1000);
					enodeB.setExpectBooting(false);
				} else
					swapBankAndReset = swapBanksAndResetEnodeb(enodeB);
			} else
				swapBankAndReset = swapBanksAndResetEnodeb(enodeB);

			if (!swapBankAndReset) {
				report.report("EnodeB: " + enodeB.getName() + " wasn't rebooted", Reporter.WARNING);
				time = System.currentTimeMillis() - time;
				return;
			}
			// Heng - dont worry about the wait this is a safe switch to wait
			// for reboot
			// the wair for allrunnig itself will take 5 minutes so this wait
			// will not affect runtime
			// but will help up to make the code simplier and not wait for
			// reboot event at this point
			GeneralUtils.unSafeSleep(2 * 60 * 1000);
			pass = enodeB.waitForAllRunning(EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
			if (!isPass()) {
				report.report("EnodeB: " + enodeB.getName() + " didn't reach All runing state during "
						+ (EnodeB.WAIT_FOR_ALL_RUNNING_TIME) / 60000 + " Minutes", Reporter.FAIL);
				time = System.currentTimeMillis() - time;
				report.report("Swaping Banks and rebooting unit " + enodeB.getName());
				// swapBanksAndResetEnodeb(enodeB);
				notToValidateNodes.add(enodeB);
				// Heng - dont worry about the wait this is a safe switch to
				// wait for reboot
				// the wair for allrunnig itself will take 5 minutes so this
				// wait will not affect runtime
				// but will help up to make the code simplier and not wait for
				// reboot event at this point
				/*
				 * GeneralUtils.unSafeSleep(2 * 60 * 1000); boolean allRun =
				 * enodeB.waitForAllRunning(EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
				 * if (allRun) { report.report("All Running State for node : " +
				 * enodeB.getName()); } else { report.report(
				 * "Failed to get to All Running after swaping back banks",
				 * Reporter.WARNING); }
				 */

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

			if (tries <= 0)

				return false;

			File destFile;
			File sourceFile;
			if (component == EnodeB.Architecture.FSM) {
				destFile = new File(destServer.getPath().toString() + File.separator + FSMBuild);
				sourceFile = new File(sourceServer.getPath().toString() + File.separator + FSMBuild);

			} else if (component == EnodeB.Architecture.FSMv4) {
				destFile = new File(destServer.getPath().toString() + File.separator + FSMv4Build);
				sourceFile = new File(sourceServer.getPath().toString() + File.separator + FSMv4Build);
			} else {
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
				GeneralUtils.printToConsole(
						"An error has been occur while trying to copy the file to the upgrade server\nError Message: "
								+ e.getMessage());
				if (tries > 0) {
					GeneralUtils.unSafeSleep(5 * 1000);
					if (!Files.exists(new File(destFile.getPath()).toPath())) {
						return copyVerToTftpServer(component, tries - 1);
					}

					report.report(type + " file has been copied after one or more retries", Reporter.PASS);
					return true;
				}

				report.report(
						"An error has been occur while trying to copy the file to the upgrade server\nError Message: "
								+ e.getMessage(),
						Reporter.FAIL);
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

		public EnodebUpgradeWorker(String build, EnodeB enodeB, ServerProtocolType downloadType, String user,
								   String password) {
			this.enodeB = enodeB;
			GeneralUtils.printToConsole("Create thread for enodeb: " + enodeB.getNetspanName() + " Method: "
					+ downloadType + " User:" + user + " Password:" + password);
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
			if (!isPass()) {
				connectionMethod = SWUpgradeConnectionMethod.SNMP;
				report.report(enodeB.getName() + " - Download failed, retry SNMP", Reporter.WARNING);
				pass = downloadSoftwareViaSnmp();
				if (!isPass()) {
					report.report(enodeB.getName() + " - SNMP download failed", Reporter.WARNING);
					if (TestConfig.getInstace().getSWUCliFallback()) {
						report.report("'SWUCliFallback' flag is true in SUT. Fallback download via CLI");
						downloadSoftwareViaCli();
						connectionMethod = SWUpgradeConnectionMethod.CLI;
					} else {
						report.report("'SWUCliFallback' flag is false in SUT or doesn't exist. Skip download via CLI");
					}
				}
			}

			if (!isStandbyUpdatedViaSnmp(enodeB)) {
				report.report(enodeB.getName() + " - The Secondary bank doesn't contain the target version ",
						Reporter.WARNING);
				pass = false;

			} else
				pass = true;
			time = System.currentTimeMillis() - time;
			GeneralUtils.printToConsole("Time for Upgrade:" + time);

		}

		private boolean downloadSoftwareViaCli() {
			String build;
			if (enodeB.getArchitecture() == EnodeB.Architecture.FSM) {
				build = FSMBuild;
			} else if (enodeB.getArchitecture() == EnodeB.Architecture.FSMv4) {
				build = FSMv4Build;
			} else {
				build = XLPBuild;
			}

			if (downloadType == ServerProtocolType.SFTP)
				build = "upload/" + build;
			enodeB.downloadSWCli(build, downloadType, user, password);
			String doneMessage = "post download stage finished succesfully";
			report.report(
					enodeB.getName() + " - Waiting for software download to finish and report \"" + doneMessage + "\"");
			boolean doneMessageFound = Log.getInstance().waitForLogLine(enodeB, EnodeBComponentTypes.XLP, doneMessage,
					EnodeB.DOWNLOAD_TIMEOUT);
			if (doneMessageFound) {
				return true;
			}

			report.report(enodeB.getName() + " - Software download failed due to timeout", Reporter.WARNING);
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
			} else if (enodeB.getArchitecture() == EnodeB.Architecture.FSMv4) {
				build = FSMv4Build;
			} else {
				build = XLPBuild;
			}

			if (downloadType == ServerProtocolType.SFTP)
				build = "upload/" + build;
			boolean downloadPass = enodeB.downloadSWSnmp(build, downloadType, user, password);
			GeneralUtils.unSafeSleep(5 * 1000);
			if (downloadPass) {
				return enodeB.getDownloadStates();
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
		return String.format("%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes(milis),
				TimeUnit.MILLISECONDS.toSeconds(milis)
						- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milis)));
	}

	/**
	 * created by Moran Goldenberg
	 *
	 * @param server
	 * @return true if the software server was found in Netspan with all
	 * parameters and false if it didn't find it.
	 */
	public boolean isSoftwareServerExists(EnodeBUpgradeServer server) {
		EnodeBUpgradeServer localServer;
		try {
			localServer = netspanServer.getSoftwareServer(server.getUpgradeServerName());
			if (localServer != null) {
				if (localServer.getUpgradeServerIp().equals(server.getUpgradeServerIp())) {
					if (localServer.getUpgradeServerProtocolType() == server.getUpgradeServerProtocolType()) {
						report.report("The upgrade server was found.");
						report.report("Server Name: " + server.getUpgradeServerName());
						report.report("Server IP: " + server.getUpgradeServerIp());
						report.report("Server Protocol: " + server.getUpgradeServerProtocolType().toString());
						return true;

					} else {
						report.report(
								"Protocol type soesn't match to " + server.getUpgradeServerProtocolType().toString());
						return false;
					}

				} else {
					report.report("The IP adress doesn't match to " + server.getUpgradeServerIp());
					return false;
				}

			} else {
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
	 *
	 * @param image
	 * @return true if the software Image was found in Netspan with all
	 * parameters and false if it didn't find it.
	 */
	public boolean isSoftwareImageExists(EnodeBUpgradeImage image) {
		EnodeBUpgradeImage localimage;
		try {
			localimage = netspanServer.getSoftwareImage(image.getName());
			if (localimage != null) {
				if (localimage.getHardwareCategory() == image.getHardwareCategory()) {
					if (localimage.getUpgradeServerName().equals(image.getUpgradeServerName())) {
						if (localimage.getBuildPath().equals(image.getBuildPath())) {
							if (localimage.getVersion().equals(image.getVersion())) {
								report.report("The upgrade Image was found.");
								report.report("Image Name: " + image.getName());
								report.report("Image Hardware Category: " + image.getHardwareCategory().toString());
								report.report("Software server name: " + image.getUpgradeServerName());
								report.report("Build path: " + image.getBuildPath());
								report.report("build version: " + image.getVersion());
								return true;
							} else {

								// report.report("varsion doesn't match to "+
								// image.getVersion());
								report.report("version is " + localimage.getVersion() + " and not " + image.getVersion()
										+ " as expected");
								return false;
							}

						} else {
							report.report("Build path doesn't match to " + image.getBuildPath());
							report.report("Build path is " + localimage.getBuildPath() + " and not "
									+ image.getBuildPath() + " as expected");
							return false;
						}

					} else {
						report.report("Hardware category doesn't match to " + image.getHardwareCategory().toString());
						report.report("Hardware category is " + localimage.getHardwareCategory() + " and not "
								+ image.getHardwareCategory() + " as expected");
						return false;
					}

				} else {
					report.report("Image name doesn't match to " + image.getName());
					report.report(
							"Image name is " + localimage.getName() + " and not " + image.getName() + " as expected");
					return false;
				}

			} else {
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
	 *
	 * @param imageName
	 * @return true if the software Image was found in Netspan by its name
	 */
	public boolean isSoftwareImageExists(String imageName) {
		EnodeBUpgradeImage localimage;
		try {
			localimage = netspanServer.getSoftwareImage(imageName);
			if (localimage != null) {
				report.report("There is upgrade Image by the name " + imageName);
				return true;

			} else {
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
	 *
	 * @param nodeName
	 * @param imageType
	 * @return software status from netspan
	 */
	public SoftwareStatus getSoftwareStatus(String nodeName, ImageType imageType) {
		SoftwareStatus status = new SoftwareStatus();
		try {
			status = netspanServer.getSoftwareStatus(nodeName, imageType);
			if (status == null) {
				report.report("Couldn't get software server status from netspan");
			}
		} catch (Exception e) {
			report.report("Couldn't get software server status from netspan");
			e.printStackTrace();
		}
		return status;
	}

	public boolean softwareConfigSet(String nodeName, RequestType requestType, String softwareImage) {
		boolean result = false;
		try {
			result = netspanServer.softwareConfigSet(nodeName, requestType, softwareImage);
		} catch (Exception e) {
			report.report("Couldn't config software downlad by netspan");
			e.printStackTrace();
		}
		return result;
	}

	public boolean createSoftwareImage(EnodeBUpgradeImage upgradeImage) {
		boolean result;
		try {
			result = netspanServer.createSoftwareImage(upgradeImage);
			if (!result) {
				return false;

			} else {
				report.report("Software Image was created");
				report.report("Image name: " + upgradeImage.getName());
				report.report("Hardware Category: " + upgradeImage.getHardwareCategory());
				report.report("Software server: " + upgradeImage.getUpgradeServerName());
				report.report("Build path: " + upgradeImage.getBuildPath());
				report.report("Software version: " + upgradeImage.getVersion());
				report.report("Software image type: " + upgradeImage.getImageType());
			}

		} catch (Exception e) {
			report.report("Couldn't create software Image by netspan");
			e.printStackTrace();
		}
		return true;
	}

	public boolean updateSoftwareImage(EnodeBUpgradeImage upgradeImage) {
		boolean result;
		try {
			result = netspanServer.updateSoftwareImage(upgradeImage);
			if (!result) {
				return false;

			} else {
				report.report("Software Image was updated");
				report.report("Image name: " + upgradeImage.getName());
				report.report("Hardware Category: " + upgradeImage.getHardwareCategory());
				report.report("Software server: " + upgradeImage.getUpgradeServerName());
				report.report("Build Path: " + upgradeImage.getBuildPath());
				report.report("Software version: " + upgradeImage.getVersion());
			}

		} catch (Exception e) {
			report.report("Couldn't update software Image by netspan");
			e.printStackTrace();
		}
		return true;
	}

	public boolean createSoftwareServer(EnodeBUpgradeServer upgradeServer) {
		boolean result;
		try {
			result = netspanServer.createSoftwareServer(upgradeServer);
			if (!result) {
				return false;

			} else {
				report.report("Software Server was created");
				report.report("Server name: " + upgradeServer.getUpgradeServerName());
				report.report("Server IP: " + upgradeServer.getUpgradeServerIp());
				report.report("Protocol Type: " + upgradeServer.getUpgradeServerProtocolType());
			}

		} catch (Exception e) {
			report.report("Couldn't create software server by netspan");
			e.printStackTrace();
		}
		return true;
	}

	public boolean createUpgradeServer(EnodeBUpgradeServer server) {
		boolean flag = true;
		GeneralUtils.startLevel("Creating Software server");
		boolean isSWExist = isSoftwareServerExists(server);
		if (!isSWExist) {
			boolean wasCreated = createSoftwareServer(server);
			if (!wasCreated) {
				report.report("The Software Server was not created as expected", Reporter.WARNING);
				flag = false;

			}
		}
		GeneralUtils.stopLevel();
		return flag;
	}

	public boolean deleteSoftwareImage(String softwareImageName) {
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

	public boolean downloadFile(EnodeB node, boolean isUpgrade) {
		boolean tempPass = false;
		AlarmsAndEvents event = AlarmsAndEvents.getInstance();
		EventInfo tempEvent = new EventInfo();
		Date current = new Date();
		Date timeOut = org.apache.commons.lang3.time.DateUtils.addMinutes(current, 20);
		while (current.before(timeOut)) {
			tempEvent = event.waitForEventShowUp(node, "Software Download", 10);
			if (tempEvent != null) {

				if (tempEvent.getEventInfo().toLowerCase().contains("download completed")
						|| tempEvent.getEventInfo().toLowerCase().contains("download and activate completed")) {
					report.report(tempEvent.getEventInfo());
					tempPass = true;
					break;
				}
				if (tempEvent.getEventInfo().contains("Download failed")) {
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
		if (!tempPass) {

			report.report("There was no event to indicate on software download", Reporter.WARNING);
			return false;
		}

		if (isUpgrade && !tempEvent.getEventInfo().contains("download and activate completed")) {
			current = new Date();
			while (current.before(timeOut)) {
				tempEvent = event.waitForEventShowUp(node, "Software Activate", 10);
				if (tempEvent != null) {

					if (tempEvent.getEventInfo().toLowerCase().contains("activate completed")) {
						report.report(tempEvent.getEventInfo());
						tempPass = true;
						break;
					}
					if (tempEvent.getEventInfo().toLowerCase().contains("activate failed")) {
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

	public boolean createUpgradeImage(EnodeBUpgradeServer server, String buildName, String imageName, String targetVer,
									  HardwareCategory hardwareCategory, String imageType) {
		if (server.getUpgradeServerProtocolType() == ServerProtocolType.SFTP) {
			buildName = "/upload/" + buildName;
		}
		EnodeBUpgradeImage image = new EnodeBUpgradeImage(imageName, hardwareCategory, server.getUpgradeServerName(),
				server.getUpgradeServerProtocolType(), buildName, targetVer, imageType);
		boolean flag = true;

		GeneralUtils.startLevel("Creating Software Image");
		boolean isSWExist = isSoftwareImageExists(image.getName());
		if (!isSWExist) {
			boolean wasCreated = createSoftwareImage(image);
			if (!wasCreated) {
				report.report("The Software Image was not created as expected", Reporter.WARNING);
				flag = false;
			}
		} else {
			isSWExist = isSoftwareImageExists(image);
			if (!isSWExist) {
				boolean wasUpdated = updateSoftwareImage(image);
				if (!wasUpdated) {
					report.report("The Software Image was not updated as expected", Reporter.WARNING);
					flag = false;
				}
			}
		}

		GeneralUtils.stopLevel();
		return flag;
	}

	public ArrayList<EnodeB> noValidationNodes() {
		return this.notToValidateNodes;
	}

	public EnodebSwStatus updateDefaultSoftwareImage(EnodeB eNodeB, String buildPath, String relayBuildPath) {
		EnodebSwStatus enbSWDetails = new EnodebSwStatus(eNodeB);
		String build = "";
		String relayBuild = "";
		String softwareImage = eNodeB.defaultNetspanProfiles.getSoftwareImage();
		if (softwareImage != null) {
			EnodeBUpgradeImage enodeBUpgradeImage = netspanServer.getSoftwareImage(softwareImage);
			EnodeBUpgradeServer enodeBUpgradeServer = netspanServer.getSoftwareServer(enodeBUpgradeImage.getUpgradeServerName());
			setDestServer(softwareImage);
			String buildsPath = buildPath;

			if (buildsPath != null && buildsPath.startsWith("\\") && !buildsPath.startsWith("\\\\")) {
				buildsPath = "\\" + buildsPath;
			}

			setSourceServer(buildsPath);
			if (setBuildFiles()) {
				VersionCopyWorker versionCopyWorker = new VersionCopyWorker(eNodeB.getArchitecture(),
						enodeBUpgradeServer.getUpgradeServerProtocolType());
				versionCopyWorker.startCopy();

				String fsmBuildFileName = getFSMBuild();
				String fsmv4BuildFileName = getFSMv4Build();
				String xlpBuildFileName = getXLPBuild();
				String relayBuildFileName = getRelayTargetBuildFileName(relayBuildPath);

				// when build not mention or not found, set the running version
				// as version target.
				if ((eNodeB instanceof AirUnity)
						&& (relayBuildFileName == null || relayBuildFileName.equals(StringUtils.EMPTY))) {
					report.report("Relay build not mention or not found, setting running version as target version.",
							Reporter.WARNING);
					SoftwareStatus softwareStatus = netspanServer.getSoftwareStatus(eNodeB.getNetspanName(),
							ImageType.RELAY);
					GeneralUtils.printToConsole("Image type: "+softwareStatus.ImageType);
					GeneralUtils.printToConsole("Running version: "+softwareStatus.RunningVersion);
					if (softwareStatus != null) {
						relayBuildFileName = "airunity-" + softwareStatus.RunningVersion + ".pak";
						report.report("Setting current relay version from software image: "+relayBuildFileName);
					}
				}
				String fsmBuild = StringUtils.EMPTY;
				if (fsmBuildFileName != null && !fsmBuildFileName.equals("")) {
					fsmBuild = fsmBuildFileName.substring(fsmBuildFileName.indexOf(".") + 1);
					fsmBuild = fsmBuild.substring(0, fsmBuild.indexOf("."));
				}
				String fsmv4Build = StringUtils.EMPTY;
				if (fsmv4BuildFileName != null && !fsmv4BuildFileName.equals("")) {
					fsmv4Build = fsmv4BuildFileName.substring(fsmv4BuildFileName.indexOf(".") + 1);
					fsmv4Build = fsmv4Build.substring(0, fsmv4Build.indexOf("."));
				}

				String xlpBuild = StringUtils.EMPTY;
				if (xlpBuildFileName != null && !xlpBuildFileName.equals("")) {
					xlpBuild = xlpBuildFileName.substring(xlpBuildFileName.indexOf(".") + 1);
					xlpBuild = xlpBuild.substring(0, xlpBuild.indexOf("."));
				}

				if (eNodeB instanceof AirUnity) {
					relayBuild = getRelayTargetBuild(eNodeB, relayBuildFileName);
				}
				String buildFileName = StringUtils.EMPTY;
				if (eNodeB.getArchitecture() == Architecture.FSM) {
					buildFileName = fsmBuildFileName;
					build = fsmBuild;
				} else if (eNodeB.getArchitecture() == Architecture.FSMv4) {
					buildFileName = fsmv4BuildFileName;
					build = fsmv4Build;
				} else if (eNodeB.getArchitecture() == Architecture.XLP) {
					buildFileName = xlpBuildFileName;
					build = xlpBuild;
				}

				build = build.replaceAll("_", ".");
				if (buildFileName != null && !buildFileName.equals(StringUtils.EMPTY)) {
					SoftwareStatus softwareStatus = netspanServer.getSoftwareStatus(eNodeB.getNetspanName(), eNodeB.getImageType());
					report.report("Checking EnodeB - Running and Standby SW version");
					updateCurrentSwStatus(enbSWDetails, build, softwareStatus);
					EnodeBUpgradeImage upgradeImage = new EnodeBUpgradeImage();
					upgradeImage.setName(softwareImage);
					if (softwareStatus != null)
						upgradeImage.setImageType(softwareStatus.ImageType);
					else
						upgradeImage.setImageType(eNodeB.getImageType().value());

					if (enodeBUpgradeServer.getUpgradeServerProtocolType() == ServerProtocolType.SFTP) {
						buildFileName = "/upload/" + buildFileName;
					}
					upgradeImage.setBuildPath(buildFileName);
					upgradeImage.setVersion(build);
					// Set HW cat to fix Netspan bug!! NEED to remove.
					upgradeImage.setHardwareCategory(netspanServer.getHardwareCategory(eNodeB));

					if (!updateSoftwareImage(upgradeImage)) {
						report.report("FAILED To Update Software Image.", Reporter.FAIL);
						enbSWDetails.setNumberOfExpectedReboots(GeneralUtils.ERROR_VALUE);
					}
				}
				if ((eNodeB instanceof AirUnity) && (relayBuild != "")) {
					SoftwareStatus softwareStatus = netspanServer.getSoftwareStatus(eNodeB.getNetspanName(), ImageType.RELAY);
					if (softwareStatus != null) {
						report.report(eNodeB.getName() + "'s Relay Running Version: " + softwareStatus.RunningVersion);
						if (!softwareStatus.RunningVersion.equals(relayBuild)) {
							enbSWDetails.increaseNumberOfExpectedReboots();
						} else {
							report.report("Running Version equals Target Version - No Need To Upgrade Relay Version.");
						}
					}
					EnodeBUpgradeImage upgradeImage = new EnodeBUpgradeImage();
					upgradeImage.setName(softwareImage);
					upgradeImage.setImageType(ImageType.RELAY.value());
					upgradeImage.setBuildPath(relayBuildFileName);
					upgradeImage.setVersion(relayBuild);
					// Set HW cat to fix Netspan bug!! NEED to remove.
					upgradeImage.setHardwareCategory(netspanServer.getHardwareCategory(eNodeB));
					if (softwareStatus != null) {
						if (!updateSoftwareImage(upgradeImage)) {
							report.report("FAILED To Update Software Image with Relay Version.", Reporter.FAIL);
							enbSWDetails.setNumberOfExpectedReboots(GeneralUtils.ERROR_VALUE);
						}
					}
				}

			} else {
				report.report("FAILED to find build files.", Reporter.FAIL);
				enbSWDetails.setNumberOfExpectedReboots(GeneralUtils.ERROR_VALUE);
			}

		} else {
			report.report("SoftwareImage doesn't exist.", Reporter.FAIL);
			enbSWDetails.setNumberOfExpectedReboots(GeneralUtils.ERROR_VALUE);
		}
		report.report("Number Of Expected Reboots: " + enbSWDetails.getNumberOfExpectedReboots());
		enbSWDetails.setTargetVersion(build);
		enbSWDetails.setRelayTargetVersion(relayBuild);
		return enbSWDetails;
	}

	/**
	 * updating flags:  isTargetEqualRunning, isTargetEqualStandby according to the current SW states.
	 *
	 * @param enbSWDetails   - enb SW Details
	 * @param build          - build
	 * @param softwareStatus - software Status
	 */
	private void updateCurrentSwStatus(EnodebSwStatus enbSWDetails, String build, SoftwareStatus softwareStatus) {
		if (softwareStatus != null) {
			report.report(enbSWDetails.geteNodeB().getName() + "'s Running Version: " + softwareStatus.RunningVersion);
			report.report(enbSWDetails.geteNodeB().getName() + "'s Standby Version: " + softwareStatus.StandbyVersion);
			if (softwareStatus.RunningVersion.equals(build)) {
				enbSWDetails.setTargetEqualRunning(true);
				report.step("Running Version equals Target Version - No Need To Upgrade.");
				return;
			}
			//Increase NumberOfExpectedReboots in case Running != Target
			enbSWDetails.increaseNumberOfExpectedReboots();
			if (softwareStatus.StandbyVersion.equals(build)) {
				enbSWDetails.setTargetEqualStandby(true);
				report.report("Standby Version equals Target Version - No Need To Upgrade.");
			}
		}else{
			report.report("softwareStatus is null",Reporter.WARNING);
		}
	}

	private String getRelayTargetBuildFileName(String relayVerPath) {
		String fileName = "";
		String relayBuildPath = relayVerPath;
		if (relayBuildPath != null) {
			if (relayBuildPath.startsWith("\\") && !relayBuildPath.startsWith("\\\\")) {
				relayBuildPath = "\\" + relayBuildPath;
			}

			File relaySourceServer = new File(relayBuildPath);
			File[] files = relaySourceServer.listFiles();
			if (files != null) {
				for (File file : files) {
					GeneralUtils.printToConsole("Checking File:" + file.toString());
					if (file.getName().contains(".pak") && (!file.getName().contains("mirror"))) {
						fileName = file.getName();
						report.report("Relay Build Name Is: " + fileName);
						File destFile = new File(destServer.getPath().toString() + File.separator + fileName);
						File sourceFile = file;
						/**
						 *
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
		}
		return fileName;
	}

	private String getRelayTargetBuild(EnodeB eNodeB, String relayBuildFileName) {
		String relayBuild = "";
		if (relayBuildFileName != null && relayBuildFileName != "") {
			relayBuild = relayBuildFileName.substring(relayBuildFileName.indexOf("-") + 1);
			relayBuild = relayBuild.substring(0, relayBuild.indexOf(".pak"));
		}

		return relayBuild;
	}

	public boolean isRelayVersionUpdated(EnodeB eNodeB, String realyTargetVersion, int logLevel) {
		boolean result = false;
		if ((eNodeB instanceof AirUnity) && (realyTargetVersion != null) && (realyTargetVersion != "")) {

			SoftwareStatus softwareStatus = netspanServer.getSoftwareStatus(eNodeB.getNetspanName(), ImageType.RELAY);
			if (softwareStatus != null) {
				report.report(eNodeB.getName() + "'s Relay Running Version: " + softwareStatus.RunningVersion);
				if (softwareStatus.RunningVersion.equals(realyTargetVersion)) {
					report.report("The running bank contains target version " + realyTargetVersion
							+ " on eNodeB's Relay: " + eNodeB.getName());
					result = true;
				} else {
					report.report("The running bank does NOT contains target version, running version is "
							+ softwareStatus.RunningVersion + " on eNodeB's Relay: " + eNodeB.getName()
							+ " while target version is " + realyTargetVersion, logLevel);
					result = false;
				}
			}

		} else {
			result = true;
		}

		return result;
	}

	public static String getVersionFromPath(String path) {
		path = path + "\\";
		String version = "";
		final String regex = "[\\\\]+[0-9,_]+[\\\\]+";
		final Pattern pattern = Pattern.compile(regex);
		final Matcher matcher = pattern.matcher(path);
		if (matcher.find()) {
			version = matcher.group(0);
			version = version.replace("\\", "");
			version = version.replaceAll("_", "\\.");
		}
		return version;
	}

	public void printSoftwareImageDetails(EnodeB eNodeB) {
		EnodeBUpgradeImage enodeBUpgradeImage = netspanServer
				.getSoftwareImage(eNodeB.getDefaultNetspanProfiles().getSoftwareImage());
		EnodeBUpgradeServer enodeBUpgradeServer = netspanServer
				.getSoftwareServer(enodeBUpgradeImage.getUpgradeServerName());

		GeneralUtils.startLevel(eNodeB.getName() + "'s Software Image Details.");
		report.report("Software Image = " + enodeBUpgradeImage.getName());
		report.report("eNodeB Software Version = " + enodeBUpgradeImage.getVersion());
		report.report("Software Server = " + enodeBUpgradeImage.getUpgradeServerName());
		report.report("Software Server IP = " + enodeBUpgradeServer.getUpgradeServerIp());
		report.report("Software Server Protocol Type = " + enodeBUpgradeServer.getUpgradeServerProtocolType());
		if (eNodeB instanceof AirUnity) {
			report.report("Relay Software Version = " + enodeBUpgradeImage.getSecondVersion());
		}
		GeneralUtils.stopLevel();
	}

	/**
	 * Follow SW Activation Progress (and download if needed)  Via Netspan
	 *
	 * @param enbSWDetailsList - enbSWDetailsList
	 */
	public void followSwUpgradeProgressViaNetspan(ArrayList<EnodebSwStatus> enbSWDetailsList) {
		ArrayList<EnodebSwStatus> enbThatHaveToRebootList = getEnbsThatHaveToReboot(enbSWDetailsList);
		followProgress(new ArrayList<>(enbSWDetailsList), enbThatHaveToRebootList);
	}

	/**
	 * Get  Enb That Have To Reboot
	 * *This method changes the original List*
	 *
	 * @param enbSWDetailsList - enbSWDetailsList
	 * @return - numberOfEnbThatHaveToReboot
	 */
	private ArrayList<EnodebSwStatus> getEnbsThatHaveToReboot(ArrayList<EnodebSwStatus> enbSWDetailsList) {
		ArrayList<EnodebSwStatus> enbThatHaveToRebootList = new ArrayList<>();
		for (EnodebSwStatus enbSWDetails : enbSWDetailsList) {
			printSoftwareImageDetails(enbSWDetails.geteNodeB());
			if (enbSWDetails.getNumberOfExpectedReboots() > 0) {
				enbSWDetails.geteNodeB().setExpectBooting(true);
				enbThatHaveToRebootList.add(enbSWDetails);
			}
		}
		return enbThatHaveToRebootList;
	}

	/**
	 * Follow Activation an Download Progress
	 *
	 * @param enbSWDetailsList        - enbSWDetailsList
	 * @param enbThatHaveToRebootList - numberOfEnbThatHaveToReboot
	 */
	private void followProgress(ArrayList<EnodebSwStatus> enbSWDetailsList, ArrayList<EnodebSwStatus> enbThatHaveToRebootList) {
		int maxNumberOfReboots = 2;
		GeneralUtils.startLevel("Verify Software Upgrade Process.");
		//todo - remove loop and replace. this loop is just for relay product.
		for (int i = 1; i <= maxNumberOfReboots; i++) {
			if (!enbThatHaveToRebootList.isEmpty()) {
				followSoftwareDownloadProgress(enbSWDetailsList);
				followSoftwareActivateProgress(enbSWDetailsList);
				enbThatHaveToRebootList.removeIf(enbThatHaveToReboot -> enbThatHaveToReboot.getNumberOfExpectedReboots() <= 1);
			}
		}
		GeneralUtils.stopLevel();
	}

	/**
	 * follow Software Download Progress Via Netspan
	 *
	 * @param eNodebSwStatusList - eNodebSwStatusList
	 */
	private void followSoftwareDownloadProgressViaNetspan(ArrayList<EnodebSwStatus> eNodebSwStatusList) {
		Iterator<EnodebSwStatus> iter;
		do {
			iter = eNodebSwStatusList.iterator();
			while (iter.hasNext()) {
				EnodebSwStatus eNodebSwStatus = iter.next();
				if (isSkipTrackingDownload(iter, eNodebSwStatus)) continue;
				//Track download progress
				Pair<Boolean, SwStatus> swStatusPair = eNodebSwStatus.geteNodeB().isSoftwareDownloadCompletedSuccessfully();
				eNodebSwStatus.setSwDownloadCompleted(swStatusPair.getElement0());
				eNodebSwStatus.setSwStatus(swStatusPair.getElement1());
				//Wait for download completion
				checkIfDownloadCompleted(iter, eNodebSwStatus);
				//Pause before next iteration
				GeneralUtils.unSafeSleep(10 * 1000);
				printNetspanEventIfReceived(eNodebSwStatus, NetspanSWEvents.NetspanEvents.DOWNLOAD_IN_PROGRESS);
				printDownloadProcessTime(eNodebSwStatus);
			}
		}
		while ((!eNodebSwStatusList.isEmpty())
				&& (System.currentTimeMillis() - softwareActivateStartTimeInMili <= (EnodeB.DOWNLOAD_TIMEOUT)));
		for (EnodebSwStatus eNodebSwStatus : eNodebSwStatusList) {
			if (!eNodebSwStatus.isSwDownloadCompleted()) {
				report.report(eNodebSwStatus.geteNodeB().getName() + ": Software Download Didn't End.", Reporter.FAIL);
			}
		}
	}

	/**
	 * Print Download Process Time and warns if it's more than 20 minutes
	 */
	private void printDownloadProcessTime(EnodebSwStatus eNodebSwStatus) {
		long downloadTime = System.currentTimeMillis() - softwareActivateStartTimeInMili;
		if (eNodebSwStatus.isSwDownloadCompleted()) {
			if (downloadTime >= EnodeB.DOWNLOAD_WARNING_TIME) {
				report.report(eNodebSwStatus.geteNodeB().getNetspanName() + ": Download process took more than 20 minutes. Actual time in minutes:"
						+ GeneralUtils.milisToMinutes(downloadTime), Reporter.WARNING);
			} else {
				report.report(eNodebSwStatus.geteNodeB().getNetspanName() + ": Download process lasted (in minutes): " +
						GeneralUtils.milisToMinutes(downloadTime));
			}
		}
	}

	/**
	 * Check If Download Completed + update flag + remove from tracking list
	 *
	 * @param iter           - current iter pointer
	 * @param eNodebSwStatus - eNodebSwStatus
	 */
	private void checkIfDownloadCompleted(Iterator<EnodebSwStatus> iter, EnodebSwStatus eNodebSwStatus) {
		if (eNodebSwStatus.isSwDownloadCompleted()) {
			iter.remove();
		}
		//If status=failed -> remove from retry
		else if (eNodebSwStatus.getSwStatus() == SwStatus.SW_STATUS_INSTALL_FAILURE
				|| eNodebSwStatus.getSwStatus() == SwStatus.SW_STATUS_ACTIVATION_FAILURE) {
			eNodebSwStatus.setSwDownloadCompleted(false);
			iter.remove();
		} else {
			//This case is for relay. When it get into this method in the second iteration
			eNodebSwStatus.setSwDownloadCompleted(false);
		}
	}

	/**
	 * If Running\StandBy==Target - there's no download to track
	 * Removes from EnodebSwStatus list
	 *
	 * @param iter           - iter
	 * @param eNodebSwStatus - eNodebSwStatus
	 * @return - true if skip is needed
	 */
	private boolean isSkipTrackingDownload(Iterator<EnodebSwStatus> iter, EnodebSwStatus eNodebSwStatus) {
		if (eNodebSwStatus.isTargetEqualRunning() || eNodebSwStatus.isTargetEqualStandby()) {
			report.report(eNodebSwStatus.geteNodeB().getNetspanName() + ": No need to download version since it's already in the bank.");
			iter.remove();
			return true;
		}
		return false;
	}

	/**
	 * follow Software Activate Progress And wait for all running + wait for the relevant 2 Netspan events
	 *
	 * @param eNodebSwStatusList - eNodebSwStatusList
	 */
	private void followSoftwareActivateProgress(ArrayList<EnodebSwStatus> eNodebSwStatusList) {
		GeneralUtils.startLevel("Verify Software Activation.");
		verifyActivateInProgress(new ArrayList<>(eNodebSwStatusList));
		for (EnodebSwStatus enodebSwStatus : eNodebSwStatusList)
			enodebSwStatus.geteNodeB().updateVersions();
		waitForAllRunningAndInService(new ArrayList<>(eNodebSwStatusList));
		GeneralUtils.stopLevel();
	}

	/**
	 * follow Software Download Progress + wait for the relevant 2 Netspan events
	 *
	 * @param eNodebSwStatusList - eNodebSwStatusList
	 */
	private void followSoftwareDownloadProgress(ArrayList<EnodebSwStatus> eNodebSwStatusList) {
		GeneralUtils.startLevel("Verify Software Download.");
		followSoftwareDownloadProgressViaNetspan(new ArrayList<>(eNodebSwStatusList));
		followSoftwareDownloadCompletedEvent(eNodebSwStatusList);
		GeneralUtils.stopLevel();
	}

	/**
	 * follow Software Download Progress Via Netspan
	 *
	 * @param eNodebSwStatusList - eNodebSwStatusList
	 */
	private void followSoftwareDownloadCompletedEvent(ArrayList<EnodebSwStatus> eNodebSwStatusList) {
		for (EnodebSwStatus enodebSwStatus : eNodebSwStatusList) {
			long eventTimeoutStart = System.currentTimeMillis();
			while (System.currentTimeMillis() - eventTimeoutStart <= (EnodeB.DOWNLOAD_COMPLETED_TIMEOUT)) {
				printNetspanEventIfReceived(enodebSwStatus, NetspanSWEvents.NetspanEvents.DOWNLOAD_COMPLETED);
			}
		}
	}

	/**
	 * Verify Activate In Progress
	 * Wait For Reboot And Set Expected Booting For Second Reboot
	 *
	 * @param eNodebSwStatusList - eNodebSwStatusList
	 */
	private void verifyActivateInProgress(ArrayList<EnodebSwStatus> eNodebSwStatusList) {
		final long waitForRebootStartTime = System.currentTimeMillis();
		Iterator<EnodebSwStatus> iter;
		do {
			iter = eNodebSwStatusList.iterator();
			while (iter.hasNext()) {
				EnodebSwStatus eNodebSwStatus = iter.next();
				printNetspanEventIfReceived(eNodebSwStatus, NetspanSWEvents.NetspanEvents.ACTIVATE_IN_PROGRESS);
				GeneralUtils.printToConsole(eNodebSwStatus.geteNodeB().getName() + ".isExpectBooting() = " + eNodebSwStatus.geteNodeB().isExpectBooting());
				if (!eNodebSwStatus.geteNodeB().isExpectBooting()) {
					eNodebSwStatus.increaseNumberOfReboots();
					//if we expect another reboot due to relay
					if (eNodebSwStatus.getNumberOfActualReboot() < eNodebSwStatus.getNumberOfExpectedReboots()) {
						eNodebSwStatus.geteNodeB().setExpectBooting(true);
					}
					iter.remove();
				}
			}
			GeneralUtils.unSafeSleep(5000);
		}
		while ((!eNodebSwStatusList.isEmpty()) && (System.currentTimeMillis() - waitForRebootStartTime <= timeout));
		for (EnodebSwStatus eNodebSwStaus : eNodebSwStatusList) {
			report.report(eNodebSwStaus.geteNodeB().getName() + " has NOT been rebooted.", Reporter.WARNING);
		}
	}

	public void waitForAllRunningAndInService(ArrayList<EnodebSwStatus> eNodebSwStatusList) {
		GeneralUtils.startLevel("Wait For ALL RUNNING And In Service.");
		while ((!eNodebSwStatusList.isEmpty())
				&& (System.currentTimeMillis() - softwareActivateStartTimeInMili <= (EnodeB.ACTIVATE_TIMEOUT + EnodeB.DOWNLOAD_TIMEOUT))) {
			Iterator<EnodebSwStatus> iter;
			iter = eNodebSwStatusList.iterator();
			while (iter.hasNext()) {
				EnodebSwStatus eNodebSwStatus = iter.next();
				printNetspanEventIfReceived(eNodebSwStatus, NetspanSWEvents.NetspanEvents.ACTIVATE_COMPLETED);
				checkOperationalStatus(iter, eNodebSwStatus);
			}
			GeneralUtils.unSafeSleep(5 * 1000);
		}
		for (EnodebSwStatus eNodebSwStatus : eNodebSwStatusList) {
			//todo looks like a bug. check why it samples isSwDownloadCompleted instead of setInRunningState.
			if (!eNodebSwStatus.isSwDownloadCompleted()) {
				report.report(eNodebSwStatus.geteNodeB().getName() + ": Didn't Reach To Running State.", Reporter.FAIL);
			}
		}
		GeneralUtils.stopLevel();
	}

	/**
	 * Check Operational Status - AllRunning+InService, update flags + remove from tracking list
	 *
	 * @param iter           - iter pointer
	 * @param eNodebSwStatus - current eNodebSwStatus object
	 */
	private void checkOperationalStatus(Iterator<EnodebSwStatus> iter, EnodebSwStatus eNodebSwStatus) {
		if (eNodebSwStatus.geteNodeB().isInOperationalStatus()) {
			eNodebSwStatus.setInRunningState(true);
			report.report(eNodebSwStatus.geteNodeB().getName() + " is in Running State.", Reporter.PASS);
			iter.remove();
		}
	}

	public boolean validateRunningVersion(ArrayList<EnodebSwStatus> eNodebSwStatusList) {
		boolean res = true;
		GeneralUtils.startLevel("Validate Running Version.");
		for (EnodebSwStatus eNodebSwStatus : eNodebSwStatusList) {
			res &= isVersionUpdated(eNodebSwStatus.geteNodeB(), Reporter.FAIL);
			res &= isRelayVersionUpdated(eNodebSwStatus.geteNodeB(), eNodebSwStatus.getRelayTargetVersion(), Reporter.FAIL);
			if (!(eNodebSwStatus.isInRunningState() || eNodebSwStatus.geteNodeB().isInService())) {
				report.report(eNodebSwStatus.geteNodeB().getName() + " is NOT in All-Running state", Reporter.FAIL);
				res = false;
			}
		}
		GeneralUtils.stopLevel();
		return res;
	}

	/**
	 * Deal with the 4 event cases - print the event if not printed yet. update the flag accordingly
	 *
	 * @param eNodebSwStatus       - eNodebSwStatus
	 * @param requiredNetspanEvent - requiredNetspanEvent
	 */
	private void printNetspanEventIfReceived(EnodebSwStatus eNodebSwStatus, NetspanSWEvents.NetspanEvents requiredNetspanEvent) {
		switch (requiredNetspanEvent) {
			case DOWNLOAD_IN_PROGRESS:
				if (!eNodebSwStatus.getReceivedEvent().downloadProgress) {
					eNodebSwStatus.getReceivedEvent().downloadProgress = printEventIfReceived(eNodebSwStatus,
							NetspanSWEvents.NetspanEvents.DOWNLOAD_IN_PROGRESS);
				}
				break;

			case DOWNLOAD_COMPLETED:
				if (!eNodebSwStatus.getReceivedEvent().downloadCompleted) {
					eNodebSwStatus.getReceivedEvent().downloadCompleted = printEventIfReceived(eNodebSwStatus,
							NetspanSWEvents.NetspanEvents.DOWNLOAD_COMPLETED);
				}
				break;

			case ACTIVATE_IN_PROGRESS:
				if (!eNodebSwStatus.getReceivedEvent().activateProgress) {
					eNodebSwStatus.getReceivedEvent().activateProgress = printEventIfReceived(eNodebSwStatus,
							NetspanSWEvents.NetspanEvents.ACTIVATE_IN_PROGRESS);
				}
				break;

			case ACTIVATE_COMPLETED:
				if (!eNodebSwStatus.getReceivedEvent().activateCompleted) {
					eNodebSwStatus.getReceivedEvent().activateCompleted = printEventIfReceived(eNodebSwStatus,
							NetspanSWEvents.NetspanEvents.ACTIVATE_COMPLETED);
				}
				break;
		}
	}

	/**
	 * Loop on all the received events from netspan and check it has received.
	 * Print all its details in cas it received.
	 *
	 * @param requiredNetspanEvent - incoming Netspan Event
	 */
	public boolean printEventIfReceived(EnodebSwStatus eNodebSwStatus, NetspanSWEvents.NetspanEvents requiredNetspanEvent) {
		List<EventInfo> allNetspanEvents = AlarmsAndEvents.getInstance().getAllEventsNode(eNodebSwStatus.geteNodeB(), softwareActivateStartTimeInDate, new Date(System.currentTimeMillis()));
		for (EventInfo currentEvent : allNetspanEvents) {
			if (isEventReceived(currentEvent, requiredNetspanEvent)) {
				printEventDetails(currentEvent, requiredNetspanEvent);
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks if the current event is the requested one
	 *
	 * @param requiredNetspanEvent - requiredNetspanEvent
	 * @param currentEvent         - currentEvent in loop
	 * @return - true if it is received
	 */
	private boolean isEventReceived(EventInfo currentEvent, NetspanSWEvents.NetspanEvents requiredNetspanEvent) {
		return currentEvent.getEventInfo().contains(requiredNetspanEvent.toString());
	}

	/**
	 * Print Event Details
	 *
	 * @param currentEvent         - current Event in loop
	 * @param incomingNetspanEvent - incoming Netspan Event
	 */
	private void printEventDetails(EventInfo currentEvent, NetspanSWEvents.NetspanEvents incomingNetspanEvent) {
		GeneralUtils.startLevel(currentEvent.getSourceName() + ": " + incomingNetspanEvent.toString());
		report.report("Event Type: " + currentEvent.getEventType());
		report.report("Source Type: " + currentEvent.getSourceType());
		report.report("Event Info: " + currentEvent.getEventInfo());
		report.report("Received Time: " + currentEvent.getReceivedTime().toString());
		GeneralUtils.stopLevel();
	}
}
package testsNG.PlatformAndSecurity.RebootManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import org.junit.Test;
import EnodeB.EnodeB;
import Netspan.EnbProfiles;
import Netspan.NetspanServer;
import Netspan.API.Enums.HandoverType;
import Netspan.API.Enums.HoControlStateTypes;
import Netspan.API.Enums.X2ControlStateTypes;
import Netspan.NBI_15_5.Lte.AutonomousRebootValues;
import Netspan.Profiles.ManagementParameters;
import Netspan.Profiles.SonParameters;
import Utils.GeneralUtils;
import Utils.Pair;
import Utils.SysObjUtils;
import jsystem.framework.ParameterProperties;
import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;
import testsNG.TestspanTest;
import testsNG.Actions.EnodeBConfig;
import testsNG.Actions.Neighbors;
import testsNG.Actions.Utils.ParallelCommandsThread;

public class Progression extends TestspanTest{
	final int PCI_COLLISION_DETECTION_TIMER = 100*1000;
	private EnodeB dut, neighbor;
	private EnodeBConfig enodeBConfig;
	private ManagementParameters managementParams;
	private NetspanServer nms;
	ParallelCommandsThread syncCommands;
	private List<String> commandList;
	private boolean is3rdPartyNeighborNeeded;
	
	@Override
	public void init() throws Exception {
		enbInTest = new ArrayList<EnodeB>();
		enbInTest.add(dut);
		is3rdPartyNeighborNeeded = false;
		if(neighbor != null){
			enbInTest.add(neighbor);
		}else{
			is3rdPartyNeighborNeeded = true;
		}
		super.init();
		dut.lteCli("logger threshold set client=OAM_MAINT_WIN std_out=0");
		enodeBConfig = EnodeBConfig.getInstance();
		nms = NetspanServer.getInstance();
		managementParams = new ManagementParameters();
		managementParams.maintenanceWindow = true;
	}

	@Override
	public void end(){
		GeneralUtils.startLevel("After test");
		enodeBConfig.setIsOngoingEmergencyCall(dut, false);
		enodeBConfig.setProfile(dut, EnbProfiles.Management_Profile,  dut.defaultNetspanProfiles.getManagement());
		enodeBConfig.setProfile(dut, EnbProfiles.Son_Profile, dut.defaultNetspanProfiles.getSON());
		enodeBConfig.deleteClonedProfiles();
		dut.reboot();
		dut.lteCli("logger threshold set client=OAM_MAINT_WIN std_out=5");
		report.report("Waiting For All Running.");
		dut.waitForAllRunning(EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
		GeneralUtils.stopLevel();
		super.end();
	}
	
	@ParameterProperties(description = "DUT")
	public void setDUT(String dutName) {
		GeneralUtils.printToConsole("Load DUT " + dutName);
		this.dut = (EnodeB) SysObjUtils.getInstnce().initSystemObject(EnodeB.class, false, dutName).get(0);
		GeneralUtils.printToConsole("DUT loaded" + this.dut.getName());
	}
	
	@ParameterProperties(description = "Neighbor (optional - if empty, NMS's 3rd party eNodeB is used).")
	public void setNeighbor(String neighborName) {
		GeneralUtils.printToConsole("Load Neighbor " + neighborName);
		this.neighbor = (EnodeB) SysObjUtils.getInstnce().initSystemObject(EnodeB.class, false, neighborName).get(0);
		GeneralUtils.printToConsole("Neighbor loaded" + this.neighbor.getName());
	}
	
	/***************************************************/
	
	/**
	 * Reboot Manager during AutoPCI Collision.
	 */
	@Test
	@TestProperties(name = "Reboot Manager test during AutoPCI Collision", 
	returnParam = { "IsTestWasSuccessful" }, 
	paramsExclude = {"IsTestWasSuccessful"})
	public void rebootManagerTestDuringAutoPCICollision(){
		syncGeneralCommands(2);
		rebootManager(2);
		stopSyncGeneralCommands();
		if (netspanServer != null && !netspanServer.deleteNeighbor(dut, neighbor)) {
			report.report("Delete Neighbor " + neighbor.getNetspanName() + " with netspan failed", Reporter.WARNING);
		}
		if(is3rdPartyNeighborNeeded){
			Neighbors.getInstance().delete3rdParty(neighbor.getNetspanName());
		}
	}
	
	/**
	 * Reboot Manager test when NMS send reboot request.
	 */
	@Test
	@TestProperties(name = "Reboot Manager test when NMS send reboot request", 
	returnParam = { "IsTestWasSuccessful" }, 
	paramsExclude = {"IsTestWasSuccessful", "Neighbor"})
	public void rebootManagerTestWhenNMSsendRebootRequest(){
		syncGeneralCommands(1);
		rebootManager(1);
		stopSyncGeneralCommands();
	}
	
	public void rebootManager(int mask) {
		String timeForReboot = getCurrentTimePlusDelay(30);
		
		managementParams.maintenanceWindow = true;
		managementParams.timeZone = "UTC";
        managementParams.maintenanceWindowStartTime = timeForReboot;
		managementParams.maintenanceWindowEndTime = getCurrentTimePlusDelay(90);
		managementParams.maxRandomDelayPercent = 2;
		managementParams.autonomousReboot = AutonomousRebootValues.MAINTENANCE_WINDOW;
		
		GeneralUtils.startLevel("Enable Maintenance Window");
		report.report("Set Maintenance Window Time Zone to: " + managementParams.timeZone);
		report.report("Set Maintenance Window Start Time to: " + managementParams.maintenanceWindowStartTime);
		report.report("Set Maintenance Window End Time to: " + managementParams.maintenanceWindowEndTime);
		report.report("Set Maintenance Window Max Random Delay: " + managementParams.maxRandomDelayPercent);
		report.report("Set Maintenance Window Autonomous Reboot: " + managementParams.autonomousReboot);
		enodeBConfig.cloneAndSetManagementProfileViaNetSpan(dut, dut.defaultNetspanProfiles.getManagement(), managementParams);
		//Boolean setDelayReboot = enodeBConfig.setDelayReboot(dut, delayReboot);
		GeneralUtils.stopLevel();
		GeneralUtils.unSafeSleep(10000);
		
		int delayReboot = enodeBConfig.getDelayReboot(dut);
		
		GeneralUtils.startLevel("Check maintenanceWindow set in eNodeB database properly");
		boolean dutMaintenanceWindow = enodeBConfig.getMaintenanceWinMode(dut);
		report.report("maintenanceWinMode = " + dutMaintenanceWindow);
		String dutMainWinStart = enodeBConfig.getMainWinStart(dut);
		report.report("mainWinStart = " + dutMainWinStart);
		int dutMainWinDuration = enodeBConfig.getMainWinDuration(dut);
		report.report("mainWinDuration = " + dutMainWinDuration);
		int dutMainWinDelayReboot = enodeBConfig.getDelayReboot(dut);
		report.report("delayReboot = " + dutMainWinDelayReboot);
		GeneralUtils.stopLevel();
		if(dutMaintenanceWindow && dutMainWinStart.substring(0, 5).equals(managementParams.maintenanceWindowStartTime)){
			
			int rebootRequiredValue = enodeBConfig.isRebootRequiredMaintenanceWinStatus(dut);
			if(rebootRequiredValue > 0){
				report.report("Reboot is required - rebooting eNB for initializing rebootRequired value");
				enodeBConfig.setProfile(dut, EnbProfiles.Management_Profile, dut.defaultNetspanProfiles.getManagement());
				dut.reboot();
				dut.waitForAllRunning(EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
				timeForReboot = getCurrentTimePlusDelay(20);
				managementParams.maintenanceWindowStartTime = timeForReboot;
				enodeBConfig.cloneAndSetManagementProfileViaNetSpan(dut, dut.defaultNetspanProfiles.getManagement(), managementParams);
				rebootRequiredValue = enodeBConfig.isRebootRequiredMaintenanceWinStatus(dut);
				if(rebootRequiredValue > 0){report.report("rebootRequired > 0", Reporter.WARNING);}
			}
			
			if(mask == 2){
				casuePciCollisionByAutoPci();
			}else if(mask == 1){
				causeForRebootRequiredByNetspan();
			}
			GeneralUtils.unSafeSleep(20 * 1000);
			rebootRequiredValue = enodeBConfig.isRebootRequiredMaintenanceWinStatus(dut);
			if(rebootRequiredValue == GeneralUtils.ERROR_VALUE || (rebootRequiredValue & mask) != mask){
				report.report("maintenanceWinCellStatus - RebootRequired flag is OFF.", Reporter.FAIL);
				reason = "maintenanceWinCellStatus - RebootRequired flag is OFF.";
				return;
			}else{
				report.report("maintenanceWinCellStatus - RebootRequierd!");
			}
			
			String currentTime = getCurrentTimePlusDelay(0);
			String currentTimePlus5Minutes = getCurrentTimePlusDelay(5);
			String maintenanceWindowStartTime = getCurrentTimePlusDelay(7);
			String endOfDelayRebootWindow = getCurrentTimePlusDelay(12);
			String endOfMaintenanceWindow = getCurrentTimePlusDelay(67);
			managementParams.maintenanceWindowStartTime = maintenanceWindowStartTime;
			managementParams.maintenanceWindowEndTime = endOfMaintenanceWindow;
			report.report("Set Maintenance Window Start Time to:" + maintenanceWindowStartTime);
			managementParams.setProfileName(managementParams.getProfileName() + "_cloned2");
			enodeBConfig.cloneAndSetManagementProfileViaNetSpan(dut, dut.defaultNetspanProfiles.getManagement(), managementParams);
			report.report("Waiting 5 minutes before Maintenance Window Start Time for validating reboot does NOT perform before Maintenance Window started.");
	        int timer = 84;
	        while(!currentTimePlus5Minutes.equals(currentTime)){
				GeneralUtils.unSafeSleep(5000);
				if(!dut.isInOperationalStatus()){
					report.report("eNodeB performed reboot before Maintenance Window started!", Reporter.FAIL);
					reason = "eNodeB performed reboot before Maintenance Window started!";
					return;
				}
				currentTime = getCurrentTimePlusDelay(0);
				timer--;
				if(timer < 0){break;}
			}
			
			report.report("Setting isOngoingEmergencyCall to true");
			if(enodeBConfig.setIsOngoingEmergencyCall(dut, true)){
				
		        report.report("Waiting for Maintenance Window Start Time.");
		        currentTime = getCurrentTimePlusDelay(0);
		        timer = 84;
		        while(!maintenanceWindowStartTime.equals(currentTime)){
					GeneralUtils.unSafeSleep(5000);
					if(!dut.isInOperationalStatus()){
						report.report("eNodeB performed reboot before Maintenance Window started!", Reporter.FAIL);
						reason = "eNodeB performed reboot before Maintenance Window started!";
						return;
					}
					currentTime = getCurrentTimePlusDelay(0);
					timer--;
					if(timer < 0){break;}
				}
		        report.report("Maintenance Window start now.");
		        
		        if(mask == 2){
		        	report.report("Waiting PCI COLLISION DETECTION TIMER will expire (100 Sec) - RebootReady notice supposed to send to RebootManager after it expired");
					GeneralUtils.unSafeSleep(PCI_COLLISION_DETECTION_TIMER + 10000);
		        }else if(mask == 1){
		        	report.report("RebootReady notice supposed to send to RebootManager from NMS.");
		        	GeneralUtils.unSafeSleep(10000);
		        }
		        
		        report.report("Checking eNodeB does not perform reboot during DelayReboot window while OngoingEmergencyCall=true (DelayReboot="+dutMainWinDelayReboot+").");
		        timer = 60;
		        while(!endOfDelayRebootWindow.equals(currentTime)){
					GeneralUtils.unSafeSleep(5000);
					if(!dut.isInOperationalStatus()){
						report.report("eNodeB performed reboot during Emergency Call!", Reporter.FAIL);
						reason = "eNodeB performed reboot during Emergency Call!";
						return;
					}
					currentTime = getCurrentTimePlusDelay(0);
					timer--;
					if(timer < 0){break;}
				}
				
		        report.report("Setting isOngoingEmergencyCall to false");
		        dut.setExpectBooting(true);
				if(enodeBConfig.setIsOngoingEmergencyCall(dut, false)){
					report.report("Validating eNodeB perform reboot during Maintenance Window - End Of Maintenance Window: " + endOfMaintenanceWindow);
					if(mask == 2){
						//waiting 5 minutes (60*5 sec) - AutoPCI ask for Reboot
						timer = 720;
					}else if(mask == 1){
						//waiting one hour (720*5 sec) - NMS ask for Reboot
						timer = 720;
					}
					boolean rebootReadyFlag = true;
					GeneralUtils.startLevel("Waiting For Reboot.");
					while(!endOfMaintenanceWindow.equals(currentTime)){
						GeneralUtils.unSafeSleep(5000);
						report.report("Waiting For Reboot.");
						
						int isRebootReady = enodeBConfig.isRebootReadyMaintenanceWinStatus(dut);
				        if(rebootReadyFlag && isRebootReady != GeneralUtils.ERROR_VALUE && ((isRebootReady & mask) == mask)){
				        	GeneralUtils.stopLevel();
							report.report("maintenanceWinCellStatus - RebootReady!");
							GeneralUtils.startLevel("Waiting For Reboot.");
							rebootReadyFlag = false;
						}
						
						if(!dut.isInOperationalStatus()){
							GeneralUtils.stopLevel();
							report.report("eNodeB performed reboot!");
							dut.waitForSnmpAvailable(EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
							return;
						}
						currentTime = getCurrentTimePlusDelay(0);
						timer--;
						if(timer < 0){break;}
					}
					GeneralUtils.stopLevel();
					report.report("eNodeB does not performed reboot during Maintenance Window!", Reporter.FAIL);
					reason = "eNodeB does not performed reboot during Maintenance Window!";
					return;
				}else{
					report.report("Failed to set isOngoingEmergencyCall to false", Reporter.FAIL);
					reason = "Failed to set isOngoingEmergencyCall to false";
					return;
				}
				
			}else{
				report.report("Failed to set isOngoingEmergencyCall to true", Reporter.FAIL);
				reason = "Failed to set isOngoingEmergencyCall to true";
				return;
			}
			
		}else{
			report.report("Failed to set Maintenance Window Table", Reporter.FAIL);
			reason = "Failed to set Maintenance Window Table";
			report.report("maintenanceWinMode equals " + dutMaintenanceWindow);
			report.report("mainWinStart equals " + dutMainWinStart);
		}
	}

	private void causeForRebootRequiredByNetspan(){
		GeneralUtils.startLevel("Causing Netspan to declare Reboot Requierd");
		enodeBConfig.setNmsRebootRequired(dut, true);
		GeneralUtils.unSafeSleep(5000);
		GeneralUtils.stopLevel();
	}
	
	private void casuePciCollisionByAutoPci(){
		GeneralUtils.startLevel("Causing PCI Collision.");
		
		report.report("Deleting all neighbors.");
		Neighbors ngh = Neighbors.getInstance();
		if(!ngh.deleteAllNeighbors(dut)){report.report("Faild to delete all neighbors.", Reporter.WARNING);}
		
		SonParameters sp = new SonParameters();
		sp.setSonCommissioning(true);
		sp.setAutoPCIEnabled(true);
		sp.setPciCollisionHandling(SonParameters.PciHandling.Deferred);
		sp.setPciConfusionHandling(SonParameters.PciHandling.Deferred);
		sp.setPciPolicyViolationHandling(SonParameters.PciHandling.Deferred);
		List<Pair<Integer, Integer>> ranges = new ArrayList<Pair<Integer, Integer>>();
		int dutPci = dut.getPci();
		int neighborPci = 0;
		if(is3rdPartyNeighborNeeded){
			neighbor = ngh.adding3rdPartyNeighbor(dut, dut.getName()+"_3rdPartyNeighbor", "123.45.67." + dut.getPci(),dut.getPci()+1, 123400 + (dut.getPci() + 1), dut.getEarfcn());
			if(neighbor == null){
				report.report("Could not create 3rd party neighbor",Reporter.FAIL);
				GeneralUtils.stopLevel();
				return;
			}
			neighborPci = dut.getPci()+1;
		}else{
			neighbor.setServiceState(GeneralUtils.CellIndex.ENB, 1);
			neighborPci = neighbor.getPci();
		}
		Pair<Integer, Integer> p = new Pair<Integer, Integer>(neighborPci, neighborPci);
		ranges.add(p);
		sp.setRangesList(ranges);
		
		GeneralUtils.startLevel("Enabling AutoPCI for causing collision");
		report.report("set Son Commissioning to true.");
		report.report("set AutoPCIEnabled to true.");
		report.report("set Pci Collision Handling to Deferred.");
		report.report("set Pci Confusion Handling to Deferred.");
		report.report("set Pci Policy Violation Handling to Deferred.");
		report.report("Ranges: PCI Start = PCI End = " +  neighborPci);
		GeneralUtils.stopLevel();
		dut.setExpectBooting(true);
		enodeBConfig.cloneAndSetSonProfileViaNetspan(dut, dut.getDefaultNetspanProfiles().getSON(), sp);
		int timeoutCounter = 0;
		while(dut.isInOperationalStatus() && timeoutCounter < 60){
			GeneralUtils.unSafeSleep(2000);
			timeoutCounter++;
		}
		report.report("WAIT FOR ALL RUNNING.");
		dut.waitForAllRunningAndInService(EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
		if(enodeBConfig.getPciFromAutoPciCell(dut) != neighborPci){report.report("PCI did not update to " + neighborPci, Reporter.WARNING);}
		
		int maxPci = 0, minPci = 0;
		if(dutPci < neighborPci){
			maxPci = neighborPci;
			minPci = dutPci;
		}else{
			maxPci = dutPci;
			minPci = neighborPci;
		}
		report.report("Set PCI Range between " + minPci + " to " + maxPci);
		ranges.clear();
		p = new Pair<Integer, Integer>(minPci, maxPci); 
		ranges.add(p);
		sp.setRangesList(ranges);
		enodeBConfig.changeSonProfile(dut, sp);
		GeneralUtils.unSafeSleep(10*1000);
		
		report.report("Adding neighbor with the same PCI to cause Collision, PCI = " + neighborPci);

		if(!ngh.addNeighbor(dut, neighbor, HoControlStateTypes.ALLOWED, X2ControlStateTypes.AUTOMATIC, HandoverType.TRIGGER_X_2, true, "0")){report.report("Faild to add neighbor.", Reporter.WARNING);}
		
		GeneralUtils.stopLevel();
	}
	
	/*return HH:MM*/
	private String getCurrentTimePlusDelay(int delay){
		TimeZone timeZone = TimeZone.getTimeZone("UTC");
		Calendar cal = Calendar.getInstance(timeZone);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int minute = cal.get(Calendar.MINUTE);
		minute += delay; //delay for reboot
		if(minute > 59){
			hour += (minute / 60);
			hour %= 24;
			minute %= 60;
		}
		String time = "";
		if(hour < 10){
			time += "0";
		}
		time += hour;
		time += ":";
		if(minute < 10){
			time += "0";
		}
		time += minute;
		
		return time;
	}
	
	
	/**
	 * @author Shahaf Shuhamy uses String [] command list and starting another
	 *         Thread to commands in cli and put them in another File located in
	 *         root folder
	 */
	private void syncGeneralCommands(int mask) {
		report.report("Starting parallel commands");
		commandList = new ArrayList<String>();
		commandList.add("db get maintenanceWinCfg");
		commandList.add("db get maintenanceWinCellStatus");
		commandList.add("db get maintenanceWinCellCfg");
		commandList.add("db get PTPStatus");
		if(mask == 2){
			commandList.add("db get AutoPciEnb");
			commandList.add("db get AutoPciCell");
			commandList.add("db get nghList");
		}
		try {
			syncCommands = new ParallelCommandsThread(commandList, dut, null, null);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		syncCommands.start();
		GeneralUtils.unSafeSleep(20*1000);
	}
	
	private void stopSyncGeneralCommands(){
		syncCommands.stopCommands();
		syncCommands.moveFileToReporterAndAddLink();
	}
	
}

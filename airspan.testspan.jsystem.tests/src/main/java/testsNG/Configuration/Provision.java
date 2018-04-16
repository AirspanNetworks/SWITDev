package testsNG.Configuration;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Test;

import EnodeB.EnodeB;
import Netspan.NetspanServer;
import Netspan.Profiles.INetspanProfile;
import Netspan.Profiles.RadioParameters;
import Utils.GeneralUtils;
import Utils.SysObjUtils;
import jsystem.framework.ParameterProperties;
import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;
import testsNG.TestspanTest;
import testsNG.Actions.EnodeBConfig;

public class Provision extends TestspanTest {
	RadioParameters firstRadio = null;
	private ArrayList<EnodeB> duts;
	private EnodeB dut;
	private int PCI;
	private int cellId = 1;
	private NetspanServer netspanServer;
	private boolean performReboot = true;
	private EnodeBConfig enbConfig;
	boolean status = true;

	HashMap<Integer, ArrayList<INetspanProfile>> profileMap = new HashMap<Integer, ArrayList<INetspanProfile>>();
	// this map is used to save different values like CA configuration
	HashMap<String, String> differentMapValues = new HashMap<String, String>();
	RadioParameters currentRadio = new RadioParameters();

	@Override
	public void init() throws Exception {
		enbInTest = new ArrayList<EnodeB>();
		netspanServer = NetspanServer.getInstance();
		enbConfig = EnodeBConfig.getInstance();
		if(dut != null)
			enbInTest.add(dut);
		else
			for (EnodeB dut : duts)
				enbInTest.add(dut);
		super.init();
	}

	@Test
	@TestProperties(name = "change Pci", paramsExclude = { "DUTs", "IsTestWasSuccessful" })
	public void changePci() {
		RadioParameters radioParams = new RadioParameters();
		radioParams.setEarfcn(PCI);
		report.report(String.format("%s - Trying to set Pci to %s ", dut.getNetspanName(), PCI));
		status = enbConfig.changeEnbCellPci(dut, PCI);
		if (!status) {
			report.report("Update Failed", Reporter.FAIL);
			return;
		}
		else
			report.step(String.format("%s - Succeeded to set PCI %s ", dut.getNetspanName(), PCI));
		if (performReboot) {
			status = rebootAndWaitForAllrunning();
			if (status) {
				report.report("Enodeb Reached all running and in service successfully");
			}
		}
	}

	@Test
	@TestProperties(name = "Re-Provision Node", paramsExclude = { "DUT", "PCI", "IsTestWasSuccessful" })
	public void reProvision() {
		for (EnodeB dut : duts) {
			report.report(String.format("%s - Trying to perform Re-Provision", dut.getNetspanName()));
			status = netspanServer.performReProvision(dut.getNetspanName());
			if (!status) {
				report.report("Re-Provision Failed", Reporter.FAIL);
				return;
			}
			else
				report.step(String.format("%s - Succeeded to perform Re-Provision", dut.getNetspanName()));
		}
		if (performReboot) {
			status = rebootAndWaitForAllrunning();
			if (status) {
				report.report("Enodeb Reached all running and in service successfully");
			}
		}

	}
	
	@Test
	@TestProperties(name = "Set Default Netspan Profiles", paramsExclude = { "DUT", "PCI", "IsTestWasSuccessful" })
	public void setDefaultNetspanProfiles() {
		for (EnodeB dut : duts) {
			report.report(String.format("%s - Trying to set default Netspan profiles", dut.getNetspanName()));
			status = netspanServer.checkAndSetDefaultProfiles(dut,true);
			if (!status) {
				report.report("Setting default Netspan profiles Failed", Reporter.FAIL);
				return;
			}
			else
				report.step(String.format("%s - Succeeded to set default Netspan profiles", dut.getNetspanName()));
		}
		if (performReboot) {
			status = rebootAndWaitForAllrunning();
			if (status) {
				report.report("Enodeb Reached all running and in service successfully");
			}
		}
	}

	/**
	 * checking if the list of netspan profiles according to the cell is filled
	 * with the INetspan instance.
	 * 
	 * @param cellId
	 * @param profile
	 * @return false if there is an instance of the INetspan profile in the map
	 *         equals to the one sent as parameter.
	 */
	public boolean needToSave(int cellId, INetspanProfile profile) {
		if (profileMap.get(cellId) != null) {
			ArrayList<INetspanProfile> netspanProfilesInCell = new ArrayList<INetspanProfile>();
			netspanProfilesInCell = profileMap.get(cellId);
			if (netspanProfilesInCell.size() == 0) {
				return true;
			} else {
				for (INetspanProfile netspanProfile : netspanProfilesInCell) {
					if (netspanProfile.getClass().isAssignableFrom(profile.getClass())) {
						return false;
					}
				}
			}
		}
		return true;
	}

	/**
	 * saving radio only if the radio is null.
	 * 
	 * @author Shahaf Shuhamy
	 * @param bw
	 * @param fc
	 * @param cellId
	 */
	public void saveRadio(RadioParameters rp, int cellId) {
		// get radio profile from netstpan
		GeneralUtils.printToConsole("\n ------ System Print : BW : " + rp.getBandwidth() + " , FC : " + rp.getFrameConfig()
				+ " for cell id: " + cellId + " for Radio profile name: " + rp.getProfileName() + " ------");
		// new list
		ArrayList<INetspanProfile> profileList = new ArrayList<INetspanProfile>();
		GeneralUtils.printToConsole("Radio profile Revieced for cell " + cellId);
		RadioParameters radioToInsert = new RadioParameters();
		radioToInsert.setProfileName(rp.getProfileName());
		radioToInsert.setBandwidth(rp.getBandwidth());
		if(rp.getFrameConfig()!=null){
			radioToInsert.setFrameConfig(rp.getFrameConfig());
		}
		// checking for the first time inserting profile.
		if (firstRadio == null) {
			firstRadio = new RadioParameters();
			firstRadio.setProfileName(rp.getProfileName());
			firstRadio.setBandwidth(rp.getBandwidth());
			if(rp.getFrameConfig()!=null){
				firstRadio.setFrameConfig(rp.getFrameConfig());				
			}
			GeneralUtils.printToConsole("first radio profile in the map");
			// checking if there is a list with other profiles
			if (profileMap.get(cellId) != null) {
				GeneralUtils.printToConsole("there is a list in the current cell ID - adding radio profile");
				profileMap.get(cellId).add(radioToInsert);
			} else {
				// if there is no list, adding new one with the radio profile.
				GeneralUtils.printToConsole("creating a list to keep profiles in");
				profileList.add(radioToInsert);
				profileMap.put(cellId, profileList);
			}
			GeneralUtils.printToConsole("setting radio default pointer so there will be an indicator to it's not empty");
			GeneralUtils.printToConsole("Radio after initating: BW: " + radioToInsert.getBandwidth() + " / FC: "
					+ radioToInsert.getFrameConfig());

			// radio isn't null - but there can be a different cellId to radio
			// Profile.
		} else {
			if (profileMap.get(cellId) == null) {
				GeneralUtils.printToConsole("there is no list for the cell id: " + cellId + ", creating new list in the map");
				profileList.add(radioToInsert);
				profileMap.put(cellId, profileList);
			}
		}
	}

	/**
	 * @return all radio profiles in a list
	 * @throws Exception
	 */
	public HashMap<Integer, INetspanProfile> loadRadio() throws Exception {
		HashMap<Integer, INetspanProfile> radioProfiles = new HashMap<Integer, INetspanProfile>();
		GeneralUtils.printToConsole("Debug Print");
		for (Integer cellNumber : profileMap.keySet()) {
			for (INetspanProfile netspanProfile : profileMap.get(cellNumber)) {
				if (netspanProfile instanceof RadioParameters) {
					GeneralUtils.printToConsole(
							"Cell number " + cellNumber + " , Parameters : " + netspanProfile.getProfileName());
					radioProfiles.put(cellNumber, netspanProfile);
				}
			}
		}
		
		//remove loaded radio profiles from map - reverting.
		for(Integer cellNumber : radioProfiles.keySet()){
			profileMap.remove(cellNumber);
		}

		if (radioProfiles.isEmpty()) {
			return null;
		} else {
			return radioProfiles;
		}

	}

	public void saveCAConfig(EnodeB dut) {
		GeneralUtils.printToConsole("saving mib / value current configuration to structure:");

		String value = dut.getTddAckMode();
		differentMapValues.put("asLteStkCellDedCfgPhyTddAckNackFeedbackMode", value);
		GeneralUtils.printToConsole("saving asLteStkCellDedCfgPhyTddAckNackFeedbackMode / " + value + " to map");

		value = dut.getParchZeroCorrrelZone();
		differentMapValues.put("asLteStkCellSib2CfgPrachZeroCorrrelZoneCfg", value);
		GeneralUtils.printToConsole("saving asLteStkCellSib2CfgPrachZeroCorrrelZoneCfg / " + value + " to map");

		value = dut.getCAMode();
		differentMapValues.put("asLteStkStackCfgCaMode", value);
		GeneralUtils.printToConsole("saving asLteStkStackCfgCaMode / " + value + " to map");
	}

	/**
	 * return a map with saved configurations as map of key-value pair as
	 * Strings
	 * 
	 * @param dut
	 * @return
	 */
	public HashMap<String, String> loadCAConfiguration() throws Exception {
		if (differentMapValues != null) {
			return differentMapValues;
		} else {
			throw new Exception("result map is empty");
		}
	}

	private Boolean rebootAndWaitForAllrunning() {
		int inserviceTime = 0;

		try {
			for (EnodeB dut : duts) {

				report.startLevel("Rebooting " + dut.getName());
				status = dut.reboot();
				if (!status) {
					report.report("Reboot Failed", Reporter.FAIL);
					report.stopLevel();
					return false;
				}
				report.stopLevel();
			}

			GeneralUtils.unSafeSleep(30 * 1000);

			for (EnodeB dut : duts) {
				status = dut.waitForAllRunningAndInService(EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
				inserviceTime = (int) (EnodeB.WAIT_FOR_ALL_RUNNING_TIME / 1000 / 60);
				if (!status)
					report.report("Enodeb didnt reach all running and in service during " + inserviceTime + " minutes",
							Reporter.WARNING);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return status;
	}

	@ParameterProperties(description = "Perform reboot to apply changes.")
	public void setPerformReboot(boolean performReboot) {
		this.performReboot = performReboot;
	}

	@ParameterProperties(description = "Pci")
	public void setPCI(String pci) {
		this.PCI = Integer.valueOf(pci);
	}

	@ParameterProperties(description = "Name of All Enb comma seperated e.g enb1,enb2")
	public void setDUTs(String dut) {
		this.duts = (ArrayList<EnodeB>) SysObjUtils.getInstnce().initSystemObject(EnodeB.class, false, dut.split(","));
	}

	@ParameterProperties(description = "Name of Enb")
	public void setDUT(String dut) {
		this.dut = (EnodeB) SysObjUtils.getInstnce().initSystemObject(EnodeB.class, false, dut).get(0);
		GeneralUtils.printToConsole("Node " + this.dut.getNetspanName() + " has been loaded");
	}

	public ArrayList<EnodeB> getDuts() {
		return duts;
	}
}

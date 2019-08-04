package testsNG.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import org.junit.Test;
import EnodeB.EnodeB;
import EnodeB.EnodeBChannelBandwidth;
import Netspan.EnbProfiles;
import Netspan.NetspanServer;
import Netspan.Profiles.INetspanProfile;
import Netspan.Profiles.MultiCellParameters;
import Netspan.Profiles.RadioParameters;
import Utils.GeneralUtils;
import Utils.SysObjUtils;
import jsystem.framework.ParameterProperties;
import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;
import testsNG.TestspanTest;
import testsNG.Actions.EnodeBConfig;

public class Radio extends TestspanTest {
	private EnodeB dut;
	private EnodeBConfig enbConfig;
	static private Provision provision = new Provision();
	static private HashMap<String,String> nodeNameMultiCellProfMap= new HashMap<String,String>();
	static private HashMap<EnodeB,ArrayList<INetspanProfile>> clonedProfilesSave = new HashMap<>();
	private int BW;
	private int FC;
	private int EARFCN;
	private ArrayList<Integer> cellIds = null;
	private NetspanServer netspanServer;
	private boolean performReboot = true;
	private boolean fddMode = false;
	private Boolean caTest = null;
	private Double frequencyAdapter15 = 15.0;
	private Double frequencyAdapter20 = 19.8;
	private Integer radioFreqInCaTest=0; 

	@Override
	public void init() throws Exception {
		enbInTest = new ArrayList<EnodeB>();
		netspanServer = NetspanServer.getInstance();
		enbInTest.add(dut);
		enbConfig = EnodeBConfig.getInstance();
		super.init();
		fddMode = enbConfig.isDuplexFdd(dut);
	}

	/**
	 * enable CA with SNMP.
	 * 
	 * @author Shahaf Shuhamy checking which UE is category 6
	 */
	@Test
	@TestProperties(name = "Enable Carrier Aggregation", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"performReboot", "EARFCN", "IsTestWasSuccessful", "BW", "FC", "cellId" })
	public void enable_Carrier_Aggregation() {
		
		String multiCellProfile = dut.getDefaultNetspanProfiles().getMultiCell();
		GeneralUtils.printToConsole("multi Cell profile from SUT: "+multiCellProfile);
		//map is for multiple nodes compatibility
		if(!saveProfileToMap(dut.getNetspanName(),multiCellProfile)){
			report.report("Ending Action since multi cell profile is different for this node!");
			return;
		}
		
		MultiCellParameters multiCellParams = new MultiCellParameters();
		multiCellParams.setCarrierAggMode(true);
		if(!enbConfig.cloneAndSetProfileViaNetSpan(dut, multiCellProfile, multiCellParams)){
			report.report("trying to change SNMP settings to change CA");
					if(enableCAWithSNMP()){
							report.report("successfully set SNMP CA parameters.");
					}else{
							report.report("did not manage to set all CA parameters with SNMP.", Reporter.FAIL);
					}
		}
		report.report("Waiting 15 seconds");
		GeneralUtils.unSafeSleep(15*1000);
		rebootAndWaitForAllrunning();
	}

	private boolean saveProfileToMap(String nodeName,String multiCellProfileName){
		boolean result = true;
		GeneralUtils.printToConsole("trying to save profile with the name: "+multiCellProfileName+" to node: "+nodeName);
		if(nodeNameMultiCellProfMap.containsKey(nodeName)){
			report.report("there is already multi cell profile for this node!");
			result = false;
		}else{
			report.report("save multi cell profile "+multiCellProfileName+" for node name "+nodeName);
			nodeNameMultiCellProfMap.put(nodeName, multiCellProfileName);
		}
		return result;
	}
	
	/**
	 * enabling CA configurations with SNMP and Saving SNMP for CA
	 * configurations in Provision Class.
	 * 
	 * @author Shahaf Shuhamy
	 * @return
	 */
	private boolean enableCAWithSNMP() {

		boolean result = true;
		provision.saveCAConfig(dut);
		report.report("saved CA configuration in provision class.");

		report.report("Setting prach zero mode with the value 8");
		GeneralUtils.printToConsole("asLteStkCellSib2CfgPrachZeroCorrrelZoneCfg MIB set to 8");
		result = dut.setPrachZeroCorrelZone(8);
		
		report.report("Setting CA mode with the value 2");
		result = dut.setCAMode(2) && result;
		GeneralUtils.printToConsole("asLteStkStackCfgCaMode MIB set to 2");
		
		report.report("Setting tdd ack with the value 1");
		result = dut.setTddAckMode(1) && result;
		GeneralUtils.printToConsole("asLteStkCellDedCfgPhyTddAckNackFeedbackMode MIB set to 1");

		return result;
	}

	/**
	 * loading CA configuration and updateing with SNMP.
	 */
	@Test
	@TestProperties(name = "Disable Carrier Aggregation", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"performReboot", "EARFCN", "IsTestWasSuccessful", "BW", "FC", "cellId" })
	public void disable_Carrier_Aggregation() {
		
		String multiCellProfileFromMap = loadMultiCellFromMapForNode(dut.getNetspanName());
		String sutMultiCell = dut.getDefaultNetspanProfiles().getMultiCell();

		report.report("trying to revert to profile : "+multiCellProfileFromMap);
		if(multiCellProfileFromMap == null){
			report.report("there is no multiCell profile saved for node: "+dut.getNetspanName());
			return;
		}
		
		if(!multiCellProfileFromMap.equals(sutMultiCell)){
			GeneralUtils.printToConsole("SUT Multi-Cell parameter is not equal to Multi-Cell profile From Netspan");
		}
		
		if(!enbConfig.setProfile(dut, EnbProfiles.MultiCell_Profile, multiCellProfileFromMap)){
		report.report("Could not set multi Cell profile to Netspan - Fall back to SNMP");		
		try {
			HashMap<String, String> caValuesMap = provision.loadCAConfiguration();
			for (String mibNameInMap : caValuesMap.keySet()) {
				String mapValue = caValuesMap.get(mibNameInMap);
				switch (mibNameInMap) {
				case "asLteStkCellDedCfgPhyTddAckNackFeedbackMode": {
					GeneralUtils.printToConsole("setting SNMP to tddAckNackFeedbackMode with the value " + mapValue);
					if (!dut.setTddAckMode(Integer.valueOf(mapValue))) {
						report.report("couldn't set tdd Ack Nack feedback with SNMP");
					}
					break;
				}
				case "asLteStkCellSib2CfgPrachZeroCorrrelZoneCfg": {
					GeneralUtils.printToConsole("setting SNMP to Prach Zero Correl Zone with the value " + mapValue);
					if (!dut.setPrachZeroCorrelZone(Integer.valueOf(mapValue))) {
						report.report("couldn't set Prach Zero Correl Zone with SNMP");
					}
					break;
				}
				case "asLteStkStackCfgCaMode": {
					GeneralUtils.printToConsole("setting SNMP to Ca-mode with the value " + mapValue);
					if (!dut.setCAMode(Integer.valueOf(mapValue))) {
						report.report("couldn't set CA Mode with SNMP");
					}
					break;
				}
				default: {
					GeneralUtils.printToConsole("value in Map is not in CA Configuration parameters : " + mibNameInMap);
					break;
					}
				}
			}

			} catch (Exception e) {
				report.report("loading CA configurations encounter a problem : " + e.getMessage());
			}
		}else{
			MultiCellParameters mcp = new MultiCellParameters();
			netspanServer.getRawNetspanGetProfileResponse(dut, multiCellProfileFromMap, mcp);
		}
		nodeNameMultiCellProfMap.remove(dut.getNetspanName());
		report.report("Waiting 15 seconds");
		GeneralUtils.unSafeSleep(15*1000);
		rebootAndWaitForAllrunning();
	}

	private String loadMultiCellFromMapForNode(String netspanName) {
		if(nodeNameMultiCellProfMap.containsKey(netspanName)){
			return nodeNameMultiCellProfMap.get(netspanName);
		}
		return null;
	}

	/**
	 * if its the first change - getting node parameters from snmp and saving
	 * them and updating radio profile either way.
	 * 
	 * @author Shahaf Shuhamy
	 */
	@Test
	@TestProperties(name = "change radio Configuration", returnParam = { "BW", "FC", "cellId" }, paramsExclude = {
			"performReboot", "EARFCN", "IsTestWasSuccessful" })
	public void change_Radio_Configuration() {
	
		boolean status = true;
		
		if(fddMode){
			setFC("0");
		}
		
		report.report("Trying to set BandWidth = " + this.BW + ", FrameConfig = " + this.FC + " And Earfcn = "
				+ this.EARFCN + " to node : " + dut.getNetspanName());
		if (cellIds == null) {
			report.report("No Values in Cells Place in Jsystem");
			return;
		}
		if (cellIds.isEmpty()) {
			multiCellHandle(null, this.BW, this.FC);
		} else
			multiCellHandle(cellIds, this.BW, this.FC);

		if (!status) {
			report.report("Update Failed", Reporter.FAIL);
			return;
		}
		GeneralUtils.unSafeSleep(20*1000);
		snmpVerification(this.BW,this.FC);
		status = rebootAndWaitForAllrunning();
		snmpVerification(this.BW,this.FC);
		
		if (status) {
			report.report("Enodeb Reached all running and in service succefully");
		}
	}

	@Test
	@TestProperties(name = "Configuration - Change earfcn", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"BW", "FC", "IsTestWasSuccessful" })
	public void changeEarfcn() {
		boolean status = true;
		RadioParameters radioParams = new RadioParameters();
		radioParams.setEarfcn(EARFCN);
		report.report("Trying to set Earfcn = " + EARFCN + " to node : " + dut.getNetspanName());
		if (cellIds == null) {
			report.report("No Values in Cells Place in Jsystem");
			return;
		}
		if (cellIds.isEmpty())
			cellIds.add(1);
		status = netspanServer.updateRadioProfile(dut, radioParams);
		if (!status) {
			report.report("Update Failed", Reporter.FAIL);
			return;
		}
		GeneralUtils.unSafeSleep(10*1000);
		report.reportHtml("db get cellcfg", dut.lteCli("db get cellcfg"), true);
		if (performReboot) {
			status = rebootAndWaitForAllrunning();
			if (status) {
				report.report("Enodeb Reached all running and in service succefully");
			}
		}
	}

	private void removeClonedProfiles(EnodeB node) {
		ArrayList<INetspanProfile> profilesToDelete = new ArrayList<>();
		
		profilesToDelete = clonedProfilesSave.get(node);
		//delete profiles and remove them from main list.
 		for (INetspanProfile profileToRemove : profilesToDelete) {
			report.report("trying to delete profile with the name : " + profileToRemove.getProfileName() + " from netspan");
			if (netspanServer.deleteEnbProfile(profileToRemove.getProfileName(), profileToRemove.getType())) {
				report.report("deleted profile successfully!");
			}
		}
 		
 		if(clonedProfilesSave.get(node).size() == 0) {
 			clonedProfilesSave.remove(node);
 		}

	}

	private void multiCellHandle(ArrayList<Integer> cells, int localBW, int localFC) {

		HashMap<Integer, INetspanProfile> clonedProfiles = new HashMap<Integer, INetspanProfile>();
		ArrayList<Integer> cellsIds = new ArrayList<Integer>();
		String localFcStr = String.valueOf(localFC);
		String localBwStr = String.valueOf(localBW);
		final int numberOfCells;
		if (cells != null) {
			// [1,2,5,6,8]
			numberOfCells = cells.size();
			cellsIds = cells;
		} else {
			// *
			numberOfCells = enbConfig.getNumberOfActiveCells(dut);
			for (int i = 1; i <= numberOfCells; i++) {
				cellsIds.add(i);
			}
		}
		this.cellIds = cellsIds;
		// for all cells in node
		for (Integer cell : cellsIds) {
			GeneralUtils.startLevel("Handle Cell : "+cell);
			RadioParameters clonedProfile = new RadioParameters();
			dut.setCellContextNumber(cell);
			RadioParameters currentRadioProf = enbConfig.getRadioProfile(dut);
			// save radio per cell
			if (provision.needToSave(dut,cell, currentRadioProf)) {
				saveRadio(currentRadioProf, cell);
			}
			// set test properties
			String newProfileName = currentRadioProf.getProfileName() + GeneralUtils.getPrefixAutomation();
			if (newProfileName.length() > 50) {
				String tempName = currentRadioProf.getProfileName();
				String tempSub = tempName.substring(0, tempName.length() - 5);
				newProfileName = tempSub + GeneralUtils.getPrefixAutomation();
			}
			clonedProfile.setProfileName(newProfileName);
			if (BW != 0)
				clonedProfile.setBandwidth(localBwStr);
			if (FC != 0)
				clonedProfile.setFrameConfig(localFcStr);
			if (EARFCN != 0)
				clonedProfile.setEarfcn(EARFCN);
			clonedProfile = handleRadioInCaseOfCA(clonedProfile,currentRadioProf,cell);
			// local saving for next loop
			clonedProfiles.put(cell, clonedProfile);
			// save profile to delete in the end.
			addClonedProfilesToSavingMap(dut,clonedProfile);
			// clone it in Netspan
			netspanServer.cloneRadioProfile(dut, currentRadioProf.getProfileName(), clonedProfile);
			GeneralUtils.startLevel("Cell " + cell + " Raw soap XML: ");
				netspanServer.getRawNetspanGetProfileResponse(dut, clonedProfile.getProfileName(), clonedProfile);
			GeneralUtils.stopLevel();
			
			GeneralUtils.stopLevel();
		}

		enbConfig.setNodeConfiguration(dut, clonedProfiles);
		
	}

	private void addClonedProfilesToSavingMap(EnodeB node,RadioParameters clonedProfile) {
		ArrayList<INetspanProfile> profilesList = new ArrayList<>();
		if(clonedProfilesSave.get(node) == null) {
			profilesList.add(clonedProfile);
			clonedProfilesSave.put(node, profilesList);
			return;
		}
		
		profilesList = clonedProfilesSave.get(node);
		profilesList.add(clonedProfile);
		clonedProfilesSave.put(node,profilesList);
	}

	private RadioParameters handleRadioInCaseOfCA(RadioParameters clonedProfile,RadioParameters currentProfile,int cellNumber) {
		GeneralUtils.startLevel("Handle CA with Cell : "+cellNumber);
		//first time checking for CA feature -> set parameter.
		if(caTest == null){
			caTest = enbConfig.isCAEnableInNode(dut);
		}
		
		//checked already and CA is not enable -> not changing radio
		if(!caTest){
			GeneralUtils.stopLevel();
			return clonedProfile;
		}
		
		/**
		 * CA is enable -> if the first cell -> should save radio frequency(only for TDD since TDD is the same frequency UL and DL).
		 * 				   if the Second cell -> modify radio frequencies (UL & DL) to first one + parameters according to Bandwidth.
		 * 				   Note: Bandwidth 20 -> 18.9 Mhz , Bandwidth 15 -> 15 MHz
		 */
		if(caTest){
			if(cellNumber == 1){
				report.report("saving 1st Radio frequency");
				radioFreqInCaTest = currentProfile.getDownLinkFrequency();
				report.report("radio frequency being saved : "+radioFreqInCaTest+"Khz");
				}
		if(cellNumber == 2){ 
				clonedProfile = modifyRadioFrequencyAccordingToBandWidth(clonedProfile); 
				}
		}
		GeneralUtils.stopLevel();
		return clonedProfile;
	}

	private RadioParameters modifyRadioFrequencyAccordingToBandWidth(RadioParameters clonedProfile) {
		Double freqToCalc = 0.0;
		report.report("setting different radio Frequency for cell");
		if(clonedProfile.getBandwidth().equals("15")){
			freqToCalc = frequencyAdapter15 * 1000;
		}
		
		if(clonedProfile.getBandwidth().equals("20")){
			freqToCalc = frequencyAdapter20 * 1000;
		}
		
		freqToCalc += radioFreqInCaTest;
		int freqInKhz = freqToCalc.intValue();
		report.report("setting Frequency : "+freqInKhz+"Khz");
		clonedProfile.setDownLinkFrequency(freqInKhz);
		clonedProfile.setUpLinkFrequency(freqInKhz);
		return clonedProfile;
		}
	

	/**
	 * save radio profile according to the cell have to be saved
	 * 
	 * @param rp
	 */
	private void saveRadio(RadioParameters rp, int currentCellId) {
		// get radio profile details
		String currentRadioForCell = "No Such Profile";
		currentRadioForCell = enbConfig.getCurrentRadioProfileName(dut);
		GeneralUtils.printToConsole("Saving : CellId = " + currentCellId + " ,FrameConfig = " + FC + " ,BandWidth = " + BW
				+ " \n into Radio profile : " + currentRadioForCell);
		try {
			GeneralUtils.printToConsole("Saving Radio Profile");
			provision.saveRadio(dut,rp, currentCellId);
		} catch (Exception e) {
			GeneralUtils.printToConsole("Error while trying to save radio Configurations : " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * loading radio profiles back to the original configurations according to
	 * their profile names.
	 * 
	 * @author Shahaf Shuhamy
	 */
	@Test
	@TestProperties(name = "set radio configurations back to Original", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "performReboot", "EARFCN", "IsTestWasSuccessful", "BW", "FC",
					"cellId" })
	public void revert_Radio_profile_To_Origins() {
		boolean status = false;
		HashMap<Integer, INetspanProfile> radioProfilesToBringBack = new HashMap<Integer, INetspanProfile>();
		try {
			radioProfilesToBringBack = provision.loadRadio(dut);
			if (radioProfilesToBringBack == null) {
				report.report("there are no Radio Profiles in the loading list.", Reporter.FAIL);
			} else {
				revertMultiRadio(dut,radioProfilesToBringBack);

				status = rebootAndWaitForAllrunning();
				if (status) {
					report.report("Enodeb Reached all running and in service succefully");
				}
			}
		} catch (Exception e) {
			report.report(e.getMessage());
			report.report("Exception while trying to update radio profile", Reporter.FAIL);
			e.printStackTrace();
		}
	}

	private void revertMultiRadio(EnodeB node,HashMap<Integer, INetspanProfile> radioProfilesToBringBack) {
		String profileName = null;
		for (Integer cell : radioProfilesToBringBack.keySet()) {
			// put current profiles name in a local map
			dut.setCellContextNumber(cell);
			profileName = netspanServer.getCurrentRadioProfileName(dut);
			GeneralUtils.printToConsole("got a profile name from node : " + profileName);
		}

		// setting old profiles back
		enbConfig.setNodeConfiguration(dut, radioProfilesToBringBack);

		removeClonedProfiles(node);
		
		//clearing all maps.
		clonedProfilesSave.clear();
		nodeNameMultiCellProfMap.clear();
	}

	private Boolean rebootAndWaitForAllrunning() {
		boolean status = false;
		int inserviceTime = 0;
		GeneralUtils.startLevel("Rebooting " + dut.getName());
		status = dut.reboot();
		if (!status) {
			report.report("Reboot Failed", Reporter.FAIL);
			return false;
		}

		GeneralUtils.unSafeSleep(30 * 1000);

		status = dut.waitForAllRunningAndInService(EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
		inserviceTime = (int) (EnodeB.WAIT_FOR_ALL_RUNNING_TIME / 1000 / 60);
		if (!status){
			report.report("Enodeb didn't reach all running and in service during " + inserviceTime + " minutes",
					Reporter.FAIL);				
		}else{
			report.report("EnodeB reached all running and in service");
		}
		GeneralUtils.stopLevel();
		return status;
	}

	@ParameterProperties(description = "Name of Enb")
	public void setDUT(String dut) {
		this.dut = (EnodeB) SysObjUtils.getInstnce().initSystemObject(EnodeB.class, false, dut).get(0);
		GeneralUtils.printToConsole("Node " + this.dut.getNetspanName() + " has been loaded");
	}

	@ParameterProperties(description = "EARFCN")
	public void setEARFCN(String EARFCN) {
		this.EARFCN = Integer.valueOf(EARFCN);
	}

	@ParameterProperties(description = "bandWidth")
	public void setBW(String BW) {
		this.BW = Integer.valueOf(BW);
	}

	/**
	 * trying to change String value to int.<br>
	 * in error case - setting 0 to FC parameter.
	 * @param FC
	 */
	@ParameterProperties(description = "Frame Config")
	public void setFC(String FC) {
		try{
			this.FC = Integer.valueOf(FC);
		}catch(Exception e){
			GeneralUtils.printToConsole("Cant Set Value to integer from String - "+FC);
			this.FC = 0;
		}
	}

	@ParameterProperties(description = "Perform reboot to apply changes.")
	public void setPerformReboot(boolean performReboot) {
		this.performReboot = performReboot;
	}

	/**
	 * returns 1 as defualt OR in cellIds there are numbers, OR 999 as *
	 * 
	 * @param cellId
	 */
	// 1 -> [1]
	// 1,2 -> [ 1 2 ]
	// * -> [ ]
	// 1,* -> [ ]
	@ParameterProperties(description = "Cell Id")
	public void setCellId(String cellId) {
		String[] tempCellIds = cellId.split(",");
		cellIds = new ArrayList<Integer>();
		for (String cellID : tempCellIds) {
			if (cellID.equals("*")) {
				cellIds = new ArrayList<Integer>();
				return;
			}
			cellIds.add(Integer.valueOf(cellID));
		}
	}
	
	private void snmpVerification(Integer bandwidth, int frameConfig) {
		String fcStr = String.valueOf(frameConfig);
		
		if(!fddMode){
			GeneralUtils.printToConsole("trying to get Frame Config");
			try{
				String fc = dut.getFrameConfig();
				
				report.report("Checking Frame Config Value with SNMP.");
				if (fc.equals(fcStr)) {
					report.report("Frame Config from SNMP = " + fc);
				} else {
					report.report("Frame Config from SNMP = " + fc + ", while it should have been : " + fcStr, Reporter.FAIL);
				}
			}catch(Exception e){
				report.report("Failed to to get Frame Config via SNMP - FAILED VERIFY.", Reporter.FAIL);
				GeneralUtils.printToConsole(e.getMessage());
				e.printStackTrace();
		}
		
		
		GeneralUtils.printToConsole("trying to get BandWidth");
		try{
			String bwSnmp = dut.getBandWidthSnmp();
			EnodeBChannelBandwidth bwEnum = EnodeBChannelBandwidth.getByDbIndx(Integer.valueOf(bwSnmp));
			Integer bwIn = bwEnum.getBw();
	
			report.report("Checking bandwidth Value with SNMP.");
			if (bandwidth.equals(bwIn)) {
				report.report("Bandwidth index from SNMP = " + bwSnmp + " (" + bwIn + "[Mhz])");
			} else {
				report.report("Bandwidth index from SNMP = " + bwSnmp + " (" + bwIn + "[Mhz]), while it should have been "
						+ bandwidth + "[Mhz]", Reporter.FAIL);
			}
		}catch(Exception e){
			report.report("Failed to to get BandWidth via SNMP - FAILED VERIFY.", Reporter.FAIL);
			GeneralUtils.printToConsole(e.getMessage());
			e.printStackTrace();
			}

		}

	}
}

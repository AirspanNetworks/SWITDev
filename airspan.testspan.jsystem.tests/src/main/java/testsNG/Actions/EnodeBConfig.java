package testsNG.Actions;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import EnodeB.EnodeB;
import EnodeB.Ninja;
import Netspan.EnbProfiles;
import Netspan.NetspanServer;
import Netspan.API.Enums.EnabledStates;
import Netspan.API.Enums.EnbStates;
import Netspan.API.Enums.EnbTypes;
import Netspan.API.Enums.HardwareCategory;
import Netspan.API.Enums.NodeManagementModes;
import Netspan.API.Enums.NrtHoTypes;
import Netspan.API.Enums.PnpModes;
import Netspan.API.Enums.SonAnrStates;
import Netspan.API.Lte.EnbCellProperties;
import Netspan.API.Lte.LteBackhaul;
import Netspan.API.Lte.NodeInfo;
import Netspan.DataObjects.NeighborData;
import Netspan.NBI_15_5.Netspan_15_5_abilities;
import Netspan.Profiles.ManagementParameters;
import Netspan.Profiles.CellAdvancedParameters;
import Netspan.Profiles.CellBarringPolicyParameters;
import Netspan.Profiles.EnodeBAdvancedParameters;
import Netspan.Profiles.INetspanProfile;
import Netspan.Profiles.MobilityParameters;
import Netspan.Profiles.MultiCellParameters;
import Netspan.Profiles.NeighbourManagementParameters;
import Netspan.Profiles.NeighbourManagementParameters.HomeEnbBand;
import Netspan.Profiles.NeighbourManagementParameters.HomeEnbEarfcn;
import Netspan.Profiles.NeighbourManagementParameters.HomeEnbPci;
import Netspan.Profiles.NeighbourManagementParameters.NrtBand;
import Netspan.Profiles.NeighbourManagementParameters.NrtEarfcn;
import Netspan.Profiles.NeighbourManagementParameters.NrtPci;
import Netspan.Profiles.NetworkParameters;
import Netspan.Profiles.RadioParameters;
import Netspan.Profiles.SecurityParameters;
import Netspan.Profiles.SonParameters;
import Netspan.Profiles.SyncParameters;
import Netspan.Profiles.SystemDefaultParameters;
import Utils.GeneralUtils;
import Utils.Pair;
import Utils.GeneralUtils.RebootType;
import Utils.Snmp.MibReader;
import jsystem.framework.report.ListenerstManager;
import jsystem.framework.report.Reporter;

public class EnodeBConfig {

	private static EnodeBConfig instance;
	private static NetspanServer netspanServer;
	public static Reporter report = ListenerstManager.getInstance();
	public Hashtable<String, EnbProfiles> clonedProfiles;
	private static PeripheralsConfig peripheralsConfig = PeripheralsConfig.getInstance();
	
	private EnodeBConfig() {
		clonedProfiles = new Hashtable<>();
	}

	public static EnodeBConfig getInstance() {
		if (instance == null)
			instance = new EnodeBConfig();
		try {
			netspanServer = NetspanServer.getInstance();
		} catch (Exception e) {
			report.report("Netspan Server is unavialable Error: " + e.toString(), Reporter.WARNING);
		}
		return instance;
	}

	public boolean deleteEnbProfile(String profileName, EnbProfiles profileType) {
		boolean res = false;
		if (netspanServer != null) {
			try{
				res = netspanServer.deleteEnbProfile(profileName, profileType);				
			}catch(Exception e){
				e.printStackTrace();
				report.report("deleteEnbProfile failed due to: " + e.getMessage(), Reporter.WARNING);
			}
			if (res) {
				report.report("Delete " + profileType + " named " + profileName + " from netspan "
						+ netspanServer.getHostname() + " succeeded");
			}else{
				report.report("Delete " + profileType + " named " + profileName + " from netspan "
						+ netspanServer.getHostname() + " failed", Reporter.WARNING);
			}
		}
		return res;
	}

	public boolean setProfile(EnodeB enodeB, EnbProfiles enbProfile, String profileName) {
		return netspanServer.setProfile(enodeB, enodeB.getCellContextID(), enbProfile, profileName);
	}

	public boolean setProfile(EnodeB enodeB, int cellID, EnbProfiles enbProfile, String profileName) {
		return netspanServer.setProfile(enodeB, cellID, enbProfile, profileName);
	}

	public boolean setEnbRadioProfile(EnodeB enodeB, String radioProfileName, Boolean isPnpNode) {
		return setEnbRadioProfile(enodeB, radioProfileName, enodeB.getCellContextID(), isPnpNode);
	}

	public boolean setEnbRadioProfile(EnodeB enodeB, String radioProfileName, int cellID, Boolean isPnpNode) {

		if (netspanServer == null) {
			return false;
		}

		if (isPnpNode != null && isPnpNode) {
			if (!netspanServer.setPnPRadioProfile(enodeB, radioProfileName)) {
				report.report("Set PnP Radio Profile Via Netspan Fail", Reporter.WARNING);
				return false;
			}
		} else {
			return netspanServer.setProfile(enodeB, cellID, EnbProfiles.Radio_Profile, radioProfileName);
		}
		return true;
	}

	public boolean setPnPState(EnodeB enb, SonParameters sonParams)  {
		boolean status = EnodeBConfig.getInstance().cloneAndSetSonProfileViaNetspan(enb,
				enb.defaultNetspanProfiles.getSON(), sonParams);
		if (!status) {
			report.report("Set PnP State with netspan Failed, Fall back to SNMP", Reporter.WARNING);
			int pnpModeVal = sonParams.getPnpMode() != PnpModes.DISABLED ? 1 : 0;
			status = enb.setPnpMode(pnpModeVal);
			if (status)
				enb.deleteFile("/bsdata/lte-net.cfg");
		} else {
			report.report("clone Son Profile Via Netspan Passed.");
		}
		report.report(String.format("PnP Status: \"PnPMode\" - %s, lte-net.cfg exists - %s.", enb.getPnpMode(),
				enb.isFileExists("/bsdata/lte-net.cfg")));
		return status;
	}
	
	public boolean setDicicState(EnodeB enb, SonParameters sonParams) throws IOException {
		boolean status = changeSonProfile(enb, sonParams);
		if (!status)
			report.report(enb.getName() + " Set DICIC State with netspan Failed.", Reporter.WARNING);
		else 
			report.report(enb.getName() + " Set DICIC State with netspan Passed.");
		// TODO: Implement with snmp.
		return status;
	}

	public boolean cloneAndSetProfileViaNetSpan(EnodeB node, String cloneFromName, INetspanProfile profileParams) {
		return cloneAndSetProfileViaNetSpan(node,cloneFromName, node.getCellContextID(),profileParams);
	}
	
	public boolean cloneAndSetProfileViaNetSpan(EnodeB node, String cloneFromName, int cellid,
			INetspanProfile profileParams) {
		GeneralUtils.printToConsole("Clone profile " + cloneFromName);
		if (profileParams.getProfileName() == null) {
			profileParams.setProfileName(addCloneSuffix(node, cloneFromName));
		}
		GeneralUtils.printToConsole("New profile name:" + profileParams.getProfileName());
		if (netspanServer == null) {
			return false;
		}

		if (netspanServer.isProfileExists(profileParams.getProfileName(), profileParams.getType())) {
			if (netspanServer.getCurrentProfileName(node, profileParams.getType()).equals(profileParams.getProfileName())) {
				netspanServer.setProfile(node, profileParams.getType(), node.defaultNetspanProfiles.getDefaultProfile(profileParams.getType()));
			}
			deleteEnbProfile(profileParams.getProfileName(), profileParams.getType());
		}

		if (!netspanServer.cloneProfile(node, cloneFromName, profileParams)) {
			return false;
		} else {
			clonedProfiles.put(profileParams.getProfileName(), profileParams.getType());

			Boolean isPnpNode = netspanServer.isPnpNode(node.getName());
			boolean result = true;
			if (isPnpNode != null && isPnpNode) {
				if (!netspanServer.setPnPRadioProfile(node, profileParams.getProfileName())) {
					report.report("Set PnP Radio Profile Via Netspan Fail", Reporter.WARNING);
					result = false;
				}
			} else {
				if (!netspanServer.setProfile(node, cellid, profileParams.getType(), profileParams.getProfileName())) {
					result = false;
				}
			}
			netspanServer.getRawNetspanGetProfileResponse(node, profileParams.getProfileName(), profileParams);
			return result;
		}
	}

	public boolean cloneAndSetRadioProfileViaNetSpan(EnodeB node, String cloneFromName, RadioParameters radioParams) {
		boolean result =  cloneAndSetRadioProfileViaNetSpan(node, cloneFromName, node.getCellContextID(), radioParams);
		if(radioParams.getEarfcn() != null ||
				radioParams.getDownLinkFrequency() != null ||
				radioParams.getUpLinkFrequency() != null){
			if(result){
				// reboot was added because of system bug that prevent the
				// frequency to change on the fly- should be fixed on 15.5
				report.report("Frequency changes are not on the fly- reboot needed", Reporter.WARNING);
				node.reboot();
				node.waitForAllRunningAndInService(EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
			}
		}
		return result;
	}

	public boolean cloneAndSetRadioProfileViaNetSpan(EnodeB node, String cloneFromName, int cellID,
			RadioParameters radioParams) {
		
		return cloneAndSetProfileViaNetSpan(node, cloneFromName, cellID, radioParams);
	}

	public boolean cloneAndSetNetworkProfileViaNetSpan(EnodeB node, String cloneFromName,
			NetworkParameters networkParams) {

		return cloneAndSetProfileViaNetSpan(node, cloneFromName, networkParams);
	}

	public boolean cloneAndSetSyncProfileViaNetSpan(EnodeB node, String cloneFromName, SyncParameters syncParams){
		return cloneAndSetProfileViaNetSpan(node, cloneFromName, syncParams);
	}

	public boolean cloneAndSetMobilityProfileViaNetSpan(EnodeB node, String cloneFromName,
			MobilityParameters mobilityParams) {
		return cloneAndSetMobilityProfileViaNetSpan(node, cloneFromName, node.getCellContextID(), mobilityParams);
	}

	public boolean cloneAndSetMobilityProfileViaNetSpan(EnodeB node, String cloneFromName, int cellid,
			MobilityParameters mobilityParams) {

		return cloneAndSetProfileViaNetSpan(node, cloneFromName, cellid, mobilityParams);
	}
	
	public boolean cloneAndSetCellAdvancedProfileViaNetspan(EnodeB node, String cloneFromName, CellAdvancedParameters advancedParams) {
		return cloneAndSetCellAdvancedProfileViaNetspan(node, cloneFromName, node.getCellContextID(), advancedParams);
	}

	public boolean cloneAndSetCellAdvancedProfileViaNetspan(EnodeB node, String cloneFromName, int cellid,
			CellAdvancedParameters advancedParams) {

		return cloneAndSetProfileViaNetSpan(node, cloneFromName, cellid, advancedParams);
	}

	public boolean cloneAndSetSonProfileViaNetspan(EnodeB node, String cloneFromName, SonParameters sonParams) {

		return cloneAndSetProfileViaNetSpan(node,cloneFromName,sonParams);
	}

	/**
	 * @author Shahaf Shahumy get Current enodeb Security Profile Save the new
	 *         profile in map change the cloned one and set in netspan
	 * @param node
	 * @param cloneFromName
	 * @param securityPramas
	 * @throws Exception
	 */
	public boolean cloneAndSetSecurityProfileViaNetSpan(EnodeB node, String cloneFromName,
			SecurityParameters securityPramas) throws Exception {

		return cloneAndSetProfileViaNetSpan(node, cloneFromName, securityPramas);
	}

	public boolean setWarmResetState(EnodeB node, boolean warmResetEnable) {
		boolean actionSucceeded = true;
		int value = warmResetEnable ? 1 : 0;
		int mask = warmResetEnable ? 4067 : 1059;
		/*
		 * EnabledDisabledStates state = warmResetEnable ?
		 * EnabledDisabledStates.ENABLED : EnabledDisabledStates.DISABLED; if
		 * (netspanServer.setPnPWarmResetMode(node, state)) { report.report(
		 * "Set PnP warm reset mode Via Netspan Succeeded.");
		 * clonedProfiles.put(addCloneSuffix(node,
		 * node.defaultNetspanProfiles.getSON()), EnbProfiles.Advanced_Profile);
		 * } else {
		 */
		// report.report("[WARNING]: Set PnP warm reset mode Via Netspan Fail",
		// Reporter.WARNING);
		// try to set using SNMP
		node.setPnpWarmResetModeAdmin(value);
		node.setPnpWarmRebootMask(mask);
		// }

		try {
			actionSucceeded &= node.getPnpWarmResetModeAdmin().equals(Integer.toString(value));
			actionSucceeded &= node.getPnpWarmRebootMask().equals(Integer.toString(mask));
			if (actionSucceeded) {
				report.report("Set PnP warm reset mode Via SNMP succeeded.");
			}
		} catch (IOException e) {
			report.report("setWarmResetState failed due to: " + e.getMessage(), Reporter.WARNING);
			e.printStackTrace();
			return false;
		}
		return actionSucceeded;
	}

	public void deleteClonedProfiles() {
		if (!clonedProfiles.isEmpty()){
			Iterator<Entry<String, EnbProfiles>> iter = clonedProfiles.entrySet().iterator();
			GeneralUtils.startLevel("Delete cloned profiles");
			while (iter.hasNext()) {
				Entry<String, EnbProfiles> entry = iter.next();
				EnodeBConfig.getInstance().deleteEnbProfile(entry.getKey(), entry.getValue());
				iter.remove();
			}
			GeneralUtils.stopLevel();			
		}
	}

	private String addCloneSuffix(EnodeB enodeB, String profileName) {
		String suffix = "_NBI_Auto" + enodeB.getCellIdentity();
		String cloneProfileName = profileName + suffix;
		if (cloneProfileName.length() >= 64) {
			StringBuilder a = new StringBuilder();
			a.append(cloneProfileName);
			a.delete(a.length() - profileName.length(), a.length() - 1);
			cloneProfileName = a.toString();
		}
		return cloneProfileName;
	}

	public RadioParameters radioProfileGetSnmp(EnodeB enodeB) {
		RadioParameters radioParams = new RadioParameters();
		try {
			radioParams.setBandWidthSNMP(enodeB.getBandWidthSnmp());
			radioParams.setDuplex(enodeB.getDuplexModeSnmp());
			radioParams.setSpecialSubFrame(enodeB.getSpecialSubFrameSnmp());
			radioParams.setFrameConfig(enodeB.getFrameConfig());
		} catch (Exception e) {
			report.report("radioProfileGetSnmp failed due to: " + e.getMessage(), Reporter.WARNING);
			e.printStackTrace();
			return null;
		}
		return radioParams;
	}

	public RadioParameters syncProfileGetSnmp(EnodeB enodeB) {
		RadioParameters radioParams = new RadioParameters();
		try {
			radioParams.setBandWidthSNMP(enodeB.getBandWidthSnmp());
			radioParams.setDuplex(enodeB.getDuplexModeSnmp());
			radioParams.setSpecialSubFrame(enodeB.getSpecialSubFrameSnmp());
			radioParams.setFrameConfig(enodeB.getFrameConfig());
		} catch (Exception e) {
			report.report("syncProfileGetSnmp failed due to: " + e.getMessage(), Reporter.WARNING);
			e.printStackTrace();
			return null;
		}
		return radioParams;
	}

	public RadioParameters getRadioProfile(EnodeB enb) {
		RadioParameters radioParams = null;
		try {
			GeneralUtils.printToConsole("trying to get Radio Parameters from NetSpan:");
			radioParams = netspanServer.radioProfileGet(enb);
			report.report("radio parameters initialized via netspan.");
		} catch (Exception e) {
			report.report(
					"Exception Interrupted while trying to get Radio Parameters from netspan method!, Trying Snmp "
							+ e.getMessage());
			e.printStackTrace();
			for (int i = 0; i < 3; i++) {
				try {
					radioParams = getRadioProfileSnmp(enb);
					break;
				} catch (Exception ex) {
					report.report(ex.getMessage());
					ex.printStackTrace();
					GeneralUtils.unSafeSleep(2000);
				}
			}
		}

		if (radioParams != null) {
			// CFI via snmp anyways
			report.report("getting CFI parameter via SNMP.");
			String cfiSnmp = enb.getCFISnmp();
			if(cfiSnmp.equals(String.valueOf(GeneralUtils.ERROR_VALUE))){
				cfiSnmp = "2";
			}
			radioParams.setCFI(cfiSnmp);
			String Messege = radioParams.toString();
			report.report(Messege);
			if(enb instanceof Ninja){
				Ninja tempNin = ((Ninja)enb);
				EnodeB donor = tempNin.getDonor();
				if(donor != null) {radioParams.donorRadioParameters = getRadioProfile(donor);}
			}
			return radioParams;
		} else {
			report.report("radio Params could not been initialized", Reporter.WARNING);
			return null;
		}
	}

	// returning the radio profile name as string.
	public String getCurrentRadioProfileName(EnodeB enb) {
		return netspanServer.getCurrentRadioProfileName(enb);
	}

	public String getCurrentMobilityProfileName(EnodeB enb) {
		return netspanServer.getCurrentMobilityProfileName(enb);
	}

	public String getCurrentEnbAdvancedConfigurationProfileName(EnodeB enb) {
		return netspanServer.getCurrentEnbAdvancedConfigurationProfileName(enb);
	}
	
	public String getCurrentCellAdvancedConfigurationProfileName(EnodeB enb) {
		return netspanServer.getCurrentCellAdvancedConfigurationProfileName(enb);
	}

	public String getCurrentNetWorkProfileName(EnodeB enb) {
		return netspanServer.getCurrentNetworkProfileName(enb);
	}

	private RadioParameters getRadioProfileSnmp(EnodeB dutENB) {
		RadioParameters radioParamsSnmp;
		radioParamsSnmp = this.radioProfileGetSnmp(dutENB);
		return radioParamsSnmp;
	}

	/**
	 *  checks if the EnodeB is managed in NetSPan
	 *
	 * @param enodeB  - enodeB
	 * @return - true if it's managed
	 */
	public boolean isEnodebManaged(EnodeB enodeB) {
		// Should happen only once in scenario
		if(enodeB.isManagedByNetspan())
			return true;
		try {
			NodeManagementModes managed = netspanServer.getManagedMode(enodeB);
			if (managed != NodeManagementModes.MANAGED){
				report.report("EnodeB: " + enodeB.getNetspanName() + " Is Not Managed in Netspan: "
						+ NetspanServer.getInstance().getHostname(), Reporter.WARNING);
				return false;
			}
			enodeB.setManagedByNetspan(true);
			return true;
		} catch (Exception e) {
			report.report("isEnodebManaged failed due to: " + e.getMessage(), Reporter.WARNING);
			e.printStackTrace();
			return false;
		}
	}

	public String getSystemDefaultProfile(EnodeB enb) {
		return netspanServer.getCurrentSystemDefaultProfileName(enb);
	}

	public void changeSecurityProfileBack(EnodeB dut) {
		String defaultProfile = dut.getDefaultNetspanProfiles().getSecurity();
		boolean settingResult = netspanServer.setProfile(dut, EnbProfiles.Security_Profile, defaultProfile);
		if (!settingResult) {
			report.report("could not return enb Security profile back", Reporter.WARNING);
		} else {
			report.report("Set security Profile to enb: " + dut.getNetspanName() + " successfully");
		}
	}

	/**
	 * @author Shahaf Shahumy returnning the NetworkParameters Object updated in
	 *         whatever user inserted into it
	 * @param firstDUTNetworkProfile
	 * @param np
	 * @return
	 */
	public NetworkParameters getNetworkParameters(String firstDUTNetworkProfile, NetworkParameters np) {
		try {
			netspanServer.getNetworkProfileResult(firstDUTNetworkProfile, np);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return np;
	}

	public void setPnPSwVersion(EnodeB dut1, String version) {
		// TODO Auto-generated method stub
	}

	public String getMMeIpAdress(EnodeB enb) {
		return netspanServer.getMMEIpAdress(enb);
	}
	
	public ArrayList<String> getMMeIpAdresses(EnodeB enb) {
		return netspanServer.getMMEIpAdresses(enb);
	}

	public NodeInfo getNodeInfo(EnodeB enb) {
		// if(ni == null)
		// WE NEET TO GET THE INFOR FROM SNMP

		return netspanServer.getNodeDetails(enb);
	}

	public String getPTPInterfaceIP(EnodeB dut) {
		String response = netspanServer.getPTPInterfaceIP(dut);
		if (response == null)
			response = dut.getPTPInterfaceIP();
		return response;
	}

	public String getGMIP(EnodeB dut) {
		String response = netspanServer.getGMIP(dut);
		if (response == null)
			response = dut.getPrimaryGrandMasterIP();
		return response;
	}

	/**
	 * Setting new network profile with new PLMN list, MME IP nad new name. the
	 * function should get the network prifule object with the params that the
	 * user want to change (the name of the profile will set automatically in
	 * the function)
	 * 
	 * @param dut
	 * @param np
	 * @return
	 * @throws IOException
	 *             author: Moran Goldenberg
	 */
	public boolean setPLMN(EnodeB dut, NetworkParameters np) {
		boolean result = true;
		try {
			String enbNetworkProfile = dut.getDefaultNetspanProfiles().getNetwork();
			report.startLevel("Cloning network profile");
			// setting the new network profile name.
			report.report("Trying to Clone and Set Network Profile to netspan");
			np.setProfileName(enbNetworkProfile + "_" + GeneralUtils.getPrefixAutomation());

			// setting the new network profile name.
			if (!cloneAndSetNetworkProfileViaNetSpan(dut, enbNetworkProfile, np)) {
				report.report("Set PLMN via netspan didnt succeded with netspan, trying to set PLMN with SNMP",
						Reporter.WARNING);
				if (!dut.setPLMN(np.plmnList)) {
					result = false;
				} else {
					// NEED TO CREATE NEW FUNCTIOIN THAT WILL CHANGE THE MME IP
					// ADRESS.
				}
			}
			if (dut.reboot(false))
				dut.waitForAllRunning(EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
			else {
				report.report("Couldn't reboot The enodeB " + dut.getNetspanName(), Reporter.WARNING);
				result = false;
			}
			return result;

		} catch (Exception e) {
			report.report("setPLMN failed due to: " + e.getMessage(), Reporter.WARNING);
			e.printStackTrace();
			return false;
		} finally {
			try {
				report.stopLevel();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * creating lte-netV6.cfg file in node in order to change enodeB to Ipv6
	 * 
	 * @author Shahaf Shahumy
	 * @param node
	 * @param version
	 * @param ipv6
	 * @param netmask
	 * @param gateWay
	 * @param ipV6Vlan
	 * @param ipV6Tag
	 * @return
	 */
	public boolean createIPv6LteConfigFile(EnodeB node, String version, String ipv6, String netmask, String gateWay,
			String ipV6Vlan, String ipV6Tag, String fileName) {
		String filesInShell = node.shell("cd /bsdata \n" + "ls");
		if (filesInShell.contains(fileName)) {
			report.report("lte-netV6 file already exists");
			return false;
		}
		String lteFileName = fileName;
		node.shell("touch " + lteFileName);
		node.shell("echo CONFIG_V6_LTE_VERSION=" + version + ">>" + lteFileName);
		node.shell("echo CONFIG_V6_LTE_IP_ADDR=" + ipv6 + ">>" + lteFileName);
		node.shell("echo CONFIG_V6_LTE_IP_NETMASK=" + netmask + ">>" + lteFileName);
		node.shell("echo CONFIG_V6_LTE_IP_GATEWAY=" + gateWay + ">>" + lteFileName);
		node.shell("echo CONFIG_V6_LTE_VLAN=" + ipV6Vlan + ">>" + lteFileName);
		node.shell("echo CONFIG_V6_LTE_VLAN_STATUS=" + ipV6Tag + ">>" + lteFileName);
		return true;
	}

	/**
	 * creating backup file from current snmpd.conf file and making a snmp file
	 * for ipv6 in node.
	 * 
	 * @author Shahaf Shuhamy
	 * @param node
	 * @param rwCommunity
	 * @param roCommunity
	 * @param trapsink
	 * @param agentuser
	 * @param dlmod
	 * @param agentaddress
	 * @return
	 */
	public boolean createIPv6SnmpConfigFile(EnodeB node, String rwCommunity, String roCommunity, String trapsink,
			String agentuser, String dlmod, String agentaddress, String fileName) {
		String tryoutFileName = fileName;
		node.shell("cd /bsdata");
		node.shell("cp" + fileName + " " + fileName + ".back");
		node.shell("touch " + tryoutFileName);
		node.shell("echo rwcommunity6 " + rwCommunity + ">>" + tryoutFileName);
		node.shell("echo rocommunity6 " + roCommunity + ">>" + tryoutFileName);
		node.shell("echo trapsink " + trapsink + ">>" + tryoutFileName);
		node.shell("echo agentuser " + agentuser + ">>" + tryoutFileName);
		node.shell("echo dlmod " + dlmod + ">>" + tryoutFileName);
		node.shell("echo agentaddress " + agentaddress + ">>" + tryoutFileName);
		return true;

	}

	/**
	 * gets managment format and transforming to ipv6 managment 20.20.2.x ->
	 * 2020::20:2:x
	 * 
	 * @param ip
	 * @return
	 */
	public String IPv4ToIPv6Convention(String ip) {
		String[] blocks = ip.split("\\.");
		return "2020::20:2:" + blocks[blocks.length - 1];
	}

	/**
	 * change enodeB state in netspan or snmp if not succeeded.
	 * 
	 * @author Shahaf Shahumy
	 * @throws IOException
	 */
	public void changeOtherENBsToOOS(EnodeB dut, ArrayList<EnodeB> enbInSetup) {
		try {
			report.startLevel("EnodeBs settings");
			peripheralsConfig.changeEnbState(dut, EnbStates.IN_SERVICE);
			ArrayList<EnodeB> toOOS = (ArrayList<EnodeB>) enbInSetup.clone();
			toOOS.remove(dut);
			for (EnodeB enb : toOOS) {
				peripheralsConfig.changeEnbState(enb, EnbStates.OUT_OF_SERVICE);
			}
			report.stopLevel();
		} catch (Exception e) {
			report.report("changeOtherENBsToOOS failed due to: " + e.getMessage(), Reporter.WARNING);
			e.printStackTrace();
		}
	}

	/**
	 * Set Enb Type Macro/Home.
	 * 
	 * @author Dor Shalom
	 */
	public boolean setEnbType(EnodeB dut, EnbTypes enbType) {
		try {
			if (!netspanServer.setEnbType(dut.getNetspanName(), enbType)) {
				if (Integer.parseInt(dut.getEnbType()) != enbType.ordinal()) {
					dut.setEnbType(enbType.ordinal());
				}
			}
			GeneralUtils.unSafeSleep(10000);
			return Integer.parseInt(dut.getEnbType()) == enbType.ordinal();
		} catch (Exception e) {
			report.report("setEnbType failed due to: " + e.getMessage(), Reporter.WARNING);
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Set Enb Type Macro/Home.
	 * 
	 * @author Dor Shalom
	 */
	public boolean setMultiCellstate(EnodeB dut, Boolean isEnabled) {
		try {
			if (!netspanServer.setMultiCellState(dut, isEnabled)) {
				report.report("setMultiCellstate via netspan failed.", Reporter.WARNING);
			}
			GeneralUtils.unSafeSleep(10000);
			return dut.getCellServiceState(41).equals((isEnabled ? "1" : "0"));
		} catch (Exception e) {
			report.report("setMultiCellstate failed due to: " + e.getMessage(), Reporter.WARNING);
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Set Enb Type Macro/Home.
	 * 
	 * @author Dor Shalom
	 */
	public boolean setOperateBehindHenbGw(EnodeB dut, EnabledStates isEnabled) {
		try {
			report.report("Setting Operate Behind Henb Gw via netspan.");
			NetworkParameters networkParams = new NetworkParameters();
			networkParams.setOperateBehindHenbGw(isEnabled);
			if (!changeNetworkProfile(dut, networkParams)) {
				report.report("setOperateBehindHenbGw via netspan failed.", Reporter.WARNING);
			}
			GeneralUtils.unSafeSleep(10000);
			return dut.getOperateBehindHenbGw().equals((isEnabled));
		} catch (Exception e) {
			report.report("setOperateBehindHenbGw failed due to: " + e.getMessage(), Reporter.WARNING);
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * En/Dis able TPM MRO Metric.
	 * 
	 * @author Avichai Yefet
	 */
	public boolean enableTpmMroMetric(EnodeB enb, boolean enable) {
		// TODO: try first set with NMS
		try {
			report.report("Enable TPM MRO Metric");
			String oid = MibReader.getInstance().resolveByName("asLteStkTpmCfgMroMetric");
			return enb.snmpSet(oid, GeneralUtils.booleanToInteger(enable));
		} catch (IOException e) {
			e.printStackTrace();
			report.report("Failed to set value via snmp", Reporter.FAIL);
			return false;
		}
	}

	/**
	 * En/Dis able TPM Power Change.
	 * 
	 * @author Avichai Yefet
	 */
	public boolean enableTpmPowerChange(EnodeB enb, boolean enable) {
		// TODO: try first set with NMS
		try {
			report.report("Enable Power Change");
			String oid = MibReader.getInstance().resolveByName(""); // add MIB
			return enb.snmpSet(oid, GeneralUtils.booleanToInteger(enable));
		} catch (IOException e) {
			e.printStackTrace();
			report.report("Failed to set value via snmp", Reporter.FAIL);
			return false;
		}
	}

	public boolean checkIfEsonServerConfigured(EnodeB enb) {
		return netspanServer.checkIfEsonServerConfigured(enb);
	}

	public int readTxMaxPowerOfProductViaShell(EnodeB enb,String productCode) {
		String txMaxPowerString = readParamFromHwProductsTableViaShell(enb, "txPowerMax",productCode);
		if (txMaxPowerString != null) {
			int txMaxPower = Integer.parseInt(txMaxPowerString) / 100;
			return txMaxPower;
		}
		// The product does not exist in the CSV file, return ERROR VALUE
		return GeneralUtils.ERROR_VALUE;
	}

	/**
	 * En/Dis able TPM Mode.
	 * 
	 * @author Avichai Yefet
	 */
	public boolean setTpmMode(EnodeB enb, boolean enable) {
		// TODO: try first set with NMS
		try {
			report.report("Set TPMMode to " + enable);
			String oid = MibReader.getInstance().resolveByName("asLteStkTpmCfgMode");
			return enb.snmpSet(oid, GeneralUtils.booleanToInteger(enable));
		} catch (Exception e) {
			e.printStackTrace();
			report.report("Failed to set value via snmp", Reporter.FAIL);
			return false;
		}
	}

	/**
	 * En/Dis able TPM MRO Metric.
	 * 
	 * @author Avichai Yefet
	 */
	public boolean setTpmMroMetric(EnodeB enb, boolean enable) {
		// TODO: try first set with NMS
		try {
			report.report("Set TPMMROMetric to " + enable);
			String oid = MibReader.getInstance().resolveByName("asLteStkTpmCfgMroMetric");
			return enb.snmpSet(oid, GeneralUtils.booleanToInteger(enable));
		} catch (Exception e) {
			e.printStackTrace();
			report.report("Failed to set value via snmp", Reporter.FAIL);
			return false;
		}
	}

	/**
	 * En/Dis able TPM Power Change.
	 * 
	 * @author Avichai Yefet
	 */
	public boolean setTpmPowerChange(EnodeB enb, boolean enable) {
		// TODO: try first set with NMS
		try {
			report.report("Set PwrChng to " + enable);
			String oid = MibReader.getInstance().resolveByName("asLteStkCellTpmStatusPwrChange");
			return enb.snmpSet(oid, GeneralUtils.booleanToInteger(enable));
		} catch (Exception e) {
			e.printStackTrace();
			report.report("Failed to set value via snmp", Reporter.FAIL);
			return false;
		}
	}

	/**
	 * Low resources - set CPU limit.
	 * 
	 * @author Avichai Yefet
	 */
	public boolean setCpuLoadThreshold(EnodeB enb, int loadThreshold) {
		// TODO: try first set with NMS
		try {
			report.report("Set CPU Load Threshold to " + loadThreshold);
			String oid = MibReader.getInstance().resolveByName("asLteStkLowResEventsParamsCfgCpuLoadTh");
			return enb.snmpSet(oid, loadThreshold);
		} catch (Exception e) {
			e.printStackTrace();
			report.report("Failed to set value via snmp", Reporter.WARNING);
			return false;
		}
	}

	/**
	 * Low resources - get CPU limit.
	 * 
	 * @author Avichai Yefet
	 */
	public int getCpuLoadThreshold(EnodeB enb) {
		// TODO: try first set with NMS
		try {
			report.report("Get CPU Load Threshold");
			String oid = MibReader.getInstance().resolveByName("asLteStkLowResEventsParamsCfgCpuLoadTh");
			return Integer.valueOf(enb.getSNMP(oid));
		} catch (Exception e) {
			e.printStackTrace();
			report.report("Failed to get value via snmp", Reporter.WARNING);
			return GeneralUtils.ERROR_VALUE;
		}
	}

	/**
	 * Low resources - set RRC limit.
	 * 
	 * @author Avichai Yefet
	 */
	public boolean setRrcLoadThreshold(EnodeB enb, int loadThreshold) {
		// TODO: try first set with NMS
		try {
			report.report("Set RRC Load Threshold to " + loadThreshold);
			String oid = MibReader.getInstance().resolveByName("asLteStkLowResEventsParamsCfgRrcLoadTh");
			return enb.snmpSet(oid, loadThreshold);
		} catch (Exception e) {
			e.printStackTrace();
			report.report("Failed to set value via snmp", Reporter.WARNING);
			return false;
		}
	}

	/**
	 * Low resources - get RRC limit.
	 * 
	 * @author Avichai Yefet
	 */
	public int getRrcLoadThreshold(EnodeB enb) {
		// TODO: try first set with NMS
		try {
			report.report("Get RRC Load Threshold");
			String oid = MibReader.getInstance().resolveByName("asLteStkLowResEventsParamsCfgRrcLoadTh");
			return Integer.valueOf(enb.getSNMP(oid));
		} catch (Exception e) {
			e.printStackTrace();
			report.report("Failed to get value via snmp", Reporter.WARNING);
			return GeneralUtils.ERROR_VALUE;
		}
	}

	/**
	 * Low resources - set Memory limit.
	 * 
	 * @author Avichai Yefet
	 */
	public boolean setMemoryLoadThreshold(EnodeB enb, int loadThreshold) {
		// TODO: try first set with NMS
		try {
			report.report("Set Memory Load Threshold to " + loadThreshold);
			String oid = MibReader.getInstance().resolveByName("asLteStkLowResEventsParamsCfgMemLoadTh");
			return enb.snmpSet(oid, loadThreshold);
		} catch (Exception e) {
			e.printStackTrace();
			report.report("Failed to set value via snmp", Reporter.WARNING);
			return false;
		}
	}

	/**
	 * Low resources - get Memory limit.
	 * 
	 * @author Avichai Yefet
	 */
	public int getMemoryLoadThreshold(EnodeB enb) {
		// TODO: try first set with NMS
		try {
			report.report("Get Memory Load Threshold");
			String oid = MibReader.getInstance().resolveByName("asLteStkLowResEventsParamsCfgMemLoadTh");
			return Integer.valueOf(enb.getSNMP(oid));
		} catch (Exception e) {
			e.printStackTrace();
			report.report("Failed to get value via snmp", Reporter.WARNING);
			return GeneralUtils.ERROR_VALUE;
		}
	}

	/**
	 * Low resources - set ResourcesSampleInterval.
	 * 
	 * @author Avichai Yefet
	 */
	public boolean setLowResourcesSampleInterval(EnodeB enb, int value) {
		// TODO: try first set with NMS
		try {
			report.report("Set ResourcesSampleInterval to " + value);
			String oid = MibReader.getInstance().resolveByName("asLteStkLowResEventsParamsCfgResourcesSampleInterval");
			return enb.snmpSet(oid, value);
		} catch (Exception e) {
			e.printStackTrace();
			report.report("Failed to set value via snmp", Reporter.WARNING);
			return false;
		}
	}

	/**
	 * Low resources - get ResourcesSampleInterval.
	 * 
	 * @author Avichai Yefet
	 */
	public int getLowResourcesSampleInterval(EnodeB enb) {
		// TODO: try first set with NMS
		try {
			report.report("Get ResourcesSampleInterval");
			String oid = MibReader.getInstance().resolveByName("asLteStkLowResEventsParamsCfgResourcesSampleInterval");
			return Integer.valueOf(enb.getSNMP(oid));
		} catch (Exception e) {
			e.printStackTrace();
			report.report("Failed to get value via snmp", Reporter.WARNING);
			return GeneralUtils.ERROR_VALUE;
		}
	}

	/**
	 * Low resources - set ResourcesReportInterval.
	 * 
	 * @author Avichai Yefet
	 */
	public boolean setLowResourcesReportInterval(EnodeB enb, int value) {
		// TODO: try first set with NMS
		try {
			report.report("Set ResourcesReportInterval to " + value);
			String oid = MibReader.getInstance().resolveByName("asLteStkLowResEventsParamsCfgResourcesReportInterval");
			return enb.snmpSet(oid, value);
		} catch (Exception e) {
			e.printStackTrace();
			report.report("Failed to set value via snmp", Reporter.WARNING);
			return false;
		}
	}

	/**
	 * Low resources - get ResourcesReportInterval.
	 * 
	 * @author Avichai Yefet
	 */
	public int getLowResourcesReportInterval(EnodeB enb) {
		// TODO: try first set with NMS
		try {
			report.report("Get ResourcesReportInterval");
			String oid = MibReader.getInstance().resolveByName("asLteStkLowResEventsParamsCfgResourcesReportInterval");
			return Integer.valueOf(enb.getSNMP(oid));
		} catch (Exception e) {
			e.printStackTrace();
			report.report("Failed to get value via snmp", Reporter.WARNING);
			return GeneralUtils.ERROR_VALUE;
		}
	}

	/**
	 * Low resources - set LowResourceHyst.
	 * 
	 * @author Avichai Yefet
	 */
	public boolean setLowResourceHyst(EnodeB enb, int value) {
		// TODO: try first set with NMS
		try {
			report.report("Set LowResourceHyst to " + value);
			String oid = MibReader.getInstance().resolveByName("asLteStkLowResEventsParamsCfgLowResourceHyst");
			return enb.snmpSet(oid, value);
		} catch (Exception e) {
			e.printStackTrace();
			report.report("Failed to set value via snmp", Reporter.WARNING);
			return false;
		}
	}

	/**
	 * Low resources - get LowResourceHyst.
	 * 
	 * @author Avichai Yefet
	 */
	public int getLowResourceHyst(EnodeB enb) {
		// TODO: try first set with NMS
		try {
			report.report("Get LowResourceHyst");
			String oid = MibReader.getInstance().resolveByName("asLteStkLowResEventsParamsCfgLowResourceHyst");
			return Integer.valueOf(enb.getSNMP(oid));
		} catch (Exception e) {
			e.printStackTrace();
			report.report("Failed to get value via snmp", Reporter.WARNING);
			return GeneralUtils.ERROR_VALUE;
		}
	}
	
	/**
	 * rabParams - set pdcpDisTmrBs.
	 * 
	 * @author Avichai Yefet
	 */
	public boolean setPdcpDisTmrBs(EnodeB enb, int qciId, int pdcpDisTmrBs) {
		try {
			report.report("Set PdcpDisTmrBs (qciId="+qciId+") to " + pdcpDisTmrBs);
			String oid = MibReader.getInstance().resolveByName("asLteStkRabParamsPdcpDisTmrBs");
			oid += ".";
			oid += qciId;
			return enb.snmpSet(oid, pdcpDisTmrBs);
		} catch (Exception e) {
			e.printStackTrace();
			report.report("Failed to set value via snmp", Reporter.WARNING);
			return false;
		}
	}

	/**
	 * rabParams - get pdcpDisTmrBs.
	 * 
	 * @author Avichai Yefet
	 */
	public int getPdcpDisTmrBs(EnodeB enb, int qciId) {
		try {
			report.report("Get PdcpDisTmrBs (qciId="+qciId+")");
			String oid = MibReader.getInstance().resolveByName("asLteStkRabParamsPdcpDisTmrBs");
			oid += ".";
			oid += qciId;
			return Integer.valueOf(enb.getSNMP(oid));
		} catch (Exception e) {
			e.printStackTrace();
			report.report("Failed to get value via snmp", Reporter.WARNING);
			return GeneralUtils.ERROR_VALUE;
		}
	}
	

	public int readTxMinPowerOfProductViaShell(EnodeB enb,String productCode) {
		String txMinPowerString = readParamFromHwProductsTableViaShell(enb, "txPowerMin",productCode);
		if (txMinPowerString != null) {
			int txMinPower = Integer.parseInt(txMinPowerString) / 100;
			return txMinPower;
		}
		// The product does not exist in the CSV file, return ERROR VALUE
		return GeneralUtils.ERROR_VALUE;
	}

	private String readParamFromHwProductsTableViaShell(EnodeB enb, String param,String productCode) {
		String[] str = enb.shell("grep " + param + " /bs/data/hwProducts").split(",");
		for (int i = 0; i < str.length; i++) {
			if (str[i].equals(param)) {
				int index = i;
				String value = enb.shell("grep " + productCode + " /bs/data/hwProducts")
						.split(",")[index];
				return value;
			}
		}
		// The product does not exist in the CSV file, return null
		return null;
	}

	/**
	 * Change cell's PCI.
	 * 
	 * @author Dor Shalom
	 */
	public boolean changeEnbCellPci(EnodeB enb, int pci) {
		boolean result = false;
		if (netspanServer.setPci(enb, pci)){
			result = true;
		}else{
			try {
				result = enb.setPci(pci);
			
			} catch (Exception e) {
				e.printStackTrace();
				report.report("Failed to set value via snmp", Reporter.FAIL);
				return false;
			}
		}
		performReProvision(enb);
		return result;
	}

	
	public boolean changeSonProfile(EnodeB enb, SonParameters sonParams){
		boolean isSucceeded;
		String currentProfile = netspanServer.getCurrentSonProfileName(enb);
		GeneralUtils.printToConsole(currentProfile);
		if (currentProfile.equals(addCloneSuffix(enb, enb.defaultNetspanProfiles.getSON()))){
			sonParams.setProfileName(currentProfile);
			isSucceeded = netspanServer.updateSonProfile(enb, currentProfile,sonParams);
			netspanServer.getRawNetspanGetProfileResponse(enb, sonParams.getProfileName(), sonParams);
		}
		else{
			isSucceeded = cloneAndSetSonProfileViaNetspan(enb, currentProfile, sonParams);			
		}
		return isSucceeded;
	}
	
	public boolean changeCellAdvancedProfile(EnodeB enb, CellAdvancedParameters advancedParams){
		boolean isSucceeded;
		String currentProfile = netspanServer.getCurrentCellAdvancedConfigurationProfileName(enb);
		GeneralUtils.printToConsole(currentProfile);
		if (currentProfile.equals(addCloneSuffix(enb, enb.defaultNetspanProfiles.getCellAdvanced()))){
			advancedParams.setProfileName(currentProfile);
			isSucceeded = netspanServer.updateCellAdvancedProfile(enb,advancedParams);			
		}
		else{
			isSucceeded = cloneAndSetCellAdvancedProfileViaNetspan(enb, currentProfile, advancedParams);			
		}
		return isSucceeded;
	}
	
	public boolean changeNetworkProfile(EnodeB enb, NetworkParameters networkParams){
		boolean isSucceeded;
		String currentProfile = netspanServer.getCurrentNetworkProfileName(enb);
		GeneralUtils.printToConsole(currentProfile);
		if (currentProfile.equals(addCloneSuffix(enb, enb.defaultNetspanProfiles.getNetwork()))){
			networkParams.setProfileName(currentProfile);
			isSucceeded = netspanServer.updateNetworkProfile(enb, currentProfile,networkParams);			
		}
		else{
			isSucceeded = cloneAndSetNetworkProfileViaNetSpan(enb, currentProfile, networkParams);			
		}
		return isSucceeded;
	}
	
	public boolean updateNetworkProfile(EnodeB enb, NetworkParameters networkParams){
		boolean isSucceeded = true;
		String currentProfile = netspanServer.getCurrentNetworkProfileName(enb);
		networkParams.setProfileName(currentProfile);
		isSucceeded = netspanServer.updateNetworkProfile(enb, currentProfile,networkParams);			
		return isSucceeded;
	}
	
	public boolean updateRadioProfile(EnodeB enb, RadioParameters radioParams){
		return netspanServer.updateRadioProfile(enb,radioParams);			
	}
	
	/**
	 * Change cell's PciRangesHomeEnb config.
	 * 
	 * @author Dor Shalom
	 */
	public boolean addPciRangeHomeEnbConfig(EnodeB enb, int startPci, int endPci, int configType, int value) {
		boolean isSucceeded;
		NeighbourManagementParameters neighbourManagementParameters = new NeighbourManagementParameters();
		HomeEnbPci homeEnbPci = neighbourManagementParameters.new HomeEnbPci();
		homeEnbPci.pciStart = startPci;
		homeEnbPci.pciEnd = endPci;
		switch (configType) {
		case 1:
			HomeEnbBand homeEnbBand = neighbourManagementParameters.new HomeEnbBand();
			homeEnbBand.band = value;
			homeEnbBand.pciList = new ArrayList<HomeEnbPci>();
			homeEnbBand.pciList.add(homeEnbPci);
			neighbourManagementParameters.homeEnbBandList = neighbourManagementParameters.new HomeEnbBandList();
			neighbourManagementParameters.homeEnbBandList.HomeEnbBandList.add(homeEnbBand);
			break;
		case 2:
			HomeEnbEarfcn homeEnbEarfcn = neighbourManagementParameters.new HomeEnbEarfcn();
			homeEnbEarfcn.earfcn = value;
			homeEnbEarfcn.pciList = new ArrayList<HomeEnbPci>();
			homeEnbEarfcn.pciList.add(homeEnbPci);
			neighbourManagementParameters.homeEnbEarfcnList = neighbourManagementParameters.new HomeEnbEarfcnList();
			neighbourManagementParameters.homeEnbEarfcnList.HomeEnbEarfcnList.add(homeEnbEarfcn);
			break;
		default:
			neighbourManagementParameters.homeEnbDefaultConfig = neighbourManagementParameters.new HomeEnbDefaultConfig();
			neighbourManagementParameters.homeEnbDefaultConfig.HomeEnbDefaultConfig.add(homeEnbPci);
			break;
		}
		try {
			report.report(String.format(
					"%s - Adding PciRangeHomeEnb config via Netspan: start-%s | end-%s | type-%s | value-%s",
					enb.getName(), startPci, endPci, configType, value));
			String currentProfile = netspanServer.getNeighborManagmentProfile(enb.getNetspanName());
			if (currentProfile.equals(addCloneSuffix(enb, enb.defaultNetspanProfiles.getNeighbourManagement())))
				isSucceeded = netspanServer.updateNeighborManagementProfile(enb, currentProfile,
						neighbourManagementParameters);
			else
				isSucceeded = cloneAndSetNeighbourManagementProfileViaNetspan(enb, currentProfile,
						neighbourManagementParameters);
			if (!isSucceeded) {
				String rowStatusOid = MibReader.getInstance().resolveByName("asLteStkPciRangesHomeEnbRowStatus");
				HashMap<String, String> params = new HashMap<String, String>();
				params.put(MibReader.getInstance().resolveByName("asLteStkPciRangesHomeEnbPciStart28BitNrt"),
						String.valueOf(startPci));
				params.put(MibReader.getInstance().resolveByName("asLteStkPciRangesHomeEnbPciEnd28BitNrt"),
						String.valueOf(endPci));
				params.put(MibReader.getInstance().resolveByName("asLteStkPciRangesHomeEnbConfigurationType"),
						String.valueOf(configType));
				params.put(MibReader.getInstance().resolveByName("asLteStkPciRangesHomeEnbEarfcnOrBand"),
						String.valueOf(value));
				report.report(String.format("%s - Failed to add PciRangeHomeEnb config via Netspan adding via SNMP.",
						enb.getName()));
				isSucceeded = enb.addRowInTable(rowStatusOid, params);
			}
			return isSucceeded;
		} catch (IOException e) {
			e.printStackTrace();
			report.report("Failed to set value via snmp", Reporter.FAIL);
			return false;
		}
	}

	/**
	 * Change cell's PciRangesForNrt config.
	 * 
	 * @author Dor Shalom
	 */
	public boolean addPciRangesForNrtEnbConfig(EnodeB enb, int startPci, int endPci, boolean x2Allowed, int hoType,
			int configType, int value) {
		boolean isSucceeded;
		NeighbourManagementParameters neighbourManagementParameters = new NeighbourManagementParameters();
		NrtPci nrtPci = neighbourManagementParameters.new NrtPci();
		nrtPci.pciStart = startPci;
		nrtPci.pciEnd = endPci;
		nrtPci.allowX2 = x2Allowed ? EnabledStates.ENABLED : EnabledStates.DISABLED;
		nrtPci.hoType = hoType == 0 ? NrtHoTypes.BLOCK_HO
				: hoType == 1 ? NrtHoTypes.X_2_PREFERRED : NrtHoTypes.S_1_ONLY;
		switch (configType) {
		case 1:
			NrtBand nrtBand = neighbourManagementParameters.new NrtBand();
			nrtBand.band = value;
			nrtBand.pciList = new ArrayList<NrtPci>();
			nrtBand.pciList.add(nrtPci);
			neighbourManagementParameters.nrtBandList = neighbourManagementParameters.new NrtBandList();
			neighbourManagementParameters.nrtBandList.NrtBandList.add(nrtBand);
			break;
		case 2:
			NrtEarfcn nrtEarfcn = neighbourManagementParameters.new NrtEarfcn();
			nrtEarfcn.earfcn = value;
			nrtEarfcn.pciList = new ArrayList<NrtPci>();
			nrtEarfcn.pciList.add(nrtPci);
			neighbourManagementParameters.nrtEarfcnList = neighbourManagementParameters.new NrtEarfcnList();
			neighbourManagementParameters.nrtEarfcnList.NrtEarfcnList.add(nrtEarfcn);
			break;
		default:
			neighbourManagementParameters.nrtDefaultConfig = neighbourManagementParameters.new NrtDefaultConfig();
			neighbourManagementParameters.nrtDefaultConfig.NrtDefaultConfig.add(nrtPci);
			break;
		}
		try {
			report.report(String.format(
					"%s - Adding PciRangeForNrt config via Netspan: start-%s | end-%s | x2Allowd-%s | hoType-%s | type-%s | value-%s",
					enb.getName(), startPci, endPci, x2Allowed, hoType, configType, value));
			String currentProfile = netspanServer.getNeighborManagmentProfile(enb.getNetspanName());
			if (currentProfile.equals(addCloneSuffix(enb, enb.defaultNetspanProfiles.getNeighbourManagement())))
				isSucceeded = netspanServer.updateNeighborManagementProfile(enb, currentProfile,
						neighbourManagementParameters);
			else
				isSucceeded = cloneAndSetNeighbourManagementProfileViaNetspan(enb, currentProfile,
						neighbourManagementParameters);
			if (!isSucceeded) {
				String rowStatusOid = MibReader.getInstance().resolveByName("asLteStkPciRangesForNrtEnbRowStatus");
				String x2Value = x2Allowed ? "1" : "0";
				HashMap<String, String> params = new HashMap<String, String>();
				params.put(MibReader.getInstance().resolveByName("asLteStkPciRangesForNrtEnbPciStartNrt"),
						String.valueOf(startPci));
				params.put(MibReader.getInstance().resolveByName("asLteStkPciRangesForNrtEnbPciEndNrt"),
						String.valueOf(endPci));
				params.put(MibReader.getInstance().resolveByName("asLteStkPciRangesForNrtEnbAllowX2"),
						String.valueOf(x2Value));
				params.put(MibReader.getInstance().resolveByName("asLteStkPciRangesForNrtEnbHoType"),
						String.valueOf(hoType));
				params.put(MibReader.getInstance().resolveByName("asLteStkPciRangesForNrtEnbConfigurationType"),
						String.valueOf(configType));
				params.put(MibReader.getInstance().resolveByName("asLteStkPciRangesForNrtEnbEarfcnOrBand"),
						String.valueOf(value));
				report.report(String.format("%s - Failed to add PciRangeForNrt config via Netspan adding via SNMP.",
						enb.getName()));
				isSucceeded = enb.addRowInTable(rowStatusOid, params);
			}
			return isSucceeded;
		} catch (IOException e) {
			e.printStackTrace();
			report.report("Failed to set value via snmp", Reporter.FAIL);
			return false;
		}
	}

	/**
	 * Change cell's PciRangesForNrt config.
	 * 
	 * @author Dor Shalom
	 */
	public boolean returnPciRangesConfigToDefault(EnodeB enb) {
		boolean isSucceeded = true;
		try {
			isSucceeded = setProfile(enb, EnbProfiles.Neighbour_Management_Profile,
					enb.defaultNetspanProfiles.getNeighbourManagement());
			if (!isSucceeded) {
				isSucceeded = enb.deletAllRowsInTable(
						MibReader.getInstance().resolveByName("asLteStkPciRangesForNrtEnbRowStatus"));
				isSucceeded = enb.deletAllRowsInTable(
						MibReader.getInstance().resolveByName("asLteStkPciRangesHomeEnbRowStatus"));
				isSucceeded = enb.addNewEntry(MibReader.getInstance().resolveByName("asLteStkPciRangesForNrtEnbRowStatus"));
				isSucceeded = enb.addNewEntry(MibReader.getInstance().resolveByName("asLteStkPciRangesForNrtEnbRowStatus"));
			}
			return isSucceeded;
		} catch (IOException e) {
			e.printStackTrace();
			report.report("Failed to set value via snmp", Reporter.FAIL);
			return false;
		}
	}

	/**
	 * Add IMMCI config.
	 * 
	 * @author Dor Shalom
	 */
	public boolean addImmciConfig(EnodeB enb, int startPci, int endPci, boolean x2Allowed, int HoType, int configType,
			int value) {
		// TODO:try first set with NMS
		try {
			String rowStatusOid = "1";
			String x2Value = x2Allowed ? "1" : "0";
			HashMap<String, String> params = new HashMap<String, String>();
			params.put(MibReader.getInstance().resolveByName("asLteStkPciRangesForNrtEnbPciStartNrt"),
					String.valueOf(startPci));
			params.put(MibReader.getInstance().resolveByName("asLteStkPciRangesForNrtEnbPciEndNrt"),
					String.valueOf(endPci));
			params.put(MibReader.getInstance().resolveByName("asLteStkPciRangesForNrtEnbAllowX2"),
					String.valueOf(x2Value));
			params.put(MibReader.getInstance().resolveByName("asLteStkPciRangesForNrtEnbHoType"),
					String.valueOf(HoType));
			params.put(MibReader.getInstance().resolveByName("asLteStkPciRangesForNrtEnbConfigurationType"),
					String.valueOf(configType));
			params.put(MibReader.getInstance().resolveByName("asLteStkPciRangesForNrtEnbEarfcnOrBand"),
					String.valueOf(value));
			report.report(String.format(
					"Adding PciRangeForNrt config:start-%s | end-%s | x2Allowd-%s | hoType-%s | type-%s | value-%s",
					startPci, endPci, x2Allowed, HoType, configType, value));
			return enb.addRowInTable(rowStatusOid, params);
		} catch (IOException e) {
			e.printStackTrace();
			report.report("Failed to set value via snmp", Reporter.FAIL);
			return false;
		}
	}

	/**
	 * Re-Provision.
	 * 
	 * @author Dor Shalom
	 */
	public boolean performReProvision(EnodeB node) {
		return netspanServer.performReProvision(node.getNetspanName());
	}

	public boolean cloneAndSetManagementProfileViaNetSpan(EnodeB node, String cloneFromName,
			ManagementParameters managementParams) {

		return cloneAndSetProfileViaNetSpan(node, cloneFromName, managementParams);
	}

	/**
	 * Management Profile - get maintenanceWinMode.
	 * 
	 * @author Avichai Yefet
	 */
	public Boolean getMaintenanceWinMode(EnodeB enb) {
		try {
			GeneralUtils.printToConsole("Get maintenanceWinMode");
			String oid = MibReader.getInstance().resolveByName("asLteStkMaintenanceWinCfgMode");
			String res = enb.getSNMP(oid);
			return GeneralUtils.stringToBoolean(res);
		} catch (Exception e) {
			e.printStackTrace();
			report.report("Failed to get value via snmp", Reporter.WARNING);
			return null;
		}
	}

	/**
	 * Management Profile - get mainWinStart.
	 * 
	 * @author Avichai Yefet
	 */
	public String getMainWinStart(EnodeB enb) {
		try {
			GeneralUtils.printToConsole("Get mainWinStart");
			String oid = MibReader.getInstance().resolveByName("asLteStkMaintenanceWinCfgStart");
			return enb.getSNMP(oid);
		} catch (Exception e) {
			e.printStackTrace();
			report.report("Failed to get value via snmp", Reporter.WARNING);
			return null;
		}
	}

	/**
	 * Management Profile - get mainWinStart.
	 * 
	 * @author Avichai Yefet
	 */
	public int getMainWinDuration(EnodeB enb) {
		try {
			GeneralUtils.printToConsole("Get mainWinDuration");
			String oid = MibReader.getInstance().resolveByName("asLteStkMaintenanceWinCfgDuration");
			return Integer.valueOf(enb.getSNMP(oid));
		} catch (Exception e) {
			e.printStackTrace();
			report.report("Failed to get value via snmp", Reporter.WARNING);
			return GeneralUtils.ERROR_VALUE;
		}
	}

	/**
	 * Management Profile - set delayReboot.
	 * 
	 * @author Avichai Yefet
	 */
	public boolean setDelayReboot(EnodeB enb, int value) {
		// TODO: try first set with NMS
		try {
			report.report("Set delayReboot to " + value);
			String oid = MibReader.getInstance().resolveByName("asLteStkMaintenanceWinCfgDelayReboot");
			return enb.snmpSet(oid, value);
		} catch (Exception e) {
			e.printStackTrace();
			report.report("Failed to set value via snmp", Reporter.WARNING);
			return false;
		}
	}
	
	/**
	 * Management Profile - get delayReboot.
	 * 
	 * @author Avichai Yefet
	 */
	public int getDelayReboot(EnodeB enb) {
		try {
			String oid = MibReader.getInstance().resolveByName("asLteStkMaintenanceWinCfgDelayReboot");
			return Integer.valueOf(enb.getSNMP(oid));
		} catch (Exception e) {
			e.printStackTrace();
			report.report("Failed to set value via snmp", Reporter.WARNING);
			return GeneralUtils.ERROR_VALUE;
		}
	}

	/**
	 * Management Profile - is Reboot Required.
	 * 
	 * @author Avichai Yefet
	 */
	public int isRebootRequiredMaintenanceWinStatus(EnodeB enb) {
		return isRebootRequiredMaintenanceWinStatus(enb, 40);
	}

	public int isRebootRequiredMaintenanceWinStatus(EnodeB enb, int cellIndex) {
		try {
			String oid = MibReader.getInstance().resolveByName("asLteStkCellMaintenanceWinStatusRebootRequired");
			oid += ".";
			oid += cellIndex;
			String res = enb.getSNMP(oid);
			return Integer.valueOf(res);
		} catch (Exception e) {
			e.printStackTrace();
			return GeneralUtils.ERROR_VALUE;
		}
	}

	/**
	 * Management Profile - is Reboot Ready.
	 * 
	 * @author Avichai Yefet
	 */
	public int isRebootReadyMaintenanceWinStatus(EnodeB enb) {
		return isRebootReadyMaintenanceWinStatus(enb, 40);
	}

	public int isRebootReadyMaintenanceWinStatus(EnodeB enb, int cellIndex) {
		try {
			String oid = MibReader.getInstance().resolveByName("asLteStkCellMaintenanceWinStatusRebootReady");
			oid += ".";
			oid += cellIndex;
			return Integer.valueOf(enb.getSNMP(oid));
		} catch (Exception e) {
			e.printStackTrace();
			report.report("Failed to get value via snmp", Reporter.WARNING);
			return GeneralUtils.ERROR_VALUE;
		}
	}

	/**
	 * get isOngoingEmergencyCall.
	 * 
	 * @author Avichai Yefet
	 */
	public Boolean isOngoingEmergencyCall(EnodeB enb) {
		return isOngoingEmergencyCall(enb, 40);
	}

	public Boolean isOngoingEmergencyCall(EnodeB enb, int cellIndex) {
		try {
			report.report("Is Ongoing Emergency Call");
			String oid = MibReader.getInstance().resolveByName("asLteStkCellStatusIsOngoingEmergencyCall");
			oid += ".";
			oid += cellIndex;
			return GeneralUtils.stringToBoolean(enb.getSNMP(oid));
		} catch (Exception e) {
			e.printStackTrace();
			report.report("Failed to get value via snmp", Reporter.WARNING);
			return null;
		}
	}

	/**
	 * set isOngoingEmergencyCall.
	 * 
	 * @author Avichai Yefet
	 */
	public Boolean setIsOngoingEmergencyCall(EnodeB enb, boolean value){
		return setIsOngoingEmergencyCall(enb, 40, value);
	}
	public boolean setIsOngoingEmergencyCall(EnodeB enb, int cellIndex, boolean value) {
		try {
			String oid = MibReader.getInstance().resolveByName("asLteStkCellStatusIsOngoingEmergencyCall");
			oid += ".";
			oid += cellIndex;
			return enb.snmpSet(oid, GeneralUtils.booleanToInteger(value));
		} catch (Exception e) {
			e.printStackTrace();
			report.report("Failed to set value via snmp", Reporter.WARNING);
			return false;
		}
	}

	/**
	 * Management Profile - set nmsRebootRequired.
	 * 
	 * @author Avichai Yefet
	 */
	public Boolean setNmsRebootRequired(EnodeB enb, boolean value){
		return setNmsRebootRequired(enb, value, 40);
	}
	public boolean setNmsRebootRequired(EnodeB enb, boolean value, int cellIndex) {
		try {
			report.report("Set nmsRebootRequired to " + value);
			String oid = MibReader.getInstance().resolveByName("asLteStkCellMaintenanceWinCfgNmsRebootRequired");
			oid += ".";
			oid += cellIndex;
			return enb.snmpSet(oid, GeneralUtils.booleanToInteger(value));
		} catch (IOException e) {
			e.printStackTrace();
			report.report("Failed to set value via snmp", Reporter.WARNING);
			return false;
		}
	}

	/**
	 * AutoPciCell - get PCI.
	 * 
	 * @author Avichai Yefet
	 */
	public int getPciFromAutoPciCell(EnodeB enb){
		return getPciFromAutoPciCell(enb, 40);
	}
	public int getPciFromAutoPciCell(EnodeB enb, int cellIndex){
		try {
			report.report("Get Pci From AutoPciCell");
			String oid = MibReader.getInstance().resolveByName("asLteStkChannelStatusPci");
			oid += ".";
			oid += cellIndex;
			return Integer.valueOf(enb.getSNMP(oid));
		} catch (Exception e) {
			e.printStackTrace();
			report.report("Failed to get value via snmp", Reporter.WARNING);
			return GeneralUtils.ERROR_VALUE;
		}
	}
	
	/**
	 * Clone And Set Neighbour Management Profile Via Netspan.
	 * 
	 * @author Dor Shalom
	 */
	public boolean cloneAndSetNeighbourManagementProfileViaNetspan(EnodeB node, String cloneFromName,
			NeighbourManagementParameters nghMgmtParams) {
		
		return cloneAndSetProfileViaNetSpan(node, cloneFromName, nghMgmtParams);
	}

	public boolean cloneAndSetSystemDefaultProfileViaNetSpan(EnodeB node, String cloneFromName,
			SystemDefaultParameters systemDefaultParams) {

		return cloneAndSetProfileViaNetSpan(node, cloneFromName, systemDefaultParams);
	}

	/**
	 * method gets node and a map of cells with netspan profiles. method changes
	 * node whole configuration with the new profiles.
	 * 
	 * @author Shahaf Shuhamy
	 * @param dut
	 * @param newProfilesToSet
	 */
	public boolean setNodeConfiguration(EnodeB dut, HashMap<Integer, INetspanProfile> newProfilesToSet) {
		HashMap<INetspanProfile, Integer> profilesMap = new HashMap<INetspanProfile, Integer>();
		for (Integer cell : newProfilesToSet.keySet()) {
			profilesMap.put(newProfilesToSet.get(cell), cell);
		}
		return netspanServer.setProfile(dut, profilesMap);

	}

	/**
	 * @author Meital Mizrachi
	 */
	public boolean cloneAndSetAdvancedProfileViaNetspan(EnodeB node, String cloneFromName,
			EnodeBAdvancedParameters advancedParams) {

		return cloneAndSetProfileViaNetSpan(node, cloneFromName, advancedParams);
	}

	public boolean verifyIsIcic(String nodeName, String nghName, boolean expectedState) {
		NeighborData nghData = netspanServer.getNeighborData(nodeName, nghName);
		report.report(String.format("%s - Interfering Neighbor of %s is %s", nodeName, nghName, nghData.interferingNeighbor));
		return nghData.interferingNeighbor.equals(expectedState);		
	}
	
	public boolean verifyAvgRsrp(String nodeName, String nghName, boolean isUeConnected) {
		NeighborData nghData = netspanServer.getNeighborData(nodeName, nghName);
		report.report(String.format("%s - Average RSRP of %s is %s", nodeName, nghName, nghData.avgRsrp));
		return isUeConnected ? nghData.avgRsrp > -140: nghData.avgRsrp == -140;
	}
	
	public boolean verifyUnmanagedInterference(String nodeName, int nghPci) {
		boolean isUnmanaged = netspanServer.getDicicUnmanagedInterferenceStatus(nodeName, nghPci);
		return isUnmanaged;
	}

	public boolean changeEnbCellRSI(EnodeB enb, int rsi) {
		report.report("Cell ID " + enb.getCellContextID() + ": Setting RSI=" + rsi);
		EnbCellProperties cellProperties = new EnbCellProperties();
		cellProperties.prachRsi = rsi;
		if (netspanServer.setEnbCellProperties(enb, cellProperties)) {
			report.report("Set RSI success with netspan");
			return true;
		}
		return enb.setRSI(rsi);
	}
	
	public boolean setAccessClassBarring(EnodeB enb, CellBarringPolicyParameters cellbarringPrameters) {
		if (netspanServer.setEnbAccessClassBarring(enb, cellbarringPrameters)) {
			report.report("Set cell Barring policy success with netspan");
			return true;
		}
		else {
			report.report("Set cell Barring policy didn't success with netspan", Reporter.WARNING);
			return false;
		}
	}
	
	public boolean setAutoRsiCellRsiValue(EnodeB eNodeB, int value) {
		return setAutoRsiCellRsiValue(eNodeB, 40, value);
	}
	public boolean setAutoRsiCellRsiValue(EnodeB eNodeB, int cellId, int value) {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellAutoRsiCfgRsiValue");
		oid += ("." + cellId);
		try {
			return eNodeB.snmpSet(oid, value);
		} catch (IOException e) {
			GeneralUtils.printToConsole("Error setting AutoRsiCellRsiValue: " + e.getMessage());
			e.printStackTrace();
		}
		return false;
	}
	
	public int getAutoRsiCellRsiValue(EnodeB eNodeB) {
		return getAutoRsiCellRsiValue(eNodeB, 40);
	}
	public int getAutoRsiCellRsiValue(EnodeB eNodeB, int cellId) {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellAutoRsiCfgRsiValue");
		oid += ("." + cellId);
		try {
			return Integer.valueOf(eNodeB.getSNMP(oid));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return GeneralUtils.ERROR_VALUE;
	}
	
	
	public boolean setsonCellCfgMlbActive(EnodeB eNodeB, int value) {
		return setsonCellCfgMlbActive(eNodeB, 40, value);
	}
	public boolean setsonCellCfgMlbActive(EnodeB eNodeB, int cellId, int value) {
		String oid = MibReader.getInstance().resolveByName("asLteStkSonCfgMlbActive");
		oid += ("." + cellId);
		try {
			return eNodeB.snmpSet(oid, value);
		} catch (IOException e) {
			GeneralUtils.printToConsole("Error setting sonCellCfgMlbActive: " + e.getMessage());
			e.printStackTrace();
		}
		return false;
	}
	
	public int getsonCellCfgMlbActive(EnodeB eNodeB) {
		return getsonCellCfgMlbActive(eNodeB, 40);
	}
	public int getsonCellCfgMlbActive(EnodeB eNodeB, int cellId) {
		String oid = MibReader.getInstance().resolveByName("asLteStkSonCfgMlbActive");
		oid += ("." + cellId);
		try {
			return Integer.valueOf(eNodeB.getSNMP(oid));
		} catch (Exception e) {
			GeneralUtils.printToConsole("Error getting sonCellCfgMlbActive: " + e.getMessage());
			e.printStackTrace();
		}
		return GeneralUtils.ERROR_VALUE;
	}
	/**
	 * Disable dynamic CFI via Snmp only
	 * @author Shahaf Shuhamy
	 */
	public void disableDynamicCFI(EnodeB dut) {
		try {
			if(dut.isDynamicCFIEnable()){
				if(dut.disableDynamicCFI()){
					report.report("Disable Dynamic CFI Successfully");
					GeneralUtils.startLevel("Rebooting Node after Disabling Dynamic CFI");
					dut.reboot();
					//Heng - dont worry about the wait this is a safe switch to wait for reboot 
					//the wair for allrunnig itself will take 5 minutes so this wait will not affect runtime
					//but will help up to make the code simplier and not wait for reboot event at this point
					GeneralUtils.unSafeSleep(2*60*1000);
					dut.waitForAllRunning(EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
					GeneralUtils.stopLevel();
				}else{
					report.report("Could not Disable Dynamic CFI",Reporter.WARNING);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
			report.report("Error while trying to set SNMP value!",Reporter.WARNING);
		}
	}

	public int getDLTPTPerQci(String nodeName, int qci){
		return netspanServer.getDLTPTPerQci(nodeName, qci);
	}
	
	public Pair<Integer,Integer> getUlDlTrafficValues(String nodeName){
		return netspanServer.getUlDlTrafficValues(nodeName);
	}
	
	/**
	 * Enable dynamic CFI via Snmp only
	 * @author Hen Goldbrud
	 */
	public void enableDynamicCFI(EnodeB dut) {
		try {
			if(!dut.isDynamicCFIEnable()){
				if(dut.enableDynamicCFI()){
					report.report("enabled Dynamic CFI Successfully");
					GeneralUtils.startLevel("Rebooting Node after enabled Dynamic CFI");
					dut.reboot();
					dut.waitForAllRunning(EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
					GeneralUtils.stopLevel();
				}else{
					report.report("Could not Enable Dynamic CFI",Reporter.WARNING);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
			report.report("Error while trying to set SNMP value!",Reporter.WARNING);
		}
	}

	public HardwareCategory getHardwareCategory (EnodeB node){
		HardwareCategory hardwareCategory = null;
		try {
			hardwareCategory = netspanServer.getHardwareCategory(node);
		} catch (Exception e) {
			report.report("couldn't get hardware category value");
			e.printStackTrace();
			return null;
		}
		return hardwareCategory;
	}
	
	public boolean configureOnlyANRtoEnableViaNms(EnodeB node, String startLevelReport, List<Integer> EARFCNs) throws IOException {
		boolean flag = false;
		SonParameters sonParams = new SonParameters();
		sonParams.setSonCommissioning(true);
		sonParams.setAnrState(SonAnrStates.PERIODICAL_MEASUREMENT);
		sonParams.setAnrFrequencyList(EARFCNs);
		try {
			report.startLevel(startLevelReport);
			flag = this.cloneAndSetSonProfileViaNetspan(node, node.getDefaultNetspanProfiles().getSON(),sonParams);
		} catch (Exception e) {
			e.printStackTrace();
			report.report("Failed to configure ANR to enable via NMS due to: " + e.getMessage(), Reporter.FAIL);
		} finally {
			report.stopLevel();
		}
		return flag;
	}
	
	public void showANRstate(EnodeB node, String ANRstate) throws IOException {
		String state = node.lteCli("cell show anrstate");
		if(state.contains(ANRstate)){
			report.report(ANRstate);
		}
		else {
			report.report(state, Reporter.WARNING);
		}
	}
	
	public void revertToDefaultSonProfile(EnodeB dut) {
		try {
			report.startLevel("Revert to default SON Profile");
			dut.setExpectBooting(true);
			this.setProfile(dut, EnbProfiles.Son_Profile, dut.defaultNetspanProfiles.getSON());
			report.stopLevel();
		} catch (Exception e) {
			e.printStackTrace();
			report.report("Failed to revert to default SON Profile due to: " + e.getMessage(), Reporter.FAIL);
		}
	}
	
	public void revertToDefaultProfile(EnodeB dut, EnbProfiles enbProfile) {
		try {
			report.startLevel("Revert to default " + enbProfile.name());
			this.setProfile(dut, enbProfile, dut.defaultNetspanProfiles.getDefaultProfile(enbProfile));
			report.stopLevel();
		} catch (Exception e) {
			e.printStackTrace();
			report.report("Failed to revert to default " + enbProfile.name() + " Profile due to: " + e.getMessage(), Reporter.FAIL);
		}
	}
	
	public boolean waitForAnrState(EnodeB dut, int timeOut, int anrState){
		String state = dut.lteCli("cell show anrstate");
		long startTime = System.currentTimeMillis();
		while(!state.contains("ANR STATE = "+anrState) && System.currentTimeMillis()-startTime<timeOut){
			GeneralUtils.unSafeSleep(5*1000);
			state = dut.lteCli("cell show anrstate");
			GeneralUtils.printToConsole(state);
		}
		GeneralUtils.printToConsole(state);
		return state.contains("ANR STATE = " + anrState);
	}
	
	public void printEnodebState(EnodeB dut, boolean inServiceExpect) {
		EnbStates enbState = dut.getServiceState(); 
		boolean flag = enbState == EnbStates.IN_SERVICE;
		String reportStr = dut.getName() + " state is " + enbState;
		if (flag == inServiceExpect)
			report.report(reportStr);
		else
			report.report(reportStr, Reporter.WARNING);
	}
	
	public boolean isDuplexFdd(EnodeB node) {
		try{
			int band = node.getBand();
			GeneralUtils.printToConsole("Band value : "+band);
			if( 51 >= band && band >= 31){
				return false;
			}
			
		}catch(Exception e){
			GeneralUtils.printToConsole("Error while trying to get band number from SNMP");
			GeneralUtils.printToConsole("Using Default TDD mode");
			return false;
		}
		return true;
	}
	
	public int getNumberOfActiveCells(EnodeB node) {
		int netspanActiveCells = netspanServer.getNumberOfActiveCellsForNode(node);
		if(netspanActiveCells == 0) {
			return node.getNumberOfActiveCells();
		}
		return netspanActiveCells;
	}

	/**
	 * 
	 * @param Latitude
	 * @param Longitude
	 */
	public boolean setLatitudeAndLongitude(EnodeB node, BigDecimal Latitude, BigDecimal Longitude) {
		return netspanServer.setLatitudeAndLongitude(node, Latitude, Longitude); 
	}
	
	/**
	 * @return boolean - true: granularity period is the input wanted. 
	 * false: granularity period is NOT the input wanted.
	 */
	public boolean setGranularityPeriod(EnodeB enodeB, int value){
		boolean actionSet = true;
		GeneralUtils.startLevel("Setting granularity period to "+value+" minutes for enodeb "+enodeB.getNetspanName());
		if(enodeB.getGranularityPeriod()!=value){
			if(enodeB.setGranularityPeriod(value)){
				report.report("Succeeded to set granularity period");
				actionSet = true;
			}else{
				report.report("Failed to set granularity period");
				actionSet = false;
			}
		}else{
			report.report("Granularity period was already "+value+" minutes. No need to change");
			actionSet = true;
		}
		GeneralUtils.stopLevel();
		return actionSet;
	}
	
	public void waitGranularityPeriodTime(EnodeB enb){
		int waitingPeriodTime = enb.getGranularityPeriod();
		report.report("Wait for "+waitingPeriodTime+" minutes of granularity period");
		if(waitingPeriodTime<0){
			report.report("Can't wait negative time",Reporter.FAIL);
			return;
		}
		GeneralUtils.unSafeSleep(waitingPeriodTime*60*1000);
	}
	
	public boolean isCAEnableInNode(EnodeB node){
		Boolean result = false;
		result = isCAEnableWithNodeViaNetspan(node);
		if(result == null){
			result = isCAEnbaleWithNodeViaSNMP(node);
		}
		return result;
	}
	
	private boolean isCAEnbaleWithNodeViaSNMP(EnodeB node) {
		GeneralUtils.startLevel("trying to check CA enable with SNMP");
		
		boolean result = false;
		report.report("checking prach zero mode with the value 8");
		GeneralUtils.printToConsole("asLteStkCellSib2CfgPrachZeroCorrrelZoneCfg MIB get");
		String prach = node.getParchZeroCorrrelZone();
		report.report("prach value : "+prach);
		
		report.report("getting tdd ack");
		String tddAck = node.getTddAckMode();
		report.report("tdd ack value : "+tddAck);
		
		report.report("Getting CA mode");
		String caMode = node.getCAMode();
		if(caMode.equals("2")){
			report.report("ca mode is contiguous");
			result = true;
		}else{
			report.report("ca Mode : "+caMode);
		}
		
		GeneralUtils.stopLevel();
		return result;
		
	}

	/**
	 * false will be returned if the node is not ca,<br>
	 * true will be returned if the node is in ca mode,<br>
	 * NULL will be returned in the case of ERROR!
	 * @param node
	 * @return
	 */
	private Boolean isCAEnableWithNodeViaNetspan(EnodeB node){
		GeneralUtils.startLevel("trying to check CA enable with netspan");
		MultiCellParameters mc = null;
		try{
			String multiCellProfileName = netspanServer.getMultiCellProfileName(node);
			mc = ((Netspan_15_5_abilities)netspanServer).getMultiCellProfileObject(node,multiCellProfileName);
		}catch(ClassCastException e){
			report.report("could not access netspan method in current version!",Reporter.WARNING);
			report.report(e.getMessage());
			return null;
		}
		
		if(mc == null){
			report.report("could not get multiCell profile from netspan");
			return null;
		}
		
		boolean result = mc.getCarrierAggMode();
		report.report(node.getName() + " - CA is " + (result ? "" :"not " ) + "enabled.");
		GeneralUtils.stopLevel();
		return result;
	}
	
	public boolean rebootWithNetspanOnly(EnodeB node, RebootType rebootType, boolean expectingBoot, long timeout) {
		boolean rebooted = false;
		node.setExpectBooting(expectingBoot);
		
		try {
			rebooted = NetspanServer.getInstance().resetNode(node.getNetspanName(),rebootType);
		} catch (Exception e) {
			report.report("Failed to reset Node Via Netspan", Reporter.WARNING);
			e.printStackTrace();
		}
		
		rebooted = rebooted && node.waitForReboot(timeout);
		node.setExpectBooting(false);
		
		if (rebooted){			
			report.report("Node " + node.getNetspanName() + " has been rebooted via Netspan.");
		}else{
			report.report("The Enodeb " + node.getName() + " failed to reboot!");
		}
		
		return rebooted;
	}
	
	public boolean convertToPnPConfig(EnodeB enb){
		return netspanServer.convertToPnPConfig(enb);
	}
	
	public List<LteBackhaul> getBackhaulInterfaceStatus(EnodeB enb) {
		return netspanServer.getBackhaulInterfaceStatus(enb);
	}
	
	public Integer getEARFCNforNode(EnodeB node) {
		Integer earfcn = 0;
		RadioParameters rp = getRadioProfile(node);
		earfcn = rp.getEarfcn();
		if(earfcn == null) {
			//SNMP fall back
			earfcn =  node.getEarfcn();
		}
		return earfcn;
	}
}
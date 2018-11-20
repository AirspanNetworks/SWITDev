package Netspan.NBI_14_50;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import EnodeB.EnodeB;
import EnodeB.EnodeBChannelBandwidth;
import EnodeB.EnodeBUpgradeImage;
import EnodeB.EnodeBUpgradeServer;
import EnodeB.ServiceState;
import Netspan.EnbProfiles;
import Netspan.API.Lte.AlarmInfo;
import Netspan.API.Enums.CategoriesLte;
import Netspan.API.Enums.EnabledDisabledStates;
import Netspan.API.Enums.EnbTypes;
import Netspan.API.Enums.HandoverType;
import Netspan.API.Enums.HardwareCategory;
import Netspan.API.Enums.HoControlStateTypes;
import Netspan.API.Enums.ImageType;
import Netspan.API.Enums.NodeManagementModeType;
import Netspan.API.Enums.PrimaryClockSourceEnum;
import Netspan.API.Enums.SecurityProfileOptionalOrMandatory;
import Netspan.API.Enums.SnmpAgentVersion;
import Netspan.API.Enums.SonAnrStates;
import Netspan.API.Enums.StopGapEventTypes;
import Netspan.API.Enums.TriggerGapEventTypes;
import Netspan.API.Enums.TriggerQuantityTypes;
import Netspan.API.Enums.X2ControlStateTypes;
import Netspan.API.Lte.EnbCellProperties;
import Netspan.API.Lte.EventInfo;
import Netspan.API.Lte.LteBackhaul;
import Netspan.API.Lte.MobilityConnectedModeStopGaps;
import Netspan.API.Lte.MobilityConnectedModeTriggerGaps;
import Netspan.API.Lte.NodeInfo;
import Netspan.API.Lte.PTPStatus;
import Netspan.API.Lte.RFStatus;
import Netspan.API.Lte.SONStatus;
import Netspan.API.Lte.IRetunTypes.ILteEnbDetailsGet;
import Netspan.API.Lte.IRetunTypes.ILteEnbDetailsSet;
import Netspan.API.Software.SoftwareStatus;
import Netspan.DataObjects.NeighborData;
import Netspan.NBI_14_50.API.Lte.PnpHardwareTypes;
import Netspan.NBI_14_50.API.Lte.ProfileResponse;
import Netspan.NBI_14_50.API.Lte.S1ListContainer;
import Netspan.NBI_14_50.API.Lte.LteCellSetWs;
import Netspan.NBI_14_50.API.Lte.AnrFreq;
import Netspan.NBI_14_50.API.Lte.PciRange;
import Netspan.NBI_14_50.NBIHelper;
import Netspan.NBI_14_50.API.FaultManagement.Alarm;
import Netspan.NBI_14_50.API.FaultManagement.AlarmActionResult;
import Netspan.NBI_14_50.API.FaultManagement.AlarmResultList;
import Netspan.NBI_14_50.API.Inventory.DiscoveryTaskActionResult;
import Netspan.NBI_14_50.API.Inventory.ErrorCodes;
import Netspan.NBI_14_50.API.Inventory.NodeActionResult;
import Netspan.NBI_14_50.API.Inventory.NodeDetailGetResult;
import Netspan.NBI_14_50.API.Inventory.WsResponse;
import Netspan.NBI_14_50.API.Lte.AnrFreqListContainer;
import Netspan.NBI_14_50.API.Lte.ClockSources;
import Netspan.NBI_14_50.API.Lte.Enb3RdParty;
import Netspan.NBI_14_50.API.Lte.EnbDetailsGet;
import Netspan.NBI_14_50.API.Lte.EnbMobilityProfile;
import Netspan.NBI_14_50.API.Lte.EnbNetworkProfile;
import Netspan.NBI_14_50.API.Lte.EnbPnpConfig;
import Netspan.NBI_14_50.API.Lte.EnbRadioProfile;
import Netspan.NBI_14_50.API.Lte.EnbSecurityProfile;
import Netspan.NBI_14_50.API.Lte.EnbSonProfile;
import Netspan.NBI_14_50.API.Lte.EnbStatesGet;
import Netspan.NBI_14_50.API.Lte.EnbSyncProfile;
import Netspan.NBI_14_50.API.Lte.Lte3RdPartyGetResult;
import Netspan.NBI_14_50.API.Lte.Lte3RdPartyResponse;
import Netspan.NBI_14_50.API.Lte.LteCSonEntryWs;
import Netspan.NBI_14_50.API.Lte.LteCellGetWs;
import Netspan.NBI_14_50.API.Lte.LteEnbDetailsSetWs;
import Netspan.NBI_14_50.API.Lte.LteNeighbourResponse;
import Netspan.NBI_14_50.API.Lte.LteNetworkProfileGetResult;
import Netspan.NBI_14_50.API.Lte.LtePlmnEntryWs;
import Netspan.NBI_14_50.API.Lte.LtePnpConfigGetResult;
import Netspan.NBI_14_50.API.Lte.LteRadioProfileGetResult;
import Netspan.NBI_14_50.API.Lte.LteS1EntryWs;
import Netspan.NBI_14_50.API.Lte.LteSecurityProfileGetResult;
import Netspan.NBI_14_50.API.Lte.LteSonCSonWs;
import Netspan.NBI_14_50.API.Lte.LteSyncProfileGetResult;
import Netspan.NBI_14_50.API.Lte.LteSyncProfileResult;
import Netspan.NBI_14_50.API.Lte.MobilityConnectedModeFreq;
import Netspan.NBI_14_50.API.Lte.NameResult;
import Netspan.NBI_14_50.API.Lte.NodeListResult;
import Netspan.NBI_14_50.API.Lte.NodeResultValues;
import Netspan.NBI_14_50.API.Lte.NodeSimple;
import Netspan.NBI_14_50.API.Lte.ObjectFactory;
import Netspan.NBI_14_50.API.Lte.PciRangeListContainer;
import Netspan.NBI_14_50.API.Lte.PlmnListContainer;
import Netspan.NBI_14_50.API.Lte.PnpConfigCreate;
import Netspan.NBI_14_50.API.Lte.PnpDetailWs;
import Netspan.NBI_14_50.API.Lte.RfProfileDuplexModes;
import Netspan.NBI_14_50.API.Lte.TddFrameConfigurationsSupported;
import Netspan.NBI_14_50.API.Server.NmsInfoResponse;
import Netspan.NBI_14_50.API.Status.LteAnrStatusWs;
import Netspan.NBI_14_50.API.Status.LteRfGetResult;
import Netspan.NBI_14_50.API.Status.LteRfStatusWs;
import Netspan.NBI_14_50.API.Status.LteSonAnrGetResult;
import Netspan.NBI_14_50.API.Status.LteUeCategory;
import Netspan.NBI_14_50.API.Status.LteUeGetResult;
import Netspan.NBI_14_50.API.Status.LteUeStatusWs;
import Netspan.NBI_14_50.API.Status.NodePtpGetResult;
import Netspan.NBI_14_50.API.Status.NodeSoftwareGetResult;
import Netspan.Profiles.ManagementParameters;
import Netspan.Profiles.CellAdvancedParameters;
import Netspan.Profiles.CellBarringPolicyParameters;
import Netspan.Profiles.EnodeBAdvancedParameters;
import Netspan.Profiles.INetspanProfile;
import Netspan.Profiles.MobilityParameters;
import Netspan.Profiles.MultiCellParameters;
import Netspan.Profiles.NeighbourManagementParameters;
import Netspan.Profiles.NetworkParameters;
import Netspan.Profiles.SecurityParameters;
import Netspan.Profiles.NetworkParameters.Plmn;
import Netspan.Profiles.RadioParameters;
import Netspan.Profiles.SonParameters;
import Netspan.Profiles.SyncParameters;
import Netspan.Profiles.SystemDefaultParameters;
import Utils.GeneralUtils;
import Utils.GeneralUtils.RebootType;
import Utils.Pair;
import jsystem.framework.IgnoreMethod;
import jsystem.framework.report.Reporter;

public class NetspanServer_14_5 extends Netspan.NetspanServer {

	/** The Constant NBI_PATH. */
	private static final String NBI_PATH = "WS/14.5";
	private static final int NO_CELL_ID_IS_NEEDED = -1;
	public NBIHelper helper_14_50;
	/**
	 * (non-Javadoc)
	 * 
	 * @see Utils.TestspanSystemObject#init()
	 */
	@Override
	public void init() throws Exception {
		if (NBI_VERSION == null) {
			NBI_VERSION = "14_5";
		}
		super.init();
		GeneralUtils.printToConsole("Initalizing NBIHelper with NBI Path " + NBI_PATH);
		this.helper_14_50 = new NBIHelper(getHostname(), NBI_PATH, getUsername(), getPassword());
		populateNodeNames();
	}

	/**
	 * Sets the operational status.
	 * 
	 * @param node
	 *            the node
	 * @param serviceState
	 *            the service state
	 * @return true, if successful
	 */
	@IgnoreMethod
	public boolean setNodeOperationalStatus(EnodeB node, ServiceState serviceState) {
		// set enable state via NBI
		if (serviceState == ServiceState.InService) {
			return setNodeServiceState(node, Netspan.API.Enums.EnbStates.IN_SERVICE);
		}
		if (serviceState == ServiceState.OutOfService) {
			return setNodeServiceState(node, Netspan.API.Enums.EnbStates.OUT_OF_SERVICE);
		}
		if (!node.dbSet("stackCfg", "1", "operationalStatus", serviceState.ordinal() + "")) {
			GeneralUtils.printToConsole("Couldn't change operational service (db didn't update).");
			return false;
		}
		return true;
	}

	/**
	 * Gets the neighbors name.
	 *
	 * @param node
	 *            the node
	 * @return the neighbors name
	 */
	public List<String> getNodeNeighborsName(EnodeB node) {
		List<LteAnrStatusWs> nghs = getNeighbourList(node);
		if (nghs == null) {
			report.report("Could not pull Neighbors Name from NBIF", Reporter.WARNING);

			return null;
		}
		ArrayList<String> names = new ArrayList<String>();
		for (LteAnrStatusWs ngh : nghs) {
			names.add(ngh.getName());
		}
		return names;
	}

	/**
	 * Clones radio profile via NETSPAN. for now it configures all the cells to
	 * the same new configurations enter negative numbers / null in arguments to
	 * use the current values of the cell
	 *
	 * @param node
	 *            the node
	 * @param cloneFromName
	 *            the clone from name
	 * @param band
	 *            the band
	 * @param bandwith
	 *            the bandwith
	 * @param earfcn
	 *            the earfcn
	 * @param downlink
	 *            the downlink
	 * @param uplink
	 *            the uplink
	 * @param mfbi
	 *            the mfbi
	 * @param txPower
	 *            the tx power
	 * @param sfr
	 *            the sfr
	 * @param fc
	 *            the fc
	 * @return true, if successful
	 */
	public boolean cloneRadioProfile(EnodeB node, String cloneFromName, RadioParameters radioParams){
		
		if (cloneFromName != null){
			GeneralUtils.printToConsole("Trying to create automation radio profile \"%s\" via NBIF , creating an automation profiles for the run."
					+cloneFromName);
		}
		else{
			GeneralUtils.printToConsole("Trying to create automation radio profile from the current Profile that in use via NBIF ,"
					+ " creating an automation profiles for the run.");
			cloneFromName = node.getDefaultNetspanProfiles().getRadio();
		}

		EnbRadioProfile radioProfile = CreateEnbRadioProfile(radioParams);
		if(radioProfile == null) return false;
		
		try {
			ProfileResponse cloneResult = ( ProfileResponse ) helper_14_50.execute( "radioProfileClone", cloneFromName, radioProfile );
			
			if ( cloneResult == null){
				report.report("Fail to get Radio profile cloning result", Reporter.WARNING );
				return false;
			}
			else if( cloneResult.getErrorCode() == Netspan.NBI_14_50.API.Lte.ErrorCodes.OK ) {
				report.report("Succeeded to clone Radio profile, new profile name: " + radioParams.getProfileName());
				return true;
			} else {
				report.report("Failed to clone Radio profile, reason: " + cloneResult.getErrorString(), Reporter.WARNING);
				return false;
			}
		} catch ( Exception e ) {
			GeneralUtils.printToConsole("Could not execute radioProfileClone on " + cloneFromName);
		}
		return false;
	}

	private EnbRadioProfile CreateEnbRadioProfile(RadioParameters radioParams) {
		
		ObjectFactory factoryDetails = new ObjectFactory();
		EnbRadioProfile radioProfile = new EnbRadioProfile();
		
		if(radioParams.getProfileName() == null){return null;}
		else{radioProfile.setName(radioParams.getProfileName());}
		
		if(radioParams.getBand() != null){radioProfile.setBand(factoryDetails.createEnbRadioProfileBand(radioParams.getBand()));}
		
		if(radioParams.getBandwidth() != null){radioProfile.setBandwidthMhz(factoryDetails.createEnbRadioProfileBandwidthMhz(radioParams.getBandwidth()));}
		
		if(radioParams.getDownLinkFrequency() != null){radioProfile.setDownlinkFreq(factoryDetails.createEnbRadioProfileDownlinkFreq(radioParams.getDownLinkFrequency()));}
		
		if(radioParams.getDuplex() != null){
			RfProfileDuplexModes duplexMode = radioParams.getDuplex().contains("1") ? RfProfileDuplexModes.FDD : RfProfileDuplexModes.TDD;
			radioProfile.setDuplexMode(factoryDetails.createEnbRadioProfileDuplexMode(duplexMode));
		}
		
		if(radioParams.getEarfcn() != null){radioProfile.setEarfcn(factoryDetails.createEnbRadioProfileEarfcn(radioParams.getEarfcn()));}
		
		
		if(radioParams.getFrameConfig() != null){
			TddFrameConfigurationsSupported fc = radioParams.getFrameConfig().contains("1") ? 
													TddFrameConfigurationsSupported.DL_40_UL_40_SP_20 :
														TddFrameConfigurationsSupported.DL_60_UL_20_SP_20 ;
			radioProfile.setFrameConfig(factoryDetails.createEnbRadioProfileFrameConfig(fc));
		}
		
		if(radioParams.getTxpower() != null){radioProfile.setTxPower(factoryDetails.createEnbRadioProfileTxPower(radioParams.getTxpower()));}
		
		if(radioParams.getUpLinkFrequency() != null){radioProfile.setUplinkFreq(factoryDetails.createEnbRadioProfileUplinkFreq(radioParams.getUpLinkFrequency()));}
		return radioProfile;
	}

	public boolean cloneProfile(EnodeB node, String cloneFromName, INetspanProfile profileParams){
		try{
			switch (profileParams.getType()) {
				case Management_Profile:
					return cloneManagementProfile(node, cloneFromName, (ManagementParameters)profileParams);
					
				case Mobility_Profile:
					return cloneMobilityProfile(node, cloneFromName, (MobilityParameters)profileParams);
					
				case Network_Profile:
					return cloneNetworkProfile(node, cloneFromName, (NetworkParameters)profileParams);
					
				case Radio_Profile:
					return cloneRadioProfile(node, cloneFromName, (RadioParameters)profileParams);
					
				case Security_Profile:
					return cloneSecurityProfile(node, cloneFromName, (SecurityParameters)profileParams);
					
				case Son_Profile:
					return cloneSonProfile(node, cloneFromName, (SonParameters)profileParams);
					
				case Sync_Profile:
					return cloneSyncProfile(node, cloneFromName, (SyncParameters)profileParams);
					
				case EnodeB_Advanced_Profile:
					return cloneEnodeBAdvancedProfile(node, cloneFromName,(EnodeBAdvancedParameters)profileParams);
					
				case Neighbour_Management_Profile:
					return cloneNeighborManagementProfile(node, cloneFromName, (NeighbourManagementParameters)profileParams);
					
				case MultiCell_Profile:
					return cloneMultiCellProfile(node, cloneFromName, (MultiCellParameters)profileParams);
					
				default:
					report.report("Enum: No Such EnbProfile", Reporter.WARNING);
					return false;
			}
		}
		catch(Exception e){
			e.printStackTrace();
			report.report("Error in cloneProfile: " + e.getMessage(), Reporter.WARNING);
			return false;
		}
	}
	

	public String getCurrentProfileName(EnodeB node, EnbProfiles profileType){
		try{
			switch (profileType) {
				case Management_Profile:
					return getCurrentManagmentProfileName(node);
					
				case Mobility_Profile:
					return getCurrentMobilityProfileName(node);
					
				case Network_Profile:
					return getCurrentNetworkProfileName(node);
					
				case Radio_Profile:
					return getCurrentRadioProfileName(node);
					
				case Security_Profile:
					return getCurrentSecurityProfileName(node);
					
				case Son_Profile:
					return getCurrentSonProfileName(node);
					
				case Sync_Profile:
					return getCurrentSyncProfileName(node);
					
				case EnodeB_Advanced_Profile:
					return getCurrentEnbAdvancedConfigurationProfileName(node);
					
				case Neighbour_Management_Profile:
					return getNeighborManagmentProfile(node.getNetspanName());
					
				default:
					report.report("Enum: No Such EnbProfile", Reporter.WARNING);
					return "";
			}
		}
		catch(Exception e){
			e.printStackTrace();
			report.report("Error in getCurrentProfileName: " + e.getMessage(), Reporter.WARNING);
			return "";
		}
	}
	
	/**
	 * Clone mobility profile via net span.
	 *
	 * @param node
	 *            the node
	 * @param intraFreqEventType
	 *            the intra freq event type
	 * @param intraFreqA3Offset
	 *            the intra freq a3 offset
	 * @param intraFreqHysteresis
	 *            the intra freq hysteresis
	 * @param intraFreqTimeToTrigger
	 *            the intra freq time to trigger
	 * @param cloneFromName
	 *            the clone from name
	 * @param UseIntraFrequencyProperties
	 *            the use intra frequency properties
	 * @param interFreqEventType
	 *            the inter freq event type
	 * @param interFreqA3Offset
	 *            the inter freq a3 offset
	 * @param interFreqHysteresis
	 *            the inter freq hysteresis
	 * @param interFreqTimeToTrigger
	 *            the inter freq time to trigger
	 * @param intraThreshold1
	 *            the intra threshold1
	 * @param intraThreshold2
	 *            the intra threshold2
	 * @param interThreshold1
	 *            the inter threshold1
	 * @param interThreshold2
	 *            the inter threshold2
	 * @return true, if successful
	 */
	public boolean cloneMobilityProfile(EnodeB node, String cloneFromName, MobilityParameters mobilityParams) {
		try{
			ObjectFactory factoryDetails = new ObjectFactory();
			EnbMobilityProfile mobilityProfile = new EnbMobilityProfile();
			MobilityConnectedModeFreq mobilityConnectedMode = null;
			if (mobilityParams.getEventType() != null) {
				mobilityConnectedMode = new MobilityConnectedModeFreq();
				mobilityConnectedMode.setEventType(factoryDetails
						.createMobilityConnectedModeFreqEventType(mobilityParams.getEventType()));
				switch (mobilityParams.getEventType()) {
				case A_3:
					if (mobilityParams.getA3Offset() != null) {
						mobilityConnectedMode.setA3Offset(factoryDetails.createMobilityConnectedModeFreqA3Offset(
								BigDecimal.valueOf(mobilityParams.getA3Offset())));
					}
					break;
				case A_4:
					if (mobilityParams.getRsrpEventThreshold1() != null) {
						mobilityConnectedMode.setRsrpEventThreshold1(
								factoryDetails.createMobilityConnectedModeFreqRsrpEventThreshold1(
										mobilityParams.getRsrpEventThreshold1()));
					}
					break;
				case A_5:
					if (mobilityParams.getRsrpEventThreshold1() != null) {
						mobilityConnectedMode.setRsrpEventThreshold1(
								factoryDetails.createMobilityConnectedModeFreqRsrpEventThreshold1(
										mobilityParams.getRsrpEventThreshold1()));
					}
					if (mobilityParams.getRsrpEventThreshold2() != null) {
						mobilityConnectedMode.setRsrpEventThreshold2(
								factoryDetails.createMobilityConnectedModeFreqRsrpEventThreshold2(
										mobilityParams.getRsrpEventThreshold2()));
					}
					break;
				default:
					break;
				}
				if (mobilityParams.getHysteresis() != null) {
					mobilityConnectedMode.setHysteresis(factoryDetails.createMobilityConnectedModeFreqHysteresis(
							BigDecimal.valueOf(mobilityParams.getHysteresis())));
				}
				if (mobilityParams.getIsIntra()) {
					mobilityProfile.setConnectedModeIntraFrequency(mobilityConnectedMode);
				}
				else{
					mobilityProfile.setConnectedModeInterFrequency(mobilityConnectedMode);
				}
			}
			
			if(mobilityParams.getThresholdBasedMeasurement()!=null){
				mobilityProfile.setIsThresholdBasedMeasurementEnabled(
						factoryDetails.createEnbMobilityProfileIsThresholdBasedMeasurementEnabled(
								mobilityParams.getThresholdBasedMeasurement()));
				
				//TODO:: remove HC values
				if(mobilityParams.getThresholdBasedMeasurement()==EnabledDisabledStates.ENABLED){
					MobilityConnectedModeTriggerGaps triggerGap = new MobilityConnectedModeTriggerGaps();
					triggerGap.setEventType(factoryDetails.createMobilityConnectedModeTriggerGapsEventType(TriggerGapEventTypes.A_2));
					triggerGap.setTriggerQuantity(factoryDetails.createMobilityConnectedModeTriggerGapsTriggerQuantity(TriggerQuantityTypes.RSRP));
					triggerGap.setRsrpEventThreshold1(factoryDetails.createMobilityConnectedModeTriggerGapsRsrpEventThreshold1(mobilityParams.getStartGap()));
					triggerGap.setHysteresis(factoryDetails.createMobilityConnectedModeTriggerGapsHysteresis(BigDecimal.valueOf(0.0)));
					triggerGap.setTimeToTrigger(factoryDetails.createMobilityConnectedModeTriggerGapsTimeToTrigger("320"));
					mobilityProfile.setConnectedModeThresholdTriggerGaps(triggerGap);
					
					MobilityConnectedModeStopGaps stopGap = new MobilityConnectedModeStopGaps();
					stopGap.setEventType(factoryDetails.createMobilityConnectedModeStopGapsEventType(StopGapEventTypes.A_1));
					stopGap.setTriggerQuantity(factoryDetails.createMobilityConnectedModeStopGapsTriggerQuantity(TriggerQuantityTypes.RSRP));
					stopGap.setRsrpEventThreshold1(factoryDetails.createMobilityConnectedModeStopGapsRsrpEventThreshold1(mobilityParams.getStopGap()));
					stopGap.setHysteresis(factoryDetails.createMobilityConnectedModeStopGapsHysteresis(BigDecimal.valueOf(0.0)));
					stopGap.setTimeToTrigger(factoryDetails.createMobilityConnectedModeStopGapsTimeToTrigger("320"));
					mobilityProfile.setConnectedModeThresholdStopGaps(stopGap);
				}
			}
			
			if(mobilityParams.getProfileName() == null){
				return false;
			}else{
				mobilityProfile.setName(mobilityParams.getProfileName());
			}
						
			ProfileResponse cloneResult = ( ProfileResponse ) helper_14_50.execute( "mobilityProfileClone", cloneFromName, mobilityProfile );
			
			if ( cloneResult == null){
				report.report("Fail to get Mobility profile cloning result", Reporter.WARNING );
				return false;
			}
			else if( cloneResult.getErrorCode() == Netspan.NBI_14_50.API.Lte.ErrorCodes.OK ) {
				report.report("Succeeded to clone Mobility profile, new profile name: " + mobilityParams.getProfileName());
				return true;
			} else {
				report.report("Failed to clone Mobility profile, reason: " + cloneResult.getErrorString(), Reporter.WARNING);
				return false;
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return false;
		
	}

	/**
	 * Clone son profile via netspan.
	 *
	 * @param node
	 *            the node
	 * @param cloneFromName
	 *            the clone from name
	 * @param sonEnabled
	 *            the son enabled
	 * @param pnpEnabled
	 *            the pnp enabled
	 * @param autoPCI
	 *            the auto pci
	 * @param ranges
	 *            the ranges
	 * @param anrState
	 *            the anr state
	 * @param anrFrequency
	 *            the anr frequency
	 * @return true, if successful
	 */
	public boolean cloneSonProfile(EnodeB node, String cloneFromName, SonParameters sonParmas) {
		
		try{
			ObjectFactory factoryDetails = new ObjectFactory();
			EnbSonProfile sonProfile = new EnbSonProfile();
			
			sonProfile.setName( sonParmas.getProfileName() );
			
			if (sonParmas.isSonCommissioning() != null && sonParmas.isSonCommissioning() ) {
				sonProfile.setSonCommissioningEnabled( factoryDetails.createEnbSonProfileSonCommissioningEnabled(sonParmas.isSonCommissioning()));
				//disable change of parameter due to NBI Change of param from boolean to enum
				sonProfile.setPciEnabled( factoryDetails.createEnbSonProfilePciEnabled( sonParmas.isAutoPCIEnabled() ));
				if ( sonParmas.isAutoPCIEnabled() != null && sonParmas.isAutoPCIEnabled() ) {
					PciRangeListContainer ranges = new PciRangeListContainer();
					PciRange pciRange;
					List<Pair<Integer, Integer>> rangesList = sonParmas.getRangesList();
					if (rangesList != null) {
						for (Pair<Integer, Integer> pair : rangesList) {
							pciRange = new PciRange();
							JAXBElement<Integer> elem0 = new JAXBElement<Integer>(null, null, pair.getElement0());
							pciRange.setPciStart(elem0);
							JAXBElement<Integer> elem1 = new JAXBElement<Integer>(null, null, pair.getElement1());
							pciRange.setPciEnd(elem1);
							ranges.getPciRange().add(pciRange);
						}
					}
					sonProfile.setPciRangeList( ranges );
				}
				SonAnrStates anrState = sonParmas.getAnrState() != null ? sonParmas.getAnrState() : SonAnrStates.DISABLED;
				
				sonProfile.setAnrState( factoryDetails.createEnbSonProfileAnrState(anrState) );
				
				sonProfile.setPnpMode(factoryDetails.createEnbSonProfilePnpMode(sonParmas.getPnpMode()));
				
				if ( anrState != SonAnrStates.DISABLED ) {
					// getting the current list and add new freqs
					AnrFreqListContainer anrFrequency = new AnrFreqListContainer();
					AnrFreq anrFreq;
					List<Integer> anrFrequencyList = sonParmas.getAnrFrequencyList();
					if (anrFrequencyList != null) {
						for (Integer integer : anrFrequencyList) {
							anrFreq = new AnrFreq();
							anrFreq.setFrequency(integer);
							anrFrequency.getAnrFrequency().add(anrFreq);
						}
						sonProfile.setAnrFrequencyList( anrFrequency );
					}
					if (anrState == SonAnrStates.HO_MEASUREMENT) {
						if(sonParmas.getMinAllowedHoSuccessRate() != null){
							sonProfile.setMinAllowedHoSuccessRate(factoryDetails.createEnbSonProfileMinAllowedHoSuccessRate(sonParmas.getMinAllowedHoSuccessRate()));
						}
					}
				}
			}
			if(sonParmas.isCSonEnabled() != null && sonParmas.isCSonEnabled()){
				
				LteSonCSonWs sonProfileCSon = factoryDetails.createLteSonCSonWs();
				sonProfileCSon.setIsCSonEnabled(factoryDetails.createLteSonCSonWsIsCSonEnabled(sonParmas.isCSonEnabled()));
				sonProfileCSon.setIsCSonRachEnabled(factoryDetails.createLteSonCSonWsIsCSonRachEnabled(sonParmas.iscSonRachMode()));
				sonProfileCSon.setIsCSonMcimEnabled(factoryDetails.createLteSonCSonWsIsCSonMcimEnabled(sonParmas.iscSonMcimMode()));
				sonProfileCSon.setIsCSonMroEnabled(factoryDetails.createLteSonCSonWsIsCSonMroEnabled(sonParmas.iscSonMroMode()));
				sonProfileCSon.setIsCSonMlbEnabled(factoryDetails.createLteSonCSonWsIsCSonMlbEnabled(sonParmas.iscSonMlbMode()));
				sonProfileCSon.setCSonMlbCapacityClassValue(factoryDetails.createLteSonCSonWsCSonMlbCapacityClassValue(sonParmas.getcSonCapacityClass()));
				sonProfileCSon.setCSonMlbPdschLoadThresh(factoryDetails.createLteSonCSonWsCSonMlbPdschLoadThresh(sonParmas.getcSonPDSCHLoad()));
				sonProfileCSon.setCSonMlbPuschLoadThresh(factoryDetails.createLteSonCSonWsCSonMlbPuschLoadThresh(sonParmas.getcSonPUSCHLoad()));
				sonProfileCSon.setCSonMlbRrcLoadThresh(factoryDetails.createLteSonCSonWsCSonMlbRrcLoadThresh(sonParmas.getcSonRRCLoad()));
				sonProfileCSon.setCSonMlbCpuLoadThresh(factoryDetails.createLteSonCSonWsCSonMlbCpuLoadThresh(sonParmas.getcSonCPUCHLoad()));
				sonProfile.setCSon(sonProfileCSon);
			}
			ProfileResponse cloneResult = ( ProfileResponse ) helper_14_50.execute( "sonProfileClone", cloneFromName, sonProfile );
			
			if ( cloneResult == null){
				report.report("Fail to get Son profile cloning resut", Reporter.WARNING );
				return false;
			}
			else if( cloneResult.getErrorCode() == Netspan.NBI_14_50.API.Lte.ErrorCodes.OK ) {
				report.report("Succeeded to clone Son profile, new profile name: " + sonParmas.getProfileName());
				return true;
			} else {
				report.report("Failed to clone Son profile, reason: " + cloneResult.getErrorString(), Reporter.WARNING);
				return false;
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Clone sync profile via netspan.
	 *
	 * @param profile
	 *            the profile
	 * @param cloneFromName
	 *            the clone from name
	 * @return true, if successful
	 */
	public boolean cloneSyncProfile(EnodeB node, String cloneFromName, SyncParameters syncParams) {
		
			EnbSyncProfile syncProfile = new EnbSyncProfile();
			syncProfile.setName(syncParams.getProfileName());
			ObjectFactory objectFactory = new ObjectFactory();
			
			if(syncParams.getPrimaryMasterIpAddress() != null){
				syncProfile.setPrimaryMasterIpAddress(syncParams.getPrimaryMasterIpAddress());
			}
			if(syncParams.getPrimaryMasterDomain()!=null){
				syncProfile.setPrimaryMasterDomain(objectFactory.createEnbSyncProfilePrimaryMasterDomain(syncParams.getPrimaryMasterDomain()));
			}
			if(syncParams.getSecondaryMasterIpAddress() != null){
				syncProfile.setSecondaryMasterIpAddress(syncParams.getSecondaryMasterIpAddress());
				if(!syncParams.getSecondaryMasterIpAddress().equals("0.0.0.0")){
					if(syncParams.getSecondaryMasterDomain()!=null){
						syncProfile.setSecondaryMasterDomain(objectFactory.createEnbSyncProfileSecondaryMasterDomain(syncParams.getSecondaryMasterDomain()));
					}
				}
			}
			if(syncParams.getAnnounceRateInMsgPerSec()!=null){
				syncProfile.setAnnounceRateInMsgPerSec(objectFactory.createEnbSyncProfileAnnounceRateInMsgPerSec(syncParams.getAnnounceRateInMsgPerSec().value()));
			}
			if(syncParams.getSyncRateInMsgPerSec()!=null){
				syncProfile.setSyncRateInMsgPerSec(objectFactory.createEnbSyncProfileSyncRateInMsgPerSec(syncParams.getSyncRateInMsgPerSec().value()));
			}
			if(syncParams.getDelayRequestResponseRateInMsgPerSec()!=null){
				syncProfile.setDelayRequestResponseRateInMsgPerSec(objectFactory.createEnbSyncProfileDelayRequestResponseRateInMsgPerSec(syncParams.getDelayRequestResponseRateInMsgPerSec().value()));
			}
			if(syncParams.getLeaseDurationInSec()!=null){
				syncProfile.setLeaseDurationInSec(objectFactory.createEnbSyncProfileLeaseDurationInSec(syncParams.getLeaseDurationInSec()));
			}
			
		try{
			ProfileResponse cloneResult = ( ProfileResponse ) helper_14_50.execute( "syncProfileClone", cloneFromName, syncProfile);
			
			if ( cloneResult == null){
				report.report("Fail to get Sync profile cloning result", Reporter.WARNING );
				return false;
			}
			else if( cloneResult.getErrorCode() == Netspan.NBI_14_50.API.Lte.ErrorCodes.OK ) {
				report.report("Succeeded to clone Sync profile, new profile name: " + syncParams.getProfileName());
				return true;
			} else {
				report.report("Failed to clone Sync profile, reason: " + cloneResult.getErrorString(), Reporter.WARNING);
				return false;
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			report.report("cloneSyncProfile Failed, reason: " + e.getMessage(), Reporter.WARNING);
		}
		
		return false;
	}

	@Override
	public PrimaryClockSourceEnum getPrimaryClockSource(EnodeB node){
		String profile = getNodeConfig(node).getSyncProfile();
		List<String> nameList = new ArrayList<String>();
		nameList.add(profile);
		try{
			LteSyncProfileGetResult syncProfile = ( LteSyncProfileGetResult ) helper_14_50.execute("syncProfileGet", nameList);
			JAXBElement<ClockSources> clockSource = syncProfile.getSyncProfileResult().get(FIRST_ELEMENT).getSyncProfile().getPrimaryClockSource();
			if(clockSource.getValue() == ClockSources.NONE){
				return PrimaryClockSourceEnum.NONE;
			}
			if(clockSource.getValue() == ClockSources.GNSS){
				return PrimaryClockSourceEnum.GNSS;
			}
			if(clockSource.getValue() == ClockSources.IEEE_1588){
				return PrimaryClockSourceEnum.IEEE_1588;
			}
		}		
		catch(Exception e){
			e.printStackTrace();
			report.report("getPrimaryClockSource Failed, reason: " + e.getMessage(), Reporter.WARNING);
			return null;
		}
		return null;
	}
	
	/**
	 * Gets the sync profile.
	 *
	 * @param node
	 *            the node
	 * @return the sync profile
	 */
	public LteSyncProfileResult getSyncProfile(EnodeB node) {
		GeneralUtils.printToConsole("trying to get Sync profiles via NBI");
		EnbDetailsGet nodeConfig = getNodeConfig(node);
		String profile = "";
		if(nodeConfig != null){
			profile = nodeConfig.getSonProfile();			
		}
		
		try {
			LteSyncProfileGetResult cloneResult = ( LteSyncProfileGetResult ) helper_14_50.execute( "syncProfileGet", profile );
			return cloneResult.getSyncProfileResult().get(FIRST_ELEMENT);
		} catch (Exception e) {
			GeneralUtils.printToConsole(e.toString());
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Populate node names.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	private void populateNodeNames() throws Exception {
		GeneralUtils.printToConsole("Stating to map eNodeB nodeName to eNodeB nodeID");
		nodeNames = new Hashtable<String, String>();

		Netspan.NBI_14_50.API.Inventory.NodeListResult result = null;
		try{
			result = (Netspan.NBI_14_50.API.Inventory.NodeListResult) helper_14_50.execute("nodeList");
		}catch(Exception e){
			e.printStackTrace();
			report.report("populateNodeNames via Netspan Failed due to: " + e.getMessage(), Reporter.WARNING);
			return;
		}
		String line = "-------------------------------------------------------------------------------------------";
		GeneralUtils.printToConsole(line);
		GeneralUtils.printToConsole("|            Name             |                      NodeID                           |");
		GeneralUtils.printToConsole(line);
		for (int nodeIndex = 0; nodeIndex < result.getNode().size(); nodeIndex++) {
			Netspan.NBI_14_50.API.Inventory.NodeSimple node = result.getNode().get(nodeIndex);
			nodeNames.put(node.getNodeId(), node.getName());
			System.out.print("| ");
			System.out.print(node.getName());
			for (int i = 0; i < line.length() / 2 - node.getName().length(); i++)
				System.out.print(" ");
			System.out.print("| ");
			System.out.print(node.getNodeId());
			for (int i = 0; i < line.length() / 2 - node.getNodeId().length(); i++)
				System.out.print(" ");
			GeneralUtils.printToConsole(" |");
		}
		GeneralUtils.printToConsole(line);
		GeneralUtils.printToConsole("Mapping complete");
	}

	private EnbDetailsGet getNodeConfig(EnodeB enodeB) {
		return getNodeConfig(enodeB.getNetspanName());
	}

	/**
	 * Gets the EnodeB configuration.
	 * 
	 * @param enodeB
	 *            the enode b
	 * @return the node config
	 */
	public EnbDetailsGet getNodeConfig(String nodeName) {
		ArrayList<String> nodeList = new ArrayList<String>();
		nodeList.add(nodeName);
		try {
			if (getPnpNode(nodeName) != null) {
				return getPnpNode(nodeName).getLteEnbConfig();
			}
			GeneralUtils.printToConsole("Sending NBI requeset \"enbConfigGet\" for eNodeB " + nodeName);

			Netspan.NBI_14_50.API.Lte.LteEnbConfigGetResult result = (Netspan.NBI_14_50.API.Lte.LteEnbConfigGetResult) helper_14_50
					.execute("enbConfigGet", nodeList);

			GeneralUtils.printToConsole(String.format("NBI method \"enbConfigGet\" for eNodeB %s returned value: %s", nodeName,result.getErrorCode().toString()));

			return result.getNodeResult().get(FIRST_ELEMENT).getEnbConfig();

		} catch (Exception e) {
			try {
				GeneralUtils.printToConsole("Connection Problem with NetSpan.. trying to execute enbConfigGet again from NBI");
				e.printStackTrace();
				GeneralUtils.printToConsole("Sending NBI requeset \"enbConfigGet\" for eNodeB " + nodeName);
				Netspan.NBI_14_50.API.Lte.LteEnbConfigGetResult result = (Netspan.NBI_14_50.API.Lte.LteEnbConfigGetResult) helper_14_50
						.execute("enbConfigGet", nodeList);

				GeneralUtils.printToConsole(String.format("NBI method \"enbConfigGet\" for eNodeB %s returned value: %s", nodeName,
						result.getErrorCode().toString()));

				return result.getNodeResult().get(FIRST_ELEMENT).getEnbConfig();

			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * Gets the EnodeB node info.
	 * 
	 * @param enodeB
	 *            the enode b
	 * @return the node Details
	 */
	@Override
	public NodeInfo getNodeDetails(EnodeB enodeB) {
		String nodeName = enodeB.getNetspanName();
		ArrayList<String> nodeList = new ArrayList<String>();
		nodeList.add(nodeName);
		for (int i = 0; i < 3; i++) {	
			try {
				
				NodeDetailGetResult result = (NodeDetailGetResult) helper_14_50.execute("nodeInfoGet", nodeList,new ArrayList<String>());
				GeneralUtils.printToConsole("NBI method \"nodeInfoGet\" for eNodeB "+nodeName+" returned value: " + result.getErrorCode());
				
				NodeInfo ni = new NodeInfo();
				ni.productCode = result.getNodeDetail().get(FIRST_ELEMENT).getNodeInfo().getProductCode();
				ni.hardwareType = result.getNodeDetail().get(FIRST_ELEMENT).getNodeInfo().getHardwareType();
				ni.nodeID = result.getNodeDetail().get(FIRST_ELEMENT).getNodeInfo().getNodeId();
				ni.snmpPort = result.getNodeDetail().get(FIRST_ELEMENT).getNodeInfo().getSnmpAgent().get(FIRST_ELEMENT).getSnmpPort();
					
				return ni;
			} catch (Exception e) {
				GeneralUtils.printToConsole("Netspan function Failed: " + e);
				e.printStackTrace();
			}
		}
		return null;
	}

	public boolean setNodeConfig(EnodeB enodeB, LteEnbDetailsSetWs netspanConfig) {
		return setNodeConfig(enodeB.getNetspanName(), netspanConfig);
	}

	/**
	 * Sets the EnodeB configuration.
	 * 
	 * @param enodeB
	 *            the EnodeB
	 * @param netspanConfig
	 *            the netspan config
	 * @return true, if successful
	 */
	public boolean setNodeConfig(String nodeName, LteEnbDetailsSetWs netspanConfig) {
		try {
			if (getPnpNode(nodeName) != null) {
				GeneralUtils.printToConsole(String.format("eNodeB %s is located in PnP list, skipping set.", nodeName));
				return true;
			}
			GeneralUtils.printToConsole("Sending NBI requeset \"enbConfigSet\" for eNodeB " + nodeName);
			Netspan.NBI_14_50.API.Lte.NodeActionResult result = (Netspan.NBI_14_50.API.Lte.NodeActionResult) helper_14_50
					.execute("enbConfigSet", nodeName, netspanConfig);
			GeneralUtils.printToConsole(String.format("NBI method \"enbConfigSet\" for eNodeB %s returned value: ", nodeName,
					result.getErrorCode() + ":" + result.getErrorString()));
			if (result.getErrorCode() == Netspan.NBI_14_50.API.Lte.ErrorCodes.OK)
				return true;
			else {
				for (Netspan.NBI_14_50.API.Lte.NodeResult node : result.getNode()) {
					if (node.getNodeResultCode() == NodeResultValues.NODE_ERROR) {
						report.report("[NMS] ERROR in " + node.getName() + ": " + node.getNodeResultString());
					}
				}
			}
		} catch (Exception e) {
			GeneralUtils.printToConsole(e.getMessage());
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * Adds neighbor.
	 *
	 * @param enodeB
	 *            the EnodeB
	 * @param neighbor
	 *            the neighbor
	 * @param hoControlStatus
	 *            the ho control status
	 * @param x2ControlStatus
	 *            the x2 control status
	 * @param handoverType
	 *            the handover type
	 * @param isStaticNeighbor
	 *            the is static neighbor
	 * @return true, if successful
	 */
	public boolean addNeighbor(EnodeB enodeB, EnodeB neighbor, HoControlStateTypes hoControlStatus,
			X2ControlStateTypes x2ControlStatus, HandoverType handoverType, boolean isStaticNeighbor,
			String qOffsetRange) {
		String sourceNodeName = enodeB.getNetspanName();
		String neighborName = neighbor.getNetspanName();
		GeneralUtils.printToConsole("Sending NBI requeset \"lteNeighbourAdd\" for eNodeB " + sourceNodeName);
		try {
			LteNeighbourResponse result = (LteNeighbourResponse) helper_14_50
					.execute("lteNeighbourAdd", sourceNodeName, neighborName, hoControlStatus, x2ControlStatus,
							handoverType, isStaticNeighbor, qOffsetRange);
			GeneralUtils.printToConsole(String.format("NBI method \"lteNeighbourAdd\" for eNodeB %s returned value: %s", sourceNodeName,
					result.getErrorCode().toString()));
			if (result.getErrorCode() == Netspan.NBI_14_50.API.Lte.ErrorCodes.ERROR) {
				report.report(result.getErrorString(), Reporter.WARNING);
			}
			return result.getErrorCode() == Netspan.NBI_14_50.API.Lte.ErrorCodes.OK;
		} catch (Exception e) {
			GeneralUtils.printToConsole(e.toString());
			e.printStackTrace();

		}
		return false;
	}


	/**
	 * Delete neighbor.
	 * 
	 * @param enodeB
	 *            the EnodeB
	 * @param neighbor
	 *            the neighbor
	 * @return true, if successful
	 */
	public boolean deleteNeighbor(EnodeB enodeB, EnodeB neighbor) {
		return deleteNeighbor(enodeB, neighbor.getNetspanName());
	}

	/**
	 * Delete neighbor.
	 *
	 * @param enodeB
	 *            the EnodeB
	 * @param neighborName
	 *            the neighbor name
	 * @return true, if successful
	 */
	public boolean deleteNeighbor(EnodeB enodeB, String neighborName) {
		try {
			GeneralUtils.printToConsole("Sending NBI requeset \"lteNeighbourDelete\" for eNodeB " + enodeB.getName());
			Netspan.NBI_14_50.API.Lte.LteNeighbourResponse result = (Netspan.NBI_14_50.API.Lte.LteNeighbourResponse) helper_14_50
					.execute("lteNeighbourDelete", enodeB.getNetspanName(), neighborName);

			GeneralUtils.printToConsole(String.format("NBI method \"lteNeighbourDelete\" for eNodeB %s returned value: %s", enodeB.getName(),
					result.getErrorCode().toString()));
			return result.getErrorCode() == Netspan.NBI_14_50.API.Lte.ErrorCodes.OK;
		} catch (Exception e) {
			GeneralUtils.printToConsole(e.toString());
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * deleteAllNeighbors
	 * 
	 * @param enodeB
	 *            the EnodeB
	 * @return true, if successful
	 */
	public boolean deleteAllNeighbors(EnodeB enodeB) {
		try {
			if (verifyNoNeighbors(enodeB)) {
				return true;
			}
			Netspan.NBI_14_50.API.Lte.LteNeighbourResponse result = (Netspan.NBI_14_50.API.Lte.LteNeighbourResponse) helper_14_50
					.execute("lteNeighbourDeleteAll", enodeB.getNetspanName());

			GeneralUtils.printToConsole("NBI method \"lteNeighbourDeleteAll\" for eNodeB "+enodeB.getName()+" returned value: "+ result.getErrorCode().toString());
			
			boolean errorCode = result.getErrorCode() == Netspan.NBI_14_50.API.Lte.ErrorCodes.OK;
			if (errorCode)
				return true;
			else 
				return result.getErrorString().toLowerCase().contains("no neighbours to delete");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see Netspan.NetspanServer#verifyNoNeighbors(EnodeB.EnodeB)
	 */
	@Override
	public boolean verifyNoNeighbors(EnodeB enodeB) {
		List<LteAnrStatusWs> allNeighbors = getNeighbourList(enodeB);
		try{
			return allNeighbors.isEmpty();
		}catch(Exception e){
			e.printStackTrace();
			report.report("Exception thrown");
			return false;
		}
	}

	/**
	 * verify that neighbor was added to the main EnodeB
	 * 
	 * For each parameter, if its value is "null", the test for the specific
	 * parameter will not be evaluated. Only hoControlType, x2ControlType,
	 * handoverType and qOffserRange can be sent as "null".
	 * 
	 * @param enodeB
	 * @param neighbor
	 * @param hoControlType
	 * @param x2ControlType
	 * @param handoverType
	 * @param isStaticNeighbor
	 * @param qOffsetRange
	 * @return
	 */
	public boolean verifyNeighbor(EnodeB enodeB, EnodeB neighbor, HoControlStateTypes hoControlType,
			X2ControlStateTypes x2ControlType, HandoverType handoverType, boolean isStaticNeighbor,
			String qOffsetRange) {
		boolean wasadded = true;
		boolean wasFound = false;
		List<LteAnrStatusWs> allNeighbors = getNeighbourList(enodeB);

		if(allNeighbors==null){
			report.report("Couldn't get neighbor list from eNodeB");
			return false;
		}
		if (allNeighbors.isEmpty()) {
			report.report("The neighbor list is empty");
			return false;
		}

		for (LteAnrStatusWs nbr : allNeighbors) {
			if (nbr.getName().equals(neighbor.getNetspanName())) {
				wasFound = true;
				report.report("Neighbor: " + neighbor.getNetspanName() + " was found in EnodeB: "
						+ enodeB.getNetspanName() + " list");

				if (null != hoControlType) {
					if (nbr.getHoControlStatus().getValue() != hoControlType) {
						report.report("Ho Control Type is " + nbr.getHoControlStatus().getValue() + " and not "
								+ hoControlType.value() + " as expected", Reporter.WARNING);
						wasadded = false;
					} else {
						report.report("Ho Control Type is " + nbr.getHoControlStatus().getValue() + " as expected");
					}
				}

				if (null != x2ControlType) {
					if (nbr.getX2ControlStatus().getValue() != x2ControlType) {
						report.report("X2 Control Type is " + nbr.getX2ControlStatus().getValue() + " and not "
								+ x2ControlType.value() + " as expected", Reporter.WARNING);
						wasadded = false;
					} else {
						report.report("X2 Control Type is " + nbr.getX2ControlStatus().getValue() + " as expected");
					}
				}

				if (null != handoverType) {
					if (nbr.getHandoverType().getValue() != handoverType) {
						report.report("HandOver Type is " + nbr.getHandoverType().getValue() + " and not "
								+ handoverType.value() + " as expected", Reporter.WARNING);
						wasadded = false;
					} else {
						report.report("HandOver Type is " + nbr.getHandoverType().getValue() + " as expected");
					}
				}

				if(nbr.getIsStaticNeighbour() != null){
					if (nbr.getIsStaticNeighbour().getValue() != isStaticNeighbor) {
						report.report("isStatic is set to " + nbr.getIsStaticNeighbour().getValue() + " and not to "
								+ isStaticNeighbor + " as expected", Reporter.WARNING);
						wasadded = false;
					} else {
						report.report("isStatic is set to " + nbr.getIsStaticNeighbour().getValue() + " as expected");
					}
				}else{
					GeneralUtils.printToConsole("nbr.getIsStaticNeighbour is null!");
				}

				/*if (null != qOffsetRange) {
					if (!nbr.    getQOffsetRange().getValue().equals(qOffsetRange)) {
						report.report("QoffsetRange is set to " + nbr.getQOffsetRange().getValue() + " and not to "
								+ qOffsetRange + " as expected", Reporter.WARNING);
						wasadded = false;
					} else {
						report.report("QoffsetRange is set to " + nbr.getQOffsetRange().getValue() + " as expected");
					}
				}*/

				break;
			}
		}
		if (!wasFound) {
			report.report("Couldn't find the neighbor " + neighbor.getNetspanName() + " in the EnodeB: "
					+ enodeB.getNetspanName() + " list", Reporter.WARNING);
			wasadded = false;
		}
		return wasadded;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see Netspan.NetspanServer#verifyNeighbor(EnodeB.EnodeB, EnodeB.EnodeB)
	 */
	@Override
	public boolean verifyNeighbor(EnodeB enodeB, EnodeB neighbor) {
		List<LteAnrStatusWs> allNeighbors = getNeighbourList(enodeB);
		if (allNeighbors.isEmpty()) {
			report.report("The neighbor list is empty");
			return false;
		}

		for (LteAnrStatusWs nbr : allNeighbors) {
			if (nbr.getName().equals(neighbor.getNetspanName())) {
				report.report("Neighbor: " + neighbor.getNetspanName() + " was found in EnodeB: "
						+ enodeB.getNetspanName() + " list");
				return true;
			}
		}
		report.report("Couldn't find the neighbor " + neighbor.getNetspanName() + " in the EnodeB: "
				+ enodeB.getNetspanName() + " list", Reporter.WARNING);
		return false;
	}

	public boolean verifyNoNeighbor(EnodeB enodeB, EnodeB neighbor) {
		List<LteAnrStatusWs> allNeighbors = getNeighbourList(enodeB);
		if (allNeighbors.isEmpty()) {
			report.report("The neighbor list is empty");
			return true;
		}

		for (LteAnrStatusWs nbr : allNeighbors) {
			if (nbr.getName().equals(neighbor.getNetspanName())) {
				report.report("Neighbor: " + neighbor.getNetspanName() + " was found in EnodeB: "
						+ enodeB.getNetspanName() + " list", Reporter.WARNING);
				return false;
			}
		}
		report.report("The neighbor " + neighbor.getNetspanName() + " was not found in the EnodeB: "
				+ enodeB.getNetspanName() + " list");
		return true;
	}

	/**
	 * Gets the neighbour list.
	 *
	 * @param enodeB
	 *            the EnodeB
	 * @return the neighbour list
	 */
	public List<LteAnrStatusWs> getNeighbourList(EnodeB enodeB) {
		return getNeighbourList(enodeB.getNetspanName());
	}

	/**
	 * Get the neighbors details
	 * 
	 * @param enodeB
	 *            the eNodeB
	 * @return - list of neighbors details lists each internal list contains the
	 *         name and the details of a single neighbor
	 */
	public ArrayList<ArrayList<String>> getNeighboursDetails(EnodeB enodeB) {
		ArrayList<ArrayList<String>> neighboursResult = null;
		String neighbourName = "";
		String neighbourDetails = "";
		String enodeBName = enodeB.getNetspanName();
		List<LteAnrStatusWs> neighbourList = getNeighbourList(enodeBName);
		if (neighbourList == null)
			report.report("Could not pull neighbours details from NBIF", Reporter.WARNING);
		else if (neighbourList.size() > 0) {
			neighboursResult = new ArrayList<ArrayList<String>>();
			report.report("Current neighbours are(via NBI):");
			for (LteAnrStatusWs neighbour : neighbourList) {
				ArrayList<String> singleNeighbourResult = new ArrayList<String>();
				neighbourName = String.format("Neighbour \"%s\" ", neighbour.getName());
				neighbourDetails = "Ho Control State: " + neighbour.getHoControlStatus().getValue()
						+ ", X2 Control State: " + neighbour.getX2ControlStatus().getValue() + ", Handover Type: "
						+ neighbour.getHandoverType().getValue() + ", Is Static Neighbour: "
						+ neighbour.getIsStaticNeighbour().getValue();
				singleNeighbourResult.add(neighbourName);
				singleNeighbourResult.add(neighbourDetails);
				neighboursResult.add(singleNeighbourResult);
			} // end for loop
		} // end if
		else
			report.step("eNodeB " + enodeBName + " has no neighbours");
		return neighboursResult;
	}

	/**
	 * Gets the neighbour list.
	 *
	 * @param enodeBName
	 *            the EnodeB name
	 * @return the neighbour list
	 */
	public List<LteAnrStatusWs> getNeighbourList(String enodeBName) {

		try {
			Netspan.NBI_14_50.API.Status.LteSonAnrGetResult result = (Netspan.NBI_14_50.API.Status.LteSonAnrGetResult) helper_14_50
					.execute("enbSonAnrStatusGet", enodeBName);
			GeneralUtils.printToConsole("Result error code " + result.getErrorCode().toString());
			if (result.getErrorCode() == Netspan.NBI_14_50.API.Status.ErrorCodes.ERROR) {
				report.report(result.getErrorString(), Reporter.WARNING);
			}
			if (result.getErrorCode() == Netspan.NBI_14_50.API.Status.ErrorCodes.OK) {
				return result.getAnr();
			}

		} catch (Exception e) {
			GeneralUtils.printToConsole(e.toString());
			e.printStackTrace();

		}
		return new ArrayList<LteAnrStatusWs>();
	}

	public Enb3RdParty get3rdParty(String... partiesArray) throws Exception {
		ArrayList<String> parties = new ArrayList<String>();
		for (int i = 0; i < partiesArray.length; i++)
			parties.add(partiesArray[i]);

		Lte3RdPartyGetResult result = (Netspan.NBI_14_50.API.Lte.Lte3RdPartyGetResult) helper_14_50.execute("lte3RdPartyGet",
				parties);
		GeneralUtils.printToConsole("NBI method \"Lte3rdPartyCreate\"  returned value: " + result.getErrorCode().toString());
		if (result.getErrorCode() != Netspan.NBI_14_50.API.Lte.ErrorCodes.OK) {
			report.report(result.getErrorString(), Reporter.WARNING);
		}
		if (result != null && result.getLte3RdPartyResult() != null && result.getLte3RdPartyResult().get(0)
				.getResultCode() == Netspan.NBI_14_50.API.Lte.Lte3RdPartyResultValues.OK)
			return result.getLte3RdPartyResult().get(0).getEnb3RdParty();
		else {
			if (result == null || result.getLte3RdPartyResult() == null)
				throw new Exception("fail to retrive a result from NBIF");
			else {
				throw new Exception("Error String:" + result.getErrorString() + " Result String:"
						+ result.getLte3RdPartyResult().get(0).getResultString());
			}
		}
	}

	public boolean is3rdPartyExist(String partyName) {
		try {
			return get3rdParty(partyName).getName().equals(partyName);
		} catch (Exception ex) {
			GeneralUtils.printToConsole(ex);
			return false;
		}

	}

	/**
	 * Gets the node service state.
	 * 
	 * @param enodeB
	 *            the node
	 * @return the node service state
	 */
	public ServiceState getNodeServiceState(EnodeB enodeB) {
		try {
			GeneralUtils.printToConsole(String.format("Sending NBI requeset \"enbStateGet\" for eNodeB %s (%s)", enodeB.getName(), enodeB.getNetspanName()));
			Netspan.NBI_14_50.API.Lte.EnbStateGetResult result = (Netspan.NBI_14_50.API.Lte.EnbStateGetResult) helper_14_50
					.execute("enbStateGet", Arrays.asList(new String[] { enodeB.getNetspanName() }));

			if (result.getErrorCode() != Netspan.NBI_14_50.API.Lte.ErrorCodes.OK) {
				throw new Exception("Returned code result different from expected \"OK\" actual code:"
						+ result.getErrorCode().toString() + " error :" + result.getErrorString());
			}

			GeneralUtils.printToConsole(String.format("NBI method \"enbStateGet\" for eNodeB %s returned value: %s %s", enodeB.getName(),result.getErrorCode().toString(), result.getErrorString()));

			// get enodB state
			EnbStatesGet nodeState = EnbStatesGet.UNKNOWN;
			if (result != null && result.getENodeB() != null && result.getENodeB().size() > 0) {
				nodeState = result.getENodeB().get(0).getEnbState();
				GeneralUtils.printToConsole("Node state value according to NBIF:" + nodeState.value());
			} else {
				GeneralUtils.printToConsole("Could not pull node state result from NBIF");
			}
			// replace EnbStates to ServiceState
			switch (nodeState.value()) {
			case "InService":
				return ServiceState.InService;
			case "OutOfService":
				return ServiceState.OutOfService;
			default:
				return ServiceState.Unknown;
			}
		} catch (Exception e) {
			GeneralUtils.printToConsole(e.toString());
			e.printStackTrace();
		}
		return ServiceState.Unknown;
	}

	/**
	 * Sets the node service state.
	 *
	 * @param enodeB
	 *            the node
	 * @param state
	 *            the state
	 * @return the node service state
	 */
	public boolean setNodeServiceState(EnodeB enodeB, Netspan.API.Enums.EnbStates state) {
		try {
			GeneralUtils.printToConsole(String.format("Sending NBI requeset \"enbStateSet\" for eNodeB %s (%s)", enodeB.getName(), enodeB.getNetspanName()));

			Netspan.NBI_14_50.API.Lte.NodeActionResult result = (Netspan.NBI_14_50.API.Lte.NodeActionResult) helper_14_50
					.execute("enbStateSet", enodeB.getNetspanName(), state);

			if (result.getErrorCode() != Netspan.NBI_14_50.API.Lte.ErrorCodes.OK) {
				throw new Exception("Returned code result different from expected \"OK\" actual code:"
						+ result.getErrorCode().toString() + " error:" + result.getErrorString());
			}

			GeneralUtils.printToConsole(String.format("NBI method \"enbStateSet\" for eNodeB %s returned value: %s", enodeB.getName(),
					result.getErrorCode().toString()));
		} catch (Exception e) {

			GeneralUtils.printToConsole(e.toString());
			e.printStackTrace();
			return false;
		}

		return true;
	}

	/**
	 * This function gets a name of EnodeB and verifies the node exists in
	 * Netspan
	 * 
	 * @param nodeName
	 *            the name of the node to verify
	 * @return true - if node exist in Netspan false - if node doesn't exist in
	 *         Netspan
	 */
	public boolean isNodeExist(String nodeName) {
		try {
			GeneralUtils.printToConsole("Sending NBI requeset \"NodeCurrentGet\" for eNodeB " + nodeName);
			Netspan.NBI_14_50.API.Status.NodeSensorGetResult result = (Netspan.NBI_14_50.API.Status.NodeSensorGetResult) helper_14_50
					.execute("nodeCurrentGet", nodeName);
			GeneralUtils.printToConsole(String.format("NBI method \"NodeCurrentGet\" for eNodeB %s returned value: %s", nodeName,
					result.getNodeResult().toString()));
			return (result.getNodeResult() == Netspan.NBI_14_50.API.Status.NodeStatusResultValues.OK);
		} catch (Exception e) {
			if (e != null)
				GeneralUtils.printToConsole(e.toString());
		}
		return false;
	}

	@Override
	public boolean setNodeConfig(EnodeB enodeB, ILteEnbDetailsSet netspanConfig) {
		LteEnbDetailsSetWs netspanConfigObj = new LteEnbDetailsSetWs();
		if (netspanConfig.getSonProfile() != null) {
			netspanConfigObj.setSonProfile(netspanConfig.getSonProfile());
		}

		if (netspanConfig.getNetworkProfile() != null) {
			netspanConfigObj.setNetworkProfile(netspanConfig.getNetworkProfile());
		}

		if (netspanConfig.getSecurityProfile() != null) {
			netspanConfigObj.setSecurityProfile(netspanConfig.getSecurityProfile());
		}
		return setNodeConfig(enodeB, netspanConfigObj);
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see Netspan.NetspanServer#changeNbrAutoX2ControlFlag(EnodeB.EnodeB,
	 *      boolean)
	 */
	@Override
	public boolean changeNbrAutoX2ControlFlag(EnodeB enodeB, boolean state) {
		// EnbDetailsGet result = this.getNodeConfig(enodeB);
		LteEnbDetailsSetWs netspanConfig = new LteEnbDetailsSetWs();
		ObjectFactory factory = new ObjectFactory();
		netspanConfig.setIsAutoX2ControlForNeighboursEnabled(
				factory.createLteEnbDetailsSetWsIsAutoX2ControlForNeighboursEnabled(state));
		return setNodeConfig(enodeB, netspanConfig);
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see Netspan.NetspanServer#changeNbrX2ConfiguratioUpdateFlag(EnodeB.EnodeB,
	 *      boolean)
	 */
	@Override
	public boolean changeNbrX2ConfiguratioUpdateFlag(EnodeB enodeB, boolean state) {
		LteEnbDetailsSetWs netspanConfig = new LteEnbDetailsSetWs();
		ObjectFactory factory = new ObjectFactory();
		netspanConfig.setIsX2ConfigurationUpdateForNeighboursEnabled(
				factory.createLteEnbDetailsSetWsIsX2ConfigurationUpdateForNeighboursEnabled(state));
		return setNodeConfig(enodeB, netspanConfig);
	}

	public boolean Create3rdParty(String name, String ipAddress, int physicalLayerCellGroup, int physicalLayerIdentity,
			int cellIdentity, int enbId, Netspan.API.Enums.EnbTypes enbType, int cellId, int tac, int dlEarfcn,
			EnodeBChannelBandwidth bandwidth, String mcc, String mnc) {

		GeneralUtils.printToConsole("Sending NBI requeset \"Lte3rdPartyCreate\" ");
		Enb3RdParty soapDetails = new Enb3RdParty();
		ObjectFactory factoryDetails = new ObjectFactory();
		soapDetails.setName(name);
		soapDetails.setIpAddress(ipAddress);
		soapDetails.setPhysicalLayerCellGroup(
				factoryDetails.createEnb3RdPartyPhysicalLayerCellGroup(physicalLayerCellGroup));
		soapDetails
				.setPhysicalLayerIdentity(factoryDetails.createEnb3RdPartyPhysicalLayerIdentity(physicalLayerIdentity));
		soapDetails.setEnbType(factoryDetails.createEnb3RdPartyEnbType(enbType));
		if (enbType == EnbTypes.MACRO) {
			soapDetails.setEnbId(factoryDetails.createEnb3RdPartyEnbId(enbId));
			soapDetails.setCellId(factoryDetails.createEnb3RdPartyCellId(cellId));
		} else {
			soapDetails.setCellIdentity(factoryDetails.createEnb3RdPartyCellIdentity(cellIdentity));
		}
		soapDetails.setTac(factoryDetails.createEnb3RdPartyTac(tac));
		soapDetails.setDlEarfcn(factoryDetails.createEnb3RdPartyDlEarfcn(dlEarfcn));
		soapDetails.setBandwidth(factoryDetails.createEnb3RdPartyBandwidth(String.valueOf(bandwidth.getBw())));
		soapDetails.setMcc(mcc);
		soapDetails.setMnc(mnc);

		try {
			Lte3RdPartyResponse result = (Netspan.NBI_14_50.API.Lte.Lte3RdPartyResponse) helper_14_50
					.execute("lte3RdPartyCreate", soapDetails);
			GeneralUtils.printToConsole("NBI method \"Lte3rdPartyCreate\"  returned value: " + result.getErrorCode().toString());
			if (result.getErrorCode() != Netspan.NBI_14_50.API.Lte.ErrorCodes.OK) {
				report.report(result.getErrorString(), Reporter.WARNING);
			}
			return result.getErrorCode() == Netspan.NBI_14_50.API.Lte.ErrorCodes.OK;
		} catch (Exception e) {
			GeneralUtils.printToConsole(e.toString());
			e.printStackTrace();
		}
		return false;
	}

	public boolean delete3rdParty(ArrayList<String> name) {
		try {

			GeneralUtils.printToConsole("Sending NBI requeset \"Lte3rdPartyDelete\" for eNodeB " + name);
			Netspan.NBI_14_50.API.Lte.Lte3RdPartyResponse result = (Netspan.NBI_14_50.API.Lte.Lte3RdPartyResponse) helper_14_50
					.execute("lte3RdPartyDelete", name);

			GeneralUtils.printToConsole(String.format("NBI method \"lte3rdPartyDelete\" for eNodeB %s returned value: %s", name,result.getErrorCode().toString()));
			return result.getErrorCode() == Netspan.NBI_14_50.API.Lte.ErrorCodes.OK;
		} catch (Exception e) {
			GeneralUtils.printToConsole(e.toString());
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean verifyNoANRNeighbors(EnodeB enodeB) {
		LteSonAnrGetResult lteSonAnrGetResult;
		try {
			lteSonAnrGetResult = (LteSonAnrGetResult) helper_14_50.execute("enbSonAnrStatusGet", enodeB.getNetspanName());
			List<LteAnrStatusWs> anrNeighbors = lteSonAnrGetResult.getAnr();
			return anrNeighbors.size() == 0;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean deleteEnbProfile(String profileName, EnbProfiles profileType) {
		try {
			ArrayList<String> nameList = new ArrayList<>();
			nameList.add(profileName);
			String methodName = profileType == EnbProfiles.EnodeB_Advanced_Profile ? "enbAdvancedProfileConfigDelete"
					: profileType.value() + "ProfileDelete";
			ProfileResponse response = (ProfileResponse) helper_14_50.execute(methodName, nameList);
			if (response.getErrorString() != null) {
				report.report(response.getErrorString());
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public String getRunningVer(EnodeB enb) {
		NodeSoftwareGetResult NodeSoftwareGetResult;
		try {
			NodeSoftwareGetResult = (NodeSoftwareGetResult) helper_14_50.execute("nodeSoftwareStatusGet",
					enb.getNetspanName());
			return NodeSoftwareGetResult.getSwList().getSwStatusWs().get(0).getRunningVersion();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public String getStandbyVer(EnodeB enb) {
		NodeSoftwareGetResult NodeSoftwareGetResult;
		try {
			NodeSoftwareGetResult = (NodeSoftwareGetResult) helper_14_50.execute("nodeSoftwareStatusGet",
					enb.getNetspanName());
			return NodeSoftwareGetResult.getSwList().getSwStatusWs().get(0).getStandbyVersion();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public List<String> getAll3rdParty() {
		NameResult nameResult;
		try {
			nameResult = (NameResult) helper_14_50.execute("lte3RdPartyList");
			return nameResult.getName();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public HashMap<Integer, Integer> getUeConnectedPerCategory(EnodeB enb) {
		LteUeGetResult lteUeGetResult;
		HashMap<Integer, Integer> ret = new HashMap<>();

		try {
			lteUeGetResult = (LteUeGetResult) helper_14_50.execute("enbConnectedUeStatusGet", enb.getNetspanName());
			LteUeStatusWs currentCell = lteUeGetResult.getCell().get(0);
			List<LteUeCategory> catDataList = currentCell.getCategoryData();
			for (LteUeCategory catData : catDataList)
				ret.put(catData.getCategory().getValue(), catData.getConnectedUes().getValue());

			return ret;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public RadioParameters radioProfileGet(EnodeB enb) throws Exception {
		String enbRadioProfileName = this.getCurrentRadioProfileName(enb);
		if(enbRadioProfileName == null) {
			report.report("Could not get Radio Profile name from Netspan - Going for SNMP");
			throw new Exception();
		}
		LteRadioProfileGetResult netspanResult = null;
		List<java.lang.String> enbList = new ArrayList<java.lang.String>();
		enbList.add(enbRadioProfileName);
		RadioParameters radioParam = null;
		netspanResult = (LteRadioProfileGetResult) helper_14_50.execute("radioProfileGet", enbList);
		radioParam = parseRadioProfile(netspanResult);
		return radioParam;
	}

	// try and check reflection over this
	private RadioParameters parseRadioProfile(LteRadioProfileGetResult netspanResult) throws NoSuchFieldException {
		RadioParameters radioParams = new RadioParameters();
		radioParams.setProfileName(netspanResult.getRadioProfileResult().get(0).getRadioProfile().getName());
		radioParams.setBandwidth(
				netspanResult.getRadioProfileResult().get(0).getRadioProfile().getBandwidthMhz().getValue());
		GeneralUtils.printToConsole("bandwidth");
		radioParams.setDuplex(
				netspanResult.getRadioProfileResult().get(0).getRadioProfile().getDuplexMode().getValue().toString());
		GeneralUtils.printToConsole("duplex");
		if (radioParams.getDuplex().equals("TDD")) {
			radioParams.setFrameConfig(netspanResult.getRadioProfileResult().get(0).getRadioProfile().getFrameConfig()
					.getValue().toString());
			GeneralUtils.printToConsole("frameconfig");
			radioParams.setSpecialSubFrame(
					netspanResult.getRadioProfileResult().get(0).getRadioProfile().getSsfType().getValue().toString());
			GeneralUtils.printToConsole("specialsubFrame");
		}
		radioParams.setBand(
				netspanResult.getRadioProfileResult().get(0).getRadioProfile().getBand().getValue());
		GeneralUtils.printToConsole("band");
		radioParams.setDownLinkFrequency(
				netspanResult.getRadioProfileResult().get(0).getRadioProfile().getDownlinkFreq().getValue());
		GeneralUtils.printToConsole("DownLink Freq");
		radioParams.setMfbi(netspanResult.getRadioProfileResult().get(0).getRadioProfile().getMfbiEnabled().getValue());
		GeneralUtils.printToConsole("MFBI");
		radioParams.setTxpower(
				netspanResult.getRadioProfileResult().get(0).getRadioProfile().getTxPower().getValue());
		GeneralUtils.printToConsole("tx power");
		radioParams.setUpLinkFrequency(
				netspanResult.getRadioProfileResult().get(0).getRadioProfile().getUplinkFreq().getValue());
		GeneralUtils.printToConsole("UpLink Freq");
		radioParams.setEarfcn(
				netspanResult.getRadioProfileResult().get(0).getRadioProfile().getEarfcn().getValue());
		GeneralUtils.printToConsole("Earfcn");
		return radioParams;
	}

	@Override
	public NodeManagementModeType getManagedMode(EnodeB enb) {
		EnbDetailsGet result = getNodeConfig(enb);
		if(result != null){
			return result.getManagedMode().getValue();
		}
		return null;
	}

	@Override
	public String getCurrentRadioProfileName(EnodeB enb) {
		EnbDetailsGet nodeConfig = getNodeConfig(enb);
		if(nodeConfig != null){
			return nodeConfig.getLteCell().get(0).getRadioProfile();
		}
		return "";
	}
	
	@Override
	public String getCurrentMobilityProfileName(EnodeB enb) {
		EnbDetailsGet nodeConfig = getNodeConfig(enb);
		if(nodeConfig != null){
			return nodeConfig.getLteCell().get(0).getMobilityProfile();
		}
		return "";
	}

	@Override
	public String getCurrentEnbAdvancedConfigurationProfileName(EnodeB enb) {
		EnbDetailsGet nodeConfig = getNodeConfig(enb);
		if(nodeConfig != null){
			return nodeConfig.getAdvancedConfigProfile();			
		}
		return "";
	}
	
	@Override
	public String getCurrentSyncProfileName(EnodeB enb) {
		EnbDetailsGet nodeConfig = getNodeConfig(enb);
		if(nodeConfig != null){
			return nodeConfig.getSyncProfile();
		}
		return "";
	}

	@Override
	public String getCurrentSecurityProfileName(EnodeB enb) {
		EnbDetailsGet nodeConfig = getNodeConfig(enb);
		if(nodeConfig != null){
			return nodeConfig.getSecurityProfile();
		}
		return "";		
	}

	@Override
	public String getCurrentManagmentProfileName(EnodeB enb) {
		EnbDetailsGet nodeConfig = getNodeConfig(enb);
		if(nodeConfig != null){
			return nodeConfig.getManagementProfile();
		}
		return "";
	}
	
	@Override
	public String getCurrentNetworkProfileName(EnodeB enb) {
		EnbDetailsGet nodeConfig = getNodeConfig(enb);
		if(nodeConfig != null){
			return nodeConfig.getNetworkProfile();
		}
		return "";
	}

	/**
	 * @author Gabriel Grunwald Name: isProfileExists Function: checks if a
	 *         profile exists Restrictions: only works for these profiles: -
	 *         Sync - Management - SON - Network - Security
	 */
	public boolean isProfileExists(String name, EnbProfiles profileType) {
		ArrayList<String> names = new ArrayList<String>();
		names.add(name);
		try {
			Netspan.NBI_14_50.API.Lte.WsResponse temp = (Netspan.NBI_14_50.API.Lte.WsResponse) helper_14_50
					.execute(profileType.value() + "ProfileGet", names);
			return temp.getErrorCode() == Netspan.NBI_14_50.API.Lte.ErrorCodes.OK;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public EnbNetworkProfile networkProfileGet(String profileName) {
		ArrayList<String> names = new ArrayList<String>();
		names.add(profileName);
		try {
			LteNetworkProfileGetResult lteNetworkProfile = (LteNetworkProfileGetResult) helper_14_50
					.execute("networkProfileGet", names);
			if (lteNetworkProfile.getErrorCode() == Netspan.NBI_14_50.API.Lte.ErrorCodes.OK) {
				return lteNetworkProfile.getNetworkProfileResult().get(0).getNetworkProfile();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public boolean cloneNetworkProfile(EnodeB node, String cloneFromName, NetworkParameters networkParams) throws Exception {
		
		EnbNetworkProfile networkProfile = new EnbNetworkProfile();

		if(networkParams.getProfileName() == null){return false;}
		networkProfile.setName(networkParams.getProfileName());
		
		// incase of plmn item to change in the profile
		if (networkParams.getPlmn() != null) {
			PlmnListContainer containerPlmn = new PlmnListContainer();
			ArrayList<LtePlmnEntryWs> listPlmnWs = new ArrayList<LtePlmnEntryWs>();
			for (Plmn item : networkParams.getPlmn()) {
				LtePlmnEntryWs plmnWs = new LtePlmnEntryWs();
				plmnWs.setMcc(item.getMCC());
				plmnWs.setMnc(item.getMNC());
				listPlmnWs.add(plmnWs);
				containerPlmn.getPlmn().add(plmnWs);
			}
			networkProfile.setPlmnList(containerPlmn);
		}

		// in case of mme ip to change in the profile
		if (networkParams.getMMEIP() != null) {
			S1ListContainer tempContainer = new S1ListContainer();
			LteS1EntryWs tempLteS1Entry = new LteS1EntryWs();
			tempLteS1Entry.setMmeIpAddress(networkParams.getMMEIP());
			tempContainer.getS1().add(tempLteS1Entry);
			// add IPG ip in the List
			networkProfile.setS1X2List(tempContainer);
		}
		
		
		if(networkParams.getcSonConfig() != null){
			
			ObjectFactory factoryDetails = new ObjectFactory();
			
			LteCSonEntryWs cSonConfig = new LteCSonEntryWs();
			cSonConfig.setIsCSonConfigured(factoryDetails.createLteCSonEntryWsIsCSonConfigured(networkParams.getIsCSonConfigured()));
			cSonConfig.setCSonIpAddress(networkParams.getcSonIpAddress());
			cSonConfig.setCSonServerPort(factoryDetails.createLteCSonEntryWsCSonServerPort(networkParams.getcSonServerPort()));
			
			networkProfile.setCSONConfig(cSonConfig);
		}

		ProfileResponse cloneResult = (ProfileResponse) helper_14_50.execute("networkProfileClone", cloneFromName, networkProfile);
		
		if ( cloneResult == null){
			report.report("Fail to get Network profile cloning result", Reporter.WARNING );
			return false;
		}
		else if( cloneResult.getErrorCode() == Netspan.NBI_14_50.API.Lte.ErrorCodes.OK ) {
			report.report("Succeeded to clone Network profile, new profile name: " + networkParams.getProfileName());
			return true;
		} else {
			report.report("Failed to clone Network profile, reason: " + cloneResult.getErrorString(), Reporter.WARNING);
			return false;
		}
	}

	/**
	 * @author Shahaf Shuhamy delete if the profile exists before trying to set
	 *         it making a security profile with the suffix : "_Connectivity"
	 * 
	 * @throws Exception
	 */
	// TODO: check if the name doesn't exceed 64 chars (or some other value)
	public boolean cloneSecurityProfile(EnodeB node, String cloneFromName, SecurityParameters securityParams) throws Exception {
		String profilePreFix = GeneralUtils.getPrefixAutomation();
		String currentSecurityProfileName = node.getDefaultNetspanProfiles().getSecurity();
		LteSecurityProfileGetResult currentSecurityProfile = null;
		ArrayList<String> profilesList = new ArrayList<String>();
		profilesList.add(currentSecurityProfileName);
		try {
			currentSecurityProfile = (LteSecurityProfileGetResult) helper_14_50.execute("securityProfileGet", profilesList);
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Fail executing SecurityProfileGet in Netspan 14_5");
		}

		if (currentSecurityProfile.getErrorCode() != Netspan.NBI_14_50.API.Lte.ErrorCodes.OK) {
			throw new Exception("Error Code is not ok after securityProfileGet method in netspan 14_5");
		} else {
			if (currentSecurityProfileName.length() >= 64) {
				StringBuilder a = new StringBuilder();
				a.append(currentSecurityProfileName);
				a.delete(a.length() - profilePreFix.length(), a.length() - 1);
				currentSecurityProfileName = a.toString();
			}
			// change the profileForTest Name to diffrent between Profiles
			//String securityProfileToClone = currentSecurityProfileName + profilePreFix;
			EnbSecurityProfile newSecurityProfile = new EnbSecurityProfile();
			newSecurityProfile = currentSecurityProfile.getSecurityProfileResult().get(0).getSecurityProfile();
			
			insertParamsToEnbSecurityObj(newSecurityProfile,
					currentSecurityProfile.getSecurityProfileResult().get(0).getSecurityProfile().getHardwareCategory(),
					securityParams.getProfileName(),
					securityParams.getIntegrityMode(),
					securityParams.getIntegrityNullLevel(),
					securityParams.getIntegrityAESLevel(),
					securityParams.getIntegritySNOWLevel(),
					securityParams.getIntegrityMode(),
					securityParams.getCipheringNullLevel(),
					securityParams.getCipheringAESLevel(),
					securityParams.getCipheringSNOWLevel());
			
			
			
			try {
				// gives the method the name of the new profile and the profile
				// as is that i would like to clone
				 ProfileResponse cloneResult = (ProfileResponse) helper_14_50.execute("securityProfileClone", currentSecurityProfileName, newSecurityProfile);
				 
				 if ( cloneResult == null){
						report.report("Fail to get Security profile cloning result", Reporter.WARNING );
						return false;
					}
					else if( cloneResult.getErrorCode() == Netspan.NBI_14_50.API.Lte.ErrorCodes.OK ) {
						report.report("Succeeded to clone Security profile, new profile name: " + securityParams.getProfileName());
						return true;
					} else {
						report.report("Failed to clone Security profile, reason: " + cloneResult.getErrorString(), Reporter.WARNING);
						return false;
					}
			} catch (Exception e) {
				e.printStackTrace();
				throw new Exception("Fail executing SecurityProfileClone in Netspan 14_5");
			}
		}
		//return false;
	}

	

	@Override
	public String getCurrentSystemDefaultProfileName(EnodeB enb) {
		EnbDetailsGet nodeConfig = getNodeConfig(enb);
		if(nodeConfig != null){
			return nodeConfig.getSystemDefaultProfile();			
		}
		return "";
	}

	/**
	 * 
	 * @param securityProfileToSendToClone
	 * @param currentSecurityProfileName
	 * @param integrityLevel
	 * @param nullIntegriLevel
	 * @param aesIntegriLevel
	 * @param showIntegriLevel
	 * @param cipheringLevel
	 * @param nullCipherLevel
	 * @param aesCipherLevel
	 * @param snowCipherLevel
	 */
	private void insertParamsToEnbSecurityObj(EnbSecurityProfile securityProfileToSendToClone,
			JAXBElement<CategoriesLte> hardware, String currentSecurityProfileName,
			SecurityProfileOptionalOrMandatory integrityLevel, int nullIntegriLevel, int aesIntegriLevel,
			int showIntegriLevel, SecurityProfileOptionalOrMandatory cipheringLevel, int nullCipherLevel,
			int aesCipherLevel, int snowCipherLevel) {
		// name
		securityProfileToSendToClone.setName(currentSecurityProfileName);
		// HardWare
		securityProfileToSendToClone.setHardwareCategory(hardware);
		// Integrity
		securityProfileToSendToClone.setSecurityForIntegrity(new JAXBElement<SecurityProfileOptionalOrMandatory>(
				new QName("SecurityForIntegrity"), SecurityProfileOptionalOrMandatory.class, null, integrityLevel));
		securityProfileToSendToClone.setNullIntegrityLevel(
				new JAXBElement<Integer>(new QName("NullIntegrityLevel"), Integer.class, null, nullIntegriLevel));
		securityProfileToSendToClone.setAesIntegrityLevel(
				new JAXBElement<Integer>(new QName("AesIntegrityLevel"), Integer.class, aesIntegriLevel));
		securityProfileToSendToClone.setSnow3GIntegrityLevel(
				new JAXBElement<Integer>(new QName("Snow3GIntegrityLevel"), Integer.class, null, showIntegriLevel));
		// Ciphering
		securityProfileToSendToClone.setSecurityForCiphering(new JAXBElement<SecurityProfileOptionalOrMandatory>(
				new QName("SecurityForCiphering"), SecurityProfileOptionalOrMandatory.class, null, cipheringLevel));
		securityProfileToSendToClone.setNullCipheringLevel(
				new JAXBElement<Integer>(new QName("NullCipheringLevel"), Integer.class, null, nullCipherLevel));
		securityProfileToSendToClone.setAesCipheringLevel(
				new JAXBElement<Integer>(new QName("AesCipheringLevel"), Integer.class, null, aesCipherLevel));
		securityProfileToSendToClone.setSnow3GCipheringLevel(
				new JAXBElement<Integer>(new QName("Snow3GCipheringLevel"), Integer.class, null, snowCipherLevel));
	}

	@Override
	public boolean movePnPConfigToNodeList(EnodeB enodeB) {
		boolean actionSucceeded = true;
		EnbPnpConfig pnpConfig = getPnpNode(enodeB);
		if (pnpConfig != null) {
			actionSucceeded &= removePnPNode(enodeB.getNetspanName());
			actionSucceeded &= createDiscoveryTaskV2(enodeB,"public","private");
			try {
				Thread.sleep(1000 * 60); // wait for the Node appearance in
											// netspan.
			} catch (InterruptedException e) {
			}
			actionSucceeded &= setNodeConfig(enodeB, EnbConfigGetToSet(pnpConfig.getLteEnbConfig()));
		} else {
			actionSucceeded &= createDiscoveryTaskV2(enodeB,"public","private");
		}
		actionSucceeded &= setManagedMode(enodeB.getNetspanName(), NodeManagementModeType.MANAGED);
		return actionSucceeded;
	}

	@Override
	public boolean convertToPnPConfig(EnodeB enodeB) {
		boolean actionSucceeded;
		LteEnbDetailsSetWs enbConfigSet = EnbConfigGetToSet(getNodeConfig(enodeB));
		PnpDetailWs pnpDetail = getPnpDetails(enodeB);
		PnpConfigCreate pnpConfigCreate = new PnpConfigCreate();
		pnpConfigCreate.setENBDetail(enbConfigSet);
		pnpConfigCreate.setPnpDetail(pnpDetail);
		removeDiscoveryTask(enodeB.getNetspanName());
		removeNode(enodeB.getNetspanName());
		actionSucceeded = createPnpNode(pnpConfigCreate);
		return actionSucceeded;
	}

	@Override
	public boolean setPnPSwVersion(EnodeB enodeB, String swVersion) {
		EnbPnpConfig enbPnpConfig = getPnpNode(enodeB);
		enbPnpConfig.getLtePnpConfig().setPnpSwImageName(swVersion);
		return updatePnPConfig(enodeB, enbPnpConfig);
	}

	@Override
	public boolean setPnPRadioProfile(EnodeB enodeB, String radioProfileName) {
		EnbPnpConfig enbPnpConfig = getPnpNode(enodeB);
		enbPnpConfig.getLteEnbConfig().getLteCell().get(FIRST_ELEMENT).setRadioProfile(radioProfileName);
		return updatePnPConfig(enodeB, enbPnpConfig);
	}

	public boolean updatePnPConfig(EnodeB enodeB, EnbPnpConfig pnpConfig) {
		GeneralUtils.printToConsole("Sending NBI requeset \"pnpConfigUpdate\"");
		Netspan.NBI_14_50.API.Lte.NodeActionResult result;
		try {
			result = (Netspan.NBI_14_50.API.Lte.NodeActionResult) helper_14_50.execute("pnpConfigUpdate",
					enodeB.getNetspanName(), pnpConfig.getLtePnpConfig(),
					EnbConfigGetToSet(pnpConfig.getLteEnbConfig()));
			
			GeneralUtils.printToConsole("NBI method \"pnpConfigUpdate\" returned value: " + result.getErrorCode().toString());
			return result.getErrorCode() == Netspan.NBI_14_50.API.Lte.ErrorCodes.OK;
		} catch (Exception e) {
			GeneralUtils.printToConsole(e.toString());
			e.printStackTrace();
		}
		return false;
	}

	public EnbPnpConfig getPnpNode(EnodeB enodeB) {
		return getPnpNode(enodeB.getNetspanName());
	}

	public EnbPnpConfig getPnpNode(String nodeName) {
		ArrayList<String> nodeList = new ArrayList<String>();
		nodeList.add(nodeName);
		if (isPnPNodeExists(nodeName)) {
			try {

				GeneralUtils.printToConsole("Sending NBI requeset \"PnpConfigGet\" for eNodeB " + nodeName);

				LtePnpConfigGetResult result = (LtePnpConfigGetResult) helper_14_50.execute("pnpConfigGet", nodeList,
						new ArrayList<String>());
				GeneralUtils.printToConsole(String.format("NBI method \"PnpConfigGet\" for eNodeB %s returned value: %s", nodeName,
						result.getErrorCode().toString()));
				if (result.getErrorCode() == Netspan.NBI_14_50.API.Lte.ErrorCodes.OK) {
					return result.getPnpConfig().get(FIRST_ELEMENT);
				}
				return null;
			} catch (Exception e) {
				GeneralUtils.printToConsole(e.toString());
				e.printStackTrace();
			}
		}
		return null;
	}

	private boolean isPnPNodeExists(String pnpNodeName) {
		GeneralUtils.printToConsole("Sending NBI requeset \"pnpConfigList\"");
		NodeListResult result;
		try {
			result = (NodeListResult) helper_14_50.execute("pnpConfigList");
			GeneralUtils.printToConsole("NBI method \"pnpConfigList\" returned value: " + result.getErrorCode().toString());
			if (result.getErrorCode() == Netspan.NBI_14_50.API.Lte.ErrorCodes.OK) {
				boolean isExist = false;
				for (NodeSimple node : result.getNode()) {
					if (node.getName().equals(pnpNodeName))
						isExist = true;
				}
				return isExist;
			}
		} catch (Exception e) {
			GeneralUtils.printToConsole(e.toString());
			e.printStackTrace();
		}
		return false;
	}

	public boolean createPnpNode(PnpConfigCreate pnpConfigCreate) {
		report.report(String.format("%s - Add node to PnP list.", pnpConfigCreate.getENBDetail().getName()));
		GeneralUtils.printToConsole("Sending NBI requeset \"pnpConfigCreate\"");
		Netspan.NBI_14_50.API.Lte.NodeActionResult result;
		try {
			result = (Netspan.NBI_14_50.API.Lte.NodeActionResult) helper_14_50.execute("pnpConfigCreate",
					pnpConfigCreate.getPnpDetail(), pnpConfigCreate.getENBDetail());
			GeneralUtils.printToConsole("NBI method \"pnpConfigCreate\" returned value: " + result.getErrorCode().toString());
			return result.getErrorCode() == Netspan.NBI_14_50.API.Lte.ErrorCodes.OK;
		} catch (Exception e) {
			GeneralUtils.printToConsole(e.toString());
			e.printStackTrace();
		}
		return false;
	}

	
	/**
	 * 
	 * @param enodeB
	 * @param version
	 * @param pswrdRead - if version is "V3" this param is "SnmpUsername"
	 * @param pswrdWrite - if version is "V3" this param is "SnmpPassword"
	 * @return
	 */
	public boolean createDiscoveryTaskV2(EnodeB enodeB, String pswrdRead, String pswrdWrite) {
		report.report(String.format("%s - Create discovery task V2.", enodeB.getNetspanName()));
		String ver = "discoveryTaskAddSnmpV2";
		GeneralUtils.printToConsole(String.format("Sending NBI request %s for eNodeB %s", ver, enodeB.getNetspanName()));
		WsResponse result;
		try {
			result = (WsResponse) helper_14_50.execute(ver, enodeB.getNetspanName(),
					enodeB.getIpAddress(), 161, pswrdRead, pswrdWrite);
			GeneralUtils.printToConsole(String.format("NBI method %s for eNodeB %s returned value: %s", ver, enodeB.getNetspanName(),
					result.getErrorCode().toString()));
			return result.getErrorCode() == ErrorCodes.OK;
		} catch (Exception e) {
			GeneralUtils.printToConsole(e.toString());
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean removePnPNode(String nodeName) {
		ArrayList<String> nodeList = new ArrayList<String>();
		nodeList.add(nodeName);
		try {
			GeneralUtils.printToConsole("Sending NBI requeset \"pnpConfigDelete\" for eNodeB " + nodeName);
			Netspan.NBI_14_50.API.Lte.NodeActionResult result = (Netspan.NBI_14_50.API.Lte.NodeActionResult) helper_14_50
					.execute("pnpConfigDelete", nodeList, new ArrayList<String>());

			GeneralUtils.printToConsole(String.format("NBI method \"pnpConfigDelete\" for eNodeB %s returned value: %s", nodeName,
					result.getErrorCode().toString()));
			return result.getErrorCode() == Netspan.NBI_14_50.API.Lte.ErrorCodes.OK;

		} catch (Exception e) {
			GeneralUtils.printToConsole(e.toString());
			e.printStackTrace();
		}
		return false;
	}

	public boolean setManagedMode(String nodeName, NodeManagementModeType managedMode) {
		ObjectFactory factoryDetails = new ObjectFactory();
		EnbDetailsGet enbConfigGet = getNodeConfig(nodeName);
		LteEnbDetailsSetWs enbConfigSet = EnbConfigGetToSet(enbConfigGet);
		enbConfigSet.setManagedMode(factoryDetails.createEnbDetailsGetManagedMode(managedMode));
		return setNodeConfig(nodeName, enbConfigSet);
	}

	public boolean removeNode(String nodeName) {
		report.report(String.format("%s - Remove node.", nodeName));
		ArrayList<String> nodeList = new ArrayList<String>();
		nodeList.add(nodeName);
		try {
			setManagedMode(nodeName, NodeManagementModeType.UNMANAGED);

			GeneralUtils.printToConsole("Sending NBI requeset \"nodeDelete\" for eNodeB " + nodeName);
			NodeActionResult result = (NodeActionResult) helper_14_50.execute("nodeDelete", nodeList,
					new ArrayList<String>());

			GeneralUtils.printToConsole(String.format("NBI method \"nodeDelete\" for eNodeB %s returned value: %s", nodeName,
					result.getErrorCode().toString()));
			return result.getErrorCode() == ErrorCodes.OK;

		} catch (Exception e) {
			GeneralUtils.printToConsole(e.toString());
			e.printStackTrace();
		}
		return false;
	}

	public boolean removeDiscoveryTask(String nodeName) {
		report.report(String.format("%s - Remove discovery task.", nodeName));
		ArrayList<String> nodeList = new ArrayList<String>();
		nodeList.add(nodeName);
		try {
			GeneralUtils.printToConsole("Sending NBI requeset \"discoveryTaskDelete\" for eNodeB " + nodeName);
			DiscoveryTaskActionResult result = (DiscoveryTaskActionResult) helper_14_50.execute("discoveryTaskDelete",
					nodeList);

			GeneralUtils.printToConsole(String.format("NBI method \"discoveryTaskDelete\" for eNodeB %s returned value: %s", nodeName,
					result.getErrorCode().toString()));

			return result.getErrorCode() == ErrorCodes.OK;
		} catch (Exception e) {
			GeneralUtils.printToConsole(e.toString());
			e.printStackTrace();
		}
		return false;
	}

	public PnpDetailWs getPnpDetails(EnodeB enodeB) {
		report.report(String.format("%s - Get PnP details.", enodeB.getNetspanName()));
		PnpDetailWs pnpDetails = new PnpDetailWs();
		NodeInfo nodeDetails = getNodeDetails(enodeB);
		ObjectFactory factoryDetails = new ObjectFactory();
		pnpDetails.setHardware(factoryDetails
				.createPnpDetailWsHardware(PnpHardwareTypes.fromValue(nodeDetails.hardwareType.replace(" ", ""))));
		pnpDetails.setPnpHardwareId(nodeDetails.nodeID);
		pnpDetails.setSnmpPort(nodeDetails.snmpPort);
		pnpDetails.setSnmpTimeoutInMilliSec(factoryDetails.createPnpDetailWsSnmpTimeoutInMilliSec(5000d));
		pnpDetails.setSnmpVersion(factoryDetails.createPnpDetailWsSnmpVersion(SnmpAgentVersion.V_2_C));
		pnpDetails.setSnmpReadContext("public");
		pnpDetails.setSnmpWriteContext("private");
		return pnpDetails;
	}

	public LteCellSetWs LteCellGetToSet(LteCellGetWs lteCellGet) {
		LteCellSetWs lteCellSet = new LteCellSetWs();
		lteCellSet.setCellAdvancedConfigurationProfile(lteCellGet.getCellAdvancedConfigurationProfile());
		lteCellSet.setCellID(lteCellGet.getCellID());
		lteCellSet.setEmbmsProfile(lteCellGet.getEmbmsProfile());
		lteCellSet.setEmergencyAreaId(lteCellGet.getEmergencyAreaId());
		if (lteCellGet.getIsAccessClassBarred().getValue().equals(true)) {
			lteCellSet.setIsAccessClassBarred(lteCellGet.getIsAccessClassBarred());
			lteCellSet.setIsEmergencyAccessBarred(lteCellGet.getIsEmergencyAccessBarred());
			if (lteCellGet.getIsSignalingAccessBarred().getValue().equals(true)) {
				lteCellSet.setIsSignalingAccessBarred(lteCellGet.getIsSignalingAccessBarred());
				lteCellSet.setSignalingAccessBarringFactor(lteCellGet.getSignalingAccessBarringFactor());
				lteCellSet.setSignalingAccessBarringTimeInSec(lteCellGet.getSignalingAccessBarringTimeInSec());
				lteCellSet.setIsSignalingAccessClass11Barred(lteCellGet.getIsSignalingAccessClass11Barred());
				lteCellSet.setIsSignalingAccessClass12Barred(lteCellGet.getIsSignalingAccessClass12Barred());
				lteCellSet.setIsSignalingAccessClass13Barred(lteCellGet.getIsSignalingAccessClass13Barred());
				lteCellSet.setIsSignalingAccessClass14Barred(lteCellGet.getIsSignalingAccessClass14Barred());
				lteCellSet.setIsSignalingAccessClass15Barred(lteCellGet.getIsSignalingAccessClass15Barred());
			}
			if (lteCellGet.getIsDataAccessBarred().getValue().equals(true)) {
				lteCellSet.setDataAccessBarringFactor(lteCellGet.getDataAccessBarringFactor());
				lteCellSet.setDataAccessBarringTimeInSec(lteCellGet.getDataAccessBarringTimeInSec());
				lteCellSet.setIsDataAccessBarred(lteCellGet.getIsDataAccessBarred());
				lteCellSet.setIsDataAccessClass11Barred(lteCellGet.getIsDataAccessClass11Barred());
				lteCellSet.setIsDataAccessClass12Barred(lteCellGet.getIsDataAccessClass12Barred());
				lteCellSet.setIsDataAccessClass13Barred(lteCellGet.getIsDataAccessClass13Barred());
				lteCellSet.setIsDataAccessClass14Barred(lteCellGet.getIsDataAccessClass14Barred());
				lteCellSet.setIsDataAccessClass15Barred(lteCellGet.getIsDataAccessClass15Barred());
			}
		}
		lteCellSet.setIsEnabled(lteCellGet.getIsEnabled());
		lteCellSet.setMobilityProfile(lteCellGet.getMobilityProfile());
		lteCellSet.setPhysicalLayerCellGroup(lteCellGet.getPhysicalLayerCellGroup());
		lteCellSet.setPhysicalLayerIdentity(lteCellGet.getPhysicalLayerIdentity());
		lteCellSet.setPrachFreqOffset(lteCellGet.getPrachFreqOffset());
		lteCellSet.setPrachRsi(lteCellGet.getPrachRsi());
		lteCellSet.setRadioProfile(lteCellGet.getRadioProfile());
		lteCellSet.setTrackingAreaCode(lteCellGet.getTrackingAreaCode());
		lteCellSet.setTrafficManagementProfile(lteCellGet.getTrafficManagementProfile());
		lteCellSet.setUtranProfile(lteCellGet.getUtranProfile());
		return lteCellSet;
	}

	public LteEnbDetailsSetWs EnbConfigGetToSet(EnbDetailsGet enbConfigGet) {
		LteEnbDetailsSetWs enbConfigSet = new LteEnbDetailsSetWs();
		enbConfigSet.setActiveAntenna(enbConfigGet.getActiveAntenna());
		enbConfigSet.setAdvancedConfigProfile(enbConfigGet.getAdvancedConfigProfile());
		enbConfigSet.setDefaultX2ControlStateForNeighbours(enbConfigGet.getDefaultX2ControlStateForNeighbours());
		enbConfigSet.setDescription(enbConfigGet.getDescription());
		enbConfigSet.setENodeBID(enbConfigGet.getENodeBID());
		if (enbConfigGet.getIsCSonServerInterfaceEnabled().getValue().toString().equals("ENABLED")) {
			enbConfigSet.setInterfaceToUseForCSonServer(enbConfigGet.getInterfaceToUseForCSonServer());
			enbConfigSet.setIsCSonServerInterfaceEnabled(enbConfigGet.getIsCSonServerInterfaceEnabled());
			enbConfigSet.setCSonServerIpAddress(enbConfigGet.getCSonServerIpAddress());
			enbConfigSet.setCSonServerSubnetMask(enbConfigGet.getCSonServerSubnetMask());
		}
		if (enbConfigGet.getIsPtpSlaveInterfaceEnabled().getValue().toString().equals("ENABLED")) {
			enbConfigSet.setInterfaceToUseForPtpSlave(enbConfigGet.getInterfaceToUseForPtpSlave());
			enbConfigSet.setIsPtpSlaveInterfaceEnabled(enbConfigGet.getIsPtpSlaveInterfaceEnabled());
			enbConfigSet.setPtpSlaveIpAddress(enbConfigGet.getPtpSlaveIpAddress());
			enbConfigSet.setPtpSlaveSubnetMask(enbConfigGet.getPtpSlaveSubnetMask());
		}
		if (enbConfigGet.getIsM2InterfaceEnabled().getValue().toString().equals("ENABLED")) {
			enbConfigSet.setIsM2InterfaceEnabled(enbConfigGet.getIsM2InterfaceEnabled());
			enbConfigSet.setInterfaceToUseForM2(enbConfigGet.getInterfaceToUseForM2());
			enbConfigSet.setM2IpAddress(enbConfigGet.getM2IpAddress());
			enbConfigSet.setM2SubnetMask(enbConfigGet.getM2SubnetMask());
		}
		if (enbConfigGet.getIsM1InterfaceEnabled().getValue().toString().equals("ENABLED")) {
			enbConfigSet.setIsM1InterfaceEnabled(enbConfigGet.getIsM1InterfaceEnabled());
			enbConfigSet.setInterfaceToUseForM1(enbConfigGet.getInterfaceToUseForM1());
			enbConfigSet.setM1IpAddress(enbConfigGet.getM1IpAddress());
			enbConfigSet.setM1SubnetMask(enbConfigGet.getM1SubnetMask());
		}
		if (enbConfigGet.getIsS1CInterfaceEnabled().getValue().toString().equals("ENABLED")) {
			enbConfigSet.setIsS1CInterfaceEnabled(enbConfigGet.getIsS1CInterfaceEnabled());
			enbConfigSet.setInterfaceToUseForS1C(enbConfigGet.getInterfaceToUseForS1C());
			enbConfigSet.setS1CIpAddress(enbConfigGet.getS1CIpAddress());
			enbConfigSet.setS1CSubnetMask(enbConfigGet.getS1CSubnetMask());
		}
		if (enbConfigGet.getIsS1UInterfaceEnabled().getValue().toString().equals("ENABLED")) {
			enbConfigSet.setIsS1UInterfaceEnabled(enbConfigGet.getIsS1UInterfaceEnabled());
			enbConfigSet.setInterfaceToUseForS1U(enbConfigGet.getInterfaceToUseForS1U());
			enbConfigSet.setS1UIpAddress(enbConfigGet.getS1UIpAddress());
			enbConfigSet.setS1USubnetMask(enbConfigGet.getS1USubnetMask());
		}
		if (enbConfigGet.getIsX2CInterfaceEnabled().getValue().toString().equals("ENABLED")) {
			enbConfigSet.setIsX2CInterfaceEnabled(enbConfigGet.getIsX2CInterfaceEnabled());
			enbConfigSet.setInterfaceToUseForX2C(enbConfigGet.getInterfaceToUseForX2C());
			enbConfigSet.setX2CIpAddress(enbConfigGet.getX2CIpAddress());
			enbConfigSet.setX2CSubnetMask(enbConfigGet.getX2CSubnetMask());
		}
		if (enbConfigGet.getIsX2UInterfaceEnabled().getValue().toString().equals("ENABLED")) {
			enbConfigSet.setIsX2UInterfaceEnabled(enbConfigGet.getIsX2UInterfaceEnabled());
			enbConfigSet.setInterfaceToUseForX2U(enbConfigGet.getInterfaceToUseForX2U());
			enbConfigSet.setX2UIpAddress(enbConfigGet.getX2UIpAddress());
			enbConfigSet.setX2USubnetMask(enbConfigGet.getX2USubnetMask());
		}
		enbConfigSet.setIpRouteList(enbConfigGet.getIpRouteList());
		enbConfigSet.setManagedMode(enbConfigGet.getManagedMode());
		enbConfigSet.setManagementProfile(enbConfigGet.getManagementProfile());
		enbConfigSet.setMax20MHzChannel(enbConfigGet.getMax20MHzChannel());
		enbConfigSet.setMax40MHzChannel(enbConfigGet.getMax40MHzChannel());
		enbConfigSet.setMin20MHzChannel(enbConfigGet.getMin20MHzChannel());
		enbConfigSet.setMin40MHzChannel(enbConfigGet.getMin40MHzChannel());
		enbConfigSet.setName(enbConfigGet.getName());
		enbConfigSet.setNetworkProfile(enbConfigGet.getNetworkProfile());
		enbConfigSet.setRegion(enbConfigGet.getRegion());
		enbConfigSet.setSecurityProfile(enbConfigGet.getSecurityProfile());
		enbConfigSet.setSite(enbConfigGet.getSite());
		enbConfigSet.setSonProfile(enbConfigGet.getSonProfile());
		enbConfigSet.setSyncProfile(enbConfigGet.getSyncProfile());
		enbConfigSet.setSystemDefaultProfile(enbConfigGet.getSystemDefaultProfile());
		enbConfigSet.getLteCell().add(LteCellGetToSet(enbConfigGet.getLteCell().get(FIRST_ELEMENT)));
		return enbConfigSet;
	}
	
	public boolean setProfile(EnodeB node, EnbProfiles profileType, String profileName){
		return setProfile(node, NO_CELL_ID_IS_NEEDED, profileType, profileName);
	}
	
	public boolean setProfile(EnodeB node, int cellid, EnbProfiles profileType, String profileName){
		
		ObjectFactory objectFactory = new ObjectFactory();
		LteEnbDetailsSetWs enbConfigSet = new LteEnbDetailsSetWs();
		LteCellSetWs lteCellSet = new LteCellSetWs();
		lteCellSet.setCellID(objectFactory.createLteCellGetWsCellID(String.valueOf(cellid)));
		
		switch (profileType) {
			case Management_Profile:
				enbConfigSet.setManagementProfile(profileName);
				break;
			case Mobility_Profile:
				lteCellSet.setMobilityProfile(profileName);
				
				break;
			case Network_Profile:
				enbConfigSet.setNetworkProfile(profileName);
				break;
			case Radio_Profile:
				lteCellSet.setRadioProfile(profileName);
				break;
			case Security_Profile:
				enbConfigSet.setSecurityProfile(profileName);
				break;
			case Son_Profile:
				enbConfigSet.setSonProfile(profileName);
				break;
			case Sync_Profile:
				enbConfigSet.setSyncProfile(profileName);
				break;
			case EnodeB_Advanced_Profile:
				enbConfigSet.setAdvancedConfigProfile(profileName);
				break;
			default:
				report.report("Enum: No Such EnbProfile", Reporter.WARNING);
				return false;
		}
		
		if(cellid != NO_CELL_ID_IS_NEEDED){
			enbConfigSet.getLteCell().add(lteCellSet);
		}
		
		try {
			Netspan.NBI_14_50.API.Lte.NodeActionResult result = (Netspan.NBI_14_50.API.Lte.NodeActionResult) helper_14_50
					.execute("enbConfigSet", node.getNetspanName(), enbConfigSet);
			if(result.getErrorCode() == Netspan.NBI_14_50.API.Lte.ErrorCodes.OK){
				report.report("Succeeded to set "+profileType.value()+" profile named "+profileName+" for " + node.getNetspanName());
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		report.report("Failed to set "+enbConfigSet.getName()+" profile for " + node.getName(), Reporter.WARNING);
		return false;
		
	}

	@Override
	public List<RFStatus> getRFStatus(EnodeB enodeB) {
		List<RFStatus> ret = new ArrayList<>();
		LteRfGetResult lteRFStatus = null;
		for (int i = 0; i < 3; i++) {

			try {
				lteRFStatus = (LteRfGetResult) helper_14_50.execute("enbRfStatusGet", enodeB.getNetspanName());
				break;
			} catch (Exception e) {
				GeneralUtils.unSafeSleep(5000);
			}
		}
		if (lteRFStatus == null)
			return ret;

		if (lteRFStatus.getErrorCode() != Netspan.NBI_14_50.API.Status.ErrorCodes.OK) {
			report.report(
					"Error accured while trying to get RF status: " + lteRFStatus.getErrorString());
		} else {
			for (LteRfStatusWs rfStatus : lteRFStatus.getRfStatus()) {
				RFStatus temp = new RFStatus();
				temp.ActualTxPowerDbm = GeneralUtils.tryParseStringToFloat(rfStatus.getActualTxPowerDbm());
				temp.ConfiguredTxPowerDbm = GeneralUtils.tryParseStringToFloat(rfStatus.getConfiguredTxPowerDbm());
				temp.ActualTxPower = rfStatus.getActualTxPowerDbm();
				temp.ConfiguredTxPower = rfStatus.getConfiguredTxPowerDbm();
				if(rfStatus.getMeasuredVswr().contains("("))
					temp.MeasuredVswr = rfStatus.getMeasuredVswr().split("\\(")[0];
				temp.OperationalStatus = rfStatus.getOperationalStatus();
				temp.RfNumber = rfStatus.getRfNumber();
				ret.add(temp);
			}
		}
		return ret;
	}	

	public boolean getNetworkProfileResult(String profileName, NetworkParameters np) throws Exception {
		LteNetworkProfileGetResult result = null;
		List<String> a = new ArrayList<String>();
		a.add(profileName);
		result = (LteNetworkProfileGetResult) helper_14_50.execute("networkProfileGet", a);
		try {
			// add parameters needed from profile to NetworkParameters Class for
			// future use.
			for (LtePlmnEntryWs item : result.getNetworkProfileResult().get(0).getNetworkProfile().getPlmnList()
					.getPlmn()) {
				np.addPLMN(item.getMcc(), item.getMnc());
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public String getMMEIpAdress(EnodeB enb) {
		ILteEnbDetailsGet temp = getNodeConfig(enb);
		EnbNetworkProfile network = networkProfileGet(temp.getNetworkProfile());
		String mmeIp = network.getS1X2List().getS1().get(0).getMmeIpAddress();
		return mmeIp;
	}

	@Override
	public String getPTPInterfaceIP(EnodeB enb) {
		EnbDetailsGet result = this.getNodeConfig(enb);
		if(result == null)
			return null;
		
		return result.getPtpSlaveIpAddress();
	}

	@Override
	public String getGMIP(EnodeB dut) {
		LteSyncProfileResult result = this.getSyncProfile(dut);
		
		if(result == null)
			return null;
		
		EnbSyncProfile sync = result.getSyncProfile();
		if(sync == null)
			return null;
		
		return sync.getPrimaryMasterIpAddress();
	}

	@Override
	public PTPStatus getPTPStatus(EnodeB dut) {
		//NodePtpGet
		PTPStatus returnObject = new PTPStatus();
		NodePtpGetResult ptpStatus;
		try {
			ptpStatus = (NodePtpGetResult) helper_14_50.execute("nodePtpGet", dut.getNetspanName());
		} catch (Exception e) {
			report.report("getPTPStatus via Netspan Fail",Reporter.WARNING);
			e.printStackTrace();
			return null;
		}
		
		if(ptpStatus.getErrorCode()!=Netspan.NBI_14_50.API.Status.ErrorCodes.OK){
			report.report("getPTPStatus via Netspan Fail",Reporter.WARNING);
			report.report(ptpStatus.getErrorString(),Reporter.WARNING);
			return null;
		}
		
		returnObject.enbName = ptpStatus.getPtpStatus().getName();
		returnObject.nodeId = ptpStatus.getPtpStatus().getNodeId();
		returnObject.masterConnectivity = ptpStatus.getPtpStatus().getMasterConnectivity();
		returnObject.syncStatus = ptpStatus.getPtpStatus().getSyncStatus();
		
		return returnObject;
	}
	
	@Override
	public boolean deleteAllAlarmsNode(EnodeB dut) {
		//AlarmListNode
		//AlarmDelete
		AlarmResultList alarmsList;
		AlarmActionResult deleteResult;
        List<String> argsNodeName = new ArrayList<String>();
        List<String> argsNodeId = new ArrayList<String>();
        List<String> argsAlarmId;
        argsNodeName.add(dut.getNetspanName());
		try {
			alarmsList = (AlarmResultList) helper_14_50.execute("alarmListNode", argsNodeName, argsNodeId);
			List<Alarm> alarms = alarmsList.getAlarm();
			for(Alarm alarm : alarms) {
				argsAlarmId = new ArrayList<String>();
				argsAlarmId.add(alarm.getAlarmId().toString());
				deleteResult = (AlarmActionResult) helper_14_50.execute("alarmDelete", argsAlarmId);
				if(deleteResult.getErrorCode()!=Netspan.NBI_14_50.API.FaultManagement.ErrorCodes.OK) {
					report.report("deleteAllAlarmsNode via Netspan Fail",Reporter.WARNING);
					report.report(deleteResult.getErrorString(),Reporter.WARNING);
					return false;
				}
			}
		} catch (Exception e) {
			report.report("deleteAllAlarmsNode via Netspan Fail",Reporter.WARNING);
			e.printStackTrace();
			return false;
		}
		report.report("Deleting all alarms of "+dut.getNetspanName()+" via Netspan succeeded");
		return true;
	}
	
	@Override
	public List<AlarmInfo> getAllAlarmsNode(EnodeB dut) {
		//AlarmListNode
		AlarmResultList alarmsList;
        List<String> argsNodeName = new ArrayList<String>();
        List<String> argsNodeId = new ArrayList<String>();
        argsNodeName.add(dut.getNetspanName());
        List<AlarmInfo> ret = new ArrayList<AlarmInfo>();
        
        try {
        	alarmsList = (AlarmResultList) helper_14_50.execute("alarmListNode", argsNodeName, argsNodeId);
		} catch (Exception e) {
			report.report("getAllAlarmsNode via Netspan Fail due to: "+e.getMessage(),Reporter.WARNING);
			e.printStackTrace();
			return ret;
		}
		
		if(alarmsList.getErrorCode()!=Netspan.NBI_14_50.API.FaultManagement.ErrorCodes.OK){
			report.report("getAllAlarmsNode via Netspan Fail : "+alarmsList.getErrorString(),Reporter.WARNING);
			return ret;
		}
		
		for(Alarm alarm: alarmsList.getAlarm()) {
			AlarmInfo temp = new AlarmInfo();
			temp.alarmId = alarm.getAlarmId();
			temp.alarmType = alarm.getAlarmType();
			temp.alarmSource = alarm.getAlarmSource();
			temp.alarmInfo = alarm.getAlarmInfo();
			temp.severity = alarm.getSeverity();
			temp.firstReceived = alarm.getFirstReceived();
			temp.lastReceived = alarm.getLastReceived();
			temp.alarmCount = alarm.getAlarmCount();
			temp.acknowledged = alarm.isAcknowledged();
			temp.userName = alarm.getUserName();
			ret.add(temp);
		}
		return ret;
	}
	
	@Override
	public boolean deleteAlarmById(String alarmId) {
		//AlarmDelete
		AlarmActionResult deleteResult;
        List<String> argsAlarmId = new ArrayList<>();
		try {
			argsAlarmId.add(alarmId);
			deleteResult = (AlarmActionResult) helper_14_50.execute("alarmDelete", argsAlarmId);
			if(deleteResult.getErrorCode()!=Netspan.NBI_14_50.API.FaultManagement.ErrorCodes.OK) {
				report.report("deleteAlarmById via Netspan Fail",Reporter.WARNING);
				report.report(deleteResult.getErrorString(),Reporter.WARNING);
				return false;
			}
		} catch (Exception e) {
			report.report("deleteAlarmById via Netspan Fail",Reporter.WARNING);
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public String getNetspanVersion() {
		if (netspanVersion == null) {
			NmsInfoResponse nms;
			try {
				nms = (NmsInfoResponse) helper_14_50.execute("nmsInfoGet");
				setNetspanVersion(nms.getNMSSoftwareVersion());
			} catch (Exception e) {
				setNetspanVersion("Pre 14.50");
			}

		}
		return netspanVersion;
	}

	@Override
	public boolean changeCurrentRadioProfileWithoutClone(EnodeB dut, RadioParameters rp) {
		
		EnbRadioProfile radioProfile =CreateEnbRadioProfile(rp);
		try {
			ProfileResponse response = (ProfileResponse) helper_14_50.execute("radioProfileUpdate",rp.getProfileName(),radioProfile);
			if(response.getErrorCode() != Netspan.NBI_14_50.API.Lte.ErrorCodes.OK){
				report.report(response.getErrorString(),Reporter.WARNING);
				return false;
			}
			else
				return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	@Override
	public List<EventInfo> getAllEventsNode(EnodeB dut) {
		report.report("getAllEventsNode via NBI_14_5 Failed : Try to use correct NBI version", Reporter.WARNING);
		return null;
	}
	
	@Override
	public List<EventInfo> getAllEventsNode(EnodeB dut, Date startTime) {
		report.report("getAllEventsNode via NBI_14_5 Failed : Try to use correct NBI version", Reporter.WARNING);
		return null;
	}
	
	@Override
	public List<EventInfo> getEventsNodeByDateRange(EnodeB dut, Date from, Date to){
		report.report("getEventsNodeByDateRange via NBI_14_5 Failed : Try to use correct NBI version", Reporter.WARNING);
		return null;
	}

	@Override
	public boolean checkIfEsonServerConfigured(EnodeB enb) {
		report.report("checkIfEsonServerConfigured via NBI_14_5 Failed : Try to use correct NBI version", Reporter.WARNING);
		return false;
	}

	@Override
	public boolean setEnbType(String nodeName, EnbTypes type) {
		report.report("setEnbType via NBI_14_5 Failed : Try to use correct NBI version", Reporter.WARNING);
		return false;
	}

	@Override
	public boolean performReProvision(String nodeName) {
		report.report("performReProvision via NBI_14_5 Failed : Try to use correct NBI version", Reporter.WARNING);
		return false;
	}
	
	@Override
	public SONStatus getSONStatus(EnodeB dut) {
		report.report("getSONStatus via NBI_14_5 Failed : Try to use correct NBI version", Reporter.WARNING);
		return null;
	}
	
	@Override
	public EnbCellProperties getEnbCellProperties(EnodeB enb) {
		EnbDetailsGet enbDetails = getNodeConfig(enb);
		for (LteCellGetWs lteCell : enbDetails.getLteCell()) {
			if ((lteCell.getCellID()!=null) && (Integer.parseInt(lteCell.getCellID().getValue())==enb.getCellContextID())) {
				EnbCellProperties res = new EnbCellProperties();
				res.cellNumber = (lteCell.getCellID()!=null) ? lteCell.getCellID().getValue() : null;
				res.cellIdentity28Bit = (lteCell.getCellIdentity28Bit()!=null) ? lteCell.getCellIdentity28Bit().getValue() : null;
				res.physicalLayerCellGroup = (lteCell.getPhysicalLayerCellGroup()!=null) ? lteCell.getPhysicalLayerCellGroup().getValue() : null;
				res.physicalLayerIdentity = (lteCell.getPhysicalLayerIdentity()!=null) ? lteCell.getPhysicalLayerIdentity().getValue() : null;
				res.physicalCellId = (lteCell.getPhysicalCellId()!=null) ? lteCell.getPhysicalCellId().getValue() : null;
				res.prachRsi = (lteCell.getPrachRsi()!=null) ? lteCell.getPrachRsi().getValue() : null;
				res.trackingAreaCode = (lteCell.getTrackingAreaCode()!=null) ? lteCell.getTrackingAreaCode().getValue() : null;
				res.emergencyAreaId = (lteCell.getEmergencyAreaId()!=null) ? lteCell.getEmergencyAreaId().getValue() : null;
				res.prachFreqOffset = (lteCell.getPrachFreqOffset()!=null) ? lteCell.getPrachFreqOffset().getValue() : null;
				res.cellAdvancedConfigurationProfile = lteCell.getCellAdvancedConfigurationProfile();
				res.radioProfile = lteCell.getRadioProfile();
				res.mobilityProfile = lteCell.getMobilityProfile();
				res.embmsProfile = lteCell.getEmbmsProfile();
				res.utranProfile = lteCell.getUtranProfile();
				res.trafficManagementProfile = lteCell.getTrafficManagementProfile();				
				res.isEnabled = (lteCell.getIsEnabled()!=null) ? lteCell.getIsEnabled().getValue() : null;
				return res;
			}
		}
		return null;
	}
	@Override
	public boolean setPci(EnodeB dut, int pci) {
		report.report("setPci via NBI_14_5 Failed : Try to use correct NBI version", Reporter.WARNING);
		return false;
	}
	@Override
	public boolean cloneManagementProfile(EnodeB node, String cloneFromName, ManagementParameters managementParmas) {
		report.report("cloneManagementProfile via NBI_14_5 Failed : Try to use correct NBI version", Reporter.WARNING);
		return false;
	}

	@Override
	public boolean cloneSystemDefaultProfile(EnodeB node, String cloneFromName,
			SystemDefaultParameters systemDefaultParmas) {
		report.report("cloneSystemDefaultProfile via NBI_14_5 Failed : Try to use correct NBI version", Reporter.WARNING);
		return false;
	}

	@Override
	public boolean isRebootRequestedMaintenanceWindowStatus(EnodeB node) {
		report.report("isRebootRequestedMaintenanceWindowStatus via NBI_14_5 Failed : Try to use correct NBI version", Reporter.WARNING);
		return false;
	}
	
	@Override
	public boolean cloneEnodeBAdvancedProfile(EnodeB node, String cloneFromName, EnodeBAdvancedParameters advancedParmas) {
		report.report("cloneAdvancedProfile via NBI_14_5 Failed : Try to use correct NBI version", Reporter.WARNING);
		return false;
	}
	@Override
	public String getCurrentSonProfileName(EnodeB node) {
		EnbDetailsGet nodeConfig = getNodeConfig(node);
		if(nodeConfig != null){
			return nodeConfig.getSonProfile();			
		}
		return null;
	}
	@Override
	public boolean setProfile(EnodeB node, HashMap<INetspanProfile, Integer> profilesPerCell) {
		report.report("setProfile via NBI_14_5 Failed : Try to use correct NBI version", Reporter.WARNING);
		return false;
	}
	@Override
	public boolean updateNeighborManagementProfile(EnodeB node, String cloneFromName,
			NeighbourManagementParameters neighbourManagementParams) {
		report.report("updateNeighborManagementProfile via NBI_14_5 Failed : Try to use correct NBI version", Reporter.WARNING);
		return false;
	}

	@Override
	public boolean cloneNeighborManagementProfile(EnodeB node, String cloneFromName,
			NeighbourManagementParameters neighbourManagementParams) {
		report.report("cloneNeighborManagementProfile via NBI_14_5 Failed : Try to use correct NBI version", Reporter.WARNING);
		return false;
	}

	@Override
	public String getNeighborManagmentProfile(String nodeName) {
		report.report("getNeighborManagmentProfile via NBI_14_5 Failed : Try to use correct NBI version", Reporter.WARNING);
		return null;
	}

	
	@Override
	public boolean updateRadioProfile(EnodeB node, RadioParameters radioParmas) {
		report.report("updateRadioProfile via NBI_14_5 Failed : Try to use correct NBI version", Reporter.WARNING);
		return false;
	}

	@Override
	public NeighborData getNeighborData(String nodeName, String nghName) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public boolean setEnbCellProperties(EnodeB dut, EnbCellProperties cellProperties){
		report.report("setEnbCellProperties via NBI_14_5 Failed : Try to use correct NBI version", Reporter.WARNING);
		return false;
	}

	@Override
	public boolean updateSonProfile(EnodeB node, String cloneFromName, SonParameters sonParmas) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean checkCannotAddNeighbor(EnodeB enodeB, EnodeB neighbor, HoControlStateTypes hoControlStatus,
			X2ControlStateTypes x2ControlStatus, HandoverType handoverType, boolean isStaticNeighbor,
			String qOffsetRange) {
		String sourceNodeName = enodeB.getNetspanName();
		String neighborName = neighbor.getNetspanName();
		GeneralUtils.printToConsole("Sending NBI requeset \"lteNeighbourAdd\" for eNodeB " + sourceNodeName);
		try {
			LteNeighbourResponse result = (LteNeighbourResponse) helper_14_50
					.execute("lteNeighbourAdd", sourceNodeName, neighborName, hoControlStatus, x2ControlStatus,
							handoverType, isStaticNeighbor, qOffsetRange);
			GeneralUtils.printToConsole(String.format("NBI method \"lteNeighbourAdd\" for eNodeB %s returned value: %s", sourceNodeName,
					result.getErrorCode().toString()));
			if (result.getErrorCode() != Netspan.NBI_14_50.API.Lte.ErrorCodes.OK) {
				report.report("lteNeighbourAdd via Netspan Failed : " + result.getErrorString());
				return true;
			}
			else {
				report.report("lteNeighbourAdd via Netspan succeeded. added neighbor: " + neighbor.getNetspanName()
				+ " for eNodeB: " + enodeB.getNetspanName(), Reporter.WARNING);
				return false;
			}
		} catch (Exception e) {
			GeneralUtils.printToConsole(e.toString());
			e.printStackTrace();
			report.report("lteNeighbourAdd via Netspan Failed due to: " + e.getMessage(), Reporter.WARNING);
		}
		return false;
	}
	public boolean createSoftwareServer(EnodeBUpgradeServer upgradeServer) {
		report.report("createSoftwareServer via NBI_14_5 Failed : Try to use correct NBI version", Reporter.WARNING);
		return false;
	}

	@Override
	public EnodeBUpgradeServer getSoftwareServer(String name) {
		report.report("getSoftwareServer via NBI_14_5 Failed : Try to use correct NBI version", Reporter.WARNING);
		return null;
	}

	@Override
	public boolean setSoftwareServer(String name, EnodeBUpgradeServer upgradeServer) {
		report.report("setSoftwareServer via NBI_14_5 Failed : Try to use correct NBI version", Reporter.WARNING);
		return false;
	}

	@Override
	public boolean deleteSoftwareServer(String name) {
		report.report("deleteSoftwareServer via NBI_14_5 Failed : Try to use correct NBI version", Reporter.WARNING);
		return false;
	}

	@Override
	public boolean cloneMultiCellProfile(EnodeB node, String cloneFromName, MultiCellParameters multiCellParams) {
		report.report("cloneMultiCellProfile via NBI_14_5 Failed : Try to use correct NBI version", Reporter.WARNING);
		return false;
	}

	@Override
	public boolean createSoftwareImage(EnodeBUpgradeImage upgradeImage) {
		report.report("createSoftwareImage via NBI_14_5 Failed : Try to use correct NBI version", Reporter.WARNING);
		return false;
	}

	@Override
	public EnodeBUpgradeImage getSoftwareImage(String upgradeImagename) {
		report.report("getSoftwareImage via NBI_14_5 Failed : Try to use correct NBI version", Reporter.WARNING);
		return null;
	}

	@Override
	public HardwareCategory getHardwareCategory(EnodeB node) {
		report.report("getHardwareCategory via NBI_14_5 Failed : Try to use correct NBI version", Reporter.WARNING);
		return null;
	}

	@Override
	public HardwareCategory LTECategoriesToHardwareCategory(CategoriesLte categories) {
		report.report("LTECategoriesToHardwareCategory via NBI_14_5 Failed : Try to use correct NBI version", Reporter.WARNING);
		return null;
	}

	@Override
	public boolean softwareConfigSet(String nodeName, Netspan.API.Software.RequestType requestType, String softwareImage) {
		report.report("softwareConfigSet via NBI_14_5 Failed : Try to use correct NBI version", Reporter.WARNING);
		return false;
	}

	@Override
	public SoftwareStatus getSoftwareStatus(String nodeName, ImageType imageType) {
		report.report("getSoftwareStatus via NBI_14_5 Failed : Try to use correct NBI version", Reporter.WARNING);
		return null;
	}


	public int getDLTPTPerQci(String nodeName, int qci) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean getDicicUnmanagedInterferenceStatus(String nodeName, int nghPci) {
		report.report("getDicicUnmanagedInterferenceStatus via NBI_14_5 Failed : Try to use correct NBI version", Reporter.WARNING);
		return false;
	}
	@Override
	public boolean updateSoftwareImage(EnodeBUpgradeImage upgradeImage) {
		report.report("updateSoftwareImage via NBI_14_5 Failed : Try to use correct NBI version", Reporter.WARNING);
		return false;
	}

	@Override
	public boolean deleteSoftwareImage(String upgradeImageName) {
		report.report("deleteSoftwareImage via NBI_14_5 Failed : Try to use correct NBI version", Reporter.WARNING);
		return false;
	}

	@Override
	public boolean updateNetworkProfile(EnodeB node, String cloneFromName, NetworkParameters networkParams) {
		report.report("updateNetworkProfile via NBI_14_5 Failed : Try to use correct NBI version", Reporter.WARNING);
		return false;
	}

	@Override
	public boolean isPnpNode(String nodeName) {
		report.report("isPnpNode via NBI_14_5 Failed : Try to use correct NBI version", Reporter.WARNING);
		return false;
	}

	@Override
	public boolean getNetspanProfile(EnodeB node, String cloneFromName, INetspanProfile profileParams) {
		report.report("Netspan 14_5 has no implementation for this method");
		return false;
	}

	@Override
	public boolean getRawNetspanGetProfileResponse(EnodeB node, String cloneFromName, INetspanProfile profileParams) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean setLatitudeAndLongitude(EnodeB node, BigDecimal Latitude, BigDecimal Longitude) {
		report.report("this Method IS NOT implemented for 14_5");
		return false;
	}
	public String getMultiCellProfileName(EnodeB enb) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean cloneCellAdvancedProfile(EnodeB node, String cloneFromName, CellAdvancedParameters advancedParmas) {
		report.report("cloneCellBAdvancedProfile via NBI_14_5 Failed : Try to use correct NBI version", Reporter.WARNING);
		return false;
	}
	
	@Override
	public boolean updateCellAdvancedProfile(EnodeB node, CellAdvancedParameters advancedParmas) {
		report.report("updateCellBAdvancedProfile via NBI_14_5 Failed : Try to use correct NBI version", Reporter.WARNING);
		return false;
	}

	@Override
	public String getCurrentCellAdvancedConfigurationProfileName(EnodeB enb) {
		report.report("getCurrentCellAdvancedConfigurationProfileName via NBI_14_5 Failed : Try to use correct NBI version", Reporter.WARNING);
		return null;
	}

	@Override
	public String getSyncState(EnodeB eNodeB) {
		report.report("getSyncState method is not implemented for this netspan(14_5)!", Reporter.WARNING);
		return null;
	}
	
	@Override
	public boolean checkAndSetDefaultProfiles(EnodeB node,boolean setProfile){
		report.report("setDefualtProfiles method is not implemented for this netspan(14_5)!", Reporter.WARNING);
		return false;
	}

	@Override
	public boolean setMultiCellState(EnodeB node, Boolean isEnabled) {
		report.report("setMultiCellState method is not implemented for this netspan(14_5)!", Reporter.WARNING);
		return false;
	}

	@Override
	public boolean isNetspanReachable() {
		// TODO Auto-generated method stub
		return true;
	}
	
	@Override
	public List<LteBackhaul> getBackhaulInterfaceStatus(EnodeB node) {
		report.report("getBackhaulInterfaceStatus method is not implemented for this netspan(14_5)!");
		return null;
	}

	@Override
	public ArrayList<String> getMMEIpAdresses(EnodeB enb) {
		report.report("getMMEIpAdresses method is not implemented for this netspan(14_5)!", Reporter.WARNING);
		return null;
	}
	
	@Override
	public String getGPSStatus(EnodeB enb) {
		report.report("getGPSStatus method is not implemented for this netspan(14_5)!", Reporter.WARNING);
		return null;
	}
	
	@Override
	public String getMangementIp(EnodeB enb) {
		report.report("getMangementIp method is not implemented for this netspan(14_5)!", Reporter.WARNING);
		return GeneralUtils.ERROR_VALUE+"";
	}

	@Override
	public int getNumberOfActiveCellsForNode(EnodeB node) {
		return 1;
	}

	@Override
	public boolean resetNode(String nodeName, RebootType rebootType) {
		report.report("resetNode method is not implemented for this netspan(14_5)!", Reporter.WARNING);
		return false;
	}
	
	@Override
	public boolean setEnbAccessClassBarring(EnodeB dut, CellBarringPolicyParameters cellBarringParams) {
		report.report("setEnbAccessClassBarring method is not implemented for this netspan(14_5)!", Reporter.WARNING);
		return false;
	}

	@Override
	public String getImageType(String nodeName) {
		report.report("getImageType method is not implemented for this netspan(14_5)!", Reporter.WARNING);
		return null;
	}
	
	@Override
	public int getMaxUeSupported(EnodeB enb) {
		report.report("getMaxUeSupported method is not implemented for this netspan(14_5)!", Reporter.WARNING);
		return 0;
	}

	/**
	 * get Running Version Of Enb
	 *
	 * @param enodeB - enodeB
	 * @return - Running version
	 */
	public  String getRunningVersionOfEnb(EnodeB enodeB){
		return null;
	}

	/**
	 * get StandBy Version Of Enb
	 *
	 * @param enodeB - enodeB
	 * @return - StandBy version
	 */
	public  String getStandByVersionOfEnb(EnodeB enodeB){
		return null;
	}

	@Override
	public Pair<Integer, Integer> getUlDlTrafficValues(String nodeName) {
		GeneralUtils.printToConsole("getUlDlTrafficValues function is not implemented for this netspan(14_5)!");
		return null;
	}
}

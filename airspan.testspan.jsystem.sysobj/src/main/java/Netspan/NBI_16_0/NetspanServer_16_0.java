package Netspan.NBI_16_0;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import EnodeB.EnodeB;
import Netspan.API.Enums.CategoriesLte;
import Netspan.API.Enums.EnabledDisabledStates;
import Netspan.API.Enums.NodeManagementModeType;
import Netspan.API.Enums.PrimaryClockSourceEnum;
import Netspan.API.Lte.EventInfo;
import Netspan.NBI_15_5.NetspanServer_15_5;
import Netspan.NBI_16_0.FaultManagement.Event;
import Netspan.NBI_16_0.FaultManagement.EventResultList;
import Netspan.NBI_16_0.Inventory.NodeActionResult;
import Netspan.NBI_16_0.Lte.AirSonWs;
import Netspan.NBI_16_0.Lte.ClockSources;
import Netspan.NBI_16_0.Lte.DuplexModeTypes;
import Netspan.NBI_16_0.Lte.EnbAdvancedProfile;
import Netspan.NBI_16_0.Lte.EnbDetailsGet;
import Netspan.NBI_16_0.Lte.EnbRadioProfile;
import Netspan.NBI_16_0.Lte.EnbSyncProfile;
import Netspan.NBI_16_0.Lte.LteCellGetWs;
import Netspan.NBI_16_0.Lte.LteEnbConfigGetResult;
import Netspan.NBI_16_0.Lte.LtePnpConfigGetResult;
import Netspan.NBI_16_0.Lte.LteSyncProfileGetResult;
import Netspan.NBI_16_0.Lte.NlSyncWs;
import Netspan.NBI_16_0.Lte.NlmIntraFreqScan;
import Netspan.NBI_16_0.Lte.NlmIntraFreqScanListContainer;
import Netspan.NBI_16_0.Lte.NlmWs;
import Netspan.NBI_16_0.Lte.ObjectFactory;
import Netspan.NBI_16_0.Lte.ProfileResponse;
import Netspan.NBI_16_0.Lte.RfSubframe;
import Netspan.NBI_16_0.Lte.TddFrameConfigurationsSupported;
import Netspan.Profiles.EnodeBAdvancedParameters;
import Netspan.Profiles.INetspanProfile;
import Netspan.Profiles.RadioParameters;
import Netspan.Profiles.SyncParameters;
import Utils.GeneralUtils;
import jsystem.framework.report.Reporter;


public class NetspanServer_16_0 extends NetspanServer_15_5 implements Netspan_16_0_abilities{
	public SoapHelper soapHelper_16_0;
	private static final String USERNAME = "wsadmin";
	private static final String PASSWORD = "password";
	private static final Netspan.NBI_16_0.Lte.Credentials credentialsLte = new Netspan.NBI_16_0.Lte.Credentials();
	private static final Netspan.NBI_16_0.Inventory.Credentials credentialsInventory = new Netspan.NBI_16_0.Inventory.Credentials();
	private static final Netspan.NBI_16_0.FaultManagement.Credentials credentialsFaultManagement = new Netspan.NBI_16_0.FaultManagement.Credentials();
	private static final Netspan.NBI_16_0.Statistics.Credentials credentialsStatistics = new Netspan.NBI_16_0.Statistics.Credentials();
	private static final Netspan.NBI_16_0.Status.Credentials credentialsStatus = new Netspan.NBI_16_0.Status.Credentials();
	private static final Netspan.NBI_16_0.Server.Credentials credentialsServer = new Netspan.NBI_16_0.Server.Credentials();
	private static final Netspan.NBI_16_0.Backhaul.Credentials credentialsBackhaul = new Netspan.NBI_16_0.Backhaul.Credentials();
	private static final Netspan.NBI_16_0.Software.Credentials credentialsSoftware = new Netspan.NBI_16_0.Software.Credentials();

	@Override
	public void init() throws Exception {
		if (NBI_VERSION == null) {
			NBI_VERSION = "16_0";
		}
		super.init();
		this.soapHelper_16_0 = new SoapHelper(getHostname());
		credentialsLte.setUsername(USERNAME);
		credentialsLte.setPassword(PASSWORD);
		credentialsInventory.setUsername(USERNAME);
		credentialsInventory.setPassword(PASSWORD);
		credentialsFaultManagement.setUsername(USERNAME);
		credentialsFaultManagement.setPassword(PASSWORD);
		credentialsStatistics.setUsername(USERNAME);
		credentialsStatistics.setPassword(PASSWORD);
		credentialsStatus.setUsername(USERNAME);
		credentialsStatus.setPassword(PASSWORD);
		credentialsServer.setUsername(USERNAME);
		credentialsServer.setPassword(PASSWORD);
		credentialsBackhaul.setUsername(USERNAME);
		credentialsBackhaul.setPassword(PASSWORD);
		credentialsSoftware.setUsername(USERNAME);
		credentialsSoftware.setPassword(PASSWORD);
		//populateNodeNames();
	}
	
	@Override
	public PrimaryClockSourceEnum getPrimaryClockSource(EnodeB node){
		EnbDetailsGet config = getNodeConfig(node);
		
		String profile = config.getSyncProfile();
		List<String> nameList = new ArrayList<String>();
		nameList.add(profile);
		
		List<Netspan.API.Enums.CategoriesLte> cat = new  ArrayList<>();
		
		try{
			LteSyncProfileGetResult syncProfile = ( LteSyncProfileGetResult ) soapHelper_16_0.getLteSoap().syncProfileGet(nameList, cat , credentialsLte);
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
			if(clockSource.getValue() == ClockSources.NLM){
				return PrimaryClockSourceEnum.NLM;
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
	 * Clone sync profile via netspan.
	 *
	 * @param profile
	 *            the profile
	 * @param cloneFromName
	 *            the clone from name
	 * @return true, if successful
	 */
	@Override
	public boolean cloneSyncProfile(EnodeB node, String cloneFromName, SyncParameters syncParams) {

		EnbSyncProfile syncProfile = new EnbSyncProfile();
		syncProfile.setName(syncParams.getProfileName());
		ObjectFactory objectFactory = new ObjectFactory();

		
		if (syncParams.getPrimaryClockSource() != null){
			syncProfile.setPrimaryClockSource(objectFactory.createEnbSyncProfileParamsPrimaryClockSource(ClockSources.fromValue(syncParams.getPrimaryClockSource())));
			if(syncParams.getPrimaryClockSource().equals("NLM")){
				syncProfile.setNlmIntraFreqScanMode(objectFactory.createEnbSyncProfileParamsNlmIntraFreqScanMode(syncParams.getNlmIntraFrequencyScanMode()));
				
				NlmIntraFreqScanListContainer array = new NlmIntraFreqScanListContainer();
				for(Integer earfcn : syncParams.getNlmIntraFrequencyScanList()){
					NlmIntraFreqScan freq = new NlmIntraFreqScan();
					freq.setEarfcn(earfcn);
					array.getNlmIntraFreqScan().add(freq);
				}
				syncProfile.setNlmIntraFreqScanList(array);
			}
			else {
				if (syncParams.getPrimaryMasterIpAddress() != null) {
					syncProfile.setPrimaryMasterIpAddress(syncParams.getPrimaryMasterIpAddress());
				}
				if (syncParams.getPrimaryMasterDomain() != null) {
					syncProfile.setPrimaryMasterDomain(
							objectFactory.createEnbSyncProfileParamsPrimaryMasterDomain(syncParams.getPrimaryMasterDomain()));
				}
				if (syncParams.getSecondaryMasterIpAddress() != null) {
					syncProfile.setSecondaryMasterIpAddress(syncParams.getSecondaryMasterIpAddress());
					if (!syncParams.getSecondaryMasterIpAddress().equals("0.0.0.0")) {
						if (syncParams.getSecondaryMasterDomain() != null) {
							syncProfile.setSecondaryMasterDomain(objectFactory
									.createEnbSyncProfileParamsSecondaryMasterDomain(syncParams.getSecondaryMasterDomain()));
						}
					}
				}
				if (syncParams.getAnnounceRateInMsgPerSec() != null) {
					syncProfile.setAnnounceRateInMsgPerSec(objectFactory.createEnbSyncProfileParamsAnnounceRateInMsgPerSec(
							syncParams.getAnnounceRateInMsgPerSec().value()));
				}
				if (syncParams.getSyncRateInMsgPerSec() != null) {
					syncProfile.setSyncRateInMsgPerSec(objectFactory
							.createEnbSyncProfileParamsSyncRateInMsgPerSec(syncParams.getSyncRateInMsgPerSec().value()));
				}
				if (syncParams.getDelayRequestResponseRateInMsgPerSec() != null) {
					syncProfile.setDelayRequestResponseRateInMsgPerSec(
							objectFactory.createEnbSyncProfileParamsDelayRequestResponseRateInMsgPerSec(
									syncParams.getDelayRequestResponseRateInMsgPerSec().value()));
				}
				if (syncParams.getLeaseDurationInSec() != null) {
					syncProfile.setLeaseDurationInSec(
							objectFactory.createEnbSyncProfileParamsLeaseDurationInSec(syncParams.getLeaseDurationInSec()));
				}
			}
		}
		
		try {
			ProfileResponse cloneResult = (ProfileResponse) soapHelper_16_0.getLteSoap().syncProfileClone(cloneFromName,
					syncProfile, credentialsLte);

			boolean result = false;
			if (cloneResult == null) {
				report.report("Fail to get Sync profile cloning result", Reporter.WARNING);
				result = false;
			} else if (cloneResult.getErrorCode() == Netspan.NBI_16_0.Lte.ErrorCodes.OK) {
				report.report("Succeeded to clone Sync profile, new profile name: " + syncParams.getProfileName());
				result = true;
			} else {
				report.report("Failed to clone Sync profile, reason: " + cloneResult.getErrorString(),
						Reporter.WARNING);
				result = false;
			}
			getNetspanProfile(node,syncParams.getProfileName(), syncParams);
			return result;
			
		} catch (Exception e) {
			e.printStackTrace();
			report.report("cloneSyncProfile Failed, due to: " + e.getMessage(), Reporter.WARNING);
			return false;
		} finally {
			soapHelper_16_0.endLteSoap();
		}
	}
	
	/**
	 * Author: Moran Goldenberg
	 * Clone sync profile via netspan.
	 *
	 * @param node
	 *            enodeB
	 * @param cloneFromName
	 *            the clone from name
	 * @param advancedParmas
	 *            all the params that was changed
	 * @return true, if successful
	 */
	@Override
	public boolean cloneEnodeBAdvancedProfile(EnodeB node, String cloneFromName, EnodeBAdvancedParameters advancedParmas) {
		try{
			getNodeConfig(node);
			ObjectFactory objectFactory = new ObjectFactory();
			EnbAdvancedProfile enbAdvancedProfile = new EnbAdvancedProfile();
			enbAdvancedProfile.setName(advancedParmas.getProfileName());
			AirSonWs airSonWs = new AirSonWs();
			NlmWs nlmWs = new NlmWs();
			NlSyncWs nlsyncWs = new NlSyncWs();
			airSonWs.setLowPower(objectFactory.createAirSonWsLowPower(advancedParmas.getLowPower()));
			airSonWs.setPowerStep(objectFactory.createAirSonWsPowerStep(advancedParmas.getPowerStep()));
			airSonWs.setPowerLevelTimeInterval(objectFactory.createAirSonWsPowerLevelTimeInterval(advancedParmas.getPowerLevelTimeInterval()));
			airSonWs.setAnrTimer(objectFactory.createAirSonWsAnrTimer(advancedParmas.getAnrTimer()));
			if(advancedParmas.getPciConfusionAllowed() != null) {
				EnabledDisabledStates state = EnabledDisabledStates.fromValue(advancedParmas.getPciConfusionAllowed().value());
				airSonWs.setPciConfusionAllowed(objectFactory.createAirSonWsPciConfusionAllowed(state));
			}
			
			airSonWs.setInitialPciListSize(objectFactory.createAirSonWsInitialPciListSize(advancedParmas.getInitialPciListSize()));
			enbAdvancedProfile.setAirSon(airSonWs);
			
			if (advancedParmas.getStartPCI() != null){
				nlsyncWs.setPciStartIsDefault(objectFactory.createNlSyncWsPciEndIsDefault(false));
				nlsyncWs.setPciStart(objectFactory.createNlSyncWsPciStart(advancedParmas.getStartPCI()));
			}
			if (advancedParmas.getStopPCI() != null){
				nlsyncWs.setPciEndIsDefault(objectFactory.createNlSyncWsPciEndIsDefault(false));
				nlsyncWs.setPciEnd(objectFactory.createNlSyncWsPciEnd(advancedParmas.getStopPCI()));
			}
			if (advancedParmas.getRSRPTreshholdForNLSync() != null){
				nlsyncWs.setRsrpThresholdForNlSyncIsDefault(objectFactory.createNlSyncWsRsrpThresholdForNlSyncIsDefault(false));
				nlsyncWs.setRsrpThresholdForNlSync(objectFactory.createNlSyncWsRsrpThresholdForNlSync(advancedParmas.getRSRPTreshholdForNLSync()));
				nlmWs.setNlSync(nlsyncWs);
				enbAdvancedProfile.setNlm(nlmWs);
			}
			if (advancedParmas.getSonInformationRequestPeriodicly() != null){
				nlsyncWs.setSonInformationRequestPeriodicityIsDefault(objectFactory.createNlSyncWsSonInformationRequestPeriodicityIsDefault(false));
				nlsyncWs.setSonInformationRequestPeriodicity(objectFactory.createNlSyncWsSonInformationRequestPeriodicity(advancedParmas.getSonInformationRequestPeriodicly()));
			}
			
			
			ProfileResponse cloneResult = (ProfileResponse) soapHelper_16_0.getLteSoap().enbAdvancedConfigProfileClone(
					cloneFromName, enbAdvancedProfile, credentialsLte);
			
			boolean result = false;
			if(cloneResult.getErrorCode() == Netspan.NBI_16_0.Lte.ErrorCodes.OK) {
				report.report("Succeeded to clone eNodeB Advanced Configuration Profile, new profile name: " + advancedParmas.getProfileName());
				result = true;
			} else {
				report.report("Failed to clone eNodeB Advanced Configuration Profile : " + cloneResult.getErrorString(), Reporter.WARNING);
				result = false;
			}
			
			getNetspanProfile(node,advancedParmas.getProfileName(), advancedParmas);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			report.report("Failed to clone eNodeB Advanced Configuration Profile, due to: " + e.getMessage(), Reporter.WARNING);
			return false;
		}
		finally{
			soapHelper_16_0.endLteSoap();
		}
	}
	
	private EnbDetailsGet convertPnPToEnb(LtePnpConfigGetResult result){
		EnbDetailsGet toReturn = new EnbDetailsGet();
		toReturn.setHardware(result.getPnpConfig().get(FIRST_ELEMENT).getLteEnbConfig().getHardware());
		toReturn.setAdvancedConfigProfile(result.getPnpConfig().get(FIRST_ELEMENT).getLteEnbConfig().getAdvancedConfigProfile());
		toReturn.setManagementProfile(result.getPnpConfig().get(FIRST_ELEMENT).getLteEnbConfig().getManagementProfile());
		toReturn.setManagementProfile(result.getPnpConfig().get(FIRST_ELEMENT).getLteEnbConfig().getManagementProfile());
		toReturn.setNetworkProfile(result.getPnpConfig().get(FIRST_ELEMENT).getLteEnbConfig().getNetworkProfile());
		toReturn.setSecurityProfile(result.getPnpConfig().get(FIRST_ELEMENT).getLteEnbConfig().getSecurityProfile());
		toReturn.setSonProfile(result.getPnpConfig().get(FIRST_ELEMENT).getLteEnbConfig().getSonProfile());
		toReturn.setSyncProfile(result.getPnpConfig().get(FIRST_ELEMENT).getLteEnbConfig().getSyncProfile());
		toReturn.setSystemDefaultProfile(result.getPnpConfig().get(FIRST_ELEMENT).getLteEnbConfig().getSystemDefaultProfile());
		toReturn.setManagedMode(null);
		toReturn.setMultiCellProfile(result.getPnpConfig().get(FIRST_ELEMENT).getLteEnbConfig().getMultiCellProfile());
		toReturn.setPtpSlaveIpAddress(result.getPnpConfig().get(FIRST_ELEMENT).getLteEnbConfig().getPtpSlaveIpAddress());
		toReturn.getLteCell().addAll(result.getPnpConfig().get(FIRST_ELEMENT).getLteEnbConfig().getLteCell());
				
		return toReturn;
	}
	
	private EnbDetailsGet getNodeConfig(EnodeB enb) {
		// EnbConfigGet
		ArrayList<String> nodeListName = new ArrayList<String>();
		nodeListName.add(enb.getNetspanName());
		
		if(isPnpNode(enb.getNetspanName())){
			LtePnpConfigGetResult result = null;
			try {
				result = (LtePnpConfigGetResult) soapHelper_16_0.getLteSoap().pnpConfigGet(nodeListName, credentialsLte);
			} catch (Exception e) {
				report.report("enbConfigGet via Netspan Failed due to: " + e.getMessage(), Reporter.WARNING);
				e.printStackTrace();
				return null;
			} finally {
				soapHelper_16_0.endLteSoap();
			}
			if (result.getErrorCode() != Netspan.NBI_16_0.Lte.ErrorCodes.OK) {
				GeneralUtils.printToConsole("enbConfigGet via Netspan Failed : " + result.getErrorString());
				return null;
			}
			GeneralUtils.printToConsole("enbConfigGet via Netspan for eNodeB " + enb.getNetspanName() + " succeeded");
			return convertPnPToEnb(result);
		}
		
		LteEnbConfigGetResult result = null;
		try {
			result = (LteEnbConfigGetResult) soapHelper_16_0.getLteSoap().enbConfigGet(nodeListName, credentialsLte);
		} catch (Exception e) {
			report.report("enbConfigGet via Netspan Failed due to: " + e.getMessage(), Reporter.WARNING);
			e.printStackTrace();
			return null;
		} finally {
			soapHelper_16_0.endLteSoap();
		}
		if (result.getErrorCode() != Netspan.NBI_16_0.Lte.ErrorCodes.OK) {
			GeneralUtils.printToConsole("enbConfigGet via Netspan Failed : " + result.getErrorString());
			return null;
		}
		GeneralUtils.printToConsole("enbConfigGet via Netspan for eNodeB " + enb.getNetspanName() + " succeeded");
		return result.getNodeResult().get(FIRST_ELEMENT).getEnbConfig();
	}

	@Override
	public boolean getNetspanProfile(EnodeB node, String cloneFromName,INetspanProfile profileParams) {
		try{
			switch (profileParams.getType()) {
			case Neighbour_Management_Profile:
				return getManagementProfile(node, cloneFromName);
				
			case MultiCell_Profile:
				return getMultiCellProfile(node, cloneFromName);
				
			default:
					return super.getNetspanProfile(node,cloneFromName,profileParams);
			}
		}
		catch(Exception e){
			e.printStackTrace();
			report.report("Error in cloneProfile: " + e.getMessage(), Reporter.WARNING);
			return false;
		}
	}
	
	@Override
	public boolean cloneRadioProfile(EnodeB node, String cloneFromName, RadioParameters radioParams){
		if (cloneFromName != null) {
			GeneralUtils.printToConsole(
					"Trying to create automation radio profile \"%s\" via NBIF , creating an automation profiles for the run."
							+ cloneFromName);
		} else {
			System.out
					.println("Trying to create automation radio profile from the current Profile that in use via NBIF ,"
							+ " creating an automation profiles for the run.");
			cloneFromName = node.getDefaultNetspanProfiles().getRadio();
		}
		EnbRadioProfile radio = createEnbRadioProfile16_0(radioParams);
		if (radio == null) return false;
		try{
			ProfileResponse cloneResult = (ProfileResponse) soapHelper_16_0.getLteSoap().radioProfileClone(cloneFromName, radio, credentialsLte);
			boolean result = false;
			
			if(cloneResult == null){
				report.report("Fail to get Radio profile cloning result", Reporter.WARNING);
				result = false;
			}else if(cloneResult.getErrorCode() == Netspan.NBI_16_0.Lte.ErrorCodes.OK){
				report.report("Succeeded to clone Radio profile, new profile name: " + radioParams.getProfileName());
				result =true;
			} else {
				report.report("Failed to clone Radio profile, reason: " + cloneResult.getErrorString(),Reporter.WARNING);
				result = false;
			}
			getNetspanProfile(node,radioParams.getProfileName(), radioParams);
			return result;
			
		}catch(Exception e){
			GeneralUtils.printToConsole("Could not execute radioProfileClone on " + cloneFromName);
			report.report("Failed to clone Radio profile, due to: " + e.getMessage(), Reporter.WARNING);
			return false;
		}finally{
			soapHelper_16_0.endLteSoap();
		}
	}

	private EnbRadioProfile createEnbRadioProfile16_0(RadioParameters radioParams) {
		ObjectFactory factoryObject = new ObjectFactory();
		EnbRadioProfile radio = new EnbRadioProfile();
		
		
		if (radioParams.getProfileName() == null) {
			return new EnbRadioProfile();
		} else {
			radio.setName(radioParams.getProfileName());
		}

		if (radioParams.getBand() != null) {
			radio.setBand(factoryObject.createEnbRadioProfileParamsBand(radioParams.getBand()));
		}

		if (radioParams.getBandwidth() != null) {
			radio.setBandwidthMhz(factoryObject.createEnbRadioProfileParamsBandwidthMhz(radioParams.getBandwidth()));
		}
		if (radioParams.getDownLinkFrequency() != null) {
			radio.setDownlinkFreqKHz(factoryObject.createEnbRadioProfileParamsUplinkFreqKHz(radioParams.getDownLinkFrequency()));
		}

		if (radioParams.getDuplex() != null) {
			DuplexModeTypes duplexMode = radioParams.getDuplex().contains("1") ? DuplexModeTypes.FDD
					: DuplexModeTypes.TDD;
			radio.setDuplexMode(factoryObject.createEnbRadioProfileParamsDuplexMode(duplexMode));
		}
		if (radioParams.getEarfcn() != null) {
			radio.setEarfcn(factoryObject.createEnbRadioProfileParamsEarfcn(radioParams.getEarfcn()));
		}
		if (radioParams.getFrameConfig() != null) {
			TddFrameConfigurationsSupported fc = radioParams.getFrameConfig().contains("1")
					? TddFrameConfigurationsSupported.DL_40_UL_40_SP_20
					: TddFrameConfigurationsSupported.DL_60_UL_20_SP_20;
			radio.setFrameConfig(factoryObject.createEnbRadioProfileParamsFrameConfig(fc));
		}
		if (radioParams.getTxpower() != null) {
			radio.setTxPower(factoryObject.createEnbRadioProfileParamsTxPower(radioParams.getTxpower()));
		}

		if (radioParams.getUpLinkFrequency() != null) {
			radio.setUplinkFreqKHz(factoryObject.createEnbRadioProfileParamsUplinkFreqKHz(radioParams.getUpLinkFrequency()));
		}
		
		Boolean ecid = radioParams.getECIDMode();
		if(ecid != null){
			EnabledDisabledStates ecidParam;
			ecidParam = ecid ? EnabledDisabledStates.ENABLED : EnabledDisabledStates.DISABLED;
			radio.setEcidMode(factoryObject.createEnbRadioProfileParamsEcidMode(ecidParam));
		}
		
		Integer ecidTimer = radioParams.getECIDProcedureTimer();
		if(ecidTimer != null){
			radio.setEcidTimer(factoryObject.createEnbRadioProfileParamsEcidTimer(ecidTimer));
		}
		
		Boolean otdoa = radioParams.getOTDOAMode();
		if(otdoa != null){
			EnabledDisabledStates otdoaParam;
			otdoaParam = otdoa ? EnabledDisabledStates.ENABLED : EnabledDisabledStates.DISABLED;
			radio.setOtdoaMode(factoryObject.createEnbRadioProfileParamsOtdoaMode(otdoaParam));
		}
		
		if(otdoa){
			String subFrames = radioParams.getSubframes();
			if(subFrames != null){
				String[] framsArr = subFrames.split(",");
				RfSubframe subFramesObj = new RfSubframe();
				for(String frame : framsArr){
					frame = frame.trim();
					switch(frame){
					case "0":
						subFramesObj.setSubFrame0(factoryObject.createRfSubframeSubFrame0(EnabledDisabledStates.ENABLED));
						break;
					case "1":
						subFramesObj.setSubFrame1(factoryObject.createRfSubframeSubFrame1(EnabledDisabledStates.ENABLED));
						break;
					case "2":
						subFramesObj.setSubFrame2(factoryObject.createRfSubframeSubFrame2(EnabledDisabledStates.ENABLED));
						break;
					case "3":
						subFramesObj.setSubFrame3(factoryObject.createRfSubframeSubFrame3(EnabledDisabledStates.ENABLED));
						break;
					case "4":
						subFramesObj.setSubFrame4(factoryObject.createRfSubframeSubFrame4(EnabledDisabledStates.ENABLED));
						break;
					case "5":
						subFramesObj.setSubFrame5(factoryObject.createRfSubframeSubFrame5(EnabledDisabledStates.ENABLED));
						break;
					case "6":
						subFramesObj.setSubFrame6(factoryObject.createRfSubframeSubFrame6(EnabledDisabledStates.ENABLED));
						break;
					case "7":
						subFramesObj.setSubFrame7(factoryObject.createRfSubframeSubFrame7(EnabledDisabledStates.ENABLED));
						break;
					case "8":
						subFramesObj.setSubFrame8(factoryObject.createRfSubframeSubFrame8(EnabledDisabledStates.ENABLED));
						break;
					case "9":
						subFramesObj.setSubFrame9(factoryObject.createRfSubframeSubFrame9(EnabledDisabledStates.ENABLED));
						break;
					default:
						break;	
					}
				}
				
				radio.setRfSubframes(subFramesObj);
			}
				
			if(radioParams.getPRSBandWidth() != null){
				Integer prsBw = radioParams.getPRSBandWidth().getPRS();
				if(prsBw != null){
					radio.setOtdoaPrsBw(factoryObject.createEnbRadioProfileParamsOtdoaPrsBw(prsBw.toString()));
				}
			}
		
			if(radioParams.getPRSPeriodicly() != null){
				Integer prsPeriodicity = radioParams.getPRSPeriodicly().getPRS();
				if(prsPeriodicity != null){
					radio.setOtdaPrsPeriodicity(factoryObject.createEnbRadioProfileParamsOtdaPrsPeriodicity(prsPeriodicity.toString()));
				}
			}
		
			Integer prsOffSet = radioParams.getPRSOffset();
			if(prsOffSet != null){
				radio.setOtdaPrsOffset(factoryObject.createEnbRadioProfileParamsOtdaPrsOffset(prsOffSet));
			}
			
			Integer prsPowerOffSet = radioParams.getPRSPowerOffset();
			if(prsPowerOffSet != null){
				radio.setOtdoaPrsTxOff(factoryObject.createEnbRadioProfileParamsOtdoaPrsTxOff(prsPowerOffSet));
			}
		
			if(radioParams.getPRSMutingPeriodicly() != null){
				Integer prsMutingPeriodicity = radioParams.getPRSMutingPeriodicly().getPRS();
				if(prsMutingPeriodicity != null){
					radio.setOtdoaPrsMutePeriod(factoryObject.createEnbRadioProfileParamsOtdoaPrsMutePeriod(prsMutingPeriodicity.toString()));
				}
			}
			
			String prsMutingPattern = radioParams.getPRSMutingPattern();
			if(prsMutingPattern != null){
				radio.setOtdoaPrsMutePattSeq(prsMutingPattern);
			}
		}
		return radio;
	}
	
	@Override
	public boolean getRawNetspanGetProfileResponse(EnodeB node, String cloneFromName, INetspanProfile profileParams) {
		List<String> name = new ArrayList<>();
		name.add(cloneFromName);
		List<CategoriesLte> categoryList = new ArrayList<>();
		EnbDetailsGet nodeConf = getNodeConfig(node);
		CategoriesLte category = null;
		try{
			category = CategoriesLte.fromValue(nodeConf.getHardware());
		}catch(Exception e){
			report.report("Incompatible HardWare Type in inner Netspan method!",Reporter.WARNING);
			GeneralUtils.printToConsole("Error : "+e.getMessage()+", nodeConfig :"+nodeConf.getHardware());
			return false;
		}
		categoryList.add(category);
		soapHelper_16_0.clearLteRawData();
		switch (profileParams.getType()){
			case System_Default_Profile:
				soapHelper_16_0.getLteSoapRaw().systemDefaultProfileGet(name,categoryList,credentialsLte);
				break;
			case Management_Profile:
				soapHelper_16_0.getLteSoapRaw().managementProfileGet(name,categoryList, credentialsLte);
				break;
			case Mobility_Profile:
				soapHelper_16_0.getLteSoapRaw().mobilityProfileGet(name,categoryList, credentialsLte);
				break;
			case Network_Profile:
				soapHelper_16_0.getLteSoapRaw().networkProfileGet(name,categoryList, credentialsLte);
				break;
			case Radio_Profile:
				soapHelper_16_0.getLteSoapRaw().radioProfileGet(name,categoryList, credentialsLte);
				break;
			case Security_Profile:
				soapHelper_16_0.getLteSoapRaw().securityProfileGet(name,categoryList, credentialsLte);
				break;
			case Son_Profile:
				soapHelper_16_0.getLteSoapRaw().sonProfileGet(name,categoryList, credentialsLte);
				break;
			case Sync_Profile:
				soapHelper_16_0.getLteSoapRaw().syncProfileGet(name,categoryList, credentialsLte);
				break;
			case EnodeB_Advanced_Profile:
				soapHelper_16_0.getLteSoapRaw().enbAdvancedConfigProfileGet(name,categoryList, credentialsLte);
				break;
			case Neighbour_Management_Profile:
				soapHelper_16_0.getLteSoapRaw().neighbourProfileGet(name, categoryList, credentialsLte);
				break;
			case MultiCell_Profile:
				soapHelper_16_0.getLteSoapRaw().multiCellProfileGet(name,categoryList, credentialsLte);
				break;
			default:	
				return false;
		}
		
		try{
			String rawData = soapHelper_16_0.getSOAPRawData();
			printHTMLRawData(rawData);
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		finally{
			soapHelper_16_0.endLteSoap();
		}
		return true;	
	}
	
	@Override
	public boolean setLatitudeAndLongitude(EnodeB node, BigDecimal latitude, BigDecimal longitude) {
		NodeActionResult result;
		try{
			result = soapHelper_16_0.getInventorySoap().nodeLatitudeLongitudeSet(node.getNetspanName(), latitude, longitude, credentialsInventory);
			if(result.getErrorCode() != Netspan.NBI_16_0.Inventory.ErrorCodes.OK){
				report.report("netspan Error : "+ result.getErrorString());
			}
			
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		
		finally{
			soapHelper_16_0.endInventorySoap();
		}
		return true;
	}
	
	@Override
	public NodeManagementModeType getManagedMode(EnodeB enb){
		EnbDetailsGet result = getNodeConfig(enb);
		if(result == null ){
			return null;
		}
		return result.getManagedMode().getValue();
	}

	@Override
	public List<EventInfo> getEventsNodeByDateRange(EnodeB dut, Date startTime, Date endTime) {
		EventResultList events = null;
		List<EventInfo> eventsList = new ArrayList<EventInfo>();
		try {
			List<String> nodeNames = new ArrayList<String>();
			nodeNames.add(dut.getNetspanName());
			GregorianCalendar temp = new GregorianCalendar();
			XMLGregorianCalendar start = null;
			XMLGregorianCalendar end = null;
			if (startTime != null) {
				temp.setTime(startTime);
				start = DatatypeFactory.newInstance().newXMLGregorianCalendar(temp);
			}
			if (endTime != null) {
				temp.setTime(endTime);
				end = DatatypeFactory.newInstance().newXMLGregorianCalendar(temp);
			}
			if(start == null || end == null){
				throw new Exception("Netspan Version 16.00 doesn't support eventListNode where startTime or endTime equals null!");
			}
			events = soapHelper_16_0.getFaultManagementSoap().eventListNode(nodeNames, null, start,
					end, credentialsFaultManagement);
		} catch (Exception e) {
			report.report("getEventsNode via netspan Failed due to: " + e.getMessage(), Reporter.WARNING);
			e.printStackTrace();
			return eventsList;

		} finally {
			soapHelper_16_0.endFaultManagementSoap();
		}
		if (events.getErrorCode() != Netspan.NBI_16_0.FaultManagement.ErrorCodes.OK) {
			report.report("getEventsNode via netspan Return Error: " + events.getErrorCode());
			report.report(events.getErrorString());
			return eventsList;
		}
		for (Event event : events.getEvent()) {
			EventInfo newEvent = new EventInfo();
			newEvent.setEventId(event.getEventId());
			newEvent.setEventInfo(event.getEventInfo());
			newEvent.setEventType(event.getEventType());
			newEvent.setEventTypeId(event.getEventTypeId());
			newEvent.setReceivedTime(event.getReceivedTime());
			newEvent.setSourceId(event.getSourceId());
			newEvent.setSourceIfIndex(event.getSourceIfIndex());
			newEvent.setSourceMacAddress(event.getSourceMacAddress());
			newEvent.setSourceName(event.getSourceName());
			newEvent.setSourceType(event.getSourceType());
			newEvent.setSourceUniqueId(event.getSourceUniqueId());
			eventsList.add(newEvent);
		}
		GeneralUtils.printToConsole("eventListNode via Netspan for eNodeB " + dut.getNetspanName() + " succeeded");
		return eventsList;
	}
	
	@Override
	public int getNumberOfActiveCellsForNode(EnodeB node) {
		int numberOfActiveCells = 0;
		EnbDetailsGet nodeConf = getNodeConfig(node);
		if(nodeConf == null){
			return 0;
		}
		List<LteCellGetWs> cells = nodeConf.getLteCell();
		for(LteCellGetWs cell : cells) {
			if(cell.getIsEnabled().getValue()) {
				numberOfActiveCells++;
			}
		}
		return numberOfActiveCells;
	}
}

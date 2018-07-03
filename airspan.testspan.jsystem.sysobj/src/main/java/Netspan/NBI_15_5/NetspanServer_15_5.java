package Netspan.NBI_15_5;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.fasterxml.jackson.databind.ser.std.StdKeySerializers.Default;

import EnodeB.EnodeB;
import EnodeB.EnodeBUpgradeImage;
import EnodeB.EnodeBUpgradeServer;
import Netspan.CellProfiles;
import Netspan.EnbProfiles;
import Netspan.API.Enums.CategoriesLte;
import Netspan.API.Enums.CellBarringPolicies;
import Netspan.API.Enums.DuplexType;
import Netspan.API.Enums.EnabledDisabledStates;
import Netspan.API.Enums.EnbTypes;
import Netspan.API.Enums.HardwareCategory;
import Netspan.API.Enums.ServerProtocolType;
import Netspan.API.Enums.SonAnrStates;
import Netspan.API.Lte.CarrierAggregationModes;
import Netspan.API.Lte.EnbCellProperties;
import Netspan.API.Lte.LteBackhaul;
import Netspan.API.Lte.LteSonDynIcic;
import Netspan.DataObjects.NeighborData;
import Netspan.NBI_15_2.NetspanServer_15_2;
import Netspan.NBI_15_5.Status.LteNetworkElementGetResult;
import Netspan.NBI_15_5.Status.LteNetworkElementStatusWs;
import Netspan.NBI_15_5.Lte.NodeResult;
import Netspan.NBI_15_5.Lte.AnrFreq;
import Netspan.NBI_15_5.Lte.AnrFreqListContainer;
import Netspan.NBI_15_5.Lte.AuEnbDetailWs;
import Netspan.NBI_15_5.Lte.AuNodeDetailsWs;
import Netspan.NBI_15_5.Lte.AuPnpDetailWs;
import Netspan.NBI_15_5.Lte.AuRelayDetails;
import Netspan.NBI_15_5.Lte.EnbCellAdvancedProfile;
import Netspan.NBI_15_5.Lte.EnbDetailsGet;
import Netspan.NBI_15_5.Lte.EnbManagementProfile;
import Netspan.NBI_15_5.Lte.EnbMobilityProfile;
import Netspan.NBI_15_5.Lte.EnbMultiCellProfile;
import Netspan.NBI_15_5.Lte.EnbNeighbourProfile;
import Netspan.NBI_15_5.Lte.EnbNetworkProfile;
import Netspan.NBI_15_5.Lte.EnbSonProfile;
import Netspan.NBI_15_5.Lte.EnbTypesWs;
import Netspan.NBI_15_5.Lte.EtwsModes;
import Netspan.NBI_15_5.Lte.EtwsWs;
import Netspan.NBI_15_5.Lte.LteCSonEntryWs;
import Netspan.NBI_15_5.Lte.LteCellGetWs;
import Netspan.NBI_15_5.Lte.LteCellSetWs;
import Netspan.NBI_15_5.Lte.LteEnbConfigGetResult;
import Netspan.NBI_15_5.Lte.LteEnbDetailsSetWs;
import Netspan.NBI_15_5.Lte.LteNetworkProfileGetResult;
import Netspan.NBI_15_5.Lte.LtePlmnEntryWs;
import Netspan.NBI_15_5.Lte.LtePnpConfigGetResult;
import Netspan.NBI_15_5.Lte.LteS1EntryWs;
import Netspan.NBI_15_5.Lte.LteSonCSonWs;
import Netspan.NBI_15_5.Lte.LteSystemDefaultProfile;
import Netspan.NBI_15_5.Lte.MaintenanceWindowConfigurationWs;
import Netspan.NBI_15_5.Lte.MobilityConnectedModeInterFreq;
import Netspan.NBI_15_5.Lte.MobilityConnectedModeQosFreq;
import Netspan.NBI_15_5.Lte.MobilityConnectedModeStopGaps;
import Netspan.NBI_15_5.Lte.MobilityConnectedModeTriggerGaps;
import Netspan.NBI_15_5.Lte.MultiCellProfileGetResult;
import Netspan.NBI_15_5.Lte.MultiCellProfileResult;
import Netspan.NBI_15_5.Lte.NeighbourHomeEnbBandConfig;
import Netspan.NBI_15_5.Lte.NeighbourHomeEnbBandListContainer;
import Netspan.NBI_15_5.Lte.NeighbourHomeEnbDefaultConfig;
import Netspan.NBI_15_5.Lte.NeighbourHomeEnbEarfcnConfig;
import Netspan.NBI_15_5.Lte.NeighbourHomeEnbEarfcnListContainer;
import Netspan.NBI_15_5.Lte.NeighbourHomeEnbPciWs;
import Netspan.NBI_15_5.Lte.NeighbourNrtBandConfig;
import Netspan.NBI_15_5.Lte.NeighbourNrtBandListContainer;
import Netspan.NBI_15_5.Lte.NeighbourNrtDefaultConfig;
import Netspan.NBI_15_5.Lte.NeighbourNrtEarfcnConfig;
import Netspan.NBI_15_5.Lte.NeighbourNrtEarfcnListContainer;
import Netspan.NBI_15_5.Lte.NeighbourNrtPciWs;
import Netspan.NBI_15_5.Lte.NodeActionResult;
import Netspan.NBI_15_5.Lte.NodeProperty;
import Netspan.NBI_15_5.Lte.ObjectFactory;
import Netspan.NBI_15_5.Lte.PciConflictHandlingValues;
import Netspan.NBI_15_5.Lte.PciRange;
import Netspan.NBI_15_5.Lte.PciRangeListContainer;
import Netspan.NBI_15_5.Lte.PlmnListContainer;
import Netspan.NBI_15_5.Lte.PnpDetailWs;
import Netspan.NBI_15_5.Lte.ProfileResponse;
import Netspan.NBI_15_5.Lte.QosEarfcn;
import Netspan.NBI_15_5.Lte.QosMobilityConnectedModeFreqEarfcnListContainer;
import Netspan.NBI_15_5.Lte.RsiRange;
import Netspan.NBI_15_5.Lte.RsiRangeListContainer;
import Netspan.NBI_15_5.Lte.S1ListContainer;
import Netspan.NBI_15_5.Lte.SnmpDetailSetWs;
import Netspan.NBI_15_5.Lte.TimeZones;
import Netspan.NBI_15_5.Lte.WsResponse;
import Netspan.NBI_15_5.Server.FileServerProtocolType;
import Netspan.NBI_15_5.Server.FileServerResponse;
import Netspan.NBI_15_5.Server.FileServerWs;
import Netspan.API.Enums.ImageType;
import Netspan.API.Enums.NetworkElementStatus;
import Netspan.NBI_15_5.Status.LteAnrStatusWs;
import Netspan.NBI_15_5.Status.LteBackhaulIfGetResult;
import Netspan.NBI_15_5.Status.LteBackhaulWs;
import Netspan.NBI_15_5.Status.LteIpThroughputGetResult;
import Netspan.NBI_15_5.Status.LteIpThroughputQciWs;
import Netspan.NBI_15_5.Status.LteSonAnrGetResult;
import Netspan.NBI_15_5.Status.LteSonDynamicIcicUnmanagedInterferenceGetResult;
import Netspan.NBI_15_5.Status.NodeGpsGetResult;
import Netspan.NBI_15_5.Status.NodeStatusResultValues;
import Netspan.Profiles.AccessBarringAdvanced;
import Netspan.Profiles.CellAdvancedParameters;
import Netspan.Profiles.CellBarringPolicyParameters;
import Netspan.Profiles.INetspanProfile;
import Netspan.Profiles.ManagementParameters;
import Netspan.Profiles.MobilityParameters;
import Netspan.Profiles.MultiCellParameters;
import Netspan.Profiles.NeighbourManagementParameters;
import Netspan.Profiles.NeighbourManagementParameters.HomeEnbPci;
import Netspan.Profiles.NeighbourManagementParameters.NrtPci;
import Netspan.Profiles.NetworkParameters;
import Netspan.Profiles.NetworkParameters.Plmn;
import Netspan.Profiles.SonParameters;
import Netspan.Profiles.SystemDefaultParameters;
import Utils.CellNetspanProfiles;
import Utils.DefaultNetspanProfiles;
import Utils.FileServer;
import Utils.GeneralUtils;
import Utils.Pair;
import Utils.Triple;
import Utils.GeneralUtils.RebootType;
import jsystem.framework.report.Reporter;

public class NetspanServer_15_5 extends NetspanServer_15_2 implements Netspan_15_5_abilities {
	public SoapHelper soapHelper_15_5;
	private static final String USERNAME = "wsadmin";
	private static final String PASSWORD = "password";
	private static final Netspan.NBI_15_5.Lte.Credentials credentialsLte = new Netspan.NBI_15_5.Lte.Credentials();
	private static final Netspan.NBI_15_5.Inventory.Credentials credentialsInventory = new Netspan.NBI_15_5.Inventory.Credentials();
	private static final Netspan.NBI_15_5.FaultManagement.Credentials credentialsFaultManagement = new Netspan.NBI_15_5.FaultManagement.Credentials();
	private static final Netspan.NBI_15_5.Statistics.Credentials credentialsStatistics = new Netspan.NBI_15_5.Statistics.Credentials();
	private static final Netspan.NBI_15_5.Status.Credentials credentialsStatus = new Netspan.NBI_15_5.Status.Credentials();
	private static final Netspan.NBI_15_5.Server.Credentials credentialsServer = new Netspan.NBI_15_5.Server.Credentials();
	private static final Netspan.NBI_15_5.Backhaul.Credentials credentialsBackhaul = new Netspan.NBI_15_5.Backhaul.Credentials();
	private static final Netspan.NBI_15_5.Software.Credentials credentialsSoftware = new Netspan.NBI_15_5.Software.Credentials();

	@Override
	public void init() throws Exception {
		if (NBI_VERSION == null) {
			NBI_VERSION = "15_5";
		}
		super.init();
		this.soapHelper_15_5 = new SoapHelper(getHostname());
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
	}

	private EnbNetworkProfile getNetworkProfile(String profileName) {
		try {
			LteNetworkProfileGetResult result = null;
			List<String> a = new ArrayList<String>();
			a.add(profileName);
			result = soapHelper_15_5.getLteSoap().networkProfileGet(a, null,
					credentialsLte);
			if (result.getErrorCode() != Netspan.NBI_15_5.Lte.ErrorCodes.OK) {
				report.report("networkProfileGet via Netspan Failed : " + result.getErrorString(), Reporter.WARNING);
			}
			return result.getNetworkProfileResult().get(0).getNetworkProfile();
		} catch (Exception e) {
			e.printStackTrace();
			report.report("networkProfileGet via Netspan Failed due to: " + e.getMessage(), Reporter.WARNING);
			return null;
		} finally {
			soapHelper_15_5.endLteSoap();
		}
	}

	@Override
	public boolean checkIfEsonServerConfigured(EnodeB enb) {
		try {
			EnbNetworkProfile networkProfile = getNetworkProfile(enb.getDefaultNetspanProfiles().getNetwork());
			if (networkProfile.getCSONConfig().getIsCSonConfigured().getValue()) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return false;
	}
	
	@Override
	public EnbCellProperties getEnbCellProperties(EnodeB enb) {
		EnbDetailsGet enbDetails = getNodeConfig(enb);
		for (LteCellGetWs lteCell : enbDetails.getLteCell()) {
			if ((lteCell.getCellNumber() != null)
					&& (Integer.parseInt(lteCell.getCellNumber().getValue()) == enb.getCellContextID())) {
				EnbCellProperties res = new EnbCellProperties();
				res.cellNumber = (lteCell.getCellNumber() != null) ? lteCell.getCellNumber().getValue() : null;
				res.cellIdForEci = (lteCell.getCellIdForEci() != null) ? lteCell.getCellIdForEci().getValue() : null;
				res.cellIdentity28Bit = (lteCell.getCellIdentity28Bit() != null)
						? lteCell.getCellIdentity28Bit().getValue() : null;
				res.physicalLayerCellGroup = (lteCell.getPhysicalLayerCellGroup() != null)
						? lteCell.getPhysicalLayerCellGroup().getValue() : null;
				res.physicalLayerIdentity = (lteCell.getPhysicalLayerIdentity() != null)
						? lteCell.getPhysicalLayerIdentity().getValue() : null;
				res.physicalCellId = (lteCell.getPhysicalCellId() != null) ? lteCell.getPhysicalCellId().getValue()
						: null;
				res.prachRsi = (lteCell.getPrachRsi() != null) ? lteCell.getPrachRsi().getValue() : null;
				res.trackingAreaCode = (lteCell.getTrackingAreaCode() != null)
						? lteCell.getTrackingAreaCode().getValue() : null;
				res.emergencyAreaId = (lteCell.getEmergencyAreaId() != null) ? lteCell.getEmergencyAreaId().getValue()
						: null;
				res.prachFreqOffset = (lteCell.getPrachFreqOffset() != null) ? lteCell.getPrachFreqOffset().getValue()
						: null;
				res.cellAdvancedConfigurationProfile = lteCell.getCellAdvancedConfigurationProfile();
				res.radioProfile = lteCell.getRadioProfile();
				res.mobilityProfile = lteCell.getMobilityProfile();
				res.embmsProfile = lteCell.getEmbmsProfile();
				res.trafficManagementProfile = lteCell.getTrafficManagementProfile();
				res.callTraceProfile = lteCell.getCallTraceProfile();
				res.cellBarringPolicy = lteCell.getCellBarringPolicy().getValue();
				res.closedSubscriberGroupMode = lteCell.getCsgMode().getValue();
				res.isEnabled = (lteCell.getIsEnabled() != null) ? lteCell.getIsEnabled().getValue() : null;
				return res;
			}
		}
		return null;
	}
	
	@Override
	public boolean setEnbCellProperties(EnodeB dut, EnbCellProperties cellProperties) {
		LteEnbDetailsSetWs enbConfigSet = new LteEnbDetailsSetWs();
		ObjectFactory objectFactory = new ObjectFactory();
		LteCellSetWs lteCellSet = new LteCellSetWs();
		try {
			lteCellSet
					.setCellNumber(objectFactory.createLteCellSetWsCellNumber(String.valueOf(dut.getCellContextID())));
			lteCellSet.setCellIdForEci(objectFactory.createLteCellSetWsCellIdForEci(cellProperties.cellIdForEci));
			
			if (cellProperties.physicalCellId != null) {
				lteCellSet.setPhysicalLayerCellGroup(
						objectFactory.createLteCellSetWsPhysicalLayerCellGroup(cellProperties.physicalCellId / 3));
				lteCellSet.setPhysicalLayerIdentity(
						objectFactory.createLteCellSetWsPhysicalLayerIdentity(cellProperties.physicalCellId % 3));
			}
			else {
				lteCellSet.setPhysicalLayerCellGroup(
						objectFactory.createLteCellSetWsPhysicalLayerCellGroup(cellProperties.physicalLayerCellGroup));
				lteCellSet.setPhysicalLayerIdentity(
						objectFactory.createLteCellSetWsPhysicalLayerIdentity(cellProperties.physicalLayerIdentity));
			}
			
			lteCellSet.setPrachRsi(objectFactory.createLteCellSetWsPrachRsi(cellProperties.prachRsi));
			lteCellSet.setTrackingAreaCode(
					objectFactory.createLteCellSetWsTrackingAreaCode(cellProperties.trackingAreaCode));
			lteCellSet.setEmergencyAreaId(
					objectFactory.createLteCellSetWsEmergencyAreaId(cellProperties.emergencyAreaId));
			lteCellSet.setPrachFreqOffset(
					objectFactory.createLteCellSetWsPrachFreqOffset(cellProperties.prachFreqOffset));
			lteCellSet.setCellBarringPolicy(
					objectFactory.createLteCellSetWsCellBarringPolicy(cellProperties.cellBarringPolicy));
			lteCellSet.setCsgMode(objectFactory.createLteCellSetWsCsgMode(cellProperties.closedSubscriberGroupMode));

			enbConfigSet.getLteCell().add(lteCellSet);

			Netspan.NBI_15_5.Lte.NodeActionResult result = soapHelper_15_5
					.getLteSoap().enbConfigSet(dut.getNetspanName(), null, enbConfigSet, null, credentialsLte);

			if (result.getErrorCode() != Netspan.NBI_15_5.Lte.ErrorCodes.OK) {
				report.report("setEnbCellProperties via Netspan Failed : " + result.getErrorString(), Reporter.WARNING);
				return false;
			} else
				return true;

		} catch (Exception e) {
			e.printStackTrace();
			report.report("setEnbCellProperties via Netspan Failed due to: " + e.getMessage(), Reporter.WARNING);
			return false;
		} finally {
			soapHelper_15_5.endLteSoap();
		}
	}

	@Override
	public boolean setEnbAccessClassBarring(EnodeB dut, CellBarringPolicyParameters  cellBarringParams) {
		LteEnbDetailsSetWs enbConfigSet = new LteEnbDetailsSetWs();
		ObjectFactory objectFactory = new ObjectFactory();
		LteCellSetWs lteCellSet = new LteCellSetWs();
		try {
			lteCellSet.setCellNumber(objectFactory.createLteCellSetWsCellNumber(String.valueOf(dut.getCellContextID())));
			if (cellBarringParams.cellBarringPolicy != null){
				lteCellSet.setCellBarringPolicy(objectFactory.createLteCellSetWsCellBarringPolicy(cellBarringParams.cellBarringPolicy));
			}
			switch(cellBarringParams.cellBarringPolicy){
			case AC_BARRING:{
				if (cellBarringParams.IsAccessClassBarred != null){
					lteCellSet.setIsAccessClassBarred(objectFactory.createLteCellSetWsIsAccessClassBarred(cellBarringParams.IsAccessClassBarred));
				}
				if (cellBarringParams.IsAccessClassBarred){
					if (cellBarringParams.IsEmergencyAccessBarred != null)
						lteCellSet.setIsEmergencyAccessBarred(objectFactory.createLteCellSetWsIsEmergencyAccessBarred(cellBarringParams.IsEmergencyAccessBarred));
					if (cellBarringParams.IsSignalingAccessBarred != null){
						lteCellSet.setIsSignalingAccessBarred(objectFactory.createLteCellSetWsIsSignalingAccessBarred(cellBarringParams.IsSignalingAccessBarred));
						if(cellBarringParams.IsSignalingAccessBarred && cellBarringParams.signalingAccessBarring != null){
							if(cellBarringParams.signalingAccessBarring.getBarringFactor().value() != null)
								lteCellSet.setSignalingAccessBarringFactor(objectFactory.createLteCellSetWsSignalingAccessBarringFactor(cellBarringParams.signalingAccessBarring.getBarringFactor().value()));
							if(cellBarringParams.signalingAccessBarring.getBarringTime().value() != null)
								lteCellSet.setSignalingAccessBarringTimeInSec(objectFactory.createLteCellSetWsSignalingAccessBarringTimeInSec(cellBarringParams.signalingAccessBarring.getBarringTime().value()));
							if (cellBarringParams.signalingAccessBarring.isBarringClass_11() != null)
								lteCellSet.setIsSignalingAccessClass11Barred(objectFactory.createLteCellGetWsIsSignalingAccessClass11Barred(cellBarringParams.signalingAccessBarring.isBarringClass_11()));
							if (cellBarringParams.signalingAccessBarring.isBarringClass_12() != null)
								lteCellSet.setIsSignalingAccessClass12Barred(objectFactory.createLteCellGetWsIsSignalingAccessClass12Barred(cellBarringParams.signalingAccessBarring.isBarringClass_12()));
							if (cellBarringParams.signalingAccessBarring.isBarringClass_13() !=null)
								lteCellSet.setIsSignalingAccessClass13Barred(objectFactory.createLteCellGetWsIsSignalingAccessClass13Barred(cellBarringParams.signalingAccessBarring.isBarringClass_13()));
							if (cellBarringParams.signalingAccessBarring.isBarringClass_14() !=null)
								lteCellSet.setIsSignalingAccessClass14Barred(objectFactory.createLteCellGetWsIsSignalingAccessClass14Barred(cellBarringParams.signalingAccessBarring.isBarringClass_14()));
							if (cellBarringParams.signalingAccessBarring.isBarringClass_15() !=null)
								lteCellSet.setIsSignalingAccessClass15Barred(objectFactory.createLteCellGetWsIsSignalingAccessClass15Barred(cellBarringParams.signalingAccessBarring.isBarringClass_15()));
						}	
					}
					if (cellBarringParams.IsDataAccessBarred != null){
						if(cellBarringParams.IsDataAccessBarred && cellBarringParams.dataAccessBarring != null){
							if (cellBarringParams.dataAccessBarring.getBarringFactor().value() != null)
								lteCellSet.setDataAccessBarringFactor(objectFactory.createLteCellGetWsDataAccessBarringFactor(cellBarringParams.dataAccessBarring.getBarringFactor().value()));
							if (cellBarringParams.signalingAccessBarring.getBarringTime().value() != null)
								lteCellSet.setSignalingAccessBarringTimeInSec(objectFactory.createLteCellSetWsSignalingAccessBarringTimeInSec(cellBarringParams.signalingAccessBarring.getBarringTime().value()));
							if (cellBarringParams.signalingAccessBarring.isBarringClass_11() != null)
								lteCellSet.setIsSignalingAccessClass11Barred(objectFactory.createLteCellGetWsIsSignalingAccessClass11Barred(cellBarringParams.signalingAccessBarring.isBarringClass_11()));
							if (cellBarringParams.signalingAccessBarring.isBarringClass_12() != null)
								lteCellSet.setIsSignalingAccessClass12Barred(objectFactory.createLteCellGetWsIsSignalingAccessClass12Barred(cellBarringParams.signalingAccessBarring.isBarringClass_12()));
							if (cellBarringParams.signalingAccessBarring.isBarringClass_13() != null)
								lteCellSet.setIsSignalingAccessClass13Barred(objectFactory.createLteCellGetWsIsSignalingAccessClass13Barred(cellBarringParams.signalingAccessBarring.isBarringClass_13()));
							if (cellBarringParams.signalingAccessBarring.isBarringClass_14() != null)
								lteCellSet.setIsSignalingAccessClass14Barred(objectFactory.createLteCellGetWsIsSignalingAccessClass14Barred(cellBarringParams.signalingAccessBarring.isBarringClass_14()));
							if (cellBarringParams.signalingAccessBarring.isBarringClass_15() != null)
							lteCellSet.setIsSignalingAccessClass15Barred(objectFactory.createLteCellGetWsIsSignalingAccessClass15Barred(cellBarringParams.signalingAccessBarring.isBarringClass_15()));
						}
					}
					
				}
			}
				break;
			default:
				break;
		}
			enbConfigSet.getLteCell().add(lteCellSet);
			Netspan.NBI_15_5.Lte.NodeActionResult result = soapHelper_15_5.getLteSoap().enbConfigSet(dut.getNetspanName(), null, enbConfigSet, null, credentialsLte);
			if (result.getErrorCode() != Netspan.NBI_15_5.Lte.ErrorCodes.OK) {
				report.report("setEnbCellProperties via Netspan Failed : " + result.getErrorString(), Reporter.WARNING);
				return false;
			} else
				return true;
			
		} catch (Exception e) {
			e.printStackTrace();
			report.report("setEnbCellProperties via Netspan Failed due to: " + e.getMessage(), Reporter.WARNING);
			return false;
		} finally {
			soapHelper_15_5.endLteSoap();
		}
	}
	
	@Override
	public boolean setEnbType(String nodeName, EnbTypes type) {
		LteEnbDetailsSetWs enbConfigSet = new LteEnbDetailsSetWs();
		ObjectFactory factoryDetails = new ObjectFactory();
		enbConfigSet.setENodeBType(factoryDetails.createEnbDetailsGetPnpENodeBType(EnbTypesWs.fromValue(type.value())));
		try {
			NodeActionResult result = soapHelper_15_5.getLteSoap().enbConfigSet(nodeName, null,
					enbConfigSet, null, credentialsLte);
			if (result.getErrorCode() == Netspan.NBI_15_5.Lte.ErrorCodes.OK) {
				report.report(String.format("%s - Succeeded to set eNodeB Type", nodeName));
				return true;
			} else {
				report.report("enbConfigSet via Netspan Failed : " + result.getErrorString(), Reporter.WARNING);
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			report.report(nodeName + ": enbConfigSet via Netspan Failed due to: ", Reporter.WARNING);
			return false;
		} finally {
			soapHelper_15_5.endLteSoap();
		}
	}

	@Override
	public boolean performReProvision(String nodeName) {
		List<String> nodeList = new ArrayList<String>();
		boolean reProPass = false;
		nodeList.add(nodeName);
		try {
			Netspan.NBI_15_5.Inventory.NodeActionResult result = soapHelper_15_5
					.getInventorySoap().nodeReprovision(nodeList, null, credentialsInventory);
			if (result.getErrorCode() == Netspan.NBI_15_5.Inventory.ErrorCodes.OK) {
				report.report(String.format("%s - ReProvision started.", nodeName));
				for (int i = 0; i < 10; i++) {
					Netspan.NBI_15_5.Inventory.NodeProvisioningGetResult result1 = soapHelper_15_5
							.getInventorySoap().nodeProvisioningStatusGet(nodeList, null, credentialsInventory);
					if (result1.getErrorCode() == Netspan.NBI_15_5.Inventory.ErrorCodes.OK) {
						reProPass = result1.getNodeResult().get(0).getNodeProvisioningDetail().getConfigQueued()
								.getValue() == 0;
						reProPass &= result1.getNodeResult().get(0).getNodeProvisioningDetail().getConfigPending()
								.getValue() == 0;
						reProPass &= result1.getNodeResult().get(0).getNodeProvisioningDetail().getConfigFailed()
								.getValue() == 0;
					} else {
						report.report("nodeProvisioningStatusGet via Netspan Failed : " + result.getErrorString(),
								Reporter.WARNING);
					}
					if (reProPass) {
						report.report(String.format("%s - ReProvision succeeded.", nodeName));
						return true;
					} else {
						GeneralUtils.unSafeSleep(10000);
					}
				}
			} else {
				report.report("nodeReprovision via Netspan Failed : " + result.getErrorString(), Reporter.WARNING);
			}
		} catch (Exception e) {
			e.printStackTrace();
			report.report("performReProvision via Netspan Failed due to: ", Reporter.WARNING);
		} finally {
			soapHelper_15_5.endInventorySoap();
		}
		return false;
	}

	@Override
	public boolean updateNeighborManagementProfile(EnodeB node, String profileName,
			NeighbourManagementParameters neighbourManagementParams) {
		try {
			EnbNeighbourProfile neighbourProfile = parseNeighbourManagementParameters(neighbourManagementParams);
			ProfileResponse cloneResult = soapHelper_15_5.getLteSoap()
					.neighbourProfileUpdate(profileName, neighbourProfile, credentialsLte);

			if (cloneResult == null) {
				report.report("Fail to get Neighbour Manangement profile update result.", Reporter.WARNING);
				return false;
			} else if (cloneResult.getErrorCode() == Netspan.NBI_15_5.Lte.ErrorCodes.OK) {
				report.report(String.format("Succeeded to update Neighbour Manangement profile \"%s\" via Netspan.",
						profileName));
				return true;
			} else {
				report.report("Failed to update Neighbour Manangement profile, reason: " + cloneResult.getErrorString(),
						Reporter.WARNING);
				return false;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			report.report("neighbourProfileUpdate via Netspan Failed due to: " + e.getMessage(), Reporter.WARNING);
		} finally {
			soapHelper_15_5.endLteSoap();
		}
		return false;
	}

	@Override
	public boolean cloneNeighborManagementProfile(EnodeB node, String profileName,
			NeighbourManagementParameters neighbourManagementParams) {
		try {
			EnbNeighbourProfile neighbourProfile = parseNeighbourManagementParameters(neighbourManagementParams);
			ProfileResponse cloneResult = soapHelper_15_5.getLteSoap()
					.neighbourProfileClone(profileName, neighbourProfile, credentialsLte);

			if (cloneResult == null) {
				report.report("Fail to get Neighbour Manangement profile clone result.", Reporter.WARNING);
				return false;
			} else if (cloneResult.getErrorCode() == Netspan.NBI_15_5.Lte.ErrorCodes.OK) {
				report.report("Succeeded to clone Neighbour Manangement profile, new profile name: "
						+ neighbourManagementParams.profileName);
				return true;
			} else {
				report.report("Failed to clone Neighbour Manangement profile, reason: " + cloneResult.getErrorString(),
						Reporter.WARNING);
				return false;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			report.report("mobilityProfileClone via Netspan Failed due to: ", Reporter.WARNING);
		} finally {
			soapHelper_15_5.endLteSoap();
		}
		return false;
	}

	private EnbNeighbourProfile parseNeighbourManagementParameters(
			NeighbourManagementParameters neighbourManagementParams) {
		EnbNeighbourProfile neighbourProfile = new EnbNeighbourProfile();
		neighbourProfile.setName(neighbourManagementParams.profileName);
		ObjectFactory factoryDetails = new ObjectFactory();
		if (neighbourManagementParams.homeEnbBandList != null) {
			NeighbourHomeEnbBandListContainer container = new NeighbourHomeEnbBandListContainer();
			if (neighbourManagementParams.homeEnbBandList.HomeEnbBandList.size() != 0) {
				NeighbourHomeEnbBandConfig config = new NeighbourHomeEnbBandConfig();
				config.setBand(factoryDetails.createNeighbourHomeEnbBandConfigBand(
						neighbourManagementParams.homeEnbBandList.HomeEnbBandList.get(FIRST_ELEMENT).band));
				for (HomeEnbPci pci : neighbourManagementParams.homeEnbBandList.HomeEnbBandList
						.get(FIRST_ELEMENT).pciList) {
					NeighbourHomeEnbPciWs homePci = new NeighbourHomeEnbPciWs();
					homePci.setPciEnd(factoryDetails.createPciRangePciEnd(pci.pciEnd));
					homePci.setPciStart(factoryDetails.createPciRangePciStart(pci.pciStart));
					config.getPci().add(homePci);
				}
				container.getHomeEnbBand().add(config);
			}
			neighbourProfile.setHomeEnbBandList(container);
		}
		if (neighbourManagementParams.homeEnbEarfcnList != null) {
			NeighbourHomeEnbEarfcnListContainer container = new NeighbourHomeEnbEarfcnListContainer();
			if (neighbourManagementParams.homeEnbEarfcnList.HomeEnbEarfcnList.size() != 0) {
				NeighbourHomeEnbEarfcnConfig config = new NeighbourHomeEnbEarfcnConfig();
				config.setEarfcn(factoryDetails.createNeighbourHomeEnbEarfcnConfigEarfcn(
						neighbourManagementParams.homeEnbEarfcnList.HomeEnbEarfcnList.get(FIRST_ELEMENT).earfcn));
				for (HomeEnbPci pci : neighbourManagementParams.homeEnbEarfcnList.HomeEnbEarfcnList
						.get(FIRST_ELEMENT).pciList) {
					NeighbourHomeEnbPciWs homePci = new NeighbourHomeEnbPciWs();
					homePci.setPciEnd(factoryDetails.createPciRangePciEnd(pci.pciEnd));
					homePci.setPciStart(factoryDetails.createPciRangePciStart(pci.pciStart));
					config.getPci().add(homePci);
				}
				container.getHomeEnbEarfcn().add(config);
			}
			neighbourProfile.setHomeEnbEarfcnList(container);
		}
		if (neighbourManagementParams.homeEnbDefaultConfig != null) {
			NeighbourHomeEnbDefaultConfig config = new NeighbourHomeEnbDefaultConfig();
			for (HomeEnbPci pci : neighbourManagementParams.homeEnbDefaultConfig.HomeEnbDefaultConfig) {
				NeighbourHomeEnbPciWs homePci = new NeighbourHomeEnbPciWs();
				homePci.setPciEnd(factoryDetails.createPciRangePciEnd(pci.pciEnd));
				homePci.setPciStart(factoryDetails.createPciRangePciStart(pci.pciStart));
				config.getPci().add(homePci);
			}
			neighbourProfile.setHomeEnbDefaultConfig(config);
		}
		if (neighbourManagementParams.nrtBandList != null) {
			NeighbourNrtBandListContainer container = new NeighbourNrtBandListContainer();
			if (neighbourManagementParams.nrtBandList.NrtBandList.size() != 0) {
				NeighbourNrtBandConfig config = new NeighbourNrtBandConfig();
				config.setBand(factoryDetails.createNeighbourNrtBandConfigBand(
						neighbourManagementParams.nrtBandList.NrtBandList.get(FIRST_ELEMENT).band));
				for (NrtPci pci : neighbourManagementParams.nrtBandList.NrtBandList.get(FIRST_ELEMENT).pciList) {
					NeighbourNrtPciWs nrtPci = new NeighbourNrtPciWs();
					nrtPci.setPciEnd(factoryDetails.createPciRangePciEnd(pci.pciEnd));
					nrtPci.setPciStart(factoryDetails.createPciRangePciStart(pci.pciStart));
					nrtPci.setHoType(factoryDetails.createNeighbourNrtPciWsHoType(pci.hoType));
					nrtPci.setAllowX2(factoryDetails.createNeighbourNrtPciWsAllowX2(pci.allowX2));
					config.getPci().add(nrtPci);
				}
				container.getNrtBand().add(config);
			}
			neighbourProfile.setNrtBandList(container);
		}
		if (neighbourManagementParams.nrtEarfcnList != null) {
			NeighbourNrtEarfcnListContainer container = new NeighbourNrtEarfcnListContainer();
			if (neighbourManagementParams.nrtEarfcnList.NrtEarfcnList.size() != 0) {
				NeighbourNrtEarfcnConfig config = new NeighbourNrtEarfcnConfig();
				config.setEarfcn(factoryDetails.createNeighbourNrtEarfcnConfigEarfcn(
						neighbourManagementParams.nrtEarfcnList.NrtEarfcnList.get(FIRST_ELEMENT).earfcn));
				for (NrtPci pci : neighbourManagementParams.nrtEarfcnList.NrtEarfcnList.get(FIRST_ELEMENT).pciList) {
					NeighbourNrtPciWs nrtPci = new NeighbourNrtPciWs();
					nrtPci.setPciEnd(factoryDetails.createPciRangePciEnd(pci.pciEnd));
					nrtPci.setPciStart(factoryDetails.createPciRangePciStart(pci.pciStart));
					nrtPci.setHoType(factoryDetails.createNeighbourNrtPciWsHoType(pci.hoType));
					nrtPci.setAllowX2(factoryDetails.createNeighbourNrtPciWsAllowX2(pci.allowX2));
					config.getPci().add(nrtPci);
				}
				container.getNrtEarfcn().add(config);
			}
			neighbourProfile.setNrtEarfcnList(container);
		}
		if (neighbourManagementParams.nrtDefaultConfig != null) {
			NeighbourNrtDefaultConfig config = new NeighbourNrtDefaultConfig();
			for (NrtPci pci : neighbourManagementParams.nrtDefaultConfig.NrtDefaultConfig) {
				NeighbourNrtPciWs nrtPci = new NeighbourNrtPciWs();
				nrtPci.setPciEnd(factoryDetails.createPciRangePciEnd(pci.pciEnd));
				nrtPci.setPciStart(factoryDetails.createPciRangePciStart(pci.pciStart));
				nrtPci.setHoType(factoryDetails.createNeighbourNrtPciWsHoType(pci.hoType));
				nrtPci.setAllowX2(factoryDetails.createNeighbourNrtPciWsAllowX2(pci.allowX2));
				config.getPci().add(nrtPci);
			}
			neighbourProfile.setNrtDefaultConfig(config);
		}
		return neighbourProfile;
	}

	@Override
	public boolean deleteEnbProfile(String profileName, EnbProfiles profileType) {
		ProfileResponse response = null;
		ArrayList<String> nameList = new ArrayList<>();
		nameList.add(profileName);
		if (profileType == EnbProfiles.Neighbour_Management_Profile) {
			response = soapHelper_15_5.getLteSoap().neighbourProfileDelete(nameList, credentialsLte);
			if (response.getErrorCode() != Netspan.NBI_15_5.Lte.ErrorCodes.OK) {
				report.report("deleteEnbProfile via Netspan Failed : " + response.getErrorString(), Reporter.WARNING);
				return false;
			} else
				return true;
		} else
			return super.deleteEnbProfile(profileName, profileType);
	}

	public boolean cloneProfile(EnodeB node, String cloneFromName, INetspanProfile profileParams) {
		try {
			if (profileParams.getType() == EnbProfiles.Neighbour_Management_Profile)
				return cloneNeighborManagementProfile(node, cloneFromName,
						(NeighbourManagementParameters) profileParams);
			else
				return super.cloneProfile(node, cloneFromName, profileParams);
		} catch (Exception e) {
			e.printStackTrace();
			report.report("Error in cloneProfile: " + e.getMessage(), Reporter.WARNING);
			return false;
		}
	}

	public String getCurrentProfileName(EnodeB node, EnbProfiles profileType) {
		try {
			if (profileType == EnbProfiles.Neighbour_Management_Profile)
				return getCurrentNeighborManagmentProfileName(node);
			else
				return super.getCurrentProfileName(node, profileType);
		} catch (Exception e) {
			e.printStackTrace();
			report.report("Error in getCurrentProfileName: " + e.getMessage(), Reporter.WARNING);
			return "";
		}
	}

	public String getCurrentNeighborManagmentProfileName(EnodeB enb) {
		EnbDetailsGet nodeConfig = getNodeConfig(enb);
		try {
			return nodeConfig.getNeighbourProfile();
		} catch (Exception e) {
			e.printStackTrace();
			report.report("Error in getCurrentNeighborManagmentProfileName: " + e.getMessage(), Reporter.WARNING);
			return "";
		}
	}

	/**
	 * setProfileWith out cell number - one Cell only
	 */
	@Override
	public boolean setProfile(EnodeB node, EnbProfiles profileType, String profileName) {
		return setProfile(node, 1, profileType, profileName);
	}

	/**
	 * setProfile with a cell number
	 */
	@Override
	public boolean setProfile(EnodeB node, int cellid, EnbProfiles profileType, String profileName) {
		if (profileType == EnbProfiles.Neighbour_Management_Profile) {
			ObjectFactory objectFactory = new ObjectFactory();
			LteEnbDetailsSetWs enbConfigSet = new LteEnbDetailsSetWs();
			LteCellSetWs lteCellSet = new LteCellSetWs();
			lteCellSet.setCellNumber(objectFactory.createLteCellGetWsCellNumber(String.valueOf(cellid)));
			enbConfigSet.setNeighbourProfile(profileName);
			return enbSetContent(node, null, enbConfigSet, null);
		} else if (profileType == EnbProfiles.Relay_Profile) {
			AuRelayDetails relayDetails = new AuRelayDetails();
			relayDetails.setRelayProfile(profileName);
			return setRelayEnbConfig(node, null, relayDetails, null, null, null);
		} else
			return super.setProfile(node, cellid, profileType, profileName);
	}

	private boolean setRelayEnbConfig(EnodeB node, AuNodeDetailsWs nodeDetail,
			AuRelayDetails relayDetail, AuEnbDetailWs enbDetail, SnmpDetailSetWs relaySnmpDetail,
			SnmpDetailSetWs enbSnmpDetail){
		try{
			Netspan.NBI_15_5.Lte.NodeActionResult result = soapHelper_15_5
			.getLteSoap()
			.relayEnbConfigSet(node.getNetspanName(), null, relayDetail, null, null, null, credentialsLte);
			if(result.getErrorCode() != Netspan.NBI_15_5.Lte.ErrorCodes.OK){
				String error = "";
				try{
					error = result.getNode().get(0).getNodeResultString();
				}catch(Exception e){
					error = result.getErrorString();					
				}
				report.report("relayEnbConfigSet via Netspan Failed : " + error, Reporter.WARNING);
				return false;
			}else{
				report.report("Succeeded to set content to " + node.getNetspanName());
				return true;
			}
		}catch(Exception e){
			e.printStackTrace();
			report.report("Failed to set content for " + node.getNetspanName()
					+ ", due to: " + e.getMessage(), Reporter.WARNING);
			return false;
		}finally{
			soapHelper_15_5.endLteSoap();
		}
	}
	
	@Override
	public boolean cloneMultiCellProfile(EnodeB node, String cloneFromName, MultiCellParameters multiCellParams) {
		try {
			ObjectFactory factoryObject = new ObjectFactory();
			EnbMultiCellProfile multiCellProfile = new EnbMultiCellProfile();

			multiCellProfile.setName(multiCellParams.getProfileName());

			CarrierAggregationModes caMode;
			if (multiCellParams.getCarrierAggMode()) {
				caMode = CarrierAggregationModes.CONTIGUOUS;
			} else {
				caMode = CarrierAggregationModes.DISABLE;
			}
			multiCellProfile.setCarrierAggMode(factoryObject.createEnbMultiCellProfileParamsCarrierAggMode(caMode));

			if (multiCellParams.getIntraEnbLoadBalanceMode() != null) {
				multiCellProfile.setIntraEnbLoadBalancingMode(
						factoryObject.createEnbMultiCellProfileParamsIntraEnbLoadBalancingMode(
								multiCellParams.getIntraEnbLoadBalanceMode()));
			}

			if (multiCellParams.getIntraEnbLoadBalanceMode() == EnabledDisabledStates.ENABLED) {
				multiCellProfile
						.setCompositeLoadDiffMax(factoryObject.createEnbMultiCellProfileParamsCompositeLoadDiffMax(
								multiCellParams.getCompositeLoadDiffMax()));
				multiCellProfile
						.setCompositeLoadDiffMin(factoryObject.createEnbMultiCellProfileParamsCompositeLoadDiffMin(
								multiCellParams.getCompositeLoadDiffMin()));
				multiCellProfile.setCalculationInterval(factoryObject
						.createEnbMultiCellProfileParamsCalculationInterval(multiCellParams.getCalculationInterval()));
			}

			ProfileResponse cloneResult = soapHelper_15_5.getLteSoap()
					.multiCellProfileClone(cloneFromName, multiCellProfile, credentialsLte);
			if (cloneResult.getErrorCode() == Netspan.NBI_15_5.Lte.ErrorCodes.OK) {
				report.report(
						"Succeeded to clone MultiCell profile, new profile name: " + multiCellParams.getProfileName());
				return true;
			} else {
				report.report("Failed to clone MultiCell profile, reason: " + cloneResult.getErrorString(),
						Reporter.WARNING);
				return false;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	private boolean enbSetContent(EnodeB node, NodeProperty nodeProperty, LteEnbDetailsSetWs enbConfigSet,
			SnmpDetailSetWs snmpDetailSetWs) {
		try {
			Netspan.NBI_15_5.Lte.NodeActionResult result = soapHelper_15_5
					.getLteSoap()
					.enbConfigSet(node.getNetspanName(), nodeProperty, enbConfigSet, snmpDetailSetWs, credentialsLte);

			if (result.getErrorCode() != Netspan.NBI_15_5.Lte.ErrorCodes.OK) {
				report.report("enbConfigSet via Netspan Failed : " + result.getErrorString(), Reporter.WARNING);
				return false;
			} else {
				report.report("Succeeded to set content to " + node.getNetspanName());
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			report.report("Failed to set " + enbConfigSet.getName() + " profile for " + node.getNetspanName()
					+ ", due to: " + e.getMessage(), Reporter.WARNING);
			return false;
		} finally {
			soapHelper_15_5.endLteSoap();
		}
	}

	@Override
	public String getNeighborManagmentProfile(String nodeName) {
		ArrayList<String> nodeListName = new ArrayList<String>();
		nodeListName.add(nodeName);
		LteEnbConfigGetResult result = null;
		try {
			result = soapHelper_15_5.getLteSoap().enbConfigGet(nodeListName, credentialsLte);
		} catch (Exception e) {
			report.report("enbConfigGet via Netspan Failed due to: " + e.getMessage(), Reporter.WARNING);
			e.printStackTrace();
			return null;
		} finally {
			soapHelper_15_5.endLteSoap();
		}
		if (result.getErrorCode() != Netspan.NBI_15_5.Lte.ErrorCodes.OK) {
			GeneralUtils.printToConsole("enbConfigGet via Netspan Failed : " + result.getErrorString());
			return null;
		}
		return result.getNodeResult().get(FIRST_ELEMENT).getEnbConfig().getNeighbourProfile();
	}

	@Override
	public boolean isProfileExists(String profileName, EnbProfiles profileType) {
		try {
			ArrayList<String> nameList = new ArrayList<>();
			WsResponse response = null;
			nameList.add(profileName);
			if (profileType == EnbProfiles.Neighbour_Management_Profile) {
				response = soapHelper_15_5.getLteSoap().neighbourProfileGet(nameList, null,
						credentialsLte);
				return response.getErrorCode() == Netspan.NBI_15_5.Lte.ErrorCodes.OK;
			} else
				return super.isProfileExists(profileName, profileType);
		} catch (Exception e) {
			e.printStackTrace();
			report.report(profileType.value() + "ProfileGet Failed, due to: " + e.getMessage(), Reporter.WARNING);
			return false;
		} finally {
			soapHelper_15_5.endLteSoap();
		}
	}
	
	private boolean isProfileExists(String profileName, CellProfiles profileType) {
		ArrayList<String> nameList = new ArrayList<String>();
		nameList.add(profileName);
		Netspan.NBI_15_5.Lte.WsResponse response = null;
		try {
			switch (profileType) {
			case Cell_Advanced_Profile:
				response = soapHelper_15_5.getLteSoap()
						.cellAdvancedConfigProfileGet(nameList, null, credentialsLte);
				break;
			case Radio_Profile:
				response = soapHelper_15_5.getLteSoap()
						.radioProfileGet(nameList, null, credentialsLte);
				break;
			case Mobility_Profile:
				response = soapHelper_15_5.getLteSoap()
						.mobilityProfileGet(nameList, null, credentialsLte);
				break;
			case Embms_Profile:
				response = soapHelper_15_5.getLteSoap()
						.embmsProfileGet(nameList, null, credentialsLte);
				break;
			case Traffic_Management_Profile:
				response = soapHelper_15_5.getLteSoap()
						.trafficManagementProfileGet(nameList, null, credentialsLte);
				break;
			case Call_Trace_Profile:
				response = soapHelper_15_5.getLteSoap()
						.callTraceProfileGet(nameList, null, credentialsLte);
				break;
			default:
				report.report("isProfileExists get error CellProfiles type: " + profileType, Reporter.WARNING);
			}
			return response.getErrorCode() == Netspan.NBI_15_5.Lte.ErrorCodes.OK;

		} catch (Exception e) {
			e.printStackTrace();
			report.report(profileType.value() + "ProfileGet Failed, due to: " + e.getMessage(), Reporter.WARNING);
			return false;
		} finally {
			soapHelper_15_5.endLteSoap();
		}
	}

	private EnbNetworkProfile parseNetworkParameters(EnodeB node, NetworkParameters networkParams) {
		ObjectFactory factoryDetails = new ObjectFactory();
		EnbNetworkProfile networkProfile = new EnbNetworkProfile();
		networkProfile.setName(networkParams.getProfileName());

		// Backhaul Qos
		if (networkParams.getBackhaulQosAdmin() != null) {
			networkProfile.setBhQosAdmin(
					factoryDetails.createEnbNetworkProfileParamsBhQosAdmin(networkParams.getBackhaulQosAdmin()));
			networkProfile.setBhQosMinReservedForCallsNotYetAttempted(
					factoryDetails.createEnbNetworkProfileParamsBhQosMinReservedForCallsNotYetAttempted(
							networkParams.getBackhaulQosMinReservedForCalls()));
		}
		
		// HeNB
		if(networkParams.getOperateBehindHenbGw() != null){
			networkProfile.setOperateBehindHenbGw(factoryDetails
					.createEnbNetworkProfileParamsOperateBehindHenbGw(networkParams.getOperateBehindHenbGw()));
		}
			
		// ETWS Logging
		if (networkParams.getEtwsEnabled() != null) {
			networkProfile.setEtwsEnabled(
					factoryDetails.createEnbNetworkProfileParamsEtwsEnabled(networkParams.getEtwsEnabled()));
			networkProfile.setEtwsUploadPeriod(
					factoryDetails.createEnbNetworkProfileParamsEtwsUploadPeriod(networkParams.getEtwsUploadPeriod()));
			networkProfile.setEtwsUploadPeriodNoData(factoryDetails
					.createEnbNetworkProfileParamsEtwsUploadPeriodNoData(networkParams.getEtwsUploadPeriodNoData()));
			networkProfile.setEtwsFileServer(networkParams.getEtwsFileServerName());
		}

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
		if (networkParams.getMMEIPS() != null) {
			S1ListContainer tempContainer = new S1ListContainer();
			LteS1EntryWs tempLteS1Entry = null;
			for (String mmeip : networkParams.getMMEIPS()) {
				tempLteS1Entry = new LteS1EntryWs();
				tempLteS1Entry.setMmeIpAddress(mmeip);
				tempContainer.getS1().add(tempLteS1Entry);
			}
			// add IPG ip in the List
			networkProfile.setS1X2List(tempContainer);
		}

		if (networkParams.getcSonConfig() != null) {
			LteCSonEntryWs cSonConfig = new LteCSonEntryWs();
			cSonConfig.setIsCSonConfigured(
					factoryDetails.createLteCSonEntryWsIsCSonConfigured(networkParams.getIsCSonConfigured()));
			cSonConfig.setCSonIpAddress(networkParams.getcSonIpAddress());
			cSonConfig.setCSonServerPort(
					factoryDetails.createLteCSonEntryWsCSonServerPort(networkParams.getcSonServerPort()));
			networkProfile.setCSONConfig(cSonConfig);
		}

		if (networkParams.getCdrxConnectedMode() != null) {
			networkProfile.setConnectedModeDrx(
					factoryDetails.createEnbNetworkProfileParamsConnectedModeDrx(networkParams.getCdrxConnectedMode()));
		}
		return networkProfile;
	}

	@Override
	public boolean updateNetworkProfile(EnodeB node, String cloneFromName, NetworkParameters networkParams) {
		try {
			EnbNetworkProfile networkProfile = parseNetworkParameters(node, networkParams);
			ProfileResponse updateResult = soapHelper_15_5.getLteSoap()
					.networkProfileUpdate(cloneFromName, networkProfile, credentialsLte);

			if (updateResult == null) {
				report.report("Fail to get Network profile update result", Reporter.WARNING);
				return false;
			} else if (updateResult.getErrorCode() == Netspan.NBI_15_5.Lte.ErrorCodes.OK) {
				report.report("Succeeded to update Network profile " + networkProfile.getName());
				return true;
			} else {
				report.report("Failed to update Network profile, reason: " + updateResult.getErrorString(),
						Reporter.WARNING);
				return false;
			}

		} catch (Exception e) {
			e.printStackTrace();
			report.report("Failed to update Network profile, due to: " + e.getMessage(), Reporter.WARNING);
			return false;
		} finally {
			soapHelper_15_5.endLteSoap();
		}
	}

	@Override
	public boolean cloneNetworkProfile(EnodeB node, String cloneFromName, NetworkParameters networkParams)
			throws Exception {

		EnbNetworkProfile networkProfile = parseNetworkParameters(node, networkParams);
		ProfileResponse cloneResult = soapHelper_15_5.getLteSoap().networkProfileClone(cloneFromName,
				networkProfile, credentialsLte);
		if (cloneResult == null) {
			report.report("Fail to get Network profile cloning result", Reporter.WARNING);
			soapHelper_15_5.endLteSoap();
			return false;
		} else if (cloneResult.getErrorCode() == Netspan.NBI_15_5.Lte.ErrorCodes.OK) {
			report.report("Succeeded to clone Network profile, new profile name: " + networkParams.getProfileName());
			soapHelper_15_5.endLteSoap();
			return true;
		} else {
			report.report("Failed to clone Network profile, reason: " + cloneResult.getErrorString(), Reporter.WARNING);
			soapHelper_15_5.endLteSoap();
			return false;
		}
	}

	@Override
	public boolean cloneMobilityProfile(EnodeB node, String cloneFromName, MobilityParameters mobilityParams) {
		try {
			ObjectFactory factoryDetails = new ObjectFactory();
			EnbMobilityProfile mobilityProfile = new EnbMobilityProfile();
			MobilityConnectedModeInterFreq mobilityConnectedMode = null;

			if (mobilityParams.getQosBasedMeasurement() != null) {
				MobilityConnectedModeQosFreq mobilityConnectedModeQosFreq = new MobilityConnectedModeQosFreq();
				QosMobilityConnectedModeFreqEarfcnListContainer qosMobilityConnectedModeFreqEarfcnListContainer = new QosMobilityConnectedModeFreqEarfcnListContainer();
				mobilityProfile.setIsQosBasedMeasurementEnabled(
						factoryDetails.createEnbMobilityProfileParamsIsQosBasedMeasurementEnabled(
								mobilityParams.getQosBasedMeasurement()));
				if (mobilityParams.getQosBasedMeasurement().equals(EnabledDisabledStates.ENABLED)) {
					if (mobilityParams.getQosHoAccessAdmin().equals(EnabledDisabledStates.ENABLED)) {
						mobilityProfile.setIsQosHoAccessAdminEnabled(
								factoryDetails.createEnbMobilityProfileParamsIsQosHoAccessAdminEnabled(
										mobilityParams.getQosHoAccessAdmin()));
						mobilityProfile.setIsQosHoBwCapcityAdminEnabled(
								factoryDetails.createEnbMobilityProfileParamsIsQosHoBwCapcityAdminEnabled(
										EnabledDisabledStates.DISABLED));
					} else {
						mobilityProfile.setIsQosHoAccessAdminEnabled(
								factoryDetails.createEnbMobilityProfileParamsIsQosHoAccessAdminEnabled(
										mobilityParams.getQosHoAccessAdmin()));
						mobilityProfile.setIsQosHoBwCapcityAdminEnabled(
								factoryDetails.createEnbMobilityProfileParamsIsQosHoBwCapcityAdminEnabled(
										EnabledDisabledStates.ENABLED));
					}
					for (Integer earfcn : mobilityParams.getQosBasedEarfcnList()) {
						QosEarfcn qosEarfcn = new QosEarfcn();
						qosEarfcn.setEarfcn(factoryDetails.createQosEarfcnEarfcn(earfcn));
						qosMobilityConnectedModeFreqEarfcnListContainer.getQosMobilityConnectedModeFreqEarfcn()
								.add(qosEarfcn);
					}
					mobilityConnectedModeQosFreq
							.setQosMobilityConnectedModeFreqEarfcnList(qosMobilityConnectedModeFreqEarfcnListContainer);
					mobilityConnectedModeQosFreq.setEventType(factoryDetails
							.createMobilityConnectedModeQosFreqEventType(mobilityParams.getQosBasedEventType()));
					mobilityConnectedModeQosFreq.setRsrpEventThreshold1(
							factoryDetails.createMobilityConnectedModeQosFreqRsrpEventThreshold1(
									mobilityParams.getQosBasedThreshold1()));
					mobilityConnectedModeQosFreq.setRsrpEventThreshold2(
							factoryDetails.createMobilityConnectedModeQosFreqRsrpEventThreshold2(
									mobilityParams.getQosBasedThreshold2()));
					mobilityProfile.setConnectedModeQos(mobilityConnectedModeQosFreq);
				}
			}

			if (mobilityParams.getEventType() != null) {
				mobilityConnectedMode = new MobilityConnectedModeInterFreq();
				mobilityConnectedMode.setEventType(
						factoryDetails.createMobilityConnectedModeFreqEventType(mobilityParams.getEventType()));
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
				} else {
					mobilityProfile.setConnectedModeInterFrequency(mobilityConnectedMode);
				}
			}

			if (mobilityParams.getThresholdBasedMeasurementDual() != null) {
				mobilityProfile.setIsThresholdBasedMeasurementDualMode(
						factoryDetails.createEnbMobilityProfileParamsIsThresholdBasedMeasurementDualMode(
								mobilityParams.getThresholdBasedMeasurementDual()));
			}

			if (mobilityParams.getThresholdBasedMeasurement() != null) {
				mobilityProfile.setIsThresholdBasedMeasurementEnabled(
						factoryDetails.createEnbMobilityProfileParamsIsThresholdBasedMeasurementEnabled(
								mobilityParams.getThresholdBasedMeasurement()));
				if (mobilityParams.getThresholdBasedMeasurement() == EnabledDisabledStates.ENABLED) {
					MobilityConnectedModeTriggerGaps triggerGap = new MobilityConnectedModeTriggerGaps();
					triggerGap.setRsrpEventThreshold1(factoryDetails
							.createMobilityConnectedModeTriggerGapsRsrpEventThreshold1(mobilityParams.getStartGap()));
					mobilityProfile.setConnectedModeThresholdTriggerGaps(triggerGap);

					MobilityConnectedModeStopGaps stopGap = new MobilityConnectedModeStopGaps();
					stopGap.setRsrpEventThreshold1(factoryDetails
							.createMobilityConnectedModeStopGapsRsrpEventThreshold1(mobilityParams.getStopGap()));
					mobilityProfile.setConnectedModeThresholdStopGaps(stopGap);
				}
			}
			if (mobilityParams.getProfileName() == null) {
				return false;
			} else {
				mobilityProfile.setName(mobilityParams.getProfileName());
			}

			ProfileResponse cloneResult = soapHelper_15_5.getLteSoap()
					.mobilityProfileClone(cloneFromName, mobilityProfile, credentialsLte);

			if (cloneResult == null) {
				report.report("Fail to get Mobility profile cloning result", Reporter.WARNING);
				return false;
			} else if (cloneResult.getErrorCode() == Netspan.NBI_15_5.Lte.ErrorCodes.OK) {
				report.report(
						"Succeeded to clone Mobility profile, new profile name: " + mobilityParams.getProfileName());
				return true;
			} else {
				report.report("Failed to clone Mobility profile, reason: " + cloneResult.getErrorString(),
						Reporter.WARNING);
				return false;
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			report.report("mobilityProfileClone via Netspan Failed due to: ", Reporter.WARNING);
		} finally {
			soapHelper_15_5.endLteSoap();
		}
		return false;
	}

	@Override
	public boolean cloneManagementProfile(EnodeB node, String cloneFromName, ManagementParameters managementParmas) {
		if (cloneFromName != null) {
			GeneralUtils.printToConsole("Trying to create automation Management Profile " + cloneFromName
					+ " via NBIF , creating an automation profiles for the run.");
		} else {
			GeneralUtils.printToConsole(
					"Trying to create automation Management Profile from the current Profile that in use via NBIF ,"
							+ " creating an automation profiles for the run.");
			cloneFromName = node.getDefaultNetspanProfiles().getManagement();
		}

		EnbManagementProfile managementProfile = CreateEnbManagementProfile(managementParmas);
		if (managementProfile == null) {
			return false;
		}

		try {
			ProfileResponse cloneResult = soapHelper_15_5.getLteSoap()
					.managementProfileClone(cloneFromName, managementProfile, credentialsLte);

			if (cloneResult == null) {
				report.report("Fail to get Management Profile cloning result", Reporter.WARNING);
				return false;
			} else if (cloneResult.getErrorCode() == Netspan.NBI_15_5.Lte.ErrorCodes.OK) {
				report.report("Succeeded to clone Management Profile, new profile name: "
						+ managementParmas.getProfileName());
				return true;
			} else {
				report.report("Failed to clone Management Profile, reason: " + cloneResult.getErrorString(),
						Reporter.WARNING);
				return false;
			}
		} catch (Exception e) {
			GeneralUtils.printToConsole("Could not execute ManagementProfileClone on " + cloneFromName);
		}
		return false;
	}

	private EnbManagementProfile CreateEnbManagementProfile(ManagementParameters managementParmas) {
		ObjectFactory factoryDetails = new ObjectFactory();

		EnbManagementProfile managementProfile = factoryDetails.createEnbManagementProfile();
		managementProfile.setName(managementParmas.profileName);

		if (managementParmas.maintenanceWindow != null) {
			managementProfile.setMaintenanceWindowIsEnabled(factoryDetails
					.createEnbManagementProfileParamsMaintenanceWindowIsEnabled(managementParmas.maintenanceWindow));
			if (managementParmas.maintenanceWindow == true) {
				MaintenanceWindowConfigurationWs maintenanceWindowConfigurationWs = factoryDetails
						.createMaintenanceWindowConfigurationWs();
				maintenanceWindowConfigurationWs
						.setTimeZone(factoryDetails.createMaintenanceWindowConfigurationWsTimeZone(
								TimeZones.fromValue(managementParmas.timeZone)));
				maintenanceWindowConfigurationWs.setStartTime(managementParmas.maintenanceWindowStartTime);
				maintenanceWindowConfigurationWs.setEndTime(managementParmas.maintenanceWindowEndTime);
				maintenanceWindowConfigurationWs.setMaxRandomDelay(
						factoryDetails.createRelayProfileParamsMaxRandomDelay(managementParmas.maxRandomDelayPercent));
				maintenanceWindowConfigurationWs.setAutonomousReboot(factoryDetails
						.createMaintenanceWindowConfigurationWsAutonomousReboot(managementParmas.autonomousReboot));

				managementProfile.setMaintenanceWindowConfiguration(maintenanceWindowConfigurationWs);

			}
		}
		return managementProfile;
	}

	@Override
	public boolean cloneSystemDefaultProfile(EnodeB node, String cloneFromName,
			SystemDefaultParameters systemDefaultParmas) {
		if (cloneFromName != null) {
			GeneralUtils.printToConsole("Trying to create automation System Default Profile " + cloneFromName
					+ " via NBIF , creating an automation profiles for the run.");
		} else {
			GeneralUtils.printToConsole(
					"Trying to create automation System Default Profile from the current Profile that in use via NBIF ,"
							+ " creating an automation profiles for the run.");
			cloneFromName = node.getDefaultNetspanProfiles().getRadio();
		}

		LteSystemDefaultProfile systemDefaultProfile = new LteSystemDefaultProfile();
		systemDefaultProfile.setName(systemDefaultParmas.getProfileName());

		try {
			ProfileResponse cloneResult = soapHelper_15_5.getLteSoap()
					.systemDefaultProfileClone(cloneFromName, systemDefaultProfile, credentialsLte);

			if (cloneResult == null) {
				report.report("Fail to get System Default Profile cloning result", Reporter.WARNING);
				return false;
			} else if (cloneResult.getErrorCode() == Netspan.NBI_15_5.Lte.ErrorCodes.OK) {
				report.report("Succeeded to clone System Default Profile, new profile name: "
						+ systemDefaultParmas.getProfileName());
				return true;
			} else {
				report.report("Failed to clone System Default Profile, reason: " + cloneResult.getErrorString(),
						Reporter.WARNING);
				return false;
			}
		} catch (Exception e) {
			GeneralUtils.printToConsole("Could not execute SystemDefaultProfileClone on " + cloneFromName);
		}
		return false;
	}

	private EnbSonProfile parseSonParameters(EnodeB node, SonParameters sonParmas) {
		ObjectFactory factoryDetails = new ObjectFactory();
		EnbSonProfile sonProfile = new EnbSonProfile();
		sonProfile.setName(sonParmas.getProfileName());
		EnbDetailsGet nodeConf = getNodeConfig(node);
		CategoriesLte category = null;
		try{
			category = CategoriesLte.fromValue(nodeConf.getHardware());
		}catch(Exception e){
			report.report("Incompatible HardWare Type in inner Netspan method!",Reporter.WARNING);
			GeneralUtils.printToConsole("Error : "+e.getMessage()+", nodeConfig :"+nodeConf.getHardware());
		}
		sonProfile.setHardwareCategory(factoryDetails.createCallTraceProfileHardwareCategory(category));
		if (sonParmas.isSonCommissioning() != null) {
			sonProfile.setSonCommissioningEnabled(
					factoryDetails.createEnbSonProfileParamsSonCommissioningEnabled(sonParmas.isSonCommissioning()));
			// disable change of parameter due to NBI Change of param from
			// boolean to enum
			if (sonParmas.isSonCommissioning()) {
				sonProfile.setPciEnabled(
						factoryDetails.createEnbSonProfileParamsPciEnabled(sonParmas.isAutoPCIEnabled()));
				if (sonParmas.isAutoPCIEnabled() != null && sonParmas.isAutoPCIEnabled()) {
					if (sonParmas.getPciCollisionHandling() != null) {
						sonProfile.setPciCollisionHandling(factoryDetails.createEnbSonProfileParamsPciCollisionHandling(
								PciConflictHandlingValues.fromValue(sonParmas.getPciCollisionHandling().value())));
					}

					if (sonParmas.getPciConfusionHandling() != null) {
						sonProfile.setPciConfusionHandling(factoryDetails.createEnbSonProfileParamsPciConfusionHandling(
								PciConflictHandlingValues.fromValue(sonParmas.getPciConfusionHandling().value())));
					}
					if (sonParmas.getPciPolicyViolationHandling() != null) {
						sonProfile.setPciPolicyViolationHandling(factoryDetails
								.createEnbSonProfileParamsPciPolicyViolationHandling(PciConflictHandlingValues
										.fromValue(sonParmas.getPciPolicyViolationHandling().value())));
					}
					PciRangeListContainer ranges = new PciRangeListContainer();
					List<Pair<Integer, Integer>> rangesList = sonParmas.getRangesList();
					if (rangesList != null) {
						for (Pair<Integer, Integer> pair : rangesList) {
							PciRange pciRange = new PciRange();
							pciRange.setPciStart(factoryDetails.createPciRangePciStart(pair.getElement0()));
							pciRange.setPciEnd(factoryDetails.createPciRangePciEnd(pair.getElement1()));
							ranges.getPciRange().add(pciRange);
						}
					}
					sonProfile.setPciRangeList(ranges);
				}
				sonProfile.setRsiEnabled(
						factoryDetails.createEnbSonProfileParamsRsiEnabled(sonParmas.isAutoRSIEnabled()));
				if (sonParmas.isAutoRSIEnabled() != null && sonParmas.isAutoRSIEnabled()) {
					RsiRangeListContainer ranges = new RsiRangeListContainer();
					List<Triple<Integer, Integer, Integer>> rangesRsiList = sonParmas.getRangesRSIList();
					if (rangesRsiList != null) {
						for (Triple<Integer, Integer, Integer> triple : rangesRsiList) {
							RsiRange rsiRange = new RsiRange();
							rsiRange.setRsiStart(factoryDetails.createRsiRangeRsiStart(triple.getLeftElement()));
							rsiRange.setRsiStep(factoryDetails.createRsiRangeRsiStep(triple.getMiddleElement()));
							rsiRange.setRsiListSize(factoryDetails.createRsiRangeRsiListSize(triple.getRightElement()));
							ranges.getRsiRange().add(rsiRange);
						}
					}
					sonProfile.setRsiRangeList(ranges);
				}
				SonAnrStates anrState = sonParmas.getAnrState() != null ? sonParmas.getAnrState()
						: SonAnrStates.DISABLED;
				sonProfile.setAnrState(factoryDetails.createEnbSonProfileParamsAnrState(anrState));// createEnbSonProfileAnrState(anrState));
				sonProfile.setPnpMode(factoryDetails.createEnbSonProfileParamsPnpMode(sonParmas.getPnpMode()));
				if (anrState != SonAnrStates.DISABLED) {
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
						sonProfile.setAnrFrequencyList(anrFrequency);
					}
					if (sonParmas.getMinAllowedHoSuccessRate() != null) {
						sonProfile.setMinAllowedHoSuccessRate(
								factoryDetails.createEnbSonProfileParamsMinAllowedHoSuccessRate(
										sonParmas.getMinAllowedHoSuccessRate()));
					}
				}
			}
		}

		if (sonParmas.getOptimizationMode() != null) {
			sonProfile.setOptimizationMode(
					factoryDetails.createEnbSonProfileParamsOptimizationMode(sonParmas.getOptimizationMode()));
			if (sonParmas.getOptimizationMode().equals(EnabledDisabledStates.ENABLED)) {
				LteSonDynIcic lteSonDynIcic = new LteSonDynIcic();
				if (sonParmas.getIcicMode() != null && sonParmas.getIcicMode().equals(EnabledDisabledStates.ENABLED)) {
					lteSonDynIcic.setIcicMode(factoryDetails.createLteSonDynIcicIcicMode(sonParmas.getIcicMode()));
					lteSonDynIcic.setIcicSchemeType(
							factoryDetails.createLteSonDynIcicIcicSchemeType(sonParmas.getIcicSchemeType()));
					lteSonDynIcic.setMinThresholdCeu(
							factoryDetails.createLteSonDynIcicMinThresholdCeu(sonParmas.getMinThresholdCeu()));
					lteSonDynIcic.setMaxThresholdCeu(
							factoryDetails.createLteSonDynIcicMaxThresholdCeu(sonParmas.getMaxThresholdCeu()));
					lteSonDynIcic.setUnmanagedInterferenceHandling(
							factoryDetails.createLteSonDynIcicUnmanagedInterferenceHandling(
									sonParmas.getUnmanagedInterferenceHandling()));
					sonProfile.setDynamicIcic(lteSonDynIcic);
				}
			}
		}

		if (sonParmas.isCSonEnabled() != null) {
			LteSonCSonWs sonProfileCSon = factoryDetails.createLteSonCSonWs();
			sonProfileCSon.setIsCSonEnabled(factoryDetails.createLteSonCSonWsIsCSonEnabled(sonParmas.isCSonEnabled()));
			if (sonParmas.isCSonEnabled()) {
				sonProfileCSon.setIsCSonRachEnabled(
						factoryDetails.createLteSonCSonWsIsCSonRachEnabled(sonParmas.iscSonRachMode()));
				sonProfileCSon.setIsCSonMcimEnabled(
						factoryDetails.createLteSonCSonWsIsCSonMcimEnabled(sonParmas.iscSonMcimMode()));
				sonProfileCSon.setIsCSonMroEnabled(
						factoryDetails.createLteSonCSonWsIsCSonMroEnabled(sonParmas.iscSonMroMode()));
				sonProfileCSon.setIsCSonMlbEnabled(
						factoryDetails.createLteSonCSonWsIsCSonMlbEnabled(sonParmas.iscSonMlbMode()));
				sonProfileCSon.setCSonMlbCapacityClassValue(
						factoryDetails.createLteSonCSonWsCSonMlbCapacityClassValue(sonParmas.getcSonCapacityClass()));
				sonProfileCSon.setCSonMlbPdschLoadThresh(
						factoryDetails.createLteSonCSonWsCSonMlbPdschLoadThresh(sonParmas.getcSonPDSCHLoad()));
				sonProfileCSon.setCSonMlbPuschLoadThresh(
						factoryDetails.createLteSonCSonWsCSonMlbPuschLoadThresh(sonParmas.getcSonPUSCHLoad()));
				sonProfileCSon.setCSonMlbRrcLoadThresh(
						factoryDetails.createLteSonCSonWsCSonMlbRrcLoadThresh(sonParmas.getcSonRRCLoad()));
				sonProfileCSon.setCSonMlbCpuLoadThresh(
						factoryDetails.createLteSonCSonWsCSonMlbCpuLoadThresh(sonParmas.getcSonCPUCHLoad()));
			}
			sonProfile.setCSon(sonProfileCSon);
		}
		return sonProfile;
	}

	@Override
	public boolean cloneSonProfile(EnodeB node, String cloneFromName, SonParameters sonParmas) {
		try {

			EnbSonProfile sonProfile = parseSonParameters(node, sonParmas);
			ProfileResponse cloneResult = soapHelper_15_5.getLteSoap().sonProfileClone(cloneFromName,
					sonProfile, credentialsLte);

			if (cloneResult == null) {
				report.report("Fail to get Son profile cloning resut", Reporter.WARNING);
				return false;
			} else if (cloneResult.getErrorCode() == Netspan.NBI_15_5.Lte.ErrorCodes.OK) {
				report.report("Succeeded to clone Son profile, new profile name: " + sonParmas.getProfileName());
				return true;
			} else {
				report.report("Failed to clone Son profile, reason: " + cloneResult.getErrorString(), Reporter.WARNING);
				return false;
			}

		} catch (Exception e) {
			e.printStackTrace();
			report.report("Failed to clone Son profile, due to: " + e.getMessage(), Reporter.WARNING);
			return false;
		} finally {
			soapHelper_15_5.endLteSoap();
		}
	}

	@Override
	public boolean updateSonProfile(EnodeB node, String cloneFromName, SonParameters sonParams) {
		try {
			EnbSonProfile sonProfile = parseSonParameters(node, sonParams);
			ProfileResponse updateResult = soapHelper_15_5.getLteSoap()
					.sonProfileUpdate(cloneFromName, sonProfile, credentialsLte);

			if (updateResult == null) {
				report.report("Fail to get Son profile update result", Reporter.WARNING);
				return false;
			} else if (updateResult.getErrorCode() == Netspan.NBI_15_5.Lte.ErrorCodes.OK) {
				report.report("Succeeded to update Son profile " + sonParams.getProfileName());
				return true;
			} else {
				report.report("Failed to update Son profile, reason: " + updateResult.getErrorString(),
						Reporter.WARNING);
				return false;
			}

		} catch (Exception e) {
			e.printStackTrace();
			report.report("Failed to update Son profile, due to: " + e.getMessage(), Reporter.WARNING);
			return false;
		} finally {
			soapHelper_15_5.endLteSoap();
		}
	}

	@Override
	public NeighborData getNeighborData(String nodeName, String nghName) {
		LteSonAnrGetResult AnrResult = soapHelper_15_5.getStatusSoap().enbSonAnrStatusGet(nodeName,
				credentialsStatus);
		if (!AnrResult.getAnr().isEmpty()) {
			for (LteAnrStatusWs ngh : AnrResult.getAnr()) {
				if (ngh.getName().equals(nghName)) {
					report.report("Getting neighbor data from Netspan.");
					return parseNeighborData(ngh);
				}
			}
		}
		report.report(String.format("Could not find %s in %s ngh list.", nghName, nodeName));
		return null;
	}

	@Override
	public boolean getDicicUnmanagedInterferenceStatus(String nodeName, int nghPci) {
		LteSonDynamicIcicUnmanagedInterferenceGetResult dicicUnmanagedInterferenceResult = soapHelper_15_5
				.getStatusSoap().enbSonDynamicIcicUnmanagedInterferenceStatusGet(nodeName, credentialsStatus);
		if (dicicUnmanagedInterferenceResult.getNodeResult().equals(NodeStatusResultValues.OK)) {
			if (dicicUnmanagedInterferenceResult.getCell().size() > 0) {
				int nghDicicPci = dicicUnmanagedInterferenceResult.getCell().get(FIRST_ELEMENT).getPci().getValue();
				report.report(String.format("%s has pci %s on unmanaged interference list.", nodeName, nghPci));
				return nghDicicPci == nghPci;
			}
		}
		report.report(String.format("Could not find pci %s in %s unmanaged interference list.", nghPci, nodeName),
				Reporter.WARNING);
		return false;

	}

	private NeighborData parseNeighborData(LteAnrStatusWs ngh) {
		try {
			NeighborData nghData = new NeighborData();
			nghData.name = ngh.getName();
			nghData.pci = ngh.getPci().getValue();
			nghData.enbType = ngh.getEnbType().getValue();
			nghData.enbId = ngh.getEnbId();
			nghData.cellId = ngh.getCellId().getValue();
			nghData.cellIdentity = ngh.getCellIdentity().getValue();
			nghData.tac = ngh.getTac().getValue();
			nghData.downlinkEarfcn = ngh.getDownlinkEarfcn().getValue();
			nghData.mcc = ngh.getMcc();
			nghData.mnc = ngh.getMnc();
			nghData.hoControlStatus = ngh.getHoControlStatus().getValue();
			nghData.x2ControlStatus = ngh.getX2ControlStatus().getValue();
			nghData.handoverType = ngh.getHandoverType().getValue();
			nghData.discoveredBy = ngh.getDiscoveredBy().getValue();
			nghData.isStaticNeighbour = ngh.getIsStaticNeighbour().getValue();
			nghData.hoSuccessRate = ngh.getHoSuccessRate().getValue();
			nghData.pi = ngh.getPi().getValue();
			nghData.qOffset = ngh.getQOffset().getValue();
			nghData.activeQOffset = ngh.getActiveQOffset().getValue();
			nghData.mvno1Mcc = ngh.getMvno1Mcc();
			nghData.mvno1Mnc = ngh.getMvno1Mnc();
			nghData.mvno2Mcc = ngh.getMvno2Mcc();
			nghData.mvno2Mnc = ngh.getMvno2Mnc();
			nghData.mvno3Mcc = ngh.getMvno3Mcc();
			nghData.mvno3Mnc = ngh.getMvno3Mnc();
			nghData.mvno4Mcc = ngh.getMvno4Mcc();
			nghData.mvno4Mnc = ngh.getMvno4Mnc();
			nghData.mvno5Mcc = ngh.getMvno5Mcc();
			nghData.mvno5Mnc = ngh.getMvno5Mnc();
			nghData.cellIndividualOffset = ngh.getCellIndividualOffset().getValue();
			nghData.activeCellIndividualOffset = ngh.getActiveCellIndividualOffset().getValue();
			nghData.prachRsi = ngh.getPrachRsi().getValue();
			nghData.prachRsi0CorelZoneConfig = ngh.getPrachRsi0CorelZoneConfig().getValue();
			nghData.prachFreqOffset = ngh.getPrachFreqOffset().getValue();
			nghData.prachCfgIndex = ngh.getPrachCfgIndex().getValue();
			nghData.commsStatus = ngh.getCommsStatus().getValue();
			nghData.commsStatusDisplay = ngh.getCommsStatusDisplay();
			nghData.neighbourOfCell = ngh.getNeighbourOfCell();
			nghData.cellStatus = ngh.getCellStatus().getValue();
			// nghData.csgId = ngh.getCsgId().getValue();
			nghData.avgRsrp = ngh.getAvgRsrp().getValue();
			nghData.interferingNeighbor = ngh.getInterferingNeighbor().getValue();
			return nghData;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public int getDLTPTPerQci(String nodeName, int qci) {
		LteIpThroughputGetResult result = soapHelper_15_5.getStatusSoap()
				.enbIpThroughputStatusGet(nodeName, credentialsStatus);
		List<LteIpThroughputQciWs> a = null;
		try {
			a = result.getCell().get(0).getQciData();
		} catch (Exception e) {
			e.printStackTrace();
			return GeneralUtils.ERROR_VALUE;
		}

		for (LteIpThroughputQciWs qciData : a) {
			if (qciData.getQci().getValue() == qci) {
				return qciData.getMacTrafficKbpsDl().getValue();
			}
		}
		return GeneralUtils.ERROR_VALUE;
	}

	@Override
	public boolean convertToPnPConfig(EnodeB node) {
		try {
			NodeActionResult result = null;
			HardwareCategory hardwareCategory = getHardwareCategory(node);
			ArrayList<HardwareCategory> relayEnodeBs = new ArrayList<>();
			relayEnodeBs.add(HardwareCategory.AIR_DENSITY);
			relayEnodeBs.add(HardwareCategory.AIR_SPEED);
			relayEnodeBs.add(HardwareCategory.AIR_UNITY);
			
			String swImageName = null;
			if(node.isSwUpgradeDuringPnP()){
				swImageName = node.getDefaultNetspanProfiles().getSoftwareImage();
			}
			
			if (relayEnodeBs.contains(hardwareCategory)) {
				AuPnpDetailWs pnpDetail = null;
				if(swImageName != null){
					pnpDetail = new AuPnpDetailWs();
					pnpDetail.setPnpSwImageName(swImageName);
				}
				result = soapHelper_15_5.getLteSoap().relayEnbPnpConfigConvertFromNode(
						node.getNetspanName(), null, pnpDetail, null, null, null, null, credentialsLte);
			} else {
				PnpDetailWs pnpDetail = null;
				if(swImageName != null){
					pnpDetail = new PnpDetailWs();
					pnpDetail.setPnpSwImageName(swImageName);
				}
				result = soapHelper_15_5.getLteSoap().pnpConfigConvertFromNode(node.getNetspanName(),
						null, pnpDetail, null, credentialsLte);
			}

			if (result == null) {
				report.report("Fail to get System Default Profile cloning result", Reporter.WARNING);
				return false;
			} else if (result.getErrorCode() == Netspan.NBI_15_5.Lte.ErrorCodes.OK) {
				report.report("Succeeded to Convert From Node to PnP Config.");
				return true;
			} else {
				report.report("Failed to Convert From Node to PnP Config.", Reporter.WARNING);
				return false;
			}
		} catch (Exception e) {
			GeneralUtils
					.printToConsole("Could not execute pnpConfigConvertFromNodeMethodName on " + node.getNetspanName());
		}

		return false;
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
				result = soapHelper_15_5.getLteSoap().pnpConfigGet(nodeListName, credentialsLte);
			} catch (Exception e) {
				report.report("enbConfigGet via Netspan Failed due to: " + e.getMessage(), Reporter.WARNING);
				e.printStackTrace();
				return null;
			} finally {
				soapHelper_15_5.endLteSoap();
			}
			if (result.getErrorCode() != Netspan.NBI_15_5.Lte.ErrorCodes.OK) {
				GeneralUtils.printToConsole("enbConfigGet via Netspan Failed : " + result.getErrorString());
				return null;
			}
			GeneralUtils.printToConsole("enbConfigGet via Netspan for eNodeB " + enb.getNetspanName() + " succeeded");
			return convertPnPToEnb(result);
		}
		
		LteEnbConfigGetResult result = null;
		try {
			result = soapHelper_15_5.getLteSoap().enbConfigGet(nodeListName, credentialsLte);
		} catch (Exception e) {
			report.report("enbConfigGet via Netspan Failed due to: " + e.getMessage(), Reporter.WARNING);
			e.printStackTrace();
			return null;
		} finally {
			soapHelper_15_5.endLteSoap();
		}
		if (result.getErrorCode() != Netspan.NBI_15_5.Lte.ErrorCodes.OK) {
			GeneralUtils.printToConsole("enbConfigGet via Netspan Failed : " + result.getErrorString());
			return null;
		}
		GeneralUtils.printToConsole("enbConfigGet via Netspan for eNodeB " + enb.getNetspanName() + " succeeded");
		return result.getNodeResult().get(FIRST_ELEMENT).getEnbConfig();
	}

	@Override
	public boolean getNetspanProfile(EnodeB node, String cloneFromName, INetspanProfile profileParams) {
		if (profileParams.getType() == EnbProfiles.System_Default_Profile) {
			return getSystemDefaultProfile(node, cloneFromName);
		} else {
			if (profileParams.getType() == EnbProfiles.Neighbour_Management_Profile) {
				return getNeighborManagementProfile(node, cloneFromName);
			} else {
				if (profileParams.getType() == EnbProfiles.MultiCell_Profile) {
					return getMultiCellProfile(node, cloneFromName);
				} else {
					return super.getNetspanProfile(node, cloneFromName, profileParams);
				}
			}
		}
	}

	private boolean getSystemDefaultProfile(EnodeB node, String cloneFromName) {
		List<String> names = new ArrayList<>();
		List<CategoriesLte> hardwares = new ArrayList<CategoriesLte>();
		EnbDetailsGet nodeConf = getNodeConfig(node);
		CategoriesLte category = null;
		try{
			category = CategoriesLte.fromValue(nodeConf.getHardware());
		}catch(Exception e){
			report.report("Incompatible HardWare Type in inner Netspan method!",Reporter.WARNING);
			GeneralUtils.printToConsole("Error : "+e.getMessage()+", nodeConfig :"+nodeConf.getHardware());
			return false;
		}
		hardwares.add(category);
		names.add(cloneFromName);
		try {
			soapHelper_15_5.getLteSoap().systemDefaultProfileGet(names, hardwares,credentialsLte);
		} catch (Exception e) {
			e.printStackTrace();
			soapHelper_15_5.endLteSoap();
			return false;
		}
		return true;
	}

	public boolean getMultiCellProfile(EnodeB node, String cloneFromName) {
		List<String> names = new ArrayList<>();
		List<CategoriesLte> hardwares = new ArrayList<CategoriesLte>();
		EnbDetailsGet nodeConf = getNodeConfig(node);
		CategoriesLte hardware = null;
		try{
			 hardware = CategoriesLte.fromValue(nodeConf.getHardware());
		}catch(Exception e){
			report.report("Incompetible HardWare Type in inner Netspan method!",Reporter.WARNING);
			GeneralUtils.printToConsole("Error : "+e.getMessage()+", nodeConfig :"+nodeConf.getHardware());
		}
		hardwares.add(hardware);
		names.add(cloneFromName);
		try {
			soapHelper_15_5.getLteSoap().multiCellProfileGet(names, hardwares,credentialsLte);
		} catch (Exception e) {
			e.printStackTrace();
			soapHelper_15_5.endLteSoap();
			return false;
		}
		return true;
	}

	public boolean getNeighborManagementProfile(EnodeB node, String cloneFromName) {
		List<String> names = new ArrayList<>();
		List<CategoriesLte> hardwares = new ArrayList<CategoriesLte>();
		EnbDetailsGet nodeConf = getNodeConfig(node);
		CategoriesLte hardware = null;
		try{
			 hardware = CategoriesLte.fromValue(nodeConf.getHardware());
		}catch(Exception e){
			report.report("Incompatible HardWare Type in inner Netspan method!",Reporter.WARNING);
			GeneralUtils.printToConsole("Error : "+e.getMessage()+", nodeConfig :"+nodeConf.getHardware());
			return false;
		}
		hardwares.add(hardware);
		names.add(cloneFromName);
		try {
			soapHelper_15_5.getLteSoap().neighbourProfileGet(names, hardwares,credentialsLte);
		} catch (Exception e) {
			e.printStackTrace();
			soapHelper_15_5.endLteSoap();
			return false;
		}
		return true;
	}
	
	@Override
	public HashMap<String, NetworkElementStatus> getMMEStatuses(EnodeB enb) {
		HashMap<String, NetworkElementStatus> mmeStatuses = new HashMap<>();
		LteNetworkElementGetResult result = soapHelper_15_5.getStatusSoap().enbNetworkElementStatusGet(enb.getNetspanName(),credentialsStatus);
		if(result != null){			
			for(LteNetworkElementStatusWs status : result.getNetworkElementStatus()){
				if (status.getNetworkElement().toLowerCase().contains("mme")){
					String s = status.getIpAddress();
					NetworkElementStatus n = NetworkElementStatus.fromValue(status.getStatus().value());
					mmeStatuses.put(s, n);
				}
			}
		}
		return mmeStatuses;
	}

	@Override
	public boolean cloneCellAdvancedProfile(EnodeB node, String cloneFromName, CellAdvancedParameters advancedParmas) {
		try {
			EnbCellAdvancedProfile advancedProfile = parseCellAdvancedParameters(node, advancedParmas);
			ProfileResponse cloneResult = soapHelper_15_5.getLteSoap()
					.cellAdvancedConfigProfileClone(cloneFromName, advancedProfile, credentialsLte);
			if (cloneResult == null) {
				report.report("Fail to get cell advanced profile cloning result", Reporter.WARNING);
				return false;
			} else if (cloneResult.getErrorCode() == Netspan.NBI_15_5.Lte.ErrorCodes.OK) {
				report.report("Succeeded to clone cell advanced  profile, new profile name: "
						+ advancedParmas.getProfileName());
				return true;
			} else {
				report.report("Failed to clone cell advanced profile, reason: " + cloneResult.getErrorString(),
						Reporter.WARNING);
				return false;
			}

		} catch (Exception e) {
			e.printStackTrace();
			report.report("Failed to clone cell advanced profile, due to: " + e.getMessage(), Reporter.WARNING);
			return false;
		} finally {
			soapHelper_15_5.endLteSoap();
		}
	}

	@Override
	public boolean updateCellAdvancedProfile(EnodeB node, CellAdvancedParameters advancedParmas) {
		try {
			EnbCellAdvancedProfile advancedProfile = parseCellAdvancedParameters(node, advancedParmas);
			ProfileResponse updateResult = soapHelper_15_5.getLteSoap()
					.cellAdvancedConfigProfileUpdate(advancedParmas.getProfileName(), advancedProfile, credentialsLte);

			if (updateResult == null) {
				report.report("Fail to get cell advanced profile update result", Reporter.WARNING);
				return false;
			} else if (updateResult.getErrorCode() == Netspan.NBI_15_5.Lte.ErrorCodes.OK) {
				report.report("Succeeded to update cell advanced profile " + advancedParmas.getProfileName());
				return true;
			} else {
				report.report("Failed to update cell advanced profile, reason: " + updateResult.getErrorString(),
						Reporter.WARNING);
				return false;
			}

		} catch (Exception e) {
			e.printStackTrace();
			report.report("Failed to update cell advanced profile, due to: " + e.getMessage(), Reporter.WARNING);
			return false;
		} finally {
			soapHelper_15_5.endLteSoap();
		}
	}

	private EnbCellAdvancedProfile parseCellAdvancedParameters(EnodeB node, CellAdvancedParameters advancedParmas) {
		EnbCellAdvancedProfile profile = new EnbCellAdvancedProfile();
		profile.setName(advancedParmas.getProfileName());
		ObjectFactory factoryDetails = new ObjectFactory();
		EtwsWs etwsWs = new EtwsWs();
		if (advancedParmas.getEtwsUserMode() == EnabledDisabledStates.ENABLED) {
			etwsWs.setEtwsModeIsDefault(factoryDetails.createEtwsWsEtwsModeIsDefault(false));
			etwsWs.setEtwsMode(factoryDetails.createEtwsWsEtwsMode(EtwsModes.USER));
			if (advancedParmas.getSib10Duration() != null) {
				etwsWs.setSib10DurationIsDefault(factoryDetails.createEtwsWsSib10DurationIsDefault(false));
				etwsWs.setSib10Duration(factoryDetails.createEtwsWsSib10Duration(advancedParmas.getSib10Duration()));
			}
			if (advancedParmas.getSib11Duration() != null) {
				etwsWs.setSib11DurationIsDefault(factoryDetails.createEtwsWsSib11DurationIsDefault(false));
				etwsWs.setSib11Duration(factoryDetails.createEtwsWsSib11Duration(advancedParmas.getSib11Duration()));
			}
		} else {
			etwsWs.setEtwsModeIsDefault(factoryDetails.createEtwsWsEtwsModeIsDefault(false));
			etwsWs.setEtwsMode(factoryDetails.createEtwsWsEtwsMode(EtwsModes.STANDARD));
		}
		profile.setEtws(etwsWs);
		return profile;
	}

	@Override
	public String getSyncState(EnodeB eNodeB) {
		NodeGpsGetResult result = null;
		try {
			result = soapHelper_15_5.getStatusSoap().nodeGpsGet(eNodeB.getNetspanName(),
					credentialsStatus);
		} catch (Exception e) {
			report.report("nodeGpsGet via Netspan Failed due to: " + e.getMessage(), Reporter.WARNING);
			e.printStackTrace();
			return null;
		}
		if (result.getErrorCode() != Netspan.NBI_15_5.Status.ErrorCodes.OK) {
			GeneralUtils.printToConsole("nodeGpsGet via Netspan Failed : " + result.getErrorString());
			return null;
		}
		GeneralUtils.printToConsole("nodeGpsGet via Netspan for eNodeB " + eNodeB.getNetspanName() + " succeeded");
		String syncState = null;
		try {
			syncState = result.getSynchronizationStatus().getSyncState().getValue().value();
		} catch (Exception e) {
			syncState = null;
			GeneralUtils.printToConsole("No Sync State");
			e.printStackTrace();
		}
		return syncState;
	}
	
	@Override
	public String getGPSStatus(EnodeB eNodeB) {
		NodeGpsGetResult result = null;
		try {
			result = soapHelper_15_5.getStatusSoap().nodeGpsGet(eNodeB.getNetspanName(),
					credentialsStatus);
		} catch (Exception e) {
			report.report("nodeGpsGet via Netspan Failed due to: " + e.getMessage(), Reporter.WARNING);
			e.printStackTrace();
			return null;
		}
		if (result.getErrorCode() != Netspan.NBI_15_5.Status.ErrorCodes.OK) {
			GeneralUtils.printToConsole("nodeGpsGet via Netspan Failed : " + result.getErrorString());
			return null;
		}
		GeneralUtils.printToConsole("nodeGpsGet via Netspan for eNodeB " + eNodeB.getNetspanName() + " succeeded");
		String gpsStatus = null;
		try {
			gpsStatus = result.getGpsStatus().getGpsLock().getValue().value();
		} catch (Exception e) {
			gpsStatus = null;
			GeneralUtils.printToConsole("No GPS Status");
			e.printStackTrace();
		}
		return gpsStatus;
	}

	@Override
	public boolean getRawNetspanGetProfileResponse(EnodeB node, String cloneFromName, INetspanProfile profileParams) {
		List<String> name = new ArrayList<>();
		name.add(cloneFromName);
		CategoriesLte category = null;
		List<CategoriesLte> categoryList = new ArrayList<>();
		EnbDetailsGet nodeConf = getNodeConfig(node);
		try{
			 category = CategoriesLte.fromValue(nodeConf.getHardware());
		}catch(Exception e){
			report.report("Incompatible HardWare Type in inner Netspan method!",Reporter.WARNING);
			GeneralUtils.printToConsole("Error : "+e.getMessage()+", nodeConfig :"+nodeConf.getHardware());
			return false;
		}
		categoryList.add(category);
		soapHelper_15_5.clearRawData();
		switch (profileParams.getType()) {
		case System_Default_Profile:
			soapHelper_15_5.getLteSoapRaw().systemDefaultProfileGet(name, categoryList, credentialsLte);
			break;
		case Management_Profile:
			soapHelper_15_5.getLteSoapRaw().managementProfileGet(name, categoryList, credentialsLte);
			break;
		case Mobility_Profile:
			soapHelper_15_5.getLteSoapRaw().mobilityProfileGet(name, categoryList, credentialsLte);
			break;
		case Network_Profile:
			soapHelper_15_5.getLteSoapRaw().networkProfileGet(name, categoryList, credentialsLte);
			break;
		case Radio_Profile:
			soapHelper_15_5.getLteSoapRaw().radioProfileGet(name, categoryList, credentialsLte);
			break;
		case Security_Profile:
			soapHelper_15_5.getLteSoapRaw().securityProfileGet(name, categoryList, credentialsLte);
			break;
		case Son_Profile:
			soapHelper_15_5.getLteSoapRaw().sonProfileGet(name, categoryList, credentialsLte);
			break;
		case Sync_Profile:
			soapHelper_15_5.getLteSoapRaw().syncProfileGet(name, categoryList, credentialsLte);
			break;
		case EnodeB_Advanced_Profile:
			soapHelper_15_5.getLteSoapRaw().enbAdvancedProfileConfigGet(name, categoryList, credentialsLte);
			break;
		case Cell_Advanced_Profile:
			soapHelper_15_5.getLteSoapRaw().cellAdvancedConfigProfileGet(name, categoryList, credentialsLte);
			break;
		case Neighbour_Management_Profile:
			soapHelper_15_5.getLteSoapRaw().neighbourProfileGet(name, categoryList, credentialsLte);
			break;
		case MultiCell_Profile:
			soapHelper_15_5.getLteSoapRaw().multiCellProfileGet(name, categoryList, credentialsLte);
			break;
		default:
			return false;
		}

		try {
			String rawData = soapHelper_15_5.getSOAPRawData();
			printHTMLRawData(rawData);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			soapHelper_15_5.endLteSoap();
		}
		return true;
	}

	@Override
	public boolean checkAndSetDefaultProfiles(EnodeB node,boolean setProfile) {
		GeneralUtils.startLevel("checking default profiles for node : "+node.getNetspanName());
		LteEnbDetailsSetWs enbConfigSet = new LteEnbDetailsSetWs();
		LteCellSetWs lteCellSet = new LteCellSetWs();
		ObjectFactory factoryDetails = new ObjectFactory();
		if(!isNetspanReachable()){
			GeneralUtils.stopLevel();
			return false;
		}
		try {
			DefaultNetspanProfiles enbProfiles = node.getDefaultNetspanProfiles();
			for (CellNetspanProfiles cellProfiles : enbProfiles.cellProfiles) {
				lteCellSet.setCellNumber(factoryDetails.createLteCellGetWsCellNumber(String.valueOf(cellProfiles.getCellId())));
				GeneralUtils.startLevel("checking profiles for Cell : "+String.valueOf(cellProfiles.getCellId()));
				lteCellSet = checkForExistsCellProfileAndSet15_5(enbProfiles,lteCellSet,cellProfiles,CellProfiles.Cell_Advanced_Profile,setProfile);
				lteCellSet = checkForExistsCellProfileAndSet15_5(enbProfiles,lteCellSet,cellProfiles,CellProfiles.Radio_Profile,setProfile);
				lteCellSet = checkForExistsCellProfileAndSet15_5(enbProfiles,lteCellSet,cellProfiles,CellProfiles.Mobility_Profile,setProfile);
				lteCellSet = checkForExistsCellProfileAndSet15_5(enbProfiles,lteCellSet,cellProfiles,CellProfiles.Embms_Profile,setProfile);
				lteCellSet = checkForExistsCellProfileAndSet15_5(enbProfiles,lteCellSet,cellProfiles,CellProfiles.Traffic_Management_Profile,setProfile);
				lteCellSet = checkForExistsCellProfileAndSet15_5(enbProfiles,lteCellSet,cellProfiles,CellProfiles.Call_Trace_Profile,setProfile);
				enbConfigSet.getLteCell().add(lteCellSet);
				GeneralUtils.stopLevel();
			}
			GeneralUtils.startLevel("checking profiles for node : "+node.getNetspanName());
			enbConfigSet = checkForExistsNodeProfileAndSet15_5(enbProfiles,enbConfigSet,EnbProfiles.System_Default_Profile,setProfile);
			enbConfigSet = checkForExistsNodeProfileAndSet15_5(enbProfiles,enbConfigSet,EnbProfiles.EnodeB_Advanced_Profile,setProfile);
			enbConfigSet = checkForExistsNodeProfileAndSet15_5(enbProfiles,enbConfigSet,EnbProfiles.Network_Profile,setProfile);
			enbConfigSet = checkForExistsNodeProfileAndSet15_5(enbProfiles,enbConfigSet,EnbProfiles.Sync_Profile,setProfile);
			enbConfigSet = checkForExistsNodeProfileAndSet15_5(enbProfiles,enbConfigSet,EnbProfiles.Security_Profile,setProfile);
			enbConfigSet = checkForExistsNodeProfileAndSet15_5(enbProfiles,enbConfigSet,EnbProfiles.Son_Profile,setProfile);
			enbConfigSet = checkForExistsNodeProfileAndSet15_5(enbProfiles,enbConfigSet,EnbProfiles.Management_Profile,setProfile);
			enbConfigSet = checkForExistsNodeProfileAndSet15_5(enbProfiles,enbConfigSet,EnbProfiles.Neighbour_Management_Profile,setProfile);
			GeneralUtils.stopLevel();
			
			if(!setProfile){
				return true;
			}

			Netspan.NBI_15_5.Lte.NodeActionResult result = soapHelper_15_5
					.getLteSoap().enbConfigSet(node.getNetspanName(), new NodeProperty(), enbConfigSet,
							new SnmpDetailSetWs(), credentialsLte);

			if (result.getErrorCode() != Netspan.NBI_15_5.Lte.ErrorCodes.OK) {
				report.report("Set default profiles via Netspan Failed : " + result.getErrorString());
				List<NodeResult> nodeErrors = result.getNode();
				for (NodeResult error : nodeErrors) {
					if (error != null) {
						report.report("SubResultError:" + error.getNodeResultString(), Reporter.WARNING);
					}
				}
				return false;
			}else
				return true;
		} catch (Exception e) {
			e.printStackTrace();
			report.report("Set default profiles via Netspan Failed due to: " + e.getMessage());
			return false;
		} finally {
			soapHelper_15_5.endLteSoap();
			GeneralUtils.stopLevel();
		}
	}

	private LteEnbDetailsSetWs checkForExistsNodeProfileAndSet15_5(DefaultNetspanProfiles enbProfiles,LteEnbDetailsSetWs enbConfigSet,EnbProfiles profileType,boolean setProfile){
		String profileName = enbProfiles.getDefaultProfile(profileType);
		if(profileName != null){
			if(isProfileExists(profileName,profileType)){
				if(setProfile){
					enbConfigSet = injectContentToWsEnbObject(enbConfigSet,enbProfiles,profileType);
				}
			}else{
				report.report("Profile type : "+profileType+", is not exists in Netspan : "+this.getHostname(),Reporter.WARNING);
			}
		}
		return enbConfigSet;
	}
	

	private LteEnbDetailsSetWs injectContentToWsEnbObject(LteEnbDetailsSetWs enbConfigSet,DefaultNetspanProfiles enbProfiles, EnbProfiles profileType){
		switch(profileType){
		case System_Default_Profile:
			enbConfigSet.setSystemDefaultProfile(enbProfiles.getSystemDefault());
			break;
		case EnodeB_Advanced_Profile:
			enbConfigSet.setAdvancedConfigProfile(enbProfiles.getEnodeBAdvanced());
			break;
		case Network_Profile:
			enbConfigSet.setNetworkProfile(enbProfiles.getNetwork());
			break;
		case Sync_Profile:
			enbConfigSet.setSyncProfile(enbProfiles.getSynchronisation());
			break;
		case Security_Profile:
			enbConfigSet.setSecurityProfile(enbProfiles.getSecurity());
			break;
		case Son_Profile:
			enbConfigSet.setSonProfile(enbProfiles.getSON());
			break;
		case Management_Profile:
			enbConfigSet.setManagementProfile(enbProfiles.getManagement());
			break;
		case MultiCell_Profile:
			enbConfigSet.setMultiCellProfile(enbProfiles.getMultiCell());
			break;
		case Neighbour_Management_Profile:
			enbConfigSet.setNeighbourProfile(enbProfiles.getNeighbourManagement());
			break;
		default:
			report.report("there is no Node Profile with the type : "+profileType);
		}
		return enbConfigSet;
	}
	
	private LteCellSetWs checkForExistsCellProfileAndSet15_5(DefaultNetspanProfiles enbProfiles, LteCellSetWs lteCellSetWs,CellNetspanProfiles cellProfiles,CellProfiles profileType,boolean setProfile){
		String profileName = cellProfiles.getProfile(profileType);
		if (profileName != null) {
			if (isProfileExists(profileName, profileType)) {
				if(setProfile){
					lteCellSetWs = injectContentToWsCellObject15_5(lteCellSetWs,enbProfiles,cellProfiles,profileType);
				}
			}else
				report.report("Profile type : "+profileType+", is not exists in Netspan : "+this.getHostname(),Reporter.WARNING);
		} 
		return lteCellSetWs;
	}	
	
	private LteCellSetWs injectContentToWsCellObject15_5(LteCellSetWs lteCellSetWs,DefaultNetspanProfiles enbProfiles, CellNetspanProfiles cellProfiles, CellProfiles profileType) {
		switch(profileType){
		case Cell_Advanced_Profile:
			lteCellSetWs.setCellAdvancedConfigurationProfile(enbProfiles.getCellAdvanced());
			break;
		case Radio_Profile:
			lteCellSetWs.setRadioProfile(cellProfiles.getRadio());
			break;
		case Mobility_Profile:
			lteCellSetWs.setMobilityProfile(cellProfiles.getMobility());
			break;
		case Embms_Profile:
			lteCellSetWs.setEmbmsProfile(cellProfiles.getEmbms());
			break;
		case Traffic_Management_Profile:
			lteCellSetWs.setTrafficManagementProfile(cellProfiles.getTrafficManagement());
			break;
		case Call_Trace_Profile:
			lteCellSetWs.setCallTraceProfile(cellProfiles.getCallTrace());
			break;
		}
		return lteCellSetWs;
	}
	
	@Override
	public boolean isFileServerExists(String fileServerName) {
		try {
			FileServerResponse result = null;
			List<String> nameList = new ArrayList<String>();
			nameList.add(fileServerName);
			result = soapHelper_15_5.getServerSoap().fileServerGet(nameList, credentialsServer);
			if (result.getErrorCode() != Netspan.NBI_15_5.Server.ErrorCodes.OK) {
				report.report("fileServerGet via Netspan Failed: " + result.getErrorString(), Reporter.WARNING);
			} else
				return true;
		} catch (Exception e) {
			e.printStackTrace();
			report.report("fileServerGet via Netspan Failed due to: " + e.getMessage(), Reporter.WARNING);
			return false;
		} finally {
			soapHelper_15_5.endServerSoap();
		}
		return false;
	}

	@Override
	public boolean createFileServer(FileServer fileServer) {
		Netspan.NBI_15_5.Server.ObjectFactory objectFactory = new Netspan.NBI_15_5.Server.ObjectFactory();
		FileServerWs fileServerWs = new FileServerWs();
		fileServerWs.setName(fileServer.getName());
		fileServerWs.setServerIpAddress(fileServer.getIpAddress());
		fileServerWs.setUserName(fileServer.getUsername());
		fileServerWs.setPassword(fileServer.getPassword());
		fileServerWs.setProtocolType(objectFactory
				.createFileServerWsProtocolType(FileServerProtocolType.valueOf(fileServer.getProtocolType())));
		try {
			FileServerResponse result = null;
			result = soapHelper_15_5.getServerSoap().fileServerCreate(fileServerWs,
					credentialsServer);
			if (result.getErrorCode() != Netspan.NBI_15_5.Server.ErrorCodes.OK) {
				report.report("fileServerCreate via Netspan Failed : " + result.getErrorString(), Reporter.WARNING);
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			report.report("fileServerCreate via Netspan Failed due to: " + e.getMessage(), Reporter.WARNING);
			return false;
		} finally {
			soapHelper_15_5.endServerSoap();
		}
	}

	@Override
	public MultiCellParameters getMultiCellProfileObject(EnodeB node, String profileName) {
		List<String> name = new ArrayList<String>();
		name.add(profileName);
		CategoriesLte category = null;
		List<CategoriesLte> categoryList = new ArrayList<>();
		EnbDetailsGet nodeConf = getNodeConfig(node);
		try{
			 category = CategoriesLte.fromValue(nodeConf.getHardware());
		}catch(Exception e){
			report.report("Incompatible HardWare Type in inner Netspan method!",Reporter.WARNING);
			GeneralUtils.printToConsole("Error : "+e.getMessage()+", nodeConfig :"+nodeConf.getHardware());
			return null;
		}
		categoryList.add(category);
		report.report("trying to get MultiCell profile with the name : "+name+" ,and hardware : "+categoryList.toString());
		MultiCellProfileGetResult multiCellNetspan = soapHelper_15_5.getLteSoap().multiCellProfileGet(name, categoryList, credentialsLte);
		MultiCellParameters multiCellParam = getAdapterMultiCellObject(multiCellNetspan);
		return multiCellParam;
	}
	
	private MultiCellParameters getAdapterMultiCellObject(MultiCellProfileGetResult multiCellResult){
		MultiCellParameters mc = new MultiCellParameters();
		MultiCellProfileResult multiCellProfileResult = multiCellResult.getMultiCellProfileResult().get(0);
		if(multiCellProfileResult != null){
			EnbMultiCellProfile multiCellProfile = multiCellProfileResult.getMultiCellProfile();
			
			CarrierAggregationModes caMode = multiCellProfile.getCarrierAggMode().getValue();
			boolean caModeBol = false;
			if(caMode == CarrierAggregationModes.CONTIGUOUS){
				caModeBol = true;
			}
			mc.setCarrierAggMode(caModeBol);
		}
		return mc;
	}
	
	@Override
	public boolean updateSoftwareImage(EnodeBUpgradeImage upgradeImage) {
		Netspan.NBI_15_5.Software.SwImageWs softwareImage = createSoftwareImageObject(upgradeImage);
		Netspan.NBI_15_5.Software.SwFileInfoWs softwareFileInfo = createFileInfoObject(upgradeImage);
		List<Netspan.NBI_15_5.Software.SwFileInfoWs> fileList = softwareImage.getSoftwareFileInfo();
		fileList.add(softwareFileInfo);

		Netspan.NBI_15_5.Software.SwImageResponse result = soapHelper_15_5
				.getSoftwareSoap().softwareImageUpdate(softwareImage.getName(), softwareImage, credentialsSoftware);
		if (result.getErrorCode() != Netspan.NBI_15_5.Software.ErrorCodes.OK) {
			report.report("softwareImageUpdate via Netspan Failed : " + result.getErrorString(), Reporter.WARNING);
			soapHelper_15_5.endSoftwareSoap();
			return false;
		} else {
			soapHelper_15_5.endSoftwareSoap();
			return true;
		}
	}
	
	@Override
	public boolean createSoftwareImage(EnodeBUpgradeImage upgradeImage) {
		Netspan.NBI_15_5.Software.SwImageWs softwareImage = createSoftwareImageObject(upgradeImage);
		Netspan.NBI_15_5.Software.SwFileInfoWs softwareFileInfo = createFileInfoObject(upgradeImage);
		List<Netspan.NBI_15_5.Software.SwFileInfoWs> fileList = softwareImage.getSoftwareFileInfo();
		fileList.add(softwareFileInfo);

		Netspan.NBI_15_5.Software.SwImageResponse result = soapHelper_15_5
				.getSoftwareSoap().softwareImageCreate(softwareImage, credentialsSoftware);
		if (result.getErrorCode() != Netspan.NBI_15_5.Software.ErrorCodes.OK) {
			report.report("softwareServerCreate via Netspan Failed : " + result.getErrorString(), Reporter.WARNING);
			soapHelper_15_5.endSoftwareSoap();
			return false;
		} else {
			soapHelper_15_5.endSoftwareSoap();
			return true;
		}

	}
	
	private Netspan.NBI_15_5.Software.SwImageWs createSoftwareImageObject(EnodeBUpgradeImage upgradeImage) {

		Netspan.NBI_15_5.Software.SwImageWs softwareImage = new Netspan.NBI_15_5.Software.SwImageWs();

		if (upgradeImage.getName() != null) {
			softwareImage.setName(upgradeImage.getName());
		}
		if (upgradeImage.getHardwareCategory() != null) {
			softwareImage.setHardwareCategory(upgradeImage.getHardwareCategory());
		}
		if (upgradeImage.getUpgradeServerName() != null) {
			softwareImage.setSoftwareServer(upgradeImage.getUpgradeServerName());
		}

		return softwareImage;

	}

	private Netspan.NBI_15_5.Software.SwFileInfoWs createFileInfoObject(EnodeBUpgradeImage upgradeImage) {
		Netspan.NBI_15_5.Software.SwFileInfoWs softwareFileInfo = new Netspan.NBI_15_5.Software.SwFileInfoWs();
		Netspan.NBI_15_5.Software.ObjectFactory objectFactory = new Netspan.NBI_15_5.Software.ObjectFactory();

		if (upgradeImage.getImageType() != null) {
			softwareFileInfo.setImageType(objectFactory.createSwFileInfoWsImageType(ImageType.fromValue(upgradeImage.getImageType())));
		}else {
			softwareFileInfo.setImageType(objectFactory.createSwFileInfoWsImageType(ImageType.fromValue("LTE")));
		}

		if (upgradeImage.getBuildPath() != null) {
			ServerProtocolType protocolType = upgradeImage.getProtocolType();
			
			if(protocolType == null){
				String softwareImageName = upgradeImage.getName();
				EnodeBUpgradeImage currentsoftwareImage = getSoftwareImage(softwareImageName);
				String upgradeServerName = currentsoftwareImage.getUpgradeServerName();
				EnodeBUpgradeServer currentUpgradeServer = getSoftwareServer(upgradeServerName);
				protocolType = currentUpgradeServer.getUpgradeServerProtocolType();
			}
			softwareFileInfo.setFileNameWithPath(upgradeImage.getBuildPath());
		}
		
		if (upgradeImage.getVersion() != null) {
			softwareFileInfo.setVersion(upgradeImage.getVersion());
		}
		return softwareFileInfo;
	}
	
	@Override
	public EnodeBUpgradeImage getSoftwareImage(String upgradeImagename) {
		ArrayList<String> names = new ArrayList<>();
		names.add(upgradeImagename);
		Netspan.NBI_15_5.Software.SwImageResponse result = soapHelper_15_5
				.getSoftwareSoap().softwareImageGet(names, credentialsSoftware);
		if (result.getErrorCode() != Netspan.NBI_15_5.Software.ErrorCodes.OK) {
			String resultStr = result.getSoftwareImageResult().get(0).getResultString();
			if (resultStr.contains("Software Image Not Found")) {
				GeneralUtils.printToConsole("Software Image Not Found");
				soapHelper_15_5.endSoftwareSoap();
				return null;
			} else {
				report.report("softwareImageGet via Netspan Failed : " + result.getErrorString(), Reporter.WARNING);
				soapHelper_15_5.endSoftwareSoap();
				return null;
			}

		} else {
			if(result.getSoftwareImageResult().size() > 0){
				Netspan.NBI_15_5.Software.SwImageWs imageResult = result.getSoftwareImageResult().get(0).getSoftwareImage();
				EnodeBUpgradeImage image = new EnodeBUpgradeImage();
				image.setName(imageResult.getName());
				image.setHardwareCategory(imageResult.getHardwareCategory());
				image.setUpgradeServerName(imageResult.getSoftwareServer());
				image.setImageType(imageResult.getSoftwareFileInfo().get(0).getImageType().getValue().name());
				image.setBuildPath(imageResult.getSoftwareFileInfo().get(0).getFileNameWithPath());
				image.setVersion(imageResult.getSoftwareFileInfo().get(0).getVersion());
				if(imageResult.getSoftwareFileInfo().size() > 1){
					image.setSecondImageType(imageResult.getSoftwareFileInfo().get(1).getImageType().getValue().name());
					image.setSecondBuildPath(imageResult.getSoftwareFileInfo().get(1).getFileNameWithPath());
					image.setSecondVersion(imageResult.getSoftwareFileInfo().get(1).getVersion());
				}
				soapHelper_15_5.endSoftwareSoap();
				return image;
			}else{
				return null;
			}
		}
	}
	
	@Override
	public List<LteBackhaul> getBackhaulInterfaceStatus(EnodeB node) {
		LteBackhaulIfGetResult serverResult = soapHelper_15_5.getStatusSoap().enbBackhaulInterfaceStatusGet(node.getNetspanName(), credentialsStatus);
		List<LteBackhaul> res = new ArrayList<LteBackhaul>();
		
		if(serverResult == null) {
			report.report("BackhaulInterfaceStatus is null",Reporter.WARNING);
			return null;
		}
		
		for(LteBackhaulWs backaul : serverResult.getBackhaul()) {
			LteBackhaul curr = new LteBackhaul();
			curr.name = backaul.getName();
			curr.ethernetDuplex = DuplexType.fromValue(backaul.getEthernetDuplex().getValue().value());
			curr.ethernetRate = backaul.getEthernetRate().getValue();
		    curr.portType = backaul.getPortType().getValue();
		    curr.autoNegConfig = EnabledDisabledStates.fromValue(backaul.getAutoNegConfig().getValue().value());
		    curr.portStatus = backaul.getPortStatus().getValue();
		    curr.portSpeed = backaul.getPortSpeed().getValue();
		    curr.flowControlStatus = EnabledDisabledStates.fromValue(backaul.getFlowControlStatus().getValue().value());
			res.add(curr);
		}
		return res;
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
	
	@Override
	public boolean resetNode(String nodeName, RebootType rebootType) {
		List<String> nodeList = new ArrayList<String>();
		nodeList.add(nodeName);
		boolean rebooted = false;
		Netspan.NBI_15_5.Inventory.NodeActionResult result = null;
		if(rebootType == RebootType.WARM_REBOOT){
			result = soapHelper_15_5.getInventorySoap().nodeReset(nodeList, null, true, credentialsInventory);
		}else if(rebootType == RebootType.COLD_REBOOT){
			result = soapHelper_15_5.getInventorySoap().nodeResetForcedCold(nodeList, null, credentialsInventory);
		}else{
			rebooted = super.resetNode(nodeName, rebootType);
		}
		if(result != null){
			if (result.getErrorCode() != Netspan.NBI_15_5.Inventory.ErrorCodes.OK) {
				report.report("Failed to Reset Node via Netspan  : " + result.getErrorString(), Reporter.WARNING);
				rebooted = false;
			}else{
				rebooted = true;
			}
		}
		
		return rebooted;
	}
}
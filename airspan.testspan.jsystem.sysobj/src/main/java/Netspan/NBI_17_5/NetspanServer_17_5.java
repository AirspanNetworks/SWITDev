package Netspan.NBI_17_5;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import EnodeB.EnodeB;
import EnodeB.EnodeBChannelBandwidth;
import EnodeB.EnodeBUpgradeImage;
import EnodeB.EnodeBUpgradeServer;
import Netspan.CellProfiles;
import Netspan.EnbProfiles;
import Netspan.NBIVersion;
import Netspan.NetspanServer;
import Netspan.API.Enums.CategoriesLte;
import Netspan.API.Enums.ClockSources;
import Netspan.API.Enums.DuplexType;
import Netspan.API.Enums.EnabledStates;
import Netspan.API.Enums.EnbStates;
import Netspan.API.Enums.EnbTypes;
import Netspan.API.Enums.HandoverType;
import Netspan.API.Enums.HardwareCategory;
import Netspan.API.Enums.HoControlStateTypes;
import Netspan.API.Enums.ImageType;
import Netspan.API.Enums.NetworkElementStatus;
import Netspan.API.Enums.NodeManagementModes;
import Netspan.API.Enums.PrimaryClockSourceEnum;
import Netspan.API.Enums.SecurityProfileOptionalOrMandatory;
import Netspan.API.Enums.ServerProtocolType;
import Netspan.API.Enums.SnmpAgentVersion;
import Netspan.API.Enums.SonAnrStates;
import Netspan.API.Enums.X2ControlStateTypes;
import Netspan.API.Lte.AlarmInfo;
import Netspan.API.Lte.CarrierAggregationModes;
import Netspan.API.Lte.EnbCellProperties;
import Netspan.API.Lte.EventInfo;
import Netspan.API.Lte.LteBackhaul;
import Netspan.API.Lte.LteSonDynIcic;
import Netspan.API.Lte.NodeInfo;
import Netspan.API.Lte.PTPStatus;
import Netspan.API.Lte.PciStatusCell;
import Netspan.API.Lte.RFStatus;
import Netspan.API.Lte.RsiStatusCell;
import Netspan.API.Lte.SONStatus;
import Netspan.API.Lte.IRetunTypes.ILteEnbDetailsGet;
import Netspan.API.Lte.IRetunTypes.ILteEnbDetailsSet;
import Netspan.API.Software.RequestType;
import Netspan.API.Software.SoftwareStatus;
import Netspan.DataObjects.NeighborData;
import Netspan.NBI_17_5.SoapHelper;
import Netspan.NBI_17_5.FaultManagement.Alarm;
import Netspan.NBI_17_5.FaultManagement.AlarmActionResult;
import Netspan.NBI_17_5.FaultManagement.AlarmResultList;
import Netspan.NBI_17_5.FaultManagement.Event;
import Netspan.NBI_17_5.FaultManagement.EventResultList;
import Netspan.NBI_17_5.Inventory.DiscoveryTaskActionResult;
import Netspan.NBI_17_5.Inventory.NodeActionResult;
import Netspan.NBI_17_5.Inventory.NodeDetailGetResult;
import Netspan.NBI_17_5.Inventory.NodeManagementMode;
import Netspan.NBI_17_5.Inventory.NodeProvisioningGetResult;
import Netspan.NBI_17_5.Inventory.NodeProvisioningGetWs;
import Netspan.NBI_17_5.Inventory.WsResponse;
import Netspan.NBI_17_5.Lte.AddlSpectrumEmissions;
import Netspan.NBI_17_5.Lte.AirSonWs;
import Netspan.NBI_17_5.Lte.AnrFreq;
import Netspan.NBI_17_5.Lte.AnrFreqListContainer;
import Netspan.NBI_17_5.Lte.AuPnpDetailWs;
import Netspan.NBI_17_5.Lte.CellToUseValues;
import Netspan.NBI_17_5.Lte.ClockContainer;
import Netspan.NBI_17_5.Lte.ClockSource;
import Netspan.NBI_17_5.Lte.Duplex;
import Netspan.NBI_17_5.Lte.DuplexModeTypes;
import Netspan.NBI_17_5.Lte.EaidsParams;
import Netspan.NBI_17_5.Lte.Enb3RdParty;
import Netspan.NBI_17_5.Lte.EnbAdvancedProfile;
import Netspan.NBI_17_5.Lte.EnbCellAdvancedProfile;
import Netspan.NBI_17_5.Lte.EnbDetailsGet;
import Netspan.NBI_17_5.Lte.EnbDetailsGetPnp;
import Netspan.NBI_17_5.Lte.EnbManagementProfile;
import Netspan.NBI_17_5.Lte.EnbMobilityProfile;
import Netspan.NBI_17_5.Lte.EnbMultiCellProfile;
import Netspan.NBI_17_5.Lte.EnbNeighbourProfile;
import Netspan.NBI_17_5.Lte.EnbNetworkProfile;
import Netspan.NBI_17_5.Lte.EnbPnpConfig;
import Netspan.NBI_17_5.Lte.EnbRadioProfile;
import Netspan.NBI_17_5.Lte.EnbSecurityProfile;
import Netspan.NBI_17_5.Lte.EnbSonProfile;
import Netspan.NBI_17_5.Lte.EnbSyncProfile;
import Netspan.NBI_17_5.Lte.EnbTypesWs;
import Netspan.NBI_17_5.Lte.EtwsModes;
import Netspan.NBI_17_5.Lte.EtwsWs;
import Netspan.NBI_17_5.Lte.Lte3RdPartyGetResult;
import Netspan.NBI_17_5.Lte.Lte3RdPartyResponse;
import Netspan.NBI_17_5.Lte.LteAddNeighbourForCellWs;
import Netspan.NBI_17_5.Lte.LteAddNeighbourWs;
import Netspan.NBI_17_5.Lte.LteCSonEntryWs;
import Netspan.NBI_17_5.Lte.LteCellGetWs;
import Netspan.NBI_17_5.Lte.LteCellSetWs;
import Netspan.NBI_17_5.Lte.LteEnbConfigGetResult;
import Netspan.NBI_17_5.Lte.LteEnbDetailsSetWs;
import Netspan.NBI_17_5.Lte.LteEnbDetailsSetWsPnp;
import Netspan.NBI_17_5.Lte.LteNeighbourResponse;
import Netspan.NBI_17_5.Lte.LteNetworkProfileGetResult;
import Netspan.NBI_17_5.Lte.LtePlmnEntryWs;
import Netspan.NBI_17_5.Lte.LtePnpConfigGetResult;
import Netspan.NBI_17_5.Lte.LteRadioProfileGetResult;
import Netspan.NBI_17_5.Lte.LteS1EntryWs;
import Netspan.NBI_17_5.Lte.LteSecurityProfileGetResult;
import Netspan.NBI_17_5.Lte.LteSonCSonWs;
import Netspan.NBI_17_5.Lte.LteSyncProfileGetResult;
import Netspan.NBI_17_5.Lte.LteSyncProfileResult;
import Netspan.NBI_17_5.Lte.LteSystemDefaultProfile;
import Netspan.NBI_17_5.Lte.MaintenanceWindowConfigurationWs;
import Netspan.NBI_17_5.Lte.MobilityConnectedModeFreq;
import Netspan.NBI_17_5.Lte.MobilityConnectedModeInterDefaultContainer;
import Netspan.NBI_17_5.Lte.MobilityConnectedModeIntraFrequencyListContainer;
import Netspan.NBI_17_5.Lte.MobilityConnectedModeThresholdBased;
import Netspan.NBI_17_5.Lte.MobilityConnectedModeThresholdBasedListContainer;
import Netspan.NBI_17_5.Lte.MultiCellProfileGetResult;
import Netspan.NBI_17_5.Lte.MultiCellProfileResult;
import Netspan.NBI_17_5.Lte.NameResult;
import Netspan.NBI_17_5.Lte.NeighbourHomeEnbBandConfig;
import Netspan.NBI_17_5.Lte.NeighbourHomeEnbBandListContainer;
import Netspan.NBI_17_5.Lte.NeighbourHomeEnbDefaultConfig;
import Netspan.NBI_17_5.Lte.NeighbourHomeEnbEarfcnConfig;
import Netspan.NBI_17_5.Lte.NeighbourHomeEnbEarfcnListContainer;
import Netspan.NBI_17_5.Lte.NeighbourHomeEnbPciWs;
import Netspan.NBI_17_5.Lte.NeighbourNrtBandConfig;
import Netspan.NBI_17_5.Lte.NeighbourNrtBandListContainer;
import Netspan.NBI_17_5.Lte.NeighbourNrtDefaultConfig;
import Netspan.NBI_17_5.Lte.NeighbourNrtEarfcnConfig;
import Netspan.NBI_17_5.Lte.NeighbourNrtEarfcnListContainer;
import Netspan.NBI_17_5.Lte.NeighbourNrtPciWs;
import Netspan.NBI_17_5.Lte.NlmIntraFreqScan;
import Netspan.NBI_17_5.Lte.NlmIntraFreqScanListContainer;
import Netspan.NBI_17_5.Lte.NodeListResult;
import Netspan.NBI_17_5.Lte.NodeProperty;
import Netspan.NBI_17_5.Lte.NodeResult;
import Netspan.NBI_17_5.Lte.NodeSimple;
import Netspan.NBI_17_5.Lte.ObjectFactory;
import Netspan.NBI_17_5.Lte.PciConflictHandlingValues;
import Netspan.NBI_17_5.Lte.PciRange;
import Netspan.NBI_17_5.Lte.PciRangeListContainer;
import Netspan.NBI_17_5.Lte.PlmnListContainer;
import Netspan.NBI_17_5.Lte.PnpConfigCreate;
import Netspan.NBI_17_5.Lte.PnpDetailWs;
import Netspan.NBI_17_5.Lte.PnpHardwareTypes;
import Netspan.NBI_17_5.Lte.ProfileResponse;
import Netspan.NBI_17_5.Lte.RelayEnodeBConfigGetResult;
import Netspan.NBI_17_5.Lte.ResourceManagementTypes;
import Netspan.NBI_17_5.Lte.RfSubframe;
import Netspan.NBI_17_5.Lte.RsiRange;
import Netspan.NBI_17_5.Lte.RsiRangeListContainer;
import Netspan.NBI_17_5.Lte.S1ListContainer;
import Netspan.NBI_17_5.Lte.SfrThresholdTypes;
import Netspan.NBI_17_5.Lte.SnmpDetailSetWs;
import Netspan.NBI_17_5.Lte.SnmpDetailWs;
import Netspan.NBI_17_5.Lte.TddFrameConfigurationsSupported;
import Netspan.NBI_17_5.Lte.TimeZones;
import Netspan.NBI_17_5.Server.FileServerProtocolType;
import Netspan.NBI_17_5.Server.FileServerResponse;
import Netspan.NBI_17_5.Server.FileServerWs;
import Netspan.NBI_17_5.Server.NmsInfoResponse;
import Netspan.NBI_17_5.Software.NodeSoftwareStatus;
import Netspan.NBI_17_5.Software.NodeSoftwareStatusResult;
import Netspan.NBI_17_5.Software.SoftwareStatusGetWs;
import Netspan.NBI_17_5.Software.SwConfigSetWs;
import Netspan.NBI_17_5.Software.SwFileInfoWs;
import Netspan.NBI_17_5.Software.SwImageWs;
import Netspan.NBI_17_5.Software.SwServerWs;
import Netspan.NBI_17_5.Status.LteAnrStatusWs;
import Netspan.NBI_17_5.Status.LteBackhaulIfGetResult;
import Netspan.NBI_17_5.Status.LteBackhaulWs;
import Netspan.NBI_17_5.Status.LteIpThroughputCellWs;
import Netspan.NBI_17_5.Status.LteIpThroughputGetResult;
import Netspan.NBI_17_5.Status.LteIpThroughputQciWs;
import Netspan.NBI_17_5.Status.LteNetworkElementGetResult;
import Netspan.NBI_17_5.Status.LteNetworkElementStatusWs;
import Netspan.NBI_17_5.Status.LtePciStatusWs;
import Netspan.NBI_17_5.Status.LteRfGetResult;
import Netspan.NBI_17_5.Status.LteRfStatusWs;
import Netspan.NBI_17_5.Status.LteRsiStatusWs;
import Netspan.NBI_17_5.Status.LteSonAnrGetResult;
import Netspan.NBI_17_5.Status.LteSonPciGetResult;
import Netspan.NBI_17_5.Status.LteSonRsiGetResult;
import Netspan.NBI_17_5.Status.LteUeGetResult;
import Netspan.NBI_17_5.Status.NodeGpsGetResult;
import Netspan.NBI_17_5.Status.NodePtpGetResult;
import Netspan.NBI_17_5.Status.NodeSoftwareGetResult;
import Netspan.Profiles.CellAdvancedParameters;
import Netspan.Profiles.CellBarringPolicyParameters;
import Netspan.Profiles.ConnectedUETrafficDirection;
import Netspan.Profiles.EnodeBAdvancedParameters;
import Netspan.Profiles.INetspanProfile;
import Netspan.Profiles.ManagementParameters;
import Netspan.Profiles.MobilityParameters;
import Netspan.Profiles.MultiCellParameters;
import Netspan.Profiles.NeighbourManagementParameters;
import Netspan.Profiles.NeighbourManagementParameters.HomeEnbPci;
import Netspan.Profiles.NeighbourManagementParameters.NrtPci;
import Netspan.Profiles.NetworkParameters;
import Netspan.Profiles.NetworkParameters.Plmn;
import Netspan.Profiles.RadioParameters;
import Netspan.Profiles.SecurityParameters;
import Netspan.Profiles.SonParameters;
import Netspan.Profiles.SyncParameters;
import Netspan.Profiles.SystemDefaultParameters;
import Utils.CellNetspanProfiles;
import Utils.DefaultNetspanProfiles;
import Utils.FileServer;
import Utils.GeneralUtils;
import Utils.GeneralUtils.CellToUse;
import Utils.GeneralUtils.RebootType;
import Utils.GeneralUtils.RebootTypesNetspan;
import Utils.GeneralUtils.RelayScanType;
import Utils.Pair;
import Utils.Triple;
import jsystem.framework.report.Reporter;

public class NetspanServer_17_5 extends NetspanServer implements Netspan_17_5_abilities{
	
    public SoapHelper soapHelper_17_5;
    private static final String USERNAME = "wsadmin";
	private static final String PASSWORD = "password";
	private static final Netspan.NBI_17_5.Lte.Credentials credentialsLte = new Netspan.NBI_17_5.Lte.Credentials();
	private static final Netspan.NBI_17_5.Inventory.Credentials credentialsInventory = new Netspan.NBI_17_5.Inventory.Credentials();
	private static final Netspan.NBI_17_5.FaultManagement.Credentials credentialsFaultManagement = new Netspan.NBI_17_5.FaultManagement.Credentials();
	private static final Netspan.NBI_17_5.Statistics.Credentials credentialsStatistics = new Netspan.NBI_17_5.Statistics.Credentials();
	private static final Netspan.NBI_17_5.Status.Credentials credentialsStatus = new Netspan.NBI_17_5.Status.Credentials();
	private static final Netspan.NBI_17_5.Server.Credentials credentialsServer = new Netspan.NBI_17_5.Server.Credentials();
	private static final Netspan.NBI_17_5.Backhaul.Credentials credentialsBackhaul = new Netspan.NBI_17_5.Backhaul.Credentials();
	private static final Netspan.NBI_17_5.Software.Credentials credentialsSoftware = new Netspan.NBI_17_5.Software.Credentials();

	@Override
	public void init() throws Exception {
		if (NBI_VERSION == null) {
			NBI_VERSION = NBIVersion.NBI_17_5;
		}
		super.init();
	}
	
	/**
     * Init soap  helper objects.
     *
     * @throws Exception the exception
     */
    @Override
    public void initSoapHelper() throws Exception{
    	this.soapHelper_17_5 = new SoapHelper(getHostname());
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
    
    /**
     * Populate node names.
     *
     * @throws Exception the exception
     */
    protected void populateNodeNames() {
        GeneralUtils.printToConsole("Stating to map eNodeB nodeName to eNodeB nodeID");
        nodeNames = new Hashtable<String, String>();
        Netspan.NBI_17_5.Inventory.NodeListResult result = soapHelper_17_5
            .getInventorySoap().nodeList(null,credentialsInventory);

        for (int nodeIndex = 0; nodeIndex < result.getNode().size(); nodeIndex++) {
            Netspan.NBI_17_5.Inventory.NodeSimple node = result.getNode().get(nodeIndex);
            nodeNames.put(node.getNodeId(), node.getName());
        }
        soapHelper_17_5.endInventorySoap();
    }

    @Override
    public RadioParameters radioProfileGet(EnodeB enb) throws Exception {
        String enbRadioProfileName = this.getCurrentRadioProfileName(enb);
        if (enbRadioProfileName == null) {
            report.report("Could not get Radio Profile name from Netspan - Going for SNMP");
            throw new Exception();
        }
        LteRadioProfileGetResult netspanResult = null;
        List<java.lang.String> enbList = new ArrayList<java.lang.String>();
        enbList.add(enbRadioProfileName);
        RadioParameters radioParam = null;
        netspanResult = soapHelper_17_5.getLteSoap().radioProfileGet(enbList,null,null,
            credentialsLte);
        radioParam = parseRadioProfile(netspanResult);
        soapHelper_17_5.endLteSoap();
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
        radioParams.setBand(netspanResult.getRadioProfileResult().get(0).getRadioProfile().getBand().getValue());
        GeneralUtils.printToConsole("band");
        radioParams.setDownLinkFrequency(
            netspanResult.getRadioProfileResult().get(0).getRadioProfile().getDownlinkFreqKHz().getValue());
        GeneralUtils.printToConsole("DownLink Freq");
        radioParams.setMfbi(netspanResult.getRadioProfileResult().get(0).getRadioProfile().getMfbiEnabled().getValue());
        GeneralUtils.printToConsole("MFBI");
        radioParams.setTxpower(netspanResult.getRadioProfileResult().get(0).getRadioProfile().getTxPower().getValue());
        GeneralUtils.printToConsole("tx power");
        radioParams.setUpLinkFrequency(
            netspanResult.getRadioProfileResult().get(0).getRadioProfile().getUplinkFreqKHz().getValue());
        GeneralUtils.printToConsole("UpLink Freq");
        radioParams.setEarfcn(netspanResult.getRadioProfileResult().get(0).getRadioProfile().getEarfcn().getValue());
        GeneralUtils.printToConsole("Earfcn");
        return radioParams;
    }

    private String getCurrentRadioProfileName(EnodeB enb, int cellNumber) {
        EnbDetailsGet nodeConfig = getNodeConfig(enb);
        if (nodeConfig != null) {
            for (LteCellGetWs cell : nodeConfig.getLteCell()) {
                int tempCellNumber = Integer.valueOf(cell.getCellNumber().getValue());
                if (tempCellNumber == cellNumber) {
                    GeneralUtils.printToConsole("Radio Profile From Netspan: " + cell.getRadioProfile() + " for cell : " + cell.getCellNumber().getValue());
                    return cell.getRadioProfile();
                }
            }
        }
        return null;
    }

    private EnbDetailsGet convertPnPToEnb(LtePnpConfigGetResult result) {
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

        if (isPnpNode(enb.getNetspanName())) {
            LtePnpConfigGetResult result = null;
            try {
                result = soapHelper_17_5.getLteSoap().pnpConfigGet(nodeListName,null, credentialsLte);
            } catch (Exception e) {
                report.report("enbConfigGet via Netspan Failed due to: " + e.getMessage(), Reporter.WARNING);
                e.printStackTrace();
                return null;
            } finally {
                soapHelper_17_5.endLteSoap();
            }
            if (result.getErrorCode() != Netspan.NBI_17_5.Lte.ErrorCodes.OK) {
                GeneralUtils.printToConsole("enbConfigGet via Netspan Failed : " + result.getErrorString());
                return null;
            }
            GeneralUtils.printToConsole("enbConfigGet via Netspan for eNodeB " + enb.getNetspanName() + " succeeded");
            return convertPnPToEnb(result);
        }

        LteEnbConfigGetResult result = null;
        try {
            result = soapHelper_17_5.getLteSoap().enbConfigGet(nodeListName,null, credentialsLte);

        } catch (Exception e) {
            report.report("enbConfigGet via Netspan Failed due to: " + e.getMessage(), Reporter.WARNING);
            e.printStackTrace();
            return null;
        } finally {
            soapHelper_17_5.endLteSoap();
        }
        if (result.getErrorCode() != Netspan.NBI_17_5.Lte.ErrorCodes.OK) {
            GeneralUtils.printToConsole("enbConfigGet via Netspan Failed : " + result.getErrorString());
            return null;
        }
        GeneralUtils.printToConsole("enbConfigGet via Netspan for eNodeB " + enb.getNetspanName() + " succeeded");
        return result.getNodeResult().get(FIRST_ELEMENT).getEnbConfig();
    }

    @Override
    public String getNetspanVersion() {
        if (netspanVersion == null) {
            NmsInfoResponse nms;
            try {
                nms = soapHelper_17_5.getServerSoap().nmsInfoGet(credentialsServer);
                if (nms.getErrorCode() != Netspan.NBI_17_5.Server.ErrorCodes.OK) {
                    report.report("nmsInfoGet via Netspan Failed : " + nms.getErrorString(), Reporter.WARNING);
                    setNetspanVersion("Unknown");
                    return null;
                }
                setNetspanVersion(nms.getNMSSoftwareVersion());
            } catch (Exception e) {
                report.report("nmsInfoGet via Netspan Failed due to: " + e.getMessage(), Reporter.WARNING);
                e.printStackTrace();
                setNetspanVersion("Unknown");
                return null;
            } finally {
                soapHelper_17_5.endServerSoap();
            }
        }
        return netspanVersion;
    }

    @Override
    public boolean verifyNeighbor(EnodeB enodeB, EnodeB neighbor, HoControlStateTypes hoControlStatus,
                                  X2ControlStateTypes x2ControlStatus, HandoverType HandoverType, boolean isStaticNeighbor,
                                  String qOffsetRange) {

        boolean wasAdded = true;
        boolean wasFound = false;
        List<LteAnrStatusWs> allNeighbors = getNeighbourList(enodeB);
        report.report("-----------------------------------------------");

        if (allNeighbors.isEmpty()) {
            report.report("The neighbor list is empty");
            return false;
        }

        for (LteAnrStatusWs nbr : allNeighbors) {
            if (nbr.getName().equals(neighbor.getNetspanName())) {
                wasFound = true;
                report.report("Neighbor: " + neighbor.getNetspanName() + " was found in EnodeB: "
                    + enodeB.getNetspanName() + " list");

                if (null != hoControlStatus) {
                    if (nbr.getHoControlStatus().getValue() != hoControlStatus) {
                        report.report("Ho Control Type is " + nbr.getHoControlStatus().getValue() + " and not "
                            + hoControlStatus.value() + " as expected", Reporter.WARNING);
                        wasAdded = false;
                    } else {
                        report.report("Ho Control Type is " + nbr.getHoControlStatus().getValue() + " as expected");
                    }
                }

                if (null != x2ControlStatus) {
                    if (nbr.getX2ControlStatus().getValue() != x2ControlStatus) {
                        report.report("X2 Control Type is " + nbr.getX2ControlStatus().getValue() + " and not "
                            + x2ControlStatus.value() + " as expected", Reporter.WARNING);
                        wasAdded = false;
                    } else {
                        report.report("X2 Control Type is " + nbr.getX2ControlStatus().getValue() + " as expected");
                    }
                }

                if (null != HandoverType) {
                    if (nbr.getHandoverType().getValue() != HandoverType) {
                        report.report("HandOver Type is " + nbr.getHandoverType().getValue() + " and not "
                            + HandoverType.value() + " as expected", Reporter.WARNING);
                        wasAdded = false;
                    } else {
                        report.report("HandOver Type is " + nbr.getHandoverType().getValue() + " as expected");
                    }
                }

                if (nbr.getIsStaticNeighbour() != null) {
                    if (nbr.getIsStaticNeighbour().getValue() != isStaticNeighbor) {
                        report.report("isStatic is set to " + nbr.getIsStaticNeighbour().getValue() + " and not to "
                            + isStaticNeighbor + " as expected", Reporter.WARNING);
                        wasAdded = false;
                    } else {
                        report.report("isStatic is set to " + nbr.getIsStaticNeighbour().getValue() + " as expected");
                    }
                } else {
                    GeneralUtils.printToConsole("nbr.getIsStaticNeighbour is null!");
                }
                if (qOffsetRange != null) {
                    if (!nbr.getQOffset().getValue().equals(qOffsetRange)) {
                        report.report("qOffsetRange is " + nbr.getQOffset().getValue() + " and not "
                            + qOffsetRange + " as expected", Reporter.WARNING);
                        wasAdded = false;
                    } else {
                        report.report("qOffsetRange is " + nbr.getQOffset().getValue() + " as expected");
                    }
                }


                break;
            }
        }
        if (!wasFound) {
            report.report("Couldn't find the neighbor " + neighbor.getNetspanName() + " in the EnodeB: "
                + enodeB.getNetspanName() + " list", Reporter.WARNING);
            wasAdded = false;
        }
        return wasAdded;
    }

    @Override
    public boolean verifyNeighbor(EnodeB enodeB, EnodeB neighbor) {
        List<LteAnrStatusWs> neighbourStatusAnrState = getNeighbourList(enodeB);
        if (neighbourStatusAnrState.isEmpty()) {
            report.report("The neighbor list is empty");
            return false;
        }
        try {
            for (LteAnrStatusWs nbr : neighbourStatusAnrState) {
                String nbrName = nbr.getName();
                String methodParamNeighbor = neighbor.getNetspanName();
                if (nbrName.equals(methodParamNeighbor)) {
                    report.report("Neighbor: " + neighbor.getNetspanName() + " was found in EnodeB: "
                        + enodeB.getNetspanName() + " list");
                    return true;
                }
            }
            report.report("Couldn't find the neighbor " + neighbor.getNetspanName() + " in the EnodeB: "
                + enodeB.getNetspanName() + " list", Reporter.WARNING);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;

    }

    @Override
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

    @Override
    public boolean verifyNoNeighbors(EnodeB enodeB) {
        List<LteAnrStatusWs> allNeighbors = getNeighbourList(enodeB);
        if (allNeighbors != null) {
            return allNeighbors.isEmpty();
        }
        return false;
    }

    /**
     * Delete neighbor.
     *
     * @param enodeB   the EnodeB
     * @param neighbor the neighbor
     * @return true, if successful
     */
    @Override
    public boolean deleteNeighbor(EnodeB enodeB, EnodeB neighbor) {
        try {
            Netspan.NBI_17_5.Lte.LteNeighbourResponse result = soapHelper_17_5
                .getLteSoap().lteNeighbourDelete(enodeB.getNetspanName(), neighbor.getNetspanName(),
                    enodeB.getCellContextID(), neighbor.getCellContextID(), credentialsLte);
            if (result.getErrorCode() != Netspan.NBI_17_5.Lte.ErrorCodes.OK) {
                report.report("lteNeighbourDelete via Netspan Failed : " + result.getErrorString(), Reporter.WARNING);
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            report.report("lteNeighbourDelete via Netspan Failed due to: " + e.getMessage(), Reporter.WARNING);
            return false;
        } finally {
            soapHelper_17_5.endLteSoap();
        }
    }

    @Override
    public boolean deleteAllNeighbors(EnodeB enodeB) {
        try {
            if (verifyNoNeighbors(enodeB))
                return true;
            LteNeighbourResponse result = soapHelper_17_5.getLteSoap()
                .lteNeighbourDeleteAll(enodeB.getNetspanName(), credentialsLte);

            if (result.getErrorCode() != Netspan.NBI_17_5.Lte.ErrorCodes.OK) {
                if (result.getErrorString().toLowerCase().contains("no neighbours to delete")) {
                    return true;
                }
                report.report("lteNeighbourDeleteAll via Netspan Failed : " + result.getErrorString(),
                    Reporter.WARNING);
                return false;
            } else {
                GeneralUtils.printToConsole(
                    "lteNeighbourDeleteAll via Netspan for eNodeB " + enodeB.getNetspanName() + " succeeded");
                return true;
            }
        } catch (Exception e) {
            report.report("lteNeighbourDeleteAll via Netspan Failed due to: " + e.getMessage(), Reporter.WARNING);
            e.printStackTrace();
            return false;
        } finally {
            soapHelper_17_5.endLteSoap();
        }
    }

    @Override
    public boolean deleteEnbProfile(String profileName, EnbProfiles profileType) {
        ArrayList<String> nameList = new ArrayList<>();
        nameList.add(profileName);
        ProfileResponse response = null;
        try {
            switch (profileType) {
                case EnodeB_Advanced_Profile:
                    response = soapHelper_17_5.getLteSoap().cellAdvancedConfigProfileDelete(nameList,
                        credentialsLte);
                    break;
                case Cell_Advanced_Profile:
                    response = soapHelper_17_5.getLteSoap().cellAdvancedConfigProfileDelete(nameList,
                        credentialsLte);
                    break;
                case Management_Profile:
                    response = soapHelper_17_5.getLteSoap().managementProfileDelete(nameList,
                        credentialsLte);
                    break;
                case Mobility_Profile:
                    response = soapHelper_17_5.getLteSoap().mobilityProfileDelete(nameList,
                        credentialsLte);
                    break;
                case Network_Profile:
                    response = soapHelper_17_5.getLteSoap().networkProfileDelete(nameList,
                        credentialsLte);
                    break;
                case Radio_Profile:
                    response = soapHelper_17_5.getLteSoap().radioProfileDelete(nameList, credentialsLte);
                    break;
                case Security_Profile:
                    response = soapHelper_17_5.getLteSoap().securityProfileDelete(nameList,
                        credentialsLte);
                    break;
                case Son_Profile:
                    response = soapHelper_17_5.getLteSoap().sonProfileDelete(nameList, credentialsLte);
                    break;
                case Sync_Profile:
                    response = soapHelper_17_5.getLteSoap().syncProfileDelete(nameList, credentialsLte);
                    break;
                case MultiCell_Profile:
                    response = soapHelper_17_5.getLteSoap().multiCellProfileDelete(nameList,
                        credentialsLte);
                    break;
                case Neighbour_Management_Profile:
                	response = soapHelper_17_5.getLteSoap().neighbourProfileDelete(nameList, credentialsLte);
                	break;
                default:
                    report.report("deleteEnbProfile get EnbProfiles type not exist: " + profileType, Reporter.WARNING);
                    return false;
            }
            if (response.getErrorCode() != Netspan.NBI_17_5.Lte.ErrorCodes.OK) {
                report.report("delete " + profileType + " profile " + profileName + " via Netspan Failed : "
                    + response.getProfileResult().get(0).getProfileResultString() + " - "
                    + response.getErrorString(), Reporter.WARNING);
                return false;
            } else
                return true;

        } catch (Exception e) {
            e.printStackTrace();
            report.report("deleteEnbProfile Failed, due to: " + e.getMessage(), Reporter.WARNING);
            return false;
        } finally {
            soapHelper_17_5.endLteSoap();
        }
    }

    /**
     * @author Gabriel Grunwald Name: isProfileExists Function: checks if a
     * profile exists Restrictions: only works for these profiles: -
     * Sync - Management - SON - Network - Security
     */
    @Override
    public boolean isProfileExists(String name, EnbProfiles profileType) {
        ArrayList<String> nameList = new ArrayList<String>();
        nameList.add(name);
        Netspan.NBI_17_5.Lte.WsResponse response = null;
        try {
            switch (profileType) {
                case System_Default_Profile:
                    response = soapHelper_17_5.getLteSoap()
                        .systemDefaultProfileGet(nameList,null,null, credentialsLte);
                    break;
                case EnodeB_Advanced_Profile:
                    response = soapHelper_17_5.getLteSoap()
                        .enbAdvancedConfigProfileGet(nameList,null,null, credentialsLte);
                    break;
                case Cell_Advanced_Profile:
                    response = soapHelper_17_5.getLteSoap()
                        .cellAdvancedConfigProfileGet(nameList,null,null, credentialsLte);
                    break;
                case Management_Profile:
                    response = soapHelper_17_5.getLteSoap().managementProfileGet(nameList,null,null,
                        credentialsLte);
                    break;
                case Mobility_Profile:
                    response = soapHelper_17_5.getLteSoap().mobilityProfileGet(nameList,null,null,
                        credentialsLte);
                    break;
                case Network_Profile:
                    response = soapHelper_17_5.getLteSoap().networkProfileGet(nameList,null,null,
                        credentialsLte);
                    break;
                case Radio_Profile:
                    response = soapHelper_17_5.getLteSoap().radioProfileGet(nameList,null,null,
                        credentialsLte);
                    break;
                case Security_Profile:
                    response = soapHelper_17_5.getLteSoap().securityProfileGet(nameList,null,null,
                        credentialsLte);
                    break;
                case Son_Profile:
                    response = soapHelper_17_5.getLteSoap().sonProfileGet(nameList,null,null,
                        credentialsLte);
                    break;
                case Sync_Profile:
                    response = soapHelper_17_5.getLteSoap().syncProfileGet(nameList,null,null,
                        credentialsLte);
                    break;
                case MultiCell_Profile:
                    response = soapHelper_17_5.getLteSoap().multiCellProfileGet(nameList,null,null,
                        credentialsLte);
                    break;
                case Neighbour_Management_Profile:
                	response = soapHelper_17_5.getLteSoap().neighbourProfileGet(nameList, null,null,
    						credentialsLte);
                	break;
                default:
                    report.report("isProfileExists get error EnbProfiles type: " + profileType, Reporter.WARNING);
            }
            return response.getErrorCode() == Netspan.NBI_17_5.Lte.ErrorCodes.OK;

        } catch (Exception e) {
            e.printStackTrace();
            report.report(profileType.value() + "ProfileGet Failed, due to: " + e.getMessage(), Reporter.WARNING);
            return false;
        } finally {
            soapHelper_17_5.endLteSoap();
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
        ObjectFactory objectFactory = new ObjectFactory();
        LteEnbDetailsSetWs enbConfigSet = new LteEnbDetailsSetWs();
        LteCellSetWs lteCellSet = new LteCellSetWs();
        lteCellSet.setCellNumber(objectFactory.createLteCellGetWsCellNumber(String.valueOf(cellid)));

        enbConfigSet = addProfile(node, cellid, profileType, profileName, enbConfigSet);
        if (enbConfigSet == null) {
            return false;
        }
        return enbSetContent(node,null, enbConfigSet, null);
    }

    /**
     * set Profiles with a map for each profile with his cell number.
     */
    @Override
    public boolean setProfile(EnodeB node, HashMap<INetspanProfile, Integer> profilesPerCell) {
        LteEnbDetailsSetWs enbConfigSet = new LteEnbDetailsSetWs();
        int cell;
        for (INetspanProfile profile : profilesPerCell.keySet()) {
            cell = profilesPerCell.get(profile);
            report.report("trying to set profile " + profile.getProfileName() + " in node : " + node.getNetspanName()
                + " to cell : " + cell);
            enbConfigSet = addProfile(node, cell, profile.getType(), profile.getProfileName(), enbConfigSet);
        }
        return enbSetContent(node,null, enbConfigSet, null);
    }

    @Override
    public boolean changeCellToUse(EnodeB node, CellToUse cellToUse) {
        LteEnbDetailsSetWs enbConfigSet = new LteEnbDetailsSetWs();
        ObjectFactory factoryDetails = new ObjectFactory();
        CellToUseValues cellToUseParam = null;
        if (cellToUse == CellToUse.MULTI_CELL){
            cellToUseParam = CellToUseValues.MULTI_CELL;
        }
        else{
            cellToUseParam = CellToUseValues.LITE_COMP;
        }
        enbConfigSet.setCellToUse(factoryDetails.createLteEnbDetailsSetWsPnpParamsCellToUse(cellToUseParam));
        return setNodeConfig(node, enbConfigSet);
    }
    
    private LteEnbDetailsSetWs addProfile(EnodeB node, int cellNumber, EnbProfiles profileType, String profileName,
                                          LteEnbDetailsSetWs enbConfigSet) {
    	switch (profileType) {
	        case System_Default_Profile:
	            enbConfigSet.setSystemDefaultProfile(profileName);
	            break;
	        case Management_Profile:
	            enbConfigSet.setManagementProfile(profileName);
	            break;
	
	        case Mobility_Profile:
	            if (cellNumber < 1) {
	                report.report("Cell ID need to be greater than 0", Reporter.WARNING);
	                return null;
	            }
	            getActiveIfActiveCell(enbConfigSet, cellNumber).setMobilityProfile(profileName);
	            break;
	
	        case Radio_Profile:
	            if (cellNumber < 1) {
	                report.report("Cell ID need to be greater than 0", Reporter.WARNING);
	                return null;
	            }
	            getActiveIfActiveCell(enbConfigSet, cellNumber).setRadioProfile(profileName);
	            break;
	
	        case Network_Profile:
	            enbConfigSet.setNetworkProfile(profileName);
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
	        case Cell_Advanced_Profile:
	            if (cellNumber < 1) {
	                report.report("Cell ID need to be greater than 0", Reporter.WARNING);
	                return null;
	            }
	            getActiveIfActiveCell(enbConfigSet, cellNumber).setCellAdvancedConfigurationProfile(profileName);
	            break;
	        case MultiCell_Profile:
	            enbConfigSet.setMultiCellProfile(profileName);
	            break;
	        case eMBMS_Profile:
	            if (cellNumber < 1) {
	                report.report("Cell ID need to be greater than 0", Reporter.WARNING);
	                return null;
	            }
	            getActiveIfActiveCell(enbConfigSet, cellNumber).setEmbmsProfile(profileName);
	            break;
	        case Traffic_Management_Profile:
	            if (cellNumber < 1) {
	                report.report("Cell ID need to be greater than 0", Reporter.WARNING);
	                return null;
	            }
	            getActiveIfActiveCell(enbConfigSet, cellNumber).setTrafficManagementProfile(profileName);
	            break;
	        case Call_Trace_Profile:
	            if (cellNumber < 1) {
	                report.report("Cell ID need to be greater than 0", Reporter.WARNING);
	                return null;
	            }
	            getActiveIfActiveCell(enbConfigSet, cellNumber).setCallTraceProfile(profileName);
	            break;
	        case Neighbour_Management_Profile:
	        	enbConfigSet.setNeighbourProfile(profileName);
				break;
	        default:
	            report.report("In method addProfile : No Enum EnbProfile", Reporter.WARNING);
	            return null;
	    }
	    return enbConfigSet;
    }

    private LteCellSetWs getActiveIfActiveCell(LteEnbDetailsSetWs enbConfigSet, int cellNumber) {
        ObjectFactory objectFactory = new ObjectFactory();
        LteCellSetWs newCell = new LteCellSetWs();
        for (LteCellSetWs tempCell : enbConfigSet.getLteCell()) {
            if (tempCell.getCellNumber().getValue().equals(String.valueOf(cellNumber)))
                return tempCell;
        }
        newCell.setCellNumber(objectFactory.createLteCellGetWsCellNumber(String.valueOf(cellNumber)));
        enbConfigSet.getLteCell().add(newCell);
        return newCell;
        /*
         * try { for (int i = 0; i <= enbConfigSet.getLteCell().size(); i++) {
         * String cellStr =
         * enbConfigSet.getLteCell().get(i).getCellNumber().getValue(); int
         * cellInt = Integer.valueOf(cellStr); if (cellInt == cellNumber) {
         * return i; } } } catch (Exception e) { return -1; } return -1;
         */
    }

    /**
     * @param node
     * @param profileType
     * @param enbConfigSet
     * @param snmpDetailSetWs
     * @return
     * @author Shahaf Shuhamy method doing enbConfigSet to an object used in
     * setProfile
     */
    private boolean enbSetContent(EnodeB node, NodeProperty nodeProperty, LteEnbDetailsSetWs enbConfigSet,
			  SnmpDetailSetWs snmpDetailSetWs) {
		try {
			Netspan.NBI_17_5.Lte.NodeActionResult result = soapHelper_17_5
			.getLteSoap()
			.enbConfigSet(node.getNetspanName(), nodeProperty,null, enbConfigSet, snmpDetailSetWs, credentialsLte);
			
			if (result.getErrorCode() != Netspan.NBI_17_5.Lte.ErrorCodes.OK) {
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
			soapHelper_17_5.endLteSoap();
		}
	}

    /**
     * Clones radio profile via NETSPAN. for now it configures all the cells to
     * the same new configurations enter negative numbers / null in arguments to
     * use the current values of the cell
     *
     * @param node          the node
     * @param cloneFromName the clone from name
     * @param band          the band
     * @param bandwith      the bandwith
     * @param earfcn        the earfcn
     * @param downlink      the downlink
     * @param uplink        the uplink
     * @param mfbi          the mfbi
     * @param txPower       the tx power
     * @param sfr           the sfr
     * @param fc            the fc
     * @return true, if successful
     */
    @Override
    public boolean cloneRadioProfile(EnodeB node, String cloneFromName, RadioParameters radioParams) {

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

        EnbRadioProfile radioProfile = createEnbRadioProfile(radioParams);
        if (radioProfile == null)
            return false;

        try {
            ProfileResponse cloneResult = soapHelper_17_5.getLteSoap()
                .radioProfileClone(cloneFromName, radioProfile, credentialsLte);

            boolean result = false;
            if (cloneResult == null) {
                report.report("Fail to get Radio profile cloning result", Reporter.WARNING);
                result = false;
            } else if (cloneResult.getErrorCode() == Netspan.NBI_17_5.Lte.ErrorCodes.OK) {
                report.report("Succeeded to clone Radio profile, new profile name: " + radioParams.getProfileName());
                result = true;
            } else {
                report.report("Failed to clone Radio profile, reason: " + cloneResult.getErrorString(),
                    Reporter.WARNING);
                result = false;
            }
            return result;

        } catch (Exception e) {
            GeneralUtils.printToConsole("Could not execute radioProfileClone on " + cloneFromName);
            report.report("Failed to clone Radio profile, due to: " + e.getMessage(), Reporter.WARNING);
            return false;
        } finally {
            soapHelper_17_5.endLteSoap();
        }
    }

    @Override
    public boolean updateRadioProfile(EnodeB node, RadioParameters radioParams) {
        String profileName = getCurrentRadioProfileName(node);
        radioParams.setProfileName(profileName);
        EnbRadioProfile radioProfile = createEnbRadioProfile(radioParams);
        if (radioProfile == null)
            return false;

        try {
            ProfileResponse cloneResult = soapHelper_17_5.getLteSoap().radioProfileUpdate(profileName,
                radioProfile, credentialsLte);

            if (cloneResult == null) {
                report.report("Fail to get Radio profile update result. ", Reporter.WARNING);
                return false;
            } else if (cloneResult.getErrorCode() == Netspan.NBI_17_5.Lte.ErrorCodes.OK) {
                report.report("Succeeded to update Radio profile, new profile name: " + profileName);
                return true;
            } else {
                report.report("Failed to update Radio profile, reason: " + cloneResult.getErrorString(),
                    Reporter.WARNING);
                return false;
            }
        } catch (Exception e) {
            GeneralUtils.printToConsole("Could not execute radioProfileUpdate on " + profileName);
            report.report("Failed to update Radio profile, due to: " + e.getMessage(), Reporter.WARNING);
            return false;
        } finally {
            soapHelper_17_5.endLteSoap();
        }
    }

    protected EnbRadioProfile createEnbRadioProfile(RadioParameters radioParams) {
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
			EnabledStates ecidParam;
			ecidParam = ecid ? EnabledStates.ENABLED : EnabledStates.DISABLED;
			radio.setEcidMode(factoryObject.createEnbRadioProfileParamsEcidMode(ecidParam));
		}
		
		Integer ecidTimer = radioParams.getECIDProcedureTimer();
		if(ecidTimer != null){
			radio.setEcidTimer(factoryObject.createEnbRadioProfileParamsEcidTimer(ecidTimer));
		}
		
		Boolean otdoa = radioParams.getOTDOAMode();
		if(otdoa != null){
			EnabledStates otdoaParam;
			otdoaParam = otdoa ? EnabledStates.ENABLED : EnabledStates.DISABLED;
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
						subFramesObj.setSubFrame0(factoryObject.createRfSubframeSubFrame0(EnabledStates.ENABLED));
						break;
					case "1":
						subFramesObj.setSubFrame1(factoryObject.createRfSubframeSubFrame1(EnabledStates.ENABLED));
						break;
					case "2":
						subFramesObj.setSubFrame2(factoryObject.createRfSubframeSubFrame2(EnabledStates.ENABLED));
						break;
					case "3":
						subFramesObj.setSubFrame3(factoryObject.createRfSubframeSubFrame3(EnabledStates.ENABLED));
						break;
					case "4":
						subFramesObj.setSubFrame4(factoryObject.createRfSubframeSubFrame4(EnabledStates.ENABLED));
						break;
					case "5":
						subFramesObj.setSubFrame5(factoryObject.createRfSubframeSubFrame5(EnabledStates.ENABLED));
						break;
					case "6":
						subFramesObj.setSubFrame6(factoryObject.createRfSubframeSubFrame6(EnabledStates.ENABLED));
						break;
					case "7":
						subFramesObj.setSubFrame7(factoryObject.createRfSubframeSubFrame7(EnabledStates.ENABLED));
						break;
					case "8":
						subFramesObj.setSubFrame8(factoryObject.createRfSubframeSubFrame8(EnabledStates.ENABLED));
						break;
					case "9":
						subFramesObj.setSubFrame9(factoryObject.createRfSubframeSubFrame9(EnabledStates.ENABLED));
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

    /**
     * Clone son profile via netspan.
     *
     * @param node          the node
     * @param cloneFromName the clone from name
     * @param sonEnabled    the son enabled
     * @param pnpEnabled    the pnp enabled
     * @param autoPCI       the auto pci
     * @param ranges        the ranges
     * @param anrState      the anr state
     * @param anrFrequency  the anr frequency
     * @return true, if successful
     */
    @Override
    public boolean cloneSonProfile(EnodeB node, String cloneFromName, SonParameters sonParmas) {
    	try {

			EnbSonProfile sonProfile = parseSonParameters(node, sonParmas);
			ProfileResponse cloneResult = soapHelper_17_5.getLteSoap().sonProfileClone(cloneFromName,
					sonProfile, credentialsLte);

			if (cloneResult == null) {
				report.report("Fail to get Son profile cloning resut", Reporter.WARNING);
				return false;
			} else if (cloneResult.getErrorCode() == Netspan.NBI_17_5.Lte.ErrorCodes.OK) {
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
			soapHelper_17_5.endLteSoap();
		}
    }

    /**
     * Clone sync profile via netspan.
     *
     * @param profile       the profile
     * @param cloneFromName the clone from name
     * @return true, if successful
     */
    @Override
    public boolean cloneSyncProfile(EnodeB node, String cloneFromName, SyncParameters syncParams) {

    	EnbSyncProfile syncProfile = new EnbSyncProfile();
		syncProfile.setName(syncParams.getProfileName());
		ObjectFactory objectFactory = new ObjectFactory();

		
		if (syncParams.getPrimaryClockSource() != null){
			//syncProfile.setPrimaryClockSource(objectFactory.createEnbSyncProfileParamsPrimaryClockSource(ClockSources.fromValue(syncParams.getPrimaryClockSource())));
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
			ProfileResponse cloneResult = (ProfileResponse) soapHelper_17_5.getLteSoap().syncProfileClone(cloneFromName,
					syncProfile, credentialsLte);

			boolean result = false;
			if (cloneResult == null) {
				report.report("Fail to get Sync profile cloning result", Reporter.WARNING);
				result = false;
			} else if (cloneResult.getErrorCode() == Netspan.NBI_17_5.Lte.ErrorCodes.OK) {
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
			soapHelper_17_5.endLteSoap();
		}
    }

    @Override
    public PrimaryClockSourceEnum getPrimaryClockSource(EnodeB node) {
    	EnbDetailsGet config = getNodeConfig(node);
		String profile = config.getSyncProfile();
		List<String> nameList = new ArrayList<String>();
		nameList.add(profile);
		List<Netspan.API.Enums.CategoriesLte> cat = new ArrayList<>();
		
		try{
			LteSyncProfileGetResult syncProfile = (LteSyncProfileGetResult) soapHelper_17_5.getLteSoap().syncProfileGet(nameList, cat ,null, credentialsLte);
			ClockSources clockSource = null;
								
			ClockContainer clockList = syncProfile.getSyncProfileResult().get(FIRST_ELEMENT).getSyncProfile()
					.getClockSourceList();
			for (ClockSource cs : clockList.getClockSource()){
				if(cs.getPriority() == 1){
					clockSource = cs.getClock();
					break;
				}
			}
			
			if(clockSource == ClockSources.NONE){
				return PrimaryClockSourceEnum.NONE;
			}
			if(clockSource == ClockSources.GNSS){
				return PrimaryClockSourceEnum.GNSS;
			}
			if(clockSource == ClockSources.IEEE_1588){
				return PrimaryClockSourceEnum.IEEE_1588;
			}
			if(clockSource == ClockSources.NLM){
				return PrimaryClockSourceEnum.NLM;
			}
		}		
		catch(Exception e){
			e.printStackTrace();
			report.report("getPrimaryClockSource Failed, reason: " + e.getMessage(), Reporter.WARNING);
			return null;
		} finally {
            soapHelper_17_5.endLteSoap();
        }
		return null;
    }

    public String getCurrentProfileName(EnodeB node, EnbProfiles profileType) {
        try {
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

                case Cell_Advanced_Profile:
                    return getCurrentCellAdvancedConfigurationProfileName(node);

                case MultiCell_Profile:
                    return getMultiCellProfileName(node);
                    
                case Neighbour_Management_Profile:
                	return getCurrentNeighborManagmentProfileName(node);
                	
                default:
                    report.report("Enum: No Such EnbProfile", Reporter.WARNING);
                    return "";
            }
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

    @Override
    public String getMultiCellProfileName(EnodeB enb) {
        EnbDetailsGet nodeConfig = getNodeConfig(enb);
        return nodeConfig != null ? nodeConfig.getMultiCellProfile() : "";
    }

    @Override
    public boolean setMultiCellState(EnodeB node, Boolean isEnabled) {
        LteEnbDetailsSetWs enbConfigSet = new LteEnbDetailsSetWs();
        LteCellSetWs lteCellSet = new LteCellSetWs();
        ObjectFactory factoryDetails = new ObjectFactory();
        lteCellSet.setCellNumber(factoryDetails.createLteCellSetWsCellNumber("2"));
        lteCellSet.setIsEnabled(factoryDetails.createLteCellGetWsIsEnabled(isEnabled));
        enbConfigSet.getLteCell().add(lteCellSet);
        return setNodeConfig(node, enbConfigSet);
    }

    public boolean cloneProfile(EnodeB node, String cloneFromName, INetspanProfile profileParams) {
        try {
            switch (profileParams.getType()) {
                case Management_Profile:
                    return cloneManagementProfile(node, cloneFromName, (ManagementParameters) profileParams);

                case Mobility_Profile:
                    return cloneMobilityProfile(node, cloneFromName, (MobilityParameters) profileParams);

                case Network_Profile:
                    return cloneNetworkProfile(node, cloneFromName, (NetworkParameters) profileParams);

                case Radio_Profile:
                    return cloneRadioProfile(node, cloneFromName, (RadioParameters) profileParams);

                case Security_Profile:
                    return cloneSecurityProfile(node, cloneFromName, (SecurityParameters) profileParams);

                case Son_Profile:
                    return cloneSonProfile(node, cloneFromName, (SonParameters) profileParams);

                case Sync_Profile:
                    return cloneSyncProfile(node, cloneFromName, (SyncParameters) profileParams);

                case EnodeB_Advanced_Profile:
                    return cloneEnodeBAdvancedProfile(node, cloneFromName, (EnodeBAdvancedParameters) profileParams);

                case Cell_Advanced_Profile:
                    return cloneCellAdvancedProfile(node, cloneFromName, (CellAdvancedParameters) profileParams);

                case MultiCell_Profile:
                    return cloneMultiCellProfile(node, cloneFromName, (MultiCellParameters) profileParams);

                case Neighbour_Management_Profile:
                	return cloneNeighborManagementProfile(node, cloneFromName,
    						(NeighbourManagementParameters) profileParams);
                	
                default:
                    report.report("Enum: No Such EnbProfile", Reporter.WARNING);
                    return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            report.report("Error in cloneProfile: " + e.getMessage(), Reporter.WARNING);
            return false;
        }
    }

    @Override
    public boolean cloneMobilityProfile(EnodeB node, String cloneFromName, MobilityParameters mobilityParams) {
        try {
            ObjectFactory factoryDetails = new ObjectFactory();
            EnbMobilityProfile mobilityProfile = new EnbMobilityProfile();
            
            MobilityConnectedModeIntraFrequencyListContainer intraList = new MobilityConnectedModeIntraFrequencyListContainer();
            MobilityConnectedModeInterDefaultContainer interList = new MobilityConnectedModeInterDefaultContainer();
            
            MobilityConnectedModeFreq innerList = null;
            //MobilityConnectedModeInterFreq mobilityConnectedMode = null;
            innerList = new MobilityConnectedModeFreq();
            if (mobilityParams.getEventType() != null) {
            	innerList.setEventType(factoryDetails.
            			createMobilityConnectedModeFreqEventType(mobilityParams.getEventType()));
                switch (mobilityParams.getEventType()) {
                    case A_3:
                        if (mobilityParams.getA3Offset() != null) {
                        	innerList.setA3Offset(factoryDetails.createMobilityConnectedModeFreqA3Offset(
                                BigDecimal.valueOf(mobilityParams.getA3Offset())));
                        }
                        break;
                    case A_4:
                        if (mobilityParams.getRsrpEventThreshold1() != null) {
                        	innerList.setRsrpEventThreshold1(
                                factoryDetails.createMobilityConnectedModeFreqRsrpEventThreshold1(
                                    mobilityParams.getRsrpEventThreshold1()));
                        }
                        break;
                    case A_5:
                        if (mobilityParams.getRsrpEventThreshold1() != null) {
                        	innerList.setRsrpEventThreshold1(
                                factoryDetails.createMobilityConnectedModeFreqRsrpEventThreshold1(
                                    mobilityParams.getRsrpEventThreshold1()));
                        }
                        if (mobilityParams.getRsrpEventThreshold2() != null) {
                        	innerList.setRsrpEventThreshold2(
                                factoryDetails.createMobilityConnectedModeFreqRsrpEventThreshold2(
                                    mobilityParams.getRsrpEventThreshold2()));
                        }
                        break;
                    default:
                        break;
                }
                if (mobilityParams.getHysteresis() != null) {
                	innerList.setHysteresis(factoryDetails.createMobilityConnectedModeFreqHysteresis(
                        BigDecimal.valueOf(mobilityParams.getHysteresis())));
                }
                if (mobilityParams.getIsIntra()) {
                	intraList.getConnectedModeIntraFrequency().add(innerList);
                    mobilityProfile.setConnectedModeIntraFrequencyList(intraList);
                } else {
                	interList.getConnectedModeInterDefaultEntry().add(innerList);
                    mobilityProfile.setConnectedModeInterFrequencyDefaultList(interList);
                }
            }

            if (mobilityParams.getThresholdBasedMeasurement() != null) {
                mobilityProfile.setIsThresholdBasedMeasurementEnabled(
                    factoryDetails.createEnbMobilityProfileParamsIsThresholdBasedMeasurementEnabled(
                        mobilityParams.getThresholdBasedMeasurement()));
                if (mobilityParams.getThresholdBasedMeasurement() == EnabledStates.ENABLED) {
                    MobilityConnectedModeThresholdBasedListContainer listGaps = new MobilityConnectedModeThresholdBasedListContainer();
                	MobilityConnectedModeThresholdBased gaps = new MobilityConnectedModeThresholdBased();
                	gaps.setTriggerGapsRsrpEventThreshold1(factoryDetails.
                			createMobilityConnectedModeThresholdBasedTriggerGapsRsrpEventThreshold1(mobilityParams.getStartGap()));
                	gaps.setStopGapsRsrpEventThreshold1(factoryDetails.
                			createMobilityConnectedModeThresholdBasedStopGapsRsrpEventThreshold1(mobilityParams.getStopGap()));
                	listGaps.getMobilityConnectedModeThresholdBased().add(gaps);
                	mobilityProfile.setMobilityConnectedModeThresholdBasedList(listGaps);
                }
            }
            if (mobilityParams.getProfileName() == null) {
                return false;
            } else {
                mobilityProfile.setName(mobilityParams.getProfileName());
            }

            ProfileResponse cloneResult = soapHelper_17_5.getLteSoap()
                .mobilityProfileClone(cloneFromName, mobilityProfile, credentialsLte);
            boolean result = false;
            if (cloneResult == null) {
                report.report("Fail to get Mobility profile cloning result", Reporter.WARNING);
                result = false;
            } else if (cloneResult.getErrorCode() == Netspan.NBI_17_5.Lte.ErrorCodes.OK) {
                report.report(
                    "Succeeded to clone Mobility profile, new profile name: " + mobilityParams.getProfileName());
                result = true;
            } else {
                report.report("Failed to clone Mobility profile, reason: " + cloneResult.getErrorString(),
                    Reporter.WARNING);
                result = false;
            }

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            report.report("Failed to clone Mobility profile, due to: " + e.getMessage(), Reporter.WARNING);
            return false;
        } finally {
            soapHelper_17_5.endLteSoap();
        }
    }

    @Override
    public boolean cloneNetworkProfile(EnodeB node, String cloneFromName, NetworkParameters networkParams)
        throws Exception {
    	EnbNetworkProfile networkProfile = parseNetworkParameters(node, networkParams);
		ProfileResponse cloneResult = soapHelper_17_5.getLteSoap().networkProfileClone(cloneFromName,
				networkProfile, credentialsLte);
		if (cloneResult == null) {
			report.report("Fail to get Network profile cloning result", Reporter.WARNING);
			soapHelper_17_5.endLteSoap();
			return false;
		} else if (cloneResult.getErrorCode() == Netspan.NBI_17_5.Lte.ErrorCodes.OK) {
			report.report("Succeeded to clone Network profile, new profile name: " + networkParams.getProfileName());
			soapHelper_17_5.endLteSoap();
			return true;
		} else {
			report.report("Failed to clone Network profile, reason: " + cloneResult.getErrorString(), Reporter.WARNING);
			soapHelper_17_5.endLteSoap();
			return false;
		}
    }

    @Override
    public boolean cloneSecurityProfile(EnodeB node, String cloneFromName, SecurityParameters securityParams)
        throws Exception {
        String profilePreFix = GeneralUtils.getPrefixAutomation();
        LteSecurityProfileGetResult currentSecurityProfile = null;
        ArrayList<String> profilesList = new ArrayList<String>();
        profilesList.add(cloneFromName);
        try {
            currentSecurityProfile = soapHelper_17_5.getLteSoap()
                .securityProfileGet(profilesList,null,null, credentialsLte);
            if (currentSecurityProfile.getErrorCode() != Netspan.NBI_17_5.Lte.ErrorCodes.OK) {
                report.report("securityProfileGet via Netspan Failed : " + currentSecurityProfile.getErrorString(),
                    Reporter.WARNING);
                return false;
            }
        } catch (Exception e) {
            report.report("securityProfileGet via Netspan Failed due to: " + e.getMessage(), Reporter.WARNING);
            e.printStackTrace();
            return false;
        }
        GeneralUtils
            .printToConsole("securityProfileGet via Netspan for eNodeB " + node.getNetspanName() + " succeeded");

        if (cloneFromName.length() >= 64) {
            StringBuilder a = new StringBuilder();
            a.append(cloneFromName);
            a.delete(a.length() - profilePreFix.length(), a.length() - 1);
            cloneFromName = a.toString();
        }
        // change the profileForTest Name to diffrent between Profiles
        // String securityProfileToClone = currentSecurityProfileName +
        // profilePreFix;
        EnbSecurityProfile newSecurityProfile = new EnbSecurityProfile();
        newSecurityProfile = currentSecurityProfile.getSecurityProfileResult().get(0).getSecurityProfile();

        this.insertParamsToEnbSecurityObj(newSecurityProfile,
            currentSecurityProfile.getSecurityProfileResult().get(0).getSecurityProfile().getHardwareCategory(),
            securityParams.getProfileName(), securityParams.getIntegrityMode(),
            securityParams.getIntegrityNullLevel(), securityParams.getIntegrityAESLevel(),
            securityParams.getIntegritySNOWLevel(), securityParams.getIntegrityMode(),
            securityParams.getCipheringNullLevel(), securityParams.getCipheringAESLevel(),
            securityParams.getCipheringSNOWLevel());
        try {
            // gives the method the name of the new profile and the profile
            // as is that i would like to clone
            ProfileResponse cloneResult = soapHelper_17_5.getLteSoap()
                .securityProfileClone(cloneFromName, newSecurityProfile, credentialsLte);
            boolean result = false;
            if (cloneResult.getErrorCode() != Netspan.NBI_17_5.Lte.ErrorCodes.OK) {
                report.report("securityProfileClone via Netspan Failed : " + cloneResult.getErrorString(),
                    Reporter.WARNING);
                result = false;
            } else {
                GeneralUtils.printToConsole("securityProfileClone via Netspan for eNodeB " + node.getNetspanName()
                    + " succeeded, new profile name: " + securityParams.getProfileName());
                report.report(
                    "Succeeded to clone Security profile, new profile name: " + securityParams.getProfileName());
                result = true;
            }
            return result;

        } catch (Exception e) {
            report.report("securityProfileClone via Netspan Failed due to: " + e.getMessage(), Reporter.WARNING);
            e.printStackTrace();
            return false;
        } finally {
            soapHelper_17_5.endLteSoap();
        }
    }

    @Override
    public boolean convertToPnPConfig(EnodeB node) {
    	try {
			Netspan.NBI_17_5.Lte.NodeActionResult result = null;
			String swImageName = null;
			if (node.isSwUpgradeDuringPnP()) {
				swImageName = node.getDefaultNetspanProfiles().getSoftwareImage();
			}

			if (isRelayEnodeb(node.getNetspanName())) {
				AuPnpDetailWs pnpDetail = null;
				if (swImageName != null) {
					pnpDetail = new AuPnpDetailWs();
					pnpDetail.setPnpSwImageName(swImageName);
				}
				result = soapHelper_17_5.getLteSoap().relayEnbPnpConfigConvertFromNode(
						node.getNetspanName(), null, pnpDetail, null, null, null, null, credentialsLte);
			} else {
				PnpDetailWs pnpDetail = null;
				if (swImageName != null) {
					pnpDetail = new PnpDetailWs();
					pnpDetail.setPnpSwImageName(swImageName);
				}
				result = soapHelper_17_5.getLteSoap().pnpConfigConvertFromNode(node.getNetspanName(),
						null, pnpDetail, null, credentialsLte);
			}

			if (result == null) {
				report.report("Fail to get System Default Profile cloning result", Reporter.WARNING);
				return false;
			} else if (result.getErrorCode() == Netspan.NBI_17_5.Lte.ErrorCodes.OK) {
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

    public boolean removeNode(EnodeB node) {
        return removeNode(node.getNetspanName());
    }

    public boolean removeNode(String nodeName) {
        ArrayList<String> nodeList = new ArrayList<String>();
        nodeList.add(nodeName);
        try {
            setManagedMode(nodeName, NodeManagementModes.UNMANAGED);

            GeneralUtils.printToConsole("Sending NBI requeset \"nodeDelete\" for eNodeB " + nodeName);
            NodeActionResult result = soapHelper_17_5.getInventorySoap().nodeDelete(nodeList, null,
                credentialsInventory);

            GeneralUtils.printToConsole(String.format("NBI method \"nodeDelete\" for eNodeB %s returned value: %s",
                nodeName, result.getErrorCode().toString()));

            if (result.getErrorCode() != Netspan.NBI_17_5.Inventory.ErrorCodes.OK) {
                report.report("nodeDelete Failed : " + result.getErrorString(), Reporter.WARNING);
                return false;
            } else {
                report.report(nodeName + " - Remove node succeeded.");
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            report.report("nodeDelete Failed, due to: " + e.getMessage(), Reporter.WARNING);
            return false;
        } finally {
            soapHelper_17_5.endInventorySoap();
        }
    }

    public boolean removeDiscoveryTask(String nodeName) {
        ArrayList<String> nodeList = new ArrayList<String>();
        nodeList.add(nodeName);
        try {
            GeneralUtils.printToConsole("Sending NBI requeset \"discoveryTaskDelete\" for eNodeB " + nodeName);
            DiscoveryTaskActionResult result = soapHelper_17_5.getInventorySoap()
                .discoveryTaskDelete(nodeList, credentialsInventory);
            GeneralUtils
                .printToConsole(String.format("NBI method \"discoveryTaskDelete\" for eNodeB %s returned value: %s",
                    nodeName, result.getErrorCode().toString()));

            if (result.getErrorCode() != Netspan.NBI_17_5.Inventory.ErrorCodes.OK) {
                String resultString = result.getTask().get(0).getDiscoveryTaskResultString();
                report.report("discoveryTaskDelete Failed : " + result.getErrorString() + "-" + resultString,
                    Reporter.WARNING);
                return false;
            } else {
                report.report(nodeName + " - Remove discovery task succeeded.");
                return true;
            }

        } catch (Exception e) {
            e.printStackTrace();
            report.report("discoveryTaskDelete Failed, due to: " + e.getMessage(), Reporter.WARNING);
            return false;
        } finally {
            soapHelper_17_5.endInventorySoap();
        }
    }

    @Override
    public boolean setManagedMode(String nodeName, NodeManagementModes managedMode) {
        Netspan.NBI_17_5.Inventory.ObjectFactory factory = new Netspan.NBI_17_5.Inventory.ObjectFactory();
        ArrayList<NodeManagementMode> nodeList = new ArrayList<NodeManagementMode>();
        NodeManagementMode node = new NodeManagementMode();
        node.setNodeNameOrId(nodeName);
        node.setManagementMode(
            factory.createNodeManagementModeManagementMode(NodeManagementModes.fromValue(managedMode.value())));
        nodeList.add(node);
        try {
            NodeActionResult result = soapHelper_17_5.getInventorySoap()
                .nodeManagementModeSet(nodeList, credentialsInventory);
            if (result.getErrorCode() != Netspan.NBI_17_5.Inventory.ErrorCodes.OK) {
                report.report("nodeManagementModeSet Failed : " + result.getErrorString(), Reporter.WARNING);
                return false;
            } else
                return true;

        } catch (Exception e) {
            e.printStackTrace();
            report.report("nodeManagementModeSet Failed, due to: " + e.getMessage(), Reporter.WARNING);
            return false;
        } finally {
            soapHelper_17_5.endInventorySoap();
        }
    }

    private PnpDetailWs getPnpDetails(EnodeB enodeB) {
        report.report(enodeB.getNetspanName() + " - Get PnP details.");
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

    @Override
    public boolean movePnPConfigToNodeList(EnodeB node) {
        boolean actionSucceeded = true;
        EnbPnpConfig pnpConfig = getPnpNode(node);
        if (pnpConfig != null) {
            actionSucceeded &= removePnPNode(node.getNetspanName());
            actionSucceeded &= createDiscoveryTaskV2(node, "public", "private");
            try {
                Thread.sleep(1000 * 60); // wait for the Node appearance in
                // netspan.
            } catch (InterruptedException e) {
            }
            actionSucceeded &= setNodeConfig(node, EnbConfigGetToSet(pnpConfig.getLteEnbConfig()));
        } else {
            actionSucceeded &= createDiscoveryTaskV2(node, "public", "private");
        }
        actionSucceeded &= setManagedMode(node.getNetspanName(), NodeManagementModes.MANAGED);
        return actionSucceeded;
    }

    private boolean createDiscoveryTaskV2(EnodeB enodeB, String pswrdRead, String pswrdWrite) {
        report.report(enodeB.getNetspanName() + " - Create discovery task V2.");
        GeneralUtils.printToConsole("Sending NBI request discoveryTaskAddSnmpV2 for eNodeB " + enodeB.getNetspanName());
        WsResponse result;
        try {
            result = soapHelper_17_5.getInventorySoap().discoveryTaskAddSnmpV2(enodeB.getNetspanName(),
                enodeB.getIpAddress(), 161, pswrdRead, pswrdWrite, credentialsInventory);
            GeneralUtils.printToConsole("NBI method discoveryTaskAddSnmpV2 for eNodeB " + enodeB.getNetspanName()
                + " returned value: " + result.getErrorCode().toString());
            if (result.getErrorCode() != Netspan.NBI_17_5.Inventory.ErrorCodes.OK) {
                report.report("discoveryTaskAddSnmpV2 Failed : " + result.getErrorString(), Reporter.WARNING);
                return false;
            } else
                return true;
        } catch (Exception e) {
            e.printStackTrace();
            report.report("discoveryTaskAddSnmpV2 Failed, due to: " + e.getMessage(), Reporter.WARNING);
            return false;
        } finally {
            soapHelper_17_5.endInventorySoap();
        }
    }

    @Override
    public boolean setPnPSwVersion(EnodeB enodeB, String swVersion) {
        EnbPnpConfig enbPnpConfig = getPnpNode(enodeB);
        enbPnpConfig.getLtePnpConfig().setPnpSwImageName(swVersion);
        return updatePnPConfig(enodeB, enbPnpConfig);
    }

    private boolean removePnPNode(String nodeName) {
        ArrayList<String> nodeList = new ArrayList<String>();
        nodeList.add(nodeName);
        try {
            GeneralUtils.printToConsole("Sending NBI requeset \"pnpConfigDelete\" for eNodeB " + nodeName);
            Netspan.NBI_17_5.Lte.NodeActionResult result = soapHelper_17_5
                .getLteSoap().pnpConfigDelete(nodeList, credentialsLte);

            if (result.getErrorCode() != Netspan.NBI_17_5.Lte.ErrorCodes.OK) {
                report.report("pnpConfigDelete Failed : " + result.getErrorString(), Reporter.WARNING);
                return false;
            } else
                return true;

        } catch (Exception e) {
            e.printStackTrace();
            report.report("pnpConfigDelete Failed, due to: " + e.getMessage(), Reporter.WARNING);
            return false;
        } finally {
            soapHelper_17_5.endLteSoap();
        }
    }

    @Override
    public boolean setPnPRadioProfile(EnodeB enodeB, String radioProfileName) {
        EnbPnpConfig enbPnpConfig = getPnpNode(enodeB);
        int cellNumber = enodeB.getCellContextID();
        if (enbPnpConfig.getLteEnbConfig() == null) {
            return false;
        }
        for (LteCellGetWs cell : enbPnpConfig.getLteEnbConfig().getLteCell()) {
            if (Integer.valueOf(cell.getCellNumber().getValue()) == cellNumber) {
                cell.setRadioProfile(radioProfileName);
            }
        }
        return updatePnPConfig(enodeB, enbPnpConfig);
    }

    private boolean updatePnPConfig(EnodeB enodeB, EnbPnpConfig pnpConfig) {
        GeneralUtils.printToConsole("Sending NBI requeset \"pnpConfigUpdate\"");
        Netspan.NBI_17_5.Lte.NodeActionResult result;
        LteEnbDetailsSetWsPnp snmpDetailWs = new LteEnbDetailsSetWsPnp();
        try {
            result = soapHelper_17_5.getLteSoap().pnpConfigUpdate(
                enodeB.getNetspanName(),null, pnpConfig.getLtePnpConfig(),
                snmpDetailWs, credentialsLte);
            GeneralUtils.printToConsole(
                "NBI method \"pnpConfigUpdate\" returned value: " + result.getErrorCode().toString());
            return result.getErrorCode() == Netspan.NBI_17_5.Lte.ErrorCodes.OK;
        } catch (Exception e) {
            e.printStackTrace();
            report.report("pnpConfigUpdate Failed, due to: " + e.getMessage(), Reporter.WARNING);
            return false;
        } finally {
            soapHelper_17_5.endLteSoap();
        }
    }

    private EnbPnpConfig getPnpNode(EnodeB enodeB) {
        return getPnpNode(enodeB.getNetspanName());
    }

    protected EnbPnpConfig getPnpNode(String nodeName) {
        ArrayList<String> nodeList = new ArrayList<String>();
        nodeList.add(nodeName);
        if (isPnPNodeExists(nodeName)) {
            try {
                GeneralUtils.printToConsole("Sending NBI requeset \"PnpConfigGet\" for eNodeB " + nodeName);
                LtePnpConfigGetResult result = soapHelper_17_5.getLteSoap()
                    .pnpConfigGet(nodeList,null, credentialsLte);
                GeneralUtils
                    .printToConsole(String.format("NBI method \"PnpConfigGet\" for eNodeB %s returned value: %s",
                        nodeName, result.getErrorCode().toString()));
                if (result.getErrorCode() != Netspan.NBI_17_5.Lte.ErrorCodes.OK) {
                    report.report("pnpConfigGet via Netspan Failed : " + result.getErrorString(), Reporter.WARNING);
                    return null;
                } else {
                    return result.getPnpConfig().get(FIRST_ELEMENT);
                }
            } catch (Exception e) {
                e.printStackTrace();
                report.report("pnpConfigGet Failed, due to: " + e.getMessage(), Reporter.WARNING);
                return null;
            } finally {
                soapHelper_17_5.endLteSoap();
            }
        }
        return null;
    }

    private boolean isPnPNodeExists(String pnpNodeName) {
        GeneralUtils.printToConsole("Sending NBI requeset \"pnpConfigList\"");
        NodeListResult result;
        try {
            result = soapHelper_17_5.getLteSoap().pnpConfigList(credentialsLte);
            GeneralUtils
                .printToConsole("NBI method \"pnpConfigList\" returned value: " + result.getErrorCode().toString());
            if (result.getErrorCode() == Netspan.NBI_17_5.Lte.ErrorCodes.OK) {
                boolean isExist = false;
                for (NodeSimple node : result.getNode()) {
                    if (node.getName().equals(pnpNodeName))
                        isExist = true;
                }
                return isExist;
            }
        } catch (Exception e) {
            e.printStackTrace();
            report.report("pnpConfigList Failed, due to: " + e.getMessage(), Reporter.WARNING);
            return false;
        } finally {
            soapHelper_17_5.endLteSoap();
        }
        return false;
    }

    private LteEnbDetailsSetWs EnbConfigGetToSet(EnbDetailsGetPnp enbConfigGet) {
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

        for (LteCellGetWs cell : enbConfigGet.getLteCell()) {
            if (cell != null) {
                if (cell.getCellIdForEci().getValue() != null) {
                    enbConfigSet.getLteCell().add(LteCellGetToSet(cell));
                }
            }
        }
        return enbConfigSet;
    }

    public LteCellSetWs LteCellGetToSet(LteCellGetWs lteCellGet) {
        LteCellSetWs lteCellSet = new LteCellSetWs();
        lteCellSet.setCellAdvancedConfigurationProfile(lteCellGet.getCellAdvancedConfigurationProfile());
        lteCellSet.setCellNumber(lteCellGet.getCellNumber());
        lteCellSet.setCellIdForEci(lteCellGet.getCellIdForEci());
        lteCellSet.setEmbmsProfile(lteCellGet.getEmbmsProfile());
        lteCellSet.setEmergencyAreaIds(lteCellGet.getEmergencyAreaIds());
        if (lteCellGet.getIsAccessClassBarred().getValue().equals(true)) {
            lteCellSet.setIsCsfbAccessBarred(lteCellGet.getIsAccessClassBarred());
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
        return lteCellSet;
    }

    private boolean createPnpNode(PnpConfigCreate pnpConfigCreate) {
        report.report(String.format("%s - Add node to PnP list.", pnpConfigCreate.getENBDetail().getName()));
        GeneralUtils.printToConsole("Sending NBI requeset \"pnpConfigCreate\"");
        Netspan.NBI_17_5.Lte.NodeActionResult result;
        SnmpDetailWs snmpDetailWs = new SnmpDetailWs();
        try {
            result = soapHelper_17_5.getLteSoap().pnpConfigCreate(null,
                pnpConfigCreate.getPnpDetail(), pnpConfigCreate.getENBDetail(), credentialsLte);

            if (result.getErrorCode() != Netspan.NBI_17_5.Lte.ErrorCodes.OK) {
                report.report("pnpConfigCreate via Netspan Failed : " + result.getErrorString(), Reporter.WARNING);
                return false;
            } else
                return true;
        } catch (Exception e) {
            report.report("pnpConfigCreate via Netspan Failed due to: " + e.getMessage(), Reporter.WARNING);
            e.printStackTrace();
            return false;
        } finally {
            soapHelper_17_5.endLteSoap();
        }
    }

    public boolean isPnpNode(String nodeName) {
        boolean isPnp = false;
        NodeListResult result = null;
        try {
            result = soapHelper_17_5.getLteSoap().pnpConfigList(credentialsLte);
        } catch (Exception e) {
            e.printStackTrace();
            report.report("isPnpNode failed due to: " + e.getMessage(), Reporter.WARNING);
            return false;
        }
        if (result.getErrorCode() != Netspan.NBI_17_5.Lte.ErrorCodes.OK)
            return false;
        for (NodeSimple node : result.getNode()) {
            if (node.getName().equals(nodeName))
                isPnp = true;
        }
        soapHelper_17_5.endLteSoap();
        return isPnp;
    }

    @Override
    public boolean isNetspanReachable() {
        try {
            soapHelper_17_5.getLteSoap();
        } catch (Exception e) {
            e.printStackTrace();
            report.report("Netspan is not reachable due to: " + e.getMessage(), Reporter.WARNING);
            return false;
        } finally {
            soapHelper_17_5.endLteSoap();
        }
        return true;
    }

    private boolean setNodeConfig(EnodeB enodeB, LteEnbDetailsSetWs netspanConfig) {
        try {
            if (getPnpNode(enodeB.getNetspanName()) != null) {
                GeneralUtils
                    .printToConsole("eNodeB " + enodeB.getNetspanName() + " is located in PnP list, skipping set.");
                return true;
            }
            GeneralUtils.printToConsole("Sending NBI requeset \"enbConfigSet\" for eNodeB " + enodeB.getNetspanName());
            Netspan.NBI_17_5.Lte.NodeActionResult result = soapHelper_17_5
                .getLteSoap().enbConfigSet(enodeB.getNetspanName(),null,null, netspanConfig, null, credentialsLte);
            GeneralUtils.printToConsole(String.format("NBI method \"enbConfigSet\" for eNodeB %s returned value: %s",
                enodeB.getNetspanName(), result.getErrorCode() + ":" + result.getErrorString()));
            if (result.getErrorCode() == Netspan.NBI_17_5.Lte.ErrorCodes.OK)
                return true;
            else {
                for (Netspan.NBI_17_5.Lte.NodeResult node : result.getNode()) {
                    if (node.getNodeResultCode() == Netspan.NBI_17_5.Lte.NodeResultValues.NODE_ERROR) {
                        report.report("[NMS] ERROR in " + node.getName() + ": " + node.getNodeResultString());
                    }
                }
            }
        } catch (Exception e) {
            report.report("enbConfigSet via Netspan Failed due to: " + e.getMessage(), Reporter.WARNING);
            e.printStackTrace();
            return false;
        } finally {
            soapHelper_17_5.endLteSoap();
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
     * boolean)
     */
    @Override
    public boolean changeNbrAutoX2ControlFlag(EnodeB enodeB, boolean state) {
        LteEnbDetailsSetWs netspanConfig = new LteEnbDetailsSetWs();
        ObjectFactory factory = new ObjectFactory();
        netspanConfig.setIsAutoX2ControlForNeighboursEnabled(
            factory.createLteEnbDetailsSetWsPnpParamsIsAutoX2ControlForNeighboursEnabled(state));
        return setNodeConfig(enodeB, netspanConfig);
    }

    /**
     * (non-Javadoc)
     *
     * @see Netspan.NetspanServer#changeNbrX2ConfiguratioUpdateFlag(EnodeB.EnodeB,
     * boolean)
     */
    @Override
    public boolean changeNbrX2ConfiguratioUpdateFlag(EnodeB enodeB, boolean state) {
        LteEnbDetailsSetWs netspanConfig = new LteEnbDetailsSetWs();
        ObjectFactory factory = new ObjectFactory();
        netspanConfig.setIsX2ConfigurationUpdateForNeighboursEnabled(
            factory.createLteEnbDetailsSetWsPnpParamsIsX2ConfigurationUpdateForNeighboursEnabled(state));
        return setNodeConfig(enodeB, netspanConfig);
    }

    @Override
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
            Lte3RdPartyResponse result = soapHelper_17_5.getLteSoap()
                .lte3RdPartyCreate(soapDetails, credentialsLte);
            GeneralUtils.printToConsole(
                "NBI method \"Lte3rdPartyCreate\"  returned value: " + result.getErrorCode().toString());
            if (result.getErrorCode() != Netspan.NBI_17_5.Lte.ErrorCodes.OK) {
                report.report("lte3RdPartyCreate via Netspan Failed : "
                    + result.getErrorString(), Reporter.WARNING);
                return false;
            } else
                return true;
        } catch (Exception e) {
            report.report("lte3RdPartyCreate via Netspan Failed due to: " + e.getMessage(), Reporter.WARNING);
            e.printStackTrace();
            return false;
        } finally {
            soapHelper_17_5.endLteSoap();
        }
    }

    @Override
    public boolean is3rdPartyExist(String partyName) {
        try {
            return get3rdParty(partyName).getName().equals(partyName);
        } catch (Exception e) {
            report.report("is3rdPartyExist via Netspan Failed due to: " + e.getMessage(), Reporter.WARNING);
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public String getRunningVer(String nodeName) {
        NodeSoftwareGetResult NodeSoftwareGetResult = null;
        try {
            NodeSoftwareGetResult = soapHelper_17_5.getStatusSoap()
                .nodeSoftwareStatusGet(nodeName, credentialsStatus);
            if (NodeSoftwareGetResult.getErrorCode() != Netspan.NBI_17_5.Status.ErrorCodes.OK) {
                report.report("nodeSoftwareStatusGet via Netspan Failed : " + NodeSoftwareGetResult.getErrorString(),
                    Reporter.WARNING);
                return null;
            }
            return NodeSoftwareGetResult.getSwList().getSwStatusWs().get(0).getRunningVersion();
        } catch (Exception e) {
            report.report("nodeSoftwareStatusGet via Netspan Failed due to: " + e.getMessage(), Reporter.WARNING);
            e.printStackTrace();
            return null;
        } finally {
            soapHelper_17_5.endStatusSoap();
        }
    }

    @Override
    public String getStandbyVer(String nodeName) {
        NodeSoftwareGetResult NodeSoftwareGetResult = null;
        try {
            NodeSoftwareGetResult = soapHelper_17_5.getStatusSoap()
                .nodeSoftwareStatusGet(nodeName, credentialsStatus);
            if (NodeSoftwareGetResult.getErrorCode() != Netspan.NBI_17_5.Status.ErrorCodes.OK) {
                report.report("nodeSoftwareStatusGet via Netspan Failed", Reporter.WARNING);
                report.report(NodeSoftwareGetResult.getErrorString(), Reporter.WARNING);
                return null;
            }
            return NodeSoftwareGetResult.getSwList().getSwStatusWs().get(0).getStandbyVersion();
        } catch (Exception e) {
            report.report("nodeSoftwareStatusGet via Netspan Failed due to: " + e.getMessage(), Reporter.WARNING);
            e.printStackTrace();
            return null;
        } finally {
            soapHelper_17_5.endStatusSoap();
        }
    }

    @Override
    public boolean delete3rdParty(ArrayList<String> names) {
        Lte3RdPartyResponse result = null;
        if (names.isEmpty() || names == null)
            return true;
        try {
            result = soapHelper_17_5.getLteSoap().lte3RdPartyDelete(names, credentialsLte);
            if (result.getErrorCode() != Netspan.NBI_17_5.Lte.ErrorCodes.OK) {
                report.report("lte3RdPartyDelete via Netspan Failed : "
                    + result.getLte3RdPartyResult().get(0).getResultString(), Reporter.WARNING);
                return false;
            }
            return result.getErrorCode() == Netspan.NBI_17_5.Lte.ErrorCodes.OK;
        } catch (Exception e) {
            report.report("lte3RdPartyDelete via Netspan Failed due to: " + e.getMessage(), Reporter.WARNING);
            e.printStackTrace();
            return false;
        } finally {
            soapHelper_17_5.endLteSoap();
        }
    }

    @Override
    public List<String> getAll3rdParty() {
        NameResult nameResult = null;
        try {
            nameResult = soapHelper_17_5.getLteSoap().lte3RdPartyList(credentialsLte);
            if (nameResult.getErrorCode() != Netspan.NBI_17_5.Lte.ErrorCodes.OK) {
                report.report("lte3RdPartyList via Netspan Failed : " + nameResult.getErrorString(), Reporter.WARNING);
                return new ArrayList<String>();
            }
            GeneralUtils.printToConsole("Get lte3RdPartyList via Netspan succeeded");
            return nameResult.getName();
        } catch (Exception e) {
            report.report("lte3RdPartyList via Netspan Failed due to: " + e.getMessage(), Reporter.WARNING);
            e.printStackTrace();
            return new ArrayList<String>();
        } finally {
            soapHelper_17_5.endLteSoap();
        }
    }

    @Override
    public HashMap<ConnectedUETrafficDirection, HashMap<Integer, Integer>> getUeConnectedPerCategory(EnodeB enb) {
        Netspan.NBI_17_5.Status.LteUeGetResult lteUeGetResult;
        HashMap<ConnectedUETrafficDirection, HashMap<Integer, Integer>> ret = new HashMap<>();
        HashMap<Integer, Integer> ulData = new HashMap<>();
        HashMap<Integer, Integer> dlData = new HashMap<>();
        
        try {
            lteUeGetResult = soapHelper_17_5.getStatusSoap()
                .enbConnectedUeStatusGet(enb.getNetspanName(), credentialsStatus);
            if (lteUeGetResult.getErrorCode() != Netspan.NBI_17_5.Status.ErrorCodes.OK) {
                report.report("enbConnectedUeStatusGet via Netspan Failed : " + lteUeGetResult.getErrorString(),
                    Reporter.WARNING);
                return ret;
            }
            for (Netspan.NBI_17_5.Status.LteUeStatusWs currentCell : lteUeGetResult.getCell()) {
                if (currentCell.getCellId().getValue() == enb.getCellContextID()) {
                    List<Netspan.NBI_17_5.Status.LteUeCategory> catDataList = currentCell.getCategoryData();
                	ulData.clear();
                	dlData.clear();
                    for (Netspan.NBI_17_5.Status.LteUeCategory catData : catDataList) {
                		dlData.put(catData.getCategory().getValue(), catData.getConnectedUesDl().getValue());
                		ulData.put(catData.getCategory().getValue(), catData.getConnectedUesUl().getValue());
                    }
                	ret.put(ConnectedUETrafficDirection.UL, ulData);
                	ret.put(ConnectedUETrafficDirection.DL, dlData);
                    GeneralUtils.printToConsole(
                        "enbConnectedUeStatusGet via Netspan for eNodeB " + enb.getNetspanName() + " succeeded");
                }
            }

        } catch (Exception e) {
            report.report("enbConnectedUeStatusGet via Netspan Failed due to: " + e.getMessage(), Reporter.WARNING);
            e.printStackTrace();
            return new HashMap<ConnectedUETrafficDirection, HashMap<Integer, Integer>>();
        } finally {
        	soapHelper_17_5.endStatusSoap();
        }
        return ret;
    }

    @Override
    public NodeManagementModes getManagedMode(EnodeB enb) {
        EnbDetailsGet result = getNodeConfig(enb);
        if (result == null) {
            return null;
        }
        return result.getManagedMode().getValue();
    }

    @Override
    public boolean verifyNoANRNeighbors(EnodeB enodeB) {
        LteSonAnrGetResult lteSonAnrGetResult = null;
        try {
            lteSonAnrGetResult = soapHelper_17_5.getStatusSoap()
                .enbSonAnrStatusGet(enodeB.getNetspanName(), credentialsStatus);
            if (lteSonAnrGetResult.getErrorCode() != Netspan.NBI_17_5.Status.ErrorCodes.OK) {
                report.report("enbSonAnrStatusGet via Netspan Failed : " + lteSonAnrGetResult.getErrorString(),
                    Reporter.WARNING);
                return false;
            }
            List<LteAnrStatusWs> anrNeighbors = lteSonAnrGetResult.getAnr();
            GeneralUtils.printToConsole(
                "enbSonAnrStatusGet via Netspan for eNodeB " + enodeB.getNetspanName() + " succeeded");
            return anrNeighbors.size() == 0;

        } catch (Exception e) {
            report.report("enbSonAnrStatusGet via Netspan Failed due to: " + e.getMessage(), Reporter.WARNING);
            e.printStackTrace();
            return false;
        } finally {
            soapHelper_17_5.endStatusSoap();
        }
    }

    @Override
    public List<String> getNodeNeighborsName(EnodeB node) {
        List<LteAnrStatusWs> nghs = getNeighbourList(node);
        if (nghs == null) {
            report.report("Could not pull Neighbors Name from NBIF", Reporter.WARNING);
            return new ArrayList<String>();
        }
        ArrayList<String> names = new ArrayList<String>();
        for (LteAnrStatusWs ngh : nghs) {
            names.add(ngh.getName());
        }
        return names;
    }

    @Override
    public String getCurrentNetworkProfileName(EnodeB enb) {
        EnbDetailsGet nodeConfig = getNodeConfig(enb);
        return nodeConfig != null ? nodeConfig.getNetworkProfile() : "";
    }

    @Override
    public String getCurrentSecurityProfileName(EnodeB enb) {
        EnbDetailsGet nodeConfig = getNodeConfig(enb);
        return nodeConfig != null ? nodeConfig.getSecurityProfile() : "";
    }

    @Override
    public String getCurrentSyncProfileName(EnodeB enb) {
        EnbDetailsGet nodeConfig = getNodeConfig(enb);
        return nodeConfig != null ? nodeConfig.getSyncProfile() : "";
    }

    @Override
    public boolean setNodeServiceState(EnodeB enb, EnbStates enbState) {
        Netspan.NBI_17_5.Lte.NodeActionResult result = null;
        try {
            result = soapHelper_17_5.getLteSoap()
                .enbStateSet(enb.getNetspanName(), enbState,null, credentialsLte);
        } catch (Exception e) {
            report.report("enbStateSet via Netspan Failed due to: " + e.getMessage(), Reporter.WARNING);
            e.printStackTrace();
            return false;
        } finally {
            soapHelper_17_5.endLteSoap();
        }
        if (result.getErrorCode() != Netspan.NBI_17_5.Lte.ErrorCodes.OK) {
            report.report("enbStateSet via Netspan Failed : " + result.getErrorString(), Reporter.WARNING);
            return false;
        }

        GeneralUtils.printToConsole("enbStateSet via Netspan for eNodeB " + enb.getNetspanName() + " succeeded");
        return true;
    }

    @Override
    public String getCurrentRadioProfileName(EnodeB enb) {
        int cell = enb.getCellContextID();
        return this.getCurrentRadioProfileName(enb, cell);
    }

    @Override
    public String getCurrenteMBMSProfileName(EnodeB enb) {
        int cell = enb.getCellContextID();
        return this.getCurrenteMBMsProfileName(enb, cell);
    }

    @Override
    public String getCurrentTrafficManagementProfileName(EnodeB enb) {
        int cell = enb.getCellContextID();
        return this.getCurrentTrafficManagementProfileName(enb, cell);
    }

    @Override
    public String getCurrentCallTraceProfileName(EnodeB enb) {
        int cell = enb.getCellContextID();
        return this.getCurrentCallTraceProfileName(enb, cell);
    }
    
    @Override
    public String getCurrentManagmentProfileName(EnodeB enb) {
        EnbDetailsGet nodeConfig = getNodeConfig(enb);
        return nodeConfig != null ? nodeConfig.getManagementProfile() : "";
    }

    @Override
    public String getCurrentMobilityProfileName(EnodeB enb) {
        int cell = enb.getCellContextID();
        return this.getCurrentMobilityProfileName(enb, cell);
    }

    private String getCurrentMobilityProfileName(EnodeB enb, int cellNumber) {
        EnbDetailsGet nodeConfig = getNodeConfig(enb);
        if (nodeConfig != null) {
            for (LteCellGetWs cell : nodeConfig.getLteCell()) {
                int tempCellNumber = Integer.valueOf(cell.getCellNumber().getValue());
                if (tempCellNumber == cellNumber)
                    return cell.getMobilityProfile();
            }
        }
        return null;
    }

    @Override
    public String getCurrentEnbAdvancedConfigurationProfileName(EnodeB enb) {
        EnbDetailsGet nodeConfig = this.getNodeConfig(enb);
        if (nodeConfig == null)
            return null;
        else
            return nodeConfig.getAdvancedConfigProfile();
    }

    @Override
    public String getCurrentCellAdvancedConfigurationProfileName(EnodeB enb) {
        int cell = enb.getCellContextID();
        return this.getCurrentCellAdvancedConfigurationProfileName(enb, cell);
    }

    private String getCurrentCellAdvancedConfigurationProfileName(EnodeB enb, int cellNumber) {
        EnbDetailsGet nodeConfig = getNodeConfig(enb);
        if (nodeConfig != null) {
            for (LteCellGetWs cell : nodeConfig.getLteCell()) {
                int tempCellNumber = Integer.valueOf(cell.getCellNumber().getValue());
                if (tempCellNumber == cellNumber)
                    return cell.getCellAdvancedConfigurationProfile();
            }
        }
        return null;
    }

    @Override
    public String getCurrentSystemDefaultProfileName(EnodeB enb) {
        EnbDetailsGet nodeConfig = this.getNodeConfig(enb);
        if (nodeConfig != null) {
            return nodeConfig.getSystemDefaultProfile();
        } else {
            return null;
        }
    }

    @Override
    public boolean getNetworkProfileResult(String profileName, NetworkParameters np) {
        LteNetworkProfileGetResult result = null;
        List<String> profilesName = new ArrayList<String>();
        profilesName.add(profileName);
        try {
            result = soapHelper_17_5.getLteSoap().networkProfileGet(profilesName,null,null,
                credentialsLte);
            // add parameters needed from profile to NetworkParameters Class for
            // future use.
            if (result.getErrorCode() != Netspan.NBI_17_5.Lte.ErrorCodes.OK) {
                report.report("networkProfileGet via Netspan Failed : " + result.getErrorString(), Reporter.WARNING);
                return false;
            }
            for (LtePlmnEntryWs item : result.getNetworkProfileResult().get(0).getNetworkProfile().getPlmnList()
                .getPlmn()) {
                np.addPLMN(item.getMcc(), item.getMnc());
            }
            np.setProfileName(profileName);

        } catch (Exception e) {
            report.report("networkProfileGet via Netspan Failed due to: " + e.getMessage(), Reporter.WARNING);
            e.printStackTrace();
            return false;
        } finally {
            soapHelper_17_5.endLteSoap();
        }
        return true;
    }

    @Override
    public NodeInfo getNodeDetails(EnodeB enodeB) {
        if (isPnpNode(enodeB.getNetspanName())) {
            return null;
        }

        NodeInfo returnObject = new NodeInfo();
        ArrayList<String> nodeListName = new ArrayList<String>();
        nodeListName.add(enodeB.getNetspanName());
        NodeDetailGetResult result = null;
        try {
            result = soapHelper_17_5.getInventorySoap().nodeInfoGet(nodeListName,
                new ArrayList<String>(),null, credentialsInventory);
        } catch (Exception e) {
            report.report("nodeInfoGet via Netspan Failed due to: " + e.getMessage(), Reporter.WARNING);
            e.printStackTrace();
            return null;
        } finally {
            soapHelper_17_5.endInventorySoap();
        }
        if (result.getErrorCode() != Netspan.NBI_17_5.Inventory.ErrorCodes.OK) {
            report.report("nodeInfoGet via Netspan Failed : " + result.getErrorString(), Reporter.WARNING);
            return null;
        } else {
            returnObject.productCode = result.getNodeDetail().get(FIRST_ELEMENT).getNodeInfo().getProductCode();
            returnObject.hardwareType = result.getNodeDetail().get(FIRST_ELEMENT).getNodeInfo().getHardwareType();
            returnObject.nodeID = result.getNodeDetail().get(FIRST_ELEMENT).getNodeInfo().getNodeId();
            returnObject.snmpPort = result.getNodeDetail().get(FIRST_ELEMENT).getNodeInfo().getSnmpAgent()
                .get(FIRST_ELEMENT).getSnmpPort();
        }
        GeneralUtils.printToConsole("nodeInfoGet via Netspan for eNodeB " + enodeB.getNetspanName() + " succeeded");
        return returnObject;
    }

    @Override
    public List<RFStatus> getRFStatus(EnodeB enodeB) {
        List<RFStatus> returnObject = new ArrayList<>();
        LteRfGetResult rfStatusResult = null;
        try {
            rfStatusResult = soapHelper_17_5.getStatusSoap().enbRfStatusGet(enodeB.getNetspanName(),
                credentialsStatus);
        } catch (Exception e) {
            report.report("enbRfStatusGet via Netspan Failed due to: " + e.getMessage(), Reporter.WARNING);
            e.printStackTrace();
            return new ArrayList<RFStatus>();
        } finally {
            soapHelper_17_5.endStatusSoap();
        }
        if (rfStatusResult.getErrorCode() != Netspan.NBI_17_5.Status.ErrorCodes.OK) {
            report.report("enbRfStatusGet via Netspan Failed : " + rfStatusResult.getErrorString(), Reporter.WARNING);
            return new ArrayList<RFStatus>();
        } else {
            for (LteRfStatusWs rfStatusCurr : rfStatusResult.getRfStatus()) {
                RFStatus newRfStatus = new RFStatus();
                newRfStatus.ActualTxPowerDbm = GeneralUtils.tryParseStringToFloat(rfStatusCurr.getActualTxPowerDbm());
                newRfStatus.ConfiguredTxPowerDbm = GeneralUtils
                    .tryParseStringToFloat(rfStatusCurr.getConfiguredTxPowerDbm());
                newRfStatus.ActualTxPower = rfStatusCurr.getActualTxPowerDbm();
                newRfStatus.ConfiguredTxPower = rfStatusCurr.getConfiguredTxPowerDbm();
                if (rfStatusCurr.getMeasuredVswr() != null && rfStatusCurr.getMeasuredVswr().contains("(")) {
                    newRfStatus.MeasuredVswr = rfStatusCurr.getMeasuredVswr().split("\\(")[0];
                }
                newRfStatus.OperationalStatus = rfStatusCurr.getOperationalStatus();
                newRfStatus.RfNumber = rfStatusCurr.getRfNumber();
                returnObject.add(newRfStatus);
            }
        }
        GeneralUtils.printToConsole("enbRfStatusGet via Netspan for eNodeB " + enodeB.getNetspanName() + " succeeded");
        return returnObject;
    }

    @Override
    public String getMMEIpAdress(EnodeB enb) {
        EnbDetailsGet temp = getNodeConfig(enb);
        if (temp == null)
            return null;
        EnbNetworkProfile network = networkProfileGet(temp.getNetworkProfile());
        String mmeIp = network.getS1X2List().getS1().get(0).getMmeIpAddress();
        return mmeIp;
    }

    @Override
    public ArrayList<String> getMMEIpAdresses(EnodeB enb) {
        ArrayList<String> mmeIps = new ArrayList<>();
        EnbDetailsGet temp = getNodeConfig(enb);
        if (temp == null)
            return null;
        EnbNetworkProfile network = networkProfileGet(temp.getNetworkProfile());
        for (LteS1EntryWs entry : network.getS1X2List().getS1()) {
            mmeIps.add(entry.getMmeIpAddress());
        }
        return mmeIps;
    }

    @Override
    public String getPTPInterfaceIP(EnodeB enb) {
        EnbDetailsGet result = this.getNodeConfig(enb);
        if (result == null)
            return null;

        return result.getPtpSlaveIpAddress();
    }

    @Override
    public String getGMIP(EnodeB dut) {
        LteSyncProfileResult result = this.getSyncProfile(dut);

        if (result == null)
            return null;

        EnbSyncProfile sync = result.getSyncProfile();

        if (sync == null)
            return null;

        return sync.getPrimaryMasterIpAddress();
    }

    @Override
    public PTPStatus getPTPStatus(EnodeB dut) {
        // NodePtpGet
        PTPStatus returnObject = new PTPStatus();
        NodePtpGetResult ptpStatus;
        try {
            ptpStatus = soapHelper_17_5.getStatusSoap().nodePtpGet(dut.getNetspanName(),
                credentialsStatus);
        } catch (Exception e) {
            report.report("nodePtpGet via Netspan Failed due to: " + e.getMessage(), Reporter.WARNING);
            e.printStackTrace();
            return null;
        } finally {
            soapHelper_17_5.endStatusSoap();
        }
        if (ptpStatus.getErrorCode() != Netspan.NBI_17_5.Status.ErrorCodes.OK) {
            report.report("nodePtpGet via Netspan Failed : " + ptpStatus.getErrorString(), Reporter.WARNING);
            return null;
        }
        if (ptpStatus.getPtpStatus() == null) {
            report.report("Enb does not have a PTP status", Reporter.WARNING);
            return null;
        }
        returnObject.enbName = ptpStatus.getPtpStatus().getName();
        returnObject.nodeId = ptpStatus.getPtpStatus().getNodeId();
        returnObject.masterConnectivity = ptpStatus.getPtpStatus().getMasterConnectivity();
        returnObject.syncStatus = ptpStatus.getPtpStatus().getSyncStatus();
        returnObject.holdover = ptpStatus.getPtpStatus().getHoldover();
        returnObject.holdExpiredTime = ptpStatus.getPtpStatus().getHoldExpiredTime();
        returnObject.holdoverExpired = ptpStatus.getPtpStatus().getHoldoverExpired();
        returnObject.activeMasterStatus = ptpStatus.getPtpStatus().getActiveMasterStatus().getValue().toString();

        GeneralUtils.printToConsole("nodePtpGet via Netspan for eNodeB " + dut.getNetspanName() + " succeeded");
        return returnObject;
    }

    @Override
    public boolean deleteAllAlarmsNode(EnodeB dut) {
        // AlarmListNode
        // AlarmDelete
        AlarmResultList alarmsList;
        List<String> argsNodeName = new ArrayList<String>();
        argsNodeName.add(dut.getNetspanName());
        try {
            alarmsList = soapHelper_17_5.getFaultManagementSoap().alarmListNode(argsNodeName, null,
                credentialsFaultManagement);
            List<Alarm> alarms = alarmsList.getAlarm();
            for (Alarm alarm : alarms) {
                if (!this.deleteAlarmById(alarm.getAlarmId().toString())) {
                    return false;
                }
            }
        } catch (Exception e) {
            report.report("deleteAllAlarmsNode via Netspan Failed due to: " + e.getMessage(), Reporter.WARNING);
            e.printStackTrace();
            return false;
        } finally {
            soapHelper_17_5.endFaultManagementSoap();
        }
        report.report("Deleting all alarms of " + dut.getNetspanName() + " via Netspan succeeded");
        return true;
    }

    @Override
    public List<AlarmInfo> getAllAlarmsNode(EnodeB dut) {
        // AlarmListNode
        AlarmResultList alarmsList;
        List<String> argsNodeName = new ArrayList<String>();
        argsNodeName.add(dut.getNetspanName());
        List<AlarmInfo> ret = new ArrayList<AlarmInfo>();

        try {
            alarmsList = soapHelper_17_5.getFaultManagementSoap().alarmListNode(argsNodeName, null,
                credentialsFaultManagement);
        } catch (Exception e) {
            report.report("alarmListNode via Netspan Failed due to: " + e.getMessage(), Reporter.WARNING);
            e.printStackTrace();
            return ret;
        } finally {
            soapHelper_17_5.endFaultManagementSoap();
        }
        if (alarmsList.getErrorCode() != Netspan.NBI_17_5.FaultManagement.ErrorCodes.OK) {
            report.report("alarmListNode via Netspan Failed : " + alarmsList.getErrorString(), Reporter.WARNING);
            return ret;
        }
        for (Alarm alarm : alarmsList.getAlarm()) {
            AlarmInfo temp = new AlarmInfo();
            temp.alarmId = alarm.getAlarmId();
            temp.alarmType = alarm.getAlarmType();
            temp.alarmTypeId = alarm.getAlarmTypeId();
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
        GeneralUtils.printToConsole("alarmListNode via Netspan for eNodeB " + dut.getNetspanName() + " succeeded");
        return ret;
    }

    @Override
    public boolean deleteAlarmById(String alarmId) {
        // AlarmDelete
        AlarmActionResult deleteResult;
        List<BigInteger> argsAlarmId = new ArrayList<BigInteger>();
        try {
            argsAlarmId.add(BigInteger.valueOf(Long.valueOf(alarmId)));
            deleteResult = soapHelper_17_5.getFaultManagementSoap().alarmDelete(argsAlarmId,
                credentialsFaultManagement);
            if (deleteResult.getErrorCode() != Netspan.NBI_17_5.FaultManagement.ErrorCodes.OK) {
                report.report("alarmDelete with id: " + alarmId + " via Netspan Failed", Reporter.WARNING);
                report.report(deleteResult.getErrorString(), Reporter.WARNING);
                return false;
            }
        } catch (Exception e) {
            report.report("alarmDelete with id: " + alarmId + " via Netspan Failed", Reporter.WARNING);
            e.printStackTrace();
            return false;
        } finally {
            soapHelper_17_5.endFaultManagementSoap();
        }
        GeneralUtils.printToConsole("alarmDelete with id: " + alarmId + " via Netspan succeeded");
        return true;
    }

    @Override
    public List<EventInfo> getAllEventsNode(EnodeB dut) {
        List<EventInfo> eventsResponse = this.getEventsNodeByDateRange(dut, null, null);
        return eventsResponse;
    }

    @Override
    public List<EventInfo> getAllEventsNode(EnodeB dut, Date startTime) {
        List<EventInfo> eventsResponse = this.getEventsNodeByDateRange(dut, startTime, null);
        return eventsResponse;
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
            events = soapHelper_17_5.getFaultManagementSoap().eventListNode(nodeNames, null, start,
                end, credentialsFaultManagement);
        } catch (Exception e) {
            report.report("getEventsNode via netspan Failed due to: " + e.getMessage(), Reporter.WARNING);
            e.printStackTrace();
            return eventsList;

        } finally {
            soapHelper_17_5.endFaultManagementSoap();
        }
        if (events.getErrorCode() != Netspan.NBI_17_5.FaultManagement.ErrorCodes.OK) {
            report.report("getEventsNode via netspan Return Error: " + events.getErrorCode());
            report.report(events.getErrorString());
            return eventsList;
        }
        for (Event event : events.getEvent()) {
            EventInfo newEvent = new EventInfo();
            newEvent.setEventId(event.getEventId().getValue());
            newEvent.setEventInfo(event.getEventInfo());
            newEvent.setEventType(event.getEventType());
            newEvent.setEventTypeId(event.getEventTypeId().getValue());
            newEvent.setReceivedTime(event.getReceivedTime().getValue());
            newEvent.setSourceId(event.getSourceId());
            newEvent.setSourceIfIndex(event.getSourceIfIndex().getValue());
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
    public boolean changeCurrentRadioProfileWithoutClone(EnodeB dut, RadioParameters rp) {
        EnbRadioProfile radioProfile = createCellEnbRadioProfile(rp);
        try {
            ProfileResponse response = soapHelper_17_5.getLteSoap()
                .radioProfileUpdate(rp.getProfileName(), radioProfile, credentialsLte);
            if (response.getErrorCode() != Netspan.NBI_17_5.Lte.ErrorCodes.OK) {
                report.report("changeCurrentRadioProfileWithoutClone via Netspan Failed", Reporter.WARNING);
                report.report(response.getErrorString(), Reporter.WARNING);
                return false;
            } else {
                GeneralUtils.printToConsole("changeCurrentRadioProfileWithoutClone via Netspan succeeded");
                return true;
            }
        } catch (Exception e) {
            report.report("changeCurrentRadioProfileWithoutClone via Netspan Failed due to: " + e.getMessage(),
                Reporter.WARNING);
            e.printStackTrace();
            return false;
        } finally {
            soapHelper_17_5.endLteSoap();
        }
    }

    private EnbRadioProfile createCellEnbRadioProfile(RadioParameters radioParams) {
        EnbRadioProfile radioProfile = new EnbRadioProfile();
        ObjectFactory factoryDetails = new ObjectFactory();

        if (radioParams.getProfileName() == null) {
            return new EnbRadioProfile();
        } else {
            radioProfile.setName(radioParams.getProfileName());
        }

        if (radioParams.getEarfcn() != null) {
            radioProfile.setEarfcn(factoryDetails.createEnbRadioProfileParamsEarfcn(radioParams.getEarfcn()));
        }

        if (radioParams.getDownLinkFrequency() != null) {
            radioProfile.setDownlinkFreqKHz(
                factoryDetails.createEnbRadioProfileParamsDownlinkFreqKHz(radioParams.getDownLinkFrequency()));
        }

        if (radioParams.getUpLinkFrequency() != null) {
            radioProfile.setUplinkFreqKHz(
                factoryDetails.createEnbRadioProfileParamsUplinkFreqKHz(radioParams.getUpLinkFrequency()));
        }

        if (radioParams.getFrameConfig() != null) {
            TddFrameConfigurationsSupported frameConfiguration;
            if (radioParams.getFrameConfig().equals("1")) {
                frameConfiguration = TddFrameConfigurationsSupported.DL_40_UL_40_SP_20;
            } else {
                frameConfiguration = TddFrameConfigurationsSupported.DL_60_UL_20_SP_20;
            }
            radioProfile.setFrameConfig(factoryDetails.createEnbRadioProfileParamsFrameConfig(frameConfiguration));
        }

        if (radioParams.getBand() != null) {
            radioProfile.setBand(factoryDetails.createEnbRadioProfileParamsBand(radioParams.getBand()));
        }

        if (radioParams.getAdditionalSpectrumEmission() != null) {
            AddlSpectrumEmissions addSpecValue = null;
            switch (radioParams.getAdditionalSpectrumEmission()) {
                case "NS 01": {
                    addSpecValue = AddlSpectrumEmissions.NS_01;
                    break;
                }
                case "NS 04": {
                    addSpecValue = AddlSpectrumEmissions.NS_04;
                    break;
                }
                case "NS 12": {
                    addSpecValue = AddlSpectrumEmissions.NS_12;
                    break;
                }
                case "NS 13": {
                    addSpecValue = AddlSpectrumEmissions.NS_13;
                    break;
                }
            }
            radioProfile
                .setAddlSpectrumEmission(factoryDetails.createEnbRadioProfileParamsAddlSpectrumEmission(addSpecValue));
        }

        if (radioParams.getDuplex() != null) {
            DuplexModeTypes duplex = radioParams.getDuplex().equals("TDD") ? DuplexModeTypes.TDD
                : DuplexModeTypes.FDD;
            radioProfile.setDuplexMode(factoryDetails.createEnbRadioProfileParamsDuplexMode(duplex));
        }

        if (radioParams.getBandwidth() != null) {
            radioProfile.setBandwidthMhz(factoryDetails.createEnbRadioProfileParamsBandwidthMhz(radioParams.getBandwidth()));
        }

        if (radioParams.getMfbi() != null) {
            radioProfile.setMfbiEnabled(factoryDetails.createEnbRadioProfileParamsMfbiEnabled(radioParams.getMfbi()));
        }

        if (radioParams.getTxpower() != null) {
            radioProfile.setTxPower(factoryDetails.createEnbRadioProfileParamsTxPower(radioParams.getTxpower()));
        }

        if (radioParams.getRmMode() != null) {
            ResourceManagementTypes rmMode = radioParams.getRmMode() ? ResourceManagementTypes.SFR
                : ResourceManagementTypes.DISABLED;
            radioProfile.setRmMode(factoryDetails.createEnbRadioProfileParamsRmMode(rmMode));

            if (radioParams.getRmMode()) {
                if (radioParams.getSFRSegment() != null) {
                    radioProfile.setSfrSegments(
                        factoryDetails.createEnbRadioProfileParamsSfrSegments(radioParams.getSFRSegment()));
                }

                if (radioParams.getSFRIndex() != null) {
                    radioProfile.setSfrIndex(factoryDetails.createEnbRadioProfileParamsSfrIndex(radioParams.getSFRIndex()));
                }

                if (radioParams.getSFRThresholdType() != null) {
                    SfrThresholdTypes sfrType = SfrThresholdTypes.STATIC;
                    radioProfile.setSfrThresholdType(factoryDetails.createEnbRadioProfileParamsSfrThresholdType(sfrType));
                }

                if (radioParams.getSFRThresholdValue() != null) {
                    radioProfile.setSfrThresholdValue(
                        factoryDetails.createEnbRadioProfileParamsSfrThresholdValue(radioParams.getSFRThresholdValue()));
                }

                if (radioParams.getSFRMaxMCSCellEdge() != null) {
                    radioProfile.setSfrMaxMcsCellEdge(
                        factoryDetails.createEnbRadioProfileParamsSfrMaxMcsCellEdge(radioParams.getSFRMaxMCSCellEdge()));
                }

                if (radioParams.getSFRMinMCSCellEdge() != null) {
                    radioProfile.setSfrMinMcsCellCenter(factoryDetails
                        .createEnbRadioProfileParamsSfrMinMcsCellCenter(radioParams.getSFRMinMCSCellEdge()));
                }
            }
        }

        if (radioParams.getECIDMode() != null) {
            EnabledStates state = radioParams.getECIDMode() ? EnabledStates.ENABLED
                : EnabledStates.DISABLED;
            radioProfile.setEcidMode(factoryDetails.createEnbRadioProfileParamsEcidMode(state));
            if (radioParams.getECIDMode()) {
                radioProfile.setEcidTimer(
                    factoryDetails.createEnbRadioProfileParamsEcidTimer(radioParams.getECIDProcedureTimer()));
            }
        }
        return radioProfile;
    }

    @Override
    public boolean addNeighbor(EnodeB enodeB, EnodeB neighbor, HoControlStateTypes hoControlStatus,
                               X2ControlStateTypes x2ControlStatus, HandoverType HandoverType, boolean isStaticNeighbor,
                               String qOffsetRange) {
        List<LteAddNeighbourWs> neighbourConfigList = new ArrayList<>();
        LteAddNeighbourWs neighbourConfig = new LteAddNeighbourWs();
        ObjectFactory factoryDetails = new ObjectFactory();
        
        neighbourConfig.setHandoverType(factoryDetails.createLteAddNeighbourWsHandoverType(HandoverType));
        neighbourConfig.setHoControlState(factoryDetails.createLteAddNeighbourWsHoControlState(hoControlStatus));
        neighbourConfig.setIsStaticNeighbour(factoryDetails.createLteAddNeighbourWsIsStaticNeighbour(isStaticNeighbor));
        neighbourConfig.setNeighbourName(neighbor.getNetspanName());
        neighbourConfig.setNodeName(enodeB.getNetspanName());
        if (qOffsetRange != null) {
            neighbourConfig.setQOffsetRange(factoryDetails.createLteAddNeighbourWsQOffsetRange(qOffsetRange));
        }
        neighbourConfig.setX2ControlState(factoryDetails.createLteAddNeighbourWsX2ControlState(x2ControlStatus));
        assignByCellNumber(enodeB, neighbor, neighbourConfig, factoryDetails);
        neighbourConfigList.add(neighbourConfig);
        return addNeighbourRetries(enodeB, neighbor, neighbourConfigList,3);
    }

    /**
     *  Loop of Retries to "add Neighbour" via netspan
     *
     * @param neighbourConfigList - neighbourConfigList
     * @return - true if succeed
     */
    private boolean addNeighbourRetries(EnodeB enodeB, EnodeB neighbor, List<LteAddNeighbourWs> neighbourConfigList,
										int numOfretries) {
        try {
            for (int i = 1; true; i++) {
                LteNeighbourResponse result = soapHelper_17_5.getLteSoap()
                    .lteNeighbourAddByCellNumber(neighbourConfigList, credentialsLte);
                if (result.getErrorCode() != Netspan.NBI_17_5.Lte.ErrorCodes.OK) {
                    report.report("lteNeighbourAdd via Netspan Failed : " + result.getLteNeighbourResult().get(0).getResultString(), Reporter.WARNING);
                    if (i == numOfretries || !result.getErrorString().contains("deadlocked")) {
                        return false;
                    }
                } else
                    break;
            }
        } catch (Exception e) {
            report.report("lteNeighbourAdd via Netspan Failed due to: " + e.getMessage(), Reporter.WARNING);
            e.printStackTrace();
            return false;
        } finally {
            soapHelper_17_5.endLteSoap();
        }
        GeneralUtils.printToConsole("lteNeighbourAdd via Netspan succeeded. added neighbor " + neighbor.getNetspanName()
            + " with Cell ID=" + neighbor.getCellContextID() + " for eNodeB " + enodeB.getNetspanName()
            + " with Cell ID=" + enodeB.getCellContextID());
        return true;
    }

    /**
     * Set "Assign By cell number" WS field, of neighbour and node, and add it to  AssignByCellNumber List.
     *
     * @param enodeB - enodeB
     * @param neighbor - neighbor
     * @param neighbourConfig - parent object.
     * @param factoryDetails - factoryDetails instance
     */
    private void assignByCellNumber(EnodeB enodeB, EnodeB neighbor, LteAddNeighbourWs neighbourConfig, ObjectFactory factoryDetails) {
        List<LteAddNeighbourForCellWs> cellMapping = neighbourConfig.getAssignByCellNumber();
        LteAddNeighbourForCellWs assignByCellNumber = setNodeAndNeighbourCellNumbers(enodeB, neighbor, factoryDetails);
        cellMapping.add(assignByCellNumber);
    }

    /**
     * Set Node And Neighbour Cell Numbers
     *
     * @param enodeB - enodeB
     * @param neighbor - neighbor
     * @param factoryDetails - the instance of factoryDetails
     */
    private LteAddNeighbourForCellWs  setNodeAndNeighbourCellNumbers(EnodeB enodeB, EnodeB neighbor, ObjectFactory factoryDetails) {
        LteAddNeighbourForCellWs assignByCellNumber = new LteAddNeighbourForCellWs();
        assignByCellNumber.setNodeCellNumber(
                factoryDetails.createLteAddNeighbourForCellWsNodeCellNumber(enodeB.getCellContextID()));
        assignByCellNumber.setNeighbourCellNumber(
            factoryDetails.createLteAddNeighbourForCellWsNeighbourCellNumber(neighbor.getCellContextID()));
        return assignByCellNumber;
    }

    @Override
    public boolean checkCannotAddNeighbor(EnodeB enodeB, EnodeB neighbor, HoControlStateTypes hoControlStatus,
                                          X2ControlStateTypes x2ControlStatus, HandoverType HandoverType, boolean isStaticNeighbor,
                                          String qOffsetRange) {
        int enbNumbersOfCells = this.getNumberOfNetspanCells(enodeB);
        int nbrNumbersOfCells = this.getNumberOfNetspanCells(neighbor);
        String sourceNodeName = enodeB.getNetspanName();
        String neighborName = neighbor.getNetspanName();

        if (enbNumbersOfCells == nbrNumbersOfCells && enbNumbersOfCells == 2) {
            return (this.addNeighbourMultiCell(enodeB, neighbor, hoControlStatus, x2ControlStatus, HandoverType,
                isStaticNeighbor, qOffsetRange));
        } else {
            try {
                for (int i = 1; i <= 3; i++) {
                    LteNeighbourResponse result = soapHelper_17_5.getLteSoap().lteNeighbourAdd(
                        sourceNodeName, neighborName, hoControlStatus, x2ControlStatus, HandoverType,
                        isStaticNeighbor, qOffsetRange, String.valueOf(enodeB.getCellContextID()), credentialsLte);
                    if (result.getErrorCode() != Netspan.NBI_17_5.Lte.ErrorCodes.OK) {
                        report.report("lteNeighbourAdd via Netspan Failed : " + result.getErrorString());
                        if (i == 3 || !result.getErrorString().contains("deadlocked")) {
                            return true;
                        }
                    }
                }
            } catch (Exception e) {
                report.report("lteNeighbourAdd via Netspan Failed due to: " + e.getMessage(), Reporter.WARNING);
                e.printStackTrace();
                return false;
            } finally {
                soapHelper_17_5.endLteSoap();
            }
            report.report("lteNeighbourAdd via Netspan succeeded. added neighbor: " + neighbor.getNetspanName()
                + " for eNodeB: " + enodeB.getNetspanName(), Reporter.WARNING);
            return false;
        }
    }

    /** Add Neighnour multi cell
     * @param enodeB
     * @param neighbor
     * @param hoControlStatus
     * @param x2ControlStatus
     * @param HandoverType
     * @param isStaticNeighbor
     * @param qOffsetRange
     * @return
     */
    @Override
    public boolean addNeighbourMultiCell(EnodeB enodeB, EnodeB neighbor, HoControlStateTypes hoControlStatus,
                                         X2ControlStateTypes x2ControlStatus, HandoverType HandoverType, boolean isStaticNeighbor,
                                         String qOffsetRange) {
        ObjectFactory factoryDetails = new ObjectFactory();
        List<LteAddNeighbourWs> neighbourConfigList = new ArrayList<>();
        LteAddNeighbourWs neighbourConfig = new LteAddNeighbourWs();
        neighbourConfig.setHandoverType(factoryDetails.createLteAddNeighbourWsHandoverType(HandoverType));
        neighbourConfig.setHoControlState(factoryDetails.createLteAddNeighbourWsHoControlState(hoControlStatus));
        neighbourConfig.setIsStaticNeighbour(factoryDetails.createLteAddNeighbourWsIsStaticNeighbour(isStaticNeighbor));
        neighbourConfig.setNeighbourName(neighbor.getNetspanName());
        neighbourConfig.setNodeName(enodeB.getNetspanName());
        if (qOffsetRange != null)
            neighbourConfig.setQOffsetRange(factoryDetails.createLteAddNeighbourWsQOffsetRange(qOffsetRange));
        neighbourConfig.setX2ControlState(factoryDetails.createLteAddNeighbourWsX2ControlState(x2ControlStatus));
        List<LteAddNeighbourForCellWs> cellMapping = neighbourConfig.getAssignByCellNumber();
        LteAddNeighbourForCellWs firstCell = new LteAddNeighbourForCellWs();
        firstCell.setNeighbourCellNumber(factoryDetails.createLteAddNeighbourForCellWsNeighbourCellNumber(1));
        firstCell.setNodeCellNumber(factoryDetails.createLteAddNeighbourForCellWsNodeCellNumber(1));
        LteAddNeighbourForCellWs secCell = new LteAddNeighbourForCellWs();
        secCell.setNeighbourCellNumber(factoryDetails.createLteAddNeighbourForCellWsNeighbourCellNumber(2));
        secCell.setNodeCellNumber(factoryDetails.createLteAddNeighbourForCellWsNodeCellNumber(2));
        cellMapping.add(firstCell);
        cellMapping.add(secCell);
        neighbourConfigList.add(neighbourConfig);
        try {
            GeneralUtils.printToConsole("Netspan func:lteNeighbourAddByCellNumber for eNodeB:" + enodeB.getNetspanName()
                + " neighbor:" + neighbor.getNetspanName());
            LteNeighbourResponse result = soapHelper_17_5.getLteSoap()
                .lteNeighbourAddByCellNumber(neighbourConfigList, credentialsLte);
            GeneralUtils.printToConsole("Netspan func:lteNeighbourAddByCellNumber for eNodeB:" + enodeB.getNetspanName()
                + " neighbor:" + neighbor.getNetspanName() + "returned value: "
                + result.getLteNeighbourResult().get(0).getResultString());
            if (result.getLteNeighbourResult().get(0).getResultCode() != Netspan.NBI_17_5.Lte.LteNeighbourResultValues.OK) {
                report.report("lteNeighbourAddByCellNumber via Netspan Failed", Reporter.WARNING);
                report.report(result.getErrorString(), Reporter.WARNING);
                return false;
            } else
                return true;

        } catch (Exception e) {
            report.report("lteNeighbourAddByCellNumber via Netspan Failed due to: " + e.getMessage(), Reporter.WARNING);
            e.printStackTrace();
            return false;
        } finally {
            soapHelper_17_5.endLteSoap();
        }
    }

    /**
     * Gets the neighbour list.
     *
     * @param enodeBName the EnodeB name
     * @return the neighbour list
     */
    private List<LteAnrStatusWs> getNeighbourList(EnodeB enb) {
        try {
            LteSonAnrGetResult result = soapHelper_17_5.getStatusSoap()
                .enbSonAnrStatusGet(enb.getNetspanName(), credentialsStatus);
            if (result.getErrorCode() != Netspan.NBI_17_5.Status.ErrorCodes.OK) {
                report.report("enbSonAnrStatusGet via Netspan Failed : " + result.getErrorString(), Reporter.WARNING);
                return new ArrayList<LteAnrStatusWs>();
            } else {
                GeneralUtils.printToConsole("enbSonAnrStatusGet via Netspan succeeded");
                return result.getAnr();
            }
        } catch (Exception e) {
            report.report("enbSonAnrStatusGet via Netspan Failed due to: " + e.getMessage(), Reporter.WARNING);
            e.printStackTrace();
            return new ArrayList<LteAnrStatusWs>();
        } finally {
            soapHelper_17_5.endStatusSoap();
        }
    }

    /**
     * Gets the sync profile.
     *
     * @param node the node
     * @return the sync profile
     */
    private LteSyncProfileResult getSyncProfile(EnodeB node) {
        GeneralUtils.printToConsole("trying to get Sync profiles via NBI");
        List<String> profiles = new ArrayList<String>();
        LteSyncProfileGetResult returnObject = null;

        if (getNodeConfig(node) != null)
            profiles.add(getNodeConfig(node).getSyncProfile());
        try {
            returnObject = soapHelper_17_5.getLteSoap().syncProfileGet(profiles,null,null,
                credentialsLte);
        } catch (Exception e) {
            report.report("getSyncProfile via Netspan Failed due to: " + e.getMessage(), Reporter.WARNING);
            e.printStackTrace();
            return null;
        } finally {
            soapHelper_17_5.endLteSoap();
        }
        if (returnObject.getErrorCode() != Netspan.NBI_17_5.Lte.ErrorCodes.OK) {
            report.report("getSyncProfile via Netspan Failed : " + returnObject.getErrorString(), Reporter.WARNING);
            return null;
        }
        GeneralUtils.printToConsole("syncProfileGet via Netspan for eNodeB " + node.getNetspanName() + " succeeded");
        return returnObject.getSyncProfileResult().get(FIRST_ELEMENT);
    }

    private EnbNetworkProfile networkProfileGet(String profileName) {
        ArrayList<String> names = new ArrayList<String>();
        names.add(profileName);
        try {
            LteNetworkProfileGetResult lteNetworkProfile = soapHelper_17_5.getLteSoap()
                .networkProfileGet(names, null,null,credentialsLte);
            if (lteNetworkProfile.getErrorCode() == Netspan.NBI_17_5.Lte.ErrorCodes.OK) {
                return lteNetworkProfile.getNetworkProfileResult().get(0).getNetworkProfile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            soapHelper_17_5.endLteSoap();
        }
        return null;
    }

    /**
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

    private Enb3RdParty get3rdParty(String... partiesArray) {
        ArrayList<String> parties = new ArrayList<String>();
        for (int i = 0; i < partiesArray.length; i++)
            parties.add(partiesArray[i]);

        try {
            Lte3RdPartyGetResult result = soapHelper_17_5.getLteSoap().lte3RdPartyGet(parties,
                credentialsLte);
            GeneralUtils.printToConsole(
                "NBI method \"Lte3rdPartyCreate\"  returned value: " + result.getErrorCode().toString());
            if (result.getErrorCode() != Netspan.NBI_17_5.Lte.ErrorCodes.OK) {
                report.report("lte3RdPartyGet via Netspan Failed : " + result.getErrorString(), Reporter.WARNING);
                return null;
            }

            if (result != null && result.getLte3RdPartyResult() != null && result.getLte3RdPartyResult().get(0)
                .getResultCode() == Netspan.NBI_17_5.Lte.Lte3RdPartyResultValues.OK)
                return result.getLte3RdPartyResult().get(0).getEnb3RdParty();
            else {
                if (result == null || result.getLte3RdPartyResult() == null)
                    throw new Exception("fail to retrive a result from NBIF");
                else {
                    throw new Exception("Error String:" + result.getErrorString() + " Result String:"
                        + result.getLte3RdPartyResult().get(0).getResultString());
                }
            }
        } catch (Exception e) {
            report.report("lte3RdPartyGet via Netspan Failed due to: " + e.getMessage(), Reporter.WARNING);
            return null;
        } finally {
            soapHelper_17_5.endLteSoap();
        }
    }

    @Override
    public boolean setEnbType(String nodeName, EnbTypes type) {
    	LteEnbDetailsSetWs enbConfigSet = new LteEnbDetailsSetWs();
		ObjectFactory factoryDetails = new ObjectFactory();
		enbConfigSet.setENodeBType(factoryDetails.createEnbDetailsGetPnpENodeBType(EnbTypesWs.fromValue(type.value())));
		try {
			Netspan.NBI_17_5.Lte.NodeActionResult result = soapHelper_17_5.getLteSoap().enbConfigSet(nodeName, null,null,
					enbConfigSet, null, credentialsLte);
			if (result.getErrorCode() == Netspan.NBI_17_5.Lte.ErrorCodes.OK) {
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
			soapHelper_17_5.endLteSoap();
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

    private EnbNetworkProfile getNetworkProfile(String profileName) {
		try {
			LteNetworkProfileGetResult result = null;
			List<String> a = new ArrayList<String>();
			a.add(profileName);
			result = soapHelper_17_5.getLteSoap().networkProfileGet(a, null,null,
					credentialsLte);
			if (result.getErrorCode() != Netspan.NBI_17_5.Lte.ErrorCodes.OK) {
				report.report("networkProfileGet via Netspan Failed : " + result.getErrorString(), Reporter.WARNING);
			}
			return result.getNetworkProfileResult().get(0).getNetworkProfile();
		} catch (Exception e) {
			e.printStackTrace();
			report.report("networkProfileGet via Netspan Failed due to: " + e.getMessage(), Reporter.WARNING);
			return null;
		} finally {
			soapHelper_17_5.endLteSoap();
		}
	}
    
    @Override
    public boolean performReProvision(String nodeName) {
    	try {
			if (verifyReProvisionResponseRetries(nodeName, 10)) {
				return true;
			} else {
				report.report("performReProvision via Netspan Failed due to: Some actions are still Queued or Failed.", Reporter.WARNING);
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			report.report("performReProvision via Netspan Failed due to: " + e.getMessage(), Reporter.WARNING);
			return false;
		} finally {
			soapHelper_17_5.endInventorySoap();
		}
    }
    
    private boolean verifyReProvisionResponseRetries(String nodeName, int numOfRetries) {
		List<String> nodeList = new ArrayList<String>();
		nodeList.add(nodeName);
		boolean isReProPass = false;
		Netspan.NBI_17_5.Inventory.NodeActionResult nodeReProvisionResult = soapHelper_17_5
				.getInventorySoap().nodeReprovision(nodeList, null, credentialsInventory);
		if (nodeReProvisionResult.getErrorCode() == Netspan.NBI_17_5.Inventory.ErrorCodes.OK) {
			report.report(String.format("%s - ReProvision started.", nodeName));
			for (int i = 0; i < numOfRetries; i++) {
				NodeProvisioningGetResult statusGetResult = soapHelper_17_5
						.getInventorySoap().nodeProvisioningStatusGet(nodeList, null, credentialsInventory);
				isReProPass = verifyActionsTableIsEmpty(isReProPass, nodeReProvisionResult, statusGetResult);
				if (isReProPass) {
					report.report(String.format("%s - ReProvision succeeded.", nodeName));
					return true;
				} else {
					GeneralUtils.unSafeSleep(10000);
				}
			}
		} else {
			report.report("nodeReProvision via Netspan Failed : " + nodeReProvisionResult.getErrorString(), Reporter.WARNING);
		}
		return false;
	}

    private boolean verifyActionsTableIsEmpty(boolean reProPass, Netspan.NBI_17_5.Inventory.NodeActionResult nodeReProvisionResult, NodeProvisioningGetResult statusGetResult) {
		if (statusGetResult.getErrorCode() == Netspan.NBI_17_5.Inventory.ErrorCodes.OK) {
			NodeProvisioningGetWs getNodeProvisioningDetail = statusGetResult.getNodeResult().get(0).getNodeProvisioningDetail();
			//Get Values
			Integer queuedValue = getNodeProvisioningDetail.getConfigQueued().getValue();
			Integer pendingValue = getNodeProvisioningDetail.getConfigPending().getValue();
			Integer failedValue = getNodeProvisioningDetail.getConfigFailed().getValue();
			//Verify values
			reProPass = (queuedValue == 0);
			reProPass &= (pendingValue == 0);
			reProPass &= (failedValue == 0);
		} else {
			report.report("nodeProvisioningStatusGet via Netspan Failed : "
					+ nodeReProvisionResult.getErrorString(), Reporter.WARNING);
		}
		return reProPass;
	}
    
    @Override
    public boolean checkAndSetDefaultProfiles(EnodeB node, boolean setProfile) {
    	GeneralUtils.startLevel("checking default profiles for node : " + node.getNetspanName());
		LteEnbDetailsSetWs enbConfigSet = new LteEnbDetailsSetWs();
		LteCellSetWs lteCellSet = new LteCellSetWs();
		ObjectFactory factoryDetails = new ObjectFactory();
		if (!isNetspanReachable()) {
			GeneralUtils.stopLevel();
			return false;
		}
		try {
			DefaultNetspanProfiles enbProfiles = node.getDefaultNetspanProfiles();
			for (CellNetspanProfiles cellProfiles : enbProfiles.cellProfiles) {
				lteCellSet.setCellNumber(factoryDetails.createLteCellGetWsCellNumber(String.valueOf(cellProfiles.getCellId())));
				GeneralUtils.startLevel("checking profiles for Cell : " + String.valueOf(cellProfiles.getCellId()));
				lteCellSet = checkForExistsCellProfileAndSet17_5(enbProfiles, lteCellSet, cellProfiles, CellProfiles.Cell_Advanced_Profile, setProfile);
				lteCellSet = checkForExistsCellProfileAndSet17_5(enbProfiles, lteCellSet, cellProfiles, CellProfiles.Radio_Profile, setProfile);
				lteCellSet = checkForExistsCellProfileAndSet17_5(enbProfiles, lteCellSet, cellProfiles, CellProfiles.Mobility_Profile, setProfile);
				lteCellSet = checkForExistsCellProfileAndSet17_5(enbProfiles, lteCellSet, cellProfiles, CellProfiles.Embms_Profile, setProfile);
				lteCellSet = checkForExistsCellProfileAndSet17_5(enbProfiles, lteCellSet, cellProfiles, CellProfiles.Traffic_Management_Profile, setProfile);
				lteCellSet = checkForExistsCellProfileAndSet17_5(enbProfiles, lteCellSet, cellProfiles, CellProfiles.Call_Trace_Profile, setProfile);
				enbConfigSet.getLteCell().add(lteCellSet);
				GeneralUtils.stopLevel();
			}
			GeneralUtils.startLevel("checking profiles for node : " + node.getNetspanName());
			enbConfigSet = checkForExistsNodeProfileAndSet17_5(enbProfiles, enbConfigSet, EnbProfiles.System_Default_Profile, setProfile);
			enbConfigSet = checkForExistsNodeProfileAndSet17_5(enbProfiles, enbConfigSet, EnbProfiles.EnodeB_Advanced_Profile, setProfile);
			enbConfigSet = checkForExistsNodeProfileAndSet17_5(enbProfiles, enbConfigSet, EnbProfiles.Network_Profile, setProfile);
			enbConfigSet = checkForExistsNodeProfileAndSet17_5(enbProfiles, enbConfigSet, EnbProfiles.Sync_Profile, setProfile);
			enbConfigSet = checkForExistsNodeProfileAndSet17_5(enbProfiles, enbConfigSet, EnbProfiles.Security_Profile, setProfile);
			enbConfigSet = checkForExistsNodeProfileAndSet17_5(enbProfiles, enbConfigSet, EnbProfiles.Son_Profile, setProfile);
			enbConfigSet = checkForExistsNodeProfileAndSet17_5(enbProfiles, enbConfigSet, EnbProfiles.Management_Profile, setProfile);
			enbConfigSet = checkForExistsNodeProfileAndSet17_5(enbProfiles, enbConfigSet, EnbProfiles.Neighbour_Management_Profile, setProfile);
			GeneralUtils.stopLevel();

			if (!setProfile) {
				return true;
			}

			Netspan.NBI_17_5.Lte.NodeActionResult result = soapHelper_17_5
					.getLteSoap().enbConfigSet(node.getNetspanName(), new NodeProperty(),null, enbConfigSet,
							new SnmpDetailSetWs(), credentialsLte);

			if (result.getErrorCode() != Netspan.NBI_17_5.Lte.ErrorCodes.OK) {
				report.report("Set default profiles via Netspan Failed : " + result.getErrorString());
				List<NodeResult> nodeErrors = result.getNode();
				for (NodeResult error : nodeErrors) {
					if (error != null) {
						report.report("SubResultError:" + error.getNodeResultString(), Reporter.WARNING);
					}
				}
				return false;
			} else
				return true;
		} catch (Exception e) {
			e.printStackTrace();
			report.report("Set default profiles via Netspan Failed due to: " + e.getMessage());
			return false;
		} finally {
			soapHelper_17_5.endLteSoap();
			GeneralUtils.stopLevel();
		}
    }

    private LteCellSetWs checkForExistsCellProfileAndSet17_5(DefaultNetspanProfiles enbProfiles, LteCellSetWs lteCellSetWs, CellNetspanProfiles cellProfiles, CellProfiles profileType, boolean setProfile) {
		String profileName = cellProfiles.getProfile(profileType);
		if (profileName != null) {
			if (isProfileExists(profileName, profileType)) {
				if (setProfile) {
					lteCellSetWs = injectContentToWsCellObject17_5(lteCellSetWs, enbProfiles, cellProfiles, profileType);
				}
			} else
				report.report("Profile type : " + profileType + ", does not exist in netspan : " + this.getHostname(), Reporter.WARNING);
		}
		return lteCellSetWs;
	}
    
    private boolean isProfileExists(String profileName, CellProfiles profileType) {
		ArrayList<String> nameList = new ArrayList<String>();
		nameList.add(profileName);
		Netspan.NBI_17_5.Lte.WsResponse response = null;
		try {
			switch (profileType) {
				case Cell_Advanced_Profile:
					response = soapHelper_17_5.getLteSoap()
							.cellAdvancedConfigProfileGet(nameList,null,null, credentialsLte);
					break;
				case Radio_Profile:
					response = soapHelper_17_5.getLteSoap()
							.radioProfileGet(nameList, null,null, credentialsLte);
					break;
				case Mobility_Profile:
					response = soapHelper_17_5.getLteSoap()
							.mobilityProfileGet(nameList, null,null, credentialsLte);
					break;
				case Embms_Profile:
					response = soapHelper_17_5.getLteSoap()
							.embmsProfileGet(nameList, null,null, credentialsLte);
					break;
				case Traffic_Management_Profile:
					response = soapHelper_17_5.getLteSoap()
							.trafficManagementProfileGet(nameList, null,null, credentialsLte);
					break;
				case Call_Trace_Profile:
					response = soapHelper_17_5.getLteSoap()
							.callTraceProfileGet(nameList, null,null, credentialsLte);
					break;
				default:
					report.report("isProfileExists get error CellProfiles type: " + profileType, Reporter.WARNING);
			}
			return response.getErrorCode() == Netspan.NBI_17_5.Lte.ErrorCodes.OK;

		} catch (Exception e) {
			e.printStackTrace();
			report.report(profileType.value() + "ProfileGet Failed, due to: " + e.getMessage(), Reporter.WARNING);
			return false;
		} finally {
			soapHelper_17_5.endLteSoap();
		}
	}
    
    private LteEnbDetailsSetWs checkForExistsNodeProfileAndSet17_5(DefaultNetspanProfiles enbProfiles, LteEnbDetailsSetWs enbConfigSet, EnbProfiles profileType, boolean setProfile) {
		String profileName = enbProfiles.getDefaultProfile(profileType);
		if (profileName != null) {
			if (isProfileExists(profileName, profileType)) {
				if (setProfile) {
					enbConfigSet = injectContentToWsEnbObject(enbConfigSet, enbProfiles, profileType);
				}
			} else {
				report.report("Profile type : " + profileType + ", does not exist in netspan : " + this.getHostname(), Reporter.WARNING);
			}
		}
		return enbConfigSet;
	}
    
    private LteEnbDetailsSetWs injectContentToWsEnbObject(LteEnbDetailsSetWs enbConfigSet, DefaultNetspanProfiles enbProfiles, EnbProfiles profileType) {
		switch (profileType) {
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
				report.report("there is no Node Profile with the type : " + profileType);
		}
		return enbConfigSet;
	}
    
    private LteCellSetWs injectContentToWsCellObject17_5(LteCellSetWs lteCellSetWs, DefaultNetspanProfiles enbProfiles, CellNetspanProfiles cellProfiles, CellProfiles profileType) {
		switch (profileType) {
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
    public SONStatus getSONStatus(EnodeB dut) {
        // EnbSonPciStatusGet
        SONStatus returnObject = new SONStatus();
        LteSonPciGetResult sonPciStatus;
        LteSonRsiGetResult sonRsiStatus;
        try {
            sonPciStatus = soapHelper_17_5.getStatusSoap().enbSonPciStatusGet(dut.getNetspanName(),
                credentialsStatus);
            sonRsiStatus = soapHelper_17_5.getStatusSoap().enbSonRsiStatusGet(dut.getNetspanName(),
                credentialsStatus);
        } catch (Exception e) {
            report.report("enbSonStatusGet via Netspan Failed due to: " + e.getMessage(), Reporter.WARNING);
            e.printStackTrace();
            return null;
        } finally {
            soapHelper_17_5.endStatusSoap();
        }
        if (sonPciStatus.getErrorCode() != Netspan.NBI_17_5.Status.ErrorCodes.OK) {
            report.report("enbSonStatusGet via Netspan Failed : " + sonPciStatus.getErrorString(), Reporter.WARNING);
            return null;
        }
        returnObject.nodeName = sonPciStatus.getName();
        returnObject.nodeId = sonPciStatus.getNodeId();
        returnObject.pciStatus = sonPciStatus.getPciStatus().getValue().value();

        returnObject.PCICells = new ArrayList<PciStatusCell>();
        for (LtePciStatusWs cellTemp : sonPciStatus.getCell()) {
            PciStatusCell newPciCell = new PciStatusCell();
            newPciCell.cellId = cellTemp.getCellId().getValue();
            newPciCell.physicalLayerCellGroup = cellTemp.getPhysicalLayerCellGroup().getValue();
            newPciCell.physicalLayerIdentity = cellTemp.getPhysicalLayerIdentity().getValue();
            newPciCell.physicalCellId = cellTemp.getPhysicalCellId().getValue();
            newPciCell.pciStatus = cellTemp.getPciStatus();
            returnObject.PCICells.add(newPciCell);
        }

        for (LteRsiStatusWs cellTemp : sonRsiStatus.getCell()) {
            RsiStatusCell newRsiCell = new RsiStatusCell();
            newRsiCell.cellId = cellTemp.getCellId();
            newRsiCell.rsiStatus = cellTemp.getRsiStatus().value();
            newRsiCell.currentRsiValue = cellTemp.getCurrentRsiValue().getValue();
            newRsiCell.availableRsiRanges = cellTemp.getAvailableRsiRanges();
            returnObject.RSICell.add(newRsiCell);
        }

        GeneralUtils.printToConsole("enbSonStatusGet via Netspan for eNodeB " + dut.getNetspanName() + " succeeded");
        return returnObject;
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
			    res.emergencyAreaId = (lteCell.getEmergencyAreaIds() != null) ? lteCell.getEmergencyAreaIds().getEmergencyAreaId().get(0)
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
    public boolean setPci(EnodeB dut, int pci) {
        LteEnbDetailsSetWs enbConfigSet = new LteEnbDetailsSetWs();
        ObjectFactory objectFactory = new ObjectFactory();
        LteCellSetWs lteCellSet = new LteCellSetWs();
        try {
            lteCellSet
                .setCellNumber(objectFactory.createLteCellGetWsCellNumber(String.valueOf(dut.getCellContextID())));
            lteCellSet.setPhysicalLayerCellGroup(objectFactory.createLteCellSetWsPhysicalLayerCellGroup(pci / 3));
            lteCellSet.setPhysicalLayerIdentity(objectFactory.createLteCellSetWsPhysicalLayerIdentity(pci % 3));
            enbConfigSet.getLteCell().add(lteCellSet);

            Netspan.NBI_17_5.Lte.NodeActionResult result = soapHelper_17_5
                .getLteSoap().enbConfigSet(dut.getNetspanName(),null,null, enbConfigSet, null, credentialsLte);

            if (result.getErrorCode() != Netspan.NBI_17_5.Lte.ErrorCodes.OK) {
                report.report("setPci via Netspan Failed : " + result.getErrorString(), Reporter.WARNING);
                return false;
            } else
                return true;

        } catch (Exception e) {
            e.printStackTrace();
            report.report("setPci via Netspan Failed due to: " + e.getMessage(), Reporter.WARNING);
            return false;
        } finally {
            soapHelper_17_5.endLteSoap();
        }
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
			ProfileResponse cloneResult = soapHelper_17_5.getLteSoap()
					.managementProfileClone(cloneFromName, managementProfile, credentialsLte);

			if (cloneResult == null) {
				report.report("Fail to get Management Profile cloning result", Reporter.WARNING);
				return false;
			} else if (cloneResult.getErrorCode() == Netspan.NBI_17_5.Lte.ErrorCodes.OK) {
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
			if (managementParmas.maintenanceWindow) {
				MaintenanceWindowConfigurationWs maintenanceWindowConfigurationWs = factoryDetails
						.createMaintenanceWindowConfigurationWs();
				maintenanceWindowConfigurationWs
						.setTimeZone(factoryDetails.createMaintenanceWindowConfigurationWsTimeZone(
								TimeZones.fromValue(managementParmas.timeZone)));
				maintenanceWindowConfigurationWs.setStartTime(managementParmas.maintenanceWindowStartTime);
				maintenanceWindowConfigurationWs.setEndTime(managementParmas.maintenanceWindowEndTime);
				maintenanceWindowConfigurationWs.setMaxRandomDelay(
						factoryDetails.createRelayProfileParamsMaxRandomDelay(managementParmas.maxRandomDelayPercent));
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
			ProfileResponse cloneResult = soapHelper_17_5.getLteSoap()
					.systemDefaultProfileClone(cloneFromName, systemDefaultProfile, credentialsLte);

			if (cloneResult == null) {
				report.report("Fail to get System Default Profile cloning result", Reporter.WARNING);
				return false;
			} else if (cloneResult.getErrorCode() == Netspan.NBI_17_5.Lte.ErrorCodes.OK) {
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
		try {
			category = CategoriesLte.fromValue(removeDigitsAndSpaces(nodeConf.getHardware()));
		} catch (Exception e) {
			report.report("Incompatible HardWare Type in inner Netspan method!", Reporter.WARNING);
			GeneralUtils.printToConsole("Error : " + e.getMessage() + ", nodeConfig :" + nodeConf.getHardware());
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
			if (sonParmas.getOptimizationMode().equals(EnabledStates.ENABLED)) {
				LteSonDynIcic lteSonDynIcic = new LteSonDynIcic();
				if (sonParmas.getIcicMode() != null && sonParmas.getIcicMode().equals(EnabledStates.ENABLED)) {
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
    public boolean isRebootRequestedMaintenanceWindowStatus(EnodeB node) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean cloneEnodeBAdvancedProfile(EnodeB node, String cloneFromName,
                                              EnodeBAdvancedParameters advancedParmas) {
        try {
            ObjectFactory objectFactory = new ObjectFactory();
            EnbAdvancedProfile enbAdvancedProfile = new EnbAdvancedProfile();
            enbAdvancedProfile.setName(advancedParmas.getProfileName());
            AirSonWs airSonWs = new AirSonWs();
            airSonWs.setLowPower(objectFactory.createAirSonWsLowPower(advancedParmas.getLowPower()));
            airSonWs.setPowerStep(objectFactory.createAirSonWsPowerStep(advancedParmas.getPowerStep()));
            airSonWs.setPowerLevelTimeInterval(
                objectFactory.createAirSonWsPowerLevelTimeInterval(advancedParmas.getPowerLevelTimeInterval()));
            airSonWs.setAnrTimer(objectFactory.createAirSonWsAnrTimer(advancedParmas.getAnrTimer()));
            if (advancedParmas.getPciConfusionAllowed() != null) {
                EnabledStates state = EnabledStates
                    .fromValue(advancedParmas.getPciConfusionAllowed().value());
                airSonWs.setPciConfusionAllowed(objectFactory.createAirSonWsPciConfusionAllowed(state));
            }
            airSonWs.setInitialPciListSize(
                objectFactory.createAirSonWsInitialPciListSize(advancedParmas.getInitialPciListSize()));
            enbAdvancedProfile.setAirSon(airSonWs);

            ProfileResponse cloneResult = soapHelper_17_5.getLteSoap()
                .enbAdvancedConfigProfileClone(cloneFromName, enbAdvancedProfile, credentialsLte);

            boolean result = false;
            if (cloneResult.getErrorCode() == Netspan.NBI_17_5.Lte.ErrorCodes.OK) {
                report.report("Succeeded to clone eNodeB Advanced Configuration Profile, new profile name: "
                    + advancedParmas.getProfileName());
                result = true;
            } else {
                report.report("Failed to clone eNodeB Advanced Configuration Profile : " + cloneResult.getErrorString(),
                    Reporter.WARNING);
                result = false;
            }
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            report.report("Failed to clone eNodeB Advanced Configuration Profile, due to: " + e.getMessage(),
                Reporter.WARNING);
            return false;
        } finally {
            soapHelper_17_5.endLteSoap();
        }
    }

    @Override
    public String getCurrentSonProfileName(EnodeB enb) {
        EnbDetailsGet nodeConfig = getNodeConfig(enb);
        if (nodeConfig == null) {
            return "";
        }
        return nodeConfig.getSonProfile();
    }

    @Override
    public boolean updateNeighborManagementProfile(EnodeB node, String profileName,
                                                   NeighbourManagementParameters neighbourManagementParams) {
    	try {
			EnbNeighbourProfile neighbourProfile = parseNeighbourManagementParameters(neighbourManagementParams);
			ProfileResponse cloneResult = soapHelper_17_5.getLteSoap()
					.neighbourProfileUpdate(profileName, neighbourProfile, credentialsLte);

			if (cloneResult == null) {
				report.report("Fail to get Neighbour Management profile update result.", Reporter.WARNING);
				return false;
			} else if (cloneResult.getErrorCode() == Netspan.NBI_17_5.Lte.ErrorCodes.OK) {
				report.report(String.format("Succeeded to update Neighbour Management profile \"%s\" via Netspan.",
						profileName));
				return true;
			} else {
				report.report("Failed to update Neighbour Management profile, reason: " + cloneResult.getErrorString(),
						Reporter.WARNING);
				return false;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			report.report("neighbourProfileUpdate via Netspan Failed due to: " + e.getMessage(), Reporter.WARNING);
		} finally {
			soapHelper_17_5.endLteSoap();
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
    public boolean cloneNeighborManagementProfile(EnodeB node, String profileName,
                                                  NeighbourManagementParameters neighbourManagementParams) {
    	try {
			EnbNeighbourProfile neighbourProfile = parseNeighbourManagementParameters(neighbourManagementParams);
			ProfileResponse cloneResult = soapHelper_17_5.getLteSoap()
					.neighbourProfileClone(profileName, neighbourProfile, credentialsLte);

			if (cloneResult == null) {
				report.report("Fail to get Neighbour Manangement profile clone result.", Reporter.WARNING);
				return false;
			} else if (cloneResult.getErrorCode() == Netspan.NBI_17_5.Lte.ErrorCodes.OK) {
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
			soapHelper_17_5.endLteSoap();
		}
		return false;
    }

    @Override
    public String getNeighborManagmentProfile(String nodeName) {
    	ArrayList<String> nodeListName = new ArrayList<String>();
		nodeListName.add(nodeName);
		LteEnbConfigGetResult result = null;
		try {
			result = soapHelper_17_5.getLteSoap().enbConfigGet(nodeListName,null, credentialsLte);
		} catch (Exception e) {
			report.report("enbConfigGet via Netspan Failed due to: " + e.getMessage(), Reporter.WARNING);
			e.printStackTrace();
			return null;
		} finally {
			soapHelper_17_5.endLteSoap();
		}
		if (result.getErrorCode() != Netspan.NBI_17_5.Lte.ErrorCodes.OK) {
			GeneralUtils.printToConsole("enbConfigGet via Netspan Failed : " + result.getErrorString());
			return null;
		}
		return result.getNodeResult().get(FIRST_ELEMENT).getEnbConfig().getNeighbourProfile();
    }

    @Override
    public NeighborData getNeighborData(String nodeName, String nghName) {
    	LteSonAnrGetResult AnrResult = soapHelper_17_5.getStatusSoap().enbSonAnrStatusGet(nodeName,
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
			nghData.HandoverType = ngh.getHandoverType().getValue();
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
			} else {
				lteCellSet.setPhysicalLayerCellGroup(
						objectFactory.createLteCellSetWsPhysicalLayerCellGroup(cellProperties.physicalLayerCellGroup));
				lteCellSet.setPhysicalLayerIdentity(
						objectFactory.createLteCellSetWsPhysicalLayerIdentity(cellProperties.physicalLayerIdentity));
			}

			lteCellSet.setPrachRsi(objectFactory.createLteCellSetWsPrachRsi(cellProperties.prachRsi));
			lteCellSet.setTrackingAreaCode(
					objectFactory.createLteCellSetWsTrackingAreaCode(cellProperties.trackingAreaCode));
		    EaidsParams eaids = new EaidsParams();
		    eaids.getEmergencyAreaId().add(cellProperties.emergencyAreaId);
		    lteCellSet.setEmergencyAreaIds(eaids);
			lteCellSet.setPrachFreqOffset(
					objectFactory.createLteCellSetWsPrachFreqOffset(cellProperties.prachFreqOffset));
			lteCellSet.setCellBarringPolicy(
					objectFactory.createLteCellSetWsCellBarringPolicy(cellProperties.cellBarringPolicy));
			lteCellSet.setCsgMode(objectFactory.createLteCellSetWsCsgMode(cellProperties.closedSubscriberGroupMode));

			enbConfigSet.getLteCell().add(lteCellSet);

			Netspan.NBI_17_5.Lte.NodeActionResult result = soapHelper_17_5
					.getLteSoap().enbConfigSet(dut.getNetspanName(),null, null, enbConfigSet, null, credentialsLte);

			if (result.getErrorCode() != Netspan.NBI_17_5.Lte.ErrorCodes.OK) {
				report.report("setEnbCellProperties via Netspan Failed : " + result.getErrorString(), Reporter.WARNING);
				return false;
			} else
				return true;

		} catch (Exception e) {
			e.printStackTrace();
			report.report("setEnbCellProperties via Netspan Failed due to: " + e.getMessage(), Reporter.WARNING);
			return false;
		} finally {
			soapHelper_17_5.endLteSoap();
		}
    }

    @Override
    public boolean updateSonProfile(EnodeB node, String cloneFromName, SonParameters sonParams) {
    	try {
			EnbSonProfile sonProfile = parseSonParameters(node, sonParams);
			ProfileResponse updateResult = soapHelper_17_5.getLteSoap()
					.sonProfileUpdate(cloneFromName, sonProfile, credentialsLte);

			if (updateResult == null) {
				report.report("Fail to get Son profile update result", Reporter.WARNING);
				return false;
			} else if (updateResult.getErrorCode() == Netspan.NBI_17_5.Lte.ErrorCodes.OK) {
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
			soapHelper_17_5.endLteSoap();
		}
    }

    @Override
    public boolean createSoftwareServer(EnodeBUpgradeServer upgradeServer) {

        SwServerWs softwareServer = CreateSoftwareServerObject(upgradeServer);

        Netspan.NBI_17_5.Software.SwServerResponse result = soapHelper_17_5
            .getSoftwareSoap().softwareServerCreate(softwareServer, credentialsSoftware);
        if (result.getErrorCode() != Netspan.NBI_17_5.Software.ErrorCodes.OK) {
            report.report("softwareServerCreate via Netspan Failed : " + result.getErrorString(), Reporter.WARNING);
            soapHelper_17_5.endSoftwareSoap();
            return false;
        } else
            return true;
    }

    private SwServerWs CreateSoftwareServerObject(EnodeBUpgradeServer upgradeServer) {
        Netspan.NBI_17_5.Software.ObjectFactory objectFactory = new Netspan.NBI_17_5.Software.ObjectFactory();
        SwServerWs softwareServer = new SwServerWs();
        if (upgradeServer.getUpgradeServerName() != null) {
            softwareServer.setName(upgradeServer.getUpgradeServerName());
        }
        if (upgradeServer.getUpgradeServerIp() != null) {
        	if (upgradeServer.getUpgradeServerIp().contains(":")){
        		softwareServer.setServerIpv6Address(upgradeServer.getUpgradeServerIp());
        	}else{
        		softwareServer.setServerIpv4Address(upgradeServer.getUpgradeServerIp());        		
        	}
        }
        if (upgradeServer.getUpgradeServerProtocolType() != null) {
            softwareServer.setProtocolType(
                objectFactory.createSwServerWsProtocolType(upgradeServer.getUpgradeServerProtocolType()));
        }

        if (upgradeServer.getUpgradeServerProtocolType() != ServerProtocolType.TFTP) {
            if (upgradeServer.getUpgradeUser() != null) {
                softwareServer.setUserName(upgradeServer.getUpgradeUser());
            }
            if (upgradeServer.getUpgradePassword() != null) {
                softwareServer.setPassword(upgradeServer.getUpgradePassword());
            }
        }
        return softwareServer;
    }

    @Override
    public EnodeBUpgradeServer getSoftwareServer(String name) {
        ArrayList<String> names = new ArrayList<>();

        names.add(name);
        Netspan.NBI_17_5.Software.SwServerResponse result = soapHelper_17_5
            .getSoftwareSoap().softwareServerGet(names, credentialsSoftware);
        if (result.getErrorCode() != Netspan.NBI_17_5.Software.ErrorCodes.OK) {
            String resultStr = result.getSoftwareServerResult().get(0).getResultString();
            if (resultStr.contains("Software Server Not Found")) {
                GeneralUtils.printToConsole("Software Server Not Found");
                soapHelper_17_5.endSoftwareSoap();
                return null;
            } else {
                report.report("softwareServerGet via Netspan Failed : " + result.getErrorString(), Reporter.WARNING);
                soapHelper_17_5.endSoftwareSoap();
                return null;
            }

        } else {
            SwServerWs serverresult = result.getSoftwareServerResult().get(0).getSoftwareServer();
            String ip = serverresult.getServerIpv4Address();
            if(ip == null){
            	ip = serverresult.getServerIpv6Address();
            }
            EnodeBUpgradeServer server = new EnodeBUpgradeServer();
            server.setUpgradeServerObject(serverresult.getName(), ip,
                serverresult.getProtocolType().getValue(), serverresult.getUserName(), serverresult.getPassword());
            soapHelper_17_5.endSoftwareSoap();
            return server;
        }

    }

    @Override
    public boolean setSoftwareServer(String name, EnodeBUpgradeServer upgradeServer) {
        SwServerWs softwareServer = CreateSoftwareServerObject(upgradeServer);
        Netspan.NBI_17_5.Software.SwServerResponse result = soapHelper_17_5
            .getSoftwareSoap().softwareServerUpdate(name, softwareServer, credentialsSoftware);
        if (result.getErrorCode() != Netspan.NBI_17_5.Software.ErrorCodes.OK) {
            report.report("softwareServerUpdate via Netspan Failed : " + result.getErrorString(), Reporter.WARNING);
            soapHelper_17_5.endSoftwareSoap();
            return false;
        } else {
            soapHelper_17_5.endSoftwareSoap();
            return true;
        }
    }

    @Override
    public boolean deleteSoftwareServer(String name) {
        ArrayList<String> names = new ArrayList<>();
        Netspan.NBI_17_5.Software.SwServerResponse result = soapHelper_17_5
            .getSoftwareSoap().softwareServerDelete(names, credentialsSoftware);
        if (result.getErrorCode() != Netspan.NBI_17_5.Software.ErrorCodes.OK) {
            report.report("softwareServerDelete via Netspan Failed : " + result.getErrorString(), Reporter.WARNING);
            soapHelper_17_5.endSoftwareSoap();
            return false;
        } else {
            soapHelper_17_5.endSoftwareSoap();
            return true;
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

			if (multiCellParams.getIntraEnbLoadBalanceMode() == EnabledStates.ENABLED) {
				multiCellProfile
						.setCompositeLoadDiffMax(factoryObject.createEnbMultiCellProfileParamsCompositeLoadDiffMax(
								multiCellParams.getCompositeLoadDiffMax()));
				multiCellProfile
						.setCompositeLoadDiffMin(factoryObject.createEnbMultiCellProfileParamsCompositeLoadDiffMin(
								multiCellParams.getCompositeLoadDiffMin()));
				multiCellProfile.setCalculationInterval(factoryObject
						.createEnbMultiCellProfileParamsCalculationInterval(multiCellParams.getCalculationInterval()));
			}

			ProfileResponse cloneResult = soapHelper_17_5.getLteSoap()
					.multiCellProfileClone(cloneFromName, multiCellProfile, credentialsLte);
			if (cloneResult.getErrorCode() == Netspan.NBI_17_5.Lte.ErrorCodes.OK) {
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

    @Override
    public boolean createSoftwareImage(EnodeBUpgradeImage upgradeImage) {
    	Netspan.NBI_17_5.Software.SwImageWs softwareImage = createSoftwareImageObject(upgradeImage);
		Netspan.NBI_17_5.Software.SwFileInfoWs softwareFileInfo = createFileInfoObject(upgradeImage);
		List<Netspan.NBI_17_5.Software.SwFileInfoWs> fileList = softwareImage.getSoftwareFileInfo();
		fileList.add(softwareFileInfo);

		Netspan.NBI_17_5.Software.SwImageResponse result = soapHelper_17_5
				.getSoftwareSoap().softwareImageCreate(softwareImage, credentialsSoftware);
		if (result.getErrorCode() != Netspan.NBI_17_5.Software.ErrorCodes.OK) {
			report.report("softwareServerCreate via Netspan Failed : " + result.getErrorString(), Reporter.WARNING);
			soapHelper_17_5.endSoftwareSoap();
			return false;
		} else {
			soapHelper_17_5.endSoftwareSoap();
			return true;
		}
    }

    private SwImageWs createSoftwareImageObject(EnodeBUpgradeImage upgradeImage) {

    	SwImageWs softwareImage = new SwImageWs();
    	Netspan.NBI_17_5.Software.ObjectFactory factory = new Netspan.NBI_17_5.Software.ObjectFactory();
		if (upgradeImage.getName() != null) {
			softwareImage.setName(upgradeImage.getName());
		}
		if (upgradeImage.getHardwareCategory() != null) {
			softwareImage.setHardwareCategory(factory.createSwImageWsHardwareCategory(upgradeImage.getHardwareCategory()));
		}
		if (upgradeImage.getUpgradeServerName() != null) {
			softwareImage.setSoftwareServer(upgradeImage.getUpgradeServerName());
		}

		return softwareImage;

    }

    private SwFileInfoWs createFileInfoObject(EnodeBUpgradeImage upgradeImage) {
    	SwFileInfoWs softwareFileInfo = new SwFileInfoWs();
		Netspan.NBI_17_5.Software.ObjectFactory objectFactory = new Netspan.NBI_17_5.Software.ObjectFactory();
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
        Netspan.NBI_17_5.Software.SwImageResponse result = soapHelper_17_5
            .getSoftwareSoap().softwareImageGet(names, credentialsSoftware);
        if (result.getErrorCode() != Netspan.NBI_17_5.Software.ErrorCodes.OK) {
            String resultStr = result.getSoftwareImageResult().get(0).getResultString();
            if (resultStr.contains("Software Image Not Found")) {
                GeneralUtils.printToConsole("Software Image Not Found");
                soapHelper_17_5.endSoftwareSoap();
                return null;
            } else {
                report.report("softwareImageGet via Netspan Failed : " + result.getErrorString(), Reporter.WARNING);
                soapHelper_17_5.endSoftwareSoap();
                return null;
            }

        } else {
            Netspan.NBI_17_5.Software.SwImageWs imageResult = result.getSoftwareImageResult().get(0).getSoftwareImage();

            EnodeBUpgradeImage image = new EnodeBUpgradeImage();
            image.setName(imageResult.getName());
            image.setHardwareCategory(imageResult.getHardwareCategory().getValue());
            image.setUpgradeServerName(imageResult.getSoftwareServer());
            image.setBuildPath(imageResult.getSoftwareFileInfo().get(0).getFileNameWithPath());
            image.setVersion(imageResult.getSoftwareFileInfo().get(0).getVersion());
            soapHelper_17_5.endSoftwareSoap();
            return image;
        }
    }

    @Override
    public HardwareCategory getHardwareCategory(EnodeB node) {
    	String enbRadioProfileName = this.getCurrentRadioProfileName(node);
		LteRadioProfileGetResult netspanResult = null;
		List<java.lang.String> enbList = new ArrayList<java.lang.String>();
		enbList.add(enbRadioProfileName);
		netspanResult = soapHelper_17_5.getLteSoap().radioProfileGet(enbList,null, null,credentialsLte);
		if (netspanResult.getErrorCode() != Netspan.NBI_17_5.Lte.ErrorCodes.OK) {
			report.report("getHardwareCategory via Netspan Failed : " + netspanResult.getErrorString(),
					Reporter.WARNING);
			soapHelper_17_5.endSoftwareSoap();
			return null;
		}

		EnbRadioProfile profileresult = netspanResult.getRadioProfileResult().get(0).getRadioProfile();
		CategoriesLte category = profileresult.getHardwareCategory().getValue();
		HardwareCategory hardwareCategory = LTECategoriesToHardwareCategory(category);
		soapHelper_17_5.endSoftwareSoap();
		return hardwareCategory;
    }

    @Override
    public HardwareCategory LTECategoriesToHardwareCategory(CategoriesLte categories) {
        HardwareCategory hardwareCategory = null;
        String catStr = categories.value();

        switch (catStr) {
            case "AirSynergy":
                hardwareCategory = HardwareCategory.AIR_SYNERGY;
                break;
            case "AirVelocity":
                hardwareCategory = HardwareCategory.AIR_VELOCITY;
                break;
            case "AirVelocityFemto":
                hardwareCategory = HardwareCategory.AIR_VELOCITY_FEMTO;
                break;
            case "AirUnity":
                hardwareCategory = HardwareCategory.AIR_UNITY;
                break;
            case "AirDensity":
                hardwareCategory = HardwareCategory.AIR_DENSITY;
                break;
            case "AirSpeed":
                hardwareCategory = HardwareCategory.AIR_SPEED;
                break;
            case "AirSpeed-Relay":
                hardwareCategory = HardwareCategory.AIR_SPEED_RELAY;
                break;
            case "AirHarmony":
                hardwareCategory = HardwareCategory.AIR_HARMONY;
                break;

            default:
                report.report("Test got unknown Hardware category Type: " + catStr);
                break;
        }
        return hardwareCategory;
    }

    @Override
    public boolean softwareConfigSet(String nodeName, RequestType requestType, String softwareImageName) {
        Netspan.NBI_17_5.Software.SwConfigSetWs softwareDetails = createsoftwareDetails(requestType, softwareImageName);
        Netspan.NBI_17_5.Software.NodeActionResult result = soapHelper_17_5.getSoftwareSoap()
            .softwareConfigSet(nodeName, softwareDetails, credentialsSoftware);
        if (result.getErrorCode() != Netspan.NBI_17_5.Software.ErrorCodes.OK) {
            report.report("softwareServerUpdate via Netspan Failed : " + result.getErrorString(), Reporter.WARNING);
            soapHelper_17_5.endSoftwareSoap();
            return false;
        } else {
            soapHelper_17_5.endSoftwareSoap();
            return true;
        }
    }

    private SwConfigSetWs createsoftwareDetails(RequestType requestType, String softwareImageName) {
        Netspan.NBI_17_5.Software.ObjectFactory objectFactory = new Netspan.NBI_17_5.Software.ObjectFactory();
        Netspan.NBI_17_5.Software.SwConfigSetWs softwareDetails = new Netspan.NBI_17_5.Software.SwConfigSetWs();

        softwareDetails.setRequest(objectFactory.createSwConfigSetWsRequest(requestType));

        if (softwareImageName != "" && softwareImageName != null) {
            softwareDetails.setSoftwareImage(softwareImageName);
        }

        return softwareDetails;
    }

    @Override
    public SoftwareStatus getSoftwareStatus(String nodeName, ImageType imageType) {
    	List<java.lang.String> enbList = new ArrayList<java.lang.String>();
		enbList.add(nodeName);
		SoftwareStatusGetWs result = soapHelper_17_5.getSoftwareSoap().softwareStatusGet(enbList, credentialsSoftware);
		if (result.getErrorCode() != Netspan.NBI_17_5.Software.ErrorCodes.OK) {
			report.report("softwareStatusGet via Netspan Failed : " + result.getErrorString(), Reporter.WARNING);
			soapHelper_17_5.endSoftwareSoap();
			return null;
		} else {
			SoftwareStatus newsoftwareStatus = null;
			int numberOfSoftwareStatus = result.getNodeSoftwareStatus().get(0).getSoftwareStatus().size();
			for(int i = 0; i < numberOfSoftwareStatus; i++){
				NodeSoftwareStatus softwareStatus = result.getNodeSoftwareStatus().get(0).getSoftwareStatus().get(i);
				if((numberOfSoftwareStatus == 1) || (imageType == null) || imageType.value().equals(softwareStatus.getImageType().getValue().value())){
					newsoftwareStatus = new SoftwareStatus();
					newsoftwareStatus.ImageType = softwareStatus.getImageType().equals("Combined LTE + Relay") ? ImageType.COMBINED_LTE_RELAY.value() : softwareStatus.getImageType().getValue().value();
					newsoftwareStatus.RunningVersion = softwareStatus.getRunningVersion();
					newsoftwareStatus.StandbyVersion = softwareStatus.getStandbyVersion();
					newsoftwareStatus.LastRequested = softwareStatus.getLastRequested();
					newsoftwareStatus.NmsStatus = softwareStatus.getNmsState();
					newsoftwareStatus.NodeState = softwareStatus.getNodeState();
					break;
				}
			}
			soapHelper_17_5.endSoftwareSoap();
			return newsoftwareStatus;
		}
    }

    public boolean updateSoftwareImage(EnodeBUpgradeImage upgradeImage) {
    	Netspan.NBI_17_5.Software.SwImageWs softwareImage = createSoftwareImageObject(upgradeImage);
		Netspan.NBI_17_5.Software.SwFileInfoWs softwareFileInfo = createFileInfoObject(upgradeImage);
		List<Netspan.NBI_17_5.Software.SwFileInfoWs> fileList = softwareImage.getSoftwareFileInfo();
		fileList.add(softwareFileInfo);

		Netspan.NBI_17_5.Software.SwImageResponse result = soapHelper_17_5
				.getSoftwareSoap().softwareImageUpdate(softwareImage.getName(), softwareImage, credentialsSoftware);
		if (result.getErrorCode() != Netspan.NBI_17_5.Software.ErrorCodes.OK) {
			report.report("softwareImageUpdate via Netspan Failed : " + result.getErrorString(), Reporter.WARNING);
			soapHelper_17_5.endSoftwareSoap();
			return false;
		} else {
			soapHelper_17_5.endSoftwareSoap();
			return true;
		}
    }

    @Override
    public boolean deleteSoftwareImage(String softwareImageName) {
        List<String> images = new ArrayList<String>();
        images.add(softwareImageName);
        Netspan.NBI_17_5.Software.SwImageResponse result = soapHelper_17_5
            .getSoftwareSoap().softwareImageDelete(images, credentialsSoftware);
        if (result.getErrorCode() != Netspan.NBI_17_5.Software.ErrorCodes.OK) {
            report.report("softwareImageDelete via Netspan Failed : " + result.getErrorString(), Reporter.WARNING);
            soapHelper_17_5.endSoftwareSoap();
            return false;
        } else {
            soapHelper_17_5.endSoftwareSoap();
            return true;
        }
    }

    public int getDLTPTPerQci(String nodeName, int qci) {
    	LteIpThroughputGetResult result = soapHelper_17_5.getStatusSoap()
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
    public boolean getDicicUnmanagedInterferenceStatus(String nodeName, int nghPci) {
        report.report("getDicicUnmanagedInterferenceStatus via NBI_17_5 Failed : Try to use correct NBI version",
            Reporter.WARNING);
        return false;
    }

    @Override
    public boolean updateNetworkProfile(EnodeB node, String cloneFromName, NetworkParameters networkParams) {
    	try {
			EnbNetworkProfile networkProfile = parseNetworkParameters(node, networkParams);
			ProfileResponse updateResult = soapHelper_17_5.getLteSoap()
					.networkProfileUpdate(cloneFromName, networkProfile, credentialsLte);

			if (updateResult == null) {
				report.report("Fail to get Network profile update result", Reporter.WARNING);
				return false;
			} else if (updateResult.getErrorCode() == Netspan.NBI_17_5.Lte.ErrorCodes.OK) {
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
			soapHelper_17_5.endLteSoap();
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
		if (networkParams.getOperateBehindHenbGw() != null) {
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
    public boolean getNetspanProfile(EnodeB node, String cloneFromName, INetspanProfile profileParams) {
        try {
            switch (profileParams.getType()) {
                case Management_Profile:
                    return getManagementProfile(node, cloneFromName);

                case Mobility_Profile:
                    return getMobilityProfile(node, cloneFromName);

                case Network_Profile:
                    return getNetworkProfile(node, cloneFromName);

                case Radio_Profile:
                    return getRadioProfile(node, cloneFromName);

                case Security_Profile:
                    return getSecurityProfile(node, cloneFromName);

                case Son_Profile:
                    return getSonProfile(node, cloneFromName);

                case Sync_Profile:
                    return getSyncProfile(node, cloneFromName);

                case EnodeB_Advanced_Profile:
                    return getAdvancedConfigurationProfile(node, cloneFromName);

                case System_Default_Profile:
                	return getSystemDefaultProfile(node, cloneFromName);
                	
                case Neighbour_Management_Profile:
                	return getNeighborManagementProfile(node, cloneFromName);
                	
                case MultiCell_Profile:
                	return getMultiCellProfile(node, cloneFromName);
                default:
                    report.report("Enum: No Such EnbProfile", Reporter.WARNING);
                    return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            report.report("Error in cloneProfile: " + e.getMessage(), Reporter.WARNING);
            return false;
        }
    }

    private boolean getSystemDefaultProfile(EnodeB node, String cloneFromName) {
		List<String> names = new ArrayList<>();
		List<CategoriesLte> hardwares = new ArrayList<CategoriesLte>();
		EnbDetailsGet nodeConf = getNodeConfig(node);
		CategoriesLte category = null;
		try {
			category = CategoriesLte.fromValue(removeDigitsAndSpaces(nodeConf.getHardware()));
		} catch (Exception e) {
			report.report("Incompatible HardWare Type in inner Netspan method!", Reporter.WARNING);
			GeneralUtils.printToConsole("Error : " + e.getMessage() + ", nodeConfig :" + nodeConf.getHardware());
			return false;
		}
		hardwares.add(category);
		names.add(cloneFromName);
		try {
			soapHelper_17_5.getLteSoap().systemDefaultProfileGet(names, hardwares,null, credentialsLte);
		} catch (Exception e) {
			e.printStackTrace();
			soapHelper_17_5.endLteSoap();
			return false;
		}
		return true;
	}
    
    @Override
    public boolean getManagementProfile(EnodeB node, String cloneFromName) {
        List<String> names = new ArrayList<>();
        names.add(cloneFromName);
        try {
            soapHelper_17_5.getLteSoap().managementProfileGet(names,null,null, credentialsLte);
        } catch (Exception e) {
            soapHelper_17_5.endLteSoap();
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean getSecurityProfile(EnodeB node, String cloneFromName) {
        List<String> names = new ArrayList<>();
        names.add(cloneFromName);
        try {
            soapHelper_17_5.getLteSoap().securityProfileGet(names,null,null, credentialsLte);
        } catch (Exception e) {
            soapHelper_17_5.endLteSoap();
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean getMobilityProfile(EnodeB node, String cloneFromName) {
        List<String> names = new ArrayList<>();
        names.add(cloneFromName);
        try {
            soapHelper_17_5.getLteSoap().mobilityProfileGet(names,null,null, credentialsLte);
        } catch (Exception e) {
            soapHelper_17_5.endLteSoap();
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean getNetworkProfile(EnodeB node, String cloneFromName) {
        List<String> names = new ArrayList<>();
        names.add(cloneFromName);
        try {
            soapHelper_17_5.getLteSoap().networkProfileGet(names,null,null, credentialsLte);
        } catch (Exception e) {
            soapHelper_17_5.endLteSoap();
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean getSonProfile(EnodeB node, String cloneFromName) {
        List<String> names = new ArrayList<>();
        names.add(cloneFromName);
        try {
            soapHelper_17_5.getLteSoap().sonProfileGet(names,null,null, credentialsLte);
        } catch (Exception e) {
            soapHelper_17_5.endLteSoap();
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean getRadioProfile(EnodeB node, String cloneFromName) {
        List<String> names = new ArrayList<>();
        names.add(cloneFromName);
        try {
            soapHelper_17_5.getLteSoap().radioProfileGet(names,null,null, credentialsLte);
        } catch (Exception e) {
            soapHelper_17_5.endLteSoap();
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean getSyncProfile(EnodeB node, String cloneFromName) {
        List<String> names = new ArrayList<>();
        names.add(cloneFromName);
        try {
            soapHelper_17_5.getLteSoap().syncProfileGet(names,null,null, credentialsLte);
        } catch (Exception e) {
            soapHelper_17_5.endLteSoap();
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean getAdvancedConfigurationProfile(EnodeB node, String cloneFromName) {
        List<String> names = new ArrayList<>();
        names.add(cloneFromName);
        try {
            soapHelper_17_5.getLteSoap().cellAdvancedConfigProfileGet(names,null,null, credentialsLte);
        } catch (Exception e) {
            soapHelper_17_5.endLteSoap();
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean getRawNetspanGetProfileResponse(EnodeB node, String cloneFromName, INetspanProfile profileParams) {
        List<String> name = new ArrayList<>();
        name.add(cloneFromName);
        soapHelper_17_5.clearRawData();
        switch (profileParams.getType()) {
            case System_Default_Profile:
                soapHelper_17_5.getLteSoapRaw().systemDefaultProfileGet(name,null,null, credentialsLte);
                break;
            case Management_Profile:
                soapHelper_17_5.getLteSoapRaw().managementProfileGet(name,null,null, credentialsLte);
                break;
            case Mobility_Profile:
                soapHelper_17_5.getLteSoapRaw().mobilityProfileGet(name,null,null, credentialsLte);
                break;
            case Network_Profile:
                soapHelper_17_5.getLteSoapRaw().networkProfileGet(name,null,null, credentialsLte);
                break;
            case Radio_Profile:
                soapHelper_17_5.getLteSoapRaw().radioProfileGet(name,null,null, credentialsLte);
                break;
            case Security_Profile:
                soapHelper_17_5.getLteSoapRaw().securityProfileGet(name,null,null, credentialsLte);
                break;
            case Son_Profile:
                soapHelper_17_5.getLteSoapRaw().sonProfileGet(name,null,null, credentialsLte);
                break;
            case Sync_Profile:
                soapHelper_17_5.getLteSoapRaw().syncProfileGet(name,null,null, credentialsLte);
                break;
            case EnodeB_Advanced_Profile:
                soapHelper_17_5.getLteSoapRaw().enbAdvancedConfigProfileGet(name,null,null, credentialsLte);
                break;
            case Cell_Advanced_Profile:
                soapHelper_17_5.getLteSoap().cellAdvancedConfigProfileGet(name,null,null, credentialsLte);
                break;
            case Neighbour_Management_Profile:
                report.report("Neighbour management profile does not exist in netspan 17_5");
                break;
            case MultiCell_Profile:
                soapHelper_17_5.getLteSoapRaw().multiCellProfileGet(name,null,null, credentialsLte);
                break;
            default:
                return false;
        }

        try {
            String rawData = soapHelper_17_5.getSOAPRawData();
            printHTMLRawData(rawData);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            soapHelper_17_5.endLteSoap();
        }
        return true;
    }

    @Override
    public int getNumberOfNetspanCells(EnodeB enb) {
        EnbDetailsGet enbDetails = getNodeConfig(enb);
        int numberOfEnableCells = 0;
        try {
            for (LteCellGetWs cell : enbDetails.getLteCell()) {
                if (cell.getIsEnabled().getValue()) {
                    numberOfEnableCells++;
                }
            }
            return numberOfEnableCells;
        } catch (Exception e) {
            return 1;
        }
    }

    @Override
    public boolean cloneCellAdvancedProfile(EnodeB node, String cloneFromName, CellAdvancedParameters advancedParmas) {
    	try {
			EnbCellAdvancedProfile advancedProfile = parseCellAdvancedParameters(node, advancedParmas);
			ProfileResponse cloneResult = soapHelper_17_5.getLteSoap()
					.cellAdvancedConfigProfileClone(cloneFromName, advancedProfile, credentialsLte);
			if (cloneResult == null) {
				report.report("Fail to get cell advanced profile cloning result", Reporter.WARNING);
				return false;
			} else if (cloneResult.getErrorCode() == Netspan.NBI_17_5.Lte.ErrorCodes.OK) {
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
			soapHelper_17_5.endLteSoap();
		}
    }

    private EnbCellAdvancedProfile parseCellAdvancedParameters(EnodeB node, CellAdvancedParameters advancedParmas) {
		EnbCellAdvancedProfile profile = new EnbCellAdvancedProfile();
		profile.setName(advancedParmas.getProfileName());
		ObjectFactory factoryDetails = new ObjectFactory();
		EtwsWs etwsWs = new EtwsWs();
		if (advancedParmas.getEtwsUserMode() == EnabledStates.ENABLED) {
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
    public boolean updateCellAdvancedProfile(EnodeB node, CellAdvancedParameters advancedParmas) {
    	try {
			EnbCellAdvancedProfile advancedProfile = parseCellAdvancedParameters(node, advancedParmas);
			ProfileResponse updateResult = soapHelper_17_5.getLteSoap()
					.cellAdvancedConfigProfileUpdate(advancedParmas.getProfileName(), advancedProfile, credentialsLte);

			if (updateResult == null) {
				report.report("Fail to get cell advanced profile update result", Reporter.WARNING);
				return false;
			} else if (updateResult.getErrorCode() == Netspan.NBI_17_5.Lte.ErrorCodes.OK) {
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
			soapHelper_17_5.endLteSoap();
		}
    }

    @Override
    public boolean setLatitudeAndLongitude(EnodeB node, BigDecimal Latitude, BigDecimal Longitude) {
    	NodeActionResult result;
		try{
			result = soapHelper_17_5.getInventorySoap().nodeLatitudeLongitudeSet(node.getNetspanName(), Latitude, Longitude, credentialsInventory);
			if(result.getErrorCode() != Netspan.NBI_17_5.Inventory.ErrorCodes.OK){
				report.report("netspan Error : "+ result.getErrorString());
			}
			
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		
		finally{
			soapHelper_17_5.endInventorySoap();
		}
		return true;
    }

    @Override
    public String getSyncState(EnodeB eNodeB) {
    	NodeGpsGetResult result = null;
		try {
			result = soapHelper_17_5.getStatusSoap().nodeGpsGet(eNodeB.getNetspanName(),
					credentialsStatus);
		} catch (Exception e) {
			report.report("nodeGpsGet via Netspan Failed due to: " + e.getMessage(), Reporter.WARNING);
			e.printStackTrace();
			return null;
		}
		if (result.getErrorCode() != Netspan.NBI_17_5.Status.ErrorCodes.OK) {
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
			result = soapHelper_17_5.getStatusSoap().nodeGpsGet(eNodeB.getNetspanName(),
					credentialsStatus);
		} catch (Exception e) {
			report.report("nodeGpsGet via Netspan Failed due to: " + e.getMessage(), Reporter.WARNING);
			e.printStackTrace();
			return null;
		}
		if (result.getErrorCode() != Netspan.NBI_17_5.Status.ErrorCodes.OK) {
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
    public List<LteBackhaul> getBackhaulInterfaceStatus(EnodeB node) {
    	LteBackhaulIfGetResult serverResult = soapHelper_17_5.getStatusSoap().enbBackhaulInterfaceStatusGet(node.getNetspanName(), credentialsStatus);
		List<LteBackhaul> res = new ArrayList<LteBackhaul>();

		if (serverResult == null) {
			report.report("BackhaulInterfaceStatus is null", Reporter.WARNING);
			return null;
		}

		for (LteBackhaulWs backaul : serverResult.getBackhaul()) {
			LteBackhaul curr = new LteBackhaul();
			curr.name = backaul.getName();
			curr.ethernetDuplex = DuplexType.fromValue(backaul.getEthernetDuplex().getValue().value());
			curr.ethernetRate = backaul.getEthernetRate().getValue();
			curr.portType = backaul.getPortType().getValue();
			curr.portStatus = backaul.getPortStatus().getValue();
			curr.portSpeed = backaul.getPortSpeed().getValue();
			curr.flowControlStatus = EnabledStates.fromValue(backaul.getFlowControlStatus().getValue().value());
			res.add(curr);
		}
		return res;
    }

    @Override
    public String getMangementIp(EnodeB enb) {
        EnbDetailsGet enbConfig = getNodeConfig(enb);
        if (enbConfig == null) {
            GeneralUtils.printToConsole("getMangementIp failed!");
            return GeneralUtils.ERROR_VALUE + "";
        }
        return enbConfig.getManagementIpAddress();
    }

    @Override
    public int getNumberOfActiveCellsForNode(EnodeB node) {
        int numberOfActiveCells = 0;
        EnbDetailsGet nodeConf = getNodeConfig(node);
        if (nodeConf == null) {
            return 0;
        }
        List<LteCellGetWs> cellsList = nodeConf.getLteCell();
        for (LteCellGetWs cell : cellsList) {
            if (cell.getIsEnabled().getValue()) {
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
		Netspan.NBI_17_5.Inventory.NodeActionResult result = null;
		if (rebootType == RebootType.WARM_REBOOT) {
			result = soapHelper_17_5.getInventorySoap().nodeReset(nodeList, null, false, credentialsInventory);
		} else {
			result = soapHelper_17_5.getInventorySoap().nodeResetCold(nodeList, null, credentialsInventory);
		}
		if (result != null) {
			if (result.getErrorCode() != Netspan.NBI_17_5.Inventory.ErrorCodes.OK) {
				report.report("Failed to Reset Node via Netspan  : " + result.getErrorString(), Reporter.WARNING);
				rebooted = false;
			} else {
				rebooted = true;
			}
		}
		soapHelper_17_5.endInventorySoap();
		return rebooted;
    }


    @Override
    public boolean setEnbAccessClassBarring(EnodeB dut, CellBarringPolicyParameters cellBarringParams) {
    	LteEnbDetailsSetWs enbConfigSet = new LteEnbDetailsSetWs();
		ObjectFactory objectFactory = new ObjectFactory();
		LteCellSetWs lteCellSet = new LteCellSetWs();
		try {
			lteCellSet.setCellNumber(objectFactory.createLteCellSetWsCellNumber(String.valueOf(dut.getCellContextID())));
			if (cellBarringParams.cellBarringPolicy != null) {
				lteCellSet.setCellBarringPolicy(objectFactory.createLteCellSetWsCellBarringPolicy(cellBarringParams.cellBarringPolicy));
			}
			switch (cellBarringParams.cellBarringPolicy) {
				case AC_BARRING: {
					if (cellBarringParams.IsAccessClassBarred != null) {
						lteCellSet.setIsCsfbAccessBarred(objectFactory.createLteCellSetWsIsCsfbAccessBarred(cellBarringParams.IsAccessClassBarred));
					}
					if (cellBarringParams.IsAccessClassBarred) {
						if (cellBarringParams.IsEmergencyAccessBarred != null)
							lteCellSet.setIsEmergencyAccessBarred(objectFactory.createLteCellSetWsIsEmergencyAccessBarred(cellBarringParams.IsEmergencyAccessBarred));
						if (cellBarringParams.IsSignalingAccessBarred != null) {
							lteCellSet.setIsSignalingAccessBarred(objectFactory.createLteCellSetWsIsSignalingAccessBarred(cellBarringParams.IsSignalingAccessBarred));
							if (cellBarringParams.IsSignalingAccessBarred && cellBarringParams.signalingAccessBarring != null) {
								if (cellBarringParams.signalingAccessBarring.getBarringFactor().value() != null)
									lteCellSet.setSignalingAccessBarringFactor(objectFactory.createLteCellSetWsSignalingAccessBarringFactor(cellBarringParams.signalingAccessBarring.getBarringFactor().value()));
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
						if (cellBarringParams.IsDataAccessBarred != null) {
							if (cellBarringParams.IsDataAccessBarred && cellBarringParams.dataAccessBarring != null) {
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
			Netspan.NBI_17_5.Lte.NodeActionResult result = soapHelper_17_5.getLteSoap().enbConfigSet(dut.getNetspanName(),null, null, enbConfigSet, null, credentialsLte);
			if (result.getErrorCode() != Netspan.NBI_17_5.Lte.ErrorCodes.OK) {
				report.report("setEnbCellProperties via Netspan Failed : " + result.getErrorString(), Reporter.WARNING);
				return false;
			} else
				return true;

		} catch (Exception e) {
			e.printStackTrace();
			report.report("setEnbCellProperties via Netspan Failed due to: " + e.getMessage(), Reporter.WARNING);
			return false;
		} finally {
			soapHelper_17_5.endLteSoap();
		}
    }

    /**
     * get Software Status for EnB - WS
     *
     * @param enodeB - enodeB
     */
    private SoftwareStatusGetWs getSoftwareStatusForEnb(EnodeB enodeB) {
        List<String> enbList = new ArrayList<>();
        enbList.add(enodeB.getNetspanName());
        return soapHelper_17_5.getSoftwareSoap().softwareStatusGet(enbList, credentialsSoftware);
    }

    /**
     * get Running Version Of Enb
     *
     * @param enodeB - enodeB
     * @return - Running version or ERROR_VALUE if fails
     */
    public String getRunningVersionOfEnb(EnodeB enodeB) {
        NodeSoftwareStatus softwareStatus = getSoftwareStatus(enodeB);
        if (!isResponseValid(softwareStatus)) {
            return String.valueOf(GeneralUtils.ERROR_VALUE);
        }
        String runningVersion = softwareStatus.getRunningVersion();
        if (!isResponseValid(runningVersion)) {
            return String.valueOf(GeneralUtils.ERROR_VALUE);
        } else return runningVersion;
    }

    /**
     * get StandBy Version Of Enb
     *
     * @param enodeB - enodeB
     * @return - Running version or ERROR_VALUE if fails
     */
    public String getStandByVersionOfEnb(EnodeB enodeB) {
        NodeSoftwareStatus softwareStatus = getSoftwareStatus(enodeB);
        if (!isResponseValid(softwareStatus)) {
            return String.valueOf(GeneralUtils.ERROR_VALUE);
        }
        String standByVersion = softwareStatus.getStandbyVersion();
        if (!isResponseValid(standByVersion)) {
            return String.valueOf(GeneralUtils.ERROR_VALUE);
        } else return standByVersion;
    }

    /**
     * Checks if response is Valid  (!= null)
     *
     * @param object - response
     * @return - true if null
     */
    private boolean isResponseValid(Object object) {
        if (object == null) {
            report.report("The request to the Netspan Failed");
            return false;
        } else
            return true;
    }

    /**
     * get Software Status of requested EnB
     *
     * @param enodeB - enodeB
     * @return - first Node SW Status, null if fails
     */
    private NodeSoftwareStatus getSoftwareStatus(EnodeB enodeB) {
        SoftwareStatusGetWs softwareStatusGetWs = getSoftwareStatusForEnb(enodeB);
        //Checks response status
        if (!Netspan.NBI_17_5.Software.ErrorCodes.OK.equals(softwareStatusGetWs.getErrorCode())) {
            return null;
        }
        NodeSoftwareStatusResult firstNodeSWStatusResult = softwareStatusGetWs.getNodeSoftwareStatus().get(0);
        NodeSoftwareStatus firstNodeSWStatus = firstNodeSWStatusResult.getSoftwareStatus().get(0);
        return firstNodeSWStatus;
    }


    @Override
    public String getImageType(String nodeName) {
        report.report("getImageType method is not implemented for this netspan(17_5)!");
        return null;
    }

    @Override
    public int getMaxUeSupported(EnodeB enb) {
    	int maxUeSupported = 0;
		try {
			LteUeGetResult result = soapHelper_17_5.getStatusSoap().enbConnectedUeStatusGet(enb.getNetspanName(), credentialsStatus);
			if (result != null) {
				maxUeSupported = result.getCell().get(0).getActualMaxUeSupported().getValue();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			soapHelper_17_5.endStatusSoap();
		}
		if (maxUeSupported <= 0) {
			report.report("Failed to get Actual Max UE Supported Cell 1", Reporter.WARNING);
		}
		return maxUeSupported;
    }

    @Override
    public Pair<Integer, Integer> getUlDlTrafficValues(String nodeName) {
        int ul = 0;
        int dl = 0;
        LteIpThroughputGetResult result = soapHelper_17_5.getStatusSoap()
            .enbIpThroughputStatusGet(nodeName, credentialsStatus);

        if (result.getErrorCode() != Netspan.NBI_17_5.Status.ErrorCodes.OK) {
            soapHelper_17_5.endStatusSoap();
            return null;
        }
        List<LteIpThroughputCellWs> listOfCells = result.getCell();

        for (LteIpThroughputCellWs cellData : listOfCells) {
            List<LteIpThroughputQciWs> cellQciData = cellData.getQciData();
            for (LteIpThroughputQciWs qciData : cellQciData) {
                ul += qciData.getPdcpTrafficKbpsUl().getValue();
                dl += qciData.getPdcpTrafficKbpsDl().getValue();
            }
        }
        soapHelper_17_5.endStatusSoap();
        return Pair.createPair(dl, ul);
    }

	@Override
	public boolean resetNodeRebootAction(String nodeName, RebootTypesNetspan rebootType) {
		NodeActionResult result = null;
		ArrayList<String> listEnb = new ArrayList<String>();
		listEnb.add(nodeName);
		if(rebootType == RebootTypesNetspan.Reset_Node){
			result = soapHelper_17_5.getInventorySoap().nodeReset(listEnb, null,false, credentialsInventory);
		}else if(rebootType == RebootTypesNetspan.Forced_Reset_Node){
			result = soapHelper_17_5.getInventorySoap().nodeResetForced(listEnb, null,false, credentialsInventory);
		}else if(rebootType == RebootTypesNetspan.Cold_Reset_Node){
			result = soapHelper_17_5.getInventorySoap().nodeResetCold(listEnb, null, credentialsInventory);
		}else{
			result = soapHelper_17_5.getInventorySoap().nodeResetForcedCold(listEnb, null, credentialsInventory);
		}
		boolean rebooted = result.getErrorCode() == Netspan.NBI_17_5.Inventory.ErrorCodes.OK;
		soapHelper_17_5.endInventorySoap();
		return rebooted;
	}

	@Override
	public boolean relayScan(EnodeB enodeB, RelayScanType scanType) {
		Netspan.NBI_17_5.Backhaul.NodeActionResult result = null;
		ArrayList<String> listEnb = new ArrayList<String>();
		listEnb.add(enodeB.getNetspanName());
		boolean scanned = false;
		try{
			if(scanType == RelayScanType.ForceScan){
				result = soapHelper_17_5.getBackhaulSoap().relayForceScan(listEnb, null, credentialsBackhaul);
				scanned = result.getErrorCode() == Netspan.NBI_17_5.Backhaul.ErrorCodes.OK;
				if(!scanned){
					report.report("Scan failed due to: "+result.getNode().get(0).getNodeResultString(),Reporter.WARNING);
				}
			}else{
				result = soapHelper_17_5.getBackhaulSoap().relayScan(listEnb, null, credentialsBackhaul);
				scanned = result.getErrorCode() == Netspan.NBI_17_5.Backhaul.ErrorCodes.OK;
				if(!scanned){
					report.report("Scan failed due to: "+result.getNode().get(0).getNodeResultString(),Reporter.WARNING);
				}
			}			
		}catch(Exception e){
			report.report("relayScan via netspan Return Error: " + e.getMessage());
			scanned = false;
		}finally {
			soapHelper_17_5.endBackhaulSoap();			
		}
		return scanned;
	}

	@Override
	public boolean isRelayEnodeb(String nodeName) {
		try {
			ArrayList<String> nameList = new ArrayList<>();
			nameList.add(nodeName);
			RelayEnodeBConfigGetResult result = soapHelper_17_5.getLteSoap().relayEnbConfigGet(nameList,null, credentialsLte);
			if (result.getErrorCode() == Netspan.NBI_17_5.Lte.ErrorCodes.OK) {
				report.report(nodeName + " has relay");
				return true;
			}else{
				report.report(nodeName + " has no relay");
				return false;
			}				
		} catch (Exception e) {
			e.printStackTrace();
			report.report("Failed to determine if " + nodeName + " has relay, due to: " + e.getMessage(), Reporter.WARNING);
			return false;
		} finally {
			soapHelper_17_5.endLteSoap();
		}
	}
	
	@Override
	public boolean setEmergencyAreaIds(EnodeB dut, ArrayList<Integer> ids) {
		String nodeName = dut.getNetspanName();
		LteEnbDetailsSetWs enbConfigSet = new LteEnbDetailsSetWs();
		ObjectFactory factoryDetails = new ObjectFactory();
		LteCellSetWs lteCellSet = new LteCellSetWs();
		EaidsParams eaidsParams = factoryDetails.createEaidsParams();
		eaidsParams.getEmergencyAreaId().addAll(ids);
		lteCellSet.setCellNumber(factoryDetails.createLteCellSetWsCellNumber(String.valueOf(dut.getCellContextID())));
		lteCellSet.setEmergencyAreaIds(eaidsParams);
		enbConfigSet.getLteCell().add(lteCellSet);
		try {
			Netspan.NBI_17_5.Lte.NodeActionResult result = soapHelper_17_5.getLteSoap().enbConfigSet(nodeName, null,null,
					enbConfigSet, null, credentialsLte);
			if (result.getErrorCode() == Netspan.NBI_17_5.Lte.ErrorCodes.OK) {
				report.report(String.format("%s - Succeeded to set Emergency Area Ids", nodeName));
				return true;
			} else {
				report.report("enbConfigSet via Netspan Failed : " + result.getErrorString(), Reporter.WARNING);
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			report.report(nodeName + ": enbConfigSet via Netspan Failed due to: "+e.getMessage(), Reporter.WARNING);
			return false;
		} finally {
			soapHelper_17_5.endLteSoap();
		}
	}

	public boolean getMultiCellProfile(EnodeB node, String cloneFromName) {
		List<String> names = new ArrayList<>();
		List<CategoriesLte> hardwares = new ArrayList<CategoriesLte>();
		EnbDetailsGet nodeConf = getNodeConfig(node);
		CategoriesLte hardware = null;
		try {
			hardware = CategoriesLte.fromValue(removeDigitsAndSpaces(nodeConf.getHardware()));
		} catch (Exception e) {
			report.report("Incompetible HardWare Type in inner Netspan method!", Reporter.WARNING);
			GeneralUtils.printToConsole("Error : " + e.getMessage() + ", nodeConfig :" + nodeConf.getHardware());
		}
		hardwares.add(hardware);
		names.add(cloneFromName);
		try {
			soapHelper_17_5.getLteSoap().multiCellProfileGet(names, hardwares,null, credentialsLte);
		} catch (Exception e) {
			e.printStackTrace();
			soapHelper_17_5.endLteSoap();
			return false;
		}
		return true;
	}

	@Override
	public boolean getNeighborManagementProfile(EnodeB node, String cloneFromName) {
		List<String> names = new ArrayList<>();
		List<CategoriesLte> hardwares = new ArrayList<CategoriesLte>();
		EnbDetailsGet nodeConf = getNodeConfig(node);
		CategoriesLte hardware = null;
		try {
			hardware = CategoriesLte.fromValue(removeDigitsAndSpaces(nodeConf.getHardware()));
		} catch (Exception e) {
			report.report("Incompatible HardWare Type in inner Netspan method!", Reporter.WARNING);
			GeneralUtils.printToConsole("Error : " + e.getMessage() + ", nodeConfig :" + nodeConf.getHardware());
			return false;
		}
		hardwares.add(hardware);
		names.add(cloneFromName);
		try {
			soapHelper_17_5.getLteSoap().neighbourProfileGet(names, hardwares,null, credentialsLte);
		} catch (Exception e) {
			e.printStackTrace();
			soapHelper_17_5.endLteSoap();
			return false;
		}
		return true;
	}

	@Override
	public boolean isFileServerExists(String fileServerName) {
		try {
			FileServerResponse result = null;
			List<String> nameList = new ArrayList<String>();
			nameList.add(fileServerName);
			result = soapHelper_17_5.getServerSoap().fileServerGet(nameList, credentialsServer);
			if (result.getErrorCode() != Netspan.NBI_17_5.Server.ErrorCodes.OK) {
				report.report("fileServerGet via Netspan Failed: " + result.getErrorString(), Reporter.WARNING);
			} else
				return true;
		} catch (Exception e) {
			e.printStackTrace();
			report.report("fileServerGet via Netspan Failed due to: " + e.getMessage(), Reporter.WARNING);
			return false;
		} finally {
			soapHelper_17_5.endServerSoap();
		}
		return false;
	}

	private String removeDigitsAndSpaces(String toRem){
		String ret = toRem.replaceAll("[0-9]", "");
		ret = ret.replace(" ", "");
		return ret;
	}
	
	@Override
	public boolean createFileServer(FileServer fileServer) {
		Netspan.NBI_17_5.Server.ObjectFactory objectFactory = new Netspan.NBI_17_5.Server.ObjectFactory();
		FileServerWs fileServerWs = new FileServerWs();
		fileServerWs.setName(fileServer.getName());
		fileServerWs.setServerIpAddress(fileServer.getIpAddress());
		fileServerWs.setUserName(fileServer.getUsername());
		fileServerWs.setPassword(fileServer.getPassword());
		fileServerWs.setProtocolType(objectFactory
				.createFileServerWsProtocolType(FileServerProtocolType.valueOf(fileServer.getProtocolType())));
		try {
			FileServerResponse result = null;
			result = soapHelper_17_5.getServerSoap().fileServerCreate(fileServerWs,
					credentialsServer);
			if (result.getErrorCode() != Netspan.NBI_17_5.Server.ErrorCodes.OK) {
				report.report("fileServerCreate via Netspan Failed : " + result.getErrorString(), Reporter.WARNING);
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			report.report("fileServerCreate via Netspan Failed due to: " + e.getMessage(), Reporter.WARNING);
			return false;
		} finally {
			soapHelper_17_5.endServerSoap();
		}
	}

	@Override
	public MultiCellParameters getMultiCellProfileObject(EnodeB node, String profileName) {
		List<String> name = new ArrayList<String>();
		name.add(profileName);
		CategoriesLte category = null;
		List<CategoriesLte> categoryList = new ArrayList<>();
		EnbDetailsGet nodeConf = getNodeConfig(node);
		try {
			category = CategoriesLte.fromValue(removeDigitsAndSpaces(nodeConf.getHardware()));
		} catch (Exception e) {
			report.report("Incompatible HardWare Type in inner Netspan method!", Reporter.WARNING);
			GeneralUtils.printToConsole("Error : " + e.getMessage() + ", nodeConfig :" + nodeConf.getHardware());
			return null;
		}
		categoryList.add(category);
		report.report("trying to get MultiCell profile with the name : " + name + " ,and hardware : " + categoryList.toString());
		MultiCellProfileGetResult multiCellNetspan = soapHelper_17_5.getLteSoap().multiCellProfileGet(name, categoryList,null, credentialsLte);
		MultiCellParameters multiCellParam = getAdapterMultiCellObject(multiCellNetspan);
		return multiCellParam;
	}

	private MultiCellParameters getAdapterMultiCellObject(MultiCellProfileGetResult multiCellResult) {
		MultiCellParameters mc = new MultiCellParameters();
		MultiCellProfileResult multiCellProfileResult = multiCellResult.getMultiCellProfileResult().get(0);
		if (multiCellProfileResult != null) {
			EnbMultiCellProfile multiCellProfile = multiCellProfileResult.getMultiCellProfile();

			CarrierAggregationModes caMode = multiCellProfile.getCarrierAggMode().getValue();
			boolean caModeBol = false;
			if (caMode == CarrierAggregationModes.CONTIGUOUS) {
				caModeBol = true;
			}
			mc.setCarrierAggMode(caModeBol);
		}
		return mc;
	}
	
	@Override
	public HashMap<String, NetworkElementStatus> getMMEStatuses(EnodeB enb) {
		HashMap<String, NetworkElementStatus> mmeStatuses = new HashMap<>();
		LteNetworkElementGetResult result = soapHelper_17_5.getStatusSoap().enbNetworkElementStatusGet(enb.getNetspanName(), credentialsStatus);
		if (result != null) {
			for (LteNetworkElementStatusWs status : result.getNetworkElementStatus()) {
				if (status.getNetworkElement().toLowerCase().contains("mme")) {
					String s = status.getIpAddress();
					NetworkElementStatus n = NetworkElementStatus.fromValue(status.getStatus().value());
					mmeStatuses.put(s, n);
				}
			}
		}
		return mmeStatuses;
	}
	
	public boolean setProfilesInCell2(EnodeB node, String cellAdvancedProfile, String radioProfile, String MobilityProfile, String eMBMSProfile, String TrafficManagementProfile, String callTraceProfile){
        ObjectFactory objectFactory = new ObjectFactory();
        LteEnbDetailsSetWs enbConfigSet = new LteEnbDetailsSetWs();
        LteCellSetWs lteCellSet = new LteCellSetWs();
        lteCellSet.setCellNumber(objectFactory.createLteCellGetWsCellNumber(String.valueOf(2)));

        enbConfigSet = addProfile(node, 2, EnbProfiles.Cell_Advanced_Profile, cellAdvancedProfile, enbConfigSet);
        enbConfigSet = addProfile(node, 2, EnbProfiles.Radio_Profile, radioProfile, enbConfigSet);
        enbConfigSet = addProfile(node, 2, EnbProfiles.Mobility_Profile, MobilityProfile, enbConfigSet);
        enbConfigSet = addProfile(node, 2, EnbProfiles.eMBMS_Profile, eMBMSProfile, enbConfigSet);
        enbConfigSet = addProfile(node, 2, EnbProfiles.Traffic_Management_Profile, TrafficManagementProfile, enbConfigSet);
        enbConfigSet = addProfile(node, 2, EnbProfiles.Call_Trace_Profile, callTraceProfile, enbConfigSet);

        return setNodeConfig(node, enbConfigSet);
    }
	
	private String getCurrentTrafficManagementProfileName(EnodeB enb, int cellNumber) {
        EnbDetailsGet nodeConfig = getNodeConfig(enb);
        if (nodeConfig != null) {
            for (LteCellGetWs cell : nodeConfig.getLteCell()) {
                int tempCellNumber = Integer.valueOf(cell.getCellNumber().getValue());
                if (tempCellNumber == cellNumber) {
                    GeneralUtils.printToConsole("Traffic management Profile From Netspan: " + cell.getTrafficManagementProfile() + " for cell : " + cell.getCellNumber().getValue());
                    return cell.getTrafficManagementProfile();
                }
            }
        }
        return null;
    }

    private String getCurrentCallTraceProfileName(EnodeB enb, int cellNumber) {
        EnbDetailsGet nodeConfig = getNodeConfig(enb);
        if (nodeConfig != null) {
            for (LteCellGetWs cell : nodeConfig.getLteCell()) {
                int tempCellNumber = Integer.valueOf(cell.getCellNumber().getValue());
                if (tempCellNumber == cellNumber) {
                    GeneralUtils.printToConsole("Traffic management Profile From Netspan: " + cell.getCallTraceProfile() + " for cell : " + cell.getCellNumber().getValue());
                    return cell.getCallTraceProfile();
                }
            }
        }
        return null;
    }

    private String getCurrenteMBMsProfileName(EnodeB enb, int cellNumber) {
        EnbDetailsGet nodeConfig = getNodeConfig(enb);
        if (nodeConfig != null) {
            for (LteCellGetWs cell : nodeConfig.getLteCell()) {
                int tempCellNumber = Integer.valueOf(cell.getCellNumber().getValue());
                if (tempCellNumber == cellNumber) {
                    GeneralUtils.printToConsole("eMBMS Profile From Netspan: " + cell.getEmbmsProfile() + " for cell : " + cell.getCellNumber().getValue());
                    return cell.getEmbmsProfile();
                }
            }
        }
        return null;
    }
}

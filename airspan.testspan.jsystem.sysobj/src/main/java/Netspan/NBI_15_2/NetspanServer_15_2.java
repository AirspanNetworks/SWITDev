package Netspan.NBI_15_2;

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
import Netspan.EnbProfiles;
import Netspan.NBIVersion;
import Netspan.NBI_15_2.Software.*;
import Netspan.NetspanServer;
import Netspan.API.Enums.CategoriesLte;
import Netspan.API.Enums.ClockSources;
import Netspan.API.Enums.EnabledStates;
import Netspan.API.Enums.EnbStates;
import Netspan.API.Enums.EnbTypes;
import Netspan.API.Enums.HandoverType;
import Netspan.API.Enums.HardwareCategory;
import Netspan.API.Enums.HoControlStateTypes;
import Netspan.API.Enums.ImageType;
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
import Netspan.NBI_15_2.FaultManagement.Alarm;
import Netspan.NBI_15_2.FaultManagement.AlarmActionResult;
import Netspan.NBI_15_2.FaultManagement.AlarmResultList;
import Netspan.NBI_15_2.FaultManagement.Event;
import Netspan.NBI_15_2.FaultManagement.EventResultList;
import Netspan.NBI_15_2.Inventory.DiscoveryTaskActionResult;
import Netspan.NBI_15_2.Inventory.ErrorCodes;
import Netspan.NBI_15_2.Inventory.NodeActionResult;
import Netspan.NBI_15_2.Inventory.NodeDetailGetResult;
import Netspan.NBI_15_2.Inventory.NodeManagementMode;
import Netspan.NBI_15_2.Inventory.WsResponse;
import Netspan.NBI_15_2.Lte.AddlSpectrumEmissions;
import Netspan.NBI_15_2.Lte.AirSonWs;
import Netspan.NBI_15_2.Lte.AnrFreq;
import Netspan.NBI_15_2.Lte.AnrFreqListContainer;
import Netspan.NBI_15_2.Lte.Enb3RdParty;
import Netspan.NBI_15_2.Lte.EnbAdvancedProfile;
import Netspan.NBI_15_2.Lte.EnbDetailsGet;
import Netspan.NBI_15_2.Lte.EnbDetailsGetPnp;
import Netspan.NBI_15_2.Lte.EnbMobilityProfile;
import Netspan.NBI_15_2.Lte.EnbMultiCellProfile;
import Netspan.NBI_15_2.Lte.EnbNetworkProfile;
import Netspan.NBI_15_2.Lte.EnbPnpConfig;
import Netspan.NBI_15_2.Lte.EnbRadioProfile;
import Netspan.NBI_15_2.Lte.EnbSecurityProfile;
import Netspan.NBI_15_2.Lte.EnbSonProfile;
import Netspan.NBI_15_2.Lte.EnbSyncProfile;
import Netspan.NBI_15_2.Lte.Lte3RdPartyGetResult;
import Netspan.NBI_15_2.Lte.Lte3RdPartyResponse;
import Netspan.NBI_15_2.Lte.LteAddNeighbourForCellWs;
import Netspan.NBI_15_2.Lte.LteAddNeighbourWs;
import Netspan.NBI_15_2.Lte.LteCSonEntryWs;
import Netspan.NBI_15_2.Lte.LteCellGetWs;
import Netspan.NBI_15_2.Lte.LteCellSetWs;
import Netspan.NBI_15_2.Lte.LteEnbConfigGetResult;
import Netspan.NBI_15_2.Lte.LteEnbDetailsSetWs;
import Netspan.NBI_15_2.Lte.LteNeighbourResponse;
import Netspan.NBI_15_2.Lte.LteNeighbourResultValues;
import Netspan.NBI_15_2.Lte.LteNetworkProfileGetResult;
import Netspan.NBI_15_2.Lte.LtePlmnEntryWs;
import Netspan.NBI_15_2.Lte.LtePnpConfigGetResult;
import Netspan.NBI_15_2.Lte.LteRadioProfileGetResult;
import Netspan.NBI_15_2.Lte.LteS1EntryWs;
import Netspan.NBI_15_2.Lte.LteSecurityProfileGetResult;
import Netspan.NBI_15_2.Lte.LteSonCSonWs;
import Netspan.NBI_15_2.Lte.LteSyncProfileGetResult;
import Netspan.NBI_15_2.Lte.LteSyncProfileResult;
import Netspan.NBI_15_2.Lte.MobilityConnectedModeInterFreq;
import Netspan.NBI_15_2.Lte.MobilityConnectedModeStopGaps;
import Netspan.NBI_15_2.Lte.MobilityConnectedModeTriggerGaps;
import Netspan.NBI_15_2.Lte.NameResult;
import Netspan.NBI_15_2.Lte.NodeListResult;
import Netspan.NBI_15_2.Lte.NodeResult;
import Netspan.NBI_15_2.Lte.NodeResultValues;
import Netspan.NBI_15_2.Lte.NodeSimple;
import Netspan.NBI_15_2.Lte.ObjectFactory;
import Netspan.NBI_15_2.Lte.PciRange;
import Netspan.NBI_15_2.Lte.PciRangeListContainer;
import Netspan.NBI_15_2.Lte.PlmnListContainer;
import Netspan.NBI_15_2.Lte.PnpConfigCreate;
import Netspan.NBI_15_2.Lte.PnpDetailWs;
import Netspan.NBI_15_2.Lte.PnpHardwareTypes;
import Netspan.NBI_15_2.Lte.ProfileResponse;
import Netspan.NBI_15_2.Lte.ResourceManagementTypes;
import Netspan.NBI_15_2.Lte.RfProfileDuplexModes;
import Netspan.NBI_15_2.Lte.RsiRange;
import Netspan.NBI_15_2.Lte.RsiRangeListContainer;
import Netspan.NBI_15_2.Lte.S1ListContainer;
import Netspan.NBI_15_2.Lte.SfrThresholdTypes;
import Netspan.NBI_15_2.Lte.SnmpDetailSetWs;
import Netspan.NBI_15_2.Lte.SnmpDetailWs;
import Netspan.NBI_15_2.Lte.TddFrameConfigurationsSupported;
import Netspan.NBI_15_2.Server.NmsInfoResponse;
import Netspan.NBI_15_2.Status.LteAnrStatusWs;
import Netspan.NBI_15_2.Status.LteIpThroughputCellWs;
import Netspan.NBI_15_2.Status.LteIpThroughputGetResult;
import Netspan.NBI_15_2.Status.LteIpThroughputQciWs;
import Netspan.NBI_15_2.Status.LtePciStatusWs;
import Netspan.NBI_15_2.Status.LteRfGetResult;
import Netspan.NBI_15_2.Status.LteRfStatusWs;
import Netspan.NBI_15_2.Status.LteRsiStatusWs;
import Netspan.NBI_15_2.Status.LteSonAnrGetResult;
import Netspan.NBI_15_2.Status.LteSonPciGetResult;
import Netspan.NBI_15_2.Status.LteSonRsiGetResult;
import Netspan.NBI_15_2.Status.LteUeCategory;
import Netspan.NBI_15_2.Status.LteUeGetResult;
import Netspan.NBI_15_2.Status.LteUeStatusWs;
import Netspan.NBI_15_2.Status.NodePtpGetResult;
import Netspan.NBI_15_2.Status.NodeSoftwareGetResult;
import Netspan.NBI_15_2.Status.StatusSoap;
import Netspan.Profiles.CellAdvancedParameters;
import Netspan.Profiles.CellBarringPolicyParameters;
import Netspan.Profiles.ConnectedUETrafficDirection;
import Netspan.Profiles.EnodeBAdvancedParameters;
import Netspan.Profiles.INetspanProfile;
import Netspan.Profiles.ManagementParameters;
import Netspan.Profiles.MobilityParameters;
import Netspan.Profiles.MultiCellParameters;
import Netspan.Profiles.NeighbourManagementParameters;
import Netspan.Profiles.NetworkParameters;
import Netspan.Profiles.NetworkParameters.Plmn;
import Netspan.Profiles.RadioParameters;
import Netspan.Profiles.SecurityParameters;
import Netspan.Profiles.SonParameters;
import Netspan.Profiles.SyncParameters;
import Netspan.Profiles.SystemDefaultParameters;
import Utils.GeneralUtils;
import Utils.GeneralUtils.RebootType;
import Utils.GeneralUtils.RebootTypesNetspan;
import Utils.GeneralUtils.RelayScanType;
import Utils.Pair;
import Utils.Triple;
import jsystem.framework.report.Reporter;

/**
 * @author dshalom
 *
 */
/**
 * @author dshalom
 *
 */
public class NetspanServer_15_2 extends NetspanServer implements Netspan_15_2_abilities {
    public SoapHelper soapHelper_15_2;
    private static final String USERNAME = "wsadmin";
    private static final String PASSWORD = "password";
    private static final Netspan.NBI_15_2.Lte.Credentials credentialsLte = new Netspan.NBI_15_2.Lte.Credentials();
    private static final Netspan.NBI_15_2.Inventory.Credentials credentialsInventory = new Netspan.NBI_15_2.Inventory.Credentials();
    private static final Netspan.NBI_15_2.FaultManagement.Credentials credentialsFaultManagement = new Netspan.NBI_15_2.FaultManagement.Credentials();
    private static final Netspan.NBI_15_2.Statistics.Credentials credentialsStatistics = new Netspan.NBI_15_2.Statistics.Credentials();
    private static final Netspan.NBI_15_2.Status.Credentials credentialsStatus = new Netspan.NBI_15_2.Status.Credentials();
    private static final Netspan.NBI_15_2.Server.Credentials credentialsServer = new Netspan.NBI_15_2.Server.Credentials();
    private static final Netspan.NBI_15_2.Software.Credentials credentialsSoftware = new Netspan.NBI_15_2.Software.Credentials();

    @Override
    public void init() throws Exception {
        if (NBI_VERSION == null) {
            NBI_VERSION = NBIVersion.NBI_15_2;
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
    	this.soapHelper_15_2 = new SoapHelper(getHostname());
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
        Netspan.NBI_15_2.Inventory.NodeListResult result = soapHelper_15_2
            .getInventorySoap().nodeList(credentialsInventory);

        for (int nodeIndex = 0; nodeIndex < result.getNode().size(); nodeIndex++) {
            Netspan.NBI_15_2.Inventory.NodeSimple node = result.getNode().get(nodeIndex);
            nodeNames.put(node.getNodeId(), node.getName());
        }
        soapHelper_15_2.endInventorySoap();
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
        netspanResult = soapHelper_15_2.getLteSoap().radioProfileGet(enbList,
            credentialsLte);
        radioParam = parseRadioProfile(netspanResult);
        soapHelper_15_2.endLteSoap();
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
                result = soapHelper_15_2.getLteSoap().pnpConfigGet(nodeListName, credentialsLte);
            } catch (Exception e) {
                report.report("enbConfigGet via Netspan Failed due to: " + e.getMessage(), Reporter.WARNING);
                e.printStackTrace();
                return null;
            } finally {
                soapHelper_15_2.endLteSoap();
            }
            if (result.getErrorCode() != Netspan.NBI_15_2.Lte.ErrorCodes.OK) {
                GeneralUtils.printToConsole("enbConfigGet via Netspan Failed : " + result.getErrorString());
                return null;
            }
            GeneralUtils.printToConsole("enbConfigGet via Netspan for eNodeB " + enb.getNetspanName() + " succeeded");
            return convertPnPToEnb(result);
        }

        LteEnbConfigGetResult result = null;
        try {
            result = soapHelper_15_2.getLteSoap().enbConfigGet(nodeListName, credentialsLte);

        } catch (Exception e) {
            report.report("enbConfigGet via Netspan Failed due to: " + e.getMessage(), Reporter.WARNING);
            e.printStackTrace();
            return null;
        } finally {
            soapHelper_15_2.endLteSoap();
        }
        if (result.getErrorCode() != Netspan.NBI_15_2.Lte.ErrorCodes.OK) {
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
                nms = soapHelper_15_2.getServerSoap().nmsInfoGet(credentialsServer);
                if (nms.getErrorCode() != Netspan.NBI_15_2.Server.ErrorCodes.OK) {
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
                soapHelper_15_2.endServerSoap();
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
            Netspan.NBI_15_2.Lte.LteNeighbourResponse result = soapHelper_15_2
                .getLteSoap().lteNeighbourDelete(enodeB.getNetspanName(), neighbor.getNetspanName(),
                    enodeB.getCellContextID(), neighbor.getCellContextID(), credentialsLte);
            if (result.getErrorCode() != Netspan.NBI_15_2.Lte.ErrorCodes.OK) {
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
            soapHelper_15_2.endLteSoap();
        }
    }

    @Override
    public boolean deleteAllNeighbors(EnodeB enodeB) {
        try {
            if (verifyNoNeighbors(enodeB))
                return true;
            LteNeighbourResponse result = soapHelper_15_2.getLteSoap()
                .lteNeighbourDeleteAll(enodeB.getNetspanName(), credentialsLte);

            if (result.getErrorCode() != Netspan.NBI_15_2.Lte.ErrorCodes.OK) {
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
            soapHelper_15_2.endLteSoap();
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
                    response = soapHelper_15_2.getLteSoap().enbAdvancedProfileConfigDelete(nameList,
                        credentialsLte);
                    break;
                case Cell_Advanced_Profile:
                    response = soapHelper_15_2.getLteSoap().cellAdvancedProfileConfigDelete(nameList,
                        credentialsLte);
                    break;
                case Management_Profile:
                    response = soapHelper_15_2.getLteSoap().managementProfileDelete(nameList,
                        credentialsLte);
                    break;
                case Mobility_Profile:
                    response = soapHelper_15_2.getLteSoap().mobilityProfileDelete(nameList,
                        credentialsLte);
                    break;
                case Network_Profile:
                    response = soapHelper_15_2.getLteSoap().networkProfileDelete(nameList,
                        credentialsLte);
                    break;
                case Radio_Profile:
                    response = soapHelper_15_2.getLteSoap().radioProfileDelete(nameList, credentialsLte);
                    break;
                case Security_Profile:
                    response = soapHelper_15_2.getLteSoap().securityProfileDelete(nameList,
                        credentialsLte);
                    break;
                case Son_Profile:
                    response = soapHelper_15_2.getLteSoap().sonProfileDelete(nameList, credentialsLte);
                    break;
                case Sync_Profile:
                    response = soapHelper_15_2.getLteSoap().syncProfileDelete(nameList, credentialsLte);
                    break;
                case MultiCell_Profile:
                    response = soapHelper_15_2.getLteSoap().multiCellProfileDelete(nameList,
                        credentialsLte);
                    break;
                default:
                    report.report("deleteEnbProfile get EnbProfiles type not exist: " + profileType, Reporter.WARNING);
                    return false;
            }
            if (response.getErrorCode() != Netspan.NBI_15_2.Lte.ErrorCodes.OK) {
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
            soapHelper_15_2.endLteSoap();
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
        Netspan.NBI_15_2.Lte.WsResponse response = null;
        try {
            switch (profileType) {
                case System_Default_Profile:
                    response = soapHelper_15_2.getLteSoap()
                        .systemDefaultProfileGet(nameList, credentialsLte);
                    break;
                case EnodeB_Advanced_Profile:
                    response = soapHelper_15_2.getLteSoap()
                        .enbAdvancedConfigProfileGet(nameList, credentialsLte);
                    break;
                case Cell_Advanced_Profile:
                    response = soapHelper_15_2.getLteSoap()
                        .cellAdvancedConfigProfileGet(nameList, credentialsLte);
                    break;
                case Management_Profile:
                    response = soapHelper_15_2.getLteSoap().managementProfileGet(nameList,
                        credentialsLte);
                    break;
                case Mobility_Profile:
                    response = soapHelper_15_2.getLteSoap().mobilityProfileGet(nameList,
                        credentialsLte);
                    break;
                case Network_Profile:
                    response = soapHelper_15_2.getLteSoap().networkProfileGet(nameList,
                        credentialsLte);
                    break;
                case Radio_Profile:
                    response = soapHelper_15_2.getLteSoap().radioProfileGet(nameList,
                        credentialsLte);
                    break;
                case Security_Profile:
                    response = soapHelper_15_2.getLteSoap().securityProfileGet(nameList,
                        credentialsLte);
                    break;
                case Son_Profile:
                    response = soapHelper_15_2.getLteSoap().sonProfileGet(nameList,
                        credentialsLte);
                    break;
                case Sync_Profile:
                    response = soapHelper_15_2.getLteSoap().syncProfileGet(nameList,
                        credentialsLte);
                    break;
                case MultiCell_Profile:
                    response = soapHelper_15_2.getLteSoap().multiCellProfileGet(nameList,
                        credentialsLte);
                    break;

                default:
                    report.report("isProfileExists get error EnbProfiles type: " + profileType, Reporter.WARNING);
            }
            return response.getErrorCode() == Netspan.NBI_15_2.Lte.ErrorCodes.OK;

        } catch (Exception e) {
            e.printStackTrace();
            report.report(profileType.value() + "ProfileGet Failed, due to: " + e.getMessage(), Reporter.WARNING);
            return false;
        } finally {
            soapHelper_15_2.endLteSoap();
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
        return enbSetContent(node, enbConfigSet, null);
    }

    public boolean setProfilesInCell2(EnodeB node, String cellAdvancedProfile, String radioProfile, String MobilityProfile, String eMBMSProfile, String TrafficManagementProfile, String callTraceProfile){
        report.report("setProfilesInCell2 function is not implemented for this netspan(15_2)!");
        return false;
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
        return enbSetContent(node, enbConfigSet, null);
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
    private boolean enbSetContent(EnodeB node, LteEnbDetailsSetWs enbConfigSet, SnmpDetailSetWs snmpDetailSetWs) {
        try {
            Netspan.NBI_15_2.Lte.NodeActionResult result = soapHelper_15_2
                .getLteSoap().enbConfigSet(node.getNetspanName(), enbConfigSet, snmpDetailSetWs, credentialsLte);

            if (result.getErrorCode() != Netspan.NBI_15_2.Lte.ErrorCodes.OK) {
                report.report("enbConfigSet via Netspan Failed : " + result.getErrorString(), Reporter.WARNING);
                List<NodeResult> nodeErrors = result.getNode();
                for (NodeResult error : nodeErrors) {
                    if (error != null) {
                        report.report("SubResultError:" + error.getNodeResultString(), Reporter.WARNING);
                    }
                }
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
            soapHelper_15_2.endLteSoap();
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
            ProfileResponse cloneResult = soapHelper_15_2.getLteSoap()
                .radioProfileClone(cloneFromName, radioProfile, credentialsLte);

            boolean result = false;
            if (cloneResult == null) {
                report.report("Fail to get Radio profile cloning result", Reporter.WARNING);
                result = false;
            } else if (cloneResult.getErrorCode() == Netspan.NBI_15_2.Lte.ErrorCodes.OK) {
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
            soapHelper_15_2.endLteSoap();
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
            ProfileResponse cloneResult = soapHelper_15_2.getLteSoap().radioProfileUpdate(profileName,
                radioProfile, credentialsLte);

            if (cloneResult == null) {
                report.report("Fail to get Radio profile update result. ", Reporter.WARNING);
                return false;
            } else if (cloneResult.getErrorCode() == Netspan.NBI_15_2.Lte.ErrorCodes.OK) {
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
            soapHelper_15_2.endLteSoap();
        }
    }

    protected EnbRadioProfile createEnbRadioProfile(RadioParameters radioParams) {
        ObjectFactory factoryDetails = new ObjectFactory();
        EnbRadioProfile radioProfile = new EnbRadioProfile();

        if (radioParams.getProfileName() == null) {
            return new EnbRadioProfile();
        } else {
            radioProfile.setName(radioParams.getProfileName());
        }

        if (radioParams.getBand() != null) {
            radioProfile.setBand(factoryDetails.createEnbRadioProfileBand(radioParams.getBand()));
        }

        if (radioParams.getBandwidth() != null) {
            radioProfile.setBandwidthMhz(factoryDetails.createEnbRadioProfileBandwidthMhz(radioParams.getBandwidth()));
        }
        if (radioParams.getDownLinkFrequency() != null) {
            radioProfile.setDownlinkFreqKHz(
                factoryDetails.createEnbRadioProfileUplinkFreqKHz(radioParams.getDownLinkFrequency()));
        }

        if (radioParams.getDuplex() != null) {
            RfProfileDuplexModes duplexMode = radioParams.getDuplex().contains("1") ? RfProfileDuplexModes.FDD
                : RfProfileDuplexModes.TDD;
            radioProfile.setDuplexMode(factoryDetails.createEnbRadioProfileDuplexMode(duplexMode));
        }
        if (radioParams.getEarfcn() != null) {
            radioProfile.setEarfcn(factoryDetails.createEnbRadioProfileEarfcn(radioParams.getEarfcn()));
        }
        if (radioParams.getFrameConfig() != null) {
            TddFrameConfigurationsSupported fc = radioParams.getFrameConfig().contains("1")
                ? TddFrameConfigurationsSupported.DL_40_UL_40_SP_20
                : TddFrameConfigurationsSupported.DL_60_UL_20_SP_20;
            radioProfile.setFrameConfig(factoryDetails.createEnbRadioProfileFrameConfig(fc));
        }
        if (radioParams.getTxpower() != null) {
            radioProfile.setTxPower(factoryDetails.createEnbRadioProfileTxPower(radioParams.getTxpower()));
        }

        if (radioParams.getUpLinkFrequency() != null) {
            radioProfile.setUplinkFreqKHz(
                factoryDetails.createEnbRadioProfileUplinkFreqKHz(radioParams.getUpLinkFrequency()));
        }
        return radioProfile;
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
            ObjectFactory factoryDetails = new ObjectFactory();
            EnbSonProfile sonProfile = new EnbSonProfile();
            EnbDetailsGet nodeConf = getNodeConfig(node);
            CategoriesLte category = null;
            try {
                category = CategoriesLte.fromValue(nodeConf.getHardware());
            } catch (Exception e) {
                report.report("Incompatible HardWare Type in inner Netspan method!", Reporter.WARNING);
                GeneralUtils.printToConsole("Error : " + e.getMessage() + ", nodeConfig :" + nodeConf.getHardware());
                return false;
            }

            sonProfile.setHardwareCategory(factoryDetails.createCallTraceProfileHardwareCategory(category));
            sonProfile.setName(sonParmas.getProfileName());

            if (sonParmas.isSonCommissioning() != null && sonParmas.isSonCommissioning()) {
                sonProfile.setSonCommissioningEnabled(
                    factoryDetails.createEnbSonProfileSonCommissioningEnabled(sonParmas.isSonCommissioning()));
                // disable change of parameter due to NBI Change of param from
                // boolean to enum
                sonProfile.setPciEnabled(factoryDetails.createEnbSonProfilePciEnabled(sonParmas.isAutoPCIEnabled()));
                if (sonParmas.isAutoPCIEnabled() != null && sonParmas.isAutoPCIEnabled()) {
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
                sonProfile.setRsiEnabled(factoryDetails.createEnbSonProfileRsiEnabled(sonParmas.isAutoRSIEnabled()));
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
                sonProfile.setAnrState(factoryDetails.createEnbSonProfileAnrState(anrState));
                sonProfile.setPnpMode(factoryDetails.createEnbSonProfilePnpMode(sonParmas.getPnpMode()));
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
                    if (anrState == SonAnrStates.HO_MEASUREMENT) {
                        sonProfile.setMinAllowedHoSuccessRate(factoryDetails
                            .createEnbSonProfileMinAllowedHoSuccessRate(sonParmas.getMinAllowedHoSuccessRate()));
                    }

                }
            }
            if (sonParmas.isCSonEnabled() != null && sonParmas.isCSonEnabled()) {
                LteSonCSonWs sonProfileCSon = factoryDetails.createLteSonCSonWs();
                sonProfileCSon
                    .setIsCSonEnabled(factoryDetails.createLteSonCSonWsIsCSonEnabled(sonParmas.isCSonEnabled()));
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
                sonProfile.setCSon(sonProfileCSon);
            }
            ProfileResponse cloneResult = soapHelper_15_2.getLteSoap().sonProfileClone(cloneFromName,
                sonProfile, credentialsLte);

            boolean result = false;
            if (cloneResult == null) {
                report.report("Fail to get Son profile cloning resut", Reporter.WARNING);
                result = false;
            } else if (cloneResult.getErrorCode() == Netspan.NBI_15_2.Lte.ErrorCodes.OK) {
                report.report("Succeeded to clone Son profile, new profile name: " + sonParmas.getProfileName());
                result = true;
            } else {
                report.report("Failed to clone Son profile, reason: " + cloneResult.getErrorString(), Reporter.WARNING);
                result = false;
            }
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            report.report("Failed to clone Son profile, due to: " + e.getMessage(), Reporter.WARNING);
            return false;
        } finally {
            soapHelper_15_2.endLteSoap();
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
        try {
            ProfileResponse cloneResult = soapHelper_15_2.getLteSoap().syncProfileClone(cloneFromName,
                syncProfile, credentialsLte);

            boolean result = false;
            if (cloneResult == null) {
                report.report("Fail to get Sync profile cloning result", Reporter.WARNING);
                result = false;
            } else if (cloneResult.getErrorCode() == Netspan.NBI_15_2.Lte.ErrorCodes.OK) {
                report.report("Succeeded to clone Sync profile, new profile name: " + syncParams.getProfileName());
                result = true;
            } else {
                report.report("Failed to clone Sync profile, reason: " + cloneResult.getErrorString(),
                    Reporter.WARNING);
                result = false;
            }
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            report.report("cloneSyncProfile Failed, due to: " + e.getMessage(), Reporter.WARNING);
            return false;
        } finally {
            soapHelper_15_2.endLteSoap();
        }
    }

    @Override
    public PrimaryClockSourceEnum getPrimaryClockSource(EnodeB node) {
        String profile = getNodeConfig(node).getSyncProfile();
        List<String> nameList = new ArrayList<String>();
        nameList.add(profile);
        try {
            LteSyncProfileGetResult syncProfile = soapHelper_15_2.getLteSoap()
                .syncProfileGet(nameList, credentialsLte);
            JAXBElement<ClockSources> clockSource = syncProfile.getSyncProfileResult().get(FIRST_ELEMENT)
                .getSyncProfile().getPrimaryClockSource();
            if (clockSource.getValue() == ClockSources.NONE) {
                return PrimaryClockSourceEnum.NONE;
            }
            if (clockSource.getValue() == ClockSources.GNSS) {
                return PrimaryClockSourceEnum.GNSS;
            }
            if (clockSource.getValue() == ClockSources.IEEE_1588) {
                return PrimaryClockSourceEnum.IEEE_1588;
            }
        } catch (Exception e) {
            e.printStackTrace();
            report.report("getPrimaryClockSource Failed, reason: " + e.getMessage(), Reporter.WARNING);
            return null;
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
            MobilityConnectedModeInterFreq mobilityConnectedMode = null;
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

            if (mobilityParams.getThresholdBasedMeasurement() != null) {
                mobilityProfile.setIsThresholdBasedMeasurementEnabled(
                    factoryDetails.createEnbMobilityProfileParamsIsThresholdBasedMeasurementEnabled(
                        mobilityParams.getThresholdBasedMeasurement()));
                if (mobilityParams.getThresholdBasedMeasurement() == EnabledStates.ENABLED) {
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

            ProfileResponse cloneResult = soapHelper_15_2.getLteSoap()
                .mobilityProfileClone(cloneFromName, mobilityProfile, credentialsLte);
            boolean result = false;
            if (cloneResult == null) {
                report.report("Fail to get Mobility profile cloning result", Reporter.WARNING);
                result = false;
            } else if (cloneResult.getErrorCode() == Netspan.NBI_15_2.Lte.ErrorCodes.OK) {
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
            soapHelper_15_2.endLteSoap();
        }
    }

    @Override
    public boolean cloneNetworkProfile(EnodeB node, String cloneFromName, NetworkParameters networkParams)
        throws Exception {
        EnbNetworkProfile networkProfile = new EnbNetworkProfile();
        if (networkParams.getProfileName() == null) {
            return false;
        }
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
        if (networkParams.getcSonConfig() != null) {
            ObjectFactory factoryDetails = new ObjectFactory();
            LteCSonEntryWs cSonConfig = new LteCSonEntryWs();
            cSonConfig.setIsCSonConfigured(
                factoryDetails.createLteCSonEntryWsIsCSonConfigured(networkParams.getIsCSonConfigured()));
            cSonConfig.setCSonIpAddress(networkParams.getcSonIpAddress());
            cSonConfig.setCSonServerPort(
                factoryDetails.createLteCSonEntryWsCSonServerPort(networkParams.getcSonServerPort()));
            networkProfile.setCSONConfig(cSonConfig);
        }
        ProfileResponse cloneResult = soapHelper_15_2.getLteSoap().networkProfileClone(cloneFromName,
            networkProfile, credentialsLte);
        boolean result = false;
        if (cloneResult == null) {
            report.report("Fail to get Network profile cloning result", Reporter.WARNING);
            soapHelper_15_2.endLteSoap();
            result = false;
        } else if (cloneResult.getErrorCode() == Netspan.NBI_15_2.Lte.ErrorCodes.OK) {
            report.report("Succeeded to clone Network profile, new profile name: " + networkParams.getProfileName());
            soapHelper_15_2.endLteSoap();
            result = true;
        } else {
            report.report("Failed to clone Network profile, reason: " + cloneResult.getErrorString(), Reporter.WARNING);
            soapHelper_15_2.endLteSoap();
            result = false;
        }

        return result;
    }

    @Override
    public boolean cloneSecurityProfile(EnodeB node, String cloneFromName, SecurityParameters securityParams)
        throws Exception {
        String profilePreFix = GeneralUtils.getPrefixAutomation();
        LteSecurityProfileGetResult currentSecurityProfile = null;
        ArrayList<String> profilesList = new ArrayList<String>();
        profilesList.add(cloneFromName);
        try {
            currentSecurityProfile = soapHelper_15_2.getLteSoap()
                .securityProfileGet(profilesList, credentialsLte);
            if (currentSecurityProfile.getErrorCode() != Netspan.NBI_15_2.Lte.ErrorCodes.OK) {
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
            ProfileResponse cloneResult = soapHelper_15_2.getLteSoap()
                .securityProfileClone(cloneFromName, newSecurityProfile, credentialsLte);
            boolean result = false;
            if (cloneResult.getErrorCode() != Netspan.NBI_15_2.Lte.ErrorCodes.OK) {
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
            soapHelper_15_2.endLteSoap();
        }
    }

    @Override
    public boolean convertToPnPConfig(EnodeB node) {
        boolean actionSucceeded;
        LteEnbDetailsSetWs enbConfigSet = EnbConfigGetToSet(getNodeConfig(node));
        PnpDetailWs pnpDetail = getPnpDetails(node);
        PnpConfigCreate pnpConfigCreate = new PnpConfigCreate();
        pnpConfigCreate.setENBDetail(enbConfigSet);
        pnpConfigCreate.setPnpDetail(pnpDetail);
        removeDiscoveryTask(node.getNetspanName());
        removeNode(node.getNetspanName());
        actionSucceeded = createPnpNode(pnpConfigCreate);
        return actionSucceeded;
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
            NodeActionResult result = soapHelper_15_2.getInventorySoap().nodeDelete(nodeList, null,
                credentialsInventory);

            GeneralUtils.printToConsole(String.format("NBI method \"nodeDelete\" for eNodeB %s returned value: %s",
                nodeName, result.getErrorCode().toString()));

            if (result.getErrorCode() != ErrorCodes.OK) {
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
            soapHelper_15_2.endInventorySoap();
        }
    }

    public boolean removeDiscoveryTask(String nodeName) {
        ArrayList<String> nodeList = new ArrayList<String>();
        nodeList.add(nodeName);
        try {
            GeneralUtils.printToConsole("Sending NBI requeset \"discoveryTaskDelete\" for eNodeB " + nodeName);
            DiscoveryTaskActionResult result = soapHelper_15_2.getInventorySoap()
                .discoveryTaskDelete(nodeList, credentialsInventory);
            GeneralUtils
                .printToConsole(String.format("NBI method \"discoveryTaskDelete\" for eNodeB %s returned value: %s",
                    nodeName, result.getErrorCode().toString()));

            if (result.getErrorCode() != ErrorCodes.OK) {
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
            soapHelper_15_2.endInventorySoap();
        }
    }

    @Override
    public boolean setManagedMode(String nodeName, NodeManagementModes managedMode) {
        Netspan.NBI_15_2.Inventory.ObjectFactory factory = new Netspan.NBI_15_2.Inventory.ObjectFactory();
        ArrayList<NodeManagementMode> nodeList = new ArrayList<NodeManagementMode>();
        NodeManagementMode node = new NodeManagementMode();
        node.setNodeNameOrId(nodeName);
        node.setManagementMode(
            factory.createNodeManagementModeManagementMode(NodeManagementModes.fromValue(managedMode.value())));
        nodeList.add(node);
        try {
            NodeActionResult result = soapHelper_15_2.getInventorySoap()
                .nodeManagementModeSet(nodeList, credentialsInventory);
            if (result.getErrorCode() != Netspan.NBI_15_2.Inventory.ErrorCodes.OK) {
                report.report("nodeManagementModeSet Failed : " + result.getErrorString(), Reporter.WARNING);
                return false;
            } else
                return true;

        } catch (Exception e) {
            e.printStackTrace();
            report.report("nodeManagementModeSet Failed, due to: " + e.getMessage(), Reporter.WARNING);
            return false;
        } finally {
            soapHelper_15_2.endInventorySoap();
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
            result = soapHelper_15_2.getInventorySoap().discoveryTaskAddSnmpV2(enodeB.getNetspanName(),
                enodeB.getIpAddress(), 161, pswrdRead, pswrdWrite, credentialsInventory);
            GeneralUtils.printToConsole("NBI method discoveryTaskAddSnmpV2 for eNodeB " + enodeB.getNetspanName()
                + " returned value: " + result.getErrorCode().toString());
            if (result.getErrorCode() != ErrorCodes.OK) {
                report.report("discoveryTaskAddSnmpV2 Failed : " + result.getErrorString(), Reporter.WARNING);
                return false;
            } else
                return true;
        } catch (Exception e) {
            e.printStackTrace();
            report.report("discoveryTaskAddSnmpV2 Failed, due to: " + e.getMessage(), Reporter.WARNING);
            return false;
        } finally {
            soapHelper_15_2.endInventorySoap();
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
            Netspan.NBI_15_2.Lte.NodeActionResult result = soapHelper_15_2
                .getLteSoap().pnpConfigDelete(nodeList, credentialsLte);

            if (result.getErrorCode() != Netspan.NBI_15_2.Lte.ErrorCodes.OK) {
                report.report("pnpConfigDelete Failed : " + result.getErrorString(), Reporter.WARNING);
                return false;
            } else
                return true;

        } catch (Exception e) {
            e.printStackTrace();
            report.report("pnpConfigDelete Failed, due to: " + e.getMessage(), Reporter.WARNING);
            return false;
        } finally {
            soapHelper_15_2.endLteSoap();
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
        Netspan.NBI_15_2.Lte.NodeActionResult result;
        SnmpDetailWs snmpDetailWs = new SnmpDetailWs();
        try {
            result = soapHelper_15_2.getLteSoap().pnpConfigUpdate(
                enodeB.getNetspanName(), pnpConfig.getLtePnpConfig(),
                EnbConfigGetToSet(pnpConfig.getLteEnbConfig()), snmpDetailWs, credentialsLte);
            GeneralUtils.printToConsole(
                "NBI method \"pnpConfigUpdate\" returned value: " + result.getErrorCode().toString());
            return result.getErrorCode() == Netspan.NBI_15_2.Lte.ErrorCodes.OK;
        } catch (Exception e) {
            e.printStackTrace();
            report.report("pnpConfigUpdate Failed, due to: " + e.getMessage(), Reporter.WARNING);
            return false;
        } finally {
            soapHelper_15_2.endLteSoap();
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
                LtePnpConfigGetResult result = soapHelper_15_2.getLteSoap()
                    .pnpConfigGet(nodeList, credentialsLte);
                GeneralUtils
                    .printToConsole(String.format("NBI method \"PnpConfigGet\" for eNodeB %s returned value: %s",
                        nodeName, result.getErrorCode().toString()));
                if (result.getErrorCode() != Netspan.NBI_15_2.Lte.ErrorCodes.OK) {
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
                soapHelper_15_2.endLteSoap();
            }
        }
        return null;
    }

    private boolean isPnPNodeExists(String pnpNodeName) {
        GeneralUtils.printToConsole("Sending NBI requeset \"pnpConfigList\"");
        NodeListResult result;
        try {
            result = soapHelper_15_2.getLteSoap().pnpConfigList(credentialsLte);
            GeneralUtils
                .printToConsole("NBI method \"pnpConfigList\" returned value: " + result.getErrorCode().toString());
            if (result.getErrorCode() == Netspan.NBI_15_2.Lte.ErrorCodes.OK) {
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
            soapHelper_15_2.endLteSoap();
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

    private boolean createPnpNode(PnpConfigCreate pnpConfigCreate) {
        report.report(String.format("%s - Add node to PnP list.", pnpConfigCreate.getENBDetail().getName()));
        GeneralUtils.printToConsole("Sending NBI requeset \"pnpConfigCreate\"");
        Netspan.NBI_15_2.Lte.NodeActionResult result;
        SnmpDetailWs snmpDetailWs = new SnmpDetailWs();
        try {
            result = soapHelper_15_2.getLteSoap().pnpConfigCreate(
                pnpConfigCreate.getPnpDetail(), pnpConfigCreate.getENBDetail(), snmpDetailWs, credentialsLte);

            if (result.getErrorCode() != Netspan.NBI_15_2.Lte.ErrorCodes.OK) {
                report.report("pnpConfigCreate via Netspan Failed : " + result.getErrorString(), Reporter.WARNING);
                return false;
            } else
                return true;
        } catch (Exception e) {
            report.report("pnpConfigCreate via Netspan Failed due to: " + e.getMessage(), Reporter.WARNING);
            e.printStackTrace();
            return false;
        } finally {
            soapHelper_15_2.endLteSoap();
        }
    }

    public boolean isPnpNode(String nodeName) {
        boolean isPnp = false;
        NodeListResult result = null;
        try {
            result = soapHelper_15_2.getLteSoap().pnpConfigList(credentialsLte);
        } catch (Exception e) {
            e.printStackTrace();
            report.report("isPnpNode failed due to: " + e.getMessage(), Reporter.WARNING);
            return false;
        }
        if (result.getErrorCode() != Netspan.NBI_15_2.Lte.ErrorCodes.OK)
            return false;
        for (NodeSimple node : result.getNode()) {
            if (node.getName().equals(nodeName))
                isPnp = true;
        }
        soapHelper_15_2.endLteSoap();
        return isPnp;
    }

    @Override
    public boolean isNetspanReachable() {
        try {
            soapHelper_15_2.getLteSoap();
        } catch (Exception e) {
            e.printStackTrace();
            report.report("Netspan is not reachable due to: " + e.getMessage(), Reporter.WARNING);
            return false;
        } finally {
            soapHelper_15_2.endLteSoap();
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
            Netspan.NBI_15_2.Lte.NodeActionResult result = soapHelper_15_2
                .getLteSoap().enbConfigSet(enodeB.getNetspanName(), netspanConfig, null, credentialsLte);
            GeneralUtils.printToConsole(String.format("NBI method \"enbConfigSet\" for eNodeB %s returned value: %s",
                enodeB.getNetspanName(), result.getErrorCode() + ":" + result.getErrorString()));
            if (result.getErrorCode() == Netspan.NBI_15_2.Lte.ErrorCodes.OK)
                return true;
            else {
                for (Netspan.NBI_15_2.Lte.NodeResult node : result.getNode()) {
                    if (node.getNodeResultCode() == NodeResultValues.NODE_ERROR) {
                        report.report("[NMS] ERROR in " + node.getName() + ": " + node.getNodeResultString());
                    }
                }
            }
        } catch (Exception e) {
            report.report("enbConfigSet via Netspan Failed due to: " + e.getMessage(), Reporter.WARNING);
            e.printStackTrace();
            return false;
        } finally {
            soapHelper_15_2.endLteSoap();
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
     * @see Netspan.NetspanServer#changeNbrAutoX2ControlFlag(EnodeB,
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
     * @see Netspan.NetspanServer#changeNbrX2ConfiguratioUpdateFlag(EnodeB,
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
            Lte3RdPartyResponse result = soapHelper_15_2.getLteSoap()
                .lte3RdPartyCreate(soapDetails, credentialsLte);
            GeneralUtils.printToConsole(
                "NBI method \"Lte3rdPartyCreate\"  returned value: " + result.getErrorCode().toString());
            if (result.getErrorCode() != Netspan.NBI_15_2.Lte.ErrorCodes.OK) {
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
            soapHelper_15_2.endLteSoap();
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
            NodeSoftwareGetResult = soapHelper_15_2.getStatusSoap()
                .nodeSoftwareStatusGet(nodeName, credentialsStatus);
            if (NodeSoftwareGetResult.getErrorCode() != Netspan.NBI_15_2.Status.ErrorCodes.OK) {
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
            soapHelper_15_2.endStatusSoap();
        }
    }

    @Override
    public String getStandbyVer(String nodeName) {
        NodeSoftwareGetResult NodeSoftwareGetResult = null;
        try {
            NodeSoftwareGetResult = soapHelper_15_2.getStatusSoap()
                .nodeSoftwareStatusGet(nodeName, credentialsStatus);
            if (NodeSoftwareGetResult.getErrorCode() != Netspan.NBI_15_2.Status.ErrorCodes.OK) {
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
            soapHelper_15_2.endStatusSoap();
        }
    }

    @Override
    public boolean delete3rdParty(ArrayList<String> names) {
        Lte3RdPartyResponse result = null;
        if (names.isEmpty() || names == null)
            return true;
        try {
            result = soapHelper_15_2.getLteSoap().lte3RdPartyDelete(names, credentialsLte);
            if (result.getErrorCode() != Netspan.NBI_15_2.Lte.ErrorCodes.OK) {
                report.report("lte3RdPartyDelete via Netspan Failed : "
                    + result.getLte3RdPartyResult().get(0).getResultString(), Reporter.WARNING);
                return false;
            }
            return result.getErrorCode() == Netspan.NBI_15_2.Lte.ErrorCodes.OK;
        } catch (Exception e) {
            report.report("lte3RdPartyDelete via Netspan Failed due to: " + e.getMessage(), Reporter.WARNING);
            e.printStackTrace();
            return false;
        } finally {
            soapHelper_15_2.endLteSoap();
        }
    }

    @Override
    public List<String> getAll3rdParty() {
        NameResult nameResult = null;
        try {
            nameResult = soapHelper_15_2.getLteSoap().lte3RdPartyList(credentialsLte);
            if (nameResult.getErrorCode() != Netspan.NBI_15_2.Lte.ErrorCodes.OK) {
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
            soapHelper_15_2.endLteSoap();
        }
    }

    @Override
    public HashMap<ConnectedUETrafficDirection, HashMap<Integer, Integer>> getUeConnectedPerCategory(EnodeB enb) {
    	LteUeGetResult lteUeGetResult;
        HashMap<Integer, Integer> res = new HashMap<>();
        HashMap<ConnectedUETrafficDirection, HashMap<Integer, Integer>> ret = new HashMap<>();

        try {
        	StatusSoap status = soapHelper_15_2.getStatusSoap();
        	lteUeGetResult = status.enbConnectedUeStatusGet(enb.getNetspanName(), credentialsStatus);
            if (lteUeGetResult.getErrorCode() != Netspan.NBI_15_2.Status.ErrorCodes.OK) {
                report.report("enbConnectedUeStatusGet via Netspan Failed : " + lteUeGetResult.getErrorString(),
                    Reporter.WARNING);
                return ret;
            }
            for (LteUeStatusWs currentCell : lteUeGetResult.getCell()) {
                if (currentCell.getCellId().getValue() == enb.getCellContextID()) {
                    List<LteUeCategory> catDataList = currentCell.getCategoryData();

                    for (LteUeCategory catData : catDataList) {
                        res.put(catData.getCategory().getValue(), catData.getConnectedUes().getValue());
                    }
                    ret.put(ConnectedUETrafficDirection.ALL, res);
                    GeneralUtils.printToConsole(
                        "enbConnectedUeStatusGet via Netspan for eNodeB " + enb.getNetspanName() + " succeeded");
                    return ret;
                }
            }
        } catch (Exception e) {
            report.report("enbConnectedUeStatusGet via Netspan Failed due to: " + e.getMessage(), Reporter.WARNING);
            e.printStackTrace();
            return new HashMap<ConnectedUETrafficDirection, HashMap<Integer, Integer>>();
        } finally {
            soapHelper_15_2.endStatusSoap();
        }
        GeneralUtils.printToConsole(
            "enbConnectedUeStatusGet via Netspan for eNodeB " + enb.getNetspanName() + " succeeded");
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
            lteSonAnrGetResult = soapHelper_15_2.getStatusSoap()
                .enbSonAnrStatusGet(enodeB.getNetspanName(), credentialsStatus);
            if (lteSonAnrGetResult.getErrorCode() != Netspan.NBI_15_2.Status.ErrorCodes.OK) {
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
            soapHelper_15_2.endStatusSoap();
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
        Netspan.NBI_15_2.Lte.NodeActionResult result = null;
        try {
            result = soapHelper_15_2.getLteSoap()
                .enbStateSet(enb.getNetspanName(), enbState, credentialsLte);
        } catch (Exception e) {
            report.report("enbStateSet via Netspan Failed due to: " + e.getMessage(), Reporter.WARNING);
            e.printStackTrace();
            return false;
        } finally {
            soapHelper_15_2.endLteSoap();
        }
        if (result.getErrorCode() != Netspan.NBI_15_2.Lte.ErrorCodes.OK) {
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
            result = soapHelper_15_2.getLteSoap().networkProfileGet(profilesName,
                credentialsLte);
            // add parameters needed from profile to NetworkParameters Class for
            // future use.
            if (result.getErrorCode() != Netspan.NBI_15_2.Lte.ErrorCodes.OK) {
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
            soapHelper_15_2.endLteSoap();
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
            result = soapHelper_15_2.getInventorySoap().nodeInfoGet(nodeListName,
                new ArrayList<String>(), credentialsInventory);
        } catch (Exception e) {
            report.report("nodeInfoGet via Netspan Failed due to: " + e.getMessage(), Reporter.WARNING);
            e.printStackTrace();
            return null;
        } finally {
            soapHelper_15_2.endInventorySoap();
        }
        if (result.getErrorCode() != Netspan.NBI_15_2.Inventory.ErrorCodes.OK) {
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
            rfStatusResult = soapHelper_15_2.getStatusSoap().enbRfStatusGet(enodeB.getNetspanName(),
                credentialsStatus);
        } catch (Exception e) {
            report.report("enbRfStatusGet via Netspan Failed due to: " + e.getMessage(), Reporter.WARNING);
            e.printStackTrace();
            return new ArrayList<RFStatus>();
        } finally {
            soapHelper_15_2.endStatusSoap();
        }
        if (rfStatusResult.getErrorCode() != Netspan.NBI_15_2.Status.ErrorCodes.OK) {
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
        ILteEnbDetailsGet temp = getNodeConfig(enb);
        if (temp == null)
            return null;
        EnbNetworkProfile network = networkProfileGet(temp.getNetworkProfile());
        String mmeIp = network.getS1X2List().getS1().get(0).getMmeIpAddress();
        return mmeIp;
    }

    @Override
    public ArrayList<String> getMMEIpAdresses(EnodeB enb) {
        ArrayList<String> mmeIps = new ArrayList<>();
        ILteEnbDetailsGet temp = getNodeConfig(enb);
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
            ptpStatus = soapHelper_15_2.getStatusSoap().nodePtpGet(dut.getNetspanName(),
                credentialsStatus);
        } catch (Exception e) {
            report.report("nodePtpGet via Netspan Failed due to: " + e.getMessage(), Reporter.WARNING);
            e.printStackTrace();
            return null;
        } finally {
            soapHelper_15_2.endStatusSoap();
        }
        if (ptpStatus.getErrorCode() != Netspan.NBI_15_2.Status.ErrorCodes.OK) {
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
            alarmsList = soapHelper_15_2.getFaultManagementSoap().alarmListNode(argsNodeName, null,
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
            soapHelper_15_2.endFaultManagementSoap();
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
            alarmsList = soapHelper_15_2.getFaultManagementSoap().alarmListNode(argsNodeName, null,
                credentialsFaultManagement);
        } catch (Exception e) {
            report.report("alarmListNode via Netspan Failed due to: " + e.getMessage(), Reporter.WARNING);
            e.printStackTrace();
            return ret;
        } finally {
            soapHelper_15_2.endFaultManagementSoap();
        }
        if (alarmsList.getErrorCode() != Netspan.NBI_15_2.FaultManagement.ErrorCodes.OK) {
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
            deleteResult = soapHelper_15_2.getFaultManagementSoap().alarmDelete(argsAlarmId,
                credentialsFaultManagement);
            if (deleteResult.getErrorCode() != Netspan.NBI_15_2.FaultManagement.ErrorCodes.OK) {
                report.report("alarmDelete with id: " + alarmId + " via Netspan Failed", Reporter.WARNING);
                report.report(deleteResult.getErrorString(), Reporter.WARNING);
                return false;
            }
        } catch (Exception e) {
            report.report("alarmDelete with id: " + alarmId + " via Netspan Failed", Reporter.WARNING);
            e.printStackTrace();
            return false;
        } finally {
            soapHelper_15_2.endFaultManagementSoap();
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
            events = soapHelper_15_2.getFaultManagementSoap().eventListNode(nodeNames, null, start,
                end, credentialsFaultManagement);
        } catch (Exception e) {
            report.report("getEventsNode via netspan Failed due to: " + e.getMessage(), Reporter.WARNING);
            e.printStackTrace();
            return eventsList;

        } finally {
            soapHelper_15_2.endFaultManagementSoap();
        }
        if (events.getErrorCode() != Netspan.NBI_15_2.FaultManagement.ErrorCodes.OK) {
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
            ProfileResponse response = soapHelper_15_2.getLteSoap()
                .radioProfileUpdate(rp.getProfileName(), radioProfile, credentialsLte);
            if (response.getErrorCode() != Netspan.NBI_15_2.Lte.ErrorCodes.OK) {
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
            soapHelper_15_2.endLteSoap();
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
            radioProfile.setEarfcn(factoryDetails.createEnbRadioProfileEarfcn(radioParams.getEarfcn()));
        }

        if (radioParams.getDownLinkFrequency() != null) {
            radioProfile.setDownlinkFreqKHz(
                factoryDetails.createEnbRadioProfileDownlinkFreqKHz(radioParams.getDownLinkFrequency()));
        }

        if (radioParams.getUpLinkFrequency() != null) {
            radioProfile.setUplinkFreqKHz(
                factoryDetails.createEnbRadioProfileUplinkFreqKHz(radioParams.getUpLinkFrequency()));
        }

        if (radioParams.getFrameConfig() != null) {
            TddFrameConfigurationsSupported frameConfiguration;
            if (radioParams.getFrameConfig().equals("1")) {
                frameConfiguration = TddFrameConfigurationsSupported.DL_40_UL_40_SP_20;
            } else {
                frameConfiguration = TddFrameConfigurationsSupported.DL_60_UL_20_SP_20;
            }
            radioProfile.setFrameConfig(factoryDetails.createEnbRadioProfileFrameConfig(frameConfiguration));
        }

        if (radioParams.getBand() != null) {
            radioProfile.setBand(factoryDetails.createEnbRadioProfileBand(radioParams.getBand()));
        }

        if (radioParams.getAdditionalSpectrumEmission() != null) {
            AddlSpectrumEmissions addSpecValue = null;
            switch (radioParams.getAdditionalSpectrumEmission()) {
                case "NS 01": {
                    addSpecValue = AddlSpectrumEmissions.NS_01;
                }
                case "NS 04": {
                    addSpecValue = AddlSpectrumEmissions.NS_04;
                }
                case "NS 12": {
                    addSpecValue = AddlSpectrumEmissions.NS_12;
                }
                case "NS 13": {
                    addSpecValue = AddlSpectrumEmissions.NS_13;
                }
            }
            radioProfile
                .setAddlSpectrumEmission(factoryDetails.createEnbRadioProfileAddlSpectrumEmission(addSpecValue));
        }

        if (radioParams.getDuplex() != null) {
            RfProfileDuplexModes duplex = radioParams.getDuplex().equals("TDD") ? RfProfileDuplexModes.TDD
                : RfProfileDuplexModes.FDD;
            radioProfile.setDuplexMode(factoryDetails.createEnbRadioProfileDuplexMode(duplex));
        }

        if (radioParams.getBandwidth() != null) {
            radioProfile.setBandwidthMhz(factoryDetails.createEnbRadioProfileBandwidthMhz(radioParams.getBandwidth()));
        }

        if (radioParams.getMfbi() != null) {
            radioProfile.setMfbiEnabled(factoryDetails.createEnbRadioProfileMfbiEnabled(radioParams.getMfbi()));
        }

        if (radioParams.getTxpower() != null) {
            radioProfile.setTxPower(factoryDetails.createEnbRadioProfileTxPower(radioParams.getTxpower()));
        }

        if (radioParams.getRmMode() != null) {
            ResourceManagementTypes rmMode = radioParams.getRmMode() ? ResourceManagementTypes.SFR
                : ResourceManagementTypes.DISABLED;
            radioProfile.setRmMode(factoryDetails.createEnbRadioProfileRmMode(rmMode));

            if (radioParams.getRmMode()) {
                if (radioParams.getSFRSegment() != null) {
                    radioProfile.setSfrSegments(
                        factoryDetails.createEnbRadioProfileSfrSegments(radioParams.getSFRSegment()));
                }

                if (radioParams.getSFRIndex() != null) {
                    radioProfile.setSfrIndex(factoryDetails.createEnbRadioProfileSfrIndex(radioParams.getSFRIndex()));
                }

                if (radioParams.getSFRThresholdType() != null) {
                    SfrThresholdTypes sfrType = SfrThresholdTypes.STATIC;
                    radioProfile.setSfrThresholdType(factoryDetails.createEnbRadioProfileSfrThresholdType(sfrType));
                }

                if (radioParams.getSFRThresholdValue() != null) {
                    radioProfile.setSfrThresholdValue(
                        factoryDetails.createEnbRadioProfileSfrThresholdValue(radioParams.getSFRThresholdValue()));
                }

                if (radioParams.getSFRMaxMCSCellEdge() != null) {
                    radioProfile.setSfrMaxMcsCellEdge(
                        factoryDetails.createEnbRadioProfileSfrMaxMcsCellEdge(radioParams.getSFRMaxMCSCellEdge()));
                }

                if (radioParams.getSFRMinMCSCellEdge() != null) {
                    radioProfile.setSfrMinMcsCellCenter(factoryDetails
                        .createEnbRadioProfileSfrMinMcsCellCenter(radioParams.getSFRMinMCSCellEdge()));
                }
            }
        }

        if (radioParams.getECIDMode() != null) {
            EnabledStates state = radioParams.getECIDMode() ? EnabledStates.ENABLED
                : EnabledStates.DISABLED;
            radioProfile.setEcidMode(factoryDetails.createEnbRadioProfileEcidMode(state));
            if (radioParams.getECIDMode()) {
                radioProfile.setEcidTimer(
                    factoryDetails.createEnbRadioProfileEcidTimer(radioParams.getECIDProcedureTimer()));
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
                LteNeighbourResponse result = soapHelper_15_2.getLteSoap()
                    .lteNeighbourAddByCellNumber(neighbourConfigList, credentialsLte);
                if (result.getErrorCode() != Netspan.NBI_15_2.Lte.ErrorCodes.OK) {
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
            soapHelper_15_2.endLteSoap();
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
                    LteNeighbourResponse result = soapHelper_15_2.getLteSoap().lteNeighbourAdd(
                        sourceNodeName, neighborName, hoControlStatus, x2ControlStatus, HandoverType,
                        isStaticNeighbor, qOffsetRange, String.valueOf(enodeB.getCellContextID()), credentialsLte);
                    if (result.getErrorCode() != Netspan.NBI_15_2.Lte.ErrorCodes.OK) {
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
                soapHelper_15_2.endLteSoap();
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
            LteNeighbourResponse result = soapHelper_15_2.getLteSoap()
                .lteNeighbourAddByCellNumber(neighbourConfigList, credentialsLte);
            GeneralUtils.printToConsole("Netspan func:lteNeighbourAddByCellNumber for eNodeB:" + enodeB.getNetspanName()
                + " neighbor:" + neighbor.getNetspanName() + "returned value: "
                + result.getLteNeighbourResult().get(0).getResultString());
            if (result.getLteNeighbourResult().get(0).getResultCode() != LteNeighbourResultValues.OK) {
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
            soapHelper_15_2.endLteSoap();
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
            LteSonAnrGetResult result = soapHelper_15_2.getStatusSoap()
                .enbSonAnrStatusGet(enb.getNetspanName(), credentialsStatus);
            if (result.getErrorCode() != Netspan.NBI_15_2.Status.ErrorCodes.OK) {
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
            soapHelper_15_2.endStatusSoap();
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
            returnObject = soapHelper_15_2.getLteSoap().syncProfileGet(profiles,
                credentialsLte);
        } catch (Exception e) {
            report.report("getSyncProfile via Netspan Failed due to: " + e.getMessage(), Reporter.WARNING);
            e.printStackTrace();
            return null;
        } finally {
            soapHelper_15_2.endLteSoap();
        }
        if (returnObject.getErrorCode() != Netspan.NBI_15_2.Lte.ErrorCodes.OK) {
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
            LteNetworkProfileGetResult lteNetworkProfile = soapHelper_15_2.getLteSoap()
                .networkProfileGet(names, credentialsLte);
            if (lteNetworkProfile.getErrorCode() == Netspan.NBI_15_2.Lte.ErrorCodes.OK) {
                return lteNetworkProfile.getNetworkProfileResult().get(0).getNetworkProfile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            soapHelper_15_2.endLteSoap();
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
            Lte3RdPartyGetResult result = soapHelper_15_2.getLteSoap().lte3RdPartyGet(parties,
                credentialsLte);
            GeneralUtils.printToConsole(
                "NBI method \"Lte3rdPartyCreate\"  returned value: " + result.getErrorCode().toString());
            if (result.getErrorCode() != Netspan.NBI_15_2.Lte.ErrorCodes.OK) {
                report.report("lte3RdPartyGet via Netspan Failed : " + result.getErrorString(), Reporter.WARNING);
                return null;
            }

            if (result != null && result.getLte3RdPartyResult() != null && result.getLte3RdPartyResult().get(0)
                .getResultCode() == Netspan.NBI_15_2.Lte.Lte3RdPartyResultValues.OK)
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
            soapHelper_15_2.endLteSoap();
        }
    }

    @Override
    public boolean setEnbType(String nodeName, EnbTypes type) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean checkIfEsonServerConfigured(EnodeB enb) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean performReProvision(String nodeName) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean checkAndSetDefaultProfiles(EnodeB node, boolean setProfile) {
        report.report("setDefaultProfiles method is not implemented for this netspan(15_2)!");
        return false;
    }

    @Override
    public SONStatus getSONStatus(EnodeB dut) {
        // EnbSonPciStatusGet
        SONStatus returnObject = new SONStatus();
        LteSonPciGetResult sonPciStatus;
        LteSonRsiGetResult sonRsiStatus;
        try {
            sonPciStatus = soapHelper_15_2.getStatusSoap().enbSonPciStatusGet(dut.getNetspanName(),
                credentialsStatus);
            sonRsiStatus = soapHelper_15_2.getStatusSoap().enbSonRsiStatusGet(dut.getNetspanName(),
                credentialsStatus);
        } catch (Exception e) {
            report.report("enbSonStatusGet via Netspan Failed due to: " + e.getMessage(), Reporter.WARNING);
            e.printStackTrace();
            return null;
        } finally {
            soapHelper_15_2.endStatusSoap();
        }
        if (sonPciStatus.getErrorCode() != Netspan.NBI_15_2.Status.ErrorCodes.OK) {
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
                res.emergencyAreaId = (lteCell.getEmergencyAreaId() != null) ? lteCell.getEmergencyAreaId().getValue()
                    : null;
                res.prachFreqOffset = (lteCell.getPrachFreqOffset() != null) ? lteCell.getPrachFreqOffset().getValue()
                    : null;
                res.cellAdvancedConfigurationProfile = lteCell.getCellAdvancedConfigurationProfile();
                res.radioProfile = lteCell.getRadioProfile();
                res.mobilityProfile = lteCell.getMobilityProfile();
                res.embmsProfile = lteCell.getEmbmsProfile();
                res.utranProfile = lteCell.getUtranProfile();
                res.trafficManagementProfile = lteCell.getTrafficManagementProfile();
                res.callTraceProfile = lteCell.getCallTraceProfile();

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

            Netspan.NBI_15_2.Lte.NodeActionResult result = soapHelper_15_2
                .getLteSoap().enbConfigSet(dut.getNetspanName(), enbConfigSet, null, credentialsLte);

            if (result.getErrorCode() != Netspan.NBI_15_2.Lte.ErrorCodes.OK) {
                report.report("setPci via Netspan Failed : " + result.getErrorString(), Reporter.WARNING);
                return false;
            } else
                return true;

        } catch (Exception e) {
            e.printStackTrace();
            report.report("setPci via Netspan Failed due to: " + e.getMessage(), Reporter.WARNING);
            return false;
        } finally {
            soapHelper_15_2.endLteSoap();
        }
    }

    @Override
    public boolean cloneManagementProfile(EnodeB node, String cloneFromName, ManagementParameters managementParmas) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean cloneSystemDefaultProfile(EnodeB node, String cloneFromName,
                                             SystemDefaultParameters systemDefaultParmas) {
        // TODO Auto-generated method stub
        return false;
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

            ProfileResponse cloneResult = soapHelper_15_2.getLteSoap()
                .enbAdvancedConfigProfileClone(cloneFromName, enbAdvancedProfile, credentialsLte);

            boolean result = false;
            if (cloneResult.getErrorCode() == Netspan.NBI_15_2.Lte.ErrorCodes.OK) {
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
            soapHelper_15_2.endLteSoap();
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
    public boolean updateNeighborManagementProfile(EnodeB node, String cloneFromName,
                                                   NeighbourManagementParameters neighbourManagementParams) {
        report.report("updateNeighborManagementProfile via NBI_15_2 Failed : Try to use correct NBI version",
            Reporter.WARNING);
        return false;
    }

    @Override
    public boolean cloneNeighborManagementProfile(EnodeB node, String cloneFromName,
                                                  NeighbourManagementParameters neighbourManagementParams) {
        report.report("cloneNeighborManagementProfile via NBI_15_2 Failed : Try to use correct NBI version",
            Reporter.WARNING);
        return false;
    }

    @Override
    public String getNeighborManagmentProfile(String nodeName) {
        report.report("getNeighborManagmentProfile via NBI_15_2 Failed : Try to use correct NBI version",
            Reporter.WARNING);
        return null;
    }

    @Override
    public NeighborData getNeighborData(String nodeName, String nghName) {
        // TODO Auto-generated method stub
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
            } else {
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
            enbConfigSet.getLteCell().add(lteCellSet);

            Netspan.NBI_15_2.Lte.NodeActionResult result = soapHelper_15_2
                .getLteSoap().enbConfigSet(dut.getNetspanName(), enbConfigSet, null, credentialsLte);

            if (result.getErrorCode() != Netspan.NBI_15_2.Lte.ErrorCodes.OK) {
                report.report("setEnbCellProperties via Netspan Failed : " + result.getErrorString(), Reporter.WARNING);
                return false;
            } else
                return true;

        } catch (Exception e) {
            e.printStackTrace();
            report.report("setEnbCellProperties via Netspan Failed due to: " + e.getMessage(), Reporter.WARNING);
            return false;
        } finally {
            soapHelper_15_2.endLteSoap();
        }
    }

    @Override
    public boolean updateSonProfile(EnodeB node, String cloneFromName, SonParameters sonParmas) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean createSoftwareServer(EnodeBUpgradeServer upgradeServer) {

        SwServerWs softwareServer = CreateSoftwareServerObject(upgradeServer);

        Netspan.NBI_15_2.Software.SwServerResponse result = soapHelper_15_2
            .getSoftwareSoap().softwareServerCreate(softwareServer, credentialsSoftware);
        if (result.getErrorCode() != Netspan.NBI_15_2.Software.ErrorCodes.OK) {
            report.report("softwareServerCreate via Netspan Failed : " + result.getErrorString(), Reporter.WARNING);
            soapHelper_15_2.endSoftwareSoap();
            return false;
        } else
            return true;
    }

    private SwServerWs CreateSoftwareServerObject(EnodeBUpgradeServer upgradeServer) {
        Netspan.NBI_15_2.Software.ObjectFactory objectFactory = new Netspan.NBI_15_2.Software.ObjectFactory();
        SwServerWs softwareServer = new SwServerWs();
        if (upgradeServer.getUpgradeServerName() != null) {
            softwareServer.setName(upgradeServer.getUpgradeServerName());
        }
        if (upgradeServer.getUpgradeServerIp() != null) {
            softwareServer.setServerIpAddress(upgradeServer.getUpgradeServerIp());
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
        Netspan.NBI_15_2.Software.SwServerResponse result = soapHelper_15_2
            .getSoftwareSoap().softwareServerGet(names, credentialsSoftware);
        if (result.getErrorCode() != Netspan.NBI_15_2.Software.ErrorCodes.OK) {
            String resultStr = result.getSoftwareServerResult().get(0).getResultString();
            if (resultStr.contains("Software Server Not Found")) {
                GeneralUtils.printToConsole("Software Server Not Found");
                soapHelper_15_2.endSoftwareSoap();
                return null;
            } else {
                report.report("softwareServerGet via Netspan Failed : " + result.getErrorString(), Reporter.WARNING);
                soapHelper_15_2.endSoftwareSoap();
                return null;
            }

        } else {
            SwServerWs serverresult = result.getSoftwareServerResult().get(0).getSoftwareServer();
            EnodeBUpgradeServer server = new EnodeBUpgradeServer();
            server.setUpgradeServerObject(serverresult.getName(), serverresult.getServerIpAddress(),
                serverresult.getProtocolType().getValue(), serverresult.getUserName(), serverresult.getPassword());
            soapHelper_15_2.endSoftwareSoap();
            return server;
        }

    }

    @Override
    public boolean setSoftwareServer(String name, EnodeBUpgradeServer upgradeServer) {
        SwServerWs softwareServer = CreateSoftwareServerObject(upgradeServer);
        Netspan.NBI_15_2.Software.SwServerResponse result = soapHelper_15_2
            .getSoftwareSoap().softwareServerUpdate(name, softwareServer, credentialsSoftware);
        if (result.getErrorCode() != Netspan.NBI_15_2.Software.ErrorCodes.OK) {
            report.report("softwareServerUpdate via Netspan Failed : " + result.getErrorString(), Reporter.WARNING);
            soapHelper_15_2.endSoftwareSoap();
            return false;
        } else {
            soapHelper_15_2.endSoftwareSoap();
            return true;
        }
    }

    @Override
    public boolean deleteSoftwareServer(String name) {
        ArrayList<String> names = new ArrayList<>();
        Netspan.NBI_15_2.Software.SwServerResponse result = soapHelper_15_2
            .getSoftwareSoap().softwareServerDelete(names, credentialsSoftware);
        if (result.getErrorCode() != Netspan.NBI_15_2.Software.ErrorCodes.OK) {
            report.report("softwareServerDelete via Netspan Failed : " + result.getErrorString(), Reporter.WARNING);
            soapHelper_15_2.endSoftwareSoap();
            return false;
        } else {
            soapHelper_15_2.endSoftwareSoap();
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
            multiCellProfile.setCarrierAggMode(factoryObject.createEnbMultiCellProfileCarrierAggMode(caMode));

            if (multiCellParams.getIntraEnbLoadBalanceMode() != null) {
                multiCellProfile
                    .setIntraEnbLoadBalancingMode(factoryObject.createEnbMultiCellProfileIntraEnbLoadBalancingMode(
                        multiCellParams.getIntraEnbLoadBalanceMode()));
            }

            if (multiCellParams.getIntraEnbLoadBalanceMode() == EnabledStates.ENABLED) {
                // compositeLoadMax
                multiCellProfile.setCompositeLoadDiffMax(factoryObject
                    .createEnbMultiCellProfileCompositeLoadDiffMax(multiCellParams.getCompositeLoadDiffMax()));
                // compositeLoadMin
                multiCellProfile.setCompositeLoadDiffMin(factoryObject
                    .createEnbMultiCellProfileCompositeLoadDiffMin(multiCellParams.getCompositeLoadDiffMin()));
                // calcInterval
                multiCellProfile.setCalculationInterval(factoryObject
                    .createEnbMultiCellProfileCalculationInterval(multiCellParams.getCalculationInterval()));
            }

            ProfileResponse cloneResult = soapHelper_15_2.getLteSoap()
                .multiCellProfileClone(cloneFromName, multiCellProfile, credentialsLte);
            boolean result = false;
            if (cloneResult.getErrorCode() == Netspan.NBI_15_2.Lte.ErrorCodes.OK) {
                report.report(
                    "Succeeded to clone MultiCell profile, new profile name: " + multiCellParams.getProfileName());
                result = true;
            } else {
                report.report("Failed to clone MultiCell profile, reason: " + cloneResult.getErrorString(),
                    Reporter.WARNING);
                result = false;
            }
            return result;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean createSoftwareImage(EnodeBUpgradeImage upgradeImage) {
        Netspan.NBI_15_2.Software.SwImageWs softwareImage = createSoftwareImageObject(upgradeImage);
        Netspan.NBI_15_2.Software.SwFileInfoWs softwareFileInfo = createFileInfoObject(upgradeImage);
        List<Netspan.NBI_15_2.Software.SwFileInfoWs> fileList = softwareImage.getSoftwareFileInfo();
        fileList.add(softwareFileInfo);

        Netspan.NBI_15_2.Software.SwImageResponse result = soapHelper_15_2
            .getSoftwareSoap().softwareImageCreate(softwareImage, credentialsSoftware);
        if (result.getErrorCode() != Netspan.NBI_15_2.Software.ErrorCodes.OK) {
            report.report("softwareServerCreate via Netspan Failed : " + result.getErrorString(), Reporter.WARNING);
            soapHelper_15_2.endSoftwareSoap();
            return false;
        } else {
            soapHelper_15_2.endSoftwareSoap();
            return true;
        }

    }

    private Netspan.NBI_15_2.Software.SwImageWs createSoftwareImageObject(EnodeBUpgradeImage upgradeImage) {

        Netspan.NBI_15_2.Software.SwImageWs softwareImage = new Netspan.NBI_15_2.Software.SwImageWs();
        Netspan.NBI_15_2.Software.ObjectFactory factoryDetails = new Netspan.NBI_15_2.Software.ObjectFactory();
        if (upgradeImage.getUpgradeServerName() != null) {
            softwareImage.setName(upgradeImage.getName());
        }
        if (upgradeImage.getHardwareCategory() != null) {
            softwareImage.setHardwareCategory(factoryDetails.createSwImageWsHardwareCategory(upgradeImage.getHardwareCategory()));
        }
        if (upgradeImage.getUpgradeServerName() != null) {
            softwareImage.setSoftwareServer(upgradeImage.getUpgradeServerName());
        }

        return softwareImage;

    }

    private Netspan.NBI_15_2.Software.SwFileInfoWs createFileInfoObject(EnodeBUpgradeImage upgradeImage) {
        Netspan.NBI_15_2.Software.SwFileInfoWs softwareFileInfo = new Netspan.NBI_15_2.Software.SwFileInfoWs();
        Netspan.NBI_15_2.Software.ObjectFactory objectFactory = new Netspan.NBI_15_2.Software.ObjectFactory();

        softwareFileInfo.setImageType(objectFactory.createSwFileInfoWsImageType(ImageType.fromValue(upgradeImage.getImageType())));

        if (upgradeImage.getBuildPath() != null) {
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
        Netspan.NBI_15_2.Software.SwImageResponse result = soapHelper_15_2
            .getSoftwareSoap().softwareImageGet(names, credentialsSoftware);
        if (result.getErrorCode() != Netspan.NBI_15_2.Software.ErrorCodes.OK) {
            String resultStr = result.getSoftwareImageResult().get(0).getResultString();
            if (resultStr.contains("Software Image Not Found")) {
                GeneralUtils.printToConsole("Software Image Not Found");
                soapHelper_15_2.endSoftwareSoap();
                return null;
            } else {
                report.report("softwareImageGet via Netspan Failed : " + result.getErrorString(), Reporter.WARNING);
                soapHelper_15_2.endSoftwareSoap();
                return null;
            }

        } else {
            Netspan.NBI_15_2.Software.SwImageWs imageResult = result.getSoftwareImageResult().get(0).getSoftwareImage();

            EnodeBUpgradeImage image = new EnodeBUpgradeImage();
            image.setName(imageResult.getName());
            image.setHardwareCategory(imageResult.getHardwareCategory().getValue());
            image.setUpgradeServerName(imageResult.getSoftwareServer());
            image.setBuildPath(imageResult.getSoftwareFileInfo().get(0).getFileNameWithPath());
            image.setVersion(imageResult.getSoftwareFileInfo().get(0).getVersion());
            soapHelper_15_2.endSoftwareSoap();
            return image;
        }
    }

    @Override
    public HardwareCategory getHardwareCategory(EnodeB node) {
        String enbRadioProfileName = this.getCurrentRadioProfileName(node);
        LteRadioProfileGetResult netspanResult = null;
        List<java.lang.String> enbList = new ArrayList<java.lang.String>();
        enbList.add(enbRadioProfileName);
        netspanResult = soapHelper_15_2.getLteSoap().radioProfileGet(enbList,
            credentialsLte);
        if (netspanResult.getErrorCode() != Netspan.NBI_15_2.Lte.ErrorCodes.OK) {
            report.report("getHardwareCategory via Netspan Failed : " + netspanResult.getErrorString(),
                Reporter.WARNING);
            soapHelper_15_2.endSoftwareSoap();
            return null;
        }

        EnbRadioProfile profileresult = netspanResult.getRadioProfileResult().get(0).getRadioProfile();
        CategoriesLte category = profileresult.getHardwareCategory().getValue();
        HardwareCategory hardwareCategory = LTECategoriesToHardwareCategory(category);
        soapHelper_15_2.endSoftwareSoap();
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
        Netspan.NBI_15_2.Software.SwConfigSetWs softwareDetails = createsoftwareDetails(requestType, softwareImageName);
        Netspan.NBI_15_2.Software.NodeActionResult result = soapHelper_15_2.getSoftwareSoap()
            .softwareConfigSet(nodeName, softwareDetails, credentialsSoftware);
        if (result.getErrorCode() != Netspan.NBI_15_2.Software.ErrorCodes.OK) {
            report.report("softwareServerUpdate via Netspan Failed : " + result.getErrorString(), Reporter.WARNING);
            soapHelper_15_2.endSoftwareSoap();
            return false;
        } else {
            soapHelper_15_2.endSoftwareSoap();
            return true;
        }
    }

    private SwConfigSetWs createsoftwareDetails(RequestType requestType, String softwareImageName) {
        Netspan.NBI_15_2.Software.ObjectFactory objectFactory = new Netspan.NBI_15_2.Software.ObjectFactory();
        Netspan.NBI_15_2.Software.SwConfigSetWs softwareDetails = new Netspan.NBI_15_2.Software.SwConfigSetWs();

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
        SoftwareStatusGetWs result = soapHelper_15_2.getSoftwareSoap().softwareStatusGet(enbList, credentialsSoftware);
        if (result.getErrorCode() != Netspan.NBI_15_2.Software.ErrorCodes.OK) {
            report.report("softwareStatusGet via Netspan Failed : " + result.getErrorString(), Reporter.WARNING);
            soapHelper_15_2.endSoftwareSoap();
            return null;
        } else {
            SoftwareStatus newsoftwareStatus = null;
            int numberOfSoftwareStatus = result.getNodeSoftwareStatus().get(0).getSoftwareStatus().size();
            for (int i = 0; i < numberOfSoftwareStatus; i++) {
                NodeSoftwareStatus softwareStatus = result.getNodeSoftwareStatus().get(0).getSoftwareStatus().get(i);
                if ((numberOfSoftwareStatus == 1) || (imageType == null) || imageType.value().equals(softwareStatus.getImageType())) {
                    newsoftwareStatus = new SoftwareStatus();
                    newsoftwareStatus.ImageType = softwareStatus.getImageType().equals("Combined LTE + Relay") ? ImageType.AirDensity.value() : softwareStatus.getImageType();
                    newsoftwareStatus.RunningVersion = softwareStatus.getRunningVersion();
                    newsoftwareStatus.StandbyVersion = softwareStatus.getStandbyVersion();
                    newsoftwareStatus.LastRequested = softwareStatus.getLastRequested();
                    newsoftwareStatus.NmsStatus = softwareStatus.getNmsState();
                    newsoftwareStatus.NodeState = softwareStatus.getNodeState();
                    break;
                }
            }
            soapHelper_15_2.endSoftwareSoap();
            return newsoftwareStatus;
        }
    }

    public boolean updateSoftwareImage(EnodeBUpgradeImage upgradeImage) {
        Netspan.NBI_15_2.Software.SwImageWs softwareImage = createSoftwareImageObject(upgradeImage);
        Netspan.NBI_15_2.Software.SwFileInfoWs softwareFileInfo = createFileInfoObject(upgradeImage);
        List<Netspan.NBI_15_2.Software.SwFileInfoWs> fileList = softwareImage.getSoftwareFileInfo();
        fileList.add(softwareFileInfo);

        Netspan.NBI_15_2.Software.SwImageResponse result = soapHelper_15_2
            .getSoftwareSoap().softwareImageUpdate(softwareImage.getName(), softwareImage, credentialsSoftware);
        if (result.getErrorCode() != Netspan.NBI_15_2.Software.ErrorCodes.OK) {
            report.report("softwareImageUpdate via Netspan Failed : " + result.getErrorString(), Reporter.WARNING);
            soapHelper_15_2.endSoftwareSoap();
            return false;
        } else {
            soapHelper_15_2.endSoftwareSoap();
            return true;
        }
    }

    @Override
    public boolean deleteSoftwareImage(String softwareImageName) {
        List<String> images = new ArrayList<String>();
        images.add(softwareImageName);
        Netspan.NBI_15_2.Software.SwImageResponse result = soapHelper_15_2
            .getSoftwareSoap().softwareImageDelete(images, credentialsSoftware);
        if (result.getErrorCode() != Netspan.NBI_15_2.Software.ErrorCodes.OK) {
            report.report("softwareImageDelete via Netspan Failed : " + result.getErrorString(), Reporter.WARNING);
            soapHelper_15_2.endSoftwareSoap();
            return false;
        } else {
            soapHelper_15_2.endSoftwareSoap();
            return true;
        }
    }

    public int getDLTPTPerQci(String nodeName, int qci) {
        LteIpThroughputGetResult result = soapHelper_15_2.getStatusSoap()
            .enbIpThroughputStatusGet(nodeName, credentialsStatus);
        List<LteIpThroughputQciWs> a = result.getCell().get(0).getQciData();

        for (LteIpThroughputQciWs qciData : a) {
            if (qciData.getQci().getValue() == qci) {
                return qciData.getMacTrafficKbpsUl().getValue();
            }
        }
        return GeneralUtils.ERROR_VALUE;
    }

    @Override
    public boolean getDicicUnmanagedInterferenceStatus(String nodeName, int nghPci) {
        report.report("getDicicUnmanagedInterferenceStatus via NBI_15_2 Failed : Try to use correct NBI version",
            Reporter.WARNING);
        return false;
    }

    @Override
    public boolean updateNetworkProfile(EnodeB node, String cloneFromName, NetworkParameters networkParams) {
        // TODO Auto-generated method stub
        return false;
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
    public boolean getManagementProfile(EnodeB node, String cloneFromName) {
        List<String> names = new ArrayList<>();
        names.add(cloneFromName);
        try {
            soapHelper_15_2.getLteSoap().managementProfileGet(names, credentialsLte);
        } catch (Exception e) {
            soapHelper_15_2.endLteSoap();
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
            soapHelper_15_2.getLteSoap().securityProfileGet(names, credentialsLte);
        } catch (Exception e) {
            soapHelper_15_2.endLteSoap();
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
            soapHelper_15_2.getLteSoap().mobilityProfileGet(names, credentialsLte);
        } catch (Exception e) {
            soapHelper_15_2.endLteSoap();
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
            soapHelper_15_2.getLteSoap().networkProfileGet(names, credentialsLte);
        } catch (Exception e) {
            soapHelper_15_2.endLteSoap();
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
            soapHelper_15_2.getLteSoap().sonProfileGet(names, credentialsLte);
        } catch (Exception e) {
            soapHelper_15_2.endLteSoap();
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
            soapHelper_15_2.getLteSoap().radioProfileGet(names, credentialsLte);
        } catch (Exception e) {
            soapHelper_15_2.endLteSoap();
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
            soapHelper_15_2.getLteSoap().syncProfileGet(names, credentialsLte);
        } catch (Exception e) {
            soapHelper_15_2.endLteSoap();
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
            soapHelper_15_2.getLteSoap().cellAdvancedConfigProfileGet(names, credentialsLte);
        } catch (Exception e) {
            soapHelper_15_2.endLteSoap();
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean getRawNetspanGetProfileResponse(EnodeB node, String cloneFromName, INetspanProfile profileParams) {
        List<String> name = new ArrayList<>();
        name.add(cloneFromName);
        soapHelper_15_2.clearRawData();
        switch (profileParams.getType()) {
            case System_Default_Profile:
                soapHelper_15_2.getLteSoapRaw().systemDefaultProfileGet(name, credentialsLte);
                break;
            case Management_Profile:
                soapHelper_15_2.getLteSoapRaw().managementProfileGet(name, credentialsLte);
                break;
            case Mobility_Profile:
                soapHelper_15_2.getLteSoapRaw().mobilityProfileGet(name, credentialsLte);
                break;
            case Network_Profile:
                soapHelper_15_2.getLteSoapRaw().networkProfileGet(name, credentialsLte);
                break;
            case Radio_Profile:
                soapHelper_15_2.getLteSoapRaw().radioProfileGet(name, credentialsLte);
                break;
            case Security_Profile:
                soapHelper_15_2.getLteSoapRaw().securityProfileGet(name, credentialsLte);
                break;
            case Son_Profile:
                soapHelper_15_2.getLteSoapRaw().sonProfileGet(name, credentialsLte);
                break;
            case Sync_Profile:
                soapHelper_15_2.getLteSoapRaw().syncProfileGet(name, credentialsLte);
                break;
            case EnodeB_Advanced_Profile:
                soapHelper_15_2.getLteSoapRaw().enbAdvancedProfileConfigGet(name, credentialsLte);
                break;
            case Cell_Advanced_Profile:
                soapHelper_15_2.getLteSoap().cellAdvancedProfileConfigGet(name, credentialsLte);
                break;
            case Neighbour_Management_Profile:
                report.report("Neighbour management profile does not exist in netspan 15_2");
                break;
            case MultiCell_Profile:
                soapHelper_15_2.getLteSoapRaw().multiCellProfileGet(name, credentialsLte);
                break;
            default:
                return false;
        }

        try {
            String rawData = soapHelper_15_2.getSOAPRawData();
            printHTMLRawData(rawData);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            soapHelper_15_2.endLteSoap();
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
        report.report("cloneCellBAdvancedProfile via NBI_15_2 Failed : Try to use correct NBI version",
            Reporter.WARNING);
        return false;
    }

    @Override
    public boolean updateCellAdvancedProfile(EnodeB node, CellAdvancedParameters advancedParmas) {
        report.report("updateCellBAdvancedProfile via NBI_15_2 Failed : Try to use correct NBI version",
            Reporter.WARNING);
        return false;
    }

    @Override
    public boolean setLatitudeAndLongitude(EnodeB node, BigDecimal Latitude, BigDecimal Longitude) {
        report.report("setLatitudeAndLongitude method is not implemented for this netspan(15_2)!");
        return false;
    }

    @Override
    public String getSyncState(EnodeB eNodeB) {
        report.report("getSyncState method is not implemented for this netspan(15_2)!");
        return null;
    }

    @Override
    public String getGPSStatus(EnodeB eNodeB) {
        report.report("getGPSStatus method is not implemented for this netspan(15_2)!");
        return null;
    }

    @Override
    public List<LteBackhaul> getBackhaulInterfaceStatus(EnodeB node) {
        report.report("getBackhaulInterfaceStatus method is not implemented for this netspan(15_2)!");
        return null;
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
        report.report("resetNode method is not implemented for this netspan(15_2)!", Reporter.WARNING);
        return false;
    }


    @Override
    public boolean setEnbAccessClassBarring(EnodeB dut, CellBarringPolicyParameters cellBarringParams) {
        report.report("setEnbAccessClassBarring method is not implemented for this netspan(15_2)!");
        return false;
    }

    /**
     * get Software Status for EnB - WS
     *
     * @param enodeB - enodeB
     */
    private SoftwareStatusGetWs getSoftwareStatusForEnb(EnodeB enodeB) {
        List<String> enbList = new ArrayList<>();
        enbList.add(enodeB.getNetspanName());
        return soapHelper_15_2.getSoftwareSoap().softwareStatusGet(enbList, credentialsSoftware);
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
        if (!Netspan.NBI_15_2.Software.ErrorCodes.OK.equals(softwareStatusGetWs.getErrorCode())) {
            return null;
        }
        NodeSoftwareStatusResult firstNodeSWStatusResult = softwareStatusGetWs.getNodeSoftwareStatus().get(0);
        NodeSoftwareStatus firstNodeSWStatus = firstNodeSWStatusResult.getSoftwareStatus().get(0);
        return firstNodeSWStatus;
    }


    @Override
    public String getImageType(String nodeName) {
        report.report("getImageType method is not implemented for this netspan(15_2)!");
        return null;
    }

    @Override
    public int getMaxUeSupported(EnodeB enb) {
        report.report("getMaxUeSupported method is not implemented for this netspan(15_2)!", Reporter.WARNING);
        return 0;
    }

    @Override
    public Pair<Integer, Integer> getUlDlTrafficValues(String nodeName) {
        int ul = 0;
        int dl = 0;
        LteIpThroughputGetResult result = soapHelper_15_2.getStatusSoap()
            .enbIpThroughputStatusGet(nodeName, credentialsStatus);

        if (result.getErrorCode() != Netspan.NBI_15_2.Status.ErrorCodes.OK) {
            soapHelper_15_2.endStatusSoap();
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
        soapHelper_15_2.endStatusSoap();
        return Pair.createPair(dl, ul);
    }

	@Override
	public boolean resetNodeRebootAction(String nodeName, RebootTypesNetspan rebootType) {
		NodeActionResult result = null;
		ArrayList<String> listEnb = new ArrayList<String>();
		listEnb.add(nodeName);
		if(rebootType == RebootTypesNetspan.Reset_Node){
			result = soapHelper_15_2.getInventorySoap().nodeReset(listEnb, null, credentialsInventory);
		}else if(rebootType == RebootTypesNetspan.Forced_Reset_Node){
			result = soapHelper_15_2.getInventorySoap().nodeResetForced(listEnb, null, credentialsInventory);
		}else{
			report.report("Reboot type is not available in version 15.2", Reporter.FAIL);
			soapHelper_15_2.endInventorySoap();
			return false;
		}
		boolean rebooted = result.getErrorCode() == ErrorCodes.OK;
		soapHelper_15_2.endInventorySoap();
		return rebooted;
	}

	@Override
	public boolean relayScan(EnodeB enodeB, RelayScanType scanType) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isRelayEnodeb(String nodeName) {
		// TODO Auto-generated method stub
		return false;
	}

    @Override
    public boolean changeCellToUse(EnodeB node, GeneralUtils.CellToUse cellToUse) {
        report.report("changeCellToUse method is not implemented for this netspan(15_2)!", Reporter.WARNING);
        return false;
    }
}
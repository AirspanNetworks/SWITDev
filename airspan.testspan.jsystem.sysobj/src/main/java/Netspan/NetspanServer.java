
package Netspan;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import EnodeB.EnodeB;
import EnodeB.EnodeBChannelBandwidth;
import EnodeB.EnodeBUpgradeImage;
import EnodeB.EnodeBUpgradeServer;
import Netspan.API.Lte.AlarmInfo;
import Netspan.API.Lte.EnbCellProperties;
import Netspan.API.Enums.CategoriesLte;
import Netspan.API.Enums.EnbStates;
import Netspan.API.Enums.EnbTypes;
import Netspan.API.Enums.HandoverType;
import Netspan.API.Enums.HardwareCategory;
import Netspan.API.Enums.HoControlStateTypes;
import Netspan.API.Enums.ImageType;
import Netspan.API.Enums.NodeManagementModeType;
import Netspan.API.Enums.PrimaryClockSourceEnum;
import Netspan.API.Enums.X2ControlStateTypes;
import Netspan.API.Lte.EventInfo;
import Netspan.API.Lte.LteBackhaul;
import Netspan.API.Lte.NodeInfo;
import Netspan.API.Lte.PTPStatus;
import Netspan.API.Lte.RFStatus;
import Netspan.API.Lte.SONStatus;
import Netspan.API.Lte.IRetunTypes.ILteEnbDetailsSet;
import Netspan.API.Software.SoftwareStatus;
import Netspan.DataObjects.NeighborData;
import Netspan.Profiles.CellAdvancedParameters;
import Netspan.Profiles.CellBarringPolicyParameters;
import Netspan.Profiles.EnodeBAdvancedParameters;
import Netspan.Profiles.INetspanProfile;
import Netspan.Profiles.ManagementParameters;
import Netspan.Profiles.MobilityParameters;
import Netspan.Profiles.MultiCellParameters;
import Netspan.Profiles.NeighbourManagementParameters;
import Netspan.Profiles.NetworkParameters;
import Netspan.Profiles.RadioParameters;
import Netspan.Profiles.SecurityParameters;
import Netspan.Profiles.SonParameters;
import Netspan.Profiles.SyncParameters;
import Netspan.Profiles.SystemDefaultParameters;
import Utils.GeneralUtils.RebootType;
import Utils.Pair;
import jsystem.framework.system.SystemManagerImpl;
import jsystem.framework.system.SystemObjectImpl;


/**
 * The Class NetspanServer is an adapter to NBI web service. -all NBI method
 * will be invoked via EnodeB
 */

/**
 * @author mgoldenberg
 */
public abstract class NetspanServer extends SystemObjectImpl {

    /**
     * The Constant NBI_DEFAULT_USERNAME.
     */
    private static final String NBI_DEFAULT_USERNAME = "wsadmin";

    /**
     * The Constant NBI_DEFAULT_PASSWORD.
     */
    private static final String NBI_DEFAULT_PASSWORD = "password";

    /**
     * The host name.
     */
    private String hostname;

    /**
     * The web services path.
     */
    private String webServicesPath;

    /**
     * The user name.
     */
    private String username;

    /**
     * The user name.
     */
    private String ipv6Addrss;

    /**
     * The password.
     */
    private String password;
    //
    /**
     * The node names key=nodeId , value=node name.
     */
    protected Hashtable<String, String> nodeNames;

    /**
     * The netspan version.
     */
    protected String netspanVersion = null;
    protected static final int FIRST_ELEMENT = 0;
    protected static String NBI_VERSION = null;

    /**
     * Gets the single instance of NetspanServer.
     *
     * @return single instance of NetspanServer
     * @throws Exception the exception
     */
    public static NetspanServer getInstance() throws Exception {
        return (NetspanServer) SystemManagerImpl.getInstance().getSystemObject("NMS");
    }

    @Override
    public void init() throws Exception {
        report.setContainerProperties(0, "Netspan_NBI", NBI_VERSION);
        super.init();
    }

    /**
     * Gets the host name.
     *
     * @return the host name
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * Sets the host name.
     *
     * @param hostname the new host name
     */
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    /**
     * Gets the web services path.
     *
     * @return the web services path
     */
    public String getWebServicesPath() {
        return webServicesPath;
    }

    /**
     * Gets the user name.
     *
     * @return the user name
     */
    public String getUsername() {
        if (username == null)
            username = NBI_DEFAULT_USERNAME;
        return username;
    }

    /**
     * Sets the user name.
     *
     * @param username the new user name
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the password.
     *
     * @return the password
     */
    public String getPassword() {
        if (password == null)
            password = NBI_DEFAULT_PASSWORD;
        return password;
    }

    /**
     * Sets the password.
     *
     * @param password the new password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Sets the Ipv6Addrss.
     *
     * @return the Ipv6Addrss
     */
    public String getIpv6Addrss() {
        return ipv6Addrss;
    }

    /**
     * Sets the Ipv6Addrss.
     *
     * @param ipv6Addrss the new Ipv6Addrss
     */
    public void setIpv6Addrss(String ipv6Addrss) {
        this.ipv6Addrss = ipv6Addrss;
    }

    /**
     * Gets the node names.
     *
     * @return the nodeNames
     */
    public Hashtable<String, String> getNodeNames() {
        return nodeNames;
    }

    /*
     * (non-Javadoc)
     *
     * @see jsystem.framework.system.SystemObjectImpl#close()
     */
    @Override
    public void close() {
        report.report("Total NBIF tryouts=" + NBIHelper.getTotalTryouts());
        super.close();
    }

    /**
     * Gets the netspan version.
     *
     * @return the netspan version
     * @throws Exception
     */
    public abstract String getNetspanVersion();

    /**
     * Sets the netspan version.
     *
     * @param netspanVersion the new netspan version
     */
    public void setNetspanVersion(String netspanVersion) {
        this.netspanVersion = netspanVersion;
    }

    /**
     * Adds the neighbor.
     *
     * @param enodeB           the EnodeB
     * @param neighbor     the neighbor name
     * @param hoControlStatus  the ho control status
     * @param x2ControlStatus  the x2 control status
     * @param handoverType     the handover type
     * @param isStaticNeighbor the is static neighbor
     * @return true, if successful
     */
    public abstract boolean addNeighbor(EnodeB enodeB, EnodeB neighbor, HoControlStateTypes hoControlStatus,
                                        X2ControlStateTypes x2ControlStatus, HandoverType handoverType, boolean isStaticNeighbor,
                                        String qOffsetRange);

    /**
     * Adds the neighbor Multi Cell.
     *
     * @param enodeB           the EnodeB
     * @param neighbor     the neighbor name
     * @param hoControlStatus  the ho control status
     * @param x2ControlStatus  the x2 control status
     * @param handoverType     the handover type
     * @param isStaticNeighbor the is static neighbor
     * @return true, if successful
     */
    public abstract boolean addNeighbourMultiCell(EnodeB enodeB, EnodeB neighbor, HoControlStateTypes hoControlStatus,
                                        X2ControlStateTypes x2ControlStatus, HandoverType handoverType, boolean isStaticNeighbor,
                                        String qOffsetRange);

    /**
     * getNumberOfNetspanCells.
     *
     * @param enb           the EnodeB
     * @return true, if successful
     */
    public abstract int getNumberOfNetspanCells(EnodeB enb);

    public abstract boolean checkCannotAddNeighbor(EnodeB enodeB, EnodeB neighbor, HoControlStateTypes hoControlStatus,
                                                   X2ControlStateTypes x2ControlStatus, HandoverType handoverType, boolean isStaticNeighbor,
                                                   String qOffsetRange);

    /**
     * verify that neighbor was added to the main EnodeB with all the parameters
     * that was inserted.
     *
     * @param enodeB
     * @param neighbor
     * @param hoControlStatus
     * @param x2ControlStatus
     * @param handoverType
     * @param isStaticNeighbor
     * @param qOffsetRange
     * @return true if all the parameters are equal to the neighbor that was
     * found.
     */
    public abstract boolean verifyNeighbor(EnodeB enodeB, EnodeB neighbor, HoControlStateTypes hoControlStatus,
                                           X2ControlStateTypes x2ControlStatus, HandoverType handoverType, boolean isStaticNeighbor,
                                           String qOffsetRange);

    /**
     * verify that the neighbor was added without parameters
     *
     * @param enodeB
     * @param neighbor
     * @return true if the neighbor was found in the neighbor list (without
     * checking the parameters)
     */
    public abstract boolean verifyNeighbor(EnodeB enodeB, EnodeB neighbor);

    /**
     * verify that there is no neighbor in the neighbor list
     *
     * @param enodeB
     * @return true if no neighbor was found in list
     */

    public abstract boolean verifyNoNeighbor(EnodeB enodeB, EnodeB neighbor);

    public abstract boolean verifyNoNeighbors(EnodeB enodeB);

    /**
     * deleting single neighbor
     *
     * @param enodeB
     * @param neighborName
     * @return
     */
    public abstract boolean deleteNeighbor(EnodeB enodeB, EnodeB neighborName);

    public abstract boolean deleteAllNeighbors(EnodeB enodeB);

    public abstract boolean deleteEnbProfile(String profileName, EnbProfiles profileType);

    public abstract boolean isProfileExists(String name, EnbProfiles profileType);

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

    //set any node profile
    public abstract boolean setProfile(EnodeB node, EnbProfiles profileType, String profileName);

    public abstract boolean setProfile(EnodeB node, int cellid, EnbProfiles profileType, String profileName);

    public abstract boolean cloneRadioProfile(EnodeB node, String cloneFromName, RadioParameters radioParmas);

    public abstract boolean updateRadioProfile(EnodeB node, RadioParameters radioParams);

    public abstract boolean cloneSonProfile(EnodeB node, String cloneFromName, SonParameters sonParmas);

    public abstract boolean cloneSyncProfile(EnodeB node, String cloneFromName, SyncParameters syncParams);

    public abstract PrimaryClockSourceEnum getPrimaryClockSource(EnodeB node);

    public abstract boolean cloneProfile(EnodeB node, String cloneFromName, INetspanProfile profileParams);

    public abstract boolean cloneMobilityProfile(EnodeB node, String cloneFromName, MobilityParameters mobilityParams);

    public abstract boolean cloneNetworkProfile(EnodeB node, String cloneFromName, NetworkParameters networkParams) throws Exception;

    public abstract boolean cloneSecurityProfile(EnodeB node, String cloneFromName, SecurityParameters securityParams) throws Exception;

    public abstract boolean cloneManagementProfile(EnodeB node, String cloneFromName, ManagementParameters managementParmas);

    public abstract boolean cloneEnodeBAdvancedProfile(EnodeB node, String cloneFromName, EnodeBAdvancedParameters advancedParmas);

    public abstract boolean cloneCellAdvancedProfile(EnodeB node, String cloneFromName, CellAdvancedParameters advancedParmas);

    public abstract boolean updateCellAdvancedProfile(EnodeB node, CellAdvancedParameters advancedParmas);

    public abstract boolean cloneSystemDefaultProfile(EnodeB node, String cloneFromName, SystemDefaultParameters systemDefaultParmas);

    public abstract boolean cloneMultiCellProfile(EnodeB node, String cloneFromName, MultiCellParameters multiCellParams);

    public abstract boolean setMultiCellState(EnodeB node, Boolean isEnabled);

    public abstract boolean convertToPnPConfig(EnodeB node);

    public abstract boolean movePnPConfigToNodeList(EnodeB node);

    public abstract boolean setPnPSwVersion(EnodeB enodeB, String swVersion);

    public abstract boolean setPnPRadioProfile(EnodeB enodeB, String radioProfileName);

    public abstract boolean isPnpNode(String nodeName);

    public abstract boolean isRebootRequestedMaintenanceWindowStatus(EnodeB node);

    /**
     * Sets the EnodeB configuration.
     *
     * @param enodeB        the enode b
     * @param netspanConfig the netspan config
     * @return true, if successful
     */
    public abstract boolean setNodeConfig(EnodeB enodeB, ILteEnbDetailsSet netspanConfig);

    /**
     * @param enodeB
     * @param state
     * @return
     */
    public abstract boolean changeNbrAutoX2ControlFlag(EnodeB enodeB, boolean state);

    public abstract boolean changeNbrX2ConfiguratioUpdateFlag(EnodeB enodeB, boolean state);

    public abstract boolean Create3rdParty(String name, String ipAddress, int physicalLayerCellGroup,
                                           int physicalLayerIdentity, int cellID, int enbId, Netspan.API.Enums.EnbTypes enbType, int cellId, int tac,
                                           int dlEarfcn, EnodeBChannelBandwidth bandwidth, String mcc, String mnc);

    public abstract boolean is3rdPartyExist(String partyName);

    public abstract String getRunningVer(EnodeB enb);

    public abstract String getStandbyVer(EnodeB enb);

    public abstract boolean delete3rdParty(ArrayList<String> names);

    public abstract List<String> getAll3rdParty();

    public abstract HashMap<Integer, Integer> getUeConnectedPerCategory(EnodeB enb);

    public abstract RadioParameters radioProfileGet(EnodeB enb) throws Exception;

    public abstract NodeManagementModeType getManagedMode(EnodeB enb) throws Exception;

    /**
     * verify that there are no ANR neighbors in the neighbor list on netspan
     *
     * @param enodeB
     * @return true if there are no neighbors in list
     */
    public abstract boolean verifyNoANRNeighbors(EnodeB enodeB);

    public abstract List<String> getNodeNeighborsName(EnodeB node);

    /**
     * @param enb
     * @return network profile Name as Writen in NBI
     * @author Shahaf Shuhamy
     */
    public abstract String getCurrentNetworkProfileName(EnodeB enb);

    public abstract boolean setNodeServiceState(EnodeB enb, EnbStates enbState);

    public abstract String getCurrentSyncProfileName(EnodeB enb);

    public abstract String getCurrentSecurityProfileName(EnodeB enb);

    public abstract String getCurrentManagmentProfileName(EnodeB enb);

    public abstract String getCurrentRadioProfileName(EnodeB enb);

    public abstract String getCurrentMobilityProfileName(EnodeB enb);

    public abstract String getCurrentEnbAdvancedConfigurationProfileName(EnodeB enb);

    public abstract String getCurrentCellAdvancedConfigurationProfileName(EnodeB enb);

    public abstract String getCurrentSystemDefaultProfileName(EnodeB enb);

    public abstract String getCurrentProfileName(EnodeB node, EnbProfiles profileType);

    public abstract String getCurrentSonProfileName(EnodeB enb);

    /**
     * @param dut
     * @param integrityLevel
     * @param nullIntegriLevel
     * @param aesIntegriLevel
     * @param showIntegriLevel
     * @param cipheringLevel
     * @param nullCipherLevel
     * @param aesCipherLevel
     * @param snowCipherLevel
     * @return new SecurityProfileName
     * @author Shahaf Shuhamy
     */
    public abstract boolean getNetworkProfileResult(String profileName, NetworkParameters np) throws Exception;

    public abstract NodeInfo getNodeDetails(EnodeB enodeB);

    public abstract List<RFStatus> getRFStatus(EnodeB enodeB);

    public abstract String getMMEIpAdress(EnodeB enb);

    public abstract String getPTPInterfaceIP(EnodeB enb);

    public abstract String getGMIP(EnodeB dut);

    public abstract PTPStatus getPTPStatus(EnodeB dut);

    public abstract boolean deleteAllAlarmsNode(EnodeB dut);

    public abstract List<AlarmInfo> getAllAlarmsNode(EnodeB dut);

    public abstract List<EventInfo> getAllEventsNode(EnodeB dut);

    public abstract List<EventInfo> getAllEventsNode(EnodeB dut, Date startTime);

    public abstract boolean deleteAlarmById(String alarmId);

    public abstract boolean changeCurrentRadioProfileWithoutClone(EnodeB dut, RadioParameters rp);

    public abstract List<EventInfo> getEventsNodeByDateRange(EnodeB dut, Date from, Date to);

    public abstract boolean checkIfEsonServerConfigured(EnodeB enb);

    public abstract boolean setEnbType(String nodeName, EnbTypes type);

    public abstract boolean setManagedMode(String nodeName, NodeManagementModeType managedMode);

    public abstract boolean performReProvision(String nodeName);

    public abstract boolean setProfile(EnodeB node, HashMap<INetspanProfile, Integer> profilesPerCell);

    public abstract boolean checkAndSetDefaultProfiles(EnodeB node, boolean setProfile);

    public abstract SONStatus getSONStatus(EnodeB dut);

    public abstract EnbCellProperties getEnbCellProperties(EnodeB enb);

    public abstract boolean setPci(EnodeB dut, int pci);

    public abstract boolean updateNeighborManagementProfile(EnodeB node, String cloneFromName, NeighbourManagementParameters neighbourManagementParams);

    public abstract boolean cloneNeighborManagementProfile(EnodeB node, String cloneFromName, NeighbourManagementParameters neighbourManagementParams);

    public abstract String getNeighborManagmentProfile(String nodeName);

    public abstract NeighborData getNeighborData(String nodeName, String nghName);

    public abstract boolean setEnbCellProperties(EnodeB dut, EnbCellProperties cellProperties);

    public abstract boolean setEnbAccessClassBarring(EnodeB dut, CellBarringPolicyParameters cellBarringParams);

    public abstract boolean updateSonProfile(EnodeB node, String cloneFromName, SonParameters sonParams);

    public abstract EnodeBUpgradeServer getSoftwareServer(String name);

    public abstract boolean setSoftwareServer(String softwareServername, EnodeBUpgradeServer upgradeServer);

    public abstract boolean deleteSoftwareServer(String softwareServername);

    public abstract boolean createSoftwareServer(EnodeBUpgradeServer upgradeServer);

    public abstract String getMultiCellProfileName(EnodeB enb);

    public abstract boolean createSoftwareImage(EnodeBUpgradeImage upgradeImage);

    public abstract EnodeBUpgradeImage getSoftwareImage(String upgradeImagename);

    public abstract HardwareCategory getHardwareCategory(EnodeB node);

    public abstract HardwareCategory LTECategoriesToHardwareCategory(CategoriesLte categories);

    public abstract boolean softwareConfigSet(String nodeName, Netspan.API.Software.RequestType requestType, String softwareImage);

    public abstract SoftwareStatus getSoftwareStatus(String nodeName, ImageType imageType);

    public abstract int getDLTPTPerQci(String nodeName, int qci);

    public abstract boolean getDicicUnmanagedInterferenceStatus(String nodeName, int nghPci);

    public abstract boolean updateSoftwareImage(EnodeBUpgradeImage upgradeImage);

    public abstract boolean deleteSoftwareImage(String upgradeImageName);

    public abstract boolean updateNetworkProfile(EnodeB node, String cloneFromName, NetworkParameters networkParams);

    /**
     * method does Get for current Netspan instance with the current Profile and attach file with the Soap to report.
     *
     * @param node
     * @param cloneFromName
     * @param profileParams
     * @return
     */
    public abstract boolean getRawNetspanGetProfileResponse(EnodeB node, String cloneFromName, INetspanProfile profileParams);

    public abstract boolean getNetspanProfile(EnodeB node, String cloneFromName, INetspanProfile profileParams);

    public abstract boolean setLatitudeAndLongitude(EnodeB node, BigDecimal Latitude, BigDecimal Longitude);

    public abstract String getSyncState(EnodeB eNodeB);

    public abstract boolean isNetspanReachable();

    public void printHTMLRawData(String rawData) {
        rawData = rawData.replaceAll(">", "&gt;");
        rawData = rawData.replaceAll("<", "&lt;");
        report.reportHtml("xml raw", rawData, true);
    }

    public abstract List<LteBackhaul> getBackhaulInterfaceStatus(EnodeB node);

    public abstract ArrayList<String> getMMEIpAdresses(EnodeB enb);

    public abstract String getGPSStatus(EnodeB eNodeB);

    public abstract String getMangementIp(EnodeB enb);

    public abstract int getNumberOfActiveCellsForNode(EnodeB node);

    public abstract boolean resetNode(String nodeName, RebootType rebootType);

    /**
     * get Running Version Of Enb
     *
     * @param enodeB - enodeB
     * @return - Running version
     */
    public abstract String getRunningVersionOfEnb(EnodeB enodeB);

    /**
     * get StandBy Version Of Enb
     *
     * @param enodeB - enodeB
     * @return - StandBy version
     */
    public abstract String getStandByVersionOfEnb(EnodeB enodeB);

	public abstract String getImageType(String nodeName);

	public abstract int getMaxUeSupported(EnodeB enb);

	public abstract Pair<Integer,Integer> getUlDlTrafficValues(String nodeName);
}
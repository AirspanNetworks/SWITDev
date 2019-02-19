package EnodeB;

import java.io.IOException;
import java.net.InetAddress;
import java.util.*;

import org.snmp4j.smi.Variable;

import EnodeB.Components.UEDist;
import EnodeB.Components.Log.LogListener;
import EnodeB.Components.Log.Logger;
import EnodeB.Components.Session.Session;
import EnodeB.Components.XLP.XLP;
import EnodeB.Components.XLP.XLP.SwStatus;
import EnodeB.Components.XLP.XLP_14_0;
import EnodeB.Components.XLP.XLP_14_5;
import EnodeB.Components.XLP.XLP_15_2;
import EnodeB.Components.XLP.XLP_16_0;
import EnodeB.ProtoBuf.PbLteStatusOuterClass.PbLteAnrStatus;
import EnodeB.ProtoBuf.PbLteStatusOuterClass.PbLteCellStatus;
import EnodeB.ProtoBuf.PbLteStatusOuterClass.PbLteMmeStatus;
import EnodeB.ProtoBuf.PbLteStatusOuterClass.PbLteNwElementStatus;
import EnodeB.ProtoBuf.PbLteStatusOuterClass.PbLteRfStatus;
import Entities.ITrafficGenerator.TransmitDirection;
import Netspan.NetspanServer;
import Netspan.API.Enums.EnabledStates;
import Netspan.API.Enums.EnbStates;
import Netspan.API.Enums.HandoverTypes;
import Netspan.API.Enums.HoControlStateTypes;
import Netspan.API.Enums.ImageType;
import Netspan.API.Enums.ServerProtocolType;
import Netspan.API.Enums.SonAnrStates;
import Netspan.API.Enums.X2ControlStateTypes;
import Netspan.API.Lte.PTPStatus;
import Netspan.API.Lte.RFStatus;
import Netspan.Profiles.NetworkParameters.Plmn;
import Utils.DefaultNetspanProfiles;
import Utils.GeneralUtils;
import Utils.GeneralUtils.CellIndex;
import Utils.GeneralUtils.RebootType;
import Utils.GeneralUtils.RebootTypesNetspan;
import Utils.InetAddressesHelper;
import Utils.Pair;
import Utils.TunnelManager;
import Utils.Snmp.MibReader;
import Utils.Snmp.SNMP;
import jsystem.framework.IgnoreMethod;
import jsystem.framework.report.Reporter;
import jsystem.framework.system.SystemObjectImpl;

/**
 * The Class EnodeB.
 */
public abstract class EnodeB extends SystemObjectImpl {
	public final long HALF_MIN = 30 * 1000;
	public final long TWO_MIN = 2 * 60 * 1000;
	public final long THREE_MIN = 3 * 60 * 1000;
	public final long FOUR_MIN = 4 * 60 * 1000;
	public final long FIVE_MIN = 5 * 60 * 1000;

	protected final Pair[] expectedDurationsAndStageNamesOrderedForWarmReboot = {
			new Pair<Long, String>((long)0, "Warm Reboot."),
			new Pair<Long, String>((long)THREE_MIN, "SNMP Availability / IPSec Bring Up."),
			new Pair<Long, String>((long)FOUR_MIN, "All Running.")
	};
	
	protected final Pair[] expectedDurationsAndStageNamesOrderedForColdReboot = {
			new Pair<Long, String>((long)0, "Cold Reboot."),
			new Pair<Long, String>((long)THREE_MIN, "SNMP Availability / IPSec Bring Up."),
			new Pair<Long, String>((long)FIVE_MIN, "Cold eNodeB PnP."),
			new Pair<Long, String>((long)TWO_MIN, "All Running.")
	};
	
	protected final Pair[] expectedDurationsAndStageNamesOrderedWithSoftwareDownloadForColdReboot = {
			new Pair<Long, String>((long)0, "Cold Reboot."),
			new Pair<Long, String>((long)THREE_MIN, "SNMP Availability / IPSec Bring Up."),
			new Pair<Long, String>((long)HALF_MIN, "Cold eNodeB PnP & Software Download."),
			new Pair<Long, String>((long)1, "Reboot After Software Download."),
			new Pair<Long, String>((long)THREE_MIN, "SNMP Availability / IPSec Bring Up."),
			new Pair<Long, String>((long)FIVE_MIN, "Cold eNodeB PnP."),
			new Pair<Long, String>((long)HALF_MIN, "eNodeb Software Activate Completed"),
			new Pair<Long, String>((long)TWO_MIN, "All Running.")
	};

	public boolean isNetspanProfilesVerified() {
		return isNetspanProfilesVerified;
	}

	public void setNetspanProfilesVerified(boolean netspanProfilesVerified) {
		isNetspanProfilesVerified = netspanProfilesVerified;
	}

	public boolean isManagedByNetspan() {
		return isManagedByNetspan;
	}

	public void setManagedByNetspan(boolean managedByNetspan) {
		isManagedByNetspan = managedByNetspan;
	}

	public enum Architecture {
		XLP, FSM, FSMv4
	}

	public Architecture architecture;
	private static final String CONTROL_COMPONENT_HW_NAME = "XLP";
	public static final int BOOTING_TIMOUT = 2 * 60 * 1000;// 120 sec
	public static final int MAX_NBI_TOTAL_TRYOUTS = 8;
	public static final long DOWNLOAD_TIMEOUT = 30 * 1000 * 60;
	public static final long ACTIVATE_TIMEOUT = 20 * 1000 * 60;
	public static long WAIT_FOR_ALL_RUNNING_TIME = 30 * 1000 * 60;
	public static long SHORT_WAIT_FOR_ALL_RUNNING_TIME = 2 * 1000 * 60;
	/** The netspan name. */
	private String netspanName;
	/** The S1 ip address. */
	private String s1IpAddress;
	/** The enodeB version */
	private String enodeBversion;
	private int interHandoverEarfcn;
	/** The xlp. */
	protected XLP XLP;
	/** The phy version. */
	private String phyVersion;
	/** the UEs distribution in the cell */
	public UEDist ueDist;
	/** The operation mode. */
	private EnodeBOperationMode operationMode;
	/** The bandwidth. */
	protected EnodeBChannelBandwidth bandwidth;
	/** The node id. */
	protected String nodeId;
	/** The txpower. */
	protected int txPower;
	/** The band. */
	protected int band;
	/** The cellID. */
	protected int cellId;
	/** is managed by netspan flag. */
	private boolean isManagedByNetspan;
	/** is netspan profiles verified flag. */
	private boolean isNetspanProfilesVerified;
	public DefaultNetspanProfiles defaultNetspanProfiles;
	private String[] staticUEs;
	private NodeWebAccess webAccess;
	public Object inServiceStateLock = new Object();

	/**
	 * The description of the eNodeB (example: AirSynergy 2000, Band 38/41M,
	 * 2.5GHz TDD, Connectorized) or product code
	 */
	private String productDescription;
	private String cellIdentity;
	private String mcc;
	private String mnc;
	private int cellContextID = 1;

	String snifferMAC = null;
	final String captureFilesPath = "\\\\asil-swit-pcaps-1\\pcaps\\";
	boolean isCaptureHasBeenMade = false;
	private boolean enableMACtoPHY;
	private boolean ipsecTunnelEnabled;
	protected boolean swUpgradeDuringPnP;
	private String ipsecCertificateMacAddress;
	int SWTypeInstance;
	private ArrayList<String> debugFlags = new ArrayList<>();
	protected boolean hasDonor = false;
	protected boolean hasDan;
	public ConnectionInfo connectInfo;
	public boolean blackListed = false;
	public boolean expecteInServiceState = true;
	private String SkipCMP = "false";

	public EnodeB() {
		super();
		architecture = Architecture.XLP;
		setSWTypeInstance(18);
		webAccess = new NodeWebAccess();
	}

	/**
	 * Inits the.
	 *
	 * @throws Exception
	 *             the exception
	 */
	public void init() throws Exception {
		super.init();

		switch (enodeBversion) {
		case "14_5": 
		case "15_1":
			XLP = new XLP_14_5();	
			break;
		case "15_2": 
		case "15_5": 
			XLP = new XLP_15_2(); 
			break;
		case "16_0": 
			XLP = new XLP_16_0(); 
			break;	
		default:
			XLP = new XLP_15_2();
			report.report("no enodeB version was found- setting to XLP_15.2 as default");
			break;
		}
		if (connectInfo.serialInfo != null) {
			XLP.createSerialCom(connectInfo.serialInfo.getSerialIP(), Integer.parseInt(connectInfo.serialInfo.getSerialPort()) );
			XLP.setSerialUsername(connectInfo.serialInfo.getUserName());	
		}
		XLP.setParent(this);
		XLP.setIpAddress(connectInfo.getIpAddress());
		XLP.setSkipCMP(getSkipCMP());
		XLP.setUsername(connectInfo.getUserName());
		if(connectInfo.getReadCommunity()!=null)
			XLP.setReadCommunity(connectInfo.getReadCommunity());
		if(connectInfo.getWriteCommunity()!=null)
			XLP.setWriteCommunity(connectInfo.getWriteCommunity());		
		XLP.debugFlags = getDebugFlags();
		XLP.hardwareName = getControlComponenetHwName();
		XLP.setName(getName());
		int swType;
		try {
			swType = getSWTypeInstance();
		} catch (Exception e1) {
			swType = 18;
		}
		XLP.setSWTypeInstnace(swType);
		XLP.init();
		if(ipsecTunnelEnabled){
			ArrayList<EnodeB> eNodeBs = new ArrayList<EnodeB>();
			eNodeBs.add(this);
			TunnelManager tunnelManager = TunnelManager.getInstance(eNodeBs, report);
			tunnelManager.waitForIPSecTunnelToOpen(10000, this);
			tunnelManager.safeStart();
		}
		XLP.printVersion();
	}

	/**
	 * Lte cli command
	 *
	 * @param command
	 *            the commands
	 * @return the string
	 */
	public String lteCli(String command) {
		return this.XLP.lteCli(command);
	}

	/**
	 * Lte cli commands.. separated by ";"
	 *
	 * @param command
	 *            the commands
	 * @return the string
	 */
	public String lteCli(String command, String response) {
		return this.XLP.lteCliWithResponse(command, response);
	}

	/**
	 * Wait for running.
	 *
	 * @param timeout the timeout (milliSec)
	 * @return true, if successful
	 */
	public boolean waitForAllRunningAndInService(long timeout) {
		return XLP.waitForAllRunningAndInService(timeout);
	}

	public boolean waitForAllRunning(double timeout) {
		report.report("Wait for " + getName() + " to reach all running state.");
		if(XLP.waitForAllRunning(timeout))
		{
			report.report(getName() + " reached all running state.");
			expecteInServiceState = true;

			return true;
		}
		else
		{
			report.report(getName() + " failed to reach all running state.", Reporter.WARNING);
			return false;
		}	
	}
	/**
	 * Check running state until timeout reached
	 *	
	 * @param timeout
	 *            the timeout
	 * @return true, if timeout is reached
	 */
	public boolean verifyNotReachAllRunningState(long timeout) {
		report.report("Wait to see if " + getName() + " reaches all running state.");
		if(XLP.waitForAllRunningAndInService(timeout))
		{
			report.report(getName() + " reached all running state.", Reporter.WARNING);
			return false;
		}
		else
		{
			report.report(getName() + " did not to reach all running state.");
			return true;
		}	
	}
	
	public boolean waitForReboot(long timeout) {
		return XLP.waitForReboot(timeout);
	}

	public boolean rebootViaNetspan(RebootTypesNetspan RTNetspan){
		boolean action = false;
		expecteInServiceState = false;
		XLP.setExpectBooting(true);

		try {
			action = NetspanServer.getInstance().resetNodeRebootAction(getNetspanName(), RTNetspan);
		} catch (Exception e) {
			e.printStackTrace();
			//report.report("Failed to reboot e",Reporter.WARNING);
		}
		if(action){
			GeneralUtils.unSafeSleep(60000);
		}else{
			expecteInServiceState = true;
			XLP.setExpectBooting(false);
		}
		return action;
	}
	
	/*
	 * the method reboot the EnodeB. its changes the state of xlp and dan to
	 * booting, disconnecting UES & reboot
	 */
	/**
	 * Reboot.
	 */
	public boolean reboot() {
		return reboot(false, RebootType.WARM_REBOOT);
	}
	
	public boolean reboot(RebootType rebootType) {
		return reboot(false, rebootType);
	}
	
	public boolean reboot(boolean swapAndReboot) {
		return reboot(swapAndReboot, RebootType.WARM_REBOOT);
	}
	public boolean reboot(boolean swapAndReboot, RebootType rebootType) {
		boolean rebootStatus = false;

		boolean bankActionPassed;
		if (swapAndReboot)
			bankActionPassed = XLP.swapBank();
		else
			bankActionPassed = XLP.preserveBank();
		if (!bankActionPassed){
			report.report("Failed To Swap/Preserve Bank - Skipping Reboot.", Reporter.WARNING); 
			return false;
		}

		GeneralUtils.unSafeSleep(1000);
		expecteInServiceState = false;
		XLP.setExpectBooting(true);
		report.report("Rebooting " + getNetspanName() + " via Netspan");
		rebootStatus = resetNodeViaNetspan(rebootType);
		if(!rebootStatus){
			report.report("Rebooting " + getNetspanName() + " via SNMP");
			rebootStatus = rebootExecutionViaSnmp(rebootType);			
		}
		// wait 1 min to avoid fake allrunning in ipsec setups.
		GeneralUtils.unSafeSleep(90000);
		return rebootStatus;
	}

	private boolean resetNodeViaNetspan(RebootType rebootType) {
		try {
			return NetspanServer.getInstance().resetNode(getNetspanName(),rebootType);
		} catch (Exception e) {
			e.printStackTrace();
			report.report("Failed to reboot via netspan",Reporter.WARNING);
		}
		return false;
	}

	protected boolean rebootExecutionViaSnmp(RebootType rebootType){
		return XLP.reboot(rebootType);
	}
	
	public boolean downloadSWSnmp(String fileToInstall, ServerProtocolType downloadType, String user,
			String password) {
		return XLP.downloadSWSnmp(fileToInstall, downloadType, user, password);
	}
	
	public boolean downloadSWCli(String fileToInstall, ServerProtocolType downloadType, String username, String password) {
		return XLP.downloadSWCli(fileToInstall, downloadType, username, password);
	}

	public boolean getDownloadStates() {
		return XLP.downloadStats();
	}

	public String getSecondaryVersion() {
		for (int i = 0; i < 3; i++) {
			try {
				return XLP.getSecondaryVersion();
			} catch (IOException e) {
			}
		}
		return null;
	}

	public String getPrimaryVersion() {
		for (int i = 0; i < 3; i++) {
			try {
				return XLP.getPrimaryVersion();
			} catch (IOException e) {
			}
		}
		return null;
	}

	public boolean swapBanksAndReboot() {
		return reboot(true);
	}

	/**
	 * Gets the product description.
	 *
	 * @return the product description
	 */

	public String getProductDescription() {
		return productDescription;
	}

	/**
	 * Get the component type by enum
	 */
	public EnodeB.Architecture getArchitecture() {
		return architecture;
	}

	/**
	 * Gets the phy version.
	 *
	 * @return the phy version
	 */
	@IgnoreMethod
	public String getPhyVersion() {
		return phyVersion;
	}

	/**
	 * Sets the phy version.
	 *
	 * @param phyVersion
	 *            the new phy version
	 */

	public void setPhyVersion(String phyVersion) {
		if (this.phyVersion == null && phyVersion != null)
			report.report(String.format("PHY version detected: %X", phyVersion), Reporter.PASS);

		this.phyVersion = phyVersion;
	}

	/**
	 * Gets the bandwidth.
	 *
	 * @return the bandwidth
	 */

	public EnodeBChannelBandwidth getBandwidth() {
		String oid = "" + MibReader.getInstance().resolveByName("asLteStkCellCfgChannelBandwidth");
		String band = "";
		try {
			band = this.XLP.snmp.get(oid);
			bandwidth = EnodeBChannelBandwidth.getByDbIndx(Integer.parseInt(band));
		} catch (Exception e1) {
			e1.printStackTrace();
			report.report("Couldn't get the band width (couldn't get asLteStkCellCfgChannelBandwidth MIB).",
					Reporter.WARNING);
			return null;
		}
		return bandwidth;
	}

	/**
	 * Sets the bandwidth.
	 *
	 * @param bandwidth
	 *            the new bandwidth
	 */

	public void setBandwidth(EnodeBChannelBandwidth bandwidth) {
		String oid = "" + MibReader.getInstance().resolveByName("asLteStkCellCfgChannelBandwidth");

		try {
			this.XLP.snmp.snmpSet(oid, bandwidth.getDbIndx());
			this.bandwidth = bandwidth;
		} catch (Exception e) {
			report.report("Couldn't change bandwidth (db didn't update).", Reporter.WARNING);
			GeneralUtils.printToConsole("Couldn't change bandwidth (db didn't update).");
		}
	}

	/**
	 * Gets the txpower.
	 * 
	 * @return the txpower
	 */

	public int getTxPower() {
		return this.XLP.getTxPower();
	}

	/**
	 * Gets the PCI.
	 * 
	 * @return the PCI
	 */
	@IgnoreMethod
	public int getPci() {
		return XLP.getPci();
	}

	/**
	 * Gets the band.
	 * 
	 * @return the band
	 */
	@IgnoreMethod
	public int getBand() {
		return XLP.getBand();
	}

	/**
	 * Gets the mac address.
	 * 
	 * @return the mac address
	 * @throws IOException
	 */

	public String getMacAddress() throws IOException {
		String oid = MibReader.getInstance().resolveByName("asMaxCmInventoryHardwareMacAddress");
		return XLP.snmp.get(oid);
	}

	/**
	 * Gets the cellId.
	 * 
	 * @return the cellId
	 */

	public int getCellId() {
		String id = "0";
		String oid = MibReader.getInstance().resolveByName("asLteStkCellCfgCellId");
		try {
			id = XLP.snmp.get(oid);
			cellId = Integer.parseInt(id);
		} catch (Exception e) {
			e.printStackTrace();
			report.report("Couldn't get the cell id", Reporter.WARNING);
		}
		try {
			return Integer.parseInt(id);
		} catch (Exception e) {
			e.printStackTrace();
			report.report("Couldn't Convert " + id + " to Integer", Reporter.WARNING);
			return 0;
		}
	}

	/**
	 * Gets the operation mode.
	 *
	 * @return the operation mode
	 */

	public EnodeBOperationMode getOperationMode() {
		if (operationMode == null) {
			String opMode = "";
			String oid = MibReader.getInstance().resolveByName("asLteStkCellCfgDuplexMode");
			try {
				opMode = XLP.snmp.get(oid);
				operationMode = EnodeBOperationMode.getByDbIndx(Integer.parseInt(opMode));
			} catch (Exception e) {
				e.printStackTrace();
				report.report("Couldn't get the duplexMode", Reporter.WARNING);
				GeneralUtils.printToConsole("Couldn't get the duplexMode.");
			}
		}
		return operationMode;
	}

	/**
	 * Gets the uplink freqency.
	 *
	 * @return the uplink freqency
	 */

	public int getUplinkFreqency() {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellCfgUplinkFreq");
		String freq = "";
		try {
			freq = this.XLP.snmp.get(oid);
		} catch (Exception e1) {
			e1.printStackTrace();
			report.report("Couldn't get the uplink frequency (couldn't get asLteStkCellCfgUplinkFreq MIB).",
					Reporter.WARNING);
			return 0;
		}
		try {
			return Integer.parseInt(freq);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			report.report("Couldn't Convert " + freq + " to Integer", Reporter.WARNING);
			return 0;
		}
	}

	/**
	 * Sets the uplink frequency.
	 *
	 * @param uplink
	 *            the new uplink frequency
	 */

	public void setUplinkFrequency(int uplink) {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellCfgUplinkFreq");
		try {
			this.XLP.snmp.snmpSet(oid, uplink);
		} catch (Exception e1) {
			e1.printStackTrace();
			report.report("Couldn't set the uplink frequency", Reporter.WARNING);
			GeneralUtils.printToConsole("Couldn't set the uplink frequency");
		}
	}

	/**
	 * Gets the downlink freqency.
	 *
	 * @return the downlink freqency
	 */

	public int getDownlinkFreqency() {
		return XLP.getDownlinkFrequency();
	}

	/**
	 * Sets the downlink frequency.
	 *
	 * @param downlink
	 *            the new downlink frequency
	 */

	public void setDownlinkFrequency(int downlink) {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellCfgDownlinkFreq");
		try {
			this.XLP.snmp.snmpSet(oid, downlink);
		} catch (Exception e1) {
			e1.printStackTrace();
			report.report("Couldn't set the uplink frequency", Reporter.WARNING);
			GeneralUtils.printToConsole("Couldn't set the uplink frequency");
		}
	}

	/**
	 * Gets all the neighbors ip address configured in the eNodeB.
	 *
	 * @return the ip address of all the neighbors.
	 */

	public String[] getNeighbors() {
		Hashtable<String, String[]> nghCellCfg = XLP.dbGet("nghCellCfg");
		if (nghCellCfg != null)
			return nghCellCfg.get("enbIpAddr.address");
		else {
			GeneralUtils.printToConsole("Couldn't get table \"nghCellCfg\"");
			return null;
		}
	}

	/**
	 * Gets the loggers.
	 *
	 * @return the loggers
	 */

	public Logger[] getLoggers() {
		return new Logger[] { XLP.getLogger() };
	}

	/**
	 * Db get.
	 *
	 * @param tableName
	 *            the table name
	 * @return the hashtable
	 */
	public Hashtable<String, String[]> dbGet(String tableName) {
		Hashtable<String, String[]> table = XLP.dbGet(tableName);
		if (table == null) {
			GeneralUtils.printToConsole("Couldn't get table \"%s\". Trying again");
			table = XLP.dbGet(tableName);
		}

		return table;
	}

	/**
	 * Gets the netspan name.
	 *
	 * @return the netspan name
	 */
	public String getNetspanName() {
		return this.netspanName;

	}

	/**
	 * Gets the control componenet hw name.
	 *
	 * @return the control componenet hw name
	 */
	public String getControlComponenetHwName() {
		return CONTROL_COMPONENT_HW_NAME;
	}

	/**
	 * Sets the netspan name.
	 *
	 * @param netspanName
	 *            the netspanName to set
	 */
	public void setNetspanName(String netspanName) {
		this.netspanName = netspanName;
	}

	/**
	 * Gets the S1 ip address.
	 *
	 * @return the s1IpAddress name
	 */
	public String getS1IpAddress() {
		return this.s1IpAddress;
	}

	/**
	 * Sets the S1 ip address.
	 *
	 * @param s1IpAddress
	 *            the s1IpAddress to set
	 */
	public void setS1IpAddress(String s1IpAddress) {
		this.s1IpAddress = s1IpAddress;
	}

	/**
	 * Get active alarms from current ENB.
	 *
	 * @return EnodeBAlarmResult - list of current alarms with timestamp.
	 * @throws Exception
	 * @throws NullPointerException
	 * @throws IllegalArgumentException
	 */
	/*
	 * public EnodeBAlarmResult getActiveAlarms() throws Exception{ return
	 * FaultManagement.getCurrentAlarmsFromEnodeB(this); }
	 */
	public String getEnodeBversion() {
		return enodeBversion;
	}

	public void setEnodeBversion(String enodeBversion) {
		this.enodeBversion = enodeBversion;
	}
	
	public void setProductDescription(String hardwareDescription) {
		if (this.productDescription == null || !this.productDescription.equals(hardwareDescription)) {
			this.productDescription = hardwareDescription;
		}
	}

	/**
	 * Sets the product description to the eNodeB product code. <br/>
	 * The eNodeB product code is taken from the Inventory table.
	 */
	protected void updateProductCode() {
		Hashtable<String, String[]> inventoryTable = XLP.dbGet("Inventory");
		if (inventoryTable == null) {
			GeneralUtils.printToConsole("Couldn't parse the \"Inventory\" db table.");
			return;
		}
		String[] inventoryIndexes = inventoryTable.get("index");

		// Find the row which has the eNodeB product code.
		for (int row = 0; row < inventoryIndexes.length; row++) {
			String index = inventoryIndexes[row];

			// the relevant product code is always at index number 4.
			if (index.equals("4")) {
				String productCode = inventoryTable.get("productCode")[row];
				setProductDescription(productCode);
				return;
			}
		}
	}

	/**
	 * @return the interHandoverEarfcn
	 */
	public int getInterHandoverEarfcn() {
		return interHandoverEarfcn;
	}

	/**
	 * @param interHandoverEarfcn
	 *            the interHandoverEarfcn to set
	 */
	public void setInterHandoverEarfcn(int interHandoverEarfcn) {
		this.interHandoverEarfcn = interHandoverEarfcn;
	}

	public DefaultNetspanProfiles getDefaultNetspanProfiles() {
		if (this.defaultNetspanProfiles != null)
			return this.defaultNetspanProfiles;
		return null;
	}

	// public void setDefaultNetspanProfiles(DefaultNetspanProfiles
	// defaultNetspanProfiles) {
	// this.defaultNetspanProfiles = defaultNetspanProfiles;
	// }

	/**
	 * @return the cellIdentity
	 * @throws IOException
	 */
	public String getCellIdentity() {
		if (cellIdentity == null || cellIdentity.isEmpty())
			cellIdentity = XLP.getCellIdentity();

		return cellIdentity;
	}

	/**
	 * @param cellIdentity
	 *            the cellIdentity to set
	 */
	public void setCellIdentity(String cellIdentity) {
		this.cellIdentity = cellIdentity;
	}

	/**
	 * @return the mnoBroadcastPlmn
	 * @throws IOException
	 */
	public String getMcc()  {
		if (mcc == null || mcc.isEmpty())
			mcc = XLP.getMcc();

		return mcc;
	}

	/**
	 * @param mcc
	 *            the mnoBroadcastPlmn to set
	 */
	public void setMcc(String mcc) {
		this.mcc = mcc;
	}

	/**
	 * @return the mnc
	 * @throws IOException
	 */
	public String getMnc() {
		if (mnc == null || mnc.isEmpty())
			mnc = XLP.getMnc();

		return mnc;
	}

	/**
	 * @param mnc
	 *            the mnc to set
	 */
	public void setMnc(String mnc) {
		this.mnc = mnc;
	}

	public String calculateMnoBroadcastPlmn() {

		String returnValue;
		String mcc = "";
		String mnc = "";

		while (mcc == "" || mnc == "") {
			mcc = getMcc();
			mnc = getMnc();
		}

		char[] mccArr = mcc.toCharArray();
		char mcc0 = mccArr[0];
		char mcc1 = mccArr[1];
		char mcc2 = mccArr[2];

		char mnc0;
		char mnc1;
		char mnc2;

		char[] mncArr = mnc.toCharArray();
		if (mnc.length() == 2) {
			mnc0 = 'F';
			mnc1 = mncArr[0];
			mnc2 = mncArr[1];
		} else {
			mnc0 = mncArr[0];
			mnc1 = mncArr[1];
			mnc2 = mncArr[2];
		}

		returnValue = "" + mcc1 + mcc0 + mnc0 + mcc2 + mnc2 + mnc1 + "00";
		String decimal = ((Integer) GeneralUtils.hex2decimal(returnValue)).toString();

		return decimal;
	}

	public String calculateEutranCellId() {

		String returnValue;
		String identity = "";
		while (identity == "") {
			identity = getCellIdentity();
		}
		identity = GeneralUtils.decimal2hex(Integer.parseInt(identity));
		String cellID = "01";

		returnValue = "" + identity + cellID;
		String decimal = ((Integer) GeneralUtils.hex2decimal(returnValue)).toString();

		return decimal;
	}

	public boolean setAnrState(SonAnrStates anrState) {
		return XLP.setANRState(anrState);

	}

	public String[] getStaticUes() {
		return staticUEs;
	}

	public void setStaticUes(String ues) {
		this.staticUEs = ues.split(",");
	}

	public String getRunningVersion() {
		return this.XLP.getRunningVersion();
	}

	public List<RFStatus> getRFStatus() {
		return this.XLP.getRFStatus();
	}

	public String getPTPInterfaceIP() {
		String oid = "" + MibReader.getInstance().resolveByName("asLteStkIpInterfaceCfgIpAddress");
		String ifIP;
		try {
			ifIP = this.XLP.snmp.get(oid + ".6");
			return ifIP;
		} catch (Exception e1) {
			e1.printStackTrace();
			report.report("Couldn't get PTP Interface IP (couldn't get asLteStkIpInterfaceCfgIpAddress MIB).");
			return "";
		}
	}

	public String ping(String ip, int tries) {
		String response = XLP.shell("ping -c " + tries + " " + ip);
		GeneralUtils.printToConsole("Response to ping: "+response);
		return response;
	}

	public String getPrimaryGrandMasterIP() {
		String oid = "" + MibReader.getInstance().resolveByName("asLteStkPtpMasterCfgPrimaryMasterIpAddress");
		String GM_IP;
		try {
			GM_IP = this.XLP.snmp.get(oid + ".1");
			return InetAddressesHelper.toDecimalIp(GM_IP, 16);

		} catch (Exception e1) {
			e1.printStackTrace();
			report.report("Couldn't getP GM IP (couldn't get asLteStkPtpMasterCfgPrimaryMasterIpAddress MIB).");
			return null;
		}
	}

	public PTPStatus getPTPStatus() {
		PTPStatus returnObject = new PTPStatus();
		String oidSync = MibReader.getInstance().resolveByName("asLteStkPtpStatusMasterSyncLocked");
		String oidConnected = MibReader.getInstance().resolveByName("asLteStkPtpStatusMasterConnected");
		try {
			returnObject.syncStatus = this.XLP.snmp.get(oidSync + ".1");
			returnObject.masterConnectivity = this.XLP.snmp.get(oidConnected + ".1");

		} catch (Exception e1) {
			e1.printStackTrace();
			report.report(
					"Couldn't PTPstatus (couldn't get asLteStkPtpStatusMasterSyncLocked, asLteStkPtpStatusMasterConnected MIB).");
			return null;
		}
		return returnObject;
	}

	public boolean isPTPLocked(){
		return this.XLP.isPTPLocked();
	}
	
	public boolean setPLMN(ArrayList<Plmn> PlmnList) throws IOException {
		return this.XLP.SetPLMN(PlmnList);
	}

	public boolean deletAllRowsInTable(String rowStatusOID) throws IOException {
		return this.XLP.deletAllRowsInTable(rowStatusOID);
	}

	public boolean addRowInTable(String rowStatusOid, HashMap<String, String> params) throws IOException {
		return XLP.addRowInTable(rowStatusOid, params);
	}

	public boolean isInService() {
		boolean res = false;
		res = this.XLP.getServiceState() == EnbStates.IN_SERVICE;
		return res;
	}

	public boolean isInService(int cellIndex) {
		boolean res = false;
		String ans = this.XLP.getCellServiceState(cellIndex);
		res = (ans!=null && ans.equals("1"));
		return res;
	}

	public boolean setServiceState(CellIndex cellIndex, int value) {
		if (cellIndex != CellIndex.ENB) {
			String oid = MibReader.getInstance().resolveByName("asLteStkChannelCfgServiceState");
			oid += ("." + cellIndex.value);
			String res = this.XLP.snmp.get(oid);
			if (res.equals("noSuchInstance")) {
				report.report("setServiceState - No such instance (" + cellIndex.value + ").", Reporter.WARNING);
			} else {
				try {
					return this.XLP.snmp.snmpSet(oid, value);
				} catch (Exception e) {
					e.printStackTrace();
					report.report("Couldn't set Service State (asLteStkChannelCfgServiceState MIB).",
							Reporter.WARNING);
				}
			}
		} else {
			EnbStates enbState = value == 0 ? EnbStates.OUT_OF_SERVICE : EnbStates.IN_SERVICE;
			return this.XLP.setOperationalStatus(enbState);
		}
		return false;
	}

	public String getTriggerX2HO(EnodeB neighbor) {
		return this.XLP.getTriggerX2Ho(neighbor);
	}

	public boolean changePTPinterface(String newPTPip) {
		String currentPTPip = this.getPTPInterfaceIP();
		currentPTPip = InetAddressesHelper.toDecimalIp(currentPTPip,16);
		
		report.report("Remove current PTP interface");
		if (!this.downPTPInterface(3))
			return false;

		report.report("Modify new PTP interface (" + newPTPip + ")");
		return this.upPTPInterface(newPTPip, 3);
	}

	public boolean waitForReachable(long timeout) {
		return this.XLP.waitForReachable(timeout);
	}

	public boolean downPTPInterface(int tries) {
		this.expecteInServiceState = false;
		for (int i = 0; i < tries; i++) {
			this.XLP.shell("ifconfig br0.6 down");
			boolean res = checkInterfaceStatus(2);
			if (res) 
			{
				report.report("ifconfig br0.6 down succeeded");
				return true;
			}
		}
		report.report("ifconfig br0.6 down fail", Reporter.WARNING);
		return false;
	}

	private boolean checkInterfaceStatus(int expectedIndex) // 1 - up, 2 - down 
	{
		long startTime = System.currentTimeMillis(); // fetch starting time
		while((System.currentTimeMillis() - startTime) < (2 * 60 * 1000))
		{
			int interfaceStatusIndex = XLP.getInterfaceStatusAvailabilityStatus(6);
			if (interfaceStatusIndex == expectedIndex)
				return true;
			GeneralUtils.unSafeSleep(10 * 1000);
		}
		return false;
	}
	
	public String getInterfaceStatusByIndex(int valueIndex,int interfaceIndex)
	{
		return XLP.getInterfaceStatusAvailabilityStatus(valueIndex, interfaceIndex);
	}
	public boolean upPTPInterface(String ptp_ip, int tries) {
		for (int i = 0; i < tries; i++) {
			this.XLP.shell("ifconfig br0.6 " + ptp_ip + " netmask 255.255.255.0 up");
			boolean res = checkInterfaceStatus(1);
			if (res) 
			{
				report.report("ifconfig br0.6 " + ptp_ip + " netmask 255.255.255.0 up succeeded");
				return true;
			}
		}
		report.report("ifconfig br0.6 " + ptp_ip + " netmask 255.255.255.0 up failed", Reporter.WARNING);
		return false;
	}

	public boolean changeBW(int bwInMHZ) {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellCfgChannelBandwidth");
		int value = GeneralUtils.ERROR_VALUE;
		switch (bwInMHZ) {
		case 5:
			value = 2;
			break;
		case 10:
			value = 3;
			break;
		case 20:
			value = 5;
			break;
		default:
			return false;
		}
		return this.XLP.snmp.snmpSet(oid, value);
	}

	public int getCellContextID() {
		return this.cellContextID;
	}

	public void setCellContextNumber(int cellNumber) {
		GeneralUtils.printToConsole("Setting cell context to " + cellNumber);
		this.cellContextID = cellNumber;
	}

	/**
	 * this method Uses SNMP only.
	 * @return
	 */
	public int getNumberOfActiveCells() {
		return this.XLP.getNumberOfActiveCells();
	}

	/**
	 * @author Avichai Yefet enable MAC to PHY capture
	 * @return
	 */
	public abstract void enableMACtoPHYcapture(SnifferFileLocation en) ;
	
	/**
	 * @author shahaf shuhamy
	 * @return
	 */
	public abstract void disableMACtoPHYcapture();

	/**
	 * @author Avichai Yefet link to MAC to PHY capture files
	 * @return
	 */
	public void showMACtoPHYCaptureFiles() {
		if (isCaptureHasBeenMade) {
			report.addLink("MAC to PHY capture files - " + getName(), captureFilesPath);
		}
	}

	public boolean isMACtoPHYEnabled() {
		return enableMACtoPHY;
	}

	public void setEnableMACtoPHY(String enableMACtoPHY) {
		this.enableMACtoPHY = Boolean.parseBoolean(enableMACtoPHY);
	}

	public boolean isSkipCMP() {
		return XLP.isSkipCMP();
	}

	/**
	 * @author Shahaf Shuhamy
	 * @param enb
	 * @param url
	 */
	public Boolean setNodeLoggerUrl(EnodeB enb, String url) {
		Boolean result = false;
		GeneralUtils.printToConsole("------Setting logger upload url------");
		String response = enb.XLP.lteCli("db set StackCfg [1] uploadUrl=" + url);
		if (response == null) {
			GeneralUtils.printToConsole("logger upload url failed to update");
			return false;
		}

		if (response.contains("entries updated")) {
			GeneralUtils.printToConsole("logger upload url updated!");
			result = true;
		} else {
			GeneralUtils.printToConsole("logger upload url failed to update");
		}
		GeneralUtils.printToConsole("------done with logger upload url------");
		return result;
	}

	/**
	 * writing "logger upload all" in lte Cli for the node
	 * 
	 * @author Shahaf Shuhamy
	 */
	public void loggerUploadAll() {
		XLP.lteCli("logger upload all");
	}

	public String getManagementIp() {
		String oid = MibReader.getInstance().resolveByName("asLteStkStackStatusActualManagementIpAddress");
		String managementIp = "";
		try {
			managementIp = this.XLP.snmp.get(oid);
			managementIp = InetAddressesHelper.toDecimalIp(managementIp, 16);
		} catch (Exception e) {
			e.printStackTrace();
			report.report("getManagementIp via SNMP due to : " + e.getMessage(), Reporter.WARNING);
		}
		return managementIp;
	}

	public boolean setMaxSshConnections(int maxSshConnections) {
		return this.XLP.setMaxSshConnections(maxSshConnections);
	}

	public int getMaxSshConnections() {
		return this.XLP.getMaxSshConnections();
	}

	public int getEarfcn() {
		String cell = getCellContextID() == 1 ? "40" : "41";
		GeneralUtils.printToConsole("Get earfcn of cell " + cell);
		int earfcn = XLP.getEarfcn(cell);
		return earfcn != -1 ? earfcn : XLP.getEarfcn(cell);
	}

	public Plmn getPlmn() {
		Plmn plmn = new Plmn();
		plmn.setMCC(this.mcc);
		plmn.setMNC(this.mnc);
		return plmn;
	}

	public boolean isSNMPAvailable() {
		return XLP.snmp.isAvailable();
	}
	
	public boolean isIpsecEnabled() {
		PbLteNwElementStatus ne1 = XLP.getPbLteNetworkElementStatus(5);
		if (ne1 == null) {
			return false;
		}
		return ne1.getGatewayIpAddress() != null;
	}
	
	
	public boolean isIpsecTunnelEnabled() {
		return ipsecTunnelEnabled;
	}

	public void setIpsecTunnelEnabled(String ipsecTunnelEnabled) {
		this.ipsecTunnelEnabled = GeneralUtils.stringToBoolean(ipsecTunnelEnabled);
	}

	public synchronized final int getSWTypeInstance() {
		return SWTypeInstance;
	}

	public synchronized final void setSWTypeInstance(int sWTypeInstance) {
		SWTypeInstance = sWTypeInstance;
	}
	
	public UEDist getUEsDist(){
		return ueDist;
	}
	
	@Override
	public void close() {
		super.close();
	}

	public int getRsrpAgingTimer() {
		return XLP.getRsrpAgingTimer();
	}
	
	public int getRntpAgingTimer() {
		return XLP.getRntpAgingTimer();
	}

	public void addCliDebugFlags(String... strings){
		for(String debugFlag : strings){
			debugFlags.add(debugFlag);
		}
	}

	public ArrayList<String> getDebugFlags() {
		return debugFlags;
	}

	public void getProfilerInfo(){
		Map<String, String> CLICommands = new HashMap<String, String>();
		CLICommands.put("prcmngr show xdbwrites attribute=1", "summary of XDB writes to persistent table");
		CLICommands.put("prcmngr show xdbwrites attribute=0 verbose=1", "summery of all XDB writes (and no writes)");
		CLICommands.put("profiler show perfstatsperkpiconsumer", "Profiling kpi Per consumer Statisitics");
		String result = "";
		String[] results;
		
		GeneralUtils.startLevel("Profiler info: " + netspanName);
		for(String command : CLICommands.keySet()){
			result = lteCli(command, "Table Name");
			if (!result.contains("Table Name") && !result.contains("Profile Name")){
				result = lteCli(command);
			}
			results = result.split("\\+", 2);
			if (results.length < 2 ){
				results = result.split("\\=", 2);
			}
			if (results.length > 1){
				result = results[1].replace("\n", "</br>");
				result = "<pre>" + result + "</pre>";
				report.reportHtml(getNetspanName() +": "+ CLICommands.get(command), result, true);
			}
			else 
				report.report("Couldn't get table from EnodeB");
		}
		GeneralUtils.stopLevel();
	}
	
	public boolean isCaptureHasBeenMade() {
		return isCaptureHasBeenMade;
	}

	public String getLoggerUploadAllEnodebIP() {
		return XLP.getIpAddress();
	}

	public boolean hasDonor() {
		return hasDonor ;
	}
	public boolean hasDan() {
		return hasDan ;
	}
	public String getMmeStatus() {
		return XLP.getMmeStatus();
	}

	public String getMmeStatusInfo() {
		return XLP.getMmeStatusInfo();
	}

	public String getAddressType() {
		return XLP.getAddressType();
	}
	
	public int getPfsType() {
		return XLP.getPfsType();
	}
	
	public boolean setPfsType(int value) {
		return XLP.setPfsType(value);
	}

	public String getParameterValueViaWebAccess(WebGuiParameters parameter){
		String ip = XLP.getIpAddress();
		return webAccess.getSecuredParameterValue(parameter, ip);
	}
	
	public void echoToSkipCmpv2(){
		XLP.echoToSkipCmpv2();
	}

	public void showLoginStatus() {
		XLP.showLoginStatus();
	}

	public boolean isSwUpgradeDuringPnP() {
		return swUpgradeDuringPnP;
	}

	public void setSwUpgradeDuringPnP(String swUpgradeDuringPnP) {
		this.swUpgradeDuringPnP = GeneralUtils.stringToBoolean(swUpgradeDuringPnP);
	}

	public ImageType getImageType() {
		return ImageType.LTE;
	}

	public String getIpsecCertificateMacAddress() {
		return ipsecCertificateMacAddress;
	}

	public void setIpsecCertificateMacAddress(String ipsecCertificateMacAddress) {
		this.ipsecCertificateMacAddress = ipsecCertificateMacAddress;
	}
	
	public String getIpAddress(){
		return XLP.getIpAddress();
		
	}
	
	public String getIpAddressType(){
		return XLP.getIpAddressType();
		
	}
	public String getSNMP(String strOID){
		return XLP.snmp.get(strOID);
	}
	
	public String getSNMP(String oid, Integer index){
		return XLP.snmp.get(oid, index);
	}
	public boolean dbSet(String table, String key, String row, String value){
		return XLP.dbSet(table, key, row, value);
	}
	
	public int getActiveUEPerQciAndTransmitDirection(TransmitDirection transmit, Character qci){
		return XLP.getActiveUEPerQciAndTransmitDirection(transmit, qci);
	}
	
	public Boolean getNumberOfUELinkStatusVolte(){
		return XLP.getNumberOfUELinkStatusVolte();
	}
	
	public HashMap<String, Integer> getUELinkStatusVolteTable(){
		return XLP.getUELinkStatusVolteTable();
	}
	
	public Boolean getNumberOfUELinkStatusEmergency(){
		return XLP.getNumberOfUELinkStatusEmergency();
	}
	
	public HashMap<String, Integer> getUELinkStatusEmergencyTable(){
		return XLP.getUELinkStatusEmergencyTable();
	}
	
	public HashMap<String, Integer> getULCrcPer(){
		return XLP.getULCrcPer();
	}
	
	public HashMap<String, Integer> getDLPer(){
		return XLP.getDLPer();
	}
	public boolean resetCounter(String tableName, String index, HashMap<String, String> KeyValuePairs){
		return XLP.resetCounter(tableName, index, KeyValuePairs);
	}
	
	public boolean snmpSet(String strOID, byte[] Value) throws IOException{
		return XLP.snmp.snmpSet(strOID, Value);
	}
	
	public boolean snmpSet(String strOID,InetAddress Value) throws IOException{
		return XLP.snmp.snmpSet(strOID, Value);
	}
	
	public boolean snmpSet(String strOID, int Value) throws IOException{
		return XLP.snmp.snmpSet(strOID, Value);
	}
	public boolean snmpSet(String strOID, String Value) throws IOException{
		return XLP.snmp.snmpSet(strOID, Value);
	}
	public int getCountersValue(String valueName){
		return XLP.getCountersValue(valueName);
	}
	
	public int getSingleSampleCountersValue(String valueName) {
		try {
			return ((XLP_15_2)XLP).getSingleSampleCountersValue(valueName);
		}catch(ClassCastException e) {
			report.report("XLP is not 15_2 and above - feature disabled");
			return -1;
		}
	}
	
	public int getCellBarredMibValue(int cellNum){
		return XLP.getCellBarredValue(cellNum);
	}
	public String shell(String command){
		return XLP.shell(command);
	}
	
	public void setDeviceUnderTest(Boolean deviceUnderTest){
		XLP.setDeviceUnderTest(deviceUnderTest);
	}
	
	public void clearTestParameters(){
		XLP.clearTestPrameters();
	}
	
	public HashSet<String> getCorePathList(){
		return XLP.getCorePathList();
	}
	
	public boolean isExpectBooting() {
		return XLP.isExpectBooting();
	}
	public boolean isStateChangedToCoreDump(){
		return XLP.isStateChangedToCoreDump();
	}
	
	public int getUnexpectedReboot(){
		return XLP.getUnexpectedReboot();
	}

	/**
	 * define the number of unexpected reboots on the current EnodeB object.
	 *
	 * @param unexpectedReboot - number of unexpected reboot
	 */
	public void setUnexpectedReboot(int unexpectedReboot){
		XLP.setUnexpectedReboot(unexpectedReboot);
	}
	 public void updateTestedVer(){
		 XLP.updateTestedVer();
	 }
	 
	 public boolean isBankSwapped() {
		 return XLP.isBankSwapped();
	 }
	 
	 public boolean setRSI(int value) {
		 return XLP.setRSI(value);
	 }
	 
	 public int getPci(int cellIndex) {
		 return XLP.getPci(cellIndex);
	 }
	 
	 public boolean setPnpMode(int value) {
		 return XLP.setPnpMode(value);
	 }
	 
	 public boolean deleteFile(String filePath) {
		 return XLP.deleteFile(filePath);
	 }
	 
	 public boolean isFileExists(String filePath) {
		 return XLP.isFileExists(filePath);
	 }
	 
	 public Boolean getPnpMode() {
		 return XLP.getPnpMode();
	 }
	 
	 public int getGranularityPeriod() {
		 return XLP.getGranularityPeriod();
	 }
	
	 public boolean setPci(int value) {
		 return XLP.setPci(value);
	 }
	 
	 public String getCFISnmp() {
		 return XLP.getCFISnmp();
	 }
	 
	 public void setPnpWarmResetModeAdmin(int value) {
		 XLP.setPnpWarmResetModeAdmin(value);
	 }
	 
	 public void setPnpWarmRebootMask(int value) {
		 XLP.setPnpWarmRebootMask(value);
	 }
	 
	 public String getTddAckMode() {
		 return XLP.getTddAckMode();
	 }
	 
	 public String getPnpWarmResetModeAdmin() throws IOException {
		 return XLP.getPnpWarmResetModeAdmin();
	 }
	 
	 public String getPnpWarmRebootMask() throws IOException {
		 return XLP.getPnpWarmRebootMask();
	 }
	 
	 public String getParchZeroCorrrelZone() {
		 return XLP.getParchZeroCorrrelZone();
	 }
	 
	 public String getDuplexModeSnmp() throws IOException {
		 return XLP.getDuplexModeSnmp();
	 }
	 
	 public String getCAMode() {
		 return XLP.getCAMode();
	 }
	 
	 public boolean setGranularityPeriod(int value){
		 return XLP.setGranularityPeriod(value); 
	 }
	 
	 public String getBandWidthSnmp() {
		 return XLP.getBandWidthSnmp();
	 }
	 
	 public String getSpecialSubFrameSnmp() throws IOException {
		 return XLP.getSpecialSubFrameSnmp();
	 }
	 
	 public void setExpectBooting(boolean expectBooting) {
		 if(expectBooting)
			 expecteInServiceState = false;
		 XLP.setExpectBooting(expectBooting);
	 }
	 
	 public String getEnbType() throws IOException {
		 return XLP.getEnbType();
	 }
	 
	 public void setEnbType(int value) {
		 XLP.setEnbType(value);
	 }
	 
	 public boolean isDynamicCFIEnable() throws IOException {
		 return XLP.isDynamicCFIEnable();
	 }
	 
	 public EnbStates getRunningState() {
		 return XLP.getRunningState();
	 }
	 
	 public String getCellServiceState() {
		 return XLP.getCellServiceState(40);
	 }
	 
	 public String getCellServiceState(int cell) {
		 return XLP.getCellServiceState(cell);
	 }
	 
	 public boolean enableDynamicCFI() throws IOException {
		 return XLP.enableDynamicCFI();
	 }
	 
	 public boolean disableDynamicCFI() throws IOException {
		 return XLP.disableDynamicCFI();
	 }
	 
	 public void addListenerToLogger(LogListener listener) {
		 XLP.addListenerToLogger(listener);
	 }
	 
	 public boolean deleteAnrNeighborsBySNMP(){
		 return XLP.deleteAnrNeighborsBySNMP();
	 }
	 
	 public boolean addNbr(EnodeB enodeB, EnodeB neighbor, HoControlStateTypes hoControlStatus,
				X2ControlStateTypes x2ControlStatus, HandoverTypes HandoverTypes, boolean isStaticNeighbor,
				String qOffsetRange) throws IOException{
		 return XLP.addNbr(enodeB, neighbor, hoControlStatus, x2ControlStatus, HandoverTypes, isStaticNeighbor, qOffsetRange);
	 }
	 
	 public boolean deleteAllNeighborsByCli(){
		 return XLP.deleteAllNeighborsByCli();
	 }
	 
	 public boolean verifyNoNeighbors(){
		 return XLP.verifyNoNeighbors();
	 }
	 
	 public boolean verifyAnrNeighbor(EnodeB neighbor){
		 return XLP.verifyAnrNeighbor(neighbor);
	 }
	 
	 public void updatePLMNandEutranCellID(EnodeB neighbor) throws IOException{
		 XLP.updatePLMNandEutranCellID(neighbor);
	 }
	 
	 public boolean deleteNeighborBySNMP(EnodeB neighbor) throws IOException{
		 return XLP.deleteNeighborBySNMP(neighbor);
	 }
	 
	 public String getAnrMode() throws IOException {
		 return XLP.getAnrMode();
	 }
	 
	 public String getAnrDurationTimer() throws IOException {
		 return XLP.getAnrDurationTimer();
	 }
	 
	 public boolean verifyNbrList(EnodeB enodeB, EnodeB neighbor, HoControlStateTypes hoControlStatus,
				X2ControlStateTypes x2ControlStatus, HandoverTypes HandoverTypes, boolean isStaticNeighbor,
				String qOffsetRange) throws IOException{
		 return XLP.verifyNbrList(enodeB, neighbor, hoControlStatus, x2ControlStatus, HandoverTypes, isStaticNeighbor, qOffsetRange);
	 }
	 
	 public boolean verifyNbrList(EnodeB neighbor) throws IOException{
		 return XLP.verifyNbrList(neighbor);
	 }
	 
	 public boolean setOperationalStatus(EnbStates enbState) {
		 return XLP.setOperationalStatus(enbState);
	 }
	 
	 public int getUeNumberToEnb() {
		 return XLP.getUeNumberToEnb();
	 }
	 
	 public boolean addNewEntry(String rowStatusOid) throws IOException{
		 return XLP.snmp.addNewEntry(rowStatusOid);
	 }
	 
	 public boolean setTddAckMode(int value) {
		 return XLP.setTddAckMode(value);
	 }
	 
	 public boolean setPrachZeroCorrelZone(int value) {
		 return XLP.setPrachZeroCorrelZone(value);
	 }
	 
	 public boolean setCAMode(int value) {
		 return XLP.setCAMode(value);
	 }
	 
	 public void grantPermission(String filePath) {
		 XLP.grantPermission(filePath);
	 }
	 
	 public String getUsername() {
		 return XLP.getUsername();
	 }
	 
	 public String getPassword() {
		 return XLP.getPassword();
	 }
	 
	 public boolean isAllRunning() {
		 return XLP.isAllRunning();
	 }
	 public boolean isAvailable(){
		 return XLP.snmp.isAvailable();
	 }
	 
	 public boolean waitForSnmpAvailable(long timeOut) {
		 return XLP.waitForSnmpAvailable(timeOut);
	 }
	 
	 public boolean setSheddingMode(int value) {
		 return XLP.setSheddingMode(value);
	 }
	 
	 public boolean setQci1And2ActivityTimer(int value) {
		 return XLP.setQci1And2ActivityTimer(value);
	 }
	 
	 public int getMaxVolteCalls() {
		 return XLP.getMaxVolteCalls();
	 }
	 
	 public void disableIdleModeAndReboot() {
		 XLP.disableIdleModeAndReboot();
	 }
	 
	 public void enableIdleModeAndReboot() {
		 XLP.enableIdleModeAndReboot();
	 }
	 
	 public void setSessionLogLevel(String sessionName, int level) {
		 XLP.setSessionLogLevel(sessionName, level);
	 }
	 
	 public void setSessionLogLevel(String client, String process, int level) {
		 XLP.setSessionLogLevel(client, process, level);
	 }
	 
	 public int getPacketForwardingEnable(int cellId){
		 return XLP.getPacketForwardingEnable(cellId);
	 }
	 
	 public boolean setPacketForwardingEnable(boolean value, int cellId) {
		 return XLP.setPacketForwardingEnable(value, cellId);
	 }
	 
	public HashMap<String,Variable> getUEShowLinkTable(){
		return XLP.getUEShowLinkTable();
	}
	
	public boolean setMaxVolteCalls(int value) {
		return XLP.setMaxVolteCalls(value);
	}
	
	public void setIpAddress(String ipAddress) {
		XLP.setIpAddress(ipAddress);
	}
	
	public void restartSessions() {
		XLP.restartSessions();
	}
	
	public void initSNMP() {
		XLP.initSNMP();
	}
	
	public synchronized boolean isReachable() {
		return XLP.isReachable();
	}
	
	public int[] getLedStatusValues() {
		return XLP.getLedStatusValues();
	}
	
	public Session getSSHlogSession() {
		return XLP.getSSHlogSession();
	}
	
	
	public int getRfStatusAchievedTxPower(int cellIndex) { 
		return XLP.getRfStatusAchievedTxPower(cellIndex);
	}
	
	public boolean setTxPower(int value) {
		return XLP.setTxPower(value);
	}
	
	public boolean isInOperationalStatus() {
		return XLP.isInOperationalStatus();
	}
	
	public String getOperationalStatus() {
		return XLP.getOperationalStatus();
	}
	
	public void snmpSet(String readCommunityOrUserName, String writeCommunityOrPassword, String strAddress,String snmpVersion) {
		XLP.snmp = new SNMP(readCommunityOrUserName,writeCommunityOrPassword,strAddress,snmpVersion);
		
	}
	
	public String getSnmpVersion() {
		return XLP.snmp.getSnmpVersion();
	}
	
	public String getReadCommunityOrUserName() {
		return XLP.snmp.getReadCommunityOrUserName();
	}
	
	public String getWriteCommunityOrPassword() {
		return XLP.snmp.getWriteCommunityOrPassword();
	}
	
	public boolean setSmonThresholdsCriticalMin(int entry, int value) {
		return XLP.setSmonThresholdsCriticalMin(entry, value);
	}
	
	public EnbStates getServiceState() {
		return XLP.getServiceState();
	}
	
	public boolean setSmonThresholdsCriticalMax(int entry, int value) {
		return XLP.setSmonThresholdsCriticalMax(entry, value);
	}
	
	public int getSmonThresholdsCriticalMax(int entry) {
		return XLP.getSmonThresholdsCriticalMax(entry);
	}
	
	public int getTemperatureSensorsSensorReading(int entry) {
		return XLP.getTemperatureSensorsSensorReading(entry);
	}
	
	public int getSmonThresholdsCriticalMin(int entry) {
		return XLP.getSmonThresholdsCriticalMin(entry);
	}
	
	public boolean waitUntilNotAvailable(long timeOut) {
		return XLP.snmp.waitUntilNotAvailable(timeOut);
	}
	
	public ArrayList<String> getVlanIds(){
		return XLP.getVlanIds();
	}
	
	public String getExternalSubNetCIDR(){
		return XLP.getExternalSubNetCIDR();
	}
	
	public ArrayList<String> getDefaultGateWayAddresses(){
		return XLP.getDefaultGateWayAddresses();
	}
	
	public boolean remove3PartyNbrs(int numberOfNbr, final long mnoBroadcastPlmn, long startEutranCellId){
		boolean result = false;
		try {
			result = XLP.remove3PartyNbrs(numberOfNbr, mnoBroadcastPlmn, startEutranCellId);
		} catch (IOException e) {
			report.report("Couldn't remove 3rd party neighbor", Reporter.WARNING);
			e.printStackTrace();
		}
		return result;
	}
	
	public boolean add3PartyNbrs(int numberOfNbr, final long mnoBroadcastPlmn, long startEutranCellId,
			Integer enbType_optional, Integer qOffsetCell_optional) throws IOException{
		return XLP.add3PartyNbrs(numberOfNbr, mnoBroadcastPlmn, startEutranCellId, enbType_optional, qOffsetCell_optional);
	}
	
	public int getNrtChanges() {
		return XLP.getNrtChanges();
	}
	
	public boolean setRfChanges(int value) {
		return XLP.setRfChanges(value);
	}
	
	public int getRfChanges() {
		return XLP.getRfChanges();
	}
	
	public boolean setNrtChanges(int value) {
		return XLP.setNrtChanges(value);
	}
	
	public boolean setNetworkElementChanges(int value) {
		return setNetworkElementChanges(value);
	}
	
	public boolean setMmeChanges(int value) {
		return setMmeChanges(value);
	}
	
	public boolean setCellChanges(int value) {
		return setCellChanges(value);
	}
	
	public boolean setCallTraceEnbCfgTraceServerIpAddr(InetAddress inetAddress) {
		return XLP.setCallTraceEnbCfgTraceServerIpAddr(inetAddress);
	}
	
	public boolean provisionStartEvent() {
		return XLP.provisionStartEvent();
	}
	
	public boolean provisionEndedEvent() {
		return XLP.provisionEndedEvent();
	}
	
	public PbLteNwElementStatus getPbLteNetworkElementStatus(int networkType) {
		return XLP.getPbLteNetworkElementStatus(networkType);
	}
	
	public PbLteAnrStatus getPbLteSAnrStatus(long mnoBroadcastPlmn, long eutranCellId) {
		return XLP.getPbLteSAnrStatus(mnoBroadcastPlmn, eutranCellId);
	}
	
	public PbLteRfStatus getPbLteRfStatus(int rfPathId) {
		return XLP.getPbLteRfStatus(rfPathId);
	}
	
	public PbLteMmeStatus getPbLteMmeStatus(String ipAddress) {
		return XLP.getPbLteMmeStatus(ipAddress);
	}
	
	public PbLteCellStatus getPbLteCellStatus(int cellNumber) {
		return XLP.getPbLteCellStatus(cellNumber);
	}
	
	public int getNghListEnbType(long mnoBroadcastPlmn, long eutranCellId) {
		return XLP.getNghListEnbType(mnoBroadcastPlmn, eutranCellId);
	}
	
	public int getNghListQOffsetCell(long mnoBroadcastPlmn, long eutranCellId) {
		return XLP.getNghListQOffsetCell(mnoBroadcastPlmn, eutranCellId);
	}
	
	public int getNetworkElementChanges() {
		return XLP.getNetworkElementChanges();
	}
	
	public String getMmeStatusIpAddress() {
		return XLP.getMmeStatusIpAddress();
	}
	
	public String getMmeConfigIpAddress() {
		return XLP.getMmeConfigIpAddress();
	}
	
	public int getMmeChanges() {
		return XLP.getMmeChanges();
	}
	
	public int getCellChanges() {
		return XLP.getCellChanges();
	}
	
	public String getCallTraceEnbCfgTraceServerIpAddr() {
		return XLP.getCallTraceEnbCfgTraceServerIpAddr();
	}
	
	public boolean setBarringValues(int factorSig, int factorDat, int timeSig, int timeDat, int sigAdmin, int datAdmin,	int enable) {
		return XLP.setBarringValues(factorSig, factorDat, timeSig, timeDat, sigAdmin, datAdmin, enable);
	}
	
	public int connectionEstabAttSumSNMP() {
		return XLP.connectionEstabAttSumSNMP();
	}
	
	public int connectionEstabAttSuccSNMP() {
		return XLP.connectionEstabAttSuccSNMP();
	}
	
	public boolean updateIntegrityNull(int integrityNullLevel) {
		return XLP.updateIntegrityNull(integrityNullLevel);
	}
	
	public boolean updateIntegritySNW(int integritySNOWLevel) {
		return XLP.updateIntegritySNW(integritySNOWLevel);
	}
	
	public boolean updateIntegrityAES(int integrityAESLevel) {
		return XLP.updateIntegrityAES(integrityAESLevel);
	}
	
	public boolean updateCipheringSNW(int cipheringSNOWLevel) {
		return XLP.updateCipheringSNW(cipheringSNOWLevel);
	}
	
	public boolean updateCipheringNull(int cipheringNullLevel) {
		return XLP.updateCipheringNull(cipheringNullLevel);
	}
	
	public boolean updateCipheringAES(int cipheringAESLevel) {
		return updateCipheringAES(cipheringAESLevel);
	}
	
	public boolean setOtdoaMode(boolean value){
		return XLP.setOtdoaMode(value);
	}
	
	public boolean setAnrLightRatio(int val) {
		return XLP.setAnrLightRatio(val);
	}
	
	public boolean setAnrLightMinUes(int val) {
		return XLP.setAnrLightMinUes(val);
	}
	
	public int getRSRPEventTriggerGaps() {
		return XLP.getRSRPEventTriggerGaps();
	}
	
	public int getRSRPEventStopGaps() {
		return XLP.getRSRPEventStopGaps();
	}
	
	public int getAnrLightRatio() {
		return XLP.getAnrLightRatio();
	}
	
	public int getAnrLightMinUes() {
		return XLP.getAnrLightMinUes();
	}
	
	public String lteCliWithResponse(String command, String response) {
		return XLP.lteCliWithResponse(command, response);
	}
	
	public void setAnrMode(int value) {
		XLP.setAnrMode(value);
	}
	
	public boolean setECIDMode(boolean value){
		return XLP.setECIDMode(value);
	}
	
	public Integer getPrsCfgIndex(){
		return XLP.getPrsCfgIndex();
	}
	
	public boolean getOTDOAMode(CellIndex cellIndex){
		return XLP.getOTDOAMode(cellIndex);
	}
	
	public Integer getECIDTimer(){
		return XLP.getECIDTimer();
	}
	
	public Integer getPrsPowerOffset(){
		return XLP.getPrsPowerOffset();
	}
	
	public boolean getECIDMode(){
		return XLP.getECIDMode();
	}
	
	public Integer getCfgPrsMutePeriod(){
		return XLP.getCfgPrsMutePeriod();
	}
	
	public Integer getCfgPrsMutePattSeq(){
		return XLP.getCfgPrsMutePattSeq();
	}
	
	public Integer getCfgPrsBandwidth(){
		return XLP.getCfgPrsBandwidth();
	}
	
	public long waitForUpdate(long timeout, String oid, Integer index, String expected) {
		return XLP.snmp.waitForUpdate(timeout, oid, index, expected);
	}
	
	public ArrayList<Plmn> getConfiguredPLMNList() {
		return XLP.getConfiguredPLMNList();
	}
	
	public void setNghRemoveThreshold(int value) {
		XLP.setNghRemoveThreshold(value);
	}
	
	public void setEvalPeriod(int value) {
		XLP.setEvalPeriod(value);
	}
	
	public String getX2ConStatus(String mnoBroadcastPlmn, String eutranCellId) {
		return XLP.getX2ConStatus(mnoBroadcastPlmn, eutranCellId);
	}
	
	public int getEvalPeriod() {
		return XLP.getEvalPeriod();
	}
	
	public int getNghRemoveThreshold(){
		return XLP.getNghRemoveThreshold();
	}
	
	public String getHomeCellIdentity() {
		return XLP.getHomeCellIdentity();
	}
	
	public String getSonCfgPciAuto() {
		return XLP.getSonCfgPciAuto();
	}
	
	public int getReEstablishmentSameCellPoorCoverage(CellIndex cellIndex) {
		return XLP.getReEstablishmentSameCellPoorCoverage(cellIndex);
	}
	
	public boolean setTPMMoEnable(boolean value, CellIndex cellIndex)  {
		return XLP.setTPMMoEnable(value, cellIndex);
	}
	
	public boolean setTPMServRsrpRlfThresh(int value, CellIndex cellIndex) {
		return XLP.setTPMServRsrpRlfThresh(value, cellIndex);
	}
	
	public boolean setTpmMo(int value, CellIndex cellIndex) {
		return XLP.setTpmMo(value, cellIndex);
	}
	
	public boolean setTpmNlPeriodic(int value, CellIndex cellIndex) {
		return XLP.setTpmNlPeriodic(value, cellIndex);
	}
	
	public boolean setTpmCycleMultiple(int value, CellIndex cellIndex) {
		return XLP.setTpmCycleMultiple(value, cellIndex);
	}
	
	public boolean setTPMNghRsrpRlfThresh(int value, CellIndex cellIndex) {
		return XLP.setTPMNghRsrpRlfThresh(value, cellIndex);
	}
	
	public boolean setTPMEnable(boolean value, CellIndex cellIndex)  {
		return XLP.setTPMEnable(value, cellIndex);
	}
	
	public int getTpmMoPwrAdj(CellIndex cellIndex) {
		return XLP.getTpmMoPwrAdj(cellIndex);
	}
	
	public int getTPMDelMoInc(CellIndex cellIndex) {
		return XLP.getTPMDelMoInc(cellIndex);
	}
	
	public int getOutgoingIntraFreqHoAttempt(CellIndex cellIndex) {
		return XLP.getOutgoingIntraFreqHoAttempt(cellIndex);
	}
	
	public int getTPMDelMoDec(CellIndex cellIndex) {
		return XLP.getTPMDelMoDec(cellIndex);
	}
	
	public int GetIntraHoTooLatePoorCoverage(CellIndex cellIndex) {
		return XLP.GetIntraHoTooLatePoorCoverage(cellIndex);
	}
	
	public int GetIntraHoTooLateGoodCoverageUnprepared(CellIndex cellIndex) {
		return XLP.GetIntraHoTooLateGoodCoverageUnprepared(cellIndex);
	}
	
	public Session getSerialSession() {
		return XLP.getSerialSession();
	}
	
	public String getXLPName(){
		return XLP.getName();
	}
	
	public boolean isInstanceOfXLP_14_0(){
		return (XLP instanceof XLP_14_0);
	}
	
	public boolean isInstanceOfXLP_15_2(){
		return (XLP instanceof XLP_15_2);
	}
	
	public Session getDefaultSession(String componentName){
		return XLP.getDefaultSession();
	}
	
	public String sendCommandsOnSession(String prompt, String command, String response, int responseTimeout){
		return XLP.sendCommandsOnSession(XLP.getDefaultSession().getName(), prompt, command, response, responseTimeout);
	}

	public String getParallelCommandsPrompt() {
		return XLP.getParallelCommandsPrompt();
	}

	public String getFrameConfig() {
		return XLP.getFrameConfig();
	}

	public String getScpPassword() {
		return XLP.getScpPassword();
	}
	
	public String getScpUsername() {
		return XLP.getScpUsername();
	}
	
	public int getScpPort() {
		return XLP.getScpPort();
	}

	public void downloadStats() {
		XLP.downloadStats();
	}

	public void waitForExpectBootingValue(long timeout, boolean status) {
		XLP.waitForExpectBootingValue(timeout, status);
	}

	public EnabledStates getOperateBehindHenbGw() {
		return XLP.getOperateBehindHenbGw();
	}
	public String getSkipCMP() {
		return SkipCMP;
	}
	public void setSkipCMP(String skipCMP) {
		SkipCMP = skipCMP;
	}

	public Pair<Boolean, SwStatus> isSoftwareDownloadCompletedSuccessfully() {
		return XLP.isSoftwareDownloadCompletedSuccessfully();
	}

	public Pair<Long, String>[] getExpectedDurationsAndStageNamesOrderedForColdReboot() {
		if(swUpgradeDuringPnP){
			return expectedDurationsAndStageNamesOrderedWithSoftwareDownloadForColdReboot;
		}else{
			return expectedDurationsAndStageNamesOrderedForColdReboot;
		}
	}

	public Pair<Long, String>[] getExpectedDurationsAndStageNamesOrderedForWarmReboot() {
		return expectedDurationsAndStageNamesOrderedForWarmReboot;
	}
	
	public boolean getLoggerDebugCapEnable() {
		return XLP.getLoggerDebugCapEnable();
	}
	
	public boolean setLoggerDebugCapEnable(boolean enable) {
		return XLP.setLoggerDebugCapEnable(enable); 
	}
	
	public int getNumberOfCells(){
		return XLP.getNumberOfCells();
	}
}
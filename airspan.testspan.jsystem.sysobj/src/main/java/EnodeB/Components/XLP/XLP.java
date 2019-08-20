package EnodeB.Components.XLP;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.Variable;

import EnodeB.EnodeB;
import EnodeB.EnodeBUpgradeServer;
import EnodeB.PhyState;
import EnodeB.Components.EnodeBComponent;
import EnodeB.Components.Cli.Cli;
import EnodeB.Components.Session.SessionManager;
import EnodeB.ProtoBuf.PbLteStatusOuterClass.PbLteAnrStatus;
import EnodeB.ProtoBuf.PbLteStatusOuterClass.PbLteCellStatus;
import EnodeB.ProtoBuf.PbLteStatusOuterClass.PbLteMmeStatus;
import EnodeB.ProtoBuf.PbLteStatusOuterClass.PbLteNwElementStatus;
import EnodeB.ProtoBuf.PbLteStatusOuterClass.PbLteRfStatus;
import EnodeB.ProtoBuf.PbLteStatusOuterClass.PbLteSgwStatus;
import EnodeB.ProtoBuf.ProtoBuf;
import Entities.ITrafficGenerator.TransmitDirection;
import Netspan.NetspanServer;
import Netspan.API.Enums.EnabledStates;
import Netspan.API.Enums.EnbStates;
import Netspan.API.Enums.HandoverType;
import Netspan.API.Enums.HoControlStateTypes;
import Netspan.API.Enums.ServerProtocolType;
import Netspan.API.Enums.SonAnrStates;
import Netspan.API.Enums.X2ControlStateTypes;
import Netspan.API.Lte.RFStatus;
import Netspan.Profiles.NetworkParameters.Plmn;
import Utils.Earfcn;
import Utils.GeneralUtils;
import Utils.GeneralUtils.CellIndex;
import Utils.InetAddressesHelper;
import Utils.MoxaCom;
import Utils.Pair;
import Utils.PasswordUtils;
import Utils.Snmp.MibReader;
import jsystem.framework.IgnoreMethod;
import jsystem.framework.report.Reporter;
import jsystem.framework.system.SystemManagerImpl;

/**
 * The Class XLP.
 */
public class XLP extends EnodeBComponent {
	String mnoBroadcastPlmn = null;
	String eutranCellId = null;

	// Sotware download status Codes
	public enum SwStatus {
		SW_STATUS_IDLE(swStatusIdle), SW_STATUS_INSTALL_IN_PROGRESS(
				swStatusInstallInProgress), SW_STATUS_ACTIVATION_IN_PROGRESS(
						swStatusActivationInProgress), SW_STATUS_INSTALL_FAILURE(
								swStatusInstallFailure), SW_STATUS_ACTIVATION_FAILURE(
										swStatusActivationFailure), SW_STATUS_ILLEGAL_VALUE("swStatusIllegalValue");

		public String value;

		SwStatus(String code) {
			value = code;
		}

		public static SwStatus fromValue(String v) {
			for (SwStatus c : SwStatus.values()) {
				if (c.value.equals(v)) {
					return c;
				}
			}
			throw new IllegalArgumentException(v);
		}
	}

	public final static String swStatusIdle = "1";
	public final static String swStatusInstallInProgress = "2";
	public final static String swStatusActivationInProgress = "3";
	public final static String swStatusInstallFailure = "4";
	public final static String swStatusActivationFailure = "5";

	/**
	 * The Constant FPGA_VERSION_DELIMITER.
	 */
	public static final String FPGA_VERSION_DELIMITER = "0x";

	/**
	 * The Constant MIN_XLP_VER_WITH_LOGGER_THRESHOLD_CHANGE.
	 */
	public static final String MIN_XLP_VER_WITH_LOGGER_THRESHOLD_CHANGE = "14.13.10.";

	/**
	 * The Constant MIN_XLP_VER_WITH_UE_SHOW_LINK_CHANGE.
	 */
	public static final String MIN_XLP_VER_WITH_UE_SHOW_LINK_CHANGE = "14.13.10.50";

	/**
	 * The Constant UE_SHOW_LINK_HEADERS.
	 */
	public static final String[] UE_SHOW_LINK_HEADERS = new String[] { "index", "crnti", "PHY-CRC", "%CRC", "DL-CQI",
			"MCS", "RI", "UL", "C2I", "RSSI", "TO", "UL", "DL", "measGap", "SFR-DL", "SFR-UL", "SFR-CQI" };

	/**
	 * Upgrade server's ip
	 */
	private static String upgradeServerIp = "";

	/**
	 * The logger thresholds.
	 */
	private Hashtable<String, String[]> loggerThresholds;

	/**
	 * The hardware name (in case this module is not a XLP).
	 */
	public String hardwareName = "XLP";
	private String testerVer = null;

	private String enodebRunningVersion;
	private String enodebStandbyVersion;

	public String getEnodebRunningVersion() {
		return enodebRunningVersion;
	}

	public void setEnodebRunningVersion(String enodebRunningVersion) {
		this.enodebRunningVersion = enodebRunningVersion;
	}

	public String getEnodebStandbyVersion() {
		return enodebStandbyVersion;
	}

	public void setEnodebStandbyVersion(String enodebStandbyVersion) {
		this.enodebStandbyVersion = enodebStandbyVersion;
	}

	public boolean deleteAnrNeighborsBySNMP() {
		boolean action = true;
		String oid = MibReader.getInstance().resolveByName("asLteStkNghListMnoBroadcastPlmn");

		HashMap<String, Variable> walkResult = snmp.SnmpWalk(oid);
		if (walkResult != null) {
			if (walkResult.size() != 0) {
				String cmdFlagOid = MibReader.getInstance().resolveByName("asLteStkCmdQNghListCmdFlag");
				String rowStatOid = MibReader.getInstance().resolveByName("asLteStkCmdQNghListRowStatus");
				provisionStartEvent();
				for (String instance : walkResult.keySet()) {
					String[] temp = instance.split("\\.");
					String ins = "";
					int len = temp.length;
					if (len >= 2) {
						ins = temp[temp.length - 2] + "." + temp[temp.length - 1];
					}
					action = action && snmp.snmpSet(cmdFlagOid + "." + ins, 1);
					action = action && snmp.snmpSet(rowStatOid + "." + ins, 1);
				}
				provisionEndedEvent();
			}
		} else {
			report.report("Failed to get nbr list via SNMP");
			return false;
		}
		return action;
	}

	private static int numOfCells = 0;

	/**
	 * (non-Javadoc)
	 *
	 * @see EnodeB.Components.EnodeBComponent# & connecting to serialCom. then
	 *      connecting via SSH, if fails get the serialCom , insure that the XLP
	 *      is in running state.
	 */
	public void init() throws Exception {
		super.init();
		updateVersions();
		sessionManager.openSSHCommandSession();
		super.startLogStreamer();
		super.initScpClient();
		try {
			XLP.setUpgradeServerIp();
		} catch (Exception e) {
			report.report("The UpgradeServer System Object is not define correctly", Reporter.FAIL);
			throw e;
		}
	}

	private boolean try2Connect(String version){
		GeneralUtils.printToConsole("Connecting with version " + version + " Credentials");
		setUserNameAndPassword(version);
		setScpParams(version);
		return validateCredentials();
	}
	
	private boolean try2ConnectMultiVersion(String... versions){
		for(String version : versions){
			if(try2Connect(version)){
				enodebRunningVersion = version;
				report.report(getName() + " - Connected with version " + version + " Credentials");
				return true;
			}
			GeneralUtils.printToConsole("Failed to connect with version " + version + " Credentials");
			resetCredentials();
		}
		return false;
	}
	
	@Override
	public boolean updateVersions() {
		// get versions from SNMP/NMS
		if(getMajorVersions()){
			return try2ConnectMultiVersion(enodebRunningVersion, enodebStandbyVersion, "16.0");
		}
		
		// try all versions with sut priority in case getMajors failed. 
		else{
			enodebRunningVersion = ((EnodeB) parent).getEnodeBversion();
			switch (enodebRunningVersion) {
			case "17.0":
			case "16.5":
				return try2ConnectMultiVersion("16.5", "16.0", "15.5");				
			case "16.0":
				return try2ConnectMultiVersion("16.0", "16.5", "15.5");
			default:
				return try2ConnectMultiVersion("15.5", "16.0", "16.5");
			}					
		}
	}

	private boolean validateCredentials() {
		GeneralUtils.printToConsole(
				"Trying to connect to serial with user:" + getSerialUsername() + " password:" + getSerialPassword());
		if (!sessionManager.openSerialLogSession()) {
			GeneralUtils.printToConsole(
					"Failed to login to serial with user:" + getSerialUsername() + " password:" + getSerialPassword());
			return false;
		}
		return true;
	}

	private void resetCredentials() {
		sessionManager.closeSession(sessionManager.getSerialSession().getName());
		sessionManager.setSerialSession(null);
		serialCom.reset();
		setSerialUsername(null);
		setSerialPassword(null);
		setUsername(null);
		setPassword(null);
	}

	private boolean getMajorVersions() {
		GeneralUtils.printToConsole("Get Major Versions");
		try {
			if (snmp != null) {
				enodebRunningVersion = getMajorVer(getRunningVersion());
				enodebStandbyVersion = getMajorVer(getStandbyVersion());
				GeneralUtils.printToConsole(
						"SNMP returned running ver: " + enodebRunningVersion + " , standby: " + enodebStandbyVersion);
				if(enodebRunningVersion == null || enodebStandbyVersion == null)
					return false;
				return true;
			}
		} catch (IOException e) {
			e.printStackTrace();
			report.report("Couldn't get SW version from SNMP", Reporter.WARNING);
		}
		/*if (enodebRunningVersion == null) {
			try {
				enodebRunningVersion = getMajorVer(
						NetspanServer.getInstance().getRunningVer(((EnodeB) parent).getNetspanName()));
				enodebStandbyVersion = getMajorVer(
						NetspanServer.getInstance().getStandbyVer(((EnodeB) parent).getNetspanName()));
				GeneralUtils.printToConsole(
						"Netspan returned running ver: " + enodebRunningVersion + " , standby: " + enodebStandbyVersion);
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				report.report("Couldn't get SW version from Netspan", Reporter.WARNING);
			}
		}*/
		return false;
	}

	private String getMajorVer(String ver) {
		if (ver != null && ver.length() > 4) {
			int start = ver.indexOf(".") + 1;
			int end = ver.lastIndexOf(".");
			if (end > 0)
				return ver.substring(start, end);
		}
		return null;
	}

	/**
	 * Prints EnodeB version to the Jenkins Console.
	 *
	 * @throws Exception
	 *             -Exception
	 */
	public void printVersion() throws Exception {
		String version = getVersion();
		if (version != null)
			GeneralUtils.printToConsole(String.format("%s(ver:%s) initialized!", getHardwareName(), version));
		else
			throw new Exception("Initialization error: Can't get " + getHardwareName() + " version.");
	}

	protected void setUserNameAndPassword(String majorVersion) {
		if (getUsername() == null) {
			setUsername(getDefaultSSHUsername(majorVersion));
		}
		if (!getUsername().equals(getDefaultSSHUsername(majorVersion))) {
			report.report("SSH username in SUT (" + getUsername() + ") is different from default for this version ("
					+ getDefaultSSHUsername(majorVersion) + ")", Reporter.WARNING);
		}
		setPassword(getMatchingPassword(getUsername(), majorVersion));

		if (getSerialUsername() == null) {
			setSerialUsername(getDefaultSerialUsername(majorVersion));
		}
		if (!getSerialUsername().equals(getDefaultSerialUsername(majorVersion))) {
			report.report("Serial username in SUT (" + getSerialUsername()
					+ ") is different from default for this version (" + getDefaultSerialUsername(majorVersion) + ")",
					Reporter.WARNING);
		}
		setSerialPassword(getMatchingPassword(getSerialUsername(), majorVersion));
		setSerialSwitchUserCommand(getMatchingSwitchUserCommand(getSerialUsername()));
	}

	private void setScpParams(String majorVersion) {
		if (Float.parseFloat(majorVersion) >= 16) {
			setScpUsername(PasswordUtils.ROOT_USERNAME);
			setScpPort(2020);
		}
		if (Float.parseFloat(majorVersion) >= 16.5)
			setScpPassword(PasswordUtils.ROOT_PASSWORD_16_50);
	}

	@Override
	public String getMatchingPassword(String username) {
		return getMatchingPassword(username, enodebRunningVersion);
	}

	public String getMatchingPassword(String username, String majorVersion) {
		String ans = "";
		if (Float.parseFloat(majorVersion) < 16.5) {
			switch (username) {
			case "root":
			case "admin":
			case "airspansu":
				ans = PasswordUtils.ADMIN_PASSWORD;
				break;
			case "op":
				ans = PasswordUtils.COSTUMER_PASSWORD;
				break;
			default:
				GeneralUtils
						.printToConsole("Unrecognised username: " + username + ". using admin password as default.");
				ans = PasswordUtils.ADMIN_PASSWORD;
				break;
			}
		} else {
			switch (username) {
			case "root":
			case "airspansu":
				ans = PasswordUtils.ROOT_PASSWORD_16_50;
				break;
			case "op":
				ans = PasswordUtils.COSTUMER_PASSWORD_16_50;
				break;
			default:
				GeneralUtils
						.printToConsole("Unrecognised username: " + username + ". using admin password as default.");
				ans = PasswordUtils.ROOT_PASSWORD_16_50;
				break;
			}
		}
		return ans;
	}

	private String getMatchingSwitchUserCommand(String username) {
		String ans = "";
		switch (username) {
		case "root":
			ans = "";
			break;
		case "admin":
			ans = "su -";
			break;
		case "op":
			ans = "airspansu";
			break;
		default:
			ans = "su -";
			break;
		}
		return ans;
	}

	protected String getDefaultSSHUsername(String majorVersion) {
		if (Float.parseFloat(majorVersion) >= 16)
			return PasswordUtils.COSTUMER_USERNAME;
		else
			return PasswordUtils.ADMIN_USERNAME;
	}

	protected String getDefaultSerialUsername(String majorVersion) {
		if (Float.parseFloat(majorVersion) >= 16)
			return PasswordUtils.ROOT_USERNAME;
		else
			return PasswordUtils.ADMIN_USERNAME;
	}

	public String getTesterVer() {
		return testerVer;
	}

	public void updateTestedVer() {
		this.testerVer = this.getRunningVersion();
	}

	/**
	 * (non-Javadoc)
	 *
	 * @see EnodeB.Components.EnodeBComponent addPrompts()
	 */
	@Override
	public void addPrompts(Cli cli) {
		super.addPrompts(cli);
		cli.addPrompt(LTE_CLI_PROMPT, SHELL_PROMPT,
				new String[] { "su -", getMatchingPassword(PasswordUtils.ROOT_USERNAME), "/bs/lteCli" }, "quit");
		cli.addPrompt(SHELL_PROMPT_OP, SHELL_PROMPT,
				new String[] { "su -", getMatchingPassword(PasswordUtils.ROOT_USERNAME) }, "exit"); // remove
																									// ?
	}

	/**
	 * Db get.
	 *
	 * @param table
	 *            the table
	 * @return the hashtable
	 */
	public Hashtable<String, String[]> dbGet(String table) {
		try {
			String output = lteCli("db get " + table);
			return processTable(output);
		} catch (Exception e) {
			report.report(String.format("Couldn't get table %s from %s", table, getHardwareName()), Reporter.WARNING);
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Db get in matrix.
	 *
	 * @param table
	 *            the table
	 * @return the string[][]
	 */
	public String[][] dbGetInMatrix(String table) {
		try {
			String output = lteCli("db get " + table);
			String[] lines = output.split("\n");
			String[][] tableArr = normalizeTable(lines);
			return tableArr;
		} catch (Exception e) {
			report.report(String.format("Couldn't get table %s from %s", table, getHardwareName()), Reporter.WARNING);
			e.printStackTrace();
			return null;
		}

	}

	public String[][] dbGetInMatrix(String table, String row, String value) {
		try {
			String output = lteCli("db get " + table + " [" + value + "] " + row);
			String[] lines = output.split("\n");
			String[][] tableArr = normalizeTable(lines);
			return tableArr;
		} catch (Exception e) {
			report.report(String.format("Couldn't get table %s from %s", table, getHardwareName()), Reporter.WARNING);
			e.printStackTrace();
			return null;
		}

	}

	/**
	 * Process table- get a table output and turn it into hashtable key=column
	 * value=column values
	 *
	 * @param output
	 *            the output
	 * @return the hashtable
	 */
	private Hashtable<String, String[]> processTable(String output) {
		String[] lines = output.split("\n");
		String[][] tableArr = normalizeTable(lines);
		if (tableArr == null) {
			GeneralUtils.printToConsole("Couldn't process table output:\n " + output);
			return null;
		}
		Hashtable<String, String[]> tableResults = new Hashtable<String, String[]>();
		for (int i = 0; i < tableArr.length; i++) {
			String[] values = new String[tableArr[0].length - 1];
			for (int j = 0; j < values.length; j++)
				values[j] = tableArr[i][j + 1];
			tableResults.put(tableArr[i][0], values);
		}
		return tableResults;
	}

	/**
	 * Db set.
	 *
	 * @param table
	 *            the table
	 * @param key
	 *            the key
	 * @param row
	 *            the row
	 * @param value
	 *            the value
	 * @return true, if successful
	 */
	public boolean dbSet(String table, String key, String row, String value) {
		String output = "";
		for (int i = 0; i < 4; i++) {
			try {
				output = lteCli("db set " + table + " [" + key + "] " + row + "=" + value);
				if (output != null)
					break;
			} catch (Exception e) {
				report.report("Couldn't set the value " + value + " in table " + table);
			}
		}

		if (output == null)
			return false;

		if (output.contains("entries updated")) {
			return !output.contains("0 entries updated");
		} else
			return false;
	}

	public boolean dbSet(String table, String key, HashMap<String, String> KeyValuePairs) {
		String cmd = "db set " + table + " [" + key + "] ";

		for (Entry<String, String> entry : KeyValuePairs.entrySet()) {
			cmd += " " + entry.getKey() + "=" + entry.getValue();
		}
		String output = lteCli(cmd);
		return output.contains("entries updated");
	}

	/**
	 * Db add.
	 *
	 * @param table
	 *            the table
	 * @param Key1
	 *            the key
	 * @param Key2
	 *            the row
	 * @param values
	 *            the value
	 * @return true, if successful
	 */
	protected boolean dbAdd(String table, String Key1, String Key2, HashMap<String, String> values) {
		String cmd = "db add " + table + " [" + Key1 + "," + Key2 + "] ";
		for (Entry<String, String> entry : values.entrySet()) {
			cmd += entry.getKey() + "=" + entry.getValue() + " ";
		}
		String output = lteCli(cmd);
		if (!output.contains("1 entries updated")) {
			report.report(output, Reporter.FAIL);
			// new method for SNMP adding Neighbor
			return false;
		}
		return true;
	}

	/**
	 * getValFromMatrix getting the value from specific matrix with specific
	 * index and column.
	 *
	 * @param mat
	 * @param colName
	 * @return
	 */
	public String getValFromMatrix(String[][] mat, String colName) {

		for (int i = 0; i < mat.length; i++) {
			if (mat[i][0].equals(colName))
				return mat[i][1];
		}
		return null;
	}

	public String getValFromMatrix(String[][] mat, String colName, int row) {

		for (int i = 0; i < mat.length; i++) {
			if (mat[i][0].equals(colName))
				return mat[i][row];
		}
		return null;
	}

	/**
	 * Db delete.
	 *
	 * @param table
	 *            the table
	 * @param key
	 *            the key
	 * @return true, if successful
	 */
	public boolean dbDelete(String table, String key) {
		String output = "";
		int tries = 0;
		while (tries < 3) {
			output = lteCli(String.format("db delete %s [%s]", table, key));
			if (output.trim().equals("") || !(output.contains("entries deleted"))) {
				tries++;
			} else {
				tries = 3;
			}
		}
		return output.contains("entries deleted");
	}

	/**
	 * Normalize table. return a matrix which represent the db table [row][col]
	 *
	 * @param table
	 *            the table
	 * @return the string[][]
	 */
	private String[][] normalizeTable(String[] table) {
		// makes an array list which every cell contains array that represents
		// the values of
		// the row
		ArrayList<String[]> normlizedTable = new ArrayList<String[]>();
		for (String line : table)
			if (line.startsWith("|")) {
				String[] values = line.substring(1, line.length() - 1).replaceAll("\\|\\|", "\\|").split("\\|");
				for (int i = 0; i < values.length; i++)
					values[i] = values[i].trim();

				normlizedTable.add(values);
			}
		if (normlizedTable.size() == 0)
			return null;
		String[][] tableResult = normlizedTable.toArray(new String[][] {});
		// The table array need to be formated from a 2D array that is sorted by
		// rows and then
		// columns
		// to a 2D array that is sorted by columns and then rows.
		String[][] formatedTable = new String[tableResult[0].length][tableResult.length];
		try {
			for (int col = 0; col < tableResult[0].length; col++)
				for (int row = 0; row < tableResult.length; row++)
					formatedTable[col][row] = tableResult[row][col];
		} catch (Exception e) {
			e.printStackTrace();
		}
		return formatedTable;
	}

	/**
	 * Gets the logger thresholds table.
	 *
	 * @return the logger thresholds table
	 * @throws IOException
	 * @throws NumberFormatException
	 */
	public Hashtable<String, String[]> getLoggerThresholdsTable() throws NumberFormatException, IOException {
		if (loggerThresholds == null) {
			if (getVersion().contains(MIN_XLP_VER_WITH_LOGGER_THRESHOLD_CHANGE)
					&& Integer.parseInt(getVersion().substring(MIN_XLP_VER_WITH_LOGGER_THRESHOLD_CHANGE.length())) >= 6)
				this.loggerThresholds = processTable(lteCli("logger threshold get"));
			else
				this.loggerThresholds = dbGet("loggerThresholds");
		}

		return loggerThresholds;
	}

	/**
	 * Lte cli.
	 *
	 * @param command
	 *            the command
	 * @return the string
	 */
	public String lteCli(String command) {
		return lteCliWithResponse(command, "");
	}

	/**
	 * Lte cli.
	 *
	 * @param command
	 *            the command
	 * @param response
	 *            the response to wait for
	 * @return the string
	 */
	public String lteCliWithResponse(String command, String response) {
		String ans = sendCommands(LTE_CLI_PROMPT, command, response);
		return ans == null ? "" : ans;
	}

	/**
	 * Gets the phy state.
	 *
	 * @return the phy state
	 */
	@IgnoreMethod
	public PhyState getPhyState() {
		String output = lteCli("pal get phystate");
		String phyStateIndicator = "Phy state :";
		if (output.contains(phyStateIndicator)) {
			output = output.substring(output.indexOf(phyStateIndicator) + phyStateIndicator.length());
			String state = output.substring(0, output.indexOf("\n")).trim();
			PhyState phyState = PhyState.valueOf(state);
			if (phyState != null)
				return phyState;
			else if (state.contains("THIS CLI COMMAND IS NOT ALLOWED BEFORE ALL RUNNING"))
				return PhyState.disconnected;
			else
				GeneralUtils.printToConsole(String.format(
						"Unrecognized phy state \"%s\". Please tell automation programmer to add this state if needed.\n Setting phy state to \"Unresponsive\".",
						state));
		}

		return PhyState.unresponsive;
	}

	/**
	 * Update operational status.
	 *
	 * @throws Exception
	 */
	@IgnoreMethod
	public boolean isInOperationalStatus() {
		if (!isReachable()) {
			return false;
		}
		int tryouts = 0;
		EnbStates service = EnbStates.UNKNOWN;
		EnbStates running = EnbStates.UNKNOWN;
		while ((service == EnbStates.UNKNOWN || running == EnbStates.UNKNOWN) && tryouts < 3) {
			tryouts++;
			service = getServiceState();
			running = getRunningState();
		}
		if (service == EnbStates.UNKNOWN || running == EnbStates.UNKNOWN) {
			GeneralUtils.printToConsole("Could not get OperationalStatus from EnodeB (after 3 retries)");
			return false;
		}
		return service.equals(EnbStates.IN_SERVICE) && running.equals(EnbStates.IN_SERVICE);
	}

	/**
	 * (non-Javadoc)
	 *
	 * @see EnodeB.Components.EnodeBComponent#setSessionLogLevel(java.lang.String,
	 *      int)
	 */
	@Override
	public void setSessionLogLevel(String sessionName, int level) {
		sendCommandsOnSession(sessionName, LTE_CLI_PROMPT,
				"logger threshold set client=* process=* cli=" + String.valueOf(level), LTE_CLI_PROMPT);
	}

	public void setSessionLogLevel(String client, String process, int level) {
		sendCommandsOnSession(SessionManager.SSH_SESSION_NAME, LTE_CLI_PROMPT, String
				.format("logger threshold set client=%s process=%s cli=%s", client, process, String.valueOf(level)),
				LTE_CLI_PROMPT);
	}

	/**
	 * set SSH Session Log Level - cli
	 *
	 * @see EnodeB.Components.EnodeBComponent#setSessionLogLevel
	 * @param sessionName
	 *            the session name
	 * @param client
	 *            - client
	 * @param process
	 *            - process
	 * @param level
	 *            the level
	 */
	@Override
	public void setSessionLogLevel(String sessionName, String client, String process, int level) {
		sendCommandsOnSession(sessionName, LTE_CLI_PROMPT, String
				.format("logger threshold set client=%s process=%s cli=%s", client, process, String.valueOf(level)),
				LTE_CLI_PROMPT);
	}

	/**
	 * Gets the hardware name.
	 *
	 * @return the hardware name
	 */
	@IgnoreMethod
	public String getHardwareName() {
		return hardwareName;
	}

	/**
	 * Sets the hardware name.
	 *
	 * @param hardwareName
	 *            the new hardware name
	 */
	@IgnoreMethod
	public void setHardwareName(String hardwareName) {
		this.hardwareName = hardwareName;
	}

	public boolean addNbr(EnodeB enodeB, EnodeB neighbor, HoControlStateTypes hoControlStatus,
			X2ControlStateTypes x2ControlStatus, HandoverType HandoverType, boolean isStaticNeighbor,
			String qOffsetRange) throws IOException {
		// Initialize qOffSet, in case the user doesn't want to fill it (It's
		// optional)
		int qOffSet = 0;
		// create double valued instance.
		String mnoBroadcastPlmn = neighbor.calculateMnoBroadcastPlmn();
		String eutranCellId = neighbor.calculateEutranCellId();
		String instance = mnoBroadcastPlmn + "." + eutranCellId;

		// create OIDs
		MibReader reader = MibReader.getInstance();

		String plnm1Oid = reader.resolveByName("asLteStkCmdQNghListMvnoBroadcastPlmn1");
		String plnm2Oid = reader.resolveByName("asLteStkCmdQNghListMvnoBroadcastPlmn2");
		String plnm3Oid = reader.resolveByName("asLteStkCmdQNghListMvnoBroadcastPlmn3");
		String plnm4Oid = reader.resolveByName("asLteStkCmdQNghListMvnoBroadcastPlmn4");
		String plnm5Oid = reader.resolveByName("asLteStkCmdQNghListMvnoBroadcastPlmn5");
		String enbTypeOid = reader.resolveByName("asLteStkCmdQNghListEnbType");
		String dlEarfcnOid = reader.resolveByName("asLteStkCmdQNghListDlEarfcn");
		String enbIPOid = reader.resolveByName("asLteStkCmdQNghListEnbIpAddr");
		String pciOid = reader.resolveByName("asLteStkCmdQNghListPci");
		String tacOid = reader.resolveByName("asLteStkCmdQNghListTac");
		String x2TrigHoOid = reader.resolveByName("asLteStkCmdQNghListTriggerX2Ho");
		String qOffOid = reader.resolveByName("asLteStkCmdQNghListQOffsetCell");
		String notRemoveOid = reader.resolveByName("asLteStkCmdQNghListNotRemovable");
		String X2CtrlOid = reader.resolveByName("asLteStkCmdQNghListX2ControlState");
		String HoCtrlOid = reader.resolveByName("asLteStkCmdQNghListHoControlState");
		String cellMaskOid = reader.resolveByName("asLteStkCmdQNghListCellMask");
		String cmdFlagOid = reader.resolveByName("asLteStkCmdQNghListCmdFlag");
		String rowStatOid = reader.resolveByName("asLteStkCmdQNghListRowStatus");
		String cellIndiviOid = reader.resolveByName("asLteStkCmdQNghListCellIndividualOffset");

		// parse parameters
		String ipAdd = neighbor.getIpAddress();
		int neighborEarfcn = neighbor.getEarfcn();
		int x2TrigHo = Integer.valueOf(HandoverType.convertEnum());
		if (qOffsetRange != null) {
			qOffSet = Integer.valueOf(qOffsetRange);
		}
		int x2Ctrl = Integer.valueOf(x2ControlStatus.convertEnum());
		int hoCtrl = Integer.valueOf(hoControlStatus.convertEnum());
		int myInt = (isStaticNeighbor) ? 1 : 0;
		int tac = Integer.valueOf(getTac(neighbor));
		int pci = neighbor.getPci();

		// snmp adding Neighbor
		provisionStartEvent();
		snmp.snmpSet(plnm1Oid + "." + instance, 0);
		snmp.snmpSet(plnm2Oid + "." + instance, 0);
		snmp.snmpSet(plnm3Oid + "." + instance, 0);
		snmp.snmpSet(plnm4Oid + "." + instance, 0);
		snmp.snmpSet(plnm5Oid + "." + instance, 0);
		snmp.snmpSet(enbTypeOid + "." + instance, 0);
		snmp.snmpSet(dlEarfcnOid + "." + instance, neighborEarfcn);
		snmp.snmpSet(enbIPOid + "." + instance, ipAdd);
		snmp.snmpSet(pciOid + "." + instance, pci);
		snmp.snmpSet(tacOid + "." + instance, tac);
		snmp.snmpSet(x2TrigHoOid + "." + instance, x2TrigHo);
		if (qOffsetRange != null) {
			snmp.snmpSet(qOffOid + "." + instance, qOffSet);
		}
		snmp.snmpSet(notRemoveOid + "." + instance, myInt);
		snmp.snmpSet(X2CtrlOid + "." + instance, x2Ctrl);
		snmp.snmpSet(HoCtrlOid + "." + instance, hoCtrl);
		snmp.snmpSet(cellMaskOid + "." + instance, 1);
		snmp.snmpSet(cmdFlagOid + "." + instance, 0);
		snmp.snmpSet(rowStatOid + "." + instance, 1);
		snmp.snmpSet(cellIndiviOid + "." + instance, 15);
		provisionEndedEvent();
		return true;

	}

	private boolean edit3PartyNbrs(int numberOfNbr, final long mnoBroadcastPlmn, long startEutranCellId,
			int zeroForAddOneForRemove, Integer enbType_optional, Integer qOffsetCell_optional) throws IOException {
		if (zeroForAddOneForRemove != 1 && zeroForAddOneForRemove != 0) {
			return false;
		} else {
			String cmdFlagOid = MibReader.getInstance().resolveByName("asLteStkCmdQNghListCmdFlag");
			String rowStatOid = MibReader.getInstance().resolveByName("asLteStkCmdQNghListRowStatus");
			String enbTypeOid = MibReader.getInstance().resolveByName("asLteStkCmdQNghListEnbType");
			String qOffsetCellOid = MibReader.getInstance().resolveByName("asLteStkCmdQNghListQOffsetCell");

			cmdFlagOid += ("." + mnoBroadcastPlmn + ".");
			rowStatOid += ("." + mnoBroadcastPlmn + ".");
			enbTypeOid += ("." + mnoBroadcastPlmn + ".");
			qOffsetCellOid += ("." + mnoBroadcastPlmn + ".");

			if (numberOfNbr <= 0 || startEutranCellId <= 0) {
				throw new IOException("Non positive number!");
			}

			provisionStartEvent();
			for (long instance = startEutranCellId; instance < (startEutranCellId + numberOfNbr); instance++) {
				snmp.snmpSet(cmdFlagOid + "." + instance, zeroForAddOneForRemove);
				snmp.snmpSet(rowStatOid + "." + instance, 1);

				if (enbType_optional != null) {
					snmp.snmpSet(enbTypeOid + "." + instance, enbType_optional);
				}
				if (qOffsetCell_optional != null) {
					snmp.snmpSet(qOffsetCellOid + "." + instance, qOffsetCell_optional);
				}

				if (instance == Long.MAX_VALUE && Long.MAX_VALUE != (startEutranCellId + numberOfNbr)) {
					report.report("EutranCellId greater than Long.", Reporter.WARNING);
					break;
				}
			}
			provisionEndedEvent();
			return true;
		}
	}

	public boolean add3PartyNbrs(int numberOfNbr, final long mnoBroadcastPlmn, long startEutranCellId,
			Integer enbType_optional, Integer qOffsetCell_optional) throws IOException {
		return edit3PartyNbrs(numberOfNbr, mnoBroadcastPlmn, startEutranCellId, 0, enbType_optional,
				qOffsetCell_optional);
	}

	public boolean remove3PartyNbrs(int numberOfNbr, final long mnoBroadcastPlmn, long startEutranCellId)
			throws IOException {
		return edit3PartyNbrs(numberOfNbr, mnoBroadcastPlmn, startEutranCellId, 1, null, null);
	}

	private String getTac(EnodeB enb) throws IOException {
		String myTac = "";
		String tac = MibReader.getInstance().resolveByName("asLteStkCellCfgTac");
		try {
			myTac = enb.getSNMP(tac);
		} catch (Exception e) {
			report.report("Couldn't get TAC", Reporter.WARNING);
			e.printStackTrace();
		}
		return myTac;
	}

	protected HashMap<String, String> enbShowNgh() {

		HashMap<String, String> values = new HashMap<>();
		try {
			String output = lteCli("enb show ngh");
			Pattern r = Pattern.compile("ECI\\s*(\\d*)\\s*EARFCN\\s*(\\d*)\\s*PCI\\s*(\\d*)");
			Matcher m = r.matcher(output);
			if (m.find()) {
				values.put("ECI", m.group(1));
				values.put("DlEarfc", m.group(2));
				values.put("PCI", m.group(3));
				return values;
			} else
				return null;
		} catch (Exception e) {
			report.report(String.format("Couldn't get table ngh from %s", getHardwareName()), Reporter.WARNING);
			e.printStackTrace();
			return null;
		}

	}

	public boolean verifyAnrNeighbor(EnodeB neighbor) {
		String oid = MibReader.getInstance().resolveByName("asLteStkNghListDiscoveredByAnr");

		HashMap<String, Variable> anrValues = snmp.SnmpWalk(oid);

		if (anrValues == null) {
			report.report("Cannot get Neighbor table Using SNMP", Reporter.WARNING);
			return false;
		}

		updatePLMNandEutranCellID(neighbor);

		String fullInstance = mnoBroadcastPlmn.trim() + "." + eutranCellId.trim();
		String output = oid + "." + fullInstance;
		GeneralUtils.printToConsole("'PLMN' and 'Eutran Cell ID' values: " + fullInstance);
		return anrValues.containsKey(output);

	}

	public String getTriggerX2Ho(EnodeB neighbor) {
		String oid = MibReader.getInstance().resolveByName("asLteStkNghListTriggerX2Ho");

		updatePLMNandEutranCellID(neighbor);

		String fullInstance = mnoBroadcastPlmn.trim() + "." + eutranCellId.trim();
		String output = oid + "." + fullInstance;
		String ans = "";
		try {
			ans = snmp.get(output);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return ans;
	}

	public boolean deleteNeighborBySNMP(EnodeB neighbor) throws IOException {
		// create double valued instance
		updatePLMNandEutranCellID(neighbor);
		String instance = mnoBroadcastPlmn + "." + eutranCellId;
		// get all OIDs
		String cmdFlagOid = MibReader.getInstance().resolveByName("asLteStkCmdQNghListCmdFlag");
		String rowStatOid = MibReader.getInstance().resolveByName("asLteStkCmdQNghListRowStatus");
		// snmp to delete Neighbor
		provisionStartEvent();
		snmp.snmpSet(cmdFlagOid + "." + instance, 1);
		snmp.snmpSet(rowStatOid + "." + instance, 1);
		provisionEndedEvent();

		return true;
	}

	public boolean deleteAllNeighborsByCli() {
		return dbDelete("nghList", "*");
	}

	public String[] getNeighbors() {
		Hashtable<String, String[]> nghList = dbGet("nghList");
		if (nghList != null)
			return nghList.get("enbIpAddr.address");
		else {
			GeneralUtils.printToConsole("Couldn't get table \"nghList\"");
			return null;
		}
	}

	public boolean verifyNbrList(EnodeB enodeB, EnodeB neighbor, HoControlStateTypes hoControlStatus,
			X2ControlStateTypes x2ControlStatus, HandoverType HandoverType, boolean isStaticNeighbor,
			String qOffsetRange) throws IOException {
		boolean wasAdded = true;
		boolean wasFound = false;
		String oidPlmn = MibReader.getInstance().resolveByName("asLteStkNghListDlEarfcn");

		HashMap<String, Variable> plmnValues = snmp.SnmpWalk(oidPlmn);

		if (plmnValues == null) {
			report.report("Cannot get Neighbor table Using SNMP", Reporter.WARNING);
			return false;
		}
		updatePLMNandEutranCellID(neighbor);

		String fullInstance = mnoBroadcastPlmn.trim() + "." + eutranCellId.trim();
		String output = oidPlmn + "." + fullInstance;
		if (plmnValues.containsKey(output)) {
			report.report("EnodeB: " + neighbor.getNetspanName() + " was found in the " + enodeB.getNetspanName()
					+ " neighbor's list");
			wasFound = true;
		}
		if (wasFound) {
			String HOStateOID = MibReader.getInstance().resolveByName("asLteStkNghListHoControlState");
			String COStateOID = MibReader.getInstance().resolveByName("asLteStkNghListX2ControlState");
			String HOtypeOID = MibReader.getInstance().resolveByName("asLteStkNghListTriggerX2Ho");
			String StaticOID = MibReader.getInstance().resolveByName("asLteStkNghListNotRemovable");
			String qoffsetOID = MibReader.getInstance().resolveByName("asLteStkNghListQOffsetCell");

			String HOstate = "";
			String Costate = "";
			String HOtype = "";
			String StaticState = "";
			boolean Static = false;
			String qoffset = "";

			for (int i = 0; i < 3; i++) {
				HOstate = snmp.get(HOStateOID + "." + fullInstance).trim();
				if (!HOstate.equals("")) {
					break;
				}
			}
			for (int i = 0; i < 3; i++) {
				Costate = snmp.get(COStateOID + "." + fullInstance).trim();
				if (!Costate.equals("")) {
					break;
				}
			}
			for (int i = 0; i < 3; i++) {
				HOtype = snmp.get(HOtypeOID + "." + fullInstance).trim();
				if (!HOtype.equals("")) {
					break;
				}
			}
			for (int i = 0; i < 3; i++) {
				StaticState = snmp.get(StaticOID + "." + fullInstance).trim();
				if (!StaticState.equals("")) {
					Static = StaticState.equals("1") ? true : false;
					break;
				}
			}
			for (int i = 0; i < 3; i++) {
				qoffset = snmp.get(qoffsetOID + "." + fullInstance).trim();
				if (!qoffset.equals("")) {
					break;
				}
			}

			if (null != hoControlStatus) {
				if (HOstate.equals("")) {
					report.report("Ho Control parameter is empty", Reporter.WARNING);
				} else if (!HOstate.equals(hoControlStatus.convertEnum())) {
					report.report("Ho Control State is " + HOstate + " and not " + hoControlStatus.convertEnum()
							+ " as expected", Reporter.WARNING);
					wasAdded = false;
				} else {
					report.report("Ho Control State is " + hoControlStatus + " as expected");
				}
			}

			if (null != x2ControlStatus) {
				if (Costate.equals("")) {
					report.report("X2 Control parameter is empty", Reporter.WARNING);
				} else if (!Costate.equals(x2ControlStatus.convertEnum())) {
					report.report("X2 Control State is " + Costate + " and not " + x2ControlStatus.convertEnum()
							+ " as expected", Reporter.WARNING);
					wasAdded = false;
				} else {
					report.report("X2 Control Type is " + x2ControlStatus + " as expected");
				}
			}

			if (null != HandoverType) {
				if (HOtype.equals("")) {
					report.report("Handover Type parameter is empty", Reporter.WARNING);
				} else if (!HOtype.equals(HandoverType.convertEnum())) {
					report.report(
							"Handover Type is " + HOtype + " and not " + HandoverType.convertEnum() + " as expected",
							Reporter.WARNING);
					wasAdded = false;
				} else {
					report.report("Handover Type is " + HandoverType + " as expected");
				}
			}

			if (StaticState.equals("")) {
				report.report("Static neighbor parameter is empty", Reporter.WARNING);
			} else if (Static != isStaticNeighbor) {
				report.report("Static Neighbor value is " + Static + " and not " + String.valueOf(isStaticNeighbor)
						+ " as expected", Reporter.WARNING);
				wasAdded = false;
			} else {
				report.report("Static Neighbor value is " + Static + " as expected");
			}

			if (null != qOffsetRange) {
				if (qoffset.equals("")) {
					report.report("Q-Offset parameter is empty", Reporter.WARNING);
				} else if (!qoffset.equals(qOffsetRange)) {
					report.report("Q-Offset value is " + qoffset + " and not " + qOffsetRange + " as expected",
							Reporter.WARNING);
					wasAdded = false;
				} else {
					report.report("Q-Offset value is " + qoffset + " as expected");
				}
			}
		}
		if (!wasFound) {
			report.report("Couldn't find the neighbor " + neighbor.getNetspanName() + " in the "
					+ enodeB.getNetspanName() + " neighbor list", Reporter.WARNING);
			report.startLevel("Debug info (Walk on asLteStkNghList indexes)");
			for (String keys : plmnValues.keySet()) {
				report.report(keys);
			}
			report.stopLevel();
			wasAdded = false;
		}
		return wasAdded;
	}

	public boolean verifyNbrList(EnodeB neighbor) throws IOException {
		String oidPlmn = MibReader.getInstance().resolveByName("asLteStkNghListDlEarfcn");

		HashMap<String, Variable> plmnValues = snmp.SnmpWalk(oidPlmn);

		if (plmnValues == null) {
			report.report("Cannot get Neighbor table Using SNMP", Reporter.WARNING);
			return false;
		}

		updatePLMNandEutranCellID(neighbor);

		String fullInstance = mnoBroadcastPlmn.trim() + "." + eutranCellId.trim();
		String output = oidPlmn + "." + fullInstance;
		if (plmnValues.containsKey(output)) {
			report.report("Neighbor: " + neighbor.getName() + " was found in EnodeB: " + getName() + " list");
			return true;
		}

		return false;
	}

	public boolean verifyNoNeighbors() {
		return dbGetInMatrix("nghList") == null;
	}

	public void updatePLMNandEutranCellID(EnodeB neighbor) {

		mnoBroadcastPlmn = neighbor.calculateMnoBroadcastPlmn();
		eutranCellId = neighbor.calculateEutranCellId();
	}

	public int getUeNumberToEnb() {
		int lowNumber = -1; // asLteStatsOnDemandConnectedUeSum
		String oid = MibReader.getInstance().resolveByName("asLteStatsOnDemandConnectedUeSum");
		HashMap<String, org.snmp4j.smi.Variable> output = snmp.SnmpWalk(oid);
		for (org.snmp4j.smi.Variable var : output.values()) {
			if (var.toInt() > lowNumber && var != null) {
				lowNumber = var.toInt();
			}
		}
		return lowNumber;
	}

	public boolean setANRState(SonAnrStates anrState) {
		String oidEnable = MibReader.getInstance().resolveByName("asLteStkCellAnrCfgEnable");
		String oidState = MibReader.getInstance().resolveByName("asLteStkCellAnrCfgAnrState");

		if (oidEnable.trim().isEmpty() || oidState.trim().isEmpty())
			return false;

		switch (anrState) {
		case DISABLED:
			snmp.snmpSet(oidEnable, 0);
			return true;

		case HO_MEASUREMENT:
			snmp.snmpSet(oidEnable, 1);
			snmp.snmpSet(oidState, 1);
			return true;
		case PERIODICAL_MEASUREMENT:
			snmp.snmpSet(oidEnable, 1);
			snmp.snmpSet(oidState, 2);
			return true;
		default:
			return false;
		}
	}

	public boolean downloadSWSnmp(String fileToDownload, ServerProtocolType downloadType, String user,
			String password) {
		String ServerAddrTypeOID = MibReader.getInstance().resolveByName("asMaxBsCmSwCfgFileServerAddrType") + "."
				+ SWTypeInstnace;
		String ServerAddrOID = MibReader.getInstance().resolveByName("asMaxBsCmSwCfgFileServerAddr") + "."
				+ SWTypeInstnace;
		String fileNameOID = MibReader.getInstance().resolveByName("asMaxBsCmSwCfgFileToInstall") + "."
				+ SWTypeInstnace;
		String commandOID = MibReader.getInstance().resolveByName("asMaxBsCmSwCfgCommand") + "." + SWTypeInstnace;
		try {
			boolean state = false;
			for (int i = 0; i < 5; i++) {
				state = setServerTypeForUpgrade(downloadType, user, password);
				if (state)
					break;
			}
			if (!state) {
				report.report(getName() + " - Server Type set failed", Reporter.WARNING);
				return false;
			}

			if (!snmp.snmpSet(ServerAddrTypeOID, 1)) {
				report.report(getName() + " - The download initialize process failed setting ipv4", Reporter.WARNING);
				return false;
			}

			byte[] finalBytesArr = InetAddressesHelper.convertIPStringTo15ByteArr(upgradeServerIp);
			if (!snmp.snmpSet(ServerAddrOID, finalBytesArr)) {
				report.report(getName() + " - The download initialize process failed while setting the server ip",
						Reporter.WARNING);
				return false;
			}

			if (!snmp.snmpSet(fileNameOID, fileToDownload)) {
				report.report(getName() + " - The download initialize process failed while setting the file name",
						Reporter.WARNING);
				return false;
			}

			if (!snmp.snmpSet(commandOID, 2)) {
				GeneralUtils.printToConsole("Download proc has failed on " + this.getIpAddress());
				report.report(getName() + " - The download initialize process failed while executing the SNMP command",
						Reporter.WARNING);
				return false;
			}
		} catch (Exception e) {
			report.report("Thread Error", Reporter.WARNING);
			return false;
		}
		report.report(getName() + " - The download initialize process passed successfully", Reporter.PASS);
		return true;
	}

	public boolean downloadSWCli(String fileToInstall, ServerProtocolType downloadType, String username,
			String password) {
		String changeDir = "cd /bs/bin";
		String credentials = "";
		if (downloadType == ServerProtocolType.SFTP) {
			credentials = username + " " + password;
		}
		String downloadCommand = "./software_upgrade.sh " + downloadType.toString().toLowerCase() + " "
				+ upgradeServerIp + " " + fileToInstall + " " + credentials;
		report.report(getName() + " - Donwloading version via CLI script: " + downloadCommand);
		shell(changeDir);
		shell(downloadCommand);
		return true;
	}

	private boolean setServerTypeForUpgrade(ServerProtocolType downloadType, String user, String password)
			throws IOException {
		String serverTypeOID = MibReader.getInstance().resolveByName("asMaxBsCmSwServerType") + "." + SWTypeInstnace;
		String serverPasswordOID = MibReader.getInstance().resolveByName("asMaxBsCmSwServerPassword") + "."
				+ SWTypeInstnace;
		String serverUserOID = MibReader.getInstance().resolveByName("asMaxBsCmSwServerUserName") + "."
				+ SWTypeInstnace;
		boolean state = true;
		switch (downloadType) {
		case FTP:
			state &= snmp.snmpSet(serverTypeOID, 1);
		case SFTP:
			state &= snmp.snmpSet(serverTypeOID, 3);
			state &= snmp.snmpSet(serverPasswordOID, password);
			state &= snmp.snmpSet(serverUserOID, user);
			break;
		case TFTP:
			state &= snmp.snmpSet(serverTypeOID, 2);
			state &= snmp.snmpSet(serverPasswordOID, "anonymous");
			state &= snmp.snmpSet(serverUserOID, "anonymous");
			break;
		default:
			state = false;
		}
		return state;
	}

	public boolean downloadStats() {
		boolean notHundredButIdle = true;
		String precentOid = MibReader.getInstance().resolveByName("asMaxBsCmSwStatusProgress") + "." + SWTypeInstnace;
		String statusCodeOid = MibReader.getInstance().resolveByName("asMaxBsCmSwStatusCode") + "." + SWTypeInstnace;
		String statusStringOid = MibReader.getInstance().resolveByName("asMaxBsCmSwStatusString") + "."
				+ SWTypeInstnace;

		// waiting for Done in the download
		// long timeout = EnodeB.DOWNLOAD_TIMEOUT; // 30 minutes
		long startTime = System.currentTimeMillis();
		while (System.currentTimeMillis() - startTime <= EnodeB.DOWNLOAD_TIMEOUT) {
			String precent = "", downloadStatus, statusString = "";

			precent = snmp.get(precentOid).trim();
			downloadStatus = snmp.get(statusCodeOid).trim();

			// Download Done
			if (precent.equals("100") && downloadStatus.equals(swStatusIdle)) {
				report.report(getName() + " - The download process passed successfully via SNMP", Reporter.PASS);
				return true;
			}

			if (downloadStatus.equals(swStatusInstallFailure)) {

				statusString = snmp.get(statusStringOid).trim();
				report.report(getName() + " - Got bad state from SNMP: State= " + downloadStatus + " Status String= "
						+ statusString, Reporter.WARNING);
				return false;

			}

			if (!precent.equals("100") && downloadStatus.equals(swStatusIdle) && !precent.trim().equals("")) {
				if (notHundredButIdle) {
					notHundredButIdle = false;
					GeneralUtils.unSafeSleep(10 * 1000);
					continue;
				}
				report.report(
						getName()
								+ " - Software download failed while in process Download Precent was != 100 but status is Idle",
						Reporter.WARNING);
				return false;

			}
			GeneralUtils.unSafeSleep(5 * 1000);
		}
		report.report(getName() + " - Software download failed due to timeout", Reporter.WARNING);
		return false;
	}

	public Pair<Boolean, SwStatus> isSoftwareDownloadCompletedSuccessfully() {

		String precentOid = MibReader.getInstance().resolveByName("asMaxBsCmSwStatusProgress") + "." + SWTypeInstnace;
		String statusCodeOid = MibReader.getInstance().resolveByName("asMaxBsCmSwStatusCode") + "." + SWTypeInstnace;
		String statusStringOid = MibReader.getInstance().resolveByName("asMaxBsCmSwStatusString") + "."
				+ SWTypeInstnace;

		String precent = "", downloadStatus = "", statusString = "";

		precent = snmp.get(precentOid).trim();
		downloadStatus = snmp.get(statusCodeOid).trim();

		SwStatus swStatus = null;
		try {
			swStatus = SwStatus.fromValue(downloadStatus);
		} catch (IllegalArgumentException e) {
			swStatus = SwStatus.SW_STATUS_ILLEGAL_VALUE;
		}

		GeneralUtils.printToConsole(getName() + ": " + precent + "% downloaded");
		GeneralUtils.printToConsole(getName() + ": " + "Download Status: " + swStatus);

		Pair<Boolean, SwStatus> result = null;
		if ((swStatus == SwStatus.SW_STATUS_ACTIVATION_IN_PROGRESS)
				|| (precent.equals("100") && downloadStatus.equals(swStatusIdle))) {
			report.report(getName() + " - The download process passed successfully via SNMP", Reporter.PASS);
			result = new Pair<Boolean, SwStatus>(true, swStatus);
		} else if ((swStatus == SwStatus.SW_STATUS_ACTIVATION_FAILURE)
				|| (swStatus == SwStatus.SW_STATUS_INSTALL_FAILURE)) {
			statusString = snmp.get(statusStringOid).trim();
			report.report(
					getName() + " - Got bad state from SNMP: State= " + swStatus + " Status String= " + statusString,
					Reporter.WARNING);
			result = new Pair<Boolean, SwStatus>(false, swStatus);
		} else if (!precent.equals("100") && swStatus == SwStatus.SW_STATUS_IDLE
				&& !precent.trim().equals(String.valueOf(GeneralUtils.ERROR_VALUE))) {
			report.report(
					getName()
							+ " - Software download failed while in process Download Precent was != 100 but status is Idle",
					Reporter.WARNING);
			result = new Pair<Boolean, SwStatus>(false, swStatus);
		} else {
			result = new Pair<Boolean, SwStatus>(false, swStatus);
		}

		return result;
	}

	public String getSecondaryVersion() throws IOException {
		String oid = MibReader.getInstance().resolveByName("asMaxBsCmSwSecondaryVersion") + "." + SWTypeInstnace;
		return snmp.get(oid);
	}

	public String getPrimaryVersion() throws IOException {
		String oid = MibReader.getInstance().resolveByName("asMaxBsCmSwPrimaryVersion") + "." + SWTypeInstnace;
		return snmp.get(oid);
	}
	
	public String getStandbyVersion() throws IOException {
		String bank0 = getPrimaryVersion();
		String bank1 = getSecondaryVersion();
		String running = getRunningVersion();
		return running.equals(bank1) ? bank0 : bank1;
	}

	/**
	 * get Mcc \ get Mnc
	 *
	 * @param mccOrMnc
	 *            - String "Mcc" or "Mnc"
	 * @return - String
	 */
	public String getMncMcc(String mccOrMnc) {
		String oid = MibReader.getInstance().resolveByName("asLteStkPlmnCfg" + mccOrMnc);
		String outputFromSNMP;
		String result = "";
		try {
			outputFromSNMP = snmp.get(oid + ".1").trim();
		} catch (Exception e) {
			report.report("Failed getting " + mccOrMnc, Reporter.WARNING);
			return String.valueOf(GeneralUtils.ERROR_VALUE);

		}
		String[] hexArray = outputFromSNMP.split(":");
		// Checking noSuchObject before casting the String to Integer.
		if (!GeneralUtils.isArrayMadeOfIntegers(hexArray)) {
			report.report("Failed getting " + mccOrMnc, Reporter.WARNING);
			return String.valueOf(GeneralUtils.ERROR_VALUE);
		}
		for (String aHexArray : hexArray) {
			if (aHexArray != String.valueOf(GeneralUtils.ERROR_VALUE) && Integer.parseInt("0" + aHexArray, 16) != 0)
				result += (char) Integer.parseInt(aHexArray, 16);
		}
		return result;
	}

	/**
	 * get Mcc
	 *
	 * @return - Mcc
	 */
	public String getMcc() {
		return getMncMcc("Mcc");
	}

	/**
	 * get Mnc
	 *
	 * @return - Mnc
	 */
	public String getMnc() {
		return getMncMcc("Mnc");
	}

	public String getCellIdentity() {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellCfgCellIdentity");
		String output = "-999";
		output = snmp.get(oid).trim();
		return output;
	}

	public boolean setOperationalStatus(EnbStates enbState) {
		String oid = MibReader.getInstance().resolveByName("asLteStkStackCfgOperationalStatus");
		try {
			if (enbState == EnbStates.IN_SERVICE) {
				return snmp.snmpSet(oid, 1);
			} else {
				return snmp.snmpSet(oid, 2);
			}
		} catch (Exception e) {
			report.report("Can't send command snmp protocol");
			e.printStackTrace();
			return false;
		}
	}

	public String getOperationalStatus() {
		String oid = MibReader.getInstance().resolveByName("asLteStkStackCfgOperationalStatus");
		try {
			return snmp.get(oid);
		} catch (Exception e) {
			report.report("Error getting operational status");
			e.printStackTrace();
		}
		return String.valueOf(GeneralUtils.ERROR_VALUE);
	}

	/**
	 * get Bandwidth via snmp
	 *
	 * @return bandwidth in Mhps
	 * @throws IOException
	 * @author Shahaf Shuhmay
	 */
	public String getBandWidthSnmp() {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellCfgChannelBandwidth");
		String output = GeneralUtils.ERROR_VALUE + "";
		try {
			output = snmp.get(oid);
		} catch (Exception e) {
			report.report("getBandWidthSnmp failed due to: " + e.getMessage(), Reporter.WARNING);
			e.printStackTrace();
		}
		return output;
	}

	/**
	 * get CFi via snmp
	 *
	 * @return CFI in int
	 * @throws IOException
	 * @author Shahaf Shuhmay
	 */
	public String getCFISnmp() {
		String oid = MibReader.getInstance().resolveByName("asLteStkMacCfgControlFormatIndicator");
		// String oid = "1.3.6.1.4.1.989.1.20.1.4.10.1.25.40";
		try {
			return snmp.get(oid);
		} catch (Exception e) {
			report.report("getCFISnmp failed due to: " + e.getMessage(), Reporter.WARNING);
			e.printStackTrace();
			return String.valueOf(GeneralUtils.ERROR_VALUE);
		}
	}

	/**
	 * get duplex mode via snmp
	 *
	 * @return
	 * @throws IOException
	 * @author Shahaf Shuhamy
	 */
	public String getDuplexModeSnmp() throws IOException {
		// No MIB for this, so getting it according to band number
		if (getBand() >= 33 && getBand() <= 51) {
			return "TDD";
		}
		return "FDD";
	}

	/**
	 * gets Special Sub Frame
	 *
	 * @return
	 * @throws IOException
	 * @author Shahaf Shuhamy
	 */
	public String getSpecialSubFrameSnmp() throws IOException {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellSib1CfgSsfPattern");
		return snmp.get(oid);
	}

	/**
	 * get Frame Configuration based on Snmp
	 *
	 * @return
	 * @throws IOException
	 * @author Shahaf Shuhamy
	 */
	public String getFrameConfig() {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellSib1TddSubframeAssignmnent");
		String output = GeneralUtils.ERROR_VALUE + "";
		try {
			output = snmp.get(oid);
		} catch (Exception e) {
			report.report("getFrameConfig failed due to: " + e.getMessage(), Reporter.WARNING);
			e.printStackTrace();
		}
		return output;
	}

	public static void setUpgradeServerIp() throws Exception {
		EnodeBUpgradeServer enodeBUpgradeServer = (EnodeBUpgradeServer) SystemManagerImpl.getInstance()
				.getSystemObject("UpgradeServer");
		upgradeServerIp = enodeBUpgradeServer.getUpgradeServerIp();

	}

	public String getRunningVersion() {
		String oid = MibReader.getInstance().resolveByName("asMaxBsCmSwRunningVersion") + "." + SWTypeInstnace;
		String output;
		output = snmp.get(oid).trim();
		return output;
	}

	/**
	 * get Eval Period based on Snmp
	 *
	 * @return
	 * @throws IOException
	 * @author Dor Shalom
	 */
	public int getEvalPeriod() {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellAnrCfgEvalPeriod");
		String output = "";
		try {
			output = snmp.get(oid);
		} catch (Exception e) {
			report.report("getEvalPeriod failed due to: " + e.getMessage(), Reporter.WARNING);
			e.printStackTrace();
		}
		try {
			if (output != null) {
				return Integer.valueOf(output);
			}
		} catch (Exception e) {
			GeneralUtils.printToConsole("Error parsing to integer Eval Period: " + e.getMessage());
			e.printStackTrace();
		}
		return GeneralUtils.ERROR_VALUE;
	}

	/**
	 * get Ngh Remove Threshold based on Snmp
	 *
	 * @return
	 * @throws IOException
	 * @author Dor Shalom
	 */
	public int getNghRemoveThreshold() {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellAnrCfgNghRemoveThreshold");
		String output = "";
		try {
			output = snmp.get(oid);
		} catch (Exception e) {
			report.report("getNghRemoveThreshold failed due to: " + e.getMessage(), Reporter.WARNING);
			e.printStackTrace();
		}
		try {
			if (output != null) {
				return Integer.valueOf(output);
			}
		} catch (Exception e) {
			GeneralUtils.printToConsole("Error parsing to integer NghRemoveThreshold: " + e.getMessage());
			e.printStackTrace();
		}
		return GeneralUtils.ERROR_VALUE;
	}

	public void setNghRemoveThreshold(int value) {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellAnrCfgNghRemoveThreshold");
		try {
			snmp.snmpSet(oid, value);
		} catch (Exception e) {
			report.report("can't send command snmp protocol");
			e.printStackTrace();
		}
	}

	public void setEvalPeriod(int value) {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellAnrCfgEvalPeriod");
		try {
			snmp.snmpSet(oid, value);
		} catch (Exception e) {
			report.report("can't send command snmp protocol");
			e.printStackTrace();
		}
	}

	/**
	 * get Ngh Remove Threshold based on Snmp
	 *
	 * @return
	 * @throws IOException
	 * @author Dor Shalom
	 */
	public String getMaxUeSupported() {
		String oid = MibReader.getInstance().resolveByName("asLteStkStackCfgMaxUeSupported");
		String output = new String();
		output = snmp.get(oid);
		return output;
	}

	public void setMaxUeSupported(int value) {
		String oid = MibReader.getInstance().resolveByName("asLteStkStackCfgMaxUeSupported");
		try {
			snmp.snmpSet(oid, value);
		} catch (Exception e) {
			report.report("can't send command snmp protocol");
			e.printStackTrace();
		}
	}

	public String getEnbType() throws IOException {
		String oid = MibReader.getInstance().resolveByName("asLteStkStackCfgEnbType");
		return snmp.get(oid);
	}

	public void setEnbType(int value) {
		String oid = MibReader.getInstance().resolveByName("asLteStkStackCfgEnbType");
		try {
			snmp.snmpSet(oid, value);
		} catch (Exception e) {
			report.report("can't send command snmp protocol");
			e.printStackTrace();
		}
	}

	/**
	 * get anrMode based on Snmp
	 *
	 * @return
	 * @throws IOException
	 * @author Dor Shalom
	 */
	public String getAnrMode() throws IOException {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellAnrCfgAnrState");
		return snmp.get(oid);
	}

	public void setAnrMode(int value) {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellAnrCfgAnrState");
		try {
			snmp.snmpSet(oid, value);
		} catch (Exception e) {
			report.report("can't send command snmp protocol");
			e.printStackTrace();
		}
	}

	/**
	 * get anrMode based on Snmp
	 *
	 * @return
	 * @throws IOException
	 * @author Dor Shalom
	 */
	public boolean getPnpMode() {
		String oid = MibReader.getInstance().resolveByName("asLteStkPnpCfgPnpMode");
		String output = snmp.get(oid);
		return output.equals("1");
	}

	public boolean setPnpMode(int value) {
		String oid = MibReader.getInstance().resolveByName("asLteStkPnpCfgPnpMode");
		try {
			return snmp.snmpSet(oid, value);
		} catch (Exception e) {
			report.report("can't send command snmp protocol");
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * get anrMode based on Snmp
	 *
	 * @return
	 * @throws IOException
	 * @author Dor Shalom
	 */
	public String getPnpWarmRebootMask() throws IOException {
		String oid = MibReader.getInstance().resolveByName("asLteStkPnpCfgWarmRebootMask");
		return snmp.get(oid);
	}

	public void setPnpWarmRebootMask(int value) {
		report.report(String.format("set warmRebootMask to %s by SNMP.", value));
		String oid = MibReader.getInstance().resolveByName("asLteStkPnpCfgWarmRebootMask");
		try {
			snmp.snmpSet(oid, value);
		} catch (Exception e) {
			report.report("can't send command snmp protocol");
			e.printStackTrace();
		}
	}

	/**
	 * get PnP warm reset mode admin based on Snmp
	 *
	 * @return
	 * @throws IOException
	 * @author Dor Shalom
	 */
	public String getPnpWarmResetModeAdmin() throws IOException {
		String oid = MibReader.getInstance().resolveByName("asLteStkPnpCfgWarmResetModeAdmin");
		return snmp.get(oid);
	}

	public void setPnpWarmResetModeAdmin(int value) {
		report.report(String.format("set warmResetModeAdmin to %s by SNMP.", value));
		String oid = MibReader.getInstance().resolveByName("asLteStkPnpCfgWarmResetModeAdmin");
		try {
			snmp.snmpSet(oid, value);
		} catch (Exception e) {
			report.report("can't send command snmp protocol");
			e.printStackTrace();
		}
	}

	/**
	 * get anrDurationTimer based on Snmp
	 *
	 * @return
	 * @throws IOException
	 * @author Dor Shalom
	 */
	public String getAnrDurationTimer() throws IOException {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellAnrCfgDrxOnDurationTimer");
		return snmp.get(oid);
	}

	public void setAnrDurationTimer(int value) {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellAnrCfgDrxOnDurationTimer");
		try {
			snmp.snmpSet(oid, value);
		} catch (Exception e) {
			report.report("can't send command snmp protocol");
			e.printStackTrace();
		}
	}

	/**
	 * wait For All Running And In Service Rhe EnodeB is being sampled
	 * repeatedly, in order to check its state
	 *
	 * @param timeout
	 *            - (milliSeconds) While this time, the EnodeB is being sampled
	 *            repeatedly, in order to check its state.
	 * @return - boolean - true if it's in all running state
	 */
	public boolean waitForAllRunningAndInService(long timeout) {
		GeneralUtils.printToConsole("Will wait for all running " + (timeout / 1000) + " seconds");
		long startTime = System.currentTimeMillis(); // fetch starting time
		while ((System.currentTimeMillis() - startTime) < timeout) {
			if (isInOperationalStatus())
				return true;
			GeneralUtils.unSafeSleep(60000);
		}

		if ((System.currentTimeMillis() - startTime) >= timeout) {
			report.report("Reach to timeout while waiting for ALL RUNNING.", Reporter.WARNING);
		}

		return isInOperationalStatus();
	}

	public int getCountersValue(String valueName) {
		HashMap<String, Variable> valueMap;
		int ret = 0;
		String protobufData = MibReader.getInstance().resolveByName("asLteEnbStatsData");
		valueMap = snmp.SnmpWalk(protobufData, false);
		for (Entry<String, Variable> entry : valueMap.entrySet()) {
			try {
				byte[] protoBuf = ((OctetString) entry.getValue()).getValue();
				ret += ProtoBuf.getStatsPbLteStatsCell(protoBuf, valueName);
			} catch (Exception e) {
				e.printStackTrace();
				return GeneralUtils.ERROR_VALUE;
			}
		}
		return ret;
	}

	public int getCellBarredValue(int cellNum) {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellSib1CfgCellBarred");
		oid = oid + "." + (40 + cellNum);
		String result = snmp.get(oid);
		return Integer.parseInt(result);
	}

	public int getCounterSampleValue() {
		for (int i = 0; i < 4; i++) {
			try {
				String[][] mat = dbGetInMatrix("statsConfig", "sampleInterval", "5");
				if (mat != null) {
					int val = Integer.parseInt(getValFromMatrix(mat, "sampleInterval", 1));
					return val;
				}
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
		}
		return GeneralUtils.ERROR_VALUE;
	}

	public int getSingleSampleCountersValue(String valueName) {
		HashMap<String, Variable> valueMap;
		int ret = 0;
		String protobufData = MibReader.getInstance().resolveByName("asLteEnbStatsData");
		valueMap = snmp.SnmpWalk(protobufData, false);
		for (Entry<String, Variable> entry : valueMap.entrySet()) {
			try {
				byte[] protoBuf = ((OctetString) entry.getValue()).getValue();
				ret = (int) ProtoBuf.getStatsPbLteStatsCell(protoBuf, valueName);
				break;
			} catch (Exception e) {
				e.printStackTrace();
				return GeneralUtils.ERROR_VALUE;
			}
		}
		return ret;
	}

	public void reboot(boolean swapAndReboot) {
		String oid;
		if (swapAndReboot) {
			oid = MibReader.getInstance().resolveByName("asMaxBsCmSwSecondaryVersion") + "." + SWTypeInstnace;
		} else {
			oid = MibReader.getInstance().resolveByName("asMaxBsCmSwPrimaryVersion") + "." + SWTypeInstnace;
		}
		try {
			String requestedVersion = snmp.get(oid);
			oid = MibReader.getInstance().resolveByName("asMaxBsCmSwCfgVersionToBecomePrimary") + "." + SWTypeInstnace;
			// byte[] versionByIp =
			// InetAddressesHelper.convertIPStringTo15ByteArr(requestedVersion);
			if (snmp.snmpSet(oid, requestedVersion)) {
				reboot();
			} else
				report.report("The ENodeB: " + this.getName() + " Could not be Restarted");
		} catch (Exception e) {
		}
	}

	public boolean setVersionToPrimary(String MibName) {
		int tries = 0;
		String target = MibReader.getInstance().resolveByName(MibName) + "." + SWTypeInstnace;
		String swCommandSwapToPrimary = MibReader.getInstance().resolveByName("asMaxBsCmSwCfgCommand") + "."
				+ SWTypeInstnace;
		String toPrim = MibReader.getInstance().resolveByName("asMaxBsCmSwCfgVersionToBecomePrimary") + "."
				+ SWTypeInstnace;

		String requestedVersion = null;
		while ((requestedVersion == null) && tries < 3) {
			tries++;
			try {
				requestedVersion = snmp.get(target);
			} catch (Exception e) {
				GeneralUtils.unSafeSleep(2000);
				GeneralUtils.printToConsole("Exception during setVersionToPrimary-get Prim" + e.getMessage());
				e.printStackTrace();
				continue;
			}
		}
		tries = 0;

		if (requestedVersion == null) {
			report.report("cannot get Primary/Secondery Version", Reporter.WARNING);
			return false;
		}

		GeneralUtils.printToConsole("requestedVersion: " + requestedVersion);
		boolean setStatus = false;
		String versionToBePrim = null;

		do {
			tries++;
			try {
				setStatus = snmp.snmpSet(toPrim, requestedVersion) && snmp.snmpSet(swCommandSwapToPrimary, 3);
				versionToBePrim = snmp.get(toPrim).trim();
			} catch (Exception e) {
				GeneralUtils.unSafeSleep(2000);
				GeneralUtils.printToConsole("Exception during setVersionToPrimary-set and get next" + e.getMessage());
				e.printStackTrace();
				continue;
			}
		} while ((!setStatus || !requestedVersion.equals(versionToBePrim)) && tries < 3);

		if (!setStatus) {
			report.report("cannot set 'Become Primary Version'", Reporter.WARNING);
			return false;
		}
		return true;
	}

	public boolean swapBank() {
		return setVersionToPrimary("asMaxBsCmSwSecondaryVersion");

	}

	public boolean preserveBank() {
		return setVersionToPrimary("asMaxBsCmSwPrimaryVersion");

	}

	/**
	 * @return false - if didn't managed to change value via snmp. true - if
	 *         successfully managed to change value.
	 * @throws IOException
	 */
	public boolean disableDynamicCFI() throws IOException {
		int numOfCells = getNumberOfCells();
		String oid = MibReader.getInstance().resolveByName("asLteStkMacCfgDcfiEnable");
		boolean result = true;
		for (int i = 0; i < numOfCells; i++) {
			String fullOid = oid + "." + (40 + i);
			result = snmp.snmpSet(fullOid, 0) && result;
		}
		return result;
	}

	/**
	 * returns true if DynamicCFi enable and false otherwise
	 *
	 * @throws IOException
	 * @author Shahaf Shuhamy
	 */
	public boolean isDynamicCFIEnable() throws IOException {
		boolean result = true;
		String resultStr = "";

		String oid = MibReader.getInstance().resolveByName("asLteStkMacCfgDcfiEnable");
		resultStr = snmp.get(oid + ".40");
		result = resultStr.equals("1");
		return result;
	}

	/**
	 * @return
	 * @author Shahaf Shuhamy does SNMP from excel file and returning the
	 *         Summing of the walk method with the MIB
	 */
	public int connectionEstabAttSumSNMP() {
		String oid = MibReader.getInstance().resolveByName("asLteStatsRrcConnEstabAttSum");
		int resultSum = 0;
		HashMap<String, Variable> result = snmp.SnmpWalk(oid);
		for (Variable var : result.values()) {
			resultSum += var.toInt();
		}
		return resultSum;
	}

	/**
	 * @return
	 * @author Shahaf Shuhamy does SNMP from excel file and returning the
	 *         Summing of the walk method with the MIB
	 */
	public int connectionEstabAttSuccSNMP() {
		String oid = MibReader.getInstance().resolveByName("asLteStatsRrcConnEstabSuccSum");
		int resultSum = 0;
		HashMap<String, Variable> result = snmp.SnmpWalk(oid);
		for (Variable var : result.values()) {
			resultSum += var.toInt();
		}
		return resultSum;
	}

	/**
	 * @return
	 * @author Avichai Yefet Disable idle mode + reboot + wait for all running
	 */
	public void disableIdleModeAndReboot() {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellCfgInactivityTimerInterval");
		snmp.snmpSet(oid, 0);
		report.report("Idle mode disabled");
		reboot();
		report.report("Waiting " + EnodeB.WAIT_FOR_ALL_RUNNING_TIME + " minutes top for ALL RUNNING");
		waitForAllRunningAndInService(EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
	}

	/**
	 * @return
	 * @author Avichai Yefet Enable idle mode + reboot + wait for all running
	 */
	public void enableIdleModeAndReboot() {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellCfgInactivityTimerInterval");
		snmp.snmpSet(oid, 1000);
		report.report("Idle mode enabled");
		reboot();
		report.report("Waiting " + EnodeB.WAIT_FOR_ALL_RUNNING_TIME + " minutes top for ALL RUNNING");
		waitForAllRunningAndInService(EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
	}

	/**
	 * @param factorSig
	 * @param factorDat
	 * @param timeSig
	 * @param timeDat
	 * @param sigAdmin
	 * @param datAdmin
	 * @param enable
	 *            values and setting them
	 * @author sshahaf enable barring mode with parameters
	 */
	public boolean setBarringValues(int factorSig, int factorDat, int timeSig, int timeDat, int sigAdmin, int datAdmin,
			int enable) {
		return (setBarringFactorDat(factorDat) && setBarringFactorSig(factorSig) && setBarringTimeSig(timeSig)
				&& setBarringTimeDat(timeDat) && setBarringSigAdmin(sigAdmin) && setbarringDatAdmin(datAdmin)
				&& setbarringEnable(enable));
	}

	/**
	 * setting value for the mib asLteStkCellSib2CfgAcBarringFactorDat
	 *
	 * @param value
	 * @return
	 */
	public boolean setBarringFactorDat(int value) {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellSib2CfgAcBarringFactorDat");
		if (snmp.snmpSet(oid, value)) {
			GeneralUtils.printToConsole("mib value: " + value);
			return true;
		} else {
			report.report("Cannot set BarringFactorDat with snmp");
			return false;
		}
	}

	/**
	 * setting value for the mib asLteStkCellSib2CfgAcBarringFactorSig
	 *
	 * @param value
	 * @return
	 */
	public boolean setBarringFactorSig(int value) {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellSib2CfgAcBarringFactorSig");

		if (snmp.snmpSet(oid, value)) {
			GeneralUtils.printToConsole("mib value: " + value);
			return true;
		} else {
			report.report("Cannot set BarringFactorSig with snmp");
			return false;
		}

	}

	/**
	 * setting value for the mib asLteStkCellSib2CfgAcBarringTimeSig
	 *
	 * @param value
	 * @return
	 */
	public boolean setBarringTimeSig(int value) {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellSib2CfgAcBarringTimeSig");
		if (snmp.snmpSet(oid, value)) {
			GeneralUtils.printToConsole("mib value: " + value);
			return true;
		} else {
			report.report("Cannot set BarringTimeSig with snmp");
			return false;
		}
	}

	/**
	 * setting value for the mib asLteStkCellSib2CfgAcBarringTimeDat
	 *
	 * @param value
	 * @return
	 */
	public boolean setBarringTimeDat(int value) {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellSib2CfgAcBarringTimeDat");
		if (snmp.snmpSet(oid, value)) {
			GeneralUtils.printToConsole("mib value: " + value);
			return true;
		} else {
			report.report("Cannot set BarringTimeDat with snmp");
			return false;
		}
	}

	/**
	 * setting value for the mib asLteStkCellSib2CfgAcBarringSigAdmin
	 *
	 * @param value
	 * @return
	 */
	public boolean setBarringSigAdmin(int value) {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellSib2CfgAcBarringSigAdmin");
		if (snmp.snmpSet(oid, value)) {
			GeneralUtils.printToConsole("mib value: " + value);
			return true;
		} else {
			report.report("Cannot set BarringSigAdmin with snmp");
			return false;
		}
	}

	/**
	 * setting value for the mib asLteStkCellSib2CfgAcBarringDatAdmin
	 *
	 * @param value
	 * @return
	 */
	public boolean setbarringDatAdmin(int value) {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellSib2CfgAcBarringDatAdmin");
		if (snmp.snmpSet(oid, value)) {
			GeneralUtils.printToConsole("mib value: " + value);
			return true;
		} else {
			report.report("Cannot set BarringDatAdmin with snmp");
			return false;
		}
	}

	/**
	 * setting value for the mib asLteStkCellSib2CfgAcBarringEnable
	 *
	 * @param value
	 * @return
	 */
	public boolean setbarringEnable(int value) {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellSib2CfgAcBarringEnable");
		if (snmp.snmpSet(oid, value)) {
			GeneralUtils.printToConsole("mib value: " + value);
			return true;
		} else {
			report.report("Cannot set BarringEnable with snmp");
			return false;
		}
	}

	/**
	 * @param integrityNullLevel
	 * @return
	 * @author Shahaf Shuhamy update with snmp
	 */
	public boolean updateIntegrityNull(int integrityNullLevel) {
		String oid = MibReader.getInstance().resolveByName("asLteStkSecurityAlgNullIntegrity");
		return snmp.snmpSet(oid, integrityNullLevel);
	}

	/**
	 * @param integrityAESLevel
	 * @return
	 * @author Shahaf Shuhamy update with snmp
	 */
	public boolean updateIntegrityAES(int integrityAESLevel) {
		String oid = MibReader.getInstance().resolveByName("asLteStkSecurityAlgAesIntegrity");
		return snmp.snmpSet(oid, integrityAESLevel);

	}

	/**
	 * @param integritySNOWLevel
	 * @return
	 * @author Shahaf Shuhamy update with snmp
	 */
	public boolean updateIntegritySNW(int integritySNOWLevel) {
		String oid = MibReader.getInstance().resolveByName("asLteStkSecurityAlgSnow3GIntegrity");
		return snmp.snmpSet(oid, integritySNOWLevel);
	}

	/**
	 * @param cipheringNullLevel
	 * @return
	 * @author Shahaf Shuhamy update with snmp
	 */
	public boolean updateCipheringNull(int cipheringNullLevel) {
		String oid = MibReader.getInstance().resolveByName("asLteStkSecurityAlgNullCiphering");
		return snmp.snmpSet(oid, cipheringNullLevel);

	}

	/**
	 * @param cipheringAESLevel
	 * @return
	 * @author Shahaf Shuhamy update with snmp
	 */
	public boolean updateCipheringAES(int cipheringAESLevel) {
		String oid = MibReader.getInstance().resolveByName("asLteStkSecurityAlgAesCiphering");
		return snmp.snmpSet(oid, cipheringAESLevel);

	}

	/**
	 * @param cipheringSNOWLevel
	 * @return
	 * @author Shahaf Shuhamy update with snmp
	 */
	public boolean updateCipheringSNW(int cipheringSNOWLevel) {
		String oid = MibReader.getInstance().resolveByName("asLteStkSecurityAlgSnow3GCiphering");
		return snmp.snmpSet(oid, cipheringSNOWLevel);

	}

	public int getTxPower() {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellCfgTxPower");
		String ans = "";
		int txpower = -999;
		ans = snmp.get(oid);
		try {
			txpower = Integer.parseInt(ans);
		} catch (Exception e) {
			report.report(
					"Couldn't get the txpower (asLteStkCellCfgTxPower MIB return illegal value - " + txpower + ").",
					Reporter.WARNING);
			e.printStackTrace();
		}
		return txpower;
	}

	public boolean setTxPower(int value) {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellCfgTxPower");
		return snmp.snmpSet(oid, value);
	}

	public int getPci() {
		return getPci(40);
	}

	public int getPci(int oidIndex) {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellCfgCellId");
		oid += ("." + oidIndex);
		String ans = "";
		int pci = GeneralUtils.ERROR_VALUE;
		ans = snmp.get(oid);
		try {
			pci = Integer.parseInt(ans);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return pci;
	}

	public boolean setPci(int value) {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellCfgCellId");
		return snmp.snmpSet(oid, value);
	}

	public int getMaxVolteCalls() {
		String oid = MibReader.getInstance().resolveByName("asLteStkVoLteCfgMaximumVolteCalls");
		String ans = "";
		int maxVolteCalls = GeneralUtils.ERROR_VALUE;
		ans = snmp.get(oid);
		try {
			maxVolteCalls = Integer.parseInt(ans);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return maxVolteCalls;
	}

	public boolean setMaxVolteCalls(int value) {
		String oid = MibReader.getInstance().resolveByName("asLteStkVoLteCfgMaximumVolteCalls");
		return snmp.snmpSet(oid, value);
	}

	public int getInterfaceStatusAvailabilityStatus(int interfaceIndex) {
		String oid = MibReader.getInstance().resolveByName("asLteStkIpInterfaceStatusAvailabilityStatus");
		String res = "";
		int answer;
		try {
			res = snmp.get(oid + "." + interfaceIndex);
			answer = Integer.parseInt(res);
		} catch (Exception e) {
			GeneralUtils.printToConsole("Error getting asLteStkIpInterfaceStatusAvailabilityStatus");
			e.printStackTrace();
			answer = GeneralUtils.ERROR_VALUE;
		}
		return answer;
	}

	public String getInterfaceStatusAvailabilityStatus(int valueIndex, int interfaceIndex) {
		String oid = MibReader.getInstance().resolveByName("asLteStkIpInterfaceStatusEntry");
		String res = "";
		res = snmp.get(oid + "." + valueIndex + "." + interfaceIndex);
		return res;
	}

	public EnbStates getRunningState() {
		EnbStates running = EnbStates.UNKNOWN;
		updateNumOfCells();
		for (int i = 0; i < numOfCells; i++) {
			int cell = 40 + i;
			if (getCellEnabled(cell)) {
				String oid = MibReader.getInstance().resolveByName("asLteStkCellStatusRunning");
				oid = oid + ("." + cell);
				String ans = "";
				try {
					ans = snmp.get(oid);
				} catch (Exception e) {
					GeneralUtils.printToConsole("Error getting asLteStkCellStatusRunning");
					e.printStackTrace();
				}
				if (ans.trim().equals("1")) {
					running = EnbStates.IN_SERVICE;
					continue;
				} else {
					GeneralUtils.printToConsole("Cell " + cell + " is not in running state, according to snmp.");
					return EnbStates.OUT_OF_SERVICE;
				}
			}
		}
		return running;
	}

	public String getCellServiceState() {
		return getCellServiceState(40);
	}

	public String getCellServiceState(int cell) {

		String oid = MibReader.getInstance().resolveByName("asLteStkChannelStatusServiceState");
		oid = oid + ("." + cell);
		String ans = "";
		try {
			ans = snmp.get(oid);
		} catch (Exception e) {
			GeneralUtils.printToConsole("Error getting asLteStkChannelStatusServiceState");
			e.printStackTrace();
		}
		return ans;
	}

	public EnbStates getServiceState() {
		EnbStates service = EnbStates.UNKNOWN;
		updateNumOfCells();
		for (int i = 0; i < numOfCells; i++) {
			int cell = 40 + i;
			if (getCellEnabled(cell)) {
				String oid = MibReader.getInstance().resolveByName("asLteStkChannelStatusServiceState");
				oid = oid + ("." + cell);
				String ans = "";
				try {
					ans = snmp.get(oid);
				} catch (Exception e) {
					GeneralUtils.printToConsole("Error getting asLteStkChannelStatusServiceState");
					e.printStackTrace();
				}
				if (ans == null || ans.trim().equals(String.valueOf(GeneralUtils.ERROR_VALUE)))
					return EnbStates.UNKNOWN;

				if (ans.trim().equals("1")) {
					service = EnbStates.IN_SERVICE;
					continue;
				} else {
					GeneralUtils.printToConsole("Cell " + cell + " is OOS, according to snmp.");
					return EnbStates.OUT_OF_SERVICE;
				}
			}
		}
		return service;
	}

	private void updateNumOfCells() {
		int sum = 0;
		if (numOfCells > 0)
			return;
		String cellEnableOID = MibReader.getInstance().resolveByName("asLteStkCellCfgEnabled");
		HashMap<String, Variable> walkResult = snmp.SnmpWalk(cellEnableOID);
		try {
			sum = walkResult.entrySet().size();
		} catch (Exception e) {
			GeneralUtils.printToConsole("Failed to count Cells " + e.getMessage());
			e.printStackTrace();
		}
		numOfCells = sum;
	}

	public List<RFStatus> getRFStatus() {
		List<RFStatus> ret = new ArrayList<RFStatus>();
		Integer RFNumber = 1;
		Integer index = 0;
		String oidVswr = MibReader.getInstance().resolveByName("asLteStkRfStatusMeasuredVswr");
		String oidTxPower = MibReader.getInstance().resolveByName("asLteStkRfStatusMeasuredTxPower");
		String oidOperationalStatus = MibReader.getInstance().resolveByName("asLteStkRfPathStatusInfo");
		String oidConfiguredTxPowerDbm = MibReader.getInstance().resolveByName("asLteStkRfStatusAchievedTxPower");
		HashMap<String, Variable> walkResult = snmp.SnmpWalk(oidVswr);

		for (String key : walkResult.keySet()) {
			RFStatus newRFStatus = new RFStatus();
			newRFStatus.RfNumber = "RF " + RFNumber.toString();
			double vswrDB = walkResult.get(key).toLong();
			GeneralUtils.printToConsole("vswrDB = " + vswrDB);
			double vswr = Math.pow(10, vswrDB / 1000);
			GeneralUtils.printToConsole("vswr = " + vswr);
			newRFStatus.MeasuredVswr = String.valueOf(vswr);
			GeneralUtils.printToConsole("MeasuredVswr = " + newRFStatus.MeasuredVswr);
			newRFStatus.ActualTxPowerDbm = GeneralUtils.tryParseStringToFloat(this.snmp.get(oidTxPower, index))
					/ 100.0f;
			GeneralUtils.printToConsole("ActualTxPowerDbm = " + newRFStatus.ActualTxPowerDbm);
			newRFStatus.OperationalStatus = this.snmp.get(oidOperationalStatus, index);
			GeneralUtils.printToConsole("OperationalStatus = " + newRFStatus.OperationalStatus);
			newRFStatus.ConfiguredTxPowerDbm = GeneralUtils
					.tryParseStringToFloat(this.snmp.get(oidConfiguredTxPowerDbm, index)) / 100.0f;
			GeneralUtils.printToConsole("ConfiguredTxPowerDbm = " + newRFStatus.ConfiguredTxPowerDbm);
			ret.add(newRFStatus);
			RFNumber++;
			index++;
		}
		return ret;
	}

	public int getEarfcn(String cell) {
		int DlFreq = GeneralUtils.ERROR_VALUE;
		int band = GeneralUtils.ERROR_VALUE;
		for (int i = 0; i < 3; i++) {
			DlFreq = getDownlinkFrequency(cell);
			if (DlFreq != GeneralUtils.ERROR_VALUE) {
				break;
			}
			GeneralUtils.unSafeSleep(2000);
		}
		for (int i = 0; i < 3; i++) {
			band = getBand();
			if (band != GeneralUtils.ERROR_VALUE) {
				break;
			}
			GeneralUtils.unSafeSleep(2000);
		}
		int earfcn = Earfcn.GetEarfcnFromFreqAndBand(DlFreq, band);
		return earfcn;
	}

	public boolean waitForAllRunning(double timeout) {
		GeneralUtils.printToConsole("Using waitForAllRunningAndInService method instead of waitForAllRunning");
		return waitForAllRunningAndInService((long) timeout);
	}

	public boolean isAllRunning() {
		return getRunningState().equals(EnbStates.IN_SERVICE);
	}

	public boolean SetPLMN(ArrayList<Plmn> PlmnList) throws IOException {
		boolean mcc_status = false;
		boolean mnc_status = false;
		// TODO :: What is this?
		// String mcc_oid =
		// MibReader.getInstance().resolveByName("asLteStkPlmnCfgMcc");
		// String mnc_oid =
		// MibReader.getInstance().resolveByName("asLteStkPlmnCfgMnc");
		// String status =
		// MibReader.getInstance().resolveByName("asLteStkPlmnCfgRowStatus");

		/*
		 * try { mcc_status = snmp.snmpSet(mcc_oid, mcc); mnc_status =
		 * snmp.snmpSet(mnc_oid, mnc); } catch (IOException e) {
		 * GeneralUtils.printToConsole("mcc_status:" + mcc_status);
		 * GeneralUtils.printToConsole("mnc_status:" + mnc_status);
		 * e.printStackTrace(); return false; }
		 */

		return mcc_status && mnc_status;
	}

	public boolean deletAllRowsInTable(String rowStatusOID) throws IOException {
		return snmp.deletAllRowsInTable(rowStatusOID);
	}

	public boolean addRowInTable(String rowStatusOid, HashMap<String, String> params) throws IOException {
		return snmp.addRowInTable(rowStatusOid, params);
	}

	public boolean resetCounter(String tableName, String index, HashMap<String, String> KeyValuePairs) {
		lteCli("kpi counters clearprotostats");
		return true;
	}

	public void grantPermission(String filePath) {
		String ans = this.shell("chmod -R 777 " + filePath);
		GeneralUtils.printToConsole("Grant permission to: " + filePath);
		GeneralUtils.printToConsole("result: " + ans);

	}

	public boolean getCellEnabled(int cellIndex) {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellCfgEnabled");
		oid = oid + "." + cellIndex;
		boolean isEnabled = false;
		try {
			isEnabled = snmp.get(oid).trim().equals("1");
		} catch (Exception e) {
			GeneralUtils.printToConsole("Failed to get cell enabled state" + e.getMessage());
			e.printStackTrace();
		}
		return isEnabled;
	}

	public boolean setCellEnabled(String cellIndex, boolean enabled) throws IOException {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellCfgEnabled");

		oid = oid + "." + cellIndex;

		return snmp.snmpSet(oid, enabled ? 1 : 0);
	}

	public int getNumberOfActiveCells() {
		int sum = 0;
		String cellEnableOID = MibReader.getInstance().resolveByName("asLteStkCellCfgEnabled");
		HashMap<String, Variable> walkResult = snmp.SnmpWalk(cellEnableOID);

		try {
			for (Entry<String, Variable> entry : walkResult.entrySet()) {
				if (entry.getValue().toInt() == 1)
					sum++;
			}
		} catch (Exception e) {
			GeneralUtils.printToConsole("Failed to count Cells " + e.getMessage());
			e.printStackTrace();
			sum = 1;
		}

		if (sum <= 0 || sum >= 3) {
			report.report("num of cell is Invalid:" + sum, Reporter.WARNING);
			sum = 1;
		}

		return sum;
	}

	public int getRSRPEventTriggerGaps() {
		String trigger = "";
		String oid = MibReader.getInstance().resolveByName("asLteStkCellMeasRepCfgMeasurementType");
		HashMap<String, Variable> mapTypes = null;
		for (int i = 0; i < 3; i++) {
			mapTypes = snmp.SnmpWalk(oid);
			for (Entry<String, Variable> var : mapTypes.entrySet()) {
				if (var.getValue().toString().equals("4")) {
					String[] temp = var.getKey().toString().split("\\.");
					trigger = temp[temp.length - 1];
					break;
				}
			}
			if (trigger != "") {
				break;
			}
			GeneralUtils.unSafeSleep(1000);
		}
		if (trigger == "") {
			return GeneralUtils.ERROR_VALUE;
		}
		try {
			oid = MibReader.getInstance().resolveByName("asLteStkCellMeasRepCfgRSRPEventThreshold1");
			String result = snmp.get(oid + "." + trigger);
			for (int i = 0; i < 3; i++) {
				result = snmp.get(oid + "." + trigger);
				if (result != String.valueOf(GeneralUtils.ERROR_VALUE)) {
					return Integer.parseInt(result);
				}
				GeneralUtils.unSafeSleep(1000);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return GeneralUtils.ERROR_VALUE;
		}
		return GeneralUtils.ERROR_VALUE;
	}

	public int getNumberOfCells() {
		int sum = 0;
		String cellEnableOID = MibReader.getInstance().resolveByName("asLteStkCellCfgEnabled");
		HashMap<String, Variable> walkResult = snmp.SnmpWalk(cellEnableOID);

		try {
			sum = walkResult.entrySet().size();
		} catch (Exception e) {
			GeneralUtils.printToConsole("Failed to count Cells " + e.getMessage());
			e.printStackTrace();
			sum = 1;
		}

		if (sum <= 0) {
			sum = 1;
			report.report("num of cell is Invalid:" + sum, Reporter.WARNING);
		}

		return sum;

	}

	public String getTddAckMode() {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellDedCfgPhyTddAckNackFeedbackMode");
		String result = "ERROR Value";
		result = snmp.get(oid);
		return result;
	}

	public boolean setTddAckMode(int value) {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellDedCfgPhyTddAckNackFeedbackMode");
		try {
			return snmp.snmpSet(oid, value);
		} catch (Exception e) {
			GeneralUtils.printToConsole(e.getMessage());
			return false;
		}
	}

	public String getParchZeroCorrrelZone() {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellSib2CfgPrachZeroCorrrelZoneCfg");
		String result = "ERROR Value";
		result = snmp.get(oid);
		return result;
	}

	public boolean setPrachZeroCorrelZone(int value) {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellSib2CfgPrachZeroCorrrelZoneCfg");
		try {
			return snmp.snmpSet(oid, value);
		} catch (Exception e) {
			GeneralUtils.printToConsole(e.getMessage());
			return false;
		}
	}

	public String getCAMode() {
		String oid = MibReader.getInstance().resolveByName("asLteStkStackCfgCaMode");
		String result = "ERROR Value";
		result = snmp.get(oid);
		return result;
	}

	public boolean setCAMode(int value) {
		String oid = MibReader.getInstance().resolveByName("asLteStkStackCfgCaMode");
		try {
			return snmp.snmpSet(oid, value);
		} catch (Exception e) {
			GeneralUtils.printToConsole(e.getMessage());
			return false;
		}
	}

	public int getRSRPEventStopGaps() {
		String trigger = "";
		String oid = MibReader.getInstance().resolveByName("asLteStkCellMeasRepCfgMeasurementType");
		HashMap<String, Variable> mapTypes = null;
		for (int i = 0; i < 3; i++) {
			mapTypes = snmp.SnmpWalk(oid);
			for (Entry<String, Variable> var : mapTypes.entrySet()) {
				if (var.getValue().toString().equals("5")) {
					String[] temp = var.getKey().toString().split("\\.");
					trigger = temp[temp.length - 1];
					break;
				}
			}
			if (trigger != "") {
				break;
			}
			GeneralUtils.unSafeSleep(1000);
		}
		if (trigger == "") {
			return GeneralUtils.ERROR_VALUE;
		}
		try {
			oid = MibReader.getInstance().resolveByName("asLteStkCellMeasRepCfgRSRPEventThreshold1");
			String result = snmp.get(oid + "." + trigger);
			for (int i = 0; i < 3; i++) {
				result = snmp.get(oid + "." + trigger);
				if (result != String.valueOf(GeneralUtils.ERROR_VALUE)) {
					return Integer.parseInt(result);
				}
				GeneralUtils.unSafeSleep(1000);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return GeneralUtils.ERROR_VALUE;
		}
		return GeneralUtils.ERROR_VALUE;
	}

	public boolean setEnhanceStartGapAdmin(boolean enhance) {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellMobilityCfgEnhanceStartGapAdmin");
		try {
			if (enhance) {
				return snmp.snmpSet(oid, 1);
			} else {
				return snmp.snmpSet(oid, 0);
			}
		} catch (Exception e) {
			e.printStackTrace();
			report.report("Exception in setting enhance gap admin mode", Reporter.FAIL);
			return false;
		}

	}

	public int getAnrLightRatio() {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellAnrCfgAnrLightRatio");
		try {
			String res = snmp.get(oid);
			return Integer.parseInt(res);
		} catch (Exception e) {
			e.printStackTrace();
			return GeneralUtils.ERROR_VALUE;
		}
	}

	public int getAnrLightMinUes() {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellAnrCfgAnrLightMinUes");
		try {
			String res = snmp.get(oid);
			return Integer.parseInt(res);
		} catch (Exception e) {
			e.printStackTrace();
			return GeneralUtils.ERROR_VALUE;
		}
	}

	public boolean setAnrLightRatio(int val) {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellAnrCfgAnrLightRatio");
		try {
			return snmp.snmpSet(oid, val);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean setAnrLightMinUes(int val) {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellAnrCfgAnrLightMinUes");
		try {
			return snmp.snmpSet(oid, val);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Checks if the banks were swapped by validating the running version.
	 *
	 * @return - true if swapped.
	 */
	public boolean isBankSwapped() {
		if (testerVer == null || !testerVer.matches(".*\\d\\d.*"))
			return false;
		long timeout = 5 * 1000 * 60;
		String runningVer = "";
		GeneralUtils
				.printToConsole("Check if Bank was swapped on " + this.getIpAddress() + " expected ver:" + testerVer);
		long startTime = System.currentTimeMillis(); // fetch starting time
		while ((System.currentTimeMillis() - startTime) < timeout) {
			runningVer = this.getRunningVersion();
			if (runningVer.matches("\\d\\d+\\.\\d\\d+\\.\\d\\d+\\.\\d\\d+")) {
				GeneralUtils.printToConsole("Receieved current version: " + runningVer);
				return !runningVer.equals(testerVer);
			} else
				GeneralUtils.unSafeSleep(5 * 1000);

		}
		GeneralUtils.printToConsole("Failed to get currecnt version, last answer: " + runningVer);
		return false;

	}

	public boolean setMaxSshConnections(int val) {
		GeneralUtils.printToConsole("Send to cli: db set NetworkServices [1] MaxSSHCon=" + val);
		String response = this.lteCliWithResponse("db set NetworkServices [1] MaxSSHCon=" + val, "1 entries updated");
		GeneralUtils.printToConsole("response: " + response);
		return response.contains("1 entries updated");

		/*
		 * String oid = MibReader.getInstance().resolveByName(
		 * "asLteStkNetworkServicesCfgMaxSshCon"); try { return
		 * snmp.snmpSet(oid, val); } catch (Exception e) { e.printStackTrace();
		 * return false; }
		 */
	}

	public int getMaxSshConnections() {
		String oid = MibReader.getInstance().resolveByName("asLteStkNetworkServicesCfgMaxSshCon");
		try {
			return Integer.valueOf(snmp.get(oid));
		} catch (Exception e) {
			e.printStackTrace();
			return GeneralUtils.ERROR_VALUE;
		}
	}

	public int getDownlinkFrequency() {
		return getDownlinkFrequency("40");
	}

	public int getDownlinkFrequency(String cell) {
		String oid = "" + MibReader.getInstance().resolveByName("asLteStkCellCfgDownlinkFreq");
		oid += ("." + cell);
		String freq;
		try {
			freq = snmp.get(oid);
		} catch (Exception e1) {
			e1.printStackTrace();
			report.report("Couldn't get the downlink frequency (couldn't get asLteStkCellCfgDownlinkFreq MIB).",
					Reporter.WARNING);
			GeneralUtils.printToConsole(
					"Couldn't get the downlink frequency (couldn't get asLteStkCellCfgDownlinkFreq MIB).");
			return 0;
		}
		try {
			return Integer.parseInt(freq);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			report.report("Couldn't get the downlink frequency (asLteStkCellCfgDownlinkFreq MIB return illegal value - "
					+ freq + ").", Reporter.WARNING);
			GeneralUtils.printToConsole("Couldn't Convert " + freq + " to Integer");
			return 0;
		}
	}

	public int getBand() {
		String band = "0";
		String oid = MibReader.getInstance().resolveByName("asLteStkCellCfgFreqBand");
		try {
			band = snmp.get(oid);
		} catch (Exception e) {
			e.printStackTrace();
			report.report("Couldn't get the band", Reporter.WARNING);
		}
		try {
			return Integer.parseInt(band);
		} catch (Exception e) {
			e.printStackTrace();
			report.report("Couldn't Convert " + band + " to Integer", Reporter.WARNING);
			return 0;
		}
	}

	public ArrayList<Plmn> getConfiguredPLMNList() {
		ArrayList<Plmn> plmns = new ArrayList<>();
		String mccOID = MibReader.getInstance().resolveByName("asLteStkPlmnCfgMcc");
		String mncOID = MibReader.getInstance().resolveByName("asLteStkPlmnCfgMnc");

		report.report("PLMN MCC OID Snmpwalk: " + mccOID);
		HashMap<String, Variable> mccWalk = this.snmp.SnmpWalk(mccOID);
		Object[] mccs = mccWalk.values().toArray();

		report.report("PLMN MNC OID Snmpwalk: " + mncOID);
		HashMap<String, Variable> mncWalk = this.snmp.SnmpWalk(mncOID);
		Object[] mncs = mncWalk.values().toArray();

		if (mncs.length != mccs.length) {
			report.report("PLMN MCCS oid: " + mccOID + " - length " + mccs.length);
			report.report("PLMN MNCS oid: " + mncOID + " - length " + mccs.length);
			report.report("PLMN MCC & MNC Snmpwalk return different results:", Reporter.WARNING);
			return plmns;
		}

		for (int i = 0; i < mccs.length; i++) {
			Plmn temp = new Plmn();
			String[] hexArraymcc = mccs[i].toString().split(":");
			String[] hexArraymnc = mncs[i].toString().split(":");
			String mcc = calculateAsciiToInt(hexArraymcc);
			String mnc = calculateAsciiToInt(hexArraymnc);
			temp.setMCC(mcc);
			temp.setMNC(mnc);
			plmns.add(temp);
		}
		return plmns;

	}

	private String calculateAsciiToInt(String[] hexArray) {
		String num = "";
		for (int i = 0; i < hexArray.length; i++) {
			if (Integer.parseInt("0" + hexArray[i], 16) != 0)
				num += (char) Integer.parseInt(hexArray[i], 16);
		}
		return num;
	}

	public void restartSessions() {
		sessionManager.restartSessions();
	}

	public boolean setRSI(int value) {
		String oid = MibReader.getInstance().resolveByName("asLteStkeSonStatusPrachRootSequenceIdx");
		return snmp.snmpSet(oid, value);
	}

	public int getNrtChanges() {
		String oid = MibReader.getInstance().resolveByName("asLteStkBulkStatusNrtChanges");
		try {
			return Integer.parseInt(snmp.get(oid));
		} catch (Exception e) {
			e.printStackTrace();
			report.report("Couldn't get the NrtChanges", Reporter.WARNING);
		}
		return GeneralUtils.ERROR_VALUE;
	}

	public PbLteAnrStatus getPbLteSAnrStatus(long mnoBroadcastPlmn, long eutranCellId) {
		String oid = MibReader.getInstance().resolveByName("asLteStkBulkStatusNrtData");
		try {
			byte[] protoBufData = ((OctetString) snmp.getVariable(oid)).getValue();
			List<PbLteAnrStatus> pbLteAnrStatusList = ProtoBuf.getPbLteAnrStatus(protoBufData);
			for (PbLteAnrStatus pbLteAnrStatus : pbLteAnrStatusList) {
				int tmpMnoBroadcastPlmn = pbLteAnrStatus.getNbPlmnId();
				int tmpEutranCellId = pbLteAnrStatus.getNbCellIdentity();
				if (mnoBroadcastPlmn == tmpMnoBroadcastPlmn && eutranCellId == tmpEutranCellId) {
					return pbLteAnrStatus;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public int getCellChanges() {
		String oid = MibReader.getInstance().resolveByName("asLteStkBulkStatusCellChanges");
		try {
			return Integer.parseInt(snmp.get(oid));
		} catch (Exception e) {
			e.printStackTrace();
			report.report("Couldn't get the CellChanges", Reporter.WARNING);
		}
		return GeneralUtils.ERROR_VALUE;
	}

	public int getMmeChanges() {
		String oid = MibReader.getInstance().resolveByName("asLteStkBulkStatusMmeChanges");
		try {
			return Integer.parseInt(snmp.get(oid));
		} catch (Exception e) {
			e.printStackTrace();
			report.report("Couldn't get the MmeChanges", Reporter.WARNING);
		}
		return GeneralUtils.ERROR_VALUE;
	}

	public int getNetworkElementChanges() {
		String oid = MibReader.getInstance().resolveByName("asLteStkBulkStatusNetworkElementChanges");
		try {
			return Integer.parseInt(snmp.get(oid));
		} catch (Exception e) {
			e.printStackTrace();
			report.report("Couldn't get the NetworkElement", Reporter.WARNING);
		}
		return GeneralUtils.ERROR_VALUE;
	}

	public int getRfChanges() {
		String oid = MibReader.getInstance().resolveByName("asLteStkBulkStatusRfChanges");
		try {
			return Integer.parseInt(snmp.get(oid));
		} catch (Exception e) {
			e.printStackTrace();
			report.report("Couldn't get the RfChanges", Reporter.WARNING);
		}
		return GeneralUtils.ERROR_VALUE;
	}

	public int getSgwChanges() {
		String oid = MibReader.getInstance().resolveByName("asLteStkBulkStatusSgwChanges");
		try {
			return Integer.parseInt(snmp.get(oid));
		} catch (Exception e) {
			e.printStackTrace();
			report.report("Couldn't get the SgwChanges", Reporter.WARNING);
		}
		return GeneralUtils.ERROR_VALUE;
	}

	public boolean setNrtChanges(int value) {
		String oid = MibReader.getInstance().resolveByName("asLteStkBulkStatusNrtChanges");
		return snmp.snmpSet(oid, value);
	}

	public boolean setCellChanges(int value) {
		String oid = MibReader.getInstance().resolveByName("asLteStkBulkStatusCellChanges");
		return snmp.snmpSet(oid, value);
	}

	public boolean setSgwChanges(int value) {
		String oid = MibReader.getInstance().resolveByName("asLteStkBulkStatusSgwChanges");
		return snmp.snmpSet(oid, value);
	}

	public boolean setRfChanges(int value) {
		String oid = MibReader.getInstance().resolveByName("asLteStkBulkStatusRfChanges");
		return snmp.snmpSet(oid, value);
	}

	public boolean setNetworkElementChanges(int value) {
		String oid = MibReader.getInstance().resolveByName("asLteStkBulkStatusNetworkElementChanges");
		return snmp.snmpSet(oid, value);
	}

	public boolean setMmeChanges(int value) {
		String oid = MibReader.getInstance().resolveByName("asLteStkBulkStatusMmeChanges");
		return snmp.snmpSet(oid, value);
	}

	public int getNghListEnbType(long mnoBroadcastPlmn, long eutranCellId) {
		String oid = MibReader.getInstance().resolveByName("asLteStkNghListEnbType");
		oid += ("." + mnoBroadcastPlmn + "." + eutranCellId);
		try {
			return Integer.valueOf(snmp.get(oid));
		} catch (Exception e) {
			GeneralUtils.printToConsole("Error setting NghListEnbType: " + e.getMessage());
			e.printStackTrace();
		}
		return GeneralUtils.ERROR_VALUE;
	}

	public boolean setNghListEnbType(int mnoBroadcastPlmn, int eutranCellId, int value) {
		String oid = MibReader.getInstance().resolveByName("asLteStkCmdQNghListEnbType");
		oid += ("." + mnoBroadcastPlmn + "." + eutranCellId);
		return snmp.snmpSet(oid, value);
	}

	public int getNghListQOffsetCell(long mnoBroadcastPlmn, long eutranCellId) {
		String oid = MibReader.getInstance().resolveByName("asLteStkNghListQOffsetCell");
		oid += ("." + mnoBroadcastPlmn + "." + eutranCellId);
		try {
			return Integer.valueOf(snmp.get(oid));
		} catch (Exception e) {
			GeneralUtils.printToConsole("Error setting NghListEnbType: " + e.getMessage());
			e.printStackTrace();
		}
		return GeneralUtils.ERROR_VALUE;
	}

	public boolean setNghListQOffsetCell(int mnoBroadcastPlmn, int eutranCellId, int value) {
		String oid = MibReader.getInstance().resolveByName("asLteStkCmdQNghListQOffsetCell");
		oid += ("." + mnoBroadcastPlmn + "." + eutranCellId);
		return snmp.snmpSet(oid, value);
	}

	public PbLteCellStatus getPbLteCellStatus(int cellNumber) {
		String oid = MibReader.getInstance().resolveByName("asLteStkBulkStatusCellData");
		try {
			byte[] protoBufData = ((OctetString) snmp.getVariable(oid)).getValue();
			List<PbLteCellStatus> pbLteCellStatusList = ProtoBuf.getPbLteCellStatus(protoBufData);
			for (PbLteCellStatus pbLteCellStatus : pbLteCellStatusList) {
				int tmpCellNumber = pbLteCellStatus.getCellNumber();
				if (cellNumber == tmpCellNumber) {
					return pbLteCellStatus;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public PbLteNwElementStatus getPbLteNetworkElementStatus(int networkType) {
		String oid = MibReader.getInstance().resolveByName("asLteStkBulkStatusNetworkElementData");
		try {
			byte[] protoBufData = ((OctetString) snmp.getVariable(oid)).getValue();
			List<PbLteNwElementStatus> pbLteNwElementStatusList = ProtoBuf.getPbLteNetworkElementStatus(protoBufData);
			for (PbLteNwElementStatus pbLteNwElementStatus : pbLteNwElementStatusList) {
				int tmpNetworkType = pbLteNwElementStatus.getNetworkType();
				if (networkType == tmpNetworkType) {
					return pbLteNwElementStatus;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public PbLteMmeStatus getPbLteMmeStatus(String ipAddress) {
		String oid = MibReader.getInstance().resolveByName("asLteStkBulkStatusMmeData");
		try {
			byte[] protoBufData = ((OctetString) snmp.getVariable(oid)).getValue();
			List<PbLteMmeStatus> pbLteMmeStatusList = ProtoBuf.getPbLteMmeStatus(protoBufData);
			for (PbLteMmeStatus pbLteMmeStatus : pbLteMmeStatusList) {
				String tmpIpAddress = pbLteMmeStatus.getIpAddress();
				if (ipAddress.equals(tmpIpAddress)) {
					return pbLteMmeStatus;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public PbLteSgwStatus getPbLteSgwStatus(String ipAddress) {
		String oid = MibReader.getInstance().resolveByName("asLteStkBulkStatusSgwData");
		try {
			byte[] protoBufData = ((OctetString) snmp.getVariable(oid)).getValue();
			List<PbLteSgwStatus> pbLteSgwStatusList = ProtoBuf.getPbLteSgwStatus(protoBufData);
			for (PbLteSgwStatus pbLteSgwStatus : pbLteSgwStatusList) {
				String tmpIpAddress = pbLteSgwStatus.getIpAddress();
				if (ipAddress.equals(tmpIpAddress)) {
					return pbLteSgwStatus;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public PbLteRfStatus getPbLteRfStatus(int rfPathId) {
		String oid = MibReader.getInstance().resolveByName("asLteStkBulkStatusRfData");
		try {
			byte[] protoBufData = ((OctetString) snmp.getVariable(oid)).getValue();
			List<PbLteRfStatus> pbLteSgwStatusList = ProtoBuf.getPbLteRfStatus(protoBufData);
			for (PbLteRfStatus pbLteSgwStatus : pbLteSgwStatusList) {
				int tmpRfPathId = pbLteSgwStatus.getRfPathID();
				if (rfPathId == tmpRfPathId) {
					return pbLteSgwStatus;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public String getMmeStatusIpAddress() {
		String oid = MibReader.getInstance().resolveByName("asLteStkMmeStatusIpAddress");
		try {
			byte[] addrBytes = ((OctetString) snmp.getVariable(oid)).getValue();
			InetAddress addrInetAddress = InetAddress.getByAddress(addrBytes);
			String addrStr = addrInetAddress.getHostAddress();
			return addrStr;
		} catch (Exception e) {
			GeneralUtils.printToConsole("Error getting MmeStatusIpAddress: " + e.getMessage());
			e.printStackTrace();
		}
		return String.valueOf(GeneralUtils.ERROR_VALUE);
	}

	public String getCallTraceEnbCfgTraceServerIpAddr() {
		String oid = MibReader.getInstance().resolveByName("asLteStkCallTraceEnbCfgTraceServerIpAddress");
		try {
			byte[] addrBytes = ((OctetString) snmp.getVariable(oid)).getValue();
			InetAddress addrInetAddress = InetAddress.getByAddress(addrBytes);
			String addrStr = addrInetAddress.getHostAddress();
			return addrStr;
		} catch (Exception e) {
			GeneralUtils.printToConsole("Error getting CallTraceEnbCfgTraceServerIpAddr: " + e.getMessage());
			e.printStackTrace();
		}
		return String.valueOf(GeneralUtils.ERROR_VALUE);
	}

	public boolean setCallTraceEnbCfgTraceServerIpAddr(InetAddress inetAddress) {
		// Provisioning is Needed
		String oid = MibReader.getInstance().resolveByName("asLteStkCallTraceEnbCfgTraceServerIpAddress");
		try {
			byte[] addr = inetAddress.getAddress();
			return snmp.snmpSet(oid, addr);
		} catch (IOException e) {
			GeneralUtils.printToConsole("Error getting MmeStatusIpAddress: " + e.getMessage());
			e.printStackTrace();
		}
		return false;
	}

	public boolean provisionStartEvent() {
		return false;
	}

	public boolean provisionEndedEvent() {
		return false;
	}

	public boolean setMmeConfigIpAddress(InetAddress inetAddress) {
		// Provisioning is Needed
		String oid = MibReader.getInstance().resolveByName("asLteStkMmeCfgIpAddr");
		try {
			byte[] addr = inetAddress.getAddress();
			return snmp.snmpSet(oid, addr);
		} catch (IOException e) {
			GeneralUtils.printToConsole("Error setting MmeConfigIpAddress: " + e.getMessage());
			e.printStackTrace();
		}
		return false;
	}

	public String getMmeConfigIpAddress() {
		String oid = MibReader.getInstance().resolveByName("asLteStkMmeCfgIpAddr");
		try {
			byte[] addrBytes = ((OctetString) snmp.getVariable(oid)).getValue();
			InetAddress addrInetAddress = InetAddress.getByAddress(addrBytes);
			String addrStr = addrInetAddress.getHostAddress();
			return addrStr;
		} catch (Exception e) {
			GeneralUtils.printToConsole("Error getting MmeConfigIpAddress: " + e.getMessage());
			e.printStackTrace();
		}
		return String.valueOf(GeneralUtils.ERROR_VALUE);
	}

	public boolean setMmeConfigS1CVlanInfo(int s1cVlanInfo) {
		// Provisioning is Needed
		String oid = MibReader.getInstance().resolveByName("asLteStkMmeCfgS1CVlanId");
		return snmp.snmpSet(oid, s1cVlanInfo);
	}

	public int getMmeConfigS1CVlanInfo() {
		String oid = MibReader.getInstance().resolveByName("asLteStkMmeCfgS1CVlanId");
		try {
			return Integer.valueOf(snmp.get(oid));
		} catch (Exception e) {
			GeneralUtils.printToConsole("Error getting MmeConfigS1CVlanInfo: " + e.getMessage());
			e.printStackTrace();
		}
		return GeneralUtils.ERROR_VALUE;
	}

	public String getIpAddressType() {
		String oid = MibReader.getInstance().resolveByName("asLteStkIpInterfaceStatusIpAddressType");
		String ans = snmp.get(oid + ".3");// check s1u interface type
		if (ans.equals("1")) {
			return "IPv4";
		} else if (ans.equals("2")) {
			return "IPv6";
		}
		return "NA";
	}

	public boolean enableDynamicCFI() throws IOException {
		int numOfCells = getNumberOfCells();
		String oid = MibReader.getInstance().resolveByName("asLteStkMacCfgDcfiEnable");
		boolean result = true;
		for (int i = 0; i < numOfCells; i++) {
			String fullOid = oid + "." + (40 + i);
			result = snmp.snmpSet(fullOid, 1) && result;
		}
		return result;
	}

	public int[] getLedStatusValues() {
		HashMap<String, Variable> hm = snmp
				.SnmpWalk(MibReader.getInstance().resolveByName("asLteStkLedDrvPatternEntry"));
		int[] ledStatusValues = new int[11];
		String[] mibNames = { "asLteStkLedDrvPatternPowerLed", "asLteStkLedDrvPatternBhGreenLed",
				"asLteStkLedDrvPatternBhRedLed", "asLteStkLedDrvPatternWanGreenLed", "asLteStkLedDrvPatternWanRedLed",
				"asLteStkLedDrvPatternLteGreenLed", "asLteStkLedDrvPatternLteRedLed",
				"asLteStkLedDrvPatternUeSignalFirstLed", "asLteStkLedDrvPatternUeSignalSecondLed",
				"asLteStkLedDrvPatternUeSignalThirdLed", "asLteStkLedDrvPatternUeSignalFourthLed" };

		for (int i = 0; i < mibNames.length; i++) {
			if (hm != null) {
				try {
					ledStatusValues[i] = (hm.get(MibReader.getInstance().resolveByName(mibNames[i]))).toInt();
				} catch (Exception e) {
					GeneralUtils.printToConsole("Error getting LedStatus: " + e.getMessage());
				}
			}
		}

		return ledStatusValues;
	}

	public int getRntpAgingTimer() {
		String oid = MibReader.getInstance().resolveByName("asLteStkDicicCfgRntpAgingFactor");
		try {
			int agingFactor = Integer.valueOf(snmp.get(oid));
			oid = MibReader.getInstance().resolveByName("asLteStkDicicCfgRntpPeriodicity");
			int rntpPeriodicity = Integer.valueOf(snmp.get(oid));
			return rntpPeriodicity * agingFactor;
		} catch (Exception e) {
			GeneralUtils.printToConsole("Error getting RntpAgeingTimer: " + e.getMessage());
			e.printStackTrace();
		}
		return GeneralUtils.ERROR_VALUE;
	}

	public int getRsrpAgingTimer() {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellDicicCfgAvgRsrpAgingFactor");
		try {
			int agingFactor = Integer.valueOf(snmp.get(oid));
			oid = MibReader.getInstance().resolveByName("asLteStkCellDicicCfgReportInterval");
			int reportInterval = Integer.valueOf(snmp.get(oid));
			int reportIntervalInMilisec;
			switch (reportInterval) {
			case 0:
				reportIntervalInMilisec = 120;
				break;
			case 1:
				reportIntervalInMilisec = 240;
				break;
			case 2:
				reportIntervalInMilisec = 480;
				break;
			case 3:
				reportIntervalInMilisec = 640;
				break;
			case 4:
				reportIntervalInMilisec = 1024;
				break;
			case 5:
				reportIntervalInMilisec = 2048;
				break;
			case 6:
				reportIntervalInMilisec = 5120;
				break;
			case 7:
				reportIntervalInMilisec = 10240;
				break;
			case 8:
				reportIntervalInMilisec = 60000;
				break;
			case 9:
				reportIntervalInMilisec = 6 * 60000;
				break;
			case 10:
				reportIntervalInMilisec = 12 * 60000;
				break;
			case 11:
				reportIntervalInMilisec = 30 * 60000;
				break;
			case 12:
				reportIntervalInMilisec = 60 * 60000;
				break;
			default:
				reportIntervalInMilisec = 10240;
			}
			return reportIntervalInMilisec * agingFactor;
		} catch (Exception e) {
			GeneralUtils.printToConsole("Error getting RsrpAgingTimer: " + e.getMessage());
			e.printStackTrace();
		}
		return GeneralUtils.ERROR_VALUE;
	}

	public boolean setPacketForwardingEnable(boolean value, int cellId) {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellCfgPktFwdEnable") + "."
				+ String.valueOf(cellId + 39);
		String snmpValue = (value) ? "1" : "0";
		if (snmp.snmpSet(oid, snmpValue)) {
			return true;
		} else {
			report.report("setting PktFwdEnable failed", Reporter.WARNING);
			return false;
		}
	}

	public int getPacketForwardingEnable(int cellId) {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellCfgPktFwdEnable") + "."
				+ String.valueOf(cellId + 39);
		try {
			int pktFwdEnable = Integer.valueOf(snmp.get(oid));
			return pktFwdEnable;
		} catch (Exception e) {
			GeneralUtils.printToConsole("Error getting pktFwdEnable due to: " + e.getMessage());
			e.printStackTrace();
		}
		return GeneralUtils.ERROR_VALUE;
	}

	public boolean setTpmMo(int value, CellIndex cellIndex) {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellTpmCfgMroCycle") + "." + cellIndex.value;
		try {
			return snmp.snmpSet(oid, value);
		} catch (Exception e) {
			GeneralUtils.printToConsole("Error setting TPMMo due to: " + e.getMessage());
			report.report("setting TPMMo failed", Reporter.WARNING);
			e.printStackTrace();
		}
		return false;
	}

	public boolean setTpmNlPeriodic(int value, CellIndex cellIndex) {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellTpmCfgNLPeriodic") + "." + cellIndex.value;
		try {
			return snmp.snmpSet(oid, value);
		} catch (Exception e) {
			GeneralUtils.printToConsole("Error setting TPMNLPeriodic due to: " + e.getMessage());
			report.report("setting TPMNLPeriodic failed", Reporter.WARNING);
			e.printStackTrace();
		}
		return false;
	}

	public boolean setTpmCycleMultiple(int value, CellIndex cellIndex) {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellTpmCfgCycleMutiple") + "." + cellIndex.value;
		try {
			return snmp.snmpSet(oid, value);
		} catch (Exception e) {
			GeneralUtils.printToConsole("Error setting TPMCycleMultiple due to: " + e.getMessage());
			report.report("setting TPMCycleMultiple failed", Reporter.WARNING);
			e.printStackTrace();
		}
		return false;
	}

	public boolean setTPMServRsrpRlfThresh(int value, CellIndex cellIndex) {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellTpmCfgMroIntraFreqRsrpThreshServCell") + "."
				+ cellIndex.value;
		try {
			return snmp.snmpSet(oid, value);
		} catch (Exception e) {
			GeneralUtils.printToConsole("Error setting TPMServRsrpRlfThresh due to: " + e.getMessage());
			report.report("setting TPMServRsrpRlfThresh failed", Reporter.WARNING);
			e.printStackTrace();
		}
		return false;
	}

	public boolean setTPMNghRsrpRlfThresh(int value, CellIndex cellIndex) {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellTpmCfgMroIntraFreqRsrpThreshNghCell") + "."
				+ cellIndex.value;
		try {
			return snmp.snmpSet(oid, value);
		} catch (Exception e) {
			GeneralUtils.printToConsole("Error setting TPMNghRsrpRlfThresh due to: " + e.getMessage());
			report.report("setting TPMNghRsrpRlfThresh failed", Reporter.WARNING);
			e.printStackTrace();
		}
		return false;
	}

	public int GetIntraHoTooLatePoorCoverage(CellIndex cellIndex) {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellTpmStatusIntraHoTooLatePoorCovrg") + "."
				+ cellIndex.value;
		try {
			return Integer.valueOf(snmp.get(oid));
		} catch (Exception e) {
			GeneralUtils.printToConsole("Error getting IntraHoTooLatePoorCoverage due to: " + e.getMessage());
			report.report("getting IntraHoTooLatePoorCoverage failed", Reporter.WARNING);
			e.printStackTrace();
		}
		return GeneralUtils.ERROR_VALUE;
	}

	public int GetIntraHoTooLateGoodCoverageUnprepared(CellIndex cellIndex) {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellTpmStatusIntraHoTooLateGoodCovrgUnprepared")
				+ "." + cellIndex.value;
		try {
			return Integer.valueOf(snmp.get(oid));
		} catch (Exception e) {
			GeneralUtils.printToConsole("Error getting IntraHoTooLateGoodCoverageUnprepared due to: " + e.getMessage());
			report.report("getting IntraHoTooLateGoodCoverageUnprepared failed", Reporter.WARNING);
			e.printStackTrace();
		}
		return GeneralUtils.ERROR_VALUE;
	}

	public int getOutgoingIntraFreqHoAttempt(CellIndex cellIndex) {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellTpmStatusOutgoingIntraFreqHoAttempt") + "."
				+ cellIndex.value;
		try {
			return Integer.valueOf(snmp.get(oid));
		} catch (Exception e) {
			GeneralUtils.printToConsole("Error getting OutgoingIntraFreqHoAttempt due to: " + e.getMessage());
			report.report("getting OutgoingIntraFreqHoAttempt failed", Reporter.WARNING);
			e.printStackTrace();
		}
		return GeneralUtils.ERROR_VALUE;
	}

	public int getRfStatusAchievedTxPower(int cellIndex) {
		String oid = MibReader.getInstance().resolveByName("asLteStkRfStatusAchievedTxPower") + "." + cellIndex;
		try {
			return Integer.valueOf(snmp.get(oid));
		} catch (Exception e) {
			GeneralUtils.printToConsole("Error getting asLteStkRfStatusAchievedTxPower due to: " + e.getMessage());
			report.report("getting RfStatusAchievedTxPower failed", Reporter.WARNING);
			e.printStackTrace();
		}
		return GeneralUtils.ERROR_VALUE;
	}

	public int getTPMDelMoInc(CellIndex cellIndex) {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellTpmCfgStepUpPwr") + "." + cellIndex.value;
		try {
			return Integer.valueOf(snmp.get(oid));
		} catch (Exception e) {
			GeneralUtils.printToConsole("Error getting TPMDelMoInc due to: " + e.getMessage());
			report.report("getting TPMDelMoInc failed", Reporter.WARNING);
			e.printStackTrace();
		}
		return GeneralUtils.ERROR_VALUE;
	}

	public int getTPMDelMoDec(CellIndex cellIndex) {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellTpmCfgStepDownPwr") + "." + cellIndex.value;
		try {
			return Integer.valueOf(snmp.get(oid));
		} catch (Exception e) {
			GeneralUtils.printToConsole("Error getting TPMDelMoDec due to: " + e.getMessage());
			report.report("getting TPMDelMoDec failed", Reporter.WARNING);
			e.printStackTrace();
		}
		return GeneralUtils.ERROR_VALUE;
	}

	public int getReEstablishmentSameCellPoorCoverage(CellIndex cellIndex) {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellTpmStatusReEstabSameCellPoorCovrg") + "."
				+ cellIndex.value;
		try {
			return Integer.valueOf(snmp.get(oid));
		} catch (Exception e) {
			GeneralUtils.printToConsole("Error getting ReEstablishmentSameCellPoorCoverage due to: " + e.getMessage());
			report.report("getting ReEstablishmentSameCellPoorCoverage failed", Reporter.WARNING);
			e.printStackTrace();
		}
		return GeneralUtils.ERROR_VALUE;
	}

	public int getTpmMoPwrAdj(CellIndex cellIndex) {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellTpmStatusMoPwrAdj") + "." + cellIndex.value;
		try {
			return Integer.valueOf(snmp.get(oid));
		} catch (Exception e) {
			GeneralUtils.printToConsole("Error getting TPMMoPwrAdj due to: " + e.getMessage());
			report.report("getting TPMMoPwrAdj failed", Reporter.WARNING);
			e.printStackTrace();
		}
		return GeneralUtils.ERROR_VALUE;
	}

	public boolean setTPMEnable(boolean value, CellIndex cellIndex) {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellTpmCfgTpmEnabled") + "." + cellIndex.value;
		try {
			return snmp.snmpSet(oid, GeneralUtils.booleanToInteger(value));
		} catch (Exception e) {
			GeneralUtils.printToConsole("Error setting TPMEnable due to: " + e.getMessage());
			report.report("setting TPMEnable failed", Reporter.WARNING);
			e.printStackTrace();
		}
		return false;
	}

	public boolean setTPMMoEnable(boolean value, CellIndex cellIndex) {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellTpmCfgTpmMoEnabled") + "." + cellIndex.value;
		try {
			return snmp.snmpSet(oid, GeneralUtils.booleanToInteger(value));
		} catch (Exception e) {
			GeneralUtils.printToConsole("Error setting TPMMoEnable due to: " + e.getMessage());
			report.report("setting TPMMoEnable failed", Reporter.WARNING);
			e.printStackTrace();
		}
		return false;
	}

	public int getTemperatureSensorsSensorReading(int entry) {
		String oid = MibReader.getInstance().resolveByName("asMaxCmTempMonValue") + "." + entry;
		try {
			return Integer.valueOf(snmp.get(oid));
		} catch (Exception e) {
			GeneralUtils.printToConsole("Error getting TemperatureSensorsSensorReading due to: " + e.getMessage());
			report.report("getting TemperatureSensorsSensorReading failed", Reporter.WARNING);
			e.printStackTrace();
		}
		return GeneralUtils.ERROR_VALUE;
	}

	public int getSmonThresholdsCriticalMax(int entry) {
		String oid = MibReader.getInstance().resolveByName("asLteStkSmonThresholdsCriticalMax") + "." + entry;
		try {
			return Integer.valueOf(snmp.get(oid));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return GeneralUtils.ERROR_VALUE;
	}

	public boolean setSmonThresholdsCriticalMax(int entry, int value) {
		String oid = MibReader.getInstance().resolveByName("asLteStkSmonThresholdsCriticalMax") + "." + entry;
		try {
			return snmp.snmpSet(oid, value);
		} catch (Exception e) {
			GeneralUtils.printToConsole("Error setting SmonThresholdsCriticalMax due to: " + e.getMessage());
			report.report("Setting SmonThresholdsCriticalMax failed", Reporter.WARNING);
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * enable = true disable = false
	 *
	 * @param value
	 * @return
	 */
	public boolean setOtdoaMode(boolean value) {
		boolean result = true;
		try {
			int numOfCells = getNumberOfCells();
			String oid = MibReader.getInstance().resolveByName("asLteStkCellPosMeasCfgOtdoaMode");
			int valueToSet = value ? 1 : 0;
			for (int i = 0; i < numOfCells; i++) {
				String fullOid = oid + "." + (40 + i);
				result = snmp.snmpSet(fullOid, valueToSet) && result;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return result;
	}

	public String getMmeStatus() {
		String oid = MibReader.getInstance().resolveByName("asLteStkMmeStatusCommsStatus");
		try {
			return this.snmp.get(oid);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return String.valueOf(GeneralUtils.ERROR_VALUE);
	}

	public String getMmeStatusInfo() {
		String oid = MibReader.getInstance().resolveByName("asLteStkMmeStatusCommsStatusInfo");
		try {
			return this.snmp.get(oid);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return String.valueOf(GeneralUtils.ERROR_VALUE);
	}

	public String getAddressType() {
		String oid = MibReader.getInstance().resolveByName("asLteStkMmeStatusIpAddressType");
		try {
			return this.snmp.get(oid);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return String.valueOf(GeneralUtils.ERROR_VALUE);
	}

	public String getX2ConStatus(String mnoBroadcastPlmn, String eutranCellId) {
		String oid = MibReader.getInstance().resolveByName("asLteStkNghListCommsStatus");
		oid = oid + "." + mnoBroadcastPlmn + "." + eutranCellId;
		try {
			return this.snmp.get(oid);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return String.valueOf(GeneralUtils.ERROR_VALUE);
	}

	public String getHomeCellIdentity() {
		return getHomeCellIdentity(CellIndex.FORTY);
	}

	public String getHomeCellIdentity(CellIndex cellIndex) {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellCfgCellIdentity28bit");
		oid = oid + "." + cellIndex.value;
		try {
			return this.snmp.get(oid);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return String.valueOf(GeneralUtils.ERROR_VALUE);
	}

	public boolean getOTDOAMode(CellIndex cellIndex) {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellPosMeasCfgOtdoaMode");
		oid = oid + "." + cellIndex.value;
		String result = snmp.get(oid);
		return result.equals("1");
	}

	/**
	 * enable = true disable = false
	 *
	 * @param value
	 * @return
	 */
	public boolean setECIDMode(boolean value) {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellPosMeasCfgEcidModeEnabled");
		int snmpValue = 0;
		boolean result = false;
		snmpValue = value ? 1 : 0;
		try {
			result = snmp.snmpSet(oid, snmpValue);
		} catch (Exception e) {
			e.printStackTrace();
			result = false;
		}
		return result;
	}

	public boolean getECIDMode() {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellPosMeasCfgEcidModeEnabled");
		try {
			String result = snmp.get(oid);
			return result.equals("1");
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public HashMap<String, Variable> getUEShowLinkTable() {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellUeStatusEntry");
		return snmp.SnmpWalk(oid);
	}

	public int getSmonThresholdsCriticalMin(int entry) {
		String oid = MibReader.getInstance().resolveByName("asLteStkSmonThresholdsCriticalMin") + "." + entry;
		try {
			return Integer.valueOf(snmp.get(oid));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return GeneralUtils.ERROR_VALUE;
	}

	public boolean setSmonThresholdsCriticalMin(int entry, int value) {
		String oid = MibReader.getInstance().resolveByName("asLteStkSmonThresholdsCriticalMin") + "." + entry;
		try {
			return snmp.snmpSet(oid, value);
		} catch (Exception e) {
			GeneralUtils.printToConsole("Error setting SmonThresholdsCriticalMni due to: " + e.getMessage());
			report.report("Setting SmonThresholdsCriticalMin failed", Reporter.WARNING);
			e.printStackTrace();
		}
		return false;
	}

	public boolean isPTPLocked() {
		String oid = MibReader.getInstance().resolveByName("asLteStkPtpStatusMasterSyncLocked") + ".1";
		try {
			String response = this.snmp.get(oid);
			if (response.equals("1")) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * @param value
	 * @return true on success
	 */
	public boolean setECIDTimer(Integer value) {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellPosMeasCfgTEcid");
		boolean result = false;
		try {
			result = snmp.snmpSet(oid, value);
		} catch (Exception e) {
			e.printStackTrace();
			result = false;
		}
		return result;
	}

	/**
	 * @return timer value, if an exception was accrued return null
	 */
	public Integer getECIDTimer() {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellPosMeasCfgTEcid");
		try {
			String result = snmp.get(oid);
			return Integer.valueOf(result);
		} catch (Exception e) {
			e.printStackTrace();
			return GeneralUtils.ERROR_VALUE;
		}
	}

	/**
	 * @param value
	 * @return true on success
	 */
	public boolean setCfgAppSource(Integer value) {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellPosMeasCfgAppSource");
		boolean result = false;
		try {
			result = snmp.snmpSet(oid, value);
		} catch (Exception e) {
			e.printStackTrace();
			result = false;
		}
		return result;
	}

	/**
	 * @return Cfg app source, if an exception was accrued return null
	 */
	public Integer getCfgAppSource() {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellPosMeasCfgAppSource");
		try {
			String result = snmp.get(oid);
			return Integer.valueOf(result);
		} catch (Exception e) {
			e.printStackTrace();
			return GeneralUtils.ERROR_VALUE;
		}
	}

	/**
	 * @param value
	 * @return true on success
	 */
	public boolean setPrsPowerOffset(Integer value) {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellPosMeasCfgPrsTxOff");
		boolean result = false;
		try {
			result = snmp.snmpSet(oid, value);
		} catch (Exception e) {
			e.printStackTrace();
			result = false;
		}
		return result;
	}

	/**
	 * @return PrsPowerOffset, if an exception was accrued return null
	 */
	public Integer getPrsPowerOffset() {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellPosMeasCfgPrsTxOff");
		try {
			String result = snmp.get(oid);
			return Integer.valueOf(result);
		} catch (Exception e) {
			e.printStackTrace();
			return GeneralUtils.ERROR_VALUE;
		}
	}

	/**
	 * @param value
	 * @return true on success
	 */
	public boolean setCfgPrsBandwidth(Integer value) {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellPosMeasCfgPrsBandwidth");
		boolean result = false;
		try {
			result = snmp.snmpSet(oid, value);
		} catch (Exception e) {
			e.printStackTrace();
			result = false;
		}
		return result;
	}

	/**
	 * @return CfgPrsBandwidth, if an exception was accrued return null
	 */
	public Integer getCfgPrsBandwidth() {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellPosMeasCfgPrsBandwidth");
		try {
			String result = snmp.get(oid);
			switch (result) {
			case "0":
				return 6;
			case "1":
				return 15;
			case "2":
				return 25;
			case "3":
				return 50;
			case "4":
				return 75;
			case "5":
				return 100;
			default:
				return Integer.parseInt(result);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return GeneralUtils.ERROR_VALUE;
		}
	}

	/**
	 * @param value
	 * @return true on success
	 */
	public boolean setPrsCfgIndex(Integer value) {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellPosMeasCfgPrsCfgIndex");
		boolean result = false;
		try {
			result = snmp.snmpSet(oid, value);
		} catch (Exception e) {
			e.printStackTrace();
			result = false;
		}
		return result;
	}

	/**
	 * @return PrsCfgIndex, if an exception was accrued return null
	 */
	public Integer getPrsCfgIndex() {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellPosMeasCfgPrsCfgIndex");
		try {
			String result = snmp.get(oid);
			return Integer.valueOf(result);
		} catch (Exception e) {
			e.printStackTrace();
			return GeneralUtils.ERROR_VALUE;
		}
	}

	/**
	 * @param value
	 * @return true on success
	 */
	public boolean setNumConsecutivePrs(Integer value) {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellPosMeasCfgNumConsecutivePrs");
		boolean result = false;
		try {
			result = snmp.snmpSet(oid, value);
		} catch (Exception e) {
			e.printStackTrace();
			result = false;
		}
		return result;
	}

	/**
	 * @return PrsCfgIndex, if an exception was accrued return null
	 */
	public Integer getNumConsecutivePrs() {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellPosMeasCfgNumConsecutivePrs");
		try {
			String result = snmp.get(oid);
			return Integer.valueOf(result);
		} catch (Exception e) {
			e.printStackTrace();
			return GeneralUtils.ERROR_VALUE;
		}
	}

	/**
	 * @param value
	 * @return true on success
	 */
	public boolean setCfgPrsMutePeriod(Integer value) {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellPosMeasCfgPrsMutePeriod");
		boolean result = false;
		try {
			result = snmp.snmpSet(oid, value);
		} catch (Exception e) {
			e.printStackTrace();
			result = false;
		}
		return result;
	}

	/**
	 * @return CfgPrsMutePeriod, if an exception was accrued return null
	 */
	public Integer getCfgPrsMutePeriod() {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellPosMeasCfgPrsMutePeriod");
		try {
			String result = snmp.get(oid);
			return Integer.valueOf(result);
		} catch (Exception e) {
			e.printStackTrace();
			return GeneralUtils.ERROR_VALUE;
		}
	}

	/**
	 * @param value
	 * @return true on success
	 */
	public boolean setCfgPrsMutePattSeq(Integer value) {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellPosMeasCfgPrsMutePattSeq");
		boolean result = false;
		try {
			result = snmp.snmpSet(oid, value);
		} catch (Exception e) {
			e.printStackTrace();
			result = false;
		}
		return result;
	}

	/**
	 * @return CfgPrsMutePattSeq, if an exception was accrued return null
	 */
	public Integer getCfgPrsMutePattSeq() {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellPosMeasCfgPrsMutePattSeq");
		try {
			String result = snmp.get(oid);
			return Integer.valueOf(result);
		} catch (Exception e) {
			e.printStackTrace();
			return GeneralUtils.ERROR_VALUE;
		}
	}

	public int getPfsType() {
		String oid = MibReader.getInstance().resolveByName("asLteStkVoLteCfgPfsType");
		try {
			return Integer.valueOf(snmp.get(oid));
		} catch (Exception e) {
			report.report("Error getting Pfs type", Reporter.WARNING);
			e.printStackTrace();
		}
		return GeneralUtils.ERROR_VALUE;
	}

	public boolean setPfsType(int value) {
		String oid = MibReader.getInstance().resolveByName("asLteStkVoLteCfgPfsType");
		try {
			return snmp.snmpSet(oid, value);
		} catch (Exception e) {
			report.report("Setting Pfs type failed", Reporter.WARNING);
			e.printStackTrace();
		}
		return false;
	}

	public boolean setSheddingMode(int value) {
		String oid = MibReader.getInstance().resolveByName("asLteStkLoadSheddingCfgLoadSheddingMode");
		try {
			return snmp.snmpSet(oid, value);
		} catch (Exception e) {
			report.report("Setting shedding mode failed", Reporter.WARNING);
			e.printStackTrace();
		}
		return false;
	}

	public boolean setQci1And2ActivityTimer(int value) {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellCfgQci1and2InactivityTimer");
		try {
			return snmp.snmpSet(oid, value);
		} catch (Exception e) {
			report.report("Setting qci 1 and 2 activity mode failed", Reporter.WARNING);
			e.printStackTrace();
		}
		return false;
	}

	public HashMap<String, Integer> getDLPer() {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellUeLinkStatusDlPer");
		HashMap<String, Variable> temp = new HashMap<>();
		HashMap<String, Integer> ans = new HashMap<>();
		temp = snmp.SnmpWalk(oid);
		ans = MapConverter(temp);
		return ans;
	}

	public HashMap<String, Integer> getULCrcPer() {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellUeLinkStatusUlCrcPer");
		HashMap<String, Variable> temp = new HashMap<>();
		HashMap<String, Integer> ans = new HashMap<>();
		temp = snmp.SnmpWalk(oid);
		ans = MapConverter(temp);
		return ans;
	}

	private HashMap<String, Integer> MapConverter(HashMap<String, Variable> oldMap) {
		HashMap<String, Integer> ret = new HashMap<String, Integer>();
		for (String key : oldMap.keySet()) {
			ret.put(key, oldMap.get(key).toInt());
		}
		return ret;
	}

	public String getSonCfgPciAuto() {
		return getSonCfgPciAuto(40);
	}

	public String getSonCfgPciAuto(int cellId) {
		String oid = MibReader.getInstance().resolveByName("asLteStkSonCfgPciAuto");
		oid += ("." + cellId);
		try {
			String snmpStr;
			for (int i = 1; i < 4; i++) {
				GeneralUtils.printToConsole("Trying to get " + oid + " for the " + i + " time.");
				snmpStr = snmp.get(oid);
				if (!snmpStr.equals(String.valueOf(GeneralUtils.ERROR_VALUE)))
					return snmpStr;
				GeneralUtils.printToConsole("Failed to get " + oid + " for the " + i + " time.");
			}
		} catch (Exception e) {
			GeneralUtils.printToConsole("Error getting getSonCfgPciAuto: " + e.getMessage());
			e.printStackTrace();
		}
		return String.valueOf(GeneralUtils.ERROR_VALUE);
	}

	public boolean setSonCfgPciAuto(boolean value) {
		return setSonCfgPciAuto(40, value);
	}

	public boolean setSonCfgPciAuto(int cellId, boolean value) {
		String oid = MibReader.getInstance().resolveByName("asLteStkSonCfgPciAuto");
		oid += ("." + cellId);
		try {
			return snmp.snmpSet(oid, GeneralUtils.booleanToInteger(value));
		} catch (Exception e) {
			GeneralUtils.printToConsole("Error getting getSonCfgPciAuto: " + e.getMessage());
			e.printStackTrace();
		}
		return false;
	}

	public ArrayList<String> getDefaultGateWayAddresses() {
		ArrayList<String> result = new ArrayList<String>();
		String oid = MibReader.getInstance().resolveByName("asLteStkExternalIpInterfaceStatusGatewayAddress");
		HashMap<String, Variable> snmpResult = snmp.SnmpWalk(oid);
		for (Variable var : snmpResult.values()) {
			result.add(var.toString());
		}

		return result;
	}

	public ArrayList<String> getExternalSubNetMasks() {
		ArrayList<String> result = new ArrayList<String>();
		String oid = MibReader.getInstance().resolveByName("asLteStkStackCfgDataSubnetMaskAddr");
		HashMap<String, Variable> snmpResult = snmp.SnmpWalk(oid);
		for (Variable var : snmpResult.values()) {
			result.add(var.toString());
		}
		return result;
	}

	public String getExternalSubNetCIDR() {
		String oid = MibReader.getInstance().resolveByName("asLteStkExternalIpInterfaceStatusSubnetCidr");
		try {
			return snmp.get(oid);
		} catch (Exception e) {
			GeneralUtils.printToConsole("Error getting CIDR with SNMP: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	public ArrayList<String> getVlanIds() {
		ArrayList<String> result = new ArrayList<String>();
		String oid = MibReader.getInstance().resolveByName("asLteStkIpInterfaceStatusVlanId");
		HashMap<String, Variable> snmpResult = snmp.SnmpWalk(oid);
		for (Variable var : snmpResult.values()) {
			result.add(var.toString());
		}
		return result;
	}

	public void showLoginStatus() {
		sessionManager.showLoginStatus();
	}

	public boolean setGranularityPeriod(int value) {
		String oid = MibReader.getInstance().resolveByName("asLteStatsIfControlRbStatsGranularityPeriod");
		try {
			return snmp.snmpSet(oid, value);
		} catch (Exception e) {
			GeneralUtils.printToConsole("Error setting Granularity Period: " + e.getMessage());
			e.printStackTrace();
		}
		return false;
	}

	public int getGranularityPeriod() {
		String oid = MibReader.getInstance().resolveByName("asLteStatsEnbControlStatsGranularityPeriod");
		String response = "";
		try {
			response = snmp.get(oid);
		} catch (Exception e) {
			GeneralUtils.printToConsole("Error getting Granularity Period: " + e.getMessage());
			e.printStackTrace();
		}
		try {
			if (response != null && !response.equals(String.valueOf(GeneralUtils.ERROR_VALUE))) {
				return Integer.valueOf(response);
			}
		} catch (Exception e) {
			GeneralUtils.printToConsole("Error parsing to integer Granularity Period: " + e.getMessage());
			e.printStackTrace();
		}
		return 5;
	}

	public int getActiveUEPerQciAndTransmitDirection(TransmitDirection transmit, Character qci) {
		String oid = "";
		if (transmit == TransmitDirection.DL) {
			oid = MibReader.getInstance().resolveByName("asLteStatsOnDemandPerQciUeActiveDl");
		} else if (transmit == TransmitDirection.UL) {
			oid = MibReader.getInstance().resolveByName("asLteStatsOnDemandPerQciUeActiveUl");
		} else {
			GeneralUtils.printToConsole("Transmit direction should be UL or DL");
			return 0;
		}
		HashMap<String, org.snmp4j.smi.Variable> output = snmp.SnmpWalk(oid);
		if (output.keySet().size() == 0) {
			return GeneralUtils.ERROR_VALUE;
		}
		int answer = 0;
		try {
			for (String key : output.keySet()) {
				if (qci.equals(key.charAt(key.length() - 1))) {
					answer += Integer.valueOf(output.get(key).toString());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return answer;
	}

	public Boolean getNumberOfUELinkStatusVolte() {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellUeLinkStatusVolteCalls");
		HashMap<String, org.snmp4j.smi.Variable> output = snmp.SnmpWalk(oid);
		if (output.isEmpty()) {
			return null; // GeneralUtils.ERROR_VALUE;
		}
		// int answer = 0;
		try {
			for (String key : output.keySet()) {
				if ("0".equals(output.get(key).toString())) {
					return false;
				}
				// answer += Integer.valueOf(output.get(key).toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	public HashMap<String, Integer> getUELinkStatusVolteTable() {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellUeLinkStatusVolteCalls");
		HashMap<String, org.snmp4j.smi.Variable> output = snmp.SnmpWalk(oid);
		HashMap<String, Integer> ans = new HashMap<>();
		ans = MapConverter(output);
		return ans;
	}

	public Boolean getNumberOfUELinkStatusEmergency() {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellUeLinkStatusEmergencyCalls");
		HashMap<String, org.snmp4j.smi.Variable> output = snmp.SnmpWalk(oid);
		if (output.isEmpty()) {
			return null; // GeneralUtils.ERROR_VALUE;
		}
		// int answer = 0;
		try {
			for (String key : output.keySet()) {
				if ("0".equals(output.get(key).toString())) {
					return false;
				}
				// answer += Integer.valueOf(output.get(key).toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	public HashMap<String, Integer> getUELinkStatusEmergencyTable() {
		String oid = MibReader.getInstance().resolveByName("asLteStkCellUeLinkStatusEmergencyCalls");
		HashMap<String, org.snmp4j.smi.Variable> output = snmp.SnmpWalk(oid);
		HashMap<String, Integer> ans = new HashMap<>();
		ans = MapConverter(output);
		return ans;
	}

	public void createSerialCom(String serialIP, Integer port) {
		if (serialIP == null || port == null)
			return;

		serialCom = new MoxaCom();
		serialCom.setComName(serialIP);
		serialCom.setName(serialIP);
		serialCom.setPort(port);
	}

	public EnabledStates getOperateBehindHenbGw() {
		String oid = MibReader.getInstance().resolveByName("asLteStkStackCfgOperateBehindHenbGw");
		try {
			return snmp.get(oid).equals("1") ? EnabledStates.ENABLED : EnabledStates.DISABLED;
		} catch (Exception e) {
			GeneralUtils.printToConsole("Error getting OperateBehindHenbGw: " + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	public boolean getLoggerDebugCapEnable() {
		String oid = MibReader.getInstance().resolveByName("asLteStkDebugStatusLoggerStatus");
		try {
			return snmp.get(oid).equals("1");
		} catch (Exception e) {
			GeneralUtils.printToConsole("Error getting LoggerDebugCapEnable: " + e.getMessage());
			e.printStackTrace();
		}
		return false;
	}

	public boolean setLoggerDebugCapEnable(boolean enable) {
		String oid = MibReader.getInstance().resolveByName("asLteStkDebugCfgLoggerAdmin");
		try {
			return snmp.snmpSet(oid, enable ? 1 : 0);
		} catch (Exception e) {
			GeneralUtils.printToConsole("Error getting LoggerDebugCapEnable: " + e.getMessage());
			e.printStackTrace();
		}
		return false;
	}

}
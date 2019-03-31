package EnodeB.Components.XLP;

import EnodeB.Components.Cli.Cli;
import EnodeB.Components.EnodeBComponent;
import EnodeB.Components.Session.SessionManager;
import EnodeB.EnodeB;
import EnodeB.EnodeBUpgradeServer;
import EnodeB.PhyState;
import EnodeB.ProtoBuf.PbLteStatusOuterClass.*;
import Entities.ITrafficGenerator.TransmitDirection;
import Netspan.API.Enums.*;
import Netspan.API.Lte.RFStatus;
import Netspan.Profiles.NetworkParameters.Plmn;
import Utils.*;
import Utils.GeneralUtils.CellIndex;
import Utils.Snmp.MibReader;
import jsystem.framework.IgnoreMethod;
import jsystem.framework.report.Reporter;
import jsystem.framework.system.SystemManagerImpl;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.Variable;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The Class XLP.
 */
public abstract class XLP extends EnodeBComponent {

    // Sotware download status Codes
    public enum SwStatus {
        SW_STATUS_IDLE(swStatusIdle),
        SW_STATUS_INSTALL_IN_PROGRESS(swStatusInstallInProgress),
        SW_STATUS_ACTIVATION_IN_PROGRESS(swStatusActivationInProgress),
        SW_STATUS_INSTALL_FAILURE(swStatusInstallFailure),
        SW_STATUS_ACTIVATION_FAILURE(swStatusActivationFailure),
        SW_STATUS_ILLEGAL_VALUE("swStatusIllegalValue");

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
    public static final String[] UE_SHOW_LINK_HEADERS = new String[]{"index", "crnti", "PHY-CRC", "%CRC", "DL-CQI",
            "MCS", "RI", "UL", "C2I", "RSSI", "TO", "UL", "DL", "measGap", "SFR-DL", "SFR-UL", "SFR-CQI"};

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

    public abstract boolean deleteAnrNeighborsBySNMP();

    private static int numOfCells = 0;

    /**
     * (non-Javadoc)
     *
     * @see EnodeB.Components.EnodeBComponent# & connecting to
     * serialCom. then connecting via SSH, if fails get the serialCom ,
     * insure that the XLP is in running state.
     */
    public void init() throws Exception {
        setUserNameAndPassword();
        // init & connecting to serialCom. then connecting via SSH, if fails get
        // the serialCom
        super.init();

        try {
            XLP.setUpgradeServerIp();
        } catch (Exception e) {
            report.report("The UpgradeServer System Object is not define correctly", Reporter.FAIL);
            throw e;
        }
    }

    /**
     * Prints EnodeB version to the Jenkins Console.
     *
     * @throws Exception -Exception
     */
    public void printVersion() throws Exception {
        String version = getVersion();
        if (version != null)
            GeneralUtils.printToConsole(String.format("%s(ver:%s) initialized!", getHardwareName(), version));
        else
            throw new Exception("Initialization error: Can't get " + getHardwareName() + " version.");
    }

    protected void setUserNameAndPassword() {
        if (getUsername() == null) {
            setUsername(getDefaultSSHUsername());
        }
        if (!getUsername().equals(getDefaultSSHUsername())) {
            report.report("SSH username in SUT (" + getUsername() + ") is different from default for this version (" + getDefaultSSHUsername() + ")", Reporter.WARNING);
        }
        setPassword(getMatchingPassword(getUsername()));


        if (getSerialUsername() == null) {
            setSerialUsername(getDefaultSerialUsername());
        }
        if (!getSerialUsername().equals(getDefaultSerialUsername())) {
            report.report("Serial username in SUT (" + getSerialUsername() + ") is different from default for this version (" + getDefaultSerialUsername() + ")", Reporter.WARNING);
        }
        setSerialPassword(getMatchingPassword(getSerialUsername()));
        setSerialSwitchUserCommand(getMatchingSwitchUserCommand(getSerialUsername()));
    }

    private String getMatchingPassword(String username) {
        String ans = "";
        switch (username) {
            case "root":
                ans = UNSECURED_PASSWORD;
                break;
            case "admin":
                ans = ADMIN_PASSWORD;
                break;
            case "op":
                ans = SECURED_PASSWORD;
                break;
            default:
                GeneralUtils.printToConsole("Unrecognised username: " + username + ". using admin password as default.");
                ans = ADMIN_PASSWORD;
                break;
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

    protected abstract String getDefaultSSHUsername();

    protected abstract String getDefaultSerialUsername();

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
                new String[]{"su -", EnodeBComponent.ADMIN_PASSWORD, "/bs/lteCli"}, "quit");
        cli.addPrompt(SHELL_PROMPT_OP, SHELL_PROMPT, new String[]{"su -", EnodeBComponent.ADMIN_PASSWORD}, "exit");
    }

    /**
     * Db get.
     *
     * @param table the table
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
     * @param table the table
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
     * @param output the output
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
     * @param table the table
     * @param key   the key
     * @param row   the row
     * @param value the value
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
     * @param table  the table
     * @param Key1   the key
     * @param Key2   the row
     * @param values the value
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
     * @param table the table
     * @param key   the key
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
     * @param table the table
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
        String[][] tableResult = normlizedTable.toArray(new String[][]{});
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
     * @param command the command
     * @return the string
     */
    public String lteCli(String command) {
        return lteCliWithResponse(command, "");
    }

    /**
     * Lte cli.
     *
     * @param command  the command
     * @param response the response to wait for
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
                GeneralUtils.printToConsole(String.format("Unrecognized phy state \"%s\". Please tell automation programmer to add this state if needed.\n Setting phy state to \"Unresponsive\".",
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
     * int)
     */
    @Override
    public void setSessionLogLevel(String sessionName, int level) {
        sendCommandsOnSession(sessionName, LTE_CLI_PROMPT, "logger threshold set client=* process=* cli=" + String.valueOf(level), LTE_CLI_PROMPT);
    }

    public void setSessionLogLevel(String client, String process, int level) {
        sendCommandsOnSession(SessionManager.SSH_SESSION_NAME, LTE_CLI_PROMPT, String.format("logger threshold set client=%s process=%s cli=%s", client, process, String.valueOf(level)), LTE_CLI_PROMPT);
    }

    /**
     * set SSH Session Log Level - cli
     *
     * @see EnodeB.Components.EnodeBComponent#setSessionLogLevel
     * @param sessionName the session name
     * @param client - client
     * @param process - process
     * @param level       the level
     */
    @Override
    public void setSessionLogLevel(String sessionName,String client, String process, int level) {
		long threadId = Thread.currentThread().getId();
		GeneralUtils.printToConsole("**DEBUG2: Thread ID# " + threadId + " is doing this task");
		GeneralUtils.printToConsole("**DEBUG: " +sessionName+" "+client+" "+process+" "+String.valueOf(level));
//        StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
//        int stackLength = stacktrace.length;
//        for (int i = 0; i < stackLength ; i++) {
//            if (stacktrace[i] != null) {
//                GeneralUtils.printToConsole(String.valueOf(stacktrace[i]));
//            }
//        }
        sendCommandsOnSession(sessionName, LTE_CLI_PROMPT, String.format("logger threshold set client=%s process=%s cli=%s", client, process, String.valueOf(level)), LTE_CLI_PROMPT);
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
     * @param hardwareName the new hardware name
     */
    @IgnoreMethod
    public void setHardwareName(String hardwareName) {
        this.hardwareName = hardwareName;
    }

    public abstract boolean addNbr(EnodeB enodeB, EnodeB neighbor, HoControlStateTypes hoControlStatus,
                                   X2ControlStateTypes x2ControlStatus, HandoverType HandoverType, boolean isStaticNeighbor,
                                   String qOffsetRange) throws IOException;

    public abstract boolean add3PartyNbrs(int numberOfNbr, final long mnoBroadcastPlmn, long startEutranCellId,
                                          Integer enbType_optional, Integer qOffsetCell_optional) throws IOException;//possible to set null to the optional fields.

    public abstract boolean remove3PartyNbrs(int numberOfNbr, final long mnoBroadcastPlmn, long startEutranCellId)
            throws IOException;

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

    /**
     * @param enodeB
     * @param neighbor
     * @param hoControlStatus
     * @param x2ControlStatus
     * @param HandoverType
     * @param isStaticNeighbor
     * @param qOffsetRange
     * @return
     * @throws IOException
     */
    public abstract boolean verifyNbrList(EnodeB enodeB, EnodeB neighbor, HoControlStateTypes hoControlStatus,
                                          X2ControlStateTypes x2ControlStatus, HandoverType HandoverType, boolean isStaticNeighbor,
                                          String qOffsetRange) throws IOException;

    public abstract boolean verifyNbrList(EnodeB neighbor) throws IOException;

    public abstract boolean verifyAnrNeighbor(EnodeB neighbor);

    public abstract String getTriggerX2Ho(EnodeB neighbor);

    public abstract boolean deleteNeighborBySNMP(EnodeB neighbor) throws IOException;

    public abstract boolean deleteAllNeighborsByCli();

    public abstract String[] getNeighbors();

    /**
     * @param neighbor
     * @return
     * @throws IOException
     */
    public abstract void updatePLMNandEutranCellID(EnodeB neighbor) throws IOException;

    public abstract boolean verifyNoNeighbors();

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

    public abstract boolean setANRState(SonAnrStates anrState);

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


    public boolean downloadSWCli(String fileToInstall, ServerProtocolType downloadType, String username, String password) {
        String changeDir = "cd /bs/bin";
        String credentials = "";
        if (downloadType == ServerProtocolType.SFTP) {
            credentials = username + " " + password;
        }
        String downloadCommand = "./software_upgrade.sh " + downloadType.toString().toLowerCase() + " " + upgradeServerIp + " " + fileToInstall + " " + credentials;
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
        //long timeout = EnodeB.DOWNLOAD_TIMEOUT; // 30 minutes
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
                report.report(getName() + " - Got bad state from SNMP: State= " + downloadStatus + " Status String= " + statusString,
                        Reporter.WARNING);
                return false;

            }

            if (!precent.equals("100") && downloadStatus.equals(swStatusIdle) && !precent.trim().equals("")) {
                if (notHundredButIdle) {
                    notHundredButIdle = false;
                    GeneralUtils.unSafeSleep(10 * 1000);
                    continue;
                }
                report.report(getName() + " - Software download failed while in process Download Precent was != 100 but status is Idle",
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
        String statusStringOid = MibReader.getInstance().resolveByName("asMaxBsCmSwStatusString") + "." + SWTypeInstnace;

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
        if ((swStatus == SwStatus.SW_STATUS_ACTIVATION_IN_PROGRESS) || (precent.equals("100") && downloadStatus.equals(swStatusIdle))) {
            report.report(getName() + " - The download process passed successfully via SNMP", Reporter.PASS);
            result = new Pair<Boolean, SwStatus>(true, swStatus);
        } else if ((swStatus == SwStatus.SW_STATUS_ACTIVATION_FAILURE) || (swStatus == SwStatus.SW_STATUS_INSTALL_FAILURE)) {
            statusString = snmp.get(statusStringOid).trim();
            report.report(getName() + " - Got bad state from SNMP: State= " + swStatus + " Status String= " + statusString,
                    Reporter.WARNING);
            result = new Pair<Boolean, SwStatus>(false, swStatus);
        } else if (!precent.equals("100") && swStatus == SwStatus.SW_STATUS_IDLE && !precent.trim().equals(String.valueOf(GeneralUtils.ERROR_VALUE))) {
            report.report(getName() + " - Software download failed while in process Download Precent was != 100 but status is Idle",
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

    /**
     * get Mcc \ get Mnc
     *
     * @param mccOrMnc - String "Mcc" or "Mnc"
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
        //No MIB for this, so getting it according to band number
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
     * wait For All Running And In Service
     * Rhe EnodeB is being sampled repeatedly, in order to check its state
     *
     * @param timeout - (milliSeconds) While this time, the EnodeB is being sampled repeatedly, in order to check its state.
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
        String counters = MibReader.getInstance().resolveByName(valueName);
        int returnValue = 0;
        String temp;

        for (int i = 0; i < 3; i++) {
            returnValue = 0;

            HashMap<String, Variable> countersValues = snmp.SnmpWalk(counters);

            if (countersValues == null) {
                report.report("Cannot get Neighbor table Using SNMP", Reporter.WARNING);
                return 0;
            }

            for (Variable count : countersValues.values()) {
                temp = "" + count;
                returnValue += Integer.parseInt(temp);
            }
            if (returnValue != 0)
                return returnValue;
        }
        return returnValue;
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
        String swCommandSwapToPrimary = MibReader.getInstance().resolveByName("asMaxBsCmSwCfgCommand") + "." + SWTypeInstnace;
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
     * successfully managed to change value.
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
     * Summing of the walk method with the MIB
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
     * Summing of the walk method with the MIB
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
     * @param enable    values and setting them
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
            report.report("Couldn't get the txpower (asLteStkCellCfgTxPower MIB return illegal value - " + txpower + ").",
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
            newRFStatus.ActualTxPowerDbm = GeneralUtils.tryParseStringToFloat(this.snmp.get(oidTxPower, index)) / 100.0f;
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
         * GeneralUtils.printToConsole("mnc_status:" + mnc_status); e.printStackTrace();
         * return false; }
         */

        return mcc_status && mnc_status;
    }

    public boolean deletAllRowsInTable(String rowStatusOID) throws IOException {
        return snmp.deletAllRowsInTable(rowStatusOID);
    }

    public boolean addRowInTable(String rowStatusOid, HashMap<String, String> params) throws IOException {
        return snmp.addRowInTable(rowStatusOid, params);
    }

    public abstract boolean resetCounter(String tableName, String index, HashMap<String, String> KeyValuePairs);

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
        GeneralUtils.printToConsole("Check if Bank was swapped on " + this.getIpAddress() + " expected ver:" + testerVer);
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
            GeneralUtils.printToConsole("Couldn't get the downlink frequency (couldn't get asLteStkCellCfgDownlinkFreq MIB).");
            return 0;
        }
        try {
            return Integer.parseInt(freq);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            report.report("Couldn't get the downlink frequency (asLteStkCellCfgDownlinkFreq MIB return illegal value - " + freq + ").",
                    Reporter.WARNING);
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

        HashMap<String, Variable> mccWalk = this.snmp.SnmpWalk(mccOID);
        HashMap<String, Variable> mncWalk = this.snmp.SnmpWalk(mncOID);
        Object[] mccs = mccWalk.values().toArray();
        Object[] mncs = mncWalk.values().toArray();

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
        report.report("getNrtChanges Unimplemented method", Reporter.WARNING);
        return GeneralUtils.ERROR_VALUE;
    }

    public int getCounterStats(String valueName) {
        report.report("getCounterStats Unimplemented method", Reporter.WARNING);
        return GeneralUtils.ERROR_VALUE;
    }

    public int getCounterStatus(String valueName) {
        report.report("getCounterStatus Unimplemented method", Reporter.WARNING);
        return GeneralUtils.ERROR_VALUE;
    }

    public PbLteAnrStatus getPbLteSAnrStatus(long mnoBroadcastPlmn, long eutranCellId) {
        report.report("PbLteAnrStatus Unimplemented method", Reporter.WARNING);
        return null;
    }

    public int getCellChanges() {
        report.report("getCellChanges Unimplemented method", Reporter.WARNING);
        return GeneralUtils.ERROR_VALUE;
    }

    public int getMmeChanges() {
        report.report("getMmeChanges Unimplemented method", Reporter.WARNING);
        return GeneralUtils.ERROR_VALUE;
    }

    public int getNetworkElementChanges() {
        report.report("getNetworkElementChanges Unimplemented method", Reporter.WARNING);
        return GeneralUtils.ERROR_VALUE;
    }

    public int getRfChanges() {
        report.report("getRfChanges Unimplemented method", Reporter.WARNING);
        return GeneralUtils.ERROR_VALUE;
    }

    public int getSgwChanges() {
        report.report("getSgwChanges Unimplemented method", Reporter.WARNING);
        return GeneralUtils.ERROR_VALUE;
    }

    public boolean setNrtChanges(int value) {
        report.report("setNrtChanges Unimplemented method", Reporter.WARNING);
        return false;
    }

    public boolean setCellChanges(int value) {
        report.report("setCellChanges Unimplemented method", Reporter.WARNING);
        return false;
    }

    public boolean setSgwChanges(int value) {
        report.report("setSgwChanges Unimplemented method", Reporter.WARNING);
        return false;
    }

    public boolean setRfChanges(int value) {
        report.report("setRfChanges Unimplemented method", Reporter.WARNING);
        return false;
    }

    public boolean setNetworkElementChanges(int value) {
        report.report("setNetworkElementChanges Unimplemented method", Reporter.WARNING);
        return false;
    }

    public boolean setMmeChanges(int value) {
        report.report("setMmeChanges Unimplemented method", Reporter.WARNING);
        return false;
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
        report.report("getPbLteCellStatus Unimplemented method", Reporter.WARNING);
        return null;
    }

    public PbLteNwElementStatus getPbLteNetworkElementStatus(int networkType) {
        report.report("getPbLteNetworkElementStatus Unimplemented method", Reporter.WARNING);
        return null;
    }

    public PbLteMmeStatus getPbLteMmeStatus(String ipAddress) {
        report.report("getPbLteMmeStatus Unimplemented method", Reporter.WARNING);
        return null;
    }

    public PbLteSgwStatus getPbLteSgwStatus(String ipAddress) {
        report.report("getPbLteSgwStatus Unimplemented method", Reporter.WARNING);
        return null;
    }

    public PbLteRfStatus getPbLteRfStatus(int rfPathId) {
        report.report("getPbLteRfStatus Unimplemented method", Reporter.WARNING);
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
        String ans = snmp.get(oid + ".3");//check s1u interface type
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
        HashMap<String, Variable> hm = snmp.SnmpWalk(MibReader.getInstance().resolveByName("asLteStkLedDrvPatternEntry"));
        int[] ledStatusValues = new int[11];
        String[] mibNames = {
                "asLteStkLedDrvPatternPowerLed",
                "asLteStkLedDrvPatternBhGreenLed",
                "asLteStkLedDrvPatternBhRedLed",
                "asLteStkLedDrvPatternWanGreenLed",
                "asLteStkLedDrvPatternWanRedLed",
                "asLteStkLedDrvPatternLteGreenLed",
                "asLteStkLedDrvPatternLteRedLed",
                "asLteStkLedDrvPatternUeSignalFirstLed",
                "asLteStkLedDrvPatternUeSignalSecondLed",
                "asLteStkLedDrvPatternUeSignalThirdLed",
                "asLteStkLedDrvPatternUeSignalFourthLed"
        };

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
        String oid = MibReader.getInstance().resolveByName("asLteStkCellCfgPktFwdEnable") + "." + String.valueOf(cellId + 39);
        String snmpValue = (value) ? "1" : "0";
        if (snmp.snmpSet(oid, snmpValue)) {
            return true;
        } else {
            report.report("setting PktFwdEnable failed", Reporter.WARNING);
            return false;
        }
    }

    public int getPacketForwardingEnable(int cellId) {
        String oid = MibReader.getInstance().resolveByName("asLteStkCellCfgPktFwdEnable") + "." + String.valueOf(cellId + 39);
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
        String oid = MibReader.getInstance().resolveByName("asLteStkCellTpmCfgMroIntraFreqRsrpThreshServCell") + "." + cellIndex.value;
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
        String oid = MibReader.getInstance().resolveByName("asLteStkCellTpmCfgMroIntraFreqRsrpThreshNghCell") + "." + cellIndex.value;
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
        String oid = MibReader.getInstance().resolveByName("asLteStkCellTpmStatusIntraHoTooLatePoorCovrg") + "." + cellIndex.value;
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
        String oid = MibReader.getInstance().resolveByName("asLteStkCellTpmStatusIntraHoTooLateGoodCovrgUnprepared") + "." + cellIndex.value;
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
        String oid = MibReader.getInstance().resolveByName("asLteStkCellTpmStatusOutgoingIntraFreqHoAttempt") + "." + cellIndex.value;
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
        String oid = MibReader.getInstance().resolveByName("asLteStkCellTpmStatusReEstabSameCellPoorCovrg") + "." + cellIndex.value;
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
     * enable = true
     * disable = false
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
     * enable = true
     * disable = false
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

    public abstract boolean setGranularityPeriod(int value);

    public int getGranularityPeriod() {
        String oid = MibReader.getInstance().resolveByName("asLteStatsIfControlRbStatsGranularityPeriod");
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
            return null; //GeneralUtils.ERROR_VALUE;
        }
        //int answer = 0;
        try {
            for (String key : output.keySet()) {
                if ("0".equals(output.get(key).toString())) {
                    return false;
                }
                //answer += Integer.valueOf(output.get(key).toString());
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
            return null; //GeneralUtils.ERROR_VALUE;
        }
        //int answer = 0;
        try {
            for (String key : output.keySet()) {
                if ("0".equals(output.get(key).toString())) {
                    return false;
                }
                //answer += Integer.valueOf(output.get(key).toString());
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
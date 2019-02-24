package testsNG.Actions;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import EnodeB.AirVelocity;
import EnodeB.EnodeB;
import EnodeB.EnodeBChannelBandwidth;
import Netspan.NetspanServer;
import Netspan.API.Enums.EnbTypes;
import Netspan.API.Enums.HandoverTypes;
import Netspan.API.Enums.HoControlStateTypes;
import Netspan.API.Enums.SonAnrStates;
import Netspan.API.Enums.X2ControlStateTypes;
import Netspan.Profiles.SonParameters;
import Utils.GeneralUtils;
import jsystem.framework.report.ListenerstManager;
import jsystem.framework.report.Reporter;

public class Neighbors {

    private static Neighbors instance;
    public static Reporter report = ListenerstManager.getInstance();
    private static NetspanServer netspanServer = null;
    public static int[] netSpanToCliQoffsetConversionTable = new int[31];

    private Neighbors() {
        buildQoffsetConversionTable();
    }

    public static void buildQoffsetConversionTable() {
        int netSpanValue = -24;
        for (int i = 0; i < netSpanToCliQoffsetConversionTable.length; i++) {
            netSpanToCliQoffsetConversionTable[i] = netSpanValue;
            if (i < 9 || i > 20)// double Jump
                netSpanValue += 1;
            netSpanValue += 1;
        }
    }

    public static int convertQoffsetToCli(int netspanValue) {
        for (int i = 0; i < netSpanToCliQoffsetConversionTable.length; i++) {
            if (netspanValue == netSpanToCliQoffsetConversionTable[i])
                return i;
        }
        return -999;
    }

    public static int convertQoffsetToNetspan(int cliValue) {
        return netSpanToCliQoffsetConversionTable[cliValue];
    }

    public static Neighbors getInstance() {
        if (instance == null) {
            instance = new Neighbors();
            try {
                netspanServer = NetspanServer.getInstance();
            } catch (Exception e) {
                report.report("Netspan Server is unavialable Error: " + e.toString(), Reporter.WARNING);
            }
        }
        return instance;
    }

    /**
	 * Add Neighbor Multi Cell
	 *
     * @param enodeB
     * @param neighbor
     * @param hoControlStatus
     * @param x2ControlStatus
     * @param HandoverTypes
     * @param isStaticNeighbor
     * @param qOffsetRange
     * @return
     */
    public boolean addNeighborMultiCell(EnodeB enodeB, EnodeB neighbor, HoControlStateTypes hoControlStatus,
                               X2ControlStateTypes x2ControlStatus, HandoverTypes HandoverTypes, boolean isStaticNeighbor,
                               String qOffsetRange) {
        //Try via Netspan
        if (netspanServer != null && netspanServer.addNeighbourMultiCell(enodeB, neighbor, hoControlStatus, x2ControlStatus,
                HandoverTypes, isStaticNeighbor, qOffsetRange)) {
            if (verifyAddNeighbourSucceed(enodeB, neighbor, "Netspan")) return true;
        }
        report.report("Didn't find neighbor. Wait 10 seconds and try again");
        GeneralUtils.unSafeSleep(10 * 1000);
        if (verifyAddNeighbourSucceed(enodeB, neighbor, "Netspan")) return true;
        report.report("Netspan add Neighbor " + neighbor.getNetspanName() + " failed", Reporter.WARNING);
        //todo impalement via SNMP (not available now)
        return false;
    }

	/**
	 *  Add Neighbor
	 *
	 * @param enodeB - enodeB
	 * @param neighbor - neighbor
	 * @param hoControlStatus - hoControlStatus
	 * @param x2ControlStatus - x2ControlStatus
	 * @param HandoverTypes - HandoverTypes
	 * @param isStaticNeighbor - isStaticNeighbor
	 * @param qOffsetRange - qOffsetRange
	 * @return - true if succeed
	 */
    public boolean addNeighbor(EnodeB enodeB, EnodeB neighbor, HoControlStateTypes hoControlStatus,
                               X2ControlStateTypes x2ControlStatus, HandoverTypes HandoverTypes, boolean isStaticNeighbor,
                               String qOffsetRange) {
        //Try via Netspan
        if (netspanServer != null && netspanServer.addNeighbor(enodeB, neighbor, hoControlStatus, x2ControlStatus,
                HandoverTypes, isStaticNeighbor, qOffsetRange)) {
            if (verifyAddNeighbourSucceed(enodeB, neighbor, "Netspan")) return true;
        }
        report.report("Didn't find neighbor. Wait 10 seconds and try again");
        GeneralUtils.unSafeSleep(10 * 1000);
        if (verifyAddNeighbourSucceed(enodeB, neighbor, "Netspan")) return true;
        report.report("Netspan add Neighbor " + neighbor.getNetspanName() + " failed", Reporter.WARNING);
        //Try via SNMP
        try {
            enodeB.addNbr(enodeB, neighbor, hoControlStatus, x2ControlStatus, HandoverTypes, isStaticNeighbor,
                    getQOffsetCliCommand(qOffsetRange));
            if (verifyAddNeighbourSucceed(enodeB, neighbor, "SNMP")) return true;
        } catch (Exception e) {
            e.printStackTrace();
            report.report("SNMP add Neighbor  " + neighbor.getNetspanName() + " verification failed");
        }
        return false;
    }

    /**
	 * Get QOffset Cli Command if != null
     *
     * @param qOffsetRange - qOffsetRange
     * @return - QOffset Cli OR null if the user didn't choose to type
     */
    private String getQOffsetCliCommand(String qOffsetRange) {
        if (qOffsetRange != null) {
        	return  String.valueOf(convertQoffsetToCli(Integer.parseInt(qOffsetRange)));
        } else {
        	return null;
        }
    }

    /**
     *  Verify Add Neighbour Succeed
     *
     * @param enodeB - enodeB
     * @param neighbor - neighbor
     * @param protocol - Netspan or SNMP - print to log
     * @return - true if succeed
     */
    private boolean verifyAddNeighbourSucceed(EnodeB enodeB, EnodeB neighbor, String protocol) {
        if (verifyNeighborExistsNSOrSNMP(enodeB, neighbor)) {
            report.report( "Add neighbour via" +  protocol + ":" + neighbor.getNetspanName() + " passed");
            return true;
        }
        return false;
    }

    public boolean addNeighborOnlyNetspan(EnodeB enodeB, EnodeB neighbor, HoControlStateTypes hoControlStatus,
                                          X2ControlStateTypes x2ControlStatus, HandoverTypes HandoverTypes, boolean isStaticNeighbor,
                                          String qOffsetRange) {
        if (netspanServer != null && !netspanServer.addNeighbor(enodeB, neighbor, hoControlStatus, x2ControlStatus,
                HandoverTypes, isStaticNeighbor, qOffsetRange)) {
            report.report("Netspan add Neighbor " + neighbor.getNetspanName() + " failed", Reporter.WARNING);
            return false;
        }
        report.report("Netspan add Neighbor  " + neighbor.getNetspanName() + " verification passed by Netspan");
        return true;
    }

    public Boolean checkCannotAddNeighbor(EnodeB enodeB, EnodeB neighbor, HoControlStateTypes hoControlStatus,
                                          X2ControlStateTypes x2ControlStatus, HandoverTypes HandoverTypes, boolean isStaticNeighbor,
                                          String qOffsetRange) {
        boolean addNeighborFlag = netspanServer.checkCannotAddNeighbor(enodeB, neighbor, hoControlStatus, x2ControlStatus, HandoverTypes, isStaticNeighbor, qOffsetRange);
        if (netspanServer != null && addNeighborFlag) {
            report.report("Netspan add Neighbor " + neighbor.getNetspanName() + " failed");
            return true;
        } else if (netspanServer != null && !addNeighborFlag) {
            report.report("Netspan add Neighbor " + neighbor.getNetspanName() + " verification passed by Netspan", Reporter.WARNING);
            return false;
        } else
            return null;
    }

    public boolean verifyNeighborNMS(EnodeB enodeB, EnodeB neighbor, HoControlStateTypes hoControlStatus, X2ControlStateTypes x2ControlStatus, HandoverTypes HandoverTypes, boolean isStaticNeighbor, String qOffsetRange) {
        return netspanServer.verifyNeighbor(enodeB, neighbor, hoControlStatus, x2ControlStatus, HandoverTypes, isStaticNeighbor, qOffsetRange);
    }

    public boolean verifyNeighbor(EnodeB enodeB, EnodeB neighbor) {
        return netspanServer.verifyNeighbor(enodeB, neighbor);
    }

    public boolean verifyAnrNeighbor(EnodeB enodeB, EnodeB neighbor) {
        return enodeB.verifyAnrNeighbor(neighbor);
    }

    public boolean verifyNeighborParametersNMSandSNMP(EnodeB tempEnodeB, EnodeB tempNeighbor,
                                                      HoControlStateTypes hoControlStatus, X2ControlStateTypes x2ControlStatus, HandoverTypes HandoverTypes,
                                                      boolean isStaticNeighbor, String qOffsetRange) throws NumberFormatException {
        if (!verifyNeighborNMS(tempEnodeB, tempNeighbor, hoControlStatus, x2ControlStatus,
                HandoverTypes, isStaticNeighbor, qOffsetRange)) {
            report.report("Verification failed by Netspan. Trying with SNMP", Reporter.WARNING);
            try {
                if (!tempEnodeB.verifyNbrList(tempEnodeB, tempNeighbor, hoControlStatus, x2ControlStatus,
                        HandoverTypes, isStaticNeighbor, qOffsetRange)) {
                    report.report("Verification failed by SNMP");
                    return false;
                }
            } catch (IOException e) {
                report.report("Couldn't verify neighbors by SNMP");
                e.printStackTrace();
            }
        }
        return true;
    }

    public boolean verifyNeighborParametersOnlySNMP(EnodeB tempEnodeB, EnodeB tempNeighbor,
                                                    HoControlStateTypes hoControlStatus, X2ControlStateTypes x2ControlStatus, HandoverTypes HandoverTypes,
                                                    boolean isStaticNeighbor, String qOffsetRange) throws NumberFormatException, IOException {
        return tempEnodeB.verifyNbrList(tempEnodeB, tempNeighbor, hoControlStatus, x2ControlStatus, HandoverTypes,
                isStaticNeighbor, String.valueOf(convertQoffsetToCli(Integer.parseInt(qOffsetRange))));
    }

    public boolean verifyNeighborExistsNSOrSNMP(EnodeB tempEnodeB, EnodeB tempNeighbor) {
        if (verifyNeighbor(tempEnodeB, tempNeighbor)) {
            return true;
        }
        try {
            if (tempEnodeB.verifyNbrList(tempNeighbor)) {
                return true;
            }
        } catch (IOException e) {
            report.report("couldn't verify neighbor list of " + tempEnodeB.getNetspanName());
            e.printStackTrace();
        }
        return false;
    }

    public boolean verifyNeighborExistsOnlyNetspan(EnodeB tempEnodeB, EnodeB tempNeighbor) throws IOException {
        return verifyNeighbor(tempEnodeB, tempNeighbor);
    }

    public void updatePLMNandEutranCellID(EnodeB enodeB, EnodeB neighbor) throws IOException {
        enodeB.updatePLMNandEutranCellID(neighbor);
    }

    public boolean deleteNeighbor(EnodeB enodeB, EnodeB neighbor) {
        if (netspanServer != null && !netspanServer.deleteNeighbor(enodeB, neighbor)) {
            report.report("Delete Neighbor " + neighbor.getNetspanName() + " with netspan failed", Reporter.WARNING);
            try {
                if (!enodeB.deleteNeighborBySNMP(neighbor)) {
                    report.report("Delete Neighbors " + neighbor.getNetspanName() + " with SNMP failed", Reporter.WARNING);
                } else {
                    report.report("Delete Neighbor " + neighbor.getNetspanName() + " via SNMP passed");
                    return true;
                }
            } catch (IOException e) {
                report.report("Delete Neighbor " + neighbor.getNetspanName() + " via SNMP failed", Reporter.WARNING);
                e.printStackTrace();
            }

        } else {
            report.report("Delete Neighbor " + neighbor.getNetspanName() + " via netspan passed");
            return true;
        }
        return false;
    }

    public boolean deleteAllNeighbors(EnodeB enodeB) {
        if (netspanServer != null && netspanServer.verifyNoNeighbors(enodeB)) {
            report.report("Netspan shows no neighbors for EnodeB " + enodeB.getNetspanName());
            return true;
        }
        if (netspanServer != null && !netspanServer.deleteAllNeighbors(enodeB))
            report.report("Delete all Neighbors with netspan failed for enodeB " + enodeB.getNetspanName(), Reporter.WARNING);

        if (netspanServer != null && !deleteAnrNeighbors(enodeB))
            report.report("Delete Anr Neighbors failed for EnodeB " + enodeB.getNetspanName(), Reporter.WARNING);

        GeneralUtils.unSafeSleep(5 * 1000);

        if (netspanServer != null && !netspanServer.verifyNoNeighbors(enodeB)) {
            report.report("Netspan verification failed for EnodeB " + enodeB.getNetspanName(), Reporter.WARNING);
            if (!enodeB.deleteAllNeighborsByCli()) {
                return false;

            } else if (enodeB.verifyNoNeighbors()) {
                report.report("delete All Neighbors via CLI failed for EnodeB " + enodeB.getNetspanName(), Reporter.WARNING);
                GeneralUtils.reportHtmlLink(enodeB.getName() + ": db get nghList", enodeB.lteCli("db get nghList"));
                return false;
            } else {
                report.report("Verified via CLI that no neighbours exist");
                GeneralUtils.reportHtmlLink(enodeB.getName() + ": db get nghList", enodeB.lteCli("db get nghList"));
                return true;

            }
        } else {
            report.report("Verified via netspan that no neighbours exist for EnodeB " + enodeB.getNetspanName());
            GeneralUtils.reportHtmlLink(enodeB.getName() + ": db get nghList", enodeB.lteCli("db get nghList"));
            return true;
        }
    }

    public boolean changeNbrAutoX2ControlFlag(EnodeB enodeB, boolean state) {
        String output;
        if (netspanServer != null && !netspanServer.changeNbrAutoX2ControlFlag(enodeB, state)) {
            output = (state) ? "enabled" : "disabeled";
            report.report(
                    "Couldn't change the Auto X2 Control Flag to " + output + " for eNodeB " + enodeB.getNetspanName(),
                    Reporter.WARNING);
            return false;
        } else {
            output = (state) ? "enabled" : "disabeled";
            report.report("Changed the Auto X2 Control Flag to " + output + " for eNodeB " + enodeB.getNetspanName());
        }
        return true;
    }

    public boolean changeNbrX2ConfiguratioUpdateFlag(EnodeB enodeB, boolean state) {
        String output;
        if (netspanServer != null && !netspanServer.changeNbrX2ConfiguratioUpdateFlag(enodeB, state)) {
            output = (state) ? "enabled" : "disabeled";
            report.report("Couldn't change the X2 Configuration update Flag to " + output + " for eNodeB "
                    + enodeB.getNetspanName(), Reporter.FAIL);
            return false;
        } else {
            output = (state) ? "enabled" : "disabeled";
            report.report(
                    "Changed the X2 Configuration update Flag to " + output + " for eNodeB " + enodeB.getNetspanName());
        }
        return true;
    }

    public boolean deleteAnrNeighbors(EnodeB enodeB) {
        if (!netspanServer.verifyNoANRNeighbors(enodeB)) {
            if (enodeB.deleteAnrNeighborsBySNMP()) {
                report.report("Delete Anr Neighbors passed.");
            } else {
                return false;
            }
        }
        return true;
    }

    public ArrayList<EnodeB> addingThirdPartyNeihbors(EnodeB enodeB, int NumberOfNeighbors, boolean differentEearfcn, ArrayList<Integer> create) {

        int enbID = 0;
        int mcc;
        int mnc;
        String name;
        String IPAdress;
        ArrayList<EnodeB> neighborsList = new ArrayList<>();
        boolean added = false;
        try {
            InetAddress addr;
            addr = InetAddress.getLocalHost();
            String cmd = addr.getHostAddress();
            String separator = cmd.contains(":") ? ":" : "\\.";
            String[] separated = cmd.split(separator);
            mcc = Integer.parseInt(separated[separated.length - 2]);
            if (mcc == 0)
                mcc++;
            mnc = Integer.parseInt(separated[separated.length - 1]);
            if (mnc == 0)
                mnc++;
            GeneralUtils.printToConsole("3rd Party MCC: " + mcc + ", MNC: " + mnc);
            // report.report("3rd Party MCC: "+mcc+", MNC: "+mnc );
            for (int j = 0; j < NumberOfNeighbors; j++) {
                name = addr.getHostName() + "_" + enbID;
                if(enodeB.getIpAddress().contains(":"))
                	IPAdress = "abcd::" + j + ":99:" + separated[2] + ":" + separated[3];
                else
                	IPAdress = "" + j + ".99." + separated[2] + "." + separated[3];
                // report.report(IPAdress);
                if (differentEearfcn) {
                    added = netspanServer.Create3rdParty(name, IPAdress, 0, 0, 0, j, EnbTypes.MACRO, 1, enbID, create.get(j),
                            EnodeBChannelBandwidth.TenMhz, String.format("%03d", mcc), String.format("%03d", mnc));
                } else {
                    added = netspanServer.Create3rdParty(name, IPAdress, 0, 0, 0, j, EnbTypes.MACRO, 1, enbID, 0,
                            EnodeBChannelBandwidth.TenMhz, String.format("%03d", mcc), String.format("%03d", mnc));
                }
                if (added) {
                    EnodeB neighbor = new AirVelocity();
                    neighbor.setNetspanName(name);
                    neighbor.setName(name);
                    neighbor.setCellIdentity(String.valueOf(j));
                    neighbor.setMcc(String.format("%03d", mcc));
                    neighbor.setMnc(String.format("%03d", mnc));
                    neighborsList.add(neighbor);
                    report.report("3rd party EnodeB " + name + " was added to the Netspan");

                } else {
                    report.report(
                            "3rd party EnodeB " + name + " was NOT added to the enodeB " + enodeB.getNetspanName(),
                            Reporter.WARNING);
                }
                enbID++;
            }
        } catch (UnknownHostException ex) {
            GeneralUtils.printToConsole("Hostname can not be resolved");
        }
        return neighborsList;
    }

    public EnodeB adding3rdPartyNeighbor(EnodeB enodeB, String neihborName, String IPAdress, int pci, int neihborId,
                                         int dlEarfcn) {
        EnodeB neighbor = new AirVelocity();
        boolean added = false;
        added = netspanServer.Create3rdParty(neihborName, IPAdress, pci / 3, pci % 3, 0, neihborId, EnbTypes.MACRO, 1,
                0, dlEarfcn, EnodeBChannelBandwidth.TenMhz, String.format("%03d", 001), String.format("%03d", 01));

        if (added) {
            neighbor.setNetspanName(neihborName);
            neighbor.setCellIdentity(String.valueOf(0));
            neighbor.setMcc(String.format("%03d", 001));
            neighbor.setMnc(String.format("%03d", 01));
            report.report("3rd party EnodeB " + neihborName + " (" + IPAdress + "), PCI=" + pci + ", ID=" + neihborId + ", Earfcn=" + dlEarfcn + " was added to the Netspan");
        } else {
            report.report("3rd party EnodeB " + neihborName + " (" + IPAdress + ") was NOT added to the enodeB " + enodeB.getNetspanName(),
                    Reporter.WARNING);
            return null;
        }
        return neighbor;
    }

    public boolean delete3rdParty(String thirdPartyNeighbor) {

        ArrayList<String> names = new ArrayList<>();
        names.add(thirdPartyNeighbor);
        boolean isPassed = true;
        if (!netspanServer.delete3rdParty(names)) {
            isPassed = false;
            report.report("couldn't delete 3rd party " + thirdPartyNeighbor + " from EnodeB", Reporter.WARNING);
        }
        names.remove(thirdPartyNeighbor);
        return isPassed;
    }

    public boolean delete3rdPartyList(ArrayList<EnodeB> thirdPartyNeighborList) {
        ArrayList<String> names = new ArrayList<>();

        for (EnodeB enb : thirdPartyNeighborList) {
            names.add(enb.getNetspanName());
        }
        if (netspanServer.delete3rdParty(names)) {
            report.report("All 3rd parties were deleted from Netspan");
            return true;
        } else {
            report.report("There was an issue when deleting 3rd parties from netspan");
            return false;
        }

    }

    public void deleteAll3rdParty() throws UnknownHostException {
        String host = InetAddress.getLocalHost().toString();
        String[] hostSplit = host.split("/");
        ArrayList<String> toDelete = new ArrayList<String>();

        List<String> names = netspanServer.getAll3rdParty();
        if (names == null) {
            report.report("Get 3rdParty Nodes Fail via Netspan", Reporter.WARNING);
            return;
        }

        if (names.isEmpty()){
        	report.report("No 3rd party enodeb at all");
            return;
        }

        for (String name : names)
            if (name.contains(hostSplit[0]))
                toDelete.add(name);

        if (toDelete.isEmpty()){
        	report.report("No 3rd party enodeb for this setup");
        	return;
        }
        
        boolean status = netspanServer.delete3rdParty(toDelete);

        if (!status)
            report.report("Delete 3rdParty Nodes " + Arrays.toString(toDelete.toArray()) + " Fail ", Reporter.WARNING);
        else
            report.report("Delete all 3rd parties by Netspan passed");
    }

    public Boolean SetANRState(EnodeB enb, SonAnrStates anrState, ArrayList<Integer> anrFrequencyList,
                               Integer MinAllowedHoSuccessRate) throws IOException {

        report.reportHtml("Before: db get anrCfg", enb.lteCli("db get anrCfg"), true);
        report.report(String.format("%s - Trying to set ANR type %s based.", enb.getName(), anrState));
        SonParameters sonParams = new SonParameters();
        sonParams.setSonCommissioning(true);
        sonParams.setAnrState(anrState);
        sonParams.setAnrFrequencyList(anrFrequencyList);
        sonParams.setMinAllowedHoSuccessRate(MinAllowedHoSuccessRate);
        boolean status = EnodeBConfig.getInstance().cloneAndSetSonProfileViaNetspan(enb,
                enb.defaultNetspanProfiles.getSON(), sonParams);
        if (!status) {
            report.report("Set ANR State with netspan Failed Fall back to SNMP.", Reporter.WARNING);
            status = enb.setAnrState(anrState);
        } else {
            report.report("clone Son Profile Via Netspan Passed.");
        }
        report.report("waiting 10 seconds to stabilize");
        GeneralUtils.unSafeSleep(10000);
        report.reportHtml("After: db get anrCfg", enb.lteCli("db get anrCfg"), true);
        String anrMode = enb.getAnrMode();
        String anrDurationTimer = enb.getAnrDurationTimer();
        report.report(String.format("[INFO]: ANR parameters: \"anrMode\" - %s, \"drxOnDurationTimer\" - %s ", anrMode,
                anrDurationTimer));
        boolean correctState = false;
        if (anrMode.equals("-999") || anrDurationTimer.equals("-999"))
            return null;
        else if (anrState == SonAnrStates.DISABLED && anrMode.equals("0")) {
            correctState = true;
        } else if (anrState == SonAnrStates.HO_MEASUREMENT && anrMode.equals("1")) {
            correctState = true;
        } else if (anrState == SonAnrStates.PERIODICAL_MEASUREMENT && anrMode.equals("2")) {
            correctState = true;
        }

        return status && correctState;
    }
};

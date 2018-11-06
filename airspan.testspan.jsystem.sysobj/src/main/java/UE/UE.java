package UE;

import java.util.ArrayList;

import jsystem.framework.IgnoreMethod;
import jsystem.framework.report.Reporter;
import jsystem.framework.system.SystemObjectImpl;
import EPC.EPC;
import Netspan.Profiles.NetworkParameters.Plmn;
import PowerControllers.PowerController;
import PowerControllers.PowerControllerPort;
import Utils.GeneralUtils;

public abstract class UE extends SystemObjectImpl {
    private static final String POWER_CONTROLLER_PORT_MAPPING_DELIMITER = "/";
    private static final int POWER_CONTROLLER_PORT_MAPPING_CONTROLLER_INDEX = 0;
    private static final int POWER_CONTROLLER_PORT_MAPPING_PORT_INDEX = 1;
    public static final int UE_CONNECT_TIMEOUT = 2 * 60 * 1000;

    private String lanIpAddress;
    private int SNMPPort = 161;
    private int DMToolPort = 7772;
    private String wanIpAddress;
    private String IPerfUlMachine;
    private String IPerfDlMachine;
    private UeState state;
    private String imsi;
    private ArrayList<UeListener> listeners;
    private EPC epc;
    private String powerControllerMapping;
    private ArrayList<String> connectedToEnodeB;
    private int ueCategory = 4;
    private String model = "unknown";
    private String vendor;

    public UE(String vendor) {
        this.vendor = vendor;
    }

    @Override
    public void init() throws Exception {
        super.init();

        state = UeState.unknown;

        listeners = new ArrayList<UeListener>();

        try {
            epc = EPC.getInstance();
        } catch (Exception e) {
            GeneralUtils.printToConsole("Failed to get EPC instance.");
            e.printStackTrace();
        }
        if (epc != null)
            epc.addUE(this);
    }


    @Override
    public void close() {
        if (epc != null)
            epc.removeUE(this);
        super.close();
    }

    @IgnoreMethod
    public String getVendor() {
        return vendor;
    }

    public void addStateChangedListener(UeListener listener) {
        listeners.add(listener);
    }

    public String getLanIpAddress() {
        return lanIpAddress;
    }


    public void setLanIpAddress(String lanIpAddress) {
        this.lanIpAddress = lanIpAddress;
    }


    public void setWanIpAddress(String wanIpAddress) {
        this.wanIpAddress = wanIpAddress;
    }


    public String getWanIpAddress() {
        return wanIpAddress;
    }


    @IgnoreMethod
    public UeState getState() {
        return state;
    }

    @IgnoreMethod
    public void setState(UeState state) {
        if (state != this.state) {
            report.report(String.format("Changed its state from %s to %s", this.state, state));
        }
        for (UeListener listener : listeners)
            listener.UeStateChanged(new UeEvent(this, 2, ""), this.state, state);

        this.state = state;
    }


    public String getImsi() {
        return imsi;
    }


    public void setImsi(String imsi) {
        this.imsi = imsi;
    }

    public String getMCCbyimsi() {
        String IMSI = getImsi();
        String MCC = IMSI.substring(0, 3);
        return MCC;
    }

    public String getMNCbyimsi() {
        String IMSI = getImsi();
        int NumOfChars = 2;
        if (IMSI.length() == 15)
            NumOfChars = 3;
        String MNC = IMSI.substring(3, NumOfChars + 3);
        return MNC;
    }


    public String getPowerControllerMapping() {
        return powerControllerMapping;
    }


    public void setPowerControllerMapping(String powerControllerMapping) {
        this.powerControllerMapping = powerControllerMapping;
    }

    public ArrayList<String> getConnectedToEnodeB() {
        return connectedToEnodeB;
    }


    public void setConnectedToEnodeB(ArrayList<String> connectedToEnodeB) {
        this.connectedToEnodeB = connectedToEnodeB;
    }

    /**
     * Takes the data from the SUT
     *
     * @return - Object from SUT
     */
    @IgnoreMethod
    public PowerControllerPort getPowerControllerPort() {
        if (powerControllerMapping != null) {
            try {
                String[] mappingParts = powerControllerMapping.split(POWER_CONTROLLER_PORT_MAPPING_DELIMITER);
                PowerController powerController = (PowerController) system.getSystemObject(mappingParts[POWER_CONTROLLER_PORT_MAPPING_CONTROLLER_INDEX]);
                return powerController.getPort(mappingParts[POWER_CONTROLLER_PORT_MAPPING_PORT_INDEX]);
            } catch (Exception e) {
                GeneralUtils.printToConsole(String.format("Error in getting power controller port from mapping \"%s\".", powerControllerMapping));
                e.printStackTrace();
            }
        }
        return null;
    }


    public String getModel() {
        return model;
    }


    public void setModel(String model) {
        this.model = model;
    }

    /**
     * @return the ueCategory
     */
    public int getUeCategory() {
        return ueCategory;
    }

    /**
     * @param ueCategory the ueCategory to set
     */
    public void setUeCategory(int ueCategory) {
        this.ueCategory = ueCategory;
    }

    @Override
    public String toString() {
        return "Name=" + getName();
    }

    public String getIPerfUlMachine() {
        return IPerfUlMachine;
    }

    public void setIPerfUlMachine(String iPerfUlMachine) {
        IPerfUlMachine = iPerfUlMachine;
    }

    public String getIPerfDlMachine() {
        return IPerfDlMachine;
    }

    public void setIPerfDlMachine(String iPerfDlMachine) {
        IPerfDlMachine = iPerfDlMachine;
    }

    public int getSNMPPort() {
        return SNMPPort;
    }

    public void setSNMPPort(int sNMPPort) {
        SNMPPort = sNMPPort;
    }

    public int getDMToolPort() {
        return DMToolPort;
    }

    public void setDMToolPort(int dMToolPort) {
        DMToolPort = dMToolPort;
    }

    public abstract boolean start();

    public abstract boolean reboot();

    public abstract boolean setAPN(String apnName);

    public abstract boolean stop();

    public abstract String getVersion();

    public abstract String getBandWidth();

    public abstract String getUEStatus();

    public abstract String getDuplexMode();

    public abstract int getRSRP(int index);

    public Plmn getPlmnByImsi() {
        Plmn plmn = new Plmn();
        plmn.setMCC(this.getMCCbyimsi());
        plmn.setMNC(this.getMNCbyimsi());
        return plmn;
    }


    public abstract int getPCI();

    public void rebootWithIpPowerWith2MinutesWaiting() {
        PowerControllerPort ipPower = getPowerControllerPort();
        try {
            ipPower.powerOff();
        } catch (Exception e) {
            report.report("Failed to turn off UE", Reporter.WARNING);
            e.printStackTrace();
            return;
        }
        GeneralUtils.unSafeSleep(10 * 1000);
        try {
            ipPower.powerOn();
        } catch (Exception e) {
            report.report("Failed to turn on UE", Reporter.WARNING);
            e.printStackTrace();
            return;
        }
        report.report("Wait 2 minutes for UE to turn on");
        GeneralUtils.unSafeSleep(2 * 60 * 1000);
    }
}

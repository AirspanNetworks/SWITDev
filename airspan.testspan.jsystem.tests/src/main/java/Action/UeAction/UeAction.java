package Action.UeAction;

import java.util.ArrayList;

import org.junit.Test;

import Action.Action;
import EnodeB.EnodeB;
import UE.GemtekUE;
import UE.UE;
import Utils.GeneralUtils;
import Utils.SysObjUtils;
import jsystem.framework.ParameterProperties;
import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;
import testsNG.Actions.PeripheralsConfig;

public class UeAction extends Action {
    protected EnodeB dut;
    private ArrayList<UE> ues;
    private UE ue;
    private int EARFCN;
    private long restartDelay;
    private String apnName;

    @ParameterProperties(description = "Name of ENB from the SUT")
    public void setDUT(String dut) {
        ArrayList<EnodeB> temp = (ArrayList<EnodeB>) SysObjUtils.getInstnce().initSystemObject(EnodeB.class, false,
                dut.split(","));
        this.dut = temp.get(0);
    }

    @ParameterProperties(description = "Name of UEs from the SUT")
    public void setUEs(String ues) {
        this.ues = (ArrayList<UE>) SysObjUtils.getInstnce().initSystemObject(UE.class, false, ues.split(","));
    }

    @ParameterProperties(description = "Name of UE from the SUT")
    public void setUE(String ue) {
        ArrayList<UE> temp = (ArrayList<UE>) SysObjUtils.getInstnce().initSystemObject(UE.class, false, ue.split(","));
        this.ue = temp.get(0);
    }

    @ParameterProperties(description = "Restart Delay")
    public void setRestartDelay(long restartDelay) {
        this.restartDelay = restartDelay;
    }

    @ParameterProperties(description = "EARFCN")
    public void setEARFCN(String EARFCN) {
        this.EARFCN = Integer.valueOf(EARFCN);
    }

    @ParameterProperties(description = "APN Name")
    public void setApnName(String apnName) {
        this.apnName = apnName;
    }

    @Test // 1
    @TestProperties(name = "Check if UEs connected to ENB", returnParam = "LastStatus", paramsInclude = {"UEs, DUT"})
    public void checkIfUEsConnected() {
        report.report("Check if UEs connected");

        if (PeripheralsConfig.getInstance().checkIfAllUEsAreConnectedToNode(this.ues, dut) == false) {
            report.report("Some of the UEs are not connected to EnodeB", Reporter.FAIL);
        } else {
            report.report("All the UEs are connected");
        }
    }

    @Test // 2
    @TestProperties(name = "Reboot UE", returnParam = "LastStatus", paramsInclude = {"UE"})
    public void rebootUe() {
        report.report("Reboot UE " + this.ue.getName() + " " + this.ue.getImsi());
        boolean flag = this.ue.reboot();

        if (flag) {
            report.report("Reboot UE Succeeded");
        } else {
            report.report("Reboot UE Failed", Reporter.FAIL);
        }
    }

    @Test // 5
    @TestProperties(name = "Start UEs", returnParam = "LastStatus", paramsInclude = {"UEs"})
    public void startUes() {
        report.report("Start UEs");
        boolean flag = PeripheralsConfig.getInstance().startUEs(ues);

        if (flag == false) {
            report.report("Start UEs Failed", Reporter.FAIL);
        } else {
            report.report("Start UEs Succeeded");
        }
    }

    @Test // 6
    @TestProperties(name = "Stop UEs", returnParam = "LastStatus", paramsInclude = {"UEs"})
    public void stopUEs() {
        report.report("Stop UEs");
        boolean flag = PeripheralsConfig.getInstance().stopUEs(ues);

        if (flag == false) {
            report.report("Stop UEs Failed", Reporter.FAIL);
        } else {
            report.report("Stop UEs Succeeded");
        }
    }

    @Test // 7
    @TestProperties(name = "change Earfcn UEs", returnParam = "LastStatus", paramsInclude = {"UEs , EARFCN"})
    public void changeEARFCNForUEs() {
        report.report("change earfcn action");
        report.report("earfcn from SUT : " + EARFCN);
        if (ues == null) {
            report.report("there are no ues from parameters!", Reporter.FAIL);
            return;
        }

        for (UE ue : ues) {
            GeneralUtils.startLevel("Setting Earfcn Value for UE : " + ue.getName());
            if (ue instanceof GemtekUE) {
                if (((GemtekUE) ue).changeEARFCN(EARFCN)) {
                    report.report("set earfcn value of " + EARFCN + " into " + ue.getName() + " successfully");
                } else {
                    report.report("could not set earfcn value to " + ue.getName() + ", retrying.", Reporter.WARNING);
                    GeneralUtils.unSafeSleep(5000);
                    if (((GemtekUE) ue).changeEARFCN(EARFCN)) {
                        report.report("set earfcn value of " + EARFCN + " into " + ue.getName() + " successfully");
                    } else {
                        report.report("could not set earfcn value to " + ue.getName(), Reporter.FAIL);
                    }
                }
            } else {
                report.report("ue : " + ue.getName() + "is not a Gemtek and canot change his EARFCN");
            }
            GeneralUtils.stopLevel();
        }
    }

    /**
     * This action reboots UEs, by SNMP or by
     */
    @Test // 8
    @TestProperties(name = "Reboot UEs", returnParam = "LastStatus", paramsInclude = {"UEs"})
    public void rebootUEs() {
        report.report("Reboot UEs");
        boolean isSucceed = PeripheralsConfig.getInstance().rebootUEs(ues);
        if (isSucceed) {
            report.report("Reboot UEs Succeeded");
        } else {
            report.report("Not all UEs were rebooted", Reporter.FAIL);
        }
    }

    @Test // 9
    @TestProperties(name = "Set UEs APN", returnParam = "LastStatus", paramsInclude = {"UEs", "apnName"})
    public void setAPN() {
        report.report("Start UEs");
        boolean flag = PeripheralsConfig.getInstance().setAPN(ues, apnName);

        if (flag == false) {
            report.report("Set UEs APN Failed", Reporter.FAIL);
        } else {
            report.step("Set UEs APN Succeeded");
        }
    }
}

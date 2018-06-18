package Action.UeAction;

import java.util.ArrayList;

import org.junit.Test;

import Action.Action;
import EnodeB.EnodeB;
import UE.GemtekUE;
import UE.UE;
import UeSimulator.Amarisoft.AmariSoftServer;
import Utils.GeneralUtils;
import Utils.SysObjUtils;
import jsystem.framework.ParameterProperties;
import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;
import testsNG.Actions.PeripheralsConfig;

public class UeSimulatorActions extends Action {
	protected EnodeB dut;
	protected ArrayList<EnodeB> duts;
	private ArrayList<UE> ues;
	private int numUes = 1;
	private int release = 13;
	private int category = 4;
	private int cellId = 0;

	@ParameterProperties(description = "Name of ENBs from the SUT")
	public void setDUTs(String duts) {
		ArrayList<EnodeB> temp = (ArrayList<EnodeB>) SysObjUtils.getInstnce().initSystemObject(EnodeB.class, false,
				duts.split(","));
		this.duts = temp;
	}
	
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
	
	@Test											
	@TestProperties(name = "Add UEs", returnParam = "LastStatus", paramsInclude = { "NumUes", "release", "category",
			"dut", "cellId" })
	public void AddUes() {
		boolean flag = false;
		try {
			report.report("Adding " + numUes + " UEs, release " + release + ", category " + category);
			AmariSoftServer a = AmariSoftServer.getInstance();

			if (!a.isRunning()) {
				report.report("Simulator is not working, cant add UEs", Reporter.WARNING);
			}
			else{
				if (dut == null) {
					report.report("No DUT provided, attaching UE to first available Cell.");
					flag = a.addUes(numUes, release, category);
				} else {
					report.report("Attaching UE to " + dut.getName() + " cell " + cellId);
					flag = a.addUes(numUes, release, category, dut, cellId);
				}
			}
		} catch (Exception e) {
			report.report("Error adding Ues: " + e.getMessage(), Reporter.WARNING);
			e.printStackTrace();
		}

		if (flag == false) {
			report.report("Add UEs Failed", Reporter.FAIL);
		} else {
			report.report("Add UEs Succeeded");
		}
	}
	
	@Test											
	@TestProperties(name = "start UE Simulator", returnParam = "LastStatus", paramsInclude = { "duts" })
	public void startUeSimulator() {
		try {
			if (duts == null) {
				report.report("No DUTs given to start UE simulator.");
				return;
			}
			report.report("Starting UE simulator with the following Cells:");
			int sdrCounter = 0;
			for (EnodeB enodeB : duts) {
				int cells = enodeB.getNumberOfActiveCells();
				for (int i = 0; i < cells; i++) {
					report.report(enodeB.getName() + " Cell #" + i);
					sdrCounter++;
				}
			}
			report.report("There are " + sdrCounter + " Cells in the enodeBs given, so " + sdrCounter + " are requiered.");
			AmariSoftServer a = AmariSoftServer.getInstance();
			a.startServer(duts);
		} catch (Exception e) {
			report.report("Error starting UE simulator: " + e.getMessage(), Reporter.WARNING);
			e.printStackTrace();
		}
	}
	
	@Test											
	@TestProperties(name = "start UE Simulator with File", returnParam = "LastStatus", paramsInclude = { "configFileName" })
	public void startUeSimulatorWithFile() {
		
	}
	
}

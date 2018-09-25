package Action.UeAction;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Test;

import Action.Action;
import EnodeB.EnodeB;
import UE.AmarisoftUE;
import UE.UE;
import UeSimulator.Amarisoft.AmariSoftServer;
import Utils.GeneralUtils;
import Utils.SysObjUtils;
import jsystem.framework.ParameterProperties;
import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;
import jsystem.framework.scenario.Parameter;

public class UeSimulatorActions extends Action {
	protected EnodeB dut;
	protected ArrayList<EnodeB> duts;
	private ArrayList<UE> ues;
	private int numUes = 1;
	private int release = 13;
	private int category = 4;
	private int cellId = 1;
	private int ueId;
	private String IMSI;
	private String groupName;
	//private SelectionMethod selectionMethod = SelectionMethod.IMSI;
	private UesOptions uesOptions = UesOptions.AMOUNT;
	


	/*public enum SelectionMethod{
		IMSI, UEID, UENAME, AMOUNT, GROUPNAME;
	}*/
	
	public enum UesOptions{
		AMOUNT, GROUPNAME;
	}
	
	/*@ParameterProperties(description = "UE Selection Method")
	public void setSelectionMethod(SelectionMethod selectionMethod) {
			this.selectionMethod = selectionMethod;
	}*/	

	@ParameterProperties(description = "UE Selection Method")
	public void setUesOptions(UesOptions uesOptions) {
		this.uesOptions = uesOptions;
	}

	@ParameterProperties(description = "UeId")
	public void setUeId(String ueId) {
		try {
			this.ueId = Integer.valueOf(ueId);
		} catch (Exception e) {
		}
	}
	
	@ParameterProperties(description = "IMSI")
	public void setIMSI(String IMSI) {
		this.IMSI = IMSI;
	}
	
	@ParameterProperties(description = "number of UEs to add/delete, default = 1")
	public void setNumUes(String numUes) {
		try {
			this.numUes = Integer.valueOf(numUes);
		} catch (Exception e) {
		}
	}
	
	@ParameterProperties(description = "groupName")
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	
	@ParameterProperties(description = "3GPP release, default = 13")
	public void setRelease(String release) {
		try {
			this.release = Integer.valueOf(release);
		} catch (Exception e) {
		}
	}
	
	@ParameterProperties(description = "LTE category of the UEs, default = 4")
	public void setCategory(String category) {
		try {
			this.category = Integer.valueOf(category);
		} catch (Exception e) {
		}
	}
	
	@ParameterProperties(description = "cellId (1/2) in the enodeb to connect to, default = 1")
	public void setCellId(String cellId) {
		try {
			this.cellId = Integer.valueOf(cellId);
		} catch (Exception e) {
		}
	}
	
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
		ArrayList<UE> tempUes = new ArrayList<>();
		String[] ueArray = ues.split(",");
		for (int i = 0; i < ueArray.length; i++) {					
			if(ueArray[i].toLowerCase().trim().equals(AmariSoftServer.amarisoftIdentifier)){
				try {
					AmariSoftServer uesim = AmariSoftServer.getInstance();
					tempUes.addAll(uesim.getUeList());
				} catch (Exception e) {
					e.printStackTrace();
				}	
			}
			else{
				try {
					tempUes.addAll((ArrayList<UE>) SysObjUtils.getInstnce().initSystemObject(UE.class, false, ueArray[i]));			
				} catch (Exception e) {
					report.report("Failed init object " + ueArray[i]);
					e.printStackTrace();
				}
			}
		}
		this.ues = (ArrayList<UE>) tempUes.clone();	
	}
	
	@Test											
	@TestProperties(name = "Add UEs", returnParam = "LastStatus", paramsInclude = {"NumUes","Release", "Category", "DUT", "CellId" , "UesOptions","GroupName"})
	public void addUes() {
		boolean res = true;

		try {
			switch (uesOptions) {
			case AMOUNT:
				addUes(numUes);
				break;
			case GROUPNAME:
				addUes(groupName);
				break;
			default:
				break;
			}
		} catch (Exception e) {
			res = false;
			report.report("Error trying to add UEs: " + e.getMessage(), Reporter.WARNING);
			e.printStackTrace();
		}
		
		if (res == false) {
			report.report("Adding UEs Failed", Reporter.FAIL);
		} else {
			report.report("Adding UEs Succeeded");
		}
	}
	private boolean addUes(String groupName) {
		boolean flag = false;
		
		try {
			report.report("Adding UEs in group: " + groupName + ", release " + release + ", category " + category);
			AmariSoftServer amariSoftServer = AmariSoftServer.getInstance();

			if (!amariSoftServer.isRunning()) {
				report.report("Simulator is not working, cant add UEs", Reporter.WARNING);
			}
			else{
				if (dut == null) {
					report.report("No DUT provided, attaching UE to first available Cell.");
					flag = amariSoftServer.addUes(groupName, release, category);
				} else {
					report.report("Attaching UE to " + dut.getName() + " cell " + cellId);
					flag = amariSoftServer.addUes(groupName, release, category, dut, cellId);
				}
			}
		} catch (Exception e) {
			report.report("Error adding Ues: " + e.getMessage(), Reporter.WARNING);
			e.printStackTrace();
		}

		if (flag == false) {
			report.report("Add UEs Failed", Reporter.FAIL);
			return false;
		} else {
			report.report("Add UEs Succeeded");
			return true;
		}
		
	}

	private boolean addUes(int numUes) {
		boolean flag = false;
		
		try {
			report.report("Adding " + numUes + " UEs, release " + release + ", category " + category);
			AmariSoftServer amariSoftServer = AmariSoftServer.getInstance();

			if (!amariSoftServer.isRunning()) {
				report.report("Simulator is not working, cant add UEs", Reporter.WARNING);
			}
			else{
				if (dut == null) {
					report.report("No DUT provided, attaching UE to first available Cell.");
					flag = amariSoftServer.addUes(numUes, release, category);
				} else {
					report.report("Attaching UE to " + dut.getName() + " cell " + cellId);
					flag = amariSoftServer.addUes(numUes, release, category, dut, cellId);
				}
			}
		} catch (Exception e) {
			report.report("Error adding Ues: " + e.getMessage(), Reporter.WARNING);
			e.printStackTrace();
		}

		if (flag == false) {
			report.report("Add UEs Failed", Reporter.FAIL);
			return false;
		} else {
			report.report("Add UEs Succeeded");
			return true;
		}
	}
	
	@Test											
	@TestProperties(name = "delete UEs in UE Simulator", returnParam = "LastStatus", paramsInclude = {"UesOptions", "NumUes", "GroupName"})
	public void deleteUes() {
		boolean res = true;

		try {
			AmariSoftServer amariSoftServer = AmariSoftServer.getInstance();
			switch (uesOptions) {
			case AMOUNT:
				amariSoftServer.deleteUes(numUes);
				break;
			case GROUPNAME:
				amariSoftServer.deleteUes(groupName);
				break;
			default:
				break;
			}
		} catch (Exception e) {
			res = false;
			report.report("Error trying to delete UEs: " + e.getMessage(), Reporter.WARNING);
			e.printStackTrace();
		}
		
		if (res == false) {
			report.report("delete UEs Failed", Reporter.FAIL);
		} else {
			report.report("delete UEs Succeeded");
		}
	}

	private void deleteUes(int amount) {
		boolean flag = false;
		try {
			AmariSoftServer amariSoftServer = AmariSoftServer.getInstance();

			if (!amariSoftServer.isRunning()) {
				report.report("Simulator is not working, cant delete UEs", Reporter.WARNING);
			}
			else{
				flag = amariSoftServer.deleteUes(amount);
			}
		} catch (Exception e) {
			report.report("Error deleting Ues: " + e.getMessage(), Reporter.WARNING);
			e.printStackTrace();
		}

		if (flag == false) {
			report.report("Delete UEs Failed", Reporter.FAIL);
		} else {
			report.report("Delete UEs Succeeded");
		}
	}
	
	
	@Test											
	@TestProperties(name = "stop UE Simulator", returnParam = "LastStatus", paramsInclude = { })
	public void stopUeSimulator() {
		try {
			report.report("Stopping UE simulator:");
			AmariSoftServer amariSoftServer = AmariSoftServer.getInstance();
			amariSoftServer.startLogger();
			if(amariSoftServer.stopServer()){
				report.report("UE simulator stopped");
			}
			else{
				report.report("UE simulator stop command failed", Reporter.WARNING);
				if (amariSoftServer.isRunning()) 
					report.report("UE simulator is still running", Reporter.FAIL);
				else
					report.report("UE simulator is stopped");
			}
			amariSoftServer.closeLog();

		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	@Test											
	@TestProperties(name = "start UE Simulator", returnParam = "LastStatus", paramsInclude = { "DUTs" })
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
			AmariSoftServer amariSoftServer = AmariSoftServer.getInstance();
			amariSoftServer.startServer(duts);
		} catch (Exception e) {
			report.report("Error starting UE simulator: " + e.getMessage(), Reporter.WARNING);
			e.printStackTrace();
		}
	}
	
	@Test											
	@TestProperties(name = "start UE Simulator with File", returnParam = "LastStatus", paramsInclude = { "configFileName" })
	public void startUeSimulatorWithFile() {
		
	}
	
	@Test											
	@TestProperties(name = "start UEs in UE Simulator", returnParam = "LastStatus", paramsInclude = {"UesOptions","GroupName", "NumUes"})
	public void startUes() {
		boolean res = true;

		try {
			AmariSoftServer amariSoftServer = AmariSoftServer.getInstance();
			int id;
			switch (uesOptions) {
			case AMOUNT:
				break;
			case GROUPNAME:
				startUE(groupName);
				break;
			}
			
			//need to add verification for IP address from amarisoft server
		} catch (Exception e) {
			res = false;
			report.report("Error trying to start UEs: " + e.getMessage(), Reporter.WARNING);
			e.printStackTrace();
		}
		
		if (res == false) {
			report.report("start UEs Failed", Reporter.FAIL);
		} else {
			report.report("start UEs Succeeded");
		}
	}

	private void startUE(String groupName) {
		try {
			GeneralUtils.startLevel("starting UEs from group : " + groupName);
			AmariSoftServer amariSoftServer = AmariSoftServer.getInstance();
			for(AmarisoftUE ue : amariSoftServer.getUeMap()) {
				for (String group: ue.groupName) {
					if(group.equals(groupName)) {
						if (ue.start())
							report.report("UE: " + ue.ueId + " (" + ue.getImsi() + ") started in amarisoft");
						else {
							report.report("UE: " + ue.ueId + " (" + ue.getImsi() + ") was not started as expected", Reporter.WARNING);
						}
					}
				}
			}
			GeneralUtils.stopLevel();
		} catch (Exception e) {
			report.report(e.getMessage());
		}
	}
	
	@Test											
	@TestProperties(name = "stop UEs in UE Simulator", returnParam = "LastStatus", paramsInclude = {"UesOptions","GroupName", "NumUes" })
	public void stopUes() {
		boolean res = true;

		try {
			switch (uesOptions) {
			case AMOUNT:
				break;
			case GROUPNAME:
				stopUes(groupName);			
				break;
			default:
				break;
			}
		} catch (Exception e) {
			res = false;
			report.report("Error trying to stop UEs: " + e.getMessage(), Reporter.WARNING);
			e.printStackTrace();
		}
		
		if (res == false) {
			report.report("stop UEs Failed", Reporter.FAIL);
		} else {
			report.report("stop UEs Succeeded");
		}
	}
	private void stopUes(String groupName) {
		try {
			GeneralUtils.startLevel("stopping UEs from group : " + groupName);
			AmariSoftServer amariSoftServer = AmariSoftServer.getInstance();
			for(AmarisoftUE ue : amariSoftServer.getUeMap()) {
				for (String group: ue.groupName) {
					if(group.equals(groupName)) {
						if (ue.stop())
							report.report("UE: " + ue.ueId + " (" + ue.getImsi() + ") stopped in amarisoft");
						else {
							report.report("UE: " + ue.ueId + " (" + ue.getImsi() + ") did not stop as expected", Reporter.WARNING);
						}
					}
				}
			}
			GeneralUtils.stopLevel();
		} catch (Exception e) {
			report.report(e.getMessage());
		}
		
	}

	@Override
	public void handleUIEvent(HashMap<String, Parameter> map, String methodName) throws Exception {

		/*if (methodName.equals("startUes") || methodName.equals("stopUes")) {
			handleUIEventGetCounterValue(map, methodName);
		}
		if (methodName.equals("deleteUes")) {
			handleUIEventDeleteFunc(map, methodName);
		}
		if (methodName.equals("addUes")) {
			handleUIEventAddFunc(map, methodName);
		}*/
		handleUIEventAddFunc(map, methodName);
	}
	

/*	private void handleUIEventGetCounterValue(HashMap<String, Parameter> map, String methodName) {
		map.get("UeId").setVisible(false);
		map.get("IMSI").setVisible(false);
		map.get("UEs").setVisible(false);
		map.get("GroupName").setVisible(false);
		
		Parameter selectMethod = map.get("SelectionMethod");

		switch (SelectionMethod.valueOf(selectMethod.getValue().toString())) {
		case IMSI:
			map.get("IMSI").setVisible(true);
			break;

		case UEID:
			map.get("UeId").setVisible(true);
			break;

		case UENAME:
			map.get("UEs").setVisible(true);
			break;
		case GROUPNAME:
			map.get("GroupName").setVisible(true);
			break;
		default:
			break;
		}
		
	}
	private void handleUIEventDeleteFunc(HashMap<String, Parameter> map, String methodName) {
		map.get("UeId").setVisible(false);
		map.get("IMSI").setVisible(false);
		map.get("UEs").setVisible(false);
		map.get("NumUes").setVisible(false);
		map.get("GroupName").setVisible(false);
		
		Parameter selectMethod = map.get("SelectionMethod");

		switch (SelectionMethod.valueOf(selectMethod.getValue().toString())) {
		case IMSI:
			map.get("IMSI").setVisible(true);
			break;
		case UEID:
			map.get("UeId").setVisible(true);
			break;
		case UENAME:
			map.get("UEs").setVisible(true);
			break;
		case AMOUNT:
			map.get("NumUes").setVisible(true);
			break;
		case GROUPNAME:
			map.get("GroupName").setVisible(true);
			break;
		}
		
	}*/
	
	private void handleUIEventAddFunc(HashMap<String, Parameter> map, String methodName) {
		map.get("NumUes").setVisible(false);
		map.get("GroupName").setVisible(false);
		
		Parameter uesOptions = map.get("UesOptions");

		switch (UesOptions.valueOf(uesOptions.getValue().toString())){
		case AMOUNT:
			map.get("NumUes").setVisible(true);
			break;
		case GROUPNAME:
			map.get("GroupName").setVisible(true);
			break;
		}
	}
}

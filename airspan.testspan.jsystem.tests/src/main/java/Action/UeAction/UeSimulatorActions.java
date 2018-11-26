package Action.UeAction;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Test;

import com.ziclix.python.sql.connect.Connect;

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
	private String groupName;
	private UesOptions uesOptions = UesOptions.AMOUNT;
	
	
	public enum UesOptions{
		AMOUNT, GROUPNAME
	}
	

	@ParameterProperties(description = "UE Selection Method - default = amount: 1 UE")
	public void setUesOptions(UesOptions uesOptions) {
		this.uesOptions = uesOptions;
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
	@TestProperties(name = "Add UEs", returnParam = "LastStatus", paramsInclude = {"Release", "Category", "DUT", "CellId" , "UesOptions","GroupName","NumUes"})
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
		
		if (!res) {
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
			amariSoftServer = AmariSoftServer.getInstance();
			//checking if ue is part of more then 1 subgroup 
			for(AmarisoftUE ue : amariSoftServer.getUnusedUEs()) {
				if(ue.groupName.size() > 1) {
					String names = String.join(",", ue.groupName);
					report.report("*** The ue : " + ue.getImsi() + " is part of the groups : " + names + " ***");
				}
			}
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

		if (!flag) {
			report.report("Add UEs Failed", Reporter.FAIL);
			return  false;
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

		if (!flag) {
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
			if(amariSoftServer.getUeMap().size() == 0) {
				report.report("There are no Ues to delete in list", Reporter.WARNING);
			}
			switch (uesOptions) {
			case AMOUNT:
				amariSoftServer.deleteUes(numUes);
				break;
			case GROUPNAME:
				amariSoftServer.deleteUes(groupName);
				break;
			default:
			}
		} catch (Exception e) {
			res = false;
			report.report("Error trying to delete UEs: " + e.getMessage(), Reporter.WARNING);
			e.printStackTrace();
		}
		
		if (!res) {
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

		if (!flag) {
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
				report.report("No DUTs given to start UE simulator.", Reporter.FAIL);
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
			if (amariSoftServer.startServer(duts)) 
				report.report("UE simulator has started as expected");
			else 
				report.report("UE simulator didn't start", Reporter.FAIL);
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
			AmariSoftServer amarisoft = AmariSoftServer.getInstance();
			if (amarisoft.getUeMap().size() == 0) {
				report.report("There are no ues to start - ue list is empty", Reporter.WARNING);
				return;
			}
		} catch (Exception e1) {
			report.report("Couldn't open amarisft istance");
			e1.printStackTrace();
		}
		
		try {
			switch (uesOptions) {
			case AMOUNT:
				startUE(numUes);
				break;
			case GROUPNAME:
				startUE(groupName);
				break;
			}
		} catch (Exception e) {
			res = false;
			report.report("Error trying to start UEs: " + e.getMessage(), Reporter.WARNING);
			e.printStackTrace();
		}
		try {
			AmariSoftServer amarisoft = AmariSoftServer.getInstance();
			amarisoft.startTrafficLogs();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (!res) {
			report.report("start UEs Failed", Reporter.FAIL);
		} else {
			report.report("start UEs Succeeded");
		}
	}

	private void startUE(String groupName) {
		boolean atlistOneUe = false;
		try {
			GeneralUtils.startLevel("starting UEs from group : " + groupName);
			AmariSoftServer amariSoftServer = AmariSoftServer.getInstance();
			for(AmarisoftUE ue : amariSoftServer.getUeMap()) {
				for (String group: ue.groupName) {
					if(group.equals(groupName)) {
						String status = amariSoftServer.getUeStatus(ue.ueId);
						if(status.equals("disconnected")) {
							if (ue.start()) {
								report.report("UE: " + ue.ueId + " (" + ue.getImsi() + ") started in amarisoft");
								atlistOneUe = true;
							}
							else {
								report.report("UE: " + ue.ueId + " (" + ue.getImsi() + ") was not started as expected", Reporter.WARNING);
							}
						}
					}
				}
			}
			if (!atlistOneUe)
				report.report("no UEs started from group " + groupName, Reporter.WARNING);
			GeneralUtils.stopLevel();
		} catch (Exception e) {
			report.report(e.getMessage());
		}
	}
	
	private void startUE(int amount) {
		try {
			int ueStarted = 0;
			GeneralUtils.startLevel("starting " + amount + " UEs");
			AmariSoftServer amariSoftServer = AmariSoftServer.getInstance();
			for(AmarisoftUE ue : amariSoftServer.getUeMap()) {
				if(ueStarted < amount) {
					String status = amariSoftServer.getUeStatus(ue.ueId);
					if(status.equals("disconnected")) {
						if (ue.start() ) {
							if (ue.getLanIpAddress() != null) {
								report.report("UE: " + ue.ueId + " (" + ue.getImsi() + ") started in amarisoft. IP: " + ue.getLanIpAddress());
								ueStarted++;
							}
							else {
								if (ue.reboot()) {
									if (ue.getLanIpAddress() != null) {
										report.report("UE: " + ue.ueId + " (" + ue.getImsi() + ") started in amarisoft. IP: " + ue.getLanIpAddress());
										ueStarted++;
									}
									else
										report.report("UE: " + ue.ueId + " (" + ue.getImsi() + ") was not started as expected after 2 tries. IP: " + ue.getLanIpAddress(), Reporter.WARNING);
								}
							}
						}
					}
				}
				else 
					break;
				
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
			AmariSoftServer amarisoft = AmariSoftServer.getInstance();
			if (amarisoft.getUeMap().size() == 0) {
				report.report("There are no ues to stop - ue list is empty", Reporter.WARNING);
				return;
			}
		} catch (Exception e1) {
			report.report("Couldn't open amarisft istance");
			e1.printStackTrace();
		}
		try {
			switch (uesOptions) {
			case AMOUNT:
				stopUEs(numUes);
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
		
		if (!res) {
			report.report("stop UEs Failed", Reporter.FAIL);
		} else {
			report.report("stop UEs Succeeded");
		}
	}
	
	private void stopUEs(int amount) {
		try {
			int ueStarted = 0;
			GeneralUtils.startLevel("stopping " + amount + " UEs");
			AmariSoftServer amariSoftServer = AmariSoftServer.getInstance();
			for(AmarisoftUE ue : amariSoftServer.getUeMap()) {
				if(ueStarted < amount) {
					String status = amariSoftServer.getUeStatus(ue.ueId);
					if(!status.equals("disconnected")) {
						if (ue.stop())
							report.report("UE: " + ue.ueId + " (" + ue.getImsi() + ") stopped");
						else {
							report.report("UE: " + ue.ueId + " (" + ue.getImsi() + ") was not stopped as expected", Reporter.WARNING);
						}
						ueStarted++;
					}
				}
				else 
					break;
				
			}
			GeneralUtils.stopLevel();
		} catch (Exception e) {
			report.report(e.getMessage());
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
	
	@Test											
	@TestProperties(name = "RRC reestablishment", returnParam = "LastStatus", paramsInclude = {"UesOptions","GroupName", "NumUes" })
	public void rrcReestablishment() {
		boolean res = true;
		try {
			AmariSoftServer amarisoft = AmariSoftServer.getInstance();
			if (amarisoft.getUeMap().size() == 0) {
				report.report("There are no ues to reestablish - ue list is empty");
				return;
			}
		} catch (Exception e1) {
			report.report("Couldn't open amarisft instance");
			e1.printStackTrace();
		}
		try {
			switch (uesOptions) {
			case AMOUNT:
				rrcReestablishment(numUes);
				break;
			case GROUPNAME:
				rrcReestablishment(groupName);			
				break;
			default:
				break;
			}
		} catch (Exception e) {
			res = false;
			report.report("Error trying to stop UEs: " + e.getMessage(), Reporter.WARNING);
			e.printStackTrace();
		}
		
		if (!res) {
			report.report("stop UEs Failed", Reporter.FAIL);
		} else {
			report.report("stop UEs Succeeded");
		}
	}

	private void rrcReestablishment(String groupName) {
		try {
			GeneralUtils.startLevel("reestablish UEs from group : " + groupName);
			AmariSoftServer amariSoftServer = AmariSoftServer.getInstance();
			for(AmarisoftUE ue : amariSoftServer.getUeMap()) {
				for (String group: ue.groupName) {
					if(group.equals(groupName)) {
						if (ue.rrcReestablishment())
							report.report("UE: " + ue.ueId + " (" + ue.getImsi() + ") reestablish");
						else {
							report.report("UE: " + ue.ueId + " (" + ue.getImsi() + ") did not reestablish", Reporter.WARNING);
						}
					}
				}
			}
			GeneralUtils.stopLevel();
		} catch (Exception e) {
			report.report(e.getMessage());
		}
		
	}
	
	private void rrcReestablishment(int amount) {
		try {
			int ueStarted = 0;
			GeneralUtils.startLevel("reestablish " + amount + " UEs");
			AmariSoftServer amariSoftServer = AmariSoftServer.getInstance();
			for(AmarisoftUE ue : amariSoftServer.getUeMap()) {
				if(ueStarted < amount) {
					String status = amariSoftServer.getUeStatus(ue.ueId);
					if(status.equals("connected")) {
						if (ue.rrcReestablishment())
							report.report("UE: " + ue.ueId + " (" + ue.getImsi() + ") reestablish");
						else {
							report.report("UE: " + ue.ueId + " (" + ue.getImsi() + ") was not reestablish", Reporter.WARNING);
						}
						ueStarted++;
					}
				}
				else 
					break;
				
			}
			GeneralUtils.stopLevel();
		} catch (Exception e) {
			report.report(e.getMessage());
		}
	}
	@Override
	public void handleUIEvent(HashMap<String, Parameter> map, String methodName) throws Exception {

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

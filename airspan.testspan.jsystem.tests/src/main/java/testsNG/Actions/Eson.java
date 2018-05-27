package testsNG.Actions;

import java.util.ArrayList;
import java.util.List;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;

import ESon.EsonServer;
import EnodeB.EnodeB;
import Utils.GeneralUtils;
import jsystem.framework.report.ListenerstManager;
import jsystem.framework.report.Reporter;

public class Eson {
	
	public static Reporter report = ListenerstManager.getInstance();
	
	public boolean isDutConnectedToEsonServer(EsonServer esonServer, EnodeB enb){
		try {	
			esonServer.openConnection();
			esonServer.login();
			esonServer.chooseEsonServer();
			String jSonData = esonServer.refresh();
			long time = System.currentTimeMillis();
			while(System.currentTimeMillis()-time<60*1000){
				if(getNodeDetails(jSonData,enb)){
					return true;
				}
				GeneralUtils.unSafeSleep(5*1000);
				jSonData = esonServer.refresh();
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	return false;
	}
	
	private boolean getNodeDetails(String jsonData, EnodeB dut){
		boolean result = true;
		try{
			List<Integer> dutPcis = getAllNodePCIs(dut);
			report.report(dut.getName() + " PCI: " + dutPcis);
			ArrayList<Integer> esonPcis = JsonPath.read(jsonData, "$.coes[*].cell.pci");
			report.report("Eson Version: " + JsonPath.read(jsonData, "$.version"));
			report.report("PCIs in Eson Server : " + dutPcis);
			for(Integer dutPci : dutPcis){
				if(esonPcis.contains(dutPci)) {
					report.report("match pci : "+dutPci);
				}else {
					report.report("didn't match pci : "+dutPci);
					result = false;
				}
			}
		}catch(PathNotFoundException e){
			GeneralUtils.printToConsole("Exception Info : There are no Cells with EnodeBs connected");
		}
		return result;
	}

	private List<Integer> getAllNodePCIs(EnodeB node) {
		List<Integer> result = new ArrayList<Integer>();
		int numOfCells = node.getNumberOfActiveCells();
		
		while(numOfCells > 0) {
			int cellOID = (numOfCells - 1) + 40;
			int cellPci = node.getPci(cellOID);
			result.add(cellPci);
			numOfCells--;
		}
		return result;
	}
}

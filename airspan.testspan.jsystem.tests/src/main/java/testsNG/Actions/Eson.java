package testsNG.Actions;

import java.util.ArrayList;

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
		return false;
	}
	
	private boolean getNodeDetails(String jsonData, EnodeB dut){
		try{
			// ArrayList<String> ecgis = JsonPath.read(jsonData,
			// "$.coes[*].cell.ecgi");
			int dutPci = dut.getPci();
			report.report(dut.getName() + " PCI: " + dutPci);
			ArrayList<Integer> pcis = JsonPath.read(jsonData, "$.coes[*].cell.pci");
			report.report("Eson Version: " + JsonPath.read(jsonData, "$.version"));
			report.report("PCIs in Eson Server : " + pcis);
			for(Integer number : pcis){
				GeneralUtils.printToConsole(number);
				if(dutPci==number){
					return true;
				}
			}
		}catch(PathNotFoundException e){
			GeneralUtils.printToConsole("Exception Info : There are no Cells with EnodeBs connected");
		}
		return false;
	}
}

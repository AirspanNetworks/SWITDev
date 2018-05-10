package testsNG.Actions;

import java.util.ArrayList;
import java.util.List;

import EnodeB.EnodeB;
import Netspan.NetspanServer;
import Netspan.API.Lte.PTPStatus;
import Netspan.API.Lte.RFStatus;
import Netspan.API.Lte.SONStatus;
import jsystem.framework.report.ListenerstManager;
import jsystem.framework.report.Reporter;

public class Statuses {
	private static Statuses instance;
	private static NetspanServer netspanServer = null;
	public static Reporter report = ListenerstManager.getInstance();
	
	private Statuses() {
	}
	
	public static Statuses getInstance() {
		if (instance == null)
			instance = new Statuses();
		try {
			netspanServer = NetspanServer.getInstance();
		} catch (Exception e) {
			report.report("Netspan Server is unavialable Error: " + e.toString(), Reporter.WARNING);
		}
		return instance;
	}
	
	public List<RFStatus> getRFStatus(EnodeB enb){
		List<RFStatus> ret = new ArrayList<RFStatus>();
		ret = netspanServer.getRFStatus(enb);
		if (ret.size()<=0 || ret == null)
			ret = enb.getRFStatus();
		return ret;
	}
	
	public PTPStatus getPTPStatusViaSnmp(EnodeB dut) {
		PTPStatus response = dut.getPTPStatus();
		return response;
	}
	
	public PTPStatus getPTPStatusViaNetspan(EnodeB dut) {
		PTPStatus response = netspanServer.getPTPStatus(dut);
		return response;
	}
	
	public PTPStatus getPTPStatus(EnodeB dut) {
		PTPStatus response = netspanServer.getPTPStatus(dut);
		if(response == null){
			report.report("getting PTPStatus with SNMP");
			response = dut.getPTPStatus();
		}
		return response;
	}
	
	public SONStatus getSONStatus(EnodeB dut) {
		SONStatus response = netspanServer.getSONStatus(dut);
		if(response == null){
			//TODO with SNMP: response = dut.getSONStatus();
		}
		return response;
	}
}
package Action.TrafficAction;

import java.util.ArrayList;

import org.junit.Test;

import jsystem.framework.ParameterProperties;
import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;
import testsNG.Actions.Traffic;
import Action.Action;
import UE.UE;
import Utils.SysObjUtils;

public class TrafficAction extends Action {
	private ArrayList<UE> ues;

	@ParameterProperties(description = "UEs")
	public void setUEs(String ues) {
		this.ues = (ArrayList<UE>) SysObjUtils.getInstnce().initSystemObject(UE.class, false, ues.split(","));
	}

	@Test // 1
	@TestProperties(name = "Start Traffic", returnParam = "LastStatus", paramsInclude = { "UEs" })
	public void startTraffic() {
		report.report("Start Traffic");

		if (Traffic.getInstance(ues).startTraffic(ues) == false) {
			report.report("Start Traffic Failed", Reporter.FAIL);
		} else {
			report.report("Start Traffic Succeeded");
		}
	}

	@Test // 2
	@TestProperties(name = "Stop Traffic", returnParam = "LastStatus", paramsInclude = { "UEs" })
	public void stopTraffic() {
		report.report("Stop Traffic");

		if (Traffic.getInstance(ues).stopTraffic() == false) {
			report.report("Stop Traffic Failed", Reporter.FAIL);
		} else {
			report.report("Stop Traffic Succeeded");
		}
	}

	// @Test // 5
	@TestProperties(name = "Get Traffic Statistics", returnParam = "LastStatus", paramsInclude = { "" })
	public void getTrafficStatistics() {
		report.report("Get Traffic Statistics");
		// TODO
	}
}
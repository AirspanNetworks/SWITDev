package testsNG.ProtocolsAndServices.Protocol1588;

import org.junit.Test;
import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;

public class P2 extends Base1588 {

	@Test // 2
	@TestProperties(name = "IP_Connectivity_To_Grand_Master", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void ipConnectivityToGrandMaster() {
		try {
			if (ptpStatus == null)
 				return;

			int tries = 50;
			String PTP_GM = enodeBConfig.getGMIP(dut);
			String pingResponse;

			report.startLevel("Making 200 pings from Enb to primary Grand Master");

			for (int i = 0; i < 4; i++) {
				pingResponse = dut.ping(PTP_GM, tries);
				if ((pingResponse != null) && (pingResponse.contains(tries + " packets received")))
					report.report(tries + " pings to the primary Grand Master succceded");
				else {
					report.report(
							tries + "pings to the primary Grand Master on enodeb: " + dut.getNetspanName() + " Failed",
							Reporter.FAIL);
					String[] responseLines = pingResponse.split("\n");
					for (int j = 0; j < responseLines.length; j++) {
						report.report(responseLines[j], Reporter.FAIL);
					}
					reason = "200 pings to the primary Grand Master on enodeb: " + dut.getNetspanName() + "Failed";
				}
			}
			report.stopLevel();

		} catch (Exception e) {
			e.printStackTrace();
			report.report("Error: "+e.getMessage(), Reporter.FAIL);
			reason = "Error: "+e.getMessage();
		}
	}
}

package testsNG.PlatformAndSecurity.PowerControl;

import org.junit.Test;

import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;

public class Progression extends PowerControlBase{
	@Test
	@TestProperties(name = "Is setting Tx Power lower than Minimum is blocked", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void isSettingTxPowerLowerThanMinimumIsBlocked() {
		String productCode = enbConfig.getNodeInfo(dut).productCode;
		int minTxPower = enbConfig.readTxMinPowerOfProductViaShell(dut,productCode);
		report.report("Minimum Tx Power for productCode: " + productCode + " is " + minTxPower);
		report.report("Trying to set Tx Power to" + (minTxPower - 1));
		if (dut.setTxPower(minTxPower - 1)){
			report.report("Set Tx Power lower than the Minimum (" + minTxPower + ") succeeded", Reporter.FAIL);
		}else{
			report.report("Set Tx Power lower than the Minimum (" + minTxPower + ") failed", Reporter.PASS);
		}
	}
}

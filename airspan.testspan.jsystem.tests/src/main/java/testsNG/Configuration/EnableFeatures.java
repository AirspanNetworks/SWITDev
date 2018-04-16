package testsNG.Configuration;

import java.util.ArrayList;

import org.junit.Test;

import EnodeB.EnodeB;
import Netspan.API.Enums.EnbTypes;
import Utils.GeneralUtils;
import Utils.SysObjUtils;
import jsystem.framework.ParameterProperties;
import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;
import testsNG.TestspanTest;
import testsNG.Actions.EnodeBConfig;

public class EnableFeatures extends TestspanTest {
	private EnodeBConfig enbConfig;
	private ArrayList<EnodeB> duts;
	private boolean performReboot = true;
	private boolean status = true;

	@Override
	public void init() throws Exception {
		enbInTest = new ArrayList<EnodeB>();
		for (EnodeB dut : duts) {
			enbInTest.add(dut);
		}
		super.init();
		enbConfig = EnodeBConfig.getInstance();
	}

	@Test
	@TestProperties(name = "Configuration - Enable Home EnodeB", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void enableHomeEnodeB() throws Exception {
		for (EnodeB dut : duts) {
			report.report(String.format("%s - Trying to set enb type to \"HOME\".", dut.getName()));
			if (!enbConfig.setEnbType(dut, EnbTypes.HOME)) {
				report.report(String.format("%s - Failed to set enb type.", dut.getName()), Reporter.FAIL);
			}
			else
				report.step(String.format("%s - Succeeded to set enb type.", dut.getName()));
		}
		if (performReboot) {
			status = rebootAndWaitForAllrunning();
			if (status) {
				report.report("Enodeb Reached all running and in service successfully.");
			}
		}
	}

	@Test
	@TestProperties(name = "Configuration - Disable Home EnodeB", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void disableHomeEnodeB() throws Exception {
		for (EnodeB dut : duts) {
			report.report(String.format("%s - Trying to set enb type to \"MACRO\".", dut.getName()));
			if (!enbConfig.setEnbType(dut, EnbTypes.MACRO)) {
				report.report(String.format("%s - Failed to set enb type.", dut.getName()), Reporter.FAIL);
			}
			else
				report.step(String.format("%s - Succeeded to set enb type.", dut.getName()));
		}
		if (performReboot) {
			status = rebootAndWaitForAllrunning();
			if (status) {
				report.report("Enodeb Reached all running and in service successfully.");
			}
		}
	}
	
	@Test
	@TestProperties(name = "Configuration - Enable Multi-Cell", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void enableMultiCell() throws Exception {
		for (EnodeB dut : duts) {
			report.report(String.format("%s - Trying to enable Multi-Cell.", dut.getName()));
			if (!enbConfig.setMultiCellstate(dut, true)) {
				report.report(String.format("%s - Failed to enable Multi-Cell.", dut.getName()), Reporter.FAIL);
			}
			else
				report.step(String.format("%s - Succeeded to enable Multi-Cell.", dut.getName()));
		}
		if (performReboot) {
			status = rebootAndWaitForAllrunning();
			if (status) {
				report.report("Enodeb Reached all running and in service successfully.");
			}
		}
	}

	@Test
	@TestProperties(name = "Configuration - Disable Multi-Cell", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void disableMultiCell() throws Exception {
		for (EnodeB dut : duts) {
			report.report(String.format("%s - Trying to disable Multi-Cell.", dut.getName()));
			if (!enbConfig.setMultiCellstate(dut, false)) {
				report.report(String.format("%s - Failed to disable Multi-Cell.", dut.getName()), Reporter.FAIL);
			}
			else
				report.step(String.format("%s - Succeeded to disable Multi-Cell.", dut.getName()));
		}
		if (performReboot) {
			status = rebootAndWaitForAllrunning();
			if (status) {
				report.report("Enodeb Reached all running and in service successfully.");
			}
		}
	}

	private Boolean rebootAndWaitForAllrunning() {
		boolean status = true;
		for (EnodeB dut : duts) {
			status = dut.reboot();
			if (!status) {
				report.report("Reboot Failed", Reporter.FAIL);
				return false;
			}
		}
		GeneralUtils.unSafeSleep(30 * 1000);

		for (EnodeB dut : duts) {
			status = dut.waitForAllRunningAndInService(EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
		}
		int inserviceTime = (int) (EnodeB.WAIT_FOR_ALL_RUNNING_TIME / 1000 / 60);
		if (!status)
			report.report("Enodeb did not reach all running and in service during " + inserviceTime + " minutes",
					Reporter.WARNING);
		return status;
	}

	@ParameterProperties(description = "Name of All Enb comma seperated e.g enb1,enb2")
	public void setDUTs(String duts) {
		this.duts = (ArrayList<EnodeB>) SysObjUtils.getInstnce().initSystemObject(EnodeB.class, false, duts.split(","));
	}
	
	@ParameterProperties(description = "Perform reboot to apply changes.")
	public void setPerformReboot(boolean performReboot) {
		this.performReboot = performReboot;
	}

	public ArrayList<EnodeB> getDuts() {
		return duts;
	}
}

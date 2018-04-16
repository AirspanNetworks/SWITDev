package testsNG.UnitTest.Mailer;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Test;

import EnodeB.EnodeB;
import Utils.GeneralUtils;
import Utils.SysObjUtils;
import jsystem.framework.ParameterProperties;
import jsystem.framework.TestProperties;
import testsNG.TestspanTest;

public class MailerTest extends TestspanTest {
	protected EnodeB dut1;
	protected EnodeB dut2;

	@Override
	public void init() throws Exception {
		enbInTest = new ArrayList<>();
		enbInTest.add(dut1);
		enbInTest.add(dut2);
		super.init();
	}

	@ParameterProperties(description = "First DUT")
	public void setDUT1(String dut) {
		GeneralUtils.printToConsole("Load DUT1" + dut);
		this.dut1 = (EnodeB) SysObjUtils.getInstnce().initSystemObject(EnodeB.class, false, dut).get(0);
		GeneralUtils.printToConsole("DUT loaded" + this.dut1.getNetspanName());
	}

	@ParameterProperties(description = "Second DUT")
	public void setDUT2(String dut) {
		GeneralUtils.printToConsole("Load DUT2" + dut);
		this.dut2 = (EnodeB) SysObjUtils.getInstnce().initSystemObject(EnodeB.class, false, dut).get(0);
		GeneralUtils.printToConsole("DUT loaded" + this.dut2.getNetspanName());
	}

	@Test // 1
	@TestProperties(name = "Mailer_Test", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void mailerTest() throws IOException {
		report.report("Mailer_Test");
		String images = "";

		String newImage1 = "title1" + "," + "imageName1.png" + ";";
		String newImage2 = "title2" + "," + "imageName2.png" + ";";

		images += newImage1;
		images += newImage2;

		report.report("Images: " + images);
		report.addProperty("Images", images);
	}

	@Test // 2
	@TestProperties(name = "Wait_5_Minutes", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void wait5Minutes() throws IOException {
		report.report("Wait 5 minutes");
		GeneralUtils.unSafeSleep(1000 * 60 * 5);
	}

	@Test // 3
	@TestProperties(name = "Wait_5_Seconds", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void wait5Seconds() throws IOException {
		report.report("Wait 5 seconds");
		GeneralUtils.unSafeSleep(1000 * 5);
	}
	
	@Test // 4
	@TestProperties(name = "Wait_10_Seconds", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void wait10Seconds() throws IOException {
		report.report("Wait 10 seconds");
		GeneralUtils.unSafeSleep(1000 * 10);
	}
}
package testsNG.ProtocolsAndServices.IMMCI;

import DMTool.DMtool;
import testsNG.TestspanTest;

public class IMMCI extends TestspanTest {
	private DMtool dm;

	@Override
	public void init() throws Exception {
		dm = new DMtool();
		dm.setUeIP("192.7.2.254");
		dm.setPORT(7772);
		dm.init();
	}

//	@Test
//	@TestProperties(name = "IMMCI Test1", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
//			"IsTestWasSuccessful" })
//	public void IMMCITest1() throws Exception {
//		report.report("IMMCI");
//		int dor = dm.getRrcInfo();
//		dor = dm.getRrcState();
//		GeneralUtils.printToConsole(dor);
//	}
}

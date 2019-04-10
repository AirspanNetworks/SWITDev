package testsNG.ProtocolsAndServices.IMMCI;

import org.junit.Test;

import DMTool.DMtool;
import DMTool.Events.EventListener;
import jsystem.framework.TestProperties;
import testsNG.TestspanTest;

public class IMMCI extends TestspanTest {
	private DMtool dm;
	private EventListener evt;

	@Override
	public void init() throws Exception {
		evt = new EventListener("dor");
		dm = new DMtool();
		dm.setUeIP("192.168.57.78");
		dm.setPORT(17772);
		dm.addEventListener(evt);
		dm.init();
		dm.enableEvents(new int[]{115});
		String dor = dm.cli("help");
	}

	@Test
	@TestProperties(name = "IMMCI Test1", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void IMMCITest1() throws Exception {
		report.report("IMMCI");
		dm.getProperties();
	}
	
}

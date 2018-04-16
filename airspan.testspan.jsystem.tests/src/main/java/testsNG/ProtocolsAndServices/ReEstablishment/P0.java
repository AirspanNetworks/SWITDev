package testsNG.ProtocolsAndServices.ReEstablishment;

import org.junit.Test;

import Utils.SetupUtils;
import jsystem.framework.TestProperties;

public class P0 extends EstablishmentBase{

	@Test
	@TestProperties(name = "Re-Establishment Basic Functionality single Ue", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
	"IsTestWasSuccessful,DUT2" })
	public void reEstablishment_Basic_Functionality_single_Ue(){
		preTest(1);
		reEstablishmentTest();
		afterTest();
	}
	
	@Test
	@TestProperties(name = "Re-Establishment Basic Functionality two - ten UEs", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
	"IsTestWasSuccessful,DUT2" })
	public void reEstablishment_Basic_Functionality_MultiUEs(){
		preTest(SetupUtils.getInstance().getAllUEs().size());
		reEstablishmentTest();
		afterTest();
	}
}

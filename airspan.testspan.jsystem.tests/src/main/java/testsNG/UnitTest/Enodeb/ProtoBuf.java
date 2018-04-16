package testsNG.UnitTest.Enodeb;

import java.util.ArrayList;

import org.junit.Test;

import EnodeB.EnodeB;
import Utils.SysObjUtils;
import jsystem.framework.ParameterProperties;
import jsystem.framework.TestProperties;
import testsNG.TestspanTest;

public class ProtoBuf extends TestspanTest {
	private EnodeB dut;	
	
	
	@Override
	public void init() throws Exception {
		enbInTest = new ArrayList<EnodeB>();
		enbInTest.add(dut);
		super.init();
	}
	
	@Test
	@TestProperties(name = "getCounters", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {"IsTestWasSuccessful" })
	public void getCounters() throws Exception {
		dut.getCountersValue("HoX2IntraFreqInAttRnlRadioRsn");
		
	}
	
	@ParameterProperties(description = "Name of Enb")
	public void setDUT(String dut) {
		ArrayList<EnodeB> temp=(ArrayList<EnodeB>)SysObjUtils.getInstnce().initSystemObject(EnodeB.class,false,dut);
		this.dut = temp.get(0);
	}
}

package testsNG.General;

import java.util.ArrayList;

import org.junit.Test;

import EnodeB.EnodeB;
import Utils.GeneralUtils;
import Utils.SysObjUtils;
import jsystem.framework.ParameterProperties;
import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;
import testsNG.TestspanTest;

public class ProfilerInfo extends TestspanTest{
	public EnodeB dut1;
	public EnodeB dut2;
	
	@Override
	public void init() throws Exception {
		enbInTest = new ArrayList<>();
		enbInTest.add(dut1);
		enbInTest.add(dut2);
		super.init();
	}
    @Test
    @TestProperties(name = "ProfilerInfo", returnParam = {"IsTestWasSuccessful"}, paramsExclude = {"IsTestWasSuccessful"})
    public void profilerInfo(){
    	//generating profiler information in DB
    	if (enbInTest.size()>0){
    		for(EnodeB enb : enbInTest){
    			enb.getProfilerInfo();
    		}
    	}
    	else 
    		report.report("There are no enodeB's to check", Reporter.WARNING);
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

	public EnodeB getDut1() {
		return dut1;
	}

	public EnodeB getDut2() {
		return dut2;
	}
	

	
}

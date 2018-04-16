package testsNG;

import Utils.GeneralUtils;
import jsystem.framework.report.ExtendTestListener;
import jsystem.framework.report.TestInfo;
import jsystem.framework.scenario.JTestContainer;
import jsystem.framework.scenario.flow_control.AntForLoop;
import junit.framework.AssertionFailedError;
import junit.framework.Test;


public class TestListener implements ExtendTestListener {
	  
	@Override
	public void addError(Test test, Throwable t) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void addFailure(Test test, AssertionFailedError t) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void endTest(Test test) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void startTest(Test test) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void addWarning(Test test) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void startTest(TestInfo testInfo) {
		GeneralUtils.clearLevelCounter();
	}
	@Override
	public void endRun() {
//		if(Traffic.getInstance(null) != null){
//			GeneralUtils.printToConsole("Shutting down Traffic Session and ports.");
//			Traffic.getInstance(null).shutDown();
//		}
	}
	
	@Override
	public void startLoop(AntForLoop loop, int count) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void endLoop(AntForLoop loop, int count) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void startContainer(JTestContainer container) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void endContainer(JTestContainer container) {
		// TODO Auto-generated method stub
		
	}
	  
	  
	 }
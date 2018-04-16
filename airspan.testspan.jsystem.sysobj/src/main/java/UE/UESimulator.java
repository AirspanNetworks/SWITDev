package UE;


import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import Ercom.XmlRpc.axmlrpc.XMLRPCClient;
import Utils.GeneralUtils;
import jsystem.framework.report.Reporter;
import jsystem.framework.system.SystemManagerImpl;

public class UESimulator extends UE{
	
final String HTTP_PREFIX = "http://"; 

	final String DIRECT_EXECUTION = "testQueue.directExecution"; 
	
	final String STOP_CURRENT_TEST = "testQueue.stopCurrentTest"; 
	
	final String GET_DEFINED_TESTS = "testStorage.getDefinedTests";
	
	final String GET_TEST_PARAMS = "testScenarios.getTestParams";
	
	final String PLATFOMR_STATE = "currentExecution.executionState"; //returns the execution state for the platform: idle, launching, running, stopping.

	final String TEST_STATE = "currentExecution.executionStateForTest"; //returns the execution state for a test: idle, launching, running, stopping.
	
	private String testPath = "Airspan!Cell2!test_STC_QCI9_7_BIDI_Automation";

	private static UESimulator instance=null;
	
	private String TESTS_URL = null;
	
	private String TEST_MGT_URL = null;
	
	private String controlBladeIP;
	
	private String platformNumber;
	
	private ArrayList<UE> virtualUEs;
	
	private String[] imsiStart;
	
	private String[] numberOfUEs;
	
	public String[] getImsiStart() {
		return imsiStart;
	}



	public void setImsiStart(String imsiStart) {
		this.imsiStart = imsiStart.split(",");
	}

	public String getTestPath() {
		return testPath;
	}

	public void setTestPath(String test_Path) {
		this.testPath = test_Path;
	}
	
	public ArrayList<UE> getUEs(){
		return virtualUEs;
	}

	public int getNumberOfUEs() {
		int sum=0;
		for(int i=0; i<numberOfUEs.length;i++){
			sum += Integer.valueOf(numberOfUEs[i]);
		}
		return sum;
	}



	private void setVirtualUEs() {
		virtualUEs = new ArrayList<>();
		int count=0;
		for(int i = 0; i < numberOfUEs.length; i++){
			String imsiPrefix = imsiStart[i].substring(0, 11);
			int imsiEnd = Integer.valueOf(imsiStart[i].substring(11));
			for(int j = 0; j < Integer.valueOf(numberOfUEs[i]); j++){
				count++;
				UE ue = new VirtualUE();
				ue.setName("dyn" + count);
				int tmp = imsiEnd+j;
				ue.setImsi(imsiPrefix + tmp);
				virtualUEs.add(ue);
			}
		}
	}



	public void setNumberOfUEs(String numberOfUEs) {
		this.numberOfUEs = numberOfUEs.split(",");
	}

	private HashMap<String,String> name2id =null;
	
	private String testID = null;
	
	private Object execId = null;
	
	/************************************/
	
	public UESimulator() {
		super("UESimulator");
	}
	
	
	
	@Override
	public void init() throws Exception {
		super.init();
		TESTS_URL = HTTP_PREFIX + controlBladeIP + "//" + "p" + platformNumber + "//" + "tests";
		TEST_MGT_URL = HTTP_PREFIX + controlBladeIP + "//" + "p" + platformNumber + "//" + "testMgt";
		getDefinedTests();
		setTestID(name2id.get(testPath));
		setVirtualUEs();
	}
	
	public synchronized static UESimulator getInstance(){
		
		try{
			if(instance==null){
				instance=(UESimulator) SystemManagerImpl.getInstance().getSystemObject("UESimulator");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return instance;
	}
	
	/*******************************/

	@Override
	public boolean start() {
		execId = directExecution();
		if(execId == null){
			return false;
		}
		return true;
		
	}

	@Override
	public boolean reboot() {
		boolean flag = stop();
		flag &= GeneralUtils.unSafeSleep(5000);
		flag &= start();
		return flag;
	}

	@Override
	public boolean stop() {
		Object result =stopCurrentTest();
		if(result == null){
			return false;
		}
		return true;
	}

	@Override
	public String getVersion() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getBandWidth() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getUEUlFrequency() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getUEDlFrequency() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getUEStatus() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDuplexMode() {
		// TODO Auto-generated method stub
		return null;
	}
	
	/***************************************/

	private Object directExecution(){
		
		if(controlBladeIP==null || platformNumber==null){return false;}
		
		if(!isTestRunning()){
			Object[] params = new Object[1];
			params[0] = this.testID;
			report.report("Starting UE simulator!!");
			Object result =  sendCommand(TEST_MGT_URL, DIRECT_EXECUTION, params);
			try {
				report.report("Waiting for 120 seconds");
				Thread.sleep(120 * 1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return result;
		}else{
			report.report("UE simulator is already running!!", Reporter.WARNING);
		}
		return null;
	}
	
	public boolean isTestRunning(){
		
		String result = (String)sendCommand(TEST_MGT_URL, TEST_STATE, testID); 
		
		if(result.equals("idle") || result.equals("stopping")){
			return false;
		}else{
			return true;
		}
	}
	
	private Object stopCurrentTest(){
		
		if(controlBladeIP==null || platformNumber==null){return false;}
		
		if(isTestRunning()){
			report.report("Stopping UE simulator");
			Object result =  sendCommand(TEST_MGT_URL, STOP_CURRENT_TEST);
			try {
				Thread.sleep(5 * 1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return result;
		}else{
			report.report("UE simulator isn't running!!");
		}
		return null;
	}
	
	private Object sendCommand(String url, String method, Object... params){
		try {
			XMLRPCClient client = new XMLRPCClient(new URL(url));
			return client.call(method,params);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private void getDefinedTests(){
		name2id = new  HashMap<String,String>();
		
		Object[] obj =(Object[])sendCommand(TESTS_URL, GET_DEFINED_TESTS, "");
  	    for (Object object : obj) {
			@SuppressWarnings("unchecked")
			HashMap<String,Object> temp = (HashMap<String,Object>)object;
			name2id.put((String)temp.get("name"), (String)temp.get("uniqId"));
		}
	}

	public Object getTestParams(){
		
		Object testParams = sendCommand(TEST_MGT_URL, GET_TEST_PARAMS, testID);
		
		GeneralUtils.printToConsole(testParams.toString());
		return testParams;
	}
	
	public String getTestCounters(){
		
		String testCounters = (String)sendCommand(TEST_MGT_URL, "testResults.getCounters", execId, -5000);
		
		GeneralUtils.printToConsole(testCounters.toString());
		return testCounters;
	}

	/******************Getters and Setters*************/
	
	public String getControlBladeIP() {
		return controlBladeIP;
	}

	public void setControlBladeIP(String controlBladeIP) {
		this.controlBladeIP = controlBladeIP;
	}

	public String getPlatformNumber() {
		return platformNumber;
	}

	public void setPlatformNumber(String platformNumber) {
		this.platformNumber = platformNumber;
	}
	
	public String getTestID() {
		return testID;
	}

	public void setTestID(String testID) {
		this.testID = testID;
	}

	@Override
	public int getRSRP(int index) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public int getPCI() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public void setTestByPath(String path){
		setTestID(name2id.get(path));

	}
	
	@Override
	public boolean setAPN(String apnName) {
		report.report("Could not perform setAPN method, UESimulator does not support SNMP", Reporter.WARNING);
		return false;
	}

}

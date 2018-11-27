package TestingServices;

import jsystem.framework.system.SystemManagerImpl;
import jsystem.framework.system.SystemObjectImpl;

public class TestConfig extends SystemObjectImpl {

	private static TestConfig instance = null;
	
	public SWUpgradeConfig swUpgradeConfig;
	private int[] defaultEarfcn;
	private int[] interEarfcn;
	private String UE_Restart = "IPPower";

	/** The nodes names. */
	private String[] nodes;
	private String[] staticUEs;
	private String[] dynamicUEs;
	private Integer pciStart;
	private Integer pciEnd;
	private Boolean dynamicCfi;
	private String customer;
	
	private String snifferIp;
	private String passCriteriaTPT;
	private String snifferMAC;
	private String loggerUploadAllUrl = "http://100.100.0.250/upload/upload.php";
	
	private Boolean SWUCliFallback;

	public TestConfig() {}

	public static TestConfig getInstace() {
		if (instance == null) {
			try {
				instance = (TestConfig) SystemManagerImpl.getInstance().getSystemObject("TestConfig");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return instance;
	}

	//fhdfyuhjfdhjfhgfh


	public String getLoggerUploadAllUrl() {
		return loggerUploadAllUrl;
	}

	public void setLoggerUploadAllUrl(String loggerUploadAllUrl) {
		this.loggerUploadAllUrl = loggerUploadAllUrl;
	}

	public String getSnifferMAC() {
		return snifferMAC;
	}

	public void setSnifferMAC(String snifferMAC) {
		this.snifferMAC = snifferMAC;
	}
	
	public void setDynamicCfi(String dynamicCFI){
		this.dynamicCfi = dynamicCFI.toLowerCase().equals("true");
	}
	
	public boolean getDynamicCFI(){
		if(dynamicCfi == null){
			dynamicCfi = false;
		}
		return this.dynamicCfi;
	}

	public String[] getNodes() {
		return nodes;
	}

	public void setNodes(String nodes) {
		this.nodes = nodes.split(",");
	}

	public String[] getStaticUEs() {
		return staticUEs;
	}
	
	public String[] getDynamicUEs() {
		return dynamicUEs;
	}
	
	public void setStaticUEs(String staticUEs) {
		this.staticUEs = staticUEs.split(",");
	}

	public void setDynamicUEs(String dynamicUEs) {
		this.dynamicUEs = dynamicUEs.split(",");
	}

	public SWUpgradeConfig getSwUpgradeConfig() {
		return swUpgradeConfig;
	}

    public int getDefaultEarfcn() {
		return defaultEarfcn[0];
	}

    public int[] getDefaultEarfcns() {
		return defaultEarfcn;
	}
    
	public void setDefaultEarfcn(String defaultEarfcn) {
		String strList[] = defaultEarfcn.split(",");
		this.defaultEarfcn = new int[strList.length];
		for (int i=0 ; i < strList.length ; i++)
			this.defaultEarfcn[i] = Integer.parseInt(strList[i]);
	}

	public int getInterEarfcn() {
		return interEarfcn[0];
	}

	public int[] getInterEarfcns() {
		return interEarfcn;
	}
	
	public void setInterEarfcn(String interEarfcn) {
		String strList[] = interEarfcn.split(",");
		this.interEarfcn = new int[strList.length];
		for (int i=0 ; i < strList.length ; i++)
			this.interEarfcn[i] = Integer.parseInt(strList[i]);
	}

	public String getUE_Restart() {
		return UE_Restart;
	}

	public void setUE_Restart(String uE_Restart) {
		UE_Restart = uE_Restart;
	}

	public String getSnifferIp() {
		return snifferIp;
	}

	public void setSnifferIp(String snifferIp) {
		this.snifferIp = snifferIp;
	}
	
	public String getpassCriteriaTPT() {
		return passCriteriaTPT;
	}

	public void setpassCriteriaTPT(String passCriteriaTPT) {
		this.passCriteriaTPT = passCriteriaTPT;
	}
	
	public void setPciStart(String pciStart){
		try{
			this.pciStart = Integer.valueOf(pciStart);
		}catch(Exception e){
			e.printStackTrace();
			report.report("Error while trying to get Numric value of value : "+pciStart);
			this.pciStart = null;
		}
	}
	
	public Integer getPciStart(){
		return this.pciStart;
	}
	
	public void setPciEnd(String pciEnd){
		try{
			this.pciEnd = Integer.valueOf(pciEnd);
		}catch(Exception e){
			e.printStackTrace();
			report.report("Error while trying to get Numric value of value : "+pciEnd);
			this.pciEnd = null;
		}
	}
	
	public Integer getPciEnd(){
		return this.pciEnd;
	}

	public String getCustomer() {
		return customer;
	}

	public void setCustomer(String costumer) {
		this.customer = costumer;
	}
	
	public Boolean getSWUCliFallback() {
		return SWUCliFallback;
	}

	public void setSWUCliFallback(String sWUCliFallback) {
		SWUCliFallback = Boolean.parseBoolean(sWUCliFallback);
	}
}

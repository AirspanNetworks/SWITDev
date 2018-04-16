package PowerControllers;

import java.io.IOException;
import java.util.HashMap;
import Utils.GeneralUtils;
import Utils.Snmp.MibReader;
import Utils.Snmp.SNMP;
import jsystem.framework.report.Reporter;

public abstract class SnmpBasedPowerController extends PowerController {

	/** 
	 * IOD prefix
	 */
	private static final String OID_PREFFIX = "."; 
	/** 
	 * must be used for scalar objects
	 */
    private static final String OID_SUFFIX= ".0";  
	/** 
	 * Number of retries 
	 */
    private static final int    NUMBER_OF_RETRIES = 5;
	/** 
	 * SNMP version currently V1/V2 supported
	 */
    private String    snmpVersion;
	/** 
	 * Read community permission
	 */
    private String    readCommunity;
	/** 
	 * write community permission
	 */
    private String    writeCommunity;
	/** 
	 * holds name to OID mapping
	 */
    private MibReader oidDataBase; 
	/** 
	 * SNMP core class
	 */
    private SNMP      snmp;	   

	/** 
	 * Port to MIB translation table
	 */
    private HashMap<String, String> portToMibTranslation= 
    		new HashMap<String, String>();
 
    
    /**
     * Initialization function. OID to MIB dependence created
     * SNMP low layer initialized.
     * @throws general Exception
     */
    public void init() throws Exception{
    	// INIT super class
    	super.init(); 
    	// create OID to MIB dependencies
		oidDataBase = MibReader.getInstance(); 
		//create SNMP infrastructure
		snmp= new SNMP( readCommunity,
						writeCommunity,
						getIpAddress(),
						snmpVersion); 
    }

    

    
    /**
     * This method implement SNMP set command.
     * OID string for scalar methods created, this means that 
     * following method can set value for column objects.
     * @param String name - MIB name
     * @param int Value - value to set
     * @exception throws NullPointerException in case name equals to null, signals to 
 		  calling function something went wrong.
     */
    protected void setSnmpObject(String name, int Value) throws NullPointerException {
	    int counter = 0;
		if(name==null){
			throw new NullPointerException("Mib name is NULL pointer"
					+ " Please check Mib name. Received in setSnmpObject function. ");
		}
		do{
			try{
				// get SNMP OID by name from translation database (mapping mane=OID)
			    String strOID=String.format("%s%s%s", 
								    		OID_PREFFIX,
								    		oidDataBase.resolveByName(name),
								    		OID_SUFFIX);
			    if(strOID.equals("")){
			    	GeneralUtils.printToConsole("Unable to perform SNMP setObject because the there is"
			    			+ " no OID found in translation table "
			    			+ "(MibHolder.csv file) for specific name:"+name);
			    	return;
			    }
			    
			    // send SNMP message to remote unit
			    snmp.snmpSet(strOID, Value);
			    // break the retries loop if no error occurred during send command 
			    break;
			}catch(Exception e){
				counter++;
				// initiate new PDU because during exception all resources will
				// be closed
			    snmp.initiatePDU();
			    GeneralUtils.printToConsole("IOException was thrown duaring SNMP send requast, "
			    		+ "trying to send new requast.");
			    GeneralUtils.printToConsole(String.format("Requast number # %s",counter));
			}
		}while(counter<NUMBER_OF_RETRIES);
    }
  
    
    /**
     * Returns string value of MIB object.
     * @param String name - MIB name
     * @return String - string value of result.
     * @throws NullPointerException - if name is null pointer.
     * @throws IOException 
     */
    protected String getSnmpObject(String name) throws NullPointerException, IOException {
    	String var = "";
		if(name==null){
			throw new NullPointerException("Mib name is NULL pointer"
					+ " Please check Mib name. Received in setSnmpObject function. ");
		}
		
		// get SNMP OID by name from translation database (mapping mane=OID)
	    String strOID=String.format("%s%s%s", 
						    		OID_PREFFIX,
						    		oidDataBase.resolveByName(name),
						    		OID_SUFFIX);
	    if(strOID.equals("")){
	    	GeneralUtils.printToConsole("Unable to perform SNMP setObject because the there is"
	    			+ " no OID found in translation table "
	    			+ "(MibHolder.csv file) for specific name:"+name);
	    	return var;
	    }
	    var = snmp.get(strOID);
    	return var;
    }
    
	/**
	 * Change power controller Port State
	 * 
	 * @param PowerControllerPort
	 * @param action- ON or OFF
	 * @throws IOException 
	 * @throws NullPointerException 
	 */
	public boolean changePortState(PowerControllerPort port , int action) throws NullPointerException, IOException {
		boolean actionChange = true;
		if(portToMibTranslation.containsKey(port.getPort())){
		   String mibLeaf = portToMibTranslation.get(port.getPort());
		   
	       setSnmpObject(mibLeaf,action);
	       GeneralUtils.unSafeSleep(3*1000);
	       
	       String currState = getSnmpObject(mibLeaf);
	       
	       if(currState == null || currState.equals("")){
	    	   report.report(String.format("INFO:Unable to read [%s] port state, "
	    	   		+ "Port status not validated", port.getPort()),Reporter.WARNING);
	    	   actionChange = false;
	       }
	       else{
	    	   if(currState.trim().equals("3")){
	    		    GeneralUtils.printToConsole("The Power controller port in pending state (3), "
	    		    		+ "wait 6 seconds before validation.");
		    		GeneralUtils.unSafeSleep(6*1000);
	    	   }
	    	   // check if port state after set operation equals to requested state.
	    	   if(String.valueOf(action).equals(currState.trim())){
	    		   report.report("Port " + port.getPort() + " state changed successfully \n");
	    	   }
	    	   else{
	    		   report.report("Port "+port.getPort()+" state didn't changed \n",Reporter.WARNING);
	    		   GeneralUtils.printToConsole(String.format("Requested state is [ %s ] received state after "
	    		   		+ "read command is [ %s ]", String.valueOf(action),currState));
	    		   actionChange = false;
	    	   }
	       }
	   }
	   else { 
		   report.report(String.format("Unable to change [%s] port state",
				   port.getPort()),Reporter.WARNING);
	       report.report(String.format("Port %s not in translation table, "
	       		+ "This means that the Port didn't configured in \"MibHolder.csv\" file",
	       		   port.getPort()));
	       actionChange = false;
	   }
		return actionChange;
	}

    
    // -------------------------  Getters/Setters -----------------------------//
 
    public String getSnmpVersion() {
        return snmpVersion;
    }
    public void setSnmpVersion(String snmpVersion) {
        this.snmpVersion = snmpVersion;
    }
    public String getReadCommunity() {
        return readCommunity;
    }
    public void setReadCommunity(String readCommunity) {
        this.readCommunity = readCommunity;
    }
    public String getWriteCommunity() {
        return writeCommunity;
    }
    public void setWriteCommunity(String writeCommunity) {
        this.writeCommunity = writeCommunity;
    }

	public HashMap<String, String> getPortToMibTranslation() {
		return portToMibTranslation;
	}

	public void setPortToMibTranslation(HashMap<String, String> portToMibTranslation) {
		this.portToMibTranslation = portToMibTranslation;
	}
    
}

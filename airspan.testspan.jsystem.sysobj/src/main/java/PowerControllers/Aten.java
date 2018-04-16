package PowerControllers;

import java.io.IOException;
import java.util.HashMap;



public class Aten extends SnmpBasedPowerController{
     
	/** ON MIB value */
	private static final  int  POWER_ON_CMD    = 2; 
	
	/** OFF MIB value */
	private static final  int  POWER_OFF_CMD   = 1; 

	/** Port to MIB translation table */
    private HashMap<String, String> portToMibTranslation= 
    		new HashMap<String, String>();
  

 
    /**
     * Initialization function initialize table
     * that translates  power controller ports to MIBs.
     *  ("A1" ,"outlet1Status")
	 *  ("A2" ,"outlet2Status")
	 *  ("A3" ,"outlet3Status")
	 *  ("A4" ,"outlet4Status")
	 *  ("A5" ,"outlet5Status")
	 *  ("A6" ,"outlet6Status")
	 *  ("A7" ,"outlet7Status")
	 *  ("A8" ,"outlet8Status")
     * @param void
     * @return void
     */
	@Override
	public void init() throws Exception {
	    super.init();
	    // Port to MIB translation table initialization
	    // for specific ATEN power controller
	    portToMibTranslation.put("A1" ,"outlet1Status");
	    portToMibTranslation.put("A2" ,"outlet2Status");
	    portToMibTranslation.put("A3" ,"outlet3Status");
	    portToMibTranslation.put("A4" ,"outlet4Status");
	    portToMibTranslation.put("A5" ,"outlet5Status");
	    portToMibTranslation.put("A6" ,"outlet6Status");
	    portToMibTranslation.put("A7" ,"outlet7Status");
	    portToMibTranslation.put("A8" ,"outlet8Status");
	}
	
	
	/**
	 * Perform power ON for specific port
	 * 
	 * @param PowerControllerPort
	 * @throws IOException 
	 * @throws NullPointerException 
	 */
	@Override
	public boolean powerOnPort(PowerControllerPort port) throws NullPointerException, IOException {
		setPortToMibTranslation(portToMibTranslation);
	    return changePortState(port,POWER_ON_CMD);
	}

	/**
	 * Perform power OFF for specific port
	 * 
	 * @param PowerControllerPort
	 * @throws IOException 
	 * @throws NullPointerException 
	 */
	@Override
	public boolean powerOffPort(PowerControllerPort port) throws NullPointerException, IOException {
		setPortToMibTranslation(portToMibTranslation);
	    return changePortState(port,POWER_OFF_CMD);
	}
	
}

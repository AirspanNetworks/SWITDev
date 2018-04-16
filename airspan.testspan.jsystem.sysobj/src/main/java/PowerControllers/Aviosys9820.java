package PowerControllers;

import java.io.IOException;
import java.util.HashMap;

public class Aviosys9820 extends SnmpBasedPowerController{
	 
	/** 
	 * ON MIB value
	 */
	private static final  int  POWER_ON    = 1; 
	
	/**
	 *  OFF MIB value
	 */
	private static final  int  POWER_OFF   = 0; 
	 
    /** 
     * Port to MIB translation table
     */
	private HashMap<String, String> portToMibTranslation= 
									new HashMap<String, String>();
	
	
	/**
	 * Methods 
	 */
	@Override
	public void init() throws Exception {
	    super.init();
	    // Port to MIB translation table initialization
	    // for specific ATEN power controller
	    portToMibTranslation.put("A1" ,"ippowerOutlet1");
	    portToMibTranslation.put("A2" ,"ippowerOutlet2");
	    portToMibTranslation.put("A3" ,"ippowerOutlet3");
	    portToMibTranslation.put("A4" ,"ippowerOutlet4");
	    portToMibTranslation.put("A5" ,"ippowerOutlet5");
	    portToMibTranslation.put("A6" ,"ippowerOutlet6");
	    portToMibTranslation.put("A7" ,"ippowerOutlet7");
	    portToMibTranslation.put("A8" ,"ippowerOutlet8");
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
	    return changePortState(port,POWER_ON);
	      
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
	    return changePortState(port,POWER_OFF);
	}

}

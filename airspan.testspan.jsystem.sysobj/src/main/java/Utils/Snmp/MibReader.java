package Utils.Snmp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Assert;
import jsystem.framework.system.SystemObjectImpl;


/*
 * MibReader:
 * the class read from DEFAULT_SOURCE_MIB_FILE_NAME the name of the MIB and OID, and maps them togther.
 */
public class MibReader {
    /**
    * Find or create a logger for a named subsystem.  If a logger has
    * already been created with the given name it is returned.  Otherwise
    * a new logger is created.
    */
    private static Logger log = Logger.getLogger(SystemObjectImpl.class.getName());
    
    /** Single instance of the class */
    private static  MibReader INSTANCE = new MibReader();
    
    /** The Constant CSV_DELIMITER. */
    private static final String CSV_DELIMITER = ",";

    /** Variable holds name to OID mapping  */
    private static Map<String, String> nameToOidMap;

    /** Default path of name to OID file  */
    private static final String DEFAULT_SOURCE_MIB_FILE_NAME =   "MibHolder.csv";

    private MibReader() {
	nameToOidMap = new ConcurrentHashMap<String, String>();
	loadMibsToMap();
    }
 
    /**
     * Open file that contains name to OID mapping,
     * and initialize nameToOidMap variable.
     * This map will be used later by SNMP class in order to translate
     * MIB names to OID identifier.
     * */
    private void loadMibsToMap() {
	try {
	    // open containing file
		File csvData = new File(System.getProperty( "user.dir" ) + File.separator + "resources" + File.separator
				+ DEFAULT_SOURCE_MIB_FILE_NAME);
	    FileReader fileReader =  new FileReader(csvData);
	    BufferedReader bufferedReader = new BufferedReader(fileReader);
	    // parsing file line by line
	    String line = bufferedReader.readLine();
	    while (line != null) {
		String[] temp = line.split(CSV_DELIMITER);
		nameToOidMap.put(temp[0], temp[1]);
		line = bufferedReader.readLine();
	    }
	    // close mapping file
	    fileReader.close();

	} catch (Exception e) {
	    e.printStackTrace();
	    // unable to continue without translation table
	    Assert.fail("unable to continue without translation table");
	}
    }

    /**
     * Return this instance of class
     * 
     * @return Current MibReader instance
     * */
    public static MibReader getInstance() {
	    return INSTANCE;
    }

    /**
     * This function return SNMP OIDs Indicator which Identifies element.
     * 
     * @param name Name
     * 
     * @return OID string or empty String if not found
     * @exception NullPointerException
     * */
    public String resolveByName(String name) {

	String oid = "";
	try{
	   if(name!=null){
	     if (nameToOidMap.containsKey(name)) {
	    	 oid = nameToOidMap.get(name);
	     }	   
	     else{
		 log.log(Level.WARNING, "The following MIB name not present in translation table \" MibHolder.csv\" file : " + name);
	     }
	   }else{
	       throw new Exception("Received MIB name is incorrect");
	   }
	}catch(Exception e){
	    e.printStackTrace();
	}
	return oid; // OID string Here's an example: 1.3.6.1.4.1.2681.1.2.102
    }
}

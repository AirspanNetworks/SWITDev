package Utils.Properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Properties;

import Utils.GeneralUtils;


/**
 * The Class ConfigurationManager manage the TestSpan config file.
 */
public class TestspanConfigurationsManager {
	
	
	/** The Constant peoprtiesFileName. */
	public final static String propertiesFileName="testspan.properties";
	
	/** The config reader. */
	private static TestspanConfigurationsManager configReader;
	
	/** The properties. */
	private Properties properties;
	
	
	/**
	 * Gets the single instance of ConfigurationManager.
	 *
	 * @return single instance of ConfigurationManager
	 */
	public static TestspanConfigurationsManager getInstance(){
		
		if(configReader==null){
			configReader=new TestspanConfigurationsManager();
			configReader.properties=new Properties();
			configReader.loadConfig();
		}
		return configReader;
	}
	
	
	/**
	 * Load configurations from proprties file.
	 */
	public void loadConfig(){
		InputStream inputStream=null;
		try{
			GeneralUtils.printToConsole("trying to load Testspan.proprties");
			GeneralUtils.printToConsole("testspan.propreties path "+ System.getProperty( "user.dir" ) + File.separator + propertiesFileName);
			inputStream= new FileInputStream(System.getProperty( "user.dir" ) + File.separator + propertiesFileName);

			properties.load(inputStream); 
		
		}catch(Exception e){
			GeneralUtils.printToConsole(e.toString());
			e.printStackTrace();
		}
		finally{
			if(inputStream!=null){
				try{
				inputStream.close();
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Gets the Configurations with a specific key.
	 * the method returns null if not key was found.
	 *
	 * @param key the key
	 * @return the config
	 */
	public String getConfig(String key){
		return properties.getProperty( key );
	}
	
	/**
	 * Sets the config.
	 *
	 * @param key the key
	 * @param value the value
	 */
	public void setConfig(String key,String value){
		properties.setProperty( key, value );
	}
	
	/**
	 * Save configurations. (after setting the config file need to use this).
	 */
	public void save(){
		OutputStream output=null;
		try {
		    output = new FileOutputStream(propertiesFileName);
			properties.store( output, null );
		} catch ( Exception e ) {
			e.printStackTrace();
		}
		finally{
			if (output != null) {
				try {
					output.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/* 
	 * print all configurations.
	 */
	@ Override
	public String toString() {
		String allConfigurations="";
		for ( Iterator<String> iterator = properties.stringPropertyNames().iterator(); iterator.hasNext(); ) {
			String property = iterator.next();
			allConfigurations+=property+"="+properties.getProperty( property )+";\n";
		}
		return allConfigurations;
	}
	
	
	
	
	
	
	
	

}

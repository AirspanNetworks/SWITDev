package Netspan;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.junit.Ignore;

import Utils.GeneralUtils;
import jsystem.framework.report.ListenerstManager;
import jsystem.framework.report.Reporter;


public abstract class NBIHelper {
	
	protected static final int MAX_TRYOUTS=3;
	protected static final long REST_TIME = 1200;
	protected static String FAULT_MANAGEMENT_WSDL_RELATIVE_PATH	= "FaultManagement.asmx?WSDL";
	protected static String INVENTORY_WSDL_RELATIVE_PATH      	= "Inventory.asmx?WSDL";
	protected static String LTE_WSDL_RELATIVE_PATH            	= "Lte.asmx?WSDL";
	protected static String STATISTICS_WSDL_RELATIVE_PATH     	= "Statistics.asmx?WSDL";
	protected static String STATUS_WSDL_RELATIVE_PATH         	= "Status.asmx?WSDL";
	protected static String SERVER_WSDL_RELATIVE_PATH 			= "Server.asmx?WSDL";
	protected static String BACKHAUL_WSDL_RELATIVE_PATH 		= "Backhaul.asmx?WSDL";
	protected static String SOFTWARE_WSDL_RELATIVE_PATH 		= "Software.asmx?WSDL";
	private static String WSDL_PREFIX                           = "http://";
	private static int totalTryouts = 0;
	private String netspanServer;
	private String webServicesPath;
	protected String username;
	protected String password;
	
	
	// List of all SOAP Components
	protected ArrayList <Object> soapComponentList = new ArrayList <Object>();
	
	
	public NBIHelper(String netspanServer, String webSevicesPath, String username, String password) throws Exception {
		this.netspanServer = netspanServer;
		this.webServicesPath = webSevicesPath;
		this.username = username;
		this.password = password;
		
		try{
			initSOAPPorts();			
		}catch(Exception e){
			e.printStackTrace();
			ListenerstManager.getInstance().report("Failed to initialize NBI helper with version 14.5", Reporter.WARNING);
		}
	}
	
	protected abstract void initSOAPPorts() throws Exception;
	
	public URL getWsdlUrl(String relativePath) {
		try {
			return new URL(WSDL_PREFIX + netspanServer + "/" + webServicesPath + "/" + relativePath);
		} catch (MalformedURLException e) {
			return null;
		}
	}
	
	public Object execute(String NBIMethodName, Object... args) throws Exception {
		
		Method method = getNBIMethod(NBIMethodName);
		Object credentials = createCredentialsObject(method.getReturnType());
		
		Object[] args2 = new Object[args.length + 1];
		args2[args.length] = credentials;
		System.arraycopy(args, 0, args2, 0, args.length);

		
		int tryouts=1;
		Object obj=null;
		while(tryouts<=MAX_TRYOUTS && obj==null){	
			try{
			obj= method.invoke(getInstance(method.getReturnType()), args2);
			}catch(Exception e){
				GeneralUtils.printToConsole("Exception on NBIHelper was catched. trying to execute the method \""+method.getName()+"\" again.. attempt#"+tryouts);
				Thread.sleep( REST_TIME );
				e.printStackTrace();
			}
			tryouts++;
		}
		
		if(obj==null)
			throw new Exception("NBI Helper could not execute the command, please check you are full connected to NBIF ");
		
		return obj;
		
		
	}
	public Object execute(String NBIMethodName) throws Exception {
		Method method = getNBIMethod(NBIMethodName);
		Object credentials = createCredentialsObject(method.getReturnType());

		int tryouts=1;
		Object obj=null;
		while(tryouts<=MAX_TRYOUTS && obj==null){	
			try{
			obj= method.invoke(getInstance(method.getReturnType()),credentials);
			}catch(Exception e){
				GeneralUtils.printToConsole("Exception on NBIHelper was catched. trying to execute the method \""+method.getName()+"\" again.. attempt#"+tryouts);
				Thread.sleep( REST_TIME );
				e.printStackTrace();
			}
			tryouts++;
			totalTryouts=totalTryouts+1;
		}
		
		if(obj==null)
			throw new Exception("NBI Helper could not execute the command, please check you are full connected to NBIF ");
		
		return obj;
	
	}
	
	protected abstract Object getInstance(Class<?> SOAPClass);

	protected abstract Object createCredentialsObject(Class<?> SOAPClass);
	
	/**
	 * This functions gets a requested methodName 
	 * and returns the required method from the listSoap  
	 * 
	 * @param methodName The required methodName 
	 * @return The required Method
	 */
	private Method getNBIMethod(String methodName) {

		for (Object soapComponent : soapComponentList) {
			for (Method method : soapComponent.getClass().getMethods()) {
				if (method.getName().equals(methodName))
					return method;
			}
		}
		
		return null;
	}

	/**
	 * 
	 * @return the totalTryouts
	 */
	@Ignore
	public static int getTotalTryouts() {
		return totalTryouts;
	}

	
}

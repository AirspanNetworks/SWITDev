package Netspan.NBI_14_50;


import Netspan.NBI_14_50.API.FaultManagement.FaultManagement;
import Netspan.NBI_14_50.API.FaultManagement.FaultManagementSoap;
import Netspan.NBI_14_50.API.Inventory.Inventory;
import Netspan.NBI_14_50.API.Inventory.InventorySoap;
import Netspan.NBI_14_50.API.Lte.Lte;
import Netspan.NBI_14_50.API.Lte.LteSoap;
import Netspan.NBI_14_50.API.Server.Server;
import Netspan.NBI_14_50.API.Server.ServerSoap;
import Netspan.NBI_14_50.API.Statistics.StatisticsSoap;
import Netspan.NBI_14_50.API.Status.Status;
import Netspan.NBI_14_50.API.Status.StatusSoap;
import Utils.GeneralUtils;


public class NBIHelper extends Netspan.NBIHelper {
	
	// SOAP Compnents
	private FaultManagementSoap faultManagement;
	private InventorySoap inventory;
	private LteSoap lte;
	//private StatisticsSoap statistics;
	private StatusSoap status;
	private ServerSoap server;
		
	public NBIHelper(String netspanServer, String webSevicesPath, 
			String username, String password) throws Exception {
		super(netspanServer, webSevicesPath, username, password);
	}
		
	protected void initSOAPPorts() throws Exception {
		boolean initSuccess = false; 
		int tryouts = 1;
		while (!initSuccess & tryouts<=MAX_TRYOUTS ) { 
			try { 
				faultManagement = new FaultManagement(getWsdlUrl(FAULT_MANAGEMENT_WSDL_RELATIVE_PATH)).getFaultManagementSoap();
				soapComponentList.add(faultManagement);
				inventory = new Inventory(getWsdlUrl(INVENTORY_WSDL_RELATIVE_PATH)).getInventorySoap();
				soapComponentList.add(inventory);
				lte = new Lte(getWsdlUrl(LTE_WSDL_RELATIVE_PATH)).getLteSoap();
				soapComponentList.add(lte);
				
				//statistics = new Statistics(getWsdlUrl(STATISTICS_WSDL_RELATIVE_PATH)).getStatisticsSoap();
				//soapComponentList.add(statistics);
				status = new Status(getWsdlUrl(STATUS_WSDL_RELATIVE_PATH)).getStatusSoap();
				soapComponentList.add(status);
				server = new Server(getWsdlUrl(SERVER_WSDL_RELATIVE_PATH)).getServerSoap();
				soapComponentList.add(server);
				initSuccess = true;
			}catch (Exception e) {
				GeneralUtils.printToConsole("Exception on NBIHelper was catched. trying to initialize the NBI classes again.. attempt#"+tryouts);
				Thread.sleep( REST_TIME );
			}
			tryouts++;
		}
		if(!initSuccess)
			throw new Exception("NBI Helper could not initialize the NBI classes");
	}
		
	protected Object createCredentialsObject(Class<?> SOAPClass) {
		if (SOAPClass.getPackage() == FaultManagementSoap.class.getPackage()){
			Netspan.NBI_14_50.API.FaultManagement.Credentials credentials = new Netspan.NBI_14_50.API.FaultManagement.Credentials();
			credentials.setUsername(username);
			credentials.setPassword(password);
			return credentials;
		}
			
		if (SOAPClass.getPackage() == InventorySoap.class.getPackage()){
			Netspan.NBI_14_50.API.Inventory.Credentials credentials = new Netspan.NBI_14_50.API.Inventory.Credentials();
			credentials.setUsername(username);
			credentials.setPassword(password);
			return credentials;
		}
		
		if (SOAPClass.getPackage() ==  LteSoap.class.getPackage()){
			Netspan.NBI_14_50.API.Lte.Credentials credentials = new Netspan.NBI_14_50.API.Lte.Credentials();
			credentials.setUsername(username);
			credentials.setPassword(password);
			return credentials;
		}
			
		if (SOAPClass.getPackage() == StatisticsSoap.class.getPackage()){
			Netspan.NBI_14_50.API.Statistics.Credentials credentials = new Netspan.NBI_14_50.API.Statistics.Credentials();
			credentials.setUsername(username);
			credentials.setPassword(password);
			return credentials;
		}
			
		if (SOAPClass.getPackage() == StatusSoap.class.getPackage()){
			Netspan.NBI_14_50.API.Status.Credentials credentials = new Netspan.NBI_14_50.API.Status.Credentials();
			credentials.setUsername(username);
			credentials.setPassword(password);
			return credentials;
		}
		
		if (SOAPClass.getPackage() == ServerSoap.class.getPackage()){
			Netspan.NBI_14_50.API.Server.Credentials credentials = new Netspan.NBI_14_50.API.Server.Credentials();
			credentials.setUsername(username);
			credentials.setPassword(password);
			return credentials;
		}
			
		return null;	
	}
		
	protected Object getInstance(Class<?> SOAPClass) {
		if (SOAPClass.getPackage() == FaultManagementSoap.class.getPackage()){
			return faultManagement;
		}
			
		if (SOAPClass.getPackage() == InventorySoap.class.getPackage()){
			return inventory;
		}
			
		if (SOAPClass.getPackage() == LteSoap.class.getPackage()){
			return lte;
		}
			
		/*if (SOAPClass.getPackage() == StatisticsSoap.class.getPackage()){
			return statistics;
		}*/
			
		if (SOAPClass.getPackage() == StatusSoap.class.getPackage()){
			return status;
		}
		
		if (SOAPClass.getPackage() == ServerSoap.class.getPackage()){
			return server;
		}
			
		return null;
	}
}

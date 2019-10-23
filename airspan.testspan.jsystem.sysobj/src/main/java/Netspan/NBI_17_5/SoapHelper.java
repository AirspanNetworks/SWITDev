package Netspan.NBI_17_5;

import Netspan.NetspanSOAPManager;
import Netspan.NBI_17_5.Backhaul.Backhaul;
import Netspan.NBI_17_5.Backhaul.BackhaulSoap;
import Netspan.NBI_17_5.FaultManagement.FaultManagement;
import Netspan.NBI_17_5.FaultManagement.FaultManagementSoap;
import Netspan.NBI_17_5.Inventory.Inventory;
import Netspan.NBI_17_5.Inventory.InventorySoap;
import Netspan.NBI_17_5.Lte.Lte;
import Netspan.NBI_17_5.Lte.LteSoap;
import Netspan.NBI_17_5.Server.Server;
import Netspan.NBI_17_5.Server.ServerSoap;
import Netspan.NBI_17_5.Software.Software;
import Netspan.NBI_17_5.Software.SoftwareSoap;
import Netspan.NBI_17_5.Statistics.Statistics;
import Netspan.NBI_17_5.Statistics.StatisticsSoap;
import Netspan.NBI_17_5.Status.Status;
import Netspan.NBI_17_5.Status.StatusSoap;
import Utils.GeneralUtils;
import javax.xml.ws.BindingProvider;

import javax.xml.namespace.QName;
import java.net.URL;

public class SoapHelper {
	private static NetspanSOAPManager soapManager = null;
	private static FaultManagement faultManagement = null;
	private static Inventory inventory = null;
	private static Statistics statistics = null;
	private static Status status = null;
	private static Server server = null;
    private static Lte lte = null;
    private static Backhaul backhaul = null;
    private static Software software = null;
    
    private static final String WS_VERSION = "17.5";

    private static final QName SERVICE_NAME_FAULTMANAGEMENT = new QName("http://Airspan.Netspan.WebServices", "FaultManagement");
    private static final QName SERVICE_NAME_INVENTORY = new QName("http://Airspan.Netspan.WebServices", "Inventory");
    private static final QName SERVICE_NAME_LTE = new QName("http://Airspan.Netspan.WebServices", "Lte");
    private static final QName SERVICE_NAME_SERVER = new QName("http://Airspan.Netspan.WebServices", "Server");
    private static final QName SERVICE_NAME_STATISTICS = new QName("http://Airspan.Netspan.WebServices", "Statistics");
    private static final QName SERVICE_NAME_STATUS = new QName("http://Airspan.Netspan.WebServices", "Status");
    private static final QName SERVICE_NAME_BACKHAUL = new QName("http://Airspan.Netspan.WebServices", "Backhaul");
    private static final QName SERVICE_NAME_SOFTWARE = new QName("http://Airspan.Netspan.WebServices", "Software");
    
    private static URL wsdl_URL_FAULTMANAGEMENT = null;
    private static URL wsdl_URL_INVENTORY = null;
    private static URL wsdl_URL_LTE = null;
    private static URL wsdl_URL_SERVER = null;
    private static URL wsdl_URL_STATISTICS = null;
    private static URL wsdl_URL_STATUS = null;
    private static URL wsdl_URL_BACKHAUL = null;
    private static URL wsdl_URL_SOFTWARE = null;
   
	public SoapHelper(String hostName) throws Exception {
		GeneralUtils.printToConsole("Initalizing SoapHelper with WS Version: " + WS_VERSION);
		wsdl_URL_FAULTMANAGEMENT = new URL("http://" + hostName + "/ws/" + WS_VERSION + "/FaultManagement.asmx?WSDL");
		wsdl_URL_INVENTORY = new URL("http://" + hostName + "/ws/" + WS_VERSION + "/Inventory.asmx?WSDL");
		wsdl_URL_LTE = new URL("http://" + hostName + "/ws/" + WS_VERSION + "/Lte.asmx?WSDL");
		wsdl_URL_SERVER = new URL("http://" + hostName + "/ws/" + WS_VERSION + "/Server.asmx?WSDL");
		wsdl_URL_STATISTICS = new URL("http://" + hostName + "/ws/" + WS_VERSION + "/Statistics.asmx?WSDL");
		wsdl_URL_STATUS = new URL("http://" + hostName + "/ws/" + WS_VERSION + "/Status.asmx?WSDL");
		wsdl_URL_BACKHAUL = new URL("http://" + hostName + "/ws/" + WS_VERSION + "/Backhaul.asmx?WSDL");
		wsdl_URL_SOFTWARE = new URL("http://" + hostName + "/ws/" + WS_VERSION + "/Software.asmx?WSDL");
		soapManager = new NetspanSOAPManager();
	}
	
	public ServerSoap getServerSoap(){
		server = new Server(wsdl_URL_SERVER, SERVICE_NAME_SERVER);
		return server.getServerSoap();
	}
	public void endServerSoap(){
		server = null;
	}
	public LteSoap getLteSoap(){
		lte = new Lte(wsdl_URL_LTE, SERVICE_NAME_LTE);
		return lte.getLteSoap();
	}
	
	public LteSoap getLteSoapRaw(){
		lte = new Lte(wsdl_URL_LTE, SERVICE_NAME_LTE);
		
		BindingProvider provider = (BindingProvider)lte.getLteSoap();
		return (LteSoap)soapManager.addRawDataHandler(provider);
	}
	
	public void clearLteRawData(){
		soapManager.clearLteRawData();
	}
	
	public void endLteSoap(){
		lte = null;
	}
	public StatusSoap getStatusSoap(){
		status = new Status(wsdl_URL_STATUS, SERVICE_NAME_STATUS);
		return status.getStatusSoap();
	}
	public void endStatusSoap(){
		status = null;
	}
	public StatisticsSoap getStatisticsSoap(){
		statistics = new Statistics(wsdl_URL_STATISTICS, SERVICE_NAME_STATISTICS);
		return statistics.getStatisticsSoap();
	}
	public void endStatisticsSoap(){
		statistics = null;
	}
	public FaultManagementSoap getFaultManagementSoap(){
		faultManagement = new FaultManagement(wsdl_URL_FAULTMANAGEMENT, SERVICE_NAME_FAULTMANAGEMENT);
		return faultManagement.getFaultManagementSoap();
	}
	public void endFaultManagementSoap(){
		faultManagement = null;
	}
	public InventorySoap getInventorySoap(){
		inventory = new Inventory(wsdl_URL_INVENTORY, SERVICE_NAME_INVENTORY);
		return inventory.getInventorySoap();
	}
	public void endInventorySoap(){
		inventory = null;
	}
	public BackhaulSoap getBackhaulSoap(){
		backhaul = new Backhaul(wsdl_URL_BACKHAUL, SERVICE_NAME_BACKHAUL);
		return backhaul.getBackhaulSoap();
	}
	public void endBackhaulSoap(){
		backhaul = null;
	}
	public SoftwareSoap getSoftwareSoap(){
		software = new Software(wsdl_URL_SOFTWARE, SERVICE_NAME_SOFTWARE);
		return software.getSoftwareSoap();
	}
	public void endSoftwareSoap(){
		software = null;
	}
	
	public void clearRawData(){
		soapManager.clearLteRawData();
	}
	
	public String getSOAPRawData(){
		return soapManager.getLatestSoapResult();
	}
}

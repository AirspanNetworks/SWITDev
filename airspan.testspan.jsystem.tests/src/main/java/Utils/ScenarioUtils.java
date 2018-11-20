package Utils;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import EnodeB.EnodeB;
import EnodeB.EnodeBWithDonor;
import EnodeB.Components.Log.Logger;
import Netspan.NetspanServer;
import Netspan.API.Lte.NodeInfo;
import Netspan.Profiles.RadioParameters;
import TestingServices.TestConfig;
import UE.UE;
import jsystem.framework.report.ListenerstManager;
import jsystem.framework.report.Reporter;
import testsNG.Actions.EnodeBConfig;

public class ScenarioUtils {

	private static ScenarioUtils instance;
	private static Object insnceLock = new Object();

	private HashMap<String, Integer> scenarioStats = new HashMap<String, Integer>();
	private static HashMap<String, Integer> unexpectedInScenario = new HashMap<String, Integer>();
	public static final String SYSOBJ_STRING_DELIMITER = ";";
	static boolean isCalledOnceInEndFunc = false;
	public static Reporter report = ListenerstManager.getInstance();

	private ScenarioUtils() {

	}

	public static ScenarioUtils getInstance() {
		synchronized (insnceLock) {
			if (instance == null)
				instance = new ScenarioUtils();
		}
		return instance;
	}

	public synchronized HashMap<String, Integer> getScenarioStats() {
		return scenarioStats;
	}

	public synchronized HashMap<String, Integer> getUnexpectedInScenario() {
		return unexpectedInScenario;
	}

	/**
	 * @author Shahaf Shuhamy
	 * @since 02/11/2016
	 * @return current JVM memory Usage in the format of how much used and how
	 *         much left.
	 */
	public String getMemoryConsumption() {
		Runtime runtime = Runtime.getRuntime();
		NumberFormat format = NumberFormat.getInstance();
		StringBuilder sb = new StringBuilder();
		sb.append("____________Memory Consumption Format:_____________________\n");
		sb.append("Date       :  " + DateFormat.getDateTimeInstance().format(System.currentTimeMillis()) + "\n");
		sb.append("Current Free memory               : " + format.format(runtime.freeMemory() / 1024) + " Kb \n");
		sb.append("Max memory for JVM attempt to use : " + format.format(runtime.maxMemory() / 1024) + " Kb \n");
		sb.append("Total JVM memory                  : " + format.format(runtime.totalMemory() / 1024) + " Kb \n");
		sb.append("___________________________________________________________\n");
		return sb.toString();
	}

	/**
	 * @author Avichai Yefet Add all ERRORs & WARNINGs to test Properties
	 * @return
	 */

	@SuppressWarnings("unchecked")
	public void scenarioStatistics(Logger logger, EnodeB eNodeB) {
		String keyProperty = "";
		HashMap<String, Integer> counters = (HashMap<String, Integer>) logger.getScenarioLoggerCounters().clone();
		if (counters != null) {
			for (String key : counters.keySet()) {
				String severity = getSeverity(key);
				String errorExpression = getErrorExpression(key);
				if (errorExpression.equals("NO ERROR FOUND")) {
					GeneralUtils.printToConsole("Failed to find error expression for key: " + key);
					continue;
				}
				keyProperty = eNodeB.getName() + "," + errorExpression + "," + severity;
				GeneralUtils.printToConsole("ScenarioStatistics: " + keyProperty + "," + counters.get(key));
				scenarioStats.put(keyProperty, counters.get(key));
			}
		}
	}

	public String getSeverity(String key) {
		String[] sev = key.split("@");
		String severity = "";
		if (sev != null && sev.length == 2) {
			severity = sev[1];
		} else {
			severity = "ERROR";
		}
		return severity;
	}

	public String getErrorExpression(String key) {
		String[] exp = key.split("@");
		String expression = "";
		if (exp != null && exp.length == 2) {
			expression = exp[0];
		} else {
			expression = "NO ERROR FOUND";
		}
		return expression;
	}

	private void fillSetupTable() {
		GeneralUtils.startLevel("Fill setup properties table");
		String caConfiguration = "";
		String pnpStatus = "";
		String IPSecStatus = "";
		String usingDonorEnb = "";
		String ipType = "";
		String enbTypes = "";
		String relayVersions = "";
		String systemDefaultsProfiles = "";
		String enbAdvancedConfigurationProfiles = "";
		String cellAdvancedConfigurationProfiles = "";
		String mobilityProfiles = "";
		String radioProfiles = "";
		ArrayList<UE> allUEs = SetupUtils.getInstance().getAllUEs();
		ArrayList<UE> staticUEs = SetupUtils.getInstance().getStaticUEs();
		ArrayList<UE> dynamicUEs = SetupUtils.getInstance().getDynamicUEs();
		ArrayList<EnodeB> enbInSetup = SetupUtils.getInstance().getAllEnb();
		NetspanServer netspanServer;
		try {
			netspanServer = NetspanServer.getInstance();
		} catch (Exception e) {
			report.report("Failed to get NetspanServer");
			e.printStackTrace();
			return;
		}

		boolean netspanReachable = netspanServer.isNetspanReachable();
		EnodeBConfig enbConfig = EnodeBConfig.getInstance();
		for (EnodeB enb : enbInSetup) {
			if(enb.hasDonor()){
				relayVersions += (((EnodeBWithDonor)enb).getRelayRunningVersion() + "(" + enb.getName() + ");");
			}
			
			String enbHardwareType = null;
			if(netspanReachable){
				NodeInfo nodeInfo = netspanServer.getNodeDetails(enb);
				if (nodeInfo != null)
					enbHardwareType = nodeInfo.hardwareType.replace(" ", "");				
			}
			if (enbHardwareType == null) {
				enbHardwareType = enb.getArchitecture().toString();
			}
			ipType += enb.getName() + "(" + enb.getIpAddressType() + "); ";
			String caConfig = enb.getCAMode();
			switch (caConfig) {
			case "0":
				caConfig = "Disabled";
				break;

			case "1":
				caConfig = "Non-Contiguous";
				break;

			case "2":
				caConfig = "Contiguous ";
				break;
			default:
				caConfig = "NA";
				break;
			}
			
			caConfiguration += enb.getName() + "(" + caConfig + "); ";
			pnpStatus += enb.getName() + "(" + enb.getPnpMode() + "); ";
			IPSecStatus += enb.getName() + "(" + enb.isIpsecEnabled() + "); ";
			usingDonorEnb += enb.getName() + "(" + enb.hasDonor() + "); ";
			String type = (enb.getName() + "," + enbHardwareType + "," + "Active cells:" + enb.getNumberOfActiveCells()
					+ ";");
			enbTypes += type;
			
			if(netspanReachable){
				systemDefaultsProfiles += enbConfig.getSystemDefaultProfile(enb) + "(" + enb.getName() + "); ";
				
				enbAdvancedConfigurationProfiles += enbConfig.getCurrentEnbAdvancedConfigurationProfileName(enb) + "("
						+ enb.getName() + "); ";				
			}
			for(CellNetspanProfiles cell : enb.defaultNetspanProfiles.cellProfiles){
				enb.setCellContextNumber(cell.getCellId());
				String cellName = "(" + enb.getName() + ":Cell" + cell.getCellId() + "); ";
				if(netspanReachable){
					cellAdvancedConfigurationProfiles += enbConfig.getCurrentCellAdvancedConfigurationProfileName(enb) + cellName;
					
					mobilityProfiles += enbConfig.getCurrentMobilityProfileName(enb) + cellName;					
				}
				
				RadioParameters rp = enbConfig.getRadioProfile(enb);
				if(rp != null){
					String fc = rp.getFrameConfig();
					String DM = rp.getDuplex();
					radioProfiles += DM + ": Band" + rp.getBand() + ", ";
					if (DM == "TDD") {
						radioProfiles += "FC" + fc + ", ";
					}
					radioProfiles += rp.getBandwidth() + "MHz " + cellName;					
				}
			}
			enb.setCellContextNumber(1);
		}
		
		if(netspanReachable){
			systemDefaultsProfiles = systemDefaultsProfiles.substring(0, systemDefaultsProfiles.length() - 2);
			enbAdvancedConfigurationProfiles = enbAdvancedConfigurationProfiles.substring(0,
					enbAdvancedConfigurationProfiles.length() - 2);
			cellAdvancedConfigurationProfiles = cellAdvancedConfigurationProfiles.substring(0,
					cellAdvancedConfigurationProfiles.length() - 2);
			mobilityProfiles = mobilityProfiles.substring(0, mobilityProfiles.length() - 2);
			radioProfiles = radioProfiles.substring(0, radioProfiles.length() - 2);
		}
		if (ipType.length() > 0 ) {
			ipType = ipType.substring(0, ipType.length() - 2);
	    }
		report.setContainerProperties(0, "IPVersion", ipType);
		
		if (caConfiguration.length() > 0 ) {
			caConfiguration = caConfiguration.substring(0, caConfiguration.length() - 2);
	    }
		report.setContainerProperties(0, "CaConfiguration", caConfiguration);
		
		if (pnpStatus.length() > 0 ) {
			pnpStatus = pnpStatus.substring(0, pnpStatus.length() - 2);
	    }
		report.setContainerProperties(0, "pnpStatus", pnpStatus);
		
		if (IPSecStatus.length() > 0 ) {
			IPSecStatus = IPSecStatus.substring(0, IPSecStatus.length() - 2);
	    }
		report.setContainerProperties(0, "IPSecStatus", IPSecStatus);
		
		if (usingDonorEnb.length() > 0 ) {
			usingDonorEnb = usingDonorEnb.substring(0, usingDonorEnb.length() - 2);
	    }
		report.setContainerProperties(0, "UsingDonorEnb", usingDonorEnb);
		
		// EnbType
		report.setContainerProperties(0, "EnbType", enbTypes);

		// UeType
		String UEtypesString = "";
		ArrayList<String> UEtypesArray = new ArrayList<String>();
		for (UE ue : allUEs) {
			if (!UEtypesArray.contains(ue.getVendor())) {
				UEtypesArray.add(ue.getVendor());
				UEtypesString += ue.getVendor() + ", ";
			}
		}
		int numberOfStaticUEs = staticUEs.size() == 1 ? 1 : staticUEs.size() / 2;
		UEtypesString += allUEs.size() + " UEs (" + dynamicUEs.size() + " dynamic UEs, " + numberOfStaticUEs
				+ " static UEs each side).";
		report.setContainerProperties(0, "UeType", UEtypesString);


		if(netspanReachable){
			if(!relayVersions.equals("")){
				// RelayVersion
				report.setContainerProperties(0, "RelayVersions", relayVersions);
			}
			
			// NetspanVar
			report.setContainerProperties(0, "NetspanVar", netspanServer.getNetspanVersion());
			
			// SystemDefaultsProfile
			report.setContainerProperties(0, "SystemDefaultsProfiles", systemDefaultsProfiles);

			// eNodeB Advanced Configuration Profile
			report.setContainerProperties(0, "EnbAdvancedConfigurationProfiles", enbAdvancedConfigurationProfiles);

			// cell Advanced Configuration Profile
			report.setContainerProperties(0, "CellAdvancedConfigurationProfiles", cellAdvancedConfigurationProfiles);
					
			// Mobility Profile
			report.setContainerProperties(0, "MobilityProfiles", mobilityProfiles);

			// RadioProfiles
			report.setContainerProperties(0, "RadioProfiles", radioProfiles);
		}
		
		GeneralUtils.stopLevel();

	}

	public void calledOnceInEndFunc(List<EnodeB> enbInTest, boolean forceUpdate) {
		
		if (!isCalledOnceInEndFunc) {
			isCalledOnceInEndFunc = true;
			
			String customer = TestConfig.getInstace().getCustomer();
			if (customer != null) {
				GeneralUtils.printToConsole("Adding customer " + customer + " to scenario properties");
				report.setContainerProperties(0, "Customer", customer);
			} else
				GeneralUtils.printToConsole("SUT has no costumer.");
			ArrayList<EnodeB> enbInSetup = SetupUtils.getInstance().getAllEnb();
			fillSetupTable();
			
			
			String enbIps = "";
			for (EnodeB enb : enbInSetup){
				
				enbIps += enb.getNetspanName() + "_IP:" + enb.getIpAddress() + ";";
				enb.updateTestedVer();
			}
			if (enbIps.length() > 0 ) {
				enbIps = enbIps.substring(0, enbIps.length() - 1);
		    }
			report.setContainerProperties(0, "enodeB_IP", enbIps);
			
			
		} else if (forceUpdate) {
			fillSetupTable();
			for (EnodeB enb : enbInTest)
				enb.updateTestedVer();
		}
	}
}
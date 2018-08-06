package EnodeB;

import TestingServices.TestConfig;
import Utils.GeneralUtils;
import jsystem.framework.report.Reporter;

public class AirVelocity extends EnodeB {
	private static final String CONTROL_COMPONENT_HW_NAME = "FSM";

	@Override
	public void init() throws Exception {
		super.init();
		setSWTypeInstance(22);
		architecture = Architecture.FSM;
	}

	@Override
	public String getControlComponenetHwName() {
		return CONTROL_COMPONENT_HW_NAME;
	}

	/**
	 * @author sshahaf
	 * enable Mac To Phy by Scrips in bs/bin - enable_phytrace.sh and enable_phytrace_tcpdump
	 * remote will add an IP from SUT and local will not include IP and files will be under /home/tmp.
	 * remote since FSM 15.50.205
	 */
	@Override
	public void enableMACtoPHYcapture(SnifferFileLocation fileLocation) {
		String snifferIp = "";
		String stringResult = "";		
		if (fileLocation == SnifferFileLocation.Remote){
			snifferIp = " "+TestConfig.getInstace().getSnifferIp();
			if(snifferIp.equals(" ")){
				report.report("there is no IP in SUT Section",Reporter.WARNING);
				return;
			}
		}
		
		if(isMACtoPHYEnabled()){
			report.report("Enabling Sniffer");
			if(snifferIp.equals(" null")){
				snifferIp = "";
			}
			stringResult = XLP.shell("/bs/bin/enable_phytrace.sh"+snifferIp);
			GeneralUtils.printToConsole(stringResult);
		}else{
			report.report("please check SUT to enable sniffer option");
		}
	}

	/***
	 * @author sshahaf
	 */
	@Override
	public void disableMACtoPHYcapture() {
		
		report.report("disable Sniffer");
		GeneralUtils.printToConsole("FSM System Enable Sniffer");
		report.report("using command - /bs/bin/disable_phytrace.sh");
		try{
			XLP.shell("/bs/bin/disable_phytrace.sh");
		}catch(Exception d){
			report.report("error while trying to send /bs/bin/disable_phytrace.sh command");
			d.printStackTrace();
		}
	}
	
	// @Override
	// public String getNodeId() {
	// if ( nodeId == null )
	// nodeId = dbGet( "Inventory" ).get( "serialNumber" )[NODE_ID_INDEX];

	// return nodeId;
	// }

}

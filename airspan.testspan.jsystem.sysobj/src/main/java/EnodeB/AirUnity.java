package EnodeB;

import EnodeB.Components.EnodeBComponent;
import Netspan.NetspanServer;
import Netspan.API.Enums.ImageType;
import Netspan.API.Software.SoftwareStatus;
import UE.UnityRelayUE;
import Utils.GeneralUtils;
import Utils.GeneralUtils.RebootType;
import jsystem.framework.report.Reporter;

public class AirUnity extends EnodeBWithDonor{
	
	public AirUnity() {
		super();
		debugPort.setDEBUG_PORT("169.254.1.128");
		debugPort.setDEBUG_PORT_USERNAME(EnodeBComponent.SECURED_USERNAME);
		debugPort.setDEBUG_PORT_PASSWOED(EnodeBComponent.SECURED_PASSWORD);
		WAIT_FOR_ALL_RUNNING_TIME = 20 * 60 * 1000;
	}
	
	@Override
	public void init() throws Exception {
		super.init();
	}

	@Override
	public String getRelayRunningVersion() {
		String relayRunningVersion = "";
		try {
			SoftwareStatus softwareStatus = NetspanServer.getInstance().getSoftwareStatus(this.getNetspanName(), ImageType.RELAY);
			if(softwareStatus != null){
				relayRunningVersion = softwareStatus.RunningVersion;
			}
			else
			{
				relayRunningVersion = ((UnityRelayUE)relay).getVersion();
			}
		} catch (Exception e) {
			GeneralUtils.printToConsole("FAILED to get Relay Version.");
			e.printStackTrace();
		}
		return relayRunningVersion;
	}

	@Override
	protected boolean rebootExecution(RebootType rebootType) {
		boolean rebooted = false;
		long timeout = 1000 * 60 * 5;
		setExpectBooting(true);
		
		try {
			rebooted = NetspanServer.getInstance().forcedResetNode(getNetspanName(), rebootType);
		} catch (Exception e) {
			report.report("Failed to reset Node Via Netspan", Reporter.WARNING);
			e.printStackTrace();
		}
		
		rebooted = rebooted && waitForReboot(timeout);
		setExpectBooting(false);
		
		if (rebooted){			
			report.report("Node " + this.getNetspanName() + " has been rebooted via Relay.");
		}else{
			report.report("The Enodeb " + this.getName() + " failed to reboot!", Reporter.WARNING);
		}
		
		return rebooted;
	}
}

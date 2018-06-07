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
	protected boolean rebootExecutionViaSnmp(RebootType rebootType) {
		report.report("Can't reboot AirUnity via snmp",Reporter.WARNING);
		return false;
	}
}

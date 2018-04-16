package EnodeB;

import Netspan.NetspanServer;
import Netspan.API.Enums.ImageType;
import Netspan.API.Software.SoftwareStatus;
import Utils.GeneralUtils;

public class AirUnity extends EnodeBWithDonor{
	
	public AirUnity() {
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
		} catch (Exception e) {
			GeneralUtils.printToConsole("FAILED to get Relay Version From Netspan.");
			e.printStackTrace();
		}
		return relayRunningVersion;
	}

}

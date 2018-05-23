package EnodeB;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import EnodeB.Components.EnodeBComponent;
import EnodeB.Components.XLP.DebugPort;
import Netspan.API.Enums.ImageType;

public class Ninja extends EnodeBWithDonor{	
	
	public Ninja() {
		super();
		debugPort.setDEBUG_PORT("192.168.0.20");
		debugPort.setDEBUG_PORT_USERNAME(EnodeBComponent.ADMIN_USERNAME);
		debugPort.setDEBUG_PORT_PASSWOED(EnodeBComponent.ADMIN_PASSWORD);
		WAIT_FOR_ALL_RUNNING_TIME = 10 * 60 * 1000;
		setSWTypeInstance(22);
		//addCliDebugFlags("db set debugFlags [*] bhQosSimulateCpeResp=1","!echo 'SKIP_CMPV2=1' > /bs/data/debug_security.cfg");
	}
	
	@Override
	public void init() throws Exception {
		super.init();
	}
	
	@Override
	public String getLoggerUploadAllEnodebIP() {
		String loggerUploadAllEnodebIP = null;
		if(relay != null){
			loggerUploadAllEnodebIP = relay.getWanIpAddress();
		}else{
			report.report("Altair's WAN IP is missing.");
		}
		return loggerUploadAllEnodebIP;
	}
	@Override
	public String getRelayRunningVersion() {
		String response = XLP.shell("ls /bs/relay/ue/altair");
		String regexPattern = 	"(FL(_\\d+)+)";
		Pattern pattern = Pattern.compile(regexPattern);
		Matcher matcher = pattern.matcher(response);
		String altairVersion =  matcher.find() ? matcher.group(1) : "";
		if(altairVersion == ""){
			response = XLP.sendCommandsOnSession(XLP.getSerialSession().getName(), EnodeBComponent.SHELL_PROMPT, "ls /bs/relay/ue/altair", "");
			matcher = pattern.matcher(response);
			altairVersion =  matcher.find() ? matcher.group(1) : "";
		}
		return altairVersion;
	}

	@Override
	public ImageType getImageType() {
		return ImageType.AirDensity;
	}
}

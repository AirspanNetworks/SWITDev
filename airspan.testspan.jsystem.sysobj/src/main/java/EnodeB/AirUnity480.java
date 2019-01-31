package EnodeB;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import EnodeB.Components.EnodeBComponent;
import Netspan.API.Enums.ImageType;

public class AirUnity480 extends EnodeBWithDonor{
	
	public AirUnity480() {
		super();
		setSWTypeInstance(22);
	}
	
	@Override
	public void init() throws Exception {
		super.init();
	}
	
	@Override
	public ImageType getImageType() {
		return ImageType.COMBINED_LTE_RELAY;
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
}

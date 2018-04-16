package Attenuators;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Attenuators.Utils.ControlledAttenuator;


public class AeroflexAttenuator extends ControlledAttenuator {

	public static final int CHANNELS = 2;


	@Override
	public void init() throws Exception {
		super.init();
		if (maxAttenuation == 0)
			maxAttenuation = 103;
	}


	@Override
	public boolean setAttenuation(float attenuation) {
		boolean status = true;
		for (int i = 1; i <= CHANNELS; i++) {
			status = status && serialCom.sendString("CHAN " + i + "; ATTN " + (int) attenuation + "; ATTN?\r", false);
		}
		return status;
	}

	@Override
	public float getAttenuation() {
		serialCom.sendString("ATTN?\r", false);
		String result = serialCom.getResult().trim();

		Pattern pattern = Pattern.compile("\\d+(\\.\\d{1,2})?");
		Matcher matcher = pattern.matcher(result);
		if (matcher.find()) {
			return Float.parseFloat(matcher.group(0));
		}

		return -999;
	}

}

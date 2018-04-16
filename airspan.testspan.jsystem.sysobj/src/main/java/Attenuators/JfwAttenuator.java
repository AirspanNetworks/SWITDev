package Attenuators;

import Attenuators.Utils.ControlledAttenuator;
import Utils.GeneralUtils;

/**
 * 
 * JFW (50BA-002-63) Attenuator
 *
 */
public class JfwAttenuator extends ControlledAttenuator {
	

	@Override
	public void init() throws Exception {
		super.init();
		if (maxAttenuation == 0)
			maxAttenuation = 63;
	}


	@Override
	public boolean setAttenuation(float attenuation) {
		GeneralUtils.unSafeSleep(500);
		serialCom.clearBuffer();
		boolean status = serialCom.sendString("SAA " + (int)attenuation + "\r", false);
		serialCom.clearBuffer();
		return status;
	}


	@Override
	public float getAttenuation() {
		GeneralUtils.unSafeSleep(500);
		serialCom.clearBuffer();
		this.serialCom.sendString("RAA\r", false);
		int tryouts=0;
		String result="";
		while(result.isEmpty() && tryouts<3){
			result = serialCom.getResult().trim();
			if(result.isEmpty())
					report.report( "Could not get info from JFW attenuator...Got Empty string." );
			tryouts++;
		}
		try{
			String[] res = result.split("\n");
			String[] val = res[res.length-1].split(" ");
			
			return Float.parseFloat(val[val.length-1]);			
		}catch(Exception e){
			e.printStackTrace();
			return GeneralUtils.ERROR_VALUE;
		}
	}

}
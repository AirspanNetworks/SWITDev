package Attenuators;

import Attenuators.Utils.ControlledAttenuator;

public class PhantomAttenuatorSet extends AttenuatorSet{

	@Override
	public void init() throws Exception {
		attenuators = new ControlledAttenuator[0];
		super.init();
		setLifeTime(TEST_LIFETIME);
		report.report("Unable to find Rudat Set in SUT - continue test without attn. abilities.");
	}
	
	public boolean setAttenuation(float attenuationValue){
		report.report("Trying to set attenuation when no attenuation available in setup.");
		return true;
	}	
	
	public float[] getAttenuation(){
		report.report("Trying to get attenuation when no attenuation available in setup.");
		float[] att = new float[attenuators.length];
	
		for (int i = 0; i < attenuators.length; i++) {
			att[i] = -999;
		}		
		
		return att;		
	}
}

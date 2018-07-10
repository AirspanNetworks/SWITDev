package Attenuators;

import Attenuators.Utils.ControlledAttenuator;
import Utils.GeneralUtils;
import jsystem.framework.system.SystemManagerImpl;
import jsystem.framework.system.SystemObjectImpl;

/**
 * The Class AttenuatorSet.
 */
public class AttenuatorSet extends SystemObjectImpl {

	public ControlledAttenuator[] attenuators;
	protected int maxAttenuation;
	protected int minAttenuation;
	protected int defaultValueAttenuation;
	protected int stepTime = 500;
	protected int attenuationStep = 2;

	public static AttenuatorSet getAttenuatorSet(String attenuatorSetName)
	{
		try {
			return (AttenuatorSet) SystemManagerImpl.getInstance().getSystemObject(attenuatorSetName);
		} catch (Exception e) {
			AttenuatorSet retSet = new PhantomAttenuatorSet();
			try {
				retSet.init();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			return retSet;
		}
	}
	
	
	
	@Override
	public void init() throws Exception {
		super.init();
		setLifeTime(TEST_LIFETIME);
		for (ControlledAttenuator attenuator : attenuators) {
			attenuator.connect();
			attenuator.setAttenuation(defaultValueAttenuation);
		}
	}
	
	public boolean setAttenuation(float attenuationValue){
		boolean status = true;
		
		for(ControlledAttenuator att: attenuators){
			
			if(!att.isConnected()) att.connect();
			if(!att.isConnected()){
				GeneralUtils.printToConsole("Attenautor is not connected");
				return false;
			}
			
			status = status && att.setAttenuation(attenuationValue);
		}
		
		return status;
	}	
	
	public float[] getAttenuation(){
		
		float[] att = new float[attenuators.length];
	
		for (int i = 0; i < attenuators.length; i++) {
			
			if(!attenuators[i].isConnected()) attenuators[i].connect();
			if(!attenuators[i].isConnected()) {
				att[i] = GeneralUtils.ERROR_VALUE;
				continue;
			}
			
			att[i] = attenuators[i].getAttenuation();
		}
		
		return att;
		
	}


	public int getMaxAttenuation() {
		return maxAttenuation;
	}

	public void setMaxAttenuation(int maxAttenuation) {
		this.maxAttenuation = maxAttenuation;
	}

	public int getMinAttenuation() {
		return minAttenuation;
	}

	public void setMinAttenuation(int minAttenuation) {
		this.minAttenuation = minAttenuation;
	}

	public int getDefaultValueAttenuation() {
		return defaultValueAttenuation;
	}

	public void setDefaultValueAttenuation(int defaultValueAttenuation) {
		this.defaultValueAttenuation = defaultValueAttenuation;
	}

	public int getStepTime() {
		return stepTime;
	}

	public void setStepTime(int stepTime) {
		this.stepTime = stepTime;
	}

	public int getAttenuationStep() {
		return attenuationStep;
	}

	public void setAttenuationStep(int attenuationStep) {
		this.attenuationStep = attenuationStep;
	}
}

package Attenuators.Utils;

import Utils.MoxaCom;
import jsystem.framework.system.SystemObjectImpl;

/**
 * Controlled Attenuator.
 */
public abstract class ControlledAttenuator extends SystemObjectImpl{
	
	protected float attenuation;
	protected float maxAttenuation;
	public MoxaCom serialCom;


	
	@Override
	public void init() throws Exception {
		super.init();
		
		setLifeTime(TEST_LIFETIME);
		
		if (getParent() != null) {
			String index = getName().substring(getName().indexOf("[") + 1);
			index = index.substring(0, index.length() - 1);
			setName(getParent().getName() + "." + getClass().getSimpleName() + index);
		}		
	}
	
	

	@Override
	public void close() {
		this.disconnect();
	}



	public abstract boolean setAttenuation(float attenuation);
	public abstract float getAttenuation();
	
	public void connect(){
		serialCom.connect();
	}
	
	public void disconnect(){
		serialCom.close();
	}
	
		
	public float getMaxAttenuation() {
		return maxAttenuation;
	}

	public void setMaxAttenuation(float maxAttenuation) {
		this.maxAttenuation = maxAttenuation;
	}

	
	
	public final MoxaCom getSerialCom() {
		return serialCom;
	}



	public final void setSerialCom(MoxaCom serialCom) {
		this.serialCom = serialCom;
	}



	public final boolean isConnected(){
		return serialCom.isConnected();
	}

	
	
}

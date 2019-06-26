package EnodeB.Components.XLP;

import Utils.PasswordUtils;

public class XLP_16_0 extends XLP_15_2 {

	@Override
	public void init() throws Exception {
		setScpUsername(PasswordUtils.ROOT_USERNAME);
		setScpPort(2020);
		super.init();
	}	
	
	@Override
	public String getDefaultSSHUsername(){
		return PasswordUtils.COSTUMER_USERNAME;
	}
	
	@Override
	public String getDefaultSerialUsername(){
		return PasswordUtils.COSTUMER_USERNAME;
	}
}
package EnodeB.Components.XLP;

public class XLP_16_0 extends XLP_15_2 {

	@Override
	public void init() throws Exception {
		setScpUsername(UNSECURED_USERNAME);
		setScpPort(2020);
		super.init();
	}	
	
	@Override
	public String getDefaultSSHUsername(){
		return SECURED_USERNAME;
	}
	
	@Override
	public String getDefaultSerialUsername(){
		return SECURED_USERNAME;
	}
}

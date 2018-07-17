package EnodeB;

public class AV100C extends AirVelocity{
	
	private static final String CONTROL_COMPONENT_HW_NAME = "FSMv4";
	
	@Override
	public void init() throws Exception {
		super.init();
		architecture = Architecture.FSMv4;
	}

	@Override
	public String getControlComponenetHwName() {
		return CONTROL_COMPONENT_HW_NAME;
	}
}

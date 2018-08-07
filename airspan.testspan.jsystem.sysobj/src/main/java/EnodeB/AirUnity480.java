package EnodeB;

import Netspan.API.Enums.ImageType;

public class AirUnity480 extends AirVelocity{
	
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
}

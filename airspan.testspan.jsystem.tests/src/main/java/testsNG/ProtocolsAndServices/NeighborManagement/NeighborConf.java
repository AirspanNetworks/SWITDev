package testsNG.ProtocolsAndServices.NeighborManagement;

import Netspan.API.Enums.HandoverTypes;
import Netspan.API.Enums.HoControlStateTypes;
import Netspan.API.Enums.X2ControlStateTypes;

public class NeighborConf {
	private HoControlStateTypes hoControlStatType;
	private X2ControlStateTypes x2ControlStatType;
	private HandoverTypes hoType;
	private boolean isStatic;

	public NeighborConf(HoControlStateTypes hoControl, X2ControlStateTypes x2Control, HandoverTypes hoType, boolean staticStatus) {
		this.hoControlStatType = hoControl;
		this.x2ControlStatType = x2Control;
		this.hoType = hoType;
		this.isStatic = staticStatus;
	}

	public HoControlStateTypes getHoControlStatType() {
		return hoControlStatType;
	}

	public X2ControlStateTypes getX2ControlStatType() {
		return x2ControlStatType;
	}

	public HandoverTypes getHoType() {
		return hoType;
	}

	public boolean isStatic() {
		return isStatic;
	}
	
	/**
	 * return neighbor configuration class according to counter
	 * 0 - HoControlStateTypes.ALLOWED, X2ControlStateTypes.AUTOMATIC,HandoverTypes.TRIGGER_X_2, static
	 * 1 - HoControlStateTypes.ALLOWED, X2ControlStateTypes.AUTOMATIC,HandoverTypes.S_1_ONLY, static
	 * 2 - HoControlStateTypes.ALLOWED, X2ControlStateTypes.NOT_ALLOWED,HandoverTypes.S_1_ONLY, static
	 * 3 - HoControlStateTypes.PROHIBITED, X2ControlStateTypes.AUTOMATIC,HandoverTypes.TRIGGER_X_2, static
	 * 4 - HoControlStateTypes.PROHIBITED, X2ControlStateTypes.NOT_ALLOWED,HandoverTypes.TRIGGER_X_2, static
	 * default case is null!
	 * @param neighborconf
	 * @return
	 */
	
	public static NeighborConf determineNeighborConf(int neighborconf) {
		NeighborConf conf = null;
		switch(neighborconf){
		case 0:
			conf = new NeighborConf(HoControlStateTypes.ALLOWED , X2ControlStateTypes.AUTOMATIC, HandoverTypes.TRIGGER_X_2, true);
			break;
		
		case 1:
			conf = new NeighborConf(HoControlStateTypes.ALLOWED , X2ControlStateTypes.AUTOMATIC, HandoverTypes.S_1_ONLY, true);
			break;
		
		case 2:
			conf = new NeighborConf(HoControlStateTypes.ALLOWED , X2ControlStateTypes.NOT_ALLOWED, HandoverTypes.S_1_ONLY, true);
			break;
		
		case 3:
			conf = new NeighborConf(HoControlStateTypes.PROHIBITED , X2ControlStateTypes.AUTOMATIC, HandoverTypes.TRIGGER_X_2, true);
			break;
		
		case 4:	
			conf = new NeighborConf(HoControlStateTypes.PROHIBITED , X2ControlStateTypes.NOT_ALLOWED, HandoverTypes.TRIGGER_X_2, true);
			break;
		}
		return conf;
	}
}
package Netspan.NBI_16_5;

import java.util.ArrayList;

import EnodeB.EnodeB;
import Netspan.NBI_16_0.Netspan_16_0_abilities;

public interface Netspan_16_5_abilities extends Netspan_16_0_abilities {
	boolean setEmergencyAreaIds(EnodeB dut, ArrayList<Integer> ids);
}

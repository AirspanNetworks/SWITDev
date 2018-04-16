package Attenuators.Utils;

import Attenuators.RudatAttenuator.RudatType;

public enum ERudatAttenuatorCommands {

	
	/**  COMMAND_NAME(COM, TELNET)  */
	ATTENUATION_GET("A\r", "ATT?\r\n"),
	ATTENUATION_SET("B%sE\r", "SETAtt=%s\r\n");
	
	
	String ComCommand;
	String IPCommand;
	
	ERudatAttenuatorCommands(String comCommand, String IPCommand)
	{
		ComCommand = comCommand;
		this.IPCommand = IPCommand;
	}
	
	public String getCommand(RudatType mode, Object args) {
		if (mode == RudatType.IP)
			return String.format(IPCommand, args);
		else
			return String.format(ComCommand, args);
	}
	
	public String getCommand(RudatType mode) {
		return getCommand(mode, null);
	}
}

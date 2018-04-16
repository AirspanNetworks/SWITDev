package EnodeB;

import java.util.Arrays;
import java.util.Hashtable;

import junit.framework.Assert;
import EnodeB.Components.Wintegra;
import EnodeB.Components.Log.Logger;

public class Air4G extends EnodeBWithDAN {
	
	public Wintegra Wintegra;
	
	@Override
	public void init() throws Exception {
		super.init();
		if (Wintegra == null)
			Assert.fail(String.format("Air4G \"%s\" cannot find wintegra", getName()));
	}
	
	@Override
	public Logger[] getLoggers() {
		Logger[] loggers = super.getLoggers();
		loggers = Arrays.copyOf(loggers, loggers.length + 1);
		loggers[loggers.length - 1] = Wintegra.getLogger();
		return loggers;
	}

	@Override
	public int getUplinkFreqency() {
		Hashtable<String, String[]> table17 = Wintegra.get(17);
		if (table17 != null)
			return Integer.parseInt(table17.get("28")[0].substring(0, table17.get("28")[0].length() - 1));
		
		return -1; 
	}
	
	@Override
	public int getDownlinkFreqency() {
		Hashtable<String, String[]> table17 = Wintegra.get(17);
		if (table17 != null)
			return Integer.parseInt(table17.get("29")[0].substring(0, table17.get("29")[0].length() - 1));
		
		return -1; 
	}
	
	@Override
	public void setUplinkFrequency(int uplink) {
		super.setUplinkFrequency(uplink);
		Wintegra.set(17, "1U", "28", uplink + "");
	}
	
	@Override
	public void setDownlinkFrequency(int downlink) {
		super.setDownlinkFrequency(downlink);
		Wintegra.set(17, "1U", "29", downlink + "");
	}


	@Override
	public void enableMACtoPHYcapture(SnifferFileLocation fileLocation) {
		report.report("No implementation for MaCToPhy in Air4G!");
	}


	@Override
	public void disableMACtoPHYcapture() {
		report.report("ERROR - no implementation for disableMACtoPHY method in Air4G");
	}
	
}

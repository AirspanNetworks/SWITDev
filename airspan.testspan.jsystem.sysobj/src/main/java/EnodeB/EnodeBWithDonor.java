package EnodeB;

import EnodeB.Components.Log.Logger;
import EnodeB.Components.XLP.DebugPort;
import UE.UE;
import Utils.GeneralUtils;
import jsystem.framework.IgnoreMethod;
import jsystem.framework.report.Reporter;
import jsystem.framework.system.SystemManagerImpl;

public abstract class EnodeBWithDonor extends AirVelocity{	
	
	protected String donorName;
	protected EnodeB donor;
	protected UE relay;
	public DebugPort DebugPort;
	public abstract String getRelayRunningVersion();
	
	@Override
	public void init() throws Exception {
		super.init();
		setDonor(donorName);
		DebugPort = new DebugPort();
	}
	
	public UE getRelay() {
		return relay;
	}

	public String getDonorName() {
		return donorName;
	}

	public void setDonorName(String donorName) {
		this.donorName = donorName;
	}
	
	public EnodeB getDonor() {
		return donor;
	}

	public void setDonor(String donorName) {
		GeneralUtils.printToConsole("Load Donor " + donorName);
		try {
			this.donor = (EnodeB) SystemManagerImpl.getInstance().getSystemObject(donorName);
			hasDonor = true;
		} catch (Exception e) {
			report.report("Failed to set Donor.", Reporter.WARNING);
			e.printStackTrace();
		}
		GeneralUtils.printToConsole("Donor loaded" + this.donor.getName());
	}
	

	
	/**
	 * Gets the loggers.
	 *
	 * @return the loggers
	 */
	@Override
	@IgnoreMethod
	public Logger[] getLoggers() {
		return new Logger[] {XLP.getLogger(), donor.XLP.getLogger()};
	}
}

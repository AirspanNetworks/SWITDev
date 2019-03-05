package EnodeB;

import EnodeB.Components.Log.Logger;
import EnodeB.Components.XLP.DebugPort;
import UE.UE;
import Utils.GeneralUtils;
import Utils.Pair;
import jsystem.framework.IgnoreMethod;
import jsystem.framework.report.Reporter;
import jsystem.framework.system.SystemManagerImpl;

public abstract class EnodeBWithDonor extends AirVelocity{	
	public final long FIRST_ALTAIR_ATTACH_EXPECTED_DURATION_IN_MILI = ((3 * 60) + 50) * 1000;
	public final long FIRST_DNS_QUERY_EXPECTED_DURATION_IN_MILI = ((1 * 60) + 20) * 1000;
	public final long FIRST_IPSEC_BRING_UP_EXPECTED_DURATION_IN_MILI = ((1 * 60) + 40) * 1000;
	public final long COLD_RELAY_PNP_EVENTS_EXPECTED_DURATION_IN_MILI = 20 * 1000;
	public final long SCAN_LIST_EVENTS_EXPECTED_DURATION_IN_MILI = ((1 * 60) + 10) * 1000;
	public final long SECOND_ALTAIR_ATTACH_EXPECTED_DURATION_IN_MILI = ((1 * 60) + 10) * 1000;
	public final long SECOND_DNS_QUERY_EXPECTED_DURATION_IN_MILI = 35 * 1000;
	public final long SECOND_IPSEC_BRING_UP_EXPECTED_DURATION_IN_MILI = 20 * 1000;
	public final long WARM_RELAY_PNP_EVENTS_EXPECTED_DURATION_IN_MILI = 20 * 1000;
	public final long COLD_ENODEB_PNP_EVENTS_EXPECTED_DURATION_IN_MILI = 30 * 1000;
	public final long WAIT_FOR_ALL_RUNNING_EXPECTED_DURATION_IN_MILI = 50 * 1000;
	
	protected final Pair[] expectedDurationsAndStageNamesOrderedOneRelayAttachForWarmReboot = {
			new Pair<Long, String>((long)0, "Warm Reboot."),
			new Pair<Long, String>((long)FIRST_ALTAIR_ATTACH_EXPECTED_DURATION_IN_MILI, "Relay Attach."),
			new Pair<Long, String>((long)FIRST_IPSEC_BRING_UP_EXPECTED_DURATION_IN_MILI, "SNMP Availability / IPSec Bring Up."),
			new Pair<Long, String>((long)2 * 60 * 1000, "All Running.")
	};
	
	protected final Pair[] expectedDurationsAndStageNamesOrderedTwoRelayAttachForColdReboot = {
			new Pair<Long, String>((long)0, "Cold Reboot."),
			new Pair<Long, String>((long)FIRST_ALTAIR_ATTACH_EXPECTED_DURATION_IN_MILI, "First Relay Attach."),
			new Pair<Long, String>((long)FIRST_DNS_QUERY_EXPECTED_DURATION_IN_MILI, "First DNS Query."),
			new Pair<Long, String>((long)FIRST_IPSEC_BRING_UP_EXPECTED_DURATION_IN_MILI, "First IPSec Bring Up / SNMP Availability."),
			new Pair<Long, String>((long)COLD_RELAY_PNP_EVENTS_EXPECTED_DURATION_IN_MILI, "Cold Relay PnP."),
			new Pair<Long, String>((long)SECOND_ALTAIR_ATTACH_EXPECTED_DURATION_IN_MILI, "Scan List And Second Relay Attach."),
			new Pair<Long, String>((long)SECOND_DNS_QUERY_EXPECTED_DURATION_IN_MILI, "Second DNS Query."),
			new Pair<Long, String>((long)SECOND_IPSEC_BRING_UP_EXPECTED_DURATION_IN_MILI, "Second IPSec Bring Up / SNMP Availability."),
			new Pair<Long, String>((long)WARM_RELAY_PNP_EVENTS_EXPECTED_DURATION_IN_MILI, "Warm Relay PnP."),
			new Pair<Long, String>((long)COLD_ENODEB_PNP_EVENTS_EXPECTED_DURATION_IN_MILI, "Cold eNodeB PnP."),
			new Pair<Long, String>((long)WAIT_FOR_ALL_RUNNING_EXPECTED_DURATION_IN_MILI, "All Running.")
	};
	
	protected final Pair[] expectedDurationsAndStageNamesOrderedWithSoftwareDownloadTwoRelayAttachForColdReboot = {
			new Pair<Long, String>((long)0, "Cold Reboot."),
			new Pair<Long, String>((long)FIRST_ALTAIR_ATTACH_EXPECTED_DURATION_IN_MILI, "First Altair Attach."),
			new Pair<Long, String>((long)FIRST_DNS_QUERY_EXPECTED_DURATION_IN_MILI, "First DNS Query."),
			new Pair<Long, String>((long)FIRST_IPSEC_BRING_UP_EXPECTED_DURATION_IN_MILI, "First IPSec Bring Up."),
			new Pair<Long, String>((long)COLD_RELAY_PNP_EVENTS_EXPECTED_DURATION_IN_MILI, "Cold Relay PnP."),
			new Pair<Long, String>((long)SECOND_ALTAIR_ATTACH_EXPECTED_DURATION_IN_MILI, "Scan List And Second Altair Attach."),
			new Pair<Long, String>((long)SECOND_DNS_QUERY_EXPECTED_DURATION_IN_MILI, "Second DNS Query."),
			new Pair<Long, String>((long)SECOND_IPSEC_BRING_UP_EXPECTED_DURATION_IN_MILI, "Second IPSec Bring Up."),
			new Pair<Long, String>((long)WARM_RELAY_PNP_EVENTS_EXPECTED_DURATION_IN_MILI, "Warm Relay PnP."),
			new Pair<Long, String>((long)COLD_ENODEB_PNP_EVENTS_EXPECTED_DURATION_IN_MILI, "Cold eNodeB PnP & Software Download."),
			new Pair<Long, String>((long)1, "Reboot After Software Download."),
			new Pair<Long, String>((long)FIRST_ALTAIR_ATTACH_EXPECTED_DURATION_IN_MILI, "First Altair Attach After Software Download."),
			new Pair<Long, String>((long)FIRST_DNS_QUERY_EXPECTED_DURATION_IN_MILI, "First DNS Query After Software Download."),
			new Pair<Long, String>((long)FIRST_IPSEC_BRING_UP_EXPECTED_DURATION_IN_MILI, "First IPSec Bring Up After Software Download."),
			new Pair<Long, String>((long)COLD_RELAY_PNP_EVENTS_EXPECTED_DURATION_IN_MILI, "Cold Relay PnP After Software Download."),
			new Pair<Long, String>((long)SECOND_ALTAIR_ATTACH_EXPECTED_DURATION_IN_MILI, "Scan List And Second Altair Attach After Software Download."),
			new Pair<Long, String>((long)SECOND_DNS_QUERY_EXPECTED_DURATION_IN_MILI, "Second DNS Query After Software Download."),
			new Pair<Long, String>((long)SECOND_IPSEC_BRING_UP_EXPECTED_DURATION_IN_MILI, "Second IPSec Bring Up."),
			new Pair<Long, String>((long)WARM_RELAY_PNP_EVENTS_EXPECTED_DURATION_IN_MILI, "Warm Relay PnP After Software Download."),
			new Pair<Long, String>((long)COLD_ENODEB_PNP_EVENTS_EXPECTED_DURATION_IN_MILI, "Cold eNodeB PnP."),
			new Pair<Long, String>((long)COLD_ENODEB_PNP_EVENTS_EXPECTED_DURATION_IN_MILI, "eNodeB Software Activate Completed"),
			new Pair<Long, String>((long)WAIT_FOR_ALL_RUNNING_EXPECTED_DURATION_IN_MILI, "All Running.")
	};
	
	protected String donorName;
	protected EnodeB donor;
	public UE relay;
	public DebugPort debugPort;
	public abstract String getRelayRunningVersion();
	
	public EnodeBWithDonor() {
		super();
		debugPort = new DebugPort();
	}

	@Override
	public void init() throws Exception {
		super.init();
		setDonor(donorName);
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
			GeneralUtils.printToConsole("Donor loaded" + this.donor.getName());
		} catch (Exception e) {
			report.report("Failed to set Donor.", Reporter.WARNING);
			e.printStackTrace();
		}
	}
	
	/**
	 * Gets the loggers.
	 *
	 * @return the loggers
	 */
	@Override
	@IgnoreMethod
	public Logger[] getLoggers() {
		Logger[] loggers;
		if(donor != null ){
			loggers = new Logger[] { XLP.getLogger(), donor.XLP.getLogger()};	
		}
		else{
			loggers = new Logger[] { XLP.getLogger()};
		}
		return loggers;
	}
	
	@Override
	public Pair<Long, String>[] getExpectedDurationsAndStageNamesOrderedForColdReboot() {
		if(swUpgradeDuringPnP){
			return expectedDurationsAndStageNamesOrderedWithSoftwareDownloadTwoRelayAttachForColdReboot;
		}else{
			return expectedDurationsAndStageNamesOrderedTwoRelayAttachForColdReboot;
		}
	}
	
	@Override
	public Pair<Long, String>[] getExpectedDurationsAndStageNamesOrderedForWarmReboot() {
		return expectedDurationsAndStageNamesOrderedOneRelayAttachForWarmReboot;
	}
}

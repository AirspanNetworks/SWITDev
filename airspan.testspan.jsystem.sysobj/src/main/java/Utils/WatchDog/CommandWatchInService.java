package Utils.WatchDog;

import EnodeB.EnodeB;
import Netspan.API.Enums.EnbStates;
import Utils.GeneralUtils;
import jsystem.framework.report.ListenerstManager;
import jsystem.framework.report.Reporter;

public class CommandWatchInService extends Command {

	private EnodeB enb;
	private boolean wasFailPrinted;
	private volatile boolean enabled = true;
	int time = 1;

	public CommandWatchInService(EnodeB enodeB) {
		name = "CommandWatchInService";
		enb = enodeB;

	}

	@Override
	public void run() {
		if (enabled) {
			GeneralUtils.printToConsole("CommandWatchInService runs for the " + time + " time");
			synchronized (enb.inServiceStateLock) {
				GeneralUtils.printToConsole("CommandWatchInService passed syncronized for the " + time + " time");
				if (enb.expecteInServiceState) {
					GeneralUtils.printToConsole("CommandWatchInService passed expecteInServiceState for the " + time + " time");
					if (enb.getServiceState().equals(EnbStates.OUT_OF_SERVICE)) {
						if (!wasFailPrinted) {
							GeneralUtils.report.report(enb.getName() + " is in Out of service state", Reporter.FAIL);
							GeneralUtils.reportHtmlLink("cell show operationalStatus",
									enb.lteCli("cell show operationalStatus"));
							wasFailPrinted = true;
						}
					} else {
						wasFailPrinted = false;
					}
				}
			}
			time++;
		}
	}

	@Override
	public int getExecutionDelaySec() {
		return 10;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}

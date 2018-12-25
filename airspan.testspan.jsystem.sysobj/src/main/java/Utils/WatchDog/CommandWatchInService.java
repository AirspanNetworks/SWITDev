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

	public CommandWatchInService(EnodeB enodeB) {
		name = "CommandWatchInService";
		enb = enodeB;
	}

	@Override
	public void run() {
		if (enabled) {
			synchronized (enb.inServiceStateLock) {
				if (enb.expecteInServiceState) {
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

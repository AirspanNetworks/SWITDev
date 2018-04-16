package Utils.WatchDog;

import EnodeB.EnodeB;
import Netspan.API.Enums.EnbStates;
import Utils.GeneralUtils;
import jsystem.framework.report.ListenerstManager;
import jsystem.framework.report.Reporter;

public class CommandWatchInService extends Command {
	private Reporter report = ListenerstManager.getInstance();
	EnodeB enb;
	boolean wasFailPrinted;
	
	public CommandWatchInService(EnodeB enodeB) {
		name="CommandWatchInService";
		enb = enodeB;
		
	}
	
	
	@Override
	public void run() {
		synchronized (enb.inServiceStateLock){
			if (enb.expecteInServiceState){
				if (enb.getServiceState().equals(EnbStates.OUT_OF_SERVICE)){
					if (!wasFailPrinted){
						GeneralUtils.report.report(enb.getName() + " is in Out of service state", Reporter.FAIL);
						GeneralUtils.reportHtmlLink("cell show operationalStatus", enb.lteCli("cell show operationalStatus"));
						wasFailPrinted = true;
					}
				}
				else{
					wasFailPrinted = false;
				}
			}
		}	
	}

	@Override
	public int getExecutionDelaySec() {
		return 10;
	}
}

package testsNG.Actions.Utils;

import java.io.IOException;
import java.util.List;

import EnodeB.EnodeBWithDonor;

public class ParallelCommandsThreadEnodbWithDonor extends ParallelCommandsThreadEnodeBComponent{
	private ParallelCommandsThreadEnodeBComponent donorSyncCommands;

	public ParallelCommandsThreadEnodbWithDonor(List<String> enodebCmdSet, EnodeBWithDonor enbWithDonor, List<String> donorCmdSet, int responseTimeout, int waitBetweenCommands) throws IOException {
		super(enodebCmdSet, enbWithDonor, enbWithDonor.getXLPName(), responseTimeout, waitBetweenCommands);
		donorSyncCommands = new ParallelCommandsThreadEnodeBComponent(donorCmdSet, enbWithDonor.getDonor(), enbWithDonor.getDonor().getXLPName(), responseTimeout, waitBetweenCommands);
	}

	@Override
	public void start() {
		super.start();
		donorSyncCommands.start();
	}
	
	@Override
	public boolean stopCommands() {
		super.stopCommands();
		return donorSyncCommands.stopCommands();
	}

	@Override
	public boolean moveFileToReporterAndAddLink() {
		super.moveFileToReporterAndAddLink();
		return donorSyncCommands.moveFileToReporterAndAddLink();
	}
	
	@Override
	public void localJoin() throws InterruptedException{
		join();
		donorSyncCommands.join();
    }
}

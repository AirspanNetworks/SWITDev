package testsNG.Actions.Utils;

import java.io.IOException;
import java.util.List;

import EnodeB.EnodeBWithDonor;
import EnodeB.Ninja;

public class ParallelCommandsThreadEnodbWithDonor extends ParallelCommandsThreadEnodeBComponent{
	private ParallelCommandsThreadEnodeBComponent donorSyncCommands;

	public ParallelCommandsThreadEnodbWithDonor(List<String> enodebCmdSet, EnodeBWithDonor enbWithDonor, List<String> donorCmdSet) throws IOException {
		super(enodebCmdSet, enbWithDonor, enbWithDonor.getXLPName());
		donorSyncCommands = new ParallelCommandsThreadEnodeBComponent(donorCmdSet, enbWithDonor.getDonor(), enbWithDonor.getDonor().getXLPName());
	}

	@Override
	public void start() {
		super.start();
		donorSyncCommands.start();
	}
	
	@Override
	public boolean stopCommands() {
		super.stopCommands();
		boolean flag = donorSyncCommands.stopCommands();
		return flag;
	}

	@Override
	public boolean moveFileToReporterAndAddLink() {
		super.moveFileToReporterAndAddLink();
		boolean flag = donorSyncCommands.moveFileToReporterAndAddLink();
		return flag;
	}
	
	@Override
	public void localJoin() throws InterruptedException{
		join();
		donorSyncCommands.join();
    }
}

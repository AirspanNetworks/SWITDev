package testsNG.Actions.Utils;

import java.io.IOException;
import java.util.List;

import EnodeB.Ninja;

public class ParallelCommandsThreadEnodbWithDonor extends ParallelCommandsThreadEnodeBComponent{
	private ParallelCommandsThreadEnodeBComponent donorSyncCommands;

	public ParallelCommandsThreadEnodbWithDonor(List<String> enodebCmdSet, Ninja ninja, List<String> donorCmdSet) throws IOException {
		super(enodebCmdSet, ninja, ninja.getXLPName());
		donorSyncCommands = new ParallelCommandsThreadEnodeBComponent(donorCmdSet, ninja.getDonor(), ninja.getDonor().getXLPName());
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

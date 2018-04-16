package testsNG.Actions.Utils;

import java.io.IOException;
import java.util.List;

import EnodeB.EnodeBWithDAN;

public class ParallelCommandsThreadEnodbWithDan extends ParallelCommandsThreadEnodeBComponent{
	private ParallelCommandsThreadEnodeBComponent[] danSyncCommands;

	public ParallelCommandsThreadEnodbWithDan(List<String> enodebCmdSet, EnodeBWithDAN enbWithDan, List<String> danCmdSet) throws IOException {
		super(enodebCmdSet, enbWithDan, enbWithDan.getXLPName());
		danSyncCommands = new ParallelCommandsThreadEnodeBComponent[enbWithDan.getDanArrlength()];
		for(int i = 0; i < danSyncCommands.length; i++){
			danSyncCommands[i] = new ParallelCommandsThreadEnodeBComponent(danCmdSet, enbWithDan, enbWithDan.getDanName(i));
		}
	}
	
	@Override
	public void start() {
		super.start();
		for(int i = 0; i < danSyncCommands.length; i++){
			danSyncCommands[i].start();
		}
	}
	
	@Override
	public boolean stopCommands() {
		super.stopCommands();
		boolean flag = true;
		for(int i = 0; i < danSyncCommands.length; i++){
			flag &= danSyncCommands[i].stopCommands();
		}
		return flag;
	}

	@Override
	public boolean moveFileToReporterAndAddLink() {
		super.moveFileToReporterAndAddLink();
		boolean flag = true;
		for(int i = 0; i < danSyncCommands.length; i++){
			flag &= danSyncCommands[i].moveFileToReporterAndAddLink();
		}
		return flag;
	}
	
	@Override
	public void localJoin() throws InterruptedException{
		join();
		for(int i = 0; i < danSyncCommands.length; i++){
			danSyncCommands[i].join();
		}
    }
}

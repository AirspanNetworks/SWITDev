package testsNG.Actions.Utils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import EnodeB.EnodeB;
import EnodeB.EnodeBWithDAN;
import EnodeB.EnodeBWithDonor;

public class ParallelCommandsThread{
	
	ParallelCommandsThreadEnodeBComponent parallelCommandsThreadEnodeBComponent;
	
	public ParallelCommandsThread(List<String> enodebCmdSet, EnodeB enb, List<String> donorCmdSet, List<String> danCmdSet) throws IOException{
		if(enb.hasDonor()){
			if(donorCmdSet == null){
				donorCmdSet = enodebCmdSet;
			}
			parallelCommandsThreadEnodeBComponent = new ParallelCommandsThreadEnodbWithDonor(enodebCmdSet, (EnodeBWithDonor)enb, donorCmdSet);
		}else if(enb.hasDan() && danCmdSet != null){
			parallelCommandsThreadEnodeBComponent = new ParallelCommandsThreadEnodbWithDan(enodebCmdSet, (EnodeBWithDAN)enb, danCmdSet);
		}else{
			parallelCommandsThreadEnodeBComponent = new ParallelCommandsThreadEnodeBComponent(enodebCmdSet, enb, enb.getXLPName());
		}
	}
	
	public void start() {
		parallelCommandsThreadEnodeBComponent.start();
	}
	
	public boolean stopCommands() {
		return parallelCommandsThreadEnodeBComponent.stopCommands();
	}

	public boolean moveFileToReporterAndAddLink() {
		return parallelCommandsThreadEnodeBComponent.moveFileToReporterAndAddLink();
	}
	
	public File getFile(){
		return parallelCommandsThreadEnodeBComponent.getFile();
	}
	
	public void join() throws InterruptedException{
		parallelCommandsThreadEnodeBComponent.localJoin();
    }
}

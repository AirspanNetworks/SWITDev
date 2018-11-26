package testsNG.Actions.Utils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import EnodeB.EnodeB;
import EnodeB.EnodeBWithDAN;
import EnodeB.EnodeBWithDonor;

public class ParallelCommandsThread{

	ParallelCommandsThreadEnodeBComponent parallelCommandsThreadEnodeBComponent;

    /**
     * ParallelCommandsThread Constructor, but with an option of an input : responseTimeout between 2 commands
     * response timeout = 10, no extra wait between commands
     *
     * @param enodebCmdSet - enodebCmdSet
     * @param enb          - enb
     * @param donorCmdSet  - donorCmdSet
     * @param danCmdSet    - danCmdSet
     * @throws IOException
     */
	public ParallelCommandsThread(List<String> enodebCmdSet, EnodeB enb, List<String> donorCmdSet, List<String> danCmdSet) throws IOException{
		this(enodebCmdSet,  enb,  donorCmdSet,  danCmdSet,10,0);
	}

    /**
     * ParallelCommandsThread Constructor, but with an option of an input : responseTimeout between 2 commands
     *
     * @param enodebCmdSet    - enodebCmdSet
     * @param enb             - enb
     * @param donorCmdSet     - donorCmdSet
     * @param danCmdSet       - danCmdSet
     * @param responseTimeout - responseTimeout
     * @throws IOException
     */
	public ParallelCommandsThread(List<String> enodebCmdSet, EnodeB enb, List<String> donorCmdSet, List<String> danCmdSet, int responseTimeout, int waitBetweenCommands) throws IOException{
		if(enb.hasDonor()){
			if(donorCmdSet == null){
				donorCmdSet = enodebCmdSet;
			}
			parallelCommandsThreadEnodeBComponent = new ParallelCommandsThreadEnodbWithDonor(enodebCmdSet, (EnodeBWithDonor)enb, donorCmdSet, responseTimeout, waitBetweenCommands);
		}else if(enb.hasDan() && danCmdSet != null){
			parallelCommandsThreadEnodeBComponent = new ParallelCommandsThreadEnodbWithDan(enodebCmdSet, (EnodeBWithDAN)enb, danCmdSet, responseTimeout, waitBetweenCommands);
		}else{
			parallelCommandsThreadEnodeBComponent = new ParallelCommandsThreadEnodeBComponent(enodebCmdSet, enb, enb.getXLPName(), responseTimeout, waitBetweenCommands);
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

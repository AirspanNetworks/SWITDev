package Utils.WatchDog;

import java.util.HashMap;

import EnodeB.EnodeB;
import Utils.GeneralUtils;

public class CommandWatchUeLinkStatusVolte extends Command{
	private EnodeB dut;
	private boolean flagActive;
	private String rntiToCheck;
	
	public CommandWatchUeLinkStatusVolte(EnodeB enb, String rnti) {
		dut = enb;
		flagActive = true;
		rntiToCheck = rnti;
	}

	@Override
	public void run() {
		if(rntiToCheck == null){
			Boolean result = dut.getNumberOfUELinkStatusVolte();
			if(result != null){
				flagActive = result && flagActive;							
			}
		}else{
			HashMap<String, Integer> result = dut.getUELinkStatusVolteTable();
			if(!result.isEmpty()){
				for (String key : result.keySet()) {
					String[] rntis = key.split("\\.");
					String rnti = rntis[rntis.length-1];
					if(rnti.equals(rntiToCheck)){
						if(result.get(key)==0){
							flagActive = false;
						}
					}else{
						if(result.get(key)==1){
							flagActive = false;
						}
					}
			    }
			}
		}
	}

	public boolean getFlagActive(){
		return flagActive;
	}
	
	@Override
	public int getExecutionDelaySec() {
		return 2;
	}
}

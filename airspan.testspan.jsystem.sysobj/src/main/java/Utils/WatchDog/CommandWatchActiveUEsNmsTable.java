package Utils.WatchDog;

import EnodeB.EnodeB;
import Entities.ITrafficGenerator.TransmitDirection;
import Utils.GeneralUtils;

public class CommandWatchActiveUEsNmsTable extends Command{
	
	
	private EnodeB dut;
	private int numUEs;
	private Character qciToCheckInWD;
	private boolean checkActiveMoreThanOne;
	private boolean flagActive;
	
	public CommandWatchActiveUEsNmsTable(EnodeB enb,int numOfUes, Character qciToCheck,boolean oneOrMore) {
		dut = enb;
		flagActive = true;
		numUEs = numOfUes;
		qciToCheckInWD = qciToCheck;
		checkActiveMoreThanOne = oneOrMore;
	}

	@Override
	public void run() {
		System.out.println("*** Checking active in NMS for QCI "+qciToCheckInWD+" ***");
		if(checkActiveMoreThanOne){
			flagActive = isQci1ActiveOneOrMoreViaXlp() && flagActive;			
		}else{
			flagActive = isQci1ActiveOneViaXlp() && flagActive;
		}
	}

	protected boolean isQci1ActiveOneOrMoreViaXlp(){
		int dlResult = getActiveUEsPerQciDLViaXlp(qciToCheckInWD);
		int ulResult = getActiveUEsPerQciULViaXlp(qciToCheckInWD);
		boolean flagDl = (dlResult>=numUEs || dlResult == GeneralUtils.ERROR_VALUE);
		boolean flagUl = (ulResult>=numUEs || ulResult == GeneralUtils.ERROR_VALUE);
		return (flagDl || flagUl);
	}
	
	protected boolean isQci1ActiveOneViaXlp(){
		int dl = getActiveUEsPerQciDLViaXlp(qciToCheckInWD);
		int ul = getActiveUEsPerQciULViaXlp(qciToCheckInWD);
		return (dl == 1 && ul <= 1) || (ul == 1 && dl <= 1);
	}
	
	protected int getActiveUEsPerQciDLViaXlp(Character qci){
		return dut.getActiveUEPerQciAndTransmitDirection(TransmitDirection.DL, qci);
	}
	
	protected int getActiveUEsPerQciULViaXlp(Character qci){
		return dut.getActiveUEPerQciAndTransmitDirection(TransmitDirection.UL, qci);
	}
	
	public boolean getFlagActive(){
		return flagActive;
	}
	
	@Override
	public int getExecutionDelaySec() {
		return 10;
	}
}

package EnodeB;

import java.util.Arrays;

import EnodeB.Components.DAN;
import EnodeB.Components.Log.Logger;
import EnodeB.Components.Session.Session;
import Utils.GeneralUtils;

public abstract class EnodeBWithDAN extends EnodeB {
	/** The dan. */
	protected DAN[] DAN;

	@Override
	public void init() throws Exception {
		super.init();

		if (connectInfo.danInfo != null){
			DAN = new DAN[connectInfo.danInfo.length];
			for(int i = 0; i<DAN.length; i++){
				DAN[i] = new DAN();
				DAN[i].setLogNeeded(!isInitCalledFromAction());
				DAN[i].createSerialCom(connectInfo.danInfo[i].getSerialIP(), Integer.parseInt(connectInfo.danInfo[i].getSerialPort()));
				DAN[i].setName(getName() + "_DAN[" + i + "]");
				DAN[i].init();
			}
			hasDan = true;
		}
	}

	@Override
	public boolean reboot() {
		if (DAN == null) {
			GeneralUtils.printToConsole("No DAN in the current base Station");
		} else {
			for (int i = 0; i < DAN.length; i++) {
				DAN[i].setExpectBooting(true);
			}
		}
		//Return status
		return super.reboot();
	}

	@Override
	public Logger[] getLoggers() {
		Logger[] loggers = super.getLoggers();

		if (DAN != null) {
			for (int i = 0; i < DAN.length; i++) {
				loggers = Arrays.copyOf(loggers, loggers.length + 1);
				loggers[loggers.length - 1] = DAN[i].getLogger();
			}
		}
		return loggers;
	}
	
	public String sendDanCommands(String shellPrompt, String cmd, String command, int danIndex){
		return DAN[danIndex].sendCommands(shellPrompt, cmd, command);
	}
	
	public int getDanArrlength(){
		return DAN.length;
	}
	
	@Override
	public Session getDefaultSession(String componentName){
		if(!componentName.contains("DAN"))
			return XLP.getDefaultSession();
		else{
			for(DAN dan : DAN){ 
				if(dan.getName().equals(componentName))
					return dan.getDefaultSession();
			}
			return DAN[0].getDefaultSession();
		}
			
	}
	
	public String getDanName(int index){
		return DAN[index].getName();
	}
}

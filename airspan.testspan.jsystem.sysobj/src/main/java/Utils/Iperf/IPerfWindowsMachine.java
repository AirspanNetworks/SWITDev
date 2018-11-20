package Utils.Iperf;

import Entities.ITrafficGenerator.TransmitDirection;
import Utils.GeneralUtils;
import Utils.Pair;

public abstract class IPerfWindowsMachine extends IPerfMachine{
	private String lastReadedLine;
	protected abstract String sendCommandAndWaitForRespond(String command);
	
	public IPerfWindowsMachine() {
		super();
		lastReadedLine = "";
	}

	@Override
	public boolean connect() {
		resetParams();
		preAddressTpFile = "C:\\Users\\"+username+"\\";
		return false;
	}

	@Override
	public void disconnect() {
		GeneralUtils.printToConsole("IPerfWindowsMachine.disconnect did NOT Implemented.");
	}
	
	public Pair<Boolean, String> returnPair(Boolean status) {
		return new Pair<Boolean, String>(status, "");
	}

	@Override
	public String startIPerfTraffic(String clientCommand, String tpFileName, TransmitDirection transmitDirection){
		clientCommand = "iperf " + clientCommand;
		//sendCommand(clientCommand);
		return clientCommand;
	}
	
	@Override
	public String startIPerfListener(Integer numberOfParallelIPerfStreams, String serverCommand, String tpFileName, TransmitDirection transmitDirection) {
		lastReadedLine = "";
		serverCommand = "iperf " + serverCommand;
		String windowsServerCommand = serverCommand + " > " + preAddressTpFile + tpFileName;
		if(numberOfParallelIPerfStreams != null && numberOfParallelIPerfStreams > 1){
			windowsServerCommand = serverCommand + " | find \"SUM\" > " + preAddressTpFile + tpFileName;
		}else{
			windowsServerCommand = serverCommand + " > " + preAddressTpFile + tpFileName;
		}
		//return sendCommand(windowsServerCommand).getElement0();
		return windowsServerCommand;
	}

	@Override
	public boolean stopIPerf(){
		return sendCommand("taskkill /f /im iperf.exe").getElement0();
	}

	protected abstract Process sendCommandAndReturtExexcProcess(String string);
	
	@Override
	public String getStrCounters(String tpCountersFileNames) {
		String respond = "";
		String[] tpCountersFileNamesArray = tpCountersFileNames.split(".txt");
		if(tpCountersFileNamesArray != null){
			for(String fileName : tpCountersFileNamesArray){
				int numberOfLinesForSample = 0;
				numberOfLinesForSample = IPerfMachine.getNumberOfLinesForSample();
				String getLastTpLines = "tail -"+numberOfLinesForSample+" " + fileName.trim() +".txt";
				respond += ("==> " + fileName.trim() + ".txt" + "\n");
				respond += sendCommandAndWaitForRespond(getLastTpLines);
			}
		}
		if(lastReadedLine.equals(respond)){
			respond = "";
		}else{
			lastReadedLine = respond;
		}
		GeneralUtils.printToConsole("### " + respond + " ###");
		return respond;
	}
}

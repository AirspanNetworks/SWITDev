package Utils.Iperf;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import Utils.GeneralUtils;
import Utils.Pair;

public class IPerfRemoteWindowsMachine extends IPerfWindowsMachine{

	public IPerfRemoteWindowsMachine() { 
		super();
	}
	
	protected Process sendCommandAndReturtExexcProcess(String command) {
		Process process = null;
		String psexecCommand = "cmd /c (psexec \\\\"+hostname+" -u AIRSPAN\\"+username+" -p "+password+" -d cmd /c \""+command+"\")";
		try {
			GeneralUtils.printToConsole("IPerfRemoteWindowsMachine.exec("+psexecCommand+")");
			process = Runtime.getRuntime().exec(psexecCommand);
		} catch (IOException e) {
			GeneralUtils.printToConsole("FAILED to send psexec command."); 
			e.printStackTrace();
		}
		return process;
	}

	@Override
	public Pair<Boolean, String> sendCommand(String command){
		return returnPair(sendCommandAndReturtExexcProcess(command) != null);
	}

	@Override
	protected String sendCommandAndWaitForRespond(String command) {
		GeneralUtils.printToConsole("IPerfRemoteWindowsMachine.sendCommandAndWaitForRespond NOT implemented!");
		return null;
	}

	@Override
	public File getFile(String fileName) {
		GeneralUtils.printToConsole("NOT Supporting Windows Remote Machine.");
		return new File("");
	}

	@Override
	public boolean putFile(String fileName) {
		return false;
		
	}
	
	@Override
	public ArrayList<File> getFileList(String fileName) {
		
		return new ArrayList<File>();
	}
}

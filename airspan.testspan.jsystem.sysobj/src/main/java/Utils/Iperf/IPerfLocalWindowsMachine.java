package Utils.Iperf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import Utils.GeneralUtils;
import Utils.Pair;
import Utils.ScpClient;

public class IPerfLocalWindowsMachine extends IPerfWindowsMachine{

	public IPerfLocalWindowsMachine() {
		super();
	}
	
	protected Process sendCommandAndReturtExexcProcess(String command) {
		Process process = null;
		String execCommand = "cmd /c \""+command+"\"";
		try {
			GeneralUtils.printToConsole("IPerfLocalWindowsMachine.sendCommandAndReturtExexcProcess.exec("+execCommand+")");
			process = Runtime.getRuntime().exec(execCommand);
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
	
	public String sendCommandAndWaitForRespond(String command){
		String respond = "";
		String execCommand = "cmd /c \""+command+"\"";
		try {
			GeneralUtils.printToConsole("IPerfLocalWindowsMachine.sendCommandAndWaitForRespond.exec("+execCommand+")");
			Process process = Runtime.getRuntime().exec(execCommand);
			process.waitFor();
			BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
			while(br.ready())
				respond += (br.readLine() + "\n");
		} catch (Exception e) {
			GeneralUtils.printToConsole("IPerfLocalWindowsMachine.sendCommandAndWaitForRespond: Cannot get respond.");
			e.printStackTrace();
		}
		GeneralUtils.printToConsole(respond);
		respond = execCommand + respond;
    	return respond;
	}

	@Override
	public File getFile(String fileName) {
		return new File(preAddressTpFile+fileName);
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

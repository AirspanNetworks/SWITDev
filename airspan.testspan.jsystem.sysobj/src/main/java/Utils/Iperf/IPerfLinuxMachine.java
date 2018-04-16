package Utils.Iperf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import Utils.GeneralUtils;
import Utils.Pair;
import Utils.SSHConnector;
import Utils.ScpClient;

public class IPerfLinuxMachine extends IPerfMachine{
	private SSHConnector sshSession;

	public IPerfLinuxMachine() {
		super();
		sshSession = null;
	}

	@Override
	public boolean connect() {
		preAddressTpFile = "/home/" + username + "/";
		resetParams();
		try {
			sshSession = new SSHConnector(hostname, username, password);
			sshSession.initConnection();
		
			GeneralUtils.unSafeSleep(2000);
		} catch (Exception e) {
			GeneralUtils.printToConsole("FAILED to connect to IPerf Machine.");
			e.printStackTrace();
		}
		return sshSession.isConnected();
	}

	public Pair<Boolean,String> sendCommand(String command, int timeoutInMili){
		Boolean status = false;
		String respond = "";
		try {
			respond = sshSession.sendCommand(command, timeoutInMili);
			status = true;
		} catch (Exception e) {
			GeneralUtils.printToConsole("FAILED to sent command to IPerf Machine.");
			e.printStackTrace();
		}
		Pair<Boolean,String> res = new Pair<Boolean, String>(status, respond);
		return res;
	}
	
	@Override
	public Pair<Boolean,String> sendCommand(String command) {
		return sendCommand(command, 2000);
	}

	@Override
	public void disconnect() {
		sshSession.disconnect();
	}

	@Override
	public boolean startIPerfTraffic(String clientCommand, String clientOutputFileName){
		String linuxClientCommand = "nohup iperf " + clientCommand + " &> "+ preAddressTpFile + clientOutputFileName +" &";
		return sendCommand(linuxClientCommand).getElement0();
	}

	@Override
	public boolean startIPerfListener(Integer numberOfParallelIPerfStreams, String serverCommand, String tpFileName){
		String linuxServerCommand = "";
		if(numberOfParallelIPerfStreams != null && numberOfParallelIPerfStreams > 1){
			linuxServerCommand = "nohup iperf " + serverCommand + " | grep SUM --line-buffered &> " + preAddressTpFile + tpFileName + " &";
		}else{
			linuxServerCommand = "nohup iperf " + serverCommand + " &> " + preAddressTpFile + tpFileName + " &";
		}
		return sendCommand(linuxServerCommand).getElement0();
	}
	
	@Override
	public boolean stopIPerf() {
		sendCommand("pkill -9 iperf").getElement0();
		return sendCommand("pkill -9 iperf").getElement0();
	}

	@Override
	public String getStrCounters(String tpCountersFileNames) {
		int numberOfLinesForSample = 0;
		numberOfLinesForSample = IPerfMachine.getNumberOfLinesForSample();
		String getLastTpLines = "tail -n-"+numberOfLinesForSample+" " + tpCountersFileNames;
		Pair<Boolean, String> res = sendCommand(getLastTpLines, 150);
		return res.getElement1();
	}

	@Override
	public File getFile(String fileName) {
		ScpClient scpClient = new ScpClient(hostname, username, password);
		if(scpClient.getFiles(System.getProperty("user.dir"), preAddressTpFile + fileName) == false){
			String fileContent = sendCommand("cat " + preAddressTpFile + fileName, 10000).getElement1();
			try {
				GeneralUtils.printToConsole("FAILED To Copy Files via SCP, fall back to getting file content via cat command.");
				PrintWriter writer = new PrintWriter(fileName, "UTF-8");
				writer.println(fileContent);
				writer.close();
			} catch (FileNotFoundException | UnsupportedEncodingException e) {
				GeneralUtils.printToConsole("FAILED To Get File From Linux IPerf Machine.");
				fileName = "";
				e.printStackTrace();
			}
		}
		File file = new File(fileName); 
		return file;
	}
}

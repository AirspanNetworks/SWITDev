package Utils.Iperf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import Entities.ITrafficGenerator.TransmitDirection;
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
			if((sshSession == null) || (!sshSession.isConnected())){
				sshSession = new SSHConnector(hostname, username, password);
				sshSession.initConnection();
				
			}else{
				GeneralUtils.printToConsole("IPerf Machine is already connected.");
			}
		
			GeneralUtils.unSafeSleep(2000);
		} catch (Exception e) {
			GeneralUtils.printToConsole("FAILED to connect to IPerf Machine.");
			e.printStackTrace();
		}
		boolean connected = sshSession.isConnected();
		if(connected){
			sendCommand("rm "+preAddressTpFile+"*clientOutputDL*");
		    sendCommand("rm "+preAddressTpFile+"*tpUL*");
			sendCommand("rm "+preAddressTpFile+"*DLclientSide.txt");
			sendCommand("rm "+preAddressTpFile+"*ULserverSide.txt");
			sendCommand("rm "+preAddressTpFile+"nohup*");
		
			sendCommand("rm "+preAddressTpFile+"*clientOutputUL*");
			sendCommand("rm "+preAddressTpFile+"*tpDL*");
			sendCommand("rm "+preAddressTpFile+"*ULclientSide.txt");
			sendCommand("rm "+preAddressTpFile+"*DLserverSide.txt");
		}
		return connected;
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
		return sendCommand(command, 200);
	}

	@Override
	public void disconnect() {
		sshSession.disconnect();
	}

	@Override
	public String startIPerfTraffic(String clientCommand, String clientOutputFileName, TransmitDirection transmitDirection){
		String linuxClientCommand = "echo 'nohup iperf " + clientCommand + " &> "+ preAddressTpFile + clientOutputFileName +" &' >> " + preAddressTpFile + transmitDirection + IPerf.clientSideCommandsFile;
		return linuxClientCommand;
	}

	@Override
	public String startIPerfListener(Integer numberOfParallelIPerfStreams, String serverCommand, String tpFileName, TransmitDirection transmitDirection){
		String linuxServerCommand = "";
		if(numberOfParallelIPerfStreams != null && numberOfParallelIPerfStreams > 1){
			linuxServerCommand = "echo 'nohup iperf " + serverCommand + " | grep SUM --line-buffered &> " + preAddressTpFile + tpFileName + " &' >> " + preAddressTpFile + transmitDirection + IPerf.serverSideCommandsFile;
		}else{
			linuxServerCommand = "echo 'nohup iperf " + serverCommand + " &> " + preAddressTpFile + tpFileName + " &' >> " + preAddressTpFile + transmitDirection + IPerf.serverSideCommandsFile;
		}
		return linuxServerCommand;
	}
	
	@Override
	public boolean stopIPerf() {
		sendCommand("pkill -15 iperf").getElement0();
		return sendCommand("pkill -15 iperf").getElement0();
	}

	@Override
	public String getStrCounters(String tpCountersFileNames) {
		int numberOfLinesForSample = 0;
		numberOfLinesForSample = IPerfMachine.getNumberOfLinesForSample();
		String getLastTpLines = "tail -n-"+numberOfLinesForSample+" " + tpCountersFileNames;
		Pair<Boolean, String> res = sendCommand(getLastTpLines, 1000);
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
		scpClient.close();
		File file = new File(fileName); 
		return file;
	}

	@Override
	public boolean putFile(String fileName) {
		ScpClient scpClient = new ScpClient(hostname, username, password);
		try {
			scpClient.putFiles(preAddressTpFile, System.getProperty("user.dir")+fileName);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		if(scpClient.getFiles(System.getProperty("user.dir"), preAddressTpFile + fileName)){
			return true;
		}
		scpClient.close();
		return false;
	}
	
	
}

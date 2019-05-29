package Utils.Iperf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

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
			sendCommand("rm -f "+preAddressTpFile+"*clientOutputDL*");
		    sendCommand("rm -f "+preAddressTpFile+"*tpUL*");
			sendCommand("rm -f "+preAddressTpFile+"*DLclientSide.txt");
			sendCommand("rm -f "+preAddressTpFile+"*ULserverSide.txt");
			sendCommand("rm -f "+preAddressTpFile+"nohup*");
		
			sendCommand("rm -f "+preAddressTpFile+"*clientOutputUL*");
			sendCommand("rm -f "+preAddressTpFile+"*tpDL*");
			sendCommand("rm -f "+preAddressTpFile+"*ULclientSide.txt");
			sendCommand("rm -f "+preAddressTpFile+"*DLserverSide.txt");
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
		return new Pair<Boolean, String>(status, respond);
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
		//Return linux Client Command
		return "nohup iperf " + clientCommand + " &> "+ preAddressTpFile + clientOutputFileName +" &";
	}

	@Override
	public String startIPerfListener(Integer numberOfParallelIPerfStreams, String serverCommand, String tpFileName, TransmitDirection transmitDirection){
		String linuxServerCommand = "";
		if(numberOfParallelIPerfStreams != null && numberOfParallelIPerfStreams > 1){
			linuxServerCommand = "nohup iperf " + serverCommand + " | grep SUM --line-buffered &> " + preAddressTpFile + tpFileName + " &";
		}else{
			linuxServerCommand = "nohup iperf " + serverCommand + " &> " + preAddressTpFile + tpFileName + " &";
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
		return new File(fileName);
	}
	
	@Override
	public ArrayList<File> getFileList(String fileName) {
		ArrayList<File> fileToRet = new ArrayList<File>();
		String[] allNames = fileName.split(" ");
		/*String namesToGet = "";
		for(String str : allNames){
			namesToGet += "\""+preAddressTpFile + str +"\",";
		}
		namesToGet = namesToGet.substring(0, namesToGet.length()-1);*/
		ScpClient scpClient = new ScpClient(hostname, username, password);
		if(scpClient.getFiles(System.getProperty("user.dir"), allNames)){
			for(String str : allNames){
				File ret = new File(str);
				GeneralUtils.printToConsole("File created: "+str);
				fileToRet.add(ret);
			}
		}
		scpClient.close();
		return fileToRet;
	}

	@Override
	public boolean putFile(String fileName) {
		ScpClient scpClient = new ScpClient(hostname, username, password);
		boolean toRet = false;
		try {
			scpClient.putFiles(preAddressTpFile, fileName);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		if(scpClient.getFiles(System.getProperty("user.dir"), preAddressTpFile + fileName)){
			toRet = true;
		}
		scpClient.close();
		return toRet;
	}
}

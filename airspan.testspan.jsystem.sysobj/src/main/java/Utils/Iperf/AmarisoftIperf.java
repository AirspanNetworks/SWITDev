package Utils.Iperf;

import java.io.IOException;
import java.util.ArrayList;

import Entities.ITrafficGenerator.Protocol;
import Entities.ITrafficGenerator.TransmitDirection;
import UE.AmarisoftUE;
import Utils.GeneralUtils;

public class AmarisoftIperf extends UEIPerf{

	public AmarisoftIperf(AmarisoftUE ue, IPerfMachine iperfMachineDL, IPerfMachine iperfMachineUL, double ulLoad, double dlLoad,
			Integer frameSize, ArrayList<Character> qciList, Protocol protocol,TransmitDirection direction,
			Integer runTime) throws IOException, InterruptedException {
		super(ue, iperfMachineDL, iperfMachineUL, ulLoad, dlLoad, frameSize, qciList,protocol,direction,runTime);
		ulStreamArrayList = new ArrayList<>();
		dlStreamArrayList = new ArrayList<>();
		for(Character qciChar : qciList){
			int qciInt = Integer.valueOf(qciChar)-Integer.valueOf('0');
			//boolean state = qciInt == 9? true : false;
			String ueNumber = GeneralUtils.removeNonDigitsFromString(this.ue.getName());
			try {
				if(direction == TransmitDirection.BOTH || direction == TransmitDirection.UL){
					ulStreamArrayList.add(new IPerfStream(TransmitDirection.UL, ueNumber, qciInt, this.ue.getIPerfDlMachine(), this.ue.getIPerfDlMachine(), true, ulLoad/qciList.size(), frameSize,protocol,runTime));
				}
				if(direction == TransmitDirection.BOTH || direction == TransmitDirection.DL){
					dlStreamArrayList.add(new AmarisoftIPerfStream(TransmitDirection.DL, ueNumber, qciInt, this.ue.getWanIpAddress(),  this.ue.getWanIpAddress(), true, dlLoad/qciList.size(), frameSize,protocol,runTime));
				}
			} catch (Exception e) {
				GeneralUtils.printToConsole(e.getMessage());
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void runTrafficULClient(long startTime) {
		if(iperfMachineUL != null){
			for(IPerfStream ulIPerfStream : ulStreamArrayList){
				if(ulIPerfStream.isActive() && !ulIPerfStream.isRunningTraffic()){
					String linuxClientCommand = "echo 'sudo ip netns exec ue"+((AmarisoftUE)ue).ueId+" nohup iperf " + ulIPerfStream.getIperfClientCommand() + " &> "+ iperfMachineUL.getPreAddressTpFile() + ulIPerfStream.getClientOutputFileName() +" &' >> " + iperfMachineUL.getPreAddressTpFile() + "UL" + IPerf.clientSideCommandsFile;
					iperfMachineUL.sendCommand(linuxClientCommand).getElement0();
					ulIPerfStream.setRunningTraffic(true);
					ulIPerfStream.setTimeStart(startTime);
				}
			}
		}else{
			GeneralUtils.printToConsole("UL IPerf Machine equals NULL.");
		}
	}

	@Override
	protected void startDLListener() {
		if(iperfMachineUL != null){
			for(IPerfStream dlIPerfStream : dlStreamArrayList){
				if(dlIPerfStream.isActive() && !dlIPerfStream.isRunningTraffic()){
					String linuxServerCommand = "";
					if(dlIPerfStream.getNumberOfParallelIPerfStreams() != null && dlIPerfStream.getNumberOfParallelIPerfStreams() > 1){
						linuxServerCommand = "echo 'sudo ip netns exec ue"+((AmarisoftUE)ue).ueId+" nohup iperf " + dlIPerfStream.getIperfServerCommand() + " | grep SUM --line-buffered &> " +  iperfMachineUL.getPreAddressTpFile() + dlIPerfStream.getTpFileName() + " &' >> " + iperfMachineUL.getPreAddressTpFile() + "DL" + IPerf.serverSideCommandsFile;
					}else{
						linuxServerCommand = "echo 'sudo ip netns exec ue"+((AmarisoftUE)ue).ueId+" nohup iperf " + dlIPerfStream.getIperfServerCommand() + " &> " +  iperfMachineUL.getPreAddressTpFile() + dlIPerfStream.getTpFileName() + " &' >> " + iperfMachineUL.getPreAddressTpFile() + "DL" + IPerf.serverSideCommandsFile;
					}
					iperfMachineUL.sendCommand(linuxServerCommand).getElement0();
				}
			}
		}else{
			GeneralUtils.printToConsole("UL IPerf Machine equals NULL.");
		}
	}
	
	@Override
	public String getKillCommand(){
		return "sudo kill -9 ";
	}
}

package Utils.PcapUtils;

import Utils.GeneralUtils;
import io.pkts.PacketHandler;
import io.pkts.Pcap;

public class PcapParser {

	Pcap pcapFile = null;
	
	public boolean loadPcapFile(String path){
		boolean result = false;
		try{
			pcapFile = Pcap.openStream(path);
			result = true;
		}catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
	
	public boolean parse(PacketHandler packetHandl){
		if(pcapFile == null){
			GeneralUtils.printToConsole("There is no allocation for File!");
			return false;
		}
		
		try{
			pcapFile.loop(packetHandl);
			return true;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}
	
}

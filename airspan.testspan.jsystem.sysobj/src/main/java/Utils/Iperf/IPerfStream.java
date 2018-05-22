package Utils.Iperf;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

import Entities.ITrafficGenerator.TransmitDirection;
import Utils.GeneralUtils;
import Utils.Pair;
import Entities.ITrafficGenerator.Protocol;

/**
 * @author ayefet
 *
 */
public class IPerfStream {
	private Double windowSizeInKbits = null; //The eNodeB suppose to set the window size to 2Mb.
	private Integer numberOfParallelIPerfStreams = null;
	private Integer frameSize = null;
	
	private final String streamName;
	private final TransmitDirection transmitDirection;
	private final String ueNumber;
	private final int qci;
	private final String destIpAddress;
	private final String tpFileName;
	private final String clientOutputFileName;
	private final String srcIpAddress;
	
	private boolean isActive;
	private double streamLoad;
	private String iperfClientCommand;
	private String iperfServerCommand;
	private ArrayList<Long> countersInBits;
	private Protocol protocol;
	private Double lastIntervalUsedForLastSample;
	private boolean isRunningTraffic; 

	public IPerfStream(TransmitDirection transmitDirection, String ueNumber, int qci, String destIpAddress, String srcIpAddress, boolean state, double streamLoad, Integer frameSize) throws Exception {
		if(transmitDirection == TransmitDirection.BOTH){
			throw new Exception("Stream Can't be to BOTH directions (UL & DL).");
		}
		this.streamName = (transmitDirection.value + "_UE" + ueNumber) + qci;
		this.transmitDirection = transmitDirection;
		this.ueNumber = ueNumber;
		this.qci = qci;
		this.protocol = Protocol.UDP;
		this.tpFileName = "tp" + transmitDirection.value+"_UE" + ueNumber + "QCI" + qci + ".txt"; //UL & DL files are named the same in purpose - for updating the counter in the opposite stream
		this.clientOutputFileName = "clientOutput" + transmitDirection.value+"_UE" + ueNumber + "QCI" + qci + ".txt";
		this.destIpAddress = destIpAddress;
		this.srcIpAddress = srcIpAddress;
		this.numberOfParallelIPerfStreams = 1;
		this.lastIntervalUsedForLastSample = 0.0;
		
		this.isActive = state;
		this.streamLoad = streamLoad;
		this.frameSize = frameSize;
		this.countersInBits = new ArrayList<Long>();
		this.isRunningTraffic = false;
		generateIPerfCommands();
	}
	
	void generateIPerfCommands(){
		if(!isRunningTraffic()){
			if(this.protocol == Protocol.UDP){
				this.iperfClientCommand = "-c " + this.destIpAddress + " -u -i 1 -p " + (5000+this.qci) + " -l " + this.frameSize + ".0B -b " + convertTo3DigitsAfterPoint(this.streamLoad) + "M -t " + UEIPerf.IPERF_TIME_LIMIT;
				this.iperfServerCommand = "-s -u -i 1 -p " + (5000+this.qci) + " -B " + this.srcIpAddress + " -l " + this.frameSize + ".0B -f k";
			}else if(this.protocol == Protocol.TCP){
				this.iperfClientCommand = "-c " + this.destIpAddress + " ";
				this.iperfServerCommand = "-s ";
				if(this.numberOfParallelIPerfStreams != null){
					this.iperfClientCommand += "-P "+numberOfParallelIPerfStreams;
					this.iperfServerCommand += "-P "+numberOfParallelIPerfStreams;
				}
				this.iperfClientCommand += " -i 1 -p " + (5000+this.qci);
				this.iperfServerCommand += " -i 1 -p " + (5000+this.qci);
				if(this.windowSizeInKbits != null){
					this.iperfClientCommand += " -w "+this.windowSizeInKbits+"k";
					this.iperfServerCommand += " -w "+this.windowSizeInKbits+"k";
				}
				this.iperfServerCommand += " -B " + this.srcIpAddress;
				if(this.frameSize != null){
					this.iperfClientCommand += " -M "+this.frameSize;
					this.iperfServerCommand += " -M "+this.frameSize;
				}
				this.iperfClientCommand += " -t " + UEIPerf.IPERF_TIME_LIMIT;
				this.iperfServerCommand += " -f k";
			}else{
				GeneralUtils.printToConsole("Protocol NOT UDP and NOT TCP - FAILURE");
			}
		}
	}
	
	private String convertTo3DigitsAfterPoint(double val){
		NumberFormat formatter = new DecimalFormat("#.###");
		return formatter.format(val);
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public String getStreamName() {
		return streamName;
	}

	public TransmitDirection getTransmitDirection() {
		return transmitDirection;
	}

	public String getUeNumber() {
		return ueNumber;
	}

	public int getQci() {
		return qci;
	}

	public double getStreamLoad() {
		return streamLoad;
	}

	public void setStreamLoad(double streamLoad) {
		if(!isRunningTraffic()){
			this.streamLoad = streamLoad;
			generateIPerfCommands();
		}
	}

	public Integer getFrameSize() {
		return frameSize;
	}

	public void setFrameSize(Integer frameSize) {
		if(!isRunningTraffic()){
			this.frameSize = frameSize;
			generateIPerfCommands();			
		}
	}

	public void setWindowSizeInKbits(Double windowSizeInKbits) {
		if(!isRunningTraffic()){
			this.windowSizeInKbits = windowSizeInKbits;
			generateIPerfCommands();			
		}
	}

	public void setNumberOfParallelIPerfStreams(Integer numberOfParallelIPerfStreams) {
		if(!isRunningTraffic()){
			this.numberOfParallelIPerfStreams = numberOfParallelIPerfStreams;
			generateIPerfCommands();			
		}
	}
	
	public Protocol getProtocol() {
		return protocol;
	}

	public void setProtocol(Protocol protocol) {
		if(!isRunningTraffic()){
			this.protocol = protocol;
			generateIPerfCommands();			
		}
	}

	public String getIpAddress() {
		return destIpAddress;
	}

	public String getIperfClientCommand() {
		return iperfClientCommand;
	}

	public String getDestIpAddress() {
		return destIpAddress;
	}

	public String getSrcIpAddress() {
		return srcIpAddress;
	}

	public String getIperfServerCommand() {
		this.lastIntervalUsedForLastSample = 0.0;
		return iperfServerCommand;
	}

	public String getTpFileName() {
		return tpFileName;
	}

	public String getClientOutputFileName() {
		return clientOutputFileName;
	}

	public ArrayList<Long> getCountersInBits() {
		return countersInBits;
	}

	public void setCountersInBits(Pair<Double, ArrayList<Long>> sample) {
		this.countersInBits = new ArrayList<Long>();
		this.countersInBits.addAll(sample.getElement1());
		if(sample.getElement0() != 0.0){
			this.lastIntervalUsedForLastSample = sample.getElement0();
		}
	}

	public Integer getNumberOfParallelIPerfStreams() {
		return numberOfParallelIPerfStreams;
	}

	public Double getLastIntervalUsedForLastSample() {
		return lastIntervalUsedForLastSample;
	}
	
	public boolean isRunningTraffic() {
		return isRunningTraffic;
	}
	
	public void setRunningTraffic(boolean isRunningTraffic) {
		this.isRunningTraffic = isRunningTraffic;
	}
}

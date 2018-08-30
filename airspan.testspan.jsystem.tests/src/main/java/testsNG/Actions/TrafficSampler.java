package testsNG.Actions;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

import Action.TrafficAction.TrafficAction.ExpectedType;
import EnodeB.EnodeB;
import Entities.StreamParams;
import Entities.ITrafficGenerator.CounterUnit;
import Entities.ITrafficGenerator.TransmitDirection;
import Utils.GeneralUtils;
import Utils.StreamList;
import jsystem.framework.report.ListenerstManager;
import jsystem.framework.report.Reporter;

public class TrafficSampler implements Runnable{
	
	private String name;
	private Traffic trafficInstance;
	private ArrayList<String> ueList;
	private ArrayList<Character> qci;
	private ExpectedType expectedLoadType;
	private String ULExpected;
	private String DLExpected;
	private TransmitDirection direction;
	private EnodeB dut;
	private Integer timeout;
	private volatile boolean keepRunning = true;
	private ArrayList<StreamParams> streams = new ArrayList<StreamParams>();
	protected ArrayList<CounterUnit> counters = new ArrayList<CounterUnit>();
	private Thread runnableThread;
	private ArrayList<String> streamList;
	public static Reporter report = ListenerstManager.getInstance();
	
	public void start(){
		if(ExpectedType.None != expectedLoadType){
			runnableThread = new Thread(this);
			runnableThread.start();			
		}
	}
	
	public void getStatistics(){
		report.report("Statistics for traffic "+getName());
		ArrayList<StreamParams> temp = trafficInstance.getAllStreamsResults(streamList);
		printPerStreamTables(temp);		
	}
	
	public void stopTraffic(){
		report.report("Stopping traffic "+getName());
		keepRunning = false;
		trafficInstance.stopTraffic(streamList);
	}
	
	protected void printPerStreamTables(ArrayList<StreamParams> listOfStreamList) {
		StreamList TablePrinter = new StreamList();
		ArrayList<String> headLines = new ArrayList<String>();
		headLines.add("Rate [Mbit/s]");
		GeneralUtils.startLevel("Per Stream Tables");
		for (StreamParams stream : listOfStreamList) {
				ArrayList<String> valuesList = new ArrayList<String>();
				valuesList.add(longToString3DigitFormat(stream.getRxRate()));
				String dateFormat = GeneralUtils.timeFormat(stream.getTimeStamp());
				TablePrinter.addValues(stream.getName(), dateFormat, headLines, valuesList);
		}
		for (String stream : streamList) {
			report.reportHtml(stream, TablePrinter.printTablesHtmlForStream(stream), true);
		}
		GeneralUtils.stopLevel();
	}
	
	protected String longToString3DigitFormat(long number) {
		double temp;
		NumberFormat formatter = new DecimalFormat("#.###");
		temp = number / 1000000.0;
		return formatter.format(temp);
	}
	
	@Override
	public void run() {
		long start = System.currentTimeMillis();
		counters.add(CounterUnit.BITS_PER_SECOND);

		if(null == timeout){
			timeout = 30*60*1000;
		}else{
			timeout*=1000;
		}
		System.out.println("Timeout value: "+timeout);
		while(keepRunning && System.currentTimeMillis() - start < timeout){
			ArrayList<StreamParams> sampleArrayList = null;
			if(TransmitDirection.BOTH == direction){
				sampleArrayList = trafficInstance.getTxAndRxCounter(counters);				
			}else{
				sampleArrayList = trafficInstance.getTxAndRxCounterDLOrUL(counters, direction);				
			}
			streams.addAll(sampleArrayList);
			for(StreamParams sample : sampleArrayList){
				GeneralUtils.printToConsole("StreamName="+sample.getName()+" / txStreamCounter=" + sample.getTxRate() +
											" / rxStreamCounter=" + sample.getRxRate());
			}
		}
	}		
	
	public ArrayList<StreamParams> getStreams() {
		return streams;
	}

	public Integer getTimeout() {
		return timeout;
	}

	public void setTimeout(Integer timeout) {
		this.timeout = timeout;
	}

	public boolean isKeepRunning() {
		return keepRunning;
	}

	public void setKeepRunning(boolean keepRunning) {
		this.keepRunning = keepRunning;
	}

	public TrafficSampler(Traffic traffic, String name,  ArrayList<String> ueList, ArrayList<Character> qci, TransmitDirection direction, ExpectedType expectedLoadType,
			String uLExpected, String dLExpected, EnodeB dut, Integer timeout, ArrayList<String> streamList) {
		super();
		this.name = name;
		this.ueList = ueList;
		this.qci = qci;
		this.direction = direction;
		this.expectedLoadType = expectedLoadType;
		ULExpected = uLExpected;
		DLExpected = dLExpected;
		this.dut = dut;
		this.trafficInstance = traffic;
		this.timeout = timeout;
		this.streamList = streamList;
	}

	public boolean checkIfStreamsExist(ArrayList<String> streamsToCheck){
		for(String str:streamsToCheck){
			if(streamList.contains(str)){
				return true;
			}
		}
		return false;
	}
	
	public TransmitDirection getDirection() {
		return direction;
	}

	public void setDirection(TransmitDirection direction) {
		this.direction = direction;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<String> getUeList() {
		return ueList;
	}

	public void setUeList(ArrayList<String> ueList) {
		this.ueList = ueList;
	}

	public ArrayList<Character> getQci() {
		return qci;
	}

	public void setQci(ArrayList<Character> qci) {
		this.qci = qci;
	}

	public ExpectedType getExpectedLoadType() {
		return expectedLoadType;
	}

	public void setExpectedLoadType(ExpectedType expectedLoadType) {
		this.expectedLoadType = expectedLoadType;
	}

	public String getULExpected() {
		return ULExpected;
	}

	public void setULExpected(String uLExpected) {
		ULExpected = uLExpected;
	}

	public String getDLExpected() {
		return DLExpected;
	}

	public void setDLExpected(String dLExpected) {
		DLExpected = dLExpected;
	}
}

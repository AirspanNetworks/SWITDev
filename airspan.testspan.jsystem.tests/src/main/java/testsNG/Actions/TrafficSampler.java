package testsNG.Actions;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

import Action.TrafficAction.TrafficAction.ExpectedType;
import EnodeB.EnodeB;
import Entities.StreamParams;
import Entities.ITrafficGenerator.CounterUnit;
import Entities.ITrafficGenerator.TransmitDirection;
import Utils.GeneralUtils;
import Utils.StreamList;
import jsystem.framework.report.ListenerstManager;
import jsystem.framework.report.Reporter;
import testsNG.PerformanceAndQos.Throughput.TPTBase;

public class TrafficSampler implements Runnable{
	
	private String name;
	private Traffic trafficInstance;
	private ArrayList<String> ueList;
	private ArrayList<Character> qci;
	private ExpectedType expectedLoadType;
	private Double ULExpected;
	private Double DLExpected;
	private TransmitDirection direction;
	private EnodeB dut;
	private Integer timeout;
	private volatile boolean keepRunning = true;
	private ArrayList<StreamParams> streams = new ArrayList<StreamParams>();
	protected ArrayList<CounterUnit> counters = new ArrayList<CounterUnit>();
	private Thread runnableThread;
	private ArrayList<String> streamList;
	public static Reporter report = ListenerstManager.getInstance();
	private Double ulLoad = null;
	private Double dlLoad = null;
	private String reason;
	
	public synchronized String getReason() {
		return reason;
	}

	public void start(){
		//runnableThread = new Thread(this);
		//runnableThread.start();			
	}
	
	public void getStatistics(){
		report.report("Statistics for traffic "+getName());
		ArrayList<ArrayList<StreamParams>> temp = trafficInstance.getAllStreamsResults(streamList);
		printPerStreamTables(temp);
		ArrayList<Long> dl_ul = trafficInstance.getMeanByFile(streamList);
		compareWithCalculator(dl_ul);
		trafficInstance.getResultFilesByList(streamList);
	}
	
	private void compareWithCalculator(ArrayList<Long> dl_ul){
		reason = StringUtils.EMPTY;
		double ul_Divided_With_Number_Of_Streams = 0;
		if(ULExpected != null){
			ul_Divided_With_Number_Of_Streams = dl_ul.get(0) / 1000000.0;
			String expectedUlToReport = convertTo3DigitsAfterPoint(ULExpected);
			String actualUlToReport = convertTo3DigitsAfterPoint(ul_Divided_With_Number_Of_Streams);
			report.report("Expected UL: "+expectedUlToReport+" Mbps");
			report.report("Actual average UL tpt: "+actualUlToReport+" Mbps");
			if(ul_Divided_With_Number_Of_Streams < ULExpected){
				report.report("UL actual is lower than expected", Reporter.FAIL);
				reason = name+":<br> ";
				reason += "Expected UL: "+expectedUlToReport+" Mbps. Actual UL: "+actualUlToReport+" Mbps.<br> ";
			}else{
				report.step("UL actual is above expected");
			}				
		}
		double dl_Divided_With_Number_Of_Streams = 0;
		if(DLExpected != null){
			dl_Divided_With_Number_Of_Streams = dl_ul.get(1) / 1000000.0;			
			String expectedDlToReport = convertTo3DigitsAfterPoint(DLExpected);
			String actualDlToReport = convertTo3DigitsAfterPoint(dl_Divided_With_Number_Of_Streams);
	
			report.report("Expected DL: "+expectedDlToReport+" Mbps");
			report.report("Actual average DL tpt: "+actualDlToReport+" Mbps");
			if(dl_Divided_With_Number_Of_Streams < DLExpected){
				report.report("DL actual is lower than expected", Reporter.FAIL);
				if(reason.isEmpty()){
					reason = name+":<br> ";
				}
				reason += "Expected DL: "+expectedDlToReport+" Mbps. Actual DL: "+actualDlToReport+" Mbps.<br> ";
			}else{
				report.step("DL actual is above expected");
			}			
		}
		TPTBase.createHTMLTableWithResults(ul_Divided_With_Number_Of_Streams, ULExpected, dl_Divided_With_Number_Of_Streams,
				DLExpected, dlLoad, ulLoad, null);
		
		
		/*ArrayList<Long> listUlAndDl = new ArrayList<Long>();
		Long ULrxTotal = new Long(0);
		Long DlrxTotal = new Long(0);
		listUlAndDl = getUlDlResultsFromList(ULrxTotal, DlrxTotal, listOfStreamList2);
		compareResults(listUlAndDl.get(0), listUlAndDl.get(1), listOfStreamList2);*/
	}
	
	private void compareResults(Long uLrxTotal, Long dlrxTotal, ArrayList<ArrayList<StreamParams>> listOfStreamList) {
		reason = StringUtils.EMPTY;
		
		int sizeDlStreams = 0;
		int sizeUlStreams = 0;
		
		for(ArrayList<StreamParams> streams:listOfStreamList){
			boolean promoteDl = true;
			boolean promoteUl = true;
			for(StreamParams stream:streams){
				if(stream.getName().contains("DL") && promoteDl){
					sizeDlStreams++;
					promoteDl = false;
				}
				if(stream.getName().contains("UL") && promoteUl){
					sizeUlStreams++;
					promoteUl = false;
				}
			}
		}
		
		double ul_Divided_With_Number_Of_Streams = 0;
		if(ULExpected != null){
			if(sizeUlStreams != 0){
				ul_Divided_With_Number_Of_Streams = uLrxTotal / 1000000.0 / sizeUlStreams;
				String expectedUlToReport = convertTo3DigitsAfterPoint(ULExpected);
				String actualUlToReport = convertTo3DigitsAfterPoint(ul_Divided_With_Number_Of_Streams);
				report.report("Expected UL: "+expectedUlToReport+" Mbps");
				report.report("Actual average UL tpt: "+actualUlToReport+" Mbps");
				if(ul_Divided_With_Number_Of_Streams < ULExpected){
					report.report("UL actual is lower than expected", Reporter.FAIL);
					reason = name+":<br> ";
					reason += "Expected UL: "+expectedUlToReport+" Mbps. Actual UL: "+actualUlToReport+" Mbps.<br> ";
				}else{
					report.step("UL actual is above expected");
				}				
			}else{
				report.report("No results available for UL traffic", Reporter.FAIL);
				reason = name+":<br> ";
				reason += "No results available for UL traffic.<br> ";
			}
		}
		double dl_Divided_With_Number_Of_Streams = 0;
		if(DLExpected != null){
			if(sizeDlStreams != 0){
				dl_Divided_With_Number_Of_Streams = dlrxTotal / 1000000.0 / sizeDlStreams;			
				String expectedDlToReport = convertTo3DigitsAfterPoint(DLExpected);
				String actualDlToReport = convertTo3DigitsAfterPoint(dl_Divided_With_Number_Of_Streams);

				report.report("Expected DL: "+expectedDlToReport+" Mbps");
				report.report("Actual average DL tpt: "+actualDlToReport+" Mbps");
				if(dl_Divided_With_Number_Of_Streams < DLExpected){
					report.report("DL actual is lower than expected", Reporter.FAIL);
					if(reason.isEmpty()){
						reason = name+":<br> ";
					}
					reason += "Expected DL: "+expectedDlToReport+" Mbps. Actual DL: "+actualDlToReport+" Mbps.<br> ";
				}else{
					report.step("DL actual is above expected");
				}			
			}else{
				report.report("No results available for DL traffic", Reporter.FAIL);
				if(reason.isEmpty()){
					reason = name+":<br> ";
				}
				reason += "No results available for DL traffic.<br> ";
			}
		}
		TPTBase.createHTMLTableWithResults(ul_Divided_With_Number_Of_Streams, ULExpected, dl_Divided_With_Number_Of_Streams,
				DLExpected, dlLoad, ulLoad, null);
	}
	
	private ArrayList<Long> getUlDlResultsFromList(Long uLrxTotal, Long dlrxTotal,
			ArrayList<ArrayList<StreamParams>> listOfStreamList2) {
		ArrayList<Long> listOfTotalRx = new ArrayList<Long>();
		
		for(ArrayList<StreamParams> list : listOfStreamList2){
			for (StreamParams stream : list) {
				if (stream.getName().contains("UL")) {
					uLrxTotal += stream.getRxRate();
				}
				if (stream.getName().contains("DL")) {
					dlrxTotal += stream.getRxRate();
				}
			}
		}
		listOfTotalRx.add(uLrxTotal);
		listOfTotalRx.add(dlrxTotal);
		return listOfTotalRx;
	}
	
	public void stopTraffic(){
		report.report("Stopping traffic "+getName());
		keepRunning = false;
		trafficInstance.stopTraffic(streamList);
	}
	
	public void removeStreams(){
		trafficInstance.removeStreams(streamList);
	}
	
	protected void printPerStreamTables(ArrayList<ArrayList<StreamParams>> listOfStreamList) {
		StreamList TablePrinter = new StreamList();
		ArrayList<String> headLines = new ArrayList<String>();
		headLines.add("Rate [Mbit/s]");
		GeneralUtils.startLevel("Per Stream Tables");
		for(ArrayList<StreamParams> list : listOfStreamList){
			for (StreamParams stream : list) {
				ArrayList<String> valuesList = new ArrayList<String>();
				valuesList.add(longToString3DigitFormat(stream.getRxRate()));
				String dateFormat = GeneralUtils.timeFormat(stream.getTimeStamp());
				TablePrinter.addValues(stream.getName(), dateFormat, headLines, valuesList);
			}			
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
	
	private String convertTo3DigitsAfterPoint(double val){
		NumberFormat formatter = new DecimalFormat("#.###");
		return formatter.format(val);
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
			Double uLExpected, Double dLExpected, EnodeB dut, Integer timeout, ArrayList<String> streamList, Double ulLoad, Double dlLoad) {
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
		this.ulLoad = ulLoad;			
		this.dlLoad = dlLoad;			
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

	public Double getULExpected() {
		return ULExpected;
	}

	public void setULExpected(Double uLExpected) {
		ULExpected = uLExpected;
	}

	public Double getDLExpected() {
		return DLExpected;
	}

	public void setDLExpected(Double dLExpected) {
		DLExpected = dLExpected;
	}
}

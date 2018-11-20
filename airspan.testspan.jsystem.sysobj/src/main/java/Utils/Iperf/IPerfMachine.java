package Utils.Iperf;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Entities.ITrafficGenerator.TransmitDirection;
import Utils.GeneralUtils;
import Utils.Pair;
import jsystem.framework.system.SystemObjectImpl;

public abstract class IPerfMachine extends SystemObjectImpl{
	private static int minNumberOfSamples = 1;
	private static int numberOfLinesForSample = minNumberOfSamples * 3;
	
	protected String preAddressTpFile;
	protected String hostname;
	protected String username;
	protected String password;
	
	public IPerfMachine(){
		super();
		preAddressTpFile = "";
	}

	public abstract boolean connect();

	protected void resetParams(){
		minNumberOfSamples = 1;
		numberOfLinesForSample = minNumberOfSamples * 3;
	}

	
	
	public abstract Pair<Boolean,String> sendCommand(String command);
	public abstract String startIPerfTraffic(String clientCommand, String tpFileName, TransmitDirection transmitDirection);
	public abstract String startIPerfListener(Integer numberOfParallelIPerfStreams, String serverCommand, String tpFileName, TransmitDirection transmitDirection);
	public abstract String getStrCounters(String tpCountersFileNames);
	public abstract File getFile(String fileName);
	public abstract boolean putFile(String fileName);
	public abstract boolean stopIPerf();

	public abstract void disconnect();

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getPreAddressTpFile() {
		return preAddressTpFile;
	}

	public static int getMinNumberOfSamples() {
		return minNumberOfSamples;
	}

	public static void setMinNumberOfSamples(int minNumberOfSamples) {
		IPerfMachine.minNumberOfSamples = minNumberOfSamples;
		numberOfLinesForSample = minNumberOfSamples * 3;
	}

	public static int getNumberOfLinesForSample() {
		return numberOfLinesForSample;
	}

	protected Pair<Double, ArrayList<Long>> parseCounterFromIPerfServerRespond(boolean isMultiSession, Double lastEndOfIntervalUsedForLastSample, String countersStr, String tpFileName) throws Exception{
		tpFileName =  preAddressTpFile + tpFileName;
		String tpLinesStr = countersStr.contains(tpFileName)? countersStr : "";
		if(tpLinesStr.contains("=> "+tpFileName)){
			tpLinesStr = countersStr.substring(countersStr.indexOf("=> "+tpFileName));
		}
		if(tpLinesStr.contains("==>")){
			tpLinesStr = tpLinesStr.substring(0, tpLinesStr.indexOf("==>"));
		}
		String[] tpLines = tpLinesStr.split("\n");
		ArrayList<Long> rxCounterList= new ArrayList<Long>();
		String lastTpLineUsedForCurrentSample = "";
		if(tpLines != null){ 
			for(String tpLine : tpLines){
				if(tpLine.contains("Kbits")){
					if((isMultiSession == false) || (tpLine.contains("SUM"))){
						Double lastEndOfIntervalOfTpLine = 0.0;
						try{
							String regexPattern = 	"] \\d+.\\d+-(\\d+.\\d+)";
							Pattern pattern = Pattern.compile(regexPattern);
							Matcher matcher = pattern.matcher(tpLine);
							matcher.find();
							lastEndOfIntervalOfTpLine = Double.valueOf(matcher.group(1));
						}catch(Exception e){
							lastEndOfIntervalOfTpLine = 0.0;
						}
						if((lastEndOfIntervalUsedForLastSample == 0.0) || (lastEndOfIntervalOfTpLine > lastEndOfIntervalUsedForLastSample)){
							if(rxCounterList.size() < minNumberOfSamples){
								long rxCounter = 0;
								try{
									String regexPattern = 	".(\\d+) Kbits";
									Pattern pattern = Pattern.compile(regexPattern);
									Matcher matcher = pattern.matcher(tpLine);
									matcher.find();
									String rxCounterStr = matcher.group(1);
									rxCounter = Long.valueOf(rxCounterStr);
									rxCounter *= 1000;
									rxCounterList.add(rxCounter);
									lastTpLineUsedForCurrentSample = tpLine;
								}catch(Exception e){
									//do nothing.
								}
							}else{
								break;
							}
						}
					}
				}
			}
		}
		if(rxCounterList.isEmpty()){ //Failed to get sample 
			GeneralUtils.printToConsole("IPerfMachine.parseCounterFromIPerfServerRespond: Failed to get sample From " + tpFileName);
			rxCounterList.add((long) 0);
		}
		//Save Minimum number of samples.
		if(rxCounterList.size() < minNumberOfSamples){
			setMinNumberOfSamples(rxCounterList.size());
		}
		//Update last Interval Used For Current Sample
		Double lastEndOfIntervalUsedForCurrentSample = lastEndOfIntervalUsedForLastSample;
		try{
			String regexPattern = 	"] \\d+.\\d+-(\\d+.\\d+)";
			Pattern pattern = Pattern.compile(regexPattern);
			Matcher matcher = pattern.matcher(lastTpLineUsedForCurrentSample);
			matcher.find();
			lastEndOfIntervalUsedForCurrentSample = Double.valueOf(matcher.group(1));
		}catch(Exception e){
			lastEndOfIntervalUsedForCurrentSample = lastEndOfIntervalUsedForLastSample;
		}
		GeneralUtils.printToConsole("lastIntervalUsedForCurrentSample="+lastEndOfIntervalUsedForCurrentSample+" From " + tpFileName);
		Pair<Double, ArrayList<Long>> sample = new Pair<Double, ArrayList<Long>>(lastEndOfIntervalUsedForCurrentSample, rxCounterList);
		return sample;
	}
}

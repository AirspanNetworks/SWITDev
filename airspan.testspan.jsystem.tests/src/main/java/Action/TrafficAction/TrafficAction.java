package Action.TrafficAction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import jsystem.framework.ParameterProperties;
import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;
import jsystem.framework.scenario.Parameter;
import testsNG.Actions.Traffic.GeneratorType;
import testsNG.Actions.TrafficManager;
import Action.Action;
import EnodeB.EnodeB;
import Entities.ITrafficGenerator.Protocol;
import Entities.ITrafficGenerator.TransmitDirection;
import UE.UE;
import UeSimulator.Amarisoft.AmariSoftServer;
import Utils.GeneralUtils;
import Utils.SysObjUtils;
import Utils.Iperf.UEIPerf;

public class TrafficAction extends Action {
	private ArrayList<UE> ues = null;
	private TrafficManager trafficManagerInstance = null;
	private GeneratorType generatorType = GeneratorType.Iperf;
	private Protocol trafficType = Protocol.UDP;
	private TransmitDirection transmitDirection = TransmitDirection.BOTH;
	private Integer runTime = null;
	private ArrayList<Character> qci = null;
	private LoadType loadType = LoadType.Calculator_Based;
	private EnodeB dut = null;
	private String ULLoad = null;
	private String DLLoad = null;
	private Integer frameSize = null;
	private Double windowSize = null;
	private Integer parallelStreams = null;
	private Integer mss = null;
	private ExpectedType expectedLoadType = ExpectedType.Calculator_Based;
	private String ULExpected = null;
	private String DLExpected = null;
	private String semanticName = null;
	private ArrayList<String> trafficToStop = null;
	private ArrayList<String> trafficToGetStatistics = null;
	private ArrayList<String> toCheck = null;

	public ArrayList<String> getTrafficToGetStatistics() {
		return trafficToGetStatistics;
	}

	@ParameterProperties(description = "Traffic names to get statistics, separated by comma. Leave empty to get all traffics statistics")
	public void setTrafficToGetStatistics(ArrayList<String> trafficToGetStatistics) {
		this.trafficToGetStatistics = trafficToGetStatistics;
	}

	public ArrayList<String> getTrafficToStop() {
		return trafficToStop;
	}

	@ParameterProperties(description = "Traffic names to stop, separated by comma. Leave empty to stop all traffics")
	public void setTrafficToStop(String trafficToStop) {
		this.trafficToStop = new ArrayList<String>();
		if (trafficToStop != null) {
			String[] temp = trafficToStop.split(",");
			for (String str : temp) {
				this.trafficToStop.add(str);
			}
		}
	}

	public Integer getFrameSize() {
		return frameSize;
	}

	@ParameterProperties(description = "Frame size for udp traffic. Default - 1400")
	public void setFrameSize(String frameSize) {
		this.frameSize = Integer.valueOf(frameSize);
	}

	public String getSemanticName() {
		return semanticName;
	}

	@ParameterProperties(description = "This string will be used for verify traffic results by name")
	public void setSemanticName(String semanticName) {
		this.semanticName = semanticName;
	}

	public String getULExpected() {
		return ULExpected;
	}

	@ParameterProperties(description = "When ExpectedLoadType is Calculator/Load Based: percentage from calculator/input value. Else, value in Mbps")
	public void setULExpected(String uLExpected) {
		ULExpected = uLExpected;
	}

	public String getDLExpected() {
		return DLExpected;
	}

	@ParameterProperties(description = "When ExpectedLoadType is Calculator/Load Based: percentage from calculator/input value. Else, value in Mbps")
	public void setDLExpected(String dLExpected) {
		DLExpected = dLExpected;
	}

	public ExpectedType getExpectedLoadType() {
		return expectedLoadType;
	}

	@ParameterProperties(description = "Type of expected traffic: calculator based/custom")
	public void setExpectedLoadType(ExpectedType expectedType) {
		this.expectedLoadType = expectedType;
	}

	public Double getWindowSize() {
		return windowSize;
	}

	@ParameterProperties(description = "value for -w Iperf Parameter (not mandatory)")
	public void setWindowSize(String windowSize) {
		this.windowSize = Double.valueOf(windowSize);
	}

	public Integer getParallelStreams() {
		return parallelStreams;
	}

	@ParameterProperties(description = "value for -P iperf parameter (not mandatory)")
	public void setParallelStreams(String parallelStreams) {
		this.parallelStreams = Integer.valueOf(parallelStreams);
	}

	public Integer getMss() {
		return mss;
	}

	@ParameterProperties(description = "value for -m iperf parameter (not mandatory)")
	public void setMss(String mss) {
		this.mss = Integer.valueOf(mss);
	}

	public String getULLoad() {
		return ULLoad;
	}

	@ParameterProperties(description = "When LoadType is custom: load value in Mbps. When is Calculator Based: load percentage from calculator value")
	public void setULLoad(String uLLoad) {
		ULLoad = uLLoad;
	}

	public String getDLLoad() {
		return DLLoad;
	}

	@ParameterProperties(description = "When LoadType is custom: load value in Mbps. When is Calculator Based: load percentage from calculator value")
	public void setDLLoad(String dLLoad) {
		DLLoad = dLLoad;
	}

	@ParameterProperties(description = "Name of ENB from the SUT (Mandatory when LoadType or ExpectedLoadType is calculator based)")
	public void setDut(String dut) {
		ArrayList<EnodeB> temp = (ArrayList<EnodeB>) SysObjUtils.getInstnce().initSystemObject(EnodeB.class, false,
				dut.split(","));
		this.dut = temp.get(0);
	}

	public EnodeB getDut() {
		return dut;
	}

	public LoadType getLoadType() {
		return loadType;
	}

	@ParameterProperties(description = "Type of traffic load: calculator based/custom")
	public void setLoadType(LoadType loadType) {
		this.loadType = loadType;
	}

	public ArrayList<Character> getQci() {
		return qci;
	}

	@ParameterProperties(description = "Comma based QCIs")
	public void setQci(String qci) {
		ArrayList<String> qciToSplit = new ArrayList<String>(Arrays.asList(qci.split(",")));
		this.qci = new ArrayList<Character>();
		for (String str : qciToSplit) {
			this.qci.add(Character.valueOf(str.toCharArray()[0]));
		}
	}

	public Integer getRunTime() {
		return runTime;
	}

	@ParameterProperties(description = "Run time in format HH:MM:SS (not mandatory)")
	public void setRunTime(String runTime) {
		Pattern p = Pattern.compile("(\\d+):(\\d+):(\\d+)");
		Matcher m = p.matcher(runTime);
		if (m.find()) {
			int hours = Integer.valueOf(m.group(1)) * 60 * 60;
			int minutes = Integer.valueOf(m.group(2)) * 60;
			int seconds = Integer.valueOf(m.group(3));
			this.runTime = hours + minutes + seconds;
		} else {
			this.runTime = null;
		}
	}

	public TransmitDirection getTransmitDirection() {
		return transmitDirection;
	}

	@ParameterProperties(description = "Which direction to run Traffic")
	public void setTransmitDirection(TransmitDirection transmitDirection) {
		this.transmitDirection = transmitDirection;
	}

	public GeneratorType getGeneratorType() {
		return generatorType;
	}

	@ParameterProperties(description = "Choose traffic generator type: Iperf/STC)")
	public void setGeneratorType(GeneratorType genType) {
		this.generatorType = genType;
	}

	public Protocol getTrafficType() {
		return trafficType;
	}

	@ParameterProperties(description = "Which traffic to run")
	public void setTrafficType(Protocol protocol) {
		this.trafficType = protocol;
	}

	public enum LoadType {
		Calculator_Based, Custom
	}

	public enum ExpectedType {
		Calculator_Based, Custom
	}

	protected ArrayList<String> convertUeToNamesList(ArrayList<UE> ueList2) {
		ArrayList<String> ueList = new ArrayList<String>();
		for (UE ue : ueList2) {
			ueList.add("UE" + ue.getName().replaceAll("\\D+", "").trim());
		}
		return ueList;
	}

	@ParameterProperties(description = "Name of UEs from the SUT")
	public void setUEs(String ues) {
		ArrayList<UE> tempUes = new ArrayList<>();
		String[] ueArray = ues.split(",");
		for (int i = 0; i < ueArray.length; i++) {
			if (ueArray[i].toLowerCase().trim().equals(AmariSoftServer.amarisoftIdentifier)) {
				try {
					AmariSoftServer uesim = AmariSoftServer.getInstance();
					tempUes.addAll(uesim.getUeList());
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				try {
					tempUes.addAll(
							(ArrayList<UE>) SysObjUtils.getInstnce().initSystemObject(UE.class, false, ueArray[i]));
				} catch (Exception e) {
					report.report("Failed init object " + ueArray[i]);
					e.printStackTrace();
				}
			}
		}
		this.ues = (ArrayList<UE>) tempUes.clone();
	}

	@Test
	@TestProperties(name = "Get Traffic Statistics", returnParam = "LastStatus", paramsInclude = {
			"TrafficToGetStatistics" })
	public void getTrafficStatistics() {
		trafficManagerInstance = TrafficManager.getInstance(null);
		if (trafficManagerInstance == null) {
			report.report("Failed to init traffic manager instance", Reporter.FAIL);
			reason = "Failed to init traffic manager instance";
			return;
		}
		if (trafficToGetStatistics == null) {
			trafficToGetStatistics = new ArrayList<String>();
		}
		trafficManagerInstance.getTrafficStatistics(trafficToGetStatistics);
		if(!trafficManagerInstance.getReason().isEmpty()){
			reason = trafficManagerInstance.getReason();
		}
	}

	@Test
	@TestProperties(name = "Stop Traffic", returnParam = "LastStatus", paramsInclude = { "TrafficToStop" })
	public void stopTraffic() {

		trafficManagerInstance = TrafficManager.getInstance(null);
		if (trafficManagerInstance == null) {
			report.report("Failed to init traffic manager instance", Reporter.FAIL);
			reason = "Failed to init traffic manager instance";
			return;
		}
		if (trafficToStop == null) {
			trafficToStop = new ArrayList<String>();
		}
		trafficManagerInstance.stopTraffic(trafficToStop);
		if(!trafficManagerInstance.getReason().isEmpty()){
			reason = trafficManagerInstance.getReason();
		}
	}

	@Test
	@TestProperties(name = "Start Traffic", returnParam = "LastStatus", paramsInclude = { "UEs", "GeneratorType",
			"TrafficType", "TransmitDirection", "RunTime", "Qci", "SemanticName", "LoadType", "Dut", "ULLoad", "DLLoad",
			"FrameSize", "WindowSize", "ParallelStreams", "Mss", "ExpectedLoadType", "ULExpected", "DLExpected" })
	public void startTraffic() {
		GeneralUtils.startLevel("Parameters for start traffic");
		if (!validateParams()){
			GeneralUtils.stopLevel();			
			return;
		}
		tptCalculation();
		GeneralUtils.stopLevel();
		if (!validateStreams())
			return;
		trafficManagerInstance.startTraffic(semanticName, convertUeToNamesList(ues), qci, loadType, ULLoad, DLLoad,
				transmitDirection, expectedLoadType, ULExpected, DLExpected, dut, parallelStreams, windowSize, mss,
				frameSize, trafficType, runTime, toCheck);
		if(!trafficManagerInstance.getReason().isEmpty()){
			reason = trafficManagerInstance.getReason();
		}
	}

	private boolean validateStreams() {
		if (trafficManagerInstance.checkIfNameExist(semanticName)) {
			report.report("Action failed - trying to run traffic with a semantic name already running", Reporter.FAIL);
			reason = "Action failed - trying to run traffic with a semantic name already running";
			return false;
		}

		if (!trafficManagerInstance.checkGeneratorType(generatorType)) {
			reason = "Can not start traffic with different generator type from before";
			return false;
		}

		toCheck = convertToStreamStrings(ues, qci, transmitDirection, trafficType);
		if (trafficManagerInstance.checkIfStreamsExist(toCheck)) {
			report.report("Action failed - trying to run traffic on a stream already running", Reporter.FAIL);
			reason = "Action failed - trying to run traffic on a stream already running";
			return false;
		}
		return true;
	}

	private void tptCalculation() {
		if (trafficType == Protocol.TCP) {
			if (windowSize != null) {
				report.report("Window size: " + windowSize);
			}
			if (parallelStreams != null) {
				report.report("Number of parallel streams: " + parallelStreams);
			}
			if (mss != null) {
				report.report("Mss: " + mss);
			}
		} else {
			report.report("Load Type: " + loadType.toString());
			boolean calc = loadType == LoadType.Calculator_Based;
			if (ULLoad != null) {
				report.report("UL Load: " + ULLoad + (calc ? "% from calculator value" : " Mbps"));
			}
			if (DLLoad != null) {
				report.report("DL Load: " + DLLoad + (calc ? "% from calculator value" : " Mbps"));
			}
			if (frameSize != null) {
				report.report("Frame size: " + frameSize);
			} else {
				frameSize = 1400;
				report.report("Frame size was not configured. Default frame size: " + frameSize);
			}
		}
		report.report("Expected load type: " + expectedLoadType.toString());
		boolean custom = expectedLoadType == ExpectedType.Custom;
		if (ULExpected != null) {
			report.report("UL expected: " + ULExpected + (custom ? " Mbps" : "%"));
		}
		if (DLExpected != null) {
			report.report("DL expected: " + DLExpected + (custom ? " Mbps" : "%"));
		}
	}

	private boolean validateParams() {
		/*if (trafficType == Protocol.TCP && generatorType == GeneratorType.STC) {
			report.report("Traffic type " + trafficType.toString() + " is not compatible with generator type "
					+ generatorType.toString(), Reporter.FAIL);
			reason = "Traffic type " + trafficType.toString() + " is not compatible with generator type "
					+ generatorType.toString();
			return false;
		}*/
		if (ues == null) {
			report.report("No UEs were configured", Reporter.WARNING);
		}
		if (qci == null) {
			report.report("No QCIs were configured", Reporter.FAIL);
			reason = "No QCIs were configured";
			return false;
		}
		if (semanticName == null) {
			report.report("No name was configured", Reporter.FAIL);
			reason = "No name was configured";
			return false;
		}
		if (runTime != null) {
			report.report("Run time: " + runTime + " seconds");
		} else {
			report.report("Default time will be applied (" + UEIPerf.IPERF_TIME_LIMIT + " seconds)");
		}
		if (dut != null) {
			report.report("EnodeB for calculator criterias: " + dut.getNetspanName());
		}
		trafficManagerInstance = TrafficManager.getInstance(generatorType);

		if (trafficManagerInstance == null) {
			report.report("Generator type was not found in SUT", Reporter.FAIL);
			reason = "Generator type was not found in SUT";
			return false;
		}
		report.report("Semantic name: " + semanticName);
		report.report("Generator type: " + generatorType.toString());
		report.report("Traffic type: " + trafficType.toString());
		report.report("Transmit direction: " + transmitDirection.toString());
		report.report("UEs: " + ues.toString());
		report.report("Qcis: " + qci.toString());
		return true;
	}

	public ArrayList<String> convertToStreamStrings(ArrayList<UE> ues, ArrayList<Character> qci,
			TransmitDirection transmitDirection, Protocol protocol) {

		ArrayList<String> toReturn = new ArrayList<String>();
		String protocolForStream = (protocol == Protocol.TCP ? "TCP_" : "UDP_");
		for (UE ue : ues) {
			String numUE = ue.getName().replaceAll("\\D+", "").trim();
			for (Character ch : qci) {
				if (transmitDirection == TransmitDirection.BOTH || transmitDirection == TransmitDirection.UL) {
					toReturn.add(protocolForStream + "UL_UE" + numUE + ch);
				}
				if (transmitDirection == TransmitDirection.BOTH || transmitDirection == TransmitDirection.DL) {
					toReturn.add(protocolForStream + "DL_UE" + numUE + ch);
				}
			}
		}
		return toReturn;
	}

	@Override
	public void handleUIEvent(HashMap<String, Parameter> map, String methodName) throws Exception {

		if (methodName.equals("startTraffic")) {
			handleUIEventStartTraffic(map);
		}
	}

	private void handleUIEventStartTraffic(HashMap<String, Parameter> map) {
		map.get("LoadType").setVisible(false);
		map.get("Dut").setVisible(false);
		map.get("ULLoad").setVisible(false);
		map.get("DLLoad").setVisible(false);
		map.get("FrameSize").setVisible(false);
		map.get("WindowSize").setVisible(false);
		map.get("ParallelStreams").setVisible(false);
		map.get("Mss").setVisible(false);
		map.get("ULExpected").setVisible(false);
		map.get("DLExpected").setVisible(false);

		Parameter trafficType = map.get("TrafficType");
		Parameter direction = map.get("TransmitDirection");

		if (Protocol.UDP == Protocol.valueOf(trafficType.getValue().toString())) {
			map.get("LoadType").setVisible(true);
			map.get("FrameSize").setVisible(true);
			if (TransmitDirection.UL == TransmitDirection.valueOf(direction.getValue().toString())
					|| TransmitDirection.BOTH == TransmitDirection.valueOf(direction.getValue().toString())) {
				map.get("ULLoad").setVisible(true);
			} else {
				map.get("ULLoad").setValue(null);
			}
			if (TransmitDirection.DL == TransmitDirection.valueOf(direction.getValue().toString())
					|| TransmitDirection.BOTH == TransmitDirection.valueOf(direction.getValue().toString())) {
				map.get("DLLoad").setVisible(true);
			} else {
				map.get("DLLoad").setValue(null);
			}
			map.get("WindowSize").setValue(null);
			map.get("ParallelStreams").setValue(null);
			map.get("Mss").setValue(null);
			if (TransmitDirection.UL == TransmitDirection.valueOf(direction.getValue().toString())
					|| TransmitDirection.BOTH == TransmitDirection.valueOf(direction.getValue().toString())) {
				map.get("ULLoad").setVisible(true);
			} else {
				map.get("ULLoad").setValue(null);
			}
			if (TransmitDirection.DL == TransmitDirection.valueOf(direction.getValue().toString())
					|| TransmitDirection.BOTH == TransmitDirection.valueOf(direction.getValue().toString())) {
				map.get("DLLoad").setVisible(true);
			} else {
				map.get("DLLoad").setValue(null);
			}
		} else {
			map.get("WindowSize").setVisible(true);
			map.get("ParallelStreams").setVisible(true);
			map.get("Mss").setVisible(true);
			map.get("FrameSize").setValue(null);
			map.get("FrameSize").setVisible(false);
			map.get("ULLoad").setValue(null);
			map.get("DLLoad").setValue(null);
			map.get("LoadType").setValue(LoadType.Custom);
		}

		if (TransmitDirection.UL == TransmitDirection.valueOf(direction.getValue().toString())
				|| TransmitDirection.BOTH == TransmitDirection.valueOf(direction.getValue().toString())) {
			map.get("ULExpected").setVisible(true);
		} else {
			map.get("ULExpected").setValue(null);
		}
		if (TransmitDirection.DL == TransmitDirection.valueOf(direction.getValue().toString())
				|| TransmitDirection.BOTH == TransmitDirection.valueOf(direction.getValue().toString())) {
			map.get("DLExpected").setVisible(true);
		} else {
			map.get("DLExpected").setValue(null);
		}

		Parameter loadType = map.get("LoadType");
		Parameter expectedType = map.get("ExpectedLoadType");
		if ((loadType != null && LoadType.Calculator_Based == LoadType.valueOf(loadType.getValue().toString()))
				|| ExpectedType.Calculator_Based == ExpectedType.valueOf(expectedType.getValue().toString())) {
			map.get("Dut").setVisible(true);
		} else {
			map.get("Dut").setValue(null);
			map.get("Dut").setVisible(false);
		}
	}
}
package Action.TrafficAction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.junit.Test;

import jsystem.framework.ParameterProperties;
import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;
import jsystem.framework.scenario.Parameter;
import testsNG.Actions.Traffic;
import testsNG.Actions.Traffic.GeneratorType;
import testsNG.Actions.Traffic.TrafficType;
import testsNG.Actions.TrafficManager;
import Action.Action;
import EnodeB.EnodeB;
import Entities.ITrafficGenerator.Protocol;
import Entities.ITrafficGenerator.TransmitDirection;
import UE.UE;
import UeSimulator.Amarisoft.AmariSoftServer;
import Utils.GeneralUtils;
import Utils.SysObjUtils;

public class TrafficAction extends Action {
	private ArrayList<UE> ues;
	private TrafficManager trafficManagerInstance = null;
	private GeneratorType generatorType = GeneratorType.Iperf;
	private Protocol trafficType = Protocol.UDP;
	private TransmitDirection transmitDirection = TransmitDirection.BOTH;
	private Integer runTime;
	private ArrayList<Character> qci;
	private LoadType loadType = LoadType.Calculator_Based;
	private EnodeB dut;
	private String ULLoad;
	private String DLLoad;
	private Integer frameSize;
	private Double windowSize;
	private Integer parallelStreams;
	private Integer mss;
	private ExpectedType expectedLoadType = ExpectedType.Custom;
	private String ULExpected;
	private String DLExpected;
	private String semanticName;
	private ArrayList<String> trafficToStop;

	public ArrayList<String> getTrafficToStop() {
		return trafficToStop;
	}

	
	public void setTrafficToStop(String trafficToStop) {
		this.trafficToStop = new ArrayList<String>();
		if(trafficToStop != null){
			String[] temp = trafficToStop.split(",");
			for(String str : temp){
				this.trafficToStop.add(str);
			}
		}
	}

	public Integer getFrameSize() {
		return frameSize;
	}

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

	@ParameterProperties(description = "Name of ENB from the SUT")
	public void setDut(String dut) {
		ArrayList<EnodeB> temp = (ArrayList<EnodeB>) SysObjUtils.getInstnce().initSystemObject(EnodeB.class, false,
				dut.split(","));
		this.dut = temp.get(0);			
	}
	
	public EnodeB getDut(){
		return dut;
	}
	
	public LoadType getLoadType() {
		return loadType;
	}

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
		for(String str : qciToSplit){
			this.qci.add(Character.valueOf(str.toCharArray()[0]));
		}
	}

	public Integer getRunTime() {
		return runTime;
	}

	@ParameterProperties(description = "Run time in seconds (not mandatory)")
	public void setRunTime(String runTime) {
		this.runTime = Integer.valueOf(runTime);			
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
	
	@ParameterProperties(description = "Choose traffic generator type, Default will use Traffic Generator Priority (Currently STC → Iperf)")
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

	public enum LoadType{
		Calculator_Based, Custom;
	}
	
	public enum ExpectedType{
		Calculator_Based, Load_Based, Custom;
	}
	
	protected ArrayList<String> convertUeToNamesList(ArrayList<UE> ueList2) {
		ArrayList<String> ueList = new ArrayList<String>();
		for (UE ue : ueList2) {
			ueList.add("UE" + ue.getName().replaceAll("\\D+", "").trim());
		}
		return ueList;
	}
	
	@ParameterProperties(description = "UEs")
	public void setUEs(String ues) {
		ArrayList<UE> tempUes = new ArrayList<>();
		String[] ueArray = ues.split(",");
		for (int i = 0; i < ueArray.length; i++) {					
			if(ueArray[i].toLowerCase().trim().equals(AmariSoftServer.amarisoftIdentifier)){
				try {
					AmariSoftServer uesim = AmariSoftServer.getInstance();
					tempUes.addAll(uesim.getUeList());
				} catch (Exception e) {
					e.printStackTrace();
				}	
			}
			else{
				try {
					tempUes.addAll((ArrayList<UE>) SysObjUtils.getInstnce().initSystemObject(UE.class, false, ueArray[i]));			
				} catch (Exception e) {
					report.report("Failed init object " + ueArray[i]);
					e.printStackTrace();
				}
			}
		}
		this.ues = (ArrayList<UE>) tempUes.clone();	
	}

	@Test // 1
	@TestProperties(name = "Start Traffic", returnParam = "LastStatus", paramsInclude = { "UEs" })
	public void startTraffic() {
		report.report("Start Traffic");

		if (Traffic.getInstance(ues).startTraffic(ues) == false) {
			report.report("Start Traffic Failed", Reporter.FAIL);
		} else {
			report.report("Start Traffic Succeeded");
		}
	}

	@Test // 2
	@TestProperties(name = "Stop Traffic", returnParam = "LastStatus", paramsInclude = { "UEs" })
	public void stopTraffic() {
		report.report("Stop Traffic");

		if (Traffic.getInstance(ues).stopTraffic() == false) {
			report.report("Stop Traffic Failed", Reporter.FAIL);
		} else {
			report.report("Stop Traffic Succeeded");
		}
	}

	// @Test // 5
	@TestProperties(name = "Get Traffic Statistics", returnParam = "LastStatus", paramsInclude = { "" })
	public void getTrafficStatistics() {
		report.report("Get Traffic Statistics");
		// TODO
	}
	
	@Test
	@TestProperties(name = "Enhanced Stop Traffic", returnParam = "LastStatus", paramsInclude = { "TrafficToStop" })
	public void enhancedStopTraffic() {

		trafficManagerInstance = TrafficManager.getInstance(null);
		if(trafficManagerInstance == null){
			report.report("Failed to init traffic manager instance",Reporter.FAIL);
			return;
		}
		if(trafficToStop == null){
			trafficToStop = new ArrayList<String>();
		}
		trafficManagerInstance.stopTraffic(trafficToStop);
	}
	
	@Test
	@TestProperties(name = "Enhanced Start Traffic", returnParam = "LastStatus", paramsInclude = { "UEs","GeneratorType",
			"TrafficType","TransmitDirection","RunTime","Qci","SemanticName","LoadType","Dut","ULLoad","DLLoad","FrameSize",
			"WindowSize","ParallelStreams","Mss","ExpectedLoadType","ULExpected","DLExpected"})
	public void enhancedStartTraffic() {
			
		if(trafficType == Protocol.TCP && generatorType == GeneratorType.STC){
			report.report("Traffic type " + trafficType.toString() + " is not compatible with generator type " + generatorType.toString(), Reporter.FAIL);
			return;
		}
		if(ues == null){
			report.report("No UEs were configured", Reporter.WARNING);
			return;
		}
		if(qci == null){
			report.report("No QCIs were configured", Reporter.FAIL);
			return;
		}
		
		if(semanticName == null){
			report.report("No name was configured", Reporter.FAIL);
			return;
		}
		
		trafficManagerInstance = TrafficManager.getInstance(generatorType);
		
		if(trafficManagerInstance == null){
			report.report("Generator type was not found in SUT",Reporter.FAIL);
			return;
		}
		
		GeneralUtils.startLevel("Parameters for start traffic");
		report.report("Semantic name: "+semanticName);
		report.report("Generator type: "+generatorType.toString());
		report.report("Traffic type: "+trafficType.toString());
		report.report("Transmit direction: "+transmitDirection.toString());
		report.report("Run time: "+runTime);
		report.report("UEs: "+ues.toString());
		report.report("Qcis: "+qci.toString());
		if(dut != null){
			report.report("EnodeB for calculator criterias: "+dut.getNetspanName());
		}
		
		if(trafficType == Protocol.TCP){
			if(windowSize != null){
				report.report("Window size: "+windowSize);
			}
			if(parallelStreams != null){
				report.report("Number of parallel streams: "+parallelStreams);
			}
			if(mss != null){
				report.report("Mss: "+mss);
			}			
		}else{
			report.report("Load Type: "+loadType.toString());
			boolean calc = loadType==LoadType.Calculator_Based;
			if(ULLoad != null){
				report.report("UL Load: "+ULLoad+(calc?"% from calculator value":" Mbps"));
			}
			if(DLLoad != null){
				report.report("DL Load: "+DLLoad+(calc?"% from calculator value":" Mbps"));
			}
			if(frameSize != null){
				report.report("Frame size: "+frameSize);
			}else{
				frameSize = 1400;
				report.report("Frame size was not configured. Default frame size: "+frameSize);
			}
		}
		report.report("Expected load type: "+expectedLoadType.toString());
		boolean custom = expectedLoadType == ExpectedType.Custom;
		if(ULExpected != null){
			report.report("UL expected: "+ULExpected+(custom?" Mbps":"%"));				
		}
		if(DLExpected != null){
			report.report("DL expected: "+DLExpected+(custom?" Mbps":"%"));				
		}
		
		GeneralUtils.stopLevel();

		if(trafficManagerInstance.checkIfNameExist(semanticName)){
			report.report("Action failed - trying to run traffic with a semantic name already running", Reporter.FAIL);
			return;
		}
		
		if(!trafficManagerInstance.checkGeneratorType(generatorType)){
			return;
		}

		ArrayList<String> toCheck = convertToStreamStrings(ues,qci,transmitDirection,trafficType);
		if(trafficManagerInstance.checkIfStreamsExist(toCheck)){
			report.report("Action failed - trying to run traffic on a stream already running", Reporter.FAIL);
			return;
		}
		
		trafficManagerInstance.startTraffic(semanticName, convertUeToNamesList(ues), qci, loadType, ULLoad,DLLoad, transmitDirection, 
				expectedLoadType, ULExpected, DLExpected, dut, parallelStreams, windowSize, mss, frameSize, trafficType, runTime, toCheck);
	}

	public ArrayList<String> convertToStreamStrings(ArrayList<UE> ues, ArrayList<Character> qci, TransmitDirection transmitDirection, Protocol protocol){
		ArrayList<String> toReturn = new ArrayList<String>();
		String protocolForStream = (protocol == Protocol.TCP ? "TCP_":"UDP_");
		for(UE ue:ues){
			String numUE = ue.getName().replaceAll("\\D+", "").trim();
			for(Character ch:qci){
				if(transmitDirection == TransmitDirection.BOTH || transmitDirection == TransmitDirection.UL){
					toReturn.add(protocolForStream+"UL_UE"+numUE+ch);
				}
				if(transmitDirection == TransmitDirection.BOTH || transmitDirection == TransmitDirection.DL){
					toReturn.add(protocolForStream+"DL_UE"+numUE+ch);
				}
			}
		}
		return toReturn;
	}
	
	@Override
	public void handleUIEvent(HashMap<String, Parameter> map, String methodName) throws Exception {

		if (methodName.equals("enhancedStartTraffic")) {
			handleUIEventGetCounterValue(map, methodName);
		}
	}
	
	private void handleUIEventGetCounterValue(HashMap<String, Parameter> map, String methodName) {
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

		if(Protocol.UDP == Protocol.valueOf(trafficType.getValue().toString())){
			map.get("LoadType").setVisible(true);
			map.get("FrameSize").setVisible(true);
			if(TransmitDirection.UL == TransmitDirection.valueOf(direction.getValue().toString()) || 
					TransmitDirection.BOTH == TransmitDirection.valueOf(direction.getValue().toString())){
				map.get("ULLoad").setVisible(true);
			}else{
				map.get("ULLoad").setValue(null);
			}
			if(TransmitDirection.DL == TransmitDirection.valueOf(direction.getValue().toString()) || 
					TransmitDirection.BOTH == TransmitDirection.valueOf(direction.getValue().toString())){
				map.get("DLLoad").setVisible(true);
			}else{
				map.get("DLLoad").setValue(null);
			}
			map.get("WindowSize").setValue(null);
			map.get("ParallelStreams").setValue(null);
			map.get("Mss").setValue(null);
		}else{
			map.get("WindowSize").setVisible(true);
			map.get("ParallelStreams").setVisible(true);
			map.get("Mss").setVisible(true);
			map.get("FrameSize").setValue(null);
			map.get("FrameSize").setVisible(false);
			map.get("ULLoad").setValue(null);
			map.get("DLLoad").setValue(null);
		}
		
		if(TransmitDirection.UL == TransmitDirection.valueOf(direction.getValue().toString()) || 
				TransmitDirection.BOTH == TransmitDirection.valueOf(direction.getValue().toString())){
			map.get("ULExpected").setVisible(true);
		}else{
			map.get("ULExpected").setValue(null);
		}
		if(TransmitDirection.DL == TransmitDirection.valueOf(direction.getValue().toString()) || 
				TransmitDirection.BOTH == TransmitDirection.valueOf(direction.getValue().toString())){
			map.get("DLExpected").setVisible(true);
		}else{
			map.get("DLExpected").setValue(null);
		}
		
		Parameter loadType = map.get("LoadType");
		Parameter expectedType = map.get("ExpectedLoadType");
		if(loadType != null && (LoadType.Calculator_Based == LoadType.valueOf(loadType.getValue().toString())
				|| ExpectedType.Calculator_Based == ExpectedType.valueOf(expectedType.getValue().toString()))){
			map.get("Dut").setVisible(true);
		}else{
			map.get("Dut").setValue(null);
			map.get("Dut").setVisible(false);
		}
	}
}
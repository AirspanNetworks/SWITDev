package testsNG.ProtocolsAndServices.NLM;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import Utils.*;
import org.junit.Test;
import Attenuators.AttenuatorSet;
import EnodeB.AirVelocity;
import EnodeB.EnodeB;
import EnodeB.SnifferFileLocation;
import Netspan.API.Enums.EnbStates;
import Netspan.API.Enums.PrimaryClockSourceEnum;
import Netspan.Profiles.EnodeBAdvancedParameters;
import Netspan.Profiles.SyncParameters;
import Utils.PcapUtils.PcapParams;
import Utils.PcapUtils.PcapParser;
import io.pkts.PacketHandler;
import io.pkts.buffer.Buffer;
import io.pkts.packet.Packet;
import jsystem.framework.ParameterProperties;
import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;
import jsystem.framework.report.ReporterHelper;
import testsNG.TestspanTest;
import testsNG.Actions.EnodeBConfig;
import testsNG.Actions.PeripheralsConfig;

public class NLM extends TestspanTest {

	private EnodeB dut = null;
	private EnodeB dut2 = null;
	private EnodeB enodeB1588 = null;
	private EnodeB GPSEnodeB = null;
	private ScpClient scpCli = null;
	private File pcapFile0;
	private File pcapFile1;
	PcapParser pcapP = null;
	pktHndl pkHandl = null;
	private boolean emptyList= false;
	private int numberOfTraverses=0;
	private final String remoteFilesLocation = "/var/log/";
	private final String firstPcapGz= "lteL1.pcap.gz";
	private final String firstPcap= "lteL1.pcap";
	private final String secondPcapGz= "lteL1.prev1.pcap.gz";
	private final String secondPcap= "lteL1.prev1.pcap";
	private PeripheralsConfig peripheralsConfig;
	private final String localFileLocation = System.getProperty("user.dir");
	private EnodeBConfig enbConfig;
	private AttenuatorSet HO_attenuatorSetUnderTest;
	private AttenuatorSet clksource_attenuatorSetUnderTest;
	private String clksource_attenuatorSetName = "rudat_set2";
	private int HO_attenuatorMin;
	private int HO_attenuatorMax;
	private int clksource_attenuatorMin;
	private int clksource_attenuatorMax;
	private ArrayList<EnodeB> sourceEnodeBs = new ArrayList<EnodeB>();
	

	@Override
	public void init() throws Exception {
		enbInTest = new ArrayList<EnodeB>();
		enbInTest.add(dut);
		enbInTest.add(dut2);
		super.init();
		GeneralUtils.startLevel("Init");
		peripheralsConfig = PeripheralsConfig.getInstance();
		enbConfig = EnodeBConfig.getInstance();
		scpCli = new ScpClient(dut.getIpAddress(), dut.getUsername(), dut.getPassword());
		
		try {
			HO_attenuatorSetUnderTest = AttenuatorSet.getAttenuatorSet(CommonConstants.ATTENUATOR_SET_NAME);
			HO_attenuatorMin = HO_attenuatorSetUnderTest.getMinAttenuation();
			HO_attenuatorMax = HO_attenuatorSetUnderTest.getMaxAttenuation();
		} catch (Exception e) {
			report.report("Can't get first attenuator object from system");
			e.printStackTrace();
		}
		
		try {
			clksource_attenuatorSetUnderTest = AttenuatorSet.getAttenuatorSet(clksource_attenuatorSetName);
			clksource_attenuatorMin = clksource_attenuatorSetUnderTest.getMinAttenuation();
			clksource_attenuatorMax = clksource_attenuatorSetUnderTest.getMaxAttenuation();
		}catch(Exception d){
			report.report("Can't get Second attenuator object from system");
			d.printStackTrace();
		}
		
		GeneralUtils.stopAllLevels();
	}

	@Override
	public void end(){
		if(dut2 != null){
			peripheralsConfig.changeEnbState(dut2, EnbStates.IN_SERVICE);
		}
		super.end();
	}

	@ParameterProperties(description = "Name of Enodeb Which the test will be run On")
	public void setDUT(String dut) {
		this.dut = (EnodeB) SysObjUtils.getInstnce().initSystemObject(EnodeB.class, false, dut).get(0);
	}

	public String getDUT() {
		return this.dut.getNetspanName();
	}
	
	@ParameterProperties(description = "Name of Enodeb Which the test will be run On")
	public void setDUT2(String dut) {
		this.dut2 = (EnodeB) SysObjUtils.getInstnce().initSystemObject(EnodeB.class, false, dut).get(0);
	}

	public String getDUT2() {
		return this.dut2.getNetspanName();
	}

	@ParameterProperties(description = "enodeB1588")
	public void setenodeB1588(String enodeB1588) {
		this.enodeB1588 = (EnodeB) SysObjUtils.getInstnce().initSystemObject(EnodeB.class, false, enodeB1588).get(0);
	}

	public String getenodeB1588() {
		return this.enodeB1588.getNetspanName();
	}
	
	@ParameterProperties(description = "GPSEnodeB")
	public void setXlpEnodeB(String GPSEnodeB) {
		this.GPSEnodeB = (EnodeB) SysObjUtils.getInstnce().initSystemObject(EnodeB.class, false, GPSEnodeB).get(0);
	}

	public String getGPSEnodeB() {
		return this.GPSEnodeB.getNetspanName();
	}
	
	@Test
	@TestProperties(name = "NLM sync Quite Time", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void quiteTime() {
		if(dut2 != null){
			peripheralsConfig.changeEnbState(dut2, EnbStates.OUT_OF_SERVICE);
		}
		boolean isDUTVelocity = dut instanceof AirVelocity;
		if (!isDUTVelocity)
			report.report("node is not a fsm node!", Reporter.WARNING);

		report.report("checking debug flags ");
		String cliResult = dut.shell("/bs/lteCli -c \"db get debugFlags enableSniffer\"");
		report.reportHtml("db get debugFlags enableSniffer", cliResult, true);

		GeneralUtils.startLevel("Enable sniffer");
		enableSniffer();
		GeneralUtils.stopLevel();

		report.report("Waiting 10 seconds");
		GeneralUtils.unSafeSleep(10 * 1000);
		
		report.report("Sending command to CLI -> setnlmMuteScheduler with duration of 100 starting Sfn 100");
		cliResult = dut.shell(
				"/bs/lteCli -c \"simulate cell setnlmMuteScheduler cellIdx=0 duration=100 startSfn=100 startSubframe=0\"");
		report.reportHtml("simulate cell setnlmMuteScheduler", cliResult, true);

		report.report("Waiting for 1 minute");
		GeneralUtils.unSafeSleep(60 * 1000);
		
		GeneralUtils.startLevel("Disable Sniffer");
		dut.disableMACtoPHYcapture();
		GeneralUtils.stopLevel();

		GeneralUtils.startLevel("Checking a case there are pcap Files in local machine");
		deleteFilesBeforeGettingNewOnes();
		GeneralUtils.stopLevel();
		
		GeneralUtils.startLevel("get Pcap Files");
		if (!getPcapFileFromDUT()) {
			report.report("there are no pcap files!", Reporter.FAIL);
			report.report("Stoping Test");
			GeneralUtils.stopLevel();
			return;
		}
		
//      ------------Parsing Files------------
		ArrayList<ArrayList<PcapParams>> wrongPacketsPcap0 = new ArrayList<ArrayList<PcapParams>>();
		GeneralUtils.stopLevel();
		report.report("Parsing first Pcap File");
		//pcapFile0
		wrongPacketsPcap0.addAll(parsePcapFile(pcapFile0.getPath(),1));

		report.report("Parsing Second Pcap file");
		wrongPacketsPcap0.addAll(parsePcapFile(pcapFile1.getPath(),2));
		
//		------------Printing Results------------
		GeneralUtils.startLevel("Pcap Files Results:");
		printPcapFileResults(wrongPacketsPcap0);
		GeneralUtils.stopLevel();

		numberOfTraverses = wrongPacketsPcap0.size();
		
		if(emptyList){
			report.report("Found Quite Time in one the lists!");
			return;
		}
		
		if(pkHandl.getNumberOfTraverses() <= numberOfTraverses){
			if(!atMostOnePacketInList(wrongPacketsPcap0)){
				report.report("there were no quite time in the pcaps From Node", Reporter.FAIL);
				return;
			}
		}
		report.report("at least one of the groups meets the threshold criteria");
		
	}
	
	@Test
	@TestProperties(name = "CheckVelocityConnectToVelocity", returnParam = {"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void CheckVelocityConnectToVelocity() {
		//need to add check that dut and velocity1 is velocitys and xlpenodeB is XLP- TODO
		enbInTest.add(enodeB1588);
		enbInTest.add(GPSEnodeB);
		//setEnodeBOutOfService(XLPEnodeB);
		sourceEnodeBs.add(enodeB1588);
		createaAndSetNewSyncProfileWithNLM();
		
		
	}

	private void deleteFilesBeforeGettingNewOnes() {
		//deleting .gz files!
		boolean gzFileDeleted = deleteFileAndChecking(localFileLocation,firstPcapGz);
		boolean secondgzFileDeleted = deleteFileAndChecking(localFileLocation,secondPcapGz);
		
		if(gzFileDeleted && secondgzFileDeleted){
			GeneralUtils.printToConsole("Both Gz files were deleted!");
		}
		
		//deleting old .pcap files!
		gzFileDeleted = deleteFileAndChecking(localFileLocation,firstPcap);
		secondgzFileDeleted = deleteFileAndChecking(localFileLocation,secondPcap);
		
		if(gzFileDeleted && secondgzFileDeleted){
			GeneralUtils.printToConsole("Both pcap files were deleted!");
		}
	}

	private boolean deleteFileAndChecking(String fileLocation,String fileName){
		File temp = new File(fileLocation+"\\"+fileName);
		return GeneralUtils.deleteFileIfExists(temp.getPath());
	}

	private boolean atMostOnePacketInList(ArrayList<ArrayList<PcapParams>> wrongPacketsPcap0) {
		int i=1;
		boolean result = false;
		
		for(ArrayList<PcapParams> list : wrongPacketsPcap0){
			if(list.size() > 1){
				report.report("list "+i+" have more than 1 packet in it.");
			}else{
				result = true;
				report.report("list "+i+" have at most 1 packet in it.");
			}
			i++;
		}
		
		return result;
	}

	/**
	 * @author sshahaf active enable_phytrace.sh and enable_phytrace_tcpdump.sh
	 *         in /bs/bin folder. waiting 10 seconds after those.
	 */
	private void enableSniffer() {
		report.report("enabling MAC to PHY for node " + dut.getNetspanName());
		report.report("/bs/bin/enable_phytrace.sh");
		report.report("/bs/bin/enable_phytrace_tcpdump.sh");
		dut.enableMACtoPHYcapture(SnifferFileLocation.Local);
	}

	private void printPcapFileResults(ArrayList<ArrayList<PcapParams>> listOfWrongPacketsPcapFile) {
		for(ArrayList<PcapParams> listOfPackets : listOfWrongPacketsPcapFile){
			printListPackets(listOfPackets,listOfWrongPacketsPcapFile.indexOf(listOfPackets));
		}
	}

	private void printListPackets(ArrayList<PcapParams> listOfPackets,int listIndex) {
		String pcapFileNumbStr = "0";
		ArrayList<String> headLines = new ArrayList<String>();
		StreamList table = new StreamList();
		
		//get pcap file number
		if(listOfPackets.size() <= 1){
			emptyList = true;
			return;
		}
		pcapFileNumbStr = listOfPackets.get(0).getId();
		listOfPackets.remove(0);
		
		//set headers for table
		headLines.add("Packet Type");
		headLines.add("Frame Number");
		headLines.add("Sub Frame Number");

		for(PcapParams packet : listOfPackets){
			ArrayList<String> valuesArr = new ArrayList<String>();
			valuesArr.add(packet.getType());
			valuesArr.add(String.valueOf(packet.getFrame()));
			valuesArr.add(String.valueOf(packet.getSubFrame()));
			table.addValues("packet list "+pcapFileNumbStr+"."+listIndex, packet.getId(), headLines, valuesArr);
		}
		report.reportHtml("packet list "+pcapFileNumbStr+"."+listIndex, table.printTablesHtmlForStream("packet list "+pcapFileNumbStr+"."+listIndex), true);	
	}

	/**
	 * checking for a pcapFile if all of his packets should be. if there should
	 * not be - add packet number into a list and print what packets were wrong.
	 * 
	 * @param file
	 */
	private ArrayList<ArrayList<PcapParams>> parsePcapFile(String file,Integer pcapNum) {
		if (pcapP == null) {
			pcapP = new PcapParser();
		}
		pcapP.loadPcapFile(file);
		if(pkHandl == null){
			pkHandl = new pktHndl();
			pkHandl.init();
		}

		if(!pkHandl.isRunning()){
			pkHandl.init();
		}
		
		// Uses nextPacket method from class implemented PacketHandler.
		if(!pcapP.parse(pkHandl)){
			//exception or something
			report.report("Warning Check Files - might be corrupted file");
			if((pkHandl.getListOfListsPacketsResults().isEmpty())){
				pcapP = null;
				pkHandl = null;
				return new ArrayList<ArrayList<PcapParams>>();
			}
		}
		
		
		ArrayList<ArrayList<PcapParams>> listsOfPacketsResults = (ArrayList<ArrayList<PcapParams>>) pkHandl.getListOfListsPacketsResults().clone();
		ArrayList<ArrayList<PcapParams>> lists = new ArrayList<>();
		if(pkHandl.isRunning()){
			listsOfPacketsResults.remove(listsOfPacketsResults.size() - 1);
		}
		
		for(ArrayList<PcapParams> list : listsOfPacketsResults){
			ArrayList<PcapParams> temp = new ArrayList<PcapParams>();	
			PcapParams dumi = new PcapParams();
			dumi.setId(String.valueOf(pcapNum));
			temp.add(dumi);
			temp.addAll(list);
			lists.add(temp);
		}
		
		return lists;
	}

	/**
	 * trying to get 2 pcap files from node /home/tmp return true if at least
	 * one of the files are delivered.
	 */
	private boolean getPcapFileFromDUT() {
		try {
			report.report("trying to get Files from node : " + dut.getNetspanName());
			File fileDest = new File(localFileLocation);
			report.report("trying to get "+firstPcapGz+" and "+secondPcapGz+" files from "+remoteFilesLocation);
			
			//granting permissions
			String result = dut.shell("chmod 777 /var/log");
			GeneralUtils.printToConsole(result);
			
			getFilesWithSCPClient(fileDest);
			
			pcapFile0 = new File(fileDest + "/"+firstPcap);
			pcapFile1 = new File(fileDest + "/"+secondPcap);
			boolean pcap0Exists = pcapFile0.exists();
			boolean pcap1Exists = pcapFile1.exists();

			if (pcap0Exists){
				ReporterHelper.copyFileToReporterAndAddLink(report, pcapFile0, "First Pcap File");
			}else{
				report.report("First File Doesn't Exists",Reporter.WARNING);
			}
			
			if(pcap1Exists){
				ReporterHelper.copyFileToReporterAndAddLink(report, pcapFile1, "Second Pcap File");
			}else{
				report.report("Second File Doesn't Exists",Reporter.WARNING);
			}
			
			if(!pcap0Exists && !pcap1Exists){
				return false;
			}

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private void getFilesWithSCPClient(File fileDest) {
		try{
			scpCli.getFiles(fileDest.getPath(), remoteFilesLocation+firstPcapGz);
			report.report("managed to get First File from node");
		}catch(Exception f1Fail){
			f1Fail.printStackTrace();
			report.report("First File Does't Exists in node!");
		}
		try{
			scpCli.getFiles(fileDest.getPath(), remoteFilesLocation+secondPcapGz);
			report.report("managed to get Second File from node");
			
		}catch(Exception f2Fail){
			f2Fail.printStackTrace();
			report.report("Second File Does't Exists in node!");
		}
		
		File firstLocalGzFile = new File(fileDest.getPath()+"\\"+firstPcapGz);
		File secondLocalGzFile = new File(fileDest.getPath()+"\\"+secondPcapGz);
		
		GeneralUtils.extractGZFile(firstLocalGzFile, fileDest.getPath()+"\\"+firstPcap);
		GeneralUtils.extractGZFile(secondLocalGzFile, fileDest.getPath()+"\\"+secondPcap);
	}
	
	private void createaAndSetNewSyncProfileWithNLM(){
		GeneralUtils.startLevel("create and set new sync profile with NLM enabeled");
			SyncParameters syncParams = new SyncParameters();
			ArrayList<Integer> earfcnList = new ArrayList<Integer>();
			syncParams.setPrimaryClockSource(PrimaryClockSourceEnum.NLM.toString());
			syncParams.setProfileName(dut.getNetspanName() + "_NLM_" + GeneralUtils.getPrefixAutomation());
			syncParams.setNlmIntraFrequencyScanMode(true);
			syncParams.setNlmScanAllBands(false);
			
			for(EnodeB enb : sourceEnodeBs){
				earfcnList.add(enb.getEarfcn());
			}
			syncParams.setNlmIntraFrequencyScanList(earfcnList);
			if (!enbConfig.cloneAndSetSyncProfileViaNetSpan(dut, netspanServer.getCurrentSyncProfileName(dut), syncParams)){
				report.report("Coldn't create sync profile", Reporter.FAIL);
				return;
			}
				
			 
			EnodeBAdvancedParameters advParams = new EnodeBAdvancedParameters();
			advParams.setProfileName(netspanServer.getCurrentEnbAdvancedConfigurationProfileName(dut) + "_" + GeneralUtils.getPrefixAutomation() );
			if (sourceEnodeBs.size() > 1){
				int min = sourceEnodeBs.get(0).getPci();
				int max = sourceEnodeBs.get(0).getPci();
				int temp = sourceEnodeBs.get(1).getPci();
				if (temp < min) 
						min = temp;
				if (temp > max)
						max = temp;
			}
			else {
				advParams.setStartPCI(sourceEnodeBs.get(0).getPci());
				advParams.setStopPCI(sourceEnodeBs.get(0).getPci());
			}
			
			advParams.setRSRPTreshholdForNLSync(-120);
			enbConfig.cloneAndSetAdvancedProfileViaNetspan(dut, netspanServer.getCurrentEnbAdvancedConfigurationProfileName(dut), advParams);
			dut.reboot();
			dut.waitForAllRunning(EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
		GeneralUtils.stopLevel();
	}

	public class pktHndl implements PacketHandler {
		private int traversesOfQuiteTime = 0;
		private boolean record = false;
		private final int frameOffSet = 68;
		private final String FAPI_ERROR_IND = "09";
		private final String DL_CONFIG_REQ = "80";
		private final String UL_CONFIG_REQ = "81";
		private final String FAPI_SF_IND = "82";
		private final String DCIO_REQ = "83";
		private final String FAPI_RX_REQ = "84";
		private final String CRC_IND = "86";
		private final String UL_SCH_IND = "87";
		private final String no_name = "8A";
		private final String CQI_IND = "8B";
		private final String FAPI_VSM_MEAS = "8C";

		int packetNumber = 1;
		private final Integer pTypeOffSet = 64;
		ArrayList<ArrayList<PcapParams>> listOfListsPackets = new ArrayList<ArrayList<PcapParams>>();
		ArrayList<PcapParams> wrongPacketNumbers;
		HashMap<String, Integer> ulTypeOffSetMap = new HashMap<>();
		HashMap<String, Integer> dlTypeOffSet = new HashMap<>();

		public int getCounter() {
			return packetNumber;
		}

		public void init(){
			packetNumber = 1;
			listOfListsPackets = new ArrayList<>();
		}
		
		public boolean isRunning(){
			return record;
		}
		
		public pktHndl() {
			// init UL type offset map.
			ulTypeOffSetMap.put("00", 8);
			ulTypeOffSetMap.put("01", 8);
			ulTypeOffSetMap.put("02", 8);
			ulTypeOffSetMap.put("03", 8);
			ulTypeOffSetMap.put("04", 8);
			ulTypeOffSetMap.put("05", 8);
			ulTypeOffSetMap.put("06", 8);
			ulTypeOffSetMap.put("07", 8);
			ulTypeOffSetMap.put("08", 8);
			ulTypeOffSetMap.put("09", 8);
			ulTypeOffSetMap.put("0a", 8);
			ulTypeOffSetMap.put("0b", 8);
			ulTypeOffSetMap.put("0c", 6);
			ulTypeOffSetMap.put("0d", 8);
			ulTypeOffSetMap.put("0e", 8);
			ulTypeOffSetMap.put("0f", 8);

			// init DL type offset map.
			dlTypeOffSet.put("00", 5);
			dlTypeOffSet.put("01", 0);
			dlTypeOffSet.put("02", 6);
			dlTypeOffSet.put("03", 0);
			dlTypeOffSet.put("04", 6);
			dlTypeOffSet.put("05", 0);
			dlTypeOffSet.put("06", 0);
			dlTypeOffSet.put("80", 6);
		}

		public ArrayList<PcapParams> getPacketsResult() {
			return wrongPacketNumbers;
		}

		public ArrayList<ArrayList<PcapParams>> getListOfListsPacketsResults() {
			return listOfListsPackets;
		}

		public int getNumberOfTraverses(){
			return numberOfTraverses;
		}
		
		/**
		 * checking packet if it should be in the pcap file. if not -> adding
		 * packet to a wrongPacketsList object.
		 */
		public boolean nextPacket(Packet packet) throws IOException {
			Buffer s = packet.getPayload();
			byte[] data = s.getArray();
			PcapParams currPacket = new PcapParams();
			if (!checkFrameAndSubFrame(data,currPacket)) {
				packetNumber++;
				return true;
			}
			// printPacketDebug(data,packetNumber);
			if (!packetOffSetValidation(data, pTypeOffSet)) {
				GeneralUtils.printToConsole("packet number " + packetNumber + " caught an exeption in off set " + pTypeOffSet);
				return true;
			}
			String pType = String.format("%02X", data[pTypeOffSet]);
			GeneralUtils.printToConsole("\nPacket Type code value : " + pType);
			switch (pType.toUpperCase()) {

			case FAPI_ERROR_IND: // -> False
				GeneralUtils.printToConsole("packet " + packetNumber + ": FAPI_ERROR_IND packet");
				currPacket.setId(String.valueOf(packetNumber));
				currPacket.setType("FAPI_ERROR_IND");
				wrongPacketNumbers.add(currPacket);
				break;

			case DL_CONFIG_REQ: // -> handle
				GeneralUtils.printToConsole("packet " + packetNumber + ": FAPI_DL_CONFIG_REQ packet");
				if (!FAPI_DL_CONFIG_REQ(data)) {
					currPacket.setId(String.valueOf(packetNumber));
					currPacket.setType("DL_CONFIG_REQ");
					wrongPacketNumbers.add(currPacket);
				}
				break;

			case UL_CONFIG_REQ: // -> handle
				GeneralUtils.printToConsole("packet " + packetNumber + ": FAPI_UL_CONFIG_REQ packet");
				if (!FAPI_UL_CONFIG_REQ(data)) {
					currPacket.setId(String.valueOf(packetNumber));
					currPacket.setType("UL_CONFIG_REQ");
					wrongPacketNumbers.add(currPacket);
				}
				break;

			case FAPI_SF_IND: // -> true
				GeneralUtils.printToConsole("packet " + packetNumber + ": FAPI_SF_IND packet");
				break;

			case DCIO_REQ: // -> handle PDU 0 only
				GeneralUtils.printToConsole("packet " + packetNumber + ": FAPI_HI_DCIO_REQ packet");
				if (!pduZeroHandle(data)) {
					currPacket.setId(String.valueOf(packetNumber));
					currPacket.setType("DCIO_REQ");
					wrongPacketNumbers.add(currPacket);
				}
				break;

			case FAPI_RX_REQ: // -> true
				GeneralUtils.printToConsole("packet " + packetNumber + ": FAPI_RX_REQ packet");
				break;

			case CRC_IND: // -> handle PDU 0 only
				GeneralUtils.printToConsole("packet " + packetNumber + ": FAPI_CRC_IND packet");
				if (!pduZeroHandle(data)) {
					currPacket.setId(String.valueOf(packetNumber));
					currPacket.setType("CRC_IND");
					wrongPacketNumbers.add(currPacket);
				}
				break;

			case FAPI_VSM_MEAS: // -> true
				GeneralUtils.printToConsole("packet " + packetNumber + ": FAPI_VSM_MEAS packet");
				break;
				
			case UL_SCH_IND: // -> handle PDU 0 only
				GeneralUtils.printToConsole("packet " + packetNumber + ": FAPI_UL_SCH_IND packet");
				if (!pduZeroHandle(data)) {
					currPacket.setId(String.valueOf(packetNumber));
					currPacket.setType("UL_SCH_IND");
					wrongPacketNumbers.add(currPacket);
				}
				break;

			case no_name: // -> handle PDU 0 only
				GeneralUtils.printToConsole("packet " + packetNumber + ": FAPI packet");
				if (!pduZeroHandle(data)) {
					currPacket.setId(String.valueOf(packetNumber));
					currPacket.setType("Unknown");
					wrongPacketNumbers.add(currPacket);
				}
				break;

			case CQI_IND: // -> handle PDU 0 only
				GeneralUtils.printToConsole("packet " + packetNumber + ": FAPI_CQI_IND packet");
				if (!pduZeroHandle(data)) {
					currPacket.setId(String.valueOf(packetNumber));
					currPacket.setType("CQI_IND");
					wrongPacketNumbers.add(currPacket);
				}
				break;

			default:
				GeneralUtils.printToConsole("packet " + packetNumber + ": Unknown!!");
				currPacket.setId(String.valueOf(packetNumber));
				currPacket.setType("Unknown");
				wrongPacketNumbers.add(currPacket);
			}
			packetNumber++;
			return true;
		}

		/**
		 * checks if the packet is in between frame 100 and 110 return false if
		 * not. Default of Frame 100 and subframe 0.
		 * 
		 * @param data
		 * @return
		 */
		private boolean checkFrameAndSubFrame(byte[] data,PcapParams packetData) {
			String lastEightBit;
			String firstEightBit;
			try {
				// last = 32, first = 07. ->3207
				lastEightBit = String.format("%02X", data[frameOffSet]);
				firstEightBit = String.format("%02X", data[frameOffSet + 1]);
			} catch (Exception e) {
				e.printStackTrace();
				GeneralUtils.printToConsole("can't parse data in packet in places(16 bits): " + frameOffSet);
				return false;
			}
			// change 3207 into 0732 so 73 is the frame and 3 is the subframe
			// 73 in hexa = 113 frame.
			// 07 in hexa = 7 subframe.
			String frameSubFrame = firstEightBit + lastEightBit;
			String frameNumber = frameSubFrame.substring(0, 3);
			String subFrameNumber = "0" + frameSubFrame.substring(3);
			//updating packetData with frame and sub Frame
			packetData.setFrame(frameNumber);
			packetData.setSubFrame(subFrameNumber);
			
//			GeneralUtils.printToConsole("packet details: frame - "+frameNumber+", sub frame - "+subFrameNumber);
			if ((frameNumber.equals("064"))) {
				GeneralUtils.printToConsole("found packet 100 with 0 start recording");
				if (wrongPacketNumbers == null) {
					wrongPacketNumbers = new ArrayList<PcapParams>();
				}
				record = true;
			}

			if ((frameNumber.equals("06E"))) {
				GeneralUtils.printToConsole("found packet 110 with 0 end recording");
				if (wrongPacketNumbers != null) {
					ArrayList<PcapParams> temp = (ArrayList<PcapParams>) wrongPacketNumbers.clone();
					listOfListsPackets.add(temp);
					wrongPacketNumbers = null;
					traversesOfQuiteTime++;
				}
				record = false;
			}
			return record;
		}

		/**
		 * checking if the off set is valid in byte[] and print message if it
		 * isn't.
		 * 
		 * @param data
		 * @param pTypeOffSet2
		 * @return
		 */
		private boolean packetOffSetValidation(byte[] data, Integer offSet) {
			try {
				String offSetTry = String.format("%02X",data[offSet]);
				GeneralUtils.printToConsole("data in place " + offSet + " is " + offSetTry);
			} catch (Exception e) {
				GeneralUtils.printToConsole("Packet have no such data in the offset value : " + offSet);
				return false;
			}
			return true;
		}

		private boolean FAPI_UL_CONFIG_REQ(byte[] data) {
			int pduNumberOffSet = pTypeOffSet + 8;
			String firstPDUType = "";
			int firstPDUSize = 0;
			int pduTypeOffSet = 0;

			if (!packetOffSetValidation(data, pduNumberOffSet)) {
				GeneralUtils.printToConsole("packet number " + packetNumber + " caught an exeption in off set " + pduNumberOffSet);
				return true;
			}
			Integer totalPDU = Integer.valueOf(data[pduNumberOffSet]);
			GeneralUtils.printToConsole("Num Of Pdus in Packet : " + totalPDU);
			if (totalPDU == 0)
				return true;

			firstPDUType = String.format("%02x", data[75]);
			GeneralUtils.printToConsole("Pdu Type is : " + firstPDUType);

			firstPDUSize = Integer.valueOf(data[75 + 1]);
			GeneralUtils.printToConsole("Pdu size in place : " + firstPDUSize);

			Integer pduRNTIOffSet = 0;
			String rnti1 = "";
			String rnti2 = "";
			String pduType = firstPDUType;
			pduTypeOffSet = 75;
			Integer pduSize = firstPDUSize;
			for (int i = 0; i < totalPDU; i++) {
				if (i != 0) {
					pduTypeOffSet += pduSize;
					pduType = String.format("%02x", data[pduTypeOffSet]);
					pduSize = Integer.valueOf(data[pduTypeOffSet + 1]);
				}
				
				//get pdu size from map and validate the result
				pduRNTIOffSet = dlTypeOffSet.get(pduType);
				if(pduRNTIOffSet == null){
					GeneralUtils.printToConsole("Pdu Type: "+pduType+", is not in System Map Object!");
					return false;
				}
				
				// Skip RNTI
				if (pduRNTIOffSet != 0) { 
					rnti1 = String.format("%02x", data[pduRNTIOffSet + pduTypeOffSet]);
					rnti2 = String.format("%02x", data[pduRNTIOffSet + pduTypeOffSet + 1]);

					rnti1 = rnti2 + rnti1;
					if (!(rnti1.equals("ffff")) && !(rnti1.equals("fffe"))) {
						GeneralUtils.printToConsole("rnti: " + rnti1 + ", is not ffff or fffe");
						return false;
					}
				}

			}
			return true;
		}

		private boolean FAPI_DL_CONFIG_REQ(byte[] data) {
			int pduNumberOffSet = pTypeOffSet + 10;
			String firstPDUType = "";
			int firstPDUSize = 0;
			int pduTypeOffSet = 0;

			if (!packetOffSetValidation(data, pduNumberOffSet)) {
				GeneralUtils.printToConsole("packet number " + packetNumber + " caught an exeption in off set " + pduNumberOffSet);
				return true;
			}
			Integer numOfPDUs = Integer.valueOf(data[pduNumberOffSet]);
			Integer numOfPDUs2 = Integer.valueOf(data[pduNumberOffSet + 1]);
			Integer totalPDU = numOfPDUs + numOfPDUs2;
			GeneralUtils.printToConsole("Num Of Pdus in Packet : " + totalPDU);
			if (totalPDU == 0)
				return true;

			firstPDUType = String.format("%02x", data[79]);
			GeneralUtils.printToConsole("Pdu Type is : " + firstPDUType);

			firstPDUSize = Integer.valueOf(data[79 + 1]);
			GeneralUtils.printToConsole("Pdu size in place : " + firstPDUSize);

			Integer pduRNTIOffSet = 0;
			String rnti1 = "";
			String rnti2 = "";
			String pduType = firstPDUType;
			pduTypeOffSet = 79;
			Integer pduSize = firstPDUSize;
			for (int i = 0; i < totalPDU; i++) {
				if (i != 0) {
					pduTypeOffSet += pduSize;
					pduType = String.format("%02x", data[pduTypeOffSet]);
					pduSize = Integer.valueOf(data[pduTypeOffSet + 1]);
				}
				
				pduRNTIOffSet = dlTypeOffSet.get(pduType);
				if(pduRNTIOffSet == null){
					GeneralUtils.printToConsole("Pdu Type: "+pduType+", is not in System Map Object!");
					return false;
				}
				
				if (pduRNTIOffSet != 0) { // Skip RNTI
					rnti1 = String.format("%02x", data[pduRNTIOffSet + pduTypeOffSet]);
					rnti2 = String.format("%02x", data[pduRNTIOffSet + pduTypeOffSet + 1]);

					rnti1 = rnti2 + rnti1;
					if (!(rnti1.equals("ffff")) && !(rnti1.equals("fffe"))) {
						GeneralUtils.printToConsole("rnti: " + rnti1 + ", is not ffff or fffe");
						return false;
					}
				}

			}
			return true;
		}

		private boolean pduZeroHandle(byte[] data) {
			String packetPdu = String.format("%02x", data[pTypeOffSet + 6]);
			return packetPdu.equals("00");
		}

		private void printPacketDebug(byte[] data, int packetNumber) {
			int byteInPacket = 0;
			System.out.println("--------------------------new Packet parsing----------------------------");
			System.out.println("Packet Number : " + packetNumber);
			for (byte Byte : data) {
				if (byteInPacket % 16 == 0)
					System.out.print("\n");
				else if (byteInPacket % 8 == 0)
					System.out.println(" ");
				System.out.print(String.format(" %02x", Byte));
				byteInPacket++;
			}

		}
		
	}
}

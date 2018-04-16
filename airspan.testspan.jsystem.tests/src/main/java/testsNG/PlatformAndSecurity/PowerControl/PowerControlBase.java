package testsNG.PlatformAndSecurity.PowerControl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import EnodeB.EnodeB;
import Netspan.NetspanServer;
import Netspan.API.Enums.EnbStates;
import Netspan.API.Lte.RFStatus;
import Netspan.Profiles.RadioParameters;
import UE.UE;
import Utils.CSVReader;
import Utils.GeneralUtils;
import Utils.SetupUtils;
import Utils.SysObjUtils;
import Utils.Snmp.MibReader;
import jsystem.framework.ParameterProperties;
import jsystem.framework.report.Reporter;
import testsNG.TestspanTest;
import testsNG.Actions.EnodeBConfig;
import testsNG.Actions.PeripheralsConfig;
import testsNG.Actions.Statuses;
import testsNG.Actions.Traffic;
import testsNG.Actions.Utils.ParallelCommandsThread;
import testsNG.Actions.Utils.TrafficGeneratorType;

/**
 * @author Meital Mizrachi
 */

public class PowerControlBase extends TestspanTest {

	public static float POWER_THRESHOLD = 0.5f;
	public static float VSWR_WARNING_LEVEL = 1.6f;
	public static float VSWR_LOWER_LIMIT = 1f;
	public static float VSWR_UPPER_LIMIT = 1.75f;
	protected EnodeB dut;
	protected EnodeBConfig enbConfig;
	protected Traffic traffic;
	protected PeripheralsConfig peripheralsConfig;
	protected Statuses status;
	protected NetspanServer netspanServer;
	protected MibReader mibReader;
	protected ArrayList<UE> staticUEs;
	protected List<String> commandList = new ArrayList<String>();
	ArrayList<String> danCommandList = new ArrayList<>();
	protected ParallelCommandsThread syncCommands;
	protected ParallelCommandsThread commandsThread = null;
	
	protected int readTxMaxPowerOfProductFromCsvFile(String productCode, String path) throws Exception {
		String[][] CSVmatrix = CSVReader.CSV2Matrix(path);
		for (int i = 0; i < CSVmatrix.length; i++) {
			if (productCode.equals(CSVmatrix[i][0])) {
				int txMaxPower = Integer.parseInt(CSVmatrix[i][1]) / 100;
				return txMaxPower;
			}
		}
		// The product does not exist in the CSV file, return ERROR VALUE
		return GeneralUtils.ERROR_VALUE;
	}

	protected boolean tryingConnectUEsAndSendTraffic(ArrayList<UE> ues) {
		boolean UesConnected = false;
		resetUes(ues);
		
		report.report("Trying to connect UEs to Enb");
		boolean status = peripheralsConfig.checkIfAllUEsAreConnectedToNode(ues, dut);
		if(!status){
			report.report("Failed to connect UEs. Reboot UEs and check again");
			peripheralsConfig.rebootUEs(ues);
			status = peripheralsConfig.checkIfAllUEsAreConnectedToNode(ues, dut);
		}
		String ChannelStatus = dut.getCellServiceState();
		if ( status && (ChannelStatus.equals("0"))) {
			report.report("Enb is Out-Of-Service but UEs succeed to connect", Reporter.WARNING);
			reason += "Enb is Out-Of-Service but UEs succeed to connect ";
			UesConnected = false;
		} else if ( !status && (ChannelStatus.equals("1"))) {
			report.report("Enb is In-Service but one of UEs wasn't connected", Reporter.WARNING);
			reason += "Enb is In-Service but one of UEs wasn't connected ";
			UesConnected = false;
		}  else if ( status && (ChannelStatus.equals("1"))) {
			report.report("Enb is In-Service and UEs succeed to connect ");
			UesConnected = true;
		} else if (!status && (ChannelStatus.equals("0"))){
			report.report("Enb is Out-of-Service and UEs are not connected ");
			UesConnected = false;
		}
		
		if(!( (ChannelStatus.equals("0")) || (ChannelStatus.equals("1")) )){
			report.report("snmp Error Value : "+ChannelStatus);
		}
		return UesConnected;
	}

	protected void resetUes(ArrayList<UE> ues) {
		peripheralsConfig.stopUEs(ues);
		peripheralsConfig.startUEs(ues);
	}

	protected boolean changeTxPowerTest(int txPower) throws IOException {
		boolean flag = true;
		if (staticUEs == null || staticUEs.isEmpty()) {
			report.report("No Static UEs defined in the Test", Reporter.WARNING);
		}
		GeneralUtils.startLevel("Change Tx Power to " + txPower + "dBm");
		RadioParameters radioParams = new RadioParameters();
		radioParams.setTxpower(txPower);
		flag = enbConfig.cloneAndSetRadioProfileViaNetSpan(dut, dut.getDefaultNetspanProfiles().getRadio(), radioParams);			
		GeneralUtils.stopLevel();
		
		if(!flag){
			report.report("Failed to set tx power via netspan radio profile.",Reporter.FAIL);
			reason += "Failed to set tx power via netspan radio profile.";
			return false;
		}

		report.report("Wait 2 minutes");
		GeneralUtils.unSafeSleep(60 * 1000 * 2);

		List<RFStatus> rfstatuses = status.getRFStatus(dut);
		for (RFStatus rfstat : rfstatuses) {
			if (rfstat.ActualTxPowerDbm == GeneralUtils.ERROR_VALUE) {
				report.report("Cannot measure VSWR on " + rfstat.RfNumber, Reporter.FAIL);
				reason += "Cannot measure VSWR on " + rfstat.RfNumber;
				flag = false;
				continue;
			}
			if (Math.abs(txPower - rfstat.ActualTxPowerDbm) > POWER_THRESHOLD) {
				report.report(rfstat.RfNumber + " - Tx Power is more then threshold(" + POWER_THRESHOLD
						+ "), Configured: " + txPower + ", Actual: " + rfstat.ActualTxPowerDbm, Reporter.FAIL);
				reason += rfstat.RfNumber + " - Tx Power is more then threshold(" + POWER_THRESHOLD + "), Configured: "
						+ txPower + ", Actual: " + rfstat.ActualTxPowerDbm + ". ";
				flag = false;
			} else
				report.report(rfstat.RfNumber + " - Configured Tx Power: " + txPower + ", Actual Tx Power: "
						+ rfstat.ActualTxPowerDbm + " (admissible error +-0.5 dBm)");
		}
		
		report.report("Starting traffic");
		traffic.startTraffic(staticUEs);
		
		boolean status = peripheralsConfig.checkIfAllUEsAreConnectedToNode(staticUEs, dut);
		if (!status) {
			report.report("One of the static UEs wasnt connected", Reporter.WARNING);
			for (UE ue : staticUEs){
				int rsrp0 = ue.getRSRP(0);
				int uePci = ue.getPCI();
				
				int enbPci= dut.getPci();
				
				report.report("UE RSRP0= " + rsrp0);
				if (uePci != enbPci) {
					report.report("UE connected PCI= " + uePci, Reporter.WARNING);
					report.report("UE sees diffrent enodeB.", Reporter.WARNING);
				}else{
					report.report("UE connected PCI= " + uePci);
				}
			}
		}else{
			GeneralUtils.unSafeSleep(5 * 1000);
			report.reportHtml("Ue show rate", dut.lteCli("Ue show rate"), true);
			report.reportHtml("ue show link", dut.lteCli("ue show link"), true);
		}
		
		EnbStates enbState = dut.getServiceState();
		if(enbState != EnbStates.IN_SERVICE)
		{
			report.report("operationalStatus: " + enbState + " instead of " + EnbStates.IN_SERVICE, Reporter.FAIL);
			reason += "operationalStatus: " + enbState + " instead of " + EnbStates.IN_SERVICE;
		} else {
			report.report("operationalStatus: " + enbState);
		}
		
		EnbStates channelStatus = dut.getServiceState();
		if(channelStatus != EnbStates.IN_SERVICE)
		{
			report.report("ChannelStatusInfo: " + channelStatus +" instead of " + EnbStates.IN_SERVICE, Reporter.FAIL);
			reason += "ChannelStatusInfo: " + channelStatus +" instead of " + EnbStates.IN_SERVICE;
		} else {
			report.report("ChannelStatusInfo: " + channelStatus);
		}
		
		report.report("Stopping traffic");
		traffic.stopTraffic();
		GeneralUtils.startLevel("Revert to default Radio Profile and delete cloned Profile");
		if (enbConfig.setEnbRadioProfile(dut, dut.defaultNetspanProfiles.getRadio(), false)) {
			report.report("Reverting to default Radio Profile");
		}
		report.report("Wait 2 seconds");
		GeneralUtils.unSafeSleep(2000);
		enbConfig.deleteClonedProfiles();
		GeneralUtils.stopLevel();

		return flag;
	}
	
	protected void syncGeneralCommands() {
		GeneralUtils.startLevel("Starting parallel commands");
		
		report.report("db get rfStatus");
		report.report("db get cellCFg txpower");
		
		commandList.add("db get rfStatus");
		commandList.add("db get cellCFg txpower");
		commandList.add("db get PTPStatus");
		
		danCommandList.add("dancli HWM_Msr_Prnt 1");
		danCommandList.add("dancli HWM_Msr_Prnt 50");
		
		for (int numberOfTrys = 0; numberOfTrys < 2; numberOfTrys++) {
			try {
				syncCommands = new ParallelCommandsThread(commandList, dut, null, danCommandList);
				break;
			} catch (Exception e) {
				report.report("Cannot initialize streamers in ParallelCommandsThread Class, try number " + numberOfTrys);
				if (numberOfTrys == 2) {
					report.report("could not init Commands in parallel", Reporter.WARNING);
				}
			}
		}
		syncCommands.start();
		GeneralUtils.stopLevel();
	}

	@Override
	public void init() throws Exception {
		enbInTest = new ArrayList<EnodeB>();
		enbInTest.add(dut);
		super.init();
		
		peripheralsConfig = PeripheralsConfig.getInstance();
		enbConfig = EnodeBConfig.getInstance();
		status = Statuses.getInstance();
		traffic = Traffic.getInstance(SetupUtils.getInstance().getAllUEs());
		netspanServer = NetspanServer.getInstance();
		mibReader = MibReader.getInstance();

		
		for (EnodeB enodeB : enbInSetup) {
			if(enodeB != dut)
				PeripheralsConfig.getInstance().changeEnbState(enodeB, EnbStates.OUT_OF_SERVICE);
		}
		
		
		if (traffic.getGeneratorType() == TrafficGeneratorType.TestCenter) {
			String TrafficFile = traffic.getTg().getDefaultConfigTccFile();
			int lastDot = TrafficFile.lastIndexOf('.');
			String HOTrafficFile = TrafficFile.substring(0, lastDot) + "_HO" + TrafficFile.substring(lastDot);
			traffic.configFile = new File(HOTrafficFile);
		}
		staticUEs = SetupUtils.getInstance().getStaticUEs(dut);
	}

	@Override
	public void end(){
		super.end();
	}

	@ParameterProperties(description = "Name of Enb")
	public void setDUT(String dut) {
		ArrayList<EnodeB> temp = (ArrayList<EnodeB>) SysObjUtils.getInstnce().initSystemObject(EnodeB.class, false, dut);
		this.dut = temp.get(0);
	}
}
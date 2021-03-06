package Action.EnodebAction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.time.DateUtils;
import org.junit.Test;

import EnodeB.EnodeB;
import EnodeB.EnodeB.Architecture;
import EnodeB.EnodeBWithDAN;
import EnodeB.Components.DAN;
import Netspan.EnbProfiles;
import Netspan.NetspanServer;
import Netspan.API.Enums.CellBarringPolicies;
import Netspan.API.Enums.CsgModes;
import Netspan.API.Enums.DuplexType;
import Netspan.API.Enums.EnabledStates;
import Netspan.API.Enums.EnbStates;
import Netspan.API.Enums.NodeManagementModes;
import Netspan.API.Enums.PrimaryClockSourceEnum;
import Netspan.API.Lte.AlarmInfo;
import Netspan.API.Lte.EnbCellProperties;
import Netspan.API.Lte.EventInfo;
import Netspan.API.Lte.LteBackhaul;
import Netspan.NBI_16_5.Netspan_16_5_abilities;
import Utils.DebugFtpServer;
import Utils.GeneralUtils;
import Utils.GeneralUtils.RebootTypesNetspan;
import Utils.GeneralUtils.RelayScanType;
import Utils.GeneralUtils.CellToUse;
import Utils.Snmp.MibReader;
import Utils.SysObjUtils;
import jsystem.framework.ParameterProperties;
import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;
import jsystem.framework.scenario.Parameter;
import testsNG.Actions.AlarmsAndEvents;
import testsNG.Actions.EnodeBConfig;
import testsNG.Actions.PeripheralsConfig;

public class Enodeb extends EnodebAction {

	public static final String VERIFY_COUNTER_VALUE_METHOD_NAME = "verifyCounterValue";
	public static final String VERIFY_DISCOVERED_PROPERTIES_METHOD_NAME = "verifyDiscoveredProperties";

	public enum TargetShell {
		CLI, MAIN_SHELL, DAN
	}

	public enum SNMPVariableType {
		INT, STRING
	}

	public enum CounterType {
		Free_Text, Counter_List
	}

	public enum Counter {
		HoX2IntraFreqInCompSuccRnlRadioRsn, HoX2InterFreqInCompSuccRnlRadioRsn, HoX2InterFreqInPrepSuccRnlRadioRsn, HoS1InterFreqInAttRnlRadioRsn, HoX2IntraFreqInAttRnlRadioRsn, HoX2InterFreqInAttRnlRadioRsn, HoS1IntraFreqInCompSuccRnlRadioRsn, HoS1IntraFreqInPrepSuccRnlRadioRsn, HoS1InterFreqInCompSuccRnlRadioRsn, HoS1InterFreqInPrepSuccRnlRadioRsn
	}

	public enum BackhaulEthernetName {
		ETH, ETH1, ETH2, OPT, OPT1, OPT2, SFP,
	}

	public enum BackhaulEthernetParameter {
		Ethernet_Duplex, Ethernet_Rate, Port_Type, Auto_Neg_Configuration, Port_Status, Port_Speed, Flow_Control_Status
	}

	public enum EthernetRate {
		Unknown("Unknown"), EthernetRate_10Mb("10 Mb"), EthernetRate_100Mb("1000 Mb"), EthernetRate_1000Mb("1000 Mb");
		private final String value;

		EthernetRate(String v) {
			value = v;
		}

		public String value() {
			return value;
		}
	}

	public enum PortType {
		Copper, CopperPOE, SFP, Fiber
	}

	public enum PortStatus {
		Up("Up"), Down("Down"), Port_Disabled("Port Disabled"), Port_Unplugged("Port Unplugged");
		private final String value;

		PortStatus(String v) {
			value = v;
		}

		public String value() {
			return value;
		}
	}

	public enum PortSpeed {
		Unknown("Unknown"), PortSpeed_10Mb("10 Mb"), PortSpeed_100Mb("1000 Mb"), PortSpeed_1000Mb("1000 Mb");
		private final String value;

		PortSpeed(String v) {
			value = v;
		}

		public String value() {
			return value;
		}
	}

	public enum verifyAlarmAction{
		Equals, Contains;
	}
	
	protected EnodeB dut;
	protected Integer cellId = 1;
	private String timeToWait = "00:00:00";
	private String pingIP = "20.20.2.254";
	private Integer triesToPing = 0;
	private String debugCommands = "ue show rate";
	private String parallelCommands = "";
	private String responseTimeout = "10";
	private String waitBetweenCommands = "0";
	private EnbStates serviceState = EnbStates.IN_SERVICE;
	private TargetShell targetShell = TargetShell.CLI;
	private String valueToSet = "";
	private String expectedValue = "";
	private String OID = "";
	private SNMPVariableType SNMPVarType = SNMPVariableType.INT;
	private boolean Answer;
	private String counter = "";
	private Comparison comparison;
	private EnbProfiles enbProfile;
	private String profileName;
	private String radioProfileName;

	private NodeManagementModes managedMode;
	private boolean enableCell;
	private String physicalCellId;
	private String PRACHRootSequenceIndex;
	private String trackingAreaCode;
	private String emergencyAreaId;
	private String PRACHFrequencyOffset;
	private CellBarringPolicies cellBarringPolicy;
	protected CsgModes closedSubscriberGroupMode;
	private RelayScanType scanType = RelayScanType.Scan;

	private static Map<String,Date> startDate;
	private final int eventStartDeafult = 20;
	private ArrayList<String> listOfAlarms;
	private ArrayList<String> listOfEvents;
	private ArrayList<Integer> emergencyAreaIds;
	private verifyAlarmAction verifyAction = verifyAlarmAction.Equals;
	private CellToUse cellToUse = CellToUse.MULTI_CELL;

	public CellToUse getCellToUse() {
		return cellToUse;
	}

	public void setCellToUse(CellToUse cellToUse) {
		this.cellToUse = cellToUse;
	}

	public synchronized RelayScanType getScanType() {
		return scanType;
	}

	public synchronized void setScanType(RelayScanType scanType) {
		this.scanType = scanType;
	}

	
	
	// Backhaul and Ethernet
	protected BackhaulEthernetName backhaulEthernetName;
	protected BackhaulEthernetParameter backhaulEthernetParameter;
	protected DuplexType ethernetDuplex;
	protected EthernetRate ethernetRate;
	protected PortType portType;
	protected EnabledStates autoNegConfig;
	protected PortStatus portStatus;
	protected PortSpeed portSpeed;
	protected EnabledStates flowControlStatus;

	private boolean performReboot = true;
	private ArrayList<EnodeB> duts;
	private int granularityPeriod = GeneralUtils.ERROR_VALUE;

	public void setGranularityPeriod(String granularityPeriod) {
		this.granularityPeriod = Integer.valueOf(granularityPeriod);
	}

	public NodeManagementModes isManaged() {
		return managedMode;
	}

	@ParameterProperties(description = "Perform reboot to apply changes.")
	public void setPerformReboot(boolean performReboot) {
		this.performReboot = performReboot;
	}

	@ParameterProperties(description = "Name of All Enb comma seperated e.g enb1,enb2")
	public void setDUTs(String dut) {
		this.duts = (ArrayList<EnodeB>) SysObjUtils.getInstnce().initSystemObject(EnodeB.class, false, dut.split(","));
	}

	@ParameterProperties(description = "Set EnodeB to Managed or Unmanaged")
	public void setManaged(NodeManagementModes managed) {
		this.managedMode = managed;
	}

	@ParameterProperties(description = "Set cell enable value")
	public void setEnableCell(boolean enableCell) {
		this.enableCell = enableCell;
	}

	@ParameterProperties(description = "Set PCI value")
	public void setPhysicalCellId(String physicalCellId) {
		this.physicalCellId = physicalCellId;
	}

	@ParameterProperties(description = "Set RSI value")
	public void setPRACHRootSequenceIndex(String pRACHRootSequenceIndex) {
		PRACHRootSequenceIndex = pRACHRootSequenceIndex;
	}

	@ParameterProperties(description = "Set PRACH Frequency Offset")
	public void setPRACHFrequencyOffset(String pRACHFrequencyOffset) {
		PRACHFrequencyOffset = pRACHFrequencyOffset;
	}

	@ParameterProperties(description = "Set Cell Barring Policy")
	public void setCellBarringPolicy(CellBarringPolicies cellBarringPolicy) {
		this.cellBarringPolicy = cellBarringPolicy;
	}

	@ParameterProperties(description = "Set Closed Subscriber Group Mode")
	public void setClosedSubscriberGroupMode(CsgModes closedSubscriberGroupMode) {
		this.closedSubscriberGroupMode = closedSubscriberGroupMode;
	}

	@ParameterProperties(description = "Name of ENB from the SUT")
	public void setDUT(String dut) {
		ArrayList<EnodeB> temp = (ArrayList<EnodeB>) SysObjUtils.getInstnce().initSystemObject(EnodeB.class, false,
				dut.split(","));
		this.dut = temp.get(0);
	}

	@ParameterProperties(description = "Cell ID (Type: int, default=1)")
	public void setCellId(String cellId) {
		try {
			this.cellId = Integer.valueOf(cellId);
		} catch (Exception exc) {
			report.report("Failed to load CellId due to: " + exc.getMessage(), Reporter.FAIL);
		}
	}

	@ParameterProperties(description = "Run time in format HH:MM:SS")
	public void setTimeToWait(String timeToWait) {
		this.timeToWait = timeToWait;
	}

	@ParameterProperties(description = "IP")
	public void setIP(String pingIp) {
		this.pingIP = pingIp;
	}

	@ParameterProperties(description = "Tries (Type: int)")
	public void setPingTries(String pingTries) {
		this.triesToPing = Integer.valueOf(pingTries);
	}

	@ParameterProperties(description = "Debug Commands List (Split by ;)")
	public void setDebugCommands(String debugCommands) {
		this.debugCommands = debugCommands;
	}

	@ParameterProperties(description = "Parallel Commands List (Split by ;)")
	public void setParallelCommands(String parallelCommands) {
		this.parallelCommands = parallelCommands;
	}

	@ParameterProperties(description = "Set Service State")
	public void setServiceState(EnbStates serviceState) {
		this.serviceState = serviceState;
	}

	@ParameterProperties(description = "Where to send the commands")
	public void setTargetShell(TargetShell targetShell) {
		this.targetShell = targetShell;
	}

	@ParameterProperties(description = "Set Value")
	public void setValueToSet(String valueToSet) {
		this.valueToSet = valueToSet;
	}

	@ParameterProperties(description = "SNMP MIB OID (e.g: 1.3.6.1.4.1.989.1.20.1.6.2.1.1)")
	public void setOID(String oID) {
		OID = oID;
	}

	@ParameterProperties(description = "SNMP MIB OID")
	public void setSNMPVarType(SNMPVariableType sNMPVarType) {
		SNMPVarType = sNMPVarType;
	}

	@ParameterProperties(description = "Expected Value")
	public void setExpectedValue(String expectedValue) {
		this.expectedValue = expectedValue;
	}

	@ParameterProperties(description = "Comparison")
	public void setComparison(Comparison comp) {
		comparison = comp;
	}

	@ParameterProperties(description = "Free Text Counter")
	public void setFreeTextCounter(String freeTextCounter) {
		this.counter = freeTextCounter;
	}

	@ParameterProperties(description = "Known Counter")
	public void setKnownCounter(Counter knownCounter) {
		this.counter = knownCounter.toString();
	}

	public synchronized final boolean isAnswer() {
		return this.Answer;
	}

	public synchronized final void setAnswer(boolean answer) {
		this.Answer = answer;
	}

	@ParameterProperties(description = "Choose how to read the counter")
	public final void setCounterType(CounterType counterType) {
	}

	@ParameterProperties(description = "Enb Profile")
	public void setEnbProfile(EnbProfiles enbProfile) {
		this.enbProfile = enbProfile;
	}

	@ParameterProperties(description = "Profile Name")
	public void setProfileName(String profileName) {
		this.profileName = profileName;
	}

	@ParameterProperties(description = "Radio Profile Name")
	public void setRadioProfileName(String radioProfileName) {
		this.radioProfileName = radioProfileName;
	}

	@ParameterProperties(description = "Backhaul and Ethernet Name")
	public void setBackhaulEthernetName(BackhaulEthernetName backhaulEthernetName) {
		this.backhaulEthernetName = backhaulEthernetName;
	}

	@ParameterProperties(description = "Backhaul and Ethernet Parameter")
	public void setBackhaulEthernetParameter(BackhaulEthernetParameter parameter) {
		this.backhaulEthernetParameter = parameter;
	}

	@ParameterProperties(description = "Ethernet Duplex")
	public void setEthernetDuplex(DuplexType ethernetDuplex) {
		this.ethernetDuplex = ethernetDuplex;
	}

	@ParameterProperties(description = "Ethernet Rate")
	public void setEthernetRate(EthernetRate ethernetRate) {
		this.ethernetRate = ethernetRate;
	}

	@ParameterProperties(description = "Port Type")
	public void setPortType(PortType portType) {
		this.portType = portType;
	}

	@ParameterProperties(description = "Auto-Neg Configuration")
	public void setAutoNegConfig(EnabledStates autoNegConfig) {
		this.autoNegConfig = autoNegConfig;
	}

	@ParameterProperties(description = "Port Status")
	public void setPortStatus(PortStatus portStatus) {
		this.portStatus = portStatus;
	}

	@ParameterProperties(description = "Port Speed")
	public void setPortSpeed(PortSpeed portSpeed) {
		this.portSpeed = portSpeed;
	}

	@ParameterProperties(description = "Flow Control Status")
	public void setFlowControlStatus(EnabledStates flowControlStatus) {
		this.flowControlStatus = flowControlStatus;
	}

	/**
	 * Setter to the parameter responseTimeout - the max timeout it waits until
	 * getting the response. [Seconds]
	 *
	 * @param responseTimeout
	 *            - responseTimeout
	 */
	@ParameterProperties(description = "Response Timeout [Sec]")
	public void setResponseTimeout(String responseTimeout) {
		this.responseTimeout = responseTimeout;
	}

	/**
	 * Setter to the parameter waitBetweenCommands - the time between commands.
	 * [Seconds]
	 *
	 * @param waitBetweenCommands
	 *            - waitBetweenCommands
	 */
	@ParameterProperties(description = "Wait Between Commands Time Interval [Sec]")
	public void setWaitBetweenCommands(String waitBetweenCommands) {
		this.waitBetweenCommands = waitBetweenCommands;
	}

	/**
	 * This action will wait for all running state. Prerequisite: Reboot State.
	 * (The action should start when the EnodeB is already in Reboot State).
	 * <p>
	 * DUT - Name of the DUTs from the SUT TimeToWait - the max timeout interval
	 * to wait, until the test ends. While this time, the EnodeB is being
	 * sampled repeatedly, in order to check its state.
	 */
	@Test // 1
	@TestProperties(name = "Wait For All Running And In Service", returnParam = "LastStatus", paramsInclude = { "DUT",
			"timeToWait" })
	public void waitForAllRunningAndInService() {
		long timeOutInMillisecond = setRunTimeToMilliSeconds(timeToWait);
		report.report(
				dut.getName() + " Wait For All Running And In Service " + timeOutInMillisecond / 1000 + "  seconds.");
		if (!this.dut.waitForAllRunningAndInService(timeOutInMillisecond)) {
			report.report("Wait For All Running And In Service Failed", Reporter.FAIL);
			reason = "Wait For All Running And In Service Failed";
		} else {
			report.report("Wait For All Running And In Service Succeeded");
		}
	}

	private RebootTypesNetspan netspanRebootType = RebootTypesNetspan.Reset_Node;

	public RebootTypesNetspan getNetspanRebootType() {
		return netspanRebootType;
	}

	public void setNetspanRebootType(RebootTypesNetspan netspanRebootType) {
		this.netspanRebootType = netspanRebootType;
	}

	@Test // 2
	@TestProperties(name = "Reboot EnodeB", returnParam = "LastStatus", paramsInclude = { "DUT", "NetspanRebootType" })
	public void reboot() {
		report.report("Reboot EnodeB: " + this.dut.getName());
		report.report("Reboot type: " + netspanRebootType);

		if (!this.dut.rebootViaNetspan(netspanRebootType)) {
			report.report("Reboot Failed", Reporter.FAIL);
			reason = "Reboot Failed";
		} else {
			report.report("Reboot Succeeded");
		}
	}
	
	@Test // 3
	@TestProperties(name = "Reset Counters", returnParam = "LastStatus", paramsInclude = { "DUT" })
	public void resetCounters() {
		report.report(this.dut.getName() + " Reset Counters");

		boolean flag = this.dut.resetCounter(null, null, null);

		if (!flag) {
			report.report("Reset Counters Failed", Reporter.FAIL);
			reason = "Reset Counters Failed";
		} else {
			report.report("Reset Counters Succeeded");
		}
	}

	@Test // 4
	@TestProperties(name = "Ping", returnParam = "LastStatus", paramsInclude = { "DUT", "IP", "PingTries" })
	public void ping() {
		report.report("Ping from " + dut.getName() + " to IP=" + this.pingIP + " Times=" + this.triesToPing);
		String str = this.dut.ping(this.pingIP, this.triesToPing);

		GeneralUtils.startLevel("Response");
		for (String s : str.split("\n")) {
			report.report(s);
		}
		GeneralUtils.stopLevel();

		if (GeneralUtils.checkPingResponse(str)) {
			report.report("Ping to IP " + this.pingIP + " Succeeded");
		} else {
			report.report("Ping to IP " + this.pingIP + " Failed", Reporter.FAIL);
			reason = "Ping to IP " + this.pingIP + " Failed";
		}
	}

	@Test // 5 //add FAIL case
	@TestProperties(name = "Send Debug Commands", returnParam = "LastStatus", paramsInclude = { "DUT", "TargetShell",
			"DebugCommands" })
	public void debugCommand() {
		report.report("Debug Commands to send:");
		for (String cmd : this.debugCommands.split(",")) {
			report.report(cmd);
		}

		report.report("Debug Commands results:");
		for (String cmd : this.debugCommands.split(",")) {
			String ans = "";
			switch (this.targetShell) {
			case CLI:
				ans = dut.lteCli(cmd);
				break;
			case DAN:
				if (this.dut instanceof EnodeBWithDAN && dut.hasDan()) {
					for (int i = 0; i < ((EnodeBWithDAN) dut).getDanArrlength(); i++) {
						ans += "\n-------------";
						ans += "      DAN " + i;
						ans += "\n-------------";
						ans += ((EnodeBWithDAN) dut).sendDanCommands(DAN.SHELL_PROMPT, cmd, "", i);
						ans += "\n" + i;
					}
				}
				break;
			case MAIN_SHELL:
				ans = dut.shell(cmd);
				break;
			}
			report.reportHtml(cmd, ans, true);
		}
	}

	@Test // 6
	@TestProperties(name = "Set Service State", returnParam = "LastStatus", paramsInclude = { "DUT", "ServiceState" })
	public void setServiceState() {
		report.report("Set Service State = " + this.serviceState);
		boolean flag = PeripheralsConfig.getInstance().changeEnbState(this.dut, this.serviceState);

		if (!flag) {
			report.report("Set Service State Failed", Reporter.FAIL);
			reason = "Set Service State Failed";
		} else {
			report.report("Set Service State Succeeded");
		}
	}

	@Test // 7
	@TestProperties(name = "Set SNMP MIB", returnParam = "LastStatus", paramsInclude = { "DUT", "OID", "ValueToSet",
			"SNMPVarType" })
	public void setSNMP() {
		report.report("Set SNMP - OID:" + this.OID + " value:" + this.valueToSet);
		boolean answer = false;
		switch (this.SNMPVarType) {
		case INT:
			int val;
			try {
				val = Integer.parseInt(this.valueToSet);
			} catch (Exception e) {
				report.report("Cant convert value:" + this.valueToSet + " to Int", Reporter.FAIL);
				reason = "Cant convert value:" + this.valueToSet + " to Int";
				return;
			}
			try {
				answer = this.dut.snmpSet(this.OID, val);
			} catch (IOException e) {
				report.report("SNMP fail for OID:" + this.OID + " Value:" + val, Reporter.FAIL);
				reason = "SNMP fail for OID:" + this.OID + " Value:" + val;
				return;
			}

			break;
		case STRING:
			try {
				this.dut.snmpSet(this.OID, this.valueToSet);
			} catch (IOException e) {
				report.report("SNMP fail for OID:" + this.OID + " Value:" + this.valueToSet, Reporter.FAIL);
				reason = "SNMP fail for OID:" + this.OID + " Value:" + this.valueToSet;
				return;
			}
			break;
		}
		if (answer) {
			report.report("SNMP Set succeded for OID:" + this.OID + " Value:" + this.valueToSet);
		} else {
			report.report("SNMP Set failed for OID:" + this.OID + " Value:" + this.valueToSet, Reporter.FAIL);
			reason = "SNMP Set failed for OID:" + this.OID + " Value:" + this.valueToSet;
		}
	}

	@Test // 8
	@TestProperties(name = "Verify SNMP MIB", returnParam = "LastStatus", paramsInclude = { "DUT", "OID",
			"ExpectedValue", "Comparison" })
	public void verifyMibSNMP() {
		String value = getMibSNMP(this.dut, this.OID);
		report.report("SNMP OID: " + this.OID + ", Current Value: " + value);
		comparison(this.comparison, value.toLowerCase(), this.expectedValue.toLowerCase());
	}

	@Test // 9
	@TestProperties(name = "Is it XLP", returnParam = { "LastStatus", "Answer" }, paramsInclude = { "DUT" })
	public void isXLPnode() {
		if (this.dut.getArchitecture() == Architecture.XLP) {
			setAnswer(true);
			report.report(this.dut.getName() + " is XLP");
		} else {
			setAnswer(false);
			report.report(this.dut.getName() + " is NOT XLP");

		}
	}

	@Test // 10
	@TestProperties(name = "Is Clock source it 1588", returnParam = { "LastStatus", "Answer" }, paramsInclude = {
			"DUT" })
	public void isClockSourceIt1588() {
		PrimaryClockSourceEnum clockSource;
		try {
			clockSource = NetspanServer.getInstance().getPrimaryClockSource(this.dut);
		} catch (Exception e) {
			report.report("Is Clock source it 1588 Test failed dut to: " + e.getMessage(), Reporter.FAIL);
			reason = "Is Clock source it 1588 Test failed dut to: " + e.getMessage();
			e.printStackTrace();
			return;
		}

		if (clockSource == PrimaryClockSourceEnum.IEEE_1588) {
			setAnswer(true);
			report.report("Clock source is 1588");
		} else {
			setAnswer(false);
			report.report("Clock source is NOT 1588");
		}
	}

	@Test // 11
	@TestProperties(name = "Verify Counter Value", returnParam = { "LastStatus" }, paramsInclude = { "DUT",
			"ExpectedValue", "Comparison", "CounterType", "FreeTextCounter", "KnownCounter" })
	public void verifyCounterValue() {
		int value = this.dut.getCountersValue(this.counter);
		report.report("Counter: " + this.counter + ", Current Value: " + value);
		comparison(this.comparison, String.valueOf(value), this.expectedValue);
	}

	@Test // 12
	@TestProperties(name = "Set Profile", returnParam = { "LastStatus" }, paramsInclude = { "DUT", "CellId",
			"EnbProfile", "ProfileName" })
	public void setProfile() {
		report.report(this.dut.getName() + "(Cell " + this.cellId + ") Set " + this.enbProfile + ", Profile Name: "
				+ this.profileName);
		boolean flag = EnodeBConfig.getInstance().setProfile(this.dut, this.cellId, this.enbProfile, this.profileName);

		if (!flag) {
			report.report("Failed to set profile " + profileName + " to enodeb " + dut.getNetspanName(), Reporter.FAIL);
			reason = "Failed to set profile " + profileName + " to enodeb " + dut.getNetspanName();
		} else {
			report.report("Succeeded to set profile " + profileName + " to enodeb " + dut.getNetspanName());
		}
	}

	@Test
	@TestProperties(name = "Set Cell Properties", returnParam = { "LastStatus" }, paramsInclude = { "DUT", "CellId",
			"EnableCell", "CellIdentity", "PhysicalLaterCellGroup", "PhysicalLayerIdentity", "PhysicalCellId",
			"PRACHRootSequenceIndex", "TrackingAreaCode", "EmergencyAreaId", "PRACHFrequencyOffset",
			"ClosedSubscriberGroupMode", "CellBarringPolicy" })
	public void setCellProperties() {
		int pciValue = 0, rsiValue = 0, trackingCodeValue = 0, emergencyAreaValue = 0, PRACHFrequencyOffsetValue = 0;
		GeneralUtils.startLevel("Setting properties for " + dut.getName() + " Cell " + cellId);
		report.report("EnableCell: " + enableCell);

		if (physicalCellId != null) {
			pciValue = Integer.parseInt(physicalCellId);
			report.report("PhysicalCellId: " + pciValue);
		}
		if (PRACHRootSequenceIndex != null) {
			rsiValue = Integer.parseInt(PRACHRootSequenceIndex);
			report.report("PRACHRootSequenceIndex: " + rsiValue);
		}
		if (trackingAreaCode != null) {
			trackingCodeValue = Integer.parseInt(trackingAreaCode);
			report.report("TrackingAreaCode: " + trackingCodeValue);
		}
		if (emergencyAreaId != null) {
			emergencyAreaValue = Integer.parseInt(emergencyAreaId);
			report.report("EmergencyAreaId: " + emergencyAreaValue);
		}
		if (PRACHFrequencyOffset != null) {
			PRACHFrequencyOffsetValue = Integer.parseInt(PRACHFrequencyOffset);
			report.report("PRACHFrequencyOffset: " + PRACHFrequencyOffsetValue);
		}
		if (closedSubscriberGroupMode != null)
			report.report("CloserSubscriberGroupMode: " + closedSubscriberGroupMode);
		if (cellBarringPolicy != null)
			report.report("CellBarringPolicy: " + cellBarringPolicy);
		GeneralUtils.stopLevel();
		dut.setCellContextNumber(cellId);
		EnbCellProperties enbCellProperties = new EnbCellProperties();
		enbCellProperties.isEnabled = enableCell;
		if (physicalCellId != null)
			enbCellProperties.physicalCellId = pciValue;
		if (PRACHRootSequenceIndex != null)
			enbCellProperties.prachRsi = rsiValue;
		if (trackingAreaCode != null)
			enbCellProperties.trackingAreaCode = trackingCodeValue;
		if (emergencyAreaId != null)
			enbCellProperties.emergencyAreaId = emergencyAreaValue;
		if (PRACHFrequencyOffset != null)
			enbCellProperties.prachFreqOffset = PRACHFrequencyOffsetValue;
		if (closedSubscriberGroupMode != null)
			enbCellProperties.closedSubscriberGroupMode = closedSubscriberGroupMode;
		if (cellBarringPolicy != null)
			enbCellProperties.cellBarringPolicy = cellBarringPolicy;

		NetspanServer netspan;
		try {
			GeneralUtils.startLevel(dut.getName() + " Cell " + cellId + " properties from netspan");
			netspan = NetspanServer.getInstance();
			boolean flag = netspan.setEnbCellProperties(dut, enbCellProperties);
			if (!flag) {
				report.report("Set cell properties Failed", Reporter.FAIL);
				reason = "Set cell properties Failed";
				return;
			} else
				report.report("Set cell properties Passed");

			EnbCellProperties enbCellProperties1 = netspan.getEnbCellProperties(dut);
			report.report("Enabled status from netspan: " + (enbCellProperties1.isEnabled ? "enabled" : "disabled"));
			if (enbCellProperties1.isEnabled != enableCell) {
				report.report("Enabled status not as expected!", Reporter.FAIL);
				reason = "Enabled status not as expected!";
				flag = false;
			}

			report.report("PCI value from netspan: " + enbCellProperties1.physicalCellId);
			if (physicalCellId != null) {
				if (enbCellProperties1.physicalCellId != pciValue) {
					report.report("PCI value not as expected. Expected: "+pciValue+", Actual: "+enbCellProperties1.physicalCellId, Reporter.FAIL);
					reason += "PCI value not as expected. Expected: "+pciValue+", Actual: "+enbCellProperties1.physicalCellId;
					flag = false;
				}
			}
			report.report("RSI value from netspan: " + enbCellProperties1.prachRsi);
			if (PRACHRootSequenceIndex != null) {
				if (enbCellProperties1.prachRsi != rsiValue) {
					report.report("RSI value not as expected. Expected: "+rsiValue+", Actual: "+enbCellProperties1.prachRsi, Reporter.FAIL);
					reason += "RSI value not as expected. Expected: "+rsiValue+", Actual: "+enbCellProperties1.prachRsi;
					flag = false;
				}
			}
			report.report("TrackingAreaCode value from netspan: " + enbCellProperties1.trackingAreaCode);
			if (trackingAreaCode != null) {
				if (enbCellProperties1.trackingAreaCode != trackingCodeValue) {
					report.report("TrackingAreaCode value not as expected. Expected: "+trackingCodeValue+", Actual: "+enbCellProperties1.trackingAreaCode, Reporter.FAIL);
					reason += "TrackingAreaCode value not as expected. Expected: "+trackingCodeValue+", Actual: "+enbCellProperties1.trackingAreaCode;
					flag = false;
				}
			}
			report.report("emergencyAreaId value from netspan: " + enbCellProperties1.emergencyAreaId);
			if (emergencyAreaId != null) {
				if (enbCellProperties1.emergencyAreaId != emergencyAreaValue) {
					report.report("emergencyAreaId value not as expected. Expected: "+emergencyAreaValue+", Actual: "+enbCellProperties1.emergencyAreaId, Reporter.FAIL);
					reason += "emergencyAreaId value not as expected. Expected: "+emergencyAreaValue+", Actual: "+enbCellProperties1.emergencyAreaId;
					flag = false;
				}
			}
			report.report("prachFreqOffset value from netspan: " + enbCellProperties1.prachFreqOffset);
			if (PRACHFrequencyOffset != null) {
				if (enbCellProperties1.prachFreqOffset != PRACHFrequencyOffsetValue) {
					report.report("prachFreqOffset value not as expected. Expected: "+PRACHFrequencyOffsetValue+", Actual: "+enbCellProperties1.prachFreqOffset, Reporter.FAIL);
					reason += "prachFreqOffset value not as expected. Expected: "+PRACHFrequencyOffsetValue+", Actual: "+enbCellProperties1.prachFreqOffset;
					flag = false;
				}
			}

			report.report(
					"closedSubscriberGroupMode value from netspan: " + enbCellProperties1.closedSubscriberGroupMode);
			if (closedSubscriberGroupMode != null) {
				if (enbCellProperties1.closedSubscriberGroupMode != closedSubscriberGroupMode) {
					report.report("closedSubscriberGroupMode value not as expected. Expected: "+closedSubscriberGroupMode.toString()+", Actual: "+enbCellProperties1.closedSubscriberGroupMode.toString(), Reporter.FAIL);
					reason += "closedSubscriberGroupMode value not as expected. Expected: "+closedSubscriberGroupMode.toString()+", Actual: "+enbCellProperties1.closedSubscriberGroupMode.toString();
					flag = false;
				}
			}

			report.report("cellBarringPolicy value from netspan: " + enbCellProperties1.cellBarringPolicy);
			if (cellBarringPolicy != null) {
				if (enbCellProperties1.cellBarringPolicy != cellBarringPolicy) {
					report.report("cellBarringPolicy value not as expected. Expected: "+cellBarringPolicy.toString()+", Actual: "+enbCellProperties1.cellBarringPolicy.toString(), Reporter.FAIL);
					reason += "cellBarringPolicy value not as expected. Expected: "+cellBarringPolicy.toString()+", Actual: "+enbCellProperties1.cellBarringPolicy.toString();
					flag = false;
				}
			}
			GeneralUtils.stopLevel();

		} catch (Exception e) {
			report.report("Could not get netspan! Set cell properties failed", Reporter.FAIL);
			reason = "Could not get netspan! Set cell properties failed";
		}
	}

	@Test
	@TestProperties(name = "Move to PNP list", returnParam = { "IsTestWasSuccessful" }, paramsInclude = { "DUT" })
	public void moveToPNPList() {
		EnodeBConfig enodeBConfig = EnodeBConfig.getInstance();
		report.report("Trying to move " + dut.getNetspanName() + " to PnP list");

		if (!enodeBConfig.convertToPnPConfig(dut)) {
			report.report("Failed to set EnodeB as Pnp", Reporter.FAIL);
			reason = "Failed to set EnodeB as Pnp";
		} else {
			report.report("Configuration done");
		}
	}
	
	@Test
	@TestProperties(name = "Relay Scan", returnParam = { "IsTestWasSuccessful" }, paramsInclude = { "DUT","ScanType" })
	public void relayScan() {
		EnodeBConfig enodeBConfig = EnodeBConfig.getInstance();
		dut.expecteInServiceState = false;
		dut.setExpectBooting(true);
		report.report("Trying to relay scan with scan type: "+scanType);
		if (!enodeBConfig.relayScan(dut, scanType)) {
			report.report("Relay failed to scan", Reporter.FAIL);
			reason = "Relay failed to scan";
			dut.expecteInServiceState = true;
			dut.setExpectBooting(false);
		} else {
			report.report("Scan done");
			GeneralUtils.unSafeSleep(60*1000);
			report.report("Wait for enodeb to reach in service state");
			if(!dut.waitForAllRunningAndInService(EnodeB.WAIT_FOR_ALL_RUNNING_TIME)){
				report.report("EnodeB failed to reach in service state", Reporter.WARNING);
			}else{
				report.report("EnodeB reached in service state");
			}
		}
	}
	

	/**
	 * Start Parallel Commands
	 * <p>
	 * DUT - Name of the DUTs from the SUT ParallelCommands - Commands
	 * responseTimeout - response Timeout interval in Seconds
	 * waitBetweenCommands - wait Between Commands in Seconds
	 */
	@Test
	@TestProperties(name = "Start Parallel Commands", returnParam = { "IsTestWasSuccessful" }, paramsInclude = { "DUT",
			"ParallelCommands", "responseTimeout", "waitBetweenCommands" })
	public void startParallelCommands() {
		report.report("Starting parallel commands: " + this.parallelCommands);
		List<String> commandList = GeneralUtils.convertStringToArrayList(this.parallelCommands, ";");
		boolean flag = true;
		try {

			startingParallelCommands(this.dut, commandList, GeneralUtils.parseInt(responseTimeout, 10),
					GeneralUtils.parseInt(waitBetweenCommands, 0));
		} catch (IOException e) {
			e.printStackTrace();
			report.report("Start parallel commands failed due to: " + e.getMessage(), Reporter.FAIL);
			reason = "Start parallel commands failed due to: " + e.getMessage();
			flag = false;
		}
		if (flag) {
			report.report("Start parallel commands done");
		}
	}

	@Test
	@TestProperties(name = "Stop Parallel Commands", returnParam = { "IsTestWasSuccessful" }, paramsInclude = {})
	public void stopParallelCommands() {
		report.report("Stopping parallel commands");
		boolean flag = stoppingParallelCommands();
		if (flag) {
			report.report("Stop parallel commands done");
		} else {
			report.report("Stop parallel commands failed", Reporter.FAIL);
			reason = "Stop parallel commands failed";
		}
	}

	@Test
	@TestProperties(name = "Verify Discovered Properties", returnParam = { "IsTestWasSuccessful" }, paramsInclude = {
			"DUT", "BackhaulEthernetName", "BackhaulEthernetParameter", "EthernetDuplex", "EthernetRate", "PortType",
			"AutoNegConfig", "PortStatus", "PortSpeed", "FlowControlStatus" })
	public void verifyDiscoveredProperties() {
		List<LteBackhaul> lteBackhauls = EnodeBConfig.getInstance().getBackhaulInterfaceStatus(this.dut);
		boolean nameFlag = false;
		boolean testResponseFlag = false;

		if (lteBackhauls == null || this.backhaulEthernetName == null || this.backhaulEthernetParameter == null) {
			report.report("Verify " + this.backhaulEthernetName + ": " + this.backhaulEthernetParameter, Reporter.FAIL);
			reason = "Verify " + this.backhaulEthernetName + ": " + this.backhaulEthernetParameter;
			return;
		} else {
			report.report("Verify " + this.backhaulEthernetName + ": " + this.backhaulEthernetParameter);
		}

		String parameterName = this.backhaulEthernetName + " " + this.backhaulEthernetParameter;
		for (LteBackhaul lteBackhaul : lteBackhauls) {
			if (lteBackhaul.name.equals(this.backhaulEthernetName.toString())) {
				nameFlag = true;
				switch (this.backhaulEthernetParameter) {
				case Ethernet_Duplex:
					testResponseFlag = reportCompareBetweenTwoStrings(parameterName, lteBackhaul.ethernetDuplex.value(),
							this.ethernetDuplex.value());
					break;
				case Ethernet_Rate:
					testResponseFlag = reportCompareBetweenTwoStrings(parameterName, lteBackhaul.ethernetRate,
							this.ethernetRate.value());
					break;
				case Port_Type:
					testResponseFlag = reportCompareBetweenTwoStrings(parameterName, lteBackhaul.portType,
							this.portType.toString());
					break;
				case Auto_Neg_Configuration:
					testResponseFlag = reportCompareBetweenTwoStrings(parameterName, lteBackhaul.autoNegConfig.value(),
							this.autoNegConfig.value());
					break;
				case Port_Status:
					testResponseFlag = reportCompareBetweenTwoStrings(parameterName, lteBackhaul.portStatus,
							this.portStatus.value());
					break;
				case Port_Speed:
					testResponseFlag = reportCompareBetweenTwoStrings(parameterName, lteBackhaul.portSpeed,
							this.portSpeed.value());
					break;
				case Flow_Control_Status:
					testResponseFlag = reportCompareBetweenTwoStrings(parameterName,
							lteBackhaul.flowControlStatus.value(), this.flowControlStatus.value());
					break;
				}
			}
		}
		if (!nameFlag) {
			report.report(this.backhaulEthernetName + " does not exist", Reporter.FAIL);
		}
		setAnswer(testResponseFlag);
	}

	@Test
	@TestProperties(name = "Set Managed Mode for EnodeB", returnParam = { "LastStatus", "Answer" }, paramsInclude = {
			"DUT", "managed" })
	public void setManaged() throws Exception {
		boolean isModeAsExpected = NetspanServer.getInstance().getManagedMode(dut).equals(managedMode);
		if (isModeAsExpected) {
			setAnswer(true);
			report.report(this.dut.getName() + " is in " + managedMode + " State Already");
		} else {
			report.report(this.dut.getName() + " is not in " + managedMode + " mode. trying to set it via Netspan");
			if (NetspanServer.getInstance().setManagedMode(dut.getNetspanName(), managedMode)) {
				setAnswer(true);
				report.report("Succeedded to set " + this.dut.getName() + " mode to " + managedMode);
			} else {
				setAnswer(false);
				report.report("Failed to set " + this.dut.getName() + " mode to " + managedMode, Reporter.FAIL);
				reason = "Failed to set " + this.dut.getName() + " mode to " + managedMode;
			}
		}
	}

	@Test
	@TestProperties(name = "Re-Provision Node", returnParam = { "IsTestWasSuccessful" }, paramsInclude = { "DUTs",
			"performReboot" })
	public void reProvision() throws Exception {
		boolean status;
		NetspanServer netspan;
		netspan = NetspanServer.getInstance();
		for (EnodeB dut : duts) {
			report.report(String.format("%s - Trying to perform Re-Provision", dut.getNetspanName()));
			status = netspan.performReProvision(dut.getNetspanName());
			if (!status) {
				report.report("Re-Provision Failed", Reporter.FAIL);
				reason = "Re-Provision Failed";
				return;
			} else
				report.step(String.format("%s - Succeeded to perform Re-Provision", dut.getNetspanName()));
		}
		if (performReboot) {
			status = rebootAndWaitForAllrunning();
			if (status) {
				report.report("Enodeb Reached all running and in service successfully");
			}
		}
	}
	
	@Test
	@TestProperties(name = "Set Emergency Area Ids", returnParam = "LastStatus", paramsInclude = { "DUT", "EmergencyAreaIds", "CellId" })
	public void setEmergencyAreaIds() throws Exception {
		NetspanServer netspan = NetspanServer.getInstance();
		report.report("Set Emergency Area Ids to cell: "+cellId);
		report.report("Trying to set area ids: "+emergencyAreaIds.toString());
		dut.setCellContextNumber(cellId);
		boolean flag = ((Netspan_16_5_abilities) netspan).setEmergencyAreaIds(dut, emergencyAreaIds);
		if (!flag) {
			report.report("Set Emergency Area Ids Failed", Reporter.FAIL);
			reason = "Set Emergency Area Ids Failed";
		} else {
			report.report("Set Emergency Area Ids Succeeded");
		}
		dut.setCellContextNumber(1);
	}

	private boolean rebootAndWaitForAllrunning() {
		int inserviceTime = 0;
		boolean status = false;
		try {
			for (EnodeB dut : duts) {

				report.startLevel("Rebooting " + dut.getName());
				status = dut.reboot();
				if (!status) {
					report.report("Reboot Failed", Reporter.FAIL);
					reason = "Reboot Failed";
					report.stopLevel();
					return false;
				}
				report.stopLevel();
			}

			GeneralUtils.unSafeSleep(30 * 1000);

			for (EnodeB dut : duts) {
				status = dut.waitForAllRunningAndInService(EnodeB.WAIT_FOR_ALL_RUNNING_TIME);
				inserviceTime = (int) (EnodeB.WAIT_FOR_ALL_RUNNING_TIME / 1000 / 60);
				if (!status)
					report.report("Enodeb didnt reach all running and in service during " + inserviceTime + " minutes",
							Reporter.WARNING);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return status;
	}

	@Override
	public void init() {
		if (enbInTest == null) {
			enbInTest = new ArrayList<EnodeB>();
		}
		if (dut != null) {
			enbInTest.add(dut);
		}
		if (duts != null) {
			enbInTest.addAll(duts);
		}
		super.init();
	}

	@Override
	public void handleUIEvent(HashMap<String, Parameter> map, String methodName) throws Exception {

		if (methodName.equals(VERIFY_COUNTER_VALUE_METHOD_NAME)) {
			handleUIEventGetCounterValue(map, methodName);
		} else if (methodName.equals(VERIFY_DISCOVERED_PROPERTIES_METHOD_NAME)) {
			handleUIEventGetDiscoveredProperties(map, methodName);
		}
	}

	private void handleUIEventGetCounterValue(HashMap<String, Parameter> map, String methodName) {
		Parameter counterType = map.get("CounterType");
		Parameter freeTextCounter = map.get("FreeTextCounter");
		Parameter knownCounter = map.get("KnownCounter");

		freeTextCounter.setVisible(false);
		knownCounter.setVisible(false);

		if (counterType.getValue().equals(CounterType.Free_Text.toString())) {
			freeTextCounter.setVisible(true);
		} else {
			knownCounter.setVisible(true);
		}
	}

	private void handleUIEventGetDiscoveredProperties(HashMap<String, Parameter> map, String methodName) {
		map.get("EthernetDuplex").setVisible(false);
		map.get("EthernetRate").setVisible(false);
		map.get("PortType").setVisible(false);
		map.get("AutoNegConfig").setVisible(false);
		map.get("PortStatus").setVisible(false);
		map.get("PortSpeed").setVisible(false);
		map.get("FlowControlStatus").setVisible(false);

		Parameter backhaulEthernetParameter = map.get("BackhaulEthernetParameter");

		if (backhaulEthernetParameter != null && backhaulEthernetParameter.getValue() != null) {
			switch (BackhaulEthernetParameter.valueOf(backhaulEthernetParameter.getValue().toString())) {
			case Ethernet_Duplex:
				map.get("EthernetDuplex").setVisible(true);
				break;
			case Ethernet_Rate:
				map.get("EthernetRate").setVisible(true);
				break;
			case Port_Type:
				map.get("PortType").setVisible(true);
				break;
			case Auto_Neg_Configuration:
				map.get("AutoNegConfig").setVisible(true);
				break;
			case Port_Status:
				map.get("PortStatus").setVisible(true);
				break;
			case Port_Speed:
				map.get("PortSpeed").setVisible(true);
				break;
			case Flow_Control_Status:
				map.get("FlowControlStatus").setVisible(true);
				break;
			default:
				break;
			}
		}
	}

	@Test
	@TestProperties(name = "Change Granulatiry Period", returnParam = { "IsTestWasSuccessful" }, paramsInclude = {
			"DUT", "GranularityPeriod" })
	public void changeGranulatiryPeriod() {
		if (granularityPeriod == GeneralUtils.ERROR_VALUE) {
			report.report("No granularity period was configured", Reporter.FAIL);
			reason = "No granularity period was configured";
			return;
		}
		if (granularityPeriod <= 0) {
			report.report("Can't set granularity period of 0 or less", Reporter.FAIL);
			reason = "Can't set granularity period of 0 or less";
			return;
		}
		boolean setGranularity = EnodeBConfig.getInstance().setGranularityPeriod(dut, granularityPeriod);
		if (!setGranularity) {
			report.report("Failed to set granularity period", Reporter.FAIL);
			reason = "Failed to set granularity period";
		}
	}

	@Test
	@TestProperties(name = "Wait Granulatiry Period", returnParam = { "IsTestWasSuccessful" }, paramsInclude = {
			"DUT" })
	public void waitGranulatiryPeriod() {
		EnodeBConfig.getInstance().waitGranularityPeriodTime(dut);
	}

	private Integer setRunTimeToMilliSeconds(String runTime) {
		Integer result;
		Pattern p = Pattern.compile("(\\d+):(\\d+):(\\d+)");
		Matcher m = p.matcher(runTime);
		if (m.find()) {
			int hours = Integer.valueOf(m.group(1)) * 60 * 60;
			int minutes = Integer.valueOf(m.group(2)) * 60;
			int seconds = Integer.valueOf(m.group(3));
			result = hours + minutes + seconds;
			result = result * 1000;
		} else {
			result = null;
		}
		return result;
	}
	
	private String serialCommand;
	private String expectedOutput;
	private int commandTimeout  = 1;
	private boolean lteCliRequired = false;
	
	@ParameterProperties(description = "Commmand to be send via serial")
	public void setSerialCommand(String serialCommand) {
		this.serialCommand = serialCommand;
	}

	public String getSerialCommand() {
		return serialCommand;
	}

	@ParameterProperties(description = "Expected Command output; If exists recieved output will be evaluated for matching")
	public void setExpectedOutput(String expectedOutput) {
		this.expectedOutput = expectedOutput;
	}

	public String getExpectedOutput() {
		return this.expectedOutput;
	}

	@ParameterProperties(description = "Command Timeout (In minutes; Default: 1)")
	public void setCommandTimeout(int commandTimeout) {
		this.commandTimeout = commandTimeout * 1000;
	}
	
	public int getCommandTimeout() {
		return commandTimeout;
	}
	
	@ParameterProperties(description = "True - if command require lteCli shell")
	public void setLteCliRequired(boolean lteCliRequired) {
		this.lteCliRequired = lteCliRequired;
	}
	
	public boolean getLteCliRequired() {
		return lteCliRequired;
	}

//	@Test
//    @TestProperties(name = "sendCommandToSerial"
//    					, returnParam = { "IsTestWasSuccessful" }
//    					, paramsInclude = { "DUT", "SerialCommand", "ExpectedOutput" , "LteCliRequired", "CommandTimeout"})
//    public void sendCommandToSerial() throws Exception {
//    			
//    	String command = this.getSerialCommand();
//    	String output = this.getExpectedOutput();
//    	int timeout = this.getCommandTimeout();
//    	Session session = this.dut.getSerialSession();
//    	
//    	GeneralUtils.startLevel("Start keyword sent command: '" + command + "'");
//    	
//    	try {
//    		File temp_cache = File.createTempFile("temp_log_cache", ".log");
//    		
//    		session.setTerminalPrintStream(new PrintStream(temp_cache));
//    		session.sendCommands(cmd, response);
//    		
//    	}finally {
//			GeneralUtils.stopAllLevels();
//		}
    	
//    	if(!session.isConnected()) {
//    		if(!session.connectSession()) {
//    			report.report("Can't connect to serial host " + dut.getName(), Reporter.FAIL);
//                return;
//    		}
//    		else
//    			report.report("Serial host " + dut.getName() + " connected", Reporter.PASS);
//    	}
//    	else {
//    		report.report("Connected to serial host " + dut.getName(), Reporter.PASS);
//    	}
//    	
//    	if(!session.isLoggedSession()) {
//    		if(!session.loginSerial()) {
//    			report.report("Can't login to serial host " + dut.getName(), Reporter.FAIL);
//                return;
//    		}
//    		else
//    			report.report("LoggedIn to serial host " + dut.getName(), Reporter.PASS);
//    		
//    		if(this.getLteCliRequired()) {
//        		if(session.isShouldStayInCli()) {
//        			if(!session.sendCommands("", "lte_cli:>>"))
//					{
//        				report.report(dut.getName() + " is out of lte_cli, trying to enter.");
//						if(session.sendCommands("/bs/lteCli", "lte_cli:>>"))
//						{
//							report.report("update log level from stay in cli");
//							
////							session.updateLogLevel();
//						}
//						else
//						{
//							GeneralUtils.printToConsole(dut.getName() + " is still out of lte_cli, disconnecting session.");
//							session.disconnectSession();
//							report.report(dut.getName() + " is still out of lte_cli, disconnecting session", Reporter.FAIL);
//			                return;
//						}
//					}
//        		}
//        	}
//    		else {
//    			if(session.isShouldStayInCli()) {
//    				session.setShouldStayInCli(false);
//    				report.report("Disable CLI to serial host " + dut.getName(), Reporter.PASS);
//    			}
//    		}
//    	}
//    	else {
//    		report.report("In CLI to serial host " + dut.getName(), Reporter.PASS);
//    	}
//    	
//    	int status = Reporter.PASS;
//    	String text_status = "";
//    	GeneralUtils.startLevel("Sent command: '" + command + "'; Timeout: " + timeout + "ms");
//    	session.getCliBuffer();
//    	String response_text = session.sendCommands(EnodeBComponent.SHELL_PROMPT, command, null, timeout);
//    	GeneralUtils.stopLevel();
//    	
//    	if(output != null) {
//    		if(response_text.indexOf(output) > 0) {
//    			status = Reporter.PASS;
//    			text_status = "PASS";
//    		}
//    		else {
//    			status = Reporter.FAIL;
//    			text_status = "FAIL";
//    		}
//    		report.report("Real output " + (status == Reporter.PASS ? "not" : "") + "match expected pattern", status);
//    	}
////    	report.report("Command output: -------------------\n" +  response_text + "\n----------------------");
//    	GeneralUtils.reportHtmlLink("Command output:", response_text);
//    	GeneralUtils.stopLevel();
////    	return status == Reporter.PASS;
//    }

	
	
	@ParameterProperties(description = "Equals: search for exact event. Contains: check if your string is in event info string")
	public void setVerifyAction(verifyAlarmAction verifyAction) {
		this.verifyAction = verifyAction;
	}
	
	@ParameterProperties(description = "List of alarms type ids, separated by ;")
	public void setListOfAlarms(String listOfAlarms) {
		this.listOfAlarms = new ArrayList<String>(Arrays.asList(listOfAlarms.split(";")));
	}

	@ParameterProperties(description = "List of events info, separated by #")
	public void setListOfEvents(String listOfEvents) {
		this.listOfEvents = new ArrayList<String>(Arrays.asList(listOfEvents.split("#")));
	}
	
	@ParameterProperties(description = "List of EmergencyAreaIds, separated by \",\"")
	public void setEmergencyAreaIds(String emergencyAreaIds) {
		this.emergencyAreaIds = new ArrayList<>();
		for(String s : emergencyAreaIds.split(",")) this.emergencyAreaIds.add(Integer.valueOf(s));
	}

	@Test
	@TestProperties(name = "Start Collect Alarms", returnParam = "LastStatus", paramsInclude = { "DUT" })
	public void startCollectAlarms() {
		if(isDutNull()){
			return;
		}
		AlarmsAndEvents alarmsAndEvents = AlarmsAndEvents.getInstance();
		alarmsAndEvents.deleteAllAlarmsNode(dut);
	}
	
	@Test
	@TestProperties(name = "Start Collect Events", returnParam = "LastStatus", paramsInclude = { "DUT" })
	public void startCollectEvents() {
		if(isDutNull()){
			return;
		}
		if(startDate == null){
			startDate = new HashMap<String,Date>();
		}
		Date dateFrom = new Date();
		startDate.put(dut.getNetspanName(), dateFrom);
		GeneralUtils.printToConsole("List of start times:");
		GeneralUtils.printToConsole(startDate.toString());
	}
	
	@Test
	@TestProperties(name = "Get All Alarms", returnParam = "LastStatus", paramsInclude = { "DUT" })
	public void getAlarms() {
		if(isDutNull()){
			return;
		}
		printAlarmsInfo(dut);
	}
	
	@Test
	@TestProperties(name = "Get All Events", returnParam = "LastStatus", paramsInclude = { "DUT" })
	public void getEvents() {
		if(isDutNull()){
			return;
		}
		Date from = setStartTime();
		Date to = new Date();
		report.report("Getting events from: "+from.toString()+" to: "+to.toString());
		printEventsInfo(dut,from,to);
	}
	
	@Test
	@TestProperties(name = "Verify Alarms", returnParam = "LastStatus", paramsInclude = { "DUT","ListOfAlarms" })
	public void verifyAlarms() {
		if(isDutNull()){
			return;
		}
		if(listOfAlarms.isEmpty()){
			report.report("No alarms were set to verify", Reporter.FAIL);
			reason = "No alarms were set to verify";
			return;
		}
		verifyAlarmsHelper();
	}
	
	@Test
	@TestProperties(name = "Verify Events", returnParam = "LastStatus", paramsInclude = { "DUT",
			"ListOfEvents","VerifyAction" })
	public void verifyEvents() {
		if(isDutNull()){
			return;
		}
		if(listOfEvents.isEmpty()){
			report.report("No events were set to verify", Reporter.FAIL);
			reason = "No events were set to verify";
			return;
		}
		if(verifyAction == verifyAlarmAction.Equals){
			report.report("Searching for exact event info");
		}else{
			report.report("Searching for containing event info");
		}
		verifyEventsHelper();
	}
	
	private boolean isDutNull(){
		if(dut == null){
			report.report("No dut in test", Reporter.FAIL);
			reason = "No dut in test";
			return true;
		}
		return false;
	}
	
	private Date setStartTime(){
		Date from = null;
		if(startDate == null || (startDate != null && startDate.get(dut.getNetspanName()) == null)){
			report.report("Clear events was not used. Getting events of last "+eventStartDeafult+" minutes", Reporter.WARNING);
			from = new Date();
			from = DateUtils.addMinutes(from, -eventStartDeafult);
		}else{
			from = startDate.get(dut.getNetspanName());
		}
		return from;
	}
	
	private void printEventsInfo(EnodeB enodeb,Date from, Date to){
		AlarmsAndEvents alarmsAndEvents = AlarmsAndEvents.getInstance();
		List<EventInfo> eventsInfo = alarmsAndEvents.getEventsNodeByDateRange(enodeb, from, to);
		if (!eventsInfo.isEmpty()) {
            GeneralUtils.startLevel(enodeb.getName() + "'s Events: ");
            for (EventInfo eventInfo : eventsInfo) {
                alarmsAndEvents.printEventInfo(eventInfo);
            }
            GeneralUtils.stopLevel();
        } else {
            report.report(enodeb.getName() + "'s events list is empty");
        }
	}
	
	private void printAlarmsInfo(EnodeB eNodeB) {
		AlarmsAndEvents alarmsAndEvents = AlarmsAndEvents.getInstance();
        List<AlarmInfo> alarmsInfo = alarmsAndEvents.getAllAlarmsNode(eNodeB);
        if (!alarmsInfo.isEmpty()) {
            GeneralUtils.startLevel(eNodeB.getName() + "'s Alarms: ");
            for (AlarmInfo alarmInfo : alarmsInfo) {
                alarmsAndEvents.printAlarmInfo(alarmInfo);
            }
            GeneralUtils.stopLevel();
        } else {
            report.report(eNodeB.getName() + "'s Alarms list is empty");
        }
    }
	
	private void verifyAlarmsHelper(){
		AlarmsAndEvents alarmsAndEvents = AlarmsAndEvents.getInstance();
        List<AlarmInfo> alarmsInfo = alarmsAndEvents.getAllAlarmsNode(dut);
        for(String id : listOfAlarms){
        	int count = 0;
        	for(AlarmInfo alarm : alarmsInfo){
        		if(id.equals(alarm.alarmTypeId.toString())){
        			count++;
        		}
        	}
        	if(count == 0){
        		report.report("Alarm with type id ["+id+"] was not found", Reporter.FAIL);
        		reason += "Alarm with type id ["+id+"] was not found.<br> ";
        	}else{
        		report.report("Alarm with type id ["+id+"] was found ["+count+"] time"+(count==1?"":"s"), Reporter.PASS);
        	}
        }
	}
	
	private void verifyEventsHelper(){
		AlarmsAndEvents alarmsAndEvents = AlarmsAndEvents.getInstance();
        Date from = setStartTime();
        Date to = new Date();
		List<EventInfo> eventsInfo = alarmsAndEvents.getEventsNodeByDateRange(dut, from, to);
        for(String info : listOfEvents){
        	int count = 0;
        	for(EventInfo event : eventsInfo){
        		if(verifyAction == verifyAlarmAction.Equals){
        			if(info.toLowerCase().equals(event.getEventInfo().toLowerCase())){
        				count++;
        			}
        		}else{
        			if(event.getEventInfo().toLowerCase().contains(info.toLowerCase())){
        				count++;
        			}
        		}
        	}
        	if(count == 0){
        		report.report("Event with event info ["+info+"] was not found",Reporter.FAIL);
        		reason += "Event with event info ["+info+"] was not found.<br> ";
        	}else{
        		report.report("Event with event info ["+info+"] was found ["+count+"] time"+(count==1?"":"s"));
        	}
        }
	}
	
	@Test
	@TestProperties(name = "Set Debug Ftp Server", returnParam = "LastStatus", paramsInclude = { "DUT" })
	public void setDebugFtpServer() {
		if(isDutNull()){
			return;
		}
		DebugFtpServer debugFtpServer = DebugFtpServer.getInstance();
        boolean action = true;
        try {
	        GeneralUtils.printToConsole("Set debug FTP server.");
	        String oid = MibReader.getInstance().resolveByName("asLteStkDebugFtpServerCfgFtpAddressType");
	        if(dut.getSNMP(oid).equals("noSuchInstance"))
	        	dut.lteCli("db add debugftpserver [1]");
	        if(!dut.getSNMP(oid).equals(debugFtpServer.addressType))
	        	dut.snmpSet(oid, Integer.parseInt(debugFtpServer.addressType));
	        action = dut.snmpSet(oid, Integer.parseInt(debugFtpServer.addressType));
	        oid = MibReader.getInstance().resolveByName("asLteStkDebugFtpServerCfgFtpServerIp");
	        action &= dut.snmpSet(oid, debugFtpServer.getDebugFtpServerIP());
	        oid = MibReader.getInstance().resolveByName("asLteStkDebugFtpServerCfgFtpAddress");
	        action &= dut.snmpSet(oid, debugFtpServer.getDebugFtpServerIPInBytes());
	        oid = MibReader.getInstance().resolveByName("asLteStkDebugFtpServerCfgFtpUser");
	        action &= dut.snmpSet(oid, debugFtpServer.getDebugFtpServerUser());
	        oid = MibReader.getInstance().resolveByName("asLteStkDebugFtpServerCfgFtpPassword");
	        action &= dut.snmpSet(oid, debugFtpServer.getDebugFtpServerPassword());
        } catch (IOException e) {
        	action = false;
        	e.printStackTrace();
        }
        if(!action){
        	report.report("Failed to set debug FTP server", Reporter.FAIL);
        	reason = "Failed to set debug FTP server";
        }else{
        	report.report("Succeeded to set debug FTP server: "+debugFtpServer.getDebugFtpServerIP());
        }
	}
	
	@Test
	@TestProperties(name = "Enable Multi-Cell", returnParam = { "LastStatus" }, paramsInclude = {
			"DUT" })
	public void enableMultiCell() throws Exception {
		if(dut == null){
			report.report("No dut was configured",Reporter.FAIL);
		}
		report.report(dut.getName()+" - Trying to enable Multi-Cell.");
		if (!EnodeBConfig.getInstance().setMultiCellstate(dut, true)) {
			report.report(String.format("%s - Failed to enable Multi-Cell.", dut.getName()), Reporter.FAIL);
		}
		else{
			report.report(String.format("%s - Succeeded to enable Multi-Cell.", dut.getName()));
		}
	}

	@Test
	@TestProperties(name = "Disable Multi-Cell", returnParam = { "LastStatus" }, paramsInclude = {
			"DUT" })
	public void disableMultiCell() throws Exception {
		if(dut == null){
			report.report("No dut was configured",Reporter.FAIL);
		}
		report.report(dut.getName()+" - Trying to disable Multi-Cell.");
		if (!EnodeBConfig.getInstance().setMultiCellstate(dut, false)) {
			report.report(String.format("%s - Failed to disable Multi-Cell.", dut.getName()), Reporter.FAIL);
		}
		else{
			report.report(String.format("%s - Succeeded to disable Multi-Cell.", dut.getName()));
		}
	}


	@Test
	@TestProperties(name = "Cell To Use", returnParam = "LastStatus", paramsInclude = { "DUT","CellToUse" })
	public void changeCellToUse(){
		if(dut == null){
			report.report("No dut was configured",Reporter.FAIL);
		}
		report.report(dut.getName()+" - Trying to change Cell To Use parameter.");
		if (!EnodeBConfig.getInstance().changeCellToUse(dut, cellToUse)) {
			report.report(String.format("%s - Failed to change Cell To Use parameter.", dut.getName()), Reporter.FAIL);
		}
		else{
			report.report(String.format("%s - Succeeded to change Cell To Use parameter.", dut.getName()));
		}
	}


	@Test
	@TestProperties(name = "Copy from cell 1", returnParam = "LastStatus", paramsInclude = { "DUT","radioProfileName" })
	public void copyCellOne() throws Exception {
		if(dut == null){
			report.report("No dut was configured",Reporter.FAIL);
		}
		if (!dut.getCellEnabled(41)){
			report.report("Cell 2 is disabled- changing state to enable");
			EnodeBConfig.getInstance().setMultiCellstate(dut, true);
		}

		report.report(dut.getName()+" - Trying to copy cell 1 profiles to cell 2.");
		String cellAdvancedConfigurationProfileName = EnodeBConfig.getInstance().getCurrentCellAdvancedConfigurationProfileName(dut);
		String mobilityProfileName = EnodeBConfig.getInstance().getCurrentMobilityProfileName(dut);
		String eMBMSProfileName = EnodeBConfig.getInstance().getCurrenteMBMSProfileName(dut);
		String trafficManagementProfileName =  EnodeBConfig.getInstance().getCurrentTrafficManagementProfileName(dut);
		String callTraceProfileName  = EnodeBConfig.getInstance().getCurrentCallTraceProfileName(dut);

		dut.setCellContextNumber(2);

		report.startLevel("profiles info");
		report.report("Cell Advanced Configuration Profile: " + cellAdvancedConfigurationProfileName);
		report.report("Radio Profile: " + radioProfileName);
		report.report("mobility Profile: " + mobilityProfileName);
		report.report("eMBMS Profile: " + eMBMSProfileName);
		report.report("Traffic Management Profile: " + eMBMSProfileName);
		report.report("call Trace Profile: " + eMBMSProfileName);
		report.stopLevel();

		if (NetspanServer.getInstance().setProfilesInCell2(dut, cellAdvancedConfigurationProfileName, radioProfileName, mobilityProfileName, eMBMSProfileName, trafficManagementProfileName, callTraceProfileName)){
			report.report("profiles were copied from cell 1 to cell 2");
		}

		else
			report.report("couldn't copy the profiles to cell 2");

		dut.setCellContextNumber(1);
	}
}
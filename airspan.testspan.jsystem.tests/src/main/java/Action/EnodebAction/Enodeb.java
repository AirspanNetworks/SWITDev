package Action.EnodebAction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
import Netspan.API.Enums.EnabledDisabledStates;
import Netspan.API.Enums.EnbStates;
import Netspan.API.Enums.PrimaryClockSourceEnum;
import Netspan.API.Lte.EnbCellProperties;
import Netspan.API.Lte.LteBackhaul;
import Utils.GeneralUtils;
import Utils.SysObjUtils;
import jsystem.framework.ParameterProperties;
import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;
import jsystem.framework.scenario.Parameter;
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

	protected EnodeB dut;
	protected Integer cellId = 1;
	protected Long timeOutInMillisecond;
	private String pingIP = "20.20.2.254";
	private Integer triesToPing = 0;
	private String debugCommands = "ue show rate";
	private String parallelCommands = "";
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

	private boolean enableCell;
	private String physicalCellId;
	private String PRACHRootSequenceIndex;
	private String trackingAreaCode;
	private String emergencyAreaId;
	private String PRACHFrequencyOffset;
	private CellBarringPolicies cellBarringPolicy;
	protected CsgModes closedSubscriberGroupMode;

	// Backhaul and Ethernet
	protected BackhaulEthernetName backhaulEthernetName;
	protected BackhaulEthernetParameter backhaulEthernetParameter;
	protected DuplexType ethernetDuplex;
	protected EthernetRate ethernetRate;
	protected PortType portType;
	protected EnabledDisabledStates autoNegConfig;
	protected PortStatus portStatus;
	protected PortSpeed portSpeed;
	protected EnabledDisabledStates flowControlStatus;

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

	@ParameterProperties(description = "Time Out in millisecond (Type: long)")
	public void setTimeOutInMillisecond(String timeOutInMillisecond) {
		this.timeOutInMillisecond = Long.valueOf(timeOutInMillisecond);
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
	public void setAutoNegConfig(EnabledDisabledStates autoNegConfig) {
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
	public void setFlowControlStatus(EnabledDisabledStates flowControlStatus) {
		this.flowControlStatus = flowControlStatus;
	}

	/**
	 * 
	 * Some nice Description
	 * 
	 * 
	 */
	@Test // 1
	@TestProperties(name = "Wait For All Running And In Service", returnParam = "LastStatus", paramsInclude = { "DUT",
			"TimeOutInMillisecond" })
	public void waitForAllRunningAndInService() {
		report.report(dut.getName() + " Wait For All Running And In Service " + this.timeOutInMillisecond + " milis");

		if (this.dut.waitForAllRunningAndInService(this.timeOutInMillisecond) == false) {
			report.report("Wait For All Running And In Service Failed", Reporter.FAIL);
		} else {
			report.report("Wait For All Running And In Service Succeeded");
		}
	}

	@Test // 2
	@TestProperties(name = "Reboot EnodeB", returnParam = "LastStatus", paramsInclude = { "DUT" })
	public void reboot() {
		report.report("Reboot EnodeB " + this.dut.getName());

		if (this.dut.reboot() == false) {
			report.report("Reboot Failed", Reporter.FAIL);
		} else {
			report.report("Reboot Succeeded");
		}
	}

	@Test // 3
	@TestProperties(name = "Reset Counters", returnParam = "LastStatus", paramsInclude = { "DUT" })
	public void resetCounters() {
		report.report(this.dut.getName() + " Reset Counters");

		boolean flag = this.dut.resetCounter(null, null, null);

		if (flag == false) {
			report.report("Reset Counters Failed", Reporter.FAIL);
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

		if (flag == false) {
			report.report("Set Service State Failed", Reporter.FAIL);
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
				return;
			}
			try {
				answer = this.dut.snmpSet(this.OID, val);
			} catch (IOException e) {
				report.report("SNMP fail for OID:" + this.OID + " Value:" + val, Reporter.FAIL);
				return;
			}

			break;
		case STRING:
			try {
				this.dut.snmpSet(this.OID, this.valueToSet);
			} catch (IOException e) {
				report.report("SNMP fail for OID:" + this.OID + " Value:" + this.valueToSet, Reporter.FAIL);
				return;
			}
			break;
		}
		if (answer) {
			report.report("SNMP Set succeded for OID:" + this.OID + " Value:" + this.valueToSet);
		} else {
			report.report("SNMP Set failed for OID:" + this.OID + " Value:" + this.valueToSet, Reporter.FAIL);
		}
	}

	@Test // 8
	@TestProperties(name = "Verify SNMP MIB", returnParam = "LastStatus", paramsInclude = { "DUT", "OID", "ExpectedValue",
			"Comparison" })
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
			report.report("Is Clock source it 1588 Test faild dut to: " + e.getMessage(), Reporter.FAIL);
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
			report.report("Set " + this.enbProfile + " Failed", Reporter.FAIL);
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
				return;
			} else
				report.report("Set cell properties Passed");

			EnbCellProperties enbCellProperties1 = netspan.getEnbCellProperties(dut);
			report.report("Enabled status from netspan: " + (enbCellProperties1.isEnabled ? "enabled" : "disabled"));
			if (enbCellProperties1.isEnabled != enableCell) {
				report.report("Enabled status not as expected!", Reporter.FAIL);
				flag = false;
			}

			report.report("PCI value from netspan: " + enbCellProperties1.physicalCellId);
			if (physicalCellId != null) {
				if (enbCellProperties1.physicalCellId != pciValue) {
					report.report("PCI value not as expected!", Reporter.FAIL);
					flag = false;
				}
			}
			report.report("RSI value from netspan: " + enbCellProperties1.prachRsi);
			if (PRACHRootSequenceIndex != null) {
				if (enbCellProperties1.prachRsi != rsiValue) {
					report.report("RSI value not as expected!", Reporter.FAIL);
					flag = false;
				}
			}
			report.report("PCI value from netspan: " + enbCellProperties1.trackingAreaCode);
			if (trackingAreaCode != null) {
				if (enbCellProperties1.trackingAreaCode != trackingCodeValue) {
					report.report("trackingAreaCode value not as expected!", Reporter.FAIL);
					flag = false;
				}
			}
			report.report("emergencyAreaId value from netspan: " + enbCellProperties1.emergencyAreaId);
			if (emergencyAreaId != null) {
				if (enbCellProperties1.emergencyAreaId != emergencyAreaValue) {
					report.report("emergencyAreaId value not as expected!", Reporter.FAIL);
					flag = false;
				}
			}
			report.report("prachFreqOffset value from netspan: " + enbCellProperties1.prachFreqOffset);
			if (PRACHFrequencyOffset != null) {
				if (enbCellProperties1.prachFreqOffset != PRACHFrequencyOffsetValue) {
					report.report("prachFreqOffset value not as expected!", Reporter.FAIL);
					flag = false;
				}
			}

			report.report(
					"closedSubscriberGroupMode value from netspan: " + enbCellProperties1.closedSubscriberGroupMode);
			if (closedSubscriberGroupMode != null) {
				if (enbCellProperties1.closedSubscriberGroupMode != closedSubscriberGroupMode) {
					report.report("closedSubscriberGroupMode value not as expected!", Reporter.FAIL);
					flag = false;
				}
			}

			report.report("cellBarringPolicy value from netspan: " + enbCellProperties1.cellBarringPolicy);
			if (cellBarringPolicy != null) {
				if (enbCellProperties1.cellBarringPolicy != cellBarringPolicy) {
					report.report("cellBarringPolicy", Reporter.FAIL);
					flag = false;
				}
			}
			GeneralUtils.stopLevel();

		} catch (Exception e) {
			report.report("Could not get netspan! Set cell properties failed ", Reporter.FAIL);
		}
	}

	@Test
	@TestProperties(name = "Move to PNP list", returnParam = { "IsTestWasSuccessful" }, paramsInclude = { "DUT" })
	public void moveToPNPList() {
		EnodeBConfig enodeBConfig = EnodeBConfig.getInstance();
		report.report("Trying to move " + dut.getNetspanName() + " to PnP list");

		if (!enodeBConfig.convertToPnPConfig(dut)) {
			report.report("Failed to set EnodeB as Pnp", Reporter.FAIL);
		} else {
			report.report("Configuration done");
		}
	}

	@Test
	@TestProperties(name = "Start Parallel Commands", returnParam = { "IsTestWasSuccessful" }, paramsInclude = { "DUT",
			"ParallelCommands" })
	public void startParallelCommands() {
		report.report("Starting parallel commands: " + this.parallelCommands);
		List<String> commandList = GeneralUtils.convertStringToArrayList(this.parallelCommands, ";");
		boolean flag = true;
		try {
			startingParallelCommands(this.dut, commandList);
		} catch (IOException e) {
			e.printStackTrace();
			report.report("Start parallel commands failed due to: " + e.getMessage(), Reporter.FAIL);
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
		}
	}

	@Test
	@TestProperties(name = "Verify Discovered Properties", returnParam = { "IsTestWasSuccessful" }, paramsInclude = {
			"DUT", "BackhaulEthernetName", "BackhaulEthernetParameter", "EthernetDuplex", "EthernetRate", "PortType", "AutoNegConfig",
			"PortStatus", "PortSpeed", "FlowControlStatus" })
	public void verifyDiscoveredProperties() {
		List<LteBackhaul> lteBackhauls = EnodeBConfig.getInstance().getBackhaulInterfaceStatus(this.dut);
		boolean nameFlag = false;
		boolean testResponseFlag = false;
		
		if(lteBackhauls == null || this.backhaulEthernetName == null || this.backhaulEthernetParameter == null) {
			report.report("Verify " + this.backhaulEthernetName + ": "+ this.backhaulEthernetParameter, Reporter.FAIL);
			return;
		}
		else {
			report.report("Verify " + this.backhaulEthernetName + ": "+ this.backhaulEthernetParameter);
		}
		
		String parameterName = this.backhaulEthernetName + " " + this.backhaulEthernetParameter;
		for (LteBackhaul lteBackhaul : lteBackhauls) {
			if (lteBackhaul.name.equals(this.backhaulEthernetName.toString())) {
				nameFlag = true;
				switch (this.backhaulEthernetParameter) {
				case Ethernet_Duplex:
					testResponseFlag = reportCompareBetweenTwoStrings(parameterName, lteBackhaul.ethernetDuplex.value(), this.ethernetDuplex.value());
					break;
				case Ethernet_Rate:
					testResponseFlag = reportCompareBetweenTwoStrings(parameterName, lteBackhaul.ethernetRate, this.ethernetRate.value());
					break;
				case Port_Type:
					testResponseFlag = reportCompareBetweenTwoStrings(parameterName, lteBackhaul.portType, this.portType.toString());
					break;
				case Auto_Neg_Configuration:
					testResponseFlag = reportCompareBetweenTwoStrings(parameterName, lteBackhaul.autoNegConfig.value(), this.autoNegConfig.value());
					break;
				case Port_Status:
					testResponseFlag = reportCompareBetweenTwoStrings(parameterName, lteBackhaul.portStatus, this.portStatus.value());
					break;
				case Port_Speed:
					testResponseFlag = reportCompareBetweenTwoStrings(parameterName, lteBackhaul.portSpeed, this.portSpeed.value());
					break;
				case Flow_Control_Status:
					testResponseFlag = reportCompareBetweenTwoStrings(parameterName, lteBackhaul.flowControlStatus.value(), this.flowControlStatus.value());
					break;
				}
			}
		}
		if(!nameFlag) {
			report.report(this.backhaulEthernetName + " does not exist", Reporter.FAIL);
		}
		setAnswer(testResponseFlag);
	}

	@Override
	public void init() {
		if (enbInTest == null) {
			enbInTest = new ArrayList<EnodeB>();
		}
		if (dut != null) {
			enbInTest.add(dut);
		}
		super.init();
	}

	@Override
	public void handleUIEvent(HashMap<String, Parameter> map, String methodName) throws Exception {

		if (methodName.equals(VERIFY_COUNTER_VALUE_METHOD_NAME)) {
			handleUIEventGetCounterValue(map, methodName);
		}

		else if (methodName.equals(VERIFY_DISCOVERED_PROPERTIES_METHOD_NAME)) {
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
}
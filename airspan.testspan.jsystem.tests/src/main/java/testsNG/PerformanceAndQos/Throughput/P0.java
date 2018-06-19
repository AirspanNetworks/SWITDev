
package testsNG.PerformanceAndQos.Throughput;

import java.util.ArrayList;

import org.junit.Test;

import Entities.ITrafficGenerator.Protocol;
import Utils.GeneralUtils;
import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;

public class P0 extends TPTBase {

	/**
	 * initialize Throughput parameters: Traffic, peripheralsConfig, enbConfig,
	 * attenuatorSetUnderTest.
	 * 
	 * @author Shahaf Shuhamy
	 */
	@Override
	public void init() throws Exception {
		super.init();
	}

	// QCI 9 Tests------------------------------------------------------QCI
	// 9------------------------------------------------------------
	@Test
	@TestProperties(name = "TPT_PtP_1400_Udp_Qci9", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void TPT_PtP_1400_Udp_Qci9() throws Exception {
		testName = "PtP_1400_Udp_Qci9";
		isQci9 = true;
		streamsMode = "PTP";
		packetSize = 1400;
		TEST_TIME_MILLIS = setTime(TEST_TIME_SHORT);
		qci.add('9');
		GeneralUtils.printToConsole(dut.getName() + " " + dut.getNetspanName());
		ueList = getUES(dut, 1);
		ueNameListStc = convertUeToNamesList(ueList);
		GeneralUtils.printToConsole(ueNameListStc.size());
		preTest();
		TestProcess();
		AfterTest(TPT_THRESHOLD_PRECENT);
	}
	
	@Test
	@TestProperties(name = "TPT_PtP_1460_Udp_Qci9", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void TPT_PtP_1460_Udp_Qci9() throws Exception {
		testName = "PtP_1460_Udp_Qci9";
		isQci9 = true;
		streamsMode = "PTP";
		packetSize = 1460;
		TEST_TIME_MILLIS = setTime(TEST_TIME_SHORT);
		qci.add('9');
		GeneralUtils.printToConsole(dut.getName() + " " + dut.getNetspanName());
		ueList = getUES(dut, 1);
		ueNameListStc = convertUeToNamesList(ueList);
		GeneralUtils.printToConsole(ueNameListStc.size());
		preTest();
		TestProcess();
		AfterTest(TPT_THRESHOLD_PRECENT);
	}

	@Test
	@TestProperties(name = "TPT_Pt2P_1400_Udp_Qci9", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void TPT_Pt2P_1400_Udp_Qci9() throws Exception {
		testName = "Pt2P_1400_Udp_Qci9";
		isQci9 = true;
		streamsMode = "PT2P";
		packetSize = 1400;
		TEST_TIME_MILLIS = setTime(TEST_TIME_SHORT);
		qci.add('9');
		ueList = getUES(dut, 2);
		ueNameListStc = convertUeToNamesList(ueList);
		GeneralUtils.printToConsole(ueNameListStc.size());
		preTest();
		TestProcess();
		AfterTest(TPT_THRESHOLD_PRECENT);
	}

	@Test
	@TestProperties(name = "TPT_PtP_1024_Udp_Qci9", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void TPT_PtP_1024_Udp_Qci9() throws Exception {
		testName = "PtP_1024_Udp_Qci9";
		isQci9 = true;
		streamsMode = "PTP";
		packetSize = 1024;
		TEST_TIME_MILLIS = setTime(TEST_TIME_SHORT);
		qci.add('9');
		ueList = getUES(dut,1);
		ueNameListStc = convertUeToNamesList(ueList);
		preTest();
		TestProcess();
		AfterTest(TPT_THRESHOLD_PRECENT);
	}
	
	@Test
	@TestProperties(name = "CA_TPT_PtP_1024_Udp_Qci9", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void CA_TPT_PtP_1024_Udp_Qci9() throws Exception {
		testName = "CA_PtP_1024_Udp_Qci9";
		isQci9 = true;
		streamsMode = "PTP";
		packetSize = 1024;
		TEST_TIME_MILLIS = setTime(TEST_TIME_SHORT);
		qci.add('9');
		isCaTest = true;
		ueList = getUES(dut, 1);
		if(ueList.size() == 0 ){
			report.report("no suitable UES - Failing and Ending Test",Reporter.FAIL);
			return;
		}
		
		ueNameListStc = convertUeToNamesList(ueList);
		preTest();
		TestProcess();
		AfterTest(TPT_THRESHOLD_PRECENT);
	}
	
	@Test
	@TestProperties(name = "CA_TPT_PtMP_1024_Udp_Qci9", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void CA_TPT_PTMP_1024_Udp_Qci9() throws Exception {
		testName = "CA_PtMP_1024_Udp_Qci9";
		isQci9 = true;
		streamsMode = "PTMP";
		packetSize = 1024;
		TEST_TIME_MILLIS = setTime(TEST_TIME_SHORT);
		qci.add('9');
		isCaTest = true;
		ueList = getUES(dut);
		if(ueList.size() == 0 ){
			report.report("no suitable UES - Failing and Ending Test",Reporter.FAIL);
			return;
		}
		
		ueNameListStc = convertUeToNamesList(ueList);
		preTest();
		TestProcess();
		AfterTest(TPT_THRESHOLD_PRECENT);
	}
	
	@Test
	@TestProperties(name = "CA_TPT_PtP_1400_Udp_Qci9", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void CA_TPT_PtP_1400_Udp_Qci9() throws Exception {
		testName = "CA_PtP_1400_Udp_Qci9";
		isQci9 = true;
		streamsMode = "PTP";
		packetSize = 1400;
		TEST_TIME_MILLIS = setTime(TEST_TIME_SHORT);
		qci.add('9');
		isCaTest = true;
		ueList = getUES(dut, 1);
		if(ueList.size() == 0 ){
			report.report("no suitable UES - Failing and Ending Test",Reporter.FAIL);
			return;
		}
		
		ueNameListStc = convertUeToNamesList(ueList);
		preTest();
		TestProcess();
		AfterTest(TPT_THRESHOLD_PRECENT);
	}
	
	@Test
	@TestProperties(name = "CA_TPT_PtMP_1400_Udp_Qci9", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void CA_TPT_PTMP_1400_Udp_Qci9() throws Exception {
		testName = "CA_PtMP_1400_Udp_Qci9";
		isQci9 = true;
		streamsMode = "PTMP";
		packetSize = 1400;
		TEST_TIME_MILLIS = setTime(TEST_TIME_SHORT);
		qci.add('9');
		isCaTest = true;
		ueList = getUES(dut);
		if(ueList.size() == 0 ){
			report.report("no suitable UES - Failing and Ending Test",Reporter.FAIL);
			return;
		}
		
		ueNameListStc = convertUeToNamesList(ueList);
		preTest();
		TestProcess();
		AfterTest(TPT_THRESHOLD_PRECENT);
	}
	
	

	@Test
	@TestProperties(name = "TPT_Pt2P_1024_Udp_Qci9", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void TPT_Pt2P_1024_Udp_Qci9() throws Exception {
		testName = "Pt2P_1024_Udp_Qci9";
		isQci9 = true;
		streamsMode = "PT2P";
		packetSize = 1024;
		TEST_TIME_MILLIS = setTime(TEST_TIME_SHORT);
		qci.add('9');
		ueList = getUES(dut,2);
		ueNameListStc = convertUeToNamesList(ueList);
		preTest();
		TestProcess();
		AfterTest(TPT_THRESHOLD_PRECENT);
	}

	@Test
	@TestProperties(name = "TPT_PtP_800_Udp_Qci9", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void TPT_PtP_800_Udp_Qci9() throws Exception {
		testName = "PtP_800_Udp_Qci9";
		isQci9 = true;
		streamsMode = "PTP";
		packetSize = 800;
		TEST_TIME_MILLIS = setTime(TEST_TIME_LONG);
		qci.add('9');
		ueList = getUES(dut, 1);
		ueNameListStc = convertUeToNamesList(ueList);
		preTest();
		TestProcess();
		AfterTest(TPT_THRESHOLD_PRECENT);
	}

	@Test
	@TestProperties(name = "TPT_Pt2P_800_Udp_Qci9", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void TPT_Pt2P_800_Udp_Qci9() throws Exception {
		testName = "Pt2P_800_Udp_Qci9";
		isQci9 = true;
		streamsMode = "PT2P";
		packetSize = 800;
		TEST_TIME_MILLIS = setTime(TEST_TIME_LONG);
		qci.add('9');
		ueList = getUES(dut,2);
		ueNameListStc = convertUeToNamesList(ueList);
		preTest();
		TestProcess();
		AfterTest(TPT_THRESHOLD_PRECENT);
	}

	@Test
	@TestProperties(name = "TPT_PtP_1522_Udp_Qci9", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void TPT_PtP_1522_Udp_Qci9() throws Exception {
		testName = "PtP_1522_Udp_Qci9";
		isQci9 = true;
		streamsMode = "PTP";
		packetSize = 1522;
		TEST_TIME_MILLIS = setTime(TEST_TIME_SHORT);
		qci.add('9');
		ueList = getUES(dut, 1);
		ueNameListStc = convertUeToNamesList(ueList);
		preTest();
		TestProcess();
		AfterTest(TPT_THRESHOLD_PRECENT);
	}

	@Test
	@TestProperties(name = "TPT_Pt2P_1522_Udp_Qci9", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void TPT_Pt2P_1522_Udp_Qci9() throws Exception {
		testName = "Pt2P_1522_Udp_Qci9";
		isQci9 = true;
		streamsMode = "PT2P";
		packetSize = 1522;
		TEST_TIME_MILLIS = setTime(TEST_TIME_SHORT);
		qci.add('9');
		ueList = getUES(dut,2);
		ueNameListStc = convertUeToNamesList(ueList);
		preTest();
		TestProcess();
		AfterTest(TPT_THRESHOLD_PRECENT);
	}

	// QCI 9 Tests---------------------------------------------------------QCI
	// 9------------------------------------------------------

	// QCI 7 Tests---------------------------------------------------------QCI
	// 7------------------------------------------------------

	@Test
	@TestProperties(name = "TPT_PtP_1400_Udp_Qci7", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void TPT_PtP_1400_Udp_Qci7() throws Exception {
		testName = "PtP_1400_Udp_Qci7";
		isQci9 = false;
		streamsMode = "PTP";
		packetSize = 1400;
		TEST_TIME_MILLIS = setTime(TEST_TIME_SHORT);
		qci.add('7');
		ueList = getUES(dut, 1);
		ueNameListStc = convertUeToNamesList(ueList);
		preTest();
		TestProcess();
		AfterTest(TPT_THRESHOLD_PRECENT);
	}

	@Test
	@TestProperties(name = "TPT_Pt2P_1400_Udp_Qci7", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void TPT_Pt2P_1400_Udp_Qci7() throws Exception {
		testName = "Pt2P_1400_Udp_Qci7";
		isQci9 = false;
		streamsMode = "PT2P";
		packetSize = 1400;
		TEST_TIME_MILLIS = setTime(TEST_TIME_SHORT);
		qci.add('7');
		ueList = getUES(dut, 2);
		ueNameListStc = convertUeToNamesList(ueList);
		preTest();
		TestProcess();
		AfterTest(TPT_THRESHOLD_PRECENT);
	}

	@Test
	@TestProperties(name = "TPT_PtP_1024_Udp_Qci7", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void TPT_PtP_1024_Udp_Qci7() throws Exception {
		testName = "PtP_1024_Udp_Qci7";
		isQci9 = false;
		streamsMode = "PTP";
		packetSize = 1024;
		TEST_TIME_MILLIS = setTime(TEST_TIME_SHORT);
		qci.add('7');
		ueList = getUES(dut, 1);
		ueNameListStc = convertUeToNamesList(ueList);
		preTest();
		TestProcess();
		AfterTest(TPT_THRESHOLD_PRECENT);
	}

	@Test
	@TestProperties(name = "TPT_Pt2P_1024_Udp_Qci7", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void TPT_Pt2P_1024_Udp_Qci7() throws Exception {
		testName = "Pt2P_1024_Udp_Qci7";
		isQci9 = false;
		streamsMode = "PT2P";
		packetSize = 1024;
		TEST_TIME_MILLIS = setTime(TEST_TIME_SHORT);
		qci.add('7');
		ueList = getUES(dut, 2);
		ueNameListStc = convertUeToNamesList(ueList);
		preTest();
		TestProcess();
		AfterTest(TPT_THRESHOLD_PRECENT);
	}

	@Test
	@TestProperties(name = "TPT_PtP_800_Udp_Qci7", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void TPT_PtP_800_Udp_Qci7() throws Exception {
		testName = "PtP_800_Udp_Qci7";
		isQci9 = false;
		streamsMode = "PTP";
		TEST_TIME_MILLIS = setTime(TEST_TIME_LONG);
		packetSize = 800;
		qci.add('7');
		ueList = getUES(dut, 1);
		ueNameListStc = convertUeToNamesList(ueList);
		preTest();
		TestProcess();
		AfterTest(TPT_THRESHOLD_PRECENT);
	}

	@Test
	@TestProperties(name = "TPT_Pt2P_800_Udp_Qci7", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void TPT_Pt2P_800_Udp_Qci7() throws Exception {
		testName = "Pt2P_800_Udp_Qci7";
		isQci9 = false;
		streamsMode = "PT2P";
		packetSize = 800;
		TEST_TIME_MILLIS = setTime(TEST_TIME_LONG);
		qci.add('7');
		ueList = getUES(dut, 2);
		ueNameListStc = convertUeToNamesList(ueList);
		preTest();
		TestProcess();
		AfterTest(TPT_THRESHOLD_PRECENT);
	}

	@Test
	@TestProperties(name = "TPT_PtP_1522_Udp_Qci7", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void TPT_PtP_1522_Udp_Qci7() throws Exception {
		testName = "PtP_1522_Udp_Qci7";
		isQci9 = false;
		streamsMode = "PTP";
		packetSize = 1522;
		TEST_TIME_MILLIS = setTime(TEST_TIME_SHORT);
		qci.add('7');
		ueList = getUES(dut, 1);
		ueNameListStc = convertUeToNamesList(ueList);
		preTest();
		TestProcess();
		AfterTest(TPT_THRESHOLD_PRECENT);
	}

	@Test
	@TestProperties(name = "TPT_Pt2P_1522_Udp_Qci7", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void TPT_Pt2P_1522_Udp_Qci7() throws Exception {
		testName = "Pt2P_1522_Udp_Qci7";
		isQci9 = false;
		streamsMode = "PT2P";
		packetSize = 1522;
		TEST_TIME_MILLIS = setTime(TEST_TIME_SHORT);
		qci.add('7');
		ueList = getUES(dut, 2);
		ueNameListStc = convertUeToNamesList(ueList);
		preTest();
		TestProcess();
		AfterTest(TPT_THRESHOLD_PRECENT);
	}
	// QCI 7 Tests----------------------------------------------------QCI
	// 7-----------------------------------------------------------

	// QCI 7 And 9 Tests----------------------------------------------------QCI
	// 7 And 9-------------------------------------------------

	@Test
	@TestProperties(name = "QOS_PtP_1400_Udp_QCI7And9", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void QOS_PtP_1400_Udp_QCI7And9() throws Exception {
		testName = "PtP_1400_Udp_QCI7And9";
		streamsMode = "PTP";
		packetSize = 1400;
		TEST_TIME_MILLIS = setTime(TEST_TIME_SHORT);
		qci.add('7');
		qci.add('9');
		ueList = getUES(dut, 1);
		ueNameListStc = convertUeToNamesList(ueList);
		preTest();
		TestProcess();
		AfterTest(QOS_THRESHOLD_PRECENT);
	}

	@Test
	@TestProperties(name = "QOS_Pt2P_1400_Udp_Qci7And9", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void QOS_Pt2P_1400_Udp_Qci7And9() throws Exception {
		testName = "Pt2P_1400_Udp_Qci7And9";
		streamsMode = "PT2P";
		packetSize = 1400;
		TEST_TIME_MILLIS = setTime(TEST_TIME_SHORT);
		qci.add('7');
		qci.add('9');
		ueList = getUES(dut, 2);
		ueNameListStc = convertUeToNamesList(ueList);
		preTest();
		TestProcess();
		AfterTest(QOS_THRESHOLD_PRECENT);
	}

	@Test
	@TestProperties(name = "QOS_PtP_1024_Udp_Qci7And9", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void QOS_PtP_1024_Udp_Qci7And9() throws Exception {
		testName = "PtP_1024_Udp_Qci7And9";
		streamsMode = "PTP";
		packetSize = 1024;
		TEST_TIME_MILLIS = setTime(TEST_TIME_SHORT);
		qci.add('7');
		qci.add('9');
		ueList = getUES(dut, 1);
		ueNameListStc = convertUeToNamesList(ueList);
		preTest();
		TestProcess();
		AfterTest(QOS_THRESHOLD_PRECENT);
	}

	@Test
	@TestProperties(name = "QOS_Pt2P_1024_Udp_Qci7And9", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void QOS_Pt2P_1024_Udp_Qci7And9() throws Exception {
		testName = "Pt2P_1024_Udp_Qci7And9";
		streamsMode = "PT2P";
		packetSize = 1024;
		TEST_TIME_MILLIS = setTime(TEST_TIME_SHORT);
		qci.add('7');
		qci.add('9');
		ueList = getUES(dut,2);
		ueNameListStc = convertUeToNamesList(ueList);
		preTest();
		TestProcess();
		AfterTest(QOS_THRESHOLD_PRECENT);
	}

	@Test
	@TestProperties(name = "QOS_PtP_800_Udp_Qci7And9", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void QOS_PtP_800_Udp_Qci7And9() throws Exception {
		testName = "PtP_800_Udp_Qci7And9";
		streamsMode = "PTP";
		packetSize = 800;
		TEST_TIME_MILLIS = setTime(TEST_TIME_SHORT);
		qci.add('7');
		qci.add('9');
		ueList = getUES(dut,1);
		ueNameListStc = convertUeToNamesList(ueList);
		preTest();
		TestProcess();
		AfterTest(QOS_THRESHOLD_PRECENT);
	}

	@Test
	@TestProperties(name = "QOS_Pt2P_800_Udp_Qci7And9", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void QOS_Pt2P_800_Udp_Qci7And9() throws Exception {
		testName = "Pt2P_800_Udp_Qci7And9";
		streamsMode = "PT2P";
		packetSize = 800;
		TEST_TIME_MILLIS = setTime(TEST_TIME_SHORT);
		qci.add('7');
		qci.add('9');
		ueList = getUES(dut, 2);
		ueNameListStc = convertUeToNamesList(ueList);
		preTest();
		TestProcess();
		AfterTest(QOS_THRESHOLD_PRECENT);
	}

	@Test
	@TestProperties(name = "QOS_PtP_1522_Udp_Qci7And9", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void QOS_PtP_1522_Udp_Qci7And9() throws Exception {
		testName = "PtP_1522_Udp_Qci7And9";
		streamsMode = "PTP";
		packetSize = 1522;
		TEST_TIME_MILLIS = setTime(TEST_TIME_SHORT);
		qci.add('7');
		qci.add('9');
		ueList = getUES(dut, 1);
		ueNameListStc = convertUeToNamesList(ueList);
		preTest();
		TestProcess();
		AfterTest(QOS_THRESHOLD_PRECENT);
	}

	@Test
	@TestProperties(name = "QOS_Pt2P_1522_Udp_Qci7And9", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void QOS_Pt2P_1522_Udp_Qci7And9() throws Exception {
		testName = "Pt2P_1522_Udp_Qci7And9";
		streamsMode = "PT2P";
		packetSize = 1522;
		TEST_TIME_MILLIS = setTime(TEST_TIME_SHORT);
		qci.add('7');
		qci.add('9');
		ueList = getUES(dut,2);
		ueNameListStc = convertUeToNamesList(ueList);
		preTest();
		TestProcess();
		AfterTest(QOS_THRESHOLD_PRECENT);
	}

	
	/************************************ TCP TP Tests ************************************/
	/*** QCI 9 Tests ***/
	
	@Test
	@TestProperties(name = "TPT_PtP_MSS1360_WS256K_TCP_Qci9", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void TPT_PtP_MSS1360_WS256K_TCP_Qci9() throws Exception {
		ArrayList<Character> qciList = new ArrayList<Character>();
		qciList.add('9');
		templateTPTest("TPT_PtP_MSS1360_WS256K_TCP_Qci9", true, "PTP", 1360, qciList, 1, Protocol.TCP, 8, 256D);
	}
	
	@Test
	@TestProperties(name = "TPT_PtMP_MSS1360_WS256K_TCP_Qci9", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void TPT_PtMP_MSS1360_WS256K_TCP_Qci9() throws Exception {
		ArrayList<Character> qciList = new ArrayList<Character>();
		qciList.add('9');
		templateTPTest("TPT_PtMP_MSS1360_WS256K_TCP_Qci9", true, "PTMP", 1360, qciList, 0, Protocol.TCP, 8, 256D);
	}
	
	@Test
	@TestProperties(name = "CA_TPT_PtMP_MSS1360_WS256K_TCP_Qci9", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void CA_TPT_PtMP_MSS1360_WS256K_TCP_Qci9() throws Exception {
		ArrayList<Character> qciList = new ArrayList<Character>();
		qciList.add('9');
		isCaTest = true;
		templateTPTest("CA_TPT_PtMP_MSS1360_WS256K_TCP_Qci9", true, "PTMP", 1360, qciList, 0, Protocol.TCP, 8, 256D);
	}
	
	@Test
	@TestProperties(name = "CA_TPT_PtMP_MSS1460_WS256K_TCP_Qci9", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void CA_TPT_PtMP_MSS1460_WS256K_TCP_Qci9() throws Exception {
		ArrayList<Character> qciList = new ArrayList<Character>();
		qciList.add('9');
		isCaTest = true;
		templateTPTest("CA_TPT_PtMP_MSS1460_WS256K_TCP_Qci9", true, "PTMP", 1460, qciList, 0, Protocol.TCP, 8, 256D);
	}
	
	@Test
	@TestProperties(name = "CA_TPT_PtMP_MSS759_WS256K_TCP_Qci9", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void CA_TPT_PtMP_MSS759_WS256K_TCP_Qci9() throws Exception {
		ArrayList<Character> qciList = new ArrayList<Character>();
		qciList.add('9');
		isCaTest = true;
		templateTPTest("CA_TPT_PtMP_MSS759_WS256K_TCP_Qci9", true, "PTMP", 759, qciList, 0, Protocol.TCP, 8, 256D);
	}
	
	@Test
	@TestProperties(name = "TPT_PtMP_MSS1460_WS256K_TCP_Qci9", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void TPT_PtMP_MSS1460_WS256K_TCP_Qci9() throws Exception {
		ArrayList<Character> qciList = new ArrayList<Character>();
		qciList.add('9');
		templateTPTest("TPT_PtMP_MSS1460_WS256K_TCP_Qci9", true, "PTMP", 1460, qciList, 0, Protocol.TCP, 8, 256D);
	}
	
	@Test
	@TestProperties(name = "TPT_PtMP_MSS759_WS256K_TCP_Qci9", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void TPT_PtMP_MSS759_WS256K_TCP_Qci9() throws Exception {
		ArrayList<Character> qciList = new ArrayList<Character>();
		qciList.add('9');
		templateTPTest("TPT_PtMP_MSS759_WS256K_TCP_Qci9", true, "PTMP", 759, qciList, 0, Protocol.TCP, 8, 256D);
	}
	
	@Test
	@TestProperties(name = "TPT_PtP_MSS1460_WS256K_TCP_Qci9", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void TPT_PtP_MSS1460_WS256K_TCP_Qci9() throws Exception {
		ArrayList<Character> qciList = new ArrayList<Character>();
		qciList.add('9');
		templateTPTest("TPT_PtP_MSS1460_WS256K_TCP_Qci9", true, "PTP", 1460, qciList, 1, Protocol.TCP, 8, 256D);
	}
	
	@Test
	@TestProperties(name = "TPT_PtP_MSS759_WS256K_TCP_Qci9", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void TPT_PtP_MSS759_WS256K_TCP_Qci9() throws Exception {
		ArrayList<Character> qciList = new ArrayList<Character>();
		qciList.add('9');
		templateTPTest("TPT_PtP_MSS759_WS256K_TCP_Qci9", true, "PTP", 759, qciList, 1, Protocol.TCP, 8, 256D);
	}
	
	@Test
	@TestProperties(name = "TPT_PtP_MSS1200_WS256K_TCP_Qci9", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void TPT_PtP_MSS1200_WS256K_TCP_Qci9() throws Exception {
		ArrayList<Character> qciList = new ArrayList<Character>();
		qciList.add('9');
		templateTPTest("TPT_PtP_MSS1200_WS256K_TCP_Qci9", true, "PTP", 1200, qciList, 1, Protocol.TCP, 8, 256D);
	}
	
	@Test
	@TestProperties(name = "CA_TPT_PtP_MSS1460_WS256K_TCP_Qci9", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void CA_TPT_PtP_MSS1460_WS256K_TCP_Qci9() throws Exception {
		ArrayList<Character> qciList = new ArrayList<Character>();
		qciList.add('9');
		isCaTest = true;
		templateTPTest("CA_TPT_PtP_MSS1460_WS256K_TCP_Qci9", true, "PTP", 1460, qciList, 1, Protocol.TCP, 8, 256D);
	}
	
	@Test
	@TestProperties(name = "CA_TPT_PtP_MSS759_WS256K_TCP_Qci9", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void CA_TPT_PtP_MSS759_WS256K_TCP_Qci9() throws Exception {
		ArrayList<Character> qciList = new ArrayList<Character>();
		qciList.add('9');
		isCaTest = true;
		templateTPTest("CA_TPT_PtP_MSS759_WS256K_TCP_Qci9", true, "PTP", 759, qciList, 1, Protocol.TCP, 8, 256D);
	}
	
	@Test
	@TestProperties(name = "CA_TPT_PtP_MSS1200_WS256K_TCP_Qci9", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void CA_TPT_PtP_MSS1200_WS256K_TCP_Qci9() throws Exception {
		ArrayList<Character> qciList = new ArrayList<Character>();
		qciList.add('9');
		isCaTest = true;
		templateTPTest("CA_TPT_PtP_MSS1200_WS256K_TCP_Qci9", true, "PTP", 1200, qciList, 1, Protocol.TCP, 8, 256D);
	}
	
	@Test
	@TestProperties(name = "CA_TPT_PtP_MSS1360_WS256K_TCP_Qci9", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void CA_TPT_PtP_MSS1360_WS256K_TCP_Qci9() throws Exception {
		ArrayList<Character> qciList = new ArrayList<Character>();
		qciList.add('9');
		isCaTest = true;
		templateTPTest("CA_TPT_PtP_MSS1360_WS256K_TCP_Qci9", true, "PTP", 1360, qciList, 1, Protocol.TCP, 8, 256D);
	}
	
	@Test
	@TestProperties(name = "TPT_Pt3P_MSS1460_WS256K_TCP_Qci9", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void TPT_Pt3P_MSS1460_WS256K_TCP_Qci9() throws Exception {
		ArrayList<Character> qciList = new ArrayList<Character>();
		qciList.add('9');
		templateTPTest("TPT_Pt3P_MSS1460_WS256K_TCP_Qci9", true, "PTMP", 1460, qciList, 3, Protocol.TCP, 8, 256D);
	}
	
	@Test
	@TestProperties(name = "CA_TPT_Pt3P_MSS1460_WS256K_TCP_Qci9", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void CA_TPT_Pt3P_MSS1460_WS256K_TCP_Qci9() throws Exception {
		ArrayList<Character> qciList = new ArrayList<Character>();
		qciList.add('9');
		isCaTest = true;
		
		templateTPTest("CA_TPT_Pt3P_MSS1460_WS256K_TCP_Qci9", true, "PTP", 1460, qciList, 3, Protocol.TCP, 8, 256D);
	}
	
	@Test
	@TestProperties(name = "TPT_PtP_TCP_Qci9", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void TPT_PtP_TCP_Qci9() throws Exception {
		ArrayList<Character> qciList = new ArrayList<Character>();
		qciList.add('9');
		templateTPTest("TPT_PtP_TCP_Qci9", true, "PTP", null, qciList, 1, Protocol.TCP, null, null);
	}
	
	@Test
	@TestProperties(name = "TPT_PtMP_TCP_Qci9", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void TPT_PtMP_TCP_Qci9() throws Exception {
		ArrayList<Character> qciList = new ArrayList<Character>();
		qciList.add('9');
		templateTPTest("TPT_PtMP_TCP_Qci9", true, "PTMP", null, qciList, 1, Protocol.TCP, null, null);
	}
	
	@Test
	@TestProperties(name = "CA_TPT_PtP_TCP_Qci9", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void CA_TPT_PtP_TCP_Qci9() throws Exception {
		ArrayList<Character> qciList = new ArrayList<Character>();
		qciList.add('9');
		isCaTest = true;
		templateTPTest("TPT_PtP_TCP_Qci9", true, "PTP", null, qciList, 1, Protocol.TCP, null, null);
	}
	
	@Test
	@TestProperties(name = "CA_TPT_PtMP_TCP_Qci9", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void CA_TPT_PtMP_TCP_Qci9() throws Exception {
		ArrayList<Character> qciList = new ArrayList<Character>();
		qciList.add('9');
		isCaTest = true;
		templateTPTest("TPT_PtMP_TCP_Qci9", true, "PTMP", null, qciList, 0, Protocol.TCP, null, null);
	}
	
	private void templateTPTest(String testName, boolean isQci9, String streamsMode, Integer packetSize, ArrayList<Character> qciList, 
			int numberOfUEsInTest, Protocol protocol, Integer numberParallelStreams, Double windowSizeInKbits){
		this.TEST_TIME_MILLIS = setTime(TEST_TIME_LONG);
		this.testName = testName;
		this.isQci9 = isQci9;
		this.streamsMode = streamsMode;
		this.packetSize = packetSize;
		this.numberParallelStreams = numberParallelStreams;
		this.windowSizeInKbits = windowSizeInKbits;
		qci.addAll(qciList);
		GeneralUtils.printToConsole(dut.getName() + " " + dut.getNetspanName());
		if(numberOfUEsInTest > 0){
			ueList = getUES(dut, numberOfUEsInTest);
		}else{
			ueList = getUES(dut);
		}
		if(ueList.size() == 0 ){
			report.report("no suitable UES - Failing and Ending Test",Reporter.FAIL);
			return;
		}
		peripheralsConfig.stopUEs(ueList);
		
		ueNameListStc = convertUeToNamesList(ueList);
		GeneralUtils.printToConsole(ueNameListStc.size());
		this.protocol = protocol;
		preTest();
		TestProcess();
		AfterTest(TPT_THRESHOLD_PRECENT);
	}
}

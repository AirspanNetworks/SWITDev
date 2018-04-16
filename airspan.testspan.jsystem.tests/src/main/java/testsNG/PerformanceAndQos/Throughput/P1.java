package testsNG.PerformanceAndQos.Throughput;

import org.junit.Test;


import jsystem.framework.TestProperties;

public class P1 extends TPTBase {
	
//---------------------------------------------------Qci7------------------------------------------------------	
	
	@Test
	@TestProperties(name = "TPT_PtP_512_Udp_Qci7", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void TPT_PtP_512_Udp_Qci7() throws Exception {
		testName = "PtP_512_Udp_Qci7";
		streamsMode = "PTP";
		packetSize = 512;
		TEST_TIME_MILLIS = setTime(TEST_TIME_SHORT);
		qci.add('7');
		ueList=getUES(dut, 1);
		ueNameListStc = convertUeToNamesList(ueList);
		preTest();
		TestProcess();
		AfterTest(TPT_THRESHOLD_PRECENT);
	}
	
	@Test
	@TestProperties(name = "TPT_Pt2P_512_Udp_Qci7", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void TPT_Pt2P_512_Udp_Qci7() throws Exception {
		testName = "Pt2P_512_Udp_Qci7";
		streamsMode = "PT2P";
		packetSize = 512;
		TEST_TIME_MILLIS = setTime(TEST_TIME_SHORT);
		qci.add('7');
		ueList=getUES(dut, 2);
		ueNameListStc = convertUeToNamesList(ueList);
		preTest();
		TestProcess();
		
		AfterTest(TPT_THRESHOLD_PRECENT);
	}
	
	@Test
	@TestProperties(name = "TPT_PtMP_1400_Udp_Qci7", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void TPT_PtMP_1400_Udp_Qci7() throws Exception {
		testName = "PtMP_1400_Udp_Qci7";
		streamsMode = "PTMP";
		packetSize = 1400;
		TEST_TIME_MILLIS = setTime(TEST_TIME_SHORT);
		qci.add('7');
		ueList=getUES(dut);
		ueNameListStc = convertUeToNamesList(ueList);
		preTest();
		TestProcess();
		AfterTest(TPT_THRESHOLD_PRECENT);
	}
	
	@Test
	@TestProperties(name = "TPT_PtMP_1024_Udp_Qci7", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void TPT_PtMP_1024_Udp_Qci7() throws Exception {
		testName = "PtMP_1024_Udp_Qci7";
		streamsMode = "PTMP";
		packetSize = 1024;
		TEST_TIME_MILLIS = setTime(TEST_TIME_SHORT);
		qci.add('7');
		ueList=getUES(dut);
		ueNameListStc = convertUeToNamesList(ueList);
		preTest();
		TestProcess();
		AfterTest(TPT_THRESHOLD_PRECENT);
	}
	
	@Test
	@TestProperties(name = "TPT_PtMP_800_Udp_Qci7", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void TPT_PtMP_800_Udp_Qci7() throws Exception {
		testName = "PtMP_800_Udp_Qci7";
		streamsMode = "PTMP";
		packetSize = 800;
		TEST_TIME_MILLIS = setTime(TEST_TIME_LONG);
		qci.add('7');
		ueList=getUES(dut);
		ueNameListStc = convertUeToNamesList(ueList);
		preTest();
		TestProcess();
		AfterTest(TPT_THRESHOLD_PRECENT);
	}
	
	@Test
	@TestProperties(name = "TPT_PtMP_1522_Udp_Qci7", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void TPT_PtMP_1522_Udp_Qci7() throws Exception {
		testName = "PtMP_1522_Udp_Qci7";
		streamsMode = "PTMP";
		packetSize = 1522;
		TEST_TIME_MILLIS = setTime(TEST_TIME_SHORT);
		qci.add('7');
		ueList=getUES(dut);
		ueNameListStc = convertUeToNamesList(ueList);
		preTest();
		TestProcess();
		AfterTest(TPT_THRESHOLD_PRECENT);
	}
	
//---------------------------------------------------Qci9------------------------------------------------------
	
	@Test
	@TestProperties(name = "TPT_PtP_512_Udp_Qci9", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void TPT_PtP_512_Udp_Qci9() throws Exception {
		testName = "PtP_512_Udp_Qci9";
		isQci9 = true;
		streamsMode = "PTP";
		packetSize = 512;
		TEST_TIME_MILLIS = setTime(TEST_TIME_SHORT);
		qci.add('9');
		ueList = getUES(dut, 1);
		ueNameListStc = convertUeToNamesList(ueList);
		preTest();
		TestProcess();
		
		AfterTest(TPT_THRESHOLD_PRECENT);
	}
	
	@Test
	@TestProperties(name = "TPT_Pt2P_512_Udp_Qci9", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void TPT_Pt2P_512_Udp_Qci9() throws Exception {
		testName = "Pt2P_512_Udp_Qci9";
		isQci9 = true;
		streamsMode = "PT2P";
		packetSize = 512;
		TEST_TIME_MILLIS = setTime(TEST_TIME_SHORT);
		qci.add('9');
		ueList=getUES(dut, 2);
		ueNameListStc = convertUeToNamesList(ueList);
		preTest();
		TestProcess();

		AfterTest(TPT_THRESHOLD_PRECENT);
	}
		
	@Test
	@TestProperties(name = "TPT_PtMP_1400_Udp_Qci9", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void TPT_PtMP_1400_Udp_Qci9() throws Exception {
		testName = "PtMP_1400_Udp_Qci9";
		isQci9 = true;
		streamsMode = "PTMP";
		packetSize = 1400;
		TEST_TIME_MILLIS = setTime(TEST_TIME_SHORT);
		qci.add('9');
		ueList=getUES(dut);
		ueNameListStc = convertUeToNamesList(ueList);
		preTest();
		TestProcess();
		AfterTest(TPT_THRESHOLD_PRECENT);
	}
	
	@Test
	@TestProperties(name = "TPT_PtMP_1024_Udp_Qci9", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void TPT_PtMP_1024_Udp_Qci9() throws Exception {
		testName = "PtMP_1024_Udp_Qci9";
		isQci9 = true;
		streamsMode = "PTMP";
		packetSize = 1024;
		TEST_TIME_MILLIS = setTime(TEST_TIME_SHORT);
		qci.add('9');
		ueList=getUES(dut);
		ueNameListStc = convertUeToNamesList(ueList);
		preTest();
		TestProcess();
		AfterTest(TPT_THRESHOLD_PRECENT);
	}
	
	@Test
	@TestProperties(name = "TPT_PtMP_800_Udp_Qci9", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void TPT_PtMP_800_Udp_Qci9() throws Exception {
		testName = "PtMP_800_Udp_Qci9";
		isQci9 = true;
		streamsMode = "PTMP";
		packetSize = 800;
		TEST_TIME_MILLIS = setTime(TEST_TIME_LONG);
		qci.add('9');
		ueList=getUES(dut);
		ueNameListStc = convertUeToNamesList(ueList);
		preTest();
		TestProcess();
		AfterTest(TPT_THRESHOLD_PRECENT);
	}
		
	@Test
	@TestProperties(name = "TPT_PtMP_1522_Udp_Qci9", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void TPT_PtMP_1522_Udp_Qci9() throws Exception {
		testName = "PtMP_1522_Udp_Qci9";
		isQci9 = true;
		streamsMode = "PTMP";
		packetSize = 1522;
		TEST_TIME_MILLIS = setTime(TEST_TIME_SHORT);
		qci.add('9');
		ueList=getUES(dut);
		ueNameListStc = convertUeToNamesList(ueList);
		preTest();
		TestProcess();
		AfterTest(TPT_THRESHOLD_PRECENT);
	}
	
//---------------------------------------------------Qci7&9------------------------------------------------------	
	
	@Test
	@TestProperties(name = "QOS_PtP_512_Udp_Qci7And9", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void TPT_PtP_512_Udp_Qci7And9() throws Exception {
		testName = "PtP_512_Udp_Qci7And9";
		isQci9 = true;
		streamsMode = "PTP";
		packetSize = 512;
		TEST_TIME_MILLIS = setTime(TEST_TIME_SHORT);
		qci.add('7');
		qci.add('9');
		ueList=getUES(dut, 1);
		ueNameListStc = convertUeToNamesList(ueList);
		preTest();
		TestProcess();
		
		AfterTest(QOS_THRESHOLD_PRECENT);
	}
	
	@Test
	@TestProperties(name = "QOS_Pt2P_512_Udp_Qci7And9", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void TPT_Pt2P_512_Udp_Qci7And9() throws Exception {
		testName = "Pt2P_512_Udp_Qci7And9";
		isQci9 = true;
		streamsMode = "PT2P";
		packetSize = 512;
		TEST_TIME_MILLIS = setTime(TEST_TIME_SHORT);
		qci.add('7');
		qci.add('9');
		ueList=getUES(dut, 2);
		ueNameListStc = convertUeToNamesList(ueList);
		preTest();
		TestProcess();
		AfterTest(QOS_THRESHOLD_PRECENT);
	}
	
	@Test
	@TestProperties(name = "QOS_PtMP_1400_Udp_Qci7And9", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void QOS_PtMP_1400_Udp_Qci7And9() throws Exception {
		testName = "PtMP_1400_Udp_Qci7And9";
		streamsMode = "PTMP";
		packetSize = 1400;
		TEST_TIME_MILLIS = setTime(TEST_TIME_SHORT);
		qci.add('7');
		qci.add('9');
		ueList=getUES(dut);
		ueNameListStc = convertUeToNamesList(ueList);
		preTest();
		TestProcess();
		AfterTest(QOS_THRESHOLD_PRECENT);
	}
	
	@Test
	@TestProperties(name = "QOS_PtMP_1024_Udp_Qci7And9", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void QOS_PtMP_1024_Udp_Qci7And9() throws Exception {
		testName = "PtMP_1024_Udp_Qci7And9";
		streamsMode = "PTMP";
		packetSize = 1024;
		TEST_TIME_MILLIS = setTime(TEST_TIME_SHORT);
		qci.add('7');
		qci.add('9');
		ueList=getUES(dut);
		ueNameListStc = convertUeToNamesList(ueList);
		preTest();
		TestProcess();
		AfterTest(QOS_THRESHOLD_PRECENT);
	}
	
	@Test
	@TestProperties(name = "QOS_PtMP_800_Udp_Qci7And9", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void QOS_PtMP_800_Udp_Qci7And9() throws Exception {
		testName = "PtMP_800_Udp_Qci7And9";
		streamsMode = "PTMP";
		packetSize = 800;
		TEST_TIME_MILLIS = setTime(TEST_TIME_SHORT);
		qci.add('7');
		qci.add('9');
		ueList=getUES(dut);
		ueNameListStc = convertUeToNamesList(ueList);
		preTest();
		TestProcess();
		AfterTest(QOS_THRESHOLD_PRECENT);
	}
	
	@Test
	@TestProperties(name = "QOS_PtMP_1522_Udp_Qci7And9", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void QOS_PtMP_1522_Udp_Qci7And9() throws Exception {
		testName = "PtMP_1522_Udp_Qci7And9";
		streamsMode = "PTMP";
		packetSize = 1522;
		TEST_TIME_MILLIS = setTime(TEST_TIME_SHORT);
		qci.add('7');
		qci.add('9');
		ueList=getUES(dut);
		ueNameListStc = convertUeToNamesList(ueList);
		preTest();
		TestProcess();
		AfterTest(QOS_THRESHOLD_PRECENT);
	}
}

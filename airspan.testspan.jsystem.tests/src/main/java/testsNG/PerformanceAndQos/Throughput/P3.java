package testsNG.PerformanceAndQos.Throughput;

import org.junit.Test;

import jsystem.framework.TestProperties;

public class P3 extends TPTBase {
	// -------------------------------------------QCI7---------------------------------------------------------------
	@Test
	@TestProperties(name = "TPT_PtP_2000_Udp_Qci7", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void TPT_PtP_2000_Udp_Qci7() throws Exception {
		testName = "PtP_2000_Udp_Qci7";
		streamsMode = "PTP";
		packetSize = 2000;
		TEST_TIME_MILLIS = setTime(TEST_TIME_SHORT);
		qci.add('7');
		ueList=getUES(dut, 1);
		ueNameListStc = convertUeToNamesList(ueList);
		preTest();
		TestProcess();
		AfterTest(TPT_THRESHOLD_PRECENT);
	}

	@Test
	@TestProperties(name = "TPT_Pt2P_2000_Udp_Qci7", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void TPT_Pt2P_2000_Udp_Qci7() throws Exception {
		testName = "Pt2P_2000_Udp_Qci7";
		streamsMode = "PT2P";
		packetSize = 2000;
		TEST_TIME_MILLIS = setTime(TEST_TIME_SHORT);
		qci.add('7');
		ueList=getUES(dut, 2);
		ueNameListStc = convertUeToNamesList(ueList);
		preTest();
		TestProcess();
		AfterTest(TPT_THRESHOLD_PRECENT);
	}
	
	@Test
	@TestProperties(name = "TPT_PtMP_256_Udp_Qci7", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void QOS_PtMP_256_Udp_Qci7() throws Exception {
		testName = "PtMP_256_Udp_Qci7";
		streamsMode = "PTMP";
		packetSize = 256;
		TEST_TIME_MILLIS = setTime(TEST_TIME_SHORT);
		qci.add('7');
		ueList=getUES(dut);
		ueNameListStc = convertUeToNamesList(ueList);
		preTest();
		TestProcess();
		AfterTest(QOS_THRESHOLD_PRECENT);
	}
	
	@Test
	@TestProperties(name = "TPT_PtMP_2000_Udp_Qci7", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void QOS_PtMP_2000_Udp_Qci7() throws Exception {
		testName = "PtMP_2000_Udp_Qci7";
		streamsMode = "PTMP";
		packetSize = 2000;
		TEST_TIME_MILLIS = setTime(TEST_TIME_SHORT);
		qci.add('7');
		ueList=getUES(dut);
		ueNameListStc = convertUeToNamesList(ueList);
		preTest();
		TestProcess();
		AfterTest(QOS_THRESHOLD_PRECENT);
	}

	// -------------------------------------------QCI9---------------------------------------------------------------
	@Test
	@TestProperties(name = "TPT_PtP_2000_Udp_Qci9", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void TPT_PtP_2000_Udp_Qci9() throws Exception {
		testName = "PtP_2000_Udp_Qci9";
		isQci9 = true;
		streamsMode = "PTP";
		packetSize = 2000;
		TEST_TIME_MILLIS = setTime(TEST_TIME_SHORT);
		qci.add('9');
		ueList=getUES(dut, 1);
		ueNameListStc = convertUeToNamesList(ueList);
		preTest();
		TestProcess();
		AfterTest(TPT_THRESHOLD_PRECENT);
	}

	@Test
	@TestProperties(name = "TPT_Pt2P_2000_Udp_Qci9", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void TPT_Pt2P_2000_Udp_Qci9() throws Exception {
		testName = "Pt2P_2000_Udp_Qci9";
		isQci9 = true;
		streamsMode = "PT2P";
		packetSize = 2000;
		TEST_TIME_MILLIS = setTime(TEST_TIME_SHORT);
		qci.add('9');
		ueList=getUES(dut, 2);
		ueNameListStc = convertUeToNamesList(ueList);
		preTest();
		TestProcess();
		AfterTest(TPT_THRESHOLD_PRECENT);
	}
	
	@Test
	@TestProperties(name = "QOS_PtMP_256_Udp_Qci9", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void QOS_PtMP_256_Udp_Qci9() throws Exception {
		testName = "PtMP_256_Udp_Qci9";
		streamsMode = "PTMP";
		packetSize = 256;
		TEST_TIME_MILLIS = setTime(TEST_TIME_SHORT);
		qci.add('9');
		ueList=getUES(dut);
		ueNameListStc = convertUeToNamesList(ueList);
		preTest();
		TestProcess();
		AfterTest(QOS_THRESHOLD_PRECENT);
	}
	
	@Test
	@TestProperties(name = "QOS_PtMP_2000_Udp_Qci9", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void QOS_PtMP_2000_Udp_Qci9() throws Exception {
		testName = "PtMP_2000_Udp_Qci9";
		streamsMode = "PTMP";
		packetSize = 2000;
		TEST_TIME_MILLIS = setTime(TEST_TIME_SHORT);
		qci.add('9');
		ueList=getUES(dut);
		ueNameListStc = convertUeToNamesList(ueList);
		preTest();
		TestProcess();
		AfterTest(QOS_THRESHOLD_PRECENT);
	}
	//-------------------------------------------QCI9And7---------------------------------------------------------------	
		@Test
		@TestProperties(name = "QOS_PtP_2000_Udp_Qci7And9", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
				"IsTestWasSuccessful" })
		public void QOS_PtP_2000_Udp_Qci7And9() throws Exception {
			testName = "PtP_2000_Udp_Qci7And9";
			streamsMode = "PTP";
			packetSize = 2000;
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
		@TestProperties(name = "QOS_Pt2P_2000_Udp_Qci7And9", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
				"IsTestWasSuccessful" })
		public void QOS_Pt2P_2000_Udp_Qci7And9() throws Exception {
			testName = "Pt2P_2000_Udp_Qci7And9";
			streamsMode = "PT2P";
			packetSize = 2000;
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
		@TestProperties(name = "QOS_PtMP_256_Udp_Qci7And9", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
				"IsTestWasSuccessful" })
		public void QOS_PtMP_256_Udp_Qci7And9() throws Exception {
			testName = "PtMP_256_Udp_Qci7And9";
			streamsMode = "PTMP";
			packetSize = 256;
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
		@TestProperties(name = "QOS_PtMP_2000_Udp_Qci7And9", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
				"IsTestWasSuccessful" })
		public void QOS_PtMP_2000_Udp_Qci7And9() throws Exception {
			testName = "PtMP_2000_Udp_Qci7And9";
			streamsMode = "PTMP";
			packetSize = 2000;
			TEST_TIME_MILLIS = setTime(TEST_TIME_SHORT);
			qci.add('7');
			qci.add('9');
			ueList=getUES(dut);
			ueNameListStc = convertUeToNamesList(ueList);
			preTest();
			TestProcess();
			AfterTest(QOS_THRESHOLD_PRECENT);
		}
		
		public void TPT_PtMP_800_Udp_Qci9_Timeless(Long time) throws Exception {
			testName = "PtMP_800_Udp_Qci9";
			isQci9 = true;
			streamsMode = "PTMP";
			packetSize = 800;
			TEST_TIME_MILLIS = time;
			qci.add('9');
			ueList=getUES(dut);
			ueNameListStc = convertUeToNamesList(ueList);
			preTest();
			TestProcess();
			AfterTest(TPT_THRESHOLD_PRECENT);
		}
		
		public void TPT_PtMP_1400_Udp_Qci9_Timeless(Long time) throws Exception {
			testName = "PtMP_1400_Udp_Qci9";
			isQci9 = true;
			streamsMode = "PTMP";
			packetSize = 1400;
			TEST_TIME_MILLIS = time;
			qci.add('9');
			ueList=getUES(dut);
			ueNameListStc = convertUeToNamesList(ueList);
			preTest();
			TestProcess();
			AfterTest(TPT_THRESHOLD_PRECENT);
		}
}

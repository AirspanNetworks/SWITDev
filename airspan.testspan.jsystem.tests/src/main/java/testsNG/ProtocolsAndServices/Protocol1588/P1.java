package testsNG.ProtocolsAndServices.Protocol1588;

import jsystem.framework.TestProperties;

public class P1 extends Base1588 {

	// @Test // 3
	@TestProperties(name = "Wrong_VLAN_ID ", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void wrongVlanId() {
	}

	// @Test // 4
	@TestProperties(name = "Wrong_IP_Address_Of_Grand_Master", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void wrongIpAddressOfGrandMaster() {
	}

	// @Test // 8
	@TestProperties(name = "Switching_Between_The_Grand_Masters", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void switchingBetweenTheGrandMasters() {
	}

	// @Test // 10
	@TestProperties(name = "Network_Delay_Matrix", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void networkDelayMatrix() {
	}
}

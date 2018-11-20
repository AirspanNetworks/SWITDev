package testsNG.ProtocolsAndServices.NeighborManagement;

import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;
import testsNG.Actions.EnodeBConfig;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Random;
import org.junit.Test;

import EnodeB.EnodeB;
import Netspan.NetspanServer;
import Netspan.API.Enums.HandoverType;
import Netspan.API.Enums.HoControlStateTypes;
import Netspan.API.Enums.X2ControlStateTypes;
import Utils.GeneralUtils;


public class P0 extends NeighborManagementBase {
	
	/* Configuring single X2 neighbor */
	@Test
	@TestProperties(name = "AddNGH_HOAllowed_X2CtrlAuto_HOTypeX2P", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {
			"IsTestWasSuccessful" })
	public void configuratingSingleX2Neighbor(){
		
		boolean status;
		tempEnodeB = DUT1;
		tempNeighbor = DUT2;
		
		GeneralUtils.startLevel("Changing flags configuration");
		changeConfigurationsFlagsToTrue(tempEnodeB,tempNeighbor);
		GeneralUtils.stopLevel();

		getDebugInfo(tempEnodeB);
		getDebugInfo(tempNeighbor);
		
		GeneralUtils.startLevel("Adding new neighbor");
		status = neighbor.addNeighbor(tempEnodeB, tempNeighbor, HoControlStateTypes.ALLOWED,
				X2ControlStateTypes.AUTOMATIC, HandoverType.TRIGGER_X_2, true, "0");
		GeneralUtils.stopLevel();
		
		if(!checkAndPrintStatusOfAdding(tempNeighbor, tempEnodeB, status)){
			return;
		}
		GeneralUtils.unSafeSleep(20000);
		GeneralUtils.startLevel("Verifying that neighbor was added with the right parameters");
		status = neighbor.verifyNeighborParametersNMSandSNMP(tempEnodeB, tempNeighbor, HoControlStateTypes.ALLOWED,X2ControlStateTypes.AUTOMATIC, HandoverType.TRIGGER_X_2, true, "0");
		
		GeneralUtils.stopLevel();
		
		checkAndPrintStatusOfVerification(status);

		getDebugInfo(tempEnodeB);
		getDebugInfo(tempNeighbor);
		
		postTest(tempEnodeB);
	}
	
	/* Deleting single X2 neighbor */
	@Test
	@TestProperties(name = "DeleteNGH_HOAllowed_X2CtrlAuto_HOTypeX2P", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {"IsTestWasSuccessful" })
	public void deletingSingleX2Neighbor(){

		boolean status;
		tempEnodeB = DUT1;
		tempNeighbor = DUT2;
		performPostTest = false;
		configuratingSingleX2Neighbor();
		performPostTest = true;
		
		GeneralUtils.startLevel("Deleting the enodeB that was added to test");
		neighbor.deleteNeighbor(tempEnodeB, tempNeighbor);
		GeneralUtils.stopLevel();
		
		GeneralUtils.unSafeSleep(20*1000);
		
		GeneralUtils.startLevel("Verifying neighbor was deleted");
		status = neighbor.verifyNeighborExistsNSOrSNMP(tempEnodeB,tempNeighbor);
		GeneralUtils.stopLevel();
		
		checkAndPrintStatusOfDeleting(tempNeighbor, tempEnodeB, status);
		
		getDebugInfo(tempEnodeB);
		getDebugInfo(tempNeighbor);
		
		postTest(tempEnodeB);
	}
	
	/* Configuring single S1 neighbor */
	@Test
	@TestProperties(name = "AddNGH_HOAllowed_X2CtrlAuto_HOTypeS1Only", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {"IsTestWasSuccessful" })
	public void configuringSingleS1Neighbor(){

		boolean status;
		tempEnodeB = DUT1;
		tempNeighbor = DUT2;
		GeneralUtils.startLevel("Changing flags configuration");
		changeConfigurationsFlagsToTrue(tempEnodeB,tempNeighbor);
		GeneralUtils.stopLevel();

		getDebugInfo(tempEnodeB);
		getDebugInfo(tempNeighbor);
		
		GeneralUtils.startLevel("Adding new neighbor");
		status = neighbor.addNeighbor(tempEnodeB, tempNeighbor, HoControlStateTypes.ALLOWED,X2ControlStateTypes.AUTOMATIC, HandoverType.S_1_ONLY, true, "0");
		GeneralUtils.stopLevel();

		if(!checkAndPrintStatusOfAdding(tempNeighbor, tempEnodeB, status)){
			return;
		}
		GeneralUtils.unSafeSleep(20000);
		GeneralUtils.startLevel("Verifying that neighbor was added with the right parameters");
		status = neighbor.verifyNeighborParametersNMSandSNMP(tempEnodeB, tempNeighbor, HoControlStateTypes.ALLOWED,
				X2ControlStateTypes.AUTOMATIC, HandoverType.S_1_ONLY, true, "0");
		GeneralUtils.stopLevel();

		checkAndPrintStatusOfVerification(status);
		
		getDebugInfo(tempEnodeB);
		getDebugInfo(tempNeighbor);
		
		postTest(tempEnodeB);
	}

	/* Deleting single S1 neighbor */
	@Test
	@TestProperties(name = "DeleteNGH_HOAllowed_X2CtrlAuto_HOTypeS1Only", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {"IsTestWasSuccessful" })
	public void deletingSingleS1Neighbor(){
		
		boolean status;
		tempEnodeB = DUT1;
		tempNeighbor = DUT2;
		
		performPostTest = false;
		configuringSingleS1Neighbor();
		performPostTest = true;
		
		GeneralUtils.startLevel("Deleting the enodeB that was added to test");
		neighbor.deleteNeighbor(tempEnodeB, tempNeighbor);
		GeneralUtils.stopLevel();
		
		GeneralUtils.unSafeSleep(20*1000);
		
		GeneralUtils.startLevel("Verifying neighbor was deleted");
		status = neighbor.verifyNeighborExistsNSOrSNMP(tempEnodeB, tempNeighbor);
		GeneralUtils.stopLevel();

		checkAndPrintStatusOfDeleting(tempNeighbor, tempEnodeB, status);

		getDebugInfo(tempEnodeB);
		getDebugInfo(tempNeighbor);
		
		postTest(tempEnodeB);
	}

	/* Configuring single S1 neighbor (X2 control disable mode) */
	@Test
	@TestProperties(name = "AddNGH_HOAllowed_X2CtrlProhibit", returnParam = {"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void configuringSingleS1Neighbor_X2ControlDisableMode(){
		boolean status;
		tempEnodeB = DUT1;
		tempNeighbor = DUT2;

		getDebugInfo(tempEnodeB);
		getDebugInfo(tempNeighbor);
		
		GeneralUtils.startLevel("Adding new neighbor");
		status = neighbor.addNeighbor(tempEnodeB, tempNeighbor, HoControlStateTypes.ALLOWED,X2ControlStateTypes.NOT_ALLOWED, HandoverType.S_1_ONLY, true, "0");
		GeneralUtils.stopLevel();

		if(!checkAndPrintStatusOfAdding(tempNeighbor, tempEnodeB, status)){
			return;
		}
		GeneralUtils.unSafeSleep(20000);
		GeneralUtils.startLevel("Verifying that neighbor was added with the right parameters");
		status = neighbor.verifyNeighborParametersNMSandSNMP(tempEnodeB, tempNeighbor, HoControlStateTypes.ALLOWED,
				X2ControlStateTypes.NOT_ALLOWED, HandoverType.S_1_ONLY, true, "0");
		GeneralUtils.stopLevel();
		
		checkAndPrintStatusOfVerification(status);

		getDebugInfo(tempEnodeB);
		getDebugInfo(tempNeighbor);
		
		postTest(tempEnodeB);
	}

	/* Deleting single S1 neighbor (X2 control disable mode) */
	@Test
	@TestProperties(name = "DeleteNGH_HOAllowed_X2CtrlProhibit", returnParam = {"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void deletingSingleS1Neighbor_X2ControlDisableMode(){

		boolean status;
		tempEnodeB = DUT1;
		tempNeighbor = DUT2;

		performPostTest = false;
		configuringSingleS1Neighbor_X2ControlDisableMode();
		performPostTest = true;
		
		GeneralUtils.startLevel("Deleting the enodeB that was added to test");
		neighbor.deleteNeighbor(tempEnodeB, tempNeighbor);
		GeneralUtils.stopLevel();
		
		GeneralUtils.unSafeSleep(20*1000);
		
		GeneralUtils.startLevel("Verifying neighbor was deleted");
		status = neighbor.verifyNeighborExistsNSOrSNMP(tempEnodeB, tempNeighbor);
		GeneralUtils.stopLevel();

		checkAndPrintStatusOfDeleting(tempNeighbor, tempEnodeB, status);

		getDebugInfo(tempEnodeB);
		getDebugInfo(tempNeighbor);
		
		postTest(tempEnodeB);
	}

	/* Configuring single X2 neighbor (HO not allowed) */
	@Test
	@TestProperties(name = "AddNGH_HOProhibit_X2CtrlAuto", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void configuringSingleX2Neighbor_HONotAllowed(){

		boolean status;
		tempEnodeB = DUT1;
		tempNeighbor = DUT2;

		getDebugInfo(tempEnodeB);
		getDebugInfo(tempNeighbor);
		
		GeneralUtils.startLevel("Adding new neighbor");
		status = neighbor.addNeighbor(tempEnodeB, tempNeighbor, HoControlStateTypes.PROHIBITED,
				X2ControlStateTypes.AUTOMATIC, HandoverType.TRIGGER_X_2, true, "0");
		GeneralUtils.stopLevel();

		if(!checkAndPrintStatusOfAdding(tempNeighbor, tempEnodeB, status)){
			return;
		}
		GeneralUtils.unSafeSleep(20000);
		GeneralUtils.startLevel("Verifying that neighbor was added with the right parameters");
		status = neighbor.verifyNeighborParametersNMSandSNMP(tempEnodeB, tempNeighbor, HoControlStateTypes.PROHIBITED,
				X2ControlStateTypes.AUTOMATIC, HandoverType.TRIGGER_X_2, true, "0");
		GeneralUtils.stopLevel();
		
		checkAndPrintStatusOfVerification(status);

		getDebugInfo(tempEnodeB);
		getDebugInfo(tempNeighbor);
		
		postTest(tempEnodeB);
	}

	/* Deleting single X2 neighbor (HO not allowed) */
	@Test
	@TestProperties(name = "DeleteNGH_HOProhibit_X2CtrlAuto", returnParam = {"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void deletingSingleX2Neighbor_HONotAllowed(){

		boolean status;
		tempEnodeB = DUT1;
		tempNeighbor = DUT2;

		performPostTest = false;
		configuringSingleX2Neighbor_HONotAllowed();
		performPostTest = true;
		
		GeneralUtils.startLevel("Deleting the enodeB that was added to test");
		neighbor.deleteNeighbor(tempEnodeB, tempNeighbor);
		GeneralUtils.stopLevel();
		
		GeneralUtils.unSafeSleep(20*1000);
		
		GeneralUtils.startLevel("Verifying neighbor was deleted");
		status = neighbor.verifyNeighborExistsNSOrSNMP(tempEnodeB, tempNeighbor);
		GeneralUtils.stopLevel();

		checkAndPrintStatusOfDeleting(tempNeighbor, tempEnodeB, status);

		getDebugInfo(tempEnodeB);
		getDebugInfo(tempNeighbor);
		
		postTest(tempEnodeB);
	}
	
	/*Configuring single S1 neighbor (HO not allowed)*/
	@Test
	@TestProperties(name = "AddNGH_HOProhibit_X2CtrlProhibit", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void configuringSingleS1Neighbor_HONotAllowed(){

		boolean status;
		tempEnodeB = DUT1;
		tempNeighbor = DUT2;
		
		getDebugInfo(tempEnodeB);
		getDebugInfo(tempNeighbor);
		
		GeneralUtils.startLevel("Adding new neighbor");
		status = neighbor.addNeighbor(tempEnodeB, tempNeighbor, HoControlStateTypes.PROHIBITED,
				X2ControlStateTypes.NOT_ALLOWED, HandoverType.TRIGGER_X_2, true, "0");
		GeneralUtils.stopLevel();

		if(!checkAndPrintStatusOfAdding(tempNeighbor, tempEnodeB, status)){
			return;
		}
		GeneralUtils.unSafeSleep(20000);
		GeneralUtils.startLevel("Verifying that neighbor was added with the right parameters");
		status = neighbor.verifyNeighborParametersNMSandSNMP(tempEnodeB, tempNeighbor, HoControlStateTypes.PROHIBITED,
				X2ControlStateTypes.NOT_ALLOWED, HandoverType.TRIGGER_X_2, true, "0");
		GeneralUtils.stopLevel();
		
		checkAndPrintStatusOfVerification(status);
		
		getDebugInfo(tempEnodeB);
		getDebugInfo(tempNeighbor);
		
		postTest(tempEnodeB);
	}
		
	/* Deleting single S1 neighbor (HO not allowed) */
	@Test
	@TestProperties(name = "DeleteNGH_HOProhibit_X2CtrlProhibit", returnParam = {
			"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void deletingSingleS1Neighbor_HONotAllowed(){

		boolean status;
		tempEnodeB = DUT1;
		tempNeighbor = DUT2;
		
		performPostTest = false;
		configuringSingleS1Neighbor_HONotAllowed();
		performPostTest = true;

		GeneralUtils.startLevel("Deleting the enodeB that was added to test");
		neighbor.deleteNeighbor(tempEnodeB, tempNeighbor);
		GeneralUtils.stopLevel();
		
		GeneralUtils.unSafeSleep(20*1000);
		
		GeneralUtils.startLevel("Verifying neighbor was deleted");
		status = neighbor.verifyNeighborExistsNSOrSNMP(tempEnodeB, tempNeighbor);
		GeneralUtils.stopLevel();

		checkAndPrintStatusOfDeleting(tempNeighbor, tempEnodeB, status);
		
		getDebugInfo(tempEnodeB);
		getDebugInfo(tempNeighbor);
		
		postTest(tempEnodeB);
	}

	/* Five neighbors Configuration Matrix */
	@Test
	@TestProperties(name = "AddNGH_FiveNgh_Mixed_Ctrl", returnParam = {"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void fiveNeighborsConfigurationMatrix(){
		int neighborConf = 0;
		realNeighborsList = new ArrayList<EnodeB>();
		virtualNeighborsList = new ArrayList<EnodeB>();
		allNeighbors = new ArrayList<>();
		tempEnodeB = DUT1;
		tempNeighbor = DUT2;
		
		getDebugInfo(tempEnodeB);
		getDebugInfo(tempNeighbor);
		
		GeneralUtils.startLevel("Adding neighbors to enodeB");
		// adding the real neighbors to list
		int NumberOfRealNeighbors = otherEnb.size() - 1;
		for (int i = 0; i <= NumberOfRealNeighbors; i++) {
			realNeighborsList.add(otherEnb.get(i));
		}

		int numberOfVirtualEnodeBs = 5 - realNeighborsList.size();
		// adding 3rd party enodebs to the 3rd party list in Netspan
		GeneralUtils.startLevel("Adding 3rd parties to Netspan");
		virtualNeighborsList = neighbor.addingThirdPartyNeihbors(tempEnodeB, numberOfVirtualEnodeBs, false, null);
		GeneralUtils.stopLevel();
		GeneralUtils.startLevel("Adding real neighbors to enodeB");
		// adding real neighbors to the neighbor list
		allNeighbors.addAll(realNeighborsList);
		allNeighbors.addAll(virtualNeighborsList);
		
		if (allNeighbors.size() != 0) {
			for (EnodeB enb : allNeighbors) {
					NeighborConf conf = NeighborConf.determineNeighborConf(neighborConf);
					neighborConf++;
				if (!neighbor.addNeighbor(tempEnodeB, enb, conf.getHoControlStatType(), conf.getX2ControlStatType(),conf.getHoType(), conf.isStatic(), "0")) {
					report.report("Neighbor " + enb.getNetspanName() + " was not added to the neighbor list",Reporter.FAIL);
					reason="FAIL: At least one neighbor was not added to the neighbor list\n";
				}
				else{
					report.report("Neighbor: " + enb.getNetspanName() + " was added to the enodeb: "+ tempEnodeB.getNetspanName()+ " as expected");
				}
				if(neighborConf >= LAST_NEIGHBOR_CONFIGURATION){
					neighborConf = 0;
				}
			}
		}else{
				report.report("There are no neighbors to add to the EnodeB", Reporter.FAIL);
				reason="FAIL: There are no neighbors to add to the EnodeB\n";
		}
		GeneralUtils.stopLevel();
		GeneralUtils.stopLevel();
		GeneralUtils.unSafeSleep(30000);
		
		//verify neighbors were added with right parameters
		GeneralUtils.startLevel("Verifying that neighbors were added with the right parameters");
		neighborConf = 0;
		for (EnodeB enb : allNeighbors) {
			NeighborConf conf = NeighborConf.determineNeighborConf(neighborConf);
			neighborConf++;
			if (!neighbor.verifyNeighborParametersNMSandSNMP(tempEnodeB, enb, conf.getHoControlStatType(), conf.getX2ControlStatType(), conf.getHoType(), conf.isStatic(), "0")) {
				report.report("Some of the parameters for neighbor " + enb.getNetspanName() + " were added with wrong values or neighbor wasn't added at all",Reporter.FAIL);
				reason="FAIL: At least one neighbor was added with wrong parameters or was not added at all\n";
			}
			else{
				report.report("Neighbor "+enb.getNetspanName()+" was verified and added with right values");				
			}
			if(neighborConf >= LAST_NEIGHBOR_CONFIGURATION){
				neighborConf = 0;
			}
		}
		
		GeneralUtils.stopLevel();
		
		getDebugInfo(tempEnodeB);
		getDebugInfo(tempNeighbor);
		
		postTest(tempEnodeB);
	}

	/* Five neighbors Deletion Matrix */
	@Test
	@TestProperties(name = "DeleteNGH_FiveNgh_Mixed_Ctrl", returnParam = {"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void fiveNeighborsDeletionMatrix(){
		realNeighborsList = new ArrayList<EnodeB>();
		virtualNeighborsList = new ArrayList<EnodeB>();
		allNeighbors = new ArrayList<>();
		Random rand = new Random();
		int randNum;
		//String neighborsName;
		tempEnodeB = DUT1;
		tempNeighbor = DUT2;
		
		performPostTest = false;
		fiveNeighborsConfigurationMatrix();
		performPostTest = true;
		
		//deleting all neighbors 
		GeneralUtils.startLevel("Deleting all neighbors from EnodeB");
		int neighborsSize = allNeighbors.size();
		int tempsize = neighborsSize;
		EnodeB neighborToDelete = null;
		for(int i = 0; i< neighborsSize; i++){
			randNum = rand.nextInt(tempsize);
			neighborToDelete = allNeighbors.get(randNum);
			if (neighbor.deleteNeighbor(tempEnodeB, neighborToDelete)){
				allNeighbors.remove(randNum);
				tempsize--;
				report.report("Neighbor " + neighborToDelete.getNetspanName() + " was deleted from EnodeB");
			}
			else{ 
				report.report("Neighbor " + neighborToDelete.getNetspanName() + " was NOT deleted from EnodeB", Reporter.FAIL);
				reason="FAIL: At least one neighbor was NOT deleted from EnodeB\n";
			}
		}
		GeneralUtils.stopLevel();
		
		getDebugInfo(tempEnodeB);
		getDebugInfo(tempNeighbor);

		postTest(tempEnodeB);
	}
	
	@Test
	@TestProperties(name = "Configuring_above_Maximum_Allowed_X2_Neighbors", returnParam = {"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void configuringAboveMaximumAllowedX2Neighbors() throws UnknownHostException{
		virtualNeighborsList = new ArrayList<EnodeB>();
		
		tempEnodeB = DUT1;
		tempNeighbor = DUT2;

		getDebugInfo(tempEnodeB);
		getDebugInfo(tempNeighbor);
		
		GeneralUtils.startLevel("Adding maximum ("+MAXIMUM_ALLOWED_NEIGHBORS+") neighbors to "+tempEnodeB.getNetspanName());

		// adding 3rd party enodebs to the 3rd party list in Netspan
		GeneralUtils.startLevel("Adding "+MAXIMUM_ALLOWED_NEIGHBORS+" 3rd parties to Netspan");
		virtualNeighborsList = neighbor.addingThirdPartyNeihbors(tempEnodeB, MAXIMUM_ALLOWED_NEIGHBORS, false, null);
		GeneralUtils.stopLevel();
		
		GeneralUtils.startLevel("Adding "+MAXIMUM_ALLOWED_NEIGHBORS+" neighbors to enodeB");
		
		for (EnodeB enb : virtualNeighborsList) {
			if (!neighbor.addNeighborOnlyNetspan(tempEnodeB, enb, HoControlStateTypes.ALLOWED, X2ControlStateTypes.AUTOMATIC,
					HandoverType.TRIGGER_X_2, true, "0")) {
				report.report("Neighbor " + enb.getNetspanName() + " was not added to the neighbor list",
						Reporter.WARNING);
				reason = "At least one neighbor was not added to the neighbor list\n";
			} else {
				report.report("Neighbor " + enb.getNetspanName() + " was added to the neighbor list");
			}
		}
		GeneralUtils.stopLevel();
		GeneralUtils.stopLevel();

		report.report("Wait 20 seconds");
		GeneralUtils.unSafeSleep(1000 * 20);
		
		// adding 1 neighbor beyond the maximum
		GeneralUtils.startLevel("Adding 1 neighbor beyond the maximum");
		
		EnodeB newParty = neighbor.adding3rdPartyNeighbor(tempEnodeB, InetAddress.getLocalHost().getHostName()+"_Above_Maximum_256","20.99.59.75",0,256,0);
		if(newParty == null){
			report.report("Couldnt create 3rd party neighbor - Fail Test!",Reporter.FAIL);
			GeneralUtils.stopLevel();
			return;
		}
		boolean flag = neighbor.checkCannotAddNeighbor(tempEnodeB, newParty, HoControlStateTypes.ALLOWED, X2ControlStateTypes.AUTOMATIC, HandoverType.TRIGGER_X_2, true, "0");
		GeneralUtils.stopLevel();
		
		if(flag == false) {
			report.report("Neighbor " + newParty.getNetspanName() + " was added to the neighbor list", Reporter.FAIL);
			reason = "Additional neighbor was configured";
		}
		else {
		report.report("Neighbor " + newParty.getNetspanName() + " was not added to the neighbor list");
		}

		getDebugInfo(tempEnodeB);
		getDebugInfo(tempNeighbor);
		
		postTest(tempEnodeB);
	}
	
	@Test
	@TestProperties(name = "DeleteNGH_MaxNgh_Mixed_Ctrl", returnParam = {"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void deletingMaximumAllowedNeighbors(){

		allNeighbors = new ArrayList<EnodeB>();
		Random rand = new Random();
		int randNum;
		//String neighborsName;
		tempEnodeB = DUT1;
		tempNeighbor = DUT2;
		boolean status = false;

		performPostTest = false;
		configurationMaximumAllowedNeighborsOneCell();
		performPostTest = true;
		
		GeneralUtils.unSafeSleep(45*1000);
		report.report("Wait 45 Second for neighbors to delete");
		GeneralUtils.startLevel("Deleting all neighbors");
		int neighborsSize = allNeighbors.size();
		int tempsize = neighborsSize;
		EnodeB tempNeighborDebug = null;
		int deletedNbrCounter=0;
		for(int i = 0; i< neighborsSize; i++){
			randNum = rand.nextInt(tempsize);
			tempNeighborDebug = allNeighbors.get(randNum);
			//tempNeighbor.setNetspanName(neighborsName);
			if (neighbor.deleteNeighbor(tempEnodeB, tempNeighborDebug)){
				allNeighbors.remove(randNum);
				tempsize--;
				deletedNbrCounter++;
				report.report("Neighbor " + tempNeighborDebug.getNetspanName() + " was deleted from EnodeB");
			}
			else{
				report.report("Neighbor " + tempNeighborDebug.getNetspanName() + " was NOT deleted from EnodeB", Reporter.FAIL);
				reason="FAIL: At least one neighbor was NOT deleted from EnodeB";
			}

		}
		GeneralUtils.stopLevel();
		GeneralUtils.unSafeSleep(30*1000);
		report.report("Total Neighbours deleted: "+deletedNbrCounter);
		GeneralUtils.startLevel("Checking that all neighbors where deleted");
		try {
			status = NetspanServer.getInstance().verifyNoNeighbors(tempEnodeB);
		} catch (Exception e) {
			e.printStackTrace();
		}
		GeneralUtils.stopLevel();
		if (!status){
			report.report("There are neighbors that didn't delete", Reporter.FAIL);
			reason="FAIL: There are neighbors that didn't delete\n";
		}else{
			report.report("All neighbors were deleted");
		}
	
		getDebugInfo(tempEnodeB);
		getDebugInfo(tempNeighbor);
		
		postTest(tempEnodeB);
	}

	@Test
	@TestProperties(name = "AddNGH_Maximum_Neighbors_Mixed_Ctrl_Per_Cell", returnParam = {"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void configurationMaximumAllowedNeighbors() throws UnknownHostException{
		performPostTest = false;
		int numberOfActiveCells = EnodeBConfig.getInstance().getNumberOfActiveCells(DUT1);
		report.report(DUT1.getNetspanName() + " Number Of Active Cells: " + numberOfActiveCells);
		for (int i = 1; i <= numberOfActiveCells; i++) {
			if (numberOfActiveCells == i) {
				performPostTest = true;
			}
			GeneralUtils.startLevel("Configuring Maximum Allowed Neighbors for Cell ID " + i);
			DUT1.setCellContextNumber(i);
			configurationMaximumAllowedNeighborsOneCell();
			neighbor.deleteAllNeighbors(tempEnodeB);
			neighbor.deleteAll3rdParty();
			GeneralUtils.stopLevel();
		}
	}
	
	//@Test
	@TestProperties(name = "Add_Maximum_X2_Neighbors_Per_Cell_Via_IPG", returnParam = {"IsTestWasSuccessful" }, paramsExclude = { "IsTestWasSuccessful" })
	public void configurationMaximumAllowedNeighborsPerCellViaIPG() throws Exception {
		performPostTest = false;
		int numberOfActiveCells = EnodeBConfig.getInstance().getNumberOfActiveCells(DUT1);
		report.report(DUT1.getNetspanName() + " Number Of Active Cells: " + numberOfActiveCells);
		for (int i = 1; i <= numberOfActiveCells; i++) {
			if (numberOfActiveCells == i) {
				performPostTest = true;
			}
			GeneralUtils.startLevel("Configuring Maximum Allowed Neighbors for Cell ID " + i + " Via IPG");
			DUT1.setCellContextNumber(i);
			configurationMaximumAllowedNeighborsViaIPG();
			neighbor.deleteAllNeighbors(tempEnodeB);
			neighbor.deleteAll3rdParty();
			GeneralUtils.stopLevel();
		}
	}
}
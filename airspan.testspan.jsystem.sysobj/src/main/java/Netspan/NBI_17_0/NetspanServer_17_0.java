package Netspan.NBI_17_0;

import java.util.HashMap;
import java.util.List;

import EnodeB.EnodeB;
import Netspan.EnbProfiles;
import Netspan.NBIVersion;
import Netspan.NBI_16_5.NetspanServer_16_5;
import Netspan.NBI_17_0.Lte.*;
import Netspan.Profiles.ConnectedUETrafficDirection;
import UeSimulator.Amarisoft.JsonObjects.ConfigFile.Cell;
import Utils.GeneralUtils;
import Utils.GeneralUtils.CellToUse;
import jsystem.framework.report.Reporter;


public class NetspanServer_17_0 extends NetspanServer_16_5 implements Netspan_17_0_abilities{
	public SoapHelper soapHelper_17_0;
	private static final String USERNAME = "wsadmin";
	private static final String PASSWORD = "password";
	private static final Netspan.NBI_17_0.Lte.Credentials credentialsLte = new Netspan.NBI_17_0.Lte.Credentials();
	private static final Netspan.NBI_17_0.Inventory.Credentials credentialsInventory = new Netspan.NBI_17_0.Inventory.Credentials();
	private static final Netspan.NBI_17_0.FaultManagement.Credentials credentialsFaultManagement = new Netspan.NBI_17_0.FaultManagement.Credentials();
	private static final Netspan.NBI_17_0.Statistics.Credentials credentialsStatistics = new Netspan.NBI_17_0.Statistics.Credentials();
	private static final Netspan.NBI_17_0.Status.Credentials credentialsStatus = new Netspan.NBI_17_0.Status.Credentials();
	private static final Netspan.NBI_17_0.Server.Credentials credentialsServer = new Netspan.NBI_17_0.Server.Credentials();
	private static final Netspan.NBI_17_0.Backhaul.Credentials credentialsBackhaul = new Netspan.NBI_17_0.Backhaul.Credentials();
	private static final Netspan.NBI_17_0.Software.Credentials credentialsSoftware = new Netspan.NBI_17_0.Software.Credentials();

	@Override
	public void init() throws Exception {
		if (NBI_VERSION == null) {
			NBI_VERSION = NBIVersion.NBI_17_0;
		}
		super.init();
	}
	
	/**
     * Init soap  helper objects.
     *
     * @throws Exception the exception
     */
    @Override
    public void initSoapHelper() throws Exception{
    	super.initSoapHelper();
    	this.soapHelper_17_0 = new SoapHelper(getHostname());
		credentialsLte.setUsername(USERNAME);
		credentialsLte.setPassword(PASSWORD);
		credentialsInventory.setUsername(USERNAME);
		credentialsInventory.setPassword(PASSWORD);
		credentialsFaultManagement.setUsername(USERNAME);
		credentialsFaultManagement.setPassword(PASSWORD);
		credentialsStatistics.setUsername(USERNAME);
		credentialsStatistics.setPassword(PASSWORD);
		credentialsStatus.setUsername(USERNAME);
		credentialsStatus.setPassword(PASSWORD);
		credentialsServer.setUsername(USERNAME);
		credentialsServer.setPassword(PASSWORD);
		credentialsBackhaul.setUsername(USERNAME);
		credentialsBackhaul.setPassword(PASSWORD);
		credentialsSoftware.setUsername(USERNAME);
		credentialsSoftware.setPassword(PASSWORD);
    }
    
    @Override
    public HashMap<ConnectedUETrafficDirection, HashMap<Integer, Integer>> getUeConnectedPerCategory(EnodeB enb) {
        Netspan.NBI_17_0.Status.LteUeGetResult lteUeGetResult;
        HashMap<ConnectedUETrafficDirection, HashMap<Integer, Integer>> ret = new HashMap<>();
        HashMap<Integer, Integer> ulData = new HashMap<>();
        HashMap<Integer, Integer> dlData = new HashMap<>();
        
        try {
            lteUeGetResult = soapHelper_17_0.getStatusSoap()
                .enbConnectedUeStatusGet(enb.getNetspanName(), credentialsStatus);
            if (lteUeGetResult.getErrorCode() != Netspan.NBI_17_0.Status.ErrorCodes.OK) {
                report.report("enbConnectedUeStatusGet via Netspan Failed : " + lteUeGetResult.getErrorString(),
                    Reporter.WARNING);
                return ret;
            }
            for (Netspan.NBI_17_0.Status.LteUeStatusWs currentCell : lteUeGetResult.getCell()) {
                if (currentCell.getCellId().getValue() == enb.getCellContextID()) {
                    List<Netspan.NBI_17_0.Status.LteUeCategory> catDataList = currentCell.getCategoryData();
                	ulData.clear();
                	dlData.clear();
                    for (Netspan.NBI_17_0.Status.LteUeCategory catData : catDataList) {
                		dlData.put(catData.getCategory().getValue(), catData.getConnectedUesDl().getValue());
                		ulData.put(catData.getCategory().getValue(), catData.getConnectedUesUl().getValue());
                    }
                	ret.put(ConnectedUETrafficDirection.UL, ulData);
                	ret.put(ConnectedUETrafficDirection.DL, dlData);
                    GeneralUtils.printToConsole(
                        "enbConnectedUeStatusGet via Netspan for eNodeB " + enb.getNetspanName() + " succeeded");
                }
            }

        } catch (Exception e) {
            report.report("enbConnectedUeStatusGet via Netspan Failed due to: " + e.getMessage(), Reporter.WARNING);
            e.printStackTrace();
            return new HashMap<ConnectedUETrafficDirection, HashMap<Integer, Integer>>();
        } finally {
        	soapHelper_17_0.endStatusSoap();
        }
        return ret;
    }
    
    @Override
    public boolean setMultiCellState(EnodeB node, Boolean isEnabled) {
        LteEnbDetailsSetWs enbConfigSet = new LteEnbDetailsSetWs();
        LteCellSetWs lteCellSet = new LteCellSetWs();
        ObjectFactory factoryDetails = new ObjectFactory();
        lteCellSet.setCellNumber(factoryDetails.createLteCellSetWsCellNumber("2"));
        lteCellSet.setIsEnabled(factoryDetails.createLteCellGetWsIsEnabled(isEnabled));
        enbConfigSet.getLteCell().add(lteCellSet);
        return setNodeConfig(node, enbConfigSet);
    }
    
    private boolean setNodeConfig(EnodeB enodeB, LteEnbDetailsSetWs netspanConfig) {
        try {
            if (getPnpNode(enodeB.getNetspanName()) != null) {
                GeneralUtils
                    .printToConsole("eNodeB " + enodeB.getNetspanName() + " is located in PnP list, skipping set.");
                return true;
            }
            GeneralUtils.printToConsole("Sending NBI requeset \"enbConfigSet\" for eNodeB " + enodeB.getNetspanName());
            Netspan.NBI_17_0.Lte.NodeActionResult result = soapHelper_17_0
                .getLteSoap().enbConfigSet(enodeB.getNetspanName(),null, netspanConfig, null, credentialsLte);
            GeneralUtils.printToConsole(String.format("NBI method \"enbConfigSet\" for eNodeB %s returned value: %s",
                enodeB.getNetspanName(), result.getErrorCode() + ":" + result.getErrorString()));
            if (result.getErrorCode() == Netspan.NBI_17_0.Lte.ErrorCodes.OK)
                return true;
            else {
                for (Netspan.NBI_17_0.Lte.NodeResult node : result.getNode()) {
                    if (node.getNodeResultCode() == NodeResultValues.NODE_ERROR) {
                        report.report("[NMS] ERROR in " + node.getName() + ": " + node.getNodeResultString());
                    }
                }
            }
        } catch (Exception e) {
            report.report("enbConfigSet via Netspan Failed due to: " + e.getMessage(), Reporter.WARNING);
            e.printStackTrace();
            return false;
        } finally {
            soapHelper_17_0.endLteSoap();
        }
        return false;
    }

    @Override
    public boolean changeCellToUse(EnodeB node, CellToUse cellToUse) {
        LteEnbDetailsSetWs enbConfigSet = new LteEnbDetailsSetWs();
        ObjectFactory factoryDetails = new ObjectFactory();
        CellToUseValues cellToUseParam = null;
        if (cellToUse == CellToUse.MULTI_CELL){
            cellToUseParam = CellToUseValues.MULTI_CELL;
        }
        else{
            cellToUseParam = CellToUseValues.LITE_COMP;
        }
        enbConfigSet.setCellToUse(factoryDetails.createLteEnbDetailsSetWsPnpParamsCellToUse(cellToUseParam));
        return setNodeConfig(node, enbConfigSet);
    }

    private LteEnbDetailsSetWs addProfile(EnodeB node, int cellNumber, EnbProfiles profileType, String profileName,
                                          LteEnbDetailsSetWs enbConfigSet) {
        switch (profileType) {
            case System_Default_Profile:
                enbConfigSet.setSystemDefaultProfile(profileName);
                break;
            case Management_Profile:
                enbConfigSet.setManagementProfile(profileName);
                break;

            case Mobility_Profile:
                if (cellNumber < 1) {
                    report.report("Cell ID need to be greater than 0", Reporter.WARNING);
                    return null;
                }
                getActiveIfActiveCell(enbConfigSet, cellNumber).setMobilityProfile(profileName);
                break;

            case Radio_Profile:
                if (cellNumber < 1) {
                    report.report("Cell ID need to be greater than 0", Reporter.WARNING);
                    return null;
                }
                getActiveIfActiveCell(enbConfigSet, cellNumber).setRadioProfile(profileName);
                break;

            case Network_Profile:
                enbConfigSet.setNetworkProfile(profileName);
                break;
            case Security_Profile:
                enbConfigSet.setSecurityProfile(profileName);
                break;
            case Son_Profile:
                enbConfigSet.setSonProfile(profileName);
                break;
            case Sync_Profile:
                enbConfigSet.setSyncProfile(profileName);
                break;
            case EnodeB_Advanced_Profile:
                enbConfigSet.setAdvancedConfigProfile(profileName);
                break;
            case Cell_Advanced_Profile:
                if (cellNumber < 1) {
                    report.report("Cell ID need to be greater than 0", Reporter.WARNING);
                    return null;
                }
                getActiveIfActiveCell(enbConfigSet, cellNumber).setCellAdvancedConfigurationProfile(profileName);
                break;
            case MultiCell_Profile:
                enbConfigSet.setMultiCellProfile(profileName);
                break;
            case eMBMS_Profile:
                if (cellNumber < 1) {
                    report.report("Cell ID need to be greater than 0", Reporter.WARNING);
                    return null;
                }
                getActiveIfActiveCell(enbConfigSet, cellNumber).setEmbmsProfile(profileName);
                break;
            case Traffic_Management_Profile:
                if (cellNumber < 1) {
                    report.report("Cell ID need to be greater than 0", Reporter.WARNING);
                    return null;
                }
                getActiveIfActiveCell(enbConfigSet, cellNumber).setTrafficManagementProfile(profileName);
                break;
            case Call_Trace_Profile:
                if (cellNumber < 1) {
                    report.report("Cell ID need to be greater than 0", Reporter.WARNING);
                    return null;
                }
                getActiveIfActiveCell(enbConfigSet, cellNumber).setCallTraceProfile(profileName);
                break;
            default:
                report.report("In method addProfile : No Enum EnbProfile", Reporter.WARNING);
                return null;
        }
        return enbConfigSet;
    }

    private LteCellSetWs getActiveIfActiveCell(LteEnbDetailsSetWs enbConfigSet, int cellNumber) {
        ObjectFactory objectFactory = new ObjectFactory();
        LteCellSetWs newCell = new LteCellSetWs();
        for (LteCellSetWs tempCell : enbConfigSet.getLteCell()) {
            if (tempCell.getCellNumber().getValue().equals(String.valueOf(cellNumber)))
                return tempCell;
        }
        newCell.setCellNumber(objectFactory.createLteCellGetWsCellNumber(String.valueOf(cellNumber)));
        enbConfigSet.getLteCell().add(newCell);
        return newCell;
        /*
         * try { for (int i = 0; i <= enbConfigSet.getLteCell().size(); i++) {
         * String cellStr =
         * enbConfigSet.getLteCell().get(i).getCellNumber().getValue(); int
         * cellInt = Integer.valueOf(cellStr); if (cellInt == cellNumber) {
         * return i; } } } catch (Exception e) { return -1; } return -1;
         */
    }

    public boolean copyFromCell1(EnodeB node, String cellAdvancedProfile, String radioProfile, String MobilityProfile, String eMBMSProfile, String TrafficManagementProfile, String callTraceProfile){
       report.report("Netspan_17_actions");
        ObjectFactory objectFactory = new ObjectFactory();
        LteEnbDetailsSetWs enbConfigSet = new LteEnbDetailsSetWs();
        LteCellSetWs lteCellSet = new LteCellSetWs();
        lteCellSet.setCellNumber(objectFactory.createLteCellGetWsCellNumber(String.valueOf(2)));

        enbConfigSet = addProfile(node, 2, EnbProfiles.Cell_Advanced_Profile, cellAdvancedProfile, enbConfigSet);
        enbConfigSet = addProfile(node, 2, EnbProfiles.Radio_Profile, radioProfile, enbConfigSet);
        enbConfigSet = addProfile(node, 2, EnbProfiles.Mobility_Profile, MobilityProfile, enbConfigSet);
        enbConfigSet = addProfile(node, 2, EnbProfiles.eMBMS_Profile, eMBMSProfile, enbConfigSet);
        enbConfigSet = addProfile(node, 2, EnbProfiles.Traffic_Management_Profile, TrafficManagementProfile, enbConfigSet);
        enbConfigSet = addProfile(node, 2, EnbProfiles.Call_Trace_Profile, callTraceProfile, enbConfigSet);

        return setNodeConfig(node, enbConfigSet);
    }


}

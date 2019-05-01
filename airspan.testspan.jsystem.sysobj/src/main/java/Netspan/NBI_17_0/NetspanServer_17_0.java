package Netspan.NBI_17_0;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import EnodeB.EnodeB;
import Netspan.NBIVersion;
import Netspan.NBI_17_0.Lte.LteCellSetWs;
import Netspan.NBI_17_0.Lte.EaidsParams;
import Netspan.NBI_17_0.Lte.EnbTypesWs;
import Netspan.NBI_17_0.Lte.LteEnbDetailsSetWs;
import Netspan.NBI_17_0.Lte.NodeActionResult;
import Netspan.NBI_17_0.Lte.ObjectFactory;
import Netspan.NBI_16_0.NetspanServer_16_0;
import Netspan.Profiles.ConnectedUETrafficDirection;
import Utils.GeneralUtils;
import jsystem.framework.report.Reporter;


public class NetspanServer_17_0 extends NetspanServer_16_0 implements Netspan_17_0_abilities{
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
	public boolean setEmergencyAreaIds(EnodeB dut, ArrayList<Integer> ids) {
		String nodeName = dut.getNetspanName();
		LteEnbDetailsSetWs enbConfigSet = new LteEnbDetailsSetWs();
		ObjectFactory factoryDetails = new ObjectFactory();
		LteCellSetWs lteCellSet = new LteCellSetWs();
		EaidsParams eaidsParams = factoryDetails.createEaidsParams();
		eaidsParams.getEmergencyAreaId().addAll(ids);
		lteCellSet.setCellNumber(factoryDetails.createLteCellSetWsCellNumber(String.valueOf(dut.getCellContextID())));
		lteCellSet.setEmergencyAreaIds(eaidsParams);
		enbConfigSet.getLteCell().add(lteCellSet);
		try {
			NodeActionResult result = soapHelper_17_0.getLteSoap().enbConfigSet(nodeName, null,
					enbConfigSet, null, credentialsLte);
			if (result.getErrorCode() == Netspan.NBI_17_0.Lte.ErrorCodes.OK) {
				report.report(String.format("%s - Succeeded to set Emergency Area Ids", nodeName));
				return true;
			} else {
				report.report("enbConfigSet via Netspan Failed : " + result.getErrorString(), Reporter.WARNING);
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			report.report(nodeName + ": enbConfigSet via Netspan Failed due to: ", Reporter.WARNING);
			return false;
		} finally {
			soapHelper_17_0.endLteSoap();
		}
	}
    
}

package Netspan.NBI_16_5;

import java.util.ArrayList;

import EnodeB.EnodeB;
import Netspan.NBIVersion;
import Netspan.NBI_16_0.NetspanServer_16_0;
import Netspan.NBI_16_5.Lte.LteCellSetWs;
import Netspan.NBI_16_5.Lte.LteEnbDetailsSetWs;
import Netspan.NBI_16_5.Lte.NodeActionResult;
import Netspan.NBI_16_5.Lte.ObjectFactory;
import Netspan.NBI_16_5.Lte.EaidsParams;
import Netspan.NBI_16_5.SoapHelper;
import jsystem.framework.report.Reporter;

public class NetspanServer_16_5 extends NetspanServer_16_0 implements Netspan_16_5_abilities {

	public SoapHelper soapHelper_16_5;
	private static final String USERNAME = "wsadmin";
	private static final String PASSWORD = "password";
	private static final Netspan.NBI_16_5.Lte.Credentials credentialsLte = new Netspan.NBI_16_5.Lte.Credentials();
	private static final Netspan.NBI_16_5.Inventory.Credentials credentialsInventory = new Netspan.NBI_16_5.Inventory.Credentials();
	private static final Netspan.NBI_16_5.FaultManagement.Credentials credentialsFaultManagement = new Netspan.NBI_16_5.FaultManagement.Credentials();
	private static final Netspan.NBI_16_5.Statistics.Credentials credentialsStatistics = new Netspan.NBI_16_5.Statistics.Credentials();
	private static final Netspan.NBI_16_5.Status.Credentials credentialsStatus = new Netspan.NBI_16_5.Status.Credentials();
	private static final Netspan.NBI_16_5.Server.Credentials credentialsServer = new Netspan.NBI_16_5.Server.Credentials();
	private static final Netspan.NBI_16_5.Backhaul.Credentials credentialsBackhaul = new Netspan.NBI_16_5.Backhaul.Credentials();
	private static final Netspan.NBI_16_5.Software.Credentials credentialsSoftware = new Netspan.NBI_16_5.Software.Credentials();

	@Override
	public void init() throws Exception {
		if (NBI_VERSION == null) {
			NBI_VERSION = NBIVersion.NBI_16_5;
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
    	this.soapHelper_16_5 = new SoapHelper(getHostname());
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
			NodeActionResult result = soapHelper_16_5.getLteSoap().enbConfigSet(nodeName, null,
					enbConfigSet, null, credentialsLte);
			if (result.getErrorCode() == Netspan.NBI_16_5.Lte.ErrorCodes.OK) {
				report.report(String.format("%s - Succeeded to set Emergency Area Ids", nodeName));
				return true;
			} else {
				report.report("enbConfigSet via Netspan Failed : " + result.getErrorString(), Reporter.WARNING);
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			report.report(nodeName + ": enbConfigSet via Netspan Failed due to: "+e.getMessage(), Reporter.WARNING);
			return false;
		} finally {
			soapHelper_16_5.endLteSoap();
		}
	}
}

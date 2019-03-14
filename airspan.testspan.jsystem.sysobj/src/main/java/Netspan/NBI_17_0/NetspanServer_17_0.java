package Netspan.NBI_17_0;

import Netspan.NBIVersion;
import Netspan.NBI_16_0.NetspanServer_16_0;


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
}

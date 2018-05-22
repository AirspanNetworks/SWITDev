package EPC;

import java.util.ArrayList;

import EnodeB.EnodeB;
import UE.UE;
import Utils.GeneralUtils;
import jsystem.framework.report.Reporter;

public class AricentEPC extends EPC {
	
	public static final String   UE_SAMPLE_COMMAND              = "ip r s t 7";
	public static final int      IMSI_LENGTH                    = 15;
	public static final String   MME_LOG_IMSI_PREFIX            = "IMSI:";
	public static final String   MME_LOG_STATE_PREFIX           = "UCA_STATE_";
	public static final String   MME_LOG_STATE_DELIMITER        = "]";
	public static final String   IMSI_STRING_DELIMITER          = ",";
	
	public static final int      MME_LOG_STATE_NUMBER           = 4;
	public static final String   MME_LOG_STATE_NULL             = "NULL";
	public static final int      MME_LOG_STATE_NULL_INDEX       = 0;
	public static final String   MME_LOG_STATE_IDLE             = "IDLE";
	public static final int      MME_LOG_STATE_IDLE_INDEX       = 1;
	public static final String   MME_LOG_STATE_DETACHED         = "DETACHED";
	public static final int      MME_LOG_STATE_DETACHED_INDEX   = 2;
	public static final String   MME_LOG_STATE_CONNECTED        = "CONNECTED";
	public static final int      MME_LOG_STATE_CONNECTED_INDEX  = 3;
	
	private String umbrella;

	@Override
	public void close() {
		super.close();
	}
	
	/*
	 * this method isn't implemented.
	 */
	@Override
	public Boolean CheckUeListConnection(ArrayList<UE> ueList) {
		report.report("No implementation for AricentEPC", Reporter.FAIL);
		return false;
	}
	
	@Override
	protected String getEPCVersion() {
		if (umbrella != null)
			return "Umbrella " + umbrella;
		
		return "Aricent EPC unknown umbrella";
	}

	public String getUmbrella() {
		return umbrella;
	}

	public void setUmbrella(String umbrella) {
		this.umbrella = umbrella;
	}
	
	@Override
	public EnodeB getCurrentEnodB(UE ue, ArrayList<EnodeB> possibleEnbs) {
		report.report("getCurrentEnodB method not implemented in AricentEPC",Reporter.WARNING);
		return null;
	}

	@Override
	public String getNodeAccordingToImsi(String imsi) {
		GeneralUtils.printToConsole("No implementation for "+this.getClass() +" to this method! getNodeAccordingToImsi() ");
		return null;
	}

	@Override
	public String getEPCConfig() {
		return umbrella;
		// TODO Auto-generated method stub
	}
	
	@Deprecated
	public int checkNumberOfConnectedUEs(ArrayList<UE> ues,EnodeB enb,boolean printToConsole) throws Exception {
		GeneralUtils.printToConsole("THIS IS UN-IMPLMENTED method!!!! - return null");
		return 0;
	}

	@Override
	public boolean checkUEConnectedToNode(UE ue, EnodeB node) {
		report.report("checkUEConnectedToNode method is not implemented in Aricent!");
		return false;
	}

	@Override
	public MME getCurrentMME(UE ue, ArrayList<MME> possibleMmes) {
		report.report("getCurrentMME method is not implemented in Aricent!");
		return null;
	}



}

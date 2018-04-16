package Utils;

import Netspan.CellProfiles;
import jsystem.framework.report.Reporter;
import jsystem.framework.system.SystemObjectImpl;

public class CellNetspanProfiles extends SystemObjectImpl  {
	private int cellId;
	private String pci;
	private String cellAdvanced;
	private String radio;
	private String mobility;
	private String embms;
	private String trafficManagement;
	private String callTrace;
	
	public String getProfile(CellProfiles cellProfile){
		try{
			switch (cellProfile) {
			case Cell_Advanced_Profile:
				return getCellAdvanced();
			case Radio_Profile:
				return getRadio();
			case Mobility_Profile:
				return getMobility();
			case Embms_Profile:
				return getEmbms();
			case Traffic_Management_Profile:
				return getTrafficManagement();
			case Call_Trace_Profile:
				return getCallTrace();
			default:
				report.report("Enum: No Such CellProfile", Reporter.WARNING);
				return "";
			}
		}
		catch(Exception e){
			e.printStackTrace();
			report.report("Error in getProfile: " + e.getMessage(), Reporter.WARNING);
			return "";
		}
	}


	public String getRadio() {
		if ( this.radio != null && this.radio.length() > 0 )
			return this.radio;
		return null;
	}


	public void setRadio(String radio) {
		this.radio = radio;
	}


	public String getMobility() {
		if ( this.mobility != null && this.mobility.length() > 0 )
			return this.mobility;
		return null;
	}


	public void setMobility(String mobility) {
		this.mobility = mobility;
	}


	public String getCellAdvanced() {
		if ( this.cellAdvanced != null && this.cellAdvanced.length() > 0 )
			return this.cellAdvanced;
		return null;
	}


	public void setCellAdvanced(String cellAdvanced) {
		this.cellAdvanced = cellAdvanced;
	}


	public String getEmbms() {
		if ( this.embms != null && this.embms.length() > 0 )
			return this.embms;
		return null;
	}


	public void setEmbms(String embms) {
		this.embms = embms;
	}


	public String getTrafficManagement() {
		if ( this.trafficManagement != null && this.trafficManagement.length() > 0 )
			return this.trafficManagement;
		return null;
	}


	public void setTrafficManagement(String trafficManagement) {
		this.trafficManagement = trafficManagement;
	}


	public String getCallTrace() {
		if ( this.callTrace != null && this.callTrace.length() > 0 )
			return this.callTrace;
		return null;
	}


	public void setCallTrace(String callTrace) {
		this.callTrace = callTrace;
	}


	public int getCellId() {
		return cellId;
	}

	public void setCellId(int cellId) {
		this.cellId = cellId;
	}

	public int getPci() {
		if (pci == null) {
			return GeneralUtils.ERROR_VALUE;
		}
		int pciValue = GeneralUtils.ERROR_VALUE;
		try{
			pciValue = Integer.parseInt(pci);			
		}
		catch (Exception e) {
			GeneralUtils.printToConsole("Error parsing default pci value from SUT.");
		}
		return pciValue;
	}

	public void setPci(String pci) {
		this.pci = pci;
	}

}

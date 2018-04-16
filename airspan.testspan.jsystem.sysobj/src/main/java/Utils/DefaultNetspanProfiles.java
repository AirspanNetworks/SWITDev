package Utils;

import Netspan.EnbProfiles;
import jsystem.framework.report.Reporter;
import jsystem.framework.system.SystemObjectImpl;

public class DefaultNetspanProfiles extends SystemObjectImpl  {
	
	private String cellAdvanced;
	private String radio;
	private String mobility;
	private String systemDefault;
	private String enodeBAdvanced;
	private String network;
	private String synchronisation;
	private String security;
	private String SON;	
	private String management;
	private String multiCell;
	private String neighbourManagement;
	private String softwareImage;
	public CellNetspanProfiles[] cellProfiles;

	@Override
	public void init() throws Exception{
		super.init();
		if(cellProfiles == null){
			GeneralUtils.printToConsole("No cell profiles in SUT.");
			cellProfiles = new CellNetspanProfiles[1];
			cellProfiles[0] = new CellNetspanProfiles();
			cellProfiles[0].setCellId(1);
			cellProfiles[0].setRadio(radio);
			cellProfiles[0].setMobility(mobility);
			cellProfiles[0].setCellAdvanced(cellAdvanced);			
		}
	}
	
	public CellNetspanProfiles getCellNetspanProfile(int cellId)
	{
		for (CellNetspanProfiles cellNetspanProfiles : cellProfiles) {
			if(cellNetspanProfiles == null){
				continue;
			}
			
			if (cellNetspanProfiles.getCellId() == cellId) {
				return cellNetspanProfiles;
			}
		}		
		return null;
	}	
	
	public String getDefaultProfile(EnbProfiles enbProfile){
		try{
			switch (enbProfile) {
				case Management_Profile:
					return getManagement();
					
				case Mobility_Profile:
					return cellProfiles[0].getMobility();
					
				case Network_Profile:
					return  getNetwork();
					
				case Radio_Profile:
					return cellProfiles[0].getRadio();
					
				case Security_Profile:
					return getSecurity();
					
				case Son_Profile:
					return getSON();
					
				case Sync_Profile:
					return getSynchronisation();
					
				case EnodeB_Advanced_Profile:
					return getEnodeBAdvanced();
					
				case Cell_Advanced_Profile:
					return cellProfiles[0].getCellAdvanced();
					
				case Neighbour_Management_Profile:
					return getNeighbourManagement();
				
				case MultiCell_Profile:
					return getMultiCell();
					
				case System_Default_Profile:
					return getSystemDefault();
					
				default:
					report.report("Enum: No Such EnbProfile", Reporter.WARNING);
					return "";
			}
		}
		catch(Exception e){
			e.printStackTrace();
			report.report("Error in getDefaultProfile: " + e.getMessage(), Reporter.WARNING);
			return "";
		}
	}
	
	public String getManagement() {
		return management;
	}

	public void setManagement(String management) {
		this.management = management;
	}

	public DefaultNetspanProfiles(){
	}
	
	public String getSecurity() {
		if ( this.security != null && this.security.length() > 0 )
			return this.security;
		return null;
	}

	public void setSecurity(String security) {
		this.security = security;
	}
	
	public String getSON() {
		if ( this.SON != null && this.SON.length() > 0 )
			return this.SON;
		return null;
	}

	public void setSON(String son) {
		this.SON = son;
	}

	public String getRadio() {
		return getRadioFromCell(1);
	}
	
	public String getRadioFromCell(int cellId){
		CellNetspanProfiles cell = getCellNetspanProfile(cellId);
		if(cell != null){
			return cell.getRadio();
		}
		report.report("Error in Radio profile tag in cell : "+cellId);
		return null;
	}
	
	public void setRadio(String radio) {
		this.radio = radio;
	}
	
	public String getMobility() {
		return getMobilityFromCell(1);
	}
	
	public String getMobilityFromCell(int cellId){
		CellNetspanProfiles cell = getCellNetspanProfile(cellId);
		if(cell != null){
			return cell.getMobility();
		}
		report.report("Error in Mobility profile tag in cell : "+cellId);
		return null;
	}
	
	public void setMobility(String mobility) {
		this.mobility = mobility;
	}

	public String getNetwork() {
		return network;
	}

	public void setNetwork(String network) {
		this.network = network;
	}

	public String getEnodeBAdvanced() {
		return enodeBAdvanced;
	}

	public void setEnodeBAdvanced(String enodeBAdvanced) {
		this.enodeBAdvanced = enodeBAdvanced;
	}
	
	public String getMultiCell() {
		return multiCell;
	}

	public void setMultiCell(String multiCell) {
		this.multiCell = multiCell;
	}

	public String getCellAdvanced() {
		return getCellAdvancedFromCell(1);
	}
	
	public String getCellAdvancedFromCell(int cellId){
		CellNetspanProfiles cell = getCellNetspanProfile(cellId);
		if(cell != null){
			return cell.getCellAdvanced();
		}
		report.report("Error in Cell Advanced profile tag in cell : "+cellId);
		return null;
	}
	
	public void setCellAdvanced(String cellAdvanced) {
		this.cellAdvanced = cellAdvanced;
	}

	public String getSynchronisation() {
		if ( this.synchronisation != null && this.synchronisation.length() > 0 )
			return this.synchronisation;
		return null;
	}

	public void setSynchronisation(String synchronisation) {
		this.synchronisation = synchronisation;
	}
	
	public String getNeighbourManagement() {
		if ( this.neighbourManagement != null && this.neighbourManagement.length() > 0 )
			return this.neighbourManagement;
		return null;
	}

	public void setNeighbourManagement(String neighbourManagement) {
		this.neighbourManagement = neighbourManagement;
	}

	public String getSystemDefault() {
		return systemDefault;
	}

	public void setSystemDefault(String systemDefault) {
		this.systemDefault = systemDefault;
	}

	public String getSoftwareImage() {
		return softwareImage;
	}

	public void setSoftwareImage(String softwareImage) {
		this.softwareImage = softwareImage;
	}
}

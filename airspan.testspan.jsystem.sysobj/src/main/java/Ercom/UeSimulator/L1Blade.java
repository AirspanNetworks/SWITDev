package Ercom.UeSimulator;

import Utils.StdOut;


public class L1Blade extends Blade{
	
	//------------------ Variables  final ---------------------------
	
	private static final String DL_FREQUENCY = "DL_Frequency        = %se6 # downlink carrier frequency in Hz";
	private static final String UL_FREQUENCY = "UL_Frequency        = %se6 # uplink carrier frequency in Hz";
	private static final String BANDWIDTH    = "Bandwidth           = %s    # Bandwidth in MHz";
	private static final String TDD_SPECIAL_SUBFRAME_CONFIG_ENABLE  = "tdd_special_subframe_config = 7";
	private static final String TDD_SPECIAL_SUBFRAME_CONFIG_DISABLE = "#tdd_special_subframe_config = 7";
	private static final String TDD_SPECIAL_SUBFRAME_CONFIG_LINE    = ".*tdd_special_subframe_config.*";
	private static final String PATH_TO_L2OPTIONS_FILE    = "/ercom/config/";
	private static final String L1_CONFIG                 = "L1.conf";
	private static final String DL_FREQUENCY_LINE         = "^DL_Frequency.*";
	private static final String UL_FREQUENCY_LINE         = "^UL_Frequency.*";
	private static final String CELL_ID_LINE              = "^Cell_Id.*";
	private static final String BANDWIDTH_LINE            = "^Bandwidth.*";
	private static final String TDD_UL_DL_CONFIG_ENABLE   = "tdd_ul_dl_config = 2";
	private static final String TDD_UL_DL_CONFIG_DISABLE  = "#tdd_ul_dl_config = 2";
	private static final String TDD_UL_DL_CONFIG_LINE     = ".*tdd_ul_dl_config.*";
	private static final String CELL_ID = "Cell_Id             = %s";
	private static final int  IGNORE_FIELD                = -1;
	
	//------------------ Variables  ---------------------------------
	
	private int	dlFrequency = IGNORE_FIELD;
	private int	ulFrequency = IGNORE_FIELD; 
	private int	bandwidth   = IGNORE_FIELD;          
	private int CellId      = IGNORE_FIELD;
	
	
	
	//------------------ public Methods  ----------------------------
	
	
	@Override
	public void init() throws Exception {
		super.init();
		sshCmd = initSshSessionToBlade();
	}

	public void setTDDMode(boolean isActive){
		if(isActive){
			findAndReplaceString(TDD_UL_DL_CONFIG_LINE,TDD_UL_DL_CONFIG_ENABLE,PATH_TO_L2OPTIONS_FILE+L1_CONFIG);
			findAndReplaceString(TDD_SPECIAL_SUBFRAME_CONFIG_LINE,TDD_SPECIAL_SUBFRAME_CONFIG_ENABLE,PATH_TO_L2OPTIONS_FILE+L1_CONFIG);
		}else{
			findAndReplaceString(TDD_UL_DL_CONFIG_LINE,TDD_UL_DL_CONFIG_DISABLE,PATH_TO_L2OPTIONS_FILE+L1_CONFIG);
			findAndReplaceString(TDD_SPECIAL_SUBFRAME_CONFIG_LINE,TDD_SPECIAL_SUBFRAME_CONFIG_DISABLE,PATH_TO_L2OPTIONS_FILE+L1_CONFIG);
		}
	}
	
	public String getL1BladeDuplexMode() throws Exception{
		return getCurrentBladeDuplexMode();
	}
	
	public void changeL1BladeDuplexModeTdd2Fdd() throws Exception{
		changeDuplexModeOfTheBlade("TDD","FDD");
	}
	
	public void changeL1BladeDuplexModeFdd2Tdd() throws Exception{
		changeDuplexModeOfTheBlade("FDD","TDD");
	}
	
	//------------------ private Methods ----------------------------
	
	private void updateSpecificLineInL1ConfigFile(int value,String line, String pattern) throws Exception{
		String newLine = String.format(pattern, value);
		boolean res = findAndReplaceString(line,newLine,PATH_TO_L2OPTIONS_FILE+L1_CONFIG);
		if(res){
			StdOut.print2console("New line written in L1.conf file "+ newLine );
		}else{
			throw new Exception("Failed to set new line in L1.config file " + newLine);
		}
	}
	
	//------------------ Setters/ Getters ---------------------------
	
	public int getCellId() {
		return CellId;
	}
	public void setCellId(int cellId) throws Exception {
		updateSpecificLineInL1ConfigFile(cellId,CELL_ID_LINE,CELL_ID);
		CellId = cellId;
	}
	
	public int getDlFrequency() {
		return dlFrequency;
	}
	public void setDlFrequency(int dlFrequency) throws Exception {
		updateSpecificLineInL1ConfigFile(dlFrequency,DL_FREQUENCY_LINE,DL_FREQUENCY);
		this.dlFrequency = dlFrequency;
		
		
	}
	
	public int getUlFrequency() {
		return ulFrequency;
	}
	public void setUlFrequency(int ulFrequency) throws Exception {
		updateSpecificLineInL1ConfigFile(ulFrequency,UL_FREQUENCY_LINE,UL_FREQUENCY);
		this.ulFrequency = ulFrequency;
		
		
	}
	
	public int getBandwidth() {
		return bandwidth;
	}
	public void setBandwidth(int bandwidth) throws Exception {
		updateSpecificLineInL1ConfigFile(bandwidth,BANDWIDTH_LINE,BANDWIDTH);
		this.bandwidth = bandwidth;
		
		
	}
	
	
}

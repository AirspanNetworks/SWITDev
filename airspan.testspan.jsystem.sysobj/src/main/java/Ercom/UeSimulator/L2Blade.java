package Ercom.UeSimulator;

import Utils.StdOut;


public class L2Blade extends Blade{
	
	
	
	//------------------ Variables  final ---------------------------
	private static final String PATH_TO_L2OPTIONS_FILE    = "/ercom/config/";
	private static final String BW_100                    = "OPT_UL_BW=100";
	private static final String BW_50                     = "OPT_UL_BW=50";
	private static final String BW_25                     = "OPT_UL_BW=25";
	private static final String L2OPTIONS                 = "L2options";
	private static final int BW_5_MZ                      = 5;
	private static final int BW_10_MZ                     = 10;
	private static final int BW_20_MZ                     = 20;
	
	//------------------ Variables ----------------------------------
	
	private int bandwidth = BW_20_MZ;
	
	//------------------ public Methods  ----------------------------
	
	
	@Override
	public void init() throws Exception {
		super.init();
		sshCmd = initSshSessionToBlade();
	}
	
	
	public boolean configureL2Bandwidth(int bandwidth){
		setBandwidth(bandwidth);
		String currentBandwidth = getCurrentBandwidth();
		if(currentBandwidth == null){
			StdOut.print2console("Unable to retrive [OPT_UL_BW] parameter from L2options file");
			return false;
		}
		boolean res = findAndReplaceString(currentBandwidth,getNewBandwidthAsString(),PATH_TO_L2OPTIONS_FILE+L2OPTIONS);	
		if(res == true && validateBandwidthSetAction()){
			StdOut.print2console("[OPT_UL_BW] parameter in L2options file changed successfully");
			StdOut.print2console("New bandwidth set for L2Blade: "+getNewBandwidthAsString());
			return true;
		}else{
			return false;
		}
	}
	
	public String getL2BladeDuplexMode() throws Exception{
		return getCurrentBladeDuplexMode();
	}
	
	public void changeL2BladeDuplexModeTdd2Fdd() throws Exception{
		changeDuplexModeOfTheBlade("TDD","FDD");
	}
	
	public void changeL2BladeDuplexModeFdd2Tdd() throws Exception{
		changeDuplexModeOfTheBlade("FDD","TDD");
	}
	
	//------------------ private Methods ----------------------------
	
	private boolean validateBandwidthSetAction(){
		String bw = getCurrentBandwidth();
		if(bw == null){
			return false;
		}else if(bw.equals(getNewBandwidthAsString())){
			return true;
		}else{
			return false;
		}
	}
	
	private String getNewBandwidthAsString(){
		switch(getBandwidth()){
		case BW_5_MZ:
			return BW_25;
		case BW_10_MZ:
			return BW_50;
		case BW_20_MZ :
			return BW_100;
		default:
			return BW_100;
		}
	}
	
	private String getCurrentBandwidth(){
		String result = null;
		try {
			result=sendCommandViaSshCmd("cat "+PATH_TO_L2OPTIONS_FILE+L2OPTIONS);
		}
		catch (Exception e) {
			StdOut.print2console("Failed to excute following command [ "+"cat "+PATH_TO_L2OPTIONS_FILE+L2OPTIONS+"] for L2 blade");
			StdOut.print2console("Possible reason : broken SSH connection with L2 Blade");
			StdOut.print2console(e.getMessage());
			StdOut.print2console(e.getStackTrace().toString());
		}
		if(result.contains(BW_100)) return BW_100;
		if(result.contains(BW_50)) return BW_50;
		if(result.contains(BW_25)) return BW_25;
		StdOut.print2console("Current Bandwidth from L2options file is: "+ result == null?"Error unable to read from file empty string returned.":result);
		return result;
	}
	
	//------------------ Setters/ Getters ---------------------------

	public int getBandwidth() {
		return bandwidth;
	}

	public void setBandwidth(int bandwidth) {
		this.bandwidth = bandwidth;
	}
}

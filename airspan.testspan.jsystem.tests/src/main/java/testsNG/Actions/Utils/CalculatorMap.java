package testsNG.Actions.Utils;

import java.util.HashMap;

import Netspan.Profiles.RadioParameters;
import Utils.GeneralUtils;
import Utils.Pair;

public class CalculatorMap {

	public HashMap<String, String> myCalc;
	public TrafficConf trafficConf = new TrafficConf();

	/**
	 * return Pair of values where the first is DL and the Second is UL.
	 * @param radio
	 * @return
	 */
	public Pair<Double, Double> getDLandULconfiguration(RadioParameters radio) {
		String fcStr = radio.getFrameConfig();
		String bwStr = radio.getBandwidth();
		boolean isFdd = radio.isFDD();
		int fc = 0;
		int bw = 20;
		GeneralUtils.printToConsole("Debug PortLoad : FC - "+fcStr + " BW - "+bwStr + " FDD - "+isFdd);
		
		if(isFdd){
			fc = 900;
		}else{
			try{
				fc = Integer.valueOf(fcStr);
			}catch(Exception FCExcpetion){
				GeneralUtils.printToConsole("*!* Exception while trying to parse Frame Config - default value 0 *!*");
			}
		}
		
		try{
			bw = Integer.valueOf(bwStr);
		}catch(Exception BWException){
			GeneralUtils.printToConsole("*!* Exception while trying to parse BandWidth - default value 20 *!*");
		}
		
		return trafficConf.getPair(fc + bw);
	}
	
	public String getPassCriteria(String key){
		if(key.contains("#")){
			String[] calculatorStringKeys = key.split("#");
			Double minUl = -1.0;
			Double minDl = -1.0;
			for(int i = 0; i < calculatorStringKeys.length; i++){
				String dl_ul = myCalc.get(calculatorStringKeys[i]);
				String[] dlul = dl_ul.split("_");
				double dl = Double.parseDouble(dlul[0]);
				double ul = Double.parseDouble(dlul[1]);
				if(minDl < 0.0 || dl < minDl){
					minDl = dl;
				}
				if(minUl < 0.0 || ul < minUl){
					minUl = ul;
				}
			}
			String dl_ul = minDl + "_" + minUl;
			return dl_ul;
		}else{
			return myCalc.get(key);
		}
	}

	public CalculatorMap() {
		myCalc = new HashMap<String, String>();

		// Key: FC_Duplex_BW_CFI_SSF_UEMode ; Value: DL_UL
		// TDD
		// Bandwidth 20
		 myCalc.put("1_TDD_20_2_7_PTP","69.78_18.14");
		  myCalc.put("1_TDD_20_2_7_PT2P","69.78_18.14");
		  myCalc.put("1_TDD_20_2_7_PTMP","69.78_18.14");
		  myCalc.put("1_TDD_20_1_7_PTP","79.06_19.57");
		  myCalc.put("1_TDD_20_1_7_PT2P","79.06_19.57");
		  myCalc.put("1_TDD_20_1_7_PTMP","79.06_19.57");
		  
		  myCalc.put("2_TDD_20_1_7_PTP","109.21_9.07");
		  myCalc.put("2_TDD_20_1_7_PT2P","109.21_9.07");
		  myCalc.put("2_TDD_20_1_7_PTMP","109.21_9.07");
		  myCalc.put("2_TDD_20_2_7_PTP","95.29_9.07");
		  myCalc.put("2_TDD_20_2_7_PT2P","95.29_9.07");
		  myCalc.put("2_TDD_20_2_7_PTMP","95.29_9.07");
		  
		  myCalc.put("1_TDD_20_1_5_PTP","60.3_19.57");
		  myCalc.put("1_TDD_20_1_5_PT2P","60.3_19.57");
		  myCalc.put("1_TDD_20_1_5_PTMP","60.3_19.77");
		  myCalc.put("1_TDD_20_2_5_PTP","51.02_18.14");
		  myCalc.put("1_TDD_20_2_5_PT2P","51.02_18.14");
		  myCalc.put("1_TDD_20_2_5_PTMP","51.02_18.14");
		  
		  myCalc.put("2_TDD_20_1_5_PTP","90.45_9.07");
		  myCalc.put("2_TDD_20_1_5_PT2P","90.45_9.07");
		  myCalc.put("2_TDD_20_1_5_PTMP","90.45_9.47");
		  myCalc.put("2_TDD_20_2_5_PTP","76.53_9.07");
		  myCalc.put("2_TDD_20_2_5_PT2P","76.53_9.07");
		  myCalc.put("2_TDD_20_2_5_PTMP","76.53_9.07");
		  
		  //Bandwidth 15
		  myCalc.put("1_TDD_15_1_7_PTP","58.11_14.68");
		  myCalc.put("1_TDD_15_1_7_PT2P","58.11_14.68");
		  myCalc.put("1_TDD_15_1_7_PTMP","58.11_14.68");
		  myCalc.put("1_TDD_15_2_7_PTP","51.57_12.68");
		  myCalc.put("1_TDD_15_2_7_PT2P","51.57_12.68");
		  myCalc.put("1_TDD_15_2_7_PTMP","51.57_12.68");
		  
		  myCalc.put("2_TDD_15_1_7_PTP","80.13_6.34");
		  myCalc.put("2_TDD_15_1_7_PT2P","80.13_6.34");
		  myCalc.put("2_TDD_15_1_7_PTMP","80.13_6.34");
		  myCalc.put("2_TDD_15_2_7_PTP","70.33_6.34");
		  myCalc.put("2_TDD_15_2_7_PT2P","70.33_6.34");
		  myCalc.put("2_TDD_15_2_7_PTMP","70.33_6.34");
		  
		  myCalc.put("1_TDD_15_1_5_PTP","44.04_14.68");
		  myCalc.put("1_TDD_15_1_5_PT2P","44.04_14.68");
		  myCalc.put("1_TDD_15_1_5_PTMP","44.04_14.68");
		  myCalc.put("1_TDD_15_2_5_PTP","37.51_12.68");
		  myCalc.put("1_TDD_15_2_5_PT2P","37.51_12.68");
		  myCalc.put("1_TDD_15_2_5_PTMP","37.51_12.68");
		  
		  myCalc.put("2_TDD_15_1_5_PTP","66.07_6.34");
		  myCalc.put("2_TDD_15_1_5_PT2P","66.07_6.34");
		  myCalc.put("2_TDD_15_1_5_PTMP","66.07_6.34");
		  myCalc.put("2_TDD_15_2_5_PTP","56.27_6.34");
		  myCalc.put("2_TDD_15_2_5_PT2P","56.27_6.34");
		  myCalc.put("2_TDD_15_2_5_PTMP","56.27_6.34");
		  
		  //BandWidth 5
		  myCalc.put("1_TDD_5_1_7_PTP","18.75_3.96");
		  myCalc.put("1_TDD_5_1_7_PT2P","18.75_3.96");
		  myCalc.put("1_TDD_5_1_7_PTMP","18.75_3.96");
		  myCalc.put("1_TDD_5_2_7_PTP","17.25_3.96");
		  myCalc.put("1_TDD_5_2_7_PT2P","17.25_3.96");
		  myCalc.put("1_TDD_5_2_7_PTMP","17.25_3.96");
		  
		  myCalc.put("2_TDD_5_1_7_PTP","26.08_1.98");
		  myCalc.put("2_TDD_5_1_7_PT2P","26.08_1.98");
		  myCalc.put("2_TDD_5_1_7_PTMP","26.08_1.98");
		  myCalc.put("2_TDD_5_2_7_PTP","23.59_1.98");
		  myCalc.put("2_TDD_5_2_7_PT2P","23.59_1.98");
		  myCalc.put("2_TDD_5_2_7_PTMP","23.59_1.98");
		  
		  myCalc.put("1_TDD_5_1_5_PTP","14.17_3.96");
		  myCalc.put("1_TDD_5_1_5_PT2P","14.17_3.96");
		  myCalc.put("1_TDD_5_1_5_PTMP","14.17_3.96");
		  myCalc.put("1_TDD_5_2_5_PTP","12.67_3.96");
		  myCalc.put("1_TDD_5_2_5_PT2P","12.67_3.96");
		  myCalc.put("1_TDD_5_2_5_PTMP","12.67_3.96");
		  
		  myCalc.put("2_TDD_5_1_5_PTP","21.50_1.98");
		  myCalc.put("2_TDD_5_1_5_PT2P","21.50_1.98");
		  myCalc.put("2_TDD_5_1_5_PTMP","21.50_1.98");
		  myCalc.put("2_TDD_5_2_5_PTP","19.01_1.98");
		  myCalc.put("2_TDD_5_2_5_PT2P","19.01_1.98");
		  myCalc.put("2_TDD_5_2_5_PTMP","19.01_1.98");
		  
		  //BandWidth 10
		  myCalc.put("1_TDD_10_1_7_PTP","38.83_9.80");
		  myCalc.put("1_TDD_10_1_7_PT2P","38.83_9.80");
		  myCalc.put("1_TDD_10_1_7_PTMP","38.83_9.80");
		  myCalc.put("1_TDD_10_2_7_PTP","34.84_9.17");
		  myCalc.put("1_TDD_10_2_7_PT2P","34.84_9.17");
		  myCalc.put("1_TDD_10_2_7_PTMP","34.84_9.17");
		  
		  myCalc.put("2_TDD_10_1_7_PTP","53.51_4.58");
		  myCalc.put("2_TDD_10_1_7_PT2P","53.51_4.58");
		  myCalc.put("2_TDD_10_1_7_PTMP","53.51_4.58");
		  myCalc.put("2_TDD_10_2_7_PTP","47.52_3.97");
		  myCalc.put("2_TDD_10_2_7_PT2P","47.52_3.97");
		  myCalc.put("2_TDD_10_2_7_PTMP","47.52_3.97");
		  
		  myCalc.put("1_TDD_10_1_5_PTP","29.36_9.80");
		  myCalc.put("1_TDD_10_1_5_PT2P","29.36_9.80");
		  myCalc.put("1_TDD_10_1_5_PTMP","29.36_9.80");
		  myCalc.put("1_TDD_10_2_5_PTP","25.36_9.17");
		  myCalc.put("1_TDD_10_2_5_PT2P","25.36_9.17");
		  myCalc.put("1_TDD_10_2_5_PTMP","25.36_9.17");
		  
		  myCalc.put("2_TDD_10_1_5_PTP","44.04_4.58");
		  myCalc.put("2_TDD_10_1_5_PT2P","44.04_4.58");
		  myCalc.put("2_TDD_10_1_5_PTMP","44.04_4.58");
		  myCalc.put("2_TDD_10_2_5_PTP","38.04_3.97");
		  myCalc.put("2_TDD_10_2_5_PT2P","38.04_3.97");
		  myCalc.put("2_TDD_10_2_5_PTMP","38.04_3.97");
		  
		  //FDD
		  //band 20
		  myCalc.put("FDD_20_1_PTP","150.75_48.94");
		  myCalc.put("FDD_20_1_PT2P","150.75_48.94");
		  myCalc.put("FDD_20_1_PTMP","150.75_48.94");
		  
		  myCalc.put("FDD_20_2_PTP","127.55_48.94");
		  myCalc.put("FDD_20_2_PT2P","127.55_49.42");
		  myCalc.put("FDD_20_2_PTMP","127.55_49.42");
		  
		  //band 15
		  myCalc.put("FDD_15_1_PTP","110.11_36.70");
		  myCalc.put("FDD_15_1_PT2P","110.11_36.70");
		  myCalc.put("FDD_15_1_PTMP","110.11_36.70");
		  
		  myCalc.put("FDD_15_2_PTP","93.78_36.70");
		  myCalc.put("FDD_15_2_PT2P","93.78_36.70");
		  myCalc.put("FDD_15_2_PTMP","93.78_36.70");
		  
		  //band 10
		  myCalc.put("FDD_10_1_PTP","73.39_24.50");
		  myCalc.put("FDD_10_1_PT2P","73.39_24.50");
		  myCalc.put("FDD_10_1_PTMP","73.39_24.50");
		  
		  myCalc.put("FDD_10_2_PTP","63.41_24.50");
		  myCalc.put("FDD_10_2_PT2P","63.41_24.50");
		  myCalc.put("FDD_10_2_PTMP","63.41_24.50");
		  
		  //band 5
		  myCalc.put("FDD_5_1_PTP","36.67_9.91");
		  myCalc.put("FDD_5_1_PT2P","36.67_9.91");
		  myCalc.put("FDD_5_1_PTMP","36.67_9.91");
		  
		  myCalc.put("FDD_5_2_PTP","31.68_9.91");
		  myCalc.put("FDD_5_2_PT2P","31.68_9.91");
		  myCalc.put("FDD_5_2_PTMP","31.68_9.91");

	}

	public class TrafficConf {
		HashMap<Integer, Pair<Double, Double>> trafficMap = new HashMap<Integer, Pair<Double, Double>>();
		boolean multiCellFlag = false;
		Integer key = 0;
		Pair<Double, Double> resultKey;

		public TrafficConf() {
			//FDD keys no FC
			trafficMap.put(905, Pair.createPair(34.84, 10.90));
			trafficMap.put(910, Pair.createPair(69.75, 26.95));
			trafficMap.put(915, Pair.createPair(103.15, 40.37));
			trafficMap.put(920, Pair.createPair(140.30, 53.83));
			
			//TDD with FC 1
			trafficMap.put(6, Pair.createPair(19.03, 4.4));
			trafficMap.put(11, Pair.createPair(38.28, 10.12));
			trafficMap.put(16, Pair.createPair(56.76, 13.97));
			trafficMap.put(21, Pair.createPair(76.78, 19.91));
			//TDD with FC 2
			trafficMap.put(7, Pair.createPair(25.96, 1.98));
			trafficMap.put(12, Pair.createPair(52.25, 4.4));
			trafficMap.put(17, Pair.createPair(77.33, 6.93));
			trafficMap.put(22, Pair.createPair(104.83, 10.01));
		}
		
		public Pair<Double,Double> getPair(int key){
			return trafficMap.get(key);
		}

	}

	public enum nodeCellType {

		MULTICELL("multiCell"), SINGLECELL("singleCell");
		nodeCellType(String c) {
			value = c;
		}

		private String value;

		public void setValue(String v) {
			value = v;
		}

		public String getValue() {
			return value;
		}
	}

}

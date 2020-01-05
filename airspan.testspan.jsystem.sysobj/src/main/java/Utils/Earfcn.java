package Utils;

import java.util.ArrayList;
import java.util.Arrays;

public class Earfcn {
	static public int GetEarfcnFromFreqAndBand(int freqInKhz, int band)
	{
		GeneralUtils.printToConsole("calculate earfcn from frequency: " + freqInKhz + " and band " + band);
		int earfcn = -1;
	    for(LteBand lteBand : m_BandList){
	    	if (lteBand.Band == band){
	    		earfcn = (int)(freqInKhz - lteBand.MinDlFreqKhz) / 100 + lteBand.EarfcnMin;
	    		earfcn = GetEarfcn(earfcn);
	            break;
	         }
	    }
	    return earfcn;
	}

	static public int GetEarfcn(int earfcn){
		if (earfcn >= 18000 && earfcn < 36000){
	    	return earfcn - 18000;
	    }else{
	    	return earfcn;
	    }
	}

	public static class LteBand{

		public int Band;
		public int EarfcnMin;
		public int EarfcnMax;
		public int MinDlFreqKhz;
	    public int DlUlDiffKhz;
		
		public LteBand(int band, int earfcnMin, int earfcnMax, int minDlFreqKhz, int dlUlDiffKhz){
			Band = band;
			EarfcnMin = earfcnMin;
			EarfcnMax = earfcnMax;
			MinDlFreqKhz = minDlFreqKhz;
		    DlUlDiffKhz = dlUlDiffKhz;
		}
	}

	private static ArrayList<LteBand> m_BandList = new ArrayList<LteBand>(
		Arrays.asList(new LteBand(1, 0, 599, 2110000, 190000),
		new LteBand(1, 18000, 18599, 2110000, 190000),
		new LteBand(2, 600, 1199, 1930000, 80000),
		new LteBand(2, 18600, 19199, 1930000, 80000),
		new LteBand(3, 1200, 1949, 1805000, 95000),
		new LteBand(3, 19200, 19949, 1805000, 95000),
		new LteBand(4, 1950, 2399, 2110000, 400000),
		new LteBand(4, 19950, 20399, 2110000, 400000),
		new LteBand(5, 2400, 2649, 869000, 45000),
		new LteBand(5, 20400, 20649, 869000, 45000),
		new LteBand(6, 2650, 2749, 875000, 45000),
		new LteBand(6, 20650, 20749, 875000, 45000),
		new LteBand(7, 2750, 3449, 2620000, 120000),
		new LteBand(7, 20750, 21449, 2620000, 120000),
		new LteBand(8, 3450, 3799, 925000, 45000),
		new LteBand(8, 21450, 21799, 925000, 45000),
		new LteBand(9, 3800, 4149, 1844900, 95000),
		new LteBand(9, 21800, 22149, 1844900, 95000),
		new LteBand(10, 4150, 4749, 2110000, 400000),
		new LteBand(10, 22150, 22749, 2110000, 400000),
		new LteBand(11, 4750, 4949, 1475900, 48000),
		new LteBand(11, 22750, 22949, 1475900, 48000),
		new LteBand(12, 5010, 5179, 729000, 30000),
		new LteBand(12, 23010, 23179, 729000, 30000),
		new LteBand(13, 5180, 5279, 746000, -31000),
		new LteBand(13, 23180, 23279, 746000, -31000),
		new LteBand(14, 5280, 5379, 758000, -30000),
		new LteBand(14, 23280, 23379, 758000, -30000),
		new LteBand(17, 5730, 5849, 734000, 30000),
		new LteBand(17, 23730, 23849, 734000, 30000),
		new LteBand(18, 5850, 5999, 860000, 45000),
		new LteBand(18, 23850, 23999, 860000, 45000),
		new LteBand(19, 6000, 6149, 875000, 45000),
		new LteBand(19, 24000, 24149, 875000, 45000),
		new LteBand(20, 6150, 6449, 791000, -41000),
		new LteBand(20, 24150, 24449, 791000, -41000),
		new LteBand(21, 6450, 6599, 1495900, 48000),
		new LteBand(21, 24450, 24599, 1495900, 48000),
		new LteBand(22, 6600, 7399, 3510000, 100000),
		new LteBand(22, 24600, 25399, 3510000, 100000),
		new LteBand(23, 7500, 7699, 2180000, 180000),
		new LteBand(23, 25500, 25699, 2180000, 180000),
		new LteBand(24, 7700, 8039, 1525000, -101500),
		new LteBand(24, 25700, 26039, 1525000, -101500),
		new LteBand(25, 8040, 8689, 1930000, 80000),
		new LteBand(25, 26040, 26689, 1930000, 80000),
		new LteBand(26, 8690, 9039, 859000, 45000),
		new LteBand(26, 26690, 27039, 859000, 45000),
		new LteBand(27, 9040, 9209, 852000, 45000),
		new LteBand(27, 27040, 27209, 852000, 45000),
		new LteBand(28, 9210, 9659, 758000, 55000),
		new LteBand(28, 27210, 27659, 758000, 55000),
	    // Ignore band 29 which is downlink only
		new LteBand(30, 9770, 9869, 2350000, 45000),
		new LteBand(30, 27660, 27759, 2350000, 45000),
		new LteBand(31, 9870, 9919, 462500, 10000),
		new LteBand(31, 27760, 27809, 462500, 10000),
	    // Ignore band 32 which is downlink only
		new LteBand(33, 36000, 36199, 1900000, 0),
		new LteBand(34, 36200, 36349, 2010000, 0),
		new LteBand(35, 36350, 36949, 1850000, 0),
		new LteBand(36, 36950, 37549, 1930000, 0),
		new LteBand(37, 37550, 37749, 1910000, 0),
		new LteBand(38, 37750, 38249, 2570000, 0),
		new LteBand(39, 38250, 38649, 1880000, 0),
		new LteBand(40, 38650, 39649, 2300000, 0),
		new LteBand(41, 39650, 41589, 2496000, 0),
		new LteBand(42, 41590, 43589, 3400000, 0),
		new LteBand(43, 43590, 45589, 3600000, 0),
		new LteBand(44, 45590, 46589, 703000, 0),
		new LteBand(48, 55240, 56739, 3550000, 0),
	            // Non-standard bands
		new LteBand(50, 50000, 51709, 698000, 0),
		new LteBand(51, 51710, 52159, 1390000, 0),
		new LteBand(52, 52160, 52609, 1785000, 0),
		new LteBand(53, 52610, 53209, 2290000, 0),
		new LteBand(54, 53210, 53849, 2480000, 0),
		new LteBand(55, 53850, 54849, 3300000, 0),
		new LteBand(56, 54850, 65349, 4900000, 0),      

		new LteBand(65, 65536, 66435, 2110000, 190000),
		new LteBand(65, 131072, 131971, 2110000, 190000),
		new LteBand(66, 66436, 67135, 2110000, 400000), // Only supports symmetrical part
		new LteBand(66, 131972, 132322, 2110000, 400000),

		new LteBand(68, 67536, 67835, 753000, 55000),
		new LteBand(68, 132672, 132971, 753000, 55000))
	);
}

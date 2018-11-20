package testsNG.Actions.Utils;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;

import Utils.GeneralUtils;

import java.io.*;
import java.util.HashMap;

/**
 * Created by owiesel on 03-Jul-16.
 */
public class CalculatorMeasurments {
	String fileName;
	private FileInputStream inputStream;
	Workbook workbook;
	public final String createdFile = "FileForWorkOnCalculatorMeasurments.xlsx";

	public enum Measurement {
		PEAK("peak per enodeb"), UPLINK("uplink"), DOWNLINK("downlink");

		private String name;

		Measurement(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

	}

	public enum Type {
		TDD("TDD"), FDD("FDD");

		private String name;

		Type(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

	}

	public enum Settings {
		BANDWIDTH("channel bandwidth"), DUPLEX("duplex"), FRAME_SPLIT("frame split"), SUBFRAME(
				"special subframe"), UE_CATEGORY("ue category"), CONFIGURATION("configuration"), MULTIPLEXING(
						"multiplexing"), NUM_SYM(
								"num_sym"), MAX_UES("max supported"), SR_PERIOD("sr period"), CQI_PERIOD("cqi period");

		private String name;

		Settings(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	public enum DuplexType {
		FDD("FDD"), TDD("TDD");

		private String name;

		DuplexType(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	public CalculatorMeasurments(String fileName) throws IOException, InvalidFormatException {
		this.fileName = fileName;
		inputStream = new FileInputStream(fileName);
		workbook = WorkbookFactory.create(inputStream);
	}

	public HashMap<Measurement, Double> getMeasurments(int bandwitdh, Duplex duplex, int ue, String multiplexing,
			CalculatorMeasurments.User user) throws IOException {

		Sheet sheet = workbook.getSheetAt(0);
		//////////////////////////
		int[] loc = getCell(Settings.BANDWIDTH, workbook);
		Row row = sheet.getRow(loc[0]);
		Cell cell = row.createCell(loc[1]);
		FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
		evaluator.evaluateFormulaCell(cell);
		cell.setCellValue(bandwitdh);
		//////////////////////////
		if (duplex.istDD()) {
			loc = getCell(Settings.DUPLEX, workbook);
			row = sheet.getRow(loc[0]);
			cell = row.createCell(loc[1]);
			GeneralUtils.printToConsole();
			cell.setCellValue(DuplexType.TDD.getName());
			//////////////////////////
			loc = getCell(Settings.FRAME_SPLIT, workbook);
			row = sheet.getRow(loc[0]);
			cell = row.createCell(loc[1]);
			cell.setCellValue(duplex.getFrameSplit());
			//////////////////////////
			loc = getCell(Settings.SUBFRAME, workbook);
			row = sheet.getRow(loc[0]);
			cell = row.createCell(loc[1]);
			cell.setCellValue(duplex.getSubframe());
			//////////////////////////
		} else {
			loc = getCell(Settings.DUPLEX, workbook);
			row = sheet.getRow(loc[0]);
			cell = row.createCell(loc[1]);
			cell.setCellValue(DuplexType.FDD.getName());
		}
		//////////////////////////
		loc = getCell(Settings.UE_CATEGORY, workbook);
		row = sheet.getRow(loc[0]);
		cell = row.createCell(loc[1]);
		cell.setCellValue(ue);
		//////////////////////////
		loc = getCell(Settings.CONFIGURATION, workbook);
		row = sheet.getRow(loc[0]);
		cell = row.createCell(loc[1]);
		cell.setCellValue("User");
		//////////////////////////
		loc = getCell(Settings.MULTIPLEXING, workbook);
		row = sheet.getRow(loc[0]);
		cell = row.createCell(loc[1]);
		cell.setCellValue(multiplexing);
		//////////////////////////
		setUser(user);
		evaluator.evaluateAll();
		return getDownlinkMesurmentPlace(workbook);
	}

	public void setUser(User user) {
		Sheet sheet = workbook.getSheetAt(0);
		int[] loc = getCell(Settings.NUM_SYM, workbook);
		Row row = sheet.getRow(loc[0]);
		Cell cell = row.createCell(loc[1]);
		cell.setCellValue(user.getNumSym());
		//////////////////////////
		loc = getCell(Settings.MAX_UES, workbook);
		row = sheet.getRow(loc[0]);
		cell = row.createCell(loc[1]);
		cell.setCellValue(user.getMaxUes());
		//////////////////////////
		loc = getCell(Settings.SR_PERIOD, workbook);
		row = sheet.getRow(loc[0]);
		cell = row.createCell(loc[1]);
		cell.setCellValue(user.getsRPeriod());
		//////////////////////////
		loc = getCell(Settings.CQI_PERIOD, workbook);
		row = sheet.getRow(loc[0]);
		cell = row.createCell(loc[1]);
		cell.setCellValue(user.getcQiPeriod());
		//////////////////////////
	}

	private int[] getCell(Settings setting, Workbook workbook) {
		int[] outline;
		Sheet sheet = workbook.getSheetAt(0);
		for (int i = 0; i < 100; i++) {
			for (int j = 0; j < 100; j++) {
				Row row = sheet.getRow(j);
				if (row == null)
					continue;
				Cell cell = row.getCell(i);
				FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
				if (cell != null && cell.toString().trim().toLowerCase().contains(setting.getName())) {
					evaluator.evaluateFormulaCell(cell);
					outline = new int[] { j, i + 1 };
					return outline;
				}
			}
		}
		return null;
	}

	private HashMap<Measurement, Double> getDownlinkMesurmentPlace(Workbook workbook) {
		
		HashMap<Measurement, Double> hashMap = new HashMap<Measurement,Double>();
		Sheet sheet = workbook.getSheetAt(0);
		for (int i = 0; i < 100; i++) {
			for (int j = 0; j < 100; j++) {
				Row row = sheet.getRow(j);
				if (row == null)
					continue;
				Cell cell = row.getCell(i);
				if (cell != null && cell.toString().trim().toLowerCase().contains(Measurement.PEAK.getName())) {
					cell = row.getCell(i + 1);
					hashMap.put(Measurement.DOWNLINK, cell.getNumericCellValue());
					cell = row.getCell(i + 2);
					hashMap.put(Measurement.UPLINK, cell.getNumericCellValue());
					return hashMap;
				}

			}
		}
		return null;
	}

	public String getVersion() {
		String ver = "";
		Sheet sheet = workbook.getSheet("Rev History");
		int lastRaw = getLastRaw(sheet);
		Row row = sheet.getRow(lastRaw);
		int i = 0;
		while (row.getCell(i) != null) {
			Cell cell = row.getCell(i++);
			switch (cell.getCellType()) {
			case Cell.CELL_TYPE_NUMERIC:
				ver += cell.getNumericCellValue() + "\t";
				continue;
			case Cell.CELL_TYPE_STRING:
				ver += cell.getStringCellValue() + "\t";
				continue;
			}
		}
		return ver.trim();
	}

	private int getLastRaw(Sheet sheet) {
		for (int i = sheet.getFirstRowNum() + 1; i < 1000; i++) {
			Row row = sheet.getRow(i);
			if (row == null) {
				return i - 1;
			}
		}
		return -1;
	}

	public class User {
		private int numSym;
		private int maxUes;
		private int sRPeriod;
		private int cQiPeriod;

		public User(int numSym, int maxUes, int sRPeriod, int cQiPeriod) {
			this.numSym = numSym;
			this.maxUes = maxUes;
			this.sRPeriod = sRPeriod;
			this.cQiPeriod = cQiPeriod;
		}

		public int getNumSym() {
			return numSym;
		}

		public int getMaxUes() {
			return maxUes;
		}

		public int getsRPeriod() {
			return sRPeriod;
		}

		public int getcQiPeriod() {
			return cQiPeriod;
		}
	}

	public class Duplex {
		Type tDD;
		String frameSplit;
		String subframe;
		TptCalculatorFrameConfigEnumNetSpan frameConfigEnum;
		TptCalculatorSubFrameEnumNetSpan subFrameEnum;
		/**
		 * C'tor for net span access parameters
		 * @param tDD
		 * @param frameSplit
		 * @param subframe
		 * @throws Exception 
		 */
		public Duplex(Type tDD, String frameSplit, String subframe) throws Exception {
			this.tDD = tDD;
			this.frameSplit = frameSplit;
			if(!checkMyEnumForStringFrameConfig(frameConfigEnum, frameSplit)){
				throw new Exception("Fail Initialize FrameConfig Value");
			}
			if(!checkMyEnumForStringSubFrame(subFrameEnum,subframe)){
				throw new Exception("Fail Initialize SubFrame Value");
			}
		}
		
		private boolean checkMyEnumForStringFrameConfig(TptCalculatorFrameConfigEnumNetSpan frameConfigEnum, String value){
				if(value.equals("1") || value.equals("DL_40_UL_40_SP_20")){
					this.frameSplit = TptCalculatorFrameConfigEnumNetSpan.one.getFrameConfig();
					return true;
				}else{
					if(value.equals("2") || value.equals("DL_60_UL_20_SP_20")){
					this.frameSplit = TptCalculatorFrameConfigEnumNetSpan.two.getFrameConfig();
					return true;
					}else{
						return false;
					}
				}	
		 }
	  
		private boolean checkMyEnumForStringSubFrame(TptCalculatorSubFrameEnumNetSpan subFrameEnum, String value){
			if(value.equals("SSF_5")|| value.equals("5")){
				this.subframe = TptCalculatorSubFrameEnumNetSpan.five.getSubFrame();
				return true;
			}else{
				if(value.equals("7")||value.equals("SSF_7")){
					this.subframe = TptCalculatorSubFrameEnumNetSpan.seven.getSubFrame();
					return true;
				}else{
					return false;
				}
			}
		}
	
		
		public boolean istDD() {
			return tDD == Type.TDD;
		}

		public String getFrameSplit() {
			return frameSplit;
		}

		public String getSubframe() {
			return subframe;
		}
	}
	/*
	 * public static void main(String[] args) throws Exception { String
	 * excelFilePath =
	 * "C:\\Users\\hgoldburd\\Desktop\\LTE Throughput calculator v_14.5.xlsx";
	 * CalculatorMeasurments calculatorMeasurments = new
	 * CalculatorMeasurments(excelFilePath); CalculatorMeasurments.Duplex duplex
	 * = calculatorMeasurments.new Duplex(Type.TDD,"(2)    DL  80% : UL 20%",
	 * "(7)     15.4   [Km]"); CalculatorMeasurments.User user =
	 * calculatorMeasurments.new User(2,64,10,10);
	 * HashMap<CalculatorMeasurments.Measurement, Double> hashMap =
	 * calculatorMeasurments.getMeasurments(20,duplex,4,"PT2P", user); String s
	 * = calculatorMeasurments.getVersion(); GeneralUtils.printToConsole(
	 * "The downlink of the ue's is: " +
	 * hashMap.get(CalculatorMeasurments.Measurement.DOWNLINK)+"\n" +
	 * "The uplink of the ue's is: " +
	 * hashMap.get(CalculatorMeasurments.Measurement.UPLINK)+ "\n" +
	 * "The version is: " + s); }
	 */
}

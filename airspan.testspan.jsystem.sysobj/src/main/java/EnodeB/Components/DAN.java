package EnodeB.Components;

import java.util.Hashtable;

import jsystem.framework.report.Reporter;
import EnodeB.Components.Log.LoggerEvent;
import Utils.GeneralUtils;
import Utils.MoxaCom;

public class DAN extends EnodeBComponent {
	private static final String STATUS_TABLE_HEADERS_RAW = "|  Node  |  Stage    |   Time   | ExcAddr  | Cause | ExcData  |      Assert     |";
	private static final String[] STATUS_TABLE_HEADERS = {"Node", "Stage", "Time", "ExcAddr", "Cause" , "ExcData", "Assert"};
	private static final String  STATUS_TABLE_TIME_HEADER = "Time";
	private static final String  STATUS_TABLE_ASSERT_HEADER = "Assert";
	private static final String STATUS_TABLE_TAIL    = "Total running nodes:";
	
	private boolean statusTableFlag;
	private String lastStatusTable;
	
	@Override
	public void init() throws Exception {
		this.statusTableFlag = false;	
		super.init();
	}
	
	
	@Override
	public void getLogLine(LoggerEvent e) {
		String line = e.getLine();
		if (line.contains(STATUS_TABLE_HEADERS_RAW)) {
			lastStatusTable = line + "\n";
			statusTableFlag = true;
		} else if (line.contains(STATUS_TABLE_TAIL)) {
			lastStatusTable += line;
			statusTableFlag = false;
			printAssert();
		} else if (statusTableFlag)
			lastStatusTable += line + "\n";
		
		super.getLogLine(e);
	}
	
	public Hashtable<String, String[]> analyzeStatusTable() {
		return analyzeStatusTable(lastStatusTable);
	}
	
	public Hashtable<String, String[]> analyzeStatusTable(String table) {
		if (table == null || table.isEmpty()) return null;
		Hashtable<String, String[]> analyzedTable = new Hashtable<String, String[]>();
		
		int headersIndx = table.indexOf(STATUS_TABLE_HEADERS_RAW);
		int tailIndx    = table.indexOf(STATUS_TABLE_TAIL);
		if (headersIndx == -1 || tailIndx == -1) {
			GeneralUtils.printToConsole(table);
			return null;
		}
		
		String rowNumString = table.substring(tailIndx + STATUS_TABLE_TAIL.length()).trim();
		int rowNum = Integer.parseInt(rowNumString);
		// add rows to analyzed table
		for(int headerIndx = 0; headerIndx < STATUS_TABLE_HEADERS.length; headerIndx++) {
			String[] colRows = new String[rowNum];
			analyzedTable.put(STATUS_TABLE_HEADERS[headerIndx], colRows);
		}
		rowNum -= 1;
		
		table = table.substring(headersIndx, tailIndx);
		table = table.replaceAll("\t", "");
		String[] lines = table.split("\n");
		for (int lineIndx = 1; lineIndx < lines.length; lineIndx++) {
			lines[lineIndx] = trimSpaces(lines[lineIndx].trim()); 
		
			String[] cols = lines[lineIndx].split(" ");
			if (cols.length != STATUS_TABLE_HEADERS.length)
				continue;
			
			for (int colIndx = 0; colIndx < cols.length; colIndx++) {
				String col = cols[colIndx].trim();
				String[] colRows = analyzedTable.get(STATUS_TABLE_HEADERS[colIndx]);
				colRows[rowNum] = col;
				analyzedTable.put(STATUS_TABLE_HEADERS[colIndx], colRows);
			}
			
			rowNum--;
			if (rowNum == -1)
				break;
			
		}
		
		return analyzedTable;
	}
	
	private String trimSpaces(String row) {
		char[] line = row.toCharArray();
		String newLine = "";
		for (int i = 1; i < line.length; i++) {
			if (line[i] == ' ' && line[i - 1] == ' ') continue;
			newLine += "" + line[i];
		}
		
		return newLine;
	}
	
	private void printAssert() {
		String phyAssert = getFirstAssert(analyzeStatusTable());
		printRawStatusTable(phyAssert != null ? Reporter.FAIL : Reporter.PASS);
		if (phyAssert != null)
			report.report("Assert is: " + phyAssert,Reporter.WARNING);
	}
	
	public String getFirstAssert(Hashtable<String, String[]> statusTable) {
		if (statusTable == null)
			return null;
		
		String[] timeCol = statusTable.get(STATUS_TABLE_TIME_HEADER);
		int smallestTimeIndx = -1;
		boolean assertFound = false;
		for (int rowIndx = 1; rowIndx < timeCol.length; rowIndx++) {
			if (!isHexValue(timeCol[rowIndx]))
				continue;
			
			assertFound = true;
			try {
				if (smallestTimeIndx == -1)
					smallestTimeIndx = rowIndx;
				else if (timeCol[rowIndx].compareTo(timeCol[smallestTimeIndx]) < 0)
					smallestTimeIndx = rowIndx;
			} catch (Exception e) {
				GeneralUtils.printToConsole("Couldn't compare asserts.");
				e.printStackTrace();
			}
		}
		
		if (assertFound)
			return statusTable.get(STATUS_TABLE_ASSERT_HEADER)[smallestTimeIndx];
		
		GeneralUtils.printToConsole("Phy assert not found");
		return null;	
	}
	
	private boolean isHexValue(String value) {
		return value.contains("0x");
	}
	
	public void printRawStatusTable(int reporterStatus) {
		if (lastStatusTable != null && !lastStatusTable.isEmpty())
			GeneralUtils.printToConsole(lastStatusTable);
	}


	@Override
	public String getParallelCommandsPrompt() {
		return SHELL_PROMPT;
	}
	
	public void createSerialCom(String serialIP, int port) {
		serialCom = new MoxaCom();
		serialCom.setComName(serialIP);
		serialCom.setName(serialIP);
		serialCom.setPort(port);
	}
}

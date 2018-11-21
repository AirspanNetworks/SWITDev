package Utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

/* 
 * Author - Gabriel Grunwald
 * 6-7-16
 */

public class ObjectStream {
	
	public LinkedHashMap<String,LinkedHashMap<String,String>> outerMapBasic;
	public LinkedHashMap<String,LinkedHashMap<String,String>> outerMapError;
	public LinkedHashMap<String,LinkedHashMap<String,String>> outerMapRest;
	
	private ArrayList<String> listBasic;
	
	/* Inner static class - singleton.
	 * Keeps a list of key words for the basic table*/
	private static class ListBasic{
		private static ListBasic listBasic = null;
		private static ArrayList<String> myBasicList;
		
		private ListBasic(){
			myBasicList = new ArrayList<String>();
			myBasicList.add("TotalTxCount");
			myBasicList.add("TotalRxCount");
			myBasicList.add("TotalTxRate");
			myBasicList.add("TotalRxRate");
			myBasicList.add("TxL1Count");
			myBasicList.add("RxL1Count");
			myBasicList.add("TxL1Rate");
			myBasicList.add("RxL1Rate");
			myBasicList.add("GeneratorCount");
			myBasicList.add("GeneratorSigCount");
			myBasicList.add("RxSigCount");
			myBasicList.add("GeneratorRate(fps)");
			myBasicList.add("GeneratorRate(bps)");
			myBasicList.add("GeneratorRate(Mps)");
			myBasicList.add("GeneratorSigRate(fps)");
			myBasicList.add("RxSigRate");
		}
		
		// get instance of ListBasic class
		public static ListBasic getInstance(){
			if(null == listBasic){
				listBasic = new ListBasic();
			}
			return listBasic;
		}
		// get list of key words
		public ArrayList<String> getListBasic()
		{
			return myBasicList;
		}
	}
	
	// c-tor
	public ObjectStream(String name){
		outerMapBasic = new LinkedHashMap<String,LinkedHashMap<String,String>>();
		outerMapError = new LinkedHashMap<String,LinkedHashMap<String,String>>();
		outerMapRest = new LinkedHashMap<String,LinkedHashMap<String,String>>();
		listBasic = ListBasic.getInstance().getListBasic();
	}
	
	// add values to hashmap by key of TS
	public void addValues(String TS, ArrayList<String> cName, ArrayList<String> value){
		LinkedHashMap<String,String> innerMapBasic = new LinkedHashMap<String,String>();
		LinkedHashMap<String,String> innerMapError = new LinkedHashMap<String,String>();
		LinkedHashMap<String,String> innerMapRest = new LinkedHashMap<String,String>();

		int size = cName.size();
		
		for(int i=0;i<size;i++){
			if(listBasic.contains(cName.get(i))){
				innerMapBasic.put(cName.get(i), value.get(i));
			}
			else if(cName.get(i).contains("Error")){
				innerMapError.put(cName.get(i), value.get(i));
			}
			else{
				innerMapRest.put(cName.get(i), value.get(i));
			}
		}
		// only if values were added to the inner map, insert to outer map
		if(innerMapBasic.size()!=0){
			outerMapBasic.put(TS, innerMapBasic);
		}
		if(innerMapError.size()!=0){
			outerMapError.put(TS, innerMapError);
		}
		if(innerMapRest.size()!=0){
			outerMapRest.put(TS, innerMapRest);
		}
	}
	
	public String printBasicTableHTML(){
		// print only if not empty
		if(outerMapBasic.size()==0){
			return null;
		}
		return printTableHtml(outerMapBasic, "Basic");
	}
	
	public String printErrorTableHTML(){
		// print only if not empty
		if(outerMapError.size()==0){
			return null;
		}
		return printTableHtml(outerMapError, "Error");
	}
	
	public String printRestTableHTML(){
		// print only if not empty
		if(outerMapRest.size()==0){
			return null;
		}
		return printTableHtml(outerMapRest, "Rest");
	}
	
	// generic function to print a table
	public String printTableHtml(LinkedHashMap<String,LinkedHashMap<String,String>> map, String str){
		
		LinkedHashMap<String,String> ent = null;
		// get the according keys
		switch (str) {
			case "Basic":
				ent = getFirstHashBasic();
				break;
			case "Error":
				ent = getFirstHashError();
				break;
			default:
				ent = getFirstHashRest();
				break;
		}
		// open table declaration html
		String toPrint ="<table><tr><th height=\"30\"></th>";
		// add all keys
		for(Entry<String, String> entry : ent.entrySet())
		{
			toPrint += "<th width=\"100\"><div>"+entry.getKey()+"</div></th>";
		}
		toPrint+="</tr>";
		// add all values by key of TS
		for (Entry<String, LinkedHashMap<String, String>> entry : map.entrySet()){
			toPrint = toPrint + "<tr><th>" + entry.getKey() + "</th>";
			LinkedHashMap<String,String> toIter = entry.getValue();
			for(Entry<String, String> entIn : toIter.entrySet()){
				if(null==entIn.getValue()){
					toPrint += "<td>-</td>";
				}
				else{
					toPrint += "<td>"+entIn.getValue()+"</td>";
				}
			}
			toPrint+="</tr>";
		}
		toPrint += "</table>";
		
		return toPrint;
	}
	
	//print all tables: Basic, Error, Rest
	public String printTablesHTML(){
		
		String basicTable = printBasicTableHTML();
		String errorTable = printErrorTableHTML();
		String restTable = printRestTableHTML();
		
		String print = "<!DOCTYPE html><html><head><style>table, th, td {border: 1px solid black; border-collapse: collapse; text-align: center;}</style></head><body>";
		if(basicTable!=null){
			print+="<td>Basic Table</td>"+basicTable;
		}
		if(errorTable!=null){
			print+="<br>Error Table<br>"+errorTable;
		}
		if(restTable!=null){
			if(basicTable==null && errorTable==null){
				print+=restTable;
			}
			else{
				print+="<br>Rest Table<br>"+restTable;
			}
		}
		print+="</body></html>";
		return print;
	}

	/** get First Hash Basic value
	 * @return
	 */
	public LinkedHashMap<String,String> getFirstHashBasic(){
		return outerMapBasic.entrySet().stream().findFirst().map(Entry::getValue).orElse(null);
	}

	/** get First Hash Error value
	 * @return
	 */
	public LinkedHashMap<String,String> getFirstHashError(){
		return outerMapError.entrySet().stream().findFirst().map(Entry::getValue).orElse(null);
	}

	/** get First Hash Error value
	 * @return
	 */
	public LinkedHashMap<String,String> getFirstHashRest(){
		return outerMapRest.entrySet().stream().findFirst().map(Entry::getValue).orElse(null);
	}
}

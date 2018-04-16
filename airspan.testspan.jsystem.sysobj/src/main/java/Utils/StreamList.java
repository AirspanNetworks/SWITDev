package Utils;

import java.util.ArrayList;
import java.util.HashMap;

/* 
 * Author - Gabriel Grunwald
 * 6-7-16
 */

public class StreamList {
	
	HashMap<String, ObjectStream> listOfStreams;
	
	public StreamList(){
		listOfStreams = new HashMap<String, ObjectStream>();
	}
	
	/**
	 * add a set of values to table
	 * @param streamName = table name
	 * @param TS = left column parameter
	 * @param cName = headlines of the table
	 * @param value = each headline value.
	 *  _________________________________________
	 * |_____________|cName1|cName2|...._________|
	 * |TS___________|value1|value2|...._________| 
	 * 
	 */
	public void addValues(String streamName, String TS, ArrayList<String> cName, ArrayList<String> value){
		
		if(!listOfStreams.containsKey(streamName)){
			addStream(streamName);
		}
		listOfStreams.get(streamName).addValues(TS, cName, value);

	}
	
	public void addStream(String stream){
		listOfStreams.put(stream, new ObjectStream(stream));
	}
	
	public String printTablesHtmlForStream(String stream){
		return listOfStreams.get(stream).printTablesHTML();
	}
}

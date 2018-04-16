package EnodeB.Components.Log;

import java.awt.event.ActionEvent;

public class LoggerEvent extends ActionEvent {

	/**
	 * For serialization purposes. Auto generated by Eclipse
	 */
	private static final long serialVersionUID = -1894611721288851503L;
	
	private String line;
	
	/**
	 * 
	 * @param source - The logger that originated the event
	 * @param line - The log line the logger got.
	 * @param id - An integer that identifies the event. For information on allowable values, see the class description for ActionEvent
	 * @param command - A string that may specify a command (possibly one of several) associated with the event 
	 */
	public LoggerEvent(Logger source, String line, int id, String command) {
		super(source, id, command);
		this.line = line;
	}
	
	public String getLine() {
		return line;
	}
	
}

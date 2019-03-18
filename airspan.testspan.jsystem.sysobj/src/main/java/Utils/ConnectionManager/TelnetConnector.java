package Utils.ConnectionManager;

import java.io.IOException;

import Utils.GeneralUtils;
import Utils.ConnectionManager.*;
import jsystem.framework.report.Reporter;
import systemobject.terminal.Telnet;
import systemobject.terminal.Terminal;


public class TelnetConnector implements IRemoteConnector {
	
	/** The terminal object is the connector. */
	public Terminal terminal;
	
	private ConnectionInfo connect_info;
	
	/** The is connected. */
	private boolean isConnected = false;

	/** The Constant TIMEOUT. */
	private static final int TIMEOUT = 120000;

	/** The Constant MAX_TRYOUTS. */
	private static final int MAX_TRYOUTS = 3;
	
	/**
	 * Instantiates a new Telnet connector.
	 * 
	 * @param ipAddress the ip address
	 * @param user the user to the server
	 * @param password the password to the server
	 */
	public TelnetConnector(ConnectionInfo connection_info) {
		this.connect_info = connection_info;
		this.isConnected = false;
	}
	
	@Override
	public void initConnection() {
		this.terminal = new Telnet(connect_info.host, connect_info.port);
		this.terminal.removePrompts();
		this.terminal.setScrollEndTimeout(TIMEOUT);
		this.terminal.setAsciiFilter(true);
		int tryouts = 0;
		do {
			tryouts++;
			GeneralUtils.printToConsole( String.format( "Initiate connection to  %s Telnet server," + " attempt#%d\n", this.connect_info.host, tryouts ) );
			try {
				terminal.connect();
				isConnected = true;
				break; // out of the loop if get connected
			} catch ( Exception e ) {
				GeneralUtils.printToConsole( String.format( "Could not connect to %s Telnet server, reason: %s", this.connect_info.host, e.getMessage() ) );
				try {
					Thread.sleep( RECONNECT_TIMEOUT );
				} catch ( InterruptedException e1 ) {
					e1.printStackTrace();
				}
				continue;
			}
		} while ( tryouts < MAX_TRYOUTS );
		if ( tryouts == MAX_TRYOUTS )
			isConnected = false;
	}

	@Override
	public void disconnect() {
		try {
			if ( isConnected ) {
				this.terminal.disconnect();
				isConnected = false; // set connection state to disconnect
				GeneralUtils.printToConsole( String.format( "SSH session to %s was closed by Automation", this.connect_info.host ) );
			} else {
				GeneralUtils.printToConsole( String.format( "SSH Session to %S is already disconnected", this.connect_info.host ) );
			}
		} catch ( Exception e ) {
			GeneralUtils.printToConsole( String.format( "Could not disconnect %s SSH session, reason:%s", this.connect_info.host, e.getMessage() ) );
		}
	}

	@Override
	public void reConnect() {
		// TODO Auto-generated method stub

	}

	@Override
	public String sendCommand(String command, int timeout){
		String result = "";
		try {
			this.terminal.sendString(command, false);
			result = this.terminal.readInputBuffer();
		
		} catch (IOException | InterruptedException e) {
			 e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return result;
	}

	@Override
	public String sendCommand(String command, int retries, int timeout) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String readCommandLine(long timeout, String finish) {
		// TODO Auto-generated method stub
		return null;
	}



}

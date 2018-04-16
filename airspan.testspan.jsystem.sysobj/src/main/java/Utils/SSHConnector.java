package Utils;

import systemobject.terminal.SSH;
import systemobject.terminal.Terminal;

/**
 * The Class SSHConnector provides a safe way to open & close an SSH session.
 */
public class SSHConnector {

	/** The terminal object is the connector. */
	private Terminal terminal;

	/** The port. */
	private int port = 22;

	/** The user name. */
	private String userName;

	/** The password. */
	private String password;

	/** The host. */
	private String host;

	/** The is connected. */
	private boolean isConnected;

	/** The Constant TIMEOUT. */
	private static final int TIMEOUT = 120000;

	/** The Constant MAX_TRYOUTS. */
	private static final int MAX_TRYOUTS = 3;

	/** The Constant RETRY_TIMER. */
	public static final int RECONNECT_TIMEOUT = 500;

	/**
	 * Instantiates a new SSH connector.
	 * 
	 * @param ipAddress the ip address
	 * @param user the user to the server
	 * @param password the password to the server
	 */
	public SSHConnector( String ipAddress, String user, String password ) {
		this.userName = user;
		this.password = password;
		this.host = ipAddress;
		this.isConnected = false;
	}

	/**
	 * Inits the connection with all the settings that were set, removing all the current
	 * promts. The method includes a tryout mechanism: try to connect, if fails try const
	 * number of times more, and sleep between each try a const mSec.
	 */
	public void initConnection() {
		this.terminal = new SSH( this.getHost(), this.userName, this.password );
		this.terminal.removePrompts();
		this.terminal.setScrollEndTimeout( TIMEOUT );
		this.terminal.setAsciiFilter( true );
		int tryouts = 0;
		do {
			tryouts++;
			GeneralUtils.printToConsole( String.format( "Initiate connection to  %s SSH server," + " attempt#%d\n", host, tryouts ) );
			try {
				terminal.connect();
				isConnected = true;
				break; // out of the loop if get connected
			} catch ( Exception e ) {
				GeneralUtils.printToConsole( String.format( "Could not connect to %s SSH server, reason: %s", host, e.getMessage() ) );
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

	/**
	 * Disconnect SSH session (only if needed).
	 */
	public void disconnect() {
		try {
			if ( isConnected() ) {
				this.terminal.disconnect();
				isConnected = false; // set connection state to disconnect
				GeneralUtils.printToConsole( String.format( "SSH session to %s was closed by Automation", host ) );
			} else {
				GeneralUtils.printToConsole( String.format( "SSH Session to %S is already disconnected", host ) );
			}
		} catch ( Exception e ) {
			GeneralUtils.printToConsole( String.format( "Could not disconnect %s SSH session, reason:%s", host, e.getMessage() ) );
		}
	}

	/**
	 * ReConnect to the SSH channel that was last set.
	 */
	public void reConnect() {
		disconnect();
		initConnection();
	}

	/**
	 * Send command via SSH session.
	 * 
	 * @param command the command
	 * @param timeout the time to wait to pull the output in mSec
	 * @return output of that command
	 * @throws Exception the exception
	 */
	public String sendCommand( String command,int timeout ){
		return sendCommand( command, MAX_TRYOUTS ,timeout);
	}

	/**
	 * Send command via SSH session.
	 * 
	 * @param command the command
	 * @param retries the number of retries in case it fails.
	 * @param timeout the time to wait to pull the output in mSec
	 * @return the output of that command
	 * @throws Exception the exception
	 */
	public String sendCommand( String command, int retries,int timeout ){
		if ( retries > 0 ) {
			String output = "";
			try {
				if ( isConnected() ) {
					this.terminal.sendString( command+"\r", true );
					Thread.sleep( timeout );
					output = this.terminal.readInputBuffer();
					if(this.terminal.getIn().available() > 0){
						Thread.sleep(2000);
						output += this.terminal.readInputBuffer(); //For long output. 
					}
					GeneralUtils.printToConsole( output );
				} else {
					throw new Exception( "Unable to send command :Session was marked as disconnected!" );
				}
			} catch ( Exception e ) {
				GeneralUtils.printToConsole( String.format( "Could not send command via SSH to %s, reason: %s", host, e.getMessage() ) );
				GeneralUtils.printToConsole( "Trying to reconnect..." );
				reConnect();
				return sendCommand( command, retries - 1, timeout);
			}

			return output;
		}
		return null;
	}

	/**
	 * @author ggrunwald
	 * @param timeout - time to wait to desired string
	 * @param finish - the string we search for in the server's console to finish the function
	 * @return answer - output from the ssh console
	 * @throws Exception
	 */
	public String readCommandLine(long timeout,String finish) {
		long startTime = System.currentTimeMillis();
		String answer = "";
		while(System.currentTimeMillis()-startTime<timeout && !answer.contains(finish)){
			try {
				answer += this.terminal.readInputBuffer()+"\n";
			} catch (Exception e) {
				e.printStackTrace();
				break;
			}
			GeneralUtils.unSafeSleep(500);
		}
		return answer;
	}
	
	/**
	 * Gets the port.
	 * 
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Sets the port.
	 * 
	 * @param port the new port
	 */
	public void setPort( int port ) {
		this.port = port;
	}

	/**
	 * Gets the user name.
	 * 
	 * @return the user name
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * Sets the user name.
	 * 
	 * @param userName the new user name
	 */
	public void setUserName( String userName ) {
		this.userName = userName;
	}

	/**
	 * Gets the password.
	 * 
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Sets the password.
	 * 
	 * @param password the new password
	 */
	public void setPassword( String password ) {
		this.password = password;
	}

	/**
	 * Checks if is connected.
	 * 
	 * @return true, if is connected
	 */
	public boolean isConnected() {
		return isConnected;
	}

	/**
	 * Gets the host.
	 * 
	 * @return the host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * Sets the host.
	 * 
	 * @param host the host to set
	 */
	public void setHost( String host ) {
		this.host = host;
	}

}

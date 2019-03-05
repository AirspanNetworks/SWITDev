package Utils.ConnectionManager;

import Utils.ConnectionManager.IRemoteConnector;
import Utils.GeneralUtils;
import systemobject.terminal.SSH;
import systemobject.terminal.Terminal;

/**
 * The Class SSHConnector provides a safe way to open & close an SSH session.
 */
public class SSHConnector implements IRemoteConnector {

	/** The terminal object is the connector. */
	private Terminal terminal;

//	/** The port. */
//	private int port = 22;
//
//	/** The user name. */
//	private String userName;
//
//	/** The password. */
//	private String password;
//
//	/** The host. */
//	private String host;
	
	private ConnectionInfo connect_info;
	/** The is connected. */
	private boolean isConnected = false;

	/** The Constant TIMEOUT. */
	private static final int TIMEOUT = 120000;

	/** The Constant MAX_TRYOUTS. */
	private static final int MAX_TRYOUTS = 3;

	/**
	 * Instantiates a new SSH connector.
	 * 
	 * @param ipAddress the ip address
	 * @param user the user to the server
	 * @param password the password to the server
	 */
	public SSHConnector(ConnectionInfo connect_info) {
		this.connect_info = connect_info;
		this.isConnected = false;
	}

	/* (non-Javadoc)
	 * @see Utils.IRemoteConnector#initConnection()
	 */
	@Override
	public void initConnection() {
		this.terminal = new SSH(this.connect_info.host, this.connect_info.user, this.connect_info.password );
		this.terminal.removePrompts();
		this.terminal.setScrollEndTimeout( TIMEOUT );
		this.terminal.setAsciiFilter( true );
		int tryouts = 0;
		do {
			tryouts++;
			GeneralUtils.printToConsole( String.format( "Initiate connection to  %s SSH server," + " attempt#%d\n", this.connect_info.host, tryouts ) );
			try {
				terminal.connect();
				isConnected = true;
				break; // out of the loop if get connected
			} catch ( Exception e ) {
				GeneralUtils.printToConsole( String.format( "Could not connect to %s SSH server, reason: %s", this.connect_info.host, e.getMessage() ) );
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

	/*
	 * @see Utils.IRemoteConnector#disconnect()
	 */
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

	/*
	 * @see Utils.IRemoteConnector#reConnect()
	 */
	@Override
	public void reConnect() {
		disconnect();
		initConnection();
	}

	/* 
	 * @see Utils.IRemoteConnector#sendCommand(java.lang.String, int)
	 */
	@Override
	public String sendCommand( String command,int timeout ){
		return sendCommand( command, MAX_TRYOUTS ,timeout);
	}

	/* 
	 * @see Utils.IRemoteConnector#sendCommand(java.lang.String, int, int)
	 */
	@Override
	public String sendCommand( String command, int retries,int timeout ){
		if ( retries > 0 ) {
			String output = "";
			try {
				if ( isConnected ) {
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
				GeneralUtils.printToConsole( String.format( "Could not send command via SSH to %s, reason: %s", this.connect_info.host, e.getMessage() ) );
				GeneralUtils.printToConsole( "Trying to reconnect..." );
				reConnect();
				return sendCommand( command, retries - 1, timeout);
			}

			return output;
		}
		return null;
	}

	/* 
	 * @see Utils.IRemoteConnector#readCommandLine(long, java.lang.String)
	 */
	@Override
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
}

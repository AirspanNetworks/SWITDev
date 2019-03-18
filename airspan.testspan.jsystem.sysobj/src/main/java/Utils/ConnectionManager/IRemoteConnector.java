package Utils.ConnectionManager;

public interface IRemoteConnector {

	/** The Constant RETRY_TIMER. */
	int RECONNECT_TIMEOUT = 500;

	/**
	 * Inits the connection with all the settings that were set, removing all the current
	 * promts. The method includes a tryout mechanism: try to connect, if fails try const
	 * number of times more, and sleep between each try a const mSec.
	 */
	void initConnection();

	/**
	 * Disconnect SSH session (only if needed).
	 */
	void disconnect();

	/**
	 * ReConnect to the SSH channel that was last set.
	 */
	void reConnect();

	/**
	 * Send command via SSH session.
	 * 
	 * @param command the command
	 * @param timeout the time to wait to pull the output in mSec
	 * @return output of that command
	 * @throws Exception the exception
	 */
	String sendCommand(String command, int timeout);

	/**
	 * Send command via SSH session.
	 * 
	 * @param command the command
	 * @param retries the number of retries in case it fails.
	 * @param timeout the time to wait to pull the output in mSec
	 * @return the output of that command
	 * @throws Exception the exception
	 */
	String sendCommand(String command, int retries, int timeout);

	/**
	 * @param timeout - time to wait to desired string
	 * @param finish - the string we search for in the server's console to finish the function
	 * @return answer - output from the ssh console
	 * @throws Exception
	 */
	String readCommandLine(long timeout, String finish);

//	/**
//	 * Gets the port.
//	 * 
//	 * @return the port
//	 */
//	int getPort();
//
//	/**
//	 * Sets the port.
//	 * 
//	 * @param port the new port
//	 */
//	void setPort(int port);
//
//	/**
//	 * Gets the user name.
//	 * 
//	 * @return the user name
//	 */
//	String getUserName();
//
//	/**
//	 * Sets the user name.
//	 * 
//	 * @param userName the new user name
//	 */
//	void setUserName(String userName);
//
//	/**
//	 * Gets the password.
//	 * 
//	 * @return the password
//	 */
//	String getPassword();
//
//	/**
//	 * Sets the password.
//	 * 
//	 * @param password the new password
//	 */
//	void setPassword(String password);
//
//	/**
//	 * Checks if is connected.
//	 * 
//	 * @return true, if is connected
//	 */
//	boolean isConnected();
//
//	/**
//	 * Gets the host.
//	 * 
//	 * @return the host
//	 */
//	String getHost();
//
//	/**
//	 * Sets the host.
//	 * 
//	 * @param host the host to set
//	 */
//	void setHost(String host);

}
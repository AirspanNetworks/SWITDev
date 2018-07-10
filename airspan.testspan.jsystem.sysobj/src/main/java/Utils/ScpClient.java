package Utils;

import java.io.IOException;

import jsystem.framework.report.Reporter;
import jsystem.framework.system.SystemObjectImpl;
import ch.ethz.ssh2.ConnectionInfo;
import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.Connection;

public class ScpClient extends SystemObjectImpl {

    
    private static final int NUMBER_OF_RETRIES = 5;
	private String username = "root";
	private String password = "air4best";

	private SCPClient scpClient = null;
	private boolean authenticated = false;
	private ConnectionInfo connInf = null;
	private Connection connection = null;
	private String ipAddress = "";
	private int port = 22;

	/**
	 * Prepares a fresh <code>SecureCopy</code> object which can then be used
	 * perform Secure Copy
	 * 
	 * @param Ip
	 *            Address of remote host.
	 */
	public ScpClient(String IpAddress) {
		this.ipAddress = IpAddress;
	}

	/**
	 * Prepares a fresh <code>SecureCopy</code> object which can then be used
	 * perform Secure Copy
	 * 
	 * @param IpAddress Address of remote host.   
	 * @param user - user name 
	 * @param pass - password
	 */
	public ScpClient(String IpAddress, String user,String pass) {
		ipAddress = IpAddress;
		username  = user;
		password  = pass;
	}
	
	public ScpClient(String IpAddress, String user,String pass, int port) {
		ipAddress = IpAddress;
		username  = user;
		password  = pass;
		this.port = port;
	}
	/**
	 * Download a set of files from the remote server to a local directory.
	 * 
	 * @param remoteFiles
	 *            Paths and names of the remote files.
	 * @param localTargetDirectory
	 *            Local directory to put the downloaded files.
	 * 
	 * @throws IOException
	 */
	public boolean getFiles(String localTargetDirectory, String... remoteFiles){
		boolean result = true;
		createScpClient();
		try {
			scpClient.get(remoteFiles, localTargetDirectory);
		} catch (Exception e) {
			report.report("Can't get scp file");
			e.printStackTrace();
			GeneralUtils.printToConsole("scpClient.getFiles - e.getMessage(): "+e.getMessage());
			GeneralUtils.printToConsole("scpClient.getFiles - e.getCause(): "+e.getCause());
			result = false;
		}
		disconnect();
		return result;
	}

	/**
	 * Copy a set of local files to a remote directory.
	 * 
	 * @param localFiles
	 *            Paths and names of local file names.
	 * @param remoteTargetDirectory
	 *            Remote target directory. Use an empty string to specify the
	 *            default directory.
	 * 
	 * @throws IOException
	 */
	public void putFiles(String remoteTargetDirectory, String... localFiles)
			throws IOException {
		createScpClient();
		try {
			scpClient.put(localFiles, remoteTargetDirectory);
		} catch (IOException e) {
			throw new IOException(e);
		}
		disconnect();
	}

	/**
	 * Close the connection to the SSH-2 server. All assigned sessions will be
	 * closed, too. Can be called at any time. Don't forget to call this once
	 * you don't need a connection anymore - otherwise the receiver thread may
	 * run forever.
	 */
	private void disconnect() {
		connection.close();
		GeneralUtils.printToConsole("Connection closed");
		authenticated = false;
		
	}

	/**
	 * Create a very basic {SCPClient} that can be used to copy files from/to
	 * the SSH-2 server.
	 * <p>
	 * Works only after one has passed successfully the authentication step.
	 * There is no limit on the number of concurrent SCP clients.
	 *
	 */
	private void createScpClient() {
		sessionConnect();
		if (authenticated) {
			try {
				scpClient = connection.createSCPClient();
			} catch (IOException e) {
				report.report("Failed to create SCP client", Reporter.WARNING);
				e.getStackTrace();
			}
		} else {
			report.report("Unable to create SCP client [authentication problem]", Reporter.WARNING);
		}
	}

	/**
	 * Prepares a fresh <code>connection</code> object which can then be used to
	 * establish a connection to the specified SSH-2 server.
	 * 
	 */
	private void sessionConnect() {
	        int retries=0; // number of connection retries 
	        do{
	            	try {
	            	    
                		connection = new Connection(ipAddress, port);                		
                		connInf = connection.connect(null,20000,20000);
                		authenticated = connection.
                			authenticateWithPassword(username,password);	
                		if (authenticated){ break; }
                			
        		} catch (IOException e) {
        	               retries++; 
        	               report.report(e.getMessage());
        	               continue; 
        		       
        		}
	        }while(NUMBER_OF_RETRIES!=retries);
	        
	        // check that is the reason for loop ending
	        // max retries reached or authentication pass
		if (authenticated) {
		    GeneralUtils.printToConsole("authentication passed");
		} else {
		    report.report("authentication failed", Reporter.WARNING);
		}

	}

	public ConnectionInfo getConnInf() {
		return connInf;
	}

	public void setConnInf(ConnectionInfo connInf) {
		this.connInf = connInf;
	}

}

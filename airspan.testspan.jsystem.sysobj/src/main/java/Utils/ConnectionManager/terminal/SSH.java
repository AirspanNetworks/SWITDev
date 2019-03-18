package Utils.ConnectionManager.terminal;

import java.io.IOException;
import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.InteractiveCallback;
import ch.ethz.ssh2.LocalPortForwarder;
import ch.ethz.ssh2.Session;

public class SSH extends Terminal {
	
	private String hostname;

	private String username;

	private String password;

	private Connection conn = null;

	private Session sess = null;
	
	//ssh port forwarding
	private LocalPortForwarder lpf = null;

	private int sourcePort = -1;

	private int destinationPort = -1;
	
	boolean xtermTerminal = true;
    
	
	
	public SSH(String hostnameP, String usernameP, String passwordP) {
		this(hostnameP,usernameP,passwordP,-1,-1,true);
	}
	
	public SSH(String hostnameP, String usernameP, String passwordP, int sourceTunnelPort,int destinationTunnelPort) {
		this(hostnameP,usernameP,passwordP,sourceTunnelPort,destinationTunnelPort,true);
	}

	public SSH(String hostnameP, String usernameP, String passwordP, int sourceTunnelPort,int destinationTunnelPort, boolean _xtermTerminal) {
		super();
		hostname = hostnameP;
		username = usernameP;
		password = passwordP;
		sourcePort = sourceTunnelPort;
		destinationPort =destinationTunnelPort;
		xtermTerminal = _xtermTerminal;
	}

	public void connect() throws IOException {

		/* Create a connection instance */

		conn = new Connection(hostname);

		/* Now connect */

		conn.connect();

		/* Authenticate */
		try {
			boolean isAuthenticated = conn.authenticateWithPassword(username,
					password);
			if (isAuthenticated == false){
				throw new IOException("Authentication failed.");
			}

		} catch (Exception e){ // authenticated method not supported
			// try to use keyboard interactive
			conn.authenticateWithKeyboardInteractive(username, 
					new InteractiveCallback(){
						public String[] replyToChallenge(String arg0,
								String arg1, int arg2, String[] arg3,
								boolean[] arg4) throws Exception {
							return new String[]{password};
						}
				}
			);
		}


		if (sourcePort > -1 && destinationPort > -1){
			lpf = conn.createLocalPortForwarder(sourcePort, "localhost" , destinationPort);
		}
		
		/* Create a session */
		sess = conn.openSession();
		
		if (xtermTerminal){
			sess.requestPTY("xterm",80, 24, 640, 480, null);
		}else {
			sess.requestPTY("dumb", 200, 50, 0, 0, null);
		}
		
		sess.startShell();
		
		in =  sess.getStdout();
		out = sess.getStdin();
	}

	public void disconnect() {
		if(lpf != null) {
			try {
				lpf.close();
			} catch (IOException e) {
			}
		}
		if (sess != null) {
			sess.close();
		}
		if (conn != null) {
			conn.close();
		}
	}

	public boolean isConnected() {
		return true;
	}

	public String getConnectionName() {
		return "SSH";
	}

}

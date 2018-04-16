package EnodeB;

import Utils.SSHConnector;
import jsystem.framework.system.SystemObjectImpl;

/**
 * The Class CoreServer.
 */
public class CoreServer extends SystemObjectImpl {
	//TODO: PULL INFO FROM SUT
	private final String  userName="swuser";
	private final String  password="sw_grp2";
	private  final String  IPAdress="192.168.58.48";
	private SSHConnector ssh;
	
	public static final String COREOPEN="./coreopen.sh ";
	
	@Override
	public void init() throws Exception{
		super.init();
		ssh=new SSHConnector( IPAdress, userName, password );
		ssh.initConnection();
		
	}
	public String sendCommand(String command) throws Exception{
		return ssh.sendCommand( command,2000 );
	}
	
	public String coreOpen(String coreName) throws Exception{
		return sendCommand( COREOPEN+coreName);
	}
	
	
}

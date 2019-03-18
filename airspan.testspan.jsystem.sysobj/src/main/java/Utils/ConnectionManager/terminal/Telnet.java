package Utils.ConnectionManager.terminal;

import java.net.Socket;
import java.io.*;


public class Telnet extends Terminal{

    private String address = null;
    private int port = 0;
    private Socket socket = null;
    private boolean useTelnetInputStream = false;
    private int soTimeout = 0;
    String vtType = "vt100";

    public Telnet(String address, int port){
        this.address = address;
        this.port = port;
    }
    public Telnet(String address, int port, boolean useTelnetInputStream){
        this(address,port);
        this.useTelnetInputStream = useTelnetInputStream;
    }

    public void connect() throws IOException {
		socket = new Socket(address,port);
		socket.setReceiveBufferSize(4*1024);
		socket.setSoTimeout(soTimeout);
		//socket.setKeepAlive(true);
		if (useTelnetInputStream) {
			//in = new  TelnetInputStream(socket.getInputStream(), socket.getOutputStream());
		    //in = new BufferedInputStream(new TelnetInputStream( socket.getInputStream(), socket.getOutputStream(), 134, 46, "vt100"),IN_BUFFER_SIZE);
	        //in = new BufferedInputStream(new SunTelnetInputStream(socket.getInputStream(), socket.getOutputStream()));
	        in = new TelnetInputStream( socket.getInputStream(), socket.getOutputStream(), 134, 46, vtType);
		} else {
		    in = new BufferedInputStream(socket.getInputStream(),IN_BUFFER_SIZE);
		}
	
		out = new BufferedOutputStream(socket.getOutputStream());
		
    }

    public void disconnect() throws IOException {
		if (socket !=null) {
			socket.shutdownInput();
			socket.shutdownOutput();
			socket.close();
		}
		closeStreams();

    }

    public boolean isConnected(){
		if (socket.isConnected() && !socket.isInputShutdown() && !socket.isOutputShutdown()) {
		    return true;
		} else {
		    return false;
		}
    }

    public String getConnectionName(){
		try {
		    return socket.getLocalAddress().getHostAddress();
		} catch (RuntimeException ignore1){
		}
		return null;
    }

	public int getSoTimeout() {
		return soTimeout;
	}
	public void setSoTimeout(int soTimeout) {
		this.soTimeout = soTimeout;
	}
	public String getVtType() {
		return vtType;
	}
	public void setVtType(String vtType) {
		this.vtType = vtType;
	}
}

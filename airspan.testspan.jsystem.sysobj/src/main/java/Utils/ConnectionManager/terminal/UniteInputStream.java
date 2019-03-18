/*
 * Created on Oct 30, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package Utils.ConnectionManager.terminal;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author embext
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class UniteInputStream extends InputStream {
	InputStream in;
	InputStream err;
	public UniteInputStream(InputStream in, InputStream err){
		this.in = in;
		this.err = err;
	}
	/* (non-Javadoc)
	 * @see java.io.InputStream#read()
	 */
	public int read() throws IOException {
		if(err.available() > 0){
			return err.read();
		} else {
			return in.read();
		}
	}
	
    public int available() throws IOException {
		return in.available() + err.available();
	}
    
    public void close() throws IOException{
    	err.close();
    	in.close();
    }

}

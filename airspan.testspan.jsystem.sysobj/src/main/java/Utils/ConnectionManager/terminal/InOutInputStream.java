/*
 * Created on Sep 23, 2005
 *
 * Copyright 2005 AQUA Software, LTD. All rights reserved.
 * AQUA PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package Utils.ConnectionManager.terminal;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author guy.arieli
 *
 */
public abstract class InOutInputStream extends InputStream {
	protected InputStream in;
	public void setInputStream(InputStream in){
		this.in = in;
	}
	public void close() throws IOException{
		if(in != null){
			in.close();
		}
	}

}

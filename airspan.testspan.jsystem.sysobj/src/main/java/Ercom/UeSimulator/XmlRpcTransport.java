package Ercom.UeSimulator;

import java.net.MalformedURLException;
import java.net.URL;

import Ercom.XmlRpc.axmlrpc.XMLRPCClient;
import Ercom.XmlRpc.axmlrpc.XMLRPCException;

public class XmlRpcTransport implements Transmittable{
	
	
	@Override
	public Object execute(String url, String method, Object... params) throws MalformedURLException{
		XMLRPCClient client = new XMLRPCClient(new URL(url));
		Object val = null;
		try {
			val = client.call(method,params);
		} catch (XMLRPCException e) {
			e.printStackTrace();
		}
		return val;
	}
	
}

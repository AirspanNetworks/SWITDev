package Ercom.UeSimulator;

import java.net.MalformedURLException;

public interface Transmittable {
	public Object execute(String url, String method, Object... params) throws MalformedURLException;
}

package ESon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import Utils.GeneralUtils;
import jsystem.framework.system.SystemObjectImpl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * class saves HTTP cookie and session Id for the current ESon server gets
 * Details from SUT
 */
public class EsonServer extends SystemObjectImpl {

	private String serverIp;
	private String user;
	private String password;
	private OkHttpClient client;
	private int proxyPort;
	private String proxyHost;
	private MyCookieJar cookieJar = new MyCookieJar();
	private ArrayList<Response> responseArray = new ArrayList<Response>();

	// setters and getters

	public ArrayList<Response> getResponses() {
		return responseArray;
	}

	public int getProxyPort() {
		return proxyPort;
	}

	public void setProxyPort(int proxyPort) {
		this.proxyPort = proxyPort;
	}

	public String getProxyHost() {
		return proxyHost;
	}

	public void setProxyHost(String proxyHost) {
		this.proxyHost = proxyHost;
	}

	public String getServerIp() {
		return serverIp;
	}

	public void setServerIp(String serverIp) {
		this.serverIp = serverIp;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	// behavioral methods
	public boolean openConnection() {
		// make fiddler socket as proxy and can see traffic in app
		client = new OkHttpClient.Builder().connectTimeout(60, TimeUnit.SECONDS).writeTimeout(60, TimeUnit.SECONDS)
				.readTimeout(60, TimeUnit.SECONDS)
				// .proxy(new Proxy(Proxy.Type.HTTP, new
				// InetSocketAddress(proxyHost, proxyPort)))
				.cookieJar(cookieJar).followRedirects(false).build();

		Request request = new Request.Builder().url("http://" + serverIp + "/accounts/login/?next=/eson/").get()
				.addHeader("accept", "text/html, application/xhtml+xml, image/jxr, */*")
				.addHeader("accept-encoding", "gzip, deflate").addHeader("accept-language", "en-US,en;q=0.7,he;q=0.3")
				.addHeader("user-agent",
						"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.79 Safari/537.36 Edge/14.14393")
				.addHeader("connection", "Keep-Alive").addHeader("host", serverIp).build();
		try {
			Response response = client.newCall(request).execute();
			responseArray.add(response);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean login() {
		MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
		RequestBody body = RequestBody.create(mediaType, "csrfmiddlewaretoken=" + cookieJar.getCookieValue("csrftoken")
				+ "&username=" + user + "&password=" + password + "&this_is_the_login_form=1");
		Request request = new Request.Builder().url("http://" + serverIp + "/accounts/login/").post(body)
				.addHeader("pragma", "no-cache").addHeader("accept", "text/html, application/xhtml+xml, image/jxr, */*")
				.addHeader("accept-language", "en-US,en;q=0.7,he;q=0.3")
				.addHeader("user-agent",
						"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.79 Safari/537.36 Edge/14.14393")
				.addHeader("content-length", "126").addHeader("content-type", "application/x-www-form-urlencoded")
				.addHeader("referer", "http://" + serverIp + "/accounts/login/?next=/eson/")
				.addHeader("connection", "Keep-Alive").addHeader("host", serverIp)
				.addHeader("cache-control", "no-cache").build();
		try {
			Response response = client.newCall(request).execute();
			responseArray.add(response);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean chooseEsonServer() {
		Request request = new Request.Builder().url("http://" + serverIp + "/eson/action?cmd=set_server&server=SWIT")
				.post(RequestBody.create(MediaType.parse("text/plain"), "")).addHeader("pragma", "no-cache")
				.addHeader("accept", "*/*").addHeader("accept-language", "en-US,en;q=0.7,he;q=0.3")
				.addHeader("x-requested-with", "XMLHttpRequest").addHeader("content-length", "0")
				.addHeader("referer", "http://" + serverIp + "/eson/")
				.addHeader("X-CSRFToken", cookieJar.getCookieValue("csrftoken")).addHeader("connection", "Keep-Alive")
				.addHeader("host", serverIp)
				.addHeader("content-type", "multipart/form-data; boundary=---011000010111000001101001")
				.addHeader("cache-control", "no-cache").build();
		try {
			Response response = client.newCall(request).execute();
			responseArray.add(response);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public String refresh() {
		Request request = new Request.Builder().url("http://" + serverIp + "/eson/action?cmd=refresh").get()
				.addHeader("pragma", "no-cache").addHeader("accept", "*/*")
				.addHeader("accept-language", "en-US,en;q=0.7,he;q=0.3")
				.addHeader("user-agent",
						"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.79 Safari/537.36 Edge/14.14393")
				.addHeader("x-requested-with", "XMLHttpRequest").addHeader("referer", "http://" + serverIp + "/eson/")
				.addHeader("connection", "Keep-Alive").addHeader("host", serverIp)
				.addHeader("cache-control", "no-cache").build();
		try {
			Response response = client.newCall(request).execute();
			responseArray.add(response);
			String jsonData = response.body().string();
			GeneralUtils.printToConsole("Resonse: " + jsonData);
			return jsonData;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String GetEcgiInfo(String ecgi) {
		Request request = new Request.Builder().url("http://" + serverIp + "/eson/coe/" + ecgi + "/").get()
				.addHeader("pragma", "no-cache").addHeader("accept", "*/*")
				.addHeader("accept-language", "en-US,en;q=0.7,he;q=0.3")
				.addHeader("user-agent",
						"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.79 Safari/537.36 Edge/14.14393")
				.addHeader("x-requested-with", "XMLHttpRequest")
				.addHeader("referer", "http://" + serverIp + "/eson/coe/" + ecgi + "/")
				.addHeader("connection", "Keep-Alive").addHeader("host", serverIp)
				.addHeader("cache-control", "no-cache").build();
		try {
			Response response = client.newCall(request).execute();
			responseArray.add(response);
			String jsonData = response.body().string();
			GeneralUtils.printToConsole("Resonse: " + jsonData);
			// return jsonData;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		request = new Request.Builder().url("http://192.168.58.198/eson/coe/" + ecgi + "/action?cmd=refresh").get()
				.addHeader("host", "192.168.58.198").addHeader("connection", "keep-alive").addHeader("accept", "*/*")
				.addHeader("cmd", "refresh").addHeader("cache-control", "no-cache")
				.build();
		try {
			Response response = client.newCall(request).execute();
			responseArray.add(response);
			String jsonData = response.body().string();
			GeneralUtils.printToConsole("Resonse: " + jsonData);
			return jsonData;
		} catch (Exception d) {
			d.printStackTrace();
			return null;
		}

	}
}

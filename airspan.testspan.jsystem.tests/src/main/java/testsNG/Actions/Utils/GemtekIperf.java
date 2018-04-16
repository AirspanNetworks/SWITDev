package testsNG.Actions.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import Utils.GeneralUtils;

public class GemtekIperf {
	protected Integer TIME_TO_RUN = 60;
	protected String templateUrl2 = "/cgi-bin/sysconf.cgi?page=ajax.asp&action=perf_measure_status_toggle&time=1472731404604&_=1472731404605";
	protected String templateUrl1 = "/cgi-bin/sysconf.cgi?page=ajax.asp&action=save_iperf_value&perf_measure_server_ip=55.5.55.55&perf_measure_server_port=5001&perf_measure_cpe_port=5001&perf_measure_test_time="+TIME_TO_RUN+"&perf_measure_protocol_type=0&perf_measure_packet_data_length=1024&perf_measure_bandwidth=2m&perf_measure_client_num=1&time=1472731404604&_=1472731404605";
	protected String url1WithIp;
	protected String url2WithIp;
	protected ArrayList<IperfRequests> ueList = new ArrayList<IperfRequests>();

	/**
	 * getting LAN ip of the UE HTTP PAGE! e.g = 192.19.2.254 adding it to
	 * template request and sends the request
	 * 
	 * @param ipToUE
	 */
	public GemtekIperf(ArrayList<String> ipToUEList) {
		for (String ip : ipToUEList) {
			url1WithIp = "http://" + ip + templateUrl1;
			url2WithIp = "http://" + ip + templateUrl2;
			IperfRequests ue = new IperfRequests(url1WithIp, url2WithIp);
			ueList.add(ue);
		}
	}

	/**
	 * traverse every ue LanIP given in Ctor and requesting for Iperf.
	 * 
	 * @return
	 */
	public void postHttpRequestForAllUES() {
		for (IperfRequests ue : ueList) {
			postHttp(ue.getRequest1());
			postHttp(ue.getRequest2());
		}
	}

	/**
	 * sending an LAN ip to method 
	 * method traverse over the list given in ctor and sending Http for only given IP ue.
	 * @param ip
	 */
	public void postHttpForSingleUE(String ip) {
		for (IperfRequests ueRequests : ueList) {
			if (ueRequests.getRequest1().contains(ip)) {
				postHttp(ueRequests.getRequest1());
				postHttp(ueRequests.getRequest2());
			}
		}
	}

	private boolean postHttp(String url) {
		try {
			URL yahoo = new URL(url);
			HttpURLConnection yc = (HttpURLConnection) yahoo.openConnection();
			yc.setRequestProperty("Content-Language", "en-US");
			GeneralUtils.printToConsole("in executePost...");
			BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));

			if (yc.getResponseCode() != 200) {
				GeneralUtils.printToConsole("Error in http request not 200");
			}

			String inputLine;
			while ((inputLine = in.readLine()) != null)
				GeneralUtils.printToConsole(inputLine);

			in.close();
		} catch (Exception e) {
			GeneralUtils.printToConsole("Exception "+e.getMessage());
			return false;
		}
		return true;
	}

	/**
	 * sets time int type to iperf request
	 * @param iperfTime
	 * @throws IOException 
	 */
	public void setIperfTime(int iperfTime) throws IOException{
		if(iperfTime >= 30 && iperfTime <= 86400){
	this.TIME_TO_RUN = iperfTime;	
		}else{
			throw new IOException("Seconds : value should be between 30 and 86,400");
		}
		
	}
	
	public class IperfRequests {

		private String request1 = new String();
		private String request2 = new String();

		public IperfRequests(String req1, String req2) {
			this.request1 = req1;
			this.request2 = req2;
		}

		public String getRequest1() {
			return request1;
		}

		public void setRequest1(String request1) {
			this.request1 = request1;
		}

		public String getRequest2() {
			return request2;
		}

		public void setRequest2(String request2) {
			this.request2 = request2;
		}

	}
}

package testsNG.Actions.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import testsNG.Actions.Utils.CloseAllIPerfSessions;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DisconnectSession {
	static String machineName = "";
	static String userIdPrefix = " - admin";
	static String lastUpdateDate = "10/04/2018";
	static String version = "1.1.1";
	static String lastChanges = "DisconnectSession Script will close local SUT session/call closeAllIPerfSessions that close IPerf Sessions.";
	static List<String> localMachineNameRelations;
	static final String server1 = "192.168.58.200";
	static final String server2 = "192.168.58.201";
	static final String server3 = "192.168.58.214";
	static ArrayList<String> serversList = new ArrayList<String>();
	static OkHttpClient client = new OkHttpClient();
	static String restIPTag = "<restMachineIP>(.*)</restMachineIP>";

	private static void init() {
		OkHttpClient client = new OkHttpClient.Builder().connectTimeout(500, TimeUnit.SECONDS)
				.readTimeout(500, TimeUnit.SECONDS).writeTimeout(500, TimeUnit.SECONDS).build();
		
		try {
			machineName = InetAddress.getLocalHost().getHostName();
			System.out.println("Machine Name: " + machineName);
			serversList.add(server1);
			serversList.add(server2);
			serversList.add(server3);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("cannot init parameters");
		}
	}

	public static void main(String[] args) {
		scriptIntro();
		init();
		
		final String filePath = "airspan.testspan.jsystem.tests/target/classes/sut/"  + args[0];// Relies that the execution run from the main project folder. 

		System.out.println("path to files "+filePath);
		String serverIP = parseSUT(filePath);
		if (serverIP != null) {
			disconnectSession(serverIP);
		} else {
			System.out.println(
					"trying to find and remove machineName : " + machineName + " from Hard Coded Recovery Values");
			for (String server : serversList) {
				disconnectSession(server);
			}
		}
		
		CloseAllIPerfSessions closeAllIPerfSessions = new CloseAllIPerfSessions();
		closeAllIPerfSessions.closeAllIPerfSessions(filePath);
		
		System.out.println("********** End DisconnectSession Script **********");
	}

	/**
	 * method will include information for the user:
	 * method will provide version, last update date and last changes.
	 */
	private static void scriptIntro() {
		System.out.println("********** Start DisconnectSession Script **********");
		System.out.println("Script info:");
		System.out.println("\t version : " + version + " ");
		System.out.println("\t last Updaged : " + lastUpdateDate + " ");
		System.out.println("\t last Changes : " + lastChanges + " ");
	}

	private static String parseSUT(String file) {
		String restServerIp;
		try {
			restServerIp = getXmlSubTag(file,restIPTag);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return restServerIp;
	}

	private static String getXmlSubTag(String file,String tagName) throws IOException {
		System.out.println("looking for subTag "+tagName+" in file : " + file);
		// open File
		BufferedReader br = new BufferedReader(new FileReader(new File(file)));
		String line;
		String result = null;
		StringBuilder sb = new StringBuilder();

		// record File and Special Segment
		while ((line = br.readLine()) != null) {
			sb.append(line.trim());
		}
		Pattern pattern = Pattern.compile(tagName);
		Matcher matcher = pattern.matcher(sb.toString());
		if (matcher.find()) {
			result = matcher.group(1);
			System.out.println(result);
		}

		return result;
	}

	private static boolean disconnectSession(String serverIp) {
		List<String> sessions = getAllSessions(serverIp);

		if (sessions != null) {
			System.out.println("found Sessions in server : " + serverIp);
			System.out.println(" ");

			localMachineNameRelations = getMachinesWithJenkinsName(sessions, machineName);
			if (!localMachineNameRelations.isEmpty()) {
				return killSession(localMachineNameRelations, serverIp);
			}
			System.out.println("did not Found sessions to kill in server : " + serverIp + " with the sessionID of : "
					+ machineName);
		}
		return false;
	}

	private static boolean killSession(List<String> sessionToKillList, String serverIp) {
		for (String sessionToKill : sessionToKillList) {
			String sessionWithoutSpaces = sessionToKill.replaceAll(" ", "%20");

			Request request = new Request.Builder()
					.url("http://" + serverIp + "/stcapi/sessions/" + sessionWithoutSpaces).delete(null)
					.addHeader("x-stc-api-session", sessionToKill).addHeader("cache-control", "no-cache")
					.addHeader("postman-token", "f01698b0-36ea-2a71-5a53-d6ca6aadca73").build();

			try {
				Response response = client.newCall(request).execute();
				if (response.code() != 204) {
					throw new IOException("error in response code : " + response.code());
				}
				System.out.println("Session : [" + sessionToKill + "] killed Successfully");
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Could not kill Sesssion [" + sessionToKill + "] - since : "+e.getMessage());
			}
		}
		return true;
	}

	private static List<String> getMachinesWithJenkinsName(List<String> sessions, String machineName) {
		ArrayList<String> sessionsToKill = new ArrayList<String>();
		for (String string : sessions) {
			String[] usersSessions = string.split(",");
			for (String str : usersSessions) {
				System.out.println("session name : " + str);
				String cleanStr = str.replaceAll("[^a-zA-Z0-9\\-\\_ ]", "");
				if (checkContainsCompletly(cleanStr, machineName)) {
					sessionsToKill.add(cleanStr);
					System.out.println(cleanStr + " was Added to kill list");
				}
			}
		}
		return sessionsToKill;
	}

	private static boolean checkContainsCompletly(String cleanStr, String jenkinsSlaveName2) {
		System.out.println("Checking session name : "+cleanStr);
		boolean result = false;
		String [] sessionNameUserIdSplit = cleanStr.split("--");
		printStringArray(sessionNameUserIdSplit);
		
		for(String sessionName : sessionNameUserIdSplit){
			String [] sessionNameJenkinsName = sessionName.split(" - ");	
			for(String sessionNameComponent : sessionNameJenkinsName){
				if(sessionNameComponent.equals(jenkinsSlaveName2)){
					result = true;
				}
			}
		}
		return result;
	}
	
	private static void printStringArray(String [] array){
		System.out.println("array split : ");
		for(String compenent : array){
			System.out.print(compenent+" ");
		}
		System.out.println("");
	}

	private static List<String> getAllSessions(String serverIp) {
		Request request = new Request.Builder().url("http://" + serverIp + "/stcapi/sessions/").get()
				.addHeader("cache-control", "no-cache")
				.addHeader("postman-token", "0c958bfa-c789-bbe1-2f52-b2f27a2f0da5").build();
		Response response;
		try {
			response = client.newCall(request).execute();

			if (response != null) {
				BufferedReader rd = new BufferedReader(new InputStreamReader(response.body().byteStream()));
				ArrayList<String> lines = new ArrayList<String>();
				String line;
				while ((line = rd.readLine()) != null) {
					lines.add(line);
				}
				return lines;
			} else {
				System.out.println("response is empty!");
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Exception while method getAllSessions");
		}
		return null;
	}
}
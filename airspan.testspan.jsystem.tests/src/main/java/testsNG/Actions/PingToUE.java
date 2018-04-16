package testsNG.Actions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import UE.UE;
import Utils.GeneralUtils;

public class PingToUE implements Runnable {
	
	private UE ue;
	private int iterator;
	
	public PingToUE(UE ue, int iterator) throws IOException{
		this.ue=ue;
		this.iterator=iterator;
	}
	

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			
			String routeAddUnix="route add -net 5.5.5."+iterator+" netmask 255.255.255.255 gw "+ue.getLanIpAddress()+"\n";
			String routeAddDos="route add 5.5.5."+iterator+" mask 255.255.255.255 "+ue.getLanIpAddress()+"\n";
			
			GeneralUtils.printToConsole("Route add Linux: "+routeAddUnix);
			Process p=Runtime.getRuntime().exec(routeAddUnix);
			p.waitFor();
		    BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
		    while(br.ready())
		       GeneralUtils.printToConsole(br.readLine());
			
		    GeneralUtils.printToConsole("Route add Windows: "+routeAddDos);
			p=Runtime.getRuntime().exec(routeAddDos);
			p.waitFor();
		    br = new BufferedReader(new InputStreamReader(p.getInputStream()));
		    while(br.ready())
		       GeneralUtils.printToConsole(br.readLine());
			
		    
			String ping="ping 5.5.5."+iterator+" -t128\n";
			GeneralUtils.printToConsole("Start Pinging: "+ping);
			Runtime.getRuntime().exec(ping);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void end() throws IOException, InterruptedException{
		String routeDelCommand="route delete 5.5.5."+iterator+"\n";
		GeneralUtils.printToConsole("Route delete: "+routeDelCommand);
		Runtime.getRuntime().exec(routeDelCommand);
	}
}

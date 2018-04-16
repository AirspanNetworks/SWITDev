package Utils;

import java.net.InetAddress;

public class PingUtils {

	public static boolean isReachable(String ipAddress, int ping_retries) {
		double numOfSuccess = 0;
		try{
            InetAddress address = InetAddress.getByName(ipAddress);
            boolean reachable = false;
            for(int i=1;i<=ping_retries;i++){
            	reachable = address.isReachable(1000);
            	if(reachable){
            		numOfSuccess++;
            	}
            }
            return (numOfSuccess/(double)ping_retries > 0.5) ;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
	}
}
package EPC;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import Netspan.Profiles.NetworkParameters.Plmn;
import jsystem.framework.system.SystemObjectImpl;

public class MME extends SystemObjectImpl {
	private String s1IpAdress;
	private ArrayList<Plmn> PLMNs = null;

	public String getS1IpAdress() {
		return s1IpAdress;
	}

	public void setS1IpAdress(String s1IpAdress) {
		this.s1IpAdress = s1IpAdress;
	}
	
	public ArrayList<Plmn> getPLMNs() {
		if(PLMNs == null)
			parsePLMN();
		return PLMNs;
	}

	public void setPLMNs(ArrayList<Plmn> pLMNs) {
		PLMNs = pLMNs;
	}

	private ArrayList<Plmn> parsePLMN() {
		PLMNs = new ArrayList<Plmn>();
		ArrayList<String> blocksArray = new ArrayList<String>();
		String configStr = EPC.getInstance().getEPCConfig();
		Matcher m = Pattern.compile(("(?s)mme-service(.*?)exit")).matcher(configStr);
		//parsing the EPCoutput to blocks that contains the MME's 
		while(m.find()){
		    String match = m.group(1);
		    blocksArray.add(match);
		}
		for(String temp: blocksArray){
			if (temp.contains("ipv4-address " + s1IpAdress)){
				String pattern = "plmn-id mcc (\\d+) mnc (\\d+)";
				m = Pattern.compile(pattern).matcher(temp);
				Plmn plmn;
				while(m.find()){
					plmn = new	Plmn();
				    plmn.setMCC(m.group(1)); 
				    plmn.setMNC(m.group(2));
				    PLMNs.add(plmn);
				}
				break;
			}
		}
		return PLMNs;
	}
	
	public boolean containsPlmn(Plmn UEplmn){
		ArrayList<Plmn> mmePlmns = getPLMNs();
		for(Plmn plmn : mmePlmns){
			if (plmn.equal(UEplmn))
				return true;
		}
		return false;
	}
}

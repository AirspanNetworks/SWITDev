package Attenuators;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Attenuators.Utils.ControlledAttenuator;
import Attenuators.Utils.ERudatAttenuatorCommands;
import Utils.MoxaCom;
import Utils.MoxaComMode;
import jsystem.utils.StringUtils;

public class RudatAttenuator extends ControlledAttenuator {

	public enum RudatType { SERIAL, IP }

	private String IP;
	private static final int RUDAT_DEAFULT_BAUD_RATE = 9600;
	private static final int RUDAT_DEAFULT_DATA_BIT = 8;
	private static final int RUDAT_DEAFULT_STOP_BIT = 1;
	private static final int RUDAT_DEAFULT_PARITY = 0;
	private static final int RUDAT_DEAFULT_MAX_ATTENUATION = 30;
	private static final int COMMAND_TIMEOUT = 1000;
	private RudatType myType = RudatType.SERIAL;
	
	@Override
	public void init() throws Exception {
		super.init();
		
		//Setting the correct mode based on SUT parameters
		if (serialCom == null) {
			
			if (StringUtils.isEmpty(IP))
				throw new Exception("IP Is not configured on Rudat attenuator");
			serialCom = new MoxaCom();
			serialCom.setMode(MoxaComMode.REVERSE_TELNET);
			serialCom.setPort(23);
			serialCom.setComName(IP);
			myType = RudatType.IP;
		} 
		connect();
	}
	
	public void connect() {
		if (serialCom.getMode() == MoxaComMode.COM && serialCom.getBaudRate() == 0) {
			serialCom.setBaudRate(RUDAT_DEAFULT_BAUD_RATE);
			serialCom.setDataBit(RUDAT_DEAFULT_DATA_BIT);
			serialCom.setStopBit(RUDAT_DEAFULT_STOP_BIT);
			serialCom.setParity(RUDAT_DEAFULT_PARITY);
		}
		
		if (maxAttenuation == 0)
			maxAttenuation = RUDAT_DEAFULT_MAX_ATTENUATION;	
		
		serialCom.connect();
	}

	public String getIP() {
		return IP;
	}

	public void setIP(String iP) {
		IP = iP;
	}

	@Override
	public boolean setAttenuation(float attenuation) {
		String command = ERudatAttenuatorCommands.ATTENUATION_SET.getCommand(myType,attenuation);
		boolean status = serialCom.sendString(command, false);
		serialCom.clearBuffer();
		return status;
	}

	@Override
	public float getAttenuation() {
		serialCom.clearBuffer();
		String command = ERudatAttenuatorCommands.ATTENUATION_GET.getCommand(myType);
		this.serialCom.sendString(command, false);
		Pattern pattern = Pattern.compile("\\d+(\\.\\d{1,2})?");
		
			
		long startTime = System.currentTimeMillis(); //fetch starting time
		while(false||(System.currentTimeMillis()-startTime)<(COMMAND_TIMEOUT*10))
		{
			String result = serialCom.getResult(COMMAND_TIMEOUT);
			Matcher matcher = pattern.matcher(result);
			if(matcher.find()){
			//	GeneralUtils.printToConsole(System.currentTimeMillis()-startTime);

				return Float.parseFloat(matcher.group(0));
			}
		}
		
			
		return -999;
	}

}



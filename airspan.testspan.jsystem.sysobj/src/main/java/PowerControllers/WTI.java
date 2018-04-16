package PowerControllers;

import Utils.GeneralUtils;
import jsystem.framework.report.Reporter;
import systemobject.terminal.SSH;


public class WTI extends TerminalPowerController{
	private static final  String  DEFAULT_USERNAME  = "super";
	private static final  String  DEFAULT_PASSWORD  = "super";
	private static final  String  EXEC_TRIGGER      = ">";
	private static final  String  EXEC_TRIGGER2      = "VMR>";
	private static final  String  VERIFY_TRIGGER    = "(Y/N):";
	private static final  String  VERIFY_YES        = "Y";
	private static final  String  POWER_ON_CMD      = "/ON ";
	private static final  String  POWER_OFF_CMD     = "/OFF ";
	private static final String STATUS_CMD			= "/S ";
	private static final String ON_STATUS="1";
	private static final String OFF_Status="0";
	
	@Override
	public void init() throws Exception {
		super.init();
		if (!getIpAddress().isEmpty() ){
			connection = new SSH(getIpAddress(), getUsername(), getPassword());
			connection.connect();
		
		}
	}
	
	
	private boolean executeCommand(String command,PowerControllerPort port) {
		String fullCommand=command+port;
		String buffer;
		GeneralUtils.printToConsole("Executing command: " + fullCommand);
		try {
			sendCommand("\r");
			sendCommand("\r");
			buffer =readBuffer();
			if (buffer.contains(EXEC_TRIGGER) ||  buffer.indexOf(EXEC_TRIGGER2)!=-1) {
				sendCommand(fullCommand);
				buffer =readBuffer();
				if (buffer.contains(VERIFY_TRIGGER))
					return sendCommand(VERIFY_YES);
			}else{
				GeneralUtils.printToConsole( "Can not find '"+EXEC_TRIGGER+"' in buffer" );
			}
		
			// Temporary solution, because older versions of WTI return nothing to this command.
		    buffer = readBuffer();
			if (buffer.isEmpty())
				return true;
			

			GeneralUtils.printToConsole(String.format("Failed to execute command \"%s\".\nbuffer:\n%s\n", fullCommand, buffer));
			
		} catch (Exception e) {
			GeneralUtils.printToConsole( e.toString() );
			e.printStackTrace();
		}
		
		
		return false;
		
	}
	
	@Override
	public boolean powerOnPort(PowerControllerPort port) {	
		for(int tryout=1;tryout<=3;tryout++){
					if (executeCommand(POWER_ON_CMD , port)){
						report.report(String.format("Port %s power on success.",port));
						return true;
					}
					else{
						//Verify port status
						try{
							String verifyCommand=STATUS_CMD+port;
							GeneralUtils.printToConsole("Executing verify command: " + verifyCommand);
							if(sendCommand(verifyCommand)){
								if(readBuffer().contains(ON_STATUS)){
								report.report(String.format("Port %s power on sucess but no VERIFY STRING WAS DETECTED, CHECK YOUR WTI!"), Reporter.WARNING);	
								return false;
								}
							
							}
							report.report(String.format("Failed to power on port %s , tryout #%s", port,tryout), Reporter.WARNING);
						}
						catch(Exception ex){
							GeneralUtils.printToConsole( ex.toString() );
							ex.printStackTrace();
						}
						
					}
		}
		report.report(String.format("Failed to power on port %s", port), Reporter.WARNING);
		return false;
	}

	@Override
	public boolean powerOffPort(PowerControllerPort port) {
		for(int tryout=1;tryout<=3;tryout++){
			if(executeCommand(POWER_OFF_CMD ,port)){
				report.report(String.format("Port %s power off success.\n", port));
				return true;
			}
			else{
				//Verify port status
				try{
				String verifyCommand=STATUS_CMD+port;
				GeneralUtils.printToConsole("Executing verify command: " + verifyCommand);
				if(sendCommand(verifyCommand)){
					if(readBuffer().contains(OFF_Status)){
						report.report(String.format("Port %s power off sucess but no VERIFY STRING WAS DETECTED, CHECK YOUR WTI!"), Reporter.WARNING);
						return false;
					}
				}
				report.report(String.format("Failed to power off port %s , tryout #%s", port,tryout), Reporter.WARNING);
				}
				catch(Exception ex){
					GeneralUtils.printToConsole( ex.toString() );
					ex.printStackTrace();
				}
			}
		}
		report.report(String.format("Failed to power on port %s", port), Reporter.WARNING);
		return false;
	}
	
	@Override
	public String getUsername() {
		if (super.getUsername() == null)
			setUsername(DEFAULT_USERNAME);
		return super.getUsername();
	}
	
	@Override
	public String getPassword() {
		if (super.getPassword() == null)
			setPassword(DEFAULT_PASSWORD);
		return super.getPassword();
	}

}

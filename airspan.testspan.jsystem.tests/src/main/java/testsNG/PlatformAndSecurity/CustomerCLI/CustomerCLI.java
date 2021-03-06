package testsNG.PlatformAndSecurity.CustomerCLI;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import Utils.PasswordUtils;
import org.junit.Test;

import EnodeB.AirHarmony1000;
import EnodeB.EnodeB;
import Utils.GeneralUtils;
import Utils.SysObjUtils;
import jsystem.framework.ParameterProperties;
import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;
import systemobject.terminal.SSH;
import systemobject.terminal.Terminal;
import testsNG.TestspanTest;

public class CustomerCLI extends TestspanTest {
	private EnodeB dut;
	private Terminal ssh;
	private List<String> testExpressions;
	private final String promptSh= "sh-4.4";

	/**
	 * Tests
	 */

	@Test
	@TestProperties(name = "cli Show Banks", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {"IsTestWasSuccessful" })
	public void cli_Show_Banks(){
		report.report("checking CLI with show banks command");
		String response = sendAndHandleCommand("show banks\n");

		report.report("adding test filters");
		testExpressions.add("SW version in bank 0");
		testExpressions.add("SW version in bank 1");
		report.report(testExpressions.toString());

		if(checkResponseWithTestFilter(response,testExpressions)) {
			report.report("all expressions are in data!");
		}else {
			report.report("not all expressions are in data!",Reporter.FAIL);
		}
	}

	@Test
	@TestProperties(name = "cli ifConfig", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {"IsTestWasSuccessful" })
	public void cli_ifConfig(){
		report.report("checking CLI with show ifconfig command");
		String response = sendAndHandleCommand("show ifconfig\n");

		report.report("adding test filters");
		testExpressions.add("br0");
		testExpressions.add("eth3");
		testExpressions.add("eth0");
		report.report(testExpressions.toString());

		if(checkResponseWithTestFilterAtLeastOne(response,testExpressions)) {
			report.report("At least one expression is in data");
		}else {
			report.report("None of the expressions are in data!",Reporter.FAIL);
		}
	}

	@Test
	@TestProperties(name = "cli help", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {"IsTestWasSuccessful" })
	public void cli_help(){
		report.report("checking CLI with help command");
		String response = sendAndHandleCommand("help\n");

		report.report("adding test filters");
		testExpressions.add("-show banks");
		testExpressions.add("-show ifconfig");
		report.report(testExpressions.toString());

		if(checkResponseWithTestFilter(response,testExpressions)) {
			report.report("all expressions are in data!");
		}else {
			report.report("not all expressions are in data!",Reporter.FAIL);
		}
	}

	@Test
	@TestProperties(name = "multiple CLI access", returnParam = { "IsTestWasSuccessful" }, paramsExclude = {"IsTestWasSuccessful" })
	public void multi_attempts(){
		report.report("Checking CLI with help command");
		if(multipleAccessToCli()) {
			report.report("All CLI attempts passed");
		}else {
			report.report("Some attempts did not pass",Reporter.FAIL);
		}

	}


	private boolean multipleAccessToCli() {
		boolean result = true;
		for(int i=1; i<11; i++) {
			GeneralUtils.startLevel("attempt "+i+" to check CLI start menu");
			if(!connectAndSendSingleCommand("show banks\n")){
				report.report("try "+i+" didn't had CLI prefix!");
				result = false;
			}
			GeneralUtils.stopLevel();

		}
		return result;
	}

	private boolean connectAndSendSingleCommand(String string) {
		String buffer = "";
		try {
			ssh.connect();
			boolean isAH1000 = dut instanceof AirHarmony1000;
			if(isAH1000){
				buffer = sendCommand("\n");
				GeneralUtils.unSafeSleep(500);
				GeneralUtils.printToConsole("Result of new line command:"+buffer);
				if(buffer.contains(promptSh)){
					GeneralUtils.printToConsole("Sending lteCli command");
					buffer = sendCommand("/bs/lteCli\n");
					GeneralUtils.printToConsole("Result of lteCli command: ");
					GeneralUtils.printToConsole(buffer);
					GeneralUtils.unSafeSleep(500);
				}else{
					report.report("EnodeB is "+dut.getClass()+" but does not contain "+promptSh+" prompt",Reporter.WARNING);
					ssh.disconnect();
					reportMultiLineMessage(buffer);
					return false;
				}
			}
			if(!isAH1000){
				buffer = sendCommand("show banks\n");				
			}
			ssh.disconnect();
			report.report("checking if ' # Welcome to Airspan CLI # ' is in CLI.");
			reportMultiLineMessage(buffer);
			return buffer.contains("# Welcome to Airspan CLI #");
		}catch(Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * class methods
	 */

	/**
	 * method will return true only if every string is in the data required
	 * @param data
	 * @param singleTestExpressions
	 * @return
	 */
	private boolean checkResponseWithTestFilter(String data,List<String> singleTestExpressions) {
		boolean result = true;
		report.report("checking if all expressions are in data as required");
		for(String mustHave : singleTestExpressions) {
			if(!data.contains(mustHave)) {
				report.report("expression : '"+mustHave+"' is not in data!");
				result = false;
			}
		}
		return result;
	}
	
	private boolean checkResponseWithTestFilterAtLeastOne(String data,List<String> singleTestExpressions) {
		boolean result = false;
		report.report("checking if at least one expression is in data as required");
		for(String mustHave : singleTestExpressions) {
			if(!data.contains(mustHave)) {
				report.report("expression : '"+mustHave+"' is not in data!",Reporter.WARNING);
			}else{
				report.report("expression : '"+mustHave+"' is in data!");
				result = true;
			}
		}
		return result;
	}

	private String sendAndHandleCommand(String command) {
		String result ="";
		try {
			ssh.connect();
			boolean isAH1000 = dut instanceof AirHarmony1000;
			if(isAH1000){
				result = sendCommand("\n");
				GeneralUtils.unSafeSleep(500);
				GeneralUtils.printToConsole("Result of new line command:"+result);
				if(result.contains(promptSh)){
					GeneralUtils.printToConsole("Sending lteCli command");
					result = sendCommand("/bs/lteCli\n");
					GeneralUtils.printToConsole("Result of lteCli command: ");
					GeneralUtils.printToConsole(result);
					GeneralUtils.unSafeSleep(500);
				}else{
					report.report("EnodeB is "+dut.getClass()+" but does not contain "+promptSh+" prompt",Reporter.WARNING);
					reportMultiLineMessage(result);
					return "";
				}
			}
			result = sendCommand(command);
			if(!isAH1000){
				result = cutResponseWithFilter(result,">>");				
			}
			int len = result.length();
			String debugResult = "";

			if(len>124){
				debugResult = result.substring(0,123);
				GeneralUtils.startLevel("response: "+debugResult+".....");
			}else{
				debugResult = result;
				GeneralUtils.startLevel("response: "+debugResult);
			}

			reportMultiLineMessage(result);
			GeneralUtils.stopLevel();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	private String sendCommand(String command) {
		String result="";
		try {
		ssh.sendString(command, true);
		GeneralUtils.unSafeSleep(1*1000);
		result = ssh.readInputBuffer();
		}catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	private void reportMultiLineMessage(String result) {
		GeneralUtils.reportHtmlLink("Message outcome", result);
	}

	private String cutResponseWithFilter(String data,String filter) {
		int index = data.indexOf(">>");
		if(index == -1) {
			report.report("error filtering data from CLI according to '>>' ");
			return data;
		}
		return data.substring(index,data.length()-1);
	}

	@Override
	public void init() throws Exception {
		//todo paz - verify it's for XLPs
		enbInTest = new ArrayList<EnodeB>();
		enbInTest.add(dut);
		testExpressions = new ArrayList<>();
		ssh = new SSH(dut.getIpAddress(),
				PasswordUtils.COSTUMER_USERNAME,
				dut.getXLP().getMatchingPassword(PasswordUtils.COSTUMER_USERNAME));
		super.init();
	}

	@Override
	public void end() {
		testExpressions.clear();
		try {
			if(ssh.isConnected()) {
				ssh.disconnect();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		super.end();
	}

	@ParameterProperties(description = "Name of Enb")
	public void setDUT(String dut) {
		ArrayList<EnodeB> temp=(ArrayList<EnodeB>)SysObjUtils.getInstnce().initSystemObject(EnodeB.class,false,dut);
		this.dut = temp.get(0);
	}


}

package Action.EnodebAction;

import java.util.ArrayList;
import java.util.HashMap;
import org.junit.Test;
import EnodeB.EnodeB;
import Utils.GeneralUtils;
import Utils.SysObjUtils;
import jsystem.framework.ParameterProperties;
import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;
import jsystem.framework.scenario.Parameter;

public class Statuses extends EnodebAction{

	private InterfaceType interfaceType;
	private ValueType valueType;
	private String stringEexpectedValue;
	private boolean booleanExpectedValue;
	private EnumExpectedValue enumExpectedValue;
	private EnodeB dut1;
	private Symbol symbol = Symbol.EQUALS;

	public enum Symbol{
		EQUALS , DIFFRENT
	}
	public enum EnumExpectedValue {
		ACTIVE, 
		UNACTIVE
	}
	
	public enum InterfaceType {
		MANAGMENT(1), S1_C(2), S1_U(3), X2_C(4), X2_U(5), PTP(6), C_SON(7), M2(8), M1(9), CALL_TRACE_SERVER(10);
		
		private int value;
		private InterfaceType(int value){
			this.value = value;
		}
		public int getValue(){
			return value;
		}
	}
	
	public enum ValueType {
		IP_ADDRESS(4), SUBNET_MASK(5), VLAN_ENABLED(6), VLAN_ID(7),DEFAULT_GETEWAY(10), VALIDATION_STATUS(13);
		
		private int value;
		private ValueType(int value){
			this.value = value;
		}
		public int getValue(){
			return value;
		}
	}
	
	@ParameterProperties(description = "Set interfaceType")
	public void setInterfaceType(InterfaceType interfaceType) {
		this.interfaceType = interfaceType;
	}
	
	@ParameterProperties(description = "Set valueType")
	public void setValueType(ValueType valueType) {
		this.valueType = valueType;
	}
	
	@ParameterProperties(description = "Set stringEexpectedValue")
	public void setStringEexpectedValue(String stringEexpectedValue) {
		this.stringEexpectedValue = stringEexpectedValue;
	}

	@ParameterProperties(description = "Set booleanExpectedValue")
	public void setBooleanExpectedValue(boolean booleanExpectedValue) {
		this.booleanExpectedValue = booleanExpectedValue;
	}

	@ParameterProperties(description = "Set enumExpectedValue")
	public void setEnumExpectedValue(EnumExpectedValue enumExpectedValue) {
		this.enumExpectedValue = enumExpectedValue;
	}
	
	@ParameterProperties(description = "set enumsymbol")
	public void setSymbol(Symbol symbol) {
		this.symbol = symbol;
	}
	

	@ParameterProperties(description = "Name of ENB from the SUT")
	public void setDut(String dut) {
		ArrayList<EnodeB> temp = (ArrayList<EnodeB>) SysObjUtils.getInstnce().initSystemObject(EnodeB.class, false,
				dut.split(","));
		this.dut1 = temp.get(0);
	}
	
	
	@Override
	public void handleUIEvent(HashMap<String, Parameter> map, String methodName) throws Exception {
		map.get("StringEexpectedValue").setVisible(false);
		map.get("BooleanExpectedValue").setVisible(false);
		map.get("EnumExpectedValue").setVisible(false);
		switch (ValueType.valueOf(map.get("ValueType").getValue().toString())) {
		case VLAN_ENABLED:
			map.get("BooleanExpectedValue").setVisible(true);
			break;
		case VALIDATION_STATUS:
			map.get("EnumExpectedValue").setVisible(true);
			break;
		default:
			map.get("StringEexpectedValue").setVisible(true);
			break;
		}
	}
	
	@Test // 1
	@TestProperties(name = "comparelogicalIPInterfaceStatus", returnParam = "IsTestWasSuccessful", paramsInclude = { "InterfaceType", "ValueType", "StringEexpectedValue", "booleanExpectedValue", "EnumExpectedValue", "symbol", "dut"})
	public void comparelogicalIPInterfaceStatus() {
		switch (valueType) {
		case VLAN_ENABLED:
			compareBoolean();
			break;
		case VALIDATION_STATUS:
			compareEnum();
			break;
		default:
			compareString();
			break;
		}
	}

	
	private void convertToHexIP(){
		if (stringEexpectedValue != null){
			String[] IPValues= stringEexpectedValue.split("\\.");
			String[] newIPValues = new String[IPValues.length];
			stringEexpectedValue = "";
			for(int i = 0; i<IPValues.length; i++){
				String temp = GeneralUtils.decimal2hex(Integer.parseInt(IPValues[i]));
				newIPValues[i] = String.format("%0"+ (3 - temp.length() ) + "d%s",0 , temp).substring(1,3);;
			}
			for (int j =0; j<newIPValues.length ; j++) {
				stringEexpectedValue = stringEexpectedValue +":" + newIPValues[j];
			}
			stringEexpectedValue = stringEexpectedValue.substring(1).toLowerCase();
		}
		else 
			stringEexpectedValue = "00:00:00:00";
	}
	private boolean compareBoolean(){
		String value = dut1.getInterfaceStatusByIndex(valueType.getValue(), interfaceType.getValue());
		if (valueType == ValueType.IP_ADDRESS || valueType == ValueType.DEFAULT_GETEWAY)
			convertToHexIP();
		boolean boolValue;
		if (value.equals("1"))
			boolValue = true;
		else
			boolValue = false;
		if (symbol == Symbol.EQUALS){
			if (boolValue == booleanExpectedValue){
				report.report("the value is : " + value + " as expected");
				return true;
			}
			else{
				report.report("the expected value: " + stringEexpectedValue + " does not equals to the value from enodeB (" + value + ")", Reporter.FAIL);
				return false;
			}
				
		}
		else {
			if (boolValue == booleanExpectedValue){
				report.report("the expected value: " + stringEexpectedValue + " equals to the value from enodeB (" + value + ")", Reporter.FAIL);
				return false;
			}
				
			else{
				report.report("the value is : " + value + " as expected");
				return true;
			}
				
		}
	}
	
	private boolean compareString(){
		String value = dut1.getInterfaceStatusByIndex(valueType.getValue(), interfaceType.getValue());
		if (valueType == ValueType.IP_ADDRESS || valueType == ValueType.DEFAULT_GETEWAY)
			convertToHexIP();
		if (symbol == Symbol.EQUALS){

			if (value.equals(stringEexpectedValue)){
				report.report("the value is : " + value + " as expected");
				return true;
			}
			else{
				report.report("the expected value: " + stringEexpectedValue + " does not equals to the value from enodeB (" + value + ")", Reporter.FAIL);
				return false;
			}	
		}
		else {
			if (value.equals(stringEexpectedValue)){
				report.report("the expected value: " + stringEexpectedValue + " equals to the value from enodeB (" + value + ")", Reporter.FAIL);
				return false;
			}		
			else{
				report.report("the value is : " + value + " as expected");
				return true;
			}
		}
	}
	
	private boolean compareEnum(){
		String value = dut1.getInterfaceStatusByIndex(valueType.getValue(),interfaceType.getValue());
		if (valueType == ValueType.IP_ADDRESS || valueType == ValueType.DEFAULT_GETEWAY)
			convertToHexIP();
		EnumExpectedValue enumValue;
		if (value.equals("1"))
			enumValue = EnumExpectedValue.ACTIVE;
		else 
			enumValue = EnumExpectedValue.UNACTIVE;
		if (symbol == Symbol.EQUALS){
			if (enumValue == enumExpectedValue.ACTIVE){
				report.report("the value is : " + value + " as expected");
				return true;
			}
				
			else{
				report.report("the expected value: " + stringEexpectedValue + " does not equals to the value from enodeB (" + value + ")", Reporter.FAIL);
				return false;
			}
				
		}
		else {
			if (enumValue == enumExpectedValue.ACTIVE){
				report.report("the expected value: " + stringEexpectedValue + " equals to the value from enodeB (" + value + ")", Reporter.FAIL);
				return false;
			}
				
			else{
				report.report("the value is : " + value + " as expected");
				return true;
			}
				
		}
	}
}

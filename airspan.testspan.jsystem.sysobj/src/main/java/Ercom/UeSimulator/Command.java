package Ercom.UeSimulator;

public class Command {

	private String methodName = "";
	private String address = "";
	public Command(String methodName , String address){
		this.methodName = methodName;
		this.address = address;
	}
	public String getMethodName() {
		return methodName;
	}
	public String getAddress() {
		return address;
	}
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
	public void setAddress(String address) {
		this.address = address;
	}
}

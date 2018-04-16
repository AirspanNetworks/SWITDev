package testsNG.Actions.Utils;

public enum recoveryStates{
	streamFirst(1),streamSecond(2),streamThird(3);
	
	private int value; 
	
	recoveryStates(int name) {
		this.value = name;
	}
}

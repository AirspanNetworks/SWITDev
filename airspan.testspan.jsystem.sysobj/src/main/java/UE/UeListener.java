package UE;

public interface UeListener {
	public void UeStateChanged(UeEvent event, UeState prevState, UeState currentState);
}

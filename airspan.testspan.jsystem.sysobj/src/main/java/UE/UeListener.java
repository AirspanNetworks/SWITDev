package UE;

public interface UeListener {
	void UeStateChanged(UeEvent event, UeState prevState, UeState currentState);
}

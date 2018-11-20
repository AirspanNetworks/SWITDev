package UE;

public enum UeState {
	disconnected(false), connected(true), idle(false), unknown(true);
	
	// is the state is a wanted state or an unwanted state.
	private boolean goodState;
	
	/**
	 * 
	 * @param level - negative level for unwanted states & positive level for wanted states.
	 */
	UeState(boolean goodState) {
		this.goodState = goodState;
	}
	
	public boolean isGoodState() {
		return goodState;
	}
}

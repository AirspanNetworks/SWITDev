package Utils.WatchDog;

public abstract class Command implements Runnable {	
	public String name = "";
	public abstract int getExecutionDelaySec();
}
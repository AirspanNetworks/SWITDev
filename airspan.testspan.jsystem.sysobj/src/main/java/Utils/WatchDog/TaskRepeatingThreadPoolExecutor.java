package Utils.WatchDog;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TaskRepeatingThreadPoolExecutor extends ThreadPoolExecutor {
	private WatchDogManager WD = null;
	
    public TaskRepeatingThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }
    
    protected void beforeExecute(Thread t, Runnable r) {    	
    	WD = WatchDogManager.getInstance();
    	Command command = ((Command)r);
    	ManagedCommand managedCommand = WD.getManagedCommand(command);
    	if (managedCommand != null) {
    		if (managedCommand.removed) {
    			System.out.println("Running a removed command.");
			}
    		managedCommand.currentStartTime = System.currentTimeMillis();
			managedCommand.delayToRun=managedCommand.command.getExecutionDelaySec() * 1000;
			managedCommand.totalRuns++;
		}
    }
 
    protected void afterExecute(Runnable r, Throwable t) {
        WD = WatchDogManager.getInstance();
    	Command command = ((Command)r);
    	ManagedCommand managedCommand = WD.getManagedCommand(command);
    	if (managedCommand != null) {
    		if (managedCommand.removed) {
    			System.out.println("finished running a removed command.");
			}
    		managedCommand.runTimeTotal+= (System.currentTimeMillis()- managedCommand.currentStartTime);
		}
    }
}
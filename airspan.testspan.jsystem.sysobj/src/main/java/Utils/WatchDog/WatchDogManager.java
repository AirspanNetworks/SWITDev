package Utils.WatchDog;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import Utils.GeneralUtils;

public class WatchDogManager extends Thread {

	public static final int infinityNumberOfTimesToRun = -1;
	public static final long MAX_THREADS = 2;

	private static WatchDogManager instance;
	private static Object instanceLock = new Object();
	private TaskRepeatingThreadPoolExecutor trtpe;
	private List<ManagedCommand> actions = new ArrayList<ManagedCommand>();
	private int SleepTimeMs = 1000;
	private int numOfThreads = 1;
	private long lastCheckTime;
	private volatile Boolean run = false;
	private volatile boolean suspended = false;

	private WatchDogManager() {
		trtpe = new TaskRepeatingThreadPoolExecutor(0, numOfThreads, 1, TimeUnit.MINUTES,
				new ArrayBlockingQueue<Runnable>(100));
	}

	public static WatchDogManager getInstance() {
		synchronized (instanceLock) {
			if (instance == null) {
				instance = new WatchDogManager();
			}
			else {
				State threadState = instance.getState();
				if (threadState == State.TERMINATED) {
					instance = new WatchDogManager();
				}
			}
		}
		return instance;
	}

	public synchronized void shutDown() {
		run = false;
		suspended = false;
		notify();
	}

	public synchronized void pause() {
		suspended = true;
	}

	public synchronized void proceed() {
		suspended = false;
		notify();
	}

	public Boolean removeCommand(Command command) {
		if (command == null) {
			GeneralUtils.printToConsole("You tried to remove a null command, dont do that...");
			return false;
		}
		GeneralUtils.printToConsole("WD - trying to remove command " + command.name);
		synchronized (actions) {
			ManagedCommand toRemove = null;
			for (ManagedCommand managedCommand : actions) {
				if (managedCommand.command.equals(command)) {
					toRemove = managedCommand;
					break;
				}
			}

			if (toRemove == null) {
				return false;
			} else {
				final Command commandToRemove = toRemove.command;
				trtpe.getQueue().removeIf(cmd -> cmd.equals(commandToRemove));
				toRemove.removed = true;
				return true;
			}
		}
	}
	
	public void removeEndedCommands() {
		synchronized (actions) {
			for (ManagedCommand managedCommand : actions) {
				if (managedCommand.maxTotalRuns != infinityNumberOfTimesToRun && managedCommand.totalRuns >= managedCommand.maxTotalRuns) {
					managedCommand.removed = true;
				}
			}
		}
	}

	public void addCommand(Command command) {
		addCommand(command, infinityNumberOfTimesToRun);
	}
	
	public void addCommand(Command command, int numberOfTimesToRun) {
		synchronized (actions) {
			actions.add(new ManagedCommand(command, numberOfTimesToRun));
			if (actions.size() == 1 && !run) {
				this.start();
			}
		}
	}

	@Override
	public void run() {
		run = true;
		lastCheckTime = System.currentTimeMillis();
		List<ManagedCommand> currentActions;
		while (run) {
			currentActions = getCurrectActionList();
			lastCheckTime = System.currentTimeMillis();
			if (numOfThreads < MAX_THREADS && trtpe.getQueue().size() > 0 && currentActions.size() > 0) {
				numOfThreads++;
				GeneralUtils.printToConsole("Queue overloaded! increasing threads number to " + numOfThreads);
				trtpe.shutdown();
				try {
					trtpe.awaitTermination(1, TimeUnit.MINUTES);
				} catch (InterruptedException e) {
					GeneralUtils.printToConsole("failed shutting down tasks gracefully");
					trtpe.shutdownNow();
				}
				trtpe = new TaskRepeatingThreadPoolExecutor(0, numOfThreads, 1, TimeUnit.MINUTES,
						new ArrayBlockingQueue<Runnable>(100));
			}
			for (ManagedCommand managedCommand : currentActions) {
				trtpe.execute(managedCommand.command);
			}
			long diff = System.currentTimeMillis() - lastCheckTime;
			if (diff < SleepTimeMs) {
				GeneralUtils.unSafeSleep(SleepTimeMs - diff);
			}
			synchronized (this) {
				while (suspended)
					try {
						wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
			}
		}
		trtpe.shutdown();
		try {
			GeneralUtils.printToConsole("Waiting for all watch dog jobs to shutdown 15 seconds.");
			trtpe.awaitTermination(15, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			GeneralUtils.printToConsole("failed shutting down tasks gracefully");
			trtpe.shutdownNow();
		}
		showStatistics();
		GeneralUtils.printToConsole("Ending watchdog");
	}

	private void showStatistics() {
		double totalTimeEff = 0;
		for (ManagedCommand managedCommand : actions) {
			if(managedCommand.totalRuns <= 0) {
				continue;
			}
			GeneralUtils.printToConsole(managedCommand.name + " statistics:");
			GeneralUtils.printToConsole("Total run time " + managedCommand.runTimeTotal + " msec");
			GeneralUtils.printToConsole("Total runs " + managedCommand.totalRuns);
			GeneralUtils.printToConsole("Total delay time " + managedCommand.command.getExecutionDelaySec() + " sec");
			double timeEff = (managedCommand.runTimeTotal / managedCommand.totalRuns)
					/ managedCommand.command.getExecutionDelaySec() / 1000.0 * 100;
			totalTimeEff += timeEff;
			GeneralUtils.printToConsole("time line efficiency used " + new DecimalFormat("##.##").format(timeEff) + "%");
		}
		totalTimeEff = totalTimeEff / numOfThreads;
		GeneralUtils.printToConsole("Total time efficiency " + new DecimalFormat("##.##").format(totalTimeEff) + ", using "
				+ numOfThreads + " threads.");
		if (totalTimeEff > 80) {
			GeneralUtils.printToConsole("WARNING, time efficiency is over 80%");
		}

	}

	private List<ManagedCommand> getCurrectActionList() {
		removeEndedCommands();
		List<ManagedCommand> currentActions = new ArrayList<ManagedCommand>();
		long diff = System.currentTimeMillis() - lastCheckTime;
		synchronized (actions) {
			for (ManagedCommand managedCommand : actions) {
				if (managedCommand.removed ) {
					continue;
				}
				if (diff >= managedCommand.delayToRun) {
					currentActions.add(managedCommand);
				} else {
					managedCommand.delayToRun -= diff;
				}
			}
		}
		return currentActions;
	}

	public ManagedCommand getManagedCommand(Command command) {
		for (ManagedCommand managedCommand : actions) {
			if (managedCommand.command.equals(command)) {
				return managedCommand;
			}
		}
		GeneralUtils.printToConsole("getManagedCommand - Impossible!!");
		return null;
	}

}

class ManagedCommand {
	public boolean removed = false;
	public long delayToRun;
	public long currentStartTime;
	public String name;
	public int runTimeTotal;
	public int totalRuns;
	public Command command;
	public final int maxTotalRuns;

	public ManagedCommand(Command command) {
		this(command, WatchDogManager.infinityNumberOfTimesToRun);//-1 for infinity
	}
	public ManagedCommand(Command command, int numberOfTimesToRun) {
		this.delayToRun = command.getExecutionDelaySec() * 1000;
		this.runTimeTotal = 0;
		this.totalRuns = 0;
		this.maxTotalRuns = numberOfTimesToRun;
		if (command.name.equals("")) {
			this.name = command.getClass().getName();
		} else {
			this.name = command.name;
		}
		this.command = command;
	}

}

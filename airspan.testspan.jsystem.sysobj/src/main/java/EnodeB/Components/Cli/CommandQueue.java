package EnodeB.Components.Cli;

import java.util.ArrayList;
import java.util.Arrays;

public class CommandQueue {
	private ArrayList<CommandQueueItem> items;
	private String name;
	
	public CommandQueue(String name) {
		this.items = new ArrayList<CommandQueueItem>();
		this.setName(name);
	}
	
	public void push(String[] commands) {
		System.out.printf("[%s]: Adding \"%s\" to queue\n", getName(), commands[commands.length - 1]);
		CommandQueueItem item = new CommandQueueItem(commands);
		items.add(item);		
	}
	
	public String[] top() {
		if (isEmpty()) return null;
		
		System.out.printf("[%s]: Top of queue is \"%s\"\n", getName(), Arrays.toString(items.get(0).getCommands()));
		return items.get(0).getCommands();
	}
	
	public String[] pop() {
		if (isEmpty()) return null;
		
		String[] commands = items.get(0).getCommands();
		System.out.printf("[%s]: Removing \"%s\" from queue\n", getName(), Arrays.toString(items.get(0).getCommands()));
		items.remove(0);
		return commands;
	}
	
	public boolean isEmpty() {
		return items.size() == 0;
	}

	public void clear() {
		System.out.printf("[%s]: Clearing queue\n", getName());
		items.clear();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}

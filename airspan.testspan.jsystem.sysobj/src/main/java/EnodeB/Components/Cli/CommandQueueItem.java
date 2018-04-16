package EnodeB.Components.Cli;

public class CommandQueueItem {
	private String[] commands;
	
	public CommandQueueItem(String[] commands) {
		this.setCommands(commands);
	}

	public String[] getCommands() {
		return commands;
	}

	public void setCommands(String[] commands) {
		this.commands = commands;
	}
}

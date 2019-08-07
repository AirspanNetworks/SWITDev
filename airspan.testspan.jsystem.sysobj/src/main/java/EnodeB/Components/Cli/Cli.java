package EnodeB.Components.Cli;

import java.util.logging.Level;
import java.util.logging.Logger;

import EnodeB.Components.Session.Session;
import Utils.GeneralUtils;

public class Cli {
	
	private static final long QUEUE_WAIT_TIME = 150;
	private Logger logger;
	private Session session;
	private CliPrompt currentPrompt;
	private String name;
	
	public Cli(Session session) {
		 this.setName(session.toString() + ".Cli");
		 this.session = session;
		 this.logger = Logger.getLogger(getName());
	}
	
	public void addPrompt(String prompt, String parentPrompt, String[] startModeCommands, String quitCommand) {
		if (currentPrompt == null) {
			setPrompt(parentPrompt);
		}
		
		if (currentPrompt.findPrompt(prompt) != null)
			return;
		
		CliPrompt parent = currentPrompt.findPrompt(parentPrompt);
		parent.addNeighboringPrompt(prompt, startModeCommands, new String[] {quitCommand});
	}
	
	public void setPrompt(String prompt) {
		currentPrompt = new CliPrompt(prompt);
		currentPrompt.setCli(this);
	}
	
	public String sendCommands(String prompt, String command, String response, int responseTimeout) throws NullPointerException {
		if (currentPrompt.findPrompt(prompt) == null) 
			throw new NullPointerException(String.format("[%s]: Prompt \"%s\" not found!", name, prompt));

		GeneralUtils.unSafeSleep(QUEUE_WAIT_TIME);
		
		String result = "";
		boolean error = false;
		try {
			result = currentPrompt.execute(prompt, command, response, responseTimeout);
		} catch (Exception e) {
			e.printStackTrace();
			error = true;
			logger.log(Level.SEVERE, "Couldn't exeute commands, caught exception: "+e.getMessage());
			try {
				result = currentPrompt.getBuffer();
			} catch (Exception e1) {}
		}
		
		if (error)
			result = "";
		return result;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public Session getSession() {
		return session;
	}

	public void setCurrentPrompt(CliPrompt currentPrompt) {
		this.currentPrompt = currentPrompt;
	}
}

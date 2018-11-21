package PowerControllers;

import Utils.GeneralUtils;
import systemobject.terminal.Terminal;

public abstract class TerminalPowerController extends PowerController {

	private static final long BUFFER_WAIT_TIME = 1000;

	protected Terminal connection;

	protected boolean sendCommand(String command) {
		return sendCommand(command, true);
	}

	protected boolean sendCommand(String command, boolean retry) {
		try {
			connection.sendString(command + "\r", true);
		}
		catch (Exception e) {
			GeneralUtils.printToConsole("Error sending command to terminal.Exception:" +
					e.getMessage());
			if (retry) {
				try {
					GeneralUtils.printToConsole("Send action failed due to :: " + e.getMessage());
					GeneralUtils.printToConsole("Retring one more time!");
					connection.disconnect();
					GeneralUtils.printToConsole("Disconnecting ...");
					connection.connect();
					GeneralUtils.printToConsole("Establishing new connection...");
					sendCommand(command, false);
					GeneralUtils.printToConsole("Sending command ...");
					return true;
				}
				catch (Exception e2) {
					GeneralUtils.printToConsole("Error sending command to terminal.Exception:" +
							e2.getMessage());
					GeneralUtils.printToConsole("Failed to send command second time, Unable to complete action.");
					return false;
				}
			}
		}

		return true;
	}

	protected String readBuffer() throws Exception {
		Thread.sleep(BUFFER_WAIT_TIME);
		return connection.readInputBuffer();
	}

}

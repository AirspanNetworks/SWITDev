package PowerControllers;

import java.io.IOException;

import Utils.GeneralUtils;
import systemobject.terminal.Telnet;

public class Aviosys extends TerminalPowerController {
	private static final int TELNET_PORT = 23;

	private static final String NEW_LINE_CHAR = "\n";

	private static final String DEFAULT_USERNAME = "admin";

	private static final String DEFAULT_PASSWORD = "12345678";

	private static final String LOGIN_OK = "Username and password is ok!";

	private static final String SET_PORT_OK = "Set Power Done!";

	private static final String SET_PORT_CMD = "SetPort=";

	private static final char SET_PORT_STATUS_ON = '1';

	private static final char SET_PORT_STATUS_OFF = '0';

	private static final String GET_PORT_CMD = "GetPort\n";

	private static final String GET_PORT_DELIMITER = "=";

	private static final String GET_PORT_STATUS_ON = "On";

	private static final String PORT_NAME_PREFIX = "Power";// prefix in <port>
															// name on SUT

	private static final int GET_PORT_STATUS_INDEX = 1;

	@Override
	public void init() throws Exception {
		super.init();

		connection = new Telnet(getIpAddress(), TELNET_PORT);
		connection.connect();

		// login
		if (!sendCommand(String.format("%s=%s%s", getUsername(), getPassword(),
				NEW_LINE_CHAR))) {
			throw new IOException(" Unable to compleate send command action, please check console log above!");
		}
		String result = readBuffer();
		if (!result.contains(LOGIN_OK))
			GeneralUtils.printToConsole("Failed to login.");

	}

	@Override
	public boolean powerOnPort(PowerControllerPort port) {
		setPort(port.getPort(), true);
		return true;
	}

	@Override
	public boolean powerOffPort(PowerControllerPort port) {
		setPort(port.getPort(), false);
		return true;
	}

	private void setPort(String portName, boolean powerOn) {
		String command = "";
		String result = "";
		try {
			command = createSetPortString(portName, powerOn) + NEW_LINE_CHAR;
			GeneralUtils.printToConsole("Executing command: " + command);
			if (!sendCommand(command)) {
				throw new IOException(" Unable to compleate send command action, please check console log above!");
			}
			result = readBuffer();
			if (!result.contains(SET_PORT_OK))
				GeneralUtils.printToConsole(String.format("Failed to execute command \"%s\".\nbuffer:\n%s\n", command,
						result));
		}
		catch (Exception e) {
			GeneralUtils.printToConsole(String.format("Failed to execute command \"%s\".\nbuffer:\n%s\n", command,
					result));
		}
	}

	private String createSetPortString(String portName, boolean powerOn)
			throws Exception {
		char[] setPortChars = getPort().toCharArray();
		int portIndex = getPortIndex(portName);
		setPortChars[portIndex] = powerOn ? SET_PORT_STATUS_ON
				: SET_PORT_STATUS_OFF;
		return SET_PORT_CMD + new String(setPortChars);
	}

	private int getPortIndex(String portName) {
		portName = portName.toLowerCase()
				.replaceAll(PORT_NAME_PREFIX.toLowerCase(), "").trim();
		int portIndex = Integer.parseInt(portName);
		return portIndex - 1;
	}

	private String getPort() throws Exception {
		if (!sendCommand(GET_PORT_CMD)) {
			throw new IOException(" Unable to compleate send command action, please check console log above!");
		}
		String[] lines = readBuffer().split(NEW_LINE_CHAR);
		String setPortString = "";
		for (String line : lines) {
			if (line.contains(GET_PORT_DELIMITER)) {
				String[] params = line.trim().split(GET_PORT_DELIMITER);
				setPortString += params[GET_PORT_STATUS_INDEX]
						.contains(GET_PORT_STATUS_ON) ? SET_PORT_STATUS_ON
								: SET_PORT_STATUS_OFF;
			}
		}

		return setPortString;
	}

	@Override
	public String getUsername() {
		if (super.getUsername() == null)
			setUsername(DEFAULT_USERNAME);
		return super.getUsername();
	}

	@Override
	public String getPassword() {
		if (super.getPassword() == null)
			setPassword(DEFAULT_PASSWORD);
		return super.getPassword();
	}
}

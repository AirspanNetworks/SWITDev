package Utils;

import java.io.IOException;

import org.joda.time.DateTime;
import org.junit.Ignore;

import jsystem.framework.report.Reporter;
import jsystem.framework.system.SystemObjectImpl;
import systemobject.terminal.RS232;
import systemobject.terminal.Telnet;
import systemobject.terminal.Terminal;

public class MoxaCom extends SystemObjectImpl {
	public static final int DEFAULT_TIMEOUT = 50;

	private static final int MAX_TRYOUTS = 3;

	private static final long REST_TIME = 1200;

	private static int totalTryouts = 0;

	private Terminal serial;
	private MoxaComMode mode;
	private String comName;
	private int baudRate;
	private int dataBit;
	private int stopBit;
	private int parity;
	private int port;
	private int timeout;

	public void init() throws Exception {
		super.init();
		if (comName.toLowerCase().contains("com"))
			mode = MoxaComMode.COM;
		else
			mode = MoxaComMode.REVERSE_TELNET;
		if (timeout == 0)
			timeout = DEFAULT_TIMEOUT;

	}

	public void connect() {
		int tryouts = 1;

		try {
			if (serial != null) {
				serial.closeStreams();
				serial = null;
			}
			if (mode == MoxaComMode.COM)
				serial = new RS232(comName.toUpperCase(), baudRate, dataBit, stopBit, parity);
			else if (mode == MoxaComMode.REVERSE_TELNET) {
				serial = new Telnet(comName, port);
				((Telnet) serial).setCharSet("UTF-8");
			}
			while (tryouts <= MAX_TRYOUTS) {
				try {
					serial.connect();
					break;
				} catch (Exception ex) {
					System.err.println("Fail to connect to serial.. trying again, attempt#" + tryouts);
					ex.printStackTrace();
					Thread.sleep(REST_TIME);
				}
				tryouts++;
				totalTryouts = totalTryouts + 1;
			}
			if (tryouts > MAX_TRYOUTS)
				throw new Exception("Fail to connect to serial " + MAX_TRYOUTS + " times, see logs above");

		} catch (Exception e) {
			report.report("Failed to connect to serial " + comName + ":" + port, Reporter.WARNING);
			e.printStackTrace();
		}

	}

	public boolean sendString(String cmd, boolean delayedTyping) {
		try {
			if (!isConnected()) {
				serial.connect();
				GeneralUtils.unSafeSleep(5 * 1000);
			}

			serial.sendString(cmd, delayedTyping);
			return true;
		} catch (Exception e) {
			GeneralUtils.printToConsole(this.getComName() + "->sendString failed Will try to reset connection: " + e.getMessage());
			this.reset();
		}

		try {
			serial.sendString(cmd, delayedTyping);
			return true;
		} catch (Exception e) {
			GeneralUtils.printToConsole(this.getComName() + "->sendString retry falied: " + e.getMessage());
			return false;
		}
	}

	public boolean isConnected() {
		try {
			return serial.isConnected();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public void reset() {
		try {
			serial.disconnect();
		} catch (IOException e) {
			GeneralUtils.printToConsole(this.getComName() + "->Disconnect from session failed: " + e.getMessage());
		}
		try {
			serial.connect();
		} catch (IOException e) {
			GeneralUtils.printToConsole(this.getComName() + "->connect to session failed: " + e.getMessage());
		}
	}

	public String getResult() {
		return getResult(5000);
	}

	public String getResult(int timeout) {
		String result = "";
		try {
			result = serial.readInputBuffer();
			String buffer = "";
			DateTime target = DateTime.now().plus(timeout);
			while (result.isEmpty() || (!buffer.isEmpty())) {
				GeneralUtils.unSafeSleep(DEFAULT_TIMEOUT);
				buffer = serial.readInputBuffer();
				result += buffer.trim();
				if (timeout > 0 && DateTime.now().isAfter(target)) {
					StdOut.print2console(String.format("Timeout reached [%d] msec", timeout));
					break;
				}
			}
		} catch (Exception e) {
			GeneralUtils.printToConsole("Failed connection with serial " + comName);
			GeneralUtils.unSafeSleep(1000);
		}
		return result;
	}

	public void addPrompt(String promt, boolean isRegExp) {
		serial.addPrompt(promt, isRegExp);
	}

	public MoxaComMode getMode() {
		return mode;
	}

	public void setMode(MoxaComMode mode) {
		this.mode = mode;
	}

	public String getComName() {
		return comName;
	}

	public void setComName(String comName) {
		this.comName = comName;
	}

	public int getTimeout() {
		return timeout;
	}

	public Terminal getSerial() {
		return this.serial;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public int getBaudRate() {
		return baudRate;
	}

	public void setBaudRate(int baudRate) {
		this.baudRate = baudRate;
	}

	public int getDataBit() {
		return dataBit;
	}

	public void setDataBit(int dataBit) {
		this.dataBit = dataBit;
	}

	public int getStopBit() {
		return stopBit;
	}

	public void setStopBit(int stopBit) {
		this.stopBit = stopBit;
	}

	public int getParity() {
		return parity;
	}

	public void setParity(int parity) {
		this.parity = parity;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void close() {
		if (serial != null) {
			report.report("Total Moxa tryouts=" + totalTryouts);
			try {
				serial.disconnect();
			} catch (IOException e) {
				report.report(e.getMessage(), Reporter.FAIL);
				e.printStackTrace();
			}
		}
		super.close();
	}

	@Override
	public String toString() {
		comName = comName.toUpperCase();
		if (comName.contains("COM"))
			return comName;
		else
			return comName + ":" + port;
	}

	/**
	 * @return the totalTryouts
	 */
	@Ignore
	public static int getTotalTryouts() {
		return totalTryouts;
	}

	public void clearBuffer() {
		try {
			serial.readInputBuffer();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

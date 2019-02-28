package testsNG.Actions.Utils;

/**
 * Inner Class
 * Organizes the received Events for the current EnB SW object.
 */
public class NetspanSWEvents {

	public boolean downloadProgress;
	public boolean downloadCompleted;
	public boolean activateProgress;
	public boolean activateCompleted;

	/**
	 * Received Event constructor, initial all the events to false at the beginning.
	 */
	NetspanSWEvents() {
		this.downloadProgress = false;
		this.downloadCompleted = false;
		this.activateProgress = false;
		this.activateCompleted = false;
	}

	/**
	 * String Enums represent the 4 netspan events while upgrading SW version
	 */
	public enum NetspanEvents {
		DOWNLOAD_IN_PROGRESS("Download in progress"),
		DOWNLOAD_COMPLETED("Download completed"),
		ACTIVATE_IN_PROGRESS("Activate in progress"),
		ACTIVATE_COMPLETED("Activate completed");

		private final String text;

		/**
		 * constructor to set string to Enum
		 *
		 * @param string - netspanEvent
		 */
		NetspanEvents(final String string) {
			this.text = string;
		}

		@Override
		public String toString() {
			return text;
		}
	}
}
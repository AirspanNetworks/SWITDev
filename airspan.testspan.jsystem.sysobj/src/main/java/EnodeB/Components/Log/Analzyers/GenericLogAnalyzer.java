package EnodeB.Components.Log.Analzyers;

import jsystem.framework.report.Reporter;

import EnodeB.Components.Log.LogListener;
import EnodeB.Components.Log.LoggerEvent;

/**
 * The Class GenericLogAnalyzer.
 */
public abstract class GenericLogAnalyzer implements LogListener {
	
	/** The report. */
	protected Reporter report;
	
	/** The suspend. */
	private boolean suspend;
	
	/**
	 * Instantiates a new generic log analyzer.
	 *
	 * @param report the report
	 */
	public GenericLogAnalyzer(Reporter report) {
		this.report = report;
		this.suspend = false;
	}
	/**
	 * the event of the log listener invoke this method.
	 */
	public void getLogLine(LoggerEvent e) {
		String line = e.getLine();
			if (!suspend && condition(line)) {
				analyzeLine(line);
			}
	}
	
	/**
	 * Suspend the analzyer.
	 */
	public void suspend() {
		this.suspend = true;
	}
	
	/**
	 * Resume the analazyer.
	 */
	public void resume() {
		this.suspend = false;
	}
	
	/**
	 * Condition to be made inorder to analyze the logline based on it's proprties.
	 *
	 * @param line the line
	 * @return true, if successful
	 */
	protected abstract boolean condition(String line);
	
	/**
	 * Analyzing the log line.
	 *
	 * @param line the line
	 */
	protected abstract void analyzeLine(String line);
	
	/**
	 * Gets the analyzed results, by using the reporter.
	 * 
	 *
	 * @return the analyzed results
	 */
	public abstract void getAnalyzedResults();
}

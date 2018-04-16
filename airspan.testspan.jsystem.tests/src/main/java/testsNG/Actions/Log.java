package testsNG.Actions;

import org.junit.Assert;

import EnodeB.EnodeB;
import jsystem.framework.report.ListenerstManager;
import jsystem.framework.report.Reporter;
import EnodeB.Components.EnodeBComponentTypes;
import EnodeB.Components.Log.Analzyers.RegExLogLineAnalazyer;
import Utils.GeneralUtils;

public class Log {

	private static Log instance;

	/** The Constant READ_INTERVAL_SEC. */
	private static final int READ_INTERVAL = 15 * 1000; 

	/** The analzyer. */
	private RegExLogLineAnalazyer analzyer;
	
	/** The analzyer. */
	private String expression;
	
	/** The enb component. */
	private EnodeBComponentTypes enbComponent = EnodeBComponentTypes.XLP;

	public static Reporter report = ListenerstManager.getInstance();

	private Log() {
	}

	public static Log getInstance() {
		if (instance == null)
			instance = new Log();
		return instance;
	}

	public void setLogLineToAnalyze(EnodeB enodeB, EnodeBComponentTypes component, String expectedLogLine) {
		expression = expectedLogLine;
		analzyer = new RegExLogLineAnalazyer(report, expectedLogLine);
		registerAnalzyer(enodeB, component);
		GeneralUtils.unSafeSleep(READ_INTERVAL);
		report.report(String.format("[INFO]: Start counting \"%s\" log line appearances.", expression));
	}

	public Integer getLogLineAnalyzerCount() throws Exception {				
		if (analzyer.getMatchCount() > 0) {
			report.report(String.format("[INFO]: The expected log line \"%s\" has been found %s Times.",
					expression, analzyer.getMatchCount()));
			analzyer.getAnalyzedResults();
			return analzyer.getMatchCount();
		}
		else{
			report.report(String.format("[INFO]: The expected log line \"%s\" was not found yet.",
					expression));
			return 0;
		}
	}

	public boolean waitForLogLine(EnodeB enodeB, EnodeBComponentTypes component, String expectedLogLine, long timeOut) {
		expression = expectedLogLine;
		analzyer = new RegExLogLineAnalazyer(report, expectedLogLine);
		registerAnalzyer(enodeB, component);
		long startTime = System.currentTimeMillis();
		long runTime = timeOut;
		
		while (runTime > 0) {
			if (analzyer.getMatchCount() > 0) {
				long endTime = (System.currentTimeMillis() - startTime) / 1000;
				report.report(String.format("[INFO]: The expected log line \"%s\" been found! \n after %s [SEC]",
						expectedLogLine, endTime));
				analzyer.getAnalyzedResults();
				return true;
			}
			GeneralUtils.unSafeSleep(READ_INTERVAL);
			runTime -= READ_INTERVAL;
		}
		
		long endTime = (System.currentTimeMillis() - startTime) / 1000;
		report.report(
				String.format("[INFO]: Timeout been reached, the expected log line \"%s\" was not found! \n after %s [SEC]",
						expectedLogLine, endTime));
		return false;
	}

	/**
	 * Register analzyer.
	 */
	private void registerAnalzyer(EnodeB enodeB, EnodeBComponentTypes component) {
		switch (enbComponent) {
		case XLP:
			enodeB.addListenerToLogger(analzyer);
			break;
		default:
			Assert.fail("No component been choosen");
			break;
		}
	}

}

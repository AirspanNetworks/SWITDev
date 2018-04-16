package EnodeB.Components.Log.Analzyers;

import jsystem.framework.report.Reporter;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * this class analyze log lines based on a regular expression.
 * more information about regular expression can be found here:
 * @url https://docs.oracle.com/javase/tutorial/essential/regex/char_classes.html
 * @author aviad shiber
 *
 */
public class RegExLogLineAnalazyer extends GenericLogAnalyzer {

	private int matchCount;
	private Pattern pattern;
	
	public RegExLogLineAnalazyer( Reporter report,String expression ) {
		super( report );
		matchCount=0;
		pattern=Pattern.compile( expression );
	}

	@ Override
	protected boolean condition( String line ) {
		 Matcher matcher = pattern.matcher( line );
		 return matcher.find();
	}

	@ Override
	protected void analyzeLine( String line ) {
		matchCount++;
	}

	@ Override
	public void getAnalyzedResults() {
	}

	/**
	 * @return the matchCount
	 */
	public int getMatchCount() {
		return matchCount;
	}
}

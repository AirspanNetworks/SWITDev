package EnodeB.Components.Log.Analzyers;

import jsystem.framework.report.Reporter;
import jsystem.framework.report.Reporter.ReportAttribute;

public class StringAnalyzer {
	/** The text to analyze. */
	private String text;
	/** The analyzer - used in Command Analyzer only. */
	private StringAnalyzers analyzer = StringAnalyzers.Contains;
	/** The analyzerValues, list of strings to analyze - used in Command Analyzer only. */
	private String analyzerValue;
	/** The report - Used to log events from your test. */
	private Reporter report;

	/**
	 * The class analyzes specific "value" in a given "text", 
	 * To determine weather a certain condition "analyzer" occurs or not.
	 * @param analyzer 
	 * @param value
	 * @param text
	 * @param report - Reporter object.
	 */
	public StringAnalyzer(StringAnalyzers analyzer, String value, String text, Reporter report){
		this.text = text;
		this.analyzer = analyzer;
		this.analyzerValue = value;
		this.report = report;
	}
	
	/**
	 * Analyze the text using the chosen analyzer and values.
	 * @param analyzer
	 * @param value 
	 * @param text 
	 * @return boolean indicates if the analysis worked as expected or not. 
	 */
	public boolean analyze(){		
		switch (analyzer.toString()) {
		case "Contains":
			report.report(String.format("Looking for: \"%s\"", analyzerValue));
			if(text.contains(analyzerValue)){
				report.report(String.format("The command output contains the value \"%s\", as expected.\n", analyzerValue), ReportAttribute.BOLD);
				return true;
			}
			else{
				report.report(String.format("Cannot find the value \"%s\" in the command output, failing test.\n", analyzerValue), Reporter.FAIL);
				return false;
			}
		case "NotContains":
			report.report(String.format("Looking for: \"%s\"", analyzerValue));
			if(!text.contains(analyzerValue)){
				report.report(String.format("The command output doesn't contain the value \"%s\", as expected.\n", analyzerValue), ReportAttribute.BOLD);
				return true;
			}
			else{
				report.report(String.format("The value \"%s\" was found in the command output, failing test.\n", analyzerValue), Reporter.FAIL);
				return false;
			}
		default:
			break;
		}
		
		return true;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public StringAnalyzers getAnalyzer() {
		return analyzer;
	}

	public void setAnalyzer(StringAnalyzers analyzer) {
		this.analyzer = analyzer;
	}

	public String getAnalyzerValue() {
		return analyzerValue;
	}

	public void setAnalyzerValue(String analyzerValue) {
		this.analyzerValue = analyzerValue;
	}

	public Reporter getReport() {
		return report;
	}

	public void setReport(Reporter report) {
		this.report = report;
	}
}

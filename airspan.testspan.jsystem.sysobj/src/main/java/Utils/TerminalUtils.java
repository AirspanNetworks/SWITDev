package Utils;

import java.text.Normalizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatterBuilder;

public class TerminalUtils {
	public static String filterVT100(String vt100Input) {
		return vt100Input.replaceAll("\\e\\[[\\d;]*[^\\d;]", "");
	}

	public static String deAccent(String str) {
		String nfdNormalizedString = Normalizer.normalize(str,
				Normalizer.Form.NFD);
		Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
		return pattern.matcher(nfdNormalizedString).replaceAll("");
	}

	/*
	 * removing characters that are unknown
	 */
	public static String removeNonASCII(String input) {
		return input.replaceAll("[^\\p{ASCII}]", "");
	}

	/**
	 * Merge all log lines in the sources into one output file that has all the
	 * log lines. The merging algorithm is using the log line's timestamp to
	 * merge the lines into a readable sequence.
	 * 
	 * @param reference
	 *            An old buffer that is used to prevent duplications with
	 *            already analyzed lines.
	 * @param sources
	 *            The buffers of all the sessions we want to merge
	 * @return a sorted string that doesn't contain duplicated log lines
	 *         (between sources).
	 */
	public static String deduplicateAndMerge(String reference, String... sources) {
		String[][] sourcesLines = new String[sources.length][];
		int[] indexs = new int[sources.length];

		// Populate the sourcesLines array.
		for (int sourceIndx = 0; sourceIndx < sources.length; sourceIndx++)
			sourcesLines[sourceIndx] = sources[sourceIndx].split("\n");

		// Saves the time stamps of last inserted lines.
		String refTimestamp = "";
		// Insert the last time stamp in the reference string to make sure that
		// older lines won't get inserted.
		if (reference.length() > 0) {
			String[] refLines = reference.split("\n");
			for (int lineIndx = refLines.length - 1; lineIndx >= 0; lineIndx--) {
				String tempTimestamp = getLogTimestamp(refLines[lineIndx]);
				if (!tempTimestamp.equals("")) {
					refTimestamp = tempTimestamp;
					break;
				}
			}

		}

		// filter log lines that appear at or older then the reference.
		boolean filtering = true;
		while (filtering) {
			filtering = false;

			for (int sourceIndx = 0; sourceIndx < sourcesLines.length; sourceIndx++)
				if (indexs[sourceIndx] < sourcesLines[sourceIndx].length) {
					filtering = true;
					String line = sourcesLines[sourceIndx][indexs[sourceIndx]++]
							.trim();
					if (!line.isEmpty()) {
						String timestamp = getLogTimestamp(line);

						if (!timestamp.isEmpty()
								&& (compareTimestamps(timestamp, refTimestamp) >= 0 || (compareTimestamps(timestamp, refTimestamp) == 0 && 
								reference.contains(line.substring(line.indexOf(timestamp)))))) {
							// Delete all the lines that came before the current
							// line (including the current line).
							for (int lineIndx = indexs[sourceIndx] - 1; lineIndx >= 0
									&& sourcesLines[sourceIndx][lineIndx] != null; lineIndx--)
								sourcesLines[sourceIndx][lineIndx] = null; // null = deleted
						}
					}
				}
		}

		deduplicateLinesBetweenSources(sourcesLines);
		return mergeLogLines(sourcesLines);
	}

	/**
	 * Extracts the timestamp from a log line.
	 * 
	 * @param line
	 * @return the timestamp if exists, otherwise returns an empty string.
	 */
	protected static String getLogTimestamp(String line) {
		String datePattern = "[\\d]{2}/[\\d]{2}/[\\d]{4} [\\d]{2}:[\\d]{2}:[\\d]{2}:[\\d]{3}";
		Pattern pattern = Pattern.compile(datePattern);
		Matcher matcher = pattern.matcher(line);
		if (!matcher.find())
			return "";

		return matcher.group();
	}

	/**
	 * Removes duplicate lines between different sources.
	 * 
	 * @param sourcesLines
	 *            the sources lines.
	 */
	private static void deduplicateLinesBetweenSources(String[]... sourcesLines) {
		// for each two sources, set one as reference and use it to find
		// duplicate lines in the other.
		for (int refSourceIndx = 0; refSourceIndx < sourcesLines.length - 1; refSourceIndx++) {
			for (int refLineIndx = 0; refLineIndx < sourcesLines[refSourceIndx].length; refLineIndx++) {
				String refLine = sourcesLines[refSourceIndx][refLineIndx];
				if (refLine == null || refLine.isEmpty())
					continue;
				String refTimestamp = getLogTimestamp(refLine);
				if(refTimestamp.isEmpty()) continue;

				for (int sourceIndx = refSourceIndx + 1; sourceIndx < sourcesLines.length; sourceIndx++) {
					for (int lineIndx = 0; lineIndx < sourcesLines[sourceIndx].length; lineIndx++) {
						String line = sourcesLines[sourceIndx][lineIndx];
						if (line == null)
							continue;
						String timestamp = getLogTimestamp(line);
						if (timestamp.equals(refTimestamp) && line.contains(refLine.trim()))
							sourcesLines[sourceIndx][lineIndx] = null; // Delete the duplicated line.
						else if (compareTimestamps(timestamp, refTimestamp) < 0)
							break;
					}
				}

			}
		}
	}

	/**
	 * Merges lines from multiple sources using their timestamp.
	 * 
	 * @param sourcesLines
	 *            the lines to merge
	 * @return merged string that contains all the lines.
	 */
	private static String mergeLogLines(String[]... sourcesLines) {
		int[] indexs = new int[sourcesLines.length];
		String output = "";

		// get each index to the first line in the array that not equals to
		// null.
		for (int sourceIndx = 0; sourceIndx < sourcesLines.length; sourceIndx++) {
			while (indexs[sourceIndx] < sourcesLines[sourceIndx].length
					&& sourcesLines[sourceIndx][indexs[sourceIndx]] == null)
				indexs[sourceIndx]++;
		}

		boolean mergeLines = true;
		while (mergeLines) {
			mergeLines = false;

			String refTimestamp = "";
			String candidateLine = null;
			int candidateSourceIndx = 0;

			for (int sourceIndx = 0; sourceIndx < sourcesLines.length; sourceIndx++) {
				String line = null;
				do{
					if(indexs[sourceIndx] >= sourcesLines[sourceIndx].length)
						break;
					
					line = sourcesLines[sourceIndx][indexs[sourceIndx]];
					if (line == null) {
						indexs[sourceIndx]++;
						continue;
					}
					mergeLines = true;
					line = line.trim();
					String timestamp = getLogTimestamp(line);
					if (candidateLine == null || 
						 (!refTimestamp.isEmpty() && 
						   (timestamp.isEmpty() || 
							compareTimestamps(timestamp, refTimestamp) > 0))) {
						refTimestamp = timestamp;
						candidateLine = line;
						candidateSourceIndx = sourceIndx;
					}
				} while (line == null);
			}

			if(candidateLine != null) {
				indexs[candidateSourceIndx]++;
				output += candidateLine + "\n";
			}
		}

		return output;
	}

	/**
	 * Generates reference string for the deduplication method
	 * 
	 * @param log
	 *            the log to create reference from.
	 * @return reference from the last timestamp found in the log.
	 */
	public static String generateReferenceString(String log) {

		// Saves the timestamps of last inserted lines.
		String[] refLines = log.split("\n");
		if (refLines.length > 0) {
			int lineIndx = refLines.length;
			String refTimestamp = "";
			
			while (lineIndx > 0 && refTimestamp.isEmpty())
				refTimestamp = getLogTimestamp(refLines[--lineIndx]);
			
			
			if (refTimestamp.isEmpty())
				return "";

			return log.substring(log.indexOf(refLines[Math.max(0, Math.min(lineIndx + 1, refLines.length - 1))]));
		}

		return "";
	}
	
	/**
	 * Compares two string timestamps in the format DD/MM/YYYY HH:MM:SS:mmm
	 * 
	 * @param timestampA
	 * @param timestampB
	 * 
	 * @return (-1) if timestampA > timestampB, 0 if timestampA = timestampB and 1 if timestampA < timestampB
	 */
	private static int compareTimestamps(String timestampA, String timestampB) {
		DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder().
				          appendDayOfMonth(2).appendLiteral('/').appendMonthOfYear(2).appendLiteral('/').appendYear(4, 4).appendLiteral(' ').
				          appendHourOfDay(2).appendLiteral(':').appendMinuteOfHour(2).appendLiteral(':').appendSecondOfMinute(2).appendLiteral(':').
				          appendMillisOfSecond(3);
		
		// Handle empty timestamps
		if (timestampA.isEmpty() && !timestampB.isEmpty())
			return 1;
		else if (!timestampA.isEmpty() && timestampB.isEmpty())
			return -1;
		else if (timestampA.isEmpty() && timestampB.isEmpty())
			return 0;
		
		
		DateTime dateTimeA = DateTime.parse(timestampA, builder.toFormatter());
		DateTime dateTimeB = DateTime.parse(timestampB, builder.toFormatter());
		
		int result = dateTimeB.compareTo(dateTimeA);
		
		// Normalize result
		if (result < 0)
			result = -1;
		else if (result > 0)
			result = 1;
		
		return result;
	}

}

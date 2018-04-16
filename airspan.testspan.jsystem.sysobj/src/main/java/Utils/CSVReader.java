package Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

// TODO: Auto-generated Javadoc
/**
 * The Class CSVReader.
 */
public class CSVReader {

	/** The CSV path. */
	private static String csvPath = "";

	/** The reader. */
	private static BufferedReader reader;

	/** The matrix. */
	private static String[][] matrix;

	/** The Constant CSV_DELIMITER. */
	private static final String CSV_DELIMITER = ",";

	/**
	 * The methods get a path of CSV file and return a matrix that represent the CSV file.
	 * 
	 * @param path the path
	 * @return the string[][]
	 */
	public static String[][] CSV2Matrix( String path ) {

		ArrayList<String> lines = null;
		if ( path != csvPath ) {
			csvPath = path;
			reader = null;
			lines = new ArrayList<String>();
		}
		if ( reader == null ) {
			try {
				GeneralUtils.printToConsole( "Trying to create buffer in CSVReader from path:"+path );
				reader = new BufferedReader( new FileReader( csvPath ) );
				GeneralUtils.printToConsole( "Trying to convert CSV file to matrix..." );
				readFromCsvFileToList( lines );
				matrix = buildMatrixFromList( lines );

			} catch ( FileNotFoundException e ) {

				GeneralUtils.printToConsole( e.getMessage() );
				e.printStackTrace();
			} catch ( IOException e ) {
				GeneralUtils.printToConsole( e.getMessage() );
				e.printStackTrace();
			}
			finally {
				try {
					reader.close();
				} catch ( IOException e ) {
					GeneralUtils.printToConsole( "Fail to close csv" );
					e.printStackTrace();
				}
			}

		}

		return matrix;
	}
	public static String[][] CSV2Matrix( File file ) {
		String path = null;
		try {
			path = file.getCanonicalPath();
		} catch ( IOException e ) {
			e.printStackTrace();
		}
		return CSV2Matrix( path );
	}

	/**
	 * Builds the matrix from list.
	 * 
	 * @param lines the lines
	 * @return the string[][]
	 */
	private static String[][] buildMatrixFromList( ArrayList<String> lines ) {
		String[][] mat = new String[lines.size()][getColumnCsvMaxSize( lines )];
		for ( int i = 0; i < lines.size(); i++ ) {
			mat[i] = lines.get( i ).split( CSV_DELIMITER );
		}
		return mat;
	}

	/**
	 * Gets the maximum number of columns in the CSV file.
	 * 
	 * @param lines the lines
	 * @return the column csv max size
	 */
	private static int getColumnCsvMaxSize( ArrayList<String> lines ) {
		int maxSize = 0;
		for ( String line : lines ) {
			String[] columns = line.split( CSV_DELIMITER );
			if ( columns.length > maxSize )
				maxSize = columns.length;
		}
		return maxSize;
	}

	/**
	 * Read from CSV file to list. -The method skips empty rows in the CSV file.
	 * 
	 * @param lines the lines
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private static void readFromCsvFileToList( ArrayList<String> lines ) throws IOException {
		String line = "";
		while ( ( line = reader.readLine() ) != null ) {
			// if not an empty line
			if ( !line.replaceAll( ",", "" ).trim().equals( "" ) )
				lines.add( line );
		}
	}

}

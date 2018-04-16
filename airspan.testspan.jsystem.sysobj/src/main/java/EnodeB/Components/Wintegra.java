package EnodeB.Components;

import java.util.Hashtable;

import EnodeB.Components.Cli.Cli;
import Utils.GeneralUtils;

// TODO: Auto-generated Javadoc
/**
 * The Class Wintegra.
 */
public class Wintegra extends EnodeBComponent {

	/** The Constant TNET_PROMPT. */
	public static final String TNET_PROMPT = "tnet > ";

	/** The Constant TNET_END_CHAR. */
	public static final String TNET_END_CHAR = "";

	/** The Constant TNET_ROWS_DELIMITER. */
	public static final String TNET_ROWS_DELIMITER = "Table: %d  ";

	/** The Constant TNET_COLS_DELIMITER. */
	public static final String TNET_COLS_DELIMITER = ", ";

	/** The Constant TNET_DATA_DELIMITER. */
	public static final String TNET_DATA_DELIMITER = "=";

	/*
	 * (non-Javadoc)
	 * @see EnodeB.Components.EnodeBUpgradableComponent#init()
	 */
	@ Override
	public void init() throws Exception {
		setName(getParent().getName() + "_Wintegra");
		super.init();
		report.report(String.format("WINTEGRA(ver:%s) initialized!", getVersion()));
	}

	/*
	 * (non-Javadoc)
	 * @see EnodeB.Components.EnodeBComponent#addPrompts(EnodeB.Components.Cli.Cli)
	 */
	@ Override
	public void addPrompts( Cli cli ) {
		super.addPrompts( cli );

		cli.addPrompt( TNET_PROMPT, SHELL_PROMPT, new String[] { "cd /bs", "./tnet\r" }, "quit" );
	}

	/**
	 * Gets the a table via tnet.
	 * 
	 * @param table the table
	 * @return the hashtable
	 */
	public Hashtable<String, String[]> get( int table ) {
		String output = tnet( "get " + table );
		try {
			int startIndex = output.indexOf( String.format( TNET_ROWS_DELIMITER, table ) );
			int endIndex = output.indexOf( TNET_END_CHAR );

			Hashtable<String, String[]> tableData = new Hashtable<String, String[]>();

			output = output.substring( startIndex, endIndex - 1 ).trim();
			output = output.replaceAll( "\\[", "index=" );
			output = output.replaceAll( "\\]", "," );
			output = output.substring( String.format( TNET_ROWS_DELIMITER, table ).length() );
			String[] rows = output.split( String.format( TNET_ROWS_DELIMITER, table ) );
			for ( int rowIndx = 0; rowIndx < rows.length; rowIndx++ ) {
				String[] cols = rows[rowIndx].split( TNET_COLS_DELIMITER );
				for ( int colIndx = 0; colIndx < cols.length; colIndx++ ) {
					String[] data = cols[colIndx].split( TNET_DATA_DELIMITER );
					String colName = data[0];
					String colValue = data[1];

					String[] tableColumn = null;
					if ( tableData.containsKey( colName ) )
						tableColumn = tableData.get( colName );
					else
						tableColumn = new String[rows.length];

					tableColumn[rowIndx] = colValue;
					tableData.put( colName, tableColumn );
				}

			}
			return tableData;

		} catch ( Exception e ) {
			GeneralUtils.printToConsole(String.format("tnet get caught the Exception \"%s\".\nCause:%s\nE output = %s\n", e.getMessage(), e.getCause(), output ));
			return null;
		}
	}

	/**
	 * Sets a value in wintegra via tnet.
	 * 
	 * @param table The table in wintegra
	 * @param index The index of the table
	 * @param column The column
	 * @param value For example Wintegra.set(17, "1U", "28", 2670000)
	 * @return true, if successful
	 */
	public boolean set( int table, String index, String column, String value ) {
		String output = tnet( String.format( "set %d [%s] %s=%s", table, index, column, value ) );
		return output.contains( "set: OK" );
	}

	/**
	 * send commands via Tnet
	 * 
	 * @param commands the commands
	 * @return the string
	 */
	public String tnet( String command ) {
		return sendCommands( TNET_PROMPT, command, "");
	}

}

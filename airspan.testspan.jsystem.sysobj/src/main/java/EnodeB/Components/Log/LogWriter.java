package EnodeB.Components.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import jsystem.framework.report.ReporterHelper;

public class LogWriter {
	private static final String LOG_FILE_SUFFIX = ".log";
	
	
	private Hashtable<File, PrintStream> logFiles;
	private Logger logger;
	private String name;
	
	public LogWriter(Logger logger) {
		this.logger = logger;
		this.name = logger.getName() + ".logWriter";
		this.logFiles = new Hashtable<File, PrintStream>();
		
		System.out.printf("[%s]: Initialized.\n", name);
	}
	
	public void addLog(String fileName, String prefix) {
		if (findlogFile(fileName, prefix) != null) {
			System.out.printf("[%s]: %s with prefix %s exsits in the logWriter, can't add log.\n", name, fileName, prefix);
			return;
		}
		
		System.out.printf("[%s]: Adding log: %s, prefix=%s.\n", name, fileName, prefix);
		File logFile = createLogFile(fileName, prefix);
		if (logFile != null) {
			try {
				logFiles.put(logFile, new PrintStream(logFile));
				System.out.printf("[%s]: Now logging %s\n", name, logFile.getAbsolutePath());
			} catch (FileNotFoundException e) 
			{
				e.printStackTrace();
			}
		} else
			System.err.printf("[%s]: Couldn't add log file:%s, prefix=%s.\n", name, fileName, prefix);
		
		if (!logger.isLogging()) {
			System.out.printf("[%s]: Logger is not running, starting logger.\n", name);
			logger.start();
		}
	}
	
	private File createLogFile(String fileName, String prefix) {
		// Check if this a path to a file.
		Path filePath = Paths.get(fileName);
		if (filePath.isAbsolute()) {
			String fname = filePath.getFileName().toString();
			filePath = Paths.get(fileName.replace(fname, prefix + "_" + fname));
			if (filePath.getParent().toFile().exists()) {
				if (filePath.toFile().exists()) {
					System.out.printf("[%s]: File \"%s\" already exists. Trying to make it writeable.\n", name, filePath.toString());
					if (filePath.toFile().setWritable(true))
						System.out.printf("[%s]: File \"%s\" is now writeable.\n", name, filePath.toString());
					else
						System.err.printf("[%s]: File \"%s\" is read-only. This log file may not be updated!\n", name, filePath.toString());
				}
			} else {
				try {
					filePath.getParent().toFile().mkdirs();
				} catch (Exception e) 
				{
					e.printStackTrace();
				}
			}
		} else { 
			return new File(logger.getReporter().getCurrentTestFolder(), prefix + "_" + fileName + LOG_FILE_SUFFIX);
			//return File.createTempFile(prefix + "_" + fileName, LOG_FILE_SUFFIX);
		}
		
		return filePath.toFile();
	}
	
	public int size() {
		return logFiles.size();
	}
	
	public synchronized void writeLog(String line, String fileName, String prefix) {
		ArrayList<File> filesToClose = new ArrayList<File>();
		for (Enumeration<File> files =  logFiles.keys(); files.hasMoreElements(); ) {
			File logFile = files.nextElement();
			if (logFile.getAbsolutePath().contains(fileName) && logFile.getName().contains(prefix)) {
				try {
					logFiles.get(logFile).println(line);
					logFiles.get(logFile).flush();
				} catch (Exception e) {	
					filesToClose.add(logFile);
					e.printStackTrace();
				}
			}
		}
		
		for (File logFile : filesToClose) {
			System.out.printf("[%s]: Closing file: %s.\n", name, logFile);
			logFiles.remove(logFile);
		}
	}
	
	public synchronized void writeLog(String line) {
		ArrayList<File> filesToClose = new ArrayList<File>();
		for (Enumeration<File> files =  logFiles.keys(); files.hasMoreElements(); ) {
			File logFile = files.nextElement();
			try {
				logFiles.get(logFile).println(line);
				logFiles.get(logFile).flush();
			} catch (Exception e) {	
				filesToClose.add(logFile);
			}
		}
		
		for (File logFile : filesToClose) {
			System.out.printf("[%s]: Closing file: %s.\n", name, logFile.getAbsolutePath());
			logFiles.remove(logFile);
		}
	}
	
	public void closeLog(String fileName) {
		closeLog(fileName, "");
	}

	
	public void closeLog(String fileName, String prefix) {
		System.out.printf("[%s]: Sending log close request to %s, prefix=%s.\n", name, fileName, prefix);
		File[] logFileArr = findlogFile(fileName, prefix);
		if (logFileArr == null) {
			System.err.printf("[%s]: Can't close log (%s, prefix=%s), file not found.\n", name, fileName, prefix);
			return;
		}

		closeLogFiles(logFileArr);
	}
	
	private void closeLogFiles(File... logFilesToClose) {
		for (File logFile : logFilesToClose) {
			try {
				System.out.printf("[%s]: Sending log file:%s.\n", name, logFile.getAbsolutePath());
				logFiles.get(logFile).close();
				linkLogToTest(logFile.getName());
				if(logFiles!= null && logFiles.containsKey(logFile))
					logFiles.remove(logFile);
			} 
			catch(Exception e) {
				System.err.printf("[%s]: Error moving log file %s to test folder.\n", name, logFile.getAbsolutePath());
				e.printStackTrace();
			}
		}
	}

	public void closeAll() {
		for (Enumeration<File> files =  logFiles.keys(); files.hasMoreElements(); ) {
			File logFile = files.nextElement();
			closeLogFiles(logFile);
		}
	}
	
	public void linkLogToTest(String fileName) {
		linkLogToTest(fileName, "");
	}
	
	public void linkLogToTest(String fileName, String prefix) {
		File[] logFileArr = findlogFile(fileName, prefix);
		if (logFileArr == null) {
			System.err.printf("[%s]: Couldn't find file (%s, prefix=%s).\n", name, fileName, prefix);
			return;
		}
		
		for (File logFile : logFileArr) 
			try {
				System.out.printf("[%s]: Processing log file %s for linking.\n", name, logFile.getAbsolutePath());
				
				if (logFile.getAbsolutePath().contains(logger.getReporter().getCurrentTestFolder())) {
					System.out.printf("[%s]: Found the log file on test folder. linking log file %s.\n", name, logFile.getAbsolutePath());
					logger.getReporter().addLink(logFile.getName(), logFile.getName());
				}
				else {
					System.out.printf("[%s]: Didn't find the log file %s on test folder. copying and linking log file.\n", name, logFile.getAbsolutePath());
					ReporterHelper.copyFileToReporterAndAddLink(logger.getReporter(), logFile, logFile.getName());
				}
		
			} catch (Exception e) {
				System.err.printf("[%s]: Error linking log file %s to test folder.\n", name, logFile.getAbsolutePath());
				e.printStackTrace();
			}
		
	}
	
	private File[] findlogFile(String fileName, String prefix) {
		ArrayList<File> logList = new ArrayList<File>();
		for (Enumeration<File> files =  logFiles.keys(); files.hasMoreElements(); ) {
			File logFile = files.nextElement();
			if (logFile.exists() && logFile.getAbsolutePath().contains(fileName) && logFile.getName().contains(prefix + "_"))
				logList.add(logFile);
		}
		
		return logList.size() != 0 ?logList.toArray(new File[]{}) : null;
	}
}

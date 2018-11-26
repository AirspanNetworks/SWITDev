package testsNG.Actions.Utils;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;

import EnodeB.EnodeB;
import EnodeB.Components.Session.Session;
import EnodeB.Components.XLP.XLP;
import Utils.GeneralUtils;
import jsystem.framework.report.ListenerstManager;
import jsystem.framework.report.Reporter;
import jsystem.framework.report.ReporterHelper;

public class ParallelCommandsThreadEnodeBComponent extends Thread {
    private File logFile;
    private volatile boolean iNeedToRun;
    private final List<String> cmdSet;
    private String componentName;
    private EnodeB enb;
    private int intervalBetweenCommandsSet = 1;
    private int responseTimeout = 10;
    private int waitBetweenCommands = 0;
    private PrintStream ps;
    private Reporter report = ListenerstManager.getInstance();

    /**
     * Parallel Commands Thread EnodeB Component
     *
     * @param cmdSet              - cmd Commands Set
     * @param enb                 - enb
     * @param componentName       - component Name
     * @param responseTimeout     - response Timeout
     * @param waitBetweenCommands - wait Between Commands
     * @throws IOException - IOException
     */
    public ParallelCommandsThreadEnodeBComponent(List<String> cmdSet, EnodeB enb, String componentName, int responseTimeout, int waitBetweenCommands) throws IOException {
        this.cmdSet = cmdSet;
        this.enb = enb;
        this.componentName = componentName;
        this.responseTimeout = responseTimeout;
        this.waitBetweenCommands = waitBetweenCommands;
        setName(componentName + "_ParallelCommandsThread");
        initializeLogger();
    }

	public boolean stopCommands() {
		boolean flag = true;
		iNeedToRun = false;
		try {
			GeneralUtils.printToConsole("waiting for command loop will finish - timeout of 3 minutes");
			this.join(3*60*1000);
		} catch (InterruptedException e) {
			GeneralUtils.printToConsole("Error when waiting for commands thread to finish.");
			e.printStackTrace();
			flag = false;
		}
		GeneralUtils.printToConsole("Closing Print Stream");
		ps.close();

		return flag;
	}

    public void start() {
        iNeedToRun = true;
        super.start();
    }

    private void initializeLogger() throws IOException {
        Date initalizeDateLogFileName = new Date();
        String initDateString = initalizeDateLogFileName.toString();
        initDateString = initDateString.replace(":", "");
        logFile = new File("ParallelCommands_" + componentName + "_" + initDateString + ".log");
        logFile.createNewFile();
        this.ps = new PrintStream(logFile);
    }

    @Override
    public void run() {
        Session parallelCommandsSSHSession = enb.getDefaultSession(componentName);
        GeneralUtils.unSafeSleep(5000);
        while (iNeedToRun) {
            if (parallelCommandsSSHSession.isConnected()) {
                writeDateTologFile();
                for (String cmd : cmdSet) {
                    String ans;
                    String response = XLP.LTE_CLI_PROMPT;
                    if (cmd.equals("ue show rate")) {
                        response = "Cell Total";
                    } else if (cmd.contains("ue show link")) {
                        response = "Legend:";
                    }
                    ans = enb.sendCommandsOnSession(enb.getParallelCommandsPrompt(), cmd, response, responseTimeout);
                    String javaTime = DateTime.now().toString("yyyy-MM-dd HH:mm:ss.SSS");
                    writeObjectToLogFile("*******************************************************************************\n");
                    writeObjectToLogFile("*** " + javaTime + " Starting command " + cmd + " ***\n");
                    writeObjectToLogFile("*******************************************************************************\n");
                    writeObjectToLogFile(ans);
                    //Wait between commands
                    GeneralUtils.unSafeSleep(waitBetweenCommands * 1000);
                }
            } else {
                GeneralUtils.printToConsole("Session is closed, not trying to send data");
            }
            GeneralUtils.unSafeSleep(intervalBetweenCommandsSet * 1000);
        }
        GeneralUtils.printToConsole("closing file");
        ps.close();
    }

    public File getFile() {
        return logFile;
    }

    private void writeObjectToLogFile(String string) {
        ps.print(string);
    }

    private void writeDateTologFile() {
        Date currentTimeForLog = new Date();
        ps.print(currentTimeForLog.toString());
    }

    public void addCommand(String command) {
        this.cmdSet.add(command);
    }

    public boolean moveFileToReporterAndAddLink() {
        try {
            ReporterHelper.copyFileToReporterAndAddLink(report, logFile,
                    "Commands File for " + enb.getXLPName());
        } catch (Exception e) {
            report.report("Exception in ReporterHelper.copyFileToReporterAndAddLink could not attach Command File");
            e.printStackTrace();
            return false;
        }

        if (!logFile.delete()) {
            report.report("Failed to delete the commands file, will attempt to delete again when run ends.");
            logFile.deleteOnExit();
        }
        return true;
    }

    public void localJoin() throws InterruptedException {
        GeneralUtils.printToConsole("Waiting for thread to finish - timeout 3 minutes");
        join(3 * 60 * 1000);
    }
}

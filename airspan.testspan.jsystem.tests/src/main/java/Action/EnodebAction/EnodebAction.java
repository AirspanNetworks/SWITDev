package Action.EnodebAction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import Action.Action;
import EnodeB.Components.Log.Logger;
import Utils.GeneralUtils;
import Utils.ScenarioUtils;
import Utils.WatchDog.WatchDogManager;
import jsystem.framework.report.Reporter;
import testsNG.Actions.AlarmsAndEvents;
import testsNG.Actions.Utils.ParallelCommandsThread;
import EnodeB.EnodeB;

public class EnodebAction extends Action {
	protected List<EnodeB> enbInTest = new ArrayList<EnodeB>();
	protected AlarmsAndEvents alarmsAndEvents = null;
	private static HashMap<String, Integer> scenarioStats;
	private static HashMap<String, Integer> unexpectedInScenario = null;
	private static ParallelCommandsThread syncCommands;
	protected ScenarioUtils scenarioUtils;

	@Override
	public void init() {
		scenarioUtils = ScenarioUtils.getInstance();
		scenarioStats = scenarioUtils.getScenarioStats();
		unexpectedInScenario = scenarioUtils.getUnexpectedRebootInScenario();

		for (EnodeB eNodeB : enbInTest) {
			if (eNodeB == null) {
				report.report("EnodeB is not initialized", Reporter.WARNING);
				return;
			}

			String enbName = eNodeB.getNetspanName();
			if (!unexpectedInScenario.containsKey(enbName)) {
				unexpectedInScenario.put(enbName, 0);
			}

			eNodeB.setDeviceUnderTest(true);
			GeneralUtils.printToConsole(String.format("Creating log files for test for eNondeB %s", eNodeB.getName()));

			Logger[] loggers = eNodeB.getLoggers();
			for (Logger logger : loggers) {
				logger.startLog(String.format("%s_%s", getMethodName(), logger.getParent().getName()));
				logger.setCountErrorBool(true);
			}
		}
		super.init();
	}

	@Override
	public void end() {
		String coreFilesPath = "";
		WatchDogManager.getInstance().shutDown();
		scenarioUtils.calledOnceInEndFunc();
		boolean isCoreOccurDuringTest = false;

		for (EnodeB eNodeB : enbInTest) {
			report.addProperty(eNodeB.getNetspanName() + "_Version", eNodeB.getRunningVersion());
			closeLogsFiles(eNodeB);
			setUnexpectedRebootStatistics(eNodeB);
			if (eNodeB.isMACtoPHYEnabled()) {
				eNodeB.disableMACtoPHYcapture();
			}
			eNodeB.showMACtoPHYCaptureFiles();
			HashSet<String> coreSet = eNodeB.getCorePathList();
			int coreIndex = 1;
			String coreValue = "";
			for (String corePath : coreSet) {
				coreValue += (coreIndex + "," + eNodeB.getName() + "," + corePath
						+ ScenarioUtils.SYSOBJ_STRING_DELIMITER);
				coreIndex++;
				coreFilesPath += coreValue;
			}
			isCoreOccurDuringTest = (isCoreOccurDuringTest || eNodeB.isStateChangedToCoreDump());

			// Initialize all the Parameters that refer each test individually.
			eNodeB.clearTestParameters();
			eNodeB.setDeviceUnderTest(false);

		}

		if (coreFilesPath != "" && coreFilesPath != null) {
			report.addProperty("CoreFiles", coreFilesPath);
		}

		if (scenarioStats != null && !scenarioStats.isEmpty()) {
			String logCounter = "";
			for (String key : scenarioStats.keySet()) {
				logCounter += (key + "," + scenarioStats.get(key) + ScenarioUtils.SYSOBJ_STRING_DELIMITER);
			}
			report.setContainerProperties(0, "LogCounter", logCounter);
		}
		GeneralUtils.printToConsole(scenarioUtils.getMemoryConsumption());
		super.end();
	}

	private void closeLogsFiles(EnodeB eNodeB) {
		Logger[] loggers = eNodeB.getLoggers();
		log.info(String.format("Closing log files for test for eNondeB %s", eNodeB.getName()));
//		closeAndGenerateEnBLogFiles(eNodeB, loggers);
		generateAutoLogs(eNodeB, loggers);
	}

	private void generateAutoLogs(EnodeB eNodeB, Logger[] loggers) {
		GeneralUtils.startLevel(String.format("eNodeB %s Automation logs", eNodeB.getName()));
		for (Logger logger : loggers) {
			logger.closeAutoLog(String.format("%s_%s", getMethodName(), logger.getParent().getName()));
		}
		GeneralUtils.stopLevel();
	}

	private void closeAndGenerateEnBLogFiles(EnodeB eNodeB, Logger[] loggers) {
		GeneralUtils.startLevel(String.format("eNodeB %s logs", eNodeB.getName()));
		for (Logger logger : loggers) {
			logger.setCountErrorBool(false);
			logger.closeEnodeBLog(String.format("%s_%s", getMethodName(), logger.getParent().getName()));
			scenarioUtils.scenarioStatistics(logger, eNodeB);
		}
		GeneralUtils.stopLevel();
	}

	public void setUnexpectedRebootStatistics(EnodeB eNodeB) {
		int value = eNodeB.getUnexpectedReboot();
		eNodeB.setUnexpectedReboot(0);
		if (value > 1) {
				report.report(
						"Number of unexpected reboots in test for EnodeB " + eNodeB.getName() + " was more than 1",
						Reporter.FAIL);
			}
		String key = eNodeB.getNetspanName();
		value += unexpectedInScenario.get(key);
		unexpectedInScenario.put(key, value);
		if (value != 0) {
			GeneralUtils.printToConsole(
					"----added unexpected reboots to scenario statistics----" + eNodeB.getNetspanName() + "----");
			String keyProperty = eNodeB.getName() + "," + "Unexpected Reboots" + "," + "WARNING";
			scenarioStats.put(keyProperty, value);
		}
	}

	public String getSeverity(String key) {
		String[] sev = key.split("@");
		String severity = "";
		if (sev != null && sev.length == 2) {
			severity = sev[1];
		} else {
			severity = "ERROR";
		}
		return severity;
	}

	public String getErrorExpression(String key) {
		String[] exp = key.split("@");
		String expression = "";
		if (exp != null && exp.length == 2) {
			expression = exp[0];
		} else {
			expression = "NO ERROR FOUND";
		}
		return expression;
	}

	public String getMibSNMP(EnodeB node, String oid) {
		String response;
		response = node.getSNMP(oid);
		return response;
	}

	/***************** Parallel Commands ********************/
	protected void startingParallelCommands(EnodeB dut, List<String> commandList,int responseTimeout, int waitBetweenCommands) throws IOException{
		syncCommands = new ParallelCommandsThread(commandList, dut, null, null,responseTimeout, waitBetweenCommands);
		syncCommands.start();
	}

	protected boolean stoppingParallelCommands() {
		boolean flag = true;
		report.report("Stopping Parallel Commands");
		flag &= syncCommands.stopCommands();
		flag &= syncCommands.moveFileToReporterAndAddLink();
		return flag;
	}
}
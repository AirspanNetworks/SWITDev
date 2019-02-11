package Utils;

import java.util.ArrayList;
import java.util.List;

import jsystem.framework.report.ListenerstManager;
import jsystem.framework.report.Reporter;
import jsystem.framework.system.SystemManagerImpl;
import jsystem.framework.system.SystemObject;
import jsystem.framework.system.SystemObjectManager;
import junit.framework.Assert;

public class SysObjUtils {

	private static SysObjUtils instance;
	private static Object InstanceLock = new Object();

	public static Reporter report = ListenerstManager.getInstance();
	public SystemObjectManager system = SystemManagerImpl.getInstance();

	public static final String SYSOBJ_STRING_DELIMITER = ";";
	private static String initializedSystemObjects = "";

	private SysObjUtils() {
	}

	public static SysObjUtils getInstnce() {
		synchronized (InstanceLock) {
			if (instance == null)
				instance = new SysObjUtils();
		}
		return instance;
	}

	/**
	 * Initializes multiple system objects of the same class and returns a list
	 * of them.
	 * 
	 * @param sysObjClass
	 *            The system object class
	 * 
	 * 
	 * @param sysObjNames
	 *            The names of the system objects the function will initialize.
	 * 
	 * @return a list of system objects casted to the class specified in the
	 *         <tt>sysObjClass</tt> field.
	 *
	 */
	public <T extends SystemObject> List<T> initSystemObject(Class<T> sysObjClass, String... sysObjNames)
			throws Exception {
		return initSystemObject(sysObjClass, false, sysObjNames);
	}

	/**
	 * Initializes multiple system objects of the same class and returns a list
	 * of them.
	 * 
	 * @param sysObjClass
	 *            The system object class
	 * 
	 * @param ignoreFailures
	 *            When a system object fails to initialize the method will
	 *            ignore it and won't add it to the list.
	 * 
	 * @param sysObjNames
	 *            The names of the system objects the function will initialize.
	 * 
	 * @return a list of system objects casted to the class specified in the
	 *         <tt>sysObjClass</tt> field.
	 * 
	 */
	@SuppressWarnings("unchecked")
	public <T extends SystemObject> List<T> initSystemObject(Class<T> sysObjClass, boolean ignoreFailures,
			String... sysObjNames) {
		// Check if we have system objects to initialize.
		if (sysObjNames == null || sysObjNames.length == 0) {
			// report.report(String.format("There are no %ss defined in test.
			// Failing test.", sysObjClass.getSimpleName()), Reporter.FAIL);
			// Assert.fail();
		}

		try {
			ArrayList<T> sysObjArrayList = new ArrayList<T>();
			for (String sysObjName : sysObjNames) {
				sysObjName = sysObjName.trim();
				// Check if the system object was already initialized.
				if (!initializedSystemObjects.contains(sysObjName)) {
					initializedSystemObjects += sysObjName + SYSOBJ_STRING_DELIMITER;
					GeneralUtils.printToConsole(String.format("Initializing \"%s\"", sysObjName));
				}

				try {
					SystemObject sysObj = system.getSystemObject(sysObjName);
					if (sysObjClass.isAssignableFrom(sysObj.getClass()))
						sysObjArrayList.add((T) system.getSystemObject(sysObjName));
					else {
						report.report(
								String.format(
										"You are trying to initialize \"%s\" (%s) as \"%s\" but they are not related. Failing test.",
										sysObj.getClass().getSimpleName(), sysObjName, sysObjClass.getSimpleName()),
								Reporter.FAIL);
						Assert.fail();
					}
				} catch (Exception e) {
					report.report(String.format(
							"Failed to initialize \"%s\". Check to see if the system object exists on the SUT.",
							sysObjName), e);
					e.printStackTrace();

					if (ignoreFailures) {
						// remove object from the initialized system objects
						// string
						sysObjName += SYSOBJ_STRING_DELIMITER;
						initializedSystemObjects = initializedSystemObjects.replaceAll(sysObjName, "");
					} else {
						Assert.fail("check initialization level to see why the test failed");
					}
				}
			}
			return sysObjArrayList;
		} catch (Exception e) {
			report.report("Error initializing the test system objects", e);
		}

		return null;
	}

	/**
	 * Close system objects.
	 *
	 * @param systemObjects
	 *            the system objects
	 * @throws Exception
	 *             the exception
	 */
	protected void closeSystemObjects(SystemObject... systemObjects) throws Exception {
		for (SystemObject sysObj : systemObjects) {
			String sysObjName = sysObj.getName();
			if (initializedSystemObjects.contains(sysObjName)) {
				report.report(String.format("removing %s %s", sysObj.getClass().getSimpleName(), sysObjName));
				system.getSystemObject(sysObjName).close();
				system.removeSystemObject(sysObj);
				sysObjName += SYSOBJ_STRING_DELIMITER;
				initializedSystemObjects = initializedSystemObjects.replaceAll(sysObjName, "");
			}
		}
	}
}
package UE;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.remote.DesiredCapabilities;

import Utils.GeneralUtils;
import Utils.Android.PackageManager;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.remote.MobileCapabilityType;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;
import io.appium.java_client.service.local.flags.GeneralServerFlag;
import jsystem.framework.report.Reporter;
import se.vidstige.jadb.JadbConnection;
import se.vidstige.jadb.JadbDevice;

public class AndroidUE extends UE {
	private static final String APK_DIR = System.getProperty("user.dir") + File.separator + "resources" + File.separator
			+ "apks";
	private JadbConnection jadb;
	private JadbDevice device;
	private AndroidDriver<?> driver;
	private String deviceId;

	public AndroidUE() {
		super("AndroidUE");
	}

	@Override
	public void init() {
		try {
			super.init();
			jadb = new JadbConnection();
			connect();
		} catch (Exception e) {
			report.report("Failed to initialize Android UE " + deviceId, Reporter.FAIL);
			e.printStackTrace();
		}
	}

	private void connect() throws Exception {
		boolean found = false;
		if (jadb.getDevices().size() >= 1) {
			for (JadbDevice dev : jadb.getDevices()) {
				if (dev.getSerial().equals(deviceId)) {
					device = dev;
					found = true;
				}
			}
		}
		if (!found) {
			report.report("Couldn't find Android Device.\nPlease make sure device connected properly.", Reporter.FAIL);
			return;
		}
		installApps();
		String androidRelease = IOUtils.toString(device.executeShell("getprop ro.build.version.release"),
				StandardCharsets.UTF_8);
		AppiumServiceBuilder builder = new AppiumServiceBuilder().usingAnyFreePort();
		builder.withArgument(GeneralServerFlag.LOG_LEVEL, "warn");
		AppiumDriverLocalService service = AppiumDriverLocalService.buildService(builder);
		service.start();
		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities.setCapability(MobileCapabilityType.PLATFORM_NAME, "Android");
		capabilities.setCapability(MobileCapabilityType.PLATFORM_VERSION, androidRelease);
		capabilities.setCapability(MobileCapabilityType.DEVICE_NAME, "Android-" + this.getName());
		capabilities.setCapability(MobileCapabilityType.UDID, device.getSerial());
		capabilities.setCapability(MobileCapabilityType.APP, new File(APK_DIR, "app.apk").getAbsolutePath());
		capabilities.setCapability("newCommandTimeout", "0");
		driver = new AndroidDriver<>(service, capabilities);
		driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
		driver.findElement(By.id("cancel_button")).click();
	}

	private void installApps() throws Exception {
		String installedApps = IOUtils.toString(device.executeShell("pm list packages |grep iperf | wc -l"),
				StandardCharsets.UTF_8);
		if (!installedApps.contains("2")) {
			PackageManager pm = new PackageManager(device);
			pm.install(new File(APK_DIR, "iPerfForAndroid.apk"));
			pm.install(new File(APK_DIR, "MagiciPerf.apk"));
		}
	}

	@Override
	public boolean stop() {
		try {
			report.report("Stop android UE");
			device.executeShell("settings put global airplane_mode_on 1");
		} catch (Exception e) {
			report.report("can't send command via adb");
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public boolean start() {
		try {
			report.report("Start android UE");
			device.executeShell("settings put global airplane_mode_on 0");
		} catch (Exception e) {
			report.report("can't send command via adb");
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public boolean reboot() {
		boolean flag = true;
		try {
			report.report("Restart android UE");
			device.executeShell("settings put global airplane_mode_on 1");
			flag &= GeneralUtils.unSafeSleep(5000);
			device.executeShell("settings put global airplane_mode_on 0");
		} catch (Exception e) {
			report.report("can't send command via adb");
			e.printStackTrace();
			flag = false;
		}
		return flag;
	}

	@Override
	public String getVersion() {
		report.report("Could not perform getVersion method, AndroidUe does not support SNMP", Reporter.WARNING);
		return GeneralUtils.ERROR_VALUE + "";
	}

	@Override
	public String getDuplexMode() {
		report.report("Could not perform getDuplexMode method, AndroidUe does not support SNMP", Reporter.WARNING);
		return GeneralUtils.ERROR_VALUE + "";
	}

	@Override
	public String getUEStatus() {
		report.report("Could not perform getUEStatus method, AndroidUe does not support SNMP", Reporter.WARNING);
		return GeneralUtils.ERROR_VALUE + "";
	}

	@Override
	public String getUEDlFrequency() {
		report.report("Could not perform getUEDlFrequency method, AndroidUe does not support SNMP", Reporter.WARNING);
		return GeneralUtils.ERROR_VALUE + "";
	}

	@Override
	public String getUEUlFrequency() {
		report.report("Could not perform getUEUlFrequency method, AndroidUe does not support SNMP", Reporter.WARNING);
		return GeneralUtils.ERROR_VALUE + "";
	}

	@Override
	public String getBandWidth() {
		report.report("Could not perform getBandWidth method, AndroidUe does not support SNMP", Reporter.WARNING);
		return GeneralUtils.ERROR_VALUE + "";
	}

	@Override
	public int getRSRP(int index) {
		report.report("Could not perform getRSRP method, AndroidUe does not support SNMP", Reporter.WARNING);
		return GeneralUtils.ERROR_VALUE;
	}

	@Override
	public int getPCI() {
		report.report("Could not perform getPCI method, AndroidUe does not support SNMP", Reporter.WARNING);
		return GeneralUtils.ERROR_VALUE;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public JadbDevice getDevice() {
		return device;
	}

	public AndroidDriver<?> getDriver() {
		return driver;
	}

	@Override
	public boolean setAPN(String apnName) {
		report.report("Could not perform setAPN method, AndroidUE does not support SNMP", Reporter.WARNING);
		return false;
	}
}

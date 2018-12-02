package Utils.Iperf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Entities.ITrafficGenerator.Protocol;
import Entities.ITrafficGenerator.TransmitDirection;
import UE.AndroidUE;
import Utils.GeneralUtils;
import Utils.Pair;
import io.appium.java_client.MobileElement;
import io.appium.java_client.android.Activity;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidKeyCode;

public class AndroidIPerf extends UEIPerf {

	private AndroidUE androidUe;
	private AndroidDriver<?> driver;

	// update Global Params
	private final Object lock = new Object();
	private MobileElement tptEl = null;
	private MobileElement clearEl = null;
	private int lastLength = 0;
	private IPerfStream ulIPerfStream;
	private IPerfStream dlIPerfStream;
	ArrayList<Long> dlCounters = null;

	public AndroidIPerf(AndroidUE ue, IPerfMachine iperfMachineDL, IPerfMachine iperfMachineUL, double ulPortLoad,
			double dlPortLoad, int frameSize, ArrayList<Character> qciList, Protocol protocol,TransmitDirection direction,Integer runTime) throws IOException, InterruptedException {
		super(ue, iperfMachineDL, iperfMachineUL, ulPortLoad, dlPortLoad, frameSize, qciList,protocol,direction,runTime);
		androidUe = ue;
		driver = androidUe.getDriver();
		ulIPerfStream = null;
		dlIPerfStream = null;
	}

	@Override
	public void runTrafficULClient(long startTime) {
		// Android support one iperf stream
		this.ulIPerfStream = ulStreamArrayList.get(0);
		boolean gotActiveStream = false;
		for (IPerfStream ulIPerfStreamIte : ulStreamArrayList) {
			if(!gotActiveStream){
				if (ulIPerfStreamIte.isActive()) {
					this.ulIPerfStream = ulIPerfStreamIte;
					gotActiveStream = true;
				}
			}else{
				ulIPerfStreamIte.setActive(false);
			}
		}
		if(this.ulIPerfStream.isActive()){
			Activity iperfForAndroid = new Activity("com.magicandroidapps.iperf", "com.magicandroidapps.iperf.iperf");
			driver.startActivity(iperfForAndroid);
			MobileElement el = (MobileElement) driver.findElementById("com.magicandroidapps.iperf:id/cmdlineargs");
			el.clear();
			el.sendKeys(this.ulIPerfStream.getIperfClientCommand());
			el = (MobileElement) driver.findElementById("com.magicandroidapps.iperf:id/startstopButton");
			el.click();
			System.out.println("Start sending UL traffic: " + this.ulIPerfStream.getIperfClientCommand());
			driver.pressKeyCode(AndroidKeyCode.HOME);
		}
	}

	@Override
	public void startDLListener() {
		// Android support one iperf stream
		this.dlIPerfStream = dlStreamArrayList.get(0);
		boolean gotActiveStream = false;
		for (IPerfStream dlIPerfStreamIte : dlStreamArrayList) {
			if(!gotActiveStream){
				if (dlIPerfStreamIte.isActive()) {
					this.dlIPerfStream = dlIPerfStreamIte;
					gotActiveStream = true;
				}
			}else{
				dlIPerfStreamIte.setActive(false);
			}
		}
		if(this.dlIPerfStream.isActive()){
			GeneralUtils.printToConsole("Start listening to DL: " + this.dlIPerfStream.getIperfServerCommand());
			Activity iperfForAndroid = new Activity("com.nextdoordeveloper.miperf.miperf",
					"com.nextdoordeveloper.miperf.miperf.MainActivity");
			driver.startActivity(iperfForAndroid);
			MobileElement el = (MobileElement) driver.findElementById("com.nextdoordeveloper.miperf.miperf:id/etCommand");
			el.clear();
			el.sendKeys(this.dlIPerfStream.getIperfServerCommand());
			el = (MobileElement) driver.findElementById("com.nextdoordeveloper.miperf.miperf:id/swStart");
			el.click();
			tptEl = (MobileElement) driver.findElementById("com.nextdoordeveloper.miperf.miperf:id/tvResult");
			clearEl = (MobileElement) driver.findElementByAccessibilityId("More options");
		}
	}
	
	@Override
	protected void updateDLCounters(String dlCountersStr) {// Android support one iperf stream
		String tptText;
		int minNumberOfSamples = IPerfMachine.getMinNumberOfSamples();
		if(this.dlIPerfStream.isActive()){
			initArray();
			if (isStarted()) {
				try {
					synchronized (lock) {
						tptText = tptEl.getAttribute("text");
					}
					if (tptText.length() > lastLength) {
						lastLength = tptText.length();
						for(int i = 0; i < minNumberOfSamples; i++){
							tptText = tptText.substring(0, tptText.lastIndexOf("\n"));
							String lastLine = tptText.substring(tptText.lastIndexOf("\n") == -1 ? 0 : tptText.lastIndexOf("\n"));
							if(lastLine.contains("datagrams")){
								i--;
								continue;
							}
							Pattern pattern = Pattern.compile("(\\d*\\.?\\d+) +Kbits\\/sec");
							Matcher matcher = pattern.matcher(lastLine);
							if (matcher.find()){
								dlCounters.add((long)(1000 * Float.parseFloat(matcher.group(1))));
							}else{
								if(dlCounters.isEmpty()){
									dlCounters.add((long) 0);
								}
								break;
							}
						}
						if (tptText.length() > 10000)
							clearOutput();
					} else if (lastLength == 0) {
						initArray();
						clearOutput();
					} else
						lastLength = 0;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			dlIPerfStream.setCountersInBits(new Pair<Double, ArrayList<Long>>(0.0, dlCounters));
			//Save Minimum number of samples.
			if(dlCounters.size() < minNumberOfSamples){
				IPerfMachine.setMinNumberOfSamples(dlCounters.size());
			}
		}
	}

	private void initArray(){
		dlCounters = new ArrayList<>(Collections.nCopies(IPerfMachine.getMinNumberOfSamples(), (long) 0));
	}
	
	public void clearOutput() {
		new Thread(new Runnable() {
			public void run() {
				try {
					synchronized (lock) {
						clearEl.click();
						clearEl = (MobileElement) driver
								.findElementByXPath("//android.widget.TextView[@text='Clear screen']");
						clearEl.click();
					}
					clearEl = (MobileElement) driver.findElementByAccessibilityId("More options");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	private boolean isStarted() {
		if (tptEl == null) {
			GeneralUtils.printToConsole("Waiting for Android traffic to start");
			long start = System.currentTimeMillis();
			while (tptEl == null && System.currentTimeMillis() - start < 20000) {
				GeneralUtils.unSafeSleep(2000);
			}
		}
		return tptEl != null;
	}

	@Override
	public void stopUlTraffic() {
		String stop = "pkill iperf\n";
		GeneralUtils.printToConsole("Stop traffic: " + stop);
		tptEl = null;
		GeneralUtils.unSafeSleep(2000);
		try {
			androidUe.getDevice().executeShell("pm clear com.nextdoordeveloper.miperf.miperf");
			androidUe.getDevice().executeShell("pm clear com.magicandroidapps.iperf");
			androidUe.getDevice().executeShell("pm clear com.example.android.borderlessbuttons");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
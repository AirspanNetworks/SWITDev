package EnodeB;

import TestingServices.TestConfig;
import Utils.GeneralUtils;
import jsystem.framework.report.Reporter;

public class AirSynergy2000 extends EnodeBWithDAN {

	public AirSynergy2000() {
	}

	/**
	 * @author sshahaf
	 * enable Mac To Phy by setting values in debugFlags [*] enableSniffer
	 * remote will add a MAC from SUT and local will not include MAC so files will be on Node.
	 */
	@Override
	public void enableMACtoPHYcapture(SnifferFileLocation fileLocation) {
		String enableSnifferSuffix = "";

		if(fileLocation == SnifferFileLocation.Remote){
			snifferMAC = TestConfig.getInstace().getSnifferMAC();	
			if(snifferMAC == null){
				report.report("there is no MAC in SUT Section",Reporter.WARNING);
				return;
			}
			enableSnifferSuffix =" SnfMac=" + snifferMAC;
		}
		
		if (isMACtoPHYEnabled()) {
			report.report(getName() + " - Enable sniffer.");
			String enableSnifferCommand = "db set debugFlags [*] enableSniffer=1" + enableSnifferSuffix;
			GeneralUtils.printToConsole("Sending Command to node : "+enableSnifferCommand);
			String resSet = lteCli(enableSnifferCommand);
			GeneralUtils.printToConsole(resSet);
			
			//Validation
			String resGet = lteCli("db get debugFlags [*] enableSniffer,SnfMac");
			GeneralUtils.printToConsole(resGet);
			if (!resSet.contains("entries updated")) {
				GeneralUtils.printToConsole("Enabling sniffer might failed");
				GeneralUtils.printToConsole(resGet);
			}
		}
	}

	/**
	 * @author sshahaf
	 */
	@Override
	public void disableMACtoPHYcapture() {
		report.report("disable sniffer");
		GeneralUtils.printToConsole("XLP System Enable Sniffer");
		String disableSniffer = "db set debugFlags [*] enableSniffer=0";
		String resSet = lteCli(disableSniffer);
		System.out.print(resSet);
		String resGet = lteCli("db get debugFlags [*] enableSniffer");
		System.out.print(resGet);
		if (!resSet.contains("entries updated")) {
			try {
				report.startLevel("Disabling sniffer might failed");
				report.report(resGet);
				report.stopLevel();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}

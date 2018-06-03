
package Utils;

import java.util.ArrayList;
import java.util.stream.Collectors;

import EnodeB.EnodeB;
import TestingServices.TestConfig;
import UE.UE;
import UE.UESimulator;
import UeSimulator.Amarisoft.AmariSoftServer;
import jsystem.framework.report.ListenerstManager;
import jsystem.framework.report.Reporter;

public class SetupUtils {

	private static SetupUtils instance;
	private static Object insnceLock = new Object();
	public static Reporter report = ListenerstManager.getInstance();

	private SetupUtils() {

	}

	public static SetupUtils getInstance() {
		synchronized (insnceLock) {
			if (instance == null)
				instance = new SetupUtils();
		}
		return instance;
	}

	/**
	 * @author Shahaf Shuhamy
	 * @return all the UES from SUT that are Category 6, empty list if there are
	 *         no UES like that
	 */
	public ArrayList<UE> getCat6UEs() {
		ArrayList<UE> allUEs = new ArrayList<UE>();
		try {
			allUEs = getAllUEs();
		} catch (Exception e) {
			e.printStackTrace();
		}

		ArrayList<UE> returnResultUes = new ArrayList<UE>();
		for (UE ue : allUEs) {
			if (ue.getUeCategory() == 6) {
				returnResultUes.add(ue);
			}
		}
		return returnResultUes;
	}

	public ArrayList<UE> getStaticUEs() {
		ArrayList<UE> temp = new ArrayList<UE>();
		String[] staticUesArray = TestConfig.getInstace().getStaticUEs();
 		if (staticUesArray != null) {
 			for (int i = 0; i < staticUesArray.length; i++) {
				if (staticUesArray[i].toLowerCase().equals(AmariSoftServer.amarisoftIdentifier)) {
					try {
						AmariSoftServer uesim = AmariSoftServer.getInstance();
						temp.addAll(uesim.getUeList());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				else
					temp.add((UE) SysObjUtils.getInstnce().initSystemObject(UE.class, false,staticUesArray[i]));
			}
		}
		return temp;
	}

	public ArrayList<UE> getStaticUEs(EnodeB enb) {
		ArrayList<UE> temp = new ArrayList<UE>();
		String[] staticUesArray = enb.getStaticUes();		
 		if (staticUesArray != null) {
 			for (int i = 0; i < staticUesArray.length; i++) {
				if (staticUesArray[i].toLowerCase().equals(AmariSoftServer.amarisoftIdentifier)) {
					try {
						AmariSoftServer uesim = AmariSoftServer.getInstance();
						temp.addAll(uesim.getUeList());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				else
					temp.add((UE) SysObjUtils.getInstnce().initSystemObject(UE.class, false,staticUesArray[i]));
			}
		}
		return temp;
	}

	public ArrayList<UE> getallUEsPerEnodeb(EnodeB enb) {
		ArrayList<UE> temp = new ArrayList<UE>();
		try {
			temp.addAll(getDynamicUEs());
			temp.addAll(getStaticUEs(enb));
		} catch (Exception e) {
			report.report("Couldn't get UEs list");
			e.printStackTrace();
		}
		return temp;
	}

	public ArrayList<UE> getCat6UEsPerEnodeB(EnodeB dut) {
		return (ArrayList<UE>) getallUEsPerEnodeb(dut).stream().filter(ue -> ue.getUeCategory() == 6).collect(Collectors.toList());
		
	}

	public ArrayList<UE> getAllUEs() {
		ArrayList<UE> temp = new ArrayList<UE>();
		temp.addAll(getStaticUEs());
		temp.addAll(getDynamicUEs());
		return temp;
	}

	public ArrayList<UE> getDynamicUEs() {
		ArrayList<UE> temp = new ArrayList<UE>();
		String[] dynamicUEs = TestConfig.getInstace().getDynamicUEs();
		if (dynamicUEs != null) {
			for (int i = 0; i < dynamicUEs.length; i++) {
				if (dynamicUEs[i].toLowerCase().equals("uesimulator")) {
					UESimulator uesim;
					try {
						uesim = UESimulator.getInstance();
						temp.addAll(uesim.getUEs());
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else if(dynamicUEs[i].toLowerCase().equals(AmariSoftServer.amarisoftIdentifier)){
					try {
						AmariSoftServer uesim = AmariSoftServer.getInstance();
						temp.addAll(uesim.getUeList());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				else
					temp.add((UE) SysObjUtils.getInstnce().initSystemObject(UE.class, false, dynamicUEs[i]));
			}
		}
		return temp;
	}

	public ArrayList<EnodeB> getAllEnb() {
		return (ArrayList<EnodeB>) SysObjUtils.getInstnce().initSystemObject(EnodeB.class, false,
				TestConfig.getInstace().getNodes());
	}
}
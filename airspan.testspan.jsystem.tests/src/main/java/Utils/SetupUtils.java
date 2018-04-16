
package Utils;

import java.util.ArrayList;
import java.util.stream.Collectors;

import EnodeB.EnodeB;
import TestingServices.TestConfig;
import UE.UE;
import UE.UESimulator;
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
		if (TestConfig.getInstace().getStaticUEs() != null) {
			temp.addAll((ArrayList<UE>) SysObjUtils.getInstnce().initSystemObject(UE.class, false,
					TestConfig.getInstace().getStaticUEs()));
		}
		return temp;
	}

	public ArrayList<UE> getStaticUEs(EnodeB enb) {
		ArrayList<UE> temp = new ArrayList<UE>();
		if (enb.getStaticUes() != null) {
			temp.addAll((ArrayList<UE>) SysObjUtils.getInstnce().initSystemObject(UE.class, false, enb.getStaticUes()));
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
			if (dynamicUEs[0].toLowerCase().equals("uesimulator")) {
				UESimulator uesim;
				try {
					uesim = UESimulator.getInstance();
					temp.addAll(uesim.getUEs());
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				temp.addAll((ArrayList<UE>) SysObjUtils.getInstnce().initSystemObject(UE.class, false, dynamicUEs));
			}
		}
		return temp;
	}

	public ArrayList<EnodeB> getAllEnb() {
		return (ArrayList<EnodeB>) SysObjUtils.getInstnce().initSystemObject(EnodeB.class, false,
				TestConfig.getInstace().getNodes());
	}
}
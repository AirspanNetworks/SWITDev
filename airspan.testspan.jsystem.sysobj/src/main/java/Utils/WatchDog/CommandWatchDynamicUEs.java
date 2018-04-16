package Utils.WatchDog;

import java.text.DecimalFormat;
import java.util.ArrayList;

import DMTool.DMtool;
import DMTool.sqnCellMeasReport;
import EPC.EPC;
import EnodeB.EnodeB;
import UE.UE;
import Utils.GeneralUtils;
import Utils.StreamList;

public class CommandWatchDynamicUEs extends Command {
	private ArrayList<EnodeB> nodesList = new ArrayList<EnodeB>();
	private ArrayList<UE> allUEsInTest = new ArrayList<UE>();
	private ArrayList<String> headLines = new ArrayList<String>();
	private ArrayList<String> dataLines;
	private ArrayList<String> rsrpHeadLines = new ArrayList<String>();
	private ArrayList<String> rsrpDataLines;
	private StreamList tablePrinter = new StreamList();
	private StreamList rsrpTablePrinter = new StreamList();
	private int sourcePci;
	private EPC epc;
	private ArrayList<DMtool> dmToolList = new ArrayList<DMtool>();

	public CommandWatchDynamicUEs(ArrayList<EnodeB> nodes, EPC epc, ArrayList<UE> ues) {
		this.nodesList = nodes;
		this.epc = epc;
		this.allUEsInTest = ues;
		headLines.add("number Of UEs");
		headLines.add("percentage");
		rsrpHeadLines.add("ue ip");
		sourcePci = nodes.get(0).getPci();
		int destPci = nodes.get(1).getPci();
		rsrpHeadLines.add("Rsrp " + sourcePci);
		rsrpHeadLines.add("Rsrp " + destPci);
		initDmTool(ues);
	}

	private void initDmTool(ArrayList<UE> ues) {
		DMtool dmTool;
		for (UE ue : ues) {
			dmTool = new DMtool();
			dmTool.setUeIP(ue.getLanIpAddress());
			dmTool.setPORT(ue.getDMToolPort());
			dmTool.init(5000);
			dmToolList.add(dmTool);
		}

	}

	@Override
	public void run() {
		int UEsFromEPCCommand = 0;
		String timeStr = GeneralUtils.timeFormat(System.currentTimeMillis());

		for (EnodeB node : nodesList) {
			try {
				UEsFromEPCCommand += epc.checkNumberOfConnectedUEs(this.allUEsInTest, node, false);
			} catch (Exception e) {
				e.printStackTrace();
				GeneralUtils.printToConsole("Exception while trying to get command from EPC");
			}
		}

		// add time stamp for this sample
		dataLines = new ArrayList<String>();
		dataLines.add(String.valueOf(UEsFromEPCCommand));

		double precentOfDynamicUEs = (((double) UEsFromEPCCommand) / ((double) allUEsInTest.size())) * 100.0;
		String precentOfUEs = new DecimalFormat("##.##").format(precentOfDynamicUEs) + "%";

		dataLines.add(precentOfUEs);
		tablePrinter.addValues("UEs statistics:", timeStr, headLines, dataLines);
		addRsrpData();
	}

	private void addRsrpData() {
		String timeStr = GeneralUtils.timeFormat(System.currentTimeMillis());
		int rsrp1, rsrp2, pci, i = 0;
		sqnCellMeasReport meas;
		for (DMtool dm : dmToolList) {
			rsrp1 = 0;
			rsrp2 = 0;
			pci = -1;
			i++;
			rsrpDataLines = new ArrayList<String>();
			rsrpDataLines.add(dm.getUeIP());
			try {
				meas = dm.getMeas();
				pci = (int) meas.servingMeas.id.id2;
				if (pci == sourcePci) {
					rsrp1 = meas.servingMeas.meas.meas1;
					if (meas.neigh.length > 0)
						rsrp2 = meas.neigh[0].meas.meas1;
				} else {
					rsrp2 = meas.servingMeas.meas.meas1;
					if (meas.neigh.length > 0)
						rsrp1 = meas.neigh[0].meas.meas1;
				}
			} catch (Exception e) {
				GeneralUtils.printToConsole("Exception while trying to get RSRP.");
				GeneralUtils.printToConsole(
						String.format("ueIp=%s, pci=%s, rsrp1=%s, rsrp2=%s.", dm.getUeIP(), pci, rsrp1, rsrp2));
			}
			rsrpDataLines.add(String.valueOf(rsrp1 / 100.0));
			rsrpDataLines.add(String.valueOf(rsrp2 / 100.0));
			// add time stamp for this sample
			rsrpTablePrinter.addValues("UEs RSRP data:", timeStr + " #" + i, rsrpHeadLines, rsrpDataLines);
		}
	}

	private void closeDm(){
		for(DMtool dm : dmToolList)
			dm.close();
	}
	
	@Override
	public int getExecutionDelaySec() {
		return 5;
	}

	public StreamList getTable() {
		return tablePrinter;
	}

	public StreamList getRsrpTable() {
		closeDm();
		return rsrpTablePrinter;
	}

}

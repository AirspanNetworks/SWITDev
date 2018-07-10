package Utils.WatchDog;

import java.util.ArrayList;

import EnodeB.EnodeB;
import Utils.GeneralUtils;
import Utils.GeneralUtils.HtmlFieldColor;
import Utils.GeneralUtils.HtmlTable;

public class CommandMemoryCPU extends Command {

	private int maxCPU = 0;
	private ArrayList<Integer> cpu = new ArrayList<Integer>();
	private int maxMemory = 0;
	private ArrayList<Integer> memory = new ArrayList<Integer>();
	private EnodeB dut;
	private volatile boolean enabled = true;

	public CommandMemoryCPU(EnodeB node) {
		this.dut = node;
	}

	@Override
	public void run() {
		if (enabled) {
			int meanCPU = dut.getSingleSampleCountersValue("EquipMeasMeanProcUsage");
			if (meanCPU > 0) {
				cpu.add(meanCPU);
			}

			int tempMaxCPU = dut.getSingleSampleCountersValue("EquipMeasMaxProcUsage");
			if (tempMaxCPU > maxCPU) {
				maxCPU = tempMaxCPU;
			}

			int meanMemory = dut.getSingleSampleCountersValue("EquipMemoryUsageMean");
			if (meanMemory > 0) {
				memory.add(meanMemory);
			}

			int tempMaxMemory = dut.getSingleSampleCountersValue("EquipMemoryUsageMax");
			if (tempMaxMemory > maxMemory) {
				maxMemory = tempMaxMemory;
			}
		}

	}

	public String getNodeName() {
		return dut.getName();
	}

	@Override
	public int getExecutionDelaySec() {
		return 60;
	}

	private Double getAvrageFromIntList(ArrayList<Integer> list) {
		Double sum = 0.0;
		for (Integer number : list) {
			sum += number;
		}
		return sum / list.size();
	}

	public void reportHTMLTableWithResults() {
		Double avgCPU = getAvrageFromIntList(cpu);
		Double avgMemory = getAvrageFromIntList(memory);

		buildHTMLTable(avgCPU, avgMemory, maxCPU, maxMemory);
	}

	public ArrayList<Integer> getMemoryValuesList() {
		return memory;
	}

	public ArrayList<Integer> getCpuValuesList() {
		return cpu;
	}

	private void buildHTMLTable(Double avgCPU, Double avgMemory, int maxCPU, int maxMemory) {
		GeneralUtils.HtmlTable table = new HtmlTable();
		// HeadLine
		table.addNewColumn("CPU %");
		table.addNewColumn("Memory %");

		// Average
		table.addNewRow("AVG");
		table.addField(HtmlFieldColor.WHITE, String.format("%.2f", avgCPU));
		table.addField(HtmlFieldColor.WHITE, String.format("%.2f", avgMemory));

		// Maximum
		table.addNewRow("MAX");
		table.addField(HtmlFieldColor.WHITE, String.valueOf(maxCPU));
		table.addField(HtmlFieldColor.WHITE, String.valueOf(maxMemory));

		GeneralUtils.startLevel("CPU and Memory for node : " + dut.getName());
		table.reportTable("");
		GeneralUtils.stopLevel();
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		
	}

}

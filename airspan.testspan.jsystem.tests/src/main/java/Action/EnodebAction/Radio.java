package Action.EnodebAction;

import java.util.ArrayList;

import org.junit.Test;

import EnodeB.EnodeB;
import Netspan.Profiles.RadioParameters;
import Utils.GeneralUtils;
import Utils.SysObjUtils;
import jsystem.framework.ParameterProperties;
import jsystem.framework.TestProperties;
import jsystem.framework.report.Reporter;
import testsNG.Actions.EnodeBConfig;

public class Radio extends EnodebAction {
	protected EnodeB dut;
	protected Integer cellId = 1;
	protected String cloneFromName = null;
	protected Integer earfcn = null;
	protected Integer txPower = null;

	@ParameterProperties(description = "Name of ENB from the SUT")
	public void setDUT(String dut) {
		ArrayList<EnodeB> temp = (ArrayList<EnodeB>) SysObjUtils.getInstnce().initSystemObject(EnodeB.class, false,
				dut.split(","));
		this.dut = temp.get(0);
	}

	@ParameterProperties(description = "Cell ID (Type: int, default=1)")
	public void setCellId(String cellId) {
		try {
			this.cellId = Integer.valueOf(cellId);
		} catch (Exception exc) {
			report.report("Failed to load CellID due to: " + exc.getMessage(), Reporter.FAIL);
			reason = "Failed to load CellID due to: " + exc.getMessage();
		}
	}

	@ParameterProperties(description = "Clone from name")
	public void setCloneFromName(String cloneFromName) {
		this.cloneFromName = cloneFromName;
	}

	@ParameterProperties(description = "Set EARFCN (Type: int)")
	public void setEARFCN(String earfcn) {
		try {
			this.earfcn = Integer.valueOf(earfcn);
		} catch (Exception exc) {
			report.report("Failed to load EARFCN due to: " + exc.getMessage(), Reporter.FAIL);
			reason = "Failed to load EARFCN due to: " + exc.getMessage();
		}
	}

	@ParameterProperties(description = "Set Tx Power (Type: int)")
	public void setTxPower(String txPower) {
		try {
			this.txPower = Integer.valueOf(txPower);
		} catch (Exception exc) {
			report.report("Failed to load TxPower due to: " + exc.getMessage(), Reporter.FAIL);
			reason = "Failed to load TxPower due to: " + exc.getMessage();
		}
	}

	@Test // 1
	@TestProperties(name = "Clone and Set Radio Profile", returnParam = "LastStatus", paramsInclude = { "DUT", "CellId",
			"CloneFromName", "EARFCN", "TxPower" })
	public void cloneAndSetRadioProfile() {
		RadioParameters radioParams = new RadioParameters();
		GeneralUtils.startLevel(this.dut.getName() + " (Cell " + this.cellId + ") Clone and Set Radio Profile");
		report.report("Clone From Name: " + this.cloneFromName);

		if (this.earfcn != null) {
			radioParams.setEarfcn(this.earfcn);
			report.report("Set EARFCN=" + this.earfcn);
		}
		if (this.txPower != null) {
			radioParams.setTxpower(this.txPower);
			report.report("Set Tx Power=" + this.txPower);
		}
		GeneralUtils.stopLevel();

		boolean flag = EnodeBConfig.getInstance().cloneAndSetRadioProfileViaNetSpan(this.dut, this.cloneFromName,
				this.cellId, radioParams);

		if (!flag) {
			report.report("Clone and Set Radio Profile Failed", Reporter.FAIL);
			reason = "Clone and Set Radio Profile Failed";
		}
	}
	
	/**
	 * 
	 * This action does not support channel spacing for CA
	 */
	@Test
	@TestProperties(name = "Change earfcn", returnParam = { "IsTestWasSuccessful" }, paramsInclude = {
			"DUT", "EARFCN" ,"CellId"})
	public void changeEarfcn() {
		EnodeBConfig enodeBConfig = EnodeBConfig.getInstance();
		RadioParameters radioParams = new RadioParameters();
		radioParams.setEarfcn(earfcn);
		report.report("Trying to set Earfcn = " + earfcn + " to node: " + dut.getNetspanName()+
				", to cell id: "+cellId);
		
		int originalContext = dut.getCellContextID();
		dut.setCellContextNumber(cellId);
		if (!enodeBConfig.updateRadioProfile(dut, radioParams)) {
			report.report("Failed to change EARFCN", Reporter.FAIL);
			reason = "Failed to change EARFCN";
		} else {
			report.report("Change done");
		}
		dut.setCellContextNumber(originalContext);
	}
	
	@Override
	public void init() {
		if(enbInTest == null) {
			enbInTest = new ArrayList<EnodeB>();
		}
		if(dut != null){
			enbInTest.add(dut);			
		}
		super.init();
	}
}
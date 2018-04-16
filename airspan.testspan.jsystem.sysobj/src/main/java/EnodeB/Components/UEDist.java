package EnodeB.Components;

import jsystem.framework.system.SystemObjectImpl;

/**
 * UEDistribution represents UEs location in Cells List from SUT.
 * @author sshahaf
 *
 */
public class UEDist extends SystemObjectImpl{

	public Cell[] cells;
	
	@Override
	public void init() throws Exception{
		super.init();
	}
	
	public Cell[] getCellsList(){
		return cells;
	}
	
	public void setCellsList(Cell[] cellsList){
		cells = cellsList;
	}
}

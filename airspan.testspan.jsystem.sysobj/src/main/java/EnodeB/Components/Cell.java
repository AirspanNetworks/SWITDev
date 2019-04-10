package EnodeB.Components;

import java.util.ArrayList;

public class Cell extends EnodeBComponent{
	int id;
	int numberOfUEs;
	String ueListNames;
	ArrayList<String> UEs = new ArrayList<String>();
	
	public void init() throws Exception{
		setName(getParent().getName() + "_Cell");
		super.init();
		setUES(ueListNames);
	}
	
	public int getId(){
		return this.id;
	}
	
	public void setId(int id){
		this.id = id;
	}
	
	public int getNumberOfUEsInCell(){
		return this.numberOfUEs;
	}
	
	public void setnumberOfUEs(int numberOfUEs){
		this.numberOfUEs = numberOfUEs;
	}
	
	public ArrayList<String> getUEsNamesInCell(){
		return this.UEs;
	}
	
	public void setueListNames(String ueListNames){
		this.ueListNames = ueListNames;
	}
	
	public void setUES(String UEsStr){
		if(UEsStr == null){
			return;
		}
		this.ueListNames = UEsStr;
		String[] ueSpliter = this.ueListNames.split(",");
		for(String ueName : ueSpliter){
			UEs.add(ueName);
		}
	}
	
	/**
	 * used for debug: "cell id :<cell id>, cell number of UEs: <numbOfUEs>"
	 * @author Shahaf Shuhamy
	 */
	@Override
	public String toString(){
		return "cell id: "+this.id+", cell numberOfUEs: "+this.numberOfUEs;
	}
	
}

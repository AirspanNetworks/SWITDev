package EnodeB;
/**
 * The Enum EnodeBOperationMode describes the two operation mode of EnodeB.
 * which are TDD and FDD.
 */
public enum EnodeBOperationMode {
	FDD("FDD",1),TDD("TDD",2);
	private String name;
	private int value;
	EnodeBOperationMode(String name,int value)
	{
		this.name=name;
		this.value=value;
	}
	public int getDbIndex()
	{
		return this.value;
	}
	
	public static EnodeBOperationMode getByDbIndx(int dbindex)
	{
		for (EnodeBOperationMode val : values())
			if (val.getDbIndex() == dbindex)
				return val;
		return null;
	}
	public String toString()
	{
		return this.name;
	}
	
}

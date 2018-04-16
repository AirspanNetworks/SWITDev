package EnodeB;

public enum RlcMode {
AM("AM",3),UM("UM",2);
	private String name;
	private int dbValue;
	RlcMode(String name,int dbValue) {
		this.name=name;
		this.dbValue=dbValue;
	}
	
	
	public static RlcMode getByDbValue(int value)
	{
		for (RlcMode val : values())
			if (val.dbValue == value)
				return val;
		return null;
	}
	@Override
	public String toString()
	{
		return this.name;
	}
}

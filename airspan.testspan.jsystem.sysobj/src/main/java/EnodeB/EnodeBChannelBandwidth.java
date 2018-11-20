package EnodeB;

// TODO: Auto-generated Javadoc
/**
 * The Enum EnodeBChannelBandwidth.
 */
public enum EnodeBChannelBandwidth {
	
	/** The Five mhz. */
	FiveMhz(5, 2), 
 /** The Ten mhz. */
 TenMhz(10, 3), 
 /**The Fifteen mhz*/
 FiftennMhz(15,4),
 /** The Twenty mhz. */
 TwentyMhz(20, 5);
	
	/** The bandWitdh. */
	private int bw;
	
	/** The index in db. */
	private int dbIndx;
	
	/**
	 * Instantiates a new channel bandwidth of EnodeB.
	 *
	 * @param bw the bw
	 * @param dbIndx the db indx
	 */
	EnodeBChannelBandwidth(int bw, int dbIndx) {
		this.bw = bw;
		this.dbIndx = dbIndx;
	}

	/**
	 * Gets the bandWidth.
	 *
	 * @return the bw
	 */
	public int getBw() {
		return bw;
	}

	/**
	 * Gets the db index.
	 *
	 * @return the db indx
	 */
	public int getDbIndx() {
		return dbIndx;
	}
	
	/**
	 * Gets the by db indx.
	 *
	 * @param dbIndex the db index
	 * @return the by db indx
	 */
	public static EnodeBChannelBandwidth getByDbIndx(int dbIndex) {
		for (EnodeBChannelBandwidth val : values())
			if (val.dbIndx == dbIndex)
				return val;
		return null;
	}
	public static EnodeBChannelBandwidth getByBwNumber(int num)
	{
		for (EnodeBChannelBandwidth val : values())
			if (val.bw == num)
				return val;
		return null;
	}
	
	@Override
	public String toString() {
		return bw + "Mhz";
	}
	
	
}

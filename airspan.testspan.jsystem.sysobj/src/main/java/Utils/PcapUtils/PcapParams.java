package Utils.PcapUtils;

import Utils.GeneralUtils;

public class PcapParams {

	private String type;
	private String id;
	private Integer frame;
	private int subFrame;
	
	public PcapParams(){
		
	}
	
	public PcapParams(String packetType){
		type = packetType;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Integer getFrame() {
		return frame;
	}

	public void setFrame(Integer frame) {
		this.frame = frame;
	}
	
	/**
	 * Service method since from pcap files its all Strings
	 * @param frame
	 */
	public void setFrame(String frame){
		this.frame = GeneralUtils.hex2decimal(frame);
	}

	public int getSubFrame() {
		return subFrame;
	}

	public void setSubFrame(int subFrame) {
		this.subFrame = subFrame;
	}
	
	/**
	 * Service method since from pcap files its all Strings
	 * @param subFrame
	 */
	public void setSubFrame(String subFrame){
		this.subFrame = GeneralUtils.hex2decimal(subFrame);
	}
	
}

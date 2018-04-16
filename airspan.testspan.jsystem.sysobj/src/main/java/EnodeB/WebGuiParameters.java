package EnodeB;

public enum WebGuiParameters {
		IP_ADDRESS("IpAddress"),SUBNET_MASK("SubnetMask"),DEFAULT_GATE_WAY("DefaultGateway"),
		VLANTagId("VLANTagId"),MANAGEMENTVLAN("ManagementVLAN");
		
		String webParam;
		
		WebGuiParameters(String webGuiParam){
			webParam = webGuiParam;
		}
		
		public String getValue(){
			return webParam;
		}
}

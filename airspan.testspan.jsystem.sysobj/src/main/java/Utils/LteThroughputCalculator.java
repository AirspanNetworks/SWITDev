package Utils;


import org.w3c.dom.Document;

import Netspan.Profiles.RadioParameters;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.*;


public class LteThroughputCalculator {
	private final String rootXml = "XmlLteThroughputCalculator";
	
	public enum FrameSplitEnum{
		FC0DL40UL60("(0)    DL 40% : UL 60%"),
		FC1DL60UL40("(1)    DL  60% : UL 40%"),
		FC2DL80UL20("(2)    DL  80% : UL 20%"),
		FC3DL70UL30("(3)    DL  70% : UL 30%"),
		FC4DL80UL20("(4)    DL  80% : UL 20%"),
		FC5DL90UL10("(5)    DL  90% : UL 10%"),
		FC6DL50UL50("(6)    DL  50% : UL 50%");
		
		public final static String fieldName = "Frame Split";
		private String value;
		FrameSplitEnum(String value){
			this.value = value;
		}
		public static FrameSplitEnum get(String frameConfig) {
			FrameSplitEnum frameSplitEnum = null;
			if(frameConfig != null){
				switch (frameConfig) {
				case "0":
					frameSplitEnum = FrameSplitEnum.FC0DL40UL60;
					break;
				case "1":
					frameSplitEnum = FrameSplitEnum.FC1DL60UL40;				
					break;
				case "2":
					frameSplitEnum = FrameSplitEnum.FC2DL80UL20;
					break;
				case "3":
					frameSplitEnum = FrameSplitEnum.FC3DL70UL30;
					break;
				case "4":
					frameSplitEnum = FrameSplitEnum.FC4DL80UL20;
					break;
				case "5":
					frameSplitEnum = FrameSplitEnum.FC5DL90UL10;
					break;
				case "6":
					frameSplitEnum = FrameSplitEnum.FC6DL50UL50;
					break;
				default:
					break;
				}
			}
			return frameSplitEnum;
		}
		
	}
	public enum DuplexEnum{
		TDD("TDD"),
		FDD("FDD");
		
		public final static String fieldName = "Duplex";
		private String value;
		DuplexEnum(String value){
			this.value = value;
		}
		public static DuplexEnum get(String duplex) {
			DuplexEnum duplexEnum = null;
			if(duplex != null){
				switch (duplex) {
				case "FDD":
					duplexEnum = FDD;
					break;
				case "TDD":
					duplexEnum = TDD;
					break;
				default:
					break;
				}
			}
			return duplexEnum;
		}
	}
	public enum ChannelBandwidthEnum{
		BW1_4("1.4"),
		BW3("3"),
		BW5("5"),
		BW10("10"),
		BW15("15"),
		BW20("20");
		
		public final static String fieldName = "Channel Bandwidth";
		private String value;
		ChannelBandwidthEnum(String value){
			this.value = value;
		}
		public static ChannelBandwidthEnum get(String bandwidth) {
			ChannelBandwidthEnum channelBandwidthEnum = null;
			if(bandwidth != null){
				switch (bandwidth) {
				case "1.4":
					channelBandwidthEnum = BW1_4;
					break;
				case "3":
					channelBandwidthEnum = BW3;
					break;
				case "5":
					channelBandwidthEnum = BW5;
					break;
				case "10":
					channelBandwidthEnum = BW10;
					break;
				case "15":
					channelBandwidthEnum = BW15;
					break;
				case "20":
					channelBandwidthEnum = BW20;
					break;
				default:
					break;
				}
			}
			return channelBandwidthEnum;
		}
	}
	public enum CfiEnum{
		CFI1("1"),
		CFI2("2"),
		CFI3("3");
		
		public final static String fieldName = "CFI";
		private String value;
		CfiEnum(String value){
			this.value = value;
		}
		public static CfiEnum get(String cfi) {
			CfiEnum cfiEnum = null;
			if(cfi != null){
				switch (cfi) {
				case "1":
					cfiEnum = CFI1;
					break;
				case "2":
					cfiEnum = CFI2;
					break;
				case "3":
					cfiEnum = CFI3;
					break;
				default:
					break;
				}
			}
			return cfiEnum;
		}
	}
	public enum ConfigurationEnum{
		DEFAULT("Default"),
		USER("User"),
		LAB_OPTIMIZED("Lab optimized");
		
		public final static String fieldName = "Configuration";
		private String value;
		ConfigurationEnum(String value){
			this.value = value;
		}
		public static ConfigurationEnum get(String configuration) {
			ConfigurationEnum configurationEnum = null;
			if(configuration != null){
				switch (configuration) {
				case "Default":
					configurationEnum = DEFAULT;
					break;
				case "User":
					configurationEnum = USER;
					break;
				case "Lab optimized":
					configurationEnum = LAB_OPTIMIZED;
					break;
				default:
					break;
				}
			}
			return configurationEnum;
		}
	}
	public enum MaxSupportedUEsEnum{
		MSU64("64"),
		MSU128("128"),
		MSU256("256"),
		MSU512("512"),
		MSU1024("1024");
		
		public final static String fieldName = "Max Supported UEs";
		private String value;
		MaxSupportedUEsEnum(String value){
			this.value = value;
		}
		public static MaxSupportedUEsEnum get(int maxSupportedUEs) {
			MaxSupportedUEsEnum maxSupportedUEsEnum = null;
			switch (maxSupportedUEs) {
			case 64:
				maxSupportedUEsEnum = MSU64;
				break;
			case 128:
				maxSupportedUEsEnum = MSU128;
				break;
			case 256:
				maxSupportedUEsEnum = MSU256;
				break;
			case 512:
				maxSupportedUEsEnum = MSU512;
				break;
			case 1024:
				maxSupportedUEsEnum = MSU1024;
				break;
			default:
				break;
			}
			return maxSupportedUEsEnum;
		}
	}
	public enum SpecialSubframeEnum{
		SSF0("(0)     101.1  [Km]"),
		SSF1("(1)     36.8   [Km]"),
		SSF2("(2)      26.1  [Km]"),
		SSF3("(3)     15.4   [Km]"),
		SSF4("(4)      4.7     [Km]"),
		SSF5("(5)      90.4   [Km]"),
		SSF6("(6)     26.1   [Km]"),
		SSF7("(7)     15.4   [Km]"),
		SSF8("(8)     4.7     [Km]");
		
		public final static String fieldName = "Special Subframe";
		private String value;
		SpecialSubframeEnum(String value){
			this.value = value;
		}
		public static SpecialSubframeEnum get(String apecialSubframe) {
			SpecialSubframeEnum specialSubframeEnum = null;
			if(apecialSubframe != null){
				switch (apecialSubframe) {
				case "0":
					specialSubframeEnum = SSF0;
					break;
				case "1":
					specialSubframeEnum = SSF1;
					break;
				case "2":
					specialSubframeEnum = SSF2;
					break;
				case "3":
					specialSubframeEnum = SSF3;
					break;
				case "4":
					specialSubframeEnum = SSF4;
					break;
				case "5":
					specialSubframeEnum = SSF5;
					break;
				case "6":
					specialSubframeEnum = SSF6;
					break;
				case "7":
					specialSubframeEnum = SSF7;
					break;
				case "8":
					specialSubframeEnum = SSF8;
					break;
				default:
					break;
				}
			}
			return specialSubframeEnum;
		}
	}
	public enum MultiplexingEnum{
		PTP("PTP"),
		PTMP("PTMP");
		
		public final static String fieldName = "Multiplexing";
		private String value;
		MultiplexingEnum(String value){
			this.value = value;
		}
		public static MultiplexingEnum get(String multiplexing) {
			MultiplexingEnum multiplexingEnum = null;
			if(multiplexing != null){
				if(multiplexing.equals("PTP")) {
					multiplexingEnum = PTP;
				}else{
					multiplexingEnum = PTMP;
				}
			}
			return multiplexingEnum;
		}
	}
	public enum TransmitDirectionEnum{
		DL,
		UL
	}
	private static LteThroughputCalculator instance;
	
	public static LteThroughputCalculator getInstance(){
		if(instance == null){
			synchronizedCreateInstance();
		}
		return instance;
	}
	private synchronized static void synchronizedCreateInstance(){
		if(instance == null){
			instance = new LteThroughputCalculator();
		}
	}
	private LteThroughputCalculator(){}
	
	
	
	private String generateXPathExpression(ChannelBandwidthEnum channelBandwidthEnum, DuplexEnum duplexEnum,
			FrameSplitEnum frameSplitEnum, SpecialSubframeEnum specialSubframeEnum, MultiplexingEnum multiplexingEnum,
			ConfigurationEnum configurationEnum, CfiEnum cfiEnum, MaxSupportedUEsEnum maxSupportedUEsEnum, TransmitDirectionEnum transmitDirectionEnum) {
		return ("//" + rootXml + "/" + configurationEnum + "/" + duplexEnum +"/" + multiplexingEnum + "/" + cfiEnum + "/" + frameSplitEnum
				+ "/" + specialSubframeEnum + "/" + channelBandwidthEnum + "/" + maxSupportedUEsEnum + "/" + transmitDirectionEnum);
	}
	
	
	public String getPassCriteriaFromXmlLteThroughputCalculator(ChannelBandwidthEnum channelBandwidthEnum, DuplexEnum duplexEnum, FrameSplitEnum frameSplitEnum, 
			SpecialSubframeEnum specialSubframeEnum, MultiplexingEnum multiplexingEnum, ConfigurationEnum configurationEnum, CfiEnum cfiEnum, MaxSupportedUEsEnum maxSupportedUEsEnum) throws Exception{
		String dl_ul = null;
		try{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse("resources/StaticXmlLteThroughputCalculatorGeneratedByAutomation.xml");
			doc.getDocumentElement().normalize();
			XPathFactory xPathfactory = XPathFactory.newInstance();
			XPath xpath = xPathfactory.newXPath();
			
			String dlPassCriteriaXmlPath = generateXPathExpression(channelBandwidthEnum, duplexEnum, frameSplitEnum, specialSubframeEnum, multiplexingEnum, configurationEnum, cfiEnum, maxSupportedUEsEnum, TransmitDirectionEnum.DL);
			String ulPassCriteriaXmlPath = generateXPathExpression(channelBandwidthEnum, duplexEnum, frameSplitEnum, specialSubframeEnum, multiplexingEnum, configurationEnum, cfiEnum, maxSupportedUEsEnum, TransmitDirectionEnum.UL);

			XPathExpression findDL = xpath.compile(dlPassCriteriaXmlPath);
			XPathExpression findUL = xpath.compile(ulPassCriteriaXmlPath);
	        
			Double dl = (Double)findDL.evaluate(doc, XPathConstants.NUMBER);
			Double ul = (Double)findUL.evaluate(doc, XPathConstants.NUMBER);
			
			if(dl != null && ul != null && dl > 0 && ul > 0){
				dl_ul = dl + "_" + ul;
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return dl_ul;
	}
	
	public String getPassCriteriaFromStaticLteThroughputCalculator(RadioParameters radioParams, ConfigurationEnum configuration,
			int maxSupportedUEs, String multiplexing) {
		String dl_ul = null;
		try{
			dl_ul = getPassCriteriaFromXmlLteThroughputCalculator(
					ChannelBandwidthEnum.get(radioParams.getBandwidth()),
					DuplexEnum.get(radioParams.getDuplex()),
					FrameSplitEnum.get(radioParams.getFrameConfig()), 
					SpecialSubframeEnum.get(radioParams.getSpecialSubFrame()),
					MultiplexingEnum.get(multiplexing),
					configuration,
					CfiEnum.get(radioParams.getCfi()),
					MaxSupportedUEsEnum.get(maxSupportedUEs));
		}catch(Exception e){
			e.printStackTrace();
		}
		return dl_ul;
	}
}

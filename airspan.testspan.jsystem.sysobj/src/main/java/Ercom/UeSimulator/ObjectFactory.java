package Ercom.UeSimulator;

public class ObjectFactory {

	public static Transmittable getTransportLayer(){
		return new XmlRpcTransport();
	}
}

package Netspan;

import java.util.List;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;

public class NetspanSOAPManager {
	
	SOAPLoggingHandler hndl;

	public NetspanSOAPManager() {
		hndl = new SOAPLoggingHandler();
	}

	public String getLatestSoapResult(){
		String result = hndl.getRawData();
		result = printXmlFormat(result);
		return result;
	}
	
	/**
	 * @param lte Soap - port
	 * @return lte Soap with a handler
	 */
	@SuppressWarnings("rawtypes")
	public BindingProvider addRawDataHandler(BindingProvider soap) {
		final Binding binding = soap.getBinding ();
		final List<Handler> handlerChain = binding.getHandlerChain ();
		handlerChain.add (hndl);
		binding.setHandlerChain (handlerChain);
		return soap;
	}
	
	public void clearLteRawData(){
		hndl.clearRawData();
	}
	
	
	/**
	 * @param onelineXML string
	 * @return xml file with new lines and indentations.
	 */
	public String printXmlFormat(String onelineXML){
		String xmlString = null;
		xmlString = onelineXML.replaceAll("</([^>]*)>", "</$1>\n");
		return xmlString;
	}
}

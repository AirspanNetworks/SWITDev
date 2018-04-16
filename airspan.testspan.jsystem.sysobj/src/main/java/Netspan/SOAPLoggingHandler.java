package Netspan;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import Utils.GeneralUtils;

public class SOAPLoggingHandler implements SOAPHandler<SOAPMessageContext> {
	 private static PrintStream out = System.out;
	 private static ByteArrayOutputStream  stream = new ByteArrayOutputStream();
	 private static String rawData = null;

	 public SOAPLoggingHandler(){
		 
	 }
	 
	public boolean handleMessage(SOAPMessageContext context) {
		 logToSystemOut(context);
		return true;
	}

	public boolean handleFault(SOAPMessageContext context) {
		 logToSystemOut(context);
		return true;
	}

	public void close(MessageContext context) {
		GeneralUtils.printToConsole("NO Implementation to Method close() in SOAPLoggingHandler");
	}

	public Set<QName> getHeaders() {
		GeneralUtils.printToConsole("NO Implementation to Method getHeaders() in SOAPLoggingHandler");
		return null;
	}

	private void logToSystemOut(SOAPMessageContext smc) {
//        Boolean outboundProperty = (Boolean)smc.get (MessageContext.MESSAGE_OUTBOUND_PROPERTY);

        SOAPMessage message = smc.getMessage();
        try {
            message.writeTo(stream);
            
            rawData = new String(stream.toByteArray(),"utf-8");
        } catch (Exception e) {
            out.println("Exception in handler: " + e);
        }
    }
	
	public String getRawData(){
		return rawData;
	}
	
	public void clearRawData(){
		rawData = "cleard";
		stream = new ByteArrayOutputStream();
	}
}

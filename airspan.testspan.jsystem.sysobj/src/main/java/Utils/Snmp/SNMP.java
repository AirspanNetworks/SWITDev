package Utils.Snmp;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map.Entry;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.ScopedPDU;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.TransportMapping;
import org.snmp4j.UserTarget;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.AuthSHA;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.security.SecurityModels;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.security.USM;
import org.snmp4j.security.UsmUser;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.IpAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import EnodeB.Components.EnodeBComponent;
import Utils.GeneralUtils;
import Utils.PingUtils;
import jsystem.framework.report.Reporter;
import jsystem.framework.system.SystemObjectImpl;

/**
 * The SNMP class provides basic SNMP functionality, which gives you ability to
 * communicate with devices installed on the local network and support SNMP
 * protocol for management. Currently class support only SNMP version1 and SNMP
 * version2c (SNMPv1 & SNMPv2c). Note: SNMP Community strings are used only by
 * devices which support SNMPv1 and SNMPv2c
 * 
 * Supported functionality: 1. SNMP Set Object value. 2. SNMP Get Object value.
 * 3. SNMP Get next Object value. 4. SNMP Walk .
 * 
 *
 * @author shaham
 * @version 1.3
 */
/**
 * @author dshalom
 *
 */
public class SNMP extends SystemObjectImpl {

	// ============================== Static variables =======================//

	/**
	 * Default SNMP port
	 */
	private static final String DEFAULT_SNMP_PORT = "161";
	private String usedPort = DEFAULT_SNMP_PORT;

	/**
	 * Sets the number of retries to be performed before a request is timed out.
	 */
	private static final int RETRIES = 5;

	/**
	 * timeout in milliseconds before a confirmed request is resent or timed
	 * out.
	 */
	private static final int TIMEOUT = 15000;
    
	private String readCommunityOrUserName;

	private String writeCommunityOrPassword;

	private String strAddress;

	private String snmpVersion;

	private Snmp snmp;

	private Address targetAddress;

	private TransportMapping<?> transport;

	private CommunityTarget target;

	PDU pdu;

	// ========================== Contractors =============================//

	/**
	 * Empty default contractor
	 */
	public SNMP() {
	}

	/**
	 * SNMP class contractor - set all parameters and call to INIT function.
	 * 
	 * @param {String} readCommunity - is like a user id or password that allows
	 * access to managed device
	 * @param {String} writeCommunity - is like a user id or password that
	 * allows access to managed device
	 * @param {String} strAddress - remote host IP address
	 * @param {String} snmpVersion - SNMP version1 or SNMP version2c or version3
	 */
	public SNMP(String readCommunityOrUserName, String writeCommunityOrPassword, String strAddress,
			String snmpVersion) {

		
		setReadCommunityOrUserName(readCommunityOrUserName);
		setWriteCommunityOrPassword(writeCommunityOrPassword);
		setStrAddress(strAddress);
		setSnmpVersion(snmpVersion);
		initiatePDU();
	}
	
	/**
	 * SNMP class contractor - set all parameters and call to INIT function.
	 * 
	 * @param {String} readCommunity - is like a user id or password that allows
	 * access to managed device
	 * @param {String} writeCommunity - is like a user id or password that
	 * allows access to managed device
	 * @param {String} strAddress - remote host IP address
	 * @param {String} snmpVersion - SNMP version1 or SNMP version2c or version3
	 */
	public SNMP(String readCommunityOrUserName, String writeCommunityOrPassword, String strAddress,
			String snmpVersion,int port) {

		usedPort = port +"";
		setReadCommunityOrUserName(readCommunityOrUserName);
		setWriteCommunityOrPassword(writeCommunityOrPassword);
		setStrAddress(strAddress);
		setSnmpVersion(snmpVersion);
		initiatePDU();
	}

	// ============================== Methods ==============================//

	/**
	 * Initiate Protocol data units (PDUs) Each SNMP message contains a protocol
	 * data unit (PDU). These SNMP PDUs are used for communication between SNMP
	 * managers and SNMP agent.
	 * 
	 * @param NO
	 * @return NO
	 */
	public void initiatePDU() {
		try {
			targetAddress = GenericAddress.parse("udp:" + getStrAddress() + "/"+ usedPort);
			transport = new DefaultUdpTransportMapping();
			
			if (snmpVersion.equals("V3")) { 
				USM usm = new USM(SecurityProtocols.getInstance(), new OctetString(MPv3.createLocalEngineID()), 0); 
				SecurityModels.getInstance().addSecurityModel(usm); 
			}
			
			snmp = new Snmp(transport);
						
			if (snmpVersion.equals("V3")){ 
				UsmUser usmUser = new UsmUser(new OctetString(readCommunityOrUserName), AuthSHA.ID, new OctetString(writeCommunityOrPassword), null, null);
				snmp.getUSM().addUser(new OctetString(readCommunityOrUserName), usmUser);
			}		
			
			target = new CommunityTarget();
			target.setCommunity(new OctetString(getWriteCommunityOrPassword()));
			target.setAddress(targetAddress);
			target.setRetries(RETRIES);
			target.setTimeout(TIMEOUT);
			target.setVersion(translateVersionToInt(getSnmpVersion()));
			pdu = new PDU();
		}
		catch (IllegalArgumentException i) {
			GeneralUtils.printToConsole("Failed to initiate SNMP protocol data unit, some of the "
					+ "parameters passed to SNMP incorrect:"+i.getMessage());
			i.printStackTrace();
			
		}
		catch (IOException e) {
			GeneralUtils.printToConsole("Failed to initiate SNMP protocol data unit, "
					+ "see Stack trace below "+e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * The snmpSet is used to actually modify information on the remote host.
	 * For each variable you want to set, you need to specify the OID to update,
	 * the data type and the value you want to set it to.
	 * 
	 * @param String strOID - SNMP Object Identifiers point to network objects
	 * stored in a database. which represented as a tree. Here's an example:
	 * 1.3.6.1.4.1.2681.1.2.102
	 * @param byte[] value - Legal value
	 * @return void
	 * @throws IOException
	 * */
	public boolean snmpSet(String strOID, byte[] Value) throws IOException {
		
		VariableBinding intBind = new VariableBinding(new OID(strOID), new OctetString(Value));
		return snmpSet(intBind);
	}
	/**
	 * The snmpSet is used to actually modify information on the remote host.
	 * For each variable you want to set, you need to specify the OID to update,
	 * the data type and the value you want to set it to.
	 * 
	 * @param String strOID - SNMP Object Identifiers point to network objects
	 * stored in a database. which represented as a tree. Here's an example:
	 * 1.3.6.1.4.1.2681.1.2.102
	 * @param int value - Legal value
	 * @return void
	 * @throws IOException
	 * */
	public boolean snmpSet(String strOID, int Value) {
		VariableBinding intBind = null;
		try{
			intBind = new VariableBinding(new OID(strOID), new Integer32(Value));
		}catch (Exception e) {
			e.printStackTrace();
			report.report("Failed SNMP set for OID " + strOID);
			return false;
		}	
		return snmpSet(intBind);
	}
	
	/**
	 * The snmpSet is used to actually modify information on the remote host.
	 * For each variable you want to set, you need to specify the OID to update,
	 * the data type and the value you want to set it to.
	 * 
	 * @param String strOID - SNMP Object Identifiers point to network objects
	 * stored in a database. which represented as a tree. Here's an example:
	 * 1.3.6.1.4.1.2681.1.2.102
	 * @param int value - Legal value
	 * @return void
	 * @throws IOException
	 * */
	public boolean snmpSet(String strOID,InetAddress Value) throws IOException {
		
		VariableBinding intBind = new VariableBinding(new OID(strOID), new IpAddress(Value));
		return snmpSet(intBind);
	}
	
	/**
	 * The snmpSet is used to actually modify information on the remote host.
	 * For each variable you want to set, you need to specify the OID to update,
	 * the data type and the value you want to set it to.
	 * 
	 * @param String strOID - SNMP Object Identifiers point to network objects
	 * stored in a database. which represented as a tree. Here's an example:
	 * 1.3.6.1.4.1.2681.1.2.102
	 * @param String value - Legal value
	 * @return void
	 * @throws IOException
	 * */
	public boolean snmpSet(String strOID, String Value) {

		VariableBinding stringBind = new VariableBinding(new OID(strOID),new OctetString(Value));
		return snmpSet(stringBind);
	}
	
	/**
	 * The snmpSet is used to actually modify information on the remote host.
	 * For each variable you want to set, you need to specify the OID to update,
	 * the data type and the value you want to set it to.
	 * 
	 * @param String strOID - SNMP Object Identifiers point to network objects
	 * stored in a database. which represented as a tree. Here's an example:
	 * 1.3.6.1.4.1.2681.1.2.102
	 * @param int value - Legal value
	 * @return void
	 * @throws IOException
	 * */
	private boolean snmpSet(VariableBinding bind)  {
		if(!isEnodebReachable()){
			return false;
		}
		
		GeneralUtils.printToConsole("SNMP set - IP:"+target.getAddress().toString()+" OID:"+bind.getOid().toString() + " With Value:" + bind.getVariable().toString());
		int tries = 3;
		for (int i = 0; i < tries; i++) {
			try {
				if (!transport.isListening()) {
					transport.listen();
				}
				
				if (snmpVersion.equals("V3")){
					pdu = new ScopedPDU(); 					
				}else{
					pdu = new PDU();
				}
				
				pdu.add(bind);
				pdu.setType(PDU.SET);
				
				ResponseEvent response = null;
				
				if (snmpVersion.equals("V3")){
					response = snmp.send(pdu, getSNMPv3Target()); 					
				}else{
					response = snmp.send(pdu, target); 				
				}
				
				//ResponseEvent response = snmp.send(pdu, target);
				
				if(response == null)
					throw new Exception("response is null");
				
				String address = target.getAddress().toString();
				String oid = bind.getOid().toString();
				String var = bind.getVariable().toString();
				int errorStatus = response.getResponse().getErrorStatus();
				
				
				GeneralUtils.printToConsole("SNMP set - IP:"+address+" OID:"+ oid+ " With Value:" + var+ " Response:" +errorStatus);  
				if( response.getResponse().getErrorStatus() == 0)
					return true;
				else
					GeneralUtils.printToConsole("SNMP got false Response " + response.getResponse().getErrorStatus());
			} catch (Exception e) {
				e.printStackTrace();
				GeneralUtils.unSafeSleep(3000);
			} finally {
				pdu.clear();
				try {
					snmp.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} 
		}
		return false;
	}

	/**
	 * SNMP GET request to query for information on a network entity. OIDs must
	 * be given as arguments.
	 * 
	 * @param String strOID - Here's an example: 1.3.6.1.4.1.2681.1.2.102
	 * @return String - SNMP Object value
	 * @throws IOException 
	 * */
	public String get(String strOID) {
		return getString(strOID, true);
	}
	
	private VariableBinding getSnmp(String strOID, boolean isCurrent) {
		if(!isEnodebReachable()){
			GeneralUtils.printToConsole("EnodeB is unreachable, can't get MIB with oid " + strOID);
			return null;
		}
		SNMPGetThread worker = new SNMPGetThread( strOID, this);
		worker.start();
		long snmpGetTimeout = 2000;
		long startTime = System.currentTimeMillis(); // fetch starting time
		while ((System.currentTimeMillis() - startTime) < snmpGetTimeout) {
			if (worker.snmpGetFinished)
				break;
			GeneralUtils.unSafeSleep(100);
		}
		
		if (!worker.snmpGetFinished) {
			GeneralUtils.printToConsole("get SNMP reached timeout, closing process.");
			worker.interrupt();
		}
		return worker.value;
	}
	
	public Variable getVariable(String strOID) throws IOException {
		VariableBinding vb = getVariableBinding(strOID);
		if(vb != null){
			return vb.getVariable();
		}else{
			Object v = new Integer(GeneralUtils.ERROR_VALUE);
			return (Variable) v;
		}
	}
	
	private String getString(String strOID, boolean isCurrent) {
		VariableBinding vb = null;
		//VariableBinding vb = getSnmp(strOID, isCurrent);
		String str = "";
		for(int i=0;i<3;i++){
			vb = getSnmp(strOID, isCurrent);
			if(vb != null){
				break;
			}
			GeneralUtils.printToConsole("Failed to get snmp value #"+(i+1));
			GeneralUtils.unSafeSleep(2*1000);
		}
		if(vb != null){
			str = vb.toString();
			if (str.contains("=")) {
				int len = str.indexOf("=");
				str = str.substring(len + 1, str.length()).trim();
			}
			return str;
		}
		return String.valueOf(GeneralUtils.ERROR_VALUE);
	}
	
	private boolean isEnodebReachable(){
		try{
			String addr = target.getAddress().toString();
			String[] temp = addr.split("/");
			addr = temp[0];	
			if(!PingUtils.isReachable(addr, EnodeBComponent.PING_RETRIES)){
				return false;
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return true;
	}
	
	public VariableBinding getVariableBinding(String strOID) {
		if(!isEnodebReachable()){
			GeneralUtils.printToConsole("EnodeB is unreachable, cant get MIB with oid " + strOID);
			return null;
		}
		VariableBinding ret;		
		
		ret = snmpGetVariableBinding(strOID);
				
		return ret;	
	}
	
	public Boolean deleteRow (String strOID) throws IOException{	
		return snmpSet(strOID, 6);
	}

	/**
	 * SNMP GET request to query for information on a network entity. OIDs must
	 * be given as arguments. The isCurrent parameter defines if returned object
	 * specified by OID or next to It on the MIB Data base tree.
	 * 
	 * @param String strOID - Here's an example: 1.3.6.1.4.1.2681.1.2.102
	 * @param bollean isCurrent
	 * @return String - SNMP Object value or anEmpty string.
	 * @throws IOException 
	 * */
	private VariableBinding snmpGetVariableBinding(String strOID) {
		VariableBinding vb = null;
		ResponseEvent response;
		for (int i = 1; i <= 3; i++) {
			try {
				if (!transport.isListening()) {
					transport.listen();
				}

				if (snmpVersion.equals("V3")) {
					pdu = new ScopedPDU();
				} else {
					pdu = new PDU();
				}

				pdu.add(new VariableBinding(new OID(strOID)));
				pdu.setType(PDU.GET);
				synchronized (this) {
					if (snmpVersion.equals("V3")) {
						response = snmp.get(pdu, getSNMPv3Target());
					} else {
						response = snmp.get(pdu, target);
					}
				}

				if (response != null && response.getResponse() != null
						&& response.getResponse().getErrorStatusText() != null) {
					if (response.getResponse().getErrorStatusText().equalsIgnoreCase("Success")) {
						PDU pduresponse = response.getResponse();
						vb = pduresponse.getVariableBindings().firstElement();
					}
				} else {
					GeneralUtils.printToConsole("Feeling like a TimeOut occured ");
				}

				pdu.clear();
				snmp.close();
				break;
			} catch (Exception e) {
				GeneralUtils.printToConsole("SNMP get failed attempt #" + i);
				e.printStackTrace();
			} finally {
				// clear for future use
				pdu.clear();
				// close SNMP session
				try {
					snmp.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		GeneralUtils.printToConsole("SNMP get IP:"+target.getAddress().toString()+" OID:"+strOID + " got Value:" + vb);  
		
		return vb;
	}

	public HashMap<String, Variable> SnmpWalk(String strOID)  {
		return SnmpWalk(strOID, true);
	}
	
	public HashMap<String, Variable> SnmpWalk(String strOID, boolean printOutput)  {
		HashMap<String, Variable> ret = new HashMap<>();
		ret =  doSnmpWalk(strOID, printOutput);
		return ret;
	}
	
	/**
	 * SnmpWalk allows you to detect a set of variables that are available for
	 * reading on a certain device. You can obtain a full list or just part of
	 * MIBs.
	 * 
	 * @return Hash map with OIDs and values or empty hash map.
	 * @param String strOID - Here's an example: 1.3.6.1.4.1.2681.1.2.102
	 * @throws IOException 
	 * @throws general exception
	 * */
	private HashMap<String, Variable> doSnmpWalk(String strOID, boolean printOutput) {
		HashMap<String, Variable> values = new HashMap<String, Variable>();
		
		if (!this.isAvailable()) {
			GeneralUtils.printToConsole("Unable to get SNMP for OID " + strOID);
			return values;
		}
		SNMPWalkThread worker = new SNMPWalkThread(snmp, target, pdu, transport, strOID, printOutput);
		worker.start();
		long snmpWalkTimeout = 10000;
		long startTime = System.currentTimeMillis(); // fetch starting time
		while ((System.currentTimeMillis() - startTime) < snmpWalkTimeout) {
			if (worker.snmpWalkFinished)
				break;
			GeneralUtils.unSafeSleep(100);
		}
		
		if (!worker.snmpWalkFinished) {
			GeneralUtils.printToConsole("SNMP Walk reached timeout, closing process.");
			worker.interrupt();
			GeneralUtils.printToConsole("Failed to complete SNMP Walk, timeout reached.");
		}
		return worker.values;
	}	

	/**
	 * Purpose of the function is to translate String representation of the SNMP
	 * version defined in SUT file to integer representation Suitable for
	 * target.setVersion() function. User can input different Strings , for
	 * example version2c == V2 Remark: Default version SNMP V2.
	 * @param String SNMP version given by string in SUT file
	 * @return integer representation of SNMP version.
	 * */
	private int translateVersionToInt(String snmpVersion) {
		int intRepresentation = -1;
		switch (snmpVersion) {
		case "version1":
			intRepresentation = 0;
			break;
		case "version2c":
			intRepresentation = 1;
			break;
		case "version3":
			intRepresentation = 2;

		case "V1":
			intRepresentation = 0;
			break;
		case "V2":
			intRepresentation = 1;
			break;
		case "V3":
			intRepresentation = 2;
			break;
		default:
			intRepresentation = 1; // SNMP version 2
			break;
		}

		return intRepresentation;
	}

	// ============================== Getters/ setters ======================//

	/**
	 * is like a user id or password that allows access to managed device Gets
	 * the community string.
	 * @return an String instance
	 */
	public String getReadCommunityOrUserName() {
		return readCommunityOrUserName;
	}

	/**
	 * is like a user id or password that allows access to managed device Sets
	 * the community sting.
	 * @param {String} community an instance which must not be null
	 */
	private void setReadCommunityOrUserName(String readCommunity) {
		this.readCommunityOrUserName = readCommunity;
	}

	/**
	 * is like a user id or password that allows access to managed device Gets
	 * the community string.
	 * @return an String instance
	 */
	public String getWriteCommunityOrPassword() {
		return writeCommunityOrPassword;
	}

	/**
	 * is like a user id or password that allows access to managed device Sets
	 * the community sting.
	 * @param {String} community an instance which must not be null
	 */
	private void setWriteCommunityOrPassword(String writeCommunity) {
		this.writeCommunityOrPassword = writeCommunity;
	}

	/**
	 * GetThe IP address IPV4 of remote managed entity
	 * @return {String} strAddress- IP address.
	 */
	private String getStrAddress() {
		return strAddress;
	}

	/**
	 * Set The IP address IPV4 of remote managed entity
	 * @param {String} strAddress- IP address.
	 */
	private void setStrAddress(String strAddress) {
		this.strAddress = strAddress;
	}

	/**
	 * Gets the SNMP version (NMP message processing model) of the target.
	 * @return the message processing model ID.
	 * @see org.snmp4j.mp.SnmpConstants#version1
	 * @see org.snmp4j.mp.SnmpConstants#version2c
	 * @see org.snmp4j.mp.SnmpConstants#version3
	 */
	public String getSnmpVersion() {
		return snmpVersion;
	}

	/**
	 * Sets the SNMP version (thus the SNMP message processing model) of the
	 * target.
	 * @param version the message processing model ID.
	 * @see org.snmp4j.mp.SnmpConstants#version1
	 * @see org.snmp4j.mp.SnmpConstants#version2c
	 * @see org.snmp4j.mp.SnmpConstants#version3
	 */
	private void setSnmpVersion(String snmpVersion) {
		this.snmpVersion = snmpVersion;
	}

	public boolean deletAllRowsInTable(String rowStatusOID) throws IOException {
		boolean retStat = true;
		HashMap<String, Variable> walkResult = this.SnmpWalk(rowStatusOID);
		for(String OidKey : walkResult.keySet())
			retStat = retStat && deleteRow(OidKey);
		return retStat;		
	}
	
	
	/**
	 * Add new entry at the next available index
	 * @param rowStatusOid
	 * @return 
	 * @throws IOException
	 */
	public boolean addNewEntry(String rowStatusOid) throws IOException{
		int newValue = 0;
		String newowStatusOid = "";
		String lastRow = getLastRowIndex(rowStatusOid);
		
		newValue = Integer.parseInt(lastRow);
		newValue++;
		newowStatusOid = rowStatusOid + "." + newValue;
		return snmpSet(newowStatusOid, 4);
	}
	
	/**
	 * Add new entry at a specific index
	 * @param rowStatusOid
	 * @param index
	 * @return
	 * @throws IOException
	 */
	public boolean addNewEntry(String rowStatusOid, int index, boolean activate) throws IOException{
		String newRowStatusOid = "";
		int activationId = activate ? 4 : 5;
		newRowStatusOid = rowStatusOid + "." + index;
		return snmpSet(newRowStatusOid, activationId);
	}
	
	public boolean addRowInTable(String rowStatusOid, HashMap<String,String> params) throws IOException{
		boolean result = true;
		HashMap<String,String> tempParams = new HashMap<>();
		if (!addNewEntry(rowStatusOid)){
			report.report("There was an error while trying to add row to table by SNMP");
			return false;
		}
		String index = getLastRowIndex(params.entrySet().iterator().next().getKey());
		for(Entry<String,String> OID : params.entrySet()){
			String OIDString = OID.getKey();
			OIDString = OIDString + "." + index;
			tempParams.put(OIDString, OID.getValue());
		}

		for(Entry<String, String> OID : tempParams.entrySet()){ 
			result = result & snmpSet(OID.getKey(), Integer.parseInt(OID.getValue()));
		}
		return result;
	}
	
	private String getLastRowIndex(String OID) throws IOException{
		String[] oidSubstring = new String[20];
		String lastRow = "0";
		String temp = "0";
		HashMap<String, Variable> walkResult = this.SnmpWalk(OID);
		
		for(String sub: walkResult.keySet()){
			oidSubstring = sub.split("\\.");
			temp = oidSubstring[oidSubstring.length-1];
			if (Integer.parseInt(temp)  > Integer.parseInt(lastRow)){
				lastRow = temp;
			}
		}
		return lastRow;
	}
	
	public Target getSNMPv3Target() { 
		UserTarget target = new UserTarget();
		target.setAddress(targetAddress);
		target.setVersion(SnmpConstants.version3);
		target.setRetries(RETRIES);
		target.setTimeout(TIMEOUT);
		target.setSecurityLevel(SecurityLevel.AUTH_NOPRIV);
		target.setSecurityName(new OctetString(readCommunityOrUserName));
		return target;
	}
	
	public long waitForUpdate(long timeout, String oid, Integer index, String expected) {
		long startTime = System.currentTimeMillis(); // fetch starting time
		String temp = "";
		if (index != null)
			oid += ("." + index);
		while ((System.currentTimeMillis() - startTime) < timeout) {
			temp = this.get(oid);
			if (temp.equals(expected)) {
				return System.currentTimeMillis() - startTime;
			}
			GeneralUtils.unSafeSleep(5000);
		}
		return GeneralUtils.ERROR_VALUE;
	}

	public String get(String oid, Integer index) {
		String responce = "";
		if (oid != null)
			oid += ("." + index);
		responce = this.get(oid);
		return responce;
	}
	
	public boolean waitUntilNotAvailable(long timeOut) {
		long startTime = System.currentTimeMillis(); // fetch starting time
		
		while ((System.currentTimeMillis() - startTime) < timeOut) {
			if(!this.isAvailable()){
					return true;
			}
			GeneralUtils.unSafeSleep(2 * 1000);
		}
		return false;
	}
	
	public boolean isAvailable(){
		String oid = MibReader.getInstance().resolveByName("SysName");
		String sysName = this.get(oid);
		GeneralUtils.printToConsole("System name - " + sysName);
		boolean available = !(sysName.equals(String.valueOf(GeneralUtils.ERROR_VALUE)) || sysName.equals("noSuchObject"));
		return available;
		
	}	
}
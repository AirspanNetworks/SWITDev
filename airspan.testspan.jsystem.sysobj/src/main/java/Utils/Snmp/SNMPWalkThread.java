package Utils.Snmp;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.TransportMapping;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.util.DefaultPDUFactory;
import org.snmp4j.util.TableEvent;
import org.snmp4j.util.TableUtils;

import Utils.GeneralUtils;

public class SNMPWalkThread extends Thread implements Runnable {

	public HashMap<String, Variable> values = new HashMap<String, Variable>();
	public boolean snmpWalkFinished = false;
	private TransportMapping<?> transport;
	private Snmp snmp;
	private String snmpWalkStrOID;
	private Target target;
	private PDU pdu;

	public SNMPWalkThread(Snmp snmp, Target target, PDU pdu, TransportMapping<?> transport, String snmpWalkStrOID) {
		this.snmp = snmp;
		this.target = target;
		this.pdu = pdu;
		this.transport = transport;
		this.snmpWalkStrOID = snmpWalkStrOID;
	}

	public void run() {
		for (int i = 1; i <= 3; i++) {
			try {
				if (!transport.isListening()) {
					transport.listen();
				}
				List<TableEvent> tableEvents = null;
				synchronized (this) {
					TableUtils tableUtils = new TableUtils(snmp, new DefaultPDUFactory(PDU.GETNEXT));

					OID[] oid = { new OID(snmpWalkStrOID) };
					GeneralUtils.printToConsole("snmpwalk on " + snmpWalkStrOID);

					tableEvents = tableUtils.getTable(target, oid, null, null);
				}
				for (TableEvent event : tableEvents) {
					if (event != null) {
						if (event.isError()) {
							throw new Exception(event.getErrorMessage());
						}
						if (event.getColumns() == null || event.getColumns().length == 0) {
							GeneralUtils.printToConsole("No result returned.");
						}
						for (VariableBinding vb : event.getColumns()) {

							String key = vb.getOid().toString();
							Variable value = vb.getVariable();
							GeneralUtils.printToConsole("snmpwalk variable Oid: " + vb.getOid().toString() + ", value: "
									+ vb.getVariable());
							values.put(key, value);
						}
					}
				}
				pdu.clear();
				snmp.close();
				break;
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				GeneralUtils.printToConsole(
						"Thread was interrupted, Failed to complete SNMPWalk for oid " + snmpWalkStrOID);
			} catch (Exception e) {
				GeneralUtils.printToConsole("Failed snmp walk attempt #" + i);
				GeneralUtils.printToConsole(e.getMessage());
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
		snmpWalkFinished = true;
	}
}

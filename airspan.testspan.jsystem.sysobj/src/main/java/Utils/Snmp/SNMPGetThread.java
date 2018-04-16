package Utils.Snmp;


import org.snmp4j.smi.VariableBinding;

public class SNMPGetThread extends Thread implements Runnable {
	
		public VariableBinding value;
		public boolean snmpGetFinished = false;
		SNMP snmp;
		String strOID;
		boolean isCurrent;

		public SNMPGetThread(String snmpGetStrOID, SNMP snmp) {
			this.snmp = snmp;
			this.strOID = snmpGetStrOID;

		}

		public void run() {
			value = snmp.getVariableBinding(strOID);
			snmpGetFinished = true;
		}
	}

//Classes we're gonna use
var Thread = Java.type('java.lang.Thread');
var ASNPathHelper = Java.type('airspan.lte_ipg.ASNPathHelper');
var AssociationWrapper = Java.type('airspan.lte_ipg.AssociationWrapper');
var System = Java.type('java.lang.System');

//protocol
var x2 = Java.type('airspan.lte_ipg.Protocol').getProtocols()['X2AP'];

//Constants
var baseFolder = './';
var remoteIP = "192.168.11.247";
var doDeletes = false; // send cell delete
var doAdds = false;    // send cell add
var count = 300; // connection count
var sleep_ms = 1000; // sleep time between sending packets
var noExit=true; // Don't exit when main is done

var configurationUpdateResponse=x2.loadPERPacket(baseFolder + 'configUpdateResponse.x2p');
var AssociationEventHandler = Java.type('airspan.lte_ipg.AssociationWrapper.AssociationEventHandler');

var setupResponses;
var AssociationToResponsesMap = {};

var ConnectionEventHandler = new AssociationEventHandler( {
	ReceivedPacket: function(eventSender , message , info) {
		//print('Received on ' + eventSender);
		
		var PDU_type = message.getChosenFlag();
		switch (PDU_type) {
			case 1: //initiating message
			
			//initiating message
			var rootHelper = new ASNPathHelper(message);
			var procedureCode = rootHelper.search('InitiatingMessage.procedureCode').object.intValue();
			switch (procedureCode ) {
				case 6:
					//Setup request
					eventSender.send(setupResponses[ AssociationToResponsesMap[eventSender.toString()] ] ,0,'response');
					print('Sent setup response');
					break;
				case 8: // configuration update
					eventSender.send(configurationUpdateResponse,0,'response');
					print('Sent configuration update response');
					break;
				default:
					print('Got message from ' + eventSender + ' : \n' + message);
			}
			break;
			case 2: // successfulMessage
				//print ('Got pdu type ' + PDU_type);
				break;
			case 3: //unsuccessful message
				print ('Got unsuccessful message! ' + message);
		}
	},
	SentPacket: function (eventSender , message ,messageName, info) {
		//print('Sent on ' + eventSender);
	},
	Disconnected: function( eventSender ) {
		print('Disconnect on ' + eventSender);
	},
	ErrorOnReceive: function(eventSender , exception) {
		print('Got error on ' + eventSender + ': ' + exception.getMessage());
		exception.printStackTrace();
	}
}	
);

//Prints all the content of these packets
function debugging() {
	var setup = x2.loadPERPacket('/home/eyalc/x2-tests/test1-singlecell-HCANR-goodERFCAN/setup.per', true);
	var deletep = x2.loadPERPacket('/home/eyalc/x2-tests/test1-singlecell-HCANR-goodERFCAN/delete_cell.per', true);
	var add = x2.loadPERPacket('/home/eyalc/x2-tests/test1-singlecell-HCANR-goodERFCAN/add_cells.per', true);
	var setupHelper = new ASNPathHelper(setup);
	var addHelper = new ASNPathHelper(add);
	var deleteHelper = new ASNPathHelper(deletep);
	
	print('setup.per');
	print(setupHelper.printSubtree());
	print('add.per');
	print(addHelper.printSubtree());	
	print('delete.per');
	print(deleteHelper.printSubtree());
}

function calculatePCI(id) {
	return id % 128 + 2; // we don't like PCI 0
}

function calculateEARFCN(id) {
	var temp = Math.floor(id / 128);
	var baseEARFCN = 38850;
	return baseEARFCN + (500*temp);
}

//sets a BitString ASN object to value 
function setBitString(BitStringObj , value) {
	//we assume lower bound is same as upper bound
	var bitCount = BitStringObj.getSize();
	//print('Bounds ' + bitCount);
	for (var i=bitCount-1;i>=0;i--) {
		var bit = value & 1;
		if (bit) {
			BitStringObj.setBit(i);
		}
		else { 
			BitStringObj.clearBit(i);
		}
		value = value >>> 1;
	}
}

//Initiate a connection to remote host
function createConnection (protocol,net, number , remoteAddr) {
	var ipAddress = "10.100." + net + "." + number;
	
	var InetSocketAddress = Java.type('java.net.InetSocketAddress');
	var localSockAddr = new InetSocketAddress( Java.type('java.net.InetAddress').getByName(ipAddress) , protocol.port  );
	var remoteSockAddr = new InetSocketAddress( Java.type('java.net.InetAddress').getByName(remoteAddr) , protocol.port  );
	
	print("Connecting to " + remoteSockAddr + " from "+  localSockAddr);
	var AssociationWrapper = Java.type('airspan.lte_ipg.AssociationWrapper');
	
	return new AssociationWrapper(ipAddress , protocol , remoteSockAddr , localSockAddr, 3,3,ConnectionEventHandler);
}

//Given an X2 setup-response packet, sets the eNB_ID and the ECGI
function setSetupResponseID(packet , id) {
	var rootHelper = new ASNPathHelper(packet);
	var eNB_ID = 				rootHelper.search('SuccessfulOutcome.value.opentype.protocolIEs.element0.value.opentype.eNB-ID.BIT STRING').object; // it's a bitstring
	setBitString(eNB_ID,id);
	var eUTRANcellIdentifier = 	rootHelper.search('SuccessfulOutcome.value.opentype.protocolIEs.element1.value.opentype.element0.servedCellInfo.cellId.eUTRANcellIdentifier').object;// it's a bitstring
	setBitString(eUTRANcellIdentifier,id);
	var pci = rootHelper.search('SuccessfulOutcome.value.opentype.protocolIEs.element1.value.opentype.element0.servedCellInfo.pCI').object;// it's an integer
	pci.setValue( calculatePCI(id)) ;
	var earfcn = rootHelper.search('SuccessfulOutcome.value.opentype.protocolIEs.element1.value.opentype.element0.servedCellInfo.eUTRA-Mode-Info.TDD-Info.eARFCN').object;// it's an integer
	earfcn.setValue(calculateEARFCN(id));
}

//Given an X2 setup-request packet, sets the eNB_ID and the ECGI
function setSetupID(packet , id) {
	var rootHelper = new ASNPathHelper(packet);
	//print(rootHelper.printSubtree());
	var eNB_ID = 				rootHelper.search('InitiatingMessage.value.opentype.protocolIEs.element0.value.opentype.eNB-ID.BIT STRING').object; // it's a bitstring
	setBitString(eNB_ID,id);
	var eUTRANcellIdentifier = 	rootHelper.search('InitiatingMessage.value.opentype.protocolIEs.element1.value.opentype.element0.servedCellInfo.cellId.eUTRANcellIdentifier').object;// it's a bitstring
	setBitString(eUTRANcellIdentifier,id);
	var pci = rootHelper.search('InitiatingMessage.value.opentype.protocolIEs.element1.value.opentype.element0.servedCellInfo.pCI').object;// it's an integer
	pci.setValue( calculatePCI(id)) ;
	var earfcn = rootHelper.search('InitiatingMessage.value.opentype.protocolIEs.element1.value.opentype.element0.servedCellInfo.eUTRA-Mode-Info.TDD-Info.eARFCN').object;// it's an integer
	earfcn.setValue(calculateEARFCN(id));
}

//Given an X2 delete request packet, sets the ECGI
function setDeleteID(packet , id) {
	var rootHelper = new ASNPathHelper(packet);
	
	var eUTRANcellIdentifier = 	rootHelper.search('InitiatingMessage.value.opentype.protocolIEs.element0.value.opentype.element0.eUTRANcellIdentifier').object;// it's a bitstring
	setBitString(eUTRANcellIdentifier,id);
}

//Given an X2 add request packet, sets the ECGI
function setAddID(packet , id) {
	var rootHelper = new ASNPathHelper(packet);

	var eUTRANcellIdentifier = 	rootHelper.search('InitiatingMessage.value.opentype.protocolIEs.element0.value.opentype.element0.servedCellInfo.cellId.eUTRANcellIdentifier').object;// it's a bitstring
	setBitString(eUTRANcellIdentifier,id);
	
	var pci = rootHelper.search('InitiatingMessage.value.opentype.protocolIEs.element0.value.opentype.element0.servedCellInfo.pCI').object;// it's an integer
	pci.setValue( calculatePCI(id)) ;
	
	var earfcn = rootHelper.search('InitiatingMessage.value.opentype.protocolIEs.element0.value.opentype.element0.servedCellInfo.eUTRA-Mode-Info.TDD-Info.eARFCN').object;// it's an integer
	earfcn.setValue(calculateEARFCN(id));
}

//Loads packets, returns an array of the form [setups , deletes , adds]
function createPackets(count) {
	var setups = [];
	var setupResponses = [];
	var deletes = [];
	var adds = [];
	for (var i=0;i<count;i++) {
		print('Loading setup ' + i);
		var setup= x2.loadPERPacket(baseFolder + 'setup.x2p');
		setSetupID(setup , i+50);
		setups.push(setup);
		
		if (doDeletes) {
			print('Loading delete');
			var delete_packet= x2.loadPERPacket(baseFolder + 'delete_cell.x2p');
			setDeleteID(delete_packet , i+50);
			deletes.push(delete_packet);
		}

		if (doAdds) {
			print('Loading add');
			var add= x2.loadPERPacket(baseFolder + 'add_cells.x2p');
			setAddID(add , i+50);
			adds.push(add);
		}
		
		print('Loading setup response');
		var setupResponse = x2.loadPERPacket(baseFolder + 'setupresponse.x2p');
		setSetupResponseID(setupResponse , i+50);
		setupResponses.push(setupResponse);
	}
	print('Loaded all packets');
	return [setups , deletes , adds , setupResponses];
}

//Create multiple connections, returns an array of AssociationWrappers
function createConnections(count , protocol , remoteIP , baseIP) {
	var connections = [];
	for (var i=0;i<count;i++) {
		var localIP = baseIP + i;
		//print("localIP " + localIP);
		var localNet = localIP >>> 8;
		var localNum = localIP & 255;
		
		if (localNum == 255 || localNum == 0) {
			//ignore this one, generate another one
			count++;
			continue;
		}
		
		connections.push( [localNet,localNum ]  );
		//print('Created ' + connections.length + ' connections' );
	}
	return connections;
}

function sendPacketsToAll(protocol , remoteIP ,allpackets , connections , streamID) {
	var count = connections.length;
	var sleep_ns = 0;

	var setups = allpackets[0] ;
	var deletes = allpackets[1] ;
	var adds = allpackets[2] ;
	
	for (var i=0;i<count;i++) {
		
		var connection = createConnection(protocol , connections[i][0] ,  connections[i][1]  , remoteIP);
		AssociationToResponsesMap[connection.toString()] = i;
		packetName = 'setup';
		connection.send(setups[i] , streamID , packetName);
		print('Sent ' + packetName + ' ' + i);
		
		if (doDeletes) {
			Thread.sleep(sleep_ms,sleep_ns); 
			packetName = 'delete';
			connection.send(deletes[i] , streamID , packetName);
			print('Sent deletes');
		}
		if (doAdds) {
			Thread.sleep(sleep_ms,sleep_ns); 
			packetName = 'add';
			connection.send(adds[i] , streamID , packetName);
			print('Sent adds');
		}
		Thread.sleep(sleep_ms,sleep_ns); 
		
	}
}


function main() {
	//debugging();
	//return;
	var allpackets = createPackets(count);

	setupResponses = allpackets[3];
	
	connections = createConnections(count , x2 , remoteIP , 61441 + 3);
	sendPacketsToAll(x2 , remoteIP ,  allpackets , connections,0 , "setup");
	print('Sent setups');
	

}


main();

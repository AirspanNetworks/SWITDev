/*Virtual eNodeB*/

/*

	Configuration structure : 

	localIPAddress - string

	basePath - string, defaults to './'

	enb_id - integer, 20 bit max, defaults to 0

	

	PLMN - hex string, 3 bytes (example: "1A2B3C"), defaults to "000000"

	

	cells - an array

		EARFCN - integer

		tac - hex string, 2 bytes

		pci - integer

		tdd - boolean, defaults to true

	

*/



var ProtocolServer = Java.type('airspan.lte_ipg.ProtocolServer');

var ASNPathHelper = Java.type('airspan.lte_ipg.ASNPathHelper');



function stringToByteArray(stringg) {

	var utils = Java.type('airspan.utils');

	var bytes = stringg.length / 2; 

	var arr = [];

	for (var i=0;i<bytes;i++) {

		arr.push(utils.parseByte(stringg.substring(i*2,i*2+2),16));

	}

	return Java.to(arr,'byte[]');

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



//Constructor

function virtualENB(configuration) {

	function verifyConfiguration(configuration) {

		if (configuration['localIPAddress'] === undefined) {

			throw "Bad configuration, no ip address";

		}

		if (configuration['cells'] === undefined) {

			throw "Bad configuration, no cells";

		}		



		if (configuration['enb_id'] === undefined) {

			configuration['enb_id'] = 0;

		}

		

		if (configuration['basePath'] === undefined) {

			configuration['basePath'] = './';

		}		



		if (configuration['PLMN'] === undefined) {

			configuration['PLMN'] = "000000";

		}

		

		for (cellID in configuration['cells']) {

			var cell = configuration['cells'][cellID];

			if (cell['EARFCN'] === undefined) {

				throw "Bad cell EARFCN," + cell;

			}

			if (cell['tac'] === undefined) {

				throw "Bad cell tac," + cell;

			}

			if (cell['pci'] === undefined) {

				throw "Bad cell pci," + cell;

			}

			if (cell['tdd'] === undefined) {

				cell['tdd'] = true;

			}

		}

	}

	

	verifyConfiguration(configuration);

	this.x2 = Java.type('airspan.lte_ipg.Protocol').getProtocols()['X2AP'].clone(); //it's an object

	

	this.configuration = configuration;

	

	function basePath(fileName) {

		return configuration['basePath'] + fileName;

	}

	

	this.defaultPackets = {};

	this.neighbours = {};

	//Load basic packets from files

	//default ServedCell-Information

	var ServedCell_Information=Java.type('x2ap.x2ap_ies.ServedCell_Information').getStaticTypeInfo();

	var FileInputStream = Java.type('java.io.FileInputStream');

	fs = new  FileInputStream(basePath('ServedCell_Information.per'));

	this.defaultPackets['ServedCell_Information'] = this.x2.decodeSpecificStruct(fs , ServedCell_Information , false);

	fs.close();

	

	var parent = this; // anonymous methods will use "parent" to access "this"

	function loadSetup(filename) { //This function both loads and fills the setup request/response

		var result = parent.x2.loadPERPacket(basePath(filename));

		var rootHelper = new ASNPathHelper(result);

		var eNB_ID = rootHelper.search('choice.value.opentype.protocolIEs.element0.value.opentype').object; 

		parent.modifyENB_ID(eNB_ID);

		

		

		var servedCells = rootHelper.search('choice.value.opentype.protocolIEs.element1.value.opentype').object; //sequence of

		var exampleCell = rootHelper.search('choice.value.opentype.protocolIEs.element1.value.opentype.element0').object; // served cell

		//it already contains a default element

		

		servedCells.removeAllElements(); // start clear

		for (var i=0;i<configuration['cells'].length;i++) {

			var newCell = parent.x2.duplicateStruct(exampleCell);

			parent.configureCell(newCell , i);

			servedCells.addElement(newCell);

		}

		return result;

	}

	

	this.defaultPackets['setupRequest'] = loadSetup('setupRequest.per');

	this.defaultPackets['setupResponse'] = loadSetup('setupResponse.per');

	this.defaultPackets['configUpdateResponse'] = this.x2.loadPERPacket(basePath('configUpdateResponse.per'));

	/*

	//Cell down

	this.defaultPackets['cellDown'] = x2.loadPERPacket(basePath('cellDown.per'));

	//Cell up

	this.defaultPackets['cellUp'] = x2.loadPERPacket(basePath('cellUp.per'));



	//default successful outcome

	this.defaultPackets['successfulOutcome'] = x2.loadPERPacket(basePath('successfulOutcome.per'));

	//default unsuccessful outcome

	this.defaultPackets['unsuccessfulOutcome'] = x2.loadPERPacket(basePath('unsuccessfulOutcome.per'));

	*/

	

	

	

	//Private methods, implementing AssociationEventHandler

	

	var AssociationEventHandler = Java.type('airspan.lte_ipg.AssociationWrapper.AssociationEventHandler');

	this.eventHandler = new AssociationEventHandler( {

		ReceivedPacket: function(eventSender , message , info) {

			//print('Received on ' + eventSender);

			var ASNPathHelper = Java.type('airspan.lte_ipg.ASNPathHelper');

			var PDU_type = message.getChosenFlag();

			switch (PDU_type) {

				case 1: //initiating message

				

				//initiating message

				var rootHelper = new ASNPathHelper(message);

				var procedureCode = rootHelper.search('choice.procedureCode').object.intValue();

				switch (procedureCode ) {

					case 6:

						//Setup request

						eventSender.send(parent.defaultPackets['setupResponse'] ,0,'response');

						print('Sent setup response');

						break;

					case 8: // configuration update

						eventSender.send(parent.defaultPackets['configUpdateResponse'],0,'response');

						//print('Sent configuration update response');

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

			//exception.printStackTrace();

		}

	}	

	);

	

	var ServerEventHandler = Java.type('airspan.lte_ipg.ProtocolServer.EventHandler'); // it's a java type

	this.acceptor = new ServerEventHandler({

		connectionAccepted : function (sctpChannel , protocol) {

			var name = sctpChannel.getRemoteAddresses().toArray()[0].toString();

			print('Accepted connection from ' + name + ' on  ' + parent.configuration['localIPAddress']);

			var protocolClone = parent.x2.clone();

			var AssociationWrapper = Java.type('airspan.lte_ipg.AssociationWrapper'); // it's a java type

			var newAssociation = new AssociationWrapper(name +'-'+parent.toString(), protocolClone  , sctpChannel , parent.eventHandler)

			parent.neighbours[name] = newAssociation;

		}

	});

}



//Public functions

//Gets a pointer to a GlobalENBID, sets it to our id

virtualENB.prototype.modifyENB_ID = function (GlobalENBID) {

	//Set pLMN-Identity

	var PLMNOctetString = GlobalENBID.getComponent(0);

	PLMNOctetString.setValue(stringToByteArray(this.configuration['PLMN']));

	

	//We assume the enb type is already set properly!!!

	

	//set the bitstring

	var bitstring = GlobalENBID.getComponent(1).getChosenValue();

	setBitString(bitstring , this.configuration['enb_id']);

}



//Gets a pointer to a ServedCell-Information , id of cell to set it to

virtualENB.prototype.configureCell= function (cellInfo , id) {

	var cell = cellInfo.getComponent(0);

	cell.getComponent(0).setValue(this.configuration['cells'][id]['pci']);

	cell.getComponent(1).getComponent(0).setValue(stringToByteArray(this.configuration['PLMN']));

	var eUTRANcellIdentifier = cell.getComponent(1).getComponent(1);

	cell.getComponent(2).setValue(stringToByteArray(this.configuration['cells'][id]['tac']));

	

	var finalIdentifier = this.configuration.enb_id * 256 + id;

	setBitString(eUTRANcellIdentifier , finalIdentifier);

	

	//first broadcast plmn

	cell.getComponent(3).getElement(0).setValue(stringToByteArray(this.configuration['PLMN']));

	

	//we assume it's already tdd

	var tdd = cell.getComponent(4).getChosenValue();

	tdd.getComponent(0).setValue(this.configuration['cells'][id]['EARFCN']);

}



//returns neighbour

virtualENB.prototype.connectTo= function (remoteHostname) {

	var InetSocketAddress = Java.type('java.net.InetSocketAddress');

	var localSockAddr = new InetSocketAddress( Java.type('java.net.InetAddress').getByName(this.configuration['localIPAddress']) , 0 ); // we need to go out on a random port

	var remoteSockAddr = new InetSocketAddress( Java.type('java.net.InetAddress').getByName(remoteHostname) , this.x2.port  );

	var AssociationWrapper = Java.type('airspan.lte_ipg.AssociationWrapper');

	

	var newAssociation = new AssociationWrapper(this.toString() + '-'+remoteHostname , this.x2 , remoteSockAddr , localSockAddr, this.eventHandler);

	this.neighbours[remoteHostname] = newAssociation;

	newAssociation.send(this.defaultPackets['setupRequest'] , 0 , "setup");

	

}



virtualENB.prototype.send= function (message) {

	

}



virtualENB.prototype.cellUp= function (id) {

	//TODO EyalC implement

}



virtualENB.prototype.cellDown= function (id) {

	//TODO EyalC implement	

}



virtualENB.prototype.stop= function () {

	ProtocolServer.getServer().stopServer(this.serverInstance);

	delete this.serverInstance;

}



virtualENB.prototype.start= function () {

	//Add it to ProtocolServer

	try {

		this.serverInstance = ProtocolServer.getServer().addServer(this.x2 , Java.type('java.net.InetAddress').getByName(this.configuration['localIPAddress'])  , this.acceptor );

	}

	catch (e) {

		print('Error binding on interface ' + this.configuration['localIPAddress']);

		throw e;

	}

	

}



virtualENB.prototype.toString = function () {

	return 'veNb:' + this.configuration['localIPAddress']  ;

}



/*end of Virtual eNodeB*/
var noExit = true;
var outgoingMessageCnt = 0;
var initialContextSetupResponseType = Java.type("s1ap.s1ap_pdu_contents.InitialContextSetupResponse").getStaticTypeInfo();

function displayTime() {
	
	return "";
}
/*
 * Callbacks prototype:
 * function receivedMessage(AssociaionWrapper incomingSocket, AssociaionWrapper outgoingSocket,AbstractData message , info)     : bool - true for "should forward",false for "ignore"
 * function receivedRawMessage( AssociaionWrapper incomingSocket, AssociaionWrapper outgoingSocket,ByteBuffer rawMessage, info) : bool - true for "should forward",false for "ignore"
 * */

function ManInTheMiddle(listenIP, remoteIP, protocol, callbacks) {
	// Members
	this.flag = true;
	this.SocketMap = {};
	this.SocketMapLock = new UsefulJavaClasses.ReentrantLock();
	this.listenIPString = listenIP;
	this.listenIPInetAddr = UsefulJavaClasses.InetAddress.getByName(this.listenIPString);
	this.remoteIP = remoteIP;
	this.protocol = protocol.clone();
	this.serverInstance = null;
	//this.Window = new IPG.guiImplementation(null);
	//this.Window.setTitle('ManInTheMiddle - ' + protocol.toString() + ' from ' + listenIP + ' to ' + remoteIP);
	this.callbacks = callbacks;

	// Callbacks
	this.enbEventHandler = new IPG.AssociationEventHandler(
			{
				mim : this,
				// this is for packet that received
				ReceivedPacket : function(eventSender, message, info) {
					//print(displayTime());
				//	print('\nGOT MESSAGE FROM: ' + eventSender + ' : \n' /*
																		 //* +
																	//	 * message
																	//	 */
				//			+ 'info is ' + info);
					this.mim.SocketMapLock.lock();
					var dest = this.mim.SocketMap[eventSender];
					this.mim.SocketMapLock.unlock();

				//	print('DESTINATION is: ' + dest);
					if ((dest === undefined) || (dest.isConnected() == false))
						print('NOT Sending Message');
					else {
						var shouldForward = true;
						if (this.mim.callbacks) {
							shouldForward = this.mim.callbacks.receivedMessage(eventSender , dest , message,info);
						}
						if (shouldForward) {
							dest.send(message, info.streamNumber(), '');
						}
					}
						
				},
				// this is for raw packet that received
				// ReceivedRawPacket: function(AssociationWrapper eventSender ,
				// ByteBuffer message , MessageInfo info){
				ReceivedRawPacket : function(eventSender, message, info) {
				//	print('\nGOT RAW MESSAGE FROM: ' + eventSender + ' : \n' /*
																	//	 * +
																//		 * message
																//	 */
				//			+ 'info is ' + info);
					this.mim.SocketMapLock.lock();
					var dest = this.mim.SocketMap[eventSender];
					this.mim.SocketMapLock.unlock();
				//	print('DESTINATION is: ' + dest);
					if (dest === undefined) {
						eventSender.disconnect();
						return;
					}
					if (dest.isConnected() == false)
						print('NOT Sending Message');
					else {
						var shouldForward = true;
						if (this.mim.callbacks) {
							shouldForward = this.mim.callbacks.receivedRawMessage(eventSender , dest , message,info);
						}
						if (shouldForward) {
							dest.sendRaw(message, info.streamNumber(), 'Sent_MSG'
									+ outgoingMessageCnt++);
						}
					}
				},

				SentPacket : function(eventSender, message, messageName, info) {
					// print('Sent on ' + eventSender);
				},

				SentRawPacket : function(eventSender, message, messageName,
						info) {
					// print('Sent on ' + eventSender);
				},

				Disconnected : function(eventSender) {
					print('Disconnect on ' + eventSender);
					this.mim.SocketMapLock.lock();
					var dest = this.mim.SocketMap[eventSender];
					this.mim.SocketMapLock.unlock();

					if ((dest !== undefined) && (dest.isConnected()))
						dest.disconnect();
				},

				ErrorOnReceive : function(eventSender, exception) {
					//print(displayTime());
					print('Got error on ' + eventSender + ': '
							+ exception.getMessage());
					// exception.printStackTrace();
				}
			});

	// handler of accepted connection, as a result, new connection should be
	// created to the MME and link between them
	this.acceptor = new IPG.ServerEventHandler(
			{
				mim : this,
				connectionAccepted : function(sctpChannel, protocol) {
					var name = sctpChannel.getRemoteAddresses().toArray()[0]
							.toString();
				//	print(protocol);
				//	print(name);
				//	print('Accepted connection from ' + name + ' on  '
				//			+ this.mim.listenIPString);
					var enbProtocolClone = this.mim.protocol.clone();
				//	print('creating association ' + name + '-' + ', '
				//			+ enbProtocolClone + ', ' + sctpChannel + ', '
				//			+ this.mim.enbEventHandler);
					this.mim.SocketMapLock.lock();

					var newAssociationEnb = new IPG.AssociationWrapper(name
							+ '-ENB-', enbProtocolClone, sctpChannel,
							this.mim.enbEventHandler);
					newAssociationEnb.setDecodingMessages(false);
					//this.mim.Window.addAssociationToList(newAssociationEnb);

					var incomingStreamCount = sctpChannel.association().maxInboundStreams();
					var outgoingStreamCount = sctpChannel.association().maxOutboundStreams();
					var mmeProtocolClone = this.mim.protocol.clone();

					
					var mmeSockAddr = new UsefulJavaClasses.InetSocketAddress(Java.type(
							'java.net.InetAddress').getByName(this.mim.remoteIP),
							protocol.port);
					try {
						var newAssociationMme = new IPG.AssociationWrapper(name
								+ '-MME-', mmeProtocolClone, mmeSockAddr, new UsefulJavaClasses.InetSocketAddress(this.mim.listenIPInetAddr,0),
								incomingStreamCount,outgoingStreamCount,
								this.mim.enbEventHandler);
					} catch (e) {
						print('Error creating mme connection' + e.getMessage());
						newAssociationEnb.disconnect();
						this.mim.SocketMapLock.unlock();
						return;
					}

					newAssociationMme.setDecodingMessages(false);
					//this.mim.Window.addAssociationToList(newAssociationMme);

				//	print('ENB ' + newAssociationEnb + ' is connected to '
				//			+ newAssociationMme);
					this.mim.SocketMap[newAssociationEnb] = newAssociationMme;
					this.mim.SocketMap[newAssociationMme] = newAssociationEnb;
					this.mim.SocketMapLock.unlock();

					print('ENB ' + this.mim.SocketMap[newAssociationMme]
							+ ' is connected to '
							+ this.mim.SocketMap[newAssociationEnb]);
					// parent.neighbours[name] = newAssociation;
				}
			});

	// Methods
	this.start = function() {
		//setTimeout(exitTest,5*60*1000);
		//this.Window.setServerVisible(false);
		//this.Window.setVisible(false);
		try {
			this.serverInstance = IPG.ProtocolServer.getServer()
					.addServer(
							this.protocol,
							UsefulJavaClasses.InetAddress
									.getByName(this.listenIPString), this.acceptor);
		} catch (e) {
			print('Error binding on interface ');
			// print('Error binding on interface ' +
			// this.configuration['localIPAddress']);
			throw e;
		}
	};

	this.stop = function() {
		IPG.ProtocolServer.getServer().stopServer(this.serverInstance);
		this.serverInstance = null;
	}
}

function myFilter(protocol) {

	this.protocol = protocol.clone();
	this.counterEstablish = 0;
	
	this.sleep = function(milliseconds) {
  		var start = new Date().getTime();
  		for (var i = 0; i < 1e8; i++) {
    			if ((new Date().getTime() - start) > milliseconds){
      				break;
    			}
  		}
	}

	this.isAnInterestingMessage = function (incomingSocket,message) {
		var setupRequest = 17;
		if(message.hasSuccessfulOutcome()){
			if(message.getSuccessfulOutcome().getProcedureCode().intValue() == setupRequest){
				return true;
			}
			else{
				return false;
			}
		}
		else{
			return false;
		}
	}
	
	this.receivedMessage = function (incomingSocket, outgoingSocket, message, info) {

		/*Message was completely decoded*/
		if (!this.isAnInterestingMessage(incomingSocket,message) ){
			return true; // forward this message
		}

		this.counterEstablish++;
		if(this.counterEstablish==2){
			setTimeout(printExit,15*1000);
		}
		return true;
	}
	
	this.receivedRawMessage = function ( incomingSocket, outgoingSocket, rawMessage, info) {
		
		/*Decode message partially*/
		this.protocol.setPartialDecoding(true);
		var partialMessage = this.protocol.decodeMessage(rawMessage , false);
		rawMessage.position(0);
		this.protocol.setPartialDecoding(false);

		/*We read from the buffer, we have to return to original position*/
		/*Message was partially decoded*/
		if (!this.isAnInterestingMessage(incomingSocket,partialMessage) ) {
			return true;		
		}
		
		this.counterEstablish++;
		if(this.counterEstablish==2){
			setTimeout(printExit,15*1000);
		}
		return true;
	}
	
}

function printExit(){
	print('EnodeB_Reestablished_To_MME');
	exit();
}

function test() {
	
    var flag = false;
	if(typeof scriptParam=='undefined'){
		print('ERROR: No parameters');
		exit();
	}
	
	var temp = JSON.parse(scriptParam);
	if(typeof temp.fakeIP=='undefined'){
		print('No fake IP');
		flag = true;
	}
	/*if(typeof temp.realMME == 'undefined'){
		print('No real MME ip');
		flag = true;
	}*/
	if(flag){
		print('ERROR with parameters');
		exit();
	}
	var myFakeIP = temp.fakeIP;
	//var myRealMME = temp.realMME;
	
	var mim = new ManInTheMiddle(myFakeIP, '172.21.52.4', IPG.s1ap,new myFilter(IPG.s1ap.clone()));
	mim.start();

}

test();

var Thread = Java.type('java.lang.Thread');
var noExit = true;
var outgoingMessageCnt = 0;
var initialContextSetupResponseType = Java.type("s1ap.s1ap_pdu_contents.InitialContextSetupResponse").getStaticTypeInfo();
var ctr = 0;
var myS1Ip;
/*
 * Callbacks prototype:
 * function receivedMessage(AssociaionWrapper incomingSocket, AssociaionWrapper outgoingSocket,AbstractData message , info)     : bool - true for "should forward",false for "ignore"
 * function receivedRawMessage( AssociaionWrapper incomingSocket, AssociaionWrapper outgoingSocket,ByteBuffer rawMessage, info) : bool - true for "should forward",false for "ignore"
 * */

 function displayTime() {
	return "";
}

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
	this.callbacks = callbacks;

	// Callbacks
	this.enbEventHandler = new IPG.AssociationEventHandler(
			{
				mim : this,
				// this is for packet that received
				ReceivedPacket : function(eventSender, message, info) {
				//print(displayTime());
				//print('\nGOT MESSAGE FROM: ' + eventSender + ' : \n' +  message + 'info is ' + info);
					this.mim.SocketMapLock.lock();
					var dest = this.mim.SocketMap[eventSender];
					this.mim.SocketMapLock.unlock();

				//print('DESTINATION is: ' + dest);
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
				
				ReceivedRawPacket : function(eventSender, message, info) {
					//print('\nGOT RAW MESSAGE FROM: ' + eventSender + ' : \n' +  message + 'info is ' + info);
					this.mim.SocketMapLock.lock();
					var dest = this.mim.SocketMap[eventSender];
					this.mim.SocketMapLock.unlock();
					//print('DESTINATION is: ' + dest);
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
					print('Got error on ' + eventSender + ': '
							+ exception.getMessage());
				}
			});

	// handler of accepted connection, as a result, new connection should be
	// created to the MME and link between them
	this.acceptor = new IPG.ServerEventHandler(
			{
				mim : this,
				connectionAccepted : function(sctpChannel, protocol) {
					var name = sctpChannel.getRemoteAddresses().toArray()[0].toString();
					var enbProtocolClone = this.mim.protocol.clone();
					this.mim.SocketMapLock.lock();
					var newAssociationEnb = new IPG.AssociationWrapper(name
							+ '-ENB-', enbProtocolClone, sctpChannel,
							this.mim.enbEventHandler);
					newAssociationEnb.setDecodingMessages(false);
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
					this.mim.SocketMap[newAssociationEnb] = newAssociationMme;
					this.mim.SocketMap[newAssociationMme] = newAssociationEnb;
					this.mim.SocketMapLock.unlock();
					print('ENB ' + this.mim.SocketMap[newAssociationMme]
							+ ' is connected to '
							+ this.mim.SocketMap[newAssociationEnb]);
				}
			});

	// Methods
	this.start = function() {
		try {
			this.serverInstance = IPG.ProtocolServer.getServer()
					.addServer(
							this.protocol,
							UsefulJavaClasses.InetAddress
									.getByName(this.listenIPString), this.acceptor);
		} catch (e) {
			print('Error binding on interface ');
			throw e;
		}
	};

	this.stop = function() {
		IPG.ProtocolServer.getServer().stopServer(this.serverInstance);
		this.serverInstance = null;
	}
}

function myFilter(protocol) {
	this.stopper = new Date().getTime();
	this.map = {};
	this.counterMessage = 0;
	this.protocol = protocol.clone();
	this.flag = true;
	this.socketToSend;
	this.infoToSend;
	
	this.sleep = function(milliseconds) {
  		var start = new Date().getTime();
  		for (var i = 0; i < 1e8; i++) {
    			if ((new Date().getTime() - start) > milliseconds){
      				break;
    			}
  		}
	}
	
	this.receivedMessage = function (incomingSocket, outgoingSocket, message, info) {
		this.flag = false;
		print('message received from ' + incomingSocket.toString());
		if(incomingSocket.toString().contains(myS1Ip)){
			print('Ip is ' + myS1Ip + ' as expected');
			if(message.hasInitiatingMessage())
			{
				print('message is initiating message!');
				if(message.getInitiatingMessage().getProcedureCode().intValue()== 17)
				{
					print('procedure code is 17!');
					this.flag = true;
				}
			}
		}
		else
			print('Ip is not ' + myS1Ip + ' ignoring packet');
		this.map = {};
		if(this.flag == true)
		{
			this.flag = false;
			this.socketToSend = incomingSocket;
			this.infoToSend = info;
			this.sendS1SetupResponseMessage();			
			print('Send ETWS');
			setTimeout(this.sendETWSMessage.bind(this),5000);
		}
		return false;
	}
	
	this.receivedRawMessage = function ( incomingSocket, outgoingSocket, rawMessage, info) {
		this.flag = false;
		print('message received from ' + incomingSocket.toString());
		/*Decode message partially*/
		this.protocol.setPartialDecoding(true);
		var partialMessage = this.protocol.decodeMessage(rawMessage , false);
		rawMessage.position(0);
		this.protocol.setPartialDecoding(false);
		if(incomingSocket.toString().contains(myS1Ip)){
			print('Ip is ' + myS1Ip + ' as expected');
			if(partialMessage.hasInitiatingMessage())
			{
				print('message is initiating message!');
				if(partialMessage.getInitiatingMessage().getProcedureCode().intValue()== 17)
				{
					print('procedure code is 17!');
					this.flag = true;
				}
			}
		}
		else
			print('Ip is not ' + myS1Ip + ' ignoring packet');
		this.map = {};
		if(this.flag == true){
			this.flag = false;
			this.socketToSend = incomingSocket;
			this.infoToSend = info;
			this.sendS1SetupResponseMessage();
			print('Send ETWS');
			setTimeout(this.sendETWSMessage.bind(this),5000);
		}
		return false;
	}
	
	this.sendS1SetupResponseMessage = function(outSocket,info){
		//load existing packet
		s1SetupRequestPacket = this.protocol.loadPERPacket("S1_Response.s1p");
     	print('\nSending Setup Response');
		this.socketToSend.send(s1SetupRequestPacket,this.infoToSend.streamNumber(),' ');
	}
	
	this.sendETWSMessage = function(outSocket,info){
		//load existing packet
		ETWSPacket = this.protocol.loadPERPacket("Primary_Secondary_with_concurrentIE.s1p");
     	print('\nSending ETWS message  #:' +(ctr+=1));
		this.socketToSend.send(ETWSPacket,this.infoToSend.streamNumber(),' ');
	}
}

function sleep(millis)
{
    var date = new Date();
    var curDate = null;
    do { curDate = new Date(); }
    while(curDate-date < millis);
}

function javascript_abort()
{
   throw new Error('This is not an error. This is just to abort javascript');
}

function test() {
	print('Begin JS Script');
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
	if(typeof temp.realMME == 'undefined'){
		print('No real MME ip');
		flag = true;
	}
	if(typeof temp.s1Ip == 'undefined'){
		print('No Enb S1 ip');
		flag = true;
	}
	if(flag){
		print('ERROR with parameters');
		exit();
	}
	print('Parameters Loaded');
	var myFakeIP = temp.fakeIP;
	var myRealMME = temp.realMME;
	myS1Ip = temp.s1Ip;
	var mim = new ManInTheMiddle(myFakeIP,myRealMME, IPG.s1ap,new myFilter(IPG.s1ap.clone()));
	mim.start();
	sleep(20000);
	mim.stop();
	javascript_abort();
}

test();
package application;

import java.io.File;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.scene.image.Image;
import network.Connection;
import network.FileManager;
import network.JRTVPacket;
import network.Router;
import network.SeqAckTable;
import network.Update;
import security.RSA;

public class Controller extends Thread {

	/**
	 * GUI of the program.
	 */
	private View view;
	
	/**
	 * Connection used to send data using a multicastsocket.
	 */
	private Connection connection;
	
	/**
	 * InetAddress used for multicasting needed for creating a datagram.
	 */
	private InetAddress multicastIAddress;
	
	/**
	 * Localaddress which will be set to the real local address in the initializing method.
	 */
	public int localIAddress = 0;
	
	/**
	 * multicastaddress used in the JRTVPacket.
	 */
	public static int multicastAddress = iptoInt("224.0.0.2");
	
	/**
	 * Router used for determining where the packets need to be send.
	 */
	private Router router = new Router(this);
	
	/**
	 * Updater used to send update packages.
	 */
	private Update update;
	
	/**
	 * Boolean value true if the system is still initializing.
	 */
	private boolean settingUp = true;
	
	/**
	 * Unique string used in the determining of the local ip Address.
	 */
	private String initString;
	
	/**
	 * Table used to determine which sequence and acknowledgement numbers to use.
	 */
	private SeqAckTable seqAckTable = new SeqAckTable(this);
	
	/**
	 * FileManager that is used for sending and receiving files.
	 */
	private FileManager fileManager = new FileManager(this);
	
	/**
	 * Name of the client used for updating other clients.
	 */
	private String clientName = "Anonymous";
	
	
	//======================================================================================================================
	//||                                                Constructor:                                                      ||  
	//======================================================================================================================
	
	/**
	 * given a view it will startup all logic of the program and start receiving and sending packets.
	 * @param view GUI which to use.
	 */
	public Controller(View view) {
		this.setName("Controller");
		this.view = view;
		
		String address = "224.0.0.2";
		try {
			multicastIAddress = InetAddress.getByName(address);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		int port = 2000;
		connection = new Connection(port, address, this);
	}
	
	//======================================================================================================================
	//||                                          Initializing method:                                                    ||  
	//======================================================================================================================

	/**
	 * This method determines the ip address of the local machine.
	 * First it will look in the network interfaces looking for 192.168.5. as we used this prefix for communication on campus.
	 * Secondly if not found using that method it will send out a string and if it receives that exact string it knows the source is the local address.
	 */
	private void setupIP() {
		boolean found = false;
		Enumeration<NetworkInterface> interfaces;
		try {
			System.out.println("starting cycling over networks");
			interfaces = NetworkInterface.getNetworkInterfaces();
			
			while (interfaces.hasMoreElements()) {
				NetworkInterface i = interfaces.nextElement();
				Enumeration<InetAddress> ips = i.getInetAddresses();
				while (ips.hasMoreElements()) {
					InetAddress address = ips.nextElement();
					if (address.toString().contains("192.168.5.")) {
						localIAddress = iptoInt(address.toString().replace("/", ""));
						found = true;
						settingUp = false;
					}
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}
		if (!found) {
			initString = randomString();
			while (settingUp) {
				DatagramPacket data;
				if (connection.getFirstInQueue() != null) {
					data = connection.getFirstInQueue();
					if (new JRTVPacket(data.getData()).getMessage().equals(initString)) {
						InetAddress add = data.getAddress(); 
						String address = add.toString();
						address = address.replace("/", "");
						localIAddress = iptoInt(address);
						settingUp = false;
					}
				}
				try {
					sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		view.start(localIAddress);
	}
	
	/**
	 * Given a String it with format \0.0.0.0 it will determine the integer representation.
	 * @param ipaddress String with format \0.0.0.0
	 * @return integer representation of the given ip address.
	 */
	private static int iptoInt(String ipaddress) {
		int[] ip = new int[4];
		String[] parts = ipaddress.split("\\.");

		for (int i = 0; i < 4; i++) {
		    ip[i] = Integer.parseInt(parts[i]);
		}
		long ipNumbers = 0;
		for (int i = 0; i < 4; i++) {
		    ipNumbers += ip[i] << (24 - (8 * i));
		}
		return (int) ipNumbers;
	}
	
	/**
	 * Genereates a random string used in the IPSetup method.
	 * @return random string.
	 */
	public String randomString() {
		SecureRandom random = new SecureRandom();
		return new BigInteger(130, random).toString(32);
	}
	
	//======================================================================================================================
	//||                                         Data sending methods:                                                    ||  
	//======================================================================================================================
	
	/**
	 * This method starts the handling of incoming packets and the sending of encrypted packets.
	 * This method also decrypts all messages in the received packet queue. 
	 */
	public void run() {
		update = new Update(this); 	
		update.setDaemon(true);
		update.start();
		setupIP();
		while (true) {
			DatagramPacket data;
			if (connection.getFirstInQueue() != null) {
				data = connection.getFirstInQueue();
				handleMessage(new JRTVPacket(data.getData()), false);
			}
			sendEncryptionMessages();
			decryptMessages();
			if (!filesToBeSend.keySet().isEmpty()) {
				List<File> keys = new ArrayList<File>();
				keys.addAll(filesToBeSend.keySet());
				for (int i = 0; i < keys.size(); i++) {
					File file = keys.get(i);
					fileManager.sendFile(file, filesToBeSend.get(file));
					filesToBeSend.remove(file);
				}
			}
			
			try {
				sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	//======================================================================================================================
	//||                                         Data sending methods:                                                    ||  
	//======================================================================================================================
	
	/**
	 * All packets that have to be send and send to their destination.
	 */
	private List<JRTVPacket> outgoingEncryptionPackets = new ArrayList<JRTVPacket>();
	
	/**
	 * All packets that have been received and need to be decrypted before being handled.
	 */
	private List<JRTVPacket> incomingEncryptionPackets = new ArrayList<JRTVPacket>();
	
	/**
	 * Checks if client exists in the forwardingtable and puts data in a JRTVPacket before sending it to the next method.
	 * @param client destination of the message.
	 * @param message String of data to be send to a client.
	 */
	public void sendMessage(String client, String message) {
		if (router.getIP(client) == null) {
			view.error("Recipient not valid!");
		} else {
			JRTVPacket packet = new JRTVPacket(message);
			packet.setNormal(true);
			sendPacket(client, packet);
		}
	}
	
	/**
	 * converts the client string to a ip of the destination
	 * @param client String name of a client.
	 * @param packet JRTVpacket that needs to be send.
	 */
	public void sendPacket(String client, JRTVPacket packet) {
		int destination = router.getIP(client);
		if (destination == Controller.multicastAddress) {
			packet.setBroadcasted(true);
		}
		sendPacket(destination, packet);
	}
	
	/**
	 * Depending on the ip given it will either add the packet to the to be encrypted list.
	 * Or it wil sign the packet using RSA and send the packet.
	 * @param client integer ip of the destination.
	 * @param packet JRTVPacket that needs to be send.
	 */
	public void sendPacket(int client, JRTVPacket packet) {
		packet.setSource(localIAddress);
		packet.setDestination(client);
		
		if (packet.isNormal() || packet.isDiffie() || packet.isFile()) {
			packet.setSeqnr(seqAckTable.getNextSeq(packet.getDestination()));
			seqAckTable.registerSendPacket(packet);
		}
		packet.setNextHop(router.getNextHop(packet.getDestination()));
		
		if (packet.getDestination() != multicastAddress 
									&& !packet.isDiffie() 
										&& !packet.isAck()
											&& !packet.isFile()) {
			outgoingEncryptionPackets.add(packet);
		} else {
			//RSA Signing
			byte[] first = packet.getByteMessage();
			byte[] second = RSA.encrypt(new String(RSA.hash(first)), 
											RSA.getPrivateKey(localIAddress));
			byte[] message = new byte[first.length + second.length];
			
			System.arraycopy(first, 0, message, 0, first.length);
			System.arraycopy(second, 0, message, message.length - second.length, second.length);
			
			packet.setByteMessage(message);
			packet.setHashPayload(second.length);
			
			sendPacket(packet);
		}
	}
	
	/**
	 * Encrypts a packet and sends the packet to the person if it has the key to encrypt.
	 * Otherwise it will send out a diffie packet initiating a diffie Hellman handshake.
	 */
	public void sendEncryptionMessages() {
		for (int i = 0; i < outgoingEncryptionPackets.size(); i++) {
			JRTVPacket packet = outgoingEncryptionPackets.get(i);
			if (router.hasEncryptionKey(packet.getDestination())) {
				packet = router.getEncryption(packet.getDestination())
									.encrypt(packet, RSA.getPrivateKey(localIAddress));
				outgoingEncryptionPackets.remove(packet);
				sendPacket(packet);
				break;
			} else if (!router.isSettingUpDiffie(packet.getDestination()) 
							 && router.getForwardingTable().getTable()
							 		.containsKey(packet.getDestination())) {
				router.setupDiffie(packet.getDestination());
				break;		
			}
		}
	}
	
	/**
	 * Decrypts incoming encrypted messages and passes them on the the handle message.
	 */
	private void decryptMessages() {
		for (int i = 0; i < incomingEncryptionPackets.size(); i++) {
			JRTVPacket packet = incomingEncryptionPackets.get(i);
			if (packet.getDestination() == multicastAddress 
							|| packet.isDiffie() 
								|| packet.isAck() 
									|| packet.isFile()) {
				byte[] message2 = packet.getByteMessage();
				byte[] second2 = new byte[packet.getHashPayload()];
				
				byte[] first2 = new byte[message2.length - packet.getHashPayload()]; 
				
				System.arraycopy(message2, 0, first2, 0, first2.length);
				System.arraycopy(message2, message2.length - second2.length, 
														second2, 0, second2.length);
				
				String decrypted = RSA.decrypt(second2, 
											RSA.getPublicKey(packet.getSource()));
				
				if (decrypted.equals(new String(RSA.hash(first2)))) {
					packet.setByteMessage(first2);
				} else {
					packet.setMessage("Sender not verified: \"" + new String(first2) + "\"");
				}
				
				incomingEncryptionPackets.remove(packet);
				i--;
				handleMessage(packet, true);
			} else {
				if (router.hasEncryptionKey(packet.getSource())) {
					packet = router.getEncryption(packet.getSource())
								.decrypt(packet, RSA.getPublicKey(packet.getSource()));
					incomingEncryptionPackets.remove(packet);
					handleMessage(packet, true);
				} 
			}
		}
	}	
	
	/**
	 * Wraps a packet into a datagrampacket and passes it on to the connection.
	 */
	private void sendPacket(JRTVPacket packet) {
		packet.setNextHop(router.getNextHop(packet.getDestination()));
		DatagramPacket data = new DatagramPacket(packet.toByteArray(), 
								packet.toByteArray().length, getMulticastIAddress(), 2000);
		connection.send(data);
	}
	
	/**
	 * Passes the packet on to the retransmit method.
	 * @param packet packet to be retransmitted.
	 */
	public void retransmit(JRTVPacket packet) {
		retransmit(packet, packet.getDestination());
	}
	
	/**
	 * Very similair to sendpacket method except for not altering any information like sequence numbers.
	 * @param packet packet to be retransmitted.
	 * @param destination integer ip address of the destination.
	 */
	public void retransmit(JRTVPacket packet, int destination) {
		packet.setSource(localIAddress);
		packet.setDestination(destination);
		
		if (packet.isNormal() || packet.isDiffie() || packet.isFile()) {
			seqAckTable.registerSendPacket(packet);
		}
		
		if (packet.getDestination() != multicastAddress && !packet.isDiffie() && !packet.isFile()) {
			packet.setNextHop(router.getNextHop(packet.getDestination()));
			outgoingEncryptionPackets.add(packet);
		} else {
			//RSA Signing
			byte[] first = packet.getByteMessage();
			byte[] second = RSA.encrypt(new String(RSA.hash(first)), 
												RSA.getPrivateKey(localIAddress));
			byte[] message = new byte[first.length + second.length];
			
			System.arraycopy(first, 0, message, 0, first.length);
			System.arraycopy(second, 0, message, message.length - second.length, second.length);
			
			packet.setByteMessage(message);
			packet.setHashPayload(second.length);
			
			sendPacket(packet);
		}
	}
	
	/**
	 * This method is used to simply alter the nexthop and relay a packet.
	 * @param packet packet to be relayed.
	 */
	public void relay(JRTVPacket packet) {
		packet.setNextHop(router.getNextHop(packet.getDestination()));
		sendPacket(packet);
	}
	
	/**
	 * This method sends an acknowledgement given a received packet.
	 * @param packet packet that needs to be acknowledged.
	 */
	private void sendAck(JRTVPacket packet) {
		JRTVPacket p = new JRTVPacket("ACK");
		p.setSource(localIAddress);
		p.setDestination(packet.getSource());
		p.setAcknr(packet.getSeqnr());
		p.setAck(true);

		if (p.getDestination() != multicastAddress) {
			p.setNextHop(router.getNextHop(p.getDestination()));
		}
		
		sendPacket(p.getDestination(), p);
	}
	
	/**
	 * Passes the packet on to the sendPacket method.
	 * @param packet packet to be broadcasted.
	 */
	public void broadcastPacket(JRTVPacket packet) {
		sendPacket("Anonymous", packet);
	}

	/**
	 * Called by the view to send a message to a person.
	 * @param client destination of the message
	 * @param message String message to be send to the destination.
	 */
	public void receiveFromView(String client, String message) {
		JRTVPacket packet = new JRTVPacket(message);
		packet.setNormal(true);
		sendPacket(client, packet);
	}
	
	/**
	 * This map stores all files that have to be send.
	 */
	Map<File, Integer> filesToBeSend = new HashMap<File, Integer>();
	
	/**
	 * Adds a certain file to the filesToBeSend list.
	 * @param file File to be send to the destination.
	 * @param client Destination of the file.
	 */
	public void sendFile(File file, String client) {
		filesToBeSend.put(file, router.getIP(client));
	}
	
	//======================================================================================================================
	//||                                     Handling incoming messages methods:                                          ||  
	//======================================================================================================================

	/**
	 * This method handles all incoming packets and determines to ignor, relay, or handle these packet.
	 * It will also make sure to decrypt the messages first and send an acknowledgment packet if needed.
	 * If it needs to be handled it will be send to the corresponding method.
	 * @param packet packet that needs to be handled by the method.
	 * @param decrypted Boolean value determining if the packet has been decrypted.
	 */
	public void handleMessage(JRTVPacket packet, boolean decrypted) {
		if (packet.getSource() != localIAddress) {
			if (packet.getNextHop() == localIAddress 
								&& packet.getDestination() != localIAddress 
										&& packet.getDestination() != multicastAddress) {
				relay(packet);
			} else {
				if (packet.getDestination() == localIAddress 
								|| packet.getDestination() == multicastAddress) {
					if (!decrypted) { // && !packet.isAck() && !packet.isUpdate()
						incomingEncryptionPackets.add(packet);
						if (packet.isNormal() || packet.isDiffie() || packet.isFile()) {
							sendAck(packet);
						}
					} else {
						if (!seqAckTable.isReceivedSeqNr(packet.getSource(), packet.getSeqnr()) 
																			|| packet.isUpdate()) {
							seqAckTable.addReceivedSeqNr(packet.getSource(), packet.getSeqnr());
							if (packet.isNormal()) {
								handleNormal(packet);
							} else if (packet.isUpdate()) {
								handleUpdate(packet);
							} else if (packet.isFile()) {
								handleFile(packet);
							} else if (packet.isAck()) {
								handleAck(packet);
							} else if (packet.isDiffie()) {
								handleDiffie(packet);
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * This method passes on normal packets to the view to be displayed.
	 * @param packet packet which payload needs to be showed in the view.
	 */
	public void handleNormal(JRTVPacket packet) {
		String message = packet.getMessage();
		view.addMessage(router.getName(packet.getSource()), message, packet.isBroadcasted());
	}
	
	/**
	 * Passes the packet on to the Router which will procces the update.
	 * @param packet update packet
	 */
	private void handleUpdate(JRTVPacket packet) {
		router.processUpdate(packet);
	}
	
	/**
	 * This method will pass the packet on to the filemanager to be stored and build if needed.
	 * @param packet Packet carrying a file packet.
	 */
	private void handleFile(JRTVPacket packet) {
		fileManager.handleFilePacket(packet);
	}
	
	/**
	 * passes a Acknowledgement to the seqAckTable to be registered.
	 * Or it will pass the packet to the router if it is a diffie acknowledgement.
	 * @param packet packet to be passed on.
	 */
	private void handleAck(JRTVPacket packet) {
		if (packet.isDiffie()) {
			router.processDiffie(packet);
		} else {
			seqAckTable.registerAckPacket(packet);
		}
	}
	
	/**
	 * This method passes the diffie packet to the router to be processed.
	 * @param packet packet to be processed.
	 */
	private void handleDiffie(JRTVPacket packet) {
		router.processDiffie(packet);
	}
	
	//======================================================================================================================
	//||                                       view interaction methods:                                                  ||  
	//======================================================================================================================
	
	/**
	 * Adds a recipient to the available recipients in the view.
	 * @param recipient recipient to be added to the view.
	 */
	public void addRecipientToView(String recipient) {
		view.addRecipient(recipient);
	}
	
	/**
	 * Removes a recipient from the available recipients in the view.
	 * @param recipient recipient to be removed.
	 */
	public void removeRecipientToView(String recipient) {
		view.removeRecipient(recipient);
	}
	
	/**
	 * this method passes the message on to the view after converting the ip to the name of the person.
	 * @param message message to be passed on to the view.
	 * @param client ip address of the client that send the message.
	 */
	public void addMessageToView(String message, int client) {
		view.addMessage(router.getName(client), message, false);
	}
	
	/**
	 * Forwards a received file to the view to be shown or show a message to say it has arrived.
	 * @param file Arrived file
	 * @param source ip address of the source who send the file.
	 */
	public void showFile(File file, int source) {
		String name = file.getName();
		if (name.contains(".png") || name.contains(".jpg") || name.contains(".gif")) {
			Image image;
			image = new Image("file:" + file.getName(), true);
			view.addImage(image, router.getName(source));
		}
	}
	
	//======================================================================================================================
	//||                                           get/set methods:                                                       ||  
	//======================================================================================================================
	
	public void setClientName(String clientName) {
		this.clientName = clientName;
	}
	
	public String getClientName() {
		return clientName;
	}
	
	public InetAddress getMulticastAddress() {
		return multicastIAddress;
	}
	
	public String getNameBySource(int source) {
		return router.getName(source);
	}
	
	public InetAddress getMulticastIAddress() {
		return multicastIAddress;
	}

	public int getLocalIAddress() {
		return localIAddress;
	}
	
	public Map<Integer, Map<Integer, Integer>> getForwardingTable() {
		return router.getTable();
	}
	
	public boolean getSettingUp() {
		return settingUp;
	}
	
	public String getInitString() {
		return initString;
	}
	
	public Router getRouter() {
		return router;
	}
	
	public FileManager getFileManager() {
		return fileManager;
	}
}

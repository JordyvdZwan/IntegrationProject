package application;

import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.Key;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;

import network.Connection;
import network.JRTVPacket;
import network.Router;
import network.SeqAckTable;
import network.Update;
import security.CreateEncryptedSessionPacket;
import security.RSA;

public class Controller extends Thread {

	private View view;
	private Connection connection;
	private InetAddress multicastIAddress;
	public int localIAddress = 0;
	public static int multicastAddress = IPtoInt("224.0.0.2");
	private Router router = new Router(this);
	private Update update;
	private boolean settingUp = true;
	private String initString;
	private SeqAckTable seqAckTable = new SeqAckTable(this);
	private InetAddress localInetAddress;
	
	public InetAddress getMulticastIAddress() {
		return multicastIAddress;
	}

	public int getLocalIAddress() {
		return localIAddress;
	}

	private String clientName = "Anonymous";
	
	public Controller(View view) {
		this.setName("Controller");
		this.view = view;
		
		String address = "224.0.0.2";
		try {
			multicastIAddress = InetAddress.getByName(address);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int port = 2000;
		connection = new Connection(port, address, this);
	}
	
	public InetAddress getLocalInetAddress() {
		return localInetAddress;
	}
//	.  TODO
	private void setupIP() {
		boolean found = false;
		Enumeration<NetworkInterface> interfaces;
		try {
			interfaces = NetworkInterface.getNetworkInterfaces();
			
			while (interfaces.hasMoreElements()) {
				NetworkInterface i = interfaces.nextElement();
				Enumeration<InetAddress> ips = i.getInetAddresses();
				while (ips.hasMoreElements()) {
					InetAddress address = ips.nextElement();
					if (address.toString().contains("192.168.5.")) {
						localIAddress = IPtoInt(address.toString().replace("/", ""));
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
				if((data = connection.getFirstInQueue()) != null) {
					JRTVPacket p = new JRTVPacket(data.getData());
					if (new JRTVPacket(data.getData()).getMessage().equals(initString)) {
						InetAddress add = data.getAddress(); 
						String address = add.toString();
						address = address.replace("/", "");
						localIAddress = IPtoInt(address);
						localInetAddress = data.getAddress();
						settingUp = false;
					}
				}
				try {
					this.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		view.start(localIAddress);
	}
	
	private static int IPtoInt(String ipaddress) {
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
	
	public Map<Integer,Map<Integer, Integer>> getForwardingTable() {
		return router.getTable();
	}
	
	private SecureRandom random = new SecureRandom();
	
	public String randomString() {
		return new BigInteger(130, random).toString(32);
	}
	
	public boolean getSettingUp() {
		return settingUp;
	}
	
	public String getInitString() {
		return initString;
	}
	
	public void run() {
		update = new Update(this);
		setupIP();
		while (true) {
			DatagramPacket data;
			if((data = connection.getFirstInQueue()) != null) {
				handleMessage(new JRTVPacket(data.getData()), false);
			}
			sendEncryptionMessages();
			decryptMessages();
			try {
				this.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private List<JRTVPacket> outgoingEncryptionPackets = new ArrayList<JRTVPacket>();
	private List<JRTVPacket> incomingEncryptionPackets = new ArrayList<JRTVPacket>();
	
	//Sends the string message as payload to the client if it can see the client otherwise error
	public void sendMessage(String client, String message) {
		if (router.getIP(client) == null) {
			view.error("Recipient not valid!");
		} else {
			JRTVPacket packet = new JRTVPacket(message);
			packet.setNormal(true);
			
			sendPacket(client, packet);
		}
	}
	
	//sends the packet after processing the packet;
	public void sendPacket(String client, JRTVPacket packet) {
		int destination = router.getIntIP(client);
		if (destination == Controller.multicastAddress) {
			packet.setBroadcasted(true);
		}
		
		sendPacket(destination, packet);
	}
	
	public void sendPacket(int client, JRTVPacket packet) {
		packet.setSource(localIAddress);
		packet.setDestination(client);
		
		if (packet.isNormal() || packet.isDiffie()) {
			packet.setSeqnr(seqAckTable.getNextSeq(packet.getDestination()));
			seqAckTable.registerSendPacket(packet);
		}
		packet.setNextHop(router.getNextHop(packet.getDestination()));
		System.out.println("==========================  Nexthop testing  ===============================================");
		System.out.println(packet.getNextHop());
		System.out.println("=============================================================================================");
		
		if (packet.getDestination() != multicastAddress && !packet.isDiffie() && !packet.isAck()) {
			outgoingEncryptionPackets.add(packet);
		} else {
			//RSA Signing
			byte[] first = packet.getByteMessage();
			byte[] second = RSA.encrypt(new String(RSA.hash(first)), RSA.getPrivateKey(localIAddress));
			byte[] message = new byte[first.length + second.length];
			
			System.arraycopy(first, 0, message, 0, first.length);
			System.arraycopy(second, 0, message, message.length - second.length, second.length);
			
			packet.setByteMessage(message);//TODO RSA
			packet.setHashPayload(second.length);
			
			sendPacket(packet);
		}
	}
	
	public void sendEncryptionMessages() {
		for (int i = 0; i < outgoingEncryptionPackets.size(); i++) {
			JRTVPacket packet = outgoingEncryptionPackets.get(i);
			if (router.hasEncryptionKey(packet.getDestination())) {
				System.out.println("======================== Before the encryption ==============================================");
				System.out.println(packet.getMessage());
				System.out.println("=============================================================================================");
				packet = router.getEncryption(packet.getDestination()).encrypt(packet, RSA.getPrivateKey(localIAddress));//TODO RSA ?
				outgoingEncryptionPackets.remove(packet);
				sendPacket(packet);
				break;
			} else {
				if (!router.isSettingUpDiffie(packet.getDestination()) && router.getForwardingTable().getTable().containsKey(packet.getDestination())) {
					router.setupDiffie(packet.getDestination());
				}
				break;
			}
		}
	}
	
	private void decryptMessages() {
		for (int i = 0; i < incomingEncryptionPackets.size(); i++) {
			JRTVPacket packet = incomingEncryptionPackets.get(i);
			if (packet.getDestination() == multicastAddress || packet.isDiffie() || packet.isAck()) {
				byte[] message2 = packet.getByteMessage();
				byte[] second2 = new byte[packet.getHashPayload()];
				
				byte[] first2 = new byte[message2.length - packet.getHashPayload()]; 
				

				
				System.arraycopy(message2, 0, first2, 0, first2.length);
				System.arraycopy(message2, message2.length - second2.length, second2, 0, second2.length);
				
				String decrypted = RSA.decrypt(second2, RSA.getPublicKey(packet.getSource()));//TODO rsa ok?
				
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
					packet = router.getEncryption(packet.getSource()).decrypt(packet, RSA.getPublicKey(packet.getSource()));
					incomingEncryptionPackets.remove(packet);
					handleMessage(packet, true);
				} 
			}
		}
	}	
	
	private void sendPacket(JRTVPacket packet) {
		packet.setNextHop(router.getNextHop(packet.getDestination()));
		DatagramPacket data = new DatagramPacket(packet.toByteArray(), packet.toByteArray().length, getMulticastIAddress(), 2000);
		connection.send(data);
	}
	
	public void retransmit(JRTVPacket packet) {
		retransmit(packet, packet.getDestination());
	}
	
	public void retransmit(JRTVPacket packet, int destination) {
		packet.setSource(localIAddress);
		packet.setDestination(destination);
		
		if (packet.isNormal() || packet.isDiffie()) {
			System.out.println("========================== Before registering in retransmit =================================");
			System.out.println(packet.getMessage());
			System.out.println("=============================================================================================");
			seqAckTable.registerSendPacket(packet);
		}
		
		if (packet.getDestination() != multicastAddress && !packet.isDiffie()) {
			packet.setNextHop(router.getNextHop(packet.getDestination()));
			outgoingEncryptionPackets.add(packet);
		} else {
			//RSA Signing
			byte[] first = packet.getByteMessage();
			byte[] second = RSA.encrypt(new String(RSA.hash(first)), RSA.getPrivateKey(localIAddress));
			byte[] message = new byte[first.length + second.length];
			
			System.arraycopy(first, 0, message, 0, first.length);
			System.arraycopy(second, 0, message, message.length - second.length, second.length);
			
			packet.setByteMessage(message);//TODO RSA
			packet.setHashPayload(second.length);
			
			sendPacket(packet);
		}
		
		
//		packet.setDestination(destination);
//		
//		if (packet.getDestination() != multicastAddress) {
//			packet.setNextHop(router.getNextHop(packet.getDestination()));
//		}
//		
//		if (packet.isNormal() || packet.isDiffie()) {
//			seqAckTable.registerSendPacket(packet);
//		}
//		DatagramPacket data = new DatagramPacket(packet.toByteArray(), packet.toByteArray().length, getMulticastIAddress(), 2000);
//		connection.send(data);
	}
	
	public void relay(JRTVPacket packet) {
		packet.setNextHop(router.getNextHop(packet.getDestination()));
		sendPacket(packet);
	}
	
	private void sendAck(JRTVPacket packet) {
		JRTVPacket p = new JRTVPacket("ACK");
		p.setSource(localIAddress);
		p.setDestination(packet.getSource());
		p.setAcknr(packet.getSeqnr());
		p.setAck(true);
//		p.setSeqnr(seqAckTable.getNextSeq(packet.getDestination()));

		if (p.getDestination() != multicastAddress) {
			p.setNextHop(router.getNextHop(p.getDestination()));
		}
		
		sendPacket(p.getDestination(), p);
//		DatagramPacket data = new DatagramPacket(p.toByteArray(), p.toByteArray().length, getMulticastIAddress(), 2000);
//		connection.send(data);
	}
	
	//sends the packet after processing the packet;
	public void broadcastPacket(JRTVPacket packet) {
		sendPacket("Anonymous", packet);
	}
	
	//Broadcasts the message to all connected clients
	public void broadcastMessage(String message) {
		sendMessage("Anonymous", message);
	}
	
	public Router getRouter() {
		return router;
	}
	
	
	public void receiveFromView(String client, String message) {
		JRTVPacket packet = new JRTVPacket(message);
		packet.setNormal(true);
		sendPacket(client, packet);
	}
	
	public void handleMessage(JRTVPacket packet, boolean decrypted) {
		if (packet.getSource() != localIAddress) {
			if (packet.getNextHop() == localIAddress && packet.getDestination() != localIAddress && packet.getDestination() != multicastAddress) {
				relay(packet);
			} else {
				if (packet.getDestination() == localIAddress || packet.getDestination() == multicastAddress) {
					if (!decrypted) {// && !packet.isAck() && !packet.isUpdate()
						incomingEncryptionPackets.add(packet);
						if (packet.isNormal() || packet.isDiffie()) {
							sendAck(packet);
						}
					} else {
						System.out.println(packet.toString());
						System.out.println("Received: " + (!seqAckTable.isReceivedSeqNr(packet.getSource(), packet.getSeqnr())));
						System.out.println("Is Update: " + packet.isUpdate());
						if (!seqAckTable.isReceivedSeqNr(packet.getSource(), packet.getSeqnr()) || packet.isUpdate()) {
							seqAckTable.addReceivedSeqNr(packet.getSource(), packet.getSeqnr());
							if(packet.isNormal()) {
								handleNormal(packet);
							} else if (packet.isUpdate()) {
								handleUpdate(packet);
							} else if (packet.isSyn()) {
								handleSyn(packet);
							} else if (packet.isFin()) {
								handleFin(packet);
							} else if (packet.isAck()) {
								handleAck(packet);
							} else if (packet.isDiffie()) {
								handleDiffie(packet);
//							} else {
//								System.out.println("Handling ack?");
//								if (packet.getMessage().contains("ACK")) {
//									System.out.println("Yes!");
//								}
							}
						}
					}
				}
			}
		}
	}
	
	public void addRecipientToView(String recipient) {
		view.addRecipient(recipient);
	}
	
	public void removeRecipientToView(String recipient) {
		view.removeRecipient(recipient);
	}
	
	public void handleNormal(JRTVPacket packet) {
		//TODO implement order sorting
		String message = packet.getMessage();
		view.addMessage(router.getName(packet.getSource()), message, packet.isBroadcasted());
		//TODO: implement setting the right sequence and acknowledgement numbers
	}
	
	private void handleUpdate(JRTVPacket packet) {
		router.processUpdate(packet);
	}
	
	private void handleSyn(JRTVPacket packet) {
		//TODO: read sequencenumber, set that as ack, give appropiate ack, set syn and ack flag
	}
	
	private void handleFin(JRTVPacket packet) {
		//TODO: send Fin + ack
	}
	
	private void handleAck(JRTVPacket packet) {
		if (packet.isDiffie()) {
			router.processDiffie(packet);
		} else {
			seqAckTable.registerAckPacket(packet);
		}
	}
	
	private void handleDiffie(JRTVPacket packet) {
		router.processDiffie(packet);
	}
	
	
	
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
}

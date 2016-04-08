package application;

import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.security.Key;
import java.security.SecureRandom;
import java.util.Map;

import network.Connection;
import network.JRTVPacket;
import network.Router;
import network.SeqAckTable;
import network.Update;
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
		connection = new Connection(port, address);
	}
	
	public InetAddress getLocalInetAddress() {
		return localInetAddress;
	}
	//NetworkInterface.getNetworkInterfaces().  TODO
	private void setupIP() {
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
				handleMessage(data);
			}
			try {
				this.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	//TODO implement SEQ and ACK numbers.
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
	//CHANGE NEXTHOP ACCORDINGLY TODO
	public void sendPacket(int client, JRTVPacket packet) {
		packet.setSeqnr(seqAckTable.getNextSeq(packet.getDestination()));
		packet.setSource(localIAddress);
		packet.setDestination(client);
		
		if (packet.getDestination() != multicastAddress) {
			packet.setNextHop(router.getNextHop(packet.getDestination()));
		}
		
		if (packet.isNormal()) {
			seqAckTable.registerSendPacket(packet);
		}
		
		DatagramPacket data = new DatagramPacket(packet.toByteArray(), packet.toByteArray().length, getMulticastIAddress(), 2000);
		connection.send(data);
	}
	
	public void retransmit(JRTVPacket packet) {
		retransmit(packet, packet.getDestination());
	}
	
	public void retransmit(JRTVPacket packet, int destination) {
		packet.setDestination(destination);
		
		if (packet.getDestination() != multicastAddress) {
			packet.setNextHop(router.getNextHop(packet.getDestination()));
		}
		
		if (packet.isNormal()) {
			seqAckTable.registerSendPacket(packet);
		}
		
		DatagramPacket data = new DatagramPacket(packet.toByteArray(), packet.toByteArray().length, getMulticastIAddress(), 2000);
		connection.send(data);
	}
	
	private void sendAck(JRTVPacket packet) {
		JRTVPacket p = new JRTVPacket("ACK");
		p.setSource(localIAddress);
		p.setDestination(packet.getSource());
		p.setAcknr(packet.getSeqnr());
		p.setSeqnr(seqAckTable.getNextSeq(packet.getDestination()));

		if (p.getDestination() != multicastAddress) {
			p.setNextHop(router.getNextHop(p.getDestination()));
		}
		
		DatagramPacket data = new DatagramPacket(p.toByteArray(), p.toByteArray().length, getMulticastIAddress(), 2000);
		connection.send(data);
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
	
	//HIERONDER IS SAFE VINCENT!
	//-----------------VVVVVVVVVVVVVVVVVVVVVVVV-------------------------
	
	public void receiveFromView(String client, String message) {
		System.out.println("FROM VIEW TO: " + client);
		JRTVPacket packet = new JRTVPacket(message);
		packet.setNormal(true);
		sendPacket(client, packet);
	}
	
	public void handleMessage(DatagramPacket message) {
		JRTVPacket packet = new JRTVPacket(message.getData());
//TODO right order?
		if (packet.getSource() != localIAddress) {
			if (packet.getNextHop() == localIAddress && packet.getDestination() != localIAddress && packet.getDestination() != multicastAddress) {
				retransmit(packet);
			} else {
				if (packet.getDestination() == localIAddress || packet.getDestination() == multicastAddress) {
					
					if (packet.isNormal()) {
						sendAck(packet);
					}
					
					if (!seqAckTable.isReceivedSeqNr(packet.getSource(), packet.getSeqnr())) {
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
						} else if (packet.isRSA()) {
							handleRSA(packet);
						} else if (packet.isDiffie()) {
							handleDiffie(packet);
						} else {
							if (packet.getMessage().equals("ACK")) {
								System.out.println("In de handle message komt ie wel");
								seqAckTable.registerAckPacket(packet);
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
	
	public void handleNormal(JRTVPacket p) {
		String message = p.getMessage();
		view.addMessage(router.getName(p.getSource()), message, p.isBroadcasted());
		//TODO: implement setting the right sequence and acknowledgement numbers
	}
	
	public String getNameBySource(int source) {
		return router.getName(source);
	}
	
	private void handleUpdate(JRTVPacket p) {
		System.out.println(" Hij handelt die update");
		router.processUpdate(p);
	}
	
	private void handleSyn(JRTVPacket p) {
		//TODO: read sequencenumber, set that as ack, give appropiate ack, set syn and ack flag
	}
	
	private void handleFin(JRTVPacket p) {
		//TODO: send Fin + ack
	}
	
	private void handleAck(JRTVPacket p) {
		//TODO: start sending data
	}
	
	private void handleRSA(JRTVPacket p) {
		router.processRSA(p);
	}
	
	private void handleDiffie(JRTVPacket p) {
		//TODO: Diffie
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
	
}

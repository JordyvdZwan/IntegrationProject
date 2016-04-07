package application;

import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.util.Map;

import network.Connection;
import network.JRTVPacket;
import network.Router;
import network.SeqAckTable;
import network.Update;
public class Controller extends Thread {

	private View view;
	private Connection connection;
	private InetAddress multicastIAddress;
	private int localIAddress = 0;
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

	private void setupIP() {
		initString = randomString();
		while (settingUp) {
			DatagramPacket data;
			if((data = connection.getFirstInQueue()) != null) {
				System.out.print("EQUAL: " +new JRTVPacket(data.getData()).getMessage().equals(initString));
				JRTVPacket p = new JRTVPacket(data.getData());
				System.out.print("1: " +p.getMessage());
				System.out.print("1: " +initString);
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		view.start();
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
	
	public Map<Integer,Map<InetAddress, Integer>> getForwardingTable() {
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
				this.sleep(100);
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
		packet.setSource(localIAddress);			
		packet.setDestination(router.getIntIP(client));
		if (router.getIntIP(client) == Controller.multicastAddress) {
			packet.setBroadcasted(true);
		}
		
		sendPacket(packet.getDestination(), packet);
	}
	
	public void sendPacket(int client, JRTVPacket packet) {
		int[] seqAck = seqAckTable.getSeqAck(client, packet.isBroadcasted());
		packet.setSeqnr(seqAck[0] + packet.getPayloadLength());
		packet.setAcknr(seqAck[1]);
		if (!packet.getMessage().equals("ACK")) {
			seqAckTable.registerOutgoingPackage(packet);
		}
		DatagramPacket data = new DatagramPacket(packet.toByteArray(), packet.toByteArray().length, router.getRouteIP(packet.getDestination()), 2000);
		connection.send(data);
	}
	
	public void retransmit(JRTVPacket packet) {
		if (!packet.getMessage().equals("ACK")) {
			seqAckTable.registerOutgoingPackage(packet);
		}
		DatagramPacket data = new DatagramPacket(packet.toByteArray(), packet.toByteArray().length, router.getRouteIP(packet.getDestination()), 2000);
		connection.send(data);
	}
	
	private void sendAck(JRTVPacket packet) {
		JRTVPacket p = new JRTVPacket("ACK");
		p.setDestination(localIAddress);//packet.getSource());
		p.setSource(localIAddress);
		sendPacket(packet.getSource(), p);
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
		JRTVPacket packet = new JRTVPacket(message);
		packet.setNormal(true);
		sendPacket(client, packet);
	}
	
	public void handleMessage(DatagramPacket message) {
		JRTVPacket packet = new JRTVPacket(message.getData());

		if (packet.getNextHop() == localIAddress) {
			retransmit(packet);
		}
		if (packet.getSource() != localIAddress && (packet.getDestination() == localIAddress || (packet.getDestination() == multicastAddress && !packet.isUpdate() ))) {
			
			seqAckTable.receivedPackage(packet);
			if (!packet.getMessage().equals("ACK") && !packet.isUpdate()){
				sendAck(packet);
			}
			System.out.println(packet.toString()); //TODO
//			if (!seqAckTable.received(packet.getSeqnr(), packet.getAcknr(), packet.isBroadcasted(), packet.getSource())) {
				if(packet.isNormal()) {
					handleNormal(packet);
				} else if (packet.isUpdate()) {
					handleUpdate(packet, message.getAddress());
				} else if (packet.isSyn()) {
					handleSyn(packet);
				} else if (packet.isFin()) {
					handleFin(packet);
				} else if (packet.isAck()) {
					handleAck(packet);
				}
//			}
			
			
			
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
		view.addMessage(router.getName(p.getSource()), message);
		//TODO: implement setting the right sequence and acknowledgement numbers
	}
	
	public String getNameBySource(int source) {
		return router.getName(source);
	}
	
	public void handleUpdate(JRTVPacket p, InetAddress address) {
		router.processUpdate(p, address);
	}
	
	public void handleSyn(JRTVPacket p) {
		//TODO: read sequencenumber, set that as ack, give appropiate ack, set syn and ack flag
	}
	
	public void handleFin(JRTVPacket p) {
		//TODO: send Fin + ack
	}
	
	public void handleAck(JRTVPacket p) {
		//TODO: start sending data
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

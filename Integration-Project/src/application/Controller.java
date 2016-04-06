package application;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import network.Connection;
import network.JRTVPacket;
import network.Router;
import network.Update;
public class Controller extends Thread {

	View view;
	Connection connection;
	InetAddress multicastIAddress;
	InetAddress localIAddress;
	Router router = new Router(this);
	Update update;
	
	String clientName = "Anonymous";
	
	public Controller(View view) {
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
		update = new Update(this);
	}
	
	public void run() {
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
		if (client.equals("Anonymous")) {
			JRTVPacket packet = new JRTVPacket(message);
			broadcastPacket(packet);
		} else if (router.getIP(client) == null) {
			view.error("Recipient not valid!");
		} else {
			JRTVPacket packet = new JRTVPacket(message);
			packet.setNormal(true);
			packet.setSource(router.getLocalIntAddress());			
			packet.setDestination(router.getIntIP(client));
			
			System.out.println(packet.toString()); //TODO
			DatagramPacket data = new DatagramPacket(packet.toByteArray(), packet.toByteArray().length, router.getRouteIP(client), 2000);
			connection.send(data);
		}
	}
	
	//sends the packet after processing the packet;
	public void sendPacket(String client, JRTVPacket packet) {
		packet.setSource(router.getLocalIntAddress());			
		packet.setDestination(router.getIntIP(client));
		
		System.out.println(packet.toString()); //TODO
		DatagramPacket data = new DatagramPacket(packet.toByteArray(), packet.toByteArray().length, router.getRouteIP(client), 2000);
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
	
	//HIERONDER IS SAFE VINCENT!
	//-----------------VVVVVVVVVVVVVVVVVVVVVVVV-------------------------
	
	public void receiveFromView(String client, String message) {
		JRTVPacket packet = new JRTVPacket(message);
		packet.setNormal(true);
		sendMessage(client, message);
	}
	
	public void handleMessage(DatagramPacket message) {
		
//		System.out.println(message.getAddress());
		JRTVPacket packet = new JRTVPacket(message.getData());

		System.out.println("is het een update? : " + packet.isUpdate());
		System.out.println(router.getName(message.getAddress()));
		System.out.println("is het een normal? : " + packet.isNormal());
		System.out.println(packet.toString());
		if(packet.isNormal()) {
			handleNormal(packet);
		} else if (packet.isUpdate()) {
			handleUpdate(message);
		} else if (packet.isSyn()) {
			handleSyn(packet);
		} else if (packet.isFin()) {
			handleFin(packet);
		} else if (packet.isAck()) {
			handleAck(packet);
		}
	}
	
	public void handleNormal(JRTVPacket p) {
		String message = p.getMessage();
		view.addMessage("" + p.getSource(), message);
		//TODO: implement setting the right sequence and acknowledgement numbers
	}
	
	public void handleUpdate(DatagramPacket p) {
		router.setEntry(p.getAddress(), p.getData().toString());
		
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

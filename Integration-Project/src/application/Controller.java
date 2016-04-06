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
	InetAddress IAddress;
	Router router = new Router(this);
	Update update = new Update(this);
	
	String clientName = "Anonymous";
	
	public Controller(View view) {
		this.view = view;
		
		String address = "224.0.0.2";
		try {
			IAddress = InetAddress.getByName(address);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int port = 2000;
		
		connection = new Connection(port, address);
	}
	
	public void run() {
		while (true) {
			DatagramPacket data;
			if((data = connection.getFirstInQueue()) != null) {
				handleMessage(data.getData());
			}
			try {
				this.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	

	//Sends the string message as payload to the client if it can see the client otherwise error
	public void sendMessage(String client, String message) {
		if (router.getIP() != null) {
			JRTVPacket packet = new JRTVPacket(message);
			
			
			
			
			
			DatagramPacket data = new DatagramPacket(, .length, router.getIP(), 2000);
			connection.send(data);
		} else if (client.equals("Anonymous")) {
			broadcastPacket()
		} else {
			view.error("Recipient")
		}
	}
	
	//sends the packet after processing the packet;
	public void sendPacket(JRTVPacket packet) {
		
	}
	
	//sends the packet after processing the packet;
	public void broadcastPacket(JRTVPacket packet) {
		
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
		sendMessage(client, packet.toByteArray());
	}
	
	public void handleMessage(byte[] message) {
		
		JRTVPacket packet = new JRTVPacket(message);
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
		}
	}
	
	public void handleNormal(JRTVPacket p) {
		String message = new String(p.getMessage());
		view.addMessage("" + p.getSource(), message);
	}
	
	public void handleUpdate(JRTVPacket p) {
		
	}
	
	public void handleSyn(JRTVPacket p) {
		
	}
	
	public void handleFin(JRTVPacket p) {
		
	}
	
	public void handleAck(JRTVPacket p) {
		
	}
	
	public void setClientName(String clientName) {
		this.clientName = clientName;
	}
	
	public String getClientName() {
		return clientName;
	}
	
	public InetAddress getAddress() {
		return IAddress;
	}
}

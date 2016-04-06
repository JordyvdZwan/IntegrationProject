package application;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import network.Connection;
import network.JRTVPacket;
import network.Router;
public class Controller extends Thread {

	View view;
	Connection connection;
	InetAddress IAddress;
	Router router;
	
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
			DatagramPacket data = new DatagramPacket(message.getBytes(), message.getBytes().length, IAddress, 2000);
			connection.send(data);
		}
		
		
	}
	
	//sends the packet
	public void sendPacket(JRTVPacket packet) {
		
	}
	
	//Broadcasts the message to all connected clients
	public void broadcastMessage(String message) {
		
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

package application;

import java.net.DatagramPacket;
import java.net.InetAddress;

import network.Connection;

public class Controller {

	View view;
	Connection connection;
	
	public Controller(View view) {
		this.view = view;
		
		String address = "224.0.0.1";
		int port = 2000;
		
		connection = new Connection(port, address);
		
		start();
	}
	
	public void start() {
		while (true) {
			DatagramPacket data = connection.getFirstInQueue();
			String message = new String(data.getData());
			view.addMessage(data.getAddress().toString(), message);
		}
	}
	
	public void sendMessage(String client, String message) {
		DatagramPacket data = new DatagramPacket(message.getBytes(), message.getBytes().length);
		connection.send(data);
	}
	
	
	
	
}

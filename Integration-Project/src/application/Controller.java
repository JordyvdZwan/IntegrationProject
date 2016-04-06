package application;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import network.Connection;

public class Controller extends Thread {

	View view;
	Connection connection;
	InetAddress IAddress;
	
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
				String message = new String(data.getData());
				view.addMessage(data.getAddress().toString(), message);
			}
			try {
				this.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void sendMessage(String client, String message) {
		DatagramPacket data = new DatagramPacket(message.getBytes(), message.getBytes().length, IAddress, 2000);
		connection.send(data);
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

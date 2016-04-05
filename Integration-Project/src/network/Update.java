package network;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

public class Update extends Thread {
	
	
	String clientname;
	Connection connection;
	InetAddress IAddress;
	public Update(String clientname, Connection c) {
		
		try {
			IAddress = InetAddress.getByName("224.0.0.2");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		this.clientname = clientname;
		connection = c;
		this.start();
	}
	
	public void run() {
		while(true) {
			
			DatagramPacket updatepacket = new DatagramPacket( clientname.getBytes() , clientname.getBytes().length, IAddress , 2000);
			 
			connection.send(updatepacket);
			
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}

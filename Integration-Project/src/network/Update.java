package network;

import java.net.DatagramPacket;
import java.util.concurrent.TimeUnit;

public class Update extends Thread {
	
	String address;
	String clientname;
	Connection connection;
	
	public Update(String address, String clientname, Connection c) {
		
		this.address = address;
		this.clientname = clientname;
		connection = c;
	
	}
	
	public void run() {
		while(true) {
			byte[] addressbytes = address.getBytes();
			byte[] clientnamebytes = clientname.getBytes();
			byte[] data = new byte[addressbytes.length + clientnamebytes.length];
			
			System.arraycopy(addressbytes, 0, data, 0, addressbytes.length);
			System.arraycopy(clientnamebytes, 0, data, addressbytes.length, clientnamebytes.length);
			
			DatagramPacket updatepacket = new DatagramPacket( data , address.getBytes().length + clientname.getBytes().length);
			connection.send(updatepacket);
			
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}

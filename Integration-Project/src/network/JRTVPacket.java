package network;

public class JRTVPacket {

	String message;
	
	Byte flags = 0;
	
	int seq = 0;
	int ack = 0;
	
	
	
	
	
	
	public JRTVPacket(String message) {
		this.message = message;
	}
	
	
	
	
	
	
	
	
}
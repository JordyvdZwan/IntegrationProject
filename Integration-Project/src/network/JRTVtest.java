package network;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class JRTVtest {
	JRTVPacket packet = null;

	@Before
	public void setup() {
		
		byte[] bytes = {00000000, 00000000, 00000000, 00000000, 00000000, 00000000, 00000000, 00000000, 
				00000000, 00000000, 00000000, 00000000, 00000000, 00000000, 00000000, 00000000, 
				00000000, 00000000, 00000000, 00000000, 00000000};
		packet = new JRTVPacket("Hello");
		packet.setAcknr(500);
		packet.setSeqnr(300);
	}
	
	@Test
	public void testSYN() {
		packet.setSyn(true);
		byte[] pakket = packet.toByteArray();
		byte syn = (byte) 128;
		
		assertEquals(pakket[19], syn);
	}
	
	@Test
	public void testNormal() {
		packet.setNormal(true);
		byte[] pakket = packet.toByteArray();
		byte syn = (byte) 16;
		assertEquals(pakket[19], syn);
	}
	
	@Test
	public void testACKSYN() {
		packet.setAck(true);
		packet.setSyn(true);
//		packet.setUpdate(true);
		System.out.println(packet.toString());
		
		byte[] pakket = packet.toByteArray();
		
		
		
		byte syn = (byte) 192;
		JRTVPacket p = new JRTVPacket(pakket);
		System.out.println(p.toString());
		assertEquals(packet.isSyn(), p.isSyn());
		assertEquals(packet.isAck(), p.isAck());
		assertEquals(pakket[19], syn);
	}
	
	@Test
	public void testSequence() {
		packet.setSeqnr(82);
		byte[] pakket = packet.toByteArray();
		byte seq = (byte) 82;
		assertEquals(pakket[8] + pakket[9] + pakket[10] + pakket[11], seq);
	}
	
}

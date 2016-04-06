package network;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

public class EcryptionTest {
	byte[] key = {0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 1, 1, 0, 1, 0, 
					0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 1, 1, 0, 
					0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 1, 0, 0, 
					0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 
					0, 0, 0, 0, 1, 0}; 
	byte[] text = {1,1,1,0,1,1,1,1};
	String tekst = "GrumpyGinger";
	OFB o = new OFB();
	RSA r = new RSA(1024);
	
	@Before
	public void setup() {
		o.setKey(key);
	}
	
	@Test
	public void testOFBEnDecrypt() {
		byte[] encrypt = o.EnDecrypt(text);
		assertTrue(Arrays.equals(o.EnDecrypt(encrypt), (text)));
	}
	
	@Test
	public void testRSA() {
		assertTrue(r.decrypt(r.encrypt(tekst)).equals(tekst));
//		assertEquals(r.decrypt(r.encrypt(tekst)), tekst);
	}
	
	@Test
	public void testRSASigning() {
		
	}

}

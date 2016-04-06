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
	byte[] text = {0,0,0,1,1,1,1,1};
	String tekst = "GrumpyGinger";
	OFB o = new OFB();
	RSA r = new RSA();
	
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
		System.out.print("{");
		for (int i = 0; i < text.length; i++) {
			if (i != 0) {
				System.out.print(", ");
			}
		System.out.print(text[i]);
		}
		System.out.print("}");
		System.out.print("\n");
		
		byte[] encrypt = r.encrypt(text);
		
		System.out.print("{");
		for (int i = 0; i < encrypt.length; i++) {
			if (i != 0) {
				System.out.print(", ");
			}
		System.out.print(encrypt[i]);
		}
		System.out.print("}");
		System.out.print("\n");
		
		byte[] decrypt = r.decrypt(encrypt);
		
		System.out.print("{");
		for (int i = 0; i < decrypt.length; i++) {
			if (i != 0) {
				System.out.print(", ");
			}
		System.out.print(decrypt[i]);
		}
		System.out.print("}");
		System.out.print("\n");
		
		
		System.out.println(text);
		assertTrue(r.decrypt(r.encrypt(text)).equals(text));
//		assertEquals(r.decrypt(r.encrypt(tekst)), tekst);
	}
	
	@Test
	public void testRSASigning() {
		String sign = r.sign(tekst);
		assertTrue(r.verify(sign, tekst));
	}

}

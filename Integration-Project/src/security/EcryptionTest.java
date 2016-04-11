package security;

import static org.junit.Assert.*;

import java.math.BigInteger;
import java.util.Arrays;
import org.junit.Test;

public class EcryptionTest {
	byte[] key = {0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 1, 1, 0, 1, 0, 
					0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 1, 1, 0, 
					0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 1, 0, 0, 
					0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 
					0, 0, 0, 0, 1, 0}; 
	byte[] text = {0,0,0,1,1,1,1,1};
	String tekst = "2634346342";
//	OFB o = new OFB();
	//RSA2 r = new RSA2();
//	DiffieHellman d = new DiffieHellman();
//	DiffieHellman h = new DiffieHellman();
	
//	@Test
//	public void testOFBEnDecrypt() {
//		byte[] encrypt = OFB.EnDecrypt(text, key);
//		assertTrue(Arrays.equals(OFB.EnDecrypt(encrypt, key), (text)));
//	}
//	
//	@Test
//	public void testDiffie() {
//		BigInteger A = d.generate(d.geta(), d.getg(), d.getp());
//		BigInteger B = h.generate(h.geta(), d.getg(), d.getp());
//		BigInteger keyd = d.generate(d.geta(), B, d.getp());
//		BigInteger keyh = h.generate(h.geta(), A, d.getp());
//		assertEquals(keyd, keyh);
//	}
	
	@Test
	public void testRSA() {
		assertTrue(RSA.decrypt(RSA.encrypt(tekst, RSA.getPublicKey(-1062730494)), RSA.getPrivateKey(-1062730494)).equals(tekst));
		assertTrue(RSA.decrypt(RSA.encrypt(tekst, RSA.getPrivateKey(-1062730494)), RSA.getPublicKey(-1062730494)).equals(tekst));
		assertEquals(RSA.getPublicKey(-1062730494), RSA.getPublicKey(-1062730494));

//		System.out.println(RSA.toKey(RSA.toBytes()) + "     " + RSA.getPublicKey(-1062730494));
//		assertTrue(RSA.toKey(RSA.toBytes()).equals(RSA.getPublicKey(-1062730494)));
	}

	@Test
	public void testRSASigning() {
		String sign = new String(RSA.hash(tekst.getBytes()));
		byte[] signed = RSA.encrypt(sign, RSA.getPrivateKey(-1062730494));
		String verify = RSA.decrypt(signed, RSA.getPublicKey(-1062730494));
		
		assertEquals(sign, verify);
		//assertTrue(RSA2.verify(tekst, sign.getBytes(), RSA2.getPublicKey()));
	}

}

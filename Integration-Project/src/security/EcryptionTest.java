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
	byte[] text = {0, 0, 0, 1, 1, 1, 1, 1};
	String tekst = "2634346342";
	OFB o = new OFB();
	RSA r = new RSA();
	DiffieHellman d = new DiffieHellman();
	DiffieHellman h = new DiffieHellman();
	
	/**
	 * This tests whether the OFB encnryption works.
	 */
	@Test
	public void testOFBEnDecrypt() {
		byte[] encrypt = OFB.enDecrypt(text, key);
		assertTrue(Arrays.equals(OFB.enDecrypt(encrypt, key), text));
	}
	
	/**
	 * This tests whether the diffie Hellman exchange works.
	 */
	@Test
	public void testDiffie() {
		BigInteger a = d.generate(d.geta(), d.getg(), d.getp());
		BigInteger b = h.generate(h.geta(), d.getg(), d.getp());
		BigInteger keyd = d.generate(d.geta(), b, d.getp());
		BigInteger keyh = h.generate(h.geta(), a, d.getp());
		assertEquals(keyd, keyh);
	}
	
	/**
	 * This tests whether the RSA signing and encrypting works.
	 */
	@Test
	public void testRSA() {
		assertTrue(RSA.decrypt(RSA.encrypt(tekst, 
						RSA.getPublicKey(-1062730494)), 
								RSA.getPrivateKey(-1062730494)).equals(tekst));
		assertTrue(RSA.decrypt(RSA.encrypt(tekst, 
						RSA.getPrivateKey(-1062730494)), 
								RSA.getPublicKey(-1062730494)).equals(tekst));
		assertEquals(RSA.getPublicKey(-1062730494), RSA.getPublicKey(-1062730494));
	}

	/**
	 * This tests whether the RSA signing works using hashing.
	 */
	@Test
	public void testRSASigning() {
		String sign = new String(RSA.hash(tekst.getBytes()));
		byte[] signed = RSA.encrypt(sign, RSA.getPrivateKey(-1062730494));
		String verify = RSA.decrypt(signed, RSA.getPublicKey(-1062730494));
		
		assertEquals(sign, verify);
	}

}

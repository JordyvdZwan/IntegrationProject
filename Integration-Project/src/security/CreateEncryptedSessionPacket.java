package security;

import java.math.BigInteger;
import java.security.Key;
import java.util.Arrays;

public class CreateEncryptedSessionPacket {
	
	private boolean presentkey = false;
	private DiffieHellman diffie = new DiffieHellman();

	/**
	 * Krijgen tekst binnen
	 * Creer een key als deze er niet is
	 * Encrypt message using key
	 * hash en sign
	 * concat
	 * stuur terug
	 *
	 */
	
	/**
	 * encrypts de data
	 * kijkt eerst of er een key is
	 * dan encrypt hij met die key
	 * signed hij de tekst en plakt dit erachter
	 * returnded deze byte array
	 * @param data de tekst die verstuurd moet worden
	 * @return
	 */
	public byte[] encrypt(String data, Key privatekey) {
		if (!presentkey) {
			//TODO
			keyDiffieHellmanFirst();
			// need to send these values to other side
			// wait
			keyDiffieHellmanFinal(//nummer uit packet from the other side);
		}
		byte[] encrypt = OFB.EnDecrypt(data.getBytes(), diffie.getKey().toByteArray());
		String sign = ((Integer) data.hashCode()).toString();
		byte[] signed = RSA.encrypt(sign, privatekey);
		byte[] encrypted = new byte[encrypt.length + signed.length];
		System.arraycopy(encrypt, 0, encrypted, 0, encrypt.length);
		System.arraycopy(signed, 0, encrypted, encrypt.length, signed.length);
		return encrypted;
	}
	
	/**
	 * knipt eerst de byte array in 2 stukken
	 * verified de laatse
	 * als ze overeen komen decrypt hij het bericht
	 * anders geeft hij een error
	 * @param encrypted de encrypted data
	 * @param length de lengte van de hash op het einde
	 * @param rsa de public key van de afzender
	 * @return
	 */
	public String decrypt(byte[] encrypted, int length, Key publickey) {
		String result = null;
		byte[] encrypt = Arrays.copyOfRange(encrypted, 0, encrypted.length - length);
		byte[] hash = Arrays.copyOfRange(encrypted, encrypted.length - length, encrypted.length);
		String sign = ((Integer) encrypt.toString().hashCode()).toString();
		String verify = RSA.decrypt(hash, publickey);
		if (sign.equals(verify)) {
			result = OFB.EnDecrypt(encrypt, diffie.getKey().toByteArray()).toString();
		} else {
			result = "error, bedieger";
		}
		return result;
	}
	
	/**
	 * generates first number
	 * @return g^a mod p
	 */
	public BigInteger[] keyDiffieHellmanFirst() {
		BigInteger A = diffie.generate(diffie.geta(), diffie.getg(), diffie.getp());
		BigInteger[] result = {A, diffie.getg(), diffie.getp()};
		return result;
	}
	
	/**
	 * gets B from the other side and computes the key with it
	 * @param B
	 */
	public void keyDiffieHellmanFinal(BigInteger B) {
		diffie.setKey(diffie.generate(diffie.geta(), B, diffie.getp()));
		presentkey = true;
	}
	
	/**
	 * gets 3 numbers from other side
	 * makes key with these numbers
	 * computes number for other side
	 * @param A
	 * @param g
	 * @param p
	 * @return B to send to the other side
	 */
	public BigInteger keyDiffieHellmanSecond(BigInteger[] numbers) {
		BigInteger A = numbers[1];
		BigInteger g = numbers[2];
		BigInteger p = numbers[3];
		BigInteger B = diffie.generate(diffie.geta(), g, p);
		diffie.setKey(diffie.generate(diffie.geta(), A, p));
		presentkey = true;
		return B;
	}
	
}


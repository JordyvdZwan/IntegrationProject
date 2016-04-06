package network;

import java.math.BigInteger;
import java.security.SecureRandom;

public class RSA {
	private BigInteger n;
	private BigInteger d;
	private BigInteger e;
	
	private int length = 1024;
	
	public RSA(int bits) {
		length = bits;
		SecureRandom r = new SecureRandom();
		BigInteger p = new BigInteger(length / 3, 100, r);
		BigInteger q = new BigInteger(length / 3, 100, r);
		n = p.multiply(q);
		BigInteger m = (p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE)));
		e = new BigInteger("3");
		while (m.gcd(e).intValue() > 1) {
			e = e.add(new BigInteger("2"));
		}
		d = e.modInverse(m);
	}
	
	public String encrypt(String message) {
		return (new BigInteger(message.getBytes())).modPow(e, n).toString();
	}
	
	public String decrypt(String message) {
		return (new BigInteger(message.getBytes())).modPow(d, n).toString();
	}
	
	
	public String sign(String message) {
		Integer hashed = message.hashCode(); 
		return (new BigInteger(hashed.toString())).modPow(d, n).toString(); 
	}
	
	public boolean verify(String signature, String message) {
		Integer hashed = message.hashCode();
		return (new BigInteger(signature.getBytes()).modPow(e, n).toString()
				.equals(new BigInteger(hashed.toString())));
	}
	
}
package network;

import java.math.BigInteger;
import java.security.SecureRandom;

public class RSA {
	private BigInteger n;
	private BigInteger d;
	private BigInteger e;
	
	private static final int LENGTH = 1024;
	
	public RSA() {
		SecureRandom r = new SecureRandom();
		BigInteger p = new BigInteger("3");//new BigInteger(LENGTH / 2, 100, r);
		BigInteger q = new BigInteger("11");//new BigInteger(LENGTH / 2, 100, r);
		n = p.multiply(q);
		BigInteger m = (p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE)));
		e = new BigInteger("3");
		while (m.gcd(e).intValue() > 1) {
			e = e.add(new BigInteger("2"));
		}
		d = e.modInverse(m);
		System.out.println("d: " + d);
		System.out.println("e: " + e);
		System.out.println("m: " + m);
		System.out.println("n: " + n);
	}
	
	public byte[] encrypt(byte[] message) {
		return (new BigInteger(message)).modPow(e, n).toByteArray();
	}
	
	public byte[] decrypt(byte[] message) {
		return (new BigInteger(message)).modPow(d, n).toByteArray();
	}
	
	
	public String sign(String message) {
		Integer hashed = message.hashCode(); 

		
		System.out.println(hashed);
		System.out.println((new BigInteger(hashed.toString())).modPow(d, n).toString());
		
		
		return (new BigInteger(hashed.toString())).modPow(d, n).toString(); 
	}
	
	public boolean verify(String signature, String message) {
		Integer hashed = message.hashCode();

		System.out.println(signature);
		System.out.println(new BigInteger(signature.getBytes()).modPow(e, n));
		System.out.println(new BigInteger(hashed.toString()));
		
		System.out.println(new BigInteger(hashed.toString()).mod(n));
		
		return (new BigInteger(signature.getBytes()).modPow(e, n))
				== (new BigInteger(hashed.toString()));
	}
	
}
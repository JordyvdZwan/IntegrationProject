package network;

import java.math.BigInteger;
import java.security.SecureRandom;

public class DiffieHellman {
	
	private BigInteger key;
	int length = 1024;
	
	/**
	 * Constructor to start a session.
	 * @param bits
	 */
	public DiffieHellman(int bits) {
		SecureRandom r = new SecureRandom();
		BigInteger a = new BigInteger(length / 3, 100, r);
		BigInteger g = new BigInteger(length / 3, 100, r);
		BigInteger p = new BigInteger(length / 3, 100, r);
		generate(a, g, p);
		key = key(A, a, p);
	}
	
	/**
	 * Constructor to receive a session
	 * @param g
	 * @param p
	 * @param A Received form other party
	 */
	public DiffieHellman(BigInteger g, BigInteger p, BigInteger A) {
		SecureRandom r = new SecureRandom();
		BigInteger a = new BigInteger(length / 3, 100, r);
		generate(a, g, p);
		key = key(A, a, p);
	}
	
	/**
	 * Generate the first round of variables.
	 * @param a own generated key
	 * @param g
	 * @param p
	 * @return
	 */
	public BigInteger generate(BigInteger a, BigInteger g, BigInteger p) {
		return g.modPow(a, p);
	}
	
	/**
	 * Extract the key from the variables.
	 * @param A Received from other party
	 * @param a own generated key
	 * @param p 
	 * @return
	 */
	public BigInteger key(BigInteger A, BigInteger a, BigInteger p) {
		return A.modPow(a, p);
	}
	
	public BigInteger getKey() {
		return key;
	}
}

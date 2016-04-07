package security;

import java.math.BigInteger;
import java.security.SecureRandom;

public class DiffieHellman {
	
	private BigInteger key;
	private BigInteger a, g, p;
	private static final int LENGTH = 1024;
	
	/**
	 * Constructor to start a session.
	 */
	public DiffieHellman() {
		SecureRandom r = new SecureRandom();
		a = new BigInteger(LENGTH / 2, 100, r);
		g = new BigInteger(LENGTH / 2, 100, r);
		p = new BigInteger(LENGTH / 2, 100, r);
	}
	
	/**
	 * Constructor to receive a session
	 * @param g
	 * @param p
	 * @param A Received form other party
	 */
	public DiffieHellman(BigInteger g, BigInteger p, BigInteger A) {
		SecureRandom r = new SecureRandom();
		a = new BigInteger(LENGTH / 2, 100, r);
	}
	
	/**
	 * Generate the first round of variables.
	 * @param a own generated key 	- Received number
	 * @param g						- own generated key
	 * @param p
	 * @return
	 */
	public BigInteger generate(BigInteger a, BigInteger g, BigInteger p) {
		return g.modPow(a, p);
	}
	
	public BigInteger getKey() {
		return key;
	}
	
	public BigInteger geta() {
		return a;
	}
	
	public BigInteger getg() {
		return g;
	}
	
	public BigInteger getp() {
		return p;
	}
	
	
}

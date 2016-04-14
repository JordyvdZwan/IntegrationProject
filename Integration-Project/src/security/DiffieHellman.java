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
	 * Generate the first round of variables.
	 * @param k own generated key 	- Received number
	 * @param m						- own generated key
	 * @param z
	 * @return
	 */
	public BigInteger generate(BigInteger k, BigInteger m, BigInteger z) {
		return m.modPow(k, z);
	}
	
	public BigInteger getKey() {
		return key;
	}
	
	public void setKey(BigInteger key) {
		this.key = key;
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
	
	public boolean hasKey() {
		return key != null;
	}
	
}

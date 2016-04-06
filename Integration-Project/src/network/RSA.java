package network;

import java.util.ArrayList;
import java.util.List;

public class RSA {
	
	public static final byte[] E = {1,0,0,1,1,0,0,0,1,0,1,0,0,1,0,1,1,0,0,0,0,0,1,0,1,0,1,1,1}; // decimale waarde 320122967
	public static final long N = 3917136493L;
	public static final byte[] d = {1,1,1,0,1,1,1,0,0,0,0,1,1,0,0,1,1,1,0,1,1,0,1,0,0,1,1,1}; // decimale waarde 249666983
	
	public List<Byte> sign(byte[] message) {
		List<Byte> signed = new ArrayList<Byte>();
		
		return signed;
	}
	
	public boolean verify(byte[] message, byte[] hash) {
		
	}
	
	public byte[] publicKeyEncrypt(byte[] publickey) {
		
	}
}

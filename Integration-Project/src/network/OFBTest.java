package network;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

public class OFBTest {
	byte[] key = {0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 1, 1, 0, 1, 0, 
					0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 1, 1, 0, 
					0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 1, 0, 0, 
					0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 
					0, 0, 0, 0, 0, 1, 0}; 
	byte[] text = {1,1,1,0,1,1,1,1};
	OFB o = new OFB();
	
	@Before
	public void setup() {
		o.setKey(key);
	}
	
	@Test
	public void testEnDecrypt() {
		o.setKey(key);
		byte[] encrypt = o.EnDecrypt(text);
		assertTrue(Arrays.equals(o.EnDecrypt(encrypt), (text)));
	}

}

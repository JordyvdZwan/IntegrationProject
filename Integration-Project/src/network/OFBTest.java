package network;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

public class OFBTest {
	byte[] text = {1,1,1,0,1,1,1,1,0};
	OFB o = new OFB();
	
	@Test
	public void testEnDecrypt() {
		byte[] encrypt = o.EnDecrypt(text);
		assertTrue(Arrays.equals(o.EnDecrypt(encrypt), (text)));
	}

}

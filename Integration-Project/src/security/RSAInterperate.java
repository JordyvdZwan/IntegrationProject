package security;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.security.Key;
import java.util.HashMap;

import org.apache.commons.codec.binary.Base64;

public class RSAInterperate {
	
	private static HashMap<Integer, Key> keys = new HashMap<Integer, Key>();
	
	
	//TODO Deze shit fixen
	
	public static Key RSAInterperateKey(int number, String file) throws NumberFormatException, IOException {
		BufferedReader in = new BufferedReader(new FileReader(file));
		String string = in.readLine();
		System.out.println(string);
		if (Integer.parseInt("" + (string.charAt(0))) == number) {
			keys.put(number, RSA.toKey(Base64.decodeBase64(string.substring(2))));
		}
		in.close();	
		System.out.println(keys);
		return null;
	}
}

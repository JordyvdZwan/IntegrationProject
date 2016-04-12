package security;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.security.Key;
import java.util.HashMap;
import java.util.Scanner;

import org.apache.commons.codec.binary.Base64;

public class RSAInterperate {
	
	public static Key RSAInterperatePublicKey(int number, String file) throws NumberFormatException, IOException {
		BufferedReader in = new BufferedReader(new FileReader(file));
		Scanner s = new Scanner(in);
		while (s.hasNextLine()) { 
			String string = s.nextLine();
			if (Integer.parseInt("" + (string.charAt(0))) == number) {
				s.close();
				in.close();	
				String print = new String(Base64.decodeBase64(string.substring(2)));
				return RSA.toPublicKey(Base64.decodeBase64(string.substring(2)));
			}
		}
		s.close();	
		in.close();
		return null;
	}
	
	public static Key RSAInterperatePrivateKey(int number, String file) throws NumberFormatException, IOException {
		BufferedReader in = new BufferedReader(new FileReader(file));
		Scanner s = new Scanner(in);
		while (s.hasNextLine()) { 
			String string = s.nextLine();
			if (Integer.parseInt("" + (string.charAt(0))) == number) {
				s.close();
				in.close();	
				String print = new String(Base64.decodeBase64(string.substring(2)));
				return RSA.toPrivateKey(Base64.decodeBase64(string.substring(2)));
			}
		}
		s.close();	
		in.close();
		return null;
	}
}

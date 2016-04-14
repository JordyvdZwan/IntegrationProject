package security;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.security.Key;
import java.util.Scanner;

import org.apache.commons.codec.binary.Base64;

public class RSAInterperate {
	
	/**
	 * This method returns a public key given a file name and number of the key.
	 * @param number which public key to get from the file.
	 * @param file in which file to read the data
	 * @return Public key
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	public static Key rsaInterperatePublicKey(int number, String file) 
												throws NumberFormatException, IOException {
		BufferedReader in = new BufferedReader(new FileReader(file));
		Scanner s = new Scanner(in);
		while (s.hasNextLine()) { 
			String string = s.nextLine();
			if (Integer.parseInt("" + (string.charAt(0))) == number) {
				s.close();
				in.close();	
				return RSA.toPublicKey(Base64.decodeBase64(string.substring(2)));
			}
		}
		s.close();	
		in.close();
		return null;
	}
	
	/**
	 * This method returns a private key given a file name and number of the key.
	 * @param number which private key to get from the file.
	 * @param file in which file to read the data.
	 * @return public key
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	public static Key rsaInterperatePrivateKey(int number, String file)
												throws NumberFormatException, IOException {
		BufferedReader in = new BufferedReader(new FileReader(file));
		Scanner s = new Scanner(in);
		while (s.hasNextLine()) { 
			String string = s.nextLine();
			if (Integer.parseInt("" + (string.charAt(0))) == number) {
				s.close();
				in.close();	
				return RSA.toPrivateKey(Base64.decodeBase64(string.substring(2)));
			}
		}
		s.close();	
		in.close();
		return null;
	}
}

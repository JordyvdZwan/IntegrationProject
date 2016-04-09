package security;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.Key;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Pattern;

public class RSAInterperate {
	
	private static HashMap<Integer, Key> keys = new HashMap<Integer, Key>();
	
	
	//TODO Deze shit fixen
	
	public static Key RSAInterperateKey(int number) throws NumberFormatException, IOException {
		BufferedReader in = new BufferedReader(new FileReader("publickeys.txt"));
		String string = in.readLine();
		if (Integer.parseInt("" + (string.charAt(0))) == number) {
			keys.put(number, RSA.toKey(string.substring(2).getBytes()));
		}
		in.close();	
		System.out.println(keys);
		return null;
	}
	
	public static void main(String[] args) {
		try {
			RSAInterperateKey(1);
		} catch (NumberFormatException | IOException e) {
			e.printStackTrace();
		}
	}
}

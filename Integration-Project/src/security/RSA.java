package security;

import java.io.BufferedReader;
import java.io.EOFException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Scanner;

import javax.crypto.Cipher;

import application.Controller;

public class RSA {
	
	/**
	 * Defining all finals
	 * PublicKey
	 * PrivateKey
	 * 
	 */
	//READER FIXEN
	public static final String ALGORITHM = "RSA";	
	private static final int NUMBER = Controller.multicastAddress;
	private static final Key PRIVATEKEY = ;
	private static final Key PUBLICKEY = static {
		if ( == Number) {
			
		};
	}
	
	/**
	 * Using native Java methods to encrypt the given text
	 * using RSA
	 * @param text to sign/encrypt
	 * @param key either private (for signing) or public (for encryption)
	 * @return
	 */
	public static byte[] encrypt(String text, Key key) {
	    byte[] cipherText = null;
	    try {
		    final Cipher cipher = Cipher.getInstance(ALGORITHM);
		    cipher.init(Cipher.ENCRYPT_MODE, key);
		    cipherText = cipher.doFinal(text.getBytes());
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
	return cipherText;
	}
	  
	/**
	 * Using native Java methods to decrypt the given text
	 * using RSA
	 * @param text to verify/decrypt
	 * @param key either public (for verifying) or private (for decryption)
	 * @return
	 */
	public static String decrypt(byte[] text, Key key) {
	    byte[] dectyptedText = null;
	    try {
		    final Cipher cipher = Cipher.getInstance(ALGORITHM);
		    cipher.init(Cipher.DECRYPT_MODE, key);
		    dectyptedText = cipher.doFinal(text);
	    } catch (Exception ex) {
	    	ex.printStackTrace();
	    }
	    return new String(dectyptedText);
	}

	
	public static Key getPublicKey() {
		//System.out.println("Length: " + PUBLICKEY.toString().getBytes().length + "    " + PUBLICKEY.toString());
		return PUBLICKEY;
	}
	
	public static Key getPrivateKey() {
		return PRIVATEKEY;
	}
	
	public static byte[] toBytes() {
		return PUBLICKEY.getEncoded();
	}
	
	//TODO
	public static Key toKey(byte[] bytes) { 
		Key key = null;
		System.out.println("0.5");	
		try {
			System.out.println("1");
			key = KeyFactory.getInstance(ALGORITHM).generatePublic(new X509EncodedKeySpec(bytes));
			System.out.println("2");
		} catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return key;
	}
}
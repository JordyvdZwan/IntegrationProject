package security;

import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.Cipher;

import network.Router;

public class RSA {
	
	/**
	 * Defining all finals
	 * PublicKey
	 * PrivateKey
	 * 
	 */
	//READER FIXEN
	public static final String ALGORITHM = "RSA";	
	private static final int NUMBER = 1;
	private static final Key PRIVATEKEY;
	static {
		Key temp = null;
		try {
			temp = RSAInterperate.RSAInterperateKey(NUMBER, "private.txt");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		PRIVATEKEY = temp;
	}
	private static final Key PUBLICKEY;
	static {
		Key temp = null;
		try {
			temp = RSAInterperate.RSAInterperateKey(NUMBER, "publickeys.txt");
		} catch (IOException e) {
			e.printStackTrace();
		}
		PUBLICKEY = temp;
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

	
	public static Key getPublicKey(int source) {
		Key key = null;
		try {
			//System.out.println("Length: " + PUBLICKEY.toString().getBytes().length + "    " + PUBLICKEY.toString());
			String file;
			
			file = "publickeys.txt";
			int number = Router.getStringIP(source).charAt(Router.getStringIP(source).length() - 1);
		
			key =  RSAInterperate.RSAInterperateKey(number, file);
		} catch (NumberFormatException e) {
			//TODO error
		} catch (IOException e) {
			//TODO error
		}
		return key;
	}
	
	public static Key getPrivateKey(int ip) {
		Key key = null;
		try {
			//System.out.println("Length: " + PUBLICKEY.toString().getBytes().length + "    " + PUBLICKEY.toString());
			String file;
			
			int number = Router.getStringIP(ip).charAt(Router.getStringIP(ip).length() - 1);
			file = "privatekey" + number + ".txt";
			System.out.println(number);
			System.out.println(Router.getStringIP(ip).charAt(Router.getStringIP(ip).length() - 1));
			System.out.println(file);
			key =  RSAInterperate.RSAInterperateKey(number, file);
		} catch (NumberFormatException e) {
			//TODO error
		} catch (IOException e) {
			//TODO error
		}
		return key;
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
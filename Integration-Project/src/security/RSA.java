package security;

import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import network.Router;

public class RSA {
	
	/**
	 * Defining all finals.
	 * PublicKey
	 * PrivateKey
	 * 
	 */
	//READER FIXEN
	public static final String ALGORITHM = "RSA";
	public static final int CORRECTION = 48;
	
	/**
	 * Using native Java methods to encrypt the given text.
	 * using RSA
	 * @param text to sign/encrypt
	 * @param key either private (for signing) or public (for encryption)
	 * @return
	 */
	public static byte[] encrypt(String text, Key key) {
	    byte[] cipherText = null;
	    Cipher cipher;
		try {
			cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, key);
			cipherText = cipher.doFinal(text.getBytes());
		} catch (NoSuchAlgorithmException | 
						NoSuchPaddingException | 
						InvalidKeyException | 
						IllegalBlockSizeException | 
						BadPaddingException e) {
			e.printStackTrace();
		}
		return cipherText;
	}
	
	
	public static byte[] hash(byte[] input) {
		MessageDigest m;
		BigInteger bi = null;
		try {
			m = MessageDigest.getInstance("MD5");
			m.update(input, 0, input.length);
			bi = new BigInteger(1, m.digest());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return bi.mod(new BigInteger("1975846582458")).toString().getBytes();
	}
	  
	/**
	 * Using native Java methods to decrypt the given text.
	 * using RSA
	 * @param text to verify/decrypt
	 * @param key either public (for verifying) or private (for decryption)
	 * @return
	 */
	public static String decrypt(byte[] text, Key key) {
	    byte[] dectyptedText = null;
		Cipher cipher;
		try {
			cipher = Cipher.getInstance(ALGORITHM);
		    cipher.init(Cipher.DECRYPT_MODE, key);
		    dectyptedText = cipher.doFinal(text);
		} catch (NoSuchAlgorithmException | 
				NoSuchPaddingException | 
				InvalidKeyException | 
				IllegalBlockSizeException | 
				BadPaddingException e) {
			e.printStackTrace();
		}
	    return new String(dectyptedText);
	}

	
	public static Key getPublicKey(int source) {
		Key key = null;
		try {
			String file = "publickeys.txt";
			int number
			    = Router.getStringIP(source).charAt(
			    			  Router.getStringIP(source).length() - 1) - CORRECTION;
			
			key = RSAInterperate.rsaInterperatePublicKey(number, file);
		} catch (NumberFormatException | IOException e) {
			e.printStackTrace();
		}
		return key;
	}
	
	public static Key getPrivateKey(int ip) {
		Key key = null;
		try {
			String file;
			int number = Router.getStringIP(ip).charAt(
							  Router.getStringIP(ip).length() - 1) - CORRECTION;
			file = "privatekey" + number + ".txt";
			
			key =  RSAInterperate.rsaInterperatePrivateKey(number, file);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return key;
	}
	
	public static Key toPublicKey(byte[] bytes) { 
		Key key = null;
		try {
			key = KeyFactory.getInstance(ALGORITHM).generatePublic(new X509EncodedKeySpec(bytes));
		} catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return key;
	}
	
	public static Key toPrivateKey(byte[] bytes) { 
		Key key = null;
		try {
			EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(bytes);
			key = KeyFactory.getInstance(ALGORITHM).generatePrivate(privKeySpec);
		} catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return key;
	}
}
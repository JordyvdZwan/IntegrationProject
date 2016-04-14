package security;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.security.*;

import org.apache.commons.codec.binary.Base64;

public class RSAGenerate {
	
	public static final String ALGORITHM = "RSA";	
	
	/**
	 * This method Creates RSA Public and private keys
	 * @param amount the amount of keys you want.
	 */
	public static void rsaGenerateKeys(int amount) {
		try {
			File publickeys = new File("publickeys.txt");
			PrintWriter printerpublic = new PrintWriter(publickeys);
			for (int i = 0; i < amount; i++) {
				try {
					KeyPairGenerator kpg = KeyPairGenerator.getInstance(ALGORITHM);
					KeyPair kp = kpg.generateKeyPair();
					Key publicKey = kp.getPublic();
					Key privateKey = kp.getPrivate();

					printerpublic.println((i + 1) + " " + 
							  new String(Base64.encodeBase64(publicKey.getEncoded())));				
					
					File privatekey = new File("privatekey" + (i + 1) + ".txt");
					PrintWriter printerprivate = new PrintWriter(privatekey);				
					printerprivate.write((i + 1) + " " + 
							  new String(Base64.encodeBase64(privateKey.getEncoded())));
					
					printerprivate.flush();
					printerpublic.flush();
					
					printerprivate.close();
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				} 
			}
			printerpublic.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		rsaGenerateKeys(4);
	}
}
package security;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.security.*;

import org.apache.commons.codec.binary.Base64;

public class RSAGenerate {
	
	public static final String ALGORITHM = "RSA";	
	
	public static void RSAGenerateKeys(int amount) {
		try {
			File publickeys = new File("publickeys.txt");
			PrintWriter printerpublic = new PrintWriter(publickeys);
			for (int i = 0; i < amount; i++) {
				try {
					KeyPairGenerator kpg = KeyPairGenerator.getInstance(ALGORITHM);
					KeyPair kp = kpg.generateKeyPair();
					Key PublicKey = kp.getPublic();
					Key PrivateKey = kp.getPrivate();
					
					System.out.println((i + 1) + " " + new String(Base64.encodeBase64(PublicKey.getEncoded())));
					printerpublic.println((i + 1) + " " + new String(Base64.encodeBase64(PublicKey.getEncoded())));				
					
					File privatekey = new File("privatekey" + (i + 1) + ".txt");
					PrintWriter printerprivate = new PrintWriter(privatekey);				
					printerprivate.write((i + 1) + " " + new String(Base64.encodeBase64(PrivateKey.getEncoded())));
					
					printerprivate.flush();
					printerpublic.flush();
					
					printerprivate.close();
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				} finally {
				}
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		RSAGenerateKeys(4);
	}
}
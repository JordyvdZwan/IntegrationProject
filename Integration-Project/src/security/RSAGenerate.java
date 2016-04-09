package security;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.security.*;

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

					printerpublic.println((i + 1) + " " + PublicKey.getEncoded().toString());				
					
					File privatekey = new File("privatekey" + (i + 1) + ".txt");
					PrintWriter printerprivate = new PrintWriter(privatekey);				
					printerprivate.write((i + 1) + " " + PrivateKey.getEncoded().toString());
					
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
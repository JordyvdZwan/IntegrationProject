package security;

import java.math.BigInteger;

public class OFB {

	private static final int BLOCKSIZE = 63;
	private static final byte[] OJ =
					new BigInteger("2983491237019287350983127509183"
							+ "275489273049832704918237409823170149"
							+ "8732104981273049871230948723019487120"
							+ "3948701239847012398021746428374698198"
							+ "2734691238756932814756934817165982735"
							+ "6894736598273465987324965987234659834"
							+ "7569238479563487659287346598347507982"
							+ "3475098740958613240985613208746098236"
							+ "508736409182364099283464869320").toByteArray();

	/**
	 * This method encrypts and decrypts a certain message.
	 * @param message to be encrypted data.
	 * @param key Key with which to encrypt.
	 * @return Encrypted message.
	 */
	public static byte[] enDecrypt(byte[] message, byte[] key) {
		/**
		 * OFB
		 */
		//Length of the message to encrypt.
		int m = message.length; 				
		//Encrypted message in the form of a byte array
		byte[] result = new byte[m];			
		//Array of the blocks used to encrypt.
		byte[][] blocks = new byte[(m / BLOCKSIZE) + 1][BLOCKSIZE];	
				
		/*
		 * Create the blocks which are used to encrypt the message
		 */
		for (int i = 0; i < blocks.length; i++) {
			if (i == 0) {
				blocks[0] = xor(OJ, key);
			} else {
				blocks[i] = xor(blocks[i - 1], key);
			}
		}
		
//		/*
//		 * For loop om te printen van de de blocks created
//		 */
//		for (int i = 0; i < blocks.length; i++) {
//			System.out.print("{" );
//			for (int j = 0; j < BLOCKSIZE; j++) {
//				if (j != 0) {
//					System.out.print(", ");
//				}
//				System.out.print(blocks[i][j]);
//			}
//			System.out.print("}");
//		}
//		System.out.print("\n");
		
		
		for (int i = 0; i < m; i += BLOCKSIZE) {
			byte[] blocki = new byte[BLOCKSIZE];
			/*
			 * Normal case, BLOCKSIZE is smaller then the still to be encrypted message
			 */
			if ((message.length - i) / BLOCKSIZE >= 1.00) {
				System.arraycopy(message, i, blocki, 0, BLOCKSIZE);
				System.arraycopy(xor(blocki, blocks[i / BLOCKSIZE]), 0, result, i, BLOCKSIZE);
			} else {
				/*
				 * Case in which the BLOCKSIZE is bigger then the still to be done message
				 * No arraycopy possible.
				 */
				int j = 0;
				while (j < message.length - i) {
					blocki[j] = message [j + i];
					j++;
				}				
				System.out.println(blocks[i / BLOCKSIZE].length + "\t\t\t" + blocki.length);
				
				System.arraycopy(xor(blocki, blocks[i / BLOCKSIZE]),
															0, result, i, result.length - i);
			}
		}
		return result;

	}
	
	/**
	 * Receives two byte arrays as input. 
	 * For every value in the given byte array - a:
	 * 	- Cast it as an int
	 *  - Cast the same index from b as int
	 *  - xOR
	 *  - Recast the whole thing as a byte.
	 *  - Put in corresponding position of the output
	 *  
	 * The method will output an array with the same
	 * length as the first byte array given.
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	private static byte[] xor(byte[] a, byte[] b) {
		int length;
		if (a.length < b.length) {
			length = a.length;
		} else {
			length = b.length;
		}
		byte[] result = new byte[length];
		for (int i = 0; i < length; i++) {
			result[i] = (byte) ((int) a[i] ^ (int) b[i]);
		}
		return result;
	}
}
	

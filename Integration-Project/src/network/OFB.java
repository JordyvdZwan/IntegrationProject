package network;

public class OFB {

	//TODO Goede waardes instellen
	private byte[] key = new byte[BLOCKSIZE];
	private static final int BLOCKSIZE = 64;
	private static final byte[] Oj = 
					{1,1,0,1,1,1,0,1,1,0,0,1,1,0,1,1,0,1,0,1,0,
					 1,1,0,1,0,1,1,0,0,1,1,1,0,1,0,0,1,0,1,0,0,
					 1,0,1,0,0,1,0,0,1,1,0,0,1,0,1,1,0,1,0,0,0}; 
	
	public byte[] EnDecrypt(byte[] message) {
		/**
		 * OFB
		 */
				
		int m = message.length; 				//Length of the message to encrypt.
		byte[] result = new byte[m];			//Encrypted message in the form of a byte array
		byte[][] blocks = new byte[(m/BLOCKSIZE) + 1][BLOCKSIZE];	//Array of the blocks used to encrypt.
				
		/*
		 * Create the blocks which are used to encrypt the message
		 */
		for (int i = 0; i < blocks.length; i++) {
			if (i == 0) {
				blocks[0] = xor(Oj, key);
			} else {
				blocks[i] = xor(blocks[i-1], key);
			}
		}
		
		/*
		 * For loop om te printen van de de blocks created
		 */
		for (int i = 0; i < blocks.length; i++) {
			System.out.print("{" );
			for (int j = 0; j < BLOCKSIZE; j++) {
				if (j != 0) {
					System.out.print(", ");
				}
				System.out.print(blocks[i][j]);
			}
			System.out.print("}");
		}
		System.out.print("\n");
		
		
		for(int i = 0; i < m; i += BLOCKSIZE) {
			byte[] blocki = new byte[BLOCKSIZE];
			/*
			 * Normal case, BLOCKSIZE is smaller then the still to be encrypted message
			 */
			if ((message.length - i) / BLOCKSIZE >= 1.00) {
				System.arraycopy(message, i, blocki, 0, BLOCKSIZE);
				System.arraycopy(xor(blocki, blocks[i/BLOCKSIZE]), 0, result, i, BLOCKSIZE);
			} else {
				/*
				 * Case in which the BLOCKSIZE is bigger then the still to be done message
				 * No arraycopy possible.
				 */
				for (int j = 0; j < message.length - i; j++) {
					blocki[j] = message [j + i];
				}
				System.arraycopy(xor(blocki, blocks[i/BLOCKSIZE]), 0, result, i, blocki.length - 1);
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
	private byte[] xor(byte[] a, byte[] b) {
		byte[] result = new byte[a.length];
		for (int i = 0; i < a.length; i++) {
			result[i] = (byte)((int)a[i] ^ (int)b[i]);
		}
		return result;
	}
	
	
	public void setKey(byte[] key) {
		this.key = key;
	}
	
	
	
	
	
	
	
	
	
	
	
	/*
	 * MAIN to test some stuff
	 */
//	public static void main(String[] args) {
//		OFB hoi = new OFB();
//		hoi.setKey(new byte[]{1,0});
//		
//		System.out.println("Encrypt: \n{1, 1, 1, 0, 1, 1, 1, 0, 0, 0}");
//		byte[] result = hoi.EnDecrypt(new byte[]{1,1,1,0,1,1,1,0,0,0});
//		System.out.print("{" );
//		for (int i = 0; i < result.length; i++) {
//			if (i != 0) {
//				System.out.print(", ");
//			}
//		System.out.print(result[i]);
//		}
//		System.out.print("}\n");
//	
//		System.out.println("Decrypt:");
//		byte[] retur = hoi.EnDecrypt(result);
//		System.out.print("{" );
//		for (int i = 0; i < retur.length; i++) {
//			if (i != 0) {
//				System.out.print(", ");
//			}
//		System.out.print(retur[i]);
//		}
//		System.out.print("}");
//	}
}
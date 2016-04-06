package network;

public class OFB {

	//TODO Goede waardes instellen
	private byte[] key = new byte[BLOCKSIZE];
	private static final int BLOCKSIZE = 2; //64
	private static final byte[] Oj = {1,1};
//						,0,1,1,1,0,1,1,0,0,1,1,0,1,1,0,1,0,1,0,
//					 1,1,0,1,0,1,1,0,0,1,1,1,0,1,0,0,1,0,1,0,0,
//					 1,0,1,0,0,1,0,0,1,1,0,0,1,0,1,1,0,1,0,0,0}; 
	
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
		
		
		for(int i = 0; i < m; i += BLOCKSIZE) {
			byte[] blocki = new byte[BLOCKSIZE];
			System.arraycopy(message, i, blocki, 0, BLOCKSIZE);
			System.arraycopy(xor(blocki, blocks[i/BLOCKSIZE]), 0, result, i, BLOCKSIZE);
		
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
	 * The method needs to receive is only used to work with the blocksize
	 * So this is hardcoded. 
	 * 
	 * The previous statement implies a.lenght && b.lenght >= BLOCKSIZE.
	 * (For this application a && b == BLOCKSIZE should hold).
	 * If a.length or b.length < BLOCKSIZE, indexoutofbound error will occur.
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	private byte[] xor(byte[] a, byte[] b) {
		byte[] result = new byte[BLOCKSIZE];
		for (int i = 0; i < BLOCKSIZE; i++) {
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
	public static void main(String[] args) {
		OFB hoi = new OFB();
		hoi.setKey(new byte[]{1,0});
		
		byte[] result = hoi.EnDecrypt(new byte[]{1,0,1,0,1,1});
		System.out.print("{" );
		for (int i = 0; i < result.length; i++) {
			if (i != 0) {
				System.out.print(", ");
			}
		System.out.print(result[i]);
		}
		System.out.print("}\n");
	
		
		byte[] retur = hoi.EnDecrypt(result);
		System.out.print("{" );
		for (int i = 0; i < retur.length; i++) {
			if (i != 0) {
				System.out.print(", ");
			}
		System.out.print(retur[i]);
		}
		System.out.print("}");
	}
}
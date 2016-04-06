package network;

public class ASCII {
	
	public String toASCII(String message) {
		int ascii = 0;
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < message.length(); i++) {
			ascii = (int) message.charAt(i);
			result.append(ascii);
		}
		return result.toString();
	}

	public String toString(BigInteger message) {
		
	}
}

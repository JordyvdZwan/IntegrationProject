package network;


public class JRTVPacket {

	static final int HEADERLENGTH = 28;
	static final int SOURCELENGTH = 4;
	static final int DESTINATIONLENGTH = 4;
	static final int SEQLENGTH = 4;
	static final int ACKLENGTH = 4;
	static final int HASHPAYLOADLENGTH = 3;
	static final int FLAGSLENGTH = 1;
	static final int PAYLOADLENGTH = 4;
	static final int NEXTHOPLENGTH = 4;
	
	private byte[] message;
	private Byte flags = 0;
	private int seqnr = 0;
	private int acknr = 0;
	private int source = 0;
	private int destination = 0;
	private int hashPayload = 0;
	private int payloadLength = 0;
	private int nextHop = 0;
	
	private boolean syn = false;
	private boolean ack = false;
	private boolean update = false;
	private boolean normal = false;
	private boolean file = false;
	private boolean broadcasted = false;
	private boolean rsa = false;
	private boolean diffie = false;
	
	//======================================================================================================================
	//||                                                  Constructor/conversions                                          ||  
	//======================================================================================================================	

	public String toString() {
		String res = "";
		res = res.concat("Addressing data:\n");
		res = res.concat("Sequence number: " + seqnr + "\n");
		res = res.concat("Acknowledgement number: " + acknr + "\n");
		res = res.concat("Source Address: " + Router.getStringIP(source) + "\n");
		res = res.concat("Destination Address: " + Router.getStringIP(destination) + "\n");
		res = res.concat("Hash Payload Length: " + hashPayload + "\n");
		res = res.concat("Payload Length: " + payloadLength + "\n");
		res = res.concat("nextHop: " + Router.getStringIP(nextHop) + "\n");
		res = res.concat("\n");
		res = res.concat("Flaggs:\n");
		res = res.concat("SYN: " + syn + "\n");
		res = res.concat("ACK: " + ack + "\n");
		res = res.concat("UPDATE: " + update + "\n");
		res = res.concat("NORMAL: " + normal + "\n");
		res = res.concat("FILE: " + file + "\n");
		res = res.concat("BROADCASTED: " + broadcasted + "\n");
		res = res.concat("\n");
		res = res.concat("DATA: \n");
		res = res.concat(new String(message));
		return res;
	}
	
	public JRTVPacket(String message) {
		this.message = message.getBytes();
		this.payloadLength = message.getBytes().length;
	}
	
	/**
	 * Constructs a JRTVPacket given a byte array.
	 * @param bytes byte array which represents a JRTVPacket.
	 */
	public JRTVPacket(byte[] bytes) {
		byte[] header = new byte[HEADERLENGTH];
		System.arraycopy(bytes, 0, header, 0, HEADERLENGTH);
		
		byte[] sender = new byte[SOURCELENGTH];
		System.arraycopy(bytes, 0, sender, 0, SOURCELENGTH);
		this.source = byteArrayToInt(sender);
		
		byte[] receiver = new byte[DESTINATIONLENGTH];
		System.arraycopy(bytes, SOURCELENGTH, receiver, 0, DESTINATIONLENGTH);
		this.destination = byteArrayToInt(receiver);
		
		byte[] seq = new byte[SEQLENGTH];
		System.arraycopy(bytes, SOURCELENGTH + DESTINATIONLENGTH, seq, 0, SEQLENGTH);
		this.seqnr = byteArrayToInt(seq);
		
		byte[] acknowledgement = new byte[ACKLENGTH];
		System.arraycopy(bytes, SOURCELENGTH + DESTINATIONLENGTH + SEQLENGTH, 
														acknowledgement, 0, ACKLENGTH);
		this.acknr = byteArrayToInt(acknowledgement);
		
		byte[] hashpayload = new byte[HASHPAYLOADLENGTH + 1];
		System.arraycopy(bytes, SOURCELENGTH + DESTINATIONLENGTH + SEQLENGTH + ACKLENGTH,
																hashpayload, 1, HASHPAYLOADLENGTH);
		this.hashPayload = byteArrayToInt(hashpayload);
		
		byte[] flgs = new byte[FLAGSLENGTH];
		System.arraycopy(bytes,
					 SOURCELENGTH + DESTINATIONLENGTH + SEQLENGTH + ACKLENGTH + HASHPAYLOADLENGTH,
																			flgs, 0, FLAGSLENGTH);
		setFlags(flgs);
		
		byte[] payloadlength = new byte[PAYLOADLENGTH];
		System.arraycopy(bytes, 
					 SOURCELENGTH + DESTINATIONLENGTH + SEQLENGTH
					 						+ ACKLENGTH + HASHPAYLOADLENGTH + FLAGSLENGTH,
					 											payloadlength, 0, PAYLOADLENGTH);
		this.payloadLength = byteArrayToInt(payloadlength);
		
		message = new byte[this.payloadLength];
		System.arraycopy(bytes, HEADERLENGTH, message, 0, this.payloadLength);
		//
		byte[] nexthop = new byte[NEXTHOPLENGTH];
		System.arraycopy(bytes, 
					 SOURCELENGTH + DESTINATIONLENGTH + SEQLENGTH + ACKLENGTH 
					 			+ HASHPAYLOADLENGTH + FLAGSLENGTH + PAYLOADLENGTH, 
					 												nexthop, 0, NEXTHOPLENGTH);
		this.nextHop = byteArrayToInt(nexthop);
	}
	
	/**
	 * @return this packet in form of a byte array.
	 */
	public byte[] toByteArray() {
		byte[] result = new byte[HEADERLENGTH + message.length];
		
		byte[] src = intToByteArray(source);
		result[0] = src[0];
		result[1] = src[1];
		result[2] = src[2];
		result[3] = src[3];
		
		byte[] dest = intToByteArray(destination);
		result[4] = dest[0];
		result[5] = dest[1];
		result[6] = dest[2];
		result[7] = dest[3];
		
		byte[] seq = intToByteArray(seqnr);
		result[8] = seq[0];
		result[9] = seq[1];
		result[10] = seq[2];
		result[11] = seq[3];
		
		byte[] acknowledgement = intToByteArray(acknr);
		result[12] = acknowledgement[0];
		result[13] = acknowledgement[1];
		result[14] = acknowledgement[2];
		result[15] = acknowledgement[3];
		
		byte[] hpayload = intToByteArray(hashPayload);
		result[16] = hpayload[1];
		result[17] = hpayload[2];
		result[18] = hpayload[3];
		
		result[19] = getByteFlags();
		
		byte[] payloadlength = intToByteArray(this.payloadLength);
		result[20] = payloadlength[0];
		result[21] = payloadlength[1];
		result[22] = payloadlength[2];
		result[23] = payloadlength[3];
		
		byte[] nexthop = intToByteArray(this.nextHop);
		result[24] = nexthop[0];
		result[25] = nexthop[1];
		result[26] = nexthop[2];
		result[27] = nexthop[3];
		
		System.arraycopy(message, 0, result, HEADERLENGTH, message.length);
		return result;
	}


	/**
	 * Sets the flags given a byte.
	 * @param flags byte containing the flags.
	 */
	private void setFlags(byte[] flags) {
		Byte b = flags[0];
		int value = b.intValue();
		if (value < 0) {
			syn = true;
			value += 128;
		}
		if (value >= 64) {
			ack = true;
			value -= 64;	
		}
		if (value >= 32) {
			update = true;
			value -= 32;
		}
		if (value >= 16) {
			normal = true;
			value -= 16;
		}
		if (value >= 8) {
			file = true;
			value -= 8;
		}
		if (value >= 4) {
			broadcasted = true;
			value -= 4;
		}
		if (value >= 2) {
			rsa = true;
			value -= 2;
		}
		if (value >= 1) {
			diffie = true;
			value -= 1;
		}
	}
	
	/**
	 * Returns a byte corresponding to the flags set in this class.
	 */
	private byte getByteFlags() {
		int value = 0;
		byte b = 0;
		if (syn) {
			value += 128;
		}
		if (ack) {
			value += 64;
		}
		if (update) {
			value += 32;
		}
		if (normal) {
			value += 16;
		}
		if (file) {
			value += 8;
		}
		if (broadcasted) {
			value += 4;
		}
		if (rsa) {
			value += 2;
		}
		if (diffie) {
			value += 1;
		}
		b = (byte) value;
		return b;
	}
	
	/**
	 * returns a int given a byte array.
	 */
	private static int byteArrayToInt(byte[] b) {
	    return   b[3] & 0xFF |
	            (b[2] & 0xFF) << 8 |
	            (b[1] & 0xFF) << 16 |
	            (b[0] & 0xFF) << 24;
	}

	/**
	 * returns a byte array given a int.
	 */
	private static byte[] intToByteArray(int a) {
	    return new byte[] {
	        (byte) ((a >> 24) & 0xFF),
	        (byte) ((a >> 16) & 0xFF),   
	        (byte) ((a >> 8) & 0xFF),   
	        (byte) (a & 0xFF)
	    };
	}
	
	//======================================================================================================================
	//||                                                  getters/setters                                                 ||  
	//======================================================================================================================
	
	public boolean isRSA() {
		return rsa;
	}

	public void setRSA(boolean rSA) {
		rsa = rSA;
	}

	public boolean isDiffie() {
		return diffie;
	}

	public void setDiffie(boolean diffie) {
		this.diffie = diffie;
	}
	
	public String getMessage() {
		return new String(message);
	}

	/**
	 * Sets the message and the payloadlength.
	 */
	public void setMessage(String message) {
		this.message = message.getBytes();
		this.payloadLength = message.getBytes().length;
	}
	
	public byte[] getByteMessage() {
		return message;
	}
	
	/**
	 * Sets the message and the payloadlength.
	 */
	public void setByteMessage(byte[] message) {
		this.message = message;
		this.payloadLength = message.length;
	}

	public Byte getFlags() {
		return flags;
	}

	public void setFlags(Byte flags) {
		this.flags = flags;
	}

	public int getSeqnr() {
		return seqnr;
	}

	public void setSeqnr(int seqnr) {
		this.seqnr = seqnr;
	}

	public int getAcknr() {
		return acknr;
	}

	public void setAcknr(int acknr) {
		this.acknr = acknr;
	}

	public int getSource() {
		return source;
	}

	public void setSource(int source) {
		this.source = source;
	}

	public int getDestination() {
		return destination;
	}

	public void setDestination(int destination) {
		this.destination = destination;
	}

	public int getHashPayload() {
		return hashPayload;
	}

	public void setHashPayload(int hashPayload) {
		this.hashPayload = hashPayload;
	}

	public boolean isSyn() {
		return syn;
	}

	public void setSyn(boolean syn) {
		this.syn = syn;
	}

	public boolean isAck() {
		return ack;
	}

	public void setAck(boolean ack) {
		this.ack = ack;
	}

	public boolean isUpdate() {
		return update;
	}

	public void setUpdate(boolean update) {
		this.update = update;
	}

	public boolean isNormal() {
		return normal;
	}

	public void setNormal(boolean normal) {
		this.normal = normal;
	}

	public boolean isFile() {
		return file;
	}

	public void setFile(boolean file) {
		this.file = file;
	}
	
	public boolean isBroadcasted() {
		return broadcasted;
	}

	public void setBroadcasted(boolean broadcasted) {
		this.broadcasted = broadcasted;
	}
	
	public int getPayloadLength() {
		return payloadLength;
	}

	public void setPayloadLength(int payloadLength) {
		this.payloadLength = payloadLength;
	}
	
	
	public int getNextHop() {
		return nextHop;
	}

	public void setNextHop(int nextHop) {
		this.nextHop = nextHop;
	}
}
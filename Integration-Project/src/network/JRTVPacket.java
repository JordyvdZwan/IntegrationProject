package network;

public class JRTVPacket {

	static final int HEADERLENGTH = 20;
	static final int SOURCELENGTH = 4;
	static final int DESTINATIONLENGTH = 4;
	static final int SEQLENGTH = 4;
	static final int ACKLENGTH = 4;
	static final int HASHPAYLOADLENGTH = 4;
	static final int FLAGSLENGTH = 1;
	
	private String message;
	private Byte flags = 0;
	private int seqnr = 0;
	private int acknr = 0;
	private int source = 0;
	private int destination = 0;
	private int hashPayload = 0;
	
	private boolean syn = false;
	private boolean ack = false;
	private boolean update = false;
	private boolean normal = false;
	private boolean fin = false;

	public JRTVPacket(String message) {
		this.message = message;
	}
	
	public JRTVPacket(Byte[] bytes) {
		byte[] message = new byte[bytes.length - HEADERLENGTH];
		System.arraycopy(bytes, HEADERLENGTH, message, 0, bytes.length - HEADERLENGTH);
		this.message = new String(message);
		
		byte[] header = new byte[HEADERLENGTH];
		System.arraycopy(bytes, 0, header, 0, HEADERLENGTH);
		
		byte[] source = new byte[SOURCELENGTH];
		System.arraycopy(bytes, 0, source, 0, SOURCELENGTH);
		this.source = byteArrayToInt(source);
		
		byte[] destination = new byte[DESTINATIONLENGTH];
		System.arraycopy(bytes, SOURCELENGTH, destination, 0, DESTINATIONLENGTH);
		this.destination = byteArrayToInt(destination);
		
		byte[] seq = new byte[SEQLENGTH];
		System.arraycopy(bytes, SOURCELENGTH + DESTINATIONLENGTH, seq, 0, SEQLENGTH);
		this.seqnr = byteArrayToInt(seq);
		
		byte[] ack = new byte[ACKLENGTH];
		System.arraycopy(bytes, SOURCELENGTH + DESTINATIONLENGTH + SEQLENGTH , ack, 0, ACKLENGTH);
		this.acknr = byteArrayToInt(ack);
		
		byte[] hashPayload = new byte[HASHPAYLOADLENGTH];
		System.arraycopy(bytes, SOURCELENGTH + DESTINATIONLENGTH + SEQLENGTH + ACKLENGTH, hashPayload, 1, HASHPAYLOADLENGTH);
		this.hashPayload = byteArrayToInt(hashPayload);
		
		byte[] flags = new byte[FLAGSLENGTH];
		System.arraycopy(bytes, SOURCELENGTH + DESTINATIONLENGTH + SEQLENGTH + ACKLENGTH + HASHPAYLOADLENGTH, flags, 0, FLAGSLENGTH);
		setFlags(flags);
	}
	
	public byte[] toByteArray() {
		byte[] result = new byte[HEADERLENGTH + message.getBytes().length];
		
		byte[] dest = intToByteArray(destination);
		result[0] = dest[0];
		result[1] = dest[1];
		result[2] = dest[2];
		result[3] = dest[3];
		
		byte[] src = intToByteArray(source);
		result[4] = src[0];
		result[5] = src[1];
		result[6] = src[2];
		result[7] = src[3];
		
		byte[] seq = intToByteArray(seqnr);
		result[8] = seq[0];
		result[9] = seq[1];
		result[10] = seq[2];
		result[11] = seq[3];
		
		byte[] ack = intToByteArray(acknr);
		result[12] = ack[0];
		result[13] = ack[1];
		result[14] = ack[2];
		result[15] = ack[3];
		
		byte[] hpayload = intToByteArray(hashPayload);
		result[16] = hpayload[1];
		result[17] = hpayload[2];
		result[18] = hpayload[3];
		
		result[19] = getByteFlags();
		
		System.arraycopy(message.getBytes(), 0, result, 20, message.getBytes().length);
		return result;
	}
	
	private void setFlags(byte[] flags) {
		Byte b = flags[0];
		int value = b.intValue();
		if (value >= 128) {
			syn = true;
			value =- 128;
		}
		if (value >= 64) {
			ack = true;
			value =- 64;	
		}
		if (value >= 32) {
			update = true;
			value =- 32;
		}
		if (value >= 16) {
			normal = true;
			value =- 16;
		}
		if (value >= 8) {
			fin = true;
			value =- 8;
		}
	}
	
	private byte getByteFlags() {
		int value = 0;
		byte b = 0;
		if (syn) {
			value =+ 128;
		}
		if (ack) {
			value =+ 64;
		}
		if (update) {
			value =+ 32;
		}
		if (normal) {
			value =+ 16;
		}
		if (fin) {
			value =+ 8;
		}
		b = (byte) value;
		return b;
	}
	
	private static int byteArrayToInt(byte[] b) {
	    return   b[3] & 0xFF |
	            (b[2] & 0xFF) << 8 |
	            (b[1] & 0xFF) << 16 |
	            (b[0] & 0xFF) << 24;
	}

	private static byte[] intToByteArray(int a) {
	    return new byte[] {
	        (byte) ((a >> 24) & 0xFF),
	        (byte) ((a >> 16) & 0xFF),   
	        (byte) ((a >> 8) & 0xFF),   
	        (byte) (a & 0xFF)
	    };
	}
	
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
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

	public boolean isFin() {
		return fin;
	}

	public void setFin(boolean fin) {
		this.fin = fin;
	}
}
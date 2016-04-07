package network;

import java.net.InetAddress;
import java.net.UnknownHostException;

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
	
	private String message;
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
	private boolean fin = false;
	private boolean broadcasted = false;

	byte[] unpack(int bytes) {
		return new byte[] {
			(byte)((bytes >>> 24) & 0xff),
			(byte)((bytes >>> 16) & 0xff),
			(byte)((bytes >>>  8) & 0xff),
			(byte)((bytes       ) & 0xff)
		};
	}



			
	
	public String toString() {
		String res = "";
		try {
			res = res.concat("Addressing data:\n");
			res = res.concat("Sequence number: " + seqnr + "\n");
			res = res.concat("Acknowledgement number: " + acknr + "\n");
			res = res.concat("Source Address: " + InetAddress.getByAddress(unpack(source)).getHostAddress().toString() + "\n");
			res = res.concat("Destination Address: " + InetAddress.getByAddress(unpack(destination)).getHostAddress().toString() + "\n");
			res = res.concat("Hash Payload Length: " + hashPayload + "\n");
			res = res.concat("Payload Length: " + payloadLength + "\n");
			res = res.concat("nextHop: " + nextHop + "\n");
			res = res.concat("\n");
			res = res.concat("Flaggs:\n");
			res = res.concat("SYN: " + syn + "\n");
			res = res.concat("ACK: " + ack + "\n");
			res = res.concat("UPDATE: " + update + "\n");
			res = res.concat("NORMAL: " + normal + "\n");
			res = res.concat("FIN: " + fin + "\n");
			res = res.concat("BROADCASTED: " + broadcasted + "\n");
			res = res.concat("\n");
			res = res.concat("DATA: \n");
			res = res.concat(message + "\n");
			res = res.concat("\n");
			res = res.concat("Bytes\n");
			for (int i = 0; i < HEADERLENGTH + payloadLength; i++) {
				Integer j = (int) this.toByteArray()[i];
				res = res.concat(i + ": " + j.toString());
				res = res.concat("\n");
				if ((i + 1) % 8 == 0) {
					res = res.concat("----------------------\n");
				}
			}
		
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		return res;
	}
	
	public JRTVPacket(String message) {
		this.message = message;
		this.payloadLength = message.getBytes().length;
	}
	
	public JRTVPacket(byte[] bytes) {
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
		
		byte[] hashPayload = new byte[HASHPAYLOADLENGTH + 1];
		System.arraycopy(bytes, SOURCELENGTH + DESTINATIONLENGTH + SEQLENGTH + ACKLENGTH, hashPayload, 1, HASHPAYLOADLENGTH);
		this.hashPayload = byteArrayToInt(hashPayload);
		
		byte[] flags = new byte[FLAGSLENGTH];
		System.arraycopy(bytes, SOURCELENGTH + DESTINATIONLENGTH + SEQLENGTH + ACKLENGTH + HASHPAYLOADLENGTH, flags, 0, FLAGSLENGTH);
		setFlags(flags);
		
		byte[] payloadLength = new byte[PAYLOADLENGTH];
		System.arraycopy(bytes, SOURCELENGTH + DESTINATIONLENGTH + SEQLENGTH + ACKLENGTH + HASHPAYLOADLENGTH + FLAGSLENGTH, payloadLength, 0, PAYLOADLENGTH);
		this.payloadLength = byteArrayToInt(payloadLength);
		
		byte[] message = new byte[this.payloadLength];
		System.arraycopy(bytes, HEADERLENGTH, message, 0, this.payloadLength);
		this.message = new String(message);
		//
		byte[] nextHop = new byte[NEXTHOPLENGTH];
		System.arraycopy(bytes, SOURCELENGTH + DESTINATIONLENGTH + SEQLENGTH + ACKLENGTH + HASHPAYLOADLENGTH + FLAGSLENGTH + PAYLOADLENGTH, nextHop, 0, NEXTHOPLENGTH);
		this.nextHop = byteArrayToInt(nextHop);
	}
	
	public byte[] toByteArray() {
		byte[] result = new byte[HEADERLENGTH + message.getBytes().length];
		
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
		
		byte[] payloadLength = intToByteArray(this.payloadLength);
		result[20] = payloadLength[0];
		result[21] = payloadLength[1];
		result[22] = payloadLength[2];
		result[23] = payloadLength[3];
		
		byte[] nextHop = intToByteArray(this.nextHop);
		result[24] = nextHop[0];
		result[25] = nextHop[1];
		result[26] = nextHop[2];
		result[27] = nextHop[3];
		
		System.arraycopy(message.getBytes(), 0, result, HEADERLENGTH, message.getBytes().length);
		return result;
	}
	
	public int getNextHop() {
		return nextHop;
	}

	public void setNextHop(int nextHop) {
		this.nextHop = nextHop;
	}

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
			fin = true;
			value -= 8;
		}
		if (value >= 4) {
			broadcasted = true;
			value -= 4;
		}
	}
	
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
		if (fin) {
			value += 8;
		}
		if (broadcasted) {
			value += 4;
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
		this.payloadLength = message.getBytes().length;
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
}
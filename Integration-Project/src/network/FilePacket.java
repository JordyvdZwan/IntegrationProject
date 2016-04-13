package network;

import java.util.Arrays;

public class FilePacket {

	byte[] data;
	int sequenceNumber;
	int totalAmount;
	int fileNumber;
	
	public FilePacket() {
	}
	
	public FilePacket(byte[] bytes) {
		sequenceNumber = byteArrayToInt(Arrays.copyOfRange(bytes, 0, 4));
		totalAmount = byteArrayToInt(Arrays.copyOfRange(bytes, 4, 8));
		fileNumber = byteArrayToInt(Arrays.copyOfRange(bytes, 8, 12));
		data = Arrays.copyOfRange(bytes, 12, bytes.length);
	}
	
	public byte[] getBytes() {
		byte[] result = new byte[12 + data.length];
		
		byte[] sequenceNumberB = intToByteArray(sequenceNumber);
		result[0] = sequenceNumberB[0];
		result[1] = sequenceNumberB[1];
		result[2] = sequenceNumberB[2];
		result[3] = sequenceNumberB[3];
		
		byte[] totalAmountB = intToByteArray(totalAmount);
		result[4] = totalAmountB[0];
		result[5] = totalAmountB[1];
		result[6] = totalAmountB[2];
		result[7] = totalAmountB[3];
		
		byte[] fileNumberB = intToByteArray(fileNumber);
		result[8] = fileNumberB[0];
		result[9] = fileNumberB[1];
		result[10] = fileNumberB[2];
		result[11] = fileNumberB[3];
		
		System.arraycopy(data, 0, result, 12, data.length);
		
		return result;
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
	
	public int getFileNumber() {
		return fileNumber;
	}

	public void setFileNumber(int fileNumber) {
		this.fileNumber = fileNumber;
	}
	
	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public int getSequenceNumber() {
		return sequenceNumber;
	}

	public void setSequenceNumber(int sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}

	public int getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(int totalAmount) {
		this.totalAmount = totalAmount;
	}
	
}

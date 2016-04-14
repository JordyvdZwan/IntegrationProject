package network;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import application.Controller;

public class FileManager {

	Controller controller;
	
	/**
	 * Maximum amount of bytes of data in the file packets.
	 */
	public static final int PACKETSIZE = 800;
	
	//======================================================================================================================
	//||                                                  Constructor                                                     ||  
	//======================================================================================================================
	
	public FileManager(Controller controller) {
		this.controller = controller;
	}
	
	//======================================================================================================================
	//||                                             File sending methods:                                                ||  
	//======================================================================================================================
	
	/**
	 * JRTVPackets that need to be send out.
	 */
	List<JRTVPacket> packetsToBeSend = new ArrayList<JRTVPacket>();
	
	/**
	 * Known sequence numbers to be connected to a file packet.
	 * If such a sequence number is received it will send a new packet.
	 */
	List<Integer> knownSeqNumbers = new ArrayList<Integer>();
	
	/**
	 * this method checks if the given ack is connected to a file packet and if so sends a new packet.
	 * @param acknr acknr to be checked.
	 */
	public void handleFileAck(Integer acknr) {
		if (knownSeqNumbers.contains(acknr)) {
			knownSeqNumbers.remove(acknr);
			sendPackets(1);
		}
	}
	
	/**
	 * Puts the sequence number in the map to signal when this packet is acked you can send a new packet.
	 * @param seqNr seq number to be registered
	 */
	public void registerSeqNr(int seqNr, int fileNr) {
		knownSeqNumbers.add(seqNr);
	}
	
	/**
	 * Send an amount of packets on the front of the to be send packets list.
	 * @param amount to be send amount.
	 */
	private void sendPackets(int amount) {
		int counter = amount;
		while (counter > 0) {
			if (packetsToBeSend.size() > 0) {
				JRTVPacket packet = packetsToBeSend.get(0);
				if (controller.getRouter().getForwardingTable()
									.getTable().containsKey(packet.getDestination())) {
					packetsToBeSend.remove(0);
					controller.sendPacket(packet.getDestination(), packet);
					counter--;
				}
			} else {
				break;
			}
		}
	}
	
	/**
	 * This method will convert a file to a byte array and split it.
	 * Then it will put it into filePacktets.
	 * And those filepackets will be put in a JRTVPacket ready for being send.
	 * @param file file to be send.
	 * @param client destination.
	 */
	public void sendFile(File file, int client) {
		byte[] fileBytes = new byte[0];
		try {
			fileBytes = FileUtils.readFileToByteArray(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		List<byte[]> fileParts = new ArrayList<byte[]>();
		
		boolean busy = true;
		while (busy) {
			if (fileBytes.length >= PACKETSIZE) {
				byte[] data = new byte[PACKETSIZE];
				byte[] temp = new byte[fileBytes.length - PACKETSIZE];
				System.arraycopy(fileBytes, 0, data, 0, PACKETSIZE);
				System.arraycopy(fileBytes, PACKETSIZE, temp, 0, fileBytes.length - PACKETSIZE);
				fileBytes = temp;
				fileParts.add(data);
			} else {
				busy = false;
				if (fileBytes.length != 0) {
					fileParts.add(fileBytes);
				}
			}
		}
		
		List<FilePacket> filePackets = new ArrayList<FilePacket>();
		
		int fileNumber = (int) (Math.random() * Integer.MAX_VALUE);
		int totalAmount = fileParts.size() + 1;
		
		FilePacket namePacket = new FilePacket();
		namePacket.setData(file.getName().getBytes());
		namePacket.setFileNumber(fileNumber);
		namePacket.setSequenceNumber(1);
		namePacket.setTotalAmount(totalAmount);
		
		filePackets.add(namePacket);
		
		int counter = 2;
		for (byte[] byteArray : fileParts) {
			FilePacket filePacket = new FilePacket();
			filePacket.setData(byteArray);
			filePacket.setFileNumber(fileNumber);
			filePacket.setSequenceNumber(counter);
			filePacket.setTotalAmount(totalAmount);
			filePackets.add(filePacket);
			counter++;
		}
		
		List<JRTVPacket> packets = new ArrayList<JRTVPacket>();
		
		int ip = client;
		
		for (FilePacket filePacket : filePackets) {
			JRTVPacket packet = new JRTVPacket("");
			packet.setByteMessage(filePacket.getBytes());
			packet.setFile(true);
			packet.setDestination(ip);
			packets.add(packet);
		}
		
		packetsToBeSend.addAll(packets);
		sendPackets(10);
	}
	
	//======================================================================================================================
	//||                                           File receiving methods:                                                ||  
	//======================================================================================================================
	
	/**
	 * Map with all file parts.
	 */
	Map<Integer, List<FilePacket>> receivedFilePackets = new HashMap<Integer, List<FilePacket>>();
	
	/**
	 * Add the previous filepacket to the list and check if you have all the parts of that file.
	 * if you have all the parts you will put the file together and write it to the disk.
	 * @param packet received JRTVPacket containing the FilePacket.
	 */
	public void handleFilePacket(JRTVPacket packet) {
		FilePacket filePacket = new FilePacket(packet.getByteMessage());
		if (!receivedFilePackets.containsKey(packet.getSource())) {
			receivedFilePackets.put(packet.getSource(), new ArrayList<FilePacket>());
		}
		receivedFilePackets.get(packet.getSource()).add(filePacket);
		
		if (isComplete(filePacket.getFileNumber(), 
							filePacket.getTotalAmount(), 
									packet.getSource())) {
			List<byte[]> byteList = getFileBytes(packet.getSource(), 
												filePacket.getFileNumber(), 
													filePacket.getTotalAmount());
			
			if (!byteList.isEmpty()) {
				byte[] nameBytes = byteList.get(0);
				byteList.remove(0);
				String name = new String(nameBytes);
				
				byte[] bytes = new byte[0];
				byte[] temp;
				for (int i = 0; i < byteList.size(); i++) {
					byte[] array = byteList.get(i);
					temp = new byte[bytes.length + array.length];
					System.arraycopy(bytes, 0, temp, 0, bytes.length);
					System.arraycopy(array, 0, temp, bytes.length, array.length);
					bytes = temp;
				}
				
				File file = new File(name);
				
				try {
					FileUtils.writeByteArrayToFile(file, bytes);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				controller.addMessageToView("New file was downloaded: " + name, packet.getSource());
				controller.showFile(file, packet.getSource());
			}
		}
	}
	
	/**
	 * This method checks if a given file is complete and ready for being put together.
	 * @param fileNumber Number of the file that is received.
	 * @param maxAmount Amount of file packets this file has.
	 * @param source Sourc address of client.
	 * @return true if all parts are present otherwise false.
	 */
	private boolean isComplete(Integer fileNumber, Integer maxAmount, Integer source) {
		for (int i = 1; i <= maxAmount; i++) {
			if (!receivedFilePart(source, fileNumber, i)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * This Method looks if it can find a certain file part.
	 * @param source source ip of the sender.
	 * @param fileNumber filenumber of the file that needs to be checked.
	 * @param seqNr sequence number of the part you want to check.
	 * @return true if the file part is present.
	 */
	private boolean receivedFilePart(int source, int fileNumber, int seqNr) {
		for (FilePacket filePacket : receivedFilePackets.get(source)) {
			if (filePacket.fileNumber == fileNumber && filePacket.sequenceNumber == seqNr) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * This method returns the entire file in form of a list with byte arrays.
	 * @param source source ip of the sender.
	 * @param fileNumber filenumber of the wanted file.
	 * @param maxAmount amount of packets that belong to this file.
	 * @return a list with bytearrays containing the file.
	 */
	private List<byte[]> getFileBytes(int source, int fileNumber, int maxAmount) {
		List<byte[]> res = new ArrayList<byte[]>();
		for (int i = 1; i <= maxAmount; i++) {
			res.add(getByteArray(source, fileNumber, i));
		}
		return res;
	}
	
	/**
	 * This method returns a single byte array.
	 * @param source ip of the sender.
	 * @param fileNumber file numer of the file you want.
	 * @param seqNr sequence number of the file part you want.
	 * @return byte array that corresponds to the criteria.
	 */
	private byte[] getByteArray(int source, int fileNumber, int seqNr) {
		for (FilePacket filePacket : receivedFilePackets.get(source)) {
			if (filePacket.fileNumber == fileNumber && filePacket.sequenceNumber == seqNr) {
				return filePacket.getData();
			}
		}
		return null;
	}
}

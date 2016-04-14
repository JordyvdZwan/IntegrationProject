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
	
	List<JRTVPacket> packetsToBeSend = new ArrayList<JRTVPacket>();
	List<Integer> knownSeqNumbers = new ArrayList<Integer>();
	
	public void handleFileAck(Integer acknr) {
		if (knownSeqNumbers.contains(acknr)) {
			knownSeqNumbers.remove(acknr);
			sendPackets(1);
		}
	}
	
	public void registerSeqNr(int seqNr, int fileNr) {
		knownSeqNumbers.add(seqNr);
	}
	
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
	
	Map<Integer, List<FilePacket>> receivedFilePackets = new HashMap<Integer, List<FilePacket>>();
	
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
	
	private boolean isComplete(Integer fileNumber, Integer maxAmount, Integer source) {
		for (int i = 1; i <= maxAmount; i++) {
			if (!receivedFilePart(source, fileNumber, i)) {
				return false;
			}
		}
		return true;
	}
	
	private boolean receivedFilePart(int source, int fileNumber, int seqNr) {
		for (FilePacket filePacket : receivedFilePackets.get(source)) {
			if (filePacket.fileNumber == fileNumber && filePacket.sequenceNumber == seqNr) {
				return true;
			}
		}
		return false;
	}
	
	private List<byte[]> getFileBytes(int source, int fileNumber, int maxAmount) {
		List<byte[]> res = new ArrayList<byte[]>();
		for (int i = 1; i <= maxAmount; i++) {
			res.add(getByteArray(source, fileNumber, i));
		}
		return res;
	}
	
	private byte[] getByteArray(int source, int fileNumber, int seqNr) {
		for (FilePacket filePacket : receivedFilePackets.get(source)) {
			if (filePacket.fileNumber == fileNumber && filePacket.sequenceNumber == seqNr) {
				return filePacket.getData();
			}
		}
		return null;
	}
}

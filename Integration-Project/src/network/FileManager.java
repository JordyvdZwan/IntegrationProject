package network;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import application.Controller;

public class FileManager {

	Controller controller;
	public static final int PACKETSIZE = 200;
	
	public FileManager(Controller controller) {
		this.controller = controller;
	}
	
	List<JRTVPacket> packetsToBeSend = new ArrayList<JRTVPacket>();
	List<Integer> knownSeqNumbers = new ArrayList<Integer>();
	
	public void handleFileAck(Integer acknr) {
//		System.out.println("Acknr that gets checked: " + acknr);
//		System.out.println("List: ");
//		System.out.println(knownSeqNumbers);
		if (knownSeqNumbers.contains(acknr)) {
			knownSeqNumbers.remove(acknr);
			sendPackets(1);
		}
	}
	
	public void registerSeqNr(int seqNr, int fileNr) {
//		System.out.println("Seqnr that gets added: " + seqNr);
		knownSeqNumbers.add(seqNr);
	}
	
	private void sendPackets(int amount) {
		while (amount > 0) {
			if (packetsToBeSend.size() > 0) {
				JRTVPacket packet = packetsToBeSend.get(0);
				packetsToBeSend.remove(0);
				controller.sendPacket(packet.getDestination(), packet);
				amount--;
			} else {
				break;
			}
		}
	}
	
	public void sendFile(File file, int client) {
		System.out.println("Reading File and converting to bytes");
		byte[] fileBytes = new byte[0];
		try {
			fileBytes = FileUtils.readFileToByteArray(file);
//			fileBytes = Files.readAllBytes(file.toPath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("fileBytes Length: " + fileBytes.length);
		System.out.println("Breaking file up into parts");
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
		System.out.println("fileParts Length: " + fileParts.size());
		System.out.println("putting bytes in filePackets");
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
		System.out.println("putting filePackets in JRTVPackets");
		
		List<JRTVPacket> packets = new ArrayList<JRTVPacket>();
		
		int ip = client;
		
		for (FilePacket filePacket : filePackets) {
			JRTVPacket packet = new JRTVPacket(new String(filePacket.getBytes()));
			packet.setFile(true);
			packet.setDestination(ip);
			packets.add(packet);
			System.out.println("seq nr: " + filePacket.getSequenceNumber());
		}
		
		packetsToBeSend.addAll(packets);
		System.out.println("Packets Length: " + packets.size());
		System.out.println("Sending first ten (or so) packets");
		System.out.println("PacketsToBeSend Length: " + packetsToBeSend.size());
		sendPackets(10);
		System.out.println("Done initializing file Stream.");
	}
	
	Map<Integer, List<FilePacket>> receivedFilePackets = new HashMap<Integer, List<FilePacket>>();
	
	public void handleFilePacket(JRTVPacket packet) {
		FilePacket filePacket = new FilePacket(packet.getByteMessage());
		System.out.println("seq nr: " + filePacket.getSequenceNumber());
		System.out.println("file nr: " + filePacket.getFileNumber());
		System.out.println("packet length: " + packet.getPayloadLength());
		if (!receivedFilePackets.containsKey(packet.getSource())) {
			receivedFilePackets.put(packet.getSource(), new ArrayList<FilePacket>());
		}
		receivedFilePackets.get(packet.getSource()).add(filePacket);
		
		System.out.println("Complete? " + isComplete(filePacket.getFileNumber(), filePacket.getTotalAmount(), packet.getSource()));
		if (isComplete(filePacket.getFileNumber(), filePacket.getTotalAmount(), packet.getSource())) {
			System.out.println("Started to gather all file bytes");
			List<byte[]> byteList = getFileBytes(packet.getSource(), filePacket.getFileNumber(), filePacket.getTotalAmount());
			
			System.out.println("Read Name Byte");
			byte[] nameBytes = byteList.get(0);
			byteList.remove(0);
			String name = new String(nameBytes);
			
			System.out.println("Started creating one big bytearray");
			byte[] bytes = new byte[0];
			byte[] temp;
			for (byte[] array : byteList) {
				temp = new byte[bytes.length + array.length];
				System.arraycopy(bytes, 0, temp, 0, bytes.length);
				System.arraycopy(array, 0, temp, bytes.length, array.length);
				bytes = temp;
			}
			
			System.out.println("Bytes length: " + bytes.length);
			
			try {
				FileUtils.writeByteArrayToFile(new File(name), bytes);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			controller.addMessageToView("New file was downloaded: " + name, packet.getSource());
			
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

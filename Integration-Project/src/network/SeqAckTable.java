package network;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import application.Controller;

public class SeqAckTable {
	
	Controller controller;
	public static final int TIMEOUT = 1000;
	Map<Integer, Integer> seqNrs = new HashMap<Integer, Integer>();
	Map<Integer, Map<Integer, Boolean>> send = new HashMap<Integer, Map<Integer, Boolean>>();
	Map<Integer, List<Integer>> receivedSeqNrs = new HashMap<Integer, List<Integer>>();
	
	//======================================================================================================================
	//||                                               Constructor:                                                       ||  
	//======================================================================================================================
	
	public SeqAckTable(Controller controller) {
		this.controller = controller;
	}
	
	//======================================================================================================================
	//||                                              checking methods:                                                   ||  
	//======================================================================================================================
	
	public boolean isReceivedSeqNr(Integer source, Integer seq) {
		boolean found = false;
		if (receivedSeqNrs.containsKey(source)) {
			found = receivedSeqNrs.get(source).contains(seq);
		}
		if (seq == 0) {
			found = false;
		}
		return found;
	}
	
	public Integer getNextSeq(Integer address) {
		Integer res = 0;
		if (seqNrs.containsKey(address)) {
			res = seqNrs.get(address) + 1;
		} else {
			res = (int) (Math.random() * 9000000);
		}
		seqNrs.put(address, res);
		return res;
	}
	
	public Boolean isReceived(Integer address, Integer seq) {
		Boolean res = false;
		if (send.containsKey(address)) {
			if (send.get(address).containsKey(seq)) {
				res = send.get(address).get(seq);
			}
		}
		return res;
	}
	
	public void retransmit(JRTVPacket packet, int destination) {
		controller.retransmit(packet, destination);
	}
	
	//======================================================================================================================
	//||                                           registering methods:                                                   ||  
	//======================================================================================================================
	
	public void addReceivedSeqNr(Integer source, Integer seq) {
		if (!receivedSeqNrs.containsKey(source)) {
			receivedSeqNrs.put(source, new ArrayList<Integer>());
		}
		receivedSeqNrs.get(source).add(seq);
	}
	
	public void registerAckPacket(JRTVPacket packet) {
		System.out.println("In de registerAck ackNr: \n" + packet.getAcknr());
		int address = packet.getSource();
		int seq = packet.getAcknr();
		if (send.containsKey(address)) {
			if (send.get(address).containsKey(seq)) {
				send.get(address).put(seq, true);
			}
		}
		controller.getFileManager().handleFileAck(packet.getAcknr());
	}
	
	public void registerSendPacket(JRTVPacket packet) {
		if (packet.isFile()) {
			controller.getFileManager().registerSeqNr(packet.getSeqnr(),
									new FilePacket(packet.getByteMessage()).getSequenceNumber());
		}
		if (packet.getDestination() == Controller.multicastAddress) {
			for (Integer integer : controller.getForwardingTable().keySet()) {
				if (integer != controller.getLocalIAddress() 
													&& integer != Controller.multicastAddress) {
					if (!send.containsKey(integer)) {
						send.put(integer, new HashMap<Integer, Boolean>());
					}
					System.out.println("putting in: " 
										+ Router.getStringIP(integer) + " " + packet.getSeqnr());
					send.get(integer).put(packet.getSeqnr(), false);
				}
			}
		} else {
			if (!send.containsKey(packet.getDestination())) {
				send.put(packet.getDestination(), new HashMap<Integer, Boolean>());
			}
			send.get(packet.getDestination()).put(packet.getSeqnr(), false);
		}
		if (controller.getForwardingTable().keySet().size() > 0) {
			TimeOutTimer timeout = new TimeOutTimer(packet, this);
			timeout.setDaemon(true);
			timeout.start();
		}
	}
	
	//======================================================================================================================
	//||                                               get/set methods:                                                   ||  
	//======================================================================================================================
	
	public Controller getController() {
		return controller;
	}
}


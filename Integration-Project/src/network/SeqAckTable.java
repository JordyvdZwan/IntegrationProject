package network;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import application.Controller;

public class SeqAckTable {
	
	Controller controller;
	public static final int TIMEOUT = 1000;
	
	public SeqAckTable(Controller controller) {
		this.controller = controller;
	}
	
	Map<Integer, Integer> seqNrs = new HashMap<Integer, Integer>();
	Map<Integer, Map<Integer, Boolean>> received = new HashMap<Integer, Map<Integer, Boolean>>();
	Map<Integer, List<Integer>> receivedSeqNrs = new HashMap<Integer, List<Integer>>();
	
	public void addReceivedSeqNr(Integer source, Integer seq) {
		if (!receivedSeqNrs.containsKey(source)) {
			receivedSeqNrs.put(source, new ArrayList<Integer>());
		}
		receivedSeqNrs.get(source).add(seq);
	}
	
	public boolean isReceivedSeqNr(Integer source, Integer seq) {
		boolean found = false;
		if (receivedSeqNrs.containsKey(source)) {
			found = receivedSeqNrs.get(source).contains(seq);
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
		Boolean res = null;
		if (received.containsKey(address)) {
			if (received.get(address).containsKey(seq)) {
				res = received.get(address).get(seq);
			}
		}
		return res;
	}
	
	public void removeReceived(Integer address, Integer seq) {
		if (received.containsKey(address)) {
			if (received.get(address).containsKey(seq)) {
				received.get(address).remove(seq);
			}
		}
	}
	
	public void retransmit(JRTVPacket packet, int destination) {
		controller.retransmit(packet, destination);
	}
	
	public void registerAckPacket(JRTVPacket packet) {
		int address = packet.getSource();
		int seq = packet.getAcknr();
		if (received.containsKey(address)) {
			if (received.get(address).containsKey(seq)) {
				received.get(address).put(seq, true);
			}
		}
	}
	
	public void registerSendPacket(JRTVPacket packet) {
		if (packet.getDestination() == Controller.multicastAddress) {
			System.out.println("peop");
			for (Integer integer : controller.getForwardingTable().keySet()) {
				if (integer != controller.getLocalIAddress()) {
					if (!received.containsKey(packet.getDestination())) {
						received.put(packet.getDestination(), new HashMap<Integer, Boolean>());
					}
					received.get(packet.getDestination()).put(packet.getSeqnr(), false);
				}
			}
		} else {
			System.out.println("poep");
			if (!received.containsKey(packet.getDestination())) {
				received.put(packet.getDestination(), new HashMap<Integer, Boolean>());
			}
			received.get(packet.getDestination()).put(packet.getSeqnr(), false);
		}
	}
	
	public Controller getController() {
		return controller;
	}
}


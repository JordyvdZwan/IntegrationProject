package network;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import application.Controller;

public class SeqAckTable {
	
	Controller controller;
	public static final int TIMEOUT = 1000;
	
	/**
	 * Map of address to next sequence number to use.
	 */
	Map<Integer, Integer> seqNrs = new HashMap<Integer, Integer>();
	
	/**
	 * Map of address to sequence numbers and if they have been acked.
	 */
	Map<Integer, Map<Integer, Boolean>> send = new HashMap<Integer, Map<Integer, Boolean>>();
	
	/**
	 * map of address to list of received sequence numbers.
	 */
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
	
	/**
	 * This method returns whether the sequence numer has been received from a certain person.
	 * @param source Address of source.
	 * @param seq sequence number you want to check.
	 * @return whether you have received that sequence number before.
	 */
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
	
	/**
	 * This method gets the next sequence number you should be using given a destination ip address.
	 * @param address ip address of the destination.
	 * @return the next sequence number.
	 */
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
	
	/**
	 * This method returns whether the Ack number has been received from a certain person.
	 * @param address Address of source.
	 * @param seq sequence number you want to check.
	 * @return whether you have received that sequence number before.
	 */
	public Boolean isReceived(Integer address, Integer seq) {
		Boolean res = false;
		if (send.containsKey(address)) {
			if (send.get(address).containsKey(seq)) {
				res = send.get(address).get(seq);
			}
		}
		return res;
	}
	
	/**
	 * This method relays the packet to the controller which will retransmit it.
	 * @param packet packet to be retransmitted.
	 * @param destination Destination of the packet.
	 */
	public void retransmit(JRTVPacket packet, int destination) {
		controller.retransmit(packet, destination);
	}
	
	//======================================================================================================================
	//||                                           registering methods:                                                   ||  
	//======================================================================================================================
	
	/**
	 * This method registers all incoming sequence numbers.
	 * @param source source of the packet.
	 * @param seq sequence number of the packet.
	 */
	public void addReceivedSeqNr(Integer source, Integer seq) {
		if (!receivedSeqNrs.containsKey(source)) {
			receivedSeqNrs.put(source, new ArrayList<Integer>());
		}
		receivedSeqNrs.get(source).add(seq);
	}
	
	/**
	 * This method registers the ack number of the received packet.
	 * It will thus make sure the timeout will not be triggered by entering a received true.
	 * @param packet acknowledgement packet to be registered.
	 */
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
	
	/**
	 * This method registers the sequence number of packet and starts a timeout.
	 * It also sends the sequence number to the fileManager if it is a file packet.
	 * @param packet packet to be registered.
	 */
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


package network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import application.Controller;

public class SeqAckTable {
	
	Controller controller;
	
	public SeqAckTable(Controller controller) {
		this.controller = controller;
	}
	
	Map<Integer[], Boolean> table = new HashMap<Integer[], Boolean>();
	
	List<ClientPacketHistory> history = new ArrayList<ClientPacketHistory>();
	
	
	public int[] getSeqAck(String client, boolean broadcasted) {
		return getSeqAck(controller.getRouter().getIntIP(client), broadcasted);
	}
	
	public boolean checkIfAckReceived(Integer[] data) {
		return table.get(data);
	}
	
	public int[] getSeqAck(int client, boolean broadcasted) {
		int[] res = new int[2];
		res[0] = getHighestSeq(client, broadcasted);
		if (getHighestAck(client, broadcasted) != null) {
			res[1] = getHighestAck(client, broadcasted);
		} else {
			res[1] = res[0];
		}
		return res;
	}
	
	public Integer getHighestSeq(int client, boolean broadcasted) {
		Integer max = null;
		Integer ref = null;
//		while (max == ref) {
			for (int i = 0; i < history.size(); i++) {
				if (history.get(i).getDestination() == client && history.get(i).isBroadcasted() == broadcasted && history.get(i).getSeq() + 1 > max) {
					max = history.get(i).getSeq();
				} else if (max == null && history.get(i).getDestination() == client && 
						history.get(i).isBroadcasted() == broadcasted) { //+ 1??
					max = history.get(i).getSeq();
				}
			}
//		}
		if (max == null) {
			max = (int) (Math.random() * 900000);
		}
		return max;
	}
	
	public boolean received(int seq, int ack, boolean broadcasted, int source) {
		for (ClientPacketHistory h : history) {
			if (h.getSeq() == seq && h.getAck() == ack && h.isBroadcasted() == broadcasted && source == h.getSource()) {
				return true;
			}
		}
		return false;
	}
	
	public Integer getHighestAck(int client, boolean broadcasted) {
		Integer max = null;
		Integer ref = null;
		while (max != ref) {
			for (int i = 0; i < history.size(); i++) {
				if (history.get(i).getDestination() == controller.getLocalIAddress() && history.get(i).isBroadcasted() == broadcasted && history.get(i).getSeq() + 1 > max && max + history.get(i).getLength() == history.get(i).getSeq()) {
					max = history.get(i).getSeq() + 1;
				} else if (max == null && history.get(i).getDestination() == controller.getLocalIAddress() && 
						history.get(i).isBroadcasted() == broadcasted) { //+ 1??
					max = history.get(i).getSeq() + 1;
				}
			}
		}
		return max;
	}
	
	public void receivedPackage(JRTVPacket packet) {
		int b;
		if (packet.isBroadcasted()) {
			b = 1;
		} else {
			b = 0;
		}
		if (getKey(packet.getSeqnr(), packet.getAcknr(), packet.getDestination(), b) != null) {
			table.put(getKey(packet.getSeqnr(), packet.getAcknr(), packet.getDestination(), b), true);
		}
		for (Integer[] integers : table.keySet()) {
			if (integers != null) {
				if (integers[3] == packet.getDestination() && b == integers[4] && table.get(integers) == false && packet.getAcknr() > integers[2]) {
					table.put(integers, true);
				}
			}
		}
		history.add(new ClientPacketHistory(packet.getSeqnr(), packet.getAcknr(), packet.getPayloadLength(),packet.getDestination(), packet.getSource() ,packet.isBroadcasted()));
	}
	
	private Integer[] getKey(int seq, int ack, int destination, int broadcasted) {
		for (Integer[] integers : table.keySet()) {
			if (integers[0] == seq && integers[1] == ack && integers[2] == destination && integers[3] == broadcasted) {
				return integers;
			}
		}
		return null;
	}
	
	public void registerOutgoingPackage(JRTVPacket packet) {
		Integer[] data = new Integer[4];
		if (packet.getDestination() == Controller.multicastAddress) {
			for (Integer integer : controller.getForwardingTable().keySet()) {
				if (integer != controller.getLocalIAddress()) {
					data[0] = packet.getSeqnr();
					data[1] = packet.getAcknr();
					data[2] = integer;
					data[3] = 1;
					table.put(data, false);
					packets.put(data, packet);
					history.add(new ClientPacketHistory(packet.getSeqnr(), packet.getAcknr(), packet.getPayloadLength(),packet.getDestination(), packet.getSource(), packet.isBroadcasted()));
					TimeOutTimer timeout = new TimeOutTimer(data, this);
					timeout.start();
				}
			}
		} else {
			data[0] = packet.getSeqnr();
			data[1] = packet.getAcknr();
			data[2] = packet.getDestination();
			data[3] = 0;
			table.put(data, false);
			packets.put(data, packet);
			history.add(new ClientPacketHistory(packet.getSeqnr(), packet.getAcknr(), packet.getPayloadLength(),packet.getDestination(), packet.getSource() ,packet.isBroadcasted()));
			TimeOutTimer timeout = new TimeOutTimer(data, this);
			timeout.start();
		}
	}
	
	private Map<Integer[], JRTVPacket> packets = new HashMap<Integer[], JRTVPacket>();
	
	public void retransmit(Integer[] data) {
		controller.retransmit(packets.get(data));
	}
	
	public void removePacket(Integer[] data) {
		packets.remove(data);
	}
}

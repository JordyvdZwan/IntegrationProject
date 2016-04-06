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
	
	public int[] getSeqAck(int client, boolean broadcasted) {
		int[] res = new int[2];
//		res[0] = highest seq + length
//		res[1] = highest received seq + 1 without gaps
		return res;
	}
	
	public Integer getHighestAck(int client, boolean broadcasted) {
		Integer max = null;
		Integer ref = null;
		while (max == ref) {
			for (int i = 0; i < history.size(); i++) {
				if (history.get(i).getDestination() == client && history.get(i).isBroadcasted() == broadcasted && history.get(i).getSeq() + 1 > max) {
					max = history.get(i).getSeq() + 1;
				}
			}
		}
		return max;
	}
	
	public void receivedAckPackage(JRTVPacket packet) {
		int b;
		if (packet.isBroadcasted()) {
			b = 1;
		} else {
			b = 0;
		}
		table.put(getKey(packet.getSeqnr(), packet.getAcknr(), packet.getDestination(), b), true);
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
				data[0] = packet.getSeqnr();
				data[1] = packet.getAcknr();
				data[2] = packet.getDestination();
				data[3] = 1;
				table.put(data, false);
			}
		} else {
			data[0] = packet.getSeqnr();
			data[1] = packet.getAcknr();
			data[2] = packet.getDestination();
			data[3] = 0;
			table.put(data, false);
		}
	}
}

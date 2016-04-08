package network;

import java.util.HashMap;

import application.Controller;

public class TimeOutTimer extends Thread {
	
	private int timeout = 5000;
	private SeqAckTable table;
	private JRTVPacket packet;
	
	public TimeOutTimer(JRTVPacket packet, SeqAckTable table, int timeout) {
		this.table = table;
		this.packet = packet;
		this.timeout = timeout;
	}
	
	public TimeOutTimer(JRTVPacket packet, SeqAckTable table) {
		this.table = table;
		this.packet = packet;
	}
	
	public void run() {
		try {
			this.sleep(timeout);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("Is the destination the multicast address? : " + (packet.getDestination() == Controller.multicastAddress));
		if (packet.getDestination() == Controller.multicastAddress) {
			System.out.println("Forwarding table keys : " + table.getController().getForwardingTable().keySet().toString() );
			for (Integer integer : table.getController().getForwardingTable().keySet()) {
				System.out.println("Is it a valid address?" + (integer != table.getController().getLocalIAddress() && integer != table.getController().multicastAddress));
				if (integer != table.getController().getLocalIAddress() && integer != table.getController().multicastAddress) {
					System.out.println("Is the packet received? : " + !table.isReceived(integer, packet.getSeqnr()));
					if (!table.isReceived(integer, packet.getSeqnr())) {
						table.retransmit(packet, integer);
					}
				}
			}
		} else {
			if (!table.isReceived(packet.getDestination(), packet.getSeqnr()) && table.getController().getForwardingTable().keySet().contains(packet.getDestination())) {
				table.retransmit(packet, packet.getDestination());
			}
		}
//		table.removeReceived(packet.getDestination(), packet.getSeqnr());
		this.interrupt();
	}
	
}

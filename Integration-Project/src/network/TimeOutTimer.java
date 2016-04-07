package network;

import java.util.HashMap;

import application.Controller;

public class TimeOutTimer extends Thread {
	
	private int timeout = 1000;
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
		if (packet.getDestination() == Controller.multicastAddress) {
			for (Integer integer : table.getController().getForwardingTable().keySet()) {
				if (integer != table.getController().getLocalIAddress()) {
					
				}
			}
		} else {
			table.get
		}
	}
	
}

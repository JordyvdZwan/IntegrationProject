package network;

import java.util.HashMap;
import java.util.Set;

import application.Controller;

public class TimeOutTimer extends Thread {
	
	private int timeout = 1000;
	private SeqAckTable table;
	private JRTVPacket packet;
	
	public TimeOutTimer(JRTVPacket packet, SeqAckTable table, int timeout) {
		this.table = table;
		this.packet = new JRTVPacket(packet.getMessage());
		this.packet.setByteMessage(packet.getByteMessage());
		this.packet.setAck(packet.isAck());
		this.packet.setSyn(packet.isSyn());
		this.packet.setUpdate(packet.isUpdate());
		this.packet.setNormal(packet.isNormal());
		this.packet.setFin(packet.isFin());
		this.packet.setBroadcasted(packet.isBroadcasted());
		this.packet.setRSA(packet.isRSA());
		this.packet.setDiffie(packet.isDiffie());
		
		this.packet.setAcknr(packet.getAcknr());
		this.packet.setSeqnr(packet.getSeqnr());
		this.packet.setSource(packet.getSource());
		this.packet.setDestination(packet.getDestination());
		this.packet.setHashPayload(packet.getHashPayload());
		this.packet.setNextHop(packet.getNextHop());
		
		this.timeout = timeout;
	}
	
	public TimeOutTimer(JRTVPacket packet, SeqAckTable table) {
		this.table = table;
		this.packet = new JRTVPacket(packet.getMessage());
		this.packet.setByteMessage(packet.getByteMessage());
		this.packet.setAck(packet.isAck());
		this.packet.setSyn(packet.isSyn());
		this.packet.setUpdate(packet.isUpdate());
		this.packet.setNormal(packet.isNormal());
		this.packet.setFin(packet.isFin());
		this.packet.setBroadcasted(packet.isBroadcasted());
		this.packet.setRSA(packet.isRSA());
		this.packet.setDiffie(packet.isDiffie());
		
		this.packet.setAcknr(packet.getAcknr());
		this.packet.setSeqnr(packet.getSeqnr());
		this.packet.setSource(packet.getSource());
		this.packet.setDestination(packet.getDestination());
		this.packet.setHashPayload(packet.getHashPayload());
		this.packet.setNextHop(packet.getNextHop());
	}
	
	public void run() {
		try {
			this.sleep(timeout);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
//		System.out.println("Is the destination the multicast address? : " + (packet.getDestination() == Controller.multicastAddress));
		if (packet.getDestination() == Controller.multicastAddress) {
//			System.out.println("Forwarding table keys : " + table.getController().getForwardingTable().keySet().toString() );
			Set<Integer> i = table.getController().getForwardingTable().keySet();
			for (Integer integer : i) {
//				System.out.println("Is it a valid address?" + (integer != table.getController().getLocalIAddress() && integer != table.getController().multicastAddress));
				if (integer != table.getController().getLocalIAddress() && integer != table.getController().multicastAddress) {
//					System.out.println("Is the packet received? : " + !table.isReceived(integer, packet.getSeqnr()));
					if (!table.isReceived(integer, packet.getSeqnr())) {
						System.out.println("======================== Before retransmission ==============================================");
						System.out.println(packet.getMessage());
						System.out.println("=============================================================================================");
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

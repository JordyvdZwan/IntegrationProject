package network;

import java.util.Set;

import application.Controller;

public class TimeOutTimer extends Thread {
	
	private int timeout = 1500;
	private SeqAckTable table;
	private JRTVPacket packet;
	
	public TimeOutTimer(JRTVPacket packet, SeqAckTable table, int timeout) {
		this.table = table;
		this.packet = new JRTVPacket("");
		this.packet.setByteMessage(packet.getByteMessage());
		this.packet.setAck(packet.isAck());
		this.packet.setSyn(packet.isSyn());
		this.packet.setUpdate(packet.isUpdate());
		this.packet.setNormal(packet.isNormal());
		this.packet.setFile(packet.isFile());
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
		this.packet = new JRTVPacket("");
		this.packet.setByteMessage(packet.getByteMessage());
		this.packet.setAck(packet.isAck());
		this.packet.setSyn(packet.isSyn());
		this.packet.setUpdate(packet.isUpdate());
		this.packet.setNormal(packet.isNormal());
		this.packet.setFile(packet.isFile());
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
			sleep(timeout);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (packet.getDestination() == Controller.multicastAddress) {
			Set<Integer> i = table.getController().getForwardingTable().keySet();
			for (Integer integer : i) {
				if (integer != table.getController().getLocalIAddress() 
									&& integer != Controller.multicastAddress) {
					if (!table.isReceived(integer, packet.getSeqnr())) {
						JRTVPacket p = new JRTVPacket("");
						p.setByteMessage(packet.getByteMessage());
						p.setAck(packet.isAck());
						p.setSyn(packet.isSyn());
						p.setUpdate(packet.isUpdate());
						p.setNormal(packet.isNormal());
						p.setFile(packet.isFile());
						p.setBroadcasted(packet.isBroadcasted());
						p.setRSA(packet.isRSA());
						p.setDiffie(packet.isDiffie());
						
						p.setAcknr(packet.getAcknr());
						p.setSeqnr(packet.getSeqnr());
						p.setSource(packet.getSource());
						p.setDestination(packet.getDestination());
						p.setHashPayload(packet.getHashPayload());
						p.setNextHop(packet.getNextHop());
						
						table.retransmit(p, integer);
					}
				}
			}
		} else {
			if (!table.isReceived(packet.getDestination(), packet.getSeqnr()) 
					  && table.getController()
					  		.getForwardingTable().keySet().contains(packet.getDestination())) {
				table.retransmit(packet, packet.getDestination());
			}
		}
		this.interrupt();
	}
	
}

package network;

public class ClientPacketHistory {
	
	int seq;
	int ack;
	int length;
	boolean broadcasted;
	int destination;
	int source;
	
	public ClientPacketHistory(int seq, int ack, int length, int destination, int source, boolean broadcasted) {
		this.seq = seq;
		this.ack = ack;
		this.length = length;
		this.destination = destination;
		this.broadcasted = broadcasted;
		this.source = source;
	}

	public int getSource() {
		return source;
	}

	public void setSource(int source) {
		this.source = source;
	}

	public int getSeq() {
		return seq;
	}

	public void setSeq(int seq) {
		this.seq = seq;
	}

	public int getAck() {
		return ack;
	}

	public void setAck(int ack) {
		this.ack = ack;
	}

	public int getLength() {
		return length;
	}

	public int getDestination() {
		return destination;
	}

	public void setDestination(int destination) {
		this.destination = destination;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public boolean isBroadcasted() {
		return broadcasted;
	}

	public void setBroadcasted(boolean broadcasted) {
		this.broadcasted = broadcasted;
	}
}

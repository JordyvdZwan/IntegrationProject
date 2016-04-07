package network;

public class TimeOutTimer extends Thread {
	
	public static final int TIMEOUT = 1000;
	SeqAckTable table;
	Integer[] data;
	
	public TimeOutTimer(Integer[] data, SeqAckTable table) {
		this.table = table;
		this.data = data;
	}
	
	public void run() {
		try {
			this.sleep(TIMEOUT);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (table.checkIfAckReceived(data)) {
			table.removePacket(data);
		} else {
			table.retransmit(data);
		}
		this.interrupt();
	}
	
}

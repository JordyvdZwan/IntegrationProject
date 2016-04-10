package network;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import application.Controller;

public class Update extends Thread {
	
	Controller controller;
	public static final long TIMEOUT = 1500;
	public Update(Controller controller) {
		this.controller = controller;
		this.setDaemon(true);
		this.start();
		
	}
	
	public void run() {
		this.setName("Updater");
		while(true) {
			JRTVPacket packet = null;
			if (controller.getSettingUp()) {
				if (controller.getInitString() != null) {
					packet = new JRTVPacket(controller.getInitString());
				}
				try {
					this.sleep(TIMEOUT / 3);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (packet != null) {
					packet.setSyn(true);
					controller.broadcastPacket(packet);
				}
			} else {
				packet = new JRTVPacket(controller.getClientName());
				packet.setHashPayload(controller.getClientName().getBytes().length);
				byte[] bytes = new byte[4 * (2 + (2 * controller.getForwardingTable().keySet().size()))];
				
				bytes[0] = intToByteArray(controller.getLocalIAddress())[0];
				bytes[1] = intToByteArray(controller.getLocalIAddress())[1];
				bytes[2] = intToByteArray(controller.getLocalIAddress())[2];
				bytes[3] = intToByteArray(controller.getLocalIAddress())[3];
				
				bytes[4] = intToByteArray(0)[0];
				bytes[5] = intToByteArray(0)[1];
				bytes[6] = intToByteArray(0)[2];
				bytes[7] = intToByteArray(0)[3];
				
				int counter = 0;
				for (Integer integer : controller.getForwardingTable().keySet()) {
					
					bytes[(counter * 8) + 8] = intToByteArray(integer)[0];
					bytes[(counter * 8) + 9] = intToByteArray(integer)[1];
					bytes[(counter * 8) + 10] = intToByteArray(integer)[2];
					bytes[(counter * 8) + 11] = intToByteArray(integer)[3];
					
					Integer cost = controller.getRouter().getNextHopCost(integer) + 1;
					
					bytes[(counter * 8) + 12] = intToByteArray(cost)[0];
					bytes[(counter * 8) + 13] = intToByteArray(cost)[1];
					bytes[(counter * 8) + 14] = intToByteArray(cost)[2];
					bytes[(counter * 8) + 15] = intToByteArray(cost)[3];
					
					counter++;
				}
				
				packet.setMessage(packet.getMessage() + new String(bytes));
				try {
					this.sleep(TIMEOUT);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (packet != null) {
					packet.setUpdate(true);
					controller.broadcastPacket(packet);
				}
			}
		}
	}
	
	private static byte[] intToByteArray(int a) {
	    return new byte[] {
	        (byte) ((a >> 24) & 0xFF),
	        (byte) ((a >> 16) & 0xFF),   
	        (byte) ((a >> 8) & 0xFF),   
	        (byte) (a & 0xFF)
	    };
	}
}

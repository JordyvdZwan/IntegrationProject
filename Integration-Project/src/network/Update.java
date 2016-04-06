package network;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import application.Controller;

public class Update extends Thread {
	
	Controller controller;
	public static final int TIMEOUT = 3;
	public Update(Controller controller) {
		this.controller = controller;
		this.start();
	}
	
	public void run() {
		while(true) {
			JRTVPacket packet = new JRTVPacket(controller.getClientName());
			packet.setUpdate(true);
			controller.broadcastPacket(packet);
			try {
				TimeUnit.SECONDS.sleep(TIMEOUT);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}

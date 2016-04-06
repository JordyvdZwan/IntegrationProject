package network;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import application.Controller;

public class Update extends Thread {
	
	Controller controller;
	
	public Update(Controller controller) {
		this.controller = controller;
		this.start();
	}
	
	public void run() {
		while(true) {
			JRTVPacket packet = new JRTVPacket(controller.getClientName());
			packet.setUpdate(true);
			controller.broadcastMessage(packet.toByteArray().toString());
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}

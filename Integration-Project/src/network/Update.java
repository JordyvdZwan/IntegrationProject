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
		this.setDaemon(true);
		this.start();
		
	}
	
	public void run() {
		this.setName("Updater");
		while(true) {
			JRTVPacket packet = null;
			if (controller.getSettingUp()) {
				System.out.println(controller.getInitString());
				if (controller.getInitString() != null) {
					packet = new JRTVPacket(controller.getInitString());
				}
			} else {
				packet = new JRTVPacket(controller.getClientName());
				System.out.println(controller.getClientName());
			}
			if (packet != null) {
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
}

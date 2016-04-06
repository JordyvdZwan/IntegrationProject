package network;

import java.net.InetAddress;

import application.Controller;

public class Router {

	private Controller controller;
	
	public Router(Controller controller) {
		this.controller = controller;
	}
	
	public InetAddress getIP(String client) {
		if (client.equals("Anonymous")) {
			return controller.getAddress();
		}
		return null;
	}
}

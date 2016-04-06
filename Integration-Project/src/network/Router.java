package network;

import java.net.InetAddress;

import java.util.Map;
import java.util.HashMap;


import application.Controller;


public class Router {


	public InetAddress getIP() {
		return null;
	}
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

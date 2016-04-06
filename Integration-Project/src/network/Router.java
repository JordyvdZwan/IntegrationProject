package network;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.HashMap;


import application.Controller;


public class Router {
	
	private Controller controller;
	Map<Integer, String> addresstable = new HashMap<Integer, String>();
	
	
	public Router(Controller controller) {
		this.controller = controller;
	}
	
	public InetAddress getIP() {
		return null;
	}
	
	public InetAddress getIP(String client) {
		
		InetAddress result = null;
		
		if (client.equals("Anonymous")) {
			result =  controller.getMulticastAddress();
		} else { 
			for(Integer e: addresstable.keySet()) {
				if(addresstable.get(e).equals(client)) {
					try {
						result = InetAddress.getByName("" + e);
					} catch (UnknownHostException e1) {
						e1.printStackTrace();
					}
					break;
				}
			}
		}

		return result;
	}
	
	public int getLocalIntAddress() {
		return 0;
	}
	
	public int getIntIP(String client) {
		return 0;
	}
	
	public InetAddress getRouteIP(String client) {
		return controller.getMulticastAddress();
	}
	
	
	
	
	public String getName(InetAddress address) {
		return addresstable.get(address);
	}
	
	public void setEntry(Integer address, String name) {
		if(addresstable.containsKey(address)) {
			addresstable.remove(address);
		}
		addresstable.put(address, name);
	}
}

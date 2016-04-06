package network;

import java.net.InetAddress;

import java.util.Map;
import java.util.Map.Entry;
import java.util.HashMap;


import application.Controller;


public class Router {

	private Controller controller;
	Map<InetAddress, String> addresstable = new HashMap<InetAddress, String>();
	
	
	public Router(Controller controller) {
		this.controller = controller;
	}
	
	public InetAddress getIP() {
		return null;
	}
	
	
	public InetAddress getIP(String client) {
		
		InetAddress result = null;
		
		if (client.equals("Anonymous")) {
			result =  controller.getAddress();
		} else { 
			for(InetAddress e: addresstable.keySet()) {
				if(addresstable.get(e).equals(client)) {
					result = e;
				}
			}
		}

		return result;
	}
	
	public String getName(InetAddress address) {
		return addresstable.get(address);
	}
	
	public void setEntry(InetAddress address, String name) {
		
		if(addresstable.containsKey(address)) {
			addresstable.remove(address);
		}
		
		addresstable.put(address, name);
	}
	
	
	
}

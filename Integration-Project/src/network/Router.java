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
	
	public Integer getIP(String client) {
		
		Integer result = null;
		
		if (client.equals("Anonymous")) {
			//result =  controller.getMulticastAddress().;
			result = null;
		} else { 
			for(Integer e: addresstable.keySet()) {
				if(addresstable.get(e).equals(client)) {
					result = e;
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
		if(!addresstable.containsKey(address)) {
			return null;
		}
		return addresstable.get(address).toString();
	}
	
	public void setEntry(Integer address, String name) {
		if(addresstable.containsKey(address)) {
			addresstable.remove(address);
		}
		addresstable.put(address, name);
	}
}

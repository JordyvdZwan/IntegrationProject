package network;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.HashMap;


import application.Controller;


public class Router {
	
	private Controller controller;
	Map<Integer, String> addresstable = new HashMap<Integer, String>();
	private ForwardingTable table = new ForwardingTable();
	
	public Router(Controller controller) {
		this.controller = controller;
	}
	
	public void processUpdate(JRTVPacket packet) {
		
		//Puts true into the list with valid hops
		try {
			table.getvalidhops().put(InetAddress.getByName("" + packet.getSource()), true);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		//TODO: Split at destination, next hop and put these into the forwardingtables
		//This creates a new timeout for the specified next hop
		try {
			EntryTimeOut e = new EntryTimeOut(this, InetAddress.getByName("" + packet.getSource()));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		addresstable.put(packet.getSource(), packet.getMessage());
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
	
	public String getName(int address) {
		if(!addresstable.containsKey(address)) {
			return "Anonymous";//TODO
		}
		return addresstable.get(address);
	}
	
	private void setEntry(Integer address, String name) {
		if(addresstable.containsKey(address)) {
			addresstable.remove(address);
		}
		addresstable.put(address, name);
	}
	
	public Map<Integer, InetAddress> getTable() {
		return table.getTable();
	}
	
	//TO BE IMPLEMENTED
	
	public int getLocalIntAddress() {
		return 0;
	}
	
	public int getIntIP(String client) {
		return 0;
	}
	
	public InetAddress getRouteIP(String client) {
		return controller.getMulticastAddress();
	}
	
	public InetAddress getIP() {
		return null;
	}
	
	public ForwardingTable getForwardingTable() {
		return table;
	}

}

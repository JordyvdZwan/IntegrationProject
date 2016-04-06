package network;

import java.net.InetAddress;
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
	
	Map<InetAddress, EntryTimeOut> timeouts = new HashMap<InetAddress, EntryTimeOut>();
	
	public void processUpdate(JRTVPacket packet, InetAddress source) {
		if (source != controller.getLocalInetAddress()) {
			//Puts true into the list with valid hops
				table.getvalidhops().put(source, true);
			
			//TODO: Split at destination, next hop and put these into the forwardingtables
			byte[] bytes = packet.getMessage().getBytes();
			byte[] addresses = new byte[bytes.length - packet.getHashPayload()];
			System.arraycopy(bytes, packet.getHashPayload(), addresses, 0, bytes.length - packet.getHashPayload());
			Integer[] integers = new Integer[addresses.length / 4];
			for (int i = 0; i < integers.length; i++) {
				byte[] b = new byte[4];
				System.arraycopy(addresses, (i * 4), b, 0, 4);
				integers[i] = byteArrayToInt(b);
			}
			for (int i = 0; i < integers.length / 2; i++) {
				table.addHop(integers[i * 2], source, integers[(i * 2) + 1]);
			}
			
			
			
			//This creates a new timeout for the specified next hop
			if (!timeouts.containsKey(source)) {
				EntryTimeOut e = new EntryTimeOut(this, source);
				timeouts.put(source, e);
			}
			
			byte[] nameBytes = new byte[packet.getMessage().getBytes().length];
			System.arraycopy(packet.getMessage().getBytes(), 0, nameBytes, 0, packet.getHashPayload());
			String name = new String(nameBytes);
			if (!name.equals("Anonymous")) {
				addresstable.put(packet.getSource(), name);
				controller.addRecipientToView(name);
			}
		}
	}
	
	public void removeFromTimeout(InetAddress source) {
		timeouts.remove(source);
	}
	
	private static int byteArrayToInt(byte[] b) {
	    return   b[3] & 0xFF |
	            (b[2] & 0xFF) << 8 |
	            (b[1] & 0xFF) << 16 |
	            (b[0] & 0xFF) << 24;
	}
	
	public Integer getIP(String client) {
		Integer result = null;
		if (client.equals("Anonymous")) {
			result = Controller.multicastAddress;
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
	
	public int getNextHopCost(Integer destination) {
		return table.getNextHopCost(destination);
	}
	
	public String getName(int address) {
		if(!addresstable.containsKey(address)) {
			return "Anonymous";//TODO
		}
		return addresstable.get(address);
	}
	
	public Map<Integer,Map<InetAddress, Integer>> getTable() {
		return table.getTable();
	}
	
	public int getLocalIntAddress() {
		return controller.getLocalIAddress();
	}
	
	public int getIntIP(String client) {
		return getIP(client);
	}
	
		//TO BE IMPLEMENTED
	public InetAddress getRouteIP(int client) {
		return controller.getMulticastAddress();
	}
	
	public ForwardingTable getForwardingTable() {
		return table;
	}

}

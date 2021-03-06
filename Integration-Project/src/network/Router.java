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
	
	Map<Integer, EntryTimeOut> timeouts = new HashMap<Integer, EntryTimeOut>();
	
	public void processUpdate(JRTVPacket packet) {
		if (packet.getSource() != controller.getLocalIAddress() && packet.getSource() != 0) {
			//Puts true into the list with valid hops
			table.getvalidhops().put(packet.getSource(), true);
			
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
				if (integers[i * 2] != controller.getLocalIAddress()) {
					table.addHop(integers[i * 2], packet.getSource(), integers[(i * 2) + 1]);
				}
			}
			
			//This creates a new timeout for the specified next hop
			if (!timeouts.containsKey(packet.getSource())) {
				EntryTimeOut e = new EntryTimeOut(this, packet.getSource());
				timeouts.put(packet.getSource(), e);
			}
			
			byte[] nameBytes = new byte[packet.getHashPayload()];
			System.arraycopy(packet.getMessage().getBytes(), 0, nameBytes, 0, packet.getHashPayload());
			String name = new String(nameBytes);
			if (!name.equals("Anonymous")) {
				if (addresstable.containsKey(packet.getSource())) {
					addresstable.remove(packet.getSource());
					if (addresstable.get(packet.getSource())!= null && !addresstable.get(packet.getSource()).equals("(" + getStringIP(packet.getSource()) + ") " + name)) {
						controller.removeRecipientToView(getName(packet.getSource()));
					}
				}
				addresstable.put(packet.getSource(), "(" + getStringIP(packet.getSource()) + ") " + name);
				
				controller.addRecipientToView("(" + getStringIP(packet.getSource()) + ") " + name);
				//TODO name changing
			}
		}
	}
	
	public void removeFromTimeout(Integer source) {
		timeouts.remove(source);
		controller.removeRecipientToView(getName(source));
		addresstable.remove(source);
	}
	
	private static int byteArrayToInt(byte[] b) {
	    return   b[3] & 0xFF |
	            (b[2] & 0xFF) << 8 |
	            (b[1] & 0xFF) << 16 |
	            (b[0] & 0xFF) << 24;
	}
	
	public static String getStringIP(int address) {
		try {
			return InetAddress.getByAddress(unpack(address)).getHostAddress().toString();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			return null;
		}
	}
	
	
	static byte[] unpack(int bytes) {
		return new byte[] {
			(byte)((bytes >>> 24) & 0xff),
			(byte)((bytes >>> 16) & 0xff),
			(byte)((bytes >>>  8) & 0xff),
			(byte)((bytes       ) & 0xff)
		};
	}
	
	//CHECK WHAT IS BELOW HERE!
	
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
	
	public int getNextHop(Integer destination) {
		return table.getNextHop(destination);
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
	
	public Map<Integer,Map<Integer, Integer>> getTable() {
		return table.getTable();
	}
	
	public int getLocalIntAddress() {
		return controller.getLocalIAddress();
	}
	
	public int getIntIP(String client) {
		return getIP(client);
	}
	
		//TO BE IMPLEMENTED
	public Integer getRouteIP(int destination) {
		Integer result = null;
		if(destination == Controller.multicastAddress) {
			result = Controller.multicastAddress;
		} else {
			result =  table.getNextHop(destination);
		}
		return result;
	}
	
	public ForwardingTable getForwardingTable() {
		return table;
	}

}

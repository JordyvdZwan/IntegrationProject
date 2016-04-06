package network;
import java.util.Map;
import java.util.HashMap;
import java.net.InetAddress;

public class ForwardingTable {
	
	//Map<Destination, Next hop>
	Map<Integer, InetAddress> forwardingtable = new HashMap<Integer, InetAddress>();
	
	public Map<Integer, InetAddress> getTable() {
		return forwardingtable;
	}
	
	public InetAddress getNextHop(Integer destination) {
		return forwardingtable.get(destination); 
	}
	
	public void  addHop(Integer destination, InetAddress nexthop) {
		forwardingtable.put(destination, nexthop);
	}
	
	public void removeRoute(Integer destination) {
		forwardingtable.remove(destination);
	}
}

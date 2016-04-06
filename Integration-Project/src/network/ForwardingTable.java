package network;
import java.util.Map;
import java.util.HashMap;
import java.net.InetAddress;

public class ForwardingTable {
	
	//Map<Destination, Next hop>
	Map<InetAddress, InetAddress> forwardingtable = new HashMap<InetAddress, InetAddress>();
	
	public Map<InetAddress, InetAddress> getTable() {
		return forwardingtable;
	}
	
	public InetAddress getNextHop(InetAddress destination) {
		return forwardingtable.get(destination); 
	}
	
	public void  addHop(InetAddress destination, InetAddress nexthop) {
		forwardingtable.put(destination, nexthop);
	}
	
	public void removeRoute(InetAddress destination) {
		forwardingtable.remove(destination);
	}
}

package network;
import java.util.Map;
import java.util.HashMap;
import java.net.InetAddress;

public class ForwardingTable {
	
	//Map<Destination, Map<Nexthop, cost>>
	Map<Integer, Map<InetAddress, Integer>> forwardingtable = new HashMap<Integer, Map<InetAddress, Integer>>();
	Map<InetAddress, Boolean> validhops = new HashMap<InetAddress, Boolean>();
	
	public Map<Integer, Map<InetAddress, Integer>> getTable() {
		return forwardingtable;
	}
	
	public InetAddress getNextHop(Integer destination) {
		//Loops through all the paths to the destination and selects the one with the lowest cost
		InetAddress result = null;
		int resultcost = 0;
		Map<InetAddress, Integer> possibilities = forwardingtable.get(destination);
		
		for(InetAddress e: possibilities.keySet()) {
			if(possibilities.get(e) <= resultcost) {
				result = e;
				resultcost = possibilities.get(e);
			}
		}
		return result;
	}
	
	public void  addHop(Integer destination, InetAddress nexthop, int cost) {
		forwardingtable.get(destination).put(nexthop, cost);
	}
	
	public void removeNextHop(Integer destination, InetAddress nexthop) {
		forwardingtable.get(destination).remove(nexthop);
		if(forwardingtable.get(destination).size() == 0) {
			forwardingtable.remove(destination);
		}
	}
	
	public Map<InetAddress, Boolean> getvalidhops() {
		return validhops;
	}
}

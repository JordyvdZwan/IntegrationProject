package network;
import java.util.Map;

import application.Controller;

import java.util.HashMap;
import java.net.InetAddress;

public class ForwardingTable {
	
	//Map<Destination, Map<Nexthop, cost>>
	Map<Integer, Map<Integer, Integer>> forwardingtable = new HashMap<Integer, Map<Integer, Integer>>();
	Map<Integer, Boolean> validhops = new HashMap<Integer, Boolean>();
	
	public ForwardingTable() {
		forwardingtable.put(Controller.multicastAddress, new HashMap<Integer, Integer>());
		forwardingtable.get(Controller.multicastAddress).put(Controller.multicastAddress, 0);
	}
	
	
	public Map<Integer, Map<Integer, Integer>> getTable() {
		return forwardingtable;
	}
	
	public int getNextHop(Integer destination) {
		//Loops through all the paths to the destination and selects the one with the lowest cost
		Integer result = 0;
		int resultcost = 200;
		if (destination == Controller.multicastAddress) {
			return Controller.multicastAddress;
		} else {
			if(forwardingtable.containsKey(destination)) {
				Map<Integer, Integer> possibilities = forwardingtable.get(destination);
				
				for(Integer e: possibilities.keySet()) {
					if(possibilities.get(e) <= resultcost) {
						result = e;
						resultcost = possibilities.get(e);
					}
				}
			} 
			return result;
		}
	}
	
	public int getNextHopCost(Integer destination) {
		//Loops through all the paths to the destination and selects the one with the lowest cost
		Integer result = 0;
		int resultcost = 0;
		Map<Integer, Integer> possibilities = forwardingtable.get(destination);
		
		for(Integer e: possibilities.keySet()) {
			if(possibilities.get(e) <= resultcost) {
				result = e;
				resultcost = possibilities.get(e);
			}
		}
		return resultcost;
	}
	
	public void addHop(Integer destination, Integer nexthop, int cost) {
		if (forwardingtable.containsKey(destination)) {
			forwardingtable.get(destination).put(nexthop, cost);
		} else {
			forwardingtable.put(destination, new HashMap<Integer, Integer>());
			addHop(destination, nexthop, cost);
			for(Integer i: forwardingtable.keySet()) {
				System.out.println(Router.getStringIP(i));
				System.out.println(forwardingtable.get(i).toString());
			}
		}
	}
	
	public void removeNextHop(Integer destination, Integer nexthop) {
		forwardingtable.get(destination).remove(nexthop);
		if(forwardingtable.get(destination).size() == 0) {
			forwardingtable.remove(destination);
		}
	}
	
	public Map<Integer, Boolean> getvalidhops() {
		return validhops;
	}
}

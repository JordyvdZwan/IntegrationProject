package network;
import java.util.Map;

import application.Controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ForwardingTable {
	
	/**
	 * Map<Destination, Map<Nexthop, cost>>.
	 * stores all the destinations with the possible next hops
	 */
	Map<Integer, Map<Integer, Integer>> forwardingtable 
									= new HashMap<Integer, Map<Integer, Integer>>();
	
	/**
	 * Stores all the nexthops that are a valid option (i.e. sent an update recently).
	 */
	Map<Integer, Boolean> validhops = new HashMap<Integer, Boolean>();
	Router router;
	
	
	/**
	 * Creates a new instance of forwardingtable.
	 * @param router the router class to which this forwardingtable is connected
	 */
	public ForwardingTable(Router router) {
		this.router = router;
	}
	
	/**
	 * gives the forwardingtable.
	 * @return the forwardingtable
	 */
	public synchronized Map<Integer, Map<Integer, Integer>> getTable() {
		return forwardingtable;
	}
	
	/**
	 * Loops through all the possible nexthops to a destination
	 * and determines the one with the least hops.
	 * 
	 * @param destination the destination to which should be routed
	 * @return result the next hop with the least cost, to which the packet should be routed
	 */
	public synchronized int getNextHop(Integer destination) {
		//Loops through all the paths to the destination and selects the one with the lowest cost
		Integer result = 1;
		int resultcost = 200;
		if (destination == Controller.multicastAddress) {
			return Controller.multicastAddress;
		} else {
			if (forwardingtable.containsKey(destination)) {
				Map<Integer, Integer> possibilities = forwardingtable.get(destination);
				for (Integer e: possibilities.keySet()) {
					if (possibilities.get(e) <= resultcost) {
						result = e;
						resultcost = possibilities.get(e);
					}
				}
			} 
			return result;
		}
	}
	
	/**
	 * Returns a list of destinations given a next hop.
	 * @param nexthop ip of the next hop.
	 * @return list of destinations this hop can reach.
	 */
	public synchronized List<Integer> getPossibilities(Integer nexthop) {
		List<Integer> result = new ArrayList<Integer>();
		for (Integer i: forwardingtable.keySet()) {
			if (forwardingtable.get(i).containsKey(nexthop)) {
				result.add(i);
			}
		}
		
		return result;
	}
	
	/**
	 * Returns the lowest hop cost to a certain destination.
	 * @param destination destination ip you want to reach.
	 * @return the lowest hop cost to the destination.
	 */
	public synchronized int getNextHopCost(Integer destination) {
		//Loops through all the paths to the destination and selects the one with the lowest cost
		int resultcost = 100;
		Map<Integer, Integer> possibilities = forwardingtable.get(destination);
		
		for (Integer e: possibilities.keySet()) {
			if (possibilities.get(e) <= resultcost) {
				resultcost = possibilities.get(e);
			}
		}
		return resultcost;
	}
	
	/**
	 * Puts a possible next hop into the forwardingtable at the right place.
	 * 
	 * @param destination the destination to which a packet can be sent
	 * @param nexthop a possible next hop to this destination
	 * @param cost the cost of the route via next hop to the destination 
	 */
	public synchronized void addHop(Integer destination, Integer nexthop, int cost) {
		if (forwardingtable.containsKey(destination)) {
			forwardingtable.get(destination).put(nexthop, cost);
		} else {
			forwardingtable.put(destination, new HashMap<Integer, Integer>());
			addHop(destination, nexthop, cost);
		}
	}
	
	/**
	 * deletes a next hop to a destination from the forwardingtable.
	 * 
	 * @param destination the destination from which the next hop should be removed
	 * @param nexthop the next hop that should be deleted
	 */
	public synchronized void removeNextHop(Integer destination, Integer nexthop) {
		if (forwardingtable.containsKey(destination)) {
			forwardingtable.get(destination).remove(nexthop);
			if (forwardingtable.get(destination).size() == 0) {
				forwardingtable.remove(destination);
			}
		}
	}
	
	/**
	 * gives all the next hops that are still valid (i.e. have recently sent an update)
	 * @return validshops the list of hops that are valid
	 */
	public synchronized Map<Integer, Boolean> getvalidhops() {
		return validhops;
	}
}

package network;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.net.InetAddress;

public class EntryTimeOut extends Thread {
	
	Router router;
	Integer nexthop;
	boolean  receivednewroute = false;
	
	/**
	 * Creates a new instance of the EntryTimeOut class
	 * 
	 * @param router the router to which this timeout is assigned
	 * @param nexthop the nexthop for which this timeOut should be set
	 */
	public EntryTimeOut(Router router, Integer nexthop) {
		this.router = router;
		this.nexthop = nexthop;
	}
	
	/**
	 * First checks if the specified hop is still a valid option, sets it to false, and waits for 3 seconds
	 * If the hop is still false, all it's entries will be deleted from the forwardingtable.
	 */
	public void run() {
		//Checks if the specified hop is still a valid hop, puts false, waits 3 seconds, if still false:
		// remove all entries in the forwardingtable that contain this next hop as next hop. 
		while (router.getForwardingTable().getvalidhops().get(nexthop)) {
			router.getForwardingTable().getvalidhops().put(nexthop, false);
			try {
				TimeUnit.SECONDS.sleep(10); //TODO change to constant
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		Set<Integer> test = new HashSet<Integer>();
		test.addAll(router.getForwardingTable().getTable().keySet());
		for(Integer i: test) {
			if(router.getForwardingTable().getTable().get(i).keySet().contains(nexthop)) {
				router.getForwardingTable().getTable().get(i).remove(nexthop);
				if (router.getForwardingTable().getTable().get(i).keySet().isEmpty()) {
					router.getForwardingTable().getTable().remove(i);
				}
			}
		}
		router.removeFromTimeout(nexthop);
		this.interrupt();
	}
}

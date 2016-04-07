package network;

import java.util.concurrent.TimeUnit;
import java.net.InetAddress;

public class EntryTimeOut extends Thread {
	
	Router router;
	InetAddress nexthop;
	boolean  receivednewroute = false;
	
	public EntryTimeOut(Router router, InetAddress nexthop) {
		this.router = router;
		this.nexthop = nexthop;
		this.start();
	}
	
	public void run() {
		
		//Checks if the specified hop is still a valid hop, puts false, waits 3 seconds, if still false:
		// remove all entries in the forwardingtable that contain this next hop as next hop. 
		while (router.getForwardingTable().getvalidhops().get(nexthop)) {
			router.getForwardingTable().getvalidhops().put(nexthop, false);
			try {
				TimeUnit.SECONDS.sleep(3);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
		for(Integer i: router.getForwardingTable().getTable().keySet()) {
			if(router.getForwardingTable().getTable().get(i).keySet().contains(nexthop)) {
				router.getForwardingTable().getTable().get(i).remove(nexthop);
			}
		}
		this.interrupt();
	}
}

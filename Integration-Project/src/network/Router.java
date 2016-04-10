package network;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.Key;
import java.util.Map;
import java.util.Arrays;
import java.util.HashMap;


import application.Controller;
import security.CreateEncryptedSessionPacket;
import security.RSA;


public class Router {
	
	/**
	 * main controller needed for basic info and transmitting data.
	 */
	private Controller controller;

	/**
	 * Class constructor which will store the given controller for acquiring data and transmitting data.
	 * @param controller main controller which will be stored.
	 */
	public Router(Controller controller) {
		this.controller = controller;
	}
	
	//======================================================================================================================
	//||                                         Encryption methods:                                                      ||  
	//======================================================================================================================
	
	/**
	 * Map which maps addresses of clients to their corresponding encryption classes.
	 */
	private Map<Integer, CreateEncryptedSessionPacket> encryption = new HashMap<Integer, CreateEncryptedSessionPacket>();
	
	/**
	 * Map which maps addresses to a boolean value which tells if a diffie packet has been send to a person.
	 */
	private Map<Integer, Boolean> diffiePacketOutstanding = new HashMap<Integer, Boolean>();
	
	/**
	 * Map which maps addresses to the send random number in the diffie packet.
	 */
	private Map<Integer, Integer> sendDiffiePacketInt = new HashMap<Integer, Integer>();
	
	/**
	 * This method creates and sends a diffie packet with the generated values from the CreateEncryptedSessionPacket class for setting up a secured connection.
	 * It will send out the packet and add the encryption to the encryption map.
	 * It also sets the diffiePacketOutstanding to true and the sendDiffiePacketint to the correct value.
	 * @param destination Address of the client with whom to set up a secured connection.
	 */
	public void setupDiffie(int destination) {
		if (!diffiePacketOutstanding.containsKey(destination) || !diffiePacketOutstanding.get(destination)) {
			//Creating encryption class and setting values that need to be added to the packet.
			CreateEncryptedSessionPacket encryption = new CreateEncryptedSessionPacket();
			BigInteger[] keys = encryption.keyDiffieHellmanFirst();
			int length1 = keys[0].toByteArray().length;
			int length2 = keys[1].toByteArray().length;
			int length3 = keys[2].toByteArray().length;
			int random = (int) (Math.random() * Integer.MAX_VALUE);
			int totalLength = length1 + length2 + length3;
			
			//Creating the byte array and putting in all data like lengths and keys.
			byte[] bytes = new byte[4 + totalLength];
			
			byte[] randomBytes = unpack(random);
			bytes[0] = randomBytes[0];
			bytes[1] = randomBytes[1];
			bytes[2] = randomBytes[2];
			bytes[3] = randomBytes[3];
			
			byte[] length1Bytes = unpack(random);
			bytes[4] = length1Bytes[0];
			bytes[5] = length1Bytes[1];
			bytes[6] = length1Bytes[2];
			bytes[7] = length1Bytes[3];
			
			byte[] length2Bytes = unpack(random);
			bytes[8] = length2Bytes[0];
			bytes[9] = length2Bytes[1];
			bytes[10] = length2Bytes[2];
			bytes[11] = length2Bytes[3];
			
			byte[] length3Bytes = unpack(random);
			bytes[12] = length3Bytes[0];
			bytes[13] = length3Bytes[1];
			bytes[14] = length3Bytes[2];
			bytes[15] = length3Bytes[3];
			
			System.arraycopy(keys[0], 0, bytes, 16, length1);
			System.arraycopy(keys[1], 0, bytes, 16 + length1, length2);
			System.arraycopy(keys[2], 0, bytes, 16 + length1 + length2, length3);
			
			//Putting data in new packet and sending it.
			String message = new String(bytes);
			JRTVPacket packet = new JRTVPacket(message);
			packet.setDiffie(true);
			controller.sendPacket(destination, packet);
		}
	}
	
	/**
	 * This method will respond to a diffie packet with a diffie/ack packet and store away the encryption for the source of the diffie packet.
	 * It will only do this when the random integer in the diffiepacket is bigger than the one it has send itself.
	 * @param packet Received packet from a client, which wants to set up a secured connection.
	 */
	public void processDiffie(JRTVPacket packet) {
		if (packet.isAck()) {
			//Finish encryption setup.
			BigInteger i = new BigInteger(packet.getMessage().getBytes());
			encryption.get(packet.getSource()).keyDiffieHellmanFinal(i);
		} else {
			//Send diffie/ack message to the source of the diffie message if the random value is larger than the one send (if send).
			byte[] bytes = packet.getMessage().getBytes();
			int random = byteArrayToInt(Arrays.copyOfRange(bytes, 0, 4));
			if (!diffiePacketOutstanding.get(packet.getSource()) ) { //TODO fix possible bug??
				//read data and put into variables.
				int length1 = byteArrayToInt(Arrays.copyOfRange(bytes, 4, 8));
				int length2 = byteArrayToInt(Arrays.copyOfRange(bytes, 8, 12));
				int length3 = byteArrayToInt(Arrays.copyOfRange(bytes, 12, 16));
				
				BigInteger[] numbers = new BigInteger[3];
				
				numbers[0] = new BigInteger(Arrays.copyOfRange(bytes, 16, length1));
				numbers[1] = new BigInteger(Arrays.copyOfRange(bytes, 16 + length1, length2));
				numbers[2] = new BigInteger(Arrays.copyOfRange(bytes, 16 + length1 + length2, length3));
				
				//Create encryption
				CreateEncryptedSessionPacket encryption = new CreateEncryptedSessionPacket();
				BigInteger reply = encryption.keyDiffieHellmanSecond(numbers);
				this.encryption.put(packet.getSource(), encryption);
				
				JRTVPacket p = new JRTVPacket(new String(reply.toByteArray()));
				p.setAck(true);
				p.setDiffie(true);
				controller.sendPacket(packet.getSource(), p);
			} else if (diffiePacketOutstanding.get(packet.getSource()) && sendDiffiePacketInt.get(packet.getSource()) < random) {
				//read data and put into variables.
				int length1 = byteArrayToInt(Arrays.copyOfRange(bytes, 4, 8));
				int length2 = byteArrayToInt(Arrays.copyOfRange(bytes, 8, 12));
				int length3 = byteArrayToInt(Arrays.copyOfRange(bytes, 12, 16));
				
				BigInteger[] numbers = new BigInteger[3];
				
				numbers[0] = new BigInteger(Arrays.copyOfRange(bytes, 16, length1));
				numbers[1] = new BigInteger(Arrays.copyOfRange(bytes, 16 + length1, length2));
				numbers[2] = new BigInteger(Arrays.copyOfRange(bytes, 16 + length1 + length2, length3));
				
				//Create encryption
				BigInteger reply = encryption.get(packet.getSource()).keyDiffieHellmanSecond(numbers);
				
				JRTVPacket p = new JRTVPacket(new String(reply.toByteArray()));
				p.setAck(true);
				p.setDiffie(true);
				controller.sendPacket(packet.getSource(), p);
			}
		}
	}
	
	public CreateEncryptedSessionPacket getEncryption(int destination) {
		if (hasEncryptionKey(destination)) {
			return encryption.get(destination);
		} else {
			return null;
		}
	}
	
	public boolean hasEncryptionKey(int destination) {
		return encryption.containsKey(destination) && encryption.get(destination).hasKey();
	}
	
	public boolean isSettingUpDiffie(int destination) {
		if (diffiePacketOutstanding.containsKey(destination)) {
			return diffiePacketOutstanding.get(destination);
		} else {
			return false;
		}
	}
	
	
	//======================================================================================================================
	//||                                           Routing methods:                                                       ||  
	//======================================================================================================================
		
	
	private Map<Integer, String> addresstable = new HashMap<Integer, String>();
	private ForwardingTable table = new ForwardingTable(this);
	private Map<Integer, EntryTimeOut> timeouts = new HashMap<Integer, EntryTimeOut>();
	
	public void processUpdate(JRTVPacket packet) {
		if (!controller.getSettingUp()) {
	//		if (packet.getSource() != controller.getLocalIAddress() && packet.getSource() != 0) {
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
					e.setDaemon(true);
					timeouts.put(packet.getSource(), e);
					timeouts.get(packet.getSource()).start();
				}
	
				byte[] nameBytes = new byte[packet.getHashPayload()];
				System.arraycopy(packet.getMessage().getBytes(), 0, nameBytes, 0, packet.getHashPayload());
				String name = new String(nameBytes);
				if (!name.equals("Anonymous")) {
					if (addresstable.containsKey(packet.getSource())) {
						if (addresstable.get(packet.getSource())!= null && !addresstable.get(packet.getSource()).equals("(" + getStringIP(packet.getSource()) + ") " + name)) {
							controller.removeRecipientToView(getName(packet.getSource()));
							System.out.println("Is it gone?");
							addresstable.remove(packet.getSource());
						}
					}
					addresstable.put(packet.getSource(), "(" + getStringIP(packet.getSource()) + ") " + name);
					controller.addRecipientToView("(" + getStringIP(packet.getSource()) + ") " + name);
					//TODO name changing
				}
	//		}
		}
	}
	
	public void removeFromTimeout(Integer source) {
		timeouts.remove(source);
		controller.removeRecipientToView(getName(source));
		encryption.remove(source);
		diffiePacketOutstanding.remove(source);
		sendDiffiePacketInt.remove(source);
		addresstable.remove(source);
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
	
	public int getIntIP(String client) {
		return getIP(client);
	}
	
	//======================================================================================================================
	//||                                              Util methods:                                                       ||  
	//======================================================================================================================

	private static int byteArrayToInt(byte[] b) {
	    return   b[3] & 0xFF |
	            (b[2] & 0xFF) << 8 |
	            (b[1] & 0xFF) << 16 |
	            (b[0] & 0xFF) << 24;
	}
	
	static byte[] unpack(int bytes) {
		return new byte[] {
			(byte)((bytes >>> 24) & 0xff),
			(byte)((bytes >>> 16) & 0xff),
			(byte)((bytes >>>  8) & 0xff),
			(byte)((bytes       ) & 0xff)
		};
	}
	
	public static String getStringIP(int address) {
		try {
			return InetAddress.getByAddress(unpack(address)).getHostAddress().toString();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			return null;
		}
	}
	
	//======================================================================================================================
	//||                                           get/set methods:                                                       ||  
	//======================================================================================================================
	
	public ForwardingTable getForwardingTable() {
		return table;
	}
	
	public Map<Integer,Map<Integer, Integer>> getTable() {
		return table.getTable();
	}

	public int getLocalIntAddress() {
		return controller.getLocalIAddress();
	}
}

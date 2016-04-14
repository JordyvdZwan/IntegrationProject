package network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.InetAddress;
import java.util.List;

import application.Controller;

import java.util.ArrayList;

public class Connection extends Thread {
	
	MulticastSocket socket;
	InetAddress paddress;
	List<DatagramPacket> queuedpackets = new ArrayList<DatagramPacket>();
	Update update;
	Controller controller;
	
	public Connection(int portnumber, String mcaddress, Controller controller) {
		this.controller = controller;
		try {
			InetAddress address = InetAddress.getByName(mcaddress);
			socket = new MulticastSocket(portnumber);
			socket.joinGroup(address);
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.setDaemon(true);
		this.start();
	}
	
	//Tries to receive a new datagram, if it gets one, put it in the queue.
	public void run() {
		while (true) {
			byte[] buffer = new byte[1000];
			DatagramPacket recv = new DatagramPacket(buffer, buffer.length);
			
			try {
				socket.receive(recv);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			queuedpackets.add(recv);
			recv.setLength(buffer.length);
		}

	}
	
	//Sends a datagrampacket coming from the client
	public void send(DatagramPacket packet) {
		try {
			socket.send(packet);
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
	}
	
	public DatagramPacket getFirstInQueue() {
		DatagramPacket selectedpacket = null;
		if (!queuedpackets.isEmpty()) {
			selectedpacket = queuedpackets.get(0);
			queuedpackets.remove(0);
		}
		return selectedpacket;
	}
}

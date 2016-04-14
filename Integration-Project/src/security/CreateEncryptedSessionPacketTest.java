package security;

import network.JRTVPacket;

public class CreateEncryptedSessionPacketTest {
	public static void main(String[] args) {
		CreateEncryptedSessionPacket a = new CreateEncryptedSessionPacket();
		CreateEncryptedSessionPacket b = new CreateEncryptedSessionPacket();
		
		a.keyDiffieHellmanFinal(b.keyDiffieHellmanSecond(a.keyDiffieHellmanFirst()));
		
		JRTVPacket packet = new JRTVPacket("Hey There!");
		System.out.println("input: " + packet.getMessage());
		
		JRTVPacket p = b.decrypt(a.encrypt(packet, 
						RSA.getPrivateKey(-1062730494)), RSA.getPublicKey(-1062730494));
		
		System.out.println("result: " + p.getMessage());
	}
}

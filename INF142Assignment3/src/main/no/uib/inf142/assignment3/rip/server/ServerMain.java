package no.uib.inf142.assignment3.rip.server;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.util.List;

import no.uib.inf142.assignment3.rip.client.RIPPacketGenerator;
import no.uib.inf142.assignment3.rip.common.PacketUtils;
import no.uib.inf142.assignment3.rip.exception.TooShortPacketLengthException;

public class ServerMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		InetSocketAddress address;
//		try {
//			address = new InetSocketAddress("localhost", 12334);
//			RIPPacketGenerator packetGen = new RIPPacketGenerator(address,
//					address);
//
//			String string = "abcdefghijklmnopqrstuvwxyz";
//			List<DatagramPacket> list = packetGen.makePackets(string);
//
//			for (DatagramPacket p : list) {
//				String data = new String(p.getData(), 0, p.getLength());
//				System.out.println(data);
//			}
//		} catch (SocketException | TooShortPacketLengthException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		System.out.println(PacketUtils.calculateMD5("teststring"));
		System.out.println(PacketUtils.calculateMD5("asdf"));
		System.out.println(PacketUtils.calculateMD5("teststring"));

		// try {
		// RIPServerSocket server = new RIPServerSocket(54322);
		// String data = server.receive();
		// server.close();
		//
		// System.out.println("main received: " + data);
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
	}

}

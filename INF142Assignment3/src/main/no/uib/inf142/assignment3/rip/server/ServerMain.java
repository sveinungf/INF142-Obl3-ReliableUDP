package no.uib.inf142.assignment3.rip.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;

import no.uib.inf142.assignment3.rip.Signal;
import no.uib.inf142.assignment3.rip.common.RIPPacket;
import no.uib.inf142.assignment3.rip.exception.TooShortPacketLengthException;

public class ServerMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		InetSocketAddress address;
		try {
			address = new InetSocketAddress("localhost", 12334);
			RIPPacket rippacket = new RIPPacket(address, address,
					Signal.REGULAR, 0, 0, "abcdefghijklmnopqrstuvwxyz");

			List<DatagramPacket> list = rippacket.makeDatagramPackets();
			
			for (DatagramPacket p : list) {
				String data = new String(p.getData(), 0, p.getLength());
				System.out.println(data);
			}
		} catch (SocketException | TooShortPacketLengthException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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

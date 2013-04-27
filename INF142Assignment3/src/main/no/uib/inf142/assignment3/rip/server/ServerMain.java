package no.uib.inf142.assignment3.rip.server;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.List;

import no.uib.inf142.assignment3.rip.client.RIPPacketGenerator;
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
			RIPPacketGenerator packetGen = new RIPPacketGenerator(address,
					address, 0);

			String string = "abcdefghijklmnopqrstuvwxyz";
			List<RIPPacket> list = packetGen.makePackets(string);

			for (RIPPacket rip : list) {
				DatagramPacket p = rip.getDatagramPacket();
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

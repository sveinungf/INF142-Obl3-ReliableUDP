package no.uib.inf142.assignment3.rip;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Test {

	public static void main(String[] args) {
		try {
			InetAddress a = InetAddress.getByName("localhost");
			InetSocketAddress s = new InetSocketAddress(a, 1234);
			DatagramPacket p = RIPPacket.makePacket(s, s, 0, 0, Signal.ACK,
					"data");
			System.out.println(new String(p.getData()));
		} catch (UnknownHostException | SocketException e) {
			System.out.println(e.getMessage());
		}

		String s = RIPPacket.buildDelimitedString("hei", "eg", "heiter", "ole");
		System.out.println(s);
	}
}

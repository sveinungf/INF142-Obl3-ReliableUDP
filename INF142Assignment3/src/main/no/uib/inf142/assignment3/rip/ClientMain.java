package no.uib.inf142.assignment3.rip;

import java.io.IOException;
import java.net.InetSocketAddress;

public class ClientMain {

	public static void main(final String[] args) {
		try {
			InetSocketAddress destination = new InetSocketAddress("localhost",
					ProtocolConstants.SERVERPORT);
			InetSocketAddress relay = new InetSocketAddress("localhost",
					ProtocolConstants.RELAYPORT);
			RIPSocket client = new RIPSocket(destination, relay);
			client.send("hei");
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}

		System.out.println("client done");
	}
}

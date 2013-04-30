package no.uib.inf142.assignment3.rip.server;

import java.net.SocketException;

import no.uib.inf142.assignment3.rip.common.Protocol;

public class ServerMain {

	public static void main(String[] args) {
		try {
			RIPServerSocket ripserver = new RIPServerSocket(
					Protocol.SERVER_LISTENING_PORT,
					Protocol.RELAY_LISTENING_PORT);

			while (true) {
				System.out.println("RIPServerSocket got: "
						+ ripserver.receive());
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
}

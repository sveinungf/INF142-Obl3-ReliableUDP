package no.uib.inf142.assignment3.rip.server;

import java.net.SocketException;

import no.uib.inf142.assignment3.rip.common.Protocol;

public class ServerMain {

	public static void main(final String[] args) {
	    boolean alive = true;
		try {
			RIPServerSocket ripserver = new RIPServerSocket(
					Protocol.SERVER_LISTENING_PORT,
					Protocol.RELAY_LISTENING_PORT);

			while (alive) {
				System.out.println("RIPServerSocket got: "
						+ ripserver.receive());
			}
			
			ripserver.close();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
}

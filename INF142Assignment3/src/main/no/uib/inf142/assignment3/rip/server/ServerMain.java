package no.uib.inf142.assignment3.rip.server;

import java.io.IOException;

import no.uib.inf142.assignment3.rip.common.Datafield;
import no.uib.inf142.assignment3.rip.common.Protocol;
import no.uib.inf142.assignment3.rip.common.PacketGenerator;

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
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

package no.uib.inf142.assignment3.rip.server;

import java.io.IOException;

import no.uib.inf142.assignment3.rip.common.Datafield;
import no.uib.inf142.assignment3.rip.common.Protocol;

public class ServerMain {

	public static void main(String[] args) {
		try {
			RIPServerSocket ripserver = new RIPServerSocket(
					Protocol.SERVER_LISTENING_PORT);
			String message = ripserver.receive();

			System.out.println("RIPServerSocket got: " + message);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

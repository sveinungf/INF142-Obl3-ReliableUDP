package no.uib.inf142.assignment3.rip.server;

import java.net.SocketException;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			RIPServerSocket server = new RIPServerSocket(54322);
			server.receive();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

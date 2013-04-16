package no.uib.inf142.assignment3.rip.server;

import java.net.SocketException;

public class ServerMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			RIPServerSocket server = new RIPServerSocket(54322);
			String data = server.receive();
			
			System.out.println("main received: " + data);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

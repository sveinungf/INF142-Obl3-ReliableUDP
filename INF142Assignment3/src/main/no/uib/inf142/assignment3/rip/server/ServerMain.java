package no.uib.inf142.assignment3.rip.server;

import java.io.IOException;
import java.net.SocketException;

public class ServerMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			RIPServerSocket server = new RIPServerSocket(54322);
			String data = server.receive();
			server.close();
			
			System.out.println("main received: " + data);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

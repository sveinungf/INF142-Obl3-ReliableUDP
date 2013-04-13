package no.uib.inf142.assignment3.rip;

import java.io.IOException;

public class ServerMain {

	public static void main(final String[] args) {
		try {
			RIPServerSocket server = new RIPServerSocket();
			String s = server.receive();
			System.out.println(s);
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}

		System.out.println("server done");
	}
}

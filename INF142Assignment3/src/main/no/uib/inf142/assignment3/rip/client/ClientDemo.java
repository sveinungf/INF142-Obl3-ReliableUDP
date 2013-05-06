package no.uib.inf142.assignment3.rip.client;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Scanner;

import no.uib.inf142.assignment3.rip.common.Protocol;

public class ClientDemo {
	// TODO increase packet length
	public static void main(final String[] args) {
		try {
			Scanner in = new Scanner(System.in, Protocol.CHARSET.name());

			InetAddress localhost = InetAddress.getLocalHost();
			InetSocketAddress server = new InetSocketAddress(localhost,
					Protocol.SERVER_LISTENING_PORT);
			InetSocketAddress relay = new InetSocketAddress(localhost,
					Protocol.RELAY_LISTENING_PORT);

			RIPSocket ripSocket = new RIPSocket(server, relay);

			String input = "";
			System.out.println("Enter strings to send to server, "
					+ "\"exit\" to stop.\n");

			while (!"exit".equals(input)) {
				input = in.nextLine();

				if (!"exit".equals(input)) {
					ripSocket.send(input);
				}
			}

			in.close();
			ripSocket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

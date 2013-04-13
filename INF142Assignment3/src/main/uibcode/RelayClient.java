package uibcode;

import java.io.*;
import java.net.*;

class RelayClient {

	static void send1() throws Exception {

		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(
				System.in));

		DatagramSocket clientSocket = new DatagramSocket();

		InetAddress relayAddress = InetAddress.getByName("localhost");
		InetAddress destAddress = InetAddress.getByName("localhost");
		int relayport = 11111;
		int destport = 9876;

		String sentence = inFromUser.readLine();
		DatagramPacket sendPacket = RelayPakke.storSendPakke(sentence,
				destAddress, destport, relayAddress, relayport, 0, 0);

		clientSocket.send(sendPacket);
		System.out.println("CLIENT: Sent: " + sentence + " to " + destAddress
				+ " at port " + destport + "through port:");

		clientSocket.close();
	}

	static void sendmany(int n) throws Exception {
		DatagramSocket clientSocket = new DatagramSocket();
		InetAddress relayAddress = InetAddress.getByName("localhost");
		InetAddress destAddress = InetAddress.getByName("localhost");
		int relayport = 11111;
		int destport = 9876;

		for (int i = 0; i < n; i++) {
			String sentence = "streng " + i;
			DatagramPacket sendPacket = RelayPakke.storSendPakke(sentence,
					destAddress, destport, relayAddress, relayport, i, 0);
			clientSocket.send(sendPacket);
			System.out.println("CLIENT: Sent: " + sentence + " to "
					+ destAddress + " at port " + destport + "through port:");
			try {
				clientSocket.wait(1000000);
			} catch (Exception e) {
			}
		}
		
		clientSocket.close();
	}

	public static void main(String args[]) throws Exception {
		sendmany(1000);
	}
}

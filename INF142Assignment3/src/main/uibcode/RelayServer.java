package uibcode;

import java.net.*;

class RelayServer {
	public static void main(String args[]) throws Exception {

		DatagramSocket serverSocket = new DatagramSocket(9876);

		byte[] receiveData = new byte[1024];

		while (true) {
			DatagramPacket receivePacket = new DatagramPacket(receiveData,
					receiveData.length);
			serverSocket.receive(receivePacket);
			String sentence = new String(receivePacket.getData(), 0,
					receivePacket.getLength());
			InetAddress relayAddress = receivePacket.getAddress();
			int relayport = receivePacket.getPort();
			System.out.println("Server: Received: " + sentence + " from "
					+ relayAddress + " at port " + relayport);
		}
	}
}

package uibcode;

import java.net.*;


public class Relay {

	@SuppressWarnings("resource")
	public static void main(String args[]) throws Exception {
		Buffer queue;
		queue = new Buffer(300, 10);
		queue.start();
		DatagramSocket serverSocket = new DatagramSocket(11111);
		byte[] receiveData = new byte[1024];

		while (true) {
			DatagramPacket receivePacket = new DatagramPacket(receiveData,
					receiveData.length);
			serverSocket.receive(receivePacket);
			DatagramPacket sendPacket = RelayPakke.reformat(receivePacket);
			queue.insert(sendPacket);
			try {
				queue.wait(25);
			} catch (Exception e) {
			}
		}
	}
}

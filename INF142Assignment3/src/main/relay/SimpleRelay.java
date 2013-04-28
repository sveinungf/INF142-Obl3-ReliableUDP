package relay;

import java.net.*;


public class SimpleRelay {

	@SuppressWarnings("resource")
	public static void main(String args[]) throws Exception {

		DatagramSocket serverSocket = new DatagramSocket(11111);

		byte[] receiveData = new byte[1024];

		while (true) {
			DatagramPacket receivePacket = new DatagramPacket(receiveData,
					receiveData.length);
			serverSocket.receive(receivePacket);
			String sentence = new String(receivePacket.getData(), 0, receivePacket.getLength());
			InetAddress sourceAddress = receivePacket.getAddress();
			int sourceport = receivePacket.getPort();

			System.out.println("RELAY: Received: " + sentence + " from "
					+ sourceAddress + " at port " + sourceport);

			int semicol = sentence.indexOf(";");
			String substring = sentence.substring(0, semicol);
			if (substring.indexOf("/") >= 0)
				substring = "localhost";

			InetAddress destAddress = InetAddress.getByName(substring);
			sentence = sentence.substring(semicol + 1);
			semicol = sentence.indexOf(";");
			substring = sentence.substring(0, semicol);

			int destport = Integer.parseInt(substring);
			sentence = sentence.substring(semicol + 1);
			DatagramPacket sendPacket = RelayPakke.storSendPakke(sentence,
					sourceAddress, sourceport, destAddress, destport, 0, 0);

			serverSocket.send(sendPacket);

			System.out.println("RELAY: Sent: " + sentence + " to "
					+ destAddress + " at port " + destport);
		}
	}
}

package relay;

import java.net.*;

public class RelayPakke {

	public static DatagramPacket storSendPakke(String sendData,
			InetAddress destAddress, int destPort, InetAddress relayAddress,
			int relayPort, int seq, int ack) {

		byte[] storData;
		String s = "" + destAddress + ";" + destPort + ";" + seq + ";" + ack
				+ ";" + sendData;
		storData = s.getBytes();

		return new DatagramPacket(storData, storData.length, relayAddress,
				relayPort);
	}

	public static DatagramPacket storSendPakke(String sendData,
			InetAddress destAddress, int destPort, InetAddress relayAddress,
			int relayPort) {

		byte[] storData;
		String s = "" + destAddress + ";" + destPort + ";" + sendData;
		storData = s.getBytes();

		return new DatagramPacket(storData, storData.length, relayAddress,
				relayPort);
	}

	public static DatagramPacket reformat(DatagramPacket receivePacket) {
		String sentence = new String(receivePacket.getData());
		InetAddress sourceAddress = receivePacket.getAddress();
		int sourceport = receivePacket.getPort();

		int semicol = sentence.indexOf(";");
		String substring = sentence.substring(0, semicol);
		int slashindex = substring.indexOf("/");
		if (substring.indexOf("/") >= 0)
			substring = substring.substring(slashindex + 1);

		InetAddress destAddress = null;
		try {
			destAddress = InetAddress.getByName(substring);
		} catch (UnknownHostException e) {
			System.out.println("Feil: <" + substring + ">");
		}

		sentence = sentence.substring(semicol + 1);
		semicol = sentence.indexOf(";");
		substring = sentence.substring(0, semicol);

		int destport = Integer.parseInt(substring);
		sentence = sentence.substring(semicol + 1);
		DatagramPacket sendPacket = RelayPakke.storSendPakke(sentence,
				sourceAddress, sourceport, destAddress, destport);

		return sendPacket;
	}

}

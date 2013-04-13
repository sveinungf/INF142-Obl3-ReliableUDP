package no.uib.inf142.assignment3.rip;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class RIPServerSocket {

	private DatagramSocket socket;

	public RIPServerSocket() throws IOException {
		socket = new DatagramSocket(ProtocolConstants.SERVERPORT);
	}

	public String receive() throws IOException {
		int packetLength = ProtocolConstants.PACKET_LENGTH;
		byte[] receiveData = new byte[packetLength];
		DatagramPacket packet = new DatagramPacket(receiveData,
				receiveData.length);
		socket.receive(packet);

		return new String(packet.getData(), 0, packet.getLength());
	}

	public void close() {
		socket.close();
	}
}


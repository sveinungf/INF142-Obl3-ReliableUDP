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

// - a constructor that produces a RIPServerSocket object, and which similarly
// to a TCP ServerSocket constructor assigns the RIPServerSocket to a port
// number.
//
// - a method String receive() that returns a String object received on the
// connection.
//
// - a method close() that closes the connection.
//
// - The RIPServerSocket should also notify the user of errors and other
// situations that can occur (e.g. lost connection, bad data, data out of order
// (really should not happen), the other party closed connection, or similar),
// for example by throwing an exception.
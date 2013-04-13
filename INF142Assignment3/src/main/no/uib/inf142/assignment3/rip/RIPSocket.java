package no.uib.inf142.assignment3.rip;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

public class RIPSocket {

	private DatagramSocket socket;
	private InetSocketAddress destination;
	private InetSocketAddress relay;
	private int sequence;

	public RIPSocket(InetSocketAddress destination, InetSocketAddress relay)
			throws SocketException {

		socket = new DatagramSocket(ProtocolConstants.CLIENTPORT);
		this.destination = destination;
		this.relay = relay;
		sequence = 0;
	}

	public void send(final String data) throws IOException {
		DatagramPacket packet = RIPPacket.makePacket(destination, relay,
				sequence, 0, Signal.NONE, data);

		socket.send(packet);
	}

	public void close() {
		socket.close();
	}
}

// - a constructor that produces a RIPsocket object, and which similarly to a
// TCP Socket constructor creates a RIPsocket object already connected to a
// RIPServerSocket at a specified location.
//
// - a method send(String s) that sends the String object s on the connection.
//
// - a method close() that closes the connection.
//
// The RIPsocket should also notify the user of errors and other situations that
// can occur (e.g. server not available, lost connection, the other party closed
// connection, or similar), for example by throwing an exception.
package no.uib.inf142.assignment3.rip.server;

import java.net.DatagramPacket;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class RIPServerSocket {

	private BlockingQueue<String> dataBuffer;
	private ReceiverThread receiver;

	public RIPServerSocket(int port) throws SocketException {
		BlockingQueue<DatagramPacket> packetBuffer = new LinkedBlockingQueue<DatagramPacket>();
		dataBuffer = new LinkedBlockingQueue<String>();
		receiver = new ReceiverThread(port, packetBuffer);

		new Thread(receiver).start();
	}

	public String receive() {
		// TODO

		return null;
	}

	public void close() {
		// TODO
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

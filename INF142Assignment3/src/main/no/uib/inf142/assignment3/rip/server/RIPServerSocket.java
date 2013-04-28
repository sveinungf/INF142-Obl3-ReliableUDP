package no.uib.inf142.assignment3.rip.server;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import no.uib.inf142.assignment3.rip.common.Protocol;

public class RIPServerSocket implements Closeable {

	private BlockingQueue<String> dataBuffer;
	private PacketReceiver receiver;
	private ACKSender sender;

	public RIPServerSocket(int port) throws IOException {
		int startingSequence = Protocol.SEQUENCE_START;
		BlockingQueue<DatagramPacket> packetBuffer = new LinkedBlockingQueue<DatagramPacket>();
		DatagramSocket socket = new DatagramSocket(port);
		dataBuffer = new LinkedBlockingQueue<String>();

		receiver = new PacketReceiver(socket, packetBuffer);
		sender = new ACKSender(socket, packetBuffer, dataBuffer,
				startingSequence);

		new Thread(receiver).start();
		new Thread(sender).start();
	}

	public String receive() {
		String data = null;

		try {
			data = dataBuffer.take();
		} catch (InterruptedException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}

		return data;
	}

	@Override
	public void close() {
		receiver.close();
		sender.close();
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

package no.uib.inf142.assignment3.rip.server;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import no.uib.inf142.assignment3.rip.common.Protocol;

public class RIPServerSocket implements Closeable {

	private BlockingQueue<String> dataBuffer;
	private DatagramSocket socket;
	private Thread ackSenderThread;
	private Thread packetReceiverThread;

	/**
	 * Constructs a {@code RIPServerSocket} object, and which similarly to a
	 * {@code ServerSocket} constructor assigns the {@code RIPServerSocket} to a
	 * port number.
	 * 
	 * @param port
	 *            - The port number this server will listen on.
	 * @param relay
	 *            - The IP address and port which the relay listens on.
	 * @throws IOException
	 *             //TODO
	 */
	public RIPServerSocket(int port, int relayPort)
			throws IOException {

		int startingSequence = Protocol.SEQUENCE_START;
		dataBuffer = new LinkedBlockingQueue<String>();
		socket = new DatagramSocket(port);

		BlockingQueue<DatagramPacket> packetBuffer = new LinkedBlockingQueue<DatagramPacket>();

		PacketReceiver packetReceiver = new PacketReceiver(socket, packetBuffer);

		ACKSender ackSender = new ACKSender(socket, packetBuffer, dataBuffer,
				relayPort, startingSequence);

		ackSenderThread = new Thread(ackSender);
		packetReceiverThread = new Thread(packetReceiver);

		ackSenderThread.start();
		packetReceiverThread.start();
	}

	/**
	 * Returns a {@code String} object received on the connection.
	 * 
	 * @return the string.
	 */
	public String receive() {
		if (socket.isClosed()) {
			// TODO throw exception
		}

		if (!ackSenderThread.isAlive() || !packetReceiverThread.isAlive()) {
			// TODO throw exception
		}

		String data = null;

		try {
			data = dataBuffer.take();
		} catch (InterruptedException e) {
		}

		return data;
	}

	/**
	 * Closes this socket.
	 * 
	 * @see java.io.Closeable#close()
	 */
	@Override
	public void close() {
		ackSenderThread.interrupt();
		packetReceiverThread.interrupt();
		
		socket.close();
	}
}

// - The RIPServerSocket should also notify the user of errors and other
// situations that can occur (e.g. lost connection, bad data, data out of order
// (really should not happen), the other party closed connection, or similar),
// for example by throwing an exception.

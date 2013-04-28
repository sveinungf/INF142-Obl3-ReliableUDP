package no.uib.inf142.assignment3.rip.client;

import java.io.Closeable;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import no.uib.inf142.assignment3.rip.common.Protocol;
import no.uib.inf142.assignment3.rip.common.RIPPacket;

public class RIPSocket implements Closeable {

	private BlockingQueue<String> dataBuffer;
	private DatagramSocket socket;
	private Thread ackReceiverThread;
	private Thread packetMakerThread;
	private Thread packetSenderThread;

	/**
	 * Constructs a {@code RIPSocket} object, and which similarly to a
	 * {@code Socket} constructor creates a {@code RIPSocket} object already
	 * connected to a {@code RIPServerSocket} at a specified location.
	 * 
	 * @param server
	 *            - The IP address and port to the server.
	 * @param relay
	 *            - The IP address and port to the relay.
	 * @throws SocketException
	 *             if the socket could not be opened.
	 */
	public RIPSocket(InetSocketAddress server, InetSocketAddress relay)
			throws SocketException {

		int startingSequence = Protocol.SEQUENCE_START;
		dataBuffer = new LinkedBlockingQueue<String>();
		socket = new DatagramSocket();

		BlockingQueue<RIPPacket> packetBuffer = new LinkedBlockingQueue<RIPPacket>();
		BlockingQueue<RIPPacket> window = new LinkedBlockingQueue<RIPPacket>();

		ACKReceiver ackReceiver = new ACKReceiver(window, socket,
				startingSequence);

		PacketMaker packetMaker = new PacketMaker(dataBuffer, packetBuffer,
				server, relay, startingSequence);

		PacketSender packetSender = new PacketSender(socket, packetBuffer,
				window);

		ackReceiverThread = new Thread(ackReceiver);
		packetMakerThread = new Thread(packetMaker);
		packetSenderThread = new Thread(packetSender);

		ackReceiverThread.start();
		packetMakerThread.start();
		packetSenderThread.start();
	}

	/**
	 * Sends the given {@code String} object on the connection.
	 * 
	 * @param string
	 *            - The string to send.
	 */
	public void send(String string) {
		if (socket.isClosed()) {
			// TODO throw exception
		}

		if (!ackReceiverThread.isAlive() || !packetMakerThread.isAlive()
				|| !packetSenderThread.isAlive()) {
			// TODO throw exception
		}

		try {
			dataBuffer.put(string);
		} catch (InterruptedException e) {
		}
	}

	/**
	 * Closes this socket.
	 * 
	 * @see java.io.Closeable#close()
	 */
	@Override
	public void close() {
		ackReceiverThread.interrupt();
		packetMakerThread.interrupt();
		packetSenderThread.interrupt();
	}
}

// TODO
// The RIPsocket should also notify the user of errors and other situations that
// can occur (e.g. server not available, lost connection, the other party closed
// connection, or similar), for example by throwing an exception.

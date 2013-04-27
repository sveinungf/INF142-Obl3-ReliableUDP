package no.uib.inf142.assignment3.rip.client;

import java.io.Closeable;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import no.uib.inf142.assignment3.rip.common.RIPPacket;

public class RIPSocket implements Closeable {

	private BlockingQueue<String> dataBuffer;
	private DatagramSocket socket;
	private ACKReceiverThread ackReceiver;
	private PacketMakerThread packetMaker;
	private PacketSenderThread packetSender;

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

		int startingSequence = 0;
		dataBuffer = new LinkedBlockingQueue<String>();
		socket = new DatagramSocket();

		BlockingQueue<RIPPacket> packetBuffer = new LinkedBlockingQueue<RIPPacket>();
		BlockingQueue<RIPPacket> window = new LinkedBlockingQueue<RIPPacket>();

		ackReceiver = new ACKReceiverThread(window, socket, startingSequence);
		packetMaker = new PacketMakerThread(dataBuffer, packetBuffer, server,
				relay, startingSequence);

		packetSender = new PacketSenderThread(socket, window, packetBuffer);

		new Thread(ackReceiver).start();
		new Thread(packetMaker).start();
		new Thread(packetSender).start();
	}

	/**
	 * Sends the given {@code String} object on the connection.
	 * 
	 * @param string
	 *            - The string to send.
	 */
	public void send(String string) {
		try {
			dataBuffer.put(string);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Closes this socket.
	 * 
	 * @see java.io.Closeable#close()
	 */
	@Override
	public void close() {
		socket.close();
	}
}

// TODO
// The RIPsocket should also notify the user of errors and other situations that
// can occur (e.g. server not available, lost connection, the other party closed
// connection, or similar), for example by throwing an exception.

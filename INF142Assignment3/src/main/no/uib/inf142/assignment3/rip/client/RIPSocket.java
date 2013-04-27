package no.uib.inf142.assignment3.rip.client;

import java.io.Closeable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class RIPSocket implements Closeable {

	private BlockingQueue<String> dataBuffer;
	private DatagramSocket socket;
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

		dataBuffer = new LinkedBlockingQueue<String>();
		socket = new DatagramSocket();

		BlockingQueue<DatagramPacket> packetBuffer = new LinkedBlockingQueue<DatagramPacket>();
		packetMaker = new PacketMakerThread(dataBuffer, packetBuffer, server,
				relay);
		
		BlockingQueue<DatagramPacket> window = new LinkedBlockingQueue<DatagramPacket>();

		packetSender = new PacketSenderThread(socket, window, packetBuffer);

		new Thread(packetMaker).start();
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

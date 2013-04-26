package no.uib.inf142.assignment3.rip.client;

import java.io.Closeable;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class RIPSocket implements Closeable {

	private BlockingQueue<String> dataBuffer;
	private DatagramSocket socket;
	private InetAddress serverAddress;
	private int serverport;

	/**
	 * Constructs a {@code RIPSocket} object, and which similarly to a
	 * {@code Socket} constructor creates a {@code RIPSocket} object already
	 * connected to a {@code RIPServerSocket} at a specified location.
	 * 
	 * @param serverAddress
	 *            - The IP address to the server.
	 * @param serverPort
	 *            - The port number which the server listens on.
	 * @throws SocketException
	 *             if the socket could not be opened.
	 */
	public RIPSocket(InetAddress serverAddress, int serverPort)
			throws SocketException {

		dataBuffer = new LinkedBlockingQueue<String>();
		socket = new DatagramSocket();
		this.serverAddress = serverAddress;
		this.serverport = serverPort;
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

// The RIPsocket should also notify the user of errors and other situations that
// can occur (e.g. server not available, lost connection, the other party closed
// connection, or similar), for example by throwing an exception.

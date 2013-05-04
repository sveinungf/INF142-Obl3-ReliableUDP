package no.uib.inf142.assignment3.rip.server;

import java.io.Closeable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import no.uib.inf142.assignment3.rip.common.Protocol;
import no.uib.inf142.assignment3.rip.common.RIPThread;

public class RIPServerSocket implements Closeable {

	private BlockingQueue<String> dataBuffer;
	private DatagramSocket socket;
	private RIPThread ackSenderThread;
	private RIPThread packetReceiverThread;

	/**
	 * Constructs a {@code RIPServerSocket} object, and which similarly to a
	 * {@code ServerSocket} constructor assigns the {@code RIPServerSocket} to a
	 * port number.
	 * 
	 * @param port
	 *            - The port number this server will listen on.
	 * @param relay
	 *            - The IP address and port which the relay listens on.
	 * @throws SocketException
	 *             if the socket could not be opened, or the socket could not
	 *             bind to the specified local port.
	 */
	public RIPServerSocket(int port, int relayPort) throws SocketException {

		int startingSequence = Protocol.SEQUENCE_START;
		dataBuffer = new LinkedBlockingQueue<String>();
		socket = new DatagramSocket(port);

		BlockingQueue<DatagramPacket> packetBuffer = new LinkedBlockingQueue<DatagramPacket>();

		packetReceiverThread = new PacketReceiverThread(socket, packetBuffer);

		ackSenderThread = new ACKSenderThread(socket, packetBuffer, dataBuffer,
				relayPort, startingSequence);

		ackSenderThread.start();
		packetReceiverThread.start();
	}

	/**
	 * Returns a {@code String} object received on the connection.
	 * 
	 * @return the string.
	 * @throws SocketException
	 *             if the socket is closed, or any of the threads this
	 *             {@code RIPServerSocket} started have died.
	 */
	public String receive() throws SocketException {
		if (socket.isClosed()) {
			throw new SocketException("Lost connection");
		}

		if (!ackSenderThread.isAlive()) {
			String error = ackSenderThread.getException().getMessage();
			throw new SocketException(error);
		}

		if (!packetReceiverThread.isAlive()) {
			String error = packetReceiverThread.getException().getMessage();
			throw new SocketException(error);
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
    public final void close() {
		ackSenderThread.interrupt();
		packetReceiverThread.interrupt();

		socket.close();
	}
}

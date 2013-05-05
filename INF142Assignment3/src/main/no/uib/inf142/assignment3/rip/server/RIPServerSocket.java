package no.uib.inf142.assignment3.rip.server;

import java.io.Closeable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import no.uib.inf142.assignment3.rip.common.Protocol;
import no.uib.inf142.assignment3.rip.common.RIPThread;

public class RIPServerSocket implements Closeable {

    private BlockingQueue<String> dataBuffer;
    private DatagramSocket socket;
    private List<RIPThread> threads;

    /**
     * Constructs a {@code RIPServerSocket} object, and which similarly to a
     * {@code ServerSocket} constructor assigns the {@code RIPServerSocket} to a
     * port number.
     * 
     * @param port
     *            - The port number this server will listen on.
     * @param relayPort
     *            - The port number which the relay listens on.
     * @throws SocketException
     *             if the socket could not be opened, or the socket could not
     *             bind to the specified local port.
     */
    public RIPServerSocket(final int port, final int relayPort)
            throws SocketException {

        int startingSequence = Protocol.SEQUENCE_START;
        dataBuffer = new LinkedBlockingQueue<String>();
        socket = new DatagramSocket(port);
        threads = new ArrayList<RIPThread>();

        BlockingQueue<DatagramPacket> packetBuffer = new LinkedBlockingQueue<DatagramPacket>();

        threads.add(new ACKSenderThread(socket, packetBuffer, dataBuffer,
                relayPort, startingSequence));

        threads.add(new PacketReceiverThread(socket, packetBuffer));

        for (RIPThread thread : threads) {
            thread.start();
        }
    }

    /**
     * Returns a {@code String} object received on the connection.
     * 
     * @return the string.
     * @throws SocketException
     *             if the socket is closed, or any of the threads this
     *             {@code RIPServerSocket} started have died.
     */
    public final String receive() throws SocketException {
        String data;

        if (dataBuffer.peek() == null) {
            if (socket.isClosed()) {
                throw new SocketException("Lost connection");
            }

            for (RIPThread thread : threads) {
                if (!thread.isAlive() || thread.isClosed()) {
                    String error = thread.getException().getMessage();
                    throw new SocketException(error);
                }
            }
        }

        try {
            data = dataBuffer.take();
        } catch (InterruptedException e) {
            throw new SocketException("Interrupted while fetching from buffer");
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
        threads.get(0).interrupt();
    }
}

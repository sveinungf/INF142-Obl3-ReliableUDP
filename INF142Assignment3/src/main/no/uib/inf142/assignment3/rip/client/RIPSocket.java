package no.uib.inf142.assignment3.rip.client;

import java.io.Closeable;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import no.uib.inf142.assignment3.rip.common.Protocol;
import no.uib.inf142.assignment3.rip.common.RIPPacket;
import no.uib.inf142.assignment3.rip.common.RIPThread;

public class RIPSocket implements Closeable {

    private BlockingQueue<String> dataBuffer;
    private DatagramSocket socket;
    private List<RIPThread> threads;
    private int sequence;

    /**
     * Constructs a {@code RIPSocket} object, and which similarly to a
     * {@code Socket} constructor creates a {@code RIPSocket} object already
     * connected to a {@code RIPServerSocket} at a specified location.
     * 
     * @param server
     *            - The IP address and port which the server listens on.
     * @param relay
     *            - The IP address and port which the relay listens on.
     * @throws SocketException
     *             if the socket could not be opened.
     */
    public RIPSocket(final InetSocketAddress server,
            final InetSocketAddress relay) throws SocketException {

        dataBuffer = new LinkedBlockingQueue<String>();
        socket = new DatagramSocket();
        threads = new ArrayList<RIPThread>();
        sequence = Protocol.SEQUENCE_START;

        BlockingQueue<RIPPacket> inPacketBuffer = new LinkedBlockingQueue<RIPPacket>();
        BlockingQueue<RIPPacket> outPacketBuffer = new LinkedBlockingQueue<RIPPacket>();
        BlockingQueue<RIPPacket> window = new ArrayBlockingQueue<RIPPacket>(
                Protocol.WINDOW_SIZE);

        threads.add(new ACKReceiverThread(inPacketBuffer, window, socket,
                sequence));

        threads.add(new PacketMakerThread(dataBuffer, inPacketBuffer,
                outPacketBuffer, server, relay, sequence));

        threads.add(new PacketSenderThread(socket, outPacketBuffer, window));

        for (RIPThread thread : threads) {
            thread.start();
        }
    }

    /**
     * Sends the given {@code String} object on the connection.
     * 
     * @param string
     *            - The string to send.
     * @throws SocketException
     *             if the socket is closed, or any of the threads this
     *             {@code RIPSocket} started have died.
     */
    public final void send(final String string) throws SocketException {
        if (socket.isClosed()) {
            throw new SocketException("Lost connection");
        }

        for (RIPThread thread : threads) {
            if (!thread.isAlive()) {
                String error = thread.getException().getMessage();
                throw new SocketException(error);
            }
        }

        try {
            dataBuffer.put(string);
        } catch (InterruptedException e) {
            throw new SocketException("Interrupted while buffering data");
        }
    }

    /**
     * Closes this socket.
     * 
     * @see java.io.Closeable#close()
     */
    @Override
    public final void close() {
        // for (RIPThread thread : threads) {
        // thread.interrupt();
        // }
        // threads.get(0).interrupt();
        threads.get(0).interrupt();
        threads.get(1).interrupt();

        // socket.close();
    }
}

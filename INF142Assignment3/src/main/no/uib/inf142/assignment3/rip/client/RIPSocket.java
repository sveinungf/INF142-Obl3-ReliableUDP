package no.uib.inf142.assignment3.rip.client;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import no.uib.inf142.assignment3.rip.common.Protocol;
import no.uib.inf142.assignment3.rip.common.RIPPacket;
import no.uib.inf142.assignment3.rip.common.RIPThread;
import no.uib.inf142.assignment3.rip.common.SequentialRIPPacketGenerator;
import no.uib.inf142.assignment3.rip.common.Signal;
import no.uib.inf142.assignment3.rip.exception.TooShortPacketLengthException;

public class RIPSocket implements Closeable {

    private BlockingQueue<String> dataBuffer;
    private DatagramSocket socket;
    private RIPThread ackReceiverThread;
    private RIPThread packetMakerThread;
    private RIPThread packetSenderThread;
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
        sequence = Protocol.SEQUENCE_START;

        BlockingQueue<RIPPacket> packetBuffer = new LinkedBlockingQueue<RIPPacket>();
        BlockingQueue<RIPPacket> window = new LinkedBlockingQueue<RIPPacket>();

        connectionSetup(server, relay);

        ackReceiverThread = new ACKReceiverThread(window, socket, sequence);

        packetMakerThread = new PacketMakerThread(dataBuffer, packetBuffer,
                server, relay, sequence);

        packetSenderThread = new PacketSenderThread(socket, packetBuffer,
                window);

        ackReceiverThread.start();
        packetMakerThread.start();
        packetSenderThread.start();
    }

    private void connectionSetup(final InetSocketAddress server,
            final InetSocketAddress relay) {

        SequentialRIPPacketGenerator packetGen = new SequentialRIPPacketGenerator(
                server, relay, sequence);

        try {
            RIPPacket syn = packetGen.makeSignalPacket(Signal.SYN);
            socket.send(syn.getDatagramPacket());
        } catch (TooShortPacketLengthException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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

        if (!ackReceiverThread.isAlive()) {
            String error = ackReceiverThread.getException().getMessage();
            throw new SocketException(error);
        }

        if (!packetMakerThread.isAlive()) {
            String error = packetMakerThread.getException().getMessage();
            throw new SocketException(error);
        }

        if (!packetSenderThread.isAlive()) {
            String error = packetSenderThread.getException().getMessage();
            throw new SocketException(error);
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
        packetMakerThread.interrupt();
        packetSenderThread.interrupt();
        ackReceiverThread.interrupt();

        socket.close();
    }
}

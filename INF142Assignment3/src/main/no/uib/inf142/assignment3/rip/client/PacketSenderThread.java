package no.uib.inf142.assignment3.rip.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import no.uib.inf142.assignment3.rip.common.PacketUtils;
import no.uib.inf142.assignment3.rip.common.Protocol;
import no.uib.inf142.assignment3.rip.common.RIPPacket;
import no.uib.inf142.assignment3.rip.common.RIPThread;
import no.uib.inf142.assignment3.rip.common.enums.Signal;

public class PacketSenderThread extends RIPThread {

    private static final int WAITTIME_IN_MILLIS = 10;

    private BlockingQueue<RIPPacket> outPacketBuffer;
    private BlockingQueue<RIPPacket> window;
    private DatagramSocket socket;
    private RIPThread packetMakerThread;
    private SimpleTimer timer;

    public PacketSenderThread(final DatagramSocket socket,
            final BlockingQueue<RIPPacket> outPacketBuffer,
            final BlockingQueue<RIPPacket> window,
            final RIPThread packetMakerThread) {

        this.window = window;
        this.outPacketBuffer = outPacketBuffer;
        this.socket = socket;
        this.packetMakerThread = packetMakerThread;
        timer = new SimpleTimer(Protocol.SENDER_TIMEOUT);
    }

    private void connectionSetup() {
        int maxAttempts = Protocol.CONNECTION_ATTEMPTS;
        int attempts = 0;

        try {
            RIPPacket syn = outPacketBuffer.take();
            RIPPacket ack = null;
            window.put(syn);

            DatagramPacket packet = syn.getDatagramPacket();

            while (ack == null && attempts < maxAttempts) {
                socket.send(packet);

                String payload = PacketUtils.getPayloadFromPacket(packet);
                System.out.println("[PacketSender] Sent: \"" + payload + "\"");

                ack = outPacketBuffer.poll(Protocol.SENDER_TIMEOUT,
                        TimeUnit.MILLISECONDS);

                ++attempts;

                if (ack == null && attempts < maxAttempts) {
                    System.out.println("[PacketSender] Timeout, resending SYN");
                }
            }

            if (attempts >= maxAttempts) {
                System.out.println("[PacketSender] "
                        + "Closing, reached max connection attempts");
                throw new SocketException("Reached max connection attempts");
            }

            packet = ack.getDatagramPacket();
            socket.send(packet);

            String payload = PacketUtils.getPayloadFromPacket(packet);
            System.out.println("[PacketSender] Sent: \"" + payload + "\"");
        } catch (IOException e) {
            active = false;
            exception = e;
        } catch (InterruptedException e) {
            exception = e;
        }
    }

    @Override
    public final void run() {
        connectionSetup();

        boolean closing = false;
        int maxAttempts = Protocol.CONNECTION_ATTEMPTS;
        int attempts = 0;

        timer.restart();

        try {
            while (active && !Thread.interrupted()) {
                boolean timeout = timer.timedOut();

                if (timeout && !window.isEmpty()) {
                    ++attempts;

                    if (attempts < maxAttempts) {
                        System.out.println("[PacketSender] "
                                + "Timeout, resending all in window");

                        for (RIPPacket ripPacket : window) {
                            socket.send(ripPacket.getDatagramPacket());
                        }
                    } else {
                        System.out.println("[PacketSender] "
                                + "Closing, reached max connection attempts");
                        throw new SocketException(
                                "Reached max connection attempts");
                    }

                    timer.restart();
                } else if (timeout) {
                    timer.restart();
                } else if (!outPacketBuffer.isEmpty()
                        && window.remainingCapacity() > 0) {

                    attempts = 0;

                    RIPPacket ripPacket = outPacketBuffer.take();

                    if (window.isEmpty()) {
                        timer.restart();
                    }

                    window.put(ripPacket);

                    DatagramPacket packet = ripPacket.getDatagramPacket();
                    socket.send(packet);

                    String payload = PacketUtils.getPayloadFromPacket(packet);
                    System.out.println("[PacketSender] Sent: \"" + payload
                            + "\"");

                    Signal signal = ripPacket.getSignal();

                    switch (signal) {
                    case FIN:
                        closing = true;
                        break;
                    case ACK:
                        if (closing) {
                            System.out.println("[PacketSender] Time wait");
                            synchronized (window) {
                                window.wait(Protocol.FIN_TIME_WAIT);
                            }

                            if (outPacketBuffer.peek() == null) {
                                throw new SocketException("Connection closed");
                            }
                        }
                        break;
                    default:
                        closing = false;
                        break;
                    }
                } else {
                    Thread.sleep(WAITTIME_IN_MILLIS);
                }
            }
        } catch (IOException e) {
            active = false;
            exception = e;
        } catch (InterruptedException e) {
            exception = e;
        }

        packetMakerThread.setActive(false);
        packetMakerThread.interrupt();

        System.out.println("[PacketSender] Connection successfully closed");
        socket.close();
    }
}

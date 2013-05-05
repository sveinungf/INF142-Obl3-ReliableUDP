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

public class PacketSenderThread extends RIPThread {

    private static final int WAITTIME_IN_MILLIS = 10;

    private BlockingQueue<RIPPacket> packetBuffer;
    private BlockingQueue<RIPPacket> window;
    private DatagramSocket socket;
    private SimpleTimer timer;

    public PacketSenderThread(final DatagramSocket socket,
            final BlockingQueue<RIPPacket> packetBuffer,
            final BlockingQueue<RIPPacket> window) {

        this.window = window;
        this.packetBuffer = packetBuffer;
        this.socket = socket;
        timer = new SimpleTimer(Protocol.TIMEOUT_IN_MILLIS);
    }

    private void connectionSetup() {
        int maxAttempts = Protocol.CONNECTION_ATTEMPTS;
        int attempts = 0;

        try {
            RIPPacket syn = packetBuffer.take();
            RIPPacket ack = null;
            window.put(syn);

            DatagramPacket packet = syn.getDatagramPacket();

            while (ack == null && attempts < maxAttempts) {
                socket.send(packet);

                String payload = PacketUtils.getPayloadFromPacket(packet);
                System.out.println("[PacketSender] Sent: \"" + payload + "\"");

                ack = packetBuffer.poll(Protocol.TIMEOUT_IN_MILLIS,
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
        } catch (IOException | InterruptedException e) {
            active = false;
            exception = e;
        }
    }

    @Override
    public final void run() {
        connectionSetup();

        int maxAttempts = Protocol.CONNECTION_ATTEMPTS;
        int attempts = 0;
        int maxWindowSize = Protocol.WINDOW_SIZE;

        timer.restart();

        while (active && !Thread.interrupted()) {
            boolean timeout = timer.timedOut();
            boolean windowFull = window.size() > maxWindowSize;

            try {
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
                } else if (!packetBuffer.isEmpty() && !windowFull) {
                    attempts = 0;

                    RIPPacket ripPacket = packetBuffer.take();

                    if (window.isEmpty()) {
                        timer.restart();
                    }

                    window.put(ripPacket);

                    DatagramPacket packet = ripPacket.getDatagramPacket();
                    socket.send(packet);

                    String payload = PacketUtils.getPayloadFromPacket(packet);
                    System.out.println("[PacketSender] Sent: \"" + payload
                            + "\"");
                } else {
                    Thread.sleep(WAITTIME_IN_MILLIS);
                }
            } catch (IOException | InterruptedException e) {
                active = false;
                exception = e;
            }
        }
    }
}

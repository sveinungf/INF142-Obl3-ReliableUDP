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

    private BlockingQueue<RIPPacket> outPacketBuffer;
    private BlockingQueue<RIPPacket> window;
    private DatagramSocket socket;
    private SimpleTimer timer;

    public PacketSenderThread(final DatagramSocket socket,
            final BlockingQueue<RIPPacket> outPacketBuffer,
            final BlockingQueue<RIPPacket> window) {

        this.window = window;
        this.outPacketBuffer = outPacketBuffer;
        this.socket = socket;
        timer = new SimpleTimer(Protocol.TIMEOUT_IN_MILLIS);
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

                ack = outPacketBuffer.poll(Protocol.TIMEOUT_IN_MILLIS,
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

        int maxAttempts = Protocol.CONNECTION_ATTEMPTS;
        int attempts = 0;
        int maxWindowSize = Protocol.WINDOW_SIZE;

        timer.restart();

        try {
            while (active && !Thread.interrupted()) {
                boolean timeout = timer.timedOut();
                boolean windowFull = window.size() > maxWindowSize;

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
                } else if (!outPacketBuffer.isEmpty() && !windowFull) {
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

        System.out.println("[PacketSender] active: " + active);
        if (active) {
            connectionTeardown();
        }
    }

    private void connectionTeardown() {
        int maxAttempts = Protocol.CONNECTION_ATTEMPTS;
        int attempts = 0;

        try {
            RIPPacket fin = outPacketBuffer.take();
            int finSequence = fin.getSequence();

            window.put(fin);
            boolean finInWindow = true;

            DatagramPacket packet = fin.getDatagramPacket();

            while (finInWindow && attempts < maxAttempts) {
                socket.send(packet);

                String payload = PacketUtils.getPayloadFromPacket(packet);
                System.out.println("[PacketSender] Sent: \"" + payload + "\"");

                // FIN wait 1
                synchronized (window) {
                    window.wait(Protocol.TIMEOUT_IN_MILLIS);
                }

                ++attempts;

                finInWindow = false;
                for (RIPPacket current : window) {
                    if (current.getSequence() == finSequence) {
                        finInWindow = true;
                        break;
                    }
                }

                if (finInWindow && attempts < maxAttempts) {
                    System.out.println("[PacketSender] Timeout, resending FIN");
                }
            }

            if (attempts >= maxAttempts) {
                System.out.println("[PacketSender] "
                        + "Closing, reached max connection attempts");
                throw new SocketException("Reached max connection attempts");
            }

            // FIN wait 2
            RIPPacket ack = outPacketBuffer.take();
            boolean serverGotLastACK = false;
            packet = ack.getDatagramPacket();

            while (!serverGotLastACK) {
                socket.send(packet);

                String payload = PacketUtils.getPayloadFromPacket(packet);
                System.out.println("[PacketSender] Sent: \"" + payload + "\"");

                // Time wait
                ack = outPacketBuffer.poll(Protocol.FIN_TIME_WAIT,
                        TimeUnit.MILLISECONDS);

                if (ack != null) {
                    packet = ack.getDatagramPacket();
                    socket.send(packet);
                } else {
                    serverGotLastACK = true;
                }
            }
        } catch (IOException | InterruptedException e) {
            exception = e;
            e.printStackTrace();
        }
    }
}

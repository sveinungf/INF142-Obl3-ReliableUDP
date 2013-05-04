package no.uib.inf142.assignment3.rip.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.BlockingQueue;

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
        try {
            RIPPacket syn = packetBuffer.take();
            window.put(syn);

            DatagramPacket packet = syn.getDatagramPacket();
            socket.send(packet);

            // TODO timeout
            String payload = PacketUtils.getPayloadFromPacket(packet);
            System.out.println("[PacketSender] Sent: \"" + payload + "\"");

            RIPPacket ack = packetBuffer.take();

            packet = ack.getDatagramPacket();
            socket.send(packet);

            payload = PacketUtils.getPayloadFromPacket(packet);
            System.out.println("[PacketSender] Sent: \"" + payload + "\"");
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public final void run() {
        connectionSetup();

        int maxWindowSize = Protocol.WINDOW_SIZE;
        timer.restart();

        while (active && !Thread.interrupted()) {
            boolean timeout = timer.timedOut();
            boolean windowFull = window.size() > maxWindowSize;

            try {
                if (timeout) {
                    if (!window.isEmpty()) {
                        for (RIPPacket ripPacket : window) {
                            socket.send(ripPacket.getDatagramPacket());
                        }
                        System.out.println("[PacketSender] "
                                + "Timeout, sent all in window");
                    }

                    timer.restart();
                } else if (!packetBuffer.isEmpty() && !windowFull) {
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

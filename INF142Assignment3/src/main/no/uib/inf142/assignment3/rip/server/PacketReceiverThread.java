package no.uib.inf142.assignment3.rip.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.BlockingQueue;

import no.uib.inf142.assignment3.rip.common.Protocol;
import no.uib.inf142.assignment3.rip.common.RIPThread;

public class PacketReceiverThread extends RIPThread {

    private BlockingQueue<DatagramPacket> packetBuffer;
    private DatagramSocket socket;
    private RIPThread ackSenderThread;

    public PacketReceiverThread(final DatagramSocket socket,
            final BlockingQueue<DatagramPacket> packetBuffer,
            final RIPThread ackSenderThread) throws SocketException {

        this.packetBuffer = packetBuffer;
        this.socket = socket;
        this.ackSenderThread = ackSenderThread;
        socket.setSoTimeout((int) Protocol.SERVER_TIMEOUT);
    }

    @Override
    public final void run() {
        while (active) {
            byte[] byteData = new byte[Protocol.PACKETDATA_LENGTH];
            DatagramPacket packet = new DatagramPacket(byteData,
                    byteData.length);

            try {
                socket.receive(packet);
                String data = new String(packet.getData(), 0,
                        packet.getLength());
                System.out.println("[PacketReceiver] Received: \"" + data
                        + "\"");

                packetBuffer.put(packet);
            } catch (SocketTimeoutException e) {
                ackSenderThread.setActive(false);
                ackSenderThread.interrupt();

                active = false;
                exception = e;
            } catch (IOException | InterruptedException e) {
                active = false;
                exception = e;
            }
        }
    }
}

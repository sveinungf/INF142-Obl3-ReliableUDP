package no.uib.inf142.assignment3.rip.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;

import no.uib.inf142.assignment3.rip.common.Datafield;
import no.uib.inf142.assignment3.rip.common.PacketUtils;
import no.uib.inf142.assignment3.rip.common.Protocol;
import no.uib.inf142.assignment3.rip.common.RIPPacket;
import no.uib.inf142.assignment3.rip.common.RIPThread;
import no.uib.inf142.assignment3.rip.common.Signal;
import no.uib.inf142.assignment3.rip.common.SignalMap;
import no.uib.inf142.assignment3.rip.exception.InvalidPacketException;

public class ACKReceiverThread extends RIPThread {

    private int expectedSequence;
    private BlockingQueue<RIPPacket> inPacketBuffer;
    private BlockingQueue<RIPPacket> window;
    private DatagramSocket socket;

    public ACKReceiverThread(final BlockingQueue<RIPPacket> inPacketBuffer,
            final BlockingQueue<RIPPacket> window, final DatagramSocket socket,
            final int startingSequence) {

        expectedSequence = startingSequence;
        this.inPacketBuffer = inPacketBuffer;
        this.window = window;
        this.socket = socket;
    }

    private RIPPacket getVerifiedRIPPacket(final DatagramPacket packet)
            throws InvalidPacketException {

        String payload = PacketUtils.getPayloadFromPacket(packet);
        PacketUtils.verifyChecksumInPayload(payload);

        String[] datafields = PacketUtils.getDatafields(payload);

        String signalString = datafields[Datafield.SIGNAL.ordinal()];
        Signal signal = SignalMap.getInstance().getByString(signalString);

        if (signal == null) {
            throw new InvalidPacketException("Invalid signal in packet: "
                    + signal);
        }

        String sequenceString = datafields[Datafield.SEQUENCE.ordinal()];
        int sequence = PacketUtils.convertFromHexStringToInt(sequenceString);

        return new RIPPacket(sequence, packet, signal);
    }

    private void removePacketsFromWindow(final int sequence) {
        Iterator<RIPPacket> it = window.iterator();

        while (it.hasNext()) {
            RIPPacket currentRIPPacket = it.next();

            if (sequence >= currentRIPPacket.getSequence()) {
                it.remove();
            }
        }
    }

    @Override
    public final void run() {
        while (active && !Thread.interrupted()) {
            try {
                byte[] byteData = new byte[Protocol.PACKETDATA_LENGTH];
                DatagramPacket ack = new DatagramPacket(byteData,
                        byteData.length);

                socket.receive(ack);

                RIPPacket ripPacket = getVerifiedRIPPacket(ack);

                String payload = PacketUtils.getPayloadFromPacket(ack);
                int sequence = ripPacket.getSequence();

                if (sequence >= expectedSequence) {
                    System.out.println("[ACKReceiver] Received expected: \""
                            + payload + "\"");

                    if (ripPacket.getSignal() == Signal.SYNACK) {
                        inPacketBuffer.put(ripPacket);
                    }

                    removePacketsFromWindow(sequence);
                    expectedSequence = sequence + 1;
                } else {
                    System.out.println("[ACKReceiver] Received unexpected: \""
                            + payload + "\"");
                }
            } catch (InvalidPacketException e) {
                System.out.println("[ACKReceiver] " + e.getMessage()
                        + ", ignored");
            } catch (IOException e) {
                active = false;
                exception = e;
                interrupt();
            } catch (InterruptedException e) {
                exception = e;
                interrupt();
            }
        }

        if (active) {
            connectionTeardown();
        }
    }

    private void connectionTeardown() {
        boolean serverGotLastACK = false;

        while (active && !serverGotLastACK) {
            try {
                byte[] byteData = new byte[Protocol.PACKETDATA_LENGTH];
                DatagramPacket ack = new DatagramPacket(byteData,
                        byteData.length);

                socket.receive(ack);

                RIPPacket ripPacket = getVerifiedRIPPacket(ack);

                String payload = PacketUtils.getPayloadFromPacket(ack);
                int sequence = ripPacket.getSequence();

                if (sequence >= expectedSequence) {
                    System.out.println("[ACKReceiver] Received expected: \""
                            + payload + "\"");

                    removePacketsFromWindow(sequence);
                    inPacketBuffer.put(ripPacket);
                    expectedSequence = sequence + 1;

                    if (ripPacket.getSignal() == Signal.FIN) {
                        socket.setSoTimeout((int) Protocol.FIN_TIME_WAIT);
                    }
                }
            } catch (InvalidPacketException e) {
                System.out.println("[ACKReceiver] " + e.getMessage()
                        + ", ignored");
            } catch (SocketTimeoutException e) {
                serverGotLastACK = true;
                socket.close();
            } catch (IOException | InterruptedException e) {
                active = false;
                exception = e;
            }
        }
    }
}

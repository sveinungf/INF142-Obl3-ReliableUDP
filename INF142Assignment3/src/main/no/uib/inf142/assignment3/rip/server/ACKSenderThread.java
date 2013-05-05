package no.uib.inf142.assignment3.rip.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;

import no.uib.inf142.assignment3.rip.common.PacketUtils;
import no.uib.inf142.assignment3.rip.common.PacketGenerator;
import no.uib.inf142.assignment3.rip.common.RIPThread;
import no.uib.inf142.assignment3.rip.common.enums.Datafield;
import no.uib.inf142.assignment3.rip.common.enums.Signal;
import no.uib.inf142.assignment3.rip.common.enums.SignalMap;
import no.uib.inf142.assignment3.rip.exception.InvalidPacketException;
import no.uib.inf142.assignment3.rip.exception.TooShortPacketLengthException;

public class ACKSenderThread extends RIPThread {

    private int expectedSequence;
    private int relayListeningPort;
    private BlockingQueue<DatagramPacket> packetBuffer;
    private BlockingQueue<String> dataBuffer;
    private DatagramPacket lastSent;
    private DatagramSocket socket;
    private StringBuilder stringBuilder;

    public ACKSenderThread(final DatagramSocket socket,
            final BlockingQueue<DatagramPacket> packetBuffer,
            final BlockingQueue<String> dataBuffer,
            final int relayListeningPort, final int startingSequence) {

        expectedSequence = startingSequence;
        this.relayListeningPort = relayListeningPort;
        this.packetBuffer = packetBuffer;
        this.dataBuffer = dataBuffer;
        lastSent = null;
        this.socket = socket;
        stringBuilder = new StringBuilder();
    }

    @Override
    public final void run() {
        boolean opening = false;
        boolean open = false;
        boolean closing = false;

        while (active && !Thread.interrupted()) {
            try {
                DatagramPacket packet = packetBuffer.take();

                InetAddress relayAddress = packet.getAddress();
                InetSocketAddress relay = new InetSocketAddress(relayAddress,
                        relayListeningPort);

                String payload = PacketUtils.getPayloadFromPacket(packet);
                PacketUtils.verifyChecksumInPayload(payload);

                String[] datafields = PacketUtils.getDatafields(payload);

                String sequenceString = datafields[Datafield.SEQUENCE.ordinal()];
                int sequence = PacketUtils
                        .convertFromHexStringToInt(sequenceString);

                String ipString = datafields[Datafield.IP.ordinal()];
                String portString = datafields[Datafield.PORT.ordinal()];

                InetSocketAddress source = PacketUtils.parseSocketAddress(
                        ipString, portString);

                if (sequence > expectedSequence) {
                    throw new InvalidPacketException(
                            "Sequence higher than expected, ignored");
                } else if (sequence < expectedSequence) {
                    if (lastSent != null) {
                        socket.send(lastSent);
                    }

                    throw new InvalidPacketException(
                            "Sequence less than expected, resent last packet");
                }

                ++expectedSequence;

                PacketGenerator packetGen = new PacketGenerator(source, relay);

                String signalString = datafields[Datafield.SIGNAL.ordinal()];
                Signal signal = SignalMap.getInstance().getByString(
                        signalString);

                switch (signal) {
                case SYN:
                    opening = true;
                    closing = false;
                    DatagramPacket synack = packetGen.makeSignalPacket(
                            sequence, Signal.SYNACK);

                    socket.send(synack);
                    lastSent = synack;

                    payload = PacketUtils.getPayloadFromPacket(synack);
                    System.out.println("[ACKSender] Sent: \"" + payload + "\"");
                    break;
                case PARTIAL:
                case REGULAR:
                    if (!open) {
                        throw new InvalidPacketException(
                                "Connection not opened properly");
                    }

                    opening = false;
                    closing = false;
                    DatagramPacket ack = packetGen.makeSignalPacket(sequence,
                            Signal.ACK);

                    socket.send(ack);
                    lastSent = ack;

                    payload = PacketUtils.getPayloadFromPacket(ack);
                    System.out.println("[ACKSender] Sent: \"" + payload + "\"");

                    boolean dataComplete = signal == Signal.REGULAR;
                    String data = datafields[Datafield.DATA.ordinal()];
                    stringBuilder.append(data);

                    if (dataComplete) {
                        dataBuffer.put(stringBuilder.toString());
                        stringBuilder = new StringBuilder();
                    }
                    break;
                case FIN:
                    if (!open) {
                        throw new InvalidPacketException(
                                "Connection not opened properly");
                    }

                    opening = false;
                    closing = true;

                    DatagramPacket finack = packetGen.makeSignalPacket(
                            sequence, Signal.ACK);

                    socket.send(finack);
                    lastSent = finack;

                    payload = PacketUtils.getPayloadFromPacket(finack);
                    System.out.println("[ACKSender] Sent: \"" + payload + "\"");

                    exception = new SocketException("Connection is closing");
                    closed = true;

                    DatagramPacket lastfin = packetGen.makeSignalPacket(
                            sequence + 1, signal);

                    socket.send(lastfin);
                    lastSent = lastfin;

                    payload = PacketUtils.getPayloadFromPacket(lastfin);
                    System.out.println("[ACKSender] Sent: \"" + payload + "\"");
                    break;
                case ACK:
                    if (opening) {
                        opening = false;
                        open = true;
                    } else if (closing) {
                        throw new SocketException("Connection closed");
                    }
                    break;
                default:
                    break;
                }
            } catch (InvalidPacketException e) {
                System.out.println("[ACKSender] " + e.getMessage());
            } catch (IOException | InterruptedException
                    | TooShortPacketLengthException e) {

                active = false;
                exception = e;
            }
        }

        System.out.println("[ACKSender] Connection successfully closed");
        socket.close();
    }
}

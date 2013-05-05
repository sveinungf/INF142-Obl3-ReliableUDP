package no.uib.inf142.assignment3.rip.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.BlockingQueue;

import no.uib.inf142.assignment3.rip.common.Datafield;
import no.uib.inf142.assignment3.rip.common.PacketUtils;
import no.uib.inf142.assignment3.rip.common.PacketGenerator;
import no.uib.inf142.assignment3.rip.common.RIPThread;
import no.uib.inf142.assignment3.rip.common.Signal;
import no.uib.inf142.assignment3.rip.common.SignalMap;
import no.uib.inf142.assignment3.rip.exception.InvalidPacketException;
import no.uib.inf142.assignment3.rip.exception.TooShortPacketLengthException;

public class ACKSenderThread extends RIPThread {

    private int expectedSequence;
    private int relayListeningPort;
    private BlockingQueue<DatagramPacket> packetBuffer;
    private BlockingQueue<String> dataBuffer;
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
        this.socket = socket;
        stringBuilder = new StringBuilder();
    }

    @Override
    public final void run() {
        while (active && !Thread.interrupted()) {
            try {
                DatagramPacket packet = packetBuffer.take();

                InetAddress relayAddress = packet.getAddress();
                InetSocketAddress relay = new InetSocketAddress(relayAddress,
                        relayListeningPort);

                String payload = PacketUtils.getPayloadFromPacket(packet);
                String[] datafields = PacketUtils.getDatafields(payload);

                boolean checksumOk = PacketUtils.validChecksumInPacket(payload);

                if (!checksumOk) {
                    throw new InvalidPacketException(
                            "Wrong checksum in packet, ignored");
                }

                String sequenceString = datafields[Datafield.SEQUENCE.ordinal()];
                int sequence = PacketUtils.convertFromHexString(sequenceString);

                String ipString = datafields[Datafield.IP.ordinal()];
                String portString = datafields[Datafield.PORT.ordinal()];

                InetSocketAddress source = PacketUtils.parseSocketAddress(
                        ipString, portString);

                if (sequence != expectedSequence) {
                    throw new InvalidPacketException(
                            "Got unexpected sequence, ignored");
                }

                ++expectedSequence;

                PacketGenerator packetGen = new PacketGenerator(source, relay);

                String signalString = datafields[Datafield.SIGNAL.ordinal()];
                Signal signal = SignalMap.getInstance().getByString(
                        signalString);

                if (signal == Signal.SYN) {
                    DatagramPacket synack = packetGen.makeSignalPacket(
                            sequence, Signal.SYNACK);

                    socket.send(synack);

                    payload = PacketUtils.getPayloadFromPacket(synack);
                    System.out.println("[ACKSender] Sent: \"" + payload + "\"");
                } else if (signal == Signal.REGULAR || signal == Signal.PARTIAL) {
                    DatagramPacket ack = packetGen.makeSignalPacket(sequence,
                            Signal.ACK);

                    socket.send(ack);

                    payload = PacketUtils.getPayloadFromPacket(ack);
                    System.out.println("[ACKSender] Sent: \"" + payload + "\"");

                    boolean dataComplete = signal == Signal.REGULAR;
                    String data = datafields[Datafield.DATA.ordinal()];
                    stringBuilder.append(data);

                    if (dataComplete) {
                        dataBuffer.put(stringBuilder.toString());
                        stringBuilder = new StringBuilder();
                    }
                }
            } catch (InvalidPacketException e) {
                System.out.println("[ACKSender] " + e.getMessage());
            } catch (IOException | InterruptedException
                    | TooShortPacketLengthException e) {

                active = false;
                exception = e;
            }
        }
    }
}

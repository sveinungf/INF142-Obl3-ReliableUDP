package no.uib.inf142.assignment3.rip.common;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import no.uib.inf142.assignment3.rip.common.enums.Datafield;
import no.uib.inf142.assignment3.rip.common.enums.Signal;
import no.uib.inf142.assignment3.rip.exception.TooShortPacketLengthException;

public class SequentialRIPPacketGenerator extends PacketGenerator {

    private int nextSequence;

    public SequentialRIPPacketGenerator(
            final InetSocketAddress finalDestination,
            final InetSocketAddress relay, final int startingSequence) {

        super(finalDestination, relay);
        nextSequence = startingSequence;
    }

    public final RIPPacket makeSignalPacket(final Signal signal)
            throws TooShortPacketLengthException {

        String seqString = PacketUtils.convertIntToHexString(nextSequence);
        String staticData = buildDelimitedString(seqString, signal.getString());

        String checksum = PacketUtils.getChecksum(Protocol.CHECKSUM_LENGTH,
                staticData);

        String payload = buildDelimitedString(storedAddressHeader, staticData,
                checksum);

        DatagramPacket packet = makePacket(payload);
        RIPPacket ripPacket = new RIPPacket(nextSequence, packet, signal);
        ++nextSequence;

        return ripPacket;
    }

    public final List<RIPPacket> makePackets(final String data)
            throws TooShortPacketLengthException {

        List<RIPPacket> packetList = new ArrayList<RIPPacket>();

        String dataLeft = data;
        boolean done = false;
        int dataLength = Datafield.calculateDataLength();

        if (dataLength <= 0) {
            throw new TooShortPacketLengthException("Packet length "
                    + Protocol.PACKETDATA_LENGTH + " too short");
        }

        while (!done) {
            String packetData;
            Signal signal;

            if (dataLeft.length() > dataLength) {
                packetData = dataLeft.substring(0, dataLength);
                dataLeft = dataLeft.substring(dataLength);
                signal = Signal.PARTIAL;
            } else {
                packetData = dataLeft;
                signal = Signal.REGULAR;
                done = true;
            }

            String seqString = PacketUtils.convertIntToHexString(nextSequence);

            String staticData = buildDelimitedString(seqString,
                    signal.getString(), packetData);

            String checksum = PacketUtils.getChecksum(Protocol.CHECKSUM_LENGTH,
                    staticData);

            String payload = buildDelimitedString(storedAddressHeader,
                    staticData, checksum);

            DatagramPacket packet = makePacket(payload);
            RIPPacket ripPacket = new RIPPacket(nextSequence, packet, signal);

            packetList.add(ripPacket);
            ++nextSequence;
        }

        return packetList;
    }
}

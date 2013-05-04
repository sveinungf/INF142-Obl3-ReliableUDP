package no.uib.inf142.assignment3.rip.common;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;

import no.uib.inf142.assignment3.rip.exception.TooShortPacketLengthException;

public class PacketGenerator {

    protected final String storedAddressHeader;
    protected final InetSocketAddress relay;

    public PacketGenerator(final InetSocketAddress finalDestination,
            final InetSocketAddress relay) {

        String ip = finalDestination.getAddress().getHostAddress();
        String port = "" + finalDestination.getPort();

        storedAddressHeader = buildDelimitedString(ip, port);
        this.relay = relay;
    }

    public final DatagramPacket makeACKPacket(final int sequence)
            throws TooShortPacketLengthException {

        String seq = PacketUtils.convertToHexString(sequence);
        String signal = Signal.ACK.getString();
        String payload = buildDelimitedString(storedAddressHeader, seq, signal);

        return makePacket(payload);
    }

    protected final DatagramPacket makePacket(final String data)
            throws TooShortPacketLengthException {

        int packetLength = Protocol.PACKET_LENGTH;

        // Need trailing spaces here since the Relay doesn't clean up
        int trailingSpaces = packetLength - data.length();
        String payload = data + PacketUtils.makeSpaces(trailingSpaces);

        byte[] byteData = payload.getBytes(Protocol.CHARSET);

        if (byteData.length > packetLength) {
            throw new TooShortPacketLengthException("Packet length "
                    + packetLength + " too short");
        }

        return new DatagramPacket(byteData, byteData.length,
                relay.getAddress(), relay.getPort());
    }

    protected static String buildDelimitedString(final String... values) {
        return PacketUtils.buildDelimitedString(Protocol.DATAFIELD_DELIMITER,
                values);
    }
}

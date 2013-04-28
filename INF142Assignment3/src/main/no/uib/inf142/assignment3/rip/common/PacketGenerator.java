package no.uib.inf142.assignment3.rip.common;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;

import no.uib.inf142.assignment3.rip.exception.TooShortPacketLengthException;

public class PacketGenerator {

	protected final String storedAddressHeader;
	protected final InetSocketAddress relay;

	public PacketGenerator(InetSocketAddress finalDestination,
			InetSocketAddress relay) {

		String ip = finalDestination.getAddress().getHostAddress();
		String port = "" + finalDestination.getPort();

		storedAddressHeader = buildDelimitedString(ip, port);
		this.relay = relay;
	}

	public DatagramPacket makeACKPacket(int sequence)
			throws TooShortPacketLengthException {

		String seq = PacketUtils.convertToHexString(sequence);
		String signal = Signal.ACK.getString();
		String payload = buildDelimitedString(storedAddressHeader, seq, signal);

		return makePacket(payload);
	}

	protected DatagramPacket makePacket(String payload)
			throws TooShortPacketLengthException {

		int packetLength = Protocol.MAX_PACKET_LENGTH;
		byte[] byteData = payload.getBytes();

		if (byteData.length > packetLength) {
			throw new TooShortPacketLengthException("Packet length "
					+ packetLength + " too short");
		}

		return new DatagramPacket(byteData, byteData.length,
				relay.getAddress(), relay.getPort());
	}

	protected static String buildDelimitedString(String... values) {
		return PacketUtils.buildDelimitedString(Protocol.PACKET_DELIMITER,
				values);
	}
}
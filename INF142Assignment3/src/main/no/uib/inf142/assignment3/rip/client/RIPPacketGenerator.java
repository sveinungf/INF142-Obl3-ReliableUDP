package no.uib.inf142.assignment3.rip.client;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import no.uib.inf142.assignment3.rip.common.PacketUtils;
import no.uib.inf142.assignment3.rip.common.Protocol;
import no.uib.inf142.assignment3.rip.common.RIPPacket;
import no.uib.inf142.assignment3.rip.common.Signal;
import no.uib.inf142.assignment3.rip.exception.TooShortPacketLengthException;

public class RIPPacketGenerator {

	private final String storedAddressHeader;
	private final InetSocketAddress relay;
	private int nextSequence;

	public RIPPacketGenerator(InetSocketAddress finalDestination,
			InetSocketAddress relay, int startingSequence) {

		String ip = finalDestination.getAddress().getHostAddress();
		String port = "" + finalDestination.getPort();

		storedAddressHeader = buildDelimitedString(ip, port);
		this.relay = relay;
		nextSequence = startingSequence;
	}

	public DatagramPacket makeACKPacket(int sequence)
			throws TooShortPacketLengthException {

		String seq = "" + sequence;
		String signal = Signal.ACK.getString();
		String payload = buildDelimitedString(storedAddressHeader, seq, signal);

		return makePacket(payload);
	}

	public DatagramPacket makeSignalPacket(final Signal signal)
			throws TooShortPacketLengthException {

		String seq = "" + nextSequence;
		String payload = buildDelimitedString(storedAddressHeader, seq,
				signal.getString());

		return makePacket(payload);
	}

	public List<RIPPacket> makePackets(final String data)
			throws TooShortPacketLengthException {

		List<RIPPacket> packetList = new ArrayList<RIPPacket>();

		String dataLeft = data;
		boolean done = false;

		int maxPacketLength = Protocol.MAX_PACKET_LENGTH;
		int checksumLength = Protocol.PACKET_DELIMITER.length()
				+ Protocol.CHECKSUM_LENGTH;
		int signalSpace = calculateMaxSignalSpace();

		while (!done) {
			String seq = "" + nextSequence;
			String header = buildDelimitedString(storedAddressHeader, seq);
			int dataLength = maxPacketLength - header.length() - signalSpace
					- checksumLength;

			if (dataLength <= 0) {
				throw new TooShortPacketLengthException("Packet length "
						+ maxPacketLength + " too short");
			}

			String packetData;
			String signal;

			if (dataLeft.length() > dataLength) {
				packetData = dataLeft.substring(0, dataLength);
				dataLeft = dataLeft.substring(dataLength);
				signal = Signal.PARTIAL.getString();
			} else {
				packetData = dataLeft;
				signal = Signal.REGULAR.getString();
				done = true;
			}

			String payload = buildDelimitedString(header, signal, packetData);
			String checksum = PacketUtils.getChecksum(Protocol.CHECKSUM_LENGTH,
					payload);

			String finalPayload = buildDelimitedString(payload, checksum);

			DatagramPacket packet = makePacket(finalPayload);
			RIPPacket ripPacket = new RIPPacket(nextSequence, packet);

			packetList.add(ripPacket);
			++nextSequence;
		}

		return packetList;
	}

	private DatagramPacket makePacket(String payload)
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

	private static int calculateMaxSignalSpace() {
		int delimiterLength = Protocol.PACKET_DELIMITER.length();
		int maxSignalSpace = 0;

		for (Signal signal : Signal.values()) {
			int signalSpace = signal.getString().length();

			if (signalSpace > maxSignalSpace) {
				maxSignalSpace = signalSpace;
			}
		}

		maxSignalSpace += (delimiterLength * 2);

		return maxSignalSpace;
	}

	private static String buildDelimitedString(String... values) {
		return PacketUtils.buildDelimitedString(Protocol.PACKET_DELIMITER,
				values);
	}
}
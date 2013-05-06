package no.uib.inf142.assignment3.rip.common;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
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

		byte[] bytesLeft = data.getBytes(Protocol.CHARSET);
		boolean done = false;
		int dataLength = Datafield.calculateDataLength();

		if (dataLength <= 0) {
			throw new TooShortPacketLengthException("Packet length "
					+ Protocol.PACKETDATA_LENGTH + " too short");
		}

		while (!done) {
			byte[] packetBytes;
			Signal signal;

			if (bytesLeft.length > dataLength) {
				packetBytes = Arrays.copyOfRange(bytesLeft, 0, dataLength);
				bytesLeft = Arrays.copyOfRange(bytesLeft, dataLength,
						bytesLeft.length);
				signal = Signal.PARTIAL;
			} else {
				packetBytes = bytesLeft;
				signal = Signal.REGULAR;
				done = true;
			}

			String packetData = new String(packetBytes, 0, packetBytes.length,
					Protocol.CHARSET);
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

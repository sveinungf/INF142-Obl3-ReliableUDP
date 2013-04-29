package no.uib.inf142.assignment3.rip.common;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import no.uib.inf142.assignment3.rip.exception.TooShortPacketLengthException;

public class SequentialRIPPacketGenerator extends PacketGenerator {

	private int nextSequence;

	public SequentialRIPPacketGenerator(InetSocketAddress finalDestination,
			InetSocketAddress relay, int startingSequence) {

		super(finalDestination, relay);
		nextSequence = startingSequence;
	}

	public RIPPacket makeSignalPacket(final Signal signal)
			throws TooShortPacketLengthException {

		String seq = PacketUtils.convertToHexString(nextSequence);
		String payload = buildDelimitedString(storedAddressHeader, seq,
				signal.getString());

		DatagramPacket packet = makePacket(payload);
		RIPPacket ripPacket = new RIPPacket(nextSequence, packet);
		++nextSequence;

		return ripPacket;
	}

	public List<RIPPacket> makePackets(final String data)
			throws TooShortPacketLengthException {

		List<RIPPacket> packetList = new ArrayList<RIPPacket>();

		String dataLeft = data;
		boolean done = false;
		int dataLength = Datafield.calculateDataLength();

		if (dataLength <= 0) {
			throw new TooShortPacketLengthException("Packet length "
					+ Protocol.MAX_PACKET_LENGTH + " too short");
		}

		while (!done) {
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

			String seqString = PacketUtils.convertToHexString(nextSequence);

			String staticData = buildDelimitedString(seqString, signal,
					packetData);

			String checksum = PacketUtils.getChecksum(Protocol.CHECKSUM_LENGTH,
					staticData);

			String payload = buildDelimitedString(storedAddressHeader,
					staticData, checksum);

			DatagramPacket packet = makePacket(payload);
			RIPPacket ripPacket = new RIPPacket(nextSequence, packet);

			packetList.add(ripPacket);
			++nextSequence;
		}

		return packetList;
	}
}

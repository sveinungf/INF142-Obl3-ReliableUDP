package no.uib.inf142.assignment3.rip.client;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import no.uib.inf142.assignment3.rip.common.PacketUtils;
import no.uib.inf142.assignment3.rip.common.Protocol;
import no.uib.inf142.assignment3.rip.common.RIPPacket;
import no.uib.inf142.assignment3.rip.common.Signal;
import no.uib.inf142.assignment3.rip.exception.TooShortPacketLengthException;

public class RIPPacketGenerator {

	private InetSocketAddress finalDestination;
	private InetSocketAddress relay;
	private int nextSequence;

	public RIPPacketGenerator(InetSocketAddress finalDestination,
			InetSocketAddress relay, int startingSequence) {

		this.finalDestination = finalDestination;
		this.relay = relay;
		nextSequence = startingSequence;
	}

	private String buildHeaderString() {
		String ip = finalDestination.getAddress().getHostAddress();
		String port = "" + finalDestination.getPort();
		String sequence = "" + nextSequence;

		return buildDelimitedString(ip, port, sequence);
	}

	public DatagramPacket makeACKPacket(int sequence)
			throws TooShortPacketLengthException, SocketException {

		String ip = finalDestination.getAddress().getHostAddress();
		String port = "" + finalDestination.getPort();
		String seqString = "" + sequence;
		String signal = Signal.ACK.getString();

		String payload = buildDelimitedString(ip, port, seqString, signal);

		return makePacket(payload);
	}

	public DatagramPacket makeSignalPacket(final Signal signal)
			throws TooShortPacketLengthException, SocketException {

		String header = buildHeaderString();
		String payload = buildDelimitedString(header, signal.getString());

		return makePacket(payload);
	}

	public List<RIPPacket> makePackets(final String data)
			throws TooShortPacketLengthException, SocketException {

		List<RIPPacket> packetList = new ArrayList<RIPPacket>();

		int maxPacketLength = Protocol.PACKET_LENGTH;
		int delimiterLength = Protocol.PACKET_DELIMITER.length();

		int signalSpace = Signal.PARTIAL.getString().length() + delimiterLength;
		int checksumSpace = delimiterLength + Protocol.CHECKSUM_LENGTH;

		int spaceLeft = maxPacketLength - signalSpace - checksumSpace
				- delimiterLength;

		String dataLeft = data;
		boolean done = false;

		while (!done) {
			String header = buildHeaderString();
			int spaceLeftForData = spaceLeft - header.length();

			if (spaceLeftForData <= 0) {
				throw new TooShortPacketLengthException("Packet length "
						+ maxPacketLength + " too short");
			}

			String packetData;
			String signal;

			if (dataLeft.length() > spaceLeftForData) {
				packetData = dataLeft.substring(0, spaceLeftForData);
				dataLeft = dataLeft.substring(spaceLeftForData);
				signal = Signal.PARTIAL.getString();
			} else {
				packetData = dataLeft;
				signal = Signal.REGULAR.getString();
				done = true;
			}

			String payload = buildDelimitedString(header, signal, packetData);
			String checksum = PacketUtils.getChecksum(payload);

			String finalPayload = buildDelimitedString(payload, checksum);

			DatagramPacket packet = makePacket(finalPayload);

			RIPPacket ripPacket = new RIPPacket(nextSequence, packet);

			packetList.add(ripPacket);
			++nextSequence;
		}

		return packetList;
	}

	private DatagramPacket makePacket(String payload)
			throws TooShortPacketLengthException, SocketException {

		int packetLength = Protocol.PACKET_LENGTH;
		byte[] byteData = payload.getBytes();

		if (byteData.length > packetLength) {
			throw new TooShortPacketLengthException("Packet length "
					+ packetLength + " too short");
		}

		return new DatagramPacket(byteData, byteData.length, relay);
	}

	private static String buildDelimitedString(String... values) {
		return PacketUtils.buildDelimitedString(Protocol.PACKET_DELIMITER,
				values);
	}
}